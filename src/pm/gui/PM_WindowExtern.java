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

package pm.gui;

//import pm.*;
//import pm.picture.*;
// 

//import java.util.*;
import java.io.*;
import javax.swing.*; //import javax.swing.event.*;
//import javax.swing.table.*;
//import java.awt.*;
//import java.awt.event.*; 
//import java.awt.image.*;
//import javax.swing.border.*;

import pm.utilities.PM_MSG;

/**
 * 
 * @author dietrich
 */
@SuppressWarnings("serial")
public class PM_WindowExtern extends JPanel {

	protected Thread waitForThread = null;

	/** Creates a new instance of PM_WindowExtern */
	public PM_WindowExtern() {
	}

	// ========================================================
	// startenProgramm()
	// 
	// ========================================================
	protected void startenProgramm(String progPath, String bildPath) {
		// Starten
		Runtime runtime = Runtime.getRuntime();

		Process process = null;
		try {
			process = runtime.exec(progPath + " " + bildPath);
		} catch (IOException e) {

			JOptionPane.showConfirmDialog(this, PM_MSG
					.getMsg("modExtMsgProgramNotFound"),
					// "Externes Programm kann nicht aufgerufen werden",
					"Fehler", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			// System.out.println("Programmladefehler:" + progPath);
			// System.out.println("       " + e);
			return;
		}

		// Jetzt Process�berwachung starten.
		// Es kann erst weitergearbeitet werden, nachdem das
		// Bildbearbeitungsprogramm
		// beendet wurde.
		waitForThread = new WaitForProcessEnde(process);
		waitForThread.start();
	}

	// ========================================================
	// waitForProcessEnde()
	// 
	// Warnung, wenn der Process zur Bildbearbeitung noch l�uft.
	// 
	// return true: Process l�uft noch.
	// ========================================================

	protected boolean waitForProcessEnde() {

		if (waitForThread == null || waitForThread.isAlive() == false)
			return false; // Process l�uft nicht

		JOptionPane.showConfirmDialog(this,
				"Bildbearbeitungsprogramm mu� beendet werden", "Fehler",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		return true;
	}

	// ========================================================
	// processEnde()
	// 
	// ========================================================
	protected void processEnde() {
		// wird ueberschrieben
	}

	// ===============================================================
	//
	// Innerclass: WaitForProcessEnde
	//
	// ===============================================================
	protected class WaitForProcessEnde extends Thread {

		private Process process;

		// =====================================================
		// Konstruktor
		// =====================================================
		public WaitForProcessEnde(Process process) {
			this.process = process;
		};

		// =====================================================
		// run()
		// =====================================================
		public void run() {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// System.out.println("  InterruptedException bei wait for " +
				// e);
				return;
			}

			// Ende der Bearbeitung
			processEnde();

		}

	}

}
