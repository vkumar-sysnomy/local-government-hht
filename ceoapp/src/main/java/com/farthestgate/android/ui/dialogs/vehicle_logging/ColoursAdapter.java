package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.utils.Colours;

import java.util.ArrayList;
import java.util.List;


public class ColoursAdapter extends ArrayAdapter<ColourRowItem> implements Filterable
{

    public interface ColourAdapterListener
    {
        void onSelectColour(String colourName, int colourValue);
    }


    public class ColourLineHolder
    {
        ColourRowItem rowItem;
        TextView colourNameText;
        RelativeLayout colourSquare;
        int colourVal;
    }

    ColourLineHolder holder = null;

    private Context context;
    private Typeface listingFont;
    private List<ColourRowItem> storedInfoList;




    public ColoursAdapter(Context context, int resourceId, List<ColourRowItem> items) {
        super(context, resourceId, items);
        this.context = context;

        if (storedInfoList == null)
            storedInfoList = new ArrayList<ColourRowItem>(items);

    }

    public View getView(int position, View convertView, ViewGroup parent)
    {

        ColourRowItem rowItem = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
        {
            // get references from the view
            convertView = mInflater.inflate(R.layout.colour_list_item, null);
            holder = new ColourLineHolder();
            holder.colourSquare = (RelativeLayout) convertView.findViewById(R.id.mainItemLayout);
            holder.colourNameText = (TextView) convertView.findViewById(R.id.colourListText);

        }
        else
        {
            holder = (ColourLineHolder) convertView.getTag();
        }
        holder.rowItem = rowItem;
        holder.colourVal = rowItem.getColourValue();
        holder.colourSquare.setBackgroundColor(Colours.colourNames.get(rowItem.getColourName()));

        holder.colourNameText.setText(rowItem.getColourName());

        convertView.setId(position);
        convertView.setTag(holder);

        return convertView;
    }


    public static void onClick(View parent, Context ctx) {
            ColourLineHolder hd = (ColourLineHolder) parent.getTag();
            ColourAdapterListener detailView = (ColourAdapterListener) ctx;
            detailView.onSelectColour(hd.rowItem.getColourName(), hd.rowItem.getColourValue());
        }

}