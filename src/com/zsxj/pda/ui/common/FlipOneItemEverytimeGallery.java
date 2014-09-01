package com.zsxj.pda.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class FlipOneItemEverytimeGallery extends Gallery {
	private MotionEvent e;

		public FlipOneItemEverytimeGallery(Context context) {
			super(context);
		}
		public FlipOneItemEverytimeGallery(Context context,AttributeSet attrs) {  
			super(context, attrs);  
		}  

		public FlipOneItemEverytimeGallery(Context context, AttributeSet attrs, int defStyle) {  
			super(context, attrs, defStyle);  
		}  

		/***
		 * Override this method to achieve that effect, which flip only one item in the gallery every time sliding.
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			int kEvent;  
			if(isScrollingLeft(e1, e2)){ 
				kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
			}
			else{
				kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;  
			}
			onKeyDown(kEvent, null);  
			return true;
		}
		private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2){  
			return e2.getX() > e1.getX();  
		}
		
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			boolean bb=super.onInterceptTouchEvent(ev);
			if(ev.getAction()==MotionEvent.ACTION_DOWN){
				e=MotionEvent.obtain(ev);
				super.onTouchEvent(ev);
			}else if(ev.getAction()==MotionEvent.ACTION_MOVE){
				if(isXhdpi()){
					System.out.println("the screen is xhdpi!");
					if(Math.abs(ev.getX()-e.getX())>8){
						bb=true;
					}
				}
				else {
					System.out.println("the screen is not xhdpi!");
					if(Math.abs(ev.getX()-e.getX())>0){
						bb=true;
					}
				}
			}
			System.out.println("bb=="+bb);
			return bb;
		}
		
		private boolean isXhdpi() {
			int d = getResources().getDisplayMetrics().densityDpi;
			System.out.println("densityDpi=="+d);
			if(d > 240) return true;
			return false;
		}
}

