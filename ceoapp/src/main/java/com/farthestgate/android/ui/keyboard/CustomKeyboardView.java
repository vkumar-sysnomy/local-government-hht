/*
 * Copyright (C) 2008-2009 The Android Open Source Project
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

package com.farthestgate.android.ui.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import com.farthestgate.android.R;

import java.util.HashMap;
import java.util.Map;

public class CustomKeyboardView extends KeyboardView 
{
	

    @Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void setPopupOffset(int x, int y)
	{
		super.setPopupOffset(x, y);
	}

	@Override
	public void setProximityCorrectionEnabled(boolean enabled)
	{
		super.setProximityCorrectionEnabled(enabled);
	}

	static final int KEYCODE_OPTIONS = -100;

    public CustomKeyboardView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);

        mPopupKeyboard = new PopupWindow(context);
        mPopupKeyboard.setBackgroundDrawable(null);
        
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyle) 
    {
    	
        super(context, attrs, defStyle);
        
    }

   	@Override
	public boolean onTouchEvent(MotionEvent me)
	{
		return super.onTouchEvent(me);
	}

	private PopupWindow mPopupKeyboard;
    private View mMiniKeyboardContainer;
    private KeyboardView mMiniKeyboard;
    private Map<Key,View> mMiniKeyboardCache = new HashMap<Key,View>();
    
    private int mPopupLayout;
	private Boolean mMiniKeyboardOnScreen = false;
	private OnKeyboardActionListener mKeyboardActionListener;
	private int mPopupX;
	private int mPopupY;
	private final int[] mCoordinates = new int[2];

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);



        return false;
    }

    @Override
    protected boolean onLongPress(Key popupKey) 
    {
		int popupKeyboardId = popupKey.popupResId;

        if (popupKeyboardId != 0) 
        {
        	
        	mKeyboardActionListener = super.getOnKeyboardActionListener();
        	
            mMiniKeyboardContainer = mMiniKeyboardCache.get(popupKey);
            if (mMiniKeyboardContainer == null) 
            {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                mMiniKeyboardContainer = inflater.inflate(R.layout.keyboard_popup_keyboard, null);
                mMiniKeyboard = (CustomKeyboardView) mMiniKeyboardContainer.findViewById(R.id.keyboardView);
                
                View closeButton = mMiniKeyboardContainer.findViewById(R.id.closeButton);
                if (closeButton != null) 
                	closeButton.setOnClickListener(this);
                
                mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() 
                {
                    public void onKey(int primaryCode, int[] keyCodes) 
                    {
                        mKeyboardActionListener.onKey(primaryCode, keyCodes);
                        dismissPopupKeyboard();
                    }
                    
                    public void onText(CharSequence text) {
                        mKeyboardActionListener.onText(text);
                        dismissPopupKeyboard();
                    }
                    
                    public void swipeLeft() { }
                    public void swipeRight() { }
                    public void swipeUp() { }
                    public void swipeDown() { }
                    public void onPress(int primaryCode) 
                    {
                        mKeyboardActionListener.onPress(primaryCode);
                    }
                    public void onRelease(int primaryCode) 
                    {
                        mKeyboardActionListener.onRelease(primaryCode);
                    }
                });
             
                Keyboard keyboard;
                if (popupKey.popupCharacters != null) 
                {
                    keyboard = new Keyboard(getContext(), popupKeyboardId, 
                            popupKey.popupCharacters, -1, getPaddingLeft() + getPaddingRight());
                }                 
                else 
                {
                    keyboard = new Keyboard(getContext(), popupKeyboardId);
                }
                mMiniKeyboard.setKeyboard(keyboard);
                mMiniKeyboard.setPopupParent(this);
                mMiniKeyboardContainer.measure(
                        MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST), 
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
                
                mMiniKeyboardCache.put(popupKey, mMiniKeyboardContainer);
            } 
            else 
            {
                mMiniKeyboard = (CustomKeyboardView) mMiniKeyboardContainer.findViewById(R.id.keyboardView);
            }
            getLocationInWindow(mCoordinates);
            mPopupX = popupKey.x + 200;
            mPopupY = popupKey.y + 30;
            mPopupX = mPopupX + popupKey.width - mMiniKeyboardContainer.getMeasuredWidth();
            mPopupY = mPopupY - mMiniKeyboardContainer.getMeasuredHeight();
            final int x = mPopupX + mMiniKeyboardContainer.getPaddingRight() + mCoordinates[0];
            final int y = mPopupY + mMiniKeyboardContainer.getPaddingBottom() + mCoordinates[1];
            mMiniKeyboard.setPopupOffset(x < 0 ? 0 : x, y);
            mMiniKeyboard.setShifted(isShifted());
            mPopupKeyboard.setContentView(mMiniKeyboardContainer);
            mPopupKeyboard.setWidth(mMiniKeyboardContainer.getMeasuredWidth());
            mPopupKeyboard.setHeight(mMiniKeyboardContainer.getMeasuredHeight());
            mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
            mMiniKeyboardOnScreen = true;
         
            invalidateAllKeys();
            return true;
        }
        return false;
		
		
//        if (popupKey.codes[0] == Keyboard.KEYCODE_CANCEL) 
//        {
//            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
//            return true;
//        } 
//        else 
//        {
//            return super.onLongPress(popupKey);
//        }
    }

	@Override
	public void setOnKeyboardActionListener(OnKeyboardActionListener listener)
	{
		super.setOnKeyboardActionListener(listener);
	}
	
	@Override
	public void onClick(View v) 
 	{
        dismissPopupKeyboard();
  	}
	  
	private void dismissPopupKeyboard() 
	{
        if (mPopupKeyboard.isShowing()) 
        {
            mPopupKeyboard.dismiss();
            mMiniKeyboardOnScreen = false;
            invalidateAllKeys();
        }
    }

}
