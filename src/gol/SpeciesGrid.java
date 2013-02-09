package gol;

import java.awt.Color;
import java.util.Random;

public class SpeciesGrid {
	private Species[][] grid;
	private int gridSize;
	
	public SpeciesGrid(int gridSize) {
		this.gridSize = gridSize;
		this.grid = new Species[gridSize][gridSize];
		this.reset();
	}
	
	public void reset() {
		for (int y = 0; y < gridSize; y++) {
			for (int x = 0; x < gridSize; x++) {
				this.setCoord(x, y, Species.NONE);
			}
		}
	}
	
	public void randomize() {
		Random rand = new Random();
		for (int y = 0; y < GameOfLife.GAME_SIZE; y++) {
			for (int x = 0; x < GameOfLife.GAME_SIZE; x++) {
				if (rand.nextBoolean()) {
					this.setCoord(x, y, Species.getRandom());
				} else {
					this.setCoord(x, y, Species.NONE);
				}
			}
		}
	}
	
	public Species getCoord(int x, int y) {
		return this.grid[x][y];
	}

	public void setCoord(int x, int y, Species value) {
		this.grid[x][y] = value;
	}
	
	/**
	 * Returns the number of neighbours around a cell of any species.
	 * @param x X-coord to be checked.
	 * @param y Y-coord to be checked.
	 * @return Number of neighbhours around a cell of any species.
	 */
	public int getNeighbours(int x, int y) {
		return 8 - getNeighbours(x, y, Species.NONE);
	}
	
	/**
	 * Returns the number of neighbhours around a cell of a given species.
	 * @param x X-coord of cell to be checked.
	 * @param y Y-coord of cell to be checked.
	 * @param color Color of neighbhour to be checked.
	 * @return Number of neighbhours around the cell which are of the species given.
	 */
	public int getNeighbours(int x, int y, Species color) {
		int n = 0;	
		
		// Loop through the cell in a square.
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				// Don't check the actual sell itself, of course.
				if (!(i == 0 && j == 0)) {
					n += (this.getCoord(wrapIndex(x + i), wrapIndex(y + j)) == color ? 1 : 0);
				}
			}
		}
		
		return n;
	}

	public Species getMajorityNeighbhour(int x, int y) {
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
	 * Returns a 2D Color array that can be passed to a ColorGrid, basically it translates the game
	 * grid into colors.
	 * @return 2D Color array for use with the ColorGrid.
	 */
	public Color[][] getColorArray() {
		final Color[][] colorGrid = new Color[this.gridSize][this.gridSize];
		
		for (int y = 0; y < this.gridSize; y++) {
			for (int x = 0; x < this.gridSize; x++) {
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
	 * Returns a valid index number for the grid by wrapping around.
	 * @param index Index to be wrapped.
	 * @return Valid index, wrapping around the grid.
	 */
	private int wrapIndex(int index) {
		return (GameOfLife.GAME_SIZE + index) % GameOfLife.GAME_SIZE;
	}
}
