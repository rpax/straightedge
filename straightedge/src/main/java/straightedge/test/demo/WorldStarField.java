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
public class WorldStarField extends World{
	public WorldStarField(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);


		ArrayList<KPolygon> allPolys = new ArrayList<KPolygon>();
		for (int i = 0; i < 5; i++){
			for (int j = 0; j < 5; j++){
				ArrayList<KPoint> pointList = new ArrayList<KPoint>();
				pointList.add(new KPoint(0, 0));
				pointList.add(new KPoint(15, 5));
				pointList.add(new KPoint(30, 0));
				pointList.add(new KPoint(30, 30));
				pointList.add(new KPoint(0, 30));
				KPolygon poly = new KPolygon(pointList);
				assert poly.isCounterClockWise();
				poly.translate(60 + 45*i, 50 + 45*j);
				//poly.rotate(i+j);
				allPolys.add(poly);
			}
		}

		for (int i = 0; i < 5; i++){
			for (int j = 0; j < 5; j++){
				ArrayList<KPoint> pointList = new ArrayList<KPoint>();
				int numPoints = 6;
				double angleIncrement = Math.PI*2f/(numPoints*2);
				float rBig = 22;
				float rSmall = 8;
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
				poly.translate(300 + 45*i, 60 + 45*j);
				allPolys.add(poly);
			}
		}

		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			pointList.add(new KPoint(5, 25));
			pointList.add(new KPoint(25, 25));
			pointList.add(new KPoint(25, 5));
			pointList.add(new KPoint(5, 0));
			pointList.add(new KPoint(30, 0));
			pointList.add(new KPoint(30, 30));
			pointList.add(new KPoint(0, 30));
			pointList.add(new KPoint(0, 0));
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			poly.translate(100, 350);
			poly.scale(3);
			allPolys.add(poly);
		}
		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();

			pointList.add(new KPoint(5, 25));
			pointList.add(new KPoint(25, 25));
			pointList.add(new KPoint(25, 5));
			pointList.add(new KPoint(5, 0));
			pointList.add(new KPoint(30, 0));
			pointList.add(new KPoint(30, 30));
			pointList.add(new KPoint(0, 30));
			pointList.add(new KPoint(0, 25));
			pointList.add(new KPoint(10, 5));
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			poly.translate(100, 550);
			poly.scale(3);
			allPolys.add(poly);
		}
		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			pointList.add(new KPoint(0, 0));
			pointList.add(new KPoint(15, 5));
			pointList.add(new KPoint(30, 0));
			pointList.add(new KPoint(30, 30));
			pointList.add(new KPoint(0, 30));
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			poly.scale(3);
			poly.translate(200, 350);
			allPolys.add(poly);
		}
		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			pointList.add(new KPoint(0, 0));
			pointList.add(new KPoint(15, 5));
			pointList.add(new KPoint(30, 0));
			pointList.add(new KPoint(30, 30));
			pointList.add(new KPoint(0, 30));
			KPolygon poly = new KPolygon(pointList);
			assert poly.isCounterClockWise();
			poly.scale(3);
			poly.rotate(1);
			poly.translate(270, 350);
			allPolys.add(poly);
		}
		{
			ArrayList<KPoint> pointList = new ArrayList<KPoint>();
			int numPoints = 6;
			double angleIncrement = Math.PI*2f/(numPoints*2);
			float rBig = 22;
			float rSmall = 8;
			double currentAngle = 0;
			for (int i = 0; i < numPoints; i++){
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
			poly.scale(3.5f);
			poly.rotate(2.5f);
			poly.translate(400, 350);
			allPolys.add(poly);
		}
		for (int i = 0; i < allPolys.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(allPolys.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}
}
