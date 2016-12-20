/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

/**
 *
 * @author Keith
 */
public interface LoopAnimation {
	public void setSystemNanosAtStart(long nanos);
	public void update(long nanosElapsed);
	public void render();
}
