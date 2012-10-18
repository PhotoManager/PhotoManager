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

 

import java.awt.Font;

import javax.swing.*;
 
import javax.swing.tree.*;


import pm.dragndrop.PM_TreeWindowDragAndDrop;
import pm.gui.*;
import pm.utilities.*;


@SuppressWarnings("serial")
public class PM_TreeWindowExtended extends PM_TreeWindowDragAndDrop implements PM_Interface {
	
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_TreeWindowExtended(PM_WindowBase windowBase) {	
		super(windowBase,  PM_TreeModelExtended.getInstance(), null);       
	}
	
	// =====================================================
	// canDrag()
	// =====================================================
	@Override
	protected boolean canDrag(TreeNode dragNode) {	
		return true; //dragNode.isLeaf(); 
	}
	
	
 
	
	 
	/**
	 * mark sequences not in album
	 */
	@Override
	protected void renderCell(JTree tree, DefaultMutableTreeNode node, DefaultTreeCellRenderer renderer )  {
		Object o = node.getUserObject();
		if (o instanceof PM_SequenceExtended) {
		      if (!PM_SequenceExtended.getSequenceInAlbum().contains((PM_SequenceExtended)o)) {
		    	  renderer.setFont(new Font("Arial", Font.BOLD, 12)); 
		      }
		}
	}
	
	
	// ======================================================
	// doMenuItemDelete()
	//   Achtung: PM_TreeWindowExtended und PM_TreeWindowBase identisch !!!!
	// ======================================================
	@Override
	protected void doMenuItemDelete(DefaultMutableTreeNode node) {	 

		if (node.getUserObject() instanceof PM_Sequence) {
			// Sequenz l�schen hier nicht zugelassen
			return;
		}  	 
			
		int n = JOptionPane.showConfirmDialog(this, "Eintrag l�schen?",
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}

		
		// aus Baum entfernen
		treeModel.removeNodeFromParent(node);
		treeModel.nodeChanged(treeModel.getRootNode());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ======================================================
	// editPopupMenu()
	//
	//   Achtung: PM_TreeWindowExtended und PM_TreeWindowBase identisch !!!!
	// ======================================================
	@Override
	protected void editPopupMenu(JPopupMenu popup, DefaultMutableTreeNode node) {
		
		if (node.getParent() == null) {
			// Root Node
			popup.add(menuItemNewNode);
			return;
		}	
		
		if ( (node.getUserObject() instanceof PM_Sequence) && node.isLeaf()) {
			// Eine Sequence
			popup.add(menuItemChange);
			return;
		}
		
		if (node.isLeaf()) {
			// Knoten und keine Kinder
			popup.add(menuItemChange);
			popup.add(menuItemDelete);
			popup.add(menuItemNewNode);
			return;
		}
		
		// Alles andere
		popup.add(menuItemChange);
		popup.add(menuItemNewNode);		
		 
	}
	 
	
	
}
