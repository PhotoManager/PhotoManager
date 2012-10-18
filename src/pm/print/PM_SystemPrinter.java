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

import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import javax.print.*;

/** Ein physikalischer Drucker, so wie am Rechner angeschlossen oder erreichbar
 * 
 * Während der Initialisierung wir für jeden physik. Drucker EINE Instanz
 * erzeugt, die nicht wieder zerstört wird.
 * 
 * Suchen der Sysstem-Drucker:
 *  In der Klasse PM_AlleSystemDrucker wird der zugehörige PrinterJob
 *  gesucht ( mit PrinterJob.lookupPrintServices()) und als 
 *  Instanzvariable hier gespeichert.
 *  
 * Der PrinterJob wird benötigt für:
 *   - Einstellungen der Druckereigenschaften (Papierqualität ...) in einem
 *     (System-)Dialog, der meist treiberabhängig ist. Auruf in 
 *     PM_WindowDrucken, da die Einstellungen vom "PM-Druck-Fenster" angestossen
 *     wird.
 *   - Zur Durchführung des eigentlichen Druckes, der immer in der entsprechenden 
 *     PM_Drucker-Instanz durchgeführt wird
 * 
 *   
 * Alle Instanzen werden in der Klasse PM_AlleSystemDrucker in einer Liste 
 * gespeichert. Die Instanz der Klasse PM_AlleSystemDrucker steht in der Klasse
 * PM_XML_File_Einstellungen.
 * 
 * 
 * 
 * Es können mehrere physikalische Drucker an einem Rechner angeschlossen oder
 * erreichbar sein.
 * 
 * Ein physikalischer Drucker hat folgende unveränderliche Eingeschaften:
 * Papierbereich: das ist die Papiergröße, die im Drucker verwendet wird, i.d.R. DIN a 4.  
 *                Sie kann vom Drucker meist nicht vollständig "bedruckt" werden, d.h.
 *                es bleib immer ein Rand, der unbedruckt bleibt.
 * Druckbereich: der Bereich INNERHALB des Papierbereiches, der vollständig
 *               bedruckt werden kann.
 *               Hier wird der max. Druckbereich eingetragen. Der tatsächlich verwendete
 *               Druckbereich wird in der PM-Drucker-Instanz (s.u.) gehalten.
 * Druckerauflösung: in Pixel (Dot) per inch. Meist 72 Dot Per inch.
 *                (72 Dot per inch werden hier HART codiert)
 * 
 * Jeder physikalischer Drucker hat ein oder mehrere (LOGISCHE) PM-Drucker. Hier wird
 * ein Array aller diesem physikalischen Drucker zugeordneten PM-Drucker geführt.
 * (die PM-Drucker haben u.a. unterschiedliche Druckbereiche)
 * 
 * 
 * 
 *  
 *
 */
public class PM_SystemPrinter {

	// final - Werte 
	private final String systemName;
	private final Point2D aufloesung;
	private final Rectangle2D papierBereichGeamt;

	// überschreibbar
	private PrinterJob printerJob;
	private PM_PmPrinter[] allePmDrucker = null;
	private boolean eingestellt = false;

	// =====================================================
	// Konstruktor
	// =====================================================
	public PM_SystemPrinter(PrintService printService) {

		//	DocPrintJob job = printService.createPrintJob();
//System.out.println("Print Service (SystemPrinter) found: " + printService.getName());
		printerJob = PrinterJob.getPrinterJob();
		try {
			printerJob.setPrintService(printService);
		} catch (PrinterException e) {
		}
		systemName = printService.getName();

		// Papierbereich (DIN A 4 hochkant) und Auflösung ermitteln 
		aufloesung = new Point2D.Double(72.0d, 72.0d); // default-Auflösung
		double breitePapierBereich = 210 * aufloesung.getX() / 25.4; // für DIN A 4 hochkant
		double hoehePapierBereich = 297 * aufloesung.getY() / 25.4; // für DIN A 4 hochkant
		papierBereichGeamt = new Rectangle2D.Double(0, 0, breitePapierBereich,
				hoehePapierBereich);

		// es wird immer ein Default-PM-drucker angelegt
		allePmDrucker = new PM_PmPrinter[1];
		allePmDrucker[0] = new PM_PmPrinter(this, "default"); // Defaultdrucker
	}

	// =====================================================
	// getPrinterJob()
	// =====================================================	 
	public PrinterJob getPrinterJob() {
		return printerJob;
	}

	// =====================================================
	// getPapierBereichGesamt()
	// =====================================================	 
	public Rectangle2D getPapierBereichGesamt() {
		return papierBereichGeamt;
	}

	// =====================================================
	// getAllePmDrucker()
	// =====================================================
	public PM_PmPrinter[] getAllePmDrucker() {
		return allePmDrucker;
	}

	// =====================================================
	// getAufloesung()
	// =====================================================	
	public Point2D getAufloesung() {
		return aufloesung; // dot per inch
	}

	public double getPixelBreiteMillimeter() {
		return 25.4 / getAufloesung().getX();
	}

	public double getPixelHoeheMillimeter() {
		return 25.4 / getAufloesung().getY();
	}

	// =====================================================
	// clear()
	//
	// Alle  PM-Drucker jetzt zurücksetzen.
	// =====================================================
	public void init(PM_PmPrinter drucker, PM_PaperFormat papierFormat) {
		for (PM_PmPrinter pmPrinter: allePmDrucker) {	 
			pmPrinter.init(drucker, papierFormat);
		}
	}

	// =====================================================
	// getName()
	// =====================================================
	public String getName() {
		return systemName;
	}

	// =====================================================
	// setEingestellt()/getEingestellt()
	// =====================================================
	public void setEingestellt(boolean eingestellt) {
		this.eingestellt = eingestellt;
	}

	public boolean getEingestellt() {
		return eingestellt;
	}

	// =====================================================
	// addPmDrucker()
	//
	// Ein PM-Drucker wird hinzugefügt.
	// =====================================================
	public void addPmDrucker(PM_PmPrinter pmDrucker) {
		Vector<PM_PmPrinter> v = new Vector<PM_PmPrinter>();
		for (int i = 0; i < allePmDrucker.length; i++) {
			v.add(allePmDrucker[i]);
		}
		v.add(pmDrucker);
		allePmDrucker = (PM_PmPrinter[]) v.toArray(new PM_PmPrinter[v.size()]);
	}

	// =====================================================
	// toString()
	//
	// für Anzeige in der ComboBox
	// =====================================================
	public String toString() {
		return systemName;
	}

}
