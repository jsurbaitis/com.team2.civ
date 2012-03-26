package com.team2.civ.Game;

import java.util.Random;

import com.team2.civ.Data.GameUnitData;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Map.MovingMapObject;

public class GameUnit extends MovingMapObject {
	private static final int FORTIFY_BASE = 5;
	
	public GameUnitData data;
	public Player owner;
	private int HP;
	public int AP;
	
	public String name;
	
	private int fortifyBonus;

	public GameUnit(int mapX, int mapY, Player owner, GameUnitData data) throws ResNotFoundException {
		super(mapX, mapY, data.id, owner);

		this.owner = owner;
		this.data = data;
		HP = data.HP;
		AP = data.AP;
		
		Random rnd = new Random();
		name = data.names.get(rnd.nextInt(data.names.size()));
	}
	
	public boolean isFortified() {
		return fortifyBonus > 0;
	}
	
	public void update() {
		if(isFortified() && fortifyBonus < 25)
			fortifyBonus += FORTIFY_BASE;
	}
	
	public void fortify() {
		if(!isFortified()) fortifyBonus = FORTIFY_BASE;
	}
	
	public void unfortify() {
		fortifyBonus = 0;
	}
	
	public int getHP() {
		return HP;
	}
	
	public int getAP() {
		return AP;
	}

	public boolean inCombatRange(GameUnit target) {
		return Math.abs(target.mapX - mapX) <= data.range
			&& Math.abs(target.mapY - mapY) <= data.range;
	}

	public void takeDmg(int dmg) {
		HP -= dmg * (1 - fortifyBonus / 100);
	}
	
	public int getDmgToDeal(String targetID) {
		int rtn = data.baseDmg;
		
		if(data.dmgModifiers.containsKey(targetID))
			rtn += data.dmgModifiers.get(targetID);
		
		return rtn;
	}

}
