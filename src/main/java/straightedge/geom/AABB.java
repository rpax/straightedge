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

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import com.jme3.math.Vector2f;

/**
 * An implementation of an Axis Aligned Bounding Box Point p is the bottom-left
 * point of the box in the normal cartesian plane with positive Y up and
 * positive X right. Point p2 is the top-right point of the box.
 *
 * @author Keith
 */
public class AABB {
	public Vector2f p; // bottom left point
	public Vector2f p2; // top right point

	public static final int UP = 100;
	public static final int DOWN = 101;
	public static final int LEFT = 102;
	public static final int RIGHT = 103;

	public AABB()
	{
		p = new Vector2f();
		p2 = new Vector2f();
	}

	public AABB(float x, float y, float x2, float y2)
	{
		p = new Vector2f(x, y);
		p2 = new Vector2f(x2, y2);
	}

	public AABB(Vector2f p, Vector2f p2, boolean copyPoints)
	{
		if (copyPoints == false)
		{
			this.p = p;
			this.p2 = p2;
		} else
		{
			this.p = new Vector2f(p.x, p.y);
			this.p2 = new Vector2f(p2.x, p2.y);
		}
	}

	public AABB(Vector2f p, Vector2f p2)
	{
		this(p, p2, true);
	}

	public AABB(AABB aabbToCopy)
	{
		this(aabbToCopy.p, aabbToCopy.p2, true);
	}

	/**
	 * Creates a Rect which will have its x and y coordinates in the bottom left
	 * corner of the cartesian coordinate system. Note that this will be in the
	 * top left corner of the Java2D coordinate system.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static AABB createFromDiagonal(float x1, float y1, float x2,
			float y2)
	{
		AABB rect = new AABB();
		rect.setFromDiagonal(x1, y1, x2, y2);
		return rect;
	}

	public static AABB createFromDiagonal(Vector2f p, Vector2f p2)
	{
		return createFromDiagonal(p.x, p.y, p2.x, p2.y);
	}

	public static AABB createFromXYWH(float x, float y, float w, float h)
	{
		AABB rect = new AABB();
		rect.setFromXYWH(x, y, w, h);
		return rect;
	}

	public void setFromDiagonal(float x1, float y1, float x2, float y2)
	{
		if (x2 < x1)
		{
			float t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y2 < y1)
		{
			float t = y1;
			y1 = y2;
			y2 = t;
		}
		setX(x1);
		setY(y1);
		setX2(x2);
		setY2(y2);
	}

	public void setFromDiagonal(Vector2f p, Vector2f p2)
	{
		setFromDiagonal(p.x, p.y, p2.x, p2.y);
	}

	public void setFromXYWH(float x, float y, float w, float h)
	{
		if (w < 0)
		{
			x = x + w;
			w = -w;
		}
		if (h < 0)
		{
			y = y + h;
			h = -h;
		}
		setX(x);
		setY(y);
		setX2(x + w);
		setY2(y + h);
	}

	public void setFromXYWH(Vector2f p, float w, float h)
	{
		setFromXYWH(p.x, p.y, w, h);
	}

	public Vector2f getBotLeft()
	{
		return p;
	}

	public Vector2f getTopRight()
	{
		return p2;
	}

	public float h()
	{
		return p2.y - p.y;
	}

	public float w()
	{
		return p2.x - p.x;
	}

	public float x()
	{
		return p.x;
	}

	public float y()
	{
		return p.y;
	}

	public float x2()
	{
		return p2.x;
	}

	public float y2()
	{
		return p2.y;
	}

	public float getHeight()
	{
		return h();
	}

	public float getWidth()
	{
		return w();
	}

	public void setH(float h)
	{
		p2.y = p.y + h;
	}

	public void setW(float w)
	{
		p2.x = p.x + w;
	}

	public float getX()
	{
		return p.x;
	}

	public void setX(float x)
	{
		p.x = x;
	}

	public float getY()
	{
		return p.y;
	}

	public void setY(float y)
	{
		p.y = y;
	}

	public float getX2()
	{
		return p2.x;
	}

	public void setX2(float x)
	{
		p2.x = x;
	}

	public float getY2()
	{
		return p2.y;
	}

	public void setY2(float y)
	{
		p2.y = y;
	}

	public AABB copy()
	{
		return new AABB(this);
	}

	public String toString()
	{
		return "" + p.x + ", " + p.y + ",  " + p2.x + ", " + p2.y;
	}

	public Vector2f getCenter()
	{
		return new Vector2f(getCenterX(), getCenterY());
	}

	public float getCenterX()
	{
		return (p.x + p2.x) / 2;
	}

	public float getCenterY()
	{
		return (p.y + p2.y) / 2;
	}

	public void setCenter(Vector2f c)
	{
		setCenterX(c.x);
		setCenterY(c.y);
	}

	public void setCenterX(float newCentreX)
	{
		float w = w();
		p.x = newCentreX - w / 2;
		p2.x = newCentreX + w / 2;
	}

	public void setCenterY(float newCentreY)
	{
		float h = h();
		p.y = newCentreY - h / 2;
		p2.y = newCentreY + h / 2;
	}

	// this assumes that there is an intersection. b1 is moving in 'direction'
	// and b2 is stationary
	public static float distanceUntilIntersection(AABB b1, int direction,
			AABB b2)
	{
		if (direction == RIGHT)
		{
			return b2.p.x - (b1.p2.x);
		} else if (direction == LEFT)
		{
			return b1.p.x - (b2.p2.x);
		} else if (direction == DOWN)
		{
			return b2.p.y - (b1.p2.y);
		} else if (direction == UP)
		{
			return b1.p.y - (b2.p2.y);
		}
		return -1f;
	}

	protected static boolean isBetween(float v, float n1, float n2)
	{
		if (v >= n1 && v <= n2)
		{
			return true;
		}
		return false;
	}

	public boolean isFacingBox(int directionThisBoxIsFacing, AABB b2)
	{
		float cx = getCenterX();
		float cy = getCenterY();
		if (directionThisBoxIsFacing == RIGHT)
		{
			if (isBetween(p.y, b2.p.y, b2.p2.y)
					|| isBetween(p2.y, b2.p.y, b2.p2.y) ||
					isBetween(b2.p.y, p.y, p2.y)
					|| isBetween(b2.p2.y, p.y, p2.y))
			{
				if (cx < b2.getCenterX())
				{
					return true;
				}
			}
		} else if (directionThisBoxIsFacing == LEFT)
		{
			if (isBetween(p.y, b2.p.y, b2.p2.y)
					|| isBetween(p2.y, b2.p.y, b2.p2.y) ||
					isBetween(b2.p.y, p.y, p2.y)
					|| isBetween(b2.p2.y, p.y, p2.y))
			{
				if (cx > b2.getCenterX())
				{
					return true;
				}
			}
		} else if (directionThisBoxIsFacing == DOWN)
		{
			if (isBetween(p.x, b2.p.x, b2.p2.x)
					|| isBetween(p2.x, b2.p.x, b2.p2.x) ||
					isBetween(b2.p.x, p.x, p2.x)
					|| isBetween(b2.p2.x, p.x, p2.x))
			{
				if (cy < b2.getCenterY())
				{
					return true;
				}
			}
		} else if (directionThisBoxIsFacing == UP)
		{
			if (isBetween(p.x, b2.p.x, b2.p2.x)
					|| isBetween(p.x, b2.p.x, b2.p2.x) ||
					isBetween(b2.p.x, p.x, p2.x)
					|| isBetween(b2.p2.x, p.x, p2.x))
			{
				if (cy > b2.getCenterY())
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(Vector2f p)
	{
		return contains(p.x, p.y);
	}

	public boolean contains(float px, float py)
	{
		return (px >= p.x &&
				py >= p.y &&
				px <= p2.x &&
				py <= p2.y);
	}

	public boolean intersects(AABB box)
	{
		return intersects(box.p.x, box.p.y, box.p2.x, box.p2.y);
	}

	public boolean intersects(float x, float y, float x2, float y2)
	{
		return intersects(p.x, p.y, p2.x, p2.y, x, y, x2, y2);
	}

	public static boolean intersects(float leftX, float botY, float rightX,
			float topY,
			float leftX2, float botY2, float rightX2, float topY2)
	{
		return (rightX >= leftX2 &&
				topY >= botY2 &&
				leftX <= rightX2 &&
				botY <= topY2);
	}

	public boolean contains(float x, float y, float x2, float y2)
	{
		return (p.x <= x &&
				p.y <= y &&
				p2.x >= x2 &&
				p2.y >= y2);
	}

	public static void union(AABB src1, AABB src2, AABB dest)
	{
		float x1 = Math.min(src1.getX(), src2.getX());
		float y1 = Math.min(src1.getY(), src2.getY());
		float x2 = Math.max(src1.getX2(), src2.getX2());
		float y2 = Math.max(src1.getY2(), src2.getY2());
		dest.setFromDiagonal(x1, y1, x2, y2);
	}

	public AABB union(AABB src1)
	{
		AABB aabb = new AABB();
		union(this, src1, aabb);
		return aabb;
	}

	public static AABB union(AABB src1, AABB src2)
	{
		AABB aabb = new AABB();
		union(src1, src2, aabb);
		return aabb;
	}

	public KPolygon createPolygon()
	{
		return KPolygon.createRect(this);
	}

	public AABB createFromPolygon(KPolygon polygon)
	{
		return polygon.getAABB();
	}

	public boolean isValid()
	{
		if (p2.x < p.x)
		{
			return false;
		}
		if (p2.y < p.y)
		{
			return false;
		}
		return true;
	}

	public Rectangle2D.Double getRectangle2D()
	{
		Rectangle2D.Double rect = new Rectangle2D.Double(p.x, p.y, w(), h());
		return rect;
	}

	public static AABB bufferAndCopy(AABB aabb, float bufferAmount)
	{
		AABB newAABB = new AABB(aabb.p.x + bufferAmount,
				aabb.p.y + bufferAmount, aabb.p2.x + bufferAmount,
				aabb.p2.y + bufferAmount);
		return newAABB;
	}

	public AABB bufferAndCopy(float bufferAmount)
	{
		return bufferAndCopy(this, bufferAmount);
	}

	public static AABB getAABBEnclosingExactPoints(
			Collection<PolygonHolder> polygonHolders)
	{
		return getAABBEnclosingExactPoints(polygonHolders.toArray());
	}

	public static AABB getAABBEnclosingExactPoints(Object[] polygonHolders)
	{
		if (polygonHolders.length == 0)
		{
			Vector2f botLeft = new Vector2f(0, 0);
			Vector2f topRight = new Vector2f(1, 1);
			AABB aabb = new AABB(botLeft, topRight);
			return aabb;
		}
		// find the bounding rectangle
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float[] bounds = new float[4];
		for (int i = 0; i < polygonHolders.length; i++)
		{
			PolygonHolder polygonHolder = (PolygonHolder) polygonHolders[i];
			KPolygon polygon = polygonHolder.getPolygon();
			polygon.getBoundsArray(bounds);
			float leftX = bounds[0];
			float rightX = bounds[2];
			float botY = bounds[1];
			float topY = bounds[3];
			if (leftX < minX)
			{
				minX = leftX;
			}
			if (rightX > maxX)
			{
				maxX = rightX;
			}
			if (botY < minY)
			{
				minY = botY;
			}
			if (topY > maxY)
			{
				maxY = topY;
			}
		}
		Vector2f botLeft = new Vector2f(minX, minY);
		Vector2f topRight = new Vector2f(maxX, maxY);
		AABB aabb = new AABB(botLeft, topRight);
		return aabb;
	}

	public static AABB getAABBEnclosingCenterAndRadius(
			Collection polygonHolders)
	{
		return getAABBEnclosingCenterAndRadius(polygonHolders.toArray());
	}

	public static AABB getAABBEnclosingCenterAndRadius(Object[] polygonHolders)
	{
		if (polygonHolders.length == 0)
		{
			Vector2f botLeft = new Vector2f(0, 0);
			Vector2f topRight = new Vector2f(1, 1);
			AABB aabb = new AABB(botLeft, topRight);
			return aabb;
		}
		// find the bounding rectangle
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		for (int i = 0; i < polygonHolders.length; i++)
		{
			PolygonHolder polygonHolder = (PolygonHolder) polygonHolders[i];
			Vector2f c = polygonHolder.getPolygon().getCenter();
			float r = polygonHolder.getPolygon().getRadius();
			float leftX = c.x - r;
			float rightX = c.x + r;
			float botY = c.y - r;
			float topY = c.y + r;
			if (leftX < minX)
			{
				minX = leftX;
			}
			if (rightX > maxX)
			{
				maxX = rightX;
			}
			if (botY < minY)
			{
				minY = botY;
			}
			if (topY > maxY)
			{
				maxY = topY;
			}
		}
		Vector2f botLeft = new Vector2f(minX, minY);
		Vector2f topRight = new Vector2f(maxX, maxY);
		AABB aabb = new AABB(botLeft, topRight);
		return aabb;
	}

}