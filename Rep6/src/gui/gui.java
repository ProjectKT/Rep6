package gui;

import java.awt.*;
import javax.swing.*;

public class gui extends JFrame{

	// コンストラクタ
	public gui() {
		initialize();
		JTabbedPane
		setVisible(true);
	}
	
	// 初期化
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10,10,1000,800);
		setTitle("gui");
	}
	
	JTabbedPaneTest2(){
	    JTabbedPane tabbedpane = new JTabbedPane();

	    JPanel tabPanel1 = new JPanel();
	    tabPanel1.add(new JButton("button1"));

	    JPanel tabPanel2 = new JPanel();
	    tabPanel2.add(new JLabel("Name:"));
	    tabPanel2.add(new JTextField("", 10));

	    tabbedpane.addTab("tab1", tabPanel1);
	    tabbedpane.addTab("tab2", tabPanel2);

	    getContentPane().add(tabbedpane, BorderLayout.CENTER);
	  }
	
	public static void main(String[] args) {
		//
		
		gui gui = new gui();
		gui.setVisible(true);
	}
}
