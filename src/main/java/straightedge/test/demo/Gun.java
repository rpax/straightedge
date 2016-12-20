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
package straightedge.test.demo;



public class Gun{
	public World world;
	public double angle;
	public double rotationSpeed;
	public boolean firing;
	public double lastTimeFiredSeconds;
	public double triggerPressSeconds;
	public float length;
	int unloadedAmmo;
	int maxClipAmmo;
	int clipAmmo;
	public double reloadSeconds;
	public double reloadClipSeconds;
	public double timeUntilReloaded;
	boolean reloadNeeded;
	public Player player;
	double coneAngle;
	int playerGunNum;
	//AcceleratedImage image = new AcceleratedImage("guns/machineGun.png");

	public Gun(World world) {
		this.world = world;
		respawn();
	}

	public Bullet createBullet(Player player, double xPosWhenFired, double yPosWhenFired, double playerAngle, double lastTimeFiredSeconds, double xLaunchSpeed, double yLaunchSpeed){
		float randomAngleIncrement = (float)((world.random.nextFloat()-0.5)*coneAngle);
		return new Bullet(this, player, xPosWhenFired, yPosWhenFired, angle + randomAngleIncrement, lastTimeFiredSeconds, 0, 0);
	}
	public int getTotalAmmo() {
		return unloadedAmmo + clipAmmo;
	}

	public void respawn() {
		angle = 0;
		firing = false;
		lastTimeFiredSeconds = 0;
		triggerPressSeconds = 0;
		timeUntilReloaded = 0;
		reloadNeeded = false;

		length = 12;
		reloadSeconds = 0.02f;//0.025f;//0.05f;//0.20f;
		reloadClipSeconds = reloadSeconds;
		unloadedAmmo = 1000000000;
		maxClipAmmo = 100;
		clipAmmo = maxClipAmmo;
		rotationSpeed = Math.PI*3;
		coneAngle = 0;//Math.PI/36f;
		playerGunNum = 0;

	}

	public void doMoveAndBulletFire(double seconds, double startTime) {
		assert seconds >= 0 && timeUntilReloaded >= 0 : seconds+", "+timeUntilReloaded;
		assert player != null;
		double endTime = startTime + seconds;

		if (timeUntilReloaded > 0){
			if (timeUntilReloaded > seconds){
				timeUntilReloaded -= seconds;
				doMoveBetweenFires(seconds, startTime);
				seconds = 0;
				startTime += seconds;
				return;
			}else{
				doMoveBetweenFires(timeUntilReloaded, startTime);
				seconds -= timeUntilReloaded;
				startTime += timeUntilReloaded;
				timeUntilReloaded = 0;
			}
		}
		assert seconds >= 0 && timeUntilReloaded >= 0 : seconds+", "+timeUntilReloaded;
		//while (player.isDead() == false && firing && getTotalAmmo() > 0 && timeUntilReloaded == 0){
		while (firing && getTotalAmmo() > 0 && timeUntilReloaded == 0){
			assert getTotalAmmo() >= 0 && timeUntilReloaded == 0: getTotalAmmo()+", "+timeUntilReloaded;
			firing = true;
			if (reloadNeeded == true) {
				assert timeUntilReloaded == 0 : timeUntilReloaded;
				doClipReload();
			}
			assert clipAmmo >= 0 : clipAmmo;
			assert seconds >= 0 : seconds;
			assert startTime <= endTime : startTime + ", " + endTime;

			double xPosWhenFired = player.pos.x;
			double yPosWhenFired = player.pos.y;
			xPosWhenFired += (float) (length * Math.cos(angle));
			yPosWhenFired += (float) (length * Math.sin(angle));
			fire(seconds, startTime, player, xPosWhenFired, yPosWhenFired, angle, player.speedX, player.speedY);
			lastTimeFiredSeconds = startTime;

			clipAmmo--;
			assert clipAmmo >= 0 : clipAmmo;
			if (clipAmmo <= 0) {
				this.reloadClip();//timeAtStartOfMoveSeconds);
			}

			timeUntilReloaded = this.getCurrentReloadSeconds();
			if (timeUntilReloaded > 0){
				if (timeUntilReloaded > seconds){
					timeUntilReloaded -= seconds;
					doMoveBetweenFires(seconds, startTime);
					seconds = 0;
					startTime += seconds;
					return;
				}else{
					doMoveBetweenFires(timeUntilReloaded, startTime);
					seconds -= timeUntilReloaded;
					startTime += timeUntilReloaded;
					timeUntilReloaded = 0;
				}
			}
		}
		if (reloadNeeded == true && timeUntilReloaded == 0){
			doClipReload();
		}
		doMoveBetweenFires(seconds, startTime);
	}
	public void doMoveBetweenFires(double seconds, double startTime){
		this.doRotation(seconds, startTime);
		player.doMoveBetweenGunFires(seconds, startTime);
	}
	public void fire(double secondsLeft, double timeAtStartOfMoveSeconds, Player player, double xPosWhenFired, double yPosWhenFired, double angle, double playerSpeedX, double playerSpeedY) {
		Bullet bullet = createBullet(player, xPosWhenFired, yPosWhenFired, angle, timeAtStartOfMoveSeconds, playerSpeedX, playerSpeedY);
		// Move the bullet to where it should be at the end of this move. Note that each bullet in world.bullets has already had doMove called on it so there won't be any doubling up.
		bullet.doMove(secondsLeft, timeAtStartOfMoveSeconds);
		world.bullets.add(bullet);
	}

	public void doRotation(double seconds, double timeAtStartOfMoveSeconds) {
		// The below is commented out since without vehicles and just using soldiers, there's no use having the gun turn independantly of the soldier.
		double oldGunAngleRelativeToPlayer = angle;
		double targetGunAngle = player.mouseAngle;//(float)KPoint.findAngle(0, 0, targetPoint.x, targetPoint.y);
		//float angleToTurn = (float) (targetGunAngle - oldGunAngleRelativeToPlayer - player.getAngle());
		double angleToTurn = (float) (targetGunAngle - oldGunAngleRelativeToPlayer);
		// Here we make sure angleToTurn is between -Math.PI and +Math.PI so
		// that it's easy to know which way the gun should turn.
		// The maximum that angleToTurn could be now is + or - 2 * 2*Math.PI.
		if (angleToTurn > Math.PI) {
			angleToTurn -= (float) (2 * Math.PI);
		}
		if (angleToTurn < -Math.PI) {
			angleToTurn += (float) (2 * Math.PI);
		}
		if (angleToTurn > Math.PI){
			// due to floating point error the angle is still too big, so set it to Math.PI exactly.
			angleToTurn = (float)(Math.PI);
		}
		assert angleToTurn >= (float)(-Math.PI) && angleToTurn <= (float)(Math.PI) : angleToTurn;
		float maxGunAngleChange = (float) (rotationSpeed * seconds);
		if (angleToTurn > 0) {
			if (angleToTurn > maxGunAngleChange) {
				angle = oldGunAngleRelativeToPlayer + maxGunAngleChange;
			} else {
				angle = oldGunAngleRelativeToPlayer + angleToTurn;
			}
		} else {
			if (angleToTurn < -maxGunAngleChange) {
				angle = oldGunAngleRelativeToPlayer - maxGunAngleChange;
			} else {
				angle = oldGunAngleRelativeToPlayer + angleToTurn;
			}
		}
		while (angle >= 2 * Math.PI) {
			angle -= (2 * Math.PI);
		}
		while (angle < 0) {
			angle += (2 * Math.PI);
		}
		assert targetGunAngle >= 0 : targetGunAngle;
		assert angle >= 0 : angle;
	}

	public void startFiring(double triggerPressSeconds) {
		this.triggerPressSeconds = triggerPressSeconds;
		firing = true;
	}

	public void stopFiring() {
		firing = false;
	}

	public double getCurrentReloadSeconds() {
		if (reloadNeeded) {
			int ammoToPutIn = maxClipAmmo - clipAmmo;
			if (ammoToPutIn > unloadedAmmo){
				ammoToPutIn = unloadedAmmo;
			}
			double proportionOfClipToReload = (ammoToPutIn/maxClipAmmo);
			assert proportionOfClipToReload > 0 && proportionOfClipToReload <= 1: proportionOfClipToReload+", note that proportionOfClipToReload should not be zero.";
			return getReloadClipSeconds()*proportionOfClipToReload;
		} else {
			return getReloadSeconds();
		}
	}
	public void addAmmo(int extraAmmo) {
		assert clipAmmo >= 0 : clipAmmo;
		if (getTotalAmmo() <= 0) {
			unloadedAmmo += extraAmmo;
			doClipReload();
		}else{
			unloadedAmmo += extraAmmo;
		}
	}

	public void reloadClip(){
		if (unloadedAmmo == 0){
			reloadNeeded = false;
			return;
		}
		if (clipAmmo == maxClipAmmo || reloadNeeded == true) {
			return;
		}
		assert unloadedAmmo >= 0 : unloadedAmmo;
		int ammoToPutIn = maxClipAmmo - clipAmmo;
		if (ammoToPutIn > unloadedAmmo){
			ammoToPutIn = unloadedAmmo;
		}
		float proportionOfClipToReload = ((float)ammoToPutIn/maxClipAmmo);
		assert proportionOfClipToReload > 0 && proportionOfClipToReload <= 1: proportionOfClipToReload+", note that proportionOfClipToReload should not be zero.";
		//System.out.println("proportionOfClipToReload == "+proportionOfClipToReload);
		this.timeUntilReloaded = getReloadClipSeconds()*proportionOfClipToReload;
		reloadNeeded = true;
		// then doClipReload is done in method doMoveAndBulletFire
	}
	protected void doClipReload(){
		if (clipAmmo == maxClipAmmo) {
			return;
		} else if (unloadedAmmo == 0) {
			return;
		} else {
			int ammoToPutIn = maxClipAmmo - clipAmmo;
			if (ammoToPutIn > unloadedAmmo) {
				ammoToPutIn = unloadedAmmo;
			}
			assert ammoToPutIn > 0 : ammoToPutIn;
			unloadedAmmo -= ammoToPutIn;
			clipAmmo += ammoToPutIn;
			assert unloadedAmmo >= 0 : unloadedAmmo;
			reloadNeeded = false;
			timeUntilReloaded = 0;
		}
	}

	public double getReloadSeconds() {
		if (player != null){
			return reloadSeconds;
		}
		return reloadSeconds;
	}

	public double getReloadClipSeconds() {
		if (player != null){
			return reloadClipSeconds;
		}
		return reloadClipSeconds;
	}



	public double getProportionOfReloadTimeElapsed(){
		//assert getLastTimeFiredSeconds() <= world.getSecondsElapsed() : world.getSecondsElapsed() + ", " + getLastTimeFiredSeconds();
		//double timeSinceLastTimeFired = world.getTotalElapsedSeconds() - getLastTimeFiredSeconds();
		float proportionOfReloadTimeElapsed;
		if (reloadNeeded == true || getTotalAmmo() == 0){// || player.isDead() == true) {
			proportionOfReloadTimeElapsed = 0;
		} else {
			assert reloadNeeded == false : reloadNeeded;
			proportionOfReloadTimeElapsed = (float)(1f - timeUntilReloaded / getReloadSeconds());
			proportionOfReloadTimeElapsed = Math.min(proportionOfReloadTimeElapsed, 1);
		}
		return proportionOfReloadTimeElapsed;
	}

	public double getProportionOfAmmoInClip(){
		float proportionOfAmmoInClip;
		if (reloadNeeded == false){
			proportionOfAmmoInClip = (float)clipAmmo/maxClipAmmo;
		}else{
			float proportionOfClipLeftToLoad = (float)(timeUntilReloaded / this.getReloadClipSeconds());
			int ammoToPutIn = maxClipAmmo - clipAmmo;
			if (ammoToPutIn > unloadedAmmo){
				ammoToPutIn = unloadedAmmo;
			}
			float clipFullnessAfterReload = ((float)ammoToPutIn + clipAmmo)/(float)maxClipAmmo;
			proportionOfAmmoInClip = clipFullnessAfterReload - proportionOfClipLeftToLoad;
			assert proportionOfAmmoInClip >= 0 && proportionOfAmmoInClip <= 1: proportionOfAmmoInClip+", "+clipFullnessAfterReload+", "+proportionOfClipLeftToLoad;
		}
		assert proportionOfAmmoInClip <= 1 && proportionOfAmmoInClip >= 0 : proportionOfAmmoInClip;
		return proportionOfAmmoInClip;
	}


}