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
package straightedge.geom.util;

//import java.awt.*;
//import java.awt.geom.*;
//import javax.swing.*;
import java.util.*;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;

/**
 *
 * @author Keith Woodward
 */

public class ConvexHull {

	public static KPolygon calcConvexHullPolygon(ArrayList<KPoint> array) {
		Coordinate[] coordinateArray = new Coordinate[array.size()];
		for (int i = 0; i < array.size(); i++){
			KPoint p = array.get(i);
			coordinateArray[i] = new Coordinate(p.x, p.y);
		}
		com.vividsolutions.jts.algorithm.ConvexHull convexHull = new com.vividsolutions.jts.algorithm.ConvexHull(coordinateArray, new GeometryFactory());
		Geometry geometry = convexHull.getConvexHull();
		if (geometry.getGeometryType() == "Polygon"){
			Polygon jtsPolygon = (Polygon)geometry;
			PolygonConverter polygonConverter = new PolygonConverter();
			KPolygon convexHullKPolygon = polygonConverter.makeKPolygonFromExterior(jtsPolygon);
			return convexHullKPolygon;
		}else{
			System.out.println(": geometry.getGeometryType() == "+geometry.getGeometryType());
			throw new RuntimeException("Unknown JTS geometry type: "+geometry.getGeometryType());
		}
	}


//	/**
//	 * From http://www.dreamincode.net/code/snippet4178.htm
//	 * Caution: this method often causes StackOverflowExceptions
//	 * by Neumann
//	 */
//    //Returns the points of convex hull in the correct order
//    public static ArrayList<KPoint> calcConvexHull(ArrayList<KPoint> array) {
//		Collections.sort(array, new Comparator<KPoint>() {
//			public int compare (KPoint pt1, KPoint pt2) {
//				float r = pt1.x - pt2.x;
//				if (r != 0)
//					return (r > 0 ? 1 : -1);
//				else
//					return (pt1.y - pt2.y >= 0 ? 1 : -1) ;
//			}
//	    });
//		int size = array.size();
//		if (size < 2)
//			return null;
//		KPoint l = array.get(0);
//		KPoint r = array.get(size - 1);
//		ArrayList<KPoint> path = new ArrayList<KPoint>();
//		path.add(l);
//		cHull(array, l, r, path);
//		path.add(r);
//		cHull(array, r, l, path);
//		return path;
//    }
//
//    protected static void cHull(ArrayList<KPoint> points, KPoint l, KPoint r, ArrayList<KPoint> path) {
//		if (points.size() < 3)
//			return;
//		float maxDist = -1;
//		float tmp;
//		KPoint p = null;
//		for (KPoint pt : points) {
//			if (pt != l && pt != r) {
//				tmp = distance(l, r, pt);
//				if (tmp > maxDist) {
//					maxDist = tmp;
//					assert pt != null;
//					p = pt;
//				}
//			}
//		}
//		if (p == null){
//			return;
//		}
//		ArrayList<KPoint> left = new ArrayList<KPoint>();
//		ArrayList<KPoint> right = new ArrayList<KPoint>();
//		left.add(l);
//		right.add(p);
//		for (KPoint pt : points) {
//			if (distance(l, p, pt) > 0)
//				left.add(pt);
//			else if (distance(p, r, pt) > 0)
//				right.add(pt);
//		}
//		left.add(p);
//		right.add(r);
//		cHull(left, l, p, path);
//		path.add(p);
//		cHull(right, p, r, path);
//    }
//
//	//Returns the determinant of the point matrix
//    //This determinant tells how far p3 is from vector p1p2 and on which side it is
//    protected static float distance(KPoint p1, KPoint p2, KPoint p3) {
//		float x1 = p1.x;
//		float x2 = p2.x;
//		float x3 = p3.x;
//		float y1 = p1.y;
//		float y2 = p2.y;
//		float y3 = p3.y;
//		return x1*y2 + x3*y1 + x2*y3 - x3*y2 - x2*y1 - x1*y3;
//    }
//
////
//////The panel that will show the CHull class in action
////class DrawPanel extends JPanel {
////
////    public void paintComponent(Graphics g) {
////	super.paintComponent(g);
////	Graphics2D g2 = (Graphics2D) g;
////	int size = 80;
////	int rad = 4;
////	Random r = new Random();
////	ArrayList<Point> array = new ArrayList<Point>(size);
////	for (int i = 0; i < size; i++) {
////	    int x = r.nextInt(350) + 15;
////	    int y = r.nextInt(350) + 15;
////	    array.add(new Point(x,y));
////	    g2.draw(new Ellipse2D.Double(x-2,y-2,rad,rad));
////	}
////
////	ArrayList<Point> hull = ConvexHull.calcConvexHull(array);
////	Iterator<Point> itr = hull.iterator();
////	Point prev = itr.next();
////	Point curr = null;
////	while (itr.hasNext()) {
////	    curr = itr.next();
////	    g2.drawLine(prev.x, prev.y, curr.x, curr.y);
////	    prev = curr;
////	}
////	curr = hull.get(0);
////	g2.drawLine(prev.x, prev.y, curr.x, curr.y);
////    }
////}
////
////	class CHullExample extends JFrame {
////		public CHullExample() {
////		setSize(400,400);
////		setLocation(100,100);
////		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////		DrawPanel dp = new DrawPanel();
////		Container cp = this.getContentPane();
////		cp.add(dp);
////		}
////
////		public static void main(String[] args) {
////		new CHullExample().setVisible(true);
////    }
}