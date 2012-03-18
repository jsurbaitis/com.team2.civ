package com.team2.civ.Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import com.team2.civ.Map.MapObjectImage;
import com.team2.civ.Map.PathNode;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;
import com.team2.civ.UI.UI;
import com.team2.civ.UI.UIEvent;

public class GameController {
	private static final boolean FOW_ON = true;

	private static final int MAP_WIDTH = 50;
	private static final int MAP_HEIGHT = 50;

	private long gameTime = 0;

	private int offsetX = 0;
	private int offsetY = 0;

	private int pressStartX;
	private int pressStartY;

	private int lastMouseX;
	private int lastMouseY;

	private static final double SCALE_MAX = 1.0;
	private static final double SCALE_MIN = 0.2;
	private static final double ZOOM_DELTA = 0.2;
	private static final double ZOOM_FACTOR = 0.02;

	private double scale = 1.0f;
	private double oldScale = scale;
	private boolean zoomingIn = false;
	private boolean zoomingOut = false;

	private boolean leftClick = true;

	private Resources res;
	private UI ui;

	private final int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 }, { 1, -1 },
			{ 0, -1 }, { -1, 0 } };

	private HashMap<CoordObject, WallTile> unwalkableMap = new HashMap<CoordObject, WallTile>();
	private HashMap<CoordObject, WalkableTile> walkableMap = new HashMap<CoordObject, WalkableTile>();

	private Vector<GameUnit> units = new Vector<GameUnit>();
	private HashMap<CoordObject, GameStaticObject> staticObjects = new HashMap<CoordObject, GameStaticObject>();

	private Vector<MapObjectImage> highDraw = new Vector<MapObjectImage>();
	private ArrayList<MapObjectImage> lowDraw = new ArrayList<MapObjectImage>();

	public List<Player> players = new ArrayList<Player>();
	public Player humanPlayer;
	private Player currentPlayer;
	private MapObject target;

	private List<GameAction> showingActionList;
	private int actionIndex;
	private int actionTimer;
	private GameAction currentAction;

	private Vector<GameUnit> combatTargets = new Vector<GameUnit>();

	public int turnCount = 1;
	public static final int MAX_TURNS = 630;

	public GameController(GraphicsConfiguration config) {
		res = new Resources(config);
	}

	public void initGame() {
		try {
			createMap();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		ui = new UI(humanPlayer, res, this);
	}

	public AI runGame(AI a1, AI a2, AI a3, AI a4) {
		try {
			createMap();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		humanPlayer = null;
		players.get(0).ai = a1;
		players.get(1).ai = a2;
		players.get(2).ai = a3;
		players.get(3).ai = a4;

		return null;
		// currentPlayer.performTurn();
	}

	private void createMap() throws ResNotFoundException {
		MapObjectImage.selectedImg = res.getImage("selected");
		BufferedImage tileImg = res.getImage("tile_grass");
		BufferedImage wallImg = res.getImage("wall");
		BufferedImage waterImg = res.getImage("water");
		BufferedImage hillImg = res.getImage("hill");
		BufferedImage metalImg = res.getImage("metal");
		BufferedImage cityImg = res.getImage("CITY");

		BufferedImage tileFowImg = res.getImage("tile_grass_fow");
		BufferedImage wallFowImg = res.getImage("wall_fow");
		BufferedImage waterFowImg = res.getImage("water_fow");
		BufferedImage hillFowImg = res.getImage("hill_fow");
		BufferedImage metalFowImg = res.getImage("metal_fow");
		BufferedImage cityFowImg = res.getImage("CITY_fow");

		String[] colors = { "#FFFFFF", "#FF5400", "#545454", "#000FFF" };
		int playerIndex = 1;

		int[][] map = HeightmapGenerator.generateMap(MAP_WIDTH, MAP_HEIGHT, 4);
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				if (map[x][y] == -1)
					continue;
				if (map[x][y] == 0) {
					WallTile wt = new WallTile(x, y, waterImg, waterFowImg);
					unwalkableMap.put(wt, wt);
					lowDraw.add(wt.getImage());
				} else if (map[x][y] == 1) {
					WalkableTile t = new WalkableTile(x, y, tileImg,
							tileFowImg, null);
					walkableMap.put(t, t);
					lowDraw.add(t.getImage());
				} else if (map[x][y] == 2) {
					WalkableTile t = new WalkableTile(x, y, hillImg,
							hillFowImg, null);
					walkableMap.put(t, t);
					lowDraw.add(t.getImage());
				} else if (map[x][y] == 3) {
					WallTile wt = new WallTile(x, y, wallImg, wallFowImg);
					unwalkableMap.put(wt, wt);
					lowDraw.add(wt.getImage());
				} else if (map[x][y] == 4) {
					GameStaticObject metal = new GameStaticObject(x, y,
							metalImg, metalFowImg, null,
							res.getStaticObject("METAL"));
					staticObjects.put(metal, metal);
					walkableMap.put(metal, metal);
					lowDraw.add(metal.getImage());
				} else if (map[x][y] == 5) {
					Player p = new Player("Player " + playerIndex,
							colors[playerIndex - 1], null);
					players.add(p);

					GameStaticObject city = new GameStaticObject(x, y, cityImg,
							cityFowImg, p, res.getStaticObject("CITY"));

					if (playerIndex == 1) {
						offsetX = -city.x + Team2Civ.WINDOW_WIDTH / 2;
						offsetY = -city.y + Team2Civ.WINDOW_HEIGHT / 2;
					}

					staticObjects.put(city, city);
					walkableMap.put(city, city);
					lowDraw.add(city.getImage());
					playerIndex++;
				}
			}
		}

		Collections.sort(lowDraw);

		humanPlayer = players.get(0);
		currentPlayer = players.get(0);

		for (int i = 1; i < players.size(); i++) {
			Player p = players.get(i);
			p.ai = new AI(this);
		}
	}

	public void update(long timeElapsedMillis) {
		gameTime++;

		ui.update(gameTime);

		if (zoomingIn || zoomingOut) {
			double os = scale;

			if (zoomingIn) {
				scale += ZOOM_FACTOR;
			} else {
				scale -= ZOOM_FACTOR;
			}

			offsetX += ((float) Team2Civ.WINDOW_WIDTH) * (os - scale) / 2
					* (1 / scale);
			offsetY += ((float) Team2Civ.WINDOW_HEIGHT) * (os - scale) / 2
					* (1 / scale);

			if (Math.abs(oldScale - scale) >= ZOOM_DELTA) {
				zoomingIn = false;
				zoomingOut = false;
			}
		}

		if (showingActionList != null) {
			if(currentAction.actor != null) {
				offsetX = -currentAction.actor.x + Team2Civ.WINDOW_WIDTH / 2;
				offsetY = -currentAction.actor.y + Team2Civ.WINDOW_HEIGHT / 2;
			}
			
			if(currentAction.event == GameAction.Event.ACTION_MOVE) {
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

		for (WalkableTile t : walkableMap.values())
			t.beingSeen = FOW_ON ? false : true;

		for (WallTile t : unwalkableMap.values())
			t.beingSeen = FOW_ON ? false : true;

		if (FOW_ON) {
			for (GameUnit u : units) {
				u.update(gameTime);
				if (u.owner == humanPlayer) {
					u.isSeen();
					updateFow(u, u.data.fowRange);
				}
			}

			for (GameStaticObject so : staticObjects.values()) {
				if (so.owner == humanPlayer) {
					so.isSeen();
					updateFow(so, so.data.fowRange);
				}
			}
		}
	}

	private void updateFow(CoordObject co, int range) {
		MapObject o = walkableMap.get(co);
		if (o != null) {
			o.isSeen();
		} else {
			o = unwalkableMap.get(co);
			if (o != null)
				o.isSeen();
		}

		updateFowAround(co, range);
	}

	private void updateFowAround(CoordObject co, int range) {
		if (range == 0)
			return;

		MapObject o;
		for (int z = 0; z < nextPos.length; z++) {
			o = walkableMap.get(new CoordObject(co.mapX + nextPos[z][0],
					co.mapY + nextPos[z][1]));
			if (o != null) {
				updateFowAround(o, range - 1);
				o.isSeen();
			} else {
				o = unwalkableMap.get(new CoordObject(co.mapX + nextPos[z][0],
						co.mapY + nextPos[z][1]));
				if (o != null) {
					updateFowAround(o, range - 1);
					o.isSeen();
				}
			}
		}
	}

	public HashMap<CoordObject, WalkableTile> getWalkableTilesCopy() {
		return new HashMap<CoordObject, WalkableTile>(walkableMap);
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Team2Civ.WINDOW_WIDTH, Team2Civ.WINDOW_HEIGHT);

		g.scale(scale, scale);

		for (MapObjectImage i : lowDraw)
			i.draw(g, (int) (offsetX), (int) (offsetY), scale);

		Collections.sort(highDraw);
		for (MapObjectImage i : highDraw)
			i.draw(g, (int) (offsetX), (int) (offsetY), scale);

		g.scale(1 / scale, 1 / scale);

		ui.draw(g);
	}

	private void zoomIn() {
		if (scale < SCALE_MAX) {
			oldScale = scale;
			zoomingIn = true;
		}
	}

	private void zoomOut() {
		if (scale > SCALE_MIN) {
			oldScale = scale;
			zoomingOut = true;
		}
	}

	public ArrayList<GameStaticObject> getObjectsOfType(String... type) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (Arrays.asList(type).contains(obj.data.id))
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameStaticObject> getPlayerObjectsOfType(Player p,
			String... type) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.owner == p && Arrays.asList(type).contains(obj.data.id))
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameStaticObject> getAllResources() {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.data.id.equals("MINE") || obj.data.id.equals("METAL"))
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameStaticObject> getAllCities() {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.data.id.equals("CITY"))
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameStaticObject> getPlayerCities(Player p) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.data.id.equals("CITY") && obj.owner == p)
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameUnit> getAllUnits() {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		// for(GameUnit obj: units.values())
		// rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameUnit> getPlayerUnits(Player p) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		// for(GameUnit obj: units.values())
		// if(obj.owner == p)
		// rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameUnit> getUnitsOfType(String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		// for(GameUnit obj: units.values())
		// if(Arrays.asList(id).contains(obj.data.id))
		// rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameUnit> getPlayerUnitsOfType(Player p, String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		// for(GameUnit obj: units.values())
		// if(obj.owner == p && Arrays.asList(id).contains(obj.data.id))
		// rtn.add(obj);

		return rtn;
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
		lowDraw.remove(target);
		staticObjects.remove(target);
		performer.metal += target.data.metalCost * 0.2;
	}

	private void captureObject(GameStaticObject target, Player performer) {
		target.owner = performer;
	}

	private void startMovement(GameUnit actor, MapObject target) {
		endCombatTargeting();
		List<WalkableTile> path = findPath(actor, target, actor.owner, true,
				actor.AP);
		actor.AP -= path.size();

		if (!Team2Civ.AI_MODE)
			actor.startMovement(path);
		else {
			actor.setPos(path.get(0).mapX, path.get(0).mapY);
		}
	}

	private void performCombat(GameUnit attacker, GameUnit target) {
		target.takeDmg(calcCombatDmg(attacker, target));
		attacker.AP = 0;
	}

	public int calcCombatDmg(GameUnit attacker, GameUnit target) {
		GameStaticObject so = staticObjects.get(target);
		int dmg = attacker.getDmgToDeal(target.data.id);
		if (so != null)
			dmg *= (1 - so.data.defensiveBonus / 100);

		Random rnd = new Random();
		double modif = (80 + rnd.nextInt(40)) / 100;
		dmg *= modif;

		return dmg;
	}

	private void startCombatTargeting() {
		for (GameUnit u : units) {
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
		synchronized (highDraw) {
			highDraw.remove(u.getImage());
		}

		units.remove(u);
		u.owner.metal += u.data.metalCost * 0.25;
	}

	private void fortifyUnit(GameUnit u) {
		u.fortify();
	}

	public int getNOfTurnsLeft() {
		return MAX_TURNS - turnCount;
	}

	private void endTurn() {
		upkeep(currentPlayer);

		int nextPlayer = (players.indexOf(currentPlayer) + 1) % players.size();
		currentPlayer = players.get(nextPlayer);

		if (currentPlayer != humanPlayer) {
			List<GameAction> actions = currentPlayer.ai.perform(
					getActionsForOthers(currentPlayer), currentPlayer);

			if (Team2Civ.AI_MODE)
				currentPlayer.previousTurn = performActions(actions);
			else
				displayActions(actions);
		} else {
			turnCount++;
		}
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
		for (GameUnit u : units) {
			if (u.owner == p) {
				GameStaticObject so = staticObjects.get(u);
				if (so != null && so.owner != p) {
					if (so.data.capturable)
						so.owner = p;
					else if (so.data.destructible)
						staticObjects.remove(so);
				}
			}
		}

		p.powerCapability = 0;
		for (GameStaticObject so : staticObjects.values()) {
			if (so.owner == p) {
				p.powerCapability += so.data.powerGiven;

				if (so.data.id.equals("MINE")) {
					p.metal += 50 / getDistToClosestCity(so, p);
				}
			}
		}

		p.powerUsage = 0;
		for (GameUnit u : units) {
			if (u.owner == p) {
				u.AP = u.data.AP;
				p.powerUsage += u.data.powerUsage;

				GameStaticObject target = staticObjects.get(u);
				if (target != null && target.owner != p
						&& target.data.capturable)
					captureObject(target, p);
			}
		}
	}

	public int getDistToClosestCity(CoordObject obj, Player p) {
		int smallestDist = Integer.MAX_VALUE;

		for (GameStaticObject so : staticObjects.values()) {
			if (so.data.id.equals("CITY") && so.owner == p) {
				int dist = findPath(obj, so, p).size();
				if (dist < smallestDist) {
					smallestDist = dist;
				}
			}
		}

		return smallestDist;
	}

	private void addUnitToPlayer(Player p, CoordObject location,
			GameUnitData data) throws ResNotFoundException {
		if (p.metal >= data.metalCost) {
			p.metal -= data.metalCost;

			GameUnit u = new GameUnit(target.mapX, target.mapY, data.id, res,
					p, data);
			highDraw.add(u.getImage());
			units.add(u);

			target = u;
			ui.showUnitInfo((GameUnit) target);
		}
	}

	private void addStaticObjToPlayer(Player p, GameUnit unit,
			GameStaticObjectData data) throws ResNotFoundException {
		p.metal -= data.metalCost;

		GameStaticObject so = new GameStaticObject(target.mapX, target.mapY,
				res.getImage(data.id), res.getImage(data.id + "_fow"), p, data);
		staticObjects.put(so, so);
		walkableMap.put(so, so);
		lowDraw.add(so.getImage());

		target = so;
		ui.showStaticObjectInfo((GameStaticObject) target);

		synchronized (highDraw) {
			highDraw.remove(unit.getImage());
		}

		units.remove(unit);
	}

	private boolean addObjectToPlayer(Player p, CoordObject location,
			String objId) throws ResNotFoundException {
		if (isStaticData(objId)) {
			GameUnit unit = (GameUnit) location;
			GameStaticObjectData data = res.getStaticObject(objId);
			if (unit.data.buildIDs.contains(objId)
					&& staticObjects.get(unit) == null
					&& p.metal >= data.metalCost) {
				addStaticObjToPlayer(p, unit, data);
				return true;
			}
		} else if (isUnitData(objId)) {
			GameStaticObject so = (GameStaticObject) location;
			GameUnitData data = res.getUnit(objId);
			if (so.data.buildIDs.contains(objId) && p.metal >= data.metalCost) {
				addUnitToPlayer(p, location, data);
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
			offsetX += (ev.getX() - lastMouseX) * (1 / scale);
			offsetY += (ev.getY() - lastMouseY) * (1 / scale);

			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick
				&& target != null && target instanceof GameUnit) {
			GameUnit movingUnit = (GameUnit) target;
			if (movingUnit.owner == currentPlayer
					&& Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				for (WalkableTile t : walkableMap.values()) {
					if (t.picked((int) (ev.getX() * (1 / scale) - offsetX),
							(int) (ev.getY() * (1 / scale) - offsetY))) {

						startMovement(movingUnit, t);
						endCombatTargeting();
					}
				}
			}
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {

				for (GameUnit u : combatTargets) {
					if (u.picked((int) (ev.getX() * (1 / scale) - offsetX),
							(int) (ev.getY() * (1 / scale) - offsetY))) {
						performCombat((GameUnit) target, u);
						return;
					}
				}

				ArrayList<MapObject> targets = new ArrayList<MapObject>();

				for (GameUnit u : units) {
					if (u.owner == humanPlayer
							&& u.picked(
									(int) (ev.getX() * (1 / scale) - offsetX),
									(int) (ev.getY() * (1 / scale) - offsetY))) {

						endCombatTargeting();
						targets.add(u);
					}
				}

				for (GameStaticObject so : staticObjects.values()) {
					if (so.owner == humanPlayer
							&& so.picked(
									(int) (ev.getX() * (1 / scale) - offsetX),
									(int) (ev.getY() * (1 / scale) - offsetY))) {

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

		if (!zoomingIn && !zoomingOut) {
			if (ev.getWheelRotation() < 0)
				zoomIn();
			else if (ev.getWheelRotation() > 0)
				zoomOut();
		}
	}

	public void onKeyboardInput(KeyEvent ev) {
		if (currentPlayer != humanPlayer)
			return;

		if (ev.getID() == KeyEvent.KEY_PRESSED) {
			if (ev.getKeyCode() == KeyEvent.VK_UP)
				offsetY += 12;
			if (ev.getKeyCode() == KeyEvent.VK_DOWN)
				offsetY -= 12;
			if (ev.getKeyCode() == KeyEvent.VK_LEFT)
				offsetX += 12;
			if (ev.getKeyCode() == KeyEvent.VK_RIGHT)
				offsetX -= 12;
		}
	}

	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj, Player owner) {
		return findPath(startObj, targetObj, owner, true, -1);
	}

	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj) {
		return findPath(startObj, targetObj, null, true, -1);
	}

	private List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj, Player owner, boolean walkOnTarget,
			int lengthLimit) {
		HashMap<CoordObject, PathNode> nodeList = new HashMap<CoordObject, PathNode>();
		ArrayList<PathNode> openList = new ArrayList<PathNode>();
		ArrayList<PathNode> closedList = new ArrayList<PathNode>();
		ArrayList<WalkableTile> returnList = new ArrayList<WalkableTile>();

		for (WalkableTile tile : walkableMap.values()) {
			for (GameUnit u : units) {
				if (u.mapX == tile.mapX && u.mapY == tile.mapY) {
					if (owner == null || u.owner == owner)
						nodeList.put(tile, new PathNode(tile.mapX, tile.mapY));
				} else {
					nodeList.put(tile, new PathNode(tile.mapX, tile.mapY));
				}
			}
		}
		nodeList.put(startObj, new PathNode(startObj.mapX, startObj.mapY));

		boolean nextToTarget = true;
		if (walkOnTarget)
			nextToTarget = false;

		PathNode target = null;
		PathNode nextTile = null;
		PathNode minTile = null;
		PathNode current = nodeList.get(startObj);
		current.heuristic = getHeuristic(current.mapX, current.mapY,
				targetObj.mapX, targetObj.mapY);
		current.cost = 0;
		current.parent = null;
		openList.add(current);

		while (openList.size() != 0) {
			if (!nextToTarget) {
				if (current.mapX == targetObj.mapX
						&& current.mapY == targetObj.mapY) {
					target = current;
					break;
				}
			} else {
				if (Math.abs(current.mapX - targetObj.mapX) < 2
						&& Math.abs(current.mapY - targetObj.mapY) < 2) {
					target = current;
					break;
				}
			}
			closedList.add(current);
			openList.remove(current);

			for (int i = 0; i < nextPos.length; i++) {
				nextTile = nodeList.get(new CoordObject(current.mapX
						+ nextPos[i][0], current.mapY + nextPos[i][1]));
				if (nextTile == null || closedList.contains(nextTile))
					continue;

				int tentativeScore = current.cost
						+ getTentativeScore(current.mapX, current.mapY,
								nextTile.mapX, nextTile.mapY);

				boolean isBetter = false;
				if (!openList.contains(nextTile)) {
					nextTile.heuristic = getHeuristic(nextTile.mapX,
							nextTile.mapY, targetObj.mapX, targetObj.mapY);
					openList.add(nextTile);
					isBetter = true;
				} else if (tentativeScore < nextTile.cost)
					isBetter = true;

				if (isBetter) {
					nextTile.parent = current;
					nextTile.cost = tentativeScore;
				}
			}

			minTile = null;
			for (int i = 0; i < openList.size(); i++) {
				if (minTile == null
						|| minTile.cost + minTile.heuristic > openList.get(i).cost
								+ openList.get(i).heuristic)
					minTile = openList.get(i);
			}

			current = minTile;
		}

		if (target == null) {
			return null;
		}

		while (target != null) {
			returnList.add(walkableMap.get(new CoordObject(target.mapX,
					target.mapY)));
			target = target.parent;
		}

		if (lengthLimit == -1)
			return returnList;

		int index = returnList.size() - 1;
		return returnList.subList(index - lengthLimit, index);
	}

	private static int getTentativeScore(int x, int y, int tx, int ty) {
		return Math.abs(x - tx) + Math.abs(y - ty);
	}

	private static int getHeuristic(int x, int y, int tx, int ty) {
		int dx = tx - x;
		int dy = ty - y;

		int result = (dx * dx) + (dy * dy);
		return result;
	}

	public Collection<WallTile> getUnwalkableMap() {
		return Collections.unmodifiableCollection(unwalkableMap.values());
	}

	public Collection<WalkableTile> getWalkableMap() {
		return Collections.unmodifiableCollection(walkableMap.values());
	}

	public Collection<GameUnit> getUnit() {
		return Collections.unmodifiableCollection(units);
	}

	public Collection<GameStaticObject> getStaticObjects() {
		return Collections.unmodifiableCollection(staticObjects.values());
	}
}
