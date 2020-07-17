package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.farthestgate.android.R;
import com.farthestgate.android.model.AdditionalInfo;

/**
 * Created by Hanson Aboagye on 28/04/2014.
 */
public class AdditionalInfoDialog extends DialogFragment
{

    public interface OnDestinationInfoEntered{
        void OnOtherInfoAdded(AdditionalInfo info);
    }

    private OnDestinationInfoEntered mAdditionalInfoListener;
    private AdditionalInfo additionalInfo;

    public static AdditionalInfoDialog NewInstance(boolean[] savedChoices)
    {
        AdditionalInfo additionalInfo = new AdditionalInfo();
        if (savedChoices != null)
        {
            additionalInfo.selectedOptions = savedChoices;
        }
        AdditionalInfoDialog ad = new AdditionalInfoDialog();
        ad.additionalInfo = additionalInfo;

        return ad;
    }

    public AdditionalInfoDialog()
    {    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setMultiChoiceItems(R.array.other_info_items,additionalInfo.selectedOptions,new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        additionalInfo.selectedOptions[which] = isChecked;
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdditionalInfoListener.OnOtherInfoAdded(additionalInfo);
                        dismiss();
                    }
                })
                .setTitle("Other Info");

        return builder.create();
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mAdditionalInfoListener = (OnDestinationInfoEntered) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
