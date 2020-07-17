package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 */
public class Location implements Parcelable
{

    public StreetCPZ streetCPZ;
    public String outside;


    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel p) {
            return new Location(p);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public Location()    {

    }


    public Location(Parcel in)
    {
        streetCPZ  = in.readParcelable(StreetCPZ.class.getClassLoader());
        outside    = in.readString();

    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(streetCPZ,0);
        dest.writeString(outside);

    }

}
