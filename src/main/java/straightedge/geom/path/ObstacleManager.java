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

import straightedge.geom.util.*;
import straightedge.geom.*;
import java.util.*;
/**
 *
 * @author Keith
 */
public class ObstacleManager<T extends PathBlockingObstacle>{
	public TileBag<T> tileBag;
	public NodeConnector<T> nodeConnector;
	public double maxConnectionDistance;

	public ObstacleManager(TileBag tileBag, double maxConnectionDistance){
		this.tileBag = tileBag;
		this.maxConnectionDistance = maxConnectionDistance;
		nodeConnector = new NodeConnector<T>();
	}

	public void addObstacles(Collection<T> newObstacles){
		// This method re-adds the obstacleList one by one which can be faster
		// since nodes are only checked against each other once rather than twice.
		for (T obst : newObstacles){
			this.addObstacle(obst);
		}
	}
	

	public void addObstacle(T obst){
		tileBag.add(obst);
		nodeConnector.addObstacle(obst, tileBag, maxConnectionDistance);
	}

	public void removeObstacle(T obst){
		long startTime = System.nanoTime();
		nodeConnector.clearConnectionsToRemovedObstacleNodes(obst);
		tileBag.remove(obst);
		KPolygon poly = obst.getInnerPolygon();
		ArrayList<T> nearByObstacles = tileBag.getAllWithin(poly.getCenter(), poly.getRadius() + maxConnectionDistance);
		// Any nodes that may have been contained but now aren't need to be marked as so.
		for (T nearByObstacle : nearByObstacles){
			for (KNodeOfObstacle node : nearByObstacle.getNodes()){
				if (poly.getCenter().distance(node.getPoint()) <= poly.getRadius()){
					node.resetContainedToUnknown();
				}
			}
		}

		ArrayList<KNodeOfObstacle> nodesToBeReconnected = new ArrayList<KNodeOfObstacle>();
		for (T nearByObstacle : nearByObstacles){
			for (KNodeOfObstacle node : nearByObstacle.getNodes()){
				if (node.getPoint().distance(poly.getCenter()) < maxConnectionDistance + poly.getRadius()){
					nodesToBeReconnected.add(node);
				}
			}
		}
		nodeConnector.reConnectNodesAroundRemovedObstacle(obst, nodesToBeReconnected, nearByObstacles, maxConnectionDistance);

		long endTime = System.nanoTime();
		System.out.println(this.getClass().getSimpleName()+".removeObstacle running time = "+((endTime - startTime)/1000000000f));
	}

	public void remakeConnectionsBetweenAllObstacles(double maxConnectionDistance){
		this.maxConnectionDistance = maxConnectionDistance;
		// This method re-adds the obstacleList one by one which can be faster
		// since nodes are only checked against each other once rather than twice.
		Bag<T> copyOfObstacles = new Bag<T>(tileBag.size());
		copyOfObstacles.addAll(tileBag);
		tileBag.clear();

		long startTime = System.nanoTime();
		for (T obst : copyOfObstacles){
			this.addObstacle(obst);
		}
		long endTime = System.nanoTime();
		System.out.println(this.getClass().getSimpleName()+".remakeConnectionsBetweenAllObstacles addObstacle running time = "+((endTime - startTime)/1000000000f));
	}

	public TileBag<T> getTileBag() {
		return tileBag;
	}

	public double getMaxConnectionDistance() {
		return maxConnectionDistance;
	}

	public NodeConnector<T> getNodeConnector() {
		return nodeConnector;
	}


}
