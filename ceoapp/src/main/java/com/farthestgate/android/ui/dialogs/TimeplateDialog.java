package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.farthestgate.android.R;
import com.farthestgate.android.model.Timeplate;

import org.droidparts.adapter.widget.StringSpinnerAdapter;


public class TimeplateDialog extends DialogFragment {

    private static final long  THIRTY_MINS  = 1800000l;
    private static final long  ONE_HOUR  = 3600000l;
    private static final long  ONE_HOUR_THIRTY  = 5400000l;
    private static final long  TWO_HOURS  = 7200000l;

    private OnTimeplateInfoListener mListener;

    private long maxTime = 0l;
    private long minReturn = 0l;
    private String title = "";
    private String message = "Timeplate Information";

    public static TimeplateDialog newInstance() {
        TimeplateDialog fragment = new TimeplateDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public TimeplateDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString("title");
            message = getArguments().getString("msg");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_timeplate_dialog, null);
        final Spinner max_spinner = (Spinner) v.findViewById(R.id.spinner_max);
        StringSpinnerAdapter sp = new StringSpinnerAdapter(max_spinner,R.array.max_time);
        max_spinner.setAdapter(sp);
        max_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        maxTime = THIRTY_MINS;
                        break;
                    case 1:
                        maxTime = ONE_HOUR;
                        break;
                    case 2:
                        maxTime = ONE_HOUR_THIRTY;
                        break;
                    case 3:
                        maxTime = TWO_HOURS;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner return_spinner = (Spinner) v.findViewById(R.id.spinner_return);
        return_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        minReturn = THIRTY_MINS;
                        break;
                    case 1:
                        minReturn = ONE_HOUR;
                        break;
                    case 2:
                        minReturn = ONE_HOUR_THIRTY;
                        break;
                    case 3:
                        minReturn = TWO_HOURS;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp = new StringSpinnerAdapter(max_spinner,R.array.max_time);
        return_spinner.setAdapter(sp);

        builder.setView(v)
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timeplate tp    = new Timeplate();
                        tp.maxTime      = maxTime;
                        tp.noReturnTime = minReturn;
                        mListener.onTimeplateInfoEntered(tp);
                    }
                })
                .setTitle(message);
        final AlertDialog ad = builder.create();

        return ad;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTimeplateInfoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnTimeplateInfoListener {
        public void onTimeplateInfoEntered(Timeplate timeplate);
    }

}
