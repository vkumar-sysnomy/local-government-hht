package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson Aboagye on 22/06/2014.
 */
@Table(name = "BreakTable")
public class BreakTable extends Model
{

    public static final String COL_BREAK_ID = "BreakID";
    public static final String COL_CEO = "CeoID";
    public static final String COL_START_TIME = "StartTime";
    public static final String COL_END_TIME = "EndTime";
    public static final String COL_EXTRACTED = "Extracted";
    public static final String COL_BREAK_TYPE = "BreakType";

    @Column(name = COL_BREAK_ID)
    private long breakID;

    @Column(name = COL_CEO)
    private String ceoID;

    @Column(name = COL_START_TIME)
    private long startTime;

    @Column(name = COL_END_TIME)
    private long endTime;

    @Column(name = COL_EXTRACTED)
    private int extracted;

    @Column(name = COL_BREAK_TYPE)
    private String breakType;

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public String getCeoID()
    {
        return ceoID;
    }

    public void setCeoID(String ceoID)
    {
        this.ceoID = ceoID;
    }

    public Long getBreakID()
    {
        return breakID;
    }
    public void setBreakID(long breakID)
    {
        this.breakID = breakID;
    }
    public int getExtracted()
    {
        return extracted;
    }
    public void setExtracted(int extracted)
    {
        this.extracted = extracted;
    }

    public String getBreakType() {
        return breakType;
    }

    public void setBreakType(String breakType) {
        this.breakType = breakType;
    }

    public BreakTable(){ super();}
    public String getJSON()
    {
        String res = "";
        res = "{\"ceo\":\""+ getCeoID() + "\","+
                "\"startTime\":\"" + String.valueOf(getStartTime()) + "\"," +
                "\"endTime\":\"" + String.valueOf(getEndTime()) + "\"" +
               "}";
        return res;
    }
}
