package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Game.GameController;

public class WalkableTile extends MapObject {
	public boolean occupied = false;

	public WalkableTile(int mapX, int mapY, BufferedImage bitmap) {
		super(mapX, mapY, bitmap);
		// TODO Auto-generated constructor stub
	}

	public boolean picked(int f, int g) {
		if(!img.isVisible())
			return false;
		
		if(f > x + GameController.TILE_WIDTH / 10 && f < x + GameController.TILE_WIDTH - GameController.TILE_WIDTH / 10 && 
		   g > y + GameController.TILE_HEIGHT / 10 && g < y + GameController.TILE_HEIGHT - GameController.TILE_HEIGHT / 10)
			return true;
		else
			return false;			
	}
}
