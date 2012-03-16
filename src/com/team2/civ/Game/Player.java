package com.team2.civ.Game;

import java.util.ArrayList;
import java.util.List;

import com.team2.civ.AI.AI;

public class Player {
	public String name;
	public String colour;
	
	public int scoreMilitary = 0;
	public int scoreEconomy = 0;
	public int scoreStratLoc = 0;
	
	public int powerUsage = 0;
	public int powerCapability = 0;
	public int metal = 2000;
	public int population = 0;

	public AI ai;
	
	public List<GameAction> previousTurn = new ArrayList<GameAction>();
	
	public ArrayList<GameUnit> units = new ArrayList<GameUnit>();
	public ArrayList<GameStaticObject> objects = new ArrayList<GameStaticObject>();
	
	public Player(String name, String colour, AI ai) {
		this.name = name;
		this.colour = colour;
		this.ai = ai;
	}
	
	
}
