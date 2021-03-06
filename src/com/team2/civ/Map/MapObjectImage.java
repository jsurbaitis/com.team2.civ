package com.team2.civ.Map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.team2.civ.GameWindow;
import com.team2.civ.Data.Resources;

public class MapObjectImage implements Comparable<MapObjectImage> {
	private static final boolean SHOW_COORDS = false;
	
	public static BufferedImage selectedImg;
	
	private BufferedImage fowImg;
	private BufferedImage defaultImg;
	private BufferedImage img;
	private MapObject parent;
	private boolean visible;

	public MapObjectImage(BufferedImage img, MapObject parent) {
		this.defaultImg = img;
		this.img = img;
		this.parent = parent;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void resetImg() {
		img = defaultImg;
	}
	
	public void setFowImg(BufferedImage fowImg) {
		this.fowImg = fowImg;
	}
	
	public void setBitmap(BufferedImage img) {
		this.img = img;
	}
	
	public int getHeight() {
		return img.getHeight();
	}
	
	public int getWidth() {
		return img.getWidth();
	}
	
	protected boolean checkVisibility(float offsetX, float offsetY, double scale) {
		if(parent.x + offsetX + getWidth() > 0 && parent.x + offsetX < GameWindow.WINDOW_WIDTH * (1/scale) && 
		   parent.y + offsetY + getHeight() > 0 && parent.y + offsetY < GameWindow.WINDOW_HEIGHT * (1/scale))
			return true;
		else
			return false;
	}

	public void draw(Graphics2D g, float offsetX, float offsetY, double scale) {
		visible = checkVisibility(offsetX, offsetY, scale);

		if(visible) {
			if(parent.beingSeen)
				drawIsSeen(g, offsetX, offsetY, scale);
			else if(parent.seen)
				drawWasSeen(g, offsetX, offsetY, scale);
		}
	}
	
	private void drawWasSeen(Graphics2D g, float offsetX, float offsetY, double scale) {
		g.drawImage(fowImg, null, (int) (parent.x + offsetX + (Resources.TILE_WIDTH - getWidth())/2), 
    	   		 (int) (parent.y - (getHeight() - Resources.TILE_HEIGHT) + offsetY));
	}
	
	private void drawIsSeen(Graphics2D g, float offsetX, float offsetY, double scale) {
		if(parent.selected) {
			g.drawImage(selectedImg, null, (int) ((int) parent.x + offsetX + (Resources.TILE_WIDTH - selectedImg.getWidth())/2), 
         	   		 (int) (parent.y - (selectedImg.getHeight() - Resources.TILE_HEIGHT) + offsetY));	
		}
		
		g.drawImage(img, null, (int) ((int) parent.x + offsetX + (Resources.TILE_WIDTH - getWidth())/2), 
				         	   		 (int) (parent.y - (getHeight() - Resources.TILE_HEIGHT) + offsetY));

		if(parent.highlighted) {
			g.drawImage(selectedImg, null, (int) ((int) parent.x + offsetX + (Resources.TILE_WIDTH - selectedImg.getWidth())/2), 
         	   		 (int) (parent.y - (selectedImg.getHeight() - Resources.TILE_HEIGHT) + offsetY));	
		}
		
		if(SHOW_COORDS)
			g.drawString(""+parent.mapX+" / "+parent.mapY, parent.x + offsetX - getWidth()/2, parent.y + offsetY);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		else return false;
	}

	@Override
	public int compareTo(MapObjectImage i) {
		return parent.compareTo(i.parent);
	}
}
