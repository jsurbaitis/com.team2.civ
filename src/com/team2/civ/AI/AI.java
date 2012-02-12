package com.team2.civ.AI;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.WalkableTile;

public class AI {
	private GameController game;
	private Resources res;
	final static int condition_bits = 21;
	final static int tolerance_bits = 26;
	final static int action_points = 5;
	
	public AI(GameController game, Resources res) {
		this.game = game;
		this.res = res;
		
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));}
	
	private static boolean[] makeNewGenome(){
		boolean[] actions = new boolean[(int) Math.pow(2, condition_bits) * action_points];
		Random rng = new SecureRandom();
		for (int i = 0; i < actions.length; i++){
			actions[i] = rng.nextBoolean();
			System.out.print(actions[i]);
		}
		boolean[] tolerances = new boolean[tolerance_bits];
		for (int i = 0; i < tolerances.length; i++){
			tolerances[i] = rng.nextBoolean();
			System.out.print(tolerances[i]);
		}
		System.out.println();
		boolean[] genome = new boolean[tolerances.length + actions.length];
		for (int i = 0; i < tolerances.length; i++){
			genome[i] = tolerances[i];
		}
		for (int i = 0; i < actions.length; i++){
			genome[i+tolerances.length-1] = actions[i];
		}
		return genome;
	}
	private static int bitsToInt(boolean[] bits){
		int output = 0;
		int bit = 0;
		for (int i = bits.length-1; i >= 0; i--){
			int val = bits[i]? 1 : 0;
			output += val * Math.pow(2, bit);
			bit++;
		}
		return output;
	}
}

