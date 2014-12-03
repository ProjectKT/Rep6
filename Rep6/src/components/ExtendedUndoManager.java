package components;

import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * 1文字単位でなく長い文字列で Undo, Redo の機能を提供する UndoManager
 */
public class ExtendedUndoManager extends UndoManager {

	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		final UndoableEdit lastEdit = lastEdit();
		if (anEdit instanceof AbstractDocument.DefaultDocumentEvent) {
			final EventType lastEditType = (lastEdit == null) ? null : ((AbstractDocument.DefaultDocumentEvent) lastEdit).getType();
			final EventType theEditType = ((AbstractDocument.DefaultDocumentEvent) anEdit).getType();
			
			boolean shouldMerge = false;
			
			// 連続した入力、変更、削除なら前の編集にマージする
			if (theEditType == lastEditType) {
				shouldMerge = true;
			}
			System.out.println("shouldMerge="+shouldMerge);

			if (shouldMerge) {
				if (!lastEdit.addEdit(anEdit)) {
					System.out.println("replaceEdit");
					lastEdit.replaceEdit(anEdit);
				}
				return true;
			} else {
				return super.addEdit(anEdit);
			}
		} else {
			return super.addEdit(anEdit);
		}
	}

	
}
