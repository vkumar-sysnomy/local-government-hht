package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanson Aboagye on 28/04/2014.
 */
public class DestinationInfo implements Parcelable
{
    public static final int HANDED_TO_DRIVER      = 10;
    public static final int VDA                   = 11;
    public static final int AFFIXED_TO_WINDSHIELD = 12;
    public static final int VOID = 13;
    public static final int PREVENTED_FROM_ISSUE  = 14;


    public static final int PCN_SPOILT          = 0;
    public static final int DRIVER_SEEN         = 1;
    public static final int VERBAL_ABUSE        = 2;
    public static final int PHYSICAL_ABUSE      = 3;
    public static final int ALL_WINDOWS_CHECKED = 4;


    public int[] driverInteraction;
    public Integer pcnDestination;
    public String actionPriority;
    public String actionToTake;

    public static final Parcelable.Creator<DestinationInfo> CREATOR = new Parcelable.Creator<DestinationInfo>() {
        @Override
        public DestinationInfo createFromParcel(Parcel p) {
            return new DestinationInfo(p);
        }

        @Override
        public DestinationInfo[] newArray(int size) {
            return new DestinationInfo[size];
        }
    };

    public DestinationInfo(Parcel in)
    {
        driverInteraction = in.createIntArray();
        pcnDestination = in.readInt();
        actionPriority = in.readString();
        actionToTake = in.readString();
    }


    public DestinationInfo()
    {
        pcnDestination = -1;
        driverInteraction = new int[4];
        actionPriority="Medium";
        actionToTake = "noaction";
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeIntArray(driverInteraction);
        dest.writeInt(pcnDestination);
        dest.writeString(actionPriority);
        dest.writeString(actionToTake);
    }
}
