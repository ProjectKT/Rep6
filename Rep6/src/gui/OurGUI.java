package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import providers.FileManager;
import providers.OurSuffixArray;
import providers.Rule;
import system.RuleBase;
import components.DataFilter;
import components.HighlightedTextPane;
import components.RuleTextPane;

public class OurGUI extends JFrame implements ActionListener , ComponentListener, ChangeListener{

	private String currentRuleFileName = "OurRule.data";
	private String currentWmFileName = "OurWm.data";
	
	// --- ロジックのメンバ ---
	String data="data";
	String text;
	OurSuffixArray osa;
	static FileManager fm;
	ArrayList<Rule> rules;
	ArrayList<String> wm;
	RuleBase rb;
	File currentDirectory = null;
	
	
	// --- ビューのメンバ ---
	JMenuItem mntmOpenRuleFile;
	JMenuItem mntmOpenWMFile;
	JMenuItem mntmSaveRuleFile;
	JMenuItem mntmSaveWMFile;
	JMenuItem mntmSaveAsRuleFile;
	JMenuItem mntmSaveAsWMFile;
	JMenuItem mntmExit;
    RuleTextPane ruleTextPane;
    HighlightedTextPane wmTextPane;
	JButton forward;
	JButton backward;
	JTextField tf;
	JTextArea aa;
	JRadioButton ruleEdit;
	JRadioButton wmEdit;
	JScrollPane sp1;
	JScrollPane sp2;
	JScrollPane sp3;
	JPanel tab1 = new JPanel(new BorderLayout());
	JPanel tab2 = new JPanel(new BorderLayout());
	JPanel radioPanel = new JPanel();
	JPanel p1;
	JPanel p2;
	RuleSuggestionsFrame suggestionsFrame = new RuleSuggestionsFrame();

    
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
		public void onRuleModified(String line) {
			showSuggestions(line);
		}
		
		private void showSuggestions(String input) {
			Iterator<String> it = getWordSuggestions(input);
			Iterator<String> it2 = getSentenceSuggestions(input);
			if (it == null) {
				suggestionsFrame.setVisible(false);
			} else {
				suggestionsFrame.updateSuggestions(input, it, it2);
				Point p = ruleTextPane.getLocationOnScreen();
				try {
					Rectangle rect = ruleTextPane.modelToView(ruleTextPane.getCaretPosition());
					if (!suggestionsFrame.isVisible()) {
						final int x = p.x + rect.x;
						final int y = p.y + rect.y + getFont().getSize() + 2; // FIXME
						final int w = ruleTextPane.getWidth();
						final int h = ruleTextPane.getHeight();
						suggestionsFrame.setBounds(x, y, w, h);
						suggestionsFrame.setVisible(true);
						toFront();
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		private Iterator<String> getWordSuggestions(String input) {
			System.out.println(input);
			String[] words = input.split(" ");
			return osa.getWords(words[words.length-1]);
		}
		
		private Iterator<String> getSentenceSuggestions(String input) {
			System.out.println(input);
			return osa.getCorrectSentencesHard(input);
		}
	};
    
    
	// コンストラクタ
	public OurGUI() {
		initialize();
		
		loadData();
		setupSuffixArray();
		
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
		
		mnFile.addSeparator();
		
		mntmSaveRuleFile = new JMenuItem("Save Rule File");
		mntmSaveRuleFile.addActionListener(this);
		mnFile.add(mntmSaveRuleFile);

		mntmSaveWMFile = new JMenuItem("Save WM File");
		mntmSaveWMFile.addActionListener(this);
		mnFile.add(mntmSaveWMFile);
		
		mnFile.addSeparator();
		
		mntmSaveAsRuleFile = new JMenuItem("Save As Rule File");
		mntmSaveAsRuleFile.addActionListener(this);
		mnFile.add(mntmSaveAsRuleFile);

		mntmSaveAsWMFile = new JMenuItem("Save As WM File");
		mntmSaveAsWMFile.addActionListener(this);
		mnFile.add(mntmSaveAsWMFile);
		
		mnFile.addSeparator();
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);
		
		/* --- content --- */
	}

    GridBagLayout gbl = new GridBagLayout();

    void addPanel(JPanel p, int x, int y, int w, int h) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        gbl.setConstraints(p, gbc);
        tab1.add(p);
    }
    
	private void set(){

		addComponentListener(this);

		JTabbedPane tabbedPane = new JTabbedPane();

		//------質問ページ-------
		tabbedPane.addTab("質問",tab1);		
		
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
		aa = new JTextArea();
		getContentPane().add(aa);
		tab1.add(aa);
		
		p1 = new JPanel();
		p1.setLayout(new FlowLayout());
		p1.add(tf);
		p1.add(forward);
		p1.add(backward);
		
		sp3 = new JScrollPane(aa);
		sp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp3.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		
		p2 = new JPanel(new FlowLayout());
		p2.add(sp3);
		
		tab1.add(p1,BorderLayout.NORTH);
		tab1.add(p2,BorderLayout.CENTER);
		
		//tab1.setLayout(gbl);
		//addPanel(p1,0,0,1,1);
		//addPanel(p2,0,1,1,1);
		

		
		//--------編集ページ-------
		tabbedPane.addTab("編集",tab2);
		
		ruleEdit = new JRadioButton("ルール",true);
		ruleEdit.addChangeListener(this);
		radioPanel.add(ruleEdit);

		wmEdit = new JRadioButton("WM",false);
		wmEdit.addChangeListener(this);
		radioPanel.add(wmEdit);

		ButtonGroup group = new ButtonGroup();
		group.add(ruleEdit);
		group.add(wmEdit);
		
		tab2.add(radioPanel, BorderLayout.NORTH);
		
		ruleTextPane = new RuleTextPane();
		ruleTextPane.setCallbacks(ruleTextPaneCallbacks);
		ruleTextPane.startAutoRuleCompiling(this);
		
		sp1 = new JScrollPane(ruleTextPane);
		sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp1.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		
		wmTextPane = new RuleTextPane();
		
		sp2 = new JScrollPane(wmTextPane);
		sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp2.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		
		JPanel editorPanel = new JPanel(new FlowLayout());
		tab2.add(editorPanel, BorderLayout.CENTER);
		
		editorPanel.add(sp1);
		sp1.setVisible(true);
		editorPanel.add(sp2);
		sp2.setVisible(false);
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		pack();
		setupTextPane();
	}
	
	
	
	public static void main(String[] args) {
		//
		
		OurGUI gui = new OurGUI();
		gui.setVisible(true);
	}
	
	private void loadData() {
		fm = new FileManager();
		// ファイルから読み込む
		rules = new ArrayList<Rule>();
				rules.addAll(fm.loadRules(currentRuleFileName));
				wm = new ArrayList<String>();
				wm.addAll(fm.loadWm(currentWmFileName));
	}
	
	private void setupSuffixArray(){
		osa = new OurSuffixArray();
		for(int i = 0; i < rules.size(); i++){
			osa.addSuffixRule(rules.get(i));
		}
		for(int i2 = 0; i2 < wm.size(); i2++){
			osa.addSuffixWm(wm.get(i2));
		}
	}
	
	private void setupTextPane(){
		File file = new File(currentRuleFileName);
		loadRuleFile(file);
		
		file = new File(currentWmFileName);
		loadWMFile(file);
	}
	
	/**
	 * ルールファイルを読み込み、設定する
	 * @param file
	 */
	private void loadRuleFile(File file) {
		try {
			String text = readFile(file);
			currentDirectory = file.getParentFile();
			ruleTextPane.setText(text);
			
			//ここからバックアップの保存
			File backup = new File(file.getPath().substring(0,file.getPath().length()-5)+"_BackUp.data");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(backup)));
			pw.println(text);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * WMファイルを読み込み、設定する
	 * @param file
	 */
	private void loadWMFile(File file) {
		try {
			String text = readFile(file);
			currentDirectory = file.getParentFile();
			wmTextPane.setText(text);
			
			//ここからバックアップの保存
			File backup = new File(file.getPath().substring(0,file.getPath().length()-5)+"_BackUp.data");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(backup)));
			pw.println(text);
			pw.close();
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
	
	/**
	 * RuleTextPaneからルールをとってきて元のファイルに保存する
	 * @param file_name
	 */
	private void saveRuleFile(String filename) {
		try {
			//ここまで普通の保存
			String text = ruleTextPane.getText();
			File file = new File(filename);
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println(text);
			 pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * RuleTextPaneからルールをとってきて元のファイルに保存する
	 * @param file_name
	 */
	private void saveWmFile(String filename) {
		try {
			String text = wmTextPane.getText();
			File file = new File(filename);
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println(text);
			 pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == mntmOpenRuleFile) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new DataFilter());
			fileChooser.setCurrentDirectory(currentDirectory);
			fileChooser.setDialogTitle("OpenRuleFile");
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				loadRuleFile(file);
				currentRuleFileName = file.getName();
				loadData();
				setupSuffixArray();
			}
		} else if (s == mntmOpenWMFile) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new DataFilter());
			fileChooser.setCurrentDirectory(currentDirectory);
			fileChooser.setDialogTitle("OpenWmFile");
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				loadWMFile(file);
				currentWmFileName = file.getName();
				loadData();
				setupSuffixArray();
			}
		} else if (s == mntmExit) {
			dispose();
		} else if (s == forward) {
			rb = new RuleBase(rules, wm);
			rb.forwardChain();
			String ans="新しいルールが生成されました\n--------------------\n";
			for(String str:rb.getForwardAnswer()){
				ans += str + "\n";
			}
				aa.setText(ans);
			
		} else if (s == backward) {
			rb = new RuleBase(rules, wm);
			ArrayList<String> temptf = new ArrayList<String>();
			String[] tf2 = tf.getText().split(",");
			for (String str : tf2) {
				temptf.add(str);
			}
			rb.backwardChain(temptf);
			String ans="";
			for(String str:rb.getBackwardAnswer()){
				ans += str + "\n";
			}
				aa.setText(ans);
			
		} else if (s == mntmSaveRuleFile) {
			saveRuleFile(currentRuleFileName);
		} else if (s == mntmSaveWMFile) {
			saveWmFile(currentWmFileName);
		} else if (s == mntmSaveAsRuleFile) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new DataFilter());
			fileChooser.setCurrentDirectory(currentDirectory);
			fileChooser.setDialogTitle("SaveAsRuleFile");
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				currentDirectory = file.getParentFile();
				saveRuleFile(file.getName());
			}
		} else if (s == mntmSaveAsWMFile) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new DataFilter());
			fileChooser.setCurrentDirectory(currentDirectory);
			fileChooser.setDialogTitle("SaveAsWmRuleFile");
			int selected = fileChooser.showOpenDialog(this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				currentDirectory = file.getParentFile();
				saveRuleFile(file.getName());
			}
		}
	}


	@Override
	public void componentResized(ComponentEvent e) {
		System.out.println("!!!");
		sp1.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		sp2.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		sp3.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
		setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		 if(ruleEdit.isSelected()) {
			 sp2.setVisible(false);
			 sp1.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
			 sp1.setVisible(true);
			 sp1.getParent().revalidate();
		 }
        if(wmEdit.isSelected()) {
        	 sp1.setVisible(false);
			 sp2.setPreferredSize(new Dimension(getWidth()-50, getHeight()-150));
			 sp2.setVisible(true);
			 sp2.getParent().revalidate();
        }
	}

	@Override
	public void componentMoved(ComponentEvent e) { }
	@Override
	public void componentShown(ComponentEvent e) { }
	@Override
	public void componentHidden(ComponentEvent e) { }
	
	
	/**
	 * ルール編集時の Suggestions 表示フレーム
	 */
	protected class RuleSuggestionsFrame extends JFrame implements KeyListener, ComponentListener {
		private List wordlist;
		private List sentencelist;
		private JPanel panel = new JPanel();
		
		public RuleSuggestionsFrame() {
			initialize();
		}
		
		private void initialize() {
			setUndecorated(true);
			setAlwaysOnTop(true);
			setResizable(true);
			addKeyListener(this);
			addComponentListener(this);
			setLayout(new BorderLayout());
			wordlist = new List();
			wordlist.setFocusable(false);
			ScrollPane sp = new ScrollPane();
			sp.add(wordlist);
			add("West",sp);
			sentencelist = new List();
			sentencelist.setFocusable(false);
			ScrollPane sp2 = new ScrollPane();
			sp2.add(sentencelist);
			add("Center",sp2);
			setVisible(false);
			setPreferredSize(new Dimension(500, 200));
		}
		
		public void updateSuggestions(String token, Iterator<String> it,Iterator<String> it2) {
			String selected = wordlist.getSelectedItem();
			wordlist.removeAll();
			while (it.hasNext()) {
				String s = it.next();
				wordlist.add(s);
				if (selected != null && selected.equals(s)) {
					wordlist.select(wordlist.getItemCount()-1);
				}
			}
			if (wordlist.getItemCount() == 1) {
				wordlist.select(0);
			}
			
			String selected2 = sentencelist.getSelectedItem();
			sentencelist.removeAll();
			while (it2.hasNext()) {
				String s = it2.next();
				sentencelist.add(s);
				if (selected2 != null && selected2.equals(s)) {
					sentencelist.select(sentencelist.getItemCount()-1);
				}
			}
			if (sentencelist.getItemCount() == 1) {
				sentencelist.select(0);
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				wordlist.select(wordlist.getSelectedIndex()-1);
				e.consume();
				break;
			case KeyEvent.VK_DOWN:
				wordlist.select(wordlist.getSelectedIndex()+1);
				e.consume();
				break;
			case KeyEvent.VK_ESCAPE:
				setVisible(false);
				e.consume();
				break;
			}
		}
		@Override
		public void keyTyped(KeyEvent e) { }
		@Override
		public void keyReleased(KeyEvent e) { }
		@Override
		public void componentResized(ComponentEvent e) { }
		@Override
		public void componentMoved(ComponentEvent e) { }
		@Override
		public void componentShown(ComponentEvent e) {
			ruleTextPane.addKeyListener(this);
		}
		@Override
		public void componentHidden(ComponentEvent e) {
			ruleTextPane.removeKeyListener(this);
		}

	}
}

