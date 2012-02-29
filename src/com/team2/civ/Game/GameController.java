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
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
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
import com.team2.civ.UI.UI.UIEvent;

public class GameController {
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

	private HashMap<CoordObject, GameUnit> units = new HashMap<CoordObject, GameUnit>();
	private HashMap<CoordObject, GameStaticObject> staticObjects = new HashMap<CoordObject, GameStaticObject>();

	private HashMap<CoordObject, MapObjectImage> highDraw = new HashMap<CoordObject, MapObjectImage>();
	private TreeMap<CoordObject, MapObjectImage> lowDraw = new TreeMap<CoordObject, MapObjectImage>();

	public List<Player> players = new ArrayList<Player>();
	public Player humanPlayer;
	private Player currentPlayer;
	private MapObject target;

	public int turnCount = 1;
	public static final int MAX_TURNS = 500;

	/*
	 * Make it so that units are always on top of all objects, basically
	 * separate hashmap for them that is sorted
	 */

	public GameController(GraphicsConfiguration config) {
		res = new Resources(config);

		try {
			createMap();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		ui = new UI(humanPlayer, res);
	}

	private void createMap() throws ResNotFoundException {
		HashMap<CoordObject, MapObjectImage> lowDraw = new HashMap<CoordObject, MapObjectImage>();
		
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

		int[][] map = HeightmapGenerator.generateMap(30, 30, 4);
		for (int x = 0; x < 30; x++) {
			for (int y = 0; y < 30; y++) {
				if (map[x][y] == -1)
					continue;
				if (map[x][y] == 0) {
					WallTile wt = new WallTile(x, y, waterImg, waterFowImg);
					unwalkableMap.put(wt, wt);
					highDraw.put(wt, wt.getImage());
				} else if (map[x][y] == 1) {
					WalkableTile t = new WalkableTile(x, y, tileImg,
							tileFowImg, null);
					walkableMap.put(t, t);
					lowDraw.put(t, t.getImage());
				} else if (map[x][y] == 2) {
					WalkableTile t = new WalkableTile(x, y, hillImg,
							hillFowImg, null);
					walkableMap.put(t, t);
					lowDraw.put(t, t.getImage());
				} else if (map[x][y] == 3) {
					WallTile wt = new WallTile(x, y, wallImg, wallFowImg);
					unwalkableMap.put(wt, wt);
					highDraw.put(wt, wt.getImage());
				} else if (map[x][y] == 4) {
					GameStaticObject metal = new GameStaticObject(x, y,
							metalImg, metalFowImg, null,
							res.getStaticObject("METAL"));
					staticObjects.put(metal, metal);
					walkableMap.put(metal, metal);
					lowDraw.put(metal, metal.getImage());
				} else if (map[x][y] == 5) {
					Player p = new Player("Player " + playerIndex,
							colors[playerIndex - 1], null);
					players.add(p);
					GameStaticObject city = new GameStaticObject(x, y, cityImg,
							cityFowImg, p, res.getStaticObject("CITY"));
					
					if(playerIndex == 1) {
						offsetX = -city.x + Team2Civ.WINDOW_WIDTH/2;
						offsetY = -city.y + Team2Civ.WINDOW_HEIGHT/2;
					}
					
					staticObjects.put(city, city);
					walkableMap.put(city, city);
					lowDraw.put(city, city.getImage());
					playerIndex++;
				}
			}
		}

		humanPlayer = players.get(0);

		GameUnit test = new GameUnit(15, 15, "WORKER", res, players.get(0),
				res.getUnit("WORKER"));
		units.put(test, test);
		highDraw.put(test, test.getImage());

		currentPlayer = players.get(0);
		
		this.lowDraw = new TreeMap<CoordObject, MapObjectImage>(lowDraw);
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

			// System.out.println(""+(((float)Team2Civ.WINDOW_WIDTH) * (os -
			// scale) / 2 * (1/os)));

			if (Math.abs(oldScale - scale) >= ZOOM_DELTA) {
				zoomingIn = false;
				zoomingOut = false;
			}
		}

		for (WalkableTile t : walkableMap.values())
			t.beingSeen = false;

		for (WallTile t : unwalkableMap.values())
			t.beingSeen = false;

		for (GameUnit u : units.values()) {
			u.update(gameTime, walkableMap);
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

		for (MapObjectImage i : lowDraw.values())
			i.draw(g, (int) (offsetX), (int) (offsetY), scale);

		TreeMap<CoordObject, MapObjectImage> temp = new TreeMap<CoordObject, MapObjectImage>(
				highDraw);
		for (MapObjectImage i : temp.values())
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
		
		for(GameStaticObject obj: staticObjects.values())
			if(Arrays.asList(type).contains(obj.data.id))
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameStaticObject> getPlayerObjectsOfType(Player p, String... type) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();
		
		for(GameStaticObject obj: staticObjects.values())
			if(obj.owner == p && Arrays.asList(type).contains(obj.data.id))
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameStaticObject> getAllResources() {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();
		
		for(GameStaticObject obj: staticObjects.values())
			if(obj.data.id.equals("MINE") || obj.data.id.equals("METAL"))
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameStaticObject> getAllCities() {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();
		
		for(GameStaticObject obj: staticObjects.values())
			if(obj.data.id.equals("CITY"))
				rtn.add(obj);
				
		return rtn;
	}

	public ArrayList<GameStaticObject> getPlayerCities(Player p) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();
		
		for(GameStaticObject obj: staticObjects.values())
			if(obj.data.id.equals("CITY") && obj.owner == p)
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameUnit> getAllUnits() {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();
		
		for(GameUnit obj: units.values())
			rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameUnit> getPlayerUnits(Player p) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();
		
		for(GameUnit obj: units.values())
			if(obj.owner == p)
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameUnit> getUnitsOfType(String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();
		
		for(GameUnit obj: units.values())
			if(Arrays.asList(id).contains(obj.data.id))
				rtn.add(obj);
				
		return rtn;
	}
	
	public ArrayList<GameUnit> getPlayerUnitsOfType(Player p, String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();
		
		for(GameUnit obj: units.values())
			if(obj.owner == p && Arrays.asList(id).contains(obj.data.id))
				rtn.add(obj);
				
		return rtn;
	}

	private void startCombat() {

	}

	private void destroyTarget() {

	}

	private void fortifyTarget() {

	}
	
	public int getNOfTurnsLeft() {
		return MAX_TURNS - turnCount;
	}

	private void endTurn() {
		currentPlayer = players.get(players.indexOf(currentPlayer) % (players.size()-1));
		if(currentPlayer != humanPlayer) {
			//TODO: show what the AI is doing
			//currentPlayer.ai.perform();
			endTurn();
		} else {
			turnCount++;
		}
	}

	private void addUnitToPlayer(Player p, CoordObject location, GameUnitData data) throws ResNotFoundException {
		if(p.metal >= data.metalCost) {
			p.metal -= data.metalCost;
			
			GameUnit u = new GameUnit(target.mapX, target.mapY, data.id, res, p, data);
			units.put(u, u);
			highDraw.put(u, u.getImage());
			
			target = u;
			ui.showUnitInfo((GameUnit) target);
		}
	}
	
	private void addStaticObjToPlayer(Player p, CoordObject location, GameStaticObjectData data) throws ResNotFoundException {
		if(staticObjects.get(location) == null && p.metal >= data.metalCost) {
			p.metal -= data.metalCost;
			
			GameStaticObject so = new GameStaticObject(target.mapX,
					target.mapY, res.getImage(data.id), res.getImage(data.id
							+ "_fow"), p, data);
			staticObjects.put(so, so);
			walkableMap.put(so, so);
			lowDraw.put(so, so.getImage());
			
			target = so;
			ui.showStaticObjectInfo((GameStaticObject) target);
			
			highDraw.remove(location);
			units.remove(location);
		}
	}

	public void addObjectToPlayer(Player p, CoordObject location, String objId) throws ResNotFoundException {
		if(isStaticData(objId))
			addStaticObjToPlayer(p, location, res.getStaticObject(objId));
		else if(isUnitData(objId))
			addUnitToPlayer(p, location, res.getUnit(objId));
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

	private void build(String obj) throws ResNotFoundException {
		addObjectToPlayer(currentPlayer, target, obj);
	}

	public void performEvent(UIEvent event) {
		if (event == UIEvent.HANDLED)
			return;

		if (event == UIEvent.ACTION_ATTACK)
			startCombat();
		else if (event == UIEvent.ACTION_DESTROY)
			destroyTarget();
		else if (event == UIEvent.ACTION_FORTIFY)
			fortifyTarget();
		else if (event == UIEvent.END_TURN)
			endTurn();
		else if (event.toString().startsWith("BUILD"))
			try {
				build(event.toString().replace("BUILD_", ""));
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
	}

	public void onMouseInput(MouseEvent ev) {
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
						movingUnit.startMovement(findPath(movingUnit, t, true,
								-1));
					}
				}
			}
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				for (GameUnit u : units.values())
					if (u.picked((int) (ev.getX() * (1 / scale) - offsetX),
							(int) (ev.getY() * (1 / scale) - offsetY))) {
						if (target != null)
							target.selected = false;
						target = u;
						target.selected = true;
						ui.showUnitInfo((GameUnit) target);
					}

				for (GameStaticObject so : staticObjects.values())
					if (so.picked((int) (ev.getX() * (1 / scale) - offsetX),
							(int) (ev.getY() * (1 / scale) - offsetY))) {
						if (target != null)
							target.selected = false;
						target = so;
						target.selected = true;
						ui.showStaticObjectInfo((GameStaticObject) target);
					}
			}
		}
	}

	public void onMouseWheelInput(MouseWheelEvent ev) {
		if (!zoomingIn && !zoomingOut) {
			if (ev.getWheelRotation() < 0)
				zoomIn();
			else if (ev.getWheelRotation() > 0)
				zoomOut();
		}
	}

	public void onKeyboardInput(KeyEvent ev) {
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

	public ArrayList<WalkableTile> findPath(GameUnit startObj,
			CoordObject targetObj, boolean walkOnTarget, int lengthLimit) {
		HashMap<CoordObject, PathNode> nodeList = new HashMap<CoordObject, PathNode>();
		ArrayList<PathNode> openList = new ArrayList<PathNode>();
		ArrayList<PathNode> closedList = new ArrayList<PathNode>();
		ArrayList<WalkableTile> returnList = new ArrayList<WalkableTile>();

		for (WalkableTile tile : walkableMap.values()) {
			GameUnit u = units.get(tile);
			if (u == null || u.owner == startObj.owner)
				nodeList.put(tile, new PathNode(tile.mapX, tile.mapY));
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

		if (lengthLimit == -1) {
			while (target != null) {
				returnList.add(walkableMap.get(new CoordObject(target.mapX,
						target.mapY)));
				target = target.parent;
			}
		} else {
			int length = 0;
			PathNode tempTarget = target;
			while (tempTarget != null) {
				tempTarget = tempTarget.parent;
				length++;
			}
			length -= lengthLimit;
			while (target != null) {
				if (length < 1)
					returnList.add(walkableMap.get(new CoordObject(target.mapX,
							target.mapY)));
				target = target.parent;
				length--;
			}
		}

		return returnList;
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

}
