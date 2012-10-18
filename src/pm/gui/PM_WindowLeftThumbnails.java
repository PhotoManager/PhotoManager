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
 
import pm.index.PM_Index;
import pm.picture.PM_Picture;
import pm.utilities.*;
 
 
 

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
 
 
 
import java.util.*;
import java.util.List;


/**  Window mit Thumbnails  
 * 
 *   
 * 
 */
@SuppressWarnings("serial")
public class PM_WindowLeftThumbnails extends PM_WindowBase implements
		PM_Interface {

	// private final PM_IndexView indexView;
 
	private PM_FocusPanel upperPanel;

	// ======================================================
	// Konstruktor
	// ======================================================
//	private JScrollPane scrollPane = null;
	public PM_WindowLeftThumbnails( ) {
		super(PM_Index.createIndexLeft());

		PM_Configuration einstellungen = PM_Configuration
		.getInstance();
		setLayout(new BorderLayout());

		// Index Panel mit Scroll-pane
		// indexView = new PM_IndexView(windowMain, this);
//		getIndexView().setPopUpLoeschen(true);
///		getIndexView().setPopUpAendern(true);
//		getIndexView().setPopUpDiaShow(true);
//		getIndexView().setPopUpExternBearbeiten(true);
//		getIndexView().setPopUpSerien(true);
//		if (einstellungen.isNurLesen()) {
//			getIndexView().setBackground(Color.pink);
//		} else {
//			getIndexView().setBackground(COLOR_BACKGROUND);
//		}
 
//		JScrollPane scrollPane = new   JScrollPane(getIndexView());
//		getIndexView().setScrollPane(scrollPane);
 
		
		// Slider
//		JSlider slider = getIndexView().getSlider();
//		upperPanel = getUpperPanel();
//		JScrollPane siv = new JScrollPane(upperPanel);

		// zusammensetzen (Index-Panel und slider)
//		JPanel rp = new JPanel();
//		rp.setLayout(new BorderLayout());
//		rp.add(siv, BorderLayout.NORTH);
//		rp.add(indexViewScrollPane, BorderLayout.CENTER);
//		rp.add(slider, BorderLayout.SOUTH);

		// Splitscreen einfuehren
		// JSplitPane splitPane = new
		// JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tree, rp);
		// splitPane.setDividerLocation(100); //
		add(getIndexPanel());

		 

		// ----------------------------------------------------------
		// Focus, Farben ...
		// ----------------------------------------------------------
		// set default aktiver Focus
//		setAktiverFocus(slider);

		// ------------------------------------------------------
		// Focus-Panels aufbereiten
		// ------------------------------------------------------
//		addFocusPanel(upperPanel);
//		addFocusPanel(getIndexView().getFocusPanel());
//		addFocusPanel(new PM_FocusPanel(null, slider, slider));

//		setBackgroundUpperPanel(COLOR_BG_PANEL);
		
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
//		buttonDiashow.setText(PM_MSG.getMsg("diashow"));		
		 
			
		
	}

 
	
	// =====================================================
	// getFocusComponent()
	//
	// Dies ist der FocusComponent f�r den IndexView vom PM_WindowThumbnail
	// =====================================================
	public PM_FocusPanel getFocusComponent() {
		PM_FocusPanel fc = new PM_FocusPanel(null, getIndexPanel(), getIndexPanel());
		return fc;
	}

 

	// ======================================================
	// closeAlbum()
	// ======================================================
	@Override
	public void closeAlbum() {	 
		 
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
	
	/**
	 * flush all windows.
	 * 
	 * remove all displayed thumbs so i.e. you can do the import 
	 *
	 */
	public boolean flush() {
		getIndex().data.removeAllPictures();
		return true;
	}
	
 
	
	 

	
 
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
//	 ==========================  GUI =================================================
	
	
	// ======================================================
	// getUpperPanelThumbnail()
	// 
	// (oberhalb der Thumbnails im linken Hauptfenster)
	// ======================================================
//	private JButton clearButton = null;
//	private JButton buttonReRead = null;
//	private JButton leftLeftButton = null;
//	private JPanel panelAnz = null;
 
//	private JButton buttonDiashow;



	// ======================================================
	// setBackgroundUpperPanel()
	//
	// 
	// ======================================================
	private void setBackgroundUpperPanel(Color color) {

		upperPanel.setBackground(color);
//		panelAnz.setBackground(color);
		
//		clearButton.setBackground(COLOR_ENABLED);
//		buttonReRead.setBackground(COLOR_ENABLED);

	}

	
} // Ende Klasse
