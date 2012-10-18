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

import pm.index.*;
import pm.picture.*;
import pm.utilities.*;

import java.util.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class PM_WindowGroup extends PM_WindowGroupUI implements PM_Interface {

	public PM_WindowGroup() {
		super();
		einstellungen = PM_Configuration.getInstance();
		buildUI();
	}

	 
	/**
	 * Insert a picture list into picture position.
	 */
	@Override
	public boolean insertPictureList(List<PM_Picture> list, PM_Picture toPosition) {
		 
		if ( !getIndex().data.insertPictureList(list,  toPosition )) {
			return false;	
		}
		setValuesInUpperPanel();
		// mark in the left window
		windowMain.getWindowLeft().getIndex().controller
				.repaintViewport( );
		return true;
	}
	
	
	@Override
	public boolean appendPicture(PM_Picture picture) {

		if (!(getIndex().data.addPicture(picture))) {
			return false; // picture known
		}
		setValuesInUpperPanel();
		// mark in the left window
		windowMain.getWindowLeft().getIndex().controller
				.repaintPicture(picture);
		return true;
	}

	@Override
	public void getAllThumbs(PM_Index ivFrom) {
		super.getAllThumbs(ivFrom);
		setValuesInUpperPanel();
	}

	@Override
	public boolean appendAllThumbs(PM_Index ivFrom) {
		if (super.appendAllThumbs(ivFrom) == false) {
			return false; // all known
		}
		setValuesInUpperPanel();
		return true;
	}

	public boolean requestToChange() {
		return true;
	}

	// ====================================================
	// ====================================================
	// ============= Private =============================
	// ====================================================
	// ====================================================

	// ======================================================
	// doUndo()
	//
	// ======================================================
	protected void doUndo() {
		setValuesInUpperPanel();
	}

	protected void doGruppieren() {
		// Datum holen und pr�fen
		String datum = getDatumMitPruefen();
		final Date date;
		if (datum.equals("-1"))
			return; // Fehler

		if (!datum.equals("0")) {
			String txt = "Sollen alle Bilder ein neues Datum erhalten?"
					+ "\n(ab " + datum + " jedes Bild eine Minute sp�ter)";
			int n = JOptionPane.showConfirmDialog(this, txt,
					"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return; // Keine �nderungen durchf�hren
			} else {
				date = PM_Utils.datumToDate(datum);
			}
		} else
			date = null;

		// Indizes und Datum �bernehmen
		gruppieren(getIndex_1(), getIndex_2(), date);

		// ---------------------------------------------------------------------------------------------
		// �nderungen synchron durchgef�hrt
		// ---------------------------------------------------------------------------------------------
		// Ubernehmen abschliessen
		doGruppierenAbschliessen(); // mit Frage und loeschen

		// alle Bilder zum uebernehmen wurden geloescht.
		// Mit folgendem Aufruf wird alles zurueckgesetzt,
		// weil ja das indexpanel leer ist.
		setValuesInUpperPanel();

	}

	// ======================================================
	// gruppieren ()
	//
	// in "gleicheIndizes" stehen die Indizes, die vor der Gruppierung
	// gleich waren.
	// ======================================================
	private void gruppieren(String index_1, String index_2, Date date) {

		// Vorbereitungen f�r Datums�nderung

		long millisecods = 0;
		final int oneMinute = 60 * 1000; // eine Minute in Millisekunden
		if (date != null) {
			// Das Datum wird ge�ndert
			millisecods = date.getTime();
		}

		List<PM_Picture> pictures = getIndex().controller
				.getPictureListDisplayed();
		for (PM_Picture picture : pictures) {
			String indexChanged = "";
			// ----------------------------------------
			// Index-1
			//
			// LABEL_UNGLEICHE_UEBERNEHMEN = "Es gibt ungleiche Indizes. Sie
			// werden �bernommen";
			// LABEL_UNGLEICHE_LOESCHEN = "Es gibt ungleiche Indizes. Sie werden
			// GEL�SCHT";
			// LABEL_UNGLEICHE_KEINE = "Es gibt KEINE ungleichen Indizes";
			// ----------------------------------------
			if (indizes1Loeschen()) { // LABEL_UNGLEICHE_LOESCHEN
				// nur die "neuen" uebernehmen
				// (Es gibt ungleiche Indizes. Sie werden GEL�SCHT)
				indexChanged = index_1;
				picture.meta.setIndex1(indexChanged);
			} else {
				// alte und neue
				// Aus den alten die "gleichen" entfernen
				SortedSet<String> indizesAlt = PM_Utils
						.getSortedSet(picture.meta.getIndex1());
				indizesAlt
						.removeAll(windowGruppierenDaten.getGleicheIndizes1());
				indexChanged = index_1 + " "
						+ PM_Utils.sortedSetToString(indizesAlt);
				picture.meta.setIndex1(indexChanged);
			}
			// ----------------------------------------
			// Index-2
			// ----------------------------------------
			if (indizes2Loeschen()) {
				// nur die "neuen" uebernehmen
				indexChanged = index_2;
				picture.meta.setIndex2(indexChanged);
			} else {
				// alte und neue
				// Aus den alten die "gleichen" entfernen
				SortedSet<String> indizesAlt = PM_Utils
						.getSortedSet(picture.meta.getIndex2());
				indizesAlt
						.removeAll(windowGruppierenDaten.getGleicheIndizes2());
				indexChanged = index_2 + " "
						+ PM_Utils.sortedSetToString(indizesAlt);
				picture.meta.setIndex2(indexChanged);
			}
			// ------------------------------------------
			// Sortierdatum
			// ------------------------------------------
			if (date != null) {
				// Calendar cal = Calendar.getInstance();
				// cal.setTimeInMillis(millisecods);
				picture.meta.setDateCurrent(new Date(millisecods));
				millisecods += oneMinute;
			}

			// ------------------------------------------
			// QS
			// ------------------------------------------
			String qsChanged = getQsChanged();
			if (qsChanged.length() != 0) {
				picture.meta.setCategory(qsChanged);
			}

		} // for

	}

	private void doGruppierenAbschliessen() {
		if (getIndex().data.getPictureSize() == 0)
			return;

		// ge�nderte Indizes jetz hier �bernehmen

		// fragen, ob links l�schen

		int n = JOptionPane.showConfirmDialog(this, PM_MSG
				.getMsg("winGrpMsgEnd"), "Group", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			// ////////////////////////// doLoeschen();
			return;
		}

		// alle in "pictureViewCollectionGruppiert" in der links
		// dargestellten ("pictureViewCollectionThumbnails") hier loeschen
		indexViewThumbnails.data.removePictureList(getIndex().controller
				.getPictureListDisplayed());

		// alle hier dargestellten loeschen
		// ////////////////////////////7 doLoeschen();

		// links die Tbumbnails updaten
		indexViewThumbnails.controller.repaintViewport_deprecated();
	}

}