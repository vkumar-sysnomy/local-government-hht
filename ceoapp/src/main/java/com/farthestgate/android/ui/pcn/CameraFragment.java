package com.farthestgate.android.ui.pcn;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.ErrorLocations;
import com.farthestgate.android.ui.components.views.CameraPreview;
import com.farthestgate.android.ui.components.widget.VerticalSeekBar;
import com.farthestgate.android.ui.photo_gallery.GalleryActivity;
import com.farthestgate.android.utils.CroutonUtils;
import com.farthestgate.android.utils.ZoomTransaction;

import java.io.File;
import java.util.List;

import static com.farthestgate.android.utils.MediaHelper.saveToFile;

/**
 *  Created by Hanson Aboagye 06/2014
 */

public class CameraFragment extends Fragment implements Camera.PictureCallback, Camera.AutoFocusCallback,
                                                        Camera.ShutterCallback, VerticalSeekBar.OnSeekBarChangeListener {

    public interface OnCammeraAction {
        void OnPictureTaken(String path);
    }

    private OnCammeraAction mListener;
    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView focus_btn;
    private VerticalSeekBar verticalSeekBar;
    private Drawable in_focus;
    private Drawable out_focus;
    private ImageButton btnSave;
    private ImageButton btnDiscard;
    private ImageButton btnGallery;
    private ImageView previewView;
    private MediaActionSound mediaActionSound;
    private ImageButton flashModeOn;
    private ImageButton flashModeOff;
    private ImageView pic_button;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCammeraAction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, container, false);

        mediaActionSound = new MediaActionSound();
        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK);

        in_focus    = getResources().getDrawable(R.drawable.photo_mark);
        out_focus   = getResources().getDrawable(R.drawable.photo_mark2);
        previewView = (ImageView) v.findViewById(R.id.preview);
        btnDiscard  = (ImageButton) v.findViewById(R.id.btnDiscardPreview);
        btnSave     = (ImageButton) v.findViewById(R.id.btnSavePreview);
        btnGallery  = (ImageButton) v.findViewById(R.id.galleryImageButton);
        cameraPreview = (CameraPreview) v.findViewById(R.id.camera_preview);
        flashModeOn = (ImageButton) v.findViewById(R.id.flashModeOn);
        flashModeOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CeoApplication.FLASH_MODE = true;
                if(cameraAvailable(camera)){
                    toggleFlashMode(!CeoApplication.FLASH_MODE);
                    initCameraPreview(false);
                }
            }
        });
        flashModeOff = (ImageButton) v.findViewById(R.id.flashModeOff);
        flashModeOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CeoApplication.FLASH_MODE = false;
                if(cameraAvailable(camera)){
                    toggleFlashMode(!CeoApplication.FLASH_MODE);
                    initCameraPreview(false);
                }
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getActivity(), GalleryActivity.class));
            }
        });

        pic_button = (ImageView) v.findViewById(R.id.photo_btn);
        pic_button.setVisibility(View.VISIBLE);
        pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.takePicture(CameraFragment.this, null, CameraFragment.this);
                }
                catch (RuntimeException eEx) {
                    ((CameraActivity) getActivity()).OnException(getActivity(), eEx, ErrorLocations.location001);
                    getActivity().setResult(CeoApplication.RESULT_CODE_ERROR);
                    getActivity().finish();
                }
            }
        });

        focus_btn = (ImageView) v.findViewById(R.id.focus);
        focus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                try {
                    camera.autoFocus(CameraFragment.this);
                } catch (RuntimeException autofocusException) {
                    //ignmore because this means that the photo has been taken or is being taken
                }
            }
        });
        verticalSeekBar = (VerticalSeekBar) v.findViewById(R.id.zoom);
        verticalSeekBar.setOnSeekBarChangeListener(this);

        toggleFlashMode(!CeoApplication.FLASH_MODE);
        // Camera may be in use by another activity or the system or not available at all
        camera = getCameraInstance();
        if((camera != null)){
            initCameraPreview(true);
        } else {
            getActivity().finish();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleFlashMode(!CeoApplication.FLASH_MODE);
        if (camera == null)  {
            camera = getCameraInstance();
            if(camera != null){
                initCameraPreview(true);
            }
        }
    }

    // ALWAYS remember to release the camera when you are finished
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success)
            focus_btn.setImageDrawable(in_focus);
        else
            focus_btn.setImageDrawable(out_focus);

        mediaActionSound.play(MediaActionSound.FOCUS_COMPLETE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            verticalSeekBar.setEnabled(false);
            zoomTo(verticalSeekBar.getProgress()).onComplete(new Runnable() {
                @Override
                public void run() {
                    verticalSeekBar.setEnabled(true);
                }
            }).go();
        }
    }

    public ZoomTransaction zoomTo(int level) {
        if (camera == null) {
            CroutonUtils.error(getActivity(), "Camera not available");
            return null;
        }
        else {
            Camera.Parameters params=camera.getParameters();

            if (level >= 0 && level <= params.getMaxZoom()) {
                return(new ZoomTransaction(camera, level));
            }
            else {
                throw new IllegalArgumentException(
                        String.format("Invalid zoom level: %d",
                                level));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onShutter() {
    //mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    }

    // Show the camera view on the activity
    private void initCameraPreview(boolean initCamera) {
        if (initCamera) {
            cameraPreview.init(camera);
            camera.getParameters().setPreviewFormat(ImageFormat.JPEG);
            camera.getParameters().setJpegQuality(80);
            Integer zoomMax = camera.getParameters().getMaxZoom();
            verticalSeekBar.setMax(zoomMax);
        }
        //Set Flash mode
        Camera.Parameters parameters = camera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null) {
            for (String flashMode : flashModes) {
                if (CeoApplication.FLASH_MODE) {
                    if (flashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        break;
                    }
                } else {
                    if (flashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        break;
                    }
                }
            }
        }
        parameters.setPictureSize(parameters.getPreviewSize().width, parameters.getPreviewSize().height);
        camera.setParameters(parameters);
    }

    @Override
    public void onPictureTaken(byte[] data, final Camera camera) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        previewView.setImageBitmap(bitmap);
        previewView.setVisibility(View.VISIBLE);
        btnDiscard.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        pic_button.setVisibility(View.INVISIBLE);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Animation scaleAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.preview_anim);
                previewView.startAnimation(scaleAnim);
                File file = saveToFile(bitmap);
                mListener.OnPictureTaken(file.getAbsolutePath());
                resetView();
            }
        });

        btnDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetView();
            }
        });

    }

    private void resetView() {
        previewView .setVisibility(View.INVISIBLE);
        btnDiscard  .setVisibility(View.INVISIBLE);
        btnSave     .setVisibility(View.INVISIBLE);
        pic_button.setVisibility(View.VISIBLE);
        toggleFlashMode(!CeoApplication.FLASH_MODE);
        cameraPreview.init(camera);
        camera.startPreview();
        previewView.destroyDrawingCache();
        if(cameraAvailable(camera)){
            initCameraPreview(false);
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available or doesn't exist
            com.farthestgate.android.utils.Log.d("getCamera failed", e);
        }
        return c;
    }
    private boolean cameraAvailable(Camera camera) {
        return camera != null;
    }

    private void toggleFlashMode(boolean on){
        if(on){
            flashModeOn.setVisibility(View.VISIBLE);
            flashModeOff.setVisibility(View.INVISIBLE);
        }else{
            flashModeOn.setVisibility(View.INVISIBLE);
            flashModeOff.setVisibility(View.VISIBLE);
        }
    }

}
