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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

 
import javax.swing.tree.DefaultMutableTreeNode;

 
import pm.inout.*;
import pm.utilities.PM_Action;
import pm.utilities.PM_Interface;
import pm.utilities.PM_Listener;
import pm.utilities.PM_MSG;

 


@SuppressWarnings("serial")
public class PM_TreeModelAlbum extends PM_TreeModel  implements PM_Interface {	
	
	private Map<PM_Sequence, Set<DefaultMutableTreeNode>> openSequenceMap
			= new HashMap<PM_Sequence, Set<DefaultMutableTreeNode>>();	 
	private Set<DefaultMutableTreeNode> markNodes = new HashSet<DefaultMutableTreeNode>();
	private static PM_TreeModelAlbum instance = null;
	 
	
	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_TreeModelAlbum getInstance() {
		if (instance == null) {
			instance = new PM_TreeModelAlbum();
		}
		return instance;
	}
	
	
	// =====================================================
	// Konstruktor  
	// =====================================================
	private PM_TreeModelAlbum() {
		super();	
		rootNode.setUserObject(PM_MSG.getMsg("photalbum"));
		
		List<PM_Sequence> liste = PM_SequencesInout.getInstance().getList(SequenceType.ALBUM);
		Iterator<PM_Sequence> it = liste.iterator();
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
		    initAddSequInAlbum(seq);
			initAddSequence(seq);		
		}		
//		createSequenceDictionary();
		makeTreeModelListener();
		
		// Init fertig
		initDone();
	}

	private void initAddSequInAlbum(PM_Sequence sequ) {
		if (sequ instanceof PM_SequenceAlbum) {
			PM_SequenceAlbum s = (PM_SequenceAlbum)sequ;
			PM_Sequence closed = s.getSeqClosed();
			if (closed == null) {
				return;
			}
			if (closed instanceof PM_SequenceBase) {
				PM_SequenceBase.getSequenceInAlbum().add((PM_SequenceBase)closed);			 
			}
			if (closed instanceof PM_SequenceExtended) {
				PM_SequenceExtended.getSequenceInAlbum().add((PM_SequenceExtended)closed);
			}
		}	
	}
	
	
	 
	
	// =====================================================
	// getSequenceType()  
	// =====================================================	 
//	@Override
//	public SequenceType getSequenceType() {
//		return  SequenceType.ALBUM;
//	}
	
	
	// =========================================================================
	// makeTreeModelListener()
	// =========================================================================
	private void makeTreeModelListener() {

		PM_Listener l = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				PM_Sequence seq = (PM_Sequence)e.getObject();
				if (!(seq instanceof PM_SequenceAlbum)) {
					return;
				}
				PM_Sequence closed = ((PM_SequenceAlbum)seq).getSeqClosed();
				if (closed == null) {
					return;
				}
				int type = e.getType();
				if (type == NODE_INSERTED) {
					if (PM_SequenceBase.addSequenceInAlbum(closed)) {
						instance.nodeChanged(instance.getRootNode());
					}
					if (PM_SequenceExtended.addSequenceInAlbum(closed)) {
						instance.nodeChanged(instance.getRootNode());
					}
				}
				if (type == NODE_REMOVED) {

				}
			}
		};
		addChangeListener(l);
	}
	
	
	
	
	// =========================================================================
	// treeNodesInsertedRemoved()
	// =========================================================================
	/*
	private void treeNodesInsertedRemoved( ) {
	 
		createSequenceDictionary();					
	//	markSequenceSelected( );
//		nodeChanged(rootNode);
		
		// nodes not in album updaten
		nodesNotInAlbum();		
	}
	
	
	*/
 
	 
	// =====================================================
	// createSequenceDictionary()
	//
	// Zum Markieren (fett, wenn Sequence im B- oder X-Baum selektiert ist)
	//
	// key: PM_Sequence
	// value: Set of MutableTreeNode
	// =====================================================
	public Map<PM_Sequence, Set<DefaultMutableTreeNode>> getSequenceDictionary() {	
		return openSequenceMap;
	}
	 
	
	// =====================================================
	// createSequenceDictionary()
	//
	// Zum Markieren (fett, wenn Sequence im B- oder X-Baum selektiert ist)
	//
	// key: PM_Sequence
	// value: Set of MutableTreeNode
	// =====================================================
/*
	public void createSequenceDictionary() {		
		openSequenceMap = new HashMap<PM_Sequence, Set<DefaultMutableTreeNode>>();
///		System.out.println("   createSequenceDictionary aufgerufen");
		Enumeration e = rootNode.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			Object o = node.getUserObject();
			if (o instanceof PM_SequenceAlbum) {
				PM_SequenceAlbum se = (PM_SequenceAlbum)o;
				PM_Sequence	sequ = se.getSeqClosed();
					 
				if (sequ == null) continue;
				if (openSequenceMap.containsKey(sequ)) {
					Set<DefaultMutableTreeNode> s = (Set<DefaultMutableTreeNode>)openSequenceMap.get(sequ);
					s.add(node);
				} else {
					Set<DefaultMutableTreeNode>  mtn = new HashSet<DefaultMutableTreeNode>();
					mtn.add(node);
					openSequenceMap.put(sequ, mtn);
				}
			}
		}
		
		 
	}
	*/
	// =====================================================
	// markSelected()
	//
	// Im Baum der B- oder X-Serien wurde eine Sequenz selektiert.
	// Hier im Album-Baum sollen die Nodes, die diese Sequenz beinhalten
	// markiert werden.
	// =====================================================
/*
	private PM_Sequence sequenceSelected = null;

	public void setSequenceSelected(PM_Sequence sequenceSelected) {
		this.sequenceSelected = sequenceSelected;
		markSequenceSelected();
		nodeChanged(rootNode);
	}
	public void markSequenceSelected( ) {	 
		if (openSequenceMap.containsKey(sequenceSelected)) {
			markNodes = openSequenceMap.get(sequenceSelected);
		} else {
			markNodes = new HashSet<DefaultMutableTreeNode>();
		}
		 
	}
	
*/	
 
	
	
	// =========================================================================
	//  getSequenceListWrite()
	// =========================================================================
	@Override
    public ArrayList<PM_Sequence> getSequenceListClose(DefaultMutableTreeNode node ) {   	
    	return _getSequenceList(true, node );
    }
	
	
	// =====================================================
	// getMarkNodes()
	// =====================================================	
	public Set<DefaultMutableTreeNode> getMarkNodes() {
		return  markNodes;
	}
 
	// =====================================================
	// getSequenceForWrite()
	//
	// Erzeugen einer Sequence für Schreiben aller Sequenzen bei close.
	// Der node ist ein leaf vom type String.
	// =====================================================
	@Override
	protected PM_Sequence getSequenceForClose(DefaultMutableTreeNode node) {
		String path = PM_TreeModel.getPathFromNode(node);
		PM_Sequence seq = new PM_SequenceAlbum(path, null);
		seq.setStringLeaf(true);
		return seq;
		 
	}
	
	 
	
 
	
}
