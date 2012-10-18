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

import java.awt.Toolkit;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;
 

 
import pm.index.PM_Index;
import pm.inout.*;
 
import pm.picture.*;

import pm.search.*;
 
import pm.utilities.*;
import pm.utilities.PM_Configuration.BackUp;
 

/**
 * Create the menu bar with all action methods.
 * 
 * 
 * 
 * 
 * @author dih
 * 
 */
public class PM_WindowMainMenu implements PM_Interface {

	private PM_WindowMain windowMain;

	private JMenuBar menuBar;

	private PM_Configuration einstellungen;

	/**
	 * Creates the menu bar for the main window
	 * 
	 * @param windowMain
	 */
	public PM_WindowMainMenu(PM_WindowMain windowMain) {
		this.windowMain = windowMain;
		einstellungen = PM_Configuration.getInstance();
		init();

		// Change Listener fï¿½r message
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
		// Menu File
		menuDatei.setText(PM_MSG.getMsg("menuFile"));
		miClose.setText(PM_MSG.getMsg("miClose"));
		// menu Search
		menuSuchen.setText(PM_MSG.getMsg("menuSearch"));
		menuItemDoppelte.setText(PM_MSG.getMsg("miDouble"));
		menuItemOhneIndex1.setText(PM_MSG.getMsg("miNotIndx1"));
		menuItemOhneIndex2.setText(PM_MSG.getMsg("miNotIndx2"));
		menuItemOhneSerien.setText(PM_MSG.getMsg("miNotSequ"));
		// Menu delete
		menuLoeschen.setText(PM_MSG.getMsg("menuDelete"));
		menuItemLoeschenIndex1.setText(PM_MSG.getMsg("miDelAllIndx1"));
		menuItemLoeschenIndex2.setText(PM_MSG.getMsg("miDelAllIndx2"));
		menuItemLoeschenSeq.setText(PM_MSG.getMsg("miDelAllSequ"));
		// Menu Extras
		menuExtras.setText(PM_MSG.getMsg("menuExtra"));
		menuItemFlush.setText(PM_MSG.getMsg("miFlush"));
		menuItemIndizieren.setText(PM_MSG.getMsg("miLucene"));
		menuItemLanguage.setText(PM_MSG.getMsg("miLanguage"));
		menuItemLoeschenMeta.setText(PM_MSG.getMsg("miUninstall"));		
		// Menu Infos
		menuInfo.setText(PM_MSG.getMsg("menuInfo"));
		menuItemInfoEinstellungen.setText(PM_MSG.getMsg("miPref"));
		menuItemInfoAbout.setText(PM_MSG.getMsg("miAbout"));
		 
	}

	/**
	 * get the menu bar for the main window
	 * 
	 * @return
	 */
	public JMenuBar getMenuBar() {
		return menuBar;
	}

	// ==========================================================
	// init()
	// ==========================================================
	private JMenu menuDatei;
	private JMenu menuView;
	private JMenu menuSuchen;
	private JMenu menuLoeschen;
	private JMenu menuChange;
	private JMenu menuExtras;
	private JMenu menuInfo;
	
	private JMenuItem miViewTemp;
	
	private JMenuItem miClose;
	private JMenuItem menuItemDoppelte;
	private JMenuItem menuItemOhneIndex1;
	private JMenuItem menuItemOhneIndex2;
	private JMenuItem menuImportedMiniSequences;
	private JMenuItem menuItemOhneSerien;
	private JMenuItem menuItemLoeschenIndex1;
	private JMenuItem menuItemLoeschenIndex2;
	private JMenuItem menuItemLoeschenSeq;
	private JMenuItem menuItemChangeIndex1;
	private JMenuItem menuItemChangeIndex2;
	
	private JMenuItem menuItemLoeschenMeta;
	private JMenuItem subMenuVDR;
	
	private JMenuItem menuItemFlush;
	private JMenuItem menuItemIndizieren;
	private JMenuItem menuItemLanguage;
	
	private JMenuItem menuItemInfoEinstellungen;
	private JMenuItem menuItemInfoAbout;
	
	
	
	private void init() {

		// JMenuItem menuItem = null;
		menuBar = new JMenuBar();
		menuDatei = new JMenu("Datei");
		menuView = new JMenu("View");
		menuSuchen = new JMenu("Suchen");
		menuLoeschen = new JMenu("Lï¿½schen");
		menuChange = new JMenu("Ändern");
		menuExtras = new JMenu("Extras");
		menuInfo = new JMenu("PM-Info");

		// ------------------------------------------------------------------
		// Menu: Datei
		// ------------------------------------------------------------------

		// beenden
		miClose = new JMenuItem("Beenden");
		ActionListener alClose = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				windowMain.close();
			}
		};
		miClose.addActionListener(alClose);
		menuDatei.add(miClose);

		// ------------------------------------------------------------------
		// Menu: Album
		// ------------------------------------------------------------------

		// Album: lï¿½schen
		// JMenuItem miAlbumDelete = new JMenuItem("Album lï¿½schen");
		// ActionListener alAlbumDelete = new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// PM_AlbumClose ac = new PM_AlbumClose(windowMain);
		// ac.start();
		// }
		// };
		// miAlbumDelete.addActionListener(alAlbumDelete);
		// menuAlbum.add(miAlbumDelete);

		// -------------------------------------------------------------
		// Menu: View
		// -------------------------------------------------------------
		miViewTemp = new JMenuItem("temp pictures");
		ActionListener alTemp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doViewTempPictures();
			}
		};
		miViewTemp.addActionListener(alTemp);
		menuView.add(miViewTemp);
		
		
		
		
		// ------------------------------------------------------------------
		// Menu: Search
		// ------------------------------------------------------------------
		menuItemDoppelte = new JMenuItem("doppelte Bilder");
		ActionListener alDoppelte = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDoppelteBilder();
			}
		};
		menuItemDoppelte.addActionListener(alDoppelte);
		menuSuchen.add(menuItemDoppelte);

		String ohneIndex1 = "Bilder ohne Index 1";
		menuItemOhneIndex1 = new JMenuItem(ohneIndex1);
		ActionListener alOhneIndex1 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOhneIndex1();
			}
		};
		menuItemOhneIndex1.addActionListener(alOhneIndex1);
		menuSuchen.add(menuItemOhneIndex1);

		String ohneIndex2 = "Bilder ohne Index 2";
		menuItemOhneIndex2 = new JMenuItem(ohneIndex2);
		ActionListener alOhneIndex2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOhneIndex2();
			}
		};
		menuItemOhneIndex2.addActionListener(alOhneIndex2);
		menuSuchen.add(menuItemOhneIndex2);

		menuItemOhneSerien = new JMenuItem(
				"Basis-Serien: nicht zugeordnete Bilder suchen");
		ActionListener alOhneSerien = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOhneSequenzen();
			}
		};
		menuItemOhneSerien.addActionListener(alOhneSerien);
		menuSuchen.add(menuItemOhneSerien);

		String importedMiniSequences = "Alle importierten Mini-Sequenzen";
		menuImportedMiniSequences = new JMenuItem(importedMiniSequences);
		menuImportedMiniSequences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAllImportedMinisequences();
			}
		});
		menuSuchen.add(menuImportedMiniSequences);
		
		
		// ------------------------------------------------------------------
		// Menu: Delete
		// ------------------------------------------------------------------
		menuItemLoeschenIndex1 = new JMenuItem("lÃ¶schen alle Index 1");
		ActionListener alLoeschenIndex1 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteIndex(IndexType.INDEX_1);
			}
		};
		menuItemLoeschenIndex1.addActionListener(alLoeschenIndex1);
		menuLoeschen.add(menuItemLoeschenIndex1);

		menuItemLoeschenIndex2 = new JMenuItem("löschen alle Index-2");
		ActionListener alLoeschenIndex2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteIndex(IndexType.INDEX_2);
			}
		};
		menuItemLoeschenIndex2.addActionListener(alLoeschenIndex2);
		menuLoeschen.add(menuItemLoeschenIndex2);

		menuItemLoeschenSeq = new JMenuItem("lï¿½schen alle Serien");
		ActionListener alLoeschenSeq = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doLoeschenSeq();
			}
		};
		menuItemLoeschenSeq.addActionListener(alLoeschenSeq);
		menuLoeschen.add(menuItemLoeschenSeq);

//		menuLoeschen.addSeparator();

		// ------------------------------------------------------------------
		// Menu: Change
		// ------------------------------------------------------------------
		menuItemChangeIndex1 = new JMenuItem("Ändern Index 1");
		ActionListener alChangeIndex1 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeIndex(IndexType.INDEX_1);
	 		}
		};
		menuItemChangeIndex1.addActionListener(alChangeIndex1);
		menuChange.add(menuItemChangeIndex1);
		
		menuItemChangeIndex2 = new JMenuItem("Ändern Index 2");
		ActionListener alChangeIndex2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeIndex(IndexType.INDEX_2);
	 		}
		};
		menuItemChangeIndex2.addActionListener(alChangeIndex2);
		menuChange.add(menuItemChangeIndex2);
		
		
		
		
		// ------------------------------------------------------------------
		// Menu: PM-Info
		// ------------------------------------------------------------------

		menuItemInfoEinstellungen = new JMenuItem("Einstellungen");
		ActionListener alInfoEinstellungen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String info = "";

				PM_Configuration einstellungen = PM_Configuration
						.getInstance();
				String homeBilder = einstellungen.getTopLevelPictureDirectory().getPath();
				Properties props = System.getProperties();

				info += "PM-Version = " + PM_Utils.getPmVersion();
				info += NL;
				info += NL + "Bilder-Verzeichnis = " + homeBilder;
				info += NL + "Einstellungen = "
						+ einstellungen.getFileEinstellungen().getPath();

				info += NL;
				info += NL + "JAVA-Runtime-Version =  "
						+ PM_Utils.getPropertyString(props, "java.version");
				info += NL + "JAVA-Compiler-Version =  "
						+ PM_Utils.getCompilerVersion()
						+ " (compiliert am  =  " + PM_Utils.getDateCompiled()
						+ ")";

				info += NL;
				info += NL + "user.dir =  "
						+ PM_Utils.getPropertyString(props, "user.dir");
				info += NL + "user.home =  "
						+ PM_Utils.getPropertyString(props, "user.home");
				info += NL
						+ "awt.multiClickInterval =  "
						+ Toolkit.getDefaultToolkit().getDesktopProperty(
								"awt.multiClickInterval") + " ms";
				info += NL + "Verzeichnis 'pm_temp' = "
						+ einstellungen.getFileHomeTemp().getPath();
				// info += NL+"Verzeichnis Lucene DB = "
				// + einstellungen.getFileHomeLuceneDB().getPath();
				// info += NL
				// + "Datei Serien-infos = "
				// + PM_SequencesInoutXML.getInstance()
				// .getFileSeqzenzenXML().getPath()

				;

				JOptionPane.showConfirmDialog(null, info, "Info",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		menuItemInfoEinstellungen.addActionListener(alInfoEinstellungen);
		menuInfo.add(menuItemInfoEinstellungen);

		// Help-System
		JMenuItem miHelp = new JMenuItem("Help");
//		PM_Help help = PM_Help.getInstance();
//		if (help != null) {
//			ActionListener alHelp = help.getActionListener();
//			miHelp.addActionListener(alHelp);
//			menuInfo.add(miHelp);
//		}

		ActionListener alHelp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String msg = PM_MSG.getMsg("menuHelp1")  
					+PM_MSG.getMsg("menuHelp2")  
					+PM_MSG.getMsg("menuHelp3") 
					+PM_MSG.getMsg("menuHelp4")  
					+PM_MSG.getMsg("menuHelp5");
				
				
				JOptionPane.showConfirmDialog(null, msg, "Help",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		miHelp.addActionListener(alHelp);
		menuInfo.add(miHelp);
		
		// --------------------------------------------------
//		menuInfo.addSeparator();

		// "ï¿½ber PhotoManager"
		menuItemInfoAbout = new JMenuItem("ï¿½ber Photomanager");
		ActionListener alInfoAbout = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String info =  

				String.format(PM_MSG.getMsg("aboutTxt"), PM_Utils.getPmVersion(), PM_MSG.getMsg("eMail"));
				
				
				JOptionPane.showConfirmDialog(null, info, PM_MSG.getMsg("miAbout"),
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		menuItemInfoAbout.addActionListener(alInfoAbout);
		menuInfo.add(menuItemInfoAbout);

		// ------------------------------------------------------------------
		// Menu:    Extras
		// ------------------------------------------------------------------

		// file: "flush"
		menuItemFlush = new JMenuItem("Metadaten sichern");
		ActionListener alFlush = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PM_MetadataContainer.getInstance().flush();
				PM_DatabaseLucene.getInstance().flush();
				// Sequenzen IMMER schreiben
				PM_SequencesInout.getInstance().setChanged(true);
				PM_SequencesInout.getInstance().flush();
			}
		};
		menuItemFlush.addActionListener(alFlush);
		menuExtras.add(menuItemFlush);

		// file: "indizieren"

		menuItemIndizieren = new JMenuItem("Datenbank neu erstellen");
		ActionListener alIndizieren = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// jetzt inidizieren (neu erstellen)
				PM_DatabaseLucene.getInstance()
						.alleLuceneEintraegeNeuErstellen();
			}
		};
		menuItemIndizieren.addActionListener(alIndizieren);
		menuExtras.add(menuItemIndizieren);

		// ---- Sprache einstellen -------------------------
		menuItemLanguage = new JMenuItem("Sprache einstellen");
		menuExtras.add(menuItemLanguage);
		ActionListener alLanguage = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// jetzt inidizieren (neu erstellen)
				Locale locale = (new PM_WindowDialogGetLocale(PM_MSG.getLocale()).getLocale());
				PM_Configuration.getInstance().setLocale(locale);
			}
		};
		menuItemLanguage.addActionListener(alLanguage);
		menuExtras.add(menuItemLanguage);

		// -----------  Uninstall -------------------
		menuExtras.addSeparator();
		menuItemLoeschenMeta = new JMenuItem("Uninstall");
		ActionListener alLoeschenMeta = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doLoeschenMeta();
			}
		};
		menuItemLoeschenMeta.addActionListener(alLoeschenMeta);
		menuExtras.add(menuItemLoeschenMeta);
		
		

		
		 
		// --------------------------------------------
		// menuExtras.addSeparator();
		/*
		 * // VDR Plugin updaten JMenuItem menuItemVDR = new JMenuItem("VDR
		 * Plugin update"); ActionListener alVDR = new ActionListener() { public
		 * void actionPerformed(ActionEvent e) { doVDRUpdate(); } };
		 * menuItemVDR.addActionListener(alVDR); if (einstellungen.vdrPlugin()) {
		 * menuExtras.add(menuItemVDR); }
		 */
		// --------------------------------------------
		

		// ---- Bilder Sicherung ----------
		List<BackUp> backupBilderList = einstellungen.getBackupBilderList();
		if (backupBilderList.size() != 0) {
			menuExtras.addSeparator();
			JMenu subMenuBilder = new JMenu("sichern Bilder ... ");
			menuExtras.add(subMenuBilder);
			ActionListener alBilder = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					System.out.println("   action" + e.getActionCommand());
					List<BackUp> backupBilderList = einstellungen
							.getBackupBilderList();
					Iterator<BackUp> it = backupBilderList.iterator();
					while (it.hasNext()) {
						BackUp bb = it.next();
						if (bb.getName().equals(e.getActionCommand())) {
							doBackupBilder(bb);
						}
					}
				}
			};
			Iterator<BackUp> it = backupBilderList.iterator();
			while (it.hasNext()) {
				BackUp bb = it.next();
				JMenuItem menuBilder = new JMenuItem(bb.getName());
				menuBilder.addActionListener(alBilder);
				subMenuBilder.add(menuBilder);
			}
		}

		// ---- Daten Sicherung ----------
		List<BackUp> backupDatenList = einstellungen.getBackupDatenList();
		if (backupDatenList.size() != 0) {
			if (backupBilderList.isEmpty()) {
				menuExtras.addSeparator();
			}
			
			JMenu subMenuDaten = new JMenu("sichern Daten ...");
			menuExtras.add(subMenuDaten);
			ActionListener alDaten = new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					System.out.println("   action" + e.getActionCommand());
					List<BackUp> backupDatenList = einstellungen
							.getBackupDatenList();
					Iterator<BackUp> it = backupDatenList.iterator();
					while (it.hasNext()) {
						BackUp bb = it.next();
						if (bb.getName().equals(e.getActionCommand())) {
							doBackupDaten(bb);
						}
					}
				}
			};
			Iterator<BackUp> it = backupDatenList.iterator();
			while (it.hasNext()) {
				BackUp bb = it.next();
				JMenuItem menuDaten = new JMenuItem(bb.getName());
				menuDaten.addActionListener(alDaten);
				subMenuDaten.add(menuDaten);
			}
		}
		
		// ----------- VDR ------------------				
		if (PM_Utils.isLinux() && PM_Configuration.getInstance().isMpeg()) {
			subMenuVDR = new JMenu("vdr ...");
			menuExtras.addSeparator();
			menuExtras.add(subMenuVDR);
			// menu item check mpegs
			JMenuItem menuItemCheckMpeg = new JMenuItem("count all mpeg");
			subMenuVDR.add(menuItemCheckMpeg);
			ActionListener alCheckMpeg = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PM_MetadataContainer.getInstance().flush();
					int count = PM_MetadataContainer.getInstance().countMpegFiles();
					JOptionPane
					.showConfirmDialog(
							null,
							count + " mpeg files vorhanden",
							"",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
				}
			};		
			menuItemCheckMpeg.addActionListener(alCheckMpeg);
			// menu item: make all mpegs
			JMenuItem menuItemMakeMpeg = new JMenuItem("make all mpeg");
			subMenuVDR.add(menuItemMakeMpeg);
			ActionListener alMakeMpeg = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doMakeMpeg();
				}
			};		
			menuItemMakeMpeg.addActionListener(alMakeMpeg);
			// menu item: make all mpegs
			JMenuItem menuItemDeleteMpeg = new JMenuItem("delete all mpeg");
			subMenuVDR.add(menuItemDeleteMpeg);
			ActionListener alDeleteMpeg = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doDeleteMpeg();
				}
			};		
			menuItemDeleteMpeg.addActionListener(alDeleteMpeg);
			// menu item: get all pictures without mpeg
			JMenuItem menuItemWithoutMpeg = new JMenuItem("all pictures without mpeg");
			subMenuVDR.add(menuItemWithoutMpeg);
			ActionListener alWithoutMpeg = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doWithoutMpeg();
				}
			};		
			menuItemWithoutMpeg.addActionListener(alWithoutMpeg);
		}
		 

		// ------------------------------------------------------------------
		// Alle Menu's sind aufbereitet. Jetzt zur MenuBar hinzufuegen
		// ------------------------------------------------------------------
		menuBar.add(menuDatei);
		menuBar.add(menuView);
		menuBar.add(menuSuchen);
		menuBar.add(menuLoeschen);
		menuBar.add(menuChange);
		menuBar.add(menuExtras);
		menuBar.add(menuInfo);

	}

	// ======================================================
	// doDoppelteBilder()
	//
	// Doppelte Bilder ermitteln
	// ======================================================

	public void doDoppelteBilder() {
		PM_DoublePictures doppelteBilder = new PM_DoublePictures(windowMain);
		doppelteBilder.doppelteBilderErmitteln();
		int anzGesamt = doppelteBilder.getAnzGesamt();
		int anzDoppelt = doppelteBilder.getAnzDoppelt();
		int anzDreifach = doppelteBilder.getAnzDreifach();
		int anzVierfachPlus = doppelteBilder.getAnzVierfachPlus();
		List<PM_Picture> pictures = doppelteBilder.getAlleFiles();

		if (anzGesamt == 0) {
			JOptionPane.showConfirmDialog(
					null,
					PM_MSG.getMsg("doubleNotFound"), // "Keine doppelten
														// Bilder gefunden",
					PM_MSG.getMsg("doubleSearch"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String msg = String.format(PM_MSG.getMsg("doubleFound"), Integer
				.toString(anzDoppelt), Integer.toString(anzDreifach), Integer
				.toString(anzVierfachPlus));

		int n = JOptionPane.showConfirmDialog(windowMain, msg, PM_MSG
				.getMsg("doubleSearch"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		PM_Index  indexView = windowMain.getWindowLeft()
				.getIndex();
		indexView.data.clearAndAdd(pictures);

	}
	
	/**
	 * doViewTempPictures()
	 */
	private void doViewTempPictures() {
//		List<PM_Picture> tempPictureList = PM_IndexView_deprecated.getTempPictureList();
		List<PM_Picture> tempPictureList = new ArrayList<PM_Picture>();
		if (tempPictureList.isEmpty()) {
			JOptionPane
					.showConfirmDialog(null, "Keine temp. Bilder vorhanden.",
							"", JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		PM_Index  indexView = windowMain.getWindowLeft()
				.getIndex();
		indexView.data.clearAndAdd(tempPictureList);
	}

	// ======================================================
	// doOhneIndex1()
	//
	// Bilder ohne Index-1
	// ======================================================

	private void doOhneIndex1() {
		PM_Search search = new PM_Search(new PM_SearchExpr(
				SearchType.NO_INDEX_1));
		search.search();
		doSearchNoIndices(search.getPictureList(SearchSortType.NOTHING), "Index 1");
	}

	// ======================================================
	// doOhneIndex2()
	//
	// Bilder ohne Index-2
	// ======================================================

	private void doOhneIndex2() {
		PM_Search search = new PM_Search(new PM_SearchExpr(
				SearchType.NO_INDEX_2));
		search.search();
		doSearchNoIndices(search.getPictureList(SearchSortType.NOTHING), "Index 2");
				 
	}

	// ======================================================
	// doOhneSequenzen()
	//
	// Bilder ohne Basis-Sequenzen/Sequenzen
	// ======================================================

	private void doOhneSequenzen() {
		// damit alles zurï¿½ckgeschrieben ist
		PM_MetadataContainer.getInstance().flush();

		List<PM_Picture> pictures = new ArrayList<PM_Picture>();
		pictures = PM_MetadataContainer.getInstance().bilderOhneSequenzen();

		if (pictures.size() == 0) {
			JOptionPane.showConfirmDialog(windowMain, PM_MSG
					.getMsg("allPicsInSequ"), PM_MSG.getMsg("noSequSearch"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;

		}

		String msg = String.format(PM_MSG.getMsg("notAllPicsInSequ"), Integer
				.toString(pictures.size()));
		int n = JOptionPane.showConfirmDialog(windowMain, msg, PM_MSG
				.getMsg("noSequSearch"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		Collections.sort(pictures, ORDER_TIME);

		
		PM_Index  index  = windowMain.getWindowLeft()
				.getIndex();
		index.data.clearAndAdd(pictures);

	}

	// =====================================================
	// ORDER_TIME:
	// resultList nach Zeit sortieren
	// =====================================================
	static private final Comparator<PM_Picture> ORDER_TIME = new Comparator<PM_Picture>() {
		public int compare(PM_Picture pic1, PM_Picture pic2) {
 
			Long time1 = pic1.meta.getDateCurrent().getTime();
			Long time2 = pic2.meta.getDateCurrent().getTime();
			return time1.compareTo(time2);

		};
	};

  
 

 
	// ======================================================
	// doSearchNoIndices()
	// ======================================================

	private void doSearchNoIndices(List<PM_Picture> pictureList, String type) {

		String search = String.format(PM_MSG.getMsg("indSearch"), type);
		if (pictureList.size() == 0) {
			String msg = String
					.format(PM_MSG.getMsg("indSearchNotFound"), type);
			JOptionPane.showConfirmDialog(windowMain, msg, search,
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String msg = String.format(PM_MSG.getMsg("indSearchFound"), Integer
				.toString(pictureList.size()));
		int n = JOptionPane.showConfirmDialog(windowMain, msg, search,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		PM_Index  indexView = windowMain.getWindowLeft()
				.getIndex();
		indexView.data.clearAndAdd(pictureList);
	}

 

	// ======================================================
	// doBackupDaten()
	//
	// ======================================================
	private void doBackupDaten(BackUp bb) {
		File datenFrom = bb.getFileDirFrom();
		File datenTo = bb.getFileDirTo();
		if (!datenFrom.isDirectory()) {
			JOptionPane.showConfirmDialog(windowMain,
					"In Datei Einstellungen:\nfrom   \'"
							+ datenFrom.getAbsolutePath()
							+ "\'    kein Directory", "Dir nigefu",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!datenTo.isDirectory()) {
			JOptionPane.showConfirmDialog(windowMain,
					"In Datei Einstellungen:\nto   \'"
							+ datenTo.getAbsolutePath()
							+ "\'    kein Directory", "Dir nigefu",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String name = bb.getName();
		// String host = bb.getHost();

		int n = JOptionPane.showConfirmDialog(windowMain, "Daten sichern?"
				+ "\nname = " + name
				// + "\nhost = " + host
				+ "\nfrom = " + datenFrom.getAbsolutePath() + "\nto = "
				+ datenTo.getAbsolutePath(),

		"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		windowMain.getWindowRechts().getWindowSelected().requestToChange();
		PM_MetadataContainer.getInstance().flush();

		PM_WindowDialogBackupDaten daten = new PM_WindowDialogBackupDaten(bb);
		if (daten.init() == false)
			return; // ERROR. Meldungen bereits ausgegben
		daten.start();
	}

	// ======================================================
	// doBackupBilder()
	//
	// ======================================================
	private void doBackupBilder(BackUp bb) {
		File bilderDir = bb.getFileDirTo();
		if (!bilderDir.isDirectory()) {
			JOptionPane.showConfirmDialog(windowMain,
					"In Datei Einstellungen:\nbilder-dir   \'"
							+ bilderDir.getAbsolutePath()
							+ "\'    kein Directory", "Keine gefunden",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String name = bb.getName();
		// String host = bb.getHost();

		int n = JOptionPane.showConfirmDialog(windowMain, "Bilder sichern?"
				+ "\nname = "
				+ name
				+ "\nfrom = "
				+ PM_Configuration.getInstance().getTopLevelPictureDirectory()
						.getPath() + "\nto = " + bilderDir.getAbsolutePath(),
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		windowMain.getWindowRechts().getWindowSelected().requestToChange();
		PM_MetadataContainer.getInstance().flush();

		PM_WindowDialogBackupBilder bilder = new PM_WindowDialogBackupBilder(bb);
		if (bilder.init(bb.getMpeg()) == false) {
			return; // ERROR. Meldungen bereits ausgegben
		}
		bilder.start();

	}

	// ======================================================
	// doLoeschenIndex1()
	//
	// ======================================================
	private void deleteIndex(IndexType type) {

		String typeString = (type == IndexType.INDEX_1) ? PM_MSG
				.getMsg("index1") : PM_MSG.getMsg("index2");

		int n = JOptionPane.showConfirmDialog(windowMain, String.format(PM_MSG
				.getMsg("wantDeleteInd"), typeString),
				String.format(PM_MSG.getMsg("deleteInd"), typeString)
				, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		PM_MetadataContainer.getInstance().deleteIndex(type);

		// jetzt inidizieren (neu erstellen)
		PM_DatabaseLucene.getInstance().alleLuceneEintraegeNeuErstellen();

	}

	
	private void changeIndex(IndexType type) {

		PM_WindowDialogChangeIndex dialog = new PM_WindowDialogChangeIndex(type);

	}
	
	// ======================================================
	// doLoeschenSeq()
	//
	// ======================================================
	private void doLoeschenSeq() {

		int n = JOptionPane.showConfirmDialog(windowMain, PM_MSG
				.getMsg("wantDeleteSequ"), PM_MSG.getMsg("deleteAllSequ"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		PM_MetadataContainer.getInstance().alleSequenzenLoeschen();

		// jetzt inidizieren (neu erstellen)
		PM_DatabaseLucene.getInstance().alleLuceneEintraegeNeuErstellen();

	}

	
	/**
	 * doMakeMpeg()
	 */
	private void doMakeMpeg() {
		
		// check for programs nessesary to create mpeg files
		if (!checkForProgramFilesMakeMPEG()) {
			return;
		}
		
		
		int count = PM_MetadataContainer.getInstance().missingsMpegFiles();
		if (count == 0) {
			JOptionPane
			.showConfirmDialog(
					null,
					"alle mpeg files vorhanden.",
					"",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
		
		
		int n = JOptionPane.showConfirmDialog(
				windowMain,				
				count + " fehlende mpeg-files erstellen?",													
				"",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		
		count = PM_VDR_MpgFile.makeMpegfiles(count);
		
		
		JOptionPane
		.showConfirmDialog(
				null,
				count + " mpeg files erzeugt",
				"",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	 
	/**
	 * doWithoutMpeg()
	 * 
	 * Get all pictures without mpeg files
	 */
	private void doWithoutMpeg() {
		
		List<PM_Picture> pictures = PM_MetadataContainer.getInstance().getMissingsMpegFiles();
		 
		if (pictures.size() == 0) {
			JOptionPane
			.showConfirmDialog(
					null,
					"alle mpeg files vorhanden.",
					"",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
		
 

		String msg = pictures.size() + " pictures found without mpeg files.\nShow them?";
			String.format(PM_MSG.getMsg("notAllPicsInSequ"), Integer
				.toString(pictures.size()));
		int n = JOptionPane.showConfirmDialog(windowMain, msg, "",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		Collections.sort(pictures, ORDER_TIME);

		PM_Index  indexView = windowMain.getWindowLeft()
				.getIndex();
		indexView.data.clearAndAdd(pictures);
	}
	
	
	
	/**
	 * doDeleteMpeg()
	 */
	private void doDeleteMpeg() {
		int n = JOptionPane.showConfirmDialog(
				windowMain,				
				"alle mpeg-files lï¿½schen?",													
				"",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		n = JOptionPane.showConfirmDialog(
				windowMain,				
				"wirklich ALLE mpeg-files unwiederruflich lï¿½schen?",													
				"",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		// jetzt ALLE lï¿½schen

		PM_MetadataContainer.getInstance().flush();
		int count = PM_MetadataContainer.getInstance().deleteMpegFiles();
		JOptionPane
		.showConfirmDialog(
				null,
				count + " mpeg files wurden gelï¿½scht",
				"", 
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	// ======================================================
	// doLoeschenMeta()
	//
	// ======================================================
	private void doLoeschenMeta() {
		int n = JOptionPane
				.showConfirmDialog(
						windowMain,				
						PM_MSG.getMsg("wantUninstall1"),													
						PM_MSG.getMsg("miUninstall"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;

		n = JOptionPane
				.showConfirmDialog(
						windowMain,
						PM_MSG.getMsg("wantUninstall2"),
						PM_MSG.getMsg("miUninstall"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION)
			return;
	 
		// ------------------------------------------------
		// Jetzt alle Verzeichnisse "pm.metadaten" lï¿½schen
		// ------------------------------------------------
		File homeBilder = einstellungen.getTopLevelPictureDirectory();
		deleteMeta(homeBilder);

		// fertig
		JOptionPane
				.showConfirmDialog(
						null,
						PM_MSG.getMsg("doneUninstall"),
						PM_MSG.getMsg("miUninstall"), JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE);
		
		// ------------------------------------------------------
		// PM beenden
		// -------------------------------------------------------
		System.exit(0);

	}

	// =====================================================
	// private: rekursiv lï¿½schen
	// =====================================================
	private void deleteMeta(File dir) {

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && f.getName().equals(DIR_METADATEN)) {
				PM_Utils.deleteDirectory(f);
			}
			if (f.isDirectory() && f.getName().equals(DIR_METADATEN_ROOT)) {
				PM_Utils.deleteDirectory(f);
			}
			if (f.isDirectory() && f.getName().equals(DIR_PM_TEMP)) {
				PM_Utils.deleteDirectory(f);
			}
			if (f.isDirectory()) {
				deleteMeta(f);
			}
		}
	}

	
	
	
	/**
	 * checkForProgramFilesMakeMPEG()
	 */
	private boolean checkForProgramFilesMakeMPEG() {
		// programms for converting files for vdr
		String msg = PM_VDR_MpgFile.checkForProgramms();
		if (msg == null) {
			return true;
		}
		JOptionPane
		.showConfirmDialog(
				null,
				"ERROR: Programms not found:\n\n" + msg,
				"", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		
		return false;
	}
	
	/**
	 * Search all imported mini-sequences.
	 */
	private void searchAllImportedMinisequences() {
		
	}
	
	
	
}
