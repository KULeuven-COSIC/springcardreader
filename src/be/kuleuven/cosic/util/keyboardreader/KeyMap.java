/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */

package be.kuleuven.cosic.util.keyboardreader;


public interface KeyMap {
	public String getKeyString(int keyCode, int control);
	
	public boolean isActionKey(int keyCode);
}
