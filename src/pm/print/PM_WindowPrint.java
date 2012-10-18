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
import pm.index.*;
import pm.utilities.*;
 
import javax.print.*;
import javax.swing.*;
import java.awt.print.*;

/**   
 * The subwindow "Print"
 *
 */

 
@SuppressWarnings("serial")
public class PM_WindowPrint extends PM_WindowPrintUI implements
		PM_Interface {

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_WindowPrint(  ) {
		super(  );
	}

	 
	/**
	 * Tells no printer anailable.
	 */
	protected void noPrinterAvailable() {
		
		
		JOptionPane.showConfirmDialog(this, PM_MSG.getMsg("winPrtMsgNoPrinter"), "Print",
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	}
  
	
	 
	/**
	 * Get all thumbs to this index view.
	 * <p>
	 * Remove the current thumbs in the index view and 
	 * take over the thumbs from another index view.
	 * 
	 * @param ivFrom - the index view with the thumbs to take over.
	 * 
	 */
	@Override
	public void getAllThumbs(PM_Index  ivFrom ) {

		if (pmPrinter == null) {
			noPrinterAvailable();
			return;
		}
		
		super.getAllThumbs(ivFrom );
 
		 
	}

	/**
	 * Append all thumbs to this index view.
	 * <p>
	 * Take over all the thumbs from another index view.
	 * 
	 * @param ivFrom - the index view with the thumbs to take over.
	 * 
	 */
	@Override
	public boolean appendAllThumbs(PM_Index  ivFrom ) {
		if (pmPrinter == null) {
			noPrinterAvailable();
			return false;
		}
		 
		return super.appendAllThumbs(ivFrom );
	} 
	
	
	
 
 
	 

	// ==========================================================
	// addPictureToPrint()
	//
	// Ein Bild zum Drucken ausw�hlen (in den Papierbereich �bernehmen)
	//
	// ========================================================== 
	/**
	 * Add the picture view to the paper area.
	 * <p>
	 * The thumbs in the index view of right side main window
	 * is double clicked. This picture added to the paper area.
	 * <p>
	 * On the right side mainwindow are in the index view
	 * all the pictures choosen to print. 
	 * In the paper area (right side of the index view) are
	 * the actual pictures to print on one paper.
	 */
	public void doubleClickOnPictureView(PM_PictureView pictureView) {	
		
		
		if (pmPrinter == null) {
			noPrinterAvailable();
			return;
		}

		int bildNummer = pmPrinter.addToPaperArea(pictureView.getPicture(), pictureFormat,
				getItemBildBeschriftung());
		if (bildNummer == -1) {
			
			
			
			JOptionPane.showConfirmDialog(this, PM_MSG.getMsg("winPrtMsgNoSpace"),
					"Print", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		doRepaint();

	}

 

	/**
	 * Set defalt zooming 100 %.
	 */
	protected void setDefaultZooming() {
		sliderValue = bildschirmAufloesung / 72.0F;
	}

	 
	/**
	 * All pictures in the 'picture to print'-window are removed.
	 */
	protected void doClear() {
		if (pmPrinter == null) {
			return;
		}

		pmPrinter.clear();
		doRepaint();
	}

	 
	/**
	 * The Print button is pressed.
	 */
	protected void doPrint() {
		pmPrinter.doPrint(this);
	}

	/**
	 * A new picture format is selected.
	 */
	protected void setPictureFormat(PM_PictureFormat bildFormat) {
		this.pictureFormat = bildFormat;
	}

	/**
	 * A new paper format is selected.
	 */
	protected void setPaperFormat(PM_PaperFormat papierFormat) {
		this.paperFormat = papierFormat;
		selectPrinter( );
	}

	 
	/**
	 * Another PmPrinter is selected.
	 */
	protected void setPmPrinter(PM_PmPrinter pmPrinter) {
		this.pmPrinter = pmPrinter;
		selectPrinter( );
	}

	 
	/**
	 * Setup the system printer.
	 * 
	 * DO NOT REMOVE THIS METHOD.
	 * 
	 * I can't set up the system printer with java. 
	 */
	protected void doSetupSystemPrinter() {
		if (pmPrinter == null) {
			noPrinterAvailable();
			return;
		}

		PrinterJob printerJob = pmPrinter.getSystemPrinter().getPrinterJob();
		PrintService psAlt = printerJob.getPrintService();

		if (printerJob.printDialog()) {
			PrintService psNeu = printerJob.getPrintService();
			if (psAlt == psNeu) {
				pmPrinter.getSystemPrinter().setEingestellt(true);
				setHinweisDruckerEinstellen();
			} else {
				
				 
				
				
//				String fehler = "Hier Druckerwechsel noch fehlerhaft.\nAnderen Drucker bei PM ausw�hlen.";
				String fehler = PM_MSG.getMsg("winPrtMsgError");
				JOptionPane.showConfirmDialog(this, fehler, "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				return;
				 
			}

		}
	}

	// ==========================================================
	// neuerDrucker()
	// 
	// Hier zentral wird Papier gel�scht, d.h. neue Drucker ...
	// ==========================================================
	/**
	 * 
	 */
	private void selectPrinter( ) {
		allSystemPrinter.init(pmPrinter, paperFormat);
		setBildFormatListe(paperFormat);
		maxPapierBereich.setText(paperFormat.getAbmessungPapierBereich());
		doRepaint();
	}
}
