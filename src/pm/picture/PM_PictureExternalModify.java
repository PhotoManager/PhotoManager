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

 
import pm.utilities.PM_Interface;
import pm.utilities.PM_Utils;

import java.io.File;
import java.util.*;

/** Alle zu einem PM_Picture geh�renden Bilder, die bearbeitet werden/wurden 
 *
 *  Unter dem Verzeichnis pm.metadaten wird ein weiteres Verzeichnis (pm.bilder_bearbeiten)
 *  angelegt.
 *
 *  (1) Ein Bild wurde das erste Mal bearbeitet:
 *      Das Originalbild wird in das Verzeichnis pm.bilder_bearbeiten mit der
 *      Endung   *.jpg_orig gespeichert. Datum: Kopierdatum
 *  (2) Das Bild wird ein zweites und weiters Mal barbeitet:
 *      Das zuvor bearbeitete wird mit der Endung *.jpg_b1 (*.jpg_b2 ...) kopiert
 *  
 */
public class PM_PictureExternalModify implements PM_Interface  {
    
    private PM_Picture picture;
    private File dirBilderBearbeiten;
    
    
    private Collection<EinBild> filesBilderBearbeitet = new TreeSet<EinBild>(TIME_ORDER); 
     
    private File fileSicherungTemp = null;
    
    // =====================================================
    // Konstruktor 
    // =====================================================
    public PM_PictureExternalModify(PM_Picture picture) {
        this.picture = picture;
        init();
    }

  // =====================================================
  // getFileList()
  //
  // =====================================================
  public EinBild[] getFileList( ) {   
     return (EinBild[]) filesBilderBearbeitet.toArray(new EinBild[filesBilderBearbeitet.size()]); 
  }
   
  // =====================================================
  // startBearbeitung()
  //
  // Die aktuelle Datei, das ist die, die bearbeitet wird (gleich picture-Instanz),
  // wird hier vor der Bearbeitung gesichert.
  //
  // Diese Methode wird unmittelbar VOR dem Start des externen Programms aufgerufen.
  //
  // =====================================================
  public void startBearbeitung( ) { 
      fileSicherungTemp.delete();
      PM_Utils.copyFile(picture.getFileOriginal(), fileSicherungTemp);
      fileSicherungTemp.setLastModified(picture.getFileOriginal().lastModified());
  }    
  // =====================================================
  // endeBearbeitung()
  //
  // Diese Methode wird unmittelbar NACH Beendigung des externen Programms aufgerufen.
  //
  // Wurde die aktuelle Datei nicht ver�ndert, so wird die Sicherung
  // gel�scht.
  // Wurde die aktuelle Date ver�ndert, so wird die Sicherung umbenannt. 
  // =====================================================
  public boolean endeBearbeitung( ) { 
    // ---------------------------------------------------  
    // l�schen Sicherung wenn keine Ver�nderung
    // ---------------------------------------------------
    if (PM_Utils.dateienIdentisch(picture.getFileOriginal(), fileSicherungTemp)) {
      // Es wurde nichts ge�ndert. L�schen Sicherung
      fileSicherungTemp.delete();
      return false;
    }
    
    // ---------------------------------------------------  
    // Die Datei am Originalplatz wurde ge�ndert
    // (es ist immer die geladene Datei)
    // ---------------------------------------------------   
    
    // holen geladene "EinBild"-Instanz
    File fileGeladen = getFileGeladen( );
     
    if ( !fileGeladen.exists()) {
       // Es existiert KEINE Sicherung 
       fileSicherungTemp.renameTo(fileGeladen);
    } else {
       // Es exisiert eine Sicherung. Tempfile l�schen
       fileSicherungTemp.delete(); 
    }
    
    // ... und eine neue Instanz anh�ngen (von der nun geladenen Datei)
    filesBilderBearbeitet.add(new EinBild(getFileSicherungNeu( )));
    setzenBildGeladen( ); 
    
    // if supporting mpeg files (for vdr), delete it.
    // So if transferring to vdr a new mpeg file shall be create
	File fileMpeg = PM_Utils.getFileMPEG(picture.getFileOriginal());
	fileMpeg.delete();	
    
    
    return true;
  } 
  
  // ========================================================
  // ladenBild()
  //  
  // 1. aktuell geladenes: - aktuelles (geladenes) "EinBild" holen
  //                       - rename Origplatz to Sicherung der neuen Instanz
  // 2. zu ladendes Bild:  - rename Sicherung to Origplatz
  // ========================================================   
  
   public void  ladenBild(int index) { 
     if (index > filesBilderBearbeitet.size() || index < 0) return;
     
     // Pr�fen, ob bereits geladen
     Object[] array = filesBilderBearbeitet.toArray();  
     EinBild geladenNeu = (EinBild)array[index];
     if (geladenNeu.getGeladen()) return; // bereits geladen
     
     // Instanz vom aktuellen Bild holen ...
     EinBild geladen = getEinBildGeladen( );
     if (!geladen.getGeladen()) return;  // darf eigentlich nicht sein
     File fileGeladen = geladen.getFile();
 //    fileGeladen.delete(); // vorsichtshalber l�schen
     // ... und File Orig umbenennen
     geladen.setGeladen(false);
     if ( !fileGeladen.exists()) {
       PM_Utils.copyFile(picture.getFileOriginal(), fileGeladen);
       fileGeladen.setLastModified(picture.getFileOriginal().lastModified());  
     }
      
     
     // 
      
     geladenNeu.setGeladen(true);
     PM_Utils.copyFile(geladenNeu.getFile(), picture.getFileOriginal());
     picture.getFileOriginal().setLastModified(geladenNeu.getFile().lastModified()); 
     
   } 

  // ========================================================
  // loeschenBild()
  //  
  // Original und geladene Bilder d�rfen nicht gel�scht werden
  // (bereits geprueft)
  // ========================================================   
  
   public void  loeschenBild(int index) {       
     if ( !hasEinBildAt(index) )  return;  // unzulaessiger Index
     // **** ein einfaches remove funktioniert nicht !!!!!!!
     Collection<EinBild> col = new TreeSet<EinBild>(TIME_ORDER);
     int i = 0;
     Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       EinBild einBild = (EinBild)iterator.next();
       if (i == index) {
           // Bild loeschen
           File f = einBild.getFile();
           f.delete();
       } else {
           col.add(einBild);
       }
       i++;
     } 
     
     filesBilderBearbeitet = col;
   }
   
   
  // =====================================================
  // getIndexGeladen()
  //
   //   ***** nicht so gut !!!!  ***  (Collection ist eben mist)
  // =====================================================
  public int getIndexGeladen( ) { 
     int index = 0;
     Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       EinBild einBild = (EinBild)iterator.next();
       if (einBild.getGeladen()) return index;
       index++;
     }  
     return 0;
  }     
  // =====================================================
  // getEinBild()/hasEinBildAt()
  //
  // =====================================================
  public EinBild getEinBildAt(int index) { 
     if ( !hasEinBildAt(index)) return null;  // unzulaessiger Index
     Object[] array = filesBilderBearbeitet.toArray(); 
     return (EinBild)array[index];
  }  
  public boolean hasEinBildAt(int index) { 
     if (index >= filesBilderBearbeitet.size() || index < 0) return false; 
     return true;
  }     
   
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================
// ============================  PRIVATE ============================================    
    
  // =====================================================
  // init()
  //
  // =====================================================
  private void init( ) { 
   
    // Verzeichnis pm.bilder_bearbeiten anlegen wenn nicht vorhanden
    File dirMetadaten = new File(picture.getFileOriginal().getParent() + File.separator + DIR_METADATEN);
    dirBilderBearbeiten = new File(dirMetadaten.getPath() + File.separator + DIR_BILDER_BEARBEITEN);
    dirBilderBearbeiten.mkdirs(); 
    
    // Sicherungsfile ermitteln
    String fileSicherungPath = dirBilderBearbeiten.getPath() 
                              + File.separator 
                              + picture.getFileOriginal().getName() + "_temp";
    fileSicherungTemp = new File(fileSicherungPath);
    
    // Sicherungsfile l�schen wenn vorhanden (darf eigentlich nicht sein)
    fileSicherungTemp.delete();
    
    // alle implements Directory "dirBilderBearbeiten" lesen und 
    // in die Collection "filesBilderBearbeitet" schreiben (sortiert nach Dateidatum)
    File[] files = dirBilderBearbeiten.listFiles();
    for (int i=0; i<files.length; i++) {
      File file = files[i];
      if (file.getName().indexOf(picture.getFileOriginal().getName()) == 0) {
          // ein File wurde gefunden
          filesBilderBearbeitet.add(new EinBild(file));
      }      
    }
   
    // ------------------------------------------------------------
    // Geladenes Bild suchen. (�ber das Datum)
    //
    // O.K. wenn das Datum vom Originalfile eine Sicherung hat.
    // Wenn nein, dann noch eine "EinBild"-Instanz erzeugen.
    // ------------------------------------------------------------
    long timeOrig = picture.getFileOriginal().lastModified();
    boolean hasEinBildOrig = false;
    Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       EinBild einBild = (EinBild)iterator.next();
       if (timeOrig == einBild.getDatumLong()) {
           hasEinBildOrig = true;
           break; // Instanz vorhanden
       }
     }
    if ( !hasEinBildOrig ) {
       // Keine Sicherung vorhanden. Virtuelle Instanz erzeugen
       filesBilderBearbeitet.add(new EinBild(getFileSicherungNeu()));
    }
    
    // geladenes Bild markieren
    setzenBildGeladen( );         
  }     
    
  // =====================================================
  // setzenBildGeladen()
  //
  // =====================================================
  private void setzenBildGeladen( ) {    
    // geladenes Bild markieren
    long timeOrig = picture.getFileOriginal().lastModified();
    Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       EinBild einBild = (EinBild)iterator.next();
       einBild.setGeladen(timeOrig == einBild.getDatumLong());  // true wenn Gleichheit
     }             
  }

 
  
  // =====================================================
  // getFileSicherungNeu()
  //
  // Letzte Sicherung + 1
  // =====================================================
  private File getFileSicherungNeu( ) { 
    int maxExt = getMaxExtension();
  //  if (maxExt < 1) maxExt = 0;
    maxExt++;
    String fileNameNeu = dirBilderBearbeiten.getPath() 
                              + File.separator 
                              + picture.getFileOriginal().getName() + "_b" + Integer.toString(maxExt);     
    return  new File(fileNameNeu);
  } 

  
  // =====================================================
  // getFileGeladen()
  //
  // Aus dem Sicherungsverzeichnis
  // =====================================================
  private File getFileGeladen( ) { 
     return getEinBildGeladen( ).getFile();   
  } 
  private EinBild getEinBildGeladen( ) { 
     EinBild einBild = null;
     Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       einBild = (EinBild)iterator.next();
       if (einBild.getGeladen()) return einBild;
     }  
     return einBild;  // es wurde kein File markiert
  } 
  
  
  // =====================================================
  // getMaxExtension()
  //
  // =====================================================
  private int getMaxExtension( ) { 
     int ext = -1;
     Iterator iterator = filesBilderBearbeitet.iterator();
     while (iterator.hasNext()) {     
       EinBild einBild = (EinBild)iterator.next();
       int e = einBild.getExtension();
       if (e > ext) ext = e;
     }
     return ext;
  } 
      
   // =====================================================
   // Compare fuer das Sortieren nach Datum (Zeit in Milliseconds)
   // =====================================================
      
   static private final Comparator<EinBild> TIME_ORDER = new Comparator<EinBild>() {
        public int compare(EinBild eb1, EinBild eb2) {                              
            int ext1 =  eb1.getExtension();
            int ext2 =  eb2.getExtension();
            if (ext1 < ext2) return -1;
            return 1;
            
        };
    };
     
 

  // ===============================================================
  // ===============================================================
  // ===============================================================
  //
  //  Innerclass: ein bearbeitetes Bild
  //
  // ===============================================================
  // ===============================================================
  // ===============================================================
  public class EinBild {
     
     private File file; 
     private int extension;
     private boolean geladen  = false;
     
    // =====================================================
    // Konstruktor 
    // =====================================================
    public  EinBild(File file)  {
       this.file = file;  
      
       // extension ermitteln: _b0, _b1 .....
       extension = -1;
       String[] a = file.getName().split("_b");
       if (a.length > 1) {
           String num = a[a.length-1];
           extension = PM_Utils.stringToInt(num);         
       }
    }
      
    // =====================================================
    // getDatumString()/getDatumLong() 
    // =====================================================
    public String getDatumString()  {         
        return PM_Utils.getDateString(new Date(getDatumLong()) ,"dd.MM.yyyy   HH:mm:ss"  );
    }     
    public long getDatumLong()  {
      long time = 0;
      if (file.exists()) {
         time = file.lastModified();    
       } else {
         time = picture.getFileOriginal().lastModified(); 
       }
        
      return time;          
    }  
    
    // =====================================================
    // setGeladen()/getGeladen() 
    // =====================================================
    public void setGeladen(boolean geladen)  {         
       this.geladen = geladen;
    }     
    public boolean getGeladen()  {
      return geladen;          
    }  
    // =====================================================
    // getFile()/setFile()
    // =====================================================
    public File getFile()  {
       return file;        
    }  
 /*   
    public void setFile(File file)  {
       this.file = file;        
    }
  */
    // =====================================================
    // getExtension()
    // =====================================================
    public int getExtension()  {
       return extension;        
    }      
    
    
  }  // End innerclass 
  
    
    
} // End class
