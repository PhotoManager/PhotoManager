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

 
import pm.sequence.*;
import pm.utilities.*;
 
 
import java.awt.datatransfer.*;
import java.io.*; 
import java.util.List;

 

/**
 * f�r alle Drag & Drops
 * 
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_Transferable implements Transferable, PM_Interface {

	public static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] supportedFlavors = { localObjectFlavor };

	
	 
	private Object object;

	// =========================================================
	// Konstruktor
	// =========================================================
	public PM_Transferable(Object c) {
		object = c;
	}

	
	/**
	 * Return the object to transfer.
	 */
	public Object getObject() {
		return object;
	}
	// =========================================================
	// getTransferData()
	//
	// Hier wird das
	// =========================================================
	 
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (isDataFlavorSupported(flavor)) {
			return object;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	// =========================================================
	// getTransferDataFlavors()
	// =========================================================
	 
	public DataFlavor[] getTransferDataFlavors() {		 
		return supportedFlavors;
	}

	// =========================================================
	// isDataFlavorSupported()
	// =========================================================
	 
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return localObjectFlavor.equals(flavor);
	}

	
	
	// =========================================================
	// getDragAndDropType()
	//
	// Aus den Transferdaten wird der DnD Type ermittelt
	// =========================================================
	static public DragAndDropType getDragAndDropType(Transferable trans) {
		Object object;
		try {
			object = trans.getTransferData(localObjectFlavor);
		} catch (UnsupportedFlavorException e) {
			return DragAndDropType.UNKNOWN;
		} catch (IOException e) {
			return DragAndDropType.

			UNKNOWN;
		}

		if (object instanceof List) {
			List l = (List) object;
			if (l.size() < 1) {
				return DragAndDropType.UNKNOWN;
			}
			Object o = l.get(0);
			if (o instanceof DragAndDropType) {
				return (DragAndDropType) o;
			}
		}
		return DragAndDropType.UNKNOWN;
	}

	// =========================================================
	// getSequence()
	//
	// Aus den Transferdaten wird die Sequenz ermittelt
	// =========================================================
	static public PM_Sequence getSequence(Transferable trans) {
		Object object;
		try {
			object = trans.getTransferData(localObjectFlavor);
		} catch (UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		if (object instanceof List) {
			List l = (List) object;
			if (l.size() < 2) {
				return null;
			}
			Object o = l.get(1);
			if (o instanceof PM_Sequence) {
				return (PM_Sequence) o;
			}
		}
		return null;
	}

}
