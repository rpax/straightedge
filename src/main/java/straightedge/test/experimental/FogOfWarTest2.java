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
package straightedge.test.experimental;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import straightedge.geom.*;
import straightedge.geom.vision.*;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.*;

/**
 *
 * @author Keith
 */
public class FogOfWarTest2 {
	JFrame frame;
	FogOfWarTest2.ViewPane view;
	volatile boolean keepRunning = true;
	FPSCounter fpsCounter;

	Object mutex = new Object();
	ArrayList<AWTEvent> events = new ArrayList<AWTEvent>();
	ArrayList<AWTEvent> eventsCopy = new ArrayList<AWTEvent>();
	KPoint lastMouseMovePoint = new KPoint();

	VisionFinder visionFinder;
	double smallAmount = 0.0001f;
	VisionData performanceCache = null;
	KPolygon visiblePolygon = null;

	PolygonConverter polygonConverter;
	Geometry jtsPolygon;
	ArrayList<OccluderImpl> occluders;

	public FogOfWarTest2(){
		frame = new JFrame("FogOfWarTest");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		view = new FogOfWarTest2.ViewPane();
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
		view.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
		});

		{
			int numPoints = 20;
			float radius = 200;
			KPolygon boundaryPolygon = KPolygon.createRegularPolygon(numPoints, radius);
			// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
			KPoint eye = new KPoint(smallAmount, smallAmount);
			performanceCache = new VisionData(eye, boundaryPolygon);
			visionFinder = new VisionFinder();
		}
		polygonConverter = new PolygonConverter();

		remakeOccluders();

		fpsCounter = new FPSCounter();

		frame.setVisible(true);

		Thread gameLoopThread = new Thread(){
			public void run(){
				long lastUpdateNanos = System.nanoTime();
				while(keepRunning){
					long currentNanos = System.nanoTime();
					float seconds = (currentNanos - lastUpdateNanos)/1000000000f;
					update(seconds);
					fpsCounter.update();
					view.render();
					Thread.yield();
					lastUpdateNanos = currentNanos;
				}
			}
		};
		gameLoopThread.setDaemon(true);
		gameLoopThread.start();
	}

	public void remakeOccluders(){
		// make the occluders
		// make random rectangles
		Random rand = new Random();
		occluders = new ArrayList<OccluderImpl>();
		for (int i = 0; i < 4; i++){
			KPoint p = new KPoint((float)rand.nextFloat()*frame.getWidth(), (float)rand.nextFloat()*frame.getHeight());
			KPoint p2 = new KPoint((float)rand.nextFloat()*frame.getWidth(), (float)rand.nextFloat()*frame.getHeight());
			float width = 10 + 30*rand.nextFloat();
			KPolygon rect = KPolygon.createRectOblique(p, p2, width);
			occluders.add(new OccluderImpl(rect));
		}
		// make a cross
		occluders.add(new OccluderImpl(KPolygon.createRectOblique(40, 70, 100, 70, 20)));
		occluders.add(new OccluderImpl(KPolygon.createRectOblique(70, 40, 70, 100, 20)));
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
			occluders.add(new OccluderImpl(poly));
		}
		jtsPolygon = null;
	}

	int counter = 0;
	public void update(float seconds){
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
					}
				}else if (awtEvent instanceof ComponentEvent){
					ComponentEvent e = (ComponentEvent)awtEvent;
					if (e.getID() == ComponentEvent.COMPONENT_RESIZED){
						this.remakeOccluders();
					}
				}
			}
			eventsCopy.clear();
		}

		// rotate every 2nd occluder
		float rotateSpeed = (float)(Math.PI*2f/16f);
		for (int i = 0; i < occluders.size(); i+=2){
			OccluderImpl occluder = occluders.get(i);
			occluder.getPolygon().rotate(rotateSpeed*seconds);
		}

		// Move the eye and boundaryPolygon to wherever they need to be.
		// By making the eye slightly offset from its integer coordinate by smallAmount,
		// it will prevent problems caused by collinearity.
		performanceCache.eye.setCoords(lastMouseMovePoint.x + smallAmount, lastMouseMovePoint.y + smallAmount);
		performanceCache.boundaryPolygon.translateTo(performanceCache.eye);
		visionFinder.calc(performanceCache, new ArrayList<Occluder>(0), new ArrayList<VPOccluderOccluderIntersection>(0), occluders);
		visiblePolygon = performanceCache.visiblePolygon;
		/* Note that the above is a slow way to process shadows - every occluder is
		 * intersected with every other occluder, which is not necessary if some of the
		 * occluders are stationary.
		 */


		if (visiblePolygon != null){
			if (jtsPolygon == null){
				jtsPolygon = polygonConverter.makeJTSPolygonFrom(visiblePolygon);
			 }
			Polygon jtsPolygonOfSightField = polygonConverter.makeJTSPolygonFrom(visiblePolygon);
			Geometry union = null;
			boolean differenceOK = false;
			try{
				Geometry[] polygons = {jtsPolygon, jtsPolygonOfSightField};
				GeometryCollection polygonCollection = polygonConverter.getGeometryFactory().createGeometryCollection(polygons);
				union = polygonCollection.buffer(0);
				differenceOK = true;
			}catch(TopologyException e){
				e.printStackTrace();
			}
			if (differenceOK){
				jtsPolygon = union;
//				Geometry simplifiedJTSPolygon = null;
//				if (jtsPolygon instanceof Polygon){
//					simplifiedJTSPolygon = polygonConverter.simplify((Polygon)jtsPolygon);
//				}else if (jtsPolygon instanceof MultiPolygon){
//					simplifiedJTSPolygon = polygonConverter.simplify((MultiPolygon)jtsPolygon);
//				}
				if (counter % 200 == 0){
		//			System.out.println(this.getClass().getSimpleName()+": jtsPolygon.getNumPoints() == "+jtsPolygon.getNumPoints());
					System.out.println(this.getClass().getSimpleName()+": getNumPoints() == "+union.getNumPoints());
					if (union instanceof Polygon){
						Polygon polygon = ((Polygon)union);
						System.out.println(this.getClass().getSimpleName()+": polygon.getExteriorRing().getCoordinateSequence().size() == "+polygon.getExteriorRing().getCoordinateSequence().size());
						System.out.println(this.getClass().getSimpleName()+": polygon.getNumInteriorRing() == "+polygon.getNumInteriorRing());
					}else if (union instanceof MultiPolygon){
						MultiPolygon multiPolygon = ((MultiPolygon)union);
						System.out.println(this.getClass().getSimpleName()+": multiPolygon.getNumGeometries() == "+multiPolygon.getNumGeometries());
						for (int i = 0; i < multiPolygon.getNumGeometries(); i++){
							Polygon poly = (Polygon)multiPolygon.getGeometryN(i);
							System.out.println(this.getClass().getSimpleName()+": poly"+i+".getNumInteriorRing() == "+poly.getNumInteriorRing());
						}
					}
				}
				counter++;
			}

			if (jtsPolygon instanceof Polygon){
				path2D = polygonConverter.makePath2DFrom((Polygon)jtsPolygon);
			}else if (jtsPolygon instanceof MultiPolygon){
				path2D = polygonConverter.makePath2DFrom((MultiPolygon)jtsPolygon);
			}

		}
	}
	Path2D.Double path2D = null;

	public class ViewPane extends JComponent {
		VolatileImage backImage;
		Graphics2D backImageGraphics2D;

		public ViewPane() {
		}

		protected void renderWorld() {
			Graphics2D g = backImageGraphics2D;

			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setColor(Color.GRAY);
			for (int i = 0; i < occluders.size(); i++) {
				g.fill(occluders.get(i).getPolygon());
			}
			g.setColor(Color.BLUE);
			for (int i = 0; i < occluders.size(); i++) {
				g.draw(occluders.get(i).getPolygon());
			}
			if (visiblePolygon != null){
				g.setColor(Color.WHITE);
				g.fill(visiblePolygon);
				g.setColor(Color.BLACK);
				g.draw(visiblePolygon);
			}
			if (path2D != null){
				g.setColor(new Color(0.1f, 0.1f, 0.1f, 0.6f));
				g.fill(path2D);
			}

			g.setColor(Color.MAGENTA);
			float r = 1f;
			g.fill(new Ellipse2D.Double(lastMouseMovePoint.x - r, lastMouseMovePoint.y - r, 2*r, 2*r));

			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 80, 30);
			g.setColor(Color.WHITE);
			int stringX = 10;
			int stringY = 20;
			int yInc = 15;
			g.drawString("FPS: " + fpsCounter.getFPSRounded(), stringX, stringY);

			path2D = null;
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



		public Graphics2D getBackImageGraphics2D() {
			return backImageGraphics2D;
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
		new FogOfWarTest2();
	}
}
