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
package pm.sequence;

import pm.dragndrop.PM_TreeWindowDragAndDrop;
import pm.gui.*;
import pm.utilities.*;
 

@SuppressWarnings("serial")
public class PM_TreeWindowOriginal extends PM_TreeWindowDragAndDrop implements PM_Interface {
	
	
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_TreeWindowOriginal(PM_WindowBase windowBase) {	
		super(windowBase,  PM_TreeModelOriginal.getInstance(), null);     
		setRootVisible(false);
	}

	
}
