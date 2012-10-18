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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.*;

import javax.swing.tree.*;

import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

 
import pm.gui.*;
import pm.index.PM_Index;
import pm.index.PM_IndexViewImport;
import pm.index.PM_PictureView;
import pm.inout.*;
import pm.search.*;
import pm.sequence.*;
 
import pm.utilities.*;
import pm.utilities.PM_Interface.Import;
 

/**
 * The base class for import external/internal.
 * 
 *
 */
abstract public class PM_Import implements PM_Interface {

	
	protected Lock lock;
	protected PM_WindowDialogImport dialogImport;
	protected PM_ListenerX listener;
	protected PM_Index index;
	
	private Map<PM_Picture, PictureMiniSequence> miniSequenceNumbers = new HashMap<PM_Picture, PictureMiniSequence>();

	 
	/**
	 * Constructor for PM_ImportInternal or PM_ImportExternal
	 * 
	 * @param imprt - the type internal/external
	 */
	public PM_Import(Import imprt) {
		index = PM_WindowImport.getImportIndex();
		dialogImport = new  PM_WindowDialogImport(imprt);
		listener = dialogImport.getListener();
		lock = dialogImport.getLock();
	}
	
	
	
	/**
	 * Get double pictures.
	 * 
	 * @param newPictures - the pictures to check
	 * @return the double pictures in the list  newPictures
	 */
	protected   List<File> getNewDouble(List<File> newPictures, PM_ListenerX listener) {
		 
		// build a dictionary:
		//   key   --> the size of the file
		//   value --> list of files with same length
		HashMap<Long, List<File>> dic  = new HashMap<Long, List<File>>();
		for (File file: newPictures) {
			long lg =  file.length();
			List<File> value = null;
			if (dic.containsKey(lg)) {
				value =  dic.get(lg);
				value.add(file);
			} else {
				value = new ArrayList<File>();
				value.add(file);
				dic.put(lg, value);
			}			
		}		
		 
		// now write the NOT double pictures in the list 'uniqueList'
		List<File> uniqueList = new ArrayList<File>();
		for (Long lg: dic.keySet()) {
			
			if (listener != null) {
				int n = dic.get(lg).size();
				listener.actionPerformed(new PM_Action(null, n));	
			}
			
			List<File> files = dic.get(lg);
			if (files.size() == 1) {
				// There are only one file with the length lg
				uniqueList.add(files.get(0));
				continue;
			}
			// There are more than one files with the length lg.
			// Now make MD5 of every file and compare them.
			List<String> md5files = getMD5(files);
			Set<String> set = new HashSet<String>(md5files);
			for (String s: set) {
				uniqueList.add(files.get(md5files.indexOf(s)));
			}		 	
		}	 
 	 
		// Now get the double pictures
		List<File> doublePictures = new ArrayList<File>(newPictures);
		doublePictures.removeAll(uniqueList);
	 	
		return doublePictures;
	}
	
	
	
	 
	
	
	/**
	 * Get all known pictures of newPictures.
	 * 
	 * @param newPictures- all new pictures
	 * @return the pictures not known (they are not in the stock)
	  
	 */
	protected List<File> getKnownPictures(List<File> newPictures , PM_ListenerX listener) {
		
		 
		// all new files into the directory (key is length)
		HashMap<Long, List<File>> dicNew = new HashMap<Long, List<File>>();
		for (File file : newPictures) {
			long lg = file.length();
			List<File> value = null;
			if (dicNew.containsKey(lg)) {
				value = dicNew.get(lg);
				value.add(file);
			} else {
				value = new ArrayList<File>();
				value.add(file);
				dicNew.put(lg, value);
			}
		}
		 
		 
		// all original files into the directory (key is length)
		HashMap<Long, List<File>> dicOrig  = new HashMap<Long, List<File>>();
		for (PM_PictureDirectory pd: PM_MetadataContainer.getInstance().getPictureDirectories()) {
			for (File file: pd.getAllOrigValidFiles(true)) {
				long lg =  file.length();
				
				List<File> value = null;
				if (dicOrig.containsKey(lg)) {
					value =  dicOrig.get(lg);
					value.add(file);
				} else {
					value = new ArrayList<File>();
					value.add(file);
					dicOrig.put(lg, value);
				}	
			}
		}
 
		 
		// look for the pictures already as original files
		List<File> fileDelete = new ArrayList<File>();
		for (Long lg: dicNew.keySet()) {
			if (listener != null) {
				int n = dicNew.get(lg).size();
				listener.actionPerformed(new PM_Action(null, n));	
			}
			if (!dicOrig.containsKey(lg)) {
				continue;
			}			 
			// there are pictures in orig and new with the same length
			List<File> newFiles = dicNew.get(lg);
			List<String> md5Origs = getMD5(dicOrig.get(lg));
			List<String> md5News = getMD5(newFiles);
			int i = -1;
			for (File fileNew: newFiles) {
				i++;
				String md5New = md5News.get(i);			 
				for (String md5Orig: md5Origs) {				 				 
					if (md5New.equals(md5Orig)) {
						// das darf nicht sein
						fileDelete.add(fileNew);
						break;
					}
				}
			}			
		}	 
		
	
		return fileDelete;
		
		 
	}
 
	
	private   List<String> getMD5(List<File> files) {
		List<String> l = new ArrayList<String>(files.size());
		for (File file: files) {
			l.add(new String(PM_Utils.getMessageDigest(file)));
		}
		return l;	
	}
	
	
	/**
	 * The import from internal and external.
	 *  
	 */
	protected void runImport(List<File>  pictureFilesToImport, PM_Index index ) {
		
		PM_WindowMain.getInstance().getWindowRechts().setSelectedIndex(TAB_IMPORTIEREN);		
		dialogImport.setEnableInfoDoublePic(false);
				
		if (pictureFilesToImport.isEmpty()) {
			return;
		}
		
		_runImport(pictureFilesToImport,index); 
	}
	
 
	
	private void _runImport(List<File> pictureFilesToImport, PM_Index index) {

		// gather in this list the imported pictures
		List<PM_Picture> newPictures = new ArrayList<PM_Picture>(
				pictureFilesToImport.size());
		 miniSequenceNumbers = new HashMap<PM_Picture, PictureMiniSequence>();

		// now make PM_Picture instances for all the pictures to import
		for (File file : pictureFilesToImport) {
			file = copyFile(file);
			if (PM_MetadataContainer.getInstance().getNumberIndexFile(file) < 0) {
				// Yet not a picture directory. Create it now.
				PM_MetadataContainer.getInstance().createPictureDirectory(
						file.getParentFile());
			}
			PM_Picture picture = createPicture(file);
			PictureMiniSequence pms = getPictureMiniSequence(picture);
			if (pms != null) {
				miniSequenceNumbers.put(picture, pms);
			}
			newPictures.add(picture);
		}
		Collections.sort(newPictures,  SORT_TIME_AND_MINI_SEQ_NUMBER);
		
		index.data.addImport(newPictures, miniSequenceNumbers);
	}

	public final Comparator<PM_Picture> SORT_TIME_AND_MINI_SEQ_NUMBER = new Comparator<PM_Picture>() {
		public int compare(PM_Picture pic1, PM_Picture pic2) {
			long time1 = pic1.meta.getDateCurrent().getTime();
			long time2 = pic2.meta.getDateCurrent().getTime();
			int seqNumber1 = 0;
			int seqNumber2 = 0;
			if (miniSequenceNumbers.containsKey(pic1)) {
				seqNumber1 = miniSequenceNumbers.get(pic1).seqNumber;
			}
			if (miniSequenceNumbers.containsKey(pic2)) {
				seqNumber2 = miniSequenceNumbers.get(pic2).seqNumber;
			}
			if (time1 - time2 != 0) {
				if (time1 < time2) {
					return -1;
				}
				return 1;
			}
			return seqNumber1 - seqNumber2;
		};
	};
	
	
	 	
	private PM_Picture createPicture(File picturefile) {
		PM_Picture picture = PM_Picture.getPicture(picturefile);
		
		boolean init = picture.meta.getInit();
		picture.meta.setInit(true); // don't write to lucene
		PM_ReadImageThumbnail.setEXIFpictureMetadaten(picture);
		picture.meta.setInit(init);	
		 		
		return picture;
	}
	
 
	 
 
	/**
	 * 
	 */
	private PictureMiniSequence getPictureMiniSequence(PM_Picture picture) {
		
		PM_PictureImageMetadaten pim = picture.getImageMetadaten();
		Tag t = pim.getTag("FujiFilm Makernote", 4353);
		String seqNumber  = "0";
		if (t != null) {
			try {
				seqNumber  = t.getDescription();
			} catch (MetadataException e) {
				return null;
			}
		}		
		PictureMiniSequence pms = new PictureMiniSequence(picture);
		pms.seqNumber = PM_Utils.stringToInt(seqNumber);	
		return pms;		
	}
	
	
	
	

	

	
	/**
	 * copy the file (here for PM_ImportInternal)
	 * 
	 */
	protected File copyFile(File f) {
		return f;
	}
	


	
	/**
	 * inner-class for new Pictures.
	 */
	public class PictureMiniSequence implements Comparable<PictureMiniSequence>{
		public PM_Picture picture;
		public int seqNumber;
		/**
		 * Constructor
		 */
		public PictureMiniSequence(PM_Picture picture) {
			this.picture = picture;
			 
		}
		
		 
		public int compareTo(PictureMiniSequence pic) {
			long time1 = picture.meta.getDateCurrent().getTime();
			long time2 = pic.picture.meta.getDateCurrent().getTime();
			if (time1 - time2 != 0) {
				if (time1 < time2) {
					return -1;
				}
				return 1;
			}
			return seqNumber - pic.seqNumber;
			 
		}		
	}
	
 
	
	
}
