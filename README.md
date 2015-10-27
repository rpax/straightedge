# Purpose
A 2D polygon library aiming to provide path-finding and lighting using polygons rather than tiles.

# Description

Includes 2 main parts:

- Path finding through 2D polygons using the A star algorithm and navigation-mesh generation
    -  Field of vision / shadows / line of sight / lighting
    - The basic polygon and point classes are the KPolygon and KPoint. KPolygon contains a list of KPoints for vertices as well as a center (centroid), area, and radius (circular bound or distance from center to furthest point). KPolygon was born out of the need for a more game-oriented and flexible polygon class than the Path2D class in the standard Java library. KPolygon implements java.awt.geom.Shape so it can be easily drawn and filled by Java2D's Graphics2D object.

- This API provides path-finding and field-of-vision. For other complex geometric operations such as buffering (fattening and shrinking) and constructive area geometry (intersections and unions) it is recommended to use the excellent Java Topology Suite (JTS). The standard Java2D library also provides the Area class which can be used for some constructive area geometry operations. Note that there is a utility class PolygonConverter that can quickly convert KPolygons to JTS polygons and vice versa.

# About
StraightEdge was written by Keith Woodward as an independent hobby project. Project contributors are welcome.

# Usage
Here are some code snippets to show you how to use the path-finding and lighting code. Click on the headings to see a complete self-contained example.

## Path-finding usage:

    // Make the obstacles which block our path
    ArrayList<PathBlockingObstacle> stationaryObstacles = new ArrayList<PathBlockingObstacle>();
    KPolygon polygon = KPolygon.createRectOblique(new KPoint(-1, 100), new KPoint(-1, 200), 50);
    PathBlockingObstacle pathBlockingObstacle = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(polygon);
    stationaryObstacles.add(pathBlockingObstacle);
    
    // Make the NodeConnector connect the obstacles' nodes (nodes are just the obstacle polygon's points).
    NodeConnector nodeConnector = new NodeConnector();
    double maxConnectionDistanceBetweenObstacles = 1000;
    for (int k = 0; k < stationaryObstacles.size(); k++){
      nodeConnector.addObstacle(stationaryObstacles.get(k), stationaryObstacles, maxConnectionDistanceBetweenObstacles);
    }
    
    // Initialise the PathFinder
    PathFinder pathFinder = new PathFinder();
    KPoint startPoint = new KPoint(0, 0);
    KPoint endPoint = new KPoint(0, 300);
    
    // This is used to store the result:
    PathData pathData = new PathData();
    
    // Calculate the shortest path
    double maxConnectionDistanceFromStartAndEndPointsToObstacles = maxConnectionDistanceBetweenObstacles;
    pathData = pathFinder.calc(startPoint, endPoint, maxConnectionDistanceFromStartAndEndPointsToObstacles, nodeConnector, stationaryObstacles);
    
    // Draw the result which is stored in pathData
    ArrayList<KPoint> pathPoints = pathData.points;
    Graphics2D g = (Graphics2D)someComponent.getGraphics();
    g.setColor(Color.RED);
    if (pathPoints.size() > 0){
      KPoint p = pathPoints.get(0);
      for (int i = 1; i < pathPoints.size(); i++) {
        KPoint p2 = pathPoints.get(i);
        g.draw(new Line2D.Double(p.x, p.y, p2.x, p2.y));
        float d = 5f;
        g.fill(new Ellipse2D.Double(p2.x - d / 2f, p2.y - d / 2f, d, d));
        p = p2;
      }
    }

## Field-of-vision or lighting usage:

    // Make the field of vision polygon
    int numPoints = 20;
    float radius = 250;
    KPolygon boundaryPolygon = KPolygon.createRegularPolygon(numPoints, radius);
    
    // Make the eye which is like the light-source
    KPoint eye = new KPoint(0, 0);
    
    // The VisionData just contains the eye and boundary polygon,
    // and also the results of the VisionFinder calculations.
    VisionData visionData = new VisionData(eye, boundaryPolygon);
    VisionFinder visionFinder = new VisionFinder();
    
    // Make the occluders which block our vision
    ArrayList<Occluder> occluders = new ArrayList<Occluder>();
    KPolygon polygon = KPolygon.createRectOblique(new KPoint(0, 100), new KPoint(0, 200), 50);
    Occluder occluder = new OccluderImpl(polygon);
    occluders.add(occluder);
    
    // Calculate what's visible
    visionFinder.calc(visionData, occluders);
    
    // Draw the result which is stored in visionData
    Graphics2D g = (Graphics2D)someComponent.getGraphics();
    if (visionData.visiblePolygon != null){
      g.setColor(Color.RED);
      g.fill(visionData.visiblePolygon);
    }