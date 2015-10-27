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

/**
 *
 * @author Keith Woodward
 */
public class KPoint{
	public double x;
	public double y;
	public final static double TWO_PI = Math.PI*2;
	public KPoint(){
	}
	public KPoint(double x, double y){
		this.x = x;
		this.y = y;
	}
	public KPoint(KPoint old){
		this.x = old.x;
		this.y = old.y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setCoords(double x, double y){
		this.x = x;
		this.y = y;
	}
	public void setCoords(KPoint p){
		this.x = p.x;
		this.y = p.y;
	}
	public void translate(KPoint pointIncrement){
		translate(pointIncrement.x, pointIncrement.y);
	}
	public void translate(double xIncrement, double yIncrement){
		this.x += xIncrement;
		this.y += yIncrement;
	}
	public KPoint translateCopy(KPoint pointIncrement){
		KPoint p = this.copy();
		p.translate(pointIncrement.x, pointIncrement.y);
		return p;
	}
	public KPoint translateCopy(double xIncrement, double yIncrement){
		KPoint p = this.copy();
		p.translate(xIncrement, yIncrement);
		return p;
	}
	
	public void rotate(double angle, KPoint center) {
		rotate(angle, center.x, center.y);
	}
	public void rotate(double angle, double xCenter, double yCenter) {
		double currentAngle;
		double distance;
		currentAngle = Math.atan2(y - yCenter, x - xCenter);
		currentAngle += angle;
		distance = KPoint.distance(x, y, xCenter, yCenter);
		x = xCenter + (distance*Math.cos(currentAngle));
		y = yCenter + (distance*Math.sin(currentAngle));
	}
	public KPoint rotateCopy(double angle, KPoint center) {
		KPoint p = this.copy();
		p.rotate(angle, center.x, center.y);
		return p;
	}
	public KPoint rotateCopy(double angle, double xCenter, double yCenter) {
		KPoint p = this.copy();
		double currentAngle = Math.atan2(p.y - yCenter, p.x - xCenter);
		currentAngle += angle;
		double distance = KPoint.distance(p.x, p.y, xCenter, yCenter);
		p.x = xCenter + (distance*Math.cos(currentAngle));
		p.y = yCenter + (distance*Math.sin(currentAngle));
		return p;
	}

	public boolean equals(KPoint p){
		if (x == p.x && y == p.y){
			return true;
		}
		return false;
	}
	
	public double distance(KPoint p){
		return distance(this.x, this.y, p.x, p.y);
	}
	public double distance(double x2, double y2){
		return distance(this.x, this.y, x2, y2);
	}
	public static double distance(KPoint p, KPoint p2){
		return distance(p.x, p.y, p2.x, p2.y);
    }
	public static double distance(double x1, double y1,
				  double x2, double y2){
		x1 -= x2;
		y1 -= y2;
		return Math.sqrt(x1 * x1 + y1 * y1);
    }
	public double distanceSq(KPoint p){
		return distanceSq(this.x, this.y, p.x, p.y);
	}
	public double distanceSq(double x2, double y2){
		return distanceSq(this.x, this.y, x2, y2);
	}
	public static double distanceSq(double x1, double y1,
				  double x2, double y2){
		x1 -= x2;
		y1 -= y2;
		return (x1 * x1 + y1 * y1);
    }

	public static boolean collinear(double x1, double y1, double x2, double y2, double x3, double y3){
		double collinearityTest = x1*(y2-y3) + x2*(y3-y1) + x3*(y1-y2);	// see http://mathworld.wolfram.com/Collinear.html
		if (collinearityTest == 0){
			return true;
		}
		return false;
	}
	public static boolean collinear(KPoint p1, KPoint p2, KPoint p3){
		return collinear(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
	}
	public boolean collinear(KPoint p1, KPoint p2){
		return collinear(x, y, p1.x, p1.y, p2.x, p2.y);
	}
	public boolean collinear(double x1, double y1, double x2, double y2){
		return collinear(x, y, x1, y1, x2, y2);
	}

	public static boolean linesIntersect(KPoint p1, KPoint p2, KPoint p3, KPoint p4){
		return linesIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	}
	public static boolean linesIntersect(double x1, double y1, double x2, double y2,
										double x3, double y3, double x4, double y4){
		// Return false if either of the lines have zero length
		if (x1 == x2 && y1 == y2 ||
				x3 == x4 && y3 == y4){
			return false;
		}
		// Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book (http://www.graphicsgems.org/)
		double ax = x2-x1;
		double ay = y2-y1;
		double bx = x3-x4;
		double by = y3-y4;
		double cx = x1-x3;
		double cy = y1-y3;

		double alphaNumerator = by*cx - bx*cy;
		double commonDenominator = ay*bx - ax*by;
		if (commonDenominator > 0){
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
				return false;
			}
		}else if (commonDenominator < 0){
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
				return false;
			}
		}
		double betaNumerator = ax*cy - ay*cx;
		if (commonDenominator > 0){
			if (betaNumerator < 0 || betaNumerator > commonDenominator){
				return false;
			}
		}else if (commonDenominator < 0){
			if (betaNumerator > 0 || betaNumerator < commonDenominator){
				return false;
			}
		}
		// if commonDenominator == 0 then the lines are parallel.
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
			return false;
		}
		return true;

		// Fast method with box test which may be slower or faster... depends
//		double x1lo,x1hi,y1lo,y1hi;
//		double ax = x2-x1;
//		double bx = x3-x4;
//		// X bound box test/
//		if (ax < 0) {
//			x1hi = x1;
//			x1lo = x2;
//		} else{
//			x1hi = x2;
//			x1lo = x1;
//		}
//		if(bx > 0) {
//			if (x1hi < x4 || x3 < x1lo){
//				return false;
//			}
//		} else {
//			if (x1hi < x3 || x4 < x1lo){
//				return false;
//			}
//		}
//
//		double ay = y2-y1;
//		double by = y3-y4;
//		// Y bound box test/
//		if (ay < 0) {
//			y1hi = y1;
//			y1lo = y2;
//		} else{
//			y1hi = y2;
//			y1lo = y1;
//		}
//		if(by > 0) {
//			if (y1hi < y4 || y3 < y1lo){
//				return false;
//			}
//		} else {
//			if (y1hi < y3 || y4 < y1lo){
//				return false;
//			}
//		}
//		double cx = x1-x3;
//		double cy = y1-y3;
//		double alphaNumerator = by*cx - bx*cy;
//		double commonDenominator = ay*bx - ax*by;
//		if (commonDenominator > 0){
//			if (alphaNumerator < 0 || alphaNumerator > commonDenominator){
//				return false;
//			}
//		}else if (commonDenominator < 0){
//			if (alphaNumerator > 0 || alphaNumerator < commonDenominator){
//				return false;
//			}
//		}
//		double betaNumerator = ax*cy - ay*cx;
//		if (commonDenominator > 0){
//			if (betaNumerator < 0 || betaNumerator > commonDenominator){
//				return false;
//			}
//		}else if (commonDenominator < 0){
//			if (betaNumerator > 0 || betaNumerator < commonDenominator){
//				return false;
//			}
//		}
//		if (commonDenominator == 0){
//			//System.out.println("1");
//			// This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
//			// The lines are parallel.
//			// Check if they're collinear.
//			double y3LessY1 = y3-y1;
//			double collinearityTestForP3 = x1*(y2-y3) + x2*(y3LessY1) + x3*(y1-y2);	// see http://mathworld.wolfram.com/Collinear.html
//			// If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
//			if (collinearityTestForP3 == 0){
//				//System.out.println("2");
//				// The lines are collinear. Now check if they overlap.
//				if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
//						x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
//						x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2){
//					if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
//							y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
//							y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2){
//						return true;
//					}
//				}
//			}
//			//System.out.println("5");
//			return false;
//		}
//		return true;
//
//		// Method 1:
//		return java.awt.geom.Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
//		// Method 2:
//		return ((relCCW(x1, y1, x2, y2, x3, y3) * relCCW(x1, y1, x2, y2, x4, y4) <= 0) &&
//				(relCCW(x3, y3, x4, y4, x1, y1) * relCCW(x3, y3, x4, y4, x2, y2) <= 0));
//		// Method 3:
//		return ((relCCWDouble(x1, y1, x2, y2, x3, y3) * relCCWDouble(x1, y1, x2, y2, x4, y4) <= 0) &&
//				(relCCWDouble(x3, y3, x4, y4, x1, y1) * relCCWDouble(x3, y3, x4, y4, x2, y2) <= 0));
//		// Method 4:
//		double x2LessX1 = x2-x1;
//		double y2LessY1 = y2-y1;
//		double x3LessX1 = x3-x1;
//		double y3LessY1 = y3-y1;
//		double x4LessX1 = x4-x1;
//		double y4LessY1 = y4-y1;
//		if (ccwDouble(x2LessX1, y2LessY1, x3LessX1, y3LessY1) * ccwDouble(x2LessX1, y2LessY1, x4LessX1, y4LessY1) > 0){
//			return false;
//		}
//		double x1LessX3 = x1-x3;
//		double y1LessY3 = y1-y3;
//		double x2LessX3 = x2-x3;
//		double y2LessY3 = y2-y3;
//		double x4LessX3 = x4-x3;
//		double y4LessY3 = y4-y3;
//		if (ccwDouble(x4LessX3, y4LessY3, x1LessX3, y1LessY3) * ccwDouble(x4LessX3, y4LessY3, x2LessX3, y2LessY3) > 0){
//			return false;
//		}
//		return true;
	}

	public static KPoint getLineLineIntersection(KPoint p1, KPoint p2, KPoint p3, KPoint p4){
		return getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	}
	public static KPoint getLineLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		double det1And2 = det(x1, y1, x2, y2);
		double det3And4 = det(x3, y3, x4, y4);
		double x1LessX2 = x1 - x2;
		double y1LessY2 = y1 - y2;
		double x3LessX4 = x3 - x4;
		double y3LessY4 = y3 - y4;
		double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
		if (det1Less2And3Less4 == 0){
			// the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
			return null;
		}
		double x = (det(det1And2, x1LessX2,
				det3And4, x3LessX4) /
				det1Less2And3Less4);
		double y = (det(det1And2, y1LessY2,
				det3And4, y3LessY4) /
				det1Less2And3Less4);
//		double x = (det(det(x1, y1, x2, y2), x1 - x2,
//				det(x3, y3, x4, y4), x3 - x4) /
//				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4));
//		double y = (det(det(x1, y1, x2, y2), y1 - y2,
//				det(x3, y3, x4, y4), y3 - y4) /
//				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4));
//		if (Double.isNaN(x) || Double.isNaN(y)){
//			return null;
//		}
		return new KPoint(x, y);
	}
	protected static double det(double a, double b, double c, double d) {
		return a * d - b * c;
	}


	/**
	 * Returns a positive double if (x, y) is counter-clockwise to (x2, y2) relative to the origin
	 * in the cartesian coordinate space (positive x-axis extends right, positive y-axis extends up).
	 * Returns a negative double if (x, y) is clockwise to (x2, y2) relative to the origin.
	 * Returns a 0.0 if (x, y), (x2, y2) and the origin are collinear.
	 *
	 * Alternatively, a value of 1 indicates that the shortest angle from (x,y) to (x2, y2)
	 * is in the direction that takes the positive X axis towards the positive Y axis.
	 *
	 * Note that this method gives different results to java.awt.geom.Line2D.relativeCCW() since Java2D
	 * uses a different coordinate system (positive x-axis extends right, positive y-axis extends down).

	 *
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double ccwDouble( double x2LessX1, double y2LessY1,
				  double pxLessX1, double pyLessY1){
		double ccw = pyLessY1 * x2LessX1 - pxLessX1 * y2LessY1;
		return ccw;
	}

	public static double ccwDoubleExtra( double x2LessX1, double y2LessY1,
				  double pxLessX1, double pyLessY1){
		double ccw = pyLessY1 * x2LessX1 - pxLessX1 * y2LessY1;
		if (ccw == 0.0) {
			// The point is colinear, classify based on which side of
			// the segment the point falls on.  We can calculate a
			// relative value using the projection of px,py onto the
			// segment - a negative value indicates the point projects
			// outside of the segment in the direction of the particular
			// endpoint used as the origin for the projection.
			ccw = pxLessX1 * x2LessX1 + pyLessY1 * y2LessY1;
			if (ccw > 0.0) {
				// Reverse the projection to be relative to the original x2,y2
				// x2 and y2 are simply negated.
				// px and py need to have (x2 - x1) or (y2 - y1) subtracted
				//    from them (based on the original values)
				// Since we really want to get a positive answer when the
				//    point is "beyond (x2,y2)", then we want to calculate
				//    the inverse anyway - thus we leave x2 & y2 negated.
				pxLessX1 -= x2LessX1;
				pyLessY1 -= y2LessY1;
				ccw = pxLessX1 * x2LessX1 + pyLessY1 * y2LessY1;
				if (ccw < 0.0) {
					ccw = 0.0;
				}
			}
		}
		return ccw;
	}


//	/**
//	 * Returns a positive double if (x, y) is counter-clockwise to (x2, y2) relative to the origin
//	 * in the cartesian coordinate space (positive x-axis extends right, positive y-axis extends up).
//	 * Returns a negative double if (x, y) is clockwise to (x2, y2) relative to the origin.
//	 * Returns a 0.0 if (x, y), (x2, y2) and the origin are collinear.
//	 *
//	 * Alternatively, a value of 1 indicates that the shortest angle from (x,y) to (x2, y2)
//	 * is in the direction that takes the positive X axis towards the positive Y axis.
//	 *
//	 * Note that this method gives different results to java.awt.geom.Line2D.relativeCCW() since Java2D
//	 * uses a different coordinate system (positive x-axis extends right, positive y-axis extends down).
//
//	 *
//	 * @param x
//	 * @param y
//	 * @param x2
//	 * @param y2
//	 * @return
//	 */
	public double ccwDouble(double x2, double y2){
		return ccwDouble(x, y, x2, y2);
	}
	public double ccwDouble(KPoint p){
		return ccwDouble(x, y, p.x, p.y);
	}
	public static int ccw(double x, double y,
				  double x2, double y2){
		double ccw = ccwDouble(x, y, x2, y2);
		return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
	}
	public double ccw(double x2, double y2){
		return ccw(x, y, x2, y2);
	}
	public double ccw(KPoint p){
		return ccw(x, y, p.x, p.y);
	}

	public static double relCCWDouble(double x1, double y1,
				  double x2, double y2,
				  double px, double py){
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;
		double ccw = py * x2 - px * y2;
		return ccw;
	}
	public double relCCWDouble(double x1, double y1, double x2, double y2){
		return relCCWDouble(x1, y1, x2, y2, x, y);
	}
	public double relCCWDouble(KPoint p1, KPoint p2){
		return relCCWDouble(p1.x, p1.y, p2.x, p2.y, x, y);
	}
	/**
	 * Returns a positive double if (px, py) is counter-clockwise to (x2, y2) relative to (x1, y1).
	 * in the cartesian coordinate space (positive x-axis extends right, positive y-axis extends up).
	 * Returns a negative double if (px, py) is clockwise to (x2, y2) relative to (x1, y1).
	 * Returns a 0.0 if (px, py), (x1, y1) and (x2, y2) are collinear.
	 * Note that this method gives different results to java.awt.geom.Line2D.relativeCCW() since Java2D
	 * uses a different coordinate system (positive x-axis extends right, positive y-axis extends down).
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param px
	 * @param py
	 * @return
	 */
	public static int relCCW(double x1, double y1,
				  double x2, double y2,
				  double px, double py){
		double ccw = relCCWDouble(x1, y1, x2, y2, px, py);
		return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
    }

	public int relCCW(double x1, double y1, double x2, double y2){
		return relCCW(x1, y1, x2, y2, x, y);
	}
	public int relCCW(KPoint p1, KPoint p2){
		return relCCW(p1.x, p1.y, p2.x, p2.y, x, y);
	}

	public static double relCCWDoubleExtra(double x1, double y1,
				  double x2, double y2,
				  double px, double py){
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;
		double ccw = py * x2 - px * y2;
		if (ccw == 0.0) {
			// The point is colinear, classify based on which side of
			// the segment the point falls on.  We can calculate a
			// relative value using the projection of px,py onto the
			// segment - a negative value indicates the point projects
			// outside of the segment in the direction of the particular
			// endpoint used as the origin for the projection.
			ccw = px * x2 + py * y2;
			if (ccw > 0.0) {
				// Reverse the projection to be relative to the original x2,y2
				// x2 and y2 are simply negated.
				// px and py need to have (x2 - x1) or (y2 - y1) subtracted
				//    from them (based on the original values)
				// Since we really want to get a positive answer when the
				//    point is "beyond (x2,y2)", then we want to calculate
				//    the inverse anyway - thus we leave x2 & y2 negated.
				px -= x2;
				py -= y2;
				ccw = px * x2 + py * y2;
				if (ccw < 0.0) {
					ccw = 0.0;
				}
			}
		}
		return ccw;
	}
	
	public String toString(){
		return ""+x+", "+y;
	}
	public double findSignedAngle(double ox, double oy){
		return findSignedAngle(this.x, this.y, ox, oy);
	}
	public double findSignedAngle(KPoint dest){
		return findSignedAngle(this, dest);
	}
	public static double findSignedAngle(KPoint start, KPoint dest){
		return findSignedAngle(start.x, start.y, dest.x, dest.y);
	}
	public static double findSignedAngle(double x1, double y1, double x2, double y2){
		double x = x2 - x1;
		double y = y2 - y1;
		double angle = (Math.atan2(y, x));
//		double angle = (float)(FastMath.atan2_fast((float)y, (float)x));
//		double angle = (float)(FastMath.atan2((float)y, (float)x));
		return angle;
	}
	public double findAngle(double ox, double oy){
		return findAngle(this.x, this.y, ox, oy);
	}
	public double findAngle(KPoint dest){
		return findAngle(this, dest);
	}

	public double findAngleFromOrigin(){
		double angle = findSignedAngleFromOrigin();
		if (angle < 0) {
			angle += TWO_PI;
		}
		return angle;
	}
	public double findSignedAngleFromOrigin(){
		return Math.atan2(y, x);
	}
	
	public static double findAngle(KPoint start, KPoint dest){
		return findAngle(start.x, start.y, dest.x, dest.y);
	}
	public static double findAngle(double x1, double y1, double x2, double y2){
		double angle = findSignedAngle(x1, y1, x2, y2);
		if (angle < 0) {
			angle += TWO_PI;
		}
		return angle;
	}

	public double findSignedRelativeAngle(double x1, double y1, double x2, double y2){
		return findSignedRelativeAngle(this.x, this.y, x1, y1, x2, y2);
	}
	public double findSignedRelativeAngle(KPoint start, KPoint end){
		return findSignedRelativeAngle(this, start, end);
	}
	public static double findSignedRelativeAngle(KPoint point, KPoint start, KPoint end){
		return findSignedRelativeAngle(point.x, point.y, start.x, start.y, end.x, end.y);
	}
	public static double findSignedRelativeAngle(double x, double y, double x1, double y1, double x2, double y2){
		double lineAngle = findAngle(x1, y1, x2, y2);
		double pointAngle = findAngle(x1, y1, x, y);
		if (pointAngle < lineAngle){
			pointAngle += TWO_PI;
		}
		double relativePointAngle = pointAngle - lineAngle;
		if (relativePointAngle > Math.PI){
			relativePointAngle -= TWO_PI;
		}
		assert relativePointAngle <= Math.PI && relativePointAngle >= -Math.PI : relativePointAngle;
		return relativePointAngle;
	}

	public double findRelativeAngle(double x1, double y1, double x2, double y2){
		return findRelativeAngle(this.x, this.y, x1, y1, x2, y2);
	}
	public double findRelativeAngle(KPoint start, KPoint end){
		return findRelativeAngle(this, start, end);
	}
	public static double findRelativeAngle(KPoint point, KPoint start, KPoint end){
		return findRelativeAngle(point.x, point.y, start.x, start.y, end.x, end.y);
	}
	public static double findRelativeAngle(double x, double y, double x1, double y1, double x2, double y2){
		double relativePointAngle = findSignedRelativeAngle(x, y, x1, y1, x2, y2);
		if (relativePointAngle < -Math.PI){
			relativePointAngle += TWO_PI;
		}
		assert relativePointAngle <= 2*Math.PI && relativePointAngle >= 0 : relativePointAngle;
		return relativePointAngle;
	}

	

	public KPoint midPoint(KPoint p){
		return midPoint(x, y, p.x, p.y);
	}
	public static KPoint midPoint(KPoint p, KPoint p2){
		return midPoint(p.x, p.y, p2.x, p2.y);
	}
	public static KPoint midPoint(double x, double y, double x2, double y2){
		return new KPoint((x + x2)/2f, (y + y2)/2f);
	}
	public KPoint createPointFromAngle(double angle, double distance){
		return createPointFromAngle(x, y, angle, distance);
	}
	public static KPoint createPointFromAngle(double x, double y, double angle, double distance){
		KPoint p = new KPoint();
		double xDist = Math.cos(angle)*distance;
		double yDist = Math.sin(angle)*distance;
		p.x = (x+xDist);
		p.y = (y+yDist);
		return p;
	}
	public KPoint createPointToward(KPoint p, double distance){
		return createPointToward(x, y, p.x, p.y, distance);
	}
	public KPoint createPointToward(double x2, double y2, double distance){
		return createPointToward(x, y, x2, y2, distance);
	}
	public static KPoint createPointToward(double x, double y, double x2, double y2, double distance){
		KPoint p = new KPoint();
		double xDiff = (x2 - x);
		double yDiff = (y2 - y);
		double ptDist = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
		double distOnPtDist = distance/ptDist;
		double xDist = xDiff*distOnPtDist;
		double yDist = yDiff*distOnPtDist;
		p.x = (x+xDist);
		p.y = (y+yDist);
		return p;
	}
	public KPoint copy(){
		return new KPoint(x, y);
	}

	public double ptLineDist(double x1, double y1, double x2, double y2){
		return ptLineDist(x1, y1, x2, y2, x, y);
	}
	public double ptLineDist(KPoint start, KPoint end){
		return ptLineDist(start.x, start.y, end.x, end.y, x, y);
	}
	public static double ptLineDist(KPoint start, KPoint end, KPoint p){
		return ptLineDist(start.x, start.y, end.x, end.y, p.x, p.y);
	}
	public static double ptLineDist(double x1, double y1, double x2, double y2, double px, double py){
		return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py));
	}

	public double ptLineDistSq(double x1, double y1, double x2, double y2){
		return ptLineDistSq(x1, y1, x2, y2, x, y);
	}
	public double ptLineDistSq(KPoint start, KPoint end){
		return ptLineDistSq(start.x, start.y, end.x, end.y, x, y);
	}
	public static double ptLineDistSq(KPoint start, KPoint end, KPoint p){
		return ptLineDistSq(start.x, start.y, end.x, end.y, p.x, p.y);
	}
	public static double ptLineDistSq(double x1, double y1, double x2, double y2, double px, double py){
		//from: Line2D.Float.ptLineDistSq(x1, y1, x2, y2, px, py);
		// Adjust vectors relative to x1,y1
		// x2,y2 becomes relative vector from x1,y1 to end of segment
		x2 -= x1;
		y2 -= y1;
		// px,py becomes relative vector from x1,y1 to test point
		px -= x1;
		py -= y1;
		double dotprod = px * x2 + py * y2;
		// dotprod is the length of the px,py vector
		// projected on the x1,y1=>x2,y2 vector times the
		// length of the x1,y1=>x2,y2 vector
		double projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
		// Distance to line is now the length of the relative point
		// vector minus the length of its projection onto the line
		double lenSq = px * px + py * py - projlenSq;
		if (lenSq < 0) {
			lenSq = 0;
		}
		return lenSq;
	}

	public double ptSegDist(double x1, double y1, double x2, double y2){
		return ptSegDist(x1, y1, x2, y2, x, y);
	}
	public double ptSegDist(KPoint start, KPoint end){
		return ptSegDist(start.x, start.y, end.x, end.y, x, y);
	}
	public static double ptSegDist(KPoint start, KPoint end, KPoint p){
		return ptSegDist(start.x, start.y, end.x, end.y, p.x, p.y);
	}
	public static double ptSegDist(double x1, double y1, double x2, double y2, double px, double py){
		return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py));
	}

	public double ptSegDistSq(double x1, double y1, double x2, double y2){
		return ptSegDistSq(x1, y1, x2, y2, x, y);
	}
	public double ptSegDistSq(KPoint start, KPoint end){
		return ptSegDistSq(start.x, start.y, end.x, end.y, x, y);
	}
	public static double ptSegDistSq(KPoint start, KPoint end, KPoint p){
		return ptSegDistSq(start.x, start.y, end.x, end.y, p.x, p.y);
	}
	public static double ptSegDistSq(double x1, double y1, double x2, double y2, double px, double py){
		//from: Line2D.Float.ptSegDistSq(x1, y1, x2, y2, px, py);
		// Adjust vectors relative to x1,y1
		// x2,y2 becomes relative vector from x1,y1 to end of segment
		x2 -= x1;
		y2 -= y1;
		// px,py becomes relative vector from x1,y1 to test point
		px -= x1;
		py -= y1;
		double dotprod = px * x2 + py * y2;
		double projlenSq;
		if (dotprod <= 0.0) {
			// px,py is on the side of x1,y1 away from x2,y2
			// distance to segment is length of px,py vector
			// "length of its (clipped) projection" is now 0.0
			projlenSq = 0.0;
		} else {
			// switch to backwards vectors relative to x2,y2
			// x2,y2 are already the negative of x1,y1=>x2,y2
			// to get px,py to be the negative of px,py=>x2,y2
			// the dot product of two negated vectors is the same
			// as the dot product of the two normal vectors
			px = x2 - px;
			py = y2 - py;
			dotprod = px * x2 + py * y2;
			if (dotprod <= 0.0) {
				// px,py is on the side of x2,y2 away from x1,y1
				// distance to segment is length of (backwards) px,py vector
				// "length of its (clipped) projection" is now 0.0
				projlenSq = 0.0;
			} else {
				// px,py is between x1,y1 and x2,y2
				// dotprod is the length of the px,py vector
				// projected on the x2,y2=>x1,y1 vector times the
				// length of the x2,y2=>x1,y1 vector
				projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
			}
		}
		// Distance to line is now the length of the relative point
		// vector minus the length of its projection onto the line
		// (which is zero if the projection falls outside the range
		//  of the line segment).
		double lenSq = px * px + py * py - projlenSq;
		if (lenSq < 0) {
			lenSq = 0;
		}
		return lenSq;
	}

	public static KPoint getClosestPointOnSegment(double x1, double y1, double x2, double y2, double px, double py){
		KPoint closestPoint = new KPoint();
		double x2LessX1 = x2 - x1;
		double y2LessY1 = y2 - y1;
		double lNum = x2LessX1*x2LessX1 + y2LessY1*y2LessY1;
		double rNum = ((px - x1)*x2LessX1 + (py - y1)*y2LessY1) / lNum;
//		double lNum = (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1);
//		double rNum = ((px - x1)*(x2 - x1) + (py - y1)*(y2 - y1)) / lNum;
		if (rNum <= 0){
			closestPoint.x = x1;
			closestPoint.y = y1;
		}else if (rNum >= 1){
			closestPoint.x = x2;
			closestPoint.y = y2;
		}else{
			closestPoint.x = (x1 + rNum*x2LessX1);
			closestPoint.y = (y1 + rNum*y2LessY1);
		}
		return closestPoint;
//    from:  http://www.codeguru.com/forum/showthread.php?t=194400
//    Let the point be C (Cx,Cy) and the line be AB (Ax,Ay) to (Bx,By).
//    Let P be the point of perpendicular projection of C on AB.  The parameter
//    r, which indicates P's position along AB, is computed by the dot product
//    of AC and AB divided by the square of the length of AB:
//
//    (1)     AC dot AB
//        r = ---------
//            ||AB||^2
//
//    r has the following meaning:
//
//        r=0      P = A
//        r=1      P = B
//        r<0      P is on the backward extension of AB
//        r>1      P is on the forward extension of AB
//        0<r<1    P is interior to AB
//
//    The length of a line segment in d dimensions, AB is computed by:
//
//        L = sqrt( (Bx-Ax)^2 + (By-Ay)^2 + ... + (Bd-Ad)^2)
//
//    so in 2D:
//
//        L = sqrt( (Bx-Ax)^2 + (By-Ay)^2 )
//
//    and the dot product of two vectors in d dimensions, U dot V is computed:
//
//        D = (Ux * Vx) + (Uy * Vy) + ... + (Ud * Vd)
//
//    so in 2D:
//
//        D = (Ux * Vx) + (Uy * Vy)
//
//    So (1) expands to:
//
//            (Cx-Ax)(Bx-Ax) + (Cy-Ay)(By-Ay)
//        r = -------------------------------
//                          L^2
//
//    The point P can then be found:
//
//        Px = Ax + r(Bx-Ax)
//        Py = Ay + r(By-Ay)
//
//    And the distance from A to P = r*L.
//
//    Use another parameter s to indicate the location along PC, with the
//    following meaning:
//           s<0      C is left of AB
//           s>0      C is right of AB
//           s=0      C is on AB
//
//    Compute s as follows:
//
//            (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
//        s = -----------------------------
//                        L^2
//
//
//    Then the distance from C to P = |s|*L.
	}
	public KPoint getClosestPointOnSegment(double x1, double y1, double x2, double y2){
		return getClosestPointOnSegment(x1, y1, x2, y2, x, y);
	}
	public KPoint getClosestPointOnSegment(KPoint p1, KPoint p2){
		return getClosestPointOnSegment(p1.x, p1.y, p2.x, p2.y, x, y);
	}


//	public static void main(String[] args){
//		int numObjects = 1000000;
//		java.util.ArrayList<KPoint> list = new java.util.ArrayList<KPoint>(numObjects);
//		java.util.Random rand = new java.util.Random(1);
//		double r = 1000;
//		KPolygon poly = KPolygon.createRegularPolygon(5, r/3);
//		poly.translateTo(r/2, r/2);
//		for (int i = 0; i < numObjects; i++){
//			list.add(new KPoint(rand.nextFloat()*r, rand.nextFloat()*r));
//		}
//		int numTests = 10;
//		long averageNanosElapsed = 0;
//		for (int j = 0; j < numTests; j++){
//			long startNanos = System.nanoTime();
//			long count = -Long.MAX_VALUE;
//			for (int i = 0; i < list.size()-1; i++){
//				list.get(i).distance(list.get(i+1));
//			}
//			long endNanos = System.nanoTime();
//			long nanosElapsed = endNanos - startNanos;
//			nanosElapsed /= numObjects;
//			System.out.println("nanosElapsed == "+nanosElapsed);
//			averageNanosElapsed += nanosElapsed;
//		}
//		averageNanosElapsed /= numTests;
//		System.out.println("ArrayList.size(), averageNanosElapsed == "+averageNanosElapsed);
//	}


	//	public static void main(String[] args){
//		int numObjects = 100000;
//		java.util.ArrayList<KPoint> list = new java.util.ArrayList<KPoint>(numObjects);
//		java.util.Random rand = new java.util.Random(1);
//		double r = 1000;
//		KPolygon poly = KPolygon.createRegularPolygon(5, r/3);
//		poly.translateTo(r/2, r/2);
//		for (int i = 0; i < numObjects; i++){
//			list.add(new KPoint(rand.nextFloat()*r, rand.nextFloat()*r));
//		}
//		// warm the JIT
//		{
//			int numTests = 10;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
//	//			System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//	//		System.out.println("ArrayList.size(), averageNanosElapsed == "+averageNanosElapsed);
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine2(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
//	//			System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//	//		System.out.println("final int size,   averageNanosElapsed == "+averageNanosElapsed);
//		}
//
//		// run the tests
//		System.out.println("test 1, with ArrayList.size() first");
//		{
//			int numTests = 100;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect, averageNanosElapsed == "+averageNanosElapsed);
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine2(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect2,   averageNanosElapsed == "+averageNanosElapsed);
//		}
//		System.out.println("test 2, with ArrayList.size() second");
//		{
//			int numTests = 100;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine2(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect2,   averageNanosElapsed == "+averageNanosElapsed);
//
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-1; i++){
//					poly.intersectsLine(list.get(i).x, list.get(i).y, list.get(i+1).x, list.get(i+1).y);
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect, averageNanosElapsed == "+averageNanosElapsed);
//
//		}
//	}

//	public static void main(String[] args){
//		for (int a = 0; a < 3; a++){
//		int numObjects = 100000;
//		java.util.ArrayList<KPoint> list = new java.util.ArrayList<KPoint>(numObjects);
//		java.util.Random rand = new java.util.Random(100);
//		double r = 1000;
//		for (int i = 0; i < numObjects; i++){
//			list.add(new KPoint(rand.nextFloat()*r, rand.nextFloat()*r));
//		}
//		// warm the JIT
//		{
//			int numTests = 10;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
//	//			System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//	//		System.out.println("ArrayList.size(), averageNanosElapsed == "+averageNanosElapsed);
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect2(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
//	//			System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//	//		System.out.println("final int size,   averageNanosElapsed == "+averageNanosElapsed);
//		}
//
//		// run the tests
//		System.out.println("test 1, with ArrayList.size() first");
//		{
//			int numTests = 100;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect, averageNanosElapsed == "+averageNanosElapsed);
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect2(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect2,   averageNanosElapsed == "+averageNanosElapsed);
//		}
//		System.out.println("test 2, with ArrayList.size() second");
//		{
//			int numTests = 100;
//			long averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect2(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("final int size,   nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect2,   averageNanosElapsed == "+averageNanosElapsed);
//
//			averageNanosElapsed = 0;
//			for (int j = 0; j < numTests; j++){
//				long startNanos = System.nanoTime();
//				long count = -Long.MAX_VALUE;
//				for (int i = 0; i < list.size()-3; i++){
//					linesIntersect(list.get(i), list.get(i+1), list.get(i+2), list.get(i+3));
//				}
//				long endNanos = System.nanoTime();
//				long nanosElapsed = endNanos - startNanos;
////				System.out.println("ArrayList.size(), nanosElapsed == "+nanosElapsed);
//				averageNanosElapsed += nanosElapsed;
//			}
//			averageNanosElapsed /= numTests;
//			System.out.println("linesIntersect, averageNanosElapsed == "+averageNanosElapsed);
//
//		}
//		}
//	}


	public static void main(String[] args){
		// a bunch of tests for the linesIntersect method
		{
			KPoint p = new KPoint(1,1);
			KPoint p2 = new KPoint(2,1);

			KPoint p3 = new KPoint(1,0);
			KPoint p4 = new KPoint(1,3);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,1);

			KPoint p3 = new KPoint(0,0);
			KPoint p4 = new KPoint(1,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,1);

			KPoint p3 = new KPoint(-1,-1);
			KPoint p4 = new KPoint(1,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}

		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,0);

			KPoint p3 = new KPoint(-1,-1);
			KPoint p4 = new KPoint(1,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(0,1);
			KPoint p4 = new KPoint(0,-1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(0,2);
			KPoint p4 = new KPoint(2,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(1,1);
			KPoint p4 = new KPoint(2,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,0);

			KPoint p3 = new KPoint(0,0);
			KPoint p4 = new KPoint(1,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,1);

			KPoint p3 = new KPoint(0,2);
			KPoint p4 = new KPoint(0,3);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,0);

			KPoint p3 = new KPoint(2,0);
			KPoint p4 = new KPoint(3,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(2,2);
			KPoint p4 = new KPoint(3,3);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(0,0);
			KPoint p4 = new KPoint(0,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,1);

			KPoint p3 = new KPoint(1,0);
			KPoint p4 = new KPoint(1,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		System.out.println("Tests for parrallel, but non-intersecting lines");
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,0);

			KPoint p3 = new KPoint(0,1);
			KPoint p4 = new KPoint(1,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(1,1);

			KPoint p3 = new KPoint(1,0);
			KPoint p4 = new KPoint(2,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
		{
			KPoint p = new KPoint(1,0);
			KPoint p2 = new KPoint(2,1);

			KPoint p3 = new KPoint(0,0);
			KPoint p4 = new KPoint(1,1);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be false");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be false");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be false");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be false");
		}
//		{
//			KPoint p  = new KPoint(146.4415283203125, 293.59331672814244);
//			KPoint p2 = new KPoint(169.93426637356671, 244.19351482289574);
//			KPoint p3 = new KPoint(146.4415283203125, 293.6166011378036);
//			KPoint p4 = new KPoint(169.94329717555914, 244.1978095418508);
//			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4));
//			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3));
//			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4));
//			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3));
//		}
		System.out.println("Tests for parrallel collinear intersecting lines");
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(4,0);

			KPoint p3 = new KPoint(1,0);
			KPoint p4 = new KPoint(2,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,4);

			KPoint p3 = new KPoint(0,1);
			KPoint p4 = new KPoint(0,2);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(4,4);

			KPoint p3 = new KPoint(1,1);
			KPoint p4 = new KPoint(2,2);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(2,0);

			KPoint p3 = new KPoint(1,0);
			KPoint p4 = new KPoint(3,0);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
		{
			KPoint p = new KPoint(0,0);
			KPoint p2 = new KPoint(0,2);

			KPoint p3 = new KPoint(0,1);
			KPoint p4 = new KPoint(0,3);
			System.out.println("KPoint.linesIntersect(p, p2, p3, p4) == "+KPoint.linesIntersect(p, p2, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p, p2, p4, p3) == "+KPoint.linesIntersect(p, p2, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p3, p4) == "+KPoint.linesIntersect(p2, p, p3, p4)+", should be true");
			System.out.println("KPoint.linesIntersect(p2, p, p4, p3) == "+KPoint.linesIntersect(p2, p, p4, p3)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p, p2) == "+KPoint.linesIntersect(p3, p4, p, p2)+", should be true");
			System.out.println("KPoint.linesIntersect(p3, p4, p2, p) == "+KPoint.linesIntersect(p3, p4, p2, p)+", should be true");
			System.out.println("KPoint.linesIntersect(p4, p3, p, p2) == "+KPoint.linesIntersect(p4, p3, p, p2)+", should be true");
		}
	}


}
