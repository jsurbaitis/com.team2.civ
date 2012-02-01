package com.team2.civ.Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.MapObject;
import com.team2.civ.Map.MapObjectImage;
import com.team2.civ.Map.MovingMapObject;
import com.team2.civ.Map.PathNode;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class GameController {
	public static int TILE_WIDTH, TILE_HEIGHT;

	private long gameTime = 0;

	private int offsetX = 0;
	private int offsetY = 0;
	
	private int pressStartX;
	private int pressStartY;
	
	private int lastMouseX;
	private int lastMouseY;
	
	private static final double SCALE_MAX = 1.0;
	private static final double SCALE_MIN = 0.4;
	private static final double ZOOM_DELTA = 0.2;
	private static final double ZOOM_FACTOR = 0.02;
	
	private double scale = 1.0f;
	private double oldScale = scale;
	private boolean zoomingIn = false;
	private boolean zoomingOut = false;

	private Resources res;
	
	private HashMap<CoordObject, WalkableTile> walkableMap = new HashMap<CoordObject, WalkableTile>();
	
	private HashMap<CoordObject, MapObjectImage> highDraw = new HashMap<CoordObject, MapObjectImage>();
	private HashMap<CoordObject, MapObjectImage> lowDraw = new HashMap<CoordObject, MapObjectImage>();

	public List<Player> players = new ArrayList<Player>();
	private Player currentPlayer;
	private MapObject target;

	private MovingMapObject test;

	public GameController(GraphicsConfiguration config) {
		//BS map creation
		MapObjectImage.highlightImg = Resources.get(new File("assets/highlight.png"), config);
		BufferedImage tileImg = Resources.get(new File("assets/empty_tile.png"), config);
		BufferedImage moveImg = Resources.get(new File("assets/move_test.png"), config);
		BufferedImage wallImg = Resources.get(new File("assets/wall.png"), config);
		
		TILE_WIDTH = tileImg.getWidth();
		TILE_HEIGHT = tileImg.getHeight();
		
		Random rnd = new Random();
		
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				int r = rnd.nextInt(4);
				if(!(i == 0 && j == 0) && r == 0) {
					WallTile wt = new WallTile(i, j, wallImg);
					highDraw.put(wt, wt.getImage());
				} else {
					WalkableTile t = new WalkableTile(i, j, tileImg);
					walkableMap.put(t, t);
					lowDraw.put(t, t.getImage());
				}
			}
		}
		
		test = new MovingMapObject(0, 0, moveImg);
		highDraw.put(test, test.getImage());
	}

	public void update(long timeElapsedMillis) {
		gameTime++;
		
		if(zoomingIn || zoomingOut) {
			double os = scale;
			
			if(zoomingIn) {		
				scale += ZOOM_FACTOR;	
			} else {
				scale -= ZOOM_FACTOR;
			}

			offsetX += Team2Civ.WINDOW_WIDTH * (os - scale) / 2;
			offsetY += Team2Civ.WINDOW_HEIGHT * (os - scale) / 2;
			
			if(Math.abs(oldScale - scale) >= ZOOM_DELTA) {
				zoomingIn = false;
				zoomingOut = false;
			}
		}
		
		test.update(gameTime, walkableMap);
	}
	
	public HashMap<CoordObject, WalkableTile> getWalkableTilesCopy() {
		return new HashMap<CoordObject, WalkableTile>(walkableMap);
	}
	
	private void onTarget(MapObject newTarget) {
		
	}
	
	private void addPlayer(Player p) {
		
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
        
        //g.scale(scale, scale);

        for(MapObjectImage i: lowDraw.values())
        	i.draw(g, (int)(offsetX*(1/scale)), (int)(offsetY*(1/scale)), scale);

        TreeMap<CoordObject, MapObjectImage> temp = new TreeMap<CoordObject, MapObjectImage>(highDraw);
        for(MapObjectImage i: temp.values())
        	i.draw(g, (int)(offsetX*(1/scale)), (int)(offsetY*(1/scale)), scale);
        
        //g.scale(1/scale, 1/scale);
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
		}
		else if(ev.getID() == MouseEvent.MOUSE_DRAGGED) {
			offsetX += ev.getX() - lastMouseX;
			offsetY += ev.getY() - lastMouseY;
			
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
		} else if(ev.getID() == MouseEvent.MOUSE_RELEASED) {
			if(Math.abs(ev.getX() - pressStartX) < 5 && Math.abs(ev.getY() - pressStartY) < 5) {
				for(WalkableTile t: walkableMap.values()) {
					if(!t.occupied && t.picked((int)((ev.getX() - offsetX)*(1/scale)), (int)((ev.getY() - offsetY)*(1/scale)))) {
						test.startMovement(walkableMap, t, true, -1);
					}
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
	
	public static ArrayList<WalkableTile> findPath(HashMap<CoordObject, WalkableTile> walkableMap, CoordObject startObj,
			CoordObject targetObj, boolean walkOnTarget, int lengthLimit)
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
			if(!tile.occupied) {
				nodeList.put(tile, new PathNode(tile.mapX, tile.mapY));
			}
		}
		nodeList.put(startObj, new PathNode(startObj.mapX, startObj.mapY));
		
		boolean nextToTarget = true;
		if(walkOnTarget)
			nextToTarget = false;
		else if(!walkableMap.get(targetObj).occupied)
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
