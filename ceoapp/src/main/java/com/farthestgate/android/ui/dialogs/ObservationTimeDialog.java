package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.utils.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hanson Aboagye on 28/04/2014.
 */
public class ObservationTimeDialog extends DialogFragment
{

    CharSequence[] waitTimeItems;
    CharSequence[] waitTimeItemsMins;
    public interface OnObservationSelectionInterface {
        void OnObservationTimeSelected(Integer waiting);
    }

    OnObservationSelectionInterface mWaitTimeListener;
    Integer waitTime;
    public ObservationTimeDialog()
    {
        waitTime = 5;
    }
    public ObservationTimeDialog(String contraventionCode, int maxStayMinutes) {
        int minimumWaitTime = -1;
        List<String> observationTimesItems = new ArrayList<String>();
        try {
            JSONObject minimumObservationTimesContent = CeoApplication.GetDataFileContentAsObject("minimumobservationtimes.json");
            JSONArray minimumObservationTimesOptions = minimumObservationTimesContent.getJSONArray("grace");
            if(minimumObservationTimesOptions != null) {
                for (int i = 0; i < minimumObservationTimesOptions.length(); i++) {
                    JSONObject minimumObservationTimesObject = minimumObservationTimesOptions.getJSONObject(i);
                    if (minimumObservationTimesObject.getString("contraventionCode").equalsIgnoreCase(contraventionCode)) {
                        minimumWaitTime = Integer.valueOf(minimumObservationTimesObject.getString("authorityMinimum"));
                        break;
                    }
                }
            }
            if (minimumWaitTime != -1){
                minimumWaitTime = maxStayMinutes + minimumWaitTime;
                observationTimesItems.add(String.valueOf(minimumWaitTime));
            }
            if (CeoApplication.IsDataFileExist("observationtimes.json")) {
                File observationTimesFile = new File(Environment.getExternalStorageDirectory() + "/" + AppConstant.CONFIG_FOLDER + "observationtimes.json");
                InputStream fileStream = new FileInputStream(observationTimesFile);
                JSONObject observationTimes = new JSONObject(StringUtil.getStringFromInputStream(fileStream));
                JSONArray observationTimesOptions = observationTimes.getJSONArray("observationtimes");
                for (int i = 0; i < observationTimesOptions.length(); i++) {
                    if (minimumWaitTime == -1) {
                        observationTimesItems.add(observationTimesOptions.getString(i));
                    } else {
                        int nextObservationTimesOption = maxStayMinutes + Integer.valueOf(observationTimesOptions.getString(i));
                        if (nextObservationTimesOption > minimumWaitTime)
                            observationTimesItems.add(String.valueOf(nextObservationTimesOption));
                    }
                }

            } else {
                String[] waitTimes = { "5", "20", "40" };
                for (int i = 0; i < waitTimes.length; i++) {
                    if (minimumWaitTime == -1) {
                        observationTimesItems.add(waitTimes[i]);
                    } else {
                        int nextObservationTimesOption = maxStayMinutes + Integer.valueOf(waitTimes[i]);
                        if (nextObservationTimesOption > minimumWaitTime)
                            observationTimesItems.add(String.valueOf(nextObservationTimesOption));
                    }
                }
            }
            Collections.sort(observationTimesItems);
            Collections.sort(observationTimesItems,new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return Integer.valueOf(lhs).compareTo(Integer.valueOf(rhs));
                }
            });
            waitTimeItems = observationTimesItems.toArray(new CharSequence[observationTimesItems.size()]);
            waitTimeItemsMins = new CharSequence[waitTimeItems.length];
            for(int i = 0; i < waitTimeItems.length; i++){
                waitTimeItemsMins[i] = waitTimeItems[i] + " " + "mins";
            }
            waitTime = Integer.valueOf(waitTimeItems[0].toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mWaitTimeListener.OnObservationTimeSelected(waitTime);
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setSingleChoiceItems(waitTimeItemsMins, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        waitTime = Integer.valueOf(waitTimeItems[which].toString());
                    }
                }
                /*.setSingleChoiceItems(R.array.waiting_time_items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                waitTime = 5;
                                break;
                            case 1:
                                waitTime = 20;
                                break;
                            case 2:
                                waitTime = 40;
                                break;
                        }
                    }
                }*/
                )
                .setTitle("Waiting Time");

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mWaitTimeListener = (OnObservationSelectionInterface) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
