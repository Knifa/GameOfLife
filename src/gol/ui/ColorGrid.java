package gol.ui;


import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

/**
 * Swing component for drawing a square grid.
 */
public class ColorGrid extends JPanel {
	private final int componentSize;
	private final int gridSize;
	private Color[][] grid;
	
	private EventListenerList gridListenerList;
	
	/**
	 * Constructor.
	 * @param component Size Size of this component in pixels.
	 * @param gridSize Size of grid (i.e, width/height of array) 
	 */
	public ColorGrid(int componentSize, int gridSize) {
		super();
		
		this.grid = new Color[gridSize][gridSize];
		this.gridListenerList = new EventListenerList();
		
		this.componentSize = componentSize;
		this.gridSize = gridSize;
		
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}
	
	/**
	 * Returns the dimension size of this component.
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(componentSize, componentSize);
	}
	
	/**
	 * Draws this component, specifically a square grid.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int rectSize = this.getRectSize();
		for (int x = 0; x < this.gridSize; x++) {
			for (int y = 0; y < this.gridSize; y++) {
				if (this.grid[x][y] != null) {
					// Draw actual square.
					g.setColor(this.grid[x][y]);
					g.fillRect(x * rectSize, y * rectSize, rectSize, rectSize);
					
					// Draw grid lines.
					g.setColor(this.grid[x][y].darker());
					g.drawRect(x * rectSize, y * rectSize, rectSize - 1, rectSize - 1);
				}
			}
		}
	}
	
	/**
	 * Calculates the size in pixels of each grid square.
	 * @return Size in pixels of a grid square.
	 */
	private int getRectSize() {
		return this.componentSize / this.gridSize;
	}
	
	/**
	 * Called each time a new mouse event occurs. Used to fire off grid events.
	 */
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		
		// If the mouse has been clicked, fire off a grid event with the particular grid coordinate.
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {			
			int x = e.getX() / this.getRectSize();
			int y = e.getY() / this.getRectSize();
			
			GridEvent gridEvent = new GridEvent(this, x, y);
			this.fireGridEvent(gridEvent);
		}
	}
	
	/**
	 * Sets the colors of the squares in this grid, and then repaints the component.
	 * @param newGrid 2D array of Colors that this grid should be painted in.
	 */
	public void setGrid(Color[][] newGrid) {
		this.grid = newGrid;
		this.repaint();
	}
	
	/**
	 * Adds a grid event listener to this component.
	 * @param l Listener to be added.
	 */
	public void addGridListener(GridListener l) {
		this.gridListenerList.add(GridListener.class, l);
	}
	
	/**
	 * Removes a grid event listener from this component.
	 * @param l Listener to be removed.
	 */
	public void removeGridListener(GridListener l) {
		this.gridListenerList.remove(GridListener.class, l);
	}
	
	/**
	 * Fires off a grid event to each listener that has been added to this component.
	 * @param e Event to be sent to each listener.
	 */
	private void fireGridEvent(GridEvent e) {
		GridListener[] listeners = this.gridListenerList.getListeners(GridListener.class);
		for (GridListener l : listeners) {
			l.eventOccured(e);
		}
	}
}
