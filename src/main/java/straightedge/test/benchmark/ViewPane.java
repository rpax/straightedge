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
import straightedge.geom.util.*;
import straightedge.geom.path.*;
import straightedge.test.benchmark.event.*;
import straightedge.geom.vision.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;



/**
 *
 * @author Keith Woodward
 */
public class ViewPane extends JComponent {

	GameLoop loop;
	VolatileImage backImage;
	Graphics2D backImageGraphics2D;
	AWTEventHandler eventHandler;
	KPoint center = new KPoint();
	Rectangle.Float viewRectInWorldCoords = new Rectangle.Float();
	float scaleFactor = 1;

	public ViewPane() {
		eventHandler = new AWTEventHandler(this);
		addKeyListener(eventHandler);
		addMouseListener(eventHandler);
		addMouseMotionListener(eventHandler);
		addMouseWheelListener(eventHandler);
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

	public void render() {
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

	//TileArrayAABB<AABBInterface> tempTileArray = new TileArrayAABB<AABBInterface>(new KPoint(0,0), new KPoint(1000,1000), 100);
	VolatileImage worldBackImage;
	protected void renderWorld() {
		Graphics2D g = backImageGraphics2D;
		float xOffset = 0;//getPlayer().getViewOffsetFromCenterX();
		float yOffset = 0;//getPlayer().getViewOffsetFromCenterY();
		center.x = (float) (getWidth() / 2f) + xOffset;
		center.y = (float) (getHeight() / 2f) + yOffset;

		float scaledWidth = getWidth() / scaleFactor;
		float scaledHeight = getHeight() / scaleFactor;

		double playerViewX = getPlayer().getPos().getX();
		double playerViewY = getPlayer().getPos().getY();

		viewRectInWorldCoords.x = (float) (playerViewX - scaledWidth / 2f - xOffset / scaleFactor);
		viewRectInWorldCoords.y = (float) (playerViewY - scaledHeight / 2f - yOffset / scaleFactor);
		viewRectInWorldCoords.width = scaledWidth;
		viewRectInWorldCoords.height = scaledHeight;

//		Bag<geom.los.OccluderImpl> occluders = getWorld().getOccluderTileArray().getBag();
//		{
//			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
//			if (worldBackImage == null || getWidth() != worldBackImage.getWidth() || getHeight() != worldBackImage.getHeight() || worldBackImage.validate(gc) != VolatileImage.IMAGE_OK) {
//				worldBackImage = createVolatileImage();
//				Graphics2D backImageGraphics2D = null;
//				do {
//					int valid = worldBackImage.validate(gc);
//					if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
//						worldBackImage = createVolatileImage();
//					}
//					backImageGraphics2D = worldBackImage.createGraphics();
//				} while (worldBackImage.contentsLost());
//				backImageGraphics2D.setColor(Color.LIGHT_GRAY);
//				backImageGraphics2D.fillRect(0, 0, getWidth(), getHeight());
//				backImageGraphics2D.setColor(Color.GRAY);
//				for (int i = 0; i < occluders.size(); i++) {
//					backImageGraphics2D.fill(occluders.get(i).getPolygon());
//				}
//			}
//			g.drawImage(worldBackImage, 0, 0, null);
//		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (getWorld().isDrawGrid()) {
			g.setColor(Color.WHITE);
			TileArray obstacleTileGrid = getWorld().getObstacleManager().getTileBag().getTileArray();
			for (int i = 0; i < obstacleTileGrid.getNumRows(); i++) {
				for (int j = 0; j < obstacleTileGrid.getNumCols(); j++) {
					float x = (float)(obstacleTileGrid.getBotLeft().x + j * obstacleTileGrid.getTileWidthAndHeight());
					float y = (float)(obstacleTileGrid.getBotLeft().y + i * obstacleTileGrid.getTileWidthAndHeight());
					TileArray.Tile tile = obstacleTileGrid.getTile(i, j);
					g.drawRect((int) x, (int) y, (int)obstacleTileGrid.getTileWidthAndHeight(), (int)obstacleTileGrid.getTileWidthAndHeight());
					g.drawString("co" + tile.getContainedObstacles().size() + ", sh" + tile.getSharedObstacles().size(), x + 2, y + 15);
				}
			}
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setColor(Color.GRAY);
//		g.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < getWorld().getMovingOccluders().size(); i++) {
			g.draw(getWorld().getMovingOccluders().get(i).getPolygon());
		}
		float raidusOfViewInWorldCoords = (float) Math.sqrt(Math.pow(viewRectInWorldCoords.width/2f, 2) + Math.pow(viewRectInWorldCoords.height/2f, 2));
		ArrayList<straightedge.geom.vision.OccluderImpl> occluders = getWorld().getOccluderTileArray().getAllWithin(center, raidusOfViewInWorldCoords);
		for (int i = 0; i < occluders.size(); i++) {
			g.fill(occluders.get(i).getPolygon());
		}

//		g.setColor(Color.RED);
//		float r = 100;
//		KPoint pos = this.getPlayer().getPos();
//		ArrayList<UpdatableObstacle> nearbyObst = getWorld().getObstacleTileArray().getAllWithin(pos, r);
//		for (int i = 0; i < nearbyObst.size(); i++) {
//			g.fill(nearbyObst.get(i).getInnerPolygon());
//		}
//		g.setColor(Color.BLUE);
//		ArrayList<astarpathfinder.los.OccluderImpl> nearbyOccluders = getWorld().getOccluderTileArray().getAllWithin(pos, r);
//		for (int i = 0; i < nearbyOccluders.size(); i++) {
//			g.draw(nearbyOccluders.get(i).getPolygon());
//		}
//		g.draw(new Ellipse2D.Float(pos.x - r, pos.y - r, 2*r, 2*r));



//		for (int i = 0; i < occluders.size(); i++){
//			KPolygon poly = occluders.get(i).getPolygon();
//			final float[] aabbArray = poly.getBoundsFloatArray();
//			AABBInterface aabb = new AABBInterface(){
//				float leftX = aabbArray[0];
//				float botY = aabbArray[1];
//				float rightX = aabbArray[2];
//				float topY = aabbArray[3];
//				public float getLeftX(){
//					return leftX;
//				}
//				public float getBotY(){
//					return botY;
//				}
//				public float getRightX(){
//					return rightX;
//				}
//				public float getTopY(){
//					return topY;
//				}
//			};
//			tempTileArray.add(aabb);
//		}
//		g.setColor(Color.RED);
//		float r = 100;
//		KPoint pos = this.getPlayer().getPos();
//		ArrayList<AABBInterface> nearbyObst = tempTileArray.getAllWithin(pos.x - r, pos.y - r, pos.x + r, pos.y + r);
//		for (int i = 0; i < nearbyObst.size(); i++) {
//			AABBInterface aabb = nearbyObst.get(i);
//			g.draw(new Rectangle2D.Float(aabb.getLeftX(), aabb.getBotY(), aabb.getRightX() - aabb.getLeftX(), aabb.getTopY() - aabb.getBotY()));
//		}
//		g.draw(new Rectangle2D.Float(pos.x - r, pos.y - r, 2*r, 2*r));
//		tempTileArray.clear();

		Player p = getPlayer();
		AffineTransform oldAT = g.getTransform();
		Stroke oldStroke = g.getStroke();
		if (getWorld().isDrawNodeConnections()) {
			g.setStroke(new BasicStroke(5));
		}

		KPolygon sightPolygon = p.visionData.visiblePolygon;
		if (p.calcVision && sightPolygon != null){
//			Point2D.Float center = new Point2D.Float(p.getPos().x, p.getPos().y);
//			float[] dist = {0.0f, 0.9f, 1.0f};
//			float a = 0.5f;
//			float b = 0.2f;
//			float c = 0.0f;
//			Color[] colors = {new Color(a, a, a, 0.7f), new Color(b, b, b, 0.7f), new Color(c, c, c, 0.0f)};
//			RadialGradientPaint paint = new RadialGradientPaint(center, p.getSightField().getMaxEyeToSightPolygonPointDist(), dist, colors);//, CycleMethod.REFLECT);
//			g.setPaint(paint);
			g.setColor(Color.WHITE);
			g.fill(sightPolygon);
		}

//			g.setStroke(new BasicStroke(3));
//			g.setColor(new Color(0,0,0,0.5f));
//			g.draw(sightPolygon);

//			g.setColor(new Color(1f, 1f, 1f, 0.015f));
//			for (int j = 0; j < p.sightFieldL.length; j++){
//				p.sightFieldL[j].recreateAndTransform(p.getPos().x, p.getPos().y, p.moveAngle);
//				KPolygon sightPolygonL = p.sightFieldL[j].getSightPolygon();
//				g.fill(sightPolygonL);
//			}
//			g.setColor(Color.BLACK);
//			for (int j = 0; j < p.sightFieldL.length; j++){
//				float d = 4f;
//				g.fill(new Ellipse2D.Float(p.sightFieldL[j].getEye().getX()-d/2f, p.sightFieldL[j].getEye().getY()-d/2f, d, d));
//			}

		//		Point2D.Float center = new Point2D.Float(pos.x, pos.y);
		//		float[] dist = {0.0f, 0.9f, 1.0f};
		//		Color[] colors = {Color.WHITE, Color.WHITE, Color.LIGHT_GRAY};
		//		RadialGradientPaint p = new RadialGradientPaint(center, sightField.getMaxEyeToSightPolygonPointDist(), dist, colors);//, CycleMethod.REFLECT);
		//		g.setPaint(p);

		//		ArrayList<SightPoint> sightPoints = sightField.getSightPoints();
		//		GeneralPath.Float gp = new GeneralPath.Float(GeneralPath.WIND_EVEN_ODD);
		//		if (sightPoints.size() >= 3){
		//			SightPoint sp = sightPoints.get(0);
		//			gp.moveTo(sp.getPoint().x, sp.getPoint().y);
		//			for (int i = 1; i < sightPoints.size(); i++){
		//				sp = sightPoints.get(i);
		//				gp.lineTo(sp.getPoint().x, sp.getPoint().y);
		//			}
		//			gp.closePath();
		//		}
		//		g.fill(gp);

		g.setColor(Color.MAGENTA.darker());
		if (p.getPathFinder() != null && p.getPathPoints().size() > 0) {
			KPoint currentPoint = p.getPos();
			for (int j = 0; j < p.getPathPoints().size(); j++) {
				KPoint nextPoint = p.getPathPoints().get(j);
				g.draw(new Line2D.Double(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(), nextPoint.getY()));
				float d = 5f;
				g.fill(new Ellipse2D.Double(nextPoint.getX() - d / 2f, nextPoint.getY() - d / 2f, d, d));
				currentPoint = nextPoint;
			}
			//int numNodes = getPathNodes().size();
			//g.setColor(Color.BLACK);
			//g.drawString("nodes == "+numNodes+", dist == "+Math.round(10*getPathFinder().getEndNode().getGCost())/10f, getPathFinder().getEndNode().getPoint().getX()+6, getPathFinder().getEndNode().getPoint().getY());
		}
//		g.setColor(Color.RED);
//		for (int i = 0; i < sightField.polygonAndDists.size(); i++){
//			//int nextI = (i+1 >= sightField.polygonAndDists.size() ? 0 : i+1);
//			PolygonAndDist polygonAndDist = sightField.polygonAndDists.get(i);
//			KPoint p1 = polygonAndDist.getPolygon().getCenter();
//			g.drawString("pZeroAbsAngle: "+polygonAndDist.getPointZeroAbsoluteAngle()+", minRelAngle: "+polygonAndDist.getMinRelativeAngle()+", maxRelAngle: "+polygonAndDist.getMaxRelativeAngle(), p1.x, p1.y+10);
//			//g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
//		}
//		g.setColor(Color.RED);
//		g.draw(sightField.getSightPolygon());
//		g.setColor(Color.RED);
//		for (int i = 0; i < sightField.sightPoints.size(); i++){
//			//int nextI = (i+1 >= sightField.pointList.size() ? 0 : i+1);
//			KPoint p1 = sightField.sightPoints.get(i).getPoint();
//			g.drawString(""+i, p1.x, p1.y+10);
//			//g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
//		}
//		g.setColor(Color.BLACK);
//		g.draw(sightField.getTransformedSightPolygon());
//
//		for (int i = 0; i < sightField.intersectedPolygons.size(); i++){
//			g.setColor(new Color(0.5f, 1f, 0.5f, 0.3f));
//			g.fill(sightField.intersectedPolygons.get(i));
//		}
//		g.setColor(Color.BLUE);
//		for (int i = 0; i < sightField.sightPoints.size(); i++){
//
//			KPoint p = sightField.sightPoints.get(i).getPoint();
//			g.drawString(""+i+", "+sightField.sightPoints.get(i).getClass().getSimpleName(), p.x, p.y+10);
//		}



		g.setStroke(oldStroke);
//		g.setColor(Color.WHITE.darker());
		g.setColor(Color.MAGENTA.darker());
		g.translate(p.pos.getX(), p.pos.getY());
		g.rotate(p.getLookAngle());
		g.fill(p.polygon);
		g.setColor(Color.YELLOW);
		float d = 3f;
		g.fill(new Ellipse2D.Float(-d / 2f, -d / 2f, d, d));
		g.setTransform(oldAT);

		if (p.editableRect != null) {
			g.setColor(Color.BLACK);
			g.draw(p.editableRect);
		}
		assert p.playerOutsideOfObstacles();

		Bag<PathBlockingObstacleImpl> obstacles = getWorld().getObstacleManager().getTileBag().getBag();
		if (getWorld().isDrawNodeConnections()) {
			p.getPathFinder().debug = true;
			// render connections between reachableNodes
			g.setColor(Color.DARK_GRAY);
			for (int i = 0; i < obstacles.size(); i++) {
				if (getWorld().isDrawNodeConnections()) {
					for (int j = 0; j < obstacles.get(i).getNodes().size(); j++) {
						KNode currentNode = obstacles.get(i).getNodes().get(j);
						for (KNode n : currentNode.getConnectedNodes()) {
							g.draw(new Line2D.Double(currentNode.getPoint().x, currentNode.getPoint().y, n.getPoint().getX(), n.getPoint().getY()));
						}
					}
				}
			}

			// render connections between tempReachableNodes
			g.setColor(Color.BLUE);
			KPoint startPoint = p.getPathFinder().startPointDebug;
			for (KNode n : p.getPathFinder().startNodeTempReachableNodesDebug) {
				g.draw(new Line2D.Double(startPoint.x, startPoint.y, n.getPoint().getX(), n.getPoint().getY()));
			}
			KPoint endPoint = p.getPathFinder().endPointDebug;
			for (KNode n : p.getPathFinder().endNodeTempReachableNodesDebug) {
				g.draw(new Line2D.Double(endPoint.x, endPoint.y, n.getPoint().getX(), n.getPoint().getY()));
			}

//			// draw the node numbers
//			for (int i = 0; i < obstacles.size(); i++) {
//				for (int j = 0; j < obstacles.get(i).getNodes().size(); j++) {
//					KNode currentNode = obstacles.get(i).getNodes().get(j);
//					KPoint point = currentNode.getPoint();
//					g.drawString(""+j, (float)point.x, (float)point.y+10);
//				}
//			}
		}else{
			p.getPathFinder().debug = false;
		}


//		g.setTransform(oldAT);
		g.setColor(Color.BLACK);
		//g.fillRect(0, 0, 80, 30);
		g.fillRect(0, 0, 80, 50);
		g.setColor(Color.WHITE);
		int stringX = 10;
		int stringY = 20;
		int yInc = 15;
		g.drawString("FPS: " + getLoop().getFpsCounter().getFPSRounded(), stringX, stringY);
		stringY += yInc;
		g.drawString("ms: " + getLoop().getFpsCounter().getAvTimeBetweenUpdatesMillis(), stringX, stringY);
		g.setColor(Color.MAGENTA.darker());
		stringY += 10;
		stringY += yInc;
		g.drawString("obstacles: ", stringX, stringY);//+world.getNumObstacles(), stringX, stringY);
		stringY += yInc;
		g.drawString("" + getWorld().getNumObstacles(), stringX, stringY);
		stringY += yInc;
		g.drawString("nodes: ", stringX, stringY);//+world.getNumNodes(), stringX, stringY);
		stringY += yInc;
		g.drawString("" + getWorld().getNumNodes(), stringX, stringY);
		stringY += yInc;
		g.drawString("nodeConnections: ", stringX, stringY);//+world.getNumPermanentNodeConnections(), stringX, stringY);
		stringY += yInc;
		g.drawString("" + getWorld().getNumPermanentNodeConnections(), stringX, stringY);
		stringY += yInc;
		g.drawString("" + getPlayer().lastMouseMove, stringX, stringY);
	}

	public Graphics2D getBackImageGraphics2D() {
		return backImageGraphics2D;
	}

	public AWTEventHandler getEventHandler() {
		return eventHandler;
	}

	public GameLoop getLoop() {
		return loop;
	}
	public void setLoop(GameLoop loop) {
		if (this.loop != loop){
			this.loop = loop;
			loop.setView(this);
		}
	}

	public Player getPlayer() {
		return getLoop().getPlayer();
	}

	public GameWorld getWorld() {
		return getLoop().getWorld();
	}
}
