package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.VehicleModel;
import com.farthestgate.android.model.database.VehicleModelTable;

import java.util.ArrayList;
import java.util.List;

/**
 *  Created by Hanson Aboagye 04/2014
 */
public class ModelsSelectFragment extends Fragment
{
    private EditText editFilter;

    private static ModelAdapter modelAdapter;
    private static ProgressBar progressBarSelect;
    private static ListView lstModels;
    public static List<ModelRowItem> modelList;

    /**
     * @return
     */
    public static ModelsSelectFragment newInstance() {
        ModelsSelectFragment fragment = new ModelsSelectFragment();
        return fragment;
    }

    public ModelsSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_model_select, container, false);

        lstModels           = (ListView) v.findViewById(R.id.list_select_models);
        editFilter          = (EditText) v.findViewById(R.id.editFilter);
        progressBarSelect   = (ProgressBar) v.findViewById(R.id.progressBarSelect);

        modelList           = new ArrayList<ModelRowItem>();
        modelAdapter        = new ModelAdapter(getActivity(),0,modelList);

        lstModels.setAdapter(modelAdapter);
        lstModels.setOnItemClickListener(itemClickListener);
        editFilter.addTextChangedListener(filterWatcher);

        editFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
        });


        return v;
    }

    ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ((ModelAdapter) adapterView.getAdapter()).onClick(view, getActivity());

        }
    };


    public void  loadModels(final Integer manufacturer,final Context ctx, final Boolean cached) {

        // Get Vehicles from API
        new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBarSelect.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object[] params)
            {

                try
                {
                    modelList = new ArrayList<ModelRowItem>();
                    List<VehicleModelTable> models = DBHelper.getModels(manufacturer);
                    for (VehicleModelTable model: models)
                    {
                        VehicleModel vehicleModel = new VehicleModel();
                        vehicleModel.modelName = model.modelName;
                        vehicleModel.modelMakeID = model.modelMakeID;
                        modelList.add(new ModelRowItem(vehicleModel));

                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                modelAdapter.notifyDataSetChanged();
                progressBarSelect.setVisibility(View.INVISIBLE);
                reloadAdapter(ctx);
            }

        }.execute(null, null, null);
    }


    public static void reloadAdapter(Context context)
    {
        modelAdapter = new ModelAdapter(context,0,modelList);
        lstModels.setAdapter(modelAdapter);

    }

    TextWatcher filterWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            modelAdapter.getFilter().filter((editFilter.getText()));
            lstModels.invalidateViews();
        }
    };



    public static void clearList()
    {
        if (modelList != null)
            modelList.clear();
    }
}
