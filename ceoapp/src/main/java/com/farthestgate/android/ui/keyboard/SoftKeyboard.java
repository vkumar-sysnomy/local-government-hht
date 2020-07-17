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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;


/**
 *  Extended by Hanson Aboagye 02/2013
 */

public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener 
{	
    static final boolean DEBUG = false;
    
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private static final int SHORT_CODE = 1000;

    private static Boolean codesVisible = false;
    private InputMethodManager mInputMethodManager;

    private CustomKeyboardView mInputView;
    
    private StringBuilder mComposing = new StringBuilder();
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;
    private EditorInfo editorInfo;

    private String[] shortCodes;
    
    private CustomKeyboard mQwertyKeyboard;
    
    private CustomKeyboard mCurKeyboard;
    
    private String mWordSeparators;
    


    @Override public void onCreate() 
    {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);

        shortCodes  = CeoApplication.getContext().getResources().getStringArray(R.array.short_codes);
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() 
    {
        if (mQwertyKeyboard != null) 
        {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
    
        	int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new CustomKeyboard(this, R.xml.qwerty);
        
    
    }
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() 
    {
        mInputView = (CustomKeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mQwertyKeyboard);
        mInputView.setShifted(true);
        
        return mInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() 
    {
        return null;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) 
    {
        super.onStartInput(attribute, restarting);
        
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        editorInfo = attribute;
        if (!restarting) 
        {
            // Clear shift states.
            mMetaState = 0;
        }
        
    
        mCurKeyboard = mQwertyKeyboard;
        
       
    }



    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() 
    {
      
        
        // Clear current composing text and candidates.
        mComposing.setLength(0);

        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null)
        {
        	mInputView.setY(0f);
        	mInputView.animate().y(350F)
            .setDuration(80)
           .setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                	mInputView.setVisibility(View.GONE);

                }
            });
           mInputView.closing();
        	mInputView.animate().start();
        }
        super.onFinishInput();
    }
    
    @Override 
    public void onStartInputView(EditorInfo attribute, boolean restarting) 
    {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        mInputView.setY(350f);
        mInputView.setAlpha(0f);
    	mInputView.setKeyboard(mCurKeyboard);
    	
    	if (mInputView.getVisibility() == View.GONE)
    		mInputView.setVisibility(View.VISIBLE); 

        mInputView.animate().y(0f).alpha(255f)
        .setDuration(80)
        .setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation) 
            {
              mInputView.setVisibility(View.VISIBLE);
              mInputView.closing();
            
            }
        });

        mInputView.animate().start();

    }

    
    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
    
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

 
    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) 
    {
    	
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }
        
        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) 
        {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        
        if (mComposing.length() > 0) 
        {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }
        
        onKey(c, null);
        
        return true;
    }


    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) 
                {
                	if (mInputView.handleBack()) 
                    {
                        return true;
                    }
                }
                break;
                
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) 
                {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;
                
            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;
                
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS)
                {
                      return true;
                   
                }
        }
        
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) 
    {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) 
        {
            
        }
        
        return super.onKeyUp(keyCode, event);
    }

    
    /**
     * We want shift on all the time 
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) 
    {
        if (attr != null 
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) 
        {
            
        	
            
            mInputView.setShifted(true);
        }
    }
    
    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) 
    {
        if (isWordSeparator(primaryCode)) 
        {
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } 
        else
        {
            switch (primaryCode) {
                case SHORT_CODE:
                {
                    if (!codesVisible) {
                        mQwertyKeyboard = new CustomKeyboard(this, R.xml.codes);
                        codesVisible = true;
                    }
                    else
                    {
                        mQwertyKeyboard = new CustomKeyboard(this, R.xml.qwerty);
                        codesVisible = false;
                    }
                    onStartInputView(editorInfo,true);
                    mInputView.setKeyboard(mQwertyKeyboard);
                    mCurKeyboard = mQwertyKeyboard;
                    mInputView.setShifted(true);
                    mInputView.invalidateAllKeys();
                    mInputView.invalidate();

                    break;
                }
                case Keyboard.KEYCODE_DELETE: {
                    handleBackspace();
                    break;
                }
                case Keyboard.KEYCODE_MODE_CHANGE: {

                    if (mInputView != null) {

                    } else {
                        handleCharacter(primaryCode, keyCodes);
                    }
                    break;
                }
                case Keyboard.KEYCODE_CANCEL: {
                    handleClose();
                    break;
                }
                case Keyboard.KEYCODE_SHIFT: {
                    handleShift();
                    break;
                }
                default:
                {
                    handleCharacter(primaryCode, keyCodes);
                    break;
                }
            }
        }
    }
    
    private void handleBackspace() 
    {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
           
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
           
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
        
        mInputView.setShifted(true);
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) 
        {
            if (mInputView.isShifted()) 
            {
                if (!codesVisible)
                    primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        String letterCode = null;
        if (primaryCode < 900 )
        {
            if (isAlphabet(primaryCode)) {// this is just a technique which can be used to create autotext and auto checking
                mComposing.append((char) primaryCode);
                getCurrentInputConnection().finishComposingText();
                getCurrentInputConnection().commitText(mComposing, 1);
                updateShiftKeyState(getCurrentInputEditorInfo());
            }
            else
            {
                letterCode = String.valueOf((char) primaryCode);
                getCurrentInputConnection().commitText(letterCode  , 1);
            }
        } else {
            letterCode = getCode(primaryCode);
            if (letterCode == null)
                letterCode = String.valueOf((char) primaryCode);

            getCurrentInputConnection().commitText(letterCode  , 1);
        }
    }

    private String getCode(int code)
    {
        for (String st:shortCodes)
        {
            if (st.startsWith(String.valueOf(code)))
                return st.split("-")[1];
        }
        return null;
    }

    private void handleClose() {

        requestHideSelf(0);
        mInputView.closing();
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }
    
    private String getWordSeparators() {
        return mWordSeparators;
    }
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    public void swipeRight() 
    {
       // nothing
    }
    
    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) 
    {
    	
    }
    
    public void onRelease(int primaryCode) {
    }

	@Override
	public void onText(CharSequence arg0)
	{
		// TODO Auto-generated method stub

//	    public void onText(CharSequence text) {
//	        InputConnection ic = getCurrentInputConnection();
//	        if (ic == null) return;
//	        ic.beginBatchEdit();
//	        if (mComposing.length() > 0) {
//	            commitTyped(ic);
//	        }
//	        
//	        ic.commitText(text, 0);
//	        ic.endBatchEdit();
//	        updateShiftKeyState(getCurrentInputEditorInfo());
//	    }
//
		
	}
}
