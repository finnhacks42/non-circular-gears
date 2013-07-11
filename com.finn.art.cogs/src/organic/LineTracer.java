package organic;

import java.awt.Dimension;
import java.awt.Point;

import processing.core.PApplet;
import processing.core.PShape;

public class LineTracer {
	private PApplet app;
	private Dimension size;
	private static final float BLACK = 10f;
	private int[] pixels;
	
	
	public LineTracer(PApplet app){
		this.app = app;
		this.size = app.getSize();
		app.loadPixels();
		pixels = app.pixels;
	}
	
	
	/*** find a starting edge black pixel. 
	 * Returns null if no black pixel is found. ***/
	private Point findStart() {
		for (int x = 1; x < size.width - 1; x ++) {
			for (int y = 1; y < size.height - 1; y++) {
				int loc = location(x, y);
				float brightness = app.brightness(pixels[loc]);
				if (brightness > BLACK) { //the pixel is black
					Point p = new Point(x, y);
					return p;
				}
			}
		}
		return null;
	}
	
	/*** returns the next adjacent black pixel that has a direct white neighbour. If no such pixel exists return null ***/
	private Point nextEdgePoint(Point p,Point prev, int[] pixels) {
		int[] shifts  = {-1,0,1};
		for (int i : shifts) {
			for (int j: shifts) {
				if (i != 0 || j != 0) {
					int x = p.x +i;
					int y = p.y +j;
					int loc = location(x,y);
					if (app.brightness(pixels[loc]) > BLACK){
						if (prev == null || prev.x != x || prev.y != y) { //if this is the previous edge point we have just come from
							//check the direct neighbours of this point and see if any of them are white. If they are then its and edge
							int [] neighbours = {location(x-1,y),location(x+1,y),location(x,y-1),location(x,y+1)};
							for (int n: neighbours) {
								//if (n > 0 && n < pixels.length) {
									float b = app.brightness(pixels[n]);
									if (b <= BLACK) { //the neighbouring pixel n is white
										return new Point(x,y);
									}
								//}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private int location(int x, int y) {
		return x + y * size.width;
	}
	
	public PShape trace() {
		PShape shape = app.createShape();
		Point start = findStart();
		Point prevP = null;
		Point nextP = null;
		Point p  = start;
		while (true) {
			shape.vertex(p.x, p.y);
			// get the first neighbour of p that is not the previous point
			nextP = nextEdgePoint(p, prevP, pixels);
			prevP = p;
			p = nextP;
			
			if (p.equals(start)) {
				break;
			}
		}
		shape.end();
		return shape;	
	}
	
	

}
