package com.team2.civ.Map;

import com.team2.civ.Game.Player;

public class WalkableTile extends MapObject {
	
	//Stuff for pathfinding
	public WalkableTile parent = null;
	public int cost = 0;
	public int heuristic = 0;

	public WalkableTile(int mapX, int mapY, String imgId, Player owner) {
		super(mapX, mapY, imgId, owner);
	}
}
