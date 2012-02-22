package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameUnit;

public class UI {
	private Resources res;
	MiniMap miniMap;
	UIButton endButton;
	final int WW = Team2Civ.WINDOW_WIDTH;
	final int WH = Team2Civ.WINDOW_HEIGHT;
	private Vector<UISlider> sliders = new Vector<UISlider>();

	private GameUnit currentunit;
	private UISlider selectionInfo;
	private UISlider buildSlider;
	
	private int pressStartX;
	private int pressStartY;
	
	private int lastMouseX;
	private int lastMouseY;
	
	private boolean leftClick = true;

	public static enum UIEvent {
		BUILD, ACTION_ATTACK, BUILD_CITY,BUILD_WORKER, ACTION_DESTROY, ACTION_FORTIFY, BUILD_LUMBER, BUILD_FARM, HANDLED, END_TURN
	}

	public UI(Resources res) {
		this.res = res;

		miniMap = new MiniMap(WW * 4 / 5 + 10, WH * 4 / 5, WW / 5, WH / 5);
		try {
			endButton = new UIButton(miniMap.x, miniMap.y - 100, 100,
					miniMap.width, UIEvent.END_TURN, res.getImage("END_TURN"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void update(long gameTime) {
		for (UISlider s : sliders)
			s.update(gameTime);
	}

	public void draw(Graphics2D g) {
		for (UISlider s : sliders) {
			s.draw(g);
		}
		endButton.draw(g);
		miniMap.draw(g);
	}

	public UIEvent toEvent(String s)
	{
		for (UIEvent event : UIEvent.values()) {  
		if (("BUILD_"+s).equals(event.toString())) return event;
		}
		return null;
	}
	public void showBuildInfo(GameUnit unit) {
		sliders.remove(buildSlider);
		try {
			buildSlider = new UISlider(0, -WH/2, WW / 4, WH, true,
					res.getImage("slider_vertical_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < unit.data.buildIDs.size(); i++) {	
				try {
					buildSlider.addChild(new UIText (0,i*100+85,unit.data.buildIDs.get(i)));
					buildSlider.addChild(new UIButton(0, (i * 100) + 100,	100, 50, toEvent(unit.data.buildIDs.get(i)), res.getImage(unit.data.buildIDs.get(i))));
				} catch (ResNotFoundException e) {
					e.printStackTrace();
				}
			}
		
		buildSlider.slideOut();
		sliders.add(buildSlider);

	}

	public void showUnitInfo(GameUnit unit) {
		sliders.remove(selectionInfo);
		try {
			selectionInfo = new UISlider(miniMap.x, WH * 8 / 10, WW
					- miniMap.width, WH * 2 / 10, false,
					res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		currentunit = unit;
		selectionInfo.addChild(new UIText(10, 15, (unit.data.name))); //
		// selectionInfo.addChild(new UIText(10, 35, (unit.data.description)));

		selectionInfo.addChild(new UIText(10, 55,
				(unit.HP + "/" + unit.data.HP)));
		selectionInfo.addChild(new UIText(10, 75,
				(unit.AP + "/" + unit.data.AP)));
     
		for (int i = 0; i < unit.data.uiActions.size(); i++) {
			
				try {
					selectionInfo.addChild(new UIButton((i * 100 + 200),
							selectionInfo.height / 4, 100, 50,
							unit.data.uiActions.get(i), res
									.getImage(unit.data.uiActions.get(i)
											.toString())));
				} catch (ResNotFoundException e) {
					e.printStackTrace();
				}
			}
		if (!unit.data.buildIDs.isEmpty()) try {
			selectionInfo.addChild(new UIButton(100, selectionInfo.height / 4, 100, 50,UIEvent.BUILD, res.getImage(UIEvent.BUILD.toString())));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		selectionInfo.slideOut();
		//showBuildInfo(unit);
		sliders.add(selectionInfo);

	}
	
	public UIEvent onMouseInput(MouseEvent ev) {
		if(ev.getID() == MouseEvent.MOUSE_PRESSED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
			
			pressStartX = lastMouseX;
			pressStartY = lastMouseY;
			
			if(SwingUtilities.isLeftMouseButton(ev))
				leftClick = true;
			else
				leftClick = false;
		}
		else if(ev.getID() == MouseEvent.MOUSE_DRAGGED) {
			lastMouseX = ev.getX();
			lastMouseY = ev.getY();
			
			//drag
		} else if(ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick) {
			if(Math.abs(ev.getX() - pressStartX) < 5 && Math.abs(ev.getY() - pressStartY) < 5) {
				//rightClick
			}
		} else if(ev.getID() == MouseEvent.MOUSE_RELEASED && leftClick) {
			if(Math.abs(ev.getX() - pressStartX) < 5 && Math.abs(ev.getY() - pressStartY) < 5) {
				//leftClick
			}
		}
		return null;
	}

	public UIEvent onClick(MouseEvent ev) {
		UIEvent event = null;
		for (int i = 0; i < sliders.size(); i++) {
			UIEvent temp = sliders.get(i).onClick(ev);
			if (temp != null)
				event = temp;
		}
		if (ev.getX() > miniMap.x && ev.getX() < miniMap.x + miniMap.width
				&& ev.getY() > miniMap.height + miniMap.y
				&& ev.getY() < miniMap.y)
			return UIEvent.HANDLED;
		if (event == UIEvent.BUILD) {
			showBuildInfo(currentunit);
			return UIEvent.HANDLED;
		}

		return event;
	}

}
