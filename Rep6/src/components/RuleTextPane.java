package components;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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
	
	
	/**
	 * カスタマイズされた UI
	 */
	protected class UI extends HighlightedTextPane.UI {
	
	}

}
