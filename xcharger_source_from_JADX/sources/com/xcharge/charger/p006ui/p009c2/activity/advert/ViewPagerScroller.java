package com.xcharge.charger.p006ui.p009c2.activity.advert;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/* renamed from: com.xcharge.charger.ui.c2.activity.advert.ViewPagerScroller */
public class ViewPagerScroller extends Scroller {
    private int mScrollDuration = 1000;

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, this.mScrollDuration);
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, this.mScrollDuration);
    }

    public void ViewPagerScroller(double autoScrollFactor) {
    }
}
