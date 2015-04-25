package com.ngstudio.friendstep.ui.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ngstudio.friendstep.R;

import java.util.Arrays;

public class ItemsAdapter extends BaseArrayAdapter<ItemsAdapter.MenuItem> {

    private static final int ID_GAP = 0;

    private static final MenuItem[] sideMenuItems = { new MenuItem(R.drawable.drawable_item_menu_map, R.string.text_item_menu_map),
            new MenuItem(R.drawable.drawable_item_menu_contacts, R.string.text_item_menu_contacts),
            new MenuItem(R.drawable.drawable_item_requests, R.string.text_item_menu_requests),
            new MenuItem(R.drawable.drawable_item_menu_settings, R.string.text_item_menu_settings),
            //new MenuItem(ID_GAP,ID_GAP),
            new MenuItem(R.drawable.drawable_item_menu_about, R.string.text_item_menu_about) };

    private static final MenuItem[] profileItems = {new MenuItem(R.drawable.drawable_item_phone, R.string.text_item_profile_call),
            new MenuItem(R.drawable.drawable_item_message, R.string.text_item_profile_sms),
            new MenuItem(R.drawable.drawable_item_requests, R.string.text_item_profile_location)};

    private LayoutInflater inflater;
    private int item;

    private ItemsAdapter(Context context, MenuItem[] items, int item) {
        super(context, 0, Arrays.asList(items));
        this.item = item;
        inflater = LayoutInflater.from(context);
    }

    public static ItemsAdapter getSideMenuAdapter(Context context) {
        return new ItemsAdapter(context,sideMenuItems, R.layout.item_menu);
    }

    public static ItemsAdapter getProfileItemsAdapter(Context context) {
        return new ItemsAdapter(context,profileItems, R.layout.item_menu);
    }

    public ItemsAdapter(Context context, int item) {
        super(context);
        this.item = item;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if(convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(item,null);

            holder.icon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.text = (TextView) convertView.findViewById(R.id.tvItem);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        MenuItem item = getItem(position);

        if(item.iconId != ID_GAP) {
            holder.icon.setImageResource(item.iconId);
            if(item.textResourceId != 0)
                holder.text.setText(item.textResourceId);
            else
                holder.text.setText(item.text);

        } else {
            holder.icon.setImageBitmap(null);
            holder.text.setText(null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,0,1);
            convertView.setLayoutParams(params);
        }

        return convertView;
    }

    public static final class MenuItem {

        private int iconId;
        private int textResourceId;
        private String text;

        private MenuItem(int iconId, int textResourceId) {
            this.iconId = iconId;
            this.textResourceId = textResourceId;
        }

        public MenuItem(int iconId, String text) {
            this.text = text;
            this.iconId = iconId;
        }

        public int getIconId() {
            return iconId;
        }
    }


    private static class Holder {
        ImageView icon;
        TextView text;
    }
}
