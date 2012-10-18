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

 
import pm.gui.*;
import pm.utilities.PM_Interface;
import pm.utilities.PM_UtilsGrafik;

//import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/** Abstrakte Basisklasse aller Bildformate
 * 
 *  Werden Bilder zum Drucken ausgewählt, so steht das "Papier"
 *  (das PM_PapierFormat F1, F2 ...) fest. Alle Bilder, die zum Drucken
 *  ausgewählt wurden, haben also ein festes Papierformat.
 *  
 *  Jedes zu druckende Bild hat ein BildFormat. 
 *  
 *   (1) Wird ein Bild zum Drucken "hinzugefügt", dann wird die zugehörige
 *   PM_Bildformat-Instanz "nur" zum ermitteln vom 
 *   BildBereich und vom CutRectangle (des Images) benötigt. Beide Rectangles
 *   müssen IMMER das gleiche Seitenverhältnis (ggf. gedreht) haben.
 *   (Methode: "setCutRecUndBildbereich(PM_PictureDrucken)"). Die Daten werdne
 *   in die PM_PictureDrucken-Instanz eingetragen, so dass die PM_BildFormat-Instanz
 *   keine "Bild"-Daten mehr hält (wieder "frei" ist).
 *   
 *   (2) Wird das Bild verändert (PM_DialogDruckenAendern), so wird die 
 *   PM_BildFormat-Instanz zum Ausschneiden benutzt. Dazu wird 
 *   init() aufgerufen und die Rectangles "Papierbereich", "Druckbereich" und
 *   "Image-CutRectangle" übergeben. Druckbereich und cutRectangle werden
 *   ggf. verändert und bei Beendigung in die zugehörige PM_PictureDrucken-Instanz
 *   übernommmen. Danach ist die PM_BildFormat-Instanz wieder "frei"
 * 
 * 
 * 
 *    boolean hasResized()
 *          fragt, ob das Rechteck gemoved und resized wird
 *    boolean resizeSeiten()
 *          fragt, ob auch die Anfasser an den Seiten zu zeichnen sind.
 *          Wird nur ausgewertet, wenn hasResized() mit true antwortet.
 *          Default auf true.
 *    Rectangle getRectangleResize()
 *          liefert das zu movende und resizende Rechteck
 *    Rectangle getRectangleMax()
 *          liefert das Rechteck in dem gemoved und resized wird
 *    double getRatio()
 *          Seitenverhaeltnis, dass beim Resizen einzuhalten ist
 *    void drawRectangleResize(Graphics)
 *          das zu resizende Rechteck wird gezeichnet.
 *          Wenn nicht überschrieben wird es vom PM_Ausschneiden
 *          gezeichnet.
 *          Hier hat die abgeleitete Klasse die Möglichkeit,
 *          z.B. das Recheck gestrichelt zu zeichnen falls das
 *          Seitenverhältnis sich geändert hat.
 *    void setRectangleResized(Rectangle)  
 *          wird aufgerufen, wenn sich das Rechteck geaendert hat
 *
 * 
 */
public class PM_PictureFormat implements PM_Interface {

	// ---------------  final ----------------------------	 
	final protected Rectangle bildGroesse; // Abmessungen Bild in mm 
	final protected String formatName; // "max Bild", "max Papier" , .....
	final protected PM_PaperFormat papierFormat; // F1, F2, ....

	// -------------------------------------------------
	// Temporäre Werte für den Änderungs-Dialog:
	// -------------------------------------------------
	// "imageCutRectangle" (aus  Picture-Metadaten) und
	// "druckBereich" (in das das Bild gedruckt wird)
	//    werden bei init()aus "pictureDrucken" entnommen, verändert und
	//    bei Ende Ausschneiden wieder in den "pictureDrucken" zurückgeschrieben
	
	protected PM_PmPrinter drucker = null;
	// ---- Drucker  (nicht Screen) -----------------
	protected Rectangle2D papierBereich = null; // fix
	protected double ratioPapierBereich = 1; // fix
	protected Rectangle2D druckBereich = null; // variabel (Ratio MUSS wie "imageCutRectangle" sein)
	protected double ratioDruckBereich = 1; // variabel (wie ratioImageCutRectangle, ggf. reziprok) 
	// ----   Image (Screen-Größen) ----------------------------------
	protected Rectangle2D imagePapierBereich = null; // variabel (zum Zeichnen auf dem Screen)
	protected Rectangle2D imageCutRectangle = null; // variabel (zum Zeichnen auf dem Screen)
	protected double ratioImageCutRectangle = 1; // variable (wie ratioDruckBereich, ggf. reziprok)
	// --- Screen Ausschneiden: scaling vom Image zum Screen und umgekehrt ---------
	protected AffineTransform scaling = new AffineTransform(); // Image --> Screen
	protected AffineTransform scalingInverse = new AffineTransform(); // Screen --> Image

	
	
	// =====================================================
	// Konstruktor 
	// =====================================================
	public PM_PictureFormat(PM_PaperFormat papierFormat, Rectangle bildGroesse, String formatName) {
		this.papierFormat = papierFormat; // F1, F2, ....
		this.bildGroesse = bildGroesse; // Abmessungen Bild in mm
		this.formatName = formatName; // "max Bild", "max Papier" , .....
	}

	// =====================================================
	// init()
	// 
	// Wird überschrieben.
	//
	// Diese Instanz wird zum Ausschneide-Dialog vorbereitet.
	// 
	// (entweder bei erstmaligen Ausschneide-Dialog-Aufruf oder
	//  wenn sich das BildFormat während des AusschneideDialoges
	//  ändert (über ComboBox))
	// =====================================================	
	public void init(PM_PicturePrint pictureDrucken) {
		init(pictureDrucken, pictureDrucken.getDruckBereich(), pictureDrucken.getCutRectangle());
	}

	public void init(PM_PicturePrint pictureDrucken, Rectangle2D dru, Rectangle2D cut) {
		papierBereich = pictureDrucken.getPapierBereich();
		drucker = pictureDrucken.getDrucker();
	}

	// =====================================================
	// drehenCutRectangle()
	//
	//  Der Button "Drehen" wurde betätigt
	// =====================================================
	public void drehenCutRectangle() {
		imageCutRectangle = PM_UtilsGrafik.rotateRoundMiddlePoint(imageCutRectangle);
		imagePapierBereich = PM_UtilsGrafik.rotateRoundMiddlePoint(imagePapierBereich);
		
		
 		druckBereich = PM_UtilsGrafik.rotateRoundMiddlePoint(druckBereich);
//		papierBereich = PM_UtilsGrafik.rotateRoundMiddlePoint(papierBereich);
	}

	// =====================================================
	// toPictureDrucken()
	//
	//  "druckBereich" und "cutRectangle" zurückschreiben
	// =====================================================
	public void toPictureDrucken(PM_PicturePrint pictureDrucken) {
		imageCutRectangle = scalingInverse.createTransformedShape(imageCutRectangle).getBounds2D();
		pictureDrucken.setCutRectangle(imageCutRectangle);
		
		pictureDrucken.setDruckBereich(druckBereich);
		
	}

	// =====================================================
	// getting/setting
	// =====================================================
	public PM_PaperFormat getPapierFormat() {
		return papierFormat;
	}

	// =====================================================
	// setScaling()
	//
	// Aufruf vom UI-Slider
	//
	// Wenn das Scaling im UI sich geänder hat.
	// =====================================================
	public void setScaling(double sliderValue) {
		scaling.setToScale(sliderValue, sliderValue);
		scalingInverse.setToScale(1 / sliderValue, 1 / sliderValue);
	}

	// =====================================================
	// anfasserPapier()
	//
	// Aufruf von der ChecBox im UI
	//
	// true:  Anfasser am PapierBereich
	// false: Anfasser am DruckBereich
	// =====================================================
	protected boolean anfasserPapier() {
		PM_WindowMain windowMain = PM_WindowMain.getInstance();
		PM_CutTempPrint da = windowMain.getDialogDruckenAendern();
		if (da == null) return true;
		return da.getAnfasserPapier().isSelected();
	}

	// =====================================================
	// getting/setting  Papier- und Cut-Bereich
	//
	// =====================================================	
	protected Rectangle2D getPapierBereich() {
		return papierBereich;
	}
/*
	public void setPapierBereich(Rectangle2D papierBereich) {
		Rectangle2D rec = new Rectangle2D.Double();
		rec.setRect(papierBereich);
		this.papierBereich = rec;
	}
*/
	public Rectangle2D getCutRectangle() {
		return scalingInverse.createTransformedShape(imageCutRectangle).getBounds2D();
	}

	public Rectangle2D getDruckBereich() {
		return druckBereich;
	}

	// =====================================================
	// toString() 
	// =====================================================
	public String toString() {
		return formatName;
	}

	// **************************************************************************
	//     WENN AUSGESCHNITTEN WIRD VOR DEM DRUCKEN
	// =====================================================
	// resizeSeiten()
	//
	// Ausschneiden vor dem Drucken (wird überschrieben)
	//
	// Fragt, ob auch die Seiten verändert werden dürfen.
	// Auswertung NUR wenn hasResized() true.
	//
	// Wenn hier true werden auch die Seitenanfasser gezeichnet.
	// =====================================================   
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

	// =====================================================
	// protected: getRectangleResize()
	//
	// Von der Vaterklasse wird das zu movende und zu 
	// resizende Rechteck geholt.
	// =====================================================   
//7	protected Rectangle2D getRectangleResize() {
//		return null; ////////////////////////////////SSSSSSSSSSSSSSSSSSSS

//	}

	// =====================================================
	// protected: drawCutRectangle()
	//
	// Muss in der abgeleiteten Klasse ueberschrieben werden.
	//
	// Zeichnet das CutRectangle
	// (wenn nicht überschrieben wird es hier gezeichnet)
	// =====================================================   	
//	protected void drawRectangleResize(Graphics g) {
//		Graphics2D g2d = (Graphics2D) g;

//		g2d.setColor(Color.YELLOW);
//		g2d.draw(getRectangleResize());
//	}

	// =====================================================
	// protected: setRectangleResized(Rectangle)
	//
	// Wird überschrieben
	//
	// Wird von der Vaterklasse aufgerufen, wenn sich das zu
	// movende und zu resizende Rechteck in der Position und/oder
	// Groesse geaendert hat.
	// =====================================================   
//	protected void setRectangleResized(Rectangle2D rectangle, AffineTransform at) {

//	}

	
	
	
	
	
	protected Rectangle2D getRectangleResize() {
		if (anfasserPapier()) {
			// Papier bekommt Anfasser
			return  imagePapierBereich ;
		}
		// cutRectangle bekommt Anfasser
		return  imageCutRectangle ;
	}

	protected void setRectangleResized(Rectangle2D rectangle, AffineTransform at) {
		if (anfasserPapier()) {
			// Anfasser ist "imagePapierBereich"
			imagePapierBereich = rectangle;
			// Cut dito
			imageCutRectangle = at.createTransformedShape(imageCutRectangle).getBounds2D();
		} else {
			// Anfasser ist "imageCutRectangle"
			imageCutRectangle =  rectangle;
			// Papier dito
			imagePapierBereich = at.createTransformedShape(imagePapierBereich).getBounds2D();
		}
	}

	protected void drawRectangleResize(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Color color = g2d.getColor();

		// PapierBereich
		Stroke strokeOld = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 3.0f, new float[] {
				50.0f, 10.0f }, 4.0f));
		g2d.draw(imagePapierBereich);
		g2d.setStroke(strokeOld);
		// CutRectangle
		g2d.setColor(Color.YELLOW);
		g2d.draw(imageCutRectangle);
		g2d.setColor(color);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// *************************************************************************************
	// *************************************************************************************

	// =====================================================
	// getRectangle() 
	//
	// return "min"-Rectangle eingepasst.
	//
	// Das "min"-Rectangle soll so in das "max"-Rectangle eingepasst
	// werden, dass möglichst kein "Platz"  verbleibt. 
	// Das Seitenverhältnis von "min" muss erhalten bleinen.
	//
	// max und min werden NICHT verändert.
	//
	// Es wird zwei mal versucht. Das größere wird genommen.
	// =====================================================
	protected Rectangle2D getRectangle(Rectangle2D max, Rectangle2D min) {

		Rectangle2D.Double testPapier = new Rectangle2D.Double(0, 0, min.getWidth(), min.getHeight());
		Rectangle2D r1 = fitIntoRectangle(max, testPapier);
		// anderes ratio
		testPapier.height = min.getWidth();
		testPapier.width = min.getHeight();
		Rectangle2D r2 = fitIntoRectangle(max, testPapier);
		// das größere von r1 une r2 wird das cutRectangle
		if (r1.getWidth() * r1.getHeight() >= r2.getWidth() * r2.getHeight()) {
			return r1;
		} else {
			return r2;
		}

	}

	// =====================================================
	// fitIntoRectangle() 
	//
	// Das "min"-Rectangle soll so in das "max"-Rectangle eingepasst
	// werden, dass möglichst kein "Platz"  verbleibt. Das Seitenver-
	// hältnis von "min" muss erhalten bleinen.
	//
	// max und min werden NICHT verändert.
	// =====================================================
	protected Rectangle2D fitIntoRectangle(Rectangle2D max, Rectangle2D min) {

		Rectangle2D rec = null;

		double scale = max.getWidth() / min.getWidth();
		rec = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(min).getBounds2D();
		if (max.getHeight() < rec.getHeight()) {
			// war nichts. Jetzt anders ...
			scale = max.getHeight() / min.getHeight();
			rec = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(min).getBounds2D();
		}

		Rectangle2D.Double r = new Rectangle2D.Double();
		r.setRect(rec);
		r.x = (double) ((max.getWidth() - r.getWidth()) / 2.0d + max.getX());
		r.y = (double) ((max.getHeight() - r.getHeight()) / 2.0d + max.getY());

		return r;
	}
}
