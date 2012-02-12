package com.team2.civ.UI;

import java.awt.Graphics2D;
import java.util.ArrayList;

import com.team2.civ.Team2Civ;
import com.team2.civ.Data.ResNotFoundException;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameUnit;

public class UI {
	private Resources res;

	final int WW=Team2Civ.WINDOW_WIDTH;
	final int WH=Team2Civ.WINDOW_HEIGHT;
	private ArrayList<UISlider> sliders = new ArrayList<UISlider>();
	
	private UISlider selectionInfo;

	public static enum UIEvent {
		ACTION_ATTACK, BUILD_CITY, ACTION_DESTROY, ACTION_FORTIFY, BUILD_LUMBER, BUILD_FARM, HANDLED
	}
	
	public UI(Resources res) {
		this.res = res;
	}
	
	public void update(long gameTime) {
		for(UISlider s: sliders)
			s.update(gameTime);
	}
	
	public void draw(Graphics2D g) {
		for(UISlider s: sliders) {
			s.draw(g);
		}
	}
	
	public void showUnitInfo(GameUnit unit){
		sliders.remove(selectionInfo);
		try {
			selectionInfo = new UISlider(WW,WH-WH*2/10,WW*6/10,WH*2/10,false, res.getImage("slider_horizontal_bg"));
		} catch (ResNotFoundException e) {
			e.printStackTrace();
		}
		
		selectionInfo.addChild(new UIText(0, 0, (unit.HP+"/"+unit.data.HP)));
		selectionInfo.addChild(new UIText(0, 0, (unit.HP+"/"+unit.data.HP)));
		selectionInfo.addChild(new UIText(0, 0, (unit.AP+"/"+unit.data.AP)));
	//	for (int i=0; i<unit.data.uiActions.size();i++)
//		{
//		 unitbar.addChild(initBtn(WW*(i*10+40)/100,WH/20,WW/10,WH/10,unit.data.uiActions.get(i)));
//		} 
		
		selectionInfo.slideOut();
		sliders.add(selectionInfo);
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


}
