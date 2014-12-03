package components;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;

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
	protected class RuleUndoManager extends CompoundEdit {
		private Document document = null;
		private boolean undoneOrRedone = true;
		
		@Override
		public synchronized boolean addEdit(UndoableEdit anEdit) {
			final UndoableEdit lastEdit = lastEdit();
			if (anEdit instanceof AbstractDocument.DefaultDocumentEvent) {
				final EventType lastEditType = (lastEdit == null) ? null : ((AbstractDocument.DefaultDocumentEvent) ((RuleUndoableEdit) lastEdit).delegate).getType();
				final EventType theEditType = ((AbstractDocument.DefaultDocumentEvent) anEdit).getType();
				
				RuleUndoableEdit theEdit = new RuleUndoableEdit(anEdit);
				boolean shouldMerge = false;
				
				// 連続した入力、変更、削除であれば直前の編集にマージ
				if (theEditType == lastEditType) {
					shouldMerge = true;
				}
				
				// ドキュメントが変わったら significant
				if (document != getDocument()) {
					document = getDocument();
					shouldMerge = false;
				}
				
				// 最後に undo, redo したなら significant
				if (undoneOrRedone) {
					undoneOrRedone = false;
					shouldMerge = false;
				}

				if (shouldMerge) {
					theEdit.isSignificant = false;
				}
				return super.addEdit(theEdit);
			} else {
				return super.addEdit(anEdit);
			}
		}
		
		@Override
		public synchronized void undo() throws CannotUndoException {
			super.undo();
			int i = edits.size();
			while (i-- > 0) {
				UndoableEdit e = edits.elementAt(i);
				e.undo();
			}
			undoneOrRedone = true;
		}
		
		@Override
		public synchronized void redo() throws CannotRedoException {
			super.redo();
			Enumeration<UndoableEdit> cursor = edits.elements();
			while (cursor.hasMoreElements()) {
				cursor.nextElement().redo();
			}
			undoneOrRedone = true;
		}

		private void setLastEditSignificant(boolean isSignificant) {
			final UndoableEdit lastEdit = lastEdit();
			if (lastEdit != null && lastEdit instanceof RuleUndoableEdit) {
				((RuleUndoableEdit) lastEdit).isSignificant = isSignificant;
			}
		}

		/**
		 * かっこ悪いけどこんな書き方しか思いつかなかった
		 */
		protected class RuleUndoableEdit implements UndoableEdit {
			private UndoableEdit delegate;
			private boolean isSignificant;
			public RuleUndoableEdit(UndoableEdit delegate) {
				this.delegate = delegate;
				this.isSignificant = true;
			}
			@Override public void undo() throws CannotUndoException { delegate.undo(); }
			@Override public boolean canUndo() { return delegate.canUndo(); }
			@Override public void redo() throws CannotRedoException { delegate.redo(); }
			@Override public boolean canRedo() { return delegate.canRedo(); }
			@Override public void die() { delegate.die(); }
			@Override public boolean addEdit(UndoableEdit anEdit) { return delegate.addEdit(anEdit); }
			@Override public boolean replaceEdit(UndoableEdit anEdit) { return delegate.replaceEdit(anEdit); }
			@Override public boolean isSignificant() { return isSignificant; } // only this method is modified
			@Override public String getPresentationName() { return delegate.getPresentationName(); }
			@Override public String getUndoPresentationName() { return delegate.getUndoPresentationName(); }
			@Override public String getRedoPresentationName() { return delegate.getRedoPresentationName(); }
		}
	}
}
