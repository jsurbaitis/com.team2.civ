package com.team2.civ.AI;
import java.util.HashMap;

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
		AI[] genomes = new AI[pop_size];
		this.populate();
	}
	
	public Population(int pop_size, GameController gc, HashMap<AI,Integer> fit, AI[] genome_array) {
		
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
		//AI winner = rungame(ai1, ai2);
		/* put two AIs in game, fuck some shit up, returns the winner */
		//fitness.put(winner, (fitness.get(winner) + 1));
		times_used.put(ai1, times_used.get(ai1) + 1);
		times_used.put(ai2, times_used.get(ai2) + 1);
	}
	
	public void cullHerd(){
		/* do shit, remove the weak, add until genome = pop_size */
	}
}
