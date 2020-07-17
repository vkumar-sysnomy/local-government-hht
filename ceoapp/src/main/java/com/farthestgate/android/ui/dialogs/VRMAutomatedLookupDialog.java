package com.farthestgate.android.ui.dialogs;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.farthestgate.android.R;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class VRMAutomatedLookupDialog extends DialogFragment {

    public static final String TAG = VRMAutomatedLookupDialog.class.getName();

    private static final String UNIVERSAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private RelativeLayout navigationLayout;
    private LinearLayout typeLayout,locationLayout, zoneLayout, estatezoneLayout, startDateLayout, expiryDateLayout, expiryTimeLayout;
    private TextView titleType, type, titleLocation, location, titleZone, zone, titleEstate, estatezone, titleStartDate, startDate,
            titleExpiryDate, expiryDate, titleExpiryTime,expiryTime, permitCount, noValidPermitTxt;
    private ImageView ivBack;
    private ImageView ivNext;

    private ArrayList<PaidParking> paidParkings;
    private OnVRMLookupListener vrmLookupListener;

    private int currentPosition = 0;
    private boolean isFromANPR;

    public static VRMAutomatedLookupDialog newInstance(ArrayList<PaidParking> paidParkings, boolean isFromANPR) {
        VRMAutomatedLookupDialog frag = new VRMAutomatedLookupDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("paidparkings", paidParkings);
        args.putBoolean("isFromANPR", isFromANPR);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            vrmLookupListener = (OnVRMLookupListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paidParkings = getArguments().getParcelableArrayList("paidparkings");
        isFromANPR = getArguments().getBoolean("isFromANPR");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.vrm_automated_lookup_dialog, null);
        navigationLayout = (RelativeLayout) v.findViewById(R.id.navigation_layout);
        typeLayout = (LinearLayout) v.findViewById(R.id.type_layout);
        titleType = (TextView) v.findViewById(R.id.title_type);
        type = (TextView) v.findViewById(R.id.tv_type);

        locationLayout = (LinearLayout) v.findViewById(R.id.location_layout);
        titleLocation = (TextView) v.findViewById(R.id.title_location);
        location = (TextView) v.findViewById(R.id.tv_location);

        zoneLayout = (LinearLayout) v.findViewById(R.id.zone_layout);
        titleZone = (TextView) v.findViewById(R.id.title_zone);
        zone = (TextView) v.findViewById(R.id.tv_zone);

        estatezoneLayout = (LinearLayout) v.findViewById(R.id.estate_layout);
        titleEstate = (TextView) v.findViewById(R.id.title_estate);
        estatezone = (TextView) v.findViewById(R.id.tv_estate);

        startDateLayout = (LinearLayout) v.findViewById(R.id.start_date_layout);
        titleStartDate = (TextView) v.findViewById(R.id.title_start_date);
        startDate = (TextView) v.findViewById(R.id.tv_start_date);

        expiryDateLayout = (LinearLayout) v.findViewById(R.id.expiry_date_layout);
        titleExpiryDate = (TextView) v.findViewById(R.id.title_expiry_date);
        expiryDate = (TextView) v.findViewById(R.id.tv_expiry_date);

        expiryTimeLayout = (LinearLayout) v.findViewById(R.id.expiry_time_layout);
        titleExpiryTime = (TextView) v.findViewById(R.id.title_expiry_time);
        expiryTime = (TextView) v.findViewById(R.id.tv_expiry_time);

        permitCount = (TextView) v.findViewById(R.id.permitCount);
        ivBack = (ImageView) v.findViewById(R.id.ivBack);
        ivNext = (ImageView) v.findViewById(R.id.ivNext);

        noValidPermitTxt = (TextView) v.findViewById(R.id.noValidPermitTxt);

        if(paidParkings != null && paidParkings.size() > 0){
            controlVisibility(true);
            setData(paidParkings.get(currentPosition));
        } else{
            controlVisibility(false);
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPosition == 0) {
                    return;
                }
                --currentPosition;
                setData(paidParkings.get(currentPosition));
            }
        });

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPosition == paidParkings.size()-1) {
                    return;
                }
                ++currentPosition;
                setData(paidParkings.get(currentPosition));
            }
        });

        if(isFromANPR){
            builder.setView(v).setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        } else {
            builder.setView(v).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            builder.setView(v).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }

        builder.setTitle("This Vehicle has the following parking session");
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data="";
                if(paidParkings.size() == 0 ){
                    /*data = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                            + " - " + "No valid permit or cashless parking session";*/

                    data = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                            + " - " + " No Valid Permit or Paid for Parking Found";
                }else{
                    for(PaidParking paidParking : paidParkings){
                        String zone = "", estatezone = "";
                        if(paidParking.zone != null && !paidParking.zone.isEmpty()){
                            zone = ", Zone: " + paidParking.zone;
                        }
                        if(paidParking.estatezone != null && !paidParking.estatezone.isEmpty()){
                            estatezone = ", Estate Zone: " + paidParking.estatezone;
                        }

                        String permitStartDate = DateUtils.changeDateFormat(paidParking.start, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT);
                        String permitExpiryDate = "";
                        if(permitStartDate.isEmpty()){
                            permitExpiryDate = DateUtils.changeDateFormat(paidParking.exp, UNIVERSAL_DATE_FORMAT, "dd/MM/yyyy HH:mm");
                        } else{
                            permitExpiryDate = DateUtils.changeDateFormat(paidParking.exp, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT);
                        }

                        if(data.isEmpty()) {
                            data = DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + paidParking.type + ", Location no: " + paidParking.cashlessloc + zone + estatezone
                                    + ", Start: " + permitStartDate + ", End: " + permitExpiryDate;
                        } else{
                            data += "\n" + DateUtils.getFormatedDate(new Date(), "EEE, d MMM yyyy HH:mm:ss")
                                    + " - " + paidParking.type + ", Location no: " + paidParking.cashlessloc + zone + estatezone
                                    + ", Start: " + permitStartDate + ", End: " + permitExpiryDate;
                        }
                    }
                }
                if(vrmLookupListener != null){
                    vrmLookupListener.OnVRMLookupConfirmed(true, data);
                }
                dialog.dismiss();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vrmLookupListener.OnVRMLookupConfirmed(false, "");
                dialog.dismiss();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    private void controlVisibility(boolean isVisible){
        if(isVisible){
            typeLayout.setVisibility(View.VISIBLE);
            locationLayout.setVisibility(View.VISIBLE);
            zoneLayout.setVisibility(View.VISIBLE);
            estatezoneLayout.setVisibility(View.VISIBLE);
            startDateLayout.setVisibility(View.VISIBLE);
            expiryDateLayout.setVisibility(View.VISIBLE);
            expiryTimeLayout.setVisibility(View.VISIBLE);
            noValidPermitTxt.setVisibility(View.GONE);
        } else{
            typeLayout.setVisibility(View.GONE);
            locationLayout.setVisibility(View.GONE);
            zoneLayout.setVisibility(View.GONE);
            estatezoneLayout.setVisibility(View.GONE);
            startDateLayout.setVisibility(View.GONE);
            expiryDateLayout.setVisibility(View.GONE);
            expiryTimeLayout.setVisibility(View.GONE);
            noValidPermitTxt.setVisibility(View.VISIBLE);
        }
        if(paidParkings.size()>1)
            navigationLayout.setVisibility(View.VISIBLE);
        else
            navigationLayout.setVisibility(View.GONE);
    }

    private void setData(PaidParking paidParking){
        if(paidParking !=null) {
            permitCount.setText(currentPosition + 1 + "/" + paidParkings.size());
            type.setText(paidParking.type);

            if (paidParking.cashlessloc != null && !paidParking.cashlessloc.isEmpty()) {
                locationLayout.setVisibility(View.VISIBLE);
                location.setText("#" + paidParking.cashlessloc);
            } else {
                locationLayout.setVisibility(View.GONE);
            }

            if (paidParking.zone != null && !paidParking.zone.isEmpty()) {
                zoneLayout.setVisibility(View.VISIBLE);
                zone.setText(paidParking.zone);
            } else {
                zoneLayout.setVisibility(View.GONE);
            }

            if (paidParking.estatezone != null && !paidParking.estatezone.isEmpty()) {
                estatezoneLayout.setVisibility(View.VISIBLE);
                estatezone.setText(paidParking.estatezone);
            } else {
                estatezoneLayout.setVisibility(View.GONE);
            }

            String permitStartDate = DateUtils.changeDateFormat(paidParking.start, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT);
            if (paidParking.start != null && !paidParking.start.isEmpty()) {
                startDateLayout.setVisibility(View.VISIBLE);
                startDate.setText(permitStartDate);
            } else {
                startDateLayout.setVisibility(View.GONE);
            }

            String permitExpiryDate = DateUtils.changeDateFormat(paidParking.exp, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT);
            expiryDate.setText(permitExpiryDate);

            if (permitStartDate.isEmpty()) {
                expiryTimeLayout.setVisibility(View.VISIBLE);
                String permitExpiryTime = DateUtils.changeDateFormat(paidParking.exp, UNIVERSAL_DATE_FORMAT, "HH:mm");
                expiryTime.setText(permitExpiryTime);
            } else {
                expiryTimeLayout.setVisibility(View.GONE);
            }

            Date expDate, todaysDate;

            if (paidParking.type.equalsIgnoreCase("Ringo")) {
                expDate = DateUtils.getDate(paidParking.exp, UNIVERSAL_DATE_FORMAT);
                todaysDate = DateUtils.getDate(DateUtils.getFormatedDate(new Date(), UNIVERSAL_DATE_FORMAT), UNIVERSAL_DATE_FORMAT);
                if (DateUtils.compareToDay(expDate, todaysDate)) {
                    permitTextColor(true);
                } else {
                    permitTextColor(false);
                }
            } else {
                expDate = DateUtils.getDate(DateUtils.changeDateFormat(paidParking.exp, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT), DateUtils.DATE_FORMAT);
                todaysDate = DateUtils.getDate(DateUtils.getCurrentDate(), DateUtils.DATE_FORMAT);
                if (DateUtils.compareToDay(expDate, todaysDate)) {
                    permitTextColor(true);
                } else {
                    permitTextColor(false);
                }
            }
        }
    }

    public interface OnVRMLookupListener {
        void OnVRMLookupConfirmed(boolean isConfirmed, String data);
    }

    private void permitTextColor(boolean isExpired) {
        if(isExpired){
            titleType.setTextColor(Color.RED);
            type.setTextColor(Color.RED);
            titleLocation.setTextColor(Color.RED);
            location.setTextColor(Color.RED);
            titleZone.setTextColor(Color.RED);
            zone.setTextColor(Color.RED);
            titleEstate.setTextColor(Color.RED);
            estatezone.setTextColor(Color.RED);
            titleStartDate.setTextColor(Color.RED);
            startDate.setTextColor(Color.RED);
            titleExpiryDate.setTextColor(Color.RED);
            expiryDate.setTextColor(Color.RED);
            titleExpiryTime.setTextColor(Color.RED);
            expiryTime.setTextColor(Color.RED);
        } else{
            titleType.setTextColor(Color.BLACK);
            type.setTextColor(Color.BLACK);
            titleLocation.setTextColor(Color.BLACK);
            location.setTextColor(Color.BLACK);
            titleZone.setTextColor(Color.BLACK);
            zone.setTextColor(Color.BLACK);
            titleEstate.setTextColor(Color.BLACK);
            estatezone.setTextColor(Color.BLACK);
            titleStartDate.setTextColor(Color.BLACK);
            startDate.setTextColor(Color.BLACK);
            titleExpiryDate.setTextColor(Color.BLACK);
            expiryDate.setTextColor(Color.BLACK);
            titleExpiryTime.setTextColor(Color.BLACK);
            expiryTime.setTextColor(Color.BLACK);
        }
    }

}
