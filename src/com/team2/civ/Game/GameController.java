package com.team2.civ.Game;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
import com.team2.civ.AI.AI;
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

	public List<Player> players = new ArrayList<Player>();
	public Player humanPlayer;
	private Player currentPlayer;
	private MapObject target;

	private List<GameAction> showingActionList;
	private int actionIndex;
	private int actionTimer;
	private GameAction currentAction;

	private Vector<GameUnit> combatTargets = new Vector<GameUnit>();

	public int turnsLeft = 630;

	public GameController() {
		this.res = Resources.getInstance();
	}

	public void runGame() {
		try {
			initGame();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		humanPlayer = players.get(0);

		for (int i = 1; i < players.size(); i++) {
			Player p = players.get(i);
			p.ai = new AI();
			p.ai.setGameVars(this, map, p);
		}

		ui = new UI(humanPlayer, map);
	}

	public AI runGame(AI a1, AI a2, AI a3, AI a4) {
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
		graphics = new GameGraphics(map);

		String[] colors = { "#FFFFFF", "#FF5400", "#545454", "#000FFF" };
		int playerIndex = 1;

		for (GameStaticObject city : map.getAllCities()) {
			Player p = new Player("Player " + playerIndex,
					colors[playerIndex - 1], null);
			players.add(p);
			city.owner = p;

			if (playerIndex == 1) {
				int offsetX = -city.x + Team2Civ.WINDOW_WIDTH / 2;
				int offsetY = -city.y + Team2Civ.WINDOW_HEIGHT / 2;
				graphics.setOffsets(offsetX, offsetY);
			}

			playerIndex++;
		}

		currentPlayer = players.get(0);
	}

	public void update(long timeElapsedMillis) {
		gameTime++;

		ui.update(gameTime);

		graphics.updateZoom();

		if (showingActionList != null && currentAction != null) {
			if (currentAction.actor != null) {
				int offsetX = -currentAction.actor.x + Team2Civ.WINDOW_WIDTH / 2;
				int offsetY = -currentAction.actor.y + Team2Civ.WINDOW_HEIGHT / 2;
				graphics.setOffsets(offsetX, offsetY);
			}

			if (currentAction.event == GameAction.Event.ACTION_MOVE) {
				GameUnit u = (GameUnit) currentAction.actor;
				if (!u.isMoving()) {
					actionIndex++;
					if (actionIndex >= showingActionList.size())
						showingActionList = null;
					else {
						currentAction = performAction(showingActionList
								.get(actionIndex));
						currentPlayer.previousTurn.add(currentAction);
					}
				}
			} else {
				if (gameTime % 5 == 0)
					actionTimer++;
				if (actionTimer > 12) {
					actionIndex++;
					if (actionIndex >= showingActionList.size())
						showingActionList = null;
					else {
						actionTimer = 0;
						currentAction = performAction(showingActionList
								.get(actionIndex));
						currentPlayer.previousTurn.add(currentAction);
					}
				}
			}
		}

		for (GameUnit u : map.getUnits()) {
			u.update(gameTime);
		}

		map.updateFow(humanPlayer);
	}

	public void draw(Graphics2D g) {
		graphics.draw(g);
		ui.draw(g);
	}

	public GameAction performAction(GameAction action) {
		if (action.actor != null && action.actor.owner != action.performer)
			return null;

		if (action.event == GameAction.Event.ACTION_ATTACK) {
			if (action.actor.owner != action.target.owner) {
				performCombat((GameUnit) action.actor, (GameUnit) action.target);
			} else {
				return null;
			}
		} else if (action.event == GameAction.Event.ACTION_DESTROY_SELF) {
			destroyUnit((GameUnit) action.actor);
		} else if (action.event == GameAction.Event.ACTION_FORTIFY) {
			fortifyUnit((GameUnit) action.actor);
		} else if (action.event == GameAction.Event.END_TURN) {
			endTurn();
		} else if (action.event == GameAction.Event.ACTION_MOVE) {
			startMovement((GameUnit) action.actor, action.target);
		} else if (action.event == GameAction.Event.ACTION_DESTROY_TARGET) {
			GameStaticObject target = (GameStaticObject) action.target;
			if (target.data.destructible && action.actor.mapX == target.mapX
					&& action.actor.mapY == target.mapY)
				destroyObject(target, action.performer);
		} else if (action.event.toString().startsWith("BUILD")) {
			try {
				String obj = action.event.toString().replace("BUILD_", "");
				if (!addObjectToPlayer(action.performer, action.actor, obj))
					return null;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		return action;
	}

	private void destroyObject(GameStaticObject target, Player performer) {
		graphics.removeLowImage(target.getImage());
		map.removeStaticObj(target);
		performer.metal += target.data.metalCost * 0.2;
		target.owner.powerCapability -= target.data.powerGiven;
	}

	private void captureObject(GameStaticObject target, Player performer) {
		target.owner.powerCapability -= target.data.powerGiven;
		target.owner = performer;
		target.owner.powerCapability += target.data.powerGiven;
	}

	private void startMovement(GameUnit actor, MapObject target) {
		endCombatTargeting();
		List<WalkableTile> path = map.findPath(actor, target, actor.owner, null,
				true, false, actor.AP + 1);

		if (path != null) {
			actor.AP -= path.size() - 1;

			if (!Team2Civ.AI_MODE)
				actor.startMovement(path);
			else {
				actor.setPos(path.get(0).mapX, path.get(0).mapY);
			}
		}
	}

	private void performCombat(GameUnit attacker, GameUnit target) {
		target.takeDmg(calcCombatDmg(attacker, target));
		attacker.AP = 0;
	}

	public int calcCombatDmg(GameUnit attacker, GameUnit target) {
		GameStaticObject so = map.getStaticObj(target);
		int dmg = attacker.getDmgToDeal(target.data.id);
		if (so != null)
			dmg *= (1 - so.data.defensiveBonus / 100);

		Random rnd = new Random();
		double modif = (80 + rnd.nextInt(40)) / 100;
		dmg *= modif;

		return dmg;
	}

	private void startCombatTargeting() {
		for (GameUnit u : map.getUnits()) {
			if (u.owner != humanPlayer) {
				if (Math.abs(target.mapX - u.mapX) <= u.data.range
						&& Math.abs(target.mapY - u.mapY) <= u.data.range) {
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

	public void destroyUnit(GameUnit u) {
		u.owner.metal += u.data.metalCost * 0.25;
		u.owner.powerUsage -= u.data.powerUsage;

		graphics.removeUnitImage(u.getImage());
		map.removeUnit(u);
	}

	private void fortifyUnit(GameUnit u) {
		u.fortify();
	}

	private void detarget() {
		if (target != null)
			target.selected = false;
		target = null;
	}

	private void endTurn() {
		if (!Team2Civ.AI_MODE)
			endTurnNormal();
	}

	private void endTurnNormal() {
		detarget();
		map.updatePathsAndStratLoc();
		upkeep(currentPlayer);
		turnsLeft--;

		Player winner = checkForWinner();
		// TODO: do something if winner != null

		int nextPlayer = (players.indexOf(currentPlayer) + 1) % players.size();
		currentPlayer = players.get(nextPlayer);

		if (currentPlayer != humanPlayer) {
			List<GameAction> actions = currentPlayer.ai
					.perform(getActionsForOthers(currentPlayer));

			displayActions(actions);
		}
	}

	private AI endTurnAIMode() {
		map.updatePathsAndStratLoc();
		upkeep(currentPlayer);
		turnsLeft--;

		Player winner = checkForWinner();
		if (winner != null)
			return winner.ai;

		int nextPlayer = (players.indexOf(currentPlayer) + 1) % players.size();
		currentPlayer = players.get(nextPlayer);

		List<GameAction> actions = currentPlayer.ai
				.perform(getActionsForOthers(currentPlayer));
		currentPlayer.previousTurn = performActions(actions);
		return endTurnAIMode();
	}

	private Player checkForWinner() {
		if (turnsLeft < 1)
			return players.get(0); // TODO: return by some statistic

		List<GameStaticObject> cities = map.getAllCities();
		Player p = cities.get(0).owner;
		for (GameStaticObject c : cities) {
			if (c.owner != p) {
				return null;
			}
		}
		return p;
	}

	private void displayActions(List<GameAction> actions) {
		showingActionList = actions;
		actionIndex = 0;
		actionTimer = 0;
		currentAction = performAction(actions.get(actionIndex));
		currentPlayer.previousTurn = new ArrayList<GameAction>();
		currentPlayer.previousTurn.add(currentAction);
	}

	private List<GameAction> performActions(List<GameAction> actions) {
		List<GameAction> rtn = new ArrayList<GameAction>();
		for (GameAction ga : actions) {
			rtn.add(performAction(ga));
		}
		return rtn;
	}

	private ArrayList<GameAction> getActionsForOthers(Player exclude) {
		ArrayList<GameAction> rtn = new ArrayList<GameAction>();
		for (Player p : players)
			if (p != exclude)
				rtn.addAll(p.previousTurn);

		return rtn;
	}

	private void upkeep(Player p) {
		for (GameUnit u : map.getUnits()) {
			if (u.owner == p) {
				GameStaticObject so = map.getStaticObj(u);
				if (so != null && so.owner != p) {
					if (so.data.capturable)
						so.owner = p;
					else if (so.data.destructible)
						map.removeStaticObj(so);
				}
			}
		}

		// p.powerCapability = 0;
		for (GameStaticObject so : map.getPlayerObjects(p)) {
			so.active = true;
			// p.powerCapability += so.data.powerGiven;

			if (so.data.id.equals("MINE")) {
				/*
				 * int smallestDist = Integer.MAX_VALUE; for (Path path : paths)
				 * { if (path.startObj.owner == p && path.endObj == so) {
				 * System.out.println("Path "+path.path.size()); if
				 * (path.path.size() < smallestDist) { smallestDist =
				 * path.path.size(); } } } p.metal += 50 /
				 * Math.pow((smallestDist - 1), 2);
				 */
				p.metal += 50 / Math.pow((map.getDistToClosestCity(so, p) - 1), 2);
			}
		}

		// p.powerUsage = 0;
		for (GameUnit u : map.getPlayerUnits(p)) {
			u.AP = u.data.AP;
			// p.powerUsage += u.data.powerUsage;

			GameStaticObject target = map.getStaticObj(u);
			if (target != null && target.owner != p
					&& target.data.capturable)
				captureObject(target, p);
		}
	}

	private void addUnitToPlayer(Player p, GameStaticObject city,
			GameUnitData data) throws ResNotFoundException {
		p.metal -= data.metalCost;
		p.powerUsage += data.powerUsage;

		GameUnit u = new GameUnit(target.mapX, target.mapY, data.id, res, p,
				data);

		graphics.addUnitImage(u.getImage());
		map.addUnit(u);

		target = u;
		ui.showUnitInfo((GameUnit) target);
	}

	private void addStaticObjToPlayer(Player p, GameUnit unit,
			GameStaticObjectData data) throws ResNotFoundException {
		p.metal -= data.metalCost;
		p.powerCapability += data.powerGiven;

		GameStaticObject so = new GameStaticObject(target.mapX, target.mapY,
				res.getImage(data.id), res.getImage(data.id + "_fow"), p, data);
		map.addStaticObj(so);

		graphics.addLowImage(so.getImage());

		target = so;
		ui.showStaticObjectInfo((GameStaticObject) target);

		destroyUnit(unit);
	}

	private boolean addObjectToPlayer(Player p, CoordObject location,
			String objId) throws ResNotFoundException {
		if (isStaticData(objId)) {
			GameUnit unit = (GameUnit) location;
			GameStaticObjectData data = res.getStaticObject(objId);
			if (unit.data.buildIDs.contains(objId) && p.canAfford(data)) {
				if (!objId.equals("MINE") && map.getStaticObj(unit) == null) {
					addStaticObjToPlayer(p, unit, data);
					return true;
				} else if (objId.equals("MINE")) {
					GameStaticObject so = map.getStaticObj(unit);
					if (so != null && so.data.id.equals("METAL")) {
						addStaticObjToPlayer(p, unit, data);
						return true;
					}
				}
			}
		} else if (isUnitData(objId)) {
			GameStaticObject so = (GameStaticObject) location;
			GameUnitData data = res.getUnit(objId);
			if (so.active && so.data.buildIDs.contains(objId)
					&& p.canAfford(data)) {
				addUnitToPlayer(p, so, data);
				return true;
			}
		}
		return false;
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
		if (event.e == UIEvent.Event.HANDLED)
			return;

		if (event.e == UIEvent.Event.ACTION_ATTACK)
			startCombatTargeting();
		else if (event.e == UIEvent.Event.ACTION_DESTROY) {
			destroyUnit((GameUnit) target);
			target = null;
		} else if (event.e == UIEvent.Event.ACTION_FORTIFY)
			fortifyUnit((GameUnit) target);
		else if (event.e == UIEvent.Event.END_TURN)
			endTurn();
		else if (event.e.toString().startsWith("BUILD"))
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
			if (ev.getKeyCode() == KeyEvent.VK_F1) {
				GameUnitData data;
				try {
					data = res.getUnit("WORKER");
					this.addUnitToPlayer(players.get(1),
							(GameStaticObject) target, data);
				} catch (ResNotFoundException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
