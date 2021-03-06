package rover;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

public class Logger {
	
    static DateFormat format = DateFormat.getDateTimeInstance(
	        DateFormat.SHORT,
	        DateFormat.MEDIUM);

    static BufferedWriter out;
    static BufferedWriter log;
	
	static public String give_date(DateFormat format) {
		return "["+format.format(new Date()).replaceAll(" PM", "").replaceAll(", ", " ")+"]";
	}
	
	public static void open() throws FileNotFoundException {
		out = new BufferedWriter(new PrintWriter(System.out));
		log = new BufferedWriter(new PrintWriter("log.txt"));
	}
	
	public static void write(String str) throws IOException {
		out.write(str);
		log.write(give_date(format) + " " + str);
	}
	
	public static void write(String str, boolean newline) throws IOException {
		if (newline) 	{ out.write(str+"\n"); log.write(give_date(format) + " " + str+"\n"); }
		else 			{ out.write(str);      log.write(give_date(format) + " " + str); }
	}
	
	public static void writeln(String str) throws IOException {
		out.write(str+"\n");
		log.write(give_date(format) + " " + str+"\n");
	}
	
	public static void print(String str) throws IOException {
		out.write(str);
		log.write(give_date(format) + " " + str);
		out.flush();
		log.flush();
	}
	public static void println(String str) throws IOException {
		out.write(str+"\n");
		log.write(give_date(format) + " " + str+"\n");
		out.flush();
		log.flush();
	}
	
	public static void flush() throws IOException {
		out.flush();
		log.flush();
	}
	
	public static void close() throws IOException {
		out.close();
		log.close();
	}

	public static void main(String[] args) throws IOException {
		open();
		for (int i = 0; i < 20; i++) {
			println("Hello World!");
			try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		write("done");
		close();
		
	}

}
