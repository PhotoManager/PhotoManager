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

import java.awt.*;
 
import java.awt.geom.*;
 
import pm.utilities.*;

public class PM_PictureFormatMaxPicture extends PM_PictureFormat {

	// =====================================================
	// Konstruktor 
	// =====================================================
	public PM_PictureFormatMaxPicture(PM_PaperFormat papierFormat, Rectangle bildGroesse, String formatName) {
		super(papierFormat, bildGroesse, formatName);
	}

	// =====================================================
	// init() 
	//
	// (druckBereich und cutRectangle in "pictureDrucken" NICHT verändern)
	// =====================================================
	public void init(PM_PicturePrint pictureDrucken, Rectangle2D dru, Rectangle2D cut) {

		super.init(pictureDrucken, dru, cut);

		// Das cutRectangle muss vollständig gedruckt werden.
		// Der Bildbereich muss also gleich dem cutRectangle werden
		//
		// (1) wie max Papier (2 mal testen)
		//      2 x fit aufrufen (das cutRectangle in den Papierbereich einpassen)
		//      und dann als Druckbereich das größere Ergebnis nehmen	
		//
		// "Max Bild": Das CutRectangle aus den PictureMetadaten muss vollständig
		//    in den Papierbereich passen. D.h. alles was ausgeschnitten wurd muss
		//    auch gedruckt werden.
		//
		// (cut muss vollständig in den papierBereich passen. Ratio von cut muss erhalten bleiben) 
		druckBereich = getRectangle(papierBereich, cut); // (max, min) 
		// (2) Das cutRectangle bleibt unverändert
		imageCutRectangle = new Rectangle2D.Double();
		imageCutRectangle.setRect(cut); // Kopie erstellen

		// imagePapierBereich erstellen
		Rectangle2D papier = new Rectangle2D.Double();
		papier.setRect(papierBereich);
		double ratioPapierBereich = papier.getWidth() / papier.getHeight();
		double ratioCutRectangle = imageCutRectangle.getWidth() / imageCutRectangle.getHeight();
		// neuer "imagePapierBereich"
		double x = imageCutRectangle.getX();
		double y = imageCutRectangle.getY();
		double w = imageCutRectangle.getWidth();
		double h = imageCutRectangle.getHeight();
		if (!PM_UtilsGrafik.istSelbeDarstellung(ratioPapierBereich, ratioCutRectangle)) {
			ratioPapierBereich = 1 / ratioPapierBereich;
		}
		if (w / ratioPapierBereich > h) {
			// w ist O.K.
			h = w / ratioPapierBereich;
			y = y - (h - imageCutRectangle.getHeight()) / 2;
		} else {
			// h ist O.K. 			 
			w = h * ratioPapierBereich;
			x = x - (w - imageCutRectangle.getWidth()) / 2;
		}
		imagePapierBereich = new Rectangle2D.Double(x, y, w, h);

		// Jetzt alles ins "Screen-Format" scalen
		imageCutRectangle = scaling.createTransformedShape(imageCutRectangle).getBounds2D();
		imagePapierBereich = scaling.createTransformedShape(imagePapierBereich).getBounds2D();
	}

	// ====================================================================================
	//
	// Methods für PM_Ausschneiden
	//
	// =====================================================================================	
	protected boolean move() {
		return true;
	}

	protected boolean resizeDiagonal() {
		return true;
	}

	protected boolean resizeDiagonalSymmetrich() {
		return false;
	}

	protected boolean resizeVertiHorizontal() {
		return false;
	}

	protected boolean resizeVertiHoriSymmetrich() {
		return false;
	}

	 

}
