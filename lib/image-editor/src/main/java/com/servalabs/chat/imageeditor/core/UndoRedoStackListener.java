package com.servalabs.chat.imageeditor.core;

public interface UndoRedoStackListener {

  void onAvailabilityChanged(boolean undoAvailable, boolean redoAvailable);
}
