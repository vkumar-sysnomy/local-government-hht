package com.farthestgate.android.ui.pcn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;


import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.helper.HttpClientHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.helper.VRMAutomatedLookupTask;
import com.farthestgate.android.model.AdditionalInfo;
import com.farthestgate.android.model.Ceo;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.ContraventionSuffix;
import com.farthestgate.android.model.EnforcementPattern;
import com.farthestgate.android.model.FootwaySuffix;
import com.farthestgate.android.model.Location;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PDTicket;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.model.PermitBadge;
import com.farthestgate.android.model.Street;
import com.farthestgate.android.model.StreetCPZ;
import com.farthestgate.android.model.Suffix;
import com.farthestgate.android.model.TaxDisc;
import com.farthestgate.android.model.Timeplate;
import com.farthestgate.android.model.WarningNotice;
import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.model.database.NotesTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.TimerObjTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.components.CustomSpinner;
import com.farthestgate.android.ui.components.CustomSpinner.CustomSpinnerListener;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.ui.components.radialpickers.RadialTimePickerDialog;
import com.farthestgate.android.ui.components.timer.TimerObj;
import com.farthestgate.android.ui.dialogs.ForeignVehicleDialog;
import com.farthestgate.android.ui.dialogs.LogLocationDialog;
import com.farthestgate.android.ui.dialogs.MaxStayDialog;
import com.farthestgate.android.ui.dialogs.OSLocationDialog;
import com.farthestgate.android.ui.dialogs.ObservationTimeDialog;
import com.farthestgate.android.ui.dialogs.TimeplateDialog;
import com.farthestgate.android.ui.dialogs.VRMAutomatedLookupDialog;
import com.farthestgate.android.ui.dialogs.vehicle_logging.VehicleMakesActivity;
import com.farthestgate.android.ui.notes.NotesActivity;
import com.farthestgate.android.ui.notes.NotesListActivity;
import com.farthestgate.android.ui.notes.TextNoteActivity;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DateUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.farthestgate.android.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.farthestgate.android.ui.pcn.PCNUtils.getPosition;

public class PCNStartActivity extends NFCBluetoothActivityBase
        implements RadialTimePickerDialog.OnTimeSetListener,
        OSLocationDialog.LocationDialogListener,ObservationTimeDialog.OnObservationSelectionInterface,
        TimeplateDialog.OnTimeplateInfoListener, ForeignVehicleDialog.onVehicleCountryListener, MaxStayDialog.OnMaxStaySelectionInterface,
        VRMAutomatedLookupTask.VRMAutomatedLookupListener, VRMAutomatedLookupDialog.OnVRMLookupListener
{
    private RadioGroup      lstSuffixes;
    private ImageButton     btnNext;
    private ImageButton     btnNotes;
    private ImageButton     btnCancel;
    private ImageButton     btnPhotos;
    private CustomSpinner   customSpinner;
    private AutoCompleteTextView streetNameTextView;
    private TextView        contDescription;
    private EditText        registrationMark;
    private TextView        clearSuffix;
    private List<Contravention> contraventionDataList;
    private Suffix suffix;
    private List<ContraventionSuffix>        contraventionSuffixes;
    private List<FootwaySuffix>        footwaySuffixes;
    private Boolean         foreignSet = false;
    private Boolean         streetChosen;
    private String recentVRM ="";
    private long queryStartTime;
    SharedPreferenceHelper sph;
    int maxStayMinutes = 0;
    private int offencesCode;
    ArrayList<PaidParking> msgpaidParkings;
    String vrmMsgtext;
    boolean msgError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_pcnstart);

        recentVRM="";
        VisualPCNListActivity.currentActivity = 1;
        customSpinner       = (CustomSpinner) findViewById(R.id.spnOffence);
        lstSuffixes         = (RadioGroup) findViewById(R.id.rgSuffixes);
        btnNext             = (ImageButton) findViewById(R.id.btnNextStep);
        btnPhotos           = (ImageButton) findViewById(R.id.btnPhotos);
        btnNotes            = (ImageButton) findViewById(R.id.imgBtnNotes);
        btnCancel           = (ImageButton) findViewById(R.id.btnCancelPcn);
        streetNameTextView  = (AutoCompleteTextView) findViewById(R.id.txtStreetNames);
        contDescription     = (TextView) findViewById(R.id.txtContDescription);
        registrationMark    = (EditText) findViewById(R.id.VRM_Auto);
        clearSuffix        = (TextView) findViewById(R.id.clear_suffix);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-CondensedBold.otf");
        registrationMark.setTypeface(tf);
        streetNameTextView.setTypeface(tf);
        tf = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-Medium.otf");
        contDescription.setTypeface(tf);
        btnNext     .setOnClickListener(btnNextClick);
        btnPhotos   .setOnClickListener(btnPhotosClick);
        btnNotes    .setOnClickListener(btnNotesClick);
        btnCancel   .setOnClickListener(btnCancelClick);
        clearSuffix.setOnClickListener(clearSuffixClick);
        streetChosen =  false;
        sph = new SharedPreferenceHelper(this);
        pcnInfo = new PCN(new Random(DateTime.now().getMillis()).nextInt(Integer.MAX_VALUE));
        suffix = loadSuffixes();
        if (VisualPCNListActivity.currentStreet != null) {
            streetNameTextView.setText(VisualPCNListActivity.currentStreet.streetname);
            streetChosen = true;
            pcnInfo.location = new Location();
            pcnInfo.location.streetCPZ = VisualPCNListActivity.currentStreet;
            pcnInfo.location2 = new Location();
            pcnInfo.location2.streetCPZ = pcnInfo.location.streetCPZ;
        }
        ArrayAdapter<String> locationsAdapter  = new ArrayAdapter<String>(PCNStartActivity.this, android.R.layout.simple_dropdown_item_1line, LogLocationDialog.streetNames);
        streetNameTextView.setAdapter(locationsAdapter);
        List<String> contraventions = new ArrayList<String>();
        contraventionDataList = pcnInfo.location.streetCPZ.contraventionList;
        for (String c : pcnInfo.location.streetCPZ.contraventions.split(","))
        {
            contraventions.add(c);
        }
        Collections.sort(contraventions);
        Collections.sort(contraventionDataList,new Comparator<Contravention>() {
            @Override
            public int compare(Contravention lhs, Contravention rhs) {
                return lhs.contraventionCode.compareTo(rhs.contraventionCode);
            }
        });
        if (contraventionDataList.size() > 0) {
            pcnInfo.contravention = contraventionDataList.get(0);
            customSpinner.setEnabled(true);
            lstSuffixes.setEnabled(true);
            contDescription.setEnabled(true);
            customSpinner.setItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registrationMark.clearFocus();
                    hideKeyboard();
                }
            });
            customSpinner.setViews(contraventions, this);
            customSpinner.setCurrentChildChangedListener(csl);
            // Load Suffixes
            contraventionSuffixes = suffix.contraventionSuffixes;
            footwaySuffixes = suffix.footwaySuffixes;
            for (ContraventionSuffix sf:contraventionSuffixes)
            {
                for (char letter : pcnInfo.contravention.codeSuffixes.toCharArray())
                {
                    String sLetter = String.valueOf(letter).toUpperCase();
                    if (sf.item.startsWith(sLetter) && !sf.item.contains("J"))
                    {
                        RadioButton rd = new RadioButton(this);
                        rd.setText(sf.item);
                        lstSuffixes.addView(rd);
                    }
                }
            }
            lstSuffixes.setOnCheckedChangeListener(radClick);
        }
        else
        {
            btnNext.setEnabled(false);
            registrationMark.setEnabled(false);
            customSpinner.removeAllViews();
            customSpinner.setEnabled(false);
            contDescription.setText("");
            contDescription.setEnabled(false);
            lstSuffixes.removeAllViews();
            lstSuffixes.setEnabled(false);
            CroutonUtils.info(this,"You can not issue contraventions on this road. Please contact your supervisor");
        }
        final Drawable x = getResources().getDrawable(R.drawable.cross_green_m);
        x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
        streetNameTextView.setCompoundDrawables(null, null, streetNameTextView.getText().toString().equals("") ? null : x, null);
        streetNameTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (streetNameTextView.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > streetNameTextView.getWidth() - streetNameTextView.getPaddingRight() - x.getIntrinsicWidth()) {
                    streetNameTextView.setText("");
                    streetNameTextView.setCompoundDrawables(null, null, null, null);

                }
                return false;
            }
        });
        streetNameTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int x = 0;
                String selRoad = ((TextView) view).getText().toString();
                for (String check : LogLocationDialog.streetNames) {
                    if (check.equals(selRoad)) {
                        if (VisualPCNListActivity.currentStreet != null && VisualPCNListActivity.currentStreet.streetname != null) {
                            LogLocationDialog.previousStreetName = VisualPCNListActivity.currentStreet.streetname;
                        }
                        //Reducing location popup data load time
                        // VisualPCNListActivity.currentStreet = new StreetCPZ(LogLocationDialog.streetCPZRows.get(x));
                        if (LogLocationDialog.streetCPZRows != null) {
                            VisualPCNListActivity.currentStreet = new StreetCPZ(LogLocationDialog.streetCPZRows.get(x));
                        } else {
                            VisualPCNListActivity.currentStreet = new StreetCPZ(GetStreetCPZRow(check));
                        }
                        pcnInfo.location.streetCPZ = VisualPCNListActivity.currentStreet;
                        pcnInfo.location2 = new Location();
                        pcnInfo.location2.streetCPZ = pcnInfo.location.streetCPZ;
                        final List<String> contraventions = new ArrayList<String>();
                        for (String c : pcnInfo.location.streetCPZ.contraventions.split(",")) {
                            contraventions.add(c);
                        }
                        Collections.sort(contraventions);
                        contraventionDataList.clear();
                        contraventionDataList.addAll(pcnInfo.location.streetCPZ.contraventionList);
                        Collections.sort(contraventionDataList, new Comparator<Contravention>() {
                            @Override
                            public int compare(Contravention lhs, Contravention rhs) {
                                return lhs.contraventionCode.compareTo(rhs.contraventionCode);
                            }
                        });
                        if (contraventionDataList.size() > 0) {
                            //TODO: refactor this !!
                            btnNext.setEnabled(true);
                            registrationMark.setEnabled(true);

                            //set contravention code value
                           String num = pcnInfo.contravention.contraventionCode;
                           final int pos = getPosition(num, contraventionDataList);

                            if(pos==-1)
                            {
                                pcnInfo.contravention = contraventionDataList.get(0);

                            }else{
                                pcnInfo.contravention = contraventionDataList.get(pos);

                            }

                            Log.i("Position="+pos,pcnInfo.contravention.contraventionCode);

                            lstSuffixes.setEnabled(true);
                            customSpinner.setEnabled(true);
                            contDescription.setEnabled(true);
                            customSpinner.setCurrentPosition(pos);
                            customSpinner.setViews(contraventions, PCNStartActivity.this);

                            customSpinner.invalidate();
                            customSpinner.setCurrentChildChangedListener(csl);



                            customSpinner.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (customSpinner.currentView!=null){
                                        Log.i("getTop",""+customSpinner.currentView.getTop());
                                        customSpinner.smoothScrollTo(0,(customSpinner.currentView).getTop());
                                    }else{
                                        customSpinner.smoothScrollTo(0,0);
                                    }

                                }
                            });



                            //pcnInfo.contravention = contraventionDataList.get(customSpinner.getCurrentOffenceId());

                            // Load Suffixes
                            contraventionSuffixes = suffix.contraventionSuffixes;
                            footwaySuffixes = suffix.footwaySuffixes;
                            for (ContraventionSuffix sf:contraventionSuffixes) {
                                for (char letter : pcnInfo.contravention.codeSuffixes.toCharArray()) {
                                    String sLetter = String.valueOf(letter).toUpperCase();
                                    if (sf.item.startsWith(sLetter) && !sf.item.contains("J")) {
                                        RadioButton rd = new RadioButton(PCNStartActivity.this);
                                        rd.setText(sf.item);
                                        lstSuffixes.addView(rd);
                                    }
                                }
                            }
                            lstSuffixes.setOnCheckedChangeListener(radClick);

                        } else {
                            btnNext.setEnabled(false);
                            customSpinner.removeAllViews();
                            customSpinner.setEnabled(false);
                            registrationMark.setEnabled(false);
                            contDescription.setText("");
                            contDescription.setEnabled(false);
                            lstSuffixes.removeAllViews();
                            lstSuffixes.setEnabled(false);
                            CroutonUtils.info(PCNStartActivity.this, "You can not issue contraventions on this road");
                            CroutonUtils.info(PCNStartActivity.this, "Please contact your supervisor");
                        }
                        streetChosen = true;
                        if (streetChosen && !selRoad.equalsIgnoreCase(LogLocationDialog.previousStreetName)) {
                            if (VisualPCNListActivity.currentStreet.verrus_code != 0) {
                                new HttpAsyncTask().execute(String.valueOf(VisualPCNListActivity.currentStreet.verrus_code));
                            }
                        }
                        break;
                    }
                    x++;
                }
            }
        });
        streetNameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String stName = streetNameTextView.getText().toString();
                if (!streetChosen || stName.length() == 0) {
                    CroutonUtils.error(CroutonUtils.DURATION_MEDIUM, PCNStartActivity.this, "Please select a Location");
                } else {
                    if (!stName.equals(VisualPCNListActivity.currentStreet.streetname)) {
                        streetChosen = false;
                        CroutonUtils.error(CroutonUtils.DURATION_MEDIUM, PCNStartActivity.this, "Please enter a valid Location");
                    }
                }
            }
        });
        streetNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                streetNameTextView.setCompoundDrawables(null, null, streetNameTextView.getText().toString().equals("") ? null : x, null);
            }
        });
        registrationMark.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String s = arg0.toString();
                if (!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    registrationMark.setText("");
                    registrationMark.append(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                String vrm = registrationMark.getText().toString();
                if (recentVRM.length() == 0) {
                    recentVRM = vrm;
                } else {
                    if (!vrm.equalsIgnoreCase(recentVRM)) {
                        recentVRM = vrm;
                    } else {
                        return;
                    }
                }
                if (vrm.length() > 0 && IsValidVRM(vrm)) {
                    if (CeoApplication.PcnReloadHours() > 0) {
                        TimerObjTable timerObjTable = GetPreviousObservation(vrm);
                        if (timerObjTable != null) {
                            TimerObj timerObj = new TimerObj(timerObjTable);
                            PrepareViewObservation(timerObj);
                        }
                    }
                }
            }
        });

        if(getIntent().hasExtra("anprVrm"))
            registrationMark.setText(getIntent().getStringExtra("anprVrm"));
         else if(getIntent().hasExtra("lookUpVRM"))
            registrationMark.setText(getIntent().getStringExtra("lookUpVRM"));

        registrationMark.setSelection(registrationMark.length());
    }

    private TextView.OnClickListener clearSuffixClick = new TextView.OnClickListener(){
        @Override
        public void onClick(View v) {
            lstSuffixes.clearCheck();
            pcnInfo.contravention.selectedSuffix = "";
            pcnInfo.contravention.codeSuffixDescription = "";
            if (pcnInfo.contravention.contraventionCode.equals("19") || pcnInfo.contravention.contraventionCode.equals("40"))
                pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;

            clearSuffix.setVisibility(View.GONE);
        }
    };

    private void PrepareViewObservation(final TimerObj timerObj){
        final AlertDialog viewObservation = new AlertDialog.Builder(this).create();
        viewObservation.setCancelable(false);
        viewObservation.setTitle("Load previous PCN");
        viewObservation.setMessage("The VRM already has a PCN issued against it that has not been printed.\n" +  "Want to load this PCN ?");
        viewObservation.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewObservation.dismiss();
                recentVRM="";
                ViewObservation(timerObj);
            }
        });
        viewObservation.setButton(AlertDialog.BUTTON_NEGATIVE,"NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recentVRM="";
                viewObservation.dismiss();
            }
        });
        if(!viewObservation.isShowing())viewObservation.show();

    }
    private TimerObjTable GetPreviousObservation(String vrm) {
        TimerObjTable timerObjTable = null;
        Gson gson = new GsonBuilder().create();
        List<TimerObjTable> timerObjRows = DBHelper.getTimers();
        for (TimerObjTable timerObjRow : timerObjRows) {
            long elapsedHours =   (DateTime.now().getMillis()-timerObjRow.getTimerStartTimeMillis())/(1000*60*60);
            //long elapsedHours =   (Utils.getTimeNow()-timerObjRow.getTimerStartTime())/(1000*60*60);
            if (elapsedHours < CeoApplication.PcnReloadHours()) {
                PCN pcn = gson.fromJson(timerObjRow.getTimerJSON(), PCN.class);
                if (vrm.equalsIgnoreCase(pcn.registrationMark) && pcn.issueTime == 0) {
                    timerObjTable = timerObjRow;
                    break;
                }
            }
        }
        return timerObjTable;
    }
    private void ViewObservation(TimerObj timerObj)
    {
        Intent infoIntent = new Intent(PCNStartActivity.this, PCNLoggingActivity.class);
        infoIntent.putExtra("timerObj",timerObj);
        infoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        startActivityForResult(infoIntent, CeoApplication.RESULT_CODE_PCNLIST);
    }
    private boolean IsValidVRM(String vrm) {
        boolean valid = false;
        if (isVRMUK(vrm) || foreignSet)
            valid = true;
        else {
            if (vrm.equalsIgnoreCase("TEST"))
                valid = true;
        }
        return valid;
    }

    /*CustomSpinnerListener csl = new CustomSpinnerListener() {
        @Override
        public void onScrollChanged(int offenceCode) {
            try {
                Contravention contravention = contraventionDataList.get(offenceCode);
                JSONObject minimumObservationTimesObject =null;
                JSONObject minimumObservationTimesContent = CeoApplication.GetDataFileContentAsObject("minimumobservationtimes.json");
                if (minimumObservationTimesContent != null) {
                    JSONArray minimumObservationTimesOptions = minimumObservationTimesContent.getJSONArray("grace");
                    for (int i = 0; i < minimumObservationTimesOptions.length(); i++) {
                        if (minimumObservationTimesOptions.getJSONObject(i).getString("contraventionCode").equalsIgnoreCase(contravention.contraventionCode)) {
                            minimumObservationTimesObject = minimumObservationTimesOptions.getJSONObject(i);
                            break;
                        }
                    }
                }
                boolean maxStayAvailable = false;
                if(minimumObservationTimesObject !=null) {
                    maxStayAvailable = minimumObservationTimesObject.has("maxStay") && minimumObservationTimesObject.getString("maxStay").equalsIgnoreCase("yes") ? true : false;
                }
                if (!maxStayAvailable) {
                    maxStayMinutes = 0;
                    completeCSL(offenceCode);
                } else {
                    JSONArray maxStays = minimumObservationTimesContent.getJSONArray("maxstay");
                    MaxStayDialog maxStayDialog = new MaxStayDialog(maxStays, offenceCode);
                    maxStayDialog.show(getFragmentManager(), "");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };*/

    CustomSpinnerListener csl = new CustomSpinnerListener() {
        @Override
        public void onScrollChanged(int offenceCode) {

            try {
                clearSuffix.setVisibility(View.GONE);
                offencesCode = offenceCode;
                completeCSL(offenceCode);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void OnMaxStayOptionSelected(int offenceCode, int maxStayMinutes) {
        this.maxStayMinutes = maxStayMinutes;
        Contravention contravention = contraventionDataList.get(offencesCode);
        int waitingTime = getAuthorityMinimumForContravention(contravention.contraventionCode);
        // completeCSL(offenceCode);
        Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
        pcnInfo.observationTime = waitingTime * ONE_MIN;;
        VehicleMakesActivity.currentPCN = pcnInfo;
        startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
        /*ObservationTimeDialog observationTimeDialog = new ObservationTimeDialog(pcnInfo.contravention.contraventionCode, maxStayMinutes);
        observationTimeDialog.show(getFragmentManager(), "");*/
    }

    private void completeCSL(int offenceCode){
        //JK : street enforcement pattern
        Log.e("offence code", String.valueOf(offenceCode));
        JSONObject verifyResult;
        try {
            verifyResult = VerifyEnforcementHours(contraventionDataList.get(offenceCode));
        }
        catch (IndexOutOfBoundsException ie) {
            verifyResult = VerifyEnforcementHours(contraventionDataList.get(0));
            Log.e("error in CSL", ie.toString());
        }
        try {
            if (verifyResult != null) {
                if (verifyResult.has("result") && verifyResult.getString("result").equalsIgnoreCase("NOK")) {
                    AlertDialog alertDialog = new AlertDialog.Builder(PCNStartActivity.this).create();
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle("Contravention not allowed");
                    alertDialog.setMessage(verifyResult.getString("message"));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if (!alertDialog.isShowing()) alertDialog.show();
                    //return;
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }

        lstSuffixes.removeAllViews();
        try
        {
            pcnInfo.contravention = contraventionDataList.get(offenceCode);
        }
        catch (IndexOutOfBoundsException ie) {
            pcnInfo.contravention = contraventionDataList.get(0);
        }
        contDescription.setText(pcnInfo.contravention.contraventionDescription);

        List<String> arrList = new ArrayList<>();
        for(ContraventionSuffix sf : contraventionSuffixes){
            arrList.add(sf.item);
        }
        if (pcnInfo.contravention.contraventionCode.contains("61") ||
                pcnInfo.contravention.contraventionCode.contains("62")) {

            arrList.clear();
            for(FootwaySuffix fs : footwaySuffixes){
                arrList.add(fs.footwayitem);
            }
        }
        for (String sf:arrList)
        {
            for (char letter : pcnInfo.contravention.codeSuffixes.toCharArray())
            {
                String sLetter = String.valueOf(letter).toUpperCase();
                if (sf.startsWith(sLetter) && !sf.contains("J"))
                {
                    RadioButton rd = new RadioButton(PCNStartActivity.this);
                    rd.setText(sf);
                    lstSuffixes.addView(rd);
                }
            }
        }
        pcnInfo.contravention.selectedSuffix = "";
        pcnInfo.contravention.codeSuffixDescription = "";
    }

    private JSONObject VerifyEnforcementHours(Contravention selectedContravention){
        JSONObject resultObject = new JSONObject();
        EnforcementPattern availableEnforcementPattern=null;
        int where =0;
        try {
            List<EnforcementPattern> enforcementPatterns = selectedContravention.contraventionEnforcementPattern;
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            Date currentDate = new Date();
            String dayOfTheWeek = sdf.format(currentDate);
            String currentTime="";
            if(enforcementPatterns.size()>0) {
                for (EnforcementPattern enforcementPattern : enforcementPatterns) {
                    if (enforcementPattern.enforcementDay.equalsIgnoreCase(dayOfTheWeek)) {
                        availableEnforcementPattern = enforcementPattern;
                        where = 1;
                        sdf = new SimpleDateFormat("hh:mma");
                        currentTime = sdf.format(currentDate);
                        break;
                    }
                }
            }else{
                enforcementPatterns = VisualPCNListActivity.currentStreet.streetEnforcementPattern;
                for (EnforcementPattern enforcementPattern : enforcementPatterns) {
                    if (enforcementPattern.enforcementDay.equalsIgnoreCase(dayOfTheWeek)) {
                        availableEnforcementPattern = enforcementPattern;
                        where = 2;
                        sdf = new SimpleDateFormat("HH:mm");
                        currentTime = sdf.format(currentDate);
                        break;
                    }
                }
            }
            if(availableEnforcementPattern !=null){
                boolean itIsOk = checkTime(availableEnforcementPattern.enforcementStartTime,availableEnforcementPattern.enforcementEndTime,currentTime, where);
                if(itIsOk){
                    resultObject.put("result","OK");
                    resultObject.put("message","");
                }else{
                    resultObject.put("result","NOK");
                    resultObject.put("message","The contravention " + selectedContravention.contraventionCode + " can only be enforced between " +  availableEnforcementPattern.enforcementStartTime + " and " + availableEnforcementPattern.enforcementEndTime + " on "+ availableEnforcementPattern.enforcementDay );
                }

            }else{
                resultObject.put("result","OK");
                resultObject.put("message","");
            }

        }catch (Exception ex){
            ex.printStackTrace();
            try {
                resultObject.put("result", "OK");
                resultObject.put("message", "");
            }catch (Exception e){
                e.printStackTrace();
            }
            return resultObject;
        }
        return resultObject;
    }

    private boolean checkTime(String startTime, String endTime, String time, int where) {
        boolean result;
        try {
            String timeFormat = where == 2 ? "HH:mm" : "hh:mma";
            Date timeStart = new SimpleDateFormat(timeFormat).parse(startTime);
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(timeStart);

            Date timeEnd = new SimpleDateFormat(timeFormat).parse(endTime);
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(timeEnd);
            calendarEnd.add(Calendar.DATE, 1);

            Date currentTime = new SimpleDateFormat(timeFormat).parse(time);
            Calendar calendarCurrent = Calendar.getInstance();
            calendarCurrent.setTime(currentTime);
            calendarCurrent.add(Calendar.DATE, 1);

            Date x = calendarCurrent.getTime();
            if (x.after(calendarStart.getTime()) && x.before(calendarEnd.getTime())) {
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = true;
        }
        return result;
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed()
    {
        cancelObservation();
    }

    private RadioGroup.OnCheckedChangeListener radClick = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId)
        {
            if (group != null)
            {
                for (View v:group.getTouchables())
                {
                    if(((RadioButton)v).isChecked())
                    {
                        clearSuffix.setVisibility(View.VISIBLE);

                        String suffix = ((TextView)v).getText().toString().toLowerCase();
                        pcnInfo.contravention.selectedSuffix = suffix.subSequence(0,1).toString();
                        pcnInfo.contravention.codeSuffixDescription = suffix.substring(2);

                        if (pcnInfo.contravention.contraventionCode.equals("19") || pcnInfo.contravention.selectedSuffix.toLowerCase().equals("s"))
                            pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;

                        if (pcnInfo.contravention.contraventionCode.equals("40") || pcnInfo.contravention.selectedSuffix.toLowerCase().equals("s"))
                            pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;

                        break;
                    }
                }
                hideKeyboard();
            }
        }
    };


    private void cancelObservation()
    {
        final AlertDialog checkCancel = new AlertDialog.Builder(this).create();
        checkCancel.setCancelable(false);
        checkCancel.setTitle("Cancel Observation ?");
        checkCancel.setButton(AlertDialog.BUTTON_POSITIVE,"YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //restrict the user to take only one diagram note
                pcnInfo.diagramNoteTaken = false;
                deleteUnusedPhotos(pcnInfo.observationNumber);
                deleteUnusedNotes(pcnInfo.observationNumber);
                //pcn disorder problem
                if(pcnInfo.pcnNumber !=null && pcnInfo.pcnNumber.length()>0){
                    DBHelper.releasePCNNumber(pcnInfo.pcnNumber);
                }
                setResult(CeoApplication.RESULT_CODE_OBS_CANCEL);
                checkCancel.dismiss();
                finish();
            }
        });
        checkCancel.setButton(AlertDialog.BUTTON_NEGATIVE,"NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                checkCancel.dismiss();
            }
        });
        checkCancel.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode)
        {
            case CeoApplication.RESULT_CODE_ERROR:
            {
                setResult(resultCode);
                finish();
                break;
            }
            case CeoApplication.RESULT_CODE_VEHICLE_DIALOG:
            {
                VehicleMakesActivity.currentPCN.logTime = DateTime.now().getMillis();
                Intent loggingIntent = new Intent(this,PCNLoggingActivity.class);
                // set pcn info
                setContraventionCharge();
                if(pcnIssueMode == AppConstant.pcnIssueMode.PAY_AND_DISPLAY){
                    FragmentManager fm = getSupportFragmentManager();
                    DateTime now = DateTime.now();
                    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                            .newInstance(this, now.getHourOfDay(), now.getMinuteOfHour(),
                                    DateFormat.is24HourFormat(this),
                                    getResources().getString(R.string.pdReference),
                                    getResources().getString(R.string.pdButton), "");
                    timePickerDialog.setThemeDark(true);
                    timePickerDialog.show(fm, "");
                }else {
                    if (pcnInfo.observationTime == 0) {
                        startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_INSTANT);
                    } else {
                        loggingIntent.putExtra("timer", pcnInfo.observationTime);
                        startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_TIMED_OBS);
                    }
                }
                break;
                /*switch (pcnInfo.contravention.contraventionType)
                {
                    case AppConstant.CONTRAVENTION_INSTANT:
                    {
                        startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_INSTANT);
                        break;
                    }
                    case AppConstant.CONTRAVENTION_LOADING: //Sufix loading
                    {
                        loggingIntent.putExtra("timer", pcnInfo.observationTime);
                        startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_TIMED_OBS);
                        break;
                    }
                    case AppConstant.CONTRAVENTION_OBSERVATION: // 5min grace
                    {
                        pcnInfo.observationTime = FIVE_MINS;
                        loggingIntent.putExtra("timer", pcnInfo.observationTime);
                        startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_TIMED_OBS);
                        break;
                    }
                    case AppConstant.CONTRAVENTION_PD: //Meter
                    {
                        FragmentManager fm = getSupportFragmentManager();
                        DateTime now = DateTime.now();
                        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                                .newInstance(this, now.getHourOfDay(), now.getMinuteOfHour(),
                                        DateFormat.is24HourFormat(this),
                                        getResources().getString(R.string.pdReference),
                                        getResources().getString(R.string.pdButton), "");
                        timePickerDialog.setThemeDark(true);
                        timePickerDialog.show(fm, "");
                    }
                    case AppConstant.CONTRAVENTION_DUAL_LOG: // Dual login
                    {

                        if (pcnInfo.contravention.contraventionCode.equals("05"))
                        {
                            FragmentManager fm = getSupportFragmentManager();
                            DateTime now = DateTime.now();
                            RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                                    .newInstance(this, now.getHourOfDay(), now.getMinuteOfHour(),
                                            DateFormat.is24HourFormat(this),
                                            getResources().getString(R.string.pdReference),
                                            getResources().getString(R.string.pdButton), "");
                            timePickerDialog.setThemeDark(true);
                            timePickerDialog.show(fm, "");
                        }
                        else
                        {
                            loggingIntent.putExtra("timer", pcnInfo.observationTime);
                            startActivityForResult(loggingIntent, CeoApplication.REQUEST_CODE_TIMED_OBS);
                        }
                        break;
                    }
                }
                break;*/
            }
            case CeoApplication.RESULT_CODE_PCNLOGGING:
            {
                break;
            }
            case CeoApplication.RESULT_CODE_PCNLIST:
            {
                setResult(resultCode, data);
                finish();
                break;
            }
            case CeoApplication.RESULT_CODE_NOTES:
            {
                //TODO: detect empty note
                NotesTable newNote = new NotesTable();
                newNote.setCeoNumber(DBHelper.getCeoUserId());
                newNote.setNoteDate(DateTime.now().getMillis());
                newNote.setSubjectLine(data.getStringExtra("subject"));
                //pcn disorder problem
                if(pcnInfo.pcnNumber !=null && pcnInfo.pcnNumber.length()>0){
                    newNote.setPcnNumber(pcnInfo.pcnNumber);
                }
                //restrict the user to take only one diagram note
                if(data.hasExtra("diagramNoteTaken")) {
                    pcnInfo.diagramNoteTaken = data.getBooleanExtra("diagramNoteTaken", false);
                }
                newNote.setNoteText(data.getStringExtra("text"));
                newNote.setPage(0);
                String filePath = data.getStringExtra("path");
                newNote.setFileName(filePath);
                newNote.setObservation(pcnInfo.observationNumber);
                newNote.save();
                break;
            }
            case CeoApplication.RESULT_CODE_NEWIMAGE:
            {
                break;
            }
            case CeoApplication.RESULT_CODE_ENDOFDAY:
            {
                finish();
                break;
            }
            case -1:
            {
                switch (requestCode) {
                    case CeoApplication.RESULT_CODE_NEWIMAGE:
                        if (resultCode == RESULT_OK) {
                            Runtime.getRuntime().gc();
                            Runtime.getRuntime().freeMemory();
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            File currentImageFile = new File(data.getStringExtra("path"));
                            PCNPhotoTable pcnPhoto = new PCNPhotoTable();
                            pcnPhoto.setCEO_Number(DBHelper.getCeoUserId());
                            pcnPhoto.setFileName(currentImageFile.getName());
                            pcnPhoto.setObservation(pcnInfo.observationNumber);
                            pcnPhoto.setTimestamp(timeStamp);
                            pcnPhoto.setPcnSession(currentSession);
                            pcnPhoto.save();
                        }
                        break;
                }
            }
            case CeoApplication.RESULT_CODE_OBS_CANCEL:
            {
                pcnInfo.pdTicketsList = new ArrayList<PDTicket>();
                pcnInfo.permitBadgeList = new ArrayList<PermitBadge>();
                pcnInfo.taxDiscs = new ArrayList<TaxDisc>();
                pcnInfo.additionalInfo = new AdditionalInfo();
                registrationMark.setText("");
                break;
            }
            case CeoApplication.REQUEST_CODE_MESSAGEVIEW:
            {
                if (CeoApplication.getCheckPermitSession()) {
                    Log.e("getCheckPermitSession","true");
                    vrmLookUpDialogAfterShowingMessage( msgpaidParkings, vrmMsgtext,  msgError);
                  } else {
                    Log.e("getCheckPermitSession","false");
                    checkSpecialVehicleType();
                }


                break;
            }

            default:
            {
                //Dialog was cancelled - continue
                break;
            }
        }
    }

    private View.OnClickListener btnNotesClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            PopupMenu popup = new PopupMenu(PCNStartActivity.this, btnNotes);
            popup.getMenuInflater().inflate(R.menu.notes_pop, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item)
                {
                    Intent notesIntent;
                    boolean isItDiagram = false;
                    if (item.getItemId() == R.id.mnuDrwNotes)
                    {
                        isItDiagram = true;
                        notesIntent = new Intent(PCNStartActivity.this, NotesActivity.class);

                    } else
                    {
                        notesIntent = new Intent(PCNStartActivity.this, TextNoteActivity.class);
                    }
                    //restrict the user to take only one diagram note
                    if(isItDiagram && pcnInfo.diagramNoteTaken){
                        CroutonUtils.info(PCNStartActivity.this, "You can take a single diagram note per PCN");
                        return true;
                    }
                    if (pcnInfo.pcnNumber != null && pcnInfo.pcnNumber.length() > 0) {
                        notesIntent.putExtra("pcn", pcnInfo.pcnNumber);
                    }
                    notesIntent.putExtra("obs", pcnInfo.observationNumber);
                    startActivityForResult(notesIntent, CeoApplication.REQUEST_CODE_ADD_NOTE);
                    return true;
                }
            });

            popup.show();
        }
    };

    private View.OnClickListener btnCancelClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            cancelObservation();
        }
    };

    private View.OnClickListener btnPhotosClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent imageIntent = new Intent(PCNStartActivity.this,CameraActivity.class);
            imageIntent.putExtra("obs", pcnInfo.observationNumber);
            //pcn disorder problem
            //imageIntent.putExtra("pcn", pcnInfo.pcnNumber);
            startActivityForResult(imageIntent,CeoApplication.RESULT_CODE_NEWIMAGE);
        }
    };
    private View.OnClickListener btnNextClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            /*if (isUserInputValid()) {
                if (ApplicableForPayByPhone() && VisualPCNListActivity.currentStreet.verrus_code != 0) {
                    try {
                        boolean validExistingParking = false;
                        Date parkingExpiryTime = null;
                        JSONObject objParking = CheckForValidExistingParking();
                        if (objParking.has("parkingFound"))
                            validExistingParking = objParking.getBoolean("parkingFound");
                        if (objParking.has("parkingExpiryTime"))
                            parkingExpiryTime = new Date(objParking.getLong("parkingExpiryTime"));
                        if (!validExistingParking) {
                            CroutonUtils.info(PCNStartActivity.this, CeoApplication.QueryingPayByPhone());
                            new EnquireFromPayByPhone().execute(String.valueOf(VisualPCNListActivity.currentStreet.verrus_code));
                        } else {
                            MoveToBackStep(parkingExpiryTime);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    MoveToNextStep();
                }
            }*/

//            checkSpecialVehicleType();
            if (isUserInputValid()) {

                checkAutomatedVRMLookup();
            }
        }
    };

    private void checkAutomatedVRMLookup(){
        if (DeviceUtils.isConnected(PCNStartActivity.this)) {
            String vrm = registrationMark.getText().toString().trim();
            VRMAutomatedLookupTask vrmAutomatedLookupTask =
                    new VRMAutomatedLookupTask(PCNStartActivity.this, vrm, PCNStartActivity.this);
            vrmAutomatedLookupTask.execute();
        } else{
            showVRMAutomatedLookupErrorDialog("No internet connectivity available at the time of search", false);
        }
    }


    private void btnContineClicked(){
        if (ApplicableForPayByPhone() && VisualPCNListActivity.currentStreet.verrus_code != 0) {
            try {
                boolean validExistingParking = false;
                Date parkingExpiryTime = null;
                JSONObject objParking = CheckForValidExistingParking();
                if (objParking.has("parkingFound"))
                    validExistingParking = objParking.getBoolean("parkingFound");
                if (objParking.has("parkingExpiryTime"))
                    parkingExpiryTime = new Date(objParking.getLong("parkingExpiryTime"));
                if (!validExistingParking) {
                    CroutonUtils.info(PCNStartActivity.this, CeoApplication.QueryingPayByPhone());
                    new EnquireFromPayByPhone().execute(String.valueOf(VisualPCNListActivity.currentStreet.verrus_code));
                } else {
                    MoveToBackStep(parkingExpiryTime);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            MoveToNextStep();
        }
    }

   /* private void MoveToNextStep(){
        LogLocation();
        pcnInfo.registrationMark = registrationMark.getText().toString();
        //TODO: this is a rubbish fix - refactor
        pcnInfo.contravention.contraventionType = DBHelper.getEnforcementType(pcnInfo.contravention.contraventionCode);

        switch (pcnInfo.contravention.contraventionType) {
            case AppConstant.CONTRAVENTION_INSTANT:
            case AppConstant.CONTRAVENTION_OBSERVATION:
            case AppConstant.CONTRAVENTION_PD: {
                final Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);

                if (pcnInfo.contravention.selectedSuffix.toLowerCase().equals("s") &&
                        pcnInfo.contravention.contraventionCode.toLowerCase().equals("19"))
                    pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;


                if (pcnInfo.contravention.contraventionCode.equals("26") ||
                        pcnInfo.contravention.contraventionCode.equals("40")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PCNStartActivity.this);
                    builder.setMessage("Is this a commercial vehicle ?")
                            .setTitle("Vehicle Type")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_LOADING;
                                    pcnInfo.observationTime = FIVE_MINS;
                                    VehicleMakesActivity.currentPCN = pcnInfo;
                                    startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    VehicleMakesActivity.currentPCN = pcnInfo;
                                    startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
                                }
                            });
                    AlertDialog ad = builder.create();
                    ad.show();
                } else {
                    VehicleMakesActivity.currentPCN = pcnInfo;
                    startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
                }
                break;
            }
            case AppConstant.CONTRAVENTION_LOADING: {
                Boolean isDisabled = pcnInfo.contravention.selectedSuffix.equals("o");
                if (isDisabled) {
                    FragmentManager fm = getSupportFragmentManager();
                    DateTime now = DateTime.now();
                    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                            .newInstance(PCNStartActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                                    DateFormat.is24HourFormat(PCNStartActivity.this), "Disabled Badge Number", "Disabled Badge", "");

                    timePickerDialog.setThemeDark(true);
                    timePickerDialog.show(fm, "");

                } else {
                    ObservationTimeDialog observationTimeDialog = new ObservationTimeDialog(pcnInfo.contravention.contraventionCode, maxStayMinutes);
                    observationTimeDialog.show(getFragmentManager(), "");
                }
                break;
            }
            case AppConstant.CONTRAVENTION_DUAL_LOG: {
                if (pcnInfo.contravention.contraventionCode.equals("22")) {
                    CroutonUtils.info(PCNStartActivity.this, "You can not issue a 22 offence without a prior observation");
                } else {
                    VehicleMakesActivity.currentPCN = pcnInfo;
                    TimeplateDialog timeplateDialog = new TimeplateDialog();
                    timeplateDialog.setCancelable(false);
                    timeplateDialog.show(getFragmentManager(), "");
                }
                break;
            }
        }
    }*/

    private int getAuthorityMinimumForContravention(String contraventionCode) {
        int minimumWaitTime = 0;
        boolean useLegalMinimum = false;
        JSONArray streetIndexUseLegalContent = CeoApplication.GetDataFileContent("streetindex.json");
        try {
            if (streetIndexUseLegalContent != null) {
            for (int j = 0; j < streetIndexUseLegalContent.length(); j++) {
                    String noderef = streetIndexUseLegalContent.getJSONObject(j).getString("noderef");
                    if (noderef.equalsIgnoreCase(VisualPCNListActivity.currentStreet.noderef)) {
                       Log.e("noderef", VisualPCNListActivity.currentStreet.streetusrn);
                       JSONArray streetIndexTimesOptions = streetIndexUseLegalContent.getJSONObject(j).getJSONArray("streetcontraventions");
                       for (int k = 0; k < streetIndexTimesOptions.length(); k++) {
                            JSONObject useLegalMinimum1TimesObject = streetIndexTimesOptions.getJSONObject(k);
                            if (useLegalMinimum1TimesObject.getString("contraventionCode").equalsIgnoreCase(contraventionCode)) {
                                if (useLegalMinimum1TimesObject.has("useLegalMinimum")) {
                                    useLegalMinimum = Boolean.valueOf(useLegalMinimum1TimesObject.getString("useLegalMinimum"));

                                }else
                                {
                                   useLegalMinimum = false;
                                }
                                break;
                            }
                       }
                    }
                }
            }

            JSONObject minimumObservationTimesContent = CeoApplication.GetDataFileContentAsObject("minimumobservationtimes.json");
            JSONArray minimumObservationTimesOptions = minimumObservationTimesContent.getJSONArray("grace");
            if (minimumObservationTimesOptions != null) {
                for (int i = 0; i < minimumObservationTimesOptions.length(); i++) {
                    JSONObject minimumObservationTimesObject = minimumObservationTimesOptions.getJSONObject(i);
                    if (minimumObservationTimesObject.getString("contraventionCode").equalsIgnoreCase(contraventionCode)) {

                       /* if (minimumObservationTimesObject.has("useLegalMinimum")) {
                            useLegalMinimum = Boolean.valueOf(minimumObservationTimesObject.getString("useLegalMinimum"));*/
                            if (useLegalMinimum) {
                                if (minimumObservationTimesObject.has("legalMinimum")) {
                                    minimumWaitTime = Integer.valueOf(minimumObservationTimesObject.getString("legalMinimum"));
                                } else {
                                    minimumWaitTime = 0;
                                }
                                break;

                            } else {
                                if (minimumObservationTimesObject.has("authorityMinimum")) {
                                    minimumWaitTime = Integer.valueOf(minimumObservationTimesObject.getString("authorityMinimum"));
                                } else {
                                    minimumWaitTime = 0;
                                }
                                break;
                            }
                       /* } else {
                            minimumWaitTime = 0;
                        }
                        break;*/
                    }
                }
            }
            minimumWaitTime = maxStayMinutes + minimumWaitTime;
            if (Utils.eligibleForWarningNotice(pcnInfo,contraventionCode)) {
                minimumWaitTime = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return minimumWaitTime;
    }




    AppConstant.pcnIssueMode  pcnIssueMode= AppConstant.pcnIssueMode.PCN_AS_NORMAL;
    private void MoveToNextStep(){
        LogLocation(registrationMark.getText().toString(), true);
        pcnInfo.registrationMark = registrationMark.getText().toString();
        //TODO: this is a rubbish fix - refactor
        pcnInfo.contravention.contraventionType = DBHelper.getEnforcementType(pcnInfo.contravention.contraventionCode);
        pcnIssueMode = AppConstant.pcnIssueMode.PCN_AS_NORMAL;
        int contraventionCode = Integer.parseInt(pcnInfo.contravention.contraventionCode);
        String selectedCodeSuffix = String.valueOf(contraventionCode).concat(pcnInfo.contravention.selectedSuffix != null ? pcnInfo.contravention.selectedSuffix : "");
        if(CeoApplication.commercialVehicleCodes != null && !CeoApplication.commercialVehicleCodes.isEmpty()){
            List<String> commercialVehicleCodeList = Arrays.asList(CeoApplication.commercialVehicleCodes.split(","));
            if(commercialVehicleCodeList.contains(selectedCodeSuffix) || commercialVehicleCodeList.contains(pcnInfo.contravention.contraventionCode)){
                pcnIssueMode = AppConstant.pcnIssueMode.COMMERCIAL_VEHICLE;
            }
        }
        if(CeoApplication.disabledBadgeCodes != null && !CeoApplication.disabledBadgeCodes.isEmpty()){
            List<String> disabledBadgeCodeList = Arrays.asList(CeoApplication.disabledBadgeCodes.split(","));
            if(disabledBadgeCodeList.contains(selectedCodeSuffix) || disabledBadgeCodeList.contains(pcnInfo.contravention.contraventionCode)){
                pcnIssueMode = AppConstant.pcnIssueMode.DISABLED_BADGE;
            }

        }
        if(CeoApplication.payAndDisplayCodes != null && !CeoApplication.payAndDisplayCodes.isEmpty()){
            List<String> payAndDisplayCodeList = Arrays.asList(CeoApplication.payAndDisplayCodes.split(","));
            if(payAndDisplayCodeList.contains(selectedCodeSuffix) || payAndDisplayCodeList.contains(pcnInfo.contravention.contraventionCode)){
                pcnIssueMode = AppConstant.pcnIssueMode.PAY_AND_DISPLAY;
            }
        }
        switch (pcnIssueMode) {
            case COMMERCIAL_VEHICLE:
                AlertDialog.Builder builder = new AlertDialog.Builder(PCNStartActivity.this);
                builder.setMessage("Is this a commercial vehicle ?")
                        .setTitle("Vehicle Type")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_LOADING;
                                pcnInfo.observationTime = FIVE_MINS;
                                if(Utils.eligibleForWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode)){
                                    pcnInfo.observationTime = 0;
                                }
                                VehicleMakesActivity.currentPCN = pcnInfo;
                                Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
                                startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                                pcnInfo.observationTime = 0;
                                VehicleMakesActivity.currentPCN = pcnInfo;
                                Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
                                startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
                            }
                        });
                AlertDialog ad = builder.create();
                ad.show();
                break;

            case DISABLED_BADGE:
                FragmentManager fm = getSupportFragmentManager();
                DateTime now = DateTime.now();
                RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                        .newInstance(PCNStartActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                                DateFormat.is24HourFormat(PCNStartActivity.this), "Disabled Badge Number", "Disabled Badge", "");

                timePickerDialog.setThemeDark(true);
                timePickerDialog.show(fm, "");
                break;

            case PAY_AND_DISPLAY:
                VehicleMakesActivity.currentPCN = pcnInfo;
                TimeplateDialog timeplateDialog = new TimeplateDialog();
                timeplateDialog.setCancelable(false);
                timeplateDialog.show(getFragmentManager(), "");
                break;

            default:
                moveNextAsNormal();
                break;
        }
    }

    private void moveNextAsNormal(){
        try {
            Contravention contravention = contraventionDataList.get(offencesCode);
            JSONObject minimumObservationTimesObject = null;
            JSONObject minimumObservationTimesContent = CeoApplication.GetDataFileContentAsObject("minimumobservationtimes.json");
            if (minimumObservationTimesContent != null) {
                JSONArray minimumObservationTimesOptions = minimumObservationTimesContent.getJSONArray("grace");
                for (int i = 0; i < minimumObservationTimesOptions.length(); i++) {
                    if (minimumObservationTimesOptions.getJSONObject(i).getString("contraventionCode").equalsIgnoreCase(contravention.contraventionCode)) {
                        minimumObservationTimesObject = minimumObservationTimesOptions.getJSONObject(i);
                        break;
                    }
                }
            }
            boolean maxStayAvailable = false;
            if (minimumObservationTimesObject != null) {
                maxStayAvailable = minimumObservationTimesObject.has("maxStay") && minimumObservationTimesObject.getString("maxStay").equalsIgnoreCase("yes") ? true : false;
            }
            if (!maxStayAvailable) {
                maxStayMinutes = 0;
                int waitingTime = getAuthorityMinimumForContravention(contravention.contraventionCode);
                // new changed
                /* ObservationTimeDialog observationTimeDialog = new ObservationTimeDialog(pcnInfo.contravention.contraventionCode, maxStayMinutes);
                observationTimeDialog.show(getFragmentManager(), "");*/
                Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
                pcnInfo.observationTime = waitingTime * ONE_MIN;
                VehicleMakesActivity.currentPCN = pcnInfo;
                startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
            } else {
                JSONArray maxStays = minimumObservationTimesContent.getJSONArray("maxstay");
                MaxStayDialog maxStayDialog = new MaxStayDialog(maxStays, offencesCode);
                maxStayDialog.show(getFragmentManager(), "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void MoveToBackStep(Date parkingExpiryTime) {
        String parkingMessage = CeoApplication.ValidParkingUntil();
        if (parkingMessage.contains("@datetime@")) {
            if (DateUtils.isToday(parkingExpiryTime.getTime())) {
                parkingMessage = parkingMessage.replace("@datetime@", new SimpleDateFormat("hh:mm").format(parkingExpiryTime));
            } else {
                parkingMessage = parkingMessage.replace("@datetime@", new SimpleDateFormat("dd/MM/yyyy").format(parkingExpiryTime));
            }
        }
        CroutonUtils.info(PCNStartActivity.this, parkingMessage);
        deleteUnusedPhotos(pcnInfo.observationNumber);
        deleteUnusedNotes(pcnInfo.observationNumber);
        if (pcnInfo.pcnNumber != null && pcnInfo.pcnNumber.length() > 0) {
            DBHelper.releasePCNNumber(pcnInfo.pcnNumber);
        }
        setResult(CeoApplication.RESULT_CODE_OBS_CANCEL);
        finish();
    }

    private void LogLocation(String vrm, boolean isCeoPublish) {
        LocationLogTable logEntry = new LocationLogTable();
        logEntry.setLogTime(new DateTime());
        logEntry.setCeoName(DBHelper.getCeoUserId());
        logEntry.setStreetName(streetNameTextView.getText().toString());
        //logEntry.setVRM(vrm);
        logEntry.setVRM(CeoApplication.getRecordObservationVRM()?vrm:CeoApplication.getRecordObservationVRMValue());
        logEntry.setLongitude(VisualPCNListActivity.longitude);
        logEntry.setLattitude(VisualPCNListActivity.latitude);
        long obsStartTime = new Date().getTime();
        CeoApplication.OBS_START_TIME = obsStartTime;
        logEntry.setStartTime(obsStartTime);
        logEntry.save();
        if(isCeoPublish)
            publishCeoTracking();
    }

    private void publishCeoTracking() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject ceoObject = new JSONObject();
            JSONObject latLong = new JSONObject();
            latLong.put("lat", VisualPCNListActivity.latitude);
            latLong.put("long", VisualPCNListActivity.longitude);
            JSONObject data = new JSONObject();
            data.put("ceoshouldernumber", DBHelper.getCeoUserId());
            data.put("currentstreet", pcnInfo == null ? "" : pcnInfo.location.streetCPZ.streetname);
            data.put("datetimeoflogin", AppConstant.ISO8601_DATE_TIME_FORMAT.format(sharedPreferenceHelper.getLong(AppConstant.LOGIN_TIME)/*CeoApplication.ceoLoginTime*/));
            BreakTable breakTable = DBHelper.getBreakData(DBHelper.getCeoUserId());
            String breakStartTime = "", breakType = "";
            if(breakTable != null){
                breakStartTime = AppConstant.ISO8601_DATE_TIME_FORMAT.format(new Date(breakTable.getStartTime()));
                breakType = breakTable.getBreakType();
            }
            data.put("datetimebreakstarted",breakStartTime);
            data.put("breaktype", breakType);
            data.put("ceorole",sharedPreferenceHelper.getString(AppConstant.CEO_ROLE,"") /*CeoApplication.ceoRole*/);
            ceoObject.put("latlong", latLong);
            ceoObject.put("data", data);
            jsonObject.put(DBHelper.getCeoUserId(), ceoObject);
            PubNubModule.publishCeoTracking(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean ApplicableForPayByPhone(){
        boolean applicableForPayByPhone = false;
        String[] conventionCodes = CeoApplication.PayByPhoneContraventionCodes().split(",");
        for (String contravention : conventionCodes) {
            if (contravention.equalsIgnoreCase(pcnInfo.contravention.contraventionCode)) {
                applicableForPayByPhone = true;
                break;
            }
        }
        return applicableForPayByPhone;
    }
    private JSONObject CheckForValidExistingParking() {
        JSONObject objParking = null;
        try {
            objParking = DBHelper.CheckForValidParking(registrationMark.getText().toString(), Integer.valueOf(VisualPCNListActivity.currentStreet.streetusrn));
        } catch (Exception ex) {
            Log.d("TAG", ex.getMessage());
            ex.printStackTrace();
        }
        return objParking;
    }

    @Override
    public void vrmLookupPaidParking(ArrayList<PaidParking> paidParkings, String vrmText, boolean isError) {

        msgpaidParkings = paidParkings;
        vrmMsgtext= vrmText;
        msgError = isError;
        if(isError){
            showVRMAutomatedLookupErrorDialog("No Valid Permit or Paid for Parking Found", true);
        }else{
        if(CeoApplication.getVrmEntryLookUp()) {
            if(msgpaidParkings.size()>0) {
                Intent infoIntent = new Intent(PCNStartActivity.this, MessageViewActivity.class);
                //ArrayList<PaidParking> paidParkings = new ArrayList<PaidParking>();
                //paidParkings.add(paidParkings);
                int sync = DataHolder.get().setListData(paidParkings);
                infoIntent.putExtra("paidParking:synccode", sync);
                //infoIntent.putExtra("paidParking", paidParkings);
                infoIntent.putExtra("VRM", vrmText);
                infoIntent.putExtra("paidParkingMsg", true);
                startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_MESSAGEVIEW);

            }else {
                if (CeoApplication.getCheckPermitSession()) {
                    Log.e("getCheckPermitSession","true");
                    vrmLookUpDialogAfterShowingMessage( msgpaidParkings, vrmMsgtext,  msgError);
                } else {
                    Log.e("getCheckPermitSession","false");
                    checkSpecialVehicleType();
                }
            }
        }else {
            if (CeoApplication.getCheckPermitSession()) {
                Log.e("getCheckPermitSession", "true");
                vrmLookUpDialogAfterShowingMessage(msgpaidParkings, vrmMsgtext, msgError);
            } else {
                Log.e("getCheckPermitSession", "false");
                checkSpecialVehicleType();
            }

        }
    }

      /*  if(paidParking != null) {
            showVRMAutomatedLookupDialog(paidParking);
        } else {
            showVRMAutomatedLookupErrorDialog("Unable to check if this vehicle has a permit or cashless parking session");
        }*/
    }






    private void vrmLookUpDialogAfterShowingMessage(ArrayList<PaidParking> paidParkings, String vrmText, boolean isError)
    {
        if(isError){
            showVRMAutomatedLookupErrorDialog("No Valid Permit or Paid for Parking Found", true);
        } else{
            showVRMAutomatedLookupDialog(paidParkings);
        }

    }

    private void showVRMAutomatedLookupDialog(ArrayList<PaidParking> paidParkings) {
        Log.e("paidparking", String.valueOf(paidParkings));
        FragmentManager fm = getSupportFragmentManager();
        VRMAutomatedLookupDialog dialog = VRMAutomatedLookupDialog.newInstance(paidParkings, false);
        dialog.setCancelable(false);
        dialog.show(fm, "");
    }

    private void showVRMAutomatedLookupErrorDialog(String message, final boolean isNetworkAvaiable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkAutomatedVRMLookup();
                    }
                })
                .setNegativeButton("Skip the look up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isNetworkAvaiable){
                            pcnInfo.firstParkingSessionCheck = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + " No Valid Permit or Paid for Parking Found";
                        } else {
                            pcnInfo.firstParkingSessionCheck = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + " No internet connectivity available at the time of search";
                        }
                        checkSpecialVehicleType();
                    }
                })
                .show();
    }

    @Override
    public void OnVRMLookupConfirmed(boolean isConfirmed, String data) {
        if(isConfirmed) {
            pcnInfo.firstParkingSessionCheck = data;
            checkSpecialVehicleType();
        } else {
            //restrict the user to take only one diagram note
            pcnInfo.diagramNoteTaken = false;
            deleteUnusedPhotos(pcnInfo.observationNumber);
            deleteUnusedNotes(pcnInfo.observationNumber);
            //pcn disorder problem
            if(pcnInfo.pcnNumber !=null && pcnInfo.pcnNumber.length()>0){
                DBHelper.releasePCNNumber(pcnInfo.pcnNumber);
            }
            setResult(CeoApplication.RESULT_CODE_OBS_CANCEL);
            finish();
        }
    }

    private class EnquireFromPayByPhone extends AsyncTask<String, Void, List<String>> {
        boolean validParking = false;
        Date parkingExpiryTime;
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PCNStartActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(CeoApplication.QueryingPayByPhone());
            progressDialog.show();
            queryStartTime = new Date().getTime();
        }

        @Override
        protected List<String> doInBackground(String... args) {
            return HttpClientHelper.doGet(args[0]);
        }

        @Override
        protected void onPostExecute(List<String> rows) {
            super.onPostExecute(rows);
            progressDialog.dismiss();
            try {
                long responseTime = (new Date().getTime() - queryStartTime) / 1000;
                if (responseTime > CeoApplication.QueryWaitTime()) {
                    validParking = false;
                } else {
                    int rowNum = 0;
                    if (rows.size() > 3) {
                        for (String row : rows) {
                            if (rowNum > 3) {
                                String[] rowArray = row.split(",");
                                String vrm = rowArray[0];
                                String expiry = rowArray[5].replace("GMT", "");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss");
                                Date expiryTime = sdf.parse(expiry.trim());
                                DBHelper.SaveVirtualPermit(vrm, Integer.valueOf(VisualPCNListActivity.currentStreet.streetusrn), expiryTime.getTime());
                            }
                            rowNum++;
                        }
                        JSONObject reObjParking = DBHelper.CheckForValidParking(registrationMark.getText().toString(), Integer.valueOf(VisualPCNListActivity.currentStreet.streetusrn));
                        boolean reParkingFound = false;
                        try {
                            if (reObjParking.has("parkingFound"))
                                reParkingFound = reObjParking.getBoolean("parkingFound");
                            if (reParkingFound) {
                                validParking = true;
                                if (reObjParking.has("parkingExpiryTime"))
                                    parkingExpiryTime = new Date(reObjParking.getLong("parkingExpiryTime"));
                            } else {
                                validParking = false;
                            }
                        } catch (Exception ex) {
                            Log.d("TAG", ex.getMessage());
                            ex.printStackTrace();
                        }

                    } else {
                        validParking = false;
                    }
                }
                if (validParking) {
                    MoveToBackStep(parkingExpiryTime);
                } else {
                    pcnInfo.firstParkingSessionCheck = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                            + " - " + " No Valid Permit or Paid for Parking Found";
                    MoveToNextStep();
                }

            } catch (Exception e) {
                Log.d("TAG", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private Boolean isUserInputValid()
    {
        if (streetNameTextView.getText().toString().length() > 0 && streetChosen)
        {
            String vrm = registrationMark.getText().toString();
            if(vrm.length() > 0)
            {
                if (isVRMUK(vrm) || foreignSet)
                    return true;
                else {
                    if (vrm.equals("TEST"))
                        return true;
                    ForeignVehicleDialog foreignVehicleDialog = ForeignVehicleDialog.newInstance();
                    foreignVehicleDialog.show(getFragmentManager(),"");
                    return false;
                }

            }
            else
            {
                CroutonUtils.error(CroutonUtils.DURATION_SHORT,this,"Please enter a Registration");
            }
        }
        else
        {
            CroutonUtils.error(CroutonUtils.DURATION_SHORT, this,"Please enter a valid street");
        }
        return false;
    }

    private Boolean isVRMUK(String vrm)
    {
        String expression = "^([A-Z]{3}\\s?(\\d{3}|\\d{2}|d{1})\\s?[A-Z])|([A-Z]\\s?(\\d{3}|\\d{2}" +
                "|\\d{1})\\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\\s?([0][2-9]|[1-9][0-9])\\s?[A-HJ-PR-Z]{3})$";

        String expression2 = "^(([A-Z]{1,2}[ ]?[0-9]{1,4})|([A-Z]{3}[ ]?[0-9]{1,3})|([0-9]{1,3}" +
                "[ ]?[A-Z]{3})|([0-9]{1,4}[ ]?[A-Z]{1,2})|([A-Z]{3}[ ]?[0-9]{1,3}[ ]?[A-Z])|([A-Z]" +
                "[ ]?[0-9]{1,3}[ ]?[A-Z]{3})|([A-Z]{2}[ ]?[0-9]{2}[ ]?[A-Z]{3})|([A-Z]{3}[ ]?[0-9]{4}))$";

        CharSequence inputStr = vrm;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        pattern = Pattern.compile(expression2, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public void onTimeSet(RadialTimePickerDialog dialog, int hourOfDay, int minute, String serial) {
        Intent pdIntent = new Intent(PCNStartActivity.this, PCNLoggingActivity.class);
        Integer minsTotal = ((hourOfDay * 60 + minute) - DateTime.now().getMinuteOfDay());
        /*if (pcnInfo.contravention.contraventionType == AppConstant.CONTRAVENTION_PD ||
                pcnInfo.contravention.contraventionType == AppConstant.CONTRAVENTION_DUAL_LOG)*/
        if (pcnIssueMode == AppConstant.pcnIssueMode.PAY_AND_DISPLAY )
        {
            if (minsTotal < -5) {
                pcnInfo.contravention.contraventionType = 0; // instant
            }
            else
            {
                minsTotal += 5;
            }
            long timer = minsTotal * ONE_MIN;
            if(Utils.eligibleForWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode)){
                timer = -1;
            }
            PDTicket newPD = new PDTicket();
            newPD.serialNo = serial;
            newPD.timeMillis = DateTime.now().plusMinutes(minsTotal).getMillis();
            newPD.expiryTime = new DateTime(newPD.timeMillis).toString();
            pdIntent.putExtra("pd",newPD);
            pdIntent.putExtra("timer", timer);

        }
        else
        {
            pdIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
            long timer = minsTotal * ONE_MIN;
            if (pcnInfo.contravention.selectedSuffix.equals("o"))
                timer += FIVE_MINS;
            pcnInfo.observationTime = timer;
            if(Utils.eligibleForWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode)){
                pcnInfo.observationTime = 0;
            }
            //pdIntent.putExtra("timer", timer);
            VehicleMakesActivity.currentPCN = pcnInfo;
        }
        startActivityForResult(pdIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
    }

    @Override
    public void onDismissPDDialog() {
        Intent logIntent = new Intent(PCNStartActivity.this, PCNLoggingActivity.class);
        startActivityForResult(logIntent, CeoApplication.REQUEST_CODE_TIMED_OBS);
    }

    @Override
    protected void onResume()    {
        super.onResume();
        if (contraventionDataList.size()>0)
            customSpinner.startController();
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        if (item.getItemId() == R.id.mnuPrinter) {
            try
            {
                /*if (mPrinterManager.isConnect())
                    mPrinterManager.disconnect();
                else
                    mPrinterManager.resetPrinter();*/
                if (mPrinterManager.isConnect()){
                    mPrinterManager.disconnect();

                }
                CeoApplication.changePrinter();
                mPrinterManager = CeoApplication.getPrinterManager();

                //CroutonUtils.info(this, "Tap on a new printer to connect");
                CroutonUtils.info(this, "Tap your device on printer first and try again");
            } catch (PrinterException e)
            {
                //CroutonUtils.error(this, e.getMessage());
                CroutonUtils.info(this, "Tap your device on printer first and try again");
            }
        }
        if (item.getItemId() == R.id.mnuNotes) {
            Intent intent = new Intent(this, NotesListActivity.class);
            intent.putExtra("obs", pcnInfo.observationNumber);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pcn_menu, menu);
        return true;
    }

    @Override
    public void onLocationSave(String loc, Boolean secondLoc)
    {
        if (secondLoc)
            pcnInfo.location2.outside = loc;
        else
            pcnInfo.location.outside = loc;
    }

    @Override
    public void onLocationCancel()
    {

    }

    @Override
    public void OnObservationTimeSelected(Integer waiting)
    {
        Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
        pcnInfo.observationTime = waiting * ONE_MIN;
        if(Utils.eligibleForWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode)){
            pcnInfo.observationTime = 0;
        }
        VehicleMakesActivity.currentPCN = pcnInfo;
        startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
    }

    @Override
    public void onTimeplateInfoEntered(Timeplate timeplate) {

        pcnInfo.timeplateInfo = timeplate;
        /*if(eligibleForWarningNotice(pcnInfo.contravention.contraventionCode)){
            pcnInfo.timeplateInfo = new Timeplate();
        }*/
        if (pcnInfo.contravention.contraventionCode.equals("05"))
        {
            Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
            VehicleMakesActivity.currentPCN = pcnInfo;
            startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
        }
        else
        {
            Intent makesIntent = new Intent(PCNStartActivity.this, VehicleMakesActivity.class);
            long timer = timeplate.maxTime + FIVE_MINS; //test
            pcnInfo.observationTime = timer;
            if(Utils.eligibleForWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode)){
                pcnInfo.observationTime = 0;
            }
            VehicleMakesActivity.currentPCN = pcnInfo;
            startActivityForResult(makesIntent, CeoApplication.RESULT_CODE_VEHICLE_DIALOG);
        }
    }

    @Override
    public void onCountrySelected(String code) {
        foreignSet = true;
        pcnInfo.countryCode = code.split(" ")[0];
        if (CeoApplication.getCheckPermitSession()) {
            checkAutomatedVRMLookup();
        } else {
            checkSpecialVehicleType();
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
                int rowNum = 0;
                if (rows.size() > 3) {
                    for (String row : rows) {
                        if (rowNum > 3) {
                            String[] rowArray = row.split(",");
                            String vrm = rowArray[0];
                            String expiry = rowArray[5].replace("GMT", "");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss");
                            Date expiryTime = sdf.parse(expiry.trim());
                            DBHelper.SaveVirtualPermit(vrm, Integer.valueOf(VisualPCNListActivity.currentStreet.streetusrn), expiryTime.getTime());
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

    private boolean compareStreetAndCPZ(String content, String compareTo){
        List<String> contentList = new ArrayList<>(Arrays.asList(content.split("\\|")));
        for(String contentStr : contentList){
            if(contentStr.equalsIgnoreCase(compareTo))
                return true;
        }
        return false;
    }

    private void checkSpecialVehicleType(){
        String vrm = registrationMark.getText().toString();
        try {
            JSONArray specialVehicleRecord = CeoApplication.GetDataFileContent("specialVehicles.json");
            JSONObject specialVehicleJson = null;
            boolean vrmFound = false;
            AppConstant.specialVehicleType recordType = AppConstant.specialVehicleType.NORMAL;
            if (specialVehicleRecord != null) {
                for (int index = 0; index < specialVehicleRecord.length(); index++) {
                    specialVehicleJson = specialVehicleRecord.getJSONObject(index);
                    if (specialVehicleJson.getString("VRM").equalsIgnoreCase(vrm)) {
                        boolean isMatched = compareStreetAndCPZ(specialVehicleJson.getString("RESTRICTSTREETNAME"), pcnInfo.location.streetCPZ.streetname);
                        if (!isMatched){
                            isMatched = compareStreetAndCPZ(specialVehicleJson.getString("RESTRICTCPZ"), pcnInfo.location.streetCPZ.owningcpz);
                        }
                        if(isMatched){
                            vrmFound = true;
                            recordType = specialVehicleJson.getString("RECORDTYPE").equalsIgnoreCase("Blacklist") ? AppConstant.specialVehicleType.BLACKLIST : AppConstant.specialVehicleType.WHITELIST;
                            break;
                        }
                    }
                }
            }

            if(vrmFound){
                String message;
                switch (recordType) {
                    case BLACKLIST:
                        message = specialVehicleJson.getString("ALERTMESSAGE").isEmpty() ? "Black listed Vehicle" : specialVehicleJson.getString("ALERTMESSAGE");
                        Utils.showDialog(PCNStartActivity.this, message, "Message", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                publishBlackList();
                                btnContineClicked();
                            }
                        });
                        break;
                    case WHITELIST:
                        message = specialVehicleJson.getString("ALERTMESSAGE").isEmpty() ? "White listed Vehicle" : specialVehicleJson.getString("ALERTMESSAGE");
                        Utils.showDialog(PCNStartActivity.this, message, "Message", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                LogLocation("WHITELIST", false);
                                btnNext.setEnabled(false);
                            }
                        });
                        break;
                    default:
                        btnContineClicked();
                }

            }else{
                btnContineClicked();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void publishBlackList(){
        try {
            JSONObject mainObject = new JSONObject();
            JSONObject blackwhitelistvehicle = new JSONObject();
            blackwhitelistvehicle.put("RECORDTYPE", "black");
            blackwhitelistvehicle.put("VRM", registrationMark.getText().toString());
            blackwhitelistvehicle.put("STREETNODE", pcnInfo.location.streetCPZ.noderef);
            blackwhitelistvehicle.put("STREETNAME", pcnInfo.location.streetCPZ.streetname);
            blackwhitelistvehicle.put("LAT", String.valueOf(VisualPCNListActivity.latitude));
            blackwhitelistvehicle.put("LON", String.valueOf(VisualPCNListActivity.longitude));
            blackwhitelistvehicle.put("CEOSHOULDER", DBHelper.getCeoUserId());
            blackwhitelistvehicle.put("HHTID", CeoApplication.getUUID());
            blackwhitelistvehicle.put("OBSERVEDAT", DateUtils.getISO8601DateTime());
            mainObject.put("blackwhitelistvehicle", blackwhitelistvehicle);
            PubNubModule.publishBlackWhiteListVehicle(mainObject);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Suffix loadSuffixes(){
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        String suffixRes = cameraImageHelper.readFile(AppConstant.CONFIG_FOLDER, "suffixes.json");
        return new Gson().fromJson(suffixRes, Suffix.class);
    }

}
