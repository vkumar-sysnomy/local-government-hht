package com.farthestgate.android.ui.pcn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.VRMAutomatedLookupTask;
import com.farthestgate.android.model.AdditionalInfo;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.DestinationInfo;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PCNJsonData;
import com.farthestgate.android.model.PDTicket;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.model.PermitBadge;
import com.farthestgate.android.model.TaxDisc;
import com.farthestgate.android.model.WarningNotice;
import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.model.database.NotesTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.printing.seikoTemplateProcessor;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.admin.LoginActivity;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.ui.components.radialpickers.RadialTimePickerDialog;
import com.farthestgate.android.ui.components.timer.TimerListItem;
import com.farthestgate.android.ui.components.timer.TimerObj;
import com.farthestgate.android.ui.dialogs.AdditionalInfoDialog;
import com.farthestgate.android.ui.dialogs.ContraventionChangeDialog;
import com.farthestgate.android.ui.dialogs.DestinationDialog;
import com.farthestgate.android.ui.dialogs.MultiLogDialog;
import com.farthestgate.android.ui.dialogs.OSLocationDialog;
import com.farthestgate.android.ui.dialogs.PermitBadgeInfoDialog;
import com.farthestgate.android.ui.dialogs.SuffixDialog;
import com.farthestgate.android.ui.dialogs.TempValveDialog;
import com.farthestgate.android.ui.dialogs.VRMAutomatedLookupDialog;
import com.farthestgate.android.ui.dialogs.VRMCheckDialog;
import com.farthestgate.android.ui.dialogs.VRMDoubleCheckDialog;
import com.farthestgate.android.ui.dialogs.vehicle_logging.VehicleMakesActivity;
import com.farthestgate.android.ui.notes.NotesActivity;
import com.farthestgate.android.ui.notes.NotesListActivity;
import com.farthestgate.android.ui.notes.TextNoteActivity;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.Base64;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DateUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PCNLoggingActivity extends NFCBluetoothActivityBase implements
        PermitBadgeInfoDialog.OnExtraInfoEntered, OSLocationDialog.LocationDialogListener,
        TempValveDialog.OnValveInfoEntered, VRMCheckDialog.OnVRMConfirmed,
        RadialTimePickerDialog.OnTimeSetListener, DestinationDialog.OnDestinationInfoEntered,
        AdditionalInfoDialog.OnDestinationInfoEntered, MultiLogDialog.OnMultiLogListener,
        SuffixDialog.OnNewSuffixInfoEnteredListener, VRMDoubleCheckDialog.OnVRMDoubleConfirmed, ContraventionChangeDialog.OnContraventionChanged,
        VRMAutomatedLookupTask.VRMAutomatedLookupListener, VRMAutomatedLookupDialog.OnVRMLookupListener

{
    private static int LINE_LENGTH = 68;
    private static String[] VALVES_REQUIRED_CODES ={"22","30","80","84"};
    public static final String TAG = PCNLoggingActivity.class.getSimpleName();
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private CameraImageHelper cameraImageHelper;
    private Boolean isVRMConfirmed = false;
    private String lastVrm = "";
    private Integer descLines = 1;
    private long duration = 0;
    private TimerListItem timerItem;
    private TimerObj oTimer;
    private Handler tickHandler;
    private ImageButton btnPrint;
    private ImageButton btnPCNNotes;
    private ImageButton btnCancel;
    private ImageButton btnPhotos;
    private ImageButton btnAdditionalInfo;
    private ImageButton btnPermitBadge;
    private ImageButton btnPDTicket;
    private ImageButton btnTax;
    private TextView contraTextView;
    private TextView suffixTextView;
    private TextView streetTextView;
    private TextView colourTextView;
    private TextView makeTextView;
    private TextView modelTextView;
    private TextView locationTextView;
    private TextView regTextView;
    private ProgressDialog progressDialog;
    private RelativeLayout clickLayout;
    private byte[] binaryPrint = asByteArray("0A");
    private byte[] binaryLine = asByteArray("1D3C");
    private byte[] binaryClearBuffer = asByteArray("18");
    private Boolean completed = false;
    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_pcnlogging);
        cameraImageHelper = new CameraImageHelper();
        VisualPCNListActivity.currentActivity = 1;
        timerItem = (TimerListItem) findViewById(R.id.timerLog);
        contraTextView = (TextView) findViewById(R.id.contraText);
        suffixTextView = (TextView) findViewById(R.id.suffixText);
        streetTextView = (TextView) findViewById(R.id.txtStreet);
        colourTextView = (TextView) findViewById(R.id.colourText);
        makeTextView = (TextView) findViewById(R.id.makeText);
        modelTextView = (TextView) findViewById(R.id.modelText);
        regTextView = (TextView) findViewById(R.id.regText);
        locationTextView = (TextView) findViewById(R.id.txtOSlocation);
        btnPrint = (ImageButton) findViewById(R.id.btnNextStep);
        btnPCNNotes = (ImageButton) findViewById(R.id.imgBtnNotes);
        btnCancel = (ImageButton) findViewById(R.id.btnCancelPcn);
        btnPhotos = (ImageButton) findViewById(R.id.btnPhotos);
        btnTax = (ImageButton) findViewById(R.id.btnTax);
        btnPDTicket = (ImageButton) findViewById(R.id.btnPD);
        btnPermitBadge = (ImageButton) findViewById(R.id.btnPermit);
        btnAdditionalInfo = (ImageButton) findViewById(R.id.btnOther);
        clickLayout = (RelativeLayout) findViewById(R.id.clickableLayout);
        btnPermitBadge.setOnClickListener(btnPermitClick);
        btnPDTicket.setOnClickListener(btnPDClick);
        //HAN-75
        //btnTax.setOnClickListener(btnTaxClick);
        locationTextView.setOnClickListener(locationClick);
        btnPrint.setOnClickListener(btnPrintClick);
        btnPhotos.setOnClickListener(btnPhotosClick);
        btnPCNNotes.setOnClickListener(btnPCNNotesClick);
        btnCancel.setOnClickListener(btnCancelClick);
        btnAdditionalInfo.setOnClickListener(btnAdditionalInfoClick);
        clickLayout.setOnClickListener(layoutClick);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Printing, please wait ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Printing");
        tickHandler = new Handler();
        Intent infoIntent = getIntent();
        long timeLeft = 0;
        if (infoIntent.hasExtra("timerObj")) // coming from the list
        {
            oTimer = infoIntent.getParcelableExtra("timerObj");
            switch (oTimer.mState) {
                case TimerObj.STATE_RUNNING: {
                    oTimer.mView = timerItem;
                    timeLeft = oTimer.updateTimeLeft(false);
                    timerItem.set(oTimer.mOriginalLength, timeLeft, false);
                    timerItem.setTime(timeLeft, true);
                    timerItem.start();
                    tickHandler.postDelayed(mClockTick, 20);
                    // load smaller buttons
                    btnPermitBadge.setImageDrawable(getResources().getDrawable(R.drawable.permit_s_selector));
                    btnTax.setImageDrawable(getResources().getDrawable(R.drawable.tax_s_selector));
                    btnPDTicket.setImageDrawable(getResources().getDrawable(R.drawable.pd_s_selector));
                    btnAdditionalInfo.setImageDrawable(getResources().getDrawable(R.drawable.other_s_selector));
                    break;
                }
                case TimerObj.STATE_TIMESUP:
                case TimerObj.STATE_DONE:
                default: {
                    timerItem.setVisibility(View.GONE);
                    btnPermitBadge.setImageDrawable(getResources().getDrawable(R.drawable.permit_selector));
                    btnTax.setImageDrawable(getResources().getDrawable(R.drawable.tax_selector));
                    btnPDTicket.setImageDrawable(getResources().getDrawable(R.drawable.pd_selector));
                    btnAdditionalInfo.setImageDrawable(getResources().getDrawable(R.drawable.other_selector));
                    break;
                }
            }
            pcnInfo = new GsonBuilder().create().fromJson(oTimer.pcnJSON, PCN.class);
            locationTextView.setText(pcnInfo.location.outside);
            if (pcnInfo.contravention.contraventionType == AppConstant.CONTRAVENTION_DUAL_LOG) {
                if (oTimer.mState == TimerObj.STATE_TIMESUP) {
                    oTimer.mState = TimerObj.STATE_DONE;
                    if (pcnInfo.pcnNumber == null) {
                        MultiLogDialog multiLogDialog = new MultiLogDialog();
                        multiLogDialog.setCancelable(false);
                        multiLogDialog.show(getFragmentManager(), "");
                    }
                }
            }

        } else {
            if(VehicleMakesActivity.currentPCN!=null)
            pcnInfo = VehicleMakesActivity.currentPCN;
            if (infoIntent.hasExtra("timer"))  // coming from the observation screen 1
            {
                duration = infoIntent.getLongExtra("timer", 0l);
                btnPrint.setEnabled(false);
                oTimer = new TimerObj(duration);
                if (duration < 0) {
                    oTimer.mState = TimerObj.STATE_DONE;
                    timerItem.setVisibility(View.GONE);
                    btnPrint.setEnabled(true);
                    btnPermitBadge.setImageDrawable(getResources().getDrawable(R.drawable.permit_selector));
                    btnTax.setImageDrawable(getResources().getDrawable(R.drawable.tax_selector));
                    btnPDTicket.setImageDrawable(getResources().getDrawable(R.drawable.pd_selector));
                    btnAdditionalInfo.setImageDrawable(getResources().getDrawable(R.drawable.other_selector));
                } else {
                    btnPermitBadge.setImageDrawable(getResources().getDrawable(R.drawable.permit_s_selector));
                    btnTax.setImageDrawable(getResources().getDrawable(R.drawable.tax_s_selector));
                    btnPDTicket.setImageDrawable(getResources().getDrawable(R.drawable.pd_s_selector));
                    btnAdditionalInfo.setImageDrawable(getResources().getDrawable(R.drawable.other_s_selector));
                    oTimer.mState = TimerObj.STATE_RUNNING;
                    oTimer.mView = timerItem;
                    timeLeft = oTimer.updateTimeLeft(false);
                    timerItem.set(oTimer.mOriginalLength, timeLeft, false);
                    timerItem.setTime(timeLeft, true);
                    timerItem.start();
                    tickHandler.postDelayed(mClockTick, 20);
                }

            } else {
                timerItem.setVisibility(View.GONE);
                btnPrint.setEnabled(true);
                if(pcnInfo!=null)
                pcnInfo.logTime = DateTime.now().getMillis();
            }
        }
        if (infoIntent.hasExtra("pd")) {
            PDTicket pdTicket = infoIntent.getParcelableExtra("pd");
            pcnInfo.pdTicketsList.add(pdTicket);
        }
        setContraventionCharge();
        if (pcnInfo.contravention.selectedSuffix.length() == 0) {
            pcnInfo.contravention.selectedSuffix = "";
            pcnInfo.contravention.codeSuffixDescription = "";
            suffixTextView.setVisibility(View.INVISIBLE);
        } else {
            if(!pcnInfo.contravention.codeSuffixDescription.startsWith("(") && !pcnInfo.contravention.codeSuffixDescription.endsWith(")"))
                pcnInfo.contravention.codeSuffixDescription = "(" + pcnInfo.contravention.codeSuffixDescription + ")";
        }
        suffixTextView.setText(pcnInfo.contravention.selectedSuffix.toUpperCase());
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        contraTextView.setText(pcnInfo.contravention.contraventionCode);
        streetTextView.setText(pcnInfo.location.streetCPZ.streetname);
        colourTextView.setText(pcnInfo.colourName);
        makeTextView.setText(pcnInfo.manufacturer.name);
        modelTextView.setText(pcnInfo.model.modelName);
        regTextView.setText(pcnInfo.registrationMark);
        getActionBar().setTitle("");
       if(pcnInfo.isUsed){
            btnCancel.setEnabled(false);
            locationTextView.setEnabled(false);
            clickLayout.setClickable(false);
            btnPrint.setImageDrawable(getResources().getDrawable(R.drawable.print_selector));
            completed = true;
        } else {
           long pcnPrintTime = DBHelper.getPCNPrintTime(pcnInfo.pcnNumber);
           if (pcnPrintTime == 0) {
               locationTextView.setEnabled(true);
               clickLayout.setClickable(true);
           } else {
               btnCancel.setEnabled(false);
               locationTextView.setEnabled(false);
               clickLayout.setClickable(false);
               btnPrint.setImageDrawable(getResources().getDrawable(R.drawable.print_selector));
               completed = true;
           }
        }
        mPrinterManager = CeoApplication.getPrinterManager();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isInit = pref.getBoolean("init", false);
        if (!isInit) {
            SharedPreferences.Editor editor = pref.edit();
            if (Locale.JAPAN.equals(Locale.getDefault())) {
                editor.putString(getText(R.string.key_international_character).toString(), "8");
                editor.putString(getText(R.string.key_code_page).toString(), "1");
            } else {
                editor.putString(getText(R.string.key_international_character).toString(), "0");
                editor.putString(getText(R.string.key_code_page).toString(), "16");
            }
            editor.putBoolean("init", true);
            editor.commit();
        }
        if (oTimer == null) {
            oTimer = new TimerObj();
            oTimer.mState = TimerObj.STATE_INSTANT;
        } else {
            if (oTimer.mState == TimerObj.STATE_RUNNING)
                btnPrint.setEnabled(false);
        }
        handleIntent(getIntent());
        if(CeoApplication.AllowContraventionChange() && pcnInfo.observationTime > 0 && pcnInfo.issueTime == 0 && pcnInfo.contraventionChanged.equalsIgnoreCase("false") && timeLeft <= 0){
            OpenContraventionChangeDialog(false);
        }
    }

    View.OnClickListener btnAdditionalInfoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdditionalInfoDialog adi = AdditionalInfoDialog.NewInstance(pcnInfo.additionalInfo.selectedOptions);
            adi.setCancelable(false);
            adi.show(getFragmentManager(), "");
        }
    };

    View.OnClickListener btnCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO: Deactivate print click
            cancelPCN();
        }
    };

    View.OnClickListener layoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(CeoApplication.AlwaysRequireValves()){
                TempValveDialog tv = TempValveDialog.NewInstance(pcnInfo.valveFront, pcnInfo.valveBack, false);
                tv.show(getFragmentManager(), "");
            }else {
                if (Arrays.asList(VALVES_REQUIRED_CODES).contains(pcnInfo.contravention.contraventionCode)) {
                    TempValveDialog tv = TempValveDialog.NewInstance(pcnInfo.valveFront, pcnInfo.valveBack, false);
                    tv.show(getFragmentManager(), "");
                } else {
                    pcnInfo.valveBack = -1;
                    pcnInfo.valveFront = -1;
                    EnableToolbar(true);
                    btnCancel.setEnabled(!completed);
                    if (oTimer.isTicking()) btnPrint.setEnabled(false);
                }
            }
        }
    };

    View.OnClickListener btnPhotosClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnalyticsUtils.trackTakePhotoButton();
            Intent imageIntent = new Intent(PCNLoggingActivity.this, CameraActivity.class);
            imageIntent.putExtra("obs", pcnInfo.observationNumber);
            if(pcnInfo.pcnNumber !=null && pcnInfo.pcnNumber.length()>0){
                imageIntent.putExtra("pcn", pcnInfo.pcnNumber);
            }
            startActivityForResult(imageIntent, CeoApplication.RESULT_CODE_NEWIMAGE);
        }
    };

    private void EnableToolbar(Boolean enable) {
        btnCancel.setEnabled(enable);
        btnPhotos.setEnabled(enable);
        btnPCNNotes.setEnabled(enable);
        btnPrint.setVisibility(View.VISIBLE);
        btnPrint.setEnabled(enable);
    }

    View.OnClickListener btnPrintClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnalyticsUtils.trackFinalPrintButton();
            try {
                EnableToolbar(false);
                if (pcnInfo.issueTime > 0) // PCN is issued
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PCNLoggingActivity.this);
                    builder.setMessage("Are you sure you want to print this PCN again ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setCancelable(false)
                            .setTitle("Re-print PCN");
                    final AlertDialog alertDialog = builder.create();

                    alertDialog.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnPrint.setVisibility(View.GONE);
                            if (!pcnInfo.isUsed)
                            {
                                SavePCN();
                                pcnInfo.isUsed = true;
                            }
                            alertDialog.dismiss();
                            if (!alertDialog.isShowing()) {
                                LogLocationForPCN(pcnInfo.registrationMark, pcnInfo.pcnNumber, true);
                                connectBluetooth(true);
                            }
                        }
                    });
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            EnableToolbar(true);
                            btnCancel.setEnabled(false);
                        }
                    });
                } else {
                    if (isRequiredInfoAdded())
                        if (!isVRMConfirmed) {
                            VRMCheckDialog vrmCheckDialog = new VRMCheckDialog(pcnInfo.registrationMark);
                            vrmCheckDialog.setCancelable(false);
                            vrmCheckDialog.show(getFragmentManager(), "");
                        } else {
                            checkSpecialVehicleType(pcnInfo.registrationMark);
                        }
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    };
    //Update ticket book for used PCN
    private void UpdateTicketBookForUsedPCN() throws Exception{
        boolean usedTicketFound = false;
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        String root = Environment.getExternalStorageDirectory().toString();
        File ticketBookPath = new File(root + File.separator + AppConstant.TICKET_BOOK_FILE_PATH);
        File ticketBookFile = ticketBookPath.listFiles()[0];
        String ticketBookContent = GetTicketBookContent(ticketBookFile);
        JSONObject ticketBook = new JSONObject(ticketBookContent);
        JSONArray ticketNumbers = ticketBook.getJSONArray("ticketNumbers");
        for(int index =0;index<ticketNumbers.length();index++){
            JSONObject ticket = ticketNumbers.getJSONObject(index);
            if(pcnInfo.pcnNumber.equalsIgnoreCase(ticket.getString("ticketReference").trim())){
                ticket.remove("dateUsed");
                ticket.put("dateUsed", sdf.format(new Date()));
                usedTicketFound = true;
                break;
            }
        }
        if(usedTicketFound){
            SaveUpdatedTicketBook(ticketBookFile,ticketBook.toString());
        }
    }
    private String GetTicketBookContent(File ticketBookFile) throws Exception {
        FileInputStream is = new FileInputStream(ticketBookFile);
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    private void SaveUpdatedTicketBook(File ticketBookFile, String fileContent) throws Exception{
        if(ticketBookFile.exists())ticketBookFile.delete();
        FileOutputStream os = new FileOutputStream(ticketBookFile, false);
        os.write(fileContent.getBytes());
        os.close();
    }
    //pcn disorder problem
    private void UpdatePCNPhotosAndNotes() {
        String currentFilePath;
        //update the pcn photos names and database
        List<PCNPhotoTable> pcnPhotos = DBHelper.PhotosForPCN(pcnInfo.observationNumber);
        int n = 0;
        for (PCNPhotoTable pcnPhoto : pcnPhotos) {
            //make sure this is the file name or full path
            currentFilePath = pcnPhoto.getFileName();
            if (!currentFilePath.contains(pcnInfo.pcnNumber)) {
                File currentFile = new File(currentFilePath);
                if (currentFile.exists()) {
                    File newFile = new File(cameraImageHelper.getPCNPhotoFolder() + File.separator + pcnInfo.pcnNumber + "-photo-" + n + ".jpg");
                    if (currentFile.renameTo(newFile)) {
                        pcnPhoto.setFileName(newFile.getAbsolutePath());
                        pcnPhoto.save();
                    } else {
                        Log.e(TAG, "Unable to rename " + currentFile.getAbsolutePath());
                    }
                    n = n + 1;
                }
            }
        }
        //update the pcn notes names and database
        List<NotesTable> pcnNotes = DBHelper.NotesForPCN(pcnInfo.observationNumber);
        n = 0;
        for (NotesTable pcnNote : pcnNotes) {
            currentFilePath = pcnNote.getFileName();
            if (!currentFilePath.contains(pcnInfo.pcnNumber)) {
                File currentFile = new File(currentFilePath);
                if (currentFile.exists()) {
                    n = n + 1;
                    File newFile = new File(cameraImageHelper.getPCNNoteFolder() + File.separator + pcnInfo.pcnNumber + "-note-" + n + ".svg");
                    if (currentFile.renameTo(newFile)) {
                        pcnNote.setFileName(newFile.getAbsolutePath());
                        pcnNote.save();
                    } else {
                        Log.e(TAG, "Unable to rename " + currentFile.getAbsolutePath());
                    }
                }
            }
        }
    }
    //include PCN number, VRM, endtime, and observation date in tour report
    private void LogLocationForPCN(String vrm, String ticketNumber, boolean isPublishCeoTracking) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        String noticeDate = sdf.format(new Date());
        sdf = new SimpleDateFormat("HH:mm:ss");
        String noticeTime = sdf.format(new Date(pcnInfo.issueTime));
        String observationDate = noticeDate + " " + noticeTime;
        String stName = VisualPCNListActivity.currentStreet != null ? VisualPCNListActivity.currentStreet.streetname : "";
        LocationLogTable logEntry = new LocationLogTable();
        logEntry.setLogTime(new DateTime());
        logEntry.setCeoName(DBHelper.getCeoUserId());
        logEntry.setStreetName(stName);
        logEntry.setTicketNumber(ticketNumber);

        if(ticketNumber==null){
            ticketNumber="";
        }

        if(!ticketNumber.isEmpty()){
            logEntry.setVRM(vrm);
        }
        else{
            logEntry.setVRM(CeoApplication.getRecordObservationVRM()?vrm:CeoApplication.getRecordObservationVRMValue());
        }

        logEntry.setObservationDate(observationDate);
        logEntry.setLongitude(VisualPCNListActivity.longitude);
        logEntry.setLattitude(VisualPCNListActivity.latitude);
        logEntry.setStartTime(CeoApplication.OBS_START_TIME);
        logEntry.setEndTime(new Date().getTime());
        logEntry.save();

        if(isPublishCeoTracking)
            publishCeoTracking();
    }

    private void publishCeoTracking(){
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject ceoObject = new JSONObject();
            JSONObject latLong = new JSONObject();
            latLong.put("lat", VisualPCNListActivity.latitude);
            latLong.put("long", VisualPCNListActivity.longitude);
            JSONObject data = new JSONObject();
            data.put("currentstreet", pcnInfo == null ? "" : pcnInfo.location.streetCPZ.streetname);
            data.put("ceoshouldernumber", DBHelper.getCeoUserId());
            data.put("datetimeoflogin", AppConstant.ISO8601_DATE_TIME_FORMAT.format(sharedPreferenceHelper.getLong(AppConstant.LOGIN_TIME)/*CeoApplication.ceoLoginTime*/));
            BreakTable breakTable = DBHelper.getBreakData(DBHelper.getCeoUserId());
            String breakStartTime = "", breakType =  "";
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

    View.OnClickListener btnPDClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fm = getSupportFragmentManager();
            DateTime now = DateTime.now();
            String serial = "";
            if (pcnInfo.pdTicketsList.size()>0) {
                PDTicket pdTicket = pcnInfo.pdTicketsList.get(0);
                now = new DateTime(pdTicket.timeMillis);
                serial = pdTicket.serialNo;
            }
            RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                    .newInstance(PCNLoggingActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                            DateFormat.is24HourFormat(PCNLoggingActivity.this), getResources().getString(R.string.pdReference), getResources().getString(R.string.pdButton),serial);
            timePickerDialog.setThemeDark(true);
            timePickerDialog.show(fm, "");
        }
    };

    View.OnClickListener btnPCNNotesClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AnalyticsUtils.trackNoteskButton();
            PopupMenu popup = new PopupMenu(PCNLoggingActivity.this, btnPCNNotes);
            popup.getMenuInflater().inflate(R.menu.notes_pop, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Intent notesIntent;
                    boolean isItDiagram = false;
                    if (item.getItemId() == R.id.mnuDrwNotes) {
                        isItDiagram = true;
                        notesIntent = new Intent(PCNLoggingActivity.this, NotesActivity.class);
                    } else {
                        notesIntent = new Intent(PCNLoggingActivity.this, TextNoteActivity.class);
                    }
                    //restrict the user to take only one diagram note
                    if (isItDiagram && pcnInfo.diagramNoteTaken) {
                        CroutonUtils.info(PCNLoggingActivity.this, "You can take a single diagram note per PCN");
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

    View.OnClickListener btnPermitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PermitBadge obj = null;
            if (pcnInfo.permitBadgeList.size() > 0)
                obj = pcnInfo.permitBadgeList.get(0);
            PermitBadgeInfoDialog permitBadgeInfoDialog = PermitBadgeInfoDialog.NewInstance(obj, PermitBadge.PERMIT_TYPE.PARKING_PERMIT);
            permitBadgeInfoDialog.setCancelable(false);
            permitBadgeInfoDialog.show(getFragmentManager(), "");
        }
    };

    View.OnClickListener btnTaxClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TaxDisc obj = null;
            if (pcnInfo.taxDiscs.size() > 0)
                obj = pcnInfo.taxDiscs.get(0);
            PermitBadgeInfoDialog pd = PermitBadgeInfoDialog.NewInstance(obj, PermitBadge.PERMIT_TYPE.TAX_DISC);
            pd.setCancelable(false);
            pd.show(getFragmentManager(), "");

        }
    };

    View.OnClickListener locationClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openOSDialog();
        }
    };

    private final Runnable mClockTick = new Runnable() {
        boolean mVisible = true;
        final static int TIME_PERIOD_MS = 1000;
        final static int SPLIT = TIME_PERIOD_MS / 2;

        @Override
        public void run() {
            // Setup for blinking
            boolean visible = Utils.getTimeNow() % TIME_PERIOD_MS < SPLIT;
            boolean toggle = mVisible != visible;
            mVisible = visible;
            if (oTimer.mState == TimerObj.STATE_RUNNING || oTimer.mState == TimerObj.STATE_TIMESUP) {
                long timeLeft = oTimer.updateTimeLeft(false);
                if (oTimer.mView != null) {
                    ((TimerListItem) (oTimer.mView)).setTime(timeLeft, false);
                    // Update button every 1/2 second
                    if (toggle) {
                        //TODO: might use an easy way to add mins
                        /*    ImageButton leftButton = (ImageButton)
                                  t.mView.findViewById(R.id.timer_plus_one);
                            leftButton.setEnabled(canAddMinute(t));*/
                    }
                }
            }
            if (oTimer.mTimeLeft <= 0 && (oTimer.mState != TimerObj.STATE_DONE || oTimer.mState != TimerObj.STATE_INSTANT)&& oTimer.mState != TimerObj.STATE_RESTART) {
                oTimer.mState = TimerObj.STATE_DONE;
                btnPrint.setEnabled(true);
                if (oTimer.mView != null) {
                    ((TimerListItem) (oTimer.mView)).timesUp();
                    oTimer.mView.setVisibility(View.GONE);
                    btnPermitBadge.setImageDrawable(getResources().getDrawable(R.drawable.permit_selector));
                    btnTax.setImageDrawable(getResources().getDrawable(R.drawable.tax_selector));
                    btnPDTicket.setImageDrawable(getResources().getDrawable(R.drawable.pd_selector));
                    btnAdditionalInfo.setImageDrawable(getResources().getDrawable(R.drawable.other_selector));
                    if(CeoApplication.AllowContraventionChange()){
                        OpenContraventionChangeDialog(true);
                    }else{
                        OpenMultiLogDialog();
                    }
                }
            }else{
                tickHandler.postDelayed(mClockTick, 20);
            }
        }
    };

    private void OpenContraventionChangeDialog(final boolean multiLogApplicable){
        if (!PCNLoggingActivity.this.isDestroyed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PCNLoggingActivity.this);
            builder.setMessage("Is the contravention code still valid ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setCancelable(false)
                    .setTitle("Confirm contravention");
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (multiLogApplicable)
                        OpenMultiLogDialog();
                }
            });
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContraventionChangeDialog contraventionChangeDialog = ContraventionChangeDialog.NewInstance(pcnInfo,multiLogApplicable);
                    contraventionChangeDialog.setCancelable(false);
                    contraventionChangeDialog.show(getFragmentManager(), "");
                    alertDialog.dismiss();
                }
            });
        }
    }

    private void OpenMultiLogDialog(){
        if (pcnInfo.contravention.contraventionType == AppConstant.CONTRAVENTION_DUAL_LOG) {
            if (!PCNLoggingActivity.this.isDestroyed()) {
                MultiLogDialog md = MultiLogDialog.newInstance();
                md.setCancelable(false);
                md.show(getFragmentManager(), "");
            }
        }
    }

    private byte[] asByteArray(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        try {
            for (int index = 0; index < bytes.length; index++) {
                String byteStr = hex.substring(index * 2, (index + 1) * 2);
                bytes[index] = (byte) Integer.parseInt(byteStr, 16);
            }
        } catch (IndexOutOfBoundsException e) {
        } catch (NumberFormatException e) {
        }
        return bytes;
    }

    private void PrintBitmap() {
        PrintPCN();
    }

    private String getHtmlTemplate() {
        String htmlContent = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String noticeDate = sdf.format(new Date());
            sdf = new SimpleDateFormat("HH:mm");
            String noticeTime = sdf.format(new Date(pcnInfo.issueTime));
            StringBuffer textBuffer = new StringBuffer("");
            try {
                FileInputStream fileIn;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Build.MODEL.equalsIgnoreCase("SM-N9005")){
                        fileIn = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "pcn3442.txt");
                    }else{
                        fileIn = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "pcn442.txt");
                    }
                }else{
                    fileIn = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "pcn1.txt");
                }
                InputStreamReader inReader = new InputStreamReader(fileIn);
                BufferedReader bufferedContentsReader = new BufferedReader(inReader);
                String readString = bufferedContentsReader.readLine();
                while (readString != null) {
                    textBuffer.append(readString);
                    readString = bufferedContentsReader.readLine();
                }
                inReader.close();
            } catch (Exception e) {
                e.printStackTrace();
                try{
                    CeoApplication.LogError(e.getMessage());
                    OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            htmlContent = textBuffer.toString();
            String barcode_data = pcnInfo.pcnNumber;
            WeakReference<Bitmap> bitmapBarcode = encodeAsBitmap(barcode_data, BarcodeFormat.CODE_128, 600, 300);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapBarcode.get().compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytesContent = stream.toByteArray();
            String base64string = Base64.encodeBytes(bytesContent);
            String embeddedImage;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.MODEL.equalsIgnoreCase("SM-N9005")){
                    embeddedImage = "<span><img width=\"75\" height=\"35\" src=\"data:image/gif;base64," + base64string + "\" /></span>";
                }else {
                    embeddedImage = "<span><img width=\"125\" height=\"50\" src=\"data:image/gif;base64," + base64string + "\" /></span>";
                }
            }else{
                embeddedImage = "<span><img width=\"250\" height=\"100\" src=\"data:image/gif;base64," + base64string + "\" /></span>";
            }
            htmlContent = htmlContent.replace("#barcode#", embeddedImage);
            htmlContent = htmlContent.replaceAll("#date#", noticeDate);
            htmlContent = htmlContent.replaceAll("#time#", noticeTime);
            htmlContent = htmlContent.replaceAll("#pcn#", pcnInfo.pcnNumber);
            htmlContent = htmlContent.replaceAll("#vrm#", pcnInfo.registrationMark);
            htmlContent = htmlContent.replaceAll("#vehicle#", pcnInfo.manufacturer.name);
            htmlContent = htmlContent.replaceAll("#fullamount#", String.valueOf(pcnInfo.fullPrice));
            htmlContent = htmlContent.replaceAll("#contraventioncode#", pcnInfo.contravention.contraventionCode + pcnInfo.contravention.selectedSuffix.toLowerCase());
            htmlContent = htmlContent.replaceAll("#ceo#", DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
            htmlContent = htmlContent.replaceAll("#colour#", pcnInfo.colourName);
            htmlContent = htmlContent.replaceAll("#location#", pcnInfo.location.streetCPZ.streetname);
            htmlContent = htmlContent.replaceAll("#contraventiondescription#", FormatDescription(pcnInfo.contravention.contraventionCode + pcnInfo.contravention.selectedSuffix.toLowerCase() + " " +
                    pcnInfo.contravention.contraventionDescription +
                    pcnInfo.contravention.codeSuffixDescription));
            htmlContent = htmlContent.replaceAll("#discountamount#", String.valueOf(pcnInfo.halfPrice));
            htmlContent = htmlContent.replaceAll("#contraventiondate#", noticeDate);
            htmlContent = htmlContent.replaceAll("#variablebreak#", changeSpacing());
        } catch (Exception e) {
            e.printStackTrace();
            CroutonUtils.error(PCNLoggingActivity.this, "Error printing - 829");
            try{
                CeoApplication.LogError(e.getMessage());
                OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return htmlContent;
    }


    private String changeSpacing() {
        String varBreak = "";
        descLines = descLines - 3;
        while (descLines < 2) {
            varBreak += "<br />";
            descLines++;
        }
        return varBreak;
    }

    private String FormatDescription(String desc) {
        Integer diff = 0;
        final String newLine ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.MODEL.equalsIgnoreCase("SM-N9005")){
                LINE_LENGTH =60;
                newLine = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;";
            }else{
                newLine = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            }
        }else{
            newLine = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        if (desc.length() > LINE_LENGTH) {
            String[] res = desc.split(" ");
            String out = "";
            String temp = "";
            String lastLine = "";
            for (String line : res) {
                if ((temp.length() + line.length() + 1 + diff) <= LINE_LENGTH) {
                    temp += line + "&nbsp;";
                    diff -= 5;
                    lastLine = "";
                } else {
                    out += temp + "&nbsp;";
                    diff = -5;
                    out += newLine;
                    descLines++;
                    temp = line + "&nbsp;";
                    lastLine = temp;
                }
            }

            if (lastLine.length() == 0) {
                lastLine = temp;
            }
            out += lastLine;

            return out;
        } else
            return desc;
    }

    public WeakReference<Bitmap> encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {

            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888));
        bitmapWeakReference.get().setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmapWeakReference;
    }

    public void PrintPCN() {
        try {
            new AsyncTask<Void, TaskProgress, Void>() {
                @Override
                protected void onPreExecute() {
                    progressDialog.setMessage("Printing, please wait..");
                    progressDialog.setProgress(0);
                    progressDialog.show();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    Runtime.getRuntime().gc();
                    Runtime.getRuntime().freeMemory();
                    try {
                        publishProgress(new TaskProgress(10, "Printing, please wait ...", "Printing"));
                        //boolean isApplicableForWarning = false;
                        /*boolean isApplicableForWarningPrinting = false;
                        List<WarningNotice> warningNoticeList = pcnInfo.location.streetCPZ.warningNoticeConfiguration;
                        //WarningNotice matchedWarningNotice = null;
                        List<WarningNotice> matchedWarningNotice=new ArrayList<>();
                        if(warningNoticeList.size() > 0){
                            for(WarningNotice warningNotice : warningNoticeList){
                                String warningContraventionCode = warningNotice.contraventionCode;
                                if(warningContraventionCode.equalsIgnoreCase("All")){
                                    //isApplicableForWarning = true;
                                    matchedWarningNotice.add(warningNotice);
                                    //matchedWarningNotice = warningNotice;
                                    //break;
                                }
                                if(warningContraventionCode.equalsIgnoreCase(pcnInfo.contravention.contraventionCode)){
                                    //isApplicableForWarning = true;
                                    matchedWarningNotice.add(warningNotice);
                                    //matchedWarningNotice = warningNotice;
                                    //break;
                                }
                            }
                        }*/
                        publishProgress(new TaskProgress(20, "Printing, please wait ....", "Printing"));
                       /* if(matchedWarningNotice.size()>0){
                            for(WarningNotice warningNotice:matchedWarningNotice){
                                String warningStartDate = warningNotice.warningStartDate;
                                String warningEndDate = warningNotice.warningEndDate;
                                Date wStartDate = DateUtils.getDate(warningStartDate, "dd/MM/yyyy");
                                Date wEndDate = DateUtils.getDate(warningEndDate, "dd/MM/yyyy");
                                isApplicableForWarningPrinting = DateUtils.isWithinRange(wStartDate, wEndDate);
                                if(isApplicableForWarningPrinting)
                                    break;
                            }
                        }*/
                        String xmlFile;
                        boolean isApplicableForWarningPrinting=Utils.checkWarningNotice(pcnInfo,pcnInfo.contravention.contraventionCode);
                        if(isApplicableForWarningPrinting){
                            pcnInfo.warningNotice = "true";
                            xmlFile = Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "SeikoPrintFormatWarn.xml";;
                        }else {
                            pcnInfo.warningNotice = "false";
                            xmlFile = Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "SeikoPrintFormat.xml";
                        }
                        publishProgress(new TaskProgress(20, "Printing, please wait .....", "Printing"));
                        byte[] retVal = null;
                        seikoTemplateProcessor stp = new seikoTemplateProcessor();
                        stp.templatePath = xmlFile;
                        stp.pageWidth = CeoApplication.getPageWidth();//40;
                        stp.setTestData = false;
                        SimpleDateFormat dateOnly = new SimpleDateFormat("dd/MM/yyyy");
                        String noticeDate = dateOnly.format(new Date(pcnInfo.issueTime));
                        SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm");
                        String noticeTime = timeOnly.format(new Date(pcnInfo.issueTime));
                        stp.PCN = pcnInfo.pcnNumber;
                        stp.warningnotice = String.valueOf(isApplicableForWarningPrinting);
                        //HAN-79
                        if(CeoApplication.getBarcodeType().equalsIgnoreCase("normal")){
                            stp.barCode = pcnInfo.pcnNumber;
                        }else if(CeoApplication.getBarcodeType().equalsIgnoreCase("paypoint")){
                            //105,48-
                            StringBuilder sb=new StringBuilder();
                            sb.append(CeoApplication.getPaypointClientID())
                                    .append(CeoApplication.getPaypointClientParam())
                                    .append(pcnInfo.pcnNumber.substring(2).replace("A","0"));

                            stp.barCode = encode_paypoint_barcode(sb.toString(),calculateCheckDigit(sb.toString()));
                        }

                        stp.contraventionDate = noticeDate;
                        stp.contraventionTime = noticeTime;
                        stp.contraventionLocation = pcnInfo.location.streetCPZ.streetname;
                        stp.exactLocation=pcnInfo.location.outside;
                        stp.contraventionDiscountFee = String.valueOf(pcnInfo.halfPrice);
                        stp.contraventionFullFee= String.valueOf(pcnInfo.fullPrice);
                        stp.VRM = pcnInfo.registrationMark;
                        stp.contraventionDescription = pcnInfo.contravention.contraventionDescription + pcnInfo.contravention.codeSuffixDescription;
                        stp.contraventionCode = pcnInfo.contravention.contraventionCode;
                        stp.contraventionSuffix = pcnInfo.contravention.selectedSuffix.toLowerCase();
                        stp.vehicleMake = pcnInfo.manufacturer.name;
                        stp.vehicleModel = pcnInfo.model.modelName;
                        stp.vehicleColour = pcnInfo.colourName;
                        stp.shoulderNumber = DBHelper.getCeoUserId();
                        publishProgress(new TaskProgress(20, "Printing, please wait ......", "Printing"));
                        if (pcnInfo.observationTime != 0) {
                            stp.obsStartDate = dateOnly.format(new Date(pcnInfo.logTime));
                            stp.obsStartTime = timeOnly.format(new Date(pcnInfo.logTime));
                        }
                        else {
                            stp.obsStartDate = dateOnly.format(new Date(pcnInfo.issueTime));
                            stp.obsStartTime = timeOnly.format(new Date(pcnInfo.issueTime));
                        }
                        stp.obsEndDate = dateOnly.format(new Date(pcnInfo.issueTime));
                        stp.obsEndTime  = timeOnly.format(new Date(pcnInfo.issueTime));
                        publishProgress(new TaskProgress(20, "Printing, please wait ......", "Printing"));
                        retVal = stp.returnTemplate();
                        mPrinterManager.sendBinary(retVal);
                        //Thread.sleep(500);
                        Thread.currentThread().sleep(1000);
                        publishProgress(new TaskProgress(10, "DONE !", "Printing"));
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pcnInfo.dInfo.pcnDestination < 0)
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                synchronized (Thread.currentThread()) {
                                                    Thread.currentThread().wait(1000);
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setDestinationInfo();
                                                }
                                            });
                                        }
                                    }).start();
                                else {
                                    progressDialog.dismiss();
                                    EnableToolbar(true);
                                    btnCancel.setEnabled(false);
                                }
                            }
                        });*/

                        try{
                            if(mPrinterManager.isConnect())
                                mPrinterManager.disconnect();
                        }catch (final PrinterException e){
                            e.printStackTrace();
                        }

                    } catch (final PrinterException e) {
                        //Assigning isUsed = false and issueTime = 0 because Display msg "To be issued" incase printer port closed msg
                        pcnInfo.isUsed = false;
                        pcnInfo.issueTime = 0;
                        try {
                            CeoApplication.LogError(e.getMessage());

                            OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CroutonUtils.error(PCNLoggingActivity.this, e.getMessage());
                                //The data whose data size is 0 byte was specified.
                                btnPrint.setVisibility(View.VISIBLE);
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        try {
                            CeoApplication.LogError(e.getMessage());
                            OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            CeoApplication.LogError(e.getMessage());
                            OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(TaskProgress... values) {
                    progressDialog.incrementProgressBy(values[0].percentage);
                    if (progressDialog.getProgress() == 100) {
                        try {
                            synchronized (Thread.currentThread()) {
                                progressDialog.setMessage(values[0].message);
                                Thread.currentThread().wait(1000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                protected void onPostExecute(Void params) {
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    if (pcnInfo.dInfo.pcnDestination < 0){
                        setDestinationInfo();
                    }else{
                        EnableToolbar(true);
                        btnCancel.setEnabled(false);
                    }
                }

            }.execute(null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                CeoApplication.LogError(e.getMessage());
                OnException(PCNLoggingActivity.this, e, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private  String encode_paypoint_barcode(String identifier, String checkDigit){
        // step1 and step2
        int pairs[]=makePair(identifier);

        // step3
        StringBuilder barCodeString =new StringBuilder("i");
        for (int val:pairs){
            char asciiValue=(char)val;
            barCodeString.append(asciiValue);
        }

        // Step 4 and step 5
        barCodeString.append("d");

        // step6
        int hashTotal=0;
        int index=0;
        for(int val:pairs){
            hashTotal=hashTotal+(val*(index+1));
            index++;
        }

        // step7
        hashTotal=hashTotal+105;

        // step8
        hashTotal=hashTotal+1000;

        // step9
        int asciiCheck=(int)checkDigit.charAt(0);
        int checkValue=asciiCheck-32;
        char val=(char) checkValue;
        barCodeString.append(val);

        // step10
        int newVal=checkValue*11;
        hashTotal=hashTotal+newVal;

        // step11
        int hashTotalMod=hashTotal%103;

        //step12
        hashTotalMod=hashTotalMod+32;
        char hashTotalModAscii=(char) hashTotalMod;
        barCodeString.append(hashTotalModAscii);

        //step13
        barCodeString.append("j");

        return barCodeString.toString();

    }

    private  int[] makePair(String str) {

        //Step1
        List<String> strList=splitToNChar(str,2);

        // Step2
        int array[]=new int[strList.size()];
        int index=0;
        for(String val : strList) {
            array[index]=Integer.parseInt(val);
            index++;
        }
        return array;
    }

    /**
     * Split text into n number of characters.
     *
     * @param text the text to be split.
     * @param size the split size.
     * @return an array of the split text.
     */
    private  List<String> splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        //.toArray(new String[0]);
        return parts;
    }



    /**
     * Calculates the last digits for the card number received as parameter
     *
     * @param card
     *           {@link String} number
     * @return {@link String} the check digit
     */
    public static String calculateCheckDigit(String card) {
        if (card == null)
            return null;
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2)	{
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

    public int getRandomNumber(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }


    private void openOSDialog()
    {
        String currentLoc = "";
        if (pcnInfo.location.outside != null)
            currentLoc = pcnInfo.location.outside;

        OSLocationDialog locationDialog = new OSLocationDialog(currentLoc);
        locationDialog.setCancelable(false);
        locationDialog.show(getFragmentManager(), "");

    }

    private boolean isRequiredInfoAdded() {
        if (!pcnInfo.hasOSLocation()) {
            openOSDialog();
            return false;
        } else {
            if (!pcnInfo.hasValvePositions()) {
                if(CeoApplication.AlwaysRequireValves()) {
                    TempValveDialog valveDialog = new TempValveDialog();
                    valveDialog.setCancelable(false);
                    valveDialog.show(getFragmentManager(), "");
                    return false;
                }else{
                    if (Arrays.asList(VALVES_REQUIRED_CODES).contains(pcnInfo.contravention.contraventionCode)) {
                        TempValveDialog valveDialog = new TempValveDialog();
                        valveDialog.setCancelable(false);
                        valveDialog.show(getFragmentManager(), "");
                        return false;
                    }else{
                        pcnInfo.valveBack = -1;
                        pcnInfo.valveFront = -1;
                        EnableToolbar(true);
                        btnCancel.setEnabled(!completed);
                        if (oTimer.isTicking())btnPrint.setEnabled(false);
                        return true;
                    }
                }
            } else {
                //EnableToolbar(true);
                btnCancel.setEnabled(!completed);
                return true;
            }
        }
    }

    private boolean isSecondRequiredInfoAdded(boolean onMoved) {
        if (!pcnInfo.hasOSLocation2()) {
            openOSDialog();
            return false;
        } else {
            if (!pcnInfo.hasValvePositions2()) {
                if(CeoApplication.AlwaysRequireValves()) {
                    TempValveDialog valveDialog = TempValveDialog.NewInstance(1, 1, true);
                    valveDialog.setCancelable(false);
                    valveDialog.show(getFragmentManager(), "");
                    return false;
                }else{
                    if (Arrays.asList(VALVES_REQUIRED_CODES).contains(pcnInfo.contravention.contraventionCode)) {
                        TempValveDialog valveDialog = TempValveDialog.NewInstance(1, 1, true);
                        valveDialog.setCancelable(false);
                        valveDialog.show(getFragmentManager(), "");
                        return false;
                    }else {
                        pcnInfo.valveBack2 = -1;
                        pcnInfo.valveFront2 = -1;
                        if(onMoved) {
                            return true;
                        }else{
                            char[] sfArr = pcnInfo.contravention.codeSuffixes.toCharArray();
                            SuffixDialog suffixDialog = SuffixDialog.NewInstance(sfArr);
                            suffixDialog.setCancelable(false);
                            suffixDialog.show(getFragmentManager(), "");
                            return false;
                        }
                    }
                }
            } else {
                EnableToolbar(true);
                if (oTimer.isTicking())
                    btnPrint.setEnabled(false);
                return true;

            }
        }
    }

    @Override
    public void OnPrinterError(PrinterException pe, final boolean reprint) {
        super.OnPrinterError(pe, reprint);
        if (progressDialog.isShowing())
            progressDialog.dismiss();
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EnableToolbar(true);
                btnPrint.setEnabled(false);
                if(reprint){
                    btnCancel.setEnabled(false);
                }else{
                    long pcnPrintTime = DBHelper.getPCNPrintTime(pcnInfo.pcnNumber);
                    if(pcnPrintTime > 0){
                        btnCancel.setEnabled(false);
                    }else {
                        btnCancel.setEnabled(true);
                    }
                }
            }
        });
        CroutonUtils.error(this, pe.getMessage());
    }

    @Override
    public void OnPrinterSuccess(boolean reprint) {
        super.OnPrinterSuccess(reprint);
        if(reprint){
            PrintBitmap();
        }else {
                if (pcnInfo.pcnNumber == null || pcnInfo.pcnNumber.length() == 0) {
                String ticketNumber = DBHelper.getNextTicketNumber();
                if (!ticketNumber.isEmpty()) {
                    pcnInfo.pcnNumber = ticketNumber;
                } else {
                    CroutonUtils.error(5000, PCNLoggingActivity.this, "No PCN is available in ticket book.\n Please contact your supervisor immediately.");
                    return;
                }
                //DBHelper.usePCNNumber(pcnInfo.pcnNumber);
                UpdatePCNPhotosAndNotes();
            }
            pcnInfo.locationStreetName = pcnInfo.location.streetCPZ.streetname;
            SavePCN();
            oTimer.pcnJSON = pcnInfo.toJSON();
            oTimer.writeToDB();
            pcnInfo.isUsed = true;
            btnPrint.setVisibility(View.GONE);
            btnCancel.setEnabled(false);
            // Done , because observation time was not correct
            DBHelper.markPCNPrinted(pcnInfo.pcnNumber, DateTime.now().getMillis());

            long pcnPrintTime = DBHelper.getPCNPrintTime(pcnInfo.pcnNumber);
            if (pcnPrintTime == 0) {
                pcnInfo.issueTime = DateTime.now().getMillis();
            } else {
                pcnInfo.issueTime = pcnPrintTime;
            }
            if (oTimer.mState != TimerObj.STATE_INSTANT)
                oTimer.mState = TimerObj.STATE_DONE;
            LogLocationForPCN(pcnInfo.registrationMark, pcnInfo.pcnNumber, true);
            PrintBitmap();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isRequiredInfoAdded()) {
                returnToList();
                return false;
            } else {
                return false;
            }
        } else {
            super.onKeyUp(keyCode, event);
            return false;
        }
    }

    private void returnToList() {
        try
        {
            Intent timerIntent = new Intent();
            oTimer.pcnJSON = pcnInfo.toJSON();
            oTimer.writeToDB();
            timerIntent.putExtra("timer", oTimer);
            timerIntent.putExtra("PCN", pcnInfo);
            setResult(CeoApplication.RESULT_CODE_PCNLIST, timerIntent);
            finish();
        } catch (final Exception exc) {
            OnException(PCNLoggingActivity.this, exc, ErrorLocations.location401);
            setResult(CeoApplication.RESULT_CODE_ERROR);
            finish();
        }
    }

    private void cancelPCN() {
        final AlertDialog checkCancel = new AlertDialog.Builder(this).create();
        checkCancel.setCancelable(false);
        checkCancel.setTitle("Cancel PCN ?");
        checkCancel.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //restrict the user to take only one diagram note
                btnPrint.setEnabled(false);
                btnPrint.setClickable(false);
                pcnInfo.diagramNoteTaken = false;
                deleteUnusedPhotos(pcnInfo.observationNumber);
                deleteUnusedNotes(pcnInfo.observationNumber);
                if (pcnInfo.pcnNumber != null && pcnInfo.pcnNumber.length() > 0) {
                    DBHelper.releasePCNNumber(pcnInfo.pcnNumber);
                }
                //TODO: activate print click but not require because we are finishing the activity
                Intent data = new Intent();
                data.putExtra("id", oTimer.mTimerId);
                setResult(CeoApplication.RESULT_CODE_OBS_CANCEL, data);
                checkCancel.dismiss();
                finish();
            }
        });
        checkCancel.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkCancel.dismiss();
                //TODO: activate print click
            }
        });
        checkCancel.show();

    }

    private void setDestinationInfo() {
        //DBHelper.markPCNPrinted(pcnInfo.pcnNumber, DateTime.now().getMillis());
        btnPrint.setVisibility(View.VISIBLE);
        locationTextView.setEnabled(false);
        btnCancel.setEnabled(false);
        progressDialog.dismiss();
        DestinationDialog destinationDialog = DestinationDialog.NewInstance(pcnInfo.dInfo);
        destinationDialog.setCancelable(false);
        destinationDialog.show(getFragmentManager(), "");
    }

    private void handleIntent(Intent intent) {
        // TODO: handle Intent
    }

    @Override
    public void onLocationSave(String loc, Boolean isSecondLoc) {
        if (!isSecondLoc) {
            locationTextView.setText(loc);
            pcnInfo.location.outside = loc;
            isRequiredInfoAdded();
        } else {
            locationTextView.setText(locationTextView.getText() + loc);
            pcnInfo.location2.outside = loc;
            isSecondRequiredInfoAdded(false);
        }
    }

    @Override
    public void onLocationCancel() {
        // Insist that a location is needed.
    }

    @Override
    public void OnTaxEntered(DateTime expiry, String taxNum) {
/*        TaxDisc tx = new TaxDisc();
        tx.expiryDate = expiry.toString();
        tx.isSeen = true;
        tx.serialNo = taxNum;
        tx.dateMillis = expiry.getMillis();
        pcnInfo.taxDiscs.add(tx);*/
    }

    @Override
    public void OnDBadgeEntered(DateTime expiry, String badgeNum) {
        /*PermitBadge permit = new PermitBadge();
        permit.expiryDate = expiry.toString();
        permit.isSeen = true;
        permit.permitType = PermitBadge.PERMIT_TYPE.DISABLED_BADGE;
        permit.serialNo = badgeNum;
        permit.dateMillis = expiry.getMillis();
        pcnInfo.permitBadgeList.add(permit);*/
    }

    @Override
    public void OnTaxEdited(Long expiry, String taxNum) {
        TaxDisc tx = new TaxDisc();
        tx.expiryDate = expiry.toString(); //permitDateFormat.format(expiry);
        tx.isSeen = true;
        tx.serialNo = taxNum;
        tx.dateMillis = expiry;
        if (pcnInfo.taxDiscs.size() > 0)
            pcnInfo.taxDiscs.set(0, tx);
        else
            pcnInfo.taxDiscs.add(tx);
    }

    @Override
    public void OnDBadgeEdited(Long expiry, String badgeNum) {
        PermitBadge permit = new PermitBadge();
        permit.expiryDate = expiry.toString();//permitDateFormat.format(expiry);
        permit.isSeen = true;
        permit.serialNo = badgeNum;
        permit.dateMillis = expiry;
        permit.permitType = PermitBadge.PERMIT_TYPE.DISABLED_BADGE;
        if (pcnInfo.permitBadgeList.size() > 0)
            pcnInfo.permitBadgeList.set(0, permit);
        else
            pcnInfo.permitBadgeList.add(permit);
    }

    @Override
    public void OnPermitEdited(Long expiry, String permitNum) {
        PermitBadge permit = new PermitBadge();
        permit.expiryDate = expiry.toString();//permitDateFormat.format(expiry);
        permit.isSeen = true;
        permit.permitType = PermitBadge.PERMIT_TYPE.PARKING_PERMIT;
        permit.serialNo = permitNum;
        permit.dateMillis = expiry;
        if (pcnInfo.permitBadgeList.size() > 0)
            pcnInfo.permitBadgeList.set(0, permit);
        else
            pcnInfo.permitBadgeList.add(permit);
    }

    //TODO: need to implement differentiation

    @Override
    public void OnPermitEntered(DateTime expiry, String permitNum) {
        /*PermitBadge permit = new PermitBadge();
        permit.expiryDate = expiry.toString();
        permit.isSeen = true;
        permit.permitType = PermitBadge.PERMIT_TYPE.PARKING_PERMIT;
        permit.serialNo = permitNum;
        permit.dateMillis = expiry.getMillis();
        pcnInfo.permitBadgeList.add(permit);*/
    }

    @Override
    public void OnValvesEntered(Integer front, Integer back, Boolean secondLog) {
        if (secondLog) {
            pcnInfo.valveBack2 = back;
            pcnInfo.valveFront2 = front;
            char[] sfArr = pcnInfo.contravention.codeSuffixes.toCharArray();

            SuffixDialog suffixDialog = SuffixDialog.NewInstance(sfArr);
            suffixDialog.setCancelable(false);
            suffixDialog.show(getFragmentManager(), "");
        } else {
            pcnInfo.valveBack = back;
            pcnInfo.valveFront = front;
            EnableToolbar(true);
            btnCancel.setEnabled(!completed);
            if (oTimer.isTicking())
                btnPrint.setEnabled(false);

        }

    }

    @Override
    public void OnVRMConfirmed(String vrm) {
        EnableToolbar(true);
        btnCancel.setEnabled(!completed);
        isVRMConfirmed = true;
        pcnInfo.registrationMark = vrm;
        regTextView.setText(vrm.toUpperCase());
        if (CeoApplication.getCheckPermitSession()) {
            checkAutomatedVRMLookup();
        } else{
            btnPrint.setImageDrawable(getResources().getDrawable(R.drawable.print_selector));
        }
    }

    @Override
    public void OnVRMChanged(String newReg) {
        //pcnInfo.registrationMark = newReg;
        if (lastVrm.equals("")) {
            isVRMConfirmed = false;
            lastVrm = newReg;
            VRMDoubleCheckDialog vrmDoubleCheckDialog = VRMDoubleCheckDialog.newInstance(newReg);
            vrmDoubleCheckDialog.setCancelable(false);
            vrmDoubleCheckDialog.show(getFragmentManager(), "");
        } else {
            if (lastVrm.equals(newReg))
                isVRMConfirmed = true;
            else {
                CroutonUtils.error(this, "Please cancel the PCN or verify the VRM again");
                isVRMConfirmed = false;
                lastVrm = "";
                EnableToolbar(true);
            }
        }
    }

    @Override
    public void onTimeSet(RadialTimePickerDialog dialog, int hourOfDay, int minute, String serial) {
        Integer minsTotal = ((hourOfDay * 60 + minute) - DateTime.now().getMinuteOfDay());
        PDTicket newPD = new PDTicket();
        newPD.serialNo = serial;
        newPD.timeMillis = DateTime.now().plusMinutes(minsTotal).getMillis();
        newPD.expiryTime = new DateTime(newPD.timeMillis).toString();
        pcnInfo.pdTicketsList.add(newPD);
        if (pcnInfo.contravention.contraventionType == AppConstant.CONTRAVENTION_PD) {
            Long newLength = minsTotal * PCNStartActivity.ONE_MIN;
            if ((oTimer.mOriginalLength + newLength) > pcnInfo.timeplateInfo.maxTime) {
                CroutonUtils.error(this, "Exceeded maximum bay time");
                Contravention contravention30 = new Contravention(DBHelper.GetContraventionsData("30").get(0));
                pcnInfo.contravention = contravention30;
                contraTextView.setText("30");
                btnPrint.setEnabled(true);
            } else {
                timerItem.setVisibility(View.VISIBLE);
                oTimer = new TimerObj(newLength);
                oTimer.mState = TimerObj.STATE_RUNNING;
                oTimer.mView = timerItem;
                long timeLeft = oTimer.updateTimeLeft(false);
                timerItem.set(oTimer.mOriginalLength, timeLeft, false);
                timerItem.setTime(timeLeft, true);
                timerItem.start();
                tickHandler.postDelayed(mClockTick, 20);
            }
        }
    }

    @Override
    public void onDismissPDDialog() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case CeoApplication.RESULT_CODE_NOTES: {
                NotesTable newNote = new NotesTable();
                newNote.setCeoNumber(DBHelper.getCeoUserId());
                String filePath = data.getStringExtra("path");
                String textNote = data.getStringExtra("text");
                //pcn disorder problem
                File oldfile, newfile;
                pcnInfo.ticketNotes = textNote;
                if(!filePath.isEmpty()) {
                    oldfile = new File(filePath);
                    if (oldfile.exists()) {
                        Integer n = DBHelper.NotesForPCN(pcnInfo.observationNumber).size() + 1;
                        if (pcnInfo.pcnNumber != null && pcnInfo.pcnNumber.length() > 0) {
                            filePath = Environment.getExternalStorageDirectory() + "/" + AppConstant.NOTES_FOLDER +
                                    pcnInfo.pcnNumber + "-note-" + n + ".svg";
                        } else {
                            filePath = Environment.getExternalStorageDirectory() + "/" + AppConstant.NOTES_FOLDER +
                                    pcnInfo.observationNumber + "-note-" + n + ".svg";
                        }
                        newfile = new File(filePath);
                        if (!oldfile.renameTo(newfile))
                            Log.e(TAG, "Unable to rename " + oldfile.getAbsolutePath());
                    }
                }
                //restrict the user to take only one diagram note
                if(data.hasExtra("diagramNoteTaken")) {
                    pcnInfo.diagramNoteTaken = data.getBooleanExtra("diagramNoteTaken", false);
                }
                newNote.setNoteDate(DateTime.now().getMillis());
                newNote.setSubjectLine(data.getStringExtra("subject"));
                newNote.setNoteText(data.getStringExtra("text"));
                if (pcnInfo.pcnNumber !=null && pcnInfo.pcnNumber.length()>0){
                    newNote.setPcnNumber(pcnInfo.pcnNumber);
                }
                newNote.setPage(0);
                newNote.setFileName(filePath);
                newNote.setObservation(pcnInfo.observationNumber);
                newNote.save();
                //Note is taken after the PCN is printed the Note is not transferred to the back office.
                if (pcnInfo.issueTime > 0){
                    updateNotesField();
                    outPCN = new PCNJsonData(pcnInfo);
                    SavePCN();
                }
                break;
            }
            case CeoApplication.RESULT_CODE_ENDOFDAY: {
                finish();
                break;
            }
            case RESULT_OK:
                if (requestCode == CeoApplication.RESULT_CODE_NEWIMAGE) {
                    Runtime.getRuntime().gc();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String filePath = data.getStringExtra("path");
                    //pcn disorder problem
                    File oldfile, newfile;
                    oldfile = new File(filePath);
                    if (oldfile.exists()) {
                        Integer n = DBHelper.PhotosForPCN(pcnInfo.observationNumber).size();
                        if (pcnInfo.pcnNumber != null && pcnInfo.pcnNumber.length() > 0) {
                            filePath = cameraImageHelper.getPCNPhotoFolder() + "/" +
                                    pcnInfo.pcnNumber + "-photo-" + n + ".jpg";
                        }else{
                            filePath = cameraImageHelper.getPCNPhotoFolder() + "/" +
                                    pcnInfo.observationNumber + "-photo-" + n + ".jpg";
                        }
                        newfile = new File(filePath);
                        if (!oldfile.renameTo(newfile))
                            Log.e(TAG, "Unable to rename " + oldfile.getAbsolutePath());
                    }
                    File currentImageFile = new File(filePath);
                    Runtime.getRuntime().gc();
                    Runtime.getRuntime().freeMemory();
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

    private void updateNotesField() {
        //Note is taken after the PCN is printed the Note is not transferred to the back office.
        String notesContent = "";
        for (NotesTable nt : DBHelper.getPCNNotes(pcnInfo.observationNumber)) {
            String noteText = nt.getNoteText();
            if (noteText != null && !noteText.isEmpty()){
                notesContent += "Date : " + new Date(nt.getNoteDate()).toString() + "\n Subject : " + nt.getSubjectLine() + "\n\n" + "Note : " + noteText
                        + "\n\n";
            }
        }
        pcnInfo.ticketNotes = notesContent;
    }

    @Override
    public void OnDestinationInfoAdded(DestinationInfo info) {
        pcnInfo.dInfo = info;
        updateNotesField();
        EnableToolbar(true);
        btnCancel.setEnabled(false);
        pcnInfo.gpsLat = VisualPCNListActivity.latitude;
        pcnInfo.gpsLong = VisualPCNListActivity.longitude;
        pcnInfo.ceoShoulderNumber = DBHelper.getCeoUserId();
        outPCN = new PCNJsonData(pcnInfo);
        SavePCN();
        String message="";
        if(CeoApplication.PhotosRequired()){
            Integer photos = DBHelper.PhotosForPCN(pcnInfo.observationNumber).size();
            if(photos==0){
                message = CeoApplication.PhotosNotesText();
            }
        }
        if(CeoApplication.NotesRequired()){
            Integer notes = DBHelper.NotesForPCN(pcnInfo.observationNumber).size();
            if(notes==0){
                message = CeoApplication.PhotosNotesText();
            }
        }
        if(message.isEmpty()) {
            new PublishPCNAsyncTask().execute(pb, null, null);
            clickLayout.setClickable(false);
            oTimer.mState = TimerObj.STATE_DONE;
            completed = true;
        }else{
            PhotoNotesDialog(message);
        }
    }

    private void PhotoNotesDialog(String message) {
        final AlertDialog photoNotesDialog = new AlertDialog.Builder(this).create();
        photoNotesDialog.setCancelable(false);
        photoNotesDialog.setMessage(message);
        photoNotesDialog.setTitle("PCN Photos/Notes");
        photoNotesDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                photoNotesDialog.dismiss();
                new PublishPCNAsyncTask().execute(pb,null,null);
                clickLayout.setClickable(false);
                oTimer.mState = TimerObj.STATE_DONE;
                completed = true;
            }
        });
        photoNotesDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pcn_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.mnuPrinter) {
            try {
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
            } catch (PrinterException e) {
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
    public void OnOtherInfoAdded(AdditionalInfo info) {
        pcnInfo.additionalInfo = info;
    }

    @Override
    public void onSameLocation(Boolean newPD) {
        if (newPD) {
            FragmentManager fm = getSupportFragmentManager();
            DateTime now = DateTime.now();
            String serial = "";

            if (pcnInfo.pdTicketsList.size()>0) {
                PDTicket pdTicket = pcnInfo.pdTicketsList.get(0);
                now = new DateTime(pdTicket.timeMillis);
                serial = pdTicket.serialNo;
            }

            RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                    .newInstance(PCNLoggingActivity.this, now.getHourOfDay(), now.getMinuteOfHour(),
                            DateFormat.is24HourFormat(PCNLoggingActivity.this),
                            getResources().getString(R.string.pdReference), getResources().getString(R.string.pdButton), serial);
            timePickerDialog.setThemeDark(true);
            timePickerDialog.show(fm, "");

        }
        btnPrint.setEnabled(true);
    }

    @Override
    public void onMoved() {
        // issue 22
        Contravention contravention22 = new Contravention(DBHelper.GetContraventionsData("22").get(0));
        pcnInfo.contravention = contravention22;
        contraTextView.setText("22");
        locationTextView.setText("Moved from " + locationTextView.getText() + " To ");
        locationTextView.setTextSize(20f);

        if (isSecondRequiredInfoAdded(true)) {

            SuffixDialog suffixDialog = SuffixDialog.NewInstance(pcnInfo.contravention.codeSuffixes.toCharArray());
            suffixDialog.setCancelable(false);
            suffixDialog.show(getFragmentManager(), "");
        }
    }

    @Override
    public void OnNewSuffix(String suffix) {
        suffixTextView.setText(suffix.toUpperCase());
        if (oTimer.mState == TimerObj.STATE_DONE)
            btnPrint.setEnabled(true);
    }


    @Override
    public void OnSuccess() {
        super.OnSuccess();
        oTimer.pcnJSON = pcnInfo.toJSON(); // update string


    }

    @Override
    public void OnFailure() {
        super.OnFailure();
        oTimer.pcnJSON = pcnInfo.toJSON(); // update string

    }

    @Override
    public void ContraventionInfoChanged(Contravention contravention,boolean multiLogApplicable) {
        pcnInfo.contravention = contravention;
        setContraventionCharge();
        pcnInfo.contraventionChanged = "true";
        if (pcnInfo.contravention.selectedSuffix.length() == 0) {
            suffixTextView.setVisibility(View.INVISIBLE);
        } else {
            suffixTextView.setVisibility(View.VISIBLE);
        }
        contraTextView.setText(pcnInfo.contravention.contraventionCode);
        suffixTextView.setText(pcnInfo.contravention.selectedSuffix.toUpperCase());
        if (multiLogApplicable)
            OpenMultiLogDialog();
    }

    @Override
    public void vrmLookupPaidParking(ArrayList<PaidParking> paidParkings, String vrmText, boolean isError) {
        if(isError){
            showVRMAutomatedLookupErrorDialog("No Valid Permit or Paid for Parking Found", true);
        } else{
            showVRMAutomatedLookupDialog(paidParkings);
        }
    }

    @Override
    public void OnVRMLookupConfirmed(boolean isConfirmed, String data) {
        if(isConfirmed) {
            pcnInfo.secondParkingSessionCheck = data;
            btnPrint.setImageDrawable(getResources().getDrawable(R.drawable.print_selector));
        } else {
            btnPrint.setVisibility(View.VISIBLE);
            btnPrint.setEnabled(false);
        }
    }

    public static class TaskProgress {
        static int percentage;
        static String message;
        static String Title;

        TaskProgress(int percentage, String message, String title) {
            this.percentage += percentage;
            this.message = message;
            this.Title = title;
        }
    }

    private boolean compareStreetAndCPZ(String content, String compareTo){
        List<String> contentList = new ArrayList<>(Arrays.asList(content.split("\\|")));
        for(String contentStr : contentList){
            if(contentStr.equalsIgnoreCase(compareTo))
                return true;
        }
        return false;
    }

    private void checkSpecialVehicleType(String vrm){
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
                        Utils.showDialog(PCNLoggingActivity.this, message, "Message", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                publishBlackList();
                                finalPrintingPCN();
                            }
                        });
                        break;
                    case WHITELIST:
                        message = specialVehicleJson.getString("ALERTMESSAGE").isEmpty() ? "White listed Vehicle" :specialVehicleJson.getString("ALERTMESSAGE");
                        Utils.showDialog(PCNLoggingActivity.this, message, "Message", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                LogLocationForPCN("WHITELIST", "", false);
                                btnPrint.setEnabled(false);
                            }
                        });
                        break;
                    default:
                        finalPrintingPCN();
                }

            }else{
                finalPrintingPCN();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void finalPrintingPCN(){
        try {
            connectBluetooth(false);
        }catch (Exception exc) {
            /**
             *  Save PCN information
             */
            oTimer.pcnJSON = pcnInfo.toJSON();
            oTimer.writeToDB();
            try{
                CeoApplication.LogError(exc);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            OnException(PCNLoggingActivity.this, exc, ErrorLocations.location402);
            setResult(CeoApplication.RESULT_CODE_ERROR);
            finish();
        }
    }

    private void publishBlackList(){
        try {
            JSONObject mainObject = new JSONObject();
            JSONObject blackwhitelistvehicle = new JSONObject();
            blackwhitelistvehicle.put("RECORDTYPE", "black");
            blackwhitelistvehicle.put("VRM", pcnInfo.registrationMark);
            blackwhitelistvehicle.put("STREETNODE", pcnInfo.location.streetCPZ.noderef);
            blackwhitelistvehicle.put("STREETNAME", pcnInfo.location.streetCPZ.streetname);
            blackwhitelistvehicle.put("LAT", String.valueOf(VisualPCNListActivity.latitude));
            blackwhitelistvehicle.put("LON", String.valueOf(VisualPCNListActivity.longitude));
            blackwhitelistvehicle.put("CEOSHOULDER", DBHelper.getCeoUserId());
            blackwhitelistvehicle.put("HHTID", CeoApplication.getUUID());
            blackwhitelistvehicle.put("OBSERVEDAT", com.farthestgate.android.utils.DateUtils.getISO8601DateTime());
            mainObject.put("blackwhitelistvehicle", blackwhitelistvehicle);
            PubNubModule.publishBlackWhiteListVehicle(mainObject);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkAutomatedVRMLookup(){
        if (DeviceUtils.isConnected(PCNLoggingActivity.this)) {
            VRMAutomatedLookupTask vrmAutomatedLookupTask =
                    new VRMAutomatedLookupTask(PCNLoggingActivity.this, pcnInfo.registrationMark, PCNLoggingActivity.this);
            vrmAutomatedLookupTask.execute();
        } else{
            showVRMAutomatedLookupErrorDialog("No internet connectivity available at the time of search", false);
        }
    }

    private void showVRMAutomatedLookupDialog(ArrayList<PaidParking> paidParkings) {
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
                        if (isNetworkAvaiable) {
                            pcnInfo.secondParkingSessionCheck = com.farthestgate.android.utils.DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + " No Valid Permit or Paid for Parking Found";
                        } else {
                            pcnInfo.secondParkingSessionCheck = com.farthestgate.android.utils.DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + " No internet connectivity available at the time of search";
                        }

                        btnPrint.setImageDrawable(getResources().getDrawable(R.drawable.print_selector));
                    }
                })
                .show();
    }


}
