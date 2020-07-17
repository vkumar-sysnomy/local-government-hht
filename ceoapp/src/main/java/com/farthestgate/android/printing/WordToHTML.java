package com.farthestgate.android.printing;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;

import java.lang.ref.WeakReference;


public class WordToHTML {

	public static WeakReference<Bitmap> PictureDrawable2Bitmap(PictureDrawable pictureDrawable){
        WeakReference<Bitmap> bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.MODEL.equalsIgnoreCase("SM-N9005")){
                bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth()/2,pictureDrawable.getIntrinsicHeight(), Config.ARGB_8888));
            }else{
                bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),pictureDrawable.getIntrinsicHeight(), Config.ARGB_8888));
            }
        }else{
            bitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),pictureDrawable.getIntrinsicHeight(), Config.ARGB_8888));
        }
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 576, 400, true); 
        //bitmap.recycle();
        Canvas canvas = new Canvas(bitmap.get());
        canvas.drawPicture(pictureDrawable.getPicture());
        return bitmap;
	}
	
}
