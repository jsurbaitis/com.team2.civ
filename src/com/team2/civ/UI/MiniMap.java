package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;

public class MiniMap extends UIElement {

	public MiniMap(int x, int y,int width, int height){
		super (x,y,width,height,null);
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.RED);
		g.fillRect(x, y, width, height);
	}
}
