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
import com.codeminders.hidapi.HIDDeviceInfo;

import be.kuleuven.cosic.util.keyboardreader.KeyMap;
import be.kuleuven.cosic.util.keyboardreader.KeyMapAzerty;
import be.kuleuven.cosic.util.keyboardreader.KeyMapQwerty;
import be.kuleuven.cosic.util.keyboardreader.PersistentKeyboardMonitor;
import be.kuleuven.cosic.util.springcardreader.ReaderEvent.Type;
import be.kuleuven.cosic.util.springcardreader.Springcard.KeyboardLayout;

/**
 * This class opens all springcard devices and tries to read them.
 * It can handle disconnects/connects of devices and recover from these.
 * It gracefully handles all exceptions.
 * 
 * 
 * Every readers runs in a separate thread.
 * There is a general thread that monitors new reader connections.
 *
 */
public class PersistentSpringcardReader extends PersistentKeyboardMonitor implements SpringcardEventListener {
	
	public PersistentSpringcardReader(int pollingInterval){
		super(pollingInterval);
	}
	
	
	

	@Override
	public void springcardEvent(SpringcardEvent event) {
		notifyListeners(event);		
	}

	@Override
	protected boolean deviceConnect(HIDDeviceInfo info) {
		return Springcard.isSpringcardDevice(info);
	}

	@Override
	protected void useNewDevice(HIDDeviceInfo info, HIDDevice device) throws IOException {
		
		
		KeyboardLayout keybLayout = Springcard.getKeyboardLayout(device);

		
		KeyMap keyMap = new KeyMapQwerty();
		
		if(keybLayout == KeyboardLayout.AZERTY_NUMPAD || keybLayout == KeyboardLayout.AZERTY_SHIFT){
			keyMap = new KeyMapAzerty();
		}
		
		SpringcardReader reader = new SpringcardReader(device, keyMap);
		reader.addListener(this);

		notifyReaderListeners(new ReaderEvent(reader,Type.CONNECT));
		
		
		reader.read();
	}
	
	protected void deviceDisconnect(HIDDeviceInfo info){
		notifyReaderListeners(new ReaderEvent(null,Type.DISCONNECT));
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
	
	protected HashSet<SpringcardReaderListener> listenersReaderEvent = new HashSet<>();
	
	public void addReaderListener(SpringcardReaderListener listener) {
		listenersReaderEvent.add(listener);
	}

	public void removeReaderListener(SpringcardReaderListener listener) {
		listenersReaderEvent.remove(listener);
	}

	protected void notifyReaderListeners(ReaderEvent event) {
		Iterator<SpringcardReaderListener> iter = listenersReaderEvent.iterator();
		while (iter.hasNext()) {
			SpringcardReaderListener listener = iter.next();
			listener.readerEvent(event);

		}
	}
}
