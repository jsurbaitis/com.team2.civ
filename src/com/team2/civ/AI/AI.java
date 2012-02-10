package com.team2.civ.AI;

import java.security.SecureRandom;
import java.util.Random;

import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;

public class AI {
	private GameController game;
	private Resources res;
	final static int condition_bits = 21;
	final static int tolerance_bits = 26;
	
	public AI(GameController game, Resources res) {
		this.game = game;
		this.res = res;
		
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));}
	
	private static byte[] makeNewGenome(){
		byte[] actions = new byte[(int) Math.pow(2, condition_bits)];
		Random rng = new SecureRandom();
		rng.nextBytes(actions);
		for (int i = 0; i < actions.length; i++){
			System.out.print(actions[i]);
		}
		byte[] tolerances = new byte[tolerance_bits];
		rng.nextBytes(tolerances);
		for (int i = 0; i < tolerances.length; i++){
			System.out.print(tolerances[i]);
		}
		System.out.println();
		byte[] genome = new byte[tolerances.length + actions.length];
		for (int i = 0; i < tolerances.length; i++){
			genome[i] = tolerances[i];
		}
		for (int i = 0; i < actions.length; i++){
			genome[i+tolerances.length-1] = actions[i];
		}
		return genome;
	}
	private static int bitsToInt(byte[] bytes){
		int output = 0;
		int bit = 0;
		for (int i = bytes.length-1; i >= 0; i--){
			output += bytes[i] * Math.pow(2, bit);
			bit++;
		}
		return output;
	}
	private static byte[] valuesToBits(boolean[] bools){
		byte[] output = new byte[bools.length];
		for (int i = 0; i < bools.length; i++){
			if (bools[i]){
				output[i] = (byte) 1;
			} else {
				output[i] = (byte) 0;
			}
		}
		return output;
	}
}

