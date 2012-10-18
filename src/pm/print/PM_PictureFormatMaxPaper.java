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

 
import pm.utilities.PM_UtilsGrafik;

public class PM_PictureFormatMaxPaper extends PM_PictureFormat {

	// =====================================================
	// Konstruktor 
	// =====================================================
	public PM_PictureFormatMaxPaper(PM_PaperFormat papierFormat, Rectangle bildGroesse, String formatName) {
		super(papierFormat, bildGroesse, formatName);
	}

	// =====================================================
	// init() 
	//
	// Aufruf wenn Ausschneiden aufgerufen wird oder wenn das Bild-Format
	// sich geändert hat (ComboBox).
	//
	// (druckBereich und cutRectangle in "pictureDrucken" NICHT verändern)
	// =====================================================
	public void init(PM_PicturePrint pictureDrucken, Rectangle2D dru, Rectangle2D cut) {

		super.init(pictureDrucken, dru, cut);

		// Es wird nur das gedruckt, was im cutRectangle ist, d.h. nur
		// das was ausgeschnitten ist.
		// Es wird also weniger oder höchstens der der gesamte cutRectangle
		// gedruckt.
		// 
		//  (1) der Bildbereich gleich Papierbereich.
		//      (eine Kopie erstellen, da später evtl der Bildbereich geändert wird)
		druckBereich = new Rectangle2D.Double();
		druckBereich.setRect(pictureDrucken.getPapierBereich()); // Kopie erstellen
		// 
		//  (2) cutRectangle neu ermitteln.
		//      2 x fit aufrufen (den Papierbereich in das cutRectangle einpassen)
		//      und dann das größere als cutRectangle  nehmen

		// "Max-Papier": das ursprüngliche CutRectangle (aus PictureMetadaten) wird 
		//  ggf. verkleinert, damit das Papier zum Drucken maximal ausgenutzt wird.
		//  Dabei geht u.U. Information aus dem CutRectangle verloren.
		//  (das Seitenverhältnis vom papierBereich muss erhalten bleiben, das
		//   CutRectangle wird daher u.U. kleiner)
		//
		// (papierBereich muss vollständig ins cut passen, Ratio vom papierBereich muss erhalten bleiben) 
		imageCutRectangle = getRectangle(cut, papierBereich); // (max, min)
		// imagePapierBereich wird um einige Pixel grösser als imageCutRectangle
		int grow = 4;
		Rectangle2D rec = new Rectangle2D.Double();
		rec.setRect(imageCutRectangle); // Papier-Bereich und Cut-Bereich sind identisch
		double ratio = rec.getWidth() / rec.getHeight();
		imagePapierBereich = PM_UtilsGrafik.grow(rec, new Point2D.Double(grow, grow / ratio));
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
