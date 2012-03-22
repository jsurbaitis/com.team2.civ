package com.team2.civ.Map;

public class PathNode extends CoordObject {
	
	public PathNode parent = null;
	public int cost = 0;
	public int heuristic = 0;
	
	public PathNode(int mapX, int mapY) {
		super(mapX, mapY);
	}
}