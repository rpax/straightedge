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

/**
 * Used to help sort the polygon lists.
 * @author Keith
 */
public class OccluderDistAndQuad implements Comparable{
	public Occluder occluder;
	public double distEyeToCenterLessRadius;
	public double distEyeToCenterLessRadiusSqSigned;
	public int xIndicator;
	public int yIndicator;

	public OccluderDistAndQuad(Occluder occluder, double distCenterToEyeLessRadius, double distEyeToCenterLessRadiusSqSigned, int xIndicator, int yIndicator){
		this.occluder = occluder;
		this.distEyeToCenterLessRadius = distCenterToEyeLessRadius;
		this.distEyeToCenterLessRadiusSqSigned = distEyeToCenterLessRadiusSqSigned;
		this.xIndicator = xIndicator;
		this.yIndicator = yIndicator;
	}
	public int compareTo(Object ob){
		assert ob instanceof OccluderDistAndQuad : ob;
		double obDist = ((OccluderDistAndQuad)ob).getDistEyeToCenterLessRadius();
		if (distEyeToCenterLessRadius > obDist){
			return 1;
		}else if (distEyeToCenterLessRadius < obDist){
			return -1;
		}else{
			return 0;
		}
	}

	public double getDistEyeToCenterLessRadius() {
		return distEyeToCenterLessRadius;
	}

	public double getDistEyeToCenterLessRadiusSqSigned() {
		return distEyeToCenterLessRadiusSqSigned;
	}

	public Occluder getOccluder(){
		return occluder;
	}

	public KPolygon getPolygon() {
		return occluder.getPolygon();
	}

	public int getXIndicator() {
		return xIndicator;
	}

	public int getYIndicator() {
		return yIndicator;
	}

}
