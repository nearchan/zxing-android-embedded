package com.journeyapps.barcodescanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.google.zxing.client.android.R;

/**
 * Created by Administrator on 2017/10/25.
 */
public class ScanBarcodeView extends BarcodeView {
    public ScanBarcodeView(Context context) {
        super(context);

        intAttrs(null);
    }

    public ScanBarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        intAttrs(attrs);
    }

    public ScanBarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        intAttrs(attrs);
    }

    float framingRectWidth;
    private void intAttrs(AttributeSet attrs) {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_camera_preview);
        framingRectWidth = styledAttributes.getFloat(R.styleable.zxing_camera_preview_zxing_framing_center_vertical_percent, 0.33f);
        styledAttributes.recycle();
    }

    @Override
    protected Rect calculateFramingRect(Rect container, Rect surface) {
        Rect intersection =  super.calculateFramingRect(container, surface);
        intersection.offset(0, (int) (container.height() * (framingRectWidth - 0.5f)));

        return intersection;
    }
}
