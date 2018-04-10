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
	
	/** Contructs a NewPanel without a parent frame. */
	public NewPanel() {
		this(null);
	}

	/** Constructs a NewPanel with a parent {@link SudokuDialog}. */
	public NewPanel(SudokuDialog parent) {
		super("New Game");
		configureUI(parent);
	}

	/** Configures and displays the NewPanel. */
	private void configureUI(SudokuDialog parent) {
		JPanel size = new JPanel();
		sizeLabel = new JLabel("Difficulty Level:", JLabel.LEFT);
		s1 = new JRadioButton("4x4");
		s2 = new JRadioButton("9x9");
		ButtonGroup sizeGroup = new ButtonGroup();
		sizeGroup.add(s1); sizeGroup.add(s2);
		
		JPanel diff = new JPanel();
		diffLabel = new JLabel("Board Size:", JLabel.LEFT);
		d1 = new JRadioButton("Easy");
		d2 = new JRadioButton("Normal");
		d3 = new JRadioButton("Hard");
		ButtonGroup diffGroup = new ButtonGroup();
		diffGroup.add(d1); diffGroup.add(d2); diffGroup.add(d3);
		
		JPanel buttons = new JPanel();
		b1 = new JButton("Play"); 
		b1.addActionListener(e -> {
				int sizeVal = -1;
				int diffVal = -1;
				if(s1.isSelected()) sizeVal = 4;
				else if(s2.isSelected()) sizeVal = 9;
				if(d1.isSelected()) diffVal = 1;
				else if(d2.isSelected()) diffVal = 2;
				else if(d3.isSelected()) diffVal = 3;
				if(sizeVal!=-1 && diffVal!=-1) {
					dispose();
					if(parent!=null) {
						parent.shutDown();
					}
					new SudokuDialog(sizeVal, diffVal);
				}
		});
		b2 = new JButton("Cancel");
		b2.addActionListener(e -> dispose());
		
		size.setAlignmentX(Component.CENTER_ALIGNMENT);
		diff.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		size.add(sizeLabel); size.add(s1); size.add(s2);
		diff.add(diffLabel); diff.add(d1); diff.add(d2); diff.add(d3);
		buttons.add(b1); buttons.add(b2);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(size);
		panel.add(diff);
		panel.add(buttons);
		
		add(panel);
		
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
		setVisible(true);
	}
}
