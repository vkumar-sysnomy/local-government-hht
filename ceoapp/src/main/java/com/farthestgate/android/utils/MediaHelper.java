package com.farthestgate.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.util.*;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.pcn.CameraActivity;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Used to make file system use in the tutorial a bit more obvious
 * in a production environment you wouldn't make these calls static
 * as you have no way to mock them for testing
 * @author paul.blundell
 *
 */
public class MediaHelper {

	public static File getOutputMediaFile(){
        String fileName = DBHelper.getCeoUserId() + "_" + DateTime.now().toString() + "_.jpg";
        File mediaFile = new CameraImageHelper().getFile(AppConstant.PHOTO_FOLDER, fileName);
        return mediaFile;
	}

	public static File saveToFile(Bitmap bitmap){
		File pictureFile = getOutputMediaFile();
		try {
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, CeoApplication.Image_x_size(), CeoApplication.Image_y_size(), true);
			FileOutputStream fos = new FileOutputStream(pictureFile);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			android.util.Log.d("CameraFragment", "File not found: " + e.getMessage());
			try {
				CeoApplication.LogError(e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			android.util.Log.d("CameraFragment", "Error accessing file: " + e.getMessage());
			try {
				CeoApplication.LogError(e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			try {
				CeoApplication.LogError(e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return pictureFile;
	}

	public static Bitmap ConvertBase64ToImage(String base64String) {
		Bitmap bmp = null;
		try {
			if (base64String == null || base64String.equals("")) {
				return null;
			} else {

				byte[] decodedString = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
				bmp =  BitmapFactory.decodeByteArray(
						decodedString, 0, decodedString.length);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

}
