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

package com.farthestgate.android.ui.components.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.model.database.TimerObjTable;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Modiified nu Hanson Aboagye 04/2014
 */
public class TimerObj implements Parcelable {




    public static final long MINUTE_IN_MILLIS = 60 * 1000;

    public int mTimerId;             // Unique id
    public long mStartTime;          // With mTimeLeft , used to calculate the correct time
    public long mTimeLeft;           // in the timer.
    public long mOriginalLength;     // length set at start of timer and by +1 min after times up
    public long mSetupLength;        // length set at start of timer
    public View mView;
    public int mState;
    public String mLabel;
    public String pcnJSON;
    public String mCeo;

    public static final int STATE_INSTANT = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_TIMESUP = 3;
    public static final int STATE_DONE = 4;
    public static final int STATE_RESTART = 5;


    public static final Parcelable.Creator<TimerObj> CREATOR = new Parcelable.Creator<TimerObj>() {
        @Override
        public TimerObj createFromParcel(Parcel p) {
            return new TimerObj(p);
        }

        @Override
        public TimerObj[] newArray(int size) {
            return new TimerObj[size];
        }
    };

    // write or update
    public void writeToDB() {
        TimerObjTable objTimerRecord = new TimerObjTable();
        List<TimerObjTable> timer = DBHelper.getTimer(mTimerId);
        if (timer.size() > 0)
            objTimerRecord = timer.get(0);
        else {
            objTimerRecord.setTimerId(mTimerId);
            objTimerRecord.setTimerStartTimeMillis(DateTime.now().getMillis());
        }
        objTimerRecord.setTimerStartTime(mStartTime);
        objTimerRecord.setTimeLeft(mTimeLeft);
        objTimerRecord.setOriginalTime(mOriginalLength);
        objTimerRecord.setSetupTime(mSetupLength);
        objTimerRecord.setTimerState(mState);
        objTimerRecord.setTimerLabel(mLabel);
        objTimerRecord.setTimerJSON(pcnJSON);
        objTimerRecord.setTimerCEO(mCeo);
        objTimerRecord.save();
    }

    public TimerObj(TimerObjTable timerRecord)
    {
        mTimerId        = timerRecord.getTimerId();
        mStartTime      = timerRecord.getTimerStartTime();
        mTimeLeft       = timerRecord.getTimeLeft();
        mOriginalLength = timerRecord.getOriginalTime();
        mSetupLength    = timerRecord.getSetupTime();
        mState          = timerRecord.getTimerState();
        mLabel          = timerRecord.getTimerLabel();
        pcnJSON         = timerRecord.getTimerJSON();
        mCeo            = timerRecord.getTimerCEO();
    }


    public void deleteFromDB()
    {
        DBHelper.deketeTimer(mTimerId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTimerId);
        dest.writeLong(mStartTime);
        dest.writeLong(mTimeLeft);
        dest.writeLong(mOriginalLength);
        dest.writeLong(mSetupLength);
        dest.writeInt(mState);
        dest.writeString(mLabel);
        dest.writeString(pcnJSON);
        dest.writeString(mCeo);
    }

    public TimerObj(Parcel p) {
        mTimerId = p.readInt();
        mStartTime = p.readLong();
        mTimeLeft = p.readLong();
        mOriginalLength = p.readLong();
        mSetupLength = p.readLong();
        mState = p.readInt();
        mLabel = p.readString();
        pcnJSON = p.readString();
        mCeo = p.readString();
    }

    public TimerObj() {
        this(0);
    }

    public TimerObj(long timerLength) {
      init(timerLength);
    }


    private void init (long length) {
        mTimerId = (int) Utils.getTimeNow();
        mStartTime = Utils.getTimeNow();
        mTimeLeft = mOriginalLength = mSetupLength = length;
        mLabel = "";
        pcnJSON = "";
        mCeo = DBHelper.getCeoUserId();//CeoApplication.CEOLoggedIn.userId;
    }

    public long updateTimeLeft(boolean forceUpdate) {
        if (isTicking() || forceUpdate) {
            long millis = Utils.getTimeNow();
            mTimeLeft = mOriginalLength - (millis - mStartTime);
        }
        return mTimeLeft;
    }

    public boolean isTicking() {
        return mState == STATE_RUNNING || mState == STATE_TIMESUP;
    }



    public long getTimesupTime() {
        return mStartTime + mOriginalLength;
    }


    public static ArrayList<TimerObj> getTimersFromDatabase() {

        ArrayList<TimerObj> res = new ArrayList<TimerObj>();

        for (TimerObjTable row :  DBHelper.getTimers())
        {
            TimerObj t = new TimerObj(row);
            res.add(t);
        }
        Collections.sort(res, new Comparator<TimerObj>() {
            @Override
            public int compare(TimerObj timerObj1, TimerObj timerObj2) {
                return timerObj2.mTimerId - timerObj1.mTimerId;
            }
        });

        return res;
    }

    public static ArrayList<TimerObj> getTimersFromDatabase(int match) {

        ArrayList<TimerObj> res = new ArrayList<TimerObj>();

        for (TimerObjTable row :  DBHelper.getTimers())
        {
            TimerObj t = new TimerObj(row);
            if (t.mState == match) {
                res.add(t);
            }

        }
        Collections.sort(res, new Comparator<TimerObj>() {
            @Override
            public int compare(TimerObj timerObj1, TimerObj timerObj2) {
                return timerObj2.mTimerId - timerObj1.mTimerId;
            }
        });

        return res;
    }


    public static void putTimersInDB(ArrayList<TimerObj> timers) {
        if (timers.size() > 0) {
            for (int i = 0; i < timers.size(); i++) {
                TimerObj t = timers.get(i);
                timers.get(i).writeToDB();
            }
        }
    }


}
