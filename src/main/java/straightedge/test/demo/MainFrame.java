/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.awt.Graphics;
import javax.swing.JFrame;

/**
 *
 * @author Keith
 */
public class MainFrame extends Main{
	JFrame frame;
	public MainFrame(){
		// Use VolatileImages in the webstart version. For some reason they don't work in applets.
		AcceleratedImage.useVolatileImage = true;
		
		frame = new JFrame(this.getClass().getSimpleName());
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		viewPane = new ViewPane(this);
		frame.add(viewPane);
		eventHandler = new EventHandler(this);
		eventHandler.init();
		frame.validate();
		
		loop = new Loop(this);
		loop.setDaemon(false);
		frame.setVisible(true);
		animationLoading = new LoadingLoopAnimation(this);
		animationLoading.show();
		loop.start();
		view = new View(this);
		Thread t = new Thread(){
			public void run(){
				Main main = MainFrame.this;
				//main.world = new WorldFractalHexagonalGosperCurve(main);
				//main.world = new WorldKochIsland(main);
				//main.world = new WorldKochSnowflake(main);
				//main.world = new WorldVogelSpiral(main);
				main.world = new WorldMaze(main);
				main.world.init();	// takes ages
				main.eventHandler.eventCache.clearAndFillCache();	// clear out any old events
				WorldLoopAnimation newAnimationWorld = new WorldLoopAnimation(main.world, main.view);
				main.loop.setAnimationAndRestart(newAnimationWorld);
			}
		};
		t.start();

		viewPane.requestFocus();
	}

	public Container getParentFrameOrApplet(){
		return getFrame();
	}

	public void close(){
		if (loop != null){
			loop.close();
		}
		frame.dispose();
		System.exit(0);
	}

	public static void main(String[] args){
		new MainFrame();
	}
	public JFrame getFrame() {
		return frame;
	}
}
