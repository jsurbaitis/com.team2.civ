package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.team2.civ.UI.UI.UIEvent;

public class UISlider extends UIElement {
	private ArrayList<UIButton> children;
	private BufferedImage img;

	public UISlider(int x, int y, int width, int height) {
		super(x, y, width, height);

		this.children = null;
	}

	public void draw(Graphics2D g) {
		g.drawImage(img, null, this.x, this.y);
		//draw children
	}
	
	public UIEvent onClick(MouseEvent ev) {
		//check all children
		return null;
	}

	public void move(long gameTime, int directionx, int directiony) {
		if (gameTime % 10 == 0) {
			if (x - directionx > 0)// missing check for end of screen
				this.x += directionx;
			if (y - directiony > 0)// missing check for end of screen
				this.y += directiony;
		}

	}
}
