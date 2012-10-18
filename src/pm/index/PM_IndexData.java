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

import java.awt.Point;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import pm.picture.*; 
import pm.picture.PM_Import.PictureMiniSequence;
import pm.utilities.PM_Action;
import pm.utilities.PM_Listener;
import pm.utilities.PM_Utils;

public class PM_IndexData {

	 

	/**
	 * All the pictures included the hidden and closed.
	 */
	private final List<PM_Picture> pictureList = new CopyOnWriteArrayList<PM_Picture>();
	/**
	 * All pictures displayed have the corresponding bits set to true. All
	 * hidden pictures bits are set to false.
	 */
//	protected final BitSet pictureViewed = new BitSet();
	/**
	 * All pictures selected have the corresponding bits set to true.
	 */
	protected final BitSet pictureSelected = new BitSet();

	private PM_Index index;

	private Vector<PM_Listener> changeListener = new Vector<PM_Listener>();

	/**
	 * constructor
	 */
	protected PM_IndexData(PM_Index index) {
		this.index = index;

	}

	/**
	 * Add a picture to be displayed as thumb.
	 * 
	 * @return false: the picture is already available
	 */

	/**
	 * Append pictures to the picture list and paint them.
	 * 
	 */
	public boolean appendPictureList(List<PM_Picture> pictures) {
		if (pictureList.isEmpty()) {
			clearAndAdd(pictures);
			return true;
		}
		int oldSize = pictureList.size();
		// add unknown pictures
		for (PM_Picture pic : pictures) {
			if (!pictureList.contains(pic)) {
				pictureList.add(pic);
			}
		}
		if (oldSize == pictureList.size()) {
			return false; // all pictures known
		}
		// update bit sets
//		pictureViewed.set(oldSize, pictureList.size(), true);
		pictureSelected.set(oldSize, pictureList.size(), false);
		// fire all changeListener
		fireChangeListener("appendPictureList", pictureList.size());
		return true;
	}

	public void clearAndAdd(List<PM_Picture> pictures) {
		// Clear all lists
		pictureList.clear();
		pictureSelected.clear();
		// add the pictures and update the bit sets
		pictureList.addAll(pictures);
 
		// fire all changeListener
		fireChangeListener("cleadAndAdd", pictureList.size());
	}

	/**
	 * Set a picture list to import.
	 * 
	 * The import is handled in the classes PM_IndexViewImport and PM_Import.
	 */
	public boolean addImport(List<PM_Picture> pictures,
			Map<PM_Picture, PictureMiniSequence> miniSequenceNumbers) {

		if (!(index.indexView instanceof PM_IndexViewImport)) {
			return false; // IndexView not for import
		}
		PM_IndexViewImport indexViewImport = (PM_IndexViewImport) index.indexView;
		indexViewImport.start(pictures, miniSequenceNumbers);

		// Clear all lists
		pictureList.clear();
		pictureSelected.clear();
		// add the pictures and update the bit sets
		pictureList.addAll(pictures);

		// fire all changeListener
		fireChangeListener("addImport", pictureList.size());

		return true;
	}
	
	/**
	 * Insert a new picture into the picture list at an intended position.
	 * 
	 * The "newPicture" picture and all subsequent shifts to the right of the
	 * "toPostion" picture. If "toPositon" picture not found the new picture
	 * appends at the end of the list.
	 * 
	 * @param newPicture
	 *            - the new picture to insert into the pictureList
	 * @param toPostion
	 *            - position to insert the new picture
	 * 
	 * @return boolean - false if the picture to insert already known
	 */

	/**
	 * Move a picture in the pictureList to a new position.
	 */

	// ===========================================================================

	/**
	 * Returns the number of all the pictures.
	 * 
	 * The number of pictures are included the hidden and closed.
	 */
	public int getPictureSize() {
		return pictureList.size();
	}

	/**
	 * Returns the over all picture list.
	 * 
	 * Do not change the list !!!!
	 * (Do not change this method to public)
	 */
	protected List<PM_Picture> getPictureList() {
		return pictureList;
	}
	
	/**
	 * Add change listener
	 */
	public void addChangeListener(PM_Listener listener) {
		if (!changeListener.contains(listener))
			changeListener.add(listener);
	}

	/**
	 * fire the change listener
	 */
	protected void fireChangeListener(String str, int size) {
		for (PM_Listener listener : changeListener) {
			// Object object, int type, String str
			listener.actionPerformed(new PM_Action(this, size, str));
		}
		if (!index.isLeft()) {
			// update the dashed line around the pictured.
 			PM_Index.indexLeft.controller.repaintViewport();
		}
		index.controller.indexView.printNumberSelectedPicturesOnStatusLine( );
	}

	/**
	 * append a picture to the picture list and paint the viewport.
	 * 
	 */
	public boolean addPicture(PM_Picture picture) {
		if (pictureList.contains(picture)) {
			return false;
		}
		pictureList.add(picture);
//		pictureViewed.set(pictureList.size() - 1, true);
		pictureSelected.set(pictureList.size() - 1, false);

		// fire all changeListener
		fireChangeListener("addPicture", 1);
		
		return true;
	}

	public boolean insertPicture(PM_Picture newPicture, PM_Picture toPostion) {
		if (pictureList.isEmpty()) {
			return false;
		}
		if (pictureList.contains(newPicture)) {
			return false; // the picture to insert already known
		}
		int toIndex = pictureList.indexOf(toPostion);
		if (toIndex < 0) {
			// toPostion unknown. Append the new picture to the list.
			return addPicture(newPicture);
		}
		// insert the picture and shift the BitSets
		pictureList.add(pictureList.indexOf(toPostion), newPicture);
//		shiftToRight(pictureViewed, pictureList.size(), toIndex, true);
		shiftToRight(pictureSelected, pictureList.size(), toIndex, false);
		// fire all changeListener
		fireChangeListener("insertPicture", pictureList.size());
		return true;
	}

	/**
	 * Insert a picture list into picture position.
	 */
	public boolean insertPictureList(List<PM_Picture> list, PM_Picture toPosition) {
		if (toPosition == null) {
			return appendPictureList(list);
		}
		return insertPictureList(list, pictureList.indexOf(toPosition));	
	}
	
	 
	public boolean insertPictureList(List<PM_Picture> list, int toIndex) {
		if (toIndex < 0 || toIndex > pictureList.size()) {
			// toPostion unknown. Append the list.
			toIndex = pictureList.size();
		}
		// check if there are pictures known
		List<PM_Picture> unknown = getUnknown(list);
		if (unknown.isEmpty()) {
			return false;
		}
		// insert the picture list
		pictureList.addAll(toIndex, unknown);
		// Select the just added pictures
		pictureSelected.clear();
		for (PM_Picture p: unknown) {
			pictureSelected.set(pictureList.indexOf(p));
		}	
		// fire all changeListener
		fireChangeListener("insertPictureList", pictureList.size());
		return true;
	}
	
	 
	
	public boolean movePictureList(PM_Picture fromPicture,
			List<PM_Picture> picMoveList, PM_Picture toPicture) {
		
		if (fromPicture == toPicture || picMoveList.isEmpty()) {
			return false;
		}
		int indexToPostion = pictureList.indexOf(toPicture);
		if (toPicture == null || indexToPostion < 0) {
			// append
			pictureList.removeAll(picMoveList);
			return appendPictureList(picMoveList);		
		}
		
 		picMoveList.remove(toPicture);
 		pictureList.removeAll(picMoveList);
 		
 		if (indexToPostion == pictureList.indexOf(toPicture)) {
 			pictureList.addAll(indexToPostion, picMoveList);
 		} else {
 			pictureList.addAll(pictureList.indexOf(toPicture) + 1, picMoveList);
 		}
 		
 		pictureSelected.clear();
 		pictureSelected.set(pictureList.indexOf(fromPicture));
 		for (PM_Picture p: picMoveList) {
 			if (pictureList.contains(p)) {
 	 			pictureSelected.set(pictureList.indexOf(p));
 			}
 		}
 		
		fireChangeListener("insertPictureList", pictureList.size());		
		return true;
	}
	
	
	/**
	 * Remove some pictures from the list.
	 *  
	 */
	public void removePictureList(List<PM_Picture> removeList) {
		int oldSize = pictureList.size();
		// add unknown pictures
		for (PM_Picture pic : removeList) {
			if (pictureList.contains(pic)) {
				pictureList.remove(pic);
			}
		}
		if (oldSize == pictureList.size()) {
			return; // nothing to remove
		}
		// Clear and set lists
		pictureSelected.clear();
		index.indexView.getPictureClosed().clear();
		fireChangeListener("removePictureList", 0);
	}

	/**
	 * remove all pictures
	 */
	public void removeAllPictures() {
		pictureList.clear();
		pictureSelected.clear();
		index.indexView.getPictureClosed().clear();
		fireChangeListener("remove all", 0);
	}
	
	
	/**
	 * hide or show the mini sequence (toggle)
	 */
	public void hideShowMiniSequence(PM_Picture picture) {
		
		int picIndex = pictureList.indexOf(picture);
		if (picIndex < 0) {
			return;  // picture not found
		}
		
		if (!picture.meta.hasMiniSequence()) {
			return;
		}
		int seqNumber = picture.meta.getMiniSequenceNumber();
		int removed = 0;  // pictures removed
		for (PM_Picture pic: pictureList) {
			if (pic == picture) {
				continue;
			}
			if (pic.meta.hasMiniSequence() 
					&& pic.meta.getMiniSequenceNumber() == seqNumber) {
				pictureList.remove(pic);
				removed++;
			}
		}
		if (removed > 0) {
			// I have compressed. Select the picture.
			pictureSelected.clear();
			pictureSelected.set(pictureList.indexOf(picture));
			// fire all changeListener
			fireChangeListener("hideShowMiniSequence", 0);
			return;
		}
		// The index view is compressed. Decompress it.
		List<PM_Picture> miniSequence = PM_Utils.getMiniSequence(picture);
		if (miniSequence.isEmpty()) {
			return;
		}
		pictureList.remove(picIndex); // this picture is in the miniSequence 
		insertPictureList(miniSequence, picIndex);
		
		// fire all changeListener
		fireChangeListener("hideShowMiniSequence", 0);	
		
	} 
	
	/**
	 * get the next visible picture.
	 */
	public PM_Picture getNextPicture(PM_Picture picture) {
		if (!hasPicture(picture)) {
			return picture;
		}
		int ind = pictureList.indexOf(picture) + 1;
		if (ind >= pictureList.size()) {
			return picture;
		}
		return pictureList.get(ind);
	}

	/**
	 * get the next visible picture.
	 */
	public PM_Picture getPreviousPicture(PM_Picture picture) {
		if (!hasPicture(picture)) {
			return picture;
		}
		int ind = pictureList.indexOf(picture) - 1;
		if (ind < 0) {
			return picture;
		}
		return pictureList.get(ind);		 
	}
	
	public PM_Picture getPictureUp(PM_Picture picture) {
		if (!hasPicture(picture)) {
			return picture;
		}
		int colRowSize = index.controller.indexView.getColRowSize(pictureList.size()).x;
		int ind = pictureList.indexOf(picture) - colRowSize;
		if (ind < 0) {
			return picture;
		}
		return pictureList.get(ind); 
	}
	
	public PM_Picture getPictureDown(PM_Picture picture) {
		if (!hasPicture(picture)) {
			return picture;
		}
		int colRowSize = index.controller.indexView.getColRowSize(pictureList.size()).x;
		int ind = pictureList.indexOf(picture) + colRowSize;
		if (ind >= pictureList.size()) {
			return picture;
		}
		return pictureList.get(ind);
	}
	
	
	// =================================================================================

	protected PM_Picture getPicture(int i) {
		return pictureList.get(i);
	}

	public boolean hasPicture(PM_Picture picture) {
		return pictureList.contains(picture);
	}

	protected int indexOf(PM_Picture picture) {
		return pictureList.indexOf(picture);
	}

	 
	/**
	 * Get unknown pictures from the list.
	 * 
	 * 
	 */
	private List<PM_Picture> getUnknown(List<PM_Picture> list) {
		List<PM_Picture> unknown = new ArrayList<PM_Picture>(list);
		unknown.removeAll(pictureList);
		return unknown;
	}
	
	
	
	/**
	 * Shift a BitSet from an index to right.
	 * 
	 * @param bs
	 *            the BitSet
	 * @param size
	 *            the size of the BitSet
	 * @param bitIndex
	 *            from this index shift to right
	 * @param value
	 *            to set at bitIndex after shift
	 */
	public void shiftToRight(BitSet bs, int size, int bitIndex, boolean value) {
		for (int i = size; i >= bitIndex; i--) {
			bs.set(i + 1, bs.get(i));
		}
		bs.set(bitIndex, value);
	}

}
