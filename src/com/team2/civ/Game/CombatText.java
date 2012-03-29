package com.team2.civ.Game;

import java.awt.Color;
import java.awt.Graphics2D;

import com.team2.civ.Data.Resources;
import com.team2.civ.Map.CoordObject;

public class CombatText extends CoordObject {
	String text;
	float alpha = 1.0f;
	int width;
	
	public CombatText(int mapX, int mapY, String text) {
		super(mapX, mapY);
		this.text = text;
		
		width = text.length() / 2;
	}
	
	public boolean isDone() {
		return alpha < 1;
	}
	
	public void update(long gameTime) {
		if(gameTime % 2 == 0) {
			alpha -= 0.05;
			y -= 1;
		}
	}
	
	public void draw(Graphics2D g, float offsetX, float offsetY) {
		g.setColor(new Color(1.0f, 0.0f, 0.0f, alpha));
		g.drawString(text, x + offsetX + Resources.TILE_WIDTH/2 - width/2,
				y + offsetY + Resources.TILE_HEIGHT/3);
	}
}
