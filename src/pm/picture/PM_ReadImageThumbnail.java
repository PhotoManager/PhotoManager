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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.imageio.ImageIO;

 
 

/**  
 * Read a thumbnail. 
 * 
 * A singleton (only one instance) is created.
 *
 */
class PM_ReadImageThumbnail implements PM_Interface {

	
	 
	public static final PM_ReadImageThumbnail INSTANCE = new PM_ReadImageThumbnail();

	//	Private constructor prevents instantiation from other classes
	private PM_ReadImageThumbnail() {};

    /**
     * Get the instance of the singleton.
     * 
     * Only one instance of the class is created.
     *  
     */
	public static PM_ReadImageThumbnail getInstance() {
		return INSTANCE;
	};

	/**
	 * Get a thumbnail.
	 * 
	 * If not available the image is created.
	 */
	synchronized public Image getImage(PM_Picture picture, boolean toImport) {
		File fileOriginal = picture.getFileOriginal();
		File fileThumbnail = PM_Utils.getFileThumbnail(fileOriginal);
		// The thumbnail file is available.	
		if (fileThumbnail.isFile()) {
			return readImageThumbnail(fileThumbnail);
		}
		// The thumb nail file is NOT available.
		if (toImport) {
//			return readThumbAsIcon(picture, fileOriginal, fileThumbnail);
		} 
		return readThumb(picture, fileThumbnail);
	};

	/**
	 * Read the original picture and scale down the thumb
	 */
	private Image readThumb(PM_Picture picture, File fileThumbnail) {
		
		// Now make a thumb nail file and store it on the disk.
		// So you can read it in future from the disk.
		picture.readImageOriginal();
		Image imageOriginal = picture.getImageOriginal();
		Image imageThumbnail = makeThumbnail(imageOriginal);
		if (imageThumbnail == null) {
			return null;
		}
		PM_UtilsGrafik.writeThumbnail(fileThumbnail, imageThumbnail);
  
		setEXIFpictureMetadaten(picture);
		// add high and width from the image-original file.
		picture.meta.setImageSize(PM_UtilsGrafik.getImageSize(imageOriginal));	
		PM_Picture.flushAllImagesOriginal();

		return imageThumbnail;
	}
  
	/**
	 * Read fast an icon as thumbnail
	*/ 
/*
	private Image readThumbAsIcon(PM_Picture picture, File fileOriginal, File fileThumbnail) {
		BufferedImage imageOriginal = null;
		try {
			imageOriginal = ImageIO.read(fileOriginal);
		} catch (IOException e) {
			System.out.println("PM_ReadImageThumbnail: io ERROR");
			return null;
		}
		BufferedImage scaledImg = Scalr.resize(imageOriginal, 400);
		PM_UtilsGrafik.writeThumbnail(fileThumbnail, scaledImg);
		  
		setEXIFpictureMetadaten(picture);
		// add high and width from the image-original file.
		picture.meta.setImageSize(PM_UtilsGrafik.getImageSize(imageOriginal));	
		PM_Picture.flushAllImagesOriginal();

		return scaledImg;

	}
*/
	/**
	 * Read the thumbnail
	 */
	private Image readImageThumbnail(File fileThumbnail) {

		Image image = null;

		try {
			image = Toolkit.getDefaultToolkit().getImage(fileThumbnail.getPath());
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(image, 0);
			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
//			System.out.println("ERROR: InterruptedException beim Lesen thumbnail (" + fileThumbnail.getPath()
//					+ "). " + e);
			return null;
		}

		return image;
	}

	/**
	 * Make (scale down) the thumbnail
	 */
	private Image makeThumbnail(Image imageOriginal) {
		 

		double ratio = (double) imageOriginal.getWidth(null) / (double) imageOriginal.getHeight(null);
		int b = THUMBNAIL.width;
		int h = THUMBNAIL.height;
		if (ratio > 0) {
			b = (int) (h * ratio);
		} else {
			h = (int) (b * ratio);
		}
		
		if (b == 0 || h == 0) {
			System.out.println("ERROR makeThumbnail: width = " + b + ", height = " + h 
					+ ", imageRation = " + ratio);
			System.out.println("  Workaround: width = " + THUMBNAIL.width + ", height = " + THUMBNAIL.height);
			b = THUMBNAIL.width;
			h = THUMBNAIL.height;
		}
		
		return getScaledImage(imageOriginal, b, h);
	}

	/**
	 * Make (scale down) the thumbnail
	 */
	private Image getScaledImage(Image image, int breite, int hoehe) {

		Image scaledImage = image.getScaledInstance(breite, hoehe, Image.SCALE_DEFAULT);

		try {
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(scaledImage, 0);
			mediaTracker.waitForID(0);

		} catch (InterruptedException e) {
			
			return null;
		}
		return scaledImage;
	}

	// =====================================================
	// setEXIFpictureMetadaten()
	//
	// Aus dem Image-Original werden die EXIF-Daten f�r die
	// PM_PictureMetadatenX geholt und eingetragen
	// =====================================================
	public static void setEXIFpictureMetadaten(PM_Picture picture) {

		PM_PictureImageMetadaten imageMetadaten = picture.getImageMetadaten();
		
		// ----------------------------------------------------
		// FujiFilm Makernote:  SequenceNummer --> virtPicture
		// ----------------------------------------------------
		
		
		
		
		// -------------------------------------------------------
		// Date
		// ------------------------------------------------------
		
		String tagDatum = "Date/Time Original";
		String description = "";	
		if (imageMetadaten.hasTag(tagDatum)) {
			description = imageMetadaten.getDescription(tagDatum);
		}

		// ----------------------------------------------------
		// Datum nicht vorhanden oder ung�ltig
		// ----------------------------------------------------		  		  	         
		Date myDate = null;
		if (description.length() == 0 || description.equals("0000:00:00 00:00:00")) {
			//			  System.out.println("......  Datum = " + description + " kann nicht konvertiert werden");
			File f = picture.getFileOriginal();
			Date date = new Date(f.lastModified());
			picture.meta.setDateImport(date);
			picture.meta.setDateCurrent(new Date(date.getTime()));
			return;
		}

		// ----------------------------------------------------
		// g�ltiges Datum gefunden
		// ----------------------------------------------------	        	                   	            	    
		DateFormat df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		try {
			myDate = df.parse(description);
		} catch (ParseException e) {
			//           System.out.println("ParseException fuer Datum = " + description);
			myDate = new Date(System.currentTimeMillis()); // default
		}
		picture.meta.setDateImport(myDate);
		picture.meta.setDateCurrent(new Date(myDate.getTime()));

	}

}
