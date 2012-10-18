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
 *  Dialag für Backup
 * @author dih
 *
 */
@SuppressWarnings("serial")
public class PM_WindowDialogBase extends JPanel implements PM_Interface {

	 
	private JProgressBar progressBar;

	private JButton buttonWeiter = null;
	private JButton buttonAbbrechen = null;
	private JDialog dialog = null;
	private JPanel panel;
	private int anzahlFuerProgressionBar = 0;
	private PM_Listener listener;
	private int nrAufruf = 0;

	// =============================================================
	// Konstruktor
	// =============================================================
	public PM_WindowDialogBase() {
		progressBar = new JProgressBar(1, 1);
	}

	// ======================================================
	// start()
	// ======================================================
	public void start() {

		panel = getDialogPanel();

		dialog.getContentPane().add(panel);
		dialog.pack();

		Thread thread = new MyDialog();
		thread.start();

		dialog.setVisible(true);

	}

	// ========================================================
	// getDialogPanel()
	// ========================================================
	private JPanel getDialogPanel() {

		JPanel panel = new JPanel();
		dialog = new JDialog(new JFrame(), true);
		dialog.setLocationRelativeTo(null); // in die Mitte des Schirms
		dialog.setUndecorated(true);
		// panel.setPreferredSize(new Dimension(600, 800));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);

		// von abgeleiteter Klasse werden jetzt alle Zeilen aufbereitet
		setZeilen(panel);

		// danach Progression
		panel.add(progressBar);
		progressBar.setValue(0);

		// Und nun noch die Buttons Weiter und abbrechen
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));

		buttonWeiter = new JButton(PM_MSG.getMsg("continue"));
		buttons.add(buttonWeiter);
		ActionListener alWeiter = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				anzahlFuerProgressionBar = 0;
				progressBar.setValue(0);
				Thread thread = new MyDialog();
				thread.start();
			}
		};
		buttonWeiter.addActionListener(alWeiter);

		buttonAbbrechen = new JButton(PM_MSG.getMsg("exit"));
		buttons.add(buttonAbbrechen);
		ActionListener alAbbrechen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		};
		buttonAbbrechen.addActionListener(alAbbrechen);

		panel.add(buttons);

		JPanel p = new JPanel();
		p.add(panel, BorderLayout.NORTH);

		return p;

	}

	// =================================================================
	// alle folgenden Methoden werden in der abgeleiteten Klasse überladen
	// ===================================================================
	protected void setZeilen(JPanel panel) {
		// wird überladen
	}

	protected void goDialog(JDialog dialog,int nrAufruf, PM_Listener listener) {
		// wird überladen
		return; //  weiter über "weiter" Button

	}

	protected void setAnzahl(int max) {
		progressBar.setMaximum(max);
		progressBar.setValue(1);
	}

	// ==================================================================
	//
	//
	// ==================================================================
	public class MyDialog extends Thread {

		/**
		 * Konstruktor
		 */
		public MyDialog() {
			anzahlFuerProgressionBar = 0;
		}

		/**
		 * run
		 */
		public void run() {
			listener = new PM_Listener() {
				public void actionPerformed(PM_Action e) {
					int anz = e.getType();
					if (anz > 0) {
						anzahlFuerProgressionBar += anz;
					} else {
						anzahlFuerProgressionBar++;
					}					
					progressBar.setValue(anzahlFuerProgressionBar);
				}
			};

			nrAufruf++;
		    goDialog(dialog,nrAufruf, listener);

		}

	}

	// ==================================================================
	//
	//
	// ==================================================================


	class ZeilenPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private JCheckBox checkbox = null;
		private JTextField zaehler = null;
		private JLabel textLabel = null;

		private Font fontPlain;
		private Font fontBold;

		/**
		 * Konstruktor
		 */
		ZeilenPanel(String text, boolean mitZaehler) {

			// fontPlain und fontBold erstellen
			JLabel l = new JLabel("");
			Font font = l.getFont();
			fontPlain = new Font(font.getName(), Font.PLAIN, font.getSize());
			fontBold = new Font(font.getName(), Font.BOLD, font.getSize());

			setLayout(new FlowLayout(FlowLayout.LEFT));

			checkbox = new JCheckBox();
			checkbox.setEnabled(false);

			zaehler = new JTextField();
			zaehler.setHorizontalAlignment(JTextField.RIGHT);
			zaehler.setEditable(false);
			zaehler.setColumns(4);

			textLabel = new JLabel(text);
			textLabel.setFont(fontPlain);

			add(checkbox);
			if (mitZaehler) {
				add(zaehler);
			}			
			add(textLabel);

		}

		/**
		 * setzen Font PLAIN
		 */
		public void setAnzahl(int anzahl) {
			zaehler.setText(Integer.toString(anzahl));
		}

		public void setAnzahl(int anzahl1, int anzahl2, int anzahl3) {
			zaehler.setText(Integer.toString(anzahl1) + "/"
					+ Integer.toString(anzahl2) + "/"
					+ Integer.toString(anzahl3));
		}

		/**
		 * Zustand vor start wiederherstellen
		 */
		public void reset() {
			setChecked(false);
			setFontPlain();
		}

		/**
		 * Starten der Bearbeitung
		 */
		public void start() {
			setFontBold();
			setChecked(false);
		}

		/**
		 * Beenden der Bearbeitung
		 */
		public void stop() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			setFontPlain();
			setChecked(true);
		}

		// =========================== PRIVATE ================================
		// =========================== PRIVATE ================================
		// =========================== PRIVATE ================================
		// =========================== PRIVATE ================================
		// =========================== PRIVATE ================================

		/**
		 * setzen Font PLAIN
		 */
		private void setChecked(boolean checked) {
			checkbox.setSelected(checked);
		}

		/**
		 * setzen Font PLAIN
		 */
		private void setFontPlain() {
			textLabel.setFont(fontPlain);
			zaehler.setFont(fontPlain);
		}

		/**
		 * setzen Font PLAIN
		 */
		private void setFontBold() {
			textLabel.setFont(fontBold);
			zaehler.setFont(fontBold);
		}

	} // Ende Klasse ZeilenPanel

}
