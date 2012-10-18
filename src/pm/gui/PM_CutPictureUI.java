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

 
import pm.picture.*;
import pm.utilities.*;
 
 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

import java.util.*;
import java.util.List;

/**
 * Window zum Zeigen und Ausschneiden in einem modalen JDialog
 * 
 * aus übern abbr dreh löschen anzeigen X (ggf. mit Rahmen OHNE Anfasser)
 * ausschneiden X X X (mit Rahmen UND Anfasser) cutRec bewegt X X X X (mit
 * Rahmen UND Anfasser) drehen X X X X (mit Rahmen UND Anfasser) löschen X (ohne
 * Rahmen) abbr X (Rahmen OHNE Anfasser wie VOR ausschn.) übernehmen X (Rahmen
 * OHNE Anfasser)
 */
@SuppressWarnings("serial")
public class PM_CutPictureUI extends PM_Cut implements
		PM_Interface {

	 

	protected Image imageOriginal = null;
	protected PM_Picture picture = null;

	 

	protected   List<PM_Picture> pictureList = null;

	protected boolean diashowAufrufen = false;
	protected JDialog dialog;

	// Qualitaet: Radio Buttons
	private JRadioButton q1 = null;
	private JRadioButton q2 = null;
	private JRadioButton q3 = null;
	private JRadioButton q4 = null;

	private JLabel labelAnzahl = null;
	private JLabel labelNameDatum = null;

	// Ausschneiden
	protected JButton buttonAusschneiden = null;
	protected JButton buttonUebernehmen = null;
	protected JButton buttonAbbrechen = null;
	protected JButton buttonDrehen = null;
	protected JButton buttonLoeschen = null;

	protected JLabel qLabel;
	
	protected JLabel bearbeitet = null; // unten links

	// ====================================================================
	// Die folgenden Methoden sind in "PM_BildInternBearbeiten" deklariert
	// ====================================================================
	protected void doLoeschenCutRectangle() {
	}

	protected void doDrehen() {
	}

	protected void doAbbrechen() {
	}

	protected void doUebernehmen() {
	}

	protected void doAussschneiden() {
	}

	protected void doZeigenNextBild() {
	}

	protected void doZeigenPreviousBild() {
	}

	protected void doAendern() {
	}

	// ======================================================
	// setLowerPanel
	//
	// ======================================================
	protected void setLowerPanel() {
		// set Qualitaet
		q1.setSelected(false);
		q2.setSelected(false);
		q3.setSelected(false);
		q4.setSelected(false);
		switch ( picture.meta.getCategory() ) {
		case 1:
			q1.setSelected(true);
			break;
		case 2:
			q2.setSelected(true);
			break;
		case 3:
			q3.setSelected(true);
			break;
		case 4:
			q4.setSelected(true);
			break;
		}
		// schreiben Anzahl
		int size = pictureList.size();
		int index = pictureList.indexOf(picture );
		String indexSize = Integer.toString(index + 1) + "///"
				+ Integer.toString(size);
		labelAnzahl.setText(indexSize);

		// schreiben Name und Datum
		Date date = picture.meta.getDateCurrent();
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");    		
		labelNameDatum.setText(picture.getFileOriginal()
				.getName()
				+ "  /  " + dateFormat.format(date)); 

		// Label "bearbeitet"
		if (picture.meta.getModified()) {
			bearbeitet.setText("x");
		} else {
			bearbeitet.setText("");
		}
	}

	// ======================================================
	// warnung()
	//
	// Warnung ausgeben wenn Aenderungen beim Ausschneiden nicht uebernommen
	// wurden
	// ======================================================
	protected void warnung() {
		JOptionPane
				.showConfirmDialog(this, PM_MSG.getMsg("modIntNotApply"),
						" ", JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE);
	}
	
	// ==========================================================
	// getLowerAendernPanel
	//
	// (Unten: einige Buttons)
	// ==========================================================
	protected JPanel getLowerAendernPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.yellow);
		panel.setPreferredSize(new Dimension(0, 30));
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Label "bearbeitet"
		bearbeitet = new JLabel("   ");
		panel.add(bearbeitet);
		// bearbeitet.setFont(new Font("Arial", Font.BOLD, fontSize));

		// Button "beenden"
		JButton buttonBeenden = new JButton("stop");
		panel.add(buttonBeenden);
		ActionListener alBeenden = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buttonUebernehmen.isEnabled()) {
					warnung();
					return;
				}
				dialog.dispose();
			}
		};
		buttonBeenden.addActionListener(alBeenden);

		// Button "diashow"
		JButton buttonDiaShow = new JButton("DiaShow");
		panel.add(buttonDiaShow);
		ActionListener alDiaShow = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buttonUebernehmen.isEnabled()) {
					warnung();
					return;
				}
				diashowAufrufen = true;
				dialog.dispose();
			}
		};
		buttonDiaShow.addActionListener(alDiaShow);

		// Button "vdr"
		JButton buttonVDR = new JButton("vdr");
//////		panel.add(buttonVDR);
		ActionListener alVDR = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buttonUebernehmen.isEnabled()) {
					warnung();
					return;
				}
				// Bild zum vdr �bertragen
				
				
				 
			}
		};
		buttonVDR.addActionListener(alVDR);

		
		// Qualitaet
		JPanel qualitaet = new JPanel();
		// qualitaet.setPreferredSize(new Dimension(200,100));
		qualitaet.setLayout(new FlowLayout(FlowLayout.LEFT));
		qLabel = new JLabel("Qualitaet");
		qualitaet.add(qLabel);
		q1 = new JRadioButton();
		q2 = new JRadioButton();
		q3 = new JRadioButton();
		q4 = new JRadioButton();
		q1.setActionCommand("q1");
		q2.setActionCommand("q2");
		q3.setActionCommand("q3");
		q4.setActionCommand("q4");
		ButtonGroup group = new ButtonGroup();
		group.add(q1);
		group.add(q2);
		group.add(q3);
		group.add(q4);
		qualitaet.add(q1);
		qualitaet.add(q2);
		qualitaet.add(q3);
		qualitaet.add(q4);

		RadioListener rl = new RadioListener();
		q1.addActionListener(rl);
		q2.addActionListener(rl);
		q3.addActionListener(rl);
		q4.addActionListener(rl);

		panel.add(qualitaet);


		// Anzahl
		labelAnzahl = new JLabel("   ");
		panel.add(labelAnzahl);

		// Button "next"
		JButton buttonNext = new JButton("-->");
		// ///// panel.add(buttonNext);
		ActionListener alNext = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doZeigenNextBild();
			}
		};
		buttonNext.addActionListener(alNext);

		// Button "ausschneiden"
		buttonAusschneiden = new JButton("aus");
		panel.add(buttonAusschneiden);
		ActionListener alAus = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAussschneiden();
			}
		};
		buttonAusschneiden.addActionListener(alAus);

		// Button "uebernehmen"
		buttonUebernehmen = new JButton("�bern");
		panel.add(buttonUebernehmen);
		ActionListener alUeb = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUebernehmen();
			}
		};
		buttonUebernehmen.addActionListener(alUeb);

		// Button "abberchen"
		buttonAbbrechen = new JButton("abbr.");
		panel.add(buttonAbbrechen);
		ActionListener alAbbrechen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAbbrechen();
			}
		};
		buttonAbbrechen.addActionListener(alAbbrechen);

		// Button "drehen"
		buttonDrehen = new JButton("dreh");
		panel.add(buttonDrehen);
		ActionListener alDrehen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDrehen();
			}
		};
		buttonDrehen.addActionListener(alDrehen);

		// Button "löschen" (cut-Rectangle)
		buttonLoeschen = new JButton("löschen");
		panel.add(buttonLoeschen);
		ActionListener alLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doLoeschenCutRectangle();
			}
		};
		buttonLoeschen.addActionListener(alLoeschen);

		// Name Datum
		labelNameDatum = new JLabel("   ");
		panel.add(labelNameDatum);

		// Alle "Ausschneide"-Buttons auf diable setzen
		disableButtonsAusschneiden();

		return panel;
	}

	// ======================================================
	// disableButtonsAusschneiden()
	//
	// ======================================================
	protected void disableButtonsAusschneiden() {
		buttonAusschneiden.setEnabled(false);
		buttonUebernehmen.setEnabled(false);
		buttonAbbrechen.setEnabled(false);
		buttonDrehen.setEnabled(false);
		buttonLoeschen.setEnabled(false);
	}

	// ============================================================
	// ============================================================
	// InnerClass: RadioListener
	// ============================================================
	// ============================================================
	/** Listens to the radio buttons. */
	class RadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String q = e.getActionCommand().substring(1);
			// System.out.println("--- set QS = " + q);
			picture.meta.setCategory(q);
		}
	}

} // Ende Klasse
