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
public class WorldVogelSpiral extends World{
	public WorldVogelSpiral(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);

		ArrayList<KPolygon> allPolys = new ArrayList<KPolygon>();

		// vogel spiral (137.5 degrees)
		{
			KPoint centerOfSpiral = new KPoint(contW/2f, contH/2f);
			double maxN = 100;
			double r = 0;
            double angle = 0;
			double angleConstant = Math.toRadians(137.5);
			for (int n = 0; n < maxN; n++){
				angle = n*angleConstant;
				r = 22*Math.sqrt(n);
				double x = r*Math.cos(angle);
				double y = r*Math.sin(angle);
				KPoint center = new KPoint((float)x, (float)y);
				KPolygon poly = KPolygon.createRegularPolygon(12, 8);
				poly.rotate((float)angle);
				poly.translateTo(center);
				poly.translate(centerOfSpiral);
				allPolys.add(poly);
			}
		}
		

		for (int i = 0; i < allPolys.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(allPolys.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}
}
