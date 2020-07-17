package com.farthestgate.android.ui.dialogs;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxStayDialog extends DialogFragment
{
    CharSequence[] maxStayItems;
    CharSequence[] maxStayItemsDesc;
    int index;
    int maxStayMinutes;
    public interface OnMaxStaySelectionInterface {
        void OnMaxStayOptionSelected(int index, int maxStayMinutes);
    }
    OnMaxStaySelectionInterface mMaxStayListener;

    public MaxStayDialog()
    {

    }







    public MaxStayDialog(JSONArray maxStays, int offenceCodeIndex) {
        this.index = offenceCodeIndex;
        List<String> maxStayItemOptions = new ArrayList<String>();
        List<String> maxStayItemValues = new ArrayList<String>();
        try {
            List<JSONObject> maxStaysList = new ArrayList<JSONObject>();
            for (int i = 0; i < maxStays.length(); i++) {
                maxStaysList.add(maxStays.getJSONObject(i));
            }
            Collections.sort(maxStaysList, new Comparator<JSONObject>() {
                private static final String KEY_NAME = "maxStayMinutes";
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int val1 = Integer.parseInt(valA);
                    int val2 = Integer.parseInt(valB);
                    if(val1 > val2){
                        return  0;
                    } else {
                        return 1;
                    }
                    //return valA.compareTo(valB);
                    //return -valA.compareTo(valB);
                }
            });
            for(JSONObject maxStayObject : maxStaysList){
                String maxStayMinutes = maxStayObject.getString("maxStayMinutes");
                maxStayItemValues.add(maxStayMinutes);
                int intMaxStayMinutes = Integer.valueOf(maxStayMinutes);
                if(intMaxStayMinutes>59) {
                    int hours = intMaxStayMinutes / 60;
                    int minutes = intMaxStayMinutes % 60;
                    maxStayMinutes += minutes == 0 ? "(" + hours + "hr)" : "(" + hours + "hr" + " " + minutes + "m)";
                }
                String maxStayDesc = maxStayMinutes + "   " + maxStayObject.getString("maxStayNote");
                maxStayItemOptions.add(maxStayDesc);

            }
            maxStayItems = maxStayItemValues.toArray(new CharSequence[maxStayItemValues.size()]);
            maxStayItemsDesc = maxStayItemOptions.toArray(new CharSequence[maxStayItemOptions.size()]);
            maxStayMinutes = Integer.valueOf(maxStayItems[0].toString());
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
                        mMaxStayListener.OnMaxStayOptionSelected(index, maxStayMinutes);
                        dismiss();
                    }
                })
                .setSingleChoiceItems(maxStayItemsDesc, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                maxStayMinutes = Integer.valueOf(maxStayItems[which].toString());
                            }
                        }

                )
                .setTitle("Maximum stays");

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mMaxStayListener = (OnMaxStaySelectionInterface) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
