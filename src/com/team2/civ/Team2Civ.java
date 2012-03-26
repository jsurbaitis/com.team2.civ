package com.team2.civ;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.team2.civ.AI.Generator;
import com.team2.civ.Data.Resources;
import com.team2.civ.Game.GameController;

/* MAIN ENTRY POINT - There should be no need to modify this file
 * 
 * Creates the window and main update thread
 * Handles the framerate limit
 * Calls game updateAll method
 * 
 * Need to separate game logic and graphics thread so that logic isn't slowed down by fps limits?
 */

public class Team2Civ extends Thread {
	public static final int WINDOW_WIDTH = 1024;
	public static final int WINDOW_HEIGHT = 768;
	public static final int FPS_LIMIT = 60;
	private static final long FPS_WAIT = (long) (1.0 / FPS_LIMIT * 1000);
	
	public static final boolean AI_MODE = true;
	public static final boolean DEBUG_OUTPUT = true;
	
	private long timeStartMillis;

	private GraphicsConfiguration config =
    		GraphicsEnvironment.getLocalGraphicsEnvironment()
							   .getDefaultScreenDevice()
							   .getDefaultConfiguration();
	
	private boolean isRunning = true;
	private JFrame frame;
	private BufferStrategy strategy;
	private Graphics2D graphics;
	private Graphics2D backgroundGraphics;
	private BufferedImage background;
	private Canvas canvas;
	
	private GameController game;

	public Team2Civ() {
		frame = new JFrame();
		frame.addWindowListener(new WindowClose());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

		Resources.init(config);

        canvas = new Canvas(config);
        canvas.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        game = new GameController();
        game.runGame();
        
        canvas.addKeyListener(new KeyboardInput(game));
        canvas.addMouseListener(new MouseInput(game));
        canvas.addMouseMotionListener(new MouseMotionInput(game));
        canvas.addMouseWheelListener(new MouseWheelInput(game));
        
        frame.add(canvas, 0);
        background = create(WINDOW_WIDTH, WINDOW_HEIGHT, false);
        canvas.createBufferStrategy(2);
        do {
                strategy = canvas.getBufferStrategy();
        } while (strategy == null);

        start();
	}

	public final BufferedImage create(final int width, final int height, final boolean alpha) {
		return config.createCompatibleImage(width, height, alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
	}
	
	public void run() {
		timeStartMillis = System.currentTimeMillis();
		backgroundGraphics = (Graphics2D) background.getGraphics();
		while(isRunning) {
			long renderStart = System.nanoTime();
			
			game.update(System.currentTimeMillis() - timeStartMillis);
			
			do {
				Graphics2D g = getBuffer();
				//Break isRunning loop here if bool has changed?
				game.draw(backgroundGraphics);
				
				g.drawImage(background, 0, 0, null);
				g.dispose();
			} while(!updateScreen());
			
			long renderTime = (System.nanoTime() - renderStart) / 1000000;
            try {
                    Thread.sleep(Math.max(0, FPS_WAIT - renderTime));
            } catch (InterruptedException e) {
                    Thread.interrupted();
                    break;
            }
            //System.out.println(""+(1000.0/renderTime));
		}
		frame.dispose();
	}
	
	private Graphics2D getBuffer() {
        if(graphics == null) {
        	try {
            	graphics = (Graphics2D) strategy.getDrawGraphics();
        	} catch (IllegalStateException e) {
        		return null;
        	}
        }
        return graphics;
    }
	
	private boolean updateScreen() {
        graphics.dispose();
        graphics = null;
        try {
        	strategy.show();
        	Toolkit.getDefaultToolkit().sync();
        	return (!strategy.contentsLost());
        } catch (NullPointerException e) {
                return true;
        } catch (IllegalStateException e) {
                return true;
        }
    }
	
	private class WindowClose extends WindowAdapter {
        @Override
    	public void windowClosing(final WindowEvent e) {
            isRunning = false;
        }
    }
	
	public static void main(String args[]) {
		if(!AI_MODE)
			new Team2Civ();
		else {
			GraphicsConfiguration config =
		    		GraphicsEnvironment.getLocalGraphicsEnvironment()
									   .getDefaultScreenDevice()
									   .getDefaultConfiguration();
			
			Resources.init(config);
			
			int generations = Integer.parseInt(args[0]);
			int populationSize = Integer.parseInt(args[1]);

			//Generator g = new Generator(generations, populationSize);
			Generator g = new Generator(generations, new File("population_metadata.xml"));
			g.generate();
		}
	}
}
