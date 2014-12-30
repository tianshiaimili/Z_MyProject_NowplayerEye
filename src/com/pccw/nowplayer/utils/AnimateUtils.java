package com.pccw.nowplayer.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class AnimateUtils {

	
public static Animation fadeInOutAnimation(final View view,final float fromAlpha,final float toAlpha ,long duration){
		
		view.clearAnimation();
		AlphaAnimation fadein = new AlphaAnimation(fromAlpha, toAlpha);
		fadein.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				view.setAlpha(fromAlpha);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setAlpha(toAlpha);
			}
		});
		fadein.setDuration(duration);
		
		return fadein;
	}
	
}
