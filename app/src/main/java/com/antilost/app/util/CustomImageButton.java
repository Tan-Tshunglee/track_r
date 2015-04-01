package com.antilost.app.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * 自定义ImageButton
 * 可以在ImageButton上面设置文字
 * @author SJR
 */
public class CustomImageButton extends ImageButton {
    private String _text = "";
    private int _color = 0;
    private float _textsize = 0f;

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setText(String text){
        this._text = text;
    }

    public void setColor(int color){
        this._color = color;
    }

    public void setTextSize(float textsize){
        this._textsize = textsize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(_color);
        textPaint.setTextSize(_textsize);
        StaticLayout layout = new StaticLayout(_text,textPaint,250,Layout.Alignment.ALIGN_CENTER,1.5F,0,false);
        canvas.translate(40, 100);
        layout.draw(canvas);
    }
}
