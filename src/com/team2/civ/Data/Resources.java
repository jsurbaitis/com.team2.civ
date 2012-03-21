package com.team2.civ.Data;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.team2.civ.UI.UIEvent;

public class Resources {
	public static int TILE_WIDTH, TILE_HEIGHT;
	
	private static GraphicsConfiguration config;
	private static Resources instance;
	
	private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private HashMap<String, GameUnitData> units = new HashMap<String, GameUnitData>();
	private HashMap<String, GameStaticObjectData> staticObjs = new HashMap<String, GameStaticObjectData>();

	public static void init(GraphicsConfiguration config) {
		Resources.config = config;
	}
	
	private Resources() {
		loadImages(config);
		
		try {
			parseUnitFile();
			parseStaticObjectsFile();
		} catch (IOException e) {
			System.out.println("error in the scripts!");
			e.printStackTrace();
		}
	}
	
	public static Resources getInstance() {
		if(config == null) return null;
		
		if(instance == null) 
			instance = new Resources();
		
		return instance;
	}

	public GameStaticObjectData getStaticObject(String tag) throws ResNotFoundException {
		GameStaticObjectData rtn = staticObjs.get(tag);
		if(rtn == null)
			throw new ResNotFoundException(tag, "GameStaticObjectData");
		
		return rtn;
	}
	
	public void parseStaticObjectsFile() throws IOException {
		Scanner in = new Scanner(new FileInputStream("scripts/StaticObjects.txt"));
		
		while(in.hasNext()) {
			parseStaticObjectData(in);
		}
	}
	
	private void parseStaticObjectData(Scanner in) throws IOException {
		String str = in.next();
		GameStaticObjectData data = new GameStaticObjectData(str);
		str = in.next();
		while(!str.equals("ENDOBJ")) {
			if(str.equals("name"))
				parseStaticObjectname(in, data);
			else if(str.equals("build"))
				parseStaticObjectBuild(in, data);
			else
				parseStaticObjectVariable(str, in, data);
			
			str = in.next();
		}
		staticObjs.put(data.id, data);
				
	}
	
	private void parseStaticObjectVariable(String varName, Scanner in, GameStaticObjectData u) throws IOException {
		if(varName.equals("MetalCost")) u.metalCost = in.nextInt();
		else if(varName.equals("DefensiveBonus")) u.defensiveBonus = in.nextInt();
		else if(varName.equals("MovementCose")) u.moveCost = in.nextInt();
		else if(varName.equals("FoWRange")) u.fowRange = in.nextInt();
		else if(varName.equals("Destructible")) u.destructible = in.nextBoolean();
		else if(varName.equals("Capturable")) u.capturable = in.nextBoolean();
		else if(varName.equals("PowerGiven")) u.powerGiven = in.nextInt();
	}
	
	private void parseStaticObjectname(Scanner in, GameStaticObjectData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			u.names.add(s.replace('_', ' '));
			s = in.next();
		}
	}
	
	private void parseStaticObjectBuild(Scanner in, GameStaticObjectData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			u.buildIDs.add(s);
			s = in.next();
		}
	}
	
// -------------------------------------------------------------------------	
	
	public GameUnitData getUnit(String tag) throws ResNotFoundException {
		GameUnitData rtn = units.get(tag);
		if(rtn == null)
			throw new ResNotFoundException(tag, "GameUnitData");
		
		return rtn;
	}
	
	public void parseUnitFile() throws IOException {
		Scanner in = new Scanner(new FileInputStream("scripts/Units.txt"));
		
		while(in.hasNext()) {
			parseUnitData(in);
		}
	}
	
	private void parseUnitData(Scanner in) throws IOException {
		String str = in.next();
		GameUnitData data = new GameUnitData(str);
		str = in.next();
		while(!str.equals("ENDUNIT")) {
			if(str.equals("DmgModifier"))
				parseUnitDmgModifiers(in, data);
			else if(str.equals("UIActions"))
				parseUnitUIActions(in, data);
			else if(str.equals("build"))
				parseUnitBuild(in, data);
			else if(str.equals("name"))
				parseUnitName(in, data);
			else
				parseUnitVariable(str, in, data);
			
			str = in.next();
		}
		units.put(data.id, data);
	}
	
	private void parseUnitName(Scanner in, GameUnitData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			u.names.add(s.replace('_', ' '));
			s = in.next();
		}
	}
	
	private void parseUnitVariable(String varName, Scanner in, GameUnitData u) throws IOException {
		if(varName.equals("HP")) u.HP = in.nextInt();
		else if(varName.equals("AP")) u.AP = in.nextInt();
		else if(varName.equals("BaseDmg")) u.baseDmg = in.nextInt();
		else if(varName.equals("Range")) u.range = in.nextInt();
		else if(varName.equals("FoWRange")) u.fowRange = in.nextInt();
		else if(varName.equals("MetalCost")) u.metalCost = in.nextInt();
		else if(varName.equals("PowerUsage")) u.powerUsage = in.nextInt();
	}

	private void parseUnitDmgModifiers(Scanner in, GameUnitData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			u.dmgModifiers.put(s,in.nextInt());
			s = in.next();
		}
	}
	
	private void parseUnitUIActions(Scanner in, GameUnitData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			if(s.equals("ATTACK"))
				u.uiActions.add(new UIEvent(UIEvent.Event.ACTION_ATTACK));
			if(s.equals("DESTROY"))
				u.uiActions.add(new UIEvent(UIEvent.Event.ACTION_DESTROY));
			if(s.equals("FORTIFY"))
				u.uiActions.add(new UIEvent(UIEvent.Event.ACTION_FORTIFY));
			
			s = in.next();
		}
	}
	
	private void parseUnitBuild(Scanner in, GameUnitData u) throws IOException {
		String s = in.next();
		s = in.next();
		while(!s.equals("}")) {
			u.buildIDs.add(s);
			s = in.next();
		}
	}
	
	private void loadImages(GraphicsConfiguration config) {
		File imageFolder = new File("assets/");
		for(File f: imageFolder.listFiles())
			images.put(f.getName().replace(".png", ""), Resources.get(new File("assets/"+f.getName()), config));

		BufferedImage tile = images.get("tile_grass");
		TILE_WIDTH = tile.getWidth();
		TILE_HEIGHT = tile.getHeight();
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
