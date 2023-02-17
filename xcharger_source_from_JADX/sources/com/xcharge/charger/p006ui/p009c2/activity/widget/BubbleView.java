package com.xcharge.charger.p006ui.p009c2.activity.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.p000v4.media.TransportMediator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.xcharge.charger.core.handler.ChargeHandler;
import java.util.ArrayList;
import java.util.Random;

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.BubbleView */
public class BubbleView extends View {
    static int center = 384;
    static int maxWidth = 14;
    public final ArrayList<Oval> balls = new ArrayList<>();
    Paint paint;

    public BubbleView(Context context) {
        super(context.getApplicationContext(), (AttributeSet) null);
    }

    public BubbleView(Context context, AttributeSet attributeSet) {
        super(context.getApplicationContext(), attributeSet);
        init();
    }

    private void init() {
        this.paint = new Paint();
        this.paint.setColor(1728053247);
        this.paint.setStrokeWidth(3.0f);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setAntiAlias(true);
        ValueAnimator colorAnim = ValueAnimator.ofInt(new int[]{0, 1});
        colorAnim.setDuration(3000);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(-1);
        colorAnim.setRepeatMode(2);
        colorAnim.start();
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                BubbleView.this.invalidate();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < this.balls.size(); i++) {
            Oval oval = this.balls.get(i);
            canvas.drawCircle((float) oval.getX(), (float) oval.getY(), oval.getRadius(), oval.paint);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != 0 && event.getAction() != 2) {
            return false;
        }
        start(addOval());
        return true;
    }

    public void start(final Oval oval) {
        ValueAnimator widthAnim = ValueAnimator.ofInt(new int[]{maxWidth, 0});
        widthAnim.setDuration(3000);
        widthAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.setRadius((float) ((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        widthAnim.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                BubbleView.this.balls.remove(oval);
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        ValueAnimator colorAnim = ValueAnimator.ofInt(new int[]{oval.getAlpha(), 0});
        colorAnim.setDuration(ChargeHandler.TIMEOUT_CHARGE_FIN);
        colorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.paint.setColor(Color.argb(((Integer) animation.getAnimatedValue()).intValue(), 0, 206, 219));
            }
        });
        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.playTogether(new Animator[]{widthAnim, colorAnim});
        ValueAnimator tranlateAnim = ValueAnimator.ofInt(new int[]{(center * 2) + 20, oval.getMaxY()});
        tranlateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                oval.setY(((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        tranlateAnim.setDuration(1500);
        tranlateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{tranlateAnim, animatorSet1});
        animatorSet.start();
    }

    public Oval addOval() {
        Oval oval = new Oval();
        this.balls.add(oval);
        return oval;
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.widget.BubbleView$Oval */
    static class Oval {
        private int alpha;
        private float borderWidth = 1.5f;
        private int color = 1711328987;
        private int maxY;
        public Paint paint = new Paint();
        float radius;

        /* renamed from: x */
        private int f131x;

        /* renamed from: y */
        private int f132y;

        public Oval() {
            this.paint.setColor(this.color);
            this.paint.setStrokeWidth(this.borderWidth);
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setAntiAlias(true);
            Random random = new Random();
            this.f131x = random.nextInt(BubbleView.center * 2) + 1;
            this.maxY = random.nextInt(BubbleView.center) + BubbleView.center;
            this.alpha = random.nextInt(TransportMediator.KEYCODE_MEDIA_PAUSE) + TransportMediator.KEYCODE_MEDIA_PAUSE;
            this.color = Color.argb(this.alpha, 0, 206, 219);
            this.radius = (float) BubbleView.maxWidth;
        }

        public int getAlpha() {
            return this.alpha;
        }

        public int getMaxY() {
            return this.maxY;
        }

        public void setMaxY(int maxY2) {
            this.maxY = maxY2;
        }

        public void setRadius(float radius2) {
            this.radius = radius2;
        }

        public int getX() {
            return this.f131x;
        }

        public void setX(int x) {
            this.f131x = x;
        }

        public int getY() {
            return this.f132y;
        }

        public void setY(int y) {
            this.f132y = y;
        }

        public float getRadius() {
            return this.radius;
        }

        public int getColor() {
            return this.color;
        }

        public void setColor(int color2) {
            this.color = color2;
        }

        public float getBorderWidth() {
            return this.borderWidth;
        }

        public void setBorderWidth(float borderWidth2) {
            this.borderWidth = borderWidth2;
        }
    }

    public void destroy() {
        this.balls.clear();
    }
}
