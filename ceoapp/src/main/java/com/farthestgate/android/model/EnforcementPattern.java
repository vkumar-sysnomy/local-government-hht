package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class EnforcementPattern implements Parcelable
{
    public String enforcementDay;
    public String enforcementStartTime;
    public String enforcementEndTime;


    public static final Parcelable.Creator<EnforcementPattern> CREATOR = new Parcelable.Creator<EnforcementPattern>() {
        @Override
        public EnforcementPattern createFromParcel(Parcel p) {
            return new EnforcementPattern(p);
        }

        @Override
        public EnforcementPattern[] newArray(int size) {
            return new EnforcementPattern[size];
        }
    };

    public EnforcementPattern()
    {
    }

    public EnforcementPattern(String Day, String startTime, String endTime)
    {
        enforcementDay = Day;
        enforcementEndTime = endTime;
        enforcementStartTime = startTime;
    }

    public EnforcementPattern(Parcel in)
    {
        enforcementDay = in.readString();
        enforcementStartTime = in.readString();
        enforcementEndTime = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(enforcementDay);
        dest.writeString(enforcementStartTime);
        dest.writeString(enforcementEndTime);

    }
}
