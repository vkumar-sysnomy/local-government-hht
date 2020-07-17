package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.farthestgate.android.R;
import com.farthestgate.android.utils.Colours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *  Created by Hanson Aboagye 04/2014
 */
public class ColourSelectFragment extends Fragment
{


    private ListView lstColours;
    private ProgressBar progressBarAdd;


    //List<MakeRowItem> makesList
    public static ColourSelectFragment newInstance() {
        ColourSelectFragment fragment = new ColourSelectFragment();
        return fragment;
    }

    public ColourSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_colour_list, container, false);

        lstColours = (ListView) v.findViewById(R.id.list_colours);
        progressBarAdd = (ProgressBar) v.findViewById(R.id.progressBarList);
        progressBarAdd.setVisibility(View.INVISIBLE);

        ArrayList<ColourRowItem> colours = new ArrayList<ColourRowItem>();
        for (String cl : Colours.newInstance().colourNames.keySet())
        {
            colours.add(new ColourRowItem(cl));
        }

        Collections.sort(colours, new Comparator<ColourRowItem>() {
            @Override
            public int compare(ColourRowItem lhs, ColourRowItem rhs) {
                return lhs.getColourName().compareTo(rhs.getColourName());
            }
        });

        lstColours.setAdapter(new ColoursAdapter(getActivity(), R.layout.colour_list_item,colours ));
        lstColours.setOnItemClickListener(itemClickListener);
        return v;
    }



    ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ((ColoursAdapter) adapterView.getAdapter()).onClick(view, getActivity());

        }
    };

}
