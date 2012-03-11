package com.team2.civ.Game;

import com.team2.civ.UI.UI;

public class HumanPlayerController {
	private static final boolean FOW_ON = true;
	
	private int offsetX = 0;
	private int offsetY = 0;

	private int pressStartX;
	private int pressStartY;

	private int lastMouseX;
	private int lastMouseY;

	private static final double SCALE_MAX = 1.0;
	private static final double SCALE_MIN = 0.2;
	private static final double ZOOM_DELTA = 0.2;
	private static final double ZOOM_FACTOR = 0.02;

	private double scale = 1.0f;
	private double oldScale = scale;
	private boolean zoomingIn = false;
	private boolean zoomingOut = false;

	private boolean leftClick = true;
	
	private UI ui;
}
