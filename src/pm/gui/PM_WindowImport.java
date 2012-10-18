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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;


 
import pm.index.PM_Index;
import pm.picture.*;
import pm.search.*;
 
import pm.sequence.*;
import pm.utilities.*;
 

@SuppressWarnings("serial")
public class PM_WindowImport extends PM_WindowBase implements PM_Interface  {

	
	static private List<File> newExternalPictureDirs = Collections.synchronizedList(new ArrayList<File>());
	private static PM_WindowImportGUI gui;  
	
	// ======================================================
	// Konstruktor
	// ======================================================
	public PM_WindowImport(  ) {
		super(null);
		
		gui = new PM_WindowImportGUI(this, windowMain);
		
//		setLayout(new FlowLayout(FlowLayout.LEFT));
		setLayout(new BorderLayout());
		add( gui.getComponent() );	
	}
	
	
	/**
	 * pictures to import from external was found.
	 * 
	 * Add them to the list "newExternalPictureDirs".
	 * 
	 * NO import take place !!!!
	 * 
	 * The method is static (perhaps no instances of this
	 * class was created)
	 */
	public static boolean addImportFiles(List<File> fileList ) {
		// in der File-Liste "newExternalPictureDirs" sind nur Canonical Files.
		Set<String> canonicalPathSet = new HashSet<String>();
		for (File f: newExternalPictureDirs) {
			canonicalPathSet.add(f.getPath());
		}
		// nur die �bernehmen, die noch nicht in der newExternalPictureDirs sind
		for(File f: fileList) {
			if (!f.exists()) {
				continue;
			}
			// darf nicht unter TLD sein
			if ( PM_Utils.isUnderTLD(f)) {
				continue;
			}		
			
			File file;
			try {
				file =  f.getCanonicalFile();        
			} catch (IOException e) {		 
				continue;
			}
			if (canonicalPathSet.contains(file.getPath())) {
				continue;
			}
			newExternalPictureDirs.add(file);
		}
		return (newExternalPictureDirs.isEmpty()) ? false : true;

	}
	
	/**
	 * An import taking place.
	 */
	public void importTakePlace(boolean takePlace) {
		gui.importTakePlace(takePlace);
	}
	
	/**
	 * set the timer to notify an external import in the status line.
	 *  
	 */
	public static void setTimer() {
		// jetzt sind alle �bernommen
		if (newExternalPictureDirs.isEmpty()) {
			PM_WindowStatusPanel.getInstance().stopImportTimer();
			gui.importButtonExternal.setEnabled(false);
			return;
		}

		PM_WindowStatusPanel.getInstance().startImportTimer();
		gui.importButtonExternal.setEnabled(true);

	}
	

	static public PM_Index getImportIndex() {
		return gui.getIndex();
	}
 
	 
	/**
	 * Check if there are pictures to import from external.
	 */
	public boolean externalImport() {
		return !newExternalPictureDirs.isEmpty();
	}
	
	/**
	 * the tab want to change.
	 */
	@Override
	public boolean requestToChange() {
		return true;
	}
	/**
	 * 
	 */
	public PM_TreeWindow getTreeWindowNew() {
		return  gui.treeWindowNew;
	}
	
	/**
	 * request to import
	 */
	public boolean requestToImport() {
		
		String text = "Vor dem Import werden alle Darstellungen gel�scht.";
		int n = JOptionPane.showConfirmDialog(null, text,
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return false;
		}		
		
		// first close all thumbs, do flush ...
	
		if (!windowMain.flushAndCloseDisplay()) {
			// can't flush
			return false;
		}
		
		return true;
	}
  
	
	
	/**
	 * do import from external
	 */
	public void doImportExternal( ) {
 		
		List<File> pictureDirs = new ArrayList<File>();
 		synchronized(newExternalPictureDirs) {
 			pictureDirs = new ArrayList<File>(newExternalPictureDirs);			
		 }
  		
 		PM_ImportExternal impExt = new PM_ImportExternal( ); 
 		impExt.checkAndImport(pictureDirs );
 
		 
	}
	
 
	// ======================================================
	// doubleClickOnTree( );
	//
	// z.B. bei Doppelclick
	// ======================================================
	@Override
	public void doubleClickOnTree(DefaultMutableTreeNode tn, PM_TreeWindow tw) {
		List<PM_Picture> picList = getPictureList(tn);
		if (picList.size() == 0) {
			return;
		}
		windowMain.getWindowLeft().getIndex().data.clearAndAdd(picList);
		 
	}
	

	// ======================================================
	// getPictureList( );
	//
	// (kopiert aus PM_WindowSuchen. Wird nicht alles ben�tigt, da
	//  hier nur neue Serien)   Besser machen !!!!!!
	// ======================================================
	private List<PM_Picture> getPictureList(DefaultMutableTreeNode tn) {

		if (tn == null) {
			return new ArrayList<PM_Picture>();
		}
		
		Object s = tn.getUserObject(); 
		if (s instanceof PM_PictureDirectory) {
			// Suchen von Original (Festplatte)
			PM_PictureDirectory pd = (PM_PictureDirectory)s;				
			List<File> files = pd.getOrigFiles();	
			List<PM_Picture> pics = PM_Pictures.getPictureList(files); 
			Collections.sort(pics, PM_Utils.SORT_TIME_ORDER);
			return 	pics;	 
		}
	
		PM_Sequence sequenz = null;
		if (s instanceof PM_Sequence) {
			sequenz = (PM_Sequence) s;			 
		} 
		if (sequenz == null) {	
			return new ArrayList<PM_Picture>();
		}	
		// Der TreeNode hat eine Sequenz
 		
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(sequenz);
		PM_Search luceneSuchen = new PM_Search(searchExpr);
		int anzahl = luceneSuchen.search();
		if (anzahl == 0) {
			return new ArrayList<PM_Picture>();
		}
		if (sequenz instanceof PM_SequenceOriginal) {
			return luceneSuchen.getPictureList(SearchSortType.FILE_PATH);
		}
		
		
		if (sequenz instanceof PM_SequenceAlbum && ((PM_SequenceAlbum)sequenz).getSeqClosed() == null) {
			return luceneSuchen.getPictureList(SearchSortType.TIME);
		}	
		return luceneSuchen.getPictureList(SearchSortType.SEQ);
	}
		
	
}
