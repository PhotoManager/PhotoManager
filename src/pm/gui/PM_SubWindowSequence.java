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

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 
import pm.dragndrop.PM_Transferable;
import pm.index.*;
import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;
 

public class PM_SubWindowSequence implements PM_Interface {

	final protected PM_Index index;
	protected PM_WindowBase windowBase;
	protected PM_WindowMain windowMain;
	
	protected PM_TreeWindow  treeWindow;
	protected TreeSelectionListener	treeSelectionListener;
	protected JLabel sequenceName  = new JLabel("Sequence Name");
	
	protected PM_Sequence sequenceDisplayed = null;
	protected DefaultMutableTreeNode selectedNode = null;
	protected boolean sequenceChanged = false;
	private PM_Sequence newCreatedSequence = null;
	protected PM_Listener treeChangeListener;
 
	protected JTextField hits;
	
	protected boolean canDrag = true;
	
	protected JButton newButton;
	protected JButton modButton;
	protected JButton delButton;
	
	/**
	 * constructor
	 */
	public PM_SubWindowSequence(PM_WindowMain windowMain, PM_WindowBase windowBase) {		
		this.windowMain = windowMain;
		this.windowBase = windowBase;
		index = PM_Index.createIndexRight( );
		index.init(windowBase);
		
		// Create the Selection listener if a sequence select on a tree
		treeSelectionListener = new TreeSelectionListener() {
			 
			public void valueChanged(TreeSelectionEvent e) {
				Object o = e.getSource();		
				if (!(o instanceof PM_TreeWindow))
					return;
				PM_TreeWindow tw = (PM_TreeWindow) o;
				if (tw.isSelectionEmpty()) {
					return;
				}
				selectSequence(e, tw);		 
			}
		};
		

		// Create a tree change listener
		treeChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				treeNodeChanged(e);
			}
		};
		
		// index data change listener
		index.data.addChangeListener(new PM_Listener() {
			 
			public void actionPerformed(PM_Action e) {
				if (sequenceDisplayed != null) {
					sequenceChanged = true;
				}  
				setButtons();
			}		
		});
		
		// Models change listerer
		index.controller.addOpenCloseListener(new PM_Listener() {
			 
			public void actionPerformed(PM_Action e) {
				if (sequenceDisplayed != null) {
					sequenceChanged = true;
				}  
				setButtons();
			}
			
		});
	}
		 
	/**
	 * A tree node have changed.
	 * 
	 * For example the name have changed.
	 */
	public void treeNodeChanged(PM_Action e) {
		setSequenceName();
	}
	
	
	
	/**
	 * Returns the PM_Index
	 */
	protected PM_Index getIndex() {
		return index;
	}
	
	
	/**
	 * Tab changed.
	 */
	protected void tabChanged(JLabel upperLabel) {
		// Override
	}
 
	/**
	 * A row was selected in a tree.
	 * 
	 * The sequence shall not be displayed.
	 * (The sequence displayed if double click or press left arrow). 
	 */
	private void selectSequence(TreeSelectionEvent e, PM_TreeWindow tw) {		
		setHits();
	}
	
	
	/**
	 * A double click on a tree node was detected.
	 * 
	 * Display the sequence.
	 */
	public void doubleClickOnTree(DefaultMutableTreeNode node, PM_TreeWindow tw) {	 	 
		displaySequence();
	}
	
	/**
	 * Set the hits into the JTextField.
	 */
	protected void setHits() {
		if (!treeWindow.isSequenceSelected()) {
			hits.setText(" ");
			return;
		}
		DefaultMutableTreeNode node = treeWindow.getSelectedNode();	
		Object s = node.getUserObject();
		if (s instanceof PM_Sequence) {
			PM_Sequence seq = (PM_Sequence)s;
			hits.setText(String.valueOf(seq.getAnzahlBilder()));		   
		} else {
			hits.setText(" ");	   
		}
	}
	 
	/**
	 * Display sequence selected from menu in pictureView
	 */
	public String displaySequence(PM_Sequence sequence) {
		if (sequenceDisplayed == sequence) {
			return "Serie bereits dargestellt";
		}
		 
		
		if (sequenceDisplayed != null &&
				sequenceChanged) {		 
				return "Eine andere Serie ist dargestellt und modifiziert";
		}
	 	
		
		index.data.clearAndAdd(sequence.getAlleBilder());
		sequenceDisplayed = sequence;
		sequenceChanged = false;
		
		setButtons();
		return "";
	}
	
	
	/**
	 * Display the selected sequence.
	 * 
	 * A sequence was selected in a sequence tree.
	 * Display the thumbs of this sequence in the index View.
	 */
	private void displaySequence() {
		
		if (!treeWindow.isSequenceSelected()) {
			return;
		}
		
		selectedNode = treeWindow.getSelectedNode();	
		PM_Sequence sequence =  (PM_Sequence)selectedNode.getUserObject();
		 	
		
		if (sequenceDisplayed != null &&
				sequenceChanged) {
			int n = JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("winSeqSeqOverwrite"),
					"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return;
			}
		}
	 	
		
		index.data.clearAndAdd(sequence.getAlleBilder());
		sequenceDisplayed = sequence;
		sequenceChanged = false;
		
		setButtons();
	 
	}
	
	/**
	 * Set the new, mod, del button, hits, and sequence name
	 */
	protected void setButtons() {
		setHits();
		setSequenceName();
		if (sequenceDisplayed != null) {
			// a sequence is displayed
			newButton.setEnabled(false);
			delButton.setEnabled(true);
			if (sequenceChanged) {				
				modButton.setEnabled(true);
			} else {			 
				modButton.setEnabled(false);
			}
		} else {
			// a sequence is NOT displayed
			delButton.setEnabled(false);
			modButton.setEnabled(false);
			if (index.controller.getPictureSize() == 0) {
				newButton.setEnabled(false);
			} else {
				newButton.setEnabled(true);
			}
		}
	}
	
 
	
	/**
	 * Remove the displayed sequence.
	 */
	protected boolean removeSequenceDisplayed() {
		if (hasSequenceChanged()) {
			int n = JOptionPane.showConfirmDialog(null, PM_MSG
					.getMsg("winSeqChangeSeq"), "", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		sequenceDisplayed = null;
		sequenceChanged = false;
		return true;
	}
	
	public boolean requestToClose() {
		return   true; 
	}
	
	
	/**
	 * Initialization: Return for Base and Extended the left side Split pane.
	 * 
	 * On top the name of the displayed sequence
	 * Then the index view: toolbar, panel for thumbs, zoom slider
	 */
	protected JComponent getBaseExtLeftPane(JLabel sequenceName) {
		
		// On top the name of the displayed sequence
		Font font = sequenceName.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, 20);
		sequenceName.setFont(fontBold);
		JScrollPane scUpperTreePanel = new JScrollPane(sequenceName);
		scUpperTreePanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scUpperTreePanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		 
		
		
		// The index view: toolbar, panel for thumbs, zoom slider
		// add some buttons to the toolbar
		JPanel toolPanel = index.getIndexToolbar(); // the default toolbar	
		 	
		// Create a new Sequence
		newButton = new JButton("New");
		newButton.setEnabled(false);
		toolPanel.add(newButton);
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("New Button pressed");
				int mod = e.getModifiers();			
				if ((mod & ActionEvent.CTRL_MASK | 
						mod	& ActionEvent.SHIFT_MASK  ) == 0) {
					// ctrl or shift pressed: new Sequence creates with new Button
					canDrag = false;
					newButton();
				}  
				// new sequence creates with drag & drop
				canDrag = true; 		
			}
		});
		new DragSourceNew(newButton); 
		// Modify a sequence
		modButton = new JButton("Mod");
		modButton.setEnabled(false);
		toolPanel.add(modButton);
		modButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				modifySequence();
			}
		});
		// Delete a sequence
		delButton = new JButton("Del");
		delButton.setEnabled(false);
		toolPanel.add(delButton);
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				delButton();
			}
		});
 						
		index.controller.setAllowGetFromRight(true);
		index.controller.setAllowCrossing(true);
		index.controller.setPopUpAendern(true);
		index.controller.setPopUpDiaShow(true);
		index.controller.setPopUpLoeschen(true);
		index.controller.setPopUpLoeschenAufheben(true);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(scUpperTreePanel, BorderLayout.NORTH);
		p.add(index.getIndexPanel(), BorderLayout.CENTER);

		return p;
	}
	
 
	
	/**
	 * Initialization: Return for Base and Extended the right Split pane.
	 * 
	 * On top: < and << arrow, hits, and slideshow button.
	 * beneath: The tree window
	 */
	protected JComponent getBaseExtRightPane(PM_TreeWindow treeWindow) {
		 
		JPanel onTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		 
		// arrow: <
		JButton left = PM_Utils.getJButon(ICON_1_LEFT);
		onTopPanel.add(left);
		ActionListener alBDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				displaySequence();
			}
		};
		left.addActionListener(alBDisplay);
 
		// Hits
		hits  = new JTextField("");
		hits.setForeground(Color.BLACK);
		hits.setEditable(false);
		hits.setFocusable(false);
		hits.setColumns(4);
		onTopPanel.add(hits );
		Font font = hits .getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hits.setFont(fontBold);

		// Button "slideshow"
		JButton slideShow = new JButton("Slideshow");
		onTopPanel.add(slideShow);
		ActionListener alBDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//////////				startDiashowFromTreeWindow(treeSerienB.getSelectedNode());
			}
		};
		slideShow.addActionListener(alBDia);
		
		// make it scrollable 
		JScrollPane scUpperTreePanel = new JScrollPane(onTopPanel);
		scUpperTreePanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scUpperTreePanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		
		// Now put all together  
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(scUpperTreePanel, BorderLayout.NORTH);
		p.add(treeWindow, BorderLayout.CENTER);
		
		
		
		return p;
	}
	
	
	/**
	 * Initialization: Returns the Component for the TabbedPane 
	 * 
	 */
	protected  JComponent getTabComponent() {
		return null; // Overrides
	}
	
	
	/**
	 * The new button was pressed to create a new sequence.
	 */
	private void newButton() {
		if (index.controller.getSizeValid() == 0) {
			return;
		}

		// Prompt a new sequence name
		String name = null;
		do {
			name = JOptionPane.showInputDialog("Gib neuen Seqenz Namen ein","neu");
			if (name == null) {
				break;
			}
			name = name.trim();
			if (name.indexOf('.') >= 0 
					|| name.indexOf('/') >= 0
					|| name.indexOf('\\') >= 0) {
				JOptionPane
				.showMessageDialog(null, ". / \\ unzulässig");
			} else {
				break;
			}			 
		} while (name != null);
		if (name == null || name.length() == 0) {
			return;
		}

		// the new picture list
		List<PM_Picture> pictures = getIndex().controller
				.getPictureListDisplayed();
		// check if pictures of another base sequence
		if (!canModify(pictures)) {
			return;
		}
		// make a new sequence 
		PM_Sequence sequ = getNewSequence(pictures);
		sequ.setLongName(name);
		sequ.makeSequence();
		// insert the sequence into the tree
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
				sequ);	
		PM_TreeModel treeModel = treeWindow.getTreeModel();		
		DefaultMutableTreeNode targetNode = treeWindow.getRootNode( );		
		treeModel.insertNodeInto(newChild, targetNode, 0);	
		treeWindow.setSelectionPath(treeWindow.getTreePath(newChild));
		// Removes the pictures on the left side?
		int n = JOptionPane.showConfirmDialog(null, String.format(PM_MSG.getMsg("winSeqNewEnd"), sequ.getShortName()), " ",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.YES_OPTION) {
			List<PM_Picture> pics = getIndex().controller.getPictureListDisplayed();
			windowMain.getIndexViewThumbnails().data.removePictureList(pics);	 
		}		
		// delete display
		index.data.removeAllPictures();
		sequenceDisplayed = null;
		sequenceChanged = false;
		 
		setButtons();
	}
	
	/**
	 * Display the sequence name.
	 */
	protected void setSequenceName() {
		if (sequenceDisplayed != null) {		
			String name = sequenceDisplayed.getShortName() + ":" + sequenceDisplayed.getLongName();		
			sequenceName.setText(name);	
		}
	}
	
	
	
	
	/**
	 * The delete button was pressed to delete a sequence.
	 */
	protected void delButton() {
		deleteSequence(sequenceDisplayed);
	}	
	
	
	 
	
	
	/**
	 * Close the application
	 */
	public void close() {
		// Overrides  
	}
	
	/**
	 * Check if the picture is in another base sequence. 
	 * 
	 * Sequence is displayed. Check if the arguments picture is
	 * in another Base sequence than the displayed Sequence.
	 */
	public boolean isPictureInAnotherBaseSequence(PM_Picture picture) {
		return false;  // Overrides
	}
	
   
	/**
	 * Create a new Sequence instance.
	 * 
	 * The sequence is not yet realized. It is the beginning
	 * of drag & drop. 
	 */
	private PM_Sequence createSequence() {
		if (sequenceDisplayed != null) {
			int n = JOptionPane.showConfirmDialog(null, PM_MSG
					.getMsg("winSeqCopy"), "", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return null;
			}
		}

		if (getIndex().controller.sizeDargestellt() == 0) {
			JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("winSeqNoPics"),
					" ", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return null;
		}
		List<PM_Picture> pictures = getIndex().controller
				.getPictureListDisplayed();
		return getNewSequence(pictures);
	}
	
	/**
	 *  Returns a new sequence with the arguments pictures.
	 */
	protected PM_Sequence getNewSequence(List<PM_Picture> pictures) {
		return null; // Overrides
	}
	
	 
	/**
	 * The drag and drop was done.
	 * 
	 * The new sequence and a new node in the tree was created.
	 */
	private void dragDropDone() { 
		
		if (newCreatedSequence == null) {
			return;
		}
		 	 
		// Removes the pictures on the left side?
		int n = JOptionPane.showConfirmDialog(null, String.format(PM_MSG.getMsg("winSeqNewEnd"), newCreatedSequence.getShortName()), " ",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.YES_OPTION) {
			List<PM_Picture> pictures = getIndex().controller.getPictureListDisplayed();
			windowMain.getIndexViewThumbnails().data.removePictureList(pictures);	 
		}
		
		
		// delete display
		index.data.removeAllPictures();
		sequenceDisplayed = null;
		sequenceChanged = false;
		 
	 
		
		setButtons();
	}
 
	

	 
	 
	
	

	/**
	 * Modify the displayed sequence.
	 */
	private void modifySequence() {
		// Message if no pictures left after modification
		if (getIndex().controller.sizeDargestellt() == 0) {		
			JOptionPane
					.showConfirmDialog(
							null,
							PM_MSG.getMsg("winSeqNoPicsMod"),
							" ", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Message if the sequence is not modified.
		if (!hasSequenceChanged()) {			
			JOptionPane
					.showConfirmDialog(
							null,							
							PM_MSG.getMsg("winSeqNoPicsModified"),
							"Keine �nderung", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
			return;
		}
		// check if pictures of another base sequence
		List<PM_Picture> picList = getIndex().controller.getPictureListDisplayed();
		if (!canModify(picList)) {
			return;
		}
		// now do the modification
		sequenceDisplayed.modifySequence(picList);
		sequenceChanged = false;
		JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("winSeqModified"),
				"O.K.", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		// set the new count into the tree
		PM_TreeModel treeModel = treeWindow.getTreeModel();
		treeModel.nodeChanged(treeModel.getRootNode());		
		// reread in case of closed pictures (delete them from the view)
		getIndex().controller.rereadAllThumbs();
		
		setButtons(); 		 
	}
	
	/**
	 * Delete the displayed sequence.
	 */
	private void deleteSequence(PM_Sequence sequence) {

		if (sequenceDisplayed == null) {
			return;
		}
		// Check if there is a node in the album tree.
		Map<PM_Sequence, Set<DefaultMutableTreeNode>>  openSequenceMap =
			PM_TreeModelAlbum.getInstance().getSequenceDictionary();
		if (openSequenceMap.containsKey(sequence)) {
			 
			JOptionPane.showConfirmDialog(null,
					PM_MSG.getMsg("winSeqDeleteAlbum"), "Message",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;		
		}
	    // security advice	
		int n = JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("winSeqDeleteSeq"),
				"delete ?", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		
		// delete the sequence
 		sequence.deleteSequence();
 		// confirmation message
		JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("winSeqDeleteEnd"),
				"gelöscht", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		// remove the node in the tree	
		removeTreeNode(selectedNode, sequence);
		sequenceDisplayed = null;
		sequenceChanged = false;
		
		index.data.removeAllPictures();
		setButtons(); 
	}
 
	
	/**
	 * Remove node from tree.
	 * 
	 * A sequence was deleted. Now remove the node from the tree.
	 */
	protected void removeTreeNode(DefaultMutableTreeNode selectedNode, PM_Sequence sequence) {
		// Overrides
	}
		 
	 
	
	
	/**
	 * Check if displayed sequence has changed.
	 *
	 */
	protected boolean hasSequenceChanged() {
		if (sequenceDisplayed == null) {
			return false; // no sequence displayed
		}
		if (getIndex().controller.getPictureClosedSize() == 0 &&
				sequenceChanged == false) {
			return false;  // displayed sequence not changed
		}		
		return true;  
	}
 	
	/**
	 * Can modify the sequence.
	 */
	protected boolean canModify(List<PM_Picture> picList) {
		return true; // Overrides
	}
	
 
	
	 
	/**
	 * Inner class for a new Sequence with Drag and Drop.
	 * 
	 * Only two instances (one for Base and one for Extended Sequences) 
	 * are created. They shall never been destroyed.
	 * 
	 */
	class DragSourceNew implements DragSourceListener, DragGestureListener {
		 
		private DragSource dragSource;
 
		
		/**
		 * constructor
		 *  
		 */
		public DragSourceNew(Component source) {		 
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(source, DnDConstants.ACTION_COPY_OR_MOVE , this);
		}
		
		// ==================================================================
		// methods for DragSourceListener interface
		// ==================================================================
		 
		public void dragDropEnd(DragSourceDropEvent dsde) {
	//		System.out.println("Window sequence D&D Button: dragDropEnd");	 				 
			if (dsde.getDropSuccess()) {
				// a new Sequence was created 
				dragDropDone();	
			}				 
		}
	 
		public void dragEnter(DragSourceDragEvent evt) {
// System.out.println("Window sequence D&D Button: dragEnter");
			DragSourceContext dsx = evt.getDragSourceContext();
			dsx.setCursor(DragSource.DefaultMoveDrop);
		}
	 
		public void dragExit(DragSourceEvent evt) {
			System.out.println("Window sequence D&D Button: dragExit");	 	
			DragSourceContext dsx = evt.getDragSourceContext();
			dsx.setCursor(DragSource.DefaultMoveNoDrop);
		}
	 
		public void dragOver(DragSourceDragEvent arg0) {
			System.out.println("Window sequence D&D Button: dragOver");	 	
		}
		 
		public void dropActionChanged(DragSourceDragEvent arg0) {
		}

		 
		/**
		 * Here a drag is recognized.
		 * 
		 * A method for the DragGestureListener interface.
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			  
			
			if ( !(newButton.isEnabled() && canDrag)) {
				return; // new Button in action
			}
			 
//			System.out.println("....... Window sequence D&D Button: dragGestureRecognized");	
			Component c = dge.getComponent();
			if ( !(c instanceof JButton)) {
				return;
			}
			JButton b = (JButton)c;
			if ( !(b.isEnabled())) {
				return;
			}
			newCreatedSequence =  createSequence();
			if (newCreatedSequence == null) {
				return;
			}
  
			// now the drag can start			
			List<Object> list = new ArrayList<Object>();
			list.add(DragAndDropType.NEW_SEQUENCE_B_X);
			list.add(newCreatedSequence);
					 
			Transferable transferable = new PM_Transferable(list);
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop  , transferable, this);			
			
		}				
		
	}
}
