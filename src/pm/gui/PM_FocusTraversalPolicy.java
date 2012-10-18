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
import java.util.Iterator;
import java.util.List;

 

public class PM_FocusTraversalPolicy extends FocusTraversalPolicy {

	// Jedes Hauptfenster hat eine focusCycleList.
	// Die aktive focusCycleList ist die Liste vom Hauptfenster, das den
	// FocusOwner hat.
	// Mit den "FocusTraversalKeys"  (Tab-Taste) kann nun innerhalb der aktiven
	// focusCycleList zwischen den Unterfenstern (das sind Instanzen der Klasse PM_FocusPanel)
	// gewechselt (traversiert) werden.
	
	
	private final PM_WindowMain windowMain;

 

	// ==========================================================
	// Konstruktor
	// ==========================================================

	public PM_FocusTraversalPolicy(PM_WindowMain windowMain) {
		this.windowMain = windowMain;
	}

	// ==========================================================
	// 
	// ==========================================================
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		List focusCycleList = getFocusCycleList();
		int index = getFocusPanelIndex(focusCycleList);
		if (index == -1) return null;
		return getNextComponent(focusCycleList, index);		
	}

	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		List focusCycleList = getFocusCycleList();
		int index = getFocusPanelIndex(focusCycleList);
		if (index == -1) return null;
		return getPrevComponent(focusCycleList, index);		
	}

	public Component getFirstComponent(Container aContainer) {
		List focusCycleList = getFocusCycleList();
		int index = getFocusPanelIndex(focusCycleList);
		if (index == -1) return null;
		return ((PM_FocusPanel) focusCycleList.get(0)).getLastFocus();
	}

	public Component getLastComponent(Container aContainer) {
		List focusCycleList = getFocusCycleList();
		int index = getFocusPanelIndex(focusCycleList);
		if (index == -1) return null;
		return ((PM_FocusPanel) focusCycleList.get(0)).getLastFocus();
	}

	public Component getDefaultComponent(Container aContainer) {
		List focusCycleList = getFocusCycleList();
		int index = getFocusPanelIndex(focusCycleList);
		if (index == -1) return null;
		return ((PM_FocusPanel) focusCycleList.get(0)).getLastFocus();
	}

	// =========================== PRIVATE
	// ===========================================


	// ==========================================================
	// getFocusCycleList()
	// 
	//  die  focusCycleList vom gerade aktive Hauptfenser holen
	// ==========================================================
	private List getFocusCycleList() {
		PM_WindowBase windowBase = windowMain.getWindowBaseWithFocusOwner();  
		if (windowBase == null)  return null;
		return windowBase.getFocusCycleList();
	}
		
	
	
	// ==========================================================
	// init()
	// 
	// setzen:
	//     focusCycleList (vom Hauptfenster, das den FocusOwner hat)
	//     index vom aktiven FocusPanel (Unter-"Panel", in dem der FocusQwner ist
	// nigefu:  index auf -1 setzen
	// ==========================================================
	private int getFocusPanelIndex(List focusCycleList) {
		
	   if (focusCycleList == null || focusCycleList.size() == 0)  return -1;	 

		// ----------------------------------------------------
		// Suchen FocusPanel (das ist das Unterfenster vom aktiven Hauptfenster),
		// in dem sich der FocusOwner befindet.
		// (er kann sich ja durch Mausklick geändert haben)
		// ----------------------------------------------------
	   int index = -1;
		Iterator it = focusCycleList.iterator();
		Component focusOwner = windowMain.getFocusOwner();
		while (it.hasNext()) {
			index++;
			PM_FocusPanel focusPanel = (PM_FocusPanel) it.next();
			if (focusPanel.hasFocusOwner(focusOwner)) return index;			 		 
		}
		 return -1;
	}
	
	
	
	
	// ==========================================================
	// 
	// ==========================================================
	private Component getPrevComponent(List focusCycleList, int index) {
		// jetzt vorherigen holen
		index--;
		index += focusCycleList.size(); // damit er nicht ins Negative rutscht
		index = index % focusCycleList.size();

		return ((PM_FocusPanel) focusCycleList.get(index))
				.getLastFocus();

	}

	// ==========================================================
	// 
	// ==========================================================
	private Component getNextComponent(List focusCycleList, int index) {
		// jetzt nächsten holen
		index++;
		index = index % focusCycleList.size();

		return ((PM_FocusPanel) focusCycleList.get(index))
				.getLastFocus();

	}

}
