package org.grouplocator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MyOverlays extends Overlay{
	private final GeoPoint geoPoint;
	private final int color;
	
	public MyOverlays(GeoPoint geoPoint, int color) {
	    this.geoPoint = geoPoint;
	    this.color = color;
	}

	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		super.draw(canvas, mapView, shadow);

		// 	Convert geo coordinates to screen pixels
		Point point = new Point();
		mapView.getProjection().toPixels(geoPoint, point);

		Paint circlePaint = new Paint();
	    circlePaint.setAntiAlias(true);
	    //fill region
	    circlePaint.setColor(color);
	    circlePaint.setAlpha(90);
	    circlePaint.setStyle(Paint.Style.FILL);
	    
	    float radius = (float)0.18 * mapView.getZoomLevel() + 4;
	    
	    canvas.drawCircle(point.x, point.y, radius, circlePaint);
	
		return true;
		
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		// Handle tapping on the overlay here
		return true;
	}
}