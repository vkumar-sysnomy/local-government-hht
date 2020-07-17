package com.farthestgate.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.model.Ceo;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PCNJsonData;
import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.model.database.ErrorTable;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.model.database.SingleViewLookUps;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.utils.FirebaseAnalyticsController;
import com.farthestgate.android.utils.StringUtil;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
//import com.pubnub.api.Pubnub;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aaronnewton on 07/02/2014.
 */
public class CeoApplication extends com.activeandroid.app.Application {
    private static final String TAG = CeoApplication.class.getSimpleName();
    private static Context context;
    public static final String PREF_CONFIG_DATA_LOADED = "pref_config_data_loaded";
    public static final int RESULT_CODE_PCNLIST = 49;
    public static final int RESULT_CODE_PCNLOGGING = 99;
    public static final int RESULT_CODE_ERROR = 9999;
    public static final int RESULT_CODE_ENDOFDAY = 11;
    public static final int RESULT_CODE_NEWIMAGE = 50;
    public static final int RESULT_CODE_NOTES = 30;
    public static final int REQUEST_CODE_START_OBS = 20;
    public static final int REQUEST_CODE_TIMED_OBS = 41;
    public static final int REQUEST_CODE_INSTANT = 40;
    public static final int RESULT_CODE_OBS_CONTINUE = 81;
    public static final int RESULT_CODE_OBS_CANCEL = 80;
    public static final int RESULT_CODE_VEHICLE_DIALOG = 45;
    public static final int RESULT_CODE_VEHICLE_DIALOG_CANCEL = 46;
    public static final int REQUEST_CODE_PERMIT_TO_PCN =55;
    public static final int PHOTO_RESAMPLE_WIDTH = 960;
    public static final int PHOTO_RESAMPLE_HEIGHT = 576;
    public static final int PHOTO_THUMB_SIZE = 400;
    public static final int REQUEST_CODE_ADD_NOTE = 10;
    public static final int REQUEST_CODE_MESSAGEVIEW = 59;
    public static final int RESULT_CODE_MESSAGEVIEW_ANPR = 58;
    public static boolean FLASH_MODE=false;
    public static long OBS_START_TIME;
    //TODO: Fix this to read file header
    public static String pcnFileHeader = "{\"nodeRef\":\"workspace://SpacesStore/67058fac-71c5-474c-98a1-7527fc5bc61d\",\"ticketNumbers\":[";
    public static Handler logHandler;
    public static Ceo CEOLoggedIn;
    //public static Date ceoLoginTime;
    //public static String ceoRole;
    private static PrinterManager printerManager;
    public static String printerBluetoothAddress;
    public static boolean configurationMissing;
    public static boolean configFileMissing;
    public static String removalPhotoPublishChannel;
    public static String removalPhotoSubscribeChannel;
    public static String ceoTrackingChannel;
    public static String codeRedChannel;
    private static PubNubModule pubNubModule;
    private static PubNub pubnub;
    public static boolean isVrmLook = false;
    public static boolean isTimeOutException = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        FirebaseAnalyticsController.getInstance().initialize(this);
        printerManager = new PrinterManager();
        logHandler = new Handler();
        //GetConfigDetails();
        //pubNubModule = new PubNubModule(context, "RemovalPhoto");
        //pubNubModule.subscribeRemovalPhotos();
        handleGlobalException();
    }

    private void handleGlobalException(){
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                try {
                    CeoApplication.LogError(paramThrowable);
                   //  android.os.Process.killProcess(android.os.Process.myPid());
                    //System.exit(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
        pubNubModule.unSubscribeRemovalPhotoChannel();
    }

    public static void LogoutProcess() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        String CEO = DBHelper.getCeoUserId();
        int n = 0;
        JSONObject toursAndBreaks = new JSONObject();
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        File tourJson = cameraImageHelper.getFile(AppConstant.TOUR_REPORT_FOLDER, "Tour_Report_" + CEO + "_" + sdf.format(new DateTime().toDate()) + "-" + n + ".json");
        String tourFileName = tourJson.getAbsolutePath();
        while (tourJson.exists()) {
            String target = "-" + String.valueOf(n) + ".";
            String newVal = "-" + String.valueOf(n + 1) + ".";
            tourFileName = tourFileName.replace(target, newVal);
            tourJson = new File(tourFileName);
            n++;
        }
        JSONArray tempArray = new JSONArray();
        for (LocationLogTable logTableRow : DBHelper.getTour(CEO)) {
            try {
                JSONObject temp = new JSONObject(logTableRow.toJSON());
                tempArray.put(temp);
            } catch (JSONException e1) {
                e1.printStackTrace();
                try {
                    LogError(e1);
                    LogErrorOnChannel(getContext(), e1, ErrorLocations.location402);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            n++;
        }
        try {
            toursAndBreaks.put("tours", tempArray);
            tempArray = new JSONArray();
            JSONArray transitArray = new  JSONArray();
            List<BreakTable> breakDetails = DBHelper.getBreakDetails(CEO, 0);
            for (BreakTable breakTable : breakDetails) {
                try {
                    JSONObject objBreak = new JSONObject(breakTable.getJSON());
                    if(breakTable.getBreakType().equalsIgnoreCase("BREAK")){
                        tempArray.put(objBreak);
                    }else{
                        transitArray.put(objBreak);
                    }
                    breakTable.setExtracted(1);
                    breakTable.save();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    try {
                        LogError(e1);
                        LogErrorOnChannel(getContext(), e1, ErrorLocations.location402);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            toursAndBreaks.put("breaks", tempArray);
            toursAndBreaks.put("intransit", transitArray);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                LogError(ex);
                LogErrorOnChannel(getContext(), ex, ErrorLocations.location402);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            OutputStream os = new FileOutputStream(tourJson, true);
            String tourData = toursAndBreaks.toString();
            os.write(tourData.getBytes());
            os.close();
            DBHelper.ClearTourTable(CEO);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            try {
                LogError(e);
                LogErrorOnChannel(getContext(), e, ErrorLocations.location402);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                LogError(e);
                LogErrorOnChannel(getContext(), e, ErrorLocations.location402);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                LogError(ex);
                LogErrorOnChannel(getContext(), ex, ErrorLocations.location402);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
        try {
            File ceoBreakJson = cameraImageHelper.getFile(AppConstant.CEO_BREAK_FOLDER, CEO + "_" + sdf.format(new DateTime().toDate()) + "_BREAKS.json");
            tempArray = new JSONArray();
            List<BreakTable> breakDetails = DBHelper.getBreakDetails(CEO,0);
            for (BreakTable breakTable : breakDetails) {
                try {
                    JSONObject objBreak = new JSONObject(breakTable.getJSON());
                    tempArray.put(objBreak);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            if(breakDetails.size()>0) {
                OutputStream breakStream = new FileOutputStream(ceoBreakJson, true);
                String breakData = tempArray.toString();
                breakStream.write(breakData.getBytes());
                breakStream.close();

                DBHelper.ClearBreakDetails(CEO, 0);
                for (BreakTable breakTable : breakDetails) {
                    breakTable.setExtracted(1);
                    breakTable.save();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        */
        /*SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getContext());
        Integer currentSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;*/
        List<String> pcnExtracted = new ArrayList<String>();
        for (PCNTable pcn : DBHelper.GetPCNs(0, true)) {
            String pcnJSON = pcn.getPcnJSON();
            Gson gson = new GsonBuilder().create();
            PCN pcnInfo = gson.fromJson(pcnJSON, PCN.class);
            long pcnPrintTime = DBHelper.getPCNPrintTime(pcnInfo.pcnNumber);
            if(pcnInfo.issueTime == 0 && pcnPrintTime > 0){
                 //continue;
                //commented above continue and get pcn printed time and assigned to pcn issuedtime to continue issue missed pcn, datetime 1970 and locationstreetname empty
                pcnInfo.issueTime = pcnPrintTime;
                pcn.setPcnJSON(pcnInfo.toJSON());
                PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                pcn.setPcnOUTJSON(outPCN.toJSON());
                pcn.save();

            } else if(pcnInfo.issueTime == 0 && pcnPrintTime == 0){
                continue;
            }
            pcnExtracted.add(pcn.getPcnNumber());
            File pcnJson = cameraImageHelper.getFile(AppConstant.PCN_DATA_FOLDER, pcn.getPcnNumber() + "-data.json");
            if (!pcnJson.exists()) {
                try {
                    OutputStream os = new FileOutputStream(pcnJson, true);
                    Integer num = getPCNPhotoCount(pcn.getObservation());
                    JSONObject tempData;
                    if (pcn.getPcnOUTJSON() == null || pcn.getPcnOUTJSON().length() == 0) {
                        PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                        tempData = new JSONObject(outPCN.toJSON());
                        pcn.setPcnOUTJSON(tempData.toString());
                        pcn.save();
                    }else{
                        tempData = new JSONObject(pcn.getPcnOUTJSON());
                    }
                    tempData.putOpt("numberofphotostaken", num);
                    tempData.putOpt("hhtid", CeoApplication.getUUID());
                    String pcnJ = tempData.toString();
                    os.write(pcnJ.getBytes());
                    os.close();
                   /* if (pcn.getPcnOUTJSON() != null && pcn.getPcnOUTJSON().length() != 0) {
                        OutputStream os = new FileOutputStream(pcnJson, true);
                        JSONObject tempData = new JSONObject(pcn.getPcnOUTJSON());
                        Integer num = getPCNPhotoCount(pcn.getObservation());
                        tempData.putOpt("numberofphotostaken", num);
                        tempData.putOpt("hhtid", telephonyManager.getDeviceId());
                        String pcnJ = tempData.toString();
                        os.write(pcnJ.getBytes());
                        os.close();
                    } else {
                        PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                        JSONObject tempJsonData = new JSONObject(outPCN.toJSON());
                        pcn.setPcnOUTJSON(tempJsonData.toString());
                        pcn.save();
                        OutputStream os = new FileOutputStream(pcnJson, true);
                        Integer num = getPCNPhotoCount(pcn.getObservation());
                        tempJsonData.putOpt("numberofphotostaken", num);
                        tempJsonData.putOpt("hhtid", telephonyManager.getDeviceId());
                        String pcnJ = tempJsonData.toString();
                        os.write(pcnJ.getBytes());
                        os.close();
                    }*/

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    try {
                        LogError(e);
                        LogErrorOnChannel(getContext(), e, ErrorLocations.location402);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        LogError(e);
                        LogErrorOnChannel(getContext(), e, ErrorLocations.location402);
                    } catch (Exception e1) {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        LogError(ex);
                        LogErrorOnChannel(getContext(), ex, ErrorLocations.location402);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            boolean fileContentOK = false;
            File pcnJsonCheck = new File(pcnJson.getAbsolutePath());
            if (pcnJsonCheck.exists()) {
                CheckZeroBytePCNFile(pcnJsonCheck,pcn.getPcnNumber(), CeoApplication.getUUID());
                File jsonDataFileCheck = new File(pcnJsonCheck.getAbsolutePath());
                if (jsonDataFileCheck.length() != 0) {
                    fileContentOK = true;
                }
            } else {
                try {
                    if(pcnJsonCheck.createNewFile()){
                        CheckZeroBytePCNFile(pcnJsonCheck,pcn.getPcnNumber(), CeoApplication.getUUID());
                        File jsonDataFileCheck = new File(pcnJsonCheck.getAbsolutePath());
                        if (jsonDataFileCheck.length() != 0) {
                            fileContentOK = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            UpdateSyncFileStatus(fileContentOK, pcn.getPcnNumber());
        }
        for(String pcnNumber : pcnExtracted){
            checkNoPCNDataFile(pcnNumber);
        }

        JSONObject lookUpObj = new JSONObject();
        JSONArray lookUpArray = new JSONArray();
        for (SingleViewLookUps vrmTableRow : DBHelper.getSingleViewLookUps()) {
            try {
                JSONObject temp = new JSONObject(vrmTableRow.toJSON());
                lookUpArray.put(temp);
            } catch (JSONException e1) {
                e1.printStackTrace();
                try {
                    LogError(e1);
                    LogErrorOnChannel(getContext(), e1, ErrorLocations.singleVRMLooksUp501);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            lookUpObj.put("lookups", lookUpArray);
            int m = 0;
            //CameraImageHelper cameraImageHelper = new CameraImageHelper();
            //SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            File lookupJson = new File(cameraImageHelper.getSingleLookUpsFolder() + File.separator + sdf.format(new DateTime().toDate()) + "-" + m +"-"+ "lookUpsData.json");
            String lookUpFileName = lookupJson.getAbsolutePath();
            while (lookupJson.exists()) {
                String target = "-" + String.valueOf(m) + ".";
                String newVal = "-" + String.valueOf(m + 1) + ".";
                lookUpFileName = lookUpFileName.replace(target, newVal);
                lookupJson = new File(lookUpFileName);
                m++;
            }
            FileOutputStream os = new FileOutputStream(lookupJson, true);
            os.write(String.valueOf(lookUpObj).getBytes());
            os.close();
            DBHelper.clearSingleViewLookUps();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static int getPCNPhotoCount(Integer observation){
        Integer pcnPhotoCount = 0;
        try {
            //Deleting zero byte image file from photo folder - HAN-32
            List<PCNPhotoTable> pcnPhotoTables = DBHelper.PhotosForPCN(observation);
            pcnPhotoCount = pcnPhotoTables.size();
            for(PCNPhotoTable pcnPhotoTable: pcnPhotoTables){
                File pcnPhotoFile = new File(pcnPhotoTable.getFileName());
                if(pcnPhotoFile.exists() && pcnPhotoFile.length()==0) {
                    pcnPhotoFile.delete();
                    pcnPhotoTable.delete();
                    pcnPhotoCount -= 1;
                    LogError("PCN photo file:" + pcnPhotoFile.getName() + " is a zero byte file and hence deleted." );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                LogError(ex.getMessage());
                LogErrorOnChannel(getContext(), ex, ErrorLocations.location402);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pcnPhotoCount;
    }

    private static void UpdateSyncFileStatus(boolean syncFile, String pcnNumber) {
        PCNTable finalPcn;
        List<PCNTable> PCNs = DBHelper.GetPCN(pcnNumber);
        if (PCNs.size() > 0) {
            finalPcn = PCNs.get(0);
            finalPcn.setSyncFiles(syncFile);
            finalPcn.setDataExtracted(1);
            finalPcn.save();
        }
    }
    private static void CheckZeroBytePCNFile(File jsonDataFile, String pcnNumber, String deviceId){
        try {
            if (jsonDataFile.length() == 0) {
                List<PCNTable> pcnTables = DBHelper.GetPCN(pcnNumber);
                if (pcnTables.size() > 0) {
                    PCNTable pcnTable = pcnTables.get(0);
                    String pcnJSON = pcnTable.getPcnJSON();
                    Gson gson = new GsonBuilder().create();
                    PCN pcnInfo = gson.fromJson(pcnJSON, PCN.class);
                    /*if(pcnInfo.issueTime==0){
                        return;
                    }*/
                    long pcnPrintTime = DBHelper.getPCNPrintTime(pcnInfo.pcnNumber);
                    if(pcnInfo.issueTime == 0 && pcnPrintTime > 0){
                        //commented above continue and get pcn printed time and assigned to pcn issuedtime to continue issue missed pcn, datetime 1970 and locationstreetname empty
                        pcnInfo.issueTime = pcnPrintTime;
                        pcnTable.setPcnJSON(pcnInfo.toJSON());
                        PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                        pcnTable.setPcnOUTJSON(outPCN.toJSON());
                        pcnTable.save();
                    } else if(pcnInfo.issueTime == 0 && pcnPrintTime == 0){
                        return;
                    }

                    String jsonOUTString = pcnTable.getPcnOUTJSON();
                    JSONObject tempData = null;
                    if(jsonOUTString != null) {
                        tempData = new JSONObject(jsonOUTString);
                    }
                    if(tempData==null || tempData.toString().length()==0){
                        PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                        tempData = new JSONObject(outPCN.toJSON());
                        pcnTable.setPcnOUTJSON(tempData.toString());
                        pcnTable.save();
                    }
                    int num = getPCNPhotoCount(pcnTable.getObservation());
                    tempData.putOpt("numberofphotostaken", num);
                    tempData.putOpt("hhtid", deviceId);
                    String pcnJsonContent = tempData.toString();
                    OutputStream os = new FileOutputStream(jsonDataFile, true);
                    os.write(pcnJsonContent.getBytes());
                    os.close();
                }
            }
        }catch (Exception ix) {
            ix.printStackTrace();
            try {
                jsonDataFile.delete();
                logJSONError(false, true, false, pcnNumber);
                LogError(ix);
                LogErrorOnChannel(context,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Context getContext() {
        return context;
    }

    public static String getUUID() {
        if (Build.VERSION.SDK_INT <= 28) {
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tManager.getDeviceId();
        }else
        {
            return getImeiFIle();
        }

    }

    public static String getImeiFIle(){
        String imeiNumber = null;
        String rootDir = Environment.getExternalStorageDirectory().toString();
        File location = new File(rootDir + File.separator);
        for (File f : location.listFiles()) {
            if (f.getName().endsWith(".imei")) {
                imeiNumber = f.getName().replace(".imei","");
                break;
            }
        }
        return imeiNumber;
    }

    public static PrinterManager getPrinterManager() {
        return printerManager;
    }

    public static PrinterManager changePrinter() {
        return printerManager=new PrinterManager();
    }
    private static String pubKey;
    public static String publishKey() {
        return pubKey;
    }
    private static String subsKey;
    public static String subscribeKey() {
        return subsKey;
    }
    private static String secKey;
    public static String secretKey() {
        return secKey;
    }
    private static boolean ssl;
    public static boolean isSSL() {
        return ssl;
    }
    private static String channel;
    public static String getChannel() {
        if(channel ==null || channel.length()==0)GetConfigDetails();
        return channel;
    }
    private static String errorChannel;
    public static String ErrorChannel() {
        if(errorChannel ==null || errorChannel.length()==0)GetConfigDetails();
        return errorChannel;
    }
    private static Boolean usePubNub = null;
    public static boolean UsePubNub() {
        if(usePubNub ==null)GetConfigDetails();
        return usePubNub;
    }
    private static int pcnReloadHours=0;
    public static int PcnReloadHours() {
        return pcnReloadHours;
    }
    public static PubNub getPubnubInstance() {
        /*if(pubnub == null) {
            if (pubKey == null || pubKey.length() == 0) GetConfigDetails();
            pubnub = new Pubnub(publishKey(), subscribeKey(), secretKey(), isSSL());
            pubnub.setUUID(getUUID());
        }s
        return pubnub;*/

        if(pubnub == null) {
            if (pubKey == null || pubKey.length() == 0) GetConfigDetails();
            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey(subscribeKey());
            pnConfiguration.setPublishKey(publishKey());
            pnConfiguration.getCipherKey();
            pnConfiguration.setSecure(true);
            pnConfiguration.setMaximumReconnectionRetries(3);
            if (secretKey() != null && !secretKey().isEmpty()) {
                pnConfiguration.setSecretKey(secretKey());
            }
            pnConfiguration.setUuid(getUUID());
            return new PubNub(pnConfiguration);
        }
        return pubnub;
    }
    private static String clientLogoText;
    public static String ClientLogoText() {
        return clientLogoText;
    }
    private static int updateGPS=30;
    public static int getUpdateGps() {
        return updateGPS;
    }

    private static int pageWidth=40;
    public static int getPageWidth() {
        return pageWidth;
    }

    private static String transferProtocol="FTP";
    public static String getTransferProtocol() {
        return transferProtocol;
    }



    private static int priorityHighAccuracy=100;
    public static int getPriority() {
        return priorityHighAccuracy;
    }

    private static boolean useGeocoder;
    public static boolean useGeocoder() {
        return useGeocoder;
    }

    private static int frequency=-1;
    public static int frequency() {
        return frequency;
    }

    private static boolean recordObservationVRM=true;
    public static boolean getRecordObservationVRM() {
        return recordObservationVRM;
    }

    private static String recordObservationVRMValue="OBSERVED";
    public static String getRecordObservationVRMValue() {
        return recordObservationVRMValue;
    }

    private static int ftpConnectionTimeout=3000;
    public static int getFtpConnectionTimeout() {
        return ftpConnectionTimeout;
    }
    private static String payByPhoneUserId;
    public static String PayByPhoneUserId() {
        return payByPhoneUserId;
    }
    private static String payByPhoneUserPassword;
    public static String PayByPhoneUserPassword() {
        return payByPhoneUserPassword;
    }

    private static String payByPhoneContraventionCodes;
    public static String PayByPhoneContraventionCodes(){
        return payByPhoneContraventionCodes;
    }
    private static String queryingPayByPhone;
    public static String QueryingPayByPhone(){
        return queryingPayByPhone;
    }
    private static String validParkingUntil;
    public static String ValidParkingUntil(){
        return validParkingUntil;
    }
    private static int queryWaitTime;
    public static int QueryWaitTime(){
        return queryWaitTime;
    }
    private static String pcnPrefix;
    public static String PcnPrefix(){
        return pcnPrefix;
    }
    private static String subscribeChannel;
    public static String SubscribeChannel() {
        return subscribeChannel;
    }
    public static String getRemovalPhotoPublishChannel() {
        return removalPhotoPublishChannel;
    }

    public static String getRemovalPhotoSubscribeChannel() {
        return removalPhotoSubscribeChannel;
    }
    public static String getCeoTrackingChannel() {
        return ceoTrackingChannel;
    }
    public static String getCodeRedChannel() {
        return codeRedChannel;
    }

    private static String syncConfirmationChannel="";
    public static String getSyncConfirmationChannel() {
        return syncConfirmationChannel;
    }

    private static String singleviewTimeout="30000";
    public static String getSingleviewTimeout() {
        return singleviewTimeout;
    }

    private static int availablePCNToAlert;
    public static int AvailablePCNToAlert() {
        return availablePCNToAlert;
    }
    private static String muleServiceUrl;
    public static String MuleServiceUrl() {
        return muleServiceUrl;
    }
    private static boolean roleSelectionAtLogin=false;
    public static boolean RoleSelectionAtLogin() {
        return roleSelectionAtLogin;
    }
    private static String selectOneText;
    public static String SelectOneText() {
        return selectOneText;
    }
    private static String defectReportingUrl;
    public static String DefectReportingUrl() {
        return defectReportingUrl;
    }
    private static String streetExtractUrl;
    public static String StreetExtractUrl() {
        return streetExtractUrl;
    }
    private static String ceoExtractUrl;
    public static String CeoExtractUrl() {
        return ceoExtractUrl;
    }
    private static int image_x_size;
    public static int Image_x_size() {
        return image_x_size;
    }
    private static int image_y_size;
    public static int Image_y_size() {
        return image_y_size;
    }
    private static boolean allowVoid=false;
    public static boolean AllowVoid() {
        return allowVoid;
    }
    private static boolean alwaysRequireValves=false;
    public static boolean AlwaysRequireValves() {
        return alwaysRequireValves;
    }
    private static String minimumObservationTimesUrl;
    public static String MinimumObservationTimesUrl() {
        return minimumObservationTimesUrl;
    }

    private static boolean photosRequired=false;
    public static boolean PhotosRequired() {
        return photosRequired;
    }
    private static boolean notesRequired=false;
    public static boolean NotesRequired() {
        return notesRequired;
    }
    private static String photosNotesText;
    public static String PhotosNotesText(){
        return photosNotesText;
    }
    private static String virtualPermitsUrl;
    public static String VirtualPermitsUrl(){
        return virtualPermitsUrl;
    }

    private static String message_url;
    public static String getMessageUrl(){
        return message_url;
    }

    private static long messageReadTimerInMinute;
    public static long getMessageReadTimerInMinute(){
        return messageReadTimerInMinute;
    }

    private static String newTicketNumbersUrl;
    public static String NewTicketNumbersUrl(){
        return newTicketNumbersUrl;
    }
    public static String commercialVehicleCodes;
    public static String disabledBadgeCodes;
    public static String payAndDisplayCodes;

    public static String unsentPcnTimeInterval;
    public static String getUnsentPcnTimeInterval(){return unsentPcnTimeInterval;}

    private static boolean allowContraventionChange=false;
    public static boolean AllowContraventionChange(){
        return allowContraventionChange;
    }

    private static String specialVehicleUrl;

    public static void setSpecialVehicleUrl(String specialVehicleUrl) {
        CeoApplication.specialVehicleUrl = specialVehicleUrl;
    }

    public static String getSpecialVehicleUrl() {
        return specialVehicleUrl;
    }

    private static String blackWhiteListVehicleChannel;

    public static String getBlackWhiteListVehicleChannel(){
        return blackWhiteListVehicleChannel;
    }

    private static String messageReadChannel;
    public static String getMessageReadChannel(){
        return messageReadChannel;
    }

    private static boolean checkPermitSession;
    public static boolean getCheckPermitSession(){
        return checkPermitSession;
    }

    private static boolean sendAllPhotosRealTime;
    public static boolean SendAllPhotosRealTime(){
        return sendAllPhotosRealTime;
    }

    private static boolean multipartUpload;
    public static boolean multipartUpload()
    {
        return  multipartUpload;
    }

    public static String BASE_URL_MULTIPART = "";
    public static String getMultipartUploadBaseURL()
    {
        return  BASE_URL_MULTIPART;
    }

    private static boolean useAnpr;
    public static boolean useAnpr(){
        return useAnpr;
    }

    private static String cashlessParkingProvider;
    public static String cashlessParkingProvider(){
        return cashlessParkingProvider;
    }

    private static String barcodeType="";
    public static String getBarcodeType(){
        return barcodeType;
    }

    private static String payPointClientID="";
    public static String getPaypointClientID(){
        return payPointClientID;
    }

    private static String payPointClientParam="";
    public static String getPaypointClientParam(){
        return payPointClientParam;
    }

    private static boolean instantWarnNotice;
    public static boolean InstantWarnNotice(){
        return instantWarnNotice;
    }

    private static boolean locationChangeLookUp;
    public static boolean getLocationChangeLookUp(){
        return locationChangeLookUp;
    }

    private static boolean vrmEntryLookUp;
    public static boolean getVrmEntryLookUp(){
        return vrmEntryLookUp;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Runtime.getRuntime().gc();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static void LogError(String errorContent) throws Exception{

        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        File fileToCreate =cameraImageHelper.getFile(AppConstant.APP_ERROR_FOLDER, sdf.format(new Date())+ "_" +"err.log");
        FileOutputStream os = new FileOutputStream(fileToCreate, true);
        errorContent = errorContent + "\n";
        os.write(errorContent.getBytes());
        os.close();
     }

    public static void LogInfo(String errorContent){
        try {

            CameraImageHelper cameraImageHelper = new CameraImageHelper();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            File fileToCreate = cameraImageHelper.getFile(AppConstant.APP_ERROR_FOLDER, sdf.format(new Date()) + "_" + "info.log");
            FileOutputStream os = new FileOutputStream(fileToCreate, true);
            errorContent = errorContent + "\n";
            os.write(errorContent.getBytes());
            os.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }





    public static void LogError(Throwable exc) throws Exception {

        //FirebaseCrashlytics.getInstance().recordException(new Exception("hi this is the main function exception "));
        //FirebaseCrashlytics.getInstance().recordException(exc);
        FirebaseCrashlytics.getInstance().log("LogError 3 log exception");
        FirebaseCrashlytics.getInstance().recordException(exc);

        final String SINGLE_LINE_SEP = "\n";
        final String DOUBLE_LINE_SEP = "\n\n";
        StackTraceElement[] arr = exc.getStackTrace();
        final StringBuffer report = new StringBuffer(exc.toString());
        final String lineSeperator = "-------------------------------\n\n";
        report.append(lineSeperator);
        report.append("--------- Stack trace ---------\n\n");
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        report.append(lineSeperator);
        report.append("--------- Cause ---------\n\n");
        Throwable cause = exc.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        String errorContent = report.toString();
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        File fileToCreate =cameraImageHelper.getFile(AppConstant.APP_ERROR_FOLDER, sdf.format(new Date())+ "_" +"err.log");
        FileOutputStream os = new FileOutputStream(fileToCreate, true);
        errorContent = errorContent + SINGLE_LINE_SEP;
        os.write(errorContent.getBytes());
        os.close();
    }
    public static void LogErrorOnChannel(Context context, Throwable exc, int location)throws Exception
    {

        Log.e("log error", "LogErrorOnChannel");

        final String SINGLE_LINE_SEP = "\n";
        final String DOUBLE_LINE_SEP = "\n\n";
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        StackTraceElement[] arr = exc.getStackTrace();
        final StringBuffer report = new StringBuffer(exc.toString());
        final String lineSeperator = "-------------------------------\n\n";
        report.append(lineSeperator);
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append(SINGLE_LINE_SEP);
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n\n");
        Throwable cause = exc.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        // Getting the Device brand,model and sdk verion details.
        report.append(lineSeperator);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(SINGLE_LINE_SEP);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(SINGLE_LINE_SEP);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(SINGLE_LINE_SEP);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(SINGLE_LINE_SEP);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append(SINGLE_LINE_SEP);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);

        String version = "";
        String errorText = report.toString();
        if (packageInfo != null)
            version = "Version :" + packageInfo.versionName + "." + packageInfo.versionCode;

        ErrorTable errorRecord = new ErrorTable();
        errorRecord.setErrorLoc(location);
        errorRecord.setErrorText(errorText);
        errorRecord.save();

        //TODO: Need to Check
        pubNubModule.publishError(errorText, location, version);
    }

    public static void GetConfigDetails(){
        try {
            if(pubNubModule==null){
                pubNubModule = new PubNubModule(context, "RemovalPhoto");
                pubNubModule.subscribeRemovalPhotos();
            }

            File configJsonFile = new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "config.json");
            if (configJsonFile.exists()) {
                InputStream configStream = new FileInputStream(configJsonFile);
                JSONObject configJSON = new JSONObject(StringUtil.getStringFromInputStream(configStream));
                if(!configJSON.has("publishkey") || configJSON.getString("publishkey").length()==0){
                    //pubKey = "pub-c-d2a39ee1-e695-4a71-91c7-54b7ca8cdb80";
                    configurationMissing = true;
                }else{
                    pubKey = configJSON.getString("publishkey");
                }
                if(!configJSON.has("subscribekey") || configJSON.getString("subscribekey").length()==0){
                    //subsKey = "sub-c-c3dc2b80-d1d9-11e3-b488-02ee2ddab7fe";
                    configurationMissing = true;
                }else{
                    subsKey = configJSON.getString("subscribekey");
                }
                if(!configJSON.has("secretkey") || configJSON.getString("secretkey").length()==0){
                    secKey = "";
                }else{
                    secKey = configJSON.getString("secretkey");
                }
                if(!configJSON.has("ssl") || configJSON.getString("ssl").length()==0){
                    ssl = false;
                }else{
                    ssl = Boolean.parseBoolean(configJSON.getString("ssl"));
                }
                if(!configJSON.has("channel") || configJSON.getString("channel").length()==0){
                    //channel = "publish_pcn";
                    configurationMissing = true;
                }else{
                    channel = configJSON.getString("channel");
                }
                if(!configJSON.has("errorchannel") || configJSON.getString("errorchannel").length()==0){
                    //errorChannel ="error_channel";
                    configurationMissing = true;
                }else{
                    errorChannel = configJSON.getString("errorchannel");
                }
                if(!configJSON.has("pcnreloadhours") || configJSON.getString("pcnreloadhours").length()==0){
                    pcnReloadHours =0;
                }else{
                    pcnReloadHours = Integer.parseInt(configJSON.getString("pcnreloadhours"));
                }
                if(!configJSON.has("clientlogotext") || configJSON.getString("clientlogotext").length()==0){
                    clientLogoText = "PARKING SERVICES";
                }else{
                    clientLogoText = configJSON.getString("clientlogotext");
                }

                if(!configJSON.has("payByPhoneUserId") || configJSON.getString("payByPhoneUserId").length()==0){
                    payByPhoneUserId = "LambethPatro";
                }else{
                    payByPhoneUserId = configJSON.getString("payByPhoneUserId");
                }
                if(!configJSON.has("payByPhoneUserPassword") || configJSON.getString("payByPhoneUserPassword").length()==0){
                    payByPhoneUserPassword = "PatR01LBL";
                }else{
                    payByPhoneUserPassword = configJSON.getString("payByPhoneUserPassword");
                }
                if(!configJSON.has("payByPhoneContraventionCodes") || configJSON.getString("payByPhoneContraventionCodes").length()==0){
                    payByPhoneContraventionCodes = "12,19,30,82,83";
                }else{
                    payByPhoneContraventionCodes = configJSON.getString("payByPhoneContraventionCodes");
                }
                if(!configJSON.has("queryingPayByPhone") || configJSON.getString("queryingPayByPhone").length()==0){
                    queryingPayByPhone = "Querying Pay By Phone";
                }else{
                    queryingPayByPhone = configJSON.getString("queryingPayByPhone");
                }
                if(!configJSON.has("validParkingUntil") || configJSON.getString("validParkingUntil").length()==0){
                    validParkingUntil = "Valid parking until @datetime@";
                }else{
                    validParkingUntil = configJSON.getString("validParkingUntil");
                }
                if(!configJSON.has("queryWaitTime") || configJSON.getString("queryWaitTime").length()==0){
                    queryWaitTime = 15;
                }else{
                    queryWaitTime = Integer.valueOf(configJSON.getString("queryWaitTime"));
                }
                if(!configJSON.has("pcnprefix") || configJSON.getString("pcnprefix").length()==0){
                    pcnPrefix = "";
                }else{
                    pcnPrefix = configJSON.getString("pcnprefix");
                }
                if(!configJSON.has("subscribeChannel") || configJSON.getString("subscribeChannel").length()==0){
                    subscribeChannel = "subscribeTicket_";
                }else{
                    subscribeChannel = configJSON.getString("subscribeChannel");
                }
                if(!configJSON.has("availablePCNToAlert") || configJSON.getString("availablePCNToAlert").length()==0){
                    availablePCNToAlert = 20;
                }else{
                    availablePCNToAlert = Integer.valueOf(configJSON.getString("availablePCNToAlert"));
                }
                if(!configJSON.has("muleserviceurl") || configJSON.getString("muleserviceurl").length()==0){
                    muleServiceUrl = "";
                }else{
                    muleServiceUrl = configJSON.getString("muleserviceurl");
                }
                if(!configJSON.has("roleSelectionAtLogin") || configJSON.getString("roleSelectionAtLogin").length()==0){
                    roleSelectionAtLogin = false;
                }else{
                    String configValue = configJSON.getString("roleSelectionAtLogin");
                    if(configValue.equalsIgnoreCase("true")){
                        roleSelectionAtLogin = true;
                    }else{
                        roleSelectionAtLogin = false;
                    }
                }
                if(!configJSON.has("selectOneText") || configJSON.getString("selectOneText").length()==0){
                    selectOneText = null;
                }else{
                    selectOneText = configJSON.getString("selectOneText");
                }
                if(!configJSON.has("defectReportingUrl") || configJSON.getString("defectReportingUrl").length()==0){
                    defectReportingUrl = null;
                }else{
                    defectReportingUrl = configJSON.getString("defectReportingUrl");
                }
                if(!configJSON.has("streetExtractUrl") || configJSON.getString("streetExtractUrl").length()==0){
                    streetExtractUrl = null;
                    //For testing
                    //streetExtractUrl = "http://82.148.231.189/stripandfixstreets/sfs-web.php"; //lambeth test
                    //streetExtractUrl = "http://82.148.231.170/stripandfixstreets/sfs-web.php";
                }else{
                    streetExtractUrl = configJSON.getString("streetExtractUrl");
                }
                if(!configJSON.has("ceoExtractUrl") || configJSON.getString("ceoExtractUrl").length()==0){
                    ceoExtractUrl = null;
                    //For testing
                    //ceoExtractUrl = "http://82.148.231.184:8010/hht/data/extract/coreservice/GetHHTExtract?userId=farthestgate&token=lazyfox&extract=ceos";
                }else{
                    ceoExtractUrl = configJSON.getString("ceoExtractUrl");
                }
                if(!configJSON.has("image_x_size") || configJSON.getString("image_x_size").length()==0){
                    image_x_size = 960;
                }else{
                    image_x_size = Integer.valueOf(configJSON.getString("image_x_size"));
                }
                if(!configJSON.has("image_y_size") || configJSON.getString("image_y_size").length()==0){
                    image_y_size = 540;
                }else{
                    image_y_size = Integer.valueOf(configJSON.getString("image_y_size"));
                }
                if(!configJSON.has("allow_void") || configJSON.getString("allow_void").length()==0){
                    allowVoid = false;
                }else{
                    String configValue = configJSON.getString("allow_void");
                    if(configValue.equalsIgnoreCase("true")){
                        allowVoid = true;
                    }else{
                        allowVoid = false;
                    }
                }
                if(!configJSON.has("always_require_valves") || configJSON.getString("always_require_valves").length()==0){
                    alwaysRequireValves = false;
                    // For Testing
                    //alwaysRequireValves = true;
                }else{
                    String configValue = configJSON.getString("always_require_valves");
                    if(configValue.equalsIgnoreCase("true")){
                        alwaysRequireValves = true;
                    }else{
                        alwaysRequireValves = false;
                    }
                }
                if(!configJSON.has("minimumObservationTimesUrl") || configJSON.getString("minimumObservationTimesUrl").length()==0){
                    minimumObservationTimesUrl = null;
                    //For testing
                    //minimumObservationTimesUrl = "http://82.148.231.189:8010/hht/data/extract/coreservice/GetHHTExtract?userId=farthestgate&token=lazyfox&extract=obs";
                }else{
                    minimumObservationTimesUrl = configJSON.getString("minimumObservationTimesUrl");
                }

                if(!configJSON.has("singleview_timeout") || configJSON.getString("singleview_timeout").length()==0){
                    singleviewTimeout = "30000";
                }else{
                    singleviewTimeout = configJSON.getString("singleview_timeout");
                }

                if(!configJSON.has("photos_required") || configJSON.getString("photos_required").length()==0){
                    photosRequired = false;
                    //For testing
                    //photosRequired = true;
                }else{
                    String configValue = configJSON.getString("photos_required");
                    if(configValue.equalsIgnoreCase("true")){
                        photosRequired = true;
                    }else{
                        photosRequired = false;
                    }
                }
                if(!configJSON.has("notes_required") || configJSON.getString("notes_required").length()==0){
                    notesRequired = false;
                    //For testing
                    //notesRequired = true;
                }else{
                    String configValue = configJSON.getString("notes_required");
                    if(configValue.equalsIgnoreCase("true")){
                        notesRequired = true;
                    }else{
                        notesRequired = false;
                    }
                }
                if(!configJSON.has("allowcontraventionchange") || configJSON.getString("allowcontraventionchange").length()==0){
                    allowContraventionChange = false;
                    //For testing
                    //allowContraventionChange = true;
                }else{
                    String configValue = configJSON.getString("allowcontraventionchange");
                    if(configValue.equalsIgnoreCase("true")){
                        allowContraventionChange = true;
                    }else{
                        allowContraventionChange = false;
                    }
                }
                if(!configJSON.has("photos_notes_text") || configJSON.getString("photos_notes_text").length()==0){
                    photosNotesText = "You do not appear to have taken the required photo and note evidence. Please ensure this information is captured.";
                }else{
                    photosNotesText = configJSON.getString("photos_notes_text");
                }
                if(!configJSON.has("virtual_permits_url") || configJSON.getString("virtual_permits_url").length()==0){
                    virtualPermitsUrl = null;
                    //For Testing
                    //virtualPermitsUrl = "http://www.taitc.co.uk/fakepermits.php";
                }else{
                    virtualPermitsUrl = configJSON.getString("virtual_permits_url");
                }
                if(!configJSON.has("new_ticket_numbers_url") || configJSON.getString("new_ticket_numbers_url").length()==0){
                    newTicketNumbersUrl = null;
                    //For Testing
                    //newTicketNumbersUrl = "http://www.taitc.co.uk/fakepermits.php";
                }else{
                    newTicketNumbersUrl = configJSON.getString("new_ticket_numbers_url");
                }
                if(!configJSON.has("removal_photo_publish_channel") || configJSON.getString("removal_photo_publish_channel").length()==0){
                    removalPhotoPublishChannel = null;
                }else{
                    removalPhotoPublishChannel = configJSON.getString("removal_photo_publish_channel");
                }

                if(!configJSON.has("removal_photo_subscribe_channel") || configJSON.getString("removal_photo_subscribe_channel").length()==0){
                    removalPhotoSubscribeChannel = null;
                }else{
                    removalPhotoSubscribeChannel = configJSON.getString("removal_photo_subscribe_channel");
                }
                if(!configJSON.has("ceo_tracking_channel") || configJSON.getString("ceo_tracking_channel").length()==0){
                    ceoTrackingChannel = null;
                }else{
                    ceoTrackingChannel = configJSON.getString("ceo_tracking_channel");
                }

                if(!configJSON.has("code_red_channel") || configJSON.getString("code_red_channel").length()==0){
                    codeRedChannel = null;
                }else{
                    codeRedChannel = configJSON.getString("code_red_channel");
                }
                if(!configJSON.has("hhtsyncinfo") || configJSON.getString("hhtsyncinfo").length()==0){
                    syncConfirmationChannel = null;
                }else{
                    syncConfirmationChannel = configJSON.getString("hhtsyncinfo");
                }
                if(!configJSON.has("commercial_vehicle_codes") || configJSON.getString("commercial_vehicle_codes").length()==0){
                    commercialVehicleCodes = null;
                }else{
                    commercialVehicleCodes = configJSON.getString("commercial_vehicle_codes");
                }
                if(!configJSON.has("disabled_badge_codes") || configJSON.getString("disabled_badge_codes").length()==0){
                    disabledBadgeCodes = null;
                }else{
                    disabledBadgeCodes = configJSON.getString("disabled_badge_codes");
                }
                if(!configJSON.has("pay_and_display_codes") || configJSON.getString("pay_and_display_codes").length()==0){
                    payAndDisplayCodes = null;
                }else{
                    payAndDisplayCodes = configJSON.getString("pay_and_display_codes");
                }
                if(!configJSON.has("unsent_pcn_time_interval") || configJSON.getString("unsent_pcn_time_interval").length()==0){
                    unsentPcnTimeInterval = null;
                }else{
                    unsentPcnTimeInterval = configJSON.getString("unsent_pcn_time_interval");
                }
                if(!configJSON.has("special_vehicle_url") || configJSON.getString("special_vehicle_url").length()==0){
                    specialVehicleUrl = null;
                }else{
                    specialVehicleUrl = configJSON.getString("special_vehicle_url");
                }
                if(!configJSON.has("black_white_vehicle_channel") || configJSON.getString("black_white_vehicle_channel").length()==0){
                    blackWhiteListVehicleChannel = null;
                }else{
                    blackWhiteListVehicleChannel = configJSON.getString("black_white_vehicle_channel");
                }

                if(!configJSON.has("messageReadChannel") || configJSON.getString("messageReadChannel").length()==0){
                    messageReadChannel = null;
                }else{
                    messageReadChannel = configJSON.getString("messageReadChannel");
                }

                if(!configJSON.has("message_url") || configJSON.getString("message_url").length()==0){
                    message_url =null;
                }else{
                    message_url = configJSON.getString("message_url");
                }

                if(!configJSON.has("message_read_timer_in_minute") || configJSON.getString("message_read_timer_in_minute").length()==0){
                    messageReadTimerInMinute =0;
                }else{
                    messageReadTimerInMinute = Long.parseLong(configJSON.getString("message_read_timer_in_minute"))*60*1000;
                }

                if(!configJSON.has("check_parking_session") || configJSON.getString("check_parking_session").length()==0){
                    checkPermitSession = false;
                }else{
                    checkPermitSession = Boolean.parseBoolean(configJSON.getString("check_parking_session"));
                }
                if(!configJSON.has("send_all_photos_realtime") || configJSON.getString("send_all_photos_realtime").length()==0){
                    sendAllPhotosRealTime = false;
                }else{
                    sendAllPhotosRealTime = Boolean.parseBoolean(configJSON.getString("send_all_photos_realtime"));
                }

                if(!configJSON.has("vrm_entry_lookup") || configJSON.getString("vrm_entry_lookup").length()==0){
                    vrmEntryLookUp = false;
                }else{
                    vrmEntryLookUp = Boolean.parseBoolean(configJSON.getString("vrm_entry_lookup"));
                }

                if(!configJSON.has("multipart_upload") || configJSON.getString("multipart_upload").length()==0){
                    multipartUpload = false;
                }else{
                    multipartUpload = Boolean.parseBoolean(configJSON.getString("multipart_upload"));
                }

                if(!configJSON.has("base_url_multipart") || configJSON.getString("base_url_multipart").length()==0){
                    BASE_URL_MULTIPART = "";
                }else{
                    BASE_URL_MULTIPART = configJSON.getString("base_url_multipart");
                }


                if(!configJSON.has("use_anpr") || configJSON.getString("use_anpr").length()==0){
                    useAnpr = false;
                }else{
                    useAnpr = Boolean.parseBoolean(configJSON.getString("use_anpr"));
                }

                if(!configJSON.has("barcode_type") || configJSON.getString("barcode_type").length()==0){
                    barcodeType = "";
                }else{
                    barcodeType = configJSON.getString("barcode_type");
                }
                if(!configJSON.has("paypoint_client_ID") || configJSON.getString("paypoint_client_ID").length()==0){
                    payPointClientID = "";
                }else{
                    payPointClientID = configJSON.getString("paypoint_client_ID");
                }
                if(!configJSON.has("paypoint_client_param") || configJSON.getString("paypoint_client_param").length()==0){
                    payPointClientParam = "";
                }else{
                    payPointClientParam = configJSON.getString("paypoint_client_param");
                }

                if(!configJSON.has("cashless_parking_provider") || configJSON.getString("cashless_parking_provider").length()==0){
                    cashlessParkingProvider = null;
                }else{
                    cashlessParkingProvider = configJSON.getString("cashless_parking_provider");
                }
                if(!configJSON.has("instant_warn_notice") || configJSON.getString("instant_warn_notice").length()==0){
                    instantWarnNotice = false;
                }else{
                    instantWarnNotice = Boolean.parseBoolean(configJSON.getString("instant_warn_notice"));
                }

                if(!configJSON.has("location_change_lookup") || configJSON.getString("location_change_lookup").length()==0){
                    locationChangeLookUp = false;
                }else{
                    locationChangeLookUp = Boolean.parseBoolean(configJSON.getString("location_change_lookup"));
                }


                if(!configurationMissing) {
                    if (!configJSON.has("usepubnub") || configJSON.getString("usepubnub").length() == 0) {
                        usePubNub = true;
                    } else {
                        usePubNub = Boolean.parseBoolean(configJSON.getString("usepubnub"));
                    }
                }else{
                    usePubNub = false;
                    try {
                        LogError("PubNub configuration missing. Please contact supervisor immediately.");
                        LogErrorOnChannel(getContext(), new Exception("PubNub configuration missing. Please contact supervisor immediately."), ErrorLocations.location402);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                if(!configJSON.has("updateGPS") || configJSON.getString("updateGPS").length()==0){
                    updateGPS = 30;
                }else{
                    updateGPS = Integer.parseInt(configJSON.getString("updateGPS"));
                }

                if(!configJSON.has("pageWidth") || configJSON.getString("pageWidth").length()==0){
                    pageWidth = 40;
                }else{
                    pageWidth = Integer.parseInt(configJSON.getString("pageWidth"));
                }

                if(!configJSON.has("transfer_protocol") || configJSON.getString("transfer_protocol").length()==0){
                    transferProtocol = "FTP";
                }else{
                    transferProtocol = configJSON.getString("transfer_protocol");
                }

                if(!configJSON.has("PRIORITY_HIGH_ACCURACY") || configJSON.getString("PRIORITY_HIGH_ACCURACY").length()==0){
                    priorityHighAccuracy = 100;
                }else{
                    priorityHighAccuracy = Integer.parseInt(configJSON.getString("PRIORITY_HIGH_ACCURACY"));
                }

                if(!configJSON.has("usegeocoder") || configJSON.getString("usegeocoder").length()==0){
                    useGeocoder = false;
                }else{
                    String val  = configJSON.getString("usegeocoder");
                     if(val.equalsIgnoreCase("-1")
                            ||val.equalsIgnoreCase("yes")
                            ||val.equalsIgnoreCase("true")
                            ){
                        useGeocoder=true;
                    }
                }
                if(!configJSON.has("frequency") || configJSON.getString("frequency").length()==0){
                    frequency = -1;
                }else{
                    frequency = Integer.parseInt(configJSON.getString("frequency"));
                }

                if(!configJSON.has("record_observation_VRM") || configJSON.getString("record_observation_VRM").length()==0){
                    recordObservationVRM = true;
                }else{
                    recordObservationVRM = configJSON.getBoolean("record_observation_VRM");
                }
                if(!configJSON.has("record_observation_VRM_value") || configJSON.getString("record_observation_VRM_value").length()==0){
                    recordObservationVRMValue = "OBSERVED";
                }else{
                    recordObservationVRMValue =configJSON.getString("record_observation_VRM_value");
                }

                if(!configJSON.has("ftpConnectionTimeout") || configJSON.getString("ftpConnectionTimeout").length()==0){
                    ftpConnectionTimeout =3000;
                }else{
                    ftpConnectionTimeout =configJSON.getInt("ftpConnectionTimeout");
                }

            }else{
                configFileMissing = true;
                usePubNub = false;
                try {
                    LogError("Missing configuration file. Please contact supervisor immediately.");
                    LogErrorOnChannel(getContext(), new Exception("Missing configuration file. Please contact supervisor immediately."), ErrorLocations.location402);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                 /* No need to save the config file information into database. Always read from the file.
                List<ConfigTable> configTableRow = DBHelper.getConfig();
                if (configTableRow.size() > 0) {
                    ConfigTable row = configTableRow.get(0);
                    pubKey = row.getPublishKey();
                    subsKey = row.getSubscribeKey();
                    secKey = row.getSecretKey();
                    ssl = Boolean.parseBoolean(row.getSSL());
                    channel = row.getChannel();
                    errorChannel = row.getErrorChannel();
                    usePubNub = Boolean.parseBoolean(row.getUsePubNub());
                }
                */
            }

        } catch (Exception e) {
            try {
                LogError(e.getMessage());
                LogErrorOnChannel(getContext(),e, ErrorLocations.location402);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

   /* private static void PublishError(String error, int location, String version) {
        try {
            if(!UsePubNub()) return;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version",version);
            jsonObject.put("error",error);
            jsonObject.put("location",location);
            Pubnub pubnub = getPubnubInstance();
            Hashtable<String, Object> args = new Hashtable<String, Object>(2);
            args.put("message", jsonObject);
            args.put("channel", ErrorChannel());
            pubnub.publish(args, new  com.pubnub.api.Callback() {
                public void successCallback(String channel, Object message) {
                    Log.i("Received response: ", message.toString());
                }

                public void errorCallback(String channel, Object message) {
                    Log.e("Error publishing msg: ", message.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    //No need to load the ceo details in database.
    //Read it from the file directly
    public static JSONArray GetDataFileContent(String fileName){
        try{
            JSONArray fileContent =null;
            File configJsonFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + fileName);
            if (configJsonFile.exists()) {
                InputStream configStream = new FileInputStream(configJsonFile);
                String jsonData = StringUtil.getStringFromInputStream(configStream);
                fileContent = new JSONArray(jsonData);
            }
            return fileContent;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static JSONObject GetDataFileContentAsObject(String fileName){
        try{
            JSONObject fileContent =null;
            File dataJsonFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + fileName);
            if (dataJsonFile.exists()) {
                InputStream configStream = new FileInputStream(dataJsonFile);
                String jsonData = StringUtil.getStringFromInputStream(configStream);
                fileContent = new JSONObject(jsonData);
            }
            return fileContent;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean IsDataFileExist(String fileName){
        boolean fileExist= false;
        try{
            File dataFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + fileName);
            fileExist = dataFile.exists();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return fileExist;
    }

    public static String logJSONError(boolean toJSONreturnednull, boolean seconbdfilewritefailed, boolean photosbutnodata, String pcnNumber){
        JSONObject jsonObject = new JSONObject();
        String filePath = null;
        try {
            jsonObject.put("toJSONreturnednull", toJSONreturnednull);
            jsonObject.put("seconbdfilewritefailed", seconbdfilewritefailed);
            jsonObject.put("photosbutnodata", photosbutnodata);
            jsonObject.put("pcnNumber", pcnNumber);

            filePath = LogError(jsonObject.toString(), pcnNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }

    public static String LogError(String errorContent, String pcnNumber) throws Exception{
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        File fileToCreate =cameraImageHelper.getFile(AppConstant.APP_ERROR_FOLDER, pcnNumber + "_nopcndata.txt");
        FileOutputStream os = new FileOutputStream(fileToCreate, true);
        errorContent = errorContent + "\n";
        os.write(errorContent.getBytes());
        os.close();
        return fileToCreate.getAbsolutePath();
    }

    private static void checkNoPCNDataFile(String pcnNumber){
        File photoFile = new CameraImageHelper().getPCNPhotoFolder();
        File [] photoFileArray  = photoFile.listFiles();
        for(int i=0; i<photoFileArray.length; i++) {
            //String pcn = photoFileArray[i].getName().split("-")[0];
            if(photoFileArray[i].getName().contains(pcnNumber)){
                boolean isFileExists = getPCNFile(pcnNumber);
                if(!isFileExists)
                    logJSONError(false, false, true, pcnNumber);
            }


        }
    }

    private static boolean getPCNFile(String photoPcnName){
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        File pcnDataFlie = cameraImageHelper.getFile(AppConstant.PCN_DATA_FOLDER, photoPcnName + "-data.json") ;
        return pcnDataFlie.exists();

        /*
         boolean isFileExists = false;
        File [] pcnFileArray  = pcnDataFlie.listFiles();
        for(int i=0; i<pcnFileArray.length; i++){
            String pcn = pcnFileArray[i].getName().split("-")[0];
            if(pcn.equals(photoPcnName)){
                isFileExists = true;
                return isFileExists;
            }
        }
        return isFileExists;*/
    }

    private void restoreDB(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data/com.farthestgate.android/databases/Ceoapp.db";
                String backupDBPath =  "ceoappdata/pcns/localDB/Ceoapp.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(), "Database Restored successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
