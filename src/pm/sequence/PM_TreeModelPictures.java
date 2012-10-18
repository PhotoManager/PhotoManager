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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
 
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

 
import pm.utilities.*;
 

/**
 * tree model for valids pictures.
 * 
 * @author dih
 *
 */
@SuppressWarnings("serial")
public class PM_TreeModelPictures extends PM_TreeModel implements PM_Interface {

	private static PM_TreeModelPictures instance = null;

	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_TreeModelPictures getInstance() {
		if (instance == null) {
			instance = new PM_TreeModelPictures();
		}
		return instance;
	}

	// =====================================================
	// Konstruktor (private)
	// =====================================================
	private PM_TreeModelPictures() {
		super();

	}
 
	
	/**
	 * clear and build the tree from igameDir.
	 *
	 */
	public int buildPictureTree(File imageDir) {
		bilderGesamt = 0;
		if (!imageDir.isDirectory()) {
			return bilderGesamt;
		}	 		 
		clear();
		lesenDir(imageDir, rootNode);	
		return bilderGesamt;
	}

	
	/**
	 * clear the tree.
	 */
	public void clear() {
		rootNode.removeAllChildren();
		nodeStructureChanged(rootNode);
	}
	
	
	/**
	 * read recursive the directory and build the tree.
	 */
	private int bilderGesamt = 0;
	private boolean lesenDir(File dir, DefaultMutableTreeNode node) {

		boolean darstellen = false;

		 
		File[] ff = dir.listFiles();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < ff.length; i++) {
			files.add(ff[i]);
		}
		Collections.sort(files, PM_Utils.SORT_FILE_NAMES);
		int bilder = 0;
		for (int i = 0; i < files.size(); i++) {
			File f = (File) files.get(i);
			if (f.isDirectory()) {
				if (f.list().length == 0) {
					continue;
				}
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(f.getName());	
				lesenDir(f, newNode);
				node.add(newNode);		
			}			
			if (PM_Utils.isPictureFile(f)) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(f.getName());
				node.add(newNode);
				darstellen = true;
				
				bilder++;
				bilderGesamt++;
			}
		}	 
		return darstellen;
	}
	

	
}
