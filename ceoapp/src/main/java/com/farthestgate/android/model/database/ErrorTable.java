package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by Hanson on 22/10/2014.
 */
public class ErrorTable extends Model {

    public static final String COL_ERROR_LOC    = "Error_Loc";
    public static final String COL_ERROR_TYPE   = "Error_Type";
    public static final String COL_ERROR_TEXT   = "Error_Text";
    public static final String COL_ERROR_COUNT  = "Error_Count";


    public ErrorTable()
    {
        errorType = 0;
        errorCount = 1;
    }

    @Column(name = COL_ERROR_LOC)
    private int errorLoc;

    @Column(name = COL_ERROR_TYPE)
    private int errorType;

    @Column(name = COL_ERROR_TEXT)
    private String errorText;

    @Column(name = COL_ERROR_COUNT)
    private int errorCount;

    public int getErrorLoc() {
        return errorLoc;
    }

    public void setErrorLoc(int errorLoc) {
        this.errorLoc = errorLoc;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
