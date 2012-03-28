package com.team2.civ;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.File;

import com.team2.civ.AI.Generator;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameMap;

public class Team2Civ {
	
	public static Boolean AI_MODE;
	public static boolean DEBUG_OUTPUT = false;
	public static boolean MULTITHREADED = false;
	public static int NUM_OF_THREADS = 2;
	private static int generations = -1;
	private static int populationSize = -1;

	private static void parseArgs(String args[]) {
		int index = 0;
		String token;
		while (index < args.length) {
			token = args[index];
			
			if(token.equals("-fowoff")) {
				GameMap.FOW_ON = false;
			} else if(token.equals("-normal")) {
				if(AI_MODE != null && AI_MODE) {
					System.out.println("Cannot use both -normal and -ai");
					break;
				}
				
				AI_MODE = false;
			} else if (token.equals("-ai")) {
				if(AI_MODE != null && !AI_MODE) {
					System.out.println("Cannot use both -normal and -ai");
					break;
				}
				
				AI_MODE = true;
			} else if (token.equals("-r")) {
				if(generations != -1) {
					System.out.println("Cannot use both -r and -n");
					break;
				}
				
				index++;
				generations = Integer.parseInt(args[index]);
			} else if (token.equals("-n")) {
				if(generations != -1) {
					System.out.println("Cannot use both -r and -n");
					break;
				}
				
				index++;
				generations = Integer.parseInt(args[index]);
				index++;
				populationSize = Integer.parseInt(args[index]);
			} else if (token.equals("-m")) {
				Team2Civ.MULTITHREADED = true;
				index++;
				Team2Civ.NUM_OF_THREADS = Integer.parseInt(args[index]);
			} else if (token.equals("-o")) {
				DEBUG_OUTPUT = true;
			} else {
				System.out.println("Invalid token "+token);
				break;
			}
			index++;
		}
	}

	public static void main(String args[]) {
		if(args.length == 0) {
			System.out.println("How to use:");
			System.out.println("------------------");
			System.out.println("Normal mode:");
			System.out.println("-normal to select");
			System.out.println("will load 3 random AIs from /genomes or generate them if needed");
			System.out.println("------------------");
			System.out.println("AI mode:");
			System.out.println("-ai to select");
			System.out.println("-n <generations> <pop_size> to start a new population");
			System.out.println("-r <generations> to resume old population from population_metadata.xml");
			System.out.println("-m <threads> to run multiple games at once");
			System.out.println("HIGHLY recommended VM command:");
			System.out.println("-Xmx<megabytes>m to increase heap size (around 2048 for 50-80 AIs)");
			System.out.println("------------------");
			System.out.println("-o for detailed AI output");
			System.out.println("-fowoff to turn off fog of war");
			System.out.println("------------------");
			return;
		}
		
		parseArgs(args);
		
		File folder = new File("genomes/");
		if(!folder.exists()) folder.mkdir();
		
		if(AI_MODE == null)
			System.out.println("You must use -ai or -normal");
		else if(AI_MODE && generations == -1)
			System.out.println("-ai mode needs -n <generations> <pop_size> or -r <generations>");
		else if (!AI_MODE)
			new GameWindow();
		else {
			GraphicsConfiguration config = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();

			Resources.init(config);

			Generator g = null;
			if(populationSize != -1)
				g = new Generator(generations, populationSize);
			else
				g = new Generator(generations, new File("population_metadata.xml"));

			g.generate();
		}
	}
}
