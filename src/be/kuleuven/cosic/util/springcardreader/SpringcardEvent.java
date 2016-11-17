/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */

package be.kuleuven.cosic.util.springcardreader;

public class SpringcardEvent {
	private String data;
	private SpringcardReader origin;
	
	public SpringcardEvent (SpringcardReader origin, String data){
		this.origin = origin;
		this.data = data;
	}
	
	public SpringcardReader getOrigin(){
		return origin;
	}
	
	public String getData(){
		return data;
	}
}
