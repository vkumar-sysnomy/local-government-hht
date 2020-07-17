package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Jitendra kumar on 08/2015.
 */
@Table(name = "CeoLoginTable")
public class CeoLoginTable extends Model{

    public static final String COL_CEO_NUMBER = "CeoNumber";
    public static final String COL_LOGIN_DATE_TIME = "LoginDateTime";
    public static final String COL_DEVICE_IN_USE = "DeviceInUse";
    public static final String COL_ROLE_SELECTED = "RoleSelected";

    @Column(name = COL_CEO_NUMBER)
    private String ceoNumber;

    @Column(name = COL_LOGIN_DATE_TIME)
    private long loginDateTime;

    @Column(name = COL_DEVICE_IN_USE)
    private String deviceInUse;

    @Column(name = COL_ROLE_SELECTED)
    private String roleSelected;

    public String getCeoNumber() {
        return ceoNumber;
    }

    public void setCeoNumber(String ceoNumber) {
        this.ceoNumber = ceoNumber;
    }

    public long getLoginDateTime() {
        return loginDateTime;
    }

    public void setLoginDateTime(long loginDateTime) {
        this.loginDateTime = loginDateTime;
    }

    public String getDeviceInUse() {
        return deviceInUse;
    }

    public void setDeviceInUse(String deviceInUse) {
        this.deviceInUse = deviceInUse;
    }

    public String getRoleSelected() {
        return roleSelected;
    }

    public void setRoleSelected(String roleSelected) {
        this.roleSelected = roleSelected;
    }
}
