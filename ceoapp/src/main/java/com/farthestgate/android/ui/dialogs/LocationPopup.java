package com.farthestgate.android.ui.dialogs;

import android.content.pm.ActivityInfo;
import android.os.Bundle;


import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.ui.pcn.VisualPCNListActivity;

/**
 * Created by Hanson Aboagye on 29/04/2014.
 */
public class LocationPopup extends FragmentActivity implements LogLocationDialog.OnNewLocationLoggedListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //TODO: Need to add bellow line
        //setTheme(android.R.style.Theme_Holo_Light_NoActionBar);

        //Theme.AppCompat.Light.NoActionBar

        String stName = "";
        if (VisualPCNListActivity.currentStreet != null)
        {
            stName = VisualPCNListActivity.currentStreet.streetname;
        }
        LogLocationDialog logLocationDialog = new LogLocationDialog();
        logLocationDialog.setCancelable(VisualPCNListActivity.firstLogin);
        logLocationDialog.show(getSupportFragmentManager(),"");
    }

    @Override
    public void onLocationChanged(String newStreet) {
        if (!VisualPCNListActivity.firstLogin)
            VisualPCNListActivity.firstLogin = true;
        VisualPCNListActivity.currentStreet.streetname = newStreet;
        finish();
    }

    @Override
    public void onLocationUnchanged() {
        finish();
    }
}