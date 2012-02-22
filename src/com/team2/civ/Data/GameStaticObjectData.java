package com.team2.civ.Data;

import java.util.ArrayList;

public class GameStaticObjectData {
	public String id;
	
	public int metalCost, moveCost;
	public int fowRange;
	public int defensiveBonus;
	public ArrayList<String> names = new ArrayList<String>();
	public ArrayList<String> buildIDs = new ArrayList<String>();
	
	public GameStaticObjectData(String id) {
		this.id = id;
	}
}
