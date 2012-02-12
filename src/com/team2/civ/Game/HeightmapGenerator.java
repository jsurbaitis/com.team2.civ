package com.team2.civ.Game;

import java.util.Random;

public class HeightmapGenerator {

	public static int[][] generateMap(int width, int height) {
		int[][] map = new int[width][height];

		int MAX_DIST = (int) (dist(width / 2, height / 2, 0, 0) * 0.7);

		Random rnd = new Random();

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

		int[][] nextPos = { { -1, 1 }, { 0, 1 }, { 1, 0 }, { 1, -1 },
				{ 0, -1 }, { -1, 0 } };
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				if (map[i][j] == 0) {
					int count = 0;
					for (int z = 0; z < nextPos.length; z++)
						if (map[i + nextPos[z][0]][j + nextPos[z][1]] == 0
								|| map[i + nextPos[z][0]][j + nextPos[z][1]] == -1)
							count++;

					//if (count == 6)
					//	map[i][j] = -1;
				}
			}
		}

		map[width / 2][height / 2] = 1;

		return map;
	}

	private static int dist(int x1, int y1, int x2, int y2) {
		int dx = Math.abs(x1 - x2);
		int dy = Math.abs(y1 - y2);
		return dx * dx + dy * dy;
	}
}
