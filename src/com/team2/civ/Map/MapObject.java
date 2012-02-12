package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.Player;


public class MapObject extends CoordObject {
	public Player owner;
	public boolean selected = false;
	public boolean highlighted = false;
	protected MapObjectImage img;
	
	public MapObject(int mapX, int mapY, BufferedImage bitmap, Player owner) {
		super(mapX, mapY);
		img = new MapObjectImage(bitmap, this);
		this.owner = owner;
	}
	
	public MapObjectImage getImage() {
		return img;
	}
	
	public boolean picked(int f, int g) {
		if(!img.isVisible())
			return false;
		
		if(f > x + Resources.TILE_WIDTH / 10 && f < x + Resources.TILE_WIDTH - Resources.TILE_WIDTH / 10 && 
		   g > y + Resources.TILE_HEIGHT / 10 && g < y + Resources.TILE_HEIGHT - Resources.TILE_HEIGHT / 10)
			return true;
		else
			return false;			
	}
}
