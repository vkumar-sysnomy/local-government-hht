package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 */
public class PDTicket implements Parcelable
{
    public String expiryTime;
    public String   serialNo;
    public long     timeMillis;

    public static final Parcelable.Creator<PDTicket> CREATOR = new Parcelable.Creator<PDTicket>() {
        @Override
        public PDTicket createFromParcel(Parcel p) {
            return new PDTicket(p);
        }

        @Override
        public PDTicket[] newArray(int size) {
            return new PDTicket[size];
        }
    };

    public PDTicket()
    {
    }


    public PDTicket(Parcel in)
    {
        expiryTime  = in.readString();
        serialNo    = in.readString();
        timeMillis  = in.readLong();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(expiryTime);
        dest.writeString(serialNo);
        dest.writeLong(timeMillis);
    }
}
