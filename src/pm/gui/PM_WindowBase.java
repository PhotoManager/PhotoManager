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

import java.awt.Color;
import java.awt.Component;
import java.util.*;

 
import pm.index.*;
 
import pm.picture.*;
import pm.sequence.*;
 
import pm.utilities.*;
//import pm.picture.*;
//import pm.lucene.*;
// 


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

 

@SuppressWarnings("serial")
public abstract class PM_WindowBase extends JPanel implements PM_Interface
{
	
    // ======================================================================================
	// ---------------------------------------------------------
	// Alle Colors f�r die Fenster ....
	// TO DO: Durch Einstellungen �berschreiben !!!!!!!
	// -----------------------------------------------------------
	  // Color  ??????????????????????????????????????????????????
	  static public  Color COLOR_SELECTED_BG = Color.ORANGE; //new Color(255,255, 0);  // gelb
	  static public  Color COLOR_DESELECTED_BG = Color.GRAY;
	  static public  Color COLOR_BACKGROUND = new Color(255,255,150);  // hellgelb
	  static public  Color COLOR_NUR_LESEN =  Color.pink;   
	  // Warnung
	  static public  Color COLOR_WARNING = new Color(255,138,138);  // rosa 
	  static public  Color COLOR_WARNING_FOCUS = Color.RED;    
	  
	  // Eingabefelder 
	  static public  Color COLOR_ENABLED = new Color(213,213,255);  // ganz helles grau
	  static public  Color COLOR_ENABLED_SEL = new Color(156,156,255);  // etwas dunkleres grau

	  // Panel und Focus
 	  static public  Color COLOR_BG_PANEL = new Color(255,255,150);  // Panel hat nicht Focus
	  static public  Color COLOR_BG_PANEL_SEL = Color.YELLOW;        // Panel hat Focus
	  static public  Color COLOR_BG_FOCUS = Color.ORANGE;            // Focus
	// --- Ende Color -------------------------------------------------
	// ======================================================================================
	
	  
	  
	  
	 protected PM_WindowMain windowMain;
//	 private int tabIndex;
 //	 private final PM_IndexView_deprecated indexView;  
 	 private final PM_Index  index ; 

 	 
	 protected Component aktiverFocus = null;
	 private List<PM_FocusPanel> focusCycleList = new ArrayList<PM_FocusPanel>();
	 
	 
	 protected JPanel indexPanel;
	
	 /**
	  * This construktor is for the sub windows.
	  * 
	  */
	 protected PM_WindowBase() {
		 index = null; 
	 }
	 
	 
	  public PM_WindowBase(PM_Index  index) {
		  this.index = index;
		  this.windowMain = PM_WindowMain.getInstance();
		  if (index != null) {
			  index.init(this);
			  indexPanel = index.getIndexPanel(); 
		  }
	  }
	 
	 
   /**
   * Constructor
   */
	  /*
  public PM_WindowBase(Side side  , boolean hasIndex)  {

    this.windowMain = PM_WindowMain.getInstance(); //windowMain;
//    this.tabIndex = tabIndex;
    if (hasIndex) {
    	index = PM_Index.createIndex(side,   this);
    	indexPanel = index.getIndexPanel();
    } else {
    	index = null;
    	indexPanel = null;
    }   
  }
 */ 
  
  
  /**
   * return the indexPanel.
   *  
   */
  protected JPanel getIndexPanel() {
	  return indexPanel;
  }
  
  
  //==========================================================
  // getAktiverFocus()/setAktiverFocus()
  //==========================================================
  public Component getAktiverFocus() {
	  return aktiverFocus;
  }
  public void setAktiverFocus(Component aktiverFocus) {
	  this.aktiverFocus = aktiverFocus;
  }  
  
  //==========================================================
  // getFocusCycleList() 
  //==========================================================
  public List getFocusCycleList() {
	  return focusCycleList;
  }   
  
  //==========================================================
  // addFocusPanel() 
  //==========================================================
  public void addFocusPanel(PM_FocusPanel focusPanel) {
	   focusCycleList.add(focusPanel);
  }     
 
  
   
   
  /**
   * Test whether this is the left side mainwindow.
   */
  public boolean isWindowLeft() {
	  return (this instanceof PM_WindowLeftThumbnails);
  }  
   
  
   
   
	/**
	 * Get all thumbs to this index view.
	 * <p>
	 * Remove the current thumbs in the index view and 
	 * take over the thumbs from another index view.
	 * 
	 * @param ivFrom - the index view with the thumbs to take over.
	 * 
	 */
	public void getAllThumbs(PM_Index  ivFrom) {
		if (getIndex() == null) {
			return;
		}
		List<PM_Picture> list = ivFrom.controller.getPictureListDisplayed();
		getIndex().data.clearAndAdd(list);
	}

	/**
	 * Append all thumbs to this index view.
	 * <p>
	 * Take over all the thumbs from another index view.
	 * 
	 * @param ivFrom - the index view with the thumbs to take over.
	 * 
	 */
	public boolean appendAllThumbs(PM_Index  ivFrom) {
		if (getIndex() == null) {
			return false;
		}
		List<PM_Picture> list = ivFrom.controller.getPictureListDisplayed();
		return getIndex().data.appendPictureList(list);
	}

	public void rereadAllThumbs() {
		if (getIndex() == null) {
			return;
		}
		getIndex().controller.rereadAllThumbs();
	}

	
	public void removeAllPictures() {
		if (getIndex() == null) {
			return;
		}
		getIndex().data.removeAllPictures();
	}
	
 
	/**
	 * Insert a picture list into picture position.
	 */
	public boolean insertPictureList(List<PM_Picture> list, PM_Picture toPosition) {
		 
		return getIndex().data.insertPictureList(list,  toPosition );	
	}
	 
 
	public boolean appendPicture(PM_Picture  picture ) {
		if (getIndex() == null) {
			return false;
		}
		return getIndex().data.addPicture(picture);
	}
	 
	 
	
	public boolean insertPicture(PM_Picture  source, PM_Picture target) {	
		if (getIndex() == null) {
			return false;
		}
		return getIndex().data.insertPicture(source, target);
	}
	
	
	// ======================================================
	// getPictureSelected()
	//
	// Wenn rechts ein Window aufliegt, das kein PM_IndexView 
	// hat (z.B. Einzelbild, InfoBild ...), dann hier return
	// mit dem dargestellten PM_Picture.
	// ======================================================
	public PM_Picture getPictureSelected() {
		return null;
	}  	
	
	
	
	
  // ======================================================
  //
  // darstellungSequenz()    
  //
  //======================================================
	public void darstellungSequenz(PM_Sequence sequenz,
			DefaultMutableTreeNode node) {
		// wird ueberladen
	}

	/**
	 * A double click on a tree node was detected.
	 * 
	 * Display the sequence.
	 */
	public void doubleClickOnTree(DefaultMutableTreeNode node, PM_TreeWindow tw) {
		// Overrides
	}

	public void doubleClickOnPictureView(PM_PictureView pictureView) {
		// HACK: ist f�r das Drucken n�tig !!!!!
		// wird ueberladen
	}
  
  
  //======================================================
  //
  // init()    
  //
  //======================================================
  public void init() {
	  // wird ueberladen
  } 
  
  //======================================================
  //
  // requestToChange()    
  //
  // Aufruf beim Tab-Wechsel: Pruefen, ob Aktivitaeten abgeschlossen.
  //======================================================
  abstract public boolean requestToChange(); 

 
  
   
  /**
   * return the associated PM_Index instance.
   * 
   * PM_Index is the access to the model/view/controller.
   * The controller is a public instance variable (getIndex().controller).
   * The view is private (you cannot access it).
   */
  public PM_Index  getIndex() {
		return index;  // null wenn nicht vorhanden !!!
	} 
  
  //======================================================
  // getFocusPanelWithFocusOwner() (mit und ohne FocusOwner-�bergabe)    
  //
  //  Liefert das PM_FocusPanel in dem sich der FocusOwner befindet
  //======================================================
  public PM_FocusPanel getFocusPanelWithFocusOwner() {
	  Component focusOwner = windowMain.getFocusOwner();
	  return getFocusPanelWithFocusOwner(focusOwner);
  } 
  public PM_FocusPanel getFocusPanelWithFocusOwner(Component focusOwner) {
	  if (focusOwner == null) return null;
	  PM_FocusPanel[] focusPanels = getFocusPanels();
	  for (int i=0; i<focusPanels.length; i++) {
    	  PM_FocusPanel fp = focusPanels[i];
    	  if (fp.getLastFocus() == focusOwner) return fp;		  
	  }
      return null; // nigefu
  }  
  
  //======================================================
  // getFocusPanels()     
  //
  // Liefert alle PM_FocusPanels als Array
  //======================================================
  public PM_FocusPanel[] getFocusPanels() {
	  Vector<PM_FocusPanel> v = new Vector<PM_FocusPanel>();
	     Iterator it = focusCycleList.iterator();
	      while (it.hasNext()) {
	    	  PM_FocusPanel fp = (PM_FocusPanel)it.next();
	    	  v.add(fp);
	      }	  
	  return (PM_FocusPanel[]) v.toArray(new PM_FocusPanel[v.size()]);
  }
  
  
  
  
  /**
   * Reread the thumb in case it rotate, flip ...
   */
	public void rereadPictureViewThumbnail(PM_Picture picture) {	
		if (getIndex() != null) {
			getIndex().controller.rereadPictureViewThumbnail(picture);
		}
	}
  
	
	
   
	/**
	 * Request for close in case the application terminates.
	 */
	public boolean requestToClose() {
		return true; // Overrides
	}

	
	/**
	 * flush all windows.
	 * 
	 * remove all displayed thumbs so i.e. you can do the import 
	 *
	 */
	public boolean flush() {
		return true;
	}
	
	 
	public void closeAlbum() {
		// Overrides
	} 
 
  public void close() {
     // Overrides
  }

   
  public String toString() {
     return "WindowBase   = " + getName();
  }

  
}
