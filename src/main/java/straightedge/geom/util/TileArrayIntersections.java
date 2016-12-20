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

import straightedge.geom.*;
import straightedge.geom.vision.Occluder;
import straightedge.geom.vision.VPOccluderOccluderIntersection;
import java.util.*;

/**
 * Used to store and retrieve 2D Occluders and their 
 * intersections with other Occluders.
 * The points of intersection are calculated once when the Occluder is added.
 * Contains a table that's used to
 * quickly find the nearest objects.
 *
 * If bloated == true, some objects are outside of the table's rectangular bounds
 * with corners in botLeft and topRight. These T's will be stored in the
 * appropriate edge tile's sharedObstacles.
 *
 *       7 8 9
 *   X   4 5 6
 *       1 2 3
 *              Z
 *           Y
 *
 * For example, if the rectangle has tiles 1,2,3,...9 as above and
 * object X is added (which is outside the rectangle), bloated will be set to true
 * and X will be stored in tile 4's shareObstacles.
 * Similarly, Y and Z will be stored in tile 3's sharedObstacles.
 *
 * @author Keith
 */
public class TileArrayIntersections<T extends Occluder> {
	public int numRows;
	public int numCols;
	public Tile[][] tiles; // rows, columns
	public double tileWidthAndHeight;
	// Euclidean coordinates are assumed with positive X axis to the right and positive y axis up.
	public KPoint botLeft;
	public KPoint topRight;
	// if bloated == true then an object has been added that lies outside of the rectangle bounded by botRight and topLeft.
	boolean bloated;

	Tracker tracker = new Tracker();

	public TileArrayIntersections(KPoint botLeft, float tileWidthAndHeight, int numRows, int numCols){
		init(botLeft, tileWidthAndHeight, numRows, numCols);
	}

	protected void init(KPoint botLeft, float tileWidthAndHeight, int numRows, int numCols){
		this.numRows = numRows;
		this.numCols = numCols;
		tiles = new Tile[numRows][numCols];
		this.tileWidthAndHeight = tileWidthAndHeight;
		this.botLeft = botLeft.copy();
		topRight = new KPoint(botLeft.x + numRows*tileWidthAndHeight, botLeft.y + numCols*tileWidthAndHeight);
		bloated = false;
		for (int i = 0; i < numRows; i++){
			for (int j = 0; j < numCols; j++){
				tiles[i][j] = new Tile(this);
			}
		}
	}

	public TileArrayIntersections(KPoint botLeft, KPoint approxTopRight, float tileWidthAndHeight){
		double minX = botLeft.x;
		double minY = botLeft.y;
		double maxX = approxTopRight.x;
		double maxY = approxTopRight.y;
		this.numRows = (int)Math.ceil((maxX - minX)/tileWidthAndHeight);
		this.numCols = (int)Math.ceil((maxY - minY)/tileWidthAndHeight);
		init(botLeft, tileWidthAndHeight, numRows, numCols);
	}

	public TileArrayIntersections(AABB aabb, float tileWidthAndHeight){
		this(aabb.p, aabb.p2, tileWidthAndHeight);
	}

	public TileArrayIntersections(Object[] polygonHolders, float tileWidthAndHeight){
		this(AABB.getAABBEnclosingCenterAndRadius(polygonHolders), tileWidthAndHeight);
	}

	public TileArrayIntersections(Collection<PolygonHolder> polygonHolders, float tileWidthAndHeight){
		this(AABB.getAABBEnclosingCenterAndRadius(polygonHolders), tileWidthAndHeight);
	}

	public void add(T t){
		addToTileArray(t);

	}

	protected void addToTileArray(T occluder){
		// Add all points of intersection between this polygon and the existing polygons
		ArrayList<T> occluders = this.getAllWithin(occluder.getPolygon().getCenter(), occluder.getPolygon().getRadius());
		ArrayList<VPOccluderOccluderIntersection> occluderIntersectionPoints = new ArrayList<VPOccluderOccluderIntersection>();
		KPolygon polygon = occluder.getPolygon();
		for (int j = 0; j < polygon.getPoints().size(); j++){
			KPoint p = polygon.getPoints().get(j);
			int jPlus = (j+1 >= polygon.getPoints().size() ? 0 : j+1);
			KPoint p2 = polygon.getPoints().get(jPlus);

			for (int k = 0; k < occluders.size(); k++){
				Occluder occluder2 = occluders.get(k);
				KPolygon polygon2 = occluder2.getPolygon();
				if (polygon2.intersectionPossible(p, p2) == false){
					// intersection is not possible, so skip to next obstacle.
					continue;
				}
				ArrayList<KPoint> points = polygon2.getPoints();
				for (int m = 0; m < points.size(); m++){
					int nextM = (m+1 >= points.size() ? 0 : m+1);
					if (KPoint.linesIntersect(p, p2, points.get(m), points.get(nextM))){
						KPoint intersection = KPoint.getLineLineIntersection(p, p2, points.get(m), points.get(nextM));
						if (intersection != null){
							occluderIntersectionPoints.add(new VPOccluderOccluderIntersection(intersection, occluder, j, occluder2, m));
						}
					}
				}
			}
		}


		// add polygon
		{
			KPoint c = occluder.getPolygon().getCenter();
			double r = occluder.getPolygon().getRadius();
			boolean outsideBounds = false;
			double leftColIndex = ((c.x - r) - botLeft.x)/tileWidthAndHeight;
			double rightColIndex = ((c.x + r) - botLeft.x)/tileWidthAndHeight;
			double botRowIndex = ((c.y - r) - botLeft.y)/tileWidthAndHeight;
			double topRowIndex = ((c.y + r) - botLeft.y)/tileWidthAndHeight;
	//		System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", botRowIndex == "+botRowIndex+", topRowIndex == "+topRowIndex+", leftColIndex == "+leftColIndex+", rightColIndex == "+rightColIndex);
			if (botRowIndex < 0){
				botRowIndex = 0;
				outsideBounds = true;
			}else if (botRowIndex >= getNumRows()){
				botRowIndex = getNumRows()-1;
				outsideBounds = true;
			}
			if (topRowIndex < 0){
				topRowIndex = 0;
				outsideBounds = true;
			}else if (topRowIndex >= getNumRows()){
				topRowIndex = getNumRows() - 1;
				outsideBounds = true;
			}
			if (leftColIndex < 0){
				leftColIndex = 0;
				outsideBounds = true;
			}else if (leftColIndex >= getNumCols()){
				leftColIndex = getNumCols()-1;
				outsideBounds = true;
			}
			if (rightColIndex < 0){
				rightColIndex = 0;
				outsideBounds = true;
			}else if (rightColIndex >= getNumCols()){
				rightColIndex = getNumCols() - 1;
				outsideBounds = true;
			}
			if (outsideBounds){
				bloated = true;
			}
			int leftColIndexInt = (int)leftColIndex;
			int rightColIndexInt = (int)rightColIndex;
			int botRowIndexInt = (int)botRowIndex;
			int topRowIndexInt = (int)topRowIndex;
			//System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", botRowIndex == "+botRowIndex+", topRowIndex == "+topRowIndex+", leftColIndex == "+leftColIndex+", rightColIndex == "+rightColIndex);
			if (leftColIndexInt == rightColIndexInt && botRowIndexInt == topRowIndexInt){
				// the obst fits in a single tile so just add it to the tile's contained obstacles.
				Tile tile = tiles[botRowIndexInt][leftColIndexInt];
				if (outsideBounds == false){
					tile.getContainedObstacles().add(occluder);
				}else{
					tile.getSharedObstacles().add(occluder);
				}
			}else{
				// the obst spans a few tiles so add it to each tiles' shared obstacles.
				for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
					for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
						Tile tile = tiles[i][j];
						tile.getSharedObstacles().add(occluder);
	//					System.out.println(this.getClass().getSimpleName()+": added c == "+c+", i == "+i+", j == "+j);
					}
				}
			}
		}

		// add intersection points
		{
			for (int i = 0; i < occluderIntersectionPoints.size(); i++){
				VPOccluderOccluderIntersection obstacleIntersectionSightPoint = occluderIntersectionPoints.get(i);
				KPoint p = obstacleIntersectionSightPoint.getPoint();
				int colIndex = (int)Math.floor((p.x - botLeft.x)/tileWidthAndHeight);
				int rowIndex = (int)Math.floor((p.y - botLeft.y)/tileWidthAndHeight);
				if (rowIndex < 0){
					rowIndex = 0;
				}else if (rowIndex >= getNumRows()){
					rowIndex = getNumRows()-1;
				}
				if (colIndex < 0){
					colIndex = 0;
				}else if (colIndex >= getNumCols()){
					colIndex = getNumCols()-1;
				}
				Tile tile = tiles[rowIndex][colIndex];
				tile.getIntersections().add(obstacleIntersectionSightPoint);
			}
		}
	}

	public boolean remove(T t){
		KPoint c = t.getPolygon().getCenter();
		double r = t.getPolygon().getRadius();
		double leftColIndex = ((c.x - r) - botLeft.x)/tileWidthAndHeight;
		double rightColIndex = ((c.x + r) - botLeft.x)/tileWidthAndHeight;
		double botRowIndex = ((c.y - r) - botLeft.y)/tileWidthAndHeight;
		double topRowIndex = ((c.y + r) - botLeft.y)/tileWidthAndHeight;

		if (botRowIndex < 0){
			botRowIndex = 0;
		}else if (botRowIndex >= getNumRows()){
			botRowIndex = getNumRows()-1;
		}
		if (topRowIndex < 0){
			topRowIndex = 0;
		}else if (topRowIndex >= getNumRows()){
			topRowIndex = getNumRows() - 1;
		}
		if (leftColIndex < 0){
			leftColIndex = 0;
		}else if (leftColIndex >= getNumCols()){
			leftColIndex = getNumCols()-1;
		}
		if (rightColIndex < 0){
			rightColIndex = 0;
		}else if (rightColIndex >= getNumCols()){
			rightColIndex = getNumCols() - 1;
		}
		int leftColIndexInt = (int)leftColIndex;
		int rightColIndexInt = (int)rightColIndex;
		int botRowIndexInt = (int)botRowIndex;
		int topRowIndexInt = (int)topRowIndex;

		boolean removed = false;
		if (leftColIndexInt == rightColIndexInt && botRowIndexInt == topRowIndexInt){
			// the obst fits in a single tile so just add it to the tile's contained obstacles.
			Tile tile = tiles[botRowIndexInt][leftColIndexInt];
			boolean justRemoved = tile.getContainedObstacles().remove(t);
			if (justRemoved == false){
				// need to remove it from containedObstacles too in case it is outside the bounds.
				justRemoved = tile.getSharedObstacles().remove(t);
			}
			if (justRemoved == true){
				removed = true;
			}
			// remove any intersection points
			Bag<VPOccluderOccluderIntersection> intersections = tile.getIntersections();
			for (int k = 0; k < intersections.size(); k++){
				VPOccluderOccluderIntersection intersection = intersections.get(k);
				if (intersection.getPolygon() == t.getPolygon() || intersection.getPolygon2() == t.getPolygon()){
					intersections.remove(k);
					k--;
				}
			}
			//System.out.println("added to getContainedObstacles: "+obst.getInnerPolygon().getCenter());
		}else{
			// the obst spans a few tiles so add it to each tiles' shared obstacles.
			for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
				for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
					Tile tile = tiles[i][j];
					boolean justRemoved = tile.getSharedObstacles().remove(t);
					if (justRemoved == false){
						// need to remove it from containedObstacles too in case it is outside the bounds.
						justRemoved = tile.getContainedObstacles().remove(t);
					}
					if (justRemoved == true){
						removed = true;
					}
					// remove any intersection points
					Bag<VPOccluderOccluderIntersection> intersections = tile.getIntersections();
					for (int k = 0; k < intersections.size(); k++){
						VPOccluderOccluderIntersection intersection = intersections.get(k);
						if (intersection.getPolygon() == t.getPolygon() || intersection.getPolygon2() == t.getPolygon()){
							intersections.remove(k);
							k--;
						}
					}
				}
			}
		}
		assert getAllWithin(c, r).contains(t) == false : "c == "+c+", r == "+r;
		return removed;
	}
	public ArrayList<T> getAllWithin(KPoint point, double radius){
		return getAllWithin(point.x, point.y, radius);
	}

	//	CodeTimer ct = new CodeTimer(this.getClass().getSimpleName()+": getAllWithin");
	public ArrayList<T> getAllWithin(double x, double y, double radius){
//		ct.click("create ArrayList");
		ArrayList<T> nearbyObstacles = new ArrayList<T>();
		double r = radius;

//		ct.click("index calcs");

		double leftColIndex = ((x - r) - botLeft.x)/tileWidthAndHeight;
		double rightColIndex = ((x + r) - botLeft.x)/tileWidthAndHeight;
		double botRowIndex = ((y - r) - botLeft.y)/tileWidthAndHeight;
		double topRowIndex = ((y + r) - botLeft.y)/tileWidthAndHeight;
//		System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", botRowIndex == "+botRowIndex+", topRowIndex == "+topRowIndex+", leftColIndex == "+leftColIndex+", rightColIndex == "+rightColIndex);

//		ct.click("index checks");
		if (botRowIndex < 0){
			botRowIndex = 0;
		}else if (botRowIndex >= getNumRows()){
			botRowIndex = getNumRows()-1;
		}
		if (topRowIndex < 0){
			topRowIndex = 0;
		}else if (topRowIndex >= getNumRows()){
			topRowIndex = getNumRows() - 1;
		}
		if (leftColIndex < 0){
			leftColIndex = 0;
		}else if (leftColIndex >= getNumCols()){
			leftColIndex = getNumCols()-1;
		}
		if (rightColIndex < 0){
			rightColIndex = 0;
		}else if (rightColIndex >= getNumCols()){
			rightColIndex = getNumCols() - 1;
		}

		int leftColIndexInt = (int)leftColIndex;
		int rightColIndexInt = (int)rightColIndex;
		int botRowIndexInt = (int)botRowIndex;
		int topRowIndexInt = (int)topRowIndex;

		//System.out.println("leftColIndex == "+leftColIndex+" rightColIndex == "+rightColIndex+" botRowIndex == "+botRowIndex+" topRowIndex == "+topRowIndex);
//		ct.click("adds");
		if (leftColIndexInt == rightColIndexInt && botRowIndexInt == topRowIndexInt){
			// the obst fits in a single tile so just add it to the tile's contained obstacles.
			Tile tile = tiles[botRowIndexInt][leftColIndexInt];
			for (int i = 0; i < tile.getSharedObstacles().size(); i++){
				T t = (T)tile.getSharedObstacles().get(i);
				KPolygon polygon = t.getPolygon();
				KPoint polygonCenter = polygon.getCenter();
				if (polygon.isTileArraySearchStatusAdded(tracker) == true){
					continue;
				}
				double radiusSumSq = (r + polygon.getRadius());
				radiusSumSq *= radiusSumSq;
				if (KPoint.distanceSq(x,y,polygonCenter.x,polygonCenter.y) < radiusSumSq){
					nearbyObstacles.add(t);
					polygon.setTileArraySearchStatus(true, tracker);
				}
			}
			for (int i = 0; i < tile.getContainedObstacles().size(); i++){
				T t = (T)tile.getContainedObstacles().get(i);
				KPolygon polygon = t.getPolygon();
				KPoint polygonCenter = polygon.getCenter();
				double radiusSumSq = (r + polygon.getRadius());
				radiusSumSq *= radiusSumSq;
				if (KPoint.distanceSq(x,y,polygonCenter.x,polygonCenter.y) < radiusSumSq){
					nearbyObstacles.add(t);
				}
			}
		}else{
			// the obst spans a few tiles so add it to each tiles' shared obstacles.
			for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
				for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
					Tile tile = tiles[i][j];
					Bag<T> sharedObstacles = tile.getSharedObstacles();
					for (int k = 0; k < sharedObstacles.size(); k++){
						T t = (T)sharedObstacles.get(k);
						KPolygon polygon = t.getPolygon();
						if (polygon.isTileArraySearchStatusAdded(tracker) == true){
							continue;
						}
						double radiusSumSq = (r + polygon.getRadius());
						radiusSumSq *= radiusSumSq;
						KPoint polygonCenter = polygon.getCenter();
						if (KPoint.distanceSq(x,y,polygonCenter.x,polygonCenter.y) < radiusSumSq){
							nearbyObstacles.add(t);
							polygon.setTileArraySearchStatus(true, tracker);
						}
					}
					Bag<T> containedObstacles = tile.getContainedObstacles();
					for (int k = 0; k < containedObstacles.size(); k++){
						T t = (T)containedObstacles.get(k);
						KPolygon polygon = t.getPolygon();
						double radiusSumSq = (r + polygon.getRadius());
						radiusSumSq *= radiusSumSq;
						KPoint polygonCenter = polygon.getCenter();
						if (KPoint.distanceSq(x,y,polygonCenter.x,polygonCenter.y) < radiusSumSq){
							nearbyObstacles.add(t);
						}
					}
				}
			}
		}
		tracker.incrementCounter();
//		ct.lastClick();
		return nearbyObstacles;
	}

	/**
	 * Returns an ArrayList of intersections between the KPolygons.
	 * Note that the list returned may include intersections outside of radius.
	 * @param point
	 * @param radius
	 * @return
	 */
	public ArrayList<VPOccluderOccluderIntersection> getIntersectionsWithinAtLeast(KPoint point, double radius){
		ArrayList<VPOccluderOccluderIntersection> nearbyIntersections = new ArrayList<VPOccluderOccluderIntersection>();
		KPoint c = point;
		double r = radius;
		double leftColIndex = ((c.x - r) - botLeft.x)/tileWidthAndHeight;
		double rightColIndex = ((c.x + r) - botLeft.x)/tileWidthAndHeight;
		double botRowIndex = ((c.y - r) - botLeft.y)/tileWidthAndHeight;
		double topRowIndex = ((c.y + r) - botLeft.y)/tileWidthAndHeight;

		if (botRowIndex < 0){
			botRowIndex = 0;
		}else if (botRowIndex >= getNumRows()){
			botRowIndex = getNumRows()-1;
		}
		if (topRowIndex < 0){
			topRowIndex = 0;
		}else if (topRowIndex >= getNumRows()){
			topRowIndex = getNumRows() - 1;
		}
		if (leftColIndex < 0){
			leftColIndex = 0;
		}else if (leftColIndex >= getNumCols()){
			leftColIndex = getNumCols()-1;
		}
		if (rightColIndex < 0){
			rightColIndex = 0;
		}else if (rightColIndex >= getNumCols()){
			rightColIndex = getNumCols() - 1;
		}
		int leftColIndexInt = (int)leftColIndex;
		int rightColIndexInt = (int)rightColIndex;
		int botRowIndexInt = (int)botRowIndex;
		int topRowIndexInt = (int)topRowIndex;

		//System.out.println("leftColIndex == "+leftColIndex+" rightColIndex == "+rightColIndex+" botRowIndex == "+botRowIndex+" topRowIndex == "+topRowIndex);

		for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
			for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
				Tile tile = tiles[i][j];
				Bag<VPOccluderOccluderIntersection> intersections = tile.getIntersections();
				for (int k = 0; k < intersections.size(); k++){
					VPOccluderOccluderIntersection obst = (VPOccluderOccluderIntersection)intersections.get(k);
					nearbyIntersections.add(obst);
				}
			}
		}
		return nearbyIntersections;
	}

	public void clear(){
		for (int i = 0; i < numRows; i++){
			for (int j = 0; j < numCols; j++){
				tiles[i][j].getContainedObstacles().clear();
				tiles[i][j].getSharedObstacles().clear();
				tiles[i][j].getIntersections().clear();
			}
		}
	}

	public Tile getTile(int row, int col){
		return tiles[row][col];
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumCols() {
		return numCols;
	}

	public KPoint getBotLeft() {
		return botLeft;
	}

	public boolean isBloated() {
		return bloated;
	}

	public double getTileWidthAndHeight() {
		return tileWidthAndHeight;
	}

	public Tile[][] getTiles() {
		return tiles;
	}

	public KPoint getTopRight() {
		return topRight;
	}

	public class Tile<T>{
		TileArrayIntersections tileArray;
		Bag<T> containedObstacles;
		Bag<T> sharedObstacles;
		Bag<VPOccluderOccluderIntersection> intersections;
		public Tile(TileArrayIntersections tileArray){
			this.tileArray = tileArray;
			sharedObstacles = new Bag<T>();
			containedObstacles = new Bag<T>();
			intersections = new Bag<VPOccluderOccluderIntersection>();
		}

		public Bag<T> getContainedObstacles() {
			return containedObstacles;
		}

		public Bag<T> getSharedObstacles() {
			return sharedObstacles;
		}
		public Bag<VPOccluderOccluderIntersection> getIntersections() {
			return intersections;
		}
	}

	public String toString(){
		return super.toString()+", numRows == "+numRows+", numCols == "+numCols+", tileWidthAndHeight == "+tileWidthAndHeight+", botLeft == "+botLeft+", topRight == "+topRight+", bloated == "+bloated;
	}


}
