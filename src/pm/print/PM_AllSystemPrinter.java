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
package pm.print;

import java.util.*;

import javax.print.*;

/**
 * The container of all system printers.
 * 
 * A singleton (only one instance) is created.
 * Here all system printers are searched (with PrintServiceLookup).
 * For every printers a PM_SystemPrinter object is created and stored
 * in the 'systemPrinters' List.
 * 
 */
public class PM_AllSystemPrinter {

 

	private List<PM_SystemPrinter> systemPrinters = new ArrayList<PM_SystemPrinter>();
	
	static private PM_AllSystemPrinter instance = null;
	
    /**
     * Get the instance of the singleton.
     * 
     * Only one instance of the class is created.
     *  
     */
	static public PM_AllSystemPrinter getInstance() {
		if (instance == null) {
			instance = new PM_AllSystemPrinter();
		}
		return instance;
	}
	
	// Private constructor prevents instantiation from other classes
	private PM_AllSystemPrinter() {
		 
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(
				null, null);

		 
		if (printServices.length == 0) {
			System.out.println("No system printer found.");
		} else {
//			System.out.println("All known system printer:");
		}

		for (PrintService ps: printServices) {
			PM_SystemPrinter sp = new PM_SystemPrinter(ps);
//			System.out.println("       " + ps.getName());
			systemPrinters.add(sp);
		}

		 

	 
	}

	 

	 
	/**
	 * Get the List of all system printers
	 */
	public List<PM_SystemPrinter> getSystemPrinters() {
		return systemPrinters;
	}

	 
	/**
	 * Search system printer.
	 * 
	 * @param name - The name of the system printer.
	 * @return the system printer for the name. null if no printer found.
	 */
	public PM_SystemPrinter getSystemPrinter(String name) {
		 
		for (PM_SystemPrinter sp: systemPrinters) {
			if (name.equalsIgnoreCase(sp.getName())) {
				return sp;
			}
		}
		return null;
	}

	// =====================================================
	// clear()
	//
	// alle PM-Drucker zurücksetzen
	// =====================================================
	public void init(PM_PmPrinter drucker, PM_PaperFormat papierFormat) {

		// alle Drucker zurücksetzen
		for (PM_SystemPrinter sp: systemPrinters) {
			sp.init(drucker, papierFormat);
		}
	}

}
