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

import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import java.util.ArrayList;

/**
 *
 * @author Keith
 */
public class VisionDataRotation extends VisionData{
	public KPoint originalEye;
	public KPolygon originalBoundaryPolygon;
	public double boundaryPolygonRotationAroundEye = 0;
	
	public VisionDataRotation(KPoint originalEye, KPolygon originalBoundaryPolygon){
		super();
		reset(originalEye, originalBoundaryPolygon);
	}
	
	public void reset(KPoint originalEye, KPolygon originalBoundaryPolygon){
		this.originalEye = originalEye;
		this.originalBoundaryPolygon = originalBoundaryPolygon;
		this.boundaryPolygonRotationAroundEye = 0;

		super.reset(originalEye.copy(), originalBoundaryPolygon.copy());
	}

	public void reset(){
		reset(originalEye, originalBoundaryPolygon);
	}

	public void copyAndTransformEyeAndBoundaryPolygon(KPoint newEyeCoords, double boundaryPolygonRotationAroundEye){
		copyAndTransformEyeAndBoundaryPolygon(newEyeCoords.x, newEyeCoords.y, boundaryPolygonRotationAroundEye);
	}
	
	public void copyAndTransformEyeAndBoundaryPolygon(double newEyeX, double newEyeY, double boundaryPolygonRotationAroundEye){
		this.boundaryPolygonRotationAroundEye = boundaryPolygonRotationAroundEye;
		eye = originalEye.copy();
		boundaryPolygon = originalBoundaryPolygon.copy();
		double translateX = newEyeX - originalEye.x;
		double translateY = newEyeY - originalEye.y;
		eye.setCoords(newEyeX, newEyeY);
		boundaryPolygon.translate(translateX, translateY);
		boundaryPolygon.rotate(boundaryPolygonRotationAroundEye, eye);
	}

	public double getBoundaryPolygonRotationAroundEye() {
		return boundaryPolygonRotationAroundEye;
	}

	public KPolygon getOriginalBoundaryPolygon() {
		return originalBoundaryPolygon;
	}

	public KPoint getOriginalEye() {
		return originalEye;
	}
	
	public KPolygon getBoundaryPolygon() {
		return boundaryPolygon;
	}

	public KPoint getEye() {
		return eye;
	}

	public ArrayList<VisiblePoint> getVisiblePoints() {
		return visiblePoints;
	}

	public KPolygon getVisiblePolygon() {
		return visiblePolygon;
	}

}
