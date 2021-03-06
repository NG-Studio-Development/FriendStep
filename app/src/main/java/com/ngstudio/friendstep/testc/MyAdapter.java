package com.ngstudio.friendstep.testc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.adapters.BaseArrayAdapter;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Николай on 06.08.2014.
 */
public class MyAdapter extends BaseArrayAdapter<String> implements StickyListHeadersAdapter {

    private String[] countries;
    private LayoutInflater inflater;

    public MyAdapter(Context context, int style, List<String> list) {
        super(context, style, list);
        inflater = LayoutInflater.from(context);
        countries = new String[]{"as","ds"};
    }

    /*@Override
    public int getCount() {
        return countries.length;
    }*/

    /*@Override
    public String getItem(int position) {
        return countries[position];
    }*/

    /*@Override
    public long getItemId(int position) {
        return position;
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.test_list_item_layout, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(countries[position]);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        Log.d("tr", "trrrrr");
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
        //holder.text.setText(headerText);
        holder.text.setText("HEADER");
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return countries[position].subSequence(0, 1).charAt(0);
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }

}
