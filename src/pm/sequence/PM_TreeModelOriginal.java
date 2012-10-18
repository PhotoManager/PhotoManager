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

import javax.swing.tree.*;

 
import pm.gui.*;
import pm.index.PM_IndexViewImport;
import pm.inout.*;
import pm.picture.*;
 
import pm.utilities.*;
 

@SuppressWarnings("serial")
public class PM_TreeModelOriginal extends PM_TreeModel implements PM_Interface {

	
	
	private static PM_TreeModelOriginal instance = null;
	private boolean initDone = false;
	
	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_TreeModelOriginal getInstance() {
		if (instance == null) {
			instance = new PM_TreeModelOriginal();
		}
		return instance;
	}
	
	
	// =====================================================
	// Konstruktor  
	// =====================================================
	private PM_TreeModelOriginal() {
		super();
	}
	
	
	/**
	 * 
	 */
	public void init() {
		if (initDone) {
			return;
		}
		initDone = true;
		
		update();
		
		PM_WindowSearch ws = PM_WindowMain.getInstance().getWindowRechts().getWindowSuchen();
		PM_TreeWindow tw = ws.getTreeWindowOriginal();	
		tw.expandTree(2);
		
		
		PM_Listener changeListerer = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// update Anzahl Pictures
				if (e.getObject() == null) {
	/////////				update();
				}
			}
		};
 
		PM_IndexViewImport.addImportListener(changeListerer);
		PM_DeletePictures.addDeleteListener(changeListerer);
	}
// ************************ update (oben) entfernt *******************
	/**
	 *    
	 */
	private void update() {
		List<PM_PictureDirectory> dirs = PM_MetadataContainer.getInstance().getPictureDirectories();
			
		Set<PM_PictureDirectory> toInstall = new HashSet<PM_PictureDirectory>(dirs);
		List<DefaultMutableTreeNode>  remove = new ArrayList<DefaultMutableTreeNode>();
		// ------------------------------------------------------------
		// pr�fen was zu tun:
		//     die neu zu installieren sind --> toInstall
		//     nodes, die zu l�schen sind   --> remove
		// -----------------------------------------------------------
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
			Object o = n.getUserObject();
			if (o instanceof PM_PictureDirectory) {
				PM_PictureDirectory pd = (PM_PictureDirectory)o;
				if (toInstall.contains(pd)) {
					toInstall.remove(pd);
				} else {
					remove.add(n);
				}
			}		
		}
		// "toInstall" jetzt anlegen
		for (PM_PictureDirectory pd: toInstall) {							
			 
			DefaultMutableTreeNode node = rootNode;
			for (String s:  pd.getTreePath()) {
				node = insert(node,  s);
			}
			
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(pd);
			String name = pd.getDirOrigFile().getName();
 		 
			Enumeration eeee = node.children();
			while (eeee.hasMoreElements()) {
				DefaultMutableTreeNode nnnn = (DefaultMutableTreeNode)eeee.nextElement();
  
				if (name.equals(nnnn.getUserObject())) {
					nnnn.setUserObject(pd);
					 
					n = null;
				}
			}	
			if (n != null) {
				node.add(n);	
			}
		}
		// "remove" jetzt l�schen (nodes)
		for (DefaultMutableTreeNode node: remove) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
			while (parent != null) {
				if (parent.getChildCount() == 1) {
					DefaultMutableTreeNode n = parent;
					parent = (DefaultMutableTreeNode)parent.getParent();
					n.removeFromParent();
					continue;
				} else {
					break;
				}
			}
		}
		// jetzt ist der Baum fertig.
		// Noch die Gesamtzahl der Bilder im Root anzeigen.
		int count = PM_MetadataContainer.getInstance().getPictureSizeValid();
		rootNode.setUserObject("Bilder-Verzeichnis (insgesamt " + count + " Bilder)");
		
//		nodeChanged(getRootNode());
		nodeStructureChanged(rootNode); 		
	}	
	/**
	 * 
	 */
	private DefaultMutableTreeNode insert(DefaultMutableTreeNode node, String s) {
		Enumeration e = node.children();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)e.nextElement();
			String str = n.getUserObject().toString();
			if (n.getUserObject() instanceof PM_PictureDirectory) {
				PM_PictureDirectory pd = (PM_PictureDirectory)n.getUserObject();
				str =  pd.getDirOrigFile().getName();
			}				
			if (str.compareTo(s) == 0) {
				return n;
			}					 			
		}
		DefaultMutableTreeNode n = new DefaultMutableTreeNode(s);
		node.add(n);
		return n;	
	}
			

 
	 
	
}
