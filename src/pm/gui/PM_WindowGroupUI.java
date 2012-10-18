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
import javax.swing.*;
import javax.swing.event.*;

import pm.PM_AddOn;
import pm.index.*;
import pm.inout.*;
import pm.picture.*;
import pm.search.*;
import pm.utilities.*;
import pm.utilities.PM_Interface.IndexType;

@SuppressWarnings("serial")
public class PM_WindowGroupUI extends PM_WindowBase implements PM_Interface {

	// protected final PM_IndexViewCollection pictureViewCollectionGruppiert;
	// protected final PM_IndexViewCollection pictureViewCollectionThumbnails;
	protected final PM_Index indexViewThumbnails;

	protected PM_Configuration einstellungen;

	// ---------------------------------------------------
	// Private
	// ---------------------------------------------------
	// private PM_FocusPanel panelNorth = null;

	protected PM_WindowGroupData windowGruppierenDaten;

	private JList listIndex1 = null;
	private JList listIndex2 = null;

	private PM_IndicesComboBox index1 = null;
	private JButton buttonDeleteIndex1 = null;
	private JLabel labelInfoIndex1 = null;

	private PM_IndicesComboBox index2 = null;
	private JButton buttonDeleteIndex2 = null;
	private JLabel labelInfoIndex2 = null;

	private ButtonGroup group = null;
	private JRadioButton q1 = null;
	private JRadioButton q2 = null;
	private JRadioButton q3 = null;
	private JRadioButton q4 = null;

	// Datum (Sort-Datum)
	private JComboBox datumJahr = null;
	private JComboBox datumMonat = null;
	private JComboBox datumTag = null;
	private JComboBox datumStunde = null;
	private JComboBox datumMinute = null;

	private JButton buttonGroup = null;
	private JButton buttonUndo = null;

	private boolean stopChangeDatum = true; // ActionListener aufrufen ja/nein

	private String qsChanged = "";

	private JComponent indexListPanel;
	protected boolean addOnIndex2 = false;

	public PM_WindowGroupUI() {
		super(PM_Index.createIndexRight());

		addOnIndex2 = PM_AddOn.getInstance().getAddOnIndex2();

		// pictureViewCollectionThumbnails =
		// windowMain.getPictureViewCollectionThumbnails();
		indexViewThumbnails = windowMain.getIndexViewThumbnails();

		// pictureViewCollectionGruppiert =
		// getIndexView().getIndexViewCollection();
		windowGruppierenDaten = new PM_WindowGroupData(getIndex());
	}

	 
	protected void buildUI() {

		// zusammensetzen (Index-Panel und slider)
		setLayout(new BorderLayout());

		// ------------------------------------------------------------
		// upper Panel
		// ------------------------------------------------------------
		PM_FocusPanel upperPanel = getUpperPanel();
		JScrollPane scUpperPanel = new JScrollPane(upperPanel);
		scUpperPanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scUpperPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// index view (toolbar, thumbs, slider)
		setIndexToolbar();
		getIndex().controller.setAllowGetFromRight(true);
		getIndex().controller.setMarkImportDatum(true); // Sortierdatum
														// besonders markieren
		getIndex().controller.setPopUpLoeschen(true);
		getIndex().controller.setPopUpLoeschenAufheben(true);
		JPanel centerRight = getIndex().getIndexPanel();

		// oben und unten
		add(scUpperPanel, BorderLayout.NORTH);
		add(centerRight, BorderLayout.CENTER);

		setBackgroundUpperPanel(COLOR_BG_PANEL);
		// ------------------------------------------------------
		// Focus-Panels aufbereiten
		// ------------------------------------------------------
		addFocusPanel(upperPanel);
		// addFocusPanel(panelNorth);
		// addFocusPanel(getIndex().getFocusPanel());
		// addFocusPanel(new PM_FocusPanel(null, slider, slider));
		addFocusPanel(new PM_FocusPanel(null, listIndex1, listIndex1));
		addFocusPanel(new PM_FocusPanel(null, listIndex2, listIndex2));

		// --------------------------------------------------------
		// Change Listener f�r message
		// --------------------------------------------------------
		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				setMsg();
			}
		};
		PM_MSG.addChangeListener(msgListener);
		// jetzt werden erstmalig die Tab Titels gesetzt
		setMsg();
	}

	private void setMsg() {

		indexLabel1.setText(PM_MSG.getMsg("index1"));
		indexLabel.setText(PM_MSG.getMsg("index1"));
		indexLabel2.setText(PM_MSG.getMsg("index2"));
		index2Label.setText(PM_MSG.getMsg("index2"));
		qLabel.setText(PM_MSG.getMsg("category14"));
		labelDatum.setText(PM_MSG.getMsg("date"));

		buttonGroup.setText(PM_MSG.getMsg("winGrpButtonGroup"));

	}

	@Override
	public void close() {

		if (indexListPanel instanceof JSplitPane) {
			int l = ((JSplitPane) indexListPanel).getDividerLocation();
			PM_All_InitValues.getInstance().putValueInt(this,
					"index-list-divider", l);
		}
	}

	@Override
	public boolean requestToChange() {
		return true;
	}

	@Override
	public void removeAllPictures() {
		super.removeAllPictures();
		clearUpperPanel();
	}

	@Override
	public void closeAlbum() {
		getIndex().data.removeAllPictures();
	}

	private void clearUpperPanel() {
		index1.setText("");
		index2.setText("");
		labelInfoIndex1.setText("");
		labelInfoIndex2.setText("");
		datumLoeschen();
		resetQsButtons();

	}

	private void setBackgroundUpperPanel(Color color) {
		panelUebernehmen.setBackground(color);
		panelHinweis.setBackground(color);
		panelHinweis2.setBackground(color);
		panelIndex.setBackground(color);
		panelIndex2.setBackground(color);
		qualitaetDatum.setBackground(color);

		index1.setBackground(COLOR_ENABLED);
		buttonDeleteIndex1.setBackground(COLOR_ENABLED);
		index2.setBackground(COLOR_ENABLED);
		buttonDeleteIndex2.setBackground(COLOR_ENABLED);
		q1.setBackground(COLOR_ENABLED);
		q2.setBackground(COLOR_ENABLED);
		q3.setBackground(COLOR_ENABLED);
		q4.setBackground(COLOR_ENABLED);
	}

	private JPanel panelUebernehmen = null;
	private JPanel panelHinweis = null;
	private JPanel panelIndex = null;
	private JPanel panelHinweis2 = null;
	private JPanel panelIndex2 = null;
	private JPanel qualitaetDatum = null;

	private JLabel indexLabel1;
	private JLabel indexLabel;
	private JLabel indexLabel2;
	private JLabel index2Label;
	private JLabel qLabel;
	private JLabel labelDatum;

	private PM_FocusPanel getUpperPanel() {
		PM_FocusPanel panel = new PM_FocusPanel() {
			public void setBackgroundColor(Color color) {
				setBackgroundUpperPanel(color);
			}
		};
		int columsIndexComboBox = 40;
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panelUebernehmen = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelHinweis = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelIndex = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelHinweis2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelIndex2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// Toolbar
		JToolBar toolBarUebernehmen = new JToolBar();
		toolBarUebernehmen.setFloatable(false);
		panelUebernehmen.add(toolBarUebernehmen);

		// ----------------------------------------------------
		// Index-1
		// ----------------------------------------------------
		indexLabel1 = new JLabel("Index 1");
		panelHinweis.add(indexLabel1);
		// Button "alle Indizes loeschen"
		buttonDeleteIndex1 = PM_Utils.getJButonKlein(ICON_DELETE); // new
		// JButton("L�schen");
		panelHinweis.add(buttonDeleteIndex1);
		ActionListener alIndexLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggelTextInIndexLabel(labelInfoIndex1);
			}
		};
		buttonDeleteIndex1.addActionListener(alIndexLoeschen);

		labelInfoIndex1 = new JLabel(" ");
		panelHinweis.add(labelInfoIndex1);
		labelInfoIndex1.setForeground(Color.BLACK);

		indexLabel = new JLabel("Index 1");
		panelIndex.add(indexLabel);
		index1 = new PM_IndicesComboBox(IndexType.INDEX_1);
		index1.setColumns(columsIndexComboBox);
		index1.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				valuesChanged(true);
			}
		});
		panelIndex.add(index1);

		// ----------------------------------------------------
		// Index-2
		// ----------------------------------------------------
		indexLabel2 = new JLabel("Index 2");
		panelHinweis2.add(indexLabel2);
		// Button "alle Indizes loeschen"
		buttonDeleteIndex2 = PM_Utils.getJButonKlein(ICON_DELETE); // new
		// JButton("L�schen");
		panelHinweis2.add(buttonDeleteIndex2);
		ActionListener alIndex2Loeschen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggelTextInIndexLabel(labelInfoIndex2);
			}
		};
		buttonDeleteIndex2.addActionListener(alIndex2Loeschen);

		labelInfoIndex2 = new JLabel(" ");
		panelHinweis2.add(labelInfoIndex2);
		labelInfoIndex2.setForeground(Color.BLACK);

		index2Label = new JLabel("Index 2");
		panelIndex2.add(index2Label);
		index2 = new PM_IndicesComboBox(IndexType.INDEX_2);
		index2.setColumns(columsIndexComboBox);
		index2.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				valuesChanged(true);
			}
		});
		panelIndex2.add(index2);

		// ----------------------------------------------------------
		// Reihe Qualitaet und Datum
		// ----------------------------------------------------------
		qualitaetDatum = new JPanel();
		qualitaetDatum.setLayout(new FlowLayout(FlowLayout.LEFT));
		qLabel = new JLabel("Kategorie");
		qualitaetDatum.add(qLabel);
		q1 = new JRadioButton();
		q2 = new JRadioButton();
		q3 = new JRadioButton();
		q4 = new JRadioButton();
		q1.setActionCommand("q1");
		q2.setActionCommand("q2");
		q3.setActionCommand("q3");
		q4.setActionCommand("q4");
		group = new ButtonGroup();
		group.add(q1);
		group.add(q2);
		group.add(q3);
		group.add(q4);
		qualitaetDatum.add(q1);
		qualitaetDatum.add(q2);
		qualitaetDatum.add(q3);
		qualitaetDatum.add(q4);

		MyRadioListener rl = new MyRadioListener();
		q1.addActionListener(rl);
		q2.addActionListener(rl);
		q3.addActionListener(rl);
		q4.addActionListener(rl);

		// --- Datum -----

		JPanel datPanel = new JPanel();
		datPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		labelDatum = new JLabel("Datum:");
		datPanel.add(labelDatum);

		// Sortierdatum (OHNE Sekunden)
		Vector j = PM_Utils.getJahre(true);
		// j.add(0, "1970");
		datumJahr = new JComboBox(j);
		datPanel.add(datumJahr);
		datPanel.add(new JLabel("."));
		datumMonat = new JComboBox(PM_Utils.getMonate(true));
		datPanel.add(datumMonat);
		datPanel.add(new JLabel("."));
		datumTag = new JComboBox(PM_Utils.getTage(true));
		datPanel.add(datumTag);
		datPanel.add(new JLabel("/"));
		datumStunde = new JComboBox(PM_Utils.getStunden(true));
		datPanel.add(datumStunde);
		datPanel.add(new JLabel(":"));
		datumMinute = new JComboBox(PM_Utils.getMinuten(true));
		datPanel.add(datumMinute);
		// datPanel.add(new JLabel(":"));
		// datumSekunde = new JComboBox(PM_Utils.getSekunden(false));
		// datPanel.add(datumSekunde);

		datumLoeschen();

		stopChangeDatum = true;

		ActionListener alDatum = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (stopChangeDatum)
					return; // Nicht uebernehmen
				// es wurde ein Datum ausgew�hlt.
				// Buttons ("�bernahme" ...) erst wenn Datum vollst�ndig auf
				// enable setzen

				String jj = ((String) datumJahr.getSelectedItem()).trim();
				String mm = ((String) datumMonat.getSelectedItem()).trim();
				String tt = ((String) datumTag.getSelectedItem()).trim();
				String hh = ((String) datumStunde.getSelectedItem()).trim();
				String min = ((String) datumMinute.getSelectedItem()).trim();
				if (jj.length() == 0 || mm.length() == 0 || tt.length() == 0
						|| hh.length() == 0 || min.length() == 0)
					return; // Datum
				// nicht
				// vollst�ndig
				// eingetragen

				// Das Datum ist vollst�ndig eingetragen
				valuesChanged(true);

			}
		};
		datumTag.addActionListener(alDatum);
		datumMonat.addActionListener(alDatum);
		datumJahr.addActionListener(alDatum);
		datumStunde.addActionListener(alDatum);
		datumMinute.addActionListener(alDatum);
		// datumSekunde.addActionListener(alDatum);

		qualitaetDatum.add(datPanel);

		// ---------------------------------------------------
		// alles zusammenbasteln
		// ---------------------------------------------------
		panel.add(panelUebernehmen);
		panel.add(panelHinweis);
		panel.add(panelIndex);

		if (addOnIndex2) {
			panel.add(panelHinweis2);
			panel.add(panelIndex2);
		}

		panel.add(qualitaetDatum);

		// --------------------------------
		// Focus Liste aufbereiten
		// ---------------------------------
		List<Component> zeile1 = new ArrayList<Component>();
		zeile1.add(buttonDeleteIndex1);

		List<Component> zeile2 = new ArrayList<Component>();
		zeile2.add(index1);

		List<Component> zeile3 = new ArrayList<Component>();
		zeile3.add(buttonDeleteIndex2);

		List<Component> zeile4 = new ArrayList<Component>();
		zeile4.add(index2);

		List<Component> zeile5 = new ArrayList<Component>();
		zeile5.add(q1);
		zeile5.add(q2);
		zeile5.add(q3);
		zeile5.add(q4);
		zeile5.add(datumJahr);
		zeile5.add(datumMonat);
		zeile5.add(datumTag);
		zeile5.add(datumStunde);
		zeile5.add(datumMinute);

		List<List> focusList = new ArrayList<List>();
		focusList.add(zeile1);
		focusList.add(zeile2);
		focusList.add(zeile3);
		focusList.add(zeile4);
		focusList.add(zeile5);

		panel.setFocusListe(focusList);
		panel.setLastFocus(index1); // default
		panel.setContainer(panel);

		return panel;
	}

	private void setIndexToolbar() {

		JPanel panel = getIndex().getIndexToolbar(); // the default toolbar

		// Button "undo"
		buttonUndo = PM_Utils.getJButon(ICON_UNDO); // new
													// JButton("R�ckg�ngig");
		panel.add(buttonUndo);
		ActionListener alUndo = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUndo();
			}
		};
		buttonUndo.addActionListener(alUndo);
		// Button "alle Gruppieren"
		buttonGroup = new JButton("Gruppieren");
		panel.add(buttonGroup);
		ActionListener alMetaUebernehmen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getIndex().controller.sizeDargestellt() == 0)
					return;
				doGruppieren();
			}
		};
		buttonGroup.addActionListener(alMetaUebernehmen);
	}

	private void toggelTextInIndexLabel(JLabel label) {

		String txt = label.getText();
		if (txt
				.equals(PM_MSG.getMsg("winGrpIndNotTouch") /* LABEL_UNGLEICHE_UEBERNEHMEN */)) {
			label
					.setText(PM_MSG.getMsg("winGrpIndDelete") /* LABEL_UNGLEICHE_LOESCHEN */);
			label.setForeground(COLOR_WARNING);
		}
		if (txt
				.equals(PM_MSG.getMsg("winGrpIndDelete") /* LABEL_UNGLEICHE_LOESCHEN */)) {

			label
					.setText(PM_MSG.getMsg("winGrpIndNotTouch") /* LABEL_UNGLEICHE_UEBERNEHMEN */);
			label.setForeground(Color.GREEN);
		}
	}

	private void setTextInIndexLabel(boolean ungleiche, JLabel label) {

		if (ungleiche) {
			// es gibt ungleiche Indizes, die �bernommen werden
			label
					.setText(PM_MSG.getMsg("winGrpIndNotTouch") /* LABEL_UNGLEICHE_UEBERNEHMEN */);
			label.setForeground(Color.GREEN);

		} else {
			// Es gibt ungleiche (und evtl. gleiche)
//			label
//					.setText(PM_MSG.getMsg("winGrpIndNo") /* LABEL_UNGLEICHE_KEINE */);
			label.setText("");
			label.setForeground(Color.GREEN);
		}
	}

	private void datumLoeschen() {

		stopChangeDatum = true; // ActionListener NICHT aufrufen

		datumJahr.setSelectedIndex(0);
		datumMonat.setSelectedIndex(0);
		datumTag.setSelectedIndex(0);
		datumStunde.setSelectedIndex(0);
		datumMinute.setSelectedIndex(0);
		// datumSekunde.setSelectedIndex(0);

		stopChangeDatum = false; // ActionListener wieder aufrufen
	}

	private void setQsButtons(int qs) {
		switch (qs) {
		case QS_1:
			q1.setSelected(true);
			break;
		case QS_2:
			q2.setSelected(true);
			break;
		case QS_3:
			q3.setSelected(true);
			break;
		case QS_4:
			q4.setSelected(true);
			break;
		default:
			resetQsButtons();
		}

	}

	private void resetQsButtons() {

		// hier sollen alle selections geloescht werden.
		// Mit qx.setSelected(false) funktioniert das nicht.
		// Hier diese Loesung ist vielleicht etwas komisch, funktioniert aber
		// !!!
		if (q1.isSelected()) {
			group.remove(q1);
			q1.setSelected(false);
			group.add(q1);
		}
		if (q2.isSelected()) {
			group.remove(q2);
			q2.setSelected(false);
			group.add(q2);
		}
		if (q3.isSelected()) {
			group.remove(q3);
			q3.setSelected(false);
			group.add(q3);
		}
		if (q4.isSelected()) {
			group.remove(q4);
			q4.setSelected(false);
			group.add(q4);
		}
	}

	private void valuesChanged(boolean valuesChanged) {

		if (getIndex().controller.sizeDargestellt() == 0) {
			return;
		}

		// this.valuesChanged = valuesChanged;
		if (valuesChanged) {
			buttonGroup.setBackground(COLOR_WARNING);
			buttonUndo.setBackground(COLOR_WARNING);
		} else {
			buttonGroup.setBackground(COLOR_BG_PANEL);
			buttonUndo.setBackground(COLOR_BG_PANEL);
		}

	}

	protected int getQS() {
		// it is really a hack !!!!!!

		boolean q1 = false;
		boolean q2 = false;
		boolean q3 = false;
		boolean q4 = false;
		List<PM_Picture> pictures = getIndex().controller
				.getPictureListDisplayed();
		for (PM_Picture picture : pictures) {
			switch (picture.meta.getCategory()) {
			case QS_1:
				q1 = true;
				break;
			case QS_2:
				q2 = true;
				break;
			case QS_3:
				q3 = true;
				break;
			case QS_4:
				q4 = true;
				break;
			}
		}

		if (q1 && (q2 || q3 || q4))
			return QS_UNBEKANNT;
		if (q2 && (q3 || q4))
			return QS_UNBEKANNT;
		if (q3 && q4)
			return QS_UNBEKANNT;

		if (q1)
			return QS_1;
		if (q2)
			return QS_2;
		if (q3)
			return QS_3;
		if (q4)
			return QS_4;

		return QS_UNBEKANNT;
	}

	protected String getDatumMitPruefen() {
		// return: "0": kein Datum eingegeben
		// "12:03:2005 12:33:00" gueltiges Datum
		// "-1" Fehlerhaftes Datum (Fehlermeldung wurde ausgegeben)

		String jj = ((String) datumJahr.getSelectedItem()).trim();
		String mm = ((String) datumMonat.getSelectedItem()).trim();
		String tt = ((String) datumTag.getSelectedItem()).trim();
		String hh = ((String) datumStunde.getSelectedItem()).trim();
		String min = ((String) datumMinute.getSelectedItem()).trim();

		// pr�fen, ob datum eingetragen
		if (jj.length() == 0 && mm.length() == 0 && tt.length() == 0
				&& hh.length() == 0 && min.length() == 0)
			return "0"; // kein
		// Datum
		// eingetragen

		// Pr�fen, ob vollst�ndig eingetragen (Fehler)
		if (jj.length() == 0 || mm.length() == 0 || tt.length() == 0
				|| hh.length() == 0 || min.length() == 0) {
			// Fehler: Meldung und return -1
			JOptionPane
					.showConfirmDialog(
							this,
							"Datum unvollst�ndig \nJahr, Monat, Tag, Minuten und Sekunden M�SSEN ausgew�hlt werden.",
							"Datum falsch", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
			return "-1"; // Fehler
		}

		// Datum O.K.
		return tt + "." + mm + "." + jj + " " + hh + ":" + min + ":00";

	};

	protected void doUndo() {
		// overrides
	}

	protected void doGruppieren() {
		// overrides
	}

	 
	protected void setValuesInUpperPanel() {
		// clear upper panel if no valid pictures are displayed
		if (!windowGruppierenDaten.hasThumbs()) {
			clearUpperPanel();
			return;
		}
		// get the new values and set them in the upper panel
		windowGruppierenDaten.setValues(); // get them
		// indices 1
		String indices1 = PM_Utils.sortedSetToString(windowGruppierenDaten
				.getGleicheIndizes1());
		index1.setText(indices1);
		setTextInIndexLabel(windowGruppierenDaten.hasUngleicheIndizes1(),
				labelInfoIndex1);		
		// indices 2
		index2.setText(PM_Utils.sortedSetToString(windowGruppierenDaten
				.getGleicheIndizes2()));
		setTextInIndexLabel(windowGruppierenDaten.hasUngleicheIndizes2(),
				labelInfoIndex2);
		// categorie
		setQsButtons(windowGruppierenDaten.getQs());
		// date
		datumLoeschen();

		valuesChanged(false);

	}

	protected String getIndex_1() {
		return index1.getText();
	}

	protected String getIndex_2() {
		return index2.getText();
	}

	protected String getQsChanged() {
		return qsChanged;
	}

	protected boolean indizes1Loeschen() {
		return labelInfoIndex1.getText().equals(
				PM_MSG.getMsg("winGrpIndDelete") /* LABEL_UNGLEICHE_LOESCHEN */);
	}

	protected boolean indizes2Loeschen() {
		return labelInfoIndex2.getText().equals(
				PM_MSG.getMsg("winGrpIndDelete") /* LABEL_UNGLEICHE_LOESCHEN */);
	}

	// ============================================================
	// ============================================================
	// InnerClass: MyRadioListener
	// ============================================================
	// ============================================================
	/** Listens to the radio buttons. */
	private class MyRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// if (picture == null) return;

			String q = e.getActionCommand().substring(1);
			// System.out.println("--- click RadiButton = " + q);
			// buttonUndo.setEnabled(true);
			// buttonUebernehmen.setEnabled(true);
			qsChanged = q;
			valuesChanged(true);
		}
	}

}
