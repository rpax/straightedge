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

/**
 *
 * @author Keith
 */
public class WorldStoneHenge extends World{
	public WorldStoneHenge(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);

		ArrayList<KPolygon> allPolys = new ArrayList<KPolygon>();
		// circle
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint centerOfSpiral = new KPoint(200, 250);
			int numPoints = 40;//250;
			double angleIncrement = Math.PI*2f/(numPoints);
			float rBig = 100;
			double currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				double x = rBig*Math.cos(currentAngle);
				double y = rBig*Math.sin(currentAngle);
				KPoint center = new KPoint((float)x, (float)y);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)currentAngle);
				poly.translateTo(center);
				poly.translate(centerOfSpiral);
				allPolys.add(poly);
				currentAngle += angleIncrement;
			}
		}

		// circle
		{
			float pillarH = 5;
			float pillarW = 1;
			KPoint centerOfSpiral = new KPoint(400, 350);
			int numPoints = 60;//200;
			double angleIncrement = Math.PI*2f/(numPoints);
			float rBig = 50;
			double currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				double x = rBig*Math.cos(currentAngle);
				double y = rBig*Math.sin(currentAngle);
				KPoint center = new KPoint((float)x, (float)y);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)currentAngle);
				poly.translateTo(center);
				poly.translate(centerOfSpiral);
				allPolys.add(poly);
				currentAngle += angleIncrement;
			}
		}

		// wall
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint wallStart = new KPoint(50, 50);
			KPoint wallEnd = new KPoint(550, 50);
			float wallGap = 15;
			float currentDist = 0;
			while(currentDist < wallStart.distance(wallEnd)){
				KPoint center = wallStart.createPointToward(wallEnd, currentDist);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)(wallStart.findAngle(wallEnd)+Math.PI/2f));
				poly.translateTo(center);
				allPolys.add(poly);
				currentDist += wallGap;
			}
		}

		// wall
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint wallStart = new KPoint(50, 80);
			KPoint wallEnd = new KPoint(550, 80);
			float wallGap = 15;
			float currentDist = 0;
			while(currentDist < wallStart.distance(wallEnd)){
				KPoint center = wallStart.createPointToward(wallEnd, currentDist);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)(wallStart.findAngle(wallEnd)+Math.PI/2f));
				poly.translateTo(center);
				allPolys.add(poly);
				currentDist += wallGap;
			}
		}
		for (int i = 0; i < allPolys.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(allPolys.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}
}
