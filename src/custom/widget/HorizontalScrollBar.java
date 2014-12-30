package custom.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A helper class to generate ScrollBar.
 * Create a ScrollBar just as creating AlertDialog with AlertDialog.Builder.
 * 
 * @author AlfredZhong
 * @version 2013-01-25
 * @version 2013-03-20, mark points in ScrollBar.
 * @version 2013-07-03, support more types.
 * @version 2013-07-08, use inner class Builder to create ScrollBar.
 */
public class HorizontalScrollBar {
	
	private static final String TAG = HorizontalScrollBar.class.getSimpleName();
	private HorizontalScrollView mHorizontalScrollView;
	private int count;
	private int contentWidth;
	private final List<View> viewList;
	/*
	 * start points are item start positions on the X axis, 
	 * used to scroll to the specific item.
	 */
	private final List<Integer> mStartPoints;
	/*
	 * middle points are item middle positions on the X axis,
	 * used to detect whether current position is in the region
	 * and used to scroll to the specific item with making it center_horizental.
	 */
	private final List<Integer> mMiddlePoints;
	/*
	 * data for ScrollBarBuilder.
	 */
	private Object tag;
	/*
	 * list data for mItemViews.
	 */
	private List<?> dataList;
	
	private HorizontalScrollBar() {
		mStartPoints = new ArrayList<Integer>();
		mMiddlePoints = new ArrayList<Integer>();
		viewList = new ArrayList<View>();
	}
	
	private void bindHorizontalScrollView(HorizontalScrollView hsv) {
		mHorizontalScrollView = hsv;
	}
	
	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	public Object getTag() {
		return this.tag;
	}
	
	public List<?> getDataList() {
		return this.dataList;
	}
	
	public List<View> getViewList() {
		return this.viewList;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getContentWidth() {
		return this.contentWidth;
	}
	
	/**
	 * Note that you'd better call scrollTo() inside Handler.post() 
	 * if the view is just built or the view is just visible from gone.
	 * 
	 * @param index
	 * @param center whether make the TextView center_horizental.
	 */
	public void scrollTo(int index, boolean center) {
		int x;
		if(center)
			x = mMiddlePoints.get(index);
		else
			x = mStartPoints.get(index);
		mHorizontalScrollView.scrollTo(x, 0);
		Log.d(TAG, "ScrollTextBuilder scrollTo " + x);
	}
	
	/**
	 * Note that you'd better call scrollTo() inside Handler.post() 
	 * if the view is just built or the view is just visible from gone.
	 * 
	 * @param index
	 * @param center whether make the TextView center_horizental.
	 */
	public void smoothScrollTo(int index, boolean center) {
		int x;
		if(center)
			x = mMiddlePoints.get(index);
		else
			x = mStartPoints.get(index);
		mHorizontalScrollView.smoothScrollTo(x, 0);
		Log.d(TAG, "ScrollTextBuilder smoothScrollTo " + x);
	}
	
	public void performClick(int index) {
		for(View t : viewList) {
			t.setSelected(false);
		}
		viewList.get(index).setSelected(true);
		viewList.get(index).performClick();
	}
	
	public void scrollAndClick(int index, boolean center) {
		scrollTo(index, center);
		performClick(index);
	}
	
	public void smoothScrollAndClick(int index, boolean center) {
		smoothScrollTo(index, center);
		performClick(index);
	}
	
	public boolean hasItem(){
		if(viewList != null && viewList.size() > 0){
			return true;
		}
		return false;
	}
	
	public boolean hasItem(int index) {
		int size = 0;
		if(viewList != null && (size = viewList.size()) > 0 && index < size) {
			return true;
		}
		return false;
	}

	private static class BlankView extends View {
		
		public BlankView(Context context, int width, int height) {
			super(context);
			setLayoutParams(new ViewGroup.LayoutParams(width, height));
		}
		
	}

	public interface ScrollBarItemInfoProvider {
		
		/**
		 * Returns the item width.
		 * 
		 * @param convertView
		 * @return
		 */
		public int getItemWidth(View convertView);
		
		/**
		 * Set item content.
		 * 
		 * @param builder
		 * @param convertView
		 * @param value
		 */
		public void setItemContent(HorizontalScrollBar horizontalScrollBar, View convertView, Object itemData, int position);
		
	}
	
	public interface OnScrollBarItemClickListener {
		/**
		 * Trigger when item clicked.
		 * @param position
		 * @param text
		 */
		public void onClick(HorizontalScrollBar horizontalScrollBar, View convertView, int position);
	}
	
	public interface OnScrollBarEdgeListener {
		/**
		 * Trigger when scroll ended.
		 * 
		 * @param leftmost
		 * @param rightmost
		 */
		public void onEdge(boolean leftmost, boolean rightmost);
	}
	
	public static final ScrollBarItemInfoProvider TEXTVIEW_BAR_ITEM_INFO_PROVIDER = new ScrollBarItemInfoProvider() {
		@Override
		public int getItemWidth(View convertView) {
			TextView t = (TextView)convertView;
			Paint paint = t.getPaint();
			// If text is empty, it will return 0.
			return Math.round(paint.measureText(t.getText().toString()));
		}

		@Override
		public void setItemContent(HorizontalScrollBar horizontalScrollBar, View convertView, Object itemData, int position) {
			String val = (String) itemData;
			TextView t = (TextView)convertView;
			t.setText(val);
		}
	};
	
    public static class Builder {
    	
    	private int mVisibleContentWidth;
    	private ScrollBarItemInfoProvider mScrollBarItemInfoProvider;
    	private HorizontalScrollBar mScrollBar;
    	private int mItemLayoutResId;
    	private boolean mLeaveTwoEdgesEmpty;
    	private Drawable mDivider;
    	private int mDividerWidth;
    	private OnScrollBarItemClickListener mOnScrollBarItemClickListener;
    	
    	public Builder(int visibleContentWidth, ScrollBarItemInfoProvider provider) {
    		mVisibleContentWidth = visibleContentWidth;
    		mScrollBarItemInfoProvider = provider;
    		mScrollBar = new HorizontalScrollBar();
    	}
    	
    	public Builder setTag(Object tag) {
    		mScrollBar.setTag(tag);
    		return this;
    	}
    	
    	public Builder setDataList(List<?> dataList) {
    		System.out.println("ScrollBar setDataList");
    		mScrollBar.dataList = dataList;
    		return this;
    	}
    	
    	public Builder setItemLayoutResId(int itemResId) {
    		mItemLayoutResId = itemResId;
    		return this;
    	}
    	
    	/**
    	 * Set whether leave two edges empty to make first/last item center in visible content width.
    	 */
    	public Builder setCenterTwoEdges(boolean leaveTwoEdgesEmpty) {
    		mLeaveTwoEdgesEmpty = leaveTwoEdgesEmpty;
    		return this;
    	} 
    	
    	public Builder setDivider(Drawable divider, int dividerWidth) {
    		mDivider = divider;
    		mDividerWidth = dividerWidth;
    		return this;
    	} 
    	
    	public Builder setOnScrollBarItemClickListener(OnScrollBarItemClickListener listener) {
    		mOnScrollBarItemClickListener = listener;
    		return this;
    	}
    	
    	/**
    	 * Generate a scroll bar.
    	 */
    	public HorizontalScrollBar create(final HorizontalScrollView hsv) {
    		// DisplayUtils.setViewCenterInParent(hsv); // layout_gravity should set by caller.
            // content LinearLayout
    		mScrollBar.bindHorizontalScrollView(hsv);
    		final Context context = hsv.getContext();
            final LinearLayout layout = new LinearLayout(context);
            layout.removeAllViews();
            // Child of HorizontalScrollView layout_width is "wrap_content" hard code inside, use android:fillViewport="true". 
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		// set LinearLayout layout_gravity="center", that means LinearLayout center in parent.
            // set Child of HorizontalScrollView layout_gravity by coding is not working, by XML will do.
            // There is a bug : https://code.google.com/p/android/issues/detail?id=20088, 
            // Child of HorizontalScrollView with gravity "center" are cutted when items width is more than the widget width.
            // lp.gravity = android.view.Gravity.CENTER;
            layout.setLayoutParams(lp);
    		layout.setOrientation(LinearLayout.HORIZONTAL);
    		// set LinearLayout gravity="center"
    		layout.setGravity(android.view.Gravity.CENTER);
    		// remove child view before adding.
    		hsv.removeAllViews();
    		hsv.addView(layout);
    		hsv.setHorizontalScrollBarEnabled(false);
    		hsv.scrollTo(0, 0);
            // add items
            List<Integer> rangePoints = new ArrayList<Integer>();
            int range = 0;
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout.LayoutParams dividerLayoutParams = null;
            if(mDivider != null && mDividerWidth > 0) {
        		dividerLayoutParams = new LinearLayout.LayoutParams(mDividerWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        		dividerLayoutParams.gravity = android.view.Gravity.CENTER;
            }
    		mScrollBar.count = mScrollBar.dataList.size();
            for(int i=0; i<mScrollBar.count; i++) {
            	mScrollBar.mStartPoints.add(range);
            	rangePoints.add(range);
    			final View t = inflater.inflate(mItemLayoutResId, layout, false);
    			mScrollBar.viewList.add(t);
    			mScrollBarItemInfoProvider.setItemContent(mScrollBar, t, mScrollBar.dataList.get(i), i);
            	if(mOnScrollBarItemClickListener != null) {
            		t.setTag(i);
    	        	t.setOnClickListener(new View.OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						for(View t : mScrollBar.viewList) {
    							t.setSelected(false);
    						}
    						v.setSelected(true);
    						mOnScrollBarItemClickListener.onClick(mScrollBar, v, (Integer) t.getTag());
    					}
    				});
            	}

            	if(i == 0) {
            		// whether leave two edges empty to make first/last item center in visible content width.
            		if(mLeaveTwoEdgesEmpty) {
                		// add leftmost blank view
            	    	ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams)t.getLayoutParams();
                		int blankWidth = (mVisibleContentWidth - mScrollBarItemInfoProvider.getItemWidth(t)) / 2 - vlp.leftMargin;
                		layout.addView(new BlankView(context, blankWidth, t.getHeight()));
                		range += blankWidth;
            		}
            	}
            	layout.addView(t);
            	if(i == mScrollBar.count - 1) {
            		// whether leave two edges empty to make first/last item center in visible content width.
            		if(mLeaveTwoEdgesEmpty) {
                		// add rightmost blank view
            	    	ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams)t.getLayoutParams();
                		int blankWidth = (mVisibleContentWidth - mScrollBarItemInfoProvider.getItemWidth(t)) / 2 - vlp.rightMargin;
                		layout.addView(new BlankView(context, blankWidth, t.getHeight()));
                		range += blankWidth;
            		}
            	} else {
            		if(dividerLayoutParams != null) {
            			ImageView d = new ImageView(context);
            			d.setImageDrawable(mDivider);
                		layout.addView(d, dividerLayoutParams);
                		range += mDividerWidth;
            		}
            	}
            	ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams)t.getLayoutParams();
            	range += mScrollBarItemInfoProvider.getItemWidth(t) + vlp.leftMargin + vlp.rightMargin + t.getPaddingLeft() + t.getPaddingRight();
            }
            // add rightmost point.
            rangePoints.add(range);
            for(int i=0; i<rangePoints.size() - 1; i++) {
            	// calculate middle points.
            	// middle point can be negative integer since left side may not wide enough if leaveTwoEdgesEmpty false.
            	mScrollBar.mMiddlePoints.add(rangePoints.get(i) - (mVisibleContentWidth - (rangePoints.get(i + 1) - rangePoints.get(i))) / 2);
            }
            System.out.println("range  points : " + rangePoints);
            System.out.println("start  points : " + mScrollBar.mStartPoints);
            System.out.println("middle points : " + mScrollBar.mMiddlePoints);
            mScrollBar.contentWidth = range;
    		return mScrollBar;
    	}
    	
    }
	
}
