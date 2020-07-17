package com.imense.anpr.launchPT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.farthestgate.android.R;
import com.farthestgate.android.ui.pcn.PCNStartActivity;
import com.farthestgate.android.utils.CroutonUtils;

/**
 * Created by Suraj Gopal on 10/4/2017.
 */
public class AnprVrmDialog extends DialogFragment {

    private EditText registrationMark;
    private String currentVRM;
    private OnAnprVrmListener onAnprVrmListener;

    public static AnprVrmDialog newInstance(String newVRM) {
        AnprVrmDialog fragment = new AnprVrmDialog();
        Bundle args = new Bundle();
        args.putString("newReg", newVRM);
        fragment.setArguments(args);
        return fragment;
    }

    public AnprVrmDialog() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onAnprVrmListener = (OnAnprVrmListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAnprVrmListener.");
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            currentVRM = args.getString("newReg");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_vrmcheck_dialog, null);
        registrationMark = (EditText) v.findViewById(R.id.editVrm);

        registrationMark.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        registrationMark.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        registrationMark.setText(currentVRM);
        registrationMark.setEnabled(false);
        registrationMark.setClickable(false);


        builder.setView(v)
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        builder.setView(v)
                .setPositiveButton("PCN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();
        final Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(boldTypeface);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackground(getResources().getDrawable(R.drawable.round_button));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(boldTypeface);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackground(getResources().getDrawable(R.drawable.round_button));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vrmText =  registrationMark.getText().toString().trim();
                if(vrmText.isEmpty()){
                    CroutonUtils.error(getActivity(), "VRM can not be empty");
                    return;
                }
                Intent intent = new Intent(getActivity(), PCNStartActivity.class);
                intent.putExtra("anprVrm", vrmText);
                startActivity(intent);
                dialog.dismiss();
                //JK: getActivity().finish();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText().toString().equalsIgnoreCase("Edit")) {
                    registrationMark.setEnabled(true);
                    registrationMark.setClickable(true);

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Lookup");

                }else {
                    //call singleview interface..
                    String vrmText =  registrationMark.getText().toString().trim();
                    if(vrmText.isEmpty()){
                        CroutonUtils.error(getActivity(), "VRM can not be empty");
                        return;
                    }
//                   checkAutomatedVRMLookup(vrmText);
                    if(onAnprVrmListener != null){
                        onAnprVrmListener.onAnprClicked(vrmText);
                        dialog.dismiss();
                    }
                }
            }
        });

        return dialog;
    }

    public interface OnAnprVrmListener{
        void onAnprClicked(String vrmText);
    }

}