package com.farthestgate.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.farthestgate.android.helper.DBHelper;
//import com.farthestgate.android.model.database.StreetsTable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class StreetCPZ implements Parcelable
{

    public String   noderef;
    public String   streetusrn;
    public String   streetname;
    public Boolean  removalsallowed;
    //JK: street Enforcement Patterns
    //public EnforcementPattern[]     streetEnforcementPattern;
    public List<EnforcementPattern> streetEnforcementPattern = new ArrayList<EnforcementPattern>();
    public String   contraventions;
    public List<Contravention> contraventionList = new ArrayList<Contravention>();
    public String contraJSON;
    public int verrus_code;
    public List<WarningNotice> warningNoticeConfiguration = new ArrayList<WarningNotice>();
    public String owningcpz;


    public static final Parcelable.Creator<StreetCPZ> CREATOR = new Parcelable.Creator<StreetCPZ>() {
        @Override
        public StreetCPZ createFromParcel(Parcel p) {
            return new StreetCPZ(p);
        }

        @Override
        public StreetCPZ[] newArray(int size) {
            return new StreetCPZ[size];
        }
    };

    public StreetCPZ()
    {
        contraventions = "";
        removalsallowed = true;
    }

    /*public StreetCPZ(StreetsTable res)
    {
        this();

        verrus_code = res.getVerrusCode();
        streetname = res.getStreetName();
        if (res.getRemovalsAllowed() != null)
            removalsallowed = res.getRemovalsAllowed();
        streetusrn = res.getUSRN();
        noderef = res.getNodeRef();
        contraventions = res.getContraventions();
        try {
            JSONArray contraList = new JSONArray(res.getContraJson());

            for (int x =0;x < contraList.length();x++)
            {
                try
                {
                    JSONObject resObj = contraList.getJSONObject(x);
                    Contravention contra = new Contravention();
                    contra.codeSuffixes = resObj.getString("codeSuffixes");
                    contra.contraventionType = 0;
                    contra.contraventionCode = resObj.getString("contraventionCode");
                    contra.agreementCode = resObj.getString("agreementCode");
                    contra.contraventionDescription = resObj.getString("contraventionDescription");
                    contraventionList.add(contra);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            contraJSON =  res.getContraJson();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public StreetCPZ(Street res) {
        this();

        verrus_code = res.verrusCode;
        streetname = res.streetName;
        if (res.removalsAllowed != null)
            removalsallowed = res.removalsAllowed;
        streetusrn = res.USRN;
        noderef = res.nodeRef;
        owningcpz = res.owningcpz;
        contraventions = res.contraventions;
        //JK: street Enforcement Patterns
        streetEnforcementPattern = res.enforcementPattern;
        warningNoticeConfiguration = res.warningNoticeConfiguration;
        try {
            JSONArray contraList = new JSONArray(res.contraJson);
            for (int x = 0; x < contraList.length(); x++) {
                try {
                    JSONObject resObj = contraList.getJSONObject(x);
                    Contravention contra = new Contravention();
                    contra.codeSuffixes = resObj.getString("codeSuffixes");
                    contra.contraventionType = 0;
                    contra.contraventionCode = resObj.getString("contraventionCode");
                    contra.agreementCode = resObj.getString("agreementCode");
                    contra.contraventionDescription = resObj.getString("contraventionDescription");
                    //JK: street Enforcement Patterns
                    JSONArray enforcementPatterns = resObj.has("contraventionEnforcementPattern") ? resObj.getJSONArray("contraventionEnforcementPattern") : null;
                    if (enforcementPatterns != null && enforcementPatterns.length() > 0) {
                        EnforcementPattern enforcementPattern;
                        for (int index = 0; index < enforcementPatterns.length(); index++) {
                            JSONObject pattern = enforcementPatterns.getJSONObject(index);
                            enforcementPattern = new EnforcementPattern(pattern.getString("enforcementDay"), pattern.getString("enforcementStartTime"), pattern.getString("enforcementEndTime"));
                            contra.contraventionEnforcementPattern.add(enforcementPattern);
                        }
                    }
                    contraventionList.add(contra);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            contraJSON = res.contraJson;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StreetCPZ(JSONObject inJSON) throws JSONException {

        this();

        Gson    gson  = new GsonBuilder().create();
    /*    Boolean add61 = true;
        Boolean add62 = true;
        Boolean add26 = true;
        Boolean add27 = true;
        Boolean add30 = true;
        Boolean add28 = true;
        Boolean add22 = true;
        Boolean has05 = false;

        String currentBand = "";*/
        verrus_code = inJSON.has("verrus_code")? inJSON.getInt("verrus_code"):0;
        streetname = inJSON.getString("streetname");
        streetusrn = inJSON.getString("streetusrn");
        noderef = inJSON.getString("noderef");
        owningcpz = inJSON.getString("owningcpz");
        removalsallowed = inJSON.getString("removalsallowed").equals("Yes");
        JSONArray warningNoticeConfigArray = inJSON.getJSONArray("warningnoticeconfiguration");
        warningNoticeConfiguration = new Gson().fromJson(warningNoticeConfigArray.toString(), new TypeToken<ArrayList<WarningNotice>>() {}.getType());
        //JK: street Enforcement Patterns
        JSONArray enforcementPatterns = inJSON.has("streetenforcementpattern")? inJSON.getJSONArray("streetenforcementpattern"):null;
        if(enforcementPatterns !=null && enforcementPatterns.length() > 0){
            EnforcementPattern enforcementPattern;
            for (int index = 0; index < enforcementPatterns.length(); index++) {
                JSONObject pattern = enforcementPatterns.getJSONObject(index);
                enforcementPattern = new EnforcementPattern(pattern.getString("enforcementDay"),pattern.getString("enforcementStartTime"),pattern.getString("enforcementEndTime"));
                streetEnforcementPattern.add(enforcementPattern);
            }
        }
        //JK: end street Enforcement Patterns
        JSONArray tempArray = new JSONArray();
        try
        {
            JSONArray contJSON = inJSON.getJSONArray("streetcontraventions");
            if (contJSON.length() > 0) {
                for (int x = 0; x < contJSON.length(); x++) {
                    JSONObject res = contJSON.getJSONObject(x);
                    String cToAdd = res.getString("contraventionCode");
                 //   if (!cToAdd.equals("07") && !cToAdd.equals("34"))
                        contraventions += cToAdd  + ",";
                /*    Integer cCheck = Integer.valueOf(cToAdd);
                    switch (cCheck)
                    {

                        case 5:
                        {
                            has05 = true;
                            break;
                        }
                        case 22:
                        {
                            add22 = false;
                            break;
                        }
                        case 26:
                        {
                            add26 = false;
                            break;
                        }
                        case 27:
                        {
                            add27 = false;
                            break;
                        }
                        case 28:
                        {
                            add28 = false;
                            break;
                        }
                        case 30:
                        {
                            add30 = false;
                            break;
                        }
                        case 61:
                        {
                            add61 = false;
                            break;
                        }
                        case 62:
                        {
                            add62 = false;
                            break;
                        }
                    }*/
                }
            }


            for (int x =0;x < contJSON.length();x++)
            {
                try
                {
                    Contravention contra = new Contravention();
                    JSONObject res = contJSON.getJSONObject(x);
                    String code = res.getString("contraventionCode");
                   /* if (!code.equals("07") && !code.equals("34"))
                    {*/
                        String test = DBHelper.GetSuffixes(code);
                        contra.codeSuffixes = "[";
                        for (char c: test.toCharArray())
                        {
                            contra.codeSuffixes += "\"" + c + "\",";
                        }
                        contra.codeSuffixes += "]";
                        contra.codeSuffixes = contra.codeSuffixes.replace(",]","]");
                        contra.contraventionType = 0;
                        contra.agreementCode = res.getString("agreementCode");
                        contra.contraventionCode = code;
                        try {
                            contra.contraventionDescription = DBHelper.GetContraventionsData(contra.
                                    contraventionCode).get(0).getContraventionDescription();
                        }
                        catch (ArrayIndexOutOfBoundsException aex)
                        {
                            contra.contraventionDescription = "";
                        }
                        catch (IndexOutOfBoundsException iex)
                        {
                            contra.contraventionDescription = "";
                        }
                        contra.contraventionCode = contra.contraventionCode.replace(".0", "");
                        //JK: street Enforcement Patterns
                        String bfsubgrid = res.has("bfsubgrid_0")?res.getString("bfsubgrid_0"):null;
                        if(bfsubgrid !=null && bfsubgrid.length()>0 ){
                            JSONArray bfsubgrid_0 = new JSONArray(bfsubgrid);
                            EnforcementPattern contraventionPattern;
                            for (int index = 0; index < bfsubgrid_0.length(); index++) {
                                JSONObject pattern = bfsubgrid_0.getJSONObject(index);
                                contraventionPattern = new EnforcementPattern(pattern.getString("codeEnforcementDay"),pattern.getString("codeEnforcementStartTime"),pattern.getString("codeEnforcementEndTime"));
                                contra.contraventionEnforcementPattern.add(contraventionPattern);
                            }
                        }
                        contraventionList.add(contra);
                        /* if (contra.agreementCode.contains("A"))
                            currentBand = "A";
                        else
                            currentBand = "B";*/

                        JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                        tempArray.put(cJSON);
                   //}
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
           /* if (has05)
            {
                // for each 05 there is a potential 30 and 22
                if (add22)
                {
                    contraventions += "22,";
                    Contravention contra = new Contravention();
                    contra.contraventionCode = "22";
                    contra.contraventionDescription = "Re-parked in the same parking place or zone within one hour after leaving";
                    contra.contraventionType = AppConstant.CONTRAVENTION_DUAL_LOG;
                    contra.codeSuffixes = "[\"c\",\"f\",\"l\",\"m\",\"n\",\"o\",\"p\",\"s\",\"v\"]";
                    contra.isInstant = false;
                    if (currentBand.equals("A"))
                        contra.agreementCode = Contravention.lowerA;
                    else
                        contra.agreementCode = Contravention.lowerB;
                    contraventionList.add(contra);
                    JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                    tempArray.put(cJSON);
                }
                if (add30)
                {
                    contraventions += "30,";
                    Contravention contra = new Contravention();
                    contra.contraventionCode = "30";
                    contra.contraventionDescription = "Parked for longer than permitted";
                    contra.contraventionType = AppConstant.CONTRAVENTION_DUAL_LOG;
                    contra.codeSuffixes = "[\"f\",\"l\",\"m\",\"n\",\"o\",\"p\",\"s\",\"u\"]";
                    contra.isInstant = false;
                    if (currentBand.equals("A"))
                        contra.agreementCode = Contravention.lowerA;
                    else
                        contra.agreementCode = Contravention.lowerB;
                    contraventionList.add(contra);
                    JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                    tempArray.put(cJSON);
                }

            }
            if (add26)
            {
                contraventions += "26,";
                Contravention contra = new Contravention();
                contra.contraventionCode = "26";
                contra.contraventionDescription = "Vehicle parked more than 50 centimetres from the " +
                        "edge of the carriageway and not within a designated parking place.";
                contra.contraventionType = 0;
                contra.codeSuffixes = "[]";
                contra.isInstant = true;
                if (currentBand.equals("A"))
                    contra.agreementCode = Contravention.higherA;
                else
                    contra.agreementCode = Contravention.higherB;
                contraventionList.add(contra);
                JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                tempArray.put(cJSON);
            }
            if (add27)
            {
                contraventions += "27,";
                Contravention contra = new Contravention();
                contra.contraventionCode = "27";
                contra.contraventionDescription = "The contravention occurs when a vehicle waits on " +
                        "the carriageway, adjacent to the footway where the footway, cycle track or " +
                        "verge has been lowered to meet the level of the carriageway";
                contra.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                contra.codeSuffixes = "[\"o\"]";
                contra.isInstant = true;
                if (currentBand.equals("A"))
                    contra.agreementCode = Contravention.higherA;
                else
                    contra.agreementCode = Contravention.higherB;
                contraventionList.add(contra);
                JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                tempArray.put(cJSON);
            }
            if (add61) {
                contraventions += "61,";
                Contravention contra = new Contravention();
                contra.contraventionCode = "61";
                contra.contraventionDescription = "A heavy commercial vehicle wholly or partly " +
                        "parked on a footway, verge or land in between two carriageways.";
                contra.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                contra.codeSuffixes = "[\"1\",\"2\",\"4\",\"c\",\"g\"]";
                contra.isInstant = true;
                if (currentBand.equals("A"))
                    contra.agreementCode = Contravention.higherA;
                else
                    contra.agreementCode = Contravention.higherB;
                contraventionList.add(contra);
                JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                tempArray.put(cJSON);
            }
            if (add62)
            {
                contraventions += "62,";
                Contravention contra = new Contravention();
                contra.contraventionCode = "62";
                contra.contraventionDescription = "Parked with one or more wheels on or over a " +
                        "footpath or any part of the road other than a carriageway.";
                contra.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                contra.codeSuffixes = "[\"1\",\"2\",\"4\",\"c\",\"g\"]";
                contra.isInstant = true;
                if (currentBand.equals("A"))
                    contra.agreementCode = Contravention.higherA;
                else
                    contra.agreementCode = Contravention.higherB;
                contraventionList.add(contra);
                JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                tempArray.put(cJSON);
            }
            if (add28) {
                contraventions += "28,";
                Contravention contra = new Contravention();
                contra.contraventionCode = "28";
                contra.contraventionDescription = "Parked in a special enforcement area on part of " +
                        "the carriageway raised to meet the level of a footway, cycle track or verge";
                contra.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                contra.codeSuffixes = "[\"o\"]";
                contra.isInstant = true;
                if (currentBand.equals("A"))
                    contra.agreementCode = Contravention.higherA;
                else
                    contra.agreementCode = Contravention.higherB;
                contraventionList.add(contra);
                JSONObject cJSON = new JSONObject(gson.toJson(contra,Contravention.class));
                tempArray.put(cJSON);
            }*/
            contraventions = contraventions.substring(0,contraventions.length()-1);
            contraJSON =  tempArray.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public StreetCPZ(Parcel in)
    {
        verrus_code = in.readInt();
        streetusrn = in.readString();
        streetname = in.readString();
        removalsallowed = in.readByte() != 0;
        //JK: street Enforcement Patterns
        //streetEnforcementPattern = (EnforcementPattern[]) in.readParcelableArray(EnforcementPattern.class.getClassLoader());
        in.readTypedList(streetEnforcementPattern,EnforcementPattern.CREATOR);
        contraventions = in.readString();
        in.readTypedList(contraventionList, Contravention.CREATOR);
        contraJSON = in.readString();
        in.readTypedList(warningNoticeConfiguration, WarningNotice.CREATOR);
   }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(verrus_code);
        dest.writeString(streetusrn);
        dest.writeString(streetname);
        dest.writeByte((byte) (removalsallowed ? 1 : 0));
        //JK: street Enforcement Patterns
        //dest.writeParcelableArray(streetEnforcementPattern,0);
        dest.writeTypedList(streetEnforcementPattern);
        dest.writeString(contraventions);
        dest.writeTypedList(contraventionList);
        dest.writeString(contraJSON);
        dest.writeTypedList(warningNoticeConfiguration);

    }
}
