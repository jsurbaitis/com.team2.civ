package com.team2.civ.AI;

import java.awt.GraphicsConfiguration;

public class Generator {
	private int init_generations;
	private Population pop;
	
	public Generator(int generations, int population_size, GraphicsConfiguration graphics){
		init_generations = generations;
		pop = new Population(population_size);
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
