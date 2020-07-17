package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.model.Contravention;
import com.farthestgate.android.model.ContraventionSuffix;
import com.farthestgate.android.model.EnforcementPattern;
import com.farthestgate.android.model.FootwaySuffix;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.Suffix;
import com.farthestgate.android.ui.components.CustomSpinner;
import com.farthestgate.android.ui.pcn.VisualPCNListActivity;
import com.farthestgate.android.utils.CroutonUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Jitendra on 1/17/2017.
 */
public class ContraventionChangeDialog extends DialogFragment {
    private static Activity activityContext;
    private RadioGroup      lstSuffixes;
    private CustomSpinner   customSpinner;
    private TextView        contDescription;
    private TextView        clearSuffix;
    private Suffix suffix;
    private List<ContraventionSuffix>        contraventionSuffixes;
    private List<FootwaySuffix>        footWaySuffixes;
    PCN pcnInfo;
    //JK:Change for multiple instance of timers
    boolean multiLogApplicable;
    private List<Contravention> contraventionDataList;

    public interface OnContraventionChanged{
        void ContraventionInfoChanged(Contravention contravention, boolean multiLogApplicable);
    }
    OnContraventionChanged mContraventionChangedListener;
    public static ContraventionChangeDialog NewInstance(PCN pcnInfo,boolean multiLogApplicable)
    {
        ContraventionChangeDialog cDialog = new ContraventionChangeDialog();
        if (pcnInfo != null) {
            cDialog.pcnInfo = pcnInfo;
            cDialog.multiLogApplicable = multiLogApplicable;
            cDialog.contraventionDataList = pcnInfo.location.streetCPZ.contraventionList;
        }
        return cDialog;
    }

    public ContraventionChangeDialog()
    {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (contraventionDataList.size()>0)
            customSpinner.startController();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_contra_change_dialog, null);

        customSpinner       = (CustomSpinner) v.findViewById(R.id.spnOffenceContraChange);
        lstSuffixes         = (RadioGroup) v.findViewById(R.id.rgSuffixesContraChange);
        contDescription     = (TextView) v.findViewById(R.id.txtContDescriptionContraChange);
        clearSuffix        = (TextView) v.findViewById(R.id.clearSuffixContraChange);

        Typeface tf = Typeface.createFromAsset(activityContext.getAssets(), "fonts/HelveticaNeue-Medium.otf");
        contDescription.setTypeface(tf);

        suffix = loadSuffixes();

        clearSuffix.setOnClickListener(clearSuffixClick);
        List<String> contraventions = new ArrayList<String>();
        for (String c : pcnInfo.location.streetCPZ.contraventions.split(","))
        {
            contraventions.add(c);
        }
        Collections.sort(contraventions);
        Collections.sort(contraventionDataList, new Comparator<Contravention>() {
            @Override
            public int compare(Contravention lhs, Contravention rhs) {
                return lhs.contraventionCode.compareTo(rhs.contraventionCode);
            }
        });
        if (contraventionDataList.size() > 0) {
            pcnInfo.contravention = contraventionDataList.get(0);
            customSpinner.setEnabled(true);
            lstSuffixes.setEnabled(true);
            contDescription.setEnabled(true);
            customSpinner.setViews(contraventions, activityContext);
            customSpinner.invalidate();
            customSpinner.setCurrentChildChangedListener(csl);
            //Load Suffixes
            contraventionSuffixes = suffix.contraventionSuffixes;
            footWaySuffixes = suffix.footwaySuffixes;

            for (ContraventionSuffix cs:contraventionSuffixes)
            {
                for (char letter : pcnInfo.contravention.codeSuffixes.toCharArray())
                {
                    String sLetter = String.valueOf(letter).toUpperCase();
                    if (cs.item.startsWith(sLetter) && !cs.item.contains("J"))
                    {
                        RadioButton rd = new RadioButton(activityContext);
                        rd.setText(cs.item);
                        lstSuffixes.addView(rd);
                    }
                }
            }
            lstSuffixes.setOnCheckedChangeListener(radClick);
        }else{
            customSpinner.removeAllViews();
            customSpinner.setEnabled(false);
            contDescription.setText("");
            contDescription.setEnabled(false);
            lstSuffixes.removeAllViews();
            lstSuffixes.setEnabled(false);
            CroutonUtils.info(activityContext, "You can not issue contraventions on this road. Please contact your supervisor");
        }
        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setTitle("Change Contravention");
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContraventionChangedListener.ContraventionInfoChanged(pcnInfo.contravention,multiLogApplicable);
                dismiss();
            }
        });
        return dialog;
    }

    private TextView.OnClickListener clearSuffixClick = new TextView.OnClickListener(){
        @Override
        public void onClick(View v) {
            lstSuffixes.clearCheck();

            pcnInfo.contravention.selectedSuffix = "";
            pcnInfo.contravention.codeSuffixDescription = "";

            if (pcnInfo.contravention.contraventionCode.equals("19") || pcnInfo.contravention.contraventionCode.equals("40"))
                pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;

            clearSuffix.setVisibility(View.GONE);
        }
    };

    private RadioGroup.OnCheckedChangeListener radClick = new RadioGroup.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId)
        {
            if (group != null)
            {
                for (View v:group.getTouchables())
                {
                    if(((RadioButton)v).isChecked())
                    {
                        clearSuffix.setVisibility(View.VISIBLE);
                        String suffix = ((TextView)v).getText().toString().toLowerCase();
                        pcnInfo.contravention.selectedSuffix = suffix.subSequence(0,1).toString();
                        pcnInfo.contravention.codeSuffixDescription = suffix.substring(2);

                        if (pcnInfo.contravention.contraventionCode.equals("19") || pcnInfo.contravention.selectedSuffix.toLowerCase().equals("s"))
                            pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;

                        if (pcnInfo.contravention.contraventionCode.equals("40") || pcnInfo.contravention.selectedSuffix.toLowerCase().equals("s"))
                            pcnInfo.contravention.contraventionType = AppConstant.CONTRAVENTION_INSTANT;
                        break;
                    }
                }
            }
        }
    };

    CustomSpinner.CustomSpinnerListener csl = new CustomSpinner.CustomSpinnerListener() {
        @Override
        public void onScrollChanged(int offenceCodeIndex) {
            try {
                clearSuffix.setVisibility(View.GONE);
                completeCSL(offenceCodeIndex);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private void completeCSL(int offenceCodeIndex){
        JSONObject verifyResult;
        try {
            verifyResult = VerifyEnforcementHours(contraventionDataList.get(offenceCodeIndex));
        }
        catch (IndexOutOfBoundsException ie) {
            verifyResult = VerifyEnforcementHours(contraventionDataList.get(0));
        }
        try {
            if (verifyResult != null) {
                if (verifyResult.has("result") && verifyResult.getString("result").equalsIgnoreCase("NOK")) {
                    AlertDialog alertDialog = new AlertDialog.Builder(activityContext).create();
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle("Contravention not allowed");
                    alertDialog.setMessage(verifyResult.getString("message"));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if (!alertDialog.isShowing()) alertDialog.show();
                }
            }

        }catch (Exception ex) {
            ex.printStackTrace();
        }
        lstSuffixes.removeAllViews();
        try
        {
            pcnInfo.contravention = contraventionDataList.get(offenceCodeIndex);
        }
        catch (IndexOutOfBoundsException ie) {
            pcnInfo.contravention = contraventionDataList.get(0);
        }
        contDescription.setText(pcnInfo.contravention.contraventionDescription);

        List<String> arrList = new ArrayList<>();
        for(ContraventionSuffix sf : contraventionSuffixes){
            arrList.add(sf.item);
        }
        if (pcnInfo.contravention.contraventionCode.contains("61") ||
                pcnInfo.contravention.contraventionCode.contains("62")) {

            arrList.clear();
            for(FootwaySuffix fs : footWaySuffixes){
                arrList.add(fs.footwayitem);
            }
        }
        for (String sf:arrList)
        {
            for (char letter : pcnInfo.contravention.codeSuffixes.toCharArray())
            {
                String sLetter = String.valueOf(letter).toUpperCase();
                if (sf.startsWith(sLetter) && !sf.contains("J"))
                {
                    RadioButton rd = new RadioButton(activityContext);
                    rd.setText(sf);
                    lstSuffixes.addView(rd);
                }
            }
        }
        pcnInfo.contravention.selectedSuffix = "";
        pcnInfo.contravention.codeSuffixDescription = "";
    }

    private JSONObject VerifyEnforcementHours(Contravention selectedContravention){
        JSONObject resultObject = new JSONObject();
        EnforcementPattern availableEnforcementPattern=null;
        int where =0;
        try {
            List<EnforcementPattern> enforcementPatterns = selectedContravention.contraventionEnforcementPattern;
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            Date currentDate = new Date();
            String dayOfTheWeek = sdf.format(currentDate);
            String currentTime="";
            if(enforcementPatterns.size()>0) {
                for (EnforcementPattern enforcementPattern : enforcementPatterns) {
                    if (enforcementPattern.enforcementDay.equalsIgnoreCase(dayOfTheWeek)) {
                        availableEnforcementPattern = enforcementPattern;
                        where = 1;
                        sdf = new SimpleDateFormat("hh:mma");
                        currentTime = sdf.format(currentDate);
                        break;
                    }
                }
            }else{
                enforcementPatterns = VisualPCNListActivity.currentStreet.streetEnforcementPattern;
                for (EnforcementPattern enforcementPattern : enforcementPatterns) {
                    if (enforcementPattern.enforcementDay.equalsIgnoreCase(dayOfTheWeek)) {
                        availableEnforcementPattern = enforcementPattern;
                        where = 2;
                        sdf = new SimpleDateFormat("HH:mm");
                        currentTime = sdf.format(currentDate);
                        break;
                    }
                }
            }
            if(availableEnforcementPattern !=null){
                boolean itIsOk = checkTime(availableEnforcementPattern.enforcementStartTime,availableEnforcementPattern.enforcementEndTime,currentTime, where);
                if(itIsOk){
                    resultObject.put("result","OK");
                    resultObject.put("message","");
                }else{
                    resultObject.put("result","NOK");
                    resultObject.put("message","The contravention " + selectedContravention.contraventionCode + " can only be enforced between " +  availableEnforcementPattern.enforcementStartTime + " and " + availableEnforcementPattern.enforcementEndTime + " on "+ availableEnforcementPattern.enforcementDay );
                }

            }else{
                resultObject.put("result","OK");
                resultObject.put("message","");
            }

        }catch (Exception ex){
            ex.printStackTrace();
            try {
                resultObject.put("result", "OK");
                resultObject.put("message", "");
            }catch (Exception e){
                e.printStackTrace();
            }
            return resultObject;
        }
        return resultObject;
    }

    private boolean checkTime(String startTime, String endTime, String time, int where) {
        boolean result;
        try {
            String timeFormat = where == 2 ? "HH:mm" : "hh:mma";
            Date timeStart = new SimpleDateFormat(timeFormat).parse(startTime);
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(timeStart);

            Date timeEnd = new SimpleDateFormat(timeFormat).parse(endTime);
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(timeEnd);
            calendarEnd.add(Calendar.DATE, 1);

            Date currentTime = new SimpleDateFormat(timeFormat).parse(time);
            Calendar calendarCurrent = Calendar.getInstance();
            calendarCurrent.setTime(currentTime);
            calendarCurrent.add(Calendar.DATE, 1);

            Date x = calendarCurrent.getTime();
            if (x.after(calendarStart.getTime()) && x.before(calendarEnd.getTime())) {
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = true;
        }
        return result;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        activityContext =activity;
        try
        {
            mContraventionChangedListener = (OnContraventionChanged) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }

    private Suffix loadSuffixes(){
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        String suffixRes = cameraImageHelper.readFile(AppConstant.CONFIG_FOLDER, "suffixes.json");

        return new Gson().fromJson(suffixRes, Suffix.class);
    }
}
