//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Student name: Lance Baker 
// Course: SENG3400 (Network & Distributed Computing)
// Student number: c3128034
// Assignment title: SENG3400 Assignment 1 
// File name: EconConverter
// Created: 20-08-2010
// Last Change: 27-08-2010
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.net.*;
import java.io.*;
import java.text.*;

public class EconConverter extends ServerSocket {
	private static final String SEPARATOR = ": ";
	private static final String REQUEST = "Request" + SEPARATOR;
	private static final String RESPONSE = "Response" + SEPARATOR;
	private static final String ECON = "ECON";
	private static final String OKAY = SEPARATOR + "OK";
	private static final String ECON_OK = ECON + OKAY;
	private static final String ERROR_MESSAGE = "ERR";
	private static final String CHANGE_OK = "CHANGE" + OKAY;
	private static final String BYE_OK = "BYE" + OKAY;
	private static final String END_OK = "END" + OKAY;
	private static final String MILES_PER_GALLON = " mpg";
	private static final String LITRES_PER_100KM = " l/100km";
	private static final String WAIT_MESSAGE = "Waiting on port: ";
	private static final String INVALID_PORT = "Please enter a valid port (must be an integer value)";
	private static final String INVALID_ARGS_AMOUNT = "You must specify a port to listen on.";
	private static final String REGEX_DECIMAL = "\\.";
	private static final String SHUTDOWN_MESSAGE = "Server shutting down.";
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
	// The command enumeration is used to determine the operation which the client desires
	public enum Command {START, LM, ML, BYE, END}; 
	private Socket client; // Supports a single client connection
	private PrintWriter out; // The output stream to the client
	private BufferedReader input; // The incoming requests sent from the client
	private Command command; // The current set command 'mode'
	
	
	// The public constructor used to instantiate the EconConverter
	public EconConverter(int port) throws Exception {
		super(port); // Instantiates the ServerSocket
		this.start(); // Starts the server, pending until a client is connected
		this.listen(); // Once the client is connected, it begins to listen for its requests
	}
	
	private void start() throws Exception {
		System.out.println(WAIT_MESSAGE + this.getLocalPort());
		this.client = this.accept(); // Accepts the connection
		// Wacks the client output stream to a printwriter, enabling the server to send back responses
		this.out = new PrintWriter(this.client.getOutputStream(), true);
		// Gets the input stream from client (which is the output that the client produces)
		this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		// Sets the stored command to the START mode
		this.command = Command.START;  // Allowing the Server to know that it has a new client
	}
	
	// The listen method performs the main functionality for the EconConverter
	// It iterates through a while loop for each request received from the client
	private void listen() throws Exception {
		String request; // Declares the request String
		while ((request = this.input.readLine()) != null) { // Iterates for each request received
			System.out.println(REQUEST + request); // Shows the client request to the server
			try {
				// If the client session has just started, then the first request would be for the 'ECON' confirmation.
				if ((this.command == Command.START) && (request.equals(ECON))) {
					this.transmit(ECON_OK); // Transmits back an ECON: OK response
					this.command = Command.ML; // Changes the command mode to the default ML
				// If the current command mode is set for a conversion to take place
				} else if (this.command == Command.LM || this.command == Command.ML) {
					// Checks to see if the request is a fuel value (integer, or real number)
					if (isFuelInput(request)) { 
							// Does the fuel conversation based on the specified formula.
							// Converts the already validated request value to a double, which is then used in the formula, 
							// and the result is assigned to the value variable.
							double value = 283 / Double.parseDouble(request);
							// Transmits a response back the the client, comprising of a formatted calculated value (to two decimal places), with the
							// corresponding conversion type concatenated next to it.
							this.transmit(decimalFormat.format(value) + 
								((this.command == Command.LM) ?  MILES_PER_GALLON : LITRES_PER_100KM));
							// Transmits another message, which has the 'ECON: OK' response, as expected by the client, 
							// and which is part of the protocol for this system.
							this.out.println(ECON_OK);
					} else {
						// If the request was not a fuel value, then it assumes it is a command - so it attempts to convert the string value
						// to an enumerated type. If the value doesn't convert, then it will throw an exception skipping the doCommand method 
						// and get caught in the catch below.
						this.doCommand(Command.valueOf(request.toUpperCase()));
					}
				}
			} catch (Exception ex) {
				// Transmits a default 'ERR' message to the client connected. 
				this.transmit(ERROR_MESSAGE);
				// The 'ECON: OK' confirmation message, which also gets transmitted.
				this.out.println(ECON_OK);
			}
		}
	}
	
	// The transmit method accepts a String value, 
	// which is then transmitted to the connected client.
	private void transmit(String data) {
		this.out.println(data);
		System.out.println(RESPONSE + data); // The sent data is also printed to the Server.
	}
	
	// The doCommand method only accepts an enumerated Command type value, 
	// which then does the corresponding case to the command received.
	// The method changes the state of the conversation type (to either LM or ML), and does an operation
	// once the BYE, or END command is requested.
	private void doCommand(Command command) throws Exception {
		// The switch statement, used to determine which case to select.
		switch(command) {
			case LM: case ML:
				// If the received request is either of these two (LM or ML) then it transmits the 'CHANGE: OK' response, along with 
				// the 'ECON: OK' response, back to the client, and assigns the received command to the one stored in the object.
				this.transmit(CHANGE_OK);
				this.out.println(ECON_OK);
				this.command = command;
				break;
			case BYE:
				// Once the BYE command is received; it transmits the 'BYE: OK' response back to the client, closes the client connection,
				// then it recalls the start method - which will wait until a new client wants to interact.
				this.transmit(BYE_OK);
				this.client.close();
				this.start();
				break;
			case END:
				// Once the END command is received; it will transmit the 'END: OK' response back to the client, close the client connection, 
				// close the output stream to the client, close the input stream from the client, and close the ServerSocket.
				this.transmit(END_OK);
				this.client.close();
				this.out.close();
				this.input.close();
				this.close();
				System.out.println(SHUTDOWN_MESSAGE);
				break;
		}
	}
	
	// A method used to determine where the String value received is a Double.
	// Returns a boolean value representing success.
	private static boolean isDouble(String input) {
		try {
			Double.parseDouble(input); // Attempts to convert the value to a double
			return true;
		} catch (Exception ex) {} // Throws an Exception if the conversion was unsuccessful.
		return false; // Returns false - it is outside the try catch just to make the code a bit neater. 
		//Never gets evaluated if the value returned was true.
	}
	
	// A method used to determine whether a String value is a Integer.
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input); // Atempts to convert the input
			return true;
		} catch (Exception ex) {} // Skips if unsuccessful
		return false;
	}
	
	// A method used to check whether a double value received is greater than zero
	// If it is, then it will return true. Otherwise it will throw an Exception.
	private static boolean chkPositive(double value) throws Exception {
		if (!(value > 0)) {
			throw new Exception(ERROR_MESSAGE);
		}
		return true;
	}
	
	
	// The following method is used to determine whether the received request from the client is a fuel value (int, or double).
	// Returns a boolean value indicating whether it is fuel input, and of valid format. Raises exceptions where errors occur, which gets 
	// caught in the try catch of the listen method.
	private static boolean isFuelInput(String input) throws Exception {
		if (!isInteger(input)) { // If the String value is not an Integer, then it proceeds onwards to check whether its a double.
			if (isDouble(input)) { // If the input is a double, then it makes sure the double value is not greater than 2 decimal places
				// If the decimal places exceed 2, then it will throw an Exception.
				String[] split = input.split(REGEX_DECIMAL); // Splits the inputed string on the decimal place using a regular expression, assigning the result to an String array.
				if (split[1].length() > 2) { // If the second String element is greater than 2 in character length, then it will throw the exception.
					throw new Exception(ERROR_MESSAGE);
				}
				// Otherwise, if all is swell; then it will check whether the double input is positive, and return a boolean value indicating that success.
				return chkPositive(Double.parseDouble(input));
			}
		} else {
			// Returns a boolean; representing whether the integer input is positive.
			return chkPositive(Integer.parseInt(input));
		}
		return false; // Returns false if nothing else was successful.
	}
	
	// The main method, which is the default method that gets called first.
	// It receives the arguments sent via the CLI.
    public static void main(String[] args) throws IOException {
		try {
			// If the first element is an integer (required for the port) then proceed 
			// to instantiate the EconConverter (passing the port as an argument)
			if (args.length == 1) { // Checks whether the right amount of args where given
				if (isInteger(args[0])) {
					new EconConverter(Integer.parseInt(args[0]));
				} else {
					// Otherwise, output to the Server that the port must be invalid.
					System.out.println(INVALID_PORT);
				}
			} else {
				System.out.println(INVALID_ARGS_AMOUNT);
			}
		} catch (Exception ex) {} // Catches any unexpected exceptions.
    }
}
