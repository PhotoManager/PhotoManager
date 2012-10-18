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
import pm.inout.*;
import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Alle Drag & Drop - Funktionen f�r einen TreeWindow
 * 
 * Achtung: bei drag&drop funktioniert unter WINDOWs nur
 * DnDConstants.ACTION_MOVE !!!!
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_TreeWindowDragAndDrop extends PM_TreeWindow implements
		PM_Interface, DragSourceListener, DropTargetListener,
		DragGestureListener {

	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] supportedFlavors = { localObjectFlavor };

	private DragSource dragSource;
	private TreeNode dropTargetNode;

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_TreeWindowDragAndDrop(PM_WindowBase windowBase,
			PM_TreeModel rootNode, MouseListener mouseListener) {
		super(windowBase, rootNode, mouseListener);

		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_MOVE, this);
		new DropTarget(this, this);
	}

	// =====================================================
	// canDrag()
	// =====================================================
	protected boolean canDrag(TreeNode dragNode) {
		// wird �berladen
		return false;
	}

	// =====================================================
	// DragGestureListener
	//
	// Es wurde �ber einem Knoten ein Drag-Anfang erkannt.
	// Hier pr�fen, ob Drag zul�ssig.
	//
	// Nein: return
	// Ja: dragSource.startDrag(...) aufrufen und return.
	// =====================================================
	private void _dragGestureRecognized(DragGestureEvent dge) {

		// find object at this x,y
		Point clickPoint = dge.getDragOrigin();
		TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
		if (path == null) {
			return; // not a Node
		}
		DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();

		// Pr�fen ob drag zul�ssig
		if (sourceNode.getParent() == null)
			return; // root nicht zul�ssig
		if (!canDrag(sourceNode))
			return;

		// Tree und Knoten in eine Liste verpacken
		List<Object> list = new ArrayList<Object>();
		list.add(this);
		list.add(sourceNode);

		// nun gehts los
		Transferable trans = new PM_Transferable(list);
		dragSource.startDrag(dge, DragSource.DefaultMoveDrop, trans, this);
	}

	/**
	 * test if can drop a source component on the selected node.
	 * 
	 */
	protected boolean canDrop(DefaultMutableTreeNode targetNode,
			Object compontentToDrop) {
		// overrides
		return false;
	}

	
	/**
	 * Get the transfer data.
	 * 
	 * @return null - cannot get the data.
	 */
	private Object getTransferData(Transferable trans) {
		if (!trans.isDataFlavorSupported(localObjectFlavor)) {
			return null;
		}
		Object transData;
		try {
			transData = trans.getTransferData(localObjectFlavor);
		} catch (UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		
		return transData;
	}
	
	// ============================================================
	// dragOver()
	//
	// Der Cursor ist �ber ein Drop Target.
	// Hier entscheiden, ob der Drop acceptiert werden soll.
	// --> acceptDrag() oder rejectDrag()
	// ============================================================
	private void _dragOver(DropTargetDragEvent evt) {

		Transferable trans = evt.getTransferable();
		Object transData = getTransferData(trans);
		if (transData == null) {
			return;
		}

		// get the target node.
		Point dropPoint = evt.getLocation();
		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		if (path == null) {
			evt.rejectDrag();
			return;
		}
		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();

		// Sources are thumb nails (from a PM_Index)
		if (transData instanceof PM_PictureTransferable) {
			if (canDrop(targetNode, transData)) {
				evt.acceptDrag(DnDConstants.ACTION_MOVE);
			} else {
				evt.rejectDrag();
			}
			return;
		}

		// continue old
		DragAndDropType dndType = PM_Transferable.getDragAndDropType(trans);

		// ----------------------------------------
		// 
		if (dndType == DragAndDropType.NEW_SEQUENCE_B_X
				|| (dndType == DragAndDropType.NEW_SEQUENCE_ALBUM && this instanceof PM_TreeWindowAlbum)) {

			evt.acceptDrag(DnDConstants.ACTION_MOVE);
			return;
		}

		// -----------------------------------------------------
		// get: sourceNode, targetNode und sourceTree
		// -----------------------------------------------------

		DefaultMutableTreeNode sourceNode = getSourceNode(trans);
		JTree sourceTree = getSourceTree(trans);

		if (sourceNode == null || sourceTree == null) {
			evt.rejectDrag();
			return;
		}

		// ---------------------------------------------------------
		// Move innerhalb eines Baumes
		// ---------------------------------------------------------
		if ((sourceTree instanceof PM_TreeWindowAlbum && this == sourceTree)
				|| (sourceTree instanceof PM_TreeWindowBase && this == sourceTree)
				|| (sourceTree instanceof PM_TreeWindowExtended && this == sourceTree))

		{

			if (targetNode == sourceNode) {
				evt.rejectDrag();
				return; // unzu�assig, da derselbe Knoten
			}
			if (sourceNode.getParent() == null
					|| targetNode.getParent() == null) {
				evt.rejectDrag();
				return; // root node unzul�ssig
			}

			// dragNode und dropNode versorgt
			if (!(dragOverMoveSameTree(sourceNode, targetNode))) {
				evt.rejectDrag();
				return;
			}
			evt.acceptDrag(DnDConstants.ACTION_MOVE);
			return;
		}
		 
		
		// ---------------------------------------------------------
		// source ist PM_TreeWindowX/B und target ist PM_TreeWindowAlbum
		// ---------------------------------------------------------
		if ((sourceTree instanceof PM_TreeWindowExtended || sourceTree instanceof PM_TreeWindowBase)
				&& this instanceof PM_TreeWindowAlbum) {
			// PM_TreeWindowAlbum
			// dragNode und dropNode versorgt
			// System.out.println("drag-over source base, target album");
			evt.acceptDrag(DnDConstants.ACTION_MOVE);
			return;
		}

		evt.rejectDrag();

	}

	// ================================================================
	// drop()
	//
	// hier wurde die Maustaste losgelassen und es kann der
	// eigentliche Drop (Import) durchgef�hrt werden.
	//
	// Wird nur aufgerufen, wenn in dragOver() accept() aufgerufen
	// wurde.
	// ================================================================
	private void _drop(DropTargetDropEvent evt) {
		PM_SequencesInout.getInstance().setChanged(true);
		 
		
		Transferable trans = evt.getTransferable();
		Object transData = getTransferData(trans);
		if (transData == null) {
			return;
		}

		// get the target node.
		Point dropPoint = evt.getLocation();
		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		if (path == null) {
			return;
		}
		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		
		// drop if source are thumbnails
		if (transData instanceof PM_PictureTransferable) {
			 
			return;
		}
		
		
		
		
		
		
		DragAndDropType dndType = PM_Transferable.getDragAndDropType(trans);
		// -------------------------------------------------------
		// DragAndDropType.NEW_SEQUENCE
		//
		// Eine neue Sequence anlegen und in den Baum einh�ngen
		// Source is a Button !!!!
		// --------------------------------------------------------
		if (dndType == DragAndDropType.NEW_SEQUENCE_B_X) {
			dropNewSequence(trans, evt);
		}
		if (dndType == DragAndDropType.NEW_SEQUENCE_ALBUM) {
			dropNewAlbum(trans, evt);
		}
		// -----------------------------------------------------
		// get: sourceNode, targetNode und sourceTree
		// -----------------------------------------------------

		DefaultMutableTreeNode sourceNode = getSourceNode(trans);
		JTree sourceTree = getSourceTree(trans);
		 
		TreePath tp = getPathForLocation(dropPoint.x, dropPoint.y);
		if (tp == null || sourceNode == null || sourceTree == null) {
			evt.dropComplete(false);
			return;
		}
		 

	 

		// ---------------------------------------------------------
		// Move innerhalb eines Baumes
		// ---------------------------------------------------------
		if ((sourceTree instanceof PM_TreeWindowAlbum && this == sourceTree)
				|| (sourceTree instanceof PM_TreeWindowBase && this == sourceTree)
				|| (sourceTree instanceof PM_TreeWindowExtended && this == sourceTree))

		{

			if (sourceNode.getParent() == null
					|| targetNode.getParent() == null) {
				evt.dropComplete(false);
				return;
			}

			if (moveInsideTree(sourceNode, targetNode )) {
				evt.dropComplete(true);
				return;
			}
			evt.dropComplete(false);
			return;
		}

		// ---------------------------------------------------------
		// source ist PM_TreeWindowX/B und target ist PM_TreeWindowAlbum
		// ---------------------------------------------------------
		if ((sourceTree instanceof PM_TreeWindowExtended || sourceTree instanceof PM_TreeWindowBase)
				&& this instanceof PM_TreeWindowAlbum) {

			copyBaseOrExtendedToAlbum(sourceNode, targetNode, evt);

			return;
		}

		// ------------------------------------------------------------
		// drop unzul�ssig
		// ------------------------------------------------------------

		evt.dropComplete(false);

	}

	// ================================================================
	// dropNewSequence()
	//
	// Drag von PM_WindowSequence.
	//
	// Hier wird eine neue Sequenz angelegt.
	// ================================================================
	private void dropNewSequence(Transferable trans, DropTargetDropEvent evt) {

		
	/*	
		
		Object object = null;
		try {
			object = trans.getTransferData(localObjectFlavor);
		} catch (UnsupportedFlavorException e) {
	System.out.println("getTransferData. UnsupportedFlavorException: " + e);		 
		} catch (IOException e) {
	System.out.println("getTransferData. IOException: " + e);		 
		}

		if (object != null) {
			System.out.println("  drop new sequence: object = " + object );
		}
		
	*/	
		
		
		Point dropPoint = evt.getLocation();
		TreePath tp = getPathForLocation(dropPoint.x, dropPoint.y);
		if (tp == null) {
			evt.dropComplete(false);
			return;
		}
		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) tp
				.getLastPathComponent();
		// Hier neue Sequenz anlegen
		PM_Sequence seq = PM_Transferable.getSequence(trans);
		if (seq == null) {
			evt.dropComplete(false);
			return;
		}
		 
		if (seq instanceof PM_SequenceBase) {
			// check for pictures not allready in another base sequence
			PM_SequenceBase seqBase = (PM_SequenceBase) seq;
			List<PM_Picture> list = seqBase.getPictureList();
			Iterator<PM_Picture> it = list.iterator();
			while (it.hasNext()) {
				PM_Picture pic = it.next();
				if (!(pic.meta.hasBaseSequence())) {
					continue;
				}

				String text = PM_MSG.getMsg("winTreeNotUnique");
				int n = JOptionPane.showConfirmDialog(null, text,
						"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (n == JOptionPane.NO_OPTION) {
					evt.dropComplete(false);
					return;
				}
				break;
			}

		}

		// Name eingeben
		String mesage = PM_MSG.getMsg("winTreeNewShortName");
		// "Gib einen neuen Kurznamen ein.";

		String name = SEQU_NAME_UNKNOWN;
		Object obj = JOptionPane.showInputDialog(mesage, "neu");
		if (obj == null) {
			evt.dropComplete(false);
			return;
		}
		if (!(obj instanceof String)) {
			name = SEQU_NAME_UNKNOWN;
		} else {
			name = (String) obj;
			name = (String) name.trim();
		}

		// jetzt kann sie angelegt werden
		if (!seq.makeSequence()) {
			evt.dropComplete(false);
			return;
		}

		seq.setLongName(name);

		// insert the new sequence into the tree
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(seq);
		seq.setPath(PM_TreeModel.getPathFromNode(newNode));
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) targetNode
				.getParent();
		if (parent != null && targetNode.isLeaf()
				&& targetNode.getUserObject() instanceof PM_Sequence) {
			int index = parent.getIndex(targetNode);
			treeModel.insertNodeInto(newNode, parent, index);
		} else {
			treeModel.insertNodeInto(newNode, targetNode, 0);
			// expand wenn target node keine Sequenz ist
			TreePath t = getTreePath(targetNode);
			if (isCollapsed(t)) {
				expandPath(t);
			}
		}
		treeModel.nodeChanged(treeModel.getRootNode());

		// complete
		evt.dropComplete(true);
		return;

	}

	// ================================================================
	// dropNewAlbum()
	//
	// Drag von PM_WindowSuchen.
	//
	// Hier wird eine neue Album-Sequenz angelegt.
	// ================================================================
	private void dropNewAlbum(Transferable trans, DropTargetDropEvent evt) {

		Point dropPoint = evt.getLocation();
		TreePath tp = getPathForLocation(dropPoint.x, dropPoint.y);
		if (tp == null) {
			evt.dropComplete(false);
			return;
		}
		DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) tp
				.getLastPathComponent();
		// Hier neue Sequenz anlegen
		PM_Sequence seq = PM_Transferable.getSequence(trans);

		if (seq == null) {
			evt.dropComplete(false);
			return;
		}

		// Name eingeben
		String mesage = PM_MSG.getMsg("winTreeNewShortName");
		// "Gib einen neuen Kurznamen ein.dih";

		String name = SEQU_NAME_UNKNOWN;
		Object obj = JOptionPane.showInputDialog(mesage, "neu");
		if (obj == null) {
			evt.dropComplete(false);
			return;
		}
		if (!(obj instanceof String)) {
			name = SEQU_NAME_UNKNOWN;
		} else {
			name = (String) obj;
			name = (String) name.trim();
		}
		seq.setLongName(name);

		// in den Baum einh�ngen
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(seq);
		seq.setPath(PM_TreeModel.getPathFromNode(newNode));
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) targetNode
				.getParent();
		if (parent != null && targetNode.isLeaf()
				&& targetNode.getUserObject() instanceof PM_Sequence) {
			int index = parent.getIndex(targetNode);
			treeModel.insertNodeInto(newNode, parent, index);
		} else {
			treeModel.insertNodeInto(newNode, targetNode, 0);
			// expand wenn target node keine Sequenz ist
			TreePath t = getTreePath(targetNode);
			if (isCollapsed(t)) {
				expandPath(t);
			}
		}
		treeModel.nodeChanged(treeModel.getRootNode());

		// alles O.K.
		evt.dropComplete(true);
		return;

	}

	 
	private boolean moveInsideTree(DefaultMutableTreeNode sourceNode,
			DefaultMutableTreeNode targetNode ) {
		// System.out.println("<<<<<<<Move innerhalb eines Baumes   >>>>>>>>>>>");
		if (sourceNode.getUserObject() instanceof PM_Sequence) {
			// neuen Path in Sequence setzen.
			// Muss vor insert erfolgen, da sonst kein update (z.B. ComboBox)
			PM_Sequence seq = (PM_Sequence) sourceNode.getUserObject();
			String path = PM_TreeModel
					.getPathFromNode((DefaultMutableTreeNode) targetNode
							.getParent());
			if (path.length() == 0) {
				seq.setPath(seq.getLongName());
			} else {
				seq.setPath(path + "." + seq.getLongName());
			}
		}

		
		
/*		
		int iSource = sourceNode.getParent().getIndex(sourceNode);
		// System.out.println(" index source node (vor remove)  = " + iSource);
		treeModel.removeNodeFromParent(sourceNode);
 
		MutableTreeNode parent;
		int indexTarget;
		if (targetNode.isLeaf() && targetNode.getUserObject() instanceof String) {
			parent = targetNode;
			indexTarget = 0;
		} else {

			parent = (MutableTreeNode) targetNode.getParent();
			indexTarget = targetNode.getParent().getIndex(targetNode);

			if (iSource <= indexTarget) {
				indexTarget++;
			}
			// System.out.println(" index target node (vor insert)  = " +
			// indexTarget);

		}
		treeModel.insertNodeInto(sourceNode, parent, indexTarget);
*/
		
		 
		MutableTreeNode parent = (MutableTreeNode) targetNode.getParent();
		int indexTarget = targetNode.getParent().getIndex(targetNode);
		if (targetNode.isLeaf() && targetNode.getUserObject() instanceof String) {
			parent = targetNode;
			indexTarget = 0;
		}  
		treeModel.removeNodeFromParent(sourceNode);
		treeModel.insertNodeInto(sourceNode, parent, indexTarget);
		
		
		
		
		
		
		
		
		
		
		
		// Neuen Knoten selektieren
		setSelectionPath(getTreePath(sourceNode));

		// expandPath(getTreePath(targetNode));

		// 

		return true;

	}

	 
	private void copyBaseOrExtendedToAlbum(DefaultMutableTreeNode sourceNode,
			DefaultMutableTreeNode targetNode, DropTargetDropEvent evt) {
		Object o = sourceNode.getUserObject();
		if (o instanceof PM_SequenceExtended || o instanceof PM_SequenceBase) {
			PM_Sequence seqClosed = (PM_Sequence) o;
			String name = PM_TreeModel.getPathFromNode(sourceNode);
			name = name.replaceAll("\\.", " ");
			String shortName = SEQ_CHARACTER_ALBUM + PM_Sequence.getNextFreeAlbumSequenceNumber();
			PM_SequenceAlbum sequenz = new PM_SequenceAlbum(name, shortName);
			sequenz.setSeqClosed(seqClosed);
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
					sequenz);

			// ------------------------------------------------
			// target ist Leaf und keine Sequenz
			// -
			// -------------------------------------------------
			if (targetNode.isLeaf()
					&& !(targetNode.getUserObject() instanceof PM_Sequence)) {

				treeModel.insertNodeInto(newChild, targetNode, 0);
				// expand node (der keine Sequenz ist)
				expandPath(getTreePath(targetNode));
				// neuer Child selektieren
				setSelectionPath(getTreePath(newChild));

				// fertig
				evt.dropComplete(false);
				return;
			}

			// ------------------------------------------
			// -----------------------------------------
			int index = 0;

			if (targetNode.isLeaf()) {
				TreeNode parent = targetNode.getParent();
				index = parent.getIndex(targetNode);
				targetNode = (DefaultMutableTreeNode) targetNode.getParent();
			} else {
				index = targetNode.getChildCount();
			}

			treeModel.insertNodeInto(newChild, targetNode, index);

			// New Child selektieren
			setSelectionPath(getTreePath(newChild));

			evt.dropComplete(true);
			return;
		}

		evt.dropComplete(false);
		return;
	}

	// =====================================================
	// canMoveInTree()
	//
	// target darf NICHT unterhalb der source sein
	// =====================================================
	private boolean dragOverMoveSameTree(DefaultMutableTreeNode sourceNode,
			DefaultMutableTreeNode targetNode) {

		// ----------------------------------------------------
		// pr�fen, ob target unterhalb des Source-Knotens
		// (das is unzul�ssig)
		// -----------------------------------------------------
		TreeNode parent = targetNode.getParent();
		while (parent != null) {
			if (parent == sourceNode)
				return false;
			parent = parent.getParent();
		}

		return true;
	}

	// ================================================================
	// getSourceNode()
	//
	// return null: nigefu
	// ================================================================
	private DefaultMutableTreeNode getSourceNode(Transferable trans) {
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
			Object o = l.get(1);
			if (o instanceof TreeNode) {
				return (DefaultMutableTreeNode) o;
			}
		}
		return null;
	}

	// ================================================================
	// getSourceTree()
	//
	// return null: nigefu
	// ================================================================
	private JTree getSourceTree(Transferable trans) {
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
			Object o = l.get(0);
			if (o instanceof JTree) {
				return (JTree) o;
			}
		}
		return null;
	}

	// ==========================================================
	// ==========================================================
	//
	// DragGesture - Listener events
	// DragSource - Listener events
	// DropTarget - Listener events
	//
	// ==========================================================
	// ==========================================================

	// =====================================================
	// DragGesture - Listener events
	// =====================================================

	public void dragGestureRecognized(DragGestureEvent dge) {
		_dragGestureRecognized(dge);
	}

	// =====================================================
	// DragSource - Listener events
	// =====================================================

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public void dragEnter(DragSourceDragEvent evt) {
		DragSourceContext dsx = evt.getDragSourceContext();
		dsx.setCursor(DragSource.DefaultMoveDrop);
	}

	public void dragExit(DragSourceEvent evt) {
		DragSourceContext dsx = evt.getDragSourceContext();
		dsx.setCursor(DragSource.DefaultMoveNoDrop);
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent arg0) {
	}

	// =====================================================
	// DropTarget - Listener events
	// =====================================================

	public void dragEnter(DropTargetDragEvent evt) {
		evt.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	public void dragExit(DropTargetEvent arg0) {
	}

	public void dragOver(DropTargetDragEvent evt) {
		_dragOver(evt);
	}

	public void drop(DropTargetDropEvent evt) {
		_drop(evt);
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
	}

	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================
	// ================== Inner Classes ================================

	// =====================================================
	// Inner Class: DnDTreeCellRenderer
	//
	// =====================================================
	class DnDTreeCellRenderer extends DefaultTreeCellRenderer {

		private boolean isTargetNode;
		private boolean isTargetNodeLeaf;

		public DnDTreeCellRenderer() {
			super();
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean isExpanded, boolean isLeaf,
				int row, boolean hasFocus) {
			isTargetNode = (value == dropTargetNode);
			isTargetNodeLeaf = (isTargetNode && ((TreeNode) value).isLeaf());

			return super.getTreeCellRendererComponent(tree, value, isSelected,
					isExpanded, isLeaf, row, hasFocus);

		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isTargetNode) {
				g.setColor(Color.black);
				if (isTargetNodeLeaf) {
					g.drawLine(0, 0, getSize().width, 0);
				} else {
					g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
				}
			}
		}

	}

	public void autoscroll(Point p) {
		// Figure out which row you are on
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();

		// Now decide if the row is at the top of the screen or at the bottom.
		// Do this
		// so that the previous row (or the next row) ist visible. If you're at
		// the
		// absolute top or bottom, just return the first or last row,
		// respectively.
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);

	}

	private int margin = 12;

	public Insets getAutoscrollInsets() {

		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		Insets insets = new Insets(inner.y - outer.y + margin, inner.x
				- outer.x + margin, outer.height - inner.height - inner.y
				+ outer.y + margin, outer.width - inner.width - inner.x
				+ outer.x + margin);
		return insets;
	}

}
