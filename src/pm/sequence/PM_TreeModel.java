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

 
import java.util.*;

import javax.swing.event.*;
import javax.swing.tree.*;

 
import pm.utilities.*;
 
 
 

/** TreeModel für einen Sequence tree
 *  
 * 
 * Er wird hier erzeugt. Mit ihm kann ein JTree instantiiert werden:
 * 
 *  
 * 
 
 */
@SuppressWarnings("serial")
abstract public class PM_TreeModel extends DefaultTreeModel implements
		PM_Interface {
	
	 
	public DefaultMutableTreeNode rootNode;
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_TreeModel () {
		super(new DefaultMutableTreeNode());	
		rootNode = (DefaultMutableTreeNode)getRoot ();		
	}

	
	// =========================================================================
	//  ChangeListener()
	//
	// Eigener ChangeListener.
	// (Der eigentliche TreeModelListener wird hier in dieser Klasse abgefangen
	// und es wird der (PM)ChangeListener "gefired")
	// =========================================================================
	private Set<PM_Listener> changeListener = new HashSet<PM_Listener>();
    public void addChangeListener(PM_Listener listener) {	
    	changeListener.add(listener); 
	}
    public void fireChangeListener(DefaultMutableTreeNode node, int type ) {
    	if (!(node.getUserObject() instanceof PM_Sequence)) {
    		return;
    	}
    	PM_Sequence seq = (PM_Sequence)node.getUserObject(); 
    	for (PM_Listener listener: changeListener) { 
    		listener.actionPerformed(new PM_Action(seq, type));
    	}
	}
    public void fireNameChanged(DefaultMutableTreeNode node) {	
 ///////////////////   	fireChangeListener(node, NODE_RENAMED );
	}
	 
//    hier weiter: myInsert ....    myRemove ....  myMove 
    
    
    public void insertNodeInto(MutableTreeNode newChild,
            MutableTreeNode parent,
            int index) {
System.out.println("... insertNodeInto: newChild: " + newChild + ", parent: " + parent + ", index: " + index);
    	super.insertNodeInto(newChild, parent, index);
    		
    }
    
    public void removeNodeFromParent(MutableTreeNode node) {
System.out.println(".... removeNodeFromParent. node: " + node);
    	super.removeNodeFromParent(node);
    }
    
	// =========================================================================
	//  initDone()
    //
    // Hier wird nach dem Aufbau der Nodes der TreeModelListener erzeugt.
    //
    // (Er darf erst nach erfolgreicher Initialisierung der Nodes erfolgen)
	// =========================================================================
    protected void initDone() {	
    	
    	
    	
		TreeModelListener tml = new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
			}

			public void treeNodesInserted(TreeModelEvent e) {		
				DefaultMutableTreeNode node = getChangedNode(e);
				if (node != null) {
					fireChangeListener(node, NODE_INSERTED  );
				}				 
			}

			public void treeNodesRemoved(TreeModelEvent e) {
				DefaultMutableTreeNode node = getChangedNode(e);
				if (node != null) {
					fireChangeListener(node, NODE_REMOVED  );
				}
			}

			public void treeStructureChanged(TreeModelEvent e) {
System.out.println("TREE Structure changed. Event: " + e);
			}
		};
		addTreeModelListener(tml);
	}
	
    
    
    
    
	private DefaultMutableTreeNode getChangedNode(TreeModelEvent e) {
		for (Object c : e.getChildren()) {
			if (c instanceof DefaultMutableTreeNode) {
				return (DefaultMutableTreeNode) c;
			}
		}
		return null;
	}
    
    
	// =========================================================================
	//  getRootNode()
	// =========================================================================
    public DefaultMutableTreeNode getRootNode() {	
		return rootNode;
	}
	
	// =========================================================================
	//  removeSequence()
	// =========================================================================
    public void removeSequence(PM_Sequence seq) { 	 
    	// Suchen node
       	List<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>();    	 
    	Enumeration en = rootNode.preorderEnumeration();
    	while (en.hasMoreElements()) {
    		DefaultMutableTreeNode tn  = (DefaultMutableTreeNode)en.nextElement();
    			if (seq == tn.getUserObject())  {
    				nodeList.add(tn);	
    			}		 
    	}   	
    	for (DefaultMutableTreeNode node: nodeList) {
    		removeNodeFromParent(node);
    		nodeChanged(rootNode); 		
    	}
    }
 
 	 
    public ArrayList<PM_Sequence> getSequenceList( ) {
    	return new ArrayList<PM_Sequence>();
    }
	 
    public ArrayList<PM_Sequence> getSequenceListClose(DefaultMutableTreeNode node ) {   	
    	return new ArrayList<PM_Sequence>();
    }

    
    

    
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	// ====================== private ========================
	
	// =========================================================================
	//  getSequenceList()
    //
    // close = true:  für getSequenceListWrite (wenn die Sequencen bei
    //                     close geschrieben werden)
	// =========================================================================
    protected ArrayList<PM_Sequence> _getSequenceList(boolean close, DefaultMutableTreeNode node ) {
    	ArrayList<PM_Sequence> list = new ArrayList<PM_Sequence>();
    	if (rootNode.getChildCount() == 0) {
    		return list; 
    	}
    	
    	DefaultMutableTreeNode fromNode = (node == null) ? rootNode : node;  	
    	Enumeration en = fromNode.preorderEnumeration();
    	while (en.hasMoreElements()) {
    		DefaultMutableTreeNode tn  = (DefaultMutableTreeNode)en.nextElement();
    		if (tn.isLeaf() && tn.getUserObject() instanceof PM_Sequence ) {
    			PM_Sequence seq = (PM_Sequence)tn.getUserObject();
    			if (close) {
    				seq.setPath(getPathFromNode(tn));
    			}
    			list.add(seq);			
    		} else if (close && tn.isLeaf() && tn.getUserObject() instanceof String) {
    			// bei Write in Datei müssen auch die Leaf ohne PM_Sequence 
    			// geschrieben werden
    			PM_Sequence seq = getSequenceForClose(tn);
    			if (seq != null) {
    				list.add(seq);
    			}
    			
    		}
    	}   	
    	return list;
    }
    
    
	protected PM_Sequence getSequenceForClose(DefaultMutableTreeNode node) {
		return null;
	}
    static public String getPathFromNode(DefaultMutableTreeNode node) {
    	 
    	if (node.getParent() == null) {
    		return "";
    	}
    	String path = "";
  
    	TreeNode parent = node;
    	if (node.getUserObject() instanceof PM_Sequence) {
    		path = ((PM_Sequence)node.getUserObject()).getLongName();
    	} else {
    		path = node.toString();
    	} 	
    	parent = node.getParent();
    	while (parent != null && parent.getParent() != null) {
    		path  = parent.toString() + "." + path;
    		parent = parent.getParent();
    	} 	
    	return path;
    }
	
 
    
    
	// =====================================================
	// initAddSequence()
	//
	// Die Sequenz wird im Tree (neu) hinzugefügt
	// =====================================================
	protected  void initAddSequence(PM_Sequence sequ) {
		
		// ---------------------------------------------------------------------
		// loop 'über' den Pfad (der letzte Eintrag wird ausgeschlossen)
		// ---------------------------------------------------------------------
		String[] alleNamen = sequ.getPath().split("\\.");
		DefaultMutableTreeNode node = rootNode;
		String lastName = "";
		if (alleNamen.length >= 0) {
		   lastName = alleNamen[0].trim();
		}
		for (int i = 0; i < alleNamen.length-1; i++) { 
			String name = alleNamen[i].trim();
			lastName = alleNamen[i+1].trim();
			if (name.length() == 0) {
				continue;
			}
			node = initAddNode(node, name);  			
		}
		// jetzt den leaf (String oder Sequenz)
		if (sequ.getStringLeaf()) {
			node = initAddNode(node, lastName); 
		} else {
			node = initAddNode(node, sequ);
		}
	}
	
	
	// =========================================================================
	// addNode()
	// 
	// Es wird ein neuer Child an den übergebenen node angehängt.
	// (userObject ist entweder String oder PM_Sequence)
	//
	// return: neuer Child oder einen bereits vorhanden.
	// =========================================================================
	private DefaultMutableTreeNode initAddNode(DefaultMutableTreeNode node,
			Object userObject) {

		for (Enumeration e = node.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) e
					.nextElement();
			if (child.toString().equals(userObject.toString())) {
				return child;
			}
		}

		// überhaupt keine Childs vorhanden
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(userObject);
		node.add(newChild);
		return newChild;
	}
	
	
	// =========================================================================
	//  getNodesNotInAlbum ()
	//  createNodesNotInAlbum()
	//
	//  Für Base und Extended:
	//	  Erzeugen  Set der Nodes, desses Sequenz NICHT im Album-Baum stehen
	//    (die sollen in den Bäumen Base und X  markiert werden)
	// =========================================================================
/*
	protected Set<DefaultMutableTreeNode> nodesNotInAlbum = new HashSet<DefaultMutableTreeNode>();
	public Set<DefaultMutableTreeNode> getNodesNotInAlbum () {
		return nodesNotInAlbum;
	}
    public  void createNodesNotInAlbum() {   
    	PM_TreeModelAlbum tma =  PM_TreeModelAlbum.getInstance();
    	tma.createSequenceDictionary();  	
    	Map<PM_Sequence, Set<DefaultMutableTreeNode>> sequenceMap = tma.getSequenceDictionary();
    	
    	
    	
    	nodesNotInAlbum = new HashSet<DefaultMutableTreeNode>();
    	Enumeration en = rootNode.preorderEnumeration();
    	while (en.hasMoreElements()) {
    		DefaultMutableTreeNode tn  = (DefaultMutableTreeNode)en.nextElement();
    		if (tn.isLeaf() && tn.getUserObject() instanceof PM_Sequence ) {
    			PM_Sequence seq = (PM_Sequence)tn.getUserObject();
    			if ( ! sequenceMap.containsKey(seq)) {
    				nodesNotInAlbum.add(tn);
    			}		 		
    		}
    	}  
    }
	*/
    
    
/*	 
	static public void nodesNotInAlbum() {
		PM_TreeModelBase tmb =  PM_TreeModelBase.getInstance();
		PM_TreeModelExtended tmx =  PM_TreeModelExtended.getInstance();
		tmb.createNodesNotInAlbum();
		tmx.createNodesNotInAlbum();
		tmb.nodeChanged(tmb.rootNode);
		tmx.nodeChanged(tmx.rootNode);
	}
	
*/	

	
	
}
