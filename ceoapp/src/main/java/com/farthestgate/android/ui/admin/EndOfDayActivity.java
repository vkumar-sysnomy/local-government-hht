package com.farthestgate.android.ui.admin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.helper.UnsentPCNService;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class EndOfDayActivity extends BaseActivity {

    private final String TAG = "END OF DAY";

    private Button btnLogOut;
    private TextView txtCEO;
    private TextView txtPCNs;
    private TextView txtPics;

    private SharedPreferenceHelper sharedPreferenceHelper;
    private Integer lastSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_end_of_day);

        btnLogOut = (Button) findViewById(R.id.btnLogout);
        txtPCNs = (TextView) findViewById(R.id.txtEndPCN);
        txtCEO = (TextView) findViewById(R.id.txtCEOEnd);
        txtPics = (TextView) findViewById(R.id.txtEndPics);

        sharedPreferenceHelper = new SharedPreferenceHelper(this);

        lastSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;

        txtCEO.setText(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
        // TODO: 2/28/2017 Changing session number Suraj
        /*txtPCNs.setText(String.valueOf(DBHelper.GetPCNCount(lastSession)));
        txtPics.setText(String.valueOf(DBHelper.PhotosCount(lastSession)));*/
        List<PCNTable> pcnTables = DBHelper.GetPCNs(0, false);
        txtPCNs.setText(String.valueOf(pcnTables.size()));
        String observations = "";
        for(PCNTable pcnTable : pcnTables){
            if(observations.isEmpty()) {
                observations += pcnTable.getObservation();
            } else{
                observations += "," + pcnTable.getObservation();
            }
        }
        txtPics.setText(String.valueOf(DBHelper.PhotosCount(observations)));

        btnLogOut.setOnClickListener(logoutClick);

    }

    private View.OnClickListener logoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            CeoApplication.LogoutProcess();
            theEnd();

        }
    };




    private void theEnd() {
        if(CeoApplication.useAnpr()){
            try {

                String path=Environment.getExternalStorageDirectory().getAbsolutePath();
                File files = new File(path);
                String [] arr=files.list();
                    for (String str:arr){
                        if(str.toLowerCase().contains("anpr")||str.toLowerCase().contains("parking")
                                ||str.toLowerCase().contains("vehiclelist")){
                            File newFile=new File(path+"/"+str);
                            boolean isDeleted=newFile.delete();
                            Log.i("isDeleted=>" + isDeleted);

                        }
                    }

                //String cmd1="del \\sdcard\\anpr*.jpg";
                //String cmd2="del \\sdcard\\parking*.csv";
                //Runtime.getRuntime().exec(new String[]{cmd1,cmd2});
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        VisualPCNListActivity.lTimer.cancel();
        stopUnsentFPNService();
        stopUnsentPhotoService();
        sharedPreferenceHelper.saveValue(LoginActivity.LAST_CEO, DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
        sharedPreferenceHelper.saveValue(PCNTable.COL_PCN_SESSION, lastSession);
        resetStatics();
        finish();
        System.exit(0); // IS THIS REALLY NECESSARY ???????
    }

    private void resetStatics() {
        VisualPCNListActivity.currentStreet = null;
        VisualPCNListActivity.lTimer = null;
        sharedPreferenceHelper.clearSharedPrefs(CeoApplication.getContext(), "ceo", MODE_PRIVATE);
    }

    public void stopUnsentFPNService() {
        Intent intent = new Intent(getApplicationContext(), UnsentPCNService.class);
        final PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public void stopUnsentPhotoService() {
        Intent intent = new Intent(getApplicationContext(), RemovalPhotoService.class);
        final PendingIntent pIntent = PendingIntent.getService(this, 1, intent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.db_extract, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_db_extract:
                dbExtract();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dbExtract() {
        String  DB_PATH = "/data/data/" + getPackageName() + "/databases/";
        try {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                DB_PATH = getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
            } else {
                DB_PATH = "/data/data/" + getPackageName() + "/databases/";
            }*/

            String currentDBPath = "Ceoapp.db";
            String backupDBPath = "Ceoapp.db";
            File currentDB = new File(DB_PATH, currentDBPath);
            File backupDB = new File(new CameraImageHelper().getLocalDBFolder() + File.separator + backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getApplicationContext(), "Data pulled successfully", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            try {
                CeoApplication.LogError(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
