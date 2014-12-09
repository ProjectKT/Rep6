package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import providers.FileManager;
import providers.OurSuffixArray;
import providers.Rule;
import system.RuleBase;
<<<<<<< HEAD

=======
>>>>>>> branch 'master' of https://github.com/ProjectKT/Rep6.git
import components.HighlightedTextPane;
import components.RuleTextPane;

public class OurGUI extends JFrame implements ActionListener , ComponentListener, ChangeListener{

	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
	
	// --- ロジックのメンバ ---
	String data="data";
	String text;
	OurSuffixArray osa = new OurSuffixArray();
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();

	RuleBase rb;
	
	
	// --- ビューのメンバ ---
	JMenuItem mntmOpenRuleFile;
	JMenuItem mntmOpenWMFile;
	JMenuItem mntmExit;
    RuleTextPane ruleTextPane;
    RuleTextPane ruleTextPane2;
	JScrollPane sp;
	JScrollPane sp2;
	JButton forward;
	JButton backward;
	JTextField tf;
	JTextField ansField;
	JRadioButton ruleEdit;
	JRadioButton wmEdit;

    
    /**
     * RuleTextPane のコールバック
     */
    private RuleTextPane.Callbacks ruleTextPaneCallbacks = new RuleTextPane.Callbacks() {
		@Override
		public void onRuleRemoved(Rule rule) {
			osa.addSuffixRule(rule);
		}
		
		@Override
		public void onRuleCreated(Rule rule) {
			osa.deleteSuffixRule(rule.getName());
		}
		
		@Override
		public Iterator<String> getSuggestions(String input) {
			return osa.getAllSentences(input);
		}
	};
    
    
	// コンストラクタ
	public OurGUI() {
		initialize();
		
		loadData();
		setupSuffixArray();
		
		// 2014-12-08 h.m. loadRuleFile() に移行
//		for(int i =0; i<rules.size();i++ )
//	    	text = text + rules.get(i).toString2()+"\n";
//		
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	    DocumentBuilder builder = null;
//	    Document doc;
//		try {
//			builder = factory.newDocumentBuilder();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
//	    try {
//			doc = builder.parse(stream);
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	    ruleTextPane.setToolTipText(text);
		
		set();
		setVisible(true);
	}
	
	// 初期化
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10,10,500,800);
		setTitle("gui");
		
		/* --- MENU --- */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOpenRuleFile = new JMenuItem("Open Rule File");
		mntmOpenRuleFile.addActionListener(this);
		mnFile.add(mntmOpenRuleFile);

		mntmOpenWMFile = new JMenuItem("Open WM File");
		mntmOpenWMFile.addActionListener(this);
		mnFile.add(mntmOpenWMFile);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);
		
		/* --- content --- */
	}

	private void set(){
		

		JTabbedPane tabbedPane = new JTabbedPane();

		//------質問ページ-------
		JPanel tab1 = new JPanel();
		tabbedPane.addTab("質問",tab1);
		
		GridBagLayout gbl = new GridBagLayout();
		
		
		tf = new JTextField("",20);
		getContentPane().add(tf);
		tab1.add(tf);
		
		//前向き推論ボタン
		forward = new JButton("前向き");
		forward.addActionListener(this);
		getContentPane().add(forward);
		tab1.add(forward);

		//後ろ向き推論ボタン
		backward = new JButton("後向き");
		backward.addActionListener(this);
		getContentPane().add(backward);
		tab1.add(backward);
		
		//解の表示欄
		ansField = new JTextField("");
		getContentPane().add(ansField);
		tab1.add(ansField);
		

		
		
		//--------編集ページ-------
		ruleEdit = new JRadioButton("ルール",true);
		ruleEdit.addChangeListener(this);
		wmEdit = new JRadioButton("WM",false);
		wmEdit.addChangeListener(this);
		JPanel tab2 = new JPanel();
		JPanel radioPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		group.add(ruleEdit);
		group.add(wmEdit);
		radioPanel.add(ruleEdit);
		radioPanel.add(wmEdit);
		tab2.add(radioPanel);
		ruleTextPane = new RuleTextPane();
		ruleTextPane.setCallbacks(ruleTextPaneCallbacks);
		sp = new JScrollPane(ruleTextPane);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		addComponentListener(this);
		getContentPane().add(sp, BorderLayout.CENTER);
		ruleTextPane2 = new RuleTextPane();
		ruleTextPane2.setCallbacks(ruleTextPaneCallbacks);
		sp2 = new JScrollPane(ruleTextPane2);
		sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//sp2.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		getContentPane().add(sp2, BorderLayout.CENTER);
		tab2.add(sp);
		sp.setVisible(true);
		sp2.setVisible(false);
		tab2.add(sp2);
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
	
	/**
	 * ルールファイルを読み込み、設定する
	 * @param file
	 */
	private void loadRuleFile(File file) {
		try {
			String text = readFile(file);
			ruleTextPane.setText(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * WMファイルを読み込み、設定する
	 * @param file
	 */
	private void loadWMFile(File file) {
		// FIXME
		try {
			String text = readFile(file);
			ruleTextPane2.setText(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ファイルから文字列を読み込み、バッファ(String)を返す
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String readFile(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		try {
			String s;
			while ((s = reader.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return sb.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == mntmOpenRuleFile) {
			JFileChooser fileChooser = new JFileChooser();
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				loadRuleFile(file);
			}
		} else if (s == mntmOpenWMFile) {
			JFileChooser fileChooser = new JFileChooser();
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				loadWMFile(file);
			}
		} else if (s == mntmExit) {
			dispose();
		}
		//こっから変更
		else if(s == forward){
			rb = new RuleBase(rules,wm);
			rb.forwardChain();
		}else if(s == backward){
			rb = new RuleBase(rules,wm);
			ArrayList<String> temptf = new ArrayList<String>();
			String[] tf2 = tf.getText().split(",");
			for(String str:tf2){
				temptf.add(str);
			}
			rb.backwardChain(temptf);
		}
		
	}


	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("!!!");
		sp.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		setVisible(true);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		 if(ruleEdit.isSelected()) {
			 sp2.setVisible(false);
			 sp.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
			 sp.setVisible(true);
			 sp.getParent().revalidate();
		 }
        if(wmEdit.isSelected()) {
        	 sp.setVisible(false);
			 sp2.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
			 sp2.setVisible(true);
			 sp2.getParent().revalidate();
        }
        
	}
}

