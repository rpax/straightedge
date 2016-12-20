/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test;

import straightedge.geom.*;
import straightedge.geom.path.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * A simple demo of path-finding.
 * 
 * Note that this uses passive rendering, meaning that
 * all rendering is done on Swing's Event Dispatch Thread via the repaint() method.
 * This is different to active rendering where the rendering happens on a
 * separate thread and is more predictable.
 *
 * @author Keith
 */
public class PathTest {
	JFrame frame;

	ArrayList<PathBlockingObstacleImpl> stationaryObstacles;
	NodeConnector nodeConnector;
	float maxConnectionDistanceBetweenObstacles;
	PathFinder pathFinder;

	Player player;
	KPoint lastMouseMovePoint = new KPoint(100, 100);

	long lastSystemTimeNanos = System.nanoTime();

	public PathTest(){
		// Set up the window:
		frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Make the obstacles:
		stationaryObstacles = new ArrayList<PathBlockingObstacleImpl>();
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				ArrayList<KPoint> pointList = new ArrayList<KPoint>();
				int numPoints = 6;
				double angleIncrement = Math.PI*2f/(numPoints*2);
				float rBig = 50;
				float rSmall = 20;
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
				poly.rotate(i+j);
				poly.translate(100 + 100*i, 100 + 100*j);
				stationaryObstacles.add(PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly));
			}
		}

		// Connect the obstacles' nodes so that the PathFinder can do its work:
		maxConnectionDistanceBetweenObstacles = 1000f;
		nodeConnector = new NodeConnector();
		for (int k = 0; k < stationaryObstacles.size(); k++){
			nodeConnector.addObstacle(stationaryObstacles.get(k), stationaryObstacles, maxConnectionDistanceBetweenObstacles);
		}

		// Initialise the PathFinder
		pathFinder = new PathFinder();
		player = new Player();
		player.pos = new KPoint(100, 100);
		player.target = player.pos.copy();

		final JComponent renderComponent = new JComponent(){
			// This method is called every time the repaintThread does renderComponent.repaint().
			public void paint(Graphics graphics){
				// calculate the time since the last update.
				long currentSystemTimeNanos = System.nanoTime();
				double nanosInASecond = 1000000000;
				double secondsElapsed = (currentSystemTimeNanos - lastSystemTimeNanos)/nanosInASecond;
				lastSystemTimeNanos = currentSystemTimeNanos;
				player.target = lastMouseMovePoint;
				// re-calculate the players path and move him forward.
				player.update(secondsElapsed);

				// render the obstacles and player:
				Graphics2D g = (Graphics2D)graphics;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				float backGroundGrey = 77f/255f;
				g.setColor(new Color(backGroundGrey, backGroundGrey, backGroundGrey));
				g.fillRect(0, 0, getWidth(), getHeight());

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				float g4 = 0.1f;
				g.setColor(new Color(g4, g4, g4));
				for (int i = 0; i < stationaryObstacles.size(); i++) {
					g.fill(stationaryObstacles.get(i).getPolygon());
				}
				// render the player's path:
				g.setColor(Color.LIGHT_GRAY);
				ArrayList<KPoint> pathPoints = player.pathData.points;
				if (pathPoints.size() > 0) {
					KPoint currentPoint = player.pos;
					for (int j = 0; j < pathPoints.size(); j++) {
						KPoint nextPoint = pathPoints.get(j);
						g.draw(new Line2D.Double(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(), nextPoint.getY()));
						float d = 5f;
						g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f, nextPoint.getY() - d / 2f, d, d));
						currentPoint = nextPoint;
					}
				}

				g.setColor(Color.RED);
				double r = 5;
				g.fill(new Ellipse2D.Double(player.pos.x - r, player.pos.y - r, 2*r, 2*r));
			}
		};

		frame.add(renderComponent);
		renderComponent.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				lastMouseMovePoint.x = e.getX();
				lastMouseMovePoint.y = e.getY();
			}
		});
		frame.setVisible(true);

		// This thread calls repaint() in a never-ending loop to animate the renderComponent.
		Thread repaintThread = new Thread(){
			public void run(){
				while(true){
					renderComponent.repaint();
					try{Thread.sleep(1);}catch(Exception e){}
				}
			}
		};
		repaintThread.setDaemon(true);
		repaintThread.start();
	}

	public class Player{
		KPoint pos;
		KPoint target;
		KPoint targetAdjusted;
		float maxConnectionDistanceFromPlayerToObstacles;
		PathData pathData;
		float speed;
		float speedX;
		float speedY;
		float moveAngle;
		KPoint currentTargetPoint = null;

		public Player(){
			maxConnectionDistanceFromPlayerToObstacles = PathTest.this.maxConnectionDistanceBetweenObstacles;
			pathData = new PathData();
			speed = 200;
		}
		public void update(double seconds){
			pos = getNearestPointOutsideOfObstacles(pos);
			targetAdjusted = getNearestPointOutsideOfObstacles(target);
			// This is where the PathFinder does its work:
			pathData = pathFinder.calc(pos, targetAdjusted, maxConnectionDistanceFromPlayerToObstacles, nodeConnector, stationaryObstacles);

			if (speed == 0){
				return;
			}
			// Update the player's position as it travels from point to point along the path.
			double secondsLeft = seconds;
			ArrayList<KPoint> pathPoints = player.pathData.points;
			for (int i = 0; i < pathPoints.size(); i++){
				currentTargetPoint = pathPoints.get(i);
				KPoint oldPos = new KPoint();
				oldPos.x = pos.x;
				oldPos.y = pos.y;
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
					pathPoints.remove(i);
					i--;
				}else{
					pos.x = (float)(oldPos.x + secondsLeft * speedX);
					pos.y = (float)(oldPos.y + secondsLeft * speedY);
					secondsLeft = 0;
					break;
				}
			}
		}

		public KPoint getNearestPointOutsideOfObstacles(KPoint point){
			// check that the target point isn't inside any obstacles.
			// if so, move it.
			KPoint movedPoint = point.copy();
			boolean targetIsInsideObstacle = false;
			int count = 0;
			while (true){
				for (PathBlockingObstacleImpl obst : stationaryObstacles){
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
	}

	public static void main(String[] args){
		new PathTest();
	}
}
