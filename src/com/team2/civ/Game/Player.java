package com.team2.civ.Game;

import java.util.ArrayList;
import java.util.Comparator;

import com.team2.civ.AI.AI;
import com.team2.civ.Data.GameStaticObjectData;
import com.team2.civ.Data.GameUnitData;

public class Player {
	public static enum Color { RED, BLUE, GREEN, PINK };
	
	public String name;
	public Color colour;
	
	public int scoreMilitary = 0;
	public int scoreEconomy = 0;
	public int scoreStratLoc = 0;
	
	public int powerUsage = 0;
	public int powerCapability = 2;
	public int metal = 125;
	public int population = 0;
	
	public boolean attackedByP1, attackedByP2, attackedByP3;
	public boolean lostMine, lostPowerplant, lostCity, lostUnit;
	public boolean builtPowerplant, builtMine, builtCity;

	public AI ai;

	public ArrayList<GameUnit> units = new ArrayList<GameUnit>();
	public ArrayList<GameStaticObject> objects = new ArrayList<GameStaticObject>();
	
	public Player(String name, Color colour, AI ai) {
		this.name = name;
		this.colour = colour;
		this.ai = ai;
	}
	
	public boolean canAfford(GameUnitData data) {
		return canAffordMetal(data) && canAffordPower(data);
	}
	
	public boolean canAffordMetal(GameUnitData data) {
		return metal >= data.metalCost;
	}
	
	public boolean canAffordPower(GameUnitData data) {
		return (powerCapability - powerUsage) >= data.powerUsage;
	}
	
	public boolean canAfford(GameStaticObjectData data) {
		return metal >= data.metalCost;
	}
	
	private static class EconomyComparator implements Comparator<Player>{
	    @Override
	    public int compare(Player p1, Player p2) {
	    	if(p1.scoreEconomy == p2.scoreEconomy)
	    		return 0;
	    	else if(p1.scoreEconomy > p2.scoreEconomy)
	        	return 1;
	        else
	        	return -1;
	    }
	}
	
	private static class MilitaryComparator implements Comparator<Player>{
	    @Override
	    public int compare(Player p1, Player p2) {
	    	if(p1.scoreMilitary == p2.scoreMilitary)
	    		return 0;
	    	else if(p1.scoreMilitary > p2.scoreMilitary)
	        	return 1;
	        else
	        	return -1;
	    }
	}
	
	private static class StratLocComparator implements Comparator<Player>{
	    @Override
	    public int compare(Player p1, Player p2) {
	    	if(p1.scoreStratLoc == p2.scoreStratLoc)
	    		return 0;
	    	else if(p1.scoreStratLoc > p2.scoreStratLoc)
	        	return 1;
	        else
	        	return -1;
	    }
	}
	
	public static StratLocComparator stratLocComparator = new StratLocComparator();
	public static MilitaryComparator militaryComparator = new MilitaryComparator();
	public static EconomyComparator economyComparator = new EconomyComparator();
	
	public void resetConditions() {
		attackedByP1 = false;
		attackedByP2 = false;
		attackedByP3 = false;
		
		lostMine = false;
		lostPowerplant = false;
		lostUnit = false;
		lostCity = false;
		
		builtPowerplant = false;
		builtMine = false;
		builtCity = false;
	}
	
	public void updateLost(GameStaticObject lost) {
		if(lost.data.id.equals("MINE"))
			lostMine = true;
		else if(lost.data.id.equals("POWERPLANT"))
			lostPowerplant = true;
		else if(lost.data.id.equals("CITY"))
			lostCity = true;
	}
	
	public void updateLost(GameUnit lost) {
		lostUnit = true;
	}
	
	public void updateAttacked(Player attacker, GameController game) {
		int index = getPlayerIndex(attacker, game);
		if(index == 1) attackedByP1 = true;
		else if(index == 2) attackedByP2 = true;
		else if(index == 3) attackedByP3 = true;
	}
	
	public void updateBuilt(GameStaticObject so) {
		if(so.data.id.equals("MINE"))
			builtMine = true;
		else if(so.data.id.equals("POWERPLANT"))
			builtPowerplant = true;
		else if(so.data.id.equals("CITY"))
			builtCity = true;
	}

	public Player getPlayer(int n, GameController game) {
		int count = 1;
		for(Player p: game.getPlayers()) {
			if(p != this && count == n)
				return p;
			else
				count++;
		}
		return null;
	}
	
	public int getPlayerIndex(Player player, GameController game) {
		int count = 1;
		for(Player p: game.getPlayers()) {
			if(p == player)
				return count;
			else
				count++;
		}
		return -1;
	}
}