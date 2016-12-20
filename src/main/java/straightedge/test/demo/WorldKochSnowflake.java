/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import straightedge.geom.AABB;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.CollinearOverlapChecker;

/**
 *
 * @author Keith
 */
public class WorldKochSnowflake extends World{
	public WorldKochSnowflake(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);
		KPoint center = new KPoint(contW/2f, contH/2f);
		KPolygon screenPoly = KPolygon.createRect(0,0, contW, contH);
		
		ArrayList<KPolygon> finalPolygons = new ArrayList<KPolygon>();
		ArrayList<KPolygon> allPolygons = new ArrayList<KPolygon>();
		// make 3 snowflakes, the second bigger than the first.
		for (int h = 0; h < 3; h++){
			// snow flake
			String initiator = "F--F--F";
			String regex = "F";
			String replacer = "F+F--F+F";
			int numIterations = 1+h;
			double dist = 15;
			double width = 4;
			double angleIncrement = Math.PI/3f;

			String instr = initiator;
			for (int i = 0; i < numIterations; i++){
				instr = instr.replaceAll(regex, replacer);
			}
			// take off the last forward move to make a hole in the boundary.
			instr = instr.substring(0, instr.lastIndexOf(regex)); 

			double angle = Math.PI/2f;	// direction starts facing up.
			KPoint p = new KPoint(0,0);
			KPoint oldP = p.copy();
			ArrayList<KPolygon> subsetPolygons = new ArrayList<KPolygon>();
			for (int i = 0; i < instr.length(); i++){
				char c = instr.charAt(i);
				if (c == ('F') || c == ('L') || c == ('R')){
					p.x += Math.cos(angle)*dist;
					p.y += Math.sin(angle)*dist;
					subsetPolygons.add(KPolygon.createRectOblique(p, oldP, width));
					oldP = p.copy();
				}else if (c == ('+')){
					angle += angleIncrement;
				}else if (c == ('-')){
					angle -= angleIncrement;
				}
			}
			// move this bunch of polygons to the middle of the screen
			AABB bounds = AABB.getAABBEnclosingCenterAndRadius(subsetPolygons);
			KPoint centerBounds = bounds.getCenter();
			for (int i = 0; i < subsetPolygons.size(); i++){
				KPolygon poly = subsetPolygons.get(i);
				poly.translate(center.x-centerBounds.x, center.y-centerBounds.y);
				allPolygons.add(poly);
			}
		}
		
		// Move the polygons into the middle of the screen, 
		// and if there are to many then chop off the excess ones.
		AABB bounds = AABB.getAABBEnclosingCenterAndRadius(allPolygons);
		KPoint centerBounds = bounds.getCenter();
		for (int i = 0; i < allPolygons.size(); i++){
			KPolygon poly = allPolygons.get(i);
			poly.translate(center.x-centerBounds.x, center.y-centerBounds.y);
			if (screenPoly.contains(poly)){
				finalPolygons.add(poly);
			}
		}
		
		System.out.println(this.getClass().getSimpleName()+": finalPolygons.size() == "+finalPolygons.size());
		
		CollinearOverlapChecker coc = new CollinearOverlapChecker();
		coc.fixCollinearOverlaps(finalPolygons);
		
		for (int i = 0; i < finalPolygons.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(finalPolygons.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}
}
