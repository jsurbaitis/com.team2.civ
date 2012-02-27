package com.team2.civ.Game;

import java.awt.image.BufferedImage;

import com.team2.civ.Data.GameUnitData;
import com.team2.civ.Map.MovingMapObject;

public class GameUnit extends MovingMapObject {
	public GameUnitData data;
	public Player owner;
	private int HP, AP;

	public GameUnit(int mapX, int mapY, BufferedImage bitmap, Player owner, GameUnitData data) {
		super(mapX, mapY, bitmap, owner);

		this.owner = owner;
		this.data = data;
		HP = data.HP;
	} 
	
	public int getHP() {
		return HP;
	}
	
	public int getAP() {
		return AP;
	}
	
	public void die() {

	}

	public void takeDmg(int dmg) {
		HP -= dmg;

		if (HP <= 0)
			die();
	}
	
	public int getDmgToDeal(String targetID) {
		int rtn = data.baseDmg;
		
		if(data.dmgModifiers.containsKey(targetID))
			rtn += data.dmgModifiers.get(targetID);
		
		return rtn;
	}

}
