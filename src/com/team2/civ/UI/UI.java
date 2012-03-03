package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Game.Player;

public class UI {
	private Resources res;
	private Player player;
	MiniMap miniMap;
	UIButton endButton;
	final int WW = Team2Civ.WINDOW_WIDTH;
	final int WH = Team2Civ.WINDOW_HEIGHT;
	private Vector<UISlider> sliders = new Vector<UISlider>();
	UIText curmetal;
	UIText curpop;
	private GameUnit currentunit;
	private GameStaticObject curstatobj;
	private UISlider selectionInfo;
	private UISlider buildSlider;

	private int pressStartX;
	private int pressStartY;

	private int lastMouseX;
	private int lastMouseY;

	private boolean leftClick = true;

	public static enum UIEvent {
		BUILD, ACTION_ATTACK, BUILD_CITY, BUILD_WORKER, BUILD_POWERPLANT, BUILD_FORTIFICATION,
		ACTION_DESTROY, ACTION_FORTIFY, BUILD_MINE, HANDLED, END_TURN
	}

	public UI(Player player, Resources res) {
		this.res = res;
		this.player = player;
		miniMap = new MiniMap(WW * 7 / 10 + 10, WH * 7 / 10, WW * 3 / 10,
				WH * 3 / 10);
		try {
			endButton = new UIButton(miniMap.x, miniMap.y - 100,
					UIEvent.END_TURN, res.getImage("END_TURN"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		curmetal = new UIText(miniMap.x + 175, miniMap.y - 10,
				"Current metal: " + player.metal);
		curpop = new UIText(miniMap.x, miniMap.y - 10, "Current population: "
				+ player.population);

	}

	public void update(long gameTime) {
		synchronized (sliders) {
			for (UISlider s : sliders)
				s.update(gameTime);
		}
	}

	public void draw(Graphics2D g) {
		synchronized (sliders) {
			for (UISlider s : sliders) {
				s.draw(g);
			}
		}
		endButton.draw(g);
		curmetal.draw(g);
		curpop.draw(g);
		miniMap.draw(g);
	}

	public UIEvent toEvent(String s) {
		for (UIEvent event : UIEvent.values()) {
			if (("BUILD_" + s).equals(event.toString()))
				return event;
		}
		return null;
	}

	public void showBuildInfo(GameUnit unit) {
		synchronized (sliders) {
			sliders.remove(buildSlider);
		}

		try {
			buildSlider = new UISlider(0, 0, WW / 8, 50, true,
					res.getImage("slider_vertical_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < unit.data.buildIDs.size(); i++) {
			try {
				buildSlider.addChild(new UIText(0, i * 100 + 85,
						unit.data.buildIDs.get(i)));
				buildSlider.addChild(new UIButton(0, (i * 100) + 100,
						toEvent(unit.data.buildIDs.get(i)), res
								.getImage(unit.data.buildIDs.get(i))));
				buildSlider.height += 200;
				buildSlider.y -= 200;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		}

		buildSlider.slideOut();

		synchronized (sliders) {
			sliders.add(buildSlider);
		}
	}

	public void showBuildInfo(GameStaticObject unit) {
		synchronized (sliders) {
			sliders.remove(buildSlider);
		}

		try {
			buildSlider = new UISlider(0, 0, WW / 8, 50, true,
					res.getImage("slider_vertical_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < unit.data.buildIDs.size(); i++) {
			try {
				buildSlider.addChild(new UIText(0, i * 100 + 85,
						unit.data.buildIDs.get(i)));
				buildSlider.addChild(new UIButton(0, (i * 100) + 100,
						toEvent(unit.data.buildIDs.get(i)), res
								.getImage(unit.data.buildIDs.get(i))));
				buildSlider.height += 200;
				buildSlider.y -= 200;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		}

		buildSlider.slideOut();
		synchronized (sliders) {
			sliders.add(buildSlider);
		}
	}

	public void showUnitInfo(GameUnit unit) {
		curstatobj = null;

		synchronized (sliders) {
			sliders.remove(selectionInfo);
		}

		try {
			selectionInfo = new UISlider(miniMap.x, WH * 8 / 10, 200,
					WH * 2 / 10, false, res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		currentunit = unit;
		selectionInfo.addChild(new UIText(10, 15, (unit.data.name))); //
		// selectionInfo.addChild(new UIText(10, 35, (unit.data.description)));

		selectionInfo.addChild(new UIText(10, 55,
				(unit.getHP() + "/" + unit.data.HP)));
		selectionInfo.addChild(new UIText(10, 75,
				(unit.getAP() + "/" + unit.data.AP)));

		for (int i = 0; i < unit.data.uiActions.size(); i++) {
			try {
				selectionInfo.width += 100;
				selectionInfo.addChild(new UIButton((i * 100 + 200),
						selectionInfo.height / 4, unit.data.uiActions.get(i),
						res.getImage(unit.data.uiActions.get(i).toString())));
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (!unit.data.buildIDs.isEmpty())
			try {
				selectionInfo.width += 100;
				selectionInfo.addChild(new UIButton(100,
						selectionInfo.height / 4, UIEvent.BUILD, res
								.getImage(UIEvent.BUILD.toString())));
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		selectionInfo.slideOut();

		synchronized (sliders) {
			sliders.add(selectionInfo);
		}
	}

	public void showStaticObjectInfo(GameStaticObject unit) {
		currentunit = null;

		synchronized (sliders) {
			sliders.remove(selectionInfo);
		}

		try {
			selectionInfo = new UISlider(miniMap.x, WH * 8 / 10, 200,
					WH * 2 / 10, false, res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		curstatobj = unit;
		// selectionInfo.addChild(new UIText(10, 15, (unit.))); //
		// selectionInfo.addChild(new UIText(10, 35, (unit.data.description)));
		// selectionInfo.addChild(new UIText(10, 55,(unit.getHP() + "/" +
		// unit.data.getHP())));
		// selectionInfo.addChild(new UIText(10, 75,(unit.getAP() + "/" +
		// unit.data.getAP())));

		if (!unit.data.buildIDs.isEmpty())
			try {
				selectionInfo.width += 100;
				selectionInfo.addChild(new UIButton(100,
						selectionInfo.height / 4, UIEvent.BUILD, res
								.getImage(UIEvent.BUILD.toString())));
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		selectionInfo.slideOut();

		synchronized (sliders) {
			sliders.add(selectionInfo);
		}
	}

	public UIEvent onMouseInput(MouseEvent ev) {
		if (ev.getID() == MouseEvent.MOUSE_PRESSED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();

			pressStartX = lastMouseX;
			pressStartY = lastMouseY;

			if (SwingUtilities.isLeftMouseButton(ev))
				leftClick = true;
			else
				leftClick = false;
		} else if (ev.getID() == MouseEvent.MOUSE_DRAGGED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();

			// drag
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				// rightClick
			}
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				return onClick(ev);

			}
		}
		return null;
	}

	public UIEvent onClick(MouseEvent ev) {
		UIEvent event = null;

		synchronized (sliders) {
			for (int i = 0; i < sliders.size(); i++) {
				UIEvent temp = sliders.get(i).onClick(ev);
				if (temp != null)
					event = temp;
			}
			try {
				if (!buildSlider.picked(ev.getX(), ev.getY()))
					sliders.remove(buildSlider);
			} catch (Exception e) {
			}
			if (ev.getX() > miniMap.x && ev.getX() < miniMap.x + miniMap.width
					&& ev.getY() < miniMap.height + miniMap.y
					&& ev.getY() > miniMap.y)
				return UIEvent.HANDLED;
			if (event == UIEvent.BUILD) {
				if (currentunit != null)
					showBuildInfo(currentunit);
				else
					showBuildInfo(curstatobj);
				return UIEvent.HANDLED;
			}
			if (ev.getX() > endButton.x
					&& ev.getX() < endButton.x + endButton.width
					&& ev.getY() < endButton.height + endButton.y
					&& ev.getY() > endButton.y)
				return UIEvent.END_TURN;
		}
		return event;
	}

}
