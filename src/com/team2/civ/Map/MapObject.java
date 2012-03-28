package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameMap;
import com.team2.civ.Game.Player;


public class MapObject extends CoordObject {
	public Player owner;
	public boolean selected = false;
	public boolean highlighted = false;
	
	public boolean seen = GameMap.FOW_ON ? false : true;
	public boolean beingSeen = true;
	
	protected MapObjectImage image;
	
	public MapObject(int mapX, int mapY, String imgId, Player owner) {
		super(mapX, mapY);
		this.owner = owner;		
		updateImage(imgId);
	}

	public void isSeen() {
		beingSeen = true;
		seen = true;
		
		//TODO:
		//check if it has been destroyed
		//if yes, finally stop drawing it since player is aware of the change
	}

	public MapObjectImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage img) {
		if(Team2Civ.AI_MODE) return; 
		
		image.setBitmap(img);
	}
	
	public void updateImage(String imgId) {
		if(Team2Civ.AI_MODE) return; 
		
		Resources res = Resources.getInstance();
		try {
			if(owner != null) imgId += "_"+owner.colour.toString();
			BufferedImage img = res.getImage(imgId);
			
			if(image != null)
				image.setBitmap(img);
			else
				image = new MapObjectImage(img, this);
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void updateFowImage(String imgId) {
		if(Team2Civ.AI_MODE) return; 
		
		Resources res = Resources.getInstance();
		try {
			imgId += "_fow";
			if(owner != null) imgId += "_"+owner.colour.toString();
			BufferedImage fowImg = res.getImage(imgId);
			image.setFowImg(fowImg);
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean picked(int f, int g) {
		if(!image.isVisible())
			return false;
		
		if(f > x + Resources.TILE_WIDTH / 10 && f < x + Resources.TILE_WIDTH - Resources.TILE_WIDTH / 10 && 
		   g > y + Resources.TILE_HEIGHT / 10 && g < y + Resources.TILE_HEIGHT - Resources.TILE_HEIGHT / 10)
			return true;
		else
			return false;			
	}
}
