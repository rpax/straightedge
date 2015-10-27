/*
 * Copyright (c) 2008, Keith Woodward
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of Keith Woodward nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
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
 *
 */
package straightedge.test.demo;

import straightedge.test.demo.*;
import java.awt.event.*;

/**
 *
 * @author woodwardk
 */
public class AWTEventWrapper implements Comparable{
	public static final int KEY_PRESS = 101;
	public static final int KEY_RELEASE = 102;
	public static final int MOUSE_PRESS = 103;
	public static final int MOUSE_RELEASE = 104;
	public static final int MOUSE_DRAG = 105;
	public static final int MOUSE_MOVE = 106;
	public static final int MOUSE_WHEEL = 107;

	int type;
	InputEvent inputEvent;
	long systemTimeStamp;	// nanos
	
	public AWTEventWrapper(int type, InputEvent inputEvent, long systemTimeStamp){
		this.type = type;
		this.inputEvent = inputEvent;
		this.systemTimeStamp = systemTimeStamp;
	}
	public InputEvent getInputEvent() {
		return inputEvent;
	}

	public int getType() {
		return type;
	}
	public boolean isKeyPress(){
		if (getType() == KEY_PRESS){
			return true;
		}
		return false;
	}
	public boolean isKeyRelease(){
		if (getType() == KEY_RELEASE){
			return true;
		}
		return false;
	}
	public boolean isMousePress(){
		if (getType() == MOUSE_PRESS){
			return true;
		}
		return false;
	}
	public boolean isMouseRelease(){
		if (getType() == MOUSE_RELEASE){
			return true;
		}
		return false;
	}
	public boolean isMouseMove(){
		if (getType() == MOUSE_MOVE){
			return true;
		}
		return false;
	}
	public boolean isMouseDrag(){
		if (getType() == MOUSE_DRAG){
			return true;
		}
		return false;
	}
	public boolean isMouseWheel(){
		if (getType() == MOUSE_WHEEL){
			return true;
		}
		return false;
	}

	public long getSystemTimeStamp() {
		return systemTimeStamp;
	}

	public int compareTo(Object ev){
		assert ev instanceof AWTEventWrapper : ev;
		double thisTimeStamp = getSystemTimeStamp();
		double eTimeStamp = ((AWTEventWrapper)ev).getSystemTimeStamp();
		if (thisTimeStamp > eTimeStamp){
			return 1;
		}else if (thisTimeStamp < eTimeStamp){
			return -1;
		}else{
			return 0;
		}
	}

}

