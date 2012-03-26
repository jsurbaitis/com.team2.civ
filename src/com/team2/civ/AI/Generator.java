package com.team2.civ.AI;

import java.io.File;

public class Generator {
	private int init_generations;
	private Population pop;
	private final boolean resume;

	public Generator(int generations, int population_size) {
		init_generations = generations;
		pop = new Population(population_size);
		resume = false;
	}

	public Generator(int generations, File f) {
		init_generations = generations;
		pop = new Population(f);
		resume = true;
	}

	public void generate() {
		int current_generation = 0;
		
		if (resume) {
			current_generation = pop.getInitGeneration() + 1;
		} else {
			pop.populate();
		}
		
		for(int i = current_generation; i < init_generations; i++) {
			pop.competeAll();
			pop.cullHerd();
			pop.writeAIs();
			pop.writeMetadata(i);
			System.out.println("\n\n\nGeneration "+i+" complete\n\n\n");
		}
	}

}
