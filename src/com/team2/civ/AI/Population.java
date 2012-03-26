package com.team2.civ.AI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.team2.civ.Game.GameController;


public class Population {
	private int population_size;
	private AI[] genomes;
	private HashMap<AI, Integer> fitness = new HashMap<AI, Integer>();
	private HashMap<AI, Integer> times_used = new HashMap<AI, Integer>();
	private double fitness_level = 0;
	private int init_generation;

	public Population(int pop_size) {
		population_size = pop_size;
		genomes = new AI[pop_size];
		this.populate();
	}

	public Population(int pop_size, HashMap<AI, Integer> fit, AI[] genome_array) {
		population_size = pop_size;
		genomes = genome_array;
		fitness = fit;
	}

	public Population(File f) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbBuilder = null;
		Document doc = null;
		try {
			dbBuilder = dbFactory.newDocumentBuilder();
			doc = dbBuilder.parse(f);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList genlist = doc.getElementsByTagName("generation");
		this.init_generation = Integer.parseInt(genlist.item(0).getTextContent());
		NodeList nList = doc.getElementsByTagName("genome");
		ArrayList<AI> ai = new ArrayList<AI>();
		String[] gens = new String[nList.getLength()];
		for (int i = 0; i < nList.getLength(); i++) {

			Node node = nList.item(i);
			NodeList children = node.getChildNodes();

			Node nFilename = children.item(0);
			AI a = new AI(new File(nFilename.getTextContent()));
			ai.add(a);
			Node nFitness = children.item(1);
			fitness.put(a, Integer.parseInt(nFitness.getTextContent()));
			Node nTimesUsed = children.item(2);
			times_used.put(a, Integer.parseInt(nTimesUsed.getTextContent()));

			String filename = nFilename.getNodeValue();
			System.out.println(filename);
		}
		population_size = ai.size();
		genomes = (AI[]) ai.toArray();
	}

	public void populate() {
		for (int i = 0; i < genomes.length; i++) {
			genomes[i] = new AI();
			fitness.put(genomes[i], 0);
		}
	}

	public int getFitness(int index) {
		return fitness.get(genomes[index]).intValue();
	}

	public int getAIFitness(AI ai) {
		return fitness.get(ai).intValue();
	}

	public void compete(AI ai1, AI ai2, AI ai3, AI ai4) throws Exception {
		if (getTimesUsed(ai1) > 5 || getTimesUsed(ai2) > 5
				|| getTimesUsed(ai3) > 5 || getTimesUsed(ai4) > 5) {
			throw new Exception("AI has already competed 5 times.");
		}

		AI winner = (new GameController()).runGame(ai1, ai2, ai3, ai4);
		fitness.put(winner, (fitness.get(winner) + 1));
		
		incTimesUsed(ai1);
		incTimesUsed(ai2);
		incTimesUsed(ai3);
		incTimesUsed(ai4);
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
		for (int i = genomes.length - 1; i > 0; i--) {
			Random random = new SecureRandom();
			for (int j = 0; j < 6; j++) {
				try {
					compete(genomes[i], genomes[random.nextInt(i - 1)],
							genomes[random.nextInt(i - 1)],
							genomes[random.nextInt(i - 1)]);
				} catch (Exception e) {
				}
			}
		}
	}

	public void cullHerd() {
		double fitsum = 0;
		for (Integer i : fitness.values()) {
			fitsum += i;
		}
		this.fitness_level = fitsum / fitness.values().size();
		AI[] newAI = new AI[population_size];
		int j = 0;
		for (int i = 0; i < genomes.length; i++) {
			if (fitness.get(genomes[i]) > fitness_level) {
				newAI[j] = genomes[i];
				j++;
			}
		}
		int k = j;
		Random random = new SecureRandom();
		if (j < population_size) {
			for (int t = j; t < genomes.length; t++) {
				AI parent1 = newAI[random.nextInt(k)];
				AI parent2 = newAI[random.nextInt(k)];
				newAI[j] = new AI(parent1, parent2);
				j++;
			}
		}
		this.genomes = newAI;
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
			out.write("<genome_metadata>");
			out.write("<date>");
			out.write(dateFormat.format(date));
			out.write("</date>");
			out.write("<generation>");
			out.write(""+generation);
			out.write("</generation>");
			
			for (int i = 0; i < genomes.length; i++) {
				out.write("<genome>");
				out.write("<filename>");
				out.write("genome" + i + ".txt");
				out.write("</filename>");
				out.write("<fitness>");
				out.write("" + fitness.get(genomes[i]));
				out.write("</fitness>");
				out.write("<times_used>");
				out.write("" + times_used.get(genomes[i]));
				out.write("</times_used>");
				out.write("</genome>");
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
