package custom.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A subclass of ImageView that support auto set height with the width.
 */
public class FitWidthImageView extends ImageView {
	
	public FitWidthImageView(Context context) {
        super(context);
    }

    public FitWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FitWidthImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (this.getDrawable() != null) {
        	if(getDrawable().getIntrinsicHeight() != 0 && getDrawable().getIntrinsicWidth() != 0){
        		height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
        	}
		}
        setMeasuredDimension(width, height);
    }
    
}
