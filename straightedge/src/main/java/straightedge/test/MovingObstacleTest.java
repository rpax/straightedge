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
package straightedge.test;

import straightedge.geom.*;
import straightedge.geom.vision.*;
import straightedge.geom.path.KNodeOfObstacle;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;
import straightedge.geom.path.PathFinder;
import straightedge.geom.PolygonBufferer;
import straightedge.geom.util.Bag;
import straightedge.geom.util.TileArray;
import straightedge.geom.util.TileBag;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import straightedge.geom.path.PathData;

/**
 *
 * @author Keith
 */
public class MovingObstacleTest {
	JFrame frame;
	MovingObstacleTest.ViewPane view;
	volatile boolean keepRunning = true;
	boolean pause = false;
	FPSCounter fpsCounter;

	Object mutex = new Object();
	ArrayList<AWTEvent> events = new ArrayList<AWTEvent>();
	ArrayList<AWTEvent> eventsCopy = new ArrayList<AWTEvent>();
	KPoint lastMouseMovePoint = new KPoint();

	ArrayList<KPolygon> originalPulsatingOccluderPolygons;
	ArrayList<OccluderImpl> pulsatingOccluders;
	ArrayList<OccluderImpl> movingOccluders;
	ArrayList<OccluderImpl> stationaryOccluders;
	ArrayList<OccluderImpl> allOccluders;

	ArrayList<KPolygon> originalPulsatingObstacleOuterPolygons;
	ArrayList<KPolygon> originalPulsatingObstacleInnerPolygons;
	ArrayList<PathBlockingObstacleImpl> pulsatingObstacles;
	ArrayList<PathBlockingObstacleImpl> movingObstacles;
	ArrayList<PathBlockingObstacleImpl> stationaryObstacles;
	TileBag<PathBlockingObstacleImpl> tileBag;

	PathFinder pathFinder;
	NodeConnector nodeConnector;
	float maxConnectionDistance;

	Player player;
	boolean makeFromOuterPolygon = false;
	double bufferAmount = 5;
	int numPointsPerQuadrant = 1;

	public MovingObstacleTest(){
		frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		view = new MovingObstacleTest.ViewPane();
		frame.add(view);

		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				keepRunning = false;
				System.exit(0);
			}
		});
		frame.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
		});
		frame.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){ synchronized (mutex){ events.add(e); } }
			public void keyReleased(KeyEvent e){ synchronized (mutex){ events.add(e); } }
			public void keyTyped(KeyEvent e){ synchronized (mutex){ events.add(e); } }
		});
		view.addMouseListener(new MouseListener(){
			public void mousePressed(MouseEvent e){ synchronized (mutex){ events.add(e); } }
			public void mouseReleased(MouseEvent e){ synchronized (mutex){ events.add(e); } }
			public void mouseClicked(MouseEvent e){ synchronized (mutex){ events.add(e); } }
			public void mouseEntered(MouseEvent e){ synchronized (mutex){ events.add(e); } }
			public void mouseExited(MouseEvent e){ synchronized (mutex){ events.add(e); } }
		});
		view.addMouseMotionListener(new MouseMotionListener(){
			public void mouseMoved(MouseEvent e){ synchronized (mutex){ events.add(e); } }
			public void mouseDragged(MouseEvent e){ synchronized (mutex){ events.add(e); } }
		});

		init();

		fpsCounter = new FPSCounter();

		frame.setVisible(true);

		Thread gameLoopThread = new Thread("GameLoop"){
			public void run(){
				long lastUpdateNanos = System.nanoTime();
				while(keepRunning){
					long currentNanos = System.nanoTime();
					float seconds = (currentNanos - lastUpdateNanos)/1000000000f;
					processEvents();
					if (pause != true){
						update(seconds);
					}
					fpsCounter.update();
					view.render();
					Thread.yield();
					//try{ Thread.sleep(1); }catch(Exception e){}
					lastUpdateNanos = currentNanos;
				}
			}
		};
		gameLoopThread.setDaemon(true);
		gameLoopThread.start();
	}
	
	public void init(){
		Random rand = new Random(0);
		stationaryOccluders = new ArrayList<OccluderImpl>();
		movingOccluders = new ArrayList<OccluderImpl>();
		pulsatingOccluders = new ArrayList<OccluderImpl>();
		originalPulsatingOccluderPolygons = new ArrayList<KPolygon>();
		allOccluders = new ArrayList<OccluderImpl>();
		// make some rectangles
		for (int i = 0; i < 4; i++){
			KPoint p = new KPoint((float)rand.nextFloat()*frame.getWidth(), (float)rand.nextFloat()*frame.getHeight());
			KPoint p2 = new KPoint((float)rand.nextFloat()*frame.getWidth(), (float)rand.nextFloat()*frame.getHeight());
			float width = 10 + 30*rand.nextFloat();
			KPolygon rect = KPolygon.createRectOblique(p, p2, width);
			allOccluders.add(new OccluderImpl(rect));
		}
		// make a cross
		allOccluders.add(new OccluderImpl(KPolygon.createRectOblique(40, 70, 100, 70, 20)));
		allOccluders.add(new OccluderImpl(KPolygon.createRectOblique(70, 40, 70, 100, 20)));
		// make a star
		for (int i = 0; i < 4; i++){
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			int numPoints = 4 + rand.nextInt(4)*2;
			double angleIncrement = Math.PI*2f/(numPoints*2);
			float rBig = 40 + rand.nextFloat()*90;
			float rSmall = 20 + rand.nextFloat()*70;
			double currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				double x = rBig*Math.cos(currentAngle);
				double y = rBig*Math.sin(currentAngle);
				pointList.add(new KPoint((float)x, (float)y));
				currentAngle += angleIncrement;
				x = rSmall*Math.cos(currentAngle);
				y = rSmall*Math.sin(currentAngle);
				pointList.add(new KPoint((float)x, (float)y));
				currentAngle += angleIncrement;
			}
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			poly.translate(20 + (float)rand.nextFloat()*frame.getWidth(), 20 + (float)rand.nextFloat()*frame.getHeight());
			allOccluders.add(new OccluderImpl(poly));
		}
		for (int i = 0; i < allOccluders.size(); i++){
			int n = 3;
			OccluderImpl occluder = allOccluders.get(i);
			if (rand.nextFloat() < 1f/n){
				movingOccluders.add(occluder);
			}else if (rand.nextFloat() < 2f/n){
				stationaryOccluders.add(occluder);
			}else if (rand.nextFloat() < 3f/n){
				pulsatingOccluders.add(occluder);
				originalPulsatingOccluderPolygons.add(occluder.getPolygon().copy());
			}
		}

		stationaryObstacles = new ArrayList<PathBlockingObstacleImpl>();
		for (int i = 0; i < stationaryOccluders.size(); i++){
			OccluderImpl occluder = stationaryOccluders.get(i);
			KPolygon poly = occluder.getPolygon();
			KPolygon bigPoly = (new PolygonBufferer()).buffer(poly, bufferAmount, numPointsPerQuadrant);
			PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(bigPoly);
			//PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly.copy());
			if (obst == null){
				continue;
			}
			stationaryObstacles.add(obst);
		}

		maxConnectionDistance = 1000f;
		pathFinder = new PathFinder();
		nodeConnector = new NodeConnector();
		TileArray tileArray = new TileArray(stationaryObstacles, 50);
		tileBag = new TileBag(tileArray, new Bag());

		for (int i = 0; i < stationaryObstacles.size(); i++){
			PathBlockingObstacleImpl obst = stationaryObstacles.get(i);
			tileBag.add(obst);
			nodeConnector.addObstacle(obst, tileBag.getTileArray(), maxConnectionDistance);
		}
		obstacleReachableNodesCaches = new ArrayList<ObstacleReachableNodesCache>();
		for (int i = 0; i < stationaryObstacles.size(); i++){
			PathBlockingObstacleImpl obst = stationaryObstacles.get(i);
			obstacleReachableNodesCaches.add(new ObstacleReachableNodesCache(obst));
		}

		movingObstacles = new ArrayList<PathBlockingObstacleImpl>();
		for (int i = 0; i < movingOccluders.size(); i++){
			OccluderImpl occluder = movingOccluders.get(i);
			KPolygon poly = occluder.getPolygon();
			KPolygon bigPoly = (new PolygonBufferer()).buffer(poly, bufferAmount, numPointsPerQuadrant);
			PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(bigPoly);
			//PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly.copy());
			if (obst == null){
				continue;
			}
			movingObstacles.add(obst);
		}
		for (int i = 0; i < movingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = movingObstacles.get(i);
			tileBag.add(obst);
			nodeConnector.addObstacle(obst, tileBag.getTileArray(), maxConnectionDistance);
		}


		pulsatingObstacles = new ArrayList<PathBlockingObstacleImpl>();
		originalPulsatingObstacleOuterPolygons = new ArrayList<KPolygon>();
		originalPulsatingObstacleInnerPolygons = new ArrayList<KPolygon>();
		for (int i = 0; i < pulsatingOccluders.size(); i++){
			OccluderImpl occluder = pulsatingOccluders.get(i);
			KPolygon poly = occluder.getPolygon();
			KPolygon bigPoly = (new PolygonBufferer()).buffer(poly, bufferAmount, numPointsPerQuadrant);
			PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(bigPoly);
			//PathBlockingObstacleImpl obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly.copy());
			if (obst == null){
				continue;
			}
			pulsatingObstacles.add(obst);
			originalPulsatingObstacleOuterPolygons.add(obst.getOuterPolygon().copy());
			originalPulsatingObstacleInnerPolygons.add(obst.getInnerPolygon().copy());
		}
		for (int i = 0; i < pulsatingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = pulsatingObstacles.get(i);
			tileBag.add(obst);
			nodeConnector.addObstacle(obst, tileBag.getTileArray(), maxConnectionDistance);
		}

		player = new Player();
		player.pos = new KPoint(10, 100);
		player.target = player.pos.copy();
	}

	ArrayList<ObstacleReachableNodesCache> obstacleReachableNodesCaches;
	public class ObstacleReachableNodesCache{
		PathBlockingObstacleImpl obst;
		ArrayList<Integer> nodesContainedState;
		ArrayList<ArrayList<KNodeOfObstacle>> copyOfEachNodesReachableNodes;

		public ObstacleReachableNodesCache(PathBlockingObstacleImpl obst){
			this.obst = obst;
			nodesContainedState = new ArrayList<Integer>();
			copyOfEachNodesReachableNodes = new ArrayList<ArrayList<KNodeOfObstacle>>();
			for (int i = 0; i < obst.getNodes().size(); i++){
				KNodeOfObstacle node = obst.getNodes().get(i);
				nodesContainedState.add(node.getContained());
				ArrayList<KNodeOfObstacle> currentNodesReachableNodes = new ArrayList<KNodeOfObstacle>();
				for (int j = 0; j < node.getConnectedNodes().size(); j++){
					KNodeOfObstacle reachableNode = (KNodeOfObstacle)node.getConnectedNodes().get(j);
					currentNodesReachableNodes.add(reachableNode);
				}
				copyOfEachNodesReachableNodes.add(currentNodesReachableNodes);
			}
		}

		public void clearAndRefillObstReachableNodes(){
			for (int i = 0; i < obst.getNodes().size(); i++){
				KNodeOfObstacle node = obst.getNodes().get(i);
				node.getConnectedNodes().clear();
				ArrayList<KNodeOfObstacle> oldReachableNodes = copyOfEachNodesReachableNodes.get(i);
				for (int j = 0; j < oldReachableNodes.size(); j++){
					KNodeOfObstacle oldReachableNode = oldReachableNodes.get(j);
					node.getConnectedNodes().add(oldReachableNode);
				}
				node.setContained(nodesContainedState.get(i));
			}
		}
	}

	public void processEvents(){
		synchronized(mutex){
			if (events.size() > 0){
				eventsCopy.addAll(events);
				events.clear();
			}
		}
		if (eventsCopy.size() > 0){
			for (int i = 0; i < eventsCopy.size(); i++){
				AWTEvent awtEvent = eventsCopy.get(i);
				if (awtEvent instanceof MouseEvent){
					MouseEvent e = (MouseEvent)awtEvent;
					if (e.getID() == MouseEvent.MOUSE_MOVED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
					}else if (e.getID() == MouseEvent.MOUSE_PRESSED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
						player.target.x = lastMouseMovePoint.x;
						player.target.y = lastMouseMovePoint.y;
					}else if (e.getID() == MouseEvent.MOUSE_DRAGGED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
						player.target.x = lastMouseMovePoint.x;
						player.target.y = lastMouseMovePoint.y;
					}
				}else if (awtEvent instanceof java.awt.event.KeyEvent){
					KeyEvent e = (KeyEvent)awtEvent;
					if (e.getID() == KeyEvent.KEY_PRESSED){
						if (e.getKeyCode() == KeyEvent.VK_R){
							this.init();
						}
						if (e.getKeyCode() == KeyEvent.VK_W){
							if (view.isDrawWireFrame() == true){
								view.setDrawWireFrame(false);
							}else{
								view.setDrawWireFrame(true);
							}
						}
						if (e.getKeyCode() == KeyEvent.VK_P){
							if (pause == true){
								pause = false;
							}else{
								pause = true;
							}
						}
					}
				}else if (awtEvent instanceof ComponentEvent){
					ComponentEvent e = (ComponentEvent)awtEvent;
					if (e.getID() == ComponentEvent.COMPONENT_RESIZED){
						this.init();
					}
				}
			}
			eventsCopy.clear();
		}
	}

	double totalSeconds = 0;
	public void update(float seconds){
		// rotate the moving occluders
		float rotateSpeed = (float)(Math.PI*2f/16f);
		for (int i = 0; i < movingOccluders.size(); i++){
			OccluderImpl occluder = movingOccluders.get(i);
			occluder.getPolygon().rotate(rotateSpeed*seconds);
		}

		for (int i = 0; i < movingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = movingObstacles.get(i);
			tileBag.remove(obst);
			assert tileBag.getBag().contains(obst) == false;
			assert tileBag.getTileArray().getAllWithin(tileBag.getTileArray().getBotLeft(), tileBag.getTileArray().getBotLeft().distance(tileBag.getTileArray().getTopRight())).contains(obst) == false;
		}
		for (int i = 0; i < obstacleReachableNodesCaches.size(); i++){
			ObstacleReachableNodesCache obstReachableNodesCache = obstacleReachableNodesCaches.get(i);
			obstReachableNodesCache.clearAndRefillObstReachableNodes();
		}

		for (int i = 0; i < movingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = movingObstacles.get(i);
			obst.getInnerPolygon().rotate(rotateSpeed*seconds);
			obst.getOuterPolygon().rotate(rotateSpeed*seconds);
			obst.resetNodes();
			// no need to reset obstacle's copyOfNodes since this is done in NodeConnector.addObstacle().
		}

//		double scaleSpeed = Math.sin(totalSeconds*(2*Math.PI))*0.5f;
//		double currentScaling = 1 + scaleSpeed*seconds;
		double scaleAmount = 1 + 0.5*Math.sin((totalSeconds + seconds)*(2*Math.PI)*0.25);
		for (int i = 0; i < pulsatingOccluders.size(); i++){
			OccluderImpl occluder = pulsatingOccluders.get(i);
//			occluder.getPolygon().scale(currentScaling);
			KPolygon polygonCopy = originalPulsatingOccluderPolygons.get(i).copy();
			polygonCopy.scale(scaleAmount);
			occluder.setPolygon(polygonCopy);
		}
		for (int i = 0; i < pulsatingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = pulsatingObstacles.get(i);
			tileBag.remove(obst);
			assert tileBag.getBag().contains(obst) == false;
			assert tileBag.getTileArray().getAllWithin(tileBag.getTileArray().getBotLeft(), tileBag.getTileArray().getBotLeft().distance(tileBag.getTileArray().getTopRight())).contains(obst) == false;
		}
		for (int i = 0; i < pulsatingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = pulsatingObstacles.get(i);
//			obst.getInnerPolygon().scale(currentScaling);
//			obst.getOuterPolygon().scale(currentScaling);
			KPolygon outerPolygonCopy = originalPulsatingObstacleOuterPolygons.get(i).copy();
			outerPolygonCopy.scale(scaleAmount);
			obst.setOuterPolygon(outerPolygonCopy);
			KPolygon innerPolygonCopy = originalPulsatingObstacleInnerPolygons.get(i).copy();
			innerPolygonCopy.scale(scaleAmount);
			obst.setInnerPolygon(innerPolygonCopy);
			// need to reset obst's node points since the inner and outerPolygons have changed.
			obst.resetNodes();
		}
		// connect the moving obstacles to all other obstacles
		for (int i = 0; i < movingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = movingObstacles.get(i);
			tileBag.add(obst);
			nodeConnector.addObstacle(obst, tileBag.getTileArray(), maxConnectionDistance);
		}
		for (int i = 0; i < pulsatingObstacles.size(); i++){
			PathBlockingObstacleImpl obst = pulsatingObstacles.get(i);
			tileBag.add(obst);
			nodeConnector.addObstacle(obst, tileBag.getTileArray(), maxConnectionDistance);
		}

//		int totalNodes = 0;
//		for (int i = 0; i < movingObstacles.size(); i++){
//			PathBlockingObstacleImpl obst = movingObstacles.get(i);
//			totalNodes += obst.getNodes().size();
//		}
//		for (int i = 0; i < stationaryObstacles.size(); i++){
//			PathBlockingObstacleImpl obst = stationaryObstacles.get(i);
//			totalNodes += obst.getNodes().size();
//		}
//		System.out.println(this.getClass().getSimpleName()+": totalNodes == "+totalNodes);

		player.update(seconds);
		totalSeconds += seconds;
	}

	public class Player{
		KPoint pos;
		KPoint target;
		KPoint targetAdjusted;
		float maxConnectionDistance;
		PathData pathData;
		float speed;
		float speedX;
		float speedY;
		float moveAngle;
		KPoint currentTargetPoint = null;

		VisionFinder visionFinder;
		VisionData cache;
		double smallAmount = 0.0001f;

		public Player(){
			maxConnectionDistance = 1000f;
			speed = 100;

			{
				int numPoints = 20;
				float radius = 200;
				KPolygon boundaryPolygon = KPolygon.createRegularPolygon(numPoints, radius);
				// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
				KPoint eye = new KPoint(smallAmount, smallAmount);
				visionFinder = new VisionFinder();
				cache = new VisionData(eye, boundaryPolygon);
			}
		}
//		public void update(double seconds){
//			pathFinder.calcPath(pos, target, maxConnectionDistance, nodeConnector, VampireGame.this.stationaryObstaclesTileArray, pathPoints);
//		}
		double radarSwipeProportion = 0;
		double radarSwipeProportionSpeed = 0.2;
		public void update(double seconds){
			pos = getNearestPointOutsideOfObstacles(pos);
			targetAdjusted = getNearestPointOutsideOfObstacles(target);
			pathData = pathFinder.calc(pos, targetAdjusted, maxConnectionDistance, nodeConnector, tileBag.getTileArray());

			if (speed == 0){
				return;
			}
			// update the player's position as it travels from point to point along the path.
			double secondsLeft = seconds;
			for (int i = 0; i < pathData.points.size(); i++){
				currentTargetPoint = pathData.points.get(i);
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
					pathData.points.remove(i);
					i--;
				}else{
					//s = t(u + v)/2
					pos.x = (float)(oldPos.x + secondsLeft * speedX);
					pos.y = (float)(oldPos.y + secondsLeft * speedY);
					secondsLeft = 0;
					break;
				}
			}

			// Move the eye and boundaryPolygon to wherever they need to be.
			// By making the eye slightly offset from its integer coordinate by smallAmount,
			// it will prevent problems caused by collinearity.
			cache.eye.setCoords(pos.x + smallAmount, pos.y + smallAmount);
			cache.boundaryPolygon.translateTo(cache.eye);
			visionFinder.calc(cache, allOccluders);
			/* Note that the above is a slow way to process shadows - every occluder is
			 * intersected with every other occluder, which is not necessary if some of the
			 * occluders are stationary.
			 */

			radarSwipeProportion += radarSwipeProportionSpeed*seconds;
			while (radarSwipeProportion > 1){
				radarSwipeProportion -= 1;
			}
		}

		public KPoint getNearestPointOutsideOfObstacles(KPoint point){
			// check that the target point isn't inside any obstacles.
			// if so, move it.
			KPoint movedPoint = point.copy();
			boolean targetIsInsideObstacle = false;
			int count = 0;
			while (true){
				for (PathBlockingObstacleImpl obst : tileBag.getBag()){
					if (obst.getOuterPolygon().contains(movedPoint)){
						targetIsInsideObstacle = true;
						KPolygon poly = obst.getOuterPolygon();
						KPoint p = poly.getBoundaryPointClosestTo(movedPoint);
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

		public float getMaxConnectionDistance() {
			return maxConnectionDistance;
		}

		public PathData getPathData() {
			return pathData;
		}

		public KPoint getPos() {
			return pos;
		}

		public KPoint getTarget() {
			return target;
		}


	}

	public class ViewPane extends JComponent {
		VolatileImage backImage;
		Graphics2D backImageGraphics2D;
		boolean drawWireFrame = true;
		public ViewPane() {
		}

		protected void renderWorld() {
			Graphics2D g = backImageGraphics2D;

			if (drawWireFrame == true){
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, getWidth(), getHeight());

				g.setColor(Color.GRAY);
				for (int i = 0; i < allOccluders.size(); i++) {
					g.fill(allOccluders.get(i).getPolygon());
				}

				if (player.cache.visiblePolygon != null){
					g.setColor(Color.WHITE);
					g.fill(player.cache.visiblePolygon);
					g.setColor(Color.BLACK);
					g.draw(player.cache.visiblePolygon);
				}

				g.setColor(Color.BLUE);
				for (int i = 0; i < movingOccluders.size(); i++) {
					g.draw(movingOccluders.get(i).getPolygon());
					assert stationaryOccluders.contains(movingOccluders.get(i).getPolygon()) == false;
				}
				for (int i = 0; i < pulsatingOccluders.size(); i++) {
					g.draw(pulsatingOccluders.get(i).getPolygon());
					assert stationaryOccluders.contains(pulsatingOccluders.get(i).getPolygon()) == false;
				}
				for (int i = 0; i < stationaryOccluders.size(); i++) {
					g.draw(stationaryOccluders.get(i).getPolygon());
					assert movingOccluders.contains(stationaryOccluders.get(i).getPolygon()) == false;
				}
//				g.setColor(Color.RED);
				for (int i = 0; i < movingObstacles.size(); i++) {
					if (movingObstacles.get(i).getInnerPolygon().intersectsPerimeter(movingObstacles.get(i).getOuterPolygon())){
						g.setColor(Color.GREEN);
						System.out.println(this.getClass().getSimpleName()+": movingObstacles perimeterIntersects");
					}else{
						g.setColor(Color.RED);
					}
					g.draw(movingObstacles.get(i).getPolygon());
				}
				for (int i = 0; i < pulsatingObstacles.size(); i++) {
					if (pulsatingObstacles.get(i).getInnerPolygon().intersectsPerimeter(pulsatingObstacles.get(i).getOuterPolygon())){
						g.setColor(Color.GREEN);
						System.out.println(this.getClass().getSimpleName()+": pulsatingObstacles perimeterIntersects");
					}else{
						g.setColor(Color.RED);
					}
					g.draw(pulsatingObstacles.get(i).getPolygon());
				}
				for (int i = 0; i < stationaryObstacles.size(); i++) {
					if (stationaryObstacles.get(i).getInnerPolygon().intersectsPerimeter(stationaryObstacles.get(i).getOuterPolygon())){
						g.setColor(Color.GREEN);
						System.out.println(this.getClass().getSimpleName()+": stationaryObstacles perimeterIntersects");
					}else{
						g.setColor(Color.RED);
					}
					g.draw(stationaryObstacles.get(i).getPolygon());
				}

				g.setColor(Color.RED);
				for (int i = 0; i < tileBag.getBag().size(); i++) {
					PathBlockingObstacleImpl obst = tileBag.getBag().get(i);
					for (int j = 0; j < obst.getNodes().size(); j++) {
						KPoint nextPoint = obst.getNodes().get(j).getPoint();
						float d = 2f;
						g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f, nextPoint.getY() - d / 2f, d, d));
					}
					//int numNodes = getPathNodes().size();
					//g.setColor(Color.BLACK);
					//g.drawString("copyOfNodes == "+numNodes+", dist == "+Math.round(10*getPathFinder().getEndNode().getGCost())/10f, getPathFinder().getEndNode().getPoint().getX()+6, getPathFinder().getEndNode().getPoint().getY());
				}

				g.setColor(Color.MAGENTA);
				float r = 1f;
				g.fill(new Ellipse2D.Double(lastMouseMovePoint.x - r, lastMouseMovePoint.y - r, 2*r, 2*r));

				Player p = player;
				g.setColor(Color.MAGENTA.darker());
				if (p.pathData.points.size() > 0) {
					KPoint currentPoint = p.getPos();
					for (int j = 0; j < p.pathData.points.size(); j++) {
						KPoint nextPoint = p.pathData.points.get(j);
						g.draw(new Line2D.Double(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(), nextPoint.getY()));
						float d = 5f;
						g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f, nextPoint.getY() - d / 2f, d, d));
						currentPoint = nextPoint;
					}
					//int numNodes = getPathNodes().size();
					//g.setColor(Color.BLACK);
					//g.drawString("copyOfNodes == "+numNodes+", dist == "+Math.round(10*getPathFinder().getEndNode().getGCost())/10f, getPathFinder().getEndNode().getPoint().getX()+6, getPathFinder().getEndNode().getPoint().getY());
				}
				g.setColor(Color.BLUE);
				r = 3;
				g.fill(new Ellipse2D.Double(player.getPos().x - r, player.getPos().y - r, 2*r, 2*r));
			}else{
				float backGroundGrey = 77f/255f;
				g.setColor(new Color(backGroundGrey, backGroundGrey, backGroundGrey));
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				float g4 = 0.1f;
				g.setColor(new Color(g4, g4, g4));
				for (int i = 0; i < allOccluders.size(); i++) {
					g.fill(allOccluders.get(i).getPolygon());
				}

				if (player.cache.visiblePolygon != null){
					{
						Point2D.Double center = new Point2D.Double(player.getPos().x, player.getPos().y);
						float[] dist = {0.0f, 0.9f, 1.0f};
						float g0 = 0.6f;
						float g1 = 0.2f * g0 + 0.8f * backGroundGrey;
						float g2 = backGroundGrey;
						Color[] colors = {new Color(g0, g0, g0, 1f), new Color(g1, g1, g1, 1f), new Color(g2, g2, g2, 1f)};
						RadialGradientPaint paint = new RadialGradientPaint(center, (float)player.cache.maxEyeToBoundaryPolygonPointDist, dist, colors);//, CycleMethod.REFLECT);
						g.setPaint(paint);
						g.fill(player.cache.visiblePolygon);
					}
//					{
//						Point2D.Double center = new Point2D.Double(player.getPos().x, player.getPos().y);
//						float space = 0.1f;
//						float radarSwipeProportionStart = (float)(player.radarSwipeProportion - space/2f);
//						if (radarSwipeProportionStart < 0){
//							radarSwipeProportionStart = 0.000001f;
//						}
//						float radarSwipeProportionEnd = (float)(player.radarSwipeProportion + space/2f);
//						if (radarSwipeProportionEnd > 1){
//							radarSwipeProportionEnd = 0.99999f;
//						}
//						float[] dist = {0.0f, radarSwipeProportionStart, (float)(radarSwipeProportionStart + radarSwipeProportionEnd)/2f, radarSwipeProportionEnd, 1.0f};
//						float g0 = 0.0f;
//						float g1 = 0.2f * g0 + 0.8f * backGroundGrey;
//						Color[] colors = {new Color(g0, g0, g0, 0f), new Color(g0, g0, g0, 0f), new Color(1, g1, g1, 1f), new Color(g0, g0, g0, 0f), new Color(g0, g0, g0, 0f)};
//						RadialGradientPaint paint = new RadialGradientPaint(center, (float)player.getSightField().getMaxEyeToSightPolygonPointDist(), dist, colors);//, CycleMethod.REFLECT);
//						g.setPaint(paint);
//						g.fill(player.getSightField().getSightPolygon());
//					}
				}
				g.setColor(Color.RED);
				double r = bufferAmount;
				double minR = 3;
				if (r < minR){
					r = minR;
				}
				g.fill(new Ellipse2D.Double(player.getPos().x - r, player.getPos().y - r, 2*r, 2*r));
			}


			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 80, 30);
			g.setColor(Color.WHITE);
			int stringX = 10;
			int stringY = 20;
			int yInc = 15;
			g.drawString("FPS: " + fpsCounter.getFPSRounded(), stringX, stringY);
			stringY += 20;
			g.setColor(Color.BLACK);
			g.fillRect(0, 30, 80, 20);
			g.setColor(Color.WHITE);
			//g.drawString(""+lastMouseMovePoint.x+", "+lastMouseMovePoint.y, stringX, stringY);
			g.drawString("Millis: " + fpsCounter.getAvTimeBetweenUpdatesMillis(), stringX, stringY);
			stringY += 20;
		}

		protected VolatileImage createVolatileImage() {
			return createVolatileImage(getWidth(), getHeight(), Transparency.OPAQUE);
		}

		protected VolatileImage createVolatileImage(int width, int height, int transparency) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
			VolatileImage image = null;

			image = gc.createCompatibleVolatileImage(width, height, transparency);

			int valid = image.validate(gc);

			if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
				image = this.createVolatileImage(width, height, transparency);
			}
			//System.out.println(this.getClass().getSimpleName() + ": initiated VolatileImage backImage for quick rendering");
			return image;
		}
//		int counter = 0;
		public void render() {
//			counter++;
//			if (counter % 100 != 0){
//				return;
//			}

			if (getWidth() <= 0 || getHeight() <= 0) {
				System.out.println(this.getClass().getSimpleName() + ": width &/or height <= 0!!!");
				return;
			}
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
			if (backImage == null || getWidth() != backImage.getWidth() || getHeight() != backImage.getHeight() || backImage.validate(gc) != VolatileImage.IMAGE_OK) {
				backImage = createVolatileImage();
			}
			do {
				int valid = backImage.validate(gc);
				if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
					backImage = createVolatileImage();
				}
				backImageGraphics2D = backImage.createGraphics();
				renderWorld();
				// It's always best to dispose of your Graphics objects.
				backImageGraphics2D.dispose();
			} while (backImage.contentsLost());
			if (getGraphics() != null) {
				getGraphics().drawImage(backImage, 0, 0, null);
				Toolkit.getDefaultToolkit().sync(); // to flush the graphics commands to the graphics card.  see http://www.javagaming.org/forums/index.php?topic=15000.msg119601;topicseen#msg119601
			}
		}

		public Graphics2D getBackImageGraphics2D() {
			return backImageGraphics2D;
		}

		public boolean isDrawWireFrame() {
			return drawWireFrame;
		}

		public void setDrawWireFrame(boolean drawWireFrame) {
			this.drawWireFrame = drawWireFrame;
		}

	}


	public class FPSCounter{
		// the following can be used for calculating frames per second:
		protected long lastUpdateNanos = -1;
		protected long cumulativeTimeBetweenUpdatesNanos = 0;
		protected float avTimeBetweenUpdatesMillis = -1f;
		protected int counter = 0;
		protected long timeBetweenUpdatesNanos = 500000000; // 1/2 second == 500000000 nanoseconds

		protected long freeMemory = Runtime.getRuntime().freeMemory();
		protected long totalMemory = Runtime.getRuntime().totalMemory();
		protected long usedMemory = totalMemory - freeMemory;

		public FPSCounter() {
		}
		public void update(){
			if (lastUpdateNanos == -1){
				lastUpdateNanos = System.nanoTime();
			}
			long newUpdateNanos = System.nanoTime();
			cumulativeTimeBetweenUpdatesNanos += newUpdateNanos - lastUpdateNanos;//controller.getWorld().getPureElapsedNanos();
			lastUpdateNanos = newUpdateNanos;
			counter++;
			if (cumulativeTimeBetweenUpdatesNanos >= timeBetweenUpdatesNanos){
				avTimeBetweenUpdatesMillis = (float)((cumulativeTimeBetweenUpdatesNanos)/(counter*1000000f));
				freeMemory = Runtime.getRuntime().freeMemory();
				totalMemory = Runtime.getRuntime().totalMemory();
				usedMemory = totalMemory - freeMemory;
				cumulativeTimeBetweenUpdatesNanos = 0;
				counter = 0;
	//			System.out.println(this.getClass().getSimpleName()+": getFPS() == "+getFPS());
			}
		}

		public float getAvTimeBetweenUpdatesMillis(){
			return avTimeBetweenUpdatesMillis;
		}
		public int getAvTimeBetweenUpdatesMillisRounded(){
			return Math.round(getAvTimeBetweenUpdatesMillis());
		}
		public float getFPS(){
			return (float)(getAvTimeBetweenUpdatesMillis() != 0 ? 1000f/getAvTimeBetweenUpdatesMillis() : -1);
		}
		public int getFPSRounded(){
			return Math.round(this.getFPS());
		}
		public int getCounter(){
			return counter;
		}
		public long getTimeBetweenUpdatesNanos(){
			return timeBetweenUpdatesNanos;
		}
		public void setTimeBetweenUpdatesNanos(long timeBetweenUpdatesNanos){
			this.timeBetweenUpdatesNanos = timeBetweenUpdatesNanos;
		}
		public long getFreeMemory() {
			return freeMemory;
		}
		public long getTotalMemory() {
			return totalMemory;
		}
		public long getUsedMemory() {
			return usedMemory;
		}
	}

	public static void main(String[] args){
		new MovingObstacleTest();
	}
}
