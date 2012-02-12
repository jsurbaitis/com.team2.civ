package com.team2.civ.Data;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Resources {
	private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	
	public Resources(GraphicsConfiguration config) {
		loadImages(config);
	}
	
	private void loadImages(GraphicsConfiguration config) {
		images.put("highlight", Resources.get(new File("assets/highlight.png"), config));
		images.put("move_test", Resources.get(new File("assets/move_test.png"), config));
		images.put("wall", Resources.get(new File("assets/wall.png"), config));
		images.put("tile_grass", Resources.get(new File("assets/tile_grass.png"), config));
		images.put("water", Resources.get(new File("assets/water.png"), config));
	}
	
	public BufferedImage getImage(String tag) throws ResNotFoundException {
		BufferedImage rtn = images.get(tag);
		if(rtn == null)
			throw new ResNotFoundException(tag, "Image");
		
		return rtn;
	}
	
	private static final BufferedImage get(final File file, GraphicsConfiguration config) {
    	try {
        	return compatible(ImageIO.read(file), config);
    	} catch (IOException e) {
    		System.out.println("IMG_READ Exception - " + file.getName());
    		return null;
        }
	}
	
	private static final BufferedImage compatible(BufferedImage image, GraphicsConfiguration config) {
        if (image.getColorModel().equals(config.getColorModel())) {
                return image;
        } else {
                BufferedImage newImage = config.createCompatibleImage(image
                                .getWidth(), image.getHeight(), image.getColorModel()
                                .getTransparency());
                
                Graphics2D g = (Graphics2D) newImage.getGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                return newImage;
        }
	}
}
