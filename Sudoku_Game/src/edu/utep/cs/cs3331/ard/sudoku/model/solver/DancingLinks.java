package edu.utep.cs.cs3331.ard.sudoku.model.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Dancing Links data structure.
 * @author Rafal Szymanski
 * @author Anthony DesArmier
 * @version     1.1
 */
public class DancingLinks {

	static final boolean verbose = false;

	class DancingNode {
		DancingNode L, R, U, D;
		ColumnNode C;

		// hooks node n1 `below` current node
		DancingNode hookDown(DancingNode n1) {
			assert (this.C == n1.C);
			n1.D = this.D;
			n1.D.U = n1;
			n1.U = this;
			this.D = n1;
			return n1;
		}

		// hooks a node n1 to the right of `this` node
		DancingNode hookRight(DancingNode n1) {
			n1.R = this.R;
			n1.R.L = n1;
			n1.L = this;
			this.R = n1;
			return n1;
		}

		void unlinkLR() {
			this.L.R = this.R;
			this.R.L = this.L;
			updates++;
		}

		void relinkLR() {
			this.L.R = this.R.L = this;
			updates++;
		}

		void unlinkUD() {
			this.U.D = this.D;
			this.D.U = this.U;
			updates++;
		}

		void relinkUD() {
			this.U.D = this.D.U = this;
			updates++;
		}

		public DancingNode() {
			L = R = U = D = this;
		}

		public DancingNode(ColumnNode c) {
			this();
			C = c;
		}
	}

	class ColumnNode extends DancingNode {
		int size; // number of ones in current column
		String name;

		public ColumnNode(String n) {
			super();
			size = 0;
			name = n;
			C = this;
		}

		void cover() {
			unlinkLR();
			for (DancingNode i = this.D; i != this; i = i.D) {
				for (DancingNode j = i.R; j != i; j = j.R) {
					j.unlinkUD();
					j.C.size--;
				}
			}
			header.size--; // not part of original
		}

		void uncover() {
			for (DancingNode i = this.U; i != this; i = i.U) {
				for (DancingNode j = i.L; j != i; j = j.L) {
					j.C.size++;
					j.relinkUD();
				}
			}
			relinkLR();
			header.size++; // not part of original
		}
	}

	private ColumnNode header;
	private int solutions;
	private int updates;
	private SolutionHandler handler;
	private List<DancingNode> answer;

	/**
	 * Searches for solutions using Dancing Links implementation of Algorithm X.
	 * @param k branch to search through.
	 */
	private void search(int k) {
		if (header.R == header) { // all the columns removed
			if (verbose) {
				System.out.println("-----------------------------------------");
				System.out.println("Solution #" + solutions + "\n");
			}
			handler.handleSolution(answer);
			if (verbose) {
				System.out.println("-----------------------------------------");
			}
			solutions++;
		} else {
			ColumnNode c = selectColumnNodeHeuristic();
			c.cover();

			for (DancingNode r = c.D; r != c; r = r.D) {
				answer.add(r);

				for (DancingNode j = r.R; j != r; j = j.R) {
					j.C.cover();
				}

				search(k + 1);

				r = answer.remove(answer.size() - 1);
				c = r.C;

				for (DancingNode j = r.L; j != r; j = j.L) {
					j.C.uncover();
				}
			}
			c.uncover();
		}
	}
	
	/**
	 * Searches for solutions using Dancing Links implementation of Algorithm X
	 * in various modes.
	 * @param k branch to search through.
	 * @param num number of solutions to generate.
	 */
	private void searchMode(int k, int num, boolean random) {
		if(solutions >= num) return;
		if (header.R == header) { // all the columns removed
			if (verbose) {
				System.out.println("-----------------------------------------");
				System.out.println("Solution #" + solutions + "\n");
			}
			handler.handleSolution(answer);
			if (verbose) {
				System.out.println("-----------------------------------------");
			}
			solutions++;
		} else {
			if(solutions >= num) return;
			ColumnNode c = selectColumnNodeHeuristic();
			c.cover();
			
			List<DancingNode> nodes = new ArrayList<>();
			for (DancingNode r = c.D; r != c; r = r.D) { // for each node r down the column, until wrapped around, move down
				nodes.add(r);
			}
			if(random) // Select a row randomly
				Collections.shuffle(nodes);
			
			for(DancingNode r : nodes) {
				answer.add(r); // select the row

				for (DancingNode j = r.R; j != r; j = j.R) { // for each node j to the right, until wrapped around, move right
					j.C.cover();
				}

				searchMode(k + 1, num, random);

				r = answer.remove(answer.size() - 1); // put the row back
				c = r.C;

				for (DancingNode j = r.L; j != r; j = j.L) {
					j.C.uncover();
				}
			}
			c.uncover();
		}
	}

	/**
	 * Selects the first column.
	 * @return ColumnNode of the first column.
	 */
	@SuppressWarnings("unused")
	private ColumnNode selectColumnNodeNaive() {
		return (ColumnNode) header.R;
	}

	/**
	 * Selects the column with least satisfied constraints.
	 * @return ColumnNode of the optimal column.
	 */
	private ColumnNode selectColumnNodeHeuristic() {
		int min = Integer.MAX_VALUE;
		ColumnNode ret = null;
		for (ColumnNode c = (ColumnNode) header.R; c != header; c = (ColumnNode) c.R) {
			if (c.size < min) {
				min = c.size;
				ret = c;
			}
		}
		return ret;
	}

	/**
	 * Selects a column randomly.
	 * @return ColumnNode of a column.
	 */
	@SuppressWarnings("unused")
	private ColumnNode selectColumnNodeRandom() {
		ColumnNode ptr = (ColumnNode) header.R;
		ColumnNode ret = null;
		int c = 1;
		while (ptr != header) {
			System.out.println("Selecting column"+c);
			if (Math.random() <= 1 / (double) c) {
				ret = ptr;
			}
			c++;
			ptr = (ColumnNode) ptr.R;
		}
		return ret;
	}

	/**
	 * Selects a specified column
	 * @param n the index of column to return.
	 * @return ColumnNode of the nth column.
	 */
	@SuppressWarnings("unused")
	private ColumnNode selectColumnNodeNth(int n) {
		int go = n % header.size;
		ColumnNode ret = (ColumnNode) header.R;
		for (int i = 0; i < go; i++)
			ret = (ColumnNode) ret.R;
		return ret;
	}

	/**
	 * Prints the board state.
	 */
	@SuppressWarnings("unused")
	private void printBoard() {
		System.out.println("Board Config: ");
		for (ColumnNode tmp = (ColumnNode) header.R; tmp != header; tmp = (ColumnNode) tmp.R) {

			for (DancingNode d = tmp.D; d != tmp; d = d.D) {
				String ret = "";
				ret += d.C.name + " --> ";
				for (DancingNode i = d.R; i != d; i = i.R) {
					ret += i.C.name + " --> ";
				}
				System.out.println(ret);
			}
		}
	}

	/**
	 * Constructs a dancing links implementation of the exact cover board.
	 * @param grid exact cover board.
	 * @return root column header node.
	 */
	private ColumnNode makeDLXBoard(int[][] grid) {
		final int COLS = grid[0].length;
		final int ROWS = grid.length;

		ColumnNode headerNode = new ColumnNode("header");
		ArrayList<ColumnNode> columnNodes = new ArrayList<ColumnNode>();

		for (int i = 0; i < COLS; i++) {
			ColumnNode n = new ColumnNode(Integer.toString(i));
			columnNodes.add(n);
			headerNode = (ColumnNode) headerNode.hookRight(n);
		}
		headerNode = headerNode.R.C;

		for (int i = 0; i < ROWS; i++) {
			DancingNode prev = null;
			for (int j = 0; j < COLS; j++) {
				if (grid[i][j] == 1) {
					ColumnNode col = columnNodes.get(j);
					DancingNode newNode = new DancingNode(col);
					if (prev == null)
						prev = newNode;
					col.U.hookDown(newNode);
					prev = prev.hookRight(newNode);
					col.size++;
				}
			}
		}

		headerNode.size = COLS;

		return headerNode;
	}

	/** Prints the number of updates. */
	private void showInfo() {
		System.out.println("Number of updates: " + updates);
	}

	/**
	 * Constructs a Dancing Links data structure from given exact cover grid and uses a default handler to decipher it.
	 * @param grid grid to convert to Dancing Links.
	 */
	public DancingLinks(int[][] grid) {
		this(grid, new DefaultHandler());
	}

	/**
	 * Constructs a Dancing Links data structure from given exact cover grid and uses a specified handler to decipher it.
	 * @param grid grid to convert to Dancing Links.
	 * @param h handler to decipher the Dancing Links.
	 */
	public DancingLinks(int[][] grid, SolutionHandler h) {
		header = makeDLXBoard(grid);
		handler = h;
	}

	/** Runs the algorithm. */
	public void runSolver() {
		init();
		search(0);
		if (verbose)
			showInfo();
	}
	
	/**
	 * Runs the algorithm in a specified mode.
	 * @param mode 1 - search for 1 solution at random, 2 - Search for a maximum of 2 solutions.
	 * @return number of solutions found.
	 */
	public int runSolverMode(int mode) {
		init();
		switch (mode) {
			case 1: searchMode(0, 1, true); break; // Search for 1 solution at random
			case 2: searchMode(0, 2, false); break; // Search for a maximum of 2 solutions
			default: throw new UnsupportedOperationException();
		}
		if (verbose)
			showInfo();
		return solutions;
	}
	
	private void init() {
		solutions = 0;
		updates = 0;
		answer = new LinkedList<DancingNode>();
	}
}