package com.team2.civ;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.team2.civ.Game.GameController;

public class MouseWheelInput implements MouseWheelListener {
	private GameController game;
	
	public MouseWheelInput(GameController game) {
		this.game = game;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent ev) {
		game.onMouseWheelInput(ev);
	}
}
