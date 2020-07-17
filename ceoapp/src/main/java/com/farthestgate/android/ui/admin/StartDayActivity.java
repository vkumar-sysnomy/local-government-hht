package com.farthestgate.android.ui.admin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.helper.UnsentPCNService;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.printing.seikoTemplateProcessor;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.pcn.NFCBluetoothActivityBase;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.google.android.gms.measurement.module.Analytics;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;

import org.joda.time.DateTime;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;


/**
 * Created by Hanson Aboagye 04/2014
 *
 *
 */
public class StartDayActivity extends NFCBluetoothActivityBase {

    public static final String  TAG = StartDayActivity.class.getSimpleName();
    private ImageButton btnTestPrint;
    private CheckBox checkConfirm;
    private ImageButton btnContinue;

    private NfcAdapter      mNfcAdapter;
    private BackTimerTask   backDelayTask = new BackTimerTask();
    private Handler         backHandler;
    private PCN             pcnInfo;
    private PrinterManager  mPrinterManager;

    private TextView        lblCEO;
    private TextView        lblDate;
    private TextView        lblTime;
    private TextView        lblPrinter;
    private String          noticeDate;
    private String          noticeTime;

    private Boolean         backPressed = false;
    private SharedPreferenceHelper  sharedPreferenceHelper;
    private SimpleDateFormat        sdf;
    private ProgressDialog          progressDialog;
    private TextView                lblAvailablePCNs;
    private TextView                lblAvailablePCNsCap;

    private int availablePCNs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_start_day);

        backHandler = new Handler();
        sharedPreferenceHelper = new SharedPreferenceHelper(this);
        mPrinterManager = CeoApplication.getPrinterManager();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        btnContinue = (ImageButton) findViewById(R.id.btnContinueDay);
        checkConfirm = (CheckBox) findViewById(R.id.checkConfirm);
        btnTestPrint = (ImageButton) findViewById(R.id.buttonPrintTest);
        lblCEO = (TextView) findViewById(R.id.lblCEONumber);
        lblTime = (TextView) findViewById(R.id.lblTime);
        lblDate = (TextView) findViewById(R.id.lblCurrentDate);
        lblPrinter = (TextView) findViewById(R.id.lblPrinterDet);
        lblAvailablePCNs = (TextView) findViewById(R.id.lblAvailablePCNs);
        lblAvailablePCNsCap = (TextView) findViewById(R.id.lblAvailablePCNsCap);

        btnTestPrint.setEnabled(false);
        btnContinue.setEnabled(false);

        pcnInfo = new PCN();

        pcnInfo.pcnNumber = "TEST PRINT PCN";

        sdf = new SimpleDateFormat("dd/MM/yyyy");
        noticeDate = sdf.format(new Date());
        sdf = new SimpleDateFormat("HH:mm");
        noticeTime = sdf.format(new Date());
        Intent data = getIntent();

        lblCEO.setText(data.getStringExtra("CEO"));
        lblDate.setText(noticeDate);
        lblTime.setText(noticeTime);
        availablePCNs = DBHelper.getAvailableTicketNumbers();
        lblAvailablePCNs.setText(String.valueOf(availablePCNs));
        if(availablePCNs < CeoApplication.AvailablePCNToAlert()){
            lblAvailablePCNsCap.setTextColor(Color.RED);
            lblAvailablePCNs.setTextColor(Color.RED);
        }

        btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LogLocationForTestPrint();
                    new PrintingAsyncTask().execute(null, null, null);
                } catch (Exception e) {
                    CroutonUtils.error(StartDayActivity.this, "Error printing - 316 \n restart the Android device");
                }
            }
        });

        checkConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 btnContinue.setEnabled(true);
                /*if (mPrinterManager.isConnect()) {
                    btnTestPrint.setEnabled(isChecked);
                    btnContinue.setEnabled(true);
                } else {
                    buttonView.setChecked(false);
                    CroutonUtils.info(StartDayActivity.this, "Please connect to a printer").show();
                }*/
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackStartOfDay();
                if (mPrinterManager.isConnect())
                    try {
                        mPrinterManager.disconnect();
                    } catch (PrinterException e) {
                        e.printStackTrace();
                    }
                scheduleUnsentPCNService();
                scheduleUnsentPhotosService();
                startActivity(new Intent(StartDayActivity.this, VisualPCNListActivity.class));
                finish();
            }
        });

    }

    private BroadcastReceiver registerNoAvailablePCNBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(AppConstant.NO_AVAILABLE_PCN_BR)) {
                CroutonUtils.errorMsgInfinite(StartDayActivity.this, "No PCN numbers available on this device. Please sync the device and try again.");
                checkConfirm.setEnabled(false);
            }
        }
    };

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(registerNoAvailablePCNBR,
                new IntentFilter(AppConstant.NO_AVAILABLE_PCN_BR));

        super.onResume();
        if(availablePCNs == 0) {
            Intent intent = new Intent(AppConstant.NO_AVAILABLE_PCN_BR);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registerNoAvailablePCNBR);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_day_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.mnuPrinterstart)
            try {
                if (mPrinterManager.isConnect()){
                    mPrinterManager.disconnect();

                }
                CeoApplication.changePrinter();
                mPrinterManager = CeoApplication.getPrinterManager();
                lblPrinter.setText("");
                /*else{

                   mPrinterManager.resetPrinter();
                }*/

                //CroutonUtils.info(this, "Tap on a new printer to connect");
                CroutonUtils.info(this, "Tap your device on printer first and try again");
            } catch (PrinterException e)
            {
                CroutonUtils.error(this, e.getMessage() + "\nTurn printer off for 5 seconds and turn back on");
            }
        return false;
    }

    @Override
    public void startConnection() {
        super.startConnection();
        lblPrinter.setText(CeoApplication.printerBluetoothAddress);
    }

    public void savePicture() {
        try {
            String xmlFile = Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "SeikoPrintFormatTest.xml";
            byte[] retVal = null;
            try {
                seikoTemplateProcessor stp = new seikoTemplateProcessor();
                stp.templatePath = xmlFile;
                stp.pageWidth = CeoApplication.getPageWidth();//40;
                stp.setTestData = true;
                retVal = stp.returnTemplate();
            } catch (Exception ex) {
                CroutonUtils.error(StartDayActivity.this,ex.getMessage());
            }
            try {
                mPrinterManager.sendBinary(retVal);
                //FF Test
                //mPrinterManager.sendBinary(new byte[] {(byte) (char) 12});
                //MP FF Test
                //mPrinterManager.sendBinary(new byte[] {(byte) (char) 29, (byte) "<".charAt(0)});
            } catch(PrinterException e) {
                CroutonUtils.error(StartDayActivity.this, e.getMessage() + ":" + e.getErrorCode());
            }
            catch (Exception e){
                CroutonUtils.error(StartDayActivity.this,e.getMessage());
                e.printStackTrace();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void LogLocationForTestPrint() {
        LocationLogTable logEntry = new LocationLogTable();
        logEntry.setLogTime(new DateTime());
        logEntry.setCeoName(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
        logEntry.setTestPCNPrintedTime(new Date().getTime());
        logEntry.save();
    }

    @Override
    public void onBackPressed() {
        if (!backPressed) {
            CroutonUtils.info(this,"Press Back again to exit");
            backPressed = true;
            backHandler.postDelayed(backDelayTask, 5000);
        }
        else {
            if (mPrinterManager.isConnect())
                try {
                    mPrinterManager.disconnect();
                } catch (PrinterException e) {
                    e.printStackTrace();
                }
            super.onBackPressed();
        }
    }

    private class BackTimerTask extends TimerTask {
        @Override
        public void run() {
            if (backPressed) {
                backPressed = false;
            }
        }
    }

    private class PrintingAsyncTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(StartDayActivity.this);
            progressDialog.setMessage("Printing, please wait ...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Printing");
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            savePicture();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            sharedPreferenceHelper.saveValue(SharedPreferenceHelper.START_OF_DAY, true);
            progressDialog.hide();
        }
    }

    public void scheduleUnsentPCNService() {
        Intent intent = new Intent(getApplicationContext(), UnsentPCNService.class);
        final PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
        long firstMillis = new Date().getTime();
        long unsentPcnTimeInterval = Integer.parseInt(CeoApplication.getUnsentPcnTimeInterval()) * 60 * 1000;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, unsentPcnTimeInterval, pIntent);
    }


    public void scheduleUnsentPhotosService() {
        Intent intent = new Intent(getApplicationContext(), RemovalPhotoService.class);
        final PendingIntent pIntent = PendingIntent.getService(this, 1, intent, 0);
        long firstMillis = new Date().getTime();
        long unsentPcnTimeInterval = Integer.parseInt(CeoApplication.getUnsentPcnTimeInterval()) * 60 * 1000;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 1000, pIntent);
    }

}