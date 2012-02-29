package com.team2.civ.Map;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.Player;

public class MovingMapObject extends MapObject {
	protected float speedX = 0;
	protected float speedY = 0;
	protected boolean isMoving = false;
	
	protected MapObject target;
	
	private BufferedImage orientations[];
	//private Animation movingAnim;

	protected List<WalkableTile> path;
	
	public MovingMapObject(int mapX, int mapY, String imgId, Resources res, Player owner) throws ResNotFoundException {
		super(mapX, mapY, res.getImage(imgId+"_0"), owner);
		
		orientations = new BufferedImage[6];
		for(int i = 0; i < 6; i++)
			orientations[i] = res.getImage(imgId+"_"+i);
		
		/*BufferedImage frames[] = new BufferedImage[7];
		try {
			frames[0] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_0.png"));
			frames[1] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_1.png"));
			frames[2] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_2.png"));
			frames[3] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_3.png"));
			frames[4] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_4.png"));
			frames[5] = ImageIO.read(new File("assets/enemy_s_torusknot_walk_5.png"));
			frames[6] = ImageIO.read(new File("assets/WORKER.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		AnimData a = new AnimData();
		a.length = 7;
		a.loopCount = -1;
		a.frames = frames;
		
		movingAnim = new Animation(a);*/
	}
	
	public void update(long gameTime, HashMap<CoordObject, WalkableTile> map) {
		if(isMoving) {
			//movingAnim.update(img, gameTime);
			updateMovement(map);
		}
	}

	public void startMovement(ArrayList<WalkableTile> path) {
		if(!isMoving) {
			this.path = path;
			if(path != null) {
				//movingAnim.reset();
				
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
			setPos(path.get(path.size() - 1).mapX, path.get(path.size() - 1).mapY);

			path.get(path.size() - 1).highlighted = false;
			path.remove(path.size() - 1);
			
			if(path.size() > 0) {	
				target = path.get(path.size() - 1);
				determineSpeed();
				setOrientationImage();
			}
			else {
				isMoving = false;
				//img.resetImg();
			}
		}
		else
		{
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
	
	private void setOrientationImage() {
		int orientationId = 0;
		
		if(speedX == 0) {
			if(speedY > 0)
				orientationId = 0;
			else if(speedY < 0)
				orientationId = 3;
		}
		else if(speedX < 0 && speedY < 0)
			orientationId = 2;
		else if(speedX < 0 && speedY > 0)
			orientationId = 1;
		else if(speedX > 0 && speedY < 0)
			orientationId = 4;
		else if(speedX > 0 && speedY > 0)
			orientationId = 5;

		this.getImage().setBitmap(orientations[orientationId]);
	}
}