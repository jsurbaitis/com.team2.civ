package com.team2.civ.Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.MapObject;
import com.team2.civ.Map.MapObjectImage;
import com.team2.civ.Map.PathNode;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;
import com.team2.civ.UI.UI;

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
	
	private HashMap<CoordObject, WallTile> unwalkableMap = new HashMap<CoordObject, WallTile>();
	private HashMap<CoordObject, WalkableTile> walkableMap = new HashMap<CoordObject, WalkableTile>();
	
	private HashMap<CoordObject, GameUnit> units = new HashMap<CoordObject, GameUnit>();
	private HashMap<CoordObject, GameStaticObject> staticObjects = new HashMap<CoordObject, GameStaticObject>();
	
	private HashMap<CoordObject, MapObjectImage> highDraw = new HashMap<CoordObject, MapObjectImage>();
	private HashMap<CoordObject, MapObjectImage> lowDraw = new HashMap<CoordObject, MapObjectImage>();

	public List<Player> players = new ArrayList<Player>();
	private Player currentPlayer;
	private MapObject target;
	
	public int turnCount = 0;
	public static final int MAX_TURNS = 500;

	public GameController(GraphicsConfiguration config) {
		res = new Resources(config);
		ui = new UI(res);
		
		createMap();
	}
	
	private void createMap() {
		BufferedImage wallImg = null;
		BufferedImage waterImg = null;
		BufferedImage tileImg = null;
		BufferedImage moveImg = null;
		BufferedImage hillImg = null;
		try {
			MapObjectImage.highlightImg = res.getImage("highlight");
			MapObjectImage.selectedImg = res.getImage("selected");
			tileImg = res.getImage("tile_grass");
			moveImg = res.getImage("move_test");
			wallImg = res.getImage("wall");
			waterImg = res.getImage("water");
			hillImg = res.getImage("hill");
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		int[][] map = HeightmapGenerator.generateMap(30, 30);
		for(int x = 0; x < 30; x++) {
			for(int y = 0; y < 30; y++) {
				if(map[x][y] == -1)
					continue;
				if(map[x][y] == 0) {
					WallTile wt = new WallTile(x, y, waterImg);
					unwalkableMap.put(wt, wt);
					highDraw.put(wt, wt.getImage());
				} else if(map[x][y] == 1) {
					WalkableTile t = new WalkableTile(x, y, tileImg, null);
					walkableMap.put(t, t);
					lowDraw.put(t, t.getImage());
				} else if(map[x][y] == 2) {
					WalkableTile t = new WalkableTile(x, y, hillImg, null);
					walkableMap.put(t, t);
					lowDraw.put(t, t.getImage());
				} else {
					WallTile wt = new WallTile(x, y, wallImg);
					unwalkableMap.put(wt, wt);
					highDraw.put(wt, wt.getImage());
				}
			}
		}
		
		players.add(new Player("P1", "#FFFFFF", null));

		try {
			GameUnit test = new GameUnit(15, 15, moveImg, players.get(0), res.getUnit("WORKER"));
			units.put(test, test);
			highDraw.put(test, test.getImage());
			
			offsetX = -test.x;
			offsetY = -test.y;
			
			GameUnit test1 = new GameUnit(18, 18, moveImg, players.get(0), res.getUnit("WORKER"));
			units.put(test1, test1);
			highDraw.put(test1, test1.getImage());
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		
		currentPlayer = players.get(0);
	}

	public void update(long timeElapsedMillis) {
		gameTime++;
		
		ui.update(gameTime);
		
		if(zoomingIn || zoomingOut) {
			double os = scale;
			
			if(zoomingIn) {		
				scale += ZOOM_FACTOR;	
			} else {
				scale -= ZOOM_FACTOR;
			}
			
			offsetX += ((float)Team2Civ.WINDOW_WIDTH) * (os - scale) / 2 * (1/scale);
			offsetY += ((float)Team2Civ.WINDOW_HEIGHT) * (os - scale) / 2 * (1/scale);
			
			System.out.println(""+(((float)Team2Civ.WINDOW_WIDTH) * (os - scale) / 2 * (1/os)));

			if(Math.abs(oldScale - scale) >= ZOOM_DELTA) {
				zoomingIn = false;
				zoomingOut = false;
			}
		}
		
		for(GameUnit u: units.values())
			u.update(gameTime, walkableMap);
	}
	
	public HashMap<CoordObject, WalkableTile> getWalkableTilesCopy() {
		return new HashMap<CoordObject, WalkableTile>(walkableMap);
	}
	
	private void onTarget(MapObject newTarget) {
		
	}
	
	private void addUnit(Player p, GameUnit u) {
		
	}
	
	public void performAction(GameAction action, MapObject actor) {
		
	}
	
	public void addUnitToPlayer(String unitDataTag, Player p) {
		
	}
	
	public void addStaticObjectToPlayer(String objectDataTag, Player p) {
		
	}

	public  void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
        g.fillRect(0, 0, Team2Civ.WINDOW_WIDTH, Team2Civ.WINDOW_HEIGHT);

        g.scale(scale, scale);

        for(MapObjectImage i: lowDraw.values())
        	i.draw(g, (int)(offsetX), (int)(offsetY), scale);

        TreeMap<CoordObject, MapObjectImage> temp = new TreeMap<CoordObject, MapObjectImage>(highDraw);
        for(MapObjectImage i: temp.values())
        	i.draw(g, (int)(offsetX), (int)(offsetY), scale);

        g.scale(1/scale, 1/scale);

        ui.draw(g);
	}
	
	private void zoomIn() {
		if(scale < SCALE_MAX) {
			oldScale = scale;
			zoomingIn = true;
		}
	}
	
	private void zoomOut() {
		if(scale > SCALE_MIN) {
			oldScale = scale;
			zoomingOut = true;
		}
	}
	
	public void onMouseInput(MouseEvent ev) {
		if(ev.getID() == MouseEvent.MOUSE_PRESSED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
			
			pressStartX = lastMouseX;
			pressStartY = lastMouseY;
			
			if(SwingUtilities.isLeftMouseButton(ev))
				leftClick = true;
			else
				leftClick = false;
		}
		else if(ev.getID() == MouseEvent.MOUSE_DRAGGED) {
			offsetX += (ev.getX() - lastMouseX)*(1/scale);
			offsetY += (ev.getY() - lastMouseY)*(1/scale);
			
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
		} else if(ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick && target != null && target instanceof GameUnit) {
			GameUnit movingUnit = (GameUnit) target;
			if(movingUnit.owner == currentPlayer && Math.abs(ev.getX() - pressStartX) < 5 && Math.abs(ev.getY() - pressStartY) < 5) {
				for(WalkableTile t: walkableMap.values()) {
					if(t.picked((int)(ev.getX()*(1/scale)-offsetX), (int)(ev.getY()*(1/scale)-offsetY))) {
						movingUnit.startMovement(findPath(movingUnit, t, true, -1));
					}
				}
			}
		} else if(ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if(Math.abs(ev.getX() - pressStartX) < 5 && Math.abs(ev.getY() - pressStartY) < 5) {
				for(GameUnit u: units.values())
					if(u.picked((int)(ev.getX()*(1/scale)-offsetX), (int)(ev.getY()*(1/scale)-offsetY))) {
						if(target != null)
							target.selected = false;
						target = u;
						target.selected = true;
						ui.showUnitInfo((GameUnit)target);
					}
				
				for(GameStaticObject so: staticObjects.values())
					if(so.picked((int)(ev.getX()*(1/scale)-offsetX), (int)(ev.getY()*(1/scale)-offsetY))) {
						if(target != null)
							target.selected = false;
						target = so;
						target.selected = true;
						
					}
			}
		}
	}
	
	public void onMouseWheelInput(MouseWheelEvent ev) {
		if(!zoomingIn && !zoomingOut) {
			if(ev.getWheelRotation() < 0)
				zoomIn();
			else if(ev.getWheelRotation() > 0)
				zoomOut();
		}
	}
	
	public void onKeyboardInput(KeyEvent ev) {
		if(ev.getID() == KeyEvent.KEY_PRESSED) {
			if(ev.getKeyCode() == KeyEvent.VK_UP)
				offsetY += 12;
			if(ev.getKeyCode() == KeyEvent.VK_DOWN)
				offsetY -= 12;
			if(ev.getKeyCode() == KeyEvent.VK_LEFT)
				offsetX += 12;
			if(ev.getKeyCode() == KeyEvent.VK_RIGHT)
				offsetX -= 12;
		}
	}
	
	public ArrayList<WalkableTile> findPath(GameUnit startObj, CoordObject targetObj, boolean walkOnTarget, int lengthLimit)
	{
		HashMap<CoordObject, PathNode> nodeList = new HashMap<CoordObject, PathNode>();
		ArrayList<PathNode> openList = new ArrayList<PathNode>();
		ArrayList<PathNode> closedList = new ArrayList<PathNode>();
		ArrayList<WalkableTile> returnList = new ArrayList<WalkableTile>();
		
		//ISO
		//int[][] nextPos = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		//HEX
		int[][] nextPos = {{-1, 1}, {0, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, 0}};

		for(WalkableTile tile: walkableMap.values()) {
			GameUnit u = units.get(tile);
			if(u == null || u.owner == startObj.owner)
				nodeList.put(tile, new PathNode(tile.mapX, tile.mapY));
		}
		nodeList.put(startObj, new PathNode(startObj.mapX, startObj.mapY));
		
		boolean nextToTarget = true;
		if(walkOnTarget)
			nextToTarget = false;
		
		PathNode target = null;
		PathNode nextTile = null;
		PathNode minTile = null;
		PathNode current = nodeList.get(startObj);
		current.heuristic = getHeuristic(current.mapX, current.mapY, targetObj.mapX, targetObj.mapY);
		current.cost = 0;
		current.parent = null;
		openList.add(current);
		
		while(openList.size() != 0) {
			if(!nextToTarget) {
				if(current.mapX == targetObj.mapX && current.mapY == targetObj.mapY) {
					target = current;
					break;
				}
			} else {
				if(Math.abs(current.mapX - targetObj.mapX) < 2 && Math.abs(current.mapY - targetObj.mapY) < 2) {
					target = current;
					break;
				}
			}
			closedList.add(current);
			openList.remove(current);

			for(int i = 0; i < nextPos.length; i++) {
				nextTile = nodeList.get(new CoordObject(current.mapX + nextPos[i][0], current.mapY + nextPos[i][1]));
				if(nextTile == null || closedList.contains(nextTile))
					continue;
				
				int tentativeScore = current.cost + getTentativeScore(current.mapX, current.mapY, nextTile.mapX, nextTile.mapY);
				
				boolean isBetter = false;
				if(!openList.contains(nextTile)) {
					nextTile.heuristic = getHeuristic(nextTile.mapX, nextTile.mapY, targetObj.mapX, targetObj.mapY);
					openList.add(nextTile);
					isBetter = true;
				} else if(tentativeScore < nextTile.cost)
					isBetter = true;

				if(isBetter) {
					nextTile.parent = current;
					nextTile.cost = tentativeScore;
				}
			}
			
			minTile = null;
			for(int i = 0; i < openList.size(); i++) {
				if(minTile == null || minTile.cost + minTile.heuristic > openList.get(i).cost + openList.get(i).heuristic)
					minTile = openList.get(i);
			}
			
			current = minTile;
		}

		if(target == null) {
			return null;
		}
		
		if(lengthLimit == -1) {
			while(target != null) {
				returnList.add(walkableMap.get(new CoordObject(target.mapX, target.mapY)));
				target = target.parent;
			}
		} else {
			int length = 0;
			PathNode tempTarget = target;
			while(tempTarget != null) {
				tempTarget = tempTarget.parent;
				length++;
			}
			length -= lengthLimit;
			while(target != null) {
				if(length < 1)
					returnList.add(walkableMap.get(new CoordObject(target.mapX, target.mapY)));
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
		
		int result = (dx*dx)+(dy*dy);
		return result;
	}
	
}
