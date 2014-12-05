package components;

import java.awt.Color;
import java.awt.ScrollPane;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import providers.OurSuffixArray;
import providers.SuffixArray;

public class RuleTextPane extends HighlightedTextPane implements HighlightedTextPane.TokenHighlighter {
	
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
	
	/** SuffixArray */
	private SuffixArray suffixArray = new OurSuffixArray();
	

	public RuleTextPane() {
		super();
		setTokenHighlighter(this);
		setUI(new RuleTextPane.UI());
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
		super.insertUpdate(e);
		traverseRules(0); // FIXME
		updateSuggestions();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		super.removeUpdate(e);
		traverseRules(0); // FIXME
		updateSuggestions();
	}
	
	/**
	 * ルールのかたまりを見つけてハイライト、SuffixArray の更新を行う
	 * @param startPos
	 */
	private void traverseRules(int startPos) {
		// TODO implement this method
	}

	/**
	 * SuffixArray を使って Suggestions を更新する
	 */
	private void updateSuggestions() {
		String token = getLastEditedToken();
		System.out.println(" --- "+token+" --- ");
		Iterator<String> sentences = suffixArray.getSentences(token);
		while (sentences.hasNext()) {
			System.out.println(sentences.next());
		}
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
