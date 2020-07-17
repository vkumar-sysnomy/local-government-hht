package com.farthestgate.android.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.ui.components.views.TouchImageView;

import java.io.File;
import java.util.ArrayList;


public class FullScreenImageAdapter extends PagerAdapter {

	private Activity _activity;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;
    private CameraImageHelper cameraImageHelper;
	// constructor
	public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
		this._activity = activity;
		this._imagePaths = imagePaths;
        cameraImageHelper = new CameraImageHelper();
	}

	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }
	
	@Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);
 
        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

       /* BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDensity = container.getResources().getDisplayMetrics().densityDpi;
        Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);*/
        Bitmap bitmap = cameraImageHelper.decodeFile(new File(_imagePaths.get(position)),
                CeoApplication.PHOTO_RESAMPLE_WIDTH, CeoApplication.PHOTO_RESAMPLE_HEIGHT);
        imgDisplay.setImageBitmap(bitmap);

        ((ViewPager) container).addView(viewLayout);
 
        return viewLayout;
	}
	
	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
 
    }

}
