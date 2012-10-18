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
package pm.print;

 
import pm.picture.*;
import pm.utilities.*;

import java.awt.*;
import java.awt.geom.*;
import java.text.*;
 
import java.util.List;
import java.util.*;
 


/**
 * Klasse zur Verwaltung eines 'Papier'-Bildes
 * 
 * Je nach Papierformat (F1,F2,F4,F6) werden 1,2,4,oder 6 Instanzen
 * von dieser Klasse erzeugt. 
 * Wird das Papierformat geändert, werden die zuvor
 * erzeugten Instanzen vernichtet und es werden für das neue Papierformat 
 * neue Instanzen (1,2,4 oder 6) erzeugt.
 *  
 *  Jeder Instanz hat folgende Instanzvariablen:
 *    PM_Picture picture (das zu druckende Bid)
 *    PM_PmDrucker drucker (damit liegt auch das Papierformat F1,F2, ... fest) 
 *    PM_BildFormat bildFormat (genaue Größe des Bildes auf dem Papier)
 *    
 *  (der PM-Drucker kennt den aktuellen System-Drucker==physikalischer Drucke.
 *   Er braucht daher hier nicht gesondert referenziert zu werden)
 *    
 */
public class PM_PicturePrint implements PM_Interface {

	// Keine Änderungen nach init() (d.h. ein Bild wurde hinzugefügt)
	private PM_Picture picture;
	private PM_PmPrinter drucker;
	private PM_PictureFormat bildFormat;
	
	private int bildNummer;
	private boolean empty = true;
	private Image imageOriginal = null;
	private Rectangle2D papierBereich = null;

	// ggf. Änderungen nach init():  im Änderungs-Dialog 	
	private Rectangle2D druckBereich = null;
	private Rectangle2D cutRectangle = null;

	private String beschriftung = "";
	
	
	// =====================================================
	// Konstruktor
	// 
	// Es wird ein "leeres" Bild angelegt.
	// =====================================================
	public PM_PicturePrint(PM_PmPrinter drucker, int bildNummer) {
		this.drucker = drucker;
		this.bildNummer = bildNummer;
	}

	
	// ================================================================
	// init()
	//
	// Jetzt ein Bild zum Drucken aufnehmen
	// Return:  Bildnummer
	// ================================================================
	public int init(PM_Picture picture, PM_PictureFormat bildFormat, String beschriftung) {
		empty = false;			
		this.picture = picture;
		this.bildFormat = bildFormat;	
		setBeschriftung(beschriftung);
		// Image Original lesen:		 
		// Liste ALLER Images zusammenstellen (die müssen nämlich IMMER "gelesen" sein) 
		lesenImages();  // ALLE Images lesen die zum Drucken ausgewählt wurden
		imageOriginal = picture.getImageOriginal();
		// das nicht gedrehte cutRectangle holen
		cutRectangle = (Rectangle2D)picture.meta.getCutRectangle();  		
		// jetzt "druckBereich" und "cutRectangle" anpassen
		bildFormat.init(this);  // anpassen ...
		bildFormat.toPictureDrucken(this);  // ... und wieder übernehmen
		
		return bildNummer;
	}
  
	// ================================================================
	// set/getBeschriftung()
	// ================================================================
	public String getBeschriftung() {
		return beschriftung;
	}	
	public void setBeschriftung(String beschriftungsItem) {
		if (beschriftungsItem.equalsIgnoreCase("datum")) {
			  Date date = picture.meta.getDateCurrent();
			   DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");    
			   beschriftung = dateFormat.format(date) ;
			return;
		} 
		beschriftung = "";
	}
	
	// ================================================================
	// getDrucker()
	// ================================================================
	public PM_PmPrinter getDrucker() {
		return drucker;
	}
	// ================================================================
	// delete()
	//
	// das Bild wird freigegeben;
	// Bildnummer darf NICHT gelöscht werden!
	// ================================================================
	public void delete() {
		empty = true;
		picture = null;
		// original wiede freigeben, d.h.die noch benötigten Images "reservieren"
		lesenImages();
	}

	// ================================================================
	// isEmpty()
	// ================================================================
	public boolean isEmpty() {
		return empty;
	}

	// ================================================================
	// getPicture()
	// ================================================================
	public PM_Picture getPicture() {
		return picture;
	}

	// ================================================================
	// getBildNummer()
	// ================================================================
	public int getBildNummer() {
		return bildNummer;
	}

	// ================================================================
	// getImage()
	// ================================================================
	public Image getImage() {
		return imageOriginal;
	}

	// ================================================================
	// getBildFormat()
	// ================================================================
	public PM_PictureFormat  getBildFormat() {
		return bildFormat;
	}
	
	
	// ================================================================
	// set/get
	// ================================================================
	public Rectangle2D getCutRectangle() {
		return cutRectangle;
	}
	public void setCutRectangle(Rectangle2D cutRectangle) {
		this.cutRectangle = cutRectangle;
	}
	public void setPapierBereich(Rectangle2D papierBereich) {
		this.papierBereich = papierBereich;
	}
	public Rectangle2D getPapierBereich() {
		return papierBereich;
	}
	public void setDruckBereich(Rectangle2D druckBereich) {
		this.druckBereich = druckBereich;
	}
	public Rectangle2D getDruckBereich() {
		return druckBereich;
	}

	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================
	//	 ============================= PRIVATE ===========================




	// ================================================================
	// lesenImages()
	//
	// Alle benötigten Images lesen
	// ================================================================	
	private void lesenImages() {
		// Liste ALLER Images zusammenstellen (die müssen nämlich IMMER "gelesen" sein) 
		List<PM_Picture> sofort = new ArrayList<PM_Picture>();
		PM_PicturePrint[] alleBilder = drucker.getAlleBilder();
		for (int i = 0; i < alleBilder.length; i++) {
			PM_PicturePrint picDru = alleBilder[i];
			if (picDru.isEmpty() == false && sofort.contains(picDru.getPicture()) == false) {
				// keine doppelten
				sofort.add(picDru.getPicture());
			}
		}
		PM_Picture.readImageOriginal(sofort, new ArrayList<PM_Picture>());

	}
}
