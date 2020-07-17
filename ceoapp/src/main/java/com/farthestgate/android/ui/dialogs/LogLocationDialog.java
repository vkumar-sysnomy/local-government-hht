package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.HttpClientHelper;
import com.farthestgate.android.helper.VRMLookupAdapter;
import com.farthestgate.android.model.Ceo;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.model.Street;
import com.farthestgate.android.model.StreetCPZ;
import com.farthestgate.android.model.database.LocationLogTable;
//import com.farthestgate.android.model.database.StreetsTable;
import com.farthestgate.android.ui.admin.BaseActivity;
import com.farthestgate.android.ui.messages.MessageResponse;
import com.farthestgate.android.ui.messages.VRMLookupMessageAsyncTask;
import com.farthestgate.android.ui.pcn.MessageViewActivity;
import com.farthestgate.android.ui.pcn.VRMLookupDetailActivity;
import com.farthestgate.android.ui.pcn.VRMLookupSummaryActivity;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.LogTimerTask;
import com.farthestgate.android.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 *
 */
public class LogLocationDialog extends DialogFragment implements MessageResponse {

    @Override
    public void onMsgResponse(String msgRes) {
        updatePaidParking(msgRes);
    }

    public interface OnNewLocationLoggedListener {
        public void onLocationChanged(String newStreet);
        public void onLocationUnchanged();
    }

    private AutoCompleteTextView newStreetName;
    private ProgressBar progressLoading;
    private OnNewLocationLoggedListener mListener;
    public static String previousStreetName = "";
    private Boolean streetChosen;
    public static List<String> streetNames;
    public static List<Street> streetCPZRows;
    ProgressDialog progressDialog;
    JSONObject  messageJsonObject = new JSONObject();
    JSONArray cpzMessages, streetMessages;
    public static boolean firstStreetName = false;
    public LogLocationDialog() {
        streetChosen = false;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_log_location_dialog, null);
        newStreetName = (AutoCompleteTextView) v.findViewById(R.id.streetLogSelectionText);
        progressLoading = (ProgressBar) v.findViewById(R.id.progressLoading);
        final Drawable x = getResources().getDrawable(R.drawable.cross_green_s);
        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        newStreetName.setCompoundDrawables(null, null, newStreetName.getText().toString().equals("") ? null : x, null);
        newStreetName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (newStreetName.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > newStreetName.getWidth() - newStreetName.getPaddingRight() - x.getIntrinsicWidth()) {
                    newStreetName.setText("");
                    newStreetName.setCompoundDrawables(null, null, null, null);
                }
                return false;
            }
        });
        newStreetName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnalyticsUtils.trackLocationPopUp();
                int x = 0;
                if (VisualPCNListActivity.currentStreet != null && VisualPCNListActivity.currentStreet.streetname != null) {
                    previousStreetName = VisualPCNListActivity.currentStreet.streetname;
                }
                String selRoad = ((TextView) view).getText().toString();
                for (String check : streetNames) {
                    if (check.equals(selRoad)) {
                        if (streetCPZRows != null) {
                            VisualPCNListActivity.currentStreet = new StreetCPZ(streetCPZRows.get(x));
                        } else {
                            VisualPCNListActivity.currentStreet = new StreetCPZ(GetStreetCPZRow(check));
                        }
                        streetChosen = true;
                        break;
                    }
                    x++;
                }
                if (CeoApplication.getLocationChangeLookUp()) {

                           new VRMLookupMessageAsyncTask(getActivity(), LogLocationDialog.this).execute("", VisualPCNListActivity.currentStreet.noderef);
                }
            }
        });

        newStreetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newStreetName.setCompoundDrawables(null, null, newStreetName.getText().toString().equals("") ? null : x, null);
            }
        });

        if (VisualPCNListActivity.currentStreet != null) {
            newStreetName.setText(VisualPCNListActivity.currentStreet.streetname);
        } else {
            VisualPCNListActivity.currentStreet = new StreetCPZ();
        }

        try {
            VisualPCNListActivity.lTimer.cancel();
        } catch (NullPointerException nex) {
            // this happens if Log Location is triggered just after log out process started
            //ignore
        }
        builder.setView(v)
                .setPositiveButton("Save", null)
                .setCancelable(VisualPCNListActivity.firstLogin)
                .setTitle("Location Log");
        final AlertDialog ad = builder.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button btnSave = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                btnSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try {
                            String stName = newStreetName.getText().toString();
                            if (stName.equals(VisualPCNListActivity.currentStreet.streetname)) {
                                if (stName.length() > 0) {
                                    if (streetChosen && !stName.equalsIgnoreCase(previousStreetName)) {
                                        if(VisualPCNListActivity.currentStreet.verrus_code != 0) {
                                            new HttpAsyncTask().execute(String.valueOf(VisualPCNListActivity.currentStreet.verrus_code));
                                        }
                                    }

                                    LocationLogTable logEntry = new LocationLogTable();
                                    logEntry.setLogTime(new DateTime());
                                    logEntry.setCeoName(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
                                    logEntry.setStreetName(stName);
                                    logEntry.setLattitude(VisualPCNListActivity.latitude);
                                    logEntry.setLongitude(VisualPCNListActivity.longitude);
                                    logEntry.save();

                                    mListener.onLocationUnchanged();
                                    VisualPCNListActivity.lTimer.cancel();
                                    VisualPCNListActivity.lTimer = new Timer();
                                    VisualPCNListActivity.lTimer.schedule(new LogTimerTask(), 180000l);
                                    LogLocationDialog.this.getDialog().dismiss();
                                    if(streetMessages !=null && streetMessages.length() > 0){
                                        messageJsonObject.put("streetmessages", streetMessages);
                                        //textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                                        Intent infoIntent = new Intent(getActivity(), MessageViewActivity.class);
                                        infoIntent.putExtra("street", VisualPCNListActivity.currentStreet.streetname);
                                        //CeoApplication.messageJsonObject = messageJsonObject;
                                        //infoIntent.putExtra("messageObj", "");
                                        int sync = DataHolder.get().setJSONObject(messageJsonObject);
                                        infoIntent.putExtra("messageObj:synccode", sync);
                                        infoIntent.putExtra("paidParkingMsg", false);
                                        startActivity(infoIntent);
                                    }
                                }
                            } else {
                                CroutonUtils.error(CroutonUtils.DURATION_MEDIUM, getActivity(), "Please enter a Location");
                                streetChosen = false;
                            }
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        ((BaseActivity) getContext()).OnException(getActivity(), exc, ErrorLocations.location005);
                        }
                    }
                });
            }
        });
        try {
            new BindLocationPopup().execute().get();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ad;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnNewLocationLoggedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNewLocationLoggedListener");
        }
    }

    private class BindLocationPopup extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            if(streetNames == null) {
                String locationData = DBHelper.GetDataFileContent("locationdataindex.json");
                Gson gson = new GsonBuilder().create();
                streetNames = (List<String>) gson.fromJson(locationData, List.class);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                ArrayAdapter<String> locationsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, streetNames);
                newStreetName.setAdapter(locationsAdapter);
                progressLoading.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                progressLoading.setVisibility(View.INVISIBLE);
            }
        }
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected List<String> doInBackground(String... args) {
            return HttpClientHelper.doGet(args[0]);
        }
        @Override
        protected void onPostExecute(List<String> rows) {
            super.onPostExecute(rows);
            try {
                int rowNum =0;
                if (rows.size() > 3) {
                    for(String row :rows){
                        if(rowNum >3){
                            String[] rowArray = row.split(",");
                            String vrm = rowArray[0];
                            String expiry = rowArray[5].replace("GMT","");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss");
                            Date expiryTime = sdf.parse(expiry.trim());
                            DBHelper.SaveVirtualPermit(vrm,Integer.valueOf(VisualPCNListActivity.currentStreet.streetusrn),expiryTime.getTime());
                        }
                        rowNum++;
                    }
                }

            } catch (Exception e) {
                Log.d("TAG", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //Reducing location popup data load time
    private Street GetStreetCPZRow(String streetName) {
        Street street = null;
        try {
            String jsonStreetData = GetContentForLocationData("streetindex.json");
            if (jsonStreetData != null) {
                try {
                    JSONArray streetJsonArray = new JSONArray(jsonStreetData);
                    StreetCPZ streetCPZ = null;
                    for (int y = 0; y < streetJsonArray.length(); y++) {
                        if (streetName.equals(streetJsonArray.getJSONObject(y).getString("streetname"))) {
                            streetCPZ = new StreetCPZ(streetJsonArray.getJSONObject(y));
                            if (streetCPZ != null)
                                street = new Street(streetCPZ);
                            break;
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return street;
    }
    public static String GetContentForLocationData(String fileName) {
        String jsonData = null;
        try {
            File streetDataFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + fileName);
            if (streetDataFile.exists()) {
                InputStream configStream = new FileInputStream(streetDataFile);
                jsonData = StringUtil.getStringFromInputStream(configStream);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }



    private void ExtractPaidParking(final String vrmParam, final String streetParam) {
        try {
            new AsyncTask() {
                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(getActivity());
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

                            Log.e("VRM URL", CeoApplication.VirtualPermitsUrl() + urlParams);


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
                                CroutonUtils.error(getActivity(), responseObject.getString("error"));
                            } else {
                                JSONArray responseArray = responseObject.getJSONArray("paidparking");
                                if(responseArray == null || responseArray.length() == 0){
                                    CroutonUtils.info(getActivity(), "There are no valid permits or PBP sessions for this location");
                                    return;
                                }
                                /*Type listType = new TypeToken<ArrayList<PaidParking>>() {}.getType();
                                paidParkingList = new Gson().fromJson(responseArray.toString(), listType);
                                vrmLookupAdapter = new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList);
                                listVRMLookup.setAdapter(vrmLookupAdapter);*/

                                /*ArrayList<String> filters = new ArrayList<String>();
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
                                }*/

                                /*spnFilterBy.setAdapter(null);
                                ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(VRMLookupSummaryActivity.this, android.R.layout.simple_spinner_item, filters);
                                filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnFilterBy.setAdapter(filterAdapter);*/

                                /*if (!vrmParam.isEmpty() && goForLookupDetails){
                                    Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, VRMLookupDetailActivity.class);
//                                    infoIntent.putExtra("paidParking",paidParkingList);
                                    int sync = DataHolder.get().setListData(paidParkingList);
                                    infoIntent.putExtra("paidParking:synccode", sync);
                                    infoIntent.putExtra("VRM", vrmParam);
                                    startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_PERMIT_TO_PCN);
                                }*/
                                if(responseObject.has("cpzmessages"))
                                    cpzMessages = responseObject.getJSONArray("cpzmessages");
                                if(responseObject.has("streetmessages"))
                                    streetMessages = responseObject.getJSONArray("streetmessages");

                                if(cpzMessages != null && cpzMessages.length() > 0){
                                    messageJsonObject.put("cpzmessages", cpzMessages);
                                    //textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                                }


                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            CroutonUtils.info(getActivity(), "An error occurred during getting the paid parking");
                        }
                    } else {
                        CroutonUtils.info(getActivity(), "Error in singleview service");
                    }
                }

            }.execute(null, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private void updatePaidParking(String response)
    {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject responseObject = new JSONObject(response);
                if (responseObject.has("errorcode")) {
                    CroutonUtils.error(getActivity(), responseObject.getString("error"));
                } else {
                    JSONArray responseArray = responseObject.getJSONArray("paidparking");
                    if(responseArray == null || responseArray.length() == 0){
                        CroutonUtils.info(getActivity(), "There are no valid permits or PBP sessions for this location");
                        return;
                    }
                                /*Type listType = new TypeToken<ArrayList<PaidParking>>() {}.getType();
                                paidParkingList = new Gson().fromJson(responseArray.toString(), listType);
                                vrmLookupAdapter = new VRMLookupAdapter(VRMLookupSummaryActivity.this, paidParkingList);
                                listVRMLookup.setAdapter(vrmLookupAdapter);*/

                                /*ArrayList<String> filters = new ArrayList<String>();
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
                                }*/

                                /*spnFilterBy.setAdapter(null);
                                ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(VRMLookupSummaryActivity.this, android.R.layout.simple_spinner_item, filters);
                                filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spnFilterBy.setAdapter(filterAdapter);*/

                                /*if (!vrmParam.isEmpty() && goForLookupDetails){
                                    Intent infoIntent = new Intent(VRMLookupSummaryActivity.this, VRMLookupDetailActivity.class);
//                                    infoIntent.putExtra("paidParking",paidParkingList);
                                    int sync = DataHolder.get().setListData(paidParkingList);
                                    infoIntent.putExtra("paidParking:synccode", sync);
                                    infoIntent.putExtra("VRM", vrmParam);
                                    startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_PERMIT_TO_PCN);
                                }*/
                    if(responseObject.has("cpzmessages"))
                        cpzMessages = responseObject.getJSONArray("cpzmessages");
                    if(responseObject.has("streetmessages"))
                        streetMessages = responseObject.getJSONArray("streetmessages");

                    if(cpzMessages != null && cpzMessages.length() > 0){
                        messageJsonObject.put("cpzmessages", cpzMessages);
                        //textCurrentStreetMsgView.setVisibility(View.VISIBLE);
                    }







                }

            } catch (Exception ex) {
                ex.printStackTrace();
                CroutonUtils.info(getActivity(), "An error occurred during getting the paid parking");
            }
        } else {
            CroutonUtils.info(getActivity(), "Error in singleview service");
        }

    }

}
