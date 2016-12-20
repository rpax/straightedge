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
package straightedge.geom.path;

import straightedge.geom.PolygonBufferer;
import straightedge.geom.*;
import java.util.*;


/**
 *
 * @author Keith Woodward
 */
public class PathBlockingObstacleImpl implements PathBlockingObstacle{
	public static float BUFFER_AMOUNT = 0.01f;
	public static int NUM_POINTS_IN_A_QUADRANT = 1;

	public KPolygon outerPolygon;
	public KPolygon innerPolygon;
	public ArrayList<KNodeOfObstacle> nodes;
	
	public PathBlockingObstacleImpl(){
	}
	
	public PathBlockingObstacleImpl(KPolygon outerPolygon, KPolygon innerPolygon){
		this.outerPolygon = outerPolygon;
		this.innerPolygon = innerPolygon;
		resetNodes();
		assert outerPolygon.intersectsPerimeter(innerPolygon) == false : "\n"+outerPolygon.toString()+"\n"+innerPolygon.toString();
		
	}
	public void resetNodes(){
		if (nodes == null){
			nodes = new ArrayList<KNodeOfObstacle>();
			for (int i = 0; i < this.outerPolygon.getPoints().size(); i++){
				nodes.add(new KNodeOfObstacle(this, i));
			}
		}else if (nodes.size() != getOuterPolygon().getPoints().size()){
			nodes.clear();
			for (int i = 0; i < this.outerPolygon.getPoints().size(); i++){
				nodes.add(new KNodeOfObstacle(this, i));
			}
		}else{
			for (int j = 0; j < nodes.size(); j++){
				KNodeOfObstacle node = nodes.get(j);
				KPoint outerPolygonPoint = getOuterPolygon().getPoint(j);
				node.getPoint().x = outerPolygonPoint.x;
				node.getPoint().y = outerPolygonPoint.y;
			}
		}
	}

	public static PathBlockingObstacleImpl createObstacleFromOuterPolygon(KPolygon outerPolygon){
		PolygonBufferer polygonBufferer = new PolygonBufferer();
		KPolygon innerPolygon = polygonBufferer.buffer(outerPolygon, -1*BUFFER_AMOUNT, NUM_POINTS_IN_A_QUADRANT);
		if (innerPolygon == null){
			// there was an error so return null;
			return null;
		}
		PathBlockingObstacleImpl pathBlockingObstacleImpl = new PathBlockingObstacleImpl(outerPolygon, innerPolygon);
		return pathBlockingObstacleImpl;
	}

	public static PathBlockingObstacleImpl createObstacleFromInnerPolygon(KPolygon innerPolygon){
		PolygonBufferer polygonBufferer = new PolygonBufferer();
		KPolygon outerPolygon = polygonBufferer.buffer(innerPolygon, BUFFER_AMOUNT, NUM_POINTS_IN_A_QUADRANT);
		if (outerPolygon == null){
			// there was an error so return null;
			return null;
		}
		PathBlockingObstacleImpl pathBlockingObstacleImpl = new PathBlockingObstacleImpl(outerPolygon, innerPolygon);
		return pathBlockingObstacleImpl;
	}
	
	public ArrayList<KNodeOfObstacle> getNodes() {
		return nodes;
	}

	public KPolygon getOuterPolygon() {
		return outerPolygon;
	}

	public KPolygon getInnerPolygon() {
		return innerPolygon;
	}

	public KPolygon getPolygon(){
		return this.getInnerPolygon();
	}

	public void setOuterPolygon(KPolygon outerPolygon){
		this.outerPolygon = outerPolygon;
	}

	public void setInnerPolygon(KPolygon innerPolygon){
		this.innerPolygon = innerPolygon;
	}
}
