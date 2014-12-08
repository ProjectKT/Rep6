package gui;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import providers.*;
import system.RuleBase;
import components.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class test extends JFrame implements ActionListener {
	OurSuffixArray osa = new OurSuffixArray();
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();
	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
	JTextArea area3;
	JTextArea area1;
	
  public static void main(String[] args){
    test frame = new test();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(10, 10, 600, 400);
    frame.setTitle("タイトル");
    frame.setVisible(true);
  }

  test(){
	  loadData();
		setupSuffixArray();
    JTabbedPane tabbedpane = new JTabbedPane();

    JPanel tabPanel1 = new JPanel();
    tabPanel1.add(new JButton("更新"));

    JPanel tabPanel2 = new JPanel();
    String text="";
    
	
    for(int i =0; i<rules.size();i++ ){
    	text = text + rules.get(i).toString2()+"\n";
    	System.out.println(text);
    }
    

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document doc;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
    try {
		doc = builder.parse(stream);
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    RuleTextPane ruleTextPane = new RuleTextPane();
    ruleTextPane.setToolTipText(text);

    area3 = new JTextArea();
    JPanel tabPanel3 = new JPanel();
    JButton b1 =new JButton("button2");

    JScrollPane scrollPane3 = new JScrollPane(area3);
	scrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane3.setPreferredSize(new Dimension(200, 100));
	getContentPane().add(scrollPane3, BorderLayout.CENTER);
    
    
    b1.addActionListener(this);
	getContentPane().add(b1);
    tabPanel3.add(b1);
    tabPanel3.add(scrollPane3);  
    
    
    
    tabbedpane.addTab("ルール", tabPanel1);
    tabbedpane.addTab("変更", ruleTextPane);
    tabbedpane.addTab("質問", tabPanel3);

    
    tabbedpane.setTabPlacement(JTabbedPane.TOP);

    getContentPane().add(tabbedpane, BorderLayout.CENTER);
    
  }
  
	private void loadData() {
		fm = new FileManager();
		// ファイルから読み込む
		for (String filename : RULE_FILES) {
				rules.addAll(fm.loadRules(filename));
		}
		for (String filename : WM_FILES) {
				wm.addAll(fm.loadWm(filename));
		}
	}
	
	private void setupSuffixArray(){
		for(int i = 0; i < rules.size(); i++){
			osa.addSuffixRule(rules.get(i));
		}
		for(int i2 = 0; i2 < wm.size(); i2++){
			osa.addSuffixWm(wm.get(i2));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("OK");
		RuleBase rb = new RuleBase();
		rb.forwardChain();
		area3.setText(rb.get_answer());
	}


}
