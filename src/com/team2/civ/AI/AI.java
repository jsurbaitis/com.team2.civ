package com.team2.civ.AI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.team2.civ.Data.GameUnitData;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameAction;
import com.team2.civ.Game.GameController;
import com.team2.civ.Game.GameMap;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Game.Player;
import com.team2.civ.Map.WalkableTile;

public class AI {
	final static int condition_bits = 21;
	final static int tolerance_bits = 26;
	final static int action_points = 5;
	final static int action_bits = 5;
	final byte[] genome;

	final boolean[] conditions = new boolean[condition_bits];
	// tolerances:
	final int init_st_dev_strat_loc;
	float current_st_dev_strat_loc;
	final float init_combat_pcent_win_chance;
	float current_combat_pcent_win_chance;
	final int init_queue_length;
	final int init_resource_threshold;
	final int init_turns_threshold;
	float current_turns_threshold;
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

	private GameController game;
	private GameMap map;
	private Player owner;
	private Resources res;

	private List<GameAction> output = new ArrayList<GameAction>();

	public AI() {
		this.genome = generateNewGenome();
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

	public AI(AI parent1, AI parent2) {
		Random rng = new SecureRandom();
		int num = rng.nextInt(1);
		if (num == 0) {
			this.genome = mate1(parent1.genome, parent2.genome);
			mutation(this.genome);
		} else {
			this.genome = mate2(parent1.genome, parent2.genome);
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

	public AI(File f) {
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

	public void setGameVars(GameController game, GameMap map, Player owner) {
		this.game = game;
		this.map = map;
		this.owner = owner;
		this.res = Resources.getInstance();
	}

	private static byte[] generateNewGenome() {
		byte[] genome = new byte[(int) (6 + (((int) Math.pow(2, condition_bits) * action_points) / 1.6))];// 1.6
																											// may
																											// need
																											// to
																											// be
																											// changed
																											// later,
																											// but
																											// should
																											// correspond
																											// adequately
																											// to
																											// 5bits/action
																											// code
		Random rng = new SecureRandom();
		rng.nextBytes(genome);
		// tolerances:
		genome[0] = (byte) rng.nextInt(3); // stdev stratloc
		genome[1] = (byte) rng.nextInt(15); // win%tol
		genome[2] = (byte) rng.nextInt(7); // max queue length
		genome[3] = (byte) rng.nextInt(63); // banked resources (*40 later)
		genome[4] = (byte) rng.nextInt(63); // turns left (*10 later)
		genome[5] = (byte) rng.nextInt(31); // default action
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
				if (rng == 0)
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

	private boolean isBitSet(byte b, int bitIndex) {
		return (b & (1 << bitIndex)) != 0;
	}

	private byte getFiveBits(int index) {
		int bitIndex = (index * 5) % 8;
		int byteIndex = (index * 5) / 8 + 6;
		byte rtn = 0;

		for (int i = 0; i < 5; i++) {
			if (isBitSet(this.genome[byteIndex], bitIndex))
				rtn |= (1 << i);
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
			for (GameUnit u : map.getPlayerUnits(p[t]))
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
		List<GameStaticObject> cities = map.getPlayerCities(owner);
		Iterator<GameStaticObject> it = cities.iterator();
		while (it.hasNext()) {
			if (isStaticObjUsed(it.next()))
				it.remove();
		}

		if (cities.size() == 0)
			return null;

		Random rnd = new Random();
		int index = rnd.nextInt(cities.size());
		return cities.get(index);
	}

	private Collection<Player> getEconomyRanking(Player p[]) {
		HashMap<Player, Integer> temp = new HashMap<Player, Integer>();
		for (int t = 0; t < 4; t++) {
			int sum = 0;
			for (GameStaticObject u : map.getPlayerObjectsOfType(p[t], "MINE"))
				sum += 50 / map.getDistToClosestCity(u, p[t]);
			temp.put(p[t], (Integer) sum);
		}

		ValueComparator vc = new ValueComparator(temp);
		TreeMap<Player, Integer> sortedMap = new TreeMap<Player, Integer>(vc);
		sortedMap.putAll(temp);
		return sortedMap.keySet();
	}

	private float chanceToBeKilled(List<GameUnit> attackers,
			List<GameUnit> chases) {
		int sum = 0;
		for (GameUnit u : attackers) {
			for (GameUnit chase : chases) {
				if(chase.owner != owner)
					sum += game.calcCombatDmg(u, chase) / chase.getHP();
			}
		}
		return sum / chases.size();
	}

	public List<GameAction> perform(List<GameAction> actions) {
		output.clear();
		output.add(new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner));
		Random r = new Random();
		boolean[] arr = new boolean[21];
		for (int i = 0; i < 21; i++)
			arr[i] = r.nextBoolean();
		byte[] b_responses = getResponseCodes(arr);
		for (byte b : b_responses) {
			System.out.print(b + ": ");
			try {
				List<GameAction> ga = this.parseActionCode(b);
				for (GameAction g : ga)
					System.out.print(g + "  ");
				output.addAll(ga);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(GameUnit unit: map.getPlayerUnits(owner)) {
			if(!isUnitUsed(unit)) {
				output.add(new GameAction(GameAction.OneAgentEvent.ACTION_CHECK_ATTACK, owner, unit));
			}
		}
		
		System.out.println("\n-------");
		output.removeAll(Collections.singleton(null));
		return output;
	}

	private boolean isUnitUsed(GameUnit u) {
		for (GameAction action : output)
			if (action != null && action.actor == u)
				return true;

		return false;
	}

	private boolean isStaticObjUsed(GameStaticObject so) {
		for (GameAction action : output)
			if (action != null && action.actor == so)
				return true;

		return false;
	}

	private boolean hasFreeWorkers() {
		for (GameUnit u : map.getPlayerUnits(owner))
			if (!isUnitUsed(u))
				return true;

		return false;

	}

	private List<GameUnit> getFreeUnits() {
		List<GameUnit> freeUnits = new ArrayList<GameUnit>();
		for (GameUnit u : map.getPlayerUnitsOfType(owner, "TANK", "ANTIAIR",
				"AIR")) {
			if (u.AP > 0)
				freeUnits.add(u);
		}
		return freeUnits;
	}
	
	private GameAction BuildPowerplant() {
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private List<GameAction> SeizeStrategicLocation() {
		List<GameAction> rtn = new ArrayList<GameAction>();

		Collection<WalkableTile> walkableMap = map.getWalkableMap();
		double stdev = 0;
		for (Double d : map.getStratLocValues().values()) {
			stdev += Math.pow(d - map.avgStratLocValue, 2);
		}

		stdev /= walkableMap.size();
		stdev = Math.sqrt(stdev);

		List<WalkableTile> stratLocs = new ArrayList<WalkableTile>();
		HashMap<WalkableTile, Double> stratLocValues = map.getStratLocValues();
		for (Entry<WalkableTile, Double> e : stratLocValues.entrySet()) {
			if (e.getValue() > stdev * current_st_dev_strat_loc) {
				stratLocs.add(e.getKey());
			}
		}

		if (stratLocs.size() == 0) {
			if (this.current_st_dev_strat_loc > 1) {
				this.current_st_dev_strat_loc--;
				return SeizeStrategicLocation();
			} else if (this.default_behavior_code != this.SEIZE_STRATEGIC_LOCATION) {
				rtn.add(new GameAction(GameAction.ZeroAgentEvent.END_TURN,
						owner)); // TODO: fix
				return rtn;
			} else {
				rtn.add(SeizeResource());
				return rtn;
			}
		}

		List<GameUnit> freeUnits = getFreeUnits();
		if (freeUnits.size() > 0) {
			for (WalkableTile wt : stratLocs) {
				if (map.isTileFree(wt)) {
					rtn.add(new GameAction(
							GameAction.TwoAgentEvent.ACTION_MOVE, owner,
							freeUnits.get(0), wt));
					return rtn;
				}
			}

			List<GameUnit> chases = map.getUnitsOnTile(stratLocs.get(0));
			List<GameUnit> toSend = getUnitsToSend(chases);
			if(toSend != null) {
				for(GameUnit u: toSend) {
					rtn.add(new GameAction(
							GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner, u,
							stratLocs.get(0)));
				}
				
				return rtn;
			}
		}

		rtn.add(makeRandomUnit());
		return rtn;
	}

	private GameAction SeizeResource() {
		ArrayList<GameUnit> ourWorkers = map.getPlayerUnitsOfType(owner,
				"WORKER");
		GameStaticObject best = null;
		int closestDist = Integer.MAX_VALUE;

		for (GameStaticObject so : map.getAllResources()) {
			for (GameUnit u : ourWorkers) {
				if (!isUnitUsed(u) && u.mapX == so.mapX && u.mapY == so.mapY)
					return new GameAction(GameAction.OneAgentEvent.BUILD_MINE,
							owner, u);

				if(map.isTileFree(so)) {
					int dist = map.getDistToClosestCity(so, owner);
					if (dist < closestDist) {
						closestDist = dist;
						best = so;
					}
				}
			}
		}

		GameUnit bestWorker = null;
		closestDist = Integer.MAX_VALUE;
		for (GameUnit u : ourWorkers) {
			if (isUnitUsed(u))
				continue;

			int dist = map.getDistBetween(u, best, owner);
			if (dist != -1 && dist < closestDist) {
				closestDist = dist;
				bestWorker = u;
			}
		}

		if (bestWorker != null)
			return new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE, owner,
					bestWorker, best);
		else
			return this.makeWorker();
	}
	
	private List<GameAction> attackPlayer(Player p) {
		List<GameAction> rtn = new ArrayList<GameAction>();
		List<GameStaticObject> pCities = map.getPlayerCities(p);
		List<GameUnit> pUnits = map.getPlayerUnits(p);
		
		GameStaticObject closestCity = null;
		int shortestDist = Integer.MAX_VALUE;
		
		for(GameStaticObject enemc: pCities) {
			for(GameStaticObject myc: map.getPlayerCities(owner)) {
				int dist = map.getDistBetween(enemc, myc);
				if (dist != -1 && dist < shortestDist) {
					shortestDist = dist;
					closestCity = enemc;
				}
			}
		}
		
		if(closestCity != null) {
			List<GameUnit> chases = map.getUnitsOnAndAroundTile(closestCity);
			List<GameUnit> toSend = getUnitsToSend(chases);
			if(toSend != null) {
				for(GameUnit u: toSend) {
					rtn.add(new GameAction(
							GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner, u,
							closestCity));
				}
				
				return rtn;
			}
			else {
				rtn.add(this.makeRandomUnit());
				return rtn;
			}
		}
		
		return rtn;
	}
	
	private List<GameAction> AttackStrongestMilitary(){
        Player[] militaryRankings = (Player[]) getmilitaryrankings((Player[]) game.players.toArray()).toArray();
        if (militaryRankings[0] == owner) {
            return attackPlayer(militaryRankings[1]);
        } 
        
        return attackPlayer(militaryRankings[0]);
    }
    private List<GameAction> AttackStrongestEconomy(){
        Player[] economyRankings = (Player[]) getEconomyRanking((Player[]) game.players.toArray()).toArray();
        if (economyRankings[0] == owner) {
            return attackPlayer(economyRankings[1]);
        } 
        
        return attackPlayer(economyRankings[0]);
    }
    
    private List<GameAction> AttackWeakestMilitary(){
        Player[] militaryRankings = (Player[]) getmilitaryrankings((Player[]) game.players.toArray()).toArray();
        if (militaryRankings[militaryRankings.length-1] == owner) {
            return attackPlayer(militaryRankings[militaryRankings.length - 2]);
        } 
        
        return attackPlayer(militaryRankings[militaryRankings.length - 1]);
    }
    
    private List<GameAction> AttackWeakestEconomy(){
        Player[] economyRankings = (Player[]) getEconomyRanking((Player[]) game.players.toArray()).toArray();
        if (economyRankings[economyRankings.length-1] == owner) {
            return attackPlayer(economyRankings[economyRankings.length - 2]);
        } 
        
        return attackPlayer(economyRankings[economyRankings.length - 1]);
    }

	private GameAction harassPlayer(Player p) {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private GameAction harassStrongestMilitary(){
        Player[] militaryRankings = (Player[]) getmilitaryrankings((Player[]) game.players.toArray()).toArray();
        if (militaryRankings[0] == owner) {
            return harassPlayer(militaryRankings[1]);
        } 
        
        return harassPlayer(militaryRankings[0]);
    }
    
    private GameAction HarassStrongestEconomy(){
        Player[] economyRankings = (Player[]) getEconomyRanking((Player[]) game.players.toArray()).toArray();
        if (economyRankings[0] == owner) {
            return harassPlayer(economyRankings[1]);
        } 
        
        return harassPlayer(economyRankings[0]);
    }
    
    private GameAction HarassWeakestMilitary(){
        Player[] militaryRankings = (Player[]) getmilitaryrankings((Player[]) game.players.toArray()).toArray();
        if (militaryRankings[militaryRankings.length-1] == owner) {
            return harassPlayer(militaryRankings[militaryRankings.length - 2]);
        } 
        
        return harassPlayer(militaryRankings[militaryRankings.length - 1]);
    }

	private GameAction makeRandomUnit() {
		return makeUnit(GameAction.OneAgentEvent.BUILD_TANK);
	}

	private GameAction makeWorker() {
		try {
			if (owner.canAfford(res.getUnit("WORKER"))) {
				return makeUnit(GameAction.OneAgentEvent.BUILD_WORKER);
			}
			if (!owner.canAffordMetal(res.getUnit("WORKER"))) {
				return this.SeizeResource();
			}
			if (!owner.canAffordPower(res.getUnit("WORKER"))) {
				return this.BuildPowerplant();
			}
			if (this.default_behavior_code != this.MAKE_WORKER
			// TODO: && other things
			) {
				return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner); // TODO:
																					// replace
																					// this
																					// later
			}
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private GameAction makeTank() {
		return makeUnit(GameAction.OneAgentEvent.BUILD_TANK);
	}

	private GameAction makeAir() {
		return makeUnit(GameAction.OneAgentEvent.BUILD_AIR);
	}

	private GameAction makeAntiAir() {
		return makeUnit(GameAction.OneAgentEvent.BUILD_ANTIAIR);
	}

	private GameAction makeUnit(GameAction.OneAgentEvent buildEvent) {
		try {
			GameUnitData data = res.getUnit(buildEvent.toString().replace(
					"BUILD_", ""));
			if (owner.canAfford(data)) {
				GameStaticObject city = getRandomActiveCity();
				if (city != null) {
					return new GameAction(buildEvent, owner, city);
				} else {
					return null; //TODO: unit queue
				}
			}
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		return SeizeResource();
	}
	
	private GameAction FortifyLocation(WalkableTile location){
    	return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
    }

	private GameAction FortifyStrategicLocation() {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private GameAction FortifyResource(){
		ArrayList<WalkableTile> locations = new ArrayList<WalkableTile>();
    	List<GameStaticObject> resources = map.getPlayerObjectsOfType(owner, "CITY", "POWERPLANT");
    	if (resources.size() == 0){
    		if (owner.scoreEconomy == 0){
    			return this.SeizeResource();
    		}
    		return this.BuildPowerplant();
    	}
    	for (WalkableTile resource : resources){
    		if (map.getUnitsOnTile(resource).size() != 0) locations.add(resource);
    	}
    	Random r = new Random();
    	if (locations.size() > 0){
    		return FortifyLocation(locations.get(r.nextInt(locations.size()-1)));
    	}
    	if (r.nextBoolean()){
    		return this.SeizeResource();
    	}
    	return this.BuildPowerplant();
	}

	private GameAction FortifyCity() {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private GameAction ClearUnitQueue() {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private List<GameAction> CombatWinChanceTolerancePlus10() throws Exception {
		this.current_combat_pcent_win_chance *= 1.1;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_PLUS_10) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> CombatWinChanceToleranceMinus10() throws Exception {
		this.current_combat_pcent_win_chance *= .9;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_MINUS_10) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> CombatWinChanceToleranceResetToDefault()
			throws Exception {
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_RESET_TO_DEFAULT) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdMinus20() throws Exception {
		this.current_turns_threshold *= .8;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_MINUS_20) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdPlus20() throws Exception {
		this.current_turns_threshold *= 1.2;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_PLUS_20) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdResetToDefault()
			throws Exception {
		this.current_turns_threshold = this.init_turns_threshold;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_RESET_TO_DEFAULT) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private GameAction CreateNewCity() throws ResNotFoundException {
		if (!owner.canAfford(res.getStaticObject("CITY"))) {
			return this.SeizeResource();
		}
		ArrayList<GameUnit> ourWorkers = map.getPlayerUnitsOfType(owner,
				"WORKER");
		ArrayList<GameUnit> freeworkers = new ArrayList<GameUnit>();
		for (GameUnit u : ourWorkers) {
			if (!isUnitUsed(u)) {
				freeworkers.add(u);
			}
		}
		if (freeworkers.size() == 0) {
			return this.makeWorker();
		}
		ArrayList<WalkableTile> city_candidate_list = getCityCandidates();
		WalkableTile current_candidate = null;
		int candidate_score = Integer.MAX_VALUE;
		for (WalkableTile candidate : city_candidate_list) {
			int score = this.map.getDistToClosestCity(candidate, owner);
			if (score < candidate_score) {
				candidate_score = score;
				current_candidate = candidate;
			}
		}
		GameUnit closest_worker = null;
		int closest_worker_distance = Integer.MAX_VALUE;
		for (GameUnit u : freeworkers) {
			int u_dist = map.getDistBetween(u, current_candidate, owner);
			if (u_dist != -1 && u_dist < closest_worker_distance) {
				closest_worker = u;
				closest_worker_distance = u_dist;
			}
			if (closest_worker_distance == 0) {
				return new GameAction(GameAction.OneAgentEvent.BUILD_CITY,
						owner, closest_worker);
			}
		}
		return new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE, owner,
				closest_worker, current_candidate);
	}

	private ArrayList<WalkableTile> getCityCandidates() {
		// TODO Auto-generated method stub
		/*
		 * Find good city locations using map generation criteria Narrow down
		 * above by determining which locations are accessible to AI
		 */
		return null;
	}

	private GameAction NoAction() {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private GameAction harassWeakestEconomy() {
		return new GameAction(GameAction.ZeroAgentEvent.END_TURN, owner);
	}

	private List<GameAction> parseActionCode(byte b) throws Exception {
		List<GameAction> rtn = new ArrayList<GameAction>();

		if (b == SEIZE_STRATEGIC_LOCATION) {
			rtn.addAll(this.SeizeStrategicLocation());
		} else if (b == SEIZE_RESOURCE) {
			rtn.add(this.SeizeResource());
		} else if (b == ATTACK_PLAYER_1) {
			rtn.addAll(this.attackPlayer(getPlayer(1)));
		} else if (b == ATTACK_PLAYER_2) {
			rtn.addAll(this.attackPlayer(getPlayer(2)));
		} else if (b == ATTACK_PLAYER_3) {
			rtn.addAll(this.attackPlayer(getPlayer(3)));
		} else if (b == ATTACK_STRONGEST_MILITARY) {
			rtn.addAll(this.AttackStrongestMilitary());
		} else if (b == ATTACK_STRONGEST_ECONOMY) {
			rtn.addAll(this.AttackStrongestEconomy());
		} else if (b == ATTACK_WEAKEST_MILITARY) {
			rtn.addAll(this.AttackWeakestMilitary());
		} else if (b == ATTACK_WEAKEST_ECONOMY) {
			rtn.addAll(this.AttackWeakestEconomy());
		} else if (b == HARASS_PLAYER_1) {
			rtn.add(this.harassPlayer(getPlayer(1)));
		} else if (b == HARASS_PLAYER_2) {
			rtn.add(this.harassPlayer(getPlayer(2)));
		} else if (b == HARASS_PLAYER_3) {
			rtn.add(this.harassPlayer(getPlayer(3)));
		} else if (b == HARASS_STRONGEST_MILITARY) {
			rtn.add(this.harassStrongestMilitary());
		} else if (b == HARASS_STRONGEST_ECONOMY) {
			rtn.add(this.HarassStrongestEconomy());
		} else if (b == HARASS_WEAKEST_MILITARY) {
			rtn.add(this.HarassWeakestMilitary());
		} else if (b == HARASS_WEAKEST_ECONOMY) {
			rtn.add(this.harassWeakestEconomy());
		} else if (b == MAKE_WORKER) {
			rtn.add(this.makeWorker());
		} else if (b == MAKE_TANK) {
			rtn.add(this.makeTank());
		} else if (b == MAKE_AIR) {
			rtn.add(this.makeAir());
		} else if (b == MAKE_ANIAIR) {
			rtn.add(this.makeAntiAir());
		} else if (b == FORTIFY_STRATEGIC_LOCATION) {
			rtn.add(this.FortifyStrategicLocation());
		} else if (b == FORTIFY_RESOURCE) {
			rtn.add(this.FortifyResource());
		} else if (b == FORTIFY_CITY) {
			rtn.add(this.FortifyCity());
		} else if (b == CLEAR_UNIT_QUEUE) {
			rtn.add(this.ClearUnitQueue());
		} else if (b == COMBAT_WIN_CHANCE_TOLERANCE_PLUS_10) {
			rtn.addAll(this.CombatWinChanceTolerancePlus10());
		} else if (b == COMBAT_WIN_CHANCE_TOLERANCE_MINUS_10) {
			rtn.addAll(this.CombatWinChanceToleranceMinus10());
		} else if (b == COMBAT_WIN_CHANCE_TOLERANCE_RESET_TO_DEFAULT) {
			rtn.addAll(this.CombatWinChanceToleranceResetToDefault());
		} else if (b == TURNS_LEFT_THRESHOLD_MINUS_20) {
			rtn.addAll(this.TurnsLeftThresholdMinus20());
		} else if (b == TURNS_LEFT_THRESHOLD_PLUS_20) {
			rtn.addAll(this.TurnsLeftThresholdPlus20());
		} else if (b == TURNS_LEFT_THRESHOLD_RESET_TO_DEFAULT) {
			rtn.addAll(this.TurnsLeftThresholdResetToDefault());
		} else if (b == CREATE_NEW_CITY) {
			rtn.add(this.CreateNewCity());
		} else if (b == NO_ACTION) {
			rtn.add(this.NoAction());
		} else {
			throw new Exception("No action found for byte: " + b);
		}

		return rtn;
	}
	
	//Gets player 1, 2 or 3
	private Player getPlayer(int n) {
		int count = 1;
		for(Player p: game.players) {
			if(p != owner && count == n)
				return p;
			else
				count++;
		}
		return null;
	}
	
	private List<GameUnit> getUnitsToSend(List<GameUnit> chases) {
		List<GameUnit> freeUnits = getFreeUnits();
		
		if (chanceToBeKilled(freeUnits, chases) > current_combat_pcent_win_chance) {
			GameUnit removed = freeUnits.get(freeUnits.size() - 1);
			freeUnits.remove(removed);
			while (chanceToBeKilled(freeUnits, chases) > current_combat_pcent_win_chance) {
				removed = freeUnits.get(freeUnits.size() - 1);
				freeUnits.remove(removed);
			}
			freeUnits.add(removed);
			return freeUnits;
		} else {
			return null;
		}
	}
	
	private boolean[] getAIPosition(Collection <Player> p){
		for(int i = 0; i < p.size(); i++){
			if(((ArrayList<Player>) p).get(i) == owner)
				return this.intToBits(i);
		}
		return null;
	}
	private boolean isMetalOverThresh(){
		 return (owner.metal > init_resource_threshold);
	}
	private boolean isTurnOverThresh(){
		 return (game.turnsLeft > current_turns_threshold);
	}
	
	private Collection<Player> strategicLocationRanking(Player p[]){
		//for ()
		return null;
	}
	
	private boolean[] majorityUnit() {
        int tankCount = 0;
        int airCount = 0;
        int antiAirCount = 0;
        boolean[] antiAir = {true,true};
        boolean[] air = {true,false};
        boolean[] tank = {false,false};
        for (GameUnit u : map.getUnits()) {
            if (u.data.id.equals("TANK") ) {
                tankCount++;
            } else if (u.data.id.equals("AIR")){
                airCount++;
            } else if (u.data.id.equals("ANTIAIR")) {
                antiAirCount++;
            }
        } if (tankCount > airCount && tankCount > antiAirCount) {
            return tank;
        } else if (antiAirCount > airCount && antiAirCount > tankCount) {
            return antiAir;
        } return air;
    }
}