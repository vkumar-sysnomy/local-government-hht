package com.farthestgate.android.ui.photo_gallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.ImageFileUtils;
import com.farthestgate.android.utils.NfcForegroundUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class GalleryActivity extends FragmentActivity implements AdapterView.OnItemClickListener,
        StickyGridHeadersGridView.OnHeaderClickListener, StickyGridHeadersGridView.OnHeaderLongClickListener {


    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String KEY_LIST_POSITION = "key_list_position";

    private ImageFileUtils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridView gridView;
    private int columnWidth;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int mFirstVisible;
    private NfcForegroundUtil nfcForegroundUtil;
    private Integer currentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_grid_view);

        VisualPCNListActivity.currentActivity = 1;
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
        currentSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;

        gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(this);

        utils = new ImageFileUtils(this);

        // Initilizing Grid View
        InitilizeGridLayout();

        // loading all image paths from SD card
        imagePaths = utils.getPhotoPaths();

        // Gridview adapter
        StickyGridHeadersSimpleArrayAdapter<String> adapter = new StickyGridHeadersSimpleArrayAdapter<String>(this, R.layout.header, imagePaths,
                columnWidth, BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo));

        // setting grid view adapter
        gridView.setAdapter(adapter);
        if (savedInstanceState != null) {
            mFirstVisible = savedInstanceState.getInt(KEY_LIST_POSITION);
        }

        gridView.setSelection(mFirstVisible);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        ((StickyGridHeadersGridView) gridView).setOnHeaderClickListener(this);
        ((StickyGridHeadersGridView) gridView).setOnHeaderLongClickListener(this);

        nfcForegroundUtil = new NfcForegroundUtil(this);

    }

    private void InitilizeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstant.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((utils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS);

        gridView.setNumColumns(AppConstant.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    @Override
    public void onHeaderClick(AdapterView<?> parent, View view, long id) {

    }

    @Override
    public boolean onHeaderLongClick(AdapterView<?> parent, View view, long id) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(this, FullScreenViewActivity.class);
        i.putExtra("position", position);
        startActivity(i);

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcForegroundUtil.enableForeground();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        CroutonUtils.error(this, "Please log in before pairing a printer");
    }

    @SuppressLint("NewApi")
    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            gridView.setItemChecked(mActivatedPosition, false);
        } else {
            gridView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.camera_menu, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_camera:
                 takePhoto();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }

      private void takePhoto() {

         startActivityForResult(new Intent(GalleryActivity.this,CameraActivity.class),CeoApplication.RESULT_CODE_NEWIMAGE);
     }
 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CeoApplication.RESULT_CODE_NEWIMAGE:
                if (resultCode == RESULT_OK) {
                    Runtime.getRuntime().gc();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    //    String watermark = timeStamp;
                    File currentImageFile = new File(data.getStringExtra("path"));
                    //  new CameraImageHelper().applyImageWatermark(currentImageFile, watermark);
                    PCNPhotoTable pcnPhoto = new PCNPhotoTable();
                    pcnPhoto.setCEO_Number(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
                    pcnPhoto.setFileName(currentImageFile.getName());
                    pcnPhoto.setObservation(0);
                    pcnPhoto.setTimestamp(timeStamp);
                    pcnPhoto.setPcnSession(currentSession);
                    pcnPhoto.save();
                }
                break;
        }
    }

}
