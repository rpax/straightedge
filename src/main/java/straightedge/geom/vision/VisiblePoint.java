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

public abstract class VisiblePoint implements Comparable{
	public static int OCCLUDER = 0;
	public static int BOUNDARY = 1;
	public static int OCCLUDER_OCCLUDER_INTERSECTION = 2;
	public static int OCCLUDER_BOUNDARY_INTERSECTION = 3;
	public static int SHADOW_ON_OCCLUDER = 4;
	public static int SHADOW_ON_BOUNDARY = 5;

	KPoint point;

	// These are used in the compareTo method to sort the points around the eye.
	// This method is a bit quicker than using the slow trig functions to find the angle.
	boolean posY;
	double xOnY;

	public VisiblePoint(){

	}
	public VisiblePoint(KPoint point){
		this.point = point;
	}

	public void preSortCalcs(KPoint eye){
		double relX = point.x - eye.x;
		double relY = point.y - eye.y;
//		if (relX >= 0){
//			if (relY >= 0){
//				quadrant = 0;
//			}else{
//				quadrant = 3;
//			}
//		}else{
//			if (relY >= 0){
//				quadrant = 1;
//			}else{
//				quadrant = 2;
//			}
//		}
		if (relY >= 0){
			posY = true;
		}else{
			posY = false;
		}
		xOnY = relX/relY;
	}

	abstract public int getType();

	public KPoint getPoint(){
		return point;
	}

	public int compareTo(Object ob){
		assert ob instanceof VisiblePoint : ob;
		VisiblePoint other = (VisiblePoint)ob;
		if (posY == other.posY){
			double otherXOnY = other.xOnY;
			if (xOnY < otherXOnY){
				return 1;
			}else if (xOnY > otherXOnY){
				return -1;
			}else{
				return 0;
			}
		}else if (posY == true){
			return 1;
		}else{
			return -1;
		}
	}

//	public int compareTo(Object ob){
//		assert ob instanceof VisiblePoint : ob;
//		double thisAngle = angleRelativeToEye;
//		double obAngle = ((VisiblePoint)ob).getAngleRelativeToEye();
//		if (thisAngle > obAngle){
//			return 1;
//		}else if (thisAngle < obAngle){
//			return -1;
//		}else{
//			return 0;
//		}
//	}


}