package com.farthestgate.android.utils;

import android.app.Activity;
import android.graphics.Color;
import android.widget.LinearLayout.LayoutParams;

import com.farthestgate.android.R;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class CroutonUtils {
	
    public static final int DURATION_INFINITE = -1;
    public static final int DURATION_SHORT  = 800;
    public static final int DURATION_MEDIUM = 2000;
    public static final int DURATION_LONG   = 10000;

	public static Crouton noInternet(final Activity context) {
		Crouton crouton = Crouton.makeText(context,
						R.string.no_connectivity,
						new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_INFINITE).build())
						.setBackgroundColorValue(Color.RED)
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		/*crouton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			}});*/
		crouton.show();
		return crouton;
	}

    public static Crouton error(int duration, final Activity context, String errorMsg)
    {
        Crouton crouton = Crouton.makeText(context,
                errorMsg,
                new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(duration).build())
                        .setBackgroundColorValue(Color.RED)
                        .setHeight(LayoutParams.WRAP_CONTENT)
                        .build());
        crouton.show();
        return crouton;
    }

    public static Crouton error(final Activity context, String errorMsg) {
		Crouton crouton = Crouton.makeText(context,
						errorMsg,
						new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_MEDIUM).build())
						.setBackgroundColorValue(Color.RED)
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		crouton.show();
		return crouton;
	}

    public static Crouton error(final Activity context, int errorMsg) {
		Crouton crouton = Crouton.makeText(context,
						errorMsg,
						new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_SHORT).build())
						.setBackgroundColorValue(Color.RED)
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		crouton.show();
		return crouton;
	}

	public static Crouton info(final Activity context, int msg) {
		Crouton crouton = Crouton.makeText(context,
						msg,
						new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_SHORT).build())
						.setBackgroundColorValue(context.getResources().getColor(R.color.blue))
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		crouton.show();
		return crouton;
	}

	public static Crouton info(final Activity context, String msg) {
		Crouton crouton = Crouton.makeText(context,
						msg,
						new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_MEDIUM).build())
						.setBackgroundColorValue(context.getResources().getColor(R.color.blue))
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		crouton.show();
		return crouton;
	}

	public static Crouton errorMsgInfinite(Activity context, String errorMsg) {
		Crouton crouton = Crouton.makeText(context,
				errorMsg,
				new Style.Builder().setConfiguration(new Configuration.Builder().setDuration(DURATION_INFINITE).build())
						.setBackgroundColorValue(Color.RED)
						.setHeight(LayoutParams.WRAP_CONTENT)
						.build());
		crouton.show();
		return crouton;
	}


	
}