package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 */
public class TaxDisc implements Parcelable
{
    public String expiryDate;
    public String   serialNo;
    public Boolean isSeen;
    public long     dateMillis;

    public static final Parcelable.Creator<TaxDisc> CREATOR = new Parcelable.Creator<TaxDisc>() {
        @Override
        public TaxDisc createFromParcel(Parcel p) {
            return new TaxDisc(p);
        }

        @Override
        public TaxDisc[] newArray(int size) {
            return new TaxDisc[size];
        }
    };

    public TaxDisc()
    {
        isSeen = false;
    }


    public TaxDisc(Parcel in)
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

