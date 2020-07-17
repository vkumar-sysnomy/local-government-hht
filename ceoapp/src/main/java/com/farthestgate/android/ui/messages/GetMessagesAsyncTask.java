package com.farthestgate.android.ui.messages;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.database.BriefingNotes;
import com.farthestgate.android.ui.photo_gallery.AsyncTask;
import com.farthestgate.android.utils.AlfrescoComponent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class GetMessagesAsyncTask extends AsyncTask<String,String,String> {

    private IMessagesCallBack iMessagesCallBack;

    public GetMessagesAsyncTask(IMessagesCallBack callback){
        iMessagesCallBack=callback;
    }
    @Override
    protected String doInBackground(String... strings) {
            try {
                if (CeoApplication.getMessageUrl() != null && !CeoApplication.getMessageUrl().isEmpty()) {
                    String ur = CeoApplication.getMessageUrl()+"hhtid="+CeoApplication.getUUID()+"&id="+DBHelper.getCeoUserId()
                            +"&status=noread";
                    return AlfrescoComponent.executeGetRequest(ur);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null && !result.isEmpty()) {
            List<BriefingNotes> briefingNotes = new Gson().fromJson(result, new TypeToken<ArrayList<BriefingNotes>>() {
            }.getType());
            if(briefingNotes != null && briefingNotes.size() > 0){
                DBHelper.saveBriefNotes(briefingNotes);
                iMessagesCallBack.updateMessage();
            }
        }
    }


}
