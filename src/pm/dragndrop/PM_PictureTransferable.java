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

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import pm.index.*;
import pm.picture.*;

/**
 * Creates a transferable for a picture list.
 * 
 * 
 */
public class PM_PictureTransferable implements Transferable {

	private static DataFlavor localObjectFlavor;
	private static DataFlavor uriListFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
			uriListFlavor = new DataFlavor(
					"text/uri-list;class=java.lang.String");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] FLAVORS = { localObjectFlavor, uriListFlavor,
			DataFlavor.javaFileListFlavor };

	
	
	private PM_Index index;
	private List<PM_Picture> pictureList;

	private PM_Picture pictureUnderCursor;
	
	private DataFlavor[] flavors;
	
	public PM_PictureTransferable(PM_Index index, List<PM_Picture> pictureList,PM_Picture pictureUnderCursor, DataFlavor[] flavors) {
		this.index = index;
		this.pictureList = new ArrayList<PM_Picture>(pictureList.size());
		this.pictureList.addAll(pictureList);
		this.flavors = flavors;
		this.pictureUnderCursor = pictureUnderCursor;
	}

	public PM_IndexView getIndexView() {
		return index.indexView;
	}

	public PM_Picture getPictureUnderCursor() {
		return pictureUnderCursor;
		
	}
 
	
	public List<PM_Picture> getPictureList() {
		return pictureList;
	}

 
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {

		if (flavor.equals(localObjectFlavor)) {
			return this;
		} else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
			List<File> fileList = new ArrayList<File>(pictureList.size());
			for (PM_Picture picture : pictureList) {
				fileList.add(picture.getFileOriginal());
			}
			return fileList;
		} else if (flavor.equals(uriListFlavor)) {
			StringBuffer data = new StringBuffer();
			for (PM_Picture picture : pictureList) {
				File file = picture.getFileOriginal();
				data.append(file.toURI() + "\r\n");
			}
			return data.toString();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	 
	public DataFlavor[] getTransferDataFlavors() {
		return flavors; //FLAVORS;
	}

	 
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor df : getTransferDataFlavors()) {
			if (df.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

}
