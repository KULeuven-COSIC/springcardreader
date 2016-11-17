package be.kuleuven.cosic.util.keyboardreader;

import java.util.EventObject;

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
public class KeyEvent extends EventObject
{   
	private static final long serialVersionUID = 7040416118241872173L;
	
	public static int KEY_PRESSED = "KEY_PRESSED".hashCode();
    public static int KEY_RELEASED = "KEY_RELEASED".hashCode();

    private int keyCode = -1;
    private int type = -1;
    private int controlFlag = -1;
    private KeyMap keyMap;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException
     *          if source is null.
     */
    public KeyEvent(Object source, int type, int keyCode, int controlFlag, KeyMap keyMap)
    {
        super(source);
        this.keyCode = keyCode;
        this.type = type;
        this.controlFlag = controlFlag;
        this.keyMap = keyMap;
    }

    public boolean isActionKey()
    {
        return keyMap.isActionKey(this.keyCode);
    }

    public boolean isModifierKey()
    {
        return keyCode<0;
    }

    public int getKeyCode()
    {
        return keyCode;
    }
    
    public int getControlFlag(){
    	return controlFlag;
    }

    public String getKeystring()
    {
        return keyMap.getKeyString(keyCode,controlFlag);
    }

    public String getKeyType()
    {
        if(type == KEY_PRESSED)
        {return "KEY_PRESSED";}
        else
        {return  "KEY_RELEASED";}
    }
}
