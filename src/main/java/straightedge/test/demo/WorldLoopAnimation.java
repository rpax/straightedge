/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

/**
 *
 * @author Keith
 */
public class WorldLoopAnimation implements LoopAnimation{
	World world; 
	View view;
	public WorldLoopAnimation(World world, View view){
		this.world = world;
		this.view = view;
	}
	public void setSystemNanosAtStart(long nanos){
		world.setSystemNanosAtStart(nanos);
	}
	public void update(long nanosElapsed){
		world.update(nanosElapsed);
	}
	public void render(){
		view.render();
	}
}
