/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test;

import straightedge.geom.*;
import straightedge.geom.vision.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * A simple demo of field-of-vision calculation.
 *
 * Note that this uses passive rendering, meaning that
 * all rendering is done on Swing's Event Dispatch Thread via the repaint() method.
 * This is different to active rendering where the rendering happens on a
 * separate thread and is more predictable.
 *
 * @author Keith
 */
public class VisionTest {
	JFrame frame;

	VisionFinder visionFinder;
	VisionData visionData = null;
	double smallAmount = 0.0001;

	ArrayList<OccluderImpl> occluders;
	KPoint lastMouseMovePoint = new KPoint(100, 100);

	public VisionTest(){
		// Make the window
		frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Make the occluders
		occluders = new ArrayList<OccluderImpl>();
		float pillarH = 20;
		float pillarW = 5;
		KPoint centerOfSpiral = new KPoint(250, 250);
		int numPillars = 40;
		double angleIncrement = Math.PI*2f/(numPillars);
		float rBig = 150;
		double currentAngle = 0;
		for (int k = 0; k < numPillars; k++){
			double x = rBig*Math.cos(currentAngle);
			double y = rBig*Math.sin(currentAngle);
			KPoint center = new KPoint((float)x, (float)y);
			KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
			poly.rotate((float)currentAngle);
			poly.translateTo(center);
			poly.translate(centerOfSpiral);
			occluders.add(new OccluderImpl(poly));
			currentAngle += angleIncrement;
		}

		// Make the field of vision polygon:
		int numPoints = 20;
		float radius = 250;
		KPolygon boundaryPolygon = KPolygon.createRegularPolygon(numPoints, radius);
		// Make the eye which is like the light-source
		// By making the eye (or light source) slightly offset from (0,0), it will prevent problems caused by collinearity.
		KPoint eye = new KPoint(smallAmount, smallAmount);
		// The VisionData just contains the eye and boundary polygon,
		// and also the results of the VisionFinder calculations.
		visionData = new VisionData(eye, boundaryPolygon);
		visionFinder = new VisionFinder();

		JComponent renderComponent = new JComponent(){
			public void paint(Graphics graphics){
				// Move the eye and boundaryPolygon to wherever they need to be.
				// By making the eye slightly offset from its integer coordinate by smallAmount,
				// it will prevent problems caused by collinearity.
				visionData.eye.setCoords(lastMouseMovePoint.x + smallAmount, lastMouseMovePoint.y + smallAmount);
				visionData.boundaryPolygon.translateTo(visionData.eye);
				visionFinder.calc(visionData, occluders);
				/* Note that the above line is the slow way to calculate the visiblePolygon since the
				 * intersection points of the stationary occluders will have to
				 * be recalculated every single time calc is called.
				 * See VisionTestActiveRendering for an example of how to do it more efficiently.
				 */

				Graphics2D g = (Graphics2D)graphics;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				float backGroundGrey = 77f/255f;
				g.setColor(new Color(backGroundGrey, backGroundGrey, backGroundGrey));
				g.fillRect(0, 0, getWidth(), getHeight());

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				float g4 = 0.1f;
				g.setColor(new Color(g4, g4, g4));
				for (int i = 0; i < occluders.size(); i++) {
					g.fill(occluders.get(i).getPolygon());
				}

				if (visionData.visiblePolygon != null){
					Point2D.Double center = new Point2D.Double(visionData.eye.x, visionData.eye.y);
					float[] dist = {0.0f, 1.0f};
					float a = 0.9f;
					float c = backGroundGrey;
					Color[] colors = {new Color(a, a, a, 0.7f), new Color(c, c, c, 0.0f)};
					RadialGradientPaint paint = new RadialGradientPaint(center, (float)visionData.maxEyeToBoundaryPolygonPointDist, dist, colors);//, CycleMethod.REFLECT);
					g.setPaint(paint);
					g.fill(visionData.visiblePolygon);
				}

				g.setColor(Color.RED);
				float r = 1f;
				g.fill(new Ellipse2D.Double(lastMouseMovePoint.x - r, lastMouseMovePoint.y - r, 2*r, 2*r));
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
		Thread thread = new Thread(){
			public void run(){
				while(true){
					frame.getContentPane().repaint();
					try{Thread.sleep(10);}catch(Exception e){}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}



	public static void main(String[] args){
		new VisionTest();
	}
}
