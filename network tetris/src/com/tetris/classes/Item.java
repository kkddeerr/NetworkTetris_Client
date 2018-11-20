package com.tetris.classes;

import javax.swing.ImageIcon;

public class Item {
	private int itemNum;
	private ImageIcon icon;
	
	public Item(int itemNum) {
		super();
		this.itemNum = itemNum;
		if(itemNum==1){icon=new ImageIcon("img/clear.png");}
		else if(itemNum==2){icon=new ImageIcon("img/plus1.png");}
		else {icon=new ImageIcon("img/minus1.png");}
	}
	
	
	//getter and setter
	public int getItemNum() {return itemNum;}
	public void setItemNum(int itemNum) {this.itemNum = itemNum;}
	public ImageIcon getIcon() {return icon;}
	public void setIcon(ImageIcon icon) {this.icon = icon;}
}
