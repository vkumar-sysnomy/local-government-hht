package com.farthestgate.android.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.model.database.SingleViewLookUps;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.SimpleSHA256;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Suraj Gopal on 8/1/2017.
 */
public class VRMAutomatedLookupTask extends AsyncTask<Void, Void, ArrayList<PaidParking>> {
    private Activity activity;
    private ProgressDialog mProgressDialog;
    private String vrmParam = "";

    private VRMAutomatedLookupListener vrmAutomatedLookupListener;

    public VRMAutomatedLookupTask(Activity activity, String vrmParam, VRMAutomatedLookupListener vrmAutomatedLookupListener){
        this.activity = activity;
        this.vrmParam = vrmParam;
        this.vrmAutomatedLookupListener = vrmAutomatedLookupListener;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait - Checking permit or parking session");
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected ArrayList<PaidParking> doInBackground(Void... params) {
        ArrayList<PaidParking> paidParkingList = null;

        if (CeoApplication.VirtualPermitsUrl() != null && !CeoApplication.VirtualPermitsUrl().isEmpty()) {
            try {


                String urlParams = "?vrm=" + vrmParam + "&device=" + CeoApplication.getUUID();
                if(CeoApplication.cashlessParkingProvider()!= null && CeoApplication.cashlessParkingProvider().equalsIgnoreCase("Ringo")){
                    urlParams += "&ocn=" + VisualPCNListActivity.currentStreet.noderef;
                }
                String response = AlfrescoComponent.executeGetRequest(CeoApplication.VirtualPermitsUrl() + urlParams);
                
                SingleViewLookUps singleViewLookUps = new SingleViewLookUps();
                singleViewLookUps.setCeoNumber(DBHelper.getCeoUserId());
                singleViewLookUps.setDateTime(AppConstant.ISO8601_DATE_TIME_FORMAT.format(new Date()));
                singleViewLookUps.setVrmHash(SimpleSHA256.encrypt(vrmParam));
                singleViewLookUps.save();
                if (response != null && !response.isEmpty()) {
                    JSONObject responseObject = new JSONObject(response);
                    if (responseObject.has("errorcode")) {
                        CroutonUtils.error(activity, responseObject.getString("error"));
                    } else {

                        JSONArray responseArray = responseObject.getJSONArray("paidparking");
                        Type listType = new TypeToken<ArrayList<PaidParking>>() {
                        }.getType();
                        paidParkingList = new Gson().fromJson(responseArray.toString(), listType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return paidParkingList;
    }

    @Override
    protected void onPostExecute(ArrayList<PaidParking> paidParkings) {
        super.onPostExecute(paidParkings);
        if(mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        boolean isError = false;
        if(paidParkings == null){
            isError = true;
        }

        if(vrmAutomatedLookupListener != null)
            vrmAutomatedLookupListener.vrmLookupPaidParking(paidParkings, vrmParam, isError);
    }

    public interface VRMAutomatedLookupListener {
        void vrmLookupPaidParking(ArrayList<PaidParking> paidParkings, String vrm, boolean isError);
    }
}
