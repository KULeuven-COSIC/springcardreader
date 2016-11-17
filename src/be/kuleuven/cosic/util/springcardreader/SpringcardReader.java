/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */

package be.kuleuven.cosic.util.springcardreader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import com.codeminders.hidapi.HIDDevice;

import be.kuleuven.cosic.util.keyboardreader.KeyEvent;
import be.kuleuven.cosic.util.keyboardreader.KeyEventListener;
import be.kuleuven.cosic.util.keyboardreader.KeyMap;
import be.kuleuven.cosic.util.keyboardreader.KeyboardReader;
import be.kuleuven.cosic.util.springcardreader.Springcard.Led;

public class SpringcardReader implements KeyEventListener {
	private KeyboardReader reader;
	private String data = "";
	private KeyMap map;
	private HIDDevice dev;

	public SpringcardReader(HIDDevice dev, KeyMap map) throws IOException {
		this.map = map;
		this.dev = dev;
		reader = new KeyboardReader(dev, false);

		reader.addManyKeyEventListener(this);
	}

	public void read() throws IOException {
		reader.readKeyboardDevices(map);
	}
	
	public HIDDevice getDevice(){
		return dev;
	}

	@Override
	public void manyKeyEvent(KeyEvent event) {
		if (event.isModifierKey()) {
			return;
		}
		if (!event.getKeyType().equals("KEY_PRESSED")) {
			return;
		}
		if (event.getKeystring() == null) {
			return;
		}
		data = data + event.getKeystring();

		if (event.getKeystring() == "\n") {
			String cmd = data.trim();
			if(cmd.length() > 0){
				notifyListeners(new SpringcardEvent(this,cmd));
			}
			data = "";
		}
	}

	public void setBuzzer(short time) throws IOException {
		Springcard.setBuzzer(dev, time);
	}

	public void setLeds(Led r, Led g, Led b) throws IOException {
		Springcard.setLeds(dev, r, g, b);
	}

	public void setLedsT(Led r, Led g, Led b, short time) throws IOException {
		Springcard.setLedsT(dev, r, g, b, time);
	}
	
	protected HashSet<SpringcardEventListener> listeners = new HashSet<>();
	
	public void addListener(SpringcardEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SpringcardEventListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(SpringcardEvent event) {
		Iterator<SpringcardEventListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			SpringcardEventListener listener = iter.next();
			listener.springcardEvent(event);

		}
	}
}
