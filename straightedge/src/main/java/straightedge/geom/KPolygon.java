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
package straightedge.geom;

import straightedge.geom.util.Tracker;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A cool polygon class that's got some pretty useful geometry methods.
 * Can be drawn and filled by Java2D's java.awt.Graphics object.
 * Note that the polygon can be convex or concave but it should not have intersecting sides.
 *
 * Some code is from here:
 * http://www.cs.princeton.edu/introcs/35purple/Polygon.java.html
 *
 * and
 * Joseph O'Rourke:
 * http://exaflop.org/docs/cgafaq/cga2.html
 *
 * Another good source
 * Paul Bourke:
 * http://local.wasp.uwa.edu.au/~pbourke/geometry/
 *
 * @author Keith Woodward
 */
public class KPolygon implements PolygonHolder, Shape{
	public ArrayList<KPoint> points;
	public KPoint center;
	public double area;
	public double radius;
	public double radiusSq;
	public boolean counterClockWise;
	
	public Object userObject;
	// for use with straightedge.geom.util.Tracker and straightedge.geom.util.TileArray
	public int trackerID = -1;
	public long trackerCounter = -1;
	public boolean trackerAddedStatus = false;

	public KPolygon(){
	}

	public KPolygon(ArrayList<KPoint> pointsList, boolean copyPoints){
		if (pointsList.size() < 3){
			throw new RuntimeException("Minimum of 3 points needed. pointsList.size() == "+pointsList.size());
		}
		this.points = new ArrayList<KPoint>(pointsList.size());
		for (int i = 0; i < pointsList.size(); i++){
			KPoint existingPoint = pointsList.get(i);
			if (copyPoints){
				points.add(new KPoint(existingPoint));
			}else{
				points.add(existingPoint);
			}
		}
		calcAll();
	}
	public KPolygon(ArrayList<KPoint> pointsList){
		this(pointsList, true);
	}

	public KPolygon(KPoint[] pointsArray, boolean copyPoints){
		if (pointsArray.length < 3){
			throw new RuntimeException("Minimum of 3 points needed. pointsArray.length == "+pointsArray.length);
		}
		this.points = new ArrayList<KPoint>(pointsArray.length);
		for (int i = 0; i < pointsArray.length; i++){
			KPoint existingPoint = pointsArray[i];
			if (copyPoints){
				points.add(new KPoint(existingPoint));
			}else{
				points.add(existingPoint);
			}
		}
		calcAll();
	}
	public KPolygon(KPoint... pointsArray){
		this(pointsArray, true);
	}

	public KPolygon(KPolygon polygon){
		points = new ArrayList<KPoint>(polygon.getPoints().size());
		for (int i = 0; i < polygon.getPoints().size() ;i++){
			KPoint existingPoint = polygon.getPoints().get(i);
			points.add(new KPoint(existingPoint));
		}
		area = polygon.getArea();
		counterClockWise = polygon.isCounterClockWise();
		radius = polygon.getRadius();
		radiusSq = polygon.getRadiusSq();
		center = new KPoint(polygon.getCenter());
	}

	public static KPolygon createRect(double x, double y, double x2, double y2){
		// make x and y the bottom left point.
		if (x2 < x) {
			double t = x;
			x = x2;
			x2 = t;
		}
		// make x2 and y2 the top right point.
		if (y2 < y) {
			double t = y;
			y = y2;
			y2 = t;
		}
		ArrayList<KPoint> pointList = new ArrayList<KPoint>();
		pointList.add(new KPoint(x, y));
		pointList.add(new KPoint(x2, y));
		pointList.add(new KPoint(x2, y2));
		pointList.add(new KPoint(x, y2));
		return new KPolygon(pointList, false);
	}
	public static KPolygon createRect(AABB aabb){
		ArrayList<KPoint> pointList = new ArrayList<KPoint>();
		pointList.add(new KPoint(aabb.p.x,  aabb.p.y));
		pointList.add(new KPoint(aabb.p2.x, aabb.p.y));
		pointList.add(new KPoint(aabb.p2.x, aabb.p2.y));
		pointList.add(new KPoint(aabb.p.x,  aabb.p2.y));
		return new KPolygon(pointList, false);
	}
	public static KPolygon createRect(KPoint p, KPoint p2){
		return createRect(p.x, p.y, p2.x, p2.y);
	}
	public static KPolygon createRect(KPoint botLeftPoint, double width, double height){
		return createRect(botLeftPoint.x, botLeftPoint.y, botLeftPoint.x + width, botLeftPoint.y + height);
	}

	public static KPolygon createRectOblique(double x, double y, double x2, double y2, double width){
		ArrayList<KPoint> pointList = new ArrayList<KPoint>();
		double r = width/2f;
		double xOffset = 0;
		double yOffset = 0;
		double xDiff = x2 - x;
		double yDiff = y2 - y;
		if (xDiff == 0){
			xOffset = r;
			yOffset = 0;
		}else if (yDiff == 0){
			xOffset = 0;
			yOffset = r;
		}else{
			double gradient = (yDiff)/(xDiff);
			xOffset = (r*gradient/(Math.sqrt(1 + gradient*gradient)));
			yOffset = -xOffset/gradient;
		}
		//System.out.println(this.getClass().getSimpleName() + ": xOffset == "+xOffset+", yOffset == "+yOffset);
		pointList.add(new KPoint(x-xOffset, y-yOffset));
		pointList.add(new KPoint(x+xOffset, y+yOffset));
		pointList.add(new KPoint(x2+xOffset, y2+yOffset));
		pointList.add(new KPoint(x2-xOffset, y2-yOffset));
		return new KPolygon(pointList, false);
	}
	public static KPolygon createRectOblique(KPoint p1, KPoint p2, double width){
		return createRectOblique(p1.x, p1.y, p2.x, p2.y, width);
	}

	public static KPolygon createRegularPolygon(int numPoints, double distFromCenterToPoints){
		if (numPoints < 3){
			throw new IllegalArgumentException("numPoints must be 3 or more, it can not be "+numPoints+".");
		}
		ArrayList<KPoint> pointList = new ArrayList<KPoint>();
		double angleIncrement = Math.PI*2f/(numPoints);
		double radius = distFromCenterToPoints;
		double currentAngle = 0;
		for (int k = 0; k < numPoints; k++){
			double x = radius*Math.cos(currentAngle);
			double y = radius*Math.sin(currentAngle);
			pointList.add(new KPoint(x, y));
			currentAngle += angleIncrement;
		}
		KPolygon createdPolygon = new KPolygon(pointList, false);
		return createdPolygon;
	}

	public ArrayList<KPoint> getPoints() {
		return points;
	}

	// Gives point of intersection with line specified, where intersectoin point
	// returned is the one closest to (x1, y1).
	// null is returned if there is no intersection.
	public KPoint getClosestIntersectionToFirstFromSecond(double x1, double y1, double x2, double y2){
		KPoint closestIntersectionPoint = null;
		double closestIntersectionDistanceSq = Double.MAX_VALUE;
		int nextI;
		for (int i = 0; i < points.size(); i++){
			nextI = (i+1 == points.size() ? 0 : i+1);
			if (KPoint.linesIntersect(x1,y1,x2,y2,points.get(i).x,points.get(i).y,points.get(nextI).x,points.get(nextI).y)){
				KPoint currentIntersectionPoint = KPoint.getLineLineIntersection(x1,y1,x2,y2,points.get(i).x,points.get(i).y,points.get(nextI).x,points.get(nextI).y);
				if (currentIntersectionPoint == null){
					continue;
				}
				double currentIntersectionDistanceSq = currentIntersectionPoint.distanceSq(x1, y1);
				if (currentIntersectionDistanceSq < closestIntersectionDistanceSq){
					closestIntersectionPoint = currentIntersectionPoint;
					closestIntersectionDistanceSq = currentIntersectionDistanceSq;
				}
			}
		}
		return closestIntersectionPoint;
	}
	public KPoint getClosestIntersectionToFirstFromSecond(KPoint first, KPoint second){
		return getClosestIntersectionToFirstFromSecond(first.x, first.y, second.x, second.y);
	}

	public KPoint getBoundaryPointClosestTo(KPoint p){
		return getBoundaryPointClosestTo(p.x, p.y);
	}
	public KPoint getBoundaryPointClosestTo(double x, double y){
		double closestDistanceSq = Double.MAX_VALUE;
		int closestIndex = -1;
		int closestNextIndex = -1;

		int nextI;
		for (int i = 0; i < points.size(); i++){
			nextI = (i+1 == points.size() ? 0 : i+1);
			KPoint p = this.getPoints().get(i);
			KPoint pNext = this.getPoints().get(nextI);
			double ptSegDistSq = KPoint.ptSegDistSq(p.x, p.y, pNext.x, pNext.y, x, y);
			if (ptSegDistSq < closestDistanceSq){
				closestDistanceSq = ptSegDistSq;
				closestIndex = i;
				closestNextIndex = nextI;
			}
		}
		KPoint p = this.getPoints().get(closestIndex);
		KPoint pNext = this.getPoints().get(closestNextIndex);
		return KPoint.getClosestPointOnSegment(p.x, p.y, pNext.x, pNext.y, x, y);
	}


	public boolean contains(KPolygon foreign){
		if (intersectsPerimeter(foreign)){
			return false;
		}
		if (contains(foreign.getPoints().get(0)) == false){
			return false;
		}
		return true;
	}
	
	public boolean contains(KPoint p){
		return contains(p.x, p.y);
	}

    // Source code from: http://exaflop.org/docs/cgafaq/cga2.html
	//Subject 2.03: How do I find if a point lies within a polygon?
	//The definitive reference is "Point in Polyon Strategies" by Eric Haines [Gems IV] pp. 24-46. The code in the Sedgewick book Algorithms (2nd Edition, p.354) is incorrect.
	//The essence of the ray-crossing method is as follows. Think of standing inside a field with a fence representing the polygon. Then walk north. If you have to jump the fence you know you are now outside the poly. If you have to cross again you know you are now inside again; i.e., if you were inside the field to start with, the total number of fence jumps you would make will be odd, whereas if you were ouside the jumps will be even.
	//The code below is from Wm. Randolph Franklin <wrf@ecse.rpi.edu> with some minor modifications for speed. It returns 1 for strictly interior points, 0 for strictly exterior, and 0 or 1 for points on the boundary. The boundary behavior is complex but determined; | in particular, for a partition of a region into polygons, each point | is "in" exactly one polygon. See the references below for more detail
	//The code may be further accelerated, at some loss in clarity, by avoiding the central computation when the inequality can be deduced, and by replacing the division by a multiplication for those processors with slow divides.
	//References:
	//[Gems IV] pp. 24-46
	//[O'Rourke] pp. 233-238
	//[Glassner:RayTracing]
	public boolean contains(double x, double y) {
		KPoint pointIBefore = (points.size() != 0 ? points.get(points.size() - 1) : null);
		int crossings = 0;
		for (int i = 0; i < points.size(); i++) {
			KPoint pointI = points.get(i);
			if (((pointIBefore.y <= y && y < pointI.y)
					|| (pointI.y <= y && y < pointIBefore.y))
					&& x < ((pointI.x - pointIBefore.x)/(pointI.y - pointIBefore.y)*(y - pointIBefore.y) + pointIBefore.x)) {
				crossings++;
			}
			pointIBefore = pointI;
		}
		return (crossings % 2 != 0);
	}

	public KPoint getPoint(int i){
		return getPoints().get(i);
	}

	public void calcArea(){
		double signedArea = getAndCalcSignedArea();
		if (signedArea < 0){
			counterClockWise = false;
		}else{
			counterClockWise = true;
		}
		area = Math.abs(signedArea);
	}
	public double getAndCalcSignedArea(){
		double totalArea = 0;
		for (int i = 0; i < points.size() - 1; i++) {
			totalArea += ((points.get(i).x - points.get(i+1).x)*(points.get(i+1).y + (points.get(i).y - points.get(i+1).y)/2));
		}
		// need to do points[point.length-1] and points[0].
		totalArea += ((points.get(points.size()-1).x - points.get(0).x)*(points.get(0).y + (points.get(points.size()-1).y - points.get(0).y)/2));
		return totalArea;
	}

	public double[] getBoundsArray(){
		return getBoundsArray(new double[4]);
	}
	public double[] getBoundsArray(double[] bounds){
		double leftX = Double.MAX_VALUE;
		double botY = Double.MAX_VALUE;
		double rightX = -Double.MAX_VALUE;
		double topY = -Double.MAX_VALUE;

		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).x < leftX) {
				leftX = points.get(i).x;
			}
			if (points.get(i).x > rightX) {
				rightX = points.get(i).x;
			}
			if (points.get(i).y < botY) {
				botY = points.get(i).y;
			}
			if (points.get(i).y > topY) {
				topY = points.get(i).y;
			}
		}
		bounds[0] = leftX;
		bounds[1] = botY;
		bounds[2] = rightX;
		bounds[3] = topY;
		return bounds;
	}

	public AABB getAABB(){
		double leftX = Double.MAX_VALUE;
		double botY = Double.MAX_VALUE;
		double rightX = -Double.MAX_VALUE;
		double topY = -Double.MAX_VALUE;

		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).x < leftX) {
				leftX = points.get(i).x;
			}
			if (points.get(i).x > rightX) {
				rightX = points.get(i).x;
			}
			if (points.get(i).y < botY) {
				botY = points.get(i).y;
			}
			if (points.get(i).y > topY) {
				topY = points.get(i).y;
			}
		}
		AABB aabb = new AABB(leftX, botY, rightX, topY);
		return aabb;
	}

	public boolean intersectsPerimeter(KPolygon foreign){
		KPoint pointIBefore = (points.size() != 0 ? points.get(points.size()-1) : null);
		KPoint pointJBefore = (foreign.points.size() != 0 ? foreign.points.get(foreign.points.size()-1) : null);
		for (int i = 0; i < points.size(); i++){
			KPoint pointI = points.get(i);
			//int nextI = (i+1 >= points.size() ? 0 : i+1);
			for (int j = 0; j < foreign.points.size(); j++){
				//int nextJ = (j+1 >= foreign.points.size() ? 0 : j+1);
				KPoint pointJ = foreign.points.get(j);
				//if (KPoint.linesIntersect(points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y, foreign.points.get(j).x, foreign.points.get(j).y, foreign.points.get(nextJ).x, foreign.points.get(nextJ).y)){
				// The below linesIntersect could be sped up slightly since many things are recalc'ed over and over again.
				if (KPoint.linesIntersect(pointI, pointIBefore, pointJ, pointJBefore)){
					return true;
				}
				pointJBefore = pointJ;
			}
			pointIBefore = pointI;
		}
		return false;
	}

	public boolean intersects(KPolygon foreign){
		if (intersectsPerimeter(foreign)){
			return true;
		}
		if (contains(foreign.getPoint(0)) || foreign.contains(getPoint(0))){
			return true;
		}
		return false;
	}
	public boolean intersectionPossible(KPolygon poly){
		return intersectionPossible(this, poly);
	}
	public static boolean intersectionPossible(KPolygon poly, KPolygon poly2){
		double sumRadiusSq = poly.getRadius() + poly2.getRadius();
		sumRadiusSq *= sumRadiusSq;
		if (poly.getCenter().distanceSq(poly2.getCenter()) > sumRadiusSq){
		//if (center.distance(foreign.getCenter()) > radius + foreign.getRadius()){
			return false;
		}
		return true;
	}
	public boolean intersectionPossible(KPoint p1, KPoint p2){
		return intersectionPossible(p1.x, p1.y, p2.x, p2.y);
	}
	public boolean intersectionPossible(double x1, double y1, double x2, double y2){
		if (center.ptSegDistSq(x1, y1, x2, y2) > radiusSq){
			return false;
		}
		return true;
	}
	
	public boolean intersectsLine(KPoint p1, KPoint p2){
		return intersectsLine(p1.x, p1.y, p2.x, p2.y);
	}
	public boolean intersectsLine(double x1, double y1, double x2, double y2){
//		// pretty much just does the following, but with some optimisations by
//		// caching some values normally recalculated in the KPoint.linesIntersect method:
//		KPoint pointIBefore = points.get(points.size()-1);
//		for (int i = 0; i < points.size(); i++){
//			KPoint pointI = points.get(i);
//			if (KPoint.linesIntersect(x1, y1, x2, y2, pointIBefore.x, pointIBefore.y, pointI.x, pointI.y)){
//				return true;
//			}
//			pointIBefore = pointI;
//		}
//		return false;

		// Sometimes this method fails if the 'lines'
		// start and end on the same point, so here we check for that.
		if (x1 == x2 && y1 == y2){
			return false;
		}
		double ax = x2-x1;
		double ay = y2-y1;
		KPoint pointIBefore = points.get(points.size()-1);
		for (int i = 0; i < points.size(); i++){
			KPoint pointI = points.get(i);
			double x3 = pointIBefore.x;
			double y3 = pointIBefore.y;
			double x4 = pointI.x;
			double y4 = pointI.y;

			double bx = x3-x4;
			double by = y3-y4;
			double cx = x1-x3;
			double cy = y1-y3;

			double alphaNumerator = by*cx - bx*cy;
			double commonDenominator = ay*bx - ax*by;
			if (commonDenominator > 0){
				if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
					pointIBefore = pointI;
					continue;
				}
			}else if (commonDenominator < 0){
				if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
					pointIBefore = pointI;
					continue;
				}
			}
			double betaNumerator = ax*cy - ay*cx;
			if (commonDenominator > 0){
				if (betaNumerator < 0 || betaNumerator > commonDenominator){
					pointIBefore = pointI;
					continue;
				}
			}else if (commonDenominator < 0){
				if (betaNumerator > 0 || betaNumerator < commonDenominator){
					pointIBefore = pointI;
					continue;
				}
			}
			if (commonDenominator == 0){
				// This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
				// The lines are parallel.
				// Check if they're collinear.
				double collinearityTestForP3 = x1*(y2-y3) + x2*(y3-y1) + x3*(y1-y2);	// see http://mathworld.wolfram.com/Collinear.html
				// If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
				if (collinearityTestForP3 == 0){
					// The lines are collinear. Now check if they overlap.
					if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
							x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
							x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2){
						if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
								y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
								y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2){
							return true;
						}
					}
				}
				pointIBefore = pointI;
				continue;
			}
			return true;
		}
		return false;
	}

	public void calcCenter() {
		if (center == null){
			center = new KPoint();
		}
		if (getArea() == 0){
			center.x = points.get(0).x;
			center.y = points.get(0).y;
			return;
		}
        double cx = 0.0f;
		double cy = 0.0f;
		KPoint pointIBefore = (points.size() != 0 ? points.get(points.size()-1) : null);
        for (int i = 0; i < points.size(); i++) {
			//int nextI = (i+1 >= points.size() ? 0 : i+1);
//			double multiplier = (points.get(i).y * points.get(nextI).x - points.get(i).x * points.get(nextI).y);
//			cx += (points.get(i).x + points.get(nextI).x) * multiplier;
//			cy += (points.get(i).y + points.get(nextI).y) * multiplier;
			KPoint pointI = points.get(i);
			double multiplier = (pointIBefore.y * pointI.x - pointIBefore.x * pointI.y);
			cx += (pointIBefore.x + pointI.x) * multiplier;
			cy += (pointIBefore.y + pointI.y) * multiplier;
			pointIBefore = pointI;
        }
		cx /= (6 * getArea());
        cy /= (6 * getArea());
		if (counterClockWise == true){
			cx *= -1;
			cy *= -1;
		}
        center.x = cx;
		center.y = cy;
    }
	public void calcRadius() {
		if (center == null){
			calcCenter();
		}
		double maxRadiusSq = -1;
		int furthestPointIndex = 0;
		for (int i = 0; i < points.size(); i++) {
			double currentRadiusSq = (center.distanceSq(points.get(i)));
			if (currentRadiusSq > maxRadiusSq) {
				maxRadiusSq = currentRadiusSq;
				furthestPointIndex = i;
			}
		}
		radius = (center.distance(points.get(furthestPointIndex)));
		radiusSq = radius*radius;
	}

	public void calcAll(){
		this.calcArea();
		this.calcCenter();
		this.calcRadius();
	}

	public double getArea() {
		return area;
	}

	public KPoint getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}
	public double getRadiusSq() {
		return radiusSq;
	}

	public double getPerimeter() {
		double perimeter = 0;
		for (int i = 0; i < points.size()-1; i++) {
			perimeter += points.get(i).distance(points.get(i+1));
		}
		perimeter += points.get(points.size()).distance(points.get(0));
		return perimeter;
	}

	public void rotate(double angle) {
		rotate(angle, center.x, center.y);
	}
	public void rotate(double angle, KPoint axle) {
		rotate(angle, axle.x, axle.y);
	}
	public void rotate(double angle, double x, double y) {
		for (int i = 0; i < points.size(); i++) {
			KPoint p = points.get(i);
			p.rotate(angle, x, y);
		}
		// rotate the center if it's not equal to the axle.
		if (x != center.x || y != center.y){
			center.rotate(angle, x, y);
		}
	}


	public void translate(double x, double y) {
		for (int i = 0; i < points.size(); i++) {
			points.get(i).x += x;
			points.get(i).y += y;
		}
		center.x += x;
		center.y += y;
	}
	public void translate(KPoint translation){
		translate(translation.x, translation.y);
	}

	public void translateTo(double x, double y){
		double xIncrement = x - center.x;
		double yIncrement = y - center.y;
		center.x = x;
		center.y = y;
		for (int i = 0; i < points.size(); i++){
			points.get(i).x += xIncrement;
			points.get(i).y += yIncrement;
		}
	}
	public void translateTo(KPoint newCentre){
		translateTo(newCentre.x, newCentre.y);
	}
	public void translateToOrigin(){
		translateTo(0, 0);
	}

	public void scale(double xMultiplier, double yMultiplier, double x, double y){
		double incX;
		double incY;
		for (int i = 0; i < points.size(); i++){
			incX = points.get(i).x - x;
			incY = points.get(i).y - y;
			incX *= xMultiplier;
			incY *= yMultiplier;
			points.get(i).x = x + incX;
			points.get(i).y = y + incY;
		}
		incX = center.x - x;
		incY = center.y - y;
		incX *= xMultiplier;
		incY *= yMultiplier;
		center.x = x + incX;
		center.y = y + incY;
		this.calcArea();
		this.calcRadius();
	}
	public void scale(double multiplierX, double multiplierY){
		scale(multiplierX, multiplierY, getCenter().x, getCenter().y);
	}
	public void scale(double multiplierX, double multiplierY, KPoint p){
		scale(multiplierX, multiplierY, p.x, p.y);
	}
	public void scale(double multiplier){
		scale(multiplier, multiplier, getCenter().x, getCenter().y);
	}
	public void scale(double multiplier, KPoint p){
		scale(multiplier, multiplier, p.x, p.y);
	}

	public KPoint getBoundaryPointFromCenterToward(KPoint endPoint){
		double distToExtendOutTo = 3*getRadius();
		double xCoord = getCenter().x;
		double yCoord = getCenter().y;
		double xDiff = endPoint.x - getCenter().x;
		double yDiff = endPoint.y - getCenter().y;
		if (xDiff == 0 && yDiff == 0){
			yCoord += distToExtendOutTo;
		}else if (xDiff == 0){
			yCoord += distToExtendOutTo * Math.signum(yDiff);
		}else if (yDiff == 0){
			xCoord += distToExtendOutTo * Math.signum(xDiff);
		}else{
			xCoord += distToExtendOutTo * Math.abs(xDiff/(xDiff + yDiff)) * Math.signum(xDiff);
			yCoord += distToExtendOutTo * Math.abs(yDiff/(xDiff + yDiff)) * Math.signum(yDiff);
		}
		KPoint boundaryPoint = getClosestIntersectionToFirstFromSecond(getCenter().x, getCenter().y, xCoord, yCoord);
		return boundaryPoint;
	}

	public boolean isCounterClockWise() {
		return counterClockWise;
	}

	public void reversePointOrder(){
		counterClockWise = !counterClockWise;
		ArrayList<KPoint> tempPoints = new ArrayList<KPoint>(points.size());
		for (int i = points.size()-1; i >= 0; i--){
			tempPoints.add(points.get(i));
		}
		points.clear();
		points.addAll(tempPoints);
	}

	public boolean isValidNoLineIntersections() {
		return isValidNoLineIntersections(points);
	}
	public static boolean isValidNoLineIntersections(ArrayList<KPoint> points) {
		for (int i = 0; i < points.size(); i++){
			int iPlus = (i+1 >= points.size() ? 0 : i+1);
			for (int j = i+2; j < points.size(); j++){
				int jPlus = (j+1 >= points.size() ? 0 : j+1);
				if (i == jPlus){
					continue;
				}
				if (KPoint.linesIntersect(points.get(i), points.get(iPlus), points.get(j), points.get(jPlus))){
					return false;
				}
			}
		}
		return true;
	}

	public boolean isValidNoConsecutiveEqualPoints() {
		return isValidNoConsecutiveEqualPoints(points);
	}
	public static boolean isValidNoConsecutiveEqualPoints(ArrayList<KPoint> points) {
		KPoint pointIBefore = (points.size() != 0 ? points.get(points.size()-1) : null);
		for (int i = 0; i < points.size(); i++){
			KPoint pointI = points.get(i);
			if (pointI.x == pointIBefore.x && pointI.y == pointIBefore.y){
				return false;
			}
		}
		return true;
	}
	public boolean isValidNoEqualPoints() {
		return isValidNoEqualPoints(points);
	}
	public static boolean isValidNoEqualPoints(ArrayList<KPoint> points) {
		for (int i = 0; i < points.size(); i++){
			KPoint pointI = points.get(i);
			for (int j = i+1; j < points.size(); j++){
				KPoint pointJ = points.get(j);
				if (pointI.x == pointJ.x && pointI.y == pointJ.y){
					return false;
				}
			}
		}
		return true;
	}
	public static boolean printOffendingIntersectingLines(ArrayList<KPoint> points) {
		boolean linesIntersect = false;
		for (int i = 0; i < points.size(); i++){
			int iPlus = (i+1 >= points.size() ? 0 : i+1);
			for (int j = i+2; j < points.size(); j++){
				int jPlus = (j+1 >= points.size() ? 0 : j+1);
				if (i == jPlus){
					continue;
				}
				if (KPoint.linesIntersect(points.get(i), points.get(iPlus), points.get(j), points.get(jPlus))){
					System.out.println(KPolygon.class.getSimpleName()+": the line between points.get("+i+") & points.get("+iPlus+") intersects with the line between points.get("+j+") & points.get("+jPlus+")");
					System.out.println(KPolygon.class.getSimpleName()+": the line between points.get("+i+") == "+points.get(i));
					System.out.println(KPolygon.class.getSimpleName()+": the line between points.get("+iPlus+") == "+points.get(iPlus));
					System.out.println(KPolygon.class.getSimpleName()+": the line between points.get("+j+") == "+points.get(j));
					System.out.println(KPolygon.class.getSimpleName()+": the line between points.get("+jPlus+") == "+points.get(jPlus));
					linesIntersect = true;
				}
			}
		}
		return linesIntersect;
	}

	public KPolygon copy(){
		KPolygon polygon = new KPolygon(this);
		return polygon;
	}

	/**
	 * Needed by PolygonHolder.
	 * @return This KPolygon.
	 */
	public KPolygon getPolygon(){
		return this;
	}
	
	
	public void setTileArraySearchStatus(boolean trackerAddedStatus, Tracker tracker){
		this.trackerAddedStatus = trackerAddedStatus;
		this.trackerCounter = tracker.getCounter();
		this.trackerID = tracker.getID();
	}

	public boolean isTileArraySearchStatusAdded(Tracker tracker){
		if (this.trackerCounter == tracker.getCounter() && this.trackerID == tracker.getID()){
			return trackerAddedStatus;
		}else{
			return false;
		}
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public int getNextIndex(int i){
		int iPlus = i+1;
		return (iPlus >= points.size() ? 0 : iPlus);
	}
	public int getPrevIndex(int i){
		int iMinus = i-1;
		return (iMinus < 0 ? points.size()-1 : iMinus);
	}
	public KPoint getNextPoint(int i){
		return points.get(getNextIndex(i));
	}
	public KPoint getPrevPoint(int i){
		return points.get(getPrevIndex(i));
	}

	public String toString() {
		String str = getClass().getName() + "@" + Integer.toHexString(hashCode());
		if (getCenter() != null) {
			str += ", center == " + getCenter().toString();
		}
		str += ", area == " + area;
		str += ", radius == " + radius;
		if (points != null) {
			//str += ", points == " + points.toString();
			str += ", points.size() == "+points.size()+":\n";
			for (int i = 0; i < points.size(); i++){
				KPoint p = points.get(i);
				str += "  i == "+i+", "+p+"\n";
			}
		}
		return str;
	}

	// Note: The following methods are neded to implement java.awt.geom.Shape.
	public Rectangle2D.Double getBounds2D(){
		double[] bounds = getBoundsArray();
		return new Rectangle2D.Double(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	public Rectangle getBounds(){
		double[] bounds = getBoundsArray();
		return new Rectangle((int)(bounds[0]), (int)(bounds[1]), (int)Math.ceil(bounds[2]), (int)Math.ceil(bounds[3]));
	}
	/**
	 * Unlike Shape.intersects, this method is exact. Note that this method should
	 * really be called overlaps(x,y,w,h) since it doesn't just test for line-line intersection.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return Returns true if the given rectangle overlaps this polygon.
	 */
	public boolean intersects(double x, double y, double w, double h){
		if (x + w < center.x - radius ||
				x > center.x + radius ||
				y + h < center.y - radius ||
				y > center.y + radius){
			return false;
		}
		for (int i = 0; i < points.size(); i++){
			int nextI = (i+1 >= points.size() ? 0 : i+1);
			if (KPoint.linesIntersect(x, y, x + w, y, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x, y, x, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x, y+h, x+w, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x+w, y, x+w, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y)){
				return true;
			}
		}
		double px = points.get(0).x;
		double py = points.get(0).y;
		if (px > x && px < x + w && py > y && py < y + h){
			return true;
		}
		if (contains(x, y) == true){
			return true;
		}
		return false;
	}
	public boolean intersects(Rectangle2D r){
		return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	public boolean contains(Point2D p){
		return contains(p.getX(), p.getY());
	}

	/**
	 * Unlike Shape.contains, this method is exact.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return Returns true if the given rectangle wholly fits inside this polygon with no perimeter intersections.
	 */
	public boolean contains(double x, double y, double w, double h){
		if (x + w < center.x - radius ||
				x > center.x + radius ||
				y + h < center.y - radius ||
				y > center.y + radius){
			return false;
		}
		for (int i = 0; i < points.size(); i++){
			int nextI = (i+1 >= points.size() ? 0 : i+1);
			if (KPoint.linesIntersect(x, y, x + w, y, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x, y, x, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x, y+h, x+w, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y) ||
					KPoint.linesIntersect(x+w, y, x+w, y+h, points.get(i).x, points.get(i).y, points.get(nextI).x, points.get(nextI).y)){
				return false;
			}
		}
		double px = points.get(0).x;
		double py = points.get(0).y;
		if (px > x && px < x + w && py > y && py < y + h){
			return false;
		}
		if (contains(x, y) == true){
			return true;
		}
		return false;
	}
	public boolean contains(Rectangle2D r){
		return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	public PathIterator getPathIterator(AffineTransform at){
		return new KPolygonIterator(this, at);
	}
	public PathIterator getPathIterator(AffineTransform at, double flatness){
		return new KPolygonIterator(this, at);
	}
	public class KPolygonIterator implements PathIterator {
		int type = PathIterator.SEG_MOVETO;
		int index = 0;
		KPolygon polygon;
		KPoint currentPoint;
		AffineTransform affine;

		double[] singlePointSetDouble = new double[2];

		KPolygonIterator(KPolygon kPolygon) {
			this(kPolygon, null);
		}

		KPolygonIterator(KPolygon kPolygon, AffineTransform at) {
			this.polygon = kPolygon;
			this.affine = at;
			currentPoint = polygon.getPoint(0);
		}

		public int getWindingRule() {
			return PathIterator.WIND_EVEN_ODD;
		}

		public boolean isDone() {
			if (index == polygon.points.size() + 1){
				return true;
			}
			return false;
		}

		public void next() {
			index++;
		}

		public void assignPointAndType(){
			if (index == 0){
				currentPoint = polygon.getPoint(0);
				type = PathIterator.SEG_MOVETO;
			} else if (index == polygon.points.size()){
				type = PathIterator.SEG_CLOSE;
			} else{
				currentPoint = polygon.getPoint(index);
				type = PathIterator.SEG_LINETO;
			}
//			if (index == 0){
//				currentPoint = polygon.getPoint(0);
//				type = PathIterator.SEG_MOVETO;
//			} else if (index == polygon.points.size()+1){
//				type = PathIterator.SEG_CLOSE;
//			} else if (index == polygon.points.size()){
//				currentPoint = polygon.getPoint(0);
//				type = PathIterator.SEG_LINETO;
//			} else{
//				currentPoint = polygon.getPoint(index);
//				type = PathIterator.SEG_LINETO;
//			}
		}

		public int currentSegment(float[] coords){
			assignPointAndType();
			if (type != PathIterator.SEG_CLOSE){
				if (affine != null){
					float[] singlePointSetFloat = new float[2];
					singlePointSetFloat[0] = (float)currentPoint.x;
					singlePointSetFloat[1] = (float)currentPoint.y;
					affine.transform(singlePointSetFloat, 0, coords, 0, 1);
				} else{
					coords[0] = (float)currentPoint.x;
					coords[1] = (float)currentPoint.y;
				}
			}
			return type;
		}

		public int currentSegment(double[] coords){
			assignPointAndType();
			if (type != PathIterator.SEG_CLOSE){
				if (affine != null){
					singlePointSetDouble[0] = currentPoint.x;
					singlePointSetDouble[1] = currentPoint.y;
					affine.transform(singlePointSetDouble, 0, coords, 0, 1);
				} else{
					coords[0] = currentPoint.x;
					coords[1] = currentPoint.y;
				}
			}
			return type;
		}
	}





}
