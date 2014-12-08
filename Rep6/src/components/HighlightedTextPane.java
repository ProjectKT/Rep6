package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.DefaultStyledDocument.AttributeUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import utils.CharArrayTokenizer;

/**
 * RuleBaseSystem のルールをハイライトする機能を持った JTextPane
 */
public class HighlightedTextPane extends JTextPane implements DocumentListener, KeyListener {
	
	/** dummy interface implementation */
	private static final TokenHighlighter sDummyTokenHighlighter = new TokenHighlighter() {
		@Override
		public AttributeSet getAttributeSetForToken(String token) { return null; }
	};
	
	/** UndoManager */
	private final CustomUndoManager undoManager = new CustomUndoManager();
	private CustomDocument document;
	private char[] delimiters = new char[]{' ','\n','\r','\t','\f'};
	private TokenHighlighter tokenHighlighter = sDummyTokenHighlighter;
	/** default attribute set applied to tokens  when null AttributeSet is returned from {@link HighlightedTextPane#TokenHighlighter} */
	private AttributeSet defaultAttributeSet = new SimpleAttributeSet();
	/** CharacterAttribute を変更している最中かどうか */
	boolean changingCharacterAttributes = false;
	/** キャレットの存在する行をハイライトする背景色 */
	Color lineHighlightColor = new Color(0xfffbffbb);
	
	// --- イベントリスナー ---
	/** テキスト編集時に UndoManager に編集内容を伝えるイベントリスナー */
	private UndoableEditListener undoableEditListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
		}
	};
	
	public HighlightedTextPane() {
		super();
		initialize(new CustomDocument());
	}
	
	public HighlightedTextPane(CustomDocument doc) {
		super();
		initialize(doc);
	}
	
	public HighlightedTextPane(String text) {
		super();
		 StyleContext sc = new StyleContext();
		 CustomDocument doc = new CustomDocument();
		    try {
				doc.insertString(0, text, sc.getStyle(StyleContext.DEFAULT_STYLE));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		initialize(doc);
	}
	
	private void initialize(CustomDocument doc) {
		document = doc;
		document.addDocumentListener(this);
		document.addUndoableEditListener(undoableEditListener);
		setStyledDocument(document);
		addKeyListener(this);
		setUI(new HighlightedTextPane.UI());
	}
	
	
	/**
	 * TokenHighlighter を設定する
	 * @param tokenHighlighter
	 */
	public void setTokenHighlighter(TokenHighlighter tokenHighlighter) {
		this.tokenHighlighter = (tokenHighlighter == null) ? sDummyTokenHighlighter : tokenHighlighter;
	}
	
	/**
	 * デフォルトの AttributeSet を設定する
	 * @param attr
	 */
	public void setDefaultAttributeSet(AttributeSet attr) {
		defaultAttributeSet = attr;
		updateWholeTokenStyle();
	}
	
	/**
	 * キャレットの存在する行をハイライトする背景色を設定する
	 * @param color
	 */
	public void setLineHighlightColor(Color color) {
		lineHighlightColor = color;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
//		System.out.println("insertUpdate(): offset="+e.getOffset()+", length="+e.getLength());
		try {
			stylizeDocument(e.getOffset()+e.getLength()-1, e.getOffset());
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
//		System.out.println("removeUpdate(): offset="+e.getOffset()+", length="+e.getLength());
		try {
			stylizeDocument(e.getOffset()-e.getLength(), e.getOffset()-e.getLength()+1);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (!changingCharacterAttributes) {
//			System.out.println("changedUpdate(): offset="+e.getOffset()+", length="+e.getLength());
			try {
				stylizeDocument(e.getOffset()+e.getLength()-1, e.getOffset());
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Z:
			if (e.isControlDown() || e.isMetaDown()) {
				if (e.isShiftDown()) {
					// redo
					try { undoManager.redo(); } catch (CannotRedoException e1) { Toolkit.getDefaultToolkit().beep(); }
				} else {
					// undo
					try { undoManager.undo(); } catch (CannotUndoException e1) { Toolkit.getDefaultToolkit().beep(); }
				}
				e.consume();
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { }
	
	public void setCharacterAttributes(final int offset, final int length, final AttributeSet s, final boolean replace) throws InvocationTargetException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				document.setCharacterAttributes(offset, length, s, replace);
			}
		});
	}
	
	/**
	 * ドキュメント全体のトークンのハイライトを更新する
	 */
	private void updateWholeTokenStyle() {
		// TODO implement this method
	}
	
	/**
	 * ドキュメント特定の範囲の文字列をハイライトする
	 * @param start
	 * @param end
	 * @throws BadLocationException 
	 */
	private void stylizeDocument(int start, int end) throws BadLocationException {
		// start, end の位置をデリミタまで拡大
		//  aaa bbb ccc       aaa bbb ccc
		//   A   A           A       A
		//   |   |      -->  |       |
		//   |  end          |      end
		// start           start
//		System.out.println("stylizeDocument("+start+", "+end+")");
		
		CharArrayTokenizer at = (start < end) ?
				document.getTokenizer().from(start).reverse():
				document.getTokenizer().from(start);
		if (at.hasMoreElements()) {
			// 一つ前のデリミタまで戻す
			at.nextToken();
			start = at.getCurrentPosition();
		}
		at = at.reverse();
		final int minPosition = Math.max((start < end) ? start : end, 0);
		final int maxPosition = Math.min((start < end) ? end : start, document.getLength());

		int pos = at.getCurrentPosition();
//		System.out.println("stylizeDocument("+start+", "+end+"): min="+minPosition+", max="+maxPosition+", pos="+pos);
		while (minPosition <= pos && pos < maxPosition && at.hasMoreElements()) {
			String token = at.nextToken();
			pos = at.getCurrentPosition();
			if (-1 <= pos) {
				AttributeSet attr = tokenHighlighter.getAttributeSetForToken(token);
				attr = (attr == null) ? defaultAttributeSet : attr;
				if (start < end) {
//					System.out.println(" token: "+token+" ("+(pos-token.length())+" --> "+(pos)+")");
					try { setCharacterAttributes(pos-token.length(), token.length(), attr, true); } catch (InvocationTargetException e) { }
				} else {
//					System.out.println(" token: "+token+" ("+(pos+1)+" --> "+(pos+token.length())+")");
					try { setCharacterAttributes(pos+1, token.length(), attr, true); } catch (InvocationTargetException e) { }
				}
			}
		}
	}
	
	/**
	 * 最後に編集したトークンを返す
	 * @return
	 */
	protected String getLastEditedToken() {
		try {
			return document.getTokenizer().from(getCaretPosition()).reverse().nextToken();
		} catch (NoSuchElementException | BadLocationException e) {
			return null;
		}
	}
	
	/**
	 * A customized Document
	 */
	protected class CustomDocument extends DefaultStyledDocument {
		
		@Override
		public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
			changingCharacterAttributes = true;
			try {
				super.setCharacterAttributes(offset, length, s, replace);
			} finally {
				changingCharacterAttributes = false;
			}
		}
		
		/**
		 * CharArrayTokenizer を返す
		 * @return
		 * @throws BadLocationException 
		 */
		public CharArrayTokenizer getTokenizer() throws BadLocationException {
			Segment txt = new Segment();
			getText(0, getLength(), txt);
//			System.out.println("getTokenizer(): (len="+getLength()+"), "+txt+", offset="+txt.offset+", count="+txt.count);
			return new CharArrayTokenizer(txt.array, delimiters)
						.offset(txt.offset)
						.limit(txt.count);
		}
	}
	
	/**
	 * A customized UndoManager
	 */
	protected class CustomUndoManager extends UndoManager {
		private boolean nextIsMergeable = false;
		
		@Override
		public synchronized boolean addEdit(UndoableEdit anEdit) {
			final UndoableEdit lastEdit = lastEdit();
			if (anEdit instanceof AbstractDocument.DefaultDocumentEvent) {
				final EventType lastEditType = (lastEdit == null) ? null : ((MultiEdit) lastEdit).lastEventType;
				final EventType theEditType = ((AbstractDocument.DefaultDocumentEvent) anEdit).getType();
				
				// プログラム側からの変更(スタイルの変更等)なら無視
				if (theEditType == EventType.CHANGE) {
					return false;
				}
				
				boolean shouldMerge = false;

				// 連続した入力、変更、削除であれば直前の編集にマージ
				if (theEditType == lastEditType) {
					shouldMerge = true;
				}
				
				shouldMerge = shouldMerge && nextIsMergeable;

				nextIsMergeable = true;
				if (shouldMerge && lastEdit != null) {
					return lastEdit.addEdit(anEdit);
				} else {
					if (lastEdit != null) {
						((MultiEdit) lastEdit).end();
					}
					return super.addEdit(new MultiEdit(anEdit));
				}
			} else {
				return false;
			}
		}
		
		@Override
		public synchronized void undo() throws CannotUndoException {
			super.undo();
			nextIsMergeable = false;
		}

		@Override
		public synchronized void redo() throws CannotRedoException {
			super.redo();
			nextIsMergeable = false;
		}

		protected class MultiEdit extends CompoundEdit {
			EventType lastEventType;
			
			public MultiEdit(UndoableEdit anEdit) {
				super();
				addEdit(anEdit);
			}
			
			@Override
			public boolean addEdit(UndoableEdit anEdit) {
				if (anEdit instanceof AbstractDocument.DefaultDocumentEvent) {
					lastEventType = ((AbstractDocument.DefaultDocumentEvent) anEdit).getType();
				}
				return super.addEdit(anEdit);
			}

			@Override
			public boolean isInProgress() {
				// canUndo(), canRedo() が常に false になるのを防ぐ
				return false;
			}
		}
	}

	/**
	 * TokenHighlighter interface
	 */
	public interface TokenHighlighter {
		public AttributeSet getAttributeSetForToken(String token);
	}
	
	/**
	 * UI
	 */
	protected class UI extends BasicTextPaneUI {
		public UI() {
			addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent e) {
					HighlightedTextPane.this.repaint();
				}
			});
		}
		
		@Override
		public void paintBackground(Graphics g) {
			super.paintBackground(g);
			try {
				Rectangle rect = modelToView(HighlightedTextPane.this, HighlightedTextPane.this.getCaretPosition());
				int y = rect.y;
				int h = rect.height;
				g.setColor(lineHighlightColor);
				g.fillRect(0, y, HighlightedTextPane.this.getWidth(), h);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		}
	}
}
