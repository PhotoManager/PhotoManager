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

 
import pm.utilities.*;

/** Papierformat füe 1 Bild
 * 
 * (Kommentar siehe Basisklasse PM_PapierFormat)
 *
 */
public class PM_PaperFormatF1 extends PM_PaperFormat implements PM_Interface {

	// ================================================================
	// Konstruktor
	// ================================================================
	public PM_PaperFormatF1() {
		super();
		 
	}
	
	// ================================================================
	// init()
	// (ein neuer Drucker wurde ausgewählt und ihm wurde
	//  dieses Papierformat zugeordnet)
	//
	// Keine Hilfslinen anlegen, da hier nur ein Bild
	// ================================================================
	public void  init(PM_PmPrinter drucker) {
		super.init(drucker);
		// eine  "leere" PM_PictureDrucken-Instanzen anlegen
		createAllePictureDrucken(1);
		// alle Papierbereiche in den PM_PictureDrucken-Instanzen anlegen
		setAllePapierBereiche();		
		// alle Hilfslinien aufbereiten
		setAlleHilfslinien( );
	}	
	 
	// ================================================================
	// setAlleHilfslinien()
	//
	// hier keine Hilfslinie, da nur ein Bild
	// ================================================================
	private void  setAlleHilfslinien( ) {	
		// keine
	}
	 
	// ================================================================
	// setAllePapierBereiche()
	//
	// Papierbereiche in den PM_PictureDruckern anlegen
	// (hier nur ein Bild und somit ist der Papierbereich des
	//  Bildes gleich dem Druckbereich des Druckers)
	// ================================================================
	private void  setAllePapierBereiche( ) {	
		allePictureDrucken[0].setPapierBereich(papierBereichGesamt);
	}
	

}
