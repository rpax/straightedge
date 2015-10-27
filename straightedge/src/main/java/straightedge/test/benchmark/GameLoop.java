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
 * Main thread, controls the world and the view.
 * @author Keith Woodward
 */
public class GameLoop extends Thread{
	
	GameFrame frame;
	ViewPane view;
	GameWorld world;
	Player player;
	volatile boolean keepRunning = true;
	FPSCounter fpsCounter;
	static double NANOS_IN_A_SECOND = 1000000000;
	
	// the minimum time to sleep after each update. if negative, no sleep.
	protected volatile int minSleepMillisBetweenUpdates = 0;
	long oldSystemTimeNanos = -1;
	
	public GameLoop(GameFrame frame, GameWorld world, Player player, ViewPane view){
		this.setName("GameLoop Thread");
		setFrame(frame);
		setWorld(world);
		setPlayer(player);
		setView(view);
		fpsCounter = new FPSCounter();
	}

	public void run() {
		System.out.println(this.getClass().getSimpleName() + ": game loop started");
		long nanoTimeNow = System.nanoTime();
		oldSystemTimeNanos = nanoTimeNow;
		world.setSystemNanosAtStart(nanoTimeNow);
		keepRunning = true;
		while (keepRunning) {
			fpsCounter.update();
			long currentSystemTimeNanos = System.nanoTime();
			long timeElapsedNanos = currentSystemTimeNanos - oldSystemTimeNanos;

			// Update the world using the time elapsed
			world.update(timeElapsedNanos);
			this.oldSystemTimeNanos = currentSystemTimeNanos;
			view.render();
			//doMinSleep();	// gives the other threads a turn
		}
		System.out.println(this.getClass().getSimpleName() + ": game loop finished");
	}
	
	public void doMinSleep(){
		if (minSleepMillisBetweenUpdates >= 0){
			try{
				Thread.sleep(minSleepMillisBetweenUpdates);
				//System.out.println(this.getClass().getSimpleName()+": sleeping for "+minSleepMillisBetweenUpdates);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		Thread.yield();
	}
	
	public void close(){
		keepRunning = false;
	}

	public FPSCounter getFpsCounter() {
		return fpsCounter;
	}

	public GameFrame getFrame() {
		return frame;
	}

	public int getMinSleepMillisBetweenUpdates() {
		return minSleepMillisBetweenUpdates;
	}

	public ViewPane getView() {
		return view;
	}

	public GameWorld getWorld() {
		return world;
	}

	public Player getPlayer() {
		return player;
	}

	public void setFrame(GameFrame frame) {
		if (this.frame != frame){
			this.frame = frame;
			frame.setLoop(this);
		}
	}

	public void setPlayer(Player player) {
		if (this.player != player){
			this.player = player;
			player.setLoop(this);
		}
	}

	public void setView(ViewPane view) {
		if (this.view != view){
			this.view = view;
			view.setLoop(this);
		}
	}

	public void setWorld(GameWorld world) {
		if (this.world != world){
			this.world = world;
			world.setLoop(this);
		}
	}
}
