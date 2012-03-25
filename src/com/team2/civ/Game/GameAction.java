package com.team2.civ.Game;

import com.team2.civ.Map.MapObject;

public class GameAction {
	public static enum Event {
		ACTION_ATTACK, ACTION_CHECK_ATTACK, BUILD_CITY, BUILD_WORKER, BUILD_POWERPLANT, BUILD_FORTIFICATION,
		ACTION_DESTROY_SELF, ACTION_FORTIFY, BUILD_MINE, END_TURN, ACTION_MOVE, ACTION_ATTACK_MOVE,
		ACTION_CAPTURE_TARGET, ACTION_DESTROY_TARGET, BUILD_ANTIAIR, BUILD_AIR, BUILD_TANK,
		NULL_ACTION
	}
	
	public static enum ZeroAgentEvent {
		END_TURN, NULL_ACTION
	}
	
	public static enum OneAgentEvent {
		BUILD_CITY, BUILD_WORKER, BUILD_ANTIAIR, BUILD_AIR, BUILD_TANK, BUILD_POWERPLANT, BUILD_FORTIFICATION, ACTION_DESTROY_SELF,
		ACTION_FORTIFY, BUILD_MINE, ACTION_CHECK_ATTACK
	}
	
	public static enum TwoAgentEvent {
		ACTION_ATTACK, ACTION_MOVE, ACTION_ATTACK_MOVE, ACTION_DESTROY_TARGET, ACTION_CAPTURE_TARGET
	}
	
	public MapObject actor, target;
	public Event event;
	public final Player performer;

	public GameAction(ZeroAgentEvent e, Player p){
		this.performer = p;
		this.event = Event.valueOf(e.toString());
	}

	public GameAction(OneAgentEvent e, Player p, MapObject f){
		this.performer = p;
		this.event = Event.valueOf(e.toString());
		this.actor = f;
	}
	
	public GameAction(TwoAgentEvent e, Player p, MapObject f1, MapObject f2){
		this.performer = p;
		this.event = Event.valueOf(e.toString());
		this.actor = f1;
		this.target = f2;
	}
	
	public String toString(){
		return this.event.toString();
	}
}
