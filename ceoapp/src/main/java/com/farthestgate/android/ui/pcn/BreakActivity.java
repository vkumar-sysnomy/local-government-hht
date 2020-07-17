package com.farthestgate.android.ui.pcn;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;

import com.farthestgate.android.R;
import com.farthestgate.android.ui.admin.BaseActivity;

public class BreakActivity extends BaseActivity implements  BreakEndFragment.OnBreakEndListener {
    public static Long breakID;
    private Boolean breakStarted = false;
    public static String breakType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_break);
        breakType = getIntent().getStringExtra("breakType");
        ActionBar actionBar = getActionBar();
        if(breakType.equalsIgnoreCase("BREAK")){
            actionBar.setTitle("Break");
        }else{
            actionBar.setTitle("In Transit");
        }
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new BreakEndFragment())
            .commit();
        }
    }

    @Override
    public void onBreakEnd() {
        finish();
    }



    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            breakUpdate();
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void breakUpdate()
    {
        if (breakStarted) {
            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setMessage("Would you like to end the break ?");
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (breakStarted) {
                        dialog.dismiss();
                    }
                }
            });
            ad.setCancelable(false);
            ad.show();
        }
    }

    @Override
         public void onBackPressed() {
            breakUpdate();
         }
}
