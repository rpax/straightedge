/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author Keith
 */
public class LoadingLoopAnimation implements LoopAnimation{
	Main main;

	public LoadingLoopAnimation(Main main){
		this.main = main;
	}

	public void show(){
		main.loop.setAnimationAndRestart(this, 10);
	}
	
	public void setSystemNanosAtStart(long nanos){
		
	}
	double totalSeconds = 0;
	public void update(long nanosElapsed){
		double seconds = nanosElapsed/World.NANOS_IN_A_SECOND;
		totalSeconds += seconds;
	}

	public void render(){
		ViewPane v = main.viewPane;
		if (v.getBackImage() == null){
			return;
		}
		Graphics2D g = (Graphics2D)v.getBackImage().getGraphics();
		int w = v.getWidth();
		int h = v.getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		String str = "Loading";
		Font oldFont = g.getFont();
		Font font = g.getFont().deriveFont(40f);
		g.setFont(font);
		FontMetrics fm = v.getFontMetrics(font);
		double strH = fm.getHeight();
		double strW = fm.stringWidth(str);
		double boxW = strW + 75;
		double boxH = strH + 25;
		double arc = 10;
		RoundRectangle2D.Double roundRect = new RoundRectangle2D.Double(w/2f - boxW/2, h/2f - boxH/2, boxW, boxH, arc, arc);
		g.setColor(Color.DARK_GRAY.darker());
		g.fill(roundRect);
		double cycleLegthSeconds = 0.1;
		float n = (float)(Math.cos(totalSeconds/(Math.PI*cycleLegthSeconds)) + 1)/2f;
		n /= 2f;
		g.setColor(new Color(n,n,n));
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(4));
		g.draw(roundRect);
		g.setColor(Color.WHITE);
		float strX = (float)(w/2f - strW/2);
		float strY = (float)(h/2f + (strH/2)/2);
		g.drawString(str, strX, strY);
		g.setStroke(oldStroke);
		g.setFont(oldFont);
		v.displayBackImage();
	}
	
}
