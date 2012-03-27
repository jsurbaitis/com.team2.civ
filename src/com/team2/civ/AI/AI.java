package com.team2.civ.AI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.GameStaticObjectData;
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
	private static Random rnd = new SecureRandom();

	private List<String> unitQueue = new ArrayList<String>();
	private List<GameAction> output = new ArrayList<GameAction>();

	public AI() {
		this.genome = generateNewGenome();
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = (float) (((float) this.genome[1]) / 15);
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		this.init_queue_length = (int) this.genome[2];
		this.init_resource_threshold = ((int) this.genome[3]) * 40;
		this.init_turns_threshold = ((int) this.genome[4]) * 10;
		this.current_turns_threshold = this.init_turns_threshold;
		this.default_behavior_code = genome[5];
	}

	public AI(AI parent1, AI parent2) {
		int num = rnd.nextInt(2);
		if (num == 0) {
			this.genome = mate1(parent1.genome, parent2.genome);
			mutation(this.genome);
		} else {
			this.genome = mate2(parent1.genome, parent2.genome);
			mutation(this.genome);
		}
		this.init_st_dev_strat_loc = (int) this.genome[0];
		this.current_st_dev_strat_loc = this.init_st_dev_strat_loc;
		this.init_combat_pcent_win_chance = (float) (((float) this.genome[1]) / 15);
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
		this.init_combat_pcent_win_chance = (float) (((float) this.genome[1]) / 15);
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
		byte[] genome = new byte[(int) (6 + (((int) Math.pow(2, condition_bits) * action_points) / 1.6))];// 1.6 not smart
		rnd.nextBytes(genome);
		// tolerances:
		genome[0] = (byte) rnd.nextInt(4); // stdev stratloc
		genome[1] = (byte) rnd.nextInt(16); // win%tol
		genome[2] = (byte) rnd.nextInt(8); // max queue length
		genome[3] = (byte) rnd.nextInt(64); // banked resources (*40 later)
		genome[4] = (byte) rnd.nextInt(64); // turns left (*10 later)
		genome[5] = (byte) rnd.nextInt(31); // default action
		return genome;
	}

	private static byte[] mate1(byte[] parent1, byte[] parent2) {
		boolean rng = rnd.nextBoolean();
		byte out[] = new byte[parent1.length];
		int len = parent1.length / 2;

		if (rng) {
			for (int i = 0; i < len; i++) {
				out[i] = parent1[i];
			}
			for (int i = len; i < parent1.length; i++) {
				out[i] = parent2[i];
			}
		} else {
			for (int i = 0; i < len; i++) {
				out[i] = parent2[i];
			}
			for (int i = len; i < parent1.length; i++) {
				out[i] = parent1[i];
			}
		}
		return out;
	}

	private static byte[] mate2(byte[] parent1, byte[] parent2) {
		boolean male = rnd.nextBoolean();
		int dominance = rnd.nextInt(parent1.length);
		byte[] out = new byte[parent1.length];
		int index = 0;
		if (male){
			for (int i = 0; i < dominance; i++){
				out[i] = parent1[i];
				index++;
			}
			while (index < parent1.length){
				out[index] = parent2[index];
				index++;
			}
		} else {
			for (int i = 0; i < dominance; i++){
				out[i] = parent2[i];
				index++;
			}
			while (index < parent1.length){
				out[index] = parent1[index];
				index++;
			}
		}
		return out;
	}

	private static void mutation(byte[] parent1) {
		int rng = rnd.nextInt(20);
		if (rng == 1) {
			for (int i = 0; i < parent1.length; i++) {
				rng = rnd.nextInt(15);
				if (rng == 1)
					if (i == 0){
						parent1[i] = (byte) rnd.nextInt(4);// stdev stratloc
					} else if (i == 1){
						parent1[1] = (byte) rnd.nextInt(16); // win%tol
					} else if (i == 2){
						parent1[2] = (byte) rnd.nextInt(8); // max queue length
					} else if (i == 3){
						parent1[3] = (byte) rnd.nextInt(64); // banked resources (*40 later)
					} else if (i == 4){	
						parent1[4] = (byte) rnd.nextInt(64); // turns left (*10 later)
					} else if (i == 5){	
						parent1[5] = (byte) rnd.nextInt(31); // default action
					} else {
					parent1[i] = (byte) rnd.nextInt();
					}
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

	private static void BytesToFile(byte[] bytes, int genomeNumber) {
		File f = new File("genomes/genome_" + genomeNumber);
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

	private static boolean[] intTo2Bits(int in) {
		boolean[] rtn = new boolean[2];
		if (in == 0) {
			rtn[0] = false;
			rtn[1] = false;
		} else if (in == 1) {
			rtn[0] = false;
			rtn[1] = true;
		} else if (in == 2) {
			rtn[0] = true;
			rtn[1] = false;
		} else {
			rtn[0] = true;
			rtn[1] = true;
		}

		return rtn;
	}

	private boolean[] getEnvironmentalConditions() {
		boolean[] rtn = new boolean[21];
		boolean[] temp;

		temp = this.getAIPosition(game.getEconomyRankings());
		rtn[0] = temp[0];
		rtn[1] = temp[1];

		temp = this.getAIPosition(game.getMilitaryRankings());
		rtn[2] = temp[0];
		rtn[3] = temp[1];

		temp = this.getAIPosition(game.getStratLocRankings());
		rtn[4] = temp[0];
		rtn[5] = temp[1];

		rtn[6] = owner.attackedByP1;
		rtn[7] = owner.attackedByP2;
		rtn[8] = owner.attackedByP3;

		rtn[9] = owner.lostMine;
		rtn[10] = owner.lostPowerplant;
		rtn[11] = owner.lostCity;
		rtn[12] = owner.lostUnit;

		rtn[13] = owner.builtMine;
		rtn[14] = owner.builtPowerplant;
		rtn[15] = owner.builtCity;

		rtn[16] = isUnitsQueuedOverThresh();
		rtn[17] = isMetalOverThresh();
		rtn[18] = isTurnOverThresh();

		temp = majorityUnit();
		rtn[19] = temp[0];
		rtn[20] = temp[1];

		return rtn;
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

		int index = rnd.nextInt(cities.size());
		return cities.get(index);
	}

	private float chanceToBeKilled(List<GameUnit> attackers,
			List<GameUnit> chases) {
		if (chases.size() == 0)
			return 1;

		int sum = 0;
		for (GameUnit u : attackers) {
			for (GameUnit chase : chases) {
				if (chase.owner != owner)
					sum += game.calcCombatDmg(u, chase) / chase.getHP();
			}
		}
		return sum / chases.size();
	}

	public List<GameAction> perform() {
		output.clear();
		
		if(Team2Civ.DEBUG_OUTPUT) {
			System.out.println("Turn: "+game.turnsLeft+" / "+this.current_turns_threshold);
			System.out.println("Turn threshold: "+this.init_turns_threshold);
			System.out.println("Current strat loc: "+this.current_st_dev_strat_loc);
			System.out.println("Current pcent win chance: "+this.current_combat_pcent_win_chance);
			System.out.println("Queue: "+this.unitQueue.size()+" / "+this.init_queue_length);
			System.out.println("Metal: "+owner.metal+" / "+this.init_resource_threshold);
		}

		byte[] b_responses = getResponseCodes(getEnvironmentalConditions());
		for (byte b : b_responses) {
			List<GameAction> ga = this.parseActionCode(b);
			output.addAll(ga);
			if(Team2Civ.DEBUG_OUTPUT) System.out.println(" RETURN");
		}

		if (unitQueue.size() > 0) {
			for (GameStaticObject city : map.getPlayerCities(owner)) {
				if (!isStaticObjUsed(city)) {
					String id = "BUILD_" + unitQueue.get(0);
					GameAction.OneAgentEvent event = GameAction.OneAgentEvent
							.valueOf(id);
					output.add(new GameAction(event, owner, city));

					unitQueue.remove(0);
					if (unitQueue.size() < 1)
						break;
				}
			}
		}

		for (GameUnit unit : map.getPlayerUnits(owner)) {
			if (!isUnitUsed(unit)) {
				output.add(new GameAction(
						GameAction.OneAgentEvent.ACTION_CHECK_ATTACK, owner,
						unit));
			}
		}

		if(Team2Civ.DEBUG_OUTPUT) System.out.println("\n-------");
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

	private List<GameAction> BuildPowerplant() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("BuildPowerPlant -> ");
		
		List<GameAction> out = new ArrayList<GameAction>();
		
		ArrayList<GameUnit> ourWorkers = map.getPlayerUnitsOfType(owner, "WORKER");
		ArrayList<GameUnit> freeworkers = new ArrayList<GameUnit>();
		for (GameUnit u : ourWorkers) {
			if (!isUnitUsed(u)) {
				freeworkers.add(u);
			}
			if ((freeworkers.size() > 0) && (map.getObjBelowUnit(u) == null)) {
				GameStaticObjectData data;
				try {
					data = res.getStaticObject("POWERPLANT");
					if (owner.canAfford(data)) {
						out.add(new GameAction(GameAction.OneAgentEvent.BUILD_POWERPLANT,
								owner, u));
						return out;
					}
				} catch (ResNotFoundException e) {
					e.printStackTrace();
				}

				out.addAll(SeizeResource());
				return out;
			} else {
				for (GameUnit worker: freeworkers){
					for (WalkableTile tile : map.getWalkableMap()){
						if (Math.abs(tile.mapX - worker.mapX) < 5 && Math.abs(tile.mapY - worker.mapY) < 5) {
							if(map.getDistBetween(tile, worker) != 0) {
								out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE,owner,worker,tile));
								return out;
							}
						}
					}
				}
				//out.addAll(makeWorker()); //TODO: suspicious 2
			}
		}

		return out;
	}

	private List<GameAction> SeizeStrategicLocation(Double stdev) {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("SeizeStrategicLocation -> ");
		
		List<GameAction> rtn = new ArrayList<GameAction>();

		if(stdev == null) {
			Collection<WalkableTile> walkableMap = map.getWalkableMap();
			double new_stdev = 0;
			for (Double d : map.getStratLocValues().values()) {
				new_stdev += Math.pow(d - map.avgStratLocValue, 2);
			}

			new_stdev /= walkableMap.size();
			stdev = Math.sqrt(new_stdev);
		}

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
				return SeizeStrategicLocation(stdev);
			} else if (this.default_behavior_code != this.SEIZE_STRATEGIC_LOCATION) {
				rtn.addAll(this.parseActionCode(this.default_behavior_code));
				return rtn;
			} else {
				rtn.addAll(SeizeResource());
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
			if (toSend != null) {
				for (GameUnit u : toSend) {
					rtn.add(new GameAction(
							GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner,
							u, stratLocs.get(0)));
				}

				return rtn;
			}
		}

		rtn.addAll(makeRandomUnit());
		return rtn;
	}

	private List<GameAction> SeizeResource() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("SeizeResource -> ");
		
		List<GameAction> out = new ArrayList<GameAction>();
		ArrayList<GameUnit> ourWorkers = map.getPlayerUnitsOfType(owner,
				"WORKER");
		GameStaticObject best = null;
		int closestDist = Integer.MAX_VALUE;

		for (GameStaticObject so : map.getMetalNodes()) {
			for (GameUnit u : ourWorkers) {
				if (!isUnitUsed(u) && u.mapX == so.mapX && u.mapY == so.mapY) {
					out.add(new GameAction(GameAction.OneAgentEvent.BUILD_MINE,
							owner, u));
					return out;
				}
				if (map.isTileFree(so)) {
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
			out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE, owner,
					bestWorker, best));
		else
			if (!this.hasFreeWorkers()) out.addAll(this.makeWorker());
		
		return out;
	}

	private List<GameAction> attackPlayer(Player p) {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("AttackPlayer -> ");
		
		List<GameAction> rtn = new ArrayList<GameAction>();
		List<GameStaticObject> pCities = map.getPlayerCities(p);

		GameStaticObject closestCity = null;
		int shortestDist = Integer.MAX_VALUE;

		for (GameStaticObject enemc : pCities) {
			for (GameStaticObject myc : map.getPlayerCities(owner)) {
				int dist = map.getDistBetween(enemc, myc);
				if (dist != -1 && dist < shortestDist) {
					shortestDist = dist;
					closestCity = enemc;
				}
			}
		}

		if (closestCity != null) {
			List<GameUnit> chases = map.getUnitsOnAndAroundTile(closestCity);
			List<GameUnit> toSend = getUnitsToSend(chases);
			if (toSend != null) {
				for (GameUnit u : toSend) {
					rtn.add(new GameAction(
							GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner,
							u, closestCity));
				}

				return rtn;
			} else {
				rtn.addAll(this.makeRandomUnit());
				return rtn;
			}
		}

		return rtn;
	}

	private List<GameAction> AttackStrongestMilitary() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("AttackStrongMil -> ");
		
		List<Player> militaryRankings = game.getMilitaryRankings();
		if (militaryRankings.get(0) == owner) {
			return attackPlayer(militaryRankings.get(1));
		}

		return attackPlayer(militaryRankings.get(0));
	}

	private List<GameAction> AttackStrongestEconomy() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("AttackStrongEcon -> ");
		
		List<Player> economyRankings = game.getEconomyRankings();
		if (economyRankings.get(0) == owner) {
			return attackPlayer(economyRankings.get(1));
		}

		return attackPlayer(economyRankings.get(0));
	}

	private List<GameAction> AttackWeakestMilitary() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("AttackWeakMil -> ");
		
		List<Player> militaryRankings = game.getMilitaryRankings();
		if (militaryRankings.get(militaryRankings.size() - 1) == owner) {
			return attackPlayer(militaryRankings
					.get(militaryRankings.size() - 2));
		}

		return attackPlayer(militaryRankings.get(militaryRankings.size() - 1));
	}

	private List<GameAction> AttackWeakestEconomy() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("AttackWeakEcon -> ");
		
		List<Player> economyRankings = game.getEconomyRankings();
		if (economyRankings.get(economyRankings.size() - 1) == owner) {
			return attackPlayer(economyRankings.get(economyRankings.size() - 2));
		}

		return attackPlayer(economyRankings.get(economyRankings.size() - 1));
	}

	private List<GameAction> harassPlayer(Player p) {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("HarassPlayer -> ");
		
		ArrayList<GameAction> out = new ArrayList<GameAction>();
		int least_dist = Integer.MAX_VALUE;
		GameStaticObject best_candidate = null;
		List<GameStaticObject> resources = map.getPlayerObjectsOfType(p, "MINE", "POWERPLANT");
		for (GameStaticObject resource : resources) {
			int dist = map.getDistToClosestCity(resource, owner);
			if ((dist != -1) && (dist < least_dist)) {
				least_dist = dist;
				best_candidate = resource;
			} 
		} 
		
		if(best_candidate == null) return out;
		
		List<GameUnit> unitsToSend = getUnitsToSend(map.getUnitsOnTile(best_candidate));
		if(unitsToSend != null) {
			for (GameUnit u : unitsToSend) {
				out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE,owner, u, best_candidate));
			}
		}
		
		return out;
	}

	private List<GameAction> harassStrongestMilitary() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("HarassStrongMil -> ");
		
		List<Player> militaryRankings = game.getMilitaryRankings();
		if (militaryRankings.get(0) == owner) {
			return harassPlayer(militaryRankings.get(1));
		}

		return harassPlayer(militaryRankings.get(0));
	}

	private List<GameAction> HarassStrongestEconomy() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("HarassStrongEcon -> ");
		
		List<Player> economyRankings = game.getEconomyRankings();
		if (economyRankings.get(0) == owner) {
			return harassPlayer(economyRankings.get(1));
		}

		return harassPlayer(economyRankings.get(0));
	}

	private List<GameAction> HarassWeakestMilitary() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("HarassWeakMil -> ");
		
		List<Player> militaryRankings = game.getMilitaryRankings();
		if (militaryRankings.get(militaryRankings.size() - 1) == owner) {
			return harassPlayer(militaryRankings
					.get(militaryRankings.size() - 2));
		}

		return harassPlayer(militaryRankings.get(militaryRankings.size() - 1));
	}

	private List<GameAction> harassWeakestEconomy() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("HarassWeakEcon -> ");
		
		List<Player> economyRankings = game.getEconomyRankings();
		if (economyRankings.get(economyRankings.size() - 1) == owner) {
			return harassPlayer(economyRankings.get(economyRankings.size() - 2));
		}

		return harassPlayer(economyRankings.get(economyRankings.size() - 1));
	}

	private List<GameAction> makeRandomUnit() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeRnd -> ");
		
		List<GameAction.OneAgentEvent> possibilities = new ArrayList<GameAction.OneAgentEvent>();

		GameUnitData data;
		
		try {
			data = res.getUnit("TANK");
			if (!owner.canAffordPower(data)){
				return this.BuildPowerplant();
			}
			if (owner.canAfford(data))
				possibilities.add(GameAction.OneAgentEvent.BUILD_TANK);
			data = res.getUnit("AIR");
			if (owner.canAfford(data))
				possibilities.add(GameAction.OneAgentEvent.BUILD_AIR);
			data = res.getUnit("ANTIAIR");
			if (owner.canAfford(data))
				possibilities.add(GameAction.OneAgentEvent.BUILD_ANTIAIR);
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		if (possibilities.size() == 0)
			return new ArrayList<GameAction>();

		int index = rnd.nextInt(possibilities.size());
		return makeUnit(possibilities.get(index));
	}

	private List<GameAction> makeWorker() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeWorker -> ");
		
		List<GameAction> out = new ArrayList<GameAction>();
		try {
			if (owner.canAfford(res.getUnit("WORKER"))) {
				out.addAll(makeUnit(GameAction.OneAgentEvent.BUILD_WORKER));
			}
			else if (!owner.canAffordMetal(res.getUnit("WORKER")) && this.hasFreeWorkers()) {
				out.addAll(this.SeizeResource());
			}
			else if (!owner.canAffordPower(res.getUnit("WORKER")) && this.hasFreeWorkers()) {
				out.addAll(this.BuildPowerplant()); //TODO: suspicious 1
			}
			else if (this.default_behavior_code != this.MAKE_WORKER 
				  && this.default_behavior_code != this.FORTIFY_CITY
				  && this.default_behavior_code != this.SEIZE_RESOURCE
				  && this.default_behavior_code != this.MAKE_AIR
				  && this.default_behavior_code != this.MAKE_ANIAIR
				  && this.default_behavior_code != this.MAKE_TANK
				  && this.default_behavior_code != this.SEIZE_STRATEGIC_LOCATION
				  && this.default_behavior_code != this.CREATE_NEW_CITY
				  && this.default_behavior_code != this.FORTIFY_RESOURCE
				  && this.default_behavior_code != this.FORTIFY_STRATEGIC_LOCATION) {
				return this.parseActionCode(this.default_behavior_code);
			}
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	private List<GameAction> makeTank() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeTank -> ");
		
		return makeUnit(GameAction.OneAgentEvent.BUILD_TANK);
	}

	private List<GameAction> makeAir() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeAir -> ");
		
		return makeUnit(GameAction.OneAgentEvent.BUILD_AIR);
	}

	private List<GameAction> makeAntiAir() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeAntiAir -> ");
		
		return makeUnit(GameAction.OneAgentEvent.BUILD_ANTIAIR);
	}

	private List<GameAction> makeUnit(GameAction.OneAgentEvent buildEvent) {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("MakeUnit -> ");
		
		List<GameAction> out = new ArrayList<GameAction>();
		
		try {
			GameUnitData data = res.getUnit(buildEvent.toString().replace(
					"BUILD_", ""));
			if (owner.canAfford(data)) {
				GameStaticObject city = getRandomActiveCity();
				if (city != null) {
					out.add(new GameAction(buildEvent, owner, city));
					return out;
				} else {
					this.unitQueue.add(data.id);
					return out;
				}
			}
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		out.addAll(SeizeResource());
		return out;
	}

	private List<GameAction> FortifyLocation(WalkableTile location) { //THIS COULD BE PROBLEM - JUST REALLY SLOW
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("FortifyLocation -> ");
		
		ArrayList<GameAction> out = new ArrayList<GameAction>();
		//&& !((GameStaticObject)location).data.id.equals("FORTIFICATION")
		if (location instanceof GameStaticObject) {
			List<GameUnit> units_on_tile = map.getPlayerUnitsOnTile(location, owner);
			for (GameUnit u : units_on_tile){
				out.add(new GameAction(GameAction.OneAgentEvent.ACTION_FORTIFY, owner, u));
			}
			
			GameUnit closest = null;
			int closestDist = Integer.MAX_VALUE;
			for(GameUnit unit: map.getPlayerUnitsOfType(owner, "AIR", "ANTIAIR", "TANK")) {
				int dist = map.getDistBetween(unit, location);
				if(dist < closestDist) {
					closestDist = dist;
					closest = unit;
				}
			}
			
			if(closest != null)
				out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner, closest, location));
		} 
		else {
			ArrayList<GameUnit> ourWorkers = map.getPlayerUnitsOfType(owner, "WORKER");
			ArrayList<GameUnit> freeworkers = new ArrayList<GameUnit>();
			for (GameUnit u : ourWorkers) {
				if (u.mapX == location.mapX && u.mapY == location.mapY) {
					out.add(new GameAction(GameAction.OneAgentEvent.BUILD_FORTIFICATION, owner, u));
					return out;
				}
				if (!isUnitUsed(u)) {
					freeworkers.add(u);
				}
			}
			
			if ((freeworkers.size() > 0)) {
				GameUnit closest = null;
				int closestDist = Integer.MAX_VALUE;
				for(GameUnit unit: freeworkers) {
					if(Math.abs(unit.mapX - location.mapX) < 10 && Math.abs(unit.mapY - location.mapY) < 10) {
						int dist = map.getDistBetween(unit, location);
						if(dist < closestDist) {
							closestDist = dist;
							closest = unit;
						}
					}
				}
				
				if(closest != null)
					out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_ATTACK_MOVE, owner, closest, location));
				return out;
				
			}
			out.addAll(this.makeWorker());
		}
		return out;
	}

	private List<GameAction> FortifyStrategicLocation() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("FortifyStrategicLocation -> ");
		
		ArrayList<GameAction> out = new ArrayList<GameAction>();

		WalkableTile worst_stratloc = null;
		int worst_stratloc_score = Integer.MAX_VALUE;
		for (WalkableTile stratloc : map.getStratLocValues().keySet()) {
			int defending_units = map.getPlayerUnitsOnTile(stratloc, owner)
					.size();
			if (defending_units > 0 && defending_units < worst_stratloc_score) {
				worst_stratloc_score = defending_units;
				worst_stratloc = stratloc;
			}
		}
		if(worst_stratloc != null) out.addAll(FortifyLocation(worst_stratloc));
		
		return out;

	}

	private List<GameAction> FortifyResource() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("FortifyResource -> ");
		
		List<GameAction> out = new ArrayList<GameAction>();
		ArrayList<WalkableTile> locations = new ArrayList<WalkableTile>();
		List<GameStaticObject> resources = map.getPlayerObjectsOfType(owner,
				"MINE", "POWERPLANT");
		if (resources.size() == 0) {
			if (owner.scoreEconomy == 0) {
				out.addAll(this.SeizeResource());
				return out;
			}
			out.addAll(this.BuildPowerplant());
			return out;
		}
		for (GameStaticObject resource : resources) {
			if (map.getUnitsOnTile(resource).size() == 0)
				locations.add(resource);
		}

		if (locations.size() > 0) {
			return FortifyLocation(locations.get(rnd.nextInt(locations.size())));
		}
		if (rnd.nextBoolean()) {
			out.addAll(this.SeizeResource());
			return out;
		}
		out.addAll(this.BuildPowerplant());
		return out;
	}

	private List<GameAction> FortifyCity() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("FortifyCity -> ");
		
		List<GameStaticObject> cities = map.getPlayerObjectsOfType(owner,
				"CITY");
		GameStaticObject worst_city = null;
		int worst_city_score = Integer.MAX_VALUE;
		int city_sum = 0;
		for (GameStaticObject city : cities) {
			for (GameUnit u : map.getPlayerUnitsOnTile(city, owner)) {
				city_sum += u.data.metalCost;
			}
			if (city_sum < worst_city_score) {
				worst_city_score = city_sum;
				worst_city = city;
			}
		}
		
		if(worst_city != null)
			return FortifyLocation(worst_city);
		else
			return new ArrayList<GameAction>();
	}

	private List<GameAction> ClearUnitQueue() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("ClearUnitQueue -> ");
		
		unitQueue.clear();
		if (default_behavior_code != CLEAR_UNIT_QUEUE) {
			try {
				return parseActionCode(default_behavior_code);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new ArrayList<GameAction>();
	}

	private List<GameAction> CombatWinChanceTolerancePlus10() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("CombatPercPlus10 -> ");
		
		if (this.current_combat_pcent_win_chance * 1.1 < .95) this.current_combat_pcent_win_chance *= 1.1;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_PLUS_10) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> CombatWinChanceToleranceMinus10() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("CombatPercMinus10 -> ");
		
		if (this.current_combat_pcent_win_chance * .9 > .05) this.current_combat_pcent_win_chance *= .9;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_MINUS_10) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> CombatWinChanceToleranceResetToDefault() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("CombatPercReset -> ");
		
		this.current_combat_pcent_win_chance = this.init_combat_pcent_win_chance;
		if (this.default_behavior_code != this.COMBAT_WIN_CHANCE_TOLERANCE_RESET_TO_DEFAULT) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdMinus20() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("TurnsThreshMinus20 -> ");
		
		if (this.current_turns_threshold * .8 > 10) this.current_turns_threshold *= .8;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_MINUS_20) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdPlus20() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("TurnsThreshPlus20 -> ");
		
		if (this.current_turns_threshold * 1.2 < 620) this.current_turns_threshold *= 1.2;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_PLUS_20) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> TurnsLeftThresholdResetToDefault() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("TurnsThreshReset -> ");
		
		this.current_turns_threshold = this.init_turns_threshold;
		if (this.default_behavior_code != this.TURNS_LEFT_THRESHOLD_RESET_TO_DEFAULT) {
			return parseActionCode(this.default_behavior_code);
		}
		return new ArrayList<GameAction>();
	}

	private List<GameAction> CreateNewCity() {
		if(Team2Civ.DEBUG_OUTPUT) System.out.print("CreateNewCity -> ");
		
		try {
			if (!owner.canAfford(res.getStaticObject("CITY"))) {
				return this.SeizeResource();
			}
		} catch (ResNotFoundException e) {
			e.printStackTrace();
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
		
		WalkableTile current_candidate = getBestCityTile();
		if(current_candidate == null) return null;
		
		List<GameAction> out = new ArrayList<GameAction>();
		GameUnit closest_worker = null;
		int closest_worker_distance = Integer.MAX_VALUE;
		for (GameUnit u : freeworkers) {
			int u_dist = map.getDistBetween(u, current_candidate, owner);
			if (u_dist != -1 && u_dist < closest_worker_distance) {
				closest_worker = u;
				closest_worker_distance = u_dist;
			}
			if (closest_worker_distance == 0) {
				out.add(new GameAction(GameAction.OneAgentEvent.BUILD_CITY,
						owner, closest_worker));
				return out;
			}
		}
		out.add(new GameAction(GameAction.TwoAgentEvent.ACTION_MOVE, owner,
				closest_worker, current_candidate));
		return out;
	}

	private WalkableTile getBestCityTile() {
		WalkableTile best = null;
		int bestScore = Integer.MIN_VALUE;
		
		for(WalkableTile wt: map.getWalkableMap()) {
			if(!(wt instanceof GameStaticObject)) {
				int dist = map.getDistToClosestCity(wt, owner);
				if(dist > 2 && dist < 20) {
					int score = map.getTileResourceScore(wt, owner);
					if(score > bestScore) {
						bestScore = score;
						best = wt;
					}
				}
			}
		}

		return best;
	}

	private List<GameAction> parseActionCode(byte b) {
		List<GameAction> rtn = new ArrayList<GameAction>();

		if (b == SEIZE_STRATEGIC_LOCATION) {
			rtn.addAll(this.SeizeStrategicLocation(null));
		} else if (b == SEIZE_RESOURCE) {
			rtn.addAll(this.SeizeResource());
		} else if (b == ATTACK_PLAYER_1) {
			rtn.addAll(this.attackPlayer(owner.getPlayer(1, game)));
		} else if (b == ATTACK_PLAYER_2) {
			rtn.addAll(this.attackPlayer(owner.getPlayer(2, game)));
		} else if (b == ATTACK_PLAYER_3) {
			rtn.addAll(this.attackPlayer(owner.getPlayer(3, game)));
		} else if (b == ATTACK_STRONGEST_MILITARY) {
			rtn.addAll(this.AttackStrongestMilitary());
		} else if (b == ATTACK_STRONGEST_ECONOMY) {
			rtn.addAll(this.AttackStrongestEconomy());
		} else if (b == ATTACK_WEAKEST_MILITARY) {
			rtn.addAll(this.AttackWeakestMilitary());
		} else if (b == ATTACK_WEAKEST_ECONOMY) {
			rtn.addAll(this.AttackWeakestEconomy());
		} else if (b == HARASS_PLAYER_1) {
			rtn.addAll(this.harassPlayer(owner.getPlayer(1, game)));
		} else if (b == HARASS_PLAYER_2) {
			rtn.addAll(this.harassPlayer(owner.getPlayer(2, game)));
		} else if (b == HARASS_PLAYER_3) {
			rtn.addAll(this.harassPlayer(owner.getPlayer(3, game)));
		} else if (b == HARASS_STRONGEST_MILITARY) {
			rtn.addAll(this.harassStrongestMilitary());
		} else if (b == HARASS_STRONGEST_ECONOMY) {
			rtn.addAll(this.HarassStrongestEconomy());
		} else if (b == HARASS_WEAKEST_MILITARY) {
			rtn.addAll(this.HarassWeakestMilitary());
		} else if (b == HARASS_WEAKEST_ECONOMY) {
			rtn.addAll(this.harassWeakestEconomy());
		} else if (b == MAKE_WORKER) {
			rtn.addAll(this.makeWorker());
		} else if (b == MAKE_TANK) {
			rtn.addAll(this.makeTank());
		} else if (b == MAKE_AIR) {
			rtn.addAll(this.makeAir());
		} else if (b == MAKE_ANIAIR) {
			rtn.addAll(this.makeAntiAir());
		} else if (b == FORTIFY_STRATEGIC_LOCATION) {
			rtn.addAll(this.FortifyStrategicLocation());
		} else if (b == FORTIFY_RESOURCE) {
			rtn.addAll(this.FortifyResource());
		} else if (b == FORTIFY_CITY) {
			rtn.addAll(this.FortifyCity());
		} else if (b == CLEAR_UNIT_QUEUE) {
			rtn.addAll(this.ClearUnitQueue());
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
			rtn.addAll(this.CreateNewCity());			
		} else if (b != NO_ACTION) {
			Exception e = new Exception("No action found for byte: " + b);
			e.printStackTrace();
		}

		return rtn;
	}

	private List<GameUnit> getUnitsToSend(List<GameUnit> chases) {
		List<GameUnit> freeUnits = getFreeUnits();

		if (freeUnits.size() < 1)
			return null;

		if (chanceToBeKilled(freeUnits, chases) > current_combat_pcent_win_chance) {
			GameUnit removed = freeUnits.get(freeUnits.size() - 1);
			freeUnits.remove(removed);
			while (freeUnits.size() > 0
					&& chanceToBeKilled(freeUnits, chases) > current_combat_pcent_win_chance) {
				removed = freeUnits.get(freeUnits.size() - 1);
				freeUnits.remove(removed);
			}
			freeUnits.add(removed);
			return freeUnits;
		} else {
			return null;
		}
	}

	private boolean[] getAIPosition(List<Player> p) {
		for (int i = 0; i < p.size(); i++) {
			if (p.get(i) == owner)
				return AI.intTo2Bits(i);
		}
		return null;
	}

	private boolean isMetalOverThresh() {
		return (owner.metal > init_resource_threshold);
	}

	private boolean isTurnOverThresh() {
		return (game.turnsLeft > current_turns_threshold);
	}

	private boolean isUnitsQueuedOverThresh() {
		return unitQueue.size() > init_queue_length;
	}

	private boolean[] majorityUnit() {
		int tankCount = 0;
		int airCount = 0;
		int antiAirCount = 0;
		boolean[] antiAir = { true, true };
		boolean[] air = { true, false };
		boolean[] tank = { false, false };
		for (GameUnit u : map.getUnits()) {
			if (u.data.id.equals("TANK")) {
				tankCount++;
			} else if (u.data.id.equals("AIR")) {
				airCount++;
			} else if (u.data.id.equals("ANTIAIR")) {
				antiAirCount++;
			}
		}
		if (tankCount > airCount && tankCount > antiAirCount) {
			return tank;
		} else if (antiAirCount > airCount && antiAirCount > tankCount) {
			return antiAir;
		}
		return air;
	}

	public void addUnitToQueue(String id) {
		unitQueue.add(id);
	}
}