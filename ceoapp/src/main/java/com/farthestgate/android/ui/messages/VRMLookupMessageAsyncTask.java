package com.farthestgate.android.ui.messages;

import android.app.ProgressDialog;
import android.content.Context;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.ui.photo_gallery.AsyncTask;
import com.farthestgate.android.utils.AlfrescoComponent;


public class VRMLookupMessageAsyncTask extends AsyncTask<String,String,String> {

    private MessageResponse listener;
    String response = "";
    ProgressDialog progressDialog;
    private Context mContext;

    public VRMLookupMessageAsyncTask(Context context, MessageResponse listener){
        this.listener=listener;
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Extracting paid parking – Please wait");
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String vrmParam =  params[0];
        String streetParam = params[1];
        try {
            if (CeoApplication.VirtualPermitsUrl() != null && !CeoApplication.VirtualPermitsUrl().isEmpty()) {
                String deviceNumber = CeoApplication.getUUID();
                String urlParams = "?device=" + deviceNumber;
                if (!streetParam.isEmpty()) {
                    urlParams += "&ocn=" + streetParam;
                }
                if (!vrmParam.isEmpty()) {
                    if (CeoApplication.cashlessParkingProvider() != null && CeoApplication.cashlessParkingProvider().equalsIgnoreCase("Ringo")) {
                        urlParams += "&vrm=" + vrmParam + "&ocn=" + VisualPCNListActivity.currentStreet.noderef;
                    } else {
                        urlParams += "&vrm=" + vrmParam;
                    }
                }
                response = AlfrescoComponent.executeGetRequest(CeoApplication.VirtualPermitsUrl() + urlParams);

            }
        } catch (Exception e) {
             CeoApplication.isVrmLook  = false;
             e.printStackTrace();
        }
        return response;
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        CeoApplication.isVrmLook  = false;
        progressDialog.dismiss();
        progressDialog = null;
        listener.onMsgResponse(response);
    }
}
