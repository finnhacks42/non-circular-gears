package utilities;

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
	private static final int OBJECT_COLOR = Color.BLACK.getRGB();
	private static final int BACKGROUND_COLOR = Color.WHITE.getRGB();
	
	
	public LineTracer(PApplet app){
		this.app = app;
		this.size = app.getSize();
		app.loadPixels();
		pixels = app.pixels;
	}
	
	
	/** print out the number of pixels in each color. Intended for debugging.***/
	public void countColors() {
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
	
	
	/*** find a starting edge object pixel. 
	 * Returns null if no object color pixel is found. ***/
	public Point findStart(int ignoreThickness) {
		for (int x = 1; x < size.width - 1; x ++) {
			for (int y = 1; y < size.height - ignoreThickness; y++) {
				int loc = location(x, y);
				int color = pixels[loc];
				if (color == OBJECT_COLOR) {
					//check the next 10 points
					boolean allObjectColor = true;
					for (int i = 0; i < ignoreThickness; i ++) {
						int nextColor = pixels[location(x,y+1)];
						if (!(OBJECT_COLOR==nextColor)) {allObjectColor = false;}
					}
					if (allObjectColor) {
						System.out.println("Valid Start: "+x+","+y);
						Point p = new Point(x, y);
						return p;
					}
				} 
			}
		}
		return null;
	}
	
	/*** convert all pixels to either black or white ***/
	public void blackAndWhite(){
		for (int x = 0; x < size.width; x++){
			for (int y = 0; y < size.height; y++) {
				int location = location(x,y);
				int color = pixels[location];
				Color c = new Color(color);
				float average = (c.getRed()+c.getBlue()+c.getGreen())/3f;
				if (average >= 255/2f){
					pixels[location] = Color.WHITE.getRGB();
				} else {
					pixels[location] = Color.BLACK.getRGB();
				}
			}
		}
	}
	
	/** sets the color of object colour pixels with only one direct object color neighbour to background repeatedly. ***/
	public void deleteTailPixels(){
		while (true) {
			int reset = 0;
			for (int x = 1; x < size.width - 1; x ++) {
				for (int y = 1; y < size.height - 1; y ++) {
					int location = location(x,y);
					if (pixels[location] == OBJECT_COLOR) {
						int objectNeighbours = numDirectNeighbours(x, y, OBJECT_COLOR);
						if (objectNeighbours <= 1) {
							pixels[location] = BACKGROUND_COLOR;
							reset ++;
						}
					}
				}
			}
			System.out.println("Reset:"+reset);
			if (reset == 0) { //we did not change the color of any pixels this time through so time to stop
				break;
			}
		}
	}
	
	
	
	private boolean isEdge2(int x, int y){
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
	
	private boolean isEdge(int x, int y) {
		int loc = location(x,y);
		int color = pixels[loc];
		if (color != OBJECT_COLOR) {
			return false;
		}
		int backgroundNeighbours = numDirectNeighbours(x, y, BACKGROUND_COLOR);
		if (backgroundNeighbours >=1) {
			return true;
		} else {
			return false;
		}
	}
	
	/*** returns the next adjacent (direct or diagonal) object color pixel that is a valid edge. If no such pixel exists return null.
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
	
	/*** return the location of position x,y in the pixel array ***/
	private int location(int x, int y) {
		return x + y * size.width;
	}
	
	/*** returns an array of length 4 containing the pixels above, below, left and right of the target pixel. ***/
	private int[] getDirectNeighbours(int x, int y){
		int[] nColors = {pixels[location(x,y -1)],pixels[location(x,y+1)],pixels[location(x-1,y)], pixels[location(x+1,y)]};
		return nColors;
	}
	
	/*** returns the number of direct neighbours of a specific color ***/
	private int numDirectNeighbours(int x, int y, int color) {
		int[] neighbours = getDirectNeighbours(x, y);
		int count = 0;
		for (int n: neighbours) {
			if (n == color) {
				count ++;
			}
		}
		return count;
		
	}
	
	/*** returns a 5 * 5 matrix centered on x, y and containing all its neighbours and neighbours' neighbours. ***/
	public int[][] getAllNeighbours(int x, int y) {
		int[][] result = new int[5][5];
		for (int i = 0; i <= 4; i ++) {
			for (int j = 0; j <= 4; j++) {
				int loc = location(x+i-2,y+j-2);
				int color = pixels[loc];
				result[i][j] =  color;
			}
		}
		return result;
	}
	

	
	
	
	public PShape trace(Point start) {	
		PShape shape = new PShape();
		Point nextP = null;
		Point p  = start;
		int pointCount = 0;
		shape.beginShape();
		while (true) {
			System.out.println(p.x+","+p.y);
			shape.vertex(p.x, p.y);
			//set the color of the point at p to red - prevents backtracking
			int ploc = location(p.x,p.y);
			pixels[ploc] = Color.RED.getRGB();
			
			
			nextP = nextEdgePoint(p, pixels);
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
