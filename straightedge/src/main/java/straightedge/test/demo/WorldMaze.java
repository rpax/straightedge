/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package straightedge.test.demo;

import java.awt.Container;
import java.util.ArrayList;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPolygon;
import straightedge.geom.vision.CollinearOverlapChecker;

/**
 *
 * @author Keith
 */
public class WorldMaze extends World{
	int cellWidth;
	int cellHeight;
	int numXAxisCells;
	int numYAxisCells;
	float wallWidth;

	public WorldMaze(Main main){
		super(main);
	}

//	public WorldMaze(Main main, int cellWidth, int numXAxisCells, int numYAxisCells, float wallWidth, long randomSeed){
//		super(main);
//		random.setSeed(randomSeed);
//		this.cellWidth = cellWidth;
//		this.cellHeight = this.cellWidth;
//		this.numXAxisCells = numXAxisCells;
//		this.numYAxisCells = numYAxisCells;
//		this.wallWidth = wallWidth;
////		cellWidth = 13;//30;
////		cellHeight = cellWidth;
////		wallWidth = 1;//1.5f;
////		numXAxisCells = 38;//17;
////		numYAxisCells = 26;//12;
//	}

	public void fillMultiPolygonsList(){
		Container cont = main.getParentFrameOrApplet();
		double contW = cont.getWidth() - (cont.getInsets().right + cont.getInsets().left);
		double contH = cont.getHeight() - (cont.getInsets().top + cont.getInsets().bottom);

		wallWidth = 10;
		cellWidth = 60;
		cellHeight = cellWidth;
		double minInset = 40;
		numXAxisCells = (int)Math.floor((contW - 2*minInset)/(cellWidth));
		numYAxisCells = (int)Math.floor((contH - 2*minInset)/(cellWidth));
		double insetXAxis = (contW - (numXAxisCells*cellWidth))/2f;
		double insetYAxis = (contH - (numYAxisCells*cellWidth))/2f;

		MazeCell[][] cells;
		ArrayList<KPolygon> wallPolygons = new ArrayList<KPolygon>();

		cells = new MazeCell[numXAxisCells][numYAxisCells];
		for (int i = 0; i < numXAxisCells; i++){
			for (int j = 0; j < numYAxisCells; j++){
				MazeCell cell = new MazeCell(i, j);
				cells[i][j] = cell;
			}
		}
		for (int i = 0; i < numXAxisCells; i++){
			int iPlus = i+1;
			for (int j = 0; j < numYAxisCells; j++){
				int jPlus = j+1;
				MazeCell currentCell = cells[i][j];
				if (iPlus < cells.length){
					MazeCell rightCell = cells[iPlus][j];
					KPolygon verticalWallPolygon = KPolygon.createRectOblique(iPlus*cellWidth, j*cellHeight - wallWidth/2f, iPlus*cellWidth, jPlus*cellHeight + wallWidth/2f, wallWidth);
					MazeWall verticalWall = new MazeWall(currentCell, rightCell, verticalWallPolygon);
					currentCell.setRightWall(verticalWall);
					rightCell.setLeftWall(verticalWall);
				}
				if (jPlus < cells[i].length){
					MazeCell bottomCell = cells[i][jPlus];
					KPolygon horizontalWallPolygon = KPolygon.createRectOblique(i*cellWidth - wallWidth/2f, jPlus*cellHeight, iPlus*cellWidth + wallWidth/2f, jPlus*cellHeight, wallWidth);
					MazeWall horizontalWall = new MazeWall(currentCell, bottomCell, horizontalWallPolygon);
					currentCell.setBotWall(horizontalWall);
					bottomCell.setTopWall(horizontalWall);
				}
			}
		}

		// Use randomised Prim's algorithm to make the maze
		// (see Wikipedia entry 'maze generation algorithm'):
		ArrayList<MazeWall> walls = new ArrayList<MazeWall>();
		MazeCell currentCell = cells[0][0];
		OuterLoop:
		while (true){
			currentCell.setProcessed(true);
			ArrayList<MazeWall> wallsOfCurrentCell = currentCell.getWalls();
			for (MazeWall w : wallsOfCurrentCell){
				if (walls.contains(w) == false){
					walls.add(w);
				}
			}
			MazeWall currentWall;
			while (true){
				if (walls.size() == 0){
					break OuterLoop;
				}
				currentWall = walls.get(random.nextInt(walls.size()));
				if (currentWall.getCell().isProcessed() == false){
					currentWall.getCell().removeWall(currentWall);
					currentWall.getCell2().removeWall(currentWall);
					walls.remove(currentWall);
					currentCell = currentWall.getCell();
					continue OuterLoop;
				}else if (currentWall.getCell2().isProcessed() == false){
					currentWall.getCell().removeWall(currentWall);
					currentWall.getCell2().removeWall(currentWall);
					walls.remove(currentWall);
					currentCell = currentWall.getCell2();
					continue OuterLoop;
				}else{
					walls.remove(currentWall);
				}
			}
		}

		for (int i = 0; i < cells.length; i++){
			for (int j = 0; j < cells[i].length; j++){
				MazeWall wall = cells[i][j].getTopWall();
				if (wall != null && wallPolygons.contains(wall.getPolygon()) == false){
					wallPolygons.add(wall.getPolygon());
				}
				wall = cells[i][j].getBotWall();
				if (wall != null && wallPolygons.contains(wall.getPolygon()) == false){
					wallPolygons.add(wall.getPolygon());
				}
				wall = cells[i][j].getLeftWall();
				if (wall != null && wallPolygons.contains(wall.getPolygon()) == false){
					wallPolygons.add(wall.getPolygon());
				}
				wall = cells[i][j].getRightWall();
				if (wall != null && wallPolygons.contains(wall.getPolygon()) == false){
					wallPolygons.add(wall.getPolygon());
				}
			}
		}
//		// add 4 border walls:
		wallPolygons.add(KPolygon.createRectOblique(0, 0, numXAxisCells*cellWidth, 0, wallWidth));
		wallPolygons.add(KPolygon.createRectOblique(numXAxisCells*cellWidth, 0, numXAxisCells*cellWidth, numYAxisCells*cellHeight, wallWidth));
		wallPolygons.add(KPolygon.createRectOblique(numXAxisCells*cellWidth, numYAxisCells*cellHeight, 0, numYAxisCells*cellHeight, wallWidth));
		wallPolygons.add(KPolygon.createRectOblique(0, (numYAxisCells - 1)*cellHeight, 0, 0, wallWidth));

		for (KPolygon polygon : wallPolygons){
			polygon.translate(insetXAxis, insetYAxis);
		}
		CollinearOverlapChecker coc = new CollinearOverlapChecker();
		coc.fixCollinearOverlaps(wallPolygons);

		for (int i = 0; i < wallPolygons.size(); i++){
			KMultiPolygon multiPolygon = new KMultiPolygon(wallPolygons.get(i).getPolygon().copy());
			allMultiPolygons.add(multiPolygon);
		}
	}


	public static class MazeCell{
		int x, y;
		MazeWall topWall;
		MazeWall botWall;
		MazeWall leftWall;
		MazeWall rightWall;
		boolean processed;
		public MazeCell(int x, int y){
			this.x = x;
			this.y = y;
			processed = false;
		}
		public MazeWall getBotWall() {
			return botWall;
		}

		public void setBotWall(MazeWall botWall) {
			this.botWall = botWall;
		}

		public MazeWall getLeftWall() {
			return leftWall;
		}

		public void setLeftWall(MazeWall leftWall) {
			this.leftWall = leftWall;
		}

		public MazeWall getRightWall() {
			return rightWall;
		}

		public void setRightWall(MazeWall rightWall) {
			this.rightWall = rightWall;
		}

		public MazeWall getTopWall() {
			return topWall;
		}

		public void setTopWall(MazeWall topWall) {
			this.topWall = topWall;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public boolean isProcessed() {
			return processed;
		}

		public void setProcessed(boolean processed) {
			this.processed = processed;
		}

		public ArrayList<MazeWall> getWalls(){
			ArrayList<MazeWall> walls = new ArrayList<MazeWall>();
			if (topWall != null){
				walls.add(topWall);
			}
			if (botWall != null){
				walls.add(botWall);
			}
			if (leftWall != null){
				walls.add(leftWall);
			}
			if (rightWall != null){
				walls.add(rightWall);
			}
			return walls;
		}

		public void removeWall(MazeWall wall){
			if (topWall == wall){
				topWall = null;
			}
			if (botWall == wall){
				botWall = null;
			}
			if (leftWall == wall){
				leftWall = null;
			}
			if (rightWall == wall){
				rightWall = null;
			}
		}

	}

	public static class MazeWall {
		MazeCell cell;
		MazeCell cell2;
		KPolygon polygon;
		public MazeWall(MazeCell cell, MazeCell cell2, KPolygon polygon){
			this.cell = cell;
			this.cell2 = cell2;
			this.polygon = polygon;
		}

		public MazeCell getCell() {
			return cell;
		}

		public void setCell(MazeCell cell) {
			this.cell = cell;
		}

		public MazeCell getCell2() {
			return cell2;
		}

		public void setCell2(MazeCell cell2) {
			this.cell2 = cell2;
		}

		public KPolygon getPolygon() {
			return polygon;
		}


	}
}
