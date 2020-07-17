package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson on 22/05/2014.
 */
public class Timeplate implements Parcelable {

    public long maxTime;
    public long noReturnTime;

    public static final Parcelable.Creator<Timeplate> CREATOR = new Parcelable.Creator<Timeplate>() {
        @Override
        public Timeplate createFromParcel(Parcel p) {
            return new Timeplate(p);
        }

        @Override
        public Timeplate[] newArray(int size) {
            return new Timeplate[size];
        }
    };

    public Timeplate()
    {
        maxTime = 0;
        noReturnTime = 0;
    }

    public Timeplate(Parcel in)
    {
        maxTime  = in.readLong();
        noReturnTime = in.readLong();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(maxTime);
        dest.writeLong(noReturnTime);
    }
}
