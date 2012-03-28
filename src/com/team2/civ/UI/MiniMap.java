package com.team2.civ.UI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.team2.civ.GameWindow;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameGraphics;
import com.team2.civ.Game.GameMap;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class MiniMap extends UIElement {

	private int maxX,maxY;
	private GameMap map;
	private GameGraphics graphics;
	
	public MiniMap(int x, int y,int width, int height, GameMap map,GameGraphics graphics){
		super (x,y,width,height,null);
		this.map = map;
		maxX = GameMap.MAP_HEIGHT * ((Resources.TILE_WIDTH) -31);
		maxY = GameMap.MAP_WIDTH * ((Resources.TILE_HEIGHT) / 2);
		this.graphics=graphics;
	}
	
	@Override
	public void draw(Graphics2D g) {


		int mapH = 2*maxY;
		int mapW = 2*maxX;
		int minimapX = (x*(this.width) / (mapW))+1 ;
		int minimapY = (y* this.height / (mapH))+1 ;
		g.setColor(Color.black);
		g.fillRect(x, y, width, height);
		
		for(WallTile wt: map.getUnwalkableMap()) {
			if (wt.type.equals(WallTile.Type.MOUNTAIN)) g.setColor(Color.gray);
			else g.setColor(Color.blue);
			if (wt.seen)
			g.fillRect(wt.x/minimapX+this.x-width/5, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor( new Color (102,205,170));
		for (WalkableTile wt:map.getWalkableMap()){
			if (wt.seen)
			g.fillRect(wt.x/minimapX+this.x-width/5, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		
		for (GameStaticObject wt:map.getAllCities()){
		 g.setColor(wt.owner.getColor());
		 if (wt.beingSeen)
			g.fillRect(wt.x/minimapX+this.x-width/5, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		
		for (GameUnit wt:map.getUnits()){
			 g.setColor(wt.owner.getColor());
			 if (wt.beingSeen)
			g.fillRect(wt.x/minimapX+this.x-width/5, 2*wt.y/minimapY+ height*2/5+5+this.y, 7, 3);
		}
		g.setColor(Color.white);
		int tempx= -graphics.getOffsetX();
		int tempy= -graphics.getOffsetY();
		g.drawRect(tempx/minimapX+this.x-width/5, 2*tempy/minimapY+ height*2/5+10+this.y, graphics.getShowingWidth()/minimapX, graphics.getShowingHeight()/minimapY);

	}
    public void pickedcrd (MouseEvent ev){
		int mapH = 2*maxY;
		int mapW = 2*maxX;
		int minimapX = (x*(this.width) / (mapW))+1 ;
		int minimapY = (y* this.height / (mapH))+1 ;

    	int mx=ev.getX();
    	int my=ev.getY();
    	// Unscrambling magic numbers;
    	mx=mx-this.x+width/5;
    	my=my-this.y-height*2/5-5;
    	my=my*minimapY/2-GameWindow.WINDOW_HEIGHT/2;
    	mx=mx*minimapX-GameWindow.WINDOW_WIDTH/2;
    	graphics.setOffsets(-mx, -my);
    	
    }
}
