/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  Modified by Hanson Aboagye 04/2014
 */

package com.farthestgate.android.ui.pcn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.helper.fused.CodeRedTrackingService;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.ui.admin.LoginActivity;
import com.farthestgate.android.ui.components.CircleButtonsLayout;
import com.farthestgate.android.ui.components.PCNListItem;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.ui.components.timer.TimerListItem;
import com.farthestgate.android.ui.components.timer.TimerObj;
import com.farthestgate.android.ui.components.timer.Timers;
import com.farthestgate.android.ui.components.widget.sgv.GridAdapter;
import com.farthestgate.android.ui.dialogs.LogLocationDialog;
import com.farthestgate.android.ui.messages.GetMessagesAsyncTask;
import com.farthestgate.android.ui.messages.IMessagesCallBack;
import com.farthestgate.android.ui.messages.MessagesActivity;
import com.farthestgate.android.ui.photo_gallery.GalleryActivity;
import com.farthestgate.android.utils.AnalyticsUtils;
import com.farthestgate.android.utils.CroutonUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imense.anpr.launchPT.ImenseLicenseServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import static android.content.Context.JOB_SCHEDULER_SERVICE;


public class ObservationListFragment extends Fragment implements OnClickListener,IMessagesCallBack
{
    private static final String TAG = "ObservationListFragment";
    private static ObservationListAdapter mAdapter;

    /***Changes for imense ANPR integration:Start***/
    //invocation codes for ANPR/ALPR Platform
    private final static String INVOCATION_USER  = "dj^ZjVwGs&dbalHS£gd";//"dj^ZjVwGs&dbalHS�gd"; //Standard user: not allowed to change preferences or view list entries
    private final static String INVOCATION_ADMIN = "Bsv$28!Gsda7jeK^V1s"; //Privileged user: able to change settings and/or edit list entries
    private static int REQUESTCODE = 55;
    //return messages from ANPR/ALPR Platform
    private final static int PT_INVALID_INVOCATION = 99;
    private final static int PT_LICENSE_MISSING_OR_INVALID = 100;
    private final static int PT_ANPR_NOTONWHITELIST = 101;
    private final static int PT_ANPR_PERMITEXPIRED = 102;
    Intent parkTellIntent = null;
    /***Changes for imense ANPR integration:End***/

    private Bundle mViewState = null;
    private ListView mTimersList;
    private List<PCN> pcnList;

    private ImageButton btnAddObservation;

    private ImageButton btnNewLocation;
    private ImageButton btnGallery;
    private ImageButton btnGalleryView1;
    private View mTimerFooter;

    private boolean mTicking = false;

    private NotificationManager mNotificationManager;
    private OnEmptyListListener mOnEmptyListListener;
    private View mLastVisibleView = null;  // used to decide if to set the view or animate to it.
    private ImageButton btnOptionMore,btnOptionSecondMore, btnOptionPrevious;
    private LinearLayout timer_footer, timer_footer_more, timer_footer_last_more;
    private ImageButton btnRecordDefect, btnVRMLookup,btnMessages, btnOptionLess, btnAnpr, btnRedCode;
    private TextView btnMessagesCount;
    private int messageCount=0;
    private int READ_MESSAGE_ACTIVITY=1000;
    private Gson gson;
    private boolean buttonClick = false;
    SharedPreferenceHelper sph;
    private FusedLocationProviderClient fusedLocationClient;

    public static double latitude;
    public static double longitude;
    public static Activity activity;
    private final static int REQUEST_PERMISSIONS_REQUEST_CODE=201;
    private CodeRedLocationReceiver redCodelocationReceiver;
    private JSONObject address=new JSONObject();
    JobScheduler jobScheduler;


    public ObservationListFragment() {
    }

    class ClickAction {
        public static final int ACTION_STOP = 1;

        public int mAction;
        public TimerObj mTimer;

        public ClickAction(int action, TimerObj t) {
            mAction = action;
            mTimer = t;
        }
    }

    // Container Activity that requests TIMESUP_MODE must implement this interface
    public interface OnEmptyListListener {
        public void onEmptyList();
        public void onListChanged();
        public void onEndOfDay();
    }

    ObservationListAdapter createAdapter(Context context) {
        if (mOnEmptyListListener == null) {
            return new ObservationListAdapter(context);
        } else {
            return new ObsTimeUpListAdapter(context);
        }
    }

    public class ObservationListAdapter extends GridAdapter
    {

        ArrayList<TimerObj> mTimers = new ArrayList<TimerObj>();
        Context mContext;


        public ObservationListAdapter(Context context) {
            mContext = context;


        }

        @Override
        public int getCount() {
            return mTimers.size();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public TimerObj getItem(int p) {
            return mTimers.get(p);
        }

        @Override
        public long getItemId(int p) {
            if (p >= 0 && p < mTimers.size()) {
                return mTimers.get(p).mTimerId;
            }
            return 0;
        }

        public void deleteTimer(int id) {
            for (int i = 0; i < mTimers.size(); i++) {
                TimerObj t = mTimers.get(i);

                if (t.mTimerId == id) {
                    if (t.mView != null) {
                        ((TimerListItem) t.mView).stop();
                    }
                    t.deleteFromDB();
                    mTimers.remove(i);

                    notifyDataSetChanged();
                    return;
                }
            }
        }

        protected int findTimerPositionById(long id) {
            for (int i = 0; i < mTimers.size(); i++) {
                TimerObj t = mTimers.get(i);
                if (t.mTimerId == id) {
                    return i;
                }
            }
            return -1;
        }

        public void removeTimer(TimerObj timerObj) {
            int position = findTimerPositionById(timerObj.mTimerId);
            if (position >= 0) {
                mTimers.remove(position);
                notifyDataSetChanged();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PCNListItem v = new PCNListItem(mContext);
            final TimerObj o = getItem(position);

            v.setTag(position);
            o.mView = v.getTimerView();
            long timeLeft =  o.updateTimeLeft(false);
            boolean drawRed = o.mState != TimerObj.STATE_RESTART;
            v.set(o.mOriginalLength, timeLeft, drawRed);
            v.setTime(timeLeft, true);

            PCN info = gson.fromJson(o.pcnJSON,PCN.class);

            TextView VRMText = (TextView) v.findViewById(R.id.registration_mark);
            TextView locText = (TextView ) v.findViewById(R.id.txtLocation_info);
            v.setInfo(info.registrationMark, info.location.streetCPZ.streetname,
                    info.contravention.contraventionCode, info.location.outside);


            VRMText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewObservation(v);
                }
            });
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewObservation(v);
                }
            });
            locText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewObservation(v);
                }
            });

            v.getTimerView().setBackgroundColor(getResources().getColor(R.color.blackish));
            CircleButtonsLayout circleLayout =
                    (CircleButtonsLayout)v.getTimerView().findViewById(R.id.timer_circle);
            circleLayout.setCircleTimerViewIds(
                    R.id.timer_time,0,0,
                    0,
                    R.dimen.plusone_reset_button_padding, R.dimen.delete_button_padding,
                    0,0);

            switch (o.mState) {
                case TimerObj.STATE_RUNNING:
                    v.start();
                    break;
                case TimerObj.STATE_TIMESUP:
                    v.timesUp();
                    v.changeViewToDone(info.pcnNumber,info.issueTime,info.isUsed);
                    break;
                case TimerObj.STATE_INSTANT:
                case TimerObj.STATE_DONE:
                default:
                    v.done();
                    v.changeViewToDone(info.pcnNumber,info.issueTime,info.isUsed);
                    break;

            }

            return v;
        }

        private void viewObservation(View v)
        {
            Integer tag = 0;

            if (v.getTag() == null)
            {
                tag = (Integer) ((PCNListItem)v.getParent().getParent().getParent().getParent()).getTag();
            }
            else
            {
                tag = (Integer) v.getTag();
            }

            TimerObj timerObj = getItem(tag);

            Intent infoIntent = new Intent(getActivity(), PCNLoggingActivity.class);
            infoIntent.putExtra("timerObj",timerObj);
            infoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivityForResult(infoIntent, CeoApplication.RESULT_CODE_PCNLIST);
        }

        @Override
        public int getItemColumnSpan(Object item, int position) { return 1;  }

        public void addTimer(TimerObj t) {
            mTimers.add(0, t);
            sort();
        }

        public void onSaveInstanceState(Bundle outState) {
            TimerObj.putTimersInDB(mTimers);
        }

        public void onRestoreInstanceState(Bundle outState) {
            mTimers = TimerObj.getTimersFromDatabase();
        }

        public void saveGlobalState() {
            TimerObj.putTimersInDB(mTimers);
        }

        public void sort() {
            if (getCount() > 0) {
                Collections.sort(mTimers, mTimersCompare);
                notifyDataSetChanged();
            }
        }

        private final Comparator<TimerObj> mTimersCompare = new Comparator<TimerObj>() {
            static final int BUZZING = 0;
            static final int IN_USE = 1;
            static final int NOT_USED = 2;

            protected int getSection(TimerObj timerObj) {
                switch (timerObj.mState) {
                    case TimerObj.STATE_TIMESUP:
                        return BUZZING;
                    case TimerObj.STATE_RUNNING:
                    case TimerObj.STATE_STOPPED:
                        return IN_USE;
                    default:
                        return NOT_USED;
                }
            }

            @Override
            public int compare(TimerObj o1, TimerObj o2) {
                int section1 = getSection(o1);
                int section2 = getSection(o2);
                if (section1 != section2) {
                    return (section1 < section2) ? -1 : 1;
                } else if (section1 == BUZZING || section1 == IN_USE) {
                    return (o1.mTimeLeft < o2.mTimeLeft) ? -1 : 1;
                } else {
                    return (o1.mSetupLength < o2.mSetupLength) ? -1 : 1;
                }
            }
        };
    }

    class ObsTimeUpListAdapter extends ObservationListAdapter {

        public ObsTimeUpListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            // This adapter has a data subset and never updates entire database
            // Individual timers are updated in button handlers.
        }

        @Override
        public void saveGlobalState() {
            // This adapter has a data subset and never updates entire database
            // Individual timers are updated in button handlers.
        }

        @Override
        public void onRestoreInstanceState(Bundle outState) {
            // This adapter loads a subset
            mTimers = TimerObj.getTimersFromDatabase(TimerObj.STATE_TIMESUP);

            if (getCount() == 0) {
                mOnEmptyListListener.onEmptyList();
            } else {
                Collections.sort(mTimers, new Comparator<TimerObj>() {
                    @Override
                    public int compare(TimerObj o1, TimerObj o2) {
                        return (o1.mTimeLeft < o2.mTimeLeft) ? -1 : 1;
                    }
                });
            }
        }
    }

    private final Runnable mClockTick = new Runnable() {
        boolean mVisible = true;
        final static int TIME_PERIOD_MS = 1000;
        final static int SPLIT = TIME_PERIOD_MS / 2;

        @Override
        public void run() {
            // Setup for blinking
            boolean visible = Utils.getTimeNow() % TIME_PERIOD_MS < SPLIT;
            boolean toggle = mVisible != visible;
            mVisible = visible;
            for (int i = 0; i < mAdapter.getCount(); i ++) {
                TimerObj t = mAdapter.getItem(i);
                if (t.mState == TimerObj.STATE_RUNNING || t.mState == TimerObj.STATE_TIMESUP) {
                    long timeLeft = t.updateTimeLeft(false);
                    if (t.mView != null) {
                        ((TimerListItem)(t.mView)).setTime(timeLeft, false);
                        // Update button every 1/2 second
                        if (toggle) {
                            //TODO: might use an easy way to add mins
                        /*    ImageButton leftButton = (ImageButton)
                                  t.mView.findViewById(R.id.timer_plus_one);
                            leftButton.setEnabled(canAddMinute(t));*/
                        }
                    }
                }
                if (t.mTimeLeft <= 0 && t.mState != TimerObj.STATE_DONE
                        && t.mState != TimerObj.STATE_RESTART) {
                    t.mState = TimerObj.STATE_TIMESUP;

                    if (t.mView != null) {
                        ((TimerListItem)(t.mView)).timesUp();
                    }
                }

                // The blinking
                if (toggle && t.mView != null) {
                    if (t.mState == TimerObj.STATE_TIMESUP) {
                        ((TimerListItem)(t.mView)).setCircleBlink(mVisible);
                    }
                    if (t.mState == TimerObj.STATE_STOPPED) {
                        ((TimerListItem)(t.mView)).setTextBlink(mVisible);
                    }
                }
            }
            mTimersList.postDelayed(mClockTick, 20);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Cache instance data and consume in first call to setupPage()
        if (savedInstanceState != null) {
            mViewState = savedInstanceState;
        }
        //startClockTicks();
        super.onCreate(savedInstanceState);
        sph = new SharedPreferenceHelper(getActivity());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        redCodelocationReceiver = new CodeRedLocationReceiver();
        sph.saveBoolean(AppConstant.CODE_RED, false);
        pcnList = new ArrayList<PCN>();
        gson = new GsonBuilder().create();
        setHasOptionsMenu(true);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main_menu,menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_observations, container, false);

        // Handle arguments from parent
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(Timers.TIMESUP_MODE)) {
            if (bundle.getBoolean(Timers.TIMESUP_MODE, false)) {
                try {
                    mOnEmptyListListener = (OnEmptyListListener) getActivity();
                } catch (ClassCastException e) {
                    Log.wtf(TAG, getActivity().toString() + " must implement OnEmptyListListener");
                }
            }
        }
        btnAddObservation   = (ImageButton)v.findViewById(R.id.add_observation);
        btnNewLocation      = (ImageButton)v.findViewById(R.id.btnNewLoc);
        btnGallery          = (ImageButton)v.findViewById(R.id.btnGalleryView);
        btnGalleryView1=(ImageButton)v.findViewById(R.id.btnGalleryView1);
        btnOptionMore       = (ImageButton)v.findViewById(R.id.btnOptionMore);

        timer_footer        = (LinearLayout)v.findViewById(R.id.timer_footer);
        timer_footer_more   = (LinearLayout)v.findViewById(R.id.timer_footer_more);
        timer_footer_last_more   = (LinearLayout)v.findViewById(R.id.timer_footer_last_more);
        btnRecordDefect     = (ImageButton)v.findViewById(R.id.btnRecordDefect);
        btnVRMLookup        = (ImageButton)v.findViewById(R.id.btnVRMLookup);
        btnMessages         = (ImageButton)v.findViewById(R.id.btnMessages);
        btnMessagesCount=(TextView) v.findViewById(R.id.btnMessageCount);


        btnOptionLess       = (ImageButton)v.findViewById(R.id.btnOptionLess);
        btnOptionSecondMore       = (ImageButton)v.findViewById(R.id.btnOptionSecondMore);
        btnAnpr             = (ImageButton)v.findViewById(R.id.btnAnpr);
        btnRedCode = (ImageButton)v.findViewById(R.id.btnCodeRed);


        btnOptionPrevious   = (ImageButton)v.findViewById(R.id.btnOptionPrevious);


        btnOptionMore     .setOnClickListener(btnOptionMoreClick);
        btnOptionSecondMore.setOnClickListener(btnOptionSecondMoreClick);
        btnAddObservation   .setOnClickListener(pcnClick);
        btnNewLocation      .setOnClickListener(locClick);
        btnGallery          .setOnClickListener(btnGallleryClick);
        btnGalleryView1.setOnClickListener(btnGalleryView1Click);
        btnRecordDefect     .setOnClickListener(btnRecordDefectClick);
        btnVRMLookup     .setOnClickListener(btnVRMLookupClick);
        btnMessages     .setOnClickListener(btnMessagesClick);
        btnOptionLess     .setOnClickListener(btnOptionLessClick);
        btnAnpr           .setOnClickListener(btnAnprClick);
        btnOptionPrevious .setOnClickListener(btnOptionPreviousClick);

        btnRedCode.setOnTouchListener(btnRedCodeClick);


        mTimersList = (ListView) v.findViewById(R.id.timers_list);

        // Put it on the right for landscape, left for portrait.
        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) btnAddObservation.getLayoutParams();

        btnAddObservation.setLayoutParams(layoutParams);

        mTimerFooter = v.findViewById(R.id.timer_footer);
        mTimerFooter.setVisibility(mOnEmptyListListener == null ? View.VISIBLE : View.GONE);
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if(CeoApplication.useAnpr()){
            btnGalleryView1.setVisibility(View.VISIBLE);
            btnMessages.setVisibility(View.GONE);
            btnGallery.setVisibility(View.GONE);
            btnAnpr.setVisibility(View.VISIBLE);
        } else{
            btnGalleryView1.setVisibility(View.GONE);
            btnMessages.setVisibility(View.VISIBLE);
            btnGallery.setVisibility(View.VISIBLE);
            btnAnpr.setVisibility(View.GONE);
        }
        // TODO: Need to verify hide or not
        btnMessages.setVisibility(View.VISIBLE);

        // set message counter and call api to fetch new messages
        //messageCountUIUpdate();
        callMessagesAPI();
        return v;
    }


    @Override
    public void updateMessage() {
        messageCountUIUpdate();
    }

    private void messageCountUIUpdate(){
        this.messageCount=DBHelper.reviewBriefNotesList(0).size();
        Log.i(TAG,"Call Message Timer Task Response==>"+messageCount);
        if(messageCount>0){
            btnMessagesCount.setVisibility(View.VISIBLE);
            btnMessagesCount.setText(String.valueOf(messageCount));
        }else{
            btnMessagesCount.setVisibility(View.INVISIBLE);
        }
    }


    private void callMessagesAPI(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.i(TAG,"Call Message Timer Task");
                            new GetMessagesAsyncTask(ObservationListFragment.this).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, CeoApplication.getMessageReadTimerInMinute()); //execute in every 50000 ms
    }

    public static ObservationListAdapter getAdapter()
    {
        return mAdapter;
    }

    private OnClickListener pcnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoPCNView(); }
    };

    private OnClickListener locClick =
            new OnClickListener() {
        @Override
        public void onClick(View v)
        {
            String stName = "";
            if (VisualPCNListActivity.currentStreet != null)
            {
                stName = VisualPCNListActivity.currentStreet.streetname;
            }
            LogLocationDialog logLocationDialog = new LogLocationDialog();
            logLocationDialog.setCancelable(VisualPCNListActivity.firstLogin);
            logLocationDialog.show(getFragmentManager(), "");
        }
    };

    private OnClickListener btnGallleryClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent galIntent = new Intent(getActivity(), GalleryActivity.class);
            startActivity(galIntent);
        }
    };

    private OnClickListener btnGalleryView1Click = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            AnalyticsUtils.trackGalleryButton();
            Intent galIntent = new Intent(getActivity(), GalleryActivity.class);
            startActivity(galIntent);
        }
    };



    private OnClickListener btnRecordDefectClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            AnalyticsUtils.trackDetectsButton();
            Intent defectIntent = new Intent(getActivity(), DefectRecordingActivity.class);
            startActivity(defectIntent);
        }
    };
    private OnClickListener btnVRMLookupClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            AnalyticsUtils.trackVRMLookUpButton();
            CeoApplication.isVrmLook  = true;
            Intent defectIntent = new Intent(getActivity(), VRMLookupSummaryActivity.class);
            startActivity(defectIntent);
        }
    };
    private OnClickListener btnMessagesClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            AnalyticsUtils.trackMessagesButton();

            try {
                if(getActivity()==null){
                    return;
                }
                if(messageCount>0){
                    Intent intent=new Intent(getActivity(),MessagesActivity.class);
                    getActivity().startActivity(intent);
                }
                else{
                    Toast.makeText(getActivity(),"Currently, there are no unread messages. Please wait.",Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                e.printStackTrace();
            }



        }
    };
    private OnClickListener btnOptionLessClick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            timer_footer_more.setVisibility(View.INVISIBLE);
            timer_footer.setVisibility(View.VISIBLE);
        }
    };

    private OnClickListener btnOptionMoreClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            timer_footer.setVisibility(View.INVISIBLE);
            timer_footer_more.setVisibility(View.VISIBLE);
           /*int[] location = new int[2];
           v.getLocationOnScreen(location);
           Point point = new Point();
           point.x = location[0];
           point.y = location[1];
           showMoreOptionPopup(getActivity(), point);*/
        }
    };

    private OnClickListener btnOptionSecondMoreClick = new OnClickListener() {
        @Override
        public void onClick(View v) {

            timer_footer_more.setVisibility(View.INVISIBLE);
            timer_footer_last_more.setVisibility(View.VISIBLE);
        }
    };

    private OnClickListener btnOptionPreviousClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            timer_footer_more.setVisibility(View.VISIBLE);
            timer_footer_last_more.setVisibility(View.INVISIBLE);
        }
    };





   @SuppressLint("NewApi")
   private View.OnTouchListener btnRedCodeClick = new View.OnTouchListener() {

    private int longClickDuration = 5000;
    private boolean isLongPress = false;



    @Override
    public boolean onTouch(View v, MotionEvent event) {

       final  View view = v;
               if (event.getAction() == MotionEvent.ACTION_DOWN) {
                   isLongPress = true;
                   Handler handler = new Handler();
                   handler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           if (isLongPress) {

                               AnalyticsUtils.trackCodeRedButton();
                               Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                               vibrator.vibrate(100);
                               // set your code here
                               // Don't forgot to add <uses-permission android:name="android.permission.VIBRATE" /> to vibrate.
                               if(!sph.getBoolan(AppConstant.CODE_RED))
                               {
                                   btnRedCode.setEnabled(true);
                                   ((ImageButton) view).setImageResource(R.drawable.code_red_cancel);
                                   sph.saveBoolean(AppConstant.CODE_RED, true);
                                   LocalBroadcastManager.getInstance(getActivity()).registerReceiver(redCodelocationReceiver,
                                           new IntentFilter(CodeRedTrackingService.ACTION_BROADCAST));
                                   Toast.makeText(getActivity(), "Code red has been sent", Toast.LENGTH_LONG).show();
                                   startBackgroundTask();
                                   //startTracking();
                               }else
                               {
                                   btnRedCode.setEnabled(true);
                                   ((ImageButton) view).setImageResource(R.drawable.red_code);
                                   sph.saveBoolean(AppConstant.CODE_RED, false);
                                   LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(redCodelocationReceiver);
                                   jobScheduler.cancel(2);
                                   Toast.makeText(getActivity(), "Code red has been cancelled", Toast.LENGTH_LONG).show();
                                   //disposables.dispose();
                                   publishToPubnub();
                               }
                           }
                       }
                   }, longClickDuration);
               } else if (event.getAction() == MotionEvent.ACTION_UP) {
                   isLongPress = false;
               }
               return true;
           }
   };



    private OnClickListener btnAnprClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //Intent intent = new Intent(getActivity(), TakePhoto.class);
            //startActivity(intent);
            AnalyticsUtils.trackANPRButton();

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ObservationListFragment.this.launchPT(true, false);
            } else {
                ObservationListFragment.this.launchPT(false, true);
            }

        }
    };

    private void showMoreOptionPopup(final Activity context, Point p) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.more_option_pop, null);

        PopupWindow moreOptionPopUp = new PopupWindow(context);
        moreOptionPopUp.setContentView(v);
        moreOptionPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        moreOptionPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        moreOptionPopUp.setFocusable(true);

        ImageButton btnRecordDefect     = (ImageButton)v.findViewById(R.id.btnRecordDefect);
        ImageButton btnVRMLookup        = (ImageButton)v.findViewById(R.id.btnVRMLookup);
        btnRecordDefect     .setOnClickListener(btnRecordDefectClick);
        btnVRMLookup        .setOnClickListener(btnVRMLookupClick);

        /*int OFFSET_X = -20;
        int OFFSET_Y = 50;*/
        int OFFSET_X = 0;
        int OFFSET_Y = 0;
       /* moreOptionPopUp.setBackgroundDrawable(new BitmapDrawable());*/
        moreOptionPopUp.showAtLocation(v, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
    }

    @Override
    public void onDestroyView() {
        mViewState = new Bundle();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        Intent newIntent = null;

        //TODO: consider this scenario - if there is a new intent
        //newIntent = activity.getIntent();
        super.onResume();

        mAdapter = createAdapter(getActivity());
        mAdapter.onRestoreInstanceState(null);


        mTimersList.setAdapter(mAdapter);
        if (mAdapter.getCount() == 0) {

            // Check CEO logged in
            if (DBHelper.GetPCNCount(((VisualPCNListActivity)getActivity()).currentSession) > 0)
                reloadAdapter();
        }
        mLastVisibleView = null;   // Force a non animation setting of the view

        // View was hidden in onPause, make sure it is visible now.
        View v = getView();
        if (v != null) {
            getView().setVisibility(View.VISIBLE);
        }
        startClockTicks();
        if (newIntent != null) {
            processIntent(newIntent);
        }

        messageCountUIUpdate();
    }

    @Override
    public void onPause() {
        //TODO: FIx this
        /*if (getActivity() instanceof DeskClock) {
            ((DeskClock)getActivity()).unregisterPageChangedListener(this);
        }*/
        super.onPause();
        stopClockTicks();
        if (mAdapter != null) {
            mAdapter.saveGlobalState ();
        }
        // This is called because the lock screen was activated, the window stay
        // active under it and when we unlock the screen, we see the old time for
        // a fraction of a second.
        View v = getView();
        if (v != null) {
            v.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            mAdapter.onSaveInstanceState (outState);
        }

        // examine the contents here
    }



    /**
     *  method to start new PCN
     */
    private void gotoPCNView() {
        stopClockTicks();
        startActivityForResult(new Intent(getActivity(), PCNStartActivity.class),CeoApplication.REQUEST_CODE_START_OBS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        /***Changes for imense ANPR integration:Start***/
        int returnMessage = 0;
        if (data!=null) returnMessage = data.getExtras().getInt("message");
        if(returnMessage != 0) {
            if (returnMessage == PT_ANPR_NOTONWHITELIST) {
                String sRegNumber = data.getExtras().getString("anpr_not_in_whitelist");
                mListener.checkAutomatedVRMLookup(sRegNumber);
            } else if (returnMessage == PT_ANPR_PERMITEXPIRED) {
                String sRegNumber = data.getExtras().getString("anpr_permit_expired");
                mListener.checkAutomatedVRMLookup(sRegNumber);
            } else if (returnMessage == PT_LICENSE_MISSING_OR_INVALID) {
                final String deviceID = data.getExtras().getString("duid");
                new AlertDialog.Builder(getActivity())
                        .setTitle("License Verification Problem")
                        .setCancelable(false)
                        .setMessage("Platform reports: license key missing or invalid. Please ensure that your device's WiFi adapter is enabled and has Internet access, then " +
                                "click <" + this.getString(android.R.string.ok) + "> to (re)generate a valid license key from our server.")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        new ImenseLicenseServer(getActivity(), deviceID).execute();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

            }
        }else {
            /***Changes for imense ANPR integration:End***/
            switch (resultCode) {

                case CeoApplication.RESULT_CODE_OBS_CANCEL:  // back from startActivity
                {
                    try {
                        if (data != null)
                            mAdapter.deleteTimer(data.getIntExtra("id", 0));
                    } catch (RuntimeException rEx) {
                        // ignore as if data was null
                    }
                    break;
                }
                case CeoApplication.RESULT_CODE_OBS_CONTINUE: // back from startActivity
                {
                    break;
                }
                case CeoApplication.RESULT_CODE_PCNLOGGING:
                case CeoApplication.RESULT_CODE_NOTES: {

                    break;
                }
                case CeoApplication.RESULT_CODE_PCNLIST: {
                    if (data.hasExtra("timer")) {
                        TimerObj t = data.getParcelableExtra("timer");
                        if (t.mState != TimerObj.STATE_DONE && t.mState != TimerObj.STATE_INSTANT) {
                            if (t.mState == TimerObj.STATE_TIMESUP)
                                t.mState = TimerObj.STATE_DONE;
                            else
                                t.mState = TimerObj.STATE_RUNNING;

                        }

                        mAdapter.addTimer(t);
                        //    if (t.mState != TimerObj.STATE_DONE)
                        updateTimersState(t, Timers.START_TIMER);

                    }
                    pcnList.add(data.<PCN>getParcelableExtra("PCN"));

                    startClockTicks();
                    break;
                }
                case CeoApplication.RESULT_CODE_NEWIMAGE: {

                    break;
                }
                case CeoApplication.RESULT_CODE_VEHICLE_DIALOG: {
                    break;
                }
                case CeoApplication.RESULT_CODE_ERROR: {
                    CeoApplication.LogoutProcess();
                    startActivity(new Intent(CeoApplication.getContext(), LoginActivity.class));
                    getActivity().finish();
                    break;
                }

            }
        }
        //What is the need of this statement:super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        ClickAction tag = (ClickAction) v.getTag();
        onClickHelper(tag);
    }

    private void onClickHelper(ClickAction clickAction) {
        switch (clickAction.mAction) {
            case ClickAction.ACTION_STOP:

                break;
            default:
                break;
        }
    }


    // Starts the ticks that animate the timers.
    private void startClockTicks() {
        mTimersList.postDelayed(mClockTick, 20);
        mTicking = true;
    }

    // Stops the ticks that animate the timers.
    private void stopClockTicks() {
        if (mTicking) {
            mTimersList.removeCallbacks(mClockTick);
            mTicking = false;
        }
    }

    /**
     *  usefull function to extend loading time
     *
     * @param t
     * @return
     */


    private void updateTimersState(TimerObj t, String action) {
        t.writeToDB();
        Intent i = new Intent();
        i.setAction(action);
        i.putExtra(Timers.TIMER_INTENT_EXTRA, t.mTimerId);
        // Make sure the receiver is getting the intent ASAP.
        i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        getActivity().sendBroadcast(i);
    }

    private void cancelTimerNotification(int timerId) {
        mNotificationManager.cancel(timerId);
    }

    private void updateTimesUpMode(TimerObj timerObj) {
        if (mOnEmptyListListener != null && timerObj.mState != TimerObj.STATE_TIMESUP) {
            mAdapter.removeTimer(timerObj);
            if (mAdapter.getCount() == 0) {
                mOnEmptyListListener.onEmptyList();
            } else {
                mOnEmptyListListener.onListChanged();
            }
        }
    }
    // Process extras that were sent to the app and were intended for the PCN
    // fragment
    public void processIntent(Intent intent) {
        // switch to timer setup view

        // redirect event possibly ?
        intent.getExtras();

        //    gotoPCNView();

    }

    private void reloadAdapter()
    {
        mAdapter = createAdapter(getActivity());
        mAdapter.onRestoreInstanceState(null);
        mTimersList.setAdapter(mAdapter);
    }

    /***Changes for imense ANPR integration:Start***/
    void launchPT( boolean admin, boolean portraitOrientation )
    {
        //obtain an Intent to launch ANPR/ALPR Platform
        try {
            parkTellIntent = new Intent();
            parkTellIntent.setComponent(new ComponentName("com.imense.anprPlatform", "com.imense.anprPlatform.ImenseParkingEnforcer"));
            //authenticate the request with the correct invocation code
            if (admin) parkTellIntent.putExtra("invocationcode", INVOCATION_ADMIN);
            else parkTellIntent.putExtra("invocationcode", INVOCATION_USER);
            //set PT into portrait mode (not recommended since it reduces effective plate pixel resolution)
            if (portraitOrientation) parkTellIntent.putExtra("orientation", "portrait");
            //optionally instruct PT to start scan (i.e. invoke shutter button) immediately
            parkTellIntent.putExtra("startscan", "1"); //leave undefined or set to "0" to not start scan immediately
			/*//////////////////////////////
			//Optionally explicitly specify values for settings such as folder for data and images, option to save context image, scan time threshold, minimum confidence threshold, region and read options.

			///////////List settings
			parkTellIntent.putExtra("preferences_saveimages_path", "/mnt/sdcard"); //Folder for data and images; has to exist and be writable
			parkTellIntent.putExtra("preferences_vehiclesfilename", "parkingList.csv"); //Vehicles list file name. Default value="parkingList.csv"
			parkTellIntent.putExtra("preferences_alertsfilename", "parkingAlerts.csv"); //Alerts list\Whitelist file name. Default value="parkingAlerts.csv"

			///////////General settings
			parkTellIntent.putExtra("preferences_savecutouts", "true"); //Save plate cut-out image after every good read. Value can be "true" or "false" (default="true")
			parkTellIntent.putExtra("preferences_savecontextimages", "false"); //Save context image to SD card after every good read. Value can be "true" or "false" (default="false")

			parkTellIntent.putExtra("preferences_expungePlatesAfterNhours", "72"); //Expunge vehicle list entries after this many hours. Value must be positive numeric, default="72".
			parkTellIntent.putExtra("preferences_warnAfterNmins", "0"); //Warn if parked vehicle time exceeds this many minutes. Value must be positive numeric, default="0".

			parkTellIntent.putExtra("preferences_confGoodread", "80"); //"High confidence threshold (0-100). Value must be positive numeric, default="80".

			parkTellIntent.putExtra("preferences_scanTimeout", "90"); //Continuous scan timeout (seconds). Value must be positive numeric, default="90".
			parkTellIntent.putExtra("preferences_playsound", "true"); //Play beep after every high confidence scan. Value can be "true" or "false" (default="true")
			parkTellIntent.putExtra("preferences_showUVC", "false"); //Display button to connect to USB camera (via UVC) if possible. Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_showsingleshot", "false"); //Display button to save single image to SD card. Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_saveSingleshotInColour", "false"); //Store single PIC images in colour. Value can be "true" or "false" (default="false")

			parkTellIntent.putExtra("preferences_showtorch", "false"); //Display torch button. Value can be "true" or "false" (default="false")

			parkTellIntent.putExtra("preferences_viewfinder", "false"); //Enable adjustable zone-of-interest within viewfinder for faster processing. Value can be "true" or "false" (default="false")

			///////////Parking Bay Numbers
			parkTellIntent.putExtra("preferences_pbn_enable", "false"); //Automatically apply PBN (parking bay number). Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_pbn_prefix", ""); //PBN prefix string. Text value of 0 to 5 characters, default is "" (empty string).
			parkTellIntent.putExtra("preferences_pbn_start", "00"); //PBN start value that is applied to the next parking bay. Must be a string of digits of between 2 and 5 characters, default is "00".
			parkTellIntent.putExtra("preferences_pbn_increment", "1"); //PBN increment value (can be positive or negative). Must be a string of digits (optionally starting with "-" to indicate a negative increment) of between 1 and 3 characters, default is "1".

			///////////Custom Data Fields
			parkTellIntent.putExtra("preferences_data1prompt", "Custom Data 1"); //Prompt for custom data field 1. Must be a text string of 0 to 20 characters, default is "Custom Data 1".
			parkTellIntent.putExtra("preferences_data2prompt", "Custom Data 2"); //Prompt for custom data field 2. Must be a text string of 0 to 20 characters, default is "Custom Data 2".
			parkTellIntent.putExtra("preferences_data3prompt", "Custom Data 3"); //Prompt for custom data field 3. Must be a text string of 0 to 20 characters, default is "Custom Data 3".

			parkTellIntent.putExtra("preferences_audiomax", "60"); //Maximum duration of voice note audio recordings in seconds. Value must be positive numeric, default="60".

			//**/
            //if we already have a license key, we send it to Platform
            //if ( ((VisualPCNListActivity)getActivity()).licenseKey!=null) parkTellIntent.putExtra("licensekey", ((VisualPCNListActivity)getActivity()).licenseKey);
            String savedLicenseKey = DBHelper.getConfig();
            if(savedLicenseKey != null && !savedLicenseKey.isEmpty())parkTellIntent.putExtra("licensekey", savedLicenseKey);
            startActivityForResult(parkTellIntent, REQUESTCODE);
        }
        catch (Exception err) {
            //Toast.makeText(getActivity(), "ANPR Platform not found: please install it", Toast.LENGTH_LONG).show();
            CroutonUtils.info(getActivity(), "ANPR Platform not found: please install it");
        }

    }

    public interface OnVRMRead {
        void checkAutomatedVRMLookup(String vrm);
    }
    private OnVRMRead mListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnVRMRead) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    /***Changes for imense ANPR integration:End***/




    @SuppressLint("NewApi")
    public void startBackgroundTask() {
        jobScheduler = (JobScheduler) getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(getActivity(), CodeRedTrackingService.class);
        JobInfo jobInfo = new JobInfo.Builder(2, componentName)
                .setMinimumLatency(1000) //1 sec interval
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setRequiresCharging(false).build();
        int result=jobScheduler.schedule(jobInfo);
        Log.e("jobid", String.valueOf(result));
        if (result == JobScheduler.RESULT_SUCCESS)
        {
            com.farthestgate.android.utils.Log.i("Success");
        }
        else
        {
            com.farthestgate.android.utils.Log.i("Failed");
        }
    }

    private class CodeRedLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            com.farthestgate.android.utils.Log.d("location updated");
            try {
                publishToPubnub();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

   private void publishToPubnub()
   {
       fusedLocationClient.getLastLocation()
               .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                   @Override
                   public void onSuccess(Location location) {
                       // Got last known location. In some rare situations this can be null.
                       String stName = null;
                       if (VisualPCNListActivity.currentStreet != null)
                       {
                           stName = VisualPCNListActivity.currentStreet.streetname;
                       }
                       try {

                           Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                           List<Address> addresses=  addresses= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                           address.put("city",addresses.get(0).getLocality());
                           address.put("state",addresses.get(0).getAdminArea());
                           address.put("country",addresses.get(0).getCountryName());
                           address.put("postalCode",addresses.get(0).getPostalCode());
                           address.put("knownName",addresses.get(0).getFeatureName());

                           JSONObject jsonObject = new JSONObject();
                           JSONObject latLong = new JSONObject();
                           latLong.put("lat", location.getLatitude());
                           latLong.put("long", location.getLongitude());
                           jsonObject.put("ceoshouldernumber", DBHelper.getCeoUserId());
                           jsonObject.put("currentstreet", stName);
                           jsonObject.put("currentTime", AppConstant.ISO8601_DATE_TIME_FORMAT.format(new Date()));
                           if(sph.getBoolan(AppConstant.CODE_RED)) {
                               jsonObject.put("action", AppConstant.CODE_RED);
                           }else{
                               jsonObject.put("action", "cancelled");
                           }
                           jsonObject.put("latlong", latLong);
                           jsonObject.put("locationaddress",address);
                           PubNubModule.publishCodeRed(jsonObject);

                       } catch (JSONException e) {
                           e.printStackTrace();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               });
   }
}
