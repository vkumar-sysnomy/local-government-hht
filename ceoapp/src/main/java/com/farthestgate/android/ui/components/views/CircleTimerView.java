package com.farthestgate.android.ui.components.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.farthestgate.android.R;
import com.farthestgate.android.ui.components.Utils;

/**
 * Class to draw a circle for timers and stopwatches.
 * These two usages require two different animation modes:
 * Timer counts down. In this mode the animation is counter-clockwise and stops at 0.
 * Stopwatch counts up. In this mode the animation is clockwise and will run until stopped.
 */
public class CircleTimerView extends View {


    private int mRedColor;
    private int mWhiteColor;
    private long mIntervalTime = 0;
    private long mIntervalStartTime = -1;
    private long mMarkerTime = -1;
    private long mCurrentIntervalTime = 0;
    private long mAccumulatedTime = 0;
    private boolean mPaused = false;
    private boolean mAnimate = false;
    private static float mStrokeSize = 4;
    private static float mDotRadius = 6;
    private static float mMarkerStrokeSize = 2;
    private final Paint mPaint = new Paint();
    private final Paint mFill = new Paint();
    private final RectF mArcRect = new RectF();
    private float mRadiusOffset;   // amount to remove from radius to account for markers on circle
    private float mScreenDensity;

    // Stopwatch mode is the default.
    private boolean mTimerMode = false;

    @SuppressWarnings("unused")
    public CircleTimerView(Context context) {
        this(context, null);
    }

    public CircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setIntervalTime(long t) {
        mIntervalTime = t;
        postInvalidate();
    }

    public void setMarkerTime(long t) {
        mMarkerTime = t;
        postInvalidate();
    }

    public void reset() {
        mIntervalStartTime = -1;
        mMarkerTime = -1;
        postInvalidate();
    }
    public void startIntervalAnimation() {
        mIntervalStartTime = Utils.getTimeNow();
        mAnimate = true;
        invalidate();
        mPaused = false;
    }
    public void stopIntervalAnimation() {
        mAnimate = false;
        mIntervalStartTime = -1;
        mAccumulatedTime = 0;
    }

    public boolean isAnimating() {
        return (mIntervalStartTime != -1);
    }

    public void pauseIntervalAnimation() {
        mAnimate = false;
        mAccumulatedTime += Utils.getTimeNow() - mIntervalStartTime;
        mPaused = true;
    }

    public void abortIntervalAnimation() {
        mAnimate = false;
    }

    public void setPassedTime(long time, boolean drawRed) {
        // The onDraw() method checks if mIntervalStartTime has been set before drawing any red.
        // Without drawRed, mIntervalStartTime should not be set here at all, and would remain at -1
        // when the state is reconfigured after exiting and re-entering the application.
        // If the timer is currently running, this drawRed will not be set, and will have no effect
        // because mIntervalStartTime will be set when the thread next runs.
        // When the timer is not running, mIntervalStartTime will not be set upon loading the state,
        // and no red will be drawn, so drawRed is used to force onDraw() to draw the red portion,
        // despite the timer not running.
        mCurrentIntervalTime = mAccumulatedTime = time;
        if (drawRed) {
            mIntervalStartTime = Utils.getTimeNow();
        }
        postInvalidate();
    }



    private void init(Context c) {

        Resources resources = c.getResources();
        mStrokeSize = resources.getDimension(R.dimen.circletimer_circle_size);
        float dotDiameter = resources.getDimension(R.dimen.circletimer_dot_size);
        mMarkerStrokeSize = resources.getDimension(R.dimen.circletimer_marker_size);
        mRadiusOffset = Utils.calculateRadiusOffset(
                mStrokeSize, dotDiameter, mMarkerStrokeSize);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mWhiteColor = resources.getColor(R.color.clock_white);
        mRedColor = resources.getColor(R.color.clock_red);
        mScreenDensity = resources.getDisplayMetrics().density;
        mFill.setAntiAlias(true);
        mFill.setStyle(Paint.Style.FILL);
        mFill.setColor(mRedColor);
        mDotRadius = dotDiameter / 2f;
    }

    public void setTimerMode(boolean mode) {
        mTimerMode = mode;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int xCenter = getWidth() / 2 + 1;
        int yCenter = getHeight() / 2;

        mPaint.setStrokeWidth(mStrokeSize);
        float radius = Math.min(xCenter, yCenter) - mRadiusOffset;

        if (mIntervalStartTime == -1) {
            // just draw a complete white circle, no red arc needed
            mPaint.setColor(mWhiteColor);
            canvas.drawCircle (xCenter, yCenter, radius, mPaint);
            if (mTimerMode) {
                drawRedDot(canvas, 0f, xCenter, yCenter, radius);
            }
        } else {
            if (mAnimate) {
                mCurrentIntervalTime = Utils.getTimeNow() - mIntervalStartTime + mAccumulatedTime;
            }
            //draw a combination of red and white arcs to create a circle
            mArcRect.top = yCenter - radius;
            mArcRect.bottom = yCenter + radius;
            mArcRect.left =  xCenter - radius;
            mArcRect.right = xCenter + radius;
            float redPercent = (float)mCurrentIntervalTime / (float)mIntervalTime;
            // prevent timer from doing more than one full circle
            redPercent = (redPercent > 1 && mTimerMode) ? 1 : redPercent;

            float whitePercent = 1 - (redPercent > 1 ? 1 : redPercent);
            // draw red arc here
            mPaint.setColor(mRedColor);
            if (mTimerMode){
                canvas.drawArc (mArcRect, 270, - redPercent * 360 , false, mPaint);
            } else {
                canvas.drawArc (mArcRect, 270, + redPercent * 360 , false, mPaint);
            }

            // draw white arc here
            mPaint.setStrokeWidth(mStrokeSize);
            mPaint.setColor(mWhiteColor);
            if (mTimerMode) {
                canvas.drawArc(mArcRect, 270, + whitePercent * 360, false, mPaint);
            } else {
                canvas.drawArc(mArcRect, 270 + (1 - whitePercent) * 360,
                        whitePercent * 360, false, mPaint);
            }

            if (mMarkerTime != -1 && radius > 0 && mIntervalTime != 0) {
                mPaint.setStrokeWidth(mMarkerStrokeSize);
                float angle = (float)(mMarkerTime % mIntervalTime) / (float)mIntervalTime * 360;
                // draw 2dips thick marker
                // the formula to draw the marker 1 unit thick is:
                // 180 / (radius * Math.PI)
                // after that we have to scale it by the screen density
                canvas.drawArc (mArcRect, 270 + angle, mScreenDensity *
                        (float) (360 / (radius * Math.PI)) , false, mPaint);
            }
            drawRedDot(canvas, redPercent, xCenter, yCenter, radius);
        }
        if (mAnimate) {
            invalidate();
        }
   }

    protected void drawRedDot(
            Canvas canvas, float degrees, int xCenter, int yCenter, float radius) {
        mPaint.setColor(mRedColor);
        float dotPercent;
        if (mTimerMode) {
            dotPercent = 270 - degrees * 360;
        } else {
            dotPercent = 270 + degrees * 360;
        }

        final double dotRadians = Math.toRadians(dotPercent);
        canvas.drawCircle(xCenter + (float) (radius * Math.cos(dotRadians)),
                yCenter + (float) (radius * Math.sin(dotRadians)), mDotRadius, mFill);
    }


}
