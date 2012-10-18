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
package pm.sequence;

 
import pm.search.*;
 
import pm.utilities.*;
 
 
import pm.inout.PM_SequencesInout;
import pm.picture.*;

import java.io.File;
import java.util.*;


/** Klasse zur Behandlung einer Sequenz
 * 
 * 
 * 
 * 
 *  
 */
abstract public class PM_Sequence implements PM_Interface {
 
	
	 
	protected String initPath = "";  // nur in der Init-Phase
 	protected int numberOfShortName = 0;             // 11
	protected String shortName = "";     // "b11"
	protected String longName= "";     // "geburtstag freundin"
	protected int count = 0;          // Anzahl der Bilder
	
	
	private boolean stringLeaf = false; // f�r write wenn nodeLeaf String ist 
	
	 

	protected boolean textFile  = false;
	
	// =====================================================
	// Konstruktor:
	// =====================================================
	public PM_Sequence() {	 
	}
   
	// ===============================================================
	// getSequenceNumberFromName()
	//
	// "b11"  --->  11
	// ===============================================================
    protected int getSequenceNumberFromName(String sequenceName)  {
    	if (sequenceName.length() <= 1) {
    		return 0;
    	}
    	return PM_Utils.stringToInt(sequenceName.substring(1));
    }
	 
	
	
	// ========================================================================
	// getTextFile()
	//
	// return File von der text-datei.
	// In die Text-Datei werden f�r jede Sequenz Informationen eingetragen.
	// ========================================================================
	abstract public File getTextFile(); 
	public boolean hasTextFile() { 
		return textFile;   
	}
	public void  textFile(boolean textFile) {
		this.textFile = textFile;    
	}
	
 

	public void setAnzahlBilder(int count) {
		if (count < 0) {
			this.count = 0;
			return;
		}
		this.count = count;
	}	 
	public String getPath( ) {
 		return initPath;
 	}
	public void setPath(String  initPath) {
 		this.initPath = initPath;
 	}
	
	// =================================================
	// stringLeaf f�r write bei close:
	//   die Sequenz ist von einem String-Knoten 
	// ==================================================
	public void  setStringLeaf(boolean stringLeaf) {
		this.stringLeaf = stringLeaf;
 	}
	public boolean getStringLeaf( ) {
 		return stringLeaf;
 	}
 

	// =====================================================
	// makeSequence()
	//
	//  Wird �berschrieben (nur Closed sequences!!!)
	//
	// Wenn im Konstruktor eine pictureList �bergeben wurde,
	// dann hier  die Serie anlegen
	// =====================================================
	public boolean makeSequence( ) {	
		 return false;
	}
 

	// ========================================================================
	// getSequenceType()
	// getSequenceCharacter()  "n", "b", "s" 
	// ========================================================================
	abstract public SequenceType getSequenceType();
	public String getSequenceCharacter() {
		// wird �berladen (nur "n", "b", "s" )
		return "";
	}
	// ========================================================================
	// get
	// ========================================================================
	public SequenceType getType() {
		return getSequenceType();
	}
	public int getSequenceNumber() {
		return numberOfShortName;
	}
	public String getShortName() {
		return shortName; // --> "s1"
	}
	public String getLongName() {	 
		return longName; 
	}
 	public int getAnzahlBilder() {
		return count;
	}

	public void setLongName(String longName ) {	 
		this.longName = longName; 
	}
 	
	// =====================================================
	// getAlleBilder()
	//
	// wird �berschrieben
	// =====================================================
	public List<PM_Picture> getAlleBilder() {
		return new ArrayList<PM_Picture>();
	}

	
	
	
	// =====================================================
	// neuNummerieren()
	//
	// Seuenz neue Nummer 
	// =====================================================
	public void neuNummerieren(int neu) {
	}
	
	// =====================================================
	// getFilesFromDirectory()
	//
	// Wird in PM_SequenzDirectory �berschrieben
	// =====================================================
	public List getFileListFromDirectory() {
 		return new ArrayList(); 
	}



 
	// =====================================================
	// toStringBilder()
	// =====================================================
	protected String toStringBilder(int anz) {
		if (anz == 1) return anz + " Bild";
		return anz + " Bilder";
	}
	
	// =====================================================
	// toStringComboBox()
	// =====================================================
	public String toStringComboBox() {
		 // TODO   longName sollte eigentlich getPath() sein. Der stimmt jedoch nicht !!
		return longName + "(" + shortName + ": " + toStringBilder(getAnzahlBilder()) + ")";
	}
	
	// =====================================================
	// getTypeString()
	//
	// F�r u.a. das Text-Fenster um den Text zu editieren
	// =====================================================
	public String getTypeString() {		 
		return "muss �berschrieben werden";
	}

	// =====================================================
	// getListenName()
	//
	// F�r Anzeige in der Tabelle bzw. Combo-Box
	// =====================================================
	public String getListenName() {

		return getPath() + " (" + getShortName() + "-" + getAnzahlBilder() + ")";

		 
	}

	// =====================================================
	// loeschenSequenz()
	//
	// Sequenz wird unwiderruflich gel�scht
	// =====================================================
	public void deleteSequence() {
		deletePictureChain();
		// DB updaten
		PM_DatabaseLucene.getInstance().flush();
	}

	// =====================================================
	// aendernSequenz()
	//
	// Sequenz aendern
	// =====================================================
	public void modifySequence(List<PM_Picture> pictures) {
 		
		// (1) Sequenz alt l�schen
		deletePictureChain();
		// (2) neu erstell
		newPictureChain(pictures);
		PM_DatabaseLucene.getInstance().flush();
	}

	 

	// =====================================================
	// makeBasisSequenz()
	//
	// Diese Sequenz (es ist eine geschl. Sequenz) in eine Basis-Sequenz wandeln
	// =====================================================
	public void makeBasisSequenz() {
	 
		for (PM_Picture pic: getPictureListFromLucene()) {
			// for each picture
			for (String s: PM_Utils.getList(pic.meta.getSequence())) {	
				// for each sequence (b1_001)
				String[] ss = s.split("_");
				if (ss.length != 2) {
					break;
				}
				if (getShortName().equals(ss[0])) {
					s = s.replaceFirst(SEQ_CLOSED_NORMAL, SEQ_CLOSED_BASE);
					pic.meta.setSequenz(s);
				}
			}
		}
		longName = longName.replaceFirst(SEQ_CLOSED_NORMAL, SEQ_CLOSED_BASE);
	}

	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================
	// ============================== private
	// =====================================================

	// =====================================================
	// getListeFromLucene()
	//
	// return List von File's
	// =====================================================
	protected List<PM_Picture> getPictureListFromLucene() {

		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(this);
		PM_Search search = new PM_Search(searchExpr);
		int anzahlHits = search.search();
		if (anzahlHits == 0) {
			return new ArrayList<PM_Picture>();
		}
		return search.getPictureList(SearchSortType.SEQ);
	}

 
	public void computeNewSize() {
		// Anzahl der Pictures neu ermitteln
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(this);
		PM_Search search = new PM_Search(searchExpr);
		setAnzahlBilder(search.search());
	}
	
	 

	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================
	// ============= Einzige Stelle von neu und l�schen Sequenz
	// ========================================

	// =====================================================
	// newPictureChain() in: Picture-Liste
	//
	// Hier EINZIGE Stelle, wo eine neue Sequenz erzeugt wird.
	//
	// Voraussetzung: Es darf KEINE alte vorhanden sein
	// =====================================================
	protected void newPictureChain(List<PM_Picture> pictures) {

		Iterator<PM_Picture> it = pictures.iterator();
		int sNr = 0;
		while (it.hasNext()) {
			PM_Picture pic = it.next();
			updateMetadaten(pic, sNr);
			sNr++;
		}

		count = pictures.size();

		PM_DatabaseLucene.getInstance().flush();

	}

	// =====================================================
	// deletePictureChain()
	//
	// Hier EINZIGE Stelle, wo tats�chlich die gesamte Kette
	// UNWIDERRUFLICH gel�scht wird !!!!!
	// =====================================================
	protected void deletePictureChain() {
		 
		List<PM_Picture> pictures = getPictureListFromLucene();
		if (pictures.isEmpty()) {
			count = 0;
			return;
		}

		// l�schen
		Iterator<PM_Picture> it = pictures.iterator();
		while (it.hasNext()) {
			PM_Picture pic = it.next();
			String seqNeu = "";
			String seq = pic.meta.getSequence();
			String[] sa = seq.split(" ");
			for (int i = 0; i < sa.length; i++) {
				String s = sa[i];
				if (s.indexOf(shortName + LUCENE_SEQUENZ_TRENNER) != 0) {
					seqNeu += s + " ";
					continue;
				}
			}
			pic.meta.setSequenz(seqNeu);
		}

		count = 0;
		
		// --------------------------------------------
		// jetzt noch die Beschreibungsdatei l�schen
		// --------------------------------------------
	//	deleteTextFile();	
	}

	
	// =====================================================
	// deleteTextFile()
	// =====================================================
	public void deleteTextFile() {
		File file = getTextFile();
		if (file == null) return;
		if (!file.isFile()) return;
		file.delete();		 
	}
	
	
	// =====================================================
	// updateMetadaten()
	//
	// Setzt den neuen "seq" in die Metadaten:
	// wenn eine Basis-Sequenz: neuer Name eintragen und
	// einen ggf. vorhanden alten l�schen. Somit wird
	// die Basis-Sequenz wieder eindeutig.
	// wenn KEINE Basis-Sequenz: neue erg�nzen
	// =====================================================
	protected void updateMetadaten(PM_Picture pic, int intlfdNr) {
		String seq = pic.meta.getSequence();
		String lfdNr = PM_Utils.stringToString("00000"
				+ Integer.toString(intlfdNr), 3);
		String newSeq = shortName + LUCENE_SEQUENZ_TRENNER + lfdNr;
		if (getSequenceType() != SequenceType.BASE) {
			// KEINE Basis-Sequenz
			seq += " " + newSeq;
		} else {
			// Basis-Sequenz
			// in einer Loop nun eine Basis-Sequenz eliminieren
			String newTempSeq = "";
			String[] sa = seq.split(" ");
			for (int i = 0; i < sa.length; i++) {
 				if (sa[i].startsWith(SEQ_CLOSED_BASE)) {
					// Es ist eine Basis-Sequenz-Nr. Diese eliminieren
					// und Z�hler um eins vermindern
					String[] ss  = sa[i].split("_");
					if (ss.length != 2) continue;
					// jetzt Z�hler um eins vermindern.
					// Wenn auf null, dann Serie l�schen
 					PM_Sequence sMinus =  getSequenz(ss[0]);
					if (sMinus != null) {
						int count = sMinus.getAnzahlBilder() - 1;
						if (count > 0) {
							sMinus.setAnzahlBilder(count);
						} else {
							// TODO Serie aus  Baum (Base und Album) entfernen
							sMinus.setAnzahlBilder(0); 
						}					
					}
 					continue;
 				}
				newTempSeq += " " + sa[i];
			}
			// newTempSeq hat jetzt keine Basis-Sequenz mehr
			// Nun die neue anh�ngen
			seq = newTempSeq + " " + newSeq;
		}
		// neue Sequenz setzen
		pic.meta.setSequenz(seq);
	}
	
 
	 
	
	 
	
	 
	// =====================================================
	// getSequenz()
	//
	// alle Sequencen holen (aus den Knoten) --> kostet Zeit
	// und die Sequenz suchen
	// =====================================================
	public PM_Sequence getSequenz(String name) {
		PM_Sequence seq = null;
		List<PM_Sequence> list = getSequenceList();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			seq = (PM_Sequence) it.next();
			if (seq.getShortName().equals(name))
				return seq;
		}
		return seq;
	}

	// =====================================================
	// getSequenzFromAll()
	//
	// Von allen geschlossenen Sequenzen
	//
	// alle Sequencen holen (aus den Knoten) --> kostet Zeit
	// und die Sequenz suchen
	// =====================================================
	static public PM_Sequence getSequenzFromAll(String name) {
		PM_Sequence seq = null;
		
		PM_TreeModel base = PM_TreeModelBase.getInstance();
		PM_TreeModel extended = PM_TreeModelExtended.getInstance();
		PM_TreeModel newl = PM_TreeModelNew.getInstance();
		List<PM_Sequence> listeGeschlSequenzen = base.getSequenceList();
		listeGeschlSequenzen.addAll(extended.getSequenceList());
		listeGeschlSequenzen.addAll(newl.getSequenceList());	
				 
		Iterator it = listeGeschlSequenzen.iterator();
		while (it.hasNext()) {
			seq = (PM_Sequence) it.next();
			if (seq.getShortName().equals(name))
				return seq;
		}
		return seq;
	}

	
	
	
	 
	 
	static public int getNextFreeSequenceNumber() {
		int nextNumber = PM_SequencesInout.getInstance().getMaxSequenceNumber();
			
		if (nextNumber == 0) {
			// Letzte belegte Nummer suchen
			// Nummern sind nur in den Sequenzen Base, Extended und New eingetragen
			List<PM_Sequence> liste = PM_TreeModelBase.getInstance().getSequenceList();
			liste.addAll(PM_TreeModelExtended.getInstance().getSequenceList());
			liste.addAll(PM_TreeModelNew.getInstance().getSequenceList());
			for (PM_Sequence seq: liste) {
				nextNumber = Math.max(nextNumber, seq.numberOfShortName);
			}
		}		
		nextNumber++;
		PM_SequencesInout.getInstance().setMaxSequenceNumber(nextNumber);
		
		return nextNumber;
	}
	 
	static public int getNextFreeAlbumSequenceNumber() {
		int nextNumber = PM_SequencesInout.getInstance().getMaxSequAlbumNumber();
		nextNumber++;
		PM_SequencesInout.getInstance().setMaxSequAlbumNumber(nextNumber);
		
		return nextNumber;
	}
	
	
	// =====================================================
	// getSequenceList()
	//
	// wird �berschrieben
	// =====================================================
	public List<PM_Sequence> getSequenceList( ) {	
		return new ArrayList<PM_Sequence>();
	}
	
	
	
	// =====================================================
	// getLongNameFromInitPath()
	//
	// f�r init-Phase
	// =====================================================
	protected String getLongNameFromInitPath(String initPath) {
		if (initPath == null || initPath.length() == 0) {
			return "unbekannter Name";
		}
		String[] sa = initPath.split(SPLIT_PUNKT);
		if (sa.length == 0) {
			return initPath;
		}
		return sa[sa.length - 1];
	}

	
	// =====================================================
	// setSequenzNumber()
	//
	// Es Wird die SequenzNumber in die Picture-Metadaten eingetragen
	// =====================================================
	protected void setSequenzNumber(PM_Picture picture, int number) {
		String lfdNr = PM_Utils.stringToString("00000"
				+ Integer.toString(number), 4);
		picture.meta.setSequenz(picture.meta.getSequence() + " "
				+ getShortName() + LUCENE_SEQUENZ_TRENNER + lfdNr);
	}
	// =====================================================
	// sequenzenNeuNummerieren()
	//
	// Alle Sequenzen ab "anfang" neu nummerieren
	// =====================================================
	static public int sequenzenNeuNummerieren(int anfang) {
		 
		
		 
		PM_TreeModel base = PM_TreeModelBase.getInstance();
		PM_TreeModel extended = PM_TreeModelExtended.getInstance();
		List<PM_Sequence> liste = base.getSequenceList();
		liste.addAll(extended.getSequenceList());
		
		Iterator<PM_Sequence> it = liste.iterator();
		int neu = anfang;
		int anz = 0;
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
			SequenceType type = seq.getType();
			if (type == SequenceType.BASE || type == SequenceType.EXTENDED) {
				String sgNameAlt = seq.getShortName();
/////				String sgNameNeu = seq.getClosedTypeName() + neu;
				seq.neuNummerieren(neu);
				neu++;
				anz++;
				// Offene Referenz �ndern
				Iterator<PM_Sequence> itx = liste.iterator();
				while (itx.hasNext()) {
					PM_Sequence seqx = itx.next();
					SequenceType typex = seqx.getType();
					if (typex == SequenceType.ALBUM) {
						PM_SequenceAlbum album = (PM_SequenceAlbum)seqx;
						String sg_name = album.getSeqClosedName();
						if (sg_name.equals(sgNameAlt)) {
						//	album.setSeqClosedName(sgNameNeu);
							System.out.println("            Close name. alt ="
									+ sg_name + ",neu = "); /// + sgNameNeu);
						}
					}
				}
			}
		} // while
		return anz;

	}
	// =====================================================
	// getSequenzKurzName()
	//
	// Holen des "eindeutigen" Sequenznamens
	// =====================================================
	public static String getSequenzKurzName(String seq) {
		String[] s = seq.split(LUCENE_SEQUENZ_TRENNER);
		if (s.length >= 1)
			return s[0];
		return null;

	}
}