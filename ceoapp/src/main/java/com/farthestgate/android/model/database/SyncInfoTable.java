package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;


@Table(name = "SyncInfoTable")
public class SyncInfoTable extends Model{

    public static final String COL_IMEI_NUMBER = "IMEINumber";
    public static final String COL_DATE_TIME_OF_SYNC= "DateTimeOfSync";
    public static final String COL_IS_SENT= "isSent";

    @Column(name = COL_IMEI_NUMBER)
    private String IMEINumber;

    @Column(name = COL_DATE_TIME_OF_SYNC)
    private String dateTimeOfSync;

    @Column(name = COL_IS_SENT)
    private String isSent;

    public SyncInfoTable(){ super(); }

    public String getIMEINumber() {
        return IMEINumber;
    }

    public void setIMEINumber(String IMEINumber) {
        this.IMEINumber = IMEINumber;
    }

    public String getDateTimeOfSync() {
        return dateTimeOfSync;
    }

    public void setDateTimeOfSync(String dateTimeOfSync) {
        this.dateTimeOfSync = dateTimeOfSync;
    }

    public String isSent() {
        return isSent;
    }

    public void setSent(String sent) {
        isSent = sent;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("IMEI",IMEINumber);
        res.put("datetimeofsync",dateTimeOfSync);
        return res;
    }
}
