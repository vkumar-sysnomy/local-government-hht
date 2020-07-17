package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.DBHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class PCN implements Parcelable
{
    public String pcnNumber;
    public boolean isUsed;
    public boolean diagramNoteTaken;
    public Location location;
    public VehicleManufacturer manufacturer;
    public VehicleModel model;
    public String colourName;
    public Contravention contravention;
    public Integer valveFront;
    public Integer valveBack;
    public List<PDTicket> pdTicketsList;
    public List<PermitBadge> permitBadgeList;
    public List<TaxDisc> taxDiscs;
    public long logTime;
    public long issueTime;
    public String registrationMark;
    public DestinationInfo dInfo;
    public long observationTime;
    public List<String> photoTimeStamps;
    public Integer observationNumber;
    public Integer fullPrice;
    public Integer halfPrice;
    public String chargeBand;
    public AdditionalInfo additionalInfo;
    public Timeplate timeplateInfo;
    public Integer valveFront2;
    public Integer valveBack2;
    public Location location2;
    public String countryCode;
    public String ticketNotes;
    public String countryName;
    public long receivedTime;
    public double gpsLat;
    public double gpsLong;
    public String ceoShoulderNumber;
    public String locationStreetName;
    public String imeiNumber;
    public String warningNotice;
    public String contraventionChanged;
    public String firstParkingSessionCheck;
    public String secondParkingSessionCheck;


    public static final Parcelable.Creator<PCN> CREATOR = new Parcelable.Creator<PCN>()
    {
        @Override
        public PCN createFromParcel(Parcel p)
        {
            return new PCN(p);
        }

        @Override
        public PCN[] newArray(int size)
        {
            return new PCN[size];
        }
    };

    public PCN()
    {
        pdTicketsList   = new ArrayList<PDTicket>();
        permitBadgeList = new ArrayList<PermitBadge>();
        taxDiscs        = new ArrayList<TaxDisc>();
        photoTimeStamps = new ArrayList<String>();
        issueTime       = 0;
        contravention   = new Contravention();
        chargeBand      = "";
        additionalInfo  = new AdditionalInfo();
        dInfo           = new DestinationInfo();
        timeplateInfo   = new Timeplate();
        valveBack2      = 0;
        valveFront2     = 0;
        isUsed          = false;
        diagramNoteTaken = false;
        countryCode     = "GBR";
        ticketNotes     = "";
        countryName     = "United Kingdom";
        receivedTime    = 0l;
        gpsLat          = 0;
        gpsLong         = 0;
        ceoShoulderNumber= DBHelper.getCeoUserId();//CeoApplication.CEOLoggedIn.userId;
        locationStreetName = "";
        imeiNumber = CeoApplication.getUUID();
        warningNotice = "false";
        contraventionChanged = "false";
    }

    public PCN(Integer obs)
    {
        this();
        observationNumber = obs;
    }

    public String toJSON()
    {
        Gson gson = new GsonBuilder().create();
        String res = gson.toJson(this,PCN.class);

        return res;
    };

    public PCN(Parcel in)
    {
        this();
        pcnNumber = in.readString();
        isUsed = in.readByte() != 0;
        diagramNoteTaken = in.readByte() != 0;
        location = in.readParcelable(Location.class.getClassLoader());
        manufacturer = in.readParcelable(VehicleManufacturer.class.getClassLoader());
        model = in.readParcelable(VehicleModel.class.getClassLoader());
        colourName          = in.readString();
        contravention       = in.readParcelable(Contravention.class.getClassLoader());
        valveFront          = in.readInt();
        valveBack           = in.readInt();
        in.readList(pdTicketsList, PDTicket.class.getClassLoader());
        in.readList(permitBadgeList, PermitBadge.class.getClassLoader());
        in.readList(taxDiscs, TaxDisc.class.getClassLoader());
        logTime             = in.readLong();
        issueTime           = in.readLong();
        registrationMark    = in.readString();
        dInfo               = in.readParcelable(DestinationInfo.class.getClassLoader());
        observationTime = in.readLong();
        in.readStringList(photoTimeStamps);
        observationNumber   = in.readInt();
        fullPrice           = in.readInt();
        halfPrice           = in.readInt();
        chargeBand          = in.readString();
        additionalInfo      = in.readParcelable(AdditionalInfo.class.getClassLoader());
        timeplateInfo       = in.readParcelable(Timeplate.class.getClassLoader());
        valveFront2         = in.readInt();
        valveBack2          = in.readInt();
        location2           = in.readParcelable(Location.class.getClassLoader());
        countryCode         = in.readString();
        ticketNotes         = in.readString();
        receivedTime        = in.readLong();
        gpsLat              = in.readDouble();
        gpsLong             = in.readDouble();
        ceoShoulderNumber   = in.readString();
        locationStreetName  = in.readString();
        imeiNumber          = in.readString();
        warningNotice       = in.readString();
        contraventionChanged = in.readString();
        firstParkingSessionCheck = in.readString();
        secondParkingSessionCheck = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(pcnNumber);
        dest.writeByte((byte) (isUsed ? 1 : 0));
        dest.writeByte((byte) (diagramNoteTaken ? 1 : 0));
        dest.writeParcelable(location, 0);
        dest.writeParcelable(manufacturer, 0);
        dest.writeParcelable(model, 0);
        dest.writeString(colourName);
        dest.writeParcelable(contravention, 0);
        dest.writeInt(valveFront);
        dest.writeInt(valveBack);
        dest.writeList(pdTicketsList);
        dest.writeList(permitBadgeList);
        dest.writeList(taxDiscs);
        dest.writeLong(logTime);
        dest.writeLong(issueTime);
        dest.writeString(registrationMark);
        dest.writeParcelable(dInfo, 0);
        dest.writeLong(observationTime);
        dest.writeStringList(photoTimeStamps);
        dest.writeInt(observationNumber);
        dest.writeInt(fullPrice);
        dest.writeInt(halfPrice);
        dest.writeString(chargeBand);
        dest.writeParcelable(additionalInfo, 0);
        dest.writeParcelable(timeplateInfo, 0);
        dest.writeInt(valveFront2);
        dest.writeInt(valveBack2);
        dest.writeParcelable(location2, 0);
        dest.writeString(countryCode);
        dest.writeString(ticketNotes);
        dest.writeLong(receivedTime);
        dest.writeDouble(gpsLat);
        dest.writeDouble(gpsLong);
        dest.writeString(ceoShoulderNumber);
        dest.writeString(locationStreetName);
        dest.writeString(imeiNumber);
        dest.writeString(warningNotice);
        dest.writeString(contraventionChanged);
        dest.writeString(firstParkingSessionCheck);
        dest.writeString(secondParkingSessionCheck);
    }

    public Boolean hasOSLocation()
    {
        return location.outside != null;
    }

    public Boolean hasValvePositions()
    {
        if ((valveFront != null))
            if ((valveBack != null))
            {
                return true;
            } else
            {
                return false;
            }
        else
        {
            return false;
        }
    }
    public Boolean hasOSLocation2()
    {
        return location2.outside != null;
    }

    public Boolean hasValvePositions2()
    {
        if ((valveFront2 != 0))
            if ((valveBack2 != 0))
            {
                return true;
            } else
            {
                return false;
            }
        else
        {
            return false;
        }
    }

}