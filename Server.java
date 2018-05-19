import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This is a class that consist of a main method used to start the application,
 * a constructor that builds a GUI, a nested class HandleSession and some other methods.
 * 
 * The class extends JFrame in order to build the GUI.
 * 
 * @author Andrei-Ionut Enache
 *
 */
public class Server extends JFrame
	
	{
	/** This is a JTextArea that is used to output every server message and client message */
	private JTextArea chatWindow; 
	
	// the initial values below should be the same in the client program
	/** This is a port number that is used to set up a server and get clients connected */
	private int portNumber = 5555;
	private String welcome = "Please type your username.";
	private  String accepted = "Your username is accepted.";
	
	/** This is a long variable that helps to register the time when the server was turned on */
	private static long serverStartTime = System.currentTimeMillis();
	/** This is a Date variable that is used to store the current date of the server */
	private Date currentDate;
	/** This is a Date variable that is used to store the start date of the server */
	private Date startDate = new Date();
	/** This is a SimpleDateFormat variable that formats the Date variables used in this application */
	private SimpleDateFormat df = new SimpleDateFormat("MM/dd/YYYY HH:mm a");
	/** This is used to listen to the client messages */
	private String line;
	
	/** This is a ServerSocket used to create a server */
	private ServerSocket ss; // for the method "shutDown"
	/** This is used to store the clients user names */
	private HashSet<String> clientNames = new HashSet<String>();
	/** This is used to store all the messages of the server as an archive */
	private ArrayList<String> archive = new ArrayList<String>();
	/** This is used to store all the messages of clients and broadcast them */
	private HashSet<PrintWriter> clientWriters 
		= new HashSet<PrintWriter>(); // for many clients

	/**
	 * This is the main method of this application, used to start the server
	 * by setting up an instance of the Server class and calling the method start.
	 * @param args is not in use for this application
	 * @throws IOException is used to throw input/output exceptions
	 */
	public static void main (String[] args) throws IOException {	
		Server server = new Server(); 
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.start();
	}
	
	/**
	 * Constructor that builds a GUI.
	 */
	public Server(){
		super("Server chat");
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150); //Sets the window size
		setVisible(true);
		chatWindow.setEditable(false);
	}
	
	/**
	 * This is a method that creates a server socket, then start the server
	 * and waits for clients because the server accepts it.
	 * After a client connected to the server, a new instance of class HandleSession is 
	 * created as a thread and the thread is started afterwards.
	 * 
	 * The finally block is used to handle the shutting down of the server.
	 * @throws IOException is used to throw input/output exceptions
	 */
	void start() throws IOException {	
		ss = new ServerSocket( portNumber );
		showMessage( "Echo server at "
			+ InetAddress.getLocalHost()+ " is waiting for connections ... " );
		Socket socket;
		Thread thread;
		try {
			while ( true ) {
				socket = ss.accept();
				thread = new Thread( new HandleSession( socket ) );
				thread.start();
			}
		} 
		catch (Exception e)  {
			showMessage( e.getMessage() );
		}
		finally {
			shutDown(); 
		}
	}
	
	/**
	 * This method is used to handle the situation when the server is shut down.
	 */
	public void shutDown() {
		try { 
			ss.close(); 
			showMessage( "The server is shut down." );	
		} 
		catch (Exception e) {
			showMessage( "Problem shutting down the server." );
			showMessage( e.getMessage() );
		}
	}

	/**
	 * This is a nested class that is created as a thread whenever a client connects
	 * to the server and manages its commands and streams. 
	 * @author Andrei-Ionut Enache
	 *
	 */
	class HandleSession implements Runnable {
		/** This is a socket that is used to connect to the server*/
		private Socket socket; // for one client
		/** This String stores the name of the user*/
		String name; // for the current client
		/** This is used to register when the client connected to the server*/
		long clientStartTime;
		/** This is used to manage the stream from clients*/
		BufferedReader in = null;
		/** This is used to handle the output of server to clients*/
		PrintWriter out = null;
		
		/**
		 * Constructor that creates instances of the client socket.
		 * @param socket is used to create a connection server-client
		 */
		HandleSession (Socket socket) {
			this.socket = socket;
		}
		
		/**
		 * This is the defined method run that calls the essential methods of the class
		 * and handle exceptions.
		 */
		public void run() {
			try {
				createStreams();
				getClientUserName();
				listenForClientMessages();
			} 
			catch (SocketException e) {
				showMessage( "Some error occured" );
			}
			catch (IOException e) {
				showMessage( "INPUT ERROR" );
			}
			finally {
				closeConnection();
			}
		} // end of run() in the class HandleSession
	
		/**
		 * This is a method that creates streams used to handle the input/output of the server
		 * and confirms when a connection is established.
		 *
		 */
		private void createStreams() {
			try {
				in = new BufferedReader( new 	
					InputStreamReader( socket.getInputStream()) );
				out = new PrintWriter( new 
					OutputStreamWriter( socket.getOutputStream(), "UTF-8"), true);
				clientWriters.add( out );
				showMessage( "One connection is established" );
			} 
			catch (IOException e) {
				showMessage( "Exception in createStreams(): " + e );
			}		
		} // end of createStreams() in the class HandleSession

		/**
		 * This is a method that takes input from the client that is meant to serve as an username.
		 */
		private void getClientUserName() {
			while ( true ) {
				out.println( welcome ); out.flush(); 
				try { name = in.readLine(); } 
				catch (IOException e) {
					showMessage("Exception in getClientUserName: " + e);
				}			
				if ( name == null ) return;
				/**
				 * This synchronized method is used to check whether the username chosen by
				 * the user is taken or not.
				 * This method is synchronized because many threads might access this method at a time.
				 */
				synchronized ( clientNames ) {
				if ( ! clientNames.contains( name) && ! name.equals("\\like")) {
					clientNames.add( name );
					clientStartTime = System.currentTimeMillis();// set up the time when the client chose the name and entered in chat
					break;
				}
				}out.println( "Sorry, this username is unavailable"); out.flush();
			}
			out.println( accepted + "Please type messages." ); 
			out.flush(); // otherwise the client may not see the message
			broadcast( name + " has entered the chat.");
            archive.add( name + " has entered the chat.");	//save the server messages		
		
		}	
		// end of getClientUserName() in the class HandleSession
		
		/**
		 * Method to read the input from clients and process it.
		 * @throws IOException throws input/output exceptions
		 */
		private void listenForClientMessages() throws IOException {
			while ( in != null ) {
				line = in.readLine();
				if ( line == null ) break;
				if ( line.startsWith("\\") ) {
					if ( ! processClientRequest( line ) ) return; // output the requests of clients
				}
				else {broadcast( name + " has said: " + line); // output the message to all the clients
				archive.add(  name + " has said: " + line);}	// save the messages			
			}
		} // end of listenForClientMessages() in the class HandleSession
		
		/**
		 * This method broadcasts all the messages of clients to everyone in chat, including server.
		 * @param message an output for the server and clients.
		 */
		private synchronized void broadcast (String message)
		{
			for (PrintWriter writer : clientWriters) {
				if(!(writer.equals(out))) // checks if the message is written by the user in order to avoid broadcast
				writer.println( message ); writer.flush();
			}
			showMessage( message );
		}
		
		/**
		 * This method processes the user commands and outputs on the client window the result of requests.
		 * 
		 * @param command is the command given by the user
		 * @return true as default if the user input is wrong
		 */
		private boolean processClientRequest (String command) {
			
			/** This is used to scan the .txt file that output the server commands*/
			Scanner helpCommand = null;
			/**This is used to output the IP address*/
			InetAddress ip = null;
			switch(command)
			{
			case "\\quit": 
				return false;
			
			case "\\help":
				out.println();
				try{
				helpCommand = new Scanner(new File("help.txt")); // read the file
				
				while(helpCommand.hasNext())
		    	{   String line = helpCommand.nextLine();
		    		out.println(line);// output the file line by line
					out.flush();
		    	}
				 helpCommand.close(); // close the file
				
			}catch(FileNotFoundException e){
			out.println("ERROR: Could not find the file: " + e);}
			break;
			
			case "\\numberclients":
				out.println();
				out.println("The number of clients is currently " + clientNames.size());
			    out.flush();
				break;
			
			case "\\ipaddress":
			    try{
				ip = InetAddress.getLocalHost(); // get the IP address
				out.println();
				out.println("The local ip address is " + ip);
				out.flush();
				}catch(UnknownHostException e){out.println("ERROR: Could not find display the ip address: " + e);}
				break;
			
			case "\\servertime":
			    long currentServerTime = System.currentTimeMillis();
			    out.println();
				long serverTime = (currentServerTime - serverStartTime)/1000; // how may seconds since the server is turned on
				out.println("The server was turned on " + serverTime + " seconds ago");
				out.flush();
				break; 
				
			case "\\currentdate":
                currentDate = new Date();
                String formattedCurrentDate = df.format(currentDate); // the current date
			    out.println();
				out.println("The date is now: " + formattedCurrentDate);
				out.flush();
                break;
         
            case "\\startdate":
                String formattedStartDate = df.format(startDate); // the start date of server
				out.println();
				out.println("The date when the server was turned on is: " + formattedStartDate);
				out.flush();
				break;

			case "\\myname":
				out.println();
				out.println("Your username is: " + name);
				out.flush();
				break;
				
			case "\\mytime":
			    long currentClientTime = System.currentTimeMillis();
				long clientTime = (currentClientTime - clientStartTime)/1000; // the client time in seconds
				out.println("You have entered in this chat with your username " + clientTime + " seconds ago");
				out.flush();
				break;	
			
			case "\\allusers":
				out.println();
				for (String users : clientNames)
				{out.println(users);
				out.flush();}
				break;
				
			case "\\archive":
                 out.println();
				 for (String archiveChat : archive)
				{out.println(archiveChat);
				out.flush();}
				break;
				
			case "\\like": // outputs a "LIKE" built from *.
               broadcast(name + " :");
			   archive.add(name + " :");
			   broadcast(" *           *      *  *     **** ");
			   archive.add(" *           *      *  *     **** ");
			   broadcast(" *           *      * *      *    ");
			   archive.add(" *           *      * *      *    ");
               broadcast(" *           *      **       ***  ");
			   archive.add(" *           *      **       ***  ");
               broadcast(" *           *      * *      *    ");
			   archive.add(" *           *      * *      *    ");
               broadcast(" ****      *      *  *     **** ");
			   archive.add(" ****      *      *  *     **** ");
               broadcast("");			   
    		
			}	
			
			return true;
			
		}
		/**
		 * Method to handle when a client leave the chat.	
		 */
		private void closeConnection() {
			if ( name != null ) {
				broadcast( name + " has left the chat." );
				archive.add( name + " has left the chat.");
				clientNames.remove( name );
			}
			if ( out != null ) {
				clientWriters.remove( out ); // remove the client name
			}
			try { 
				socket.close(); 
			} 
			catch (IOException e) {
				showMessage( "Exception when closing the socket" );						
				showMessage( e.getMessage() );
			}
		} // end of closeConnection() in the class HandleSession
		
	} // end of the class HandleSession
/**
 * This method updates the text in the text area of the server
 * @param text is updated in the text area
 */
private void showMessage(final String text){
	SwingUtilities.invokeLater(
		new Runnable(){
			public void run(){
				chatWindow.append(text + "\n");
			}
		}
	);
}
	
} // end of the class Server