package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;

/**
 * Created by Hanson Aboagye on 27/04/2014.
 */
@Table(name = "LocationLogTable")
public class LocationLogTable extends Model
{
    public static final String COL_LOCLOG_LAT    = "Lattitude";
    public static final String COL_LOCLOG_LNG    = "Longitude";
    public static final String COL_LOCLOG_STREET = "StreetName";
    public static final String COL_LOCLOG_CEO    = "CeoName";
    public static final String COL_LOCLOG_TIME   = "LogTime";
    //include PCN number, VRM and observation date in tour report
    public static final String COL_LOCLOG_TICKET_NUMBER   = "TicketNumber";
    public static final String COL_LOCLOG_VRM   = "VRM";
    public static final String COL_LOCLOG_OBSERVATION_DATE   = "ObservationDate";
    //include observation start and end time in tour report
    public static final String COL_LOCLOG_OBS_START   = "ObservationStartTime";
    public static final String COL_LOCLOG_OBS_END   = "ObservationEndTime";

    //include login, start of day, and end of day time in tour report
    public static final String COL_LOCLOG_CEO_LOGIN   = "CeoLoginTime";
    public static final String COL_LOCLOG_TEST_PCN_PRINTED_TIME   = "TestPCNPrintedTime";
    public static final String COL_LOCLOG_END_OF_DAY_TIME   = "EndOfDayTime";

    //include AnprRead, AnprReadTime, and VrmLookupResponse in tour report
    public static final String COL_ANPR_READ   = "AnprRead";
    public static final String COL_ANPR_READ_TIME   = "AnprReadTime";
    public static final String COL_VRM_LOOKUP_RESP   = "VrmLookupResponse";


    @Column(name = COL_LOCLOG_LAT)
    private double lattitude;

    @Column(name = COL_LOCLOG_LNG)
    private double longitude;

    @Column(name = COL_LOCLOG_STREET)
    private String streetName;

    @Column(name = COL_LOCLOG_CEO)
    private String ceoName;

    @Column(name = COL_LOCLOG_TIME)
    private String logTime;

    @Column(name = COL_LOCLOG_TICKET_NUMBER)
    private String ticketNumber;

    @Column(name = COL_LOCLOG_VRM)
    private String vrm;

    @Column(name = COL_LOCLOG_OBSERVATION_DATE)
    private String observationDate;

    @Column(name = COL_LOCLOG_OBS_START)
    private long startTime;

    @Column(name = COL_LOCLOG_OBS_END)
    private long endTime;

    @Column(name = COL_LOCLOG_CEO_LOGIN)
    private long ceoLoginTime;

    @Column(name = COL_LOCLOG_TEST_PCN_PRINTED_TIME)
    private long testPCNPrintedTime;

    @Column(name = COL_LOCLOG_END_OF_DAY_TIME)
    private long endOfDayTime;

    @Column(name = COL_ANPR_READ)
    private String anprRead;

    @Column(name = COL_ANPR_READ_TIME)
    private long anprReadTime;

    @Column(name = COL_VRM_LOOKUP_RESP)
    private String vrmLookupResponse;

    private SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy HH:mm:ss");

    public LocationLogTable()
    { super(); }

    public String toJSON()
    {
        String res = "";

        res = "{\"streetname\":\""+ getStreetName() + "\","+
                "\"logTime\":\"" + getLogTime() + "\"," +
                "\"ticketNumber\":\"" + getTicketNumber() + "\"," +
                "\"vrm\":\"" + getVRM() + "\"," +
                "\"observationDate\":\"" + getObservationDate() + "\"," +
                "\"lattitude\":\"" + String.valueOf(getLattitude()) + "\"," +
                "\"longitude\":\"" + String.valueOf(getLongitude()) + "\"," +
                "\"starttime\":\"" + String.valueOf(getStartTime()) + "\"," +
                "\"endtime\":\"" + String.valueOf(getEndTime()) + "\"," +
                "\"ceoLoginTime\":\"" + String.valueOf(getCeoLoginTime()) + "\"," +
                "\"testPCNPrintedTime\":\"" + String.valueOf(getTestPCNPrintedTime()) + "\"," +
                "\"endOfDayTime\":\"" + String.valueOf(getEndOfDayTime()) + "\"," +
                "\"anprRead\":\"" + getAnprRead() + "\"," +
                "\"anprReadTime\":\"" + String.valueOf(getAnprReadTime()) + "\"," +
                "\"vrmLookupResponse\":\"" + getVrmLookupResponse() + "\"" +
                "}";

        return res;
    }


    public String getStreetName()
    {
        return streetName;
    }
    public void setStreetName(String streetName)
    {
        this.streetName = streetName;
    }
    public double getLattitude()
    {
        return lattitude;
    }
    public void setLattitude(double lattitude)
    {
        this.lattitude = lattitude;
    }
    public double getLongitude()
    {
        return longitude;
    }
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
    public String getLogTime()
    {
        return logTime;
    }
    public void setLogTime(String logTime)
    {
        this.logTime = logTime;
    }
    public void setLogTime(DateTime logTime)
    {
        this.logTime = sdf.format(logTime.toDate());
    }
    public String getCeoName() {
        return ceoName;
    }
    public void setCeoName(String ceoName) {
        this.ceoName = ceoName;
    }
    public String getTicketNumber()
    {
        return ticketNumber;
    }
    public void setTicketNumber(String ticketNumber)
    {
        this.ticketNumber = ticketNumber;
    }
    public String getVRM()
    {
        return vrm;
    }
    public void setVRM(String vrm)
    {
        this.vrm = vrm;
    }
    public String getObservationDate()
    {
        return observationDate;
    }
    public void setObservationDate(String observationDate)
    {
        this.observationDate = observationDate;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    public long getCeoLoginTime() {
        return ceoLoginTime;
    }

    public void setCeoLoginTime(long ceoLoginTime) {
        this.ceoLoginTime = ceoLoginTime;
    }

    public long getTestPCNPrintedTime() {
        return testPCNPrintedTime;
    }

    public void setTestPCNPrintedTime(long testPCNPrintedTime) {
        this.testPCNPrintedTime = testPCNPrintedTime;
    }

    public long getEndOfDayTime() {
        return endOfDayTime;
    }

    public void setEndOfDayTime(long endOfDayTime) {
        this.endOfDayTime = endOfDayTime;
    }

    public String getAnprRead() {
        return anprRead;
    }

    public void setAnprRead(String anprRead) {
        this.anprRead = anprRead;
    }

    public long getAnprReadTime() {
        return anprReadTime;
    }

    public void setAnprReadTime(long anprReadTime) {
        this.anprReadTime = anprReadTime;
    }

    public String getVrmLookupResponse() {
        return vrmLookupResponse;
    }

    public void setVrmLookupResponse(String vrmLookupResponse) {
        this.vrmLookupResponse = vrmLookupResponse;
    }
}
