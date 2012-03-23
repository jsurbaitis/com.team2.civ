package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameMap;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class MiniMap extends UIElement {

	private int maxX,maxY;
	private GameMap map;
	
	public MiniMap(int x, int y,int width, int height, GameMap map){
		super (x,y,width,height,null);
		this.map = map;
		maxX = GameMap.MAP_HEIGHT * ((Resources.TILE_WIDTH) -31);
		maxY = GameMap.MAP_WIDTH * ((Resources.TILE_HEIGHT) / 2);
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
		
		for(WallTile wt: map.getUnwalkableMap()) {
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.green);
		for (WalkableTile wt:map.getWalkableMap()){
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.yellow);
		for (GameStaticObject wt:map.getAllCities()){
			g.fillRect(wt.x/minimapX+this.x-30, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.red);
		for (GameUnit wt:map.getUnits()){
		}

	}
}
