package edu.utep.cs.cs3331.ard.sudoku.dialog;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * A window providing networking functions.
 * @author Anthony
 * @version 0.5
 */
public class NetworkWindow extends JDialog {
	/** Number of columns of the text fields. */
	private int textFieldCol = 18;
	/** Host address of this machine. */
	InetAddress host;
	/** Default server port to use. */
	int port = 8000;
	/** Connection status. */
	boolean connected;
	
	private JButton connect;
	private JButton disconnect;
	private JTextArea log;
	
	/** Create a new NetworkWindow. */
	public NetworkWindow() {
		connected = false;
		try {
			host = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("IP address of this host could not be determined.");
		}
		configureUI();
		setTitle("Connection");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	         @Override
	         public void windowClosing(WindowEvent e) {
	            shutDown();
	         }
	    });
        setLocationRelativeTo(null);
        setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Retrieves the computer's name.
	 * @return the computer's name.
	 */
	static String getComputerName()
	{
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
		pane.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
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
		JTextField playerIPText = new JTextField(host.getHostAddress());
		playerIPText.setColumns(textFieldCol);
		playerIPText.setEditable(false);
		player.add(playerIPText);
		JLabel playerPortLabel = new JLabel("Port:");
		player.add(playerPortLabel);
		JTextField playerPortText = new JTextField(String.valueOf(port));
		playerPortText.setColumns(textFieldCol);
		playerPortText.setEditable(false);
		player.add(playerPortText);
		
		layout.putConstraint(SpringLayout.WEST, hostLabel, 4, SpringLayout.WEST, player);
		layout.putConstraint(SpringLayout.WEST, playerIPLabel, 4, SpringLayout.WEST, player);
		layout.putConstraint(SpringLayout.WEST, playerPortLabel, 4, SpringLayout.WEST, player);
		
		layout.putConstraint(SpringLayout.WEST, hostText, 4, SpringLayout.EAST, hostLabel);
		layout.putConstraint(SpringLayout.WEST, playerIPText, 4, SpringLayout.EAST, playerIPLabel);
		layout.putConstraint(SpringLayout.WEST, playerPortText, 4, SpringLayout.EAST, playerPortLabel);
		
		layout.putConstraint(SpringLayout.EAST, player, 4, SpringLayout.EAST, hostText);
		layout.putConstraint(SpringLayout.EAST, player, 4, SpringLayout.EAST, playerIPText);
		layout.putConstraint(SpringLayout.EAST, player, 4, SpringLayout.EAST, playerPortText);
		
		layout.putConstraint(SpringLayout.NORTH, hostText, 4, SpringLayout.NORTH, player);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, hostLabel, 0, SpringLayout.VERTICAL_CENTER, hostText);
		
		layout.putConstraint(SpringLayout.NORTH, playerIPText, 4, SpringLayout.SOUTH, hostText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, playerIPLabel, 0, SpringLayout.VERTICAL_CENTER, playerIPText);
		
		layout.putConstraint(SpringLayout.NORTH, playerPortText, 4, SpringLayout.SOUTH, playerIPText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, playerPortLabel, 0, SpringLayout.VERTICAL_CENTER, playerPortText);
		
		layout.putConstraint(SpringLayout.SOUTH, player, 4, SpringLayout.SOUTH, playerPortText);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(4,4,4,4);
		pane.add(player, c);
		
		c = new GridBagConstraints();
		JPanel peer = new JPanel(layout);
		peer.setBorder(BorderFactory.createTitledBorder("Peer"));
		JLabel peerIPLabel = new JLabel("Host name/IP:");
		peer.add(peerIPLabel);
		JTextField peerIPText = new JTextField("localhost");
		peerIPText.setColumns(textFieldCol);
		peer.add(peerIPText);
		JLabel peerPortLabel = new JLabel("Port:");
		peerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		peer.add(peerPortLabel);
		JTextField peerPortText = new JTextField(port);
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
		layout.putConstraint(SpringLayout.WEST, peerPortText, 4, SpringLayout.EAST, peerPortLabel);
		
		layout.putConstraint(SpringLayout.EAST, peer, 4, SpringLayout.EAST, peerIPText);
		layout.putConstraint(SpringLayout.EAST, peer, 4, SpringLayout.EAST, playerPortText);
		
		layout.putConstraint(SpringLayout.NORTH, peerIPText, 4, SpringLayout.NORTH, peer);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, peerIPLabel, 0, SpringLayout.VERTICAL_CENTER, peerIPText);
		
		layout.putConstraint(SpringLayout.NORTH, peerPortText, 4, SpringLayout.SOUTH, peerIPText);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, peerPortLabel, 0, SpringLayout.VERTICAL_CENTER, peerPortText);
		
		layout.putConstraint(SpringLayout.NORTH, connection, 8, SpringLayout.SOUTH, peerPortText);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connection, 0, SpringLayout.HORIZONTAL_CENTER, peer);
		
		layout.putConstraint(SpringLayout.SOUTH, peer, 4, SpringLayout.SOUTH, connection);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0,4,4,4);
		pane.add(peer, c);
		
		c = new GridBagConstraints();
		log = new JTextArea();
		log.setBorder(BorderFactory.createEtchedBorder());
		log.setEditable(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 120;
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(0,4,4,4);
		pane.add(log, c);
		layout.putConstraint(SpringLayout.EAST, log, -6, SpringLayout.EAST, pane);
		
		c = new GridBagConstraints();
		JPanel close = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this::closeClicked);
		close.add(closeButton);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		c.insets = new Insets(0,4,4,4);
		c.anchor = GridBagConstraints.PAGE_END;
		pane.add(close, c);
		// Set Labels to size of largest label
		hostLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		playerIPLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		playerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		peerPortLabel.setPreferredSize(peerIPLabel.getPreferredSize());
		
		pack();
	}
	
	/** Properly shuts down this object and all related streams. */
    public void shutDown() {
    	dispose();
    }
	
	/** ClickListener for connect button.*/
	private void connectClicked(ActionEvent e) {
		connected = true;
		connect.setEnabled(false);
		disconnect.setEnabled(true);
	}
	
	/** ClickListener for disconnect button.*/
	private void disconnectClicked(ActionEvent e) {
		connected = false;
		disconnect.setEnabled(false);
		connect.setEnabled(true);
	}
	
	/** ClickListener for close button.*/
	private void closeClicked(ActionEvent e) {
		shutDown();
	}
	
	public static void main(String[] args) {
		new NetworkWindow();
	}

}
