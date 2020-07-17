package com.farthestgate.android.ui.components.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import static com.farthestgate.android.R.color.CEO_green;

/**
 * Created by Hanson Aboagye on 09/06/2014.
 */
public class HelveticaTextView extends AppCompatTextView
{



        public HelveticaTextView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        public HelveticaTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public HelveticaTextView(Context context) {
            super(context);
            init();
        }

        private void init() {
            if (!isInEditMode()) {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-CondensedBold.otf");
                //setTextColor(CEO_green);
                setTextColor(getResources().getColor(CEO_green));
                setTypeface(tf);
            }
        }

    }
