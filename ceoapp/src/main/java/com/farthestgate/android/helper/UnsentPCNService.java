package com.farthestgate.android.helper;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.PCNJsonData;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class UnsentPCNService extends IntentService {
    private static ServiceCallbacks serviceCallbacks;
    private static final int MINTUES = 5;

    public UnsentPCNService() {
        super("UnsentPCNService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("UnsentPCNService", "UnsentPCNService running");
        processUnsentJsonPcn(DBHelper.getUnsentPCNs());
    }

    public interface ServiceCallbacks {
        void unsentPCNJson(PCNJsonData outPCN);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        this.serviceCallbacks = callbacks;
    }

    public void processUnsentJsonPcn(List<PCNTable> pcnTables) {
        if(pcnTables != null && pcnTables.size() > 0) {
            for (PCNTable pcn : pcnTables) {
                try {
                    String pcnJSON = pcn.getPcnJSON();
                    Gson gson = new GsonBuilder().create();
                    PCN pcnInfo = gson.fromJson(pcnJSON, PCN.class);
                    PCNJsonData outPCN = new PCNJsonData(pcnInfo);
                    pcn.setPcnOUTJSON(outPCN.toJSON());
                    pcn.save();
                    if (serviceCallbacks != null) {
                        if(pcnInfo.issueTime != 0 && DateUtils.getCurrentDateMinusDateInMilis(pcnInfo.issueTime)> MINTUES)
                            serviceCallbacks.unsentPCNJson(outPCN);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

}