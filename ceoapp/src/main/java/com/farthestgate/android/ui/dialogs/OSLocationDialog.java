package com.farthestgate.android.ui.dialogs;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.farthestgate.android.R;

public class OSLocationDialog extends DialogFragment {

    String location;
    Boolean isSecondLocation = false;

    public interface LocationDialogListener {
        public void onLocationSave(String loc, Boolean secondLoc);
        public void onLocationCancel();
    }
/*
    public static OSLocationDialog newInstance(Boolean isSecondLocation) {
        OSLocationDialog fragment = new OSLocationDialog(currentLoc);
        Bundle args = new Bundle();
        args.putBoolean("second", isSecondLocation);
        fragment.setArguments(args);
        return fragment;
    }*/
    /*
    public OSLocationDialog()
    {  // Required empty public constructor
    }*/


    public OSLocationDialog(String currentLoc) {
        location = currentLoc;
    }


   LocationDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle info = getArguments();
        if ( info != null)
            isSecondLocation = (Boolean) info.get("second");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_location_dialog, null);
        RadioButton radioStreet = (RadioButton) v.findViewById(R.id.radioStreet);
        RadioButton radioLampost= (RadioButton) v.findViewById(R.id.radioLampost);
        RadioButton radioOpposite= (RadioButton) v.findViewById(R.id.radioOpposite);
        RadioButton radioOther  = (RadioButton) v.findViewById(R.id.radioOther);

        final EditText editLocation   = (EditText) v.findViewById(R.id.editLocation);

        editLocation.setText(location);
        radioStreet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    location = "Outside No: ";
                    editLocation.setText(location);
                    editLocation.append(" ");
                    editLocation.moveCursorToVisibleOffset();

                }
            }
        });
        radioLampost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    location = "Lampost No: ";
                    editLocation.setText(location);
                    editLocation.append(" ");
                    editLocation.moveCursorToVisibleOffset();
                }

            }
        });

        radioOpposite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    location = "Opposite: ";
                    editLocation.setText(location);
                    editLocation.append(" ");
                    editLocation.moveCursorToVisibleOffset();
                }

            }
        });
        radioOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    location = "Outside  ";
                    editLocation.setText(location);
                    editLocation.append(" ");
                    editLocation.moveCursorToVisibleOffset();
                }
            }
        });


        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        mListener.onLocationSave(editLocation.getText().toString(),isSecondLocation);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (LocationDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LocationDialogListener");
        }
    }

}
