package com.team2.civ.Map;

import java.awt.image.BufferedImage;


public class MapObject extends CoordObject {
	public boolean highlighted = false;
	protected MapObjectImage img;
	
	public MapObject(int mapX, int mapY, BufferedImage bitmap) {
		super(mapX, mapY);
		img = new MapObjectImage(bitmap, this);
	}
	
	public MapObjectImage getImage() {
		return img;
	}
}
