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

import pm.index.*;
import pm.picture.*;
import pm.utilities.*;
 

/**
 *  (1) create the dates for upper panel
 *  (2) make the changes
 */
public class PM_WindowGroupData implements PM_Interface {

	private final PM_Index  index;

	private SortedSet<String>  gleicheIndizes1 = new TreeSet<String>();
	private SortedSet<String>  gleicheIndizes2 = new TreeSet<String>();
	private boolean ungleicheIndizes1 = false;
	private boolean ungleicheIndizes2 = false;

	private int qs = QS_UNBEKANNT;

	 
	public PM_WindowGroupData(
			PM_Index   index) {
		this.index  = index ;
	}

	// ======================================================
	// setValues ()
	//
	// Die "pictureViewCollection" hat sich ge�ndert.
	// Hier jetzt die neuen Werte f�r die Darstellung im "UpperPanel"
	// neu ermitteln.
	// ======================================================
	public void setValues() {

		RetSortedSet ret = getAllEqualsIndices(IndexType.INDEX_1);
		gleicheIndizes1 = ret.sortedSet;
		ungleicheIndizes1 = ret.ungleiche;

		ret = getAllEqualsIndices(IndexType.INDEX_2);
		gleicheIndizes2 = ret.sortedSet;
		ungleicheIndizes2 = ret.ungleiche;

		qs = getQSFromAll();
	}

	protected SortedSet<String> getGleicheIndizes1() {
		return gleicheIndizes1;
	}

	protected SortedSet<String> getGleicheIndizes2() {
		return gleicheIndizes2;
	}

	protected boolean hasUngleicheIndizes1() {
		return ungleicheIndizes1;
	}

	protected boolean hasUngleicheIndizes2() {
		return ungleicheIndizes2;
	}

	protected int getQs() {
		return qs;
	}

	protected boolean hasThumbs() {
		return index.controller.sizeDargestellt() != 0;
	}

	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================
	// ================= PRIVATE
	// =======================================================

	// ==============================================================================
	// getAllEqualsIndices()
	//
	// return: alle gleichen und ob es ungleiche gibt
	// ==============================================================================
	private class RetSortedSet {
		public SortedSet<String> sortedSet;
		public boolean ungleiche;

		public RetSortedSet(SortedSet<String> sortedSet, boolean ungleiche) {
			this.sortedSet = sortedSet;
			this.ungleiche = ungleiche;
		}
	}

	private RetSortedSet getAllEqualsIndices(IndexType indexType) {
		boolean ungleiche = false;
		// ----------------------------------------------------------------
		// Alle gleichen ermitteln und ob es ungleiche gibt.
		// ----------------------------------------------------------------

		SortedSet<String> sortedSet = new TreeSet<String>();
 
		// jetzt den Rest durchhuehnern
		boolean erstesBild = true;
		List<PM_Picture> pictures = index.controller.getPictureListDisplayed();
		for (PM_Picture picture: pictures) {
			if (erstesBild) {
				erstesBild = false;
				sortedSet = getSortedSetIndex(indexType, picture);
				continue; // der erste darstellbare wurde erzeugt
			}
			int anz = sortedSet.size();
			SortedSet<String> sortedSetMeta = getSortedSetIndex(indexType, picture);
			sortedSet.retainAll(sortedSetMeta); // der Durchschnitt
												// (Intersektion) bleibt
												// erhalten
			if (sortedSetMeta.size() != sortedSet.size()
					|| anz != sortedSetMeta.size()) ungleiche = true;
		}
 
		return new RetSortedSet(sortedSet, ungleiche);
	}

	// ==============================================================================
	// getSortedSetIndex1()
	//
	// Aus den Metadaten die Indices holen (f�r Index-1 oder Index-2 je nach
	// Parameter)
	// ==============================================================================
	private SortedSet<String> getSortedSetIndex(IndexType indexType, PM_Picture picture) {
		if (indexType == IndexType.INDEX_1) {
			return PM_Utils.getSortedSet(picture.meta.getIndex1());  
		} else {
			return PM_Utils.getSortedSet(picture.meta.getIndex2());
		}
	}


	
	
	// ================================================================================
	// getQS()
	// 
	// Holten die Qualit�t, die in allen Thumbs identische ist oder
	// QS_UNBEKANNT, wenn einige oder alle unterschiedlich sind
	//
	// (das Folgende ist ganz, ganz schrecklich programmiert !!!!!!!!!!!!!!!!)
	// ( pfui !!!)
	// =================================================================================

	private int getQSFromAll() {

		 

		boolean q1 = false;
		boolean q2 = false;
		boolean q3 = false;
		boolean q4 = false;
		List<PM_Picture> pictures = index.controller.getPictureListDisplayed();
		for (PM_Picture picture : pictures) {
			switch (picture.meta.getCategory()) {
			case QS_1:
				q1 = true;
				break;
			case QS_2:
				q2 = true;
				break;
			case QS_3:
				q3 = true;
				break;
			case QS_4:
				q4 = true;
				break;
			}
		}
 

		if (q1 && (q2 || q3 || q4)) return QS_UNBEKANNT;
		if (q2 && (q3 || q4)) return QS_UNBEKANNT;
		if (q3 && q4) return QS_UNBEKANNT;

		if (q1) return QS_1;
		if (q2) return QS_2;
		if (q3) return QS_3;
		if (q4) return QS_4;

		return QS_UNBEKANNT;
	}

}
