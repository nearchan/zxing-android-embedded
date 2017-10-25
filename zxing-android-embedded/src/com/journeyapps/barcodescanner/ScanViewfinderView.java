package com.journeyapps.barcodescanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：梦想编织者灬小楠
 * 链接：http://www.jianshu.com/p/b85812b6f7c1
 * 源：简书
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */

public class ScanViewfinderView extends ViewfinderView {

    /**
     * 重绘时间间隔
     */
    public static final long CUSTOME_ANIMATION_DELAY = 16;

    /* ******************************************    边角线相关属性    ************************************************/

    /**
     * "边角线长度/扫描边框长度"的占比 (比例越大，线越长)
     */
    public float mLineLength = 0;

    /**
     * 边角线厚度 (建议使用dp)
     */
    public float mLineDepth = 0;

    /**
     * 边角线颜色
     */
    public int mLineColor = Color.WHITE;

    /* *******************************************    扫描线相关属性    ************************************************/

    /**
     * 扫描线起始位置
     */
    public int mScanLinePosition = 0;

    /**
     * 扫描线厚度
     */
    public float mScanLineDepth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

    /**
     * 扫描线每次重绘的移动距离
     */
    public float mScanLineDy = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());

    /**
     * 线性梯度
     */
    public LinearGradient mLinearGradient;

    /**
     * 线性梯度位置
     */
    public float[] mPositions = new float[]{0f, 0.5f, 1f};

    /**
     * 线性梯度各个位置对应的颜色值
     */
    public int[] mScanLineColor = new int[]{0x00FFFFFF, Color.WHITE, 0x00FFFFFF};


    public ScanViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder);

        this.mLineColor = attributes.getColor(R.styleable.zxing_finder_zxing_framing_stroke_color,
                Color.rgb(0x00, 0x99, 0xCC));
        float defaultDepth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        this.mLineDepth = attributes.getDimension(R.styleable.zxing_finder_zxing_framing_stroke_depth, defaultDepth);
        float defaultLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 23, getResources().getDisplayMetrics());
        this.mLineLength = attributes.getDimension(R.styleable.zxing_finder_zxing_framing_stroke_length, defaultLength);

        attributes.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }

        Rect frame = framingRect;
        Rect previewFrame = previewFramingRect;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //绘制4个角
        paint.setColor(mLineColor); // 定义画笔的颜色
        canvas.drawRect(frame.left, frame.top, frame.left + mLineLength, frame.top + mLineDepth, paint);
        canvas.drawRect(frame.left, frame.top, frame.left + mLineDepth, frame.top + mLineLength, paint);

        canvas.drawRect(frame.right - mLineLength, frame.top, frame.right, frame.top + mLineDepth, paint);
        canvas.drawRect(frame.right - mLineDepth, frame.top, frame.right, frame.top + mLineLength, paint);

        canvas.drawRect(frame.left, frame.bottom - mLineDepth, frame.left + mLineLength, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - mLineLength, frame.left + mLineDepth, frame.bottom, paint);

        canvas.drawRect(frame.right - mLineLength, frame.bottom - mLineDepth, frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - mLineDepth, frame.bottom - mLineLength, frame.right, frame.bottom, paint);

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            // 绘制扫描线
            mScanLinePosition += mScanLineDy;
            if(mScanLinePosition > frame.height()){
                mScanLinePosition = 0;
            }
            mLinearGradient = new LinearGradient(frame.left, frame.top + mScanLinePosition, frame.right, frame.top + mScanLinePosition, mScanLineColor, mPositions, Shader.TileMode.CLAMP);
            paint.setShader(mLinearGradient);
            canvas.drawRect(frame.left, frame.top + mScanLinePosition, frame.right, frame.top + mScanLinePosition + mScanLineDepth, paint);
            paint.setShader(null);

            float scaleX = frame.width() / (float) previewFrame.width();
            float scaleY = frame.height() / (float) previewFrame.height();

            List<ResultPoint> currentPossible = possibleResultPoints;
            List<ResultPoint> currentLast = lastPossibleResultPoints;
            int frameLeft = frame.left;
            int frameTop = frame.top;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new ArrayList<>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint);
                }
            }
        }

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(CUSTOME_ANIMATION_DELAY,
                frame.left,
                frame.top,
                frame.right,
                frame.bottom);
    }
}