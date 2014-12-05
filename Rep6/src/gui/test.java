package gui;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import SuffixArray.*;
import components.*;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;


public class test extends JFrame{
	OurSuffixArray osa = new OurSuffixArray();
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();
	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
	
  public static void main(String[] args){
    test frame = new test();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(10, 10, 300, 200);
    frame.setTitle("タイトル");
    frame.setVisible(true);
  }

  test(){
	  loadData();
		setupSuffixArray();
    JTabbedPane tabbedpane = new JTabbedPane();

    JPanel tabPanel1 = new JPanel();
    tabPanel1.add(new JButton("button1"));

    JPanel tabPanel2 = new JPanel();
    String text="";
    
	RuleTextPane ruleTextPane = new RuleTextPane();
    for(int i =0; i<rules.size();i++ ){
    	text = text + rules.get(i).toString()+"\n";
    	System.out.println(text);
    }
    

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
    Document doc = builder.parse(stream);

    

    JPanel tabPanel3 = new JPanel();
    tabPanel3.add(new JButton("button2"));

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
}
