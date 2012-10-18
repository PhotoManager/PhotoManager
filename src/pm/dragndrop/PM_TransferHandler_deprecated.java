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
package pm.dragndrop;

 

import javax.swing.*;

import pm.index.*;
 

import java.awt.datatransfer.*;
 

/**
 * The only TranfsferHandler for the application.
 * <p>
 * Created only once (singleton) and use it for all drag and drop's.
 * <p>
 * Export:
 * 		getSourceAction()
 * 		createTransferable()
 * 		exportDone()
 * Import:
 * 		canImport()
 * 		importData()
 * 
 */
@SuppressWarnings("serial")
public class PM_TransferHandler_deprecated extends TransferHandler {

	 

}
