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
package pm.search;

 
import pm.gui.*;
import pm.picture.*;
 
import pm.utilities.*;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;

import java.io.*;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.store.*;


/** Hier wird die Lucene Datenbank erstellt
 * 
 *  Nur EINE Instanz (wird bei Start Applikation angelegt)
 *  
 */
public class PM_DatabaseLucene implements PM_Interface {

	private File luceneDB = null;
	 
	private IndexWriter indexWriter = null;
	private PM_MetadataContainer metaContainer = null;
	private FSDirectory directory;
	private JProgressBar progressionBar = null;	
	private PM_Listener metadatenChangeListener;	 
	private List<PM_Picture> listePictureMetadaten = new ArrayList<PM_Picture>();	

	private boolean gesperrt = true;
	private boolean neuErzeugen = false;
	private int mergeFactor = 500;
	
	
	// =================================================
	// Class Variable
	// =================================================
	private static PM_DatabaseLucene instance = null;

	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_DatabaseLucene getInstance() {
		if (instance == null) instance = new PM_DatabaseLucene();
		return instance;
	}

	// =====================================================
	// open()
	//
	//  Wird unmittelbar nach dem Start von PM aufgerufen
	// =====================================================
	public void open() {
 
		// ----------------------------------------------------------------
		// Open Directory
		// ----------------------------------------------------------------		
		String msg = "";
		try {
			directory = FSDirectory.getDirectory(luceneDB.getPath());
		} catch (IOException e) {
			msg = "ERROR: lucene IOException bei getDirectory. ";
			JOptionPane.showConfirmDialog(null, msg, "Fehler Lucene", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		// --------------------------------------------------------------------
		// Open IndexWriter (damit wird geprüft, ob DB O.K.
		// --------------------------------------------------------------------
		try {
			indexWriter = new IndexWriter(directory, new StandardAnalyzer(), false);
			indexWriter.setMaxMergeDocs(mergeFactor);
		} catch (IOException e) {
			try {
				neuErzeugen = true;
				indexWriter = new IndexWriter(directory, new StandardAnalyzer(), true);
				indexWriter.setMaxMergeDocs(mergeFactor);
			} catch (IOException ee) {

				msg = "ERROR: lucene IOException bei open IndexWriter. ";
//				System.out.println(msg + " =  " + e);

				JOptionPane.showConfirmDialog(null, msg, "Fehler Lucene", JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		
	 
		
		
//		if (neuErzeugen) {
//			msg = "INIT: Metadaten werden neu erstellt," +
//			              "\nda nicht vorhanden oder fehlerhaft";
//			JOptionPane.showConfirmDialog(null, msg,"Fehler Metadaten",
//					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
//		}

		 

	}

 

	
	// =====================================================
	// initBatch()    (OHNE GUI - Meldungen)
	//
	//  Wird unmittelbar nach dem Start von PM aufgerufen
	// =====================================================
	public void initBatch() {
 
		// ----------------------------------------------------------------
		// Open Directory
		// ----------------------------------------------------------------		
		String msg = "";
		try {
			directory = FSDirectory.getDirectory(luceneDB.getPath());
		} catch (IOException e) {
			msg = "ERROR: lucene IOException bei getDirectory. ";
			System.out.println(msg);
			System.exit(0);
		}
		// --------------------------------------------------------------------
		// Open IndexWriter (damit wird geprï¿½ft, ob DB O.K.
		// --------------------------------------------------------------------
		try {
			indexWriter = new IndexWriter(directory, new StandardAnalyzer(), false);
			indexWriter.setMaxMergeDocs(mergeFactor);
		} catch (IOException e) {
			try {
				neuErzeugen = true;
				indexWriter = new IndexWriter(directory, new StandardAnalyzer(), true);
				indexWriter.setMaxMergeDocs(mergeFactor);
			} catch (IOException ee) {

				msg = "ERROR: lucene IOException bei open IndexWriter. ";
				System.out.println(msg);
				System.exit(0);
			}
		}					 
	}
	
	// =====================================================
	// neuErzeugen()
	//
	// true, wenn in der Init-Phase die DB neu erstellt werden muss
	// (wenn zerstört oder nigefu)
	// =====================================================
	public boolean getNeuErzeugen() {
		return neuErzeugen;
	}
	public void setNeuErzeugen(boolean neuErzeugen) {
		this.neuErzeugen = neuErzeugen; 
	}
	// =====================================================
	// close()
	//  
	// =====================================================
	public void close() {
		flush();
		try {
			indexWriter.close();
		} catch (IOException e) {
			 
			e.printStackTrace();
		}
	}

	// =====================================================
	// createEintrag()
	//
	//  DB wird NEU erstellt !!!!!
	//
	// (Aufruf von PM_xmlFileMetadaten, wenn DB NEU erstellt wird)
	// Die Daten fuer ein neues Dokument werden uebergeben.
	// Hier jetzt ein neues Dokument erzeugen
	// =====================================================
	public void createEintrag(PM_Picture  picture) {

		PM_LuceneDocument doc = PM_LuceneDocument.create(picture);
		if (doc != null) {
//			//System.out.println("........... Document = " + doc.getDocument());
			addDocument(doc.getDocument());
		}
	}

	// =====================================================
	// flush()
	//
	// Falls noch updates anliegen, diese jetzt durchfuehren
	// =====================================================
	public void flush() {
		
		if (listePictureMetadaten.size() == 0) {
			gesperrt = false;
			return;
		}
		// ----------------------------------------------------
		// sperren, flushen, entsperren und liste lï¿½schen
		// ----------------------------------------------------
//		System.out.println("Lucene DB flush aufgerufen. Anzahl = " + listePictureMetadaten.size());
		gesperrt = true;
		doFlush();
		listePictureMetadaten.clear();
		gesperrt = false;
		
	}
	private void doFlush() {



		 
		 
		// ----------------------------------------------------
		// delete all
		// ----------------------------------------------------
		Iterator<PM_Picture> it = listePictureMetadaten.iterator();
		while (it.hasNext()) {
			PM_Picture  picture =   it.next();
			String filePathOriginal = picture.getFileOriginal().getPath();
			Term term = new Term("id", filePathOriginal);

			try {
				indexWriter.deleteDocuments(term);
			} catch (IOException e) {
//				System.out.println("LUCENE io-Error: deleteEintrag. id =  " + filePathOriginal);
			}
		}

		// ----------------------------------------------------
		// alle neu aufnehmen
		// ----------------------------------------------------		
		it = listePictureMetadaten.iterator();
		while (it.hasNext()) {
			PM_Picture  picture = (PM_Picture ) it.next();
			if (picture.meta.isInvalid()) {
				continue; // not valid
			}
			PM_LuceneDocument doc = PM_LuceneDocument.create(picture);
			if (doc != null) {
				try {
					indexWriter.addDocument(doc.getDocument());
				} catch (IOException e) {
//					System.out.println("IO-Exeption addDocument(Document). " + e);
				}
			}
		}

		// ----------------------------------------------------
		// optimize
		// ----------------------------------------------------			
		try {
			indexWriter.optimize();
		} catch (IOException e) {
			 
			//			e.printStackTrace();
		}

		 
		
		
		
	}

	// =====================================================
	// alleLuceneEintraegeNeuErstellen()
	//
	// Gesamte Lucene-Datenbank neu erstellen
	//
	// Hier wird eine ProgressionBar dargestellt
	// =====================================================
	public void alleLuceneEintraegeNeuErstellen() {
		alleLuceneEintraegeNeuErstellenWithDialog();
	}

	// =====================================================
	// alleLuceneEintraegeNeuErstellen()
	//
	// PM-init-Phase: Gesamte Lucene-Datenbank neu erstellen
	// =====================================================

	public void alleLuceneEintraegeNeuErstellen(PM_ListenerX listener) {
		luceneDBneuErstellen(listener);
	}

	//========================================================================
	// <<<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// <<<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// <<<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//========================================================================

	// =====================================================
	// Konstruktor
	// =====================================================
	private PM_DatabaseLucene() {
		metaContainer = PM_MetadataContainer.getInstance();
		PM_Configuration einstellungen = PM_Configuration.getInstance();
		luceneDB = einstellungen.getFileHomeLuceneDB();

		metadatenChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
 				if (gesperrt) {
 					return; // DB gesperrt. Keine Änderungen annehmen
 				}
				//   Metadaten wurden geändert
				if (e.getObject() instanceof PM_Picture ) {
					PM_Picture  picture = (PM_Picture ) e.getObject();
					int type = e.getType();
					if (!PM_LuceneDocument.isRelevant(type)) {
						return; // nicht relevant
					}
					if (!listePictureMetadaten.contains(picture)) {
						listePictureMetadaten.add(picture);
					}
				}
			}
		};
		PM_PictureMetadaten .addChangeListener(metadatenChangeListener);		

	}

	 
	// =====================================================
	// addDocument()
	// =====================================================
	private void addDocument(Document doc) {

		try {
			indexWriter.addDocument(doc);
		} catch (IOException e) {
	//		System.out.println("IO-Exeption addDocument(Document). " + e);
		}
	}

 

	// =====================================================================
	// alleLuceneEintraegeNeuErstellenWithDialog()
	// =====================================================================	
	int anzahlFuerProgressionBar = 0;

	private void alleLuceneEintraegeNeuErstellenWithDialog() {

		JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);

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

		//   dialogPanel.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		dialog.pack();

		// ---------- alle indexieren -----------------------
		PM_ListenerX listener = new PM_ListenerX() {
			public boolean actionPerformed(PM_Action e) {
				progressionBar.setValue(anzahlFuerProgressionBar++);
				return true;
			}
		};

		anzahlFuerProgressionBar = 0;
		// Start importThread
		Thread thread = new Indexieren(listener, dialog);
		thread.start();

		dialog.setVisible(true);

	}

	// =====================================================================
	// luceneDBneuErstellen
	//
	// NUR hier wird tatsï¿½chlich die DB neu erstellt
	// =====================================================================
	private void luceneDBneuErstellen(PM_ListenerX listener) {
		listePictureMetadaten.clear();		
		gesperrt = true;		 			
		luceneDBneuErstellenXXX( listener);
		gesperrt = false;
	}
	private void luceneDBneuErstellenXXX(PM_ListenerX listener) {

		// ------------------------------------------------------
		// IndexWriter mit "create" erï¿½ffnen
		// ------------------------------------------------------
		try {
			indexWriter.close();
			indexWriter = new IndexWriter(directory, new StandardAnalyzer(), true);
			indexWriter.setMaxMergeDocs(mergeFactor);
		} catch (IOException e) {
//			System.out.println("IO-Exeption open IndexWriter. " + e);
			JOptionPane.showConfirmDialog(null, "FEHLER: Lucene-DB kann nicht neu  erstellt werden","Fehler LuceneDB",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return; 
		}

		// ----------------------------------------------------------
		// DB-Einträge neu erstellen
		// ----------------------------------------------------------
		Date start = new Date();
		listePictureMetadaten.clear();
		metaContainer.flush();
		metaContainer.alleLuceneEintraegeNeuErstellen(listener);
 
		Date end = new Date();

		// --------- close indexWriter -------------
		int anzahlIndexiert = 0;
		try {
			indexWriter.optimize();
			anzahlIndexiert = indexWriter.docCount();
		} catch (IOException e) {
//			System.out.println("ERROR: Lucene IO-Exeption indexWriter.close(). " + e);
			System.exit(0);
		}

		// -----------------------------------
		// Protokoll
		// -----------------------------------

		long dauer = end.getTime() - start.getTime();
 		System.out.println("======================================================");
 		System.out.println("Lucene DB neu indexiert. Anzahl Eintraege = " + anzahlIndexiert);
 		System.out.println("                         Dauer = " + dauer + " Millisekunden");
 		System.out.println("                         Pfad = " + luceneDB.getPath());
 		System.out.println("======================================================");
		
 	 
		
		return;
		
	}

	//=============================================================
	//=============================================================
	//=============================================================
	//
	// Inner Class:  Thread indexieren
	//
	//=============================================================
	//=============================================================
	//=============================================================

	private class Indexieren extends Thread {

		private PM_ListenerX listener;
		private JDialog dialog;

		// =====================================================
		// Konstruktor
		// =====================================================
		public Indexieren(PM_ListenerX listener, JDialog dialog) {
			this.listener = listener;
			this.dialog = dialog;
		}

		// =====================================================
		// run()
		// =====================================================
		public void run() {

			luceneDBneuErstellen(listener);

			// -----------------------------------------------------------------
			// alle indexiert. Jetzt Dialog wieder freigeben und thread beenden
			// -----------------------------------------------------------------

			if (dialog != null) dialog.dispose();
		}

	} // Ende inner class Indexieren

} // Ende Klasse
