package com.team2.civ.Map;


public class WallTile extends MapObject {
	
	public static enum Type { WATER, MOUNTAIN };
	public Type type;

	public WallTile(int mapX, int mapY, String imgId, Type type) {
		super(mapX, mapY, imgId, null);
		this.type = type;
	}

}
