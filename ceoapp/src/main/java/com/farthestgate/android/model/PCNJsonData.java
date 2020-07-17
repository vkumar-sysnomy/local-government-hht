package com.farthestgate.android.model;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

/**
 * Created by Hanson on 06/05/2014.
 */
public class PCNJsonData {

    public String streetnoderef;
    public String ceonoderef;// ceo document noderef
    public String ticketserialnumber;//ticket refernece
    public String tickettype = "hht";
    public String contraventioncode;
    public String contraventiondescription;
    public String contraventionsuffix = "";
    public String contraventionchargecode;
    public String vrm;
    public String make;
    public String model;
    public String colour;
    public String foreignvehicle; //""Yes/No"
    public String foreignvehiclecountry = ""; //Vehicle ISO 3166-1 Alpha-3 country code",
    public String diplomaticvehicle;//""Yes/No",
    public String observationstarts;//":"ISO8601datetime",
    public String observationends; //":"ISO8601datetime",
    public String taxdiscserialnumber = "";
    public String taxdiskexpiry;//":"expiry mm/yy",
    public String panddexpiry;//":"ISO8601datetime P&D Ticket expiry date / time",
    public String panddserialnumber;//":"P&D Ticket number",
    public String panddmachine = "";//":"P&D Machine ID",
    public String valvepositionfront;//":"Valve position front",
    public String valvepositionrear;//":"Valve position rear”,
    public String onorby  = "";//":"On or by",
    public String onorbyjunction = ""; //":"On or by junction of",
    public String sideofroad = ""; // ":"Side of road",
    public String facing = "";//":"Facing",
    public String osoropp = ""; //":"Outside or opposite",
    public String osoppwhere = ""; //":"Outside or opposite where",
    public String distancefrom = ""; //":"Distance from",
    public String direction = "";//":"Direction",
    public String parkedonfootway = "No"; //":"Yes/No",
    public String loadingorunloading = "No"; //":"Yes/No",
    public String parkedagainstflow= "No"; //":"Yes/No",
    public String brokendown = "No"; //":"Yes/No",
    public String driverseen = "No"; //":"Yes/No",
    public String vehicledrivenaway = "No"; //":"Yes/No",
    public String contraventiondateandtime = ""; //public String ":"ISO8601datetime",
    public String ticketissuedateandtime = ""; //":"ISO8601datetime",
    public String methodofissue = ""; //":"Method of issue (affix to vehicle, hand to driver, post)",
    public String numberofphotostaken = ""; //":"number",
    public String ticketnotes = ""; //":"long text"
    public String physicalabuse = "No";
    public String pcnspoilt = "No";
    public String allwindows = "No";
    public String foreignvehiclecountryname = "";
    public String verbalabuse = "";
    public String receivedTime = "";
    public String actiontotake = "noaction";
    public String actionpriority = "5";
    public String gpslat = "0";
    public String gpslong = "0";
    public String ceoshouldernumber= DBHelper.getCeoUserId();//CeoApplication.CEOLoggedIn.userId;
    public String locationstreetname="";
    public String imeinumber=CeoApplication.getUUID();
    public String warningnotice = "false";
    public String firstparkingsessioncheck;
    public String secondparkingsessioncheck;

    public PCNJsonData()
    {  }

    public PCNJsonData(PCN pcnInfo)
    {
        streetnoderef = pcnInfo.location.streetCPZ.noderef;
        if(CeoApplication.CEOLoggedIn!=null){
            ceonoderef = CeoApplication.CEOLoggedIn.noderef;
        }else{
            Ceo ceo = DBHelper.getCeoNodeRef();
            ceonoderef = ceo!=null?ceo.noderef:"";
        }
        ticketserialnumber = pcnInfo.pcnNumber;
        contraventioncode  = pcnInfo.contravention.contraventionCode;
        contraventiondescription = pcnInfo.contravention.contraventionDescription;
        contraventionsuffix = pcnInfo.contravention.selectedSuffix;
        contraventionchargecode = pcnInfo.contravention.agreementCode;
        vrm = pcnInfo.registrationMark;
        make = pcnInfo.manufacturer.name;
        model = pcnInfo.model.modelName;
        colour = pcnInfo.colourName;

        /*if (pcnInfo.contravention.contraventionType != AppConstant.CONTRAVENTION_INSTANT)
            observationstarts = new DateTime(pcnInfo.logTime).toString();//":"ISO8601datetime",
        else
            observationstarts = new DateTime(pcnInfo.issueTime).toString();//":"ISO8601datetime",*/

        long issueTime=pcnInfo.issueTime>0?pcnInfo.issueTime:DateTime.now().getMillis();

        if (pcnInfo.observationTime != 0)
            observationstarts = new DateTime(pcnInfo.logTime).toString();//":"ISO8601datetime",
        else
            observationstarts = new DateTime(issueTime).toString();//":"ISO8601datetime",

        observationends = new DateTime(issueTime).toString(); //":"ISO8601datetime",
        if (pcnInfo.taxDiscs.size() > 0)
        {
            taxdiscserialnumber = pcnInfo.taxDiscs.get(0).serialNo;
            taxdiskexpiry = new DateTime(pcnInfo.taxDiscs.get(0).dateMillis).toString();//":"expiry mm/yy",
        }
        else
        {
            taxdiscserialnumber = "";
            taxdiskexpiry = "";
        }

        if (pcnInfo.pdTicketsList.size() > 0) {
            panddexpiry = pcnInfo.pdTicketsList.get(0).expiryTime;//":"ISO8601datetime P&D Ticket expiry date / time",
            panddserialnumber = pcnInfo.pdTicketsList.get(0).serialNo;//":"P&D Ticket number",
        }
        else
        {
            panddexpiry ="";
            panddserialnumber = "";
        }
        panddmachine = "";
        valvepositionfront = String.valueOf(pcnInfo.valveFront);//":"Valve position front",
        valvepositionrear = String.valueOf(pcnInfo.valveBack);//":"Valve position rear”,
        onorby  = "";
        onorbyjunction = "";
        sideofroad = "";
        facing = "";
        osoropp = "";
        osoppwhere = pcnInfo.location.outside;
        distancefrom = "";
        direction = "";
        foreignvehiclecountryname = pcnInfo.countryName;
        if (pcnInfo.contravention.contraventionCode.equals("61") || pcnInfo.contravention.contraventionCode.equals("62"))
            parkedonfootway = "Yes";
        else
            parkedonfootway = "No";
        if (pcnInfo.additionalInfo.selectedOptions[1])
            brokendown = "Yes";
        else
            brokendown = "No";
        if (pcnInfo.additionalInfo.selectedOptions[2])
            foreignvehicle = "Yes";
        else
            foreignvehicle = "No";
        if (pcnInfo.additionalInfo.selectedOptions[0])
            diplomaticvehicle = "Yes";
        else
            diplomaticvehicle = "No";
        if (pcnInfo.additionalInfo.selectedOptions[3])
            loadingorunloading = "Yes";
        else
            loadingorunloading = "No";
        parkedagainstflow= "No"; //":"Yes/No",
        if (pcnInfo.dInfo.driverInteraction[0] == DestinationInfo.DRIVER_SEEN)
            driverseen = "Yes";
        else
            driverseen = "No";
        if (pcnInfo.dInfo.pcnDestination == DestinationInfo.PCN_SPOILT)
            pcnspoilt = "Yes";
        if (pcnInfo.dInfo.pcnDestination == DestinationInfo.VDA)
            vehicledrivenaway = "Yes";
        else
            vehicledrivenaway = "No";
        if (pcnInfo.dInfo.driverInteraction[1] == DestinationInfo.PHYSICAL_ABUSE)
            physicalabuse = "Yes";
        else
            physicalabuse = "No";
        if (pcnInfo.dInfo.driverInteraction[2] == DestinationInfo.VERBAL_ABUSE)
            verbalabuse= "Yes";
        else
            verbalabuse = "No";

        if (pcnInfo.dInfo.driverInteraction[3] == DestinationInfo.ALL_WINDOWS_CHECKED)
            allwindows = "Yes";
        else
            allwindows = "No";
        actiontotake = pcnInfo.dInfo.actionToTake;
        actionpriority = String.valueOf(pcnInfo.dInfo.actionPriority);
        gpslat = String.valueOf(pcnInfo.gpsLat);
        gpslong = String.valueOf(pcnInfo.gpsLong);
        ceoshouldernumber = pcnInfo.ceoShoulderNumber;
        locationstreetname = pcnInfo.locationStreetName;
        imeinumber = pcnInfo.imeiNumber;
        foreignvehiclecountry = pcnInfo.countryCode;
        if (!pcnInfo.countryCode.equals("GBR")) {
            foreignvehicle = "Yes";
        }
        contraventiondateandtime = new DateTime(issueTime).toString(); //":"ISO8601datetime",
        ticketissuedateandtime = new DateTime(issueTime).toString(); //":"ISO8601datetime",
        switch (pcnInfo.dInfo.pcnDestination)
        {
            case DestinationInfo.AFFIXED_TO_WINDSHIELD:
            {
                methodofissue = "Affixed to Vehicle";
                break;
            }
            case DestinationInfo.HANDED_TO_DRIVER:
            {
                methodofissue = "Handed to Driver";
                break;
            }
            case DestinationInfo.VDA:
            {
                methodofissue = "VDA";
                break;
            }
            case DestinationInfo.VOID:
            {
                methodofissue = "void";
                break;
            }
            case DestinationInfo.PREVENTED_FROM_ISSUE:
            {
                methodofissue = "Prevented from Issue";
                break;
            }
            default:
                methodofissue = "";

                //":"Method of issue (affix to vehicle, hand to driver, post)",
        }


        numberofphotostaken = String.valueOf(DBHelper.PhotosForPCN(pcnInfo.observationNumber).size()); //":"number",
        ticketnotes         = pcnInfo.ticketNotes; //":"long text"
        if (pcnInfo.receivedTime == 0l)
        {
            receivedTime = "Not Received via Pubnub";
        }
        else
        {
            receivedTime = new DateTime(pcnInfo.receivedTime).toString();
        }
        warningnotice = pcnInfo.warningNotice;
        firstparkingsessioncheck = pcnInfo.firstParkingSessionCheck;
        secondparkingsessioncheck = pcnInfo.secondParkingSessionCheck;
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().create();
        String res = gson.toJson(this,PCNJsonData.class);

        return res;
    }

}
