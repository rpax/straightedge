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
import java.util.ArrayList;

/**
 *
 * @author Keith
 */
public class AWTEventHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{

	ViewPane view;
	final Object awtEventMutex = new Object();
	final Object nonAWTEventMutex = new Object();
	AWTEventCache awtEventCache;
	PlayerEventCache nonAWTEventCache;

//	volatile boolean scaleUp = false;
//	volatile boolean scaleDown = false;

	public AWTEventHandler(ViewPane view){
		this.view = view;
		awtEventCache = new AWTEventCache();
		nonAWTEventCache = new PlayerEventCache();
	}

//	public void fillEventsList(ArrayList<PlayerEvent> allEvents){
//		PlayerEventCache nonAWTEventCache = getView().getEventHandler().getNonAWTEventCache();
//		nonAWTEventCache.clearAndFillCache();
//		ArrayList<PlayerEvent> playerEvents = nonAWTEventCache.getEventsList();
//		allEvents.addAll(playerEvents);
//		AWTEventCache eventCache = getView().getEventHandler().getAWTEventCache();
//		eventCache.clearAndFillCache();
//		ArrayList<AWTEventWrapper> events = eventCache.getEventsList();
//		for (int i = 0; i < events.size(); i++){
//			AWTEventWrapper awtE = events.get(i);
//			InputEvent inputEvent = awtE.getInputEvent();
//			if (awtE.isKeyPress()){
//				PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(this, awtE.getTimeStamp(), awtE.getType(), ((KeyEvent)inputEvent).getKeyCode());
//				allEvents.add(playerKeyEvent);
//			}else if (awtE.isKeyRelease()){
//				PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(this, awtE.getTimeStamp(), awtE.getType(), ((KeyEvent)inputEvent).getKeyCode());
//				allEvents.add(playerKeyEvent);
//			}else if (awtE.isMousePress()){
//				MouseEvent me = (MouseEvent)inputEvent;
//				float mx = me.getPoint().x;
//				float my = me.getPoint().y;
//				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
//				allEvents.add(playerMouseEvent);
//			}else if (awtE.isMouseRelease()){
//				MouseEvent me = (MouseEvent)inputEvent;
//				float mx = me.getPoint().x;
//				float my = me.getPoint().y;
//				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
//				allEvents.add(playerMouseEvent);
//			}else if (awtE.isMouseMove()){
//				MouseEvent me = (MouseEvent)inputEvent;
//				float mx = me.getPoint().x;
//				float my = me.getPoint().y;
//				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
//				allEvents.add(playerMouseEvent);
//			}else if (awtE.isMouseDrag()){
//				MouseEvent me = (MouseEvent)inputEvent;
//				float mx = me.getPoint().x;
//				float my = me.getPoint().y;
//				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
//				allEvents.add(playerMouseEvent);
//			}else if (awtE.isMouseWheel()){
//				MouseWheelEvent me = (MouseWheelEvent)inputEvent;
//				PlayerMouseWheelEvent playerMouseEvent = new PlayerMouseWheelEvent(this, awtE.getTimeStamp(), me.getScrollAmount());
//				allEvents.add(playerMouseEvent);
//			}
//		}
//	}

	public void doMove(double seconds, double startTime) {
		// change the scaling factor if needs be.
//		if (scaleUp != scaleDown) {
//			if (scaleUp) {
//				view.setScaleFactor((float)(view.getScaleFactor() - view.getScaleSpeed() * seconds));
//			}
//			if (scaleDown) {
//				view.setScaleFactor((float)(view.getScaleFactor() + view.getScaleSpeed() * seconds));
//			}
//		}
	}

	public void keyPressed(KeyEvent e) {
//		if (e.getKeyCode() == KeyEvent.VK_MINUS) {
//			scaleUp = true;
//		} else if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
//			scaleDown = true;
//		}else{
			addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.KEY_PRESS, e, getWorld().getTimeStampForEventNow()));
//		}
	}
	public void keyReleased(KeyEvent e) {
//		if (e.getKeyCode() == KeyEvent.VK_MINUS) {
//			scaleUp = false;
//		} else if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
//			scaleDown = false;
//		}else{
			addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.KEY_RELEASE, e, getWorld().getTimeStampForEventNow()));
//		}
	}
	public void keyTyped(KeyEvent e) {
	}
	public void mousePressed(MouseEvent e) {
		if (view.hasFocus() == false) {
			view.requestFocus();
		}
		addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.MOUSE_PRESS, e, getWorld().getTimeStampForEventNow()));
	}
	public void mouseReleased(MouseEvent e) {
		if (view.hasFocus() == false) {
			view.requestFocus();
		}
		addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.MOUSE_RELEASE, e, getWorld().getTimeStampForEventNow()));
	}
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	// The mouseDragged and mouseMoved methods are modified so that they don't send
	// too many events which can clog things up (like network communication).
	MouseEvent lastMouseMovedEvent = null;
	long lastMouseMovedEventSystemTime = 0;
	long minNanosBetweenMouseMoveEventSends = 50000000;	// 0.05 seconds
	public void mouseDragged(MouseEvent e) {
		lastMouseMovedEvent = e;
		long timeNow = System.nanoTime();
		if (timeNow > lastMouseMovedEventSystemTime + minNanosBetweenMouseMoveEventSends){
			if (view.hasFocus() == false) {
				view.requestFocus();
			}
			addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.MOUSE_DRAG, e, getWorld().getTimeStampForEventNow()));
		}
	}
	public void mouseMoved(MouseEvent e) {
		lastMouseMovedEvent = e;
		long timeNow = System.nanoTime();
		if (timeNow > lastMouseMovedEventSystemTime + minNanosBetweenMouseMoveEventSends){
			addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.MOUSE_MOVE, e, getWorld().getTimeStampForEventNow()));
		}
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		addNewEvent(new AWTEventWrapper(getPlayer(), AWTEventWrapper.MOUSE_WHEEL, e, getWorld().getTimeStampForEventNow()));
	}
	protected void addNewEvent(AWTEventWrapper ev){
		synchronized (awtEventMutex){
			awtEventCache.addEvent(ev);
		}
	}

	public void addNewEvent(PlayerEvent ev){
		synchronized (nonAWTEventMutex){
			nonAWTEventCache.addEvent(ev);
		}
	}

	public ViewPane getViewPane(){
		return view;
	}

	public Player getPlayer(){
		return getViewPane().getPlayer();
	}

	public GameWorld getWorld(){
		return getPlayer().getWorld();
	}

	public AWTEventCache getAWTEventCache() {
		return awtEventCache;
	}
	public PlayerEventCache getNonAWTEventCache() {
		return nonAWTEventCache;
	}
}
