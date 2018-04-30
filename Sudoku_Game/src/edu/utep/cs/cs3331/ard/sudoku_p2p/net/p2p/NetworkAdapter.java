package edu.utep.cs.cs3331.ard.sudoku_p2p.net.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An abstraction of a TCP/IP socket for sending and receiving Sudoku game messages.
 * This class allows two players to communicate with each other through a socket and
 * solve Sudoku puzzles together. It is assumed that a socket connection is already
 * established between the players. 
 * 
 * @author		Yoonsik Cheon
 * @author		Anthony DesArmier
 * @author 		Trevor McCarthy
 * @version 1.2
 */
public class NetworkAdapter {

	/** Different type of game messages. */
	public enum MessageType {
		/** Connection closed. */
		CLOSE (null),
		/** Request to fill a number in the board. */
		FILL ("fill:"),
		/** Acknowledgement of a fill message. */
		FILL_ACK ("fill_ack:"),
		/** Request to join an existing game. */
		JOIN ("join:"),
		/** Acknowledgement of a join request. */
		JOIN_ACK ("join_ack:"),
		/** Request to play a new game. */
		NEW ("new:"),
		/** Acknowledgement of a new game request. */
		NEW_ACK ("new_ack:"),
		/** Quit the game. */
		QUIT ("quit:"),
		/** Solve the board. */
		SOLVE ("solve:"),
		/** Unknown message received. */
		UNKNOWN (null);
		
		/** Message header. */
        private final String header;
        
        MessageType(String header) {
            this.header = header;
        }
	}
	
	private static final int[] EMPTY_INT_ARRAY = new int[0];
	/** To be notified when a message is received. */
    private MessageListener listener;
    /** Asynchronous message writer. */
    private MessageWriter messageWriter;
    /** Reader connected to the peer to read messages from it. */
    private BufferedReader in;
    /** Writer connected to the peer to write messages to it. */
    private PrintWriter out;
    /** If not null, log all messages sent and received. */
    private PrintStream logger;
    /** Associated socket to communicate with the peer. */
    private Socket socket;
	
	/**
	 * Create a new network adapter to read messages from
	 * and to write messages to the given socket.
	 * @param socket Socket to read and write messages.
	 */
	public NetworkAdapter (Socket socket) {
		this(socket, null);
	}
	
	/**
	 * Create a new network adapter. Messages are to be read
	 * from and written to the given socket. All incoming and outgoing
	 * messages will be logged on the given logger.
	 * @param socket Socket to read and write messages.
	 * @param logger Log all incoming and outgoing messages.
	 */
	public NetworkAdapter (Socket socket, PrintStream logger) {
		this.socket = socket;
        this.logger = logger;
        messageWriter = new MessageWriter();
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
	}
	
    /** 
     * Return the associated socket.
     * @return Socket associated with this adapter.
     */
    public Socket socket() {
    	return socket;
    }
    
    /** 
     * Close the IO streams of this adapter. Note that the socket
     * to which the streams are attached is not closed by
     * this method.
     */
	public void close() {
		try {
            // close "out" first to break the circular dependency
            // between peers.
            out.close();  
            in.close();
            messageWriter.stop();
        } catch (Exception e) {}
	}
	
    /**
     * Register the given messageListener to be notified when a message
     * is received.
     * @param listener To be notified when a message is received.
     * @see MessageListener
     * @see #receiveMessages()
     * @see #receiveMessagesAsync()
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    /**
     * Start accepting messages from this network adapter and
     * notifying them to the registered listener. This method blocks
     * the caller. To receive messages synchronously, use the
     * {@link #receiveMessagesAsync()} method that creates a new
     * background thread.
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessagesAsync()
     */
    public void receiveMessages() {
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                if (logger != null)
                    logger.format(" < %s\n", line);
                parseMessage(line);
            }
        } catch (IOException e) {}
        notifyMessage(MessageType.CLOSE);
    }
    
    /**
     * Start accepting messages asynchronously from this network
     * adapter and notifying them to the registered listener.
     * This method doesn't block the caller. Instead, a new
     * background thread is created to read incoming messages.
     * To receive messages synchronously, use the
     * {@link #receiveMessages()} method.
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessages()
     */
	
	public void recieveMessagesAsync() {
		new Thread(() -> receiveMessages()).start();
	}
	
    /** Parse the given message and notify to the registered listener. */
    private void parseMessage(String msg) {
        if (msg.startsWith(MessageType.QUIT.header)) {
                notifyMessage(MessageType.QUIT);
        } else if (msg.startsWith(MessageType.JOIN_ACK.header)) {
            parseJoinAckMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.JOIN.header)) {
            notifyMessage(MessageType.JOIN);
        } else if (msg.startsWith(MessageType.NEW_ACK.header)) {
        	parseNewAckMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.NEW.header)) {
        	parseNewMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.FILL_ACK.header)) {
            parseFillMessage(MessageType.FILL_ACK, msgBody(msg));
        } else if (msg.startsWith(MessageType.FILL.header)) {
            parseFillMessage(MessageType.FILL, msgBody(msg));
        } else if (msg.startsWith(MessageType.SOLVE.header)) {
        	notifyMessage(MessageType.SOLVE);
        } else {
            notifyMessage(MessageType.UNKNOWN);
        }
    }
    
    /** Parse and return the body of the given message. */
    private String msgBody(String msg) {
        int i = msg.indexOf(':');
        if (i > -1)
            msg = msg.substring(i + 1);
        return msg;
    }
    

    /** Parse and notify the given play_ack message body. */
    private void parseJoinAckMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: join_ack 0
        	int response = parseInt(parts[0].trim());
        	if (response == 0) {
        		notifyMessage(MessageType.JOIN_ACK, 0);
        		return;
        	}
        	if (response == 1 && parts.length >= 2) {
        		// message: join_ack 1 size squares
        		int size = parseInt(parts[1].trim());
        		if (size > 0) {
        			int[] others = new int[parts.length - 2];
        			for (int i = 2; i < parts.length; i++)
        				others[i-2] = parseInt(parts[i]);
        			notifyMessage(MessageType.JOIN_ACK, 1, size, others);
        			return;
        		}
        	}
        }
        notifyMessage(MessageType.UNKNOWN);
    }
    
    /** Parse and notify the given new_ack message body. */
    private void parseNewAckMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: new_ack response
        	int response = parseInt(parts[0].trim());
        	response = response == 0 ? 0 : 1;
        	notifyMessage(MessageType.NEW_ACK, response);
        	return;
        }
        notifyMessage(MessageType.UNKNOWN);
    }
    
    /** Parse and notify the given play_ack message body. */
    private void parseNewMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: new size squares
        	int size = parseInt(parts[0].trim());
        	if (size > 0) {
        		int[] others = new int[parts.length - 1];
        		for (int i = 1; i < parts.length; i++)
        			others[i-1] = parseInt(parts[i]);
        		notifyMessage(MessageType.NEW, size, others);
        		return;
        	}
        }
        notifyMessage(MessageType.UNKNOWN);
    }    

    /** 
     * Parse the given string as an int; return -1 if the input
     * is not well-formed. 
     */
    private int parseInt(String txt) {
        try { return Integer.parseInt(txt); } 
        catch (NumberFormatException e) { return -1; }
    }
    
    /** Parse and notify the given move or move_ack message. */
    private void parseFillMessage(MessageType type, String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 3) {
            int x = parseInt(parts[0].trim());
            int y = parseInt(parts[1].trim());
            int v = parseInt(parts[2].trim());
            notifyMessage(type, x, y, v);
        } else
            notifyMessage(MessageType.UNKNOWN);
    }
    
    /** Write the given message asynchronously. */
    private void writeMsg(String msg) {
        messageWriter.write(msg);
    }
    
    /**
     * Write a join message asynchronously.
     * @see #writeJoinAck()
     * @see #writeJoinAck(int, int...)
     */
    public void writeJoin() {
        writeMsg(MessageType.JOIN.header);
    }

    /**
     * Write a "declined" join_ack message asynchronously.
     * @see #writeJoin()
     */
    public void writeJoinAck() {
        writeMsg(MessageType.JOIN_ACK.header + "0");
    }
    
    /**
     * Write an "accepted" join_ack message asynchronously. 
     * @param size Size of the board
     * @param squares Non-empty squares of the board. Each square is represented
     *   as a tuple of (x, y, v, f), where x and y are 0-based column/row indexes,
     *   v is a non-zero number, and f is a flag indicating whether the number
     *   is given (1) or entered by the user (0).
     * @see #writeJoin()
     */
    public void writeJoinAck(int size, int... squares) {
    	StringBuilder builder = new StringBuilder(MessageType.JOIN_ACK.header);
    	builder.append("1,"); 
    	builder.append(size);
    	for (int v: squares) {
    		builder.append(",");
    		builder.append(v);
    	}
        writeMsg(builder.toString());
    }
    
    /**
     * Write a new game message asynchronously.
     * @param size Size of the board
     * @param squares Non-empty squares of the board. Each square is represented
     *   as a tuple of (x, y, v, f), where x and y are 0-based column/row indexes,
     *   v is a non-zero number, and f is a flag indicating whether the number
     *   is given (1) or entered by the user (0).
     * @see #writeNewAck(boolean)
     */
    public void writeNew(int size, int... squares) {
    	StringBuilder builder = new StringBuilder(MessageType.NEW.header);
    	builder.append(size);
    	for (int v: squares) {
    		builder.append(",");
    		builder.append(v);
    	}
        writeMsg(builder.toString());    	
    }
    
    /**
     * Write an new_ack message asynchronously. 
     * @param response True for accepted; false for declined.
     * @see #writeNew(int, int...)
     */
    public void writeNewAck(boolean response) {
        writeMsg(MessageType.NEW_ACK.header + toInt(response));
    }
    
    /** Convert the given boolean flag to an int. */
    private int toInt(boolean flag) {
        return flag ? 1 : 0;
    }
    
    /**
     * Write a fill message asynchronously. 
     * 
     * @param x 0-based column index of the square
     * @param y 0-based row index of the square
     * @param number Filled-in number
     *
     * @see #writeFillAck(int, int, int)
     */
    public void writeFill(int x, int y, int number) {
        writeMsg(String.format("%s%s,%s,%s", MessageType.FILL.header, x, y, number));
    }

    /**
     * Write a fill_ack message asynchronously.
     * @param x 0-based column index of the square
     * @param y 0-based row index of the square
     * @param number Filled-in number
     * @see #writeFill(int, int, int)
     */
    public void writeFillAck(int x, int y, int number) {
        writeMsg(String.format("%s%s,%s,%s", MessageType.FILL_ACK.header, x, y, number));
    }
    
    /** Write a quit (gg) message (to quit the game) asynchronously. */
    public void writeQuit() {
        writeMsg(MessageType.QUIT.header);
    }
    
    /** Write a solve message asynchronously. */
	public void writeSolve() {
		writeMsg(MessageType.SOLVE.header);
	}
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type) {
        listener.messageReceived(type, 0, 0, 0, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x) {
        listener.messageReceived(type, x, 0, 0, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x, int[] others) {
        listener.messageReceived(type, x, 0, 0, others);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x, int y, int v) {
        listener.messageReceived(type, x, y, v, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x, int y, int[] others) {
        listener.messageReceived(type, x, y, 0, others);
    }
	
	/** 
     * Write messages asynchronously. This class uses a single 
     * background thread to write messages asynchronously in a FIFO
     * fashion. To stop the background thread, call the stop() method.
     */
    private class MessageWriter {
        
        /** Background thread to write messages asynchronously. */
        private Thread writerThread;
        
        /** Store messages to be written asynchronously. */
        private BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        /** Write the given message asynchronously on a new thread. */
        public void write(final String msg) {
            if (writerThread == null) {
                writerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                String m = messages.take();
                                out.println(m);
                                out.flush();
                            } catch (InterruptedException e) { return; }
                        }
                    }
                });
                writerThread.setName("Writer_Thread");
                writerThread.start();
            }

            synchronized (messages) {
                try {
                    messages.put(msg);
                    if (logger != null)
                        logger.format(" > %s\n", msg);
                } catch (InterruptedException e) {}
            }
        }
        
        /** Stop this message writer. */
        public void stop() {
            if (writerThread != null)
                writerThread.interrupt();
        }
    }

	/** Called when a message is received. */
	public interface MessageListener {

		/**
		 * To be called when a message is received. The type of the received message
		 * along with optional content (x, y, z and others) are provided as arguments.
		 * @param type Type of the message received
		 * @param x First argument
		 * @param y Second argument
		 * @param z Third argument
		 * @param others Additional arguments
		 */
		void messageReceived(MessageType type, int x, int y, int z, int[] others);
	}
}
