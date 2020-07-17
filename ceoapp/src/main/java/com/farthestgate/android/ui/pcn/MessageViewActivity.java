package com.farthestgate.android.ui.pcn;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DataHolder;
import com.farthestgate.android.model.Messages;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.ui.CustomScrollView;
import com.farthestgate.android.utils.CroutonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageViewActivity extends Activity implements View.OnClickListener {

    CustomScrollView msgScrollView;
    LinearLayout mainLayout;
    Button btnGoBack, btnGoNext, btnOK;
    TextView txtLookupDetails, txtMessageTitle, txtMessageCount;
    WebView wvMessageBody;
    //ImageView icon_complete;

    private static final String POUND = "\u00A3";
    private static final String UNIVERSAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy'T'HH:mm";

    List<PaidParking> paidParkings;
    String VRM ="", street ="";
    boolean paidParkingMsg = false;
    List<Messages> messages = new ArrayList<Messages>();
    JSONObject messageJsonObject;
    JSONArray messageJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("VRM Lookup Message");
        msgScrollView = (CustomScrollView) findViewById(R.id.msgScrollView);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        btnGoBack = (Button) findViewById(R.id.btnGoBack);
        btnGoNext = (Button) findViewById(R.id.btnGoNext);
        btnOK = (Button) findViewById(R.id.btnOK);
        txtLookupDetails = (TextView) findViewById(R.id.txtLookupDetails);
        txtMessageTitle = (TextView) findViewById(R.id.txtMessageTitle);
        txtMessageCount = (TextView) findViewById(R.id.txtMessageCount);
        wvMessageBody = (WebView) findViewById(R.id.wvMessageBody);
        //icon_complete = (ImageView) findViewById(R.id.icon_complete);

        btnGoBack.setOnClickListener(this);
        btnGoNext.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                paidParkingMsg = extras.getBoolean("paidParkingMsg");
                if(paidParkingMsg) {
//                    paidParkings = CeoApplication.paidParkings;//extras.getParcelableArrayList("paidParking");
                    int sync = getIntent().getIntExtra("paidParking:synccode", -1);
                    paidParkings = (List<PaidParking>) DataHolder.get().getListData(sync);
                    VRM = extras.getString("VRM");
                }else {
//                    messageJsonObject = CeoApplication.messageJsonObject;//new JSONObject(jsonString);
                    int sync = getIntent().getIntExtra("messageObj:synccode", -1);
                    messageJsonObject = DataHolder.get().getJsonObject(sync);
                    street = extras.getString("street");
                    messageJsonArray = new JSONArray();
                }
            }
            if(paidParkingMsg) {
                if (paidParkings != null && paidParkings.size() > 0) {
                    boolean firstAdded = false;
                    String content = "";
                    boolean isShowContent = true;
                    for (PaidParking paidParking : paidParkings) {
                        if (!firstAdded) {
                            if (paidParking.vrm != null && !paidParking.vrm.isEmpty())
                                content += "VRM : " + paidParking.vrm + "\n";
                            firstAdded = true;
                        }
                        String makeModel = "\n";

                        if (paidParking.make != null && !paidParking.make.isEmpty()) {
                            makeModel += paidParking.make;
                        }else {
                            isShowContent = false;
                        }
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
                            content += "Amount Paid - " + POUND + paidParking.amountpaid + "\n";

                        String start = paidParking.start != null && !paidParking.start.isEmpty() ? paidParking.start : "";
                        if (!start.isEmpty()) {
                            Date startDate = new SimpleDateFormat(UNIVERSAL_DATE_FORMAT).parse(start);
                            String startDateStr = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startDate);
                            String[] startParts = startDateStr.split("T");
                            if (startParts[1].equalsIgnoreCase("00:00")) {
                                content += "Start Date - " + startParts[0] + "\n";
                            } else {
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
                            } else {
                                content += "Expiry Date/Time - " + expParts[0] + " " + expParts[1] + "\n";
                            }
                        }

                        if (paidParking.estatezone != null && !paidParking.estatezone.isEmpty())
                            content += "Estate Zone - " + paidParking.estatezone + "\n";

                        messages = paidParking.messages;
                        setVRMMessages(0);

                        if (paidParkings.size() > 1 && paidParking.make !=null && !paidParking.make.isEmpty() )
                            content += "\n----------------------------------------";
                    }
                    if(isShowContent) {
                        txtLookupDetails.setText(content);
                    }else{
                        txtLookupDetails.setText("VRM : " + VRM);
                    }
                } else {
                    txtLookupDetails.setText("There is no valid parking sessions for " + VRM);
                    txtLookupDetails.setTextColor(Color.RED);
                }
            } else{
                setStreetMessages(0);
            }
        }catch (Exception ex){
            CroutonUtils.error(MessageViewActivity.this, "An error occurred  during getting the details.");
        }
    }

    private String getDecodedString(String content){
        try {
            byte[] data = Base64.decode(content, Base64.DEFAULT);
            return new String(data, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    int count = 0;
    private void setVRMMessages(int messageIndex){
        try {
                clearData();
                txtMessageCount.setText(messageIndex+1 + "/" + messages.size());
                ControlPageState(messageIndex+1,messages.size());
                String messageTitle = getDecodedString(messages.get(messageIndex).messagetitle);
                String messageBody = getDecodedString(messages.get(messageIndex).messagebody);
                txtMessageTitle.setText(messageTitle);
                //txtMessageCount.setText(messageIndex+1 + "/" + messages.size());
                wvMessageBody.loadData(messageBody, "text/html", "UTF-8");
                count = messageIndex;

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setStreetMessages(int messageIndex){
        clearData();
        try {
            if(messageJsonArray ==null || messageJsonArray.length()==0) {
                JSONArray messageArray;
                if (messageJsonObject.has("cpzmessages")) {
                    messageArray = messageJsonObject.getJSONArray("cpzmessages");
                    for (int i = 0; i < messageArray.length(); i++) {
                        JSONObject messageObject = messageArray.getJSONObject(i);
                        messageObject.put("type", "cpz");
                        messageJsonArray.put(messageObject);
                    }
                }
                if (messageJsonObject.has("streetmessages")) {
                    messageArray = messageJsonObject.getJSONArray("streetmessages");
                    for (int i = 0; i < messageArray.length(); i++) {
                        JSONObject messageObject = messageArray.getJSONObject(i);
                        messageObject.put("type", "street");
                        messageJsonArray.put(messageObject);
                    }
                }
            }
            if(messageIndex == messageJsonArray.length()) {
                txtMessageTitle.setText("Briefing Completed.");
                ControlPageState(messageIndex+1,messageJsonArray.length());
                btnGoNext.setVisibility(View.GONE);
                btnOK.setVisibility(View.VISIBLE);
            }else {
                ControlPageState(messageIndex + 1,messageJsonArray.length());
                JSONObject messageToDisplay = messageJsonArray.getJSONObject(messageIndex);
                String messageTitle = getDecodedString(messageToDisplay.getString("messagetitle"));
                String messageBody = getDecodedString(messageToDisplay.getString("messagebody"));
                String type = messageToDisplay.getString("type");
                txtLookupDetails.setText(type.equalsIgnoreCase("cpz") ? "CPZ Messages for : " + street : "Street Messages for : " + street);
                txtMessageCount.setText(messageIndex + 1 + "/" + messageJsonArray.length());
                txtMessageTitle.setText(messageTitle);
                wvMessageBody.loadData(messageBody, "text/html", "UTF-8");
                count = messageIndex;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnGoBack){
            --count;
            if(count == -1) finish();
            if(paidParkingMsg){
                setVRMMessages(count);
            }else{
                setStreetMessages(count);
            }
            btnGoNext.setVisibility(View.VISIBLE);
            btnOK.setVisibility(View.GONE);
        }

        if(v.getId() == R.id.btnGoNext){
            ++count;
            if(paidParkingMsg){
                setVRMMessages(count);
            }else{
                setStreetMessages(count);
            }
        }

        if(v.getId() == R.id.btnOK){
            setResult(CeoApplication.REQUEST_CODE_MESSAGEVIEW);
            finish();
        }
    }

    private void clearData(){
        txtMessageTitle.setText("");
        txtMessageCount.setText("");
        wvMessageBody.loadData("", "text/html", "UTF-8");
    }

    private void ControlPageState(int msgCount,int arrayLength){
       if(msgCount<arrayLength)
       {
           btnGoNext.setEnabled(true);
           btnGoNext.setVisibility(View.VISIBLE);
           btnGoBack.setVisibility(View.VISIBLE);
           btnGoBack.setEnabled(true);
           btnOK.setEnabled(false);
           btnOK.setVisibility(View.GONE);
           msgScrollView.setEnableScrolling(true);
           txtLookupDetails.setVisibility(View.VISIBLE);
           txtMessageCount.setVisibility(View.VISIBLE);
           wvMessageBody.setVisibility(View.VISIBLE);
       }else
       {
           btnGoNext.setEnabled(false);
           btnGoNext.setVisibility(View.GONE);
           btnGoBack.setVisibility(View.GONE);
           btnGoBack.setEnabled(false);
           btnOK.setEnabled(true);
           btnOK.setVisibility(View.VISIBLE);
           //icon_complete.setVisibility(View.GONE);
           msgScrollView.setEnableScrolling(true);
           txtLookupDetails.setVisibility(View.VISIBLE);
           txtMessageCount.setVisibility(View.VISIBLE);
           wvMessageBody.setVisibility(View.VISIBLE);

       }

    }
}
