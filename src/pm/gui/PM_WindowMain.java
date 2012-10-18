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
import pm.print.*;
import pm.search.*;
import pm.utilities.*;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;
import pm.dragndrop.PM_PictureViewDragAndDrop;
import pm.index.*;
import pm.inout.*;

import java.util.*;
import java.util.List;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;


/**
 * Von JFrame abgeleitet. Laden aller Fenster und Initialisierungen vor
 * Darstellung.
 * 
 * (1) ZIP-Dateien zur�ckladen 
 * (2) Lucene-DB er�ffnen. Wenn Fehler, dann NICHT
 * er�ffnen. 
 * (3) Pr�fen Metadaten auf Konsistenz 
 * (4) Wenn Fehler, dann bereigen
 * (neue Metadaten ....) 
 * (5) Wenn Korrekturen oder Lucene-DB nicht er�ffnet,
 * dann Lucene neu erstellen 
 * (6) Alle Fenster erzeugen und darstellen (sichtbar
 * machen)
 * 
 */

@SuppressWarnings("serial")
public class PM_WindowMain extends JFrame implements PM_Interface {


	private PM_WindowLeftThumbnails windowThumbnails = null;
	private PM_WindowRightTabbedPane windowRechts = null;
	private PM_WindowStatusPanel statusPanel = null;
	private PM_ModifyExternal windowExternBearbeiten;
	private PM_WindowSlideshow windowDiaShow;
	private PM_CutPicture windowZeigenOriginal;

	private JDialog dialogBildAendernDrucken = null;
	private JDialog dialogZeigenOriginal;
	private JDialog dialogDiaShow;
	private JDialog dialogExternBearbeiten;

	private JPanel gesPanel = null;
	 
	private PM_Configuration einstellungen;

	private JSplitPane splitPane = null;

	 
	private static PM_WindowMain instance = null;

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_WindowMain( ) {
		super();
		String albumName = PM_Configuration.getInstance().getAlbumName();
		String title = "Photo Manager " + PM_Utils.getPmVersion() + " (Album: " + albumName + ")";
		setTitle(title);
		instance = this;
	}

	// ======================================================
	// getInstance()
	// ======================================================
	static public PM_WindowMain getInstance() {
		// null if not instatiated.
		// (do not instatiate here. Do not change this behaviour !!! )
		return instance;
	}

	// ======================================================
	// init()
	// ======================================================
	public void init(PM_ListenerX listener) {
		
		if (listener != null) {
			listener.actionPerformed(new PM_Action("max", 8));
		}
		
		einstellungen = PM_Configuration.getInstance();
		 
	 

		// JMenuItem startJavaHelp()

	
		// PM_LuceneDatenbank.getInstance().setWindowMain(this);

		// --------------------------------------------------------------------------
		// Alle Fenster erzeugen und initialisieren, aber noch nicht darstellen
		// --------------------------------------------------------------------------
		windowThumbnails = new PM_WindowLeftThumbnails();
		windowThumbnails.setName("windowThumbnails");
		statusPanel = PM_WindowStatusPanel.getInstance();
		
		// statusPanel.setStatus(); // das erste Mal den Status setzen
		windowRechts = new PM_WindowRightTabbedPane(this, listener);
		JScrollPane rechtsScrollPane = new JScrollPane();
		rechtsScrollPane.setViewportView(windowRechts);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				windowThumbnails, windowRechts); // rechtsScrollPane);
		
		int location = PM_All_InitValues.getInstance().getValueInt(this, "vertical-devider");
		if (location == 0) {
			location = 500;
		}
		splitPane.setDividerLocation(location); 
System.out.println("divider location: " + location);
 		splitPane.setOneTouchExpandable(true);
//splitPane.setEnabled(false);		


		PM_WindowMainMenu windowMenu = new PM_WindowMainMenu(this);		
		setJMenuBar(windowMenu.getMenuBar());
		statusPanel.init();
		// alles zusammenbasteln
		gesPanel = new JPanel();
		gesPanel.setLayout(new BorderLayout());
		gesPanel.add(splitPane, BorderLayout.CENTER);
		gesPanel.add(statusPanel, BorderLayout.SOUTH);
		setContentPane(gesPanel);

		// -----------------------------------------------------
		// Dialog-Fenster (modale: diashow, zeigen, ext.bearbeiten ...) anlegen
		// -----------------------------------------------------

		// --- Ausschneiden (DiaShow) ----
		dialogZeigenOriginal = new JDialog(this, true);
		windowZeigenOriginal = new PM_CutPicture(this,
				dialogZeigenOriginal);
		windowZeigenOriginal.setPreferredSize(PM_Utils.getScreenSize());
		dialogZeigenOriginal.setUndecorated(true);
		dialogZeigenOriginal.getContentPane().add(windowZeigenOriginal);
		dialogZeigenOriginal.pack();

		// --- DiaShow ----
		dialogDiaShow = new JDialog(this, true);
		windowDiaShow = new PM_WindowSlideshow(this, dialogDiaShow);
		Dimension d = PM_Utils.getScreenSize();
		
	 	windowDiaShow.setPreferredSize(new Dimension(1024-26, 768-26));
		
		windowDiaShow.setBounds(26, 0, 1000, 750);
//                int y,
//                int width,
//                int height)
//                top=0,left=26,bottom=26,right=0]
//screenSize. java.awt.Dimension[width=1024,height=768]
		
		
		dialogDiaShow.setUndecorated(true);
		dialogDiaShow.getContentPane().add(windowDiaShow);
		dialogDiaShow.pack();

		// --- Ausschneiden (drucken)
		dialogBildAendernDrucken = new JDialog(this, true);
		windowDruckenAendern = new PM_CutTempPrint(this,
				dialogBildAendernDrucken);
		windowDruckenAendern.setPreferredSize(PM_Utils.getScreenSize());
		dialogBildAendernDrucken.setUndecorated(true);
		dialogBildAendernDrucken.getContentPane().add(windowDruckenAendern);
		dialogBildAendernDrucken.pack();

		// --- externes Bildbearbeitungsprogramm ----
		dialogExternBearbeiten = new JDialog(this, true);
		windowExternBearbeiten = new PM_ModifyExternal(this,
				dialogExternBearbeiten);
		Dimension sizeExternBearbeiten = PM_Utils.getScreenSize();
		// Taskleiste muss sichtbar sein
		sizeExternBearbeiten.height = sizeExternBearbeiten.height - 20;
		windowExternBearbeiten.setPreferredSize(sizeExternBearbeiten);
		dialogExternBearbeiten.setUndecorated(true);
		dialogExternBearbeiten.getContentPane().add(windowExternBearbeiten);
		dialogExternBearbeiten.pack();

		/*
		 * *** Muss neu gemacht werden, wenn ?berhaupt sinnvoll und noch
		 * ben?tigt wird (1.9.2006) // ---- zu exportierendes Bild ausschneiden
		 * (?hnlich "zeigen Original") ---- dialogAusschneidenExport = new
		 * JDialog(this, true); ausschneidenExport = new
		 * PM_AusschneidenExport(this, dialogAusschneidenExport);
		 * ausschneidenExport.setPreferredSize(PM_Utils.getScreenSize());
		 * dialogAusschneidenExport.setUndecorated(true);
		 * dialogAusschneidenExport.getContentPane().add(ausschneidenExport);
		 * dialogAusschneidenExport.pack();
		 */
		// alle Lucene-Indizes dargstellen
//		PM_LuceneLists luceneIndexInstance = PM_LuceneLists.getInstance();
//		luceneIndexInstance.fireAllListener();

		// ----------------------------------------------------
		// ... und ganz zum Schluss: Jetzt alles darstellen !!!!!!!!!!
		// ---------------------------------------------------
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
				
			}
		});

		pack();

//		Rectangle position = PM_XML_Alle_InitValues_deprecated.getInstance()
//				.getMainWindowPosition();
		 
		
		int x = PM_All_InitValues.getInstance().getValueInt(this, "positionX");
		int y = PM_All_InitValues.getInstance().getValueInt(this, "positionY");
		int w = PM_All_InitValues.getInstance().getValueInt(this, "positionW");
		int h = PM_All_InitValues.getInstance().getValueInt(this, "positionH");
		if (w > 0) {
			Rectangle position = new Rectangle(x,y,w,h);
			setSize(position.width, position.height);
			setLocation(new Point(position.x, position.y));
		}

		// --------------------------------------------------------
		// Key Bindigs
		// -------------------------------------------------------
		keyBindingsForFocusPanel(this);
		keyBindingsForDiashow(this);
		keyBindingsForChangeMainWindow(this);
		keyBindingsForEnterFocusOwner(this);
		keyBindingsForClose(this);
		keyBindingsForTabChange(this);

		keyBindingsForHelpSet(this);

		/*
		 * 
		 * PM_KeyboardBindings kb = new PM_KeyboardBindings(this);
		 * kb.printAllBindings(gesPanel);
		 * 
		 * 
		 * 
		 * kb.printAllBindings(new JTabbedPane()); kb.printAllBindings(new
		 * JSplitPane()); kb.printAllBindings(new JComboBox());
		 * kb.printAllBindings(new JTextField()); kb.printAllBindings(new
		 * JButton()); kb.printAllBindings(new JSlider());
		 * kb.printAllBindings(new JMenu()); kb.printAllBindings(new
		 * JMenuItem()); kb.printAllBindings(new JRootPane());
		 * kb.printAllBindings(new JOptionPane()); kb.printAllBindings(new
		 * JTree()); kb.printAllBindings(new JList());
		 * 
		 * kb.printAllBindings(new JDesktopPane()); kb.printAllBindings(new
		 * JRadioButton()); kb.printAllBindings(new JDesktopPane());
		 * 
		 */

		// Object[] ob =
		// windowRechts.getInputMap(JComponent.WHEN_FOCUSED).allKeys();
		// System.out.println(" INPUP MAP windowRechts = \n " +
		// windowRechts.getInputMap(JComponent.WHEN_FOCUSED).allKeys());
		// OHNE Erfolg
		// SwingUtilities.replaceUIInputMap(windowRechts,JComponent.WHEN_FOCUSED,
		// null );
		// SwingUtilities.replaceUIInputMap(windowRechts,JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
		// null );
		// SwingUtilities.replaceUIInputMap(windowRechts,JComponent.WHEN_IN_FOCUSED_WINDOW,
		// null );
		// --------------------------------------------------------
		// Focus:
		// Traversal Keys: nur Tab und Tab+shift
		// -------------------------------------------------------
		/*
		 * ////// Funktioniert nicht !!!!! Set newForwardKeys = new HashSet();
		 * newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		 * setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
		 * newForwardKeys); Set newBackwardKeys = new HashSet();
		 * newBackwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_A,
		 * KeyEvent.SHIFT_MASK));
		 * setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
		 * newBackwardKeys);
		 */
		// --------------------------------------------------------
		// Focus:
		// -- nach dem Start soll hier der Focus gesetzt werden
		// -- Neue Focus Traversal Policy setzen
		// --------------------------------------------------------
		Component focus = windowRechts.getWindowSuchen().initRequestFocus();
		setAlterFocus(focus);

		PM_FocusTraversalPolicy ftp = new PM_FocusTraversalPolicy(this);
		setFocusTraversalPolicy(ftp);

		// --------------------------------------------------------
		// PropertyChangeListener
		// --------------------------------------------------------

		KeyboardFocusManager focusManager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();
				Component neuerFocus = (Component) e.getNewValue();
				if ("focusOwner".equals(prop) && neuerFocus != null) {
					focusChanged(neuerFocus);
				}
			}
		});

		 
		
		 
		// ---- damit die Sequencen zur�ckgeschrieben werden ----
		PM_SequencesInout.getInstance().setChangeListener();
 
		// --------------------------------------------------------
		// Nun ist aber wirklich alles vorbereitet und es kann losgehen
		// --------------------------------------------------------

		windowThumbnails.getIndex().controller.initEnd();
		// ---- im Fenster suchen den tree selectieren ------
		windowRechts.getWindowSuchen().initSelectTree();
		windowRechts.getWindowSuchen().initTreeOriginal();
			 
		statusPanel.updateAnzahlBilder();
		System.out.println("--- Beginn Verarbeitung ------");


 
		// fertig !!!
		setVisible(true);

	}

	
	// ======================================================
	// close()
	// ======================================================
	public void close() {
		if (!requestToClose()) {
			return;
		}

		int abfrage;
		// ask to delete K4 pictures
		String searchString = PM_LuceneDocument.LUCENE_QS + ":4";

		PM_Search search = new PM_Search(new PM_SearchExpr(SearchType.NORMAL,
				searchString));
		search.search();
		List<PM_Picture> listK4 = search.getPictureList(SearchSortType.TIME);

		if (!listK4.isEmpty()) {
			String msg = "Es gibt " + listK4.size() + " Bilder mit K4. Diese l�schen ?";
			abfrage = JOptionPane.showConfirmDialog(null,  
					msg, "Terminate", JOptionPane.YES_NO_OPTION);

			if (abfrage == JOptionPane.YES_OPTION) {
				// now select subwindow "export" and view the pictures
				windowRechts.deletePictures(listK4);
				return;
			}
		}
		
		
		abfrage = JOptionPane.showConfirmDialog(null, PM_MSG
				.getMsg("winMainExit"), "Terminate", JOptionPane.YES_NO_OPTION);

		if (abfrage == JOptionPane.NO_OPTION) {
			return;
		}
		doClose();
		setVisible(false);
		dispose();
		System.out.println("PhotoManager terminated");
		System.exit(0);
	}
 
	
	// ======================================================
	// keyBindingsForTabChange()
	//
	// Wechsel der rechten Hauptfenster (Reiter-Wechsel)
	// ======================================================
	private void keyBindingsForTabChange(PM_WindowMain windowMain) {

		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = gesPanel.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK),
				"VK_Suchen");
		Action aVK_Suchen = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_SEARCH);
			}
		};
		actionMap.put("VK_Suchen", aVK_Suchen);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK),
				"VK_Bild");
		Action aVK_Bild = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_ZEIGEN_EINZEL);
			}
		};
		actionMap.put("VK_Bild", aVK_Bild);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK),
				"VK_Gruppe");
		Action aVK_Gruppe = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_ZEIGEN_GRUPPE);
			}
		};
		actionMap.put("VK_Gruppe", aVK_Gruppe);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK),
				"VK_Serie");
		Action aVK_Serie = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_SEQUENCE);
			}
		};
		actionMap.put("VK_Serie", aVK_Serie);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK),
				"VK_Drucken");
		Action aVK_Drucken = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_DRUCKEN);
			}
		};
		actionMap.put("VK_Drucken", aVK_Drucken);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK),
				"VK_Export");
		Action aVK_Export = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_EXPORTIEREN);
			}
		};
		actionMap.put("VK_Export", aVK_Export);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK),
				"VK_Info");
		Action aVK_Info = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				reiter(TAB_INFO_BILD);
			}
		};
		actionMap.put("VK_Info", aVK_Info);

	}

	// ======================================================
	// keyBindingsForEnterFocusOwner()
	//
	// Enter-Taste auf den FocusOwner
	// ======================================================
	private void keyBindingsForEnterFocusOwner(PM_WindowMain windowMain) {
		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = gesPanel.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
		Action aVK_ENTER = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				enterPressed();
			}
		};
		actionMap.put("VK_ENTER", aVK_ENTER);
	}

	// ======================================================
	// keyBindingsForClose()
	//
	// F12 f�r Close PM (Anwendung wird �ber F12 beendet)
	// ======================================================
	private void keyBindingsForClose(PM_WindowMain windowMain) {
		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = gesPanel.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "VK_F12");
		Action aVK_F12 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		};
		actionMap.put("VK_F12", aVK_F12);
	}

	// ======================================================
	// keyBindingsForChangeMainWindow()
	//
	// Zwischen linkem und rechtem Hauptfenster wechseln
	// ======================================================
	private void keyBindingsForChangeMainWindow(PM_WindowMain windowMain) {
		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = gesPanel.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				KeyEvent.CTRL_MASK), "VK_CTRL_ENTER");

		Action aVK_CTRL_ENTER = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ctrlEnterPressed();
			}
		};
		actionMap.put("VK_CTRL_ENTER", aVK_CTRL_ENTER);
	}

	// ======================================================
	// keyBindingsForHelpSet()
	//
	// mit F1 wir das Help-System aufgerufen
	// ======================================================
	private void keyBindingsForHelpSet(PM_WindowMain windowMain) {
		PM_Help help = PM_Help.getInstance();
		
		if (help == null) {
			// kein Help-System
//			return;
		}
//		final ActionListener alHelp = help.getActionListener();
		// Input-map
		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");

		// Action-Map
		Action aVK_Help = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				
				String msg = PM_MSG.getMsg("menuHelp1")  
				+PM_MSG.getMsg("menuHelp2")  
				+PM_MSG.getMsg("menuHelp3") 
				+PM_MSG.getMsg("menuHelp4")  
				+PM_MSG.getMsg("menuHelp5");
			
			
			JOptionPane.showConfirmDialog(null, msg, "Help",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
				
//				alHelp.actionPerformed(e);
			}
		};
		ActionMap actionMap = gesPanel.getActionMap();
		actionMap.put("help", aVK_Help);

	}

	// ======================================================
	// keyBindingsForFocusPanel()
	//
	// Traversieren innerhalb eines PM_FocusPanels: Crtl+Pfeiltasten
	//
	// Achtung: Es muessen sowohl f�r links als auch rechts keybindings erstellt
	// werden,
	// da, wenn gesPanel, auch die Tabs eingeschlossen werden. (warum???)
	// ======================================================
	private void keyBindingsForFocusPanel(PM_WindowMain windowMain__) {
		// InputMap inputMap =
		// gesPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// //WHEN_IN_FOCUSED_WINDOW);
		// ActionMap actionMap = gesPanel.getActionMap();

		KeyStroke ksRechts = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				KeyEvent.CTRL_MASK);
		KeyStroke ksLinks = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				KeyEvent.CTRL_MASK);
		KeyStroke ksUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP,
				KeyEvent.CTRL_MASK);
		KeyStroke ksDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
				KeyEvent.CTRL_MASK);
		Action aVK_RIGHT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				pfeil(KeyEvent.VK_RIGHT);
			}
		};
		Action aVK_LEFT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				pfeil(KeyEvent.VK_LEFT);
			}
		};
		Action aVK_UP = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				pfeil(KeyEvent.VK_UP);
			}
		};
		Action aVK_DOWN = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				pfeil(KeyEvent.VK_DOWN);
			}
		};

		InputMap inputMapR = windowRechts
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		// //WHEN_FOCUSED);
		inputMapR.put(ksRechts, "VK_RIGHT");
		inputMapR.put(ksLinks, "VK_LEFT");
		inputMapR.put(ksUp, "VK_UP");
		inputMapR.put(ksDown, "VK_DOWN");

		ActionMap actionMapR = windowRechts.getActionMap();
		actionMapR.put("VK_RIGHT", aVK_RIGHT);
		actionMapR.put("VK_LEFT", aVK_LEFT);
		actionMapR.put("VK_UP", aVK_UP);
		actionMapR.put("VK_DOWN", aVK_DOWN);

		InputMap inputMapL = windowThumbnails
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		// //WHEN_FOCUSED);
		inputMapL.put(ksRechts, "VK_RIGHT");
		inputMapL.put(ksLinks, "VK_LEFT");
		inputMapL.put(ksUp, "VK_UP");
		inputMapL.put(ksDown, "VK_DOWN");

		ActionMap actionMapL = windowThumbnails.getActionMap();
		actionMapL.put("VK_RIGHT", aVK_RIGHT);
		actionMapL.put("VK_LEFT", aVK_LEFT);
		actionMapL.put("VK_UP", aVK_UP);
		actionMapL.put("VK_DOWN", aVK_DOWN);

	}

	// ======================================================
	// keyBindingsForDiashow()
	//
	// Achtung: F�r "mainWindow" UND "windowDiaShow" (Dialog)
	//
	// F2 manuel
	// F3 automatisch sequentiell
	// F4 automatisch random
	// ======================================================
	private void keyBindingsForDiashow(PM_WindowMain windowMain__) {

		KeyStroke ksNormal = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		KeyStroke ksSequent = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		KeyStroke ksRandom = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);

		// ------------------------------------------------------------
		// Binden an das "mainWindow" (KEIN Dialog)
		// ------------------------------------------------------------
		InputMap inputMap = gesPanel
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); // WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = gesPanel.getActionMap();

		Action actionNormal = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				diashow(DIASHOW_NORMAL);
			}
		};
		Action actionSequent = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				diashow(DIASHOW_AUTOM_SEQUENT);
			}
		};
		Action actionRandom = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				diashow(DIASHOW_AUTOM_RANDOM);
			}
		};

		// PM_Utils.removeKeyCode(gesPanel, KeyEvent.VK_F2);

		inputMap.put(ksNormal, STR_DIASHOW_NORMAL);
		inputMap.put(ksSequent, STR_DIASHOW_AUTOM_SEQUENT);
		inputMap.put(ksRandom, STR_DIASHOW_AUTOM_RANDOM);

		actionMap.put(STR_DIASHOW_NORMAL, actionNormal);
		actionMap.put(STR_DIASHOW_AUTOM_SEQUENT, actionSequent);
		actionMap.put(STR_DIASHOW_AUTOM_RANDOM, actionRandom);

		// ------------------------------------------------------------
		// Binden an die "windowDiaShow" (DIALOG)
		// ------------------------------------------------------------
		InputMap inputMapDiashow = windowDiaShow.getInputMap(); // JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// //WHEN_IN_FOCUSED_WINDOW);
		inputMapDiashow.put(ksNormal, STR_DIASHOW_NORMAL);
		inputMapDiashow.put(ksSequent, STR_DIASHOW_AUTOM_SEQUENT);
		inputMapDiashow.put(ksRandom, STR_DIASHOW_AUTOM_RANDOM);

		ActionMap actionMapDiashow = windowDiaShow.getActionMap();
		Action aDiaNormal = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowDiaShow.keyPressedFkt(DIASHOW_NORMAL);
			}
		};
		Action aDiaSeqent = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowDiaShow.keyPressedFkt(DIASHOW_AUTOM_SEQUENT);
			}
		};
		Action aDiaRandom = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowDiaShow.keyPressedFkt(DIASHOW_AUTOM_RANDOM);
			}
		};
		actionMapDiashow.put(STR_DIASHOW_NORMAL, aDiaNormal);
		actionMapDiashow.put(STR_DIASHOW_AUTOM_SEQUENT, aDiaSeqent);
		actionMapDiashow.put(STR_DIASHOW_AUTOM_RANDOM, aDiaRandom);
		// ------------------------------------------------------------
		// Binden an die "windowZeigenOriginal" (DIALOG)
		// ------------------------------------------------------------
		InputMap inputMapAusschneiden = windowZeigenOriginal.getInputMap();
		inputMapAusschneiden.put(ksNormal, STR_DIASHOW_NORMAL);
		inputMapAusschneiden.put(ksSequent, STR_DIASHOW_AUTOM_SEQUENT);
		inputMapAusschneiden.put(ksRandom, STR_DIASHOW_AUTOM_RANDOM);

		ActionMap actionMapAusschneiden = windowZeigenOriginal.getActionMap();
		Action aAusNormal = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowZeigenOriginal.keyPressedFkt(DIASHOW_NORMAL);
			}
		};
		Action aAusSeqent = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowZeigenOriginal.keyPressedFkt(DIASHOW_AUTOM_SEQUENT);
			}
		};
		Action aAusRandom = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				windowZeigenOriginal.keyPressedFkt(DIASHOW_AUTOM_RANDOM);
			}
		};
		actionMapAusschneiden.put(STR_DIASHOW_NORMAL, aAusNormal);
		actionMapAusschneiden.put(STR_DIASHOW_AUTOM_SEQUENT, aAusSeqent);
		actionMapAusschneiden.put(STR_DIASHOW_AUTOM_RANDOM, aAusRandom);

	}

	// ======================================================
	// set/getJustChanged()
	//
	// F�r den Fall, das ein rechtes Fenster (�ber Reiter oder Ctrl+..)
	// gewechselt wurde.
	// (in einem solchen Falle gibt es keinen Property-Listener - Aufruf)
	// ======================================================
	private PM_WindowBase justChanged = null;

	public void setJustChanged(PM_WindowBase justChanged) {
		this.justChanged = justChanged;
	}

	// ======================================================
	// set/getAlterFocus()
	// ======================================================
	private Component focusOld = null;

	private void setAlterFocus(Component focusOld) {
		this.focusOld = focusOld;
	}

	private Component getAlterFocus() {
		return focusOld;
	}

	// ======================================================
	// focusChanged()
	//
	// (Aufruf vom PropertyListener, d.h. es gibt einen neuen Focus)
	// Neue Focus:
	// u.a. hier alterFocus versorgen
	// ======================================================
	private void focusChanged(Component neuerFocus) {

		// Wenn WindowBase gewechselt, dann hier neuen Focus setzen.
		// F�r den Fall, das ein rechtes Fenster (�ber Reiter oder Ctrl+..)
		// gewechselt wurde.
		// (in einem solchen Falle gibt es keinen Property-Listener - Aufruf)
		// (muss als erstes ausgef�hrt werden)
		if (justChanged != null) {
			Component oldFocus = justChanged.getAktiverFocus();
			if (oldFocus != null)
				oldFocus.requestFocusInWindow();
			justChanged = null;
		}

		Component alterFocus = getAlterFocus();
		if (neuerFocus == alterFocus) {
			return; // keine �nderung (Focus alt ist gleich Focus neu)
		}
		PM_WindowBase wbNeu = getWindowBaseWithFocusOwner(neuerFocus);
		if (wbNeu == null) {
			return; // Neu ist weder links noch rechts
		}

		// Hier in WindowMain den neuen Focus merken
		setAlterFocus(neuerFocus);

		// Im (neuen) PM_WindowBase den neuen Focus merken
		wbNeu.setAktiverFocus(neuerFocus);

		// holen beide PM_FocusPanel (alt und neu)
		PM_FocusPanel fcAlt = getFocusPanel(alterFocus);
		PM_FocusPanel fcNeu = getFocusPanel(neuerFocus);

		 
		if (fcNeu != null)
			 

		// Im PM_FocusPanel den neuen Focus sicherstellen
		if (fcNeu != null)
			fcNeu.setLastFocus(neuerFocus);

		// BG-color vom PM_FocusPanel setzen
		if (fcAlt != fcNeu) {
			if (fcAlt != null) {
				fcAlt.setBackgroundColor(PM_WindowBase.COLOR_BG_PANEL);
			}
			if (fcNeu != null) {
				fcNeu.setBackgroundColor(PM_WindowBase.COLOR_BG_PANEL_SEL);
			}
		}

		// jetzt noch BG-Color vom alten und neuen Focus setzen (nur vom Focus)
		setBackgroundColor(alterFocus, PM_WindowBase.COLOR_ENABLED,
				PM_WindowBase.COLOR_BG_PANEL);
		setBackgroundColor(neuerFocus, PM_WindowBase.COLOR_BG_FOCUS,
				PM_WindowBase.COLOR_BG_PANEL_SEL);

	}

	// =====================================================
	// setBackgroundColor()
	// 
	// (Aufruf vom PropertyListener, d.h. es gibt einen neuen Focus)
	// =====================================================
	private void setBackgroundColor(Component component, Color colorFocus,
			Color colorBackground) {
		if (component instanceof JTree) {
			JTree tree = (JTree) component;
			 

			tree.setBackground(colorBackground);
			return;
		}
		if (component instanceof JList) {
			((JList) component).setSelectionBackground(colorFocus);
			((JList) component).setBackground(colorBackground);
			return;
		}

		// *******************************
		// DAS FOLGENDE NOCH UNTERDSUCHEN: ÜBERSETZUNGSFEHLER !!!!!!!!!!!!!!!!!!
		/*
		if (component instanceof PM_IndexView) {
			PM_Index iV = ((PM_Index) component);
			iV.setBackground(colorBackground);
			PM_Picture picSelected = iV.getFirstPictureSelected();
			if (picSelected != null && iV.hasPictureView(picSelected)) {
				PM_PictureView pV = iV.getPictureView(picSelected);  
				pV.requestFocusInWindow();
			}
			return;
		}
*/
		if (component instanceof PM_PictureView ) {
			PM_PictureView  pV = ((PM_PictureView ) component);
			PM_Index iV = pV.getIndexView();
// ?????????????			iV.setBackground(colorBackground);
			return;
		}

		component.setBackground(colorFocus);
	}

	// =====================================================
	// getFocusPanel()
	//
	// Achtung: Aufruf NUR vom PropertyListener, da Sonderbehandlung f�r
	// IndexView !!
	// 
	// (Wenn IndexView ist kein FocusPAnel direkt zu finden)
	//
	// return null: nigefu
	// =====================================================
	private PM_FocusPanel getFocusPanel(Component component) {
		if (component == null)
			return null;
		Component parent = component.getParent();
		PM_Index iV = null;
		while (parent != null) {
			if (parent instanceof PM_FocusPanel) {
				return (PM_FocusPanel) parent;
			}
	// ****************** folgendes gibt Übersezzungsfehler !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// KORRIGIEREN !!!!!!!!!!!!!!!!!!!!!!
	//		if (parent instanceof PM_Index) {
	//			iV = (PM_Index) parent;
	//			return iV.getFocusPanel();
	//		}
			parent = parent.getParent();
		}
		return null;
	}

	// ======================================================
	// keyTextFieldPressed()
	//
	// in einem JTextField wurde eine Taste gedr�ckt.
	// Da hier Crtl+left/right nicht abgefangen werden kann,
	// muss es hier herausgefiltert werden.
	//
	// HACK #1 !!!!!!
	// ======================================================
	public JTextField textFieldPressed = null;

	public void keyTextFieldPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT)
				&& (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
			// System.out.println(" --- ber Keylistener abgefangen ");
			if (textFieldPressed != null) {
				// HACK #1
				textFieldPressed = null;
				return;
			}
			pfeil(key);
		}
	}

	// ======================================================
	// getFocusPanelWithFocusOwner()
	//
	// liefert den PM_FocusPanel, in dem sich z.Zt. der
	// FocusOwner befindet.
	// ======================================================
	public PM_FocusPanel getFocusPanelWithFocusOwner() {
		PM_WindowBase windowBase = getWindowBaseWithFocusOwner();
		if (windowBase == null)
			return null;
		return windowBase.getFocusPanelWithFocusOwner();
	}

	public PM_FocusPanel getFocusPanelWithFocusOwner(Component focus) {
		PM_WindowBase windowBase = getWindowBaseWithFocusOwner(focus);
		if (windowBase == null)
			return null;
		return windowBase.getFocusPanelWithFocusOwner(focus);
	}

	// ======================================================
	// pfeil()
	//
	// Navigieren mit den Pfeiltasten in der Instanz von der Klasse
	// PM_FocusPanel,
	// die gerade den focusOwner hat.
	// ======================================================
	private void pfeil(int pfeilTasten) {
		// System.out.println("###### WindowMain: Pfeiltaste zum Navigieren
		// gedr�ckt = ");
		Component focusOwner = this.getFocusOwner();

		// PM_FocusPanel focusPanel = getFocusPanelWithFocusOwner();
		PM_FocusPanel focusPanel = getFocusPanel(focusOwner);
		if (focusPanel == null)
			return; // nigefu

		Component focus = focusPanel
				.getNextFocus(focusOwner, pfeilTasten, this /* this ist HACK # 1 */);
		if (focus == null)
			return; // nigefu

		focus.requestFocusInWindow();

	}

	// ======================================================
	// diashow()
	//
	// Aurufen der Diashow �ber Tasten.
	// Mode: DIASHOW_NORMAL
	// DIASHOW_AUTOM_SEQUENT
	// DIASHOW_AUTOM_RANDOM
	// ======================================================
	public void diashow(int mode) {

		Component focusOwner = this.getFocusOwner();
		PM_WindowBase windowBase = getWindowBaseWithFocusOwner(focusOwner);
		if (windowBase == null) {
			return;
		}

		// Aufruf im Fenster "Suchen"
		if (windowBase instanceof PM_WindowSearch) {
			PM_WindowSearch windowSuchen = (PM_WindowSearch) windowBase;
			windowSuchen.startDiashow(mode);
		}

	}

 
	

	// ======================================================
	// reiter()
	//
	// �ber Ctrl+... wird ein neues rechtes Fenster angefordert.
	// ======================================================
	private void reiter(int index) {
		windowRechts.setSelectedIndex(index);
	}

	// ======================================================
	// getWindowBaseWithFocusOwner() (mit und ohne FocusOwner-�bergabe)
	//
	// Liefert das PM_WindowBase in dem sich der FocusOwner befindet
	// ======================================================
	public PM_WindowBase getWindowBaseWithFocusOwner(Component focusOwner) {
		if (PM_Utils.isAncestorOf(focusOwner, windowThumbnails)) {
			return windowThumbnails;
		} else if (PM_Utils.isAncestorOf(focusOwner, windowRechts)) {
			return windowRechts.getWindowSelected();
		}
		return null; // nigefu

	}

	public PM_WindowBase getWindowBaseWithFocusOwner() {
		Component focusOwner = this.getFocusOwner();
		return getWindowBaseWithFocusOwner(focusOwner);
	}

	// ======================================================
	// ctrlEnterPressed()
	//
	// Wechsel vom linken Hauptfenster zum rechten Hauptfenster
	// ======================================================
	private void ctrlEnterPressed() {
		Component focus = this.getFocusOwner();
		if (focus == null)
			return;

		if (PM_Utils.isAncestorOf(focus, windowThumbnails)) {
			// System.out.println(" Focus Owner is windowThumbnails");

			Component aFocus = windowRechts.getWindowSelected()
					.getAktiverFocus();
			if (aFocus != null)
				aFocus.requestFocusInWindow();

			return;
		}
		if (PM_Utils.isAncestorOf(focus, windowRechts)) {
			// System.out.println(" Focus Owner is windowRechts");
///			windowThumbnails.getIndex().requestFocusInWindow();
			return;
		}

	}

	// ======================================================
	// enterPressed()
	//
	// Den Focus hat ein Button oder eine Checkbox.
	// Durch dr�cken der Enter-Taste soll hier ein Click simunliert werden.
	// ======================================================
	private void enterPressed() {
		Component focus = this.getFocusOwner();
		if (focus == null)
			return;

		if (focus instanceof JButton) {
			((JButton) focus).doClick();
		} else if (focus instanceof JCheckBox) {
			((JCheckBox) focus).doClick();
		}

	}

 
	
	// ======================================================
	// getWindowThumbnails()
	// ======================================================
	public PM_WindowLeftThumbnails getWindowLeft() {
		return windowThumbnails;
	}

	// ======================================================
	// getWindowRightSelected()
	// ======================================================
	public PM_WindowBase getWindowRightSelected() {
		return windowRechts.getWindowSelected();
	}

	 
	// =====================================================================
	// flushPictureViewThumbnail()
	//
	// Wenn es ein PictureView gibt wird dort das Thumbnail 
	// gel�scht.
	// Erforderlich, wenn �nderungen im Drehen, spiegeln ... erforderlich sind.
	// ======================================================================
	public void rereadPictureViewThumbnail(PM_Picture picture) {	
		windowRechts.flushPictureViewThumbnail(picture);
		windowThumbnails.rereadPictureViewThumbnail(picture);
	}
	
	
	
	// ======================================================
	// getWindowRechts()
	// ======================================================
	public PM_WindowRightTabbedPane getWindowRechts() {
		return windowRechts;
	} 
	
	/**
	 * setEnabled of JTabbedPane in class PM_WindowRightTabbedPane.
	 * 
	 * This is needed for import.
	 *  
	 */
	public void setEnabledWindowRightTabbedPane(boolean enabled) {
		windowRechts.setEnabled(enabled);
	}
	
	// ======================================================
	// getSelectedTab()
	//
	// ======================================================
	public int getSelectedTab() {
		return windowRechts.getSelectedTab();
	}

	// ======================================================
	// getIndexViewThumbnails()
	// ======================================================
	public PM_Index   getIndexViewThumbnails() {
		return windowThumbnails.getIndex() ;
	}
	public PM_Index   getIndexViewWindowRight() {
		return  getWindowRightSelected().getIndex();
	}

	/**
	 * requestToClose()
	 */
	public boolean requestToClose() {
		return windowRechts.requestToClose();
	}

	public void closeAlbum() {
		
		windowThumbnails.closeAlbum();
		 windowRechts.closeAlbum();
	}

	
	public void rereadAllThumbs() {
		
		windowThumbnails.rereadAllThumbs();
		 windowRechts.rereadAllThumbs();
	}
	/**
	 * flush and close all displayed thumb on all windows
	 * 
	 *
	 */
	public boolean flushAndCloseDisplay() {
		
		
		if (!requestToClose() )  {
			return false;
		}				
		
		PM_MetadataContainer.getInstance().flush();		 				
		PM_DatabaseLucene.getInstance().flush();		 		
		PM_SequencesInout.getInstance().flush();
		 
		
		if (!windowThumbnails.flush()) {
			return false;
		}
		if (!windowRechts.flush()) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Anwendung beenden
	 */
	private void doClose() {
		
		PM_All_InitValues.getInstance().putValueInt(this, "positionX",getLocation().x);
		PM_All_InitValues.getInstance().putValueInt(this, "positionY",getLocation().y);
		PM_All_InitValues.getInstance().putValueInt(this, "positionW",getSize().width);
		PM_All_InitValues.getInstance().putValueInt(this, "positionH",getSize().height);
		
		
		
		PM_All_InitValues.getInstance().putValueInt(this, "vertical-devider",
				splitPane.getDividerLocation());
		
		
		
		// ALLE Windows benachrichtigen
		windowRechts.close(); // alle rechten Fenster
		windowThumbnails.close(); // linkes Fenster
				 
		// Delete irreversible all invalid pictures and metadata.
		int size = 0;
		for (PM_PictureDirectory pd: PM_MetadataContainer.getInstance().getPictureDirectories()) { 
			  size += pd.deletePictureInvalid();
		}
		
		
		PM_MetadataContainer.getInstance().close();		 				
		PM_DatabaseLucene.getInstance().close();		 		
		PM_SequencesInout.getInstance().close();
		PM_All_ExternalPrograms.getInstance().close();	 
		PM_XML_File_Session.getInstance().close();
		einstellungen.close();
		
		
		
		PM_All_InitValues.getInstance().close();
		PM_LogFile.getInstance().close();

		// Jetzt ist wirklich schluss !!!!!!!

	}


	/**
	 * Set divider location of split pane
	 */
	public void setDividerLocation(int location) {
		splitPane.setDividerLocation(location);
	}

	// ======================================================
	// doBildAendernDrucken()
	//
	// Bild vor dem Drucken tempor�r �ndern (in einem modalen Dialog)
	//
	// return true: es wurden �nderungen vorgenommen
	// ======================================================
	private PM_CutTempPrint windowDruckenAendern = null;

	public PM_CutTempPrint getDialogDruckenAendern() {
		return windowDruckenAendern;
	}

	public boolean doBildAendernDrucken(PM_PicturePrint pictureDruckdaten) {

		windowDruckenAendern.start(pictureDruckdaten);

		dialogBildAendernDrucken.setVisible(true);
		// warten solange bis dispose() aufgerugen wird

		return windowDruckenAendern.getReturnStatus();

	}



	// ======================================================
	// closeBildAendern() (Aendern beim DRUCKEN !!!)
	//
	// Bild Aendern ist ein JDialog.
	// Diese Methode wird daher vom windowAnedern aufgerufen,
	// um diesen JDialog mit dispose zu beenden.
	// ======================================================
	public void closeBildAendernDrucken() {
		dialogBildAendernDrucken.dispose();
	}

	// ======================================================
	// doBildOriginalZeigen()
	//
	// Bild in Originalgroesse zeigen (und Ausschneide-M?glichkeit ohne Drucken)
	// ======================================================
	public PM_Picture  doBildZeigenOriginal(PM_Picture  picture ,
			List<PM_Picture> pictureList) {

		if (!windowZeigenOriginal.start(picture , pictureList))
			return picture; // nicht darstellbar
		dialogZeigenOriginal.setVisible(true);
		// warten solange bis dispose() aufgerugen wird
		PM_Picture  pic = windowZeigenOriginal.getPicture ();
		// Jetzt kann ggf. die DiaShow aufgerufen werden
		if (windowZeigenOriginal.getDiaShowAufrufen()) {
			pic = doDiaShow(pic, pictureList, DIASHOW_NORMAL);
		}

		// alle prefetches l�schen
		PM_Picture.readImageOriginal(new ArrayList<PM_Picture>(),
				new ArrayList<PM_Picture>());

		return pic;
	}

	// ======================================================
	// doDiaShow()
	// ======================================================
	public PM_Picture  doDiaShow(PM_Picture  picture ,
			List<PM_Picture> pictures, int mode) {

		if (!windowDiaShow.start(picture , pictures, mode)) {
			return picture ; // nicht darstellbar
		}
		dialogDiaShow.setVisible(true);

		// warten solange bis dispose() aufgerugen wird
		PM_Picture  pic = windowDiaShow.getPicture ();
		// Jetzt kann ggf. der Dialog "BildZeigen" (zum Aendern des
		// Ausschnittes) angezeigt werden
		if (windowDiaShow.getAendernAufrufen()) {
			pic = doBildZeigenOriginal(pic, pictures );
		}

		// alle prefetches l�schen
		PM_Picture.flushAllImagesOriginal();
 


		return pic;
	}

	
	
 
	
	
	
	
	
	
	
	
	// ======================================================
	// doExternBearbeiten()
	//
	// Bild in Originalgroesse zeigen (und Ausschneide-M�glichkeit ohne Drucken)
	// ======================================================
	public void doExternBearbeiten(PM_Picture picture) {

		if (!windowExternBearbeiten.start(picture)) {
			return; // nicht darstellbar
		}
		dialogExternBearbeiten.setVisible(true);

		return;
	}

	
	

}
