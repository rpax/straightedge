/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.util.ArrayList;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.OccluderImpl;

/**
 *
 * @author Keith
 */
public class WorldShapes extends World{
	public WorldShapes(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);
		ArrayList<KPolygon> polygons = new ArrayList<KPolygon>();
		// make some rectangles
		for (int i = 0; i < 2; i++){
			KPoint p = new KPoint(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			KPoint p2 = new KPoint(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			float width = 20 + 20*random.nextFloat();
			KPolygon rect = KPolygon.createRectOblique(p, p2, width);
			polygons.add(rect);
		}

		// make some rectangles
		for (int i = 0; i < 2; i++){
			KPoint p = new KPoint(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			KPoint p2 = new KPoint(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			float width = 20 + 20*random.nextFloat();
			KPolygon rect = KPolygon.createRectOblique(p, p2, width);
			polygons.add(rect);
		}
		// make a cross
		polygons.add(KPolygon.createRectOblique(40, 70, 100, 70, 20));
		polygons.add(KPolygon.createRectOblique(70, 40, 70, 100, 20));
		// make stars
		for (int i = 0; i < 2; i++){
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			int numPoints = 4 + random.nextInt(4)*2;
			double angleIncrement = Math.PI*2f/(numPoints*2);
			float rBig = 40 + random.nextFloat()*90;
			float rSmall = 20 + random.nextFloat()*70;
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
			//poly.translate(20 + (float)random.nextFloat()*aabb.getWidth(), 20 + (float)random.nextFloat()*aabb.getHeight());
			KPoint p = new KPoint(innerAABB.p.x + random.nextFloat()*innerAABB.getWidth(), innerAABB.p.y + random.nextFloat()*innerAABB.getHeight());
			poly.translateTo(p);
			polygons.add(poly);
		}
		for (int i = 0; i < polygons.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(polygons.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}
}
