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
package pm.gui;

import java.awt.*;

import java.awt.event.*;
 
import javax.swing.*;

 
import pm.utilities.*;
 
 

/**
 * Text für eine Serie editieren
 * 
 * Es wirde EINE Instanz erzeugt und im Window "Suchen" im 
 * linken Tabpane-Bereich aufgerufen.
 * 
 * In der Klasse "PM_SequenceText" wird EIN Dokument verwaltet 
 * (schreiben, lesen, verändern....)
 */
@SuppressWarnings("serial")
public class PM_WindowSequenceText extends JPanel implements PM_Interface {

 
 



	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_WindowSequenceText(PM_WindowMain windowMain)  {

		 
	 
		 
	 
		add(getPanel());

	}

	// ======================================================
	// start()
	//
	// ======================================================
	public JPanel getPanel() {
	
	 
		 	 
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		p.add(getUpperPanel(), BorderLayout.NORTH);
		p.add(getTextPanel(), BorderLayout.CENTER);
		p.add(getLowerPanel(), BorderLayout.SOUTH);
		 
		
		return p;
	}
 
	
	
	
 


	// ======================================================
	// doAbbrechen()
	// ======================================================
	private void doAbbrechen() {

		 
	}

	// ======================================================
	// Upper Panel generieren
	// ======================================================
	private JPanel getUpperPanel() {
		JPanel panel = new JPanel();
//		panel.setPreferredSize(new Dimension(500, 500));
		panel.setBackground(Color.YELLOW);
		// panel.setLayout(new FlowLayout(FlowLayout.LEFT));

//		JLabel lab = new JLabel(sequence.getTypeString() + ": "
//				+ sequence.toStringComboBox());
		JLabel lab = new JLabel("Tagebuch-Einträge fürs Fotoalbum und alle Serien:");
		panel.add(lab);

		return panel;
	}

	// ======================================================
	// getBelowPanelDarstellen()
	//
	// Eine selektierte Serien wird dargestellt oder
	// es wird die Diashow aufgerugen
	// ======================================================



	private JScrollPane getTextPanel() {

		JTextPane pane = new JTextPane();
		JScrollPane scroll = new JScrollPane(pane);
		scroll.setPreferredSize(new Dimension(400, 300));


		
		return scroll;

	}

	// ======================================================
	// getLowerPanel()
	// ======================================================
	private JButton buttonBeenden = null;
	private JButton buttonUebernehmen = null;

	private JPanel getLowerPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 50));
		panel.setBackground(Color.GRAY);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// ---------------------------------------------------
		// Button "Übernehmen"
		// ---------------------------------------------------
		buttonUebernehmen = new JButton("Text übernehmen");
		buttonUebernehmen.setEnabled(false);
		panel.add(buttonUebernehmen);
		// buttonUebernehmen.setBackground(Color.ORANGE);
		ActionListener alButtonUebernehmen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	//			writeTextFile();
				buttonUebernehmen.setEnabled(false);
				buttonBeenden.setEnabled(true);
			}
		};
		buttonUebernehmen.addActionListener(alButtonUebernehmen);
		// ---------------------------------------------------
		// Button "beenden"
		// ---------------------------------------------------
		buttonBeenden = new JButton("Beenden");
		buttonBeenden.setEnabled(true);
		panel.add(buttonBeenden);
		// buttonBeenden.setBackground(Color.ORANGE);
		ActionListener alButtonBeenden = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 
			}
		};
		buttonBeenden.addActionListener(alButtonBeenden);

		// ---------------------------------------------------
		// Button "Abbrechen"
		// ---------------------------------------------------
		JButton buttonAbbrechen = new JButton("Abbrechen");
		panel.add(buttonAbbrechen);
		// buttonUebernehmen.setBackground(Color.ORANGE);
		ActionListener alButtonAbbrechen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAbbrechen();
			}
		};
		buttonAbbrechen.addActionListener(alButtonAbbrechen);

		return panel;

	}

}
