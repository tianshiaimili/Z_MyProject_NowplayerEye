package com.pccw.nowplayer.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentUtils {
	
	public static void add(FragmentActivity act, Fragment fragment, int containerId, String tag) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(containerId, fragment, tag);
		ft.commit();
	}
	
	public static void remove(FragmentActivity act, Fragment fragment) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.remove(fragment);
		ft.commit();
	}
	
	public static void replace(FragmentActivity act, Fragment fragment, int containerId, String tag) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.replace(containerId, fragment, tag);
		ft.commit();
	}

	public static void attach(FragmentActivity act, Fragment fragment) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.attach(fragment);
		ft.commit();
	}
	
	public static void detach(FragmentActivity act, Fragment fragment) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.detach(fragment);
		ft.commit();
	}
	
	public static void show(FragmentActivity act, Fragment fragment) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.show(fragment);
		ft.commit();
	}
	
	public static void hide(FragmentActivity act, Fragment fragment) {
		FragmentManager manager = act.getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		ft.hide(fragment);
		ft.commit();
	}
	
	public static interface FragmentFeed {
		public Fragment newFragment(String tag); 
	}
	
	/**
	 * @author AlfredZhong
	 * @version 2013-08-27
	 */
	public static class FragmentSwitcher {
		
		// current showing fragment.
		private Fragment mCurrentFragment;
		// fragment container id.
		private int mContainerId;
		// fragment feed.
		private FragmentFeed mFragmentFeed;

		public FragmentSwitcher(int containerId, FragmentFeed feed) {
			mContainerId = containerId;
			mFragmentFeed = feed;
		}
		
		public Fragment switchFragment(FragmentActivity act, String newFragmentTag) {
			if(newFragmentTag == null) {
				throw new NullPointerException("Fragment tag can NOT be null.");
			}
			FragmentManager manager = act.getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			if(mCurrentFragment != null) {
				// detach current fragment from UI if any.
				ft.detach(mCurrentFragment);
			}
			mCurrentFragment = manager.findFragmentByTag(newFragmentTag);
			if(mCurrentFragment == null) {
				// this fragment never show before, add it and show.
				mCurrentFragment = mFragmentFeed.newFragment(newFragmentTag);
				ft.add(mContainerId, mCurrentFragment, newFragmentTag);
			} else {
				// re-attach fragment to show.
				ft.attach(mCurrentFragment);
			}
			ft.commit();
			return mCurrentFragment;
		}
		
		public Fragment getCurrentFragment() {
			return mCurrentFragment;
		}
		
	}

}
