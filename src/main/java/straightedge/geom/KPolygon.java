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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

import straightedge.geom.util.Tracker;

/**
 * A cool polygon class that's got some pretty useful geometry methods. Can be
 * drawn and filled by Java2D's java.awt.Graphics object. Note that the polygon
 * can be convex or concave but it should not have intersecting sides.
 *
 * Some code is from here:
 * http://www.cs.princeton.edu/introcs/35purple/Polygon.java.html
 *
 * and Joseph O'Rourke: http://exaflop.org/docs/cgafaq/cga2.html
 *
 * Another good source Paul Bourke:
 * http://local.wasp.uwa.edu.au/~pbourke/geometry/
 *
 * @author Keith Woodward
 */
public class KPolygon implements PolygonHolder, Shape {
	public ArrayList<Vector2f> points;
	public Vector2f center;
	public float area;
	public float radius;
	public float radiusSq;
	public boolean counterClockWise;

	public Object userObject;
	// for use with straightedge.geom.util.Tracker and
	// straightedge.geom.util.TileArray
	public int trackerID = -1;
	public long trackerCounter = -1;
	public boolean trackerAddedStatus = false;

	public KPolygon()
	{
	}

	public KPolygon(ArrayList<Vector2f> pointsList, boolean copyPoints)
	{
		if (pointsList.size() < 3)
		{
			throw new RuntimeException(
					"Minimum of 3 points needed. pointsList.size() == "
							+ pointsList.size());
		}
		this.points = new ArrayList<Vector2f>(pointsList.size());
		for (int i = 0; i < pointsList.size(); i++)
		{
			Vector2f existingPoint = pointsList.get(i);
			if (copyPoints)
			{
				points.add(new Vector2f(existingPoint));
			} else
			{
				points.add(existingPoint);
			}
		}
		calcAll();
	}

	public KPolygon(ArrayList<Vector2f> pointsList)
	{
		this(pointsList, true);
	}

	public KPolygon(Vector2f[] pointsArray, boolean copyPoints)
	{
		if (pointsArray.length < 3)
		{
			throw new RuntimeException(
					"Minimum of 3 points needed. pointsArray.length == "
							+ pointsArray.length);
		}
		this.points = new ArrayList<Vector2f>(pointsArray.length);
		for (int i = 0; i < pointsArray.length; i++)
		{
			Vector2f existingPoint = pointsArray[i];
			if (copyPoints)
			{
				points.add(new Vector2f(existingPoint));
			} else
			{
				points.add(existingPoint);
			}
		}
		calcAll();
	}

	public KPolygon(Vector2f... pointsArray)
	{
		this(pointsArray, true);
	}

	public KPolygon(KPolygon polygon)
	{
		points = new ArrayList<Vector2f>(polygon.getPoints().size());
		for (int i = 0; i < polygon.getPoints().size(); i++)
		{
			Vector2f existingPoint = polygon.getPoints().get(i);
			points.add(new Vector2f(existingPoint));
		}
		area = polygon.getArea();
		counterClockWise = polygon.isCounterClockWise();
		radius = polygon.getRadius();
		radiusSq = polygon.getRadiusSq();
		center = new Vector2f(polygon.getCenter());
	}
	public static KPolygon createRect(double x, double y, double x2, double y2){
		return createRect((float) x, (float) y, (float) x2, (float) y2);
	}
	public static KPolygon createRect(float x, float y, float x2, float y2)
	{
		// make x and y the bottom left point.
		if (x2 < x)
		{
			float t = x;
			x = x2;
			x2 = t;
		}
		// make x2 and y2 the top right point.
		if (y2 < y)
		{
			float t = y;
			y = y2;
			y2 = t;
		}
		ArrayList<Vector2f> pointList = new ArrayList<Vector2f>();
		pointList.add(new Vector2f(x, y));
		pointList.add(new Vector2f(x2, y));
		pointList.add(new Vector2f(x2, y2));
		pointList.add(new Vector2f(x, y2));
		return new KPolygon(pointList, false);
	}

	public static KPolygon createRect(AABB aabb)
	{
		ArrayList<Vector2f> pointList = new ArrayList<Vector2f>();
		pointList.add(new Vector2f(aabb.p.x, aabb.p.y));
		pointList.add(new Vector2f(aabb.p2.x, aabb.p.y));
		pointList.add(new Vector2f(aabb.p2.x, aabb.p2.y));
		pointList.add(new Vector2f(aabb.p.x, aabb.p2.y));
		return new KPolygon(pointList, false);
	}

	public static KPolygon createRect(Vector2f p, Vector2f p2)
	{
		return createRect(p.x, p.y, p2.x, p2.y);
	}

	public static KPolygon createRect(Vector2f botLeftPoint, float width,
			float height)
	{
		return createRect(botLeftPoint.x, botLeftPoint.y,
				botLeftPoint.x + width, botLeftPoint.y + height);
	}

	public static KPolygon createRectOblique(float x, float y, float x2,
			float y2, float width)
	{
		ArrayList<Vector2f> pointList = new ArrayList<Vector2f>();
		float r = width / 2f;
		float xOffset = 0;
		float yOffset = 0;
		float xDiff = x2 - x;
		float yDiff = y2 - y;
		if (xDiff == 0)
		{
			xOffset = r;
			yOffset = 0;
		} else if (yDiff == 0)
		{
			xOffset = 0;
			yOffset = r;
		} else
		{
			float gradient = (yDiff) / (xDiff);
			xOffset = (float) (r * gradient
					/ (FastMath.sqrt(1 + gradient * gradient)));
			yOffset = -xOffset / gradient;
		}
		// System.out.println(this.getClass().getSimpleName() + ": xOffset ==
		// "+xOffset+", yOffset == "+yOffset);
		pointList.add(new Vector2f(x - xOffset, y - yOffset));
		pointList.add(new Vector2f(x + xOffset, y + yOffset));
		pointList.add(new Vector2f(x2 + xOffset, y2 + yOffset));
		pointList.add(new Vector2f(x2 - xOffset, y2 - yOffset));
		return new KPolygon(pointList, false);
	}

	public static KPolygon createRectOblique(Vector2f p1, Vector2f p2,
			float width)
	{
		return createRectOblique(p1.x, p1.y, p2.x, p2.y, width);
	}

	public static KPolygon createRegularPolygon(int numPoints,
			double distFromCenterToPoints){
		return createRegularPolygon(numPoints,(float)distFromCenterToPoints);
	}
	public static KPolygon createRegularPolygon(int numPoints,
			float distFromCenterToPoints)
	{
		if (numPoints < 3)
		{
			throw new IllegalArgumentException(
					"numPoints must be 3 or more, it can not be " + numPoints
							+ ".");
		}
		ArrayList<Vector2f> pointList = new ArrayList<Vector2f>();
		float angleIncrement = FastMath.PI * 2f / (numPoints);
		float radius = distFromCenterToPoints;
		float currentAngle = 0;
		for (int k = 0; k < numPoints; k++)
		{
			float x = radius * FastMath.cos(currentAngle);
			float y = radius * FastMath.sin(currentAngle);
			pointList.add(new Vector2f(x, y));
			currentAngle += angleIncrement;
		}
		KPolygon createdPolygon = new KPolygon(pointList, false);
		return createdPolygon;
	}

	public ArrayList<Vector2f> getPoints()
	{
		return points;
	}

	// Gives point of intersection with line specified, where intersectoin point
	// returned is the one closest to (x1, y1).
	// null is returned if there is no intersection.
	public Vector2f getClosestIntersectionToFirstFromSecond(float x1, float y1,
			float x2, float y2)
	{
		Vector2f closestIntersectionPoint = null;
		float closestIntersectionDistanceSq = Float.MAX_VALUE;
		int nextI;
		for (int i = 0; i < points.size(); i++)
		{
			nextI = (i + 1 == points.size() ? 0 : i + 1);
			if (Vector2fUtils.linesIntersect(x1, y1, x2, y2, points.get(i).x,points.get(i).y, points.get(nextI).x, points.get(nextI).y))
			{
				Vector2f currentIntersectionPoint = Vector2fUtils
						.getLineLineIntersection(x1, y1, x2, y2,
								points.get(i).x, points.get(i).y,
								points.get(nextI).x, points.get(nextI).y);
				if (currentIntersectionPoint == null)
				{
					continue;
				}
				float currentIntersectionDistanceSq = currentIntersectionPoint
						.distanceSquared(x1, y1);
				if (currentIntersectionDistanceSq < closestIntersectionDistanceSq)
				{
					closestIntersectionPoint = currentIntersectionPoint;
					closestIntersectionDistanceSq = currentIntersectionDistanceSq;
				}
			}
		}
		return closestIntersectionPoint;
	}

	public Vector2f getClosestIntersectionToFirstFromSecond(Vector2f first,
			Vector2f second)
	{
		return getClosestIntersectionToFirstFromSecond(first.x, first.y,
				second.x, second.y);
	}

	public Vector2f getBoundaryPointClosestTo(Vector2f p)
	{
		return getBoundaryPointClosestTo(p.x, p.y);
	}

	public Vector2f getBoundaryPointClosestTo(float x, float y)
	{
		float closestDistanceSq = Float.MAX_VALUE;
		int closestIndex = -1;
		int closestNextIndex = -1;

		int nextI;
		for (int i = 0; i < points.size(); i++)
		{
			nextI = (i + 1 == points.size() ? 0 : i + 1);
			Vector2f p = this.getPoints().get(i);
			Vector2f pNext = this.getPoints().get(nextI);
			float ptSegDistSq = Vector2fUtils.ptSegDistSq(pNext.x,pNext.y, x, y,p.x, p.y);
			if (ptSegDistSq < closestDistanceSq)
			{
				closestDistanceSq = ptSegDistSq;
				closestIndex = i;
				closestNextIndex = nextI;
			}
		}
		Vector2f p = this.getPoints().get(closestIndex);
		Vector2f pNext = this.getPoints().get(closestNextIndex);
		return Vector2fUtils.getClosestPointOnSegment(p.x, p.y, pNext.x,
				pNext.y, x, y);
	}

	public boolean contains(KPolygon foreign)
	{
		if (intersectsPerimeter(foreign))
		{
			return false;
		}
		if (contains(foreign.getPoints().get(0)) == false)
		{
			return false;
		}
		return true;
	}

	public boolean contains(Vector2f p)
	{
		return contains(p.x, p.y);
	}

	// Source code from: http://exaflop.org/docs/cgafaq/cga2.html
	// Subject 2.03: How do I find if a point lies within a polygon?
	// The definitive reference is "Point in Polyon Strategies" by Eric Haines
	// [Gems IV] pp. 24-46. The code in the Sedgewick book Algorithms (2nd
	// Edition, p.354) is incorrect.
	// The essence of the ray-crossing method is as follows. Think of standing
	// inside a field with a fence representing the polygon. Then walk north. If
	// you have to jump the fence you know you are now outside the poly. If you
	// have to cross again you know you are now inside again; i.e., if you were
	// inside the field to start with, the total number of fence jumps you would
	// make will be odd, whereas if you were ouside the jumps will be even.
	// The code below is from Wm. Randolph Franklin <wrf@ecse.rpi.edu> with some
	// minor modifications for speed. It returns 1 for strictly interior points,
	// 0 for strictly exterior, and 0 or 1 for points on the boundary. The
	// boundary behavior is complex but determined; | in particular, for a
	// partition of a region into polygons, each point | is "in" exactly one
	// polygon. See the references below for more detail
	// The code may be further accelerated, at some loss in clarity, by avoiding
	// the central computation when the inequality can be deduced, and by
	// replacing the division by a multiplication for those processors with slow
	// divides.
	// References:
	// [Gems IV] pp. 24-46
	// [O'Rourke] pp. 233-238
	// [Glassner:RayTracing]
	public boolean contains(double x, double y)
	{
		Vector2f pointIBefore = (points.size() != 0
				? points.get(points.size() - 1) : null);
		int crossings = 0;
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f pointI = points.get(i);
			if (((pointIBefore.y <= y && y < pointI.y)
					|| (pointI.y <= y && y < pointIBefore.y))
					&& x < ((pointI.x - pointIBefore.x)
							/ (pointI.y - pointIBefore.y) * (y - pointIBefore.y)
							+ pointIBefore.x))
			{
				crossings++;
			}
			pointIBefore = pointI;
		}
		return (crossings % 2 != 0);
	}

	public Vector2f getPoint(int i)
	{
		return getPoints().get(i);
	}

	public void calcArea()
	{
		float signedArea = getAndCalcSignedArea();
		if (signedArea < 0)
		{
			counterClockWise = false;
		} else
		{
			counterClockWise = true;
		}
		area = FastMath.abs(signedArea);
	}

	public float getAndCalcSignedArea()
	{
		float totalArea = 0;
		for (int i = 0; i < points.size() - 1; i++)
		{
			totalArea += ((points.get(i).x - points.get(i + 1).x)
					* (points.get(i + 1).y
							+ (points.get(i).y - points.get(i + 1).y) / 2));
		}
		// need to do points[point.length-1] and points[0].
		totalArea += ((points.get(points.size() - 1).x - points.get(0).x)
				* (points.get(0).y
						+ (points.get(points.size() - 1).y - points.get(0).y)
								/ 2));
		return totalArea;
	}

	public float[] getBoundsArray()
	{
		return getBoundsArray(new float[4]);
	}

	public float[] getBoundsArray(float[] bounds)
	{
		float leftX = Float.MAX_VALUE;
		float botY = Float.MAX_VALUE;
		float rightX = -Float.MAX_VALUE;
		float topY = -Float.MAX_VALUE;

		for (int i = 0; i < points.size(); i++)
		{
			if (points.get(i).x < leftX)
			{
				leftX = points.get(i).x;
			}
			if (points.get(i).x > rightX)
			{
				rightX = points.get(i).x;
			}
			if (points.get(i).y < botY)
			{
				botY = points.get(i).y;
			}
			if (points.get(i).y > topY)
			{
				topY = points.get(i).y;
			}
		}
		bounds[0] = leftX;
		bounds[1] = botY;
		bounds[2] = rightX;
		bounds[3] = topY;
		return bounds;
	}

	public AABB getAABB()
	{
		float leftX = Float.MAX_VALUE;
		float botY = Float.MAX_VALUE;
		float rightX = -Float.MAX_VALUE;
		float topY = -Float.MAX_VALUE;

		for (int i = 0; i < points.size(); i++)
		{
			if (points.get(i).x < leftX)
			{
				leftX = points.get(i).x;
			}
			if (points.get(i).x > rightX)
			{
				rightX = points.get(i).x;
			}
			if (points.get(i).y < botY)
			{
				botY = points.get(i).y;
			}
			if (points.get(i).y > topY)
			{
				topY = points.get(i).y;
			}
		}
		AABB aabb = new AABB(leftX, botY, rightX, topY);
		return aabb;
	}

	public boolean intersectsPerimeter(KPolygon foreign)
	{
		Vector2f pointIBefore = (points.size() != 0
				? points.get(points.size() - 1) : null);
		Vector2f pointJBefore = (foreign.points.size() != 0
				? foreign.points.get(foreign.points.size() - 1) : null);
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f pointI = points.get(i);
			// int nextI = (i+1 >= points.size() ? 0 : i+1);
			for (int j = 0; j < foreign.points.size(); j++)
			{
				// int nextJ = (j+1 >= foreign.points.size() ? 0 : j+1);
				Vector2f pointJ = foreign.points.get(j);
				// if (Vector2fUtils.linesIntersect(points.get(i).x,
				// points.get(i).y, points.get(nextI).x, points.get(nextI).y,
				// foreign.points.get(j).x, foreign.points.get(j).y,
				// foreign.points.get(nextJ).x, foreign.points.get(nextJ).y)){
				// The below linesIntersect could be sped up slightly since many
				// things are recalc'ed over and over again.
				if (Vector2fUtils.linesIntersect(pointI, pointIBefore, pointJ,
						pointJBefore))
				{
					return true;
				}
				pointJBefore = pointJ;
			}
			pointIBefore = pointI;
		}
		return false;
	}

	public boolean intersects(KPolygon foreign)
	{
		if (intersectsPerimeter(foreign))
		{
			return true;
		}
		if (contains(foreign.getPoint(0)) || foreign.contains(getPoint(0)))
		{
			return true;
		}
		return false;
	}

	public boolean intersectionPossible(KPolygon poly)
	{
		return intersectionPossible(this, poly);
	}

	public static boolean intersectionPossible(KPolygon poly, KPolygon poly2)
	{
		float sumRadiusSq = poly.getRadius() + poly2.getRadius();
		sumRadiusSq *= sumRadiusSq;
		if (poly.getCenter().distanceSquared(poly2.getCenter()) > sumRadiusSq)
		{
			// if (center.distance(foreign.getCenter()) > radius +
			// foreign.getRadius()){
			return false;
		}
		return true;
	}

	public boolean intersectionPossible(Vector2f p1, Vector2f p2)
	{
		return intersectionPossible(p1.x, p1.y, p2.x, p2.y);
	}

	public boolean intersectionPossible(float x1, float y1, float x2, float y2)
	{
		// if (center.ptSegDistSq(x1, y1, x2, y2) > radiusSq){
		if (Vector2fUtils.ptSegDistSq(x1, y1, x2, y2,center.x,center.y) > radiusSq)
		{
			return false;
		}
		return true;
	}

	public boolean intersectsLine(Vector2f p1, Vector2f p2)
	{
		return intersectsLine(p1.x, p1.y, p2.x, p2.y);
	}

	public boolean intersectsLine(float x1, float y1, float x2, float y2)
	{
		// // pretty much just does the following, but with some optimisations
		// by
		// // caching some values normally recalculated in the
		// Vector2fUtils.linesIntersect method:
		// Vector2f pointIBefore = points.get(points.size()-1);
		// for (int i = 0; i < points.size(); i++){
		// Vector2f pointI = points.get(i);
		// if (Vector2fUtils.linesIntersect(x1, y1, x2, y2, pointIBefore.x,
		// pointIBefore.y, pointI.x, pointI.y)){
		// return true;
		// }
		// pointIBefore = pointI;
		// }
		// return false;

		// Sometimes this method fails if the 'lines'
		// start and end on the same point, so here we check for that.
		if (x1 == x2 && y1 == y2)
		{
			return false;
		}
		float ax = x2 - x1;
		float ay = y2 - y1;
		Vector2f pointIBefore = points.get(points.size() - 1);
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f pointI = points.get(i);
			float x3 = pointIBefore.x;
			float y3 = pointIBefore.y;
			float x4 = pointI.x;
			float y4 = pointI.y;

			float bx = x3 - x4;
			float by = y3 - y4;
			float cx = x1 - x3;
			float cy = y1 - y3;

			float alphaNumerator = by * cx - bx * cy;
			float commonDenominator = ay * bx - ax * by;
			if (commonDenominator > 0)
			{
				if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
				{
					pointIBefore = pointI;
					continue;
				}
			} else if (commonDenominator < 0)
			{
				if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
				{
					pointIBefore = pointI;
					continue;
				}
			}
			float betaNumerator = ax * cy - ay * cx;
			if (commonDenominator > 0)
			{
				if (betaNumerator < 0 || betaNumerator > commonDenominator)
				{
					pointIBefore = pointI;
					continue;
				}
			} else if (commonDenominator < 0)
			{
				if (betaNumerator > 0 || betaNumerator < commonDenominator)
				{
					pointIBefore = pointI;
					continue;
				}
			}
			if (commonDenominator == 0)
			{
				// This code wasn't in Franklin Antonio's method. It was added
				// by Keith Woodward.
				// The lines are parallel.
				// Check if they're collinear.
				float collinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3 - y1)
						+ x3 * (y1 - y2); // see
											// http://mathworld.wolfram.com/Collinear.html
				// If p3 is collinear with p1 and p2 then p4 will also be
				// collinear, since p1-p2 is parallel with p3-p4
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
				pointIBefore = pointI;
				continue;
			}
			return true;
		}
		return false;
	}

	public void calcCenter()
	{
		if (center == null)
		{
			center = new Vector2f();
		}
		if (getArea() == 0)
		{
			center.x = points.get(0).x;
			center.y = points.get(0).y;
			return;
		}
		float cx = 0.0f;
		float cy = 0.0f;
		Vector2f pointIBefore = (points.size() != 0
				? points.get(points.size() - 1) : null);
		for (int i = 0; i < points.size(); i++)
		{
			// int nextI = (i+1 >= points.size() ? 0 : i+1);
			// float multiplier = (points.get(i).y * points.get(nextI).x -
			// points.get(i).x * points.get(nextI).y);
			// cx += (points.get(i).x + points.get(nextI).x) * multiplier;
			// cy += (points.get(i).y + points.get(nextI).y) * multiplier;
			Vector2f pointI = points.get(i);
			float multiplier = (pointIBefore.y * pointI.x
					- pointIBefore.x * pointI.y);
			cx += (pointIBefore.x + pointI.x) * multiplier;
			cy += (pointIBefore.y + pointI.y) * multiplier;
			pointIBefore = pointI;
		}
		cx /= (6 * getArea());
		cy /= (6 * getArea());
		if (counterClockWise == true)
		{
			cx *= -1;
			cy *= -1;
		}
		center.x = cx;
		center.y = cy;
	}

	public void calcRadius()
	{
		if (center == null)
		{
			calcCenter();
		}
		float maxRadiusSq = -1;
		int furthestPointIndex = 0;
		for (int i = 0; i < points.size(); i++)
		{
			float currentRadiusSq = (center.distanceSquared(points.get(i)));
			if (currentRadiusSq > maxRadiusSq)
			{
				maxRadiusSq = currentRadiusSq;
				furthestPointIndex = i;
			}
		}
		radius = (center.distance(points.get(furthestPointIndex)));
		radiusSq = radius * radius;
	}

	public void calcAll()
	{
		this.calcArea();
		this.calcCenter();
		this.calcRadius();
	}

	public float getArea()
	{
		return area;
	}

	public Vector2f getCenter()
	{
		return center;
	}

	public float getRadius()
	{
		return radius;
	}

	public float getRadiusSq()
	{
		return radiusSq;
	}

	public float getPerimeter()
	{
		float perimeter = 0;
		for (int i = 0; i < points.size() - 1; i++)
		{
			perimeter += points.get(i).distance(points.get(i + 1));
		}
		perimeter += points.get(points.size()).distance(points.get(0));
		return perimeter;
	}

	public void rotate(float angle)
	{
		rotate(angle, center.x, center.y);
	}
	public void rotate(double angle, Vector2f axle){
		rotate((float)angle,axle);
	}
	public void rotate(float angle, Vector2f axle)
	{
		rotate(angle, axle.x, axle.y);
	}

	public void rotate(float angle, float x, float y)
	{
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f p = points.get(i);
			// p.rotate(angle, x, y);
			Vector2fUtils.rotate(p, angle, x, y);
		}
		// rotate the center if it's not equal to the axle.
		if (x != center.x || y != center.y)
		{
			// center.rotate(angle, x, y);
			Vector2fUtils.rotate(center, angle, x, y);

		}
	}

	public void translate(double x, double y)
	{
		translate((float) x, (float) y);
	}

	public void translate(float x, float y)
	{
		for (int i = 0; i < points.size(); i++)
		{
			points.get(i).x += x;
			points.get(i).y += y;
		}
		center.x += x;
		center.y += y;
	}

	public void translate(Vector2f translation)
	{
		translate(translation.x, translation.y);
	}

	public void translateTo(float x, float y)
	{
		float xIncrement = x - center.x;
		float yIncrement = y - center.y;
		center.x = x;
		center.y = y;
		for (int i = 0; i < points.size(); i++)
		{
			points.get(i).x += xIncrement;
			points.get(i).y += yIncrement;
		}
	}

	public void translateTo(Vector2f newCentre)
	{
		translateTo(newCentre.x, newCentre.y);
	}

	public void translateToOrigin()
	{
		translateTo(0, 0);
	}

	public void scale(double xMultiplier, double yMultiplier, double x, double y)
	{
		double incX;
		double incY;
		for (int i = 0; i < points.size(); i++)
		{
			incX = points.get(i).x - x;
			incY = points.get(i).y - y;
			incX *= xMultiplier;
			incY *= yMultiplier;
			points.get(i).x = (float) (x + incX);
			points.get(i).y = (float) (y + incY);
		}
		incX = center.x - x;
		incY = center.y - y;
		incX *= xMultiplier;
		incY *= yMultiplier;
		center.x = (float) (x + incX);
		center.y = (float) (y + incY);
		this.calcArea();
		this.calcRadius();
	}

	public void scale(double multiplierX, double multiplierY)
	{
		scale(multiplierX, multiplierY, getCenter().x, getCenter().y);
	}

	public void scale(double multiplierX, double multiplierY, Vector2f p)
	{
		scale(multiplierX, multiplierY, p.x, p.y);
	}

	public void scale(double multiplier)
	{
		scale(multiplier, multiplier, getCenter().x, getCenter().y);
	}

	public void scale(double multiplier, Vector2f p)
	{
		scale(multiplier, multiplier, p.x, p.y);
	}

	public Vector2f getBoundaryPointFromCenterToward(Vector2f endPoint)
	{
		float distToExtendOutTo = 3 * getRadius();
		float xCoord = getCenter().x;
		float yCoord = getCenter().y;
		float xDiff = endPoint.x - getCenter().x;
		float yDiff = endPoint.y - getCenter().y;
		if (xDiff == 0 && yDiff == 0)
		{
			yCoord += distToExtendOutTo;
		} else if (xDiff == 0)
		{
			yCoord += distToExtendOutTo * FastMath.sign(yDiff);
		} else if (yDiff == 0)
		{
			xCoord += distToExtendOutTo * FastMath.sign(xDiff);
		} else
		{
			xCoord += distToExtendOutTo * FastMath.abs(xDiff / (xDiff + yDiff))
					* FastMath.sign(xDiff);
			yCoord += distToExtendOutTo * FastMath.abs(yDiff / (xDiff + yDiff))
					* FastMath.sign(yDiff);
		}
		Vector2f boundaryPoint = getClosestIntersectionToFirstFromSecond(
				getCenter().x, getCenter().y, xCoord, yCoord);
		return boundaryPoint;
	}

	public boolean isCounterClockWise()
	{
		return counterClockWise;
	}

	public void reversePointOrder()
	{
		counterClockWise = !counterClockWise;
		ArrayList<Vector2f> tempPoints = new ArrayList<Vector2f>(points.size());
		for (int i = points.size() - 1; i >= 0; i--)
		{
			tempPoints.add(points.get(i));
		}
		points.clear();
		points.addAll(tempPoints);
	}

	public boolean isValidNoLineIntersections()
	{
		return isValidNoLineIntersections(points);
	}

	public static boolean isValidNoLineIntersections(ArrayList<Vector2f> points)
	{
		for (int i = 0; i < points.size(); i++)
		{
			int iPlus = (i + 1 >= points.size() ? 0 : i + 1);
			for (int j = i + 2; j < points.size(); j++)
			{
				int jPlus = (j + 1 >= points.size() ? 0 : j + 1);
				if (i == jPlus)
				{
					continue;
				}
				if (Vector2fUtils.linesIntersect(points.get(i),
						points.get(iPlus), points.get(j), points.get(jPlus)))
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean isValidNoConsecutiveEqualPoints()
	{
		return isValidNoConsecutiveEqualPoints(points);
	}

	public static boolean isValidNoConsecutiveEqualPoints(
			ArrayList<Vector2f> points)
	{
		Vector2f pointIBefore = (points.size() != 0
				? points.get(points.size() - 1) : null);
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f pointI = points.get(i);
			if (pointI.x == pointIBefore.x && pointI.y == pointIBefore.y)
			{
				return false;
			}
		}
		return true;
	}

	public boolean isValidNoEqualPoints()
	{
		return isValidNoEqualPoints(points);
	}

	public static boolean isValidNoEqualPoints(ArrayList<Vector2f> points)
	{
		for (int i = 0; i < points.size(); i++)
		{
			Vector2f pointI = points.get(i);
			for (int j = i + 1; j < points.size(); j++)
			{
				Vector2f pointJ = points.get(j);
				if (pointI.x == pointJ.x && pointI.y == pointJ.y)
				{
					return false;
				}
			}
		}
		return true;
	}

	public static boolean printOffendingIntersectingLines(
			ArrayList<Vector2f> points)
	{
		boolean linesIntersect = false;
		for (int i = 0; i < points.size(); i++)
		{
			int iPlus = (i + 1 >= points.size() ? 0 : i + 1);
			for (int j = i + 2; j < points.size(); j++)
			{
				int jPlus = (j + 1 >= points.size() ? 0 : j + 1);
				if (i == jPlus)
				{
					continue;
				}
				if (Vector2fUtils.linesIntersect(points.get(i),
						points.get(iPlus), points.get(j), points.get(jPlus)))
				{
					System.out.println(KPolygon.class.getSimpleName()
							+ ": the line between points.get(" + i
							+ ") & points.get(" + iPlus
							+ ") intersects with the line between points.get("
							+ j + ") & points.get(" + jPlus + ")");
					System.out.println(KPolygon.class.getSimpleName()
							+ ": the line between points.get(" + i + ") == "
							+ points.get(i));
					System.out.println(KPolygon.class.getSimpleName()
							+ ": the line between points.get(" + iPlus + ") == "
							+ points.get(iPlus));
					System.out.println(KPolygon.class.getSimpleName()
							+ ": the line between points.get(" + j + ") == "
							+ points.get(j));
					System.out.println(KPolygon.class.getSimpleName()
							+ ": the line between points.get(" + jPlus + ") == "
							+ points.get(jPlus));
					linesIntersect = true;
				}
			}
		}
		return linesIntersect;
	}

	public KPolygon copy()
	{
		KPolygon polygon = new KPolygon(this);
		return polygon;
	}

	/**
	 * Needed by PolygonHolder.
	 * 
	 * @return This KPolygon.
	 */
	public KPolygon getPolygon()
	{
		return this;
	}

	public void setTileArraySearchStatus(boolean trackerAddedStatus,
			Tracker tracker)
	{
		this.trackerAddedStatus = trackerAddedStatus;
		this.trackerCounter = tracker.getCounter();
		this.trackerID = tracker.getID();
	}

	public boolean isTileArraySearchStatusAdded(Tracker tracker)
	{
		if (this.trackerCounter == tracker.getCounter()
				&& this.trackerID == tracker.getID())
		{
			return trackerAddedStatus;
		} else
		{
			return false;
		}
	}

	public Object getUserObject()
	{
		return userObject;
	}

	public void setUserObject(Object userObject)
	{
		this.userObject = userObject;
	}

	public int getNextIndex(int i)
	{
		int iPlus = i + 1;
		return (iPlus >= points.size() ? 0 : iPlus);
	}

	public int getPrevIndex(int i)
	{
		int iMinus = i - 1;
		return (iMinus < 0 ? points.size() - 1 : iMinus);
	}

	public Vector2f getNextPoint(int i)
	{
		return points.get(getNextIndex(i));
	}

	public Vector2f getPrevPoint(int i)
	{
		return points.get(getPrevIndex(i));
	}

	public String toString()
	{
		String str = getClass().getName() + "@"
				+ Integer.toHexString(hashCode());
		if (getCenter() != null)
		{
			str += ", center == " + getCenter().toString();
		}
		str += ", area == " + area;
		str += ", radius == " + radius;
		if (points != null)
		{
			// str += ", points == " + points.toString();
			str += ", points.size() == " + points.size() + ":\n";
			for (int i = 0; i < points.size(); i++)
			{
				Vector2f p = points.get(i);
				str += "  i == " + i + ", " + p + "\n";
			}
		}
		return str;
	}

	// Note: The following methods are neded to implement java.awt.geom.Shape.
	public Rectangle2D.Double getBounds2D()
	{
		float[] bounds = getBoundsArray();
		return new Rectangle2D.Double(bounds[0], bounds[1], bounds[2],
				bounds[3]);
	}

	public Rectangle getBounds()
	{
		float[] bounds = getBoundsArray();
		return new Rectangle((int) (bounds[0]), (int) (bounds[1]),
				(int) FastMath.ceil(bounds[2]), (int) FastMath.ceil(bounds[3]));
	}

	/**
	 * Unlike Shape.intersects, this method is exact. Note that this method
	 * should really be called overlaps(x,y,w,h) since it doesn't just test for
	 * line-line intersection.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return Returns true if the given rectangle overlaps this polygon.
	 */
	public boolean intersects(double x, double y, double w, double h)
	{
		if (x + w < center.x - radius ||
				x > center.x + radius ||
				y + h < center.y - radius ||
				y > center.y + radius)
		{
			return false;
		}
		for (int i = 0; i < points.size(); i++)
		{
			int nextI = (i + 1 >= points.size() ? 0 : i + 1);
			if (Vector2fUtils.linesIntersect(x, y, x + w, y, points.get(i).x,
					points.get(i).y, points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x, y, x, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x, y + h, x + w, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x + w, y, x + w, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y))
			{
				return true;
			}
		}
		float px = points.get(0).x;
		float py = points.get(0).y;
		if (px > x && px < x + w && py > y && py < y + h)
		{
			return true;
		}
		if (contains(x, y) == true)
		{
			return true;
		}
		return false;
	}

	public boolean intersects(Rectangle2D r)
	{
		return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public boolean contains(Point2D p)
	{
		return contains(p.getX(), p.getY());
	}

	/**
	 * Unlike Shape.contains, this method is exact.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return Returns true if the given rectangle wholly fits inside this
	 *         polygon with no perimeter intersections.
	 */
	public boolean contains(double x, double y, double w, double h)
	{
		if (x + w < center.x - radius ||
				x > center.x + radius ||
				y + h < center.y - radius ||
				y > center.y + radius)
		{
			return false;
		}
		for (int i = 0; i < points.size(); i++)
		{
			int nextI = (i + 1 >= points.size() ? 0 : i + 1);
			if (Vector2fUtils.linesIntersect(x, y, x + w, y, points.get(i).x,
					points.get(i).y, points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x, y, x, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x, y + h, x + w, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y)
					||
					Vector2fUtils.linesIntersect(x + w, y, x + w, y + h,
							points.get(i).x, points.get(i).y,
							points.get(nextI).x, points.get(nextI).y))
			{
				return false;
			}
		}
		float px = points.get(0).x;
		float py = points.get(0).y;
		if (px > x && px < x + w && py > y && py < y + h)
		{
			return false;
		}
		if (contains(x, y) == true)
		{
			return true;
		}
		return false;
	}

	public boolean contains(Rectangle2D r)
	{
		return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public PathIterator getPathIterator(AffineTransform at)
	{
		return new KPolygonIterator(this, at);
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness)
	{
		return new KPolygonIterator(this, at);
	}

	public class KPolygonIterator implements PathIterator {
		int type = PathIterator.SEG_MOVETO;
		int index = 0;
		KPolygon polygon;
		Vector2f currentPoint;
		AffineTransform affine;

		float[] singlePointSetDouble = new float[2];

		KPolygonIterator(KPolygon kPolygon)
		{
			this(kPolygon, null);
		}

		KPolygonIterator(KPolygon kPolygon, AffineTransform at)
		{
			this.polygon = kPolygon;
			this.affine = at;
			currentPoint = polygon.getPoint(0);
		}

		public int getWindingRule()
		{
			return PathIterator.WIND_EVEN_ODD;
		}

		public boolean isDone()
		{
			if (index == polygon.points.size() + 1)
			{
				return true;
			}
			return false;
		}

		public void next()
		{
			index++;
		}

		public void assignPointAndType()
		{
			if (index == 0)
			{
				currentPoint = polygon.getPoint(0);
				type = PathIterator.SEG_MOVETO;
			} else if (index == polygon.points.size())
			{
				type = PathIterator.SEG_CLOSE;
			} else
			{
				currentPoint = polygon.getPoint(index);
				type = PathIterator.SEG_LINETO;
			}
			// if (index == 0){
			// currentPoint = polygon.getPoint(0);
			// type = PathIterator.SEG_MOVETO;
			// } else if (index == polygon.points.size()+1){
			// type = PathIterator.SEG_CLOSE;
			// } else if (index == polygon.points.size()){
			// currentPoint = polygon.getPoint(0);
			// type = PathIterator.SEG_LINETO;
			// } else{
			// currentPoint = polygon.getPoint(index);
			// type = PathIterator.SEG_LINETO;
			// }
		}

		public int currentSegment(float[] coords)
		{
			assignPointAndType();
			if (type != PathIterator.SEG_CLOSE)
			{
				if (affine != null)
				{
					float[] singlePointSetFloat = new float[2];
					singlePointSetFloat[0] = (float) currentPoint.x;
					singlePointSetFloat[1] = (float) currentPoint.y;
					affine.transform(singlePointSetFloat, 0, coords, 0, 1);
				} else
				{
					coords[0] = (float) currentPoint.x;
					coords[1] = (float) currentPoint.y;
				}
			}
			return type;
		}

		public int currentSegment(double[] coords)
		{
			assignPointAndType();
			if (type != PathIterator.SEG_CLOSE)
			{
				if (affine != null)
				{
					singlePointSetDouble[0] = currentPoint.x;
					singlePointSetDouble[1] = currentPoint.y;
					affine.transform(singlePointSetDouble, 0, coords, 0, 1);
				} else
				{
					coords[0] = currentPoint.x;
					coords[1] = currentPoint.y;
				}
			}
			return type;
		}
	}

}
