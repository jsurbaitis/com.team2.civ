package com.team2.civ.AI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.team2.civ.Game.GameController;


public class Population {
	private int population_size;
	private AI[] genomes;
	private HashMap<AI, Float> fitness = new HashMap<AI, Float>();
	private HashMap<AI, Integer> times_used = new HashMap<AI, Integer>();
	private double fitness_level = 0;
	private int init_generation;
	private int game = 0;

	public Population(int pop_size) {
		population_size = pop_size;
		genomes = new AI[pop_size];
	}

	public Population(int pop_size, HashMap<AI, Float> fit, AI[] genome_array) {
		population_size = pop_size;
		genomes = genome_array;
		fitness = fit;
	}

	public Population(File f) {
		Scanner in;
		try {
			in = new Scanner(new FileInputStream(f));
			
			in.next(); in.next(); in.next();
			in.next(); in.next(); in.next();
			
			this.init_generation = in.nextInt();
			in.next(); in.next();
			
			genomes = new AI[in.nextInt()];
			population_size = genomes.length;
			in.next();

			for (int i = 0; i < genomes.length; i++) {
				in.next(); in.next();
				String fileName = in.next();
				in.next(); in.next();
				Float fitnessVal = in.nextFloat();
				in.next(); in.next();
				Integer timesUsed = in.nextInt();

				genomes[i] = new AI(new File("genomes/"+fileName));
				fitness.put(genomes[i], fitnessVal);
				times_used.put(genomes[i], timesUsed);
				
				in.next(); in.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void populate() {
		for (int i = 0; i < genomes.length; i++) {
			genomes[i] = new AI();
			fitness.put(genomes[i], 0f);
			
			if(i != 0 && (i + 1) % 10 == 0)
				System.out.println("Completed "+(i+1)+" AIs");
		}
	}

	public void compete(AI ai1, AI ai2, AI ai3, AI ai4) {
		if (getTimesUsed(ai1) > 5 || getTimesUsed(ai2) > 5
				|| getTimesUsed(ai3) > 5 || getTimesUsed(ai4) > 5) {
			return;
		}

		AIGameResult winner = (new GameController()).runGame(ai1, ai2, ai3, ai4);
		fitness.put(winner.winner, (getFitness(winner.winner) + winner.score));
		
		incTimesUsed(ai1);
		incTimesUsed(ai2);
		incTimesUsed(ai3);
		incTimesUsed(ai4);
		
		game++;
		System.out.println("\nGame "+game+" completed\n");
	}
	
	public float getFitness(AI ai) {
		if(!fitness.containsKey(ai))
			return 0;
		else return fitness.get(ai);
	}

	private int getTimesUsed(AI ai) {
		if (!times_used.containsKey(ai))
			return 0;
		else
			return times_used.get(ai);
	}
	
	private void incTimesUsed(AI ai) {
		if (!times_used.containsKey(ai))
			times_used.put(ai, 1);
		else
			times_used.put(ai, times_used.get(ai) + 1);
	}

	public void competeAll() {
		this.fitness.clear();
		this.times_used.clear();
		
		Random random = new SecureRandom();
		for (int i = genomes.length - 1; i > 1; i--) {
			for (int j = 0; j < 5; j++) {
				compete(genomes[i], genomes[random.nextInt(i)],
						genomes[random.nextInt(i)],
						genomes[random.nextInt(i)]);
			}
		}
	}

	public void cullHerd() {
		double fitsum = 0;
		for (Float i : fitness.values()) {
			fitsum += i;
		}
		this.fitness_level = fitsum / fitness.values().size();
		AI[] newAI = new AI[population_size];
		int j = 0;
		for (int i = 0; i < genomes.length; i++) {
			if (getFitness(genomes[i]) > fitness_level) {
				newAI[j] = genomes[i];
				j++;
			}
		}
		int k = j;
		Random random = new SecureRandom();
		
		if(k == 0) {
			this.populate();
		} else {
			for (int t = j; t < genomes.length; t++) {
				AI parent1 = newAI[random.nextInt(k)];
				AI parent2 = newAI[random.nextInt(k)];
				newAI[j] = new AI(parent1, parent2);
				j++;
			}
			
			this.genomes = newAI;
		}
	}

	public void writeAIs() {
		int index = 0;
		for (AI ai : this.genomes) {
			ai.WriteSelf(index);
			index++;
		}
	}

	public void writeMetadata(int generation) {
		File f = new File("population_metadata.xml");
		if (f.exists())
			f.delete();

		BufferedWriter out;
		try {
			f.createNewFile();
			out = new BufferedWriter(new FileWriter(f));
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			out.write("<genome_metadata>\n");
			out.write("<date>\n");
			out.write(dateFormat.format(date)+"\n");
			out.write("</date>\n");
			out.write("<generation>\n");
			out.write(""+generation+"\n");
			out.write("</generation>\n");
			out.write("<genome_count>\n");
			out.write(""+genomes.length+"\n");
			out.write("</genome_count>\n");
			
			for (int i = 0; i < genomes.length; i++) {
				out.write("<genome>\n");
				out.write("<filename>\n");
				out.write("genome_" + i + "\n");
				out.write("</filename>\n");
				out.write("<fitness>\n");
				out.write("" + getFitness(genomes[i]) + "\n");
				out.write("</fitness>\n");
				out.write("<times_used>\n");
				out.write("" + getTimesUsed(genomes[i]) + "\n");
				out.write("</times_used>\n");
				out.write("</genome>\n");
			}
			out.write("</genome_metadata>");

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getPopulationSize(){
		return this.population_size;
	}
	
	public int getInitGeneration(){
		return this.init_generation;
	}
}
