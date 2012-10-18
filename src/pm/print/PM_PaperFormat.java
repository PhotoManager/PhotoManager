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

 
import java.awt.geom.*;
 
import java.util.*;
 

 
import pm.utilities.*;

/** Abstrakte Basisklasse aller Papierformate (F1, F2, ....)
 * 
 *  Von allen abgeleiteten Klassen wird je EINE Klasse (final und sigleton)
 *  instantiiert. Sie wird nie wieder erneut intstantiiert und auch nicht
 *  überschrieben (sie ist eben final)
 *  
 *  Wird ein anderes Papierformat ausgewählt (über ComboBox "Papierformat"
 *  oder ein anderer Systemdrucker), so wird hier (beim ausgewählten
 *  Papieformat) init augerufen. Damit werden Hilflinien und Papierformate
 *  NEU ermittelt.
 *
 */
public class PM_PaperFormat implements PM_Interface {

	protected List<Line2D> hilfsLinien = new ArrayList<Line2D>(); 
	protected PM_PicturePrint[] allePictureDrucken = null;
	protected PM_PmPrinter drucker = null;
	
	// Papierbereich Gesamt (for convenience)
	protected Rectangle2D papierBereichGesamt = null;
	protected double papierGesX = 0;
	protected double papierGesY = 0;
	protected double papierGesB = 0;
	protected double papierGesH = 0;
	
	// ================================================================
	// static:  getPapierFormat()
	// ================================================================
	private static final PM_PaperFormatF1 papierFormatF1 = new PM_PaperFormatF1();
	private static final PM_PaperFormatF2 papierFormatF2 = new PM_PaperFormatF2();
	private static final PM_PaperFormatF4 papierFormatF4 = new PM_PaperFormatF4();
	private static final PM_PaperFormatF6 papierFormatF6 = new PM_PaperFormatF6();

	public static PM_PaperFormat getPaperFormat(String formatName) {
		if (formatName.equals(PAPER_FORMAT_F1)) return papierFormatF1;
		if (formatName.equals(PAPER_FORMAT_F2)) return papierFormatF2;
		if (formatName.equals(PAPER_FORMAT_F4)) return papierFormatF4;
		if (formatName.equals(PAPER_FORMAT_F6)) return papierFormatF6;

		return null;
	}

	// ================================================================
	// Konstruktor
	// ================================================================
	public PM_PaperFormat() {}
	
	// ================================================================
	// init()
	// (ein neuer Drucker wurde ausgewählt und ihm wurde
	//  dieses Papierformat zugeordnet)
	// 
	// Wird von den abgeleiteten Klasse mit super.init(drucker) aufgerufen !!
	// ================================================================
	public void  init(PM_PmPrinter drucker) {
		this.drucker = drucker;
	    papierBereichGesamt = drucker.getDruckBereichGesamt();
		papierGesX = papierBereichGesamt.getX();
		papierGesY = papierBereichGesamt.getY();
		papierGesB = papierBereichGesamt.getWidth();
		papierGesH = papierBereichGesamt.getHeight();
	}	
	
	// ================================================================
	// getAbmessungPapierBereich()
	//  
	//  Für ein Bild wird hier der Papierbereich in mm returnt
    //  (Umrechnung auf Druckerauflösung)
	// ================================================================
	public String  getAbmessungPapierBereich( ) {
		if (allePictureDrucken == null || drucker == null) return "unbekannt";		 				
		Rectangle2D bereich = allePictureDrucken[0].getPapierBereich();
		return getAbmessungen(bereich);
	}	

	// ================================================================
	// getAbmessungDruckBereich()
	//  
	//  Für ein Bild wird hier der Druck-(Bild-)bereich in mm returnt
     //  (Umrechnung auf Druckerauflösung)
	// ================================================================
	public String  getAbmessungDruckBereich( ) {
		if (allePictureDrucken == null || drucker == null) return "unbekannt";		 				
		Rectangle2D bereich = allePictureDrucken[0].getDruckBereich();
		return getAbmessungen(bereich);
	}	

	// ================================================================
	// getAbmessungen()
	//  
	//  (Umrechnung auf Druckerauflösung)
	// ================================================================
	private String  getAbmessungen(Rectangle2D rec ) {

		Point2D auf = drucker.getAufloesung();
		int breite = (int) (Math.round(  (rec.getWidth()/auf.getX() *25.4F)));
		int hoehe = (int)(Math.round(  (rec.getHeight()/auf.getY() *25.4F)));
		
		return  Integer.toString(breite) + "x" + Integer.toString(hoehe);
		
		
		 
	}
	
	
	// ================================================================
	// createAllePictureDrucken()
	// (ein neuer Drucker wurde ausgewählt 
	//  Hier werden jetzt alle "leeren" PM_PictureDrucken-Instanzen angelegt)
	// ================================================================
	protected void  createAllePictureDrucken(int anzahl) {
		allePictureDrucken = new PM_PicturePrint[anzahl];
		for (int i=0; i<anzahl; i++)  {
			allePictureDrucken[i] = new PM_PicturePrint(drucker, i);		 
		}
		hilfsLinien = new ArrayList<Line2D>();
	}	
	
	// ================================================================
	// getAlleBilder()
	// ================================================================
	public PM_PicturePrint[]  getAllePictureDrucken() {
		return allePictureDrucken;
	}	

	// ================================================================
	// getHilfsLinien()
	// ================================================================
	public List  getHilfsLinien() {
		return hilfsLinien;
	}
	
}
