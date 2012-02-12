package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.team2.civ.UI.UI.UIEvent;

public class UISlider extends UIElement {
	private BufferedImage bg;
	private ArrayList<UIElement> children = new ArrayList<UIElement>();
	
	private boolean slidingOut = false;
	private boolean slidingIn = false;
	
	private final boolean vertical;
	private final int initialx;
	private final int initialy;

	public UISlider(int x, int y, int width, int height, boolean isVertical, BufferedImage bg) {
		super(x, y, width, height, null);
		initialx = x;
		initialy = y;
        this.vertical = isVertical;
        this.bg = bg;
	}

	public void update(long gameTime) {
		if (slidingOut && gameTime % 1 == 0 && !vertical) {
			x -= 16;
			if (initialx > x + width)
				slidingOut = false;
		}
		else if (slidingIn && gameTime % 1 == 0 && !vertical) {
			x += 16;
			if (initialx < x + width)
				slidingIn = false;
		}
		else if (slidingOut && gameTime % 1 == 0 && vertical) {
			y += 16;
			if (initialy < y + height)
				slidingOut = false;
		}
		else if (slidingIn && gameTime % 1 == 0 && vertical) {
			y -= 16;
			if (initialy > y + height)
				slidingIn = false;
		}
	}

	public void slideIn(){
		slidingIn=true;
		slidingOut=false;
	}
	public void slideOut(){
		slidingIn=false;
		slidingOut=true;
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(bg, x, y, x+width, y+height, null);
		
		for(UIElement elem: children)
			elem.draw(g);
	}

	public void addChild(UIElement ele) {
		ele.parent = this;
		children.add(ele);
	}

	public UIEvent onClick(MouseEvent ev) {
		UIEvent temp = null;
		for (int i = 0; i < this.children.size(); i++) {

			temp = this.children.get(i).clicked(ev.getX(), ev.getY());

			if (temp != null)
				return temp;
		}
		return temp;
	}
}
