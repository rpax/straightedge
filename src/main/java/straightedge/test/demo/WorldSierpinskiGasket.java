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
 * See book 'The Algorithmic Beauty of Plants' (http://algorithmicbotany.org/papers/#abop)
 * @author Keith
 */
public class WorldSierpinskiGasket extends World{
	public WorldSierpinskiGasket(Main main){
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
		// From book 'The Algorithmic Beauty of Plants' (http://algorithmicbotany.org/papers/#abop)
		
		// Sierpinski Gasket
		String initiator = "R";
		char regex = 'L';
		String replacer = "R+L+R";
		char regex2 = 'R';
		String replacer2 = "L-R-L";
		int numIterations = 5;
		double dist = 14;
		double width = 5;
		double angleIncrement = Math.PI/3f;
		
		
		StringBuilder instrBuf = new StringBuilder(initiator);
		for (int i = 0; i < numIterations; i++){
			for (int j = 0; j < instrBuf.length(); j++){
				if (instrBuf.charAt(j) == regex){
					instrBuf.replace(j,j+1,replacer);
					j += replacer.length()-1;
				}else if (instrBuf.charAt(j) == regex2){
					instrBuf.replace(j,j+1,replacer2);
					j += replacer2.length()-1;
				}
			}
		}
		String instr = instrBuf.toString();
		
		
		
		double angle = Math.PI/2f;	// direction starts facing up.
		KPoint p = new KPoint(0,0);
		KPoint oldP = p.copy();
		for (int i = 0; i < instr.length(); i++){
			char c = instr.charAt(i);
			if (c == ('F') || c == ('L') || c == ('R')){
				p.x += Math.cos(angle)*dist;
				p.y += Math.sin(angle)*dist;
//				System.out.println(this.getClass().getSimpleName()+": p == "+p);
				allPolygons.add(KPolygon.createRectOblique(p, oldP, width));
				oldP = p.copy();
			}else if (c == ('+')){
				angle += angleIncrement;
			}else if (c == ('-')){
				angle -= angleIncrement;
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
