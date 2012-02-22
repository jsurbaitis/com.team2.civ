package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Game.Player;

public class WalkableTile extends MapObject {

	public WalkableTile(int mapX, int mapY, BufferedImage bitmap, BufferedImage fowImg, Player owner) {
		super(mapX, mapY, bitmap, owner);
		img.setFowImg(fowImg);
	}
}
