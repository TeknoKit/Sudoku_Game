package edu.utep.cs.cs3331.ard.sudoku.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Sudoku game board cell consisting of a value and state.
 * 
 * @author      Anthony DesArmier
 * @version     1.2
 */
public class Cell {
	
	/** Various states a cell may be in.*/
	public enum State {
		/** Selected cell*/
		SELECTED,
		/** Conflicts with an input value*/
		ERROR,
		/** Cannot be modified.*/
		FIXED
	}

	/** Value of the cell. */
	int value;

	/** States of the cell. */
	Set<State> states;
	
	/** Creates a default empty cell. */
	public Cell() {
		this(0);
	}
	
	/** Creates a normal cell with a given value. */
	public Cell(int value) {
		this.value = value;
		states = new HashSet<>();
	}
	
	/** Creates a cell with a given value and state. */
	public Cell(int value, State state) {
		this(value);
		states.add(state);
	}
	
	/**
	 * @return {@link #value}
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value {@link #value}.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * @return {@link #states}
	 */
	public Set<State> getStates() {
		return states;
	}
	
	/**
	 * Checks if a given state is present.
	 * @param state state to check for.
	 * @return true is the state is present, false otherwise.
	 */
	public boolean getState(State state) {
		return (states.contains(state)) ? true : false;
	}

	/**
	 * Assigns one or more {@link #states}.
	 * @param states one or more states to add.
	 */
	public void setState(State... states) {
		for(State state : states)
			this.states.add(state);
	}
	
	/**
	 * Removes one or more {@link #states}.
	 * @param states one or more states to remove.
	 */
	public void removeState(State... states) {
		for(State state : states) {
			if(this.states.contains(state))
				this.states.remove(state);
			else
				System.out.printf("Tried to remove state that did not exist: %s%n", state.toString());
		}
	}
	
	/**
	 * Compares this cell's value with another value.
	 * @param value value to compare with.
	 * @return true if equal, false otherwise.
	 */
	public boolean equals(int value ) {
		return this.value==value;
	}
}
