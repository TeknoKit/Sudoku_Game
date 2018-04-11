package edu.utep.cs.cs3331.ard.sudoku.model.solver;

import java.util.Arrays;

/**
 * Dancing Links Algorithm X based Sudoku solver.
 * @author Rafal Szymanski
 * @author Anthony DesArmier
 * @version     1.1
 */
public class SudokuDLX extends AbstractSudokuSolver {

	/**
	 * Generates the exact cover grid for a given Sudoku puzzle.
	 * @return exact cover grid.
	 */
	private int[][] makeExactCoverGrid(int[][] sudoku) {
		int[][] R = sudokuExactCover();
		for (int i = 1; i <= S; i++) {
			for (int j = 1; j <= S; j++) {
				int n = sudoku[i - 1][j - 1];
				if (n != 0) { // zero out in the constraint board
					for (int num = 1; num <= S; num++) {
						if (num != n) {
							Arrays.fill(R[getIdx(i, j, num)], 0);
						}
					}
				}
			}
		}
		return R;
	}

	/**
	 * Generates the base exact cover grid for a Sudoku puzzle.
	 * @return base exact cover grid.
	 */
	private int[][] sudokuExactCover() {
		//int[][] R = new int[9 * 9 * 9][9 * 9 * 4]; // 9x9 constraint matrix
		int[][] R = new int[S * S * S][S * S * 4];

		int hBase = 0;

		// row-column constraints
		for (int r = 1; r <= S; r++) {
			for (int c = 1; c <= S; c++, hBase++) {
				for (int n = 1; n <= S; n++) {
					R[getIdx(r, c, n)][hBase] = 1;
				}
			}
		}

		// row-number constraints
		for (int r = 1; r <= S; r++) {
			for (int n = 1; n <= S; n++, hBase++) {
				for (int c1 = 1; c1 <= S; c1++) {
					R[getIdx(r, c1, n)][hBase] = 1;
				}
			}
		}

		// column-number constraints
		for (int c = 1; c <= S; c++) {
			for (int n = 1; n <= S; n++, hBase++) {
				for (int r1 = 1; r1 <= S; r1++) {
					R[getIdx(r1, c, n)][hBase] = 1;
				}
			}
		}

		// box-number constraints
		for (int br = 1; br <= S; br += side) {
			for (int bc = 1; bc <= S; bc += side) {
				for (int n = 1; n <= S; n++, hBase++) {
					for (int rDelta = 0; rDelta < side; rDelta++) {
						for (int cDelta = 0; cDelta < side; cDelta++) {
							R[getIdx(br + rDelta, bc + cDelta, n)][hBase] = 1;
						}
					}
				}
			}
		}
		return R;
	}

	/**
	 * @param row row [1,S]
	 * @param col col [1,S]
	 * @param num num [1,S]
	 * @return true column index.
	 */
	private int getIdx(int row, int col, int num) {
		return (row - 1) * S * S + (col - 1) * S + (num - 1);
	}

	/** Prints all valid Sudoku boards. Will not stop any time soon. */
	public void generateAllSolutions() {
		int[][] cover = sudokuExactCover();
		DancingLinks dlx = new DancingLinks(cover, new SudokuHandler(S));
		dlx.runSolver();
	}

	/** Prints all valid Sudoku board solutions. */
	protected void runSolverAll(int[][] sudoku){
        int[][] cover = makeExactCoverGrid(sudoku);
        DancingLinks dlx = new DancingLinks(cover, new SudokuHandler(S));
        dlx.runSolver();
	}
	
	/**
	 * Runs the solver in a specified mode.
	 * @param sudoku valid sudoku board.
	 * @mode mode to run algorithm in.
	 * @return sudoku board solution, or error code array signifying how many solutions were found.
	 * @see {@link edu.utep.cs.cs3331.ard.sudoku.model.solver.DancingLinks#runSolverMode}
	 */
	@Override
	protected int[][] runSolver(int[][] sudoku, int mode){
        int[][] cover = makeExactCoverGrid(sudoku);
        SudokuReturnHandler s = new SudokuReturnHandler(S);
        DancingLinks dlx = new DancingLinks(cover, s);
        int i = dlx.runSolverMode(mode);
        if(i==1)
        	return s.getResult();
        else {
        	return new int[][] {{i}}; // Return a special error code array signifying how many solutions were found.
        } 	
	}
}