package com.farthestgate.android.ui.pcn;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.database.BreakTable;

import java.util.Date;
import java.util.Random;

public class BreakEndFragment extends Fragment {

    private static final String TWO_DIGITS = "%02d";
    private static final String ONE_DIGIT = "%01d";
    private static final String NEG_TWO_DIGITS = "-%02d";
    private static final String NEG_ONE_DIGIT = "-%01d";

    private BreakTable breakRow = new BreakTable();
    private Chronometer timeDisplay;
    private TextView totalDisplay;

    /*long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();*/

    public interface OnBreakEndListener {
        public void onBreakEnd();
    }

    private OnBreakEndListener mListener;

    public static BreakEndFragment newInstance(String param1, String param2) {
        BreakEndFragment fragment = new BreakEndFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public BreakEndFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VisualPCNListActivity.currentActivity = 1;
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_break_end, container, false);
        timeDisplay = (Chronometer) rootView.findViewById(R.id.clockview);
        totalDisplay = (TextView) rootView.findViewById(R.id.totalTime);
        ImageButton btnEndBreak = (ImageButton) rootView.findViewById(R.id.btnEndBreak);
        String totalDisplayText="";
        if(BreakActivity.breakType.equalsIgnoreCase("BREAK")){
            totalDisplayText = "Total break time today :";
            btnEndBreak.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.end_break_selector));
        }else{
            totalDisplayText = "Total transit time today :";
            btnEndBreak.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.end_transit_selector));
        }

        totalDisplay.setText(totalDisplayText + " " + getBreakString(DBHelper.getTotalBreak(BreakActivity.breakType)));

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeue-UltraLight.otf");
        timeDisplay.setTypeface(tf);

        btnEndBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDisplay.stop();
                breakRow = DBHelper.getBreakTableObj(BreakActivity.breakID);
                breakRow.setEndTime(new Date().getTime());
                breakRow.save();

                if (mListener != null) {
                    mListener.onBreakEnd();
                }

            }
        });

        timeDisplay.setBase(SystemClock.elapsedRealtime());
        timeDisplay.start();

        Random rnd = new Random();
        BreakActivity.breakID = rnd.nextLong();
        breakRow.setCeoID(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
        breakRow.setBreakID(BreakActivity.breakID);
        breakRow.setStartTime(new Date().getTime());
        breakRow.setExtracted(0);
        breakRow.setBreakType(BreakActivity.breakType);
        breakRow.setEndTime(0);
        breakRow.save();
        /*startTime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);*/

        return rootView;
    }

    /* public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedTime % 1000);
            timeDisplay.setText("" + mins + ":" + String.format("%02d", secs));
            handler.postDelayed(this, 0);
        }};*/

    @Override
    public void onPause() {
        super.onPause();
    }


    private String getBreakString(long breakTime) {
        String bHours, bMinutes, bSeconds;
        long bseconds, bminutes, bhours;
        String format;
        boolean showNeg = false;

        bseconds = breakTime / 1000;
        bminutes = bseconds / 60;
        bseconds = bseconds - bminutes * 60;
        bhours = bminutes / 60;
        bminutes = bminutes - bhours * 60;

        if (bhours >= 10) {
            format = showNeg ? NEG_TWO_DIGITS : TWO_DIGITS;
            bHours = String.format(format, bhours);
        } else if (bhours > 0) {
            format = showNeg ? NEG_ONE_DIGIT : ONE_DIGIT;
            bHours = String.format(format, bhours);
        } else {
            bHours = "00";
        }

        // Minutes are never empty and when hours are non-empty, must be two digits
        if (bminutes >= 10) {
            format = (showNeg && bhours == 0) ? NEG_TWO_DIGITS : TWO_DIGITS;
            bMinutes = String.format(format, bminutes);
        } else {
            format = (showNeg && bhours == 0) ? NEG_ONE_DIGIT : TWO_DIGITS;
            bMinutes = String.format(format, bminutes);
        }
        // Seconds are always two digits
        bSeconds = String.format(TWO_DIGITS, bseconds);


        return String.format("%s:%s:%s", bHours, bMinutes, bSeconds);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBreakEndListener) activity;
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
}
