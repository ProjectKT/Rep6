package components;

import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
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
		/** ルールが編集された時(入力中)に呼ばれる */
		public void onRuleModified(String line);
	}
	
	protected static final Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onRuleCreated(Rule rule) { }
		@Override
		public void onRuleRemoved(Rule rule) { }
		@Override
		public void onRuleModified(String line) { }
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
	 * 最後に編集したトークンを置き換える
	 * @param str
	 * @throws BadLocationException 
	 */
	public void replaceLastEditedToken(String str) throws BadLocationException {
		int len = getLastEditedToken().length();
		getDocument().remove(getCaretPosition()-len, len);
		getDocument().insertString(getCaretPosition(), str, null);
	}
	
	/**
	 * 最後に編集した行を置き換える
	 * @param str
	 * @throws BadLocationException 
	 */
	public void replaceLastEditedLine(String str) throws BadLocationException {
		int len = getLastEditedLine().length();
		getDocument().remove(getCaretPosition()-len, len);
		getDocument().insertString(getCaretPosition(), str, null);
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
			callbacks.onRuleModified(getLastEditedLine());
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
			callbacks.onRuleModified(getLastEditedLine());
		}
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) { }
	
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
