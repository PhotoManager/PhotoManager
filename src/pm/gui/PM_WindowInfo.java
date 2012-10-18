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
package pm.gui;

 
import pm.utilities.*;
import pm.index.PM_Index;
import pm.index.PM_PictureView;
import pm.inout.*;
import pm.picture.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Sub window to display informations of one picture.
 * 
 * You display one picture as thumbnail 
 * and display all the EXIF data of the picture.
 * 
 *  
 * 
 */
@SuppressWarnings("serial")
public class PM_WindowInfo extends PM_WindowBase implements PM_Interface {

	private Image imageThumb = null;
	private Rectangle cutRectangle = null;
	private JPanel windowPicture = null;
	private PM_Picture picture = null;
	private JSplitPane splitPane;
 
	// ************ EXIF *******************
	private JTable exifTable = null;
	private DefaultTableModel tm = new DefaultTableModel();
	private JScrollPane exifTableScrollPane;
	private String lastSelectedKey = "";

	// ==========================================================
	// Konstruktor
	//
	// Wird nicht wieder vernichtet !!!!!
	//
	// Wird ein Bild angezeigt, so wird "doZeigenBild" aufgerufen
	// ==========================================================
	public PM_WindowInfo() {
		super(null);

	 	
		
		setLayout(new BorderLayout());

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				getPicturePanel(), getExifPanel());
		int location = PM_All_InitValues.getInstance().getValueInt(this,
				"devider");
		if (location == 0) {
			location = 150;
		}
			
		splitPane.setDividerLocation(location);
		add(splitPane);

	 

	}
	
	
	
	// =====================================================================
	// rereadPictureViewThumbnail()
	//
	// das Bild hat sich ggf. ge�ndert.
	// Thumb neu einlesen.
	// ======================================================================
	public void rereadPictureViewThumbnail(PM_Picture pic ) {	
		if (pic != picture)  {
			return;  // nicht dieses Bild
		}
		setPicture(pic);
	}

	@Override
	public void rereadAllThumbs() {
		// alles l�schen
		setPicture(null);
	
	}
	
	
	// ======================================================
	// close()
	//
	// Ende der Verarbeitung
	// ======================================================
	public void close() {
		PM_All_InitValues.getInstance().putValueInt(this, "devider",
				splitPane.getDividerLocation());
	}

	// ======================================================
	// requestToChange()
	//
	// Aufruf beim Tab-Wechsel: Pruefen, ob Aktivitaeten abgeschlossen.
	// ======================================================
	@Override
	public boolean requestToChange() {
		return true;
	}
	
	// ======================================================
	// getPictureSelected()
	//
	// Wenn rechts ein Window aufliegt, das kein PM_IndexView 
	// hat (z.B. Einzelbild, InfoBild ...), dann hier return
	// mit dem dargestellten PM_Picture.
	// ======================================================
	@Override
	public PM_Picture getPictureSelected() {
		return picture;
	}  
	
	
	
	// ======================================================
	// appendPicture()
	//
	// Ein Bild im Fenster "zeigen" darstellen
	// ======================================================
	@Override
	public boolean appendPicture(PM_Picture  picture) {		 
		setPicture(picture);		 
		return true;
	}

	
	// ======================================================
	// setPicture()
	// ======================================================
	public void setPicture(PM_Picture picture) {
		if (picture == null) {
			// alles l�schen
			this.picture = null;
			imageThumb = null; 
			Vector vec = tm.getDataVector();
			vec.removeAllElements();
			exifFeld.setText("");
	
			windowPicture.repaint();
		    return;
		}
		
		
		
		
		
		
		 this.picture = picture;

		// ----------------------------------------------------------------
		// imageThumb und cutRectangle aufbereiten
		// ---------------------------------------------------------------
		 
		// Ausgangslage !!!!
		imageThumb = picture.getImageThumbnail(false);
		if (picture.meta.hasCutRectangle()) {
			cutRectangle = picture.meta.getCutRectangle();
		} else {
			cutRectangle = null;
		}
		
		
		int rotation = picture.meta.getRotation();
//		private Rectangle getCutRectangle(PM_Picture picture, Image image,Rectangle cutRectangle, Rotation rotation) {	
		cutRectangle = 	getCutRectangle(picture,imageThumb,cutRectangle,rotation);	
		
 
		
		imageThumb = PM_UtilsGrafik.rotate(imageThumb, rotation);

		// --------------------------------------------------------
		//  EXIF-Daten lesen und aufbereiten
		// --------------------------------------------------------
		PM_PictureImageMetadaten imageMetadaten = picture.getImageMetadaten();
		SortedMap sortedMap = imageMetadaten.getMap();
		Set set = sortedMap.entrySet();
		Vector vec = tm.getDataVector();
 		vec.removeAllElements();
		for (Iterator i = set.iterator(); i.hasNext();) {
			Map.Entry me = (Map.Entry) i.next();
			Vector<Object> v = new Vector<Object>();
			v.add(me.getKey());
			v.add(me.getValue());
			vec.add(v);
		}

		if (lastSelectedKey.length() != 0) {
			String val = (String) sortedMap.get(lastSelectedKey);
			if (val != null) {
				String text = lastSelectedKey + ": " + val;
				exifFeld.setText(text);
			} else {
				exifFeld.setText("");
			}
		}

		exifTable.clearSelection();
		exifTable.updateUI();

		// --------------------------------------------------------
		// Bild jetzt zeichnen
		// --------------------------------------------------------
		windowPicture.doLayout();
		windowPicture.repaint();
	}

	
	
	
	// ======================================================
	// getListDivPos()
	// ======================================================
	public int getListDivPos() {

		return 20; // splitListenPane.getDividerLocation();

	}

	// =================================================================
	// ================================================================
	//
	// P R I V A T E
	//
	// =================================================================
	// =================================================================
 
 

	// ==========================================================
	// getExifPanel()
	// ==========================================================
	private JTextField exifFeld;

	private JPanel getExifPanel() {

		// ------------------------------------------------------
		// Selectierter EXIF-Eintrag (mit Doppelclick)
		// ------------------------------------------------------
		JPanel exifFeldPanel = new JPanel();
		exifFeldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// JTextField exifFeldLabel = new JTextField("irgendein Tag");
		// exifFeldPanel.add(exifFeldLabel);
		// exifFeldLabel.setColumns(40);
		exifFeld = new JTextField();
		exifFeldPanel.add(exifFeld);
		exifFeld.setColumns(40);
		// Font auf Bold
		Font font = exifFeld.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		exifFeld.setFont(fontBold);

		// -------------------------------------------------------------
		// Liste aller EXIF-Eintr�ge
		// -------------------------------------------------------------

		exifTable = new JTable();
		exifTable.setModel(tm);
		tm.setColumnCount(2);
		// exifTable.setPreferredScrollableViewportSize(new Dimension(300,300));
		exifTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ListSelectionModel sm = exifTable.getSelectionModel();
		sm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					int ind = exifTable.getSelectedRow();
					if (ind == -1) {
						exifFeld.setText("");
						return; // nichts selectiert
					}
					Vector data = tm.getDataVector();
					Vector v = (Vector) data.elementAt(ind);
					lastSelectedKey = (String) v.elementAt(0);
					String text = lastSelectedKey + ": "
							+ (String) v.elementAt(1);
					exifFeld.setText(text);
				}
			}
		});

		// -------------------------------------------------------------
		// Beide zusammen (Einzelfeld �ber Liste)
		// -------------------------------------------------------------
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(exifFeldPanel, BorderLayout.NORTH);
		exifTableScrollPane = new JScrollPane(exifTable);
		panel.add(exifTableScrollPane, BorderLayout.CENTER);

		return panel;

	}

	// ==========================================================
	// getPicturePanel()
	// ==========================================================

	private JPanel getPicturePanel() {

		// -------------------------------------------------------------
		// das eigentliche Bild
		// -------------------------------------------------------------
		windowPicture = new JPanel() {
			public void paintComponent(Graphics g) {
				doPaintComponent(g);
			}
		};
		windowPicture.setBackground(Color.YELLOW);
		// JScrollPane windowPictureScrollPane = new JScrollPane(windowPicture);

		// -------------------------------------------------------------
		// zusammen im splitpane
		// -------------------------------------------------------------
		// JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// splitListenPane,
		// windowPictureScrollPane);
		// splitPane.setDividerLocation(70); //

		return windowPicture;
	}

	// ======================================================
	// paintComponent
	//
	// Hier wird das Bild gezeichnet
	//
	// Image: imageThumb
	// cutRec: cutRectangle
	// ======================================================
	private void doPaintComponent(Graphics g) {
		super.paintComponent(g);

		if (imageThumb != null) {

			// -----------------------------------------------------------
			// Jetzt das Image zeichnen
			// -----------------------------------------------------------
			g.drawImage(imageThumb, 20, 20, this);

			// ---------------------------------------------------------
			// CutRectangle ausgeben
			// --------------------------------------------------------
			Color color = g.getColor();
			if (cutRectangle != null) {
				g.setColor(Color.WHITE);
				g.drawRect(cutRectangle.x + 20, cutRectangle.y + 20,
						cutRectangle.width, cutRectangle.height);
			}

			// --------------------------------------------------------
			// �ber das Bild den vollst�ndigen Path-Namen ausgeben
			// --------------------------------------------------------
			g.setColor(color);
			Font font = g.getFont();
			Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
			g.setFont(fontBold);
			g.drawString(picture.getFileOriginal().getPath(), 10, 12);

		}
	}

	 
 
	
private Rectangle getCutRectangle(PM_Picture picture, Image image,Rectangle cutRectangle, int rotation) {	
	
		Rectangle rectReturn = null;
		if (cutRectangle != null) {
			rectReturn = new Rectangle(cutRectangle);
		}
	  
		// ----------------------------------------------------------------
		// imageThumb und cutRectangle aufbereiten
		// erst neues cutRect
		// falls cutRect, dieses auf imageThumb reduzieren
		if (rectReturn != null) {
			// dieses jetzt auf Thumbgr��e reduzieren
			Rectangle recThumb = PM_UtilsGrafik.getImageRectangle(image);
			Dimension origSize = picture.meta.getImageSize();//getImageOriginalSize();
			double zoomThumb = origSize.getWidth() / recThumb.getWidth();
			rectReturn = PM_UtilsGrafik.resizeRectangle(rectReturn,
					zoomThumb, 1);
		}

		// imageThumb und cutRectangle jetzt o.k.
		// .. aber noch nicht gedreht !!!!
		
		if (rectReturn != null && rotation != CLOCKWISE_0_DEGREES) {
			// jetzt das cutRect drehen
			Rectangle recThumb = PM_UtilsGrafik.getImageRectangle(image);
			rectReturn = PM_UtilsGrafik.rotateCutRectangle(rotation,
					rectReturn, recThumb);
		}


		return rectReturn;
}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
