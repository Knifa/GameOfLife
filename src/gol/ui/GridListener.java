package gol.ui;

import java.util.EventListener;

/**
 * Interface for grid event listeners.
 */
public interface GridListener extends EventListener {
	public void eventOccured(GridEvent e);
}