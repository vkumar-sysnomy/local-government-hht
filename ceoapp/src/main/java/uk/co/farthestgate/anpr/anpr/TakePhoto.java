package uk.co.farthestgate.anpr.anpr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.VRMAutomatedLookupTask;
import com.farthestgate.android.model.PaidParking;
import com.farthestgate.android.ui.admin.BaseActivity;
import com.farthestgate.android.ui.components.Utils;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.DeviceUtils;
import com.imense.anpr.launchPT.AnprVrmDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

//import java.lang.annotation.Native;

public class TakePhoto extends BaseActivity implements VRMAutomatedLookupTask.VRMAutomatedLookupListener, AnprVrmDialog.OnAnprVrmListener {

    public native int  InitCaller(int v);

    public native byte[]  ProcessImageCaller(byte[] buffer, int width, int height,
                                             int aspectRatioX, int apectRatioY,
                                             int useLightCorrection, int detectSquarePlates, int detectWhiteOnBlack);
    public int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE;

    public int newWidth = 640;
    public int newHeight = 480;
    Bitmap bitmap = null;

    public int rotation;

    static public int count = 0;

//    private TextView textView;
    private Preview mPreview;
    private CameraSource mCameraSource;
    private String TAG = "** TAKEPHOTO **";
    public String vrmText;
    private boolean vrmTextCaptured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
//        System.loadLibrary("cardet");

        int f = InitCaller(2);

//        textView = (TextView) findViewById(R.id.textView);

        mPreview = (Preview) findViewById(R.id.preview);
        boolean autoFocus = true;
        boolean useFlash = false;
        createCameraSource(autoFocus, useFlash);
        startCameraSource();

        Overlay overlay = new Overlay(this.getApplicationContext());
        addContentView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

    }

    public void editBitmap(Bitmap b){

        int scaledTo = 1500;
        int w = b.getWidth();
        int incr = w / 100;

        scaledTo =  10 * incr;
        if(scaledTo > 1500){
            scaledTo = 1500;
        }
        if(scaledTo < newWidth) {
            scaledTo = newWidth;
        }
        b = scaleDown(b, scaledTo, true);
        readPlate(b);
    }

    public void readPlate(Bitmap b){
        int height1 = b.getHeight();
        int width1 = b.getWidth();
        int fromTop = (height1 - newHeight) / 2;
        int fromLeft = (width1 - newWidth) / 2;

        Matrix matrix = new Matrix();
        matrix.postScale(1, -1, width1/2, height1/2); // turn upside down

        // create bitmap at the correct size - upside down
        Bitmap bit = Bitmap.createBitmap(b, fromLeft, fromTop, newWidth, newHeight, matrix, true);

        // turn bitmap into 8bit grayscale
        final byte[] buffer = CreateGrayBitmapArray(bit);

        byte[] buffer1 = Arrays.copyOfRange(buffer, 1078, buffer.length);

        byte[] ret = ProcessImageCaller(buffer1, newWidth,newHeight, 0, 0, 0, 0, 0);

        ByteBuffer bb = ByteBuffer.wrap(ret).order(ByteOrder.LITTLE_ENDIAN);
        int noOfChars = bb.getInt(20);
        final byte[] ret1 =  Arrays.copyOfRange(ret, 8, 8 + noOfChars);

        if(noOfChars < 3){
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                try {
                    if (!vrmTextCaptured) {
                        vrmText = new String(ret1, "UTF-8");
                        checkAutomatedVRMLookup();

                        vrmTextCaptured = true;
                    }

                    /*vrmText = new String(ret1, "UTF-8");
                    textView.setText(vrmText);*/

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void openAnprVrmDialog(String vrm){
        AnprVrmDialog anprVrmDialog = AnprVrmDialog.newInstance(vrm);
        anprVrmDialog.setCancelable(false);
        anprVrmDialog.show(getFragmentManager(), "");
    }

    private void checkAutomatedVRMLookup(){
        if (DeviceUtils.isConnected(this)) {
            VRMAutomatedLookupTask vrmAutomatedLookupTask =
                    new VRMAutomatedLookupTask(this, vrmText, this);
            vrmAutomatedLookupTask.execute();
        } else{
            CroutonUtils.errorMsgInfinite(this, "No internet connectivity available at the time of search");
            openAnprVrmDialog(vrmText);
        }
    }



    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {

        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        if(height < 480){
            height = 480;
        }
        if(width < 640){
            width = 640;
        }

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }


    private  byte[] Color_palette = new byte[1024]; //a palette containing 256 colors
    private   byte[] BMP_File_Header = new byte[14];
    private  byte[] DIB_header = new byte[40];
    private   byte[] Bitmap_Data = null;


    //returns a byte array of a grey scale bitmap image
    public    byte[] CreateGrayBitmapArray(Bitmap Image) {
        try {
            create_parts(Image);
            //Create the array
            byte[] bitmap_array = new byte[BMP_File_Header.length + DIB_header.length
                    + Color_palette.length + Bitmap_Data.length];
            Copy_to_Index(bitmap_array, BMP_File_Header, 0);
            Copy_to_Index(bitmap_array, DIB_header, BMP_File_Header.length);
            Copy_to_Index(bitmap_array, Color_palette, BMP_File_Header.length + DIB_header.length);
            Copy_to_Index(bitmap_array, Bitmap_Data, BMP_File_Header.length + DIB_header.length + Color_palette.length);



            return bitmap_array;
        } catch (Exception e) {
            return null; //return a null single byte array if fails
        }
    }

    public byte[] reverseBitmap(byte[] bitmap_array){
        byte[] newBytes = new byte[bitmap_array.length];
        Log.d("*** newBytes.length", String.valueOf(newBytes.length));
        for(int i = 0; i<bitmap_array.length; i++){
            newBytes[i] = bitmap_array[bitmap_array.length - i - 1];
        }
        return newBytes;
    }


    //creates byte array of 256 color grayscale palette
    private   byte[] create_palette() {
        byte[] color_palette = new byte[1024];
        for (int i = 0; i < 256; i++) {
            color_palette[i * 4 + 0] = (byte) (i); //bule
            color_palette[i * 4 + 1] = (byte) (i); //green
            color_palette[i * 4 + 2] = (byte) (i); //red
            color_palette[i * 4 + 3] = (byte) 0; //padding
        }
        return color_palette;
    }


    //adds dtata of Source array to Destinition array at the Index
    private   boolean Copy_to_Index(byte[] destination, byte[] source, int index) {
        try {
            for (int i = 0; i < source.length; i++) {
                destination[i + index] = source[i];
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    //create different part of a bitmap file
    private    void create_parts(Bitmap img) {
        //Create Bitmap Data
        Bitmap_Data = ConvertToGrayscale(img);
        //Create Bitmap File Header (populate BMP_File_Header array)
        Copy_to_Index(BMP_File_Header, new byte[]{(byte) 'B', (byte) 'M'}, 0); //magic number
        Copy_to_Index(BMP_File_Header, writeInt(BMP_File_Header.length
                + DIB_header.length + Color_palette.length + Bitmap_Data.length), 2); //file size
        Copy_to_Index(BMP_File_Header, new byte[]{(byte) 'M', (byte) 'C', (byte) 'A', (byte) 'T'}, 6); //reserved for application generating the bitmap file (not imprtant)
        Copy_to_Index(BMP_File_Header, writeInt(BMP_File_Header.length
                + DIB_header.length + Color_palette.length), 10); //bitmap raw data offset
        //Create DIB Header (populate DIB_header array)
        Copy_to_Index(DIB_header, writeInt(DIB_header.length), 0); //DIB header length
        Copy_to_Index(DIB_header, writeInt(((Bitmap) img).getWidth()), 4); //image width
        Copy_to_Index(DIB_header, writeInt(((Bitmap) img).getHeight()), 8); //image height
        Copy_to_Index(DIB_header, new byte[]{(byte) 1, (byte) 0}, 12); //color planes. N.B. Must be set to 1
        Copy_to_Index(DIB_header, new byte[]{(byte) 8, (byte) 0}, 14); //bits per pixel
        Copy_to_Index(DIB_header, writeInt(0), 16); //compression method N.B. BI_RGB = 0
        Copy_to_Index(DIB_header, writeInt(Bitmap_Data.length), 20); //lenght of raw bitmap data
        Copy_to_Index(DIB_header, writeInt(1000), 24); //horizontal reselution N.B. not important
        Copy_to_Index(DIB_header, writeInt(1000), 28); //vertical reselution N.B. not important
        Copy_to_Index(DIB_header, writeInt(256), 32); //number of colors in the palette
        Copy_to_Index(DIB_header, writeInt(0), 36); //number of important colors used N.B. 0 = all colors are imprtant
        //Create Color palett
        Color_palette = create_palette();
    }


    //convert the color pixels of Source image into a grayscale bitmap (raw data)
    private    byte[] ConvertToGrayscale(Bitmap Source) {
        Bitmap source = (Bitmap) Source;
        int padding = (source.getWidth() % 4) != 0 ? 4 - (source.getWidth() % 4) : 0; //determine padding needed for bitmap file
        byte[] bytes = new byte[source.getWidth() * source.getHeight() + padding * source.getHeight()]; //create array to contain bitmap data with paddin
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getPixel(x, y);
                int g = (int) (0.3 * Color.red(pixel) + 0.59 * Color.green(pixel) + 0.11 * Color.blue(pixel)); //grayscale shade corresponding to rgb
                bytes[(source.getHeight() - 1 - y) * source.getWidth() + (source.getHeight() - 1 - y) * padding + x] = (byte) g;
            }
            //add the padding
            for (int i = 0; i < padding; i++) {
                bytes[(source.getHeight() - y) * source.getWidth() + (source.getHeight() - 1 - y) * padding + i] = (byte) 0;
            }
        }



        return bytes;
    }


    /**
     * Write integer to little-endian
     *
     * @param value
     * @return
     * @throws IOException
     */
    private  byte[] writeInt(int value) {
        byte[] b = new byte[4];

        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);

        return b;
    }


    static {
        System.loadLibrary("cardet");
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }
    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }



    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), this)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
//                        .setRequestedPreviewSize(2560, 1920)
                        .setRequestedFps(1.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    public void tryFrame(byte[] data, int rotation) throws IOException, InterruptedException {
        if(count > 0){
            return;
        }
        //count++;
        System.gc();
        this.rotation = rotation;

        Log.d("*****TRY FRAME", data.toString());
        Log.d("*****data length", String.valueOf(data.length));


        YuvImage im = new YuvImage(data, ImageFormat.NV21, CameraSource.mRequestedPreviewWidth, CameraSource.mRequestedPreviewHeight, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        im.compressToJpeg(new Rect(0,0, CameraSource.mRequestedPreviewWidth, CameraSource.mRequestedPreviewHeight), 100, out);
        byte[] imageBytes = out.toByteArray();
        /*FileOutputStream fos = new FileOutputStream(file);
        fos.write(imageBytes);
        fos.flush();
        fos.close();
        Log.d("****","thread going to sleep");
        Thread.sleep(5000);
        bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),  Uri.fromFile(file));*/

        Matrix matrix = new Matrix();
        int angle = rotation * 90;


        Log.d("*** angle ", String.valueOf(angle));
        matrix.postRotate(angle);

        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, out.size());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, CameraSource.mRequestedPreviewWidth, CameraSource.mRequestedPreviewHeight, matrix, true);
        // Log.d("****","thread waking up...");



        editBitmap(bitmap);
        System.gc();
    }

    @Override
    public void vrmLookupPaidParking(ArrayList<PaidParking> paidParkings, String vrmText, boolean isError) {
        if((paidParkings != null && paidParkings.size() == 0) || isError){
            // vibration for 800 milliseconds
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(800);
            openAnprVrmDialog(vrmText);
        } else{
            Utils.showDialog(this, "Valid parking session found", "Parking Session");
            vrmTextCaptured = false;
        }
    }

    @Override
    public void onAnprClicked(String vrmText) {
        this.vrmText = vrmText;
        checkAutomatedVRMLookup();
    }
}
