/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farthestgate.android.ui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.*;
import android.view.View;

import org.joda.time.DateTime;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.WarningNotice;
import com.farthestgate.android.utils.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    /**
     * Returns whether the SDK is KitKat or later
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }



    public static long getTimeNow() {
        //return SystemClock.elapsedRealtime();
        return DateTime.now().getMillis();
    }

    /**
     * Calculate the amount by which the radius of a CircleTimerView should be offset by the any
     * of the extra painted objects.
     */
    public static float calculateRadiusOffset(
            float strokeSize, float dotStrokeSize, float markerStrokeSize) {
        return Math.max(strokeSize, Math.max(dotStrokeSize, markerStrokeSize));
    }

    /**
     * Uses {@link Utils#calculateRadiusOffset(float, float, float)} after fetching the values
     * from the resources just as {@link CircleTimerView#init(android.content.Context)} does.
     */
    public static float calculateRadiusOffset(Resources resources) {
        if (resources != null) {
            float strokeSize = resources.getDimension(R.dimen.circletimer_circle_size);
            float dotStrokeSize = resources.getDimension(R.dimen.circletimer_dot_size);
            float markerStrokeSize = resources.getDimension(R.dimen.circletimer_marker_size);
            return calculateRadiusOffset(strokeSize, dotStrokeSize, markerStrokeSize);
        } else {
            return 0f;
        }
    }

    /**  The pressed color used throughout the app. If this method is changed, it will not have
     *   any effect on the button press states, and those must be changed separately.
    **/
    public static int getPressedColorId() {
        return R.color.clock_red;
    }


    /***
     * @param amPmFontSize - size of am/pm label (label removed is size is 0).
     * @return format string for 12 hours mode time
     */
    public static CharSequence get12ModeFormat(int amPmFontSize) {
        String skeleton = "hma";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        // Remove the am/pm
        if (amPmFontSize <= 0) {
            pattern.replaceAll("a", "").trim();
        }
        // Replace spaces with "Hair Space"
        pattern = pattern.replaceAll(" ", "\u200A");
        // Build a spannable so that the am/pm will be formatted
        int amPmPos = pattern.indexOf('a');
        if (amPmPos == -1) {
            return pattern;
        }
        Spannable sp = new SpannableString(pattern);
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new AbsoluteSizeSpan(amPmFontSize), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new TypefaceSpan("sans-serif-condensed"), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        return sp;
    }

    public static CharSequence get24ModeFormat() {
        String skeleton = "Hm";
        return DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
    }

    public static File getFile(String path, String fileName) {
        File mediaStorageDir = getDirectory(path);
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private static File getDirectory(String path) {
        File mediaStorageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String rootDir = Environment.getExternalStorageDirectory().toString();
            mediaStorageDir = new File(rootDir + File.separator + path);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    android.util.Log.d("TAG", "failed to create directory");
                }
            }
        }
        return mediaStorageDir;
    }

    public static File[] getListOfFile(){
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstant.PHOTO_FOLDER);
        File[] fileList = file.listFiles();
        return fileList;
    }

    public static List<File> getFiles(String name){

        List<File> list = new ArrayList<File>();
        try {
            File[] fileArray = getListOfFile();
            String fileName = null;
            for (File file : fileArray) {
                if (file.isFile()) {
                    fileName = file.getName();
                    if (fileName.contains(name)) {
                        list.add(file);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;

    }

    public static void CopyDirectory (File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            File targetDir = new File(targetLocation.getAbsolutePath() + "/" + sourceLocation.getName());
            if (!targetDir.exists())
                targetDir.mkdir();

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                CopyDirectory(new File(sourceLocation, children[i]),
                        new File(targetDir, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


    public static void showExitDialog(Context context, String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showDialog(Context context, String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showDialog(Context context, String message, String title, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setPositiveButton("OK", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static boolean eligibleForWarningNotice(PCN pcnInfo, String contraventionCode){
        //Code for HAN-56: Require the ability for instant Warning Notices
        boolean isApplicableForWarning = false;
        if(CeoApplication.InstantWarnNotice()){
            isApplicableForWarning=checkWarningNotice(pcnInfo,contraventionCode);
        }
        return isApplicableForWarning;
    }

    public static boolean checkWarningNotice(PCN pcnInfo,String contraventionCode){
        boolean isApplicableForWarning=false;
        List<WarningNotice> warningNoticeList = pcnInfo.location.streetCPZ.warningNoticeConfiguration;
        List<WarningNotice> matchedWarningNotice = new ArrayList<>();
        if(warningNoticeList.size() > 0){
            for(WarningNotice warningNotice : warningNoticeList){
                String warningContraventionCode = warningNotice.contraventionCode;
                if(warningContraventionCode.equalsIgnoreCase("All")){
                    //matchedWarningNotice = warningNotice;
                    //break;
                    matchedWarningNotice.add(warningNotice);
                }
                if(warningContraventionCode.equalsIgnoreCase(contraventionCode)){
                    //matchedWarningNotice = warningNotice;
                    //break;
                    matchedWarningNotice.add(warningNotice);
                }
            }
        }
        if(matchedWarningNotice .size()>0){
            for(WarningNotice warningNotice:matchedWarningNotice){
                String warningStartDate = warningNotice.warningStartDate;
                String warningEndDate = warningNotice.warningEndDate;
                Date wStartDate = DateUtils.getDate(warningStartDate, "dd/MM/yyyy");
                Date wEndDate = DateUtils.getDate(warningEndDate, "dd/MM/yyyy");
                isApplicableForWarning = DateUtils.isWithinRange(wStartDate, wEndDate);
                if(isApplicableForWarning)
                    break;
            }
        }
        return isApplicableForWarning;
    }


}
