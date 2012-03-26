package com.team2.civ.Game;

import java.util.Random;

import com.team2.civ.Data.GameStaticObjectData;
import com.team2.civ.Map.WalkableTile;

public class GameStaticObject extends WalkableTile {
	public GameStaticObjectData data;
	public String name;

	private int HP;
	
	public boolean active = true;

	public GameStaticObject(int mapX, int mapY, Player owner,
			GameStaticObjectData data) {
		super(mapX, mapY, data.id, owner);

		this.owner = owner;
		this.data = data;
		
		Random rnd = new Random();
		name = data.names.get(rnd.nextInt(data.names.size()));
	}
	
	public int getHP() {
		return HP;
	}
}
