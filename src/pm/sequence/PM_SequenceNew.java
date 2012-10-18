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

 
 
import pm.picture.*;
import pm.utilities.*;
 

import java.io.*;
import java.text.*;
import java.util.*;
 
/**
 *  Nur in der Init-Phase werden neue Instanzen erzeugt !!!!
 * 
 *  
 *
 */
public class PM_SequenceNew extends PM_Sequence   {
	
	private List<PM_Picture> pictureList;
	
	// =====================================================
	// Konstruktor:  nur für die INIT-Phase
	// =====================================================
    // ----- init (aus xml-Datei) ------
	public PM_SequenceNew(String initPath, String shortName) {
		super();
		this.initPath = initPath;
		this.shortName = shortName;
		numberOfShortName = getSequenceNumberFromName(shortName);
		longName = getLongNameFromInitPath(initPath);
	}
	
	// ---- während der init-Phase neue Bilder gefunden -----
	public PM_SequenceNew(String initPath, List<PM_Picture> pictureList) {
		super();
		this.pictureList = pictureList;
		this.initPath = initPath;
		longName = getLongNameFromInitPath(initPath);
		 
		makeSequence();
	}
	
	// =====================================================
	// makeSequence()
	//
	// Wenn im Konstruktor eine pictureList übergeben wurde,
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
	// getSequenceList()
	//
	// Kostet Zeit !!!
	// =====================================================
	@Override
	public List<PM_Sequence> getSequenceList( ) {	
		return PM_TreeModelNew.getInstance().getSequenceList();
	}
	
	@Override
    public String getSequenceCharacter() {
    	return SEQ_CHARACTER_NEW;
    }
	
	
	
	// ========================================================================
	// getTextFile()
	//
	// return File von der text-datei.
	// In die Text-Datei werden für jede Sequenz Informationen eingetragen.
	// ========================================================================
	@Override
	public File getTextFile() {	 
		return new File(PM_Configuration.getInstance().getSequencesDirNew(), "???" + ".txt");	            
	}
	
	// =====================================================
	// getAlleBilder()
	// =====================================================
	@Override
	public List<PM_Picture> getAlleBilder() {		 
		return getPictureListFromLucene();		 
	}
	
	// =====================================================
	// toString()
	// =====================================================
	@Override
	public String toString() {

		// letzter Eintrag im Pfad
		String ret = getLongName() + "  ";
 
		 
			// In path steht die Aufnahmezeit als String (long)
			Long time = System.currentTimeMillis();

			try {
				time = new Long(initPath);
				if (PM_Utils.isToDay(time)) {
					Date date = new Date(time);
					DateFormat df = new SimpleDateFormat("HH:mm");
					String toDay = "    " + PM_MSG.getMsg("sequNewToDay")  + "    ";
					ret = toDay + df.format(date);
				} else {
					Date date = new Date(time);
					DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
					ret = df.format(date);
				}
			} catch (NumberFormatException e) {
				ret = "ERROR. path = " + initPath;
			}
			return ret + " (" + shortName + ": " + toStringBilder(getAnzahlBilder()) + ")";

	 
		 
	}
	// =====================================================
	// getTypeString()
	//
	// Für u.a. das Text-Fenster um den Text zu editieren
	// =====================================================
	@Override
	public String getTypeString() {		 
		return "Neue Bilder";
	}
	@Override
	public SequenceType getSequenceType() {
		 
		return SequenceType.NEW;
	}
	
	 
}
