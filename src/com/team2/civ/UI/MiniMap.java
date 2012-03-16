package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;

import com.team2.civ.Game.GameController;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

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
		g.setColor(Color.blue);
		for(WallTile wt: gc.getUnwalkableMap()) {
			g.fillRect(wt.mapX*4+this.x, wt.mapY*4+this.y, 3, 5);
		}
		g.setColor(Color.green);
		for (WalkableTile wt:gc.getWalkableMap()){
			g.fillRect(wt.mapX*4+this.x, wt.mapY*4+this.y, 3, 5);
		}

	}
}
