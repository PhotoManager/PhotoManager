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

import java.awt.Component;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import pm.index.PM_Index;
import pm.index.PM_IndexView;
import pm.picture.PM_Picture;

/**
 * Class to handle the drop of the picture list. 
 *  
 *
 */
public class PM_DropTargetPictureList  implements  DropTargetListener  {

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
	
	
	private DropTarget dropTarget;
	private JComponent component;
 

	/**
	 * Constructor.
	 */
	public PM_DropTargetPictureList( JComponent component) {
		this.component = component;
		 
		dropTarget = new DropTarget(component, DnDConstants.ACTION_COPY,
				this /* DropTargetListener */  );
	}

   
	public void dragEnter(DropTargetDragEvent event) {
	}

 
	public void dragOver(DropTargetDragEvent dtde) {
//System.out.println("PM_DropTargetPictureList:   drag over");
		// TODO Auto-generated method stub

	}
	
	
 
	public void dragExit(DropTargetEvent arg0) {
System.out.println("PM_DropTargetPictureList (DROP):   dragExit");
		
	}

	 
	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
 
	public void drop(DropTargetDropEvent event) {
		
System.out.println("--..--..--PM_DropTargetPictureList:   drop");		

		Transferable transferable = event.getTransferable();
		Object data = null;
		
 
		
		try {
			data = transferable.getTransferData(localObjectFlavor);
		} catch (Exception e) {
			// Not a local object to drop
			event.rejectDrop(); 
			return;
		}  
		
		if (data instanceof PM_PictureTransferable  == false) {
			event.rejectDrop(); 
			return;
		}
		PM_PictureTransferable source = (PM_PictureTransferable)data;
//		PM_Index sourceIndex = source.getIndex();
		List<PM_Picture> pictureList = source.getPictureList();
		
		
		
		
		
		Component target = dropTarget.getComponent();
		 
		if (component instanceof PM_PictureViewDragAndDrop) {
			System.out.println("Target >> PM_PictureViewDragAndDrop");
		}
		if (component instanceof PM_IndexView) {
			System.out.println("Target >> PM_IndexView");
		}
		
	 
		
		
		
		
/*		
	    try {
	        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            java.util.List data = (java.util.List)
	                    transferable.getTransferData(DataFlavor.javaFileListFlavor);
	            // the list contains java.io.File(s)
	            System.out.println(data);
	        } else if (transferable.isDataFlavorSupported(uriListFlavor)) {
	        	Object o = transferable.getTransferData(uriListFlavor);
//	            String data = (String)transferable.getTransferData(uriListFlavor);
//	            System.out.println(textURIListToFileList(data));
	        	System.out.println("Drop uriListFlavor: " + o);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

		
*/		
		
	 	/*
	      try {
	        Transferable tr = event.getTransferable();
	        Color color = (Color) tr.getTransferData(TransferableColor.colorFlavor);
	        if (event.isDataFlavorSupported(TransferableColor.colorFlavor)) {
	          event.acceptDrop(DnDConstants.ACTION_COPY);
	          this.panel.setBackground(color);
	          event.dropComplete(true);
	          return;
	        }
	        event.rejectDrop();
	      } catch (Exception e) {
	        e.printStackTrace();
	        event.rejectDrop();
	      }
	      
	      */
	}
	
	 
	
	 
	public boolean isLocalPictureTransfer(Transferable trans) {
		PM_PictureTransferable tpl = getTransferPictureList(trans);
		return (tpl == null)? false: true;
	}
	
	public PM_PictureTransferable getTransferPictureList(Transferable trans) {
		Object data;
		try {
			data = trans.getTransferData(localObjectFlavor);
		} catch (Exception e) {
			return null;
		}
		if (data instanceof PM_PictureTransferable) {
			return (PM_PictureTransferable)data;
		}
		return null;
	}

	 
	
	
	@SuppressWarnings("unchecked")
	private List<File> getFileList(DropTargetDropEvent event) {
		Transferable trans = event.getTransferable();
		List<File> list = new ArrayList<File>(1);
		try {
			if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				list =  (List<File>)trans.getTransferData(DataFlavor.javaFileListFlavor);
			} else if (trans.isDataFlavorSupported(uriListFlavor)) {
				String data = (String) trans.getTransferData(uriListFlavor);
				list =  textURIListToFileList(data);
			}
		} catch (Exception e) {
			return list;
		}
		return list;
	}

	/**
	 * Parse a data String to a List of files
	 */
	private List<File> textURIListToFileList(String data) {
		List<File> list = new ArrayList<File>();
		for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st
				.hasMoreTokens();) {
			String s = st.nextToken();
			if (s.startsWith("#")) {
				// the line is a comment (as per the RFC 2483)
				continue;
			}
			try {
				java.net.URI uri = new java.net.URI(s);
				java.io.File file = new java.io.File(uri);
				list.add(file);
			} catch (java.net.URISyntaxException e) {
				// malformed URI
			} catch (IllegalArgumentException e) {
				// the URI is not a valid 'file:' URI
			}
		}
		return list;
	}
	
	 

}
