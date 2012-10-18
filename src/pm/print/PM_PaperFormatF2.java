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

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

 
import pm.utilities.PM_Interface;

/** Papierformat füe 2 Bilder
 * 
 * (Kommentar siehe Basisklasse PM_PapierFormat)
 *
 */
public class PM_PaperFormatF2 extends PM_PaperFormat implements PM_Interface {

	// ================================================================
	// Konstruktor
	// ================================================================
	public PM_PaperFormatF2() {
		super();
	}

	// ================================================================
	// init()
	// (ein neuer Drucker wurde ausgewählt und ihm wurde
	//  dieses Papierformat zugeordnet)
	// ================================================================
	public void init(PM_PmPrinter drucker) {
		super.init(drucker);
		// zwei  "leere" PM_PictureDrucken-Instanzen anlegen
		createAllePictureDrucken(2);
		// alle Papierbereiche in den PM_PictureDrucken-Instanzen anlegen
		setAllePapierBereiche();
		// alle Hilfslinien aufbereiten
		setAlleHilfslinien();
	}

	// ================================================================
	// setAlleHilfslinien()
	//
	// hier eine quer in der Mitte
	// ================================================================
	private void setAlleHilfslinien() {

		// Hilfslinien über "alles"
		Rectangle2D papierGesamt = drucker.getPapierBereichGesamt();
		
		Line2D line = new Line2D.Double(
				papierGesamt.getX(), 
				papierGesY + papierGesH / 2, 
				papierGesamt.getWidth(),  
				papierGesY + papierGesH / 2);
		hilfsLinien.add(line);
	}

	// ================================================================
	// setAllePapierBereiche()
	//
	// Papierbereiche in den PM_PictureDruckern anlegen
	// ================================================================
	private void setAllePapierBereiche() {

		// 1. oberen
		Rectangle2D druckBereich = new Rectangle2D.Double(papierGesX, papierGesY, papierGesB, papierGesH / 2);
		allePictureDrucken[0].setPapierBereich(druckBereich);

		// 2. unten
		druckBereich = new Rectangle2D.Double(papierGesX, papierGesY + papierGesH / 2, papierGesB,
				papierGesH / 2);
		allePictureDrucken[1].setPapierBereich(druckBereich);
	}
}