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

/**
 *
 * @author Keith
 */
public class Cell {
	CellArray cellArray;
	int row;
	int col;
	boolean discovered = false;
	Point botLeft;
	Point botRight;
	Point topRight;
	Point topLeft;
	KPoint center;

	public Cell(CellArray cellArray, int row, int col){
		this.cellArray = cellArray;
		this.row = row;
		this.col = col;
	}

	public KPoint getCenter() {
		return center;
	}

	public void setCenter(KPoint center) {
		this.center = center;
	}
	
	public Point getBotLeft() {
		return botLeft;
	}

	public Point getBotRight() {
		return botRight;
	}

	public Point getTopLeft() {
		return topLeft;
	}

	public Point getTopRight() {
		return topRight;
	}

	public void setBotLeft(Point botLeft) {
		this.botLeft = botLeft;
	}

	public void setBotRight(Point botRight) {
		this.botRight = botRight;
	}

	public void setTopLeft(Point topLeft) {
		this.topLeft = topLeft;
	}

	public void setTopRight(Point topRight) {
		this.topRight = topRight;
	}

	public boolean isDiscovered() {
		return discovered;
	}

	public CellArray getCellArray() {
		return cellArray;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public void setDiscovered(boolean discovered) {
		this.discovered = discovered;
	}

	public Cell getCellUp(){
		int r = row+1;
		if (r < cellArray.getNumRows()){
			return cellArray.getCells()[r][col];
		}else{
			return null;
		}
	}
	public Cell getCellDown(){
		int r = row-1;
		if (r >= 0){
			return cellArray.getCells()[r][col];
		}else{
			return null;
		}
	}
	public Cell getCellLeft(){
		int c = col-1;
		if (c >= 0){
			return cellArray.getCells()[row][c];
		}else{
			return null;
		}
	}
	public Cell getCellRight(){
		int c = col+1;
		if (c < cellArray.getNumCols()){
			return cellArray.getCells()[row][c];
		}else{
			return null;
		}
	}

	public Cell getCellUpRight(){
		int r = row+1;
		int c = col+1;
		if (r < cellArray.getNumRows() && c < cellArray.getNumCols()){
			return cellArray.getCells()[r][c];
		}else{
			return null;
		}
	}
	public Cell getCellUpLeft(){
		int r = row+1;
		int c = col-1;
		if (r < cellArray.getNumRows() && c >= 0){
			return cellArray.getCells()[r][c];
		}else{
			return null;
		}
	}
	public Cell getCellDownRight(){
		int r = row-1;
		int c = col+1;
		if (r >= 0 && c < cellArray.getNumCols()){
			return cellArray.getCells()[r][c];
		}else{
			return null;
		}
	}
	public Cell getCellDownLeft(){
		int r = row-1;
		int c = col-1;
		if (r >= 0 && c >= 0){
			return cellArray.getCells()[r][c];
		}else{
			return null;
		}
	}

	//	public boolean isBorder(){
//		for (int i = 0; i < neighbours.length; i++){
//			Cell neighbour = neighbours[i];
//			if (neighbour == null || neighbour.isDiscovered()){
//				return true;
//			}
//		}
//		Cell neighbour = this.getCellUpRight();
//		if (neighbour == null || neighbour.isDiscovered()){
//			return true;
//		}
//		neighbour = this.getCellUpLeft();
//		if (neighbour == null || neighbour.isDiscovered()){
//			return true;
//		}
//		neighbour = this.getCellDownLeft();
//		if (neighbour == null || neighbour.isDiscovered()){
//			return true;
//		}
//		neighbour = this.getCellDownRight();
//		if (neighbour == null || neighbour.isDiscovered()){
//			return true;
//		}
//
//		return false;
//	}


}
