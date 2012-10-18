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

 

 
 
 

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Set;
import javax.swing.*;
import javax.swing.tree.*;

 
import pm.dragndrop.PM_TreeWindowDragAndDrop;
import pm.gui.*;
import pm.utilities.*;


@SuppressWarnings("serial")
public class PM_TreeWindowAlbum  extends PM_TreeWindowDragAndDrop implements PM_Interface {
	
	// wenn hier auf true, dann aus Model die zu markierende Map holen und markieren
	boolean markSelectedSequence = false;
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_TreeWindowAlbum(PM_WindowBase windowBase, MouseListener mouseListener) {	
		super(windowBase,  PM_TreeModelAlbum.getInstance(), mouseListener);       
		 
	}
 
	
	// =====================================================
	// setMarkSelectedSequence()
	//
	// Wenn true wird aus dem openSequenceMap Dictionary
	// die entsprechenden Knoten markiert.
	//
	// (Wenn in X- oder B-Baum eine Sequenz markiert ist, soll
	//  die zugeh�rigen offenen Sequenzen im Album-Baum markiert 
	//  werden)
	// ==================================================== 
	public void setMarkSelectedSequence(boolean markSelectedSequence) {	
		this.markSelectedSequence = markSelectedSequence;
	}
	public boolean getMarkSelectedSequence( ) {	
		return markSelectedSequence;
	}
	
	// =====================================================
	// canDrag()
	// ==================================================== 
	@Override
	protected boolean canDrag(TreeNode dragNode) {	
		return true;
	}
	
	/**
	 *  Test if pictures can drop into the album.
	 *  
	 *  One or more pictures shall drop into the album.
	 *  (This is a drag and drop from the index View).
	 *  
	 *  If node has a user sequence object, only 
	 *  PM_SequenceExtended is allowed.
	 *  	 
	 */
	@Override
	protected boolean canDrop(DefaultMutableTreeNode node, Object compontentToDrop) {
//		if (node.isRoot()) {
//			return false;
//		}
		Object userObject = node.getUserObject();
		if (userObject instanceof PM_Sequence) {
			PM_Sequence sequ = (PM_Sequence)userObject;
			
	//		Sind offenbar nur album instancen ??????
			if (sequ instanceof PM_SequenceExtended) {
				return true;
			} else {
				return false;
			}		
		}
		
		return true;
	}
	
	
	// ======================================================
	// renderCell()
	//
	// Makieren, wenn eine Sequenz im Baum X oder B selektiert wurde.
	// ======================================================
	@Override
	protected void renderCell(JTree tree, DefaultMutableTreeNode node, DefaultTreeCellRenderer renderer )  {
		if ( ! ((PM_TreeWindowAlbum)tree).getMarkSelectedSequence()) {
			return;
		}		
		
		PM_TreeModelAlbum tma = (PM_TreeModelAlbum)getModel();
		Set<DefaultMutableTreeNode> markNodes = tma.getMarkNodes();
		if (markNodes == null) {
			return;
		}
		if (markNodes.contains(node)) {
			renderer.setFont(new Font("Arial", Font.BOLD, 12));
			renderer.setForeground(Color.RED);				
		}
		return;
	}
	
	
	
	
	// ======================================================
	// editPopupMenu()
	//
	// wird �berschrieben
	// ======================================================
	@Override
	protected void editPopupMenu(JPopupMenu popup, DefaultMutableTreeNode node) {
		if (node.getParent() == null) {
			// root
			popup.add(menuItemNewNode);
			return;
		}	
		popup.add(menuItemDelete);
		popup.add(menuItemChange);
			
		if (node.getUserObject() instanceof PM_Sequence) {
			return;
		}
		popup.add(menuItemNewNode);
		 
	}
	 
	// ======================================================
	// doMenuItemDelete()
	// ======================================================
	@Override
	protected void doMenuItemDelete(DefaultMutableTreeNode node) {	 

			int n = JOptionPane.showConfirmDialog(this, "Eintrag entfernen?",
					"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return;
			}
		 
		// aus Baum entfernen
		treeModel.removeNodeFromParent(node);
		treeModel.nodeChanged(treeModel.getRootNode());

	}

	

}
