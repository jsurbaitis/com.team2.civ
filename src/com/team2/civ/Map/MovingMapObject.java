package com.team2.civ.Map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import com.team2.civ.Data.AnimData;
import com.team2.civ.Game.GameController;

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
			path = GameController.findPath(map, this, target, walkOnTarget, lengthLimit);
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
}