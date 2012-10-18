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

import java.io.*;
import java.util.*;
 
 
  
import pm.picture.*;
import pm.search.*;
 
import pm.utilities.*;

/**
 * Fotoalbum-Eintrag
 * 
 * <toc type="komplex" name="v481" von="1998.03.01" bis="2000.02.01"
 * inhalt="test.ich.harz" index=" kal2008 kal2009kuechealle" qs="13" ort="
 * bad_sooden towcqnq" sg-name="b17" markieren="n"/>
 * 
 */
public class PM_SequenceAlbum extends PM_Sequence {
	
	// Offene Sequenz
	private String von = "";
	private String bis = "";
	private String index = "";
	private String ort = "";
	private String qual = "";
	private PM_Sequence seqClosed = null;
	private String seqClosedName = ""; // Geschl.-Sequ-Name in einer offenen
	 
	// Sequenz
	 
	// =====================================================
	// Konstruktor:
	// =====================================================
    // ----- init (aus xml-Datei) ------
	public PM_SequenceAlbum(String initPath, String shortName) {
		super(); 
		this.initPath = initPath;
		longName = getLongNameFromInitPath(initPath);
		setShortName(shortName);
	}
	public PM_SequenceAlbum( ) {
		super();
		longName = "temp";
	}
 
	
	public void setShortName(String shortName) {
		if (shortName != null && shortName.length() > 0) {
			this.shortName = shortName;
			this.numberOfShortName = getSequenceNumberFromName(shortName);
		}
	}
	
	// =====================================================
	// get/set
	// =====================================================	
	public String getVon() {
		return von;
	}
	
	
	public void setBis(String bis) {
		this.bis = bis;
	}

	public void setIndex(String index) {
		this.index = index;
	}
	public void setVon(String von) {
		this.von = von;
	}
	public void setSeqClosedName(String seqClosedName) {
		this.seqClosedName = seqClosedName;
	}
	
	public void setQual(String qual) {
		this.qual = qual;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}
	public void setSeqClosed(PM_Sequence seqClosed) {
			this.seqClosed = seqClosed;
	}
	
	
	public String getSeqClosedName() {
		return seqClosedName;
	}
 

	public String getBis() {
		return bis;
	}

	public String getIndex() {
		return index;
	}
	public String getQual() {
		return qual;
	}

	public String getOrt() {
		return ort;
	}

	public PM_Sequence getSeqClosed() {
		return seqClosed;
	}
	// ========================================================================
	// getSequenceType()
	// getSequenceCharacter()
	// ========================================================================
	@Override
	public SequenceType getSequenceType() {
		return  SequenceType.ALBUM;
	}
	 
	
	
	// ========================================================================
	// getTextFile()
	//treePath = stringToTreePath(path);
	// return File von der text-datei.
	// In die Text-Datei werden für jede Sequenz Informationen eingetragen.
	// ========================================================================
	@Override
	public File getTextFile() {	 
		return new File(PM_Configuration.getInstance().getSequencesDirVirtual(), "??" + ".txt");	            
	}
	
	// =====================================================
	// toString()
	// =====================================================
	@Override
	public String toString() {

		// letzter Eintrag im Pfad
		String ret = getLongName();

		
		if (shortName != null && shortName.length() > 0) {
			ret = ret + "<" + shortName + ">"; 
		}
		
		
		PM_Sequence seq = getSeqClosed();
		if (seq != null) {
			int anz = seq.getAnzahlBilder();
			return ret + "  (" + seq.shortName + ": " + toStringBilder(anz) + ")";
		} else {
			return ret + "  (" + PM_MSG.getMsg("winSeqNoSequence") + ")";
		}
		 
	}

	// =====================================================
	// getTypeString()
	//
	// Für u.a. das Text-Fenster (Beschreibung) um den Text zu editieren
	// =====================================================
	@Override
	public String getTypeString() {		 
		return PM_MSG.getMsg("photalbum");
	}
	
	@Override
    public String getSequenceCharacter() {
    	return SEQ_CHARACTER_ALBUM;
    }
	
	
	// =====================================================
	// getAlleBilder()
	// =====================================================
	@Override
	public List<PM_Picture> getAlleBilder() {			
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(this);
		PM_Search search = new PM_Search(searchExpr);
		int anzahlHits = search.search();
		if (anzahlHits == 0) {
			return new ArrayList<PM_Picture>();
		}
		return search.getPictureList(SearchSortType.SEQ);	 
	}
 
 
	
	
}
