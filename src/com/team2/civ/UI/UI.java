package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.SwingUtilities;

import com.team2.civ.GameWindow;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;
import com.team2.civ.Game.GameGraphics;
import com.team2.civ.Game.GameMap;
import com.team2.civ.Game.GameStaticObject;
import com.team2.civ.Game.GameUnit;
import com.team2.civ.Game.Player;
import com.team2.civ.Map.MapObject;

public class UI {
	private Resources res;
	private Player player;
	MiniMap miniMap;
	UIButton endButton;
	final int WW = GameWindow.WINDOW_WIDTH;
	final int WH = GameWindow.WINDOW_HEIGHT;
	private Vector<UISlider> sliders = new Vector<UISlider>();
	UIText curmetal;
	UIText curpop;
	private GameUnit currentunit;
	private GameStaticObject curstatobj;
	private UISlider selectionInfo;
	private UISlider buildSlider;
    private UISlider choiceSlider;
	private int pressStartX;
	private int pressStartY;
	public GameGraphics graphics;
	public GameController game;
	private int lastMouseX;
	private int lastMouseY;

	private boolean leftClick = true;

	public UI(Player player, GameMap map,GameGraphics graphics, GameController game) {
		this.res = Resources.getInstance();
		this.player = player;
		this.game = game;
		miniMap = new MiniMap(WW * 28/40, WH * 28/40, WW * 12/40,
				WH * 12 /40, map,graphics);
		try {
			endButton = new UIButton(miniMap.x, miniMap.y - 50, new UIEvent(UIEvent.Event.END_TURN),res.getImage("END_TURN"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}

		curmetal = new UIText(miniMap.x + 5, miniMap.y - 10,
				"Current metal: " + player.metal);
		curpop = new UIText(miniMap.x + 5, miniMap.y -30, "Current Power Usage: "
				+ player.powerUsage);

	}

	public void update(long gameTime) {
		synchronized (sliders) {
			for (UISlider s : sliders)
				s.update(gameTime);
		}
		curmetal.text = "Current Metal: " + player.metal;
		curpop.text = "Current Power Usage: " + player.powerUsage+"/"+player.powerCapability;
		updateslider();
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
	

	public void closeVSlide() {
		synchronized (sliders) {
			sliders.remove(buildSlider);
		}
	}

	public void closeHSlide() {
		synchronized (sliders) {
			sliders.remove(selectionInfo);
			sliders.remove(choiceSlider);
		}
	}
	public void closeSelSlide(){
		synchronized (sliders) {
			sliders.remove(selectionInfo);
		}
	}

	public UIEvent toEvent(String s) {
		for (UIEvent.Event event : UIEvent.Event.values()) {
			if (("BUILD_" + s).equals(event.toString()))
				return new UIEvent(event);
		}
		return null;
	}
	public void updateslider(){
		try{
		((UIText) selectionInfo.children.get(1)).text=(currentunit.getHP() + "/" + currentunit.data.HP);
		((UIText) selectionInfo.children.get(2)).text=(currentunit.getAP() + "/" + currentunit.data.AP);
		}
		catch(Exception e){}
	}

	public void showInfo(ArrayList<MapObject> list) {
		closeHSlide();
		closeVSlide();
		int totalwidth=50;
		
		if (list.size()==1)
		{
			if(list.get(0) instanceof GameUnit) {
				showUnitInfo((GameUnit) list.get(0));
			}
			else showStaticObjectInfo((GameStaticObject)list.get(0));
		}
		else{ 
			try{
				choiceSlider= new UISlider(miniMap.x,WH*8/10,totalwidth,100,false,res.getImage("slider_horizontal_bg"));
			   
		for(int i=0;i<list.size();i++) {
			if(list.get(i) instanceof GameUnit) {
				choiceSlider.addChild(new UIText(totalwidth-10,10,((GameUnit) list.get(i)).name));
				choiceSlider.addChild(new UIButton(totalwidth,40,(new UIEvent(list.get(i))),res.getImage(((GameUnit) list.get(i)).data.id+"_1")));
				totalwidth+=res.getImage(((GameUnit) list.get(i)).data.id+"_1").getWidth()+50;
			}
			else {
				choiceSlider.addChild(new UIText(totalwidth-10,10,((GameStaticObject) list.get(i)).name));
				choiceSlider.addChild(new UIButton(totalwidth,10,(new UIEvent(list.get(i))),res.getImage(((GameStaticObject) list.get(i)).data.id)));
				totalwidth+=res.getImage(((GameStaticObject) list.get(i)).data.id).getWidth()+50;
			}
			}
		choiceSlider.width=totalwidth;
		choiceSlider.slideOut();
		synchronized (sliders) {
			sliders.add(choiceSlider);
		
		}
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
			}
	}

	public void showBuildInfo(GameUnit unit) {
		closeVSlide();
		int totalheight = 20;
		try {
			buildSlider = new UISlider(0, 0, WW / 8, 50, true,
					res.getImage("slider_vertical_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < unit.data.buildIDs.size(); i++) {
			try {
				buildSlider.addChild(new UIText(0, totalheight,
						unit.data.buildIDs.get(i)));
				buildSlider.addChild(new UIText(0, totalheight+10,(res.getStaticObject(unit.data.buildIDs.get(i)).metalCost)+" metal"));
				buildSlider.addChild(new UIButton(0, totalheight+20, toEvent(unit.data.buildIDs.get(i)), res
								.getImage(unit.data.buildIDs.get(i))));
				totalheight += res.getImage(unit.data.buildIDs.get(i))
						.getHeight() + 50;
				buildSlider.height = totalheight;
				buildSlider.y -= res.getImage(unit.data.buildIDs.get(i))
						.getHeight() - 50;
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
		closeVSlide();
		closeHSlide();
		int totalheight = 20;
		try {
			buildSlider = new UISlider(0, 0, WW / 8, 50, true,
					res.getImage("slider_vertical_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < unit.data.buildIDs.size(); i++) {
			try {
				buildSlider.addChild(new UIText(0, totalheight,res.getUnit(unit.data.buildIDs.get(i)).metalCost+" metal"));
				buildSlider.addChild(new UIText(0, totalheight+20,res.getUnit(unit.data.buildIDs.get(i)).powerUsage+" power"));
				
				buildSlider.addChild(new UIText(0, totalheight+40,
						unit.data.buildIDs.get(i)));
				buildSlider.addChild(new UIButton(0, totalheight+60,
						toEvent(unit.data.buildIDs.get(i)), res
								.getImage(unit.data.buildIDs.get(i)+"_1")));
				totalheight+=80+res.getImage(unit.data.buildIDs.get(i)+"_1").getHeight();
				buildSlider.height = totalheight;
				buildSlider.y -= (80+res.getImage(unit.data.buildIDs.get(i)+"_1").getHeight());
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

		int totalwidth = 75;
		closeHSlide();

		try {
			selectionInfo = new UISlider(miniMap.x, WH * 8 / 10, 0,
					WH * 2 / 10, false, res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		currentunit = unit;
		selectionInfo.addChild(new UIText(10, 15, (unit.name))); //
		//selectionInfo.addChild(new UIText(10, 35, (unit.)));

		selectionInfo.addChild(new UIText(10, 55,(unit.getHP() + "/" + unit.data.HP)));
		selectionInfo.addChild(new UIText(10, 75,(unit.getAP() + "/" + unit.data.AP)));

		for (int i = 0; i < unit.data.uiActions.size(); i++) {
			try {
				selectionInfo.addChild(new UIButton(totalwidth,	(selectionInfo.height - res.getImage(
								unit.data.uiActions.get(i).e.toString())
								.getHeight()) / 4, new UIEvent(unit.data.uiActions.get(i).e),
						res.getImage(unit.data.uiActions.get(i).e.toString())));
				totalwidth += res.getImage(
						unit.data.uiActions.get(i).e.toString()).getWidth() + 25;
				selectionInfo.width = totalwidth;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (!unit.data.buildIDs.isEmpty())
			try {
				 
				selectionInfo.addChild(new UIButton(totalwidth,
						(selectionInfo.height - res.getImage(UIEvent.Event.BUILD.toString())
								.getHeight()) / 4, new UIEvent(UIEvent.Event.BUILD), res
								.getImage(UIEvent.Event.BUILD.toString())));
				totalwidth+= res.getImage(UIEvent.Event.BUILD.toString()).getWidth()+25;
				selectionInfo.width =totalwidth;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
			
		selectionInfo.slideOut();

		synchronized (sliders) {
			sliders.add(selectionInfo);
		}
	}

	public void showStaticObjectInfo(GameStaticObject unit) {
		closeVSlide();
		closeHSlide();
		currentunit = null;
		int totalwidth = 75;
		synchronized (sliders) {
			sliders.remove(selectionInfo);
		}

		try {
			selectionInfo = new UISlider(miniMap.x, WH * 8 / 10,0,
					WH * 2 / 10, false, res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		curstatobj = unit;
		selectionInfo.addChild(new UIText(10, 15, (unit.name))); //
		if (curstatobj.data.id.equals("MINE")){
			selectionInfo.addChild(new UIText(10, 35, "Metal per turn: " + game.getMineIncome(curstatobj)));	
		}

		if (!unit.data.buildIDs.isEmpty())
			try {				
				selectionInfo.addChild(new UIButton(totalwidth,
						(selectionInfo.height - res.getImage(UIEvent.Event.BUILD.toString())
								.getHeight()) / 4, new UIEvent(UIEvent.Event.BUILD), res
								.getImage(UIEvent.Event.BUILD.toString())));
				totalwidth+=res
						.getImage(UIEvent.Event.BUILD.toString()).getWidth();
				selectionInfo.width = totalwidth;
			} catch (ResNotFoundException e) {
				e.printStackTrace();
			}
			else selectionInfo.width=150;
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
			
			if (Math.abs(ev.getX() - pressStartX) > 5
					&& Math.abs(ev.getY() - pressStartY) > 5) {
			
				UIEvent uiev = onClick(ev);
				if(uiev != null) return new UIEvent(UIEvent.Event.HANDLED);
			}
			// drag
		} else if (ev.getID() == MouseEvent.MOUSE_RELEASED && !leftClick) {
			if (Math.abs(ev.getX() - pressStartX) < 5
					&& Math.abs(ev.getY() - pressStartY) < 5) {
				UIEvent uiev = onClick(ev);
				if(uiev != null) return new UIEvent(UIEvent.Event.HANDLED);
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

			if (event!=null&&event.actor!=null){
				if(event.actor instanceof GameUnit) 
				showUnitInfo((GameUnit) event.actor);
				else showStaticObjectInfo((GameStaticObject)event.actor);
			}
			if (buildSlider != null && !buildSlider.picked(ev.getX(), ev.getY()))
				sliders.remove(buildSlider);

			if (ev.getX() > miniMap.x && ev.getX() < miniMap.x + miniMap.width
					&& ev.getY() < miniMap.height + miniMap.y
					&& ev.getY() > miniMap.y) {

				miniMap.pickedcrd(ev);

				return (new UIEvent(UIEvent.Event.HANDLED));
			}
			
			if (event != null && event.e == UIEvent.Event.BUILD) {
				if (currentunit != null)
					showBuildInfo(currentunit);
				else
					showBuildInfo(curstatobj);
				return (new UIEvent(UIEvent.Event.HANDLED));
			}
			
			if (endButton.picked(ev.getX(), ev.getY())) {
				closeVSlide();
				closeHSlide();
				return (new UIEvent(UIEvent.Event.END_TURN));
			}
		}
		if (choiceSlider !=null && !choiceSlider.picked(ev.getX(),ev.getY())) 		
		{closeHSlide();
		closeVSlide();}
		return event;
	}

}
