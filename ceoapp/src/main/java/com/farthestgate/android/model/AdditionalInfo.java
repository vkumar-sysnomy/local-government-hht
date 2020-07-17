package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 28/04/2014.
 */
public class AdditionalInfo implements Parcelable
{
    public boolean[] selectedOptions;

    public static final Parcelable.Creator<AdditionalInfo> CREATOR = new Parcelable.Creator<AdditionalInfo>() {
        @Override
        public AdditionalInfo createFromParcel(Parcel p) {
            return new AdditionalInfo(p);
        }

        @Override
        public AdditionalInfo[] newArray(int size) {
            return new AdditionalInfo[size];
        }
    };

    public AdditionalInfo(Parcel in)
    {
        selectedOptions = in.createBooleanArray();
    }


    public AdditionalInfo()
    {
        selectedOptions = new boolean[5];
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeBooleanArray(selectedOptions);
    }
}
