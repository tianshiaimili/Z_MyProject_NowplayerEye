package com.pccw.nowplayer.utils;

import android.content.Context;
import android.graphics.Typeface;

public class FontFaceUtils {
	static Typeface fontFace = null;
	
	public static final Typeface getRoman(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-Roman.otf");
	}
	
	public static final Typeface getBold(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-Bd.otf");
	}
	
	public static final Typeface getLightCondensed(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-LtCn.otf");
	}
	
	public static final Typeface getMediumCondensed(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-MdCn.otf");
	}
	
	public static final Typeface getTh(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-Th.otf");
	}
	
	public static final Typeface getCondensed(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-Cn.otf");
	}
	
	public static final Typeface getBoldCondensed(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-BdCn.otf");
	}
	
	public static final Typeface getMedium(Context context){
		return Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLTStd-Md.otf");
	}
}
