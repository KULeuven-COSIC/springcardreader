/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */

package be.kuleuven.cosic.util.springcardreader;

import java.io.IOException;
import java.util.Arrays;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;

public class Springcard {
	protected static final byte ACTION_GET_PROTOCOL_INFO = 0x01;
	protected static final byte ACTION_GET_VARIABLE = 0x02;
	protected static final byte GET_VARIABLE_ITEM_LAST_ERROR = 0x01;
	protected static final byte ACTION_GET_CONST_ASCII = 0x04;
	protected static final byte GET_CONST_ITEM_VENDOR_NAME = 0x01;
	protected static final byte GET_CONST_ITEM_PRODUCT_NAME = 0x02;
	protected static final byte GET_CONST_ITEM_SERIAL_NUMBER = 0x03;
	protected static final byte GET_CONST_ITEM_PID_VID = 0x04;
	protected static final byte GET_CONST_ITEM_PRODUCT_VERSION = 0x05;
	protected static final byte ACTION_GET_FEED = 0x20;
	protected static final byte ACTION_SET_BEHAVIOUR = (byte) 0x80;
	protected static final byte SET_BEHAVIOUR_ITEM_STOP_READER = 0x10;
	protected static final byte SET_BEHAVIOUR_ITEM_START_READER = 0x11;
	protected static final byte SET_BEHAVIOUR_ITEM_STOP_KEYBOARD = 0x20;
	protected static final byte SET_BEHAVIOUR_ITEM_START_KEYBOARD = 0x21;
	protected static final byte SET_BEHAVIOUR_ITEM_APPLY_CONFIG = (byte) 0xC0;
	protected static final byte SET_BEHAVIOUR_ITEM_RESET = (byte) 0xD0;
	protected static final byte ACTION_SET_LEDS = (byte) 0x88;
	protected static final byte ACTION_SET_BUZZER = (byte) 0x8A;
	protected static final byte ACTION_SET_FEED = (byte)0xA0;
	protected static final byte ACTION_SET_MIFARE_KEY_E2 = (byte)0xB0;
	
	public enum Led {
		OFF (0x00),
		ON (0x01),
		SLOW (0x02),
		AUTO (0x03),
		FAST (0x04),
		HEART (0x05); 
		
		private byte data;
		private Led(byte data){
			this.data = data;
		}
		private Led(int data){
			this.data = (byte) data;
		}
		
		public byte getData(){
			return data;
		}
	}
	
	public enum KeyboardLayout {
		QWERTY (0x00),
		AZERTY_NUMPAD (0x01),
		QWERTZ (0x02),
		AZERTY_SHIFT (0x03);
		
		private byte data;
		private KeyboardLayout(byte data){
			this.data = data;
		}
		private KeyboardLayout(int data){
			this.data = (byte) data;
		}
		
		public byte getData(){
			return data;
		}
		
	}
	
	protected static final int BUFFER_SIZE = 65;
	
	protected static void set(HIDDevice dev, byte action, byte item, byte [] data) throws IOException{
		byte[] buf = new byte[BUFFER_SIZE];
		
		buf[0] = 0x00;
		buf[1] = (byte) (3+data.length);
		buf[2] = 0x00;
		buf[3] = (byte) (0x80 | action);
		buf[4] = item;
		
		for(int i=0;i<data.length;i++)
			buf[5+i] = data[i];
		
		exchange(dev,buf);
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	protected static byte [] get(HIDDevice dev, byte action, byte item) throws IOException{
		byte[] buf = new byte[BUFFER_SIZE];
		
		buf[0] = 0x00;
		buf[1] = (byte) (3);
		buf[2] = 0x00;
		buf[3] = (byte) (0x7F & action);
		buf[4] = item;
		
		
		int rc = exchange(dev,buf);
		
		if(rc < 0){
			return new byte[0];
		}
		
		if(buf[2] != 0){
			throw new IOException("Reader raised error "+rc);
		}
		
		if(buf[1] > 3){
			rc = buf[1] - 3;
			
			return Arrays.copyOfRange(buf, 5, 5+rc);
		}
		
		return new byte[0];
	}
	
	protected static int exchange(HIDDevice dev, byte []buf) throws IOException{
		dev.sendFeatureReport(buf);
		
		return dev.getFeatureReport(buf);
	}
	
	protected static byte[] readRegister(HIDDevice dev, byte addr) throws IOException {
		return get(dev, ACTION_GET_FEED, addr);
	}

	static void setLeds(HIDDevice dev, Led r, Led g, Led b) throws IOException{
		byte[] buf = new byte[4];
		buf[0] = r.getData(); buf[1] = g.getData(); buf[2] = b.getData(); buf[3] = 0x00;
		
		set(dev,ACTION_SET_LEDS,(byte) 0x00,buf);
	}
	
	static void setBuzzer(HIDDevice dev, short time) throws IOException {
		byte[] buf = new byte[2];
		
		buf[0] = (byte) (time / 0x100);
		buf[1] = (byte) (time);
		
		set(dev,ACTION_SET_BUZZER,(byte) 0x00,buf);
	}
	
	
	static void setLedsT(HIDDevice dev, Led r, Led g, Led b, short time) throws IOException{
		byte[] buf = new byte[6];
		buf[0] = r.getData(); buf[1] = g.getData(); buf[2] = b.getData(); buf[3] = 0x00;
		
		buf[4] = (byte) (time / 0x100);
		buf[5] = (byte) (time);
		
		set(dev,ACTION_SET_LEDS,(byte) 0x00,buf);
	}
	
	public static boolean isSpringcardDevice(HIDDeviceInfo info){
		if(info.getVendor_id() == 0x1c34 && info.getProduct_id() == 0x7241){
			return true;
		}
		return false;
	}
	
	public static KeyboardLayout getKeyboardLayout(HIDDevice dev) throws IOException{
		byte [] register = readRegister(dev, (byte) 0xA0);
		
		if(register.length != 1)
			throw new IOException("Read error");
		
		for(KeyboardLayout val : KeyboardLayout.class.getEnumConstants()){
			if(val.getData() == register[0])
				return val;
		}
		
		throw new IOException("Unknown keyboard layout");
	}
}
