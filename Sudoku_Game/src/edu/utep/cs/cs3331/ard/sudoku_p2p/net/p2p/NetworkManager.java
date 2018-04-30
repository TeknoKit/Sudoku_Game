package edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.utep.cs.cs3331.ard.sudoku_p2p.dialog.NetworkWindow;

/**
 * Provides various socket creating and handling.
 * 
 * @author Anthony DesArmier
 * @author Trevor McCarthy
 * @version 1.0
 */
public class NetworkManager {
	
	/** Network adapter to handle logic. */
	public NetworkAdapter network;
	/** Attached NetworkWindow. */
	private NetworkWindow netWin;
	/** Server thread. */
	private Thread server;
	/** Client thread. */
	private Thread client;
	/** Server thread. */
	private ServerSocket serverSocket;
	/** Port number. */
	private int port;
	
	/** Last fill message. */
	private int[] lastFill;
	/** Server thread counter. */
	private int serverNum;
	/** Connection status of this networkManager. */
	private boolean connected;
	
	/**
	 * Creates a new NetworkManager
	 * @param netWin The attached NetworkWindow to communicate with.
	 */
	public NetworkManager (NetworkWindow netWin) {
		this.netWin = netWin;
		port = 8000;
		lastFill = new int[3];
		connected = true;
	}

	/** Starts a new server socket on a new thread. */
	public void startServer() {
		port = 8000;
		server = new Thread(() -> {
			boolean connecting = true;
			while (connecting) {
				try {
					serverSocket = new ServerSocket(port);
					netWin.setPlayerPort(String.valueOf(serverSocket.getLocalPort()));
					netWin.logMessage("Server started...");
					connecting = false;
					connected = true;
					Socket clientSocket = serverSocket.accept(); // Listen for a connection
					serverSocket.close();
					String address = clientSocket.getRemoteSocketAddress().toString();
					String[] parts = address.split(":");
					netWin.setPeerIP(parts[0].substring(1));
					netWin.setPeerPort(parts[1]);
					netWin.toggleConnection();
					netWin.logMessage(
							String.format("%s has connected", address));
					pairSocket(clientSocket, false);
				} catch (Exception exc) {
					port++;
				}
				
			}
		});
		server.setName("Server_Thread-"+serverNum++);
		server.start();
	}
	
	/**
	 * Pairs a socket with a MessageHandler and begins listening for messages.
	 * @param socket the socket to pair.
	 * @param client true the socket is originating as a client or false if a server.
	 */
	private synchronized void pairSocket(Socket socket, boolean client) {
		network = new NetworkAdapter(socket);
		network.setMessageListener(new MessageHandler(netWin, network, this));
		if(client)
			network.writeJoin();
		network.receiveMessages(); // loop till disconnected
	}
	
	/**
	 * Attempts to connect a client socket with a peer.
	 * @return true is successful, false otherwise.
	 */
	public boolean connect() {
		if(netWin.getPeerPort() == port) {
			netWin.logMessage("Cannot self connect.");
			return false;
		}
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(netWin.getPeerIP(), netWin.getPeerPort()), 3000);
		} catch (Exception exc) {
			netWin.logMessage(String.format("Failed to connect to %s:%d", netWin.getPeerIP(), netWin.getPeerPort()));
			return false;
		}
		client = new Thread(() -> {
			pairSocket(socket, true);
		});
		client.setName("Client_Thread");
		client.start();
		shutDownServer(); // Terminate the server
		connected = true;
		netWin.toggleConnection();
		return true;
	}
	
	/**
	 * Properly closes the NetworkAdapter.
	 * @param hard true if this is a hard disconnect with no intention of starting a new connection.
	 */
	public boolean disconnect(boolean hard) {
		if(network != null) {
			connected = false;
			netWin.logMessage(
					String.format("Disconnecting..."));
			network.close();
			try {
				network.socket().close();
			} catch (IOException e) {
				netWin.logMessage(
						String.format("Failed to disconnect from %s:%d", netWin.getPeerIP(), netWin.getPeerPort()));
				return false;
			}
			network = null; // Trash this network
			//netWin.setPeerPort(""); // If we wish to clear the port field on a disconnection
			netWin.toggleConnection();
		}
		if(!hard)
			startServer(); // Start a new server
		return true;
	}
	
	/** Shuts down an open ServerSocket. */
	private boolean shutDownServer() {
		if(serverSocket != null) { // Shutdown the server if it is running
			try {
				serverSocket.close();
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
	/** Properly shuts down this NetWorkManager. */
	public boolean shutDown() {
		return disconnect(true) && shutDownServer();
	}
	
	/** Write a solve message if a network exists. */
	public void writeSolve() {
		if(network != null)
			network.writeSolve();
	}

	/**
     * Write a fill message if a network exists. 
     * @param x 0-based column index of the square.
     * @param y 0-based row index of the square.
     * @param number filled-in number.
     */
	public void writeFill(int x, int y, int number) {
		if(network != null) {
			network.writeFill(x, y, number);
			setLastFill(x, y, number);
		}
	}
	
	/**
	 * Checks if this fill_ack message matches the last sent fill message.
	 * @param x 0-based column index of the square
     * @param y 0-based row index of the square
     * @param number Filled-in number
	 * @return true if the messages match, false otherwise.
	 */
	boolean checkLastFill(int x, int y, int number) {
		return lastFill[0] == x && lastFill[1] == y && lastFill[2] == number;
	}
	
	/** 
	 * Stores the lastFilled message to verify in a fill-ack message.
	 * @see #checkLastFill(int, int, int)
	 */
	private void setLastFill(int x, int y, int number) {
		lastFill[0] = x;
		lastFill[1] = y;
		lastFill[2] = number;
	}
	
    /** Write a quit message if a network exists. */
    public void writeQuit() {
    	if(network != null)
    		network.writeQuit();
    }

    /** Write a new message if a network exists. */
	public void writeNew(int size, int[] arr) {
		if(network != null)
			network.writeNew(size, arr);
	}

	/**
	 * @return {@link connected}
	 */
	boolean isConnected() {
		return connected;
	}
	
	/**
	 * @param {@link connected}
	 */
	void setConnected(boolean connected) {
		this.connected = connected;
	}

}
