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

/** Papierformat füe 6 Bilder
 * 
 * (Kommentar siehe Basisklasse PM_PapierFormat)
 *
 */
public class PM_PaperFormatF6 extends PM_PaperFormat implements PM_Interface {

	// ================================================================
	// Konstruktor
	// ================================================================
	public PM_PaperFormatF6() {
		super();

	}

	// ================================================================
	// init()
	// (ein neuer Drucker wurde ausgewählt und ihm wurde
	//  dieses Papierformat zugeordnet)
	// ================================================================
	public void init(PM_PmPrinter drucker) {
		super.init(drucker);
		// sechs  "leere" PM_PictureDrucken-Instanzen anlegen
		createAllePictureDrucken(6);
		// alle Papierbereiche in den PM_PictureDrucken-Instanzen anlegen
		setAllePapierBereiche();
		// alle Hilfslinien aufbereiten
		setAlleHilfslinien();
	}

	// ================================================================
	// setAlleHilfslinien()
	//
	// hier vier Linien
	// ================================================================
	private void setAlleHilfslinien() {

		// Hilfslinien über "alles"
		Rectangle2D papierGesamt = drucker.getPapierBereichGesamt();
		
		// 1. quer
		Line2D line = new Line2D.Double(
				papierGesamt.getX(), 
				papierGesY + papierGesH / 3, 
				papierGesamt.getWidth(),  
				papierGesY + papierGesH / 3);
		hilfsLinien.add(line);
		// 2. quer
		line = new Line2D.Double(
				papierGesamt.getX(), 
				papierGesY + papierGesH * 2 / 3, 
				papierGesamt.getWidth(),  
				papierGesY + papierGesH * 2 / 3);
		hilfsLinien.add(line);
		// senkrechte
		line = new Line2D.Double(
				papierGesX + papierGesB / 2, 
				papierGesamt.getY(), 
				papierGesX + papierGesB / 2,
				papierGesamt.getHeight());  
		hilfsLinien.add(line);

	}

	// ================================================================
	// setAllePapierBereiche()
	//
	// Papierbereiche in den PM_PictureDruckern anlegen
	// ================================================================
	private void setAllePapierBereiche() {

		double b = papierGesB / 2;
		double h = papierGesH / 3;

		// 1. oben links
		Rectangle2D druckBereich = new Rectangle2D.Double(papierGesX, papierGesY, b, h);
		allePictureDrucken[0].setPapierBereich(druckBereich);

		// 2. oben rechts
		druckBereich = new Rectangle2D.Double(papierGesX + b, papierGesY, b, h);
		allePictureDrucken[1].setPapierBereich(druckBereich);

		// 3. Mitte rechts
		druckBereich = new Rectangle2D.Double(papierGesX, papierGesY + h, b, h);
		allePictureDrucken[2].setPapierBereich(druckBereich);

		// 4. Mitte links
		druckBereich = new Rectangle2D.Double(papierGesX + b, papierGesY + h, b, h);
		allePictureDrucken[3].setPapierBereich(druckBereich);

		// 5. unten rechts
		druckBereich = new Rectangle2D.Double(papierGesX, papierGesY + h + h, b, h);
		allePictureDrucken[4].setPapierBereich(druckBereich);

		// 6. unten links
		druckBereich = new Rectangle2D.Double(papierGesX + b, papierGesY + h + h, b, h);
		allePictureDrucken[5].setPapierBereich(druckBereich);
	}

}