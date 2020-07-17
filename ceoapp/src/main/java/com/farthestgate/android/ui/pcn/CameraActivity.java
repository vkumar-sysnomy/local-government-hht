package com.farthestgate.android.ui.pcn;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.OrientationEventListener;

import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.database.ErrorTable;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.utils.DateUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Takes a photo saves it to the SD card and returns the path of this photo to the calling Activity
 * @author paul.blundell
 *
 * Modified by Hanson Aboagye 05/2014
 */
public class CameraActivity extends FragmentActivity implements CameraFragment.OnCammeraAction
{


    Integer observationNumber;
    String pcnNumber;
    CameraFragment cameraFragment;
    private Integer                currentSession;
    public PackageInfo packageInfo;

    private OrientationListener orientationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        orientationListener = new OrientationListener(this);
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
        currentSession = sharedPreferenceHelper.getValue(PCNTable.COL_PCN_SESSION, Integer.class, 0) + 1;

        cameraFragment = new CameraFragment();

        observationNumber = getIntent().getIntExtra("obs",0);
        //pcn disorder problem
        if(getIntent().getStringExtra("pcn")!=null && getIntent().getStringExtra("pcn").length()>0){
            pcnNumber = getIntent().getStringExtra("pcn");
        }

        try {
            packageInfo     = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        getFragmentManager().beginTransaction().replace(R.id.camera_fragment,
                cameraFragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void OnPictureTaken(String filePath) {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().freeMemory();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String watermark = timeStamp;
        //pcn disorder problem
        File oldfile, newfile;
        oldfile = new File(filePath);
        if (oldfile.exists()) {
            Integer n = DBHelper.PhotosForPCN(observationNumber).size();
            if (pcnNumber != null && pcnNumber.length() > 0) {
                filePath = Environment.getExternalStorageDirectory() + "/" +
                        AppConstant.PHOTO_FOLDER +
                        pcnNumber + "-photo-" + n + ".jpg";
            }else{
                filePath = Environment.getExternalStorageDirectory() + "/" +
                        AppConstant.PHOTO_FOLDER +
                        observationNumber + "-photo-" + n + ".jpg";
            }
            newfile = new File(filePath);
            if (!oldfile.renameTo(newfile))
                Log.e("CAMERA ACTIVITY", "Unable to rename " + oldfile.getAbsolutePath());
        }
        File currentImageFile = new File(filePath);
        // apply water mark
        new CameraImageHelper().applyImageWatermark(currentImageFile, timeStamp);
        saveExif(currentImageFile.getAbsolutePath(), currentImageFile.getName());
        // no use doing anything here - can't recover from this
        PCNPhotoTable pcnPhoto = new PCNPhotoTable();
        pcnPhoto.setCEO_Number(DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
        pcnPhoto.setFileName(currentImageFile.getAbsolutePath());
        pcnPhoto.setObservation(observationNumber);
        pcnPhoto.setTimestamp(timeStamp);
        pcnPhoto.setPcnSession(currentSession);
        pcnPhoto.save();

    }

    public void OnException(Context context, Throwable exc, int location)
    {
        final String SINGLE_LINE_SEP = "\n";
        final String DOUBLE_LINE_SEP = "\n\n";


        StackTraceElement[] arr = exc.getStackTrace();
        final StringBuffer report = new StringBuffer(exc.toString());
        final String lineSeperator = "-------------------------------\n\n";
        report.append(lineSeperator);
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append(SINGLE_LINE_SEP);
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n\n");
        Throwable cause = exc.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        // Getting the Device brand,model and sdk verion details.
        report.append(lineSeperator);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(SINGLE_LINE_SEP);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(SINGLE_LINE_SEP);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(SINGLE_LINE_SEP);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(SINGLE_LINE_SEP);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append(SINGLE_LINE_SEP);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);

        String version = "";
        String errorText = report.toString();
        if (packageInfo != null)
            version = "Version :" + packageInfo.versionName + "." + packageInfo.versionCode;

        ErrorTable errorRecord = new ErrorTable();
        errorRecord.setErrorLoc(location);
        errorRecord.setErrorText(errorText);
        errorRecord.save();

        new PubNubModule(context).publishError(errorText,location,version);

    }

    private int rotation = 0;
    private class OrientationListener extends OrientationEventListener{
        final int ROTATION_O    = 6;
        final int ROTATION_90   = 1;
        final int ROTATION_180  = 8;
        final int ROTATION_270  = 3;

        //        private int rotation = 0;
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {
            if( (orientation < 35 || orientation > 325) && rotation!= ROTATION_O){ // PORTRAIT
                rotation = ROTATION_O;
            }
            else if( orientation > 145 && orientation < 215 && rotation!=ROTATION_180){ // REVERSE PORTRAIT
                rotation = ROTATION_180;
            }
            else if(orientation > 55 && orientation < 125 && rotation!=ROTATION_270){ // REVERSE LANDSCAPE
                rotation = ROTATION_270;
            }
            else if(orientation > 235 && orientation < 305 && rotation!=ROTATION_90){ //LANDSCAPE
                rotation = ROTATION_90;
            }
        }
    }

    @Override protected void onStart() {
        orientationListener.enable();
        super.onStart();
    }

    @Override protected void onStop() {
        orientationListener.disable();
        super.onStop();
    }

    private void saveExif(String filePath, String fileName){
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            exifInterface.setAttribute("Make",
                    Build.BRAND);
            exifInterface.setAttribute("Model",
                    Build.MODEL);
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH,
                    exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH,
                    exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME,
                    DateUtils.getISO8601DateTime());
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    String.valueOf(rotation));

            //GPS latitude and longitude
           /*exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                    String.valueOf(VisualPCNListActivity.latitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                    String.valueOf(VisualPCNListActivity.longitude));*/

            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToDegreeMinuteSeconds(VisualPCNListActivity.latitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, getLatitudeRef(VisualPCNListActivity.latitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToDegreeMinuteSeconds(VisualPCNListActivity.longitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, getLongitudeRef(VisualPCNListActivity.longitude));

            exifInterface.setAttribute("UserComment", "Filename:" + fileName + ",Rotation:" + rotation);

            exifInterface.saveAttributes();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * returns ref for latitude which is S or N.
     *
     * @param latitude
     * @return S or N
     */
    private static String getLatitudeRef(double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    /**
     * returns ref for latitude which is S or N.
     *
     * @param longitude
     * @return W or E
     */
    private static String getLongitudeRef(double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     * 79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     *
     * @param latitude could be longitude.
     * @return
     */
    private static String convertToDegreeMinuteSeconds(double latitude) {
        latitude = Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude * 1000.0d);

        StringBuilder sb = new StringBuilder();
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}