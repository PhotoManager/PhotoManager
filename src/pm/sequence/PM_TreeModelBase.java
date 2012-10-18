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

import pm.inout.*;
import pm.utilities.*;

@SuppressWarnings("serial")
public class PM_TreeModelBase extends PM_TreeModel {

	private static PM_TreeModelBase instance = null;

	static public PM_TreeModelBase getInstance() {
		if (instance == null) {
			instance = new PM_TreeModelBase();
		}
		return instance;
	}

	private PM_TreeModelBase() {
		super();
		rootNode.setUserObject(PM_MSG.getMsg("winSeqBaseSequence"));
		List<PM_Sequence> liste = PM_SequencesInout.getInstance().getList(
				SequenceType.BASE);
		Iterator<PM_Sequence> it = liste.iterator();
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
			initAddSequence(seq);
		}
//		createNodesNotInAlbum();
		initDone();
	}

	@Override
	public ArrayList<PM_Sequence> getSequenceList() {
		return _getSequenceList(false, null);
	}

	@Override
	public ArrayList<PM_Sequence> getSequenceListClose(DefaultMutableTreeNode node) {
		return _getSequenceList(true, node);
	}

	@Override
	protected PM_Sequence getSequenceForClose(DefaultMutableTreeNode node) {
		String path = PM_TreeModel.getPathFromNode(node);
		PM_Sequence seq = new PM_SequenceBase(path, "");
		seq.setStringLeaf(true);
		return seq;
	}

	
}
