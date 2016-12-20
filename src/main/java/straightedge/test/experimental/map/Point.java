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
package straightedge.test.experimental.map;

import com.jme3.math.Vector2f;
/**
 *
 * @author Keith
 */
public class Point {
	Vector2f point;
	Link upLink;
	Link downLink;
	Link leftLink;
	Link rightLink;

	public Point(Vector2f point){
		this.point = point;
	}

	public Vector2f getPoint() {
		return point;
	}

	public void setPoint(Vector2f point) {
		this.point = point;
	}

	public Link getDownLink() {
		return downLink;
	}

	public void setDownLink(Link downLink) {
		this.downLink = downLink;
	}

	public Link getLeftLink() {
		return leftLink;
	}

	public void setLeftLink(Link leftLink) {
		this.leftLink = leftLink;
	}

	public Link getRightLink() {
		return rightLink;
	}

	public void setRightLink(Link rightLink) {
		this.rightLink = rightLink;
	}

	public Link getUpLink() {
		return upLink;
	}

	public void setUpLink(Link upLink) {
		this.upLink = upLink;
	}


}
