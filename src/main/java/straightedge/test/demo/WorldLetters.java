/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import straightedge.geom.AABB;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;
import straightedge.geom.vision.OccluderImpl;

/**
 *
 * @author Keith
 */
public class WorldLetters extends World{
	public WorldLetters(Main main){
		super(main);
	}

	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);

		// Add some words as shapes
		Graphics2D g2d = (Graphics2D)AcceleratedImage.createTransparentBufferedImage(1, 1).getGraphics();
		FontRenderContext frc = g2d.getFontRenderContext();
		Font font = g2d.getFont().deriveFont(130.0f);
//			KPolygon border = KPolygon.createRectOblique(0, 0, frameW, 0, 1);
//			KPolygon border2 = KPolygon.createRectOblique(frameW, 0, frameW, frameH, 1);
//			KPolygon border3 = KPolygon.createRectOblique(frameW, frameH, 0, frameH, 1);
//			KPolygon border4 = KPolygon.createRectOblique(0, frameH, 0, 0, 1);
//			allOccluders.add(new OccluderImpl(border));
//			allOccluders.add(new OccluderImpl(border2));
//			allOccluders.add(new OccluderImpl(border3));
//			allOccluders.add(new OccluderImpl(border4));
//			allMultiPolygons.add(new KMultiPolygon(border));
//			allMultiPolygons.add(new KMultiPolygon(border2));
//			allMultiPolygons.add(new KMultiPolygon(border3));
//			allMultiPolygons.add(new KMultiPolygon(border4));

		{
			String text = "Straight";
			TextLayout tl = new TextLayout(text, font, frc);
			FontMetrics fm = cont.getFontMetrics(font);
			double height = fm.getHeight();
			double textW = fm.stringWidth(text);
			Shape textShape = tl.getOutline(null);
			PolygonConverter pc = new PolygonConverter();
			ArrayList<KPolygon> polygons = pc.makeKPolygonListFrom(textShape, 1);
			ArrayList<KMultiPolygon> multiPolygons = pc.makeKMultiPolygonListFrom(polygons);
			AABB bigAABB = new AABB();
			for (int i = 0; i < polygons.size(); i++){
				bigAABB = bigAABB.union(polygons.get(i).getAABB());
			}
			double translateX = -bigAABB.w()/2f + contW/2f;
			double translateY = -bigAABB.h()/2f + contH/2f;
			for (int i = 0; i < multiPolygons.size(); i++){
				KMultiPolygon polygon = multiPolygons.get(i);
				polygon.translate(translateX, translateY);
			}
			allMultiPolygons.addAll(multiPolygons);
		}
		{
			String text = "Edge";
			TextLayout tl = new TextLayout(text, font, frc);
			FontMetrics fm = cont.getFontMetrics(font);
			double height = fm.getHeight();
			double textW = fm.stringWidth(text);
			Shape textShape = tl.getOutline(null);
			PolygonConverter pc = new PolygonConverter();
			ArrayList<KPolygon> polygons = pc.makeKPolygonListFrom(textShape, 1);
			ArrayList<KMultiPolygon> multiPolygons = pc.makeKMultiPolygonListFrom(polygons);
			AABB bigAABB = new AABB();
			for (int i = 0; i < polygons.size(); i++){
				bigAABB = bigAABB.union(polygons.get(i).getAABB());
			}
			double translateX = -bigAABB.w()/2f + contW/2f;
			double translateY = +bigAABB.h()/2f + bigAABB.h()/2f + contH/2f;
			for (int i = 0; i < multiPolygons.size(); i++){
				KMultiPolygon polygon = multiPolygons.get(i);
				polygon.translate(translateX, translateY);
			}
			allMultiPolygons.addAll(multiPolygons);
		}
	}
}
