package com.pccw.nowplayer.app;

import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * A helper class to handle ProgressDialog.
 * 
 * User guide:
 * 1 new AppDialogAide() onCreate()/onActivityCreated().
 * 2 setProgressDialogMessage() before using showProgressDialog().
 * 3 destroyProgressDialog() onDestroy()/onDestroyView().
 * 
 * @author AlfredZhong
 * @version 2013-07-24
 */
public final class AppProgressDialogAide {

	private static final String TAG = AppProgressDialogAide.class.getSimpleName();
	private ProgressDialog mProgressDialog;
	private CharSequence mProgressDialogMessage;
	private boolean hasCanceled;
	private Object dialogCancelTag;
	private DialogInterface.OnCancelListener mProgressDialogCancelListener;
	private DialogInterface.OnDismissListener mProgressDialogDismissListener;
	private Activity mActivity;
	
	public AppProgressDialogAide(Activity act) {
		mActivity = act;
	}
	
    ////////////////////// ProgressDialog //////////////////////
	
    public static ProgressDialog createProgressDialog(Context context, CharSequence title, CharSequence message, 
    		boolean indeterminate, boolean cancelable, DialogInterface.OnCancelListener cancelListener, int style) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message); 
        dialog.setIndeterminate(indeterminate); // default true
        dialog.setCancelable(cancelable); // default true
        dialog.setOnCancelListener(cancelListener);
        dialog.setProgressStyle(style); // default ProgressDialog.STYLE_SPINNER
        return dialog;
    }
	
	private static String getDefaultLoadingMessage() {
		String msg = "";
		Locale loc = Locale.getDefault();
		String language = loc.getLanguage();
		String country = loc.getCountry();
		System.out.println("Default locale is " + language + "_" + country);
		if(language.equals("zh")) {
			if(country.equals("TW") || country.equals("HK")) {
				// traditional Chinese
				msg = "載入中";
			} else {
				// simplified Chinese
				msg = "载入中";
			}
		} else {
			// non zh, use en.
			msg = "Loading...";
		}
		return msg;
	}
	
	public void setProgressDialogMessage(CharSequence message) {
		mProgressDialogMessage = message;
	}
	
	/**
	 * Show progress dialog with Activity context.
	 * 
	 * @param cancelable
	 */
	public final void showProgressDialog(boolean cancelable) {
		showProgressDialog(mActivity, cancelable);
	}
	
	public final void showProgressDialog(Context context, boolean cancelable) {
		// for inner usage.
		if(mProgressDialogCancelListener == null) {
			Log.w(TAG, getClass().getName() + " ProgressDialog OnCancelListener is null.");
			mProgressDialogCancelListener = new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Log.d(TAG, "ProgressDialog is onCancel().");
					hasCanceled = true;
				}
			};
		}
		if(mProgressDialogDismissListener == null) {
			Log.w(TAG, getClass().getName() + " ProgressDialog OnDismissListener is null.");
			mProgressDialogDismissListener = new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					Log.d(TAG, "ProgressDialog is onDismiss().");
					// Not to refer to dialog instance any more.
					mProgressDialog = null;
				}
			};
		}
		// for outer caller.
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			Log.d(TAG, "ProgressDialog isShowing.");
			return;
		}
		if(!mActivity.isFinishing()) {
			hasCanceled = false;
			if(mProgressDialogMessage == null) {
				mProgressDialogMessage = getDefaultLoadingMessage();
			}
			mProgressDialog = createProgressDialog(context, null, mProgressDialogMessage, 
					true, cancelable, mProgressDialogCancelListener, ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setOnDismissListener(mProgressDialogDismissListener);
			mProgressDialog.setCancelable(cancelable);
			mProgressDialog.show();
			Log.w(TAG, getClass().getName() + " show a new ProgressDialog.");
		}
	}
	
	public final void dismissProgressDialog() {
		if(mProgressDialog == null || mActivity == null) {
			Log.w(TAG, getClass().getName() + " has no ProgressDialog, no need to call dismissProgressDialog().");
			return;
		}
		Log.d(TAG, getClass().getName() + ".dismissProgressDialog(), isFinishing() : " + mActivity.isFinishing());
		// If isFinishing(), will cause java.lang.IllegalArgumentException: View not attached to window manager.
		if(mProgressDialog.isShowing() && !mActivity.isFinishing()) {
			// DO NOT use cancel(), otherwise you will trigger onCancelListener when dismiss dialog, not cancel dialog.
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * @param cancelTag should be unique to distinguish the specific dialog task.
	 * @see #progressDialogHasCanceled(Object)
	 */
	public void setProgressDialogCancelTag(Object cancelTag) {
		dialogCancelTag = cancelTag;
	}
	
	/**
	 * Whether ProgressDialog with the specific tag has been canceled.
	 * <p>For example, you may show ProgressDialog to load data from the Internet off the UI thread,
	 * and this ProgressDialog is cancelable. When the thread job is done, you should check
	 * whether this ProgressDialog has been canceled before you apply the thread job result.
	 * Because you may run into the case 
	 * "Cancel ProgressDialog 1, show ProgressDialog 2, Thread 1 check "canceled" is false due to ProgressDialog 2 is showing",
	 * in this case, you'd better check it with tag like:
	 * <pre>
	 * new Thread(new Runnable() {
	 *     public void run() {
	 *         String tag = this.toString();
	 *         setProgressDialogCancelTag(tag);
	 *         // do something.
	 *         if(!progressDialogHasCanceled(tag)) {
	 *             // you can use the thread result.
	 *         } else {
	 *             // user has cancel the task, do NOT use the thread result.
	 *         }
	 *     }
	 * });
	 * </pre>
	 * 
	 * @param cancelTag
	 * @return
	 * @see #setProgressDialogCancelTag(Object)
	 */
	public final boolean progressDialogHasCanceled(Object cancelTag) {
		if(mProgressDialog == null || mActivity == null) {
			Log.w(TAG, getClass().getName() + " progressDialogHasCanceled() no dialog or activity.");
			return true;
		}
		boolean retval;
		Log.d(TAG, getClass().getName() + " ProgressDialog has canceled " + hasCanceled);
		if(hasCanceled) {
			// latest ProgressDialog OnCancelListener is true.
			retval = true;
		} else if(!mProgressDialog.isShowing()) {
			// no ProgressDialog is showing.
			retval = true;
		} else {
			// Check the case "Cancel ProgressDialog 1, show ProgressDialog 2, Thread 1 check "canceled" is false due to ProgressDialog 2 is showing".
			boolean notSameTag = (cancelTag != dialogCancelTag);
			Log.d(TAG, getClass().getName() + " ProgressDialog tag is not the same " + notSameTag);
			retval = notSameTag;
		}
		Log.w(TAG, getClass().getName() + " progressDialogHasCanceled() returns " + retval);
		return retval;
	}
	
	/**
	 * Call removeProgressDialog() onDestroy(), if not, may cause:
	 * Activity xxx has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView@48572978 that was originally added here.
	 */
	public void destroyProgressDialog() {
		mActivity = null;
		try {
			Log.w(TAG, getClass().getName() + " removeProgressDialog().");
			dismissProgressDialog();
			mProgressDialog = null;
		} catch(Exception e) {
			Log.e(TAG, getClass().getName() + " removeProgressDialog() failed.", e);
		}
	}

    ////////////////////// end of "ProgressDialog" //////////////////////
	
}
