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
package straightedge.test.benchmark;

import straightedge.geom.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

/**
 *
 * @author Phillip
 */
public class PillarWorld extends GameWorld{
	protected ArrayList<KPolygon> makePolygons(){
		ArrayList<KPolygon> allPolys = new ArrayList<KPolygon>();

		// circle
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint centerOfSpiral = new KPoint(200, 250);
			int numPoints = 40;//250;
			double angleIncrement = Math.PI*2f/(numPoints);
			float rBig = 100;
			double currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				double x = rBig*Math.cos(currentAngle);
				double y = rBig*Math.sin(currentAngle);
				KPoint center = new KPoint((float)x, (float)y);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)currentAngle);
				poly.translateTo(center);
				poly.translate(centerOfSpiral);
				allPolys.add(poly);
				currentAngle += angleIncrement;
			}
		}

		// circle
		{
			float pillarH = 5;
			float pillarW = 1;
			KPoint centerOfSpiral = new KPoint(400, 250);
			int numPoints = 60;//200;
			double angleIncrement = Math.PI*2f/(numPoints);
			float rBig = 50;
			double currentAngle = 0;
			for (int k = 0; k < numPoints; k++){
				double x = rBig*Math.cos(currentAngle);
				double y = rBig*Math.sin(currentAngle);
				KPoint center = new KPoint((float)x, (float)y);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)currentAngle);
				poly.translateTo(center);
				poly.translate(centerOfSpiral);
				allPolys.add(poly);
				currentAngle += angleIncrement;
			}
		}

		// wall
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint wallStart = new KPoint(50, 50);
			KPoint wallEnd = new KPoint(550, 50);
			float wallGap = 15;
			float currentDist = 0;
			while(currentDist < wallStart.distance(wallEnd)){
				KPoint center = wallStart.createPointToward(wallEnd, currentDist);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)(wallStart.findAngle(wallEnd)+Math.PI/2f));
				poly.translateTo(center);
				allPolys.add(poly);
				currentDist += wallGap;
			}
		}

		// wall
		{
			float pillarH = 10;
			float pillarW = 2;
			KPoint wallStart = new KPoint(50, 80);
			KPoint wallEnd = new KPoint(550, 80);
			float wallGap = 15;
			float currentDist = 0;
			while(currentDist < wallStart.distance(wallEnd)){
				KPoint center = wallStart.createPointToward(wallEnd, currentDist);
				KPolygon poly = KPolygon.createRectOblique(0,0, pillarH,0, pillarW);
				poly.rotate((float)(wallStart.findAngle(wallEnd)+Math.PI/2f));
				poly.translateTo(center);
				allPolys.add(poly);
				currentDist += wallGap;
			}
		}


//		// spiral
//		{
//			float pillarH = 5;
//			float pillarW = 1;
//			KPoint centerOfSpiral = new KPoint(400, 350);
//			int numPoints = 40;
//			int numSpirals = 3;
//			int totalPillars = numPoints*numSpirals;
//			double angleIncrement = Math.PI*2f/(numPoints);
//			float rBig = 100;
//			float rSmall = 20;
//			float currentRadius = rBig;
//			double currentAngle = 0;
//			for (int k = 0; k < totalPillars; k++){
//				currentRadius = rSmall + (rBig - rSmall)*k/totalPillars;
//				double x = currentRadius*Math.cos(currentAngle);
//				double y = currentRadius*Math.sin(currentAngle);
//				KPoint center = new KPoint((float)x, (float)y);
//				KPolygon poly = KPolygon.createRectangle(0,0, pillarH,0, pillarW);
//				poly.rotate((float)currentAngle);
//				poly.translateTo(center);
//				poly.translate(centerOfSpiral);
//				allPolys.add(poly);
//				currentAngle += angleIncrement;
//			}
//		}
		
		return allPolys;
	}
}
