package com.farthestgate.android.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.ui.components.timer.TimerListItem;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Hanson Aboagye on 23/04/2014.
 */
public class PCNListItem extends LinearLayout
{
    TimerListItem timerView;


    public PCNListItem(Context context)
    {
        this(context, null);
    }

    public PCNListItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.pcn_list_item, this);

        initView();
    }

    public void setInfo(String VRM, String Location, String code, String osLoc)
    {
        TextView vrmText = (TextView) this.findViewById(R.id.registration_mark);
        vrmText.setText(VRM);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"fonts/HelveticaNeue-Bold.otf");
        vrmText.setTypeface(tf);
        TextView locText = (TextView) this.findViewById(R.id.txtLocation_info);
        locText.setText(Location);
        TextView osText = (TextView) this.findViewById(R.id.txtOS);
        osText.setText(osLoc);
        TextView codeText = (TextView) this.findViewById(R.id.txtOffence);
        codeText.setText(code);
    }

    public TimerListItem getTimerView()
    {
        return timerView;
    }

    private void initView()
    {
        timerView = (TimerListItem) this.findViewById(R.id.timerView);
    }

    public void set(long timerLength, long timeLeft, boolean drawRed) {
        timerView.set(timerLength,timeLeft,drawRed);
    }

    public void setTime(long time, boolean forceUpdate){
        timerView.setTime(time,forceUpdate);
    }

    public void start()
    {
        timerView.start();
    }

    public void timesUp()
    {
        timerView.timesUp();
    }

    public void done()
    {
        timerView.done();
    }

    public void changeViewToDone(String pcn, long issuedTime, Boolean isIssued)
    {
        timerView.setVisibility(GONE);
        LinearLayout infoPanel = (LinearLayout) findViewById(R.id.infoPanelLayout);
        infoPanel.setVisibility(VISIBLE);

        TextView pcnText        = (TextView) findViewById(R.id.txtPCNItemText);
        TextView issueTimeText  = (TextView) findViewById(R.id.txtIssueTime);
        String issuedAt = new DateTime(issuedTime).toString("HH:mm dd/MM/yyyy");

        if (isIssued) {
            pcnText.setText(pcnText.getText().toString() + " " + pcn);
            issueTimeText.setText(issueTimeText.getText().toString() + " " + issuedAt);
        } else {
            //Check whether PCN is printed or not. Changing the pcnText value here
            long pcnPrintTime = 0;
            List<PCNTable> pcns = DBHelper.GetPCN(pcn);
            if (pcns.size() > 0) {
                PCNTable finalPcn = pcns.get(0);
                pcnPrintTime = finalPcn.getPrintTime();
            }
            if(pcnPrintTime > 0 && issuedTime == 0){
                pcnText.setText(pcnText.getText().toString() + " " + pcn);
                issueTimeText.setText("Printed but\nNot completed");
            } else {
                pcnText.setText("To be Issued");
                issueTimeText.setText("");
            }

        }
    }
}
