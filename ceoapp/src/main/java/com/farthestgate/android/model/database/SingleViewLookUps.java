package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "SingleViewLookUps")
public class SingleViewLookUps extends Model {

    public static final String CEO_NUMBER = "CeoNumber";
    public static final String DATE_TIME = "DateTime";
    public static final String VRM_HASH = "VrmHash";


    @Column(name = CEO_NUMBER)
    private String ceoNumber;

    @Column(name = DATE_TIME)
    private String dateTime;

    @Column(name = VRM_HASH)
    private String vrmHash;


    public SingleViewLookUps()
    { super(); }

    public String toJSON()
    {
        String res = "";

        res = "{\"ceoNumber\":\""+ getCeoNumber() + "\","+
                "\"dateTime\":\"" + getDateTime() + "\"," +
                "\"vrmHash\":\"" + getVrmHash() + "\"" +
                "}";


        return res;
    }


    public String getCeoNumber() {
        return ceoNumber;
    }

    public void setCeoNumber(String ceoNumber) {
        this.ceoNumber = ceoNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getVrmHash() {
        return vrmHash;
    }

    public void setVrmHash(String vrmHash) {
        this.vrmHash = vrmHash;
    }

}
