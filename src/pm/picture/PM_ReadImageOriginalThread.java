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
 
/**
 * Read the picture images in a thread asynchronous.
 * 
 * A singleton (only one instance) is created.
 * Start a never ending thread.
 *
 */
public class PM_ReadImageOriginalThread  extends Thread {

	private PM_ReadImageOriginal  imageOriginalRead;
	
	private PM_Picture picture = null;
    private MediaTracker mediaTracker = null;
    private Image image = null;
	
	/**
	 * Create the singleton instance.
	 * 
	 * @param imageOriginalRead - this instance know what to read
	 */
	public  PM_ReadImageOriginalThread(PM_ReadImageOriginal  imageOriginalRead) {
		this.imageOriginalRead = imageOriginalRead;
	};

 
	/**
	 * Check if just reading a picture.
	 */
	public  boolean isAktiv() {
		return picture != null;
	}
	
	
	/**
	 * Now you can start to read.
	 *
	 */
	public void startToRead() {
		synchronized (this) {			
			 notifyAll();
		}	
	}
	
	/**
	 * Stop to read.
	 *
	 */
	public  void stopLesen() {
		 if ( !isAktiv())  {
			 return;  // I am not aktive
		 }
		 if (mediaTracker != null) {
			 mediaTracker.removeImage(image);
		 }	 
	}	

    /**
     * Now I can read.
     * 
     * Need to prevent a deadlock
     */
	public  void setPicture(PM_Picture picture) {
		this.picture = picture;
	}
	
 
	/**
	 * Start the never ending thread.
	 * 
	 * The instance PM_ReadImageOriginal call start.
	 */
	public  void run() { 
 		
		while (true) {
		    picture = null;
			while (picture == null) {
				// loop until there is something to read
				imageOriginalRead.getNextToRead(); // get with 'setPicture' 
				if (picture != null)  {
					break; // break the loop: there is something to read
				}
				try {
					synchronized (this) {
				       wait(); // nothing to read
					}
				} catch (InterruptedException e) {}
			}		     
		    
		    try {    
		      Image image = Toolkit.getDefaultToolkit().getImage(picture.getFileOriginal().getPath());
		      picture.setImageOriginal(image);
		      mediaTracker = new MediaTracker(new Container());
		      mediaTracker.addImage(image, 0);
		      mediaTracker.waitForID(0); 
		    } catch (InterruptedException e) {
              if (image != null) image.flush();
		      picture.setImageOriginal(null);  // cannot read	       
		    }  	    
		} // while (true)  
	}
	
	
	
}

