package com.pccw.nowplayer.utils;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * A handy class to request auth token with specific account type and token type from AccountManager.
 * 
 * steps:
 * 1. request auth token.
 * 2. check account with account type and token type.
 *    a. if no logged-in account, lead user to login page with AccountManagerCallback;
 *    b. if there is logged-in account, getAuthToken with AccountManagerCallback;
 * 3. handle callback.
 *    a. if callback is for login, check login success or not with AccountManagerCallback result.
 *       [It is OK to request permission here if login success.]
 *    b. if callback is for token, start activity to request permission if has intent or get token from result.
 * 4. handle onActivityResult. 
 *    [It is OK to request auth token here if request permission result OK.]
 */
public class AccountManagerHelper {

	public static final int ACCOUNT_MANAGER_KEY_INTENT_REQUEST_CODE = 1001;
	private static final String TAG = AccountManagerHelper.class.getSimpleName();
	private Context mContext;
	private String mAccountsType;
	private String mAuthTokenType;
	private InnerAccountManagerCallback mAccountManagerCallback;
	public static String latestToken = "";
	/**
	 * @param context
	 * @param accountsType the type of accounts to return, null to retrieve all accounts
	 * @param authTokenType the auth token type, an authenticator-dependent string token, must not be null
	 * @param caller the caller of AccountManagerHelper.
	 */
	public AccountManagerHelper(Context context, String accountsType, String authTokenType, Activity caller) {
		mContext = context;
		mAccountsType = accountsType;
		mAuthTokenType = authTokenType;
		mAccountManagerCallback = new InnerAccountManagerCallback(caller);
	}
	
	/**
	 * @param context
	 * @param accountsType the type of accounts to return, null to retrieve all accounts
	 * @param authTokenType the auth token type, an authenticator-dependent string token, must not be null
	 * @param caller the caller of AccountManagerHelper.
	 */
	public AccountManagerHelper(Context context, String accountsType, String authTokenType, Fragment caller) {
		mContext = context;
		mAccountsType = accountsType;
		mAuthTokenType = authTokenType;
		mAccountManagerCallback = new InnerAccountManagerCallback(caller);
	}
	
	public void onAccountEmpty() {
		// empty implement.
	}
	
	public void onLoginSuccess(String type, String name) {
		// empty implement.
	}
	
	public void onAuthToken(String token) {
		// empty implement.
	}
	
	public void onException(Exception e) {
		// empty implement.
	}
	
	/**
	 * Need android.permission.GET_ACCOUNTS.
	 */
	public AccountManagerFuture<Bundle> requestAuthToken() {
		mAccountManagerCallback.setCallbackAction(InnerAccountManagerCallback.CALLBACK_ACTION_REQUEST_TOKEN);
		AccountManager am = AccountManager.get(mContext);
		// Lists all accounts of a particular type.
		// Empty (never null) if no accounts of the specified type have been added.
		Account[] accounts = am.getAccountsByType(mAccountsType);
		if (accounts == null || accounts.length == 0) {
			Log.d(TAG, "getAccountsByType " + mAccountsType + " returns null.");
			// no app to handle the account, or the the account no logged-in the app.
			onAccountEmpty();
			return null;
		} else {
			for(Account acc : accounts) {
				//acc.name, acc.type
				Log.d(TAG, "getAccountsByType " + mAccountsType + " returns : " + acc);
			}
			return am.getAuthToken(accounts[0], mAuthTokenType, null, true, mAccountManagerCallback, null);
		}
	}
	
	/**
	 * Need android.permission.GET_ACCOUNTS.
	 */
	public AccountManagerFuture<Bundle> getCachedAuthTokenAMF() {
		Log.d(TAG, "getCachedAuthTokenAMF()");
		mAccountManagerCallback.setCallbackAction(InnerAccountManagerCallback.CALLBACK_ACTION_REQUEST_TOKEN);
		AccountManager am = AccountManager.get(mContext);
		// Lists all accounts of a particular type.
		// Empty (never null) if no accounts of the specified type have been added.
		Account[] accounts = am.getAccountsByType(mAccountsType);
		if (accounts == null || accounts.length == 0) {
			Log.d(TAG, "getAccountsByType " + mAccountsType + " returns null.");
			return null;
		} else {
			return am.getAuthToken(accounts[0], mAuthTokenType, null, true, null, null);
		}
	}
	
	/**
	 * Login account.
	 * 
	 * @param activity
	 */
	public void loginAccount(Activity activity) {
		AccountManager am = AccountManager.get(mContext);
		mAccountManagerCallback.setCallbackAction(InnerAccountManagerCallback.CALLBACK_ACTION_LOGIN);
		am.addAccount(mAccountsType, null, null, null, activity, mAccountManagerCallback, null);
	}
	
	private class InnerAccountManagerCallback implements AccountManagerCallback<Bundle> {

		private static final String CALLBACK_ACTION_LOGIN = "CALLBACK_ACTION_LOGIN";
		private static final String CALLBACK_ACTION_REQUEST_TOKEN = "CALLBACK_ACTION_REQUEST_TOKEN";
		private Object mActivityStarter;
		private String mCallbackAction;
		
		public InnerAccountManagerCallback(Activity activity) {
			mActivityStarter = activity;
		}
		
		public InnerAccountManagerCallback(Fragment fragment) {
			mActivityStarter = fragment;
		}
		
		public void setCallbackAction(String callbackAction) {
			mCallbackAction = callbackAction;
		}
		
		@Override
		public void run(AccountManagerFuture<Bundle> future) {
        	Log.w(TAG, "AccountManagerCallback begin running action = " + mCallbackAction + " in thread " + Thread.currentThread());
        	Bundle result = null;
        	try {
        		result = future.getResult();
        		Log.d(TAG, "AccountManagerCallback onResult : " + result);
				for (String key : result.keySet()) {
					Log.d(TAG, "AccountManagerCallback key \"" + key + "\" = " + result.get(key));
				}
    			if (result.containsKey(AccountManager.KEY_INTENT)) {
    				// start GrantCredentialsPermissionActivity to request permission to access the account, now and in the future. 
    				// It will prompt up a "access request" dialog theme activity with "Deny" and "Allow" button.
    				Intent intent = (Intent) result.get(AccountManager.KEY_INTENT);
    				Log.w(TAG, "AccountManagerCallback start AccountManagerFuture result intent " + intent);
    				// clear the new task flag just in case, since a result is expected
    				int flags = intent.getFlags();
    				flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
    				intent.setFlags(flags);
    				// deny, onActivityResult, data = null, requestCode = 1001, resultCode = 0(Activity.RESULT_CANCELED)
    				// allow, onActivityResult, data = Intent { (has extras) }, requestCode = 1001, resultCode = -1(Activity.RESULT_OK)
    				if(mActivityStarter instanceof Activity) {
    					((Activity) mActivityStarter).startActivityForResult(intent, ACCOUNT_MANAGER_KEY_INTENT_REQUEST_CODE);
    				} else {
    					((Fragment) mActivityStarter).startActivityForResult(intent, ACCOUNT_MANAGER_KEY_INTENT_REQUEST_CODE);
    				}
    				return;
    			}
    			if (result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
    				String token = result.getString(AccountManager.KEY_AUTHTOKEN);
    				Log.w(TAG, "AccountManagerCallback onAuthToken = " + token);
    				latestToken = token;
    				onAuthToken(token);
    				return;
    			}
				if (result.containsKey(AccountManager.KEY_BOOLEAN_RESULT) && result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)
						&& result.containsKey(AccountManager.KEY_ACCOUNT_TYPE) && result.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
					// We don't need to check token is null here. Because if it is not null, it will return in the code above.
					Log.w(TAG, "AccountManagerCallback onLoginSuccess");
					onLoginSuccess(result.getString(AccountManager.KEY_ACCOUNT_TYPE), result.getString(AccountManager.KEY_ACCOUNT_NAME));
				}
        	} catch (Exception e) {
        		// OperationCanceledException, user cancel login
        		// AuthenticatorException, ?
        		// IOException, ?
            	Log.e(TAG, "AccountManagerCallback onException : " + e);
        		onException(e);
            }
		}
		
	} // end of inner class.
	
	/**
	 * Clear auth token.
	 */
	public void clearAuthToken() {
    	Log.d(TAG, "clear token");
		/*try {
			// I need to have the token before I can clear it
			AccountManagerFuture<Bundle> amf = getCachedAuthTokenAMF();
			Log.i(TAG, "clear token, amf = " + amf.toString());
			String token;
			Log.i(TAG, "amf.isDone() = " + amf.isDone());
			// If the token is cached, it will be done in moment
			if (amf.isDone()) {
				token = amf.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				Log.i(TAG, "clear token =" + token);
				AccountManager.get(mContext).invalidateAuthToken(mAccountsType, token);
			}
		} catch (OperationCanceledException e) {
		} catch (AuthenticatorException e) {
		} catch (IOException e) {
		}
		*/
    	//clear latest token stored in app
    	AccountManager.get(mContext).invalidateAuthToken(mAccountsType, latestToken);
	}
	
	/**
	 * Check onActivityResult from permission grant activity.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return true if the granted, false if denied, null if it's not onActivityResult from permission grant activity.
	 */
	public Boolean checkActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "checkActivityResult, data = " + data + ", requestCode = " + requestCode + ", resultCode = " + resultCode);
		if (requestCode == ACCOUNT_MANAGER_KEY_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				Bundle b = data.getExtras();
				Log.d(TAG, "onActivityResult, b = " + b);
				for (String key : b.keySet()) {
					Log.d(TAG, "key \"" + key + "\" = " + b.get(key));
				}
			}
			Log.w(TAG, "User allowed the access request.");
			return true;
		} else if (requestCode == ACCOUNT_MANAGER_KEY_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
			Log.w(TAG, "User denied the access request.");
			return false;
		}
		Log.i(TAG, "Not the access request activity result.");
		return null;
	}

}
