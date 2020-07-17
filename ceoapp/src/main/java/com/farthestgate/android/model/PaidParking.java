package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 18/12/15.
 */
public class PaidParking implements Parcelable {
    public String vrm;
    public String make;
    public String model;
    public String colour;
    public String zone;
    public String type;
    public String exp;
    public String group;
    public String cashlessloc;
    public String cashlesslocname;
    public String status;
    public String timepaid;
    public String amountpaid;
    public String start;
    public String estatezone;
    public List<Messages> messages = new ArrayList<Messages>();


    public static final Parcelable.Creator<PaidParking> CREATOR = new Parcelable.Creator<PaidParking>() {
        @Override
        public PaidParking createFromParcel(Parcel p) {
            return new PaidParking(p);
        }

        @Override
        public PaidParking[] newArray(int size) {
            return new PaidParking[size];
        }
    };

    public PaidParking()
    {
    }


    public PaidParking(Parcel in)
    {
        vrm  = in.readString();
        make = in.readString();
        model = in.readString();
        colour = in.readString();
        zone = in.readString();
        type = in.readString();
        exp = in.readString();
        group = in.readString();
        cashlessloc = in.readString();
        cashlesslocname = in.readString();
        status = in.readString();
        timepaid = in.readString();
        amountpaid = in.readString();
        start = in.readString();
        estatezone = in.readString();
        in.readTypedList(messages, Messages.CREATOR);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(vrm);
        dest.writeString(make);
        dest.writeString(model);
        dest.writeString(colour);
        dest.writeString(zone);
        dest.writeString(type);
        dest.writeString(exp);
        dest.writeString(group);
        dest.writeString(cashlessloc);
        dest.writeString(cashlesslocname);
        dest.writeString(status);
        dest.writeString(timepaid);
        dest.writeString(amountpaid);
        dest.writeString(start);
        dest.writeString(estatezone);
        dest.writeTypedList(messages);
    }

    public String toJSON()
    {
        Gson gson = new GsonBuilder().create();
        String res = gson.toJson(this,PaidParking.class);
        return res;
    }

}
