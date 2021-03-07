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

    static BufferedWriter out;
    static BufferedWriter log;
	
	static String give_date(DateFormat format) {
		return "["+format.format(new Date()).replaceAll(" PM", "").replaceAll(", ", " ")+"]";
	}
	
	static void open(String log_filename){
		out = new BufferedWriter(new PrintWriter(System.out));
		try {
			log = new BufferedWriter(new PrintWriter(log_filename));
		} catch (FileNotFoundException e) {
			System.out.println("unable to open log.log");
		}
	}
	
	static void write(String str) throws IOException {
		out.write(str);
		log.write(give_date(format) + " " + str);
	}
	
	static void write(String str, boolean newline) throws IOException {
		if (newline) 	{ out.write(str+"\n"); log.write(give_date(format) + " " + str+"\n"); }
		else 			{ out.write(str);      log.write(give_date(format) + " " + str); }
	}
	
	static void writeln(String str) throws IOException {
		out.write(str+"\n");
		log.write(give_date(format) + " " + str+"\n");
	}
	
	static void print(String str) throws IOException {
		out.write(str);
		out.flush();
		log.write(give_date(format) + " " + str);
		log.flush();
	}
	static void println(String str) {
		try {
			out.write(str+"\n");
			out.flush();
			log.write(give_date(format) + " " + str+"\n");
			log.flush();
		} catch (IOException e) {
			System.out.println("unable to write in log.log");
		}
	}
	
	static void flush() throws IOException {
		out.flush();
		log.flush();
	}
	
	static void close() throws IOException {
		out.close();
		log.close();
	}

	public static void main(String[] args) throws IOException {
		open("log.log");
		for (int i = 0; i < 20; i++) {
			println("Hello World!");
			try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		write("done");
		close();
		
	}

}
