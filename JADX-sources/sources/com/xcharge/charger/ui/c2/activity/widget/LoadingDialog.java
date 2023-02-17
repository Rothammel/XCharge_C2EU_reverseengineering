package com.xcharge.charger.ui.c2.activity.widget;

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
import com.xcharge.charger.R;

/* loaded from: classes.dex */
public class LoadingDialog extends Dialog {
    private final int MSG_DISMISS_DIALOG;
    private final int MSG_SHOW_DIALOG;
    AnimatorSet animatorSet;
    private Context context;
    private ImageView iv_point_1;
    private ImageView iv_point_2;
    private ImageView iv_point_3;
    private Handler mHandler;
    private TextView tv_loading_text;
    private int width;

    private LoadingDialog(Context context, String loadingText) {
        super(context, R.style.CustomDialog);
        this.MSG_SHOW_DIALOG = 1;
        this.MSG_DISMISS_DIALOG = 2;
        this.mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.widget.LoadingDialog.1
            @Override // android.os.Handler
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
        this.context = context;
        init(loadingText);
    }

    public static LoadingDialog createDialog(Context context, String loadingText) {
        return new LoadingDialog(context, loadingText);
    }

    private void init(String loadingText) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.dialog_loading_view, (ViewGroup) null);
        setContentView(view);
        this.tv_loading_text = (TextView) view.findViewById(R.id.tv_loading_text);
        this.iv_point_1 = (ImageView) view.findViewById(R.id.iv_point_1);
        this.iv_point_2 = (ImageView) view.findViewById(R.id.iv_point_2);
        this.iv_point_3 = (ImageView) view.findViewById(R.id.iv_point_3);
        this.tv_loading_text.setText(new StringBuilder(String.valueOf(loadingText)).toString());
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) this.context.getResources().getDimension(R.dimen.dialog_loading_width);
        lp.height = (int) this.context.getResources().getDimension(R.dimen.dialog_loading_height);
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
        if (length * 32.0f > maxWidth) {
            float newTextSize = ((maxWidth - 40.0f) * 1.0f) / length;
            this.tv_loading_text.setTextSize(newTextSize);
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

    @Override // android.app.Dialog
    public void show() {
        super.show();
        if (this.animatorSet == null) {
            startAnim();
        } else {
            this.animatorSet.start();
        }
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    @SuppressLint({"NewApi"})
    public void dismiss() {
        super.dismiss();
        if (this.animatorSet != null) {
            this.animatorSet.end();
        }
        this.animatorSet = null;
    }

    private AnimatorSet createAnim(final View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator animLonger = ValueAnimator.ofInt(16, 30);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.LoadingDialog.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.height = ((Integer) animation.getAnimatedValue()).intValue();
                view.setLayoutParams(lp);
            }
        };
        animLonger.addUpdateListener(animatorUpdateListener);
        animLonger.setDuration(400L);
        animLonger.setInterpolator(new AccelerateDecelerateInterpolator());
        ValueAnimator animShorter = ValueAnimator.ofInt(30, 16);
        animShorter.addUpdateListener(animatorUpdateListener);
        animShorter.setDuration(400L);
        animShorter.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playSequentially(animLonger, animShorter);
        return animatorSet;
    }

    private void startAnim() {
        this.animatorSet = new AnimatorSet();
        AnimatorSet anim1 = createAnim(this.iv_point_1);
        AnimatorSet anim2 = createAnim(this.iv_point_2);
        anim2.setStartDelay(350L);
        AnimatorSet anim3 = createAnim(this.iv_point_3);
        anim3.setStartDelay(750L);
        this.animatorSet.playTogether(anim1, anim2, anim3);
        this.animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.LoadingDialog.3
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animation) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                if (LoadingDialog.this.animatorSet != null) {
                    LoadingDialog.this.animatorSet.start();
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
            }
        });
        this.animatorSet.start();
    }

    @Override // android.app.Dialog
    public void onBackPressed() {
    }
}
