package com.team2.civ.Data;

import java.util.ArrayList;
import java.util.HashMap;

import com.team2.civ.UI.UIEvent;

public class GameUnitData {
	public String id, description;
	
	public int metalCost;
	public int HP;
	public int AP;
	public int baseDmg;
	public int range;
	public int fowRange;
	public int powerUsage;
	
	public HashMap<String, Integer> dmgModifiers = new HashMap<String, Integer>();
	public ArrayList<String> names = new ArrayList<String>();
	public ArrayList<UIEvent> uiActions = new ArrayList<UIEvent>();
	public ArrayList<String> buildIDs = new ArrayList<String>();
	
	public GameUnitData(String id) {
		this.id = id;
	}

}
