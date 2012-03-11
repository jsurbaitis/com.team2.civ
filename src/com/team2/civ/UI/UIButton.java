package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UIButton extends UIElement {

	private BufferedImage img;

	public UIButton(int x, int y, UIEvent commandID,	BufferedImage img) {
		super(x, y, img.getWidth(), img.getHeight(), commandID);
		this.img = img;
	}

	@Override
	public void draw(Graphics2D g) {
		if (parent == null)
			g.drawImage(img, null, x, y);
		else {
			g.drawImage(img, null, parent.x + x, parent.y + y);
		}
	}

}
