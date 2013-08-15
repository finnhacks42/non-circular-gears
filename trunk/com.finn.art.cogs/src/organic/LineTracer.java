package organic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PShape;

public class LineTracer {
	private PApplet app;
	private Dimension size;
	private int[] pixels;
	private static final int OBJECT_COLOR = Color.WHITE.getRGB();
	private static final int BACKGROUND_COLOR = Color.BLACK.getRGB();
	
	
	public LineTracer(PApplet app){
		this.app = app;
		this.size = app.getSize();
		app.loadPixels();
		pixels = app.pixels;
	}
	
	
	public void countColors() {
		app.loadPixels();
		Map<Integer,Integer> colors = new HashMap<Integer,Integer>();
		for (int x = 0; x < app.width; x ++) {
			for ( int y = 0; y < app.height; y ++) {
				int location = location(x,y);
				int color = pixels[location];
				if (colors.containsKey(color)) {
					colors.put(color, colors.get(color) +1 );
				} else {
					colors.put(color, 1);
				}
			}
		}
		System.out.println(app.width+","+app.height);
		System.out.println(colors);
		System.out.println(Color.BLACK.getRGB());
		System.out.println(Color.WHITE.getRGB());
	}
	
	
	/*** find a starting edge white pixel. 
	 * Returns null if no white pixel is found. ***/
	public Point findStart(int ignoreThickness) {
		for (int x = 1; x < size.width - 1; x ++) {
			for (int y = 1; y < size.height - ignoreThickness; y++) {
				int loc = location(x, y);
				int color = pixels[loc];
				if (color == OBJECT_COLOR) {
					//check the next 10 points
					boolean allWhite = true;
					for (int i = 0; i < ignoreThickness; i ++) {
						int nextColor = pixels[location(x,y+1)];
						if (!(OBJECT_COLOR==nextColor)) {allWhite = false;}
					}
					if (allWhite) {
						System.out.println("Valid Start: "+x+","+y);
						Point p = new Point(x, y);
						return p;
					}
				} 
			}
		}
		return null;
	}
	
	
	private boolean isEdge(int x, int y){
		int loc = location(x,y);
		int color = pixels[loc];
		if (color != OBJECT_COLOR){
			return false;
		} else { //  pixel is the correct color
			// check the direct neighbours of this point to check it is not an interior point or part of a 1 pixel think extension
			int[] nColors = {pixels[location(x,y -1)],pixels[location(x,y+1)],pixels[location(x-1,y)], pixels[location(x+1,y)]};
			System.out.println("neighbour colors:"+Arrays.toString(nColors));
			int backgroundNeighbourCount = 0;
			for (int neighbourColor: nColors) {
				if (BACKGROUND_COLOR == neighbourColor) {
					backgroundNeighbourCount ++;
				}
			}
			if (backgroundNeighbourCount == 1) {
				return true;
			} else if (backgroundNeighbourCount == 2 && nColors[0] != nColors[1]) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/*** returns the next adjacent white pixel that has a direct black neighbour. If no such pixel exists return null.
	 * Assumes that the color of points already visited has been changed to a third color to avoid re-visiting pixels. ***/
	public Point nextEdgePoint(Point p, int[] pixels) {
		int[] shifts  = {-1,0,1};
		for (int i : shifts) {
			for (int j: shifts) { 
				if (i != 0 || j != 0) {	//iterate through the 8 neighbouring pixels of p (including diagonals)
					int x = p.x +i;
					int y = p.y +j;
					if (isEdge(x, y)) {
						return new Point(x,y);
					}
				}
			}
		}
		return null;
	}
	
	int location(int x, int y) {
		return x + y * size.width;
	}
	
	public PShape trace() {
		app.loadPixels();
		pixels = app.pixels;
			
		PShape shape = new PShape();
		
		Point start = findStart(10);
		Point prevP = null;
		Point nextP = null;
		Point p  = start;
		int pointCount = 0;
		shape.beginShape();
		while (true) {
			shape.vertex(p.x, p.y);
			//set the color of the point at p to red - prevents backtracking
			int ploc = location(p.x,p.y);
			System.out.println("initial color: "+pixels[ploc]); // should be -1 
			pixels[ploc] = Color.RED.getRGB();
			
			
			// get the first neighbour of p that is not the previous point
			nextP = nextEdgePoint(p, pixels);
			prevP = p;
			p = nextP;
			
			if (p == null || p.equals(start)) {
				break;
			}
			if (pointCount % 1000 == 0) {
				System.out.println(pointCount);
			}
			pointCount ++;
		}
		shape.endShape();
		return shape;	
	}
	
	

}
