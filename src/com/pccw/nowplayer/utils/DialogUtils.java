package com.pccw.nowplayer.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.ListAdapter;

public class DialogUtils {
	
	private DialogUtils() {
	}

	public static AlertDialog.Builder createAlertDialogBuilder(Context context,
			Drawable icon, String title, String message,
			String positiveButtonText, String neutralButtonText, String negativeButtonText,
			final DialogInterface.OnClickListener listener, boolean cancelable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (icon != null) {
			builder.setIcon(icon);
		}
		if (title != null) {
			/*
			 * If you want to setTitle() onPrepareDialog(), you can not setTitle() null or empty or not call setTitle() at all.
			 * You should setTitle() with a non-empty string onCreateDialog(). e.g. setTitle("Title can not be empty if you need.").
			 * Otherwise setTitle() won't be work onPrepareDialog().
			 */
			builder.setTitle(title);
		}
		if (message != null) {
			/*
			 * If you want to setMessage() onPrepareDialog(), you can not setMessage() null or not call setMessage() at all.
			 * You should setMessage() empty or non-empty string onCreateDialog().
			 * Otherwise setMessage() won't be work onPrepareDialog().
			 */
			builder.setMessage(message);
		} 
		/*
		 * If title not null and message is null, the dialog UI will be strange.
		 * Note that even message is empty, it will occupy space in dialog UI.
		 */
		if (positiveButtonText != null) {
			builder.setPositiveButton(positiveButtonText, listener);
		}
		if (neutralButtonText != null) {
			builder.setNeutralButton(neutralButtonText, listener);
		}
		if (negativeButtonText != null) {
			builder.setNegativeButton(negativeButtonText, listener);
		}
		builder.setCancelable(cancelable);
		return builder;
	}

	public static Dialog createMessageAlertDialog(Context context,
			Drawable icon, String title, String message,
			String positiveButtonText, String neutralButtonText, String negativeButtonText,
			final DialogInterface.OnClickListener listener, boolean cancelable) {
		return createAlertDialogBuilder(context, icon, title, message,
				positiveButtonText, neutralButtonText, negativeButtonText, listener, cancelable).create();
	}
	
	public static Dialog createItemsAlertDialog(Context context, 
			Drawable icon, String title, CharSequence[] items, 
			String positiveButtonText, String neutralButtonText, String negativeButtonText,
			final DialogInterface.OnClickListener listener, boolean cancelable) {
		AlertDialog.Builder builder = createAlertDialogBuilder(context, icon,
				title, null, positiveButtonText, neutralButtonText, negativeButtonText, listener, cancelable);
		if (items != null) {
			// Can not call Builder.setMessage() which will replace the items view.
			// Once an item is clicked, dialog will dismiss.
			builder.setItems(items, listener);
		}
		return builder.create();
	}

	public static Dialog createSingleChoiceItemsAlertDialog(Context context,
			Drawable icon, String title, CharSequence[] items, int checkedItem,
			String positiveButtonText, String neutralButtonText, String negativeButtonText,
			final DialogInterface.OnClickListener listener, boolean cancelable) {
		AlertDialog.Builder builder = createAlertDialogBuilder(context, icon,
				title, null, positiveButtonText, neutralButtonText, negativeButtonText, listener, cancelable);
		if (items != null) {
			// Can not call Builder.setMessage() which will replace the items view.
			// Even an item is selected, dialog will NOT dismiss.
			builder.setSingleChoiceItems(items, checkedItem, listener);
		}
		return builder.create();
	}
	
	public static Dialog createCustomSingleChoiceItemsAlertDialog(Context context,
			Drawable icon, String title, ListAdapter adapter, int checkedItem,
			String positiveButtonText, String neutralButtonText, String negativeButtonText,
			final DialogInterface.OnClickListener listener, boolean cancelable) {
		AlertDialog.Builder builder = createAlertDialogBuilder(context, icon,
				title, null, positiveButtonText, neutralButtonText, negativeButtonText, listener, cancelable);
		if (adapter != null) {
			builder.setSingleChoiceItems(adapter, checkedItem, listener);
		}
		return builder.create();
	}
	
}