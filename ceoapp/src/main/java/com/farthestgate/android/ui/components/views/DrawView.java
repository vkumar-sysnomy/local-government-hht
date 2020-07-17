package com.farthestgate.android.ui.components.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

public class DrawView extends View 
{

		static public SVGInterface onPathChange;
		
		public interface SVGInterface 
		{
			public void onPath(String point);
			public void onMove(String point, Boolean afterAnim);
			public void onUndo();
			public void onReplace(Integer length);
			public void penOn();
			public void penOff();
		}
		
		public void setSVGInterface(SVGInterface listener) 
		{
			onPathChange = listener;
		}
			
		private static final float STROKE_WIDTH = 4f;

		Handler bufferHandler;
		Canvas mCanvas;
		StringBuilder sb;		
		
		 
		Float[] newPoint = new Float[2];
		Float[] lastPoint = new Float[2];
			
		
		private final String PATH_START = "<path fill=\"none\" stroke=\"#000000\" stroke-width=\"1\" d=\"M";
			
		private Paint paint = new Paint();
		private Path path = new Path();
		
		private Bitmap mBitmap;
			
		private float mX, mY;	
		private float eventX, eventY;
		    
		private HashMap<Integer,Path> undoList;
			
		private static final float RANGE2 = 4;
		    
		public Integer undoCounter = 0;
		
		public DrawView(Context context, AttributeSet attrs) 
		{
			super(context, attrs);
			
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH);
			
			bufferHandler = new Handler();
			
			sb = new StringBuilder();
			    
			undoList = new HashMap<Integer,Path>();
			
			
		}

		
		
	 	/**
		* Erases the drawing.
		*/
		public void clear() 
		{
		    path.reset();
		    
		    mBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
	        
		    mCanvas = new Canvas(mBitmap);
		    // Redraw the view
		    invalidate();	
		    undoList.clear();
		    undoCounter = 0;
		    onPathChange.onUndo();
		}

	
	
		protected void onSizeChanged(int w, int h, int oldw, int oldh)
		{
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawColor(0x00000000);

			canvas.drawBitmap(mBitmap, 0, 0, paint);

			canvas.drawPath(path, paint);
		}
	
      
		private void touch_start(float x, float y) 
		{
			path.reset();
    	  
			synchronized(newPoint)
			{
				newPoint[0] = x;
				newPoint[1] = y;
				bufferHandler.post(newPath);
			}
    
			path.moveTo(x, y);
    			
			mX = x;
			mY = y;
		}

    
		private void touch_move(float x, float y) 
		{
			
	    	  float dx = x - mX;
	    	  float dy = y - mY;    	  
	    	  
	    	  dx = Math.abs(dx);
	    	  dy = Math.abs(dy);
	    	  
	    	  // we use range to skip some points that are not a big enough change
	    	  if (dx >= RANGE2 || dy >= RANGE2)
	    	  {
	    		  float nextX = (x + mX)/2;
	    		  float nextY = (y + mY)/2;  
	    		  
	        	  path.quadTo(mX, mY, nextX , nextY);
	        	  
	        	  RectF bounds = new RectF();
	    		  path.computeBounds(bounds, false);
		  		
	    		  if (bounds.contains(0f, 0f))
	    		  {
	    			  lastPoint[0] = x;
	    			  lastPoint[1] = y;        	  
	  				
	    			 Path oldPath = new Path();
	    				
	    				// 	recompile the path
	    	    	  			
	    				for (int i =0;i < undoCounter-1;i++)
	    				{
	    					oldPath.addPath(undoList.get(i)); 
	    				}
	    	    	  
	    				path.set(oldPath);
	    	    	  	
	    				undoCounter --;
	    				mBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
	    		    	  
	    				mCanvas = new Canvas(mBitmap);
	    				// Redraw the view    	  
	    				mCanvas.drawBitmap(mBitmap, 0, 0, paint);
	    				mCanvas.drawPath(path, paint);
	    				invalidate();	    			
	    				
	    				bufferHandler.post(getSVG);
	    			  
	    		  }			
	        	  
	        	
	  			  mX = x;
	  			  mY = y;	
	  	
	    	  }	    	      	   
		}
 
		Boolean errorPoint = false;
		
		private void touch_up(Float X, Float Y) 
		{
			
			RectF bounds = new RectF();
   		  	path.computeBounds(bounds, false);
	  		
   		  	if (bounds.contains(0f, 0f))
   		  	{
   			  	errorPoint = true;
   		  	}	
   		  	else
   		  	{
   			  	path.lineTo(mX, mY);  
   		  	}
   		  	
   		  	path.computeBounds(bounds, false);
	  		
   		  	if (bounds.contains(0f, 0f))
   		  	{
   		  		
	    	  	
				Path oldPath = new Path();
				
				// 	recompile the path
	    	  			
				for (int i =0;i < undoCounter-1;i++)
				{
					oldPath.addPath(undoList.get(i)); 
				}
	    	  
				path.set(oldPath);
	    	  	
				undoCounter --;
				mBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
		    	  
				mCanvas = new Canvas(mBitmap);
				// Redraw the view    	  
				mCanvas.drawBitmap(mBitmap, 0, 0, paint);
				mCanvas.drawPath(path, paint);
				invalidate();	    			
				
				bufferHandler.post(getSVG);
			  

   		  	}	
			
   		  	path.computeBounds(bounds, false);
	  		
		  	if (bounds.contains(0f, 0f))
		  	{
		  		errorPoint = false;
		  	}
   		  	
			synchronized(path)
			{
				addUndo();
				lastPoint[0] = X;
				lastPoint[1] = Y;        	  
				bufferHandler.post(getSVG);    		
			}
	    	  
			mCanvas.drawPath(path, paint);
			
		}
	
		
		
		@Override
		public boolean onTouchEvent(MotionEvent event) 
		{
			eventX = event.getX();
			eventY = event.getY();
	
			if (this.isEnabled())
			{
				
				switch (event.getAction()) 
				{
					case MotionEvent.ACTION_DOWN:
						
						path.incReserve(100); // prepare for points to be added
						onPathChange.penOn();		
						touch_start(eventX,eventY);	    			
						invalidate();						
						break;
		
					case MotionEvent.ACTION_MOVE:
						touch_move(eventX,eventY);	    			
						invalidate();	    			
						break;
			    	  	
			    	  	
					case MotionEvent.ACTION_UP:
						touch_up(eventX,eventY);	    			
						invalidate();
						onPathChange.penOff();

						break;
			    	  	
		
					default:
						Log.d("Ignored touch event: " , event.toString());
						return false;
				}
				
			}
			return true;
		}
	
	    
		private void addUndo()
		{
			Path tempPath = new Path(path);
	    	  
			undoList.put(undoCounter, tempPath);
			undoCounter ++;
		}
	      
		public void performUndo()
		{ 
			// remove last path
			undoList.remove(undoCounter-1);
			onPathChange.onUndo();
    	  
			Path oldPath = new Path();
			
			// 	recompile the path
    	  			
			for (int i =0;i < undoCounter-1;i++)
			{
				oldPath.addPath(undoList.get(i)); 
			}
    	   
			path.set(oldPath);
    	  	
			undoCounter --;
			mBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
	    	  
			mCanvas = new Canvas(mBitmap);
			// Redraw the view    	  
			mCanvas.drawBitmap(mBitmap, 0, 0, paint);
			mCanvas.drawPath(path, paint);
			invalidate();
		
			
	
			
		} 
		
	
		Runnable getSVG = new Runnable()
		{
			
			@Override
			public void run()
			{
				sb.setLength(0);
				
				int lastStart = 0;
				int lastEnd = 0;
				
				PathMeasure pm = new PathMeasure(path, false);
				float length = pm.getLength();
				float distance = 0f;
				float jump = 2f; // we are moving in 2 point jumps - can be more 
				
				float[] aPoint = new float[2];
			
				if (length == distance)
				{
					Integer backSpace = PATH_START.length() + pointLength;
					onPathChange.onReplace(backSpace);
				}
				else
				{
					sb.append("\n Q");
		    		  
					int oddCounter = 0;		    		  
				
					while (distance < length) 
					{
						
						// get point from the path
						pm.getPosTan(distance, aPoint, null);
						lastStart = sb.length();  
						
						sb.append(aPoint[0]);
						sb.append(",");
						sb.append(aPoint[1]);
						sb.append(" ");
						
						lastEnd = sb.length()-1;
						distance = distance + jump;
						oddCounter++;
					}
			    		  
					//delete last control point if needed
					if ((oddCounter & 1) != 0)
						sb.delete(lastStart, lastEnd);
			    		  
					// 	reset path so it stops filling up and taking up all the memory and 
					// 	to stop it being drawn again
			        	  
					path.reset();
					onPathChange.onPath(sb.toString());
				}	
			}
				
				
		};
		
		int pointLength = 0;
		Boolean afterAnim = false;
		
		Runnable newPath = new Runnable()
		{
			
			@Override
			public void run()
			{
				sb.setLength(0);
				sb.append(newPoint[0]);
				sb.append(",");
				sb.append(newPoint[1]);
				pointLength = sb.length();
				newPoint = new Float[2];
				onPathChange.onMove(sb.toString(),afterAnim);
			}								
		};

		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);
		}			
}