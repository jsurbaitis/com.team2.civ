package com.team2.civ.Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Vector;

import com.team2.civ.Team2Civ;
import com.team2.civ.Map.MapObjectImage;
import com.team2.civ.Map.WalkableTile;
import com.team2.civ.Map.WallTile;

public class GameGraphics {

	private int offsetX = 0;
	private int offsetY = 0;
	
	private static final double SCALE_MAX = 1.0;
	private static final double SCALE_MIN = 0.2;
	private static final double ZOOM_DELTA = 0.2;
	private static final double ZOOM_FACTOR = 0.02;

	private double scale = 1.0f;
	private double oldScale = scale;
	private boolean zoomingIn = false;
	private boolean zoomingOut = false;
	
	private Vector<MapObjectImage> unitDraw = new Vector<MapObjectImage>();
	private Vector<MapObjectImage> lowDraw = new Vector<MapObjectImage>();
	
	public GameGraphics(GameMap map) {
		for (WallTile wt : map.getUnwalkableMap()) {
			lowDraw.add(wt.getImage());
		}

		for (WalkableTile wt : map.getWalkableMap()) {
			lowDraw.add(wt.getImage());
		}

		for (GameStaticObject so : map.getStaticObjects()) {
			lowDraw.add(so.getImage());
		}
		
		Collections.sort(lowDraw);
	}
	
	public int getShowingWidth() {
		return (int) (Team2Civ.WINDOW_WIDTH * (1 / scale));
	}
	
	public int getShowingHeight() {
		return (int) (Team2Civ.WINDOW_HEIGHT * (1 / scale));
	}
	
	public int getOffsetX() {
		return offsetX;
	}
	
	public int getOffsetY() {
		return offsetY;
	}
	
	public void setOffsets(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public void addToOffsets(int dx, int dy) {
		offsetX += dx * (1 / scale);
		offsetY += dy * (1 / scale);
	}
	
	public int mouseToMapX(int mx) {
		return (int) (mx * (1 / scale) - offsetX);
	}
	
	public int mouseToMapY(int my) {
		return (int) (my * (1 / scale) - offsetY);
	}
	
	public void updateZoom() {
		if (zoomingIn || zoomingOut) {
			int oldWidth = getShowingWidth();
			int oldHeight = getShowingHeight();

			if (zoomingIn) {
				scale += ZOOM_FACTOR;
				offsetX -= (oldWidth - getShowingWidth()) / 2;
				offsetY -= (oldHeight - getShowingHeight()) / 2;
			} else {
				scale -= ZOOM_FACTOR;
				offsetX += (getShowingWidth() - oldWidth) / 2;
				offsetY += (getShowingHeight() - oldHeight) / 2;
			}

			if (Math.abs(oldScale - scale) >= ZOOM_DELTA) {
				zoomingIn = false;
				zoomingOut = false;
			}
		}
	}
	
	public void zoomInput(int rot) {
		if (!zoomingIn && !zoomingOut) {
			if (rot < 0)
				zoomIn();
			else if (rot > 0)
				zoomOut();
		}
	}
	
	public void zoomIn() {
		if (scale < SCALE_MAX) {
			oldScale = scale;
			zoomingIn = true;
		}
	}

	public void zoomOut() {
		if (scale > SCALE_MIN) {
			oldScale = scale;
			zoomingOut = true;
		}
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Team2Civ.WINDOW_WIDTH, Team2Civ.WINDOW_HEIGHT);

		g.scale(scale, scale);

		synchronized (lowDraw) {
			for (MapObjectImage i : lowDraw)
				i.draw(g, (int) (offsetX), (int) (offsetY), scale);
		}

		Collections.sort(unitDraw);
		for (MapObjectImage i : unitDraw)
			i.draw(g, (int) (offsetX), (int) (offsetY), scale);

		g.scale(1 / scale, 1 / scale);
	}
	
	public void removeLowImage(MapObjectImage img) {
		lowDraw.remove(img);
	}
	
	public void removeUnitImage(MapObjectImage img) {
		unitDraw.remove(img);
	}
	
	public void addLowImage(MapObjectImage img) {
		lowDraw.add(img);
		Collections.sort(lowDraw);
	}
	
	public void addUnitImage(MapObjectImage img) {
		unitDraw.add(img);
	}
}
