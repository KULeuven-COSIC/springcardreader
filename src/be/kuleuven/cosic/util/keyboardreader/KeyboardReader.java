package be.kuleuven.cosic.util.keyboardreader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.codeminders.hidapi.HIDDevice;

/*
 * Partially based on ManyKeyboard
 *
 * Copyright (c) 2016, Jens Hermans 
 * Copyright (c) 2013, Nan Li All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of Sirikata nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

public class KeyboardReader {
	private static final int BUFSIZE = 2048;

	private HIDDevice dev;
	private byte[] buffer;
	private int controlFlag;
	private HashSet<Integer> keyFlag = new HashSet<>();

	private Collection<KeyEventListener> listeners = new HashSet<>();
	
	private boolean blocking;

	public KeyboardReader(HIDDevice dev, boolean blocking) throws IOException {
		this.dev = dev;
		
		this.blocking = blocking;

		if (blocking) {
			dev.enableBlocking();

		} else {
			dev.disableBlocking();
		}

		buffer = new byte[BUFSIZE];
		controlFlag = 0;
		keyFlag.add(0);
	}

	public void readKeyboardDevices(KeyMap keyMap) throws IOException {
		while (true) {
			/* very short sleep to prevent the CPU from going crazy in non-blocking mode */
			if(!blocking){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
			
			
			// the byte array is called input report,which only works
			// when there is an input event;
			// n is the number of bytes
			int n = dev.read(buffer);

			if (n != 0) { // sometimes n=0, resulting in the periphiral
							// keyboard always clear the hashset

				HashSet<Integer> currentSet = new HashSet<Integer>();
				int currentControl = 0;

				if (n == 8) // mostly 8
				{
					for (int i = 0; i < n; i++) {
						int current = buffer[i];

						if (current < 0) {
							current = current + 256;
						}

						if (i != 0) {
							currentSet.add(current);
						} else // the control flag
						{
							currentControl = current;
						}

					}

				} else if (n == 10) // macbook keyboard 10
				{
					for (int i = 0; i < n; i++) {
						if (i != 0 && i != 9) {
							int current = buffer[i];

							if (current < 0) {
								current = current + 256;
							}

							if (i != 1) {
								currentSet.add(current);
							} else // the control flag
							{
								currentControl = current;
							}
						}
					}

				} else {
					if (n != 0)
						System.err.println("other than 8 or 10: " + n);
				}

				// if no one listens, then return
				if (listeners == null)
					return;

				// if not return, someone is listening, we process with
				// the following code

				// measure the diff between current flag and last flag
				int lastFlag = controlFlag;
				int diff = currentControl - lastFlag;

				if (diff > 0) // more keys are pressed
				{
					// note that 1,2,4,8,16,32,64,128, these digits are
					// 2^n, so we can judge which one is pressed with
					// Binary number
					int i = 0;
					char[] digits = Integer.toBinaryString(diff).toCharArray();
					for (char digit : digits) {
						if (digit == '1') {
							int power = digits.length - 1 - i;
							KeyEvent event = new KeyEvent(dev, KeyEvent.KEY_PRESSED, -(int) Math.pow(2, power), 0,
									keyMap);
							// System.err.println((int)Math.pow(2,power));
							notifyListeners(event);
						}
						i++;
					}

					controlFlag = currentControl;

				} else if (diff < 0) // some keys are released
				{
					int i = 0;
					char[] digits = Integer.toBinaryString(-diff).toCharArray();
					for (char digit : digits) {
						if (digit == '1') {
							int power = digits.length - 1 - i;
							KeyEvent event = new KeyEvent(dev, KeyEvent.KEY_RELEASED, -(int) Math.pow(2, power), 0,
									keyMap);
							// System.err.println((int)Math.pow(2,power));
							notifyListeners(event);
						}
						i++;
					}

					controlFlag = currentControl;
				}

				// measure the diff between current key flags and last
				// key flags
				HashSet<Integer> lastSet = keyFlag;

				HashSet<Integer> newKeySet = getSubtraction(currentSet, lastSet);
				HashSet<Integer> removedKeySet = getSubtraction(lastSet, currentSet);

				if (newKeySet.size() != 0) // new key pressed
				{
					for (Integer key : newKeySet) {
						KeyEvent event = new KeyEvent(dev, KeyEvent.KEY_PRESSED, key, currentControl, keyMap);
						notifyListeners(event);
					}
				}

				if (removedKeySet.size() != 0) // key released
				{
					for (Integer key : removedKeySet) {
						KeyEvent event = new KeyEvent(dev, KeyEvent.KEY_RELEASED, key, currentControl, keyMap);
						notifyListeners(event);
					}
				}

				keyFlag = currentSet;

			}

		}

	}

	public void addManyKeyEventListener(KeyEventListener listener) {
		listeners.add(listener);
	}

	public void removeManyKeyEventListener(KeyEventListener listener) {
		listeners.remove(listener);
	}

	public void notifyListeners(KeyEvent event) {
		Iterator<KeyEventListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			KeyEventListener listener = (KeyEventListener) iter.next();
			listener.manyKeyEvent(event);

		}
	}

	private static HashSet<Integer> getSubtraction(HashSet<Integer> coll1, HashSet<Integer> coll2) {
		HashSet<Integer> result = new HashSet<>(coll1);
		result.removeAll(coll2);
		return result;
	}
}
