package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class MiniMap extends UIElement {

	private int maxX,maxY;
	private GameController gc;
	
	public MiniMap(int x, int y,int width, int height, GameController gc){
		super (x,y,width,height,null);
		this.gc = gc;
		maxX = GameController.MAP_HEIGHT * ((Resources.TILE_WIDTH) -31);
		maxY = GameController.MAP_WIDTH * ((Resources.TILE_HEIGHT) / 2);
	}
	
	@Override
	public void draw(Graphics2D g) {


		int mapH = 2*maxY;
		int mapW = 2*maxX;
		int minimapX = (x*(this.width) / (mapW)) ;
		int minimapY = (y* this.height / (mapH)) ;
		g.setColor(Color.blue);
		g.fillRect(x, y, width, height);
		g.setColor(Color.gray);
		for(WallTile wt: gc.getUnwalkableMap()) {
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.green);
		for (WalkableTile wt:gc.getWalkableMap()){
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.yellow);
		for (GameStaticObject wt:gc.getAllCities()){
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.red);
		for (GameUnit wt:gc.getUnits()){
		}

	}
}
