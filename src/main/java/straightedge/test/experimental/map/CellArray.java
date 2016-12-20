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
package straightedge.test.experimental.map;

import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import java.util.ArrayList;

/**
 *
 * @author Keith
 */
public class CellArray {

	// cells are in row, column order, cells[row][col]
	// the bottom left cell has index cells[0][0].
	// the top right cell has index cells[numRows-1][numCols-1].
	Cell[][] cells = null;
	float w = -1;
	float h = -1;
	int numRows;
	int numCols;
	float cellWidthAndHeight = 5;



	public CellArray(float width, float height){
		w = width;
		h = height;
		numRows = (int)(h/cellWidthAndHeight);//(int)Math.ceil(h/cellWidthAndHeight);
		numCols = (int)(w/cellWidthAndHeight);//(int)Math.ceil(w/cellWidthAndHeight);
		cells = new Cell[numRows][numCols];
		// make cells
		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = new Cell(this, row, col);
				cells[row][col] = cell;
			}
		}
		// add botLeft points to all cells, which will be neighbouring cells' botRight, topRight and topLeft Points.
		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = cells[row][col];
				float x = col*cellWidthAndHeight;
				float y = row*cellWidthAndHeight;
				Point botLeft = new Point(new KPoint(x, y));
				cell.setBotLeft(botLeft);
				if (cell.getCellLeft() != null){
					cell.getCellLeft().setBotRight(botLeft);
				}
				if (cell.getCellDownLeft() != null){
					cell.getCellDownLeft().setTopRight(botLeft);
				}
				if (cell.getCellDown() != null){
					cell.getCellDown().setTopLeft(botLeft);
				}
			}
		}
		// add botRight points to the right col
		for (int row = 0; row < numRows; row++){
			int col = numCols-1;
			Cell cell = cells[row][col];
			float x = col*cellWidthAndHeight;
			float y = row*cellWidthAndHeight;
			Point botRight = new Point(new KPoint(x + cellWidthAndHeight, y));
			cell.setBotRight(botRight);
			if (row != 0){
				cell.getCellDown().setTopRight(botRight);
			}
		}

		// add topLeft points to the top row
		for (int col = 0; col < numCols; col++){
			int row = numRows-1;
			Cell cell = cells[row][col];
			float x = col*cellWidthAndHeight;
			float y = row*cellWidthAndHeight;
			Point topLeft = new Point(new KPoint(x, y + cellWidthAndHeight));
			cell.setTopLeft(topLeft);
			if (col != 0){
				cell.getCellLeft().setTopRight(topLeft);
			}
		}
		// add the topRightPoint to the top right cell.
		{
			int row = numRows-1;
			int col = numCols-1;
			Cell cell = cells[row][col];
			float x = col*cellWidthAndHeight;
			float y = row*cellWidthAndHeight;
			Point topRight = new Point(new KPoint(x + cellWidthAndHeight, y + cellWidthAndHeight));
			cell.setTopRight(topRight);
		}

		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = cells[row][col];
				assert cell.getBotLeft() != null : ""+row+", "+col;
				assert cell.getBotRight() != null : ""+row+", "+col;
				assert cell.getTopLeft() != null : ""+row+", "+col;
				assert cell.getTopRight() != null : ""+row+", "+col;

			}
		}

		// add Links to all Points in the botLeft of all cells
		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = cells[row][col];
				Point p = cell.getBotLeft();
				{
					// make the right link
					Link link = new Link();
					link.setOrientation(Link.HORIZONTAL);
					Cell cell2 = cell.getCellDown();
					Point p2 = cell.getBotRight();
					link.setPoint(p);
					link.setPoint2(p2);
					link.setCell(cell);
					link.setCell2(cell2);
					p.setRightLink(link);
					p2.setLeftLink(link);
				}
				{
					// make the up link
					Link link = new Link();
					link.setOrientation(Link.VERTICAL);
					Cell cell2 = cell.getCellLeft();
					Point p2 = cell.getTopLeft();
					link.setPoint(p);
					link.setPoint2(p2);
					link.setCell(cell);
					link.setCell2(cell2);
					p.setUpLink(link);
					p2.setDownLink(link);
				}
			}
		}
		// add vertical Links to the right Points of the right col
		for (int row = 0; row < numRows; row++){
			int col = numCols-1;
			Cell cell = cells[row][col];
			Point p = cell.getBotRight();
			{
				// make the up link
				Link link = new Link();
				link.setOrientation(Link.VERTICAL);
				Cell cell2 = cell.getCellRight();	// will be null
				Point p2 = cell.getTopRight();
				link.setPoint(p);
				link.setPoint2(p2);
				link.setCell(cell);
				link.setCell2(cell2);
				p.setUpLink(link);
				p2.setDownLink(link);
			}
		}

		// add horizontal Links to the right Points of the right col
		for (int col = 0; col < numCols; col++){
			int row = numRows-1;
			Cell cell = cells[row][col];
			Point p = cell.getTopLeft();
			{
				// make the up link
				Link link = new Link();
				link.setOrientation(Link.HORIZONTAL);
				Cell cell2 = cell.getCellUp();	// will be null
				Point p2 = cell.getTopRight();
				link.setPoint(p);
				link.setPoint2(p2);
				link.setCell(cell);
				link.setCell2(cell2);
				p.setRightLink(link);
				p2.setLeftLink(link);
			}
		}


		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = cells[row][col];
				//cell.makePolygon();
				cell.setCenter(KPoint.midPoint(cell.getBotLeft().getPoint(), cell.getTopRight().getPoint()));
			}
		}
		


		//check:
		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				Cell cell = cells[row][col];
				if (cell.getBotLeft() == null){
					System.out.println(this.getClass().getSimpleName()+": cell.getBotLeft() == null, row == "+row+", col == "+col);
				}else if (cell.getBotRight() == null){
					System.out.println(this.getClass().getSimpleName()+": cell.getBotRight() == null, row == "+row+", col == "+col);
				}else if (cell.getTopRight() == null){
					System.out.println(this.getClass().getSimpleName()+": cell.getTopRight() == null, row == "+row+", col == "+col);
				}else if (cell.getTopLeft() == null){
					System.out.println(this.getClass().getSimpleName()+": cell.getTopLeft() == null, row == "+row+", col == "+col);
				}
			}
		}
	}

	ArrayList<Link> newBorderLinks = new ArrayList<Link>();
	public void explore(KPolygon sightPolygon){
		KPoint c = sightPolygon.getCenter();
		double r = sightPolygon.getRadius();
		KPoint botLeft = cells[0][0].getBotLeft().getPoint();//new KPoint(0,0);
		double leftColIndex = ((c.x - r) - botLeft.x)/cellWidthAndHeight;
		double rightColIndex = ((c.x + r) - botLeft.x)/cellWidthAndHeight;
		double botRowIndex = ((c.y - r) - botLeft.y)/cellWidthAndHeight;
		double topRowIndex = ((c.y + r) - botLeft.y)/cellWidthAndHeight;
		//System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", leftColIndex == "+leftColIndex+", rightColIndex == "+rightColIndex+", botRowIndex == "+botRowIndex+", topRowIndex == "+topRowIndex);
		if (botRowIndex < 0){
			botRowIndex = 0;
		}else if (botRowIndex >= numRows){
			botRowIndex = numRows - 1;
		}
		if (topRowIndex < 0){
			topRowIndex = 0;
		}else if (topRowIndex >= numRows){
			topRowIndex = numRows - 1;
		}
		if (leftColIndex < 0){
			leftColIndex = 0;
		}else if (leftColIndex >= numCols){
			leftColIndex = numCols - 1;
		}
		if (rightColIndex < 0){
			rightColIndex = 0;
		}else if (rightColIndex >= numCols){
			rightColIndex = numCols - 1;
		}
		int leftColIndexInt = (int)leftColIndex;
		int rightColIndexInt = (int)rightColIndex;
		int botRowIndexInt = (int)botRowIndex;
		int topRowIndexInt = (int)topRowIndex;
		//System.out.println(this.getClass().getSimpleName()+": c == "+c+botRowIndex+", leftColIndexInt == "+leftColIndexInt+", rightColIndexInt == "+rightColIndexInt+", botRowIndexInt == "+botRowIndexInt+", topRowIndexInt == "+topRowIndexInt);


		// see if any more cells are contained
		double cellRadiusSq = cellWidthAndHeight/2f;
		cellRadiusSq *= cellRadiusSq;
		for (int i = botRowIndexInt; i <= topRowIndexInt; i++){
			Cell[] row = cells[i];
			for (int j = leftColIndexInt; j <= rightColIndexInt; j++){
				Cell cell = row[j];
				if (cell.isDiscovered() == false){
					double sumRadiusSq = sightPolygon.getRadius() + cellRadiusSq;
					sumRadiusSq *= sumRadiusSq;
					if (c.distanceSq(cell.getCenter()) < sumRadiusSq){
						if (sightPolygon.contains(cell.getBotLeft().getPoint()) == true){
							cell.setDiscovered(true);
							if (cell.getCellDown() != null){
								cell.getCellDown().setDiscovered(true);
							}
							if (cell.getCellLeft() != null){
								cell.getCellLeft().setDiscovered(true);
							}
							if (cell.getCellDownLeft() != null){
								cell.getCellDownLeft().setDiscovered(true);
							}
							continue;
						}else{
							if (sightPolygon.intersectsLine(cell.getBotLeft().getPoint(), cell.getBotRight().getPoint())){
								cell.setDiscovered(true);
								if (cell.getCellDown() != null){
									cell.getCellDown().setDiscovered(true);
								}
							}
							if (sightPolygon.intersectsLine(cell.getBotRight().getPoint(), cell.getTopRight().getPoint())){
								cell.setDiscovered(true);
								if (cell.getCellRight() != null){
									cell.getCellRight().setDiscovered(true);
								}
							}
							if (sightPolygon.intersectsLine(cell.getTopRight().getPoint(), cell.getTopLeft().getPoint())){
								cell.setDiscovered(true);
								if (cell.getCellUp() != null){
									cell.getCellUp().setDiscovered(true);
								}
							}
							if (sightPolygon.intersectsLine(cell.getTopLeft().getPoint(), cell.getBotLeft().getPoint())){
								cell.setDiscovered(true);
								if (cell.getCellLeft() != null){
									cell.getCellLeft().setDiscovered(true);
								}
							}
						}
					}

//					if (sightPolygon.intersectionPossible(cell.getPolygon())){
//						if (sightPolygon.contains(cell.getBotLeft().getPoint()) == true ||
//								sightPolygon.intersectsPerimeter(cell.getPolygon()) == true){
//							cell.setDiscovered(true);
//							continue;
//						}
//					}

//					float centerToCenterDistSq = cell.getPolygon().getCenter().distanceSq(c);
//					float radiusPlusRadiusSq = cell.getPolygon().getRadius() + r;
//					radiusPlusRadiusSq *= radiusPlusRadiusSq;
//					if (centerToCenterDistSq < radiusPlusRadiusSq){
//							cell.setDiscovered(true);
//							continue;
//					}
//					if (sightPolygon.intersectionPossible(cell.getPolygon())){
//						//if (sightPolygon.intersects(cell.getPolygon())){
//						if (sightPolygon.contains(cell.getPolygon().getCenter()) == true ||
//								sightPolygon.intersectsPerimeter(cell.getPolygon()) == true){
//							cell.setDiscovered(true);
//							continue;
//						}
//					}
				}
			}
		}

	}

	//ArrayList<ArrayList<Cell>> bigList = new ArrayList<ArrayList<Cell>>();


	public float getCellWidthAndHeight() {
		return cellWidthAndHeight;
	}

	public float getHeight() {
		return h;
	}

	public int getNumCols() {
		return numCols;
	}

	public int getNumRows() {
		return numRows;
	}

	public float getWidth() {
		return w;
	}

	public Cell[][] getCells() {
		return cells;
	}

}
