package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.farthestgate.android.model.database.ContraventionsTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class Contravention implements Parcelable
{

    public static final String higherA = "HIGHER-A";
    public static final String lowerA = "LOWER-A";
    public static final String higherB = "HIGHER-B";
    public static final String lowerB = "LOWER-B";

    public String   contraventionCode;
    public String   contraventionDescription;
    public Integer  contraventionType;

    public String   codeSuffixDescription;
    public String   codeSuffixes;
    public String   selectedSuffix;
    public Boolean  isInstant;
    public String   agreementCode;
    //JK: street Enforcement Patterns
    public List<EnforcementPattern> contraventionEnforcementPattern = new ArrayList<EnforcementPattern>();

    public static final Parcelable.Creator<Contravention> CREATOR = new Parcelable.Creator<Contravention>() {
        @Override
        public Contravention createFromParcel(Parcel p) {
            return new Contravention(p);
        }

        @Override
        public Contravention[] newArray(int size) {
            return new Contravention[size];
        }
    };

    public Contravention()
    {
        isInstant = false;
        codeSuffixDescription = "";
        contraventionCode = "TEST";
        agreementCode = lowerA;
        selectedSuffix = "";
    }

    public Contravention(ContraventionsTable data){
        this();

        contraventionCode           = data.getContraventionCode();
        contraventionDescription    = data.getContraventionDescription();
        contraventionType           = data.getEnforcementType();
        codeSuffixes                = data.getSuffixes();
        selectedSuffix              = "";
        agreementCode               = data.getAgreementCode();
    }


    public Contravention(Parcel in)
    {
        contraventionCode = in.readString();
        contraventionDescription = in.readString();
        contraventionType = in.readInt();
        codeSuffixDescription = in.readString();
        codeSuffixes = in.readString();
        selectedSuffix = in.readString();
        isInstant = in.readByte() != 0;
        agreementCode = in.readString();
        //JK: street Enforcement Patterns
        in.readTypedList(contraventionEnforcementPattern,EnforcementPattern.CREATOR);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(contraventionCode);
        dest.writeString(contraventionDescription);
        dest.writeInt(contraventionType);
        dest.writeString(codeSuffixDescription);
        dest.writeString(codeSuffixes);
        dest.writeString(selectedSuffix);
        dest.writeByte((byte) (isInstant ? 1 : 0));
        dest.writeString(agreementCode);
        //JK: street Enforcement Patterns
        dest.writeTypedList(contraventionEnforcementPattern);
    }
}
