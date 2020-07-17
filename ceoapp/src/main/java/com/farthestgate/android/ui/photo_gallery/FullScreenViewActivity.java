package com.farthestgate.android.ui.photo_gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import androidx.viewpager.widget.ViewPager;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.FullScreenImageAdapter;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.ImageFileUtils;
import com.farthestgate.android.utils.NfcForegroundUtil;


public class FullScreenViewActivity extends Activity{

	private ImageFileUtils utils;
	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
    private NfcForegroundUtil nfcForegroundUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_view);
        Button btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
		viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(1);
		utils = new ImageFileUtils(getApplicationContext());

		Intent i = getIntent();
		int position = i.getIntExtra("position", 0);

		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
				utils.getPhotoPaths());

		viewPager.setAdapter(adapter);

		// displaying selected image first
		viewPager.setCurrentItem(position);
        nfcForegroundUtil = new NfcForegroundUtil(this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        nfcForegroundUtil.enableForeground();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        //super.onNewIntent(intent);
        CroutonUtils.error(this, "Please log in before pairing a printer");
    }
}
