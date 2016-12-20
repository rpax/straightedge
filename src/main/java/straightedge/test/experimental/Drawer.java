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
public class Drawer {
	JFrame frame;
	Drawer.ViewPane view;
	volatile boolean keepRunning = true;
	FPSCounter fpsCounter;

	Object mutex = new Object();
	ArrayList events = new ArrayList();
	ArrayList eventsCopy = new ArrayList();
	Point2D.Double lastMouseMovePoint = new Point2D.Double();
	boolean draw = false;

	Arm arm;

	public Drawer(){
		frame = new JFrame("LineOfSight");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		view = new Drawer.ViewPane();
		frame.add(view);

		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				keepRunning = false;
				frame.dispose();
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

//	public void init(){
//		arm = new Arm();
//
//		Arm childArm = new Arm();
//		arm.setChildArm(childArm);
//		childArm.setParentArm(arm);
//
//		Arm childArm2 = new Arm();
//		childArm.setChildArm(childArm2);
//		childArm2.setParentArm(childArm);
//
//        Arm childArm3 = new Arm();
//		childArm2.setChildArm(childArm3);
//		childArm3.setParentArm(childArm2);
//
//		arm.setRotateSpeed(Math.PI*2/0.2);
//		childArm.setRotateSpeed(Math.PI*2*33);
//		childArm2.setRotateSpeed(Math.PI*2*132);
//		childArm3.setRotateSpeed(Math.PI*2*33);
//
////		arm.setRotateSpeed(Math.PI*2*721);
////		childArm.setRotateSpeed(Math.PI*2*33);
////		childArm2.setRotateSpeed(Math.PI*2*132);
////		childArm3.setRotateSpeed(Math.PI*2*33);
//
////		arm.setRotateSpeed(Math.PI*2*14);
////		childArm.setRotateSpeed(Math.PI*2*66);
////		childArm2.setRotateSpeed(Math.PI*2*132);
////		childArm3.setRotateSpeed(Math.PI*2*33);
//
////		arm.setRotateSpeed(Math.PI*2*3);
////		childArm.setRotateSpeed(Math.PI*2*39);
////		childArm2.setRotateSpeed(Math.PI*2*99);
////		childArm3.setRotateSpeed(Math.PI*2*39);
//
////		arm.setRotateSpeed(Math.PI*2*59);
////		childArm.setRotateSpeed(Math.PI*2*39);
////		childArm2.setRotateSpeed(Math.PI*2*99);
////		childArm3.setRotateSpeed(Math.PI*2*39);
//
//		arm.setRotateSpeed(Math.PI*2*59);
//		childArm.setRotateSpeed(Math.PI*2*39);
//		childArm2.setRotateSpeed(Math.PI*2*99);
//		childArm3.setRotateSpeed(Math.PI*2*39);
//
//	}

	public void init(){
		arm = new Arm();
		arm.setLength(30);

		Arm arm2 = makeChildArmFromParentArm(arm);
		Arm arm3 = makeChildArmFromParentArm(arm2);
		Arm arm4 = makeChildArmFromParentArm(arm3);
		Arm arm5 = makeChildArmFromParentArm(arm4);
		Arm arm6 = makeChildArmFromParentArm(arm5);
		Arm arm7 = makeChildArmFromParentArm(arm6);
//
 		arm.setRotateSpeed(Math.PI*2 * 0.2);
 		arm2.setRotateSpeed(Math.PI*2 * 33);
 		arm3.setRotateSpeed(Math.PI*2 * 32);
 		arm4.setRotateSpeed(Math.PI*2 * 33);
 		arm5.setRotateSpeed(Math.PI*2 * 132);
 		arm6.setRotateSpeed(Math.PI*2 * 33);
 		arm7.setRotateSpeed(Math.PI*2 * 33);

//		arm.setRotateSpeed(Math.PI*2 * 0.2);
//		arm2.setRotateSpeed(Math.PI*2 * 33);
//		arm3.setRotateSpeed(Math.PI*2 * 32);
//		arm4.setRotateSpeed(Math.PI*2 * 33);
//		arm5.setRotateSpeed(Math.PI*2 * 32);
//		arm6.setRotateSpeed(Math.PI*2 * 33);
//		arm7.setRotateSpeed(Math.PI*2 * 33);

//		arm.setRotateSpeed(Math.PI*2 * 1);
//		arm2.setRotateSpeed(Math.PI*2 * 2);
//		arm3.setRotateSpeed(Math.PI*2 * 3);
//		arm4.setRotateSpeed(Math.PI*2 * 4);
//		arm5.setRotateSpeed(Math.PI*2 * 5);
//		arm6.setRotateSpeed(Math.PI*2 * 6);
//		arm7.setRotateSpeed(Math.PI*2 * 7);

//		arm.setRotateSpeed(Math.PI*2 * 600);
//		arm2.setRotateSpeed(Math.PI*2 * 7);
//		arm3.setRotateSpeed(Math.PI*2 * 2);
//		arm4.setRotateSpeed(Math.PI*2 * 12);
//		arm5.setRotateSpeed(Math.PI*2 * 2);
//		arm6.setRotateSpeed(Math.PI*2 * 7);
//		arm7.setRotateSpeed(Math.PI*2 * 2);

//		arm.setRotateSpeed(Math.PI*2 * 520);
//		arm2.setRotateSpeed(Math.PI*2 * 2);
//		arm3.setRotateSpeed(Math.PI*2 * 2);
//		arm4.setRotateSpeed(Math.PI*2 * 12);
//		arm5.setRotateSpeed(Math.PI*2 * 2);
//		arm6.setRotateSpeed(Math.PI*2 * 14);
//		arm7.setRotateSpeed(Math.PI*2 * 2);

//		arm.setRotateSpeed(Math.PI*2 * 0);
//		arm2.setRotateSpeed(Math.PI*2 * 0);
//		arm3.setRotateSpeed(Math.PI*2 * 0);
//		arm4.setRotateSpeed(Math.PI*2 * 59);
//		arm5.setRotateSpeed(Math.PI*2 * 39);
//		arm6.setRotateSpeed(Math.PI*2 * 99);
//		arm7.setRotateSpeed(Math.PI*2 * 99);

//		Random rand = new Random();
//		arm.setRotateSpeed(Math.PI*2 * rand.nextInt(100));
//		arm2.setRotateSpeed(Math.PI*2 * rand.nextInt(100));
//		arm3.setRotateSpeed(Math.PI*2 * rand.nextInt(100));
//		arm4.setRotateSpeed(Math.PI*2 * 59);
//		arm5.setRotateSpeed(Math.PI*2 * 39);
//		arm6.setRotateSpeed(Math.PI*2 * 99);
//		arm7.setRotateSpeed(Math.PI*2 * 99);

//		arm.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm2.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm3.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm4.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm5.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm6.setRotateSpeed(Math.PI*2 * 0.5f);
//		arm7.setRotateSpeed(Math.PI*2 * 0.5f);

	}

    public void randomiseSpeeds(){
        Random rand = new Random();
        int maxInt = 500;
		Arm currentArm = arm;
		while (true){
			currentArm.setRotateSpeed(Math.PI*2 * rand.nextInt(maxInt));
			Arm childArm = currentArm.getChildArm();
			if (childArm != null){
				currentArm = childArm;
			}else{
				break;
			}
		}
		System.out.println(this.getClass().getSimpleName()+": randomiseSpeeds");
    }

	protected Arm makeChildArmFromParentArm(Arm arm){
		Arm childArm = new Arm();
		childArm.setLength(arm.getLength());
		arm.setChildArm(childArm);
		childArm.setParentArm(arm);
		return childArm;
	}


	public class Arm{
		Arm parentArm;
		Arm childArm;
		Point2D.Double basePoint;
		double currentAngle;
		float length;
		double rotateSpeed;
		Point2D.Double endPoint;
		public Arm(){
			basePoint = new Point2D.Double();
			currentAngle = 0;
			length = 50;
			rotateSpeed = Math.PI*2;
			endPoint = new Point2D.Double();
			endPoint.x = (float)(basePoint.x + Math.cos(currentAngle)*length);
			endPoint.y = (float)(basePoint.y + Math.sin(currentAngle)*length);
		}
		public void update(double seconds){
			currentAngle += rotateSpeed*seconds;
			endPoint.x = (float)(basePoint.x + Math.cos(currentAngle)*length);
			endPoint.y = (float)(basePoint.y + Math.sin(currentAngle)*length);
			if (childArm != null){
				Point2D.Double childArmBasePoint = childArm.getBasePoint();
				childArmBasePoint.x = endPoint.x;
				childArmBasePoint.y = endPoint.y;
				childArm.update(seconds);
			}
		}

		public Point2D.Double getBasePoint() {
			return basePoint;
		}

		public Arm getChildArm() {
			return childArm;
		}

		public double getCurrentAngle() {
			return currentAngle;
		}

		public float getLength() {
			return length;
		}

		public Arm getParentArm() {
			return parentArm;
		}

		public double getRotateSpeed() {
			return rotateSpeed;
		}

		public void setChildArm(Arm childArm) {
			this.childArm = childArm;
		}

		public void setCurrentAngle(float currentAngle) {
			this.currentAngle = currentAngle;
		}

		public void setLength(float length) {
			this.length = length;
		}

		public void setParentArm(Arm parentArm) {
			this.parentArm = parentArm;
		}

		public void setRotateSpeed(double rotateSpeed) {
			this.rotateSpeed = rotateSpeed;
		}

		public Point2D.Double getEndPoint() {
			return endPoint;
		}


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
				AWTEvent awtEvent = (AWTEvent)eventsCopy.get(i);
				if (awtEvent instanceof MouseEvent){
					MouseEvent e = (MouseEvent)awtEvent;
					if (e.getID() == MouseEvent.MOUSE_MOVED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
						if (draw == false){
							Point2D.Double p = arm.getBasePoint();
							p.x = lastMouseMovePoint.x;
							p.y = lastMouseMovePoint.y;
						}
					}
					if (e.getID() == MouseEvent.MOUSE_PRESSED){
						lastMouseMovePoint.x = e.getX();
						lastMouseMovePoint.y = e.getY();
						if (e.getButton() == MouseEvent.BUTTON3){
							img = null;
							draw = false;
							continue;
						}
						if (draw == true){
							draw = false;
						}else{
							draw = true;
						}
						if (draw == false){
							Point2D.Double p = arm.getBasePoint();
							p.x = lastMouseMovePoint.x;
							p.y = lastMouseMovePoint.y;
						}
					}
				}else if (awtEvent instanceof KeyEvent){
					KeyEvent e = (KeyEvent)awtEvent;
					if (e.getID() == KeyEvent.KEY_PRESSED){
						if (e.getKeyCode() == KeyEvent.VK_R){
							randomiseSpeeds();
						}
					}
                }else if (awtEvent instanceof ComponentEvent){
					ComponentEvent e = (ComponentEvent)awtEvent;
					if (e.getID() == ComponentEvent.COMPONENT_RESIZED){

					}
				}
			}
			eventsCopy.clear();
		}
		doMaxUpdate(seconds);
	}
	protected void doMaxUpdate(double seconds){
		double maxUpdateSeconds = 0.0001;
		while (seconds > maxUpdateSeconds){
			arm.update(maxUpdateSeconds);
			paintImage();
			seconds -= maxUpdateSeconds;
		}
		arm.update(seconds);
		paintImage();
	}

	BufferedImage img;
	boolean lastChildArmEndPointSet = false;
	double lastChildArmEndPointX;
	double lastChildArmEndPointY;
	protected void paintImage(){
		if (img == null || view.getWidth() != img.getWidth() || view.getHeight() != img.getHeight()) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
			BufferedImage newImg = gc.createCompatibleImage(view.getWidth(), view.getHeight(), Transparency.TRANSLUCENT);
			Graphics2D imgG = (Graphics2D)newImg.getGraphics();
			imgG.setColor(new Color(0,0,0,0));
			imgG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT));
			imgG.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());
			if (img == null){
				img = newImg;
			}else{
				imgG.drawImage(img, null, null);
				img = newImg;
			}
		}
		Arm lastChildArm = arm;
		while (true){
			Arm childArm = lastChildArm.getChildArm();
			if (childArm != null){
				lastChildArm = childArm;
			}else{
				break;
			}
		}
		if (lastChildArmEndPointSet == false){
			lastChildArmEndPointX = lastChildArm.getEndPoint().x;
			lastChildArmEndPointY = lastChildArm.getEndPoint().y;
			lastChildArmEndPointSet = true;
		}

		float r = (float)(lastChildArm.getCurrentAngle()*lastChildArm.getCurrentAngle()/(Math.PI*2));
		float g = (float)(lastChildArm.getParentArm().getCurrentAngle()/(Math.PI*2));
		float b = (float)(lastChildArm.getParentArm().getParentArm().getCurrentAngle()/(Math.PI*2));
		if (r < 0f){
			r *= -1f;
		}
		if (g < 0f){
			g *= -1f;
		}
		if (b < 0f){
			b *= -1f;
		}
		while (r > 1f){
			r /= 2f;
		}
		while (g > 1f){
			g /= 2f;
		}
		while (b > 1f){
			b /= 2f;
		}


		if (img != null && draw){
			Graphics2D imgG = (Graphics2D)img.getGraphics();
			imgG.setColor(new Color(r, g, b));
//			imgG.setColor(Color.BLACK);
			imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			imgG.draw(new Line2D.Double(lastChildArmEndPointX, lastChildArmEndPointY, lastChildArm.getEndPoint().x, lastChildArm.getEndPoint().y));
		}
		lastChildArmEndPointX = lastChildArm.getEndPoint().x;
		lastChildArmEndPointY = lastChildArm.getEndPoint().y;
	}


	public class ViewPane extends JComponent {
		VolatileImage backImage;
		Graphics2D backImageGraphics2D;

		public ViewPane() {
		}

		protected void renderWorld() {
			Graphics2D g = backImageGraphics2D;

			g.setColor(Color.WHITE);
			//g.setColor(Color.GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.drawImage(img, null, null);

			g.setColor(Color.BLACK);
			Arm currentArm = arm;
			while (true){
				Point2D.Double base = currentArm.getBasePoint();
				Point2D.Double end = currentArm.getEndPoint();
				g.draw(new Line2D.Double(base.x, base.y, end.x, end.y));
				float r = 2;
				g.fill(new Ellipse2D.Double(base.x - r, base.y - r, 2*r, 2*r));
				if (currentArm.getChildArm() != null){
					currentArm = currentArm.getChildArm();
				}else{
					break;
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
		new Drawer();
	}

}
