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


 
 
import pm.inout.*;
import pm.search.*;
import pm.utilities.*;

import java.io.*;
import java.util.*;

 

/**  
 * The class for all pictures in one directory in or under the TLPD.
 * 
 * For every directory in or under the Top Level Picture Directory TLPD
 * an instance is created.
 * All instances are in the list 'pictureDirectories' an aggregate
 * of class PM_MetadataContainer.
 * <p>
 * 
 */
public class PM_PictureDirectory implements PM_Interface {

	private File dirOrigFile; // Parent von pm_index.xml
	private File pmIndexFile; // File von pm_index.xml
	private PM_IndexFile indexFileXML;
 
	 
	private HashMap<String, PM_Picture> pictureToChange = new HashMap<String, PM_Picture>();
	 
	 
	private  int indexFileID = 0; // Identification	
	
	
	private int level;
//	private String uuid = "";
	
	private PM_XML_File_Session sessionInstance;
	
	/**
	 * Creates a picture directory 
	 * 
	 *  @param dirOrigFile - the directory file
	 *  @param level - path depth relative to TLPD (TLPD is level 0) 
	 * 
	 * 
	 */
	public PM_PictureDirectory(File dirOrigFile, int level ) {
 
		this.dirOrigFile = dirOrigFile;
		this.level = level;
		
		sessionInstance = PM_XML_File_Session.getInstance();
		
		pmIndexFile = PM_Utils.getFileIndexXML(dirOrigFile);
		indexFileXML = new PM_IndexFileXML(pmIndexFile);
		indexFileID = indexFileXML.getIndexFileID();
	}
	
	public void setIndexFileID(int indexFileID) {
		if (this.indexFileID == 0) {
			this.indexFileID = indexFileID;
			indexFileXML.setIndexFileID(indexFileID);
			
		}
	}
	
//	public String getUUID() {
//		return xmlIndexFile.getUUID();
//	}
	
	public int getIndexFileID() {
		return indexFileID;
	}
	
	
	 
	
	/**
	 * Call by initialization:
	 * 
	 * (1) Check double uuid 
	 * (2) Check double fileId (for directories with uuid)
	 * (3) No fileID or uuid: set them
	 * 
	 */
	static void initCheckIndexFiles(List<PM_PictureDirectory> pds) {
		PM_XML_File_Session.getInstance().checkMaxIdNumber(pds);
		// (1) Check double uuid 
		/*
		for (PM_PictureDirectory pd: pds) {
			for (PM_PictureDirectory p: pds) {
				if (p.getUUID().length() == 0 || p == pd) {
					continue;
				}
				if (p.getUUID().equals(pd.getUUID())) {
					String msg = "Es gibt zwei (uuid-)identische " + FILE_INDEX_XML + "-Dateien:" 
							+ "\n" + p.pmIndexFile.getPath() 
							+ "\n" + pd.pmIndexFile.getPath();
					PM_Utils.writeErrorExit(msg);
				}
			}
		}
		*/
		// (2) Check double fileId (for directories with uuid)
		for (PM_PictureDirectory pd: pds) {
			if (pd.getIndexFileID() == 0) {
				continue;
			}
			for (PM_PictureDirectory p: pds) {
				if (pd.getIndexFileID() == 0 || p == pd) {
					continue;
				}
				if (p.getIndexFileID() == pd.getIndexFileID()) {
					String msg = "Es gibt zwei " + FILE_INDEX_XML + "-Dateien mit identischer " 
				            + PM_IndexFileXML.XML_FILE_ID + " :" 
							+ "\n" + p.pmIndexFile.getPath() 
							+ "\n" + pd.pmIndexFile.getPath();
					PM_Utils.writeErrorExit(msg);
				}
			}
			
			
		}
		// (3) No fileID: set them
		for (PM_PictureDirectory pd: pds) {
			if (pd.getIndexFileID() == 0) {
				pd.setIndexFileID(PM_XML_File_Session.getInstance().getNextIndexFileID());		
			}
		}
	}
	
 
	 
	public File getPmIndexFile() {
		return pmIndexFile;
	}

	 
	public int getLevel() {
		return level;
	}
	 
	/**
	 * Initialization on program start.
	 * 
	 * All picture files, thumbnails files, and the xml-index file
	 * are read and are checked for consistency.
	 * <p>
	 * All thumbs without an original picture files are deleted.
	 * <p>
	 * All xml-index entries without a reference to an thumbs file are deleted.  
	 *  
	 */
	public  void initProgramStart( PM_ListenerX listener) {

		boolean initGeaendert = false;
		// wenn true muss Lucene neu erstellt werden

	 
		// Listener aufrufen
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, dirOrigFile.getPath()));
		}
 
		// -----------------------------------------------------------------------
		// L�schen alle Thumbs f�r die keine OrigFiles (gesamte Liste) vorhanden
		// -----------------------------------------------------------------------
		Set<String> origFileNames = new HashSet<String>();		
		File[] files = dirOrigFile.listFiles();
		for (File file : files) {
			if (PM_Utils.isPictureFile(file)) {
				origFileNames.add(file.getName());
			}
		}	
		// l�schen thumbs, die kein original file haben
		File dirThumnails = getDirThumbnails();
		for (String name : dirThumnails.list()) {
			int i = name.lastIndexOf(EXT_THUMBNAIL);
			if (i > 0 && !origFileNames.contains(name.substring(0, i))) {
				(new File(dirThumnails.getPath() + File.separator + name)).delete();
			}
		}
		
		// --------------------------------------------------------------------------
		// L�schen Index-Eintr�ge f�r die keine Thumbs
		// -------------------------------------------------------------------------
		Set<String> allFileNames =  indexFileXML.getValidFileNames();
		allFileNames.addAll(indexFileXML.getInvalidFileNames());
		Set<String> thumbnailIDs = getThumbnailIDs();
		for (String id: allFileNames) {
			if ( ! thumbnailIDs.contains(id)) {
				indexFileXML.removeID(id);
				initGeaendert = true;
			}
		}
		
		// ------------------------------------------------------------
		// L�schen Thumbs, f�r die es keine Index-Eintr�ge gibt
		// ------------------------------------------------------------
		thumbnailIDs = new HashSet<String>(getThumbnailIDs());
		thumbnailIDs.removeAll(indexFileXML.getValidFileNames());
		thumbnailIDs.removeAll(indexFileXML.getInvalidFileNames());
		for (String name: thumbnailIDs) {
			(new File(dirThumnails.getPath() + File.separator + name + EXT_THUMBNAIL)).delete();
		}
		
		
		// -------------------------------------------------------------
		// IndexFile schreiben und Lucene neu erstellen
		// -------------------------------------------------------------
		if (initGeaendert) {
			PM_DatabaseLucene.getInstance().setNeuErzeugen(true);
			indexFileXML.writeDocument ();
		}
			
		 
		
		indexFileXML.initComplete();
		
		
	 
	}

	 
	// * A list for all picture files without an thumb file is created.
	  
	 
	
	/**
	 * return unkwown pictures.
	 *  
	 *  collect all pictures without thumbnails
	 *  (they are unknown pictures)
	 * @param listener  
	 */
	public List<File> getPicturesWithoutThumbs(PM_ListenerX listener) {
		List<File> newPictures = new ArrayList<File>();
		Set<String> origFileNames = getOrigFileNames();
		origFileNames.removeAll(getThumbnailIDs());
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null,1,dirOrigFile.getPath()));
		}
		
		
		for (String neu: origFileNames) {
			File fileOrig = new File(dirOrigFile.getPath() + File.separator + neu);
//System.out.println("getNewPictures: " + fileOrig.getAbsolutePath());
			newPictures.add(fileOrig);
		}	
		
		return newPictures;
	}
	
	/**
	 * return orig files  (nur die g�ltigen)
	 *  
	 *  Einschliesslich der ge�nderten
	 * @param modifiedIncluded TODO
	 */
	public List<File> getAllOrigValidFiles(boolean modifiedIncluded) {
		List<File> origPictures = new ArrayList<File>();
	
		Set<String> thumbsIDs = indexFileXML.getValidFileNames(); //getThumbnailIDs();
		for (String neu: thumbsIDs) {
			File fileOrig = new File(dirOrigFile.getPath() + File.separator + neu);
			origPictures.add(fileOrig);
		}	
		if (!modifiedIncluded) {
			return origPictures;
		}
		// jetzt noch die g�nderten
		File dirMetadaten = new File(dirOrigFile.getPath() + File.separator
				+ DIR_METADATEN);
		File dirBilderBearb = new File(dirMetadaten.getPath() + File.separator
				+ DIR_BILDER_BEARBEITEN);
		if (dirBilderBearb.isDirectory()) {
			for (File file : dirBilderBearb.listFiles()) {
				if (file.getName().endsWith("0")) {
					// ein Orig file
					origPictures.add(file);
				}
			}
		}
		return origPictures;
	}
	
	
	/**
	 * return orig files that has thumbnails
	 *  
	 *  (OHNE gel�schte !!!  und ohne die ge�nderten) 
	 *  
	 */
	public List<File> getOrigFiles() { 
		List<File> origPictures = new ArrayList<File>();
		Set<String> thumbsIDs = indexFileXML.getValidFileNames();
		for (String neu: thumbsIDs) {
			File fileOrig = new File(dirOrigFile.getPath() + File.separator + neu);
			origPictures.add(fileOrig);
		}			 
		return origPictures;
	}
 
 
	/**
	 * return origfile names (without blocked Files)
	 */
	private Set<String> getOrigFileNames() {
		Set<String> origNames = new HashSet<String>();
//		String[] names = dirOrigFile.list();
		File[] files = dirOrigFile.listFiles();
		for (File file : files) {
			if (PM_Utils.isPictureFile(file)) {
				origNames.add(file.getName());
			}
		}	 
		return origNames;
	}
	
	
	/**
	 * return with thumbnails id
	 * 
	 * id's without EXT_THUMBNAIL.
	 */
	private Set<String> getThumbnailIDs() {
		Set<String> thumbnailIDs = new HashSet<String>();
		for (String name : getDirThumbnails().list()) {
			int i = name.lastIndexOf(EXT_THUMBNAIL);
			if (i > 0) {
				thumbnailIDs.add(name.substring(0, i));
			}
		}
		return thumbnailIDs;
	}
		
	
	/**
	 * return thumbnails directory
	 * 
	 */
	File getDirThumbnails() {
		File dirThumnails = new File(getDirMetadaten() + File.separator
				+ DIR_THUMBNAILS);
		if (!dirThumnails.isDirectory()) {
			dirThumnails.mkdirs();
		}
		return dirThumnails;		
	}
	
	/**
	 * return metadatens directory
	 *  
	 */
	File getDirMetadaten() {
		File dirMetadaten = new File(dirOrigFile.getPath() + File.separator
				+ DIR_METADATEN);
		if (!dirMetadaten.isDirectory()) {
			dirMetadaten.mkdirs();
		}
		return dirMetadaten;
	}
 
 
	/**
	 * getTreePath()
	 * 
	 */
	public List<String> getTreePath() {
		List<String> treePath = new ArrayList<String>();
		if (level < 0) {
			treePath.add("????");
			treePath.add("unbekannt");
			return treePath;
		}
		
		
		File file;
		try {
			file = dirOrigFile.getCanonicalFile();
		} catch (IOException e) {		 
			treePath.add("unbekannt");
			return treePath;
		}
		int lv = level;
		file = file.getParentFile();
		while (file != null && lv > 0) {
			lv--;
			treePath.add(0, file.getName());
			file = file.getParentFile();		
		}
		
		return treePath;
		
		
	}
  


	// =====================================================
	// getFile()
	// =====================================================
	public File getDirOrigFile() {
		return dirOrigFile;
	}

	 
	/**
	 * Returns with relative path to top level picture directory.
	 *  
	 */
	public String getRelativePathTLD() {	 
		return PM_Utils.getRelativePath
		   (PM_Configuration.getInstance().getTopLevelPictureDirectory(), dirOrigFile);
	}
	

	public int getPictureSizeValid() {
		return indexFileXML.getPictureSizeValid();
	}
	
	public int getPictureSizeInvalid() {
		return indexFileXML.getPictureSizeInvalid();
	}

	// =====================================================
	// metadatenChanged()
	//
	// Aufruf durch ChangeListener der Metadaten
	// =====================================================

	public void metadatenChanged(PM_Picture picture) {
		pictureToChange.put(picture.meta.getId(), picture);
	}

	// =================================================================
	// loeschenIndex1()
	//
	// Alle Index-1 Eintr�ge l�schen
	// ==================================================================
	public void loeschenIndex1() {

		flush();

		// nun ALLE l�schen
		indexFileXML.loeschenIndex1();
	}

	// =================================================================
	// loeschenIndex2()
	//
	// Alle Index-2 Eintr�ge l�schen
	// ==================================================================
	public void loeschenIndex2() {

		flush();

		// nun ALLE l�schen
		indexFileXML.loeschenIndex2();
	}

	// =================================================================
	// alleSequenzenLoeschen()
	//
	// Alle Sequen- Eintr�ge l�schen
	// ==================================================================
	public void alleSequenzenLoeschen() {

		flush();

		// nun ALLE l�schen
		indexFileXML.alleSequenzenLoeschen();
	}

	
	/**
	 * makeMpegFiles()
	 */
	public boolean makeMpegFiles(PM_ListenerX listener) {
		
		for (File fileOrig: getAllOrigValidFiles(false)) {
			
			File fileThumbnail = PM_Utils.getFileThumbnail(fileOrig);
			File fileMpeg = PM_Utils.getFileMPEG(fileOrig);
						
			if (!PM_VDR_MpgFile.isPictureConverted(fileThumbnail, fileMpeg)) {
				PM_Picture pic = PM_Picture.getPicture(fileOrig);
				if (pic == null) {
					continue;
				}
				PM_VDR_MpgFile mpg = new PM_VDR_MpgFile(pic);
				if (mpg.convert(fileOrig)) {
					if (listener != null) {
						if (!listener.actionPerformed(new PM_Action(null))) {
							return false; // cancel
						}
					}
				} else {
					// cannot make a mgeg file.
					// Create an empty file.
					try {
						fileMpeg.delete();
						fileMpeg.createNewFile();
					} catch (IOException e) {}
				}
			}		
		} // for
		return true;
	}
	
	 
	
	
	/**
	 * deleteMpegFiles()
	 */
	public int deleteMpegFiles() {
		int count = 0;
		for (File fileOrig: getAllOrigValidFiles(false)) {		 
			 if (PM_Utils.getFileMPEG(fileOrig).delete()) {
				 count++;
			 }
		}
		return count;
	}
	
	/**
	 * countMpegFiles()
	 */
	public int countMpegFiles() {
		int count = 0;
		for (File fileOrig: getAllOrigValidFiles(false)) {		 
			 if (PM_Utils.getFileMPEG(fileOrig).exists()) {
				 count++;
			 }
		} 
		return count;
	}
	
	/**
	 * missingsMpegFiles()
	 */
	public int missingsMpegFiles() {
		int count = 0;
		for (File fileOrig: getAllOrigValidFiles(false)) {		 
			 if (!PM_Utils.getFileMPEG(fileOrig).exists()) {
				 count++;
			 }
		} 
		return count;
	}
	
	// =====================================================
	// flush()
	//
	//  Alle  Metadatenaenderungen jetzt ausfuehren und aus der Liste entfernen.
	// Und XML-Daetei schreiben. KEIN Close der XML-Datei!!!!
	// =====================================================
	public void flush() {
	
		
		boolean write = false;
		if (indexFileXML.getVersionRead() != indexFileXML.getVersionWrite()) {
			// Die index-Datei muss unbedingt geschrieben werden, da
			// Versionswechsel !!!!!
			write = true;
		}
		
		
		if (pictureToChange.isEmpty() && write == false) {
			return;
		}

		// Update
		for (PM_Picture picture: pictureToChange.values()) {
			update(picture);
		}
		
		
		// alles l�schen
		pictureToChange.clear();

		// Schreiben XML-Datei auf Platte (ohne Close)
		indexFileXML.writeDocument ();
		 
	}

	// =====================================================
	// close()
	//
	//  flush und close XML-Datei
	// =====================================================
	public void close() {
		flush();
	}
 
	
	// =====================================================
	// alleLuceneEintraegeNeuErstellen()
	//
	//
	// =====================================================
	public void alleLuceneEintraegeNeuErstellen(PM_ListenerX listener) {

		indexFileXML.alleLuceneEintraegeNeuErstellen(listener);

	}

 
	// =====================================================
	// bilderOhneSequenzen()
	//      
	// Liefert eine Liste von Bildern, die keiner Serie  zugeordnet sind
	// =====================================================  
	public List<PM_Picture> getAllPicturesNotInSequences( ) {
		return indexFileXML.getAllPicturesNotInSequences( );
	}

	public List<PM_Picture> getMissingsMpegFiles() {
		List<PM_Picture> liste = new ArrayList<PM_Picture>();

		for (File fileOrig : getAllOrigValidFiles(false)) {
			if (!PM_Utils.getFileMPEG(fileOrig).exists()) {
				PM_Picture pic = PM_Picture.getPicture(fileOrig);
				if (pic != null) {
					liste.add(pic);
				}
			}
		}
		return liste;

	}
	
	 
	public List<PM_Picture> bilderDoppelteSequenzen() {
		return indexFileXML.bilderDoppelteSequenzen();
	}	
	
	  
	public List<File> getPicturesNotInSequences(List seqNamen, boolean doppelt) {
		return indexFileXML.getPicturesNotInSequences(seqNamen, doppelt);
	}

	// =====================================================
	// initMetadaten()
	//
	// Hier wird die Metadaten-Instanz vervollstaendigen
	// (sie befindet sich noch in der init-Phase befinden, d.h. getInit() == true).
	//
	// Nach der Vervollstaendigung wird setInitComplete() fuer die
	// Medataten-Instanz aufgerufen.
	//
	// Die Daten werden NUR gelesen. Im Document (und damit im
	// xml-File) wird NICHTS veraendert.
	//
	// return false: Es konnten keine Metadaten gefunden werden
	// =====================================================
	public void initMetadaten(PM_Picture  picture) {

		if (!picture.meta.getInit()) {
			return; // nicht mehr in der Init-Phase
		}

		// jetzt aufdatieren
		indexFileXML.getMetadaten(picture);

	}

	// =====================================================
	// toString()
	// =====================================================
	public String toString() {
		int count = indexFileXML.getPictureSizeValid();
		if (count == 1) {
			return 	dirOrigFile.getName() + ": 1 "  + PM_MSG.getMsg("picture")  ;
		}  
		return 	dirOrigFile.getName() + ": " + String.valueOf(count) + " "  + PM_MSG.getMsg("pictures"); 		 
	}

	//======================================================
	// getAnzahlMetadatenToChange()
	//
	// Liefert die Gesamtanzahl der anstehenden Metadatenaenderungen
	//======================================================
	public int getAnzahlMetadatenToChange() {
		return pictureToChange.size();
	}

	

	/**
	 * Delete irreversible all invalid pictures and metadata.
	 *  
	 */
	public int deletePictureInvalid() {	 
		return indexFileXML.deletePictureInvalid(dirOrigFile);	
	}
	
	
	// ============================================================
	//<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	//<<<<<<<<<<<<<<<<< PRIVATE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// ============================================================

	// =====================================================
	// updateMetadaten()
	//
	// =====================================================
	private void update(PM_Picture picture) {
		indexFileXML.update(picture, picture.meta.getId());

	}

 
 

  

}
