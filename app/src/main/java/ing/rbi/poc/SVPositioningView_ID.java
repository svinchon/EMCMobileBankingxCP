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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SVPositioningView_ID extends View {

    private final float         RADIUS = 35.0f;
	private final int           COLOR = 0x7FFF0000;
	private Point[]             _bounds;                // topLeft, bottomRight
    private Point               _circleCenter;
	private Paint               _boundsPaint;

	public SVPositioningView_ID(Context context) {
        super(context);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        _boundsPaint = new Paint();
        _boundsPaint.setColor(COLOR);
        _boundsPaint.setStyle(Paint.Style.STROKE);
        _boundsPaint.setStrokeWidth(10f);
    }

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		_bounds = new Point[2];
        int _boxWidthPercent = 90;
        int _boxWidth = canvas.getWidth()*_boxWidthPercent/100;
        float _boxWidthHeightRatio = 1.5f;
        int _boxHeight = (int)(_boxWidth / _boxWidthHeightRatio);
        int _left = (int)((canvas.getWidth()-_boxWidth)/2);
        int _top=(int)((canvas.getHeight()-_boxHeight)/2);
        int _right=(int)((canvas.getWidth()-_boxWidth)/2+_boxWidth);
        int _bottom=(int)((canvas.getHeight()-_boxHeight)/2+_boxHeight);
		_bounds[0] = new Point(
                _left,
                _top
        );
		_bounds[1] = new Point(
                _right,
                _bottom
        );
		canvas.drawRect(
                _bounds[0].x,
                _bounds[0].y,
                _bounds[1].x,
                _bounds[1].y,
                _boundsPaint
        );
        //Paint _boundsPaint.setColor(Color.BLACK);
        _boundsPaint.setTextSize(40);
        canvas.drawText(
                "TOP",
                (int) (_left),
                (int) (_top - 20),
                _boundsPaint
        );
        canvas.drawText(
                "BOTTOM",
                (int) (_left),
                (int) (_bottom + 45 ),
                _boundsPaint
        );
        //_circleCenter = new Point(canvas.getWidth()/4, canvas.getHeight() / 2);
        //canvas.drawCircle(_circleCenter.x, _circleCenter.y, RADIUS, _boundsPaint);
        //int ovalWidth = 350;
        //int ovalHeight = 400;
        //RectF r = new RectF(_circleCenter.x-ovalWidth/2, _circleCenter.y-ovalHeight/2, _circleCenter.x+ovalWidth/2, _circleCenter.y+ovalHeight/2);
        //canvas.drawOval(r, _boundsPaint);
    }
}
