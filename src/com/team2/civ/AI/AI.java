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
	final static int condition_bits = 21;
	final static int tolerance_bits = 26;
	final static int action_points = 5;
	boolean[] genome;

	public AI(GameController game) {
		this.game = game;
		this.genome = generateNewGenome();
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));}

	public AI(GameController game, AI parent1, AI parent2){
		this.game = game;
		Random rng = new SecureRandom();
		int num = rng.nextInt(1);
		if (num == 0){
			this.genome = mutation(mate(parent1.genome,parent2.genome));
		} else {
			this.genome = mutation(mate2(parent1.genome,parent2.genome));
		}
	}
	
	private static boolean[] generateNewGenome() {
		boolean[] actions = new boolean[(int) Math.pow(2, condition_bits)
				* action_points];
		Random rng = new SecureRandom();
		for (int i = 0; i < actions.length; i++) {
			actions[i] = rng.nextBoolean();
			System.out.print(actions[i]);
		}
		boolean[] tolerances = new boolean[tolerance_bits];
		for (int i = 0; i < tolerances.length; i++) {
			tolerances[i] = rng.nextBoolean();
			System.out.print(tolerances[i]);
		}
		System.out.println();
		boolean[] genome = new boolean[tolerances.length + actions.length];
		for (int i = 0; i < tolerances.length; i++) {
			genome[i] = tolerances[i];
		}
		for (int i = 0; i < actions.length; i++) {
			genome[i + tolerances.length - 1] = actions[i];
		}
		return genome;
	}

	private static boolean[] mate(boolean[] b1, boolean[] b2) {
		Random random = new SecureRandom();
		boolean rng = random.nextBoolean();
		boolean temp [] = new boolean[b1.length];
		int len = b1.length % 2;
		if (rng) {
			for (int i = 0; i < len; i++) {
				temp[i] = b1[i];
				temp[len + i] = b2[len + i];
			}
		} else {
			for (int i = 0; i < len; i++) {
				temp[i] = b2[i];
				temp[len + i] = b1[len + i];
			}
		}
		return temp;
	}
	
	private static boolean[] mate2(boolean[] b1, boolean[] b2) {
		Random random = new SecureRandom();
		boolean rng = random.nextBoolean();
		
		Random random1 = new SecureRandom();
		int rng2 = random1.nextInt(b1.length);
		
		int a = b1.length % 5;
		rng2 = random.nextInt(a);
		boolean temp2 [] = new boolean[b1.length];
		if (rng) {
			for (int i = 0; i < rng2*5; i++) {
				temp2[i] = b1[i];
				temp2[rng2*5 + i] = b2[rng2*5 + i];
			}
		} else {
			for (int i = 0; i < rng2*5; i++) {
				temp2[i] = b2[i];
				temp2[rng2*5 + i] = b1[rng2*5 + i];
			}
		}
		return temp2;
	}
	
	private static boolean[] mutation(boolean[] b1) {
		Random random = new SecureRandom();
		int rng3 = random.nextInt(19);
		boolean temp3 [] = new boolean[b1.length];
		  if (rng3 == 0){
			  for(int i = 0; i < b1.length; i++){
				  int rng4 = random.nextInt(9);
				  if (rng4 == 0)
					  temp3[i] = random.nextBoolean();
					  else  temp3[i] = b1[i];
	
			  }}
		 return b1;	
	}

	private static int bitsToInt(boolean[] bits) {
		int output = 0;
		int bit = 0;
		for (int i = bits.length - 1; i >= 0; i--) {
			int val = bits[i] ? 1 : 0;
			output += val * Math.pow(2, bit);
			bit++;
		}
		return output;
	}
}