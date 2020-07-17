package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by Hanson on 24/10/2014.
 */
public class TimerObjTable extends Model {

    public static final String COL_TIMER_ID         = "timer_id";
    public static final String COL_START_TIME       = "timer_start_time";
    public static final String COL_TIME_LEFT        = "timer_time_left";
    public static final String COL_ORIGINAL_TIME    = "timer_original_timer";
    public static final String COL_SETUP_TIME       = "timer_setup_timer";
    public static final String COL_STATE            = "timer_state";
    public static final String COL_LABEL            = "timer_label";
    public static final String COL_JSON             = "pcn_json";
    public static final String COL_CEO              = "timer_CEO";
    public static final String COL_START_TIME_MILLIS = "time_start_time_millis";

    @Column(name = COL_TIMER_ID)
    private int timerId;

    @Column(name = COL_START_TIME)
    private long timerStartTime;

    @Column(name = COL_TIME_LEFT)
    private long timeLeft;

    @Column(name = COL_ORIGINAL_TIME)
    private long originalTime;

    @Column(name = COL_SETUP_TIME)
    private long setupTime;

    @Column(name = COL_STATE)
    private int timerState;

    @Column(name = COL_LABEL)
    private String timerLabel;

    @Column(name = COL_JSON)
    private String timerJSON;

    @Column(name = COL_CEO)
    private String timerCEO;

    @Column(name = COL_START_TIME_MILLIS)
    private long timerStartTimeMillis;

    public String getTimerJSON() {
        return timerJSON;
    }

    public void setTimerJSON(String timerJSON) {
        this.timerJSON = timerJSON;
    }

    public String getTimerLabel() {
        return timerLabel;
    }

    public void setTimerLabel(String timerLabel) {
        this.timerLabel = timerLabel;
    }

    public int getTimerId() {
        return timerId;
    }

    public void setTimerId(int timerId) {
        this.timerId = timerId;
    }

    public long getTimerStartTime() {
        return timerStartTime;
    }

    public void setTimerStartTime(long timerStartTime) {
        this.timerStartTime = timerStartTime;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public long getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(long originalTime) {
        this.originalTime = originalTime;
    }

    public long getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(long setupTime) {
        this.setupTime = setupTime;
    }

    public int getTimerState() {
        return timerState;
    }

    public void setTimerState(int timerState) {
        this.timerState = timerState;
    }

    public String getTimerCEO() {
        return timerCEO;
    }

    public void setTimerCEO(String timerCEO) {
        this.timerCEO = timerCEO;
    }
    public long getTimerStartTimeMillis() {
        return timerStartTimeMillis;
    }
    public void setTimerStartTimeMillis(long timerStartTimeMillis) {
        this.timerStartTimeMillis = timerStartTimeMillis;
    }
}
