package com.xcharge.charger.ui.c2.activity.advert;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/* loaded from: classes.dex */
public class ViewPagerScroller extends Scroller {
    private int mScrollDuration;

    public ViewPagerScroller(Context context) {
        super(context);
        this.mScrollDuration = 1000;
    }

    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
        this.mScrollDuration = 1000;
    }

    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
        this.mScrollDuration = 1000;
    }

    @Override // android.widget.Scroller
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, this.mScrollDuration);
    }

    @Override // android.widget.Scroller
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, this.mScrollDuration);
    }

    public void ViewPagerScroller(double autoScrollFactor) {
    }
}
