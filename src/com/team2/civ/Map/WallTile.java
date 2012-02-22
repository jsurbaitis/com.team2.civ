package com.team2.civ.Map;

import java.awt.image.BufferedImage;

public class WallTile extends MapObject {

	public WallTile(int mapX, int mapY, BufferedImage bitmap, BufferedImage fowImg) {
		super(mapX, mapY, bitmap, null);
		img.setFowImg(fowImg);
	}

}
