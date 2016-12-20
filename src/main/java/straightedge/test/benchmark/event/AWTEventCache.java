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
package straightedge.test.benchmark.event;

import java.util.*;

/**
 * KeyEvent and MouseEvent's are added to this object's events list from any thread.
 * The method clearAndFillCache empties these events into another list in a thread safe manner,
 * and this list can be browsed to do useful things with those events.
 * 
 * @author CommanderKeith
 */
public class AWTEventCache {
	ArrayList<AWTEventWrapper> events = new ArrayList<AWTEventWrapper>();
	Object mutex = new Object();
	ArrayList<AWTEventWrapper> eventsCopy = new ArrayList<AWTEventWrapper>();
	
	public void addEvent(AWTEventWrapper e){
		synchronized(mutex){
			events.add(e);
		}
	}
	
	public void clearAndFillCache(){
		eventsCopy.clear();
		synchronized(mutex){
			eventsCopy.addAll(events);
			events.clear();
		}
	}
	
	public ArrayList<AWTEventWrapper> getEventsList() {
		return eventsCopy;
	}

}
