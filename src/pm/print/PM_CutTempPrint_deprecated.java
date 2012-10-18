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

// 
 
import pm.gui.*;
import pm.picture.*;
import pm.utilities.PM_Interface;
import pm.utilities.PM_UtilsGrafik;
 
 

//import java.util.*;
//import java.io.*;
//import javax.swing.*;
//import javax.swing.event.*;
import java.awt.*;
 
import java.awt.geom.*;
 
 
 

import javax.swing.*;
 

/** Dialog zum verändern des Bildausschnittes vor dem Drucken 
 * 
 *  Die zu ändernden Daten (BildBereich und (Image-)CutRectangle) werden
 *  temporär in der zugehörigen PM_BildFormat-Instanz gehalten,
 *  geändert und  vor Dialog-Ende in die PM_PictureDrucken-Instanz übernommen
 *  (oder verworfen)
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_CutTempPrint_deprecated extends PM_Cut implements PM_Interface {


	
	protected PM_WindowMain windowMain;
	protected PM_PicturePrint pictureDruckdaten;
	protected PM_Picture picture;

	protected PM_PictureFormat bildFormat = null;

	// Werte zu ändern (Kopien aus "pictureDruckdaten")
	// Sie werden, falls die Änderungen fertig sind und zu
	// übernehmen sind, in "pictureDruckdaten" wieder eingetragen)
	// (Alle Werte sind immer in Originalgrösse)
	//
	// CutRectangle und DruckBereich müssen IMMER im ratio gleich sein !!!!!!!
	// Der Papierbereich darf NICHT verändert werden
	//	private Rectangle2D papierBereich;
	//	private Rectangle2D cutRectangle;
	//	private double ratioCut;

	private AffineTransform scaling = new AffineTransform();

	protected JPanel picturePanel = null;
    protected final JDialog dialog;
    
	// =====================================================
	// Konstruktor
	// =====================================================
	public PM_CutTempPrint_deprecated(PM_WindowMain windowMain, final JDialog dialog) {
		this.windowMain = windowMain;
		this.dialog = dialog;

		 
	}
	


	//======================================================
	//  setBildFormatNeu()
	//
	// Der Dialog zum Ändern wurde aufgerufen.
	//======================================================	
	protected void setBildFormatNeu(PM_PictureFormat bildFormat) {
		this.bildFormat = bildFormat;
		bildFormat.setScaling(sliderValue);
		bildFormat.init(pictureDruckdaten, pictureDruckdaten.getDruckBereich(), pictureDruckdaten
				.getCutRectangle());
	}



	//======================================================
	//  setText()
	//
	// Text zum Drucken
	//======================================================
	private String text = "";

	protected void setText(String text) {
		this.text = text;
		////////////////////////////  	pictureDruckdaten.setText(text);
		repaint();
	}

	//==========================================================
	// getPicturePanel
	//
	// In diesem Panel wird das picture dargestellt
	//==========================================================
	protected double sliderValue = 0.4F;

	protected JPanel getPicturePanel() {
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;

				// --------------------------------------------------------
				// Graphic-Context etwas verschieben, so dass das Bild
				// nicht links oben in die Ecke gezeichnet wird
				// ---------------------------------------------------------
				Point translate = new Point(20, 20);
				g2.translate(translate.x, translate.y); // alles verschieben

				// ------------------------------------------------------				
				// Das Bild zeichnen
				// ------------------------------------------------------
				Image image = pictureDruckdaten.getImage();
				scaling.setToScale(sliderValue, sliderValue);
				g2.drawImage(image, scaling, this);

				//	-----------------------------------------------------
				// Jetzt den Papierbereich zeichnen.
				// ------------------------------------------------------
/*
				Rectangle2D papierBer = bildFormat.getPapierBereich();
				if (papierBer != null) {
					Color saveColor = g2.getColor();
					g2.setColor(Color.GREEN);
					g2.draw(scaling.createTransformedShape(papierBer));
					g2.setColor(saveColor);
				} else {

				}
*/
				//-----------------------------------------------------
				// Jetzt das CutRectangle zeichnen   
				// ----------------------------------------------------
				g2.setColor(Color.YELLOW);
				Point correct = new Point(getX(), getY());
				correct.translate(translate.x, translate.y);
				//	wird in PM_Ausschneiden gezeichnet
				// (wird mit getRectangleResize() geholt. Dort muss
				// es gescaled werden!!)
				drawRectangleResize(g, correct);

				// ---------------------------------------------------
				// jetzt noch den Text ausgeben
				// ---------------------------------------------------
				if (text.length() != 0) {
					Rectangle recResize = PM_UtilsGrafik.rectangle2DToRectangle(getRectangleResize());
					g2.setFont(new Font("Arial", Font.BOLD, 18));
					g2.setColor(Color.YELLOW);
					g2.drawString(text, recResize.x + 20, recResize.y + recResize.height - 20);
				}
			} // paintComponent
		}; // new JPanel

		panel.setBackground(Color.LIGHT_GRAY);

		return panel;
	}

	// ================ Aufruf von der Vaterklasse (PM_Ausschneiden) ==================================
	// ================ Aufruf von der Vaterklasse (PM_Ausschneiden) ==================================
	// ================ Aufruf von der Vaterklasse (PM_Ausschneiden) ==================================
	// ================ Aufruf von der Vaterklasse (PM_Ausschneiden) ==================================
	// ================ Aufruf von der Vaterklasse (PM_Ausschneiden) ==================================

 

	// =====================================================
	// protected: resize()  ......
	//
	// =====================================================   
	protected boolean move() {
		return bildFormat.move();
	}

	protected boolean resizeDiagonal() {
		return bildFormat.resizeDiagonal();
	}

	protected boolean resizeDiagonalSymmetrich() {
		return bildFormat.resizeDiagonalSymmetrich();
	}

	protected boolean resizeVertiHorizontal() {
		return bildFormat.resizeVertiHorizontal();
	}

	protected boolean resizeVertiHoriSymmetrich() {
		return bildFormat.resizeVertiHoriSymmetrich();
	}

	// =====================================================
	// protected: setRectangleResized(Rectangle)
	//
	// Wird von der Vaterklasse aufgerufen, wenn sich das zu
	// movende und zu resizende Rechteck in der Position und/oder
	// Groesse geaendert hat.
	// =====================================================   
	protected void setRectangleResized(Rectangle2D rectangle, AffineTransform at) {
		bildFormat.setRectangleResized(rectangle, at); 
	}

	// =====================================================
	// protected: getRectangleResize()
	//
	// Von der Vaterklasse wird das zu movende und zu 
	// resizende Rechteck geholt.
	// =====================================================   
	protected Rectangle2D getRectangleResize() {
		return bildFormat.getRectangleResize();
 
	}

 

	// =====================================================
	// protected: getRectangleMax()
	//
	// Von der Vaterklasse wird das max. Rechteck, in dem das zu 
	// movende und zu resizende Rechteck sich befinden darf, geholt.
	// =====================================================   
	protected Rectangle2D getRectangleMax() {
		scaling.setToScale(sliderValue, sliderValue);
		return null;//scaling.createTransformedShape(papierBereich).getBounds();
	}

	// =====================================================
	// protected: drawCutRectangle()
	//
	// Muss in der abgeleiteten Klasse ueberschrieben werden.
	//
	// Zeichnet das CutRectangle
	// (wenn nicht überschrieben wird es hier gezeichnet)
	// =====================================================   
	protected void drawRectangleResize(Graphics g) {
		bildFormat.drawRectangleResize(g);	 
	}
}
