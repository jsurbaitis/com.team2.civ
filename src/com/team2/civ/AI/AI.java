package com.team2.civ.AI;

import java.util.HashMap;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.WalkableTile;



public class AI {
	private GameController game;
	private Resources res;
	
	public AI(GameController game, Resources res) {
		this.game = game;
		this.res = res;
		
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));
	}

}
