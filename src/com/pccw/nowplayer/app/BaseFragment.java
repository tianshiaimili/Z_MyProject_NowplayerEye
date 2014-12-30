package com.pccw.nowplayer.app;

import com.pccw.nowplayer.app.AppLocaleAide.AppLocaleAideSupport;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


/**
 * How to convert Activity to one Fragment:
 * 
 * 1. Your Fragment extends BaseFragment
 * 2. setContentView() --> onCreateView(), inflater.inflate(R.layout.contentViewResId, container, false);
 * 3. onCreate() without setContentView --> onActivityCreated()
 * 4. this --> getActivity()
 * 
 * @author AlfredZhong
 * @version 2012-11-28
 * @version 2013-07-12
 */
public class BaseFragment extends Fragment implements AppLocaleAideSupport {

	private static final String TAG = BaseFragment.class.getSimpleName();
	private View mFragmentView;
	private boolean mFragmentHasOnStopBefore;
	private AppLocaleAide mAppLocaleAide = new AppLocaleAide(this);
	private AppProgressDialogAide mAppDialogAide;
	
	/**
	 * Holds the fragment view in this fragment instance even fragment has called
	 * {@link #onDestroyView()}, and you can retrieve the fragment view with
	 * {@link #retrieveFragmentView()} on {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * 
	 * @param view the fragment view you returns for {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 */
	public void holdFragmentView(View view) {
		mFragmentView = view;
	}
	
	/**
	 * Retrieves the fragment view after it had been hold in this fragment instance
	 * with {@link #holdFragmentView(View)}.
	 * 
	 * @return the fragment view.
	 */
	public View retrieveFragmentView() {
		return mFragmentView;
	}
	
	public View createOrRetrieveFragmentView(LayoutInflater inflater, ViewGroup container, int fragmentLayoutResId) {
    	View view = retrieveFragmentView();
    	if(view == null) {
            view = inflater.inflate(fragmentLayoutResId, container, false);
            holdFragmentView(view);
    	} 
        return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, getTag() + " onAttach()");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, getTag() + " onCreate()");
		mAppLocaleAide.maintainAppLocale(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, getTag() + " onCreateView()");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, getTag() + " onActivityCreated()");
		mAppDialogAide = new AppProgressDialogAide(getActivity());
	}
	
	protected void onRestart() {
		Log.d(TAG, getTag() + " onRestart()");
	}
	
	@Override
	public void onStart() {
		if(mFragmentHasOnStopBefore) {
			// simulate onRestart().
			onRestart();
		}
		super.onStart();
		Log.d(TAG, getTag() + " onStart()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, getTag() + " onResume()");
		mAppLocaleAide.checkAppLocale(getActivity());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, getTag() + " onPause()");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, getTag() + " onStop()");
		mFragmentHasOnStopBefore = true;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// unbind fragment view with its parent for adding fragment view again after retrieves it.
		if(mFragmentView != null) {
			ViewParent parent = mFragmentView.getParent();
			if(parent instanceof ViewGroup) {
				((ViewGroup)parent).removeView(mFragmentView);
			}
		}
		Log.d(TAG, getTag() + " onDestroyView()");
		mAppDialogAide.destroyProgressDialog();
		destroyManagedDialogs();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, getTag() + " onDestroy()");
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		// Do NOT refer to fragment view any more.
		mFragmentView = null;
		Log.d(TAG, getTag() + " onDetach()");
	}
	
	/**
	 * Call it on host activity onBackPressed().
	 * 
	 * @return True if the Fragment has handled back button event, false otherwise.
	 */
	public boolean onHostActivityBackPressed() {
		return false;
	}
	
	public boolean isFragmentRecreatedWithHoldView() {
		return mFragmentHasOnStopBefore && mFragmentView != null;
	}
	
	////////////////////// Show a dialog managed by its host activity //////////////////////
	
    private static class ManagedDialog {
        Dialog mDialog;
        Bundle mArgs;
    }
	
	private SparseArray<ManagedDialog> mManagedDialogs;
	
    private Dialog createDialog(Integer dialogId, Bundle state, Bundle args) {
        final Dialog dialog = onCreateDialog(dialogId, args);
        if (dialog == null) {
            return null;
        }
        dispatchDialogOnCreate(dialog, state);
        return dialog;
    }
    
	/*
	 * Invoke Dialog internal method dispatchOnCreate(Bundle) to make sure Dialog internal field 
	 * mcreated is set properly without requiring users to call through to super in onCreate().
	 * If don't, AlertDialog.getButton() will return null if you never call Dialog.show() before showDialog(int).
	 */
    private static void dispatchDialogOnCreate(Dialog dialog, Bundle state) {
    	// refer to https://code.google.com/p/android/issues/detail?id=6360
   	 	// Dialog.dispatchOnCreate(state) will setup views.
        try {
            java.lang.reflect.Method dispatchOnCreateMethod = Dialog.class.getDeclaredMethod("dispatchOnCreate", Bundle.class);
    		if(!dispatchOnCreateMethod.isAccessible()) {
    			dispatchOnCreateMethod.setAccessible(true);
    		}
    		dispatchOnCreateMethod.invoke(dialog, state);
        } catch (Exception e) {
        	// It should not have NoSuchMethodException.
		}
    }
	
    public final void showDialog(int id) {
    	showDialog(id, null);
    }
    
    public final boolean showDialog(int id, Bundle args) {
        if (mManagedDialogs == null) {
            mManagedDialogs = new SparseArray<ManagedDialog>();
        }
        ManagedDialog md = mManagedDialogs.get(id);
        if (md == null) {
            md = new ManagedDialog();
            md.mDialog = createDialog(id, null, args);
            if (md.mDialog == null) {
                return false;
            }
            mManagedDialogs.put(id, md);
        }
        md.mArgs = args;
        onPrepareDialog(id, md.mDialog, md.mArgs);
        md.mDialog.show();
        return true;
    }
    
    protected Dialog onCreateDialog(int id) {
        return null;
    }
    
    protected Dialog onCreateDialog(int id, Bundle args) {
        return onCreateDialog(id);
    }
    
    protected void onPrepareDialog(int id, Dialog dialog) {
        dialog.setOwnerActivity(getActivity());
    }
    
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        onPrepareDialog(id, dialog);
    }
    
    public final void dismissDialog(int id) {
        if (mManagedDialogs == null) {
            throw missingDialog(id);
        }
        final ManagedDialog md = mManagedDialogs.get(id);
        if (md == null) {
            throw missingDialog(id);
        }
        md.mDialog.dismiss();
    }
    
    public final void removeDialog(int id) {
        if (mManagedDialogs != null) {
            final ManagedDialog md = mManagedDialogs.get(id);
            if (md != null) {
                md.mDialog.dismiss();
                mManagedDialogs.remove(id);
            }
        }
    }
    
    /**
     * Creates an exception to throw if a user passed in a dialog id that is
     * unexpected.
     */
    private IllegalArgumentException missingDialog(int id) {
        return new IllegalArgumentException("no dialog with id " + id + " was ever "
                + "shown via Activity#showDialog");
    }
    
    private final void destroyManagedDialogs() {
        // dismiss any dialogs we are managing.
        if (mManagedDialogs != null) {
            final int numDialogs = mManagedDialogs.size();
            for (int i = 0; i < numDialogs; i++) {
                final ManagedDialog md = mManagedDialogs.valueAt(i);
                if (md.mDialog.isShowing()) {
                    md.mDialog.dismiss();
                }
            }
            mManagedDialogs.clear();
            mManagedDialogs = null;
        }
    }
	
	////////////////////// End of "Show a dialog managed by its host activity" //////////////////////
    
	////////////////////// methods of Activity //////////////////////
    
	/**
	 * Return the view belonging to host activity that has the given id in the hierarchy 
	 * or null if onCreateView() has not been called yet.
	 */
	public final View findActivityViewById(int id) {
		if(getActivity() == null)
			return null;
		return getActivity().findViewById(id);
	}
    
	/**
	 * Return the view belonging to fragment view that has the given id in the hierarchy 
	 * or null if onCreateView() has not been called yet.
	 */
	public final View findViewById(int id) {
		if(getView() == null)
			return null;
		return getView().findViewById(id);
	}
	
    public Context getApplicationContext() {
		if(getActivity() == null)
			return null;
        return getActivity().getApplicationContext();
    }
    
    public final Application getApplication() {
		if(getActivity() == null)
			return null;
        return getActivity().getApplication();
    }
    
    public Intent getIntent() {
		if(getActivity() == null)
			return null;
        return getActivity().getIntent();
    }
    
    public void finish() {
		if(getActivity() == null)
			return;
    	getActivity().finish();
    }
    
    public boolean isFinishing() {
		if(getActivity() == null)
			return false;
        return getActivity().isFinishing();
    }
    
    ////////////////////// end of "methods of Activity" //////////////////////
    
    ////////////////////// ProgressDialog //////////////////////
    
	public void setProgressDialogMessage(CharSequence message) {
		mAppDialogAide.setProgressDialogMessage(message);
	}
	
	public final void showProgressDialog(boolean cancelable) {
		mAppDialogAide.showProgressDialog(cancelable);
	}
	
	public final void showProgressDialog(Context context, boolean cancelable) {
		mAppDialogAide.showProgressDialog(context, cancelable);
	}
	
	public final void dismissProgressDialog() {
		mAppDialogAide.dismissProgressDialog();
	}
	
	public void setProgressDialogCancelTag(Object cancelTag) {
		mAppDialogAide.setProgressDialogCancelTag(cancelTag);
	}
	
	public final boolean progressDialogHasCanceled(Object cancelTag) {
		return mAppDialogAide.progressDialogHasCanceled(cancelTag);
	}
    
    ////////////////////// end of "ProgressDialog" //////////////////////
	
	////////////////////// Application Locale Configuration //////////////////////
    
	@Override
	public void onLocaleChanged() {
	}

	@Override
	public AppLocaleAide getAppLocaleAide() {
		return mAppLocaleAide;
	}

    ////////////////////// end of "Application Locale Configuration" //////////////////////
	
}
