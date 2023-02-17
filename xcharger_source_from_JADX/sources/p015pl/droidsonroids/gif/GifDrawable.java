package p015pl.droidsonroids.gif;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.MediaController;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

/* renamed from: pl.droidsonroids.gif.GifDrawable */
public class GifDrawable extends Drawable implements Animatable, MediaController.MediaPlayerControl {
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private boolean mApplyTransformation;
    /* access modifiers changed from: private */
    public int[] mColors;
    private final Rect mDstRect;
    /* access modifiers changed from: private */
    public volatile int mGifInfoPtr;
    private final long mInputSourceLength;
    private final Runnable mInvalidateTask;
    private volatile boolean mIsRunning;
    private final int[] mMetaData;
    protected final Paint mPaint;
    private final Runnable mResetTask;
    private final Runnable mSaveRemainderTask;
    private final Runnable mStartTask;
    private float mSx;
    private float mSy;

    private static native void free(int i);

    private static native long getAllocationByteCount(int i);

    private static native String getComment(int i);

    private static native int getCurrentPosition(int i);

    private static native int getDuration(int i);

    private static native int getLoopCount(int i);

    private static native int openByteArray(int[] iArr, byte[] bArr) throws GifIOException;

    private static native int openDirectByteBuffer(int[] iArr, ByteBuffer byteBuffer) throws GifIOException;

    private static native int openFd(int[] iArr, FileDescriptor fileDescriptor, long j) throws GifIOException;

    private static native int openFile(int[] iArr, String str) throws GifIOException;

    private static native int openStream(int[] iArr, InputStream inputStream) throws GifIOException;

    private static native void renderFrame(int[] iArr, int i, int[] iArr2);

    /* access modifiers changed from: private */
    public static native boolean reset(int i);

    /* access modifiers changed from: private */
    public static native int restoreRemainder(int i);

    /* access modifiers changed from: private */
    public static native int saveRemainder(int i);

    /* access modifiers changed from: private */
    public static native int seekToFrame(int i, int i2, int[] iArr);

    /* access modifiers changed from: private */
    public static native int seekToTime(int i, int i2, int[] iArr);

    private static native void setSpeedFactor(int i, float f);

    static {
        System.loadLibrary("gif");
    }

    private static void runOnUiThread(Runnable task) {
        if (Looper.myLooper() == UI_HANDLER.getLooper()) {
            task.run();
        } else {
            UI_HANDLER.post(task);
        }
    }

    public GifDrawable(Resources res, int id) throws Resources.NotFoundException, IOException {
        this(res.openRawResourceFd(id));
    }

    public GifDrawable(AssetManager assets, String assetName) throws IOException {
        this(assets.openFd(assetName));
    }

    public GifDrawable(String filePath) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (filePath == null) {
            throw new NullPointerException("Source is null");
        }
        this.mInputSourceLength = new File(filePath).length();
        this.mGifInfoPtr = openFile(this.mMetaData, filePath);
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
    }

    public GifDrawable(File file) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (file == null) {
            throw new NullPointerException("Source is null");
        }
        this.mInputSourceLength = file.length();
        this.mGifInfoPtr = openFile(this.mMetaData, file.getPath());
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
    }

    public GifDrawable(InputStream stream) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (stream == null) {
            throw new NullPointerException("Source is null");
        } else if (!stream.markSupported()) {
            throw new IllegalArgumentException("InputStream does not support marking");
        } else {
            this.mGifInfoPtr = openStream(this.mMetaData, stream);
            this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
            this.mInputSourceLength = -1;
        }
    }

    public GifDrawable(AssetFileDescriptor afd) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (afd == null) {
            throw new NullPointerException("Source is null");
        }
        try {
            this.mGifInfoPtr = openFd(this.mMetaData, afd.getFileDescriptor(), afd.getStartOffset());
            this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
            this.mInputSourceLength = afd.getLength();
        } catch (IOException ex) {
            afd.close();
            throw ex;
        }
    }

    public GifDrawable(FileDescriptor fd) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (fd == null) {
            throw new NullPointerException("Source is null");
        }
        this.mGifInfoPtr = openFd(this.mMetaData, fd, 0);
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
        this.mInputSourceLength = -1;
    }

    public GifDrawable(byte[] bytes) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (bytes == null) {
            throw new NullPointerException("Source is null");
        }
        this.mGifInfoPtr = openByteArray(this.mMetaData, bytes);
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
        this.mInputSourceLength = (long) bytes.length;
    }

    public GifDrawable(ByteBuffer buffer) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() {
            public void run() {
                boolean unused = GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() {
            public void run() {
                int unused = GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() {
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (buffer == null) {
            throw new NullPointerException("Source is null");
        } else if (!buffer.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer is not direct");
        } else {
            this.mGifInfoPtr = openDirectByteBuffer(this.mMetaData, buffer);
            this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
            this.mInputSourceLength = (long) buffer.capacity();
        }
    }

    public GifDrawable(ContentResolver resolver, Uri uri) throws IOException {
        this(resolver.openAssetFileDescriptor(uri, "r"));
    }

    public void recycle() {
        this.mIsRunning = false;
        int tmpPtr = this.mGifInfoPtr;
        this.mGifInfoPtr = 0;
        this.mColors = null;
        free(tmpPtr);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    public int getIntrinsicHeight() {
        return this.mMetaData[1];
    }

    public int getIntrinsicWidth() {
        return this.mMetaData[0];
    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -2;
    }

    public void start() {
        this.mIsRunning = true;
        runOnUiThread(this.mStartTask);
    }

    public void reset() {
        runOnUiThread(this.mResetTask);
    }

    public void stop() {
        this.mIsRunning = false;
        runOnUiThread(this.mSaveRemainderTask);
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    public String getComment() {
        return getComment(this.mGifInfoPtr);
    }

    public int getLoopCount() {
        return getLoopCount(this.mGifInfoPtr);
    }

    public String toString() {
        return String.format(Locale.US, "Size: %dx%d, %d frames, error: %d", new Object[]{Integer.valueOf(this.mMetaData[0]), Integer.valueOf(this.mMetaData[1]), Integer.valueOf(this.mMetaData[2]), Integer.valueOf(this.mMetaData[3])});
    }

    public int getNumberOfFrames() {
        return this.mMetaData[2];
    }

    public GifError getError() {
        return GifError.fromCode(this.mMetaData[3]);
    }

    public static GifDrawable createFromResource(Resources res, int resourceId) {
        try {
            return new GifDrawable(res, resourceId);
        } catch (IOException e) {
            return null;
        }
    }

    public void setSpeed(float factor) {
        if (factor <= 0.0f) {
            throw new IllegalArgumentException("Speed factor is not positive");
        }
        setSpeedFactor(this.mGifInfoPtr, factor);
    }

    public void pause() {
        stop();
    }

    public int getDuration() {
        return getDuration(this.mGifInfoPtr);
    }

    public int getCurrentPosition() {
        return getCurrentPosition(this.mGifInfoPtr);
    }

    public void seekTo(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position is not positive");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                int unused = GifDrawable.seekToTime(GifDrawable.this.mGifInfoPtr, position, GifDrawable.this.mColors);
                GifDrawable.this.invalidateSelf();
            }
        });
    }

    public void seekToFrame(final int frameIndex) {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex is not positive");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                int unused = GifDrawable.seekToFrame(GifDrawable.this.mGifInfoPtr, frameIndex, GifDrawable.this.mColors);
                GifDrawable.this.invalidateSelf();
            }
        });
    }

    public boolean isPlaying() {
        return this.mIsRunning;
    }

    public int getBufferPercentage() {
        return 100;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return getNumberOfFrames() > 1;
    }

    public int getAudioSessionId() {
        return 0;
    }

    public int getFrameByteCount() {
        return this.mMetaData[0] * this.mMetaData[1] * 4;
    }

    public long getAllocationByteCount() {
        long nativeSize = getAllocationByteCount(this.mGifInfoPtr);
        int[] colors = this.mColors;
        return colors == null ? nativeSize : nativeSize + (((long) colors.length) * 4);
    }

    public long getInputSourceByteCount() {
        return this.mInputSourceLength;
    }

    public void getPixels(int[] pixels) {
        int[] colors = this.mColors;
        if (colors != null) {
            if (pixels.length < colors.length) {
                throw new ArrayIndexOutOfBoundsException("Pixels array is too small. Required length: " + colors.length);
            }
            System.arraycopy(colors, 0, pixels, 0, colors.length);
        }
    }

    public int getPixel(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        } else if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        } else if (x >= this.mMetaData[0]) {
            throw new IllegalArgumentException("x must be < GIF width");
        } else if (y >= this.mMetaData[1]) {
            throw new IllegalArgumentException("y must be < GIF height");
        } else {
            int[] colors = this.mColors;
            if (colors != null) {
                return colors[(this.mMetaData[1] * y) + x];
            }
            throw new IllegalArgumentException("GifDrawable is recycled");
        }
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mApplyTransformation = true;
    }

    public void draw(Canvas canvas) {
        if (this.mApplyTransformation) {
            this.mDstRect.set(getBounds());
            this.mSx = ((float) this.mDstRect.width()) / ((float) this.mMetaData[0]);
            this.mSy = ((float) this.mDstRect.height()) / ((float) this.mMetaData[1]);
            this.mApplyTransformation = false;
        }
        if (this.mPaint.getShader() == null) {
            if (this.mIsRunning) {
                renderFrame(this.mColors, this.mGifInfoPtr, this.mMetaData);
            } else {
                this.mMetaData[4] = -1;
            }
            canvas.scale(this.mSx, this.mSy);
            int[] colors = this.mColors;
            if (colors != null) {
                canvas.drawBitmap(colors, 0, this.mMetaData[0], 0.0f, 0.0f, this.mMetaData[0], this.mMetaData[1], true, this.mPaint);
            }
            if (this.mMetaData[4] >= 0 && this.mMetaData[2] > 1) {
                UI_HANDLER.postDelayed(this.mInvalidateTask, (long) this.mMetaData[4]);
                return;
            }
            return;
        }
        canvas.drawRect(this.mDstRect, this.mPaint);
    }

    public final Paint getPaint() {
        return this.mPaint;
    }

    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    public void setFilterBitmap(boolean filter) {
        this.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    public void setDither(boolean dither) {
        this.mPaint.setDither(dither);
        invalidateSelf();
    }

    public int getMinimumHeight() {
        return this.mMetaData[1];
    }

    public int getMinimumWidth() {
        return this.mMetaData[0];
    }
}
