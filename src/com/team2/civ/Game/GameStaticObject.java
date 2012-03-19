package com.team2.civ.Game;

import java.awt.image.BufferedImage;
import java.util.Random;

import com.team2.civ.Data.GameStaticObjectData;
import com.team2.civ.Map.WalkableTile;

public class GameStaticObject extends WalkableTile {
	public GameStaticObjectData data;
	public String name;

	private int HP;
	
	public boolean active = true;

	public GameStaticObject(int mapX, int mapY, BufferedImage bitmap, BufferedImage fowImg, Player owner,
			GameStaticObjectData data) {
		super(mapX, mapY, bitmap, fowImg, owner);

		this.owner = owner;
		this.data = data;
		
		Random rnd = new Random();
		name = data.names.get(rnd.nextInt(data.names.size()));
	}
	
	public int getHP() {
		return HP;
	}
}
