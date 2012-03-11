package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;

import com.team2.civ.Game.GameController;

public class MiniMap extends UIElement {

	private GameController gc;
	public MiniMap(int x, int y,int width, int height,GameController gmc){
		super (x,y,width,height,null);
	    this.gc=gmc;
	}
	
	@Override
	public void draw(Graphics2D g) {
		
		g.setColor(Color.RED);
		g.fillRect(x, y, width, height);
	}
}
