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

import java.io.File;
import java.util.*;

 

import pm.picture.*;
import pm.utilities.*;
 

public class PM_SequenceExtended extends PM_Sequence   {
	
	static private Set<PM_SequenceExtended> sequenceInAlbum = new HashSet<PM_SequenceExtended >();
	private List<PM_Picture> pictureList;
	
	// =====================================================
	// Konstruktor:
	// =====================================================
    // ----- init (aus xml-Datei) ------
	public PM_SequenceExtended(String initPath, String shortName) {
		super();
		this.initPath = initPath;
		this.shortName = shortName;
		numberOfShortName = getSequenceNumberFromName(shortName);
		longName = getLongNameFromInitPath(initPath);
	}
	public PM_SequenceExtended(String longName,List<PM_Picture> pictureList ) {
		super();
		this.longName = longName;
		this.pictureList = pictureList;
		// TODO  workarround
		initPath = longName;
		 
	}
	
	// =====================================================
	// getSequenceList()
	//
	// Kostet Zeit !!!
	// =====================================================
	@Override
	public List<PM_Sequence> getSequenceList( ) {	
		return PM_TreeModelExtended.getInstance().getSequenceList();
	}
	
	// =====================================================
	// makeSequence()
	//
	// Wenn im Konstruktor eine pictureList übergeben wurde,
	// dann hiert die Serie anlegen
	// =====================================================
	@Override
	public boolean makeSequence( ) {	
		if (pictureList != null) {			
			numberOfShortName = getNextFreeSequenceNumber ();
			shortName = getSequenceCharacter() + numberOfShortName;
			newPictureChain(pictureList);
			pictureList = null;
			return true;
		}
		return false;
	}
 
	
	// ========================================================================
	// getTextFile()
	
	//
	// return File von der text-datei.
	// In die Text-Datei werden für jede Sequenz Informationen eingetragen.
	// ========================================================================
	@Override
	public File getTextFile() {	 
		return new File(PM_Configuration.getInstance().getSequencesDirNormal(), "?????" + ".txt");	            
	}
	
	// =====================================================
	// getAlleBilder()
	// =====================================================
	@Override
	public List<PM_Picture> getAlleBilder() {		 
		return getPictureListFromLucene();		 
	}
	
	
	// ========================================================================
	// getSequenceType()
	// getSequenceCharacter()
	// ========================================================================
	@Override
	public SequenceType getSequenceType() {
		return  SequenceType.EXTENDED;
	}
	@Override
    public String getSequenceCharacter() {
    	return SEQ_CHARACTER_EXTENDED;
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
	// Für u.a. das Text-Fenster um den Text zu editieren
	// =====================================================
	@Override
	public String getTypeString() {		 
		return "'normale' Serie";
	}
	
	
	static public Set<PM_SequenceExtended> getSequenceInAlbum () {
		return sequenceInAlbum;
	}
	
	static public boolean addSequenceInAlbum(PM_Sequence seq) {
		if (seq instanceof PM_SequenceExtended) {
			if (sequenceInAlbum.add((PM_SequenceExtended) seq)) {
				return true;
			}
		}
		return false;
	}
	
	 
	
}
