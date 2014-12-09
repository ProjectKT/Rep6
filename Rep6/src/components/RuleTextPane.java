package components;

import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import providers.Rule;
import system.RuleCompiler;
import system.RuleCompiler.RuleContainer;

public class RuleTextPane extends HighlightedTextPane implements HighlightedTextPane.TokenHighlighter, DocumentListener {
	
	/** コールバックのインタフェース定義 */
	public interface Callbacks {
		/** ルールが生成されたときに呼ばれる */
		public void onRuleCreated(Rule rule);
		/** ルールが削除された時に呼ばれる */
		public void onRuleRemoved(Rule rule);
		/** SuffixArray から Suggestion を取得するときに呼ばれる */
		public Iterator<String> getSuggestions(String input);
	}
	
	protected static final Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onRuleCreated(Rule rule) { }
		@Override
		public void onRuleRemoved(Rule rule) { }
		@Override
		public Iterator<String> getSuggestions(String input) { return null; }
	};
	
	/** コールバック */
	private Callbacks callbacks = sDummyCallbacks;
	
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
	
	/** SyntaxChecker */
	private RuleCompiler compiler = new RuleCompiler();
	private Thread ruleCompileThread = new Thread(new Runnable() {
		@Override
		public void run() {
			
		}
	});
	
	/** ルールとその書かれている位置を保管するリスト */
	private ArrayList<RuleCompiler.RuleContainer> rules = new ArrayList<RuleCompiler.RuleContainer>();
	private Comparator<RuleContainer> ruleComparator = new Comparator<RuleCompiler.RuleContainer>() {
		@Override
		public int compare(RuleContainer o1, RuleContainer o2) {
			if (o1.count == 0 && (o2.offset < o1.offset && o1.offset < o2.offset+o2.count)) {
				return 0;
			} else if (o2.count == 0 && (o1.offset < o2.offset && o2.offset < o1.offset+o1.count)) {
				return 0;
			} else {
				return (o2.offset - o1.offset);
			}
		}
	};
	
	private ComponentListener componentListener = new ComponentListener() {
		
		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void componentResized(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
			
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
		addComponentListener(componentListener);
		setUI(new RuleTextPane.UI());
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
//		traverseRules(0); // FIXME
//		updateSuggestions();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
//		traverseRules(0); // FIXME
//		updateSuggestions();
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
//		System.out.println(compiler.compile(getText()));
	}
	
	/**
	 * TextPane の offset 文字目にあるルールの index を BinarySearch により見つけて返す
	 * @param offset 文字目。e.g. キャレットの位置
	 * @return
	 */
	private int findRuleAt(int offset) {
		RuleContainer key = new RuleCompiler.RuleContainer(offset, 0, null);
		return Collections.binarySearch(rules, key, ruleComparator);
	}
	
	/**
	 * ルールのかたまりを見つけてハイライト、SuffixArray の更新を行う
	 * @param startPos
	 */
	private void traverseRules(int startPos) {
		// TODO implement this method
	}

	/**
	 * カスタマイズされた UI
	 */
	protected class UI extends HighlightedTextPane.UI {
	
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
	}
}
