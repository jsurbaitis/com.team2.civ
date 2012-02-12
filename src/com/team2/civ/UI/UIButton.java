package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.team2.civ.UI.UI.UIEvent;

public class UIButton extends UIElement {
	public UIEvent commandID;
	private BufferedImage img;

	public UIButton(int x, int y, int width, int height, UIEvent commandID, BufferedImage img) {
		super(x, y, width,height);
		
		this.commandID = commandID;
		this.img = img;
	}

	public void draw(Graphics2D g) {
		if(parent == null)
			g.drawImage(img, null, x, y);
		else
			g.drawImage(img, null, parent.x + x, parent.y + y);
	}
}