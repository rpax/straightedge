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

import straightedge.geom.AABB;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.VisionDataRotation;
import straightedge.geom.vision.VisionFinder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;



/**
 *
 * @author Keith
 */
public class Player implements TargetUser{
	public World world;
	KPoint pos;
	TargetFinder targetFinder;
	KPolygon originalPolygon;
	KPolygon polygon;
	double maxConnectionDistance;
	double speed;
	double speedX;
	double speedY;
	double moveAngle;
	KPoint currentTargetPoint = null;
	double mouseAngle;
	double angle;
	double rotationSpeed = Math.PI;
	Gun gun;
	double health;
	double maxHealth;

	boolean dead = false;
	double deathTime = -1;
	double respawnTime = 1;

	VisionFinder visionFinder;
	VisionDataRotation cache;
	double smallAmount = 0.0001f;

	public Player(World world, KPoint pos){
		this.world = world;
		this.pos = pos;
		targetFinder = new TargetFinder(this, world);
		targetFinder.respawn();
		targetFinder.setFixedTarget(pos, false);
		originalPolygon = KPolygon.createRegularPolygon(10, 5);
		originalPolygon.getPoints().get(0).x += 5;
		originalPolygon.calcAll();
		copyAndTransformPolygon();
		maxConnectionDistance = 1000f;
		speed = 100;
		gun = new Gun(world);
		gun.player = this;

		{
			int numPoints = 50;
			float radius = 90;
			KPolygon originalBoundaryPolygon = KPolygon.createRegularPolygon(numPoints, radius);
			originalBoundaryPolygon.scale(1, 0.6);
			originalBoundaryPolygon.translate(50, 0);
			// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
			KPoint originalEye = new KPoint(smallAmount, smallAmount);
			visionFinder = new VisionFinder();
			cache = new VisionDataRotation(originalEye, originalBoundaryPolygon);
		}

		respawn(pos);
	}

	public void respawn(KPoint spawnPos){
		this.pos.setCoords(spawnPos);
		targetFinder.setFixedTarget(pos, true);
		copyAndTransformPolygon();

		dead = false;
		deathTime = -1;
		maxHealth = 1;
		health = maxHealth;
	}

	public boolean isMoving(){
		if (speedX != 0 || speedY != 0){
			return false;
		}
		return true;
	}

	public void copyAndTransformPolygon(){
		polygon = originalPolygon.copy();
		polygon.translateTo(pos);
		polygon.rotate(angle, pos);
	}

	public void nowAtTimeStop(double timeNow){
		if (dead){
			if (deathTime + respawnTime < timeNow){
				if (isBot()){
					KPoint newPos = world.getNearestPointOutsideOfObstacles(world.makeRandomPointWithin(world.enemySpawnAABB));
					respawn(newPos);
				}else{
					KPoint newPos = world.getNearestPointOutsideOfObstacles(world.makeRandomPointWithin(world.playerSpawnAABB));
					respawn(newPos);
				}
			}
		}else{
			if (isBot()){
				playerVisible = false;
				if (cache.getVisiblePolygon() != null && cache.getVisiblePolygon().intersects(world.player.polygon)){
					playerVisible = true;
					if (world.player.dead == false && world.player.pos.distance(pos) < minFollowDist){
						// too close, so move away from player to be able to shoot him
						KPoint target = world.player.pos.createPointToward(pos, minFollowDist-0.01);
						KPoint targetAdjusted = world.getNearestPointOutsideOfObstacles(target);
						targetFinder.setFixedTarget(targetAdjusted, true);
					}else{
						KPoint target = world.player.pos.copy();
						KPoint targetAdjusted = world.getNearestPointOutsideOfObstacles(target);
						targetFinder.setFixedTarget(targetAdjusted, true);
					}
				}else{
					KPoint targetAdjusted = targetFinder.getAbsoluteTarget();
					//System.out.println(this.getClass().getSimpleName()+": bot.pos == "+this.pos+", targetAdjusted == "+targetAdjusted+", targetFinder.getPathPoints().size() == "+targetFinder.getPathPoints().size());
					if (targetAdjusted == null || pos.distanceSq(targetAdjusted) < 10 || targetFinder.pathData.isError()){
						//System.out.println(this.getClass().getSimpleName()+": targetAdjusted == null || pos.distanceSq(targetAdjusted) < 10");
						KPoint target = world.makeRandomPointWithin(world.innerAABB);
						targetAdjusted = world.getNearestPointOutsideOfObstacles(target);
						targetFinder.setFixedTarget(targetAdjusted, true);
					}
				}
			}else{
				if (targetFinder.isTargetRelative()){
					targetFinder.calcPath();
				}
			}
		}
	}

	public void doMove(double seconds, double startTime){
		if (dead == false){
			gun.doMoveAndBulletFire(seconds, startTime);
		}
	}

	double minFollowDist = 10;
	boolean playerVisible = false;
	public void doMoveBetweenGunFires(double seconds, double startTime){
		// update the player's position as it travels from point to point along the path.
		double secondsLeft = seconds;
		ArrayList<KPoint> pathPoints = targetFinder.getPathData().getPoints();
		for (int i = 0; i < pathPoints.size(); i++){
			currentTargetPoint = pathPoints.get(i);
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
				moveAngle = KPoint.findAngle(0, 0, xCoordToWorkOutAngle, yCoordToWorkOutAngle);//(float)Math.atan(yCoordToWorkOutAngle/xCoordToWorkOutAngle);
				speedX = Math.cos(moveAngle) * speed;
				speedY = Math.sin(moveAngle) * speed;
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
				pathPoints.remove(i);
				i--;
			}else{
				pos.x = oldPos.x + secondsLeft * speedX;
				pos.y = oldPos.y + secondsLeft * speedY;
				secondsLeft = 0;
				break;
			}
		}

		// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
		cache.copyAndTransformEyeAndBoundaryPolygon(pos.x + smallAmount, pos.y + smallAmount, gun.angle);
		visionFinder.calc(cache, world.allOccluders);

		if (isBot()){
			if (playerVisible){
				mouseAngle = pos.findAngle(world.player.pos);
				gun.startFiring(startTime);
			}else{
				mouseAngle = moveAngle;
				gun.stopFiring();
			}
		}else{
			if (pos.equals(world.main.eventHandler.lastMousePointInWorldCoords) == false){
				mouseAngle = KPoint.findAngle(pos, world.main.eventHandler.lastMousePointInWorldCoords);
			}
		}

		copyAndTransformPolygon();
		doRotation(seconds, startTime);

	}

	public boolean isBot(){
		if (this != world.player){
			return true;
		}else{
			return false;
		}
	}

	public void doRotation(double seconds, double startTime){
		double oldAngle = angle;
		double targetAngle;
		if (speedX == 0 && speedY == 0){
			// stationary, so turn in direction of mouse.
			targetAngle = mouseAngle;
		}else{
			// moving, so turn in direction of destination.
			targetAngle = moveAngle;
		}
		double angleToTurn = targetAngle - oldAngle;
		// Here we make sure angleToTurn is between -Math.PI and +Math.PI so
		// that it's easy to know which way we should turn.
		// The maximum/minimum that angleToTurn could be now is +/-2*Math.PI.
		while (angleToTurn < -Math.PI) {
			angleToTurn += 2 * Math.PI;
		}
		while (angleToTurn > Math.PI) {
			angleToTurn -= 2 * Math.PI;
		}
		assert angleToTurn >= -Math.PI && angleToTurn <= Math.PI : angleToTurn + ", " + Math.PI;
		double maxAngleChange = rotationSpeed * seconds;
		double timeUsed = 0;
		if (angleToTurn > 0) {
			if (angleToTurn > maxAngleChange) {
				angle = oldAngle + maxAngleChange;
				timeUsed = seconds;
			} else {
				angle = targetAngle;
				timeUsed = Math.abs(angleToTurn/rotationSpeed);
			}
		} else {
			if (angleToTurn < -maxAngleChange) {
				angle = oldAngle - maxAngleChange;
				timeUsed = seconds;
			} else {
				angle = targetAngle;
				timeUsed = Math.abs(angleToTurn/rotationSpeed);
			}
		}
		if (angle < 0) {
			angle += 2 * Math.PI;
		}
		if (angle >= 2 * Math.PI) {
			angle -= 2 * Math.PI;
		}
		assert mouseAngle >= 0 : mouseAngle;
		assert angle >= 0 : angle;
	}
	public void die(double timeNow){
		dead = true;
		this.deathTime = timeNow;
		if (isBot()){
			// this Player is a bot so stop firing.
			gun.stopFiring();
		}
	}

	public KPoint getPos() {
		return pos;
	}

	AABB originalBoundaryPolygonAABB = null;
	public AcceleratedImage ai;
	public void makeImage(){
		originalBoundaryPolygonAABB = cache.getOriginalBoundaryPolygon().getAABB();
		int picW = (int)Math.ceil(originalBoundaryPolygonAABB.getWidth());
		int picH = (int)Math.ceil(originalBoundaryPolygonAABB.getHeight());

		BufferedImage image = new BufferedImage(picW, picH, BufferedImage.TYPE_INT_ARGB);
		int[] imagePixelData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		double alphaEye = 0.3;
		double redEye = 1;
		double greenEye = 1;
		double blueEye = 1;

		double alphaEdge = 0;
		double redEdge = 1;
		double greenEdge = 1;
		double blueEdge = 1;
		for (int i = 0; i < picW; i++){
			for (int j = 0; j < picH; j++){
				KPoint p = new KPoint();
				p.x = originalBoundaryPolygonAABB.getX() + i;
				p.y = originalBoundaryPolygonAABB.getY() + j;
				KPoint boundaryP = cache.getOriginalBoundaryPolygon().getClosestIntersectionToFirstFromSecond(cache.getOriginalEye(), cache.getOriginalEye().createPointToward(p, cache.getOriginalBoundaryPolygon().getRadius()*2));
				double eyeToPixelDist = cache.getOriginalEye().distanceSq(p);
				double eyeThruPixelToBoundaryDist =  cache.getOriginalEye().distanceSq(boundaryP);
				double lightness = (1 - eyeToPixelDist/eyeThruPixelToBoundaryDist);
				if (lightness < 0){
					lightness = 0;
				}
				double alpha = alphaEye*lightness + alphaEdge*(1-lightness);
				double red = redEye*lightness + redEdge*(1-lightness);
				double green = greenEye*lightness + greenEdge*(1-lightness);
				double blue = blueEye*lightness + blueEdge*(1-lightness);
				imagePixelData[j*picW+i]= (int)(alpha*255) << 24 | (int)(red*255) << 16 | (int)(green*255) << 8 | (int)(blue*255);
			}
		}
		ai = new AcceleratedImage(image);
	}

	public AcceleratedImage ai2;
	public void makeImage2(){
		Color fogColor = world.main.view.fogColor;
		originalBoundaryPolygonAABB = cache.getOriginalBoundaryPolygon().getAABB();
		int picW = (int)Math.ceil(originalBoundaryPolygonAABB.getWidth());
		int picH = (int)Math.ceil(originalBoundaryPolygonAABB.getHeight());

		BufferedImage image = new BufferedImage(picW, picH, BufferedImage.TYPE_INT_ARGB);
		int[] imagePixelData = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		double alphaEye = 0;
		double redEye = fogColor.getRed()/255f;
		double greenEye = fogColor.getGreen()/255f;
		double blueEye = fogColor.getBlue()/255f;

		double alphaEdge = fogColor.getAlpha()/255f;
		double redEdge = fogColor.getRed()/255f;
		double greenEdge = fogColor.getGreen()/255f;
		double blueEdge = fogColor.getBlue()/255f;
		for (int i = 0; i < picW; i++){
			for (int j = 0; j < picH; j++){
				KPoint p = new KPoint();
				p.x = originalBoundaryPolygonAABB.getX() + i;
				p.y = originalBoundaryPolygonAABB.getY() + j;
				KPoint boundaryP = cache.getOriginalBoundaryPolygon().getClosestIntersectionToFirstFromSecond(cache.getOriginalEye(), cache.getOriginalEye().createPointToward(p, cache.getOriginalBoundaryPolygon().getRadius()*2));
				double eyeToPixelDist = cache.getOriginalEye().distanceSq(p);
				double eyeThruPixelToBoundaryDist =  cache.getOriginalEye().distanceSq(boundaryP);
				double lightness = (1 - eyeToPixelDist/eyeThruPixelToBoundaryDist);
				if (lightness < 0){
					lightness = 0;
				}
				//lightness = Math.pow(lightness, 0.75);
				//lightness = Math.pow(lightness, 2);
				double alpha = alphaEye*lightness + alphaEdge*(1-lightness);
				double red = redEye*lightness + redEdge*(1-lightness);
				double green = greenEye*lightness + greenEdge*(1-lightness);
				double blue = blueEye*lightness + blueEdge*(1-lightness);
				imagePixelData[j*picW+i]= (int)(alpha*255) << 24 | (int)(red*255) << 16 | (int)(green*255) << 8 | (int)(blue*255);
			}
		}
		ai2 = new AcceleratedImage(image);
	}
	public World getWorld(){
		return world;
	}

}
