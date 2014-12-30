package custom.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * A HorizontalScrollView subclass which supports OnScrollListener.
 * 
 * @author AlfredZhong
 * @version 2012-10-17
 * @since API Level 3
 */
public class ObservableHorizontalScrollView extends HorizontalScrollView {

	private static final String TAG = ObservableHorizontalScrollView.class.getSimpleName();
	private OnScrollChangedListener mOnScrollChangedListener;
	
	public ObservableHorizontalScrollView(Context context) {
		super(context);
	}
	
	public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
    @Override
    public void fling(int velocityX) {
    	// Change velocityX to change initial velocity in the X direction.
    	super.fling(velocityX);
    }
    
    @Override
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
    	// horizontalScrollBarEnabled default true. 
    	super.setHorizontalScrollBarEnabled(horizontalScrollBarEnabled);
    }
	
    /**
     * Returns the content view.
     * 
     * @return the content view or null if the content view does not exist within ScrollView.
     */
    public View getContentView() {
    	return getChildAt(0);
    }
    
    public interface OnScrollChangedListener {
        void onScrollChanged(ObservableHorizontalScrollView view, int x, int y, int oldx, int oldy);
    }
    
    public void setOnScrollChangedListener(OnScrollChangedListener l) {
    	mOnScrollChangedListener = l;
    }
    
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        // Only call OnScrollListener.onScrollChanged() when OnScrollListener is not null.
        if(mOnScrollChangedListener != null) {
        	Log.v(TAG, "onScrollChanged : x = " + x + ", y = " + y);
        	mOnScrollChangedListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
	
}
