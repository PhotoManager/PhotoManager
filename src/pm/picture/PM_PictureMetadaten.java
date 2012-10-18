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

import java.util.*;
 
import java.awt.*;
import java.io.*;


/**
 * The metadata of the picture.
 * 
 */
public class PM_PictureMetadaten implements PM_Interface {

	 
	static public final int UNKNOWN = 0;
	static public final int LOESCHEN_INDEX_FILE = 1;  
	static public final int INDEX_1 = 10;
	static public final int QS = 11;
	static public final int BEMERKUNGEN = 12;
	static public final int HOEHE = 13;
	static public final int BREITE = 14;
	static public final int CUT_X = 15;
	static public final int CUT_Y = 16;
	static public final int CUT_BREITE = 17;
	static public final int CUT_HOEHE = 18;
	static public final int CUT_RECTANGLE = 19;
	static public final int INDEX_2 = 20;
	static public final int ROTATE = 21;
	static public final int DATUM = 22;
	static public final int BEARBEITET = 23; // mit externem Prog bearbeitet
	static public final int SEQUENCE = 24;
	static public final int SPIEGELN = 25;
	static public final int MINI_SEQUENCE = 26;
 
	private String id = "";
	private String datum = "";  
	 
	
	private Date dateImport = null;
	private Date dateCurrent = null;
	
	private String qs = "3";  
	private String rotate = ""; // "L" left, "R" right 
	private String index1 = "";
	private String index2 = "";
	private String bemerkungen = "";
	private String miniSequence = "";
	private String sequence = "";
	private boolean spiegeln = false;
	private String breite = "";
	private String hoehe = "";
	private String cutX = "";
	private String cutY = "";
	private String cutBreite = "";
	private String cutHoehe = "";
//	private Date date = null;  
	private boolean bearbeitet = false;  
	private boolean invalid = false;  
 
	private File fileOriginal;
	private boolean init = true;
	private static Vector<PM_Listener> alleChangeListener = new Vector<PM_Listener>();

	private PM_Picture picture;
 
	
	/**
	 * Create an instance.
	 * 
	 * 
	 */
	protected PM_PictureMetadaten(File fileOriginal, PM_Picture picture) {
		this.fileOriginal = fileOriginal;
		this.picture = picture;
		id = PM_Utils.fileOriginalToXmlId(fileOriginal);
		// set dateImport and dateCurrent to 'now'
		long millis = System.currentTimeMillis();
		dateImport = new Date(millis);
		dateCurrent = new Date(millis);
	}

	/**
	 * Set init mode.
	 * 
	 * @param init - true in init mode.
	 */
	public void setInit(boolean init) {
		this.init = init;
	}

	public boolean getInit() {
		return init;
	}

	// =====================================================
	// Class Methods: addChangeListener()/removeChangeListener()
	//
	// Update wenn sich die Metadaten geaendert haben
	// =====================================================
	public static void addChangeListener(PM_Listener listener) {
		if (!alleChangeListener.contains(listener))
			alleChangeListener.add(listener);
	}

	// =====================================================
	// getId()
	//  
	// =====================================================
	public String getId() {
		return id;
	}

	// =====================================================
	// setIndex() / getIndex()
	// =====================================================
	public void setIndex1(String ind) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		// Umwandlung in Kleinbuchstaben und doppelte Eintraege entfernen
		String index = ind.toLowerCase(); // toLowerCase ist hier in Ordnung
		index = PM_Utils.sortedSetToString(PM_Utils.getSortedSet(index));
		// uebernhemen, wenn geaendert
		if (hasUpdate(this.index1, index)) {
			// die Indizes haben sich geaendert
			this.index1 = getUpdate(this.index1, index);

			update(INDEX_1);
		}
	}

	public String getIndex1() {
		return index1;
	}

 
	// =====================================================
	// setOrt() / getOrt()
	// =====================================================
	public void setIndex2(String or) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		String ort = or.toLowerCase(); // toLowerCase ist hier in Ordnung
		ort = PM_Utils.sortedSetToString(PM_Utils.getSortedSet(ort));
		if (hasUpdate(this.index2, ort)) {
			this.index2 = getUpdate(this.index2, ort);
			update(INDEX_2);
		}
	}

	public String getIndex2() {
		return index2;
	}

	 

	// =====================================================
	// setSequenz() / getSequenz()
	// =====================================================
	public void setSequenz(String se) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		String sequenz = se.toLowerCase(); // toLowerCase ist hier in Ordnung
		sequenz = PM_Utils.sortedSetToString(PM_Utils.getSortedSet(sequenz));
		if (hasUpdate(this.sequence, sequenz)) {
			this.sequence = getUpdate(this.sequence, sequenz);
			update(SEQUENCE);
		}
	}

	public String getSequence() {
		return sequence;
	}	
	 
	public boolean hasExtendedSequence() {
		// true: "x"-Sequenz vorhanden
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			if (s.indexOf("s") >= 0) {
				return true;
			}
		}
		// nigefu
		return false;				
	}	
 
	
	public boolean hasBaseSequence() {
		// true: "b"-Sequenz vorhanden
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			if (s.indexOf("b") >= 0) {
				return true;
			}
		}
		// nigefu
		return false;				
	} 
	
	public String getBaseSequenceName() {
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			if (s.indexOf("b") >= 0) {
				String[] sss = sa[i].split(LUCENE_SEQUENZ_TRENNER);
				if (sss.length < 1) {
					return "";
				}
				return sss[0];
			}
		}
		// nigefu
	    return "";				
	} 
	
	
	public boolean hasClosedSequence() {
		// true: "s"-Sequenz vorhanden
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			if (s.indexOf("s") >= 0) {
				return true;
			}
		}
		// nigefu
		return false;				
	} 
	
	public boolean hasNewSequence() {
		// true: "n"-Sequenz vorhanden
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			if (s.indexOf("n") >= 0) {
				return true;
			}
		}
		// nigefu
		return false;
	}

	public boolean hasSequenz() {
		 
		return hasNewSequence() || hasClosedSequence() || hasBaseSequence();
		
	}

	
	
	
	// =====================================================
	// setBearbeitet() / getBearbeitet()
	//
	// Mit externem Bildbearbeitungsprogramm veraendert
	// =====================================================
	public boolean getModified() {
		return bearbeitet;
	}
	public void setModified(boolean bearbeitet) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (this.bearbeitet == bearbeitet) return; // kein update
		this.bearbeitet =  bearbeitet;
		update(BEARBEITET);
	}

	// =====================================================
	// setLoeschen() / getLoeschen()
	//
	// Loeschen XML-Eintrag in der pm_index.xml - Datei.
	//
	// Wenn loeschen gesetzt ist, dann die Metadaten als nicht vorhanden
	// betrachten. Es werden dann auch keine updates mehr durchgefuehrt
	//
	// Das l�schen der Dateien (Original, Thumbnails, bearbeitete Bilder ...)
	// erfolgt
	// in: PM_PictureDirectory.updateMetadaten() !!!
	// Es wird aufgerufen, wenn das ge�nderte XML-Index-File mit flush auf die
	// Platte
	// geschrieben wird.
	// =====================================================
	/**
	 * Set this picture invalid.
	 * 
	 */
	public void setInvalid(boolean invalid) {
		if (this.invalid == invalid) {
			return;
		}
		this.invalid = invalid;
		// nicht update aufrufen, da das nicht mit loeschen funktioniert
		fireChangeListener(LOESCHEN_INDEX_FILE);
	}

	public boolean isInvalid() {
		return invalid;
	}
 
	 
	/**
	 * Sets the import and current date.
	 * 
	 * Generally the import date is the pictures EXIF date.
	 * I set both (dateImport and dateCurrent) to the same value
	 * because the import date is selecting only once (at the
	 * beginning)
	 */
	
	public void setDateImport(Date date ) {
		dateImport = new Date(date.getTime());
		update(DATUM);
	}
	
	
	/**
	 * Sets the current date.
	 *  
	 *  The current date is an update date.
	 *  I never change the import date.
	 */
	public void setDateCurrent(Date date ) {
		dateCurrent = new Date(date.getTime());
		update(DATUM);
	}
	
	
	public boolean hasDateChanged() {
		return dateImport.getTime() != dateCurrent.getTime();		
	}
	
	
	public Date getDateImport() {
		return dateImport;
	}
	
	public Date getDateCurrent() {
		return dateCurrent;
	}

 
	
  
	// =====================================================
	// setBemerkungen() / getBemerkungen()
	// =====================================================
	public void setRemarks(String bemerkungen) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (hasUpdate(this.bemerkungen, bemerkungen)) {
			this.bemerkungen = getUpdate(this.bemerkungen, bemerkungen);
			update(BEMERKUNGEN);
		}
	}

	public String getRemarks() {
		return bemerkungen;
	}

	public void setMiniSequence(String miniSequence) {
		if (!requestToUpdate()) {
			return; // no update
		}
		if (hasUpdate(this.miniSequence, miniSequence)) {
			this.miniSequence = getUpdate(this.miniSequence, miniSequence);
			update(MINI_SEQUENCE);
		}
	}
	
	/**
	 * 
	 * For example: "m26_2_"
	 *  
	 */
	public String getMiniSequence() {
		return miniSequence;
	}
	
	/**
	 * Returns the sequence number.
	 * 
	 * That is the number succeed the 'm'. 
	 * For example: "m26_2_" --> return 26;
	 */
	public int getMiniSequenceNumber() {
		int n = -1;
		if (!hasMiniSequence()) {
			return n;
		}
		String[] ss = miniSequence.split("_"); 
		return PM_Utils.stringToInt(ss[0].substring(1));
	}
	
	/**
	 * Returns the current number of the mini sequence.
	 * 
	 * That is the second number. 
	 * For example: "m26_2_" --> return 2;
	 */
	public int getCurrentMiniNumber() {
		int n = -1;
		if (!hasMiniSequence()) {
			return n;
		}
		String[] ss = miniSequence.split("_");
		if (ss.length < 2) {
			return n;
		}
		return PM_Utils.stringToInt(ss[1]);
	}
	
	/**
	 * Check if the mini Sequence ends with X.
	 * 
	 * This picgture is visible if the mini sequence
	 * is compressed.
	 */
	public boolean isMiniSequenceX() {
		if (!hasMiniSequence()) {
			return false;
		}
		return miniSequence.endsWith("X");
	}
	
	public boolean hasMiniSequence() {
		return miniSequence.length() != 0;
	}
	
	 
	private void setHoehe(String hoehe) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (hasUpdate(this.hoehe, hoehe)) {
			this.hoehe = getUpdate(this.hoehe, hoehe);
			update(HOEHE);
		}
	}
 

	public long getResolution() {
		return PM_Utils.stringToLong(breite) * PM_Utils.stringToLong(hoehe);	
	}
	
	/**
	 * Returns with image size in pixel
	 *  
	 */
	public Dimension getImageSize() {
		
		return new Dimension(PM_Utils.stringToInt(breite), PM_Utils.stringToInt(hoehe));
	}
	
	/**
	 * Set the image size
	 *  
	 */
	public void setImageSize(Dimension size) {
		setBreite(String.valueOf(size.width));
		setHoehe(String.valueOf(size.height));	
	}
	
	
	 
	private void setBreite(String breite) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (hasUpdate(this.breite, breite)) {
			this.breite = getUpdate(this.breite, breite);
			update(BREITE);
		}
	}

	 

	/**
	 * Test and sets the category.
	 * 
	 * Test validity. If unvalid set the category to 3.
	 *  
	 */
	public void setCategory(String qs) {
		int q = PM_Utils.stringToInt(qs);
		if (q < 1 || q > 4) {
			qs = "3";			 
		}		
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (hasUpdate(this.qs, qs)) {
			this.qs = getUpdate(this.qs, qs);
			update(QS);
		}
	}

	/**
	 * Test and sets the category.
	 * 
	 * Test validity. If unvalid set the category to 3.
	 *  
	 */
	public void setCategory(int qs) { 
		setCategory(String.valueOf(qs));
	}
	
	 
	/**
	 * Returns the category.
	 * 
	 * @return the valid (1..4) category
	 *  
	 */
	public int getCategory() {
		if (qs.equals("1"))
			return QS_1;
		if (qs.equals("2"))
			return QS_2;
		if (qs.equals("3"))
			return QS_3;
		if (qs.equals("4"))
			return QS_4;

		return QS_3;
	}
 
	// =====================================================
	// getCutRectangle() /setCutRectangle()
	// =====================================================
	public Rectangle getCutRectangle() {
		if (cutBreite.length() == 0) {
			// kein cut-Rectangle vorhanden. Original ausgeben
			return new Rectangle(getImageSize());
//			return getRecOriginal();
		}
		// Cut-Rectangle vorhanden
		return new Rectangle(PM_Utils.stringToInt(cutX), PM_Utils
				.stringToInt(cutY), PM_Utils.stringToInt(cutBreite), PM_Utils
				.stringToInt(cutHoehe));
	}

	public void setCutRectangle(Rectangle rec) {
		if (!requestToUpdate()) {
			return; // Update nicht durchf�hren
		}
		if (rec.x == 0 && rec.y == 0 && rec.width == 0 && rec.height == 0) {
			cutX = "";
			cutY = "";
			cutBreite = "";
			cutHoehe = "";
		} else {
			cutX = Integer.toString(rec.x);
			cutY = Integer.toString(rec.y);
			cutBreite = Integer.toString(rec.width);
			cutHoehe = Integer.toString(rec.height);
		}

		update(CUT_RECTANGLE);
	}

	 
 
	// =====================================================
	// setGespiegelt()/getSpiegeln()
	// =====================================================
	public boolean getMirror() {
		return spiegeln;
	}
	public void setMirror(boolean spiegeln) {
		if (!requestToUpdate())
			return; // Update nicht durchf�hren
		if (this.spiegeln == spiegeln) return; // kein update
		this.spiegeln =  spiegeln;
		update(SPIEGELN);
	}
	
	 
	 


	
	// =====================================================
	// hasCutRectangle()
	// =====================================================
	public boolean hasCutRectangle() {
		 
		int b = PM_Utils.stringToInt(cutBreite);
		int h = PM_Utils.stringToInt(cutHoehe);

		if (b == 0 || h == 0)
			return false;
		return true;
 
	}

	 

	 
	
	/**
	 * Returns the rotation in degrees.
	 *  
	 * @return degrees clockwise 
	 */
	public int getRotation() {
		if (rotate.equals("L"))
			return 270;
		if (rotate.equals("R"))
			return 90;
		if (rotate.equals("U"))
			return 180;
		return 0;
	}
	
	
 
	
	public void setRotation(int rotation) {
		if (!requestToUpdate()) {
			return; // Update nicht durchf�hren, da gel�scht
		}
		String rotate = "";
		switch (rotation) {
		case CLOCKWISE_0_DEGREES:
			rotate = "";
			break;
		case CLOCKWISE_90_DEGREES:
			rotate = "R";
			break;
		case CLOCKWISE_180_DEGREES:
			rotate = "U";
			break;
		case CLOCKWISE_270_DEGREES:
			rotate = "L";
			break;
		}
		if (hasUpdate(this.rotate, rotate)) {
			this.rotate = getUpdate(this.rotate, rotate);
			update(ROTATE);
		}
	}

 
 
	
	public File getFileOriginal() {
		return fileOriginal;
	}

	 
	 
	
	private boolean hasUpdate(String old, String n) {
		if (n == null) {
			return false;
		}
		String neu = n.trim();
		if (old.equals(neu)) {
			return false;
		}
		return true;
	}
 
	
	
	
	private String getUpdate(String old, String n) {
		if (n == null)
			return old;
		String neu = n.trim();
		if (old.equals(neu))
			return old;
		return neu;
	}

	 
	private boolean requestToUpdate() {
		return !invalid;
	}

	 
	private void update(int type) {
		 
		if (init || invalid) {
			return;  
		}
		
		fireChangeListener(type);
		makeMpeg(type);
		changeLastModifiedThumbnail(type);
		return;
	}

	
	/**
	 *
	 */
	private void makeMpeg(int type) {
		if (!PM_Configuration.getInstance().isMpeg()) {
			return;
		}
		if (type == ROTATE || type == CUT_RECTANGLE || type == SPIEGELN) {

			// we must create a new mpeg2 file.
			// Delete the mpeg file.
			// I create a new file before backup.
			File fileMpeg = PM_Utils.getFileMPEG(fileOriginal);
			fileMpeg.delete();
		}
	}
	
	private void changeLastModifiedThumbnail(int type) {
		if (type == ROTATE || type == CUT_RECTANGLE || type == SPIEGELN) {

			File thumb = PM_Utils.getFileThumbnail(fileOriginal);
			if (thumb.isFile()) {
				Date current = new Date(System.currentTimeMillis());
				thumb.setLastModified(current.getTime());
			}

		}
	}
	
	 
	private void fireChangeListener(int type) {
		for (int i = 0; i < alleChangeListener.size(); i++) {
			PM_Listener listener = (PM_Listener) alleChangeListener
					.elementAt(i);
			listener.actionPerformed(new PM_Action(picture, type));
		}
	}

	 
	public String toString() {

		String retId = "ID = " + id;
		String retDatum = "datum  = " + datum;
		String retQs = "qs = " + qs;
		String retBreite = "breite = " + breite;
		String retHoehe = "hoehe = " + hoehe;
		String retIndex = "index = " + index1;
		String retCutX = "cutX = " + cutX;
		String retCutY = "cutY = " + cutY;
		String retCutHoehe = "cutHoehe = " + cutHoehe;
		String retCutBreite = "cutBreite = " + cutBreite;

		return "Metadaten = " + retId + "/" + retDatum + "/" + retQs + "/"
				+ retBreite + "/" + retHoehe + "/" + retIndex + "/" + retCutX
				+ "/" + retCutY + "/" + retCutHoehe + "/" + retCutBreite

		;
	}

}