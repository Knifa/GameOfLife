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
	public static int GAME_SIZE = 128;
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
	 * Entry point for starting the simulation and whatnot.
	 */
	public void run() {
		this.grid = new SpeciesGrid(GameOfLife.GAME_SIZE);
		this.gridChanges = new Species[GameOfLife.GAME_SIZE][GameOfLife.GAME_SIZE];
		
		this.frame = new GameOfLifeFrame(this, GameOfLife.WINDOW_SIZE, GameOfLife.GAME_SIZE);
		
		this.running = true;
		this.step = false;
		this.gameLoop();
	}
	
	public void step() {
		this.step = true;	
	}

	public void toggleRunning() {
		this.running = !this.running;		
	}

	public void resetGrid() {
		this.reset = true;
	}

	public void registerGridChange(int x, int y, Species value) {
		this.gridChanges[x][y] = value;
	}

	public SpeciesGrid getGrid() {
		return this.grid;
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

	public void randomizeGrid() {
		this.randomize = true;		
	}
}
