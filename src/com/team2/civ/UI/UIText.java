package com.team2.civ.UI;

import java.awt.Graphics2D;

public class UIText extends UIElement {
	public String text;

	public UIText(int x, int y, String text) {
		super(x, y, text.length()*20, 20, null);

		this.text = text;
	}

	@Override
	public void draw(Graphics2D g) {
		if (parent == null)
	 		g.drawString(text, x, y);
		else
			g.drawString(text, x + parent.x, y + parent.y);
	}
	
}
