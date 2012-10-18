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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import pm.dragndrop.PM_PictureViewDragAndDrop;
import pm.index.*;
import pm.utilities.PM_Utils;

// aktiver Focus in einer Component, die nicht unbedingt Focusable ist

@SuppressWarnings("serial")
public class PM_FocusPanel extends JComponent {

	private Component lastFocus = null; // letzter (oder auch aktiver) Focus in
										// diesem FocusPanel

	// container muss final werden, da es NIE geaendert werden darf !!!
	private Component container = null; // in dieser Componente befindet sich
										// der aktivFocus

	private List focusListe = null;

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_FocusPanel() {

	}

	public PM_FocusPanel(List focusListe, Component container,
			Component lastFocus) {
		this.container = container;
		this.lastFocus = lastFocus;
		this.focusListe = focusListe;
	}

	// ==========================================================
	// getContainer()
	// ==========================================================
	public List getFocusListe() {
		return focusListe;
	}

	public void setFocusListe(List focusListe) {
		this.focusListe = focusListe;
	}

	// ==========================================================
	// getContainer()
	// ==========================================================
	public Component getContainer() {
		return container;
	}

	public void setContainer(Component container) {
		this.container = container;
	}

	// ==========================================================
	// getLastFocus()/setLastFocus()
	// ==========================================================
	public Component getLastFocus() {
		return lastFocus;
	}

	public void setLastFocus(Component lastFocus) {
		this.lastFocus = lastFocus;
	}

	// ==========================================================
	// hasFocusOwner()
	// ==========================================================
	public boolean hasFocusOwner(Component focusOwner) {
		if (PM_Utils.isAncestorOf(focusOwner, container)) {
			return true;
		}
		return false;
	}

	// ==========================================================
	// setBackgroundColor()
	// ==========================================================
	public void setBackgroundColor(Color color) {
		// Wird �berschrieben !!
	}

	// ==========================================================
	// getNextFocus()
	//
	// liefert den n�chsten Focus (ab lastAktivFocus) entsprechend der
	// Pfeiltasten (up, down, left oder right)
	//
	// (diese Instanz hier (this) von PM_FocusPanel hat den FocusOwner;
	// das ist bei Aufruf Sichergestellt)
	//
	// setLastFocus(neuerFocus) wird erst im PropertyListener in WindowMain
	// aufgerufen
	// (denn erst dann ist ja der Focus tats�chlich neu gesetzt worden)
	// 
	// Achtung: �bergabe von windowMain ist HACK #1 !!!!!!
	// ==========================================================
	public Component getNextFocus(Component focusOwner, int pfeilTasten,
			PM_WindowMain windowMain) {

		// ------------------------------------------------------------------------
		// In Thumbnails navigieren
		// ------------------------------------------------------------------------
		if (this.getContainer() instanceof PM_IndexView) {
			if (!(focusOwner instanceof PM_PictureViewDragAndDrop))
				return null;
			PM_IndexView  indexView = (PM_IndexView ) this.getContainer();
//	**** ???????????		indexView.doSelectNextPictureView(
//					((PM_PictureViewDragAndDrop) focusOwner).getPicture(), pfeilTasten);
			return null;
		}

		// ------------------------------------------------------------------------
		// Wenn KEINE Thumbnails
		// ------------------------------------------------------------------------
		if (focusListe == null) {
			return null; // KEINE focusListe
		}

		Component focusNeu = null;

		// ---------------------------------------------------------------------
		// jetzt die FocusListe durchh�hnern
		// ---------------------------------------------------------------------

		// System.out.println("###### focusliste gefu. Anzahl Zeilen = " +
		// focusListe.size());

		List zeile = getZeileFocusOwner(focusOwner);
		if (zeile == null) {
			return null; // Zeile mit FocusOwner nigefu
		}
		int index = getIndexFocusOwnerInZeile(focusOwner, zeile);
		// System.out.println("###### index in Zeile = " + index);
		if (index == -1) {
			return null; // Zeile mit FocusOwner nigefu
		}

		if (pfeilTasten == KeyEvent.VK_LEFT || pfeilTasten == KeyEvent.VK_RIGHT) {

			focusNeu = pfeilLeftRight(pfeilTasten, focusOwner, zeile, index,
					windowMain);
			return focusNeu;
		}
		if (pfeilTasten == KeyEvent.VK_UP || pfeilTasten == KeyEvent.VK_DOWN) {
			focusNeu = pfeilUpDown(pfeilTasten, focusOwner, zeile, index);
			return focusNeu;
		}

		return null;
	}

	// ================================= PRIVATE
	// =======================================
	// ================================= PRIVATE
	// =======================================
	// ================================= PRIVATE
	// =======================================
	// ================================= PRIVATE
	// =======================================

	// ======================================================
	// pfeilLeftRight()
	// ======================================================
	private Component pfeilLeftRight(int key, Component focusOwner, List zeile,
			int index, PM_WindowMain windowMain) {
		// System.out.println("... pfeilLeftRight");
		Component focusNeu = getNextPrevFocus(key, zeile, index);
		if (focusNeu == null)
			return null;

		if (focusNeu instanceof JTextField) {
			// HACK # 1
			// focusNeu merken.
			// Wenn erneuter Aufruf, dann NICHT bearbeiten
			windowMain.textFieldPressed = (JTextField) focusNeu;
		}

		return focusNeu;
	}

	// ======================================================
	// pfeilUpDown()
	// ======================================================
	private Component pfeilUpDown(int key, Component focusOwner, List zeile,
			int index) {
		List next = getNextPrevZeile(key, zeile);
		if (next == null)
			return null;
		int neu = index;
		if (index >= next.size())
			neu = next.size() - 1; // falls neue Zeile k�rzer
		Component focus = (Component) next.get(neu);
		return focus;
	}

	// ======================================================
	// getZeileFocusOwner()
	//
	// In der FoccusListe Zeile mit dem FocusOwner suchen
	// ======================================================
	private List getZeileFocusOwner(Component focusOwner) {
		Iterator it = focusListe.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof ArrayList))
				continue;
			if (getIndexFocusOwnerInZeile(focusOwner, (ArrayList) o) != -1) {
				return (ArrayList) o;
			}
		}
		return null;
	}

	// ======================================================
	// getIndexFocusOwnerInZeile()
	//
	// Return mit Index vom focusOwner in der Zeile
	// ======================================================
	private int getIndexFocusOwnerInZeile(Component focusOwner, List zeile) {
		int index = -1;
		Iterator it = zeile.iterator();
		while (it.hasNext()) {
			index++;
			Component c = (Component) it.next();
			if (c == focusOwner) {
				return index;
			}
		}
		return -1; // nigefu

	}

	// ======================================================
	// getNextPrevZeile()
	// ======================================================
	private List getNextPrevZeile(int key, List zeile) {
		int index = -1;
		Iterator it = focusListe.iterator();
		// ---- Suchen Zeile in focusListe ----
		while (it.hasNext()) {
			index++;
			Object o = it.next();
			if (!(o instanceof ArrayList))
				continue;
			if (o == zeile) {
				// ---- Zeile gefunden -----
				if (key == KeyEvent.VK_DOWN) {
					// vorw�rts in FocusListe
					index++;
				} else {
					// r�ckw�rts in Focuslist
					index--;
					if (index < 0)
						index = focusListe.size() - 1;
				}
				index = index % focusListe.size();
				return (ArrayList) focusListe.get(index);
			}
		}
		return zeile; // keine gefunden
	}

	// ======================================================
	// getNextPrevZeile()
	// ======================================================
	private Component getNextPrevFocus(int key, List zeile, int index) {
		// der FocusOwner ist in "zeile" am "index"
		if (key == KeyEvent.VK_RIGHT) {
			// rechts in Zeile
			index++;
		} else {
			// links in Zeile
			index--;
			if (index < 0)
				index = zeile.size() - 1;
		}
		index = index % zeile.size();
		return (Component) zeile.get(index);
	}

}
