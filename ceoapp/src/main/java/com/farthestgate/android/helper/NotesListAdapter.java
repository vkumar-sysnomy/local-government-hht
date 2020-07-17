package com.farthestgate.android.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.farthestgate.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 *  Adapter to display the notes list
 */
public class NotesListAdapter extends BaseAdapter
{
    private List<String> items;
    private LayoutInflater inflater;

    public NotesListAdapter(LayoutInflater layoutInflater) {
        this.inflater = layoutInflater;
        this.items = new ArrayList<String>();
    }

    public void setItems(List<String> list) {
        items.clear();
        items.addAll(list);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.note_list_item, parent, false);
            holder.nameText = (TextView) convertView.findViewById(R.id.noteName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.nameText.setText(name);
        return convertView;
    }

    class ViewHolder {
        public TextView nameText;
    }
}