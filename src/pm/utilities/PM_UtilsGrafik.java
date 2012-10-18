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
package pm.utilities;


import pm.gui.PM_WindowMain;
import pm.gui.PM_WindowRightTabbedPane;
import pm.index.PM_Index;
import pm.picture.*;
 

//import java.util.*;
import java.io.*;
import java.util.List;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
 
import javax.swing.*;
 

 

public class PM_UtilsGrafik implements PM_Interface {

	// =========================================================================
	// RotateLeft()
	// 
	// =========================================================================
	public static Image rotate(Image image, int rotation) {
		switch (rotation) {
		case CLOCKWISE_0_DEGREES:
			return image;
		case CLOCKWISE_90_DEGREES:
			return RotateRight(getBufferedImage(image));
		case CLOCKWISE_180_DEGREES:
			return RotateDown(getBufferedImage(image));
		case CLOCKWISE_270_DEGREES:
			return RotateLeft(getBufferedImage(image));
		}
		return image;
	}

	// =========================================================================
	// spiegeln()
	// 
	// =========================================================================
	public static Image spiegeln(Image image) {
		return Spiegeln(getBufferedImage(image));
	}

	// =========================================================================
	// BufferedImage()
	// 
	// =========================================================================
	public static BufferedImage getBufferedImage(Image image) {
		BufferedImage bi = new BufferedImage((int) image.getWidth(null),
				(int) image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D gBi = bi.createGraphics();
		gBi.drawImage(image, 0, 0, (int) image.getWidth(null), (int) image
				.getHeight(null), null);
		return bi;
	}

	// =========================================================================
	// RotateLeft()
	// RotateDown() 180 Grad
	// RotateRight()
	// Spiegeln()
	// =========================================================================
	private static BufferedImage RotateLeft(BufferedImage bi) {
		AffineTransform afLeft = AffineTransform.getRotateInstance(Math
				.toRadians(270));
		afLeft.translate(bi.getWidth() * -1, 0);
		AffineTransformOp lOp = new AffineTransformOp(afLeft, null);

		BufferedImage dstbi = lOp.filter(bi, null);

		return dstbi;
	}

	private static BufferedImage RotateDown(BufferedImage bi) {
		AffineTransform afDown = AffineTransform.getRotateInstance(Math
				.toRadians(180));
		afDown.translate(bi.getWidth() * -1, bi.getHeight() * -1);
		AffineTransformOp rOp = new AffineTransformOp(afDown, null);

		BufferedImage dstbi = rOp.filter(bi, null);

		return dstbi;
	}

	private static BufferedImage RotateRight(BufferedImage bi) {
		AffineTransform afRight = AffineTransform.getRotateInstance(Math
				.toRadians(90));
		afRight.translate(0, bi.getHeight() * -1);
		AffineTransformOp rOp = new AffineTransformOp(afRight, null);

		BufferedImage dstbi = rOp.filter(bi, null);

		return dstbi;
	}

	private static BufferedImage Spiegeln(BufferedImage bi) {
		AffineTransform afRight = AffineTransform.getRotateInstance(Math
				.toRadians(95));
		afRight.translate(0, bi.getHeight() * -1);
		AffineTransformOp rOp = new AffineTransformOp(afRight, null);

		BufferedImage dstbi = rOp.filter(bi, null);

		return dstbi;

	}

	
	
	
	
	
	
	// =========================================================================
	// getImageSize()
	// getImageRectangle()
	// =========================================================================
	public static Dimension getImageSize(Image image) {
		return new Dimension((int) image.getWidth(null), (int) image
				.getHeight(null));
	}

	public static Rectangle getImageRectangle(Image image) {
		return new Rectangle(getImageSize(image));

	}

	// =========================================================================
	// getRatio()
	// =========================================================================
	public static double getRatio(Dimension dim) {
		return dim.getWidth() / dim.getHeight();
	}

	public static double getRatio(Rectangle rec) {
		return rec.getWidth() / rec.getHeight();
	}

	public static double getRatio(Image image) {
		return (double) image.getWidth(null) / (double) image.getHeight(null);
	}

	// =========================================================================
	// rectangle2DToRectangle()
	// =========================================================================
	public static Rectangle rectangle2DToRectangle(Rectangle2D rec) {
		int x = (int) Math.round(rec.getX());
		int y = (int) Math.round(rec.getY());
		int b = (int) Math.round(rec.getWidth());
		int h = (int) Math.round(rec.getHeight());
		return new Rectangle(x, y, b, h);
	}

	// =========================================================================
	// getInset()
	// 
	// =========================================================================
	public static Rectangle getInset(Rectangle outerSize, double innerRatio) {
		double outerRatio = getRatio(outerSize);
		int breite = 0;
		int hoehe = 0;
		int x = outerSize.x;
		int y = outerSize.y;
		if (outerRatio < innerRatio) {
			breite = outerSize.width;
			hoehe = (int) ((double) breite / innerRatio);
			y = y + (int) (((double) outerSize.height - (double) hoehe) / 2);
		} else {
			hoehe = outerSize.height;
			breite = (int) ((double) hoehe * innerRatio);
			x = x + (int) (((double) outerSize.width - (double) breite) / 2);
		}

		Rectangle rec = new Rectangle(x, y, breite, hoehe);
		// System.out.println("getInset. outerRatio = " + outerRatio + ",
		// innerRatio = " + innerRatio );
		// System.out.println("getInset. outerSize = " + outerSize + ", return =
		// " + rec);

		return rec;
	}

	// =========================================================================
	// rectangleEinpassen()
	// 
	// "recEinpassen" soll in "recMax" passen.
	// "recEinpassen" so verkleinern, das es passt.
	//
	// recturn: ggf. verkleinertes Rectangle
	// =========================================================================
	public static Rectangle rectangleEinpassen(Rectangle recMax,
			Rectangle recEinpassen) {
		if (recMax.contains(recEinpassen))
			return recEinpassen;
		Point mPoint = getMiddlePoint(recEinpassen);
		if (!recMax.contains(mPoint))
			return recEinpassen;

		// TO DO Fehlt noch !!!!!!!!!

		return recEinpassen;
	}

	// =========================================================================
	// getMiddlePoint()
	// =========================================================================
	public static Point getMiddlePoint(Rectangle rec) {
		return new Point(rec.x + (int) (rec.getWidth() / 2), rec.y
				+ (int) (rec.getHeight() / 2));
	}

	// =========================================================================
	// grow()
	//
	//  
	// =========================================================================
	public static Rectangle2D grow(Rectangle2D rec, Point2D grow) {

		double x = rec.getX();
		double y = rec.getY();
		double w = rec.getWidth();
		double h = rec.getHeight();

		x = x - grow.getX();
		y = y - grow.getY();
		w = w + grow.getX() * 2;
		h = h + grow.getY() * 2;

		return new Rectangle2D.Double(x, y, w, h);
	}

	// =========================================================================
	// rotateRoundMiddlePoint()
	//
	// Rectangle um dem Mittelpunkt drehen
	// =========================================================================
	public static Rectangle2D rotateRoundMiddlePoint(Rectangle2D rectangle) {
		Rectangle2D rec = new Rectangle2D.Double();
		rec.setRect(rectangle); // Kopieren
		double x = (rec.getWidth() / 2);
		double y = (rec.getHeight() / 2);
		// auf Mittelpunkt "schrumpfen"
		rec = grow(rec, new Point2D.Double(-x, -y));
		// jetzt "wachsen"
		rec = grow(rec, new Point2D.Double(y, x));
		return rec;
	}

	// =========================================================================
	// istSelbeDarstellung()
	//
	// pruefen, ob die beiden Rectangles dieselber Darstellung
	// (Portrait/Landscape)
	// haben
	// =========================================================================
	public static boolean istSelbeDarstellung(double ratio1, Rectangle rec2) {
		return istSelbeDarstellung(ratio1, getRatio(rec2));
	}

	public static boolean istSelbeDarstellung(Rectangle rec1, double ratio2) {
		return istSelbeDarstellung(getRatio(rec1), ratio2);
	}

	public static boolean istSelbeDarstellung(double ratio1, double ratio2) {
		return (ratio1 >= 1 && ratio2 >= 1) || (ratio1 < 1 && ratio2 < 1);
	}

	public static boolean istSelbeDarstellung(Rectangle rec1, Rectangle rec2) {
		return istSelbeDarstellung(getRatio(rec1), getRatio(rec2));
	}

	public static boolean istSelbeDarstellung(Rectangle2D rec1, Rectangle2D rec2) {
		double r1 = rec1.getWidth() / rec1.getHeight();
		double r2 = rec2.getWidth() / rec2.getHeight();
		return istSelbeDarstellung(r1, r2);
	}

	// =========================================================================
	// rotateRectangle()
	//
	// Achtung:
	// wenn hier Aenderungen, dann auch in Klasse "PM_RectangleContainer"
	// aendern.
	//
	// =========================================================================
	public static Rectangle2D rotateRectangle(Rectangle2D rectangle) {
		rectangle.setRect(rectangle.getY(), rectangle.getX(), rectangle
				.getHeight(), rectangle.getWidth());
		return rectangle;
	}

	// =========================================================================
	// rotateRectangle()
	//
	// drehen eines Rechteckes um einen Punkt (Links oder rechts)
	// =========================================================================
	public static Rectangle rotateRectangle(Rectangle recIn, Rectangle cutIn,
			int rotation) {
		int pointXY = 0;
		if (rotation == CLOCKWISE_90_DEGREES) { // Links
			pointXY = (int) (recIn.getWidth() / 2);
		} else {
			pointXY = (int) (recIn.getHeight() / 2);
		}
		return PM_UtilsGrafik.rotateRectangle(cutIn,
				new Point(pointXY, pointXY), rotation);

	}

	public static Rectangle rotateRectangle(Rectangle rec, Point p,
			int rotation) {

		int neuX = 0;
		int neuY = 0;

		// wenn Du das nicht verstehts, mach Dir eine Zeichnung !!!!
		if (rotation == CLOCKWISE_270_DEGREES) {
			neuX = p.x + (rec.y - p.y);
			neuY = p.y + (p.x - rec.x - rec.width);
		} else {
			neuX = p.x + (p.y - rec.y - rec.height);
			neuY = p.y - (p.x - rec.x);
		}

		return new Rectangle(neuX, neuY, rec.height, rec.width);
	}

	 

	// =========================================================================
	// changeRatio()
	//
	// recToChange soll, bezogen auf dem Mittelpunkt, im Seitenverhaeltnis
	// (ratio)
	// veraendert werden.
	// Es darf aber nicht groesser als recMax werden.
	//
	// =========================================================================
	public static Rectangle changeRatio(Rectangle recToChange,
			Rectangle recMax, double ratio) {
		Rectangle r = new Rectangle(recToChange);
		int hHalbe = (int) (recToChange.getHeight() / 2);
		double d = getRatio(recToChange) / ratio;
		r.grow(0, -hHalbe);
		r.grow(0, (int) (hHalbe * d));

		Rectangle rr = new Rectangle(recToChange);
		int wHalbe = (int) (recToChange.getWidth() / 2);
		rr.grow(0, -wHalbe);
		rr.grow(0, (int) (wHalbe * d));

		// nun noch in recMax einpassen
		// System.out.println("+++ recMax = " + recMax + ", rec, das passen muss
		// = " + r);

		if (recMax.contains(r))
			return r;

		// return PM_Utils.getDestinationRectangle(r, recMax);

		return PM_Utils.getDestinationRectangle(r, recToChange);
	}

	// =========================================================================
	// resizeRectangle()
	//
	//
	// =========================================================================
	public static Rectangle resizeRectangle(Rectangle rectangle,
			double zoomAlt, double zoomNeu) {
		// das Rectangle was geresized wird
		// Achtung:
		// wenn hier Aenderungen, dann auch in Klasse "RectangleContainer"
		// aendern.
		rectangle.setRect(rectangle.getX() * zoomNeu / zoomAlt, rectangle
				.getY()
				* zoomNeu / zoomAlt, rectangle.getWidth() * zoomNeu / zoomAlt,
				rectangle.getHeight() * zoomNeu / zoomAlt);
		return rectangle;
	}

 

	// ========================================================
	// Class Methods: WriteThumbnail
	// ========================================================
	public static void writeThumbnail(File fileThumbnail, Image imageThumbnail) {

		 
		int hoehe = imageThumbnail.getHeight(null);
		int breite = imageThumbnail.getWidth(null);

		// Der Pfad von FileOut muss vorhanden sein
		File dirFileOut = new File(fileThumbnail.getParent());
		try {
			// System.out.println("schreibenFileOut . Path = " +
			// dirFileOut.getPath());
			dirFileOut.mkdir();
		} catch (Exception e) {
		}
		 
		// -------------------------------------------------------------------------
		// BufferedImage aufbereiten
		// -------------------------------------------------------------------------

		BufferedImage bufferedImage = null;
		Graphics2D graphics2D = null;

		// ----------------------------------------------------
		// zeichnen Bild
		// ----------------------------------------------------
		bufferedImage = getBufferedImage(breite, hoehe);
		graphics2D = getGraphics(bufferedImage);
		graphics2D.drawImage(imageThumbnail, 0, 0, breite, hoehe, null);

		
		
		// -------------------------------------------------------------------------
		// "bufferedImage" schreiben
		// -------------------------------------------------------------------------
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(fileThumbnail);
			writer.setOutput(ios);
			writer.write(bufferedImage);
		} catch (IOException e2) {
			String fehlerText = "Fehler: beim Schreiben Thumbnail in Datei "
					+ fileThumbnail.getName();
			System.out.println(fehlerText);
			return;
		}
	    
 
		bufferedImage.flush();
		
		
	 
		
		
/*
		// -------------------------------------------------------------------------
		// "bufferedImage" schreiben
		// -------------------------------------------------------------------------
		try {
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(fileThumbnail.getPath()));

			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder
					.getDefaultJPEGEncodeParam(bufferedImage);

			quality = Math.max(0, Math.min(quality, 100));
			param.setQuality((float) quality / 100.0f, false);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(bufferedImage);
		} catch (FileNotFoundException e) {
			String fehlerText = "Fehler: Ausgabedatei unzulaessig: "
					+ fileThumbnail.getPath();
			System.out.println(fehlerText);
			return;
		} catch (IOException e) {
			String fehlerText = "Fehler: beim Schreiben Thumbnail in Datei "
					+ fileThumbnail.getName();
			System.out.println(fehlerText);
			return;
		}
		
*/		
		
		return;

		
		
	}

	// =========================================================================
	// getBufferedImage()
	//
	//
	// =========================================================================
	public static BufferedImage getBufferedImage(int breite, int hoehe) {

		BufferedImage bufferedImage = new BufferedImage(breite, hoehe,
				BufferedImage.TYPE_INT_RGB);

		return bufferedImage;
	}

	// =========================================================================
	// getGraphics()
	//
	//
	// =========================================================================
	public static Graphics2D getGraphics(BufferedImage bufferedImage) {

		Graphics2D graphics2D = bufferedImage.createGraphics();

		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		return graphics2D;
	}

	// =========================================================================
	// getBildAusschnitt()
	//
	//
	// =========================================================================
	public static Image getBildAusschnitt(Image image, Rectangle recCut) {
		// Ausschneiden
		Image croppedImage = null;
		CropImageFilter croppFilter = new CropImageFilter(recCut.x, recCut.y,
				recCut.width, recCut.height);
		FilteredImageSource imFi = new FilteredImageSource(image.getSource(),
				croppFilter);
		croppedImage = Toolkit.getDefaultToolkit().createImage(imFi);
		try {
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(croppedImage, 0);
			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
//			String fehlerText = "Fehler beim Erzeugen croppedImage (InterruptedException)";
			// System.out.println(fehlerText);
			return null;
		}

		return croppedImage;
	}

	// =========================================================================
	// rotateCutRectangle()
	//
	// Das Cutrectangle so drehen, wie das image Original gedreht ist.
	//
	// Eingabe:
	// rotation: OBEN, LINKS, RECHTS (aus den Metadaten)
	// recCut: Cut Rectangle aus dem Metadaten
	// recImage: Image Rectangle des nicht gedrehten Images
	//
	// return: entsprechend gedrehtes rectangle
	// =========================================================================
	public static Rectangle rotateCutRectangle(int rotation,
			Rectangle recCut, Rectangle recImage) {

		Rectangle recGedreht = null;

		int imageHeigth = recImage.height;
		int imageWidth = recImage.width;

		switch (rotation) {
		case CLOCKWISE_90_DEGREES: // RECHTS:
			recGedreht = new Rectangle(imageHeigth - recCut.height - recCut.y,
					recCut.x, recCut.height, recCut.width);
			break;
		case CLOCKWISE_270_DEGREES: // LINKS:
			recGedreht = new Rectangle(recCut.y, imageWidth - recCut.x
					- recCut.width, recCut.height, recCut.width);
			break;
		case CLOCKWISE_180_DEGREES: // Unten:
			recGedreht = new Rectangle(imageWidth - recCut.width - recCut.x,
					imageHeigth - recCut.height - recCut.y, recCut.width,
					recCut.height);
			break;
		default:
			// nicht gedreht
			recGedreht = recCut;
		}

		return recGedreht;

	}

	
	
	
	
	// =====================================================
	// verkleinern()
	// =====================================================
	public static Image getScaledImage(Image image, int breite, int hoehe) {

		if (breite == 0 || hoehe == 0)  {
			return image;
		}
		
		Image scaledImage = image.getScaledInstance(breite, hoehe,
				Image.SCALE_DEFAULT);

		try {
			MediaTracker mediaTracker = new MediaTracker(new Container());
			mediaTracker.addImage(scaledImage, 0);
			mediaTracker.waitForID(0);

		} catch (InterruptedException e) {
	//		String fehlerText = "Fehler beim Erzeugen scaledImage (InterruptedException)";
			// System.out.println(fehlerText);
			return null;
		}
		return scaledImage;
	}

	// ========================================================
	// Class Methods: MakeThumbnail
	// ========================================================
	public static Image makeThumbnail(Image imageOriginal) {
		// PM_LoadImage loadImage = new PM_LoadImage();

		double ratio = (double) imageOriginal.getWidth(null)
				/ (double) imageOriginal.getHeight(null);
		int b = THUMBNAIL.width;
		int h = THUMBNAIL.height;
		if (ratio > 0) {
			b = (int) (h * ratio);
		} else {
			h = (int) (b * ratio);
		}
		return getScaledImage(imageOriginal, b, h);
	}

	 

 
	// ==================================================================================
	// getBufferedImage()
	//
	// mit Cutrectangle und zoom
	// =================================================================================
	static public BufferedImage getBufferedImage(Image image, Rectangle cut,
			Dimension cutSize, double zoom) {

		BufferedImage bufferedImage = new BufferedImage((int) (image
				.getWidth(null) * zoom), (int) (image.getHeight(null) * zoom),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gBi = bufferedImage.createGraphics();

		AffineTransform T = new AffineTransform();
		T.scale(zoom, zoom);
		gBi.drawImage(image, T, null);

		// Jetzt Cutrectangle Zeichnen
		
		if (cut == null) {
			return bufferedImage;
		}
		
		Graphics2D big2 = PM_UtilsGrafik.getGraphics(bufferedImage);

		
		
		double scX = image.getWidth(null) / cutSize.getWidth();
		double scY = image.getHeight(null) / cutSize.getHeight();
		AffineTransform Tx = new AffineTransform();
		Tx.scale(scX, scY);
		Tx.scale(zoom, zoom);
		Color c = big2.getColor();
		big2.setTransform(Tx);
		big2.setColor(Color.YELLOW);
		big2.drawRect(cut.x, cut.y, cut.width, cut.height);
		big2.setColor(c);

		return bufferedImage;
	}
	
	
	
	static public Point2D.Double getMovePoint(int rotation, double w, double h) {

		// moveX/moveY ermitteln
		// (da um den Nullpunkt gedreht wird, steht das Bild
		// nicht immer im sichtbaren Bereich. Es muss daher
		// nach dem Drehen um moveX/moveY verschoben werden)
		 
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

		return new Point2D.Double(moveX, moveY);
	}
	
	
	
	
	
	
	
	
	
} // Ende Klasse
