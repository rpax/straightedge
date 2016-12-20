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
package straightedge.test.benchmark.event;

import straightedge.test.benchmark.*;
import java.awt.event.*;
/**
 *
 * @author CommanderKeith
 */


public class PlayerMouseEvent extends PlayerEvent{
	
	public static final int MOUSE_PRESS = AWTEventWrapper.MOUSE_PRESS;
	public static final int MOUSE_RELEASE = AWTEventWrapper.MOUSE_RELEASE;
	public static final int MOUSE_DRAG = AWTEventWrapper.MOUSE_DRAG;
	public static final int MOUSE_MOVE = AWTEventWrapper.MOUSE_MOVE;
	
	// Coords given in world coordinates
	protected float absoluteX;
	protected float absoluteY;
	protected int button;	// this will be MouseEvent.NOBUTTON for mouse drag events
	
	/** Creates a new instance of PlayerMouseEvent */
	public PlayerMouseEvent() {
	}
	public PlayerMouseEvent(Player player, double timeStamp, int mouseEventType, float x, float y, int button){
		this.player = player;
		this.timeStamp = timeStamp;
		this.type = mouseEventType;
		this.setAbsoluteX(x);
		this.setAbsoluteY(y);
		this.button = button;
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

	public int getButton() {
		return button;
	}
	
	public void setButton(int button) {
		this.button = button;
	}

	public boolean isLeftButton(){
		if (button == MouseEvent.BUTTON1){
			return true;
		}
		return false;
	}
	public boolean isMiddleButton(){
		if (button == MouseEvent.BUTTON2){
			return true;
		}
		return false;
	}
	public boolean isRightButton(){
		if (button == MouseEvent.BUTTON3){
			return true;
		}
		return false;
	}
	
	public float getAbsoluteX() {
		return absoluteX;
	}

	public void setAbsoluteX(float x) {
		this.absoluteX = x;
	}

	public float getAbsoluteY() {
		return absoluteY;
	}

	public void setAbsoluteY(float y) {
		this.absoluteY = y;
	}
}


