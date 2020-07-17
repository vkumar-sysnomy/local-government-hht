package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.VehicleManufacturer;
import com.farthestgate.android.model.database.VehicleMakeTable;

import java.util.ArrayList;
import java.util.List;

/**
 *  Created by Hanson Aboagye 04/2014
 */
public class MakesSelectFragment extends Fragment {


    private EditText editFilter;

    private Context ctx;
    private MakesAdapter makesAdapter;

    private static ListView lstMakes;
    private ProgressBar progressBarWait;

    public static List<MakeRowItem> manufacturerList;


    public static MakesSelectFragment newInstance(Context context) {
        MakesSelectFragment fragment = new MakesSelectFragment();
        fragment.ctx = context;
        return fragment;
    }
    public MakesSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_manufacturer, container, false);

        lstMakes            = (ListView) v.findViewById(R.id.list_select_manufacturer);
        progressBarWait     = (ProgressBar) v.findViewById(R.id.pbManufacturer);
        editFilter          = (EditText) v.findViewById(R.id.searchFilter);

        editFilter.addTextChangedListener(filterWatcher);
        progressBarWait.setVisibility(View.INVISIBLE);
        manufacturerList = new ArrayList<MakeRowItem>();
        lstMakes.setOnItemClickListener(itemClickListener);

        // Get Vehicles from API
        new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBarWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    List<VehicleMakeTable> makes = DBHelper.getVehicleMakes();
                    for (VehicleMakeTable make: makes)
                    {
                        VehicleManufacturer manufacturer = new VehicleManufacturer();
                        manufacturer.id = make.vehicleMakeID;
                        manufacturer.name = make.vehicleMakeName;
                        manufacturerList.add(new MakeRowItem(manufacturer));
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                makesAdapter = new MakesAdapter(ctx,0,manufacturerList);
                lstMakes.setAdapter(makesAdapter);
                progressBarWait.setVisibility(View.INVISIBLE);
            }

        }.execute(null, null, null);

        return v;
    }


    ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ((MakesAdapter) adapterView.getAdapter()).onClick(view, getActivity());

        }
    };

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
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
            makesAdapter.getFilter().filter((editFilter.getText()));
            lstMakes.invalidateViews();
        }
    };


}
