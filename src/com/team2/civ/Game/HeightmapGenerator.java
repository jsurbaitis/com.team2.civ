package com.team2.civ.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.PathNode;

public class HeightmapGenerator {
	private static HashMap<CoordObject, PathNode> nodeList;
	private static boolean nodeListIncludesWater = false;
	
	private static final int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 },
			{ 1, -1 }, { 0, -1 }, { -1, 0 } };
	private static int[][] map;
	private static int citiesToPlace;

	public static int[][] generateMap(int width, int height, int cityCount) {
		citiesToPlace = cityCount;

		generateHeightMap(width, height);

		Random rnd = new Random();

		// ADD SOME RESOURCES
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (map[i][j] == 2 && rnd.nextFloat() < 0.3)
					map[i][j] = 4;
			}
		}

		// ADD SOME CITIES
		addCities();

		return map;
	}

	private static void addCities() {
		ArrayList<int[]> tilesByWeight = getTilesByWeight();
		ArrayList<int[]> citiesPlaced = new ArrayList<int[]>();

		int tileIndex = 0;
		while (citiesPlaced.size() < citiesToPlace
				&& tileIndex < tilesByWeight.size()) {
			int[] coords = tilesByWeight.get(tileIndex);
			if (canPlaceCity(coords[0], coords[1], citiesPlaced)) {
				citiesPlaced.add(coords);
				map[coords[0]][coords[1]] = 5;

			}
			tileIndex++;
		}
	}

	private static boolean canPlaceCity(int i, int j,
			ArrayList<int[]> citiesPlaced) {
		for (int[] cityCoords : citiesPlaced) {
			ArrayList<int[]> path = findPath(i, j, cityCoords[0], cityCoords[1], false);
			if (path == null || path.size() < 18)
				return false;
		}

		return true;
	}

	private static ArrayList<int[]> getTilesByWeight() {
		int[][] weights = getTileWeights();
		ArrayList<int[]> tilesByWeight = new ArrayList<int[]>();

		int[] coords = { 0, 0 };
		coords = getHighestWeight(weights);
		while (coords[0] != -1) {
			weights[coords[0]][coords[1]] = -1;
			tilesByWeight.add(coords);
			coords = getHighestWeight(weights);
		}

		return tilesByWeight;
	}

	private static int[] getHighestWeight(int[][] weights) {
		int maxX = -1, maxY = -1;
		int maxWeight = Integer.MIN_VALUE;

		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[0].length; j++) {
				if (weights[i][j] != -1 && weights[i][j] > maxWeight) {
					maxX = i;
					maxY = j;
					maxWeight = weights[i][j];
				}
			}
		}

		int[] maxCoords = { maxX, maxY };
		return maxCoords;
	}

	private static int[][] getTileWeights() {
		int[][] weights = new int[map.length][map[0].length];

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j] == 0 || map[i][j] == 3)
					weights[i][j] = -1;
				else
					weights[i][j] = getTileWeight(i, j);
			}
		}
		return weights;
	}

	private static int getTileWeight(int i, int j) {
		int weight = 0;
		for (int m = 1; m <= 3; m++) {
			for (int z = 0; z < nextPos.length; z++) {
				int x = i + nextPos[z][0] * m;
				int y = j + nextPos[z][1] * m;
				if (goodCoords(x, y) && map[x][y] == 4)
					weight += 6 / m;
			}
		}
		return weight;
	}

	private static boolean goodCoords(int x, int y) {
		if (x > 0 && y > 0 && x < map.length && y < map[0].length)
			return true;
		return false;
	}

	private static void generateHeightMap(int width, int height) {
		map = new int[width][height];

		int MAX_DIST = (int) (dist(width / 2, height / 2, 0, 0) * 0.7);

		Random rnd = new Random();

		// CREATE MAIN LANDMASS AND MOUNTAINS
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (dist(i, j, width / 2, height / 2) * 1.5 < rnd
						.nextInt(MAX_DIST))
					if (rnd.nextFloat() < 0.3)
						map[i][j] = 3;
					else
						map[i][j] = 1;
				else if (dist(i, j, width / 2, height / 2) * 2 > rnd
						.nextInt(MAX_DIST))
					map[i][j] = 0;
				else
					map[i][j] = 1;
			}
		}

		// DO PASSES TO ADD WATER AND REDUCE THE FEELING OF A CIRCULAR ISLAND
		for (int z = 0; z < 3; z++) {
			int rndx = rnd.nextInt(width);
			int rndy = rnd.nextInt(height);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (dist(i, j, rndx, rndy) * 18 < rnd.nextInt(MAX_DIST))
						map[i][j] = 0;
				}
			}
		}

		// REMOVE WATER ON THE EDGES
		int[] toCount = { 0, -1 };
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (map[i][j] == 0) {
					int count = 0;
					for (int z = 0; z < nextPos.length; z++)
						if (getTileCountAround(i + nextPos[z][0], j
								+ nextPos[z][1], toCount, true) == 6)
							count++;

					if (count == 6)
						map[i][j] = -1;
				}
			}
		}
		
		ArrayList<int[]> riverSources = new ArrayList<int[]>();

		// REMOVE WATER IN THE MIDDLE OF LANDMASS, ADD HILLS
		int[] toCountWaterRemove = { 1, 2, 3 };
		int[] toCountHillAdd = { 3 };
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (map[i][j] == 0) {
					if (getTileCountAround(i, j, toCountWaterRemove,false) >= 4 
							&& getTileCountAround(i, j, toCountHillAdd, false) < 2)
						map[i][j] = 1;
					else {
						int[] toAdd = { i, j };
						riverSources.add(toAdd);
					}
				} else if (map[i][j] == 1) {
					if (getTileCountAround(i, j, toCountHillAdd, false) == 2)
						map[i][j] = 2;
				}
			}
		}
		
		// ADD RIVERS
		for(int[] arr: riverSources) {
			int[] closestWater = getClosestWater(arr[0], arr[1]);
			ArrayList<int[]> path = findPath(arr[0], arr[1], closestWater[0], closestWater[1], true);
			if(path != null) {
				for(int[] tile: path) {
					map[tile[0]][tile[1]] = 0;
				}
			}
		}

		map[width / 2][height / 2] = 1;
	}
	
	private static int[] getClosestWater(int x, int y) {
		int[] closest = {x, y};
		int dist = Integer.MAX_VALUE;
		int tempDist;
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if(i != x && j != y && map[i][j] == 0) {
					tempDist = dist(x, y, i, j);
					if(tempDist < dist) {
						closest[0] = i;
						closest[1] = j;
						dist = tempDist;
					}
				}
			}
		}
		
		return closest;
	}

	private static int getTileCountAround(int x, int y, int toCount[],
			boolean countNone) {
		int count = 0;
		for (int z = 0; z < nextPos.length; z++) {
			if (x + nextPos[z][0] > 0 && x + nextPos[z][0] < map.length
					&& y + nextPos[z][1] > 0
					&& y + nextPos[z][1] < map[0].length) {
				for (int i = 0; i < toCount.length; i++) {
					if (map[x + nextPos[z][0]][y + nextPos[z][1]] == toCount[i]) {
						count++;
						break;
					}
				}
			} else {
				if (countNone)
					count++;
			}
		}

		return count;
	}

	private static int dist(int x1, int y1, int x2, int y2) {
		int dx = Math.abs(x1 - x2);
		int dy = Math.abs(y1 - y2);
		return dx * dx + dy * dy;
	}

	private static ArrayList<int[]> findPath(int startX, int startY, int endX,
			int endY, boolean includeWater) {
		ArrayList<PathNode> openList = new ArrayList<PathNode>();
		ArrayList<PathNode> closedList = new ArrayList<PathNode>();
		ArrayList<int[]> returnList = new ArrayList<int[]>();

		int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 }, { 1, -1 },
				{ 0, -1 }, { -1, 0 } };

		if (nodeList == null || includeWater != nodeListIncludesWater) {
			nodeListIncludesWater = includeWater;
			buildNodeList(map, includeWater);
		}

		CoordObject targetObj = new CoordObject(endX, endY);
		PathNode target = null;
		PathNode nextTile = null;
		PathNode minTile = null;
		PathNode current = nodeList.get(new CoordObject(startX, startY));
		current.heuristic = getHeuristic(current.mapX, current.mapY,
				targetObj.mapX, targetObj.mapY);
		current.cost = 0;
		current.parent = null;
		openList.add(current);

		while (openList.size() != 0) {
			if (current.mapX == targetObj.mapX
					&& current.mapY == targetObj.mapY) {
				target = current;
				break;
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
			int[] toAdd = { target.mapX, target.mapY };
			returnList.add(toAdd);
			target = target.parent;
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

	private static void buildNodeList(int[][] map, boolean includeWater) {
		nodeList = new HashMap<CoordObject, PathNode>();
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if ((includeWater || map[i][j] != 0) && map[i][j] != 3)
					nodeList.put(new CoordObject(i, j), new PathNode(i, j));
			}
		}
	}
}
