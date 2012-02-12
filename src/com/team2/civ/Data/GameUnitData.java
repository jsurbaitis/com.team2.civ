package com.team2.civ.Data;

import java.util.ArrayList;
import java.util.HashMap;

import com.team2.civ.UI.UI.UIEvent;

public class GameUnitData {
	private String id, name, description;
	
	private int metalCost, moveCost;
	
	private int HP;
	private int AP;
	private int baseDmg;
	private int range;
	
	private HashMap<String, Integer> dmgModifiers;
	private ArrayList<UIEvent> uiActions;
	private ArrayList<String> build;
	
	public GameUnitData(String id) {
		name = id.toLowerCase();
	}

}
