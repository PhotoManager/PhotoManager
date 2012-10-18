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


import pm.sequence.*;
import pm.utilities.*;
import pm.index.*;
import pm.picture.*;


import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 
import java.awt.*;
 
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;


@SuppressWarnings("serial")
public class PM_WindowSequence extends PM_WindowBase implements PM_Interface {

	 
 
 

	 
	private PM_SubWindowSequence[] subWindows = new PM_SubWindowSequence[3];
	
	private PM_SubWindowSequence selectedSubWindow;
	
	private JTabbedPane tabPaneTreeWindows;
	final private int TAB_BASE 		= 0;
	final private int TAB_EXTEND 	= 1;
	final private int TAB_MINI 		= 2;

	private JLabel upperLabel;
	
	
	
	/**
	 * The constructor for this class.
	 */
	public PM_WindowSequence(  ) {
		super(null);
		
		subWindows[TAB_BASE] = new PM_SubWindowSeqBase(windowMain, this);
		subWindows[TAB_EXTEND] = new PM_SubWindowSeqExtend(windowMain, this);
		subWindows[TAB_MINI] = new PM_SubWindowSeqMini(windowMain, this);
		
		createGUI();	
	
		 
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
	 * Create the gui.
	 */
	private void createGUI() {

		PM_FocusPanel upperPanel = getUpperLabel();
		JScrollPane scUpperTreePanel = new JScrollPane(upperPanel);
		scUpperTreePanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scUpperTreePanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
	 
		
		tabPaneTreeWindows = new JTabbedPane();
		tabPaneTreeWindows.insertTab("?B-Serien", null, subWindows[TAB_BASE].getTabComponent(), "",
				TAB_BASE);
		tabPaneTreeWindows.insertTab("?X-Serien", null, subWindows[TAB_EXTEND].getTabComponent(), "",
				TAB_EXTEND);
		tabPaneTreeWindows.insertTab("M-Sequence", null, subWindows[TAB_MINI].getTabComponent(), "", 
				TAB_MINI);
		
		
		tabPaneTreeWindows.setSelectedIndex(TAB_BASE);		
		selectedSubWindow = subWindows[TAB_BASE];

		// ChangeListener for the tab pane
		tabPaneTreeWindows.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				int selIndex = tabPaneTreeWindows.getSelectedIndex();
				selectedSubWindow = subWindows[selIndex];
				selectedSubWindow.tabChanged(upperLabel);
				//	 mark the thumbs at right
				windowMain.getIndexViewThumbnails().controller.repaintViewport_deprecated( );
			}
		});

		setLayout(new BorderLayout());
		add(scUpperTreePanel, BorderLayout.NORTH);
		add(tabPaneTreeWindows, BorderLayout.CENTER);
		 
	}
	
	
	/**
	 * Returns the upper panel with the label.
	 */
	private PM_FocusPanel getUpperLabel() {
		PM_FocusPanel panel = new PM_FocusPanel() {
			public void setBackgroundColor(Color color) {
//				setBackgroundUpperPanel(color);
			}
		};
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		upperLabel = new JLabel(""); //labelTextBase);
		Font font = upperLabel.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, 30);
		upperLabel.setFont(fontBold);
		panel.add(upperLabel);
		return panel;
	}
	
	/**
	 * Returns the selected Sub Window
	 */
	public PM_SubWindowSequence getSelectedSubWindow() {
		return selectedSubWindow;
	}

	 
	public boolean isBaseSelected() {
		return tabPaneTreeWindows.getSelectedIndex() == TAB_BASE;
	}

	
	 
	
	public String displaySequenceBase(PM_Sequence seq) {
		PM_SubWindowSequence subWin = subWindows[TAB_BASE];
		String ret = subWin.displaySequence(seq);
		if (ret.length() == 0) {	
			tabPaneTreeWindows.setSelectedIndex(TAB_BASE);		
			selectedSubWindow = subWindows[TAB_BASE];
		}
		return ret;
		 
	}
	
	public String displaySequenceExtended(PM_Sequence seq) {
		PM_SubWindowSequence subWin =  subWindows[TAB_EXTEND]; 
		String ret = subWin.displaySequence(seq);
		if (ret.length() == 0) {	
			tabPaneTreeWindows.setSelectedIndex(TAB_EXTEND);		
			selectedSubWindow = subWindows[TAB_EXTEND];
		}		
		return ret;
	}
	
	/**
	 * remove all pictures from a sequence.
	 * 
	 * If the sequence is modified display a warning.
	 */
	@Override
	public void removeAllPictures() {
		if (selectedSubWindow.removeSequenceDisplayed() == false) {
			return;
		}
		super.removeAllPictures();
	}
	  
	  
	  
	// ======================================================
	// getIndexView()
	// ======================================================
	@Override
	public PM_Index  getIndex() {	
		return selectedSubWindow.getIndex();
	 
	}
	
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	
	
 
	
	 

//	private PM_ControllerSequenceMini controllerMiniSequence;
	
	
	
	// =====================================================================================

	private void setMsg() {
		
		 
		tabPaneTreeWindows.setTitleAt(TAB_BASE, PM_MSG.getMsg("winSeqTabBase"));
		tabPaneTreeWindows.setTitleAt(TAB_EXTEND, PM_MSG.getMsg("winSeqTabExt"));
	
		
		if (tabPaneTreeWindows.getSelectedIndex() == TAB_BASE) {
			upperLabel.setText(PM_MSG.getMsg("winSeqHeaderB"));
		} else {
			upperLabel.setText(PM_MSG.getMsg("winSeqHeaderX"));
		}
	 
	}

	
  
 
	
	@Override
	public boolean appendPicture(PM_Picture  picture) {
		if (getIndex() == subWindows[TAB_MINI].getIndex()) {
			// if this is a hidden mini sequence expand it
			PM_SubWindowSeqMini swsm = (PM_SubWindowSeqMini)subWindows[TAB_MINI];
 			boolean compressed =  swsm.addCompressedPicture(picture);
 			if (compressed) {
 				return true; // picture was compressed
 			}
		}	 
		return getIndex().data.addPicture(picture); 
	}

	@Override
	public void rereadAllThumbs() {
		for (PM_SubWindowSequence sws: subWindows) {
			sws.getIndex().controller.rereadAllThumbs();
		}		 
	}
	 
	
 
	
	
	/**
	 * Close the application
	 */
	@Override
	public void close() {	 
		for (PM_SubWindowSequence sws: subWindows) {
			sws.close();
		}
	}

	
	
	
	/**
	 * A double click on a tree node was detected.
	 * 
	 * Display the sequence.
	 */
	@Override
	public void doubleClickOnTree(DefaultMutableTreeNode node, PM_TreeWindow tw) {
		selectedSubWindow.doubleClickOnTree(node, tw);
	}
		
	 /**
	   * Reread the thumb in case it rotate, flip ...
	   */
	@Override
	public void rereadPictureViewThumbnail(PM_Picture picture) {	
		for (PM_SubWindowSequence sws: subWindows) {
			sws.getIndex().controller.rereadPictureViewThumbnail(picture);
		}
	}
	
	/**
	 * flush all windows.
	 * 
	 * remove all displayed thumbs so i.e. you can do the import 
	 *
	 */
	@Override
	public boolean flush() {
		if (!requestToClose()) {
			return false;
		}
		for (PM_SubWindowSequence sws: subWindows) {
			sws.getIndex().data.removeAllPictures() ;
			sws.getIndex().controller.repaintViewport_deprecated();
		}	 
		return true;
	}

	 
	@Override
	public boolean requestToChange() {
		return true;
	}

	@Override
	public boolean requestToClose() {
		for (PM_SubWindowSequence sws: subWindows) {
			if (sws.requestToClose() == false) {
				return false;
			}
		}		
		return true; 
	}


}