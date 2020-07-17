package com.farthestgate.android.ui.dialogs.vehicle_logging;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.farthestgate.android.R;
import com.farthestgate.android.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;


public class MakesAdapter extends ArrayAdapter<MakeRowItem> implements Filterable
{

    public interface MakeModelAdapterListener
    {
        void onSelect(Integer  makeID, MakeRowItem.ITEM_MODES mode, int position, Boolean isUserFavourite);
    }


    public class MakeModelLineHolder
    {
        MakeRowItem rowItem;
        TextView manufModelName;
        Boolean isUserFavourite;
        Integer _id;
        MakeRowItem.ITEM_MODES itemMode;
    }

    MakeModelLineHolder holder = null;

    private Context context;
    private Typeface listingFont;
    private List<MakeRowItem> storedInfoList;



    public MakesAdapter(Context context, int resourceId, List<MakeRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
        initCEOIconFont();
        if (storedInfoList == null)
            storedInfoList = new ArrayList<MakeRowItem>(items);


    }

    public View getView(int position, View convertView, ViewGroup parent) {

        MakeRowItem rowItem = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // get references from the view
            convertView             = mInflater.inflate(R.layout.make_model_row_item, null);
            holder                  = new MakeModelLineHolder();

            holder.manufModelName   = (TextView) convertView.findViewById(R.id.drawerListTaxonomyText);

        } else {
            holder = (MakeModelLineHolder) convertView.getTag();
        }
        holder.rowItem  = rowItem;
        holder.itemMode = rowItem.getMODE();
        holder._id      = rowItem.id;
        if (rowItem.getIsUserPreferredManufacturer() != null)
            holder.isUserFavourite = rowItem.getIsUserPreferredManufacturer();
        else
            holder.isUserFavourite = false;

        RelativeLayout mainLayout   = (RelativeLayout) convertView.findViewById(R.id.mainLayout);

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MakesAdapter.this.onClick(((View) view.getParent()), getContext());
            }
        });
        convertView.setId(position);
        convertView.setTag(holder);
        // set values

        holder.manufModelName.setText(rowItem.name);

    //    String iconString = rowItem.slug;

      //  if (rowItem.level > 0)
       //     iconString = rowItem.root_slug;


        mainLayout.setBackgroundColor(Color.TRANSPARENT);
        holder.manufModelName.setVisibility(View.VISIBLE);

        if (holder.itemMode != null)
            switch (holder.itemMode) {

                case EXPAND_VIEW: {
        //           setIcon(holder.manufIcon, iconString);
                    break;
                }
                case SINGLE_VIEW: {
           //         setIcon(holder.manufIcon, iconString);
                    break;
                }

                case HEADER_NOT_PROVIDE: {
                    mainLayout.setBackgroundColor(Color.BLACK);
                    holder.manufModelName.setText(R.string.prefs_list_nosupport_header);
                    break;
                }
                case HEADER_PROVIDE: {
                    mainLayout.setBackgroundColor(Color.BLACK);
                    holder.manufModelName.setText(R.string.prefs_list_support_header);
                    break;
                }

            }
        return convertView;
    }


    public static void onClick(View parent, Context ctx) {

        MakeModelLineHolder hd = (MakeModelLineHolder) parent.getTag();
            if (hd.itemMode != null) {
                switch (hd.itemMode) {

                    case EXPAND_VIEW: {
                        MakeModelAdapterListener detailView = (MakeModelAdapterListener) ctx;
                        detailView.onSelect(hd.rowItem.id, hd.itemMode, parent.getId(), hd.rowItem.getIsUserPreferredManufacturer());
                        break;
                    }
                    case SINGLE_VIEW: {
                        break;
                    }
                    case HEADER_NOT_PROVIDE: {
                        break;
                    }
                    case HEADER_PROVIDE: {
                        break;
                    }
                }
            }
        }


    @Override
    public android.widget.Filter getFilter() {

        android.widget.Filter filter = new android.widget.Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                List<MakeRowItem> resList = new ArrayList<MakeRowItem>();
                List<MakeRowItem> temp = storedInfoList;
                Integer count = temp.size();
                String searchParameter = charSequence.toString().toLowerCase();

                if ((charSequence != null) || (charSequence.length() != 0)) {
                    for (MakeRowItem item : temp) {
                        if (item.name.toLowerCase().startsWith(searchParameter))
                            resList.add(item);
                    }
                } else {
                    resList = temp;
                }
                results.values = resList;
                results.count = resList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                clear();
                addAll(((List<MakeRowItem>) filterResults.values));
                notifyDataSetChanged();
            }
        };

        return filter;
    }

    private void initCEOIconFont() {
        listingFont = Typeface.createFromAsset(context.getAssets(), "fonts/CEO_iconFont.ttf");
    }

    private void setIcon(TextView view, String manufacturer) {
        try {
            view.setTypeface(listingFont);
            manufacturer = manufacturer.replace("-", " ");
            String iconCharacter = FontUtils.TAXONOMY_FONT_MAPPING.get(manufacturer);
            view.setText(iconCharacter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}