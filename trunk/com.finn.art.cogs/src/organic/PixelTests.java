package organic;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;

public class PixelTests extends PApplet {
	
	public void setup() {
		size(500,500);
		noSmooth();
		background(Color.BLACK.getRGB());
		ellipse(0,0,100,100);
		countColors();
	}
	
	private int location(int x, int y) {
		return x + y * width;
	}
	public void countColors() {
		loadPixels();
		Map<Integer,Integer> colors = new HashMap<Integer,Integer>();
		for (int x = 0; x < width; x ++) {
			for ( int y = 0; y < height; y ++) {
				int location = location(x,y);
				int color = pixels[location];
				if (colors.containsKey(color)) {
					colors.put(color, colors.get(color) +1 );
				} else {
					colors.put(color, 1);
				}
			}
		}
		System.out.println(colors);
	}

	public void draw2() {
		
	}
}
