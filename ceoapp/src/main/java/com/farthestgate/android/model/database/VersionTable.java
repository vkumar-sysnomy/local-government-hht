package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by Hanson on 30/07/2014.
 */
public class VersionTable extends Model {

    public static final String COL_VERSION_NUM = "Version";
    public static final String COL_VERSION_BUILD = "Build";


    @Column(name = COL_VERSION_NUM)
    private String versionNumber;

    @Column(name = COL_VERSION_BUILD)
    private int buildNumber;

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }
}
