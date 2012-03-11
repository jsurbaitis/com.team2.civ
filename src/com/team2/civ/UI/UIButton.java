package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UIButton extends UIElement {

	private BufferedImage img;
    private UIEvent actor;
	public UIButton(int x, int y, UIEvent.Event commandID,	BufferedImage img) {
		super(x, y, img.getWidth(), img.getHeight(), new UIEvent (commandID));

		this.img = img;
	}

	public UIButton(int x,int y,UIEvent event, BufferedImage img){
		super(x, y, img.getWidth(), img.getHeight(), null);
		this.img=img;
		actor=event;
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
