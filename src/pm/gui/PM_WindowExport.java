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

 
import pm.search.*;
import pm.sequence.*;
import pm.utilities.*;
import pm.utilities.PM_Configuration.BackUp;
import pm.index.*;
import pm.inout.*;
import pm.picture.*;
 
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Sub window to export pictures.
 * 
 * You export pictures changed, unchanged with or without matadata.
 * Further you can delete pictures IRREVERSIBLE.
 * (This is the only place were you can physical delete pictures)
 *
 *
 */
@SuppressWarnings("serial")
public class PM_WindowExport extends PM_WindowBase implements PM_Interface {

	private JComponent panelLeft = null;
	private JPanel panelRight = null;
	private PM_Configuration einstellungen = null;
	private JSplitPane splitPane = null; 
	private final Color colorRight = Color.YELLOW;
	private Border border;
	
	private Vector<PM_Listener> exportDeleteListener = new Vector<PM_Listener>();
	private Vector<String> seitenVerhaeltnisse = new Vector<String>();
	private Vector<String> resolutions = new Vector<String>();
	private JSpinner overscanX = null;
	private JSpinner overscanY = null;
	private JCheckBox testOverscanColor = null;

	private PM_TreeWindow 	treeAlbum;
	
	private List<BackUp> backupAlbumList;
	
	private PM_WindowExport instance;
	static public final int TAB_PICTURE = 0;
	static public final int TAB_ALBUM = 1;
	
	
	// ======================================================
	// Konstruktor
	// ======================================================
	public PM_WindowExport( ) {
		super(PM_Index.createIndexRight( ));

		instance = this;
		border = BorderFactory.createLineBorder(Color.GRAY, 3);
		setLayout(new BorderLayout());
	
		einstellungen = PM_Configuration.getInstance();
		backupAlbumList = einstellungen.getBackupAlbumList();
		 
		
		// ------------------------------------------------
		// Panel left
		// -------------------------------------------------
//   	getIndexView().setAllowGetFromRight(true);
//		getIndexView().setPopUpLoeschen(true);
//		getIndexView().setPopUpLoeschenAufheben(true);
//		getIndexView().setPopUpAendern(true);
//		getIndexView().setPopUpDiaShow(true);
//		getIndexView().setTextAufloesung(true);

		
		
	 
		
		
	 
	    
		
		// Album Panel
		treeAlbum = new PM_TreeWindowAlbum(this, getTreeAlbumMouseListener());	
//		treeAlbum.addTreeSelectionListener(treeSelectionListener);
//		treeAlbum.setMarkSelectedSequence(true);
		JScrollPane spTreeAlbumLeft = new JScrollPane(treeAlbum);
		
		// Index Panel
		panelLeft = getIndexPanel();
		addIndexToolbar(getIndex().getIndexToolbar());
		
		final JTabbedPane tabbedPane = new JTabbedPane();
//		tabbedPane.insertTab("Bilder",null, panelLeft, "", 1);
//		tabbedPane.insertTab("Album",null, spTreeAlbumLeft, "", 2);
		tabbedPane.addTab("Bilder", panelLeft);
		if (!backupAlbumList.isEmpty()) {
			tabbedPane.addTab("Album", spTreeAlbumLeft);
		}
		tabbedPane.setSelectedIndex(TAB_PICTURE);
		
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				int sel = tabbedPane.getSelectedIndex();
				if (tabbedPane.getSelectedIndex() == TAB_PICTURE) {
					buttonExport.setEnabled(true);
					buttonExportAlbum.setEnabled(false);
				} else {
					buttonExport.setEnabled(false);
					buttonExportAlbum.setEnabled(true);
				}
				 
			}
		});
		
		
		 
		
		
		 
//		panelLeft.setLayout(new BorderLayout());
//		JPanel linksOben = getPanelUpperLeft();
//		JScrollPane spLinksOben = new JScrollPane(linksOben);
//		spLinksOben.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		spLinksOben.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//		panelLeft.add(spLinksOben, BorderLayout.NORTH);
//		panelLeft.add(indexViewScrollPane, BorderLayout.CENTER);
//		panelLeft.add(getIndexView().getSlider(), BorderLayout.SOUTH);

		// ----------------------------------------------------------
		// panel right
		// -----------------------------------------------------------
		panelRight = getPanelRight();
		panelRight.setBackground(colorRight);
		JScrollPane scPanelRechts = new JScrollPane(panelRight);

		// -----------------------------------------------------------
		// Splitscreen  
		// ------------------------------------------------------------
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane,
				scPanelRechts);		
		
		int location = PM_All_InitValues.getInstance().getValueInt(this, "vertical-devider");
		if (location == 0) {
			location = 200;
		}
		splitPane.setDividerLocation(location);		
		add(splitPane);

		// ganz zum Schluss Eigenschaften setzen
		doEigenUnveraendert(); // bei Start 'Bilder unveraendert' exportieren

		
		// --------------------------------------------------------
		// Change Listener for message
		// --------------------------------------------------------
		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {				 
				setMsg();
			}			
		};		
		PM_MSG.addChangeListener(msgListener);		
		// setting among other things tab-title 
//		setMsg();
	}
	
	
	
	private MouseListener getTreeAlbumMouseListener() {
		MouseAdapter ml = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Point clickPoint = e.getPoint();
				TreePath path = treeAlbum.getPathForLocation(clickPoint.x, clickPoint.y);
				if (path == null) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				int button = e.getButton();
				int click = e.getClickCount();

				if (button == 1) {
					if (click >= 2) {
//						windowBase.doubleClickOnTree(node, treeWindow);
					}
				} else if (button == 3) {
 					showContextMenu(e, node);
				}
			}
		};		
		return ml;		 
	}
	
	private void showContextMenu(MouseEvent e, final DefaultMutableTreeNode node ) {		 
		JPopupMenu popup = new JPopupMenu();		 		
		JMenuItem menuItemExport = new JMenuItem("Export");
		ActionListener alZeileLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {

				 
				 
				// TEST
				BackUp backUp = backupAlbumList.get(0); // TEST TEST TEST TEST
				List<PM_Sequence> sequences = PM_TreeModelAlbum.getInstance()
						.getSequenceListClose(node);
				PM_ExportChangedAlbum.removeNoSequences(sequences);
				if (sequences.isEmpty()) {
					return;
				}
				// ab Knoten exportieren
				String text = sequences.size() +" Sequenzen Exportieren ?";
				int n = JOptionPane
						.showConfirmDialog(null, text, null,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (n == JOptionPane.NO_OPTION) {
					return;
				}

				PM_ExportChangedAlbum eca = new PM_ExportChangedAlbum(
						windowMain, instance, sequences, backUp, false);
				eca.doExport();
			}
		};
		menuItemExport.addActionListener(alZeileLoeschen);
		popup.add(menuItemExport);		 
		if (popup.getSubElements().length > 0) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
		
	
	private void setMsg() {
		
		
		buttonDelete.setText(PM_MSG.getMsg("winExpButtonDelete"));
		buttonExport.setText(PM_MSG.getMsg("winExpButtonExport"));
		
		
		// ------ Output -------------------
		titleOutput.setTitle(PM_MSG.getMsg("winExpTitleOutput"));
		labelExportDirectory.setText(PM_MSG.getMsg("winExpOutDir"));
		mitMetadaten.setText(PM_MSG.getMsg("winExpOutMeta"));
		ausgabeNameUnveraendert.setText(PM_MSG.getMsg("winExpOutUnchng"));
		ausgabeNameVeraendert.setText(PM_MSG.getMsg("winExpOutChng"));
		labelNewBaseName.setText(PM_MSG.getMsg("winExpOutBaseName"));
		labelStartNumber.setText(PM_MSG.getMsg("winExpOutStartNr"));
		
		
		// ----- Properties  -----
		titleProperties.setTitle(PM_MSG.getMsg("winExpTitleProperties"));
		eigenUnveraendert.setText(PM_MSG.getMsg("winExpCBnotChanged"));	
		eigenVeraendert.setText(PM_MSG.getMsg("winExpCBchanged"));	
		checkBoxCut.setText(PM_MSG.getMsg("winExpCBcut"));	
		checkBoxRatio.setText(PM_MSG.getMsg("winExpCBratio"));
		checkBoxRotateMirror.setText(PM_MSG.getMsg("winExpCBrotate"));
		checkBoxSolution.setText(PM_MSG.getMsg("winExpCBsolution"));		
		checkBoxPicText.setText(PM_MSG.getMsg("winExpCBtext"));	
		labelColorFG.setText(PM_MSG.getMsg("winExpCBtextColor"));
		buttonColorFG.setText(PM_MSG.getMsg("winExpCBtextFG"));
		buttonColorBG.setText(PM_MSG.getMsg("winExpCBtextBG"));		
		labelTestFgBg.setText(PM_MSG.getMsg("winExpCBtextFont"));	
		labelTransparent.setText(PM_MSG.getMsg("winExpCBtextTrans"));		
		checkBoxOverscan.setText(PM_MSG.getMsg("winExpCBoverscan"));	
			
		
	}

	/**
	 * Delete pictures before close the application.
	 * 
	 * The user want to delete pictures with category 4 before
	 * close the application. 
	 */
	public void deletePictures(List<PM_Picture> pictures) {
	//	Dimension size = getSize();
		splitPane.setDividerLocation(1000);
		getIndex().data.clearAndAdd(pictures);
		buttonDelete.setBackground(Color.RED);
	}
	
	// ======================================================
	// requestToChange()
	//
	// Aufruf beim Tab-Wechsel: Pruefen, ob Aktivitaeten abgeschlossen.
	// ======================================================
	@Override
	public boolean requestToChange() {
		return true;
	}

	// ======================================================
	// close()
	//
	// Ende der Verarbeitung
	// ======================================================
	@Override
	public void close() {
		PM_All_InitValues.getInstance().putValueInt(this, "color-fg",
				colorFG.getRGB());
		PM_All_InitValues.getInstance().putValueInt(this, "color-bg",
				colorBG.getRGB());
		PM_All_InitValues.getInstance().putValueBoolean(this, "transparent",
				getTransparent());	
		PM_All_InitValues.getInstance().putValueInt(this, "overscanX",
				getOverscanValueX());
		PM_All_InitValues.getInstance().putValueInt(this, "overscanY",
				getOverscanValueY());
		
		PM_All_InitValues.getInstance().putValueInt(this, "vertical-devider",
				splitPane.getDividerLocation());
		
		
	}

	// ======================================================
	// getOverscanValueX()/Y
	// ======================================================
	public boolean getOverscan() {
		return checkBoxOverscan.isSelected();
	}
	public int getOverscanValueX() {
		Object o = overscanX.getValue();
		if (o instanceof Integer) {
			return ((Integer) o).intValue();
		}
		return 0;
	}

	public int getOverscanValueY() {
		Object o = overscanY.getValue();
		if (o instanceof Integer) {
			return ((Integer) o).intValue();
		}

		return 0;
	}

	
	// ======================================================
	// getResolution() 
	//   in Pixel (0 --> unver�ndert)
	// ======================================================
	public double getResolution() {
		
		if (!checkBoxSolution.isSelected()) {
			return 0; // check Box nicht selektiert
		}
		
		double f = 1000000;
		int item = cbAufloesung.getSelectedIndex();

		if (item <= 0) {
			return 0; // unver�ndert
		}
		// TODO  this is a hack !!!!!!
		
		String rString = (String) resolutions.elementAt(item);
		if (rString.indexOf("hoch") >= 0) {
			return 3*f;
		} else if (rString.indexOf("mittel") >= 0) {
			return 1*f;
		} else if (rString.indexOf("niedrig") >= 0) {
			return 0.4*f;
		}
		
		return  0;
	}
	// ======================================================
	// getAufloesungen)
	// ======================================================
	
	private Vector getAufloesungen() {
		resolutions = new Vector<String>();
		resolutions.add("unver�ndert");
		resolutions.add("hoch    (3 MegaPixel");
		resolutions.add("mittel  (1 MegaPixel");
		resolutions.add("niedrig (0,4 MegaPixel");

		return resolutions;
	}
	  
	// ======================================================
	// getForgroundColor()/getBackgroundColor()
	// ======================================================
	public Color getForgroundColor() {
		return colorFG;
	}

	public Color getBackgroundColor() {
		return colorBG;
	}

	// ======================================================
	// getTest()
	// ======================================================
	public boolean getTestOverscancolor() {
		return testOverscanColor.isSelected();
	}

	// ======================================================
	// getDrehen()
	// ======================================================
	public boolean getRotate() {
		return checkBoxRotateMirror.isSelected();
	}

	// ======================================================
	// getAusschneiden()
	// ======================================================
	public boolean getCut() {
		return checkBoxCut.isSelected();
	}

	// ======================================================
	// getTransparent()
	// ======================================================
	public boolean getTransparent() {
		return transparent.isSelected();
	}

	// ======================================================
	// getBildunterschrift()
	// ======================================================
	public boolean getTextOnPicture() {
		return checkBoxPicText.isSelected();
	}

 

	// =====================================================
	// addExportDeleteListener()
	//
	// wenn Export mit Metadaten.
	// =====================================================
	public void addExportDeleteListener(PM_Listener listener) {
		if (!exportDeleteListener.contains(listener))
			exportDeleteListener.add(listener);
	}

	// =====================================================
	// fireExportDeleteListener()
	//
	// Wird von "PM_ExportierenMitMetadaten" aufgerufen !!!!!
	//
	// (siehe dort: "vorbereitenLoeschen()")
	//
	// (Der fire muss hier erfolgen, da fuer jeden Export
	// eine neue PM_Export-Instanz erzeugt wird, die Listener
	// jedoch f�r die gesamte session gelten)
	// =====================================================
	public void fireExportDeleteListener() {
		for (int i = 0; i < exportDeleteListener.size(); i++) {
			PM_Listener listener = (PM_Listener) exportDeleteListener
					.elementAt(i);
			listener.actionPerformed(new PM_Action(this));
		}
	}

	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================
	// ======================= PRIVATE ==================================

	// ======================================================
	// getPanelLinksOben()
	// ======================================================
	
	private JButton buttonDelete;
	
	 
	private void addIndexToolbar(JPanel toolBar) {
		 
		// Button delete
 		buttonDelete = new JButton("Bilder L�schen");
 		toolBar.add(buttonDelete);
		ActionListener alExportieren = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
  			   doDeleteIrreversible();			 
			}
		};
		buttonDelete.addActionListener(alExportieren);
 
		 
	}

  	 

	// ======================================================
	// Index Panel generieren
	// ======================================================
 
	private JButton buttonExport;
	private JButton buttonExportAlbum;
	private JPanel getPanelRight() {
		JPanel panel = new JPanel();
		panel.setBackground(colorRight);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);

		
		// Button "Exportieren"
		JPanel panelExp = new JPanel();
		panelExp.setBackground(Color.YELLOW);
		panelExp.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		
 		buttonExport = new JButton("Bilder exportieren");
 		panelExp.add(buttonExport);
		ActionListener alExportieren = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				doExport();
			}
		};
		buttonExport.addActionListener(alExportieren);
		
		buttonExportAlbum = new JButton("Album exportieren");
 		panelExp.add(buttonExportAlbum);
		ActionListener alExportierenAlbum = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				PM_TreeModel treeModel = PM_TreeModelAlbum.getInstance();
				// TEST
				BackUp backUp = backupAlbumList.get(0); // TEST TEST TEST TEST
				List<PM_Sequence> sequences = PM_TreeModelAlbum.getInstance()
						.getSequenceListClose(treeModel.getRootNode());
				PM_ExportChangedAlbum.removeNoSequences(sequences);
				if (sequences.isEmpty()) {
					return;
				}
				// ab Knoten exportieren
				String text = "Exportieren Album: Update?";
				int n = JOptionPane
						.showConfirmDialog(null, text, null,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (n == JOptionPane.NO_OPTION) {
					return;
				}

				PM_ExportChangedAlbum eca = new PM_ExportChangedAlbum(
						windowMain, instance, sequences, backUp, true);
				eca.doExport ( );
			}
		};
		buttonExportAlbum.addActionListener(alExportierenAlbum);
		
		
		buttonExport.setEnabled(true);
		buttonExportAlbum.setEnabled(false);
		
		panel.add(panelExp);
		panel.add(Box.createVerticalStrut(10));
		panel.add(getGroupOutput());
		panel.add(Box.createVerticalStrut(10));
		panel.add(getGroupProperties());

		return panel;
	} 

	private JRadioButton ausgabeNameUnveraendert = null;
	private JRadioButton ausgabeNameVeraendert = null;
	private JTextField ausgabeBildName = null;
	private JTextField lfdNummer = null;
	private JCheckBox mitMetadaten = null;
	private JLabel labelExportDirectory;
	private JLabel labelNewBaseName;
	private JLabel labelStartNumber;
	// ======================================================
	// getGroupAusgabe()
	// ======================================================
	private JPanel getGroupOutput() {
		// Ordner
		JPanel ordner = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ordner.setBackground(colorRight);
		labelExportDirectory = new JLabel("Export-Verzeichnis:");
		ordner.add(labelExportDirectory);
		JTextField ordnerName = new JTextField(einstellungen.getFileHomeTemp()
				.getPath());
		ordnerName.setColumns(22);
		ordner.add(ordnerName);
		JButton browserButton = new JButton("...");
		browserButton.setEnabled(false);
		ordner.add(browserButton);

		// CheckBox "mit Metadaten"
		JPanel zeileMitMetadaten = new JPanel(new FlowLayout(FlowLayout.LEFT));		
		mitMetadaten = new JCheckBox("mit Metadaten");
		mitMetadaten.setSelected(false);
		ActionListener alMetadata = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mitMetadaten.isSelected()) {
					eigenUnveraendert.setSelected(true); 
				}  
			}
		};
		mitMetadaten.addActionListener(alMetadata);
		
		
		
		zeileMitMetadaten.setBackground(colorRight);
		zeileMitMetadaten.add(mitMetadaten);
		 
		
		
		
		// Panel unveraendert (radio Button)
		JPanel panelUnveraendert = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelUnveraendert.setBackground(colorRight);
		ausgabeNameUnveraendert = new JRadioButton(
				"Namen der Bilder (Filename) unver�ndert");
		ausgabeNameUnveraendert.setBackground(colorRight);
		ausgabeNameUnveraendert.setSelected(true);
		ActionListener alUnveraendert = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPictureNamesChanged(false);
			}
		};
		ausgabeNameUnveraendert.addActionListener(alUnveraendert);
		panelUnveraendert.add(ausgabeNameUnveraendert);

		// Panel veraendert (radio Button)
		JPanel panelVeraendert = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelVeraendert.setBackground(colorRight);
		ausgabeNameVeraendert = new JRadioButton(
				"Namen der Bilder (Filename) neu vergeben");
		ausgabeNameVeraendert.setBackground(colorRight);
		ActionListener alVeraendert = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPictureNamesChanged(true);
			}
		};
		ausgabeNameVeraendert.addActionListener(alVeraendert);
		panelVeraendert.add(ausgabeNameVeraendert);

		// Radio Buttons gruppieren
		ButtonGroup group = new ButtonGroup();
		group.add(ausgabeNameUnveraendert);
		group.add(ausgabeNameVeraendert);

		// Eingabe Bild-Namen
		JPanel bildNamePanel = new JPanel(); // new
		// FlowLayout(FlowLayout.LEFT));
		bildNamePanel.setLayout(new BoxLayout(bildNamePanel, BoxLayout.X_AXIS));
		bildNamePanel.add(Box.createHorizontalStrut(30));
		bildNamePanel.setBackground(colorRight);
		labelNewBaseName = new JLabel("neuer (Basis-)Name:");
		bildNamePanel.add(labelNewBaseName);
		ausgabeBildName = new JTextField(" ");
		ausgabeBildName.setEnabled(false);
		ausgabeBildName.setBackground(Color.LIGHT_GRAY);
		bildNamePanel.add(Box.createHorizontalStrut(5));
		KeyListener klBildName = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				neuerBildName = ausgabeBildName.getText();
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}
		};
		ausgabeBildName.addKeyListener(klBildName);
		ausgabeBildName.setColumns(15);
		bildNamePanel.add(ausgabeBildName);
		labelStartNumber = new JLabel("+ lfd.Nummer");
		bildNamePanel.add(labelStartNumber) ;
		lfdNummer = new JTextField("001");
		lfdNummer.setColumns(5);
		lfdNummer.setEnabled(false);
		lfdNummer.setBackground(Color.LIGHT_GRAY);
		bildNamePanel.add(lfdNummer);

		KeyListener klLfdNummer = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				// pr�fen auf numerisch
				String nr = lfdNummer.getText().trim();
				if (nr.length() == 0) {
					neueLfdNummer = "";
					return;
				}

				// if (PM_Utils.stringToInt(nr)==0) {
				// lfdNummer.setText("0001");
				// return;
				// }
				try {
					Integer.parseInt(nr);
				} catch (NumberFormatException ee) {
					lfdNummer.setText(neueLfdNummer);
					return;
				}
				neueLfdNummer = lfdNummer.getText();
			}

			public void keyPressed(KeyEvent e) {
				// neueLfdNummer = lfdNummer.getText();
			}

			public void keyTyped(KeyEvent e) {
			}
		};
		lfdNummer.addKeyListener(klLfdNummer);

		JPanel panel = new JPanel();
		panel.setBackground(colorRight);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);
		panel.add(ordner);
		panel.add(zeileMitMetadaten);
		panel.add(panelUnveraendert);
		panel.add(panelVeraendert);
		panel.add(bildNamePanel);

		titleOutput = BorderFactory.createTitledBorder(border,
				"Ausgabe");
		panel.setBorder(titleOutput);

		JPanel out = new JPanel(new FlowLayout(FlowLayout.LEFT));
		out.setBackground(colorRight);
		out.add(panel);
		return out;
	}

	// ======================================================
	// getGroupEigenschaften()
	// ======================================================
	private int ident = 30;
	
	private JCheckBox checkBoxCut = null;
	private JCheckBox checkBoxRotateMirror = null;
	private JCheckBox checkBoxRatio = null;
	private JCheckBox checkBoxSolution = null;
	private JComboBox cbAufloesung = null;
	private JCheckBox checkBoxOverscan = null;	
	private JCheckBox checkBoxPicText = null;
	
	private JCheckBox transparent = null;
	private JRadioButton eigenUnveraendert = null;
	private JRadioButton eigenVeraendert = null;
	private JComboBox cbSeite = null;
	 
	private JButton buttonColorBG = null;
	private Color colorBG = Color.RED;
	private JButton buttonColorFG = null;
	private Color colorFG = Color.BLACK;
	private JLabel labelTestFgBg = null;
 

 
	
	private TitledBorder titleOutput;
	private TitledBorder titleProperties;
	
	 
	private JLabel labelColorFG;
	private JLabel labelTransparent;
	
	private JPanel getGroupProperties() {
		// Color f�r Export Ver�ndert
		int fgRGB = PM_All_InitValues.getInstance().getValueInt(this,
				"color-fg");
		int bgRGB = PM_All_InitValues.getInstance().getValueInt(this,
				"color-bg");
		if (fgRGB != 0) {
			colorFG = new Color(fgRGB);
		}
		if (bgRGB != 0) {
			colorBG = new Color(bgRGB);
		}
	 
	 
		// ----------------------------------------------------------------
		// unveraendert
		// ----------------------------------------------------------------
		JPanel panelUnveraendert = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelUnveraendert.setBackground(colorRight);
		eigenUnveraendert = new JRadioButton("Bilder unver�ndert exportieren");
		eigenUnveraendert.setBackground(colorRight);
		ActionListener alEigenUnveraendert = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEigenUnveraendert();
			}
		};
		eigenUnveraendert.addActionListener(alEigenUnveraendert);
		panelUnveraendert.add(eigenUnveraendert);
  
		// -----------------------------------------------------------------------
		// veraendern
		// ----------------------------------------------------------------------
		JPanel panelVeraendert = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelVeraendert.setBackground(colorRight);
		eigenVeraendert = new JRadioButton("Bilder in dieser Reihenfolge ver�ndern:");
		eigenVeraendert.setBackground(colorRight);
		ActionListener alEigenVeraendert = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// der Radio-Button "Bilder ver�ndert exportieren" aktiviert
				doEigenVeraendert();
				if (eigenVeraendert.isSelected()) {
					mitMetadaten.setSelected(false);
				}
				
			}
		};
		eigenVeraendert.addActionListener(alEigenVeraendert);
		panelVeraendert.add(eigenVeraendert);

		// 
		JPanel panelAusGes = new JPanel();  
		panelAusGes.setLayout(new BoxLayout(panelAusGes, BoxLayout.X_AXIS));
		panelAusGes.setBackground(colorRight);
		panelAusGes.add(Box.createHorizontalStrut(ident));
		
		
		
		JPanel panelAusschneiden = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelAusGes.add(panelAusschneiden);
		panelAusschneiden.setBackground(colorRight);
		
		
		// (1) ausschneiden----
		JPanel zeileCut = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		zeileCut.setBackground(colorRight);
		zeileCut.add(Box.createHorizontalStrut(ident));
		checkBoxCut = new JCheckBox("ausschneiden");
		checkBoxCut.setBackground(colorRight);
		zeileCut.add(checkBoxCut);
		ActionListener alSchneiden = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (eigenVeraendert.isSelected()) {
					doEigenVeraendert();
				}
			}
		};
		checkBoxCut.addActionListener(alSchneiden);
		
		// (2)-----  Zeile Seitenverh�ltnis -------
		JPanel zeileRatio = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		zeileRatio.setBackground(colorRight);
		zeileRatio.add(Box.createHorizontalStrut(ident));
		
		checkBoxRatio = new JCheckBox("Seitenverh�ltnis:");
		zeileRatio.add(checkBoxRatio);
		checkBoxRatio.setEnabled(false);
		zeileRatio.setBackground(colorRight);			 
		cbSeite = new JComboBox(getSeitenVerhaeltnisse());
		ComboBoxListener cl = new ComboBoxListener();
		cbSeite.addActionListener(cl);
		cbSeite.setEnabled(false);
		zeileRatio.add(cbSeite);	
		
		// (3) ---- Zeile: drehen / spiegeln ------
		JPanel zeileRotate = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		zeileRotate.setBackground(colorRight);
		zeileRotate.add(Box.createHorizontalStrut(ident));	
		checkBoxRotateMirror = new JCheckBox("drehen/spiegeln");
		checkBoxRotateMirror.setBackground(colorRight);
		zeileRotate.add(checkBoxRotateMirror);
		ActionListener alDrehen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (eigenVeraendert.isSelected()) {
					doEigenVeraendert();
				}
			}
		};
		checkBoxRotateMirror.addActionListener(alDrehen);


		
		// ------ Zeile:  Aufl�sung ----------------
		JPanel panelAufloesung = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelAufloesung.setBackground(colorRight);
		panelAufloesung.add(Box.createHorizontalStrut(ident));
		checkBoxSolution = new JCheckBox("Aufl�sung:");
		panelAufloesung.add(checkBoxSolution);
//		labelSolution = new JLabel("Aufl�sung:");
//		panelAufloesung.add(labelSolution);
		cbAufloesung = new JComboBox(getAufloesungen());
		cbAufloesung.setEnabled(false);
		panelAufloesung.add(cbAufloesung);
		

		
		
		// ==============  Bildunterschrift ========================
		int indent2 = ident + 30;
		JPanel zeileText = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		zeileText.setBackground(colorRight);
		zeileText.add(Box.createHorizontalStrut(ident));
		checkBoxPicText = new JCheckBox("Bildunterschrift:"); // mit Bildunterschrift
		checkBoxPicText.setBackground(colorRight);
		zeileText.add(checkBoxPicText);
		 
			
		// --- Bildunterschrift: Farbe der Schrift --------------------
		JPanel zeileFarben = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		zeileFarben.setBackground(colorRight);
		zeileFarben.add(Box.createHorizontalStrut(indent2));
		labelColorFG = new JLabel("Textfarben: ");
		zeileFarben.add(labelColorFG);
		buttonColorFG = new JButton("Schrift");
		zeileFarben.add(buttonColorFG);	
		ActionListener alColorFG = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newFG = JColorChooser.showDialog(null,
						"W�hle Schriftfarbe", colorFG);
				if (newFG != null) {
					colorFG = newFG;
				}
				labelTestFgBg.setForeground(colorFG);
			}
		};
		buttonColorFG.addActionListener(alColorFG);												
		buttonColorBG = new JButton("Hintergrund");
		zeileFarben.add(buttonColorBG);
		ActionListener alColorBG = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newBG = JColorChooser.showDialog(null,
						"W�hle Hintergrundfarbe", colorBG);
				if (newBG != null) {
					colorBG = newBG;
				}
				labelTestFgBg.setBackground(colorBG);
			}
		};
		buttonColorBG.addActionListener(alColorBG);
		
		labelTestFgBg = new JLabel("Schrift");
		labelTestFgBg.setOpaque(true);
		labelTestFgBg.setForeground(colorFG);
		labelTestFgBg.setBackground(colorBG);
		zeileFarben.add(labelTestFgBg);
		
		// --- Bildunterschrift: Hintergrund transparent --------------------
		JPanel zeileTransparent = new JPanel(new FlowLayout(FlowLayout.LEFT));
		zeileTransparent.setBackground(colorRight);
		zeileTransparent.add(Box.createHorizontalStrut(indent2));
		transparent = new JCheckBox();			
		boolean trans = PM_All_InitValues.getInstance().getValueBoolean(this,
			"transparent");	
		transparent.setSelected(trans);
		labelTransparent = new JLabel("Hintergrund transparent");
		zeileTransparent.add(labelTransparent);
		zeileTransparent.add(transparent);
		ActionListener alTrans = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (transparent.isSelected())  {
					buttonColorBG.setEnabled(false);
					labelTestFgBg.setBackground(colorRight);
				} else {
					buttonColorBG.setEnabled(true);
					labelTestFgBg.setBackground(colorBG);
				}
			}
		};
		transparent.addActionListener(alTrans);		
		// -------- Zeile:Overscan -----------------
		JPanel panelOverGes = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		panelOverGes.setLayout(new BoxLayout(panelOverGes, BoxLayout.X_AXIS));		
		
		panelOverGes.setBackground(colorRight);
		panelOverGes.add(Box.createHorizontalStrut(ident));
		checkBoxOverscan = new JCheckBox("Overscan ");
		checkBoxOverscan.setBackground(colorRight);
		ActionListener alOver = new ActionListener() {
			public void actionPerformed(ActionEvent e) {				 
				testOverscanColor.setEnabled(checkBoxOverscan.isSelected());
				overscanX.setEnabled(checkBoxOverscan.isSelected());
				overscanY.setEnabled(checkBoxOverscan.isSelected());			 
			}
		};
		checkBoxOverscan.addActionListener(alOver);
		
		
		panelOverGes.add(checkBoxOverscan);		
		JPanel panelOverscan = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelOverscan.setBackground(colorRight);
		// Overscan-X
		panelOverscan.add(new JLabel("X"));
		int currentPerCentX = 0;
		SpinnerModel overscaModelX = new SpinnerNumberModel(currentPerCentX, // initial
				// value
				currentPerCentX, // min
				currentPerCentX + 50, // max
				1); // step
		overscanX = new JSpinner(overscaModelX);
		panelOverscan.add(overscanX);
		// Overscan-Y
		panelOverscan.add(new JLabel("Y"));
		int currentPerCentY = 0;
		SpinnerModel overscaModelY = new SpinnerNumberModel(currentPerCentY, // initial
				// value
				currentPerCentY, // min
				currentPerCentY + 50, // maxgroupBG.add(bgTrans);
				1); // step
		overscanY = new JSpinner(overscaModelY);
		panelOverscan.add(overscanY);
		// test (andere "Overscan"-Farbe)
		panelOverscan.add(new JLabel("Test"));
		testOverscanColor = new JCheckBox();
		panelOverscan.add(testOverscanColor);
		// Overscan fertig
		panelOverGes.add(panelOverscan);
		// Set Overscan
		int overX = PM_All_InitValues.getInstance().getValueInt(this, "overscanX");
		int overY = PM_All_InitValues.getInstance().getValueInt(this, "overscanY");
		overscanX.setValue(overX);
		overscanY.setValue(overY);
		
		// -----------------------------------------------------------------
		// Alles fertig !!
		// --------------------------------------------------------------------
		// Setzen enable ....
		eigenUnveraendert.setSelected(true);
		checkBoxCut.setEnabled(false);
		
		checkBoxCut.setEnabled(false);	
		checkBoxRatio.setEnabled(false);
		checkBoxRotateMirror.setEnabled(false);
		checkBoxSolution.setEnabled(false);
		checkBoxPicText.setEnabled(false);	 
		checkBoxOverscan.setEnabled(false);
		

		JPanel panel = new JPanel();
		panel.setBackground(colorRight);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);

		panel.add(panelUnveraendert);
		panel.add(panelVeraendert);

		// Bilder ver�ndert exportieren
		panel.add(zeileCut);  
		panel.add(zeileRatio);
		panel.add(zeileRotate);  	
		panel.add(panelAufloesung);		
		panel.add(zeileText);  
		panel.add(zeileFarben);
		panel.add(zeileTransparent);
		panel.add(panelOverGes);

	 
		
		
		ButtonGroup group2 = new ButtonGroup();
		group2.add(eigenUnveraendert);
		group2.add(eigenVeraendert);

		titleProperties = BorderFactory.createTitledBorder(border,
				"Eigenschaften");
		panel.setBorder(titleProperties);

		JPanel out = new JPanel(new FlowLayout(FlowLayout.LEFT));
		out.setBackground(colorRight);
		out.add(panel);
		return out;
	}
 

	// ======================================================
	// setBildnameVeraendertExportieren()
	//
	// Der RadioButton "Bilder ver�ndert exportieren" ist selektiert/nicht
	// selektiert.
	// (hier die check-boxen setzen)
	// ======================================================
	private String neuerBildName = "";
	private String neueLfdNummer = "";

	private void setPictureNamesChanged(boolean selected) {
		if (selected) {
			ausgabeBildName.setText(neuerBildName);
			ausgabeBildName.setBackground(Color.WHITE);
			lfdNummer.setText(neueLfdNummer);
			if (PM_Utils.stringToInt(neueLfdNummer) == 0)
				lfdNummer.setText("0001");
			lfdNummer.setBackground(Color.WHITE);
		} else {
			ausgabeBildName.setText("");
			ausgabeBildName.setBackground(Color.LIGHT_GRAY);
			neueLfdNummer = lfdNummer.getText().trim();
			lfdNummer.setText("");
			lfdNummer.setBackground(Color.LIGHT_GRAY);
		}

		ausgabeBildName.setEnabled(selected);
		lfdNummer.setEnabled(selected);
	}

	// ======================================================
	// setBilderVeraendertExportieren()
	//
	// Der RadioButton "Bilder ver�ndert exportieren" ist selektiert/nicht
	// selektiert.
	// (hier die check-boxen setzen)
	// ======================================================

	private void setPictureChanged(boolean selected) {	
		// vorne die CheckBoxen
		checkBoxCut.setEnabled(selected);	
		checkBoxRatio.setEnabled(selected);
		checkBoxRotateMirror.setEnabled(selected);
		checkBoxSolution.setEnabled(selected);
		checkBoxPicText.setEnabled(selected);	
		// overscan
		checkBoxOverscan.setEnabled(selected);
		if (selected) {
//			transparent.setEnabled(eigenOverscan.isSelected());
			overscanX.setEnabled(checkBoxOverscan.isSelected());
			overscanY.setEnabled(checkBoxOverscan.isSelected());
		}
		
		// der Rest
		cbAufloesung.setEnabled(selected);
		cbSeite.setEnabled(selected);
		buttonColorFG.setEnabled(selected);
		buttonColorBG.setEnabled(selected);
		
	}

	// ======================================================
	// doExportieren()
	//
	// Es wird aus dem indexView exportiert
	// ======================================================
	private void doExport() {

		List<PM_Picture> pictures = getIndex().controller.getPictureListDisplayed();
		if (pictures.isEmpty()) {
			return;
		}

		// ------------------------------------------------------------------
		// es gibt etwas zu exportieren
		// ------------------------------------------------------------------
		PM_Export exportieren = null;

//		if (eigenMitMetadaten.isSelected()) {
//			exportieren = new PM_ExportierenMitMetadaten(windowMain, this);
//		}
//		if (eigenLoeschen.isSelected()) {
//			exportieren = new PM_ExportierenMitMetadaten(windowMain, this);
//			exportieren.setLoeschen(true);
//		}
		if (eigenUnveraendert.isSelected()) {
			exportieren = new PM_ExportUnchanged(windowMain, this);
		}
		if (eigenVeraendert.isSelected()) {
			exportieren = new PM_ExportChanged(windowMain, this);
		}

		if (exportieren == null)
			return; // Fehler

		// Jetzt noch etwas setzen, was fuer alle gilt
		if (ausgabeNameVeraendert.isSelected()) {
			exportieren.setBildName(ausgabeBildName.getText());
			exportieren.setStartLfdBildNummer(PM_Utils.stringToInt(lfdNummer
					.getText()));
		}

		exportieren.setMitMetadaten(mitMetadaten.isSelected());
		// --------------------------------------------------------------------------
		// Jetzt alle Bilder exportieren
		// --------------------------------------------------------------------------
		String text = pictures.size() + " Bilder exportieren?";
		int n = JOptionPane.showConfirmDialog(null, text, "",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		exportieren.setPicturesToExport(pictures);
		exportieren.doExport();

		 

	}

	
	
	
	// ======================================================
	// doLoeschen()
	//
	// Es wird aus dem indexView exportiert
	// ======================================================
	private void doDeleteIrreversible() {

		List<PM_Picture> pictures = getIndex().controller.getPictureListDisplayed();
		if (pictures.isEmpty()) {
			return;
		}
		if (!windowMain.getWindowRechts().getWindowSequence().requestToClose()  ) {
			return;
		}
		
		
		
				 
//		String text = "Sollen die dargestellten " + pictures.size() + " Bilder \n    UNWIDERRUFLICH\ngel�scht werden?";
		String text = String.format(PM_MSG.getMsg("winExpDialogDel"), pictures.size());
		int n = JOptionPane.showConfirmDialog(this, text, PM_MSG.getMsg("winExpDialogMsg"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}

		// Zur Sicherheit:::
		// flush Metadaten  		
		PM_MetadataContainer.getInstance().flush();
		PM_DatabaseLucene.getInstance().flush();
		PM_SequencesInout.getInstance().flush();
		
		// ------------------------------------------------------------------
		// es gibt etwas zu l�schen
		// ------------------------------------------------------------------
		PM_DeletePictures deletePictures = new PM_DeletePictures();		
		deletePictures.deletePictures(pictures);
//		String message = "Es wurden " + pictures.size() + " Bilder gel�scht.";
		String message = String.format(PM_MSG.getMsg("winExpDialogDelEnd"), pictures.size());
		JOptionPane.showConfirmDialog(null, message,
				PM_MSG.getMsg("winExpDialogMsg"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		
		// flush Metadaten and reread		
 		windowMain.rereadAllThumbs();
		
		PM_MetadataContainer.getInstance().flush();
		PM_DatabaseLucene.getInstance().flush();
		PM_SequencesInout.getInstance().flush();
		
		deletePictures.fireDeleteListener(null);

	}
		
		
	
	// ======================================================
	// getSeitenVerhaeltnisse()
	// ======================================================
	private Vector<String> getSeitenVerhaeltnisse() {
		seitenVerhaeltnisse = new Vector<String>();
		seitenVerhaeltnisse.add("unver�ndert");
//		seitenVerhaeltnisse.add(" 4 : 3 ");
//		seitenVerhaeltnisse.add(" 3 : 2 ");
//		seitenVerhaeltnisse.add(" 16 : 9 ");
//	    seitenVerhaeltnisse.add(" 16 : 2 ");
		// seitenVerhaeltnisse.add("3:4");
		// seitenVerhaeltnisse.add("1:2");

		return seitenVerhaeltnisse;
	}

	public double getSeitenVerhaeltniss() {
		int item = cbSeite.getSelectedIndex();

		if (item <= 0) {
			return 0; // unver�ndert
		}
		String rString = (String) seitenVerhaeltnisse.elementAt(item);
		String[] sa = rString.split(":");
		if (sa.length != 2) {
			return 0; // Fehler
		}
		int h = PM_Utils.stringToInt(sa[0].trim());
		int b = PM_Utils.stringToInt(sa[1].trim());

		if (h == 0 || b == 0) {
			return 0; // fehler
		}
		double ratio = (double) b / (double) h;
		return ratio;

	}



	// ======================================================
	// doEigenUnveraendert()
	//
	// Der RadioButton "Bilder unveraendert exportieren" wurde gedrueckt:
	// Links alle Bilder 'original', d.h. weder gedreht noch ausgeschnitten
	// darstellen.
	// ======================================================
	private void doEigenUnveraendert() {
		setPictureChanged(false);
		getIndex().controller.setDrehenAusschneiden(false);

		getIndex().controller.setGlobalCutSize(0);
		// indexView.setPopUpAusschneiden(false);
		getIndex().controller.setPaintBildText(false);
		// jetzt noch   alle schreiben
		getIndex().controller.repaintViewport_deprecated();
	}

	// ======================================================
	// doEigenVeraendert()
	//
	// Der RadioButton "Bilder veraendert exportieren" wurde gedrueckt:
	// Links alle Bilder je nach Einstellungen darstellen.
	// ======================================================
	private void doEigenVeraendert() {
		setPictureChanged(true);
		// sperren
		getIndex().controller.setAusschneiden(checkBoxCut.isSelected());
		getIndex().controller.setDrehenSpiegeln(checkBoxRotateMirror.isSelected());
		getIndex().controller.setGlobalCutSize(getSeitenVerhaeltniss());
		// if (getSeitenVerhaeltniss() > 0) {
		// indexView.setPopUpAusschneiden(true);
		// } else {
		// indexView.setPopUpAusschneiden(false);
		// }

		// Alle Thumbs neu lesen und zeichnen
		getIndex().controller.setPaintBildText(checkBoxPicText.isSelected());
		getIndex().controller.rereadAllPictureViewThumbnail();
	}
 	
	// ============================================================
	// InnerClass: ComboBoxListener (das Seitenverhaeltnis hat sich geaendert)
	// ============================================================
	// ============================================================
	class ComboBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (eigenVeraendert.isSelected())
				doEigenVeraendert();
		}
	} // inner Class

} // Ende Klasse
