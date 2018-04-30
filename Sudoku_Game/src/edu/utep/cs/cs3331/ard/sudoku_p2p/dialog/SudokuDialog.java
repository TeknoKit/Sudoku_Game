package edu.utep.cs.cs3331.ard.sudoku_p2p.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import edu.utep.cs.cs3331.ard.sudoku.dialog.BoardPanel;
import edu.utep.cs.cs3331.ard.sudoku.model.Board;
import edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkManager;

/**
 * Dialog template for playing simple Sudoku games.
 * 
 * @author		Yoonsik Cheon
 * @author		Anthony DesArmier
 * @author 		Trevor McCarthy
 * @version     1.5
 */
public class SudokuDialog extends edu.utep.cs.cs3331.ard.sudoku.dialog.SudokuDialog {
	private static final long serialVersionUID = -7356364696389964682L;
	
	/** Attached NetworkWindow. */
	private NetworkWindow netWin;
	/** Attached NetWorkManager. */
	private NetworkManager netMan;

	/** Network menu item. */
	private JMenuItem multiplayer;
	/** Network button. */
	private JButton networkB;
	/** Network icon status. */
	private boolean connected;

	/**
     * Create a new dialog with the default screen dimensions.
     * @param size Sudoku game board size.
     * @param difficulty Sudoku game difficulty.
     */
    public SudokuDialog(int size, int difficulty) {
    	super(DEFAULT_DIM, size, difficulty);
    	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    	addWindowListener(new WindowAdapter() {
	         @Override
	         public void windowClosing(WindowEvent e) {
	            shutDown();
	         }
	    });
    	netWin = new NetworkWindow(this);
    	netMan = netWin.netMan;
    	connected = false;
    }
    
    /**
     * @return {@link BoardPanel}
     */
	public BoardPanel getBoardPanel() {
		return boardPanel;
	}
    
	@Override
	protected void createMenu() {
		super.createMenu();
        JMenu network = new JMenu("Network");
        network.setMnemonic(KeyEvent.VK_N);
        multiplayer = new JMenuItem("Multiplayer", KeyEvent.VK_M);
        multiplayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        multiplayer.setIcon(createImageIcon("/wireless16.png"));
        multiplayer.addActionListener(this::wirelessClicked);
        network.add(multiplayer);
        menuBar.add(network, menuBar.getComponentCount() - 1);
	}
	
	@Override
	protected void createToolbar() {
		super.createToolbar();
		networkB = new JButton(createImageIcon("/wireless16.png"));
    	networkB.setToolTipText("Play with another player");
    	networkB.addActionListener(this::wirelessClicked);
    	networkB.setFocusPainted(false);
    	toolBar.add(networkB);
	}
	
    @Override
	protected void newClicked(ActionEvent e) {
		playClick();
		new NewPanel(this);
	}
	
	@Override
    protected void numberClicked(int number) {
    	super.numberClicked(number);
    	writeFill();
    }
	
	@Override
    protected void solveClicked(ActionEvent e) {
		super.solveClicked(e);
		netMan.writeSolve();
	}
    
	@Override
	protected void undoClicked(ActionEvent e) {
		super.undoClicked(e);
		writeFill();
	}
	
	@Override
	protected void redoClicked(ActionEvent e) {
		super.redoClicked(e);
		writeFill();
	}
	
	/** Writes a fill message if the update is valid. */
	private void writeFill() {
		int[] select = board.getLastSelected();
		if(!(select[0] == -1 || select[1] == -1))
			netMan.writeFill(select[0], select[1], board.getValue(select[0], select[1]));
	}
	
    /** ClickListener for network buttons.*/
    protected void wirelessClicked(ActionEvent e) {
		playClick();
		netWin.setVisible(!netWin.isVisible());
		netWin.floorScrollBar();
	}
    
    @Override
    protected void shutDown() {
    	super.shutDown();
    	netMan.writeQuit();
    	netWin.shutDown();
    	if(!netMan.shutDown())
    		System.exit(-1); // Force termination on error
    }
    
	/**
	 * @return {@link board}
	 */
	public Board getBoard() {
		return board;
	}
	
	@Override
	public void startNewBoard(int size, int difficulty) {
		super.startNewBoard(size, difficulty);
		netMan.writeNew(size, board.toArr());
	}
	
	/**
	 * Starts a Sudoku game board of a given size and populates it with a pre-made list.
	 * Note it does not generate a new message request as it is assumed this method is
	 * to construct a new board resulting from a new or join_ack message.
	 * @param size size of the board.
	 * @param list list of {x, y, z, state} tuples that define new cells.
	 */
	public void startNewBoard(int size, int[] list) {
		board = new Board(size, list);
		configureNewBoard();
		configureControlPanel();
	}
	
    /**
     * Queries the user for a Sudoku game board size and difficulty, 
     * then constructs and handles the Sudoku board game.
     * @param args not used.
     */
    public static void main(String[] args) {
    	new NewPanel();
    }

    /** Toggles the wireless button icon. */
	public void toggleConnection() {
		connected = !connected;
		if(connected) {
			multiplayer.setIcon(WIRELESS_G);
			networkB.setIcon(WIRELESS_G);
		} else {
			multiplayer.setIcon(WIRELESS_N);
			networkB.setIcon(WIRELESS_N);
		}
	}
}
