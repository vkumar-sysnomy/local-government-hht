package com.farthestgate.android.ui.dialogs;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.farthestgate.android.R;
import com.farthestgate.android.utils.CroutonUtils;

/**
 * Created by Hanson Aboagye 04/2014
 */
public class VRMCheckDialog extends DialogFragment
{
    EditText registrationMark;
    String currentVRM;
    
    public interface OnVRMConfirmed
    {
        void OnVRMConfirmed(String sameReg);
        void OnVRMChanged(String newReg);
    }


    OnVRMConfirmed OnVRMConfirmedListener;


    public VRMCheckDialog() {
        // Required empty public constructor
    }
    
    public VRMCheckDialog(String vrm)
    {
        this();
        currentVRM = vrm;
    }

    /* To restrict Space Bar in Keyboard */
    InputFilter spaceFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }

    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_vrmcheck_dialog, null);
        registrationMark = (EditText) v.findViewById(R.id.editVrm);
        registrationMark.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        registrationMark.setFilters(new InputFilter[]{new InputFilter.AllCaps(),spaceFilter});
        //No need the first confirm pop-up to open with no pre-populated data so they have to type the VRM again
        //registrationMark.setText(currentVRM);

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });
        builder.setTitle("Confirm VRM");
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String newVRM = registrationMark.getText().toString();
                if (newVRM.equals(currentVRM))
                {
                    OnVRMConfirmedListener.OnVRMConfirmed(newVRM);
                    dialog.dismiss();
                }
                else
                {
                    if (!newVRM.equals(""))
                    {
                        OnVRMConfirmedListener.OnVRMChanged(newVRM);
                        VRMCheckDialog.this.getDialog().dismiss();
                    }
                    else
                    {
                        CroutonUtils.error(CroutonUtils.DURATION_MEDIUM, getActivity(),"Please enter a VRM").show();
                    }
                }
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            OnVRMConfirmedListener = (OnVRMConfirmed) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
