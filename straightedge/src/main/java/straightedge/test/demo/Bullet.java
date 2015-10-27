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

import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.OccluderImpl;
import java.util.ArrayList;

/**
 *
 * @author Keith
 */

public class Bullet{
	public World world;
	public Player ownerGunUser;
	public double x;
	public double y;
	public double speed;
	public double speedX;
	public double speedY;
	double accelX;
	double accelY;
	public double spawnTimeSeconds;
	public boolean dead;
	public Player playerThatWasHit = null;
	public double oldX;
	public double oldY;

	public double radius;
	public double maxSpeed;
	public double damage;
	public double angle;
	public double lifeTimeSeconds;

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;

	public Bullet(Gun gun, Player gunUser, double newX, double newY, double angle, double spawnTimeSeconds, double xLaunchSpeed, double yLaunchSpeed) {
		world = gun.world;
		this.ownerGunUser = gunUser;
		this.spawnTimeSeconds = spawnTimeSeconds;
		dead = false;
		radius = 1f;
		damage = 4f;
		this.angle = angle;
		speed = 400;//1000;
		double randomRangeIncrement = 0;
		double range = 300;
		speedX = xLaunchSpeed + Math.cos(angle) * speed;
		speedY = yLaunchSpeed + Math.sin(angle) * speed;
		double launchSpeed = speed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
		lifeTimeSeconds = range / launchSpeed;

		this.x = newX;
		this.y = newY;
		oldX = x;
		oldY = y;
	}

	public void afterLastUpdate(){
//		sightField.recreateAndTransform(x, y, angle);
//		KPolygon currentSightPolygon = sightField.getSightPolygon();
//		ArrayList<OccluderImpl> polygons = getWorld().getPolygonTileGrid().getObstaclesWithin(currentSightPolygon.getCenter(), currentSightPolygon.getCircularBound());
//		ArrayList<SPObstObstIntersection> obstacleIntersectionPoints = getWorld().getPolygonTileGrid().getIntersectionsWithin(currentSightPolygon.getCenter(), currentSightPolygon.getCircularBound());
//		sightField.intersectSightPolygon(polygons, obstacleIntersectionPoints, getWorld().movingPolygons);
	}

	public void doMove(double seconds, double startTime) {
		assert dead == false : "dead == " + dead;
		assert seconds >= 0 : seconds;
		if (spawnTimeSeconds + lifeTimeSeconds < startTime + seconds) {
			seconds = spawnTimeSeconds + lifeTimeSeconds - startTime;
			if (seconds > 0){
				doBulletMove(seconds, startTime);
			}
			dead = true;
		} else {
			doBulletMove(seconds, startTime);
//			assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		}
	}

	ArrayList<OccluderIntersection> previousIntersections = new ArrayList<OccluderIntersection>();
	ArrayList<OccluderIntersection> currentIntersections = new ArrayList<OccluderIntersection>();
	protected void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;

		double secondsLeft = seconds;
		double timeAtStartOfMoveSecondsAdjusted = timeAtStartOfMoveSeconds;
		double[] recentSecondsFromStartToImpact = new double[5];
		int recentSecondsFromStartToImpactIndex = 0;
		int impacts = 0;
		while (true){
			double distLeft = speed * secondsLeft;
			double xIncrement = speedX * secondsLeft;
			double yIncrement = speedY * secondsLeft;
			oldX = x;
			oldY = y;
			x += xIncrement;
			y += yIncrement;


			double approxDistCoveredHalved = (Math.abs(xIncrement) + Math.abs(yIncrement))/2;
			double midPointX = (x + oldX)/2f;
			double midPointY = (y + oldY)/2f;
			ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(midPointX, midPointY, approxDistCoveredHalved);
			//codeTimer.click("");

			boolean touch = false;
			OccluderIntersection closestOccluderIntersection = null;
			double distToClosestHitObstacle = Double.MAX_VALUE;
			ObstacleLoop:
			for (int i = 0; i < obstacles.size(); i++) {
				OccluderImpl obstacle = obstacles.get(i);
				KPolygon shape = obstacle.getPolygon();
//				double error = 0.1f;
//				if (KPoint.distance(oldX, oldY, shape.getCenter().x, shape.getCenter().y) > shape.getRadius() + approxDistCovered + error) {
//					continue;
//				}
				ArrayList<KPoint> points = shape.getPoints();
				for (int j = 0; j < points.size(); j++) {
					for (int k = 0; k < previousIntersections.size(); k++){
						OccluderIntersection occluderIntersection = previousIntersections.get(k);
						if (obstacle == occluderIntersection.occluderImpl && j == occluderIntersection.occluderSideJ){
							continue ObstacleLoop;
						}
					}

					int jPlus = (j+1 == points.size() ? 0 : j+1);
					if (KPoint.linesIntersect(oldX, oldY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y)){
						KPoint intersection = KPoint.getLineLineIntersection(oldX, oldY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y);
						if (intersection == null){
							continue;
						}
						double distToIntersection = intersection.distance(oldX, oldY);
						OccluderIntersection occluderIntersection = new OccluderIntersection(distToIntersection, intersection, obstacle, j, jPlus);
						currentIntersections.add(occluderIntersection);
						if (distToIntersection < distToClosestHitObstacle) {
							distToClosestHitObstacle = distToIntersection;
							closestOccluderIntersection = occluderIntersection;
							touch = true;
						}
					}
				}
			}
			//ArrayList players = world.enemies.getAllWithin(midPointX, midPointY, approxDistCoveredHalved);
			//players.add(world.player);
			ArrayList<Player> players = new ArrayList<Player>();
			players.add(world.player);
			players.addAll(world.enemies);
			Player hitPlayer = null;
			double distToClosestHitPlayer = Double.MAX_VALUE;
			for (int i = 0; i < players.size(); i++) {
				Player p = (Player)players.get(i);
				if (p.dead == true){
					continue;
				}
				if (p.polygon.intersectsLine(oldX, oldY, x, y)) {
					// The below is not really the right distance to where the
					// player was hit, but it is an OK approximation.
					double dist = KPoint.distance(oldX, oldY, x, y);
					if (dist < distToClosestHitPlayer) {
						distToClosestHitPlayer = dist;
						touch = true;
						hitPlayer = p;
					}
				}
			}

			if (touch) {
				if (distToClosestHitPlayer < distToClosestHitObstacle) {
					hit(hitPlayer, timeAtStartOfMoveSeconds);
					previousIntersections.clear();
					currentIntersections.clear();
					break;
				}else{
					hitObstacle(closestOccluderIntersection.occluderImpl, timeAtStartOfMoveSeconds);
					double secondsFromStartToImpact = secondsLeft*distToClosestHitObstacle/distLeft;
					secondsLeft -= secondsFromStartToImpact;
					timeAtStartOfMoveSecondsAdjusted += secondsFromStartToImpact;

					KPoint incident = new KPoint(oldX, oldY);
					KPoint surface = closestOccluderIntersection.occluderImpl.getPolygon().points.get(closestOccluderIntersection.occluderSideJ).copy();

					incident.x -= closestOccluderIntersection.intersection.x;
					incident.y -= closestOccluderIntersection.intersection.y;
					surface.x -= closestOccluderIntersection.intersection.x;
					surface.y -= closestOccluderIntersection.intersection.y;

					double surfaceLength = Math.sqrt(surface.x*surface.x + surface.y*surface.y);
					// normalise the surface:
					surface.x /= surfaceLength;
					surface.y /= surfaceLength;
					double dotproduct = incident.x * surface.x + incident.y * surface.y;
					KPoint incidentProjectedOntoSurface = new KPoint(surface.x * dotproduct, surface.y * dotproduct);
					KPoint reflect = new KPoint(incident.x - 2*incidentProjectedOntoSurface.x, incident.y - 2*incidentProjectedOntoSurface.y);
//					System.out.println(this.getClass().getSimpleName()+": incident == "+incident);
//					System.out.println(this.getClass().getSimpleName()+": surface == "+surface);
//					//System.out.println(this.getClass().getSimpleName()+": normal == "+normal);
//					System.out.println(this.getClass().getSimpleName()+": incidentProjectedOntoSurface == "+incidentProjectedOntoSurface);
//					System.out.println(this.getClass().getSimpleName()+": reflect == "+reflect);
					angle = reflect.findSignedAngleFromOrigin();
//					System.out.println(this.getClass().getSimpleName()+": angle == "+angle);


					x = closestOccluderIntersection.intersection.x;
					y = closestOccluderIntersection.intersection.y;
					speedX = Math.cos(angle) * speed;
					speedY = Math.sin(angle) * speed;
					oldX = x;
					oldY = y;
					previousIntersections.clear();
					previousIntersections.addAll(currentIntersections);
					currentIntersections.clear();

					// check that the bullet isn't intersecting in an infinite loop.
					impacts++;
					recentSecondsFromStartToImpact[recentSecondsFromStartToImpactIndex] = secondsFromStartToImpact;
					double sum = 0;
					for (int i = 0; i < recentSecondsFromStartToImpact.length; i++){
						sum += recentSecondsFromStartToImpact[i];
					}
					recentSecondsFromStartToImpactIndex = (recentSecondsFromStartToImpactIndex + 1 >= recentSecondsFromStartToImpact.length ? 0 : recentSecondsFromStartToImpactIndex + 1);
					if (impacts > 5 && sum < 0.0001){
						// there is likely to be a rogue bullet that needs getting rid of
						dead = true;
						break;
					}
					continue;
				}
			}
			previousIntersections.clear();
			currentIntersections.clear();
			break;
		}
	}

	public static class OccluderIntersection{
		public double distToIntersection;
		public KPoint intersection;
		public OccluderImpl occluderImpl;
		public int occluderSideJ;
		public int occluderSideJPlus;
		public OccluderIntersection(double distToIntersection, KPoint intersection, OccluderImpl occluderImpl, int occluderSideJ, int occluderSideJPlus){
			this.distToIntersection = distToIntersection;
			this.intersection = intersection;
			this.occluderImpl = occluderImpl;
			this.occluderSideJ = occluderSideJ;
			this.occluderSideJPlus = occluderSideJPlus;
		}
	}

	public void hitObstacle(OccluderImpl hitObstacle, double timeOfHit){
		//dead = true;
	}
	public void hit(Player hitPlayer, double timeOfHit){
		//hitPlayer.takeDamage(this, timeOfHit);
		this.playerThatWasHit = hitPlayer;
		dead = true;
		playerThatWasHit.health -= this.damage;
		if (playerThatWasHit.health <= 0){
			playerThatWasHit.die(timeOfHit);
		}
	}
	public boolean isDead(){
		return dead;
	}
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
}















///*
// * Copyright (c) 2008, Keith Woodward
// *
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// * 3. Neither the name of Keith Woodward nor the names
// *    of its contributors may be used to endorse or promote products derived
// *    from this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// *
// */
//package straightedge.test.demo;
//
//import straightedge.geom.KPoint;
//import straightedge.geom.KPolygon;
//import straightedge.geom.vision.OccluderImpl;
//import java.awt.geom.Point2D;
//import java.util.ArrayList;
//
///**
// *
// * @author Keith
// */
//
//public class Bullet{
//	public World world;
//	public Player ownerGunUser;
//	public double x;
//	public double y;
//	public double speedX;
//	public double speedY;
//	double accelX;
//	double accelY;
//	public double spawnTimeSeconds;
//	public boolean dead;
//	public Player playerThatWasHit = null;
//	public double backX;
//	public double backY;
//	public double oldBackX;
//	public double oldBackY;
//
//	public double radius;
//	public double length;
//	public double maxSpeed;
//	public double damage;
//	public double angle;
//	public double lifeTimeSeconds;
//
//	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
//
////	SightField sightField;
//
//	public Bullet(Gun gun, Player gunUser, double newX, double newY, double angle, double spawnTimeSeconds, double xLaunchSpeed, double yLaunchSpeed) {
//		world = gun.world;
//		this.ownerGunUser = gunUser;
//		this.spawnTimeSeconds = spawnTimeSeconds;
//		dead = false;
////		assert Point2D.distance(ownerGunUser.getX(), ownerGunUser.getY(), newX, newY) < ownerGunUser.getR() : Point2D.distance(ownerGunUser.getX(), ownerGunUser.getY(), newX, newY);
//		radius = 2f;
//		length = 2*radius;
//		double damagePerSecond = 200f;
//		damage = gun.getReloadSeconds()*damagePerSecond;//4f;
//		this.angle = angle;
//		/*gun.getWorld().getRandom().setSeed(gun.getSeed());
//		gun.setSeed(gun.getSeed()+3041);
//		float randomSpeedIncrement = world.getRandom().nextFloat()*200;*/
//		double startSpeed = 1000;//1500;//600;// + randomSpeedIncrement;
//		double randomRangeIncrement = 0;//(float)Math.random()*200;
//		double range = 1000;//1500;//180 + randomRangeIncrement;
//		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
//		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;
//		//float accel = -50;
//		//accelX = (float) Math.cos(angle) * accel;
//		//accelY = (float) Math.sin(angle) * accel;
//		double launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
//		lifeTimeSeconds = range / launchSpeed;
//
//		this.x = newX + (float) Math.cos(angle) * length;
//		this.y = newY + (float) Math.sin(angle) * length;
//		backX = newX;
//		backY = newY;
//		oldBackX = backX;
//		oldBackY = backY;
//	}
//
//	public void afterLastUpdate(){
////		sightField.recreateAndTransform(x, y, angle);
////		KPolygon currentSightPolygon = sightField.getSightPolygon();
////		ArrayList<OccluderImpl> polygons = getWorld().getPolygonTileGrid().getObstaclesWithin(currentSightPolygon.getCenter(), currentSightPolygon.getCircularBound());
////		ArrayList<SPObstObstIntersection> obstacleIntersectionPoints = getWorld().getPolygonTileGrid().getIntersectionsWithin(currentSightPolygon.getCenter(), currentSightPolygon.getCircularBound());
////		sightField.intersectSightPolygon(polygons, obstacleIntersectionPoints, getWorld().movingPolygons);
//	}
//
//	public void doMove(double seconds, double startTime) {
//		assert dead == false : "dead == " + dead;
//		assert seconds >= 0 : seconds;
////		assert spawnTimeSeconds <= timeAtStartOfMoveSeconds + seconds : "this bullet was spawned in the future! getSSCode() == " + getSSCode() + ", spawnTimeSeconds == " + (spawnTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
////		assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
//		if (spawnTimeSeconds + lifeTimeSeconds < startTime + seconds) {
//			seconds = spawnTimeSeconds + lifeTimeSeconds - startTime;
//			if (seconds > 0){
//				doBulletMove(seconds, startTime);
//			}
//			dead = true;
//		} else {
//			doBulletMove(seconds, startTime);
////			assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
//		}
//		oldBackX = backX;
//		oldBackY = backY;
//	}
//
//	protected void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
////		codeTimer.setEnabled(false);
////		codeTimer.click("");
//		assert Double.isNaN(x) == false;
//		assert seconds >= 0 : seconds;
//		double newSpeedX = (float) (speedX + accelX * seconds);
//		double newSpeedY = (float) (speedY + accelY * seconds);
//		double xIncrement = (float) (((newSpeedX + speedX) / 2f) * seconds);
//		double yIncrement = (float) (((newSpeedY + speedY) / 2f) * seconds);
//		x += xIncrement;
//		y += yIncrement;
//		backX += xIncrement;
//		backY += yIncrement;
//
//		speedX = newSpeedX;
//		speedY = newSpeedY;
//		//codeTimer.click("");
//		boolean touch = false;
//		KPoint pos = new KPoint(x, y);
////		ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(pos, length);
//		double approxDistCoveredHalved = (Math.abs(xIncrement) + Math.abs(yIncrement) + length)/2;
//		double midPointX = (x + backX)/2f;
//		double midPointY = (y + backY)/2f;
//		ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(midPointX, midPointY, approxDistCoveredHalved);
//		//codeTimer.click("");
//		OccluderImpl hitObstacle = null;
//		KPoint closestIntersection = null;
//		double distToClosestHitObstacle = Double.MAX_VALUE;
//		//float distCovered = (float) Math.pow(Math.pow(xIncrement, 2) + Math.pow(yIncrement, 2), 0.5f);
//		double distCovered = Math.abs(xIncrement) + Math.abs(yIncrement);
//		for (int i = 0; i < obstacles.size(); i++) {
//			OccluderImpl obstacle = obstacles.get(i);
//			KPolygon shape = obstacle.getPolygon();
//			double error = 0.1f;
//			if (KPoint.distance(x, y, shape.getCenter().x, shape.getCenter().y) > shape.getRadius() + length + distCovered + error) {
//				continue;
//			}
//			ArrayList<KPoint> points = shape.getPoints();
//			for (int j = 0; j < points.size(); j++) {
//				int jPlus = (j+1 == points.size() ? 0 : j+1);
//				if (KPoint.linesIntersect(oldBackX, oldBackY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y)){
//					KPoint intersection = KPoint.getLineLineIntersection(oldBackX, oldBackY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y);
//					if (intersection == null){
//						continue;
//					}
//					double dist = intersection.distance(oldBackX, oldBackY);
//					if (dist < distToClosestHitObstacle) {
//						distToClosestHitObstacle = dist;
//						closestIntersection = intersection;
//						touch = true;
//						hitObstacle = obstacle;
//					}
//				}
//			}
//		}
//		//codeTimer.click("");
//		KPoint midPoint = KPoint.midPoint(oldBackX, oldBackY, x, y);
//		double halfDist = KPoint.distance(oldBackX, oldBackY, x, y)/2f;
//		//ArrayList<Player> monsters = getWorld().getMonsters().getAllWithin(midPoint, halfDist);
//		ArrayList<Player> monsters = new ArrayList<Player>();
//		monsters.add(world.player);
//		monsters.addAll(world.enemies);
//		// Note that world.players won't always contain this bullet.ownerGunUser since the ownerGunUser
//		// may have been removed from the world's list, therefore the following assert will
//		// not always be true.
//		// assert players.get(players.indexOf(ownerGunUser)) == ownerGunUser : ownerGunUser+", "+players;
//		Player hitPlayer = null;
//		//Point2D.Double playerIntersection = null;
//		double distToClosestHitPlayer = Double.MAX_VALUE;
//		for (int i = 0; i < monsters.size(); i++) {
//			Player p = monsters.get(i);
//			if (p.dead == true){
//				continue;
//			}
////				if (p == ownerGunUser && timeAtStartOfMoveSeconds < spawnTimeSeconds + getCanNotHitOwnPlayerTimeSeconds()) {
////					continue;
////				}
////			if (p.getTeam() == ((Player)ownerGunUser).getTeam()){
////				continue;
////			}
//			if (p.polygon.intersectsLine(oldBackX, oldBackY, x, y)) {
//				// The below is not really the right distance to where the
//				// ownerGunUser was hit, but it is an OK approximation.
//				double dist = KPoint.distance(oldBackX, oldBackY, x, y);
//				if (dist < distToClosestHitPlayer) {
//					distToClosestHitPlayer = dist;
//					touch = true;
//					hitPlayer = p;
//				}
//			}
//		}
//		//codeTimer.click("");
//
//		if (touch) {
//			if (hitPlayer != null || hitObstacle != null){
//				if (distToClosestHitPlayer < distToClosestHitObstacle) {
//					hit(hitPlayer, timeAtStartOfMoveSeconds);
//				}else{
//					hitObstacle(hitObstacle, timeAtStartOfMoveSeconds);
//				}
//			}
//		}
//		//codeTimer.lastClick();
//	}
//
//
//	public void hitObstacle(OccluderImpl hitObstacle, double timeOfHit){
//		dead = true;
//	}
//	public void hit(Player hitPlayer, double timeOfHit){
//		//hitPlayer.takeDamage(this, timeOfHit);
//		this.playerThatWasHit = hitPlayer;
//		dead = true;
//		playerThatWasHit.health -= this.damage;
//		if (playerThatWasHit.health <= 0){
//			playerThatWasHit.die(timeOfHit);
//		}
//	}
//}
