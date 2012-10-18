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

import pm.gui.*;
import pm.index.*;
import pm.picture.*;
import pm.utilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.List;

/**
 * 
 */
@SuppressWarnings("serial")
public class PM_PictureViewDragAndDrop extends JComponent implements
		PM_Interface, DragSourceListener, DragGestureListener, Autoscroll {

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
	static DataFlavor[] mayFlavors = { localObjectFlavor, uriListFlavor,
			DataFlavor.javaFileListFlavor };

	private DragSource dragSource;

	private static boolean installInputMapBindings = true;

	protected PM_WindowMain windowMain = null;
	protected PM_Picture picture = null;
	protected PM_Index index;

	/**
	 * Creates the picture view and sets the transfer handler.
	 */
	public PM_PictureViewDragAndDrop(PM_Picture picture,
			PM_WindowMain windowMain, PM_Index index) {

		this.picture = picture;
		this.windowMain = windowMain;
		this.index = index;

		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this /*
															 * Component to
															 * recognize
															 */,
				DnDConstants.ACTION_COPY, this /* DragGestureListener */);

		new PM_DropTargetPictureList(this) {

			@Override
			public void dragEnter(DropTargetDragEvent event) {
				// System.out.println("......drag enter PictureView");

				Transferable transferable = event.getTransferable();
				PM_PictureTransferable data = getTransferPictureList(transferable);
				if (data == null) {
					event.rejectDrag();
					return;
				}
				if (canDrop(data)) {
					event.acceptDrag(DnDConstants.ACTION_COPY);
					return;
				}
				event.rejectDrag();

			}

			@Override
			public void drop(DropTargetDropEvent event) {
				System.out.println(">>>>>>>>>> drop in PictureView");
				Transferable transferable = event.getTransferable();
				PM_PictureTransferable data = getTransferPictureList(transferable);
				if (data == null) {
					return;
				}
				doDrop(data);
			};

		};

		// key-Bindigs f�r cut/copy/paste f�r input map:
		if (installInputMapBindings) {
			InputMap imap = this.getInputMap();
			// imap.put(KeyStroke.getKeyStroke("ctrl X"), TransferHandler
			// .getCutAction().getValue(Action.NAME));
			imap.put(KeyStroke.getKeyStroke("ctrl C"), TransferHandler
					.getCopyAction().getValue(Action.NAME));
			imap.put(KeyStroke.getKeyStroke("ctrl V"), TransferHandler
					.getPasteAction().getValue(Action.NAME));
		}

		// Add the cut/copy/paste actions to the action map.
		// This step is necessary because the menu's action listener
		// looks for these actions to fire.
		ActionMap map = this.getActionMap();
		// map.put(TransferHandler.getCutAction().getValue(Action.NAME),
		// TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
				TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
				TransferHandler.getPasteAction());

	}

	public boolean canDrop(PM_PictureTransferable transferData) {
		return index.indexView.canDrop(transferData);
	}

	public void doDrop(PM_PictureTransferable transferData) {
		index.indexView.doDrop(transferData, picture);
	}

	// ======================================================
	// getWindowBase()
	// holen PM_WindowBase f�r this (das ist ein PM_PictureView)
	// ======================================================
	private PM_WindowBase getWindowBase(PM_Index iv) {
		if (windowMain.getWindowLeft().getIndex() == iv) {
			return windowMain.getWindowLeft();
		} else if (windowMain.getWindowRightSelected().getIndex() == iv) {
			return windowMain.getWindowRightSelected();
		}
		return null;
	}

	// ================== DragSourceListener ================================

	public void dragDropEnd(DragSourceDropEvent dsde) {
		System.out.println("--------- DRAG dragDropEnd. action: "
				+ dsde.getDropAction() + ", success: " + dsde.getDropSuccess());

	}

	public void dragEnter(DragSourceDragEvent dsde) {
		System.out.println("------ DRAG DragSourceListener: drag ENTER");
		DragSourceContext dsx = dsde.getDragSourceContext();
		dsx.setCursor(DragSource.DefaultCopyDrop);
	}

	public void dragExit(DragSourceEvent dse) {
		System.out.println("------ DRAG DragSourceListener: drag EXIT");
		DragSourceContext dsx = dse.getDragSourceContext();
		dsx.setCursor(DragSource.DefaultCopyNoDrop);
	}

	public void dragOver(DragSourceDragEvent dsde) {
		// System.out.println("--DragSourceListener: drag Over");
		Point dropPoint = dsde.getLocation();
		SwingUtilities.convertPointFromScreen(dropPoint, this);
		if (this.contains(dropPoint)) {
			// The cursor location is on the picture view to drag.
			DragSourceContext dsx = dsde.getDragSourceContext();
			dsx.setCursor(DragSource.DefaultCopyNoDrop);
		}

	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	// ========================== DragGestureListener
	// ================================

	public void dragGestureRecognized(DragGestureEvent dge) {
		List<PM_Picture> picList = index.controller.getSelectedPictures();
		if (picList.isEmpty()) {
			return;
		}

		DataFlavor[] mayFlavors = { localObjectFlavor, uriListFlavor,
				DataFlavor.javaFileListFlavor };
		PM_Picture pictureUnderCursor = picture;
		Transferable trans = new PM_PictureTransferable(index, picList,
				pictureUnderCursor, mayFlavors);
		System.out.println("dragGestureRecognized called: class trans: "
				+ trans.getClass().getName());
		dragSource
				.startDrag(dge, DragSource.DefaultCopyNoDrop, trans, this /* DragSourceListener */);
	}

	public void autoscroll(Point cursorLocn) {
		// make point to view port co-ordinates
		cursorLocn.setLocation(cursorLocn.x, cursorLocn.y + this.getHeight());
		index.controller.autoscroll(cursorLocn);
	}

	public Insets getAutoscrollInsets() {

		Insets insets = new Insets(0, 0, 0, 0);
		Insets ins = index.controller.getAutoscrollInsets();
		JPanel client = index.controller.getClient();
		// top
		int top = ins.top;
		Rectangle b = getBounds();
		if (top - b.y > 0) {
			insets.top = top - b.y;
		}
		// bottom
		int bottom = client.getHeight() - ins.bottom;
		int diff = b.y + b.height - bottom;
		if (diff > 0) {
			insets.bottom = diff;
		}

		return insets;
	}

}
