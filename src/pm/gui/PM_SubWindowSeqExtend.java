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

import java.util.List;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
 

 
import pm.inout.*;
import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;

public class PM_SubWindowSeqExtend extends PM_SubWindowSequence{
 
	private JSplitPane splitPane;
	
	
	/**
	 * constructor
	 */
	public PM_SubWindowSeqExtend(PM_WindowMain windowMain, PM_WindowBase windowBase) {
		super(windowMain, windowBase);
		 
		treeWindow = new PM_TreeWindowExtended(windowBase);
		treeWindow.addTreeSelectionListener(treeSelectionListener);
		PM_TreeModelExtended.getInstance().addChangeListener(treeChangeListener);	 
		setSequenceName();
	}
	
 
	/**
	 * Tab changed.
	 * 
	 */
	protected void tabChanged(JLabel upperLabel) {
		upperLabel.setText(PM_MSG.getMsg("winSeqHeaderX"));	
	}
	
	/**
	 * Remove the displayed sequence.
	 */
	@Override
	 protected boolean removeSequenceDisplayed() {
		if (super.removeSequenceDisplayed() == false) {
			return false;
		}
		sequenceName.setText(PM_MSG.getMsg("winSeqNoDisplayesX"));		 
		return true;
	 }
	
 

	/**
	 * Display the sequence name.
	 */
	@Override
	protected void setSequenceName() {
		super.setSequenceName();
		if (sequenceDisplayed == null) {
			sequenceName.setText(PM_MSG.getMsg("winSeqNoDisplayesX"));	
		}
	} 
	
	
	/**
	 * Close the application
	 */
	@Override
	public void close() {	 	
 		PM_All_InitValues.getInstance().putValueInt(this, "devider",
 				splitPane.getDividerLocation());		
		 
	}
	
	/**
	 * Initialization: Returns the Component for the TabbedPane
	 */
	@Override
	protected  JComponent getTabComponent() {	
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					getBaseExtLeftPane(sequenceName), getBaseExtRightPane(treeWindow));
			int location = PM_All_InitValues.getInstance().getValueInt(this, "devider");
			if (location == 0) {
				location = 200;
			}
			splitPane.setDividerLocation(location);
			return splitPane;
	}
	
	
	/**
	 *  Returns a new sequence with the arguments pictures.
	 */
	@Override
	protected PM_Sequence getNewSequence(List<PM_Picture> pictures) {
		return new PM_SequenceExtended(SEQU_NAME_UNKNOWN,pictures);
	}
	
	/**
	 * Remove node from tree.
	 * 
	 * A sequence was deleted. Now remove the node from the tree.
	 */
	@Override
	protected void removeTreeNode(DefaultMutableTreeNode selectedNode, PM_Sequence sequence) {
		// now remove the node
		PM_TreeModel treeModel = PM_TreeModelExtended.getInstance();
		treeModel.removeNodeFromParent(selectedNode);
		treeModel.nodeChanged(treeModel.getRootNode());
	}
}
