package edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p;

import javax.swing.JOptionPane;

import edu.utep.cs.cs3331.ard.sudoku.dialog.BoardPanel;
import edu.utep.cs.cs3331.ard.sudoku.model.Board;
import edu.utep.cs.cs3331.ard.sudoku_p2p.dialog.NetworkWindow;
import edu.utep.cs.cs3331.ard.sudoku_p2p.dialog.SudokuDialog;
import edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkAdapter.MessageType;

/**
 * Handles network messages.
 * 
 * @author Anthony DesArmier
 * @author Trevor McCarthy
 * @version 1.0
 * @see edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkAdapter.MessageType
 */
public class MessageHandler implements edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkAdapter.MessageListener {

	private SudokuDialog root;
	private Board board;
	private BoardPanel boardPanel;
	private NetworkAdapter network;
	private NetworkWindow netWin;
	private NetworkManager netMan;
	
	/**
	 * Create a new MessageHandler.
	 * @param root The SudokuDialog root of this program.
	 */
	public MessageHandler(NetworkWindow netWin, NetworkAdapter network, NetworkManager netMan) {
		this.netWin = netWin;
		this.root = netWin.getParent();
		board = root.getBoard();
		boardPanel = root.getBoardPanel();
		this.network = network;
		this.netMan = netMan;
	}

	/**
	 * Handles incoming messages. 
	 * @see edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkAdapter.MessageListener#messageReceived​(MessageType, int, int, int, int[])
	 */
	public void messageReceived(MessageType type, int x, int y, int z, int[] others) {
		switch (type) {
		case CLOSE:
			if(netMan.isConnected()) {
				netWin.logMessage("Peer has disconnected.");
				if(network != null)
					netMan.disconnect(false);
			}
			break;
		case FILL:
			if(board.update(new int[] {x, y, z}, true, true)) {
				boardPanel.repaint();
				netWin.logMessage(String.format("FILL accepted: x:%d, y:%d, z:%d.", x, y, z));
			}
			else
				netWin.logMessage(String.format("FILL rejected: x:%d, y:%d, z:%d.", x, y, z));
			network.writeFillAck(x, y, z);
			break;
		case FILL_ACK:
			if(netMan.checkLastFill(x, y, z))
				netWin.logMessage(String.format("FILL_ACK accepted: x:%d, y:%d, z:%d.", x, y, z));
			else {
				netWin.logMessage(String.format("FILL_ACK rejected: x:%d, y:%d, z:%d.", x, y, z));
				netMan.disconnect(false);
			}
			break;
		case JOIN:
			netWin.logMessage("Join request was recieved.");
			network.writeJoinAck(board.getSize(), board.toArr());
			break;
		case JOIN_ACK:
			if(x==1) {
				netWin.logMessage("Join request was accepted.");
				startNewBoard(y, others);
			}
			else if (x==0) {
				netWin.logMessage("Join request was refused.");
				netMan.disconnect(false);
			}
			break;
		case NEW:
			int n = JOptionPane.showConfirmDialog(null,
				    String.format("Peer has requested a new %dx%d game. Accept?", x, x),
				    "Start New Game",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null);
			if(n==0) {
				netWin.logMessage("Accepted a new game request.");
				startNewBoard(x, others);
				network.writeNewAck(true);
			}
			else {
				netWin.logMessage("Rejected a new game request.");
				network.writeNewAck(false);
			}
			break;
		case NEW_ACK:
			if(x==1) {
				updateBoardRef();
				netWin.logMessage("Peer has accepted new game request.");
			}
			else if(x==0) {
				netWin.logMessage("Peer rejected new game request.");
				netMan.disconnect(false);
			}
			break;
		case QUIT:
			netWin.logMessage("Peer has quit the game.");
			netMan.disconnect(false);
			break;
		case SOLVE:
			netWin.logMessage("Peer has used the solver.");
			root.showMessage("");
			if(board.solve(true))
				boardPanel.repaint();
	    	else
	    		root.showMessage("Not solvable");
			break;
		case UNKNOWN:
			netWin.logMessage("Unknown request was recieved.");
			break;
		default:
			netWin.logMessage("Unknown message was recieved.");
			break;
		}
	}
	
	/**
	 * Start a new board and update references around it.
	 * @param size size of the board.
	 * @param list of {x, y, z, state} tuples that define new cells.
	 */
	private void startNewBoard(int size, int... list) {
		root.startNewBoard(size, list);
		updateBoardRef();
	}
	
	/** Updates board references for when a new board is created. */
	private void updateBoardRef() {
		board = root.getBoard();
		boardPanel = root.getBoardPanel();
	}
}
