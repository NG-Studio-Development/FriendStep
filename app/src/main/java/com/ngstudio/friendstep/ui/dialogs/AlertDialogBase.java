package com.ngstudio.friendstep.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ngstudio.friendstep.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class AlertDialogBase extends Dialog {

	private final LayoutInflater inflater;

	public AlertDialogBase(Context context) {
		super(context, R.style.ThemeAppGeneric_AlertDialog);

		this.inflater = LayoutInflater.from(context);

		setCanceledOnTouchOutside(true);
	}

	@Override
	public LayoutInflater getLayoutInflater() {
		return inflater;
	}


	private TextView  title;
	private ViewGroup content;
	private ViewGroup buttonsBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayoutInflater().inflate(R.layout.dialog_alert_base, null));

		title = (TextView) findViewById(R.id.title);
		content = (ViewGroup) findViewById(R.id.content);
		buttonsBar = (ViewGroup) findViewById(R.id.buttons_bar);

		dispatchUpdate(FACILITY_TITLE);
		dispatchUpdate(FACILITY_CONTENT);
		dispatchUpdate(FACILITY_BUTTONS);
	}

	private static final int FACILITY_TITLE   = 0;
	private static final int FACILITY_CONTENT = 1;
	private static final int FACILITY_BUTTONS = 2;

	private static final int[][] BUTTONS_BACKGROUND_ARRAY = {
			{R.drawable.bg_dialog_alert_base_button_generic},
			{R.drawable.bg_dialog_alert_base_button_left, R.drawable.bg_dialog_alert_base_button_right},
			{R.drawable.bg_dialog_alert_base_button_left, R.drawable.bg_dialog_alert_base_button_central, R.drawable.bg_dialog_alert_base_button_right}
	};


	private void dispatchUpdate(int facility) {
		switch (facility) {
			case FACILITY_TITLE:
				if (title != null) {
					title.setText(titleText);
					title.setVisibility(android.text.TextUtils.isEmpty(titleText) ? View.GONE : View.VISIBLE);
				}
				break;

			case FACILITY_CONTENT:
				if (content != null) {
					content.removeAllViews();

					if (contentView != null && contentViewResId == 0) {
						ViewGroup.LayoutParams params = contentView.getLayoutParams();
						if (params == null) {
							params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
						}
						content.addView(contentView, params);
					} else if (contentViewResId != 0) {
						contentView = getLayoutInflater().inflate(contentViewResId, content, true);
					}
					content.setVisibility(contentView == null ? View.GONE : View.VISIBLE);
				}
				break;

			case FACILITY_BUTTONS:
				if (buttonsBar != null) {
					Collection<View> buttonsVisible = new ArrayList<>(3);
					for (DialogButtonHolder buttonHolder : buttonHolders) {
						if (buttonHolder != null)
							buttonHolder.rigButton(buttonsVisible);
					}

					int wholeButtonsBarVisibility = View.GONE;
					if (buttonsVisible.size() > 0) {
						int[] buttonBackgroundArray = BUTTONS_BACKGROUND_ARRAY[buttonsVisible.size() - 1];

						int backgroundIndex = 0;
						for (View button : buttonsVisible) {
							button.setBackgroundResource(buttonBackgroundArray[backgroundIndex++]);
						}

						wholeButtonsBarVisibility = View.VISIBLE;
					}
					buttonsBar.setVisibility(wholeButtonsBarVisibility);
				}
				break;
		}
	}


	private CharSequence titleText;

	public CharSequence getTitle() {
		return titleText;
	}

	public void setTitle(CharSequence titleText) {
		this.titleText = titleText;

		dispatchUpdate(FACILITY_TITLE);
	}

	public void setTitle(int resId) {
		this.titleText = getContext().getString(resId);


		dispatchUpdate(FACILITY_TITLE);
	}


	private View contentView;

	public View getCustomView() {
		return contentView;
	}

	public void setCustomView(View view) {
		this.contentView = view;
		this.contentViewResId = 0;

		dispatchUpdate(FACILITY_CONTENT);
	}


	private int contentViewResId;

	public int getCustomViewResId() {
		return contentViewResId;
	}

	public void setCustomView(int resId) {
		this.contentViewResId = resId;
		this.contentView = null;

		dispatchUpdate(FACILITY_CONTENT);
	}


	private static final int BUTTON_NEGATIVE = 0;
	private static final int BUTTON_NEUTRAL  = 1;
	private static final int BUTTON_POSITIVE = 2;

	private static final int[] buttonIds = {
			android.R.id.button1,
			android.R.id.button2,
			android.R.id.button3
	};

	private final class DialogButtonHolder implements View.OnClickListener {

		private final int             which;
		private final int             textResId;
		private final OnClickListener clickListener;


		public DialogButtonHolder(int which, int textResId, OnClickListener clickListener) {
			if (clickListener == null) {
				clickListener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						/* onClick method stub */
					}
				};
			}

			this.which = which;
			this.textResId = textResId;
			this.clickListener = clickListener;
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(AlertDialogBase.this, which);

			dismiss();
		}


		private Button subjectButton;

		private boolean rigButton(@NotNull Collection<View> buttonsVisible) {
			subjectButton = ((Button) findViewById(buttonIds[which]));

			if (subjectButton == null)
				return false;

			if (textResId != 0) {
				subjectButton.setOnClickListener(this);
				subjectButton.setText(textResId);

				buttonsVisible.add(subjectButton);
				return true;
			} else {
				subjectButton.setVisibility(View.GONE);
			}
			return false;
		}
	}

	private final DialogButtonHolder[] buttonHolders = {
			new DialogButtonHolder(BUTTON_POSITIVE, 0, null),
			new DialogButtonHolder(BUTTON_NEUTRAL, 0, null),
			new DialogButtonHolder(BUTTON_NEGATIVE, 0, null)
	};

	private void addButton(int which, int resId, OnClickListener clickListener) {
		buttonHolders[which] = new DialogButtonHolder(which, resId, clickListener);

		dispatchUpdate(FACILITY_BUTTONS);
	}


	public void addPositiveButton(int textResId, OnClickListener clickListener) {
		addButton(BUTTON_POSITIVE, textResId, clickListener);
	}

	public void addNeutralButton(int textResId, OnClickListener clickListener) {
		addButton(BUTTON_NEUTRAL, textResId, clickListener);
	}

	public void addNegativeButton(int textResId, OnClickListener clickListener) {
		addButton(BUTTON_NEGATIVE, textResId, clickListener);
	}

}
