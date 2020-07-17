package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.farthestgate.android.R;

public class MultiLogDialog extends DialogFragment
{

    private CheckBox checkPD;
    private RadioButton radioMoved;
    private RadioButton radioSame;
    private RadioGroup rg;

    public interface OnMultiLogListener
    {
        public void onSameLocation(Boolean newPD);
        public void onMoved();
    }


    private OnMultiLogListener mListener;

    public static MultiLogDialog newInstance() {
        MultiLogDialog fragment = new MultiLogDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public MultiLogDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v      = inflater.inflate(R.layout.fragment_multi_log_dialog, null);
        checkPD     = (CheckBox) v.findViewById(R.id.chkPD);
        radioMoved  = (RadioButton) v.findViewById(R.id.radNew);
        radioSame   = (RadioButton) v.findViewById(R.id.radSame);
        rg          = (RadioGroup) v.findViewById(R.id.rgLocation);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == radioSame.getId())
                {
                    if (radioSame.isChecked())
                    {
                        checkPD.setVisibility(View.VISIBLE);
                    }
                    else {
                        checkPD.setVisibility(View.INVISIBLE);
                        checkPD.setChecked(false);
                    }
                }
                if (checkedId == radioMoved.getId())
                {
                    if (radioMoved.isChecked())
                    {
                        checkPD.setVisibility(View.INVISIBLE);
                        checkPD.setChecked(false);
                    }
                    else {
                        checkPD.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        builder.setView(v)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if (radioMoved.isChecked())
                        {
                            mListener.onMoved();
                        }
                        else
                        {
                            mListener.onSameLocation(checkPD.isChecked());
                        }
                        dismiss();
                    }
                });
        builder.setTitle("Vehicle Log Options");
        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMultiLogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



}
