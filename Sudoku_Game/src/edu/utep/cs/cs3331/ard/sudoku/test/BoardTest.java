package edu.utep.cs.cs3331.ard.sudoku.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import edu.utep.cs.cs3331.ard.sudoku.model.Board;
import edu.utep.cs.cs3331.ard.sudoku.model.Cell.State;
import edu.utep.cs.cs3331.ard.sudoku.net.JsonClient;

public class BoardTest {

    private Board board;
    
    @Before
    public void setUp() {
        board = new Board();
    }

    @Test
    public void testSize() {
        assertEquals(9, board.getSize());
        board = new Board(4);
        assertEquals(4, board.getSize());
        board = new Board(JsonClient.requestBoard(9, 1));
        assertEquals(9, board.getSize());
    	assertEquals(3, board.getCellDim());
    }
    
    @Test
    @SuppressWarnings("deprecation")
    public void testValidInput() {
    	assertTrue(board.validInput(5));
    	assertFalse(board.validInput(12));
    	assertFalse(board.validInput(-8));
    }
    
    
	@Test
    public void testUpdate() {
    	board.update(new int[] {6, 6, 4});
    	board.update(new int[] {6, 7, 8});
    	board.update(new int[] {6, 8, 9});
    	board.update(new int[] {7, 8, 5});
    	board.update(new int[] {8, 8, 1});
    	assertEquals(4, board.getValue(6, 6));
    	assertArrayEquals(new int[] {8, 8}, board.getLastSelected());
    	board.update(new int[] {7, 7, 5});
    	assertEquals(0, board.getValue(7, 7));
    	board.update(new int[] {8, 7, 8});
    	assertEquals(0, board.getValue(8, 7));
    	board.update(new int[] {7, 8, 0});
    	assertEquals(0, board.getValue(7, 8));
    	

    }
    
	@Test
    public void testSquareState() {
		final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		board.getGrid().get(0).setState(State.NORMAL);
    	board.getGrid().get(0).removeState(State.NORMAL);
    	assertEquals("", outContent.toString());
    	board.getGrid().get(0).removeState(State.ERROR);
    	assertEquals(String.format("Tried to remove state that did not exist: %s%n",
    					State.ERROR.toString()), outContent.toString());
    	board.getGrid().get(0).setState(State.FIXED);
    	board.update(new int[] {0, 0, 3});
    	assertEquals(0, board.getValue(0, 0));
    	System.setOut(System.out);
    }
	
	@Test
	/* Interestingly, because the board blocks invalid values from being entered
	 * into grid squares, the board will never enter a state where
	 * the columns of the board are correct, but the rows and/or sub-grids are not,
	 * i.e. once the columns, rows, or sub-grids are correct, the rest must also be correct.
	 * Thus the validity check will never reach a false return on rows or sub-grids because
	 * it checks columns first, and if the columns are correct, the others
	 * are also automatically correct.
	 */
	public void testSolved() {
    	board = new Board(4);
    	board.update(new int[] {0, 0, 3});
    	board.update(new int[] {0, 1, 4});
    	board.update(new int[] {0, 2, 1});
    	board.update(new int[] {0, 3, 2});
    	board.update(new int[] {1, 0, 1});
    	board.update(new int[] {1, 1, 2});
    	board.update(new int[] {1, 2, 3});
    	board.update(new int[] {1, 3, 4});
    	board.update(new int[] {2, 0, 4});
    	board.update(new int[] {2, 1, 1});
    	board.update(new int[] {2, 2, 2});
    	board.update(new int[] {2, 3, 3});
    	board.update(new int[] {3, 0, 2});
    	board.update(new int[] {3, 1, 3});
    	board.update(new int[] {3, 2, 4});
    	board.update(new int[] {3, 3, 1});
    	assertTrue(board.isSolved());
    	board.update(new int[] {0, 1, 2});
    	assertEquals(4, board.getValue(0, 1));
	}
    
}
