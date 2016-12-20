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

import java.util.*;

public class PlayersCurrentDirection{
	// If empty, the player will not be trying to move
	// The last pressed keys are first.
	ArrayList<Integer> directionsPressed;
	public final static int CENTER = -1;
	public final static int UP = 0;
	public final static int RIGHT = 1;
	public final static int DOWN = 2;
	public final static int LEFT = 3;
	// The x direction and the y direction.
	// The ints will be -1, 0 or 1.
	// Allows for 8 directions along the horizontal, vertical and diagonal: N, NE, E, SE, S, SW, W, NW
	public int x = 0;
	int y = 0;
	// The point (xCoord, yCoord) will always be at the origin or 1 unit from the origin.
	double xCoord = 0;
	double yCoord = 0;


	public PlayersCurrentDirection(){
		directionsPressed = new ArrayList<Integer>();
	}

	public int getLastKeyDirection(){
		if (directionsPressed.size() > 0){
			return directionsPressed.get(directionsPressed.size()-1);
		}else{
			return CENTER;
		}
	}
	public void clear(){
		directionsPressed.clear();
	}

	// this should really be called isTryingToMove, since it doesn't return whether or not the player is actually moving.
	public boolean isMoving(){
		return (directionsPressed.size() > 0 ? true : false);
	}

	double oneOnRoot2 = 1.0/Math.sqrt(2);
	protected void calcDirectionXY(){
		x = 0;
		y = 0;
		for (int dir : directionsPressed){
			if (dir == UP){
				y += 1;
			}else if (dir == DOWN){
				y -= 1;
			}else if (dir == LEFT){
				x -= 1;
			}else if (dir == RIGHT){
				x += 1;
			}
		}
		if (x > 1){
			x = 1;
		}else if (x < -1){
			x = -1;
		}
		if (y > 1){
			y = 1;
		}else if (y < -1){
			y = -1;
		}

		if (x == 0){
			if (y == 0){
				xCoord = 0;
				yCoord = 0;
			}else{
				xCoord = 0;
				yCoord = y;
			}
		}else{
			if (y == 0){
				xCoord = x;
				yCoord = 0;
			}else{
				xCoord = x*oneOnRoot2;
				yCoord = y*oneOnRoot2;
			}
		}
	}

//	public void applyKeyEvent(PlayerKeyEvent e){
//		if (e.getKeyEventType() == e.KEY_PRESS){
//			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
//				addDirection(LEFT);
//			}else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
//				addDirection(RIGHT);
//			}else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
//				addDirection(UP);
//			}else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
//				addDirection(DOWN);
//			}
//		}else if (e.getKeyEventType() == e.KEY_RELEASE){
//			// Here we remove the key from directionsPressed if it is present.
//			// Note that we must remove all occurences, since there may be more
//			// than one of the same key press in the list if something weird happened.
//			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
//				removeDirection(LEFT);
//			}else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
//				removeDirection(RIGHT);
//			}else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
//				removeDirection(UP);
//			}else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
//				removeDirection(DOWN);
//			}
//		}
//	}
	protected boolean addDirection(int dir){
		boolean directionChanged = false;
		if (directionsPressed.contains(dir) == false){
			directionsPressed.add(dir);
			directionChanged = true;
			calcDirectionXY();
		}
		return directionChanged;
	}
	protected boolean removeDirection(int dir){
		boolean directionChanged = true;
		for (int i = 0; i < directionsPressed.size(); i++){
			if (directionsPressed.get(i).intValue() == dir){
				directionsPressed.remove(i);
				i--;
			}
		}
		calcDirectionXY();
		return directionChanged;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public double getXCoord() {
		return xCoord;
	}

	public double getYCoord() {
		return yCoord;
	}


}