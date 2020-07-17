package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.helper.FileHelper;
import com.farthestgate.android.model.DestinationInfo;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Hanson Aboagye on 28/04/2014.
 */
public class DestinationDialog extends DialogFragment
{

    private CheckBox checkDriver;
    private CheckBox checkWindows;
    private CheckBox checkVerbal;
    private CheckBox checkPhysical;
    private RadioButton radioHD;
    private RadioButton radioVDA;
    private RadioButton radioAFW;
    private RadioButton radioVoid;
    private RadioButton radioPFI;
    private RadioGroup  rg;
    private Spinner actionToTake;
    private Spinner actionPriority;

    private Boolean checkedRadio = false;
    private static Context activityContext;

    public interface OnDestinationInfoEntered{
        void OnDestinationInfoAdded(DestinationInfo info);
    }

    OnDestinationInfoEntered mDestinationInfoListener;
    DestinationInfo destinationInfo;

    public static DestinationDialog NewInstance(DestinationInfo destinationInfo)
    {
        DestinationDialog dDialog = new DestinationDialog();
        if (destinationInfo != null) {
            dDialog.destinationInfo = destinationInfo;
        }
        return dDialog;
    }

    public DestinationDialog()
    {

    }

    private Boolean hasInfoBeenEntered()
    {
        if ((checkDriver.isChecked() || checkPhysical.isChecked() ||
             checkVerbal.isChecked() || checkWindows.isChecked()) && checkedRadio)
        {
            return true;
        }
        else
            return false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_destination_dialog, null);

        checkWindows    = (CheckBox) v.findViewById(R.id.checkWindows);
        checkDriver     = (CheckBox) v.findViewById(R.id.checkDriver);
        checkVerbal     = (CheckBox) v.findViewById(R.id.checkVerbal);
        checkPhysical   = (CheckBox) v.findViewById(R.id.checkPhysical);
        radioAFW        = (RadioButton) v.findViewById(R.id.radioAffix);
        radioVDA        = (RadioButton) v.findViewById(R.id.radioVDA);
        radioHD         = (RadioButton) v.findViewById(R.id.radioHD);
        radioVoid       = (RadioButton) v.findViewById(R.id.radioVoid);
        radioPFI       = (RadioButton) v.findViewById(R.id.radioPFI);
        rg              = (RadioGroup) v.findViewById(R.id.rgDest);
        actionToTake    = (Spinner) v.findViewById(R.id.spnActionToTake);
        actionPriority  = (Spinner) v.findViewById(R.id.spnActionPriority);

        if(destinationInfo == null){
            destinationInfo =  new DestinationInfo();
        }
        if(CeoApplication.AllowVoid()){
            radioVoid.setVisibility(View.VISIBLE);
        }else{
            radioVoid.setVisibility(View.INVISIBLE);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_spinner_item, loadActionPriority("action_to_take"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionToTake.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_spinner_item, loadActionPriority("action_priority"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionPriority.setAdapter(adapter);
        int defaultPriority = adapter.getPosition("5");
        actionPriority.setSelection(defaultPriority);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == radioAFW.getId())
                {
                    destinationInfo.pcnDestination = DestinationInfo.AFFIXED_TO_WINDSHIELD;
                    checkedRadio = true;
                }
                if (checkedId == radioVDA.getId())
                {
                    destinationInfo.pcnDestination = DestinationInfo.VDA;
                    checkedRadio = true;
                }
                if (checkedId == radioHD.getId())
                {
                    destinationInfo.pcnDestination = DestinationInfo.HANDED_TO_DRIVER;
                    checkedRadio = true;
                }
                if (checkedId == radioVoid.getId())
                {
                    destinationInfo.pcnDestination = DestinationInfo.VOID;
                    checkedRadio = true;
                }
                if (checkedId == radioPFI.getId())
                {
                    destinationInfo.pcnDestination = DestinationInfo.PREVENTED_FROM_ISSUE;
                    checkedRadio = true;
                }
            }
        });


        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                        Log.e("destinationdialog","saved");
                    }
                });
        builder.setTitle("PCN Destination");
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackDestinaitonDialog();
                Log.e("destinationdialog","Positive");
                readState();

                if (hasInfoBeenEntered()) {
                    Log.e("destinationdialog","Entered");
                    mDestinationInfoListener.OnDestinationInfoAdded(destinationInfo);
                    Log.e("destinationdialog","before dismiss");
                    dismiss();

                }
            }
        });

        return dialog;
    }

    private void readState()
    {
        String action = (String)actionToTake.getSelectedItem();
        destinationInfo.actionToTake = action.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

        //HAN-80
        //String priorityValue=(String) actionPriority.getSelectedItem();
        destinationInfo.actionPriority=(String) actionPriority.getSelectedItem();
        /*if(priorityValue.equalsIgnoreCase("High")){
            destinationInfo.actionPriority=1;
        }
        else if(priorityValue.equalsIgnoreCase("Medium")){
            destinationInfo.actionPriority=5;
        }
        else if(priorityValue.equalsIgnoreCase("Low")){
            destinationInfo.actionPriority=10;
        }*/

        //destinationInfo.actionPriority = Integer.parseInt((String) actionPriority.getSelectedItem());
        if (checkDriver.isChecked())
            destinationInfo.driverInteraction[0] = DestinationInfo.DRIVER_SEEN;
        if (checkVerbal.isChecked())
            destinationInfo.driverInteraction[1] = DestinationInfo.VERBAL_ABUSE;
        if (checkPhysical.isChecked())
            destinationInfo.driverInteraction[2] = DestinationInfo.PHYSICAL_ABUSE;
        if (checkWindows.isChecked())
            destinationInfo.driverInteraction[3] = DestinationInfo.ALL_WINDOWS_CHECKED;

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        activityContext =activity;
        try
        {
            mDestinationInfoListener = (OnDestinationInfoEntered) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }


    //need to improve
    private List<String> loadActionPriority(String name){
        List<String> list = new ArrayList<>();
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        String actionPriorityStr = cameraImageHelper.readFile(AppConstant.CONFIG_FOLDER, "action_priority.json");

        try {
            JSONArray actionPriorityArray = new JSONObject(actionPriorityStr).getJSONArray(name);
            Type type = new TypeToken<List<String>>(){}.getType();

            list = new Gson().fromJson(actionPriorityArray.toString(), type );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
