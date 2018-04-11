package edu.utep.cs.cs3331.ard.sudoku.model.solver;

import java.util.List;

import edu.utep.cs.cs3331.ard.sudoku.model.solver.DancingLinks.DancingNode;

/**
 * Deciphers a list of dancing nodes representing a solution into a more usable data structure.
 * @author Rafal Szymanski
 */
public interface SolutionHandler {
	void handleSolution(List<DancingNode> solution);
}

/**
 * Deciphers a list of dancing nodes representing a solution into a Sudoku board.
 * @author Rafal Szymanski
 */
class SudokuHandler implements SolutionHandler {
	int size = 9;

	@Override
	public void handleSolution(List<DancingNode> answer) {
		int[][] result = parseBoard(answer);
		AbstractSudokuSolver.printSolution(result);
	}

	/**
	 * Parses a list of dancing nodes representing a solution into a Sudoku board.
	 * @param answer list of dancing nodes representing a solution.
	 * @return 2D Sudoku board.
	 */
	protected int[][] parseBoard(List<DancingNode> answer) {
		int[][] result = new int[size][size];
		for (DancingNode n : answer) {
			DancingNode rcNode = n;
			int min = Integer.parseInt(rcNode.C.name);
			for (DancingNode tmp = n.R; tmp != n; tmp = tmp.R) {
				int val = Integer.parseInt(tmp.C.name);
				if (val < min) {
					min = val;
					rcNode = tmp;
				}
			}
			int ans1 = Integer.parseInt(rcNode.C.name);
			int ans2 = Integer.parseInt(rcNode.R.C.name);
			int r = ans1 / size;
			int c = ans1 % size;
			int num = (ans2 % size) + 1;
			result[r][c] = num;
		}
		return result;
	}

	public SudokuHandler(int boardSize) {
		size = boardSize;
	}

}

/**
 * Deciphers a list of dancing nodes representing a solution into a Sudoku board.
 * and saves a copy of it.
 * @author Anthony DesArmier
 */
class SudokuReturnHandler extends SudokuHandler {
	/** 2D Sudoku board solution. */
	private int[][] result;
	
	@Override
	public void handleSolution(List<DancingNode> answer) {
		result = parseBoard(answer);
	}
	
	/**
	 * Getter for {@link #result}.
	 * @return {@link #result}
	 */
	public int[][] getResult() {
		return result;
	}
	
	public SudokuReturnHandler(int boardSize) {
		super(boardSize);
	}
	
}

/**
 * Deciphers a list of dancing nodes representing a solution into a String printout.
 * @author Rafal Szymanski
 */
class DefaultHandler implements SolutionHandler {
	@Override
	public void handleSolution(List<DancingNode> answer) {
		for (DancingNode n : answer) {
			String ret = "";
			ret += n.C.name + " ";
			DancingNode tmp = n.R;
			while (tmp != n) {
				ret += tmp.C.name + " ";
				tmp = tmp.R;
			}
			System.out.println(ret);
		}
	}
}