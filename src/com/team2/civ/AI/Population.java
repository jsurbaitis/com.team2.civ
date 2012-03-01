package com.team2.civ.AI;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import com.team2.civ.Game.GameController;

public class Population {
	private int population_size;
	private GameController game;
	private AI[] genomes;
	private HashMap<AI,Integer> fitness = new HashMap();
	private HashMap<AI,Integer> times_used= new HashMap();
	private final int fitness_level = 2;
	
	public Population(int pop_size, GameController gc){
		population_size = pop_size;
		game = gc;
		genomes = new AI[pop_size];
		this.populate();
	}
	
	public Population(int pop_size, GameController gc, HashMap<AI,Integer> fit, AI[] genome_array) {
		population_size = pop_size;
		game = gc;
		genomes = genome_array;
		fitness = fit;
	}
	
	public void populate(){
		for (int i = 0; i < genomes.length; i++){
			genomes[i] = new AI(null);
			fitness.put(genomes[i], 0);
		}
	}
	
	public int getFitness(int index){
		return fitness.get(genomes[index]).intValue();
	}
	
	public int getAIFitness(AI ai){
		return fitness.get(ai).intValue();
	}
	
	public void compete(AI ai1, AI ai2) throws Exception{
		if (! times_used.containsKey(ai1)){
			times_used.put(ai1, 0);
		}
		if (! times_used.containsKey(ai2)){
			times_used.put(ai2, 0);
		}
		if (times_used.get(ai1) > 5 || times_used.get(ai2) > 5){
			throw new Exception("AI has already competed 5 times.");
		}
		//AI winner = rungame(ai1, ai2);
		/* put two AIs in game, fuck some shit up, returns the winner */
		//fitness.put(winner, (fitness.get(winner) + 1));
		times_used.put(ai1, times_used.get(ai1) + 1);
		times_used.put(ai2, times_used.get(ai2) + 1);
	}
	
	public void competeAll(){
		for (int i = genomes.length - 1; i > 0; i--){
			Random random = new SecureRandom();
			for (int j = 0; j < 5; j++){
				try {
					compete(genomes[i], genomes[random.nextInt(i-1)]);
				} catch (Exception e) {
				}
			}
		}
	}
	
	public void cullHerd(){
		AI[] newAI = new AI[population_size];
		int j = 0;
		for (int i = 0; i < genomes.length; i++){
			if (fitness.get(genomes[i]) > fitness_level){
				newAI[j] = genomes[i];
				j++;
			}
		}
		int k = j;
		Random random = new SecureRandom();
	    AI parent1 =newAI[random.nextInt(k)];
	    AI parent2 =newAI[random.nextInt(k)];
	    if (j < population_size){
	    	for (int t = j; t < genomes.length; t++){
	    		newAI[j] = new AI(game, parent1, parent2);
	    		j++;
	    	}
	    }
	}

}