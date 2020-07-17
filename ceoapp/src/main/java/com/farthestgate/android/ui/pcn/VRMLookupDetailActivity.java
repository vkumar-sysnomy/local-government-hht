package com.farthestgate.android.ui.pcn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.utils.CroutonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VRMLookupDetailActivity extends Activity {
    Button btnStartPCN,btnGoBack;
    TextView txtLookupDetails;
    private static final String POUND = "\u00A3";
    private static final String UNIVERSAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy'T'HH:mm";

    private String lookUpVRM = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_vrm_lookup_detail);
        btnStartPCN = (Button) findViewById(R.id.btnStartPCN);
        btnGoBack = (Button) findViewById(R.id.btnGoBack);
        txtLookupDetails = (TextView) findViewById(R.id.txtLookupDetails);
        btnStartPCN.setOnClickListener(btnStartPCNClick);
        btnGoBack.setOnClickListener(btnGoBackClick);
        try {
            Bundle extras = getIntent().getExtras();
            List<PaidParking> paidParkings = null;
            String VRM ="";
            if (extras != null) {
//                paidParkings = extras.getParcelableArrayList("paidParking");
                int sync = getIntent().getIntExtra("paidParking:synccode", -1);
                paidParkings = (List<PaidParking>) DataHolder.get().getListData(sync);
                VRM = extras.getString("VRM");
            }
            if (paidParkings != null && paidParkings.size()>0) {
                boolean firstAdded = false;
                String content = "";
                for(PaidParking paidParking : paidParkings) {
                    if (!firstAdded) {
                        if (paidParking.vrm != null && !paidParking.vrm.isEmpty()) {
                            lookUpVRM = paidParking.vrm;
                            content += "VRM " + lookUpVRM + "\n\n\n";
                        }
                        firstAdded = true;
                    }
                    String makeModel = "\n";
                    if (paidParking.make != null && !paidParking.make.isEmpty())
                        makeModel += paidParking.make;
                    if (paidParking.model != null && !paidParking.model.isEmpty())
                        makeModel += " " + paidParking.model;
                    if (!makeModel.isEmpty()) content += makeModel + "\n";
                    if (paidParking.colour != null && !paidParking.colour.isEmpty())
                        content += paidParking.colour + "\n";
                    if (paidParking.type != null && !paidParking.type.isEmpty())
                        content += paidParking.type + "\n";
                    if (paidParking.zone != null && !paidParking.zone.isEmpty())
                        content += paidParking.zone + "\n";
                    if (paidParking.cashlessloc != null && !paidParking.cashlessloc.isEmpty())
                        content += "Loc #" + paidParking.cashlessloc + "\n";
                    if (paidParking.cashlesslocname != null && !paidParking.cashlesslocname.isEmpty())
                        content += paidParking.cashlesslocname + "\n";
                    if (paidParking.timepaid != null && !paidParking.timepaid.isEmpty())
                        content += "Time Paid - " + paidParking.timepaid + "\n";
                    if (paidParking.amountpaid != null && !paidParking.amountpaid.isEmpty())
                        content += "Amount Paid - " + POUND +paidParking.amountpaid + "\n";

                    String start = paidParking.start != null && !paidParking.start.isEmpty() ? paidParking.start : "";
                    if (!start.isEmpty()) {
                        Date startDate = new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).parse(start);
                        String startDateStr = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startDate);
                        String[] startParts = startDateStr.split("T");
                        if (startParts[1].equalsIgnoreCase("00:00")){
                            content += "Start Date - " + startParts[0] + "\n";
                        }else {
                            content += "Start Date/Time - " + startParts[0] + " " + startParts[1] + "\n";
                        }
                    }

                    String exp = paidParking.exp != null && !paidParking.exp.isEmpty() ? paidParking.exp : "";
                    if (!exp.isEmpty()) {
                        Date expDate = new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).parse(exp);
                        String expDateStr = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(expDate);
                        String[] expParts = expDateStr.split("T");
                        if (expParts[1].equalsIgnoreCase("00:00")) {
                            content += "Expiry Date - " + expParts[0] + "\n";
                        }else{
                            content += "Expiry Date/Time - " + expParts[0] + " " + expParts[1] + "\n";
                        }
                    }
                  /*  exp = paidParking.exp != null && !paidParking.exp.isEmpty() ? paidParking.exp : "";
                    if(!exp.isEmpty() && !exp.isEmpty()){
                        SimpleDateFormat sdf = new SimpleDateFormat(UNIVERSAL_DATE_FORMAT);
                        Date expDate = sdf.parse(exp);
                        long diff =  expDate.getTime()-new Date().getTime();
                        Date diffDate = new Date(diff);
                        sdf = new SimpleDateFormat("HH:mm");
                        content += "Time left - " + sdf.format(diffDate);
                    }*/

                    if(paidParking.estatezone != null && !paidParking.estatezone.isEmpty())
                        content += "Estate Zone - " + paidParking.estatezone + "\n";

                    if(paidParkings.size()>1) content += "\n----------------------------------------";
                }
                txtLookupDetails.setText(content);
            }else{
                txtLookupDetails.setText("There is no valid parking sessions for " + VRM);
                txtLookupDetails.setTextColor(Color.RED);
            }
        }catch (Exception ex){
            CroutonUtils.error(VRMLookupDetailActivity.this, "An error occurred  during getting the details.");
        }
    }

    private View.OnClickListener btnStartPCNClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent();
            intent.putExtra("permitToPCN", true);
            intent.putExtra("lookUpVRM", lookUpVRM);
            setResult(CeoApplication.REQUEST_CODE_PERMIT_TO_PCN, intent);
            finish();
        }
    };
    private View.OnClickListener btnGoBackClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //Intent defectIntent = new Intent(VRMLookupDetailActivity.this, VRMLookupSummaryActivity.class);
            //startActivity(defectIntent);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        //Intent defectIntent = new Intent(VRMLookupDetailActivity.this, VRMLookupSummaryActivity.class);
        //startActivity(defectIntent);
        finish();
       /* super.onBackPressed();*/
    }

}
