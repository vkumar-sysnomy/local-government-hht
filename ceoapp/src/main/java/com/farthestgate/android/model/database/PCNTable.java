package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson on 02/05/2014.
 */
@Table(name = "PCNTable")
public class PCNTable extends Model{

    public static final String COL_PCN_NUMBER = "PCN_NUM";
    public static final String COL_PCN_JSON = "PCN_JSON";
    public static final String COL_PCN_OUTJSON = "PCN_OUTJSON";
    public static final String COL_PCN_SENT = "PCN_SENT";
    public static final String COL_PCN_SESSION =  "PCN_SESSION";
    public static final String COL_CEO_NUMBER = "CEO_NUM";
    public static final String COL_PHOTO_OBS = "observation";
    public static final String COL_SYNC_SERVICE = "sync_service";
    public static final String COL_SYNC_FILES = "sync_files";
    public static final String COL_SYNC_OUTCOME = "sync_outcome";
    public static final String COL_SYNC_STATUS = "sync_status";
    public static final String COL_RECEIVED_STATUS = "received_status";
    public static final String COL_DATA_EXTRACTED = "data_extracted";
    public static final String COL_PRINTING_TIME = "PrintTime";
    public static final String COL_BACK_OFFICE_SENT = "BO_SENT";

    @Column(name = COL_PCN_NUMBER)
    private String pcnNumber;

    @Column(name = COL_PCN_JSON)
    private String pcnJSON;

    @Column(name = COL_PCN_OUTJSON)
    private String pcnOUTJSON;

    @Column(name = COL_PCN_SENT)
    private boolean pcnSent;

    @Column(name = COL_PHOTO_OBS)
    private int observation;

    @Column(name = COL_PCN_SESSION)
    private int pcnSession;

    @Column(name = COL_CEO_NUMBER)
    private String ceoNumber;

    @Column(name = COL_SYNC_SERVICE)
    private boolean syncService;

    @Column(name = COL_SYNC_FILES)
    private boolean syncFiles;

    @Column(name = COL_SYNC_OUTCOME)
    private String syncOutcome;

    @Column(name = COL_SYNC_STATUS)
    private boolean syncStatus;

    @Column(name = COL_RECEIVED_STATUS)
    private boolean receivedStatus;

    @Column(name = COL_DATA_EXTRACTED)
    private int dataExtracted;

    @Column(name = COL_PRINTING_TIME)
    private long printTime;

    @Column(name = COL_BACK_OFFICE_SENT)
    private boolean backOfficeSent;

    public PCNTable()
    {
        super();
        setDataExtracted(0);
        setPrintTime(0);
    }

    public String getPcnNumber() {
        return pcnNumber;
    }

    public void setPcnNumber(String pcnNumber) {
        this.pcnNumber = pcnNumber;
    }

    public String getPcnJSON() {
        return pcnJSON;
    }

    public void setPcnJSON(String pcnJSON) {
        this.pcnJSON = pcnJSON;
    }

    public String getPcnOUTJSON() {
        return pcnOUTJSON;
    }

    public void setPcnOUTJSON(String pcnOUTJSON) {
        this.pcnOUTJSON = pcnOUTJSON;
    }

    public Boolean getPcnSent() {
        return pcnSent;
    }

    public void setPcnSent(Boolean pcnSent) {
        this.pcnSent = pcnSent;
    }

    public int getObservation() {
        return observation;
    }

    public void setObservation(int observation) {
        this.observation = observation;
    }

    public int getPcnSession() {
        return pcnSession;
    }

    public void setPcnSession(int pcnSession) {
        this.pcnSession = pcnSession;
    }

    public String getCeoNumber() {
        return ceoNumber;
    }

    public void setCeoNumber(String ceoNumber) {
        this.ceoNumber = ceoNumber;
    }

    public boolean isSyncService() {
        return syncService;
    }

    public void setSyncService(boolean syncService) {
        this.syncService = syncService;
    }

    public boolean isSyncFiles() {
        return syncFiles;
    }

    public void setSyncFiles(boolean syncFiles) {
        this.syncFiles = syncFiles;
    }

    public String getSyncOutcome() {
        return syncOutcome;
    }

    public void setSyncOutcome(String syncOutcome) {
        this.syncOutcome = syncOutcome;
    }

    public boolean isSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(boolean syncStatus) {
        this.syncStatus = syncStatus;
    }

    public boolean isReceivedStatus() {
        return receivedStatus;
    }

    public void setReceivedStatus(boolean receivedStatus) {
        this.receivedStatus = receivedStatus;
    }

    public int getDataExtracted() {
        return dataExtracted;
    }

    public void setDataExtracted(int dataExtracted) {
        this.dataExtracted = dataExtracted;
    }

    public long getPrintTime() {
        return printTime;
    }

    public void setPrintTime(long printTime) {
        this.printTime = printTime;
    }

    public boolean getBOSent() {
        return backOfficeSent;
    }

    public void setBOSent(boolean backOfficeSent) {
        this.backOfficeSent = backOfficeSent;
    }
}
