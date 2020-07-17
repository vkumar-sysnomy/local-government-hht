package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "PCNPhotoTable")
public class PCNPhotoTable extends Model {

    public static final String COL_PHOTO_FILE_NAME = "FileName";
    public static final String COL_PHOTO_CEO = "CEO_Number";
    public static final String COL_PHOTO_OBS = "observation";
    public static final String COL_PHOTO_TIME = "timestamp";
    public static final String COL_PHOTO_MARK = "isWatermarked";
    public static final String COL_PCN_SESSION =  "PCN_SESSION";

    @Column(name = COL_PHOTO_FILE_NAME)
    private String fileName;

    @Column(name = COL_PHOTO_CEO)
    private String CEO_Number;

    @Column(name = COL_PHOTO_OBS)
    private Integer observation;

    @Column(name = COL_PHOTO_TIME)
    private String timestamp;

    @Column(name = COL_PHOTO_MARK)
    private int isWatermarked;

    @Column(name = COL_PCN_SESSION)
    public int pcnSession;


    public PCNPhotoTable()
    {
        super();
        setWatermarked(0);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCEO_Number() {
        return CEO_Number;
    }

    public void setCEO_Number(String CEO_Number) {
        this.CEO_Number = CEO_Number;
    }

    public Integer getObservation() {
        return observation;
    }

    public void setObservation(Integer observation) {
        this.observation = observation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int isWatermarked() {
        return isWatermarked;
    }

    public void setWatermarked(int isWatermarked) {
        this.isWatermarked = isWatermarked;
    }

    public int getPcnSession() {
        return pcnSession;
    }

    public void setPcnSession(int pcnSession) {
        this.pcnSession = pcnSession;
    }

}
