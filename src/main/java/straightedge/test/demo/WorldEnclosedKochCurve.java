/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.util.ArrayList;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

import straightedge.geom.AABB;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.CollinearOverlapChecker;

/**
 * See book 'The Algorithmic Beauty of Plants' (http://algorithmicbotany.org/papers/#abop)
 * @author Keith
 */
public class WorldEnclosedKochCurve extends World{
	public WorldEnclosedKochCurve(Main main){
		super(main);
	}
	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		float contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		float contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);
		Vector2f center = new Vector2f(contW/2f, contH/2f);
		KPolygon screenPoly = KPolygon.createRect(0,0, contW, contH);
		
		ArrayList<KPolygon> finalPolygons = new ArrayList<KPolygon>();
		ArrayList<KPolygon> allPolygons = new ArrayList<KPolygon>();
		// From book 'The Algorithmic Beauty of Plants' (http://algorithmicbotany.org/papers/#abop)
		
		// Enclosed Koch Curve
		String initiator = "F-F-F-F";
		String regex = "F";
		String replacer = "FF-F--F-F";
		int numIterations = 3;
		float dist = 17;
		float width = 4;
		float angleIncrement = FastMath.PI/2f;
		
		String instr = initiator;
		for (int i = 0; i < numIterations; i++){
			instr = instr.replaceAll(regex, replacer);
		}
//		// take off the last forward move to make a hole in the boundary.
//		instr = instr.substring(0, instr.lastIndexOf(regex)); 
		
		float angle = FastMath.PI/2f;	// direction starts facing up.
		Vector2f p = new Vector2f(0,0);
		Vector2f oldP = p.clone();
		for (int i = 0; i < instr.length(); i++){
			char c = instr.charAt(i);
			if (c == ('F') || c == ('L') || c == ('R')){
				p.x += FastMath.cos(angle)*dist;
				p.y += FastMath.sin(angle)*dist;
//				System.out.println(this.getClass().getSimpleName()+": p == "+p);
				allPolygons.add(KPolygon.createRectOblique(p, oldP, width));
				oldP = p.clone();
			}else if (c == ('+')){
				angle += angleIncrement;
			}else if (c == ('-')){
				angle -= angleIncrement;
			}
		}
		
		// Move the polygons into the middle of the screen, 
		// and if there are to many then chop off the excess ones.
		AABB bounds = AABB.getAABBEnclosingCenterAndRadius(allPolygons);
		Vector2f centerBounds = bounds.getCenter();
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
