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

import java.io.*;
import java.util.*;
import javax.swing.*;

 
import pm.utilities.*;
 

 

@SuppressWarnings("serial")
public class PM_WindowDialogBackupDaten extends PM_WindowDialogBase implements
		PM_Interface {

	 

	private File sourceDir;
	private File targetDir;
	
 
	
/*	
 
	
	
*/	
	
	
	

	// =============================================================
	// Konstruktor
	// =============================================================
	public PM_WindowDialogBackupDaten(
			PM_Configuration.BackUp backUp) {
		super();

		 
		sourceDir = backUp.getFileDirFrom();
		targetDir = backUp.getFileDirTo();	 
		
		 

	}

	// ========================================================
	// init()
	//
	// Initialisierung und Prüfung
	//
	// return false: Fehler gefunden. (Meldungen bereits ausgegeben)
	// ==========================================================
	public boolean init() {

		 

		return true;
	}

	// ========================================================
	// pruefen()
	//
	// folgende Listen erstellen:
	//    listeDateienLoeschen
	//    listeDateienNeu
	//    listeDateienUeberschreiben
	// ==========================================================
	private void pruefen(PM_Listener listener) {
		lesenDateienLoeschen(targetDir);
		lesenDateienNeuUeberschreiben(sourceDir);
	}

	// ===================================================
	// rekursiv: lesenDirLoeschen()
	//   "listeDateienLoeschen" erstellen
	// ===================================================
	private void lesenDateienLoeschen(File dir) {
		
		 
		// --------------------------------------------------
		// hier kann ein ganzes Directory ausgeschlossen werden
		// --------------------------------------------------
//		if (dir.isDirectory() && dir.getName().equals(DIR_PM_TEMP)) {
//			return;
//		}
		
		// --------------------------------------------------
		// Loop über alle Files
		// -------------------------------------------------
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				lesenDateienLoeschen(f); // rekursiv
				continue;
			}
			addFileToListeDateiLoeschen(f);
		}
	}

	private void addFileToListeDateiLoeschen(File target) {
		if (!target.isFile()) {
			return;
		}
		// hier wird geprüft, ob Datei auch auf dem Source-Dir vorhanden ist
		String relPathTarget = PM_Utils.getRelativePath(targetDir, target);
		File source = new File(sourceDir + File.separator + relPathTarget);
		if (!source.isFile()) {
			listeDateienLoeschen.add(target);
		}
	}

	// ===================================================
	// rekursiv: lesenDateienNeuUeberschreiben()
	//   "listeDateienNeu" erstellen
	//   "listeDateienUeberschreiben" erstellen
	// ===================================================
	private void lesenDateienNeuUeberschreiben(File dir) {
		 
		
		// --------------------------------------------------
		// hier kann ein ganzes Directory ausgeschlossen werden
		// --------------------------------------------------
//		if (dir.isDirectory() && dir.getName().equals(DIR_PM_TEMP)) {
//			return;
//		}

		// --------------------------------------------------
		// Loop über alle Files von der Quelle (source)
		// -------------------------------------------------
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				lesenDateienNeuUeberschreiben(f); // rekursiv
				continue;
			}

			addFileToListeDateiNeuUeberschreiben(f);

		}
	}

	private void addFileToListeDateiNeuUeberschreiben(File source) {
		if (!source.isFile()) {
			return;
		}
		String relPathSource = PM_Utils.getRelativePath(sourceDir, source);

		File target = new File(targetDir + File.separator + relPathSource);

		if (!target.isFile()) {
			listeDateienNeu.add(target);
		} else if (source.lastModified() != target.lastModified()) {
			listeDateienUeberschreiben.add(target);
		}
	}

	// ========================================================
	// loeschen()
	//
	//  
	// Files in listeDateienLoeschen jetzt löschen
	//		 
	// ==========================================================
	private void loeschen(PM_Listener listener) {
		Iterator<File> it = listeDateienLoeschen.iterator();
		while (it.hasNext()) {
			File f = it.next();
			listener.actionPerformed(new PM_Action(null));
			f.delete();
		}
	}

	// ========================================================
	// neu()
	//
	//  
	// Files in listeDateienLoeschen jetzt neu
	//		 
	// ==========================================================
	private void neu(PM_Listener listener) {
		Iterator<File> it = listeDateienNeu.iterator();
		while (it.hasNext()) {
			File target = it.next();
			listener.actionPerformed(new PM_Action(null));
			copyFile(target);
		}
	}

	// ========================================================
	// ueberschreiben()
	//
	//  
	// Files in listeDateienLoeschen jetzt ueberschreiben
	//		 
	// ==========================================================
	private void ueberschreiben(PM_Listener listener) {
		Iterator<File> it = listeDateienUeberschreiben.iterator();
		while (it.hasNext()) {
			File target = it.next();
			listener.actionPerformed(new PM_Action(null));
			copyFile(target);
		}
	}

	// ========================================================
	// copyFile()
	// ==========================================================
	private void copyFile(File target) {
		String relPathTarget = PM_Utils.getRelativePath(targetDir, target);
		File source = new File(sourceDir + File.separator + relPathTarget);
		target.getParentFile().mkdirs();
		PM_Utils.copyFile(source, target);
		target.setLastModified(source.lastModified());
	}

	// ======================================================
	// setZeilen(JPanel panel)
	//
	// Auf Reihenfolge achten !!!!
	// ======================================================
	private ZeilenPanel pruefen = new ZeilenPanel("Daten. Prüfen löschen/neu",
			false);
	private ZeilenPanel dateienLoeschen = new ZeilenPanel("Daten-Dateien löschen",
			true);
	private ZeilenPanel dateienNeu = new ZeilenPanel("neue Daten-Dateien übertragen",
			true);
	private ZeilenPanel dateienUeberschreiben = new ZeilenPanel(
			"vorhandene Daten-Dateien überschreiben", true);
	private ZeilenPanel fertig = new ZeilenPanel("fertig", false);

	@Override
	protected void setZeilen(JPanel panel) {

		panel.add(pruefen);
		panel.add(dateienLoeschen);
		panel.add(dateienNeu);
		panel.add(dateienUeberschreiben);
		panel.add(fertig);

	}

	// ======================================================
	// weiterDialog()
	// 
	// Es wurde der weiter-Button betätigt.
	//
	// Wenn dialog.dispose() aufgerufen wird, wird der
	// gesammte Dialog beendet.
	//
	// Wenn nur return kann einer der Buttons erneut betätigt
	// werden
	// ======================================================
	private List<File> listeDateienLoeschen = new ArrayList<File>();
	private List<File> listeDateienNeu = new ArrayList<File>();
	private List<File> listeDateienUeberschreiben = new ArrayList<File>();

	@Override
	protected void goDialog(JDialog dialog, int nrAufruf, PM_Listener listener) {

		switch (nrAufruf) {

		case 1: {
			// -----------------------------------------------------------------
			// (1) Anzahl neu zu erstellende Inidizes (nur anzeigen)
			// ------------------------------------------------------------
			pruefen.start();
			setAnzahl(99999);
			pruefen(listener);
			dateienLoeschen.setAnzahl(listeDateienLoeschen.size());
			dateienNeu.setAnzahl(listeDateienNeu.size());
			dateienUeberschreiben.setAnzahl(listeDateienUeberschreiben.size());
			pruefen.stop();

			break;
		}
		case 2: {
			 
			// --------- Löschen --------------
			setAnzahl(listeDateienLoeschen.size());
			dateienLoeschen.start();
			loeschen(listener);
			dateienLoeschen.stop();
			// ---------- Neu ----------------
			setAnzahl(listeDateienNeu.size());
			dateienNeu.start();
			neu(listener);
			dateienNeu.stop();
			// ---------- Ueberschreiben ---------
			setAnzahl(listeDateienUeberschreiben.size());
			dateienUeberschreiben.start();
			ueberschreiben(listener);
			dateienUeberschreiben.stop();
			// ----------- fertig ----------------
			fertig.start();
			fertig.stop();

			break;
		}

		case 3: {
			// definitives ENDE
			dialog.dispose();
			break;
		}

		}

	}

}
