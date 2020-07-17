package com.farthestgate.android.ui.messages;

import android.app.Fragment;
import android.os.Bundle;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.database.BriefingNotesTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BriefingNotesFragment extends Fragment implements View.OnClickListener,IMessagesCallBack {
    private static String TAG = BriefingNotesFragment.class.getName();

    //private Activity activity;
    private ScrollView briefingNoteMainScroll;
    private LinearLayout briefing_notes_header, briefing_notes_footer;
    private TextView briefing_notes_title, briefing_notes_count, no_briefing_notes;
    private WebView briefing_notes_content;
    private CheckedTextView declaration;
    private Button btnBack, btnNext, btnExit;
    private ImageView icon_complete;
    private SwipeRefreshLayout swipeContainer;

    private boolean isReviewMode = false;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private List<BriefingNotesTable> briefingNotesTables;
    private int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity = (Activity) getContext();
        //((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_briefing_notes, container, false);
        Bundle bundle = getArguments();
       /* if(bundle != null)
            isReviewMode = bundle.getBoolean("isReviewMode");
*/
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        briefing_notes_title = (TextView) view.findViewById(R.id.briefing_notes_title);
        briefing_notes_count = (TextView) view.findViewById(R.id.briefing_notes_count);
        no_briefing_notes = (TextView) view.findViewById(R.id.no_briefing_notes);
        briefing_notes_content = (WebView) view.findViewById(R.id.briefing_notes_content);
        declaration = (CheckedTextView) view.findViewById(R.id.declaration);
        btnBack = (Button) view.findViewById(R.id.btnBack);
        btnNext = (Button) view.findViewById(R.id.btnNext);
        btnExit = (Button) view.findViewById(R.id.btnExit);
        icon_complete = (ImageView) view.findViewById(R.id.icon_complete);
        briefingNoteMainScroll = (ScrollView) view.findViewById(R.id.briefing_notes_main_scroll);
        briefing_notes_header = (LinearLayout) view.findViewById(R.id.briefing_notes_header);
        briefing_notes_footer = (LinearLayout) view.findViewById(R.id.briefing_notes_footer);
        sharedPreferenceHelper = new SharedPreferenceHelper(getActivity());
        /*if(isReviewMode){
            briefingNotesTables = DBHelper.reviewBriefNotesList(1);
            buildUI();
            btnBack.setEnabled(false);
            btnNext.setEnabled(true);
        }else{*/
            getBriefNotesData();
        //}

        new SwipeDetector(briefingNoteMainScroll).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
            @Override
            public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT) {
                    if (count == 0) return;
                    MoveToBack();
                } else if (swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
                    if (count == briefingNotesTables.size()) return;
                    if (declaration.isChecked()) {
                        MoveToNext();
                    }
                }
            }
        });

        swipeContainer.setColorSchemeColors(getResources().getIntArray(R.array.swipe_refresh_colors));
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                if (!isReviewMode) {
                    if(count == 0)
                        getBriefNotesData();
                } else {
                    briefingNotesTables = DBHelper.reviewBriefNotesList(1);
                    buildUI();
                }
                swipeContainer.setRefreshing(false);
            }
        });

        declaration.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void updateMessage() {
       /* briefingNotesTables  = DBHelper.reviewBriefNotesList(0);
        buildUI();*/
    }

    private void getBriefNotesData(){

        //TODO: NOW I AM GETTING DATA FROM LOCAL DB BECAUSE WE ARE FETCHING APIS ON OBSERVATION LIST FRAGMENT: MAY BE WE CAN CHANGE LATER
        // new GetMessagesAsyncTask(this).execute();

        briefingNotesTables  = DBHelper.reviewBriefNotesList(0);
        buildUI();

    }

    private void buildUI() {
        if(briefingNotesTables.size() == 0){
            CroutonUtils.info(getActivity(), "No briefing notes available for CEO : " + DBHelper.getCeoUserId());
            controlUIVisibility(false);
            setHasOptionsMenu(true);
            return;
        } else {
            controlUIVisibility(true);
        }

        clearData();

        if(count == briefingNotesTables.size()) {
            doBriefingCompleted();
        }else {
            int i = count;

            try {
                BriefingNotesTable briefingNotesTable = briefingNotesTables.get(i);
                briefing_notes_title.setText(briefingNotesTable.title);
                byte[] data = Base64.decode(briefingNotesTable.content, Base64.DEFAULT);
                String content = new String(data, "UTF-8");
                briefing_notes_content.loadData(content, "text/html", "UTF-8");
                briefing_notes_count.setText("Notes : " + ++i + "/" + briefingNotesTables.size());
                if(isReviewMode) {
                    briefing_notes_header.setVisibility(View.VISIBLE);
                    briefing_notes_footer.setVisibility(View.VISIBLE);
                    briefing_notes_content.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private void doBriefingCompleted(){

        if(getActivity()!=null)
        ((MessagesActivity)getActivity()).onBackPressed();

      /*  briefing_notes_title.setText("Briefing Completed");
        briefing_notes_title.setTextSize(25);
        briefing_notes_count.setVisibility(View.INVISIBLE);
        briefing_notes_content.setVisibility(View.GONE);
        icon_complete.setVisibility(View.VISIBLE);
        declaration.setVisibility(View.INVISIBLE);
        setHasOptionsMenu(true);
        btnNext.setVisibility(View.INVISIBLE);
        btnExit.setVisibility(View.VISIBLE);*/

    }

    private void clearData(){
        briefing_notes_title.setText("");
        briefing_notes_count.setText("");
        briefing_notes_content.loadData("", "text/html", "UTF-8");
    }

    private void controlUIVisibility(boolean isUIVisisble) {
        if(isUIVisisble){
            briefing_notes_header.setVisibility(View.VISIBLE);
            briefing_notes_footer.setVisibility(View.VISIBLE);
            briefing_notes_content.setVisibility(View.VISIBLE);
            if(isReviewMode)
                declaration.setVisibility(View.INVISIBLE);
            else
                declaration.setVisibility(View.VISIBLE);
            no_briefing_notes.setVisibility(View.GONE);
        } else {
            briefing_notes_header.setVisibility(View.INVISIBLE);
            briefing_notes_footer.setVisibility(View.VISIBLE);
            briefing_notes_content.setVisibility(View.GONE);
            no_briefing_notes.setVisibility(View.VISIBLE);
            declaration.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)btnExit.getLayoutParams();
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            btnExit.setLayoutParams(layoutParams);

            btnNext.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            btnExit.setVisibility(View.VISIBLE);
        }
    }

    private void MoveToBack(){
        Runtime.getRuntime().gc();
        Runtime.getRuntime().freeMemory();
        --count;
        setHasOptionsMenu(false);
        briefing_notes_count.setVisibility(View.VISIBLE);
        briefing_notes_content.setVisibility(View.VISIBLE);
        if(!isReviewMode) {
            icon_complete.setVisibility(View.GONE);
            declaration.setVisibility(View.VISIBLE);
        }else{
            icon_complete.setVisibility(View.GONE);
            declaration.setVisibility(View.INVISIBLE);
        }
        buildUI();
        declaration.setChecked(true);
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setEnabled(true);
        btnExit.setVisibility(View.GONE);
        if(count == 0) {
            btnBack.setEnabled(false);
        }
    }

    private void MoveToNext(){
        Runtime.getRuntime().gc();
        Runtime.getRuntime().freeMemory();
        ++count;
        btnNext.setEnabled(false);
        declaration.setChecked(false);
        btnBack.setEnabled(true);
        if(!isReviewMode) {
            if(briefingNotesTables.get(count-1).getSentToPubNub() == 0) {
                sendDataToPubNub(briefingNotesTables.get(count - 1));
                DBHelper.updateBriefingNotes(briefingNotesTables.get(count - 1));
            }
        }

        if(isReviewMode)
            btnNext.setEnabled(true);

        buildUI();
    }

    /*private void exitApp(){
        ((AppMainActivity)getActivity()).logout();
    }*/

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnBack){
            MoveToBack();
        }
        if(v.getId() == R.id.btnNext){
            MoveToNext();
        }
        if(v.getId() == R.id.btnExit){
            ((MessagesActivity)getActivity()).onBackPressed();
            //exitApp();
        }
        if(v.getId() == R.id.declaration){
            if(declaration.isChecked()){
                declaration.setChecked(false);
                btnNext.setEnabled(false);
            }else{
                declaration.setChecked(true);
                btnNext.setEnabled(true);
            }
        }
    }

    private void sendDataToPubNub(BriefingNotesTable briefingNotesTable){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", DBHelper.getCeoUserId() /*Ceo.GetCeo().userId*/);
            jsonObject.put("messageId", briefingNotesTable.id);
            jsonObject.put("timestamp", DateUtils.getISO8601DateTime());
            jsonObject.put("messageType", "message-read-confirmation");
            jsonObject.put("imei", CeoApplication.getUUID());

            PubNubModule.publishBriefingNotes(jsonObject);
            int index = 0;
            for(BriefingNotesTable localBriefingNote :briefingNotesTables){
                if(localBriefingNote.id == briefingNotesTable.id){
                    break;
                }
                index++;
            }
            briefingNotesTables.get(index).setSentToPubNub(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
