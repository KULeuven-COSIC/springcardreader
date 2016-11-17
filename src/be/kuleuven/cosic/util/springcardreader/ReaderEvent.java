/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */

package be.kuleuven.cosic.util.springcardreader;

public class ReaderEvent {
	private SpringcardReader reader;
	public enum Type {
		CONNECT, DISCONNECT
	}
	
	private Type type;
	
	public ReaderEvent(SpringcardReader reader, Type type){
		this.type = type;
		this.reader = reader;
	}
	
	public SpringcardReader getReader(){
		return reader;
	}
	
	public Type getType(){
		return type;
	}
	
	
}
