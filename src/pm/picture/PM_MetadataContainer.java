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

import pm.utilities.*;
import pm.inout.*;
import pm.sequence.*;

import java.io.*;
import java.util.*;
 
/**
 * A singleton (only one instance) to manage all the metadata.
 * 
 *  
 */
public class PM_MetadataContainer implements PM_Interface {

	 
	private static PM_MetadataContainer instance = null;
	private PM_Configuration properties;
	private List<PM_PictureDirectory> pictureDirectories = new ArrayList<PM_PictureDirectory>();
	
	private Vector<PM_Listener> changeListener = new Vector<PM_Listener>();
	
	private Set<Integer> miniSequences = new HashSet<Integer>();
	
    /**
     * Get the instance of the singleton.
     * 
     * Only one instance of the class is created.
     *  
     */
	static public PM_MetadataContainer getInstance() {
		if (instance == null) instance = new PM_MetadataContainer();
		return instance;
	}

	// Private constructor prevents instantiation from other classes
	private PM_MetadataContainer() {

		properties = PM_Configuration.getInstance();

		// Change Listerner fuer Metadatenanederungen hier erzeugen
	    PM_Listener metadatenChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// die Metadaten wurden geaneder
				if (e.getObject() instanceof PM_Picture) {
					PM_Picture picture = (PM_Picture) e.getObject();
					int type = e.getType();
					// call class "PM_VDR_MpgFile" to delete the mpegfile if VDR
					PM_VDR_MpgFile.metadataChanged( picture, type);
					// Suchen und aufrufen den entsprechenden treeNode
					PM_PictureDirectory pd = getPictureDirectory(picture);
					if (pd == null) {
						return; // Fehler: kein treeNode gefunden
					}
					pd.metadatenChanged(picture); // nur zur Liste hinzufügen
					metadatenChanged();
				}
			}
		};

		PM_PictureMetadaten.addChangeListener(metadatenChangeListener);
	}

 
	/**
	 * on start up create all picture directories.
	 */
	public void initialize(PM_ListenerX listener) { 
		 
		if (listener != null) {
			listener.actionPerformed(new PM_Action("max", getAnzahlIndexFiles(), "prüfen Metadaten"));	
		} else {
			System.out.println("prüfen Metadaten. Anzahl Bilder = " + getPictureSizeValid());
		}
		initCreateAllPictureDirectories(listener);
		 
	} 
 
	/**
	 *  
	 */
	public Set<Integer> getMiniSequences() {
		return miniSequences;
	}
	 
	/**
	 * get a new mini sequence number
	 */
	public int getNewMiniSequenceNumber() {
		int i;
		for (i=1;;i++) {
			if (!miniSequences.contains(i)) {		 
				break;
			} 
		}
		miniSequences.add(i);
		return i;
	}
	
	/**
	 * Get the list off all PM_PictureDirectory instances.
	 * 
	 */
	public List<PM_PictureDirectory> getPictureDirectories() {
		return pictureDirectories;
	}
	
	/**
	 * Get original picture file from picture key.
	 * 
	 * 
	 */
	public File getFileOriginal(String pictureKey) {
		String[] a = pictureKey.split("_",2);
		if (a.length != 2) {
			return null;
		}
		int fileNumber = -1;
		try {
			fileNumber = Integer.parseInt(a[0]);
		} catch (NumberFormatException e) {
			return null;
		}

		File dirOrigFile = getDirOrigFile(fileNumber);
		if (dirOrigFile == null) {
			return null;
		}
		return new File(dirOrigFile.getPath() + File.separator + a[1]);

	}

	// ======================================================
	// getDirOrigFile()
	//
	// nigefu: return null
	// =====================================================		
	private File getDirOrigFile(int fileNumber) {
		
		for (PM_PictureDirectory pd: pictureDirectories) {
			if (pd.getIndexFileID() == fileNumber) {
				return pd.getDirOrigFile();
			}
		}
		return null;  // nigefu
		 
	}

	 
	/**
	 * Get the file number for a original picture file.
	 * 
	 * Every picture belong to a PM_PictureDirectory instance.
	 * All PM_PictureDirectory instances are in the pictureDirectories
	 * list.
	 * All PM_PictureDirectory instances has a unique number, the idNumber.
	 * 
	 * @return the idNumber of the PM_PictureDirectory instance. -1 not found.
	 */
	public int getNumberIndexFile(File fileOriginal) {
		if (!fileOriginal.canRead()) {
			return -1;
		}
	 
		String dir = fileOriginal.getParent();
		for (PM_PictureDirectory pd: pictureDirectories) {			 
			if (dir.equalsIgnoreCase(pd.getDirOrigFile().getPath())) {
				return pd.getIndexFileID();			 
			}
		}

		return -1;
	}

	// ======================================================
	// addChangeListener()/fireChangeListener()
	//
	// Wenn im Container Metadaten geaendert werde,
	// werden die Listerner NACH der entgegebenahme der Aenderungen
	// aufgerufen.
	// =====================================================
	public void addChangeListener(PM_Listener listener) {
		if (!changeListener.contains(listener)) changeListener.add(listener);
	}

	private void fireChangeListener() {
		for (int i = 0; i < changeListener.size(); i++) {
			PM_Listener listener = (PM_Listener) changeListener.elementAt(i);
			listener.actionPerformed(new PM_Action(this));
		}
	}

 
 
	
	
	
	// ======================================================
	// addSequenz()
	//
	// Initphase: 
	// 	Bei der Konsistenzprüfung aller Daten werden
	// 	hier die geschlossenen Sequenzen aus allen
	// 	pm_index.xml - Files gelesen und hier gesammelt.
	//
	// initSequences: key = 'b11' (eindeutiger Sequenzname)
	//                value = Anzahl der Pictures der Sequenz
	// =====================================================
	private Hashtable<String, Integer> initSequences = new Hashtable<String, Integer>();  
	public void addInitSequence(String sequenz) {
		 
		if (sequenz.trim().length() == 0) return;

		// jetzt Sequnzname extrahieren und in das Dictionary schreiben
		String[] alle = sequenz.split(" ");
		for (int i = 0; i < alle.length; i++) {
			String name = PM_Sequence.getSequenzKurzName(alle[i]);
			if (name == null) continue;
			if (initSequences.containsKey(name)) {
				Integer anz = (Integer) initSequences.get(name);
				initSequences.put(name, new Integer(anz.intValue() + 1));

			} else {
				initSequences.put(name, new Integer(1));
			}
		}
	}
	public Hashtable<String, Integer> getInitSequences( ) {
		return initSequences;
	}
 
	
	// ======================================================
	// getAnzahlMetadatenToChange()
	//
	// Liefert die Gesamtanzahl der anstehenden Metadatenaenderungen
	// ======================================================
	private int getAnzahlMetadatenToChange() {

		int anz = 0;
		for (PM_PictureDirectory pd: pictureDirectories) {
			anz += pd.getAnzahlMetadatenToChange();
		}

		return anz;

	}

 

	// ======================================================
	// initMetadaten()
	//
	// Hier werden die Metadaten von nur EINEM Bild gelesen
	// ======================================================
	public void initMetadaten(PM_Picture picture) {

		PM_PictureDirectory fileTreeNode = getPictureDirectory(picture);
		if (fileTreeNode == null) {
			return;
		}
		fileTreeNode.initMetadaten(picture );
	}

	// =====================================================
	// getBilderGesamt()
	//
	// Anzahl der gesamten Originalbilder (nicht Thumbnails)
	// =====================================================
	public int getPictureSizeValid() {
		int anz = 0;	
		for (PM_PictureDirectory pd: pictureDirectories) {
			anz += pd.getPictureSizeValid();		 
		}
		return anz;
	}
	public int getPictureSizeInvalid() {
		int anz = 0;	
		for (PM_PictureDirectory pd: pictureDirectories) {
			anz += pd.getPictureSizeInvalid();		 
		}
		return anz;
	}
	 
	/**
	 * makeMpegFiles()
	 */
	public void makeMpegFiles(PM_ListenerX listener) {
		for (PM_PictureDirectory pd: pictureDirectories) {
			if (!pd.makeMpegFiles(listener)) {
				return;  // cancel
			} 
		}
	}
  
	/**
	 * deleteMpegFiles()
	 */
	public int deleteMpegFiles() {
		int count = 0;
		for (PM_PictureDirectory pd: pictureDirectories) {
			count += pd.deleteMpegFiles();	 
		}
		return count;
	}
	
	/**
	 * countMpegFiles()
	 */
	public int countMpegFiles() {
		int count = 0;
		for (PM_PictureDirectory pd: pictureDirectories) {
			count += pd.countMpegFiles();	 
		}
		return count;
	}
	
	/**
	 * missingsMpegFiles()
	 */
	public int missingsMpegFiles() {
		int count = 0;
		for (PM_PictureDirectory pd: pictureDirectories) {
			count += pd.missingsMpegFiles();	 
		}
		return count;
	}
	
	/**
	 * getMissingsMpegFiles()
	 */
	public List<PM_Picture> getMissingsMpegFiles() {
		
		List<PM_Picture> liste = new ArrayList<PM_Picture>();
		for (PM_PictureDirectory pd: pictureDirectories) {
			liste.addAll(pd.getMissingsMpegFiles( ));
		}
		return liste;
		
		 
	}
	
	// =====================================================
	// flush() and close()
	//
	// Alle geaenderten Metadaten in die XML-Files schreiben.
	// Offenen XML-Files schliessen ???
	// Evtl. zu löschende Bilder werden jetzt gelöscht
	// =====================================================
	public void flush() {
		for (PM_PictureDirectory pd: pictureDirectories) {
			pd.flush();	 
		}
		
		// ganz zum Schluss fire changeListener
		fireChangeListener();
	}

	public void close() {

		flush();
		for (PM_PictureDirectory pd: pictureDirectories) {
			pd.close();	 
		}
		 

		// ganz zum Schluss fire changeListener
		fireChangeListener();
	}

	// =====================================================
	// alleLuceneEintraegeNeuErstellen
	//
	// =====================================================
	public void alleLuceneEintraegeNeuErstellen(PM_ListenerX listener) {

		 
		if (listener != null) {
			listener.actionPerformed(new PM_Action("max", getPictureSizeValid(), "Datenbank neu erstellen"));
		} else {
			System.out.println("Datenbank neu erstellen. Anzahl Bilder = " + getPictureSizeValid());
		}
		
		for (PM_PictureDirectory pd: pictureDirectories) {
			pd.alleLuceneEintraegeNeuErstellen(listener); 
		}
	}

 

	// =====================================================
	// bilderOhneSequenzen()
	//      
	// Liefert eine Liste von Bildern, die keiner Serie  zugeordnet sind
	// =====================================================  
	public List<PM_Picture> bilderOhneSequenzen( ) {

		List<PM_Picture> liste = new ArrayList<PM_Picture>();
		for (PM_PictureDirectory pd: pictureDirectories) {
			liste.addAll(pd.getAllPicturesNotInSequences( ));
		}
		return liste;

	}

	
	// =====================================================
	// bilderDoppelteSequenzen()
	//      
	// Liefert eine Liste von Bildern, die in doppelte Serien sind
	// =====================================================  
	public List<PM_Picture> bilderDoppelteSequenzen() {

		List<PM_Picture> liste = new ArrayList<PM_Picture>();
		for (PM_PictureDirectory pd: pictureDirectories) {
			liste.addAll(pd.bilderDoppelteSequenzen());
		}
		 

		return liste;

	}	
 

	

	// =====================================================
	// getTreeNode()
	//
	// Aus den Metadaten wird das zugehoerige FileTreeNode Object geholt
	//
	// null wenn keine treeNode vorhanden
	// =====================================================
	public PM_PictureDirectory getPictureDirectory(PM_Picture picture) {
		return getPictureDirectory(picture.getFileOriginal().getParentFile());
	}

	private PM_PictureDirectory getPictureDirectory(File dir) {

		String dirOrigFile = dir.getAbsolutePath();

		for (PM_PictureDirectory pd: pictureDirectories) {
			String path = pd.getDirOrigFile().getAbsolutePath();
			if (dirOrigFile.equalsIgnoreCase(path)) {
				return pd;
			}
		}
		return null; // nigefu
	}

	
	 
	// =================================================================
	// deleteIndex()
	// ==================================================================	

	public void deleteIndex(IndexType type) {

		if (type == IndexType.INDEX_1) {

			for (PM_PictureDirectory pd : pictureDirectories) {
				pd.loeschenIndex1();
			}
		} else {
			for (PM_PictureDirectory pd : pictureDirectories) {
				pd.loeschenIndex2();
			}
		}
	}

	 

	// =================================================================
	// alleSequenzenLoeschen()
	//
	// Alle Sequenz-Einträge   löschen
	// ==================================================================	

	public void alleSequenzenLoeschen() {

		for (PM_PictureDirectory pd: pictureDirectories) {
			pd.alleSequenzenLoeschen();
		}

		// Jetzt noch in PM_XML_Alle_Sequenzen löschen, damit
		// u.a. auch die Anzeige in den Windows upgedatet wird
	//	TODO PM_AllSequences_deprecated.getInstance().alleSequenzenLoeschen();
	}

 

	// ************************** PRIVATE *****************************
	// ************************** PRIVATE *****************************
	// ************************** PRIVATE *****************************

	// =====================================================
	// metadatenChanged()
	//
	// In den einzelnen FileNodeTrees wurde der Update bereits veranlasst,
	// aber noch nicht ausgefuehrt.
	//
	// Hier jetzt pruefen, ob tatsaechlich die XML-Dateien geschrieben werden
	// sollen.
	// =====================================================
	private void metadatenChanged() {
		// flush, wenn mehr als "anzahlDarstellen" aus PM_Einstellungen anliegen
		int anzToChange = getAnzahlMetadatenToChange();
		// Damit nicht zu oft flush ....
		if (anzToChange > 100) {
			flush();
		}
		// ganz zum Schluss fire changeListener
		fireChangeListener();

	}

	// =====================================================
	// init: makeAllPictureDirectories()
	//
	// Initialisierung !!
	// =====================================================
	private void initCreateAllPictureDirectories(PM_ListenerX listener) {

		flush(); // evtl. offene Indexfiles zurueckschreiben
		pictureDirectories = new ArrayList<PM_PictureDirectory>();
		System.out.print("Lesen Index-Files ");
		File homeBilder = PM_Configuration.getInstance().getTopLevelPictureDirectory();	
		  
		level = -1;
		lesenDirInit(homeBilder , listener );
		
		System.out.println();
		
		// now check and set the idNumbers
		// (get the idNumber from the index file)
 		PM_PictureDirectory.initCheckIndexFiles(pictureDirectories);
		
		flush(); // alle geaenderten/neuen/geloeschten Metadaten	
	 
	}

	// =====================================================
	// private: rekursive lesen
	//
	// Initialisierung !!
	// =====================================================
	private int level = 0;
	private void lesenDirInit(File dir , PM_ListenerX listener  ) {
		level++;
	
		 

		// ----------------------------------------------------
		// Index-Datei updaten/anlegen wenn in diesem Directory
		// Bilddateien vorhanden sind
		// ----------------------------------------------------
		File[] files = dir.listFiles();		
		Arrays.sort(files);		
		boolean bilder = false;
		// es werden nur die Bilder gezählt
		for (File file: files) {			 
			if (PM_Utils.isPictureFile(file)) {
				bilder = true;
				break;
			}
		} 
		PM_PictureDirectory pictureDirectory = null;
		if (bilder) {
			// Es sind Bilder vorhanden. PM_PictureDirectory erstellen:
		    pictureDirectory = new PM_PictureDirectory(dir, level);					
			pictureDirectory.initProgramStart(listener);
		} 
		// pictureDirectory aufnehmen wenn "gültige" Bilder vorhanden
		if (pictureDirectory != null && (pictureDirectory.getPictureSizeValid() > 0 ||
				pictureDirectory.getPictureSizeInvalid() > 0)) {
			pictureDirectories.add(pictureDirectory);
			System.out.print(".");
			if (listener != null) {
				listener.actionPerformed(new PM_Action(null, 1, dir.getPath()));	
			}
		}
	 
			// keine Bilder. Metadaten löschen (falls vorhanden)
//			File meta = new File(dir.getPath() + File.separator + DIR_METADATEN);
//			PM_Utils.deleteDirectory(meta);

		// -----------------------------------------------------
		// Jetzt weitere Directories behandeln
		// -----------------------------------------------------
		for (File file: files) {
		    if (file.isFile()) {
		    	continue;
		    }
		    String[] fileList = file.list();
		    if (fileList == null) {
		    	continue;
		    }
		    if (fileList.length == 1 && fileList[0].equals(DIR_METADATEN)) {
		    	PM_Utils.deleteDirectory(file);
		    	continue;
		    }
		    
			if (fileList.length == 0) {
				// ein leeres Directory
				file.delete();
				continue;
			}
		    
			if (PM_Utils.isBilderDir(file)) {
				lesenDirInit(file , listener );
				level--;
			}
 
		} // for

	 
	}

 

	/**
	 * new PictureDirectory
	 * 
	 * NOT NOT initialization
	 * =======
	 */
	public PM_PictureDirectory createPictureDirectory(File dir) {
		 
		 // TODO level is undefined 
		PM_PictureDirectory pd = new PM_PictureDirectory(dir, -1 );
		pd.setIndexFileID(PM_XML_File_Session.getInstance().getNextIndexFileID());
		pictureDirectories.add(pd);
		 
		 return pd;
	}
	
	
	
	/**
	 * Returns all picture files without thumbs.
	 *  
	 * 
	 */
	public List<File> getPicturesWithoutThumbs(PM_ListenerX listener) {
		List<File> newPictures = new ArrayList<File>();
		// get files from the PM_PictureDirectory instances
		for (PM_PictureDirectory picDir: pictureDirectories) {
			newPictures.addAll(picDir.getPicturesWithoutThumbs(listener));
		}
		
		// Check directories WITHOUT PM_PictureDirectory instances
		File homeBilder = properties.getTopLevelPictureDirectory();
		getNewPictures(homeBilder, newPictures); 
		
		return newPictures;
	}
	
	
	 
	/**
	 * get all picture files without thumbs for unknown directories.
	 * 
	 * That are directories that don't yet have a PM_PictureDirectory 
	 * instance.
	 */
	private void getNewPictures(File dir, List<File> newPictures) {

		PM_PictureDirectory pd = getPictureDirectory(dir);

		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				if (PM_Utils.isBilderDir(f)) {
					getNewPictures(f, newPictures);
				}
				continue;
			} else if (PM_Utils.isPictureFile(f) && pd == null) {
				// dies ist ein neues Bilde
				newPictures.add(f);
			}

		} // for

		return;
	}

	
	
	/**
	 *  get number of xml-index files.
	 */
	private int getAnzahlIndexFiles() {
		return lesenDir(properties.getTopLevelPictureDirectory());
	}

	/** 
	 * get number of xml-index files (recursive)
	 *  
	 */
	private int lesenDir(File dir) {
		int anzahl = 0;
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File f = files[i];

			if (f.isDirectory()) {
				anzahl += lesenDir(f);
				continue;
			}

			if (f.getName().equals(FILE_INDEX_XML)) {
				anzahl++;
				continue;
			}
		}
		return anzahl;
	}
	
	
	
	
	
	
	
} 

