package com.farthestgate.android.ui.components.buttons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

import com.farthestgate.android.R;


public class TwoToneIconButton extends AppCompatButton
{
	private double iconBackgroundWidth;
	private static int PRESS_AMOUNT = 2;
	private int background;
	private int iconBackground;
	private int icon;
	private float cornerRadius;

	public TwoToneIconButton(Context context) {
		super(context);
	}

	// bug - button changes to default radius randomly
	public TwoToneIconButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageButton);
		setupAttributes(attributes);
	}

	public TwoToneIconButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// load the styled attributes and set their properties
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageButton, defStyle, 0);
		setupAttributes(attributes);
	}

	@SuppressWarnings("deprecation")
	private void setupAttributes(TypedArray attributes) {
		background = attributes.getColor(R.styleable.RoundedImageButton_button_background, -1);
		iconBackground = attributes.getColor(R.styleable.RoundedImageButton_iconBackground, -1);
		icon = attributes.getResourceId(R.styleable.RoundedImageButton_button_icon, -1);
		cornerRadius = attributes.getInt(R.styleable.RoundedImageButton_cornerRadiusInDip, -1);
		iconBackgroundWidth = (100 - attributes.getInt(R.styleable.RoundedImageButton_iconBackgroundPercent, 15));
		iconBackgroundWidth = iconBackgroundWidth / 100;
		attributes.recycle();
		applyAttributes();
		this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				int width = (int) (getWidth() * iconBackgroundWidth);
				((LayerDrawable) getBackground()).setLayerInset(1, 0, 0, width, 0);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					TwoToneIconButton.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					TwoToneIconButton.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	private void applyAttributes() {
		LayerDrawable d = (LayerDrawable) getResources().getDrawable(R.drawable.two_tone_icon_btn);
		if (background != -1)
			((GradientDrawable) d.getDrawable(0)).setColor(background);
		if (iconBackground != -1)
			((GradientDrawable) d.getDrawable(1)).setColor(iconBackground);
		if (cornerRadius != -1) {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, metrics);
			((GradientDrawable) d.getDrawable(0)).setCornerRadius(cornerRadius);
			((GradientDrawable) d.getDrawable(1))
			.setCornerRadii(new float[] { cornerRadius, cornerRadius, 0, 0, 0, 0, cornerRadius, cornerRadius });
		}
		int pL = getPaddingLeft();
		int pT = getPaddingTop();
		int pR = getPaddingRight();
		int pB = getPaddingBottom();
		int dP = getCompoundDrawablePadding();
		setBackgroundDrawable(d);
		if (icon != -1)
			setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
		setPadding(pL, pT, pR, pB);
		setCompoundDrawablePadding(dP);
	}

	@Override
	public void setBackgroundColor(int color) {
		((GradientDrawable) ((LayerDrawable) getBackground()).getDrawable(0)).setColor(color);
	}

	public void setIconBackgroundColor(int color) {
		((GradientDrawable) ((LayerDrawable) getBackground()).getDrawable(1)).setColor(color);
	}

	public void setIconResource(int resource) {
		setCompoundDrawablesWithIntrinsicBounds(resource, 0, 0, 0);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			getBackground().setColorFilter(Color.argb(50, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
			this.setPadding(getPaddingLeft(), getPaddingTop() + PRESS_AMOUNT, getPaddingRight(), getPaddingBottom() - PRESS_AMOUNT);
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			getBackground().clearColorFilter();
			this.setPadding(getPaddingLeft(), getPaddingTop() - PRESS_AMOUNT, getPaddingRight(), getPaddingBottom() + PRESS_AMOUNT);
		}
		return super.onTouchEvent(event);
	}

//	@Override
//	public Parcelable onSaveInstanceState() {
//		Bundle bundle = new Bundle();
//		bundle.putParcelable("instanceState", super.onSaveInstanceState());
//		bundle.putDouble("iconBackgroundWidth", iconBackgroundWidth);
//		bundle.putInt("background", background);
//		bundle.putInt("iconBackground", iconBackground);
//		bundle.putInt("icon", icon);
//		bundle.putFloat("cornerRadius", cornerRadius);
//		return bundle;
//	}
//
//	@Override
//	public void onRestoreInstanceState(Parcelable state) {
//		if (state instanceof Bundle) {
//			Bundle bundle = (Bundle) state;
//			iconBackgroundWidth = bundle.getDouble("iconBackgroundWidth", iconBackgroundWidth);
//			background = bundle.getInt("background", background);
//			background = bundle.getInt("iconBackground", iconBackground);
//			icon = bundle.getInt("icon", icon);
//			cornerRadius = bundle.getFloat("cornerRadius", cornerRadius);
//			state = bundle.getParcelable("instanceState");
//			applyAttributes();
//		}
//		super.onRestoreInstanceState(state);
//	}
}
