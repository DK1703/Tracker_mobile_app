package com.example.my_application;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class TiredImageView extends AppCompatImageView {

    private int fatigueLevel;
    private Bitmap maskBitmap;

    public TiredImageView(Context context) {
        super(context);
        init(null);
    }

    public TiredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TiredImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TiredImageView);
            fatigueLevel = a.getInt(R.styleable.TiredImageView_fatigueLevel, 0);
            a.recycle();
        }

        setLayerType(LAYER_TYPE_HARDWARE, null);
        maskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.human); // Замените на имя вашей маски
        updateTiredImage();
    }

    public void setFatigueLevel(int fatigueLevel) {
        this.fatigueLevel = fatigueLevel;
        updateTiredImage();
    }

    private void updateTiredImage() {
        Bitmap baseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.human);
        Bitmap tiredBitmap = Bitmap.createBitmap(baseBitmap.getWidth(), baseBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(tiredBitmap);
        canvas.drawBitmap(baseBitmap, 0, 0, null);

        int heightToDarken = (int) (baseBitmap.getHeight() * (fatigueLevel / 100.0));

        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(150); // Прозрачность тени, можно настроить по своему усмотрению

        Paint maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // Рисуем маску
        canvas.drawBitmap(maskBitmap, null, new RectF(0, 0, baseBitmap.getWidth(), baseBitmap.getHeight()), null);

        // Рисуем затемнение только по контуру
        canvas.drawRect(0, baseBitmap.getHeight() - heightToDarken, baseBitmap.getWidth(), baseBitmap.getHeight(), shadowPaint);
        canvas.drawBitmap(maskBitmap, 0, 0, maskPaint);

        setImageBitmap(tiredBitmap);
    }
}
