package com.team2.civ.Data;

import java.util.ArrayList;
import java.util.HashMap;

import com.team2.civ.UI.UI.UIEvent;

public class GameUnitData {
	public String id, name, description;
	
	public int metalCost, moveCost;
	
	public int HP;
	public int AP;
	public int baseDmg;
	public int range;
	public int fowRange;
	
	public HashMap<String, Integer> dmgModifiers = new HashMap<String, Integer>();
	public ArrayList<UIEvent> uiActions = new ArrayList<UIEvent>();
	public ArrayList<String> buildIDs = new ArrayList<String>();
	
	public GameUnitData(String id) {
		this.id = id;
		name = id.toLowerCase();
	}

}
