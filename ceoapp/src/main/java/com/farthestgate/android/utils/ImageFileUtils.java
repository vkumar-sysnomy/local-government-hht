package com.farthestgate.android.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.SharedPreferenceHelper;
import com.farthestgate.android.model.database.PCNPhotoTable;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.ui.photo_gallery.GalleryActivity;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: eliminate this file

public class ImageFileUtils {

	private Context _context;

	// constructor
	public ImageFileUtils(Context context) {
		this._context = context;

    }

	/*
	 * Reading file paths from SDCard
	 */
	public ArrayList<String> getPhotoPaths() {
		ArrayList<String> filePaths = new ArrayList<String>();

		File directory = new File(
				android.os.Environment.getExternalStorageDirectory()
						+ File.separator + AppConstant.PHOTO_FOLDER
        );

		// check for directory
		if (directory.isDirectory()) {
			File[] listFiles = directory.listFiles();
            DateTime today = new DateTime();
            List<PCNPhotoTable> ceoPhotos = DBHelper.GetPhotosForCEO();
            long startofday = today.getMillis() - today.getMillisOfDay();
			if (listFiles.length > 0) {
				for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i].lastModified() > startofday)
                    {
                        for (PCNPhotoTable photo: ceoPhotos) {
                            String filePath = listFiles[i].getAbsolutePath();
                            if (filePath.equals(photo.getFileName()))
                                filePaths.add(filePath);
                        }
                    }
				}
			} else {
			    CroutonUtils.info(((GalleryActivity) _context), "You have not taken any photos in this session");
			}
		} else {
            CroutonUtils.info(((GalleryActivity) _context), "You have not taken any photos in this session");
		}
        Collections.sort(filePaths);

		return filePaths;
	}


	/*
	 * getting screen width
	 */
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}
}
