package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.StyleContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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


public class OurTest extends JFrame implements ActionListener {
	OurSuffixArray osa = new OurSuffixArray();
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();
	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
	JTextArea area3;
	
  public static void main(String[] args){
    OurTest frame = new OurTest();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(10, 10, 300, 200);
    frame.setTitle("タイトル");
    frame.setVisible(true);
  }

  OurTest(){
	  loadData();
		setupSuffixArray();
    JTabbedPane tabbedpane = new JTabbedPane();

    JPanel tabPanel1 = new JPanel();
    tabPanel1.add(new JButton("button1"));

    JPanel tabPanel2 = new JPanel();
    String text="";
    
	
    for(int i =0; i<rules.size();i++ ){
    	text = text + rules.get(i).toString2()+"\n";
    	System.out.println(text);
    }
    

    StyleContext sc = new StyleContext();
    DefaultStyledDocument doc = new DefaultStyledDocument(sc);
    try {
		doc.insertString(0, text, sc.getStyle(StyleContext.DEFAULT_STYLE));
	} catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    RuleTextPane ruleTextPane = new RuleTextPane();
    ruleTextPane.setDocument(doc);

    area3 = new JTextArea();
    JPanel tabPanel3 = new JPanel();
    JButton b1 =new JButton("button2");

    JScrollPane scrollPane = new JScrollPane(area3);
	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setPreferredSize(new Dimension(200, 100));
	getContentPane().add(scrollPane, BorderLayout.CENTER);
    
    
    b1.addActionListener(this);
	getContentPane().add(b1);
    tabPanel3.add(b1);
    tabPanel3.add(scrollPane);  

    tabbedpane.addTab("main", tabPanel1);
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
