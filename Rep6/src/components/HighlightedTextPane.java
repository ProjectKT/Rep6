package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.GapContent;
import javax.swing.text.StyleContext;
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
	
	/** UndoManager */
	private final CustomUndoManager undoManager = new CustomUndoManager();
	private CustomContent content;
	private char[] delimiters = new char[]{' ','\n','\r','\t','\f'};
	
	// --- イベントリスナー ---
	/** テキスト編集時に UndoManager に編集内容を伝えるイベントリスナー */
	private UndoableEditListener undoableEditListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
		}
	};
	
	public HighlightedTextPane() {
		CustomDocument document = new CustomDocument();
		document.addDocumentListener(this);
		document.addUndoableEditListener(undoableEditListener);
		setDocument(document);
		addKeyListener(this);
		setUI(new HighlightedTextPaneUI(this));
	}

	@Override
	public void insertUpdate(DocumentEvent e) { updateTokenStyle(); }

	@Override
	public void removeUpdate(DocumentEvent e) { updateTokenStyle(); }

	@Override
	public void changedUpdate(DocumentEvent e) { updateTokenStyle(); }

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

	private void updateTokenStyle() {
		System.out.println("updateStyle(): getTokenAtCaret="+getTokenAtCaret());
	}
	
	private String getTokenAtCaret() {
		return content.getTokenAtCaret();
	}
	
	/**
	 * A customized Document
	 */
	protected class CustomDocument extends DefaultStyledDocument {
		public CustomDocument() {
			super((HighlightedTextPane.this.content = new CustomContent()), new StyleContext());
		}
	}
	
	/**
	 * A customized Content
	 */
	protected class CustomContent extends GapContent {
		/**
		 * キャレットの前のトークンを返す
		 * @return トークン
		 */
		private String getTokenAtCaret() {
			final int caretPosition = getCaretPosition();
			CharArrayTokenizer at = new CharArrayTokenizer((char[]) getArray(), delimiters, true).from(caretPosition-1);
			return at.hasMoreElements() ? at.nextToken() : "";
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
}

/**
 * UI
 */
class HighlightedTextPaneUI extends BasicTextPaneUI {
	JTextPane tc;
	Color lineColor = Color.yellow;
	
	public HighlightedTextPaneUI(JTextPane t) {
		this.tc = t;
		tc.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				tc.repaint();
			}
		});
	}
	
	@Override
	public void paintBackground(Graphics g) {
		super.paintBackground(g);
		try {
			Rectangle rect = modelToView(tc, tc.getCaretPosition());
			int y = rect.y;
			int h = rect.height;
			g.setColor(lineColor);
			g.fillRect(0, y, tc.getWidth(), h);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}
}
