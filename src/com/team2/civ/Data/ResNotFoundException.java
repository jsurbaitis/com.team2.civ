package com.team2.civ.Data;

public class ResNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ResNotFoundException(String resID, String resType) { 
		super(resType + " " + resID + " not found"); 
	}

}
