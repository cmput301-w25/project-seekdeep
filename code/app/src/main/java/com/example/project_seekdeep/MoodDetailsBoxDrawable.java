package com.example.project_seekdeep;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;

/**
 * This class is Drawable thingy.
 * @author Nikola Despotoski, Nancy Lin
 * https://stackoverflow.com/questions/18478210/how-do-i-programatically-change-a-drawable-resources-background-color?rq=3
 */
public class MoodDetailsBoxDrawable extends ShapeDrawable {

    private Paint mFillPaint;
    private Paint mStrokePaint;
    private int mColor;

    @Override
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        shape.draw(canvas, mFillPaint);
        shape.draw(canvas, mStrokePaint);
        super.onDraw(shape, canvas, paint);
    }

    public MoodDetailsBoxDrawable(){
        super();
    }

    public void setColor(Paint.Style style, int c){

        mColor = c;
        if(style.equals(Paint.Style.FILL)){
            mFillPaint.setColor(mColor);
        }else if(style.equals(Paint.Style.STROKE)){
            mStrokePaint.setColor(mColor);
        }else{
            mFillPaint.setColor(mColor);
            mStrokePaint.setColor(mColor);
        }
        super.invalidateSelf();
    }
}
