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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.ui.components.TimerRingService;
import com.farthestgate.android.ui.components.Utils;

import java.util.ArrayList;
import java.util.Iterator;

public class TimerReceiver extends BroadcastReceiver {
    private static final String TAG = "TimerReceiver";

    // Make this a large number to avoid the alarm ID's which seem to be 1, 2, ...
    // Must also be different than StopwatchService.NOTIFICATION_ID
    private static final int IN_USE_NOTIFICATION_ID = Integer.MAX_VALUE - 2;

    ArrayList<TimerObj> mTimers;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Timers.LOGGING) {
            Log.v(TAG, "Received intent " + intent.toString());
        }
        String actionType = intent.getAction();
        // This action does not need the timers data
        if (Timers.NOTIF_IN_USE_CANCEL.equals(actionType)) {
            cancelInUseNotification(context);
            return;
        }

        // Get the updated timers data.
        if (mTimers == null) {
            mTimers = new ArrayList<TimerObj>();
        }
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferenceHelper.SHARED_PREFERENCE_FILE,Context.MODE_PRIVATE);
        mTimers = TimerObj.getTimersFromDatabase();

        // These actions do not provide a timer ID, but do use the timers data
        if (Timers.NOTIF_IN_USE_SHOW.equals(actionType)) {
        //    showInUseNotification(context);
            return;
        } else if (Timers.NOTIF_TIMES_UP_SHOW.equals(actionType)) {
            showTimesUpNotification(context);
            return;
        } else if (Timers.NOTIF_TIMES_UP_CANCEL.equals(actionType)) {
            cancelTimesUpNotification(context);
            return;
        }

        // Remaining actions provide a timer Id
        if (!intent.hasExtra(Timers.TIMER_INTENT_EXTRA)) {
            // No data to work with, do nothing
            Log.e(TAG, "got intent without Timer data");
            return;
        }

        // Get the timer out of the Intent
        int timerId = intent.getIntExtra(Timers.TIMER_INTENT_EXTRA, -1);
        if (timerId == -1) {
            Log.d(TAG, "OnReceive:intent without Timer data for " + actionType);
        }

        TimerObj t = Timers.findTimer(mTimers, timerId);

        if (Timers.TIMES_UP.equals(actionType)) {
            // Find the timer (if it doesn't exists, it was probably deleted).
            if (t == null) {
                Log.d(TAG, " timer not found in list - do nothing");
                return;
            }

            t.mState = TimerObj.STATE_TIMESUP;
            t.writeToDB();
            // Play ringtone by using TimerRingService service with a default alarm.
            Log.d(TAG, "playing ringtone");
            Intent si = new Intent();
            si.setClass(context, TimerRingService.class);
            context.startService(si);

            // Update the in-use notification
            if (getNextRunningTimer(mTimers, false, Utils.getTimeNow()) == null) {
                // Found no running timers.
                cancelInUseNotification(context);
            } else {
         //       showInUseNotification(context);
            }

            // show notification

        } else if (Timers.TIMER_RESET.equals(actionType)
                || Timers.DELETE_TIMER.equals(actionType)
                || Timers.TIMER_DONE.equals(actionType)) {
            // Stop Ringtone if all timers are not in times-up status
            stopRingtoneIfNoTimesup(context);
        } else if (Timers.NOTIF_TIMES_UP_STOP.equals(actionType)) {
            // Find the timer (if it doesn't exists, it was probably deleted).
            if (t == null) {
                Log.d(TAG, "timer to stop not found in list - do nothing");
                return;
            } else if (t.mState != TimerObj.STATE_TIMESUP) {
                Log.d(TAG, "action to stop but timer not in times-up state - do nothing");
                return;
            }

            // Update timer state
            t.mState = TimerObj.STATE_DONE;
            t.mTimeLeft = t.mOriginalLength - (Utils.getTimeNow() - t.mStartTime);
            t.writeToDB();

            // Flag to tell DeskClock to re-sync with the database
            prefs.edit().putBoolean(Timers.FROM_NOTIFICATION, true).apply();

            cancelTimesUpNotification(context, t);

            // Stop Ringtone if no timers are in times-up status
            stopRingtoneIfNoTimesup(context);
        } else if (Timers.NOTIF_TIMES_UP_PLUS_ONE.equals(actionType)) {
            // Find the timer (if it doesn't exists, it was probably deleted).
            if (t == null) {
                Log.d(TAG, "timer to +1m not found in list - do nothing");
                return;
            } else if (t.mState != TimerObj.STATE_TIMESUP) {
                Log.d(TAG, "action to +1m but timer not in times up state - do nothing");
                return;
            }

            // Restarting the timer with 1 minute left.
            t.mState = TimerObj.STATE_RUNNING;
            t.mStartTime = Utils.getTimeNow();
            t.mTimeLeft = t. mOriginalLength = TimerObj.MINUTE_IN_MILLIS;
            t.writeToDB();

            // Flag to tell DeskClock to re-sync with the database
            prefs.edit().putBoolean(Timers.FROM_NOTIFICATION, true).apply();

            cancelTimesUpNotification(context, t);

            // If the app is not open, refresh the in-use notification
            if (!prefs.getBoolean(Timers.NOTIF_APP_OPEN, false)) {
         //       showInUseNotification(context);
            }

            // Stop Ringtone if no timers are in times-up status
            stopRingtoneIfNoTimesup(context);
        } else if (Timers.TIMER_UPDATE.equals(actionType)) {
            // Refresh buzzing notification
            if (t.mState == TimerObj.STATE_TIMESUP) {
                // Must cancel the previous notification to get all updates displayed correctly
                cancelTimesUpNotification(context, t);
                showTimesUpNotification(context, t);
            }
        }
        // Update the next "Times up" alarm
        updateNextTimesup(context);
    }

    private void stopRingtoneIfNoTimesup(final Context context) {
        if (Timers.findExpiredTimer(mTimers) == null) {
            // Stop ringtone
            Log.d(TAG, "stopping ringtone");
            Intent si = new Intent();
            si.setClass(context, TimerRingService.class);
            context.stopService(si);
        }
    }

    // Scan all timers and find the one that will expire next.
    // Tell AlarmManager to send a "Time's up" message to this receiver when this timer expires.
    // If no timer exists, clear "time's up" message.
    private void updateNextTimesup(Context context) {
        TimerObj t = getNextRunningTimer(mTimers, false, Utils.getTimeNow());
        long nextTimesup = (t == null) ? -1 : t.getTimesupTime();
        int timerId = (t == null) ? -1 : t.mTimerId;

        Intent intent = new Intent();
        intent.setAction(Timers.TIMES_UP);
        intent.setClass(context, TimerReceiver.class);
        if (!mTimers.isEmpty()) {
            intent.putExtra(Timers.TIMER_INTENT_EXTRA, timerId);
        }
        AlarmManager mngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        if (t != null) {
            if (Utils.isKitKatOrLater()) {
              //  mngr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTimesup, p);
            } else {
                mngr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTimesup, p);
            }
            if (Timers.LOGGING) {
                Log.d(TAG, "Setting times up to " + nextTimesup);
            }
        } else {
            mngr.cancel(p);
            if (Timers.LOGGING) {
                Log.v(TAG, "no next times up");
            }
        }
    }

    private TimerObj getNextRunningTimer(
            ArrayList<TimerObj> timers, boolean requireNextUpdate, long now) {
        long nextTimesup = Long.MAX_VALUE;
        boolean nextTimerFound = false;
        Iterator<TimerObj> i = timers.iterator();
        TimerObj t = null;
        while(i.hasNext()) {
            TimerObj tmp = i.next();
            if (tmp.mState == TimerObj.STATE_RUNNING) {
                long timesupTime = tmp.getTimesupTime();
                long timeLeft = timesupTime - now;
                if (timesupTime < nextTimesup && (!requireNextUpdate || timeLeft > 60) ) {
                    nextTimesup = timesupTime;
                    nextTimerFound = true;
                    t = tmp;
                }
            }
        }
        if (nextTimerFound) {
            return t;
        } else {
            return null;
        }
    }

    private void cancelInUseNotification(final Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(IN_USE_NOTIFICATION_ID);
    }

    private void showTimesUpNotification(final Context context) {
        for (TimerObj timerObj : Timers.timersInTimesUp(mTimers) ) {
            showTimesUpNotification(context, timerObj);
        }
    }

    private void showTimesUpNotification(final Context context, TimerObj timerObj) {


        // Notification creation and trigger

    }

    private void cancelTimesUpNotification(final Context context) {
        for (TimerObj timerObj : Timers.timersInTimesUp(mTimers) ) {
            cancelTimesUpNotification(context, timerObj);
        }
    }

    private void cancelTimesUpNotification(final Context context, TimerObj timerObj) {

    }
}
