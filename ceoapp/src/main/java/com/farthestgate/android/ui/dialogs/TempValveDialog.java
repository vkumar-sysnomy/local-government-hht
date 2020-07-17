package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.ui.components.CustomSpinner;
import com.farthestgate.android.ui.components.OnSwipeTouchListener;
import com.farthestgate.android.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Hanson Aboagye 04/2014
 *
 */
public class TempValveDialog extends DialogFragment
{

    public interface OnValveInfoEntered{
        void OnValvesEntered(Integer front, Integer back, Boolean isSecondReading);
    }

    OnValveInfoEntered  mValveInfoEnteredListener;
    /*NumberPicker        frontValve;
    NumberPicker        backValve;*/

    RelativeLayout layoutFrontValue, layoutRearValue;
    TextView textViewFrontValue, textViewFrontPre, textViewFrontPost, textViewRearValue, textViewRearPre, textViewRearPost;
    ImageView imageViewFront, imageViewRear;

    Integer frontV = 1;
    Integer backV = 1;

    Boolean isSecondLocation = false;

    public static TempValveDialog NewInstance(Integer front, Integer back, Boolean secondValves)
    {
        TempValveDialog tvd = new TempValveDialog();
        Bundle args = new Bundle();
        args.putBoolean("second", secondValves);
        tvd.setArguments(args);
        android.util.Log.e("valvepositionfront", String.valueOf(front));
        android.util.Log.e("valvepositionback", String.valueOf(back));

        if (front != null)
           tvd.frontV = front;
        if (back != null)
            tvd.backV = back;
        return tvd;
    }

    public TempValveDialog() {
        // Required empty public constructor
       }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle info = getArguments();
        if (info != null)
            isSecondLocation = (Boolean) getArguments().get("second");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_temp_valve_dialog, null);

        /*backValve = (NumberPicker) v.findViewById(R.id.backPicker);
        frontValve = (NumberPicker) v.findViewById(R.id.frontPicker);

        backValve.setMinValue(1);
        backValve.setMaxValue(12);
        frontValve.setMinValue(1);
        frontValve.setMaxValue(12);
        frontValve.setValue(frontV);
        backValve.setValue(backV);*/

        layoutFrontValue = (RelativeLayout) v.findViewById(R.id.layoutFrontValue);
        layoutRearValue = (RelativeLayout) v.findViewById(R.id.layoutRearValue);
        textViewFrontValue = (TextView) v.findViewById(R.id.textViewFrontValue);
        textViewFrontPre = (TextView) v.findViewById(R.id.textViewFrontPre);
        textViewFrontPost = (TextView) v.findViewById(R.id.textViewFrontPost);
        textViewRearValue = (TextView) v.findViewById(R.id.textViewRearValue);
        textViewRearPre = (TextView) v.findViewById(R.id.textViewRearPre);
        textViewRearPost = (TextView) v.findViewById(R.id.textViewRearPost);
        imageViewFront = (ImageView) v.findViewById(R.id.imageViewFront);
        imageViewRear = (ImageView) v.findViewById(R.id.imageViewRear);
        if(frontV!=null) {
            if(frontV==12)
            {
                textViewFrontPre.setText(String.valueOf(frontV-1));
                textViewFrontValue.setText(String.valueOf(frontV));
                textViewFrontPost.setText(String.valueOf(1));
            }else if(frontV==1){
                textViewFrontPre.setText(String.valueOf(12));
                textViewFrontValue.setText(String.valueOf(frontV));
                textViewFrontPost.setText(String.valueOf(frontV+1));
            }
            else {
                textViewFrontPre.setText(String.valueOf(frontV-1));
                textViewFrontValue.setText(String.valueOf(frontV));
                textViewFrontPost.setText(String.valueOf(frontV+1));
            }
        }
        if(backV!=null)
        {
            if(backV==12)
            {
                textViewRearPre.setText(String.valueOf(backV-1));
                textViewRearValue.setText(String.valueOf(backV));
                textViewRearPost.setText(String.valueOf(1));
            }else if(backV==1)
            {
                textViewRearPre.setText(String.valueOf(12));
                textViewRearValue.setText(String.valueOf(backV));
                textViewRearPost.setText(String.valueOf(backV+1));

            }else {
                textViewRearPre.setText(String.valueOf(backV-1));
                textViewRearValue.setText(String.valueOf(backV));
                textViewRearPost.setText(String.valueOf(backV+1));

            }

        }

        layoutFrontValue.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeLeft() {
                //Next
                int frontValue = Integer.parseInt(textViewFrontValue.getText().toString());
                if(frontValue ==12){
                    frontValue = 1;
                    textViewFrontValue.setText(String.valueOf(frontValue));
                    textViewFrontPre.setText(String.valueOf("12"));
                    textViewFrontPost.setText(String.valueOf(frontValue+1));
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(frontValue);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewFront.setImageDrawable(res);
                }else {
                    textViewFrontPre.setText(String.valueOf(frontValue));
                    textViewFrontValue.setText(String.valueOf(frontValue + 1));
                    if (frontValue + 2 > 12) {
                        textViewFrontPost.setText(String.valueOf("1"));
                    } else {
                        textViewFrontPost.setText(String.valueOf(frontValue + 2));
                    }
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(frontValue + 1);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewFront.setImageDrawable(res);
                }
            }

            @Override
            public void onSwipeRight() {
                //Back
                int frontValue = Integer.parseInt(textViewFrontValue.getText().toString());
                if(frontValue ==1){
                    frontValue = 12;
                    textViewFrontValue.setText(String.valueOf(frontValue));
                    textViewFrontPre.setText(String.valueOf(frontValue-1));
                    textViewFrontPost.setText(String.valueOf(1));
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(frontValue);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewFront.setImageDrawable(res);
                }else {
                    if (frontValue - 2 < 1) {
                        textViewFrontPre.setText(String.valueOf("12"));
                    } else {
                        textViewFrontPre.setText(String.valueOf(frontValue - 2));
                    }
                    textViewFrontValue.setText(String.valueOf(frontValue - 1));
                    textViewFrontPost.setText(String.valueOf(frontValue));

                    String imageUri = "@drawable/wheeloclock" + String.valueOf(frontValue - 1);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewFront.setImageDrawable(res);
                }
            }
        });

        layoutRearValue.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeLeft() {
                //Next
                int rearValue = Integer.parseInt(textViewRearValue.getText().toString());
                if(rearValue ==12){
                    rearValue = 1;
                    textViewRearValue.setText(String.valueOf(rearValue));
                    textViewRearPre.setText(String.valueOf("12"));
                    textViewRearPost.setText(String.valueOf(rearValue+1));
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(rearValue);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewRear.setImageDrawable(res);
                }else {
                    textViewRearPre.setText(String.valueOf(rearValue));
                    textViewRearValue.setText(String.valueOf(rearValue + 1));
                    if (rearValue + 2 > 12) {
                        textViewRearPost.setText(String.valueOf("1"));
                    } else {
                        textViewRearPost.setText(String.valueOf(rearValue + 2));
                    }
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(rearValue + 1);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewRear.setImageDrawable(res);
                }
            }

            @Override
            public void onSwipeRight() {
                //Back
                int rearValue = Integer.parseInt(textViewRearValue.getText().toString());
                if(rearValue ==1){
                    rearValue = 12;
                    textViewRearValue.setText(String.valueOf(rearValue));
                    textViewRearPre.setText(String.valueOf(rearValue-1));
                    textViewRearPost.setText(String.valueOf(1));
                    String imageUri = "@drawable/wheeloclock" + String.valueOf(rearValue);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewRear.setImageDrawable(res);
                }else {
                    if (rearValue - 2 < 1) {
                        textViewRearPre.setText(String.valueOf("12"));
                    } else {
                        textViewRearPre.setText(String.valueOf(rearValue - 2));
                    }
                    textViewRearValue.setText(String.valueOf(rearValue - 1));
                    textViewRearPost.setText(String.valueOf(rearValue));

                    String imageUri = "@drawable/wheeloclock" + String.valueOf(rearValue - 1);
                    int imageResource = getResources().getIdentifier(imageUri, null, getActivity().getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    imageViewRear.setImageDrawable(res);
                }
            }
        });

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                        //mValveInfoEnteredListener.OnValvesEntered(frontValve.getValue(),backValve.getValue(),isSecondLocation);
                        mValveInfoEnteredListener.OnValvesEntered(Integer.parseInt(textViewFrontValue.getText().toString()),Integer.parseInt(textViewRearValue.getText().toString()),isSecondLocation);
                        TempValveDialog.this.getDialog().dismiss();
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
            mValveInfoEnteredListener = (OnValveInfoEntered) activity;

        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }


}
