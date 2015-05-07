package com.ngstudio.friendstep.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private static final MenuItem[] sideMenuItems = { new MenuItem(R.drawable.drawable_item_menu_map, R.string.text_item_menu_map),
            new MenuItem(R.drawable.drawable_item_menu_contacts, R.string.text_item_menu_contacts),
            new MenuItem(R.drawable.drawable_item_requests, R.string.text_item_menu_requests),
            new MenuItem(R.drawable.drawable_item_menu_settings, R.string.text_item_menu_settings),
            new MenuItem(R.drawable.drawable_item_menu_about, R.string.text_item_menu_about) };

    private String name;
    private String email;

    OnItemClickListener onItemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        int Holderid;

        TextView textView;
        ImageView imageView;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvEmail;

        public ViewHolder(View itemView,int ViewType) {
            super(itemView);

            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.tvItem);
                imageView = (ImageView) itemView.findViewById(R.id.ivIcon);
                Holderid = 1;
            } else {
                tvName = (TextView) itemView.findViewById(R.id.name);
                tvEmail = (TextView) itemView.findViewById(R.id.email);
                ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
                Holderid = 0;
            }
        }
    }

    public ItemsAdapter(String name, String email, OnItemClickListener onItemClickListener) {
        this(name, email);
        this.onItemClickListener = onItemClickListener;
    }

    public ItemsAdapter(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu,parent,false); //Inflating the layout

            final ViewHolder viewHolderItem = new ViewHolder(view,viewType); //Creating ViewHolder and passing the object of type view
            viewHolderItem.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClickListener != null)
                        onItemClickListener.onItemClick(viewHolderItem.getAdapterPosition() - 1);
                }
            });
            return viewHolderItem;

        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_drawer,parent,false);
            ViewHolder vhHeader = new ViewHolder(v,viewType);
            return vhHeader;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(ItemsAdapter.ViewHolder holder, int position) {

        if (holder.Holderid == 1) {
            holder.textView.setText(sideMenuItems[position - 1].textResourceId);
            holder.imageView.setImageResource(sideMenuItems[position - 1].iconId);
        } else if(holder.Holderid == 0){
            WhereAreYouApplication.getInstance()
                    .getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile()),holder.ivAvatar);
            holder.tvName.setText(name);
            holder.tvEmail.setText(email);
        }
    }

    @Override
    public int getItemCount() {
        return sideMenuItems.length+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }


    public MenuItem getItem(int position) {
        return sideMenuItems[position];
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

     public interface OnItemClickListener {
        public void onItemClick(int position);
     }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}
