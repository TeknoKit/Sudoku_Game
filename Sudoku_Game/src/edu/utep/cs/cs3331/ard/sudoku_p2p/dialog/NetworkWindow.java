package edu.utep.cs.cs3331.ard.sudoku_p2p.dialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p.NetworkManager;

/**
 * Provides networking control.
 * 
 * @author Anthony DesArmier
 * @version 1.5
 */
public class NetworkWindow extends JFrame {
	private static final long serialVersionUID = -3450758535260468131L;
	/** Number of columns of the text fields. */
	private int textFieldCol = 18;
	/** Host address of this machine. */
	private String host;
	//InetAddress host;
	/** Default server port. */
	public int port = 8000;
	
	/** Attached NetWorkManager. */
	NetworkManager netMan;
	/** Parent SudokuDialog window. */
	private SudokuDialog parent;
	
	/** Connect button. */
	private JButton connect;
	/** Disconnect button. */
	private JButton disconnect;
	/** Network message log. */
	private JTextArea log;
	/** Text field containing the IP address to connect to. */
	private JTextField peerIPText;
	/** Text field containing the port number to connect through. */
	private JTextField peerPortText;
	/** Text field containing the IP address of the host. */
	private JTextField playerIPText;
	/** Text field containing the port number of the host. */
	private JTextField playerPortText;
	/** ScrollPane for the log to reside in. */
	private JScrollPane logPane;

	/** Create a new NetworkWindow. */
	public NetworkWindow(SudokuDialog parent) {
		this.parent = parent;
		URL whatismyip = null;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
		} catch (MalformedURLException e1) {}
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			host = in.readLine();
		} catch (IOException e1) {}
		/*try {
			host = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("IP address of this host could not be determined.");
		}*/
		configureUI();
		setTitle("Connection");
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		netMan = new NetworkManager(this);
		netMan.startServer();
	}

	/**
	 * Retrieves the computer's name.
	 * @return the computer's name.
	 */
	static String getComputerName() {
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else
			return "Unknown Computer";
	}

	/** Configures the NetWorkWindow. */
	private void configureUI() {
		setIconImage(null);
		Container pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		SpringLayout layout = new SpringLayout();
		JPanel player = new JPanel(layout);
		player.setBorder(BorderFactory.createTitledBorder("Player"));
		JLabel hostLabel = new JLabel("Host name:");
		player.add(hostLabel);
		JTextField hostText = new JTextField(getComputerName());
		hostText.setColumns(textFieldCol);
		hostText.setEditable(false);
		player.add(hostText);
		JLabel playerIPLabel = new JLabel("IP:");
		player.add(playerIPLabel);
		//playerIPText = new JTextField(host.getHostAddress());
		playerIPText = new JTextField(host);
		playerIPText.setColumns(textFieldCol);
		playerIPText.setEditable(false);
		player.add(playerIPText);
		JLabel playerPortLabel = new JLabel("Port:");
		player.add(playerPortLabel);
		playerPortText = new JTextField(String.valueOf(port));
		playerPortText.setColumns(textFieldCol);
		playerPortText.setEditable(false);
		player.add(playerPortText);

		layout.putConstraint(SpringLayout.WEST, hostLabel, 4, SpringLayout.WEST, player);
		layout.putConstraint(SpringLayout.WEST, playerIPLabel, 4, SpringLayout.WEST, player);
		layout.putConstraint(SpringLayout.WEST, playerPortLabel, 4, SpringLayout.WEST, player);

		layout.putConstraint(SpringLayout.WEST, hostText, 4, SpringLayout.EAST, hostLabel);
		layout.putConstraint(SpringLayout.WEST, playerIPText, 0, SpringLayout.WEST, hostText);
		layout.putConstraint(SpringLayout.WEST, playerPortText, 0, SpringLayout.WEST, hostText);

		layout.putConstraint(SpringLayout.EAST, player, 4, SpringLayout.EAST, hostText);
		layout.putConstraint(SpringLayout.EAST, playerIPText, 0, SpringLayout.EAST, hostText);
		layout.putConstraint(SpringLayout.EAST, playerPortText, 0, SpringLayout.EAST, hostText);

		layout.putConstraint(SpringLayout.NORTH, hostText, 4, SpringLayout.NORTH, player);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, hostLabel, 0, SpringLayout.VERTICAL_CENTER, hostText);

		layout.putConstraint(SpringLayout.NORTH, playerIPText, 4, SpringLayout.SOUTH, hostText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, playerIPLabel, 0, SpringLayout.VERTICAL_CENTER,
				playerIPText);

		layout.putConstraint(SpringLayout.NORTH, playerPortText, 4, SpringLayout.SOUTH, playerIPText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, playerPortLabel, 0, SpringLayout.VERTICAL_CENTER,
				playerPortText);

		layout.putConstraint(SpringLayout.SOUTH, player, 4, SpringLayout.SOUTH, playerPortText);
		
		pane.add(player);

		JPanel peer = new JPanel(layout);
		peer.setBorder(BorderFactory.createTitledBorder("Peer"));
		JLabel peerIPLabel = new JLabel("Host name/IP:");
		peer.add(peerIPLabel);
		peerIPText = new JTextField("localhost");
		peerIPText.setColumns(textFieldCol);
		peer.add(peerIPText);
		JLabel peerPortLabel = new JLabel("Port:");
		peerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		peer.add(peerPortLabel);
		peerPortText = new JTextField(port);
		peerPortText.setColumns(textFieldCol);
		peer.add(peerPortText);
		JPanel connection = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
		connect = new JButton("Connect");
		connect.addActionListener(this::connectClicked);
		connection.add(connect);
		disconnect = new JButton("Disconnect");
		disconnect.setEnabled(false);
		disconnect.addActionListener(this::disconnectClicked);
		connection.add(disconnect);
		peer.add(connection);

		layout.putConstraint(SpringLayout.WEST, peerIPLabel, 4, SpringLayout.WEST, peer);
		layout.putConstraint(SpringLayout.WEST, peerPortLabel, 4, SpringLayout.WEST, peer);

		layout.putConstraint(SpringLayout.WEST, peerIPText, 4, SpringLayout.EAST, peerIPLabel);
		layout.putConstraint(SpringLayout.WEST, peerPortText, 0, SpringLayout.WEST, peerIPText);

		layout.putConstraint(SpringLayout.EAST, peer, 4, SpringLayout.EAST, peerIPText);
		layout.putConstraint(SpringLayout.EAST, peerPortText, 0, SpringLayout.EAST, peerIPText);

		layout.putConstraint(SpringLayout.NORTH, peerIPText, 4, SpringLayout.NORTH, peer);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, peerIPLabel, 0, SpringLayout.VERTICAL_CENTER, peerIPText);

		layout.putConstraint(SpringLayout.NORTH, peerPortText, 4, SpringLayout.SOUTH, peerIPText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, peerPortLabel, 0, SpringLayout.VERTICAL_CENTER,
				peerPortText);

		layout.putConstraint(SpringLayout.NORTH, connection, 8, SpringLayout.SOUTH, peerPortText);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connection, 0, SpringLayout.HORIZONTAL_CENTER, peer);

		layout.putConstraint(SpringLayout.SOUTH, peer, 4, SpringLayout.SOUTH, connection);
		
		pane.add(peer);

		log = new JTextArea();
		log.setLineWrap(true);
		log.setWrapStyleWord(true);
		// log.setBorder(BorderFactory.createEtchedBorder());
		log.setEditable(false);
		logPane = new JScrollPane(log);
		Border border = logPane.getBorder();
	    Border margin = new EmptyBorder(4,4,4,4);
		logPane.setBorder(new CompoundBorder(margin, border));
		logPane.setPreferredSize(new Dimension(peer.getWidth(), 200));
		pane.add(logPane);
		
		JPanel close = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this::closeClicked);
		close.add(closeButton);
		border = close.getBorder();
	    margin = new EmptyBorder(0,0,4,4);
		close.setBorder(new CompoundBorder(margin, border));
		pane.add(close);
		
		// Set Labels to size of largest label
		hostLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		playerIPLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		playerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		peerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		
		pack();
		setMinimumSize(getSize());
	}
	
	/** Toggles connection status. */
	public void toggleConnection() {
		connect.setEnabled(!connect.isEnabled());
		disconnect.setEnabled(!disconnect.isEnabled());
		peerIPText.setEditable(!peerIPText.isEditable());
		peerPortText.setEditable(!peerPortText.isEditable());
		parent.toggleConnection();
	}

	/** Logs a message to the log TestArea. */
	public void logMessage(String message) {
		log.append(">" + message + "\n");
		floorScrollBar();
	}

	/** Sets the scroll bar of the logPane to the bottom. */
	public void floorScrollBar() {
		JScrollBar vertical = logPane.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
	
	/** ClickListener for connect button. */
	private void connectClicked(ActionEvent e) {
		if(!getPeerIP().isEmpty() && !peerPortText.getText().isEmpty()) {
			if(netMan.connect()) {
				logMessage(String.format("Connected to %s:%d", getPeerIP(), getPeerPort()));
				
			}
		} else {
			logMessage("Please enter a valid IP and port number.");
		}
	}

	/** ClickListener for disconnect button. */
	private void disconnectClicked(ActionEvent e) {
		int port = getPeerPort();
		if (netMan.disconnect(false)) {
			logMessage(String.format("Disconnected from %s:%d", getPeerIP(), port));
		}
	}

	/** ClickListener for close button. */
	private void closeClicked(ActionEvent e) {
		setVisible(false);
	}

	/**
	 * @return {@link parent}
	 */
	public SudokuDialog getParent() {
		return parent;
	}

	/**
	 * @param parent {@link parent}
	 */
	public void setParent(SudokuDialog parent) {
		this.parent = parent;
	}

	/**
	 * @return String value of {@link peerIPText}
	 */
	public String getPeerIP() {
		return peerIPText.getText();
	}

	/**
	 * @param peerIPText String value of {@link peerIPText}
	 */
	public void setPeerIP(String peerIPText) {
		this.peerIPText.setText(peerIPText);
	}

	/**
	 * @return int value of {@link peerPortText}
	 */
	public int getPeerPort() {
		return Integer.valueOf(peerPortText.getText());
	}

	/**
	 * @param parts String value of {@link peerPortText}
	 */
	public void setPeerPort(String peerPortText) {
		this.peerPortText.setText(peerPortText);
	}

	/**
	 * @return String value of {@link playerIPText}
	 */
	public String getPlayerIP() {
		return playerIPText.getText();
	}

	/**
	 * @param playerIPText String value of {@link playerIPText}
	 */
	public void setPlayerIP(String playerIPText) {
		this.playerIPText.setText(playerIPText);
	}

	/**
	 * @return String value of {@link playerPortText}
	 */
	public int getPlayerPort() {
		return Integer.valueOf(playerPortText.getText());
	}

	/**
	 * @param playerPortText String value of {@link playerPortText}
	 */
	public void setPlayerPort(String playerPortText) {
		this.playerPortText.setText(playerPortText);
	}

	/** Properly shuts down this window. */
	public void shutDown() {
		dispose();
	}

}
