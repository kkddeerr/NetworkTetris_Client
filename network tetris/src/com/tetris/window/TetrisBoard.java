package com.tetris.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tetris.classes.Block;
import com.tetris.classes.Item;
//import com.tetris.classes.Itemimg;
import com.tetris.classes.TetrisBlock;
import com.tetris.controller.TetrisController;
import com.tetris.network.GameClient;
import com.tetris.shape.CenterUp;
import com.tetris.shape.LeftTwoUp;
import com.tetris.shape.LeftUp;
import com.tetris.shape.Line;
import com.tetris.shape.Nemo;
import com.tetris.shape.RightTwoUp;
import com.tetris.shape.RightUp;

public class TetrisBoard extends JPanel implements Runnable, KeyListener, MouseListener, ActionListener{
	private static final long serialVersionUID = 1L;
	
	private ImageIcon emptyIcon=new ImageIcon("img/empty.png");
	
	private Tetris tetris;
	private GameClient client;
	
	public static final int BLOCK_SIZE = 20;
	public static final int SMALL_BLOCK_SIZE = 11;
	public static final int BOARD_X = 120;
	public static final int BOARD_Y = 50;
	public static final int CREATE_ITEM = 1000;//�������� ����� �ֱ� 100=1��
	private int minX=1, minY=0, maxX=10, maxY=21, down=50, up=0
			;
	private final int MESSAGE_X = 2;
	private final int MESSAGE_WIDTH = BLOCK_SIZE * (7 + minX);
	private final int MESSAGE_HEIGHT = BLOCK_SIZE * (6 + minY);
	private final int PANEL_WIDTH = maxX*BLOCK_SIZE + MESSAGE_WIDTH + BOARD_X;
	private final int PANEL_HEIGHT = maxY*BLOCK_SIZE + MESSAGE_HEIGHT + BOARD_Y;
	
	private SystemMessageArea systemMsg = new SystemMessageArea(BLOCK_SIZE*1,BOARD_Y + BLOCK_SIZE , BLOCK_SIZE*5, BLOCK_SIZE*12+ BLOCK_SIZE*7);
	private MessageArea messageArea = new MessageArea(this,2, PANEL_HEIGHT - (MESSAGE_HEIGHT-MESSAGE_X)+45, PANEL_WIDTH-BLOCK_SIZE*7-2, MESSAGE_HEIGHT-50);
	
	
	private JButton btnStart = new JButton("�����ϱ�");
	private JButton btnExit = new JButton("������");
	private JLabel[] itemListLabel =new JLabel[10];
	private Integer[] lv = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
	private JComboBox<Integer> comboSpeed = new JComboBox<Integer>(lv);
	
	private String ip;
	private int port;
	private String nickName;
	private Thread th;
	private ArrayList<Block> blockList;
	private ArrayList<TetrisBlock> nextBlocks;
	private TetrisBlock shap;
	private TetrisBlock ghost;
	private TetrisBlock hold;
	private Block[][] map;
	private Block[][][] maps = new Block[5][maxY][maxX];;
	private TetrisController controller;
	private TetrisController controllerGhost;
	private ArrayList<Item> itemList;
	
	private boolean isMaster = false;
	private boolean isPlay = false;
	private boolean usingGhost = true;
	private boolean usingGrid = true;
	private int removeLineCount = 0;
	private int removeLineCombo = 0;
	private Image img = Toolkit.getDefaultToolkit().createImage("/network tetris/src/img/Desert.jpg");

	public TetrisBoard(Tetris tetris, GameClient client) {
		this.tetris = tetris;
		this.client = client;
		this.setPreferredSize(new Dimension(PANEL_WIDTH+440,PANEL_HEIGHT));//�⺻ũ��
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.setLayout(null);
		this.setFocusable(true);
		
		btnStart.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight(), BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnStart.setFocusable(false);
		btnStart.setEnabled(false);
		btnStart.addActionListener(this);
		btnExit.setBounds(PANEL_WIDTH - BLOCK_SIZE*7, PANEL_HEIGHT - messageArea.getHeight()/2, BLOCK_SIZE*7, messageArea.getHeight()/2);
		btnExit.setFocusable(false);	
		btnExit.addActionListener(this);
		
		for(int i=0;i<10;i++){
			itemListLabel[i]=new JLabel();
			itemListLabel[i].setBounds(BOARD_X + BLOCK_SIZE*minX+(BLOCK_SIZE)*i, BOARD_Y+20*BLOCK_SIZE+30, BLOCK_SIZE+1, BLOCK_SIZE+1);
			itemListLabel[i].setIcon(emptyIcon);
			this.add(itemListLabel[i]);
		}
		
		comboSpeed.setBounds(PANEL_WIDTH - BLOCK_SIZE*8, 5, 45, 20);
		this.add(comboSpeed);
		
		this.add(systemMsg);
		this.add(messageArea);
		this.add(btnStart);		
		this.add(btnExit);

	}
	
	public void startNetworking(String ip, int port, String nickName){
		this.ip = ip;
		this.port = port;
		this.nickName = nickName;
		this.repaint();
	}
	
	/**TODO : ���ӽ���
	 * ������ �����Ѵ�.
	 */
	
	public void gameStart(int speed){
		comboSpeed.setSelectedItem(new Integer(speed));
		//���� ���� �����带 ������Ų��.
		if(th!=null){
			try {isPlay = false;th.join();} 
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		//�ʼ���
		map = new Block[maxY][maxX];
		maps = new Block[5][maxY][maxX];
		itemList=new ArrayList<Item>();
		blockList = new ArrayList<Block>();
		nextBlocks = new ArrayList<TetrisBlock>();
		
		for(int i=0;i<10;i++){
			itemListLabel[i].setIcon(emptyIcon);
		}
		itemList.clear();
		
		//��������
		shap = getRandomTetrisBlock();
		ghost = getBlockClone(shap,true);
		hold = null;
		controller = new TetrisController(shap,maxX-1,maxY-1,map);
		controllerGhost = new TetrisController(ghost,maxX-1,maxY-1,map);
		this.showGhost();
		for(int i=0 ; i<5 ; i++){
			nextBlocks.add(getRandomTetrisBlock());
		}
		
		//������ ����
		isPlay = true;
		th = new Thread(this);
		th.start();
	}
	
	
	//TODO : paint
	@Override
	protected void paintComponent(Graphics g) {

		g.clearRect(0, 0, this.getWidth(), this.getHeight()+1);
		

		g.setColor(new Color(0,87,102));
		g.fillRect(0, 0, (maxX+minX+13)*BLOCK_SIZE+501, BOARD_Y);
		g.drawImage(img, 0, 0, this.getWidth(), this.getHeight()+1, null);
		
		g.setColor(new Color(92,209,229));
		g.fillRect(0, BOARD_Y, (maxX+minX+13)*BLOCK_SIZE+501, maxY*BLOCK_SIZE+121);
		g.setColor(Color.WHITE);
		
		//IP ���
		g.drawString("ip : "+ip+"     port : "+port, 20, 20);
		
		//NickName ���
		g.drawString("�г��� : "+nickName, 20, 40);
		
		//�ӵ�
		Font font= g.getFont();
		g.setFont(new Font("����", Font.BOLD,13));
		g.drawString("�ӵ�", PANEL_WIDTH - BLOCK_SIZE*10, 20);
		g.setFont(font);
		
		g.setColor(Color.BLACK);
		g.fillRect(BOARD_X + BLOCK_SIZE*minX, BOARD_Y, maxX*BLOCK_SIZE+1, maxY*BLOCK_SIZE+1);//�ڽ��� ��Ʈ���� ȭ��
		
		g.fillRect(350+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y, maxX*SMALL_BLOCK_SIZE+1, maxY*SMALL_BLOCK_SIZE+1);//����1�� ��Ʈ���� ȭ��
		g.drawString("1", 350+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y+243);
		g.fillRect(493+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y, maxX*SMALL_BLOCK_SIZE+1, maxY*SMALL_BLOCK_SIZE+1);//����2�� ��Ʈ���� ȭ��
		g.drawString("2", 493+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y+243);
		g.fillRect(636+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y, maxX*SMALL_BLOCK_SIZE+1, maxY*SMALL_BLOCK_SIZE+1);//����3�� ��Ʈ���� ȭ��
		g.drawString("3", 636+BOARD_X + BLOCK_SIZE*minX+4, BOARD_Y+243);
		g.fillRect(350+BOARD_X + BLOCK_SIZE*minX+4, 275+BOARD_Y, maxX*SMALL_BLOCK_SIZE+1, maxY*SMALL_BLOCK_SIZE+1);//����4�� ��Ʈ���� ȭ��
		g.drawString("4", 350+BOARD_X + BLOCK_SIZE*minX+4, 275+BOARD_Y+243);
		g.fillRect(493+BOARD_X + BLOCK_SIZE*minX+4, 275+BOARD_Y, maxX*SMALL_BLOCK_SIZE+1, maxY*SMALL_BLOCK_SIZE+1);//����5�� ��Ʈ���� ȭ��
		g.drawString("5", 493+BOARD_X + BLOCK_SIZE*minX+4, 275+BOARD_Y+243);
		
		g.fillRect(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1,BOARD_Y + BLOCK_SIZE, BLOCK_SIZE*5,BLOCK_SIZE*5); 
		
		g.fillRect(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1,BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*7, BLOCK_SIZE*5,BLOCK_SIZE*12);
		

		
		g.setFont(new Font(font.getFontName(),font.getStyle(),20));
		g.drawString("N E X T", BOARD_X + BLOCK_SIZE + (maxX+1)*BLOCK_SIZE+1 + 12, BOARD_Y + BLOCK_SIZE + BLOCK_SIZE*5 + 20);
		g.setFont(font);
		
		//�׸��� ǥ��
		if(usingGrid){
			g.setColor(Color.darkGray);
			for(int i=1;i<maxY;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX, BOARD_Y+BLOCK_SIZE*(i+minY), BOARD_X + (maxX+minX)*BLOCK_SIZE, BOARD_Y+BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(BOARD_X + BLOCK_SIZE*(i+minX), BOARD_Y+BLOCK_SIZE*minY, BOARD_X + BLOCK_SIZE*(i+minX), BOARD_Y+BLOCK_SIZE*(minY+maxY));
			
			for(int i=1;i<maxY;i++) g.drawLine(363+BOARD_X + SMALL_BLOCK_SIZE*minX, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY), 363+BOARD_X + (maxX+minX)*SMALL_BLOCK_SIZE, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(363+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*minY, 363+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*(minY+maxY));
			for(int i=1;i<maxY;i++) g.drawLine(506+BOARD_X + SMALL_BLOCK_SIZE*minX, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY), 506+BOARD_X + (maxX+minX)*SMALL_BLOCK_SIZE, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(506+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*minY, 506+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*(minY+maxY));
			for(int i=1;i<maxY;i++) g.drawLine(649+BOARD_X + SMALL_BLOCK_SIZE*minX, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY), 649+BOARD_X + (maxX+minX)*SMALL_BLOCK_SIZE, BOARD_Y+SMALL_BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(649+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*minY, 649+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), BOARD_Y+SMALL_BLOCK_SIZE*(minY+maxY));
			for(int i=1;i<maxY;i++) g.drawLine(363+BOARD_X + SMALL_BLOCK_SIZE*minX, 275+BOARD_Y+SMALL_BLOCK_SIZE*(i+minY), 363+BOARD_X + (maxX+minX)*SMALL_BLOCK_SIZE, 275+BOARD_Y+SMALL_BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(363+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), 275+BOARD_Y+SMALL_BLOCK_SIZE*minY, 363+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), 275+BOARD_Y+SMALL_BLOCK_SIZE*(minY+maxY));
			for(int i=1;i<maxY;i++) g.drawLine(506+BOARD_X + SMALL_BLOCK_SIZE*minX, 275+BOARD_Y+SMALL_BLOCK_SIZE*(i+minY), 506+BOARD_X + (maxX+minX)*SMALL_BLOCK_SIZE, 275+BOARD_Y+SMALL_BLOCK_SIZE*(i+minY));
			for(int i=1;i<maxX;i++) g.drawLine(506+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), 275+BOARD_Y+SMALL_BLOCK_SIZE*minY, 506+BOARD_X + SMALL_BLOCK_SIZE*(i+minX), 275+BOARD_Y+SMALL_BLOCK_SIZE*(minY+maxY));
			
			for(int i=1;i<5;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+1, BOARD_Y + BLOCK_SIZE*(i+1), BOARD_X + BLOCK_SIZE*minX + (maxX+1)*BLOCK_SIZE+BLOCK_SIZE*5,BOARD_Y + BLOCK_SIZE*(i+1));
			for(int i=1;i<5;i++) g.drawLine(BOARD_X + BLOCK_SIZE*minX + (maxX+1+i)*BLOCK_SIZE+1, BOARD_Y + BLOCK_SIZE, BOARD_X + BLOCK_SIZE*minX + BLOCK_SIZE+BLOCK_SIZE*(10+i)+1,BOARD_Y + BLOCK_SIZE*6-1);	
		}
		
		int x=0,y=0,newY=0;
		if(hold!=null){
			x=0; y=0; newY=3;
			x = hold.getPosX();
			y = hold.getPosY();
			hold.setPosX(-4+minX);
			hold.setPosY(newY+minY);
			hold.drawBlock(g);
			hold.setPosX(x);
			hold.setPosY(y);
		}
		
		if(nextBlocks!=null){
			x=0; y=0; newY=3;
			for(int i = 0 ; i<nextBlocks.size() ; i++){
				TetrisBlock block = nextBlocks.get(i);
				x = block.getPosX();
				y = block.getPosY();
				block.setPosX(13+minX);
				block.setPosY(newY+minY);
				if(newY==3) newY=6;
				block.drawBlock(g);
				block.setPosX(x);
				block.setPosY(y);
				newY+=3;
			}
		}
		
		if(blockList!=null){
			x=0; y=0;
			for(int i = 0 ; i<blockList.size() ; i++){
				Block block = blockList.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();
				block.setPosGridX(x+minX);
				block.setPosGridY(y+minY);
				block.drawColorBlock(g);
				block.setPosGridX(x);
				block.setPosGridY(y);
			}
		}

		if(ghost!=null){

			if(usingGhost){
				x=0; y=0;
				x = ghost.getPosX();
				y = ghost.getPosY();
				ghost.setPosX(x+minX);
				ghost.setPosY(y+minY);
				ghost.drawBlock(g);
				ghost.setPosX(x);
				ghost.setPosY(y);
			}
		}
		
		if(shap!=null){
			x=0; y=0;
			x = shap.getPosX();
			y = shap.getPosY();
			shap.setPosX(x+minX);
			shap.setPosY(y+minY);
			shap.drawBlock(g);
			shap.setPosX(x);
			shap.setPosY(y);
		}
		
		//����1ȭ��
		if(maps[0]!=null){
			for(int i=0;i<maps[0].length;i++){
				for(int j=0;j<maps[0][i].length;j++){
					if(maps[0][i][j]!=null){
						x=0; y=0;
						x= maps[0][i][j].getPosGridX();
						y= maps[0][i][j].getPosGridY();
						maps[0][i][j].setPosGridX(x+minX+33);
						maps[0][i][j].setPosGridY(y+minY);
						maps[0][i][j].drawSmallColorBlock(g);
						maps[0][i][j].setPosGridX(x);
						maps[0][i][j].setPosGridY(y);
					}
				}
			}
		}
		
		//����2ȭ��
		if(maps[1]!=null){
			for(int i=0;i<maps[1].length;i++){
				for(int j=0;j<maps[1][i].length;j++){
					if(maps[1][i][j]!=null){
						x=0; y=0;
						x= maps[1][i][j].getPosGridX();
						y= maps[1][i][j].getPosGridY();
						maps[1][i][j].setPosGridX(x+minX+46);
						maps[1][i][j].setPosGridY(y+minY);
						maps[1][i][j].drawSmallColorBlock(g);
						maps[1][i][j].setPosGridX(x);
						maps[1][i][j].setPosGridY(y);
					}
				}
			}
		}
		
		//����3ȭ��
		if(maps[2]!=null){
			for(int i=0;i<maps[2].length;i++){
				for(int j=0;j<maps[2][i].length;j++){
					if(maps[2][i][j]!=null){
						x=0; y=0;
						x= maps[2][i][j].getPosGridX();
						y= maps[2][i][j].getPosGridY();
						maps[2][i][j].setPosGridX(x+minX+59);
						maps[2][i][j].setPosGridY(y+minY);
						maps[2][i][j].drawSmallColorBlock(g);
						maps[2][i][j].setPosGridX(x);
						maps[2][i][j].setPosGridY(y);
					}
				}
			}
		}
		
		//����4ȭ��
		if(maps[3]!=null){
			for(int i=0;i<maps[3].length;i++){
				for(int j=0;j<maps[3][i].length;j++){
					if(maps[3][i][j]!=null){
						x=0; y=0;
						x= maps[3][i][j].getPosGridX();
						y= maps[3][i][j].getPosGridY();
						maps[3][i][j].setPosGridX(x+minX+33);
						maps[3][i][j].setPosGridY(y+minY+25);
						maps[3][i][j].drawSmallColorBlock(g);
						maps[3][i][j].setPosGridX(x);
						maps[3][i][j].setPosGridY(y);
					}
				}
			}
		}
		
		//����5ȭ��
		if(maps[4]!=null){
			for(int i=0;i<maps[4].length;i++){
				for(int j=0;j<maps[4][i].length;j++){
					if(maps[4][i][j]!=null){
						x=0; y=0;
						x= maps[4][i][j].getPosGridX();
						y= maps[4][i][j].getPosGridY();
						maps[4][i][j].setPosGridX(x+minX+46);
						maps[4][i][j].setPosGridY(y+minY+25);
						maps[4][i][j].drawSmallColorBlock(g);
						maps[4][i][j].setPosGridX(x);
						maps[4][i][j].setPosGridY(y);
					}
				}
			}
		}
		
	}
	
	@Override
	public void run() {
		int countMove = (21-(int)comboSpeed.getSelectedItem())*5;
		int countDown = 0;
		int countUp = up;
		int count =0;
		
		while(isPlay){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(countDown!=0){
				countDown--;
				if(countDown==0){
					
					if(controller!=null && !controller.moveDown()) this.fixingTetrisBlock();
				}
				this.repaint();
				continue;
			}
			
			countMove--;
			if (countMove == 0) {
				countMove = (21-(int)comboSpeed.getSelectedItem())*5;
				if (controller != null && !controller.moveDown()) countDown = down;
				else this.showGhost();
			}
			
			if (countUp != 0) {
				countUp--;
				if (countUp == 0) {
					countUp = up;
					addBlockLine(1);
				}
			}
			++count;
			if(count%100==0){
				if(client!=null){
					client.sendBoard(convertMapBI(map));
				}
			}
			if(count%CREATE_ITEM==0){
				Random random=new Random();
				int item=random.nextInt(100);
				if(item<10)item=1;
				else if(item<55)item=2;
				else item=3;
				while(true){
					int tmpX=random.nextInt(maxX);
					int tmpY=random.nextInt(maxY);
					if(map[tmpY][tmpX]!=null){
						map[tmpY][tmpX].setItem(item);
						break;
					}
				}
			}
			
			this.repaint();
		}//while()
	}//run()

	
	/**
	 * ��(���̱�, ��)�� ���Ϸ� �̵��Ѵ�.
	 * @param lineNumber	
	 * @param num -1 or 1
	 */
	public void dropBoard(int lineNumber, int num){
		
		// ���� ����Ʈ����.
		this.dropMap(lineNumber,num);
		
		//��ǥ�ٲ��ֱ�(1��ŭ����)
		this.changeTetrisBlockLine(lineNumber,num);
		
		//�ٽ� üũ�ϱ�
		this.checkMap();
		
		//��Ʈ �ٽ� �Ѹ���
		this.showGhost();
	}
	
	
	/**
	 * lineNumber�� ���� ���ε��� ��� numĭ�� ������.
	 * @param lineNumber
	 * @param num ĭ�� -1,1
	 */
	private void dropMap(int lineNumber, int num) {
		if(num==1){
			//���پ� ������
			for(int i= lineNumber ; i>0 ;i--){
				for(int j=0 ; j<map[i].length ;j++){
					map[i][j] = map[i-1][j];
				}
			}
			
			//�� ������ null�� �����
			for(int j=0 ; j<map[0].length ;j++){
				map[0][j] = null;
			}
		}
		else if(num==-1){
			//���پ� �ø���
			for(int i= 1 ; i<=lineNumber ;i++){
				for(int j=0 ; j<map[i].length ;j++){
					map[i-1][j] = map[i][j];
				}
			}
			
			//removeLine�� null�� �����
			for(int j=0 ; j<map[0].length ;j++){
				map[lineNumber][j] = null;
			}
		}
	}
	
	
	/**
	 * lineNumber�� ���� ���ε��� ��� num��ŭ �̵���Ų��.
	 * @param lineNumber 
	 * @param num	�̵��� ����
	 */
	private void changeTetrisBlockLine(int lineNumber, int num){
		int y=0, posY=0;
		for(int i=0 ; i<blockList.size() ; i++){
			y = blockList.get(i).getY();
			posY = blockList.get(i).getPosGridY();
			if(y<=lineNumber)blockList.get(i).setPosGridY(posY + num);
		}
	}

	
	/**
	 * ��Ʈ���� ���� ������Ų��. 
	 */
	private void fixingTetrisBlock() {
		synchronized (this) {
			if(stop){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		boolean isCombo = false;
		removeLineCount = 0;
		
		// drawList �߰�
		for (Block block : shap.getBlock()) {
			blockList.add(block);
		}
		
		// check
		isCombo = checkMap();

		if(isCombo) removeLineCombo++;
		else removeLineCombo = 0;
		
		//�ݹ�޼ҵ�
		this.getFixBlockCallBack(blockList,removeLineCombo,removeLineCount);
		
		//���� ��Ʈ���� ���� �����´�.
		this.nextTetrisBlock();
	}//fixingTetrisBlock()
	
	
	/**
	 * 
	 * @return true-����⼺��, false-��������
	 */
	private boolean checkMap(){
		boolean isCombo = false;
		int count = 0;
		Block mainBlock;
		
		for(int i=0 ; i<blockList.size() ;i++){
			mainBlock = blockList.get(i);
			
			// map�� �߰�
			if(mainBlock.getY()<0 || mainBlock.getY() >=maxY) continue;
			
			if(mainBlock.getY()<maxY && mainBlock.getX()<maxX) 
				map[mainBlock.getY()][mainBlock.getX()] = mainBlock;

			// ���� �� á�� ���. ������ �����Ѵ�.
			if (mainBlock.getY() == 1 && mainBlock.getX() > 2 && mainBlock.getX() < 7) {
				this.gameEndCallBack();
				break;
			}
			
			//1�ٰ��� üũ
			count = 0;
			for (int j = 0; j < maxX; j++) {
				if(map[mainBlock.getY()][j] != null) count++;
				
			}
			
			//block�� �ش� line�� �����.
			if (count == maxX) {
				for(int j=0;j<maxX;j++){
					if(itemList.size()<10){
						if(map[mainBlock.getY()][j].getItem()!=0){
							System.out.println(map[mainBlock.getY()][j].getItem());
							itemList.add(new Item(map[mainBlock.getY()][j].getItem()));//�����۸���Ʈ�� �������� �߰���
							if(!itemList.isEmpty())
								for(int k=0;k<itemList.size();k++){
									itemListLabel[k].setIcon(itemList.get(k).getIcon());
								}
						}
					}
				}
				removeLineCount++;
				this.removeBlockLine(mainBlock.getY());
				isCombo = true;
				
			}
		}
		return isCombo;
	}
	
	/**
	 * ��Ʈ���� �� ����Ʈ���� ��Ʈ���� ���� �޾ƿ´�.
	 */
	public void nextTetrisBlock(){
		shap = nextBlocks.get(0);
		this.initController();
		nextBlocks.remove(0);
		nextBlocks.add(getRandomTetrisBlock());
	}
	private void initController(){
		controller.setBlock(shap);
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
	}
	
	
	/**
	 * lineNumber ������ �����ϰ�, drawlist���� �����ϰ�, map�� �Ʒ��� ������.
	 * @param lineNumber ��������
	 */
	private void removeBlockLine(int lineNumber) {
		// 1���� ������
		for (int j = 0; j < maxX ; j++) {
			for (int s = 0; s < blockList.size(); s++) {
				Block b = blockList.get(s);
				if (b == map[lineNumber][j])
					blockList.remove(s);
			}
			map[lineNumber][j] = null;
		}// for(j)

		this.dropBoard(lineNumber,1);
	}
	
	/**TODO : ���������ݺ�
	 * ������ ����Ǹ� ����Ǵ� �޼ҵ�
	 */
	public void gameEndCallBack(){
		client.gameover();
		this.isPlay = false;
	}
	
	
	/**
	 * ��Ʈ���� �����ش�.
	 */
	private void showGhost(){
		ghost = getBlockClone(shap,true);
		controllerGhost.setBlock(ghost);
		controllerGhost.moveQuickDown(shap.getPosY(), true);
	}	
	
	
	/**
	 * �������� ��Ʈ���� ���� �����ϰ� ��ȯ�Ѵ�.
	 * @return ��Ʈ���� ��
	 */
	public TetrisBlock getRandomTetrisBlock(){
		switch((int)(Math.random()*7)){
		case TetrisBlock.TYPE_CENTERUP : return new CenterUp(4, 1);
		case TetrisBlock.TYPE_LEFTTWOUP : return new LeftTwoUp(4, 1);
		case TetrisBlock.TYPE_LEFTUP : return new LeftUp(4, 1);
		case TetrisBlock.TYPE_RIGHTTWOUP : return new RightTwoUp(4, 1);
		case TetrisBlock.TYPE_RIGHTUP : return new RightUp(4, 1);
		case TetrisBlock.TYPE_LINE : return new Line(4, 1);
		case TetrisBlock.TYPE_NEMO : return new Nemo(4, 1);
		}
		return null;
	}
	
	
	/**
	 * tetrisBlock�� ���� ������� ��Ʈ�� ������� ��ȯ�Ѵ�.
	 * @param tetrisBlock ��Ʈ�� ������� ������ ��
	 * @return ��Ʈ�� ������� ��ȯ
	 */
	public TetrisBlock getBlockClone(TetrisBlock tetrisBlock, boolean isGhost){
		TetrisBlock blocks = null;
		switch(tetrisBlock.getType()){
		case TetrisBlock.TYPE_CENTERUP : blocks =  new CenterUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTTWOUP : blocks =  new LeftTwoUp(4, 1); break;
		case TetrisBlock.TYPE_LEFTUP : blocks =  new LeftUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTTWOUP : blocks =  new RightTwoUp(4, 1); break;
		case TetrisBlock.TYPE_RIGHTUP : blocks =  new RightUp(4, 1); break;
		case TetrisBlock.TYPE_LINE : blocks =  new Line(4, 1); break;
		case TetrisBlock.TYPE_NEMO : blocks =  new Nemo(4, 1); break;
		}
		if(blocks!=null && isGhost){
			blocks.setGhostView(isGhost);
			blocks.setPosX(tetrisBlock.getPosX());
			blocks.setPosY(tetrisBlock.getPosY());
			blocks.rotation(tetrisBlock.getRotationIndex());
		}
		return blocks;
	}	
	
	
	/**TODO : �ݹ�޼ҵ�
	 * ��Ʈ���� ���� ������ �� �ڵ� ȣ�� �ȴ�.
	 * @param removeCombo	���� �޺� ��
	 * @param removeMaxLine	�ѹ��� ���� �ټ� 
	 */
	public void getFixBlockCallBack(ArrayList<Block> blockList, int removeCombo, int removeMaxLine){
		if(removeCombo<3){
			if(removeMaxLine==3)client.addBlock(1);
			else if(removeMaxLine==4)client.addBlock(3);
		}else if(removeCombo<10){
			if(removeMaxLine==3)client.addBlock(2);
			else if(removeMaxLine==4)client.addBlock(4);
			else client.addBlock(1);
		}else{
			if(removeMaxLine==3)client.addBlock(3);
			else if(removeMaxLine==4)client.addBlock(5);
			else client.addBlock(2);
		}
	}
	
	/**
	 * ���� �ؿ� �ٿ� ���� �����Ѵ�.
	 * @param numOfLine
	 */
	boolean stop = false;
	public void addBlockLine(int numOfLine){
		stop = true;
		// �����Ⱑ ���� ������ ����Ѵ�.
		// �����⸦ ��� ������ �� �ٽ� �����Ѵ�.
		Block block;
		int rand = (int) (Math.random() * maxX);
		for (int i = 0; i < numOfLine; i++) {
			this.dropBoard(maxY - 1, -1);
			for (int col = 0; col < maxX; col++) {
				if (col != rand) {
					block = new Block(0, 0, Color.GRAY, Color.GRAY);
					block.setPosGridXY(col, maxY - 1);
					blockList.add(block);
					map[maxY - 1][col] = block;
				}
			}
			//���� �������� ���� ��ġ�� ���� ���� �ø���.
			boolean up = false;
			for(int j=0 ; j<shap.getBlock().length ; j++){
				Block sBlock = shap.getBlock(j);
				if(map[sBlock.getY()][sBlock.getX()]!=null){
					up = true;
					break;
				}
			}
			if(up){
				controller.moveDown(-1);
			}
		}
		
		
		
		
		this.showGhost();
		this.repaint();
		synchronized (this) {
			stop = false;
			this.notify();
		}
	}
	
	public void isMaster(boolean isMaster) {
		if(isMaster) {
			this.btnStart.setEnabled(true);
		} else {
			this.btnStart.setEnabled(false);
		}
		
		this.repaint();
		synchronized (this) {
			stop = false;
			this.notify();
		}
		
		
	}
	
	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			messageArea.requestFocus();
		}
		if(!isPlay) return;
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			controller.moveLeft();
			controllerGhost.moveLeft();
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			controller.moveRight();
			controllerGhost.moveRight();
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			controller.moveDown();
		}else if(e.getKeyCode() == KeyEvent.VK_UP){
			controller.nextRotationLeft();
			controllerGhost.nextRotationLeft();
		}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
			controller.moveQuickDown(shap.getPosY(), true);
			this.fixingTetrisBlock();
		}else if(e.getKeyCode() == KeyEvent.VK_1){
			usingItem();
		}else if(e.getKeyCode() == KeyEvent.VK_2){
			System.out.println("2!!!");
			client.usingItem(2, itemList.get(0).getItemNum());
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}else if(e.getKeyCode() == KeyEvent.VK_3){
			System.out.println("3!!!");
			client.usingItem(3, itemList.get(0).getItemNum());
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}else if(e.getKeyCode() == KeyEvent.VK_4){
			System.out.println("4!!!");
			client.usingItem(4, itemList.get(0).getItemNum());
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}else if(e.getKeyCode() == KeyEvent.VK_5){
			System.out.println("5!!!");
			client.usingItem(5, itemList.get(0).getItemNum());
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}else if(e.getKeyCode() == KeyEvent.VK_6){
			System.out.println("6!!!");
			client.usingItem(6, itemList.get(0).getItemNum());
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}
		this.showGhost();
		this.repaint();
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		this.requestFocus();
	}
	public void mouseReleased(MouseEvent e) {}
	
	
	public void usingItem(){
		if(!itemList.isEmpty()){
			if(itemList.get(0).getItemNum()==1){//clear����
				for(int i=0;i<maxY;i++)
					removeBlockLine(maxY-1);
			}
			else if(itemList.get(0).getItemNum()==2){//plus1����
				addBlockLine(1);
			}
			else if(itemList.get(0).getItemNum()==3){//minus1����
				removeBlockLine(maxY-1);
			}
			itemList.remove(0);
			for(int i=0;i<9;i++){
				itemListLabel[i].setIcon(itemListLabel[i+1].getIcon());
			}
			itemListLabel[9].setIcon(emptyIcon);
		}
	}
	
	public void usingItem(int itemNum){
		if(itemNum==1){//clear����
			for(int i=0;i<maxY;i++)
				removeBlockLine(maxY-1);
		}
		else if(itemNum==2){//plus1����
			addBlockLine(1);
		}
		else if(itemNum==3){//minus1����
			removeBlockLine(maxY-1);
		}
	}
	

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnStart){
			if(client!=null){
				client.gameStart((int)comboSpeed.getSelectedItem());
			}else{
				this.gameStart((int)comboSpeed.getSelectedItem());
			}
		}else if(e.getSource() == btnExit){
			if(client!=null ){
				if(tetris.isNetwork()){
					client.closeNetwork(tetris.isServer());
				}
			}else{
				System.exit(0);
			}
			
		}
	}
	
	public void convertMapIB(int[][][] map, int index){
		//int[][][] -> Block[][]���·� ��ȯ
		for(int i=0;i<maxY;i++){
			for(int j=0;j<maxX;j++){
				maps[index-1][i][j]=null;
				if(map[i][j][0]==1){
					maps[index-1][i][j]=new Block(map[i][j][1],map[i][j][2],Color.GREEN,Color.GRAY);
					maps[index-1][i][j].setPosGridXY(map[i][j][3], map[i][j][4]);
				}
			}
		}
	}	
	
	public int[][][] convertMapBI(Block[][] map) {
		//Block[][] -> int[][][]���·� ��ȯ
		int[][][] block= new int[maxY][maxX][5];
		for(int i=0;i<maxY;i++){
			for(int j=0;j<maxX;j++){
				if(map[i][j]!=null){
					block[i][j][0]=1;
					block[i][j][1]=map[i][j].getFixGridX();
					block[i][j][2]=map[i][j].getFixGridY();
					block[i][j][3]=map[i][j].getPosGridX();
					block[i][j][4]=map[i][j].getPosGridY();
				}
			}
		}
		
		return block;
	}
	
	public boolean isPlay(){return isPlay;}
	public void setPlay(boolean isPlay){this.isPlay = isPlay;}
	public JButton getBtnStart() {return btnStart;}
	public JButton getBtnExit() {return btnExit;}
	public void setClient(GameClient client) {this.client = client;}
	public void printSystemMessage(String msg){systemMsg.printMessage(msg);}
	public void printMessage(String msg){messageArea.printMessage(msg);}
	public GameClient getClient(){return client;}
	public void changeSpeed(Integer speed) {comboSpeed.setSelectedItem(speed);}
	public void clearMessage() {
		messageArea.clearMessage();
		systemMsg.clearMessage();
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

}

