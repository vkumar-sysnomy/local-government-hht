package com.farthestgate.android.helper;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.farthestgate.android.R;
import com.farthestgate.android.model.Messages;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 18/12/15.
 */


public class VRMLookupAdapter extends BaseAdapter {
    ArrayList<PaidParking> paidParkingList;
    private ArrayList<PaidParking> arrayList;
    LayoutInflater inflater;
    Context context;
    private static final String UNIVERSAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String SIMPLE_DATE_TIME_FORMAT = "dd/MM/yyyy'T'HH:mm";
    private MessageViewClickListener listener;

    public VRMLookupAdapter(Context context, ArrayList<PaidParking> paidParkingList) {
        this.context = context;
        this.listener = (MessageViewClickListener)context;
        Collections.sort(paidParkingList, new VRMComparator());
        this.paidParkingList = paidParkingList;
        inflater = LayoutInflater.from(this.context);
        this.arrayList = new ArrayList<PaidParking>();
        this.arrayList.addAll(paidParkingList);

    }

    public int getCount() {
        return paidParkingList.size();
    }

    public PaidParking getItem(int position) {
        return paidParkingList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder mViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.vrm_lookup_custom_row, null);
            mViewHolder = new Holder();
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (Holder) convertView.getTag();
        }
        mViewHolder.paidParking = paidParkingList.get(position);
        mViewHolder.TextViewVRM = textViewWork(convertView, R.id.textViewVRM, paidParkingList.get(position).vrm);
        mViewHolder.TextViewType = textViewWork(convertView, R.id.textViewType, paidParkingList.get(position).type);
        mViewHolder.imageViewMsg = (ImageView) convertView.findViewById(R.id.imageViewMsg);
        String exp = paidParkingList.get(position).exp != null && !paidParkingList.get(position).exp.isEmpty() ? paidParkingList.get(position).exp : "";
        // Changed by SG for display message on adapter
        List<Messages> messages = mViewHolder.paidParking.messages;
        if(messages!=null && messages.size() > 0){
            mViewHolder.imageViewMsg.setVisibility(View.VISIBLE);
            mViewHolder.imageViewMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.vrmMsgClick(v, mViewHolder.paidParking);
                    }
                }
            });
        }else{
            mViewHolder.imageViewMsg.setVisibility(View.INVISIBLE);
        }
        if (!exp.isEmpty()) {
            try {
                Date expDate = new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).parse(exp);
                String expDateStr = new SimpleDateFormat(SIMPLE_DATE_TIME_FORMAT).format(expDate);
                String[] expParts = expDateStr.split("T");
                if (expParts[1].equalsIgnoreCase("00:00")) {
                    mViewHolder.TextViewExp = textViewWork(convertView, R.id.textViewExp, expParts[0]);
                } else {
                    mViewHolder.TextViewExp = textViewWork(convertView, R.id.textViewExp, expParts[1]);
                }

                Date todaysDate;
                if(mViewHolder.TextViewType.getText().toString().equalsIgnoreCase("Ringo")) {
                    todaysDate = DateUtils.getDate(new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).format(new Date()), UNIVERSAL_DATE_FORMAT);
                    if (DateUtils.compareToDay(expDate, todaysDate)) {
                        permitTextColor(mViewHolder, true);
                    } else {
                        permitTextColor(mViewHolder, false);
                    }
                } else {
                    Date expDateOnly = DateUtils.getDate(DateUtils.changeDateFormat(exp, UNIVERSAL_DATE_FORMAT, DateUtils.DATE_FORMAT), DateUtils.DATE_FORMAT);
                    todaysDate = DateUtils.getDate(DateUtils.getCurrentDate(), DateUtils.DATE_FORMAT);
                    if (DateUtils.compareToDay(expDateOnly, todaysDate)) {
                        permitTextColor(mViewHolder, true);
                    } else {
                        permitTextColor(mViewHolder, false);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                mViewHolder.TextViewExp = textViewWork(convertView, R.id.textViewExp, paidParkingList.get(position).exp);
            }
        } else {
            mViewHolder.TextViewExp = textViewWork(convertView, R.id.textViewExp, paidParkingList.get(position).exp);
        }
        return convertView;
    }

    private TextView textViewWork(View v, int resId, String text) {
        TextView tv = (TextView) v.findViewById(resId);
        tv.setText(text);
        return tv;
    }

    public class Holder {
        TextView TextViewVRM;
        TextView TextViewType;
        TextView TextViewExp;
        ImageView imageViewMsg;
        public PaidParking paidParking;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        paidParkingList.clear();
        if (charText.equalsIgnoreCase("None")) {
            paidParkingList.addAll(arrayList);
        }else if (charText.equalsIgnoreCase("Exp")) {
            for (PaidParking parking : arrayList) {
                Date expDate = DateUtils.getDate(parking.exp, UNIVERSAL_DATE_FORMAT);
                Date todaysDate = DateUtils.getDate(new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).format(new Date()), UNIVERSAL_DATE_FORMAT);

                if(DateUtils.compareToDay(expDate, todaysDate))
                    paidParkingList.add(parking);
            }
        } else {
            for (PaidParking parking : arrayList) {
                if (parking.group.toLowerCase(Locale.getDefault()).contains(charText) || parking.cashlesslocname.toLowerCase(Locale.getDefault()).contains(charText)) {
                    paidParkingList.add(parking);
                }
            }
        }
        notifyDataSetChanged();
    }

    /*public static boolean compareToDay(Date expDate, Date todays) {
        if (expDate == null) {
            return false;
        } else if(expDate.before(todays)){
            return true;
        }
        return false;
    }

    public static Date getDate(String date, String format) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat(format).parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return parsedDate;

    }*/

    private void permitTextColor(Holder mViewHolder, boolean isExpired){
        if(isExpired){
            mViewHolder.TextViewVRM.setTextColor(Color.RED);
            mViewHolder.TextViewType.setTextColor(Color.RED);
            mViewHolder.TextViewExp.setTextColor(Color.RED);
        } else{
            mViewHolder.TextViewVRM.setTextColor(Color.BLACK);
            mViewHolder.TextViewType.setTextColor(Color.BLACK);
            mViewHolder.TextViewExp.setTextColor(Color.BLACK);
        }

    }

    public interface MessageViewClickListener {
        void vrmMsgClick(View view, PaidParking paidParking);
    }
}

class VRMComparator implements Comparator{
    @Override
    public int compare(Object o1,Object o2){
        PaidParking p1 = (PaidParking) o1;
        PaidParking p2 = (PaidParking) o2;
        return p1.vrm.compareTo(p2.vrm);
    }
}




