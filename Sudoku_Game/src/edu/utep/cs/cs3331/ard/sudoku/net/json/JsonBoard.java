package edu.utep.cs.cs3331.ard.sudoku.net.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Sudoku game board from a Sudoku Web Service API.
 * <p>
 * See <a href="http://www.cs.utep.edu/cheon/ws/sudoku/">http://www.cs.utep.edu/cheon/ws/sudoku/</a>
 * 
 * @author		Anthony DesArmier
 * @version     1.1
 */
public class JsonBoard {
	/** True if API produced a valid response, false otherwise. */
	private boolean response;
	/** Size of the Sudoku game board. */
 	private int size;
 	/** Sudoku game board squares. @see {@link JsonSquare} */
	private List<JsonSquare> squares = new ArrayList<>();
	/** Message for why response was false. */
	private String reason;

	/**
	 * Getter for {@link #response}.
	 * @return {@link #response}
	 */
	public boolean isResponse() {
		return response;
	}

	/**
	 * Setter for {@link #response}.
	 */
	public void setResponse(boolean response) {
		this.response = response;
	}

	/**
	 * Getter for {@link #size}.
	 * @return {@link #size}
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Setter for {@link #size}.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Getter for {@link #squares}.
	 * @return {@link #squares}
	 */
	public List<JsonSquare> getSquares() {
		return squares;
	}

	/**
	 * Setter for {@link #squares}.
	 */
	public void setSquares(List<JsonSquare> squares) {
		this.squares = squares;
	}
	
	/**
	 * Getter for {@link #reason}.
	 * @return {@link #reason}
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Setter for {@link #reason}.
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Adds a new JSONSquare to the JSONBoard.
	 * @param x {@link JsonSquare#x}
	 * @param y {@link JsonSquare#y}
	 * @param value {@link JsonSquare#value}
	 */
	public void addSquare(int x, int y, int value) {
		squares.add(new JsonSquare(x, y, value));
	}
}
