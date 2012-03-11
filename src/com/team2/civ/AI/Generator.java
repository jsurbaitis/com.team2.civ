package com.team2.civ.AI;

import com.team2.civ.Game.GameController;

public class Generator {
	private int init_generations;
	private Population pop;
	
	public Generator(int generations, int population_size, GameController gc){
		init_generations = generations;
		pop = new Population(population_size, gc);
	}
	
	public void generate(){
		pop.populate();
		for (int i = 0; i < init_generations; i++){
			pop.competeAll();
			pop.cullHerd();
		}
		pop.writeAIs();
	}

}
