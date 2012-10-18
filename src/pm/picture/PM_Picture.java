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

import pm.utilities.*;

import java.lang.ref.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**  
 * Class for a picture with the associated metadata.
 * 
 * 
 */
public class PM_Picture implements PM_Interface {
 
	private Image imageOriginal = null;
	private File fileOriginal = null;
	public final PM_PictureMetadaten meta;
	private static Hashtable<String, WeakReference> allePictureReferences = new Hashtable<String, WeakReference>();
	private static ReferenceQueue<PM_Picture> refQueue = new ReferenceQueue<PM_Picture>();
	private static Thread thread = null;
 	private PM_PictureImageMetadaten pictureImageMetadaten = null;
	
	/**
	 * Factory to create a picture instance.
	 * 
	 * @param pictureKey - idNumber of PM_PictureDirectory and file name
	 * @return the picture instance. null if no PM_PictureDirectory or
	 *   invalid fileOriginal.
	 */
	public static PM_Picture getPicture(String pictureKey) {
		File fileOriginal = PM_MetadataContainer.getInstance().getFileOriginal(pictureKey);
		return getPicture(fileOriginal);
	}

	/**
	 * Factory to create a picture instance.
	 * 
	 * @param fileOriginal -  file of the picture
	 * @return the picture instance. null if no PM_PictureDirectory or
	 *   invalid fileOriginal.
	 */
	public static PM_Picture getPicture(File fileOriginal) {

		PM_Picture picture = null;
		// Create the pictureKey
		String picKey = getPictureKey(fileOriginal);
		if (picKey == null) {
			return null; // error: no PM_PictureDirectory available 
		}
		// return the instance if in cache
		if (allePictureReferences.containsKey(picKey)) {
			WeakReference ref = (WeakReference) allePictureReferences.get(picKey);
			picture = (PM_Picture) ref.get();
			if (picture != null) {
				return picture;  
			}
		}

		// not in the cache. Create the picture instance		
		picture = new PM_Picture(fileOriginal);

		// put into the cache.
		WeakReference<PM_Picture> reference = new WeakReference<PM_Picture>(picture, refQueue);
		allePictureReferences.put(picKey, reference);

		return picture;
	}
 
 
	/**
	 * Create an instance.
	 * 
	 * Don't yet write the instance into the cache (WeakReference)
	 */
	private PM_Picture(File fileOriginal) {
			
		this.fileOriginal = fileOriginal;	 
		meta = new PM_PictureMetadaten(fileOriginal, this);
		 // initialize the metadata instance
		PM_MetadataContainer.getInstance().initMetadaten(this);
		meta.setInit(false);	

		if (thread == null) {
			thread = new Thread() {
				public void run() {
					while (true) {
						try {
							 refQueue.remove();						 							  					 
						} catch (InterruptedException e) {							
							e.printStackTrace();
						}	
					}
				}
			};
			thread.start();
		}
	}

	/** 
	 * Check if there are a PM_Picture instance avaiable.
	 *
	 * All avaiable picture instances are in a cache (WeakReference)
	 */
	public static boolean isPictureInstanceAvailable(File fileOrig) {
		String picKey = getPictureKey(fileOrig);
		if (picKey == null) {
			return false;
		}
		if (allePictureReferences.containsKey(picKey)) {
			WeakReference ref = (WeakReference) allePictureReferences.get(picKey);
			PM_Picture picture = (PM_Picture) ref.get();
			if (picture != null) {
				return true;
			}
		}
		return false;
	}

	 

 
 
	/**
	 * Set the image for this picture.
	 * 
	 * Only allowed from the image reading classes 
	 * (PM_ReadImageOriginal and PM_ReadImageOriginalThread)
	 */
	protected void setImageOriginal(Image imageOriginal) {
		this.imageOriginal = imageOriginal;
	}

	/**
	 * Check if the image for the picture is available.
	 * 
	 */
	public boolean hasImageOriginal() {
		return imageOriginal != null;
	}

	 
	/**
	 * Get the image for this picture.
	 * 
	 * @return the image. null if not available.
	 */
	public Image getImageOriginal() {
		return imageOriginal;
	}

	 
	/**
	 * Set the image for the thumbnail.
	 * 
	 * I.e. if the original picture has changes in the external
	 * image modification program a new image for the thumbnail
	 * is created.
	 * The file for the thumbnail in the metadata has already changed. 
	 * 
	 * @param imageThumbnail - the new image to set.
	 * 
	 * 
	 */
	public void setImageThumbnail(Image imageThumbnail) {
		srImageThumbnail = new SoftReference<Image>(imageThumbnail);
	}

	 
	/**
	 * Get thumbnails image.
	 * <p>
	 * They are managed with SoftReferences.
	 * If in "srImageThumbnail" (the instance of class SoftReference)
	 * return the image.
	 * Otherwise create the image from the thumbnail file.
	 * <p>
	 * If no thumbnail file available create the file from
	 * the original picture file.
	 *  
	 */
	private SoftReference<Image> srImageThumbnail = null;
	synchronized public Image getImageThumbnail(boolean toImport) {
		Image image = (srImageThumbnail == null) ? null : srImageThumbnail.get();
		if (image == null) {
			image = PM_ReadImageThumbnail.getInstance().getImage(this, toImport);
			if (image == null) {
				return null;
			}
			srImageThumbnail = new SoftReference<Image>(image);
		}
		return image;
	}

 
	
	/**
	 * Check if the thumbnails image  is avaiable. 
	 */
	public boolean hasImageThumbnail() {
		Image image = (srImageThumbnail == null) ? null : srImageThumbnail.get();		
		return image != null;
	}

	 
	/**
	 * Read the picture image synchron.
	 * 
	 * The image for this picture reads synchronous and stored into
	 * the instance variable 'imageOriginal'.
	 * Stop all asynchronous image reading and delete all other picture images.
	 * After reading only the image for this picture is available.
	 */
	public void readImageOriginal() {
		List<PM_Picture> directly = new ArrayList<PM_Picture>();
		directly.add(this);
		PM_ReadImageOriginal.getInstance().readImages(directly, new ArrayList<PM_Picture>());
	}

	/**
	 * Read the images of a picture list synchronous and start reading asynchronous.
	 * 
	 * The asynchronous reading of other pictures disabled and picture images
	 * not in the lists deletes.
	 * I.e. after return the images of the list 'directly' are
	 * read and can be get via the method 'getImageOriginal'.
	 * The images of the list 'prefetch' reads asynchronous.
	 * No other images are avaiable or reading.
	 *    
	 * 
	 * @param directly - all images of this pictures reads synchronously.
	 * @param prefetch - starts to read the images of the pictures asynchronous.
	 */	
	static public void readImageOriginal(List<PM_Picture> directly, List<PM_Picture> prefetch) {
		PM_ReadImageOriginal.getInstance().readImages(directly, prefetch);
	}

	/**
	 * Stop all asynchronous image reading and delete all other picture images.
	 *
	 * No other images are avaiable or reading.
	 */
	static public void flushAllImagesOriginal() {
		PM_ReadImageOriginal.getInstance().readImages(new ArrayList<PM_Picture>(), new ArrayList<PM_Picture>());
	}

	 
	/**
	 * Get the EXIF metadata for the  picture file.
	 */
	public PM_PictureImageMetadaten getImageMetadaten() {
		if (pictureImageMetadaten != null) {
			return pictureImageMetadaten;
		}
		
		PM_ReadImageMetadaten imageMetadatenRead = new PM_ReadImageMetadaten();
		pictureImageMetadaten = imageMetadatenRead.readMetadaten(getFileOriginal());
		return pictureImageMetadaten;
	}
 
	 
	/**
	 * Get the original file for this picture.
	 */
	public File getFileOriginal() {
		return fileOriginal;
	}

	 
	 
	 
	/**
	 * Get the ratio  of the picture image.
	 * 
	 * The ratio is stored in the xml-index file.
	 * It is the quotient width/hight.
	 */
	public double getImageOriginalRatio() {		 	
		Dimension imageSize = meta.getImageSize();		
		return   (double)imageSize.width /   (double)imageSize.height;
	}
 

	 
 
	/**
	 * Get the unique pictureKey for a picture file.
	 * 
	 * The pictureKey is the idNumber of the associated 
	 * PM_PictureDirectory instance followed by an underscore
	 * and the picture file name.
	 * 
	 */
	public static String getPictureKey(File fileOriginal) {
		int fileNumber = PM_MetadataContainer.getInstance().getNumberIndexFile(fileOriginal);
		if (fileNumber < 0) {
			return null;
		}
		return Integer.toString(fileNumber) + "_" + fileOriginal.getName();
	}
	 
	public String getPictureKey() {
		return  getPictureKey(fileOriginal);
	}
	
}
