package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Game.Player;

public class WalkableTile extends MapObject {
	
	//Stuff for pathfinding
	public WalkableTile parent = null;
	public int cost = 0;
	public int heuristic = 0;

	public WalkableTile(int mapX, int mapY, BufferedImage bitmap, BufferedImage fowImg, Player owner) {
		super(mapX, mapY, bitmap, owner);
		img.setFowImg(fowImg);
	}
}
