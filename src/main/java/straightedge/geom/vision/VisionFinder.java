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
package straightedge.geom.vision;

import straightedge.geom.*;
import straightedge.geom.util.*;
import java.util.*;


/**
 *
 * @author Keith
 */
public class VisionFinder {
	public ArrayList<OccluderDistAndQuad> polygonAndDists = new ArrayList<OccluderDistAndQuad>();
	public ArrayList<VPOccluderOccluderIntersection> occluderIntersectionPoints = new ArrayList<VPOccluderOccluderIntersection>();
	public ArrayList<VPOccluderBoundaryIntersection> boundaryOccluderIntersectionPoints = new ArrayList<VPOccluderBoundaryIntersection>();

	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, List<? extends Occluder> allOccluders){
		return calc(eye, boundaryPolygon, new ArrayList<Occluder>(0), new ArrayList<VPOccluderOccluderIntersection>(0), allOccluders);
	}
	public VisionData calc(VisionData cache, List<? extends Occluder> allOccluders){
		return calc(cache, new ArrayList<Occluder>(0), new ArrayList<VPOccluderOccluderIntersection>(0), allOccluders);
	}
	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, TileArrayIntersections<? extends Occluder> fixedOccludersTileArrayIntersections, ArrayList<? extends Occluder> movingOccluders){
		List<? extends Occluder> fixedOccluders = fixedOccludersTileArrayIntersections.getAllWithin(boundaryPolygon.getCenter(), boundaryPolygon.getRadius());
		List<VPOccluderOccluderIntersection> fixedOccludersIntersectionPoints = fixedOccludersTileArrayIntersections.getIntersectionsWithinAtLeast(boundaryPolygon.getCenter(), boundaryPolygon.getRadius());
		return calc(eye, boundaryPolygon, fixedOccluders, fixedOccludersIntersectionPoints, movingOccluders);
	}
	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, TileBagIntersections<? extends Occluder> fixedOccludersTileBagIntersections, ArrayList<? extends Occluder> movingOccluders){
		return calc(eye, boundaryPolygon, fixedOccludersTileBagIntersections.getTileArray(), movingOccluders);
	}
	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, TileBagIntersections<? extends Occluder> fixedOccludersTileBagIntersections){
		return calc(eye, boundaryPolygon, fixedOccludersTileBagIntersections.getTileArray(), new ArrayList<Occluder>(0));
	}
	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, TileArrayIntersections<? extends Occluder> fixedOccludersTileArrayIntersections){
		return calc(eye, boundaryPolygon, fixedOccludersTileArrayIntersections, new ArrayList<Occluder>(0));
	}
	public VisionData calc(VisionData cache, TileArrayIntersections<? extends Occluder> fixedOccludersTileArrayIntersections, List<? extends Occluder> movingOccluders){
		KPolygon boundaryPolygon = cache.getBoundaryPolygon();
		ArrayList<? extends Occluder> fixedOccluders = fixedOccludersTileArrayIntersections.getAllWithin(boundaryPolygon.getCenter(), boundaryPolygon.getRadius());
		ArrayList<VPOccluderOccluderIntersection> fixedOccludersIntersectionPoints = fixedOccludersTileArrayIntersections.getIntersectionsWithinAtLeast(boundaryPolygon.getCenter(), boundaryPolygon.getRadius());
		return calc(cache, fixedOccluders, fixedOccludersIntersectionPoints, movingOccluders);
	}
	public VisionData calc(VisionData cache, TileBagIntersections<? extends Occluder> fixedOccludersTileBagIntersections, List<? extends Occluder> movingOccluders){
		return calc(cache, fixedOccludersTileBagIntersections.getTileArray(), movingOccluders);
	}
	public VisionData calc(VisionData cache, TileBagIntersections<? extends Occluder> fixedOccludersTileBagIntersections){
		return calc(cache, fixedOccludersTileBagIntersections.getTileArray(), new ArrayList<Occluder>(0));
	}
	public VisionData calc(VisionData cache, TileArrayIntersections<? extends Occluder> fixedOccludersTileArrayIntersections){
		return calc(cache, fixedOccludersTileArrayIntersections, new ArrayList<Occluder>(0));
	}
	public VisionData calc(KPoint eye, KPolygon boundaryPolygon, List<? extends Occluder> fixedOccluders, List<VPOccluderOccluderIntersection> fixedOccludersIntersectionPoints, List<? extends Occluder> movingOccluders){
		VisionData cache = new VisionData(eye, boundaryPolygon);
		return calc(cache, fixedOccluders, fixedOccludersIntersectionPoints, movingOccluders);
	}
	
	//CodeTimer codeTimer = new CodeTimer("calc", CodeTimer.Output.Millis, CodeTimer.Output.Millis);
//	{
//		codeTimer.setEnabled(true);
//	}
	public VisionData calc(VisionData cache, List<? extends Occluder> fixedOccluders, List<VPOccluderOccluderIntersection> fixedOccludersIntersectionPoints, List<? extends Occluder> movingOccluders){
		KPoint eye = cache.eye;
		KPolygon boundaryPolygon = cache.boundaryPolygon;
		cache.visiblePoints = null;
		cache.visiblePolygon = null;
		int[] boundaryPolygonXIndicators = cache.boundaryPolygonXIndicators;
		int[] boundaryPolygonYIndicators = cache.boundaryPolygonYIndicators;
		double maxEyeToBoundaryPolygonPointDist = cache.maxEyeToBoundaryPolygonPointDist;
		double minEyeToBoundaryPolygonPointDist = cache.minEyeToBoundaryPolygonPointDist;
		double maxEyeToBoundaryPolygonPointDistSq = cache.maxEyeToBoundaryPolygonPointDistSq;
		double minEyeToBoundaryPolygonPointDistSq = cache.minEyeToBoundaryPolygonPointDistSq;
		//double[] boundaryPolygonPointAngles = cache.boundaryPolygonPointAngles;
		//double boundaryPolygonRotationAroundEye = cache.getBoundaryPolygonRotationAroundEye();

		ArrayList<KPoint> boundaryPolygonPoints = boundaryPolygon.getPoints();
		ArrayList<VisiblePoint> visiblePoints = new ArrayList<VisiblePoint>(boundaryPolygonPoints.size());	// size is likely to be boundaryPolygon.size() or more.
//		codeTimer.click("polygonAndDists clear");
		polygonAndDists.clear();
//		codeTimer.click("polygonAndDists");
		// add the fixedPolygons to polygonAndDists
		for (int n = 0; n < fixedOccluders.size(); n++){
			Occluder occluder = fixedOccluders.get(n);
			KPolygon poly = occluder.getPolygon();
			double distCenterToEyeLessCircBound = eye.distance(poly.getCenter()) - poly.getRadius();
			double distCenterToEyeLessCircBoundSq = distCenterToEyeLessCircBound*distCenterToEyeLessCircBound;
			if (distCenterToEyeLessCircBound < 0){
				distCenterToEyeLessCircBoundSq *= -1;
			}
			int xIndicator = getXIndicator(poly, eye);
			int yIndicator = getYIndicator(poly, eye);
			OccluderDistAndQuad polygonAndDist = new OccluderDistAndQuad(occluder, distCenterToEyeLessCircBound, distCenterToEyeLessCircBoundSq, xIndicator, yIndicator);
			polygonAndDists.add(polygonAndDist);
		}
		// add the movingPolygons to polygonAndDists
		for (int n = 0; n < movingOccluders.size(); n++){
			Occluder occluder = movingOccluders.get(n);
			KPolygon poly = occluder.getPolygon();
			double distCenterToEyeLessCircBound = eye.distance(poly.getCenter()) - poly.getRadius();
			double distCenterToEyeLessCircBoundSq = distCenterToEyeLessCircBound*distCenterToEyeLessCircBound;
			if (distCenterToEyeLessCircBound < 0){
				distCenterToEyeLessCircBoundSq *= -1;
			}
			int xIndicator = getXIndicator(poly, eye);
			int yIndicator = getYIndicator(poly, eye);
			OccluderDistAndQuad polygonAndDist = new OccluderDistAndQuad(occluder, distCenterToEyeLessCircBound, distCenterToEyeLessCircBoundSq, xIndicator, yIndicator);
			polygonAndDists.add(polygonAndDist);
		}
//		codeTimer.click("sort");
		// Sort the list.
		Collections.sort(polygonAndDists);
		//codeTimer.click();

//		codeTimer.click("visiblePoints clear");
		visiblePoints.clear();

//		codeTimer.click("Add occluder points");
		// Add occluder points
		for (int i = 0; i < polygonAndDists.size(); i++){
			OccluderDistAndQuad polygonAndDist = polygonAndDists.get(i);
			// check if it's possible for occluder to be inside the boundaryPolygon
			if (polygonAndDist.getDistEyeToCenterLessRadius() > maxEyeToBoundaryPolygonPointDist){
				continue;
			}
			KPolygon polygon = polygonAndDist.getPolygon();
			ArrayList<KPoint> points = polygon.getPoints();
			boolean allPointsInsideBoundaryPolygon = (polygonAndDist.getDistEyeToCenterLessRadius() + 2*polygon.getRadius()) < minEyeToBoundaryPolygonPointDist;
			PointLoop:
			for (int j = 0; j < points.size(); j++){
				KPoint p = points.get(j);
				double eyeToPDistSq = eye.distanceSq(p);
				// Only add occluder points if they're inside the boundaryPolygon and they're unobstructed.
				if (allPointsInsideBoundaryPolygon == false){
					// The polygon points may not all be inside the boundaryPolygon so need to check that this point is using the contains method.
					if (eyeToPDistSq > maxEyeToBoundaryPolygonPointDistSq){
						continue;
					}else if (eyeToPDistSq >= minEyeToBoundaryPolygonPointDistSq){
						if (boundaryPolygon.contains(p) == false){
							continue;
						}
					}
				}

				// check if the line from the eye to the polygon point intersects other lines that make up the polygon
				for (int m = 0; m < points.size(); m++){
					int nextM = (m+1 >= points.size() ? 0 : m+1);
					if (j == m || j == nextM){
						continue;
					}
					if (KPoint.linesIntersect(p, eye, points.get(m), points.get(nextM))){
						continue PointLoop;
					}
				}

				// check if the line from the eye to the polygon point intersects other polygons
				for (int k = 0; k < polygonAndDists.size(); k++){
					OccluderDistAndQuad polygonAndDist2 = polygonAndDists.get(k);
					if (polygonAndDist == polygonAndDist2){
						// check if the line from the eye to the polygon point intersects other lines that make up the polygon
//						for (int m = 0; m < points.size(); m++){
//							int nextM = (m+1 >= points.size() ? 0 : m+1);
//							if (j == m || j == nextM){
//								continue;
//							}
//							if (KPolygon.isValidNoLineIntersections(p, eye, points.get(m), points.get(nextM))){
//								continue PointLoop;
//							}
//						}
						continue;
					}
					if (polygonAndDist2.getDistEyeToCenterLessRadiusSqSigned() > eyeToPDistSq){
						break;
					}
					if (polygonAndDist.getXIndicator()*polygonAndDist2.getXIndicator() == -1 || polygonAndDist.getYIndicator()*polygonAndDist2.getYIndicator() == -1){
						continue;
					}
					KPolygon polygon2 = polygonAndDist2.getPolygon();
					if (polygon2.intersectionPossible(p, eye) && polygon2.intersectsLine(p, eye)){
						continue PointLoop;
					}
				}
				//double angleRelativeToEye = eye.findAngle(p);
				//VisiblePoint vp = new VPOccluder(p, polygonAndDist.getOccluder(), j, angleRelativeToEye);
				VisiblePoint vp = new VPOccluder(p, polygonAndDist.getOccluder(), j);
				visiblePoints.add(vp);
			}
		}
//		codeTimer.click("Find intersection between occluders and the boundaryPolygon");
		// Add all points of intersection between occluders and the boundaryPolygon
		boundaryOccluderIntersectionPoints.clear();
		{
			for (int j = 0; j < boundaryPolygonPoints.size(); j++){
				KPoint p = boundaryPolygonPoints.get(j);
				int jPlus = (j+1 >= boundaryPolygonPoints.size() ? 0 : j+1);
				KPoint p2 = boundaryPolygonPoints.get(jPlus);

				int xIndicator = getXIndicator(p, p2, eye);
				int yIndicator = getYIndicator(p, p2, eye);
				boundaryPolygonXIndicators[j] = xIndicator;
				boundaryPolygonYIndicators[j] = yIndicator;
				for (int k = 0; k < polygonAndDists.size(); k++){
					OccluderDistAndQuad polygonAndDist = polygonAndDists.get(k);
					KPolygon polygon = polygonAndDist.getPolygon();
					if ((xIndicator*polygonAndDist.getXIndicator() == -1 || yIndicator*polygonAndDist.getYIndicator() == -1) == true){
						continue;
					}
					if (polygon.intersectionPossible(p, p2) == false){
						// intersection is not possible, so skip to next occluder.
						continue;
					}
					ArrayList<KPoint> points = polygon.getPoints();
					for (int i = 0; i < points.size(); i++){
						int nextI = (i+1 >= points.size() ? 0 : i+1);
						KPoint p3 = points.get(i);
						KPoint p4 = points.get(nextI);
						if (KPoint.linesIntersect(p, p2, p3, p4)){
							KPoint intersection = KPoint.getLineLineIntersection(p, p2, p3, p4);
							if (intersection != null){
								boundaryOccluderIntersectionPoints.add(new VPOccluderBoundaryIntersection(intersection, polygonAndDist.getOccluder(), i));
							}
						}
					}
				}
			}
		}
//		codeTimer.click("Only add boundary-occluder intersection points that are unoccluded");
		// Only add boundary-occluder intersection points that are unoccluded
		OuterLoop:
		for (int j = 0; j < boundaryOccluderIntersectionPoints.size(); j++){
			VPOccluderBoundaryIntersection visiblePoint = boundaryOccluderIntersectionPoints.get(j);
			KPoint p = visiblePoint.getPoint();
			// see if the occluder that makes this visiblePoint intersection occludes this point from the eye.
			ArrayList<KPoint> points = visiblePoint.getPolygon().getPoints();
			for (int m = 0; m < points.size(); m++){
				int nextM = (m+1 >= points.size() ? 0 : m+1);
				if (visiblePoint.getPolygonPointNum() == m){
					continue;
				}
				if (KPoint.linesIntersect(p, eye, points.get(m), points.get(nextM))){
					continue OuterLoop;
				}
			}
			int xIndicator = getXIndicator(p, eye);
			int yIndicator = getYIndicator(p, eye);
			// see if any other occluders cause an obstruction
			double eyeToPDistSq = eye.distanceSq(p);
			for (int k = 0; k < polygonAndDists.size(); k++){
				OccluderDistAndQuad polygonAndDist2 = polygonAndDists.get(k);
				KPolygon polygon2 = polygonAndDists.get(k).getPolygon();
				if (visiblePoint.getPolygon() == polygon2){
					continue;
				}
				if (polygonAndDists.get(k).getDistEyeToCenterLessRadiusSqSigned() > eyeToPDistSq){
					break;
				}
				if (xIndicator*polygonAndDist2.getXIndicator() == -1 || yIndicator*polygonAndDist2.getYIndicator() == -1){
					continue;
				}
				if (polygon2.intersectionPossible(p, eye) && polygon2.intersectsLine(p, eye)){
					continue OuterLoop;
				}
			}
			visiblePoints.add(visiblePoint);
		}
//		codeTimer.click("movingOccluders intersections");
		boundaryOccluderIntersectionPoints.clear();
		// Add all points of intersection between movingOccluders and fixedOccluders and other movingOccluders
		occluderIntersectionPoints.clear();
		occluderIntersectionPoints.addAll(fixedOccludersIntersectionPoints);
		for (int i = 0; i < movingOccluders.size(); i++){
			Occluder occluder = movingOccluders.get(i);
			KPolygon polygon = occluder.getPolygon();
			if (boundaryPolygon.getCenter().distance(polygon.getCenter()) > boundaryPolygon.getRadius() + polygon.getRadius()){
				continue;
			}
			for (int j = 0; j < polygon.getPoints().size(); j++){
				KPoint p = polygon.getPoints().get(j);
				int jPlus = (j+1 >= polygon.getPoints().size() ? 0 : j+1);
				KPoint p2 = polygon.getPoints().get(jPlus);
				// first intersect with other movingPolygons
				for (int k = i+1; k < movingOccluders.size(); k++){
					Occluder occluder2 = movingOccluders.get(k);
					KPolygon polygon2 = occluder2.getPolygon();
					if (polygon2.intersectionPossible(p, p2) == false){
						// intersection is not possible, so skip to next occluder.
						continue;
					}
					ArrayList<KPoint> points = polygon2.getPoints();
					for (int m = 0; m < points.size(); m++){
						int nextM = (m+1 >= points.size() ? 0 : m+1);
						if (KPoint.linesIntersect(p, p2, points.get(m),points.get(nextM))){
							KPoint intersection = KPoint.getLineLineIntersection(p, p2, points.get(m), points.get(nextM));
							if (intersection != null){
								occluderIntersectionPoints.add(new VPOccluderOccluderIntersection(intersection, occluder, j, occluder2, m));
							}
						}
					}
				}
				// intersect with fixedPolygons
				for (int k = 0; k < fixedOccluders.size(); k++){
					Occluder occluder2 = fixedOccluders.get(k);
					KPolygon polygon2 = occluder2.getPolygon();
					if (polygon2.intersectionPossible(p, p2) == false){
						// intersection is not possible, so skip to next occluder.
						continue;
					}
					ArrayList<KPoint> points = polygon2.getPoints();
					for (int m = 0; m < points.size(); m++){
						int nextM = (m+1 >= points.size() ? 0 : m+1);
						if (KPoint.linesIntersect(p, p2, points.get(m),points.get(nextM))){
							KPoint intersection = KPoint.getLineLineIntersection(p, p2, points.get(m), points.get(nextM));
							if (intersection != null){
								occluderIntersectionPoints.add(new VPOccluderOccluderIntersection(intersection, occluder, j, occluder2, m));
							}
						}
					}
				}
			}
		}
//		codeTimer.click("take out occluder points unobstructed");
		// only add occluder intersection points that are unobstructed
		OuterLoop:
		for (int j = 0; j < occluderIntersectionPoints.size(); j++){
			VPOccluderOccluderIntersection visiblePoint = occluderIntersectionPoints.get(j);
			KPoint p = visiblePoint.getPoint();
			// it's not guaranteed that the occluder intersection points are actually inside the boundaryPolygon so must check this.
			double eyeToPDistSq = eye.distanceSq(p);
			if (eyeToPDistSq > maxEyeToBoundaryPolygonPointDistSq){
				continue;
			}else if (eyeToPDistSq >= minEyeToBoundaryPolygonPointDistSq){
				if (boundaryPolygon.contains(p) == false){
					continue;
				}
			}
			int xIndicator = getXIndicator(p, eye);
			int yIndicator = getYIndicator(p, eye);
			for (int k = 0; k < polygonAndDists.size(); k++){
				OccluderDistAndQuad polygonAndDist = polygonAndDists.get(k);
				KPolygon polygon = polygonAndDist.getPolygon();
				if (visiblePoint.getPolygon() == polygon){
					ArrayList<KPoint> points = polygon.getPoints();
					for (int m = 0; m < points.size(); m++){
						int nextM = (m+1 >= points.size() ? 0 : m+1);
						if (visiblePoint.getPolygonPointNum() == m){
							continue;
						}
						if (KPoint.linesIntersect(p, eye, points.get(m), points.get(nextM))){
							continue OuterLoop;
						}
					}
				}else if (visiblePoint.getPolygon2() == polygon){
					ArrayList<KPoint> points = polygon.getPoints();
					for (int m = 0; m < points.size(); m++){
						int nextM = (m+1 >= points.size() ? 0 : m+1);
						if (visiblePoint.getPolygonPointNum2() == m){
							continue;
						}
						if (KPoint.linesIntersect(p, eye, points.get(m), points.get(nextM))){
							continue OuterLoop;
						}
					}
				}else{
					if (polygonAndDist.getDistEyeToCenterLessRadiusSqSigned() > eyeToPDistSq){
						break;
					}
					if (xIndicator*polygonAndDist.getXIndicator() == -1 || yIndicator*polygonAndDist.getYIndicator() == -1){
						continue;
					}
					if (polygon.intersectionPossible(p, eye) && polygon.intersectsLine(p, eye)){
						continue OuterLoop;
					}
				}
			}
			VPOccluderOccluderIntersection occluderIntersectionVisiblePoint = occluderIntersectionPoints.get(j);
			visiblePoints.add(occluderIntersectionVisiblePoint);
		}
		occluderIntersectionPoints.clear();
//		codeTimer.click("add visible boundaryPolygon points");
		// Add all points on the boundaryPolygon if they're unobstructed.
		OuterLoop:
		for (int j = 0; j < boundaryPolygonPoints.size(); j++){
			KPoint p = boundaryPolygonPoints.get(j);
			int xIndicator = getXIndicator(p, eye);
			int yIndicator = getYIndicator(p, eye);
			for (int k = 0; k < polygonAndDists.size(); k++){
				OccluderDistAndQuad polygonAndDist = polygonAndDists.get(k);
				KPolygon polygon = polygonAndDists.get(k).getPolygon();
				if (xIndicator*polygonAndDist.getXIndicator() == -1 || yIndicator*polygonAndDist.getYIndicator() == -1){
					continue;
				}
				if (polygon.intersectionPossible(p, eye) && polygon.intersectsLine(p, eye)){
					continue OuterLoop;
				}
			}
			VPBoundary vp = new VPBoundary(p);
			visiblePoints.add(vp);
		}
//		codeTimer.click("sort visiblePoints");
		for (int i = 0; i < visiblePoints.size(); i++){
			visiblePoints.get(i).preSortCalcs(eye);
		}
		Collections.sort(visiblePoints);
//		codeTimer.click("add shadow points");
		// Make new points by casting a ray from the eye thru each occluder end point and finding the closest intersection.
		for (int j = 0; j < visiblePoints.size(); j++){
			int jPlus = (j+1 >= visiblePoints.size() ? 0 : j+1);
			if (visiblePoints.get(j).getType() == VisiblePoint.OCCLUDER){
				// see if the the points on the polygon on either side of this one are on the same side so a shadow can be cast
				VPOccluder sp = (VPOccluder)visiblePoints.get(j);
				KPoint p = sp.getPoint();
				KPolygon polygon = sp.getPolygon();
				int pNum = sp.getPolygonPointNum();
				int pNumPlus = (pNum+1 >= polygon.getPoints().size() ? 0 : pNum+1);
				KPoint pPlus = polygon.getPoints().get(pNumPlus);
				int pNumMinus = (pNum-1 < 0 ? polygon.getPoints().size()-1 : pNum-1);
				KPoint pMinus = polygon.getPoints().get(pNumMinus);
				int pPlusRCCW = pPlus.relCCW(eye, p);
				int pMinusRCCW = pMinus.relCCW(eye, p);
				if (pPlusRCCW == pMinusRCCW){
					double pToEyeDist = p.distance(eye);
					KPoint endOfRayPoint = eye.createPointToward(p, pToEyeDist + boundaryPolygon.getRadius()*2);	//p.createPointFromAngle(angleRelativeToEye, getOriginalSightPolygon().getRadius()*2);
					KPoint closestIntersectionPoint = null;
					double closestDist = Double.MAX_VALUE;
					Occluder closestOccluder = null;
					int closestObstPolygonEdgeIndex = -1;
					boolean obstCloser = false;
					int xIndicator = getXIndicator(p, eye);
					int yIndicator = getYIndicator(p, eye);
					// cast a ray from the shadow point and find the closest intersection with the occluders
					for (int k = 0; k < polygonAndDists.size(); k++){
						OccluderDistAndQuad polygonAndDist = polygonAndDists.get(k);
						KPolygon polygon2 = polygonAndDist.getPolygon();
						if (pToEyeDist > polygonAndDist.getDistEyeToCenterLessRadius() + polygon2.getRadius()*2){
							// intersection not possible since the polygon is even closer than the ray start point.
							continue;
						}
						if (closestDist < polygonAndDist.getDistEyeToCenterLessRadius()){
							// break since this polygon is further away than the existing closestIntersection.
							break;
						}
						if (xIndicator*polygonAndDist.getXIndicator() == -1 || yIndicator*polygonAndDist.getYIndicator() == -1){
							// intersection is not possible since the point and
							// the occluders are in different quadrants, so skip to next occluder.
							continue;
						}
						if (polygon2.intersectionPossible(p, endOfRayPoint) == false){
							// intersection is not possible, so skip to next occluder.
							continue;
						}
						ArrayList<KPoint> points = polygon2.getPoints();
						for (int m = 0; m < points.size(); m++){
							int mPlus = (m+1 >= points.size() ? 0 : m+1);
							if (polygon == polygon2 && (pNum == m || pNum == mPlus)){
								continue;
							}

							if (KPoint.linesIntersect(p, endOfRayPoint, points.get(m), points.get(mPlus))){
								KPoint intersection = KPoint.getLineLineIntersection(p, endOfRayPoint, points.get(m), points.get(mPlus));
								if (intersection != null){
									double dist = eye.distance(intersection);
									if (dist < closestDist){
										closestDist = dist;
										closestIntersectionPoint = intersection;
										closestOccluder = polygonAndDist.getOccluder();
										closestObstPolygonEdgeIndex = m;
										obstCloser = true;
									}
								}
							}
						}
					}
					int closestBoundaryPolygonEdgeIndex = -1;
					// also see if the closest intersection is with the boundaryPolygon
					if (closestIntersectionPoint == null || closestDist > minEyeToBoundaryPolygonPointDist){
						ArrayList<KPoint> points = boundaryPolygon.getPoints();
						for (int m = 0; m < points.size(); m++){
							if (xIndicator*boundaryPolygonXIndicators[m] == -1 || yIndicator*boundaryPolygonYIndicators[m] == -1){
								// intersection is not possible since the boundaryPolygon points and
								// the endOfRayPoint are in different quadrants, so skip to next occluder.
								continue;
							}
							int mPlus = (m+1 >= points.size() ? 0 : m+1);
							if (KPoint.linesIntersect(p, endOfRayPoint, points.get(m), points.get(mPlus))){
//								atLeastOneIntersection = true;
								KPoint intersection = KPoint.getLineLineIntersection(p, endOfRayPoint, points.get(m), points.get(mPlus));
								if (intersection != null){
									double dist = eye.distance(intersection);
									if (dist < closestDist){
										closestDist = dist;
										closestIntersectionPoint = intersection;
										closestBoundaryPolygonEdgeIndex = m;
										obstCloser = false;
										// There should only be one intersection with the boundaryPolygon, so we can break.
										break;
									}
								}
							}
						}


						// for debugging:
//						if (atLeastOneIntersection == false || closestIntersectionPoint != null && closestDist > maxEyeToBoundaryPolygonPointDist){
//							System.out.println(this.getClass().getSimpleName()+": atLeastOneIntersection == "+atLeastOneIntersection+", closestIntersectionPoint != null, closestDist == "+closestDist+", maxEyeToBoundaryPolygonPointDist == "+maxEyeToBoundaryPolygonPointDist+", minEyeToBoundaryPolygonPointDist == "+minEyeToBoundaryPolygonPointDist+", boundaryPolygon.contains(p) == "+boundaryPolygon.contains(p)+", eye.distance(p) == "+eye.distance(p));
//						}
					}
					if (closestIntersectionPoint != null){
						VisiblePoint newSightPoint = null;
						if (obstCloser){
							newSightPoint = new VPShadowOnOccluder(closestIntersectionPoint, closestOccluder, closestObstPolygonEdgeIndex, sp);
//							newSightPoint.quadrant = sp.quadrant;
//							newSightPoint.xOnY = sp.xOnY;
						}else{
							newSightPoint = new VPShadowOnBoundary(closestIntersectionPoint, closestBoundaryPolygonEdgeIndex, sp);
//							newSightPoint.quadrant = sp.quadrant;
//							newSightPoint.xOnY = sp.xOnY;
						}
						if (pPlusRCCW == -1 && pMinusRCCW == -1){
							visiblePoints.add(jPlus, newSightPoint);
							j++;
							continue;
						}else if (pPlusRCCW == 1 && pMinusRCCW == 1){
							visiblePoints.add(j, newSightPoint);
							j++;
							continue;
						}
//						if (pPlusRCCW == -1 && pMinusRCCW == -1){
//							shadowPointsToBeAdded.add(newSightPoint);
//							shadowPointsToBeAddedIndexes.add(jPlus);
//							continue;
//						}else if (pPlusRCCW == 1 && pMinusRCCW == 1){
//							shadowPointsToBeAdded.add(newSightPoint);
//							shadowPointsToBeAddedIndexes.add(j);
//							continue;
//						}
					}
				}
			}
		}

//		int nextInsert = 0;
//		int lastInsert = 0;
//		for (int i = 0; i < shadowPointsToBeAddedIndexes.size(); i++){
//			nextInsert = shadowPointsToBeAddedIndexes.get(i);
//			if (nextInsert == 0 && lastInsert > nextInsert){
//				// nextInsert == jPlus == 0 because j == sightPoints.size()-1,
//				// so the point can be inserted on the end or the beginning so
//				// add it on the end so that this simple loop logic works.
//				nextInsert = sightPoints.size();
//			}
//			for (int j = lastInsert; j < nextInsert; j++){
//				sightPoints2.add(sightPoints.get(j));
//			}
//			sightPoints2.add(shadowPointsToBeAdded.get(i));
//			lastInsert = nextInsert;
//		}
//		for (int j = lastInsert; j < sightPoints.size(); j++){
//			sightPoints2.add(sightPoints.get(j));
//		}
//		sightPoints.clear();
//		//sightPoints.addAll(sightPoints2);
//		//sightPoints2.clear();
//		shadowPointsToBeAdded.clear();
//		shadowPointsToBeAddedIndexes.clear();


//		codeTimer.click("make polygon");
		polygonAndDists.clear();
		cache.visiblePoints = visiblePoints;
		cache.visiblePolygon = createPolygonFromVisiblePoints(visiblePoints);
//		codeTimer.lastClick();
		return cache;
		
	}

	public KPolygon createPolygonFromVisiblePoints(ArrayList<VisiblePoint> visiblePoints){
		KPolygon visiblePolygon;
		ArrayList<KPoint> pointList = new ArrayList<KPoint>(visiblePoints.size());
		for (int i = 0; i < visiblePoints.size(); i++){
			pointList.add(visiblePoints.get(i).getPoint().copy());
		}
		if (pointList.size() >= 3){
			// should check that points do not make intersecting lines
			//KPolygon.isValidNoLineIntersections(pointList);
			visiblePolygon = new KPolygon(pointList);
		}else{
			visiblePolygon = null;//visiblePolygon.copy();
		}
		return visiblePolygon;
	}

	protected int getXIndicator(KPolygon poly, KPoint p2){
		int xIndicator;
		double relX = poly.getCenter().x - p2.getX();
		if (relX - poly.getRadius() > 0){
			xIndicator = 1;
		}else if (relX + poly.getRadius() < 0){
			xIndicator = -1;
		}else{
			xIndicator = 0;
		}
		return xIndicator;
	}

	protected int getYIndicator(KPolygon poly, KPoint p2){
		int yIndicator;
		double relY = poly.getCenter().y - p2.getY();
		if (relY - poly.getRadius() > 0){
			yIndicator = 1;
		}else if (relY + poly.getRadius() < 0){
			yIndicator = -1;
		}else{
			yIndicator = 0;
		}
		return yIndicator;
	}
	// p2 is the eye
	protected int getXIndicator(KPoint p0, KPoint p1, KPoint p2){
		int xIndicator;
		double relX0 = p0.x - p2.x;
		double relX1 = p1.x - p2.x;
		if (relX0 < 0 && relX1 < 0){
			xIndicator = -1;
		}else if (relX0 > 0 && relX1 > 0){
			xIndicator = 1;
		}else{
			xIndicator = 0;
		}
		return xIndicator;
	}
	protected int getYIndicator(KPoint p0, KPoint p1, KPoint p2){
		int yIndicator;
		double relY0 = p0.y - p2.y;
		double relY1 = p1.y - p2.y;
		if (relY0 < 0 && relY1 < 0){
			yIndicator = -1;
		}else if (relY0 > 0 && relY1 > 0){
			yIndicator = 1;
		}else{
			yIndicator = 0;
		}
		return yIndicator;
	}
	protected int getXIndicator(KPoint p, KPoint p2){
		int xIndicator;
		double relX = p.x - p2.x;
		if (relX > 0){
			xIndicator = 1;
		}else if (relX < 0){
			xIndicator = -1;
		}else{
			xIndicator = 0;
		}
		return xIndicator;
	}

	protected int getYIndicator(KPoint p, KPoint p2){
		int yIndicator;
		double relY = p.y - p2.y;
		if (relY > 0){
			yIndicator = 1;
		}else if (relY < 0){
			yIndicator = -1;
		}else{
			yIndicator = 0;
		}
		return yIndicator;
	}


}