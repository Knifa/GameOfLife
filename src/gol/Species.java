package gol;

import java.util.Random;

/**
 * Species used in the game simulation.
 */
public enum Species {
	A,
	B,
	C,
	D,
	E,
	NONE;
	
	public static Species getRandom() {
		return Species.values()[new Random().nextInt(Species.values().length)];
	}
	
	public Species next() {
		if (this.ordinal() >= Species.values().length - 1) {
			return Species.values()[0];
		} else {
			return Species.values()[this.ordinal() + 1];
		}
	}
}
