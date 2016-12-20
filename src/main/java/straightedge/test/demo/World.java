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

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.vividsolutions.jts.geom.Geometry;

import straightedge.geom.AABB;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;
import straightedge.geom.path.KNode;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.util.TileBag;
import straightedge.geom.util.TileBagIntersections;
import straightedge.geom.vision.OccluderImpl;

/**
 *
 * @author Keith
 */
public class World {

	long systemNanosAtStart;
	long nanosElapsed;
	static float NANOS_IN_A_SECOND = 1000000000;
	boolean pause = false;

	public void setSystemNanosAtStart(long systemNanosAtStart){
		this.systemNanosAtStart = systemNanosAtStart;
	}
	public long getSystemNanosAtStart(){
		return systemNanosAtStart;
	}

	public void update(long nanos){
		float seconds = nanos/NANOS_IN_A_SECOND;
		float startTime = nanosElapsed/NANOS_IN_A_SECOND;
		assert seconds >= 0 : seconds;
		main.getEventHandler().eventCache.clearAndFillCache();
		ArrayList<AWTEventWrapper> events = main.getEventHandler().eventCache.getEventsList();
		Collections.sort(events);
		for (int i = 0; i < events.size(); i++){
			AWTEventWrapper ev = events.get(i);
			main.getEventHandler().processEvent(ev);
		}
		if (pause == false){
			doMove(seconds, startTime);
			nowAtTimeStop(startTime + seconds);
			nanosElapsed += nanos;
		}else{
			doMove(0, startTime);
			nowAtTimeStop(startTime + 0);
		}

	}


	public Random random;
	public ArrayList<KMultiPolygon> allMultiPolygons;
	public TileBagIntersections<OccluderImpl> allOccluders;

	public TileBag<PathBlockingObstacleImpl> allObstacles;
	public AABB originalScreenAABB;
	public AABB innerAABB;
	public AABB obstaclesAABB;
	public AABB enemySpawnAABB;
	public AABB playerSpawnAABB;

	public NodeConnector nodeConnector;
	public float maxConnectionDistance;

	public Player player;
	public ArrayList<Player> enemies;
	public ArrayList<Bullet> bullets;

	public boolean makeFromOuterPolygon = false;
	public float obstacleBufferAmount = 5;
	public int numPointsPerQuadrant = 1;

	public Main main;

	public World(Main main){
		this.main = main;
	}

	// this method should be over-ridden by sub-classes
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		float contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		float contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);
		ArrayList<KPolygon> polygons = new ArrayList<KPolygon>();
		
		// make stars
		for (int i = 0; i < 5; i++){
			ArrayList<Vector2f> pointList = new ArrayList<Vector2f>();
			int numPoints = 4 + random.nextInt(4)*2;
			float angleIncrement = FastMath.PI*2f/(numPoints*2);
			float rBig = 40 + random.nextFloat()*90;
			float rSmall = 20 + random.nextFloat()*70;
			float currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				float x = rBig*FastMath.cos(currentAngle);
				float y = rBig*FastMath.sin(currentAngle);
				pointList.add(new Vector2f((float)x, (float)y));
				currentAngle += angleIncrement;
				x = rSmall*FastMath.cos(currentAngle);
				y = rSmall*FastMath.sin(currentAngle);
				pointList.add(new Vector2f((float)x, (float)y));
				currentAngle += angleIncrement;
			}
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			//poly.translate(20 + (float)random.nextFloat()*aabb.getWidth(), 20 + (float)random.nextFloat()*aabb.getHeight());
			Vector2f p = new Vector2f(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			poly.translateTo(p);
			polygons.add(poly);
		}
		for (int i = 0; i < polygons.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(polygons.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}

	public void init(){
		Container cont = main.getParentFrameOrApplet();
		
		random = new Random();
		allMultiPolygons = new ArrayList<KMultiPolygon>();
		
		int insets = 20;
		float contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		float contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);
		originalScreenAABB = AABB.createFromDiagonal(0,0,contW,contH);
		innerAABB = AABB.createFromDiagonal(insets, insets, contW - 2*insets, contH - 2*insets);
		float spawnWH = 150;
		enemySpawnAABB = new AABB(insets, insets, spawnWH+insets, spawnWH+insets);
		playerSpawnAABB = new AABB(contW - (spawnWH+insets), contH - (spawnWH+insets), contW - insets, contH - insets);

		fillMultiPolygonsList();

		ArrayList<OccluderImpl> tempOccludersList = new ArrayList<OccluderImpl>();
		for (int i = 0; i < allMultiPolygons.size(); i++){
			KPolygon poly = allMultiPolygons.get(i).getExteriorPolygon().copy();
			OccluderImpl occluder = new OccluderImpl(poly);
			tempOccludersList.add(occluder);
		}
		allOccluders = new TileBagIntersections(tempOccludersList, 50);
		allOccluders.addAll(tempOccludersList);

		ArrayList<PathBlockingObstacleImpl> tempStationaryObstacles = new ArrayList<PathBlockingObstacleImpl>();
		PolygonConverter pc = new PolygonConverter();
		for (int i = 0; i < allMultiPolygons.size(); i++){
			KPolygon poly = allMultiPolygons.get(i).getExteriorPolygon().copy();
			com.vividsolutions.jts.geom.Polygon jtsPolygon = pc.makeJTSPolygonFrom(poly);
			Geometry bufferedJTSPolygon = jtsPolygon.buffer(obstacleBufferAmount, numPointsPerQuadrant);
			KPolygon bufferedPoly = pc.makeKMultiPolygonListFrom(bufferedJTSPolygon).get(0).getExteriorPolygon();
			PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(bufferedPoly);
			if (obst == null){
				continue;
			}
			tempStationaryObstacles.add(obst);
		}
//		// Here, we combine all allObstacles that are touching. Note that this leads
//		// to sub-optimal performance when you've got just one massive obstacle
//		// because then the obstacle-sorting optimisations in the path finding code don't work.
//		// This affects the WorldMaze in particular...
//		ArrayList<com.vividsolutions.jts.geom.Polygon> allJTSPolygons = new ArrayList<com.vividsolutions.jts.geom.Polygon>();
//		PolygonConverter pc = new PolygonConverter();
//		for (int i = 0; i < allMultiPolygons.size(); i++){
//			KPolygon poly = allMultiPolygons.get(i).getExteriorPolygon().copy();
//			com.vividsolutions.jts.geom.Polygon jtsPolygon = pc.makeJTSPolygonFrom(poly);
//			allJTSPolygons.add(jtsPolygon);
//		}
//		com.vividsolutions.jts.geom.Polygon[] jtsPolygonArray = allJTSPolygons.toArray(new com.vividsolutions.jts.geom.Polygon[allJTSPolygons.size()]);
//		com.vividsolutions.jts.geom.MultiPolygon jtsMultiPolygon = new com.vividsolutions.jts.geom.MultiPolygon(jtsPolygonArray, new com.vividsolutions.jts.geom.GeometryFactory());
//		com.vividsolutions.jts.geom.Geometry bufferedGeometry = jtsMultiPolygon.buffer(obstacleBufferAmount, numPointsPerQuadrant);
//		ArrayList<KMultiPolygon> multiPolygons = pc.makeKMultiPolygonListFrom(bufferedGeometry);
//		ArrayList<KPolygon> bufferedPolygons = new ArrayList<KPolygon>();
//		for (int i = 0; i < multiPolygons.size(); i++){
//			KMultiPolygon mpoly = multiPolygons.get(i);
//			bufferedPolygons.add(mpoly.getExteriorPolygon());
//		}
//		ArrayList<PathBlockingObstacleImpl> tempStationaryObstacles = new ArrayList<PathBlockingObstacleImpl>();
//		for (int i = 0; i < bufferedPolygons.size(); i++){
//			KPolygon poly = bufferedPolygons.get(i);
//			PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly);
//			if (obst == null){
//				continue;
//			}
//			tempStationaryObstacles.add(obst);
//		}

		maxConnectionDistance = 700f;
		nodeConnector = new NodeConnector();
		obstaclesAABB = AABB.getAABBEnclosingCenterAndRadius(tempStationaryObstacles.toArray());
		allObstacles = new TileBag(obstaclesAABB, 50);

		for (int i = 0; i < tempStationaryObstacles.size(); i++){
			PathBlockingObstacleImpl obst = tempStationaryObstacles.get(i);
			allObstacles.add(obst);
			nodeConnector.addObstacle(obst, allObstacles, maxConnectionDistance);
		}
		
		Vector2f spawnPoint = getNearestPointOutsideOfObstacles(makeRandomPointWithin(playerSpawnAABB));
		player = new Player(this, spawnPoint);
		player.cache.originalBoundaryPolygon.scale(3, player.cache.originalEye);
		player.cache.reset();
		player.makeImage();
		player.makeImage2();

		enemies = new ArrayList<Player>();
		changeNumEnemies(3);
		bullets = new ArrayList<Bullet>();
		
		
	}

	public void changeNumEnemies(int numEnemies){
		if (enemies.size() == numEnemies){
			return;
		}
		while (enemies.size() > numEnemies){
			enemies.remove(enemies.size()-1);
		}
		while (enemies.size() < numEnemies){
			Vector2f spawnPoint = getNearestPointOutsideOfObstacles(makeRandomPointWithin(enemySpawnAABB));
			Player enemy = new Player(this, spawnPoint);
			enemy.cache.originalBoundaryPolygon.scale(0.75 + random.nextFloat()*0.5, 0.75 + random.nextFloat()*0.5, enemy.cache.originalEye);
			enemy.cache.reset();
			enemy.makeImage();
			enemy.gun.rotationSpeed *= 0.5;
			enemies.add(enemy);
		}
	}

	public void doMove(float seconds, float startTime){
		Vector2f botLeft = this.obstaclesAABB.getBotLeft();
		Vector2f topRight = this.obstaclesAABB.getTopRight();
		float worldEdgeDistance = 1000;
		AABB worldBounds = AABB.createFromDiagonal(botLeft.x - worldEdgeDistance,
											botLeft.y - worldEdgeDistance,
											topRight.x + worldEdgeDistance,
											topRight.y + worldEdgeDistance);
		for (int i = 0; i < bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			bullet.doMove(seconds, startTime);
			// if the bullet is outside of the world's bounds, remove it.
			if (worldBounds.contains(bullet.x, bullet.y) == false){
				bullets.remove(i);
				i--;
			}
		}
		
		for (int i = 0; i < enemies.size(); i++){
			Player enemy = enemies.get(i);
			enemy.doMove(seconds, startTime);
		}
		player.doMove(seconds, startTime);

		// Note that dead bullets need to be eliminated after player.doMove method
		// since when a bullet is fired by the player, bullet.doMove is called.
		for (int j = 0; j < bullets.size(); j++) {
			if (bullets.get(j).dead == true) {
				bullets.remove(j);
				j--;
			}
		}

		//countStats();
	}

	public int numObstacles;
	public int numNodes;
	public int numPermanentNodeConnections;
	public int numTemporaryNodeConnections;
	public void countStats(){
		// Counts number of obstacles, nodes, connections
		numObstacles = 0;
		numNodes = 0;
		numPermanentNodeConnections = 0;
		numTemporaryNodeConnections = 0;
		numObstacles = allObstacles.size();
		for (int i = 0; i < allObstacles.size(); i++) {
			PathBlockingObstacleImpl obst = allObstacles.get(i);
			numNodes += obst.getNodes().size();
			for (int j = 0; j < obst.getNodes().size(); j++) {
				KNode node = obst.getNodes().get(j);
				numPermanentNodeConnections += node.getConnectedNodes().size();
				numTemporaryNodeConnections += node.getTempConnectedNodes().size();
			}
		}
		// divide numPermanentNodeConnections by 2 since connections are float-counted.
		numPermanentNodeConnections /= 2;
		// numTemporaryNodeConnections are not float-counted since the pathFinder's start and end nodes were not included in the count.
		System.out.println(this.getClass().getSimpleName()+": numObstacles == "+numObstacles);
		System.out.println(this.getClass().getSimpleName()+": numNodes == "+numNodes);
		System.out.println(this.getClass().getSimpleName()+": numTemporaryNodeConnections == "+numTemporaryNodeConnections);
		System.out.println(this.getClass().getSimpleName()+": numPermanentNodeConnections == "+numPermanentNodeConnections);
	}

	protected void nowAtTimeStop(float timeNow) {
		player.nowAtTimeStop(timeNow);
		for (int i = 0; i < enemies.size(); i++){
			Player enemy = enemies.get(i);
			enemy.nowAtTimeStop(timeNow);
		}
	}

	public Vector2f makeRandomPointWithin(AABB aabb){
		return new Vector2f(aabb.getX() + random.nextFloat()*aabb.w(), aabb.getY() + random.nextFloat()*aabb.h());
	}
	public Vector2f getNearestPointOutsideOfObstacles(Vector2f point){
		// check that the target point isn't inside any allObstacles.
		// if so, move it.
		Vector2f movedPoint = point.clone();
		boolean targetIsInsideObstacle = false;
		int count = 0;
		while (true){
			for (PathBlockingObstacleImpl obst : allObstacles.getBag()){
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
}
