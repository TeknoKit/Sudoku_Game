package edu.utep.cs.cs3331.ard.sudoku.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.utep.cs.cs3331.ard.sudoku.model.Cell.State;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.AbstractSudokuSolver;
import edu.utep.cs.cs3331.ard.sudoku.model.solver.SudokuDLX;
import edu.utep.cs.cs3331.ard.sudoku.net.json.JsonBoard;
import edu.utep.cs.cs3331.ard.sudoku.net.json.JsonSquare;

/**
 * Sudoku game board and various game logic.
 * 
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
	/** Whether the selected square is the same square selected last time. */
	private boolean sameSelected;
	/** Indices of the last set of error squares. */
	private Set<Integer> lastError;
	/** Sudoku game board solver. */
	private AbstractSudokuSolver solver = new SudokuDLX();
	/** 
	 * Input guide mode. 
	 * 0 - no guide, 1 - disable incorrect inputs, 2 - show conflicting inputs
	 */
	private int guideMode;
	/** List of actions that simulate undo commands. */
	private LinkedList<int[]> undo = new LinkedList<>();
	/** List of actions that simulate redo commands. */
	private LinkedList<int[]> redo = new LinkedList<>();

	/**
	 * Generates a Sudoku game board from a given template.
	 * @param boardJSON	JsonObject containing information used to create a Sudoku game board.
	 */
	public Board(JsonBoard jsonBoard) {
		this.size = jsonBoard.getSize();
		generateGrid(size, 0);
        for (JsonSquare square : jsonBoard.getSquares()) {
        	int index = square.getX()*size + square.getY();
        	grid.get(index).value = square.getValue();
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
	 * Generates a Sudoku game board of a given size and populates it with a pre-made list.
	 * @param size size of the board.
	 * @param list list of {x, y, z, state} tuples that define new cells.
	 */
	public Board(int size, int... list) {
		this(size);
		for(int i=0; i<list.length; i+=4) {
			grid.get(list[i+1]*size+list[i]).value = list[i+2];
			if(list[i+3]==1)
				grid.get(list[i+1]*size+list[i]).setState(State.FIXED);
		}
	}
	
	/**
	 * Generates an empty Sudoku game board of a default size (9).
	 */
	public Board() {
		this(9);
	}

	/**
	 * @return {@link #size}
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return {@link #cellDim}
	 */
	public int getCellDim() {
		return cellDim;
	}
	
	/**
	 * @return {@link #solved}
	 */
	public boolean isSolved() {
		return solved;
	}
	
	/**
	 * @return {@link #grid}
	 */
	public List<Cell> getGrid() {
		return grid;
	}
	
	/**
	 * @return {@link #lastSelected}
	 */
	public int[] getLastSelected() {
		return lastSelected;
	}
	
	/**
	 * @return {@link #sameSelected}
	 */
	public boolean isSameSelected() {
		return sameSelected;
	}
	
	/**
	 * Setter for {@link #lastSelected}.
	 * @param x x-position of the cell space.
	 * @param y y-position of the cell space.
	 */
	public void select(int x, int y) {
		if(lastSelected[0]==x && lastSelected[1]==y)
			sameSelected = true;
		else {
			lastSelected[0] = x;
			lastSelected[1] = y;
			sameSelected = false;
		}
	}

	/**
	 * Returns the value in a given cell of a Sudoku game board.
	 * @param x x-position of the cell space.
	 * @param y y-position of the cell space.
	 * @return value of the provided cell space.
	 */
	public int getValue(int x, int y) {
		return grid.get(x*size+y).value;
	}
	
	/**
	 * Determines of the last selected cell is in a fixed state.
	 * @return true if the last selected cell is fixed, false otherwise.
	 */
	public boolean getLastSelectedFixed() {
		if(cellSelected())
			return getState(lastSelected[0], lastSelected[1], State.FIXED);
		else
			return false;
	}
	
	/**
	 * Returns true/false if a given cell has a certain state.
	 * @param x x-position of the cell space.
	 * @param y y-position of the cell space.
	 * @return true if the cell has the requested state, false otherwise.
	 */
	public boolean getState(int x, int y, State state) {
		return grid.get(x*size+y).states.contains(state);
	}
	
	/**
	 * @return {@link #guideMode}
	 */
	public int getGuideMode() {
		return guideMode;
	}

	/**
	 * @param {@link #guideMode}
	 */
	public void setGuideMode(int guideMode) {
		this.guideMode = guideMode;
		if(this.guideMode!=2)
			clearErrors();
	}
	
	/**
	 * Increases the {@link #guideMode} by 1, or to 0 if it was 2.
	 * @param {@link #guideMode}
	 */
	public void incGuideMode() {
		guideMode=(guideMode+1)%3;
		if(this.guideMode!=2)
			clearErrors();
	}
	
	/**
	 * Determines if any cell is selected.
	 * @return true if a cell is selected, false otherwise.
	 */
	public boolean cellSelected() {
		return !(lastSelected[0]==-1 && lastSelected[1]==-1);
	}
	
	/** Returns this board grid as list of {x, y, z, state} tuples that define new cells. */
	public int[] toArr() {
		int i = 0;
		List<Integer> list = new ArrayList<>();
		for(Cell c : grid ) {
			if(c.getValue() != 0) {
				list.add(i%size); // x
				list.add(i/size); // y
				list.add(c.getValue()); // value
				list.add((c.getState(State.FIXED) ? 1 : 0)); // fixed state or not
			}
			i++;
		}
		return list.stream().mapToInt(Integer::intValue).toArray();
	}
	
	/**
	 * Constructs a grid representation of a Sudoku game board.
	 * @param size size of the board.
	 * @param difficulty 0 - empty, 1 - easy, 2 - normal, 3 - difficult.
	 */
	private final void generateGrid(int size, int difficulty) {
		grid = new ArrayList<>(size*size);
		this.cellDim = (int)Math.sqrt(size); // Should be a perfect square
		for(int i=0; i<size*size; i++)
			grid.add(new Cell());
		if(difficulty!=0) {
			int[][] preMade = SudokuGenerator.generate(size, difficulty);
			int x = 0;
			for(int i=0; i<preMade.length; i++)
				for(int j=0; j<preMade[0].length; j++)
					if(preMade[i][j]!=0) {
						x = j+(i*size);
						grid.get(x).value = preMade[i][j];
						grid.get(x).setState(State.FIXED);
					}
		}
		lastSelected = new int[] {-1,-1};
        lastError = new HashSet<>();
        guideMode = 0;
	}

	/**
	 * Inserts a value into the last selected cell space on the Sudoku game board if valid.
	 * @param num number to insert.
	 * @param fresh whether this is a new action or an undo/redo action.
	 * @param fillOverride whether this update originated as a fill message and should ignore any input guide rules.
	 */
	public boolean update(int num, boolean fresh, boolean fillOverride) {
		if((solved && num!=0) || lastSelected[0]<0 || lastSelected[1]<0) {
			return false;
		}
		int index = lastSelected[0]*size+lastSelected[1];
		Cell cell = grid.get(index);
		if(cell.states.contains(State.FIXED)) {
			return false;
		}
		if(!fillOverride)
			if(guideMode!=0 && !isValidEntry(new int[] {lastSelected[0], lastSelected[1], num}, true)) {
				return true; // Do not reject the update but block it anyway
			}
		int oldNum = cell.value;
		cell.value = num;
		cell.setState(State.SELECTED);
		if(num!=0 && AbstractSudokuSolver.validateSudoku(grid, size, cellDim)) // no need to check board if a 0 was just inserted
			solved = true;
		else
			solved = false;
		if(fresh) {
			undo.push(new int[] {lastSelected[0], lastSelected[1], oldNum});
			redo.clear();
		}
		return true;
	}
	
	/**
	 * Inserts a value into a given cell space on the Sudoku game board if valid.
	 * @param values x,y and z values corresponding to the Sudoku game board position and value.
	 * @param fresh whether this is a new action or an undo/redo action.
	 * @param fillOverride whether this update originated as a fill message and should ignore any input guide rules.
	 * @return 
	 */
	public boolean update(int[] values, boolean fresh, boolean fillOverride) {
		lastSelected[0] = values[0];
		lastSelected[1] = values[1];
		return update(values[2], fresh, fillOverride);
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
	 * Searches for all invalid inputs for the last selected cell.
	 * @return list of invalid input values.
	 */
	public List<Integer> invalidInputs() {
		return invalidInputs(lastSelected);
	}
	
	/**
	 * Searches for all invalid inputs for a given cell.
	 * @return list of invalid input values.
	 */
	public List<Integer> invalidInputs(int[] cell) {
		List<Integer> invalids = new ArrayList<>();
		for(int i=1; i<=size; i++)
			if(!isValidEntry(new int[] {cell[0], cell[1], i}, false))
				invalids.add(i);
		return invalids;
	}
	
	/**
	 * Checks if a given input is a valid move for a Sudoku game. Populates {@link #errorSquares} as any are found.
	 * <p>
	 * A valid move is considered to be inserting a number that does not already exist
	 * within the same row, column, or sub-grid.
	 * @param values x,y and z values corresponding to the Sudoku game board position and value.
	 * @param report true if it should record where the conflict was located, false otherwise.
	 * @return true if valid, false otherwise.
	 */
	private boolean isValidEntry(int[] values, boolean report) {
		clearErrors();
		if(values[2]!=0 ) { // no validity check if value is 0
			int[] subGrid = {(values[0]/cellDim)*cellDim, (values[1]/cellDim)*cellDim}; // floor indices to nearest multiple of cell dimension
			int j=-1, x=0, y=0, index=0;
			boolean error = false;
			for(int i=0; i<size; i++) {
				index = values[0]*size+i; // column
				error = checkError(values, report, index, error);
				index = i*size+values[1]; // row
				error = checkError(values, report, index, error);
				if(i%cellDim==0) j++;
				x = i%cellDim+subGrid[0];
				y = j%cellDim+subGrid[1];
				index = x*size+y; // sub-grid
				error = checkError(values, report, index, error);
			}
			if(error) return false;
		}
		return true;
	}

	/**
	 * Checks whether a specified cell conflicts with the input values.
	 * @param values x,y and z values corresponding to the Sudoku game board position and value.
	 * @param report true if it should record where the conflict was located, false otherwise.
	 * @param index cell index of the cell to check conflict with.
	 * @param error the current overall error state.
	 * @return true if cell conflicts with input values, previous overall state otherwise.
	 */
	private boolean checkError(int[] values, boolean report, int index, boolean error) {
		Cell cell = grid.get(index);
		if(cell.equals(values[2])) {
			if(report) {
				cell.setState(State.ERROR);
				lastError.add(index);
			}
			error = true;
		}
		return error;
	}
	
	/**
	 * Solves this board's grid.
	 * @param apply whether to apply the solution to the board or not.
	 * @return true if the board is solvable, false otherwise.
	 */
	public boolean solve(boolean apply) {
		if(solved) return true;
		int[][] solution = solver.solve(grid);
		if(solution[0][0]==0)
			return false;
		else if(solution.length<size) {
			//System.out.println("Found more than 1 solution when trying to solve the board.");
			throw new UnsupportedOperationException();
		}
		if(apply) {
			for(int i=0; i<solution.length; i++)
				for(int j=0; j<solution[0].length; j++)
					grid.get(j+(i*size)).value = solution[i][j];
			solved = true;
		}
		return true;
	}
	
	/** Clears out all error squares. */
	private void clearErrors() {
		lastError.forEach(i -> grid.get(i).removeState(State.ERROR));
		lastError.clear();
	}
	
	/** Steps back an update step. */
	public void undo() {
		if(undo.isEmpty())
			return;
		int[] action = undo.pop();
		int num = getValue(action[0], action[1]);
		update(action, false, true);
		redo.push(new int[] {action[0], action[1], num});
	}
	
	/** Steps forward an update step. Only valid after an undo. */
	public void redo() {
		if(redo.isEmpty())
			return;
		int[] action = redo.pop();
		int num = getValue(action[0], action[1]);
		update(action, false, true);
		undo.push(new int[] {action[0], action[1], num});
	}
}
