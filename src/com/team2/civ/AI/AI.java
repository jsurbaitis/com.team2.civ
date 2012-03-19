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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.team2.civ.Data.GameUnitData;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameAction;
import com.team2.civ.Game.GameController;
import com.team2.civ.Game.GameStaticObject;
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
	final float init_combat_pcent_win_chance;
	float current_combat_pcent_win_chance;
	final int init_queue_length;
	final int init_resource_threshold;
	final int init_turns_threshold;
	int current_turns_threshold;
	final byte default_behavior_code;
	
	final byte SEIZE_STRATEGIC_LOCATION = 0;
	final byte SEIZE_RESOURCE = 1;
	final byte ATTACK_PLAYER_1 = 2;
	final byte ATTACK_PLAYER_2 = 3;
	final byte ATTACK_PLAYER_3 = 4;
	final byte ATTACK_STRONGEST_MILITARY = 5;
	final byte ATTACK_STRONGEST_ECONOMY = 6;
	final byte ATTACK_WEAKEST_MILITARY = 7;
	final byte ATTACK_WEAKEST_ECONOMY = 8;
	final byte HARASS_PLAYER_1 = 9;
	final byte HARASS_PLAYER_2 = 10;
	final byte HARASS_PLAYER_3 = 11;
	final byte HARASS_STRONGEST_MILITARY = 12;
	final byte HARASS_STRONGEST_ECONOMY = 13;
	final byte HARASS_WEAKEST_MILITARY = 14;
	final byte HARASS_WEAKEST_ECONOMY = 15;
	final byte MAKE_WORKER = 16;
	final byte MAKE_TANK = 17;
	final byte MAKE_AIR = 18;
	final byte MAKE_ANIAIR = 19;
	final byte FORTIFY_STRATEGIC_LOCATION = 20;
	final byte FORTIFY_RESOURCE = 21;
	final byte FORTIFY_CITY = 22;
	final byte CLEAR_UNIT_QUEUE = 23;
	final byte COMBAT_WIN_CHANCE_TOLERANCE_PLUS_10 = 24;
	final byte COMBAT_WIN_CHANCE_TOLERANCE_MINUS_10 = 25;
	final byte COMBAT_WIN_CHANCE_TOLERANCE_RESET_TO_DEFAULT = 26;
	final byte TURNS_LEFT_THRESHOLD_MINUS_20 = 27;
	final byte TURNS_LEFT_THRESHOLD_PLUS_20 = 28;
	final byte TURNS_LEFT_THRESHOLD_RESET_TO_DEFAULT = 29;
	final byte CREATE_NEW_CITY = 30;
	final byte NO_ACTION = 31;
	
	
	private Player owner;
	private Resources res;
	
	private List<GameAction> output = new ArrayList<GameAction>();

	public AI(GameController game, Player owner, Resources res) {
		this.game = game;
		this.owner = owner;
		this.res = res;
		this.genome = generateNewGenome();
		HashMap<CoordObject, WalkableTile> walkableMap = game.getWalkableTilesCopy();
		walkableMap.get(new CoordObject(0, 0));
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = ((float) this.genome[1]) / 7;
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		this.init_queue_length = (int) this.genome[2];
		this.init_resource_threshold = ((int) this.genome[3]) * 40;
		this.init_turns_threshold = ((int) this.genome[4]) * 10;
		this.current_turns_threshold = this.init_turns_threshold;
		this.default_behavior_code = genome[5];
		this.WriteSelf(0);
	}

	public AI(GameController game, AI parent1, AI parent2){
		this.game = game;
		Random rng = new SecureRandom();
		int num = rng.nextInt(1);
		if (num == 0) {
			this.genome = mate1(parent1.genome,parent2.genome);
			mutation(this.genome);
		} else {
			this.genome = mate2(parent1.genome,parent2.genome);
			mutation(this.genome);
		}
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = ((float) this.genome[1]) / 7;
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
		this.init_combat_pcent_win_chance = ((float) this.genome[1]) / 7;
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
	
	private GameStaticObject getRandomActiveCity() {
		List<GameStaticObject> cities = game.getPlayerCities(owner);
		Iterator<GameStaticObject> it = cities.iterator();
		while(it.hasNext()) {
			if(isStaticObjUsed(it.next()))
				it.remove();
		}
		
		Random rnd = new Random();
		int index = rnd.nextInt(cities.size());
		return cities.get(index);
	}
	
	private Collection<Player> getmine(Player p[]) {
		HashMap<Player, Integer> temp = new HashMap<Player, Integer>();
		for (int t = 0; t < 4; t++) {
			int sum = 0;
			for (GameStaticObject u : game.getPlayerObjectsOfType(p[t], "MINE"))
				sum += 50/game.getDistToClosestCity(u ,p[t]);
			temp.put(p[t], (Integer) sum);
		}

		ValueComparator vc = new ValueComparator(temp);
		TreeMap<Player, Integer> sortedMap = new TreeMap<Player, Integer>(vc);
		sortedMap.putAll(temp);
		return sortedMap.keySet();
	}
	
	private float chanceToBeKilled(List<GameUnit> attackers, GameUnit chase) {
		int sum = 0;
		for(GameUnit u: attackers) {
			sum += game.calcCombatDmg(u, chase);
		}
		return sum/chase.getHP();
	}
	
	public List<GameAction> perform(List<GameAction> actions, Player p) {
		output.clear();
		output.add(new GameAction(GameAction.ZeroAgentEvent.END_TURN, p));
		
		boolean[] arr = new boolean[21];
		for(int i = 0; i < 21; i++) arr[i] = true;
		getResponseCodes(arr);
		return output;
	}
	
	private boolean isUnitUsed(GameUnit u) {
		for(GameAction action: output)
			if(action.actor == u)
				return true;
		
		return false;
	}
	
	private boolean isStaticObjUsed(GameStaticObject so) {
		for(GameAction action: output)
			if(action.actor == so)
				return true;
		
		return false;
	}
	
	private boolean hasFreeWorkers() {
		for(GameUnit u: game.getPlayerUnits(owner))
			if(!isUnitUsed(u)) return true;
		
		return false;
				
	}
	
	private GameAction SeizeStrategicLocation(Player p){
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
	private GameAction SeizeResource() {
		ArrayList<GameUnit> ourWorkers = game.getPlayerUnitsOfType(owner, "WORKER");
		GameStaticObject best = null;
		int closestDist = Integer.MAX_VALUE;
		
		for(GameStaticObject so: game.getAllResources()) {
			for(GameUnit u: ourWorkers) {
				if(!isUnitUsed(u) && u.mapX == so.mapX && u.mapY == so.mapY)
					return new GameAction(GameAction.OneAgentEvent.BUILD_MINE, owner, u);
				
				int dist = game.getDistToClosestCity(so, owner);
				if(dist < closestDist) {
					closestDist = dist;
					best = so;
				}
			}
		}

		GameUnit bestWorker = null;
		closestDist = Integer.MAX_VALUE;
		for(GameUnit u: ourWorkers) {
			if(isUnitUsed(u)) continue;
			
			int dist = game.getDistBetween(u, best, owner);
			if(dist != -1 && dist < closestDist) {
				closestDist = dist;
				bestWorker = u;
			}
		}
		
		if(bestWorker != null)
			return new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE, owner, bestWorker, best);
		else
			return this.makeWorker();
	}
	
    private GameAction AttackPlayer1(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackPlayer2(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackPlayer3(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackStrongestMilitary(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackStrongestEconomy(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackWeakestMilitary(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction AttackWeakestEconomy(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassPlayer1(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassPlayer2(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassPlayer3(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassStrongestMilitary(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassStrongestEconomy(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction HarassWeakestMilitary(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
    
    private GameAction makeWorker() {
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}
    
    private GameAction makeTank() {
    	return makeUnit(GameAction.OneAgentEvent.BUILD_TANK);
	}
    
    private GameAction makeAir(){
    	return makeUnit(GameAction.OneAgentEvent.BUILD_AIR);
	}
    
    private GameAction makeAntiair(){
    	return makeUnit(GameAction.OneAgentEvent.BUILD_ANTIAIR);
	}
    
    private GameAction makeUnit(GameAction.OneAgentEvent buildEvent) {
    	try {
			GameUnitData data = res.getUnit(buildEvent.toString().replace("BUILD_", ""));
			if(owner.canAfford(data))
	    		return new GameAction(buildEvent, owner, getRandomActiveCity());
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		return SeizeResource();
    }
    
    private GameAction FortifyStrategicLocation(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction FortifyResource(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction FortifyCity(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction ClearUnitQueue(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction CombatWinChanceTolerancePlus10(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction CombatWinChanceToleranceMinus10(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction CombatWinChanceToleranceResetToDefault(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction TurnsLeftThresholdMinus20(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction TurnsLeftThresholdPlus20(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction TurnsLeftThresholdResetToDefault(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction CreateNewCity(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, p);
	}
	
    private GameAction NoAction(Player p){
    	return new GameAction(GameAction.ZeroAgentEvent.NULL_ACTION, p);
	}
	
}