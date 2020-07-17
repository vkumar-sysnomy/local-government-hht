package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 8/1/2016.
 */
public class WarningNotice implements Parcelable{
    public String contraventionCode;
    public String warningStartDate;
    public String warningEndDate;
    public String maxPerVRM;


    protected WarningNotice(Parcel in) {
        contraventionCode = in.readString();
        warningStartDate = in.readString();
        warningEndDate = in.readString();
        maxPerVRM = in.readString();
    }

    public static final Creator<WarningNotice> CREATOR = new Creator<WarningNotice>() {
        @Override
        public WarningNotice createFromParcel(Parcel in) {
            return new WarningNotice(in);
        }

        @Override
        public WarningNotice[] newArray(int size) {
            return new WarningNotice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contraventionCode);
        dest.writeString(warningStartDate);
        dest.writeString(warningEndDate);
        dest.writeString(maxPerVRM);
    }
}
