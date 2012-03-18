package com.team2.civ.AI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.team2.civ.Game.GameAction;
import com.team2.civ.Game.GameController;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Game.Player;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.WalkableTile;

public class AI {
	private GameController game;
	final static int condition_bits = 21;
	final static int tolerance_bits = 26;
	final static int action_points = 5;
	final static int action_bits = 5;
	final byte[] genome;
	
	final boolean[] conditions = new boolean[condition_bits];
	//tolerances:
	final int init_st_dev_strat_loc;
	int current_st_dev_strat_loc;
	final int init_combat_pcent_win_chance;
	int current_combat_pcent_win_chance;
	final int init_queue_length;
	final int init_resource_threshold;
	final int init_turns_threshold;
	int current_turns_threshold;
	final byte default_behavior_code;

	public AI(GameController game) {
		this.game = game;
		this.genome = generateNewGenome();
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = (int) this.genome[1];
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		this.init_queue_length = (int) this.genome[2];
		this.init_resource_threshold = ((int) this.genome[3]) * 40;
		this.init_turns_threshold = ((int) this.genome[4]) * 10;
		this.current_turns_threshold = this.init_turns_threshold;
		this.default_behavior_code = genome[5];
	}

	public AI(GameController game, AI parent1, AI parent2){
		this.game = game;
		Random rng = new SecureRandom();
		int num = rng.nextInt(1);
		if (num == 0){
			this.genome = mutation(mate1(parent1.genome,parent2.genome));
		} else {
			this.genome = mutation(mate2(parent1.genome,parent2.genome));
		}
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = (int) this.genome[1];
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		this.init_queue_length = (int) this.genome[2];
		this.init_resource_threshold = ((int) this.genome[3]) * 40;
		this.init_turns_threshold = ((int) this.genome[4]) * 10;
		this.current_turns_threshold = this.init_turns_threshold;
		this.default_behavior_code = genome[5];
	}
	
	public AI(GameController game, File f){
		this.game = game;
		this.genome = FileToBytes(f);
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = (int) this.genome[1];
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		this.init_queue_length = (int) this.genome[2];
		this.init_resource_threshold = ((int) this.genome[3]) * 40;
		this.init_turns_threshold = ((int) this.genome[4]) * 10;
		this.current_turns_threshold = this.init_turns_threshold;
		this.default_behavior_code = genome[5];
	}
	
	private static byte[] generateNewGenome() {
		byte[] genome = new byte[(int) (6 + (((int) Math.pow(2, condition_bits) * action_points) / 1.6))];//1.6 may need to be changed later, but should correspond adequately to 5bits/action code
		Random rng = new SecureRandom();
		rng.nextBytes(genome);
		//tolerances: 
		genome[0] = (byte) rng.nextInt(3); //stdev stratloc
		genome[1] = (byte) rng.nextInt(15); //win%tol
		genome[2] = (byte) rng.nextInt(7); //max queue length
		genome[3] = (byte) rng.nextInt(63); //banked resources (*40 later)
		genome[4] = (byte) rng.nextInt(63); //turns left (*10 later)
		genome[5] = (byte) rng.nextInt(31); //default action
		return genome;
	}

private static byte[] mate1(byte[] b1, byte[] b2) {
		Random random = new SecureRandom();
		boolean rng = random.nextBoolean();
		byte temp[] = new byte[b1.length];
		int len = b1.length % 2;
		if (rng) {
			for (int i = 0; i < len; i++) {
				temp[i] = b1[i];
				temp[len + i] = b2[len + i];
			}
		} else {
			for (int i = 0; i < len; i++) {
				temp[i] = b2[i];
				temp[len + i] = b1[len + i];
			}
		}
		return temp;
	}

	private static byte[] mate2(byte[] b1, byte[] b2) {
		Random random = new SecureRandom();
		boolean rng = random.nextBoolean();

		Random random1 = new SecureRandom();
		int rng2 = random1.nextInt(b1.length);

		int a = b1.length % 5;
		rng2 = random.nextInt(a);
		byte temp2[] = new byte[b1.length];
		if (rng) {
			for (int i = 0; i < rng2 * 5; i++) {
				temp2[i] = b1[i];
				temp2[rng2 * 5 + i] = b2[rng2 * 5 + i];
			}
		} else {
			for (int i = 0; i < rng2 * 5; i++) {
				temp2[i] = b2[i];
				temp2[rng2 * 5 + i] = b1[rng2 * 5 + i];
			}
		}
		return temp2;
	}

	private static void mutation(byte[] b1) {
		Random random = new SecureRandom();
		int rng = random.nextInt(19);

		if (rng == 0) {
			for (int i = 0; i < b1.length; i++) {
				rng = random.nextInt(19);
				if(rng == 0)
					b1[i] = (byte) random.nextInt();
			}
		}
	}

	private byte[] getResponseCodes(boolean[] env_vars) {
		byte[] responses = new byte[action_points];
		int index = bitsToInt(env_vars) * action_points;
		for (int i = 0; i < action_points; i++) {
			responses[i] = getFiveBits(index + i);
		}
		return responses;
	}

	public ArrayList<GameAction> perform(List<GameAction> actions, Player p) {
		ArrayList<GameAction> output = new ArrayList<GameAction>();
		output.add(new GameAction(GameAction.ZeroAgentEvent.END_TURN, p));
		return output;
	}

	private byte getFiveBits(int index) {
		int bitIndex = (index * 5) % 8;
		int byteIndex = (index * 5) / 8 + 6;
		byte rtn = 0;

		for (int i = 0; i < 5; i++) {
			rtn |= (this.genome[byteIndex] << bitIndex);
			bitIndex++;
			if (bitIndex > 7) {
				bitIndex = 0;
				byteIndex++;
			}
		}

		return rtn;
	}

	@SuppressWarnings("rawtypes")
	class ValueComparator implements Comparator {

		@SuppressWarnings("rawtypes")
		Map base;

		public ValueComparator(Map base) {
			this.base = base;
		}

		public int compare(Object a, Object b) {

			if ((Integer) base.get(a) < (Integer) base.get(b)) {
				return 1;
			} else if ((Integer) base.get(a) == (Integer) base.get(b)) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	private Collection<Player> getmilitaryrankings(Player p[]) {
		HashMap<Player, Integer> temp = new HashMap<Player, Integer>();
		for (int t = 0; t < 4; t++) {
			int sum = 0;
			for (GameUnit u : game.getPlayerUnits(p[t]))
				sum += u.data.metalCost;
			temp.put(p[t], (Integer) sum);
		}

		ValueComparator vc = new ValueComparator(temp);
		TreeMap<Player, Integer> sortedMap = new TreeMap<Player, Integer>(vc);
		sortedMap.putAll(temp);
		return sortedMap.keySet();
	}

	private static int bitsToInt(boolean[] bits) {
		int output = 0;
		int bit = 0;
		for (int i = bits.length - 1; i >= 0; i--) {
			int val = bits[i] ? 1 : 0;
			output += val * Math.pow(2, bit);
			bit++;
		}
		return output;
	}

	// set bit to 1
	// my_byte = my_byte | (1 << pos);

	// set bit to 0
	// my_byte = my_byte & ~(1 << pos);

	// 8 * i, where i is an integer

	// http://docs.oracle.com/javase/1.4.2/docs/api/java/io/FileOutputStream.html

	// take 8 booleans from the boolean array
	// create a byte and set its bits to be equal to the values of the booleans

	private static byte[] boolToBytes(boolean[] bools) {
		byte[] byteArray = new byte[bools.length / 8];
		for (int i = 0; i < (bools.length / 8); i++) {
			for (int j = 0; j < 8; j++) {
				if (bools[i * 8 + j])
					byteArray[i] |= (1 << j);
				else
					byteArray[i] &= (1 << j);
			}
		}
		return byteArray;
	}

	private static void BytesToFile(byte[] bytes, int genomeNumber) {
		File f = new File("genome_" + genomeNumber);
		if (f.exists())
			f.delete();

		FileOutputStream out;
		try {
			f.createNewFile();
			out = new FileOutputStream(f);
			out.write(bytes);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void WriteSelf(int genomeNumber) {
		BytesToFile(this.genome, genomeNumber);
	}

	private static byte[] FileToBytes(File f) {
		byte[] byteArray = new byte[(int) f.length()];
		FileInputStream in;
		try {
			in = new FileInputStream(f);
			in.read(byteArray);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	private static boolean[] intToBits(int in) {
		String binaryString = Integer.toBinaryString(in);
		char[] binaryArray = binaryString.toCharArray();
		boolean[] boolArray = new boolean[binaryArray.length];
		for (int i = 0; i < binaryString.length(); i++) {
			if (binaryArray[i] == '0') {
				boolArray[i] = false;
			} else if (binaryArray[i] == '1') {
				boolArray[i] = true;
			}
		}
		return boolArray;
	}
}
