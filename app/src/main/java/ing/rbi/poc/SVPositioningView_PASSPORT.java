package ing.rbi.poc; /** -------------------------------------------------------------------------
 * Copyright 2013-2015 EMC Corporation.  All rights reserved.
 ---------------------------------------------------------------------------- */
// TODO SEB custom view to display ellipse and box

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SVPositioningView_PASSPORT extends View {

    private final float         RADIUS = 35.0f;
	private final int           COLOR = Color.WHITE; //0x7FFF0000;

    private Point[]             _bounds;                // topLeft, bottomRight
    private Point               _circleCenter;
	private Paint               _boundsPaint;
    private Paint               _textPaint;

	public SVPositioningView_PASSPORT(Context context) {
        super(context);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        _boundsPaint = new Paint();
        _boundsPaint.setColor(COLOR);
        _boundsPaint.setStyle(Paint.Style.STROKE);
        _boundsPaint.setStrokeWidth(10f);
        _textPaint = new Paint();
        _textPaint.setColor(COLOR);
        _textPaint.setTextAlign(Paint.Align.CENTER);
        _textPaint.setTypeface(
                Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                )
        );
        _textPaint.setTextSize(40);
        _textPaint.setStrokeWidth(10f);
    }

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
        _bounds = new Point[2];
        int _boxWidthPercent = 95;
        int cw = canvas.getWidth();
        int ch = canvas.getHeight();
        int _boxWidth = cw*_boxWidthPercent/100;
        float _boxWidthHeightRatio = 1.25f;
        int _boxHeight = (int)(_boxWidth / _boxWidthHeightRatio);
        int _left = (int)((cw-_boxWidth)/2);
        int _top=(int)((ch-_boxHeight)/2);
        int _right=(int)((cw-_boxWidth)/2+_boxWidth);
        int _bottom=(int)((ch-_boxHeight)/2+_boxHeight);
        _bounds[0] = new Point(_left, _top);
        _bounds[1] = new Point( _right,  _bottom);
        canvas.drawRect(_bounds[0].x,_bounds[0].y, _bounds[1].x, _bounds[1].y, _boundsPaint);
        _textPaint.setTextSize(40);
        canvas.drawText("Align in box",(int) (cw/2), (int) (_top - 20), _textPaint);
        canvas.drawText("Passport",(int) (cw/2),(int) (_bottom + 45), _textPaint);
        int ovalHeight = (int)(_boxHeight * 60 / 100);
        int ovalWidth = (int) (ovalHeight * 60 / 100);
        _circleCenter = new Point(_left + 100 + ovalWidth/2, _top + _boxHeight / 2);
        RectF r = new RectF( _circleCenter.x-ovalWidth/2, _circleCenter.y-ovalHeight/2, _circleCenter.x+ovalWidth/2, _circleCenter.y+ovalHeight/2);
        canvas.drawOval(r, _boundsPaint);
    }
}
