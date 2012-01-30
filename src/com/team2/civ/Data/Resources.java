package com.team2.civ.Data;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Resources {
	private HashMap<String, BufferedImage> images;
	
	public BufferedImage getImage(String tag) throws Exception {
		BufferedImage rtn = images.get(tag);
		if(rtn == null)
			throw new Exception("Image " + tag + " does not exist.");
		
		return rtn;
	}
	
	public static final BufferedImage get(final File file, GraphicsConfiguration config) {
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
