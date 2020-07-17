package com.farthestgate.android.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import com.farthestgate.android.CeoApplication;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;


public class CameraImageHelper {
    private final String TAG = CameraImageHelper.class.getSimpleName();


    public boolean applyImageWatermark(File imageFile, String watermark) {
        try {
            WeakReference<Bitmap> src = new WeakReference<Bitmap>(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            WeakReference<Bitmap> result = createBitmapWatermark(src, watermark);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            result.get().compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            result.get().recycle();
            FileOutputStream fo = new FileOutputStream(imageFile);
            fo.write(bytes.toByteArray());
            fo.close();
            result.clear();
            src.clear();
            return true;
        } catch (Exception ex) {
            try {
                Log.d(TAG, "Failed to watermark the image");
                CeoApplication.LogError(ex);
            }catch (Exception e){
                Log.d(TAG, "Failed to log the error message while watermarking the image");
            }
            return false;
        }
    }



    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth)
        {
            if(width > height)
            {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            }
            else
            {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public Bitmap decodeFile(File file, int newWidth, int newHeight)
    {// target size
        try
        {
            Context ctx = CeoApplication.getContext();
            Resources res = ctx.getResources();

            Bitmap bmp = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), Uri.fromFile(file));
            if(bmp == null)
            {
                // avoid concurrence
                // Decode image size
                BitmapFactory.Options option = new BitmapFactory.Options();

                // option = getBitmapOutput(file);

                option.inDensity = ctx.getResources().getDisplayMetrics().densityDpi < DisplayMetrics.DENSITY_HIGH ? 120 : 240;
                option.inTargetDensity = res.getDisplayMetrics().densityDpi;

                if(newHeight > 0 && newWidth > 0)
                    option.inSampleSize = calculateInSampleSize(option, newWidth, newWidth);

                option.inJustDecodeBounds = false;
                byte[] decodeBuffer = new byte[20 * 1024];
                option.inTempStorage = decodeBuffer;
                option.inPurgeable = true;
                option.inInputShareable = true;
                option.inScaled = true;

                bmp = BitmapFactory.decodeStream(new FileInputStream(file), null, option);
                if(bmp == null)
                {
                    return null;
                }
            }
            else
            {
                /*int inDensity = res.getDisplayMetrics().densityDpi < DisplayMetrics.DENSITY_HIGH ? 120 : 240;
                int inTargetDensity = res.getDisplayMetrics().densityDpi;
                if(inDensity != inTargetDensity)
                {
                    int newBmpWidth = (bmp.getWidth() * inTargetDensity) / inDensity;
                    int newBmpHeight = (bmp.getHeight() * inTargetDensity) / inDensity;*/
                    bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
                //}
            }

            return bmp;
        }
        catch(Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
        return null;
    }

    public File getExtraFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.EXTRAS_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("CAM", "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }



    public File getBackupFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.BACKUP_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("CAM", "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }


    public File CreateGetBackupFolder(String dateStamp) {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.BACKUP_FOLDER + dateStamp);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("CAM", "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }


    public File getPCNPhotoFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.PHOTO_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }


    public File getSingleLookUpsFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.APP_SINGLEVIEW_LOOKUP_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    public File getPCNDataFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.PCN_DATA_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    public File getPCNNoteFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.NOTES_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }
    /*
    * Report folder path
    */
    public File getPCNReportFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.TOUR_REPORT_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    public File getRemovalPhotoFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.REMOVALPHOTO_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    public File getLocalDBFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.LOCAL_DB_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    public File getPCNErrorFolder() {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + AppConstant.APP_ERROR_FOLDER);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                    return null;
                }
            }
        }
        return mediaStorageDir;
    }

    private WeakReference<Bitmap> createBitmapWatermark(WeakReference<Bitmap> src, String watermark) {
            int w = src.get().getWidth();
            int h = src.get().getHeight();
            WeakReference<Bitmap> result = new WeakReference<Bitmap>(Bitmap.createBitmap(w, h, src.get().getConfig()));
            Canvas canvas = new Canvas(result.get());
            canvas.drawBitmap(src.get(), 0, 0, null);
            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            canvas.drawText(watermark, 50f, 110f, paint);
            src.get().recycle();
            src.clear();
            return result;
    }


    public File getFile(String path, String fileName) {
        File mediaStorageDir = getDirectory(path);
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private File getDirectory(String path) {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + path);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                }
            }
        }
        return mediaStorageDir;
    }

    public String readFile(String path, String fileName) {
        BufferedReader br = null;
        String response = null;
        try {
            StringBuffer output = new StringBuffer();
            File file = getFile(path, fileName);
            if(file.exists()) {
//                String fpath = file.getAbsolutePath();

//                br = new BufferedReader(new FileReader(fpath), );
                br  = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file),"ISO-8859-1"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    output.append(line);
                }
                response = output.toString();
            }

        } catch (IOException e) {
            try {
                CeoApplication.LogError(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }
        return response;
    }

}
