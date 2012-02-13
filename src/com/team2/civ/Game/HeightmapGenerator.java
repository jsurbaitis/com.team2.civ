package com.team2.civ.Game;

import java.util.Random;

public class HeightmapGenerator {
	private static int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, 0 } };

	public static int[][] generateMap(int width, int height) {
		int[][] map = new int[width][height];

		int MAX_DIST = (int) (dist(width / 2, height / 2, 0, 0) * 0.7);

		Random rnd = new Random();

		//CREATE MAIN LANDMASS AND MOUNTAINS
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (dist(i, j, width / 2, height / 2) * 1.5 < rnd.nextInt(MAX_DIST))
					if (rnd.nextFloat() < 0.3)
						map[i][j] = 3;
					else
						map[i][j] = 1;
				else if (dist(i, j, width / 2, height / 2) * 2 > rnd.nextInt(MAX_DIST))
					map[i][j] = 0;
				else
					map[i][j] = 1;
			}
		}

		//REMOVE WATER ON THE EDGES
		int[] toCount = {0, -1};
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (map[i][j] == 0) {
					int count = 0;
					for (int z = 0; z < nextPos.length; z++)
						if(getTileCountAround(map, i + nextPos[z][0], j + nextPos[z][1], toCount, true) == 6)
							count++;

					if (count == 6)
						map[i][j] = -1;
				}
			}
		}
		
		//REMOVE WATER IN THE MIDDLE OF LANDMASS, ADD HILLS (6 passes)
		int[] toCountWaterRemove = {1, 2, 3};
		int[] toCountHillAdd = {3};
		for(int p = 0; p < 6; p++) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if(map[i][j] == 0) {
						if(getTileCountAround(map, i, j, toCountWaterRemove, false) == 4)
							map[i][j] = 1;
					} else if(map[i][j] == 1) {
						if(getTileCountAround(map, i, j, toCountHillAdd, false) == 2)
							map[i][j] = 2;
					}
				}
			}
		}

		map[width / 2][height / 2] = 1;

		return map;
	}
	
	private static int getTileCountAround(int[][] map, int x, int y, int toCount[], boolean countNone) {
		int count = 0;
		for (int z = 0; z < nextPos.length; z++) {
			if(x + nextPos[z][0] > 0 && x + nextPos[z][0] < map.length &&
					y + nextPos[z][1] > 0 && y + nextPos[z][1] < map[0].length) {
				for(int i = 0; i < toCount.length; i++) {
					if(map[x + nextPos[z][0]][y + nextPos[z][1]] == toCount[i]) {
						count++;
						break;
					}
				}	
			} else {
				if(countNone)
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
}
