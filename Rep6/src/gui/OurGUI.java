package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import components.RuleTextPane;
import providers.FileManager;
import providers.OurSuffixArray;
import providers.Rule;

import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class OurGUI extends JFrame implements ActionListener{

	String data="data";
	String text;
	OurSuffixArray osa = new OurSuffixArray();
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();
	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
    RuleTextPane ruleTextPane = new RuleTextPane();
	// コンストラクタ
	public OurGUI() {
		initialize();
		
		loadData();
		setupSuffixArray();
		
		for(int i =0; i<rules.size();i++ )
	    	text = text + rules.get(i).toString2()+"\n";
		
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


	    ruleTextPane.setToolTipText(text);
		
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
		JScrollPane sp = new JScrollPane(ruleTextPane);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setPreferredSize(new Dimension(900, 700));
		getContentPane().add(sp, BorderLayout.CENTER);
		tab2.add(sp);
		tabbedPane.addTab("編集",tab2);

		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		
	}
	
	
	
	public static void main(String[] args) {
		//
		
		OurGUI gui = new OurGUI();
		gui.setVisible(true);
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
		// TODO Auto-generated method stub
		
	}
}

