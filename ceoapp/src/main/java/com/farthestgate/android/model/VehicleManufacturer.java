package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
//TODO: refactor this properly
public class VehicleManufacturer implements Parcelable
{
    public Integer id;
    public String name;


    public static final Parcelable.Creator<VehicleManufacturer> CREATOR = new Parcelable.Creator<VehicleManufacturer>() {
        @Override
        public VehicleManufacturer createFromParcel(Parcel p) {
            return new VehicleManufacturer(p);
        }

        @Override
        public VehicleManufacturer[] newArray(int size) {
            return new VehicleManufacturer[size];
        }
    };

    public VehicleManufacturer()
    {
    }


    public VehicleManufacturer(Parcel in)
    {
        id = in.readInt();
        name  = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(name);
    }
}
