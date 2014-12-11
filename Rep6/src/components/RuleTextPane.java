package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import providers.Rule;
import system.RuleCompiler;
import system.RuleCompiler.Result;
import system.RuleCompiler.RuleContainer;

public class RuleTextPane extends HighlightedTextPane implements HighlightedTextPane.TokenHighlighter, DocumentListener {
	
	private static final long COMPILE_DELAY_MILLISECONDS = 500;
	
	/**
	 * RuleTextPane で用いる AttributeSet
	 */
	interface RuleAttributeSet {
		/** デフォルト */
		class DefaultAttributeSet extends SimpleAttributeSet {
			public DefaultAttributeSet() {
				StyleConstants.setFontSize(this, 12);
			}
		}
		
		/** ルールのハイライト */
		SimpleAttributeSet rule = new DefaultAttributeSet() {{
			StyleConstants.setBold(this, true);
			StyleConstants.setForeground(this, Color.magenta);
		}};
		/** if 部のハイライト */
		SimpleAttributeSet ifClause = new DefaultAttributeSet() {{
			StyleConstants.setBold(this, true);
			StyleConstants.setForeground(this, Color.orange);
		}};
		/** then 部のハイライト */
		SimpleAttributeSet thenClause = new DefaultAttributeSet() {{
			StyleConstants.setBold(this, true);
			StyleConstants.setForeground(this, Color.pink);
		}};
	}
	
	/** ハイライトする文字列と AttributeSet の対応 */
	private static final HashMap<String,AttributeSet> attributeSetMap = new HashMap<String,AttributeSet>() {{
		// 小文字の token と対応する AttributeSet を入れる
		put("rule", RuleAttributeSet.rule);
		put("if", RuleAttributeSet.ifClause);
		put("then", RuleAttributeSet.thenClause);
	}};

	/** コールバックのインタフェース定義 */
	public interface Callbacks {
		/** ルールが生成されたときに呼ばれる */
		public void onRuleCreated(Rule rule);
		/** ルールが削除された時に呼ばれる */
		public void onRuleRemoved(Rule rule);
		/** SuffixArray から単語の Suggestion を取得するときに呼ばれる */
		public Iterator<String> getWordSuggestions(String input);
		/** SuffixArray から文の Suggestion を取得するときに呼ばれる */
		public Iterator<String> getSentenceSuggestions(String input);
	}
	
	protected static final Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onRuleCreated(Rule rule) { }
		@Override
		public void onRuleRemoved(Rule rule) { }
		@Override
		public Iterator<String> getWordSuggestions(String input) { return null; }
		@Override
		public Iterator<String> getSentenceSuggestions(String input) { return null; }
	};
	
	/** コールバック */
	private Callbacks callbacks = sDummyCallbacks;
	
	/** SyntaxChecker */
	private RuleCompiler compiler = new RuleCompiler();
	protected DelayQueue<RuleCompileRequest> ruleCompileQueue = new DelayQueue<RuleCompileRequest>();
	private Runnable ruleCompileRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					consume(ruleCompileQueue.take());
				}
			} catch (InterruptedException e) { System.out.println(e); }
		}
		private void consume(RuleCompileRequest request) {
			Result result = compiler.compile(getText()); System.out.println(result);
			synchronized (rulesLock) {
				if (result.succeeded) {
					// FIXME ここでルールの差分を求め、callbacks.onRuleAdded(), callbacks.onRuleRemoved() を呼ぶ
					for (RuleCompiler.RuleContainer r : rules) {
						callbacks.onRuleRemoved(r.rule);
					}
					rules = result.rules;
					for (RuleCompiler.RuleContainer r : rules) {
						callbacks.onRuleCreated(r.rule);
					}
				}
			}
		}
	};
	private Thread ruleCompileThread = null;
	
	/** ルールとその書かれている位置を保管するリスト */
	private ArrayList<RuleCompiler.RuleContainer> rules = new ArrayList<RuleCompiler.RuleContainer>();
	private final Object rulesLock = new Object();
	private Comparator<RuleContainer> ruleComparator = new Comparator<RuleCompiler.RuleContainer>() {
		@Override
		public int compare(RuleContainer o1, RuleContainer o2) {
			if (	o1.offset < o2.offset && o2.count < o1.count ||
					o2.offset < o1.offset && o1.count < o2.count		) {
				return 0;
			} else {
				return (o2.offset - o1.offset);
			}
		}
	};
	
	/** SuggestionsFrame */
	private SuggestionsFrame suggestionsFrame = new SuggestionsFrame();
	
	/** キーリスナー */
	private KeyAdapter keyAdapter = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				if (e.isControlDown() || e.isMetaDown()) {
					e.consume();
					showSuggestions(getLastEditedLine());
				}
				break;
			}
		}
	};
	
	public RuleTextPane() {
		super();
		initialize();
	}
	
	public RuleTextPane(String text) {
		super(text);
		initialize();
	}
	
	private void initialize() {
		setTokenHighlighter(this);
		getDocument().addDocumentListener(this);
		setUI(new RuleTextPane.UI());
	}
	
	public void startAutoRuleCompiling(JFrame frame) {
		if (ruleCompileThread != null) {
			throw new IllegalThreadStateException("already running");
		}
		
		ruleCompileThread = new Thread(ruleCompileRunnable);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ruleCompileThread.interrupt();
				try {
					System.out.println("joining ruleCompileThread");
					ruleCompileThread.join();
					System.out.println("joined ruleCompileThread");
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} finally {
					ruleCompileThread = null;
				}
			}
		});
		ruleCompileThread.start();
	}

	/**
	 * コールバックを設定する
	 * @param callbacks コールバックインターフェースの実装
	 */
	public void setCallbacks(Callbacks callbacks) {
		this.callbacks = (callbacks == null) ? sDummyCallbacks : callbacks;
	}
	
	/**
	 * トークンに対する AttributeSet を返す
	 */
	@Override
	public AttributeSet getAttributeSetForToken(String token) {
		return attributeSetMap.get(token.toLowerCase());
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		RuleCompileRequest request = ruleCompileQueue.peek();
		if (request != null /* && request.offset + request.count + 1 == e.getOffset() */) {
			request.resetDelay(COMPILE_DELAY_MILLISECONDS);
			request.count += e.getLength();
		} else {
			request = new RuleCompileRequest(e.getOffset(), COMPILE_DELAY_MILLISECONDS);
			ruleCompileQueue.add(request);
		}
		if (e.getLength() == 1) {
			showSuggestions(getLastEditedLine());
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		RuleCompileRequest request = ruleCompileQueue.peek();
		if (request != null /* && request.offset - request.count - 1 == e.getOffset() */) {
			request.resetDelay(COMPILE_DELAY_MILLISECONDS);
			request.count += e.getLength();
		} else {
			request = new RuleCompileRequest(e.getOffset(), COMPILE_DELAY_MILLISECONDS);
			ruleCompileQueue.add(request);
		}
		if (e.getLength() == 1) {
			showSuggestions(getLastEditedLine());
		}
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) { }
	
	public void showSuggestions(String token) {
		Iterator<String> it = callbacks.getWordSuggestions(token);
		Iterator<String> it2 = callbacks.getSentenceSuggestions(token);
		if (it == null) {
			suggestionsFrame.setVisible(false);
		} else {
			suggestionsFrame.updateSuggestions(token, it, it2);
			if (!suggestionsFrame.isVisible()) {
				try {
					Point p = getLocationOnScreen();
					Rectangle rect = modelToView(getCaretPosition());
					final int x = p.x + rect.x;
					final int y = p.y + rect.y + getFont().getSize() + 2; // FIXME
					final int w = 500; // FIXME
					final int h = 200; // FIXME
					suggestionsFrame.setBounds(x, y, w, h);
//					suggestionsFrame.setLocation(rect.x, rect.y);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				suggestionsFrame.setVisible(true);
//				SwingUtilities.getWindowAncestor(this).requestFocus();
			}
		}
	}
	
	/**
	 * TextPane の offset 文字目にあるルールの index を BinarySearch により見つけて返す
	 * @param offset 文字目。e.g. キャレットの位置
	 * @return
	 */
	private int findRuleAt(int offset) {
		RuleContainer key = new RuleCompiler.RuleContainer(offset, 0, null);
		synchronized (rulesLock) {
			return Collections.binarySearch(rules, key, ruleComparator);
		}
	}

	/**
	 * カスタマイズされた UI
	 */
	protected class UI extends HighlightedTextPane.UI {
	
	}
	
	private class RuleCompileRequest extends RuleCompiler.RuleContainer implements Delayed {
		private long expireTime;
		
		public RuleCompileRequest(int offset, long delay) {
			super(offset, 0, null);
			expireTime = System.currentTimeMillis() + delay;
		}

		public synchronized boolean resetDelay(long delay) {
			if (0 < getDelay(TimeUnit.MILLISECONDS)) {
				expireTime = System.currentTimeMillis() + delay;
				return true;
			}
			return false;
		}

		@Override
		public synchronized long getDelay(TimeUnit unit) {
			return unit.convert((expireTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed delayed) {
			if (delayed == this) {
				return 0;
			}

			long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
			return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
		}
	}
	
	protected class SuggestionsFrame extends JFrame implements KeyListener, ComponentListener {
		private List wordlist;
		private List sentencelist;
		private JPanel panel = new JPanel();
		
		public SuggestionsFrame() {
			initialize();
		}
		
		private void initialize() {
			setUndecorated(true);
			setAlwaysOnTop(true);
			setResizable(true);
			addKeyListener(this);
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
				break;
			case KeyEvent.VK_DOWN:
				wordlist.select(wordlist.getSelectedIndex()+1);
				break;
			case KeyEvent.VK_ESCAPE:
				setVisible(false);
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
			RuleTextPane.this.addKeyListener(this);
		}
		@Override
		public void componentHidden(ComponentEvent e) {
			RuleTextPane.this.removeKeyListener(this);
		}

	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		
		f.setBounds(100, 100, 400, 600);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		

		RuleTextPane ruleTextPane = new RuleTextPane();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(ruleTextPane);
		
		f.add(scrollPane);
		f.setVisible(true);
		
		f.pack();
		
		ruleTextPane.startAutoRuleCompiling(f);
	}
}
