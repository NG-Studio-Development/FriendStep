package com.ngstudio.friendstep.ui.widgets;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ActionBarHolder {

    public static final int STATE_CONTACT_PROFILE = -1;

    private View actionBarView;

    AspectImageButton actionBarIcon, refresh, search, nearby, delete, edit;
    SearchView enterTextField;
    TextView title;
    RelativeLayout actionbarHolder;


    public View initHolder(@NotNull Context context) {
        actionBarView = LayoutInflater.from(context).inflate(R.layout.actionbar_main,null);
        findChildren(actionBarView);
        return actionBarView;
    }

    private void findChildren(@NotNull View view) {
        this.actionBarIcon = (AspectImageButton) view.findViewById(R.id.ivActionbarIcon);
        this.refresh = (AspectImageButton) view.findViewById(R.id.ivRefresh);
        this.search = (AspectImageButton) view.findViewById(R.id.ivSearch);
        this.nearby = (AspectImageButton) view.findViewById(R.id.ivNearby);
        this.delete = (AspectImageButton) view.findViewById(R.id.ivDelete);
        this.edit = (AspectImageButton) view.findViewById(R.id.ivEdit);
        this.title = (TextView) view.findViewById(R.id.tvActionbarTitle);
        this.actionbarHolder = (RelativeLayout) view.findViewById(R.id.rlActionbarHolder);
        this.enterTextField = (SearchView) view.findViewById(R.id.etEnterText);
    }


    public void setOnQueryTextListener(SearchView.OnQueryTextListener listener) {
        enterTextField.setOnQueryTextListener(listener);
    }

    public void setActionBarIconClickListener(View.OnClickListener listener) {
        this.actionBarIcon.setOnClickListener(listener);
    }

    public void setMenuItemClickListener(int id, View.OnClickListener listener) {
        actionBarView.findViewById(id).setOnClickListener(listener);
    }

    public void setBackgroundColor(int color) {
        actionbarHolder.setBackgroundColor(color);
    }

    public void setActionBarIcon(int icon) {
        this.actionBarIcon.setImageResource(icon);
    }

    public void setTitle(String titleText) {
        this.title.setText(titleText);
    }

    public void setTitle(int resId) {
        this.title.setText(resId);
    }

    public boolean expandSearchField(View controlView) {
        if(enterTextField.getVisibility() == View.VISIBLE)
            return false;
        if(controlView != null)
            controlView.setVisibility(View.GONE);

        enterTextField.setVisibility(View.VISIBLE);

        WhereAreYouApplication.softInputMethodStateManage(enterTextField, true);
        return true;
    }

    public boolean collapseSearchField(View controlView) {
        if(enterTextField.getVisibility() == View.GONE)
            return false;

        WhereAreYouApplication.softInputMethodStateManage(enterTextField, false);

        enterTextField.setVisibility(View.GONE);
        if(controlView != null)
            controlView.setVisibility(View.VISIBLE);
        return true;
    }

    public void setSearchField(int drawable, @Nullable View.OnClickListener listener) {
        enterTextField.setIcon(drawable, listener);
    }

    public void enterState(int id) {
        enterTextField.setVisibility(View.GONE);
        switch (id) {
            case R.drawable.drawable_item_menu_map:
                setButtonsVisible(R.id.ivRefresh);
                setTitle(R.string.text_item_menu_map);
                break;

            case R.drawable.drawable_item_menu_contacts:
                setButtonsVisible(R.id.ivNearby, R.id.ivSearch);
                setTitle(R.string.text_item_menu_contacts);
                break;

            case R.drawable.drawable_item_menu_settings:
                setButtonsVisible();
                setTitle(R.string.text_item_menu_settings);
                break;

            case STATE_CONTACT_PROFILE:
                setButtonsVisible(R.id.ivEdit,R.id.ivDelete);
                setTitle(null);
                break;

            default:
                setButtonsVisible();
                setTitle(null);
                break;
        }
    }

    public View findViewById(int id) {
        return actionBarView.findViewById(id);
    }

    private void setButtonsVisible(Integer... id) {
        List<Integer> ids = Arrays.asList(id);
        refresh.setVisibility(ids.contains(R.id.ivRefresh) ? View.VISIBLE : View.GONE);
        search.setVisibility(ids.contains(R.id.ivSearch) ? View.VISIBLE : View.GONE);
        nearby.setVisibility(ids.contains(R.id.ivNearby) ? View.VISIBLE : View.GONE);
        delete.setVisibility(ids.contains(R.id.ivDelete) ? View.VISIBLE : View.GONE);
        edit.setVisibility(ids.contains(R.id.ivEdit) ? View.VISIBLE : View.GONE);

    }

    public void setTitleVisibility(int visibility) {
        title.setVisibility(visibility);
    }

}
