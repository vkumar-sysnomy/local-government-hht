package com.farthestgate.android.ui.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.ResourceHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.Ceo;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.StreetCPZ;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.model.database.TicketNoTable;
import com.farthestgate.android.model.database.VehicleMakeTable;
import com.farthestgate.android.model.database.VehicleModelTable;
import com.farthestgate.android.model.database.VersionTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.ui.pcn.NFCBluetoothActivityBase;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.FirebaseAnalyticsController;
import com.farthestgate.android.utils.Log;
import com.farthestgate.android.utils.NFCTagUtil;
import com.farthestgate.android.utils.NfcForegroundUtil;
import com.farthestgate.android.utils.SimpleSHA256;
import com.farthestgate.android.utils.StringUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginActivity extends NFCBluetoothActivityBase
{
    public static final String TAG = LoginActivity.class.getSimpleName();
    public static final String CONFIG_PATH_ASSETS = "configdata";
    public static final String CONFIG_PATH_ROOT = "ceoappdata";
    public static final String CONFIG_PATH_NOTES = "pcns/notes";
    public static final String CONFIG_PATH_PCNS = "pcnnumbers";
    public static final String PCN_FILE_PATH = Environment.getExternalStorageDirectory() + "/" + CONFIG_PATH_ROOT + "/" + CONFIG_PATH_PCNS;

    private Gson gson;
    private TextView    versionText;
    private TextView    errorText;
    private EditText    passwordEditText;
    private Button      loginButton;
    private ProgressBar progressBar;
    private TextView    progressText;
    private ImageView   clientLogo;
    private TextView    clientLogoText;

    private AutoCompleteTextView ceoEditText;
    private static String loadMessage = "";
    private static String errorMessages = "";

    private String id;
    private boolean backupChecked = false;
    private Spinner loginCeoRolesSpn;
    private boolean ceoRolesFileFound = false;
    private ArrayList<String> missedFiles= new ArrayList<String>();
    ProgressDialog  progressDialog;

    private static final int REQUEST_WRITE_PERMISSION = 1001;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseCrashlytics mCrashlytics;
    private static final String WELCOME_MESSAGE_KEY = "virtual_permits_url";
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CeoApplication.GetConfigDetails();
            loadData();
        }
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,Manifest.permission.CAMERA
            }, REQUEST_WRITE_PERMISSION);
        } else {
            CeoApplication.GetConfigDetails();
            loadData();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_login);
        requestPermission();
        //int a=1/0;
        //FirebaseCrashlytics.getInstance().recordException(new Exception("hi this is the main function exception "));
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);


       // Log the onCreate event, this will also be printed in logcat
        /*mCrashlytics = FirebaseCrashlytics.getInstance();
        mCrashlytics.log("onCreate");
        // Add some custom values and identifiers to be included in crash reports
        mCrashlytics.setCustomKey("MeaningOfLife", 42);
        mCrashlytics.setCustomKey("LastUIAction", "Test value");
        mCrashlytics.setUserId("123456789");
        mCrashlytics.recordException(new Exception("Non-fatal exception: something went wrong!"));

*/
       /* mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Report a non-fatal exception, for demonstration purposes
        mFirebaseAnalytics.setCurrentScreen(this, "first screen-login screen", null *//* class override *//*);
*/

       /* Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "001");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,  "login screen" );
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Authentication");
        mFirebaseAnalytics.logEvent("hjvbjvjv", bundle);*/

        gson = new GsonBuilder().create();
        loginCeoRolesSpn = (Spinner)findViewById(R.id.login_ceo_roles_spn);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar);
        loginButton     = (Button) findViewById(R.id.login_button);
        progressText    = (TextView) findViewById(R.id.lblProgress);
        ceoEditText     = (AutoCompleteTextView) findViewById(R.id.login_ceo_id_edit_text);
        passwordEditText= (EditText) findViewById(R.id.login_ceo_password_edit_text);
        versionText     = (TextView) findViewById(R.id.txtVersion);
        errorText       = (TextView) findViewById(R.id.txtErrors);
        clientLogo      = (ImageView) findViewById(R.id.imageView);
        clientLogoText  = (TextView) findViewById(R.id.textView);


        fetchRemoteConfigData();
    }

    private void loadData(){
        clientLogoText.setText(CeoApplication.ClientLogoText());
        ceoEditText.setText(CeoApplication.PcnPrefix());
        File logo = new File (Environment.getExternalStorageDirectory() + File.separator + LoginActivity.CONFIG_PATH_ROOT + File.separator + "logo.png");
        if(logo.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(logo.getAbsolutePath());
            clientLogo.setImageBitmap(bitmap);
        }
        if(CeoApplication.RoleSelectionAtLogin()) {
            loginCeoRolesSpn.setVisibility(View.VISIBLE);
            try {
               
                File ceoRolesJsonFile =  new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "ceo-roles.json");
                if (ceoRolesJsonFile.exists()) {
                    ceoRolesFileFound = true;
                    InputStream fileStream = new FileInputStream(ceoRolesJsonFile);
                    JSONObject ceoRolesJson = new JSONObject(StringUtil.getStringFromInputStream(fileStream));
                    ArrayList<String> roles = new ArrayList<String>();
                    roles.add(CeoApplication.SelectOneText());
                    JSONArray ceoRoles = ceoRolesJson.getJSONArray("roles");
                    for (int i=0;i<ceoRoles.length();i++){
                        roles.add((String)ceoRoles.get(i));
                    }
                    ArrayAdapter<String> rolesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roles);
                    rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    loginCeoRolesSpn.setAdapter(rolesAdapter);
                } else {
                    ceoRolesFileFound = false;
                }

            } catch (Exception ex) {
                ceoRolesFileFound = false;
                ex.printStackTrace();

            }
        }else{
            loginCeoRolesSpn.setVisibility(View.GONE);
        }

        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nfcForegroundUtil = new NfcForegroundUtil(this);

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    onLogin();
                return false;
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String s = arg0.toString();
                if (!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    passwordEditText.setText("");
                    passwordEditText.append(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        ceoEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    passwordEditText.requestFocus();
                    passwordEditText.setSelection(0, passwordEditText.length());
                }

                return false;
            }
        });

        ceoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String s=arg0.toString();
                if(!s.equals(s.toUpperCase()))
                {
                    s=s.toUpperCase();
                    ceoEditText.setText("");
                    ceoEditText.append(s);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        if (packageInfo != null)
            versionText.setText("Version :" + packageInfo.versionName + "." + packageInfo.versionCode);
        else
            versionText.setText("");

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

               onLogin();
            }
        });
        loginButton.setEnabled(false);
        progressText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        ceoEditText.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                InputMethodManager inputManager = (InputMethodManager) LoginActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        errorMessages = "";
        if (CeoApplication.configFileMissing) {
            errorMessages = "Missing configuration file. Please contact supervisor immediately.\n";
        }
        if (CeoApplication.configurationMissing) {
            errorMessages += "PubNub configuration missing. Please contact supervisor immediately.\n";
        }

        if (!backupChecked) {
            PrettyTime prettyTime = new PrettyTime();
            long lastBackup = DBHelper.GetLastBackup();

            if (lastBackup > 0)
                errorMessages += "Last backup performed :" + prettyTime.format(new Date(lastBackup));
            else
                errorMessages += "This device has not been backed up yet";

            backupChecked = true;
        }

        runOnUiThread(logErrorMessage);

        checkRequiredFiles();

        String missedUrls = checkRequiredUrls();
        if(!missedUrls.isEmpty()){
            Utils.showExitDialog(LoginActivity.this, missedUrls, "Missing service URLs");
        }else {
            boolean masterFilesMissing = false;
            String missingFiles = "";
            if (!CeoApplication.IsDataFileExist("ceos.json")) {
                missingFiles += String.valueOf(missedFiles.size() + 1) + ". ceos.json\n";
                missedFiles.add("ceos.json");
                masterFilesMissing = true;
            }
            if (!CeoApplication.IsDataFileExist("streetindex.json")) {
                missingFiles += String.valueOf(missedFiles.size() + 1) + ". streetindex.json\n";
                missedFiles.add("streetindex.json");
                masterFilesMissing = true;
            }
            if (!CeoApplication.IsDataFileExist("minimumobservationtimes.json")) {
                missingFiles += String.valueOf(missedFiles.size() + 1) + ". minimumobservationtimes.json\n";
                missedFiles.add("minimumobservationtimes.json");
                masterFilesMissing = true;
            }

            if (masterFilesMissing) {
                AlertDialog missingFilesDialog = new AlertDialog.Builder(this).create();
                missingFilesDialog.setCancelable(false);
                missingFilesDialog.setTitle("Required file(s) missing");
                missingFilesDialog.setMessage("Following file(s) is/are missing from device.\n" + missingFiles + "Do you want to download the file(s) to the device?");
                missingFilesDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ExtractMissingFiles();
                    }
                });
                missingFilesDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
                if (!missingFilesDialog.isShowing()) missingFilesDialog.show();
            } else {
                createMasterData();
            }
        }

        if(CeoApplication.getUUID()==null||CeoApplication.getUUID().isEmpty()){

            Utils.showExitDialog(this, "Your device is not registered with Liberator, please contact your supervisor.","");
        }
    }

    private void createMasterData(){
        new AsyncTask(){

            @Override
            protected void onPreExecute()
            {
                loadMessage = "Loading Credentials...";
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object[] params)
            {
                //IsNewVersion() ||
                if (!hasDatabaseData())
                {
                    SetNewVersion();
                    //No need to load the ceo details in database.
                    //Read it from the file directly

                    /*loadMessage = "Loading CEO's...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initCeoCredentials(CONFIG_PATH_ASSETS);*/

                    loadMessage = "Loading Contraventions...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initContraventionCodes(CONFIG_PATH_ASSETS);

                    //No need to load the street details in database.
                    //Read it from the file directly

                    /*runOnUiThread(addProgress);
                    loadMessage = "Loading Streets...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initStreetList();*/

                    runOnUiThread(addProgress);
                    loadMessage = "Loading Vehicle Makes and Models...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initVehicleMakesModels();
                }
                //No need to maintain the JSON ticket numbers file at all.
                /*if (!hasTicketData())
                {
                    loadMessage = "Loading PCN's...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initPCNNumbers(CONFIG_PATH_PCNS);
                }*/

                //Reducing location popup data load time
                if(!CeoApplication.IsDataFileExist("locationdataindex.json")){
                    loadMessage = "Creating location data file...";
                    runOnUiThread(changeMessage);
                    runOnUiThread(addProgress);
                    initLogLocationDataList("locationdataindex.json");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                progressText.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(true);
                loginButton.setVisibility(View.VISIBLE);
                List<String> ceoNums = DBHelper.getCeoNumbers();
                ArrayAdapter<String> ceoAdapter = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, ceoNums);
                ceoEditText.setAdapter(ceoAdapter);

                super.onPostExecute(o);
            }
        }.execute(null, null, null);
    }

    private void ExtractMissingFiles() {
        try {
            new AsyncTask() {
                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Downloading required file(s) – Please wait");
                    progressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected Object doInBackground(Object[] params) {
                    String missingResponse = "";
                    try {
                        int count = 0;
                        for (String fileName : missedFiles) {
                            if(fileName.equalsIgnoreCase("ceos.json")) {
                                if (CeoApplication.CeoExtractUrl() != null && !CeoApplication.CeoExtractUrl().isEmpty()) {
                                    String response = AlfrescoComponent.executeGetRequest(CeoApplication.CeoExtractUrl());
                                    if (response != null && !response.isEmpty()) {
                                        JSONObject responseObject = new JSONObject(response);
                                        JSONArray responseArray = responseObject.getJSONArray("ceos");
                                        File ceoDataFile = getDataFilePath("ceos.json");
                                        SaveToFile(ceoDataFile, responseArray.toString());
                                    }else{
                                        missingResponse += ++count +". CEO extract url \n";
                                    }
                                }
                            }
                            if(fileName.equalsIgnoreCase("streetindex.json")) {
                                if (CeoApplication.StreetExtractUrl() != null && !CeoApplication.StreetExtractUrl().isEmpty()) {
                                    String response = AlfrescoComponent.executeGetRequest(CeoApplication.StreetExtractUrl());
                                    if (response != null && !response.isEmpty()) {
                                        File streetDataFile = getDataFilePath("streetindex.json");
                                        SaveToFile(streetDataFile, response);
                                        //Reducing location popup data load time
                                        File locationDataFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + "locationdataindex.json");
                                        if(locationDataFile.exists())locationDataFile.delete();
                                    }else{
                                        missingResponse += ++count +". Street extract url \n";
                                    }
                                }
                            }
                            if(fileName.equalsIgnoreCase("minimumobservationtimes.json")) {
                                if (CeoApplication.MinimumObservationTimesUrl() != null && !CeoApplication.MinimumObservationTimesUrl().isEmpty()) {
                                    String response = AlfrescoComponent.executeGetRequest(CeoApplication.MinimumObservationTimesUrl());
                                    if (response != null && !response.isEmpty()) {
                                        JSONObject responseObject = new JSONObject(response);
                                        JSONObject responseContent = responseObject.getJSONObject("obs");
                                        File minimumObservationTimesDataFile = getDataFilePath("minimumobservationtimes.json");
                                        SaveToFile(minimumObservationTimesDataFile, responseContent.toString());
                                    }else{
                                        missingResponse += ++count +". Observation extract url \n";
                                    }
                                }
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
                    progressDialog.dismiss();
                    progressDialog = null;
                    String response = (String)o;
                    if(!response.isEmpty()) {
                        response = "Liberator was unable to access the required URLs\n" + response + "Please contact your supervisor immediately.";
                        Utils.showExitDialog(LoginActivity.this, response, "Service response error");
                    }else {
                        createMasterData();
                    }
                }

            }.execute(null, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private File getDataFilePath(String name) throws IOException {
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

    private void checkPCNPhotos()
    {

    }


    @Override
    public void onNewIntent(Intent intent) {


        NdefMessage[] msgs = NFCTagUtil.getNdefMessages(intent.getAction(), intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));

        if (msgs != null) {
            openUploadActivity();
            backupChecked = false;
        }
    }

    private void onLogin()
    {
        try {




            if (validateForm()) {
                /**
                 * Log the ceo login details
                 */
                AnalyticsUtils.trackLogin();
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                String roleSelected = CeoApplication.RoleSelectionAtLogin() && ceoRolesFileFound ? (String) loginCeoRolesSpn.getSelectedItem() : "NOTINUSE";
                //CeoApplication.ceoRole = roleSelected;

                DBHelper.SaveCeoLoginDetails(id, CeoApplication.getUUID(), roleSelected);


                /**
                 *  If a different CEO has logged in delete the previous PCN's
                 */

                if (!sharedPreferenceHelper.getValue(LAST_CEO, String.class, "").equals(id))
                    sharedPreferenceHelper.clearSharedPrefs(this);

                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(CeoApplication.getContext());
                sharedPreferenceHelper.saveValue("ceo",CeoApplication.CEOLoggedIn);
                sharedPreferenceHelper.saveString(AppConstant.CEO_ROLE,roleSelected);
                PubNubModule.publishSyncConfirmation(AppConstant.SYNC_INFO_ON_LOGIN);

                //0012,0001,Supervisor,TEST
                if (id.equalsIgnoreCase("Supervisor")) {
                    openUploadActivity();
                } else {
                    //CeoApplication.ceoLoginTime = new Date();
                    sharedPreferenceHelper.saveLong(AppConstant.LOGIN_TIME,new Date().getTime());
                    LogLocationForLogin();
                    if (sharedPreferenceHelper.getValue("StartOfDay", Boolean.class, false))
                        //Application flow changed after conversation with Simon
                        //openPCNListActivity();
                        openStartDayActivity();
                    else
                        openStartDayActivity();
                }
                finish();
            }
       }
        catch (Exception exc)
        {
            OnException(this, exc, ErrorLocations.location202);
            CroutonUtils.error(this,"An error occurred when logging in, please restart the app and try again");
        }
    }

    private void LogLocationForLogin() {
        LocationLogTable logEntry = new LocationLogTable();
        logEntry.setLogTime(new DateTime());
        logEntry.setCeoName(CeoApplication.CEOLoggedIn.userId);
        logEntry.setCeoLoginTime(sharedPreferenceHelper.getLong(AppConstant.LOGIN_TIME)/*CeoApplication.ceoLoginTime.getTime()*/);
        logEntry.save();
    }

    private boolean validateForm() {
        try
        {
            id = ceoEditText.getText().toString();
            if (id.isEmpty()) return false;

            String password = passwordEditText.getText().toString();
            if (password.isEmpty()) return false;

            if(CeoApplication.RoleSelectionAtLogin() && ceoRolesFileFound){
                String selectedRole = (String)loginCeoRolesSpn.getSelectedItem();
                if(selectedRole.equalsIgnoreCase(CeoApplication.SelectOneText())){
                    CroutonUtils.error(CroutonUtils.DURATION_SHORT, this, "Select a role");
                    return false;
                }
            }

            if (id.equalsIgnoreCase("Supervisor"))
            {
                if (password.equals("FARTHESTGATE"))
                {
                    return true;
                }
            }

            //No need to load the ceo details in database.
            //Read it from the file directly
            //CeoApplication.CEOLoggedIn = DBHelper.getCeoInfo(id).get(0);
            JSONArray ceoFileContent = CeoApplication.GetDataFileContent("ceos.json");
            if(ceoFileContent !=null){
                for(int index=0; index<ceoFileContent.length();index++){
                    JSONObject jsonCeo = ceoFileContent.getJSONObject(index);
                    if(jsonCeo.getString("userId").equalsIgnoreCase(id)){
                        CeoApplication.CEOLoggedIn = gson.fromJson(jsonCeo.toString(), Ceo.class);
                        break;
                    }
                }
            }

            try {
                String test = SimpleSHA256.SHA256(password);
                if (CeoApplication.CEOLoggedIn != null && CeoApplication.CEOLoggedIn.userId.equals(id) && true) {
                    return true;
                } else {
                    CroutonUtils.error(CroutonUtils.DURATION_SHORT, this, "Incorrect Password");
                    return false;
                }
            } catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            CroutonUtils.error(this,"Incorrect Login");
        }
        return false;

    }

    private void openPCNListActivity() {

        startActivity(new Intent(this, VisualPCNListActivity.class));
    }

    private void openUploadActivity() {
        startActivityForResult(new Intent(this, SupervisorActivity.class), 11);
        PrettyTime prettyTime = new PrettyTime();
        long lastBackup = DBHelper.GetLastBackup();

        errorMessages = "";
        if (CeoApplication.configFileMissing) {
            errorMessages = "Missing configuration file. Please contact supervisor immediately.\n";
        }
        if (CeoApplication.configurationMissing) {
            errorMessages += "PubNub configuration missing. Please contact supervisor immediately.\n";
        }
        if (lastBackup > 0)
            errorMessages += "Last backup performed :" + prettyTime.format(new Date(lastBackup));
        else
            errorMessages += "This device has not been backed up yet";

        runOnUiThread(logErrorMessage);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CeoApplication.RESULT_CODE_ERROR)
        {
            CroutonUtils.error(this,"An error occurred, please try again");
        }

    }

    private void openStartDayActivity() {

        Intent ceoIntent = new Intent(this, StartDayActivity.class);
        ceoIntent.putExtra("CEO",ceoEditText.getText().toString());
        startActivity(ceoIntent);
        finish();
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run()
        {
            progressText.setText(loadMessage);
        }
    };

    private Runnable logErrorMessage = new Runnable() {
        @Override
        public void run()
        {
            errorText.setText(errorMessages);
        }
    };

    private Runnable addProgress = new Runnable() {
        @Override
        public void run()
        {
            progressBar.incrementProgressBy(20);
        }
    };

    @Override
    protected void onResume()
    {

        Log.e("last backup" + String.valueOf(DBHelper.GetLastBackup()));


        super.onResume();
        if(backupChecked && errorMessages.isEmpty()){
            backupChecked=false;
        }else{
            errorMessages = "";
        }

        if (CeoApplication.configFileMissing) {
            errorMessages = "Missing configuration file. Please contact supervisor immediately.\n";
        }
        if (CeoApplication.configurationMissing) {
            errorMessages += "PubNub configuration missing. Please contact supervisor immediately.\n";
        }
        nfcForegroundUtil.enableForeground();
        if (!backupChecked) {
            PrettyTime prettyTime = new PrettyTime();
            long lastBackup = DBHelper.GetLastBackup();
            if (lastBackup > 0)
                errorMessages += "Last backup performed :" + prettyTime.format(new Date(lastBackup));
            else
                errorMessages += "This device has not been backed up yet";

            backupChecked = true;
            runOnUiThread(logErrorMessage);
        }

    }

    /**
     *
     * This only checks the street table
     *
     * @return Boolean
     */
    private Boolean hasDatabaseData()
    {
        return  DBHelper.hasData();
    };

    private Boolean hasTicketData()
    {
        return DBHelper.hasTicketNoData();
    }

    /**
     *  Not in use anymore since the tables
     *  get flushed each day
     */

  /*  private Boolean IsNewVersion()
    {
        VersionTable versionInfo = null;
        try
        {
            versionInfo = DBHelper.getVersion().get(0);
        }
        catch (IndexOutOfBoundsException ex)
        {
            SetNewVersion();
            versionInfo = DBHelper.getVersion().get(0);
            return false;
        }
        catch (Exception e)
        {
            return false;
        }

        if (versionInfo != null)
        {
            Boolean res = false;
            res = !versionInfo.getVersionNumber().equals(packageInfo.versionName);
            return res || versionInfo.getBuildNumber() != packageInfo.versionCode;
        }
        else
            return true;
    }*/

    private void SetNewVersion()
    {
        VersionTable versionTable = new VersionTable();
        versionTable.setBuildNumber(packageInfo.versionCode);
        versionTable.setVersionNumber(packageInfo.versionName);
        versionTable.save();
    }

    private String[] getAssetsList(String path) throws IOException
    {

        String[] list = new String[0];
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root + File.separator + CONFIG_PATH_ROOT + File.separator + path);

        return dir.list();
    }

    private String getDataFile(String path) throws IOException {


        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + File.separator + CONFIG_PATH_ROOT + File.separator+ path);

        InputStream json = new FileInputStream(file);

        return StringUtil.getStringFromInputStream(json);
    }

    public void initCeoCredentials(String path) {
        try {

            String fileName = "ceos";
            String json = getDataFile(path + File.separator + fileName + ".json");

            try
            {
                JSONArray JSONCEOS = new JSONArray(json);
                for (int idx = 0;idx < JSONCEOS.length();idx++)
                {
                    Ceo objCeo = gson.fromJson(JSONCEOS.getJSONObject(idx).toString(), Ceo.class);
                    DBHelper.saveCeos(objCeo);
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            errorMessages += "CEO file not found \n";
            runOnUiThread(logErrorMessage);
        }
        Ceo objCeo = new Ceo();
        objCeo.userId = "SUPERVISOR";
        objCeo.noderef = "noderef://SUPERVISOR999999999";
        objCeo.hash = "";
        DBHelper.saveCeos(objCeo);
    }

    public void initPCNNumbers(String path) {
        try {
            String[] list = getAssetsList(path);
            if (list != null) {
                String file = list[0]; //TODO : this may need changing
                String json = getDataFile(path + File.separator + file);
                // CeoApplication.pcnFileHeader = json.substring(0,json.indexOf("["));
                if (json != null) {
                    try {
                        JSONArray ticketArray = new JSONObject(json).getJSONArray("ticketNumbers");
                        for (int ind = 0; ind < ticketArray.length(); ind++) {
                            if (ticketArray.getJSONObject(ind).getString("dateUsed") == null || ticketArray.getJSONObject(ind).getString("dateUsed").length() == 0) {
                                TicketNoTable tt = new TicketNoTable();
                                tt.setTicketReference(ticketArray.getJSONObject(ind).getString("ticketReference"));
                                tt.save();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessages += "PCN file not found \n";
            runOnUiThread(logErrorMessage);
        }
    }

    public void initStreetList() {
        try {
            String path = CONFIG_PATH_ASSETS;
            String json = getDataFile(path + File.separator + "streetindex.json");
            if (json != null) {
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    for (int y = 0; y < jsonArray.length(); y++) {
                        StreetCPZ street = new StreetCPZ(jsonArray.getJSONObject(y));

                        if (street != null)
                            DBHelper.saveStreet(street);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.getStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessages += "Street index file not found \n";
            runOnUiThread(logErrorMessage);
        }
    }
    //Reducing location popup data load time
    public void initLogLocationDataList(String logLocationFileName) {
        try {
            String path = CONFIG_PATH_ASSETS;
            String json = getDataFile(path + File.separator + "streetindex.json");
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
                    File logLocationFile = getDataFilePath(logLocationFileName);
                    SaveToFile(logLocationFile, locationArray.toString());

                } catch (JSONException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.getStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessages += "Street index file not found \n";
            runOnUiThread(logErrorMessage);
        }
    }

    private List<String> makeList;

    private Boolean hasMakeOther = false;

    public void initVehicleMakesModels() {

        Integer index = 0;
        try {
            makeList = ResourceHelper.getVehicleMakes();

            Boolean otherExists;
            for (String makeName: makeList)
            {
                otherExists = false;
                VehicleMakeTable vTabRow = new VehicleMakeTable();
                vTabRow.vehicleMakeID = index;
                vTabRow.vehicleMakeName = makeName;
                vTabRow.save();
                if (makeName.toLowerCase().contains("other"))
                    hasMakeOther = true;

                try
                {
                    List<String> models = ResourceHelper.getManufacturerModels(makeName.toLowerCase().replace(" ","_").replace("-","_"));
                    if (models.size() > 0)
                    {
                        for (String md:models)
                        {
                            if (md.toLowerCase().contains("other"))
                                otherExists = true;
                            VehicleModelTable vehicleModelTable = new VehicleModelTable();
                            vehicleModelTable.modelMakeID = index;
                            vehicleModelTable.modelName = md;
                            vehicleModelTable.save();
                        }
                    }
                    if (!otherExists)
                    {
                        VehicleModelTable vehicleModelTable = new VehicleModelTable();
                        vehicleModelTable.modelMakeID = index;
                        vehicleModelTable.modelName = "Other";
                        vehicleModelTable.save();
                        otherExists = true;
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                index ++;
            }

            if (!hasMakeOther)
            {
                VehicleMakeTable vTabRow = new VehicleMakeTable();
                vTabRow.vehicleMakeID = index;
                vTabRow.vehicleMakeName = "Other";
                vTabRow.save();

                VehicleModelTable vehicleModelTable = new VehicleModelTable();
                vehicleModelTable.modelMakeID = index;
                vehicleModelTable.modelName = "Other";
                vehicleModelTable.save();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorMessages += "Make or Model file not found \n";
            runOnUiThread(logErrorMessage);
        }
    }

    public void initContraventionCodes(String path) {
        try {
            String fileName = "contraventions";
            String json = getDataFile(path + File.separator + fileName + ".json");

            try
            {
                JSONArray contraventionArray = new JSONArray(json);
                for (int x =0;x < contraventionArray.length();x++)
                {
                    try
                    {
                        JSONObject res = contraventionArray.getJSONObject(x);

                        Contravention contra = new Contravention();
                        contra.codeSuffixes = res.getString("Suffixes");
                        contra.contraventionType = res.getInt("enforcementType");
                        contra.contraventionCode = res.getString("contraventionCode");
                        contra.contraventionDescription = res.getString("contraventionDescription");
                        contra.contraventionCode = contra.contraventionCode.replace(".0","");
                        if (Integer.parseInt(contra.contraventionCode) < 10)
                            contra.contraventionCode = "0" + contra.contraventionCode;
                        DBHelper.saveContraventions(contra);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
            errorMessages += "Contraventions file not found \n";
            runOnUiThread(logErrorMessage);
        }
    }

    private String checkRequiredUrls() {
        String missingURLs = "";
        int count =0;
        if (CeoApplication.CeoExtractUrl() == null || CeoApplication.CeoExtractUrl().isEmpty()) {
            missingURLs += ++count +". CEO extract url\n";
        }
        if (CeoApplication.StreetExtractUrl() == null || CeoApplication.StreetExtractUrl().isEmpty()) {
            missingURLs += ++count +". Street extract url\n";
        }
        if (CeoApplication.MinimumObservationTimesUrl() == null || CeoApplication.MinimumObservationTimesUrl().isEmpty()) {
            missingURLs += ++count +". Observation extract url\n";
        }
        if (CeoApplication.NewTicketNumbersUrl() == null || CeoApplication.NewTicketNumbersUrl().isEmpty()) {
            missingURLs += ++count +". Ticket Book extract url\n";
        }
        if (CeoApplication.MuleServiceUrl() == null || CeoApplication.MuleServiceUrl().isEmpty()) {
            missingURLs += ++count +". Direct ticket interface url\n";
        }
        if (CeoApplication.VirtualPermitsUrl() == null || CeoApplication.VirtualPermitsUrl().isEmpty()) {
            missingURLs += ++count +". Virtual permits url\n";
        }
        if (CeoApplication.DefectReportingUrl() == null || CeoApplication.DefectReportingUrl().isEmpty()) {
            missingURLs += ++count +". Defect reporting url\n";
        }
        if (CeoApplication.getSpecialVehicleUrl() == null || CeoApplication.getSpecialVehicleUrl().isEmpty()) {
            missingURLs += ++count +". Special Vehicle url\n";
        }
        if (!missingURLs.isEmpty())
            missingURLs = "Following URLs are not configured.\n" + missingURLs + "Please contact your supervisor immediately.";
        return missingURLs;
    }

    private void checkRequiredFiles() {
        String missingFiles = "";
        int count = 0;
        if (!CeoApplication.IsDataFileExist("suffixes.json")) {
            missingFiles += ++count +". suffixes.json\n";
        }
        if (!CeoApplication.IsDataFileExist("countries.json")) {
            missingFiles += ++count +". countries.json\n";
        }
        if (!CeoApplication.IsDataFileExist("action_priority.json")) {
            missingFiles += ++count +". action_priority.json\n";
        }
        if (!CeoApplication.IsDataFileExist("keyboard.json")) {
            missingFiles += ++count +". keyboard.json\n";
        }

        if (!missingFiles.isEmpty()) {
            missingFiles = "Following Files are missing.\n\n" + missingFiles + "\nPlease contact your supervisor immediately.";
            Utils.showExitDialog(LoginActivity.this, missingFiles, "Required file(s) missing");
        }
    }
    private void fetchRemoteConfigData() {

        //clientLogoText.setText(mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY));
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                           // String updated = task.getResult();
                            Toast.makeText(LoginActivity.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                       // String welcomeMessage = mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY);
                        //clientLogoText.setText(welcomeMessage);

                    }
        });
    }


}
