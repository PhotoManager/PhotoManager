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
 

 
import java.util.*;
 

import pm.picture.*;
import pm.print.*;
import pm.sequence.PM_Sequence;
import pm.utilities.*;
 
 
import javax.swing.*;
import javax.swing.event.*;

 
@SuppressWarnings("serial")
public class PM_WindowRightTabbedPane extends JTabbedPane implements PM_Interface {
	
	// ------- all the right sub windows ---------
	private PM_WindowBase[] subWindows;
	private PM_WindowPicture windowEinzelbild;
	private PM_WindowInfo windowInfoBild;
	private PM_WindowGroup windowGruppieren;
	private PM_WindowPrint windowDrucken;
	private PM_WindowExport windowExport;
	private PM_WindowImport windowImport;
	private PM_WindowSearch windowSuchen;
	private PM_WindowSequence windowSequence;
	
	
	
	private final PM_WindowMain windowMain;
	private int tabSelected = TAB_SEARCH;
	private PM_WindowBase windowSelected = null;

	private static Set<PM_Listener> changeListener = new HashSet<PM_Listener>();
	
	 
	/**
	 * Constructor.
	 */
	public PM_WindowRightTabbedPane(final PM_WindowMain windowMain, PM_ListenerX listener) {
		super(SwingConstants.TOP);
		this.windowMain = windowMain;
		
		PM_Configuration einstellungen = PM_Configuration
				.getInstance();
		if (einstellungen.isNurLesen()) {
			setBackground(PM_WindowBase.COLOR_NUR_LESEN);
		} else {
			setBackground(PM_WindowBase.COLOR_BACKGROUND);
		}

		// Window "suchen"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Suchen"));
		}
		windowSuchen = new PM_WindowSearch(windowMain );
		windowSuchen.setName("windowSuchen");
		insertTab("", null, windowSuchen, "", TAB_SEARCH);

		// Window "sequenz"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Sequenz"));
		}
		windowSequence = new PM_WindowSequence( );
		windowSequence.setName("windowSequenz");
		insertTab("", null, windowSequence, "", TAB_SEQUENCE);
		
		// Window "zeigen" EinzelBild
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Einzelbild"));
		}
		windowEinzelbild = new PM_WindowPicture();
		windowEinzelbild.setName("windowEinzelbild");
		insertTab("", null, windowEinzelbild, "", TAB_ZEIGEN_EINZEL);

		// Window "zeigen" Gruppe von Bildern
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Gruppe"));
		}
		windowGruppieren = new PM_WindowGroup();
		windowGruppieren.setName("windowGruppieren");
		insertTab("", null, windowGruppieren, "", TAB_ZEIGEN_GRUPPE);

		// Window "drucken"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Drucken"));
		}
		windowDrucken = new PM_WindowPrint( );
		windowDrucken.setName("windowDrucken");
		insertTab("", null, windowDrucken, "", TAB_DRUCKEN);
		 
		// Window "exportieren"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Exportieren"));
		}
		windowExport = new PM_WindowExport( );
		windowExport.setName("windowExport");
		insertTab("", null, windowExport, "", TAB_EXPORTIEREN);

		// Window "importieren"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Importieren"));
		}
		windowImport = new PM_WindowImport( );
		windowImport.setName("windowImport");
		insertTab("", null, windowImport, "", TAB_IMPORTIEREN);
		
		
		// Window "Info-Bild"
		if (listener != null) {
			listener.actionPerformed(new PM_Action(null, 1, "Window Info-Bild"));
		}
		windowInfoBild = new PM_WindowInfo();
		windowInfoBild.setName("windowInfoBild");
		insertTab("", null, windowInfoBild, "", TAB_INFO_BILD);

		
		// Now all sub windows are instantiated.	
		subWindows = new PM_WindowBase[] {windowEinzelbild,  
										windowInfoBild, 
										windowGruppieren,
										windowDrucken,
										windowExport,
										windowImport,
										windowSuchen,
										windowSequence};
		
		// --------------------------------------------------------------------------
		// bevor der ChangeListener eingefuegt wird, noch einige
		// Initialisierungen
		// --------------------------------------------------------------------------
	
		
		if (windowImport.externalImport()) {
			tabSelected = TAB_IMPORTIEREN;
			windowSelected = windowImport;
		} else {
			tabSelected = TAB_SEARCH;
			windowSelected = windowSuchen;
		}
		
		
		
		

		setSelectedIndex(tabSelected);
		

		// --------- ACHTUNG: erst am Schluss den ChangeListener einfuegen
		// ------

		// ChangeListener fuer alle Tabbed-Pane
		ChangeListener ch = new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				int index = getSelectedIndex();
				if (index == -1 || getSelectedComponent() instanceof PM_WindowBase == false) {
					// es war nichts selektiert. Das ist sicher ein Fehler.
					// Grudzustand herstellen.
					tabSelected = TAB_SEARCH;
					windowSelected = windowSuchen;
					setSelectedIndex(tabSelected);
					return;					
				}
				if (tabSelected == index) {
					// kein wechsel. Z.B. alter Zustand wieder hergestellt
					return;
				}
				// --------------------------------------------------------
				//   requestToChange vom zuletzt aufgelegten Window aufrufen.
				// 
				// return true: es kann gewechselt werden
				// false: nicht wechseln
				// --------------------------------------------------------	 
				if (windowSelected.requestToChange() == false) {
					// NICHT wechseln. Alten Index wieder herstellen
					setSelectedIndex(tabSelected);  
					return;
				}

				// --------------------------------------------------------
				// Jetzt wird gewechselt werden
				// (es gibt kein Zur�ck mehr)
				// --------------------------------------------------------
				tabSelected = index; // neuer Index
				windowSelected = (PM_WindowBase)getSelectedComponent(); 
				setJustChanged(windowSelected); // f�r setzen Focus
				// fire the change listener
				fireChangeListener(tabSelected);
				 
				
				// neues Window aufrufen 
				windowMain.getIndexViewThumbnails().controller.repaintViewport_deprecated();				  
 
				 
			}
		};
		addChangeListener(ch);

		// Change Listener f�r message
		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {				 
				setMsg();
			}			
		};		
		PM_MSG.addChangeListener(msgListener);		
		// jetzt werden erstmalig die Tab Titels gesetzt
		setMsg();
	}
	

	/**
	 * add change listener for tab change.
	 */
	static public void addChangeListener(PM_Listener listener) {
		changeListener.add(listener);
	}
	
	private void fireChangeListener(int tab) {
		for (PM_Listener listener : changeListener) {
			// Object object, int type, String str
			listener.actionPerformed(new PM_Action(this, tab));
		}
	}
	
	private void setMsg() {
		setTitleAt(TAB_SEARCH, PM_MSG.getMsg("tabSearch"));
		setTitleAt(TAB_SEQUENCE, PM_MSG.getMsg("tabSeq"));
		setTitleAt(TAB_ZEIGEN_EINZEL, PM_MSG.getMsg("tabPic"));
		setTitleAt(TAB_ZEIGEN_GRUPPE, PM_MSG.getMsg("tabGroup"));
		setTitleAt(TAB_DRUCKEN, PM_MSG.getMsg("tabPrint"));
		setTitleAt(TAB_EXPORTIEREN, PM_MSG.getMsg("tabExport"));
		setTitleAt(TAB_IMPORTIEREN, PM_MSG.getMsg("tabImport"));
		setTitleAt(TAB_INFO_BILD, PM_MSG.getMsg("tabInfo"));		
	}
 	
	/**
	 * 
	 *  
	 */
	public void doStartExternalImport() {
		tabSelected = TAB_IMPORTIEREN;
		windowSelected = windowImport;
		setSelectedIndex(tabSelected);
		
		windowImport.doImportExternal();
		
	}

	/**
	 * Delete pictures.
	 *  
	 *  
	 */
	public void deletePictures(List<PM_Picture> pictures) {
		tabSelected = TAB_EXPORTIEREN;
		windowSelected = windowExport;
		setSelectedIndex(tabSelected); 
		
		windowMain.setDividerLocation(20);
		windowExport.deletePictures(pictures);
	}
	
	public void changeToWindowSequence() {
		tabSelected = TAB_SEQUENCE;
		windowSelected = windowSequence;
		setSelectedIndex(tabSelected);
	}
	
	
	// ======================================================
	// setJustChanged()
	//
	// muss in extra Methode, da in ChangeListener aufgerufen !!!
	// ======================================================
	private void setJustChanged(PM_WindowBase wbChanged) {
		windowMain.setJustChanged(wbChanged);
	}

	 

	
	// ======================================================
	// flushPictureViewThumbnail()
	//
	//  
	// ======================================================
	public void flushPictureViewThumbnail(PM_Picture picture) {
		for (PM_WindowBase wb: subWindows) {
			wb.rereadPictureViewThumbnail(picture);
		}
	}
	
 

	// ======================================================
	// close()
	//
	// Abschliessende Arbeiten durchfuehren
	// ======================================================
	public boolean requestToClose() {
		for (PM_WindowBase wb: subWindows) {
			if (!wb.requestToClose()) {
				return false;
			}
		}
		return true;
	}
	
 
	
	// ======================================================
	// closeAlbum()
	// ======================================================
	public boolean flush() {
		for (PM_WindowBase wb: subWindows) {
			if (!wb.flush()) {
				return false;
			}
		}
		return true;
	}
	
	// ======================================================
	// closeAlbum()
	// ======================================================
	public void closeAlbum() {
		for (PM_WindowBase wb: subWindows) {
			wb.closeAlbum();
		}
	}
	
	// ======================================================
	// rereadAllThumbs()
	// ======================================================
	public void rereadAllThumbs() {
		for (PM_WindowBase wb: subWindows) {
			wb.rereadAllThumbs();
		}
	}

 
	/**
	 * close all the subwindows.
	 */
	public void close() {
		for (PM_WindowBase wb: subWindows) {
			wb.close();
		}
	}

	// ======================================================
	// getWindow......()
	//    
	// ======================================================
	public PM_WindowSequence getWindowSequence() {
		return windowSequence;
	}

	public PM_WindowSearch getWindowSuchen() {
		return windowSuchen;
	}
	public PM_WindowImport getWindowImport() {
		return windowImport;
	}

	public PM_WindowGroup getWindowGruppieren() {
		return windowGruppieren;
	}

	public PM_WindowPicture getWindowEinzelbild() {
		return windowEinzelbild;
	}

	public PM_WindowInfo getWindowInfoBild() {
		return windowInfoBild;
	}

	// ======================================================
	// getSelectedTab()
	//
	// ======================================================
	public int getSelectedTab() {
		return getSelectedIndex();
	}

	// ======================================================
	// getWindow()
	//
	// Returnt das aufgelegte Window
	// ======================================================
	public PM_WindowBase getWindowSelected() {
		return windowSelected;

	}

 

}
