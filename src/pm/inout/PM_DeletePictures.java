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
package pm.inout;

import pm.sequence.*;

import pm.utilities.*;

import pm.gui.*;
import pm.picture.*;

import java.io.*;

import java.util.*;

/**
 * Delete pictures physically.
 * 
 * That is the only place where delete pictures physically
 * 
 * 
 */
public class PM_DeletePictures implements PM_Interface {

	private static Set<PM_Listener> deleteListeners = new HashSet<PM_Listener>();

	public PM_DeletePictures() {

	}

	// =====================================================
	// deletePictures()
	//
	// Einzige Stelle, wo Bilder gel�scht werden
	// =====================================================
	public void deletePictures(List<PM_Picture> pictures) {

		Set<String> touchedSequences = new HashSet<String>();

		for (PM_Picture picture : pictures) {
			picture.meta.setInvalid(true);
			List<String> allSequenceNames = new ArrayList<String>();
			for (String str : PM_Utils.getList(picture.meta.getSequence())) {
				String[] sss = str.split(LUCENE_SEQUENZ_TRENNER);
				if (sss.length < 1) {
					continue;
				}
				allSequenceNames.add(sss[0]);
			}
			touchedSequences.addAll(allSequenceNames);
			fireDeleteListener(picture);
		}

		fireDeleteListener(null);
		List<PM_Sequence> listeSeqToTouch = new ArrayList<PM_Sequence>();
		if (!touchedSequences.isEmpty()) {
			PM_TreeModel base = PM_TreeModelBase.getInstance();
			PM_TreeModel extended = PM_TreeModelExtended.getInstance();
			PM_TreeModel newl = PM_TreeModelNew.getInstance();
			List<PM_Sequence> listeGeschlSequenzen = base.getSequenceList();
			listeGeschlSequenzen.addAll(extended.getSequenceList());
			listeGeschlSequenzen.addAll(newl.getSequenceList());
			for (PM_Sequence seq : listeGeschlSequenzen) {
				if (touchedSequences.contains(seq.getShortName())) {
					listeSeqToTouch.add(seq);
					seq.computeNewSize();
					if (seq.getAnzahlBilder() == 0) {
						seq.deleteSequence();
						// aus dem Baum entfernen
						base.removeSequence(seq);
						extended.removeSequence(seq);
						newl.removeSequence(seq);
					}
				}
			}
			return;
		}
	}

	/**
	 * Add the deletion listener.
	 * 
	 */
	public static void addDeleteListener(PM_Listener listener) {
		deleteListeners.add(listener);
	}

	/**
	 * Fire deletion listener.
	 * 
	 * @param picture
	 *            - the deleted picture. null if all pictures deleted.
	 */
	public void fireDeleteListener(PM_Picture picture) {
		for (PM_Listener listener : deleteListeners) {
			listener.actionPerformed(new PM_Action(picture));
		}
	}

} // Ende Klasse
