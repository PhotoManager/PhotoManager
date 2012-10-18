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
package pm.inout;

 

import pm.sequence.*;
import pm.utilities.*;
 

 
import pm.gui.*;
import pm.picture.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

 

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** 
 * Im PM_WindowExport wurde "Bilder verändert exportieren" ausgewählt.
 *
 */
public class PM_ExportChanged extends PM_Export implements
		PM_Interface {

	protected boolean cut = false;
	protected boolean rotateReflected = false;
	protected double ratio = 0;
	protected double resolution = 0;
	protected boolean overscan = false;
	protected int overscanX = 0;
	protected int overscanY = 0;
	protected boolean testOverscan = false;
	protected boolean imageText = false;
	protected Color colorBG = null;
	protected Color colorFG = null;
	protected boolean transparent = false;
	
	 
	public PM_ExportChanged(PM_WindowMain windowMain,
			PM_WindowExport windowExport) {
		super(windowMain, windowExport);
		
		getInitValues();
	}

	 
	
	 
	@Override
	protected boolean writePicture(PM_Picture picture, File fileWrite,
			int bildNr, int bilderGes) {

		

		if (fileWrite.isFile()) {
			return false;
		}		 							
 
		BufferedImage bi = getBufferedImage(picture, bildNr, bilderGes, windowExport.getTextOnPicture());
		if (bi == null) {
			// write out unchanged
			copyFile(picture.getFileOriginal(), fileWrite);
		     
		    return true;
		}

		// -------------------------------------------------------------------------
		// BufferedImage write out
		// -------------------------------------------------------------------------
	
	 
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(fileWrite);
			writer.setOutput(ios);
			writer.write(bi);
		} catch (IOException e2) {
			String fehlerText = "Fehler: beim Schreiben Thumbnail in Datei "
					+ fileWrite.getName();
			System.out.println(fehlerText);
			return false;
		}
	    
 
		bi.flush();
				
		
		System.gc();  // ?????????????????
 

	 

		return true;
 
	}

	 
 
	
	 
	protected void getInitValues() {
		cut = windowExport.getCut();
		rotateReflected = windowExport.getRotate();
		ratio = windowExport.getSeitenVerhaeltniss();
		resolution = windowExport.getResolution();
		overscan = windowExport.getOverscan();
		if (overscan) {
			overscanX = windowExport.getOverscanValueX();
			overscanY = windowExport.getOverscanValueY();
			testOverscan = windowExport.getTestOverscancolor();
		} else {
			overscanX = 0;
			overscanY = 0;
			testOverscan = false;
		}
		imageText = windowExport.getTextOnPicture();
		colorBG = windowExport.getBackgroundColor();
		colorFG = windowExport.getForgroundColor();
		transparent = windowExport.getTransparent();
	}

	
	
	
	private BufferedImage getBufferedImage (PM_Picture picture) {
		// get image
		Image image = null;
		PM_Picture.readImageOriginal(Collections.singletonList(picture),
				new ArrayList<PM_Picture>());
		image = picture.getImageOriginal();
		
		// get Buffered Image
		BufferedImage bufferedImage = PM_UtilsGrafik.getBufferedImage(image);
		
		// flush image
		image.flush();
		
		return bufferedImage;
	}
 
	
	
	
	 

	// ========================================================================
	// getBufferedImage()
	//
	// in folgender Reihenfolge:
	//   (1) aussschneiden
	//   (2) geändertes Seitenverhältnis
	//   (3) drehen/spiegeln
	//   (4) geänderte Auflösung
	//   (5) Text auf das Bild
	//   (6) Overscan
	// ========================================================================
	private BufferedImage getBufferedImage(PM_Picture picture, int bildNr,
			int bilderGes, boolean isTextUnderPicture) {
		 
		
		BufferedImage bufferedImage = null;
		
	 	// --------------------------------------------------------
		// (1) ausschneiden 
		// ---------------------------------------------------------	
		if (cut && picture.meta.hasCutRectangle()) {
			Image image = null;
			PM_Picture.readImageOriginal(Collections.singletonList(picture),
					new ArrayList<PM_Picture>());
			image = picture.getImageOriginal();
			
			Rectangle cutRec = picture.meta.getCutRectangle();

			bufferedImage = PM_UtilsGrafik.getBufferedImage(cutRec.width,
					cutRec.height);
			Graphics2D graphics2D = PM_UtilsGrafik.getGraphics(bufferedImage);
			graphics2D.drawImage(image, 0, 0, cutRec.width, cutRec.height,
					cutRec.x, cutRec.y, cutRec.width + cutRec.x, cutRec.height
							+ cutRec.y, null);
			image.flush();
		}  		 
		 
		
		// --------------------------------------------------------
		// (2) ggf. Seitenverhältnis ändern
		// ---------------------------------------------------------
		
		// --------------------------------------------------------
		// (3) drehen und spiegeln
		// ---------------------------------------------------------	
		int rotation =  picture.meta.getRotation();
		boolean mirror = picture.meta.getMirror();
		if (rotateReflected && (mirror || rotation != CLOCKWISE_0_DEGREES)) {
			if (bufferedImage == null) {
				bufferedImage = getBufferedImage(picture);
			}
			
			// ------------------------------------------------------------------
			// BufferedImage bi vom gespiegelten und gedrehten Image erstellen
			// ------------------------------------------------------------------

			// moveX/moveY ermitteln
			// (da um den Nullpunkt gedreht wird, steht das Bild
			// nicht immer im sichtbaren Bereich. Es muss daher
			// nach dem Drehen um moveX/moveY verschoben werden)
			double w = bufferedImage.getWidth(null);
			double h = bufferedImage.getHeight(null); 
			double moveX = 0;
			double moveY = 0;
			switch (rotation) {
			case CLOCKWISE_90_DEGREES:
				moveX = h;
				break;
			case CLOCKWISE_180_DEGREES:
				moveX = w;
				moveY = h;
				break;
			case CLOCKWISE_270_DEGREES:
				moveY = w;
				break;
			}

			BufferedImage bi = bufferedImage;
			int degrees = CLOCKWISE_0_DEGREES;
			if (rotateReflected) {
				degrees =  picture.meta.getRotation();
			}

			if (degrees == CLOCKWISE_0_DEGREES || degrees == CLOCKWISE_180_DEGREES) {
				bi = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
						BufferedImage.TYPE_INT_RGB);

			} else {
				bi = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(),
						BufferedImage.TYPE_INT_RGB);
			}

			Graphics2D gBi = bi.createGraphics();

			AffineTransform Tx = new AffineTransform();
			// (2) drehen
			if (degrees != CLOCKWISE_0_DEGREES) {
				Tx.translate(moveX, moveY);
				Tx.rotate( picture.meta.getRotation () * Math.PI / 180.);
			}
			// (1) spiegeln
			if (mirror) {
				Tx.translate(bi.getWidth(), 0);
				Tx.scale(-1, 1);
			}
			gBi.drawImage(bufferedImage, Tx, null);

			bufferedImage = bi;
		}
		
		// ---------------------------------------------------------
		// (4) Auflösung ändern
		// --------------------------------------------------------	 
		if (resolution > 0) {
			double width = Math.sqrt(resolution * bufferedImage.getWidth()
					/ bufferedImage.getHeight());
			Image im = PM_UtilsGrafik.getScaledImage(bufferedImage,
					(int) width, (int) (resolution / width));
			bufferedImage = PM_UtilsGrafik.getBufferedImage(im);
			im.flush();
		}
		
		// ---------------------------------------------------------
		// (5) Text auf das Bild
		// --------------------------------------------------------	
		if (imageText) {
			if (bufferedImage == null) {
				bufferedImage = getBufferedImage(picture);
			}
			
			Graphics2D graphics2D = PM_UtilsGrafik.getGraphics(bufferedImage);
			 
			int fH = bufferedImage.getHeight() / 25;
			int b = bufferedImage.getWidth();
			int h = bufferedImage.getHeight();
			Rectangle textRec = new Rectangle( 0, h  - fH, b, fH);
			if (!transparent) {
				graphics2D.setColor(colorBG);  
				graphics2D.fillRect(textRec.x, textRec.y, textRec.width,
						textRec.height);
			}
			graphics2D.setFont(new Font("Arial", Font.BOLD, fH));
			graphics2D.setColor(colorFG);  
 
			String textUnderPic = "";
			if (isTextUnderPicture) {
				textUnderPic = getTextUnterBild(picture, bildNr, bilderGes );		 
			}
			
			graphics2D.drawString(textUnderPic,  0, h);
 
		}
		// ---------------------------------------------------------
		// (6) Overscan
		// --------------------------------------------------------
	 	if (overscan) {
	 		if (bufferedImage == null) {
				bufferedImage = getBufferedImage(picture);
			}
	 		
			int breite = bufferedImage.getWidth(null);
			int hoehe = bufferedImage.getHeight(null);
			double ovB = ((100. + overscanX) / 100.) * breite;
			int ovBreite = (int) ovB;
			double ovH = ((100. + overscanY) / 100.) * hoehe;
			int ovHoehe = (int) ovH;

			BufferedImage bi = PM_UtilsGrafik.getBufferedImage(ovBreite,
					ovHoehe);
			Graphics2D graphics2D = PM_UtilsGrafik.getGraphics(bi);
			Color cc = graphics2D.getColor();
			if (testOverscan) {
				graphics2D.setColor(Color.YELLOW);
			} else {
				graphics2D.setColor(Color.DARK_GRAY);
			}

			graphics2D.fill(new Rectangle(0, 0, ovBreite, ovHoehe));
			graphics2D.setColor(cc);

			int dx = (int) ((ovBreite - breite) / 2.);
			int dy = (int) ((ovHoehe - hoehe) / 2.);
			graphics2D.drawImage(bufferedImage, dx, dy, breite, hoehe, null);		
			
			bufferedImage = bi;
 		}
	 
		

		
		return bufferedImage;

	}
	
	// ======================================================
	// getTextUnterBild()
	// ======================================================
	private String getTextUnterBild(PM_Picture picture, int bildNr,
			int bilderGes) {

		PM_Configuration einStllg = PM_Configuration
				.getInstance();

		String textResult = String.valueOf(bildNr) + "/"
				+ String.valueOf(bilderGes) + " ";

		textResult = addText(picture, textResult, einStllg.getSlideshowText1());
		textResult = addText(picture, textResult, einStllg.getSlideshowText2());
		textResult = addText(picture, textResult, einStllg.getSlideshowText3());

		return textResult;

	}

	
	// ======================================================
	// Statuszeile: addText()
	//
	// Hilfsmethode für setTextUnterBild()
	// ======================================================
	private String addText(PM_Picture picture, String text, String type) {
		String neu = "";
		if (type.equals("index1")) {
			neu = picture.meta.getIndex1();
		} else if (type.equals("index2")) {
			neu = picture.meta.getIndex2();
		} else if (type.equals("datum")) {
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");			 
			neu = dateFormat.format(picture.meta.getDateCurrent());
		} else if (type.equals("bemerkung") || type.equals("bemerkungen")) {
			neu = picture.meta.getRemarks();
		} else if (type.equals("name")) {
			File file = picture.meta.getFileOriginal();
			neu = file.getName();
		} else if (type.equals("kategorie")) {
			neu = "K";
			neu += picture.meta.getCategory();
		} else if (type.equals("serie")) {
			// neu = picture.meta.getSequenz();
			String seq = picture.meta.getSequence();
			String[] sa = seq.split(" ");
			for (int i = 0; i < sa.length; i++) {
				String s = sa[i];
				if (s.indexOf("s") < 0)
					continue;
				String[] ss = s.split("_");
				if (ss.length != 2)
					break;
				PM_Sequence sequenz = PM_Sequence.getSequenzFromAll(ss[0]);
				if (sequenz.getType() != SequenceType.BASE)
					continue;
				if (sequenz.getType() != SequenceType.EXTENDED)
					continue;
				neu = sequenz.getPath();

				break;

			}

			// neu += picture.meta.getQs();
		}

		// anhängen wenn vorhanden
		if (neu.length() == 0)
			return text; // nichs anhängen

		if (text.length() == 0)
			return neu;

		return text + " " + neu;
	}

} // Ende Klasse
