//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Student name: Lance Baker 
// Course: SENG3400 (Network & Distributed Computing)
// Student number: c3128034
// Assignment title: SENG3400 Assignment 1 
// File name: EconClient
// Created: 20-08-2010
// Last Change: 27-08-2010
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class EconClient extends Socket {
	
	// Constants used throughout the EconClient Class.
	private static final String SEPARATOR = ": ";
	private static final String CONFIRM = SEPARATOR + "OK";
	private static final String INIT_REQUEST = "ECON";
	private static final String INIT_CONFIRM = INIT_REQUEST + CONFIRM;
	private static final String BYE_CONFIRM = "BYE" + CONFIRM;
	private static final String END_CONFIRM = "END" + CONFIRM;
	private static final String SERVER = "SERVER" + SEPARATOR;
	private static final String CLIENT = "Client" + SEPARATOR;
	private static final String ERR_SERVER_NOT_RESPONDING = "SERVER NOT RESPONDING";
	private static final String MENU = "Enter command [LM, ML, BYE, END] or a number to convert" + SEPARATOR;
	private static final String REGEX_IPADDRESS = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	private static final String INVALID_PORT = "Please enter a valid port (must be an integer value)";
	private static final String INVALID_IPADDRESS = "Please enter a valid ipv4 address ([0-255].[0-255].[0-255].[0-255])";
	private static final String INVALID_ARGS_AMOUNT = "You are required to specify an ip address, and a port.";
	private static final String INDENTED_GAP = "	";

	private PrintWriter out; // Used to send output to the server
	private BufferedReader in; // The output from the server
	
	// The constructor to instantiate the EconClient object.
	public EconClient(String hostname, int port) throws Exception {
		super(hostname, port); // Instantiates the socket super class.
		this.out = new PrintWriter(this.getOutputStream(), true); // Grabs the output stream from the Socket (which is this object)
		this.in = new BufferedReader(new InputStreamReader(this.getInputStream())); // Gets the input stream from the Socket, and places it in a stream reader - then to a buffer.
		this.promptMenu(); // Starts the client system; by entering a continuous keyboard input loop.
	}
	
	// Sends the initial 'ECON' request to the server for confirmation that the connection is established.
	private boolean isEstablished() throws Exception {
		out.println(INIT_REQUEST); // Sends 'Econ' to the server
		System.out.println(CLIENT + INIT_REQUEST); // Displays sent 'Econ' message to client.
		String response = this.in.readLine(); // Grabs the response output from the server
		if (response != null) { // If the response was something other than nothing
			System.out.println(SERVER + response); // Displays output response to the client.
			return (response.equals(INIT_CONFIRM)); // Returns a boolean value indicating whether the response was an ECON: OK
		} else {
			// The response from the server was nothing, and hence the server isn't responding.
			System.out.println(ERR_SERVER_NOT_RESPONDING);
			return false; // Returns false indicating the connection was not established.
		}
	}
	
	// Prompts the user for keyboard input (with the menu displayed), and returns the input as string.
	private String getInput(BufferedReader input) throws Exception {
		System.out.print(MENU); // Displays the nice menu to the client (which requests for input)
		return input.readLine(); // Grabs the input from the keyboard once a value has been entered.
	}
	
	// Checks whether the connection is established to the server, and prompts the user for input. The input
	// is then passed to the server, and the result is then outputted back to the user.
	private void promptMenu() {
		try {
			// Checks whether the client-server connection is established; by sending the ECON request.
			if (this.isEstablished()) {
				String input; // Declaring a input String
				// Grabbing the input stream from the keyboard, and wacking it into a buffered reader.
				BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));
				// Iterates the loop, reprompting the client each time once they have sent a request.
				// Assigns the keyboard input to the input variable for repetitive use.
				while ((input = this.getInput(clientInput)) != null) {
					// Gets the response from the server.
					// Returns an array - due to the server sometimes sending multiple responses. 
					String[] response = this.request(input); 
					System.out.print(SERVER); // Prints 'Server' neatly next to the output
					if (response[0] != null) { // Only does the following if there was a response
						System.out.println(response[0]); // Outputs the first response element to client
						// If the response was a bye, or an end confirmation then it should exit from the while loop - ending the program.
						if (response[0].equals(BYE_CONFIRM) || response[0].equals(END_CONFIRM)) {
							break;
						}
						// Only outputs the second element if there was a second response returned.
						if (response[1] != null) {
							// Places it neatly (underneath 'SERVER') - indented a couple of tabs.
							System.out.println(INDENTED_GAP + response[1]);
						}
					}
				}
			}
			// If the loop has ended, then the application has;
			// so the connections will need to be closed.
			this.out.close();
			this.in.close();
			this.close(); // Closes itself (since this object is a Socket).
		} catch (Exception ex) {
			// Outputs any exceptions. 
            System.err.println(ex.getMessage());
        }
	}
	
	private String[] request(String input) throws Exception {
		System.out.println(CLIENT + input); // Shows input to client
		this.out.println(input); // Sends Request to server
		String[] response = {this.in.readLine(), this.in.readLine()}; // Grabs the two line response from server
		return response; // Returns the Response[s] from server
	}
	
	// Validates whether the string input is an integer value
	// Returns a boolean true value indicating if it is.
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception ex) {}
		return false;
	}

	// The main method of the client
	// Once the Class is invoked to run, it will go here first.
	// The args array contains the input options from the CLI.
    public static void main(String[] args) throws IOException {
		try {
			// Pattern used to validate the ipaddress given (args element 0)
			Pattern ip_pattern = Pattern.compile(REGEX_IPADDRESS);
			if (args.length == 2) { // Ensures that the correct amount of args were given
				if (ip_pattern.matcher(args[0]).matches()) { // If the ip address is valid
					if (isInteger(args[1])) { // Validates whether the port is an integer
						// Invokes the EconClient Class, and passes the valid ipaddress and port as arguments
						new EconClient(args[0], Integer.parseInt(args[1]));
					} else {
						// Invalid port (not an integer)
						System.out.println(INVALID_PORT);
					}
				} else {
					// Invalid ip address format
					System.out.println(INVALID_IPADDRESS);
				}
			} else {
				System.out.println(INVALID_ARGS_AMOUNT);
			}
        } catch (Exception ex) {
			// Used to catch any unexpected exceptions.
            System.err.println(ex.getMessage());
        }
    }
}