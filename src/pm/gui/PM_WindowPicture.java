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



 
import pm.PM_AddOn;
import pm.utilities.*;
import pm.index.*;
import pm.picture.*;
 
import pm.search.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

@SuppressWarnings("serial")
public class PM_WindowPicture extends PM_WindowBase implements PM_Interface {

	 
	private Image image = null;
	private final PM_Index   indexViewThumbnails;
	private JScrollPane scrollMetaPanel = null;
	private PM_FocusPanel metaPanel = null;
	private PM_FocusPanel belowPanel = null;
	private JPanel windowPicture = null;
	private JTextField datumImport = null;
	private PM_IndicesComboBox index1 = null;
	private PM_IndicesComboBox index2 = null;
	private JTextField bemerkungen = null;	 
	private PM_Picture picture = null;
	private JRadioButton q1 = null;
	private JRadioButton q2 = null;
	private JRadioButton q3 = null;
	private JRadioButton q4 = null;
	private JCheckBox checkBoxSpiegeln = null; 
	private JButton buttonUndo = null; 
	private JComboBox datumJahr = null;
	private JComboBox datumMonat = null;
	private JComboBox datumTag = null;
	private JComboBox datumStunde = null;
	private JComboBox datumMinute = null;
	private JComboBox datumSekunde = null;
	private boolean stopChangeDatum = true; // ActionListener aufrufen ja/nein
	private JLabel labelAbweichendesDatum = null;
	private PM_Listener metadatenChangeListener;
	private boolean addOnIndex2 = false;
	private boolean changed = false;
	 
	public PM_WindowPicture( ) {
		super(null);
		addOnIndex2 = PM_AddOn.getInstance().getAddOnIndex2();
		indexViewThumbnails = windowMain.getIndexViewThumbnails();	 

		metaPanel = getMetaPanel();
		scrollMetaPanel = new JScrollPane(metaPanel);
		scrollMetaPanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollMetaPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		
		belowPanel = getBelowPanel();
		JScrollPane scrollBelowPanel = new JScrollPane(belowPanel);
		scrollBelowPanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollBelowPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// zusammensetzen (Index-Panel und slider)
		setLayout(new BorderLayout());

	    
		add(scrollMetaPanel, BorderLayout.NORTH);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(scrollBelowPanel, BorderLayout.SOUTH);

		// Change Listerner fuer Metadatenanederungen hier erzeugen
		metadatenChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// die Metadaten wurden geaneder
				if (picture == null) {
					return; // hier wird gar nichts dargestellt
				}
				if (e.getObject() instanceof PM_Picture) {
					PM_Picture pic = (PM_Picture) e.getObject();
					// Pruefen, ob Aenderungen das hier angezeigte Bild
					// betreffen

					if (picture != pic) {
						return; // Action nicht relavant
					}
					// loeschen, wenn LOESCHEN_INDEX_FILE
					if (e.getType() == PM_PictureMetadaten.LOESCHEN_INDEX_FILE) {
						doClearBild();
					}

				}
			} // actionPerformed
		}; // PM_Listener
		PM_PictureMetadaten.addChangeListener(metadatenChangeListener);

		setAktiverFocus(index1);
		addFocusPanel(metaPanel);		 
		addFocusPanel(belowPanel);
		setBackgroundMetaPanel(COLOR_BG_PANEL);		
		windowZeichnen();

		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {				 
				setMsg();
			}			
		};		
		PM_MSG.addChangeListener(msgListener);		
		
		setMsg();
	}
	
	
	private void setMsg() {
		
		labelSortDatum.setText(PM_MSG.getMsg("winPicLabelDate"));
		
		 indexLabel_1.setText(PM_MSG.getMsg("index1"));
		 indexLabel_2.setText(PM_MSG.getMsg("index2"));
		 bemerkungenLabel.setText(PM_MSG.getMsg("winPicLabRemark"));
		 qLabel.setText(PM_MSG.getMsg("category14"));
		 drehenLabel.setText(PM_MSG.getMsg("winPicLabRotate"));
		 spiegelnLabel.setText(PM_MSG.getMsg("winPicLabMirror"));
		
		buttonCut.setText(PM_MSG.getMsg("winPicButtonCut"));	
		buttonClear.setText(PM_MSG.getMsg("winPicButtonClear"));
		buttonUndo.setText(PM_MSG.getMsg("winPicButtonUndo"));
		 
	}

 
	@Override
	public PM_Picture getPictureSelected() {
		return picture;
	}  
	
	
 
	@Override
	public boolean appendPicture(PM_Picture  picture ) {
		requestToChange();
		this.picture = picture;	 
		image = picture.getImageThumbnail(false);
		doSetGuiFromMetadaten();
		windowZeichnen();
		 
		return true;
	}

 
	@Override
	public void close() {
		requestToChange();				
	}
	
	@Override
	public void rereadAllThumbs() {
		requestToChange();		
	}
	
	private String index1Changed = "";
	private String index2Changed = "";
	private String bemerkungenChanged = "";
	private int qsChanged = 0;
	private Date datumChanged = new Date();
	private boolean spiegelnChanged = false;
	private int rotationChanged = CLOCKWISE_0_DEGREES;

	public boolean requestToChange() {
		
		if (picture == null) {
			return true;
		}
		// if (buttonUndo.isEnabled() == false) return true;
		if (istGeaendert() == false) {
			return true;
		}
		
		// make permanent all the changes 
		picture.meta.setIndex1(index1Changed);
		picture.meta.setIndex2(index2Changed);
		picture.meta.setRemarks(bemerkungenChanged);
		if (picture.meta.getDateCurrent().getTime() != datumChanged.getTime()) {
		    picture.meta.setDateCurrent(datumChanged);
		}	  
		picture.meta.setCategory(qsChanged);
		picture.meta.setRotation(rotationChanged);
		picture.meta.setMirror(spiegelnChanged);		 
		
		// all changes are made.
		
		// now display the changes on the left side
		windowMain.rereadPictureViewThumbnail(picture);
	
		// delete the changes
		index1Changed = "";
		index2Changed = "";
		bemerkungenChanged = "";
		qsChanged = 0;
		rotationChanged = CLOCKWISE_0_DEGREES;
		datumChanged = new Date();
		spiegelnChanged = false;

		
		
		
		geaendert(false);

		return true;
	}
 

	// =================================================================
	// ================================================================
	//
	// P R I V A T E
	//
	// =================================================================
	// =================================================================

	private void windowZeichnen() {
		
		windowPicture.repaint();
		scrollMetaPanel.repaint();
		metaPanel.repaint();
	}

	// ==========================================================
	// getBelowPanel
	//
	// (Unten: slider und einige Buttons)
	// ==========================================================
	private JButton buttonCut ;
	private JButton buttonClear ;
 
	private PM_FocusPanel getBelowPanel() {
		PM_FocusPanel panel = new PM_FocusPanel();
		panel.setBackground(Color.yellow);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Button "Original"
		buttonCut = new JButton("Ausschneiden");
		panel.add(buttonCut);
		ActionListener alMetaOrig = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (picture  == null) {
					return;
				}
				// vorher Aenderungen in die Metadaten uebertragen
				requestToChange();
				// jetzt Origianlbile darstellen
				windowMain.doBildZeigenOriginal(picture ,
						indexViewThumbnails.controller.getPictureListDisplayed() );
				windowPicture.repaint();
				metaPanel.repaint();
			}
		};
		buttonCut.addActionListener(alMetaOrig);

		// Button "Clear"
		buttonClear = new JButton("Clear");
		panel.add(buttonClear);
		ActionListener alClear = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearBild();
			}
		};
		buttonClear.addActionListener(alClear);

		// Button ""
		buttonUndo = new JButton("R�ckg�ngig");
		geaendert(false);
		panel.add(buttonUndo);
		ActionListener alUndo = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUndo();
			}
		};
		buttonUndo.addActionListener(alUndo);

		List<Component> focusList = new ArrayList<Component>();

		// ACHTUNG: focusList noch aufbereiten !!!!!!!!!!!!!!!!!!!!
		// Es gibt noch ein disabled Button !!! (Undo !!)

		panel.setFocusListe(focusList);
		panel.setLastFocus(buttonCut); // default
		panel.setContainer(panel);

		return panel;
	}

	// ======================================================
	// setBackgroundMetaPanel()
	// ======================================================
	private void setBackgroundMetaPanel(Color color) {

		labelSortDatum.setBackground(color);
		panelDatumName.setBackground(color);
		panelSortDatum.setBackground(color);
		panelIndex1.setBackground(color);
		panelIndex2.setBackground(color);
		panelBemerkungen.setBackground(color);
		panelQualitaetDrehen.setBackground(color);
		datPanel.setBackground(color);
		index1Panel.setBackground(color);
		index2Panel.setBackground(color);
		bemerkungenP.setBackground(color);
		qualitaet.setBackground(color);
		drehen.setBackground(color);
		qualitaetDrehen.setBackground(color);

		index1.setBackgroundTextField(COLOR_ENABLED);
		index2.setBackgroundTextField(COLOR_ENABLED);
		
		
		
		bemerkungen.setBackground(COLOR_ENABLED);
		datumTag.setBackground(COLOR_ENABLED);
		datumMonat.setBackground(COLOR_ENABLED);
		datumJahr.setBackground(COLOR_ENABLED);
		datumStunde.setBackground(COLOR_ENABLED);
		datumMinute.setBackground(COLOR_ENABLED);
		datumSekunde.setBackground(COLOR_ENABLED);
		q1.setBackground(COLOR_ENABLED);
		q2.setBackground(COLOR_ENABLED);
		q3.setBackground(COLOR_ENABLED);
		q4.setBackground(COLOR_ENABLED);
		// links.setBackground(COLOR_ENABLED);
		// oben.setBackground(COLOR_ENABLED);
		// rechts.setBackground(COLOR_ENABLED);

	}

	// ======================================================
	// Meta Panel generieren
	// ======================================================
	JLabel labelSortDatum = null;
	JLabel indexLabel_1;
	JLabel indexLabel_2;
	JLabel bemerkungenLabel;
	JLabel qLabel;
	JLabel drehenLabel;
	JLabel spiegelnLabel;
	
	JPanel panelDatumName = null;
	JPanel panelSortDatum = null;
	JPanel panelIndex1 = null;
	JPanel panelIndex2 = null;
	JPanel panelBemerkungen = null;
	JPanel panelQualitaetDrehen = null;
	JPanel datPanel = null;
	JPanel index1Panel = null;
	JPanel index2Panel = null;
	JPanel bemerkungenP = null;
	JPanel qualitaet = null;
	JPanel drehen = null;
	JPanel qualitaetDrehen = null;
	JTextField degrees = null;
	private PM_FocusPanel getMetaPanel() {

		int columsIndexComboBox = 40;
		
		PM_FocusPanel panel = new PM_FocusPanel() {
			public void setBackgroundColor(Color color) {
				setBackgroundMetaPanel(color);
			}
		};
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panelDatumName = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelSortDatum = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelIndex1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelIndex2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelBemerkungen = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelQualitaetDrehen = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// ----------------------------------------------------------
		// Sortierdatum
		// ----------------------------------------------------------
		datPanel = new JPanel();
		datPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		labelSortDatum = new JLabel("Datum:");
		datPanel.add(labelSortDatum);

		// Import-Datum
		datumImport = new JTextField();
		Font font = datumImport.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		datumImport.setFont(fontBold);
		
		datumImport.setEditable(false);
		datumImport.setEnabled(false);
		datumImport.setColumns(11);
		datPanel.add(datumImport);
		// Sortierdatum
		datumTag = new JComboBox(PM_Utils.getTage(false));
		datPanel.add(datumTag);
		datPanel.add(new JLabel("."));
		datumMonat = new JComboBox(PM_Utils.getMonate(false));
		datPanel.add(datumMonat);
		datPanel.add(new JLabel("."));
		Vector<String> j = PM_Utils.getJahre(false);
		j.add(0, "1970");
		datumJahr = new JComboBox(j);
		datPanel.add(datumJahr);
		datPanel.add(new JLabel("/"));
		datumStunde = new JComboBox(PM_Utils.getStunden(false));
		datPanel.add(datumStunde);
		datPanel.add(new JLabel(":"));
		datumMinute = new JComboBox(PM_Utils.getMinuten(false));
		datPanel.add(datumMinute);
		datPanel.add(new JLabel(":"));
		datumSekunde = new JComboBox(PM_Utils.getSekunden(false));
		datPanel.add(datumSekunde);

		clearSortDatum();

		stopChangeDatum = true;

		ActionListener alDatum = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (stopChangeDatum) {
					return; // Nicht uebernehmen
				}
				// get date -> "02.04.2004 09:40:22"
				String date = datumTag.getSelectedItem() + "."
						+ datumMonat.getSelectedItem() + "."
						+ datumJahr.getSelectedItem() + " "
						+ datumStunde.getSelectedItem() + ":"
						+ datumMinute.getSelectedItem() + ":"
						+ datumSekunde.getSelectedItem();

				datumChanged = PM_Utils.datumToDate(date);

				if (buttonUndo != null)
					geaendert(true);

				
			}
		};
		datumTag.addActionListener(alDatum);
		datumMonat.addActionListener(alDatum);
		datumJahr.addActionListener(alDatum);
		datumStunde.addActionListener(alDatum);
		datumMinute.addActionListener(alDatum);
		datumSekunde.addActionListener(alDatum);

		panelSortDatum.add(datPanel);

		// Label: abweichend vom ....
		labelAbweichendesDatum = new JLabel();
		panelSortDatum.add(labelAbweichendesDatum);

		// ----------------------------------------------------------
		// Index
		// ----------------------------------------------------------
		index1Panel = new JPanel();
		index1Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		indexLabel_1 = new JLabel("Index 1");
		index1Panel.add(indexLabel_1);
		index1 = new PM_IndicesComboBox(IndexType.INDEX_1);
		index1.setColumns(columsIndexComboBox);
		index1.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				if (picture == null || picture.meta == null) {
					return;
				}
				index1Changed = index1.getText();
				geaendert(true);
			}
		});
		index1Panel.add(index1);
		panelIndex1.add(index1Panel);

		// ----------------------------------------------------------
		// Index 2
		// ----------------------------------------------------------
		index2Panel = new JPanel();
		index2Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		indexLabel_2 = new JLabel("Index 2");
		index2Panel.add(indexLabel_2);
		
		
		index2 = new PM_IndicesComboBox(IndexType.INDEX_2);
		index2.setColumns(columsIndexComboBox);
		index2.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				if (picture == null || picture.meta == null) {
					return;
				}
				index2Changed = index2.getText();
				geaendert(true);
			}
		});
		index2Panel.add(index2);
		panelIndex2.add(index2Panel);

		// ----------------------------------------------------------
		// Bemerkugnen
		// ----------------------------------------------------------
		bemerkungenP = new JPanel();
		bemerkungenP.setLayout(new FlowLayout(FlowLayout.LEFT));

	    bemerkungenLabel = new JLabel("Bemerkungen");
		bemerkungenP.add(bemerkungenLabel);
		bemerkungen = new JTextField("    ");
		bemerkungen.setColumns(40);
		// ---- KeyListener --------
		KeyListener bemerkungenLIndex = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				// Keine CTRL-Tasten
				windowMain.keyTextFieldPressed(e);
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					return;
				}
				if (picture == null || picture.meta == null) {
					return;
				}
				bemerkungenChanged = bemerkungen.getText();
				geaendert(true);
				// metadaten.setBemerkungen(bemerkungen.getText());
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}
		};
		bemerkungen.addKeyListener(bemerkungenLIndex);

		bemerkungenP.add(bemerkungen);
		panelBemerkungen.add(bemerkungenP);

		// ----------------------------------------------------------
		// Qualitaet
		// ----------------------------------------------------------
		qualitaet = new JPanel();
		qualitaet.setLayout(new FlowLayout(FlowLayout.LEFT));
		qLabel = new JLabel("Kategorie");
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

		MyRadioListener rl = new MyRadioListener();
		q1.addActionListener(rl);
		q2.addActionListener(rl);
		q3.addActionListener(rl);
		q4.addActionListener(rl);

		// panel.add(qualitaet);

		// --------------------------------------------------------------
		// drehen
		// --------------------------------------------------------------
		drehen = new JPanel();
		drehen.setLayout(new FlowLayout(FlowLayout.LEFT));

		// label "drehen"
		drehenLabel = new JLabel("drehen");
		drehen.add(drehenLabel);

		// links
		JButton buttonLeft = new JButton("L");
		ActionListener alLeft = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDrehen(Rotate.LEFT);
			}
		};
		buttonLeft.addActionListener(alLeft);
		drehen.add(buttonLeft);
		 
		degrees = new JTextField();
		degrees.setColumns(3);
		degrees.setEnabled(false);
		drehen.add(degrees);
		// rechts
		JButton buttonRight = new JButton("R");
		ActionListener alRight = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDrehen(Rotate.RIGHT);
			}
		};
		buttonRight.addActionListener(alRight);
		drehen.add(buttonRight);

		// label "spiegeln"
		spiegelnLabel = new JLabel("spiegeln");
		drehen.add(spiegelnLabel);
		checkBoxSpiegeln = new JCheckBox();
		ActionListener alSpiegeln = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSpiegeln(checkBoxSpiegeln.isSelected());
			}
		};
		checkBoxSpiegeln.addActionListener(alSpiegeln);
		drehen.add(checkBoxSpiegeln);

		// Button "--->" (weiter)
//		JButton buttonWeiter = new JButton("N�chstes Bild");
//		ActionListener alWeiter = new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				indexViewThumbnails.doSelectNextPictureView(picture ,
//						KeyEvent.VK_RIGHT);
//			}
//		};
//		buttonWeiter.addActionListener(alWeiter);

		// -------------------------------------------------------
		// Qualitaet und drehen
		// -------------------------------------------------------
		qualitaetDrehen = new JPanel();
		qualitaetDrehen.setLayout(new FlowLayout(FlowLayout.LEFT));
		qualitaetDrehen.add(qualitaet);
		qualitaetDrehen.add(drehen);
//		qualitaetDrehen.add(buttonWeiter);
		panelQualitaetDrehen.add(qualitaetDrehen);

		// ----------------------------------------------------------
		// alles ferting, jetzt zusammensetzen
		// ----------------------------------------------------------
		panel.add(panelSortDatum);
		panel.add(panelDatumName);
		panel.add(panelIndex1);
		
		if (addOnIndex2) {
			panel.add(panelIndex2);
		}
		panel.add(panelBemerkungen);
		panel.add(panelQualitaetDrehen);

		// falls noch Metadaten eingetragen sind
		doClearMetadaten();
		stopChangeDatum = true;

		// --------------------------------
		// Focus Liste aufbereiten
		// ---------------------------------
		List<Component> zeile1 = new ArrayList<Component>();
		zeile1.add(datumTag);
		zeile1.add(datumMonat);
		zeile1.add(datumJahr);
		zeile1.add(datumStunde);
		zeile1.add(datumMinute);
		zeile1.add(datumSekunde);
		List<Component> zeile2 = new ArrayList<Component>();
		zeile2.add(index1);
		List<Component> zeile3 = new ArrayList<Component>();
		zeile3.add(index2);
		List<Component> zeile4 = new ArrayList<Component>();
		zeile4.add(bemerkungen);
		List<Component> zeile5 = new ArrayList<Component>();
		zeile5.add(q1);
		zeile5.add(q2);
		zeile5.add(q3);
		zeile5.add(q4);
		// zeile5.add(links);
		// zeile5.add(oben);
		// zeile5.add(rechts);
	//	zeile5.add(buttonWeiter);

		List<List<Component>> focusList = new ArrayList<List<Component>>();

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

	// ==========================================================
	// getCenterPanel()
	// ==========================================================
//	private int lastSelected = -1;

	private JComponent getCenterPanel() {
		
	  
		
		// -------------------------------------------------------------
		// das eigentliche Bild
		// -------------------------------------------------------------
		windowPicture = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				doPaintComponent(g);
			}
 		
 			@Override
			public Dimension getPreferredSize() {
 				if (image == null) {
 					return new Dimension(10,10);
 				}
 				
 				int size = Math.max(image.getWidth(null), image.getHeight(null));
 				size += 40;  // offset
 				
				return new Dimension(size,size);
			}
			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}
 			
		};
		
		windowPicture.setBackground(Color.YELLOW);
		JScrollPane windowPictureScrollPane = new JScrollPane(windowPicture);

		 
		
		return windowPictureScrollPane;
	}

	// ======================================================
	// doClearBild()
	//
	//
	// ======================================================
	private void doClearBild() {
		if (picture == null) {
			doClearMetadaten();
			return;
		}
		// die bis jetzt durchgefuehrten Anederungen persistent machen
		requestToChange();

		doClearMetadaten();
		
		// ... und nun alles loeschen
		indexViewThumbnails.controller.repaintPicture(picture); 
		picture = null;
		image = null;

		// Anzeige der Metadaten loeschen
	//	doClearMetadaten();

		// Bild loeschen
		windowZeichnen();
 
		
	}

	// ======================================================
	// doUndo()
	//
	// Felder wieder aus Metadaten beschicken
	// ======================================================
	private void doUndo() {
		geaendert(false);
		doSetGuiFromMetadaten();
		windowZeichnen();
	}

	// ======================================================
	// geaendert()
	//
	// Die Metadaten des Bildes haben sich ge�ndert
	// (kann dann mit "" wieder r�ckg�ngig gemacht werden)
	// ======================================================
	

	private void geaendert(boolean mode) {
		changed = mode;
		if (changed) {
			buttonUndo.setBackground(COLOR_WARNING);
		} else {
			buttonUndo.setBackground(COLOR_ENABLED);
		}
	}

	private boolean istGeaendert() {
		return changed;
	}

	// ======================================================
	// doDrehen()
	//
	//
	// ======================================================
	private void doDrehen(Rotate richtung) {
		if (picture == null) {
			return;
		}
		rotationChanged = PM_Utils.getNextRotation(rotationChanged, richtung);	 
		degrees.setText(String.valueOf(rotationChanged));
		// weiter
		geaendert(true);
		windowZeichnen();		
		
	}

	// ======================================================
	// doSpiegeln()
	//
	//
	// ======================================================
	private void doSpiegeln(boolean spiegeln) {
		spiegelnChanged = spiegeln;
		geaendert(true);
		windowZeichnen();
	}

	// ======================================================
	// doSetGuiFromMetadaten()
	//
	// Unver�ndert aus den Picture-Metadaten die GUI-Werte setzen
	// ======================================================
	private void doSetGuiFromMetadaten() {

		if (picture == null || picture.meta == null) {
			return;
		}

 
		doClearMetadaten();

		// -----------------------------------------
		// alle Felder aus den Metadaten versorgen
		// ---------------------------------------------

		// -------------- Datum und Import-Datum --------------
		setDatumToSortedDatum(picture.meta.getDateCurrent());
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		if (picture.meta.hasDateChanged()) {
			// dates are different
			datumImport.setText(dateFormat.format(picture.meta.getDateImport()));
		} else {
			datumImport.setText(dateFormat.format(picture.meta.getDateCurrent()));
		}
		// -------------- die uebrigen Felder --------------------
		index1.setText(picture.meta.getIndex1());
		index2.setText(picture.meta.getIndex2());
		bemerkungen.setText(picture.meta.getRemarks());
		checkBoxSpiegeln.setSelected(picture.meta.getMirror());
		degrees.setText(String.valueOf(picture.meta.getRotation()));

		// set Category
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

		spiegelnChanged = picture.meta.getMirror();
		index1Changed = picture.meta.getIndex1();
		index2Changed = picture.meta.getIndex2();
		bemerkungenChanged = picture.meta.getRemarks();
		qsChanged = picture.meta.getCategory();
		rotationChanged = picture.meta.getRotation();
		datumChanged = picture.meta.getDateCurrent();

		stopChangeDatum = false;
		if (picture.meta.hasDateChanged()) {
			labelSortDatum.setForeground(Color.RED);
		}

	}

	// ======================================================
	// setDatumToSortedDatum()
	//
	//
	// ======================================================
	private void setDatumToSortedDatum(Date date) {
//		Date date = PM_Utils.datumToDate(datum);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String jahr = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.YEAR)), 4);
		String mon = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.MONTH) + 1), 2);
		String tag = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2);
		String std = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.HOUR_OF_DAY)), 2);
		String min = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.MINUTE)), 2);
		String sec = PM_Utils.stringToString("000"
				+ Integer.toString(cal.get(Calendar.SECOND)), 2);

		boolean oldChangeStatus = stopChangeDatum;
		stopChangeDatum = true; // ActionListener NICHT aufrufen
		datumJahr.setSelectedItem(jahr);
		datumMonat.setSelectedItem(mon);
		datumTag.setSelectedItem(tag);
		datumStunde.setSelectedItem(std);
		datumMinute.setSelectedItem(min);
		datumSekunde.setSelectedItem(sec);
		stopChangeDatum = oldChangeStatus; // ActionListener wieder "setzen"
	}

	// ======================================================
	// clearSortDatum()
	//
	//
	// ======================================================
	private void clearSortDatum() {

		stopChangeDatum = true; // ActionListener NICHT aufrufen

		datumJahr.setSelectedIndex(0);
		datumMonat.setSelectedIndex(0);
		datumTag.setSelectedIndex(0);
		datumStunde.setSelectedIndex(0);
		datumMinute.setSelectedIndex(0);
		datumSekunde.setSelectedIndex(0);

		// / stopChangeDatum = false; // ActionListener wieder aufrufen
	}

	// ======================================================
	// doClearMetadaten()
	//
	//
	// ======================================================
	private void doClearMetadaten() {

		// datumImport.setBackground(datumImportColorBG);
		labelAbweichendesDatum.setText(null);
		labelAbweichendesDatum.setForeground(Color.BLACK);
		labelSortDatum.setForeground(Color.BLACK);

		 
		datumImport.setText("");
		index1.setText("");
		index2.setText("");
		bemerkungen.setText(" ");
		degrees.setText(" ");

		q1.setSelected(false);
		q2.setSelected(false);
		q3.setSelected(false);
		q4.setSelected(false);
 
		clearSortDatum();

	}

	
	
	
	
	// ==============================================================================
	// ==============================================================================
	// ======================  NEU  =================================================
	// ======================  NEU  =================================================
	// ======================  NEU  =================================================
	// ======================  NEU  =================================================
	// ======================  NEU  =================================================
	// ==============================================================================
	// ==============================================================================
	// ==============================================================================
	
	
	private void doPaintComponent (Graphics g1) {
		super.paintComponent(g1);
		// ======================  NEU  =================================================
		Graphics2D g2 = (Graphics2D) g1;

		if (image == null) {
			return;
		}

		double zoom = 1; //.75;
		
		Color color = g2.getColor();	
		
		Rectangle cut = null;
		Dimension cutSize = picture.meta.getImageSize();   //   getImageOriginalSize();
		if (picture.meta.hasCutRectangle()) {
			cut = picture.meta.getCutRectangle();
		}
		BufferedImage bufferedImage = PM_UtilsGrafik.getBufferedImage ( image,   cut, cutSize, zoom);
		// Das ganze Bild soll nicht in der oberen linken Ecker sein
	 	int offsetX = 20;
		int offsetY = 20;	
		
		// in umgekehrter Reihenfolge aufbereiten
		AffineTransform Tx = new AffineTransform();		
		// (4) zum Schluss etwas mehr in die Mitte
		Tx.translate(offsetX, offsetY);
		// (3) nach dem Drehen wieder in den richtigen (sichtbaren)
		// Bereich verschieben (es wurde ja um den Nullpunkt gedreht)
		Point2D.Double point = PM_UtilsGrafik.getMovePoint(rotationChanged, 
				 image.getWidth(null)*zoom,image.getHeight(null)*zoom );
 		Tx.translate(point.x, point.y);
		// (2) drehen
		Tx.rotate(rotationChanged * Math.PI / 180.);
		//	(1) spiegeln
		if (spiegelnChanged) {
			Tx.translate(image.getWidth(null)*zoom, 0);
			Tx.scale(-1, 1);
		}
		
	 
		
		
		g2.drawImage(bufferedImage, Tx, this);
		
		// --------------------------------------------------------
		// �ber das Bild den vollst�ndigen Path-Namen ausgeben
		// --------------------------------------------------------
		g2.setColor(color);
		Font font = g2.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		g2.setFont(fontBold);
		g2.drawString(picture.getFileOriginal().getPath(), 10, 12);

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
			geaendert(true);

		}
	}

} // End Class PM_WindowEinzelbild
