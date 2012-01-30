package com.team2.civ.Map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import com.team2.civ.Data.AnimData;

public class MovingMapObject extends MapObject {
	
	protected float speedX = 0;
	protected float speedY = 0;
	protected boolean isMoving = false;
	
	protected MapObject target;
	
	private Animation movingAnim;

	protected List<WalkableTile> path;
	
	public MovingMapObject(int mapX, int mapY, BufferedImage bitmap) {
		super(mapX, mapY, bitmap);
		
		//TEMPORARY ANIM STUFF
		BufferedImage frames[] = new BufferedImage[7];
		try {
			frames[0] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_0.png"));
			frames[1] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_1.png"));
			frames[2] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_2.png"));
			frames[3] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_3.png"));
			frames[4] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_4.png"));
			frames[5] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_5.png"));
			frames[6] = ImageIO.read(new File("assets/move_test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		AnimData a = new AnimData();
		a.length = 7;
		a.loopCount = -1;
		a.frames = frames;
		
		movingAnim = new Animation(a);
	}
	
	public void update(long gameTime, HashMap<CoordObject, WalkableTile> map) {
		if(isMoving) {
			movingAnim.update(img, gameTime);
			updateMovement(map);
		}
	}

	public void startMovement(HashMap<CoordObject, WalkableTile> map, CoordObject target, 
			 				  boolean walkOnTarget, int lengthLimit) {
		if(!isMoving) {
			path = findPath(map, target, walkOnTarget, lengthLimit);
			if(path != null) {
				movingAnim.reset();
				
				for(WalkableTile wt : path)
					wt.highlighted = true;
				
				speedX = 0;
				speedY = 0;
				isMoving = true;
			}
		}
	}
	
	private void updateMovement(HashMap<CoordObject, WalkableTile> map) {
		if(speedX == 0 && speedY == 0) {
			map.get(this).occupied = false; // not sure about this line
			
			setPos(path.get(path.size() - 1).mapX, path.get(path.size() - 1).mapY);

			path.get(path.size() - 1).highlighted = false;
			path.remove(path.size() - 1);
			
			if(path.size() > 0) {	
				target = path.get(path.size() - 1);
				determineSpeed();
			}
			else {
				map.get(this).occupied = true;
				isMoving = false;
				img.resetImg();
			}
		}
		else
		{
			//SET TO 1 FOR ISO
			if(Math.abs(x - target.x) < 4)
				speedX = 0;
			else
				x += speedX;
			
			if(Math.abs(y - target.y) < 4)
				speedY = 0;
			else
				y += speedY;
		}
	}
	
	private void determineSpeed() {
		//ISO
		/*if(x < target.x)
			speedX = 4f;
		else
			speedX = -4f;
		
		if(y < target.y)
			speedY = 2f;
		else
			speedY = -2f;*/
		
		//HEX (should work for iso as well)
		int dx = x - target.x;
		int dy = y - target.y;
		
		if(dx == 0) {
			if(dy < 0)
				speedY = 4f;
			else
				speedY = -4f;
			
			speedX = 0;
		} else if(dy == 0) {
			if(dx < 0)
				speedX = 4f;
			else
				speedX = -4f;
			
			speedY = 0;
		} else if(Math.abs(dx) > Math.abs(dy)) {
			if(dy < 0)
				speedY = 2f;
			else
				speedY = -2f;

			speedX = - dx / Math.abs(dy) * 2;
		} else {
			if(dx < 0)
				speedX = 2f;
			else
				speedX = -2f;

			speedY = - dy / Math.abs(dx) * 2;
		}
	}
	
	private ArrayList<WalkableTile> findPath(HashMap<CoordObject, WalkableTile> walkableMap, CoordObject targetObj, 
											 boolean walkOnTarget, int lengthLimit)
	{
		//for(int i = 0; i < returnList.size(); i++) {
		//	returnList.get(i).inRange = false;
		//}
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
		nodeList.put(this, new PathNode(mapX, mapY));
		
		boolean nextToTarget = true;
		if(walkOnTarget)
			nextToTarget = false;
		else if(!walkableMap.get(targetObj).occupied)
				nextToTarget = false;
		
		PathNode target = null;
		PathNode nextTile = null;
		PathNode minTile = null;
		PathNode current = nodeList.get(this);
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
	
	private int getTentativeScore(int x, int y, int tx, int ty) {
		return Math.abs(x - tx) + Math.abs(y - ty);
	}
	
	private int getHeuristic(int x, int y, int tx, int ty) {		
		int dx = tx - x;
		int dy = ty - y;
		
		int result = (dx*dx)+(dy*dy);
		return result;
	}
}