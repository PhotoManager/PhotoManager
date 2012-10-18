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
 
import java.util.List;

 

import pm.picture.*;
import pm.utilities.*;
 
public class PM_SequenceOriginal extends PM_Sequence  {
	
	protected File dirAlleBilder = null; // Directory für type ALLE
	 
	// =====================================================
	// Konstruktor:
	// =====================================================
	public PM_SequenceOriginal(String longName ) {
		super();
		this.longName = longName; 
	}

	
	public void setDirAlleBilder(File dirAlleBilder) {
		this.dirAlleBilder = dirAlleBilder;
	}
	
	
	// ========================================================================
	// getTextFile()
	//
	// return File von der text-datei.
	// In die Text-Datei werden für jede Sequenz Informationen eingetragen.
	// ========================================================================
	@Override
	public File getTextFile() {	 
		return new File(PM_Configuration.getInstance().getSequencesDir(), "???" + ".txt");	            
	}
	
	 
	
	
	// =====================================================
	// getFilesFromDirectory()
	//
	// dies ist ein Hack !!!!
	// =====================================================
	public List getFileListFromDirectory() {

		List fileList = new ArrayList();		 

		if (dirAlleBilder == null)
			return fileList; // kein Directory
		if (!dirAlleBilder.isDirectory())
			return fileList; // kein Directory

		return Arrays.asList(dirAlleBilder.listFiles());
	}

	public String getLongName() {
		 
			return dirAlleBilder.getName();

		 
	}
	
	// =====================================================
	// getAlleBilder()
	//
	// =====================================================
	@Override
	public List<PM_Picture> getAlleBilder() {
		List<PM_Picture> liste = new ArrayList<PM_Picture>();
		if (dirAlleBilder == null)
			return liste; // kein Directory
		if (!dirAlleBilder.isDirectory())
			return liste; // kein Directory
		File[] files = dirAlleBilder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (PM_Utils.isPictureFile(file)) {
				PM_Picture pic = PM_Picture.getPicture(file);
				if (pic == null)
					continue;
				liste.add(pic);
			}
		}
		return liste;
	}
	
	// =====================================================
	// toString()
	// =====================================================	
	@Override
	public String toString() {

		// letzter Eintrag im Pfad
		String ret = getLongName() + "  ";

		 
			ret += " (" + getAnzahlBilder() + " Bilder)";
			return ret;
	}
	
	// =====================================================
	// getTypeString()
	//
	// Für u.a. das Text-Fenster um den Text zu editieren
	// =====================================================
	@Override
	public String getTypeString() {		 
		return "'reales' Verzeichnis";
	}


	@Override
	public SequenceType getSequenceType() {
		return SequenceType.ORIGINAL;
	}
}
