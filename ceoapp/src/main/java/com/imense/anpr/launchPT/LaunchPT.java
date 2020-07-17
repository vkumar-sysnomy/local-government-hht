package com.imense.anpr.launchPT;
//Copyright Imense Ltd 2017. Unauthorised usage or distribution strictly prohibited.


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.farthestgate.android.R;


public class LaunchPT extends Activity {
	
	public static String tag = "launchPT"; //tag for debugging
	public boolean debug = true;
	
	Button launchButton1, launchButton2, launchButton1_portrait, launchButton2_portrait;
	
	//invocation codes for ANPR/ALPR Platform
	private final static String INVOCATION_USER  = "dj^ZjVwGs&dbalHS£gd"; //Standard user: not allowed to change preferences or view list entries
	private final static String INVOCATION_ADMIN = "Bsv$28!Gsda7jeK^V1s"; //Privileged user: able to change settings and/or edit list entries

	private static int REQUESTCODE = 55;
	
	
	//return messages from ANPR/ALPR Platform
	private final static int PT_INVALID_INVOCATION = 99;
	private final static int PT_LICENSE_MISSING_OR_INVALID = 100;
	private final static int PT_ANPR_NOTONWHITELIST = 101;
	private final static int PT_ANPR_PERMITEXPIRED = 102;
	
	protected String licenseKey = null;
	
	Intent parkTellIntent = null;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launchpt);
		
		launchButton1 = (Button) findViewById(R.id.launchButton1);
		launchButton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			  LaunchPT.this.launchPT(false, false);
			}
		});	
		
		launchButton2 = (Button) findViewById(R.id.launchButton2);
		launchButton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			  LaunchPT.this.launchPT(true, false);
			}
		});	

		launchButton1_portrait = (Button) findViewById(R.id.launchButton1_portrait);
		launchButton1_portrait.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			  LaunchPT.this.launchPT(false, true);
			}
		});	
		
		launchButton2_portrait = (Button) findViewById(R.id.launchButton2_portrait);
		launchButton2_portrait.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			  LaunchPT.this.launchPT(true, true);
			}
		});	

	}
	
	
	void launchPT( boolean admin, boolean portraitOrientation ) 
	{
		//obtain an Intent to launch ANPR/ALPR Platform
		try {
			parkTellIntent = new Intent();
			parkTellIntent.setComponent(new ComponentName("com.imense.anprPlatform", "com.imense.anprPlatform.ImenseParkingEnforcer"));
			
			//authenticate the request with the correct invocation code
			if (admin) parkTellIntent.putExtra("invocationcode", INVOCATION_ADMIN);
			else parkTellIntent.putExtra("invocationcode", INVOCATION_USER);

			
			//set PT into portrait mode (not recommended since it reduces effective plate pixel resolution)
			if (portraitOrientation) parkTellIntent.putExtra("orientation", "portrait");

			
			//optionally instruct PT to start scan (i.e. invoke shutter button) immediately
			parkTellIntent.putExtra("startscan", "1"); //leave undefined or set to "0" to not start scan immediately
			

			/*//////////////////////////////
			//Optionally explicitly specify values for settings such as folder for data and images, option to save context image, scan time threshold, minimum confidence threshold, region and read options.

			///////////List settings
			parkTellIntent.putExtra("preferences_saveimages_path", "/mnt/sdcard"); //Folder for data and images; has to exist and be writable
			parkTellIntent.putExtra("preferences_vehiclesfilename", "parkingList.csv"); //Vehicles list file name. Default value="parkingList.csv"
			parkTellIntent.putExtra("preferences_alertsfilename", "parkingAlerts.csv"); //Alerts list\Whitelist file name. Default value="parkingAlerts.csv"

			///////////General settings
			parkTellIntent.putExtra("preferences_savecutouts", "true"); //Save plate cut-out image after every good read. Value can be "true" or "false" (default="true")
			parkTellIntent.putExtra("preferences_savecontextimages", "false"); //Save context image to SD card after every good read. Value can be "true" or "false" (default="false")

			parkTellIntent.putExtra("preferences_expungePlatesAfterNhours", "72"); //Expunge vehicle list entries after this many hours. Value must be positive numeric, default="72".
			parkTellIntent.putExtra("preferences_warnAfterNmins", "0"); //Warn if parked vehicle time exceeds this many minutes. Value must be positive numeric, default="0".

			parkTellIntent.putExtra("preferences_confGoodread", "80"); //"High confidence threshold (0-100). Value must be positive numeric, default="80".
			
			parkTellIntent.putExtra("preferences_scanTimeout", "90"); //Continuous scan timeout (seconds). Value must be positive numeric, default="90".
			parkTellIntent.putExtra("preferences_playsound", "true"); //Play beep after every high confidence scan. Value can be "true" or "false" (default="true")
			parkTellIntent.putExtra("preferences_showUVC", "false"); //Display button to connect to USB camera (via UVC) if possible. Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_showsingleshot", "false"); //Display button to save single image to SD card. Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_saveSingleshotInColour", "false"); //Store single PIC images in colour. Value can be "true" or "false" (default="false")

			parkTellIntent.putExtra("preferences_showtorch", "false"); //Display torch button. Value can be "true" or "false" (default="false")
    
			parkTellIntent.putExtra("preferences_viewfinder", "false"); //Enable adjustable zone-of-interest within viewfinder for faster processing. Value can be "true" or "false" (default="false")

			///////////Parking Bay Numbers
			parkTellIntent.putExtra("preferences_pbn_enable", "false"); //Automatically apply PBN (parking bay number). Value can be "true" or "false" (default="false")
			parkTellIntent.putExtra("preferences_pbn_prefix", ""); //PBN prefix string. Text value of 0 to 5 characters, default is "" (empty string).
			parkTellIntent.putExtra("preferences_pbn_start", "00"); //PBN start value that is applied to the next parking bay. Must be a string of digits of between 2 and 5 characters, default is "00".
			parkTellIntent.putExtra("preferences_pbn_increment", "1"); //PBN increment value (can be positive or negative). Must be a string of digits (optionally starting with "-" to indicate a negative increment) of between 1 and 3 characters, default is "1".

			///////////Custom Data Fields
			parkTellIntent.putExtra("preferences_data1prompt", "Custom Data 1"); //Prompt for custom data field 1. Must be a text string of 0 to 20 characters, default is "Custom Data 1".
			parkTellIntent.putExtra("preferences_data2prompt", "Custom Data 2"); //Prompt for custom data field 2. Must be a text string of 0 to 20 characters, default is "Custom Data 2".
			parkTellIntent.putExtra("preferences_data3prompt", "Custom Data 3"); //Prompt for custom data field 3. Must be a text string of 0 to 20 characters, default is "Custom Data 3".

			parkTellIntent.putExtra("preferences_audiomax", "60"); //Maximum duration of voice note audio recordings in seconds. Value must be positive numeric, default="60". 

			//**/


			
			//if we already have a license key, we send it to Platform
			if (licenseKey!=null) parkTellIntent.putExtra("licensekey", licenseKey);
	
			
			startActivityForResult(parkTellIntent, REQUESTCODE);
		} 
		catch (Exception err) {
			Toast.makeText(LaunchPT.this, "ANPR Platform not found: please install it", Toast.LENGTH_LONG).show();
		}

	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (debug) Log.d(tag, "onActivityResult:  requestCode="+requestCode+", resultCode="+resultCode+", data="+data+", parkTellIntent="+parkTellIntent);
		
		int returnMessage = 0;
		
		if (data!=null) returnMessage = data.getExtras().getInt("message");
		
		
		if (returnMessage == PT_ANPR_NOTONWHITELIST)
		{
				String sRegNumber = data.getExtras().getString("anpr_not_in_whitelist");
				int regConf = data.getExtras().getInt("anpr_not_in_whitelist_conf");
				Toast.makeText(this, "Platform returned with vehicle plate that is not in the whitelist: "+sRegNumber+" (conf="+regConf+")", Toast.LENGTH_LONG).show();
			
		} else if (returnMessage == PT_ANPR_PERMITEXPIRED)
		{
				String sRegNumber = data.getExtras().getString("anpr_permit_expired");
				int regConf = data.getExtras().getInt("anpr_permit_expired_conf");
				String sTimeExceeded = data.getExtras().getString("time_since_permit_expired");
				Toast.makeText(this, "Platform returned with whitelisted plate: "+sRegNumber+" (conf="+regConf+") having exceeded parking permit by "+sTimeExceeded, Toast.LENGTH_LONG).show();
			
		} else if (returnMessage == PT_LICENSE_MISSING_OR_INVALID)
		{
			
			final String deviceID = data.getExtras().getString("duid"); //unique device ID
			final LaunchPT caller = this;
			
			//obtain new license key
			new AlertDialog.Builder(this)
			.setTitle( "License Verification Problem" )
			.setCancelable(false)
			.setMessage( "Platform reports: license key missing or invalid. Please ensure that your device's WiFi adapter is enabled and has Internet access, then "+
					"click <"+this.getString(android.R.string.ok)+"> to (re)generate a valid license key from our server.")
					.setPositiveButton( android.R.string.ok,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();

							// try to obtain new license key from Imense Server
							new ImenseLicenseServer( caller, deviceID ).execute();
						}
					})
					.setNegativeButton( android.R.string.cancel,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			
		}
	
	}

	
	
	
}


