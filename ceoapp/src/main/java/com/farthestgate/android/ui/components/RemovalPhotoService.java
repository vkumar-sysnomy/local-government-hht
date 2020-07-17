package com.farthestgate.android.ui.components;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.GeneralResponse;
import com.farthestgate.android.model.database.ImageTable;
import com.farthestgate.android.pubnub.PubNubModule;
import com.farthestgate.android.retrofit.Retrofit2_Util;
import com.farthestgate.android.retrofit.RetrofitCallback;
import com.farthestgate.android.retrofit.RetrofitRequest;
import com.farthestgate.android.utils.CommonUtils;

import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Administrator on 5/10/2016.
 */
public class RemovalPhotoService extends IntentService{

    private static final String TAG = RemovalPhotoService.class.getSimpleName();
    private static final int IMG_WIDTH = 640;
    private static final int IMG_HEIGHT = 480;
    private String removal;

    public RemovalPhotoService() {
        super(RemovalPhotoService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        android.util.Log.e("RemovalPhotoService", "AlarmManager");

        removal = intent.getStringExtra("removal");
        if(CeoApplication.multipartUpload())
        {
           uploadFilesOnServer();
        }else {
           decodeBitmapFromFile();
        }
    }

    private void uploadFilesOnServer(){
        try {
            CameraImageHelper cameraImageHelper = new CameraImageHelper();
            File photoFile = null;

            if(CeoApplication.SendAllPhotosRealTime()){
                photoFile =  cameraImageHelper.getPCNPhotoFolder();
            } else if(removal.equalsIgnoreCase("Remove")){
                photoFile = cameraImageHelper.getRemovalPhotoFolder();
            } else {
                return;
            }
            File[] files = getListOfFile(photoFile.getAbsolutePath());
            for (int index = 0; index < files.length; index++) {

                String[] imageNameArr = files[index].getName().split("-",2);
                boolean sendPCNStatus = DBHelper.getSendPCNStatus(imageNameArr[0]);
                if(sendPCNStatus)
                {
                    int retry = imageRetry(files, index);
                    if(retry<=3)
                    {
                        uploadFile(files[index]);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void uploadFile(final File file){
        android.util.Log.e("file: ----", String.valueOf(file));

        HashMap<String,String> params=new HashMap<>();
        RetrofitRequest.Async.uploadFile(this, params, new RetrofitCallback<GeneralResponse>(this, R.string.please_wait, false) {

            @Override
            protected void onApiSuccess(@NonNull GeneralResponse response) {
                android.util.Log.e("onApiSuccess: ----", String.valueOf(response.isSuccess()));

                if (response.isSuccess()) {
                    try{
                        File photo = Utils.getFile(AppConstant.REMOVALPHOTO_FOLDER, file.getName());
                        if (photo.exists()) {
                            Utils.CopyDirectory(photo, Utils.getFile(AppConstant.SENT_PHOTO, photo.getName()));
                            photo.delete();
                        } else{
                            photo = Utils.getFile(AppConstant.PHOTO_FOLDER, file.getName());
                            if (photo.exists()) {
                                Utils.CopyDirectory(photo, Utils.getFile(AppConstant.SENT_PHOTO, photo.getName()));
                                photo.delete();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onApiFailure(@Nullable Response<GeneralResponse> response) {
                android.util.Log.e("onApiFailure: ----", String.valueOf(response));
                CommonUtils.showToast(context, Retrofit2_Util.getInstance().codeError(response), Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(@NonNull Call<GeneralResponse> call, @NonNull Throwable throwable) {
                super.onFailure(call, throwable);
                android.util.Log.e("onFailure: ----", String.valueOf(call));
                android.util.Log.e("onFailure-stackTrace: -", String.valueOf(throwable.getStackTrace()));
                android.util.Log.e("onFailure-message: -", String.valueOf(throwable.getMessage()));
                try {
                        CommonUtils.showToast(context, throwable.getMessage(), Toast.LENGTH_SHORT);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },file);
    }


    public void decodeBitmapFromFile() {
        try {
            CameraImageHelper cameraImageHelper = new CameraImageHelper();
            File photoFile = null;

            if(CeoApplication.SendAllPhotosRealTime()){
                photoFile =  cameraImageHelper.getPCNPhotoFolder();
            } else if(removal.equalsIgnoreCase("Remove")){
                photoFile = cameraImageHelper.getRemovalPhotoFolder();
            } else {
                return;
            }

            File[] fileArray = getListOfFile(photoFile.getAbsolutePath());
            String b64String;
            JSONObject jsonObject;
            for (int i = 0; i < fileArray.length; i++) {

                String[] imageNameArr = fileArray[i].getName().split("-",2);
                boolean sendPCNStatus = DBHelper.getSendPCNStatus(imageNameArr[0]);
                if(sendPCNStatus)
                {
                    int retry = imageRetry(fileArray, i);
                    if(retry<=3)
                    {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile + "/" + fileArray[i].getName());
                        try {
                            b64String = base64StringImage(bitmap, fileArray[i]);//resizeBase64Image(bitmap, IMG_WIDTH, IMG_HEIGHT);

                            int sizeInKB = b64String.getBytes().length / 1024;
                            if (sizeInKB > 22) {
                                int segs = sizeInKB / 21;
                                if ((/*segs*/ sizeInKB% 21) != 0)
                                    segs +=1;

                                int start = 0, end = 21 * 1024;
                                for (int k = 1; k <= segs; k++) {
                                    jsonObject = new JSONObject();
                                    jsonObject.put("to-uuid", CeoApplication.getUUID());
                                    jsonObject.put("photo", fileArray[i].getName());
                                    if(k == segs) {
                                        jsonObject.put("data", b64String.substring(start, b64String.length()));
                                    }else{
                                        jsonObject.put("data", b64String.substring(start, end));
                                    }
                                    jsonObject.put("chunk", String.valueOf(k));
                                    jsonObject.put("of", String.valueOf(segs));

                                    start = end;
                                    end = end + 21 * 1024;

                                    PubNubModule.publishRemovalPhotos(jsonObject);
                                    Runtime.getRuntime().gc();
                                    Runtime.getRuntime().freeMemory();
                                    Thread.sleep(1000);
                                }

                       /*JSONObject exifJSONObject = getExifFromImage(fileArray[i].getAbsolutePath());
                        if(exifJSONObject != null) {
                           jsonObject = new JSONObject();
                            jsonObject.put("to-uuid", CeoApplication.getUUID());
                            jsonObject.put("photo", fileArray[i].getName());
                            jsonObject.put("data", exifJSONObject);
                            jsonObject.put("chunk", "exif");
                            jsonObject.put("of", String.valueOf(segs));
                            PubNubModule.publishRemovalPhotos(jsonObject);
                        }*/
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        Runtime.getRuntime().gc();
                        Runtime.getRuntime().freeMemory();
                        Thread.sleep(500);
                    }
                }

           }
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    public File[] getListOfFile(String name){
        File file = new File(name);
        File[] fileList = file.listFiles();
        return fileList;
    }

    private static String base64StringImage(Bitmap bitmap, File file){
        String encoded = "";
        if(bitmap != null) {
            /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            System.gc();*/
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos); //Bitmap object is your image
                byte[] data = bos.toByteArray();

                TiffOutputSet outputSet = null;

                IImageMetadata metadata = Sanselan.getMetadata(file); // filepath is the path to your image file stored in SD card (which contains exif info)
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                if (null != jpegMetadata) {
                    TiffImageMetadata exif = jpegMetadata.getExif();
                    if (null != exif) {
                        outputSet = exif.getOutputSet();
                    }
                }
                if (null != outputSet) {
                    bos.flush();
                    bos.close();
                    bos = new ByteArrayOutputStream();
                    ExifRewriter ER = new ExifRewriter();
                    ER.updateExifMetadataLossless(data, bos, outputSet);
                    data = bos.toByteArray(); //Update you Byte array, Now it contains exif information!

                    encoded = Base64.encodeToString(data, Base64.DEFAULT);
                    System.gc();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return  encoded;
    }

    /*private JSONObject getExifFromImage(String imagePath){
        JSONObject jsonObject = null;
        try {
            ExifInterface exif = new ExifInterface(imagePath);

            jsonObject = new JSONObject();
            jsonObject.put(ExifInterface.TAG_MAKE, exif.getAttribute(ExifInterface.TAG_MAKE));
            jsonObject.put(ExifInterface.TAG_MODEL, exif.getAttribute(ExifInterface.TAG_MODEL));
            jsonObject.put(ExifInterface.TAG_IMAGE_LENGTH, exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            jsonObject.put(ExifInterface.TAG_IMAGE_WIDTH, exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            jsonObject.put(ExifInterface.TAG_DATETIME, exif.getAttribute(ExifInterface.TAG_DATETIME));
            jsonObject.put(ExifInterface.TAG_ORIENTATION, exif.getAttribute(ExifInterface.TAG_ORIENTATION));

            //GPS latitude and longitude
            jsonObject.put(ExifInterface.TAG_GPS_LATITUDE, convertFromDegreeMinuteSeconds(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)));
            jsonObject.put(ExifInterface.TAG_GPS_LATITUDE_REF, exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
            jsonObject.put(ExifInterface.TAG_GPS_LONGITUDE, convertFromDegreeMinuteSeconds(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)));
            jsonObject.put(ExifInterface.TAG_GPS_LONGITUDE_REF, exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));

        } catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }*/

    /*private static String convertFromDegreeMinuteSeconds(String stringDMS) {
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);
        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0 / D1;
        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0 / M1;
        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0 / S1;
        result = (float) (FloatD + (FloatM / 60) + (FloatS / 3600));
        return String.valueOf(result);
    }

    public static String resizeBase64Image(Bitmap bitmap, int width, int height){
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte[] ba = bao.toByteArray();
        String base64image = Base64.encodeToString(ba, Base64.DEFAULT);

        byte [] encodeByte=Base64.decode(base64image.getBytes(),Base64.DEFAULT);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length,options);


        if(image.getHeight() <= height && image.getWidth() <= width){
            return base64image;
        }
        image = Bitmap.createScaledBitmap(image, width, height, false);

        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,100, baos);

        byte [] b=baos.toByteArray();
        System.gc();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }*/

    private int imageRetry(File[] files, int index){
        int retry=1;
        ImageTable imageTable =  DBHelper.getFindImage(files[index].getName());
        if(imageTable!=null)
        {
            retry = imageTable.getRetryCount();
            DBHelper.updateRetryCount(files[index].getName(),retry);
        }
        else
        {
            ImageTable img   = new ImageTable();
            img.setImageName(files[index].getName());
            img.setRetryCount(1);
            img.save();
        }
        return retry;
    }



}
