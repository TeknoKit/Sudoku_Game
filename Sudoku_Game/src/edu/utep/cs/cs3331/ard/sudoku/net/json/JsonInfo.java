package edu.utep.cs.cs3331.ard.sudoku.net.json;

import java.util.List;

/**
 * Various information from a Sudoku Web Service API.
 * <p>
 * See <a href="http://www.cs.utep.edu/cheon/ws/sudoku/">http://www.cs.utep.edu/cheon/ws/sudoku/</a>
 * 
 * @author		Anthony DesArmier
 * @version     1.2
 */
public class JsonInfo {
	/** Sudoku game board sizes. */
	private List<Integer> sizes;
	/** Sudoku game board levels. */
	private List<Integer> levels;
	/** Default Sudoku game board size. */
	private int defaultSize;
	/** Default Sudoku game board difficulty. */
	private int defaultLevel;
	
	/**
	 * Getter for {@link #sizes}.
	 * @return {@link #sizes}
	 */
	public List<Integer> getSizes() {
		return sizes;
	}
	
	/**
	 * Setter for {@link #sizes}.
	 */
	public void setSizes(List<Integer> sizes) {
		this.sizes = sizes;
	}
	
	/**
	 * Getter for {@link #levels}.
	 * @return {@link #levels}
	 */
	public List<Integer> getLevels() {
		return levels;
	}
	
	/**
	 * Setter for {@link #levels}.
	 */
	public void setLevels(List<Integer> levels) {
		this.levels = levels;
	}
	
	/**
	 * Getter for {@link #defaultSize}.
	 * @return {@link #defaultSize}
	 */
	public int getDefaultSize() {
		return defaultSize;
	}
	
	/**
	 * Setter for {@link #defaultSize}.
	 */
	public void setDefaultSize(int defaultSize) {
		this.defaultSize = defaultSize;
	}
	
	/**
	 * Getter for {@link #defaultLevel}.
	 * @return {@link #defaultLevel}
	 */
	public int getDefaultLevel() {
		return defaultLevel;
	}
	
	/**
	 * Setter for {@link #defaultLevel}.
	 */
	public void setDefaultLevel(int defaultLevel) {
		this.defaultLevel = defaultLevel;
	}
}
