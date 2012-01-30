package com.team2.civ.Map;

import java.awt.image.BufferedImage;

import com.team2.civ.Data.AnimData;
import com.team2.civ.Map.MapObjectImage;

public class Animation {
	private static final int FRAME_TIME = 5;
	
	private BufferedImage frames[];
	private int length;
	private int currentFrame = 0;
	private int loops = 0;
	private int loopCount;
	private boolean complete = false;
	
	public Animation(AnimData data) {
		this.frames = data.frames;
		this.length = data.length;
		this.loopCount = data.loopCount;
	}

	public void reset() {
		loops = 0;
		currentFrame = 0;
		complete = false;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public int getFrameNumber() {
		return currentFrame;
	}
	
	public void update(MapObjectImage img, long gameTime)
	{
		if(gameTime % FRAME_TIME == 0)
			currentFrame++;
		if(currentFrame > length - 1) {
			currentFrame = 0;
			loops++;
			if(loopCount != -1 && loops > loopCount - 1)
				complete = true;
		}
		
		img.setBitmap(frames[currentFrame]);
	}
}