package com.team2.civ.Map;

import java.awt.image.BufferedImage;

public class WallTile extends MapObject {
	
	public static enum Type { WATER, MOUNTAIN };
	public Type type;

	public WallTile(int mapX, int mapY, BufferedImage bitmap, BufferedImage fowImg, Type type) {
		super(mapX, mapY, bitmap, null);
		this.type = type;
		img.setFowImg(fowImg);
	}

}
