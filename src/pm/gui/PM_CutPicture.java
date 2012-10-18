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

 
import pm.picture.*;
import pm.utilities.*;
 
 
import javax.swing.*;
//import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
 

 
import java.util.Collections;
import java.util.List;

/** Window Bild intern bearbeiten in einem modalen JDialog
 *
 *                          aus   übern   abbr   dreh     l?schen
 *   anzeigen                X                                     (ggf. mit Rahmen OHNE Anfasser)
 *   ausschneiden                          X       X         X     (mit Rahmen UND Anfasser)
 *   cutRec bewegt                  X      X       X         X     (mit Rahmen UND Anfasser)
 *   drehen                         X      X       X         X     (mit Rahmen UND Anfasser)
 *   löschen                 X                                     (ohne Rahmen)
 *   abbr                    X                                     (Rahmen OHNE Anfasser wie VOR ausschn.)
 *   übernehmen              X                                     (Rahmen OHNE Anfasser)
 */
@SuppressWarnings("serial")
public class PM_CutPicture extends PM_CutPictureUI implements PM_Interface {

	 
	
	// AffineTransform wird am Anfang eines Bildaufrufen (next, prev ...) versorgt
	private AffineTransform affineTransform = new AffineTransform();
	private AffineTransform inverseAffineTransform = new AffineTransform();
	private Rectangle2D deviceRectCut = new Rectangle2D.Double();
	private Rectangle2D deviceRectOtherSize = new Rectangle2D.Double();	
	private Rectangle2D deviceRectMax = new Rectangle2D.Double();

	private PM_WindowMain windowMain = null;

	private JPanel picturePanel = null;
	private JPanel lowerAendernPanel = null;

	//==========================================================
	// Konstruktor
	//
	//  Sigleton: wird nur einmal instantiiert
	//==========================================================
	public PM_CutPicture(PM_WindowMain windowMain, final JDialog dialog) {
		this.windowMain = windowMain;
		this.dialog = dialog;

		// ESC abfangen
		final ActionListener listener = new ActionListener() {
			public final void actionPerformed(final ActionEvent e) {
				if (buttonUebernehmen.isEnabled()) {
					warnung();
					return;
				}
				dialog.dispose();
			}
		};
		final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
		dialog.getRootPane().registerKeyboardAction(listener, keyStroke,
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		picturePanel = getPicturePanel();
		lowerAendernPanel = getLowerAendernPanel();

		addMouseListener(new MyMouseAdapter());

		// zusammensetzen  
		setLayout(new BorderLayout());
		add(picturePanel, BorderLayout.CENTER);
		add(lowerAendernPanel, BorderLayout.SOUTH);
		 
		
		// Weiter mit start(). Wird von PM_WindowMain aufgerufen.

		// --------------------------------------------------------
		// Change Listener für message
		// --------------------------------------------------------
		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {				 
				setMsg();
			}			
		};		
		PM_MSG.addChangeListener(msgListener);		
		// jetzt werden erstmalig die Tab Titels gesetzt
		setMsg();
	}
	
	
	private void setMsg() {
		
		 buttonAusschneiden.setText(PM_MSG.getMsg("modIntCut"));
		 buttonUebernehmen.setText(PM_MSG.getMsg("modIntApply"));
		 buttonAbbrechen.setText(PM_MSG.getMsg("modIntQuit"));
		 buttonDrehen.setText(PM_MSG.getMsg("modIntRotate"));
		 buttonLoeschen.setText(PM_MSG.getMsg("modIntDelete"));
		
		 qLabel.setText(PM_MSG.getMsg("category14"));
		
	 
		
		
	}
	 

	
	
	
	
	
	
	
	
	
	
	
	//==========================================================
	// start()
	//
	// Aufruf beim Start einer neuen "Bild intern bearbeiten".
	// (es existiert nur eine Instanz dieser Klasse, die beim
	//  Hochfahren von PM angelegt wird; bei jedem Aufruf von 
	//  "Bild intern bearbeiten" wird lediglich hier start() aufgerufen)
	//==========================================================
	public boolean start(PM_Picture  picture, List<PM_Picture> pictureList) {
	
		 
		if (picture  == null) {
			return false;  // there is no picture 
		}
		 
            
		this.picture  = picture ;
		this.pictureList = pictureList;
		 
		diashowAufrufen = false;
		doZeigenBild();

		return true;
	}
	//==========================================================
	// keyPressedFkt()    
	//
	//  Mode:  DIASHOW_NORMAL
	//         DIASHOW_AUTOM_SEQUENT
	//         DIASHOW_AUTOM_RANDOM
	//==========================================================
	public void keyPressedFkt(int mode) {
        diashowAufrufen = true;
        dialog.dispose();
	}
	//==========================================================
	// Aufrufe nach Beenden des Dialogs vom MainWindow aus:
	// 
	//    getAendernAufrufen()
	//
	//==========================================================
	public boolean getDiaShowAufrufen() {
		return diashowAufrufen;
	}
 

	//======================================================
	//  resizeDiagonal()
	// resizeVertiHorizontal()
	//
	// Wird von PM_Ausschneiden aufgerufen
	//
	// true: Das CutRect wird MIT Anfassers gezeichnet
	//======================================================  
	protected boolean resizeDiagonal() {  
		return !buttonAusschneiden.isEnabled();
	}
	protected boolean resizeVertiHorizontal() {  
		return !buttonAusschneiden.isEnabled();
	}
	protected boolean move() {  
		return !buttonAusschneiden.isEnabled();
	}	
	
	// =====================================================
	// protected: drawCutRectangle()
	//
	// Wird von PM_Ausschneiden aufgerufen wenn das Cut-Rectangle
	// gezeichnet wird.
	// =====================================================   
	protected void drawRectangleResize(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		Rectangle rectCut = PM_UtilsGrafik.rectangle2DToRectangle(getRectangleResize()) ;

		// Wenn das Ratio vom Cut-Rectangle nicht mehr dem Ratio  vom
		// Original-Image ist, dann cut hier gestrichelt darstellen
		double ratioCut = rectCut.getWidth() / rectCut.getHeight();
		double ratioImage = picture.getImageOriginalRatio();
		if (!PM_UtilsGrafik.istSelbeDarstellung(ratioCut, ratioImage)) {
			ratioCut = 1 / ratioCut;
		}
		double diff = Math.abs(ratioImage - ratioCut);
		Stroke strokeOld = g2d.getStroke();
		if (diff > 0.005) {
			g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 3.0f,
					new float[] { 50.0f, 10.0f }, 4.0f));
		}

		g2d.setColor(Color.YELLOW);
		g2d.drawRect(rectCut.x, rectCut.y, rectCut.width, rectCut.height);
		
		g2d.setStroke(strokeOld);
		
		g2d.setColor(Color.RED);
		
		// ---------------------------------------------------------------------
		// ein zweites Rectangle ohne Anfasser zeichnen
		// (nur wenn ein anderes Seitenverhältnis)
		// ---------------------------------------------------------------------
		
		double cutSize  = 0;  // pictureView.getIndexView().getGlobalCutSize();
		if (cutSize != 0) {		
			Rectangle rectCutOtherSize = PM_UtilsGrafik.rectangle2DToRectangle(getRectangleResizeOtherSize()) ;
			g2d.drawRect(rectCutOtherSize.x, rectCutOtherSize.y, rectCutOtherSize.width, rectCutOtherSize.height);		
		}
		
		g2d.setColor(Color.YELLOW);
		
	}

	// =====================================================
	// protected: setRectangleResized(Rectangle)
	//
	// Wird von der Vaterklasse aufgerufen, wenn sich das zu
	// movende und zu resizende Rechteck in der Position und/oder
	// Groesse geaendert hat.
	// =====================================================   
	protected void setRectangleResized(Rectangle2D rectangleResize, AffineTransform at) {
		deviceRectCut = rectangleResize; 
		buttonUebernehmen.setEnabled(true);
		buttonAbbrechen.setEnabled(true);
		
		// ----------------------------------------------------
		// deviceRectOtherSize ermitteln
		// ------------------------------------------------------
		double cutSize  = 0; // pictureView.getIndexView().getGlobalCutSize();	
		if (cutSize == 0) {
			return;
		}
		double x = deviceRectCut.getX();
		double y = deviceRectCut.getY();
		double w = deviceRectCut.getWidth();
		double h = deviceRectCut.getHeight();
		double zoom = w/h;   // das mit den Anfassern
		
		if (!PM_UtilsGrafik.istSelbeDarstellung(zoom,cutSize)) {
			cutSize = 1 / cutSize;
		}
		
		
		
	 
		
		
		deviceRectOtherSize= new Rectangle2D.Double(x,y,w,h/cutSize);	
		

	}	

	// =====================================================
	// protected: getRectangleResize()
	//
	// Von der Vaterklasse wird das zu movende und zu 
	// resizende Rechteck geholt.
	// =====================================================   
	protected Rectangle2D getRectangleResize() {
		return deviceRectCut;
	}
	protected Rectangle2D getRectangleResizeOtherSize() {
		return deviceRectOtherSize;
	}

	// =====================================================
	// protected: getRectangleMax()
	//
	// Von der Vaterklasse wird das max. Rechteck, in dem das zu 
	// movende und zu resizende Rechteck sich befinden darf, geholt.
	// =====================================================   
	protected Rectangle2D getRectangleMax() {
		return deviceRectMax;
	}

	// =====================================================
	// getPicture()
	// =====================================================   
	public PM_Picture getPicture() {
		return picture;
	}
	
 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 
	//================================= PRIVATE ============================================= 

	//======================================================
	//  doAussschneiden()
	//
	//======================================================  
	protected void doAussschneiden() {

		if (deviceRectCut.getWidth() == 0 || deviceRectCut.getWidth() == 0) {
			Rectangle imageOrigRect = new Rectangle(imageOriginal.getWidth(this), imageOriginal
					.getHeight(this));					
			deviceRectCut = affineTransform.createTransformedShape(imageOrigRect).getBounds();
			double ratio = deviceRectCut.getWidth()/deviceRectCut.getHeight();
			deviceRectCut = PM_UtilsGrafik.grow(deviceRectCut, new Point2D.Double(-100, -100/ratio));			 
		}

		disableButtonsAusschneiden(); // Alle Buttons auf false     
		buttonAbbrechen.setEnabled(true);
		buttonDrehen.setEnabled(true);
		buttonLoeschen.setEnabled(true);
		buttonUebernehmen.setEnabled(true);

		picturePanel.paintImmediately(PM_UtilsGrafik.rectangle2DToRectangle(deviceRectMax)); // sonst wird cut nicht gezeichnet

	}

	//======================================================
	//  doUebernehmen()
	//
	// Ein rectangleResize wird in die Metadaten (CUT) uebernommen
	//======================================================  
	protected void doUebernehmen() {
		// inverse Transformation und zur?ck in die Metadaten   	  	  
		Rectangle origRectCut = inverseAffineTransform.createTransformedShape(deviceRectCut).getBounds();
		picture.meta.setCutRectangle(origRectCut);
		windowMain.rereadPictureViewThumbnail(picture);
		// Buttons setzen 
		// (Anfasser werden erst beim erneuten Dr?cken des Ausschneidebuttons sichtbar)
		disableButtonsAusschneiden(); // Alle Buttons auf false
		buttonAusschneiden.setEnabled(true);
		// zeichnen
		paintWindow();
	}

	//======================================================
	//  doAbbrechen()
	//
	// wieder alles neu aufbereiten. Das Cut Rectangle wieder aus den Metadatn holen   
	//======================================================  
	protected void doAbbrechen() {

		deviceRectCut = getOrigDeviceRectCut();
		//  setRectangleContainer();  // alles neu (cut aus Metadaten)

		// Buttons setzen 
		// (Anfasser werden erst beim erneuten Dr?cken des Ausschneidebuttons sichtbar)
		disableButtonsAusschneiden(); // Alle Buttons auf false
		buttonAusschneiden.setEnabled(true);

		// zeichnen
		paintWindow();
	}

	//======================================================
	//  doDrehen()
	//
	//======================================================  
	protected void doDrehen() {

		deviceRectCut = PM_UtilsGrafik.rotateRoundMiddlePoint(deviceRectCut);

		disableButtonsAusschneiden(); // Alle Buttons auf false
		buttonUebernehmen.setEnabled(true);
		buttonAbbrechen.setEnabled(true);
		buttonDrehen.setEnabled(true);
		buttonLoeschen.setEnabled(true);

		// zeichnen
		paintWindow();
	}

	//======================================================
	//  doLoeschenCutRectangle()
	//
	// Loeschen Cut-Rectangle
	//======================================================  
	protected void doLoeschenCutRectangle() {

		picture.meta.setCutRectangle(new Rectangle()); // loeschen	 
		windowMain.rereadPictureViewThumbnail(picture);
		deviceRectCut = getOrigDeviceRectCut();

		// Buttons setzen 
		// (Anfasser werden erst beim erneuten Dr?cken des Ausschneidebuttons sichtbar)
		disableButtonsAusschneiden(); // Alle Buttons auf false
		buttonAusschneiden.setEnabled(true);

		// zeichnen
		paintWindow();
	}

	//======================================================
	//  doZeigenBild
	//
	// (beim Zeigen des ERSTEN Bildes, d.h. beim Aufruf von start())
	//======================================================  
	private void doZeigenBild() {

		readImageOriginal(true); // vorw�rts
		setLowerPanel();		
		paintWindow();
	}

	//======================================================
	//  paintWindow
	//
 
	//======================================================  
	private void paintWindow() {
		picturePanel.repaint();
		lowerAendernPanel.repaint();
	}
	
	//======================================================
	//  doZeigenNextBild
	//
	//======================================================  
	protected void doZeigenNextBild() {

		if (buttonUebernehmen.isEnabled()) {
			warnung();
			return;
		}

		int index = pictureList.indexOf(picture);
		if (index == -1 || index +2 >= pictureList.size()) {
			 
			return  ;  
		}
		picture = pictureList.get(index + 1) ;
		
		
		
		
		
		
//		PM_Picture  pic = pictureViewCollection.nextDarstellen(pictureView.getPicture());
//		if (pic == null) return;

//		pictureView = pictureViewCollection.getPictureView(pic);
//		picture = pic;

		readImageOriginal(true); // vorwärts
		setLowerPanel();
		paintWindow();
	}

	//======================================================
	//  doZeigenPreviousBild
	//
	//======================================================  
	protected void doZeigenPreviousBild() {

		if (buttonUebernehmen.isEnabled()) {
			warnung();
			return;
		}

		int index = pictureList.indexOf(picture);
		if (index <= 0) {
			 
			JOptionPane.showConfirmDialog(this, "Keine weiteren Bilder", "Ende", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return; // kein Bild mehrr
		}
		picture = pictureList.get(index - 1) ;

		
		
		
		
//		PM_Picture  pic = pictureViewCollection.previousDarstellen(pictureView.getPicture());
//		if (pic == null) return;

//		pictureView = pictureViewCollection.getPictureView(pic);
//		picture = pic;

		readImageOriginal(false); // rückwärts
		setLowerPanel();
		paintWindow();
	}

	 

	// *****************************************************************************************************  

	//======================================================
	//  readImageOriginal()
	//
	// Lesen ImageOriginal mit Prefetch weiterer Images
	//
	// prefetchRichtung: true = vorwärts
	//                   false = rückwärts
	//======================================================  
	private void readImageOriginal(boolean prefetchRichtung) {
		//	  picture.meta = picture.getPictureMetadaten();

		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		// ------------------------------------------------------------    
		// Lesen 'picture' sofort und einige im Voraus.     
		// -------------------------------------------------------------
		List<PM_Picture> sofort = Collections.singletonList(picture);
		
		
		 List<PM_Picture> prefetch = PM_WindowSlideshow.getPrefetchList( picture,  pictureList, prefetchRichtung);
			
		
		
	//	List prefetch = pictureViewCollection.getPrefetchList(pictureView, prefetchRichtung);

		PM_Picture.readImageOriginal(sofort, prefetch);
		imageOriginal = picture.getImageOriginal();
		
		disableButtonsAusschneiden();
		buttonAusschneiden.setEnabled(true);

		// --------------------------------------------------------------------------
		// AffineTransform versorgen und deviceRectCut/Max tranformiert erzeugen
		// --------------------------------------------------------------------------
		affineTransform = createAffineTransform();
		try {
			inverseAffineTransform = affineTransform.createInverse();
		} catch (NoninvertibleTransformException e) {
			// Kann keine inverse erzeugen
//			System.out.println("### ERROR ###: Inverse Affine Transform kann nicht erstellet werden. " + e);
			inverseAffineTransform = new AffineTransform();
		}
		deviceRectCut = getOrigDeviceRectCut();
		deviceRectMax = affineTransform.createTransformedShape(new Rectangle(picture.meta.getImageSize()))
				.getBounds();

		// Cursor wieder zurueck !!!!! 
		setCursor(cursor);

		return;

	}

	//======================================================
	//  getOrigDeviceRectCut()
	//
	// cutRect aus Metadaten holen und transformieren
	//======================================================  
	private Rectangle getOrigDeviceRectCut() {
		Rectangle rec = null;
		if (picture.meta.hasCutRectangle()) {
			rec = picture.meta.getCutRectangle();
			rec = affineTransform.createTransformedShape(rec).getBounds();
		} else {
			rec = new Rectangle();
		}

		return rec;
	}

	//==========================================================
	// createAffineTransform()
	//
	// Bevor das Bild das erste Mal gezeichnet wird, wird einmalig
	// hier eine AffineTransform erzeugt und bei allen
	// "Bewegungen" (paintComponent, cut-Rectangle verschieben, inverse Transformation ...)
	// verwendet.
	//==========================================================
	private AffineTransform createAffineTransform() {
		
		Rectangle deviceRect = new Rectangle(picturePanel.getWidth(), picturePanel.getHeight());
		// rotate image and move after rotation
		int drehenMoveX = 0;
		int drehenMoveY = 0;
		// ===============  drehen vorbereiten ===============
		Rectangle imageOrigRect;
		int w = imageOriginal.getWidth(this);
		int h = imageOriginal.getHeight(this);
		switch (picture.meta.getRotation()) {
		case CLOCKWISE_90_DEGREES:  
			imageOrigRect = new Rectangle(h,w);
			drehenMoveX = imageOrigRect.width;
			drehenMoveY = 0;
			break;
		case CLOCKWISE_180_DEGREES:
			imageOrigRect = new Rectangle(h,w);
			drehenMoveX = imageOrigRect.height;
			drehenMoveY = imageOrigRect.width;
			break;
		case CLOCKWISE_270_DEGREES:
			imageOrigRect = new Rectangle(h,w);
			drehenMoveX = 0;
			drehenMoveY = imageOrigRect.height;
			break;
		default:  // CLOCKWISE_0_DEGREES
			imageOrigRect = new Rectangle(w,h);
			drehenMoveX = 0;
			drehenMoveY = 0;
			break;
		}
		
		
 
		// scalen	
		double sc = 0;
		if (PM_UtilsGrafik.getRatio(imageOrigRect) < PM_UtilsGrafik.getRatio(deviceRect)) {
			sc = (double) deviceRect.height / (double) imageOrigRect.height;
		} else {
			sc = (double) deviceRect.width / (double) imageOrigRect.width;
		}

		// ---------------------------------------------------
		// create now affine transformation
		// ---------------------------------------------------
		// in umgekehrter Reihenfolge aufbereiten
		AffineTransform Tx = new AffineTransform();
		// am Ende set !!! (dummy)
		Tx.setToTranslation(0, 0);
		// (3) scalen
		Tx.scale(sc, sc);
		// (2) Verschieben nach drehen (sodass Image im II. Quadranten steht)
		Tx.translate(drehenMoveX, drehenMoveY);
		// (1) drehen
		Tx.rotate(picture.meta.getRotation() * Math.PI / 180.);

		return Tx;
	}

	//==========================================================
	// getPicturePanel
	//
	// In diesem Panel wird das picture dargestellt
	//==========================================================
	private JPanel getPicturePanel() {
		JPanel panel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				if (imageOriginal != null) {

					g2.setClip(deviceRectMax);

					// Image zeichnen
					g2.drawImage(imageOriginal, affineTransform, this);
					 
					g2.setColor(Color.YELLOW);
					drawRectangleResize(g, new Point(this.getX(), this.getY()));

				} // if imageOriginal
			} // paintComponent
			
			
			
			
		}; // new JPanel

		panel.setBackground(Color.LIGHT_GRAY);

		return panel;
	}

	// ======================================================
	//======================================================
	//  Inner Class: MyKeyAdaptergetRotateInt_deprecated
	// (fuer den gesamten Dialog)
	//======================================================  

	// ***********  ACHTUNG: im JDIalog kein KeyAdapter moeglich ???? !!!!!!!!!!!!!

	class MyKeyAdapter extends KeyAdapter {

		public void keyPressed(KeyEvent e) {

//			System.out.println(" ............. Dialog: keyPressed");

			/****
			 if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			 // beenden
			 if ( buttonUebernehmen.isEnabled() ) {
			 warnung();
			 return;
			 } 
			 dialog.dispose();  
			 }      
			 ***/

		}

	}

	//======================================================
	//  Inner Class: MyMouseAdapter
	//
	// Wird in der pictureViewCollection an jedes 
	// PM_PictureView hinzugefuegt
	//======================================================
	//======================================================  
	class MyMouseAdapter extends MouseAdapter {

		// ===========================================================
		// Inner Class MyMouseAdapter:    Konstruktor
		// ============================================================    
		public MyMouseAdapter() {}

		// ===========================================================
		// Inner Class MyMouseAdapter:     mouseClicked(MouseEvent e)  
		// ============================================================
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == 3) {
				// vorwaerts
				doZeigenNextBild();
				return;
			}
			if (e.getButton() == 1) {
				// zurueck
				doZeigenPreviousBild();
				return;
			}

		} // mouseClicked 
	} // MyMouseAdapter   

} // Ende Klasse