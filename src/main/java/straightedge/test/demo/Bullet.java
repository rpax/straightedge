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

import java.util.ArrayList;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

import straightedge.geom.KPolygon;
import straightedge.geom.Vector2fUtils;
import straightedge.geom.vision.OccluderImpl;

/**
 *
 * @author Keith
 */

public class Bullet{
	public World world;
	public Player ownerGunUser;
	public float x;
	public float y;
	public float speed;
	public float speedX;
	public float speedY;
	float accelX;
	float accelY;
	public float spawnTimeSeconds;
	public boolean dead;
	public Player playerThatWasHit = null;
	public float oldX;
	public float oldY;

	public float radius;
	public float maxSpeed;
	public float damage;
	public float angle;
	public float lifeTimeSeconds;

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;

	public Bullet(Gun gun, Player gunUser, float newX, float newY, float angle, float spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		world = gun.world;
		this.ownerGunUser = gunUser;
		this.spawnTimeSeconds = spawnTimeSeconds;
		dead = false;
		radius = 1f;
		damage = 4f;
		this.angle = angle;
		speed = 400;//1000;
		float randomRangeIncrement = 0;
		float range = 300;
		speedX = xLaunchSpeed + FastMath.cos(angle) * speed;
		speedY = yLaunchSpeed + FastMath.sin(angle) * speed;
		float launchSpeed = speed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
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

	public void doMove(float seconds, float startTime) {
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
	protected void doBulletMove(float seconds, float timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;

		float secondsLeft = seconds;
		float timeAtStartOfMoveSecondsAdjusted = timeAtStartOfMoveSeconds;
		float[] recentSecondsFromStartToImpact = new float[5];
		int recentSecondsFromStartToImpactIndex = 0;
		int impacts = 0;
		while (true){
			float distLeft = speed * secondsLeft;
			float xIncrement = speedX * secondsLeft;
			float yIncrement = speedY * secondsLeft;
			oldX = x;
			oldY = y;
			x += xIncrement;
			y += yIncrement;


			float approxDistCoveredHalved = (Math.abs(xIncrement) + Math.abs(yIncrement))/2;
			float midPointX = (x + oldX)/2f;
			float midPointY = (y + oldY)/2f;
			ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(midPointX, midPointY, approxDistCoveredHalved);
			//codeTimer.click("");

			boolean touch = false;
			OccluderIntersection closestOccluderIntersection = null;
			float distToClosestHitObstacle = Float.MAX_VALUE;
			ObstacleLoop:
			for (int i = 0; i < obstacles.size(); i++) {
				OccluderImpl obstacle = obstacles.get(i);
				KPolygon shape = obstacle.getPolygon();
//				float error = 0.1f;
//				if (Vector2f.distance(oldX, oldY, shape.getCenter().x, shape.getCenter().y) > shape.getRadius() + approxDistCovered + error) {
//					continue;
//				}
				ArrayList<Vector2f> points = shape.getPoints();
				for (int j = 0; j < points.size(); j++) {
					for (int k = 0; k < previousIntersections.size(); k++){
						OccluderIntersection occluderIntersection = previousIntersections.get(k);
						if (obstacle == occluderIntersection.occluderImpl && j == occluderIntersection.occluderSideJ){
							continue ObstacleLoop;
						}
					}

					int jPlus = (j+1 == points.size() ? 0 : j+1);
					if (Vector2fUtils.linesIntersect(oldX, oldY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y)){
						Vector2f intersection = Vector2fUtils.getLineLineIntersection(oldX, oldY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y);
						if (intersection == null){
							continue;
						}
						float distToIntersection = intersection.distance(new Vector2f(oldX, oldY));
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
			float distToClosestHitPlayer = Float.MAX_VALUE;
			for (int i = 0; i < players.size(); i++) {
				Player p = (Player)players.get(i);
				if (p.dead == true){
					continue;
				}
				if (p.polygon.intersectsLine(oldX, oldY, x, y)) {
					// The below is not really the right distance to where the
					// player was hit, but it is an OK approximation.
					float dist = Vector2fUtils.distance(oldX, oldY, x, y);
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
					float secondsFromStartToImpact = secondsLeft*distToClosestHitObstacle/distLeft;
					secondsLeft -= secondsFromStartToImpact;
					timeAtStartOfMoveSecondsAdjusted += secondsFromStartToImpact;

					Vector2f incident = new Vector2f(oldX, oldY);
					Vector2f surface = closestOccluderIntersection.occluderImpl.getPolygon().points.get(closestOccluderIntersection.occluderSideJ).clone();

					incident.x -= closestOccluderIntersection.intersection.x;
					incident.y -= closestOccluderIntersection.intersection.y;
					surface.x -= closestOccluderIntersection.intersection.x;
					surface.y -= closestOccluderIntersection.intersection.y;

					float surfaceLength = FastMath.sqrt(surface.x*surface.x + surface.y*surface.y);
					// normalise the surface:
					surface.x /= surfaceLength;
					surface.y /= surfaceLength;
					float dotproduct = incident.x * surface.x + incident.y * surface.y;
					Vector2f incidentProjectedOntoSurface = new Vector2f(surface.x * dotproduct, surface.y * dotproduct);
					Vector2f reflect = new Vector2f(incident.x - 2*incidentProjectedOntoSurface.x, incident.y - 2*incidentProjectedOntoSurface.y);
//					System.out.println(this.getClass().getSimpleName()+": incident == "+incident);
//					System.out.println(this.getClass().getSimpleName()+": surface == "+surface);
//					//System.out.println(this.getClass().getSimpleName()+": normal == "+normal);
//					System.out.println(this.getClass().getSimpleName()+": incidentProjectedOntoSurface == "+incidentProjectedOntoSurface);
//					System.out.println(this.getClass().getSimpleName()+": reflect == "+reflect);
					angle = Vector2fUtils.findSignedAngleFromOrigin(reflect);
//					System.out.println(this.getClass().getSimpleName()+": angle == "+angle);


					x = closestOccluderIntersection.intersection.x;
					y = closestOccluderIntersection.intersection.y;
					speedX = FastMath.cos(angle) * speed;
					speedY = FastMath.sin(angle) * speed;
					oldX = x;
					oldY = y;
					previousIntersections.clear();
					previousIntersections.addAll(currentIntersections);
					currentIntersections.clear();

					// check that the bullet isn't intersecting in an infinite loop.
					impacts++;
					recentSecondsFromStartToImpact[recentSecondsFromStartToImpactIndex] = secondsFromStartToImpact;
					float sum = 0;
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
		public float distToIntersection;
		public Vector2f intersection;
		public OccluderImpl occluderImpl;
		public int occluderSideJ;
		public int occluderSideJPlus;
		public OccluderIntersection(float distToIntersection, Vector2f intersection, OccluderImpl occluderImpl, int occluderSideJ, int occluderSideJPlus){
			this.distToIntersection = distToIntersection;
			this.intersection = intersection;
			this.occluderImpl = occluderImpl;
			this.occluderSideJ = occluderSideJ;
			this.occluderSideJPlus = occluderSideJPlus;
		}
	}

	public void hitObstacle(OccluderImpl hitObstacle, float timeOfHit){
		//dead = true;
	}
	public void hit(Player hitPlayer, float timeOfHit){
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
	public float getX(){
		return x;
	}
	public float getY(){
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
//import straightedge.geom.Vector2f;
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
//	public float x;
//	public float y;
//	public float speedX;
//	public float speedY;
//	float accelX;
//	float accelY;
//	public float spawnTimeSeconds;
//	public boolean dead;
//	public Player playerThatWasHit = null;
//	public float backX;
//	public float backY;
//	public float oldBackX;
//	public float oldBackY;
//
//	public float radius;
//	public float length;
//	public float maxSpeed;
//	public float damage;
//	public float angle;
//	public float lifeTimeSeconds;
//
//	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
//
////	SightField sightField;
//
//	public Bullet(Gun gun, Player gunUser, float newX, float newY, float angle, float spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
//		world = gun.world;
//		this.ownerGunUser = gunUser;
//		this.spawnTimeSeconds = spawnTimeSeconds;
//		dead = false;
////		assert Point2D.distance(ownerGunUser.getX(), ownerGunUser.getY(), newX, newY) < ownerGunUser.getR() : Point2D.distance(ownerGunUser.getX(), ownerGunUser.getY(), newX, newY);
//		radius = 2f;
//		length = 2*radius;
//		float damagePerSecond = 200f;
//		damage = gun.getReloadSeconds()*damagePerSecond;//4f;
//		this.angle = angle;
//		/*gun.getWorld().getRandom().setSeed(gun.getSeed());
//		gun.setSeed(gun.getSeed()+3041);
//		float randomSpeedIncrement = world.getRandom().nextFloat()*200;*/
//		float startSpeed = 1000;//1500;//600;// + randomSpeedIncrement;
//		float randomRangeIncrement = 0;//(float)Math.random()*200;
//		float range = 1000;//1500;//180 + randomRangeIncrement;
//		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
//		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;
//		//float accel = -50;
//		//accelX = (float) Math.cos(angle) * accel;
//		//accelY = (float) Math.sin(angle) * accel;
//		float launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
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
//	public void doMove(float seconds, float startTime) {
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
//	protected void doBulletMove(float seconds, float timeAtStartOfMoveSeconds) {
////		codeTimer.setEnabled(false);
////		codeTimer.click("");
//		assert Double.isNaN(x) == false;
//		assert seconds >= 0 : seconds;
//		float newSpeedX = (float) (speedX + accelX * seconds);
//		float newSpeedY = (float) (speedY + accelY * seconds);
//		float xIncrement = (float) (((newSpeedX + speedX) / 2f) * seconds);
//		float yIncrement = (float) (((newSpeedY + speedY) / 2f) * seconds);
//		x += xIncrement;
//		y += yIncrement;
//		backX += xIncrement;
//		backY += yIncrement;
//
//		speedX = newSpeedX;
//		speedY = newSpeedY;
//		//codeTimer.click("");
//		boolean touch = false;
//		Vector2f pos = new Vector2f(x, y);
////		ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(pos, length);
//		float approxDistCoveredHalved = (Math.abs(xIncrement) + Math.abs(yIncrement) + length)/2;
//		float midPointX = (x + backX)/2f;
//		float midPointY = (y + backY)/2f;
//		ArrayList<OccluderImpl> obstacles = world.allOccluders.getAllWithin(midPointX, midPointY, approxDistCoveredHalved);
//		//codeTimer.click("");
//		OccluderImpl hitObstacle = null;
//		Vector2f closestIntersection = null;
//		float distToClosestHitObstacle = Double.MAX_VALUE;
//		//float distCovered = (float) Math.pow(Math.pow(xIncrement, 2) + Math.pow(yIncrement, 2), 0.5f);
//		float distCovered = Math.abs(xIncrement) + Math.abs(yIncrement);
//		for (int i = 0; i < obstacles.size(); i++) {
//			OccluderImpl obstacle = obstacles.get(i);
//			KPolygon shape = obstacle.getPolygon();
//			float error = 0.1f;
//			if (Vector2f.distance(x, y, shape.getCenter().x, shape.getCenter().y) > shape.getRadius() + length + distCovered + error) {
//				continue;
//			}
//			ArrayList<Vector2f> points = shape.getPoints();
//			for (int j = 0; j < points.size(); j++) {
//				int jPlus = (j+1 == points.size() ? 0 : j+1);
//				if (Vector2f.linesIntersect(oldBackX, oldBackY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y)){
//					Vector2f intersection = Vector2f.getLineLineIntersection(oldBackX, oldBackY, x, y, points.get(j).x, points.get(j).y, points.get(jPlus).x, points.get(jPlus).y);
//					if (intersection == null){
//						continue;
//					}
//					float dist = intersection.distance(oldBackX, oldBackY);
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
//		Vector2f midPoint = Vector2f.midPoint(oldBackX, oldBackY, x, y);
//		float halfDist = Vector2f.distance(oldBackX, oldBackY, x, y)/2f;
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
//		float distToClosestHitPlayer = Double.MAX_VALUE;
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
//				float dist = Vector2f.distance(oldBackX, oldBackY, x, y);
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
//	public void hitObstacle(OccluderImpl hitObstacle, float timeOfHit){
//		dead = true;
//	}
//	public void hit(Player hitPlayer, float timeOfHit){
//		//hitPlayer.takeDamage(this, timeOfHit);
//		this.playerThatWasHit = hitPlayer;
//		dead = true;
//		playerThatWasHit.health -= this.damage;
//		if (playerThatWasHit.health <= 0){
//			playerThatWasHit.die(timeOfHit);
//		}
//	}
//}
