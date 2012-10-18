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

 
import pm.inout.PM_VDR_MpgFile;
import pm.picture.*;
import pm.utilities.*;
 
 

public class PM_WindowDialogBackupBilder extends PM_WindowDialogBase implements
		PM_Interface {

	private static final long serialVersionUID = 1L;

	private PM_Configuration einstellungen;

	 

	private File fileBilderDir;
	private String pathFileBilderdir;
	private int bilderGesamt = 0;
	private File fileHomeBilder;
	private String pathHomeBilder;

	// =============================================================
	// Konstruktor
	// =============================================================
	public PM_WindowDialogBackupBilder(
			PM_Configuration.BackUp backUp) {
		super();

		 
		fileBilderDir = backUp.getFileDirTo();
		pathFileBilderdir = fileBilderDir.getAbsolutePath();
		if (!pathFileBilderdir.endsWith(File.separator)) {
			pathFileBilderdir = pathFileBilderdir + File.separator;
		}
		einstellungen = PM_Configuration.getInstance();
		bilderGesamt = PM_MetadataContainer.getInstance().getPictureSizeValid();
		fileHomeBilder = einstellungen.getTopLevelPictureDirectory();
		pathHomeBilder = einstellungen.getTopLevelPictureDirectory().getAbsolutePath();
		if (!pathHomeBilder.endsWith(File.separator)) {
			pathHomeBilder = pathHomeBilder + File.separator;
		}

	}

	// ========================================================
	// init()
	//
	// Initialisierung und Prï¿½fung
	//
	// return false: Fehler gefunden. (Meldungen bereits ausgegeben)
	// ==========================================================
	public boolean init(boolean mpeg) {

		// prï¿½fen, ob target erreichbar
		File metaRoot = new File(pathFileBilderdir + DIR_METADATEN_ROOT);
		if (!metaRoot.isDirectory()) {
			JOptionPane.showConfirmDialog(this, "Fehler:" 
					+ "\nbilder-dir = " + pathFileBilderdir 
//					+ "\nhost = " + backUp.getHost() + " nicht erreichbar" 
					+ "\nVerzeichnis '" + DIR_METADATEN_ROOT + "' nicht vorhanden"
					, "Keine gefunden",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// prï¿½fen, ob LOCK
		File pmLock = new File(pathFileBilderdir + DIR_METADATEN_ROOT
				+ File.separator + FILE_LOCK);
		if (pmLock.exists()) {
			JOptionPane
					.showConfirmDialog(
							this,
							"Fehler (pm-lock Datei):"
									+ "\n"
									+ "bilder-dir   \'" + pmLock.getPath() + "\'"
									+ "\nAnwendung muss beendet sein (pm-lock Datei ist vorhanden)",
							"Keine gefunden", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// if transfer to the vdr-server, check, if there are to make
		// mpeg-files
		 
		if (mpeg) {
			// check if there are mpeg files to create
			int count = PM_MetadataContainer.getInstance().missingsMpegFiles();
			if (count != 0) {
				
				
				int n = JOptionPane.showConfirmDialog(
						null,				
						"Es sind noch " + count + " mpeg files zu erzeugen" +
						"\nJetzt erstellen?",												
						"",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (n == JOptionPane.NO_OPTION) {
					return false;
				}
				
				count = PM_VDR_MpgFile.makeMpegfiles(count);
				
				int missings = PM_MetadataContainer.getInstance().missingsMpegFiles();
				
				
				JOptionPane
				.showConfirmDialog(
						null,
						count + " mpeg files erzeugt",
						"",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
				
				
				if (missings != 0) {
					return false;
				}
				
				
			}
			
		}
 	
		
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
		lesenDateienLoeschen(fileBilderDir);
		lesenDateienNeuUeberschreiben(einstellungen.getTopLevelPictureDirectory());
	}

	// ===================================================
	// rekursiv: lesenDirLoeschen()
	// "listeDateienLoeschen" erstellen
	// ===================================================
	private void lesenDateienLoeschen(File dir) {
		// Achtung: s. auch 'lesenDateienLoeschen' !!!!
		if (dir.isDirectory() && dir.getName().equals(DIR_PM_TEMP)) {
			return;
		}
		File[] files = dir.listFiles();

		if (dir.getName().equals(DIR_METADATEN_ROOT)) {
			// Achtung: s. auch 'lesenDateienNeuUeberschreiben' !!!!
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.getName().equals(FILE_XML_SEQUENZEN)) {
					addFileToListeDateiLoeschen(f);
				}
			}
			return;
		}

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				lesenDateienLoeschen(f);
				continue;
			}
			addFileToListeDateiLoeschen(f);
		}
	}

	private void addFileToListeDateiLoeschen(File file) {
		if (!file.isFile()) {
			return;
		}
		String relPathTarget = PM_Utils.getRelativePath(fileBilderDir, file);
		File source = new File(pathHomeBilder + relPathTarget);
		if (!source.isFile()) {
			listeDateienLoeschen.add(file);
		}
	}

	// ===================================================
	// rekursiv: lesenDateienNeuUeberschreiben()
	// "listeDateienNeu" erstellen
	// "listeDateienUeberschreiben" erstellen
	// ===================================================
	private void lesenDateienNeuUeberschreiben(File dir) {
		// Achtung: s. auch 'lesenDateienLoeschen' !!!!
		if (dir.isDirectory() && dir.getName().equals(DIR_PM_TEMP)) {
			return;
		}
		File[] files = dir.listFiles();
		if (dir.getName().equals(DIR_METADATEN_ROOT)) {
			// Achtung: s. auch 'lesenDateienLoeschen' !!!!
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.getName().equals(FILE_XML_SEQUENZEN)) {
					addFileToListeDateiNeuUeberschreiben(f);
				}
			}
			return;
		}
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				lesenDateienNeuUeberschreiben(f);
				continue;
			}

			addFileToListeDateiNeuUeberschreiben(f);

		}
	}

	private void addFileToListeDateiNeuUeberschreiben(File source) {
		if (!source.isFile()) {
			return;
		}
		String relPathSource = PM_Utils.getRelativePath(fileHomeBilder, source);

		File target = new File(pathFileBilderdir + relPathSource);

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
	// Files in listeDateienLoeschen jetzt lï¿½schen
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
		for (File target: listeDateienUeberschreiben) {
			listener.actionPerformed(new PM_Action(null));
			copyFile(target);
		}
	} 

	// ========================================================
	// copyFile()
	//
	// ==========================================================
	private void copyFile(File target) {
		String relPathTarget = PM_Utils.getRelativePath(fileBilderDir, target);
		File source = new File(pathHomeBilder + relPathTarget);
		target.getParentFile().mkdirs();
		 
		PM_Utils.copyFile(source, target);
		target.setLastModified(source.lastModified());
	}

	// ======================================================
	// setZeilen(JPanel panel)
	//
	// Auf Reihenfolge achten !!!!
	// ======================================================
	private ZeilenPanel pruefen = new ZeilenPanel("Bilder. Prüfen löschen/neu",
			false);
	private ZeilenPanel dateienLoeschen = new ZeilenPanel("Dateien löschen",
			true);
	private ZeilenPanel dateienNeu = new ZeilenPanel("neue Dateien übertragen",
			true);
	private ZeilenPanel dateienUeberschreiben = new ZeilenPanel(
			"vorhandene Dateien überschreiben", true);
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
	// Es wurde der weiter-Button betï¿½tigt.
	//
	// Wenn dialog.dispose() aufgerufen wird, wird der
	// gesammte Dialog beendet.
	//
	// Wenn nur return kann einer der Buttons erneut betï¿½tigt
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
			setAnzahl(bilderGesamt);
			pruefen(listener);
			dateienLoeschen.setAnzahl(listeDateienLoeschen.size());
			dateienNeu.setAnzahl(listeDateienNeu.size());
			dateienUeberschreiben.setAnzahl(listeDateienUeberschreiben.size());
			pruefen.stop();

			break;
		}
		case 2: {
			// wenn lï¿½schen,neu oder ï¿½berschreiben, dann muss DB neu erstellt werden
			if (listeDateienLoeschen.size() > 0 || listeDateienNeu.size() > 0
					|| listeDateienUeberschreiben.size() > 0) {			
//				File neuLucene = new File(pathFileBilderdir + DIR_METADATEN_ROOT
//						+ File.separator + FILE_LUCENE_DB_NEU_ERSTELLEN);
//				try {
//					neuLucene.createNewFile();
//				} catch (IOException e) {
//				}				 
			}
			// --------- Lï¿½schen --------------
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
