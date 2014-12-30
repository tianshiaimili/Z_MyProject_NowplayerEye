package custom.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * A ScrollView subclass which supports OnScrollChangedListener.
 * 
 * @author AlfredZhong
 * @version 2012-10-17
 * @since API Level 3
 */
public class ObservableScrollView extends ScrollView {

    private OnScrollChangedListener mOnScrollChangedListener;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void fling(int velocityX) {
    	// Change velocityX to change initial velocity in the X direction.
    	super.fling(velocityX);
    }
    
    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
    	// verticalScrollBarEnabled default true. 
    	super.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
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
    	void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }
    
    public void setOnScrollChangedListener(OnScrollChangedListener l) {
    	mOnScrollChangedListener = l;
    }
    
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(mOnScrollChangedListener != null) {
        	mOnScrollChangedListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

}