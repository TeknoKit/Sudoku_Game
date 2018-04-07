package edu.utep.cs.cs3331.ard.sudoku.model.solver;

import java.util.Arrays;
import java.util.List;

import edu.utep.cs.cs3331.ard.sudoku.model.Cell;

/**
 * Various common Sudoku game board solution logic.
 * @author Rafal Szymanski
 * @author Anthony DesArmier
 * @version     1.1
 */
public abstract class AbstractSudokuSolver {

	protected int S = 9; // size of the board
	protected int side = 3; // how long the side is

	/**
	 * Returns a solution for a Sudoku board.
	 * @param sudoku sudoku board.
	 * @mode mode to run algorithm in.
	 * @see {@link edu.utep.cs.cs3331.ard.sudoku.model.solver.DancingLinks#runSolverMode}
	 */
	protected abstract int[][] runSolver(int[][] sudoku, int mode);
	/** Prints all possible solutions for a Sudoku board. */
	protected abstract void runSolverAll(int[][] sudoku);

	public int[][] generate(int[][] sudoku) {
		S = sudoku.length;
		side = (int) Math.sqrt(S);
		return runSolver(sudoku, 1);
	}
	
	/**
	 * Solves a Sudoku game board.
	 * @param sudoku Sudoku board.
	 * @return Sudoku board solution.
	 */
	public int[][] solve(int[][] sudoku) {
		if (!validateSudoku(sudoku)) {
			System.out.println("Error: Invalid sudoku. Aborting....");
			return null;
		}
		S = sudoku.length;
		side = (int) Math.sqrt(S);
		return runSolver(sudoku, 2);
	}

	/**
	 * Solves a Sudoku game board from a string representation.
	 * @param sudoku Sudoku board.
	 */
	public int[][] solve(String[] s) {
		return solve(fromCharArr(s));
	}
	
	/**
	 * Solves a Sudoku game board from a list representation.
	 * @param sudoku Sudoku board.
	 */
	public int[][] solve(List<Cell> l) {
		return solve(fromList(l));
	}

	/**
	 * Converts a string representation of a Sudoku game board to a 2D array.
	 * @param s Sudoku board.
	 * @return Sudoku board.
	 */
	private static int[][] fromCharArr(String[] s) {
		int S = s.length;
		int[][] out = new int[S][S];
		for (int i = 0; i < S; i++) {
			for (int j = 0; j < S; j++) {
				int num = s[i].charAt(j) - '1';
				if (num >= 1 && num <= S)
					out[i][j] = num;
			}
		}
		return out;
	}
	
	/**
	 * Converts a list representation of a Sudoku game board to a 2D array.
	 * @param l Sudoku board.
	 * @return Sudoku board.
	 */
	private static int[][] fromList(List<Cell> l) {
		int S = (int) Math.sqrt(l.size()); // list should be n^2
		int[][] out = new int[S][S];
		for (int i = 0; i < S; i++) {
			for (int j = 0; j < S; j++) {
				int num = l.get(j+(i*S)).getValue();
				if (num >= 1 && num <= S)
					out[i][j] = num;
			}
		}
		return out;
	}

	/** 
	 * Prints a solution.
	 * @param result solved sudoku board.
	 */
	public static void printSolution(int[][] result) {
		int N = result.length;
		for (int i = 0; i < N; i++) {
			String ret = "";
			for (int j = 0; j < N; j++) {
				ret += result[i][j] + " ";
			}
			System.out.println(ret);
		}
		System.out.println();
	}

	/**
	 * Determines whether the Sudoku board is complete and valid.
	 * <p>
	 * Uses bitmapping to determine the requirements for a completed Sudoku game board:
	 * Only one of each number exists in every row, column, and sub-grid.
	 * <p>
	 * Runs in O(n) time.
	 * @param grid 2D array of cell values.
	 * @return true of the Sudoku game board is complete and valid, false otherwise.
	 */
	protected static boolean validateSudoku(int[][] grid) {
		if (grid.length != 4 && grid.length != 9 && grid.length != 16)
			return false; // only 4, 9 or 16 for now
		for (int i = 0; i < grid.length; i++) {
			if (grid[i].length != grid.length)
				return false;
			for (int j = 0; j < grid[i].length; j++) {
				if (!(i >= 0 && i <= grid.length))
					return false; // 0 means not filled in
			}
		}

		int N = grid.length;

		boolean[] b = new boolean[N + 1];

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (grid[i][j] == 0)
					continue;
				if (b[grid[i][j]])
					return false;
				b[grid[i][j]] = true;
			}
			Arrays.fill(b, false);
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (grid[j][i] == 0)
					continue;
				if (b[grid[j][i]])
					return false;
				b[grid[j][i]] = true;
			}
			Arrays.fill(b, false);
		}

		int side = (int) Math.sqrt(N);

		for (int i = 0; i < N; i += side) {
			for (int j = 0; j < N; j += side) {
				for (int d1 = 0; d1 < side; d1++) {
					for (int d2 = 0; d2 < side; d2++) {
						if (grid[i + d1][j + d2] == 0)
							continue;
						if (b[grid[i + d1][j + d2]])
							return false;
						b[grid[i + d1][j + d2]] = true;
					}
				}
				Arrays.fill(b, false);
			}
		}
		return true;
	}
	
	/**
	 * Determines whether the Sudoku board is complete and valid.
	 * <p>
	 * Uses bitmapping to determine the requirements for a completed Sudoku game board:
	 * Only one of each number exists in every row, column, and sub-grid.
	 * <p>
	 * Runs in O(n) time.
	 * @param grid list of cell values.
	 * @param size length of Sudoku board.
	 * @param cellDim length of Sudoku board sub-grid.
	 * @return true of the Sudoku game board is complete and valid, false otherwise.
	 */
	public static boolean validateSudoku(List<Cell> grid, int size, int cellDim) {
		boolean[] bitmap;
		for(int i=0; i<size; i++) {
			bitmap = new boolean[size+1];
			bitmap[0] = true;
			for(int j=0; j<size; j++)
				if (!(bitmap[grid.get(i*size+j).getValue()] ^= true))
					return false;
		}
		
		for(int i=0; i<size; i++) {
			bitmap = new boolean[size+1];
			bitmap[0] = true;
			for(int j=0; j<size; j++)
				if (!(bitmap[grid.get(j*size+i).getValue()] ^= true))
					return false;
		}
		
		for(int i=0; i<cellDim; i++)
			for(int j=0; j<cellDim; j++) {
				bitmap = new boolean[size+1];
				bitmap[0] = true;
				for(int x=i*cellDim; x<i*cellDim+cellDim; x++)
					for(int y=j*cellDim; y<j*cellDim+cellDim; y++)
	                   	if (!(bitmap[grid.get(x*size+y).getValue()] ^= true))
	                   		return false;
			}

		return true;
	}
}