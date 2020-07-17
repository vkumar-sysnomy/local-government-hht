package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.model.PermitBadge;
import com.farthestgate.android.model.TaxDisc;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Hanson Aboagye on 22/04/2014.
 */
public class PermitBadgeInfoDialog extends DialogFragment
{
    public interface OnExtraInfoEntered{
        void OnTaxEntered(DateTime expiry, String taxNum);
        void OnPermitEntered(DateTime expiry, String permitNum);
        void OnDBadgeEntered(DateTime expiry, String badgeNum);
        void OnTaxEdited(Long expiry, String taxNum);
        void OnDBadgeEdited(Long expiry, String badgeNum);
        void OnPermitEdited(Long expiry, String permitNum);

    }

    private FrameLayout patch;
    private RelativeLayout dpLayout;
    private PermitBadge.PERMIT_TYPE infoType;
    private OnExtraInfoEntered onExtraInfoEnteredListener;

    private DatePicker  datePicker;
    private EditText    serialNum;

    public PermitBadgeInfoDialog(PermitBadge.PERMIT_TYPE permit_type)
    {
        super();
        infoType = permit_type;
    }

    public static PermitBadgeInfoDialog NewInstance(Object existingItem, PermitBadge.PERMIT_TYPE permitType)
    {
        PermitBadgeInfoDialog nDialog = new PermitBadgeInfoDialog(permitType);
        Bundle args = new Bundle();
        if (permitType == PermitBadge.PERMIT_TYPE.PARKING_PERMIT)
            args.putParcelable("permit",((PermitBadge)existingItem));
        else
            args.putParcelable("tax", ((TaxDisc)existingItem));
        nDialog.setArguments(args);

        return nDialog;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_permit_badge_dialog, null);
        LinearLayout options    = (LinearLayout) v.findViewById(R.id.layoutOptions);
        patch                   = (FrameLayout) v.findViewById(R.id.patchLayout);
        dpLayout                = (RelativeLayout) v.findViewById(R.id.dpLayout);
        RadioButton checkPermit = (RadioButton) v.findViewById(R.id.radioPermit);
        RadioButton checkBadge  = (RadioButton) v.findViewById(R.id.radioBadge);
        TextView header         = (TextView) v.findViewById(R.id.textDlgHeader);
        datePicker              = (DatePicker) v.findViewById(R.id.dpExpiry);
        serialNum               = (EditText) v.findViewById(R.id.editNumber);

        //TODO; make use of polymorphism !!!

        if (getArguments().containsKey("permit")) {
            PermitBadge info = getArguments().getParcelable("permit");

            if (info != null) {
                DateTime tDate = new DateTime(info.dateMillis);
                serialNum.setText(info.serialNo);
                datePicker.updateDate(tDate.getYear(),tDate.getMonthOfYear()-1,tDate.getDayOfMonth());
                checkPermit.setChecked(info.permitType == PermitBadge.PERMIT_TYPE.PARKING_PERMIT);
                checkBadge.setChecked(info.permitType == PermitBadge.PERMIT_TYPE.DISABLED_BADGE);

            }
        }
        if (getArguments().containsKey("tax")) {
            TaxDisc info = getArguments().getParcelable("tax");

            if (info != null) {
                serialNum.setText(info.serialNo);
                DateTime tDate = new DateTime(info.dateMillis);
                datePicker.updateDate(tDate.getYear(),tDate.getMonthOfYear()-1,tDate.getDayOfMonth());

            }
        }

        checkBadge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                    infoType = PermitBadge.PERMIT_TYPE.DISABLED_BADGE;
            }
        });
        checkPermit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                    infoType = PermitBadge.PERMIT_TYPE.PARKING_PERMIT;
            }
        });

        if (infoType == PermitBadge.PERMIT_TYPE.TAX_DISC)
        {
            header.setText("Enter Tax Details:");
            serialNum.setHint("Tax Disc Number");
            options.setVisibility(View.GONE);
            datePicker.setMaxDate(new DateTime().plusMonths(12).getMillis());
            patch.setBackgroundColor(Color.parseColor("#FFEEEEEE"));
            /*RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getActivity(),dpLayout.generateLayoutParams());
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.setMargins(30,1,0,0);*/
            //datePicker.setLayoutParams(lp);
        }

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth();
                        int year =  datePicker.getYear();

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        Long selectedDate = calendar.getTimeInMillis();


                        String serial = serialNum.getText().toString();
                        switch (infoType)
                        {
                            case TAX_DISC:
                            {
                                onExtraInfoEnteredListener.OnTaxEdited(selectedDate, serial);
                                break;
                            }
                            case PARKING_PERMIT:
                            {
                                onExtraInfoEnteredListener.OnPermitEdited(selectedDate, serial);
                                break;
                            }
                            case DISABLED_BADGE:
                            {
                                onExtraInfoEnteredListener.OnDBadgeEdited(selectedDate, serial);
                                break;
                            }
                        }
                        PermitBadgeInfoDialog.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        PermitBadgeInfoDialog.this.getDialog().cancel();
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
            onExtraInfoEnteredListener = (OnExtraInfoEntered) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
