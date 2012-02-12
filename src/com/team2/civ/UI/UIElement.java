package com.team2.civ.UI;

public class UIElement {
	public int x, y;
	public int width, height;
	public UIElement parent;

	public UIElement(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean picked(int mx, int my) {
		if(x > mx && x < mx + width && y > my && y < my + height)
			return true;
		
		return false;
	}
}
