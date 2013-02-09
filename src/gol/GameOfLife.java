package gol;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.SwingUtilities;

import gol.ui.GameOfLifeFrame;
import gol.ui.GridEvent;
import gol.ui.GridListener;

/**
 * Conway's Game of Life simulator!
 */
public class GameOfLife {
	public static int GAME_SIZE = 96;
	public static int BLOCK_SIZE = 640 / GameOfLife.GAME_SIZE;
	public static int WINDOW_SIZE = GameOfLife.GAME_SIZE * GameOfLife.BLOCK_SIZE;
	public static int STEP_DELAY = (int) (1.0/16.0 * 1000.0);
	
	private GameOfLifeFrame frame;
	private SpeciesGrid grid;
	
	private boolean running;
	private boolean step;
	private boolean reset;
	private boolean randomize;
	
	private Species[][] gridChanges;
	
	/**
	 * Key listener for pausing the simulation and whatnot.
	 */
	private class GameKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			// Start/Pause the simulation when space is pressed.
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				GameOfLife.this.running = !GameOfLife.this.running;
			}
			
			// Perform a simulation step if S is pressed.
			if (e.getKeyCode() == KeyEvent.VK_S) {
				GameOfLife.this.step = true;
			}
			
			// Reset the game grid if R is pressed.
			if (e.getKeyCode() == KeyEvent.VK_R) {
				GameOfLife.this.reset = true;
			}
			
			// Fill with random cells
			if (e.getKeyCode() == KeyEvent.VK_F) {
				GameOfLife.this.randomize = true;
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
			GameOfLife.this.gridChanges[e.getX()][e.getY()] = 
					GameOfLife.this.grid.getCoord(e.getX(), e.getY()).next();
		}
	}
	
	/**
	 * Entry point for starting the simulation and whatnot.
	 */
	public void run() {
		this.grid = new SpeciesGrid(GameOfLife.GAME_SIZE);
		this.gridChanges = new Species[GameOfLife.GAME_SIZE][GameOfLife.GAME_SIZE];
		
		this.frame = new GameOfLifeFrame(GameOfLife.WINDOW_SIZE, GameOfLife.GAME_SIZE);
		
		this.frame.addKeyListener(new GameKeyListener());
		this.frame.getColorGrid().addGridListener(new GameGridListener());
		
		this.running = true;
		this.step = false;
		this.gameLoop();
	}
	
	/**
	 * Main simulation loop. Loops forever as long as the window is showing.
	 */
	private void gameLoop() {
		// Loop while the game window is open -- this should matter too much as closing the window
		// should kill the entire application.
		while (this.frame.isShowing()) {
			if (this.reset) {
				this.grid.reset();
				this.reset = false;
			} else if (this.randomize) {
				this.grid.randomize();
				this.randomize = false;
			} else if (this.running || this.step) {
				this.gameStep();
				
				if (this.step) {
					this.step = false;
				}
			}
			
			this.applyGridChanges();
			this.updateColorGrid();
		}
	}
	
	/**
	 * Runs a single game step.
	 */
	private void gameStep() {
		SpeciesGrid newGrid = new SpeciesGrid(GameOfLife.GAME_SIZE);
		
		// Loop through each block on the grid running the game rules.
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				this.runRules(newGrid, x, y);
			}
		}
		
		this.grid = newGrid;
	}
	
	/**
	 * Runs Conway's rules on a particular coord in the grid.
	 * 
	 * Specifically, the rules are as follows:
	 * 		If a cell is alive:
	 * 			If it has less than 2 neighbhours, the cell dies due to underpopulation.
	 * 			if it has more than 3 neighbhours, the cell dies due to overpopulation.
	 * 		If a cell is dead:
	 * 			If it has exactly 3 neighbhours, a new cell is born.
	 * 
	 * @param newGrid New grid where changes should be saved.
	 * @param x X-coord of cell to be checked.
	 * @param y Y-coord of cell to be checked.
	 */
	private void runRules(SpeciesGrid newGrid, int x, int y) {
		int n = this.grid.getNeighbours(x, y);
		int speciesN = this.grid.getNeighbours(x, y, this.grid.getCoord(x, y));
		
		if (this.grid.getCoord(x, y) != Species.NONE) {
			if (speciesN < 2) {
				newGrid.setCoord(x, y, Species.NONE);
			} else if (speciesN > 3) {
				newGrid.setCoord(x, y, Species.NONE);
			} else {
				newGrid.setCoord(x, y, this.grid.getCoord(x, y));
			}
		} else {
			if (n == 3) {
				newGrid.setCoord(x, y, this.grid.getMajorityNeighbhour(x, y));
			}
		}
	}

	/**
	 * Merge changes from the user grid to the actual game grid because THREADS.
	 */
	private void applyGridChanges() {
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				if (this.gridChanges[x][y] != null) {
					this.grid.setCoord(x, y, this.gridChanges[x][y]);
				}
				
				this.gridChanges[x][y] = null;
			}
		}
	}
	
	/**
	 * Updates the ColorGrid. This will block while it updates to keep everything synchronised in a
	 * sane way.
	 */
	private void updateColorGrid() {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.getColorGrid().setGrid(GameOfLife.this.grid.getColorArray());
				}
			});
			
			Thread.sleep(GameOfLife.STEP_DELAY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
