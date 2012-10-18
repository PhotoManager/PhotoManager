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

import javax.swing.*;
 

import pm.gui.*;

/** Vor dem Drucken ändern des Bildes in einem modalen Dialog
 * 
 * Diese Methode wird von einem zum Drucken ausgewählten Bild
 * aufgerufen. Das Bild muss im Papierbereich stehen.
 *
 * Die Änderungen werden nur temporär zum Drucken vorgenommen. Wird das Bild
 * aus dem Papierbereich entfernt, so werden auch die hier vorgenommenen
 * Änderungen verworfen.
 * 
 * JPanel 
 *   |--> PM_Ausschneiden
 *              |---> PM_AusschneidenDrucken   
 *                          |--> PM_DialogDruckenAendernUI 
 *                                      |--> PM_DialogDruckenAendern  
 *                          
 * 
 *
 */
@SuppressWarnings("serial")
public class PM_CutTempPrint extends PM_CutTempPrintUI {

	protected boolean returnStatus = true;

	//==========================================================
	// Konstruktor (wird angelegt und bleibt IMMER erhalten)
	//  (wenn ein anderes Bild ausgeschnitten wird, wird start() aufgerufen)
	//==========================================================
	public PM_CutTempPrint(PM_WindowMain windowMain, final JDialog dialog) {
		super(windowMain, dialog);
	}

	//======================================================
	//  start()
	//	
	// ein anderes Bild wird ausgeschnitten
	//======================================================	
	public void start(PM_PicturePrint pictureDruckdaten) {	
		this.pictureDruckdaten = pictureDruckdaten;
		setBildFormatNeu(pictureDruckdaten.getBildFormat());
		picture = pictureDruckdaten.getPicture();
		
		super.start();
		

	}
	
	
	//==========================================================
	// doDrehen()
	//
	// Der Button "Drehen" wurde betätigt
	//==========================================================
	protected void doDrehen() {
		bildFormat.drehenCutRectangle();
		repaint();
	}

	//======================================================
	//  setBildFormatAenderung()
	//
	// Das Bildformat wird geändert (ComboBox)
	//======================================================	
	protected void setBildFormatAenderung(PM_PictureFormat bildFormat) {
		if (this.bildFormat == bildFormat) return; // keine Änderung
		this.bildFormat = bildFormat;
		bildFormat.setScaling(sliderValue);
		// Das "neue" mit den "alten" Werten initialisieren
		bildFormat.init(pictureDruckdaten, this.bildFormat.getDruckBereich(), this.bildFormat
				.getCutRectangle());
		
		repaint();
	}
	
	//==========================================================
	// getReturnStatus()
	//
	// Wird nach Beendigung des Dialogs aufgerufen.
	// 
	// return true: Es wurden Änderungen vorgenommen
	//        false: Es wurden KEINE Änderungen vorgenommen	
	//==========================================================
	public boolean getReturnStatus() {
		 
		pictureDruckdaten.setCutRectangle(bildFormat.getCutRectangle());
		
		
		//	pictureDruckdaten.setDruckBereich( bildFormat.getDruckBereich());				 				
		return returnStatus;
	}

}
