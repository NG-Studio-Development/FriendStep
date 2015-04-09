package com.ngstudio.friendstep.ui.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.ngstudio.friendstep.ui.fragments.BaseFragment;
import com.ngstudio.friendstep.WhereAreYouApplication;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseActivity extends ActionBarActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected abstract int getFragmentContainerId();

    protected ActivityLifecycleStateTracker lifecycleStateTracker = new ActivityLifecycleStateTracker(this);


    private static Fragment instantiateFragment(@NotNull String fragmentClassName, @Nullable Bundle args) {
        try {
            Fragment fragment = (Fragment) Class.forName(fragmentClassName).newInstance();
            if(args != null)
                fragment.setArguments(args);
            return fragment;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static final int RUN_LINGER_ON_START  = 0;
    protected static final int RUN_LINGER_ON_RESUME = 1;

    private final SparseArray<Collection<Runnable>> lingerTasks = new SparseArray<>(0);

    protected boolean scheduleLingerTask(@MagicConstant(intValues = {
            RUN_LINGER_ON_START, RUN_LINGER_ON_RESUME
    }) int when, @NotNull Runnable task) {
        Collection<Runnable> lingerTasksArray = lingerTasks.get(when);

        if (lingerTasksArray == null) {
            lingerTasks.put(when, lingerTasksArray = new ArrayList<>(1));
        }
        return lingerTasksArray.add(task);
    }

    protected void postLingerTasks(@MagicConstant(intValues = {
            RUN_LINGER_ON_START, RUN_LINGER_ON_RESUME
    }) int when) {
        Collection<Runnable> lingerTasksArray = lingerTasks.get(when);

        if (lingerTasksArray != null) {
            for (Runnable lingerTask : lingerTasksArray) {
                runOnUiThread(lingerTask);
            }
            lingerTasksArray.clear();
            lingerTasks.remove(when);
        }
    }


    private class UpdateFragmentTask implements Runnable {

        private final Fragment fragment;
        private final boolean  saveInBackStack;
        private final boolean  add;

        public UpdateFragmentTask(@NotNull Fragment fragment, boolean saveInBackStack, boolean add) {
            this.fragment = fragment;
            this.saveInBackStack = saveInBackStack;
            this.add = add;
        }


        @Override
        public void run() {
            updateFragment(fragment, saveInBackStack, add);
        }
    }

    protected void updateFragment(@NotNull Fragment fragment, boolean saveInBackStack, boolean add) {
        if (!lifecycleStateTracker.areFragmentManipulatonsAllowed()) {
            scheduleLingerTask(RUN_LINGER_ON_RESUME, new UpdateFragmentTask(fragment, saveInBackStack, add));
            return;
        }

        final String fragmentTag = ((Object) fragment).getClass().getName();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (add) {
            ft.add(getFragmentContainerId(), fragment, fragmentTag);
        } else {
            ft.replace(getFragmentContainerId(), fragment, fragmentTag);
        }
        if (saveInBackStack) {
            ft.addToBackStack(fragmentTag);
        }
        ft.commit();
    }

    public void switchFragment(@NotNull Fragment fragment, boolean saveInBackStack) {
        updateFragment(fragment, saveInBackStack, false);
    }

    public void addFragment(@NotNull Fragment fragment, boolean saveInBackStack) {
        updateFragment(fragment, saveInBackStack, true);
    }

    public void switchFragment(@NotNull String fragmentClassName,@Nullable Bundle args, boolean saveInBackStack) {
        updateFragment(instantiateFragment(fragmentClassName,args), saveInBackStack, false);
    }

    public void addFragment(@NotNull String fragmentClassName, @Nullable Bundle args, boolean saveInBackStack) {
        updateFragment(instantiateFragment(fragmentClassName,args), saveInBackStack, true);
    }

    public void switchFragment(@NotNull Class<? extends BaseFragment<? extends BaseActivity>> fragmentClass,@Nullable Bundle args, boolean saveInBackStack) {
        switchFragment(fragmentClass.getName(), args, saveInBackStack);
    }

    public void addFragment(@NotNull Class<? extends BaseFragment<? extends BaseActivity>> fragmentClass,@Nullable Bundle args, boolean saveInBackStack) {
        addFragment(fragmentClass.getName(),args, saveInBackStack);
    }


    protected <T extends BaseFragment> T getFragment(@NotNull Class<T> fragmentClass) {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());

        //noinspection unchecked
        return (T) fragment;
    }


    private class PopBackStackUpToTask implements Runnable {

        private final Class<? extends BaseFragment<?>> fragmentClass;
        private final int                              flags;

        public PopBackStackUpToTask(@Nullable Class<? extends BaseFragment<?>> fragmentClass, int flags) {
            this.fragmentClass = fragmentClass;
            this.flags = flags;
        }


        @Override
        public void run() {
            popBackStackUpTo(fragmentClass, flags);
        }
    }

    public void popBackStackUpTo(@Nullable Class<? extends BaseFragment<?>> fragmentClass, int flags) {
        if (lifecycleStateTracker.isInSavedState()) {
            scheduleLingerTask(RUN_LINGER_ON_RESUME, new PopBackStackUpToTask(fragmentClass, flags));
            return;
        }

        getSupportFragmentManager().popBackStackImmediate(fragmentClass != null ? fragmentClass.getName() : null, flags);
    }

    public void popBackStackUpTo(@Nullable Class<? extends BaseFragment<?>> fragmentClass) {
        popBackStackUpTo(fragmentClass, 0);
    }


    protected boolean hasFragments(boolean attached) {
        List<Fragment> items = getSupportFragmentManager().getFragments();

        if (!attached)
            return !(items == null || items.isEmpty());

        for (Fragment f : items) {
            if (f.isAdded())
                return true;
        }
        return false;
    }

    protected boolean hasFragments() {
        return hasFragments(false);
    }


    private class ProgressDialog extends AlertDialogBase {

        public ProgressDialog(Context context) {
            super(context);
        }


        private TextView progressMessage;

        public CharSequence getText() {
            return progressMessage != null ? progressMessage.getText() : null;
        }


        private Runnable setTextLingerTask;

        public void setText(final CharSequence text) {
            if (progressMessage == null) {
                setTextLingerTask = new Runnable() {
                    @Override
                    public void run() {
                        setText(text);

                        setTextLingerTask = null;
                    }
                };
                return;
            }
            progressMessage.setText(text);
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getWindow().getAttributes().dimAmount = .35f;
            getWindow().getAttributes().flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            getWindow().getAttributes().softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

            setCustomView(R.layout.dialog_progress);

            progressMessage = (TextView) findViewById(R.id.progressMessage);

            if (setTextLingerTask != null)
                setTextLingerTask.run();
        }
    }

    private ProgressDialog mProgressDialog;


    public final void showProgressDialog(final @NotNull String progressDialogString, @Nullable DialogInterface.OnDismissListener dismissListener) {
        if (this.isFinishing()) {
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }

        mProgressDialog.setText(progressDialogString);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(dismissListener != null);
        mProgressDialog.setOnDismissListener(dismissListener);

        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    public final void showProgressDialog(int resId, @Nullable DialogInterface.OnDismissListener dismissListener) {
        showProgressDialog(getString(resId), dismissListener);
    }

    public final void showProgressDialog(String progressDialogString) {
        showProgressDialog(progressDialogString, null);
    }

    public final void showProgressDialog(int resId) {
        showProgressDialog(getString(resId), null);
    }

    public final void showProgressDialog(@Nullable DialogInterface.OnDismissListener dismissListener) {
        showProgressDialog(getString(R.string.dialog_progress_default), dismissListener);
    }

    public void showProgressDialog() {
        showProgressDialog((DialogInterface.OnDismissListener) null);
    }


    public void hideProgressDialog() {
//		if (this.isFinishing()) {
//			return;
//		}
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lifecycleStateTracker.reportStateCreated();
        Log.d(((Object) this).getClass().getSimpleName(), getResources().getConfiguration().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        lifecycleStateTracker.reportStateRestored();

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        postLingerTasks(RUN_LINGER_ON_START);
        lifecycleStateTracker.reportStateStarted();
    }

    @Override
    protected void onResume() {
        super.onResume();

        postLingerTasks(RUN_LINGER_ON_RESUME);
        lifecycleStateTracker.reportStateResumed();
        WhereAreYouApplication.getInstance().activityResumed(((Object) this).getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        WhereAreYouApplication.getInstance().activityPaused(((Object) this).getClass().getSimpleName());
        lifecycleStateTracker.reportStatePaused();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        lifecycleStateTracker.reportStateSaved();
    }

    @Override
    protected void onStop() {
        lifecycleStateTracker.reportStateStopped();
        hideProgressDialog();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleStateTracker.reportStateDestroyed();

        super.onDestroy();
    }


    public interface OnKeyDownListener {
        public boolean onKeyDown(int keyCode, KeyEvent event);
    }


    private static final OnKeyDownListener KEY_DOWN_LISTENER_STUB = new OnKeyDownListener() {
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return false;
        }
    };

    private OnKeyDownListener keyDownListener = KEY_DOWN_LISTENER_STUB;

    public void setOnKeyDownListener(@Nullable OnKeyDownListener keyDownListener) {
        if (keyDownListener == null) {
            keyDownListener = KEY_DOWN_LISTENER_STUB;
        }
        this.keyDownListener = keyDownListener;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyDownListener.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}