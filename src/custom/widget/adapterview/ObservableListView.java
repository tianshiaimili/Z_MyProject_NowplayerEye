package custom.widget.adapterview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * A ListView subclass which supports OnScrollChangedListener.
 * Note that OnScrollChangedListener just notify user that the ListView is scrolling;
 * not like ScrollView or HorizontalScrollView, ListView's scrollX and scrollY are always 0.
 * 
 * @author AlfredZhong
 * @version 2012-10-17
 */
public class ObservableListView extends ListView {
	
	private OnScrollChangedListener mOnScrollChangedListener;

	public ObservableListView(Context context) {
		super(context);
	}

	public ObservableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ObservableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
    public interface OnScrollChangedListener {
        void onScrollChanged(ObservableListView lv, int x, int y, int oldx, int oldy);
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
