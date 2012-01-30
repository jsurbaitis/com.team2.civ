package com.team2.civ;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.team2.civ.Game.GameController;

/* SENDS KEYBOARD INPUT TO THE GAME
 * 
 * This will need to be extended if we will need
 * 	to differentiate between long and short press
 */

public class KeyboardInput implements KeyListener {
	private GameController game;
	
	public KeyboardInput(GameController game) {
		this.game = game;
	}

	@Override
	public void keyPressed(KeyEvent ev) {
		game.onKeyboardInput(ev);
	}

	@Override
	public void keyReleased(KeyEvent ev) {
		game.onKeyboardInput(ev);
	}

	@Override
	public void keyTyped(KeyEvent ev) {
		//Don't think we need this
	}

}
