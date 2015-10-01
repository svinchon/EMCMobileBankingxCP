package ing.rbi.poc; /** -------------------------------------------------------------------------
 * Copyright 2013-2015 EMC Corporation.  All rights reserved.
 ---------------------------------------------------------------------------- */
// TODO SEB custom view to display ellipse and box

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class SVPositioningView extends View {

    private final float         RADIUS = 35.0f;
	private final int           COLOR = 0x7FFF0000;
	private Point[]             _bounds;                // topLeft, bottomRight
    private Point               _circleCenter;
	private Paint               _boundsPaint;

	public SVPositioningView(Context context) {
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
		_bounds[0] = new Point(20, 500);
		_bounds[1] = new Point(canvas.getWidth()-20, canvas.getHeight()-500);
		canvas.drawRect(_bounds[0].x, _bounds[0].y, _bounds[1].x, _bounds[1].y, _boundsPaint);
        _circleCenter = new Point(canvas.getWidth()/4, canvas.getHeight() / 2);
        //canvas.drawCircle(_circleCenter.x, _circleCenter.y, RADIUS, _boundsPaint);
        int ovalWidth = 350;
        int ovalHeight = 400;
        RectF r = new RectF(_circleCenter.x-ovalWidth/2, _circleCenter.y-ovalHeight/2, _circleCenter.x+ovalWidth/2, _circleCenter.y+ovalHeight/2);
        canvas.drawOval(r, _boundsPaint);
    }
}
