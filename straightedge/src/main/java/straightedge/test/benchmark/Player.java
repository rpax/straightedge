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
package straightedge.test.benchmark;

import straightedge.geom.*;
import straightedge.test.benchmark.event.*;
import straightedge.geom.path.*;
import straightedge.geom.vision.*;
import java.util.*;
import java.awt.event.*;

/**
 *
 * @author woodwardk
 */
public class Player implements Monster{
	PathFinder pathFinder;
	PathData pathData;
	KPoint pos;
	KPoint target;
	boolean recalcPathOnEveryUpdate;
	float maxConnectionDist;
	boolean calcPathNeeded;

	GameWorld world;
	GameLoop loop;
	float moveAngle;
	float rotationSpeed;
	float lookAngle;
	float stationaryAngleTarget;
	float speed;
	float speedX;
	float speedY;
	KPolygon polygon;

	boolean calcVision = true;
	VisionFinder visionFinder;
	VisionDataRotation visionData;

	protected boolean leftMouseDown;
    protected boolean rightMouseDown;
	
	public Player(){
		moveAngle = 0;
		lookAngle = moveAngle;
		stationaryAngleTarget = lookAngle;
		rotationSpeed = (float)(2.5f*Math.PI);
		speed = 120;
		pos = new KPoint(298,302);
		target = new KPoint(580, 1);
		pathData = new PathData();
		recalcPathOnEveryUpdate = true;
		maxConnectionDist = 300;
		calcPathNeeded = false;
		
		
		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			pointList.add(new KPoint(0, 0));
			pointList.add(new KPoint(-5, 6));
			pointList.add(new KPoint(10, 0));
			pointList.add(new KPoint(-5, -6));
			polygon = new KPolygon(pointList);
			polygon.translateToOrigin();
		}

		{
			KPolygon originalBoundaryPolygon = KPolygon.createRegularPolygon(20, 300);
			KPoint originalEye = new KPoint(0, 0);
			visionData = new VisionDataRotation(originalEye, originalBoundaryPolygon);
			visionFinder = new VisionFinder();
		}
		

		leftMouseDown = false;
		rightMouseDown = false;
	}

	
	public void fillEventsList(ArrayList<PlayerEvent> allEvents){
		PlayerEventCache nonAWTEventCache = getView().getEventHandler().getNonAWTEventCache();
		nonAWTEventCache.clearAndFillCache();
		ArrayList<PlayerEvent> playerEvents = nonAWTEventCache.getEventsList();
		allEvents.addAll(playerEvents);
		AWTEventCache eventCache = getView().getEventHandler().getAWTEventCache();
		eventCache.clearAndFillCache();
		ArrayList<AWTEventWrapper> events = eventCache.getEventsList();
		for (int i = 0; i < events.size(); i++){
			AWTEventWrapper awtE = events.get(i);
			InputEvent inputEvent = awtE.getInputEvent();
			if (awtE.isKeyPress()){
				PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(this, awtE.getTimeStamp(), awtE.getType(), ((KeyEvent)inputEvent).getKeyCode());
				allEvents.add(playerKeyEvent);
			}else if (awtE.isKeyRelease()){
				PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(this, awtE.getTimeStamp(), awtE.getType(), ((KeyEvent)inputEvent).getKeyCode());
				allEvents.add(playerKeyEvent);
			}else if (awtE.isMousePress()){
				MouseEvent me = (MouseEvent)inputEvent;
				float mx = me.getPoint().x;
				float my = me.getPoint().y;
				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
				allEvents.add(playerMouseEvent);
			}else if (awtE.isMouseRelease()){
				MouseEvent me = (MouseEvent)inputEvent;
				float mx = me.getPoint().x;
				float my = me.getPoint().y;
				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
				allEvents.add(playerMouseEvent);
			}else if (awtE.isMouseMove()){
				MouseEvent me = (MouseEvent)inputEvent;
				float mx = me.getPoint().x;
				float my = me.getPoint().y;
				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
				allEvents.add(playerMouseEvent);
			}else if (awtE.isMouseDrag()){
				MouseEvent me = (MouseEvent)inputEvent;
				float mx = me.getPoint().x;
				float my = me.getPoint().y;
				PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(this, awtE.getTimeStamp(), awtE.getType(), mx , my, me.getButton());
				allEvents.add(playerMouseEvent);
			}else if (awtE.isMouseWheel()){
				MouseWheelEvent me = (MouseWheelEvent)inputEvent;
				PlayerMouseWheelEvent playerMouseEvent = new PlayerMouseWheelEvent(this, awtE.getTimeStamp(), me.getScrollAmount());
				allEvents.add(playerMouseEvent);
			}
		}

	}
	KPolygon editableRect = null;
	KPoint lastMousePress;
	KPoint lastMouseDragOrRelease;
	KPoint lastMouseMove;
	public void processEvent(PlayerEvent playerEvent){
		if (playerEvent.getType() == AWTEventWrapper.MOUSE_PRESS || 
				playerEvent.getType() == AWTEventWrapper.MOUSE_RELEASE ||
				playerEvent.getType() == AWTEventWrapper.MOUSE_DRAG){
			PlayerMouseEvent ev = (PlayerMouseEvent)playerEvent;
			boolean leftMousePressed = false;
			boolean leftMouseDragged = false;
			boolean leftMouseReleased = false;
			if (ev.getButton() == MouseEvent.BUTTON1 || ev.getButton() == MouseEvent.NOBUTTON && leftMouseDown){
				if (ev.getType() == PlayerMouseEvent.MOUSE_PRESS){
					leftMousePressed = true;
					leftMouseDown = true;
				}else if (ev.getType() == PlayerMouseEvent.MOUSE_DRAG){
					leftMouseDragged = true;
				}else if (ev.getType() == PlayerMouseEvent.MOUSE_RELEASE){
					leftMouseReleased = true;
					leftMouseDown = false;
				}
			}
			boolean rightMousePressed = false;
			boolean rightMouseDragged = false;
			boolean rightMouseReleased = false;
			if (ev.getButton() == MouseEvent.BUTTON3 || ev.getButton() == MouseEvent.NOBUTTON && rightMouseDown){
				if (ev.getType() == PlayerMouseEvent.MOUSE_PRESS){
					rightMousePressed = true;
					rightMouseDown = true;
				}else if (ev.getType() == PlayerMouseEvent.MOUSE_DRAG){
					rightMouseDragged = true;
				}else if (ev.getType() == PlayerMouseEvent.MOUSE_RELEASE){
					rightMouseReleased = true;
					rightMouseDown = false;
				}
			}

			stationaryAngleTarget = (float)KPoint.findAngle(pos.x, pos.y, ev.getAbsoluteX(), ev.getAbsoluteY());

			if (rightMousePressed || rightMouseDragged || rightMouseReleased){
				// insert an obstacle or take one away
				float worldX = ev.getAbsoluteX();
				float worldY = ev.getAbsoluteY();
				int width = 20;
				if (rightMousePressed){
					lastMousePress = new KPoint(worldX, worldY);
					editableRect = null;
					return;
				}else if (rightMouseDragged){
					lastMouseDragOrRelease = new KPoint(worldX, worldY);
					editableRect = KPolygon.createRectOblique(lastMousePress, lastMouseDragOrRelease, width);
					return;
				}else if (rightMouseReleased){
					lastMouseDragOrRelease = new KPoint(worldX, worldY);
					if (lastMousePress.distance(lastMouseDragOrRelease) > 4){
						// only add an obstacle if it isn't a slither.
						editableRect = KPolygon.createRectOblique(lastMousePress, lastMouseDragOrRelease, width);
						PathBlockingObstacleImpl obstacle = PathBlockingObstacleImpl.createObstacleFromOuterPolygon(editableRect);
						if (obstacle != null){
							getWorld().getObstacleManager().addObstacle(obstacle);
							getWorld().getOccluderTileArray().add(new OccluderImpl(obstacle.getInnerPolygon()));
						}
					}else{
						ArrayList<PathBlockingObstacleImpl> obstacles = getWorld().getObstacleManager().getTileBag().getAllWithin(lastMouseDragOrRelease, 1);
						PathBlockingObstacleImpl obstacleToBeRemoved = null;
						for (int j = 0; j < obstacles.size(); j++){
							PathBlockingObstacleImpl obst = obstacles.get(j);
							if (obst.getInnerPolygon().contains(lastMouseDragOrRelease)){
								obstacleToBeRemoved = obst;
								break;
							}
						}
						OccluderImpl occluderToBeRemoved = null;
						ArrayList<OccluderImpl> occluders  = getWorld().getOccluderTileArray().getAllWithin(lastMouseDragOrRelease, 1);
						for (int j = 0; j < occluders.size(); j++){
							OccluderImpl obst = occluders.get(j);
							if (obst.getPolygon().contains(lastMouseDragOrRelease)){
								occluderToBeRemoved = obst;
								break;
							}
						}
						if (obstacleToBeRemoved != null && occluderToBeRemoved != null){
							getWorld().getObstacleManager().removeObstacle(obstacleToBeRemoved);
							getWorld().getOccluderTileArray().remove(occluderToBeRemoved);
						}
					}
					editableRect = null;
					return;
				}
			}else if (leftMousePressed || leftMouseDragged || leftMouseReleased){
				float worldX = ev.getAbsoluteX();
				float worldY = ev.getAbsoluteY();
				target = new KPoint(worldX, worldY);
				// check that the target point isn't inside any obstacles.
				// if so, move it.
				boolean targetIsInsideObstacle = false;
				int count = 0;
				while (true){
					for (PathBlockingObstacleImpl obst : getWorld().getObstacles()){
						if (obst.getOuterPolygon().contains(target)){
							targetIsInsideObstacle = true;
							KPolygon poly = obst.getOuterPolygon();
							KPoint p = poly.getBoundaryPointClosestTo(target);
							if (p != null){
								target = p;
							}
							assert target != null : "pos == "+pos+", target == "+target;
						}
					}
					count++;
					if (targetIsInsideObstacle == false || count >= 3){
						break;
					}
				}
				calcPathNeeded = true;
			}
		}else if (playerEvent.getType() == AWTEventWrapper.MOUSE_MOVE){
			PlayerMouseEvent ev = (PlayerMouseEvent)playerEvent;
			stationaryAngleTarget = (float)KPoint.findAngle(pos.x, pos.y, ev.getAbsoluteX(), ev.getAbsoluteY());
			float worldX = ev.getAbsoluteX();
			float worldY = ev.getAbsoluteY();
			lastMouseMove = new KPoint(worldX, worldY);
//			float relCCW = m.relativeCCW(pos, new KPoint(pos.x+1, pos.y));
//			System.out.println(this.getClass().getSimpleName()+": relCCW == "+relCCW);
		}else if (playerEvent.getType() == PlayerKeyEvent.KEY_PRESS){
			PlayerKeyEvent ke = (PlayerKeyEvent)playerEvent;
			if (ke.getKeyCode() == KeyEvent.VK_K){
				KPolygon poly = KPolygon.createRectOblique(new KPoint(0,0), new KPoint(0,60), 20);
				poly.translateTo(lastMouseMove);
				getWorld().movingOccluders.add(new OccluderImpl(poly));
			}
		}else if (playerEvent.getType() == PlayerStatusEvent.PLAYER_RECALC_PATH_EVERY_FRAME){
			PlayerStatusEvent ev = (PlayerStatusEvent)playerEvent;
			if (ev.getValue() == 0){
				setRecalcPathOnEveryUpdate(false);
			}
			else{
				setRecalcPathOnEveryUpdate(true);
			}
		}else if (playerEvent.getType() == PlayerStatusEvent.PLAYER_CALC_VISION){
			PlayerStatusEvent ev = (PlayerStatusEvent)playerEvent;
			if (ev.getValue() == 0){
				setCalcVision(false);
			}
			else{
				setCalcVision(true);
			}
		}else if (playerEvent.getType() == PlayerStatusEvent.PLAYER_SPEED_CHANGE){
			PlayerStatusEvent ev = (PlayerStatusEvent)playerEvent;
			this.setSpeed(ev.getValue());
			//System.out.println(this.getClass().getSimpleName()+": PLAYER_SPEED_CHANGE");
		}else if (playerEvent.getType() == PlayerStatusEvent.PLAYER_MAX_CONNECTION_DIST){
			PlayerStatusEvent ev = (PlayerStatusEvent)playerEvent;
			this.setMaxConnectionDist(ev.getValue());
		}
	}

	public void beforeLastUpdate(){
		if (calcPathNeeded || recalcPathOnEveryUpdate){
			calcPath();
		}
		calcPathNeeded = false;
	}
	protected void calcPath(){
//		for (int i = 0; i < 50; i++){
			pathData = getPathFinder().calc(getPos(), target, maxConnectionDist, this.getWorld().getObstacleManager().getNodeConnector(), this.getWorld().getObstacleManager().getTileBag().getTileArray());
//		}
	}
	public void afterLastUpdate(){
//		if (sightField.getSightPolygon() == null){
//		for (int i = 0; i < 50; i++){
		if (calcVision){
			// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
			visionData.copyAndTransformEyeAndBoundaryPolygon(pos, lookAngle);
			visionFinder.calc(visionData, getWorld().getOccluderTileArray(), getWorld().movingOccluders);
		}
//		}
//		}
	}

	// this method is only for displaying the tempReachableNodes, it is not performant...
	public ArrayList<KNode>[] getStartAndEndTempReachableNodes(){
		ArrayList<KNode>[] array = new ArrayList[2];
		return array;
	}


	KPoint currentTargetPoint = null;
	public void update(double seconds, double startTime){
		if (speed == 0){
			return;
		}
		// update the player's position as it travels from point to point along the path.
		double secondsLeft = seconds;
		for (int i = 0; i < getPathPoints().size(); i++){
			currentTargetPoint = getPathPoints().get(i);
			KPoint oldPos = new KPoint();
			oldPos.x = pos.x;
			oldPos.y = pos.y;
			//System.out.println(this.getClass().getSimpleName()+": targetX == "+targetX+", x == "+x+", targetY == "+targetY+", y == "+y);
			double distUntilTargetReached = KPoint.distance(currentTargetPoint.x, currentTargetPoint.y, pos.x, pos.y);
			double timeUntilTargetReached = distUntilTargetReached/speed;
			assert timeUntilTargetReached >= 0 : timeUntilTargetReached;
			double xCoordToWorkOutAngle = currentTargetPoint.x - pos.x;
			double yCoordToWorkOutAngle = currentTargetPoint.y - pos.y;
			if (xCoordToWorkOutAngle != 0 || yCoordToWorkOutAngle != 0) {
				double dirAngle = KPoint.findAngle(0, 0, xCoordToWorkOutAngle, yCoordToWorkOutAngle);//(float)Math.atan(yCoordToWorkOutAngle/xCoordToWorkOutAngle);
				moveAngle = (float)dirAngle;
				speedX = (float)Math.cos(moveAngle) * speed;
				speedY = (float)Math.sin(moveAngle) * speed;
			}else{
				speedX = 0f;
				speedY = 0f;
			}
			if (secondsLeft >= timeUntilTargetReached){
				pos.x = currentTargetPoint.x;
				pos.y = currentTargetPoint.y;
				speedX = 0f;
				speedY = 0f;
				secondsLeft -= timeUntilTargetReached;
				assert i == 0 : "i == "+i;
				// remove the current node from the pathNodes list since we've now reached it
				getPathPoints().remove(i);
				i--;
			}else{
				//s = t(u + v)/2
				pos.x = (float)(oldPos.x + secondsLeft * speedX);
				pos.y = (float)(oldPos.y + secondsLeft * speedY);
				secondsLeft = 0;
				break;
			}
		}
		doRotation(seconds, startTime);
	}

	public void doRotation(double seconds, double timeAtStartOfMoveSeconds){
		float oldAngle = lookAngle;
		float currentLookAngleTarget;
		if (speedX == 0 && speedY == 0){
			currentLookAngleTarget = stationaryAngleTarget;
		}else{
			currentLookAngleTarget = moveAngle;
		}
		float angleToTurn = (float) (currentLookAngleTarget - oldAngle);
		// Here we make sure angleToTurn is between -Math.PI and +Math.PI so
		// that it's easy to know which way we should turn.
		// The maximum that angleToTurn could be now is +/-2*Math.PI.
		if (angleToTurn < -Math.PI) {
			angleToTurn += (float) (2 * Math.PI);
			if (angleToTurn < -Math.PI) {
				angleToTurn += (float) (2 * Math.PI);
			}
		}
		if (angleToTurn > Math.PI) {
			angleToTurn -= (float) (2 * Math.PI);
			if (angleToTurn > Math.PI) {
				angleToTurn -= (float) (2 * Math.PI);
			}
		}
		assert angleToTurn >= -(float)Math.PI && angleToTurn <= (float)Math.PI : angleToTurn + ", " + Math.PI;
		float maxAngleChange = (float) (rotationSpeed * seconds);
		double timeUsed = 0;
		if (angleToTurn > 0) {
			if (angleToTurn > maxAngleChange) {
				lookAngle = oldAngle + maxAngleChange;
				timeUsed = seconds;
			} else {
				lookAngle = currentLookAngleTarget;//oldAngle + angleToTurn;
				timeUsed = Math.abs(angleToTurn/rotationSpeed);
			}
		} else {
			if (angleToTurn < -maxAngleChange) {
				lookAngle = oldAngle - maxAngleChange;
				timeUsed = seconds;
			} else {
				lookAngle = currentLookAngleTarget;//oldAngle + angleToTurn;
				timeUsed = Math.abs(angleToTurn/rotationSpeed);
			}
		}
		if (lookAngle < 0) {
			lookAngle += (float) (2 * Math.PI);
		}
		if (lookAngle >= 2 * Math.PI) {
			lookAngle -= (float) (2 * Math.PI);
		}
		assert stationaryAngleTarget >= 0 : stationaryAngleTarget;
		assert lookAngle >= 0 : lookAngle;
	}

	protected boolean playerOutsideOfObstacles(){
		for (int i = 0; i < getWorld().getObstacles().size(); i++){
			KPolygon innerPolygon = getWorld().getObstacles().get(i).getInnerPolygon();
			if (innerPolygon.contains(getPos())){
				return false;
			}
		}
		return true;
	}
	
	public PathFinder getPathFinder(){
		return pathFinder;
	}

	public GameWorld getWorld() {
		return world;
	}

	public void setWorld(GameWorld world) {
		this.world = world;
		pathFinder = new PathFinder();
	}

	public KPoint getPos() {
		return pos;
	}

	public float getSpeed() {
		return speed;
	}

	public float getSpeedX() {
		return speedX;
	}

	public float getSpeedY() {
		return speedY;
	}

	public KPoint getTarget() {
		return target;
	}

	public float getMoveAngle() {
		return moveAngle;
	}

	public float getLookAngle() {
		return lookAngle;
	}

	public void setPos(KPoint pos) {
		this.pos = pos;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setTarget(KPoint target) {
		this.target = target;
	}

	public ArrayList<KPoint> getPathPoints() {
		return pathData.points;
	}

	public boolean isRecalcPathOnEveryUpdate() {
		return recalcPathOnEveryUpdate;
	}

	public void setRecalcPathOnEveryUpdate(boolean recalcPathOnEveryUpdate) {
		this.recalcPathOnEveryUpdate = recalcPathOnEveryUpdate;
	}

	public void setCalcVision(boolean calcVision) {
		this.calcVision = calcVision;
	}


	public float getMaxConnectionDist() {
		return maxConnectionDist;
	}

	public void setMaxConnectionDist(float maxConnectionDist) {
		this.maxConnectionDist = maxConnectionDist;
	}


	public ViewPane getView(){
		return getLoop().getView();
	}

	public GameLoop getLoop(){
		return loop;
	}
	public void setLoop(GameLoop loop) {
		if (this.loop != loop){
			this.loop = loop;
			loop.setPlayer(this);
		}
	}
}
