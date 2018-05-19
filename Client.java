import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * This is a class that contains the main method in order to run the application.
 * 
 * @author Andrei-Ionut Enache
 *
 */
public class Client {
	/**
	 * This is the main method that runs the application by setting up an instance of the 
	 * ClientInstance class and call the method start.
	 * @param args is not in use for this application
	 * @throws Exception is used to throw any kind of exception
	 */
	public static void main(String[] args) throws Exception {
		ClientInstance client = new ClientInstance();
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.start();
	}
}// end of Client class

/**
 * This is the class that contains all the details about a client
 * such as building the GUI, sending new connections to a server,
 * accept user input and handle sending to the server and
 * handle responses from the server.
 *
 * This class extends the class JFrame in order to build a GUI.
 * 
 * @author Andrei-Ionut Enache
 *
 */

class ClientInstance extends JFrame {
	/** This is a JTextField used in the GUI to get input from user*/
	private JTextField userText;
	/** This is a JTextArea used in the GUI to output everything in the caht room*/
	private JTextArea chatWindow;
	/** This is a JPanel used in the GUI to build the upper side of the frame and add on it buttons or text fields*/
	private JPanel button;
	/** This is a JTextFied used in the GUI to show the instructions an user should follow*/
	private JTextField instructions;
	
	// the initial values below should be the same in the server program
	/** This is the port number for the server to connect with*/
	private int portNumber = 5555;
	private String welcome = "Please type your username.";
	private String accepted = "Your username is accepted.";
	
	/** This is a client socket to establish a connection with the server*/
	private Socket socket = null;
	/** This is a buffer to store all the input from server*/
	private BufferedReader in;
	/** This is a PrintWriter to send the output to a server */
	private PrintWriter out;
	/** This is a boolean value to know when a client is allowed to chat*/
	private boolean isAllowedToChat = false;
	/** This is used to be aware when the server is connected*/
	private boolean isServerConnected = false;
	/** This is used to store the client name*/
	private String clientName;
	
	/**
	 * This is a constructor of the class that builds the GUI itself.
	 */
	public ClientInstance(){
		super("Client");//Declare the JFrame
	    
		//Create instances of panels
	    JPanel top = new JPanel();
	    JPanel down = new JPanel();
	
	
		userText = new JTextField();
		ableToType(false);
		
		instructions = new JTextField();
		instructions.setEditable(false);//The user can not edit this text field
		
		
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));// add a scroll bar on the chat window
		chatWindow.setEditable(false);
		
		JButton enter = new JButton("\uD83D\uDC4D");//create a button
		
		top.add(enter); 
		
		//set the position of elements on frame
		add(top, BorderLayout.NORTH);
		add(down, BorderLayout.SOUTH);
		
		//set the position of elements on panel
		top.setLayout(new BorderLayout());
        top.add(new JLabel("Input: "), BorderLayout.WEST);
        top.add(userText, BorderLayout.CENTER);
        top.add(enter, BorderLayout.EAST);
		
		down.setLayout(new BorderLayout());
        down.add(new JLabel(), BorderLayout.WEST);
		down.setBorder(new TitledBorder(new EtchedBorder(), "Instruction:"));
        down.add(instructions, BorderLayout.CENTER);

		/**
		 * Method to add an event when the action of pressing "Enter" is performed.
		 */
		userText.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage(event.getActionCommand()); // get the input from user and send it to the server
					userText.setText("");// empty the user text area	
				}
				
			}
		);
		
		 
	    enter.addActionListener(new ActionListener() {
		/**
		 * Method to add an action event when the button is pressed.
		 * 
		 */
	    	public void actionPerformed(ActionEvent e) {
			{  out.println("\\like");
               out.flush();
			   showMessage("");
			   showMessage(" *          *      *  *    **** ");
			   showMessage(" *          *      * *     *    ");
               showMessage(" *          *      **      ***  ");
               showMessage(" *          *      * *     *    ");
               showMessage(" ****     *      *  *    **** ");  
			   showMessage("");
			}
	  		
		}
	});
		
		setSize(300, 150); //Sets the window size
		chatWindow.setEditable(false);
		setVisible(true);
		
		addWindowListener(new WindowAdapter(){
         /**
          * Method to pop up a message window when the user closes the frame.
          */
			public void windowClosing(WindowEvent we){
          
		 ImageIcon icon = new ImageIcon("byebye.png");
	     JOptionPane.showMessageDialog(null,
                                       "You have just left the chat.",
                                       "Chat exit",
                                       JOptionPane.INFORMATION_MESSAGE,
                                       icon);
                       
						showMessage(null);
						dispose();
              }
        });
	
	
	}
	/**
	 * Method to start the running operation of the ClientInstance class.
	 */
	public void start() {
		
		establishConnection();
		handleIncomingMessages();
	}
	
	       /**
	        * Method to create a connection between the client and server
	        * and set up the input buffer and output stream.
	        * 
	        */
            public void establishConnection() {
			/** String to save the IP address*/
            	String serverAddress = getServerAddress( "What is the address of the server that you wish to connect to?" );
			
			try {
				socket = new Socket(serverAddress, portNumber );
				ableToType(true);
				in = new BufferedReader( new InputStreamReader( socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);//auto-flush
				chatWindow.append("Connection Established! Connected to: " + socket.getInetAddress().getHostName() + "\n");
				isServerConnected = true;
				instructions.setText("");//empty the instruction text field
			} 
			catch (IOException e) {
				showMessage( "Exception in handleConnection(): " + e );
			}
			handleProfileSetUp();
		} 
	
        /**
         * This method contains a while operation in order to take output from a server
         * and decides what instructions to show and whether the user name is available or not.
         * After the user name is chosen this method only broadcasts the input from user.     
         */
		private void handleProfileSetUp() {
		String line = null;
		while ( ! isAllowedToChat ) {
			try { line = in.readLine(); }
			catch (IOException e) {
				showMessage( "Exception in handleProfileSetUp:" + e );
			}
			if ( line.startsWith( welcome ) ) {
				instruction(welcome);
			} 
			else if (line.startsWith( accepted ) ) {
				isAllowedToChat = true;
				showMessage( accepted +" You can type messages." );
				instruction( "To see a list of commands, type \\help." );
			}
			else showMessage( line );
		}
	}	
		
	/**
	 * This method shows a message in console and GUI for user and 
	 * takes the input from console which is meant to be an IP address.
	 * @param hint is a message for user 
	 * @return the IP address to connect to the server
	 */
	private String getServerAddress (String hint) {
		String address = null;
		try {
			BufferedReader reader = new BufferedReader(
				new InputStreamReader( System.in ) );
			if ( hint != null ) 
			{ System.out.println( hint ); 
		      instruction("INPUT IN CONSOLE THE ADDRESS OF THE SERVER");
			}
			address = reader.readLine();
			showMessage(address);
		}
		catch (IOException e) {
		showMessage( "Exception in getClientInput(): " + e );
		}
		return address;
	} 
	    /**
	     * Method to update the instruction text area.
	     * @param hint is used to get a message to be shown as instruction
	     */
		private void instruction (String hint) 
		{
			instructions.setText( hint );			
        }
		
		/**
		 * Method to create a thread for each client that outputs
		 * all the incoming messages from server.
		 */
		private void handleIncomingMessages() { // Listener thread
		Thread listenerThread = new Thread( new Runnable() {
			/**
			 * The defined run method which takes the messages
			 * from server and handles errors.
			 */
			public void run() {
				while ( isServerConnected ) {
					String line = null;
					try {
						line = in.readLine();
						if ( line == null ) {
							isServerConnected = false;
							showMessage( "Disconnected from the server" );
							closeConnection();
							break;
						}
						showMessage( line );
					}
					catch (IOException e) {
						isServerConnected = false;
						showMessage( "IOE in handleIncomingMessages()" );
						showMessage( "Server shut down" );
						ableToType(false);
						break;
					}
				}
			}
		});
		listenerThread.start();//start the thread	
	} 	
	
	/**
	 * Method to handle the end of a client connection
	 */
    void closeConnection() {
		try {
            ableToType(false);			
			socket.close(); 
			System.exit(0); // finish the client program
		} 
		catch (IOException e) {
			showMessage( "Exception when closing the socket" );						
			showMessage( e.getMessage() );
		}
	} // end of closeConnection() in the class ClientInstance		
	
    /**
     * Method to set if the user text area where the user executes inputs can be editable or not.
     * @param tof is a boolean value to set if the userText area can be editable
     */
    private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
    
    /**
     * Method to send the input of user to the server and call the showMessage method to
     * set a new message on the user chat window.
     * @param message is broadcasted to the user and server window
     */
	private void sendMessage(String message){
		try{
			out.println(message);
			out.flush();
			showMessage(message);
		}catch(Exception e){showMessage("Invalid input");}	
	}
	
	/**
	 * Method to update the update the chat window where all the messages are shown.
	 * @param message is updated to the chat window
	 */
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(message + "\n");//update chat window
				}
			}
		);
	} // end of showMessage method
	
} // end of ClientInstance class