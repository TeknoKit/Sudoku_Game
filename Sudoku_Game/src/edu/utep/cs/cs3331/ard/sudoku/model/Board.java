package edu.utep.cs.cs3331.ard.sudoku.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.utep.cs.cs3331.ard.sudoku.model.Cell.State;
import edu.utep.cs.cs3331.ard.sudoku.net.JsonBoard;
import edu.utep.cs.cs3331.ard.sudoku.net.JsonSquare;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.AbstractSudokuSolver;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.SudokuDLX;

/**
 * Sudoku game board and various game logic.
 * @author      Anthony DesArmier
 * @version     1.3
 */
public class Board {
	/** Dimension of a Sudoku game board. */
	private int size;
	/** Dimension of a sub-grid of a Sudoku game board. */
	private int cellDim;
	/** Sudoku game board has been solved or not. */
	private boolean solved;
	/** Sudoku game board represented as a 2D integer array containing cell values. */
	private List<Cell> grid;
	/** x, y coordinate of the last selected square. */
	private int[] lastSelected;
	/** Indices of the last set of error squares. */
	private Set<Integer> lastError;
	/** Sudoku game board solver. */
	private AbstractSudokuSolver solver = new SudokuDLX();
	
	/**
	 * Generates a Sudoku game board from a given template.
	 * @param boardJSON	JsonObject containing information used to create a Sudoku game board.
	 */
	public Board(JsonBoard jsonBoard) {
		this.size = jsonBoard.getSize();
		generateGrid(size, 0);
        for (JsonSquare square : jsonBoard.getSquares()) {
        	int index = square.getX()*size + square.getY();
        	grid.get(index).setValue(square.getValue());
        	grid.get(index).setState(State.FIXED);
        }
	}

	/**
	 * Generates a Sudoku game board of a given size and difficulty.
	 * @param size size of the board.
	 * @param difficulty difficulty of the board.
	 */
	public Board(int size, int difficulty) {
		this.size = size;
		generateGrid(size, difficulty);
	}
	
	/**
	 * Generates an empty Sudoku game board of a given size.
	 * @param size size of the board.
	 */
	public Board(int size) {
		this(size, 0);
	}
	
	/**
	 * Generates an empty Sudoku game board of a default size (9).
	 */
	public Board() {
		this(9);
	}

	/**
	 * Constructs a grid representation of a Sudoku game board.
	 * @param size size of the board.
	 * @param difficulty 0 - empty, 1 - easy, 2 - normal, 3 - difficult.
	 */
	private final void generateGrid(int size, int difficulty) {
		this.cellDim = (int)Math.sqrt(size); // Should be a perfect square
		grid = new ArrayList<>(size*size);
		for(int i=0; i<size*size; i++)
			grid.add(new Cell());
		if(difficulty!=0) {
			int[][] preMade = SudokuGenerator.generate(size, difficulty);
			int x = 0;
			for(int i=0; i<preMade.length; i++)
				for(int j=0; j<preMade[0].length; j++)
					if(preMade[i][j]!=0) {
						x = j+(i*size);
						grid.get(x).setValue(preMade[i][j]);
						grid.get(x).setState(State.FIXED);
					}
		}
		lastSelected = new int[] {-1,-1};
        lastError = new HashSet<>();
	}

	/**
	 * Getter for {@link #size}.
	 * @return {@link #size}
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Getter for {@link #cellDim}.
	 * @return {@link #cellDim}
	 */
	public int getCellDim() {
		return cellDim;
	}
	
	/**
	 * Getter for {@link #solved}.
	 * @return {@link #solved}
	 */
	public boolean isSolved() {
		return solved;
	}
	
	/**
	 * Getter for {@link #grid}.
	 * @return {@link #grid}
	 */
	public List<Cell> getGrid() {
		return grid;
	}
	
	/**
	 * Getter for {@link #lastSelected}.
	 * @return {@link #lastSelected}
	 */
	public int[] getLastSelected() {
		return lastSelected;
	}
	
	/**
	 * Setter for {@link #lastSelected}.
	 * @param x x-position of the cell space.
	 * @param y y-position of the cell space.
	 */
	public void select(int x, int y) {
		lastSelected[0] = x;
		lastSelected[1] = y;
	}
	
	/**
	 * Returns the value in a given cell of a Sudoku game board.
	 * @param x x-position of the cell space.
	 * @param y y-position of the cell space.
	 * @return value of the provided cell space.
	 */
	public int getValue(int x, int y) {
		return grid.get(x*size+y).getValue();
	}

	/**
	 * Inserts a value into the last selected cell space on the Sudoku game board if valid.
	 * @param num number to insert.
	 */
	public void update(int num) {
		if((solved && num!=0) || lastSelected[0]<0 || lastSelected[1]<0)
			return;
		int index = lastSelected[0]*size+lastSelected[1];
		Cell cell = grid.get(index);
		if(cell.getState().contains(State.FIXED))
			return;
		if(!isValidEntry(new int[] {lastSelected[0], lastSelected[1], num}))
			return;
		cell.setValue(num);
		cell.setState(State.SELECTED);
		if(num!=0 && AbstractSudokuSolver.validateSudoku(grid, size, cellDim)) // no need to check board if a 0 was just inserted
			solved = true;
		else
			solved = false;
	}
	
	/**
	 * Inserts a value into a given cell space on the Sudoku game board if valid.
	 * @param values x,y and z values corresponding to the Sudoku game board position and value.
	 */
	public void update(int[] values) {
		lastSelected[0] = values[0];
		lastSelected[1] = values[1];
		update(values[2]);
	}

	/**
	 * Determines if a certain input is valid for the Sudoku game board.
	 * <p>
	 * A valid integer can be any integer (0,board size).
	 * @param value	value to determine if it is valid for the Sudoku game board.
	 * @return true if the input is valid, false otherwise.
	 * @deprecated
	 */
	public boolean validInput(int value) {
		if(value > size || value < 0)
			return false;
		return true;
	}
	
	/**
	 * Checks if a given input is a valid move for a Sudoku game. Populates {@link #errorSquares} as any are found.
	 * <p>
	 * A valid move is considered to be inserting a number that does not already exist
	 * within the same row, column, or sub-grid.
	 * @param values x,y and z values corresponding to the Sudoku game board position and value.
	 * @return true if valid, false otherwise.
	 */
	private boolean isValidEntry(int[] values) {
		lastError.forEach(i -> grid.get(i).removeState(State.ERROR)); // clear out old errors
		lastError.clear();
		if(values[2]!=0 ) { // no validity check if value is 0
			int[] subGrid = {(values[0]/cellDim)*cellDim, (values[1]/cellDim)*cellDim}; // floor indices to nearest multiple of cell dimension
			int j=-1, x=0, y=0, index=0;
			boolean error = false;
			Cell cell;
			for(int i=0; i<size; i++) {
				index = values[0]*size+i;
				cell = grid.get(index); // column
				if(cell.equals(values[2]) ) {
					cell.setState(State.ERROR);
					lastError.add(index);
					error = true;
				}
				index = i*size+values[1];
				cell = grid.get(index); // row
				if(cell.equals(values [2])) { 
					cell.setState(State.ERROR);
					lastError.add(index);
					error = true;
				}
				if(i%cellDim==0) j++; 
				x = i%cellDim+subGrid[0];
				y = j%cellDim+subGrid[1];
				index = x*size+y;
				cell = grid.get(index); // sub-grid
				if(cell.equals(values[2])) {
					cell.setState(State.ERROR);
					lastError.add(index);
					error = true;
				}
			}
			if(error) return false;
		}
		return true;
	}
	
	/** Solves this board's grid. */
	public void solve() {
		if(solved) return;
		int[][] solution = solver.solve(grid);
		if(solution.length<size) {
			System.out.println("Found more than 1 solution when trying to solve the board.");
			return;
		}
		for(int i=0; i<solution.length; i++)
			for(int j=0; j<solution[0].length; j++)
				grid.get(j+(i*size)).setValue(solution[i][j]);
		solved = true;
	}

}
