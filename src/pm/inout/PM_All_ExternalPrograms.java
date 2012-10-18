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

import java.util.*;
import java.io.*;

/**
 * All external image modification programs are stored in the file
 * "pm_external_programs.txt".
 * 
 * A singleton (only one instance) is created.
 * 
 * It is a text file.
 * 
 * <pre>
 *     -%%%Gimp%%%/usr/bin/gimp
 * </pre>
 * 
 */
public class PM_All_ExternalPrograms implements PM_Interface {

	private static PM_All_ExternalPrograms instance;
	private Vector<PM_ExternalProgram> alleExternalPrograms = new Vector<PM_ExternalProgram>();  
	private final String TRENNUNG = "%%%";
	private final String DARSTELLEN = "X";
	private final String NICHT_DARSTELLEN = "-";
	private File fileExterneProgramme;

	/**
	 * Get the instance of the singleton.
	 * 
	 * Only one instance of the class is created.
	 * 
	 */
	static public PM_All_ExternalPrograms getInstance() {
		if (instance == null) {
			instance = new PM_All_ExternalPrograms();
		}
		return instance;
	}

	// Private constructor prevents instantiation from other classes
	private PM_All_ExternalPrograms() {

	}

	/**
	 * Read on start up the file with all external programs.
	 * 
	 * Create for every program an PM_ExternalProgram instance and add it to the
	 * list of external programs.
	 */
	public void init() {
		fileExterneProgramme = PM_Configuration.getInstance()
				.getFileHomeExtProgramme();
		if (!fileExterneProgramme.isFile())
			return; // noch keine Datei vorhanden

		try {
			BufferedReader in = new BufferedReader(new FileReader(
					fileExterneProgramme));
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				String[] a = line.split(TRENNUNG);
				if (a.length != 3)
					continue;
				String name = a[1];
				String pfad = a[2];
				boolean anzeigen = a[0].equals("DARSTELLEN");

				PM_ExternalProgram ext = new PM_ExternalProgram(name, pfad,
						anzeigen);
				alleExternalPrograms.add(ext);
			}
			in.close();
		} catch (IOException e) {
			alleExternalPrograms = new Vector<PM_ExternalProgram>();
			return; // error
		}

	}

	/**
	 * Write the file out.
	 * 
	 */
	public void close() {

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(fileExterneProgramme)));
			for (int i = 0; i < alleExternalPrograms.size(); i++) {
				PM_ExternalProgram ext = (PM_ExternalProgram) alleExternalPrograms
						.elementAt(i);
				String line = (ext.getAnzeigen()) ? DARSTELLEN
						: NICHT_DARSTELLEN;
				line += TRENNUNG + ext.getName() + TRENNUNG + ext.getPath();
				out.println(line);
			}
			out.close();
		} catch (IOException e) {
			alleExternalPrograms = new Vector<PM_ExternalProgram>();
			return; // fehler beim Lesen (Open) File
		}

	}

	/**
	 * Get list of all external program instances.
	 * 
	 */
	public Vector getAlleExternenProgramme() {
		return alleExternalPrograms;
	}

	/**
	 * Get an external programm.
	 * 
	 * @param name -
	 *            short name of the external program.
	 * @return the external program instance
	 */
	public PM_ExternalProgram getExternesProgramm(String name) {

		for (int i = 0; i < alleExternalPrograms.size(); i++) {
			PM_ExternalProgram extProg = (PM_ExternalProgram) alleExternalPrograms
					.elementAt(i);
			if (name.equalsIgnoreCase(extProg.getName()))
				return extProg;
		}

		return null;
	}

	/**
	 * Add an external program to the list.
	 * 
	 * When close the external program write to the file.
	 */
	public void addExternalProgram(PM_ExternalProgram extProg) {
		alleExternalPrograms.add(extProg);
	}

	/**
	 * Remove an external program from the list.
	 * 
	 */
	public void removeExternalProgram(PM_ExternalProgram extProg) {
		alleExternalPrograms.remove(extProg);
	}

}