package com.team2.civ.Map;

import com.team2.civ.Data.Resources;

public class CoordObject implements Comparable<CoordObject> {
	public int x, y;
	public int mapX, mapY;
	
	public CoordObject(int mapX, int mapY) {
		setPos(mapX, mapY);
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null) return false;
		CoordObject cp = (CoordObject)o;
		return mapX == cp.mapX && mapY == cp.mapY;
	}
		
	@Override
	public int hashCode() {
		return mapX + mapY * 65536;
	}
	
	public void setPos(int mapX, int mapY) {
		this.mapX = mapX;
		this.mapY = mapY;
		//ISO
		//x = (mapY * GameController.TILE_WIDTH / 2) + (mapX * GameController.TILE_WIDTH / 2);
		//y = (mapX * GameController.TILE_HEIGHT / 2) - (mapY * GameController.TILE_HEIGHT / 2);
		
		//HEX - 30 is a messed up hard coded value
		x = (mapY * (Resources.TILE_WIDTH - 31)) + (mapX * (Resources.TILE_WIDTH - 31));
		y = (mapX * Resources.TILE_HEIGHT / 2) - (mapY * Resources.TILE_HEIGHT / 2);
	}

	public void shiftPos(int offsetX, int offsetY) {
		setPos(mapX + offsetX, mapY + offsetY);
	}

	@Override
	public int compareTo(CoordObject another) {
		if(y > another.y)
			return 1;
		else if(y < another.y)
			return -1;
		else if(x < another.x)
			return 1;
		else if(x > another.x)
			return -1;
		return 0;
	}
}

