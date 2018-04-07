package edu.utep.cs.cs3331.ard.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.utep.cs.cs3331.ard.sudoku.model.solver.AbstractSudokuSolver;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.SudokuDLX;

public class SudokuGenerator {

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
		Random rnd = new Random();
		int solutions = 1;
		int value = 0;
		int a = 0;
		int b = 0;
		int[][] temp;
		while(solutions==1) {
			a = rnd.nextInt(size);
			b = rnd.nextInt(size);
			value = board[a][b]; // save the cell value
			board[a][b] = 0; // randomly empty a board cell
			temp = solver.solve(board); // see how many solutions the board now has
			if(temp.length<size) // if the board has something other than exactly 1 solution
				solutions = board[0][0];
		}
		board[a][b] = value; // put last removed value back
		return board;
	}

}
