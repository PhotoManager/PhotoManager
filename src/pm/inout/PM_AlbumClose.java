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

import pm.gui.*;
import pm.picture.*;
import pm.search.*;
 


public class PM_AlbumClose {

	private PM_WindowMain windowMain;
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_AlbumClose(PM_WindowMain windowMain ) {
		this.windowMain = windowMain;
	}
	
	
	// ======================================================
	// start()
	// ======================================================
	public void start() {
		if ( !windowMain.requestToClose()) {
			return;
		}
		
		
 		PM_MetadataContainer.getInstance().flush();		 				
 		PM_DatabaseLucene.getInstance().flush();
 		
 		
 		
		PM_SequencesInout.getInstance().close();
		  
		 
//		PM_XML_File_Einstellungen.getInstance().close();
		
		
		windowMain.closeAlbum();
		
		
		
		
		
	}
	
	
	
}
