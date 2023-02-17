package com.xcharge.charger.ui.c2.activity.widget;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import com.xcharge.charger.R;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class KeyboardUtil {
    public static KeyboardUtil mInstance;
    private Activity activity;
    private EditText et_click;
    private EditText et_visual;
    private Keyboard k1;
    private Keyboard k2;
    private KeyboardView keyboardView;
    public boolean isUpper = false;
    public boolean isShow = false;
    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.KeyboardUtil.1
        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void swipeUp() {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void swipeRight() {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void swipeLeft() {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void swipeDown() {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void onText(CharSequence text) {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void onRelease(int primaryCode) {
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void onPress(int primaryCode) {
            checkIShowPrewiew(primaryCode);
        }

        private void checkIShowPrewiew(int primaryCode) {
            List<Integer> list = Arrays.asList(-3, -5, -1, 46, 32, 44);
            if (list.contains(Integer.valueOf(primaryCode))) {
                KeyboardUtil.this.keyboardView.setPreviewEnabled(false);
            } else {
                KeyboardUtil.this.keyboardView.setPreviewEnabled(true);
            }
        }

        @Override // android.inputmethodservice.KeyboardView.OnKeyboardActionListener
        public void onKey(int primaryCode, int[] keyCodes) {
            checkIShowPrewiew(primaryCode);
            Log.i("KeyBoard", "primaryCode=" + primaryCode);
            Editable editable = KeyboardUtil.this.et_visual.getText();
            int start = KeyboardUtil.this.et_visual.getSelectionStart();
            if (primaryCode == -3) {
                KeyboardUtil.this.hideKeyboard();
            } else if (primaryCode == -5) {
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
            } else if (primaryCode != -1) {
                if (primaryCode == 1000) {
                    KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.k2);
                } else if (primaryCode == 1001) {
                    KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.k1);
                } else if (primaryCode == 1003) {
                    editable.insert(start, StringUtils.SPACE);
                } else if (primaryCode == 1002) {
                    if (start > 0) {
                        KeyboardUtil.this.et_visual.setSelection(start - 1);
                    }
                } else if (primaryCode == 1004) {
                    if (start < KeyboardUtil.this.et_visual.length()) {
                        KeyboardUtil.this.et_visual.setSelection(start + 1);
                    }
                } else {
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            } else {
                KeyboardUtil.this.changeKey();
                KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.k1);
            }
        }
    };

    public KeyboardUtil(Activity activity, EditText editText) {
        this.activity = activity;
        this.et_click = editText;
        this.k1 = new Keyboard(activity, R.xml.letter_softkeyboard);
        this.k2 = new Keyboard(activity, R.xml.character_softkeyboard);
        this.keyboardView = (KeyboardView) activity.findViewById(R.id.keyboard_view);
        this.keyboardView.setKeyboard(this.k1);
        this.keyboardView.setEnabled(true);
        this.keyboardView.setOnKeyboardActionListener(this.listener);
        this.et_visual = (EditText) activity.findViewById(R.id.et_visual);
        this.et_visual.setOnTouchListener(new View.OnTouchListener() { // from class: com.xcharge.charger.ui.c2.activity.widget.KeyboardUtil.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View arg0, MotionEvent arg1) {
                int inputType = KeyboardUtil.this.et_visual.getInputType() | 524288;
                KeyboardUtil.this.et_visual.setInputType(0);
                KeyboardUtil.this.et_visual.setInputType(inputType);
                KeyboardUtil.this.et_visual.setSelection(KeyboardUtil.this.et_visual.getText().toString().length());
                return false;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeKey() {
        List<Keyboard.Key> keylist = this.k1.getKeys();
        if (this.isUpper) {
            this.isUpper = false;
            for (Keyboard.Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                } else if (key.label.toString().equals(this.activity.getString(R.string.keyboard_lower_case))) {
                    key.label = this.activity.getString(R.string.keyboard_upper_case);
                }
            }
            return;
        }
        this.isUpper = true;
        for (Keyboard.Key key2 : keylist) {
            if (key2.label != null && isword(key2.label.toString())) {
                key2.label = key2.label.toString().toUpperCase();
                key2.codes[0] = key2.codes[0] - 32;
            } else if (key2.label.toString().equals(this.activity.getString(R.string.keyboard_upper_case))) {
                key2.label = this.activity.getString(R.string.keyboard_lower_case);
            }
        }
    }

    public void showKeyboard() {
        int visibility = this.keyboardView.getVisibility();
        if (visibility == 8 || visibility == 4) {
            int inputType = this.et_visual.getInputType() | 524288;
            this.et_visual.setInputType(0);
            this.keyboardView.setVisibility(0);
            this.et_visual.setVisibility(0);
            this.et_visual.setInputType(inputType);
            this.isShow = true;
        }
    }

    public void hideKeyboard() {
        int visibility = this.keyboardView.getVisibility();
        if (visibility == 0) {
            this.keyboardView.setVisibility(8);
            this.et_visual.setVisibility(8);
            this.et_click.setText(this.et_visual.getText().toString());
            this.et_visual.setText((CharSequence) null);
            this.isShow = false;
        }
    }

    private boolean isword(String str) {
        return "abcdefghijklmnopqrstuvwxyz".indexOf(str.toLowerCase()) > -1;
    }

    public static KeyboardUtil shared(Activity activity, EditText editText) {
        if (mInstance == null) {
            mInstance = new KeyboardUtil(activity, editText);
        }
        mInstance.et_click = editText;
        mInstance.et_visual.setFocusable(true);
        mInstance.et_visual.setFocusableInTouchMode(true);
        mInstance.et_visual.requestFocus();
        String text = editText.getText().toString();
        mInstance.et_visual.setText(text);
        mInstance.et_visual.setSelection(text.length());
        return mInstance;
    }

    public static KeyboardUtil getmInstance() {
        return mInstance;
    }

    public static void setmInstance(KeyboardUtil mInstance2) {
        mInstance = mInstance2;
    }
}
