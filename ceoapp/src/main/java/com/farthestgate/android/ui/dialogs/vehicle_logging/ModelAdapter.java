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


public class ModelAdapter extends ArrayAdapter<ModelRowItem> implements Filterable
{

    public interface ModelAdapterListener
    {
        void onSelectModel(String makeID, ModelRowItem.ITEM_MODES mode, int position, Boolean isUserFavourite);

    }


    public class MakeModelLineHolder
    {
        ModelRowItem rowItem;
        TextView manufModelName;

        Boolean isUserFavourite;
        Integer _id;
        ModelRowItem.ITEM_MODES itemMode;
    }

    MakeModelLineHolder holder = null;

    private Context context;
    private Typeface listingFont;
    private List<ModelRowItem> storedInfoList;



    public ModelAdapter(Context context, int resourceId, List<ModelRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
        initCEOIconFont();
        if (storedInfoList == null)
            storedInfoList = new ArrayList<ModelRowItem>(items);


    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ModelRowItem rowItem = getItem(position);
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
        holder._id      = rowItem.modelMakeID;
        if (rowItem.getIsUserPreferredModel() != null)
            holder.isUserFavourite = rowItem.getIsUserPreferredModel();
        else
            holder.isUserFavourite = false;

        RelativeLayout mainLayout   = (RelativeLayout) convertView.findViewById(R.id.mainLayout);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModelAdapter.this.onClick(((View) view.getParent()), getContext());
            }
        });

        // set values

        holder.manufModelName.setText(rowItem.modelName);

        convertView.setId(position);
        convertView.setTag(holder);


    //    String iconString = rowItem.slug;

      //  if (rowItem.level > 0)
       //     iconString = rowItem.root_slug;


        mainLayout.setBackgroundColor(Color.TRANSPARENT);
        holder.manufModelName.setVisibility(View.VISIBLE);

        if (holder.itemMode != null)
            switch (holder.itemMode) {

                case SINGLE_VIEW: {

           //         setIcon(holder.manufIcon, iconString);
                    break;
                }

                case HEADER_NOT_PROVIDE: {
                    mainLayout.setBackgroundColor(Color.BLACK);
                    holder.manufModelName.setText(R.string.prefs_list_nosupport_header);

                    break;
                }


            }
        return convertView;
    }



    public static void onClick(View parent, Context ctx) {

        MakeModelLineHolder hd = (MakeModelLineHolder) parent.getTag();
            if (hd.itemMode != null) {
                switch (hd.itemMode) {
                    case SINGLE_VIEW: {
                        ModelAdapterListener detailView = (ModelAdapterListener) ctx;
                        detailView.onSelectModel(hd.rowItem.modelName, hd.itemMode, parent.getId(), hd.rowItem.getIsUserPreferredModel());

                        break;
                    }
                    case HEADER_NOT_PROVIDE: {
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
                List<ModelRowItem> resList = new ArrayList<ModelRowItem>();
                List<ModelRowItem> temp = storedInfoList;
                Integer count = temp.size();
                String searchParameter = charSequence.toString().toLowerCase();

                if ((charSequence != null) || (charSequence.length() != 0)) {
                    for (ModelRowItem item : temp) {
                        if (item.modelName.toLowerCase().startsWith(searchParameter))
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
                addAll(((List<ModelRowItem>) filterResults.values));
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