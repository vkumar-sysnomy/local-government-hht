package com.farthestgate.android.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.CameraImageHelper;
import com.farthestgate.android.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForeignVehicleDialog extends DialogFragment {

    public interface onVehicleCountryListener {
        public void onCountrySelected(String code);
    }

    private onVehicleCountryListener mListener;
    private EditText countryFilter;

    public static ForeignVehicleDialog newInstance() {
        ForeignVehicleDialog fragment = new ForeignVehicleDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ForeignVehicleDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_foreign_dialog, null);
        countryFilter = (EditText) v.findViewById(R.id.countryFilter);
        ListView listCountries = (ListView) v.findViewById(R.id.lstCountries);
        final ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, loadCoutries());
        listCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onCountrySelected(countryAdapter.getItem(position));
                dismiss();
            }
        });
        listCountries.setAdapter(countryAdapter);

        countryFilter.addTextChangedListener(new
                                                     TextWatcher() {

                                                         @Override
                                                         public void onTextChanged (CharSequence cs,int arg1, int arg2, int arg3){
                                                             // When user changed the Text
                                                             countryAdapter.getFilter().filter(cs);
                                                         }

                                                         @Override
                                                         public void beforeTextChanged (CharSequence arg0,int arg1, int arg2,
                                                                                        int arg3){
                                                             // TODO Auto-generated method stub

                                                         }

                                                         @Override
                                                         public void afterTextChanged (Editable arg0){
                                                             // TODO Auto-generated method stub
                                                         }
                                                     }

        );


        builder.setView(v);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onVehicleCountryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private List<String> loadCoutries(){
        List<String> list = new ArrayList<>();
        CameraImageHelper cameraImageHelper = new CameraImageHelper();
        String countriesStr = cameraImageHelper.readFile(AppConstant.CONFIG_FOLDER, "countries.json");

        try {
            JSONArray countriesArray = new JSONObject(countriesStr).getJSONArray("countries");
            Type type = new TypeToken<List<String>>(){}.getType();

            list = new Gson().fromJson(countriesArray.toString(), type );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }


}
