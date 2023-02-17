package com.xcharge.charger.p006ui.p009c2.activity.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.LoadingDialog */
public class LoadingDialog extends Dialog {
    private final int MSG_DISMISS_DIALOG = 2;
    private final int MSG_SHOW_DIALOG = 1;
    AnimatorSet animatorSet;
    private Context context;
    private ImageView iv_point_1;
    private ImageView iv_point_2;
    private ImageView iv_point_3;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!LoadingDialog.this.isShowing()) {
                        LoadingDialog.this.show();
                        return;
                    }
                    return;
                case 2:
                    LoadingDialog.this.dismiss();
                    return;
                default:
                    return;
            }
        }
    };
    private TextView tv_loading_text;
    private int width;

    private LoadingDialog(Context context2, String loadingText) {
        super(context2, C0221R.style.CustomDialog);
        this.context = context2;
        init(loadingText);
    }

    public static LoadingDialog createDialog(Context context2, String loadingText) {
        return new LoadingDialog(context2, loadingText);
    }

    private void init(String loadingText) {
        View view = LayoutInflater.from(this.context).inflate(C0221R.layout.dialog_loading_view, (ViewGroup) null);
        setContentView(view);
        this.tv_loading_text = (TextView) view.findViewById(C0221R.C0223id.tv_loading_text);
        this.iv_point_1 = (ImageView) view.findViewById(C0221R.C0223id.iv_point_1);
        this.iv_point_2 = (ImageView) view.findViewById(C0221R.C0223id.iv_point_2);
        this.iv_point_3 = (ImageView) view.findViewById(C0221R.C0223id.iv_point_3);
        this.tv_loading_text.setText(new StringBuilder(String.valueOf(loadingText)).toString());
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) this.context.getResources().getDimension(C0221R.dimen.dialog_loading_width);
        lp.height = (int) this.context.getResources().getDimension(C0221R.dimen.dialog_loading_height);
        this.width = lp.width;
        lp.gravity = 17;
        window.setBackgroundDrawableResource(17170445);
        window.setAttributes(lp);
    }

    private void refitText(String text, int textWidth) {
        if (textWidth > 0) {
            Paint testPaint = new Paint();
            testPaint.set(this.tv_loading_text.getPaint());
            int availableWidth = (textWidth - this.tv_loading_text.getPaddingLeft()) - this.tv_loading_text.getPaddingRight();
            float[] widths = new float[text.length()];
            Rect rect = new Rect();
            testPaint.getTextBounds(text, 0, text.length(), rect);
            int textWidths = rect.width();
            float cTextSize = this.tv_loading_text.getTextSize();
            while (textWidths > availableWidth) {
                cTextSize -= 1.0f;
                testPaint.setTextSize(cTextSize);
                textWidths = testPaint.getTextWidths(text, widths);
            }
            this.tv_loading_text.setTextSize(0, cTextSize);
        }
    }

    private void autoSetTextSize(String text, int maxWidth, int length) {
        if (((float) length) * 32.0f > ((float) maxWidth)) {
            this.tv_loading_text.setTextSize(((((float) maxWidth) - 40.0f) * 1.0f) / ((float) length));
            this.tv_loading_text.setText(text);
            this.tv_loading_text.invalidate();
            return;
        }
        this.tv_loading_text.setText(text);
    }

    public void dissmissByNotUIThread() {
        this.mHandler.sendEmptyMessage(2);
    }

    public void showByNotUIThread() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void changeLoadingText(String text) {
        this.tv_loading_text.setText(text);
    }

    public void show() {
        super.show();
        if (this.animatorSet == null) {
            startAnim();
        } else {
            this.animatorSet.start();
        }
    }

    @SuppressLint({"NewApi"})
    public void dismiss() {
        super.dismiss();
        if (this.animatorSet != null) {
            this.animatorSet.end();
        }
        this.animatorSet = null;
    }

    private AnimatorSet createAnim(final View view) {
        AnimatorSet animatorSet2 = new AnimatorSet();
        ValueAnimator animLonger = ValueAnimator.ofInt(new int[]{16, 30});
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.height = ((Integer) animation.getAnimatedValue()).intValue();
                view.setLayoutParams(lp);
            }
        };
        animLonger.addUpdateListener(animatorUpdateListener);
        animLonger.setDuration(400);
        animLonger.setInterpolator(new AccelerateDecelerateInterpolator());
        ValueAnimator animShorter = ValueAnimator.ofInt(new int[]{30, 16});
        animShorter.addUpdateListener(animatorUpdateListener);
        animShorter.setDuration(400);
        animShorter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet2.playSequentially(new Animator[]{animLonger, animShorter});
        return animatorSet2;
    }

    private void startAnim() {
        this.animatorSet = new AnimatorSet();
        AnimatorSet anim1 = createAnim(this.iv_point_1);
        AnimatorSet anim2 = createAnim(this.iv_point_2);
        anim2.setStartDelay(350);
        AnimatorSet anim3 = createAnim(this.iv_point_3);
        anim3.setStartDelay(750);
        this.animatorSet.playTogether(new Animator[]{anim1, anim2, anim3});
        this.animatorSet.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (LoadingDialog.this.animatorSet != null) {
                    LoadingDialog.this.animatorSet.start();
                }
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        this.animatorSet.start();
    }

    public void onBackPressed() {
    }
}
