package rover;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * Output streams wrapper to help writing stuff in both the console and log files.
 * 
 * @author Antoine Stevan
 *
 */
class Logger {
	
	/**
	 * Date format for every logger used by the rover.
	 * In log files, there is a bit more informations than what is printed in the console, namely date and time. To do so
	 * with ease, let's define a static format for dates and times: DD/MM/YY, HH:MM:SS .. where '..' stands for either 'AM'
	 * or 'PM'.
	 */
    static DateFormat format = DateFormat.getDateTimeInstance(
	        DateFormat.SHORT,
	        DateFormat.MEDIUM);

    /** A logger uses an standard output stream to print strings in the console directly. */
    static BufferedWriter out = new BufferedWriter(new PrintWriter(System.out));
    /** The main feature of a logger is its ability to write in any log file. */
    BufferedWriter log;
	
    /**
     * Formating the date and time to have something more user friendly inside log files.
     * When using the Date class with the static Logger format, some features of the resulting string are unwanted. This
     * Logger method removes the trailing 'PM' or 'AM', removes the ',' in the middle and adds some brackets in front and
     * behind date and time.
     * 
     * @return a string of with following format '[DD/MM/YY HH:MM:SS]'
     */
	static String give_date() {
		return "["+Logger.format.format(new Date()).replaceAll(" PM", "").replaceAll(" AM", "").replaceAll(", ", " ")+"]";
	}
	
	/**
	 * Opens the log file output stream.
	 * By giving a log filename, the logger opens the wanted log file. If a FileNotFoundException is thrown during the
	 * opening process, an error message is printed but the execution continues.
	 * 
	 * @param log_filename the name of the log file.
	 */
	void open(String log_filename){
		try {
			this.log = new BufferedWriter(new PrintWriter(log_filename));
		} catch (FileNotFoundException e) { System.out.println("unable to open " + log_filename); }
	}
	
	void write(String str) throws IOException {
		out.write(str);
		this.log.write(give_date() + " " + str);
	}
	
	void write(String str, boolean newline) throws IOException {
		if (newline) 	{ out.write(str+"\n"); this.log.write(give_date() + " " + str+"\n"); }
		else 			{ out.write(str);      this.log.write(give_date() + " " + str); }
	}
	
	void writeln(String str) throws IOException {
		out.write(str+"\n");
		this.log.write(give_date() + " " + str+"\n");
	}
	
	void print(String str) throws IOException {
		out.write(str);
		out.flush();
		this.log.write(give_date() + " " + str);
		this.log.flush();
	}
	
	/**
	 * Prints a string with a newline both in the console and in the log file (auto flush).
	 * 
	 * @param str the string to be broadcasted.
	 */
	void println(String str) {
		try {
			out.write(str+"\n"); out.flush();
			this.log.write(give_date() + " " + str+"\n"); this.log.flush();
		} catch (IOException e) { System.out.println("unable to write in log.log"); }
	}
	
	/**
	 * Prints a integer value with a newline both in the console and in the log file (auto flush).
	 * 
	 * @param value the integer value to be broadcasted.
	 */
	void println(int value) {
		try {
			out.write(value+"\n"); out.flush();
			this.log.write(give_date() + " " + value+"\n"); this.log.flush();
		} catch (IOException e) { System.out.println("unable to write in log.log"); }
	}
	
	void flush() throws IOException {
		out.flush();
		this.log.flush();
	}
	
	void close() throws IOException {
		out.close();
		this.log.close();
	}

	public static void main(String[] args) throws IOException {
		Logger logger = new Logger();
		logger.open("log.log");
		for (int i = 0; i < 20; i++) {
			logger.println("Hello World!");
			try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		logger.write("done");
		logger.close();
		
	}

}
