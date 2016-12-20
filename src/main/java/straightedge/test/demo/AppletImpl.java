/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Graphics;
import javax.swing.JApplet;

/**
 *
 * @author Keith
 */
public class AppletImpl extends JApplet{
	MainApplet mainApplet;
	public AppletImpl(){
		this.mainApplet = new MainApplet(this);
	}
	public void init(){
		mainApplet.init();
	}
	public void start(){
		mainApplet.start();
	}
	public void stop(){
		mainApplet.stop();
	}
	public void destroy(){
		mainApplet.destroy();
	}
}
