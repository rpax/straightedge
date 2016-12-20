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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
 *
 * @author Keith
 */
public class LineTest {
	JFrame frame;
	LineTest.ViewPane view;
	volatile boolean keepRunning = true;
	FPSCounter fpsCounter;

	Object mutex = new Object();
	ArrayList<InputEvent> events = new ArrayList<InputEvent>();
	ArrayList<InputEvent> eventsCopy = new ArrayList<InputEvent>();
	Point2D.Float lastMouseMovePoint = new Point2D.Float();
	ArrayList<Point2D.Float> clicks = new ArrayList<Point2D.Float>();


	protected GeometryFactory geometryFactory;
	Path2D.Float polygon;

	public LineTest(){
		frame = new JFrame("JTS Line to Polygon Buffer Test");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				keepRunning = false;
				System.exit(0);
			}
		});

		view = new LineTest.ViewPane();
		frame.add(view);

		init();

		view.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
			public void mouseDragged(MouseEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
		});
		view.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
		});
		frame.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				synchronized (mutex){
					events.add(e);
				}
			}
		});

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

	
	public void init(){
		geometryFactory = new GeometryFactory();
		polygon =  null;
	}
	
	public void update(float seconds){
		synchronized(mutex){
			if (events.size() > 0){
				eventsCopy.addAll(events);
				events.clear();
			}
		}
		if (eventsCopy.size() > 0){
			for (int i = 0; i < eventsCopy.size(); i++){
				InputEvent ie = eventsCopy.get(i);
				if (ie instanceof MouseEvent){
					MouseEvent e = (MouseEvent)ie;
					if (e.getID() == MouseEvent.MOUSE_MOVED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
					}else if (e.getID() == MouseEvent.MOUSE_PRESSED){
						Point2D.Float p = new Point2D.Float(e.getX(), e.getY());
						clicks.add(p);
					}else if (e.getID() == MouseEvent.MOUSE_DRAGGED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
						Point2D.Float p = new Point2D.Float(e.getX(), e.getY());
						clicks.add(p);
					}
				}else if (ie instanceof KeyEvent){
					KeyEvent e = (KeyEvent)ie;
					if (e.getID() == KeyEvent.KEY_PRESSED){
						if (e.getKeyCode() == KeyEvent.VK_C){
							clicks.clear();
						}else if (e.getKeyCode() == KeyEvent.VK_Z){
							if (clicks.size() > 0){
								clicks.remove(clicks.size()-1);
							}
						}
					}
				}
			}
			eventsCopy.clear();
		}

		// if there are enough clicks to make a line, buffer that line (which turns
		// it into a jts Polygon), then make a path2D out of it so we can render it.
		if (clicks.size() >= 1){
			Coordinate[] coordinateArray = new Coordinate[clicks.size() + 1];
			for (int i = 0; i < clicks.size(); i++){
				Point2D.Float p = clicks.get(i);
				coordinateArray[i] = new Coordinate(p.x, p.y);
			}
			// add the lastMouseMovedPoint
			coordinateArray[coordinateArray.length-1] = new Coordinate(this.lastMouseMovePoint.x, lastMouseMovePoint.y);

			LineString lineString = geometryFactory.createLineString(coordinateArray);
			int quadrantSegments = 4;
			Geometry bufferedGeometry = lineString.buffer(30, quadrantSegments);

			com.vividsolutions.jts.geom.Polygon bufferedJTSPolygon = null;
			if (bufferedGeometry instanceof com.vividsolutions.jts.geom.Polygon){
				bufferedJTSPolygon = (com.vividsolutions.jts.geom.Polygon)bufferedGeometry;
			}else{
				throw new RuntimeException("JTS didn't make a proper polygon...");
			}
			polygon = makePath2DFrom(bufferedJTSPolygon);
		}else{
			polygon = null;
		}
	}

	public Path2D.Float makePath2DFrom(com.vividsolutions.jts.geom.Polygon jtsPolygon){
		Path2D.Float path = new Path2D.Float();
		// the jtsPolygon could be made of a number of 'rings', so add each to the path.
		LineString lineString = jtsPolygon.getExteriorRing();
		addLineStringToPath(lineString, path);
		for (int i = 0; i < jtsPolygon.getNumInteriorRing(); i++){
			lineString = jtsPolygon.getInteriorRingN(i);
			addLineStringToPath(lineString, path);
		}
		return path;
	}

	public void addLineStringToPath(LineString lineString, Path2D path){
		CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
		// add the first coord to the path
		Coordinate coord = coordinateSequence.getCoordinate(0);
		path.moveTo((float)coord.x, (float)coord.y);
		// The loop stops at the second-last coord since the last coord will be
		// the same as the start coord.
		for (int i = 1; i < coordinateSequence.size()-1; i++){
			coord = coordinateSequence.getCoordinate(i);
			path.lineTo((float)coord.x, (float)coord.y);
		}
		path.closePath();
	}

	
	public class ViewPane extends JComponent {
		VolatileImage backImage;
		Graphics2D backImageGraphics2D;

		public ViewPane() {
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

		VolatileImage worldBackImage;
		protected void renderWorld() {
			Graphics2D g = backImageGraphics2D;

			// clear the background
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());

			// draw a grid
			float gridSpace = 100;
			int colGridLines = (int)(this.getWidth()/gridSpace) + 1;
			int rowGridLines = (int)(this.getHeight()/gridSpace) + 1;
			g.setColor(Color.WHITE);
			for (int i = 0; i < colGridLines; i++){
				g.draw(new Line2D.Float(i*gridSpace, 0, i*gridSpace, this.getHeight()));
			}
			for (int i = 0; i < rowGridLines; i++){
				g.draw(new Line2D.Float(0, i*gridSpace, this.getWidth(), i*gridSpace));
			}

			// draw the mouse dot
			g.setColor(Color.MAGENTA);
			float r = 1f;
			g.fill(new Ellipse2D.Float(lastMouseMovePoint.x - r, lastMouseMovePoint.y - r, 2*r, 2*r));

			// draw the polygon (a buffered line) if there is one
			if (polygon != null){
				g.setColor(new Color(0.5f, 0.5f, 0.75f, 0.5f));
				g.fill(polygon);
				PathIterator pathIterator = polygon.getPathIterator(null);
				float[] coords = new float[6];
				while (pathIterator.isDone() == false){
					pathIterator.currentSegment(coords);
					float radius = 2;
					float x = coords[0];
					float y = coords[1];
					g.fill(new Ellipse2D.Float(x - radius, y - radius, radius*2, radius*2));
					pathIterator.next();
				}
			}

			// draw the clicks in a line if it exists
			if (clicks.size() > 0){
				g.setColor(Color.DARK_GRAY);
				for (int i = 0; i < clicks.size(); i++){
					float radius = 2;
					float x = clicks.get(i).x;
					float y = clicks.get(i).y;
					g.fill(new Ellipse2D.Float(x - radius, y - radius, radius*2, radius*2));
					if (i+1 != clicks.size()){
						float x2 = clicks.get(i+1).x;
						float y2 = clicks.get(i+1).y;
						g.draw(new Line2D.Float(x, y, x2, y2));
					}
				}
			}

			// draw the fps
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 80, 30);
			g.setColor(Color.WHITE);
			int stringX = 10;
			int stringY = 20;
			int yInc = 15;
			g.drawString("FPS: " + fpsCounter.getFPSRounded(), stringX, stringY);
			g.drawString("Click or drag to insert points, Z to undo, C to clear.", stringX+85, stringY);
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
		new LineTest();
	}
}
