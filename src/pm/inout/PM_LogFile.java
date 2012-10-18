/*
 * photo-manager is a program to manage and organize your photos; Copyright (C) 2010 Dietrich Hentschel
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package pm.inout;

import pm.utilities.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Write a log file.
 *
 * This class is a singleton 
 * 
 *
 */
public class PM_LogFile implements PM_Interface {

	private static PM_LogFile instance = null;
	private PrintWriter out = null;

	 
	/**
	 * Return the only instance of the class. 
	 */
	static public PM_LogFile getInstance() {
		if (instance == null) {
			instance = new PM_LogFile();
		}
		return instance;
	}

	/**
	 * Constructor
	 * 
	 * The constructor is private. You cannot create from "outside"
	 * an instance.
	 * There are only one instance of the class. You get the instance with
	 * the class method getInstance().
	 */
	private PM_LogFile() {
		open();
	}

	/**
	 * open the logfile.
	 * 
	 * Create the logfile if not found.
	 */
	private void open() {

		if (out != null)
			return;

		
		File logFile = new File(PM_Utils.getConfigDir() + File.separator
				+ FILE_LOG);
		if (!logFile.isFile()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// ERROR
				PM_Utils
						.writeErrorExit("PM wird beendet, da keine pm_log.txt angelegt werden kann");
			}
		}

		// open the file
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(logFile,
					true))); // append

		} catch (IOException e) {

			PM_Utils
					.writeErrorExit("PM wird beendet, da keine pm_log.txt angelegt werden kann");
		}
	}

	/**
	 * Write the start info
	 */
	public void start() {
		write("pm ------ START ------ ");

	}

	/**
	 * Write the String out on the file with a line feed at end
	 *  
	 */
	private void write(String txt) {
		open();

		Date date = new Date(System.currentTimeMillis());

		DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

		String dateTime = df.format(date);

		String str = dateTime + " " + txt;
		out.println(str);
		out.flush();
	}

	/**
	 * close file
	 */
	public void close() {
		if (out == null) {
			return;
		}
		write("pm ------ ENDE ------ ");
		out.close();
		out = null;
	}

}
