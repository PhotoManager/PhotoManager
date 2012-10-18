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
 
 
import java.util.List;
 

import javax.swing.tree.*;

import pm.inout.*;
import pm.utilities.*;
 

@SuppressWarnings("serial")
public class PM_TreeModelExtended  extends PM_TreeModel  implements PM_Interface {

	
	private static PM_TreeModelExtended instance = null;
	 
	
	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_TreeModelExtended getInstance() {
		if (instance == null) {
			instance = new PM_TreeModelExtended();
		}
		return instance;
	}
	 
	 
	private PM_TreeModelExtended() {
		super();
		rootNode.setUserObject(PM_MSG.getMsg("winSeqExtendedSequence"));//"Erweiterte Serien (X-Serien)");
		

		List<PM_Sequence> liste = PM_SequencesInout.getInstance().getList(
				SequenceType.EXTENDED);
		Iterator<PM_Sequence> it = liste.iterator();
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
			initAddSequence(seq);
		}
		
		
//		createNodesNotInAlbum();
		initDone();
	}
	
	 
	@Override
    public ArrayList<PM_Sequence> getSequenceList( ) {   	
    	return _getSequenceList(false, null );
    }
	
	// =========================================================================
	//  getSequenceListWrite()
    // Wenn alle Sequenzen zurückgeschrieben werden
	// =========================================================================
	@Override
    public ArrayList<PM_Sequence> getSequenceListClose(DefaultMutableTreeNode node ) {   	
    	return _getSequenceList(true, node );
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
		PM_Sequence seq = new PM_SequenceExtended(path, "");
		seq.setStringLeaf(true);
		return seq; 
	}
    
 
	 
	
}
