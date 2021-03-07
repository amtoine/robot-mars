package rover;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

class Logger {
	
    static DateFormat format = DateFormat.getDateTimeInstance(
	        DateFormat.SHORT,
	        DateFormat.MEDIUM);

    BufferedWriter out;
    BufferedWriter log;
	
	static String give_date(DateFormat format) {
		return "["+format.format(new Date()).replaceAll(" PM", "").replaceAll(", ", " ")+"]";
	}
	
	void open(String log_filename){
		out = new BufferedWriter(new PrintWriter(System.out));
		try {
			log = new BufferedWriter(new PrintWriter(log_filename));
		} catch (FileNotFoundException e) {
			System.out.println("unable to open " + log_filename);
		}
	}
	
	void write(String str) throws IOException {
		out.write(str);
		log.write(give_date(format) + " " + str);
	}
	
	void write(String str, boolean newline) throws IOException {
		if (newline) 	{ out.write(str+"\n"); log.write(give_date(format) + " " + str+"\n"); }
		else 			{ out.write(str);      log.write(give_date(format) + " " + str); }
	}
	
	void writeln(String str) throws IOException {
		out.write(str+"\n");
		log.write(give_date(format) + " " + str+"\n");
	}
	
	void print(String str) throws IOException {
		out.write(str);
		out.flush();
		log.write(give_date(format) + " " + str);
		log.flush();
	}
	void println(String str) {
		try {
			out.write(str+"\n");
			out.flush();
			log.write(give_date(format) + " " + str+"\n");
			log.flush();
		} catch (IOException e) {
			System.out.println("unable to write in log.log");
		}
	}
	
	void flush() throws IOException {
		out.flush();
		log.flush();
	}
	
	void close() throws IOException {
		out.close();
		log.close();
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
