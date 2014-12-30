package custom.widget.adapterview;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

/**
 * <pre>
 * User Guide:
 * Just use HorizontalListView like you do with ListView, but note that:
 * 1. You should pay attention to layout_height:
 *    Currently it does NOT honor the wrap_content value for its height,
 *    you should use exact size height or fill_parent.
 * 2. Make sure your adapter.getView() are using LayoutInflater.inflate(resId, parent, false) instead of LayoutInflater.inflate(resId, null);
 * 3. ListView XML attributes can not be applied to HorizontalListView, e.g. android:listSelector.
 * 4. setSelection() and getSelectedView() has not yet implemented.
 * 5. Just use this class to show items. Do NOT use scrollTo and auto center features.
 * 
 * For more info, please see:
 * https://github.com/mtparet/HorizontalListView
 * http://www.dev-smart.com/archives/34
 * https://github.com/dinocore1/DevsmartLib-Android
 * </pre>
 * 
 * Since Android 2.1
 * 
 * @author AlfredZhong
 * @version 2012-03-07, base version, use super.onMeasure().
 * @version 2012-09-13, add onMeasure() and measureListViewChild() to fixed super.onMeasure() "out of sight views still remained in screen" bug.
 * @version 2012-09-14, added listSelector.
 * @version 2012-09-17, fixed first child left in layout_height EXACTLY mode.
 * @version 2012-09-17, use super.onMeasure().
 */
public class HorizontalListView extends AdapterView<ListAdapter> {

	private static final String TAG = HorizontalListView.class.getSimpleName();
	public boolean mAlwaysOverrideTouch = true;
	protected ListAdapter mAdapter;
	private int mLeftViewIndex = -1;
	private int mRightViewIndex = 0;
	protected int mCurrentX;
	protected int mNextX;
	private int mMaxX = Integer.MAX_VALUE;
	private int mDisplayOffset = 0;
	protected Scroller mScroller;
	private GestureDetector mGesture;
	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener mOnItemSelectedListener;
	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	private boolean mDataChanged = false;
	// modified
	private boolean mUseDefaultOnMeasure = true;
	
	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private synchronized void initView() {
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mDisplayOffset = 0;
		mCurrentX = 0;
		mNextX = 0;
		mMaxX = Integer.MAX_VALUE;
		mScroller = new Scroller(getContext());
		mGesture = new GestureDetector(getContext(), mOnGesture);
	}
	
	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
	}
	
	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		mOnItemClickListener = listener;
	}
	
	@Override
	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
		mOnItemLongClickListener = listener;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized(HorizontalListView.this){
				mDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}
		
	};

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if(mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataObserver);
		}
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		reset();
	}
	
	private synchronized void reset(){
		initView();
		removeAllViewsInLayout();
        requestLayout();
	}

	@Override
	public View getSelectedView() {
		throw new RuntimeException(TAG + " has not yet implemented getSelectedView().");
	}

	@Override
	public void setSelection(int position) {
		throw new RuntimeException(TAG + " has not yet implemented setSelection().");
	}
	
	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if(params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			throw new RuntimeException(TAG + " does NOT honor the wrap_content value for its height.");
		}
		if(mUseDefaultOnMeasure) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			/*
			 * The following code is the measure way for layout_height is wrap_content or unspecified, 
			 * but super.onMeasure() has "out of sight views still remained in screen bug".
			 * So we also measure children ourself even layout_height is fill_parent or fixed size.
			 */
			final int count = getChildCount();
			View child = null;
			int height = 0;
			for (int i = 0; i < count; i++) {
				child = getChildAt(i);
				measureListViewChild(child, widthMeasureSpec, heightMeasureSpec);
				//Log.d(TAG, "Child = (" + child.getLeft() + ", " + child.getTop() + ", " + child.getRight() + ", " + child.getBottom() + ")");
				if(child.getMeasuredHeight() > height) {
					height = child.getMeasuredHeight();
				}
				// set child selector
				setSelector(child);
			}
			//Log.d(TAG, "ListView height " + getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec) + ", item max height " + height);
			if(height == 0) {
				height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			} 
			// Note that we use parent layout width here, not item width.
	        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
		}
	}
	
	/**
	 * Measure child with child's widthMeasureSpec and parent layout's heightMeasureSpec.
	 * 
	 * @param child
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	private void measureListViewChild(View child, int widthMeasureSpec, int heightMeasureSpec) {
		android.view.ViewGroup.LayoutParams p = (android.view.ViewGroup.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            		android.view.ViewGroup.LayoutParams.FILL_PARENT);
            child.setLayoutParams(p);
        }
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        child.measure(android.view.ViewGroup.getChildMeasureSpec(widthMeasureSpec, horizontalPadding, p.width), heightMeasureSpec);
	}

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(mAdapter == null){
			return;
		}
		if(mDataChanged){
			int oldCurrentX = mCurrentX;
			initView();
			removeAllViewsInLayout();
			mNextX = oldCurrentX;
			mDataChanged = false;
		}
		if(mScroller.computeScrollOffset()){
			int scrollx = mScroller.getCurrX();
			mNextX = scrollx;
		}
		if(mNextX <= 0){
			mNextX = 0;
			mScroller.forceFinished(true);
		}
		if(mNextX >= mMaxX) {
			mNextX = mMaxX;
			mScroller.forceFinished(true);
		}
		int dx = mCurrentX - mNextX;
		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);
		mCurrentX = mNextX;
		if(!mScroller.isFinished()){
			post(new Runnable(){
				@Override
				public void run() {
					requestLayout();
				}
			});
		}
	}
	
	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount()-1);
		if(child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);
		edge = 0;
		child = getChildAt(0);
		if(child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);
	}
	
	private void fillListRight(int rightEdge, final int dx) {
		while(rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {
			View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();
			if(mRightViewIndex == mAdapter.getCount()-1) {
				mMaxX = mCurrentX + rightEdge - getWidth();
			}
			if (mMaxX < 0) {
				mMaxX = 0;
			}
			mRightViewIndex++;
		}
	}
	
	private void fillListLeft(int leftEdge, final int dx) {
		while(leftEdge + dx > 0 && mLeftViewIndex >= 0) {
			View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			mLeftViewIndex--;
			mDisplayOffset -= child.getMeasuredWidth();
		}
	}
	
	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while(child != null && child.getRight() + dx <= 0) {
			mDisplayOffset += child.getMeasuredWidth();
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mLeftViewIndex++;
			child = getChildAt(0);
		}
		child = getChildAt(getChildCount()-1);
		while(child != null && child.getLeft() + dx >= getWidth()) {
			mRemovedViewQueue.offer(child);
			removeViewInLayout(child);
			mRightViewIndex--;
			child = getChildAt(getChildCount()-1);
		}
	}
	
	private void positionItems(final int dx) {
		if(getChildCount() > 0){
			mDisplayOffset += dx;
			int left = mDisplayOffset;
			for(int i=0;i<getChildCount();i++){
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
				left += childWidth;
			}
		}
	}
	
	public synchronized void scrollTo(int x) {
		mScroller.startScroll(mNextX, 0, x - mNextX, 0);
		requestLayout();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= mGesture.onTouchEvent(ev);
		return handled;
	}
	
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		synchronized(HorizontalListView.this) {
			mScroller.fling(mNextX, 0, (int)-velocityX, 0, 0, mMaxX, 0, 0);
		}
		requestLayout();
		return true;
	}
	
	protected boolean onDown(MotionEvent e) {
		mScroller.forceFinished(true);
		return true;
	}
	
	private GestureDetector.OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {
	
		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			synchronized(HorizontalListView.this){
				mNextX += (int)distanceX;
			}
			requestLayout();
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			EventView child = findEvenView(e);
			if(child != null) {
				int position = visiblePositionToAdapterPosition(child.position);
				mSelectedPosition = position;
				Log.v(TAG, "onItemClick " + position);
				if(mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(HorizontalListView.this, child.view, position, mAdapter.getItemId(position));
				}
				if(checkSelectedPosition(position)) {
					Log.v(TAG, "onItemSelected " + position);
					mLastSelectedPosition = position;
					if(mOnItemSelectedListener != null) {
						mOnItemSelectedListener.onItemSelected(HorizontalListView.this, child.view, position, mAdapter.getItemId(position));
					}
				}
			}
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			super.onShowPress(e);
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			EventView child = findEvenView(e);
			if (child != null) {
				int position = visiblePositionToAdapterPosition(child.position);
				Log.v(TAG, "onItemLongClick " + position);
				if (mOnItemLongClickListener != null) {
					mOnItemLongClickListener.onItemLongClick(HorizontalListView.this, child.view, position, mAdapter.getItemId(position));
				}
			}
		}
		
		private boolean checkSelectedPosition(int position) {
			if(position == mLastSelectedPosition) {
				if(mIsSameSelectedPositionValid) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
		
		private int visiblePositionToAdapterPosition(int visiblePosition) {
			return mLeftViewIndex + 1 + visiblePosition;
		}
		
		private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
		
		class EventView {
			final View view;
			final int position;
			EventView(View v, int pos) {
				view = v;
				position = pos;
			}
		}
		
		private EventView findEvenView(MotionEvent e) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					return new EventView(child, i);
				}
			}
			return null;
		}
		
	}; // end of inner class.
	
	private Drawable mSelector;
	private boolean selectorFromUser;
	private int mSelectedPosition;
	private boolean mIsSameSelectedPositionValid;
	private int mLastSelectedPosition = -1;
	
	public int getDefaultSelector() {
		return android.R.drawable.list_selector_background;
	}
	
	public void setSelector(int resID) {
        setSelector(getResources().getDrawable(resID));
    }
	
	public void setSelector(Drawable sel) {
		selectorFromUser = true;
		mSelector = sel;
	}
	
	private void setSelector(View view) {
		// default selector is transparent.
		if(selectorFromUser) {
			view.setClickable(true);
			view.setBackgroundDrawable(mSelector);
		} 
	}
	
	public int getSelectedPosition() {
		return mSelectedPosition;
	}
	
	public void setSameSelectedPositionValid(boolean valid) {
		mIsSameSelectedPositionValid = valid;
	}
	
}
