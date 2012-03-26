package com.team2.civ.UI;

import com.team2.civ.Map.MapObject;

public class UIEvent {
	public static enum Event {
		BUILD, ACTION_ATTACK, BUILD_CITY, BUILD_WORKER, BUILD_POWERPLANT, BUILD_FORTIFICATION, 
		ACTION_DESTROY, ACTION_FORTIFY, BUILD_MINE, HANDLED, END_TURN, TARGET_CHANGED, BUILD_TANK,
		BUILD_ANTIAIR, BUILD_AIR;
	}

	public MapObject actor;
	public Event e;

	public UIEvent(Event ev, MapObject ob) {
		this.e = ev;
		actor = ob;
	}
	
	public UIEvent(Event ev) {
		this.e = ev;
	}
	
	public UIEvent(MapObject ob){
		e = Event.TARGET_CHANGED;
		actor = ob;
	}
}
