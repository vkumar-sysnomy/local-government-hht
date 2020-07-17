package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by Hanson on 04/08/2014.
 */
public class BackupTable extends Model {

    public static final String COL_BACKUP_NAME = "ZipName";
    public static final String COL_BACKUP_DONE = "BackedUp";
    public static final String COL_BACKUP_DATE = "BackDate";

    @Column(name = COL_BACKUP_NAME)
    private String backupName;

    @Column(name = COL_BACKUP_DONE)
    private int backupDone;

    @Column(name = COL_BACKUP_DATE)
    private long backDate;



    public BackupTable()
    {
        super();
        backupDone = 0;
    }

    public int getBackupDone() {
        return backupDone;
    }

    public void setBackupDone(int backupDone) {
        this.backupDone = backupDone;
    }

    public String getBackupName() {
        return backupName;
    }

    public void setBackupName(String backupName) {
        this.backupName = backupName;
    }

    public long getBackDate() {
        return backDate;
    }

    public void setBackDate(long backDate) {
        this.backDate = backDate;
    }
}
