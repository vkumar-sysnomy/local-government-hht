package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson ABoagye on 04/2014.
 */
@Table(name = "CeoTable")
public class CeoTable extends Model{

    public static final String COL_USERID = "UserID";
    public static final String COL_USER_HASH = "Hash";
    public static final String COL_USER_NODE = "CeoNodeRef";

    @Column(name = COL_USERID)
    private String userId;

    @Column(name = COL_USER_HASH)
    private String hash;

    @Column(name = COL_USER_NODE)
    private String ceonoderef;


    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCeonoderef() {
        return ceonoderef;
    }

    public void setCeonoderef(String ceonoderef) {
        this.ceonoderef = ceonoderef;
    }
}
