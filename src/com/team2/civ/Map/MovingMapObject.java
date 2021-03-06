package com.team2.civ.Map;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.team2.civ.Team2Civ;
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
	
	public MovingMapObject(int mapX, int mapY, String imgId, Player owner) throws ResNotFoundException {
		super(mapX, mapY, imgId + "_1", owner);
		
		if(!Team2Civ.AI_MODE) initImages(imgId);
		
		
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
	
	private void initImages(String imgId) {
		orientations = new BufferedImage[6];
		
		Resources res = Resources.getInstance();
		try {
			for(int i = 0; i < 6; i++)
				orientations[i] = res.getImage(imgId+"_"+i+"_"+owner.colour.toString());
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isMoving() {
		return isMoving;
	}
	
	public boolean movementNotFinished() {
		if(path == null) return false;
		return path.size() > 0;
	}
	
	public void update(long gameTime) {
		if(isMoving) {
			//movingAnim.update(img, gameTime);
			updateMovement();
		}
	}

	public void startMovement(List<WalkableTile> path) {
		if(!isMoving && path.size() > 0) {
			if(this.path != null) {
				for(WalkableTile wt : path)
					wt.highlighted = false;
			}
	
			if(path != null) {
				this.path = new ArrayList<WalkableTile>(path);
				//movingAnim.reset();
				
				for(WalkableTile wt : this.path)
					wt.highlighted = true;
				
				speedX = 0;
				speedY = 0;
				isMoving = true;
				target = this.path.get(this.path.size() - 1);
				determineSpeed();
				setOrientationImage();
			}
		}
	}
	
	public void startMovement() {
		if(!isMoving && path.size() > 0) {
			for(WalkableTile wt : path)
				wt.highlighted = true;
				
			speedX = 0;
			speedY = 0;
			isMoving = true;
			target = path.get(path.size() - 1);
			determineSpeed();
			setOrientationImage();
		}
	}
	
	protected void onArriveAtTile() {
		setPos(target.mapX, target.mapY);

		target.highlighted = false;
		path.remove(target);
		
		if(path.size() > 0) {	
			target = path.get(path.size() - 1);
			determineSpeed();
			setOrientationImage();
		}
		else {
			isMoving = false;
		}
	}
	
	private void updateMovement() {
		if(speedX == 0 && speedY == 0) {
			onArriveAtTile();
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
		float dx = x - target.x;
		float dy = y - target.y;
		
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

		this.setImage(orientations[orientationId]);
	}
}