package com.tetris.window;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.tetris.network.GameClient;

public class Tetris extends JFrame {
	private static final long serialVersionUID = 1L;
	private GameClient client;
	private TetrisBoard board = new TetrisBoard(this,client);
	private JMenuItem itemServerStart = new JMenuItem("서버로 접속하기");
	private JMenuItem itemClientStart = new JMenuItem("클라이언트로 접속하기");
	
	private boolean isNetwork;
	private boolean isServer;

	

	public Tetris() {
		String ip=null;
		int port=0;
		String nickName=null;
		
		//"211.212.62.252"
		while(true){
			try {
				ip = JOptionPane.showInputDialog("IP를 입력해주세요.",InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			String sp = JOptionPane.showInputDialog("port번호를 입력해주세요","9500");
			if(sp!=null && !sp.equals(""))port = Integer.parseInt(sp);
			nickName = JOptionPane.showInputDialog("닉네임을 입력해주세요","이름없음");
			if(port==0)
				JOptionPane.showMessageDialog(null, "IP와 port번호를 입력해주세요","Dialog Message", JOptionPane.ERROR_MESSAGE);
			else break;
		}

	
		if(ip!=null){
			client = new GameClient(this,ip,port,nickName);
			if(client.start()){
				itemServerStart.setEnabled(false);
				itemClientStart.setEnabled(false);
				board.setClient(client);
				board.startNetworking(ip, port, nickName);
				isNetwork = true;
				board.getBtnStart().setEnabled(true);
			}
		}
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Thread.currentThread().interrupt();
		this.getContentPane().add(board);
		
		this.setResizable(false);
		this.pack();
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((size.width-this.getWidth())/2,(size.height-this.getHeight())/2);
		this.setVisible(true);
		
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if(client!=null ){
					
					if(isNetwork){
						client.closeNetwork(isServer);
					}
				}else{
					System.exit(0);
				}
				
			}
			
		});
		
	}


	public void closeNetwork(){
		isNetwork = false;
		client = null;
		itemServerStart.setEnabled(true);
		itemClientStart.setEnabled(true);
		board.setPlay(false);
		board.setClient(null);
	}

	public JMenuItem getItemServerStart() {return itemServerStart;}
	public JMenuItem getItemClientStart() {return itemClientStart;}
	public TetrisBoard getBoard(){return board;}
	public void gameStart(int speed){board.gameStart(speed);}
	public boolean isNetwork() {return isNetwork;}
	public void setNetwork(boolean isNetwork) {this.isNetwork = isNetwork;}
	public void printSystemMessage(String msg){board.printSystemMessage(msg);}
	public void printMessage(String msg){board.printMessage(msg);}
	public boolean isServer() {return isServer;}
	public void setServer(boolean isServer) {this.isServer = isServer;}

	public void changeSpeed(Integer speed) {board.changeSpeed(speed);}
}
