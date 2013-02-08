package gol.ui;

import javax.swing.JFrame;

/**
 * Swing frame for the simulation.
 */
public class GameOfLifeFrame extends JFrame {
	private ColorGrid colorGrid;
	
	/**
	 * Constructor.
	 * @param windowSize Size of this window.
	 * @param gridSize Size of the grid.
	 */
	public GameOfLifeFrame(int windowSize, int gridSize) {
		super();	
		
		// Set window settings.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		// Create and add the color grid to this frame.
		this.colorGrid = new ColorGrid(windowSize, gridSize);		
		this.add(this.colorGrid);
		this.pack();		
		
		this.setVisible(true);
	}
	
	/**
	 * Returns the color grid attached to this frame.
	 * @return Color grid attached to this frame.
	 */
	public ColorGrid getColorGrid() {
		return this.colorGrid;
	}
}
