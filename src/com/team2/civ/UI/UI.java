package com.team2.civ.UI;

import java.util.ArrayList;

import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;

public class UI {
	private Resources res;

	private ArrayList<UIButton> buttons = new ArrayList();
	private ArrayList<UISlider> sliders = new ArrayList();

	public static enum UIEvent {
		ACTION_ATTACK, BUILD_CITY, HANDLED
	};
	
	public UI(Resources res) {
		this.res = res;
		
		
	}

	private void initBtn(int x, int y, int width, int height, UIEvent command,
			String imageID) {
		try {
			buttons.add(new UIButton(x, y, width, height, command, res.getImage(imageID)));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initSldr(int x, int y, int width, int height) {
		sliders.add(new UISlider(x, y, width, height));
	}

	public boolean checkpanels(int mx, int my) {
		boolean inarea = false;
		for (int i = 0; i < sliders.size(); i++)
			if (!inarea)
				inarea = mx >= sliders.get(i).x
						&& mx <= sliders.get(i).x + sliders.get(i).width
						&& my <= sliders.get(i).y
						&& my >= sliders.get(i).y + sliders.get(i).height;
			else
				break;
		return inarea;
	}

	public UIEvent checkbuttons(int mx, int my) {
		for(UIButton b: buttons) {
			if(b.picked(mx, my))
				return b.commandID;
		}
		return null;
	}

}
// MOUSE STUFF

/*
 * private UI eventChecker;
 * 
 * @Override public void mouseClicked(MouseEvent ev) { UIButton chosenbutton;
 * boolean inUI = eventChecker.checkpanels(ev.getY(), ev.getY()); if (inUI)
 * {chosenbutton = eventChecker.checkbuttons(ev.getX(), ev.getY()); if
 * (chosenbutton != null) chosenbutton.pressed(); } else ; // send back to main
 * program mouse is not in UI section, it's in // game area
 */
