package com.team2.civ;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import com.team2.civ.Game.GameController;

public class MouseMotionInput implements MouseMotionListener {
	private GameController game;
	
	public MouseMotionInput(GameController game) {
		this.game = game;
	}
	@Override
	public void mouseDragged(MouseEvent ev) {
		game.onMouseInput(ev);
	}

	@Override
	public void mouseMoved(MouseEvent ev) {
		//Not used for now
	}
}
