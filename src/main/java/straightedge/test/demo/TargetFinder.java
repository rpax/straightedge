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

import com.jme3.math.Vector2f;

import straightedge.geom.KPolygon;
import straightedge.geom.Vector2fUtils;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

/**
 *
 * @author Keith
 */
public class TargetFinder{
	public TargetUser targetUser;
	public World world;
	PathFinder pathFinder;
	public PathData pathData;

	public static int TARGET_FIXED = 0;
	public static int TARGET_RELATIVE = 1;
	public static int TARGET_PLAYER = 2;
	public int targetType;

	public float minRange = 50f;//getPlayer().getMaxAttackDist()*3/4f;
	public float maxRange = 60f;

	public Vector2f target = new Vector2f();
	public TargetUser targetPlayerToFollow = null;

	public TargetFinder(TargetUser targetUser, World world){
		this.targetUser = targetUser;
		this.world = world;
		pathFinder = new PathFinder();
		pathData = new PathData();
		respawn();
	}

	public void respawn(){
		setFixedTarget(targetUser.getPos(), false);
		pathData.reset();
	}

	public Vector2f getAbsoluteTarget(){
		Vector2f absoluteTarget = new Vector2f();
		if (targetType == TARGET_FIXED){
			absoluteTarget.x = target.x;
			absoluteTarget.y = target.y;
		} else if (targetType == TARGET_RELATIVE){
			absoluteTarget.x = target.x + targetUser.getPos().x;
			absoluteTarget.y = target.y + targetUser.getPos().y;
			Vector2f movedAbsoluteTarget = getNearestPointOutsideOfObstacles(absoluteTarget);
			absoluteTarget.set(movedAbsoluteTarget);
		} else if (targetType == TARGET_PLAYER){
			float distToTarget = targetUser.getPos().distance(targetPlayerToFollow.getPos());
//			float minRange = 50;//getPlayer().getMaxAttackDist()*3/4f;
//			float maxRange = minRange/8f;//(getPlayer().getMaxAttackDist() - minRange)/2f;
			if (isWithinRangeOfTargetPlayer(distToTarget)){
				return new Vector2f(targetUser.getPos());
			}
			Vector2f p = Vector2fUtils.createPointToward(targetPlayerToFollow.getPos(),targetUser.getPos(), minRange);
			// Check that the point is not inside any allObstacles, or else the pathFinder won't work.
			for (PathBlockingObstacleImpl obst : getWorld().allObstacles){
				if (obst.getOuterPolygon().contains(p)){
					p = targetPlayerToFollow.getPos().clone();
					//System.out.println(this.getClass().getSimpleName() + ": getPlayer().getName() == "+getPlayer().getName()+", ");
					break;
				}
			}
			return p;
		}
		return absoluteTarget;
	}
	protected boolean isWithinRangeOfTargetPlayer(float distToTarget){
		if (distToTarget < maxRange){
			return true;
		}
		return false;
//		//float smallAmount = 0.01f;
//		if (targetUser.isMoving() == true){
//			//if (distToTarget < minRange + smallAmount){
//			if (distToTarget < (minRange + maxRange)/2f){
//				return true;
//			}
//		}else{
//			if (distToTarget < maxRange){
//				return true;
//			}
//		}
//		return false;
	}
	public boolean isTargetPlayer(){
		if (targetType == TARGET_PLAYER){
			return true;
		}
		return false;
	}
	public boolean isTargetFixed(){
		if (targetType == TARGET_FIXED){
			return true;
		}
		return false;
	}
	public boolean isTargetRelative(){
		if (targetType == TARGET_RELATIVE){
			return true;
		}
		return false;
	}

	public void setFixedTarget(float targetX, float targetY, boolean calcPathNow) {
		targetType = TARGET_FIXED;
		target.x = targetX;
		target.y = targetY;
		Vector2f movedTarget = getNearestPointOutsideOfObstacles(target);
		target.set(movedTarget);
		if (calcPathNow){
			calcPath();
		}
	}
	public void setFixedTarget(Vector2f p, boolean calcPathNow) {
		setFixedTarget(p.x, p.y, calcPathNow);
	}
	public void setRelativeTarget(double targetX, double targetY, boolean calcPathNow) {
		setRelativeTarget((float) targetX, (float) targetY, calcPathNow);
	}
	public void setRelativeTarget(float targetX, float targetY, boolean calcPathNow) {
		targetType = TARGET_RELATIVE;
		target.x = targetX;
		target.y = targetY;
		if (calcPathNow){
			calcPath();
		}
	}
	public void setRelativeTarget(Vector2f p, boolean calcPathNow) {
		setRelativeTarget(p.x, p.y, calcPathNow);
	}

	public void setTargetPlayer(TargetUser targetPlayerToFollow, boolean calcPathNow) {
		assert targetPlayerToFollow != null : targetPlayerToFollow;
		targetType = TARGET_PLAYER;
		this.targetPlayerToFollow = targetPlayerToFollow;
		if (calcPathNow){
			calcPath();
		}
	}
	public Vector2f getNearestPointOutsideOfObstacles(Vector2f point){
		// check that the target point isn't inside any allObstacles.
		// if so, move it.
		Vector2f movedPoint = point.clone();
		boolean targetIsInsideObstacle = false;
		int count = 0;
		while (true){
			for (PathBlockingObstacleImpl obst : getWorld().allObstacles){
				if (obst.getOuterPolygon().contains(movedPoint)){
					targetIsInsideObstacle = true;
					KPolygon poly = obst.getOuterPolygon();
					Vector2f p = poly.getBoundaryPointClosestTo(movedPoint);
					if (p != null){
						movedPoint.x = p.x;
						movedPoint.y = p.y;
					}
					assert point != null;
				}
			}
			count++;
			if (targetIsInsideObstacle == false || count >= 3){
				break;
			}
		}
		return movedPoint;
	}

	transient long time = -1;
	transient int count = 0;
	transient long oneSecond = 1000000000;
	public void calcPath(){
		if (time == -1){
			time = System.nanoTime();
		}
		count++;
		long timeNow = System.nanoTime();
		if (timeNow - time >= oneSecond){
			float calcsPerSecond = count/((timeNow - time)/oneSecond);
			//System.out.println(this.getClass().getSimpleName()+": calcsPerSecond == "+calcsPerSecond);
			count = 0;
			time = timeNow;
		}
		pathData = getPathFinder().calc(targetUser.getPos(), getAbsoluteTarget(), getWorld().maxConnectionDistance, this.getWorld().nodeConnector, world.allObstacles.getTileArray());
	}
	public TargetUser getTargetPlayerToFollow() {
		if (targetType == TARGET_PLAYER){
			return targetPlayerToFollow;
		}
		return null;
	}

	public boolean isAtTarget(){
		if (targetType == TARGET_RELATIVE){
			return false;
		}else if (targetType == TARGET_FIXED){
			if (this.getTargetUser().getPos().equals(target)){
				return true;
			}
			return false;
		}else if (targetType == TARGET_PLAYER){
			float dist = this.getTargetUser().getPos().distance(targetPlayerToFollow.getPos());
			if (isWithinRangeOfTargetPlayer(dist)){
				return true;
			}
			return false;
		}else{
			throw new RuntimeException("Unknown targetType == "+targetType);
		}
	}

	public World getWorld(){
		return world;
	}

	public PathFinder getPathFinder() {
		return pathFinder;
	}

	public TargetUser getTargetUser() {
		return targetUser;
	}

	public PathData getPathData() {
		return pathData;
	}

	public float getMinRange() {
		return minRange;
	}

	public float getMaxRange() {
		return maxRange;
	}

	public void setMinRange(float minRange) {
		this.minRange = minRange;
	}

	public void setMaxRange(float maxRange) {
		this.maxRange = maxRange;
	}
}
