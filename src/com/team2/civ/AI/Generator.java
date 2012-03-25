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

	public Generator(File f) {
		pop = new Population(f);
		resume = true;
	}

	public void generate() {
		if (resume) {
			int current_generation = pop.getInitGeneration();
			while (true) {
				pop.competeAll();
				pop.cullHerd();
				current_generation++;
				pop.writeAIs();
				pop.writeMetadata(current_generation);
			}
		} else {
			pop.populate();
			for (int i = 0; i < init_generations; i++) {
				pop.competeAll();
				pop.cullHerd();
				pop.writeAIs();
				pop.writeMetadata(i);
			}
			pop.writeAIs();
			pop.writeMetadata(init_generations);
		}
	}

}
