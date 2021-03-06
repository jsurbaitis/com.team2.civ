package com.team2.civ.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.MapObject;
import com.team2.civ.Map.MapObjectImage;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class GameMap {
	
	public static boolean FOW_ON = true;

	public static final int MAP_WIDTH = 50;
	public static final int MAP_HEIGHT = 50;
	
	private static final int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 }, { 1, -1 },
			{ 0, -1 }, { -1, 0 } };
	
	private HashMap<CoordObject, WallTile> unwalkableMap = new HashMap<CoordObject, WallTile>();
	private HashMap<CoordObject, WalkableTile> walkableMap = new HashMap<CoordObject, WalkableTile>();

	private Vector<GameUnit> units = new Vector<GameUnit>();
	private HashMap<CoordObject, GameStaticObject> staticObjects = new HashMap<CoordObject, GameStaticObject>();

	private List<Path> paths = new ArrayList<Path>();
	private HashMap<WalkableTile, HashMap<Path, Double>> stratLocData = new HashMap<WalkableTile, HashMap<Path, Double>>();
	private HashMap<WalkableTile, Double> stratLocValues = new HashMap<WalkableTile, Double>();
	public double avgStratLocValue;
	private double stratLocPathSum;
	private boolean stratLocsNeedUpdate = true;

	private Resources res;
	
	public GameMap() {
		res = Resources.getInstance();
		
		try {
			createMap();
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void createMap() throws ResNotFoundException {
		MapObjectImage.selectedImg = res.getImage("selected");

		HeightmapGenerator gen = new HeightmapGenerator();
		int[][] map = gen.generateMap(MAP_WIDTH, MAP_HEIGHT, 4);
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_HEIGHT; y++) {
				if (map[x][y] == -1)
					continue;
				if (map[x][y] == 0) {
					WallTile wt = new WallTile(x, y, "water", WallTile.Type.WATER);
					unwalkableMap.put(wt, wt);
				} else if (map[x][y] == 1) {
					WalkableTile t = new WalkableTile(x, y, "tile_grass", null);
					walkableMap.put(t, t);
				} else if (map[x][y] == 2) {
					WalkableTile t = new WalkableTile(x, y, "hill", null);
					walkableMap.put(t, t);
				} else if (map[x][y] == 3) {
					WallTile wt = new WallTile(x, y, "wall", WallTile.Type.MOUNTAIN);
					unwalkableMap.put(wt, wt);
				} else if (map[x][y] == 4) {
					GameStaticObject metal = new GameStaticObject(x, y, null,
							res.getStaticObject("METAL"));
					staticObjects.put(metal, metal);
					walkableMap.put(metal, metal);
				} else if (map[x][y] == 5) {
					GameStaticObject city = new GameStaticObject(x, y, null, res.getStaticObject("CITY"));

					staticObjects.put(city, city);
					walkableMap.put(city, city);
				}
			}
		}

		calcAvgStratLocValue();
	}
	
	public void updateFow(Player humanPlayer) {
		if (FOW_ON) {
			for (WalkableTile t : walkableMap.values())
				t.beingSeen = false;

			for (WallTile t : unwalkableMap.values())
				t.beingSeen = false;
			
			for (GameUnit u : units) {
				if (u.owner == humanPlayer) {
					u.isSeen();
					updateFowAroundObj(u, u.data.fowRange);
				}
			}

			for (GameStaticObject so : staticObjects.values()) {
				if (so.owner == humanPlayer) {
					so.isSeen();
					updateFowAroundObj(so, so.data.fowRange);
				}
			}
			
			for(GameUnit u: units) {
				if(walkableMap.get(u).beingSeen == true)
					u.isSeen();
				else
					u.beingSeen = false;
			}
		}
	}
	
	private void updateFowAroundObj(CoordObject co, int range) {
		MapObject o = walkableMap.get(co);
		if (o != null) {
			o.isSeen();
		} else {
			o = unwalkableMap.get(co);
			if (o != null)
				o.isSeen();
		}

		updateFowAroundRec(co, range);
	}

	private void updateFowAroundRec(CoordObject co, int range) {
		if (range == 0)
			return;

		MapObject o;
		for (int z = 0; z < nextPos.length; z++) {
			CoordObject key = new CoordObject(co.mapX + nextPos[z][0], co.mapY + nextPos[z][1]);
			o = walkableMap.get(key);
			if (o != null) {
				updateFowAroundRec(o, range - 1);
				o.isSeen();
			} else {
				o = unwalkableMap.get(key);
				if (o != null) {
					updateFowAroundRec(o, range - 1);
					o.isSeen();
				}
			}
		}
	}
	
	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj, Player owner) {
		return findPath(startObj, targetObj, owner, null, true, true, -1);
	}

	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj) {
		return findPath(startObj, targetObj, null, null, true, true, -1);
	}

	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj, WalkableTile exclude, boolean ignoreUnits) {
		return findPath(startObj, targetObj, null, exclude, true, ignoreUnits,
				-1);
	}
	
	public List<WalkableTile> findPath(CoordObject startObj,
			CoordObject targetObj, Player owner, WalkableTile exclude,
			boolean walkOnTarget, boolean ignoreUnits, int lengthLimit) {

		if (exclude == startObj || exclude == targetObj)
			return null;

		ArrayList<WalkableTile> openList = new ArrayList<WalkableTile>();
		ArrayList<WalkableTile> closedList = new ArrayList<WalkableTile>();
		ArrayList<WalkableTile> returnList = new ArrayList<WalkableTile>();

		boolean nextToTarget = true;
		if (walkOnTarget)
			nextToTarget = false;

		WalkableTile target = null;
		WalkableTile nextTile = null;
		WalkableTile minTile = null;
		WalkableTile current = walkableMap.get(startObj);
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
				nextTile = walkableMap.get(new CoordObject(current.mapX
						+ nextPos[i][0], current.mapY + nextPos[i][1]));

				if (nextTile == null || closedList.contains(nextTile)
						|| exclude == nextTile)
					continue;

				if (!ignoreUnits) {
					if (!isTileFree(nextTile, owner))
						continue;
				}

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
			for (WalkableTile wt: openList) {
				if(minTile == null || (minTile.cost + minTile.heuristic > wt.cost + wt.heuristic))
					minTile = wt;
			}

			current = minTile;
		}

		if (target == null) {
			return null;
		}

		while (target != null) {
			returnList.add(target);
			target = target.parent;
		}
		
		returnList.remove(returnList.size() - 1);

		if (lengthLimit == -1)
			return returnList;

		int index = returnList.size();
		int fromIndex = index - lengthLimit;
		return returnList.subList((fromIndex < 0) ? 0 : fromIndex, index);
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
	
	public HashMap<WalkableTile, Double> getStratLocValues() {
		if (stratLocsNeedUpdate) {
			List<WalkableTile> path;
			stratLocValues.clear();
			
			for (Path p : paths) {
				for (int i = 0; i < p.path.size(); i++) {
					WalkableTile t = p.path.get(i);
					double val = 0;
					double score;
					for (Path p2 : paths) {
						if(p2.updated || stratLocData.get(t) == null || stratLocData.get(t).get(p2) == null) {
							if (p2 != p && p2.path.contains(t)) {
								path = findPath(p2.startObj, p2.endObj, t, false);
								if (path != null) {
									score = path.size();
								} else
									score = 500;
							} else {
								score = p2.path.size();
							}
							
							HashMap<Path, Double> tileScores = stratLocData.get(t);
							if(tileScores == null) {
								tileScores = new HashMap<Path, Double>();
								tileScores.put(p2, score);
								stratLocData.put(t, tileScores);
							} else {
								tileScores.put(p2, score);
							}
						} else {
							score = stratLocData.get(t).get(p2);
						}
						val += score;
					}
					stratLocValues.put(t, val / stratLocPathSum);
				}
			}
			
			for(Path p: paths) p.updated = false;
		}
		stratLocsNeedUpdate = false;
		return stratLocValues;
	}

	public boolean isTileFree(WalkableTile t) {
		for (GameUnit u : units) {
			if (u.mapX == t.mapX && u.mapY == t.mapY)
				return false;
		}
		return true;
	}

	public boolean isTileFree(WalkableTile t, Player p) {
		for (GameUnit u : units) {
			if (u.owner != p && u.mapX == t.mapX && u.mapY == t.mapY)
				return false;
		}
		return true;
	}

	private int calcStratValue(WalkableTile tile) {
		int rtn = 0;

		List<GameStaticObject> mines = getObjectsOfType("MINE");
		List<GameStaticObject> cities = getAllCities();
		List<WalkableTile> path;

		for (GameStaticObject c1 : cities) {
			for (GameStaticObject c2 : cities) {
				if (c1 != c2) {
					path = findPath(c1, c2, tile, false);
					if (path != null) {
						if (tile != null)
							rtn += 500;
						rtn += path.size();
					}
				}
			}

			for (GameStaticObject m : mines) {
				path = findPath(c1, m, tile, false);
				if (path != null) {
					if (tile != null)
						rtn += 500;
					rtn += path.size();
				}
			}
		}

		return rtn;
	}

	private void calcAvgStratLocValue() {
		double sum = 0;
		avgStratLocValue = 0;

		for (WalkableTile tile : walkableMap.values()) {
			sum += calcStratValue(tile);
		}

		avgStratLocValue = sum / walkableMap.values().size();
	}

	private boolean needToUpdatePath(GameStaticObject o1, GameStaticObject o2) {
		for (Path p : paths) {
			if ((p.startObj == o1 && p.endObj == o2)
					|| (p.startObj == o2 && p.endObj == o1)) {
				return !p.isFree();
			}
		}

		return true;
	}
	
	private Path getPath(GameStaticObject o1, GameStaticObject o2) {
		for (Path p : paths) {
			if ((p.startObj == o1 && p.endObj == o2)
					|| (p.startObj == o2 && p.endObj == o1)) {
				return p;
			}
		}
		return null;
	}
	
	private void removePath(Path p) {
		paths.remove(p);
		for(HashMap<Path, Double> h: stratLocData.values())
			h.remove(p);
	}

	public void updatePathsAndStratLoc() {
		List<GameStaticObject> mines = getObjectsOfType("MINE");
		List<GameStaticObject> cities = getAllCities();
		List<WalkableTile> path;
		stratLocPathSum = 0;

		for (GameStaticObject c1 : cities) {
			for (GameStaticObject c2 : cities) {
				if (c1 != c2 && needToUpdatePath(c1, c2)) {
					removePath(getPath(c1, c2));
					
					path = findPath(c1, c2, null, false);
					if (path != null) {
						paths.add(new Path(c1, c2, path));
						stratLocPathSum += path.size();
					}
				}
			}

			for (GameStaticObject m : mines) {
				if (needToUpdatePath(c1, m)) {
					removePath(getPath(c1, m));
					
					path = findPath(c1, m, null, false);
					if (path != null) {
						paths.add(new Path(c1, m, path));
						stratLocPathSum += path.size();
					}
				}
			}
		}

		stratLocsNeedUpdate = true;
	}

	private class Path {
		public GameStaticObject startObj, endObj;
		public List<WalkableTile> path;
		public boolean updated = true;

		public Path(GameStaticObject startObj, GameStaticObject endObj,
				List<WalkableTile> path) {
			this.startObj = startObj;
			this.endObj = endObj;
			this.path = path;
		}

		public boolean isFree() {
			for (WalkableTile t : path) {
				if (!isTileFree(t))
					return false;
			}

			return true;
		}
	}
	
	public List<GameStaticObject> getFreeEnemyObjectsAround(GameUnit loc) {
		List<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for(int i = 0; i < nextPos.length; i++) {
			GameStaticObject obj = staticObjects.get(
					new CoordObject(loc.mapX + nextPos[i][0], loc.mapY + nextPos[i][1]));
			if (obj != null && obj.owner != loc.owner && isTileFree(obj, loc.owner))
				rtn.add(obj);
		}

		return rtn;
	}
	
	public List<GameStaticObject> getPlayerObjects(Player p) {
		List<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.owner == p)
				rtn.add(obj);

		return rtn;
	}
	
	public List<GameStaticObject> getObjectsOfType(String... type) {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (Arrays.asList(type).contains(obj.data.id))
				rtn.add(obj);

		return rtn;
	}

	public List<GameStaticObject> getPlayerObjectsOfType(Player p,
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
	
	public ArrayList<GameStaticObject> getMetalNodes() {
		ArrayList<GameStaticObject> rtn = new ArrayList<GameStaticObject>();

		for (GameStaticObject obj : staticObjects.values())
			if (obj.data.id.equals("METAL"))
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

	public ArrayList<GameUnit> getPlayerUnits(Player p) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : units)
			if (obj.owner == p)
				rtn.add(obj);

		return rtn;
	}

	public ArrayList<GameUnit> getUnitsOfType(String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : units)
			if (Arrays.asList(id).contains(obj.data.id))
				rtn.add(obj);

		return rtn;
	}
	
	public List<GameUnit> getUnits() {
		return units;
	}

	public ArrayList<GameUnit> getPlayerUnitsOfType(Player p, String... id) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : units)
			if (obj.owner == p && Arrays.asList(id).contains(obj.data.id))
				rtn.add(obj);

		return rtn;
	}
	
	public List<GameUnit> getUnitsOnTile(WalkableTile t) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : units)
			if (t.mapX == obj.mapX && t.mapY == obj.mapY)
				rtn.add(obj);

		return rtn;
	}
	
	public List<GameUnit> getPlayerUnitsOnTile(WalkableTile t, Player p) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : getPlayerUnits(p))
			if (t.mapX == obj.mapX && t.mapY == obj.mapY)
				rtn.add(obj);

		return rtn;
	}
	
	public List<GameUnit> getUnitsOnAndAroundTile(WalkableTile t) {
		ArrayList<GameUnit> rtn = new ArrayList<GameUnit>();

		for (GameUnit obj : units)
			if (Math.abs(t.mapX - obj.mapX) <= 1 && Math.abs(t.mapY - obj.mapY) <= 1)
				rtn.add(obj);

		return rtn;
	}
	
	public int getPlayerStratLocScore(Player p) {
		int score = 0;
		for(WalkableTile wt: stratLocValues.keySet()) {
			for(GameUnit u: getPlayerUnits(p)) {
				if(u.mapX == wt.mapX && u.mapY == wt.mapY) {
					score++;
					break;
				}
			}
		}
		return score;
	}
	
	public boolean isOnStratLoc(GameUnit unit) {
		return stratLocValues.containsKey(getTileAt(unit));
	}
	
	public WalkableTile getTileAt(CoordObject key) {
		return walkableMap.get(key);
	}
	
	public GameStaticObject getObjBelowUnit(GameUnit u) {
		return staticObjects.get(u);
	}
	
	public Collection<WallTile> getUnwalkableMap() {
		return unwalkableMap.values();
	}
	
	public Collection<WalkableTile> getWalkableMap() {
		return walkableMap.values();
	}
	
	public Collection<GameStaticObject> getStaticObjects() {
		return staticObjects.values();
	}
	
	public void addStaticObj(GameStaticObject obj) {
		walkableMap.put(obj, obj);
		staticObjects.put(obj, obj);
	}
	
	public GameStaticObject getStaticObj(CoordObject key) {
		return staticObjects.get(key);
	}
	
	public void removeStaticObj(GameStaticObject obj) {
		staticObjects.remove(new CoordObject(obj.mapX, obj.mapY));
	}
	
	public void removeUnit(GameUnit unit) {
		units.remove(unit);
	}

	public void addUnit(GameUnit unit) {
		units.add(unit);
	}
	
	public int getDistBetween(CoordObject obj1, CoordObject obj2, Player p) {
		List<WalkableTile> path = findPath(obj1, obj2, p);
		if (path != null) {
			return path.size();
		}
		return -1;
	}
	
	public int getDistBetween(CoordObject obj1, CoordObject obj2) {
		List<WalkableTile> path = findPath(obj1, obj2);
		if (path != null) {
			return path.size();
		}
		return -1;
	}
	
	public int getTileResourceScore(WalkableTile wt, Player p) {
		int sum = 0;
		for(int i = 0; i < nextPos.length; i++) {
			GameStaticObject so = staticObjects.get(wt);
			if(so != null) {
				if(so.data.id.equals("MINE"))
					sum++;
				else if(so.data.id.equals("METAL"))
					sum++;
			}
		}
		return sum;
	}

	public GameStaticObject getClosestCity(Player agent, Player target) {
		int smallestDist = Integer.MAX_VALUE;
		GameStaticObject closest = null;

		for (GameStaticObject c1 : getPlayerCities(agent)) {
			for (GameStaticObject c2 : getPlayerCities(target)) {
				List<WalkableTile> path = findPath(c1, c2, agent);
				if (path != null) {
					int dist = path.size();
					if (dist < smallestDist) {
						smallestDist = dist;
						closest = c2;
					}
				}
			}
		}

		return closest;
	}
	
	public int getDistToClosestCity(CoordObject obj, Player p) {
		int smallestDist = Integer.MAX_VALUE;

		for (GameStaticObject so : getPlayerCities(p)) {
			List<WalkableTile> path = findPath(obj, so, p);
			if (path != null) {
				int dist = path.size();
				if (dist < smallestDist) {
					smallestDist = dist;
				}
			}
		}

		return smallestDist;
	}
}
