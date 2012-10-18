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

 
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import pm.utilities.PM_UtilsGrafik;

public class PM_PictureFormatFix extends PM_PictureFormat {

	// =====================================================
	// Konstruktor 
	// =====================================================
	public PM_PictureFormatFix(PM_PaperFormat papierFormat, Rectangle bildGroesse, String formatName) {
		super(papierFormat, bildGroesse, formatName);
	}

	// =====================================================
	// init() 
	//
	// (druckBereich und cutRectangle in "pictureDrucken" NICHT verändern)
	// =====================================================
	public void init(PM_PicturePrint pictureDrucken, Rectangle2D dru, Rectangle2D cut) {

		super.init(pictureDrucken, dru, cut);

		// BILD_FORMAT_FIX:
		// Bildbereich wird das hier hinterlegte mm-Format werden
		//    (einpassen falls es nicht in den Papierbereich passt)
		// ratio vom cutRec muss ratio vom Bildbereich werden
		double x = papierBereich.getX();
		double y = papierBereich.getY();
		double w = bildGroesse.getWidth() / drucker.getPixelBreiteMillimeter();
		double h = bildGroesse.getHeight() / drucker.getPixelHoeheMillimeter();
		Rectangle2D.Double druBer = new Rectangle2D.Double(x, y, w, h);

		// Nun muss es in den Papierbereich eingepasst werden
		if (papierBereich.contains(druBer)) {
			// zentrieren
			druBer.x = druBer.x + (papierBereich.getWidth() - druBer.getWidth()) / 2;
			druBer.y = druBer.y + (papierBereich.getHeight() - druBer.getHeight()) / 2;
			druckBereich = druBer;

		} else {
			// einpassen
			druckBereich = fitIntoRectangle(papierBereich, druBer);
		}

		// Jetzt das CutRectangle in den DruckBereich einpassen	
		imageCutRectangle = getRectangle(cut, druckBereich);

		// imagePapierBereich ermitteln
		// imagePapierBereich erstellen
		// #############  kopiert aus max Bild !!!!!!!!!!!!!!!!!!
		Rectangle2D papier = new Rectangle2D.Double();
		papier.setRect(papierBereich);
		double ratioPapierBereich = papier.getWidth() / papier.getHeight();
		double ratioCutRectangle = imageCutRectangle.getWidth() / imageCutRectangle.getHeight();
		// neuer "imagePapierBereich"
		double faktorW = papierBereich.getWidth() / druckBereich.getWidth();
//		double faktorH = papierBereich.getHeight() / druckBereich.getHeight();

		x = imageCutRectangle.getX();
		y = imageCutRectangle.getY();
		w = imageCutRectangle.getWidth();
		h = imageCutRectangle.getHeight();
		if (!PM_UtilsGrafik.istSelbeDarstellung(ratioPapierBereich, ratioCutRectangle)) {
			ratioPapierBereich = 1 / ratioPapierBereich;
		}
		if (w / ratioPapierBereich > h) {
			// w ist O.K.
			h = w / ratioPapierBereich;
			w = w*faktorW;
			h = h*faktorW;
			
			x = x - (w - imageCutRectangle.getWidth()) / 2;
			y = y - (h - imageCutRectangle.getHeight()) / 2;
		} else {
			// h ist O.K. 			 
			w = h * ratioPapierBereich;
			w = w*faktorW;
			h = h*faktorW;			
			
			
			x = x - (w - imageCutRectangle.getWidth()) / 2;
			y = y - (h - imageCutRectangle.getHeight()) / 2;
		}
		imagePapierBereich = new Rectangle2D.Double(x, y, w, h);
		// ###########  ende kopieren ###################

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
