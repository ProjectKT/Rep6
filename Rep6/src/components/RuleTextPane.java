package components;

import java.awt.Color;
import java.awt.ScrollPane;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import providers.Rule;
import system.RuleCompiler;

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
	

	public RuleTextPane() {
		super();
		setTokenHighlighter(this);
		setUI(new RuleTextPane.UI());
	}
	
	public RuleTextPane(String text) {
		super(text);
		setTokenHighlighter(this);
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
		System.out.println(compiler.compile(getText()));
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
