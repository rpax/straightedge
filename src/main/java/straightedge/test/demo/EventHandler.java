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

import straightedge.geom.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Keith
 */
public class EventHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, WindowListener{
	AWTEventCache eventCache;
	KPoint lastMousePointInWorldCoords;
	PlayersCurrentDirection playersCurrentDirection;

	public Main main;

	public EventHandler(Main main){
		this.main = main;
		
	}
	public void init(){
		Container parentFrameOrApplet = main.getParentFrameOrApplet();

		eventCache = new AWTEventCache();
		lastMousePointInWorldCoords = new KPoint();
		playersCurrentDirection = new PlayersCurrentDirection();

		if (main instanceof MainFrame){
			((MainFrame)main).getFrame().addWindowListener(this);
		}
		
		main.viewPane.addKeyListener(this);
		main.viewPane.addMouseListener(this);
		main.viewPane.addMouseMotionListener(this);
		main.viewPane.addMouseWheelListener(this);
	}

	public void close(){
		main.viewPane.removeKeyListener(this);
		main.viewPane.removeMouseListener(this);
		main.viewPane.removeMouseMotionListener(this);
		main.viewPane.removeMouseWheelListener(this);
	}

	public void keyPressed(KeyEvent e) {
		addNewEvent(new AWTEventWrapper(AWTEventWrapper.KEY_PRESS, e, System.nanoTime()));
	}

	public void keyReleased(KeyEvent e) {
		addNewEvent(new AWTEventWrapper(AWTEventWrapper.KEY_RELEASE, e, System.nanoTime()));
	}
	public void keyTyped(KeyEvent e) {
	}
	public void mousePressed(MouseEvent e) {
		if (getView().hasFocus() == false) {
			getView().requestFocus();
		}
		addNewEvent(new AWTEventWrapper(AWTEventWrapper.MOUSE_PRESS, e, System.nanoTime()));
	}
	public void mouseReleased(MouseEvent e) {
		if (getView().hasFocus() == false) {
			getView().requestFocus();
		}
		addNewEvent(new AWTEventWrapper(AWTEventWrapper.MOUSE_RELEASE, e, System.nanoTime()));
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
			if (getView().hasFocus() == false) {
				getView().requestFocus();
			}
			addNewEvent(new AWTEventWrapper(AWTEventWrapper.MOUSE_DRAG, e, timeNow));
			lastMouseMovedEventSystemTime = timeNow;
		}
	}
	public void mouseMoved(MouseEvent e) {
		lastMouseMovedEvent = e;
		long timeNow = System.nanoTime();
		if (timeNow > lastMouseMovedEventSystemTime + minNanosBetweenMouseMoveEventSends){
			addNewEvent(new AWTEventWrapper(AWTEventWrapper.MOUSE_MOVE, e, timeNow));
			lastMouseMovedEventSystemTime = timeNow;
		}
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		addNewEvent(new AWTEventWrapper(AWTEventWrapper.MOUSE_WHEEL, e, System.nanoTime()));
	}
	public void windowOpened(WindowEvent e){}
    public void windowClosing(WindowEvent e){
		main.close();
	}
    public void windowClosed(WindowEvent e){}
    public void windowIconified(WindowEvent e){}
    public void windowDeiconified(WindowEvent e){}
    public void windowActivated(WindowEvent e){}
    public void windowDeactivated(WindowEvent e){}

	protected void addNewEvent(AWTEventWrapper ev){
		eventCache.addEvent(ev);
	}

	public ViewPane getView(){
		return main.viewPane;
	}
	public World getWorld(){
		return main.world;
	}

	boolean altKeyDown = false;
	boolean leftMouseButtonDown = false;
	boolean rightMouseButtonDown = false;
	public void processEvent(AWTEventWrapper awtEventWrapper){
		View view = main.view;
		Loop loop = main.loop;
		KPoint viewCenterInScreenCoords = view.viewCenterInScreenCoords;
		KPoint viewCenterInWorldCoords = view.viewCenterInWorldCoords;
		final World world = main.world;
		Player player = world.player;

		AWTEvent awtEvent = awtEventWrapper.getInputEvent();
		double timeNow = awtEventWrapper.getSystemTimeStamp();
		if (awtEvent instanceof MouseEvent){
			MouseEvent e = (MouseEvent)awtEvent;
			if (e.getID() == MouseEvent.MOUSE_MOVED){
				lastMousePointInWorldCoords.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
				lastMousePointInWorldCoords.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);
			}else if (e.getID() == MouseEvent.MOUSE_PRESSED){
				if (altKeyDown == false){
					lastMousePointInWorldCoords.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoords.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);
					if (e.getButton() == MouseEvent.BUTTON1){
						player.targetFinder.setFixedTarget(lastMousePointInWorldCoords, true);
						leftMouseButtonDown = true;
					}else if (e.getButton() == MouseEvent.BUTTON3){
						player.gun.startFiring(timeNow);
						rightMouseButtonDown = true;
					}
				}else{
					KPoint lastMousePointInWorldCoordsExclViewMove = new KPoint();
					lastMousePointInWorldCoordsExclViewMove.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoordsExclViewMove.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);

					viewCenterInWorldCoords.x -= lastMousePointInWorldCoordsExclViewMove.x - lastMousePointInWorldCoords.x;
					viewCenterInWorldCoords.y -= lastMousePointInWorldCoordsExclViewMove.y - lastMousePointInWorldCoords.y;
				}
			}else if (e.getID() == MouseEvent.MOUSE_DRAGGED){
				if (altKeyDown == false){
					lastMousePointInWorldCoords.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoords.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);
					if (leftMouseButtonDown){
						player.targetFinder.setFixedTarget(lastMousePointInWorldCoords, true);
					}
				}else{
					KPoint lastMousePointInWorldCoordsExclViewMove = new KPoint();
					lastMousePointInWorldCoordsExclViewMove.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoordsExclViewMove.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);

					viewCenterInWorldCoords.x -= lastMousePointInWorldCoordsExclViewMove.x - lastMousePointInWorldCoords.x;
					viewCenterInWorldCoords.y -= lastMousePointInWorldCoordsExclViewMove.y - lastMousePointInWorldCoords.y;
				}
			}else if (e.getID() == MouseEvent.MOUSE_RELEASED){
				if (altKeyDown == false){
					lastMousePointInWorldCoords.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoords.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);
					if (e.getButton() == MouseEvent.BUTTON1){
						player.targetFinder.setFixedTarget(lastMousePointInWorldCoords, true);
						leftMouseButtonDown = false;
					}else if (e.getButton() == MouseEvent.BUTTON3){
						player.gun.stopFiring();
						rightMouseButtonDown = false;
					}
				}else{
					KPoint lastMousePointInWorldCoordsExclViewMove = new KPoint();
					lastMousePointInWorldCoordsExclViewMove.x = ((e.getX() - viewCenterInScreenCoords.x) / view.scaleFactor + viewCenterInWorldCoords.x);
					lastMousePointInWorldCoordsExclViewMove.y = ((e.getY() - viewCenterInScreenCoords.y) / view.scaleFactor + viewCenterInWorldCoords.y);

					viewCenterInWorldCoords.x -= lastMousePointInWorldCoordsExclViewMove.x - lastMousePointInWorldCoords.x;
					viewCenterInWorldCoords.y -= lastMousePointInWorldCoordsExclViewMove.y - lastMousePointInWorldCoords.y;
				}
			}else if (e.getID() == MouseEvent.MOUSE_WHEEL){
				MouseWheelEvent mwe = (MouseWheelEvent)awtEvent;
				int wheelRotations = mwe.getWheelRotation();
				if (altKeyDown == true){
					//scaleFactor -= wheelRotations/4f;
					view.scaleFactor *= (1 - wheelRotations/4f);
				}
			}
		}else if (awtEvent instanceof java.awt.event.KeyEvent){
			KeyEvent e = (KeyEvent)awtEvent;
			if ((e.getModifiersEx() & (KeyEvent.ALT_DOWN_MASK)) == KeyEvent.ALT_DOWN_MASK) {
				if (altKeyDown == false){
					main.viewPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					altKeyDown = true;
				}
			}else{
				if (altKeyDown == true){
					main.viewPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					altKeyDown = false;
				}
			}
			TargetFinder targetFinder = player.targetFinder;
			double keyDirectionDist = 10;
			if (e.getID() == KeyEvent.KEY_PRESSED){
				if (e.getKeyCode() == KeyEvent.VK_R){
					main.animationLoading.show();
					Thread t = new Thread(){
						public void run(){
							main.world.init();	// takes ages
							main.eventHandler.eventCache.clearAndFillCache();	// clear out any old events
							WorldLoopAnimation newAnimationWorld = new WorldLoopAnimation(main.world, main.view);
							main.loop.setAnimationAndRestart(newAnimationWorld);
						}
					};
					t.start();
				}else if (e.getKeyCode() == KeyEvent.VK_V){
					if (view.paintMode == view.WIRE_FRAME){
						view.paintMode = view.ALL;
					}else if (view.paintMode == view.ALL){
						view.paintMode = view.CLIPPED;
					}else if (view.paintMode == view.CLIPPED){
						view.paintMode = view.FOG_OF_WAR;
					}else if (view.paintMode == view.FOG_OF_WAR){
						view.paintMode = view.FOG_OF_WAR_NO_CLIP;
					}else if (view.paintMode == view.FOG_OF_WAR_NO_CLIP){
						view.paintMode = view.WIRE_FRAME;
					}
				}else if (e.getKeyCode() == KeyEvent.VK_L){
					view.antialias = !view.antialias;
				}else if (e.getKeyCode() == KeyEvent.VK_I){
					AcceleratedImage.useVolatileImage = !AcceleratedImage.useVolatileImage;
				}else if (e.getKeyCode() == KeyEvent.VK_N){
					main.animationLoading.show();
					Thread t = new Thread(){
						public void run(){
							if (main.world instanceof WorldMaze){
								main.world = new WorldStarField(main);
							}else if (main.world instanceof WorldStarField){
								main.world = new WorldStoneHenge(main);
							}else if (main.world instanceof WorldStoneHenge){
								main.world = new WorldLetters(main);
							}else if (main.world instanceof WorldLetters){
								main.world = new WorldShapes(main);
							}else if (main.world instanceof WorldShapes){
								main.world = new WorldKochIsland(main);
							}else if (main.world instanceof WorldKochIsland){
								main.world = new WorldKochSnowflake(main);
							}else if (main.world instanceof WorldKochSnowflake){
								main.world = new WorldEnclosedKochCurve(main);
							}else if (main.world instanceof WorldEnclosedKochCurve){
								main.world = new WorldSierpinskiGasket(main);
							}else if (main.world instanceof WorldSierpinskiGasket){
								main.world = new WorldHexagonalGosperCurve(main);
							}else if (main.world instanceof WorldHexagonalGosperCurve){
								main.world = new WorldQuadraticGosperCurve(main);
							}else if (main.world instanceof WorldQuadraticGosperCurve){
								main.world = new WorldVogelSpiral(main);
							}else if (main.world instanceof WorldVogelSpiral){
								main.world = new WorldMaze(main);
							}else{
								main.world = new WorldMaze(main);
							}
							main.world.init();	// takes ages
							main.eventHandler.eventCache.clearAndFillCache();	// clear out any old events
							WorldLoopAnimation newAnimationWorld = new WorldLoopAnimation(main.world, main.view);
							main.loop.setAnimationAndRestart(newAnimationWorld);
						}
					};
					t.start();
				}else if (e.getKeyCode() == KeyEvent.VK_C){
					if (view.renderConnections == false){
						view.renderConnections = true;
						view.paintMode = view.WIRE_FRAME;
					}else{
						view.renderConnections = false;
					}
				}else if (e.getKeyCode() == KeyEvent.VK_0 || 
						e.getKeyCode() == KeyEvent.VK_1 ||
						e.getKeyCode() == KeyEvent.VK_2 || 
						e.getKeyCode() == KeyEvent.VK_3 || 
						e.getKeyCode() == KeyEvent.VK_4 || 
						e.getKeyCode() == KeyEvent.VK_5 || 
						e.getKeyCode() == KeyEvent.VK_6 || 
						e.getKeyCode() == KeyEvent.VK_7 || 
						e.getKeyCode() == KeyEvent.VK_8 || 
						e.getKeyCode() == KeyEvent.VK_9){
					int num = Integer.valueOf(""+e.getKeyChar());
					world.changeNumEnemies(num);
				}else if (e.getKeyCode() == KeyEvent.VK_P){
					if (getWorld().pause == false){
						getWorld().pause = true;
					}else{
						getWorld().pause = false;
					}
//				}else if (e.getKeyCode() == KeyEvent.VK_O){
//					// print screen:
//					try{
//						String fileName = System.getProperty("user.home")+ "/screenshot"+getWorld().getClass().getSimpleName()+"_"+view.paintMode+".png";
//						File saveFile = new File(fileName);
//						saveFile.createNewFile();
//						ImageIO.write((BufferedImage)main.viewPane.acceleratedImage.bi, "PNG", saveFile);
//						System.out.println(this.getClass().getSimpleName()+": saveFile.getAbsolutePath() == "+saveFile.getAbsolutePath());
//					}catch(Exception ex){
//						ex.printStackTrace();
//					}
				}else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
					boolean dirChanged = playersCurrentDirection.addDirection(PlayersCurrentDirection.LEFT);
					if (playersCurrentDirection.getXCoord() == 0 && playersCurrentDirection.getYCoord() == 0){
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
					boolean dirChanged = playersCurrentDirection.addDirection(PlayersCurrentDirection.RIGHT);
					if (playersCurrentDirection.getXCoord() == 0 && playersCurrentDirection.getYCoord() == 0){
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
					boolean dirChanged = playersCurrentDirection.addDirection(PlayersCurrentDirection.UP);
					if (playersCurrentDirection.getXCoord() == 0 && playersCurrentDirection.getYCoord() == 0){
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
					boolean dirChanged = playersCurrentDirection.addDirection(PlayersCurrentDirection.DOWN);
					if (playersCurrentDirection.getXCoord() == 0 && playersCurrentDirection.getYCoord() == 0){
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}

			}else if (e.getID() == KeyEvent.KEY_RELEASED){
				if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
					boolean dirChanged = playersCurrentDirection.removeDirection(PlayersCurrentDirection.LEFT);
					if (playersCurrentDirection.getLastKeyDirection() == playersCurrentDirection.CENTER){
						// only set a fixed target if no direction exists
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
					boolean dirChanged = playersCurrentDirection.removeDirection(PlayersCurrentDirection.RIGHT);
					if (playersCurrentDirection.getLastKeyDirection() == playersCurrentDirection.CENTER){
						// only set a fixed target if no direction exists
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
					boolean dirChanged = playersCurrentDirection.removeDirection(PlayersCurrentDirection.UP);
					if (playersCurrentDirection.getLastKeyDirection() == playersCurrentDirection.CENTER){
						// only set a fixed target if no direction exists
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
					boolean dirChanged = playersCurrentDirection.removeDirection(PlayersCurrentDirection.DOWN);
					if (playersCurrentDirection.getLastKeyDirection() == playersCurrentDirection.CENTER){
						// only set a fixed target if no direction exists
						targetFinder.setFixedTarget(player.getPos(), dirChanged);
					}else{
						targetFinder.setRelativeTarget(keyDirectionDist*playersCurrentDirection.getXCoord(), -keyDirectionDist*playersCurrentDirection.getYCoord(), dirChanged);
					}
				}
			}
		}else if (awtEvent instanceof ComponentEvent){
			ComponentEvent e = (ComponentEvent)awtEvent;
			if (e.getID() == ComponentEvent.COMPONENT_RESIZED){
				//this.init();
			}
		}
	}

}
