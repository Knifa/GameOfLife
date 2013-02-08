package gol.ui;

import java.util.EventObject;

/**
 * Event fired when a ColorGrid is clicked on.
 */
public class GridEvent extends EventObject {
	private int x;
	private int y;
	
	/**
	 * Constructor.
	 * @param source Source of this event.
	 * @param x X-coord where the grid was clicked on.
	 * @param y Y-coord where the grid was clicked on.
	 */
	public GridEvent(Object source, int x, int y) {
		super(source);
		
		this.x = x;
		this.y = y;
	}	
	
	/**
	 * Returns the grid X-coord.
	 * @return Grid X-coord.
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Returns the grid Y-coord.
	 * @return Grid Y-coord.
	 */
	public int getY() {
		return this.y;
	}
}