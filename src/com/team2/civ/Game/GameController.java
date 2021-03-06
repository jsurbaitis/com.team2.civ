package com.team2.civ.Game;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
import com.team2.civ.AI.AI;
import com.team2.civ.AI.AIGameResult;
import com.team2.civ.Data.GameStaticObjectData;
import com.team2.civ.Data.GameUnitData;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.MapObject;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.UI.UI;
import com.team2.civ.UI.UIEvent;

public class GameController {

	private long gameTime = 0;

	private int pressStartX;
	private int pressStartY;

	private int lastMouseX;
	private int lastMouseY;

	private boolean leftClick = true;

	private UI ui;
	private Resources res;

	private GameMap map;
	private GameGraphics graphics;

	private Iterator<Player> playersIt;
	private List<Player> players = new ArrayList<Player>();
	private List<Player> lostPlayers = new ArrayList<Player>();
	private List<Player> militaryRankings = new ArrayList<Player>();
	private List<Player> economyRankings = new ArrayList<Player>();
	private List<Player> stratLocRankings = new ArrayList<Player>();
	public Player humanPlayer;
	private Player currentPlayer;
	private MapObject target;

	private List<ActionToShow> toShow = new ArrayList<ActionToShow>();
	private List<GameAction> toPerform = new ArrayList<GameAction>();
	private int actionTimer;
	private ActionToShow currentShowing;

	private Vector<GameUnit> combatTargets = new Vector<GameUnit>();

	private int turnsLeft = 630;

	public GameController() {
		this.res = Resources.getInstance();
	}

	public void runGame() {
		try {
			initGame();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		if(!Team2Civ.NO_HUMAN_PLAYER)
			humanPlayer = players.get(0);
		
		File folder = new File("genomes/");
		String[] genomes = folder.list();
		
		Random rnd = new Random();
		int rndGenome;
		
		for(Player p: players) {
			if(genomes.length > 2) {
				rndGenome = rnd.nextInt(genomes.length);
				p.ai = new AI(new File("genomes/genome_"+rndGenome));
				System.out.println("USING GENOME: "+"genome_"+rndGenome);
			} else {
				p.ai = new AI();
			}
			
			p.ai.setGameVars(this, map, p);
		}

		if(!Team2Civ.AI_MODE)
			ui = new UI(players.get(0), map, graphics, this);
		
		if(humanPlayer == null)
			endTurnNormal();
	}

	public AIGameResult runGame(AI a1, AI a2, AI a3, AI a4) {
		try {
			initGame();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		humanPlayer = null;
		players.get(0).ai = a1;
		players.get(1).ai = a2;
		players.get(2).ai = a3;
		players.get(3).ai = a4;

		for (Player p : players)
			p.ai.setGameVars(this, map, p);

		return endTurnAIMode();
	}

	private void initGame() throws ResNotFoundException {
		map = new GameMap();
		
		Player.PlayerColor[] colors = { Player.PlayerColor.RED, Player.PlayerColor.GREEN, 
				Player.PlayerColor.BLUE, Player.PlayerColor.PINK };
		int playerIndex = 1;

		for (GameStaticObject city : map.getAllCities()) {
			Player p = new Player("Player " + playerIndex,
					colors[playerIndex - 1], null);
			players.add(p);
			city.owner = p;
			city.updateImage("CITY");
			city.updateFowImage("CITY");
			p.powerCapability += city.data.powerGiven;

			if (!Team2Civ.AI_MODE && playerIndex == 1) {
				graphics = new GameGraphics(map);
				graphics.centerOn(city);
			}

			playerIndex++;
		}
		
		for(Player p: players) {
			economyRankings.add(p);
			militaryRankings.add(p);
			stratLocRankings.add(p);
		}

		currentPlayer = players.get(0);
	}

	public void update(long timeElapsedMillis) {
		gameTime++;

		ui.update(gameTime);
		graphics.update(gameTime);

		if(toPerform.size() > 0) {
			updateActionShowing();
		}

		List<GameUnit> units = map.getUnits();
		for (int i = 0; i < units.size(); i++) {
			GameUnit u = units.get(i);
			u.update(gameTime);
		}

		map.updateFow(humanPlayer);
	}
	
	private void updateActionShowing() {
		if(toShow.size() > 0) {
			if(currentShowing == null)
				currentShowing = toShow.get(0);

			graphics.centerOn(currentShowing.target);
			
			if (currentShowing.movement) {
				GameUnit u = (GameUnit) currentShowing.target;
				if (!u.isMoving()) {
					checkForAttack(u);	
					toShow.remove(0);
					currentShowing = null;
				}
			} else {
				if (gameTime % 5 == 0)
					actionTimer++;
				if (actionTimer > 6) {
					toShow.remove(0);
					currentShowing = null;
					actionTimer = 0;
				}
			}
		} else {
			toPerform.remove(0);
			
			if(toPerform.size() > 0) {
				performAction(toPerform.get(0));
			} else {
				endTurnNormal();
			}
		}
	}

	public void draw(Graphics2D g) {
		graphics.draw(g);
		ui.draw(g);
	}

	public void performAction(GameAction action) {
		if (action.actor != null && action.actor.owner != action.performer)
			return;

		if (action.event == GameAction.Event.ACTION_ATTACK) {
			GameUnit unit = (GameUnit) action.actor;
			if (unit.owner != action.target.owner && unit.inCombatRange((GameUnit) action.target)) {
				performCombat((GameUnit) action.actor, (GameUnit) action.target);
			}
		} else if (action.event == GameAction.Event.CONTINUE_MOVE) {
			endCombatTargeting();
			GameUnit u = (GameUnit) action.actor;
			u.startMovement();
			showAction(new ActionToShow(u, true));
		} else if (action.event == GameAction.Event.ACTION_CHECK_ATTACK) {
			checkForAttack((GameUnit) action.actor);
		} else if (action.event == GameAction.Event.ACTION_DESTROY_SELF) {
			destroyUnit((GameUnit) action.actor);
		} else if (action.event == GameAction.Event.ACTION_FORTIFY) {
			fortifyUnit((GameUnit) action.actor);
		} else if (action.event == GameAction.Event.ACTION_MOVE) {
			startMovement((GameUnit) action.actor, action.target);
		} else if (action.event == GameAction.Event.ACTION_ATTACK_MOVE) {
			startAttackMove((GameUnit) action.actor, action.target);
		} else if (action.event == GameAction.Event.ACTION_DESTROY_TARGET) {
			GameStaticObject target = (GameStaticObject) action.target;
			if (target.data.destructible && action.actor.mapX == target.mapX
					&& action.actor.mapY == target.mapY)
				destroyObject(target, action.performer);
		} else if (action.event.toString().startsWith("BUILD")) {
			try {
				String obj = action.event.toString().replace("BUILD_", "");
				addObjectToPlayer(action.performer, action.actor, obj);
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void destroyObject(GameStaticObject target, Player performer) {
		showAction(new ActionToShow(target, false));
		
		if(!Team2Civ.AI_MODE)
			graphics.removeLowImage(target.getImage());
		map.removeStaticObj(target);
		performer.metal += target.data.metalCost * 0.2;
		target.owner.powerCapability -= target.data.powerGiven;
	}

	private void captureObject(GameStaticObject target, Player performer) {
		target.owner.powerCapability -= target.data.powerGiven;
		target.owner = performer;
		target.owner.powerCapability += target.data.powerGiven;
		
		target.updateImage(target.data.id);
		target.updateFowImage(target.data.id);

	}

	private void startMovement(GameUnit actor, MapObject target) {
		if(actor.AP < 1) return;
		
		endCombatTargeting();

		List<WalkableTile> path = map.findPath(actor, target, actor.owner, null,
				true, false, actor.AP);

		if (path != null && path.size() > 0) {
			actor.AP -= path.size();
			
			if (!Team2Civ.AI_MODE) {
				actor.startMovement(path);
				showAction(new ActionToShow(actor, true));
			} else {
				actor.setPos(path.get(0).mapX, path.get(0).mapY);
				checkForAttack(actor);
			}
		}
	}
	
	private void startAttackMove(GameUnit actor, MapObject target) {
		if(actor.AP < 1) return;
		
		endCombatTargeting();
		List<WalkableTile> path = map.findPath(actor, target, actor.owner, null,
				true, true, actor.AP);

		if (path != null && path.size() > 0) {
			int offset = 0;
			for(int i = path.size() - 1; i >= 0; i--) {
				if(!map.isTileFree(path.get(i), actor.owner)) {
					offset = i + 1;
				}
			}
			
			int start = Math.max(offset, path.size() - actor.AP);
			List<WalkableTile> toWalk = path.subList((start < 0) ? 0 : start, path.size());

			if(toWalk.size() > 0) {
				actor.AP -= toWalk.size();
				
				if (!Team2Civ.AI_MODE) {
					actor.startMovement(toWalk);
					showAction(new ActionToShow(actor, true));
				} else {
					actor.setPos(toWalk.get(0).mapX, toWalk.get(0).mapY);
					checkForAttack(actor);
				}
			}
		}
	}
	
	private void checkForAttack(GameUnit unit) {
		if(unit.AP < 1) return;
		
		GameStaticObject bestTarget = null;
		for(GameStaticObject so: map.getFreeEnemyObjectsAround(unit)) {
			if(so.data.id.equals("CITY")) {
				bestTarget = so;
			}
			if(bestTarget == null) {
				bestTarget = so;
			}
		}
		
		if(bestTarget != null) {
			startMovement(unit, bestTarget);
			unit.AP = 0;
			return;
		}
		
		for(GameUnit u: map.getUnits()) {
			if(u.owner != unit.owner && unit.inCombatRange(u)) {
				performCombat(unit, u);
				break;
			}
		}
	}

	private void performCombat(GameUnit attacker, GameUnit target) {
		if(attacker.AP < 1) return;
		
		int dmg = calcCombatDmg(attacker, target);
		target.takeDmg(dmg);
		if (target.getHP() <= 0) {
			target.owner.updateLost(target);
			unitDead(target);
		}
		target.owner.updateAttacked(attacker.owner, this);
		attacker.AP = 0;
		showAction(new ActionToShow(attacker, false));
		
		if(!Team2Civ.AI_MODE) {
			graphics.addCombatText(new CombatText(target.mapX, target.mapY, ""+dmg));
			//graphics.addParticle(new Particle(attacker.mapX, attacker.mapY, target));
		}
	}

	public int calcCombatDmg(GameUnit attacker, GameUnit target) {
		GameStaticObject so = map.getStaticObj(target);
		double dmg = attacker.getDmgToDeal(target.data.id);
		if (so != null)
			dmg *= (1 - so.data.defensiveBonus / 100.0);

		Random rnd = new Random();
		double modif = (80 + rnd.nextInt(40)) / 100.0;
		dmg *= modif;

		return (int) Math.round(dmg);
	}

	private void startCombatTargeting() {
		GameUnit actor = (GameUnit) target;
		for (GameUnit u : map.getUnits()) {
			if (u.owner != humanPlayer) {
				if (actor.inCombatRange(u)) {
					combatTargets.add(u);
					u.selected = true;
				}
			}
		}
	}

	private void endCombatTargeting() {
		for (GameUnit u : combatTargets)
			u.selected = false;

		combatTargets.clear();
	}

	private void destroyUnit(GameUnit u) {
		showAction(new ActionToShow(u, false));
		
		u.owner.metal += u.data.metalCost * 0.25;
		u.owner.powerUsage -= u.data.powerUsage;

		if (!Team2Civ.AI_MODE)
			graphics.removeUnitImage(u.getImage());
		map.removeUnit(u);
	}
	
	private void unitDead(GameUnit u) {
		showAction(new ActionToShow(u, false));

		u.owner.powerUsage -= u.data.powerUsage;

		if (!Team2Civ.AI_MODE)
			graphics.removeUnitImage(u.getImage());
		map.removeUnit(u);
	}

	private void fortifyUnit(GameUnit u) {
		showAction(new ActionToShow(u, false));
		u.fortify();
	}

	private void detarget() {
		if (target != null)
			target.selected = false;
		target = null;
	}

	private void endTurnNormal() {
		detarget();
		
		if(!lostPlayers.contains(currentPlayer)) {
			map.updatePathsAndStratLoc();
			upkeep(currentPlayer);
			globalUpkeep();
			checkPlayersForCityLose();
			
			Player winner = checkForWinner();
			if(winner != null) {
				if(winner.ai != null)
					System.out.println("AI "+winner.colour.toString()+" won!");
				else
					System.out.println("Human player "+winner.colour.toString()+" won!");
				return;
			}
		}

		if(playersIt == null || !playersIt.hasNext())  {
			turnsLeft--;
			if(turnsLeft == 0) {
				System.out.println("No more turns left!");
				return;
			}
			playersIt = players.iterator();
		}
			
		currentPlayer = playersIt.next();

		if(lostPlayers.contains(currentPlayer)) {
			endTurnNormal();
		} else if (currentPlayer != humanPlayer) {
			toPerform.addAll(currentPlayer.ai.perform());
			if(toPerform.size() > 0) {
				performAction(toPerform.get(0));
			}
			
			currentPlayer.resetConditions();
		} else {
			graphics.resetOffsets();
		}
	}

	private AIGameResult endTurnAIMode() {
		if(!lostPlayers.contains(currentPlayer)) {
			map.updatePathsAndStratLoc();
			upkeep(currentPlayer);
			globalUpkeep();
			checkPlayersForCityLose();
			
			AIGameResult winner = checkForAIWinner();
			if (winner != null)
				return winner;
		}

		if(playersIt == null || !playersIt.hasNext())  {
			turnsLeft--;

			if(turnsLeft == 0) {
				return getBestResult();
			}
			playersIt = players.iterator();
		}
			
		currentPlayer = playersIt.next();
		
		if(!lostPlayers.contains(currentPlayer)) {
			List<GameAction> actions = currentPlayer.ai.perform();
		
			currentPlayer.resetConditions();
			performActions(actions);
		}
			
		return endTurnAIMode();
	}
	
	private AIGameResult getBestResult() {
		Player best = null;
		float bestScore = Integer.MIN_VALUE;
		float totalScore = 0;
		for(Player p: players) {
			int score = map.getPlayerCities(p).size();
			totalScore += score;
			if(score > bestScore) {
				best = p;
				bestScore = score;
			}
		}

		return new AIGameResult(best.ai, bestScore / totalScore / 5);
	}
	
	private AIGameResult checkForAIWinner() {
		if(lostPlayers.size() == 3) {
			for(Player p: players) {
				if(!lostPlayers.contains(p)) {
					if(map.getPlayerCities(p).size() > 1)
						return new AIGameResult(p.ai);
					else
						return getBestResult();
				}
			}
		}

		List<GameStaticObject> cities = map.getAllCities();
		Player p = cities.get(0).owner;
		for (GameStaticObject c : cities) {
			if (c.owner != p) {
				return null;
			}
		}

		return new AIGameResult(p.ai);
	}

	private Player checkForWinner() {
		if(lostPlayers.size() == 3) {
			for(Player p: players) {
				if(!lostPlayers.contains(p)) {
					if(map.getPlayerCities(p).size() > 1)
						return p;
				}
			}
		}

		List<GameStaticObject> cities = map.getAllCities();
		Player p = cities.get(0).owner;
		for (GameStaticObject c : cities) {
			if (c.owner != p) {
				return null;
			}
		}
		return p;
	}

	private void performActions(List<GameAction> actions) {
		for (GameAction ga : actions) {
			performAction(ga);
		}
	}

	public int getMineIncome(GameStaticObject mine) {
		return (int) (50 / Math.pow((map.getDistToClosestCity(mine, mine.owner)), 2));
	}
	
	public int getPlayerMineIncome(Player p) {
		int sum = 0;
		for(GameStaticObject so: map.getPlayerObjectsOfType(p, "MINE")) {
			sum += getMineIncome(so);
		}
		return sum;
	}

	private void upkeep(Player p) {
		/*if(p == humanPlayer) {
			for (GameUnit u : map.getPlayerUnits(p)) {
				if(u.AP > 0 && u.movementNotFinished())
					toPerform.add(new GameAction(GameAction.OneAgentEvent.CONTINUE_MOVE, p, u));
			}
		}*/
		
		for (GameUnit u : map.getPlayerUnits(p)) {
			u.AP = u.data.AP;
			
			GameStaticObject so = map.getStaticObj(u);
			if (so != null && so.owner != p) {
				if (so.data.capturable) {
					so.owner.updateLost(so);
					captureObject(so, p);
					
					if (!Team2Civ.AI_MODE) {
						graphics.addCombatText(new CombatText(so.mapX, so.mapY, "Captured!"));
					}					
				} else if (so.data.destructible) {
					so.owner.updateLost(so);
					so.owner.powerCapability -= so.data.powerGiven;
					
					if (!Team2Civ.AI_MODE) {
						graphics.removeLowImage(so.getImage());
						graphics.addCombatText(new CombatText(so.mapX, so.mapY, "Destroyed!"));
					}
					map.removeStaticObj(so);
				}
			}
		}

		// p.powerCapability = 0;
		int totalIncome = 0;
		for (GameStaticObject so : map.getPlayerObjects(p)) {
			so.active = true;
			// p.powerCapability += so.data.powerGiven;

			if (so.data.id.equals("MINE")) {
				totalIncome += getMineIncome(so);
			}
		}
		p.metal += totalIncome;
		
		if(!lostPlayers.contains(p) && totalIncome == 0 && p.metal < 50) { //TODO: ahh hardcoded worker cost
			lostPlayers.add(p);
			if(Team2Civ.DEBUG_OUTPUT) System.out.println("AI lost!");
		}
	}
	
	private void checkPlayersForCityLose() {
		for(Player p: players) {
			if(!lostPlayers.contains(p) && map.getPlayerCities(p).size() == 0) {
				lostPlayers.add(p);
				if(Team2Civ.DEBUG_OUTPUT) System.out.println("AI lost!");
			}
		}
	}
	
	private void globalUpkeep() {
		for(Player p: players) {
			p.scoreEconomy = 0;
			for (GameStaticObject u : map.getPlayerObjectsOfType(p, "MINE"))
				p.scoreEconomy += 50 / map.getDistToClosestCity(u, p);
			
			p.scoreMilitary = 0;
			for (GameUnit u : map.getPlayerUnits(p)) {
				p.scoreMilitary += u.data.metalCost;
			}
			
			p.scoreStratLoc = map.getPlayerStratLocScore(p);
		}
		
		Collections.sort(militaryRankings, Player.militaryComparator);
		Collections.sort(economyRankings, Player.economyComparator);
		Collections.sort(stratLocRankings, Player.stratLocComparator);
	}

	private void addUnitToPlayer(Player p, GameStaticObject city,
			GameUnitData data) throws ResNotFoundException {
		p.metal -= data.metalCost;
		p.powerUsage += data.powerUsage;

		GameUnit u = new GameUnit(city.mapX, city.mapY, p, data);

		if (!Team2Civ.AI_MODE)
			graphics.addUnitImage(u.getImage());
		map.addUnit(u);
	}

	private void addStaticObjToPlayer(Player p, GameUnit unit,
			GameStaticObjectData data) throws ResNotFoundException {
		p.metal -= data.metalCost;
		p.powerCapability += data.powerGiven;

		GameStaticObject so = new GameStaticObject(unit.mapX, unit.mapY, p, data);
		map.addStaticObj(so);
		
		p.updateBuilt(so);

		if (!Team2Civ.AI_MODE)
			graphics.addLowImage(so.getImage());

		unit.owner.powerUsage -= unit.data.powerUsage;

		if (!Team2Civ.AI_MODE)
			graphics.removeUnitImage(unit.getImage());
		map.removeUnit(unit);
	}

	private void addObjectToPlayer(Player p, CoordObject location,
			String objId) throws ResNotFoundException {

		if (isStaticData(objId)) {
			GameUnit unit = (GameUnit) location;
			GameStaticObjectData data = res.getStaticObject(objId);
			if (unit.data.buildIDs.contains(objId) && p.canAfford(data)) {
				if (!objId.equals("MINE") && map.getStaticObj(unit) == null) {
					showAction(new ActionToShow(unit, false));
					addStaticObjToPlayer(p, unit, data);
				} else if (objId.equals("MINE")) {
					GameStaticObject so = map.getStaticObj(unit);
					if (so != null && so.data.id.equals("METAL")) {
						showAction(new ActionToShow(unit, false));
						addStaticObjToPlayer(p, unit, data);
					}
				}
			}
		} else if (isUnitData(objId)) {
			GameStaticObject so = (GameStaticObject) location;
			GameUnitData data = res.getUnit(objId);
			if (so.active && so.data.buildIDs.contains(objId)
					&& p.canAfford(data)) {
				so.active = false;
				showAction(new ActionToShow(so, false));
				addUnitToPlayer(p, so, data);
			} else if(p.ai != null) {
				p.ai.addUnitToQueue(objId);
			}
		}
	}

	private boolean isStaticData(String objId) {
		try {
			res.getStaticObject(objId);
			return true;
		} catch (ResNotFoundException e) {
			return false;
		}
	}

	private boolean isUnitData(String objId) {
		try {
			res.getUnit(objId);
			return true;
		} catch (ResNotFoundException e) {
			return false;
		}
	}

	public void performEvent(UIEvent event) {
		if (currentPlayer != humanPlayer || event.e == UIEvent.Event.HANDLED)
			return;

		if (event.e == UIEvent.Event.ACTION_ATTACK)
			startCombatTargeting();
		else if (event.e == UIEvent.Event.ACTION_DESTROY) {
			destroyUnit((GameUnit) target);
			detarget();
		} else if (event.e == UIEvent.Event.ACTION_FORTIFY)
			fortifyUnit((GameUnit) target);
		else if (event.e == UIEvent.Event.END_TURN) {
			graphics.saveOffsets();
			endTurnNormal();
		} else if (event.e.toString().startsWith("BUILD"))
			try {
				addObjectToPlayer(currentPlayer, target, event.e.toString()
						.replace("BUILD_", ""));
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		else if (event.e == UIEvent.Event.TARGET_CHANGED) {
			if (target != null)
				target.selected = false;
			target = event.actor;
			target.selected = true;
		}
	}

	public void onMouseInput(MouseEvent ev) {
		if (currentPlayer != humanPlayer)
			return;

		UIEvent event = ui.onMouseInput(ev);
		if (event == null)
			onGameInput(ev);
		else
			performEvent(event);
	}

	public void onGameInput(MouseEvent ev) {
		if (ev.getID() == MouseEvent.MOUSE_PRESSED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();

			pressStartX = lastMouseX;
			pressStartY = lastMouseY;

			if (SwingUtilities.isLeftMouseButton(ev))
				leftClick = true;
			else
				leftClick = false;
		} else if (ev.getID() == MouseEvent.MOUSE_DRAGGED) {
			int dx = ev.getX() - lastMouseX;
			int dy = ev.getY() - lastMouseY;
			graphics.addToOffsets(dx, dy);

			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick
				&& target != null && target instanceof GameUnit) {
			GameUnit movingUnit = (GameUnit) target;
			if (movingUnit.owner == currentPlayer
					&& Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				for (WalkableTile t : map.getWalkableMap()) {
					if (t.picked(graphics.mouseToMapX(ev.getX()),
							graphics.mouseToMapY(ev.getY()))) {

						startMovement(movingUnit, t);
						endCombatTargeting();
					}
				}
			}
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {

				for (GameUnit u : combatTargets) {
					if (u.picked(graphics.mouseToMapX(ev.getX()),
							graphics.mouseToMapY(ev.getY()))) {
						performCombat((GameUnit) target, u);
						return;
					}
				}

				ArrayList<MapObject> targets = new ArrayList<MapObject>();

				for (GameUnit u : map.getPlayerUnits(humanPlayer)) {
					if (u.picked(graphics.mouseToMapX(ev.getX()),
							graphics.mouseToMapY(ev.getY()))) {

						endCombatTargeting();
						targets.add(u);
					}
				}

				for (GameStaticObject so : map.getPlayerObjects(humanPlayer)) {
					if (so.picked(graphics.mouseToMapX(ev.getX()),
							graphics.mouseToMapY(ev.getY()))) {

						endCombatTargeting();
						targets.add(so);
					}
				}

				if (targets.size() > 0) {
					ui.showInfo(targets);
					if (target != null)
						target.selected = false;
					target = null;
					if (targets.size() == 1) {
						target = targets.get(0);
						target.selected = true;
					}
				}
			}
		}
	}

	public void onMouseWheelInput(MouseWheelEvent ev) {
		if (currentPlayer != humanPlayer)
			return;

		graphics.zoomInput(ev.getWheelRotation());
	}

	public void onKeyboardInput(KeyEvent ev) {
		if (currentPlayer != humanPlayer)
			return;

		if (ev.getID() == KeyEvent.KEY_PRESSED) {
			if(ev.getKeyCode() == KeyEvent.VK_ENTER) {
				this.performEvent(new UIEvent(UIEvent.Event.END_TURN));
			}
		}
	}
	
	private void showAction(ActionToShow show) {
		if(!Team2Civ.AI_MODE && currentPlayer != humanPlayer) {
			if(map.getTileAt(show.target).beingSeen)
				toShow.add(show);
		}
	}
	
	private class ActionToShow {
		public boolean movement;
		public MapObject target;
		
		public ActionToShow(MapObject target, boolean movement) {
			this.target = target;
			this.movement = movement;
		}
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public List<Player> getMilitaryRankings() {
		return militaryRankings;
	}
	
	public List<Player> getEconomyRankings() {
		return economyRankings;
	}
	
	public List<Player> getStratLocRankings() {
		return stratLocRankings;
	}
	
	public int getTurnsLeft() {
		return turnsLeft;
	}
}
