/*
 *  Partially based on ManyKeyboard
 *
 *  Copyright (c) 2016, Jens Hermans
 *  Copyright (c) 2013, Nan Li
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of Sirikata nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package be.kuleuven.cosic.util.keyboardreader;



import java.io.IOException;
import java.util.HashSet;

import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

public class KeyboardsManager {
	/**
	 * Load javahidlib and create key mappings
	 */
	static {
		ClassPathLibraryLoader.loadNativeHIDLibrary();
	}
	
	private static HashSet<HIDDevice> openDevices = new HashSet<>();

	/**
	 * Get IO of the keyboard devices connected to the computer
	 */
	public static HIDDeviceInfo[] getKeyboardDevices() throws IOException {
		HIDManager manager = HIDManager.getInstance();
		HIDDeviceInfo[] ret = manager.listDevices();
		System.gc();
		return ret;

	}

	/**
	 * Close the IO of the keyboard devices connected to the computer
	 */
	public static void closeKeyboardDevice(HIDDevice device) {
		try {
			device.close();
			System.gc();
			openDevices.remove(device);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void closeAllKeyboardDevices() {
		try {
			for(HIDDevice dev : openDevices){
				try {
					dev.close();
				} catch(IOException e){}
			}
			HIDManager.getInstance().release();
		} catch (IOException e) {
		}
	}

	/**
	 * Open the IO input from the keyboard devices
	 */
	public static HIDDevice openKeyboardDevice(HIDDeviceInfo hidDeviceInfo) throws IOException {
		return HIDManager.getInstance().openByPath(hidDeviceInfo.getPath());
	}

}
