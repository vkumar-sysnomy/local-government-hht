package com.farthestgate.android.ui.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.FileHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PCNJsonData;
import com.farthestgate.android.model.database.BackupTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.model.database.SyncInfoTable;
import com.farthestgate.android.model.database.TicketNoTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DateUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.farthestgate.android.utils.StringUtil;
import com.farthestgate.android.utils.ZIPUtils;
import com.farthestgate.android.utils.sftp.SFTPClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.ChannelSftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SupervisorActivity extends BaseActivity implements FileHelper.OnCopy {

    private static String progressMsg = "";
    private static Integer pct = 10;

    private File targetDirectory;
    private File configFile;
    private CameraImageHelper cameraImageHelper;
    private Integer totalFiles = 0;
    private Integer index = 1;
    private TextView statusText;
    private ProgressBar progressBar;
    private String zipFileName;
    private FileHelper fileHelper;
    private Button btnSync;

    private String user = "";
    private String host1 = "";
    private String host2 = "";
    private String host3 = "";
    private String host4 = "";
    private String pass = "";
    private String rootfolder = "/hhsync";

    private Boolean noConfigFound = false;
    private SharedPreferenceHelper spH;
    ProgressDialog  progressDialog;
    private static String loadMessage="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_supervisor);
        spH = new SharedPreferenceHelper(SupervisorActivity.this);
        btnSync = (Button) findViewById(R.id.uploadBtn);
        statusText = (TextView) findViewById(R.id.txtUpdate);
        progressBar = (ProgressBar) findViewById(R.id.fileProgressBar);
        fileHelper = new FileHelper(this);
        cameraImageHelper = new CameraImageHelper();
        btnSync.setEnabled(false);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStamp = simpleDateFormat.format(new Date());

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        targetDirectory = cameraImageHelper.CreateGetBackupFolder(dateStamp + "/pcn");

        zipFileName = cameraImageHelper.getBackupFolder().getAbsolutePath() + "/" + dateStamp + "_" + CeoApplication.getUUID() + "-0.zip";

        Runtime.getRuntime().gc();
        Runtime.getRuntime().freeMemory();

        Runtime.getRuntime().gc();
        Runtime.getRuntime().freeMemory();

        statusText.setText("Housekeeping in progress. Please wait...");
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        progressDialog = new ProgressDialog(SupervisorActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        //Watermarking and config setting

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                CameraImageHelper cameraImageHelper = new CameraImageHelper();
                for (PCNPhotoTable photoTableRow : DBHelper.GetPhotosToWatermark()) {
                    File photo = new File(photoTableRow.getFileName());
                    if (photo.exists()) {
                        ExifInterface exifInterface = readExifData(photo.getAbsolutePath());
                        boolean marked = cameraImageHelper.applyImageWatermark(photo, photoTableRow.getTimestamp());
                        saveExifData(exifInterface, photo.getAbsolutePath());
                        if(marked){
                            photoTableRow.setWatermarked(1);
                            photoTableRow.save();
                        }else{
                            try {
                                CeoApplication.LogError("Failed to watermarked the image:" + photo.getName());
                                showMessage("Failed to watermarked the image:" + photo.getName());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    }
                    Runtime.getRuntime().gc();
                    Runtime.getRuntime().freeMemory();
                }

                configFile = new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "config.json");
                if (configFile.exists()) {
                    //read json
                    InputStream json = null;
                    try {
                        json = new FileInputStream(configFile);
                        JSONObject configJSON = new JSONObject(StringUtil.getStringFromInputStream(json));
                        user = configJSON.getString("username");
                        pass = configJSON.getString("password");
                        host1 = configJSON.getString("ftpserver1");
                        host2 = configJSON.getString("ftpserver2");
                        host3 = configJSON.getString("ftpserver3");
                        host4 = configJSON.getString("ftpserver4");
                        rootfolder = configJSON.getString("rootfolder");
                        /* No need to save the config file information into database. Always read from the file.
                        ConfigTable configTable = new ConfigTable();
                        configTable.setHost1(host1);
                        configTable.setHost2(host2);
                        configTable.setUsername(user);
                        configTable.setPassword(pass);zz
                        configTable.setRoot(rootfolder);

                        configTable.setPublishKey(configJSON.getString("publishkey"));
                        configTable.setSubscribeKey(configJSON.getString("subscribekey"));
                        if(!configJSON.has("secretkey") || configJSON.getString("secretkey").length()==0){
                            configTable.setSecretKey("");
                        }else{
                            configTable.setSecretKey(configJSON.getString("secretkey"));
                        }
                        if(!configJSON.has("ssl") || configJSON.getString("ssl").length()==0){
                            configTable.setSSL("false");
                        }else{
                            configTable.setSSL(configJSON.getString("ssl"));
                        }
                        configTable.setChannel(configJSON.getString("channel"));
                        configTable.setErrorChannel(configJSON.getString("errorchannel"));
                        if(!configJSON.has("usepubnub") || configJSON.getString("usepubnub").length()==0){
                            configTable.setUsePubNub("true");
                        }else{
                            configTable.setUsePubNub(configJSON.getString("usepubnub"));
                        }
                        configTable.save();
                        configFile.delete();
                        */
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        try {
                            CeoApplication.LogError(e.getMessage());
                            CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        try {
                            CeoApplication.LogError(e.getMessage());
                            CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    noConfigFound = true;
                    try {
                        CeoApplication.LogError("config.json file is not available at the specified path");
                        CeoApplication.LogErrorOnChannel(SupervisorActivity.this, new Exception("config.json file is not available at the specified path"), ErrorLocations.location402);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                     /* No need to save the config file information into database. Always read from the file.
                    List<ConfigTable> configTableRow = DBHelper.getConfig();
                    if (configTableRow.size() > 0) {
                        ConfigTable row = configTableRow.get(0);
                        user = row.getUsername();
                        host1 = row.getHost1();
                        host2 = row.getHost2();
                        pass = row.getPassword();
                        rootfolder = row.getRoot();
                    } else {
                        noConfigFound = true;
                    }
                    */
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (!noConfigFound) {
                    btnSync.setEnabled(true);
                    statusText.setText("Housekeeping Complete. Please click Start Sync to sync the device");
                } else {
                    statusText.setText("Can not connect to server - no config details loaded. Please contact Technical Support");
                }
                progressBar.setVisibility(View.INVISIBLE);
                SyncUnSyncedPCNs();
            }
        }.execute(null, null, null);

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtils.isConnected(SupervisorActivity.this)) {

                }
                AnalyticsUtils.trackDeviceSync();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                /*
                 * Deleting zero byte image file from photo folder - HAN-32
                 */
                FileHelper.deleteZeroByteFile(cameraImageHelper.getPCNPhotoFolder());
                BackupFiles();
                progressBar.setProgress(25);
                if(ZipFiles()) {
                    progressBar.setProgress(progressBar.getProgress() + 20);
                    DeleteFiles();
                    progressBar.setProgress(progressBar.getProgress() + 20);
                    UploadFiles();
                }else{
                    //delete the targetDirectory
                    FileHelper.deleteDirectory(targetDirectory);
                    progressBar.setVisibility(View.INVISIBLE);
                    statusText.setText("Sync failed- Either zipping of files or CRC of zipped file failed. Please contact Technical Support.");
                }
            }
        });
    }

    private String getNotesPath() {
        return "/Notes";
    }

    private String getPCNPath() {
        return "/PCNs";
    }

    private String getPCNImagePath() {
        return "/PCNImage";
    }

    private Boolean hasError = false;
    private String successMessage = "";

    private void UploadFiles() {
        successMessage="";
        hasError = false;
        if(CeoApplication.getTransferProtocol().equalsIgnoreCase("SFTP")){
            SFTPUploadFile();
        }
        else{
            FTPUploadFile();
        }
    }


    private void FTPUploadFile(){
        try {
            final FTPClient ftpClient = new FTPClient();
            File dir = cameraImageHelper.getBackupFolder();
            if (dir != null) {
                try {
                    totalFiles = DBHelper.getZipFilesToUpload().size();
                    if (totalFiles > 0)
                        new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] params) {
                                try {
                                    /*DBHelper.deleteTimers();
                                    DBHelper.deleteVirtualPermits();
                                    DBHelper.deleteBreakRecords();*/
                                    DBHelper.removeData();
                                    progressMsg = "Connecting ...";
                                    runOnUiThread(changeMessage);
                                    pct = 50;
                                    runOnUiThread(addProgress);
//                                    try {
                                    boolean isFtpConnected = false;
                                    int i = 1;
                                    String host = "";
                                    do {
                                        switch(i){
                                            case 1 :
                                                host = host1;
                                                break;
                                            case 2 :
                                                host = host2;
                                                break;
                                            case 3 :
                                                host = host3;
                                                break;
                                            case 4 :
                                                host = host4;
                                                break;
                                        }

                                        isFtpConnected = connectFTP(ftpClient, host , 21);
                                        i++;
                                        if(i > 4) break;
                                    }while (!isFtpConnected);

                                        /*try {
                                            ftpClient.connect(host1, 21); // Using port no=21
                                            Log.e("SFTP", "Connected");
                                        } catch (Exception e) {
                                            ftpClient.connect(host2, 21); // Using port no=21
                                        }*/

                                    //if (ftpClient.isConnected()) {
                                    if(isFtpConnected){
                                        ftpClient.login(user, pass);

                                        Log.e("SFTP", "To " + host1);
                                        pct = Math.round(100 / totalFiles + 1) - 1;
                                        progressMsg = "Connected. \n Uploading " + totalFiles + " files";
                                        runOnUiThread(changeMessage);
                                        runOnUiThread(addProgress);

                                        ftpClient.setBufferSize(2048);
                                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                        ftpClient.enterLocalPassiveMode();
                                        int n = 1;
                                        for (BackupTable row : DBHelper.getZipFilesToUpload()) {
                                            File zipFile = new File(row.getBackupName());
                                            FileInputStream zipStream = new FileInputStream(zipFile);
                                            Boolean complete = ftpClient.storeFile(rootfolder + "/" + zipFile.getName(),
                                                    zipStream);
                                            Log.e("SFTP", "Uploading");
                                            if (!complete) {
                                                Log.e("SFTP", "Failed");
                                                row.setBackupDone(0);
                                                progressMsg = "Failed :" + zipFile.getName();
                                                hasError = true;
                                                runOnUiThread(changeMessage);
                                                runOnUiThread(addProgress);

                                            } else {
                                                progressMsg = "Uploaded " + n + " of " + totalFiles + " files";
                                                Log.e("SFTP", "In Progress");
                                                runOnUiThread(changeMessage);
                                                runOnUiThread(addProgress);
                                                n++;
                                                row.setBackDate(DateTime.now().getMillis());
                                                row.setBackupDone(1);
                                            }
                                            row.save();
                                            zipStream.close();

                                        }
                                        ftpClient.disconnect();
                                    } else
                                        successMessage = "Unable to reach server - please contact Technical Support";
                                    /*} catch (Exception e) {
                                        successMessage += e.getMessage() + " Please contact Technical Support";
                                        Log.d("SFTP TRANSFER", e.getMessage());
                                        e.printStackTrace();
                                        try {
                                            CeoApplication.LogError(e.getMessage());
                                            CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        hasError = true;
                                    }*/
                                /*} catch (ArithmeticException aex) {
                                    successMessage = "Nothing to upload";
                                    Log.d("SFTP TRANSFER", aex.getMessage());
                                    try {
                                        CeoApplication.LogError(aex.getMessage());
                                        CeoApplication.LogErrorOnChannel(SupervisorActivity.this,aex, ErrorLocations.location402);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }*/
                                } catch (Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    successMessage += e.getMessage() + " Please contact Technical Support";
                                    Log.d("SFTP TRANSFER", e.getMessage());
                                    e.printStackTrace();
                                    try {
                                        CeoApplication.LogError(e.getMessage());
                                        CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    hasError = true;
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                statusText.setText("");
                                Log.d("SFTP TRANSFER", "FINISHING");
                                progressBar.setVisibility(View.INVISIBLE);
                                if (!hasError) {
                                    //No need to delete the PCN data from database
                                    //DBHelper.removeDayPCNs();
                                    //DBHelper.removeDayPhotos();
//                                    DBHelper.removeData();
                                    spH.clearSharedPrefs(SupervisorActivity.this);
//                                    spH.saveValue("CEO", 0);
                                    successMessage = "Transfer complete, files deleted";
                                    btnSync.setEnabled(false);

                                    SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(SupervisorActivity.this);
                                    Integer lastSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;
                                    sharedPreferenceHelper.saveValue(PCNTable.COL_PCN_SESSION, lastSession);

                                    //sync confirmation
                                    saveSyncInfoRecord();

                                }
                                statusText.setText(successMessage);
                                CeoAndStreetExtracts();
                            }
                        }.execute(null, null, null);
                    else {
                        statusText.setText("Last CEO session uploaded already");
                        CeoAndStreetExtracts();
                    }

                } finally {
                    //disconnect();
                }
            }
        } catch (Exception e) {
            try {
                CeoApplication.LogError(e.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location201);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //OnException(this, e, ErrorLocations.location201);
            setResult(CeoApplication.RESULT_CODE_ERROR);
            finish();
        }
    }

    private void saveSyncInfoRecord(){
        SyncInfoTable syncInfoTable=new SyncInfoTable();
        syncInfoTable.setIMEINumber(CeoApplication.getUUID());
        syncInfoTable.setSent(AppConstant.NOT_SYNC_INFO);
        syncInfoTable.setDateTimeOfSync(AppConstant.ISO8601_DATE_TIME_FORMAT.format(System.currentTimeMillis()));
        syncInfoTable.save();
        PubNubModule.publishSyncConfirmation(AppConstant.SYNC_INFO);
    }

    private SFTPClient sftpClient=null;
    private ChannelSftp sftpChannel=null;

    private void SFTPUploadFile(){
        try {

            File dir = cameraImageHelper.getBackupFolder();
            if (dir != null) {
                try {
                    totalFiles = DBHelper.getZipFilesToUpload().size();
                    if (totalFiles > 0)
                        new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] params) {
                                try {
                                    /*DBHelper.deleteTimers();
                                    DBHelper.deleteVirtualPermits();
                                    DBHelper.deleteBreakRecords();*/
                                    DBHelper.removeData();
                                    progressMsg = "Connecting ...";
                                    runOnUiThread(changeMessage);
                                    pct = 50;
                                    runOnUiThread(addProgress);
//                                    try {
                                    boolean isFtpConnected = false;
                                    int i = 1;
                                    String host = "";
                                    do {
                                        switch(i){
                                            case 1 :
                                                host = host1;
                                                break;
                                            case 2 :
                                                host = host2;
                                                break;
                                            case 3 :
                                                host = host3;
                                                break;
                                            case 4 :
                                                host = host4;
                                                break;
                                        }
                                        try {
                                            sftpClient = new SFTPClient(null, user, pass, host);
                                            sftpChannel = sftpClient.createSFTPChannel();

                                            if(sftpChannel!=null){
                                                isFtpConnected=sftpChannel.isConnected();
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        i++;
                                        if(i > 4) break;
                                    }while (!isFtpConnected);

                                        /*try {
                                            ftpClient.connect(host1, 21); // Using port no=21
                                            Log.e("SFTP", "Connected");
                                        } catch (Exception e) {
                                            ftpClient.connect(host2, 21); // Using port no=21
                                        }*/

                                    //if (ftpClient.isConnected()) {
                                    if(isFtpConnected){
                                        //ftpClient.login(user, pass);

                                        Log.e("SFTP", "To " + host1);
                                        pct = Math.round(100 / totalFiles + 1) - 1;
                                        progressMsg = "Connected. \n Uploading " + totalFiles + " files";
                                        runOnUiThread(changeMessage);
                                        runOnUiThread(addProgress);

                                        /*ftpClient.setBufferSize(2048);
                                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                                        ftpClient.enterLocalPassiveMode();*/
                                        int n = 1;
                                        for (BackupTable row : DBHelper.getZipFilesToUpload()) {
                                            File zipFile = new File(row.getBackupName());
                                            //FileInputStream zipStream = new FileInputStream(zipFile);
                                            // remote, local
                                            /*Boolean complete = ftpClient.storeFile(rootfolder + "/" + zipFile.getName(),
                                                    zipStream);*/

                                            /*new FTPClient().storeFile(rootfolder + "/" + zipFile.getName(),
                                                    zipStream);*/

                                            String target=sftpClient.uploadToFTP(sftpChannel,zipFile.getPath(),rootfolder + "/" + zipFile.getName());

                                            Log.e("SFTP", "Uploading");
                                            if (target.isEmpty()) {
                                                Log.e("SFTP", "Failed");
                                                row.setBackupDone(0);
                                                progressMsg = "Failed :" + zipFile.getName();
                                                hasError = true;
                                                runOnUiThread(changeMessage);
                                                runOnUiThread(addProgress);

                                            } else {
                                                progressMsg = "Uploaded " + n + " of " + totalFiles + " files";
                                                Log.e("SFTP", "In Progress");
                                                runOnUiThread(changeMessage);
                                                runOnUiThread(addProgress);
                                                n++;
                                                row.setBackDate(DateTime.now().getMillis());
                                                row.setBackupDone(1);
                                            }
                                            row.save();
                                            //zipStream.close();

                                        }
                                        sftpClient.releaseConnection(sftpChannel);
                                        //ftpClient.disconnect();
                                    } else{
                                        hasError = true;
                                        showSFTPTimeoutMsg();
                                        successMessage = "Unable to connect to SFTP server.Please contact your administrator.*";
                                    }

                                    /*} catch (Exception e) {
                                        successMessage += e.getMessage() + " Please contact Technical Support";
                                        Log.d("SFTP TRANSFER", e.getMessage());
                                        e.printStackTrace();
                                        try {
                                            CeoApplication.LogError(e.getMessage());
                                            CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        hasError = true;
                                    }*/
                                /*} catch (ArithmeticException aex) {
                                    successMessage = "Nothing to upload";
                                    Log.d("SFTP TRANSFER", aex.getMessage());
                                    try {
                                        CeoApplication.LogError(aex.getMessage());
                                        CeoApplication.LogErrorOnChannel(SupervisorActivity.this,aex, ErrorLocations.location402);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }*/
                                } catch (Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    successMessage += e.getMessage() + " Please contact Technical Support";
                                    Log.d("SFTP TRANSFER", e.getMessage());
                                    e.printStackTrace();
                                    try {
                                        CeoApplication.LogError(e.getMessage());
                                        CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    hasError = true;
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                super.onPostExecute(o);
                                statusText.setText("");
                                Log.d("SFTP TRANSFER", "FINISHING");
                                progressBar.setVisibility(View.INVISIBLE);
                                if (!hasError) {
                                    //No need to delete the PCN data from database
                                    //DBHelper.removeDayPCNs();
                                    //DBHelper.removeDayPhotos();
//                                    DBHelper.removeData();
                                    spH.clearSharedPrefs(SupervisorActivity.this);
//                                    spH.saveValue("CEO", 0);
                                    successMessage = "Transfer complete, files deleted";
                                    btnSync.setEnabled(false);

                                    SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(SupervisorActivity.this);
                                    Integer lastSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;
                                    sharedPreferenceHelper.saveValue(PCNTable.COL_PCN_SESSION, lastSession);
                                    //sync confirmation
                                    saveSyncInfoRecord();
                                }
                                statusText.setText(successMessage);
                                CeoAndStreetExtracts();
                            }
                        }.execute(null, null, null);
                    else {
                        statusText.setText("Last CEO session uploaded already");
                        CeoAndStreetExtracts();
                    }

                } finally {
                    //disconnect();
                }
            }
        } catch (Exception e) {
            try {
                CeoApplication.LogError(e.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location201);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //OnException(this, e, ErrorLocations.location201);
            setResult(CeoApplication.RESULT_CODE_ERROR);
            finish();
        }
    }


    private ExifInterface readExifData(String filename){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exifInterface;
    }

    private void saveExifData(ExifInterface exif, String filePath){
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            exifInterface.setAttribute("Make",  exif.getAttribute("Make"));
            exifInterface.setAttribute("Model", exif.getAttribute("Model"));
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH,  exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, exif.getAttribute(ExifInterface.TAG_DATETIME));
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, exif.getAttribute(ExifInterface.TAG_ORIENTATION));

            //GPS latitude and longitude
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));

            exifInterface.setAttribute("UserComment", exif.getAttribute("UserComment"));

            exifInterface.saveAttributes();
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    private boolean connectFTP(FTPClient ftpClient, String ftpServer, int port){
        try {
            ftpClient.setConnectTimeout(CeoApplication.getFtpConnectionTimeout());
            ftpClient.connect(ftpServer, port);
            if(!ftpClient.isConnected()){
                showFTPTimeoutMsg();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            showFTPTimeoutMsg();
            return false;
        }
        return true;
    }

    private void showFTPTimeoutMsg(){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Unable to connect to FTP service.Please contact your administrator.*", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showSFTPTimeoutMsg(){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Unable to connect to SFTP service.Please contact your administrator.*", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void CheckZeroBytePCNFile(){
        try{
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            File jsonDataPath = cameraImageHelper.getPCNDataFolder();
            File[] jsonDataFiles = jsonDataPath.listFiles();
            for(File jsonDataFile : jsonDataFiles ){
                String pcn = jsonDataFile.getName().split("-")[0];
                if(jsonDataFile.length() == 0){
                    List<PCNTable> pcnTables = DBHelper.GetPCN(pcn);
                    if(pcnTables.size()>0){
                        PCNTable pcnTable = pcnTables.get(0);
                        String pcnJSON = pcnTable.getPcnJSON();
                        Gson gson = new GsonBuilder().create();
                        PCN pcnInfo = gson.fromJson(pcnJSON, PCN.class);
                        if(pcnInfo.issueTime == 0){
                            try {
                                jsonDataFile.delete();
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            continue;
                        }
                        JSONObject tempData = new JSONObject(pcnTable.getPcnOUTJSON());
                        if(tempData==null || tempData.toString().length()==0){
                            PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                            tempData = new JSONObject(outPCN.toJSON());
                            pcnTable.setPcnOUTJSON(tempData.toString());
                            pcnTable.save();
                        }
                        int num = DBHelper.PhotosForPCN(pcnTable.getObservation()).size();
                        tempData.putOpt("numberofphotostaken", num);
                        tempData.putOpt("hhtid", CeoApplication.getUUID());
                        String pcnJsonContent = tempData.toString();
                        OutputStream os = new FileOutputStream(jsonDataFile, true);
                        os.write(pcnJsonContent.getBytes());
                        os.close();
                    }
                }
                boolean fileContentOK = false;
                File jsonDataFileCheck = new File(jsonDataFile.getAbsolutePath());
                if (jsonDataFileCheck.length() != 0) {
                    fileContentOK = true;
                }
                UpdateSyncFileStatus(fileContentOK,pcn);
            }
        } catch (Exception ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private void UpdateSyncFileStatus(boolean syncFile, String pcnNumber) {
        PCNTable finalPcn;
        List<PCNTable> PCNs = DBHelper.GetPCN(pcnNumber);
        if (PCNs.size() > 0) {
            finalPcn = PCNs.get(0);
            finalPcn.setSyncFiles(syncFile);
            finalPcn.save();
        }
    }

    private void BackupFiles() {
        try {
            CheckZeroBytePCNFile();
            fileHelper.CopyDirectory(cameraImageHelper.getPCNDataFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getPCNPhotoFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        try {
            fileHelper.CopyDirectory(cameraImageHelper.getPCNNoteFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getExtraFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getPCNReportFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getPCNErrorFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getRemovalPhotoFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            fileHelper.CopyDirectory(cameraImageHelper.getSingleLookUpsFolder(), targetDirectory);
        } catch (IOException ix) {
            ix.printStackTrace();
            try {
                CeoApplication.LogError(ix.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ix, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private void DeleteFiles() {
        try{
            for (File f : cameraImageHelper.getPCNDataFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getPCNPhotoFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getPCNNoteFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getPCNReportFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getRemovalPhotoFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getLocalDBFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getSingleLookUpsFolder().listFiles())
                f.delete();
            for (File f : cameraImageHelper.getPCNErrorFolder().listFiles()) {
                if (f.isDirectory()) {
                    for (File fi : f.listFiles())
                        fi.delete();
                } else {
                    f.delete();
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
            try {
                CeoApplication.LogError(ex.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,ex, ErrorLocations.location402);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean ZipFiles() {
        Integer n = 0;
        File zipFile = new File(zipFileName);
        while (zipFile.exists()) {
            String target = "-" + String.valueOf(n) + ".";
            String newVal = "-" + String.valueOf(n + 1) + ".";
            zipFileName = zipFileName.replace(target, newVal);
            zipFile = new File(zipFileName);
            n++;
        }
        try {
            File pcnDir = new File(Environment.getExternalStorageDirectory() + "/"
                    + AppConstant.PCN_FOLDER);
            ZIPUtils.zip(pcnDir, zipFile,SupervisorActivity.this,0);

            BackupTable backupTable = new BackupTable();
            backupTable.setBackupName(zipFile.getAbsolutePath());
            backupTable.setBackupDone(0);
            backupTable.setBackDate(0l);
            backupTable.save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CeoApplication.LogError(e.getMessage());
                CeoApplication.LogErrorOnChannel(SupervisorActivity.this,e, ErrorLocations.location402);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            statusText.setText(progressMsg);
        }
    };

    private Runnable addProgress = new Runnable() {
        @Override
        public void run() {
            progressBar.incrementProgressBy(pct);
            if (progressBar.getProgress() == 100) {
                progressBar.setProgress(0);
            }
        }
    };
    //JK
    private void showMessage(final String messageText){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CroutonUtils.error(SupervisorActivity.this, messageText);
            }
        });
    }

    @Override
    public void OnFileTransferUpdate(Integer copiedMb) {

        progressMsg = "Uploaded : " + index + "/" + totalFiles + " Files";
        index++;
        runOnUiThread(changeMessage);
        runOnUiThread(addProgress);
    }

    @Override
    public void OnFileCopied() {

    }

    private void SyncUnSyncedPCNs() {
        try {
            if (CeoApplication.MuleServiceUrl() != null && !CeoApplication.MuleServiceUrl().isEmpty()) {
                new AsyncTask() {
                    @Override
                    protected void onPreExecute() {
                        if(progressDialog!=null) {
                            progressDialog.setMessage("Syncing with service, Please wait...");
                            progressDialog.show();
                        }
                        super.onPreExecute();
                    }

                    @Override
                    protected Object doInBackground(Object[] params) {
                        for (PCNTable pcn : DBHelper.getUnSyncPCNs()) {
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
                                if (PcnOutJSON != null && !PcnOutJSON.isEmpty()) {
                                    JSONObject finalOutJson = new JSONObject("{\"pcn\":" + PcnOutJSON + "}");
                                    CreateTicketToLiberator(finalOutJson);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        if(progressDialog!=null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }

                }.execute(null, null, null);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void CreateTicketToLiberator(JSONObject jsonFileObject) throws Exception {
        String ticketReference = "";
        JSONObject responseObject ;
        try {
            JSONObject pcnObj = (JSONObject) jsonFileObject.get("pcn");
            ticketReference = (String) pcnObj.get("ticketserialnumber");
            String url = CeoApplication.MuleServiceUrl() + URLEncoder.encode(jsonFileObject.toString(), "UTF-8");
            String response = AlfrescoComponent.executeGetRequest(url);
            if (response != null && !response.isEmpty()) {
                responseObject = new JSONObject(response);
                DBHelper.UpdateSyncServiceStatus(responseObject);
            } else {
                responseObject = new JSONObject();
                responseObject.put("message", "No response from server");
                responseObject.put("status", false);
                responseObject.put("pcn", ticketReference);
                DBHelper.UpdateSyncServiceStatus(responseObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObject = new JSONObject();
            responseObject.put("message", e.toString());
            responseObject.put("status", false);
            responseObject.put("pcn", ticketReference);
            DBHelper.UpdateSyncServiceStatus(responseObject);
        }
    }

   /* public void CreateTicketToLiberator(JSONObject jsonFileObject) throws Exception {
        String createResponse = null;
        String ticketReference = "";
        try {
            JSONObject pcnObj = (JSONObject) jsonFileObject.get("pcn");
            ticketReference = (String) pcnObj.get("ticketserialnumber");
            File PdfTemplateFile = new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "IssuedHHTPCN.pdf");
            String outputPdfFileName = PdfTemplateFile.getAbsolutePath();
            File pdfDocument = new File(outputPdfFileName);
            MultipartEntity reqEntity = new MultipartEntity();
            FileBody documentBody = new FileBody(pdfDocument);
            reqEntity.addPart("uploadFile", documentBody);
            reqEntity.addPart("streetnoderef", new StringBody((String) pcnObj.get("streetnoderef")));
            reqEntity.addPart("ceonoderef", new StringBody((String) pcnObj.get("ceonoderef")));
            reqEntity.addPart("ticketserialnumber", new StringBody((String) pcnObj.get("ticketserialnumber")));
            reqEntity.addPart("tickettype", new StringBody((String) pcnObj.get("tickettype")));
            if (pcnObj.has("contraventioncode"))
                reqEntity.addPart("contraventioncode", new StringBody((String) pcnObj.get("contraventioncode")));
            if (pcnObj.has("contraventiondescription"))
                reqEntity.addPart("contraventiondescription", new StringBody((String) pcnObj.get("contraventiondescription")));
            if (pcnObj.has("contraventionsuffix"))
                reqEntity.addPart("contraventionsuffix", new StringBody((String) pcnObj.get("contraventionsuffix")));
            if (pcnObj.has("contraventionchargecode"))
                reqEntity.addPart("contraventionchargecode", new StringBody((String) pcnObj.get("contraventionchargecode")));
            if (pcnObj.has("vrm"))
                reqEntity.addPart("vrm", new StringBody((String) pcnObj.get("vrm")));
            if (pcnObj.has("make"))
                reqEntity.addPart("make", new StringBody((String) pcnObj.get("make")));
            if (pcnObj.has("model"))
                reqEntity.addPart("model", new StringBody((String) pcnObj.get("model")));
            if (pcnObj.has("colour"))
                reqEntity.addPart("colour", new StringBody((String) pcnObj.get("colour")));
            if (pcnObj.has("foreignvehicle"))
                reqEntity.addPart("foreignvehicle", new StringBody((String) pcnObj.get("foreignvehicle")));
            if (pcnObj.has("foreignvehiclecountry"))
                reqEntity.addPart("foreignvehiclecountry", new StringBody((String) pcnObj.get("foreignvehiclecountry")));
            if (pcnObj.has("diplomaticvehicle"))
                reqEntity.addPart("diplomaticvehicle", new StringBody((String) pcnObj.get("diplomaticvehicle")));
            if (pcnObj.has("observationstarts")) {
                String observationstarts = (String) pcnObj.get("observationstarts");
                if (observationstarts.length() > 0) {
                    observationstarts = observationstarts.substring(0, observationstarts.indexOf("."));
                    observationstarts = observationstarts.replace("T", " ");
                    reqEntity.addPart("observationstarts", new StringBody(observationstarts));
                } else {
                    reqEntity.addPart("observationstarts", new StringBody(""));
                }
            }
            if (pcnObj.has("observationends")) {
                String observationends = (String) pcnObj.get("observationends");
                if (observationends.length() > 0) {
                    observationends = observationends.substring(0, observationends.indexOf("."));
                    observationends = observationends.replace("T", " ");
                    reqEntity.addPart("observationends", new StringBody(observationends));
                } else {
                    reqEntity.addPart("observationends", new StringBody(""));
                }
            }
            if (pcnObj.has("taxdiscserialnumber"))
                reqEntity.addPart("taxdiscserialnumber", new StringBody((String) pcnObj.get("taxdiscserialnumber")));
            if (pcnObj.has("taxdiskexpiry"))
                reqEntity.addPart("taxdiskexpiry", new StringBody((String) pcnObj.get("taxdiskexpiry")));
            if (pcnObj.has("panddexpiry")) {
                String panddexpiry = (String) pcnObj.get("panddexpiry");
                if (panddexpiry.length() > 0) {
                    panddexpiry = panddexpiry.substring(0, panddexpiry.indexOf("."));
                    panddexpiry = panddexpiry.replace("T", " ");
                    reqEntity.addPart("panddexpiry", new StringBody(panddexpiry));
                } else {
                    reqEntity.addPart("panddexpiry", new StringBody(""));
                }
            }
            if (pcnObj.has("panddserialnumber"))
                reqEntity.addPart("panddserialnumber", new StringBody((String) pcnObj.get("panddserialnumber")));
            if (pcnObj.has("panddmachine"))
                reqEntity.addPart("panddmachine", new StringBody((String) pcnObj.get("panddmachine")));
            if (pcnObj.has("valvepositionfront"))
                reqEntity.addPart("valvepositionfront", new StringBody((String) pcnObj.get("valvepositionfront")));
            if (pcnObj.has("valvepositionrear"))
                reqEntity.addPart("valvepositionrear", new StringBody((String) pcnObj.get("valvepositionrear")));
            if (pcnObj.has("onorby"))
                reqEntity.addPart("onorby", new StringBody((String) pcnObj.get("onorby")));
            if (pcnObj.has("onorbyjunction"))
                reqEntity.addPart("onorbyjunction", new StringBody((String) pcnObj.get("onorbyjunction")));
            if (pcnObj.has("sideofroad"))
                reqEntity.addPart("sideofroad", new StringBody((String) pcnObj.get("sideofroad")));
            if (pcnObj.has("facing"))
                reqEntity.addPart("facing", new StringBody((String) pcnObj.get("facing")));
            if (pcnObj.has("osoropp"))
                reqEntity.addPart("osoropp", new StringBody((String) pcnObj.get("osoropp")));
            if (pcnObj.has("osoppwhere"))
                reqEntity.addPart("osoppwhere", new StringBody((String) pcnObj.get("osoppwhere")));
            if (pcnObj.has("distancefrom"))
                reqEntity.addPart("distancefrom", new StringBody((String) pcnObj.get("distancefrom")));
            if (pcnObj.has("direction"))
                reqEntity.addPart("direction", new StringBody((String) pcnObj.get("direction")));
            if (pcnObj.has("parkedonfootway"))
                reqEntity.addPart("parkedonfootway", new StringBody((String) pcnObj.get("parkedonfootway")));
            if (pcnObj.has("loadingorunloading"))
                reqEntity.addPart("loadingorunloading", new StringBody((String) pcnObj.get("loadingorunloading")));
            if (pcnObj.has("parkedagainstflow"))
                reqEntity.addPart("parkedagainstflow", new StringBody((String) pcnObj.get("parkedagainstflow")));
            if (pcnObj.has("brokendown"))
                reqEntity.addPart("brokendown", new StringBody((String) pcnObj.get("brokendown")));
            if (pcnObj.has("driverseen"))
                reqEntity.addPart("driverseen", new StringBody((String) pcnObj.get("driverseen")));
            if (pcnObj.has("vehicledrivenaway"))
                reqEntity.addPart("vehicledrivenaway", new StringBody((String) pcnObj.get("vehicledrivenaway")));
            if (pcnObj.has("contraventiondateandtime")) {
                String contraventiondateandtime = (String) pcnObj.get("contraventiondateandtime");
                if (contraventiondateandtime.length() > 0) {
                    contraventiondateandtime = contraventiondateandtime.substring(0, contraventiondateandtime.indexOf("."));
                    contraventiondateandtime = contraventiondateandtime.replace("T", " ");
                    reqEntity.addPart("contraventiondateandtime", new StringBody(contraventiondateandtime));
                } else {
                    reqEntity.addPart("contraventiondateandtime", new StringBody(""));
                }
            }
            if (pcnObj.has("ticketissuedateandtime")) {
                String ticketissuedateandtime = (String) pcnObj.get("ticketissuedateandtime");
                if (ticketissuedateandtime.length() > 0) {
                    ticketissuedateandtime = ticketissuedateandtime.substring(0, ticketissuedateandtime.indexOf("."));
                    ticketissuedateandtime = ticketissuedateandtime.replace("T", " ");
                    reqEntity.addPart("ticketissuedateandtime", new StringBody(ticketissuedateandtime));
                } else {
                    reqEntity.addPart("ticketissuedateandtime", new StringBody(""));
                }
            }
            if (pcnObj.has("methodofissue"))
                reqEntity.addPart("methodofissue", new StringBody((String) pcnObj.get("methodofissue")));
            if (pcnObj.has("numberofphotostaken"))
                reqEntity.addPart("numberofphotostaken", new StringBody((String) pcnObj.get("numberofphotostaken")));
            if (pcnObj.has("ticketnotes"))
                reqEntity.addPart("ticketnotes", new StringBody((String) pcnObj.get("ticketnotes")));
            String response = AlfrescoComponent.executePostRequest(CeoApplication.FG_CREATE_PARKING_TICKET_URL, reqEntity);
            JSONObject responseObject = new JSONObject();
            if (response != null && !"".equals(response)) {
                JSONObject jsonObjectResponse = new JSONObject(response);
                if (jsonObjectResponse.has("parkingTicketDocument") && jsonObjectResponse.get("parkingTicketDocument") != null && !"".equals(jsonObjectResponse.get("parkingTicketDocument"))) {
                    JSONObject jsonObjectResponseNode = (JSONObject) jsonObjectResponse.get("parkingTicketDocument");
                    String targetNodeRef = (String) jsonObjectResponseNode.get("nodeRef");
                    String pcnId = (String) jsonObjectResponseNode.get("pcnId");
                    responseObject.put("message", targetNodeRef);
                    responseObject.put("status", true);
                    responseObject.put("pcn", pcnId);
                    DBHelper.UpdateSyncServiceStatus(responseObject);
                } else {
                    createResponse = (String) jsonObjectResponse.get("msg");
                    responseObject.put("message", createResponse);
                    responseObject.put("status", false);
                    responseObject.put("pcn", ticketReference);
                    DBHelper.UpdateSyncServiceStatus(responseObject);
                }
            } else {
                createResponse = "No response from server";
                responseObject.put("message", createResponse);
                responseObject.put("status", false);
                responseObject.put("pcn", ticketReference);
                DBHelper.UpdateSyncServiceStatus(responseObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
            createResponse = e.toString();
            JSONObject responseObject = new JSONObject();
            responseObject.put("message", createResponse);
            responseObject.put("status", false);
            responseObject.put("pcn", ticketReference);
            DBHelper.UpdateSyncServiceStatus(responseObject);
        }
    }*/

    private void CeoAndStreetExtracts() {
        try {
            new AsyncTask() {
                @Override
                protected void onPreExecute() {
                        progressDialog = new ProgressDialog(SupervisorActivity.this);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("Extracting CEO, street and special vehicle data, Please wait...");
                        progressDialog.show();

                    super.onPreExecute();
                }

                @Override
                protected Object doInBackground(Object[] params) {
                    String missingResponse = "";
                    int count = 0;
                    try {
                        if (CeoApplication.CeoExtractUrl() != null && !CeoApplication.CeoExtractUrl().isEmpty()) {
                            String response = AlfrescoComponent.executeGetRequest(CeoApplication.CeoExtractUrl());
                            if (response != null && !response.isEmpty()) {
                                JSONObject responseObject = new JSONObject(response);
                                JSONArray responseArray = responseObject.getJSONArray("ceos");
                                File ceoDataFile = getDataFile("ceos.json");
                                SaveToFile(ceoDataFile, responseArray.toString());
                            }else{
                                missingResponse += ++count +". CEO extract url \n";
                            }
                        }
                        if (CeoApplication.StreetExtractUrl() != null && !CeoApplication.StreetExtractUrl().isEmpty()) {
                            String response = AlfrescoComponent.executeGetRequest(CeoApplication.StreetExtractUrl());
                            if (response != null && !response.isEmpty()) {
                                File streetDataFile = getDataFile("streetindex.json");
                                SaveToFile(streetDataFile, response);
                                //Reducing location popup data load time
                                initLogLocationDataList("locationdataindex.json");
                            }else{
                                missingResponse += ++count +". Street extract url \n";
                            }
                        }
                        if (CeoApplication.MinimumObservationTimesUrl() != null && !CeoApplication.MinimumObservationTimesUrl().isEmpty()) {
                            String response = AlfrescoComponent.executeGetRequest(CeoApplication.MinimumObservationTimesUrl());
                            if (response != null && !response.isEmpty()) {
                                JSONObject responseObject = new JSONObject(response);
                                JSONObject responseContent = responseObject.getJSONObject("obs");
                                File minimumObservationTimesDataFile = getDataFile("minimumobservationtimes.json");
                                SaveToFile(minimumObservationTimesDataFile, responseContent.toString());
                            }else{
                                missingResponse += ++count +". Observation extract url \n";
                            }
                        }
                        //No need to maintain the JSON ticket numbers file at all.
                        int availablePCNs = DBHelper.getAvailableTicketNumbers();
                        if(availablePCNs < CeoApplication.AvailablePCNToAlert()) {
                            loadMessage = "Adding new ticket numbers to the ticket book, Please wait...";
                            runOnUiThread(updateMessage);
                            if (CeoApplication.NewTicketNumbersUrl() != null && !CeoApplication.NewTicketNumbersUrl().isEmpty()) {
                                String response = AlfrescoComponent.executeGetRequest(CeoApplication.NewTicketNumbersUrl() + "?hhtid=" + CeoApplication.getUUID());
                                if (response != null && !response.isEmpty()) {
                                    addNewTicketNumbers(response);
                                }else{
                                    missingResponse += ++count +". Ticket Book extract url \n";
                                }
                            }
                        }

                        if (CeoApplication.getSpecialVehicleUrl() != null && !CeoApplication.getSpecialVehicleUrl().isEmpty()) {
                            String response = AlfrescoComponent.executeGetRequest(CeoApplication.getSpecialVehicleUrl() + "?imei=" + CeoApplication.getUUID() + "&restrictionDate=" + DateUtils.getISO8601DateTime());
                            if (response != null && !response.isEmpty()) {
                                JSONObject responseObject = new JSONObject(response);
                                JSONArray responseArray = responseObject.getJSONArray("specialVehicles");
                                File specialVehiclesData = getDataFile("specialVehicles.json");
                                SaveToFile(specialVehiclesData, responseArray.toString());
                            }else{
                                missingResponse += ++count +". Special vehicle extract url \n";
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return missingResponse;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    if(progressDialog!=null){
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    String response = (String)o;
                    if(!response.isEmpty()) {
                        response = "Liberator was unable to access the required URLs\n" + response + "Please contact your technical support.";
                        Utils.showDialog(SupervisorActivity.this, response, "Service response error");
                    }
                }

            }.execute(null, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Runnable updateMessage = new Runnable() {
        @Override
        public void run()
        {
            if(progressDialog!=null){
            progressDialog.setMessage(loadMessage);}
        }
    };

    private void addNewTicketNumbers(String content) {
        try {
            List<TicketNoTable> availableTicketNos = DBHelper.exportTickets();
            JSONArray ticketArray = new JSONObject(content).getJSONArray("ticketNumbers");
            for (int ind = 0; ind < ticketArray.length(); ind++) {
                boolean found = false;
                String ticketReference = ticketArray.getJSONObject(ind).getString("ticketReference");
                for (TicketNoTable ticketNoTable : availableTicketNos){
                    if(ticketReference.equalsIgnoreCase(ticketNoTable.getTicketReference())){
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    TicketNoTable tt = new TicketNoTable();
                    tt.setTicketReference(ticketReference);
                    tt.save();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Reducing location popup data load time
    public void initLogLocationDataList(String logLocationFileName) {
        try {
            String json = getDataFileContent("streetindex.json");
            if (json != null && !json.isEmpty()) {
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    ArrayList<String> locations = new ArrayList<String>();
                    for (int index = 0; index < jsonArray.length(); index++) {
                        JSONObject inJSON = jsonArray.getJSONObject(index);
                        String streetName = inJSON.getString("streetname");
                        if (streetName != null && !streetName.isEmpty())
                            locations.add(streetName);
                    }
                    JSONArray locationArray = new JSONArray(locations);
                    File logLocationFile = getDataFile(logLocationFileName);
                    SaveToFile(logLocationFile, locationArray.toString());

                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.getStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getDataFileContent(String name) throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + File.separator + AppConstant.CONFIG_FOLDER + name);
        InputStream json = new FileInputStream(file);
        return StringUtil.getStringFromInputStream(json);
    }

    private File getDataFile(String name) throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        return new File(root + File.separator + AppConstant.CONFIG_FOLDER + name);
    }

    private void SaveToFile(File fileName, String fileData) {
        try {
            Writer writer =  new BufferedWriter(new FileWriter(fileName));
            writer.write(fileData);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SupervisorActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }


}
