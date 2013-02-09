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
	public static int STEP_DELAY = (int) (1.0/18.0 * 1000.0);
	
	private GameOfLifeFrame frame;
	private Species[][] grid;
	
	private boolean running;
	private boolean step;
	private boolean reset;
	private boolean fill;
	
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
				GameOfLife.this.fill = true;
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
			// Add a cell to where the user clicked if nothing is already there, otherwise remove
			// it.
			switch (GameOfLife.this.grid[e.getX()][e.getY()]) {
				case NONE:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.A;
					break;
				
				case A:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.B;
					break;
					
				case B:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.C;
					break;
					
				case C:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.D;
					break;
				
				case D:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.E;
					break;
					
				case E:
					GameOfLife.this.gridChanges[e.getX()][e.getY()] = Species.NONE;
					break;
			}
		}
	}
	
	/**
	 * Entry point for starting the simulation and whatnot.
	 */
	public void run() {
		this.grid = this.createEmptyGrid();
		this.gridChanges = new Species[GameOfLife.GAME_SIZE][GameOfLife.GAME_SIZE];
		
		this.frame = new GameOfLifeFrame(GameOfLife.WINDOW_SIZE, GameOfLife.GAME_SIZE);
		
		this.frame.addKeyListener(new GameKeyListener());
		this.frame.getColorGrid().addGridListener(new GameGridListener());
		
		this.running = true;
		this.step = false;
		this.gameLoop();
	}
	
	/**
	 * Creates and returns a new species grid, initialised so that each point is Species.NONE
	 * @return A new species grid.
	 */
	private Species[][] createEmptyGrid() {
		Species[][] grid = new Species[GameOfLife.GAME_SIZE][GameOfLife.GAME_SIZE];
		
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				grid[x][y] = Species.NONE;
			}
		}
		
		return grid;
	}
	
	/**
	 * Main simulation loop. Loops forever as long as the window is showing.
	 */
	private void gameLoop() {
		// Loop while the game window is open -- this should matter too much as closing the window
		// should kill the entire application.
		while (this.frame.isShowing()) {
			if (this.reset) {
				this.resetGrid();
				this.reset = false;
			} else if (this.fill) {
				this.fillGrid();
				this.fill = false;
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
		Species[][] newGrid = this.createEmptyGrid();
		
		// Loop through each block on the grid running the game rules.
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				this.runRules(newGrid, x, y);
			}
		}
		
		this.grid = newGrid;
	}
	
	/**
	 * Completely destroys and recreates the grid.
	 */
	private void resetGrid() {
		this.grid = this.createEmptyGrid();
	}
	
	/**
	 * Resets the grid and fills it with random data.
	 */
	private void fillGrid() {
		this.resetGrid();
		
		Random rand = new Random();
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				if (rand.nextBoolean()) {
					Species s = Species.values()[rand.nextInt(Species.values().length)];
					this.grid[x][y] = s;
				}
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
					this.grid[x][y] = this.gridChanges[x][y];
				}
				
				this.gridChanges[x][y] = null;
			}
		}
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
	private void runRules(Species[][] newGrid, int x, int y) {
		int n = getNeighbours(x, y);
		int speciesN = getNeighbours(x, y, this.grid[x][y]);
		
		if (this.grid[x][y] != Species.NONE) {
			if (speciesN < 2) {
				newGrid[x][y] = Species.NONE;
			} else if (speciesN > 3) {
				newGrid[x][y] = Species.NONE;
			} else {
				newGrid[x][y] = this.grid[x][y];
			}
		} else {
			if (n == 3) {
				newGrid[x][y] = this.getMajorityNeighbhour(x, y);
			}
		}
	}
	
	/**
	 * Returns the number of neighbhours around a cell of a given species.
	 * @param x X-coord of cell to be checked.
	 * @param y Y-coord of cell to be checked.
	 * @param color Color of neighbhour to be checked.
	 * @return Number of neighbhours around the cell which are of the species given.
	 */
	private int getNeighbours(int x, int y, Species color) {
		int n = 0;	
		
		// Loop through the cell in a square.
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				// Don't check the actual sell itself, of course.
				if (!(i == 0 && j == 0)) {
					n += (this.grid[wrapIndex(x + i)][wrapIndex(y + j)] == color ? 1 : 0);
				}
			}
		}
		
		return n;
	}
	
	/**
	 * Returns the number of neighbours around a cell of any species.
	 * @param x X-coord to be checked.
	 * @param y Y-coord to be checked.
	 * @return Number of neighbhours around a cell of any species.
	 */
	private int getNeighbours(int x, int y) {
		return 8 - getNeighbours(x, y, Species.NONE);
	}
	
	private Species getMajorityNeighbhour(int x, int y) {
		Species majority = null;
		int majorityN = -1;
		
		for (Species s : Species.values()) {
			if (s != Species.NONE) {
				int testN = getNeighbours(x, y, s);
				
				if (testN > majorityN) {
					majority = s;
					majorityN = testN;
				} else if (testN == majorityN) {
					Random rand = new Random();
					boolean shouldSwap = rand.nextBoolean();
					
					if (shouldSwap) {
						majority = s;
					}
				}
			}
		}
		
		return majority;
	}
	
	/**
	 * Returns a valid index number for the grid by wrapping around.
	 * @param index Index to be wrapped.
	 * @return Valid index, wrapping around the grid.
	 */
	private int wrapIndex(int index) {
		return (GameOfLife.GAME_SIZE + index) % GameOfLife.GAME_SIZE;
	}
	
	/**
	 * Returns a 2D Color array that can be passed to a ColorGrid, basically it translates the game
	 * grid into colors.
	 * @return 2D Color array for use with the ColorGrid.
	 */
	private Color[][] getColorArray() {
		final Color[][] colorGrid = new Color[GameOfLife.GAME_SIZE][GameOfLife.GAME_SIZE];
		
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				switch (this.grid[x][y]) {
					case A:
						colorGrid[x][y] = Color.ORANGE;
						break;	
						
					case B:
						colorGrid[x][y] = Color.CYAN;
						break;
						
					case C:
						colorGrid[x][y] = Color.WHITE;
						break;
						
					case D:
						colorGrid[x][y] = Color.MAGENTA;
						break;
						
					case E:
						colorGrid[x][y] = Color.GREEN;
						break;
						
					default:
						colorGrid[x][y] = Color.DARK_GRAY;
						break;
				}
			}
		}
		
		return colorGrid;
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
					frame.getColorGrid().setGrid(GameOfLife.this.getColorArray());
				}
			});
			
			Thread.sleep(GameOfLife.STEP_DELAY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
