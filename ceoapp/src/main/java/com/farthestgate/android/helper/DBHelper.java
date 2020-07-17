package com.farthestgate.android.helper;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.model.Ceo;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.Street;
import com.farthestgate.android.model.StreetCPZ;
import com.farthestgate.android.model.database.BackupTable;
import com.farthestgate.android.model.database.BreakTable;
import com.farthestgate.android.model.database.BriefingNotes;
import com.farthestgate.android.model.database.BriefingNotesTable;
import com.farthestgate.android.model.database.CeoLoginTable;
import com.farthestgate.android.model.database.ConfigTable;
import com.farthestgate.android.model.database.ContraventionsTable;
import com.farthestgate.android.model.database.ImageTable;
import com.farthestgate.android.model.database.LocationLogTable;
import com.farthestgate.android.model.database.NotesTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.model.database.SingleViewLookUps;
import com.farthestgate.android.model.database.SyncInfoTable;
import com.farthestgate.android.model.database.TicketNoTable;
import com.farthestgate.android.model.database.TimerObjTable;
import com.farthestgate.android.model.database.VehicleMakeTable;
import com.farthestgate.android.model.database.VehicleModelTable;
import com.farthestgate.android.model.database.VersionTable;
import com.farthestgate.android.model.database.VirtualPermitTable;
import com.farthestgate.android.utils.DateUtils;
import com.farthestgate.android.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.farthestgate.android.model.database.CeoTable;
//import com.farthestgate.android.model.database.StreetsTable;


public class DBHelper {

    private static final String TAG = DBHelper.class.getSimpleName();

    public static void saveContraventions(Contravention contravention) {

        if (contravention != null)
            try {
                ContraventionsTable contraventionsTable = new ContraventionsTable();
                contraventionsTable.setContraventionCode(contravention.contraventionCode);
                contraventionsTable.setContraventionDescription(contravention.contraventionDescription);
                contraventionsTable.setEnforcementType(contravention.contraventionType);
                contraventionsTable.setSuffixes(contravention.codeSuffixes);
                contraventionsTable.setAgreementCode(contravention.agreementCode);
                contraventionsTable.save();
            } catch (Exception e) {
                Log.e("Saving STREETS", e.getMessage());
            }

    }



    public static List<LocationLogTable> getTour(String ceo) {
        return new Select()
                .from(LocationLogTable.class)
                .where(LocationLogTable.COL_LOCLOG_CEO + " =?", ceo)
                .execute();
    }


    public static List<SingleViewLookUps> getSingleViewLookUps() {
        return new Select()
                .from(SingleViewLookUps.class)
                .execute();
    }

    public static List<SingleViewLookUps> clearSingleViewLookUps() {

        return new Delete()
                .from(SingleViewLookUps.class)
                //.where(LocationLogTable.COL_LOCLOG_CEO + " =?", ceo)
                .execute();
    }

    public static List<LocationLogTable> ClearTourTable(String ceo) {
        return new Delete()
                .from(LocationLogTable.class)
                .where(LocationLogTable.COL_LOCLOG_CEO + " =?", ceo)
                .execute();
    }

    public static List<BreakTable> getBreakDetails(String ceo, int extracted) {

        String whereQuery = BreakTable.COL_EXTRACTED + " = " + extracted + " AND " +
                BreakTable.COL_CEO + " = '" + ceo + "'";

        return new Select()
                .from(BreakTable.class)
                .where(whereQuery)
                .execute();
    }

    public static BreakTable getBreakData(String ceo) {
        return new Select()
                .from(BreakTable.class)
//                .where(BreakTable.COL_END_TIME + " =? " + " AND " + BreakTable.COL_CEO + " =? " + " AND " + BreakTable.COL_BREAK_TYPE + " =? ", 0, ceo, "BREAK")
                .where(BreakTable.COL_END_TIME + " =? " + " AND " + BreakTable.COL_CEO + " =? ", 0, ceo)
                .executeSingle();
    }

    public static BreakTable getBreakTableObj(long breakId){
        return new Select()
                .from(BreakTable.class)
                .where(BreakTable.COL_BREAK_ID + " =? " , breakId)
                .executeSingle();
    }

    public static List<BreakTable> ClearBreakDetails(String ceo, int extracted) {
        String whereQuery = BreakTable.COL_EXTRACTED + " = " + extracted + " AND " +
                BreakTable.COL_CEO + " = '" + ceo + "'";

        return new Delete()
                .from(BreakTable.class)
                .where(whereQuery)
                .execute();
    }




    public static boolean removeData() {
        //No need to load the street details in database.
        //Read it from the file directly
       /* List<StreetsTable> res = new Delete()
                .from(StreetsTable.class)
                .execute();*/

        //No need to load the ceo details in database.
        //Read it from the file directly
        /*List<CeoTable> res2 = new Delete()
                .from(CeoTable.class)
                .execute();*/

        List<ContraventionsTable> res3 = new Delete()
                .from(ContraventionsTable.class)
                .execute();


        List<VehicleModelTable> res4 = new Delete()
                .from(VehicleModelTable.class)
                .execute();

        List<VehicleMakeTable> res5 = new Delete()
                .from(VehicleMakeTable.class)
                .execute();

        /**
         *  Stop deleting the ticket table
         */


       /* List<TicketNoTable> res6 =  new Delete()
                .from(TicketNoTable.class)
                .execute();*/

        //No need to delete the PCN data from database
        /*List<NotesTable> res7 = new Delete()
                .from(NotesTable.class)
                .execute();*/

        List<LocationLogTable> res9 = new Delete()
                .from(LocationLogTable.class)
                .execute();

        List<TimerObjTable> res10 = new Delete()
                .from(TimerObjTable.class)
                .execute();

        List<VirtualPermitTable> res11 = new Delete()
                .from(VirtualPermitTable.class)
                .execute();

        List<BreakTable> res12 = new Delete()
                .from(BreakTable.class)
                .execute();

        // && res6.size() == 0
        //&& res7.size() == 0
        //No need to load the ceo details in database.
        //Read it from the file directly
        //&& res2.size() == 0
        //res.size() == 0  &&

        if(res3 != null && res4 != null && res5 != null && res9 != null && res10 != null && res11 != null && res12 != null){
            return (res3.size() == 0
                    && res4.size() == 0 && res5.size() == 0
                    && res9.size() == 0 && res10.size() == 0
                    && res11.size() == 0 && res12.size() == 0);
        }else{
            return false;
        }
    }
    /***Changes for imense ANPR integration:Start***/
    public static String getConfig() {
        String licenseKey = null;
        ConfigTable configTable = new Select()
                .from(ConfigTable.class)
                .executeSingle();
        if(configTable != null){
            licenseKey = configTable.getSecretKey();
        }
        return licenseKey;
    }
    public static void saveConfig(String licenseKey) {
        List<ConfigTable> configTables = new Delete()
                .from(ConfigTable.class)
                .execute();

        ConfigTable configTable = new ConfigTable();
        configTable.setSecretKey(licenseKey);
        configTable.save();
    }
    /***Changes for imense ANPR integration:End***/

    public static List<TicketNoTable> exportTickets() {
        return new Select()
                .from(TicketNoTable.class)
//                .where(TicketNoTable.COL_DATE_USED + " is null")
                .execute();
    }

    public static List<PCNTable> removeDayPCNs() {
        return new Delete()
                .from(PCNTable.class)
                .execute();

    }

    public static List<PCNTable> removeDayPhotos() {
        return new Delete()
                .from(PCNPhotoTable.class)
                .execute();

    }


    public static List<PCNPhotoTable> removePhotosForCancelObs(int observation) {
        List<PCNPhotoTable> pcnPhotoTables = new Delete().from(PCNPhotoTable.class)
                .where(PCNPhotoTable.COL_PHOTO_OBS +" = ?", observation)
                .execute();
        return pcnPhotoTables;

    }

    public static List<NotesTable> removeNotesForCancelObs(int observation) {
        return new Delete()
                .from(NotesTable.class)
                .where(NotesTable.COL_NOTE_OBS + " = ?", observation)
                .execute();

    }

    public static List<TimerObjTable> deketeTimer(int timerID) {
        return new Delete()
                .from(TimerObjTable.class)
                .where(TimerObjTable.COL_TIMER_ID + "=?", timerID)
                .execute();
    }

    /*public static List<TimerObjTable> deleteTimers() {
        return new Delete()
                .from(TimerObjTable.class)
                .execute();
    }
    public static List<VirtualPermitTable> deleteVirtualPermits() {
        return new Delete()
                .from(VirtualPermitTable.class)
                .execute();
    }

    public static List<BreakTable> deleteBreakRecords(){
        return new Delete()
                .from(BreakTable.class)
                .execute();
    }*/

    public static JSONObject CheckForValidParking(String VRM, int streetUSRN ) {
        JSONObject obj = new JSONObject();
        try {
            long currentDate = new Date().getTime();
            VirtualPermitTable validParking = new Select()
                    .from(VirtualPermitTable.class)
                    .where(VirtualPermitTable.COL_VIRTUAL_PERMIT_VRM + "=?" + " AND " + VirtualPermitTable.COL_VIRTUAL_PERMIT_STREET_USRN + "=?" + " AND " + VirtualPermitTable.COL_VIRTUAL_PERMIT_EXPIRY + ">?", VRM, streetUSRN, currentDate)
                    .executeSingle();

            if (validParking != null) {
                obj.put("parkingFound", true);
                obj.put("parkingExpiryTime", validParking.getExpiry());

            } else {
                obj.put("parkingFound", false);
                obj.put("parkingExpiryTime", 0);
            }

        } catch (Exception ex) {

        }
        return obj;
    }

    public static void SaveVirtualPermit(String VRM, int streetUSRN, long expiry ) {
        VirtualPermitTable validParking = new Select()
                .from(VirtualPermitTable.class)
                .where(VirtualPermitTable.COL_VIRTUAL_PERMIT_VRM + "=?" + " AND " + VirtualPermitTable.COL_VIRTUAL_PERMIT_STREET_USRN + "=?", VRM, streetUSRN)
                .executeSingle();
        if (validParking != null) {
            validParking.setExpiry(expiry);
            validParking.save();
        } else {
            VirtualPermitTable virtualPermitTable = new VirtualPermitTable();
            virtualPermitTable.setVrm(VRM);
            virtualPermitTable.setExpiry(expiry);
            virtualPermitTable.setStreetUSRN(streetUSRN);
            virtualPermitTable.save();
        }
    }




    public static List<PCNTable> getUnsentPCNs() {
        /*List<PCNTable> lst  = new Select()
                .from(PCNTable.class)
                .execute();*/
        return new Select()
                .from(PCNTable.class)
               /*.where(PCNTable.COL_PCN_SENT + " = ?", false)*/
                .where(PCNTable.COL_PCN_SENT + " = ?", 0)
                .execute();
    }

    public static Integer getEnforcementType(String code) {
        List<ContraventionsTable> res = getContraventionsType(code);
        if (res.size() > 0)
            return res.get(0).getEnforcementType();
        else
            return 0;
    }

    /*public static List<PCNTable> GetPCNs(Integer session, int extracted) {

        String whereQuery = PCNTable.COL_PCN_SESSION + " = " + session + " AND " +
                PCNTable.COL_DATA_EXTRACTED + " = " + extracted + " AND " +
                PCNTable.COL_CEO_NUMBER + " = '" + getCeoUserId() + "'";


        return new Select().from(PCNTable.class)
                .where(whereQuery)
                .execute();
    }*/

    public static List<PCNTable> GetPCNs(int extracted, boolean isEOD) {
        String whereQuery = "";
        if(isEOD)
            whereQuery = PCNTable.COL_DATA_EXTRACTED + " = " + extracted;
        else
            whereQuery = PCNTable.COL_DATA_EXTRACTED + " = " + extracted + " AND " +
                    PCNTable.COL_CEO_NUMBER + " = '" + getCeoUserId() + "'";

        return new Select().from(PCNTable.class)
                .where(whereQuery)
                .execute();
    }

    public static List<PCNTable> GetPCN(String pcnnumber) {
        return new Select().from(PCNTable.class)
                .where(PCNTable.COL_PCN_NUMBER + " = '" + pcnnumber + "'")
                .execute();
    }

    public static Integer GetPCNCount(Integer session) {

        /*String whereQuery = PCNTable.COL_PCN_SESSION + " = " + session + " AND " +
                PCNTable.COL_CEO_NUMBER + " = '" + CeoApplication.CEOLoggedIn.userId + "'";*/

        String whereQuery = PCNTable.COL_PCN_SESSION + " = " + session + " AND " +
                PCNTable.COL_DATA_EXTRACTED + " = " + 0 + " AND " +
                PCNTable.COL_CEO_NUMBER + " = '" + getCeoUserId() + "'";

        return new Select().from(PCNTable.class)
                .where(whereQuery)
                .execute().size();
    }


    public static long GetLastBackup() {
        List<BackupTable> res = new Select()
                .from(BackupTable.class)
                .where(BackupTable.COL_BACKUP_DATE + " > 0")
                .orderBy(BackupTable.COL_BACKUP_DATE)
                .execute();
        if (res.size() > 0) {
            return res.get(res.size()-1).getBackDate();
        } else
            return 0l;
    }


    public static List<BackupTable> getZipFilesToUpload() {
        return new Select()
                .from(BackupTable.class)
                .where(BackupTable.COL_BACKUP_DONE + " = 0")
                .execute();
    }


    public static List<ContraventionsTable> getContraventionsType(String code) {
        return new Select(ContraventionsTable.COL_CONTRAVENTION_ENFORCEMENT_TYPE)
                .from(ContraventionsTable.class)
                .where(ContraventionsTable.COL_CONTRAVENTION_CODE + "=?", code)
                .execute();

    }

    ;

    public static String GetSuffixes(String code) {
        List<ContraventionsTable> res = new Select(ContraventionsTable.COL_CONTRAVENTION_SUFFIXES)
                .from(ContraventionsTable.class)
                .where(ContraventionsTable.COL_CONTRAVENTION_CODE + "=?", code)
                .execute();
        if (res.size() > 0)
            return res.get(0).getSuffixes();
        else
            return "";
    }

    ;


    public static List<ContraventionsTable> GetContraventionsData(String code) {
        return new Select()
                .from(ContraventionsTable.class)
                .where(ContraventionsTable.COL_CONTRAVENTION_CODE + "=?", code)
                .execute();

    }

    ;


    public static List<VehicleMakeTable> getVehicleMakes() {
        return new Select()
                .from(VehicleMakeTable.class)
                .orderBy(VehicleMakeTable.COL_VEHICLE_NAME)
                .execute();
    }

    public static String getNextTicketNumber() {
        String ticketNumber = "";
        try {

        // Get Ticket Number
        String selectQuery = "SELECT MIN(" + TicketNoTable.COL_TICKET_REF + ") AS " + TicketNoTable.COL_TICKET_REF  +" FROM TicketNoTable WHERE " + TicketNoTable.COL_DATE_USED + " IS NULL AND "
                + TicketNoTable.COL_TICKET_REF + " > (SELECT IFNULL(MAX(" + TicketNoTable.COL_TICKET_REF + "),'') FROM TicketNoTable WHERE " + TicketNoTable.COL_DATE_USED + " IS NOT NULL)";

        TicketNoTable ticketNoTables = SQLiteUtils.rawQuerySingle(TicketNoTable.class, selectQuery, null);
        if(ticketNoTables != null){
            ticketNumber = ticketNoTables.getTicketReference();
        }
        CeoApplication.LogInfo("PCN #" + ticketNumber + " retrieved successfully on " + DateTime.now().toString());

        // update ticket number
            String updateQuery = "UPDATE TicketNoTable SET " + TicketNoTable.COL_DATE_USED + " = '" + DateTime.now().toString() + "' WHERE " +
                    TicketNoTable.COL_TICKET_REF + " = '" + ticketNumber + "'";

            ActiveAndroid.beginTransaction();
            SQLiteUtils.execSql(updateQuery);
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
            CeoApplication.LogInfo("PCN #" + ticketNumber + " used successfully on " + DateTime.now().toString());

        // check end time updated or not in db
            TicketNoTable ticketNoTable =new Select()
                    .from(TicketNoTable.class)
                    .where(TicketNoTable.COL_TICKET_REF + "= ?", ticketNumber)
                    .executeSingle();

            String endDate="";
            if(ticketNoTable!=null){
                endDate=ticketNoTable.getDateUsed();
            }

            if(endDate==null){
                return "";
            }
            else if(endDate.isEmpty()){
                return "";
            }else{
                return ticketNumber;
            }

        }catch (Exception e){
            return "";
        }

    }

    public static void usePCNNumber(String pcnNumber) {
        /*TicketNoTable tkt = new Select()
                .from(TicketNoTable.class)
                .where(TicketNoTable.COL_TICKET_REF + "= ?", pcnNumber)
                .executeSingle();
        if (tkt != null) {
            tkt.setDateUsed(DateTime.now().toString());
            tkt.save();
            CeoApplication.LogInfo("PCN #" + pcnNumber + " used successfully on " + DateTime.now().toString());
        }else{
            CeoApplication.LogInfo("PCN #" + pcnNumber + ".Failed to mark used on " + DateTime.now().toString());
        }*/

      /*  new Update(TicketNoTable.class)
                .set(TicketNoTable.COL_DATE_USED + " = '" + DateTime.now().toString() + "'")
                .where("TicketReference =? ", pcnNumber)
                .execute();*/

        //"update TicketNoTable set DateUsed='" + DateTime.now().toString() + "'" + " where TicketReference ='" + pcnNumber +"'"

        String updateQuery = "UPDATE TicketNoTable SET " + TicketNoTable.COL_DATE_USED + " = '" + DateTime.now().toString() + "' WHERE " +
                TicketNoTable.COL_TICKET_REF + " = '" + pcnNumber + "'";

        SQLiteUtils.execSql(updateQuery);

        CeoApplication.LogInfo("PCN #" + pcnNumber + " used successfully on " + DateTime.now().toString());

    }

    public static void releasePCNNumber(String pcnNumber) {
        List<TicketNoTable> res = new Select()
                .from(TicketNoTable.class)
                .where(TicketNoTable.COL_TICKET_REF + "= ?", pcnNumber)
                .execute();
        if (res != null) {
            for (TicketNoTable tkt : res) {
                tkt.setDateUsed(null);
                tkt.save();
            }
        }

    }

    public static List<TimerObjTable> getTimer(long timerID) {
        return new Select()
                .from(TimerObjTable.class)
                .where(TimerObjTable.COL_TIMER_ID + "=?", timerID)
                .execute();
    }

    public static String getCeoUserId(){
        if(CeoApplication.CEOLoggedIn == null){
            SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(CeoApplication.getContext());
//            CeoApplication.CEOLoggedIn = sharedPreferenceHelper.getValue("ceo", Ceo.class, null);
            CeoApplication.CEOLoggedIn = sharedPreferenceHelper.getObject(CeoApplication.getContext(),"ceo", Ceo.class);
        }
        return CeoApplication.CEOLoggedIn.userId;
    }

    public static List<TimerObjTable> getTimers() {
        return new Select()
                .from(TimerObjTable.class)
                .where(TimerObjTable.COL_CEO + "=?", getCeoUserId())
                .execute();
    }

    public static long getTotalBreak(String breakType) {
        long totalBreak = 0l;
        List<BreakTable> breaks = new Select()
                .from(BreakTable.class)
                .where(BreakTable.COL_CEO + "=? " + " AND " + BreakTable.COL_BREAK_TYPE + "=?", getCeoUserId(), breakType)
                .execute();

        for (BreakTable breakRow : breaks) {
            totalBreak += breakRow.getEndTime() - breakRow.getStartTime();
        }

        return totalBreak;
    }

    public static List<VersionTable> getVersion() {
        return new Select()
                .from(VersionTable.class)
                .execute();
    }

    public static boolean hasTicketNoData() {
        return new Select(TicketNoTable.COL_TICKET_NODE)
                .from(TicketNoTable.class).execute().size() > 0;
    }

    public static List<NotesTable> getNotesData() {
        return new Select()
                .from(NotesTable.class)
                .orderBy(NotesTable.COL_NOTE_CEO)
                .execute();
    }


    public static List<VehicleModelTable> getModels(Integer makeID) {
        return new Select()
                .from(VehicleModelTable.class)
                .where(VehicleModelTable.COL_MODEL_ID + "= ?", makeID)
                .execute();
    }

    /*public static List<NotesTable> getNotes(String ceoNum) {
        Long midnight = DateTime.now().getMillis() - DateTime.now().getMillisOfDay();

        return new Select()
                .from(NotesTable.class)
                .where(NotesTable.COL_NOTE_CEO + "= ? AND " +
                        NotesTable.COL_NOTE_DATE + " > " + midnight, ceoNum)
                .execute();
    }*/

    public static List<NotesTable> getNotes(String observations){
        Long midnight = DateTime.now().getMillis() - DateTime.now().getMillisOfDay();
        String query = NotesTable.COL_NOTE_OBS + " IN(" + observations + ") AND " + NotesTable.COL_NOTE_CEO + " = '" + getCeoUserId() + "' AND " +
                NotesTable.COL_NOTE_DATE + " > " + midnight;
        return new Select()
                .from(NotesTable.class)
                .where(query)
                .execute();
    }

    public static List<NotesTable> getPCNNotes(Integer pcnObs) {
        return new Select()
                .from(NotesTable.class)
                .where(NotesTable.COL_NOTE_OBS + "= ?", pcnObs)
                .execute();
    }


    public static List<PCNPhotoTable> PhotosForPCN(Integer seq) {
        return new Select()
                .from(PCNPhotoTable.class)
                .where(PCNPhotoTable.COL_PHOTO_OBS + "=?", seq)
                .execute();
    }

    public static List<PCNPhotoTable> GetPhotosToWatermark() {

        return new Select()
                .from(PCNPhotoTable.class)
                .where(PCNPhotoTable.COL_PHOTO_MARK + "=?", 0)
                .execute();
    }

    public static List<PCNPhotoTable> GetPhotosForCEO() {
        return new Select()
                .from(PCNPhotoTable.class)
                .where(PCNPhotoTable.COL_PHOTO_CEO + " = '" + getCeoUserId() + "'")
                .execute();
    }


    public static List<NotesTable> NotesForPCN(Integer seq) {
        return new Select()
                .from(NotesTable.class)
                .where(NotesTable.COL_NOTE_OBS + "=?", seq)
                .execute();
    }


    public static Integer PhotosCount(Integer session) {

        String whereQuery = PCNPhotoTable.COL_PCN_SESSION + " = " + session + " AND " +
                PCNPhotoTable.COL_PHOTO_CEO + " = '" + getCeoUserId() + "'";

        return new Select()
                .from(PCNPhotoTable.class)
                .where(whereQuery)
                .execute().size();
    }

    public static Integer PhotosCount(String observations) {
        String query = PCNPhotoTable.COL_PHOTO_OBS + " IN(" + observations + ") AND " + PCNPhotoTable.COL_PHOTO_CEO + " = '" + getCeoUserId() + "'";
        return new Select()
                .from(PCNPhotoTable.class)
                .where(query)
                .execute().size();
    }

    public static List<PCNTable> getUnSyncPCNs() {
        return new Select()
                .from(PCNTable.class)
                .where(PCNTable.COL_SYNC_SERVICE + " = ?" + " AND " + PCNTable.COL_PCN_SENT + " = ?", 0, 0)
                .execute();
    }

    public static void UpdateSyncServiceStatus(JSONObject responseObject ) {
        try {
            PCNTable pcn;
            List<PCNTable> PCNs = GetPCN(responseObject.getString("pcn"));
            if (PCNs.size() > 0) {
                pcn = PCNs.get(0);
                pcn.setSyncService(responseObject.getBoolean("status"));
                pcn.setSyncOutcome(responseObject.getString("message"));
                pcn.setSyncStatus(responseObject.getBoolean("status"));
                pcn.save();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static int getAvailableTicketNumbers() {
        List<TicketNoTable> ticketNoTables = new Select()
                .from(TicketNoTable.class)
                .where(TicketNoTable.COL_DATE_USED + " is null")
                .execute();
        if (ticketNoTables == null) {
            return 0;
        } else {
            return ticketNoTables.size();
        }
    }

    public static List<SyncInfoTable> getSyncInfo() {
        List<SyncInfoTable> syncInfoTable = new Select()
                .from(SyncInfoTable.class)
                .where(SyncInfoTable.COL_IS_SENT + " =?", AppConstant.NOT_SYNC_INFO)
                .execute();
        if(syncInfoTable!=null){
            return syncInfoTable;
        }
        return null;
    }


    public static void SaveCeoLoginDetails(String CeoNumber,String DeviceInUse, String RoleSelected ) {
        try {
            CeoLoginTable ceoLoginTable = new CeoLoginTable();
            ceoLoginTable.setCeoNumber(CeoNumber);
            ceoLoginTable.setDeviceInUse(DeviceInUse);
            ceoLoginTable.setRoleSelected(RoleSelected);
            ceoLoginTable.setLoginDateTime(new Date().getTime());
            ceoLoginTable.save();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //Ceo functions
    //No need to load the ceo details in database.
    //Read it from the file directly

    /*private static List<CeoTable> getCeos() {
        return new Select()
                .from(CeoTable.class)
                .orderBy(CeoTable.COL_USERID)
                .execute();
    }*/

    /*private static List<CeoTable> getCeo(String number) {
        return new Select()
                .from(CeoTable.class)
                .where(CeoTable.COL_USERID + " = ?", number)
                .execute();
    }*/

    public static void saveCeos(Ceo ceo) {
       /* if (ceo != null)
            try {
                CeoTable ceoTable = new CeoTable();
                ceoTable.setUserId(ceo.userId);
                ceoTable.setHash(ceo.hash);
                ceoTable.setCeonoderef(ceo.noderef);
                ceoTable.save();
            } catch (Exception e) {
                Log.e("Saving CEOS", e.getMessage());
            }*/
    }
    /*public static List<Ceo> getCeoInfo(String LH) {
        ArrayList<Ceo> ceoInfo = new ArrayList<Ceo>();
        for (CeoTable CEO : getCeo(LH)) {
            Ceo res = new Ceo();
            res.hash = CEO.getHash();
            res.userId = CEO.getUserId();
            res.noderef = CEO.getCeonoderef();
            ceoInfo.add(res);
        }

        return ceoInfo;
    }*/

    /*public static List<String> getCeoNumbers() {
        ArrayList<String> ceoNumbers = new ArrayList<String>();
        for (CeoTable CEO : getCeos()) {
            ceoNumbers.add(CEO.getUserId());
        }
        return ceoNumbers;
    }*/

    /*public static Ceo getCeoNodeRef() {
        Ceo ceoInfo =null;
        List<CeoTable> CEOs = getCeos();
        if (CEOs != null && CEOs.size() > 0) {
            ceoInfo = new Ceo();
            ceoInfo.hash = CEOs.get(0).getHash();
            ceoInfo.userId = CEOs.get(0).getUserId();
            ceoInfo.noderef = CEOs.get(0).getCeonoderef();
        }
        return ceoInfo;
    }*/

    private static List<Ceo> getCeos() {
        List<Ceo> ceoList=null;
        Gson gson = new GsonBuilder().create();
        JSONArray ceoJsonArray = CeoApplication.GetDataFileContent("ceos.json");
        if(ceoJsonArray !=null){
            Type ceoListType = new TypeToken<List<Ceo>>() {}.getType();
            ceoList = gson.fromJson(ceoJsonArray.toString(), ceoListType);
        }
        return ceoList;
    }

    private static List<Ceo> getCeo(String number){
        List<Ceo> ceos = null;
        try {
            Gson gson = new GsonBuilder().create();
            JSONArray ceoJsonArray = CeoApplication.GetDataFileContent("ceos.json");
            if (ceoJsonArray != null) {
                for (int index = 0; index < ceoJsonArray.length(); index++) {
                    JSONObject jsonCeo = ceoJsonArray.getJSONObject(index);
                    if (jsonCeo.getString("userId").equalsIgnoreCase(number)) {
                        Ceo ceo = gson.fromJson(jsonCeo.toString(), Ceo.class);
                        ceos.add(ceo);
                        break;
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ceos;
    }

    public static List<Ceo> getCeoInfo(String LH) {
        ArrayList<Ceo> ceoInfo = new ArrayList<Ceo>();
        for (Ceo CEO : getCeo(LH)) {
            Ceo res = new Ceo();
            res.hash = CEO.hash;
            res.userId = CEO.userId;
            res.noderef = CEO.noderef;
            ceoInfo.add(res);
        }
        return ceoInfo;
    }

    public static List<String> getCeoNumbers() {
        ArrayList<String> ceoNumbers = new ArrayList<String>();
        List<Ceo> ceos = getCeos();
        if(ceos !=null) {
            for (Ceo CEO : getCeos()) {
                ceoNumbers.add(CEO.userId);
            }
        }
        return ceoNumbers;
    }

    public static Ceo getCeoNodeRef() {
        Ceo ceoInfo =null;
        List<Ceo> CEOs = getCeos();
        if (CEOs != null && CEOs.size() > 0) {
            ceoInfo = new Ceo();
            ceoInfo.hash = CEOs.get(0).hash;
            ceoInfo.userId = CEOs.get(0).userId;
            ceoInfo.noderef = CEOs.get(0).noderef;
        }
        return ceoInfo;
    }

    //Street functions
    //No need to load the street details in database.
    //Read it from the file directly
   /* public static List<StreetsTable> getStreetsData() {
        return new Select()
                .from(StreetsTable.class)
                .orderBy(StreetsTable.COL_STREET_NAME)
                .execute();
    }*/

    /* public static boolean hasData() {
         return new Select(StreetsTable.COL_STREET_NAME)
                 .from(StreetsTable.class).execute().size() > 0;
     }*/
    public static boolean hasData() {
        return new Select(VehicleMakeTable.COL_VEHICLE_NAME)
                .from(VehicleMakeTable.class).execute().size() > 0;
    }

    public static List<Street> getStreetsData() {
        List<Street> streets = new ArrayList<Street>();
        try{
            String jsonStreetData = GetDataFileContent("streetindex.json");
            if (jsonStreetData != null) {
                try {
                    JSONArray streetJsonArray= new JSONArray(jsonStreetData);
                    for (int y = 0; y < streetJsonArray.length(); y++) {
                        StreetCPZ street = new StreetCPZ(streetJsonArray.getJSONObject(y));

                        if (street != null)
                            streets.add(new Street(street));
                    }
                }
                catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return streets;
    }

    public static void saveStreet(StreetCPZ street) {
        /*if (street != null)
            try {
                StreetsTable streetData = new StreetsTable(street);
                streetData.save();
            } catch (Exception e) {
                Log.e("Saving STREETS", e.getMessage());
            }*/
    }


    //No need to load the street details in database.
    //Read it from the file directly
    public static String GetDataFileContent(String fileName) {
        String jsonData = null;
        try {

            File configJsonFile = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.CONFIG_FOLDER + fileName);
            if (configJsonFile.exists()) {
                InputStream configStream = new FileInputStream(configJsonFile);
                jsonData = StringUtil.getStringFromInputStream(configStream);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }

    public static void markPCNPrinted(String pcnNumber, long printTime) {
        List<PCNTable> pcns = DBHelper.GetPCN(pcnNumber);
        if (pcns.size() > 0) {
            PCNTable finalPcn = pcns.get(0);
            finalPcn.setPrintTime(printTime);
            finalPcn.save();
        }
    }

    public static long getPCNPrintTime(String pcnNumber) {
        List<PCNTable> pcns = DBHelper.GetPCN(pcnNumber);
        long pcnPrintTime = 0;
        if (pcns.size() > 0) {
            PCNTable finalPcn = pcns.get(0);
            pcnPrintTime =  finalPcn.getPrintTime();
        }
        return pcnPrintTime;
    }

    //-----------------------------------

    public static List<BriefingNotesTable> reviewBriefNotesList(int readMarked){
        List<BriefingNotesTable> briefNotesTables = new ArrayList<>();
        try{
            briefNotesTables =  new Select().from(BriefingNotesTable.class)
                    .where(BriefingNotesTable.CEO_SHOULDER_NO + " =?" +" AND " + BriefingNotesTable.READ_MARKED + " =?", getCeoUserId(), readMarked)
                    .execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        return briefNotesTables;
    }

    public static boolean isBriefingNotesExists(String id) {
        boolean isBriefingNotesExists = false;
        try {
            BriefingNotesTable briefingNotesTable = new Select().from(BriefingNotesTable.class)
                    .where(BriefingNotesTable.MESSAGE_ID + " =?" + " AND " + BriefingNotesTable.CEO_SHOULDER_NO + " =?", id, getCeoUserId())
                    .executeSingle();
            if (briefingNotesTable != null) {
                isBriefingNotesExists = true;
            }
        } catch (Exception e) {
            isBriefingNotesExists = false;
        }
        return isBriefingNotesExists;
    }

    private static BriefingNotesTable isBriefingNotesExistsForUpdate(String id) {
        BriefingNotesTable briefingNotesTable = null;
        try {
            briefingNotesTable = new Select().from(BriefingNotesTable.class)
                    .where(BriefingNotesTable.MESSAGE_ID + " =?" + " AND " + BriefingNotesTable.CEO_SHOULDER_NO + " =?", id, getCeoUserId())
                    .executeSingle();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return briefingNotesTable;
    }


    public static void updateBriefingNotes(BriefingNotesTable briefingNotesTable) {
        try {
            briefingNotesTable.setReadMarked(1);
            briefingNotesTable.setSentToPubNub(1);
            briefingNotesTable.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void saveBriefNotes(BriefingNotes briefNotes) {
        try {
            BriefingNotesTable briefingNotesTable = new BriefingNotesTable();
            briefingNotesTable.setId(briefNotes.id);
            briefingNotesTable.setDate(briefNotes.date);
            briefingNotesTable.setTitle(briefNotes.title);
            briefingNotesTable.setContent(briefNotes.content);
            briefingNotesTable.setCreatedDate(DateUtils.getDateInMillis());
            briefingNotesTable.setCeoShoulderNo(getCeoUserId());
            briefingNotesTable.setReadMarked(0);
            briefingNotesTable.setSentToPubNub(0);
            briefingNotesTable.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBriefNotes(List<BriefingNotes> briefNotesList) {
        try {
            for(BriefingNotes briefingNotes : briefNotesList){
                updateBriefingNotes(briefingNotes);
                /*boolean isExist = isBriefingNotesExists(briefingNotes.id);
                if(!updatedMessages) {
                    saveBriefNotes(briefingNotes);
                }else{
                    updateBriefingNotes(briefingNotes);
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateBriefingNotes(BriefingNotes briefNotes) {
        try {
            BriefingNotesTable briefingNotesTable = isBriefingNotesExistsForUpdate(briefNotes.id);
            if (briefingNotesTable == null) {
                briefingNotesTable = new BriefingNotesTable();
            }
            briefingNotesTable.setId(briefNotes.id);
            briefingNotesTable.setDate(briefNotes.date);
            briefingNotesTable.setTitle(briefNotes.title);
            briefingNotesTable.setContent(briefNotes.content);
            briefingNotesTable.setCreatedDate(DateUtils.getDateInMillis());
            briefingNotesTable.setCeoShoulderNo(getCeoUserId());
            briefingNotesTable.setReadMarked(0);
            briefingNotesTable.setSentToPubNub(0);
            briefingNotesTable.save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getSendPCNStatus(String pcnNumber)
    {
        boolean pcnSent;
        try {
           PCNTable pcnTable = new Select().from(PCNTable.class)
                    .where(PCNTable.COL_PCN_NUMBER +"=?",pcnNumber).executeSingle();

            pcnSent = pcnTable.getBOSent();
        }catch (Exception e)
        {
            pcnSent = false;

        }
        return pcnSent;
    }


    public static ImageTable  getFindImage(String imgName)
    {
      ImageTable imageTable;
      try{
         imageTable =  new Select()
                  .from(ImageTable.class)
                  .where(ImageTable.COL_IMAGE_NAME + "=?", imgName)
                  .executeSingle();
         
    } catch (Exception e) {
          imageTable = null;
    }
        return imageTable;
    }

    public static void updateRetryCount(String imgName, int retry)
    {
        retry = retry + 1;
        String updateQuery = "UPDATE ImageTable SET " + ImageTable.COL_RETRY_COUNT + " = '"+ retry +"' WHERE " +
                ImageTable.COL_IMAGE_NAME + " = '" + imgName + "'";
        SQLiteUtils.execSql(updateQuery);

    }
}
