package com.ngstudio.friendstep.ui.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.ui.widgets.extension.ImeAwareExtension;
import com.ngstudio.friendstep.utils.CommonUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchView extends FrameLayout {
	public SearchView(Context context) {
		this(context, null);
	}

	public SearchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initWidget(context, attrs);
	}


	private boolean ignoreQueryTextChange;

	private void ignoreNextQueryTextChange() {
		ignoreQueryTextChange = true;
	}

	private EditTextFontView queryText;
    private IconView icon;

	public void initWidget(Context context, AttributeSet attrs) {
		LayoutInflater.from(context)
				.inflate(R.layout.widget_search_view, this, true);

		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);

		queryText = (EditTextFontView) findViewById(R.id.searchQueryText);
        icon = (IconView) findViewById(R.id.icIcon);

		queryText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				focusChangeListener.onFocusChange(v, hasFocus);
			}
		});

		queryText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				/* Nothing to do */
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				/* Nothing to do */
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!ignoreQueryTextChange) {
					queryTextListener.onQueryTextChange(s.toString());
				} else {
					ignoreQueryTextChange = false;
				}
			}
		});

		queryText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (v != null && v.length() > 0 && (actionId == 0 || actionId == v.getImeOptions()
						|| event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					//noinspection ConstantConditions
					queryTextListener.onQueryTextSubmit(v.getText().toString());
					inputFieldFocusClear();
					return true;
				}
				return false;
			}
		});

		queryText.setOnPreImeEventListener(new ImeAwareExtension.OnPreImeEventListener() {
			@Override
			public boolean onPreImeEvent(int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					inputFieldFocusClear();
				}
				return false;
			}
		});
	}

    public void setIcon(int drawable, @Nullable OnClickListener listener) {
        icon.setBackgroundResource(drawable);
        icon.setOnClickListener(listener);
    }

    private void inputFieldFocusRequest() {
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		clearFocus();

		WhereAreYouApplication.softInputMethodStateManage(queryText, true);
		queryText.requestFocus();
	}

	private void inputFieldFocusClear() {
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		requestFocus();

        WhereAreYouApplication.softInputMethodStateManage(queryText, false);
		queryText.clearFocus();
	}


	@Override
	protected void onVisibilityChanged(@NotNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);

		if (changedView != this)
			return;

		if (visibility == VISIBLE) {
			inputFieldFocusRequest();
		} else if (queryText != null) {
			inputFieldFocusClear();
			queryText.setText(null);
		}
	}

	public String getQueryText() {
		return CommonUtils.getText(queryText);
	}


	private static final OnFocusChangeListener FOCUS_CHANGE_LISTENER_STUB = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			/* onFocusChange method stub */
		}
	};

	private OnFocusChangeListener focusChangeListener = FOCUS_CHANGE_LISTENER_STUB;

	public void setOnQueryTextFocusChangeListener(OnFocusChangeListener listener) {
		if (listener == null)
			listener = FOCUS_CHANGE_LISTENER_STUB;
		focusChangeListener = listener;
	}

	private static final OnQueryTextListener QUERY_TEXT_LISTENER_STUB = new OnQueryTextListener() {
		@Override
		public void onQueryTextSubmit(String query) {
			/* onQueryTextSubmit method stub */
		}

		@Override
		public void onQueryTextChange(String newText) {
			/* onQueryTextChange method stub */
		}
	};

	private OnQueryTextListener queryTextListener = QUERY_TEXT_LISTENER_STUB;

	public void setOnQueryTextListener(OnQueryTextListener listener) {
		if (listener == null) {
			listener = QUERY_TEXT_LISTENER_STUB;
		}

		this.queryTextListener = listener;
	}


	public interface OnQueryTextListener {
		public void onQueryTextSubmit(String query);

		public void onQueryTextChange(String newText);
	}
}
