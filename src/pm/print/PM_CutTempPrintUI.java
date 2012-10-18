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
import pm.utilities.*;

import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



//
//
//  Vor dem Drucken werden hier die PM_OLD_PictureDrucken eines Bildes geändert
//  und ggf. ein Text zum Drucken aufgenommen und positioniert.
//   
//  (z.B. neu ausgeschnitten, das Bildformat wird geaendert .....)
//
//
//

/** Das UI für das Ändern eines Bildes vor dem Drucken (als modaler Dialog)
 * 
 * Hier wird "PM_AusschneidenDrucken" instantiiert.
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
public class PM_CutTempPrintUI extends PM_CutTempPrint_deprecated implements PM_Interface {

	//	JScrollPane scrollPane = null;
//	private JSlider slider = null;
	//  PM_AusschneidenDrucken ausschneidenDrucken = null;

//	private int sliderValue = 100;

	


	private JComboBox formatListe;

	private JButton clearButtonText = null;

	private JTextField textField = null;
	private JButton datum = null;
	private JButton uhrzeit = null;
	private JButton bemerkungen = null;
	private JTextField maxPapierBereich = null;

	private JRadioButton anfasserPapier = null;
	private JRadioButton anfasserDruck = null;

	//==========================================================
	// getAnfasserPapier()
	//==========================================================
	public JRadioButton getAnfasserPapier() {
		return anfasserPapier;
	}

	//==========================================================
	// Konstruktor
	//==========================================================
	public PM_CutTempPrintUI(PM_WindowMain windowMain, final JDialog dialog) {
		super(windowMain, dialog);

		setBackground(Color.BLUE);
	 
		JPanel upperPanel = getUpperPanel();
		picturePanel = getPicturePanel();

		// assemble  
		setLayout(new BorderLayout());
		add(upperPanel, BorderLayout.NORTH);
		add(picturePanel, BorderLayout.CENTER);
	}

	//======================================================
	//  start()
	//
	// ein anderes Bild wird ausgeschnitten
	//======================================================
	protected void start() {	
		
		String abmess = pictureDruckdaten.getDrucker().getPapierFormat().getAbmessungPapierBereich();
		maxPapierBereich.setText(abmess);
		
		
		PM_PictureFormatCollection instance = PM_PictureFormatCollection.getInstance();
		PM_PictureFormat[] liste = instance.getBildFormate(pictureDruckdaten.getDrucker().getPapierFormat());
		formatListe.setModel(new DefaultComboBoxModel(liste));
		formatListe.setSelectedItem(pictureDruckdaten.getBildFormat());
		
		// Datum, Uhrzeit und Bemerkung setzen 
		//  (diese Werte ändern sich nicht)
		Date date = picture.meta.getDateCurrent();
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		datum.setText(dateFormat.format(date));
		dateFormat = new SimpleDateFormat("HH:mm");
		uhrzeit.setText(dateFormat.format(date));
		bemerkungen.setText(picture.meta.getRemarks());
	}
	
 
	//==========================================================
	// doDrehen()
	//
	// Der Button "Drehen" wurde betätigt
	//==========================================================
	protected void doDrehen() {
		// Wird überschrieben
	}
	
	
	//======================================================
	//  setSliderValue
	//
	// (der aktuell geaenderte Slider Value)
	//
	// hier jetzt alle Bilder mit der geaenderten Größe neu zeichnen !!
	//======================================================  
	public void setSliderValue(int sliderValue) {
		this.sliderValue = sliderValue;
		//   ausschneidenDrucken.doSetSize(sliderValue);  

		setPreferredSize(new Dimension(sliderValue, (int) ((double) sliderValue * 0.75)));
		//		scrollPane.doLayout();
		//		scrollPane.repaint();
	}

	//======================================================
	//  setBildFormatAenderung()
	//
	// Das Bildformat wird geändert (ComboBox)
	//======================================================	
	protected void setBildFormatAenderung(PM_PictureFormat bildFormat) {
		// wird überschrieben
	}
	
	
	//==========================================================
	// getUpperPanel
	//
	// (Oben: einige Buttons)
	//==========================================================
 
	private JPanel getUpperPanel() {

		Color color = Color.yellow;

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(color);

		// -----------------------------------------------------------------
		// 1. Zeile
		// -----------------------------------------------------------------

		JPanel panelErsteZeile = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelErsteZeile.setBackground(color);

		// ----------------------------------------------------
		// Button "Uebernehmen"
		// ----------------------------------------------------
		JButton buttonUebernehmen = new JButton("Übernehmen");
		//   panelErsteZeile.setBackground(color);
		panelErsteZeile.add(buttonUebernehmen);
		ActionListener alUebernehmen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				windowMain.closeBildAendernDrucken();
			}
		};
		buttonUebernehmen.addActionListener(alUebernehmen);

		// -------------------------------------------------------
		// Button "Abbrechen"
		// -------------------------------------------------------
		JButton buttonAbbrechen = new JButton("Abbrechen");
		//   panelErsteZeile.setBackground(color);
		panelErsteZeile.add(buttonAbbrechen);
		ActionListener alAbbrechen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//      ausschneidenDrucken.doUebernehmen();
				/////////////////////////////////////////////////////////////////       result = true;
				windowMain.closeBildAendernDrucken();
			}
		};
		buttonAbbrechen.addActionListener(alAbbrechen);

		// ----------------------------------------------------------
		// Papierformat (nur Anzeige)
		//    (kann nicht geändert werden)
		// ----------------------------------------------------------
		panelErsteZeile.add(new JLabel("Papierbereich [mm]:"));
		maxPapierBereich = new JTextField();
		maxPapierBereich.setEditable(false);
		//	 maxPapierBereich(10);
		panelErsteZeile.add(maxPapierBereich);

		// -------------------------------------------------------
		// ComboBox Bild-Formate
		// -------------------------------------------------------
		panelErsteZeile.add(new JLabel("Bildformat:"));		 
		formatListe = new JComboBox();		 
		panelErsteZeile.add(formatListe);
		  // ComboBox einrichten

		 
		// --------------------------------------------------------------------
		// Button "drehen"
		// --------------------------------------------------------------------
		JButton buttonDrehen = new JButton("Drehen");
		panelErsteZeile.add(buttonDrehen);
		ActionListener alDrehen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDrehen();
			} 
		};
		buttonDrehen.addActionListener(alDrehen);

		// --------------------------------------------------------------------
		// Anfasser "PapierBereich" oder "DruckBereich"
		// --------------------------------------------------------------------		
		anfasserPapier = new JRadioButton("Papier Bereich");
		anfasserPapier.setSelected(true);
		anfasserDruck = new JRadioButton("Druck Bereich");
		anfasserDruck.setSelected(false);
		ButtonGroup group = new ButtonGroup();
		group.add(anfasserPapier);
		group.add(anfasserDruck);
		panelErsteZeile.add(anfasserPapier);
		panelErsteZeile.add(anfasserDruck);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		};
		anfasserPapier.addActionListener(al);
		anfasserDruck.addActionListener(al);
		//	     TitledBorder title1 = BorderFactory.createTitledBorder(border,"Eingabe");
		//	      panel.setBorder(title1);

		// ----------------------------------------------------
		// 2. Zeile: Text zum Drucken
		// ----------------------------------------------------
		JPanel panelZweiteZeile = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelZweiteZeile.setBackground(color);

		clearButtonText = PM_Utils.getJButon(ICON_DELETE);
		ActionListener alClearButtonText = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText("");
				setText("");
			}
		};
		clearButtonText.addActionListener(alClearButtonText);
		panelZweiteZeile.add(clearButtonText);

		panelZweiteZeile.add(new JLabel("Text: "));
		textField = new JTextField("    ");
		textField.setColumns(40);
		panelZweiteZeile.add(textField);

		// ---- KeyListener --------
		KeyListener keylIndex = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				setText(textField.getText().trim());
			}

			public void keyTyped(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {}
		};
		textField.addKeyListener(keylIndex);

		// Datum
		datum = new JButton();
		panelZweiteZeile.add(datum);
		ActionListener alButtonDarstellen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText(textField.getText() + " " + datum.getText());
				setText(textField.getText().trim());
			}
		};
		datum.addActionListener(alButtonDarstellen);

		//  Uhrzeit
		uhrzeit = new JButton();
		panelZweiteZeile.add(uhrzeit);
		ActionListener alUhrzeit = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText(textField.getText() + " " + uhrzeit.getText());
				setText(textField.getText().trim());
			}
		};
		uhrzeit.addActionListener(alUhrzeit);

		//  Bemerkungen
		bemerkungen = new JButton();
		panelZweiteZeile.add(bemerkungen);
		ActionListener alBemerkung = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText(textField.getText() + " " + bemerkungen.getText());
				setText(textField.getText().trim());
			}
		};
		bemerkungen.addActionListener(alBemerkung);

		//  ----------------------------------------------------
		// alle Zeilen zusammen
		// ----------------------------------------------------

		panel.add(panelErsteZeile);
		panel.add(panelZweiteZeile);

		return panel;
	}

}
