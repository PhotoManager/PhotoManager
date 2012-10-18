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
import pm.utilities.*;
 
 

public class PM_SequenceBase extends PM_Sequence  {

	 private List<PM_Picture> pictureList;
	 static private Set<PM_SequenceBase> sequenceInAlbum = new HashSet<PM_SequenceBase >();
	 
	// =====================================================
	// Konstruktor:
	// =====================================================
    // ----- init (aus xml-Datei) ------
	public PM_SequenceBase(String initPath, String shortName) {
		super();
		this.initPath = initPath;
		this.shortName = shortName;
		numberOfShortName = getSequenceNumberFromName(shortName);
		longName = getLongNameFromInitPath(initPath);
	}
	public PM_SequenceBase(String longName ,List<PM_Picture> pictureList) {
		super();
		this.longName = longName;
		this.pictureList = pictureList;
		
		 
		// TODO  workarround
		initPath = longName;
	}
	
	// =====================================================
	// makeSequence()
	//
	// Wenn im Konstruktor eine pictureList �bergeben wurde,
	// dann hiert die Serie anlegen
	// =====================================================
	@Override
	public boolean makeSequence() {	
		if (pictureList != null) {			
			numberOfShortName = getNextFreeSequenceNumber ();
			shortName = getSequenceCharacter() + numberOfShortName;
			newPictureChain(pictureList);
			pictureList = null;
			return true;
		}
		return false;
	}
 
	
	// =====================================================
	// getAlleBilder()
	//
	// return alle Bilder dieser Sequenz
	// =====================================================
	@Override
	public List<PM_Picture> getAlleBilder() {		 
		return getPictureListFromLucene();		 
	}

	
	// =====================================================
	// getPictureList()
	// =====================================================
	public List<PM_Picture> getPictureList() {		 
		return pictureList ;		 
	}
	
	// ========================================================================
	// newSequenceChain()
	//
	// Es wird eine Sequenz angelegt.
	// Eine vorhandene wird ggf. gel�scht.
	// ========================================================================
	@Override
	protected void newPictureChain(List<PM_Picture> pictures) {	 
		deletePictureChain();	
		super.newPictureChain(pictures);
	}
	
	
	// ========================================================================
	// deleteSequenceChain()
	//
	// Die gesamte Liste in den Metadaten wird unwiderruflich gel�scht.
	// Es wird auch die Textdatei gel�scht.
	// (die Instanz bleibt nat�rlich vorhanden)
	// ========================================================================
	@Override
	public void deletePictureChain() {	 
		super.deletePictureChain();	  
	}
	
	// ========================================================================
	// updateSequence()
	//
	// Die gesamte Liste in den Metadaten wird unwiderruflich gel�scht.
	// Es wird auch die Textdatei gel�scht.
	// (die Instanz bleibt nat�rlich vorhanden)
	// ====================setSequenzNumber====================================================
//	@Override
//	public void updateSequence() {	 
//		super.updateSequence();	  
//	}

	// ========================================================================
	// getTextFile()	
	//
	// return File von der text-datei.
	// In die Text-Datei werden f�r jede Sequenz Informationen eingetragen.
	// ========================================================================
	@Override
	public File getTextFile() {	 
		return new File(PM_Configuration.getInstance().getSequencesDirBase(), "???" + ".txt");	            
	}	
	
	// ========================================================================
	// getSequenceType()
	// getSequenceCharacter()
	// ========================================================================
	@Override
	public SequenceType getSequenceType() {
		return  SequenceType.BASE;
	}
	@Override
    public String getSequenceCharacter() {
    	return SEQ_CLOSED_BASE;
    }
	
	
	// =====================================================
	// toString()
	// =====================================================
	@Override
	public String toString() {

		return longName + "   (" + shortName + ": "
				+ toStringBilder(getAnzahlBilder()) + ")";
	}
	// =====================================================
	// getTypeString()
	//
	// F�r u.a. das Text-Fenster um den Text zu editieren
	// =====================================================
	@Override
	public String getTypeString() {		 
		return "Basis - Serie";
	}
	
	// =====================================================
	// getSequenceList()
	//
	// Kostet Zeit !!!
	// =====================================================
	@Override
	public List<PM_Sequence> getSequenceList( ) {	
		return PM_TreeModelBase.getInstance().getSequenceList();
	}
	
	// =====================================================
	// setSequenzNumber()
	//
	// Es Wird die SequenzNumber in die Picture-Metadaten eingetragen.
	// Jedes Bild darf nur EINER Basis-Serie angeh�ren.
	// Hier evtl. vorhandene l�schen.
	// =====================================================
	@Override
	protected void setSequenzNumber(PM_Picture picture, int number) {

		
		String lfdNr = PM_Utils.stringToString("00000"
				+ Integer.toString(number), 3);
		String newSeq = shortName + LUCENE_SEQUENZ_TRENNER + lfdNr;

		// Basis-Sequenz
		// in einer Loop nun eine Basis-Sequenz eliminieren
		String newTempSeq = "";
		String seq = picture.meta.getSequence();
		String[] sa = seq.split(" ");
		for (int i = 0; i < sa.length; i++) {
			if (sa[i].startsWith(SEQ_CLOSED_BASE)) {
				// Es ist eine Basis-Sequenz-Nr. Diese eliminieren
				// und Z�hler um eins vermindern
				String[] ss = sa[i].split("_");
				if (ss.length != 2)
					continue;
// *** z.Zt. nicht m�glich, da gesamt-sequenz-liste nicht mehr vorhanden !!!!
//				PM_Sequence sMinus = PM_AllSequences_deprecated.getInstance()
//						.getSequenz(ss[0]);
//				if (sMinus != null) {
//					sMinus.setAnzahlBilder(sMinus.getAnzahlBilder() - 1);
//				}
//				continue;
			}
			newTempSeq += " " + sa[i];
		}
		// newTempSeq hat jetzt keine Basis-Sequenz mehr
		// Nun die neue anh�ngen
		seq = newTempSeq + " " + newSeq;

		// neue Sequenz setzen
		picture.meta.setSequenz(seq);

	}
	
	static public Set<PM_SequenceBase> getSequenceInAlbum () {
		return sequenceInAlbum;
	}
	
	static public boolean addSequenceInAlbum(PM_Sequence seq) {
		if (seq instanceof PM_SequenceBase) {
			if (sequenceInAlbum.add((PM_SequenceBase) seq)) {
				return true;
			}
		}
		return false;
	}
	
	
}
