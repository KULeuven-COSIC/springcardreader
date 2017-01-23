/*
 * Springcardreader
 * 
 * Copyright (c) 2016, Jens Hermans 
 *
 */
package be.kuleuven.cosic.util.keyboardreader;

import java.io.IOException;
import java.util.HashSet;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;


public abstract class PersistentKeyboardMonitor {
	private HashSet<HIDDeviceInfo> openedDevices = new HashSet<>();
	
	private int pollingInterval;
	
	public PersistentKeyboardMonitor(int pollingInterval){
		this.pollingInterval = pollingInterval;
	}

	/**
	 * 
	 */
	public void run(){
		new Thread(
			new Runnable(){

				@Override
				public void run() {
					Thread.currentThread().setName("PersistentKeyboardMonitor");
					while(true){
						try {
							Thread.sleep(pollingInterval);
						} catch (InterruptedException e) {}
						
						
						try {
							HIDDeviceInfo [] deviceInfos = KeyboardsManager.getKeyboardDevices();
							
							if(deviceInfos == null){
								continue;
							}
							
							for(HIDDeviceInfo info : deviceInfos){
								if(!deviceConnect(info)){
									continue;
								}
								
								synchronized(PersistentKeyboardMonitor.this){
									boolean found = false;
									for(HIDDeviceInfo deviceInfo : openedDevices){
										if(deviceInfo.getPath().equals(info.getPath())){
											found = true;
										}
									}
									
									if(!found){
										newDeviceDetected(info);
									}
								}
							}
							
							
						} catch (Exception e){
							e.printStackTrace();
						}
						
					}
					
				}
				
		}).start();
	}
	
	
	protected abstract boolean deviceConnect(HIDDeviceInfo info);
	protected abstract void deviceDisconnect(HIDDeviceInfo info);
	protected abstract void useNewDevice(HIDDeviceInfo info, HIDDevice device) throws IOException;
	
	public synchronized void newDeviceDetected(final HIDDeviceInfo deviceInfo){
		try {
			final HIDDevice device = KeyboardsManager.openKeyboardDevice(deviceInfo);
			if(device == null){
				return; // for some odd reason, device is sometimes null
			}
			openedDevices.add(deviceInfo);			
			
			
			new Thread(new Runnable(){
				public void run(){
					Thread.currentThread().setName("useNewDevice");
					try {
						useNewDevice(deviceInfo, device);
					} catch (IOException e) {
						// Reader is dead
						synchronized(PersistentKeyboardMonitor.this){
							openedDevices.remove(deviceInfo);
							deviceDisconnect(deviceInfo);
						}	
						
					} finally {
						
					}
				}
			}).start();
			
		} catch (IOException e) {
			// stop - no need to clean up (openKeyboardDevice can give last possible IOException)
		}
	}
	
	public synchronized int getNumberConnectedDevices(){
		return openedDevices.size();
	}
	
	
}
