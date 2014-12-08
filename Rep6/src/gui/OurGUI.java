package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;

public class OurGUI extends JFrame implements ActionListener{

	String data="data";
	
	// コンストラクタ
	public OurGUI() {
		initialize();
		set();
		setVisible(true);
	}
	
	// 初期化
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10,10,1000,800);
		setTitle("gui");
	}

	private void set(){
		

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel tab1 = new JPanel();//質問ページ

		tabbedPane.addTab("質問",tab1);
//		getContentPane().setLayout(new GridLayout(2,2));
		JTextField tf = new JTextField("",20);
		getContentPane().add(tf);
		tab1.add(tf);
		
		JButton b1 = new JButton("OK");
		b1.addActionListener(this);
		getContentPane().add(b1);
		tab1.add(b1);

		JButton b2 = new JButton("OK");
		b2.addActionListener(this);
		getContentPane().add(b2);
		tab1.add(b2);
		
		
		JPanel tab2 = new JPanel();//編集ページ
		tabbedPane.addTab("編集",tab2);

		getContentPane().add(tabbedPane,BorderLayout.CENTER);

		
	}
	
	
	
	public static void main(String[] args) {
		//
		
		OurGUI gui = new OurGUI();
		gui.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

