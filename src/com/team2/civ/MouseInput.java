package com.team2.civ;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.team2.civ.Game.GameController;

/* SENDS MOUSE INPUT TO THE GAME
 * 
 *	Could need mouse moved for highlighting
 */

public class MouseInput implements MouseListener {
	private GameController game;
	
	public MouseInput(GameController game) {
		this.game = game;
	}

	@Override
	public void mouseClicked(MouseEvent ev) {
		//game.onMouseInput(ev);
	}

	@Override
	public void mouseEntered(MouseEvent ev) {
		//Called the mouse enters game canvas
		//game.onMouseInput(ev);
	}

	@Override
	public void mouseExited(MouseEvent ev) {
		//Called the mouse exits game canvas
		//game.onMouseInput(ev);
	}

	@Override
	public void mousePressed(MouseEvent ev) {
		game.onMouseInput(ev);
	}

	@Override
	public void mouseReleased(MouseEvent ev) {
		game.onMouseInput(ev);
	}
}
