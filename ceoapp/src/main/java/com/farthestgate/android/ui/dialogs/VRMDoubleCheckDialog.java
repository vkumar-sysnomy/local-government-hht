package com.farthestgate.android.ui.dialogs;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.farthestgate.android.R;
import com.farthestgate.android.utils.CroutonUtils;

/**
 * Created by Hanson Aboagye 07/2014
 */
public class VRMDoubleCheckDialog extends DialogFragment
{
    EditText registrationMark;
    String currentVRM;

    public interface OnVRMDoubleConfirmed
    {
        void OnVRMConfirmed(String sameReg);
        void OnVRMChanged(String newReg);
    }


    OnVRMDoubleConfirmed OnVRMDoubleConfirmedListener;

    public static VRMDoubleCheckDialog newInstance(String newVRM) {
        VRMDoubleCheckDialog fragment = new VRMDoubleCheckDialog();
        Bundle args = new Bundle();
        args.putString("newReg", newVRM);
        fragment.setArguments(args);
        return fragment;
    }
    public VRMDoubleCheckDialog() {
        // Required empty public constructor
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
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_vrmcheck_dialog, null);
        registrationMark = (EditText) v.findViewById(R.id.editVrm);
        registrationMark.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        registrationMark.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        registrationMark.setText("");


        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

        builder.setTitle("Verify New VRM");

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
                    OnVRMDoubleConfirmedListener.OnVRMConfirmed(currentVRM);
                    dialog.dismiss();
                }
                else
                {
                    if (!newVRM.equals(""))
                    {
                        OnVRMDoubleConfirmedListener.OnVRMChanged(newVRM);
                        VRMDoubleCheckDialog.this.getDialog().dismiss();
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
            OnVRMDoubleConfirmedListener = (OnVRMDoubleConfirmed) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " add Interface");
        }
    }
}
