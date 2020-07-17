package com.farthestgate.android.ui.components.views;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 *
 * Extending work by
 *
 */
public class FontFitTextView extends AppCompatTextView {

    public FontFitTextView(Context context) {
        super(context);
        initialise();
    }

    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    private void initialise() {
        testPaint = new Paint();
        testPaint.set(this.getPaint());
        //max size defaults to the intially specified text size unless it is too small
        maxTextSize = this.getTextSize();

        // rough estimation
        minTextSize = maxTextSize/getMaxLines();
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     *
     * This also assumes that you have setup your layout to keep the TextView a set width and height
     */
    private void refitText(String text, int textBoxWidth, int textBoxHeight) {
        if (textBoxWidth > 0) {

            //check if more height is needed since the text will wrap anyway

            testPaint.set(this.getPaint());
            float trySize = maxTextSize;
            int availableWidth = textBoxWidth - this.getPaddingLeft() - this.getPaddingRight();
            int padding = this.getPaddingLeft() + this.getPaddingRight();
            Float factor =  testPaint.measureText(text)/(availableWidth + padding);

            while ((trySize > minTextSize) && ( factor > getMaxLines() - 0.5 )) {
                trySize -= 0.5;
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }
                testPaint.setTextSize(trySize);
                factor =  testPaint.measureText(text)/(availableWidth +  padding);
            }

            this.setTextSize(trySize);
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w, h);
        }
    }

    //Getters and Setters
    public float getMinTextSize() {
        return minTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        this.minTextSize = minTextSize;
    }

    public float getMaxTextSize() {
        return maxTextSize;
    }

    public void setMaxTextSize(int minTextSize) {
        this.maxTextSize = minTextSize;
    }

    //Attributes
    private Paint testPaint;
    private float minTextSize;
    private float maxTextSize;

}