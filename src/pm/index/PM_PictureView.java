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
package pm.index;
 
 
import pm.dragndrop.*; 
import pm.gui.*;
import pm.picture.*; 
import pm.sequence.PM_Sequence;
import pm.utilities.*; 
import pm.utilities.PM_Interface.SequenceType;

import javax.swing.*;

import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*; 
import java.util.List;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * Class of the thumbnail.
 *  
 * The thumbnails displayed in the index view.
 * <p>
 * Every index view creates its own PM_PictureView instances.
 * I.e. no PM_PictureView instance belongs to more than one index views.
 *  
 */
@SuppressWarnings("serial")
public class PM_PictureView extends PM_PictureViewDragAndDrop implements PM_Interface,
		MouseListener  , FocusListener   {

	  
	private static AlphaComposite compositeK4 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	private static BasicStroke strokeK4 = new BasicStroke(6.0f);
	private static Color colorK4 = Color.red;
	

	//  values for the thumb nail
	static public final int THUMBNAIL_BORDER = 6;  
	static public final int THUMBNAIL_TEXT = 18;  

	 
	/**
	 * Constructor.
	 */
	public PM_PictureView(PM_Picture picture, PM_WindowMain windowMain,
			  PM_Index  index ) {
		super(picture, windowMain, index);
		 
 		addMouseListener(this);
 		addFocusListener(this);
		
//		setToolTips();
		setKeyBindings();	
	}
	
	/**
	 * Return true if picture is selected
	 */
	private boolean isSelected() {
		return index.controller.isPictureSelected(picture);
	}
	
	/**
	 * Rotate the thumbnail.
	 * 
	 * Key "l" --> left
	 * Key "r" --> right
	 */
	private void keyPressedRotate(Rotate richtung) {
		if (!isSelected()) { 
			return;
		}
		int rotation = PM_Utils.getNextRotation (picture.meta.getRotation(), richtung);
		picture.meta.setRotation(rotation); 	
		this.repaint();		 
	}
	
	/**
	 * Mirror the thumbnail.
	 * 
	 * Key "s" mirrot the thumbnail
	 *  
	 */
	private void keyPressedMirror(int key) {
		if (!isSelected()) { 
			return;
		}
		 // TODO  Mirror the thumbnail

		this.repaint();
	}
	
	/**
	 * Next or previous thumbnail
	 *  
	 *  Arrow right --> next thumbnail
	 *  Arrow left --> previous thumbnail
	 */
	private void keyPressedArrow(int keyCode) {
		index.controller.selectNextPictureView(getPicture(), keyCode);
	}
	
	/**
	 * Key 'Enter' pressed.
	 *
	 */
	private void keyEnterPressed() {	
		 
		if (index.isLeft()) {
			windowMain.getWindowRightSelected().appendPicture(picture);	
		}
		index.controller.selectNextPictureView(getPicture(), KeyEvent.VK_RIGHT);	
	}
	
	/**
	 * Change Category.
	 * 
	 * Key 1,2,3, or 4 pressed.
	 * Set the category to respective key value.
	 */
	private void keyPressedQs(String qs) {	
		if (!isSelected()) { 
			return;
		}
		picture.meta.setCategory(qs);		
		this.repaint();
	}	
	
	
	

	/**
	 * Set the tooltips for the thumbnail.
	 *
	 */
	private void setToolTips() {
		File file = picture.getFileOriginal();
		Date date = picture.meta.getDateCurrent();
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); 	
		String txt = dateFormat.format(date) + "/ K" + picture.meta.getCategory()
				+ "/" + file.getPath()
		// + NL + metadaten.getIndex()
		// + NL + metadaten.getBemerkungen()
		;
		setToolTipText(txt);
	}

	/**
	 * Paint the thumbnail and the helper lines for auto scrolling.
	 */
	@Override
	public void paintComponent(Graphics g1) {
		Graphics2D g2d = (Graphics2D)g1;
		super.paintComponent(g2d);
		
		// get the size to paint.
		Dimension pictureViewSize = getPreferredSize();
		int w = pictureViewSize.width - THUMBNAIL_BORDER - THUMBNAIL_BORDER;
		Rectangle thumbnailImageSize = new Rectangle(THUMBNAIL_BORDER,
				THUMBNAIL_BORDER, w, w);	
		
		// get image thumb nail
		Image imageThumbnail  = getThumbnail();
		
		// paint the thumb nail
		if (imageThumbnail != null) {
			paintBackground(g2d, pictureViewSize);
			paintThumbnail(g2d, imageThumbnail, pictureViewSize, thumbnailImageSize);			
			paintBorderAndMarks(g2d, pictureViewSize);
			if (picture.meta.getCategory() == QS_4) {
				paintK4(g2d, pictureViewSize);
			}
		} else {
			paintColoredRectangle(g2d, pictureViewSize);
		}
		paintTextUnderThumbnail(g2d, pictureViewSize);
	 
		// paint helper lines for auto scrolling.
		// (if needed on top and bottom of the index view)
		Insets insets = index.controller.getAutoscrollInsets();
		JPanel client = index.controller.getClient();
		int top = insets.top;
		int bottom = client.getHeight() - insets.bottom;
		Rectangle b = getBounds();
		if (top - b.y > 0) {

			g2d.setColor(Color.RED);
			g2d.drawLine(0, top - b.y, b.width, top - b.y);
		}
		int diff = b.y + b.height - bottom;
		if (diff > 0) {

			g2d.setColor(Color.RED);
			g2d.drawLine(0, b.height - diff, b.width, b.height - diff);
		}

		 
	}
	
	/**
	 * Paint a red cross if K4.
	 */
	private void paintK4(Graphics2D g2d, Dimension pictureViewSize) {
		Composite composite  = g2d.getComposite();
		Color color = g2d.getColor();
		Stroke stroke  = g2d.getStroke(); 
		
		g2d.setComposite(compositeK4);
		g2d.setColor(colorK4);
		g2d.setStroke(strokeK4);
		
		g2d.drawLine(0,0, pictureViewSize.width, pictureViewSize.height);
		g2d.drawLine(0, pictureViewSize.height, pictureViewSize.width,  0);
		
		g2d.setStroke(stroke);
		g2d.setColor(color);		
		g2d.setComposite(composite);
	}
	
	/**
	 * Get thumb nail for painting.
	 */
	private Image getThumbnail() {
		if (index.controller.isPictureClosed(picture)  || picture.meta.isInvalid()) {
			return null; // don't paint the thumb nail
		}
		if (picture.hasImageThumbnail()) {
			return picture.getImageThumbnail(false);
		}
		return null;		
	}

	/**
	 * Paint a colored rectangle.
	 * 
	 * There is not a valid thumb nail. 
	 * Paint a colored rectangle instead of the thumb nail.
	 */
	private void paintColoredRectangle(Graphics2D g2d, Dimension pictureViewSize) {
		Color color = g2d.getColor();
		if (picture.meta.isInvalid()) {
			g2d.setColor(Color.RED);
		} else {
			g2d.setColor(Color.WHITE);
		}

		g2d.drawRect(0, 0, (int) pictureViewSize.getWidth() - THUMBNAIL_BORDER,
				(int) pictureViewSize.getWidth() - THUMBNAIL_BORDER);
		g2d.fillRect(0, 0, (int) pictureViewSize.getWidth() - THUMBNAIL_BORDER,
				(int) pictureViewSize.getWidth() - THUMBNAIL_BORDER);
		g2d.setColor(color);
		
	}
 	
	
	/**
	 * Paint background if selected or have focus.
	 */
	private void paintBackground(Graphics2D g2d,Dimension pictureViewSize) {
		// set the focus if selected.
		if (index.controller.isPictureSelected(picture)) {

			Color focusColor = PM_WindowBase.COLOR_ENABLED_SEL;
			Component focusOwner = windowMain.getFocusOwner();
			if (focusOwner == this || focusOwner == index.controller.indexView) {
				focusColor = PM_WindowBase.COLOR_BG_FOCUS;
			}
			g2d.setColor(focusColor);
			g2d.drawRect(0, 0, (int) pictureViewSize.getWidth(),
					(int) pictureViewSize.getHeight());
			g2d.fillRect(0, 0, (int) pictureViewSize.getWidth(),
					(int) pictureViewSize.getHeight());
		}
	}
	
	/**
	 * Paint the thumb nail.
	 *  
	 */
	private void paintThumbnail(Graphics2D g2d, Image imageThumbnail,
			Dimension pictureViewSize, Rectangle thumbnailImageSize) {	

		// now I can print
		double zoom = thumbnailImageSize.getWidth()
				/ Math.max(imageThumbnail.getWidth(null), imageThumbnail
						.getHeight(null));
		Rectangle cut = null;
		Dimension cutSize = picture.meta.getImageSize(); // getImageOriginalSize();
		if (picture.meta.hasCutRectangle()
				&& index.controller.getAusschneiden()) {
			cut = picture.meta.getCutRectangle();
		}
		// jetzt ist das Bild im bufferedImage, wenn vorhanden mit dem
		// cut-rectangel
		BufferedImage bufferedImage = PM_UtilsGrafik.getBufferedImage(
				imageThumbnail, cut, cutSize, zoom);

		// Jetzt die affine transformation vorbeiten:
		// >>zum drehen, spiegeln und verschieben<<
		int moveX = 0; // zum verschieben in den Mittelpunkt
		int moveY = 0; // zum verschieben in den Mittelpunkt
		double thumbWidth = imageThumbnail.getWidth(null);
		double thumbHeight = imageThumbnail.getHeight(null);
		double offset = thumbWidth * zoom * (thumbHeight / thumbWidth) / 4;
		if (index.controller.getDrehenSpiegeln()) {
			int rotation = picture.meta.getRotation();
			if ((rotation == CLOCKWISE_0_DEGREES || rotation == CLOCKWISE_180_DEGREES)
					&& (thumbWidth > thumbHeight)) {
				moveY = (int) offset;
			} else {
				moveX = (int) offset;
			}
		} else {
			// KEIN drehen und KEIN spiegeln
			if (thumbWidth > thumbHeight) {
				moveY = (int) offset;
			} else {
				moveX = (int) offset;
			}
		}

		// in umgekehrter Reihenfolge aufbereiten
		AffineTransform Tx = new AffineTransform();

		// zum Schluss in den Mittelpunkt verschieben
		Tx.translate(THUMBNAIL_BORDER + moveX, THUMBNAIL_BORDER + moveY);
		if (index.controller.getDrehenSpiegeln()) {
			// nach dem Drehen wieder in den richtigen (sichtbaren)
			// Bereich verschieben (es wurde ja um den Nullpunkt gedreht)

			// nach drehen in den richtigen Quadranten verschieben
			// (es wurde um den Nullpunkt gedreht)
			Point2D.Double point = PM_UtilsGrafik.getMovePoint(picture.meta
					.getRotation(), imageThumbnail.getWidth(null) * zoom,
					imageThumbnail.getHeight(null) * zoom);
			Tx.translate(point.x, point.y);
			// drehen
			Tx.rotate(picture.meta.getRotation() * Math.PI / 180.);
			// spiegeln
			if (picture.meta.getMirror()) {
				Tx.translate(imageThumbnail.getWidth(null) * zoom, 0);
				Tx.scale(-1, 1);
			}
		}

		// jetzt die affine transformation anwenden
		g2d.drawImage(bufferedImage, Tx, this);

		// ggf. Datum in das Bild schreiben
		if (index.controller.getPaintBildText()) {
			g2d.setFont(new Font("Arial", Font.BOLD, 12));
			g2d.setColor(Color.YELLOW);
			// Date date = picture.meta.getDate();
			// DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			Rectangle source = new Rectangle(imageThumbnail.getWidth(null),
					imageThumbnail.getHeight(null));
			Rectangle dest = PM_Utils.getDestinationRectangle(source,
					thumbnailImageSize);
			String datum = "Bildtext"; // dateFormat.format(date);
			g2d.drawString(datum, dest.x + 2, dest.y + dest.height - 2);
		}
 

	}
	
	/**
	 * Paint border and miscellaneous marks.
	 */
	private void paintBorderAndMarks(Graphics2D g2d, Dimension pictureViewSize) {
		// The index view is a left side mainwindow:
		// Mark this thumb if it is displayed also in the
		// right side mainwindow.
		boolean rightAndLeftSide = false;

		if (index.isLeft()) {
			PM_Index ivRight = windowMain.getIndexViewWindowRight();
			if (ivRight != null) {
				if (ivRight.data.hasPicture(picture)
						&& !ivRight.controller.isPictureClosed(picture)) {
					rightAndLeftSide = true;
				}
			} else {
				// right side mainwindow can only be 'one' picture.
				rightAndLeftSide = (picture == windowMain
						.getWindowRightSelected().getPictureSelected());
			}
		}

		// mark bold black dashed line if in right and left is the same picture
		if (rightAndLeftSide) {
			Stroke strokeOld = g2d.getStroke();
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 3.0f, new float[] { 50.0f, 10.0f },
					4.0f));
			g2d.drawRect(2, 2, (int) pictureViewSize.getWidth() - 3,
					(int) pictureViewSize.getWidth() - 3);
			g2d.setStroke(strokeOld);
		}

		markBaseSequence(picture, g2d, pictureViewSize);
		markVirtualPicture(picture, g2d, pictureViewSize);

		// if b sequence then print sequence number on the top left side
		// (only if subwindow b-sequences)
		if (index.isLeft() == false
				&& windowMain.getWindowRightSelected() instanceof PM_WindowSequence
				&& windowMain.getWindowRechts().getWindowSequence()
						.isBaseSelected() && picture.meta.hasBaseSequence()) {
			String sNumber = picture.meta.getBaseSequenceName();
			g2d.setFont(new Font("Arial", Font.BOLD, 12));
			g2d.setColor(Color.BLUE);

			int x = pictureViewSize.width - sNumber.length() - 10;

			g2d.drawString(sNumber, x, 12 + 2);
		}
	}

	/** 
	 * Paint text under the thumb nail.
	 */
	private void paintTextUnderThumbnail(Graphics2D g2d, Dimension pictureViewSize) {
		// Text under the thumb.
		g2d.setFont(new Font("Arial", Font.BOLD, 12));
		if (picture.meta.hasDateChanged()
				&& index.controller.getMarkImportDatum()) {
			g2d.setColor(Color.RED);
		} else {
			g2d.setColor(Color.BLUE);
		}
		g2d.drawString(getTextUnderThumbnail(picture), THUMBNAIL_BORDER,
				(int) pictureViewSize.getHeight() - THUMBNAIL_BORDER);
	}
	
	/**
	 * mark a virtual picture (mini sequence)
	 */
	private void  markVirtualPicture(PM_Picture picture, Graphics2D g2d, Dimension pictureViewSize) {
		 
	    if (!picture.meta.hasMiniSequence()) {
	    	return;  // not a mini sequence
	    }	
	    Color  color= g2d.getColor();		
		Rectangle rec = new Rectangle(2,2,12,12);
		// compressed
		String text = "m" + picture.meta.getMiniSequenceNumber() 
					+ "/" + picture.meta.getCurrentMiniNumber();
	    if (picture.meta.isMiniSequenceX()) {
	    	// paint a filled circel
	    	g2d.setColor(Color.GREEN);
	    	g2d.fillOval(rec.x, rec.y, rec.width, rec.height);
	    	
	    } else {	    	
	    	// paint a circle
	    	g2d.setColor(Color.BLUE);
	    	g2d.drawOval(rec.x, rec.y, rec.width, rec.height);
	    }
	    // draw text
	    g2d.setColor(Color.BLUE);
	    g2d.drawString(text, rec.x + rec.width + 2, rec.y + rec.height);	
	    
	    // return with original color
		g2d.setColor(color);
	}
	
	/**
	 * 
	 */
	private void markBaseSequence(PM_Picture picture, Graphics2D g2d,
			Dimension pictureViewSize) {
		PM_WindowBase wb = windowMain.getWindowRightSelected();

		if (index.isLeft() == false
				&& wb instanceof PM_WindowSequence) {
			// it is a b-sequence subwindow
			PM_WindowSequence windowSequence = (PM_WindowSequence) wb; 
			if (windowSequence.getSelectedSubWindow().isPictureInAnotherBaseSequence(picture)) {
				// In PM_WindowSequence is a base sequence present.
				// Check only if the picture is NOT associated to the sequence
				// RED bold
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(3.0f));
				g2d.drawRect(1, 1, (int) pictureViewSize.getWidth(),
						(int) pictureViewSize.getWidth());
				return;
			}
		}
		 
		// small red rectangle:  not b and not x
		boolean base = picture.meta.hasBaseSequence();
		boolean extended = picture.meta.hasExtendedSequence();
		if (base || extended) {
			return;
		}	
		g2d.setColor(Color.RED);
		g2d.drawRect(0, 0, (int) pictureViewSize.getWidth(),
				(int) pictureViewSize.getWidth());
		return;
	}
	
	
	/**
	 * Set the preferred size.
	 *  
	 */
	public void setPictureViewPreferredSize(Dimension preferredSize) {
		setPreferredSize(preferredSize);
	}
	 
	/**
	 * Returns the associated index view.
	 * @return
	 */
	public PM_Index  getIndexView() {
		return index ;
	}
	 
	/**
	 * Moused pressed on a thumbnail.
	 */
	public void mousePressed(MouseEvent e) {
		if (picture.meta.isInvalid()) {
			return;  // The picture is invalid (mark to delete)
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			// left button
			if (picture.getImageThumbnail(false) != null) {
				if (picture.getImageThumbnail(false) != null) {		
					if (index.controller.mousePressed(getPicture(), e.isControlDown(), e.isShiftDown())) {
						index.controller.indexView.requestFocus(getPicture());
						index.controller.indexView.refreshViewport();
						index.controller.indexView.printNumberSelectedPicturesOnStatusLine( );
					}					 
				}
			}
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 1) {
			doPopUpMiddleClick(this, picture, e); 
		}
		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			// right mouse: context Pop-Up Menu
			doPopUpContextMenu(this, picture, e);
		} 
	} 

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if (picture.meta.isInvalid()) {
			return; // The picture is invalid (marked to delete)
		}	  
		if (e.getButton() == MouseEvent.BUTTON1) {
			// left mouse
			if (picture.getImageThumbnail(false) != null) {		
				if (index.controller.mouseReleased(getPicture(), e.isControlDown(), e.isShiftDown())) {
					index.controller.indexView.requestFocus(getPicture());
					index.controller.indexView.refreshViewport();
					index.controller.indexView.printNumberSelectedPicturesOnStatusLine( );
				}					 
			}
		}
	}

	
	public PM_Picture getPicture() {
		return picture;
	}

	 
 
	private String getTextUnderThumbnail(PM_Picture picture) {
 
		Point p = index.controller.getIndexSize(picture);
		int size = p.y; 
		int ind  = p.x;  
 
		String indexSize = Integer.toString(ind +1) + "/" + Integer.toString(size);  

	 
		// paint the resolution
		if (index.controller.getTextAufloesung()) {	
			DecimalFormat df = new DecimalFormat("0.0");
			long res  = picture.meta.getResolution();
			File file = picture.meta.getFileOriginal();
			Dimension imageSize = picture.meta.getImageSize();
			String wH = " " +  imageSize.width   + "x" + imageSize.height;
			String resolution = "(" + df.format(res/1000000.)  + " MPix";
			String length = " " + df.format(file.length()/1000.)  + " KB)";		
			
			return indexSize + wH + resolution + length;
		}
		
		// Kennzeichen ">" wenn Bild gedreht
		String flip = "";
		if (picture.meta.getMirror()) {
			flip  = ">";
		}
		
		// Temporaeres Bild
		String temp = "";
//		if (PM_IndexView_deprecated.hasTempPictureList(picture)) {
//			temp = "t";			
//		}

		// Kennzeichen "Bild mit externem Programm bearbeitet" vor den Text
		String bearbeitet = "";
		if (picture.meta.getModified()) {
			bearbeitet = "x";
		}  
		// Kennzeichen "s" wenn Bild in einer Serie
		String sequenz = "";
		if (picture.meta.hasClosedSequence()) {
			sequenz = "s";
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");			 
		String datum = dateFormat.format(picture.meta.getDateCurrent());		
		
		return   flip + temp + bearbeitet + indexSize + "-K"
				+ picture.meta.getCategory() + "-" + datum;

	}
 
 
	/**
	 *   Bildnummer(Datum): 1/4 (14.08.2012 12:33)
	 *   Sequenz b0:
	 *   Sequenz s1:
	 *   Index1:
	 *   Index2:
	 *   File:
	 *   Aufloesung:
	 *   Make/Model:
	 * 	
	 */
	private void doPopUpMiddleClick(final PM_PictureView pictureView,
			final PM_Picture picture, MouseEvent e) {

		if (index.controller.isImport()) {
			return;
		}
		// -- edit the pop up menu ----
		JPopupMenu popup = new JPopupMenu();
		
		// Bildnummer(Datum): 1/4 (14.08.2012 12:33)
		Point p = index.controller.getIndexSize(picture);
		int size = p.y;
		int ind = p.x;
		String picNumber = Integer.toString(ind + 1) + "/"
				+ Integer.toString(size);
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");			 
		String date = dateFormat.format(picture.meta.getDateCurrent());
		addMenuItem(popup, "Datum: "  + date + " Bildnummer: " + picNumber);
		
		// -- sequences ---
		List<PM_Sequence> liste = new ArrayList<PM_Sequence>();
		String sequence = picture.meta.getSequence();
		String[] sa = sequence.split(" ");
		for (int i = 0; i < sa.length; i++) {
			String s = sa[i];
			String[] ss = s.split("_");
			if (ss.length != 2)
				break;
			PM_Sequence sequenz = PM_Sequence.getSequenzFromAll(ss[0]);
			liste.add(sequenz);
		}
		popup.addSeparator();
		if (liste.isEmpty()) {
			// NO sequences
			addMenuItem(popup, "Sequenz: keine");		
		} else {
			// show all sequences in a pop up list
			for (PM_Sequence seq : liste) {
				String text = "Sequenz " + seq.getShortName() + ": " 
			 				+ seq.getLongName() 
							+ " (" + seq.getPath() + ")";				
				ActionListenerSequence als = new ActionListenerSequence(seq, this);
				if (als.canDisplaySequence()) {
					JMenuItem mi = new JMenuItem(text); 
					mi.addActionListener(als);	
					popup.add(mi);		
				} else {
					addMenuItem(popup, text);
				}						
			}
		}
		popup.addSeparator();
		// Index1 and Index2 
		String index1 = picture.meta.getIndex1();
		String index2 = picture.meta.getIndex2();
		addMenuItem(popup, "Index1: " + index1);
		addMenuItem(popup, "Index2: " + index2);
		popup.addSeparator();
		// File path
		addMenuItem(popup, "File: " + picture.getFileOriginal().getPath());

		// paint the resolution		 
		Dimension imageSize = picture.meta.getImageSize();
		String wH = " " + imageSize.width + " x " + imageSize.height;		
		addMenuItem(popup, "Resolution: " + wH );
 
		// EXIF: Model an Make
		PM_PictureImageMetadaten pim = picture.getImageMetadaten();
		addMenuItem(popup, "Make/Model: " + pim.getTagValue("Exif", 271) + "/" + pim.getTagValue("Exif", 272));
		 
		// ---- po up the menu --------
		popup.show(e.getComponent(), e.getX(), e.getY());
		
	}

	private void addMenuItem(JPopupMenu popup, String item) {
		JMenuItem menuItem  = new JMenuItem(item);
		menuItem.setEnabled(false);
		popup.add(menuItem);
	}
	
	
	class ActionListenerSequence implements ActionListener {
		private PM_Sequence seq;
		private SequenceType type;
		private Component component;
		public ActionListenerSequence(PM_Sequence seq, Component component) {
			this.seq = seq;
			this.component = component;
			type = seq.getType();
		}	
		
		public boolean canDisplaySequence() {
			return type == SequenceType.BASE || type == SequenceType.EXTENDED; 
		}
		
		public void displaySequence() {
			// check if seq already displayed
			PM_WindowRightTabbedPane wr = windowMain.getWindowRechts();
			PM_WindowSequence winSeq = wr.getWindowSequence();
			String ret = "";
			if (type == SequenceType.BASE) {
				ret = winSeq.displaySequenceBase(seq);
			}
			if (type == SequenceType.EXTENDED) {
				ret = winSeq.displaySequenceExtended(seq);
			}
			if (ret.length() != 0) {
				JOptionPane.showConfirmDialog(component,
						ret, "Hinweis",
						JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
			} else {
				// change to window sequence (the subwindow of window sequence
				// is already visible)
				wr.changeToWindowSequence();
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			displaySequence( );
		}
	}
 
	 
	
	// ----------------  Context Menu -------------------------------
	private void doPopUpContextMenu(final PM_PictureView pictureView,
			final PM_Picture picture, MouseEvent e) {

		if (index.controller.isImport()) {
			return;
		}
		
		JPopupMenu popup = new JPopupMenu();
		/**
		 * Close all the selected thumbs.
		 * 
		 * Paint all the selected thumbs as white rectangle.
		 */
		JMenuItem menuItemLoeschen = new JMenuItem(PM_MSG.getMsg("indViewPopUpDel"));
		ActionListener alLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				index.controller.closePicture();
			}
		};
		menuItemLoeschen.addActionListener(alLoeschen);
//		if (indexpopUpLoeschen) {
			popup.add(menuItemLoeschen);
//		}
 
		 
		/**
		 * Remove all closed pictures.
		 * 
		 * Closed pictures are painted as white rectangle.
		 */
		JMenuItem menuItemLoeschenRetour = new JMenuItem(PM_MSG.getMsg("indViewPopUpSus"));
		ActionListener alLoeschenRetour = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				index.controller.openPicture();
			}
		};
		menuItemLoeschenRetour.addActionListener(alLoeschenRetour);
//		if (popUpLoeschenAufheben) {
			popup.add(menuItemLoeschenRetour);
//		}


		// ----------------------------------------------
		// Menue: slideshow
		// ----------------------------------------------
		JMenuItem menuItemDiaShow = new JMenuItem("?Diashow");
		menuItemDiaShow.setText(PM_MSG.getMsg("diashow"));
		ActionListener alDiaShow = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				if (pictureView != null) {
					windowMain.doDiaShow(pictureView.getPicture(),
							 index.controller.getPictureListDisplayed(), DIASHOW_NORMAL);
		 
				}
			}
		};
		menuItemDiaShow.addActionListener(alDiaShow);
//		if (popUpDiaShow)
			popup.add(menuItemDiaShow);
		// ----------------------------------------------
		// Menue: modify internal (cut)
		// ----------------------------------------------
		JMenuItem menuItemZeigen = new JMenuItem(PM_MSG.getMsg("indViewPopUpCut"));
		ActionListener alZeigen = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				if (pictureView != null) {
					windowMain.doBildZeigenOriginal(
							pictureView.getPicture(),  index.controller.getPictureListDisplayed());
				}
			}
		};
		menuItemZeigen.addActionListener(alZeigen);
//		if (popUpAendern)
			popup.add(menuItemZeigen);

		// ----------------------------------------------
		// Menue: modify external
		// ----------------------------------------------
		JMenuItem menuItemExtern = new JMenuItem(PM_MSG.getMsg("indViewPopUpExtern"));
		ActionListener alExtern = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pictureView != null) {
					windowMain.doExternBearbeiten(pictureView.getPicture());
				}
			}
		};
		menuItemExtern.addActionListener(alExtern);
//		if (popUpExternBearbeiten)
			popup.add(menuItemExtern);
	 
		
		 
		// ------------------------------------------------------
		// Menu: Hide/Show (toggle) a mini sequence
		// ------------------------------------------------------
		JMenuItem menuMini = new JMenuItem("Hide/Show this mini sequence");
		ActionListener alMini = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				if (pictureView != null) {
					index.data.hideShowMiniSequence(picture);
				}				 
			}
		};
		menuMini.addActionListener(alMini);
		if (picture.meta.hasMiniSequence()) {
			popup.add(menuMini);
		}
		
		// -------------- Menue aufpoppen ----------------------
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Set key bindings 
	 */
	private void setKeyBindings() {
 
		Action aCtrlA = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {			
				index.controller.setAllPicturesSelected();
				index.controller.repaintViewport(); 
				index.controller.indexView.printNumberSelectedPicturesOnStatusLine( );
			}
		};
		
		
		Action aVK_LEFT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedArrow(KeyEvent.VK_LEFT);
			}
		};
		Action aVK_RIGHT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedArrow(KeyEvent.VK_RIGHT);
			}
		};
		
		Action aVK_UP = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedArrow(KeyEvent.VK_UP);
			}
		};
		Action aVK_DOWN = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedArrow(KeyEvent.VK_DOWN);
			}
		};
		
		
		Action aVK_ENTER = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyEnterPressed();
			}
		};
		
		
		
		Action aVK_Q1 = new AbstractAction() {		 
			public void actionPerformed(ActionEvent e) {
				keyPressedQs("1");
			}
		};
		Action aVK_Q2 = new AbstractAction() {		 
			public void actionPerformed(ActionEvent e) {
				keyPressedQs("2");
			}
		};
		Action aVK_Q3 = new AbstractAction() {			 
			public void actionPerformed(ActionEvent e) {
				keyPressedQs("3");
			}
		};
		Action aVK_Q4 = new AbstractAction() {			 
			public void actionPerformed(ActionEvent e) {
				keyPressedQs("4");
			}
		};		
		Action aVK_L = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
					keyPressedRotate(Rotate.LEFT);
			}
		};
		Action aVK_R = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (index.controller.isPictureSelected(picture)) { 
					keyPressedRotate(Rotate.RIGHT);
				}
			}
		};	 
		Action aVK_S = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedMirror(KeyEvent.VK_S);
			}
		};
						
		InputMap imap =  getInputMap();
		ActionMap map =  getActionMap();

		imap.put(KeyStroke.getKeyStroke("ctrl A"), "ctrl-A");
		map.put("ctrl-A", aCtrlA);
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "VK_LEFT");
		map.put("VK_LEFT", aVK_LEFT);
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "VK_RIGHT");
		map.put("VK_RIGHT", aVK_RIGHT);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "VK_UP");
		map.put("VK_UP", aVK_UP);
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "VK_DOWN");
		map.put("VK_DOWN", aVK_DOWN);
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
		map.put("VK_ENTER", aVK_ENTER);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "VK_1");
		map.put("VK_1", aVK_Q1);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "VK_2");
		map.put("VK_2", aVK_Q2);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "VK_3");
		map.put("VK_3", aVK_Q3);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "VK_4");
		map.put("VK_4", aVK_Q4);
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "VK_L");
		map.put("VK_L", aVK_L);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "VK_R");
		map.put("VK_R", aVK_R);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "VK_S");
		map.put("VK_S", aVK_S);
	}

	 
	public void focusGained(FocusEvent arg0) {
	}

  
	public void focusLost(FocusEvent arg0) {	
	} 

}