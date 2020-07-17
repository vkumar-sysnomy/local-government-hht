package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class VehicleModel implements Parcelable
{
    public String modelName;
    public Integer modelMakeID;

    public static final Parcelable.Creator<VehicleModel> CREATOR = new Parcelable.Creator<VehicleModel>() {
        @Override
        public VehicleModel createFromParcel(Parcel p) {
            return new VehicleModel(p);
        }

        @Override
        public VehicleModel[] newArray(int size) {
            return new VehicleModel[size];
        }
    };

    public VehicleModel()
    {
    }


    public VehicleModel(Parcel in)
    {
        modelMakeID  = in.readInt();
        modelName = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(modelMakeID);
        dest.writeString(modelName);
    }
}
