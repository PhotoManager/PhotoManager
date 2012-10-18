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
 
import pm.utilities.*;
 

public class PM_KeyboardBindings implements PM_Interface {

	private static PM_KeyboardBindings instance = null;
	
	 
	
	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_KeyboardBindings getInstance() {
		if (instance == null) instance = new PM_KeyboardBindings(); 
		return instance;
	}

	   
	// ==========================================================
	// Konstruktor
	// ==========================================================
 	private  PM_KeyboardBindings () {
 		
 	}
  
	
	
 	
 	
 	
	
}
