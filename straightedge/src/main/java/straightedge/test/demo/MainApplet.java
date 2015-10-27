/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JApplet;

/**
 *
 * @author Keith
 */
public class MainApplet extends Main{
	AppletImpl applet;
	public MainApplet(AppletImpl applet){
		this.applet = applet;
	}

	public void init(){
		System.out.println(this.getClass().getSimpleName()+": init");
	}
	public void start(){
		Thread t = new Thread(){
			public void run(){
				System.out.println(this.getClass().getSimpleName()+": start 0");
				System.out.println(this.getClass().getSimpleName()+": start 1");
				Main main = MainApplet.this;
				viewPane = new ViewPane(main);
				System.out.println(this.getClass().getSimpleName()+": start .1");
				applet.add(viewPane);
				System.out.println(this.getClass().getSimpleName()+": start .2");
				eventHandler = new EventHandler(main);
				eventHandler.init();
				System.out.println(this.getClass().getSimpleName()+": start .3.");
				view = new View(main);
				System.out.println(this.getClass().getSimpleName()+": start .4");
				applet.validate();
				System.out.println(this.getClass().getSimpleName()+": start 2");


				System.out.println(this.getClass().getSimpleName()+": start 3");
				loop = new Loop(main);
				loop.setDaemon(false);
				animationLoading = new LoadingLoopAnimation(main);
				animationLoading.show();
				loop.start();
				System.out.println(this.getClass().getSimpleName()+": start 4");



				System.out.println(this.getClass().getSimpleName()+": start 5");
				main.world = new WorldLetters(main);
				main.world.init();	// takes ages
				main.eventHandler.eventCache.clearAndFillCache();	// clear out any old events
				WorldLoopAnimation newAnimationWorld = new WorldLoopAnimation(main.world, main.view);
				main.loop.setAnimationAndRestart(newAnimationWorld);
				System.out.println(this.getClass().getSimpleName()+": start 6");
				viewPane.requestFocus();
				System.out.println(this.getClass().getSimpleName()+": start 7");
			}
		};
		t.start();

		



//		System.out.println(this.getClass().getSimpleName()+": start 0");
////		try{
////			javax.swing.SwingUtilities.invokeAndWait(new Runnable(){
////				public void run(){
//					System.out.println(this.getClass().getSimpleName()+": start 1");
//					Main main = MainApplet.this;
//					viewPane = new ViewPane(main);
//					System.out.println(this.getClass().getSimpleName()+": start .1");
//					applet.add(viewPane);
//					System.out.println(this.getClass().getSimpleName()+": start .2");
//					eventHandler = new EventHandler(main);
//					eventHandler.init();
//					System.out.println(this.getClass().getSimpleName()+": start .3");
//					view = new View(main);
//					System.out.println(this.getClass().getSimpleName()+": start .4");
//					applet.validate();
//					System.out.println(this.getClass().getSimpleName()+": start 2");
////				}
////			});
////		}catch(InterruptedException e){
////			e.printStackTrace();
////		}catch(InvocationTargetException e){
////			e.printStackTrace();
////		}
//
//		System.out.println(this.getClass().getSimpleName()+": start 3");
//		loop = new Loop(this);
//		loop.setDaemon(false);
//		animationLoading = new LoadingLoopAnimation(this);
//		animationLoading.show();
//		loop.start();
//		System.out.println(this.getClass().getSimpleName()+": start 4");
//
//
//		Thread t = new Thread(){
//			public void run(){
//				System.out.println(this.getClass().getSimpleName()+": start 5");
//				Main main = MainApplet.this;
//				main.world = new WorldMaze(main);
//				main.world.init();	// takes ages
//				main.eventHandler.eventCache.clearAndFillCache();	// clear out any old events
//				WorldLoopAnimation newAnimationWorld = new WorldLoopAnimation(main.world, main.view);
//				main.loop.setAnimationAndRestart(newAnimationWorld);
//				System.out.println(this.getClass().getSimpleName()+": start 6");
//			}
//		};
//		t.start();
//
//		viewPane.requestFocus();
//		System.out.println(this.getClass().getSimpleName()+": start 7");
	}
	public void stop(){
		System.out.println(this.getClass().getSimpleName()+": stop");
		loop.close();
	}
	public void destroy(){
		System.out.println(this.getClass().getSimpleName()+": destroy");
		loop.close();
	}

	public Container getParentFrameOrApplet(){
		return getApplet();
	}

	public void close(){
		if (loop != null){
			loop.close();
		}
	}

	public JApplet getApplet() {
		return applet;
	}
}
