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
package pm.picture;

import java.io.File;
import java.util.*;

import pm.index.PM_Index;
import pm.inout.*;
import pm.search.*;
import pm.utilities.*;
 
/**
 * Import pictures external.
 *  
 * An external import get pictures from outside the 
 * Top Level Picture Directory TLPD.
 *
 */
public class PM_ImportExternal extends PM_Import implements PM_Interface  {
	
 
	
	
	/**
	 * Create an instance to import pictures from external.
	 *
	 */
	public PM_ImportExternal( ) {
		super(Import.EXTERN );
	}
	 
	/**
	 * Check to import pictures.
	 * 
	 * All the pictures in the pictureDirs list are to import.
	 * The pictures to import are checked if already in the stock.
	 * If so this pictures EXCLUDED from import.
	 * <p>
	 * All pictures are copied into a the temporary directory
	 * DIR_IMPORT_EXTERNAL (a directory under the TLPD).
	 * <p>
	 * Then the pictures copies from the temp. directory to a
	 * target directory (yyyy/yyyy_mm --> year / month) and are 
	 * deleted in the temp. directory.
	 *  
	 * 
	 * 
	 * 
	 * @param pictureDirs - list of directories and files 
	 */
	public void checkAndImport(final List<File> pictureDirs ) {
 		
		// dialogImport.
		Thread startThread = new Thread() {
			public void run() {

				PM_MetadataContainer.getInstance().flush();
				PM_DatabaseLucene.getInstance().flush();
				PM_SequencesInout.getInstance().flush();

				boolean stop = false;

				// in the list "pictureDirs" are all directories and files
				// to import.
				// Put only files into the newPictureFile list.
				// get the number of pictures  to import
				List<File> newPictureFile = new ArrayList<File>();
				listener.actionPerformed(new PM_Action("max", -1, ""));
				 
				for (File file : pictureDirs) {
					if (file.isFile()) {
						if (PM_Utils.isPictureFile(file)) {
							newPictureFile.add(file);
						}
					} else {
						addFiles(file, newPictureFile);
					}		
				}
				
				int size = newPictureFile.size();
				if (size == 0) {
					String text = "Keine neuen Bilder zum Importieren gefunden.";
							 
					listener.actionPerformed(new PM_Action("max", 0, text));
					dialogImport.setEnableContinue(true);
					dialogImport.setEnableStop(false);
					dialogImport.await();
					// gesamten Dialog beenden
					dialogImport.dispose();
					return;
				} else {
					String t = (size == 1) ? "1 neues Bild gefunden."
							: size + " neue Bilder gefunden";
					listener.actionPerformed(new PM_Action("max", size,
							t));
					dialogImport.setEnableContinue(true);
					dialogImport.setEnableStop(true);
					stop = dialogImport.await();
					if (stop) {
						dialogImport.dispose();
						return;
					}
				}			
	 				
				// --------------------------------------------------------
				// check if double. NO double pictures shall be import.
				// --------------------------------------------------------
				int newPics = newPictureFile.size();
				listener.actionPerformed(new PM_Action("max", newPics * 2, "Pr�fen auf vorhandene Bilder.")); 
				List<File> picsDouble = getNewDouble(newPictureFile, listener);
				newPictureFile.removeAll(picsDouble);			 
				List<File> picsInStock = getKnownPictures(newPictureFile, listener);
				newPictureFile.removeAll(picsInStock);
				 
				if (newPictureFile.isEmpty()) {
					// All pictures are known (no pictures are left to import)
					listener.actionPerformed(new PM_Action("max", 0, "Alle Bilder bereits vorhanden"));
					dialogImport.setEnableContinue(true);
					dialogImport.setEnableStop(false);
					dialogImport.await();
					// terminate the dialog
					dialogImport.dispose();
					return;
				}				
				int doublePics = size - newPictureFile.size();
				if (doublePics == 0) {
					listener.actionPerformed(new PM_Action("max", size, "Alle Bilder importieren (keine vorhanden)"));
				} else {
					listener.actionPerformed(new PM_Action("max", newPictureFile.size(), 
							newPictureFile.size() + " Bilder importieren (" +
							doublePics + " Bilder sind bereits vorhanden)" ));
				}

				dialogImport.setEnableContinue(true);
				dialogImport.setEnableStop(true);
				stop = dialogImport.await();
				if (stop) {
					dialogImport.dispose();
					return;
				}
				// The pictures to import are in the list "newPictureFile
				runImport(newPictureFile, index);
				dialogImport.dispose();
				return;

			}
		};
		
		startThread.start();

	}
 
	
	/**
	 * add all files to newPictureFile.
	 * 
	 * @param - dir a directory with all the files to add (recursive)
	 * @param - newPictureFileList the result list with all the files
	 */
	private void addFiles(File dir, List<File> newPictureFileList) {
		for (File file: dir.listFiles()) {
			if (file.isDirectory()) {
				addFiles(file, newPictureFileList);
			}
			if (PM_Utils.isPictureFile(file)) {
				newPictureFileList.add(file);
			}
		}	
		 
	}
	
	/**
	 * Copy a picture from the temporary file to the target file.
	 * 
	 * Delete the temporary file. If the target file already exists
	 * it create a 'unique' target file.
	 * 
	 * @param file - temp. file to copy
	 * @return the 'unique' target file
	 */
	@Override
	protected File copyFile(File file) {
		 
		File f = PM_Utils.copyFileUnique(file, dialogImport.getTargetDir());
		if (f == null) {
			return file; // ERROR cpoy
		}
		 
		return f;
	}

}
