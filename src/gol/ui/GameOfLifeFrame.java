package gol.ui;

import gol.GameOfLife;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

/**
 * Swing frame for the simulation.
 */
public class GameOfLifeFrame extends JFrame {
	private GameOfLife game;
	private ColorGrid colorGrid;
	
	/**
	 * Key listener for pausing the simulation and whatnot.
	 */
	private class GameKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			// Start/Pause the simulation when space is pressed.
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				GameOfLifeFrame.this.game.toggleRunning();
			}
			
			// Perform a simulation step if S is pressed.
			if (e.getKeyCode() == KeyEvent.VK_S) {
				GameOfLifeFrame.this.game.step();
			}
			
			// Reset the game grid if R is pressed.
			if (e.getKeyCode() == KeyEvent.VK_R) {
				GameOfLifeFrame.this.game.resetGrid();
			}
			
			// Fill with random cells
			if (e.getKeyCode() == KeyEvent.VK_F) {
				GameOfLifeFrame.this.game.randomizeGrid();
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	/**
	 * Grid listener for updating the grid when a user clicks on a particular square.
	 * 
	 * This will be called in the AWT thread so instead we specifically update another array, which
	 * is then checked and merged at the end of the loop cycle otherwise everything goes a bit
	 * weird and crazy.
	 */
	private class GameGridListener implements GridListener {
		@Override
		public void eventOccured(GridEvent e) {
			GameOfLifeFrame.this.game.registerGridChange(e.getX(), e.getY(), 
					GameOfLifeFrame.this.game.getGrid().getCoord(e.getX(), e.getY()).next());
		}
	}
	
	/**
	 * Constructor.
	 * @param windowSize Size of this window.
	 * @param gridSize Size of the grid.
	 */
	public GameOfLifeFrame(GameOfLife game, int windowSize, int gridSize) {
		super();	
		this.game = game;
		
		// Set window settings.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		// Create and add the color grid to this frame.
		this.colorGrid = new ColorGrid(windowSize, gridSize);		
		this.add(this.colorGrid);
		this.pack();		
		
		// Register events
		this.addKeyListener(new GameKeyListener());
		this.getColorGrid().addGridListener(new GameGridListener());
		
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
