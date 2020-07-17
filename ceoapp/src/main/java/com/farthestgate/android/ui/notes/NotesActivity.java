
package com.farthestgate.android.ui.notes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.ui.admin.LoginActivity;
import com.farthestgate.android.ui.components.views.DrawView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Hanson Aboagye 06/2013
 * Modified 04/2014
 */
public class NotesActivity extends FragmentActivity
{

	DrawView dv;
	TextView tv;
	ScrollView sv;

	static StringBuilder sbSVG;
	
	public static Boolean exited;
	public static Integer triggerCounter;

	Boolean drawing = false;
	
	ImageButton btnHome;
	ImageButton btnReset;
	ImageButton btnSave;
	ImageButton btnUndo;

	ArrayList<Integer> undoPointers;
	String pcnNumber;
    String subjectLine;
    Integer observation;
	SimpleDateFormat sf = new SimpleDateFormat("ddMMyy_HHmmss_SS");


	
	final String END_LINE = "\">\n</path>\n</g>\n<g>\n";
    final String END_FILE = "\">\n</path>\n</g>\n</g>\n</svg>";
	final String START_LINE = "<path fill=\"none\" stroke=\"#000000\" stroke-width=\"1\" d=\"M";
	final String HEADER = "<?xml version=\"1.0\" standalone=\"yes\"?>" +
						  "<svg xmlns='http://www.w3.org/2000/svg' width=\"1080px\" height=\"1920px\" version=\"1.1\">" +
						  "<g transform=\"translate(0,0)\">\n<g>\n";
	final Integer firstUndoMarker = HEADER.length()-1;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_drawing_view);

        Intent data = getIntent();

        if (data != null)
        {
            //pcn disorder problem
            if(data.getStringExtra("pcn")!=null && data.getStringExtra("pcn").length()>0){
                pcnNumber = data.getStringExtra("pcn");
            }
            observation = data.getIntExtra("obs",0);
        }

		triggerCounter = 0;
		exited = false;
				
		btnHome = (ImageButton) findViewById(R.id.btnHome);
		btnReset = (ImageButton) findViewById(R.id.btnReset);
		btnSave = (ImageButton) findViewById(R.id.btnSave);
		btnUndo = (ImageButton) findViewById(R.id.btnUndo);
		
		btnReset.setOnClickListener(onResetClick);
		btnHome.setOnClickListener(onHomeClick);
		btnSave.setOnClickListener(onSaveClick);
		btnUndo.setOnClickListener(onUndoClick);
		
		dv = (DrawView) findViewById(R.id.noteView);
		dv.setSVGInterface(svgout);
			
		sbSVG = new StringBuilder();
		
		sbSVG.append(HEADER);
		
		undoPointers = new ArrayList<Integer>();
		undoPointers.add(firstUndoMarker);


	}
	
	static boolean penOn = false;
	Context ctx;
	

	
	static Boolean started = false;
	
	static String lastPoint;

    /*
	 * Implementation of the SVGInterface
	 */

	DrawView.SVGInterface svgout = new DrawView.SVGInterface()
	{
				
		@Override
		public void onPath(String point)
		{
			// Generate SVG string;
			if (point.length() > 0)
			{
				// record the last length before adding the string
				sbSVG.append(point);
			}				
			started = true;
			drawing = true;
			triggerCounter = 0;
		}

		@Override
		public void onMove(String point, Boolean afterAnim)
		{
			if (started)
			{
				sbSVG.append(END_LINE);				
				started = false;
				
			}				
			drawing = true;
			triggerCounter = 0;
			if (!afterAnim)
			{

				Integer lastItem =undoPointers.get(undoPointers.size()-1); 
				Integer lastIndex = sbSVG.length()-1;
				
				// can't add (start) pointer twice 
				if (lastItem < lastIndex)
					undoPointers.add(lastIndex);
			
				if (!started )
				{
					String gCheck = sbSVG.substring(sbSVG.lastIndexOf("<g>"), sbSVG.length()-1);
					
					if (!gCheck.equals("<g>"))
					{
						sbSVG.append("<g>");
					}
				
					sbSVG.append(START_LINE);
					started = true;
				}
			}
			if (!point.contains("null"))
			{
				sbSVG.append(point);
				lastPoint = point;
			}
			else
				sbSVG.append(lastPoint);

		}

		
		
		@Override
		public void onReplace(Integer length)
		{			
			try
			{
				if (undoPointers.size()>1)
				{
					
					Integer toIndex = sbSVG.length()-length -1;
					
					sbSVG =	sbSVG.replace(toIndex,undoPointers.get(undoPointers.size()-1) + 1, "");
					
					//undoPointers.remove(undoPointers.size()-1);
					
					String error = "<g>\n"+END_LINE;
					Integer startIndex = sbSVG.indexOf(error);
					
					if (startIndex > 0)
					{
						toIndex = startIndex  + error.length() -1;
						sbSVG =	sbSVG.replace(startIndex ,toIndex, "<g>");
					}
					
					undoPointers.remove(undoPointers.size()-1);
					
 					penOn = false;
 					triggerCounter = 0;					
				}	
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		@Override
		public void onUndo()
		{						
			if (undoPointers.size() > 1)
			{
				sbSVG =	sbSVG.replace(undoPointers.get(undoPointers.size()-1), sbSVG.length()-1, "");

				// This resets the path creation sequence 
				started = false;

				undoPointers.remove(undoPointers.size()-1);
				penOn = false;
				triggerCounter = 0;
			}
		}

		@Override
		public void penOn() 
		{			
			penOn = true;
			triggerCounter = 0;
			
		}

		@Override
		public void penOff() 
		{			
			penOn = false;
			
			triggerCounter = 0;
		}
	};

    @Override
    public void onBackPressed()
    {
       /* setResult(CeoApplication.RESULT_CODE_NOTES);
        finish();*/
        // ask user for confirmation
    }

    // Respond to Reset Click
	ImageButton.OnClickListener onResetClick = new ImageButton.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			//Add confirmation
			
		    response(3); //Erase

		}
		
	};
	
	// check for multiple clicks
	Boolean clicked = false;
	
	// Respond to Home Click
	
	ImageButton.OnClickListener onHomeClick = new ImageButton.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{			
			response(1); // Back to PCN
		}		
	};
	
	// Respond to Save Click	
	ImageButton.OnClickListener onSaveClick = new ImageButton.OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{

            AlertDialog.Builder builder = new AlertDialog.Builder(NotesActivity.this);
            final EditText tv = new EditText(NotesActivity.this);
            builder.setView(tv);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    subjectLine = tv.getText().toString();
                    response(2);
                }
            })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Subject Line");

            builder.create().show();


		}
	};
		
	ImageButton.OnClickListener onUndoClick = new ImageButton.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			triggerCounter = 0;
			if (dv.undoCounter > 0)
				dv.performUndo();		
		}		
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
			
		Integer opt;
		
		svgout.penOff();
		
		triggerCounter = 0;
		clicked = false;
		exited = false;
		

		
		 
		super.onActivityResult(requestCode, resultCode, data);
		
	}


	public void response(Integer res)
	{
		switch (res)		
		{
			case 1:
					clicked = true;
					exited = true;
					finish();					
					break;		
			case 2:

					clicked = true;
                    createSvg();
								
					break;
			case 3:
				{					
					dv.clear();		
					
					if (undoPointers.size() > 0)
						sbSVG.setLength(undoPointers.get(0));
					
					undoPointers.clear();
					started = false;
					undoPointers.add(firstUndoMarker);
					clicked = false;
					break;
				}
			case 4:
				{
					clicked = true;
					exited = true;
					finish();
					triggerCounter = 0;
					break;
				}
			case 5:
				{
					// this is to catch the cancel buttons and restart the timer				
					svgout.penOff();
					clicked = false;		
					triggerCounter = 0;
				}
		}	
	}

    /**
     *   PCN-timestamp-note-seq.svg
     *
     *   or CEO-timestamp-note-seq.svg
     */
    private void createSvg()
    {
        sbSVG.append(END_FILE);
        // double check the file
        String error = "<g>\n"+END_LINE;
        Integer startIndex = sbSVG.indexOf(error);
        Integer toIndex;
        while (startIndex > 0)
        {
            toIndex = startIndex  + error.length() -1;
            sbSVG =	sbSVG.replace(startIndex ,toIndex, "<g>");
            startIndex = sbSVG.indexOf(error);
        }
        try
        {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String noteFolderPath = LoginActivity.CONFIG_PATH_ROOT + "/" + LoginActivity.CONFIG_PATH_NOTES;
            String filePath = DBHelper.getCeoUserId() + "_" + timeStamp +".svg";
            File SVGFile = new CameraImageHelper().getFile(noteFolderPath,filePath);
            startIndex = sbSVG.indexOf("<g>\n\">");
            while (startIndex > 0)
            {
                toIndex = startIndex  + error.length() -1;
                sbSVG =	sbSVG.replace(startIndex ,toIndex, "<g>");
                startIndex = sbSVG.indexOf("<g>\n\">");
            }
            String finalst = sbSVG.toString().replace(error, "<g>");
            finalst = finalst.replace("<g><g>", "<g>");
            Intent noteIntent = new Intent();
            noteIntent.putExtra("subject",subjectLine);
            noteIntent.putExtra("path", SVGFile.getAbsolutePath());
            OutputStream os = new FileOutputStream(SVGFile ,true);
            os.write(finalst.getBytes());
            os.close();
            //restrict the user to take only one diagram note
			noteIntent.putExtra("diagramNoteTaken",true);
            setResult(CeoApplication.RESULT_CODE_NOTES, noteIntent);
            finish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}


