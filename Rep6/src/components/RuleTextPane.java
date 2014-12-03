package components;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * RuleBaseSystem のルールをハイライトする機能を持った JTextPane
 */
public class RuleTextPane extends JTextPane implements DocumentListener, KeyListener {
	
	/** UndoManager */
	private RuleUndoManager undoManager = new RuleUndoManager();
	
	// --- イベントリスナー ---
	/** テキスト編集時に UndoManager に編集内容を伝えるイベントリスナー */
	private UndoableEditListener undoableEditListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
		}
	};
	
	public RuleTextPane() {
		RuleDocument document = new RuleDocument();
		document.addDocumentListener(this);
		document.addUndoableEditListener(undoableEditListener);
		setDocument(document);
		addKeyListener(this);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
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

	
	/**
	 * A customized Document
	 */
	protected class RuleDocument extends DefaultStyledDocument {
		
		
	}
	
	/**
	 * A customized UndoManager
	 */
	protected class RuleUndoManager extends UndoManager {
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
				System.out.println("shouldMerge = "+shouldMerge);

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
