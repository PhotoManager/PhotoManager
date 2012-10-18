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
import java.util.*;
import java.util.List;

import pm.index.PM_PictureView;
import pm.picture.*;

import javax.swing.*;

import javax.swing.event.*;

 

 
import pm.sequence.*;
 
import pm.utilities.*;
 
 

/** Anzeigen einer Liste von Serien
 * 
 *  Wird eine Serie in der Liste selektiert, kann sie angezeigt werden.
 *  
 * 
 */
public class PM_WindowDialogSequences implements PM_Interface {

	private PM_WindowMain windowMain = null;
	 
 
	 
	 
	private JDialog dialog = null;
	private JList listSerien = null;
	private PM_Picture picture;

	private List jListe = null;
	 

	// ==========================================================
	// Konstruktor f�r
	// (1) links im Vorschaufenster darstellen oder
	// (2) als Diashow aufrufen
	// ==========================================================
	public PM_WindowDialogSequences(PM_WindowMain windowMain,
			PM_PictureView pictureView 
	/*		PM_IndexViewCollection_deprecated pictureViewCollection */ ) {

		this.windowMain = windowMain;
		 
		picture = pictureView.getPicture();
		 
		jListe = getListeSerien();

		start(getBelowPanelDarstellen());

	}

 
	// ======================================================
	// start()
	//
	// ======================================================
	private void start(JPanel upperPanel) {
		 
	 

		JPanel panel = new JPanel();
		dialog = new JDialog(windowMain, true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(getUpperPanel(jListe), BorderLayout.NORTH);
		p.add(upperPanel, BorderLayout.CENTER);
		dialog.getContentPane().add(p);
		dialog.pack();

		dialog.setVisible(true);
	}

	// ======================================================
	// serieDarstellen()
	//
	// Die selectierte Serie jetzt als Thumbs darstellen
	// ======================================================
	private void serieDarstellen() {

		dialog.dispose();
	}

	 
 

	// ======================================================
	// doAbbrechen()
	// ======================================================
	private void doAbbrechen() {

		dialog.dispose();
	}

	// ======================================================
	// setListeSerien()
	//
	// Alle Serien-Namen anzeigen
	// ======================================================
	private List getListeSerien() {

		List<PM_Sequence> liste = new ArrayList<PM_Sequence>();

		String seq = picture.meta.getSequence();
		String[] sa = seq.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			String[] ss = s.split("_");
			if (ss.length != 2)
				break;
			PM_Sequence sequenz = PM_Sequence.getSequenzFromAll(ss[0]);
			liste.add(sequenz);
		}

		return liste;
	}

	// ======================================================
	// Upper Panel generieren
	// ======================================================
	private JPanel getUpperPanel(List liste) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 500));
		panel.setBackground(Color.YELLOW);
		// panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Liste
		listSerien = new JList(liste.toArray());
		listSerien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSerien.setVisibleRowCount(10);
		listSerien.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Object o = listSerien.getSelectedValue();
				if (o instanceof PM_Sequence) {
					 
					buttonUebernehmen.setEnabled(true);
				}
			}
		});

		panel.add(listSerien);

		return panel;
	}

	// ======================================================
	// getBelowPanelDarstellen()
	//
	// Eine selektierte Serien wird dargestellt oder
	// es wird die Diashow aufgerugen
	// ======================================================
	private JButton buttonUebernehmen = null;

	private JPanel getBelowPanelDarstellen() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 50));
		panel.setBackground(Color.GRAY);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// ---------------------------------------------------
		// Button "�bernehmen"
		// ---------------------------------------------------
		buttonUebernehmen = new JButton("Serie darstellen");
		buttonUebernehmen.setEnabled(false);
		panel.add(buttonUebernehmen);
		// buttonUebernehmen.setBackground(Color.ORANGE);
		ActionListener alButtonUebernehmen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serieDarstellen();
			}
		};
		buttonUebernehmen.addActionListener(alButtonUebernehmen);

		// ---------------------------------------------------
		// Button "Abbrechen"
		// ---------------------------------------------------
		JButton buttonAbbrechen = new JButton("abbrechen");
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
