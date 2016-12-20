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

import java.util.*;
import com.vividsolutions.jts.geom.*;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
	
/**
 *
 * @author Keith Woodward
 */
public class PolygonConverter{
	public GeometryFactory geometryFactory = new GeometryFactory();

	public PolygonConverter(){
	}
	
	public com.vividsolutions.jts.geom.Polygon makeJTSPolygonFrom(KPolygon polygon){
		com.vividsolutions.jts.geom.Polygon jtsPolygon;
		Coordinate[] coordinateArray = new Coordinate[polygon.getPoints().size() + 1];
		for (int i = 0; i < polygon.getPoints().size(); i++){
			KPoint p = polygon.getPoints().get(i);
			coordinateArray[i] = new Coordinate(p.x, p.y);
		}
		// link the first and last points
		coordinateArray[polygon.getPoints().size()] = new Coordinate(coordinateArray[0].x, coordinateArray[0].y);
		LinearRing linearRing = geometryFactory.createLinearRing(coordinateArray);
		jtsPolygon = new com.vividsolutions.jts.geom.Polygon(linearRing, null, geometryFactory);
		return jtsPolygon;
	}

	public com.vividsolutions.jts.geom.Polygon makeJTSPolygonFrom(KMultiPolygon polygon){
		com.vividsolutions.jts.geom.Polygon jtsPolygon;
		KPolygon exteriorPolygon = polygon.getExteriorPolygon();
		Coordinate[] coordinateArray = new Coordinate[exteriorPolygon.points.size() + 1];
		for (int i = 0; i < exteriorPolygon.getPoints().size(); i++){
			KPoint p = exteriorPolygon.getPoints().get(i);
			coordinateArray[i] = new Coordinate(p.x, p.y);
		}
		// link the first and last points
		coordinateArray[exteriorPolygon.getPoints().size()] = new Coordinate(coordinateArray[0].x, coordinateArray[0].y);
		LinearRing exteriorLinearRing = geometryFactory.createLinearRing(coordinateArray);

		
		ArrayList<KPolygon> interiorPolygons = polygon.getInteriorPolygonsCopy();
		ArrayList<LinearRing> interiorLinearRings = new ArrayList<LinearRing>(interiorPolygons.size());
		for (int i = 0; i < interiorPolygons.size(); i++){
			KPolygon interiorPolygon = interiorPolygons.get(i);
			coordinateArray = new Coordinate[interiorPolygon.points.size() + 1];
			for (int j = 0; j < interiorPolygon.getPoints().size(); j++){
				KPoint p = interiorPolygon.getPoints().get(j);
				coordinateArray[j] = new Coordinate(p.x, p.y);
			}
			// link the first and last points
			coordinateArray[interiorPolygon.getPoints().size()] = new Coordinate(coordinateArray[0].x, coordinateArray[0].y);
			LinearRing interiorLinearRing = geometryFactory.createLinearRing(coordinateArray);
			interiorLinearRings.add(interiorLinearRing);
		}

		LinearRing[] interiorLinearRingArray = interiorLinearRings.toArray(new LinearRing[interiorLinearRings.size()]);
		jtsPolygon = new com.vividsolutions.jts.geom.Polygon(exteriorLinearRing, interiorLinearRingArray, geometryFactory);
		return jtsPolygon;
	}

	/**
	 * Returns a KPolygon of this polygon, or returns null if there was a problem.
	 * For example, if there are less than 3 points, returns null.
	 * If there are more than 3 points but consecutive points have the same coordinates,
	 * and when the duplicates are deleted then there are less than 3 points.
	 * @param jtsPolygon
	 * @return
	 */
	public KPolygon makeKPolygonFromExterior(com.vividsolutions.jts.geom.Polygon jtsPolygon){
		LineString exteriorRingLineString = jtsPolygon.getExteriorRing();
		KPolygon polygon = makeKPolygonFrom(exteriorRingLineString);
		return polygon;
	}
	public KMultiPolygon makeKMultiPolygonFrom(com.vividsolutions.jts.geom.Polygon jtsPolygon){
		LineString exteriorRingLineString = jtsPolygon.getExteriorRing();
		KPolygon polygon = makeKPolygonFrom(exteriorRingLineString);
		if (polygon == null){
			return null;
		}
		KMultiPolygon multiPolygon = new KMultiPolygon(polygon);
		for (int i = 0; i < jtsPolygon.getNumInteriorRing(); i++){
			LineString interiorRingLineString = jtsPolygon.getInteriorRingN(i);
			KPolygon internalPolygon = makeKPolygonFrom(interiorRingLineString);
			if (internalPolygon == null){
				continue;
			}
			multiPolygon.getPolygons().add(internalPolygon);
		}
		return multiPolygon;
	}
	public KPolygon makeKPolygonFrom(com.vividsolutions.jts.geom.LineString lineString){
		CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
		ArrayList<KPoint> points = new ArrayList<KPoint>();
		// The loop stops at the second-last coord since the last coord will be
		// the same as the start coord.
		KPoint lastAddedPoint = null;
		for (int i = 0; i < coordinateSequence.size()-1; i++){
			Coordinate coord = coordinateSequence.getCoordinate(i);
			KPoint p = new KPoint(coord.x, coord.y);
			if (lastAddedPoint != null && p.x == lastAddedPoint.x && p.y == lastAddedPoint.y){
				// Don't add the point since it's the same as the last one
//				System.out.println(this.getClass().getSimpleName()+": skipping p == "+p+", lastAddedPoint == "+lastAddedPoint+", i == "+i+", coordinateSequence.size()-1 == "+(coordinateSequence.size()-1));
				continue;
			}else{
				points.add(p);
				lastAddedPoint = p;
			}
		}
		if (points.size() < 3){
			return null;
		}
		KPolygon polygon = new KPolygon(points);
		return polygon;
	}
	

//	public ArrayList<KPolygon> makeKPolygonsFrom(com.vividsolutions.jts.geom.MultiPolygon jtsMultiPolygon){
//		ArrayList<KPolygon> polygons = new ArrayList<KPolygon>();
//		int numGeomtries = jtsMultiPolygon.getNumGeometries();
//		for (int i = 0; i < numGeomtries; i++){
//			Geometry geom = jtsMultiPolygon.getGeometryN(i);
//			if (geom instanceof Polygon){
//				Polygon jtsPoly = (Polygon)geom;
//				LineString lineString = jtsPoly.getExteriorRing();
//				CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
//				ArrayList<KPoint> points = new ArrayList<KPoint>(coordinateSequence.size());
//				// The loop stops at the second-last coord since the last coord will be
//				// the same as the start coord.
//				for (int j = 0; j < coordinateSequence.size()-1; j++){
//					Coordinate coord = coordinateSequence.getCoordinate(j);
//					points.add(new KPoint(coord.x, coord.y));
//				}
//				KPolygon poly = new KPolygon(points);
//				polygons.add(poly);
//
//			}else{
//				System.out.println(this.getClass().getSimpleName()+": geom.getClass() == "+geom.getClass());
//				System.out.println(this.getClass().getSimpleName()+": numGeomtries == "+numGeomtries);
//				if (geom instanceof LineString){
//					LineString lineString = (LineString)geom;
//					System.out.println(this.getClass().getSimpleName()+": lineString.isRing() == "+lineString.isRing());
//				}
//				//throw new RuntimeException("geom is not a polygon, geom == "+geom);
//			}
//		}
//		return polygons;
//	}

	public ArrayList<KMultiPolygon> makeKMultiPolygonListFrom(Geometry geometry){
		ArrayList<KMultiPolygon> list = new ArrayList<KMultiPolygon>();
		if (geometry instanceof com.vividsolutions.jts.geom.Polygon){
			com.vividsolutions.jts.geom.Polygon jtsIntersectionPoly = (com.vividsolutions.jts.geom.Polygon)geometry;
			KMultiPolygon path2D = makeKMultiPolygonFrom(jtsIntersectionPoly);
			if (path2D != null){
				list.add(path2D);
			}
		}else if (geometry instanceof com.vividsolutions.jts.geom.MultiPolygon){
			com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon)geometry;
			addGeometryToKMultiPolygonList(multiPolygon, list);
		}else if (geometry instanceof com.vividsolutions.jts.geom.GeometryCollection){
			/* Even though MultiPolygon extends GeometryCollection, sometimes a
			 * buffer or intersection operation will result in random points, in
			 * addition to Polygons and MultiPolygons. For this reason we should
			 * still deal with GeometryCollections, and simply ignore the random points.
			 */
			com.vividsolutions.jts.geom.GeometryCollection geometryCollection = (com.vividsolutions.jts.geom.GeometryCollection)geometry;
			addGeometryToKMultiPolygonList(geometryCollection, list);
		}else{
			// Sometimes these are found:
			//com.vividsolutions.jts.geom.Point
			//com.vividsolutions.jts.geom.LineString
		}
		return list;
	}
	public void addGeometryToKMultiPolygonList(Geometry geometry, ArrayList<KMultiPolygon> list){
		if (geometry instanceof com.vividsolutions.jts.geom.Polygon){
			com.vividsolutions.jts.geom.Polygon jtsIntersectionPoly = (com.vividsolutions.jts.geom.Polygon)geometry;
			KMultiPolygon path2D = makeKMultiPolygonFrom(jtsIntersectionPoly);
			if (path2D != null){
				list.add(path2D);
			}
		}else if (geometry instanceof com.vividsolutions.jts.geom.GeometryCollection){
			// GeometryCollection is the super-class of MultiPolygon and
			// MultiLineString and MultiPoint so these ones are taken care of in this code block.
			com.vividsolutions.jts.geom.GeometryCollection geometryCollection = (com.vividsolutions.jts.geom.GeometryCollection)geometry;
			for (int i = 0; i < geometryCollection.getNumGeometries(); i++){
				Geometry internalGeometry = geometryCollection.getGeometryN(i);
				addGeometryToKMultiPolygonList(internalGeometry, list);
			}
		}else{
			// Sometimes these are found:
			//com.vividsolutions.jts.geom.Point
			//com.vividsolutions.jts.geom.LineString
			//System.out.println(this.getClass().getSimpleName()+": geometry.getClass() == "+geometry.getClass());
		}
	}



	public Path2D.Double makePath2DFrom(Geometry geometry){
		Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		addGeometryToPath2D(geometry, path);
		return path;
	}

	public void addGeometryToPath2D(Geometry geometry, Path2D.Double path){
		if (geometry instanceof com.vividsolutions.jts.geom.Polygon){
			com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon)geometry;
			addPolygonToPath2D(polygon, path);
		}else if (geometry instanceof com.vividsolutions.jts.geom.GeometryCollection){
			// GeometryCollection is the super-class of MultiPolygon and
			// MultiLineString and MultiPoint so these ones are taken care of in this code block.
			com.vividsolutions.jts.geom.GeometryCollection geometryCollection = (com.vividsolutions.jts.geom.GeometryCollection)geometry;
			for (int i = 0; i < geometryCollection.getNumGeometries(); i++){
				Geometry internalGeometry = geometryCollection.getGeometryN(i);
				addGeometryToPath2D(internalGeometry, path);
			}
		}else{
			// Sometimes these are found:
			//com.vividsolutions.jts.geom.Point
			//com.vividsolutions.jts.geom.LineString
			//System.out.println(this.getClass().getSimpleName()+": geometry.getClass() == "+geometry.getClass());
		}
	}

	public void addPolygonToPath2D(Polygon jtsPolygon, Path2D path){
		LineString lineString = jtsPolygon.getExteriorRing();
		addLineStringToPath2D(lineString, path);
		for (int i = 0; i < jtsPolygon.getNumInteriorRing(); i++){
			lineString = jtsPolygon.getInteriorRingN(i);
			addLineStringToPath2D(lineString, path);
		}
	}

	public void addLineStringToPath2D(LineString lineString, Path2D path){
		CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
		if (coordinateSequence.size() == 0){
			// sometimes JTS gives an empty LineString
			return;
		}
		// add the first coord to the path
		Coordinate coord = coordinateSequence.getCoordinate(0);
		path.moveTo(coord.x, coord.y);
		// The loop stops at the second-last coord since the last coord will be
		// the same as the start coord.
		for (int i = 1; i < coordinateSequence.size()-1; i++){
			coord = coordinateSequence.getCoordinate(i);
			path.lineTo(coord.x, coord.y);
		}
		path.closePath();
	}

	public GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}
	

	public ArrayList<KMultiPolygon> makeKMultiPolygonListFrom(ArrayList<KPolygon> polygons){
		ArrayList<KMultiPolygon> multiPolygons = new ArrayList<KMultiPolygon>();
		OuterLoop:
		for (int i = 0; i < polygons.size(); i++){
			KPolygon polygon = polygons.get(i).copy();
			KMultiPolygon mpoly = new KMultiPolygon(polygon);
			for (int j = 0; j < multiPolygons.size(); j++){
				KMultiPolygon mpoly2 = multiPolygons.get(j);
				if (mpoly2.getExteriorPolygon().contains(polygon.getPoint(0))){
					mpoly2.getPolygons().add(polygon);
					continue OuterLoop;
				}else if (polygon.contains(mpoly2.getExteriorPolygon().getPoint(0))){
					multiPolygons.remove(j);
					j--;
					mpoly.getPolygons().add(mpoly2.getExteriorPolygon());
					mpoly.getPolygons().addAll(mpoly2.getInteriorPolygonsCopy());
					// Note that we might need to swallow up other mpolygons too, so keep going.
					continue;
				}
			}
			multiPolygons.add(mpoly);
		}
		return multiPolygons;
	}
	public ArrayList<KMultiPolygon> makeKMultiPolygonListFrom(Shape shape, double flatness){
		ArrayList<KPolygon> list = makeKPolygonListFrom(shape, flatness);
		ArrayList<KMultiPolygon> multiPolygon = makeKMultiPolygonListFrom(list);
		return multiPolygon;
	}

	public ArrayList<KPolygon> makeKPolygonListFrom(Shape shape, double flatness){
		ArrayList<KPolygon> list = new ArrayList<KPolygon>();
		PathIterator pathIterator = shape.getPathIterator(null, flatness);
		double[] coords = new double[6];
		while (pathIterator.isDone() == false){
			int type = pathIterator.currentSegment(coords);
			if (type == PathIterator.SEG_MOVETO){
				ArrayList<KPoint> points = new ArrayList<KPoint>();
				addPathIteratorPointsToList(pathIterator, points);
				// remove last point if it's the same as the first one.
				while (points.size() >= 2){
					KPoint firstPoint = points.get(0);
					KPoint lastPoint = points.get(points.size()-1);
					if (firstPoint.equals(lastPoint)){
						points.remove(points.size()-1);
					}else{
						break;
					}
				}
				KPolygon polygon = new KPolygon(points, true);
				list.add(polygon);
			}
		}
		return list;
	}

	public void addPathIteratorPointsToList(PathIterator pathIterator, ArrayList<KPoint> points){
		double[] coords = new double[6];
		while (pathIterator.isDone() == false){
			int type = pathIterator.currentSegment(coords);
			if (type == PathIterator.SEG_MOVETO){
				points.add(new KPoint(coords[0], coords[1]));
				pathIterator.next();
			}else if (type == PathIterator.SEG_LINETO){
				points.add(new KPoint(coords[0], coords[1]));
				pathIterator.next();
			}else if (type == PathIterator.SEG_CLOSE){
				pathIterator.next();
				return;
			}
		}
	}

	public Path2D.Double makePath2DFrom(ArrayList<KMultiPolygon> multiPolygons){
		Path2D.Double path = new Path2D.Double(PathIterator.WIND_EVEN_ODD);
		for (int h = 0; h < multiPolygons.size(); h++){
			KMultiPolygon multiPolygon = multiPolygons.get(h);
			ArrayList<KPolygon> polygons = multiPolygon.getPolygons();
			for (int i = 0; i < polygons.size(); i++){
				KPolygon polygon = polygons.get(i);
				KPoint p = polygon.getPoint(0);
				path.moveTo(p.x, p.y);
				for (int j = 1; j < polygon.getPoints().size(); j++){
					p = polygon.getPoint(j);
					path.lineTo(p.x, p.y);
				}
				path.closePath();
			}
		}
		return path;
	}
	public Path2D.Double makePath2DFrom(KMultiPolygon multiPolygon){
		Path2D.Double path = new Path2D.Double(PathIterator.WIND_EVEN_ODD);
		ArrayList<KPolygon> polygons = multiPolygon.getPolygons();
		for (int i = 0; i < polygons.size(); i++){
			KPolygon polygon = polygons.get(i);
			KPoint p = polygon.getPoint(0);
			path.moveTo(p.x, p.y);
			for (int j = 1; j < polygon.getPoints().size(); j++){
				p = polygon.getPoint(j);
				path.lineTo(p.x, p.y);
			}
			path.closePath();
		}
		return path;
	}
	public Path2D.Double makePath2DFrom(KPolygon polygon){
		Path2D.Double path = new Path2D.Double(PathIterator.WIND_EVEN_ODD);
		KPoint p = polygon.getPoint(0);
		path.moveTo(p.x, p.y);
		for (int j = 1; j < polygon.getPoints().size(); j++){
			p = polygon.getPoint(j);
			path.lineTo(p.x, p.y);
		}
		path.closePath();
		return path;
	}

	public void printGeometry(Geometry geometry){
		if (geometry instanceof com.vividsolutions.jts.geom.Polygon){
			com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon)geometry;
			System.out.println(this.getClass().getSimpleName()+": geometry.getClass() == "+geometry.getClass());
			Coordinate[] coords = geometry.getCoordinates();
			for (int i = 0; i < coords.length; i++){
				System.out.println(this.getClass().getSimpleName()+": coords["+i+"] == "+coords[i].x+", "+coords[i].y);
			}
		}else if (geometry instanceof com.vividsolutions.jts.geom.GeometryCollection){
			// GeometryCollection is the super-class of MultiPolygon and
			// MultiLineString and MultiPoint so these ones are taken care of in this code block.
			com.vividsolutions.jts.geom.GeometryCollection geometryCollection = (com.vividsolutions.jts.geom.GeometryCollection)geometry;
			System.out.println(this.getClass().getSimpleName()+": geometryCollection.getClass() == "+geometryCollection.getClass());
			for (int i = 0; i < geometryCollection.getNumGeometries(); i++){
				Geometry internalGeometry = geometryCollection.getGeometryN(i);
				printGeometry(internalGeometry);
			}
		}else{
			// Sometimes these are found:
			//com.vividsolutions.jts.geom.Point
			//com.vividsolutions.jts.geom.LineString
			System.out.println(this.getClass().getSimpleName()+": geometry.getClass() == "+geometry.getClass());
		}
	}
}
