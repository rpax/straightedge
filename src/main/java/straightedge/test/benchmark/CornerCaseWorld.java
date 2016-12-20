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

/**
 *
 * @author Phillip
 */
public class CornerCaseWorld extends GameWorld{
	public CornerCaseWorld(){

	}
	protected ArrayList<KPolygon> makePolygons(){
		ArrayList<KPolygon> allPolys = new ArrayList<KPolygon>();
		int width = 20;
		KPolygon poly = KPolygon.createRectOblique(new KPoint(50, 200), new KPoint(200, 200), width);
		KPolygon poly2 = KPolygon.createRectOblique(new KPoint(200 - width/2, 200 + width/2), new KPoint(200 - width/2, 50 + width/2), width);

		KPolygon poly3 = KPolygon.createRectOblique(new KPoint(300, 200), new KPoint(400, 200), width);
		KPolygon poly4 = KPolygon.createRectOblique(new KPoint(350, 200), new KPoint(450, 200), width);
		KPolygon poly5 = KPolygon.createRectOblique(new KPoint(375, 100), new KPoint(375, 300), width);
		
		allPolys.add(poly);
		allPolys.add(poly2);
		allPolys.add(poly3);
		allPolys.add(poly4);
		allPolys.add(poly5);

		KPolygon circle = KPolygon.createRegularPolygon(1020, 50);
		circle.translateTo(200, 400);
		allPolys.add(circle);

		return allPolys;
	}
}
