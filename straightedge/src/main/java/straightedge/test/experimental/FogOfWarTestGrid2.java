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
public class FogOfWarTestGrid2 {
	JFrame frame;
	FogOfWarTestGrid2.ViewPane view;
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
	ArrayList<OccluderImpl> occluders;

	public FogOfWarTestGrid2(){
		frame = new JFrame("FogOfWarTest");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		view = new FogOfWarTestGrid2.ViewPane();
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
	}

	Cell[][] cells = null;
	public void update(float seconds){
		// copy the events list:
		synchronized(mutex){
			if (events.size() > 0){
				eventsCopy.addAll(events);
				events.clear();
			}
		}
		// process the events
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

		// If the viewPane has a non-zero size, create the cell array if it's still null.
		if ((cells == null || view.getWidth() != w || view.getHeight() != h) &&
				(view.getWidth() != 0 && view.getHeight() != 0)){
			w = view.getWidth();
			h = view.getHeight();
			numRows = (int)Math.ceil(h/cellWidthAndHeight);
			numCols = (int)Math.ceil(w/cellWidthAndHeight);
			cells = new Cell[numRows][numCols];
			for (int i = 0; i < numRows; i++){
				Cell[] row = cells[i];
				for (int j = 0; j < numCols; j++){
					float x = j*cellWidthAndHeight + cellWidthAndHeight/2f;
					float y = i*cellWidthAndHeight + cellWidthAndHeight/2f;
					Cell cell = new Cell(cells, new KPoint(x, y), i, j);
					// make it as a border cell if it is on the edge.
					if (i == 0 || i == numRows-1 || j == 0 || j == numCols-1){
						cell.setBorder(true);
					}
					row[j] = cell;
				}
			}
		}
		if (cells != null && visiblePolygon != null){
			KPolygon sightPolygon = visiblePolygon;
			KPoint c = sightPolygon.getCenter();
			double r = sightPolygon.getRadius();
			KPoint botLeft = cells[0][0].getPoint();//new KPoint(0,0);
			double leftColIndex = ((c.x - r) - botLeft.x)/cellWidthAndHeight;
			double rightColIndex = ((c.x + r) - botLeft.x)/cellWidthAndHeight;
			double botRowIndex = ((c.y - r) - botLeft.y)/cellWidthAndHeight;
			double topRowIndex = ((c.y + r) - botLeft.y)/cellWidthAndHeight;
			//System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", leftColIndex == "+leftColIndex+", rightColIndex == "+rightColIndex+", botRowIndex == "+botRowIndex+", topRowIndex == "+topRowIndex);
			if (botRowIndex < 0){
				botRowIndex = 0;
			}else if (botRowIndex >= numRows){
				botRowIndex = numRows - 1;
			}
			if (topRowIndex < 0){
				topRowIndex = 0;
			}else if (topRowIndex >= numRows){
				topRowIndex = numRows - 1;
			}
			if (leftColIndex < 0){
				leftColIndex = 0;
			}else if (leftColIndex >= numCols){
				leftColIndex = numCols - 1;
			}
			if (rightColIndex < 0){
				rightColIndex = 0;
			}else if (rightColIndex >= numCols){
				rightColIndex = numCols - 1;
			}
			int leftColIndexInt = (int)leftColIndex;
			int rightColIndexInt = (int)rightColIndex;
			int botRowIndexInt = (int)botRowIndex;
			int topRowIndexInt = (int)topRowIndex;
			//System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", leftColIndexInt == "+leftColIndexInt+", rightColIndexInt == "+rightColIndexInt+", botRowIndexInt == "+botRowIndexInt+", topRowIndexInt == "+topRowIndexInt);

			// see if any more cells are contained
			for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
				Cell[] row = cells[i];
				for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
					Cell cell = row[j];
					if (cell.isDiscovered() == false){
						if (sightPolygon.contains(cell.getPoint())){
							cell.setDiscovered(true);
							cell.setBorder(false);
							newlyDiscoveredCells.add(cell);
						}
					}
				}
			}

			// find new border cells
			ArrayList<Cell> adjacentCellsOrtho = new ArrayList<Cell>();
			for (int i = 0; i < newlyDiscoveredCells.size(); i++){
				Cell cell = newlyDiscoveredCells.get(i);
				cell.getAdjacentCellsOrtho(adjacentCellsOrtho);
				for (int k = 0; k < adjacentCellsOrtho.size(); k++){
					Cell adjCell = adjacentCellsOrtho.get(k);
					if (adjCell.isDiscovered() == false){
						adjCell.setBorder(true);
					}
				}
				adjacentCellsOrtho.clear();
			}
			newlyDiscoveredCells.clear();
		}

	}
	ArrayList<Cell> newlyDiscoveredCells = new ArrayList<Cell>();
	float w = -1;
	float h = -1;
	int numRows;
	int numCols;
	float cellWidthAndHeight = 20;

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
//			if (path2D != null){
//				g.setColor(new Color(0.1f, 0.1f, 0.1f, 0.6f));
//				g.fill(path2D);
//			}
			if (cells != null){
				for (int i = 0; i < numRows; i++){
					Cell[] row = cells[i];
					for (int j = 0; j < numCols; j++){
						Cell cell = row[j];
						if (cell.isDiscovered() == false){
							if (cell.isBorder()){
								g.setColor(new Color(0.5f, 0.1f, 0.1f, 0.6f));
							}else{
								g.setColor(new Color(0.1f, 0.1f, 0.1f, 0.6f));
							}
							KPoint p = cell.getPoint();
							int r = (int)(cellWidthAndHeight/2f);
							g.fill(new Ellipse2D.Double(p.x - r, p.y - r, 2*r, 2*r));
							//g.fillRect((int)(p.x - r), (int)(p.y - r), 2*r, 2*r);
						}
					}
				}
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

	public class Cell{
		Cell[][] cells;
		KPoint point;
		int row;
		int col;
		boolean discovered;
		boolean border;
		public Cell(Cell[][] cells, KPoint point, int row, int col){
			this.cells = cells;
			this.point = point;
			this.row = row;
			this.col = col;

			discovered = false;
			border = false;
		}

		public void getAdjacentCellsOrtho(ArrayList<Cell> list){
			if (row != 0){
				list.add(cells[row-1][col]);
			}
			if (row != numRows-1){
				list.add(cells[row+1][col]);
			}
			if (col != 0){
				list.add(cells[row][col-1]);
			}
			if (col != numCols-1){
				list.add(cells[row][col+1]);
			}
		}

		public boolean isBorder() {
			return border;
		}

		public void setBorder(boolean border) {
			this.border = border;
		}

		public boolean isDiscovered() {
			return discovered;
		}

		public void setDiscovered(boolean discovered) {
			this.discovered = discovered;
		}

		public KPoint getPoint() {
			return point;
		}

		public int getCol() {
			return col;
		}

		public int getRow() {
			return row;
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
		new FogOfWarTestGrid2();
	}


}
