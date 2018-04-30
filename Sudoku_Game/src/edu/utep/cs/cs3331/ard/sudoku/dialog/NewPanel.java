package edu.utep.cs.cs3331.ard.sudoku.dialog;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A user prompt for a board size and difficulty to create a new Sudoku board game.
 * @author Anthony DesArmier
 * @version 1.0
 */
@SuppressWarnings("serial")
public class NewPanel extends JFrame {
	private JLabel diffLabel, sizeLabel;
	private JRadioButton s1, s2, d1, d2, d3;
	private JButton b1, b2;
	protected SudokuDialog parent;
	
	/** Constructs a NewPanel without a parent frame. */
	public NewPanel() {
		this(null);
	}

	/** Constructs a NewPanel with a parent {@link SudokuDialog}. */
	public NewPanel(SudokuDialog parent) {
		this.parent = parent;
		setTitle("New Game");
		configureUI(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
		setVisible(true);
	}

	/** Configures the NewPanel. */
	private void configureUI(SudokuDialog parent) {
		JPanel sizePanel = new JPanel();
		sizeLabel = new JLabel("Board Size:", JLabel.LEFT);
		s1 = new JRadioButton("4x4");
		s2 = new JRadioButton("9x9");
		ButtonGroup sizeGroup = new ButtonGroup();
		sizeGroup.add(s1); sizeGroup.add(s2);
		
		JPanel diffPanel = new JPanel();
		diffLabel = new JLabel("Difficulty Level:", JLabel.LEFT);
		d1 = new JRadioButton("Easy");
		d2 = new JRadioButton("Normal");
		d3 = new JRadioButton("Hard");
		ButtonGroup diffGroup = new ButtonGroup();
		diffGroup.add(d1); diffGroup.add(d2); diffGroup.add(d3);
		
		JPanel buttons = new JPanel();
		b1 = new JButton("Play"); 
		b1.addActionListener(e -> {
				int size = -1;
				int difficulty = -1;
				if(s1.isSelected()) size = 4;
				else if(s2.isSelected()) size = 9;
				if(d1.isSelected()) difficulty = 1;
				else if(d2.isSelected()) difficulty = 2;
				else if(d3.isSelected()) difficulty = 3;
				if(size!=-1 && difficulty!=-1) {
					dispose();
					//if(parent!=null)
						//parent.shutDown();
					createDialog(size, difficulty);
				}
		});
		b2 = new JButton("Cancel");
		b2.addActionListener(e -> dispose());
		
		sizePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		diffPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		sizePanel.add(sizeLabel); sizePanel.add(s1); sizePanel.add(s2);
		diffPanel.add(diffLabel); diffPanel.add(d1); diffPanel.add(d2); diffPanel.add(d3);
		buttons.add(b1); buttons.add(b2);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(sizePanel);
		panel.add(diffPanel);
		panel.add(buttons);
		
		add(panel);
		
		pack();
	}

	/**
	 * Constructs a new {@link SudokuDialog}.
	 * @param size Sudoku game board size.
     * @param difficulty Sudoku game difficulty.
	 */
	protected void createDialog(int size, int difficulty) {
		if(parent == null)
			new SudokuDialog(size, difficulty);
		else
			parent.startNewBoard(size, difficulty);
	}
}
