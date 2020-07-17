package com.farthestgate.android.ui.pcn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.helper.VRMLookupAdapter;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.ui.messages.MessageResponse;
import com.farthestgate.android.ui.messages.VRMLookupMessageAsyncTask;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class VRMLookupSummaryActivity extends Activity implements VRMLookupAdapter.MessageViewClickListener, MessageResponse {
    AutoCompleteTextView textCurrentStreet;
    ImageView textCurrentStreetMsgView;
    Button btnAllSessions,btnVRMSearch;
    EditText textVRMSearch;
    Spinner spnFilterBy;
    ListView listVRMLookup;
    ProgressDialog progressDialog;
    TextView textCapViewVRM, textCapViewType,textCapViewExp;
    ArrayList<PaidParking> paidParkingList;
    VRMLookupAdapter vrmLookupAdapter;

    JSONObject  messageJsonObject = new JSONObject();
    JSONArray cpzMessages, streetMessages;

    private Crouton noInternetCrouton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_vrm_lookup_summary);


        noInternetCrouton = CroutonUtils.noInternet(this);

        textCurrentStreet = (AutoCompleteTextView) findViewById(R.id.textCurrentStreet);
        textCurrentStreetMsgView = (ImageView) findViewById(R.id.textCurrentStreetMsgView);
        btnAllSessions = (Button) findViewById(R.id.btnAllSessions);
        btnVRMSearch = (Button) findViewById(R.id.btnVRMSearch);
        textVRMSearch = (EditText) findViewById(R.id.textVRMSearch);
        spnFilterBy = (Spinner) findViewById(R.id.spnFilterBy);
        listVRMLookup = (ListView) findViewById(R.id.listVRMLookup);
        textCapViewVRM = (TextView) findViewById(R.id.textCapViewVRM);
        textCapViewType = (TextView) findViewById(R.id.textCapViewType);
        textCapViewExp = (TextView) findViewById(R.id.textCapViewExp);
        textCurrentStreet.setText(VisualPCNListActivity.currentStreet.streetname);
        btnAllSessions.setOnClickListener(btnAllSessionsClick);
        btnVRMSearch.setOnClickListener(btnVRMSearchClick);
        textCapViewVRM.setOnClickListener(sortOnVRM);
        textCapViewType.setOnClickListener(sortOnType);
        textCapViewExp.setOnClickListener(sortOnExp);
        spnFilterBy.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        textVRMSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String s = arg0.toString();
                if (!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    textVRMSearch.setText("");
                    textVRMSearch.append(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        listVRMLookup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
                VRMLookupAdapter.Holder mViewHolder = (VRMLookupAdapter.Holder) view.getTag();
                PaidParking paidParking = mViewHolder.paidParking;
                Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, VRMLookupDetailActivity.class);
                List<PaidParking> paidParkings = new ArrayList<PaidParking>();
                paidParkings.add(paidParking);
//                infoIntent.putExtra("paidParking", paidParkings);
                int sync = DataHolder.get().setListData(paidParkings);
                infoIntent.putExtra("paidParking:synccode", sync);
                infoIntent.putExtra("VRM", paidParking.vrm);
                startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_PERMIT_TO_PCN);
            }
        });

        textCurrentStreetMsgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, MessageViewActivity.class);
                infoIntent.putExtra("street", VisualPCNListActivity.currentStreet.streetname);
//              CeoApplication.messageJsonObject = messageJsonObject;
//              infoIntent.putExtra("messageObj", "");
                int sync = DataHolder.get().setJSONObject(messageJsonObject);
                infoIntent.putExtra("messageObj:synccode", sync);
                infoIntent.putExtra("paidParkingMsg", false);
                startActivity(infoIntent);
            }
        });

    }

    private void ExtractPaidParking(final String vrmParam, final String streetParam) {
        try {
            new AsyncTask() {
                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(VRMLookupSummaryActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Extracting paid parking – Please wait");
                    progressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected Object doInBackground(Object[] params) {
                    String response = "";
                    try {
                        if (CeoApplication.VirtualPermitsUrl() != null && !CeoApplication.VirtualPermitsUrl().isEmpty()) {
                            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                            String deviceNumber = CeoApplication.getUUID();
                            String urlParams = "?device=" + deviceNumber;
                            if(!streetParam.isEmpty()) {
                                urlParams += "&ocn=" + streetParam;
                            }
                            if (!vrmParam.isEmpty()) {
                                if(CeoApplication.cashlessParkingProvider()!= null && CeoApplication.cashlessParkingProvider().equalsIgnoreCase("Ringo")){
                                    urlParams += "&vrm=" + vrmParam +  "&ocn=" + VisualPCNListActivity.currentStreet.noderef;
                                } else {
                                    urlParams += "&vrm=" + vrmParam;
                                }

                            }
                            response = AlfrescoComponent.executeGetRequest(CeoApplication.VirtualPermitsUrl() + urlParams);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    progressDialog.dismiss();
                    progressDialog = null;
                    String response = (String) o;
                    if (response != null && !response.isEmpty()) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            if (responseObject.has("errorcode")) {
                                CroutonUtils.error(VRMLookupSummaryActivity.this, responseObject.getString("error"));
                            } else {
                                JSONArray responseArray = responseObject.getJSONArray("paidparking");
                                if(responseArray == null || responseArray.length() == 0){
                                    CroutonUtils.info(VRMLookupSummaryActivity.this, "There are no valid permits or PBP sessions for this location");
                                    return;
                                }
                                Type listType = new TypeToken<ArrayList<PaidParking>>() {}.getType();
                                paidParkingList = new Gson().fromJson(responseArray.toString(), listType);
                                vrmLookupAdapter = new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList);
                                listVRMLookup.setAdapter(vrmLookupAdapter);

                                ArrayList<String> filters = new ArrayList<String>();
                                filters.add("None");
                                JSONArray groups = responseObject.getJSONArray("groups");
                                for (int i=0;i<groups.length();i++){
                                    filters.add((String)groups.get(i));
                                }
                                filters.add("Exp");

                                boolean goForLookupDetails= false;
                                for(PaidParking loc :paidParkingList) {
                                    if (loc.cashlesslocname != null & !loc.cashlesslocname.isEmpty()) {
                                        if(!filters.contains(loc.cashlesslocname))
                                            filters.add(loc.cashlesslocname);
                                    }
                                    if (loc.make != null && !loc.make.isEmpty()) {
                                        goForLookupDetails = true;
                                    }
                                }

                                spnFilterBy.setAdapter(null);
                                ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(VRMLookupSummaryActivity.this, android.R.layout.simple_spinner_item, filters);
                                filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnFilterBy.setAdapter(filterAdapter);

                                if (!vrmParam.isEmpty() && goForLookupDetails){
                                    Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, VRMLookupDetailActivity.class);
//                                    infoIntent.putExtra("paidParking",paidParkingList);
                                    int sync = DataHolder.get().setListData(paidParkingList);
                                    infoIntent.putExtra("paidParking:synccode", sync);
                                    infoIntent.putExtra("VRM", vrmParam);
                                    startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_PERMIT_TO_PCN);
                                }
                                if(responseObject.has("cpzmessages"))
                                    cpzMessages = responseObject.getJSONArray("cpzmessages");
                                if(responseObject.has("streetmessages"))
                                    streetMessages = responseObject.getJSONArray("streetmessages");

                                if(cpzMessages != null && cpzMessages.length() > 0){
                                    messageJsonObject.put("cpzmessages", cpzMessages);
                                    textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                                }
                                if(streetMessages !=null && streetMessages.length() > 0){
                                    messageJsonObject.put("streetmessages", streetMessages);
                                    textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            CroutonUtils.info(VRMLookupSummaryActivity.this, "An error occurred during getting the paid parking");
                        }
                    } else {
                        CroutonUtils.info(VRMLookupSummaryActivity.this, "Error in singleview service");
                    }
                }

            }.execute(null, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View.OnClickListener btnAllSessionsClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            textVRMSearch.setText("");
            listVRMLookup.setAdapter(null);

            if(hasInternet)
                new VRMLookupMessageAsyncTask(VRMLookupSummaryActivity.this, VRMLookupSummaryActivity.this).execute("", VisualPCNListActivity.currentStreet.noderef);
                //ExtractPaidParking("", VisualPCNListActivity.currentStreet.noderef);

        }
    };
    private View.OnClickListener sortOnVRM = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(paidParkingList !=null) {
//                listVRMLookup.setAdapter(null);
                Collections.sort(paidParkingList, new Comparator<PaidParking>() {
                    @Override
                    public int compare(PaidParking lhs, PaidParking rhs) {
                        return lhs.vrm.compareTo(rhs.vrm);
                    }
                });
//                listVRMLookup.setAdapter(new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList));
                vrmLookupAdapter.notifyDataSetChanged();
            }
        }
    };
    private View.OnClickListener sortOnType = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(paidParkingList !=null) {
//                listVRMLookup.setAdapter(null);
                Collections.sort(paidParkingList, new Comparator<PaidParking>() {
                    @Override
                    public int compare(PaidParking lhs, PaidParking rhs) {
                        return lhs.type.compareTo(rhs.type);
                    }
                });
//                listVRMLookup.setAdapter(new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList));
                vrmLookupAdapter.notifyDataSetChanged();
            }
        }
    };
    private View.OnClickListener sortOnExp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(paidParkingList !=null) {
//                listVRMLookup.setAdapter(null);
                Collections.sort(paidParkingList, new Comparator<PaidParking>() {
                    @Override
                    public int compare(PaidParking lhs, PaidParking rhs) {
                        return lhs.exp.compareTo(rhs.exp);
                    }
                });
//                listVRMLookup.setAdapter(new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList));

                vrmLookupAdapter.notifyDataSetChanged();
            }
        }
    };

    private View.OnClickListener btnVRMSearchClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String vrm = textVRMSearch.getText().toString();
            if(!vrm.isEmpty()) {
                listVRMLookup.setAdapter(null);

                if(hasInternet)
                    new VRMLookupMessageAsyncTask(VRMLookupSummaryActivity.this, VRMLookupSummaryActivity.this).execute(vrm, "");
                    //ExtractPaidParking(vrm, "");

            }else{
                CroutonUtils.info(VRMLookupSummaryActivity.this, "Please select VRM");
            }
        }
    };

    @Override
    public void vrmMsgClick(View view, PaidParking paidParking) {
        Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, MessageViewActivity.class);
        ArrayList<PaidParking> paidParkings = new ArrayList<PaidParking>();
        paidParkings.add(paidParking);
//        CeoApplication.paidParkings.add(paidParking);

        int sync = DataHolder.get().setListData(paidParkings);
        infoIntent.putExtra("paidParking:synccode", sync);
//        infoIntent.putExtra("paidParking", paidParkings);
        infoIntent.putExtra("VRM", paidParking.vrm);
        infoIntent.putExtra("paidParkingMsg", true);
        startActivity(infoIntent);
    }

    @Override
    public void onMsgResponse(String response) {
        CeoApplication.isVrmLook  = false;
        String vrmParam = textVRMSearch.getText().toString();
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                if (responseObject.has("errorcode")) {
                    CroutonUtils.error(VRMLookupSummaryActivity.this, responseObject.getString("error"));
                } else {
                    JSONArray responseArray = responseObject.getJSONArray("paidparking");
                    if (responseArray == null || responseArray.length() == 0) {
                        CroutonUtils.info(VRMLookupSummaryActivity.this, "There are no valid permits or PBP sessions for this location");
                        return;
                    }
                    Type listType = new TypeToken<ArrayList<PaidParking>>() {
                    }.getType();
                    paidParkingList = new Gson().fromJson(responseArray.toString(), listType);
                    vrmLookupAdapter = new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList);
                    listVRMLookup.setAdapter(vrmLookupAdapter);

                    ArrayList<String> filters = new ArrayList<String>();
                    filters.add("None");
                    JSONArray groups = responseObject.getJSONArray("groups");
                    for (int i = 0; i < groups.length(); i++) {
                        filters.add((String) groups.get(i));
                    }
                    filters.add("Exp");

                    boolean goForLookupDetails = false;
                    for (PaidParking loc : paidParkingList) {
                        if (loc.cashlesslocname != null & !loc.cashlesslocname.isEmpty()) {
                            if (!filters.contains(loc.cashlesslocname))
                                filters.add(loc.cashlesslocname);
                        }
                        if (loc.make != null && !loc.make.isEmpty()) {
                            goForLookupDetails = true;
                        }
                    }

                    spnFilterBy.setAdapter(null);
                    ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(VRMLookupSummaryActivity.this, android.R.layout.simple_spinner_item, filters);
                    filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnFilterBy.setAdapter(filterAdapter);

                    if (!vrmParam.isEmpty() && response != null && goForLookupDetails) {
                        Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, VRMLookupDetailActivity.class);
//                                    infoIntent.putExtra("paidParking",paidParkingList);
                        int sync = DataHolder.get().setListData(paidParkingList);
                        infoIntent.putExtra("paidParking:synccode", sync);
                        infoIntent.putExtra("VRM", vrmParam);
                        startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_PERMIT_TO_PCN);
                    }
                    if (responseObject.has("cpzmessages"))
                        cpzMessages = responseObject.getJSONArray("cpzmessages");
                    if (responseObject.has("streetmessages"))
                        streetMessages = responseObject.getJSONArray("streetmessages");

                    if (cpzMessages != null && cpzMessages.length() > 0) {
                        messageJsonObject.put("cpzmessages", cpzMessages);
                        textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                    }
                    if (streetMessages != null && streetMessages.length() > 0) {
                        messageJsonObject.put("streetmessages", streetMessages);
                        textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CroutonUtils.info(VRMLookupSummaryActivity.this, "An error occurred during getting the paid parking");
            }
        }
        else if(CeoApplication.isTimeOutException){
                CeoApplication.isTimeOutException = false;
                CroutonUtils.info(VRMLookupSummaryActivity.this, "We are unable to connect to the interface at the moment.  Please try again later. ");
        } else  {
            CroutonUtils.info(VRMLookupSummaryActivity.this, "Error in singleview service");
        }

    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            String filter = parent.getItemAtPosition(pos).toString();
            if (vrmLookupAdapter != null)
                vrmLookupAdapter.filter(filter);
        }
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case  CeoApplication.REQUEST_CODE_PERMIT_TO_PCN:
                if(data != null) {
                    boolean permitToPCN = data.getBooleanExtra("permitToPCN", false);
                    if(permitToPCN) {
                        String lookUpVRM = data.getStringExtra("lookUpVRM");
                        Intent intent = new Intent(VRMLookupSummaryActivity.this, PCNStartActivity.class);
                        intent.putExtra("lookUpVRM", lookUpVRM);
                        startActivityForResult(intent, CeoApplication.REQUEST_CODE_START_OBS);
                        finish();
                    }
                }
        }
    }

    @Override
    protected void onResume() {
        registerConnectivityReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        noInternetCrouton.clearCroutonsForActivity(this);
//        Crouton.cancelAllCroutons();
        unregisterReceiver(connectivityBR);
        super.onPause();
    }

    private void registerConnectivityReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityBR, filter);

    }

    private Boolean hasInternet = true;

    public BroadcastReceiver connectivityBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!DeviceUtils.isConnected(context)) {
                noInternetCrouton.show();
                hasInternet = false;
            } else {
                noInternetCrouton.cancel();
                noInternetCrouton.clearCroutonsForActivity(VRMLookupSummaryActivity.this);
                hasInternet = true;

                if(listVRMLookup.getAdapter() == null || listVRMLookup.getAdapter().getCount() == 0) {
                    textVRMSearch.setText("");
                    //ExtractPaidParking("", VisualPCNListActivity.currentStreet.noderef);
                    new VRMLookupMessageAsyncTask(VRMLookupSummaryActivity.this, VRMLookupSummaryActivity.this).execute("", VisualPCNListActivity.currentStreet.noderef);
//                }
                }
            }
        }
    };

}
