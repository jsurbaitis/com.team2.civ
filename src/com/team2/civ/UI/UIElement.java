package com.team2.civ.UI;

import java.awt.Graphics2D;

public class UIElement {
	public int x, y;
	public int width, height;
	public UIElement parent;
	private UIEvent commandID;

	public UIElement(int x, int y, int width, int height, UIEvent commandID) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
        this.commandID = commandID;
	}
	public UIEvent clicked(int mx, int my) {
		if(picked(mx, my))
			return commandID;
		else
			return null;
	}
	
	public boolean picked(int mx, int my) {
		if(parent != null) {
			mx -= parent.x;
			my -= parent.y;
		}
		
		if(mx > x && mx < x + width && my > y && my < y + height)
			return true;
		
		return false;
	}
	
	public void draw(Graphics2D g) {}
}
