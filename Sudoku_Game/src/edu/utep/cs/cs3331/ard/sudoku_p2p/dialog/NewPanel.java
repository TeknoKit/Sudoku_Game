package edu.utep.cs.cs3331.ard.sudoku_p2p.dialog;

public class NewPanel extends edu.utep.cs.cs3331.ard.sudoku.dialog.NewPanel {
	private static final long serialVersionUID = -7228876505738706463L;

	/** Constructs a NewPanel without a parent frame. */
	public NewPanel() {
		super(null);
	}
	
	/** Constructs a NewPanel with a parent {@link SudokuDialog}. */
	public NewPanel(SudokuDialog parent) {
		super(parent);
	}
	
	@Override
	protected void createDialog(int size, int difficulty) {
		if(parent == null)
			new SudokuDialog(size, difficulty);
		else
			parent.startNewBoard(size, difficulty);
	}
}
