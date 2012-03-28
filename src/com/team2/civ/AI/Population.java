package com.team2.civ.AI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.team2.civ.Team2Civ;
import com.team2.civ.Game.GameController;


public class Population {
	private int population_size;
	private AI[] genomes;
	private HashMap<AI, Float> fitness = new HashMap<AI, Float>();
	private HashMap<AI, Integer> times_used = new HashMap<AI, Integer>();
	private double fitness_level = 0;
	private int init_generation;
	private int gameNumber = 0;
	private Random rnd = new Random();

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
				
				if(i != 0 && (i + 1) % 10 == 0)
					System.out.println("Loaded "+(i+1)+" AIs");
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
	
	private class GameThread extends Thread {
		
		public AI a1, a2, a3, a4;
		private AIGameResult winner;
		private boolean isDone = false;
		
		public GameThread(AI a1, AI a2, AI a3, AI a4) {
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.a4 = a4;
		}
		
		public AIGameResult getWinner() {
			return winner;
		}
		
		public boolean isDone() {
			return isDone;
		}
		
		@Override
		public void run() {
			winner = (new GameController()).runGame(a1, a2, a3, a4);
			isDone = true;
		}
	}
	
	public void compete(AI ai1, AI ai2, AI ai3, AI ai4) {
		AIGameResult winner = (new GameController()).runGame(ai1, ai2, ai3, ai4);
		updateStats(winner, ai1, ai2, ai3, ai4);
	}
	
	private boolean aiValidForGame(AI ai1, AI ai2, AI ai3, AI ai4) {
		return (getTimesUsed(ai1) <= 5 || getTimesUsed(ai2) <= 5
				|| getTimesUsed(ai3) <= 5 || getTimesUsed(ai4) <= 5);
	}
	
	public void updateStats(GameThread thread) {
		updateStats(thread.getWinner(), thread.a1, thread.a2, thread.a3, thread.a4);
	}
	
	public void updateStats(AIGameResult winner, AI ai1, AI ai2, AI ai3, AI ai4) {
		fitness.put(winner.winner, (getFitness(winner.winner) + winner.score));
		
		incTimesUsed(ai1);
		incTimesUsed(ai2);
		incTimesUsed(ai3);
		incTimesUsed(ai4);
		
		gameNumber++;
		System.out.println("\nGame "+gameNumber+" completed\n");
	}

	public void competeAll() {
		this.fitness.clear();
		this.times_used.clear();
		
		AI a1, a2, a3, a4;
		for(int j = 0; j < 5; j++) {
			if(!Team2Civ.MULTITHREADED) {
				for (int i = 0; i < genomes.length; i += 4) {
					if(i+3 >= genomes.length)
						continue;
					
					a1 = genomes[i];
					a2 = genomes[i+1];
					a3 = genomes[i+2];
					a4 = genomes[i+3];
					if(this.aiValidForGame(a1, a2, a3, a4))
						compete(a1, a2, a3, a4);
				}
			}
			else {
				GameThread[] threads = new GameThread[Team2Civ.NUM_OF_THREADS];

				for (int i = 0; i < genomes.length; i += 4*threads.length) {
					for(int z = 0; z < threads.length; z++) {
						if(i+z*4+3 >= genomes.length)
							continue;
						
						a1 = genomes[i+z*4];
						a2 = genomes[i+z*4+1];
						a3 = genomes[i+z*4+2];
						a4 = genomes[i+z*4+3];
						if(this.aiValidForGame(a1, a2, a3, a4)) {
							threads[j] = new GameThread(a1, a2, a3, a4);
							threads[j].setPriority(Thread.MAX_PRIORITY);
							threads[j].start();
						}
					}
					while(!threadsComplete(threads)) {}
					
					for(int z = 0; z < threads.length; z++)
						if(threads[z] != null)
							updateStats(threads[z]);
				}
				
				
			}
			
			this.shuffleGenomes();
		}
	}
	
	private void shuffleGenomes() {
		AI temp;
		int next;
		for(int i = 0; i < genomes.length; i++) {
			next = rnd.nextInt(genomes.length);
			temp = genomes[next];
			genomes[next] = genomes[i];
			genomes[i] = temp;
		}
	}
	
	private boolean threadsComplete(GameThread[] threads) {
		for(int i = 0; i < threads.length; i++) {
			if(threads[i] != null && !threads[i].isDone())
				return false;
		}

		return true;
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
		
		if(k == 0) {
			this.populate();
		} else {
			for (int t = j; t < genomes.length; t++) {
				if (rnd.nextInt(50) == 1){
					AI parent1 = newAI[rnd.nextInt(k)];
					AI parent2 = newAI[rnd.nextInt(k)];
					newAI[j] = new AI(parent1, parent2);
				} else {
					newAI[j] = new AI();
				}
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
