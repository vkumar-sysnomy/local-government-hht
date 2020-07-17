package com.farthestgate.android.utils;

import android.content.Intent;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.ui.dialogs.LocationPopup;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;

import java.util.TimerTask;

/**
 * Created by Hanson Aboagye on 29/04/2014.
 */
public class LogTimerTask extends TimerTask
{

    @Override
    public void run()
    {
        Intent locIntent = new Intent(CeoApplication.getContext(), LocationPopup.class);
        locIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (VisualPCNListActivity.currentActivity > 0)
            CeoApplication.logHandler.postDelayed(this,180000l);
        else
            CeoApplication.getContext().startActivity(locIntent);
    }

}
