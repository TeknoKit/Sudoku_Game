package edu.utep.cs.cs3331.ard.sudoku.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.utep.cs.cs3331.ard.sudoku.model.Board;
import edu.utep.cs.cs3331.ard.sudoku.net.JsonClient;

/**
 * Dialog template for playing simple Sudoku games.
 *
 * @author		Yoonsik Cheon
 * @author		Anthony DesArmier
 * @author 		Trevor McCarthy
 * @version     1.2
 */
@SuppressWarnings("serial")
public class SudokuDialog extends JFrame {

    /** Default dimension of the dialog. */
    private final static Dimension DEFAULT_DIM = new Dimension(310, 430);
    
    /** Default size of the Sudoku game board. */
    private final static int DEFAULT_SIZE = 9;
    
    /** Default difficulty of the Sudoku game board. */
    private final static int DEFAULT_DIFFICULTY = 1;

    /** Relative path to the resource directory. */
    private final static String RES_DIR = "/";
    
    /** Click clip to be used on the panel. */
    private Clip clip;
    
    /** Sudoku board. */
    private Board board;

    /** Special panel to display a Sudoku board. */
    private BoardPanel boardPanel;

    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");

    /** Create a new dialog with default values. */
    public SudokuDialog() {
    	this(DEFAULT_DIM, DEFAULT_SIZE, DEFAULT_DIFFICULTY);
    }
    
    /** Create a new dialog with the default screen dimensions.
     * @param size Sudoku game board size.
     * @param difficulty Sudoku game difficulty.
     */
    public SudokuDialog(int size, int difficulty) {
    	this(DEFAULT_DIM, size, difficulty);
    }
    
    /** Create a new dialog.
     * @param dim dialog dimension.
     * @param size Sudoku game board size.
     * @param difficulty Sudoku game difficulty.
     */
    public SudokuDialog(Dimension dim, int size, int difficulty) {
        super("Sudoku");
        setSize(dim);
        //board = new Board(JsonClient.requestBoard(size, difficulty));
        board = new Board(size, difficulty);
        boardPanel = new BoardPanel(board, this::boardClicked);
        configureUI();
        configureSound();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

	/**
     * Callback to be invoked when a square of the board is clicked.
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     */
    private void boardClicked(int x, int y) {
		playClick();
		board.select(x, y);
		boardPanel.repaint();
    }
    
    /**
     * Callback to be invoked when a number button is clicked.
     * @param number clicked number (1-9), or 0 for "X".
     */
    private void numberClicked(int number) {
    	playClick();
    	board.update(number);
    	boardPanel.repaint();
    }
    
    /**
     * Callback to be invoked when a new button is clicked.
     * If the current game is over, start a new game of the given size;
     * otherwise, prompt the user for a confirmation and then proceed
     * accordingly.
     * @param size requested puzzle size, either 4 or 9.
     * @deprecated
     */
    private void newClicked(int size) {
    	int x = JOptionPane.showConfirmDialog(null, String.format("Start a new %dx%d game?", size, size),
    			"New Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    	if(x==0) {
    		List<Integer> levels = JsonClient.getInfo().getLevels();
    		int difficulty = JOptionPane.showOptionDialog(null, "Choose Difficulty", "New Game",
    				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, levels.toArray(), levels.get(0));
    		if(difficulty!=-1) {
    			difficulty = levels.get(difficulty);
    			this.dispose();
    			new SudokuDialog(DEFAULT_DIM, size, difficulty);
    		}
    	}
    }

    /**
     * Display the given string in the message bar.
     * @param msg message to be displayed.
     */
    private void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configure the UI. */
    private void configureUI() {
        setIconImage(createImageIcon("sudoku.png").getImage());
        setLayout(new BorderLayout());
        
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem newGame = new JMenuItem("New Game", /*new ImageIcon(""),*/ KeyEvent.VK_N);
        newGame.addActionListener(e -> new NewPanel(this));
        file.add(newGame);
        JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
        exit.addActionListener(e -> System.exit(0));
        file.add(exit);
        menuBar.add(file);
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem solve = new JMenuItem("Solve", /*new ImageIcon(""),*/ KeyEvent.VK_S);
        solve.addActionListener(e -> {
        	board.solve();
        	boardPanel.repaint();
        });
        help.add(solve);
        menuBar.add(help);
        setJMenuBar(menuBar);
        
        JPanel buttons = makeControlPanel();
        // border: top, left, bottom, right
        buttons.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        add(buttons, BorderLayout.NORTH);
        
        JPanel board = new JPanel();
        board.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        board.setLayout(new GridLayout(1,1));
        board.add(boardPanel);
        add(board, BorderLayout.CENTER);
        
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        add(msgBar, BorderLayout.SOUTH);
    }
    
    /** Configures sound clips. */
    private void configureSound() {
    	URL soundURL = getClass().getResource(RES_DIR + "click.wav");
    	try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
	}
    
    /** Plays a specific sound file. */
    private void playClick() {
    	if (clip.isRunning())
    		clip.stop();
    	clip.setFramePosition(0);
    	clip.start();
    }
      
    /** 
     * Create a control panel consisting of new and number buttons.
     * @return configured JPanel.
     */
    private JPanel makeControlPanel() {
    	JPanel newButtons = new JPanel(new FlowLayout());
    	JButton newButton = new JButton("New");
    	newButton.setFocusPainted(false);
    	newButton.addActionListener(e -> new NewPanel(this));
        newButtons.add(newButton);
        newButtons.setAlignmentX(LEFT_ALIGNMENT);
        
    	// buttons labeled 1, 2, ..., 9, and X.
    	JPanel numberButtons = new JPanel(new FlowLayout());
    	int maxNumber = board.getSize() + 1;
    	for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            button.setFocusPainted(false);
            button.setMargin(new Insets(0,2,0,2));
            button.addActionListener(e -> numberClicked(number));
    		numberButtons.add(button);
    	}
    	numberButtons.setAlignmentX(LEFT_ALIGNMENT);

    	JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(newButtons);
        content.add(numberButtons);
        return content;
    }

    /**
     * Create an image icon from the given image file.
     * @return configured ImageIcon.
     */
    private ImageIcon createImageIcon(String filename) {
        URL imageUrl = getClass().getResource(RES_DIR + filename);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl);
        }
        return null;
    }

    /**
     * Queries the user for a Sudoku game board size and difficulty, 
     * then constructs and handles the Sudoku board game.
     * @param args not used.
     */
    public static void main(String[] args) {
    	new NewPanel();
    }
}
