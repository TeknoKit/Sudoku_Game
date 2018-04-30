package edu.utep.cs.cs3331.ard.sudoku.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import edu.utep.cs.cs3331.ard.sudoku.model.Board;

/**
 * Dialog template for playing simple Sudoku games.

 * @author		Yoonsik Cheon
 * @author		Anthony DesArmier
 * @author 		Trevor McCarthy
 * @version     1.2
 */
@SuppressWarnings("serial")
public class SudokuDialog extends JFrame {

    /** Default dimension of the dialog. */
    protected final static Dimension DEFAULT_DIM = new Dimension(330, 430);
    /** Default size of the Sudoku game board. */
    private final static int DEFAULT_SIZE = 9;
    /** Default difficulty of the Sudoku game board. */
    private final static int DEFAULT_DIFFICULTY = 1;
    /** Wireless icon for unconnected status. */
    protected final ImageIcon WIRELESS_N = createImageIcon("/wireless16.png");
    /** Wireless icon for connected status. */
    protected final ImageIcon WIRELESS_G = createImageIcon("/wireless16green.png");
    /** Zero (0) icon. */
    private final ImageIcon ZERO = createImageIcon("/00s.png");
    /** One (1) icon. */
    private final ImageIcon ONE = createImageIcon("/01s.png");
    /** Two (2) icon. */
    private final ImageIcon TWO = createImageIcon("/02s.png");
    
    /** Click clip to be used on the panel. */
    private Clip clip;
    /** Sudoku board. */
    protected Board board;
    /** Special panel to display a Sudoku board. */
    protected BoardPanel boardPanel;
    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");
    /** List of buttons representing the number pad. */
    private ArrayList<JButton> numPad = new ArrayList<>();
    /** Whether the entire number pad needs to be enabled. */
	private boolean numPadEnable;
	
	private JMenuItem input;
	private JButton inputB;
	protected JToolBar toolBar;
	protected JMenuBar menuBar;
	protected JPanel mainPanel;
	private JPanel buttons;

    /** Create a new dialog with default values. */
    public SudokuDialog() {
    	this(DEFAULT_DIM, DEFAULT_SIZE, DEFAULT_DIFFICULTY);
    }
    
    /**
     * Create a new dialog with the default screen dimensions.
     * @param size Sudoku game board size.
     * @param difficulty Sudoku game difficulty.
     */
    public SudokuDialog(int size, int difficulty) {
    	this(DEFAULT_DIM, size, difficulty);
    }
    
    /**
     * Create a new dialog.
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
	 * Starts a Sudoku game board of a given size and difficulty.
	 * @param size size of the board.
	 * @param list list of {x, y, z, state} tuples that define new cells.
	 */
	public void startNewBoard(int size, int difficulty) {
		board = new Board(size, difficulty);
		configureNewBoard();
		configureControlPanel();
	}

	/** Configures a new boardPanel and replaces the old boardPanel. */
	protected void configureNewBoard() {
		int index = getComponentZOrder(boardPanel);
		mainPanel.remove(boardPanel);
		boardPanel = new BoardPanel(board, this::boardClicked);
        mainPanel.add(boardPanel, boardPanelConstraints(), index);
		validate();
		repaint();
	}
	
	/** Configures a new controlPanel and replaces the old controlPanel. */
	protected void configureControlPanel() {
		int index = getComponentZOrder(buttons);
		mainPanel.remove(buttons);
		buttons = makeControlPanel();
        mainPanel.add(buttons, controlPanelConstraints(), index);
        validate();
		repaint();
	}
	
    /**
     * Display the given string in the message bar.
     * @param msg message to be displayed.
     */
    public void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configure the UI. */
    private void configureUI() {
        setIconImage(createImageIcon("/sudoku.png").getImage());
        setLayout(new BorderLayout());
        
        createMenu();
        createToolbar();
    	
    	mainPanel = new JPanel(); 
    	mainPanel.setLayout(new GridBagLayout());
    	
        buttons = makeControlPanel();
        mainPanel.add(buttons, controlPanelConstraints());
        
        mainPanel.add(boardPanel, boardPanelConstraints());
        
        msgBar.setPreferredSize(new Dimension(120, 12));
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      
        mainPanel.add(msgBar, msgBarConstraints());
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private GridBagConstraints boardPanelConstraints() {
    	GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1.0; 
        c.gridx = 0;
        c.gridy = 1;
        return c;
    }
    
    private GridBagConstraints controlPanelConstraints() {
    	GridBagConstraints c = new GridBagConstraints();
    	c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 0;
        return c;
	}

	private GridBagConstraints msgBarConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.ipady = 12;
		c.gridx = 0;
		c.gridy = 2;
		return c;
	}

	/** Configures the menu. */
	protected void createMenu() {
		menuBar = new JMenuBar();
        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);
        JMenuItem newGame = new JMenuItem("New Game", KeyEvent.VK_N);
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newGame.setIcon(createImageIcon("/toolbarButtonGraphics/media/Play16.gif"));
        newGame.addActionListener(this::newClicked);
        game.add(newGame);
        game.addSeparator();
        JMenuItem undo = new JMenuItem("Undo", KeyEvent.VK_Z);
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        undo.setIcon(createImageIcon("/toolbarButtonGraphics/media/StepBack16.gif"));
        undo.addActionListener(this::undoClicked);
        game.add(undo);
        JMenuItem redo = new JMenuItem("Redo", KeyEvent.VK_Y);
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        redo.setIcon(createImageIcon("/toolbarButtonGraphics/media/StepForward16.gif"));
        redo.addActionListener(this::redoClicked);
        game.add(redo);
        game.addSeparator();
        JMenuItem exit = new JMenuItem("Quit", KeyEvent.VK_Q);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exit.setIcon(createImageIcon("/toolbarButtonGraphics/general/Stop16.gif"));
        exit.addActionListener(e -> shutDown());
        game.add(exit);
        menuBar.add(game);
        
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem check = new JMenuItem("Check Progress", KeyEvent.VK_C);
        check.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        check.setIcon(createImageIcon("/toolbarButtonGraphics/general/Information16.gif"));
        check.addActionListener(this::checkClicked);
        help.add(check);
        input = new JMenuItem("Input Guide", KeyEvent.VK_I);
        input.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        input.setIcon(ZERO);
        input.addActionListener(this::inputGuideClicked);
        help.add(input);
        JMenuItem solve = new JMenuItem("Solve", KeyEvent.VK_V);
        solve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        solve.setIcon(createImageIcon("/toolbarButtonGraphics/media/FastForward16.gif"));
        solve.addActionListener(this::solveClicked);
        help.add(solve);
        menuBar.add(help);
        setJMenuBar(menuBar);
	}

    /** Configures the toolbar. */
	protected void createToolbar() {
		toolBar = new JToolBar();
        JButton newGameB = new JButton(createImageIcon("/toolbarButtonGraphics/media/Play16.gif"));
        newGameB.setToolTipText("Start a new game");
    	newGameB.addActionListener(this::newClicked);
    	newGameB.setFocusPainted(false);
    	toolBar.add(newGameB);
    	JButton checkB = new JButton(createImageIcon("/toolbarButtonGraphics/general/Information16.gif"));
    	checkB.setToolTipText("Check if the board is solvable");
    	checkB.addActionListener(this::checkClicked);
    	checkB.setFocusPainted(false);
    	toolBar.add(checkB);
    	inputB = new JButton(ZERO);
    	inputB.setToolTipText("Toggle input guide help modes");
    	inputB.addActionListener(this::inputGuideClicked);
    	inputB.setFocusPainted(false);
    	toolBar.add(inputB);
    	JButton solveB = new JButton(createImageIcon("/toolbarButtonGraphics/media/FastForward16.gif"));
    	solveB.setToolTipText("Solve the board (if possible)");
    	solveB.addActionListener(this::solveClicked);
    	solveB.setFocusPainted(false);
    	toolBar.add(solveB);
    	JButton undoB = new JButton(createImageIcon("/toolbarButtonGraphics/media/StepBack16.gif"));
    	undoB.setToolTipText("Undo an action");
    	undoB.addActionListener(this::undoClicked);
    	undoB.setFocusPainted(false);
    	toolBar.add(undoB);
    	JButton redoB = new JButton(createImageIcon("/toolbarButtonGraphics/media/StepForward16.gif"));
    	redoB.setToolTipText("Redo an action");
    	redoB.addActionListener(this::redoClicked);
    	redoB.setFocusPainted(false);
    	toolBar.add(redoB);
    	add(toolBar, BorderLayout.NORTH);
	}
    
    /** 
     * Create a control panel consisting of number buttons.
     * @return configured JPanel.
     */
    private JPanel makeControlPanel() {
    	numPadEnable = false;
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
    		numPad.add(button);
    	}
    	return numberButtons;
    }
    
    /** Configures the number pad buttons to be enabled or disabled based on the board guide state and selected cell. */
	private void handlePadEnables() {
		if(board.getLastSelectedFixed()) {
			for (JButton num : numPad)
				num.setEnabled(false);
			numPadEnable = true;
		}
		else if(board.getGuideMode() == 0) {
			if(numPadEnable)
				for (JButton num : numPad)
					num.setEnabled(true);
			else return;
		}
		else if(board.getGuideMode() == 1 && board.cellSelected()) {
			ArrayList<Integer> invalids = board.invalidInputs();
			int n = 1;
			for(JButton num : numPad) {
				if (invalids.contains(n))
					num.setEnabled(false);
				else
					num.setEnabled(true);
				n++;
			}
			numPadEnable = true;
		} else if(numPadEnable && board.getGuideMode() == 2 && board.cellSelected()) {
			for (JButton num : numPad)
				num.setEnabled(true);
			numPadEnable = false;
		}
	}
    
    /** Configures sound clips. */
    private void configureSound() {
    	URL soundURL = getClass().getResource("/click.wav");
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
    protected void playClick() {
    	if (clip.isRunning())
    		clip.stop();
    	clip.setFramePosition(0);
    	clip.start();
    }
	
    /**
     * Create an image icon from the given image file.
     * @return configured ImageIcon.
     */
    protected ImageIcon createImageIcon(String filename) {
        //URL imageUrl = getClass().getResource(filename);
    	BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(filename));
		} catch (IOException e) {
			System.out.println("Error: File " + filename + " not found!");
		}
        if (image != null)
            return new ImageIcon(image);
        return null;
    }
    
    /** Properly shuts down this object and all related streams. */
    protected void shutDown() {
    	clip.close();
    	dispose();
    }

    /**
     * Queries the user for a Sudoku game board size and difficulty, 
     * then constructs and handles the Sudoku board game.
     * @param args not used.
     */
    public static void main(String[] args) {
    	new NewPanel();
    }
    
    /** ClickListener for newGame buttons. */
	protected void newClicked(ActionEvent e) {
		playClick();
		new NewPanel(this);
	}
    
	/**
     * Callback to be invoked when a square of the board is clicked.
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     */
    protected void boardClicked(int x, int y) {
		playClick();
		board.select(x, y);
		boardPanel.repaint();
		showMessage("");
		if(!board.isSameSelected())
			handlePadEnables();
    }
    
    /**
     * Callback to be invoked when a number button is clicked.
     * @param number clicked number (1-9), or 0 for "X".
     */
    protected void numberClicked(int number) {
    	playClick();
    	board.update(number, true);
    	boardPanel.repaint();
    	showMessage("");
    }
    
    /** ClickListener for check buttons.*/
    private void checkClicked(ActionEvent e) {
		playClick();
		if(board.solve(false))
    		showMessage("Solvable");
    	else
    		showMessage("Not solvable");
	}
    
    /** ClickListener for input guide buttons.*/
    private void inputGuideClicked(ActionEvent e) {
		playClick();
		board.incGuideMode();
    	boardPanel.repaint();
		handlePadEnables();
		switch (board.getGuideMode()) {
			case 0:
				input.setIcon(ZERO);
				inputB.setIcon(ZERO);
				break;
			case 1:
				input.setIcon(ONE);
				inputB.setIcon(ONE);
				break;
			case 2:
				input.setIcon(TWO);
				inputB.setIcon(TWO);
				break;
			default: System.out.println("Could not set InputGuide icon. Invalid input guide state: " + board.getGuideMode());
		}
	}
	
    /** ClickListener for solve buttons.*/
    protected void solveClicked(ActionEvent e) {
    	showMessage("");
		playClick();
		if(board.solve(true))
			boardPanel.repaint();
    	else
    		showMessage("Not solvable");
	}
	
    /** ClickListener for undo buttons.*/
    protected void undoClicked(ActionEvent e) {
		playClick();
		board.undo();
		boardPanel.repaint();
	}
	
    /** ClickListener for redo buttons.*/
    protected void redoClicked(ActionEvent e) {
		playClick();
		board.redo();
		boardPanel.repaint();
    }
}
