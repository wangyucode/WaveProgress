package cn.wycode.waveprogress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static android.graphics.PixelFormat.TRANSLUCENT;

/**
 * 自定义形状波浪进度
 * Created by wycode.cn on 2017/7/24.
 */
public class ImageWaveProgress extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Paint mPaint;

    private SurfaceHolder mHolder;
    //子线程标志位
    private boolean mIsDrawing;

    int w; //画布宽
    int h; //画布高


    private Bitmap bitmap;

    private int progress;
    private Rect rectCanvas;
    private Rect rectBitmap;

    private int tintColor = Color.GREEN;
    private int untintColor = Color.WHITE;

    int calculateSize = 100;
    int waveLength = 16;
    int waveHeightMax = 8;
    int waveSpeed = 10;

    public ImageWaveProgress(Context context) {
        super(context);
        init();
    }

    public ImageWaveProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageWaveProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(60);

        mHolder = getHolder();
        setZOrderOnTop(true);
        mHolder.setFormat(TRANSLUCENT);
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        bitmap = setBitmapSize(bitmap, calculateSize * bitmap.getWidth() / bitmap.getHeight(), calculateSize);


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        rectCanvas = new Rect(0, 0, width, height);
        rectBitmap = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            draw();
            processBitmap();
        }
    }

    private void processBitmap() {
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int color = bitmap.getPixel(i, j);
                if (color >>> 24 > 0) {
                    int y = getWaveY(i);
                    if (j > y) {
                        bitmap.setPixel(i, j, tintColor);
                    } else {
                        bitmap.setPixel(i, j, untintColor);
                    }
                }
            }
        }
    }

    private int getWaveY(int x) {
        double sinX = Math.PI + (((((System.currentTimeMillis() / (1000 / waveSpeed)) + x) % waveLength)) / (double) waveLength * Math.PI);
        int waveHeight = (int) (waveHeightMax * (Math.sin(sinX) + 1));
        int y = (int) ((100 - progress) / 100f * bitmap.getHeight());
        y -= waveHeight;
        return y;
    }


    private void draw() {
        Canvas canvas = mHolder.lockCanvas();
        canvas.drawBitmap(bitmap, rectBitmap, rectCanvas, mPaint);
        mHolder.unlockCanvasAndPost(canvas);
    }

    public void setDrawableResource(@DrawableRes int id) {
        bitmap = BitmapFactory.decodeResource(getResources(), id);
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * 缩放图片
     *
     * @param bitmap    原图片
     * @param newWidth
     * @param newHeight
     * @return
     */
    private Bitmap setBitmapSize(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (newWidth * 1.0f) / width;
        float scaleHeight = (newHeight * 1.0f) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}