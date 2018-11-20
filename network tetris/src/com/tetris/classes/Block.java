package com.tetris.classes;

import java.awt.Color;
import java.awt.Graphics;

import com.tetris.window.TetrisBoard;

public class Block {
	private int size = TetrisBoard.BLOCK_SIZE;
	private int smallSize = TetrisBoard.SMALL_BLOCK_SIZE;
	private int width = size, height = size;
	private int smallWidth = smallSize, smallHeight = smallSize;
	private int gap = 3;
	private int fixGridX, fixGridY;
	private int posGridX, posGridY;
	private Color color;
	private Color ghostColor;	
	private boolean ghost;
	private int item=0;//0 = no item, 1 = clear item, 2 = +1 item, 3 = -1 item
	
	/**
	 * 
	 * @param fixGridX : �簢�� ���� X �׸�����ǥ
	 * @param fixGridY : �簢�� ���� Y �׸�����ǥ
	 * @param color : �簢�� ����
	 */
	public Block(int fixGridX, int fixGridY, Color color, Color ghostColor) {
		this.fixGridX = fixGridX;
		this.fixGridY = fixGridY;
		this.color=color;
		this.ghostColor = ghostColor;
	}
	

	/**
	 * �簢���� �׷��ش�.
	 * @param g
	 */
	public void drawColorBlock(Graphics g){
		if(item>0)g.setColor(Color.YELLOW);
		else if(ghost)g.setColor(ghostColor);
		else g.setColor(color);
		
		g.fillRect((fixGridX+posGridX)*size + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size + TetrisBoard.BOARD_Y, width, height);
		g.setColor(Color.BLACK);
		g.drawRect((fixGridX+posGridX)*size + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size + TetrisBoard.BOARD_Y, width, height);
		g.drawLine((fixGridX+posGridX)*size + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size + TetrisBoard.BOARD_Y, (fixGridX+posGridX)*size+width + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size+height + TetrisBoard.BOARD_Y);
		g.drawLine((fixGridX+posGridX)*size + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size+height + TetrisBoard.BOARD_Y, (fixGridX+posGridX)*size+width + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size + TetrisBoard.BOARD_Y);
		if(item>0)g.setColor(Color.YELLOW);
		else if(ghost)g.setColor(ghostColor);
		else g.setColor(color);
		g.fillRect((fixGridX+posGridX)*size+gap + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size+gap + TetrisBoard.BOARD_Y, width-gap*2, height-gap*2);
		g.setColor(Color.BLACK);
		g.drawRect((fixGridX+posGridX)*size+gap + TetrisBoard.BOARD_X, (fixGridY+posGridY)*size+gap + TetrisBoard.BOARD_Y, width-gap*2, height-gap*2);
	}
	
	
	public void drawSmallColorBlock(Graphics g){
		if(ghost)g.setColor(ghostColor);
		else g.setColor(color);
		g.fillRect((fixGridX+posGridX)*smallSize + TetrisBoard.BOARD_X, (fixGridY+posGridY)*smallSize + TetrisBoard.BOARD_Y, smallWidth, smallHeight);
		g.setColor(Color.BLACK);
		g.drawRect((fixGridX+posGridX)*smallSize + TetrisBoard.BOARD_X, (fixGridY+posGridY)*smallSize + TetrisBoard.BOARD_Y, smallWidth, smallHeight);
		if(ghost)g.setColor(ghostColor);
		else g.setColor(color);
		g.fillRect((fixGridX+posGridX)*smallSize+gap + TetrisBoard.BOARD_X, (fixGridY+posGridY)*smallSize+gap + TetrisBoard.BOARD_Y, smallWidth-gap*2, smallHeight-gap*2);
		g.setColor(Color.BLACK);
		g.drawRect((fixGridX+posGridX)*smallSize+gap + TetrisBoard.BOARD_X, (fixGridY+posGridY)*smallSize+gap + TetrisBoard.BOARD_Y, smallWidth-gap*2, smallHeight-gap*2);
	}
	
	/**
	 * ���� ���� ������ǥ�� �����ش�.
	 * @return ������� X������ǥ
	 */
	public int getX(){return posGridX + fixGridX;}	
	
	
	/**
	 * ���� ���� ������ǥ�� �����ش�.
	 * @return ������� Y������ǥ
	 */
	public int getY(){return posGridY + fixGridY;}

	
	/**
	 * Getter Setter
	 */
	public int getPosGridX(){return this.posGridX;}
	public int getPosGridY(){return this.posGridY;}
	public void setPosGridX(int posGridX) {this.posGridX = posGridX;}
	public void setPosGridY(int posGridY) {this.posGridY = posGridY;}
	public void setPosGridXY(int posGridX, int posGridY){this.posGridX = posGridX;this.posGridY = posGridY;}
	public void setFixGridX(int fixGridX) {this.fixGridX = fixGridX;}
	public void setFixGridY(int fixGridY) {this.fixGridY = fixGridY;}
	public void setFixGridXY(int fixGridX, int fixGridY){this.fixGridX = fixGridX;this.fixGridY = fixGridY;}
	public void setGhostView(boolean b){this.ghost = b;}
	public int getFixGridX() {return fixGridX;}
	public int getFixGridY() {return fixGridY;}
	public int getItem() {return item;}
	public void setItem(int item) {this.item = item;}
	
}
