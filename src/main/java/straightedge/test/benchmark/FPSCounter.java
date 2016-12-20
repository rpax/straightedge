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

/**
 * Convenience class that lets you know about performance.
 *
 * @author Keith Woodward
 */
public class FPSCounter{
	// the following can be used for calculating frames per second:
	protected long lastUpdateNanos = -1;
	protected long cumulativeTimeBetweenUpdatesNanos = 0;
	protected float avTimeBetweenUpdatesMillis = -1f;
	protected int counter = 0;
	protected long timeBetweenUpdatesNanos = 500000000; // 1/2 second == 500000000 nanoseconds
	
	protected long freeMemory = Runtime.getRuntime().freeMemory();
	protected long totalMemory = Runtime.getRuntime().totalMemory();
	protected long usedMemory = totalMemory - freeMemory;
	
	public FPSCounter() {
	}
	public void update(){
		if (lastUpdateNanos == -1){
			lastUpdateNanos = System.nanoTime();
		}
		long newUpdateNanos = System.nanoTime();
		cumulativeTimeBetweenUpdatesNanos += newUpdateNanos - lastUpdateNanos;//controller.getWorld().getPureElapsedNanos();
		lastUpdateNanos = newUpdateNanos;
		counter++;
		if (cumulativeTimeBetweenUpdatesNanos >= timeBetweenUpdatesNanos){
			avTimeBetweenUpdatesMillis = (float)((cumulativeTimeBetweenUpdatesNanos)/(counter*1000000f));
			freeMemory = Runtime.getRuntime().freeMemory();
			totalMemory = Runtime.getRuntime().totalMemory();
			usedMemory = totalMemory - freeMemory;
			cumulativeTimeBetweenUpdatesNanos = 0;
			counter = 0;
//			System.out.println(this.getClass().getSimpleName()+": getFPS() == "+getFPS());
		}
	}
	
	public float getAvTimeBetweenUpdatesMillis(){
		return avTimeBetweenUpdatesMillis;
	}
	public int getAvTimeBetweenUpdatesMillisRounded(){
		return Math.round(getAvTimeBetweenUpdatesMillis());
	}
	public float getFPS(){
		return (float)(getAvTimeBetweenUpdatesMillis() != 0 ? 1000f/getAvTimeBetweenUpdatesMillis() : -1);
	}
	public int getFPSRounded(){
		return Math.round(this.getFPS());
	}
	public int getCounter(){
		return counter;
	}
	public long getTimeBetweenUpdatesNanos(){
		return timeBetweenUpdatesNanos;
	}
	public void setTimeBetweenUpdatesNanos(long timeBetweenUpdatesNanos){
		this.timeBetweenUpdatesNanos = timeBetweenUpdatesNanos;
	}
	public long getFreeMemory() {
		return freeMemory;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public long getUsedMemory() {
		return usedMemory;
	}
}
