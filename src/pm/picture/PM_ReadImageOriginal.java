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
 
import java.awt.*;
import java.util.*;
import java.util.List;


/**  
 * Class to read the picture image.
 * 
 * A singleton (only one instance) is created.
 *   
 *
 */
public class PM_ReadImageOriginal {
	
	private static final PM_ReadImageOriginal INSTANCE = new PM_ReadImageOriginal();
	
	private List<PM_Picture>  picturesRead  = new ArrayList<PM_Picture>();
	private List<PM_Picture>  picturesPrefetch = new ArrayList<PM_Picture>();
	private PM_Picture justReadAsynchron = null;
	
	private final PM_ReadImageOriginalThread imageOriginalReadThread;
	
    /**
     * Get the instance of the singleton.
     * 
     * Only one instance of the class is created.
     *  
     */
	static public PM_ReadImageOriginal getInstance() {
		return INSTANCE;
	}
	
	// Private constructor prevents instantiation from other classes
	private PM_ReadImageOriginal () {
		imageOriginalReadThread = new PM_ReadImageOriginalThread(this);
		imageOriginalReadThread.start();
	};
	
 
	
	 
	/**
	 * Read the images.
	 * 
	 * Here I only read the images. They are stored in the PM_Picture instances.
	 * I know the images are read (stored in the list 'picturesRead') and
	 * the images I must read in the future (stored in the list 'picturesPrefetch'). 
	 * I delete all the images not needed. 
	 * 
	 * @param readSynchron - the images to read synchron (I need them immediately)
	 * @param readAsynchron - start to read asynchronous
	 */
	public synchronized void readImages(List<PM_Picture> readSynchron, List<PM_Picture> readAsynchron) {
   
		// remove PM_Pictures if in both lists
		for (Iterator i=readSynchron.iterator(); i.hasNext();) {
			PM_Picture p = (PM_Picture)i.next();
			if (readAsynchron.contains(p)) readAsynchron.remove(p);
		}

		 
		// I need at once an image that are just reading asynchronous.
		if (justReadAsynchron != null && readSynchron.contains(justReadAsynchron)) {
			// I nead it. Wait until read.
			waitUntilRead();
		}  
		 
		// remove the pictures not needed to read.
		for (Iterator i=picturesRead.iterator(); i.hasNext();) {
			PM_Picture p = (PM_Picture)i.next();
			if ( !readSynchron.contains(p)) {
				if ( !readAsynchron.contains(p)) {
					removeImage(p);  // I don't need it
					i.remove();      // remove from the list 'picturesRead'
				}
			}
		}		
		
		// set the lists 
		picturesRead.clear();
		picturesRead.addAll(readSynchron);
		
		picturesPrefetch.clear();
		picturesPrefetch.addAll(readAsynchron);
	 	
		// Now I must read the images that I need at once.
		for (PM_Picture p: picturesRead) {
			if ( !p.hasImageOriginal() ) {
				readSynchron(p);
			}
		}		
			 
		// Start the asynchronous reading.
		imageOriginalReadThread.startToRead();
 
		// That's all for now.
		 
	}
	
	
	
	 
	/**
	 * Next to read asynchronously.
	 * 
	 * The read thread need the next picture.
	 */
	public synchronized void getNextToRead() {
		 
		// If one are read put it to the list 'picturesRead'
		if (justReadAsynchron != null) {
			if ( ! picturesRead.contains(justReadAsynchron)) {
				picturesRead.add(justReadAsynchron);
			}
			justReadAsynchron = null;
		}
		
		// get the next from prefetch list
		Iterator it = picturesPrefetch.iterator();
		while (it.hasNext()) {
			PM_Picture picture = (PM_Picture)it.next();	
			if (picture.hasImageOriginal()) {
				// I have already the image
				picturesRead.add(picture);
				it.remove();
				continue;
			}
			justReadAsynchron = picture;
			imageOriginalReadThread.setPicture(picture);
			it.remove();	
			return;	// now you can read	 
		}
 
		return;  // nothing to read
		 
		
 	}
	
	 
	/**
	 * I don't need the image.
	 */
	private void removeImage(PM_Picture picture) {

		if (picture.hasImageOriginal()) {
			picture.getImageOriginal().flush();
			picture.setImageOriginal(null);
		}
		
	}

	 
	/**
	 * Wait until the picture is read asynchronous.
	 * 
	 * I need this image.
	 */
	private void waitUntilRead() {	
		int anz = 0;
		while (imageOriginalReadThread.isAktiv()) {
			anz++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}	
		if ( ! picturesRead.contains(justReadAsynchron)) {
			picturesRead.add(justReadAsynchron);
		}
		justReadAsynchron = null;
	}
	
 

 
	/**
	 * Read the image synchron. 
	 * 
	 * I need the image at once.
	 */
	private void readSynchron(PM_Picture picture) {
		try {
			Image image = Toolkit.getDefaultToolkit().getImage(
					picture.getFileOriginal().getPath());
			picture.setImageOriginal(image);
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(image, 0);

			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
			picture.setImageOriginal(null); // error
		}
	}	
   
	 
}
