package com.ngstudio.friendstep.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.ngstudio.friendstep.R;

import org.jetbrains.annotations.NotNull;

public class SelectableDialog extends AlertDialogBase {

	private final ListView listView;

	public SelectableDialog(Context context, String[] options) {
		super(context);

		listView = new ListView(getContext(), null, android.R.attr.listViewStyle);

		listView.setId(android.R.id.list);
		listView.setAdapter(new ArrayAdapter<>(getContext(), R.layout.dialog_selectable_list_item, options));
	}


	@NotNull
	public ListView getListView() {
		return listView;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCustomView(listView);
	}


	public void setOnListItemClickListener(ListView.OnItemClickListener listItemClickListener) {
		listView.setOnItemClickListener(listItemClickListener);
	}

}
