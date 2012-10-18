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

 
import pm.utilities.*;
import pm.picture.*;
 
import pm.inout.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
//import java.awt.image.*;
import java.security.*;

/**
 * Dialog-Window (modal) zum Starten eines externen Bildbearbeitungsprogramms
 * 
 * (Es wird nur eine Instanz (beim Starten von PM) erzeugt)
 * 
 * Es werden 2 Fenster (�ber Reiter erreichbar) dargestellt:
 * 
 * Fenster 1: Starten externes Programm Fenster 2: Externes Programm einrichten
 * 
 */
@SuppressWarnings("serial")
public class PM_ModifyExternal extends PM_WindowExtern  implements PM_Interface {

	 
	private JDialog dialog;

 
	private PM_Picture picture = null;

	private PM_PictureExternalModify pictureExternBearbeiten;

	private int qsChanged = 0;

	private boolean qsGeaendert = false;
	
	private JPanel picturePanel;

 

	private JComboBox cbProgrammName = null;
	// Tabelle
	private JTable table = new JTable();
	private Vector<String> header = new Vector<String>();
	private DefaultTableModel tableModel;

	// Sichern Daten vom geladenen Bild um Ver�nderungen festzustellen
	private byte[] messageDigestIn;

	private JTextField bildPfadName = null;
	private JTextField bildGroesse = null;
	private JTextField progPfadName = null;

	 

	private Image imageToDisplay = null;

	private JRadioButton q1 = null;
	private JRadioButton q2 = null;
	private JRadioButton q3 = null;
	private JRadioButton q4 = null;

	private JTabbedPane tabbedPane;
	
	// ===========================================================
	// Konstruktor
	// ===========================================================
	public PM_ModifyExternal(PM_WindowMain windowMain,
			final JDialog dialog) {
		 
		this.dialog = dialog;

		tabbedPane = new JTabbedPane(SwingConstants.TOP);

		// Panel "Bild bearbeiten" ist ein Split-Pane
		picturePanel = getPicturePanel();
		JSplitPane bearbeitenPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, getPanelStarten(), picturePanel);

		PM_WindowExternalSetup externEinrichten = new PM_WindowExternalSetup();
		PM_Listener listenerDarstellung = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// ComboBox aktualisieren
				Object object = e.getObject();
				PM_ExternalProgram extProg = null;
				if (object instanceof PM_ExternalProgram) {
					extProg = (PM_ExternalProgram) e.getObject();
				}
				setzenProgrammNamen(extProg);
			}
		};
		externEinrichten.addChangeListenerExterneProgramme(listenerDarstellung);
// 	tabbedPane.insertTab("Indizes",null, getIndex12(), "", 1);

		tabbedPane.insertTab("Bild bearbeiten", null,  bearbeitenPanel, "", 0);
		tabbedPane.insertTab("Programm einrichten", null,  externEinrichten, "", 1);
		
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		
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
		
		 
		 tabbedPane.setTitleAt(0, PM_MSG.getMsg("modExtModifyPic"));
		 tabbedPane.setTitleAt(1, PM_MSG.getMsg("modExtSetupProgram"));
		 
		 labelPicSize.setText(PM_MSG.getMsg("modExtLabelPicSize"));	
		labelFileName.setText(PM_MSG.getMsg("modExtLabelFileName"));	
		labelProgramName.setText(PM_MSG.getMsg("modExtProgramName"));	
		labelProgramPath.setText(PM_MSG.getMsg("modExtProgramPath"));	
		labelCat.setText(PM_MSG.getMsg("category14"));	
		buttonStart.setText(PM_MSG.getMsg("modExtButtonStart"));	
		buttonDelete.setText(PM_MSG.getMsg("delete"));	
		buttonExit.setText(PM_MSG.getMsg("exit"));	
		buttonLoad.setText(PM_MSG.getMsg("modExtButtonLoad"));	
	 
		header.set(0, PM_MSG.getMsg("modExtTabLoad"));
		header.set(1, PM_MSG.getMsg("modExtTabDate"));
		header.set(2, PM_MSG.getMsg("modExtTabBackUp"));
		 
		 	
		 
		
	}

	// ==========================================================
	// start()
	//
	// Start ein Bild bearbeiten
	//
	// (es existiert nur eine Instanz dieser Klasse, die beim
	// Hochfahren von PM angelegt wird; bei jedem Aufruf von
	// "Bild bearbeiten mit externem Programm"
	// wird lediglich hier start() aufgerufen)
	// ==========================================================
	public boolean start(PM_Picture picture) {
		this.picture = picture;
		pictureExternBearbeiten = new PM_PictureExternalModify(picture);

		// Sichern Daten vom geladenen File (um Ver�nderungen bei Ende
		// festzustellen)
		messageDigestIn = PM_Utils.getMessageDigest(picture.getFileOriginal());

		// setzen Inhalt der Tabelle (alle gesicherten Dateien inkl. der
		// geladenen)
		setzenInhaltTabelle();
		bildPfadName.setText(picture.getFileOriginal().getPath());

		// ComboBox mit Programmnamen versehen
		// String progName = PM_XML_Session.getInstance().getExternesProgramm();
		// PM_XML_ExternesProgramm extProg =
		// PM_XML_MetadatenGlobal.getInstance().getExternesProgramm(progName);
		setzenProgrammNamen(null); // extProg);

		// Kategorie
		qsChanged = picture.meta.getCategory();
		qsGeaendert = false;
		switch (picture.meta.getCategory()) {
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
		return true;
	}

	// ====================================================================================
	// ====================================================================================
	// ====================================================================================
	// ======================= PRIVATE
	// ====================================================
	// ======================= PRIVATE
	// ====================================================
	// ====================================================================================
	// ====================================================================================
	// ====================================================================================
	// ====================================================================================

	// ========================================================
	// setzenProgrammNamen()
	//
	// Setzen alle Programmnamen in der ComboBox
	// ========================================================
	private void setzenProgrammNamen(PM_ExternalProgram extProg) {
		// Alle Programmnamen
		Vector progNamen = PM_All_ExternalPrograms.getInstance()
				.getAlleExternenProgramme();

		cbProgrammName.removeAllItems();
		for (int i = 0; i < progNamen.size(); i++) {
			cbProgrammName.addItem(progNamen.elementAt(i));
		}

		// Eintrag selektieren
		if (extProg != null) {
			cbProgrammName.setSelectedItem(extProg);
		} else {
			if (progNamen.size() > 0)
				cbProgrammName.setSelectedIndex(0);
		}

	}

	// ========================================================
	// setzenInhaltTabelle()
	//
	// setzen Inhalt der Tabelle (alle gesicherten Dateien inkl. der geladenen)
	// ========================================================

	private void setzenInhaltTabelle() {

		Vector<Vector> datenNeu = new Vector<Vector>();
		PM_PictureExternalModify.EinBild[] fileList = pictureExternBearbeiten
				.getFileList();
		for (int i = 0; i < fileList.length; i++) {
			PM_PictureExternalModify.EinBild einBild = fileList[i];
			Vector<String> v = new Vector<String>();
			if (einBild.getGeladen()) {
				v.add(PM_MSG.getMsg("modExtLabelLoad"));
			} else {
				v.add("");
			}
			v.add(einBild.getDatumString());

			// Kennung
			int ext = einBild.getExtension();
			String kennung = "b" + Integer.toString(einBild.getExtension());
			if (ext == 0) {
				kennung +=  "  ("+PM_MSG.getMsg("modExtLabelOriginal")+")"; //        " (Original)";
			}
			v.add(kennung);

			// Zeile hinzufuegen
			datenNeu.add(v);
		}
		
		// Daten in die Tabelle schreiben
		tableModel.setDataVector(datenNeu, header);

		// Zeile selektieren
		int indexGeladen = pictureExternBearbeiten.getIndexGeladen();
		if (indexGeladen >= 0) {
			table.setRowSelectionInterval(indexGeladen, indexGeladen);
		}

		repaint();
	}

	// ========================================================
	// doStarten()
	// Das externe programm wird mit dem Bild aufgerufen
	// ========================================================

	private void doStarten() {

		// das Bild muss geladen sein.
		int indexGeladen = pictureExternBearbeiten.getIndexGeladen();
		if (indexGeladen != table.getSelectedRow()) {
			JOptionPane
					.showConfirmDialog(this, PM_MSG.getMsg("modExtMsgNotLoaded"), "Error" ,//"Bild nicht geladen", "Fehler",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// aus den Session-Daten aktueller Programmname holen
		String progPath = progPfadName.getText();

		// Vor dem Starten die zu beabeitende Datei sichern
		pictureExternBearbeiten.startBearbeitung();

		// Starten
		String bildPath = picture.getFileOriginal().getPath();
		if ( ! PM_Utils.isLinux()) {
			bildPath = "\"" + bildPath + "\"";
		}
		startenProgramm(progPath, bildPath);

	}
	
	// ========================================================
	// doLaden()
	//  
	// 1. aktuell geladenes: - neue Instanz von "EinBild"
	// - rename Origplatz to Sicherung der neuen Instanz
	// 2. zu ladendes Bild: - rename Sicherung to Origplatz
	// ========================================================

	private void doLaden() {
		pictureExternBearbeiten.ladenBild(table.getSelectedRow());
		setzenInhaltTabelle();
	}

	// ========================================================
	// doLoeschen()
	//  
	// ========================================================

	private void doLoeschen() {
		String msg = "";
		if (table.getSelectedRow() == 0) {
			msg = PM_MSG.getMsg("modExtMsgNotDelete");
			JOptionPane
					.showConfirmDialog(
							this,
							msg,
	//						"Die Sicherung des Originalbildes darf nicht gel�scht werden",
							"Fehler", JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// das Bild darf NICHT geladen sein.
		int indexGeladen = pictureExternBearbeiten.getIndexGeladen();
		if (indexGeladen == table.getSelectedRow()) {
			msg = PM_MSG.getMsg("modExtMsgNotLoad");
			JOptionPane.showConfirmDialog(this, msg,
					"Fehler", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		msg = PM_MSG.getMsg("modExtMsgBackUpDel");
		int n = JOptionPane.showConfirmDialog(this, msg,
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		;

		pictureExternBearbeiten.loeschenBild(table.getSelectedRow());
		/*
		 * if ( !pictureExternBearbeiten.loeschenBild(table.getSelectedRow())) {
		 * JOptionPane.showConfirmDialog(this, "remove gescheitert", "Fehler",
		 * JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE); };
		 */
		setzenInhaltTabelle();
	}

	// ========================================================
	// doBeenden()
	//  
	// ========================================================

	private void doBeenden() {

		// -------------------------------------------------------------
		// Pr�fen, ob das geladene Bild auch dargestellt wird.
		// Wenn nein, dann Meldung
		// --------------------------------------------------------------
		int indexGeladen = pictureExternBearbeiten.getIndexGeladen();
		if (indexGeladen != table.getSelectedRow()) {
			String msg = PM_MSG.getMsg("modExtMsgNotLoaded");
			int n = JOptionPane.showConfirmDialog(this,
					msg, //"Dargestelltes Bild ist nicht geladen",
					"", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.CANCEL_OPTION) {
				return;
			}
			;
		}

		// -----------------------------------------------------------------
		// ggf. Kategorie ge�ndert
		//
		// Achtung: auch wenn Bild nicht ge�ndert wurde !!!!
		// ------------------------------------------------------------------
		if (qsGeaendert) {		 
			picture.meta.setCategory( qsChanged );
		}
		
		// ----------------------------------------------------------------
		// wurde das geladene Bild geandert?
		// Wenn nein, dann return.
		// ----------------------------------------------------------------
		byte[] messageDigestOut = PM_Utils.getMessageDigest(picture
				.getFileOriginal());
		if (MessageDigest.isEqual(messageDigestIn, messageDigestOut)) {
			// System.out.println(" Ende Bearbeitung: geladene Datei wurde NICHT
			// veraendert");
			dialog.dispose();
			return;
		}

		// System.out.println(" Ende Bearbeitung: geladene Datei wurde
		// veraendert");

		// -----------------------------------------------------------------------
		// Ja, es wurde ver�ndert
		//
		// Metadaten updaten:
		// - neue Abmessungen
		// - cut und rotate l�schen
		// - Thumbnail neu erzeugen
		// Thumbnails neu zeichnen.
		// In PM_Picture Image l�schen, so dass es gelesen wird
		// ------------------------------------------------------------------------
		// PM_PictureMetadatenX metadaten = picture.getPictureMetadaten();
		picture.meta.setCutRectangle(new Rectangle()); // Cut l�schen
		picture.meta.setRotation(CLOCKWISE_0_DEGREES); // rotate l�schen
		Image image = getImage(picture.getFileOriginal().getPath());
		if (image != null) {
			picture.meta.setImageSize(PM_UtilsGrafik.getImageSize(image));
			// Thumbnail neu erstellen
			File fileThumbnail = PM_Utils.getFileThumbnail(picture
					.getFileOriginal());
			Image imageThumbnail = PM_UtilsGrafik.makeThumbnail(image);
			PM_UtilsGrafik.writeThumbnail(fileThumbnail, imageThumbnail);
			picture.setImageThumbnail(imageThumbnail);
		}

		// setzen Attribut "bearbeitet" in die Metadaten
		if (table.getSelectedRow() == 0) {
			picture.meta.setModified(false);  
		} else {
			picture.meta.setModified(true);  
		}

		// Alle original Images freigeben
		PM_Picture.readImageOriginal(new ArrayList<PM_Picture>(), new ArrayList<PM_Picture>());


		
		dialog.dispose();
	}

	// ========================================================
	// processEnde()
	// 
	// ========================================================
	protected void processEnde() {

		// Ende der Bearbeitung
		boolean bildGeandert = pictureExternBearbeiten.endeBearbeitung();
		if (!bildGeandert) {
			return; // Bild wurde nicht veraendert
		}

		// ---------------------------------------------------------------------
		// Bild wurde veraendert.
		// Geladenes Bild selektieren und darstellen
		// --------------------------------------------------------------------
		bildDarstellen(pictureExternBearbeiten.getIndexGeladen());
		// Tabelle neu aufbereiten
		setzenInhaltTabelle();
	}

	// ========================================================
	// getPanelStarten()
	// ========================================================
	private JLabel labelProgramName;
	private JLabel labelProgramPath;
	private JLabel labelCat;
	private JLabel labelFileName;
	private JLabel labelPicSize;
	private JButton buttonStart;
	private JButton buttonDelete;
	private JButton buttonExit;
	private JButton buttonLoad;
	private JScrollPane getPanelStarten() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		// panel.setAlignmentY(0);

		// ---------------------------------------------------------
		// Tabelle der Sicherungen
		// ---------------------------------------------------------
		header.add("geladen");
		header.add("Datum");
		header.add("Sicherung");

		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.GREEN);
		table.setRowSelectionAllowed(true);
		JScrollPane tabelleScrollPane = new JScrollPane(table);
		tabelleScrollPane.setPreferredSize(new Dimension(0, 250));
		tableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		table.setModel(tableModel);
		tableModel.setDataVector(new Vector(), header);

		TableColumn column = null;
		column = table.getColumnModel().getColumn(0);
		column.setMaxWidth(200);
		column.setPreferredWidth(80);
		column = table.getColumnModel().getColumn(1);
		column.setMaxWidth(200);
		column.setPreferredWidth(160);

		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (imageToDisplay != null)
					imageToDisplay.flush();
				if (picture == null)
					return; // noch nicht gestartet
				// Holen ROW �ber ListSelectionModel
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (e.getValueIsAdjusting())
					return; // nicht der letzte Aufruf
				if (lsm.isSelectionEmpty())
					return; // keine Selection
				int row = lsm.getMinSelectionIndex();
				if (!pictureExternBearbeiten.hasEinBildAt(row))
					return; // Fehler

				// Bild rechts darstellen
				bildDarstellen(row);
			}
		});

		// ---------------------------------------------------------
		// Zeile Combobox mit Programmname
		// ---------------------------------------------------------
		JPanel programmNamePanel = new JPanel();
		programmNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelProgramName = new JLabel("Programmname: ");
		programmNamePanel.add(labelProgramName);
		cbProgrammName = new JComboBox();
		programmNamePanel.add(cbProgrammName);
		ActionListener alProgName = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object o = cbProgrammName.getSelectedItem();
				if (!(o instanceof PM_ExternalProgram)) {
					return;
				}
				PM_ExternalProgram extProg = (PM_ExternalProgram) o;
				progPfadName.setText(extProg.getPath());
				 
				// PM_XML_Session.getInstance().setExternesProgramm(extProg.getName());
			}
		};
		cbProgrammName.addActionListener(alProgName);

		// ---------------------------------------------------------
		// Zeile Programmpfad
		// ---------------------------------------------------------
		JPanel progPfadPanel = new JPanel();
		progPfadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelProgramPath = new JLabel("Programmpfad: ");
		progPfadPanel.add(labelProgramPath);
		progPfadName = new JTextField("    ");
		progPfadName.setColumns(30);
		progPfadPanel.add(progPfadName);

		// ---------------------------------------------------------
		// Zeile mit Bild-Originalpfad
		// ---------------------------------------------------------
		JPanel pfadPanel = new JPanel();
		pfadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelFileName = new JLabel("Dateiname: ");
		pfadPanel.add(labelFileName);
		bildPfadName = new JTextField("    ");
		bildPfadName.setColumns(30);
		pfadPanel.add(bildPfadName);

		// ---------------------------------------------------------
		// Image Groesse
		// ---------------------------------------------------------
		JPanel groessePanel = new JPanel();
		groessePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// groessePanel.setLayout(new BoxLayout(groessePanel,
		// BoxLayout.X_AXIS));
		labelPicSize = new JLabel("Bildgroe�e: ");
		groessePanel.add(labelPicSize);
		bildGroesse = new JTextField("    ");
		// bildGroesse.setColumns(30);
		groessePanel.add(bildGroesse);

		// ---------------------------------------------------------
		// (1) Zeile mit Buttons
		// ---------------------------------------------------------
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonStart = new JButton("Bild bearbeiten");
		buttonPanel.add(buttonStart);
		ActionListener alStarten = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (waitForProcessEnde())
					return;
				doStarten();
			}
		};
		buttonStart.addActionListener(alStarten);

		buttonLoad = new JButton("Sicherung laden");
		buttonPanel.add(buttonLoad);
		ActionListener alLaden = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (waitForProcessEnde())
					return;
				doLaden();
			}
		};
		buttonLoad.addActionListener(alLaden);

		buttonDelete = new JButton("Sicherung l�schen");
		buttonPanel.add(buttonDelete);
		ActionListener alLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (waitForProcessEnde())
					return;
				doLoeschen();
			}
		};
		buttonDelete.addActionListener(alLoeschen);

		// ---------------------------------------------------------
		// (2) Zeile mit Buttons
		// ---------------------------------------------------------
		JPanel buttonBeendenPanel = new JPanel();
		buttonBeendenPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		buttonExit = new JButton("beenden");
		buttonBeendenPanel.add(buttonExit);
		ActionListener alBeenden = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (waitForProcessEnde())
					return;
				doBeenden();
			}
		};
		buttonExit.addActionListener(alBeenden);

		// ---------------------------------------------------------
		// (3) Zeile mit Buttons (Kategorie)
		// ---------------------------------------------------------

		JPanel qualitaet = new JPanel();
		qualitaet.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelCat = new JLabel("Kategorie");
		qualitaet.add(labelCat);
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

		MyRadioListener rl = new MyRadioListener();
		q1.addActionListener(rl);
		q2.addActionListener(rl);
		q3.addActionListener(rl);
		q4.addActionListener(rl);

		panel.add(tabelleScrollPane);
		panel.add(programmNamePanel);
		panel.add(progPfadPanel);
		panel.add(pfadPanel);
		panel.add(groessePanel);
		panel.add(buttonPanel);
		panel.add(buttonBeendenPanel);
		panel.add(qualitaet);

		JPanel p = new JPanel(new BorderLayout());
		p.add(panel, BorderLayout.NORTH);

		// das Panel wird ein Scroll Pane
		JScrollPane panelScrollPane = new JScrollPane(p);

		return panelScrollPane;

	}

	// ========================================================
	// bildDarstellen()
	//
	// Bild lesen und darstellen (inklusive paint)
	// ========================================================
	private void bildDarstellen(int index) {

		PM_PictureExternalModify.EinBild einBild = pictureExternBearbeiten
				.getEinBildAt(index);
		File fileDarstellen = einBild.getFile();
		if (!fileDarstellen.exists()) {
			fileDarstellen = picture.getFileOriginal();
		}
		if (!fileDarstellen.exists()) {
			return; // Datei nicht vorhanden
		}
		// Jetzt kann das Bild dargestellt werden
		imageToDisplay = getImage(fileDarstellen.getPath());

		// Groesse anzeigen
		 
		String groesse = Integer.toString(imageToDisplay.getWidth(null)) + "x"
				+ Integer.toString(imageToDisplay.getHeight(null)) + " Pixel";
		bildGroesse.setText(groesse);

		picturePanel.repaint();

	}

	// ========================================================
	// getImage()
	// ========================================================
	private Image getImage(String path) {
		Image image = null;
		try {
			image = Toolkit.getDefaultToolkit().getImage(path);
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(image, 0);
			mediaTracker.waitForID(0);

		} catch (InterruptedException ee) {
			
			
			
			 
			return null;
		}

		return image;

	}

	// ========================================================
	// getPanelBilder()
	// ========================================================
	private JPanel getPicturePanel() {
		JPanel panel = new JPanel() {

			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (imageToDisplay != null) {

					// Dimension imageDim =
					// PM_UtilsGrafik.getImageSize(imageToDisplay);

					// Rectangle maxBild = new
					// Rectangle(picturePanel.getWidth(),
					// picturePanel.getHeight());
					int b = picturePanel.getWidth();
					double ratio = PM_UtilsGrafik.getRatio(imageToDisplay);
					int h = (int) (b / ratio);
					// maxBild = PM_UtilsGrafik.rectangleEinpassen(maxBild, new
					// Rectangle(imageDim));

					// ----------- Bild zeichnen ------------------
					g.drawImage(imageToDisplay,
					// destination
							0, 0, b, // maxBild.width,
							h, // maxBild.height,
							// source
							// cutRec.x,
							// cutRec.y,
							// cutRec.x + cutRec.width,
							// cutRec.y + cutRec.height,
							this);
				} // if imageOriginal
			} // paintComponent
		};

//		JScrollPane panelScrollPane = new JScrollPane(panel);

		return panel;
	}

	 
	// ============================================================
	// ============================================================
	// InnerClass: MyRadioListener
	// ============================================================
	// ============================================================
	/** Listens to the radio buttons. */
	class MyRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (picture == null)
				return;

			String q = e.getActionCommand().substring(1);
			// System.out.println("--- set QS = " + q);
			qsChanged = PM_Utils.stringToInt(q);
			qsGeaendert = true;

		}
	}

}
