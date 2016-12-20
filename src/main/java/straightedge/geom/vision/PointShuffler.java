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
import java.util.*;

/**
 * @author Keith
 */
public class PointShuffler {
	public Random rand;
	public int maxNumTries;
	public double maxPointMoveDist;

	public PointShuffler(){
		this(0.001, 100);
	}
	public PointShuffler(double maxPointMoveDist, int maxNumTries){
		rand = new Random();
		this.maxPointMoveDist = maxPointMoveDist;
		this.maxNumTries = maxNumTries;
	}
	public void shufflePoint(KPolygon poly, int pointIndex){
		KPoint p = poly.getPoints().get(pointIndex);
		KPoint pNext = p.createPointToward(poly.getNextPoint(pointIndex), 10);
		KPoint pPrev = p.createPointToward(poly.getPrevPoint(pointIndex), 10);
		KPoint mid = pNext.midPoint(pPrev);
		double oldPX = p.x;
		double oldPY = p.y;
		boolean done = false;
		// Here we attempt to move the point at a random angle but in
		// the general direction away from the polygon
		for (int j = 0; j < maxNumTries; j++){
			double angle = mid.findAngle(p) + ((rand.nextFloat()-0.5f)*Math.PI/5f);
			KPoint newPoint = p.createPointFromAngle(angle, maxPointMoveDist);
			p.x = newPoint.x;
			p.y = newPoint.y;
			poly.calcAll();
			if (poly.isValidNoLineIntersections() == true){
				done = true;
				break;
			}else{
				p.x = oldPX;
				p.y = oldPY;
			}
		}
		// Here we attempt to move the point at a random angle but in
		// the general direction into the center of the polygon
		if (done == false){
			for (int j = 0; j < maxNumTries; j++){
				double angle = mid.findAngle(p) + ((rand.nextFloat()-0.5f)*Math.PI/5f) + Math.PI;
				KPoint newPoint = p.createPointFromAngle(angle, maxPointMoveDist);
				p.x = newPoint.x;
				p.y = newPoint.y;
				poly.calcAll();
				if (poly.isValidNoLineIntersections() == true){
					done = true;
					break;
				}else{
					p.x = oldPX;
					p.y = oldPY;
				}
			}
		}
	}
	public void shufflePoints(KPolygon poly){
		for (int i = 0; i < poly.getPoints().size(); i++){
			shufflePoint(poly, i);
		}
	}

	public int getMaxNumTries() {
		return maxNumTries;
	}

	public void setMaxNumTries(int maxNumTries) {
		this.maxNumTries = maxNumTries;
	}

	public double getMaxPointMoveDist() {
		return maxPointMoveDist;
	}

	public void setMaxPointMoveDist(float maxPointMoveDist) {
		this.maxPointMoveDist = maxPointMoveDist;
	}


}
