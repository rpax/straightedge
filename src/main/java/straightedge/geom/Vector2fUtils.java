package straightedge.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

public class Vector2fUtils {
	private static final double TWO_PI = Math.PI * 2;

	public static float findAngle(double x1, double y1, double x2, double y2)
	{
		double angle = findSignedAngle(x1, y1, x2, y2);
		if (angle < 0)
		{
			angle += TWO_PI;
		}
		return (float) angle;
	}

	public static float findAngle(Vector2f start, Vector2f dest)
	{
		return findAngle(start.x, start.y, dest.x, dest.y);
	}

	public static double findSignedAngle(double x1, double y1, double x2,
			double y2)
	{
		double x = x2 - x1;
		double y = y2 - y1;
		double angle = (Math.atan2(y, x));
		return angle;
	}

	/**
	 * Returns a positive double if (px, py) is counter-clockwise to (x2, y2)
	 * relative to (x1, y1). in the cartesian coordinate space (positive x-axis
	 * extends right, positive y-axis extends up). Returns a negative double if
	 * (px, py) is clockwise to (x2, y2) relative to (x1, y1). Returns a 0.0 if
	 * (px, py), (x1, y1) and (x2, y2) are collinear. Note that this method
	 * gives different results to java.awt.geom.Line2D.relativeCCW() since
	 * Java2D uses a different coordinate system (positive x-axis extends right,
	 * positive y-axis extends down).
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param px
	 * @param py
	 * @return
	 */
	public static int relCCW(
			double x1, double y1,
			double x2, double y2,
			double px, double py)
	{
		double ccw = relCCWDouble(x1, y1, x2, y2, px, py);
		return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
	}

	public static int relCCW(
			Vector2f v1,
			Vector2f v2,
			double px, double py)
	{
		double ccw = relCCWDouble(v1.x, v1.y, v2.x, v2.y, px, py);
		return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
	}

	public static double relCCWDouble(
			Vector2f v1,
			Vector2f v2,
			double px, double py)
	{
		return relCCWDouble(v1.x, v1.y, v2.x, v2.y, px, py);
	}

	public static double relCCWDouble(
			double x1, double y1,
			double x2, double y2,
			double px, double py)
	{
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;
		double ccw = py * x2 - px * y2;
		return ccw;
	}

	public static double relCCWDoubleExtra(double x1, double y1,
			double x2, double y2,
			double px, double py)
	{
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;
		double ccw = py * x2 - px * y2;
		if (ccw == 0.0)
		{
			// The point is colinear, classify based on which side of
			// the segment the point falls on. We can calculate a
			// relative value using the projection of px,py onto the
			// segment - a negative value indicates the point projects
			// outside of the segment in the direction of the particular
			// endpoint used as the origin for the projection.
			ccw = px * x2 + py * y2;
			if (ccw > 0.0)
			{
				// Reverse the projection to be relative to the original x2,y2
				// x2 and y2 are simply negated.
				// px and py need to have (x2 - x1) or (y2 - y1) subtracted
				// from them (based on the original values)
				// Since we really want to get a positive answer when the
				// point is "beyond (x2,y2)", then we want to calculate
				// the inverse anyway - thus we leave x2 & y2 negated.
				px -= x2;
				py -= y2;
				ccw = px * x2 + py * y2;
				if (ccw < 0.0)
				{
					ccw = 0.0;
				}
			}
		}
		return ccw;
	}

	public static double findSignedRelativeAngle(Vector2f point, Vector2f start,
			Vector2f end)
	{
		return findSignedRelativeAngle(point.x, point.y, start.x, start.y,
				end.x, end.y);
	}

	public static double findSignedRelativeAngle(double x, double y, double x1,
			double y1, double x2, double y2)
	{
		double lineAngle = findAngle(x1, y1, x2, y2);
		double pointAngle = findAngle(x1, y1, x, y);
		if (pointAngle < lineAngle)
		{
			pointAngle += TWO_PI;
		}
		double relativePointAngle = pointAngle - lineAngle;
		if (relativePointAngle > Math.PI)
		{
			relativePointAngle -= TWO_PI;
		}
		assert relativePointAngle <= Math.PI
				&& relativePointAngle >= -Math.PI : relativePointAngle;
		return relativePointAngle;
	}

	public static double findRelativeAngle(Vector2f point, Vector2f start,
			Vector2f end)
	{
		return findRelativeAngle(point.x, point.y, start.x, start.y, end.x,
				end.y);
	}

	public static double findRelativeAngle(double x, double y, double x1,
			double y1, double x2, double y2)
	{
		double relativePointAngle = findSignedRelativeAngle(x, y, x1, y1, x2,
				y2);
		if (relativePointAngle < -Math.PI)
		{
			relativePointAngle += TWO_PI;
		}
		assert relativePointAngle <= 2 * Math.PI
				&& relativePointAngle >= 0 : relativePointAngle;
		return relativePointAngle;
	}

	public static Vector2f midPoint(Vector2f p, Vector2f p2)
	{
		return midPoint(p.x, p.y, p2.x, p2.y);
	}

	public static Vector2f midPoint(double x, double y, double x2, double y2)
	{
		return newVector2f((x + x2) / 2f, (y + y2) / 2f);
	}

	public static Vector2f createPointFromAngle(double x, double y,
			double angle,
			double distance)
	{
		Vector2f p = newVector2f();
		double xDist = Math.cos(angle) * distance;
		double yDist = Math.sin(angle) * distance;
		p.x = (float) (x + xDist);
		p.y = (float) (y + yDist);
		return p;
	}

	public static Vector2f createPointToward(
			double x, double y,
			double x2, double y2,
			double distance)
	{
		Vector2f p = newVector2f();
		double xDiff = (x2 - x);
		double yDiff = (y2 - y);
		double ptDist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		double distOnPtDist = distance / ptDist;
		double xDist = xDiff * distOnPtDist;
		double yDist = yDiff * distOnPtDist;
		p.x = (float) (x + xDist);
		p.y = (float) (y + yDist);
		return p;
	}

	public static Vector2f createPointToward(
			Vector2f v1,
			Vector2f v2,
			double distance)
	{
		return createPointToward(v1.x, v1.y, v2.x, v2.y, distance);
	}

	public static double ptLineDist(Vector2f start, Vector2f end, Vector2f p)
	{
		return ptLineDist(start.x, start.y, end.x, end.y, p.x, p.y);
	}

	public static double ptLineDist(double x1, double y1, double x2, double y2,
			double px, double py)
	{
		return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py));
	}

	public static double ptLineDistSq(Vector2f start, Vector2f end, Vector2f p)
	{
		return ptLineDistSq(start.x, start.y, end.x, end.y, p.x, p.y);
	}

	public static double ptLineDistSq(double x1, double y1, double x2,
			double y2, double px, double py)
	{
		// from: Line2D.Float.ptLineDistSq(x1, y1, x2, y2, px, py);
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
		if (lenSq < 0)
		{
			lenSq = 0;
		}
		return lenSq;
	}

	public static void rotate(Vector2f p, double angle, double xCenter,
			double yCenter)
	{
		double currentAngle;
		double distance;
		currentAngle = Math.atan2(p.y - yCenter, p.x - xCenter);
		currentAngle += angle;
		distance = distance(p.x, p.y, xCenter, yCenter);
		p.x = (float) (xCenter + (distance * Math.cos(currentAngle)));
		p.y = (float) (yCenter + (distance * Math.sin(currentAngle)));
	}

	public static float distance(double x, double y, double x2,
			double y2)
	{
		return FastMath.sqrt(distanceSq(x, y, x2, y2));
	}

	public static double ptSegDist(Vector2f start, Vector2f end, Vector2f p)
	{
		return ptSegDist(start.x, start.y, end.x, end.y, p.x, p.y);
	}

	public static double ptSegDist(double x1, double y1, double x2, double y2,
			double px, double py)
	{
		return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py));
	}

	public static double ptSegDistSq(Vector2f start, Vector2f end, Vector2f p)
	{
		return ptSegDistSq(start.x, start.y, end.x, end.y, p.x, p.y);
	}

	public static float ptSegDistSq(double x1, double y1, double x2, double y2,
			double px, double py)
	{
		// from: Line2D.Float.ptSegDistSq(x1, y1, x2, y2, px, py);
		// Adjust vectors relative to x1,y1
		// x2,y2 becomes relative vector from x1,y1 to end of segment
		x2 -= x1;
		y2 -= y1;
		// px,py becomes relative vector from x1,y1 to test point
		px -= x1;
		py -= y1;
		double dotprod = px * x2 + py * y2;
		double projlenSq;
		if (dotprod <= 0.0)
		{
			// px,py is on the side of x1,y1 away from x2,y2
			// distance to segment is length of px,py vector
			// "length of its (clipped) projection" is now 0.0
			projlenSq = 0.0;
		} else
		{
			// switch to backwards vectors relative to x2,y2
			// x2,y2 are already the negative of x1,y1=>x2,y2
			// to get px,py to be the negative of px,py=>x2,y2
			// the dot product of two negated vectors is the same
			// as the dot product of the two normal vectors
			px = x2 - px;
			py = y2 - py;
			dotprod = px * x2 + py * y2;
			if (dotprod <= 0.0)
			{
				// px,py is on the side of x2,y2 away from x1,y1
				// distance to segment is length of (backwards) px,py vector
				// "length of its (clipped) projection" is now 0.0
				projlenSq = 0.0;
			} else
			{
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
		// of the line segment).
		double lenSq = px * px + py * py - projlenSq;
		if (lenSq < 0)
		{
			lenSq = 0;
		}
		return (float) lenSq;
	}

	public static double findAngleFromOrigin(Vector2f v)
	{
		double angle = findSignedAngleFromOrigin(v);
		if (angle < 0)
		{
			angle += TWO_PI;
		}
		return angle;
	}

	public static float findSignedAngleFromOrigin(Vector2f v)
	{
		return (float)Math.atan2(v.y, v.x);
	}

	public static Vector2f getClosestPointOnSegment(double x1, double y1,
			double x2, double y2, double px, double py)
	{
		Vector2f closestPoint = newVector2f();
		double x2LessX1 = x2 - x1;
		double y2LessY1 = y2 - y1;
		double lNum = x2LessX1 * x2LessX1 + y2LessY1 * y2LessY1;
		double rNum = ((px - x1) * x2LessX1 + (py - y1) * y2LessY1) / lNum;
		// double lNum = (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1);
		// double rNum = ((px - x1)*(x2 - x1) + (py - y1)*(y2 - y1)) / lNum;
		if (rNum <= 0)
		{
			closestPoint.x = (float) x1;
			closestPoint.y = (float) y1;
		} else if (rNum >= 1)
		{
			closestPoint.x = (float) x2;
			closestPoint.y = (float) y2;
		} else
		{
			closestPoint.x = (float) (x1 + rNum * x2LessX1);
			closestPoint.y = (float) (y1 + rNum * y2LessY1);
		}
		return closestPoint;
		// from: http://www.codeguru.com/forum/showthread.php?t=194400
		// Let the point be C (Cx,Cy) and the line be AB (Ax,Ay) to (Bx,By).
		// Let P be the point of perpendicular projection of C on AB. The
		// parameter
		// r, which indicates P's position along AB, is computed by the dot
		// product
		// of AC and AB divided by the square of the length of AB:
		//
		// (1) AC dot AB
		// r = ---------
		// ||AB||^2
		//
		// r has the following meaning:
		//
		// r=0 P = A
		// r=1 P = B
		// r<0 P is on the backward extension of AB
		// r>1 P is on the forward extension of AB
		// 0<r<1 P is interior to AB
		//
		// The length of a line segment in d dimensions, AB is computed by:
		//
		// L = sqrt( (Bx-Ax)^2 + (By-Ay)^2 + ... + (Bd-Ad)^2)
		//
		// so in 2D:
		//
		// L = sqrt( (Bx-Ax)^2 + (By-Ay)^2 )
		//
		// and the dot product of two vectors in d dimensions, U dot V is
		// computed:
		//
		// D = (Ux * Vx) + (Uy * Vy) + ... + (Ud * Vd)
		//
		// so in 2D:
		//
		// D = (Ux * Vx) + (Uy * Vy)
		//
		// So (1) expands to:
		//
		// (Cx-Ax)(Bx-Ax) + (Cy-Ay)(By-Ay)
		// r = -------------------------------
		// L^2
		//
		// The point P can then be found:
		//
		// Px = Ax + r(Bx-Ax)
		// Py = Ay + r(By-Ay)
		//
		// And the distance from A to P = r*L.
		//
		// Use another parameter s to indicate the location along PC, with the
		// following meaning:
		// s<0 C is left of AB
		// s>0 C is right of AB
		// s=0 C is on AB
		//
		// Compute s as follows:
		//
		// (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		// s = -----------------------------
		// L^2
		//
		//
		// Then the distance from C to P = |s|*L.
	}

	private static Vector2f newVector2f()
	{
		return new Vector2f();
	}

	private static Vector2f newVector2f(double x, double y)
	{
		return new Vector2f((float) x, (float) y);
	}

	public static boolean linesIntersect(Vector2f p1, Vector2f p2, Vector2f p3,
			Vector2f p4)
	{
		return linesIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	}

	public static boolean linesIntersect(double x1, double y1, double x2,
			double y2,
			double x3, double y3, double x4, double y4)
	{
		// Return false if either of the lines have zero length
		if (x1 == x2 && y1 == y2 ||
				x3 == x4 && y3 == y4)
		{
			return false;
		}
		// Fastest method, based on Franklin Antonio's "Faster Line Segment
		// Intersection" topic "in Graphics Gems III" book
		// (http://www.graphicsgems.org/)
		double ax = x2 - x1;
		double ay = y2 - y1;
		double bx = x3 - x4;
		double by = y3 - y4;
		double cx = x1 - x3;
		double cy = y1 - y3;

		double alphaNumerator = by * cx - bx * cy;
		double commonDenominator = ay * bx - ax * by;
		if (commonDenominator > 0)
		{
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
			{
				return false;
			}
		} else if (commonDenominator < 0)
		{
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
			{
				return false;
			}
		}
		double betaNumerator = ax * cy - ay * cx;
		if (commonDenominator > 0)
		{
			if (betaNumerator < 0 || betaNumerator > commonDenominator)
			{
				return false;
			}
		} else if (commonDenominator < 0)
		{
			if (betaNumerator > 0 || betaNumerator < commonDenominator)
			{
				return false;
			}
		}
		// if commonDenominator == 0 then the lines are parallel.
		if (commonDenominator == 0)
		{
			// This code wasn't in Franklin Antonio's method. It was added by
			// Keith Woodward.
			// The lines are parallel.
			// Check if they're collinear.
			double collinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3 - y1)
					+ x3 * (y1 - y2); // see
										// http://mathworld.wolfram.com/Collinear.html
			// If p3 is collinear with p1 and p2 then p4 will also be collinear,
			// since p1-p2 is parallel with p3-p4
			if (collinearityTestForP3 == 0)
			{
				// The lines are collinear. Now check if they overlap.
				if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 ||
						x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4 ||
						x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2)
				{
					if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 ||
							y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4 ||
							y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2)
					{
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	public static Vector2f getLineLineIntersection(Vector2f p1, Vector2f p2,
			Vector2f p3, Vector2f p4)
	{
		return getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x,
				p4.y);
	}

	public static Vector2f getLineLineIntersection(double x1, double y1,
			double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double det1And2 = det(x1, y1, x2, y2);
		double det3And4 = det(x3, y3, x4, y4);
		double x1LessX2 = x1 - x2;
		double y1LessY2 = y1 - y2;
		double x3LessX4 = x3 - x4;
		double y3LessY4 = y3 - y4;
		double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
		if (det1Less2And3Less4 == 0)
		{
			// the denominator is zero so the lines are parallel and there's
			// either no solution (or multiple solutions if the lines overlap)
			// so return null.
			return null;
		}
		double x = (det(det1And2, x1LessX2,
				det3And4, x3LessX4) /
				det1Less2And3Less4);
		double y = (det(det1And2, y1LessY2,
				det3And4, y3LessY4) /
				det1Less2And3Less4);

		return newVector2f(x, y);
	}

	protected static double det(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}

	public static float distanceSq(double x, double y, double x2, double y2)
	{
		double dx = x - x2;
		double dy = y - y2;
		return (float) (dx * dx + dy * dy);
	}
}
