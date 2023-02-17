package com.xcharge.charger.ui.c2.activity.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.media.TransportMediator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.xcharge.charger.core.handler.ChargeHandler;
import java.util.ArrayList;
import java.util.Random;

/* loaded from: classes.dex */
public class BubbleView extends View {
    static int center = 384;
    static int maxWidth = 14;
    public final ArrayList<Oval> balls;
    Paint paint;

    public BubbleView(Context context) {
        super(context.getApplicationContext(), null);
        this.balls = new ArrayList<>();
    }

    public BubbleView(Context context, AttributeSet attributeSet) {
        super(context.getApplicationContext(), attributeSet);
        this.balls = new ArrayList<>();
        init();
    }

    private void init() {
        this.paint = new Paint();
        this.paint.setColor(1728053247);
        this.paint.setStrokeWidth(3.0f);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setAntiAlias(true);
        ValueAnimator colorAnim = ValueAnimator.ofInt(0, 1);
        colorAnim.setDuration(3000L);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(-1);
        colorAnim.setRepeatMode(2);
        colorAnim.start();
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.BubbleView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                BubbleView.this.invalidate();
            }
        });
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < this.balls.size(); i++) {
            Oval oval = this.balls.get(i);
            canvas.drawCircle(oval.getX(), oval.getY(), oval.getRadius(), oval.paint);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != 0 && event.getAction() != 2) {
            return false;
        }
        start(addOval());
        return true;
    }

    public void start(final Oval oval) {
        ValueAnimator widthAnim = ValueAnimator.ofInt(maxWidth, 0);
        widthAnim.setDuration(3000L);
        widthAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.BubbleView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.setRadius(((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        widthAnim.addListener(new Animator.AnimatorListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.BubbleView.3
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                BubbleView.this.balls.remove(oval);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }
        });
        ValueAnimator colorAnim = ValueAnimator.ofInt(oval.getAlpha(), 0);
        colorAnim.setDuration(ChargeHandler.TIMEOUT_CHARGE_FIN);
        colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.BubbleView.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.paint.setColor(Color.argb(((Integer) animation.getAnimatedValue()).intValue(), 0, 206, 219));
            }
        });
        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.playTogether(widthAnim, colorAnim);
        ValueAnimator tranlateAnim = ValueAnimator.ofInt((center * 2) + 20, oval.getMaxY());
        tranlateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.BubbleView.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.setY(((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        tranlateAnim.setDuration(1500L);
        tranlateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(tranlateAnim, animatorSet1);
        animatorSet.start();
    }

    public Oval addOval() {
        Oval oval = new Oval();
        this.balls.add(oval);
        return oval;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Oval {
        private int alpha;
        private int color;
        private int maxY;
        float radius;
        private int x;
        private int y;
        private float borderWidth = 1.5f;
        public Paint paint = new Paint();

        public Oval() {
            this.color = 1711328987;
            this.paint.setColor(this.color);
            this.paint.setStrokeWidth(this.borderWidth);
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setAntiAlias(true);
            Random random = new Random();
            this.x = random.nextInt(BubbleView.center * 2) + 1;
            this.maxY = random.nextInt(BubbleView.center) + BubbleView.center;
            this.alpha = random.nextInt(TransportMediator.KEYCODE_MEDIA_PAUSE) + TransportMediator.KEYCODE_MEDIA_PAUSE;
            this.color = Color.argb(this.alpha, 0, 206, 219);
            this.radius = BubbleView.maxWidth;
        }

        public int getAlpha() {
            return this.alpha;
        }

        public int getMaxY() {
            return this.maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public float getRadius() {
            return this.radius;
        }

        public int getColor() {
            return this.color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public float getBorderWidth() {
            return this.borderWidth;
        }

        public void setBorderWidth(float borderWidth) {
            this.borderWidth = borderWidth;
        }
    }

    public void destroy() {
        this.balls.clear();
    }
}
