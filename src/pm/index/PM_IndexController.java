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

package pm.index;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

 
import pm.gui.PM_WindowMain;
import pm.gui.PM_WindowRightTabbedPane;
import pm.picture.*;
import pm.search.PM_LuceneDocument;
import pm.search.PM_Search;
import pm.search.PM_SearchExpr;
import pm.utilities.PM_Action;
import pm.utilities.PM_Listener;
import pm.utilities.PM_Utils;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;

/**
 * This class controls the indexView.
 *  
 * If painting the view, only the controller class call the indexView class. 
 * 
 * The controller class manage the hidden pictures, 
 * for instance if a mini sequence is to display or not.
 *
 * 
 *
 */
public class PM_IndexController {

	 
	protected boolean popUpLoeschen = false;
	protected boolean popUpLoeschenAufheben = false;
	protected boolean popUpAendern = false;
	protected boolean popUpDiaShow = false;
	protected boolean popUpExternBearbeiten = false;
	protected boolean popUpSerien = false;
	
	private double cutGlobalSize = 0;   
	protected boolean drehenSpiegeln = true;
	protected boolean ausschneiden = true;
	protected boolean allowGetFromRight = false;
	protected boolean allowCrossing = false;
	
	protected boolean markImportDatum = false; // Import-Datum (Sortierdatum)
	private boolean paintBildText = false; // Bild Text auf das Thumb schreiben
	
	protected PM_IndexView indexView;
	private PM_IndexData indexModel;
	
	private PM_Index index;
	
	private PM_WindowMain windowMain;
	private Vector<PM_Listener> openCloseListener = new Vector<PM_Listener>();
	
	private int lastChanged = 0;
	
	/**
	 * A Set of pictures not shown as thumbnails.
	 * (for instance a picture in a mPointini sequence)
	 */
//	final private Set<PM_Picture> pictureHidden = new HashSet<PM_Picture>();
	
	/**
	 * constructor
	 */
	protected PM_IndexController(PM_Index index ) {
		this.index = index;
		this.windowMain = PM_WindowMain.getInstance(); //windowMain;
		 
	}
	
	/**
	 * Add open-close listener
	 */
	public void addOpenCloseListener(PM_Listener listener) {
		if (!openCloseListener.contains(listener))
			openCloseListener.add(listener);
	}

	/**
	 * fire the open-close listener
	 */
	protected void fireOpenCloseListener( ) {
		for (PM_Listener listener : openCloseListener) {
			// Object object, int type, String str
			listener.actionPerformed(new PM_Action(this));
		}
	}
	
	
	/**
	 * Check if pictures are importing.
	 */
	protected boolean isImport() {
		return indexView.isImport();
	} 
	
	/**
	 * 
	 */
	protected void setViewAndModel(PM_IndexView indexView, PM_IndexData indexModel ) {
		this.indexView = indexView;
		this.indexModel = indexModel;
	}
	
	/**
	 * repaint a picture if displayed.
	 * 
	 * Don't check all pictures to displayed and 
	 * repaint NOT the entire viewPort. 
	 * If the picture to repaint is not in the 
	 * pictureList do nothing.
	 * 
	 * @param picture - the picture to repaint.
	 */
	public void repaintPicture(PM_Picture picture) {
		indexView.repaintPicture(picture);
	}
	
	/**
	 * returns index and size of displayed pictures.
	 * 
	 *  @return point x = index, y = size
	 */
	public Point getIndexSize(PM_Picture picture) {
		int indx = index.data.indexOf(picture);
//		int indx = getIndexDisplayed(picture);
		int size = getPictureSize();
		return new Point(indx, size);
	}
	
	/**
	 * returns the picture size to display in the view
	 */
	public int getPictureSize() {
		return index.data.getPictureSize();
	}
	
	/**
	 * Returns the size of valid pictures.
	 * 
	 * That is the size displayed minus the closed.
	 */
	public int getSizeValid() {
		int size = getPictureSize() - getPictureClosedSize();
		return (size < 0) ? 0 : size;
	}
	
	
	/**
	 * returns the "displayed" index of picture 
	 */
/*	protected int getIndexDisplayed(PM_Picture picture) {
		
		
		
		int i = index.data.indexOf(picture);
		if (i >= 0) {
			return index.data.pictureViewed.get(0, i).cardinality();
		}
		return 0; // not found	
	}
	*/
	// ======================================================
	// setTextAufloesung()
	//
	// Unter das Bild wird nur die Aufl�sung des
	// Bildes in Pixel geschrieben (z.B. f�r Export)
	// ======================================================
	boolean textAufloesung = false;
	public void setTextAufloesung(boolean textAufloesung) {
		this.textAufloesung = textAufloesung;
	}
	public boolean getTextAufloesung() {
		return textAufloesung;
	}
	
	/**
	 * repaint all pictures in the view port.
	 * 
	 */
	public void repaintViewport_deprecated() {
		// Save the first picture in the view port.
		 
	}
	
	/**
	 * repaint all pictures in the view port.
	 * 
	 */
	public void repaintViewport()  {
		indexView.refreshViewport(); 
	}
	
	public void initEnd() {
		 indexView.initEnd(); 
	}
	
	
	public Insets getAutoscrollInsets() {
		return indexView.getAutoscrollInsets();
	}
	
	/**
	 * this is a hack !!
	 */
	public JPanel getClient() {
		return indexView.getClient();
	}
	
	/**
	 * Autoscroll was detected in Pictureview.
	 * 
	 * Invoke to scroll the scrollpane.
	 */
	public void autoscroll(Point cursorLocn) {
		
		indexView.autoscroll(cursorLocn);	 			
	}
	
	/**
	 * Paint all selected pictures to close as white rectangles.
	 * 
	 */
	public void closePicture( ) {		
		List<PM_Picture> list =  getSelectedPictures();	
		indexView.closePicture(list);	
		repaintViewport();
		if (!index.isLeft()) {
			// remove dashed line on left
			PM_Index.indexLeft.controller.repaintViewport();
		}
		fireOpenCloseListener();
	}
	 
	
	/**
	 * Paint all selected closed pictures as 'normal' thumb.
	 * 
	 */	
	public void openPicture( ) {
		List<PM_Picture> list =  getSelectedPictures();		
		if (!indexView.openPicture(list)) {
			return; // nothing to open
		}
		repaintViewport();
		if (!index.isLeft()) {
			// paint dashed line on left
			PM_Index.indexLeft.controller.repaintViewport();
		}
		fireOpenCloseListener();
	}
	
	/**
	 * Remove all closed pictures.
	 * 
	 */
	public void rereadAllThumbs() {
		Set<PM_Picture> closed = indexView.getPictureClosed();	
		index.data.removePictureList(new ArrayList<PM_Picture>(closed));
		
//		PM_WindowRightTabbedPane wrt = PM_WindowMain.getInstance()
//		.getWindowRechts();
//PM_IndexView iv = wrt.getWindowSelected().getIndexView();
//if (iv.getSizeClosed() == 0) {
//	return; // rechts nichts verstecktes vorhanden
//}

// jetzt gehts los
//wrt.getWindowSelected().rereadAllThumbs();

	}
	
	public void setDrehenAusschneiden(boolean drehenAusschneiden) {
		this.drehenSpiegeln = drehenAusschneiden;
		this.ausschneiden = drehenAusschneiden;
	}
	public void setAusschneiden(boolean ausschneiden) {
		this.ausschneiden = ausschneiden;
	}
	public void setDrehenSpiegeln(boolean drehenSpiegeln) {
		this.drehenSpiegeln = drehenSpiegeln;
	}
	public void setAllowGetFromRight(boolean allowGetFromRight) {
		this.allowGetFromRight = allowGetFromRight;
	}
	public void setAllowCrossing(boolean allowCrossing) {
		this.allowCrossing = allowCrossing;
	}
	
	
	public boolean getAllowGetFromRight( ) {
		return allowGetFromRight;
	}
	public boolean getAllowCrossing( ) {
		return allowCrossing;
	}
	
	
	
	public void setPaintBildText(boolean paintBildText) {
		this.paintBildText = paintBildText;
	}
	public boolean getPaintBildText( ) {
		return paintBildText;
	}
	
	
	public void setPopUpLoeschen(boolean popUpLoeschen) {
		this.popUpLoeschen = popUpLoeschen;
	}

	public void setPopUpLoeschenAufheben(boolean popUpLoeschenAufheben) {
		this.popUpLoeschenAufheben = popUpLoeschenAufheben;
	}

	public void setPopUpAendern(boolean popUpAendern) {
		this.popUpAendern = popUpAendern;
	}

	public void setPopUpDiaShow(boolean popUpDiaShow) {
		this.popUpDiaShow = popUpDiaShow;
	}

	public void setPopUpExternBearbeiten(boolean popUpExternBearbeiten) {
		this.popUpExternBearbeiten = popUpExternBearbeiten;
	}

	public void setPopUpSerien(boolean popUpSerien) {
		this.popUpSerien = popUpSerien;
	}
//	getIndex().setAllowGetFromRight(true);
//	getIndex().setMarkImportDatum(true); // Sortierdatum besonders markieren 
//	getIndex().setPopUpLoeschen(true);
//	getIndex().setPopUpLoeschenAufheben(true);
	
	// ======================================================
	// setGlobalCutSize()/getGlobalCutSize()
	//
	// Wenn ungleich null gilt diese CutSize f�r ALLE Bilder.
	// (z.B. f�r Export, wenn die CutSize ge�ndert und f�r alle 
	//  Bilder gelten soll)
	// ======================================================
	public void setGlobalCutSize(double cutGlobalSize) {
		this.cutGlobalSize = cutGlobalSize;
	}
	public double getGlobalCutSize() {
		return cutGlobalSize;
	}
	
	
	
	
	public boolean getAusschneiden() {
		return ausschneiden;
	}
	
	 
	public boolean getDrehenSpiegeln( ) {
		return drehenSpiegeln;
	}
	
	// ============================================================
	// setMarkImportDatum()
	// getMarkImportDatum()
	//
	// Das Importdatum (Sortierdatum) wird bei der PictureView-Ausgabe
	// besonders markiert.
	// =============================================================
	public void setMarkImportDatum(boolean markImportDatum) {
		this.markImportDatum = markImportDatum;
	}

	public boolean getMarkImportDatum() {
		return markImportDatum;
	}
	
	
	 
	
	/** 
	 * returns the list of all selected pictures
	 */
	public List<PM_Picture> getSelectedPictures() {
		List<PM_Picture> list = new ArrayList<PM_Picture>();
		int from = index.data.pictureSelected.nextSetBit(0);
		for (int i = from; i >= 0; i = index.data.pictureSelected.nextSetBit(i+1)) {
			if (i >= index.data.getPictureSize()) {
				break;
			}
			list.add(index.data.getPicture(i));
		 }		
		return list;
	}
	
	
	// =====================================================================
	// flushPictureViewThumbnail()
	//
	// Wenn es ein PictureView gibt wird dort das Thumbnail 
	// gel�scht.
	// Erforderlich, wenn �nderungen im Drehen, spiegeln ... erforderlich sind.
	// ======================================================================
	public void rereadPictureViewThumbnail(PM_Picture picture) {	
//		if (indexViewCollection.hasPictureView(picture)) {
//			indexViewCollection.getPictureView(picture).flushImageThumbnail();
//			indexViewCollection.getPictureView(picture).repaint();
//		}
	}
	public void rereadAllPictureViewThumbnail() {	
		// ALLE Thumbs neu lesen und zeichnen	
//		indexViewCollection.rereadAllPictureViewThumbnail( );
//		paintViewPort(-1);
  
	}
	
	
 
	
	
	public PM_Picture getFirstPictureSelected() {
	/*
		int index = pictureSelected.nextSetBit(0);
		
		//  (index < 0 || index >= size()
		
		if (index < 0 || index >= pictureList.size()) {
			return null;
		}
		
		return pictureList.get(index);
	*/	
		return null;
	}
	
	
	
	/**
	 * returns a cloned list of all displayed pictures.
	 * 
	 * Without hidden and closed.
	 * (Please note: a hidden picture can be closed)
	 */
	public List<PM_Picture> getPictureListDisplayed() { 
//		BitSet picViewed = index.data.pictureViewed;	
		List<PM_Picture> list = new ArrayList<PM_Picture>(index.data.getPictureSize());
		for (int i=0; i<index.data.getPictureSize(); i++) {
	//		if (!picViewed.get(i)) {
	//			continue; // not to display
	//		}
			PM_Picture p = index.data.getPicture(i);
			if (index.indexView.getPictureClosed().contains(p)) {
				continue; // not to display
			}
			list.add(p);
		}
		return list;
	}
	
	 
	
	/**
	 * returns size of pictures displayed
	 * 
	 * Without hidden and closed.
	 * (Please note: a hidden picture can be closed)
	 */
	public int sizeDargestellt() {
		int size = 0;
	//	BitSet picViewed = index.data.pictureViewed;
		for (int i=0; i<index.data.getPictureSize(); i++) {
	//		if (!picViewed.get(i)) {
	//			continue; // not to display
	//		}
			PM_Picture p = index.data.getPicture(i);
			if (index.indexView.getPictureClosed().contains(p)) {
				continue; // not to display
			}
			size++;
		}
		return  size;
	}
	
	
	/**
	 * 
	 */
	public boolean isPictureClosed(PM_Picture picture) {
		return indexView.getPictureClosed().contains(picture);
	}
	
	/**
	 * Returns the size of closed pictures.
	 */
	public int getPictureClosedSize() {
		return indexView.getPictureClosed().size();
	}
	
	
	public boolean isPictureSelected(PM_Picture picture) {
		int ind  = index.data.indexOf(picture);
		if (ind  < 0) {
			return false; // picture not found
		}
		return index.data.pictureSelected.get(ind);
	}
	
 
	/**
	 * Select the next or previous picture.
	 */
	public void selectNextPictureView(PM_Picture  picture , int keyCode) {
	 
		PM_Picture  pic = picture;
		if (keyCode == KeyEvent.VK_RIGHT) {
			pic = index.data.getNextPicture(pic);
		} else if (keyCode == KeyEvent.VK_LEFT)  {
			pic = index.data.getPreviousPicture(pic);			
		} else if (keyCode == KeyEvent.VK_UP)  {
			pic = index.data.getPictureUp(pic);
		} else if (keyCode == KeyEvent.VK_DOWN)  {
			pic = index.data.getPictureDown(pic);	
		} else {
			return;
		}

		if (pic == null) {
			return; // first or last thumb was selected.
		}
 
		// Select the next or previous.
		index.data.pictureSelected.clear();
		index.data.pictureSelected.set(index.data.indexOf(pic) );
		indexView.requestFocus(pic);
		repaintViewport();
		 
		indexView.printNumberSelectedPicturesOnStatusLine( );
	 
	}
	

	/**
	 * Set the BitSet "pictureSelected" if mouse pressed.
	 *  
	 * @return true - refreshViewport is needed
	 */
	protected boolean mousePressed(PM_Picture picture, boolean controlDown, boolean shiftDown) {
		int ind = index.data.indexOf(picture);
		if (ind < 0) {
			return false;  // selection invalid
		}
		BitSet pictureSelected = index.data.pictureSelected;
		
		// ------ NO control and NO shift ---------------------
		if (controlDown == false && shiftDown == false) {
			if (!pictureSelected.get(ind)) {
				// picture is NOT selected: 
				// Select this, delesect all other
				pictureSelected.clear();
				pictureSelected.set(ind);
				return true;
			}  
			// IS selected: do nothing.
			return false;
		}  
		// ------- DOWN control and NO shift -----------
		if (controlDown == true && shiftDown == false) {
			// do nothing
			return false;
		}
		// -------- NO control and DOWN shift -------
		if (controlDown == false && shiftDown == true) {
			// from last with control selected to this.
			pictureSelected.clear();
			int min = Math.min(lastChanged, ind);
			int max = Math.max(lastChanged, ind);
			pictureSelected.set(min, max + 1);
			return true;
		}
		// -------- DOWN control and DOWN shift -----------
		// do nothing ????
		return false;
		
	}
	
	/**
	 * Set the BitSet "pictureSelected" if mouse released.
	 *  
	 * @return true - refreshViewport is needed
	 */
	protected boolean mouseReleased(PM_Picture picture, boolean controlDown, boolean shiftDown) {
		int ind = index.data.indexOf(picture);
		if (ind < 0) {
			return false;  // selection invalid
		}
		BitSet pictureSelected = index.data.pictureSelected;
		
		// ------ NO control and NO shift ---------------------
		if (controlDown == false && shiftDown == false) {
			if (pictureSelected.get(ind)) {
				// picture IS selected: 
				// Select this, delesect all other
				pictureSelected.clear();
				pictureSelected.set(ind);
				return true;
			} 
			// NOT selected: do nothing
			return false;
		}  
		// ------- DOWN control and NO shift -----------
		if (controlDown == true && shiftDown == false) {
			lastChanged = ind;
			// toggle the selection.
			pictureSelected.flip(ind);
			return true;
		}
		// -------- NO control and DOWN shift -------
		if (controlDown == false && shiftDown == true) {
			// do nothing		 
			return false;
		}
		// -------- DOWN control and DOWN shift -----------
		// do nothing ????
		return false;
		
	}
	
	 
	
	
 
 
	protected void setAllPicturesSelected() {	
		index.data.pictureSelected.set(0,index.data.getPictureSize());
	}
 	
	 
 
	
	
}