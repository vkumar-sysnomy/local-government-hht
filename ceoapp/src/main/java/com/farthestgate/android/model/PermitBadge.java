package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 */
public class PermitBadge implements Parcelable
{
    public enum PERMIT_TYPE
    {
        TAX_DISC,
        PARKING_PERMIT,
        DISABLED_BADGE
    }

    public String expiryDate;
    public String   serialNo;
    public Boolean isSeen;
    public long     dateMillis;
    public PERMIT_TYPE permitType;

    public static final Parcelable.Creator<PermitBadge> CREATOR = new Parcelable.Creator<PermitBadge>() {
        @Override
        public PermitBadge createFromParcel(Parcel p) {
            return new PermitBadge(p);
        }

        @Override
        public PermitBadge[] newArray(int size) {
            return new PermitBadge[size];
        }
    };

    //TODO: need to write differentiation
    public PermitBadge()
    {
        isSeen = false;
    }


    public PermitBadge(Parcel in)
    {
        expiryDate  = in.readString();
        serialNo    = in.readString();
        isSeen = in.readByte() != 0;
        dateMillis  = in.readLong();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(expiryDate);
        dest.writeString(serialNo);
        dest.writeByte((byte)(isSeen ? 1:0));
        dest.writeLong(dateMillis);
    }

}
