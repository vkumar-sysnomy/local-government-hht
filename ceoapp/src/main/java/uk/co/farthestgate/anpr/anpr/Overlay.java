package uk.co.farthestgate.anpr.anpr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.farthestgate.android.R;

/**
 * Created by MartinStokes on 22/09/2017.
 */

public class Overlay extends View {
    private Context context;
    private Canvas canvas;

    Overlay(Context context) {
        super(context);
        this.context = context;
    }



    private Bitmap getBitmap(){


        Drawable drawable = context.getResources().getDrawable(R.drawable.blankreg);// Use your image
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        int width = 600;
        int height = width/3;
        if(width>canvas.getWidth()){
            width = canvas.getWidth()-20;
            height = width/3;
        }

        b = Bitmap.createScaledBitmap(b, width, height, true);
        return Bitmap.createBitmap(b, 0, 0, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);
        this.canvas = canvas;
        int width = canvas.getWidth();
        int height = canvas.getHeight();


        Paint p = new Paint();

        Bitmap bitmap = getBitmap();
        int bitmapX1 = (width/2)- (bitmap.getWidth()/2);
        int bitmapY1 = (height/2)- (bitmap.getHeight()/2);
        p.setAlpha(65);
        canvas.drawBitmap(bitmap, bitmapX1, bitmapY1, p);


    }
}
