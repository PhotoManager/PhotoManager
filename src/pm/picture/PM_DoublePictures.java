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
package pm.picture;
 
import java.awt.*;
 
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
 

import pm.gui.*;
 

import pm.utilities.*;
 
// 
//
//
public class PM_DoublePictures implements PM_Interface {

	private PM_WindowMain windowMain;
	private JProgressBar progressionBar = null;
	private PM_MetadataContainer metaContainer;
	// private PM_XML_File_Einstellungen einstellungen;

	// Ergebnisse
	private int anzGesamt = 0; // alle doppelten
	private int anzDoppelt = 0; // davon doppelt vorhanen
	private int anzDreifach = 0; // dreifach vorhanden
	private int anzVierfachPlus = 0; // mehr als dreifach vorhandne
	private File[] alleFiles = new File[0]; // alle doppelten

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_DoublePictures(PM_WindowMain windowMain) {
		this.windowMain = windowMain;
		metaContainer = PM_MetadataContainer.getInstance();
	}

	// =====================================================
	// alleLuceneEintraegeNeuErstellen()
	//
	// Gesamte Lucene-Datenbank neu erstellen
	// =====================================================
	int anzahlFuerProgressionBar = 0;

	public void doppelteBilderErmitteln() {

		JDialog dialog = new JDialog(windowMain, true);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setPreferredSize(new Dimension(300, 200));
		dialogPanel.setLayout(new BorderLayout());

		// progression
		int max = metaContainer.getPictureSizeValid();
		if (max <= 0) {
			return;
		}
		progressionBar = new JProgressBar(1, max);
		dialogPanel.add(progressionBar, BorderLayout.NORTH);

		// dialogPanel.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		dialog.pack();

		anzahlFuerProgressionBar = 0;
		// Start importThread
		Thread thread = new DoppelteBilder(dialog);
		thread.start();

		dialog.setVisible(true);

	}

	// =====================================================
	// getAnzGesamt()
	// getAnzDoppelt()
	// getAnzDreifach()
	// getAnzVierfachPlus()
	// getAlleFiles()
	// =====================================================
	public int getAnzGesamt() {
		return anzGesamt;
	}

	public int getAnzDoppelt() {
		return anzDoppelt;
	}

	public int getAnzDreifach() {
		return anzDreifach;
	}

	public int getAnzVierfachPlus() {
		return anzVierfachPlus;
	}

	public List<PM_Picture> getAlleFiles() {
		List<PM_Picture> list = new ArrayList<PM_Picture>();
		for (int i = 0; i < alleFiles.length; i++) {
			File file = alleFiles[i];
			PM_Picture pic = PM_Picture.getPicture(file);
			if (pic == null)
				continue;
			list.add(pic);
		}
		return list;
	}

	// =============================================================
	//
	// Thread DoppelteBilder
	//
	// =============================================================

	private class DoppelteBilder extends Thread {

		private JDialog dialog;
		private HashMap<String,Vector<File>> dic = null;

		// =====================================================
		// Konstruktor
		// =====================================================
		public DoppelteBilder(JDialog dialog) {
			this.dialog = dialog;
		}

		// =====================================================
		// private: rekursive lesen
		//
		// Aufbereiten Dictionary
		// =====================================================
		private void lesenDir(File dir) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile() && PM_Utils.isPictureFile(file)) {
					// ein gültiges Bild
					// String path = file.getPath();
					// testen, ob Bild zum Löschen markiert wurde
					if (PM_Picture.isPictureInstanceAvailable(file)) {
						PM_Picture picture = PM_Picture.getPicture(file);
						if (picture.meta.isInvalid()) {
							continue;
						}
					}
					// wurde nicht zum Löschen markiert.
					String lg = Long.toString(file.length());
					Vector<File> value = null;
					if (dic.containsKey(lg)) {
						value =  dic.get(lg);
						value.add(file);
					} else {
						value = new Vector<File>();
						value.add(file);
						dic.put(lg, value);
					}
					progressionBar.setValue(anzahlFuerProgressionBar++);
				}
			}
			// -----------------------------------------------------
			// Jetzt weitere Directories behandeln
			// -----------------------------------------------------
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (PM_Utils.isBilderDir(file)) {
					// rekursiv dieses Directory verarbeiten
					lesenDir(files[i]);
				}
			}
			return;
		}

		// =====================================================
		// private: makeArrayFromDic()
		//
		// Alle Values aus Dic in ein Array schreiben.
		// =====================================================
		private File[] makeArrayFromDic(HashMap<String,Vector<File>> dic) {
			Collection<Vector<File>> col = dic.values();
			Iterator<Vector<File>> it = col.iterator();
			Vector<File> files = new Vector<File>();
			while (it.hasNext()) {
				Vector v =  it.next();
				if (v.size() > 1) {
					// pfui: geht bestimmt eleganter !!!
					for (int ii = 0; ii < v.size(); ii++) {
						File p = (File) v.elementAt(ii);
						files.add(p);
					}
				}
			}
			return (File[]) files.toArray(new File[files.size()]);
		}

		// =====================================================
		// run()
		// =====================================================
		public void run() {
			PM_Configuration einstellungen = PM_Configuration
					.getInstance();
			File homeBilder = einstellungen.getTopLevelPictureDirectory();

			// Dictinary dic aufbereiten:
			// Key = Laenge, Value = Vector Dateinamen der Bilder
			dic = new HashMap<String,Vector<File>>();
			lesenDir(homeBilder);

			// Ermitteln doppelte Laengen
			// ("dic" wurde in "lesenDir" gefüllt)
			File[] files = makeArrayFromDic(dic);

			// In files stehen jetzt Kandidaten für doppelte Bilder
			// Sind jedoch noch nicht eindeutig, da nur nach Laenge gesucht
			// wurde
			// Jetzt M5 aller Dateien in vec erstellen
			progressionBar.setMinimum(1);
			progressionBar.setMaximum(files.length);
			anzahlFuerProgressionBar = 0;
			dic = new HashMap<String, Vector<File>>();
			for (int i = 0; i < files.length; i++) {
				File f = (File) files[i];
				if (f.isFile()) {
					// MD 5 erzeugen
					String md5 = new String(PM_Utils.getMessageDigest(f));
					Vector<File> value = null;
					if (dic.containsKey(md5)) {
						value =  dic.get(md5);
						value.add(f);
					} else {
						value = new Vector<File>();
						value.add(f);
						dic.put(md5, value);
					}

				}
				progressionBar.setValue(anzahlFuerProgressionBar++);
			}

			// Im dic stehen jetzt die "endgültig" ermittelten doppelten
			alleFiles = makeArrayFromDic(dic);

			Collection col = dic.values();
			Iterator it = col.iterator();
			while (it.hasNext()) {
				Vector v = (Vector) it.next();
				if (v.size() == 2)
					anzDoppelt++;
				if (v.size() == 3)
					anzDreifach++;
				if (v.size() > 3)
					anzVierfachPlus++;
			}
			anzGesamt = alleFiles.length;

			// -----------------------------------------------------------------
			// alle indexiert. Jetzt Dialog wieder freigeben und thread beenden
			// -----------------------------------------------------------------

			if (dialog != null)
				dialog.dispose();
		}

	} // Ende inner class Indexieren

}
