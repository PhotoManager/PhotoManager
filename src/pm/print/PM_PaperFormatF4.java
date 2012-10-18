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

/** Papierformat füe 4 Bilder
 * 
 * (Kommentar siehe Basisklasse PM_PapierFormat)
 *
 */
public class PM_PaperFormatF4 extends PM_PaperFormat implements PM_Interface {

	// ================================================================
	// Konstruktor
	// ================================================================
	public PM_PaperFormatF4() {
		super();

	}

	// ================================================================
	// init()
	// (ein neuer Drucker wurde ausgewählt und ihm wurde
	//  dieses Papierformat zugeordnet)
	// ================================================================
	public void init(PM_PmPrinter drucker) {
		super.init(drucker);
		// vier  "leere" PM_PictureDrucken-Instanzen anlegen
		createAllePictureDrucken(4);
		// alle Papierbereiche in den PM_PictureDrucken-Instanzen anlegen
		setAllePapierBereiche();
		// alle Hilfslinien aufbereiten
		setAlleHilfslinien();
	}

	// ================================================================
	// setAlleHilfslinien()
	//
	// hier zwei Hilfslinien (eine senkrecht und eine horizontal)
	// ================================================================
	private void setAlleHilfslinien() {

		// Hilfslinien über "alles"
		Rectangle2D papierGesamt = drucker.getPapierBereichGesamt();
		

		Line2D line = new Line2D.Double(
				papierGesamt.getX(), 
				papierGesY + papierGesH / 2.0F, 
				papierGesamt.getWidth(), 
				papierGesY + papierGesH / 2.0F);
		hilfsLinien.add(line);
		line = new Line2D.Double(
				papierGesX + papierGesB / 2.0F, 
				papierGesamt.getY(), 
				papierGesX + papierGesB / 2.0F,
				papierGesamt.getHeight());  
		hilfsLinien.add(line);

	}

	// ================================================================
	// setAllePapierBereiche()
	//
	// Papierbereiche in den PM_PictureDruckern anlegen
	// ================================================================
	private void setAllePapierBereiche() {

		// 1. oben links
		Rectangle2D druckBereich = new Rectangle2D.Double(papierGesX, papierGesY, papierGesB / 2,
				papierGesH / 2);
		allePictureDrucken[0].setPapierBereich(druckBereich);

		// 2. oben rechts
		druckBereich = new Rectangle2D.Double(papierGesX + papierGesB / 2, papierGesY, papierGesB / 2,
				papierGesH / 2);
		allePictureDrucken[1].setPapierBereich(druckBereich);

		// 3. unten links
		druckBereich = new Rectangle2D.Double(papierGesX, papierGesY + papierGesH / 2, papierGesB / 2,
				papierGesH / 2);
		allePictureDrucken[2].setPapierBereich(druckBereich);

		// 4. unten rechts
		druckBereich = new Rectangle2D.Double(papierGesX + papierGesB / 2, papierGesY + papierGesH / 2,
				papierGesB / 2, papierGesH / 2);
		allePictureDrucken[3].setPapierBereich(druckBereich);
	}

}