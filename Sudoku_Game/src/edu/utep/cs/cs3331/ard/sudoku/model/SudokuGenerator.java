package edu.utep.cs.cs3331.ard.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.utep.cs.cs3331.ard.sudoku.model.solver.AbstractSudokuSolver;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.SudokuDLX;

/** 
 * A Sudoku board generator.
 * @author Anthony DesArmier
 * @version 1.0
 */
public class SudokuGenerator {

	/**
	 * Generates a complete Sudoku board and removes cells until
	 * removing any more would make the board improper.
	 * @param size size of the Sudoku board.
	 * @param difficulty difficulty of the board.
	 * @return a generated incomplete proper Sudoku board.
	 */
	public static int[][] generate(int size, int difficulty) {
		final AbstractSudokuSolver solver = new SudokuDLX();
		List<Integer> list = new ArrayList<Integer>();
		for(int i=1; i<size+1; i++)
			list.add(i);
		Collections.shuffle(list);
		int[][] preBoard = new int[size][size];
		for(int i=0; i<size; i++)
			preBoard[0][i] = list.get(i).intValue();
		int[][] board = solver.generate(preBoard);
		if(board.length<size) {
			System.out.printf("Found %d solutions when trying to generate a new board.%n", board[0][0]);
			return board;
		}
		ArrayList<Integer[]> cells = new ArrayList<>();
		for(int i=0; i<board.length; i++)
			for(int j=0; j<board[0].length; j++)
				cells.add(new Integer[] {i, j});
		Collections.shuffle(cells);
		int value = 0;
		int[][] temp;
		for(Integer[] cell : cells) { // remove all cells that don't
			value = board[cell[0]][cell[1]]; // save the cell value
			board[cell[0]][cell[1]] = 0; // empty a board cell
			temp = solver.solve(board); // see how many solutions the board now has
			if(temp.length<size) // if the board has something other than exactly 1 solution
				board[cell[0]][cell[1]] = value; // put last removed value back
		}
		return board;
	}

}
