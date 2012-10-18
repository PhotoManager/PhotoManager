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
import javax.swing.tree.*;
 
import pm.inout.*;
import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;
 

public class PM_SubWindowSeqBase extends PM_SubWindowSequence {
	
	private JSplitPane splitPane;
	
	public PM_SubWindowSeqBase(PM_WindowMain windowMain, PM_WindowBase windowBase) {
		super(windowMain, windowBase);
	 
		treeWindow = new PM_TreeWindowBase(windowBase);
		treeWindow.addTreeSelectionListener(treeSelectionListener);
		PM_TreeModelBase.getInstance().addChangeListener(treeChangeListener);
				
		setSequenceName();	
	}
	
	
	/**
	 * Remove the displayed sequence.
	 */
	@Override
	 protected boolean removeSequenceDisplayed() {
		if (super.removeSequenceDisplayed() == false) {
			return false;
		}
		setSequenceName();	
	    return true;
	 }
	
	/**
	 * Tab changed.
	 * 
	 */
	protected void tabChanged(JLabel upperLabel) {
		upperLabel.setText(PM_MSG.getMsg("winSeqHeaderB"));  
	}
	
	/**
	 * Display the sequence name.
	 */
	protected void setSequenceName() {
		super.setSequenceName();
		if (sequenceDisplayed == null) {
			sequenceName.setText(PM_MSG.getMsg("winSeqNoDisplayesB"));	
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
	 * Check if the picture is in another base sequence. 
	 * 
	 * Sequence is displayed. Check if the arguments picture is
	 * in another Base sequence than the displayed Sequence.
	 * 
	 * @return true - is in another base sequence
	 *         false - it can be modify 
	 * 
	 */
	@Override
	public boolean isPictureInAnotherBaseSequence(PM_Picture picture) {
		 
		// a new sequence are created	
		if (sequenceDisplayed == null) {
			if (picture.meta.hasBaseSequence()) {
				return true;
			} else {
				return false;
			}
		}
		// modify a sequence
		
		if (!picture.meta.hasBaseSequence()) {
			return false; // in NOT in a base sequence
		}
		
		String sNumber = picture.meta.getBaseSequenceName();
		if (sequenceDisplayed.getShortName().equals(sNumber)) {
			return false; // same base sequence number
		}
		return true; 
	}
	
	
	
 
	
	/**
	 *  Returns a new sequence with the arguments pictures.
	 */
	protected PM_Sequence getNewSequence(List<PM_Picture> pictures) {
		return new PM_SequenceBase(SEQU_NAME_UNKNOWN,pictures);
	}
	
	/**
	 * Remove node from tree.
	 * 
	 * A sequence was deleted. Now remove the node from the tree.
	 */
	@Override
	protected void removeTreeNode(DefaultMutableTreeNode selectedNode, PM_Sequence sequence) {
		// now remove the node
		PM_TreeModel treeModel = PM_TreeModelBase.getInstance();
		treeModel.removeNodeFromParent(selectedNode);
		treeModel.nodeChanged(treeModel.getRootNode());
	}
	
	
	
	/**
	 * Can modify the sequence.
	 *
	 * Check if the displayed pictures contains pictures of
	 * another base sequence.
	 */
	@Override
	protected boolean canModify(List<PM_Picture> picList) {
		int anzRot = 0;
		for (PM_Picture p: picList) {		 
			if (isPictureInAnotherBaseSequence(p)) {
				anzRot++;
			}
		}

		if (anzRot > 0) {		
			String text = String.format(PM_MSG.getMsg("winSeqPicsPresent"), anzRot);		
			int n = JOptionPane.showConfirmDialog(null, text,
					"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
		
/*
		else {
			// -------------------------------------------------------------
			// Sicherheitsabfrage f�rs �ndern
			// -------------------------------------------------------------

//			String text = "Serie '" + getSequenceDargestellt().getShortName()
//					+ "' jetzt �ndern?";
			String text = String.format(PM_MSG.getMsg("winSeqCanModify"), sequenceDisplayed.getShortName() );
						
			
			int n = JOptionPane.showConfirmDialog(null, text,
					" ", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION)
				return false;
		*/		
		
		 
		
		
	}
}
