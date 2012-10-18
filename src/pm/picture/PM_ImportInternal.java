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
 
 
import pm.index.PM_Index;
import pm.inout.*;
import pm.search.*; 
import pm.utilities.*;
 
import java.io.File;
import java.util.*;
 


/**
 * Import pictures internal.
 * 
 * An internal import imports pictures from in or under the 
 * Top Level Picture Dirertory TLPD.
 *  
 *
 */
public class PM_ImportInternal extends PM_Import implements PM_Interface {
 	
	/**
	 * Create an instance to import pictures from internal.
	 *
	 */
	public PM_ImportInternal( ) {
		super(Import.INTERN );
	}
	
	
	 
	/**
	 * Check to import pictures.
	 * 
	 * All directories in or under the top level pictures directory
	 * are checked for unknown pictures.
	 * <p>
	 * All known pictures (pictures already in the stock) are identified and
	 * a wizard (dialog window) is started.
	 * There the user can decide to import or not the double pictures and
	 * start the import.
	 */
	public void checkAndImport( ) {

		 
		Thread startThread = new Thread() {
			public void run() {
				
				PM_MetadataContainer.getInstance().flush();
				PM_DatabaseLucene.getInstance().flush();
				PM_SequencesInout.getInstance().flush();
	
				// check for new pictures
				List<File> newPictures =  searchNewPicture( );
				if (newPictures == null) {
					return;
				}
 			
				// -------------------------------------------------------------
				// pr�fen auf doppelte:
				// 	newPictures  --> alle neuen inklusive doppelter oder vorhandener
				// 	picsDouble   --> doppelte innerhalb der neuen
				// 	picsInStock  --> Bilder, die bereits im Bestand sind
				// -------------------------------------------------------------
				// check if double:
				//    newPictures --> all the news including the double
				//    picsDouble  --> double within the new pictures
				//    picsInStock --> already known pictures
				List<File> picsDouble = getNewDouble(newPictures, null);  
				// pictures therefrom in the stock
				List<File> allNewsPic = new  ArrayList<File>(newPictures);
				allNewsPic.removeAll(picsDouble);
				List<File> picsInStock = getKnownPictures(allNewsPic, null);
				// Now import
				if (picsDouble.isEmpty() &&  picsInStock.isEmpty()) {
					int sizeNewPics = newPictures.size();
					// nodouble pictures found.
					// all to import.
					String msg = String.format(PM_MSG.getMsg("importMsgCountNoDouble"),sizeNewPics);				
					listener.actionPerformed(new PM_Action("max", sizeNewPics, msg));
					dialogImport.setEnableContinue(true);
					dialogImport.setEnableStop(true);					 
					boolean stop = dialogImport.await();
					if (stop) {
						dialogImport.dispose();
						return;
					}	
dialogImport.dispose();		// new !!!!!!!!!!!! 24.7.2011
					runImport(newPictures, index);						  		
// delete 24.7.2011						dialogImport.dispose();
					return;								
				}
	 			// there are double pictures found
				int doublePics = picsDouble.size() + picsInStock.size();
			 
				String msgWithDouble =  String.format(PM_MSG.getMsg("importMsgWithDouble"), newPictures.size(), doublePics);
				String msgWithoutDouble =  String.format(PM_MSG.getMsg("importMsgWithoutDouble"), (newPictures.size() - doublePics), doublePics);
				
				
				boolean withDouble = dialogImport.isDoublePicSelected();
				if (withDouble) {					
					listener.actionPerformed(new PM_Action("max", newPictures.size(),  msgWithDouble));							 
				}  else {
					listener.actionPerformed(new PM_Action("max", newPictures.size() - doublePics,  msgWithoutDouble));
				}
				dialogImport.setEnableContinue(true);
				dialogImport.setEnableStop(true);					 
				boolean stop = dialogImport.await();
				if (stop) {
					dialogImport.dispose();
					return;
				}	
				if (withDouble != dialogImport.isDoublePicSelected()) {
					withDouble = dialogImport.isDoublePicSelected();
					if (withDouble) {					
						listener.actionPerformed(new PM_Action("max", newPictures.size(),  msgWithDouble));							 
					}  else {
						listener.actionPerformed(new PM_Action("max", newPictures.size() - doublePics,  msgWithoutDouble));
					}				
				}			
				if (withDouble) {
dialogImport.dispose();		// new !!!!!!!!!!!! 24.7.2011
					runImport(newPictures, index);								
			 
				} else {
					newPictures.removeAll(picsDouble);
					newPictures.removeAll(picsInStock);
dialogImport.dispose();		// new !!!!!!!!!!!! 24.7.2011
					runImport(newPictures, index);	
					// doppelte l�schen
					for (File f: picsDouble) {
						PM_Utils.setStop(f);
	//					f.delete();
					}
					for (File f: picsInStock) {
						PM_Utils.setStop(f);
	//					f.delete();
					}
				}			
	// delete 24.7.2011			dialogImport.dispose();
			}
		};

		startThread.start();
 		
		
	}
	
	 
	/**
	 * check for new pictures
	 */
	private List<File> searchNewPicture() {
		  
	 

		int sizePicDir = PM_MetadataContainer.getInstance()
				.getPictureDirectories().size();
		listener.actionPerformed(new PM_Action("max", sizePicDir));
		List<File> newPictures = PM_MetadataContainer.getInstance()
				.getPicturesWithoutThumbs(dialogImport.getListener());

		int sizeNewPics = newPictures.size();
		if (sizeNewPics == 0) {
			
 
			String tld = PM_Configuration.getInstance().getTopLevelPictureDirectory().getPath();
			String text =  String.format(PM_MSG.getMsg("importMsgNoPictures"),  tld);
			
			
			listener.actionPerformed(new PM_Action("max", 0, text));
			dialogImport.setEnableContinue(true);
			dialogImport.setEnableStop(false);
			dialogImport.setEnableInfoDoublePic(false);
			dialogImport.await();
			// gesamten Dialog beenden
			dialogImport.dispose();
			return null;
		} else {
 
		 
			String t =  String.format(PM_MSG.getMsg("importMsgPicsFound"),  sizeNewPics);
			
			
			listener.actionPerformed(new PM_Action("max", sizeNewPics,
					t));
 
			dialogImport.setEnableContinue(true);
			dialogImport.setEnableStop(true);
			boolean stop = dialogImport.await();
			if (stop) {
				dialogImport.dispose();
				return null;
			}
		}
		
		return newPictures;

	}
	

	
	
}
