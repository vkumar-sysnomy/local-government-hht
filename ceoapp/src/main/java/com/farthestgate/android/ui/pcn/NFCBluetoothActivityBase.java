package com.farthestgate.android.ui.pcn;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.helper.UnsentPCNService;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PCNJsonData;
import com.farthestgate.android.model.database.ErrorTable;
import com.farthestgate.android.model.database.NotesTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.PrinterCommInterface;
import com.farthestgate.android.ui.PubNubCommInterface;
import com.farthestgate.android.ui.admin.StartDayActivity;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.farthestgate.android.utils.NFCTagUtil;
import com.farthestgate.android.utils.NfcForegroundUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.pubnub.api.PubnubException;
import com.pubnub.api.PubNubException;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by Hanson Aboagye on 23/04/2014.
 *
 * Needed to extend FragmentActivity to allow all activities to connect to the printer
 *
 */
public class NFCBluetoothActivityBase extends FragmentActivity implements PubNubModule.ResponseListener,PubNubModule.BackOfficePcnResponse, PrinterCommInterface, PubNubModule.PostResponseListener, UnsentPCNService.ServiceCallbacks
{
    public static final String LAST_CEO = "lastCEO";
    public static final long  ONE_MIN   = 60000l;
    public static final long  FIVE_MINS = 300000l;
    private static final int    REQUEST_ENABLE_BLUETOOTH = 1;

    final PubNubModule pb = new PubNubModule(NFCBluetoothActivityBase.this);
    public PCNJsonData outPCN;
    public PCN pcnInfo;
    public NfcForegroundUtil nfcForegroundUtil;
    public Crouton noInternetCrouton;
    public NfcAdapter mNfcAdapter;
    public PrinterManager mPrinterManager;
    public SharedPreferenceHelper sharedPreferenceHelper;
    public Integer currentSession;
    public PackageInfo packageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        UnsentPCNService unsentPCNService = new UnsentPCNService();
        unsentPCNService.setCallbacks(this);
        nfcForegroundUtil = new NfcForegroundUtil(this);
        noInternetCrouton = CroutonUtils.noInternet(this);
        mPrinterManager= CeoApplication.getPrinterManager();
        sharedPreferenceHelper = new SharedPreferenceHelper(this);
        currentSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;
        try {
            packageInfo     = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onNewIntent(Intent intent) {
        try {
            if (this instanceof StartDayActivity) {
                if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                    NdefMessage[] msgs = NFCTagUtil.getNdefMessages(intent.getAction(), intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
                    NdefRecord[] ndefRecords = msgs[0].getRecords();
                    SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
                    String printerBluetoothAddress = NFCTagUtil.readTagText(ndefRecords[0]);
                    sharedPreferenceHelper.saveString("printerBluetoothAddress",printerBluetoothAddress);
                    CeoApplication.printerBluetoothAddress = printerBluetoothAddress;
                    startConnection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getPrinterBluetoothAddress() {
        try {
            if (CeoApplication.printerBluetoothAddress == null) {
                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
                CeoApplication.printerBluetoothAddress = sharedPreferenceHelper.getString("printerBluetoothAddress", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CeoApplication.printerBluetoothAddress;
    }

    public void startConnection() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectBluetooth(false);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        checkBluetooth();
    }

    private void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            CroutonUtils.error(this, "Bluetooth is not supported");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }


    @Override
    protected void onResume()
    {
        registerConnectivityReceiver();
        super.onResume();
        nfcForegroundUtil.enableForeground();

    }

    @Override
    protected void onPause() {
        unregisterReceiver(connectivityBR);
        Crouton.clearCroutonsForActivity(this);
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        nfcForegroundUtil.disableForeground();

        super.onPause();
    }

    private void registerConnectivityReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityBR, filter);

    }

    public BroadcastReceiver connectivityBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!DeviceUtils.isConnected(context)) {
                noInternetCrouton.show();
            } else {
                noInternetCrouton.cancel();
                SendPCNs();
            }
        }
    };

    public void SendPCNs() {
        for (PCNTable pcn : DBHelper.getUnsentPCNs()) {
            try {
                String pcnJSON = pcn.getPcnJSON();
                Gson gson = new GsonBuilder().create();
                PCN pcnInfo = gson.fromJson(pcnJSON, PCN.class);
                if(pcnInfo.issueTime==0){
                    continue;
                }
                String PcnOutJSON = pcn.getPcnOUTJSON();
                if (PcnOutJSON == null || PcnOutJSON.isEmpty()) {
                    PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                    JSONObject tempData = new JSONObject(outPCN.toJSON());
                    PcnOutJSON = tempData.toString();
                    pcn.setPcnOUTJSON(PcnOutJSON);
                    pcn.save();
                }
                if (PcnOutJSON != null || !PcnOutJSON.isEmpty()) {
                    pb.publishUnsentPCN(NFCBluetoothActivityBase.this,pcnInfo,new JSONObject("{\"pcn\":" + PcnOutJSON + "}"));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void connectBluetooth(boolean reprint) {
        int model = Integer.parseInt(getResources().getString(R.string.printer_device_model));
        try {
            mPrinterManager.setSendTimeout(100000);
            mPrinterManager.setReceiveTimeout(100000);
            mPrinterManager.setInternationalCharacter(8);
            mPrinterManager.setCodePage(16);
            if(mPrinterManager.isConnect()) {
                mPrinterManager.disconnect();
            }
            String printerBluetoothAddress = getPrinterBluetoothAddress();
            mPrinterManager.connect(model, printerBluetoothAddress, false);
            if(this instanceof StartDayActivity) {
                String msg = "Connected to BT Printer " + printerBluetoothAddress;
                CroutonUtils.info(NFCBluetoothActivityBase.this, msg);
            }
            OnPrinterSuccess(reprint);
        } catch (PrinterException e) {
            OnPrinterError(e,reprint);
        }
    }

    @Override
    public void OnSuccess() {
        pcnInfo.receivedTime = new DateTime().getMillis();
        outPCN = new PCNJsonData(pcnInfo);
        UpdateSavedPCN(true);
    }

    @Override
    public void OnFailure() {
        UpdateSavedPCN(false);
    }

    @Override
    public void OnPostSuccess(String pcnNumber) {
        UpdateUnsentPCN(pcnNumber, true);
    }

    @Override
    public void OnPostFailure(String pcnNumber) {
        UpdateUnsentPCN(pcnNumber, false);
    }

    public void SavePCN()
    {
        try {
            PCNTable finalPcn = new PCNTable();
            List<PCNTable> pcns = DBHelper.GetPCN(pcnInfo.pcnNumber);
            if (pcns.size() > 0)
                finalPcn = pcns.get(0);
            else {
                finalPcn.setObservation(pcnInfo.observationNumber);
                finalPcn.setPcnNumber(pcnInfo.pcnNumber);
                finalPcn.setCeoNumber(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
            }
            if (outPCN != null) {
                String jsonOutString = outPCN.toJSON();
                if(jsonOutString != null){
                    finalPcn.setPcnOUTJSON(jsonOutString);
                } else{
                    String filePath = CeoApplication.logJSONError(true, false, false, pcnInfo.pcnNumber);
                    jsonOutString = reCreatePcnOUTJSON(filePath);
                    finalPcn.setPcnOUTJSON(jsonOutString);
                }
            }
            finalPcn.setPcnJSON(pcnInfo.toJSON());
            finalPcn.setPcnSession(currentSession);
            finalPcn.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String reCreatePcnOUTJSON(String filePath){
        PCNJsonData outPCN = new PCNJsonData(pcnInfo);
        String jsonOutString = outPCN.toJSON();
        if(jsonOutString != null){
            File file = new File(filePath);
            file.delete();
        }else{
            CeoApplication.logJSONError(true, false, false, pcnInfo.pcnNumber);
        }
        return  jsonOutString;
    }

    private void UpdateSavedPCN(boolean sent)
    {
        PCNTable finalPcn;
        List<PCNTable> pcns = DBHelper.GetPCN(pcnInfo.pcnNumber);
        if (pcns.size() > 0)
        {
            finalPcn = pcns.get(0);
            finalPcn.setPcnOUTJSON(outPCN.toJSON());
            finalPcn.setPcnSent(sent);
            finalPcn.save();
        }
    }

    private void updateBackOfficeSent(String pcnNumber, boolean status)
    {
        PCNTable finalPcn;
        List<PCNTable> pcns = DBHelper.GetPCN(pcnNumber);
        if (pcns.size() > 0)
        {
            finalPcn = pcns.get(0);
            finalPcn.setBOSent(status);
            finalPcn.save();
        }
    }


    private void UpdateUnsentPCN(String pcnNumber, boolean status) {
        PCNTable finalPcn;
        List<PCNTable> PCNs = DBHelper.GetPCN(pcnNumber);
        if (PCNs.size() > 0) {
            finalPcn = PCNs.get(0);
            finalPcn.setPcnSent(status);
            finalPcn.save();
        }
    }

    public void setContraventionCharge(){
        if (pcnInfo.contravention.agreementCode.equals(Contravention.higherA)) {
            pcnInfo.fullPrice = 130;
            pcnInfo.halfPrice = 65;
            pcnInfo.chargeBand = Contravention.higherA;
        }
        if (pcnInfo.contravention.agreementCode.equals(Contravention.lowerA)) {
            pcnInfo.fullPrice = 80;
            pcnInfo.halfPrice = 40;
            pcnInfo.chargeBand = Contravention.lowerA;
        }
        if (pcnInfo.contravention.agreementCode.equals(Contravention.higherB)) {
            pcnInfo.fullPrice = 110;
            pcnInfo.halfPrice = 55;
            pcnInfo.chargeBand = Contravention.higherB;
        }
        if ((pcnInfo.contravention.agreementCode.equals(Contravention.lowerB))) {
            pcnInfo.fullPrice = 60;
            pcnInfo.halfPrice = 30;
            pcnInfo.chargeBand = Contravention.lowerB;
        }
    }

    public void OnException(Context context, Throwable exc, int location) {
        final String SINGLE_LINE_SEP = "\n";
        final String DOUBLE_LINE_SEP = "\n\n";


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

        new PubNubModule(context).publishError(errorText,location,version);


    }

    public void deleteUnusedPhotos(int observation) {
        List<PCNPhotoTable> photos = DBHelper.PhotosForPCN(observation);
        if (photos.size() >0) {
            for (PCNPhotoTable photo : photos) {
                File filePhoto = new File(photo.getFileName());
                filePhoto.delete();
            }
            DBHelper.removePhotosForCancelObs(observation);
        }
    }

    public void deleteUnusedNotes(int observation) {
        List<NotesTable> notesTables = DBHelper.getPCNNotes(observation);
        if (notesTables.size() >0) {
            for (NotesTable notesTable : notesTables) {
                File noteFile = new File(notesTable.getFileName());
                noteFile.delete();
            }
            DBHelper.removeNotesForCancelObs(observation);
        }
    }

    @Override
    public void OnPrinterError(PrinterException pe, boolean reprint) {

    }

    @Override
    public void OnPrinterSuccess(boolean reprint) {

    }

    @Override
    public void unsentPCNJson(PCNJsonData outPCN) {
        try {
            String PcnOutJSON = outPCN.toJSON();
            pb.publishUnsentPCN(NFCBluetoothActivityBase.this,pcnInfo,new JSONObject("{\"pcn\":" + PcnOutJSON + "}"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackOfficePcnSuccess(String pcnNumber) {
        updateBackOfficeSent(pcnNumber,true);
    }

    @Override
    public void onBackOfficePcnFailure(String pcnNumber) {
        updateBackOfficeSent(pcnNumber,false);
    }

    class PublishPCNAsyncTask extends AsyncTask<PubNubModule, Void, Void> implements PubNubCommInterface {
        String outputJSON;

        public PublishPCNAsyncTask(String inJSON)
        {
            outputJSON = inJSON;
        }

        public PublishPCNAsyncTask() {
            outputJSON = outPCN.toJSON();
        }

        @Override
        protected Void doInBackground(PubNubModule... params) {
            try {
                params[0].publish(NFCBluetoothActivityBase.this,pcnInfo,new JSONObject("{\"pcn\":" + outputJSON  + "}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (Exception ex)
            {
                OnPubNubSendError(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //TODO: Move this method inside PubNub subscribe on success
            //initiatePhotoTransmission();
        }

        @Override
        public void OnPubNubSendError(PubNubException e) {

        }

        @Override
        public void OnPubNubSendError(Exception e) {

        }
    }

  /*  private void initiatePhotoTransmission(){
        if(pcnInfo.dInfo.actionToTake.equalsIgnoreCase("Remove") && !CeoApplication.SendAllPhotosRealTime()){
            List<File> files = Utils.getFiles(pcnInfo.pcnNumber);
            for (int i =0; i<files.size(); i++) {
                try {
                    Utils.CopyDirectory(files.get(i), Utils.getFile(AppConstant.REMOVALPHOTO_FOLDER, files.get(i).getName()));
                } catch (IOException e) {
                    try {
                        CeoApplication.LogError(e.getMessage());
                        CeoApplication.LogErrorOnChannel(this,e, ErrorLocations.location402);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Intent intent = new Intent(this, RemovalPhotoService.class);
        intent.putExtra("removal", pcnInfo.dInfo.actionToTake);
        startService(intent);
    }*/

    class PublishUnsentPCNAsyncTask extends AsyncTask<PubNubModule, Void, Void> implements PubNubCommInterface {
        String unsentOutputJSON;
        public PublishUnsentPCNAsyncTask(String inJSON)
        {
            unsentOutputJSON = inJSON;
        }
        @Override
        protected Void doInBackground(PubNubModule... params) {
            try {
                params[0].publishUnsentPCN(NFCBluetoothActivityBase.this,pcnInfo,new JSONObject("{\"pcn\":" + unsentOutputJSON  + "}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (Exception ex)
            {
                OnPubNubSendError(ex);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
        @Override
        public void OnPubNubSendError(PubNubException e) {

        }

        @Override
        public void OnPubNubSendError(Exception e) {

        }
    }
}
