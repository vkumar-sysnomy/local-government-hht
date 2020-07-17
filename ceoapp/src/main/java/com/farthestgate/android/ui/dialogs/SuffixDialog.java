package com.farthestgate.android.ui.dialogs;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.model.ContraventionSuffix;
import com.farthestgate.android.model.Suffix;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Hanson Aboagye 05/2014
 *
 */
public class SuffixDialog extends DialogFragment
{
    Integer indexSelected;
    List<String> newSuffixes;

    public interface OnNewSuffixInfoEnteredListener{
        void OnNewSuffix(String suffix);
    }

    OnNewSuffixInfoEnteredListener  mNewSuffixListener;

    public static SuffixDialog NewInstance(char[] suffixes)
    {
        SuffixDialog tvd = new SuffixDialog();
        Bundle args = new Bundle();
        args.putCharArray("contraventionSuffixes", suffixes);
        tvd.setArguments(args);

        return tvd;
    }

    public SuffixDialog() {
        // Required empty public constructor
       }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final char[] suffixes = getArguments().getCharArray("contraventionSuffixes");
        Suffix suffix = loadSuffixes();
        newSuffixes = new ArrayList<String>();
        for (ContraventionSuffix cf:suffix.contraventionSuffixes)
        {
            for (char letter : suffixes)
            {
                String sLetter = String.valueOf(letter).toUpperCase();
                if (cf.item.startsWith(sLetter) && !cf.item.contains("J"))
                {
                    newSuffixes.add(cf.item);
                }
            }
        }

        String[] nSfx = new String[newSuffixes.size()];
        newSuffixes.toArray(nSfx);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("New ContraventionSuffix")
                .setItems(nSfx, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        indexSelected = which;
                        mNewSuffixListener.OnNewSuffix(newSuffixes.get(indexSelected).substring(0,1));
                    }
                })
        .setPositiveButton("Continue", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (indexSelected != null)
                    mNewSuffixListener.OnNewSuffix(newSuffixes.get(indexSelected));
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mNewSuffixListener = (OnNewSuffixInfoEnteredListener) activity;

        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }


    private Suffix loadSuffixes(){
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        String suffixRes = cameraImageHelper.readFile(AppConstant.CONFIG_FOLDER, "suffixes.json");

        return new Gson().fromJson(suffixRes, Suffix.class);
    }


}
