package com.team2.civ.Game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;
import com.team2.civ.Map.MapObject;

public class Particle extends CoordObject {
	private BufferedImage img;
	
	private boolean complete = false;
	private MapObject target;
	private float speedX, speedY;
	
	public Particle(int mapX, int mapY, MapObject target) {
		super(mapX, mapY);
		try {
			this.img = Resources.getInstance().getImage("particle");
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		this.target = target;
		determineSpeed();
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void update(long gameTime) {
		x += speedX;
		y += speedY;
		
		if(Math.abs(x - target.x) < 14 && Math.abs(y - target.y) < 14)
			complete = true;
	}
	
	public void draw(Graphics2D g, float offsetX, float offsetY) {		
		g.drawImage(img, null, (int)(x + offsetX), (int)(y + offsetY - (img.getHeight() - Resources.TILE_HEIGHT)));
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
}