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
 
import pm.picture.*;
import pm.utilities.PM_Interface;
import pm.utilities.PM_MSG;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;


 

/** Logische Drucker fest EINEM physikalischen Drucker zugeordnet.
 * 
 * Während der Initialisierungsphase wird für jeden PM_SystemDrucker (das ist
 * ein physik. Drucker) eine oder mehrere fest zugeordnete PM-Drucker 
 * (logische Drucker) zugeordnet. Alles wird in der Klasse PM_XML_File_Einstellungen
 * während der Initialisierung durchgeführ.
 * 
 * Die Instanzen werder in einer Liste im zugehörigen phys. Drucker gehalten.
 *
 *  HIER erfolgt das eigentliche Drucken
 *  ====================================
 *
 *  Die Werte für den Drucker stehen in der Datei "Einstellungen".
 *  
 *  
 *  
 *  System-Name: Druckername eines realen Druckers (gleich System-Drucker). Unter diesem Namen, wurde
 *               der Drucker im Betriebssystem eingerichtet.
 *               Es können mehrere System-Drucker vorhanden sein, z.B. Laserdrucker, Farbdrucker. 
 *               
 *  PM-Name: Logischer Druckername. Es können mehrere logische Druckernamen
 *           zu einem System-Drucker eingerichtet werden, z.B. mit unterschiedlichen Druckbereichen.
 *           
 *                  
 *  Beispiel eines System-Druckers mit dem System-Namen "Laserdrucker" und zwei PM-Drucker,
 *           die sich im Druckbereich unterscheiden. Es werden 2 PM_PmDrucker-Instanzen eingerichtet:
 *           eine mit dem pm-namen "standard" und eine weitere Instanz mit dem PM-Namen "Kalender".
 *           
 *                    
 *  <system-drucker system-name="Laserdrucker">
 *     <!--   Farbdrucker -->
 *     <pm-drucker pm-name="standard">
 *       <druck-bereich x1="10" x2="585" y1="5" y2="809"/>
 *     </pm-drucker>
 *     <pm-drucker pm-name="Kalender">
 *       <druck-bereich x1="20" x2="575" y1="32" y2="809"/>
 *     </pm-drucker>
 *   </system-drucker>
 *
 *
 *  Bei der Aktion "Drucker einrichten" wird ein Dialog geöffnet mit allen vorhandenen System-Druckernamen,
 *  die das SYSTEM kennt. Wird einer ausgewählt, so wird mit dem ausgewählten System-Namen eine PM_PmDrucker-
 *  Instanz gesucht. Wenn nigefu, dann wird eine immer vorhandene Default-Instanz genommen.
 *
 */
public class PM_PmPrinter implements PM_Interface, Printable {

 
	 

	private PM_SystemPrinter systemPrinter;  
	private String pmName;

 	private PM_PaperFormat papierFormat;

 
	private Rectangle2D printArea; 
	private PM_PicturePrint[] picturesToPrint = null;  

	// =====================================================
	// Konstruktor
	// =====================================================

	public PM_PmPrinter(PM_SystemPrinter systemDrucker, String pmName) {
		this.pmName = pmName;
		this.systemPrinter = systemDrucker;
		// Papierbereich (wird  bei init() überschrieben)		
		printArea = new Rectangle2D.Double();
		printArea.setRect(systemDrucker.getPapierBereichGesamt());
		
	}

	// =====================================================
	// init()
	//
	// Ein Drucker wurde neu ausgewählt, d.h. der zuvor ausgewählte Drucker
	// muss freigegeben werden und der jetzt aktuelle initialisiert werden.
	//
	//  Der übergebene Drucker ist:
	//      Nicht "this":  Drucker-Resources freigeben.
	//      Er ist "this": Drucker initialisieren
	//           
	// =====================================================
	public void init(PM_PmPrinter drucker, PM_PaperFormat papierFormat) {
		if (drucker == this) {
			// Dies wird jetzt der aktuelle, ausgewählte Drucker.
			// Damit wird dann gedruckt
			this.papierFormat = papierFormat;
			this.papierFormat.init(this);
			picturesToPrint = this.papierFormat.getAllePictureDrucken();
		} else {
			// Drucker Resourcen freigeben (drucker zurücksetzen)
			papierFormat = null;
		}

	}

	// =====================================================
	// getAufloesung()
	// getPixelBreiteMillimeter()
	// getPixelHoeheMillimeter()
	// =====================================================	
	public Point2D getAufloesung() {
		return systemPrinter.getAufloesung();
	}

	public double getPixelBreiteMillimeter() {
		return systemPrinter.getPixelBreiteMillimeter();
	}

	public double getPixelHoeheMillimeter() {
		return systemPrinter.getPixelHoeheMillimeter();
	}

	// =====================================================
	// setPapierFormat()
	// =====================================================	
	public void setPapierFormat(PM_PaperFormat papierFormat) {
		this.papierFormat = papierFormat;
	}

	// =====================================================
	// getSystemDrucker()
	// =====================================================	
	public PM_SystemPrinter getSystemPrinter() {
		return systemPrinter;
	}

	// =====================================================
	// addPictureDrucken()
	//
	// Es wird ein Bild zum Drucken hinzugefügt
	// Return: Bildnummer (0..max)
	//         Kein Platz:  -1
	//         
	// =====================================================	
	
	/**
	 * Add the picture to the paper area.
	 * <p>
	 * The subwindow "Print" has an index view and
	 * the paper area.
	 * The index view hosts all pictures to be print.
	 * In the paper area (right side of the index view) are
	 * the actual pictures to print on one paper.
	 * 
	 */
	public int addToPaperArea(PM_Picture picture, PM_PictureFormat bildFormat, String beschriftung) {
		// check whether you can take over the picture.
		// (you can only print 1, 2, 4 or 6 picture on one hard copy)
		for (int i = 0; i < picturesToPrint.length; i++) {
			if ((picturesToPrint[i].isEmpty())) {
				// you can take over
				return picturesToPrint[i].init(picture, bildFormat, beschriftung);
			}
		}
		return -1;
	}

	// =====================================================
	// clear()
	//
	// Alle Bilder löschen         
	// =====================================================	
	public void clear() {
		for (int i = 0; i < picturesToPrint.length; i++) {
			picturesToPrint[i].delete();
		}
	}

	// =====================================================
	// setPapierbereich() / setDruckbereich()
	// =====================================================
 
//	public void setDruckBereichGesamt(Rectangle druckBereichGesamt) {
//		this.druckBereichGesamt = new Rectangle(druckBereichGesamt);
//	}
	 
	public void setDruckBereichGesamtRaender(double oben, double rechts, double unten, double links) {
		Rectangle2D papierBereich = systemPrinter.getPapierBereichGesamt();
		oben = oben / systemPrinter.getPixelHoeheMillimeter();
		unten = unten / systemPrinter.getPixelHoeheMillimeter();
		links = links / systemPrinter.getPixelBreiteMillimeter();
		rechts = rechts / systemPrinter.getPixelBreiteMillimeter();
		
		printArea = new Rectangle2D.Double(
				links,
				oben,
				papierBereich.getWidth() - rechts - links,
				papierBereich.getHeight() - oben - unten); 
	}	
	
	
	// =====================================================
	// getAnzahlFreieBilder()
	//
	// liefert die Anzahl der noch zu druckenden Bilder
	// =====================================================
	public int getAnzahlFreieBilder() {
		int ret = 0;
		if (picturesToPrint == null) return 0;
		for (int i = 0; i < picturesToPrint.length; i++) {
			if (!(picturesToPrint[i].isEmpty())) ret++;
		}
		return ret;
	}

	// =====================================================
	// getAlleBilder()
	// liefert die Anzahl der noch zu druckenden Bilder
	// =====================================================
	public PM_PicturePrint[] getAlleBilder() {
		return picturesToPrint;
	}

	// =====================================================
	// getPapierFormat()
	// =====================================================
	public PM_PaperFormat getPapierFormat() {
		return papierFormat;
	}

	// =====================================================
	// getPapierbereich() / getDruckbereich()
	// =====================================================
	public Rectangle2D getPapierBereichGesamt() {
		Rectangle2D rec = new Rectangle2D.Double();
		rec.setRect(systemPrinter.getPapierBereichGesamt());
		return rec;
	}

	public Rectangle2D getDruckBereichGesamt() {
		Rectangle2D rec = new Rectangle2D.Double();
		rec.setRect(printArea);
		return rec;
	}

	// =====================================================
	// toString()
	//
	// für Anzeige in der ComboBox
	// =====================================================
	public String toString() {
		return pmName;
	}

	// =====================================================
	// doPrint()
	//
	// Jetzt wird gedruckt
	// =====================================================
	private PM_WindowPrint windowDrucken = null;

	public void doPrint(PM_WindowPrint windowDrucken) {
		this.windowDrucken = windowDrucken;
		drucken();
	}

	//   ========================  PRIVATE =======================================	
	//	 ========================  PRIVATE =======================================
	//	 ========================  PRIVATE =======================================
	//	 ========================  PRIVATE =======================================
	//	 ========================  PRIVATE =======================================
	//	 ========================  PRIVATE =======================================

	// =====================================================
	// getAnzahlBilder()
	// =====================================================
	private int getAnzahlBilder() {
		int anzahl = 0;
		// -------------------------------------------------------
		// Fehler: Keine Bilder in den Druckbereich uebertragen
		// -------------------------------------------------------
		for (int i = 0; i < picturesToPrint.length; i++) {
			PM_PicturePrint bild = picturesToPrint[i];
			if (bild.isEmpty()) continue;
			anzahl++;
		}
		return anzahl;
	}

	// =====================================================
	// doPrint()
	//
	// Jetzt wird gedruckt
	// =====================================================
	private void drucken() {

		// -------------------------------------------------------
		//  Keine Bilder in den Druckbereich uebertragen
		// -------------------------------------------------------
		if (getAnzahlBilder() == 0) {
			
			 

			if (JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("pmPrinterNoPictures"),
					"Print", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			};
		} else {
			// -------------------------------------------------------
			// Sicherheitsabfrage: Soll tatsächlich gedruckt werden?
			// -------------------------------------------------------
			String text;
			if (windowDrucken.hilfslinienDrucken()) {
				text = PM_MSG.getMsg("pmPrinterWithSubLines");
			} else {
				text = PM_MSG.getMsg("pmPrinterNoSubLines");
			}
			if (JOptionPane.showConfirmDialog(null, text, "Print", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			};
		}

		// --------------------------------------------------------------------------
		// --------------------------------------------------------------------------
		// Jetzt drucken !!!!
		// --------------------------------------------------------------------------
		// --------------------------------------------------------------------------
		PrinterJob printerJob = systemPrinter.getPrinterJob();
		// --------------------------------------------------------------------------
		// Vor dem eigentlichen Drucken den im System eingestellten Drcukbereich
		// (imageableArea) auf die groesse des Papierbereiches stellen, da sonst
		// zu klein
		// gedruckt wird. Nach dem Drucken den urspruenglichen Bereich wieder
		// einstellen.
		// -----------------------------------------------------------------------------

		PageFormat pageFormat = printerJob.defaultPage();
		Paper paper = pageFormat.getPaper();
		// Werte sichern
		double imageableX = paper.getImageableX();
		double imageableY = paper.getImageableY();
		double imageableWidth = paper.getImageableWidth();
		double imageableHeight = paper.getImageableHeight();
		// neu setzern
		paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
		pageFormat.setPaper(paper);

		printerJob.setPrintable(this, pageFormat); // this bedeutet, dass hier print aufgerufen wird !!!
		// ----- nun kann endlich gedrucktt werden ------
		gedruckt = false; // workaround !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
//		 System.out.println("Pageformat: getHeight() = " + pageFormat.getHeight());
//		 System.out.println("Pageformat: getWidth() = " + pageFormat.getWidth());
//		 System.out.println("Pageformat: getImageableHeight() = " + pageFormat.getImageableHeight());
//		 System.out.println("Pageformat: getImageableWidth() = " + pageFormat.getImageableWidth());
//		 System.out.println("Pageformat: getImageableX() = " + pageFormat.getImageableX());
//		 System.out.println("Pageformat: getImageableY() = " + pageFormat.getImageableY());
		
		
//		 System.out.println("Paper: getHeight() = " + paper.getHeight());
//		 System.out.println("Paper: getWidth() = " + paper.getWidth());
//		 System.out.println("Paper: getImageableHeight() = " + paper.getImageableHeight());
//		 System.out.println("Paper: getImageableWidth() = " + paper.getImageableWidth());
//		 System.out.println("Paper: getImageableX() = " + paper .getImageableX());
//		 System.out.println("Paper: getImageableY() = " + paper.getImageableY());	
		
		
		try {
 			printerJob.print(); // hier in dieser Klasse Implementiert (Printable)
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// urspruengliche Wert wieder eintragen
		paper.setImageableArea(imageableX, imageableY, imageableWidth, imageableHeight);
		pageFormat.setPaper(paper);

	}

	// ==============================================================
	// print()
	//
	// Implementierung vom Interface "Printable"
	// (wird von printerJob.print() aufgerufen)
	// =============================================================
	private boolean gedruckt = false; // workaround

	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {

		

//		 System.out.println("getHeight() = " + pf.getHeight());
//		 System.out.println("getWidth() = " + pf.getWidth());
//		 System.out.println("getImageableHeight() = " + pf.getImageableHeight());
//		 System.out.println("getImageableWidth() = " + pf.getImageableWidth());
//		 System.out.println("getImageableX() = " + pf.getImageableX());
//		 System.out.println("getImageableY() = " + pf.getImageableY());

//		 System.out.println(" int pi = " + pi);


		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		// boolen gedruckt ist ein Workaround (print wird 2 mal mit pi == 0
		// aufgerufen !!)
		// Beim 2. Aufruf darf erst gedruckt werden
		if (gedruckt) aufbereitenDruckausgabe(g);
		gedruckt = true;
		return Printable.PAGE_EXISTS;

	}

	//======================================================
	//  aufbereitenDruckausgabe()
	//
	// Aufbereiten "Graphics" zum Drucken.
	// (hier wird noch NICHT gedruckt)
	//======================================================
	private void aufbereitenDruckausgabe(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform scaling = new AffineTransform();
		scaling.setToScale(1, 1);
		// Hilfslinien aufbereiten
		if (windowDrucken.hilfslinienDrucken()) {
			windowDrucken.hilfslinienAufbereiten(g2, scaling);
		}
		// Alle Bilder 
		for (int i = 0; i < picturesToPrint.length; i++) {
			PM_PicturePrint bild = picturesToPrint[i];
			if (bild.isEmpty()) continue;
			windowDrucken.einBildAufbereiten(g2, bild, scaling);
		}
	}

} // PM_PmDrucker
