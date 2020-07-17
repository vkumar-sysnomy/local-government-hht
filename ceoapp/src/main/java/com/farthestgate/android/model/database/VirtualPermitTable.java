package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Jitendra on 02/04/15.
 */
@Table(name = "VirtualPermitTable")
public class VirtualPermitTable extends Model {
    public static final String COL_VIRTUAL_PERMIT_STREET_USRN = "StreetUSRN";
    public static final String COL_VIRTUAL_PERMIT_VRM = "VRM";
    public static final String COL_VIRTUAL_PERMIT_EXPIRY = "Expiry";

    @Column(name = COL_VIRTUAL_PERMIT_STREET_USRN)
    private int streetUSRN;
    @Column(name = COL_VIRTUAL_PERMIT_VRM)
    private String vrm;
    @Column(name = COL_VIRTUAL_PERMIT_EXPIRY)
    private long expiry;
    public VirtualPermitTable() {
        super();
    }
    public long getExpiry() {
        return expiry;
    }
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }
    public String getVrm() {
        return vrm;
    }
    public void setVrm(String vrm) {
        this.vrm = vrm;
    }
    public int getStreetUSRN() {
        return streetUSRN;
    }
    public void setStreetUSRN(int streetUSRN) {
        this.streetUSRN = streetUSRN;
    }

}
