package com.xcharge.charger.p006ui.p009c2.activity.advert;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.p000v4.view.MotionEventCompat;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/* renamed from: com.xcharge.charger.ui.c2.activity.advert.AutoScrollViewPager */
public class AutoScrollViewPager extends ViewPager {
    public static final int DEFAULT_INTERVAL = 1500;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int SCROLL_WHAT = 0;
    public static final int SLIDE_BORDER_MODE_CYCLE = 1;
    public static final int SLIDE_BORDER_MODE_NONE = 0;
    public static final int SLIDE_BORDER_MODE_TO_PARENT = 2;
    /* access modifiers changed from: private */
    public double autoScrollFactor = 1.0d;
    private int direction = 1;
    private float downX = 0.0f;
    private Handler handler;
    /* access modifiers changed from: private */
    public long interval = 1500;
    private boolean isAutoScroll = false;
    private boolean isBorderAnimation = true;
    private boolean isCycle = true;
    private boolean isStopByTouch = false;
    /* access modifiers changed from: private */
    public ViewPagerScroller scroller = null;
    private int slideBorderMode = 0;
    private boolean stopScrollWhenTouch = true;
    /* access modifiers changed from: private */
    public double swipeScrollFactor = 1.0d;
    private float touchX = 0.0f;

    public AutoScrollViewPager(Context paramContext) {
        super(paramContext);
        init();
    }

    public AutoScrollViewPager(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    private void init() {
        this.handler = new MyHandler(this);
        setViewPagerScroller();
    }

    public void startAutoScroll() {
        this.isAutoScroll = true;
        sendScrollMessage((long) (((double) this.interval) + ((((double) this.scroller.getDuration()) / this.autoScrollFactor) * this.swipeScrollFactor)));
    }

    public void startAutoScroll(int delayTimeInMills) {
        this.isAutoScroll = true;
        sendScrollMessage((long) delayTimeInMills);
    }

    public void stopAutoScroll() {
        this.isAutoScroll = false;
        this.handler.removeMessages(0);
    }

    public void setSwipeScrollDurationFactor(double scrollFactor) {
        this.swipeScrollFactor = scrollFactor;
    }

    public void setAutoScrollDurationFactor(double scrollFactor) {
        this.autoScrollFactor = scrollFactor;
    }

    /* access modifiers changed from: private */
    public void sendScrollMessage(long delayTimeInMills) {
        this.handler.removeMessages(0);
        this.handler.sendEmptyMessageDelayed(0, delayTimeInMills);
    }

    public void setViewPagerScroller() {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Field interpolatorField = ViewPager.class.getDeclaredField("sInterpolator");
            interpolatorField.setAccessible(true);
            this.scroller = new ViewPagerScroller(getContext(), (Interpolator) interpolatorField.get((Object) null));
            scrollerField.set(this, this.scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scrollOnce() {
        int totalCount;
        int currentItem;
        int currentItem2;
        PagerAdapter adapter = getAdapter();
        int currentItem3 = getCurrentItem();
        if (adapter != null && (totalCount = adapter.getCount()) > 1) {
            if (this.direction == 0) {
                currentItem = currentItem3 - 1;
                currentItem2 = currentItem;
            } else {
                currentItem = currentItem3 + 1;
                currentItem2 = currentItem;
            }
            if (currentItem < 0) {
                if (this.isCycle) {
                    setCurrentItem(totalCount - 1, this.isBorderAnimation);
                }
            } else if (currentItem != totalCount) {
                setCurrentItem(currentItem, true);
            } else if (this.isCycle) {
                setCurrentItem(0, this.isBorderAnimation);
            }
            int nextItem = currentItem2;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (this.stopScrollWhenTouch) {
            if (action == 0 && this.isAutoScroll) {
                this.isStopByTouch = true;
                stopAutoScroll();
            } else if (ev.getAction() == 1 && this.isStopByTouch) {
                startAutoScroll();
            }
        }
        if (this.slideBorderMode == 2 || this.slideBorderMode == 1) {
            this.touchX = ev.getX();
            if (ev.getAction() == 0) {
                this.downX = this.touchX;
            }
            int currentItem = getCurrentItem();
            PagerAdapter adapter = getAdapter();
            int pageCount = adapter == null ? 0 : adapter.getCount();
            if ((currentItem == 0 && this.downX <= this.touchX) || (currentItem == pageCount - 1 && this.downX >= this.touchX)) {
                if (this.slideBorderMode == 2) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    if (pageCount > 1) {
                        setCurrentItem((pageCount - currentItem) - 1, this.isBorderAnimation);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.dispatchTouchEvent(ev);
            }
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.advert.AutoScrollViewPager$MyHandler */
    private static class MyHandler extends Handler {
        private final WeakReference<AutoScrollViewPager> autoScrollViewPager;

        public MyHandler(AutoScrollViewPager autoScrollViewPager2) {
            this.autoScrollViewPager = new WeakReference<>(autoScrollViewPager2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    AutoScrollViewPager pager = (AutoScrollViewPager) this.autoScrollViewPager.get();
                    if (pager != null) {
                        pager.scroller.ViewPagerScroller(pager.autoScrollFactor);
                        pager.scrollOnce();
                        pager.scroller.ViewPagerScroller(pager.swipeScrollFactor);
                        pager.sendScrollMessage(pager.interval + ((long) pager.scroller.getDuration()));
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public long getInterval() {
        return this.interval;
    }

    public void setInterval(long interval2) {
        this.interval = interval2;
    }

    public int getDirection() {
        return this.direction == 0 ? 0 : 1;
    }

    public void setDirection(int direction2) {
        this.direction = direction2;
    }

    public boolean isCycle() {
        return this.isCycle;
    }

    public void setCycle(boolean isCycle2) {
        this.isCycle = isCycle2;
    }

    public boolean isStopScrollWhenTouch() {
        return this.stopScrollWhenTouch;
    }

    public void setStopScrollWhenTouch(boolean stopScrollWhenTouch2) {
        this.stopScrollWhenTouch = stopScrollWhenTouch2;
    }

    public int getSlideBorderMode() {
        return this.slideBorderMode;
    }

    public void setSlideBorderMode(int slideBorderMode2) {
        this.slideBorderMode = slideBorderMode2;
    }

    public boolean isBorderAnimation() {
        return this.isBorderAnimation;
    }

    public void setBorderAnimation(boolean isBorderAnimation2) {
        this.isBorderAnimation = isBorderAnimation2;
    }
}
