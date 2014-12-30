package custom.widget;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * @author AlfredZhong
 * @version 2013-08-13
 */
public class ScrollableHelper {

	/**
	 * Synchronize ScrollView scrolling.
	 * 
	 * @param scrollX
	 * @param scrollY
	 * @param scrollViews
	 */
	public static void syncScrollViewScrolling(ScrollView currentScrolling, ScrollView... scrollViews) {
		if(currentScrolling == null || scrollViews == null || scrollViews.length == 0) {
			// If you don't set scrollViews = null explicitly, scrollViews is [], not null.
			return;
		}
		for(ScrollView sv : scrollViews) {
			if(sv != null && sv != currentScrolling) {
				// ScrollView scrollX should be 0.
				// no need to check x, y, ScrollView.scrollTo() will check them.
				sv.scrollTo(0, currentScrolling.getScrollY());			
			}
		}
	}
	
	/**
	 * Synchronize HorizontalScrollView scrolling.
	 * 
	 * @param scrollX
	 * @param scrollY
	 * @param horizontalScrollViews
	 */
	public static void syncHorizontalScrollViewScrolling(HorizontalScrollView currentScrolling, HorizontalScrollView... horizontalScrollViews) {
		if(currentScrolling == null || horizontalScrollViews == null || horizontalScrollViews.length == 0) {
			return;
		}
		for(HorizontalScrollView hsv : horizontalScrollViews) {
			if(hsv != null && hsv != currentScrolling) {
				// HorizontalScrollView scrollY should be 0.
				// no need to check x, y, HorizontalScrollView.scrollTo() will check them.
				hsv.scrollTo(currentScrolling.getScrollX(), 0);
			}
		}
	}
	
	/**
	 * A handy class to synchronize ListView Scrolling.
	 * 
	 * For optimal performance, be aware of:
	 * 1. set ListView item and widgets inside ListView item fixed size height if possible.
	 * 2. ListView height must be fill_parent or fixed size, do NOT use wrap_content.
	 * 
	 * @version 2013-08-24
	 */
	public static class ListViewScrollingSynchronizer {
		
		private ListView[] mListViews;
		
		public ListViewScrollingSynchronizer(ListView... listViews) {
			if(listViews == null || listViews.length < 2) {
				throw new IllegalArgumentException("ListViews that need synchronize scrolling can NOT be null or size < 2.");
			}
			ListView child1 = listViews[0];
			ViewGroup parent = (ViewGroup)child1.getParent();
			while(parent != null) {
				if(isSameParent(parent, listViews)) {
					break;
				}
				parent = (ViewGroup) parent.getParent();
			}
			if(parent == null) {
				throw new RuntimeException("ListViews do NOT have a same parent.");
			} else {
				Log.i("DEBUG", "Found parent " + parent.getId());
			}
			// create and add TEMP ListView to the same parent.
			ListView mTempListView = new ListView(child1.getContext());
			((ViewGroup) parent).addView(mTempListView, new FrameLayout.LayoutParams(0, 0));
			// must call setAdapter().
			mTempListView.setAdapter(new BaseAdapter() {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return null;
				}
				@Override
				public long getItemId(int position) {
					return 0;
				}
				@Override
				public Object getItem(int position) {
					return null;
				}
				@Override
				public int getCount() {
					return 0;
				}
			});
			// add TEMP ListView to synchronizing list.
			int size = listViews.length + 1;
			mListViews = new ListView[size + 1];
			for(int i=0; i<size-1; i++) {
				mListViews[i] = listViews[i];
			}
			mListViews[size-1] = mTempListView;
		}
		
		/**
		 * Whether the parent is the same parent of all ListViews.
		 */
		private boolean isSameParent(ViewGroup parent, ListView... listViews) {
			boolean parentFound = true;
			View temp = null;
			for(ListView lv : listViews) {
				temp = parent.findViewById(lv.getId());
				if(temp == null) {
					parentFound = false;
					break;
				}
			}
			return parentFound;
		}
		
		/**
		 * Synchronize ListView scrolling.
		 * Note that all these synchronized scrolling ListView should use one field
		 * to remember the which ListView is the latestTouchedView.
		 * 
		 * @param latestTouchedView the latest touched ListView.
		 * @param currentScrollingListView
		 */
		public void syncListViewScrolling(View latestTouchedView, ListView currentScrolling) {
			if(latestTouchedView == null || currentScrolling == null) {
				return;
			}
			if(latestTouchedView != currentScrolling) {
				// Avoid other ListViews ask current scrolling ListView to react.
				return;
			}
			if(currentScrolling.getChildAt(0) == null) {
				return;
			}
	    	int position = currentScrolling.getFirstVisiblePosition();
	    	int top = currentScrolling.getChildAt(0).getTop();
			for(ListView lv : mListViews) {
				if(lv != null && lv != currentScrolling) {
					// stop other ListViews' scrolling before synchronizing current scrolling.
					lv.smoothScrollBy(0, 0);
				}
			}
			for(ListView lv : mListViews) {
				if(lv != null && lv != currentScrolling) {
					// synchronizing current scrolling.
					lv.setSelectionFromTop(position, top);
				}
			}
		}
		
	} // end of inner class.
	
}
