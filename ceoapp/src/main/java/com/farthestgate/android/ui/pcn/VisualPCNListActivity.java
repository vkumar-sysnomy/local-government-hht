package com.farthestgate.android.ui.pcn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;


/*
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
*/

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.farthestgate.android.BuildConfig;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;

import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.helper.LocationHelperFused;
import com.farthestgate.android.helper.VRMAutomatedLookupTask;
import com.farthestgate.android.helper.fused.BackgroundService;
import com.farthestgate.android.helper.fused.MyService;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.model.StreetCPZ;
import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.admin.EndOfDayActivity;
import com.farthestgate.android.ui.admin.LoginActivity;
import com.farthestgate.android.ui.dialogs.LogLocationDialog;
import com.farthestgate.android.ui.dialogs.VRMAutomatedLookupDialog;
import com.farthestgate.android.ui.notes.NotesListActivity;
import com.farthestgate.android.ui.notes.TextNoteActivity;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.farthestgate.android.utils.Log;
import com.farthestgate.android.utils.LogTimerTask;
import com.imense.anpr.launchPT.AnprVrmDialog;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class VisualPCNListActivity extends NFCBluetoothActivityBase
        implements ObservationListFragment.OnEmptyListListener,
        LogLocationDialog.OnNewLocationLoggedListener/*, LocationHelper.OnLocationChangedListener*/, VRMAutomatedLookupTask.VRMAutomatedLookupListener, AnprVrmDialog.OnAnprVrmListener, ObservationListFragment.OnVRMRead, VRMAutomatedLookupDialog.OnVRMLookupListener
{

    public static Timer lTimer;
    public static Boolean firstLogin = false;
    public static Integer currentActivity;
    public static StreetCPZ currentStreet;
    public static double latitude;
    public static double longitude;
    public static Activity activity;
    private final static int REQUEST_PERMISSIONS_REQUEST_CODE=200;
    private LocationReceiver locationReceiver;
    private JSONObject address=new JSONObject();
    public BackgroundService gpsService;
    boolean mBound = false;
    public static Boolean locationUpdate = false;
    ArrayList<PaidParking> paidParkings;


    /***Changes for imense ANPR integration:Start***/
    //public String licenseKey = null;
//    public String vrmText;

    /***Changes for imense ANPR integration:End***/

    private LogTimerTask mTimerTask = new LogTimerTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_visual_pcnlist);
        currentActivity = 0;
        lTimer = new Timer();
        activity=this;
        locationReceiver = new LocationReceiver();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content, new ObservationListFragment()).commit();

        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            startJob();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }

        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        this.getApplication().startService(intent);
//        this.getApplication().startForegroundService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        /*LocationHelper locationHelper = new LocationHelper(this);
        if(locationHelper.canGetLocation()){
            latitude = locationHelper.getLatitude();
            longitude = locationHelper.getLongitude();
        }else{
            locationHelper.showSettingsAlert();
        }*/

        if (currentStreet == null)
            lTimer.schedule(mTimerTask,10l);

        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter(MyService.ACTION_BROADCAST));
    }

    private void startJob(){

        if(!isMyServiceRunning(LocationHelperFused.class)){
            Intent intent=new Intent(this,LocationHelperFused.class);
            startService(intent);
        }
        StartBackgroundTask();
    }

    @SuppressLint("NewApi")
    public void StartBackgroundTask() {
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(getApplicationContext(), MyService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setMinimumLatency(1000) //1 sec interval
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build();
        int result=jobScheduler.schedule(jobInfo);

        if (result == JobScheduler.RESULT_SUCCESS)
        {
            Log.i("Success");
        }
        else
        {
            Log.i("Failed");
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        currentActivity = 0;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Intent intent=new Intent(this,LocationHelperFused.class);
        stopService(intent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);*/
    }


    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View v = super.onCreateView(name, context, attrs);
        return v;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.mnuTxtNotes)
        {
            startActivity(new Intent(this, TextNoteActivity.class));
        }
        if (item.getItemId() == R.id.mnuDrwNotes)
        {
            startActivity(new Intent(this, NotesListActivity.class));
        }
        if (item.getItemId() == R.id.mnuEndDay)
        {
            AnalyticsUtils.trackMenuEndOfDay();

            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setTitle("End of Day");
            ad.setMessage("Are you sure you want to end your session and log out ?");
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "End Session", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LogLocationForEOD();
                    startActivity(new Intent(getApplicationContext(), EndOfDayActivity.class));
                    Runtime.getRuntime().gc();
                    Runtime.getRuntime().freeMemory();
                    finish();
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE , "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ad.setCancelable(false);
            ad.show();
        }
        if (item.getItemId() == R.id.mnuPrinter) {
            AnalyticsUtils.trackMenuPrinter();

            try
            {
               /* if (mPrinterManager.isConnect())
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
        if (item.getItemId() == R.id.mnuBreak)
        {
            AnalyticsUtils.trackMenuBreak();
            // startActivityForResult(new Intent(this, BreakActivity.class), 75);
            Intent intent = new Intent(this, BreakActivity.class);
            intent.putExtra("breakType", "BREAK");
            startActivityForResult(intent,75);
        }
        if (item.getItemId() == R.id.mnuNotesOut)
        {
            AnalyticsUtils.trackMenuNotesList();
            Intent intent = new Intent(this, NotesListActivity.class);
            intent.putExtra("obs", 0);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.mnuInTransit)
        {
            AnalyticsUtils.trackMenuInTransit();
            Intent intent = new Intent(this, BreakActivity.class);
            intent.putExtra("breakType", "TRANSIT");
            startActivityForResult(intent,75);
        }
        return false;
    }


    @Override
    public void onEmptyList()
    {

    }

    @Override
    public void onListChanged()
    {

    }

    @Override
    public void onEndOfDay() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        currentActivity = 0;
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == CeoApplication.RESULT_CODE_ENDOFDAY) {
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

        switch (resultCode)
        {
            case CeoApplication.RESULT_CODE_ENDOFDAY:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;

            case CeoApplication.RESULT_CODE_MESSAGEVIEW_ANPR:
                    showVRMAutomatedLookupDialog(paidParkings);
                break;
            default:
            {
                //Dialog was cancelled - continue
                break;
            }

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU)
        {
            openOptionsMenu();
        }
        else
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            CroutonUtils.info(this, "Please click End of Day to exit");
        }
        else
            return super.onKeyDown(keyCode, event);

        return false;
    }

    @Override
    public void onLocationChanged(String newStreet)
    {
        firstLogin = true;
    }

    @Override
    public void onLocationUnchanged()
    {
        firstLogin = true;
    }

   /* @Override
    public void onCurrentLocationChanged(Location newLocation) {
        if (newLocation != null) {
            latitude = newLocation.getLatitude();
            longitude = newLocation.getLongitude();
            LogLocationForGPS();
        }
    }*/

    private class LocationReceiver extends BroadcastReceiver {
        int frequency=-1;
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                Location location = intent.getParcelableExtra(MyService.EXTRA_LOCATION);
                if (location != null) {
                    frequency=frequency+1;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Geocoder geocoder = new Geocoder(VisualPCNListActivity.this, Locale.getDefault());
                    List<Address> addresses= null;
                    if(CeoApplication.useGeocoder()&&CeoApplication.frequency()==frequency){
                        frequency=-1;
                        addresses= geocoder.getFromLocation(latitude, longitude, 1);
                        address.put("city",addresses.get(0).getLocality());
                        address.put("state",addresses.get(0).getAdminArea());
                        address.put("country",addresses.get(0).getCountryName());
                        address.put("postalCode",addresses.get(0).getPostalCode());
                        address.put("knownName",addresses.get(0).getFeatureName());
                        sharedPreferenceHelper.saveString(AppConstant.CURRENT_ADDRESS,address.toString());
                    }

                    LogLocationForGPS();
                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    private void LogLocationForGPS() {
        if(currentStreet != null) {
            String streetName = currentStreet != null ? currentStreet.streetname : "";
            LocationLogTable logEntry = new LocationLogTable();
            logEntry.setLogTime(new DateTime());
            logEntry.setCeoName(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
            logEntry.setStreetName(streetName);
            logEntry.setLongitude(longitude);
            logEntry.setLattitude(latitude);
            logEntry.save();

            publishCeoTracking(streetName);
        }
    }

    private void LogLocationForEOD() {
        if(currentStreet != null) {
            String streetName = currentStreet != null ? currentStreet.streetname : "";
            LocationLogTable logEntry = new LocationLogTable();
            logEntry.setLogTime(new DateTime());
            logEntry.setCeoName(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
            logEntry.setStreetName(streetName);
            logEntry.setLongitude(longitude);
            logEntry.setLattitude(latitude);
            logEntry.setEndOfDayTime(new Date().getTime());
            logEntry.save();
        }
    }

    public void publishCeoTracking(String streetName){
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject ceoObject = new JSONObject();
            JSONObject latLong = new JSONObject();
            latLong.put("lat", latitude);
            latLong.put("long", longitude);
            JSONObject data = new JSONObject();
            data.put("ceoshouldernumber", DBHelper.getCeoUserId());
            data.put("currentstreet", streetName == null ? "" : streetName);
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
            ceoObject.put("locationaddress",sharedPreferenceHelper.getString(AppConstant.CURRENT_ADDRESS,""));


            jsonObject.put(DBHelper.getCeoUserId(), ceoObject);

            PubNubModule.publishCeoTracking(jsonObject);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /***Changes for imense ANPR integration:Start***/
    private void openAnprVrmDialog(String vrm){
        AnprVrmDialog anprVrmDialog = AnprVrmDialog.newInstance(vrm);
        anprVrmDialog.setCancelable(false);
        anprVrmDialog.show(getFragmentManager(), "");
    }

    @Override
    public void checkAutomatedVRMLookup(String vrm){
//        this.vrmText = vrm;
        if (DeviceUtils.isConnected(this)) {
            VRMAutomatedLookupTask vrmAutomatedLookupTask = new VRMAutomatedLookupTask(this, vrm, this);
            vrmAutomatedLookupTask.execute();
        } else{
            CroutonUtils.errorMsgInfinite(this, "No internet connectivity available at the time of search");
            openAnprVrmDialog(vrm);
        }
    }


    @Override
    public void vrmLookupPaidParking(ArrayList<PaidParking> paidParkings, String vrmText, boolean isError) {
        if((paidParkings != null && paidParkings.size() == 0) || isError){
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(800);
            LogLocationForANPR(vrmText,"No Sessions Found");
            openAnprVrmDialog(vrmText);
        } else{
            paidParkings = paidParkings;
            LogLocationForANPR(vrmText, "Permits/Cashless Session Found");
            //Utils.showDialog(this, "Valid parking session found", "Parking Session");
            if(CeoApplication.getVrmEntryLookUp()){
                Intent infoIntent = new Intent(VisualPCNListActivity.this, MessageViewActivity.class);
                //ArrayList<PaidParking> paidParkings = new ArrayList<PaidParking>();
                //paidParkings.add(paidParkings);
                int sync = DataHolder.get().setListData(paidParkings);
                infoIntent.putExtra("paidParking:synccode", sync);
                //infoIntent.putExtra("paidParking", paidParkings);
                infoIntent.putExtra("VRM", vrmText);
                infoIntent.putExtra("paidParkingMsg", true);
                startActivityForResult(infoIntent, CeoApplication.REQUEST_CODE_MESSAGEVIEW);

            }else
            {
                showVRMAutomatedLookupDialog(paidParkings);
            }
           //
        }
    }

    private void showVRMAutomatedLookupDialog(ArrayList<PaidParking> paidParkings) {
        FragmentManager fm = getSupportFragmentManager();
        VRMAutomatedLookupDialog dialog = VRMAutomatedLookupDialog.newInstance(paidParkings, true);
        dialog.setCancelable(false);
        dialog.show(fm, "");
    }

    @Override
    public void onAnprClicked(String vrmText) {
//        this.vrmText = vrmText;
        checkAutomatedVRMLookup(vrmText);
    }

    private void LogLocationForANPR(String vrmText, String vrmLookupResponse) {
        LocationLogTable logEntry = new LocationLogTable();
        logEntry.setCeoName(DBHelper.getCeoUserId());
        logEntry.setLattitude(latitude);
        logEntry.setLongitude(longitude);
        logEntry.setStreetName(currentStreet.streetname);
        logEntry.setLogTime(new DateTime());
        logEntry.setAnprRead(vrmText);
        logEntry.setAnprReadTime(new Date().getTime());
        logEntry.setVrmLookupResponse(vrmLookupResponse);
        logEntry.save();
    }

    //Implemented for only ANPR automated lookup..No need to do anything here
    @Override
    public void OnVRMLookupConfirmed(boolean isConfirmed, String data) {

    }

    /***Changes for imense ANPR integration:End***/

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
                gpsService.startTracking();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                gpsService = null;
            }
        }
    };
}
