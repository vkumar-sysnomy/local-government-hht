package com.farthestgate.android.helper;

import java.text.SimpleDateFormat;

/**
 *
 */
public class AppConstant {

	// Number of columns of Grid View
	public static final int NUM_OF_COLUMNS = 3;

	// Gridview image padding
	public static final int GRID_PADDING = 8; // in dp

	// SD card image directory
	public static final String PHOTO_FOLDER     =  "ceoappdata/pcns/photos/";
    public static final String NOTES_FOLDER     =  "ceoappdata/pcns/notes/";
    public static final String PCN_FOLDER       =  "ceoappdata/pcns/";
    public static final String EXTRAS_FOLDER    =  "ceoappdata/pcns/extra_photos/";
    public static final String PCN_DATA_FOLDER  =  "ceoappdata/pcns/json/";
    public static final String CONFIG_FOLDER    =  "ceoappdata/configdata/";
    public static final String BACKUP_FOLDER    =  "ceoappdata/backup/";
    public static final String REMOVALPHOTO_FOLDER    =  "ceoappdata/pcns/removalPhoto";
    public static final String SENT_PHOTO    =  "ceoappdata/pcns/sentPhoto";
    public static final String LOCAL_DB_FOLDER    =  "ceoappdata/pcns/localDB";

    public static final int CONTRAVENTION_INSTANT = 0;
    public static final int CONTRAVENTION_LOADING = 1;
    public static final int CONTRAVENTION_OBSERVATION = 2;
    public static final int CONTRAVENTION_PD = 3;
    public static final int CONTRAVENTION_DUAL_LOG = 4;


    public static final int NOTE_SIZE = 714;
    public static final String TOUR_REPORT_FOLDER  =  "ceoappdata/pcns/reports/";
    public static final String APP_ERROR_FOLDER  =  "ceoappdata/pcns/errors/";
    public static final String APP_SINGLEVIEW_LOOKUP_FOLDER  =  "ceoappdata/pcns/singleViewLookup/";
    public static final String JSON_ERROR_FOLDER  =  "ceoappdata/pcns/errors/json_error/";
    public static final String TICKET_BOOK_FILE_PATH = "ceoappdata/pcnnumbers/";

    public static final SimpleDateFormat ISO8601_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public enum  pcnIssueMode   {
        PCN_AS_NORMAL ,COMMERCIAL_VEHICLE, DISABLED_BADGE, PAY_AND_DISPLAY;
    }

    public enum specialVehicleType {
        BLACKLIST, WHITELIST , NORMAL;
    }

    public static final String NO_AVAILABLE_PCN_BR = "noavailable_pcn";
    public static final String CURRENT_ADDRESS = "current_address";
    public static final String LOGIN_TIME = "loginTime";
    public static final String CEO_ROLE = "CEO_ROLE";
    public static final String CODE_RED = "codered";


    public static final String NOT_SYNC_INFO = "N";
    public static final String SYNC_INFO = "S";
    public static final String SYNC_INFO_ON_LOGIN = "L";



}
