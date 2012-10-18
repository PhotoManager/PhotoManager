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

 
//import pm.view.*;
import pm.picture.*;
import pm.utilities.*;
 

 
import org.dom4j.*; 
import java.util.*;
 
import java.io.*;



/**
 *  
 *  Hier werden in einer XML-Datei ("pm_session.xml") die Name und Dateilängen
 *  aller pm_index.xml -files gespeichert.
 *  
 *  Bei Programmstart wird geprüft, ob sich was geändert hat. 
 *  Wenn ja, wird lucene neu erstellt.
 * 
 * 
 */
public class PM_XML_File_Session extends PM_XML implements PM_Interface {
    
   private static PM_Configuration einstellungen = null;
   private static PM_XML_File_Session instance = null;   
 
   private int maxIndexFileID  = 0;
   
   private final String TAG_SESSION = "pm-session"; 
    
   
   // <pm-index-files>
   private final String TAG_INDEX_FILES = "pm-index-files";  
   private final String ATR_INDEX_FILES_MAX_ID = "max-index-file-id";
   private final String TAG_INDEX_FILE = "pm-index-file"; 
   private final String ATR_INDEX_FILE_ID = "id";
   private final String ATR_INDEX_FILE_LG = "laenge";   
   private final String ATR_INDEX_FILE_NAME = "name";
   
   
   
   // =====================================================
   // Class Method: getInstance()
   //
   // Es wird nur eine Instanz angelegt (Singleton)
   // =====================================================
	static public PM_XML_File_Session getInstance() {
		if (instance == null)
			instance = new PM_XML_File_Session();
		return instance;
	}   

	public void open() {
		readXMLFile();
	}

  
	public int getNextIndexFileID( ) {
		return ++maxIndexFileID;
	}
	
	public void checkMaxIdNumber(List<PM_PictureDirectory> pds) {
		for (PM_PictureDirectory pd: pds) {
			maxIndexFileID = Math.max(maxIndexFileID, pd.getIndexFileID());		
		}
	}
	
	
	public void setAllPictureDirectoryIDs() {
		PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();
		   
	     HashMap<String, File> alleFiles  = new HashMap<String, File>();       
	     for (PM_PictureDirectory pd: metadatenContainer.getPictureDirectories()) {
	    	 File xmlFile = pd.getPmIndexFile();
	         alleFiles.put(xmlFile.getPath(), xmlFile);   
	     }	
		
		
		XPath  xpathSelector = DocumentHelper.createXPath("//" + TAG_INDEX_FILE);    
	     List<Element> result = xpathSelector.selectNodes(document);
	     for (Iterator it = result.iterator(); it.hasNext(); ) {      
	       Element element = (Element) it.next();
	       int id = PM_XML_Utils.getAttributeInt(element, ATR_INDEX_FILE_ID);
	       String lg = PM_XML_Utils.getAttribute(element, ATR_INDEX_FILE_LG);
	       String name = PM_XML_Utils.getAttribute(element, ATR_INDEX_FILE_NAME);
	     }
	}
	
	
	
	
  // =====================================================
  // indexFilesGeaendert()
  //
  // Hier wird geprueft, ob die Index-File-Name und File-Laenge 
  // noch der Wirklichkeit entsprechen, d.h. ob die Index-Files
  // sich gegenueber der letzten Session geandert haben.
  // =====================================================
  public boolean indexFilesGeaendert( ) {     	 
      
     PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();
   
     HashMap<String, File> alleFiles  = new HashMap<String, File>();       
     for (PM_PictureDirectory pd: metadatenContainer.getPictureDirectories()) {
    	 File xmlFile = pd.getPmIndexFile();
         alleFiles.put(xmlFile.getPath(), xmlFile);   
     }
     
     // Holen alle Files aus pm_session.xml
     XPath  xpathSelector = DocumentHelper.createXPath("//" + TAG_INDEX_FILE);    
     List<Element> result = xpathSelector.selectNodes(document);
     for (Iterator it = result.iterator(); it.hasNext(); ) {      
       Element element = (Element) it.next();
       int id = PM_XML_Utils.getAttributeInt(element, ATR_INDEX_FILE_ID);
       String lg = PM_XML_Utils.getAttribute(element, ATR_INDEX_FILE_LG);
       String name = PM_XML_Utils.getAttribute(element, ATR_INDEX_FILE_NAME);
       if ( !alleFiles.containsKey(name)) {
    	   return true;
       }
       File fInMap = (File)alleFiles.get(name);
       long lLong = fInMap.length();
       long lll = PM_Utils.stringToLong(lg);
       if (lLong != lll) {
    	   return true;
       }
       alleFiles.remove(name);  // Eintrag loeschen
     }
     // Jetzt muss das hashMap leer sein, da gefundene gelöscht wurden
     if ( !alleFiles.isEmpty()) {
    	 return true;               
     }
     return false;
  
  }  

  // =====================================================
  // close()
  //
  // =====================================================
  public void close() {
      
  	 // löschen alle Index-Files-Eintraäge und leeren Tag aufnehmen
     Element elementIndexFiles = deleteTagIndexFiles();
     // Jetzt ist folgendes eingetragen
     // 
    
     Element rootElement = document.getRootElement();
     updateAttribute(rootElement, ATR_INDEX_FILES_MAX_ID, String.valueOf(maxIndexFileID));
      
     
     // Jetzt neu aufnehmen
     PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();    
     for (PM_PictureDirectory pd: metadatenContainer.getPictureDirectories()) {
        File xmlFile = pd.getPmIndexFile();  
        Element e = new org.dom4j.tree.DefaultElement(TAG_INDEX_FILE);              
        updateAttribute(e, ATR_INDEX_FILE_LG, Long.toString(xmlFile.length()));
        updateAttribute(e, ATR_INDEX_FILE_NAME, xmlFile.getPath());        
        elementIndexFiles.add(e);   
     }   
 
   
     // .. und zum Schluss Schreiben
     writeDocument();    
     
     xmlFile = null;
  }
  
 
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================

  // =====================================================
  // Konstruktor
  //
  // =====================================================
    private PM_XML_File_Session() {
        einstellungen = PM_Configuration.getInstance();
    }

    
    
  
  
  // =====================================================
  // readXMLFile()
  //
  // lesen pm_session.xml File.
  // Wenn nicht vorhanden, dann leer anlegen und herausschreiben
  // =====================================================
  private void readXMLFile( ) {  
  	
      if (xmlFile != null) return;    // File pm_session.xml bereits vorhanden
      
      // ---------------------------------------------------------------------
      // Es exusiert noch keine Session.xml-File.
      // dieses wird jetzt neu angelegt
      // ---------------------------------------------------------------------
      File homeBilder = einstellungen.getTopLevelPictureDirectory();
      String pathXMLFile = homeBilder.getAbsolutePath() + File.separator +  DIR_METADATEN_ROOT
                                                     + File.separator +  FILE_XML_SESSION;
      xmlFile = new File(pathXMLFile);
      rootTagName = TAG_SESSION;     
      openDocument(OPEN_CREATE); 
      // read maxIdNumber
      Element rootElement = document.getRootElement();
      maxIndexFileID = getAttributeInt(rootElement, ATR_INDEX_FILES_MAX_ID);
  }
  
  
  
  
  
  
  // =====================================================
  // deleteTagIndexFiles()
  //
  // =====================================================
  private Element deleteTagIndexFiles() {
     // alle alten Index-File Eintraege loeschen 
     Element root =  document.getRootElement();
     java.util.List elements = root.elements();     
     for (int i=0; i<elements.size(); i++) {
        Element element = (Element)elements.get(i); 
        if (element.getName().equals(TAG_INDEX_FILES)) {
            elements.remove(i);
            break;
        }         
     }
     Element neuesElement = new org.dom4j.tree.DefaultElement(TAG_INDEX_FILES);
     root.add(neuesElement);      
     
     return neuesElement;
  }
   
  
}
