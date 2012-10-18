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

 

import pm.sequence.PM_Sequence;
import pm.utilities.*;

import pm.inout.PM_SequencesInout;
import pm.picture.*;
// 

//import java.util.*;
//import java.io.*;
import javax.swing.*;
//import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.Timer;
import java.io.File;
import java.lang.Math;

/** Window zum Zeigen und Ausschneiden in einem modalen JDialog
 *
 * Nur EINE Instanz von PM_WindowDiaShow
 * (wird in PM_WindowMain erzeugt)
 * 
 *
 */
@SuppressWarnings("serial")
public class PM_WindowSlideshow extends JPanel implements PM_Interface {

	
	private boolean tempViewed = false;
	private List<PM_Picture> pictureList = null;
	private PM_Picture picture = null;    // das wird gerade dargestellt
	// Tempor�r
	private List<PM_Picture> tempPictureList; 
	 
	// NICHT Tempor�r
	private PM_Picture pictureOrig = null;    // das wird gerade dargestellt
	private List<PM_Picture> pictureListOrig = null;
	
	
	
	
	private Image imageOriginal = null;
	

	private PM_WindowMain windowMain = null;
	private final JDialog dialog;
//	private PM_PictureView pictureView = null;

	
	
	
	
	
	private JLabel labelAnzahl = null;
	//  private JLabel lesenBildNr = null;

	private JPanel picturePanel = null;
	private JPanel lowerPanel = null;

	private boolean aendernAufrufen = false;

	private JLabel labelBezeichnung = null;
	private JLabel labelTime = null;

	private int fontSize = 30;

	private JLabel bearbeitet = null;

	private Timer timer = null;

	 
	private boolean automatic = false;
	private boolean automaticSequ = false;
	private int timeSeconds = 4;
	private Random random = new Random();

	private JLabel temporare;
	
	//==========================================================
	// Konstruktor
	//==========================================================
	public PM_WindowSlideshow(PM_WindowMain windowMain, final JDialog dialog) {
		this.windowMain = windowMain;
		this.dialog = dialog;

//		tempPictureList = PM_IndexView_deprecated.getTempPictureList();
		tempPictureList = new ArrayList<PM_Picture>();
		
		KeyAdapter keyAdapter = new KeyAdapter() {
			public void  keyPressed(KeyEvent e) {
		
				if (PM_Utils.isTemp(e)) {
					doTemp( ); 
				}
				if (PM_Utils.isTempDelete(e)) {
					doTempDelete( ); 
				}
				if (PM_Utils.isTempViewed(e)) {
					doTempViewed( ); 
				}
				
				
				
			}
		};
		addKeyListener(keyAdapter);
		
		
		
		
		
		// ----  Key - Bindigs ----------------------
	/*	 
		
		Action aVK_F2 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedFktHier(KeyEvent.VK_F2);
			}
		};
		Action aVK_F3 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedFktHier(KeyEvent.VK_F3);
			}
		};
		Action aVK_F4 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedFktHier(KeyEvent.VK_F4);
			}
		};
*/		
		
		Action aVK_UP = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPfeil(KeyEvent.VK_UP);
			}
		};
		Action aVK_DOWN = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPfeil(KeyEvent.VK_DOWN);
			}
		};
		Action aVK_LEFT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPfeil(KeyEvent.VK_LEFT);
			}
		};
		Action aVK_RIGHT = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPfeil(KeyEvent.VK_RIGHT);
			}
		};
		Action aVK_ESCAPE = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedEscape();
			}
		};
		Action aVK_ENTER = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedEnter();
			}
		};
		Action aVK_PLUS = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPlusMinus(KeyEvent.VK_PLUS);
			}
		};
		Action aVK_MINUS = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedPlusMinus(KeyEvent.VK_MINUS);
			}
		};
		Action aVK_K1 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(KeyEvent.VK_1);
			}
		};
		Action aVK_K2 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(KeyEvent.VK_2);
			}
		};
		Action aVK_K3 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(KeyEvent.VK_3);
			}
		};
		Action aVK_K4 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(KeyEvent.VK_4);
			}
		};
		Action aVK_L = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedRotate(Rotate.LEFT);
			}
		};
		Action aVK_R = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedRotate(Rotate.RIGHT);
			}
		};	 
		Action aVK_S = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				keyPressedSpiegeln(KeyEvent.VK_S);
			}
		};
		 

		InputMap imap = this.getInputMap();
		ActionMap map = this.getActionMap();

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "VK_L");
		map.put("VK_L", aVK_L);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "VK_R");
		map.put("VK_R", aVK_R);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "VK_S");
		map.put("VK_S", aVK_S);
		
		
 
		 
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "VK_1");
		map.put("VK_1", aVK_K1);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "VK_2");
		map.put("VK_2", aVK_K2);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "VK_3");
		map.put("VK_3", aVK_K3);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "VK_4");
		map.put("VK_4", aVK_K4);

		 
		
/*		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "VK_F2");
		map.put("VK_F2", aVK_F2);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "VK_F3");
		map.put("VK_F3", aVK_F3);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "VK_F4");
		map.put("VK_F4", aVK_F4);
*/
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "VK_UP");
		map.put("VK_UP", aVK_UP);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "VK_DOWN");
		map.put("VK_DOWN", aVK_DOWN);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "VK_LEFT");
		map.put("VK_LEFT", aVK_LEFT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "VK_RIGHT");
		map.put("VK_RIGHT", aVK_RIGHT);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "VK_ESCAPE");
		map.put("VK_ESCAPE", aVK_ESCAPE);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "VK_ENTER");
		map.put("VK_ENTER", aVK_ENTER);

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "VK_PLUS");
		map.put("VK_PLUS", aVK_PLUS);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "VK_MINUS");
		map.put("VK_MINUS", aVK_MINUS);

		// ---- 
		picturePanel = getPicturePanel();
		addMouseListener(new MyMouseAdapter());

		lowerPanel = getLowerShowPanel();
		// zusammensetzen  
		setLayout(new BorderLayout());
		add(picturePanel, BorderLayout.CENTER);
		add(lowerPanel, BorderLayout.SOUTH);

	}


	//==========================================================
	// keyPressedFkt()    
	//
	//  Mode:  DIASHOW_NORMAL
	//         DIASHOW_AUTOM_SEQUENT
	//         DIASHOW_AUTOM_RANDOM
	//==========================================================
	public void keyPressedFkt(int mode) {
		if (mode == DIASHOW_NORMAL) {
			// in den normalen Modus
			if (automatic) stopAutomatic();
			updateStatusZeile();
			picturePanel.repaint();
			return;
		}
		// in den Automatik-modus
		startAutomatic(mode == DIASHOW_AUTOM_SEQUENT);
	}

	//==========================================================
	// keyPressedPfeil()    
	//
	// KeyEvent.VK_UP
	// KeyEvent.VK_DOWN
	// KeyEvent.VK_LEFT
	// KeyEvent.VK_RIGHT
	//==========================================================
	private void keyPressedPfeil(int key) {

		if (key == KeyEvent.VK_UP) return; // noch nicht realisiert
		if (key == KeyEvent.VK_DOWN) return; // noch nicht realisiert

		if (automatic) stopAutomatic();

		if (key == KeyEvent.VK_RIGHT) doZeigenNextBild();
		if (key == KeyEvent.VK_LEFT) doZeigenPreviousBild();

	}

	// ==========================================================
	// keyPressedKategorie()    
	//
	// KeyEvent.VK_1
	// KeyEvent.VK_2
	// KeyEvent.VK_3
	// KeyEvent.VK_4
	//==========================================================
	private void keyPressedKategorie(int key) {

		switch (key) {
			case KeyEvent.VK_1:
				picture.meta.setCategory("1");
				break;
			case KeyEvent.VK_2:
				picture.meta.setCategory("2");
				break;
			case KeyEvent.VK_3:
				picture.meta.setCategory("3");
				break;
			case KeyEvent.VK_4:
				picture.meta.setCategory("4");
				break;
		}

		updateStatusZeile();
	}

	
	
	 
	// ==========================================================
	// keyPressedRotate()    
	//	
	// "L" = links drehen
    // "R" = rechts drehen
	//==========================================================
	private void keyPressedRotate(Rotate richtung) {

		int rotation = PM_Utils.getNextRotation (picture.meta.getRotation(), richtung);
		 

		// Neue Position setzen und neu zeichnen
		picture.meta.setRotation(rotation); 
		updateStatusZeile();
		picturePanel.repaint();
		windowMain.rereadPictureViewThumbnail(picture);
	}
	
	// ==========================================================
	// keyPressedSpiegeln()    
	//	
	// "S" = Bild Spiegeln
	//==========================================================
	private void keyPressedSpiegeln(int key) {

		 // to do !!!!

		updateStatusZeile();
		picturePanel.repaint();
		windowMain.rereadPictureViewThumbnail(picture);
		
	}
	
	
	// ==========================================================
	// doTemp()    
	//	
	//==========================================================
	private void doTemp( ) { 		
		if (tempPictureList.contains(picture)) {
			tempPictureList.remove(picture);
		} else {
			tempPictureList.add(picture);
		}
		updateStatusZeile();
	}
	private void doTempDelete( ) { 		
		// ----------------------------------------------------------------
		// Liste der temporaeren Bilder l�schen
		// ----------------------------------------------------------------
		if (tempPictureList.size() == 0) {
			return;
		}
		int n = JOptionPane.showConfirmDialog(this,
				"Sollen alle Temp Bilder gel�scht werden?",
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		tempPictureList.clear();
		updateStatusZeile();
	}	
	private void doTempViewed() {

		if (tempPictureList.size() == 0) {
			JOptionPane.showConfirmDialog(this, "Keine Temp-Bilder-Liste",
					"keine Temp-Liste", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return; // keine temp list
		}
		stopAutomatic(); // vorsichtshalber
		if (tempViewed) {
			// Original liste darstellen
			tempViewed = false;
			pictureList = pictureListOrig;
			picture = pictureOrig;
		} else {
			// Temp - liste darstellen
			tempViewed = true;
			pictureList = tempPictureList;
			pictureOrig = picture;
			picture = tempPictureList.get(0);
		}

		// jetzt Bild anzeigen
		doZeigenBild();

	}
	
	
	
	
// private boolean tempViewed = false;
//	private List<PM_Picture> pictureList = null;
//	private PM_Picture picture = null;    // das wird gerade dargestellt
	// Tempor�r
//	private List<PM_Picture> tempPictureList; 
//	private PM_Picture pictureTemp = null;
	// NICHT Tempor�r
//	private PM_Picture pictureOrig = null;    // das wird gerade dargestellt
//	private List<PM_Picture> pictureListOrig = null;
	
	 
	
	
	//==========================================================
	// keyPressedSeconds()    
	//
	// KeyEvent.VK_PLUS    Sekunden erh�hen
	// KeyEvent.VK_MINUS   Sekunden vermindern
	//==========================================================
	private void keyPressedPlusMinus(int key) {
		if (!automatic) return;
		if (timeSeconds < 2 && key == KeyEvent.VK_MINUS) return; // nicht noch kleiner
		if (key == KeyEvent.VK_PLUS) timeSeconds++;
		if (key == KeyEvent.VK_MINUS) timeSeconds--;
	}

	//==========================================================
	// keyPressedEscape()    
	//
	// KeyEvent.VK_ESCAPE  -- Automatic beenden
	//                     -- Diashow verlassen
	//==========================================================
	private void keyPressedEscape() {
		stopDiashow();
	}

	//==========================================================
	// keyPressedEnter()    
	//
	// KeyEvent.VK_ENTER  �nderungen aufrufen (Diashow wird verlassen)

	//==========================================================
	private void keyPressedEnter() {
		aendernAufrufen = true;
		stopDiashow();
	}

	//==========================================================
	// start()
	//
	// Aufruf beim Start einer neuen Diashow.
	// (es existiert nur eine Instanz dieser Klasse, die beim
	//  Hochfahren von PM angelegt wird; bei jedem Aufruf der 
	//  Diashow wird lediglich hier start() aufgerufen)
	// 
	//  Mode:  DIASHOW_NORMAL
	//         DIASHOW_AUTOM_SEQUENT
	//         DIASHOW_AUTOM_RANDOM
	//==========================================================
	public boolean start(PM_Picture  picture, List<PM_Picture> pictureList, int mode) {

		// vorsichtshalber einige Vorarbeiten
		PM_SequencesInout.getInstance().flush();
		
		 

		// wenn dieses Bild nicht dargestellt werden kann, dann sofort beenden
		if (picture  == null) {
			return false;
		}
		
		// kann dargestellt werden                           
		this.picture  = picture ;
		this.pictureList = pictureList;
		pictureListOrig = pictureList;
		tempViewed = false;
		aendernAufrufen = false;

		// -----------------------------------------------
		// Start automatisch
		//  (auskommentieren wenn nicht erw�nscht) !!!!!!!!!!!
		// -------------------------------------------------
		if (mode != DIASHOW_NORMAL) {
			automatic = true;
			automaticSequ = (mode == DIASHOW_AUTOM_SEQUENT);
			// jetzt Timer stellen
			timer = new Timer();
			timer.schedule(new MyTimerTask(), timeSeconds * 1000);
		}

		// -----------------------------------------------
		// nun das erster Bild darstellen
		// -------------------------------------------------
		doZeigenBild();

		return true;
	}

	//==========================================================
	// Aufrufe nach Beenden des Dialogs vom MainWindow aus:
	// 
	//    getAendernAufrufen()
	//
	//==========================================================
	public boolean getAendernAufrufen() {
		return aendernAufrufen;
	}

//	public PM_PictureView getPictureView() {
//		return pictureView;
//	}

	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================
	// ======================= PRIVATE ===============================================

	//==========================================================
	// stopDiashow()
	//
	//==========================================================
	private void stopDiashow() {
		 
		stopAutomatic(); // vorsichtshalber
		dialog.dispose();
	}

	//==========================================================
	// startAutomatic()
	//
	//==========================================================
	private void startAutomatic(boolean sequ) {
		// wenn schon l�uft, dann return
		if (automatic && automaticSequ == sequ) return;

		// starten (evtl. Wechsel von random to sequ or vs.)
		automatic = true;
		automaticSequ = sequ; // true, wenn sequentiell (nicht random)

		boolean ok = doZeigenNextBild();
		if (!ok) {
			// es gibt kein n�chstes Bild
			stopAutomatic();
			return;
		}
		// jetzt Timer stellen
		timer = new Timer();
		timer.schedule(new MyTimerTask(), timeSeconds * 1000);
	}

	//==========================================================
	// stopAutomatic()
	//
	//==========================================================
	private void stopAutomatic() {
		automatic = false;
		if (timer != null) timer.cancel(); // alten canceln

	}

	// Timer
	//  timer = new Timer();    
	//   timer.schedule(new MyTimerTask(), 3*1000);
	//==========================================================
	// Innerclass: MyTimerTask
	// ==========================================================
	class MyTimerTask extends TimerTask {
		public void run() {
			// Timer ist jetzt abgelaufen
			boolean ok = doZeigenNextBild();
			if (!ok) {
				// es gibt kein n�chstes Bild
				stopAutomatic();
				return;
			}
			if (timer != null) timer.cancel(); // alten canceln
			timer = new Timer();
			timer.schedule(new MyTimerTask(), timeSeconds * 1000);

		}
	}

	//==========================================================
	// getMoveAfterRotation
	//
 
	//==========================================================
	private Point getMoveAfterRotation(int rotation, Rectangle imageOrigRect ) {
		// Image drehen mit "degrees" und Verschieben nach Drehung
		 
		int drehenMoveX = 0;
		int drehenMoveY = 0;

		if (rotation == CLOCKWISE_90_DEGREES) {		 
			drehenMoveX = imageOrigRect.width;
			drehenMoveY = 0;
		} else if (rotation == CLOCKWISE_270_DEGREES) {			 			 
			drehenMoveX = 0;
			drehenMoveY = imageOrigRect.height;
		} else if (rotation == CLOCKWISE_180_DEGREES) {		 		 
			drehenMoveX = imageOrigRect.height;
			drehenMoveY = imageOrigRect.width;
		}

		return new Point(drehenMoveX, drehenMoveY);
	}
	
	//==========================================================
	// getPicturePanel
	//
	// In diesem Panel wird das picture dargestellt
	//==========================================================
	private JPanel getPicturePanel() {
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				if (imageOriginal != null) {
					// --------------------------------------------------------
					//
					//  Achtung: dieses ist der Master. Eine Kopie in 'PM_AusschneidenDiaShow' !!!
					//
					// ---------------------------------------------------------
					Rectangle deviceRect = new Rectangle(picturePanel.getWidth(), picturePanel.getHeight());
					 
					// ===============  drehen vorbereiten ===============			
					Rectangle imageRectNichtGedreht = new Rectangle(PM_UtilsGrafik
							.getImageSize(imageOriginal));
					Rectangle cutRect = (picture.meta.hasCutRectangle()) ? picture.meta.getCutRectangle()
							: imageRectNichtGedreht;
					
					
					int rotation = picture.meta.getRotation();
					Rectangle imageOrigRect = new Rectangle(imageOriginal.getWidth(this), imageOriginal
							.getHeight(this));
					if (rotation != CLOCKWISE_0_DEGREES) {
						// Bild wird gedreht !!!
						imageOrigRect.setSize(imageOrigRect.height, imageOrigRect.width);
						cutRect = PM_UtilsGrafik
						    .rotateCutRectangle(rotation, cutRect, imageRectNichtGedreht);
					}
					// === jetzt stimmen imageOrigRect und cutRect in der richtigen Drehung =====
					
					
					
					// Scalen mit "sc"
					double sc = 0;
					if (PM_UtilsGrafik.getRatio(cutRect) < PM_UtilsGrafik.getRatio(deviceRect)) {
						sc = (double) deviceRect.height / (double) cutRect.height;
					} else {
						sc = (double) deviceRect.width / (double) cutRect.width;
					}
					// transfer zum Schluss (in die Mitte vom Decive)
					int deviceMitteX = (deviceRect.width + 1) / 2;
					int cutMitteX = (int) (((cutRect.width + 1) / 2) * sc);
					int moveX = deviceMitteX - cutMitteX;
					int deviceMitteY = (deviceRect.height + 1) / 2;
					int cutMitteY = (int) (((cutRect.height + 1) / 2) * sc);
					int moveY = deviceMitteY - cutMitteY;

					// ---------------------------------------------------
					// jetzt Affine Transformation
					// ---------------------------------------------------
					// in umgekehrter Reihenfolge aufbereiten
					AffineTransform Tx = new AffineTransform();
					// (5) zum Schluss Bild in die Mitte
					Tx.translate(moveX, moveY);
					// (4) Cut nach links oben verschieben
					Tx.translate(-cutRect.x * sc, -cutRect.y * sc);
					// (3) scalen
					Tx.scale(sc, sc);
					// (2) Verschieben nach drehen (sodass Image im II. Quadranten steht)
					Point moveAfterRotation =  getMoveAfterRotation(rotation, imageOrigRect);	
					Tx.translate(moveAfterRotation.x, moveAfterRotation.y);
					// (1) drehen
					Tx.rotate(picture.meta.getRotation() * Math.PI / 180.);

					// ========== Tx aufbereitet ======================
					// AffineTransform und Clipping-Area sichern und ...
					//			AffineTransform saveAT = g2.getTransform();				 
					Rectangle saveClip = g2.getClipBounds();
					// ... setzen (erst setClip, dann setTransform !!!	) danach ....    
					g2.setClip(moveX, moveY, (int) (cutRect.width * sc), (int) (cutRect.height * sc));
					//	 	    g2.setTransform(Tx);  
					// .... transformieren und zeichnen und zum Schluss ...
					g2.drawImage(imageOriginal, Tx, this);
					// .... gesicherte AffineTransform und Clipping-Area zur�ck
					// (wichtig: Reihenfolge !!)
					//        g2.setTransform(saveAT);	        
					g2.setClip(saveClip.x, saveClip.y, saveClip.width, saveClip.height);

				} // if imageOriginal            
			} // paintComponent
		};

		panel.setBackground(Color.BLACK);

		return panel;
	}

	
	
	
	
	
	//==========================================================
	// getLowerPanelShow
	//
	//  
	//==========================================================
	private JPanel getLowerShowPanel() {
		Color colorBackground = Color.red;
		JPanel panelLeft = new JPanel();
		panelLeft.setBackground(colorBackground);
		panelLeft.setPreferredSize(new Dimension(0, 30));
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		panelLeft.setLayout(fl);

		temporare = new JLabel( );
		temporare.setFont(new Font("Arial", Font.BOLD, fontSize)); 
		panelLeft.add(temporare);
		
		// Label "Zeit in Sekunden f�r Automatic"
		labelTime = new JLabel("   ");
		panelLeft.add(labelTime);
		labelTime.setFont(new Font("Arial", Font.BOLD, 20));

		// Label "bearbeitet"
		bearbeitet = new JLabel("   ");
		panelLeft.add(bearbeitet);
		bearbeitet.setFont(new Font("Arial", Font.BOLD, fontSize));

		/*   
		 // Button "stop"
		 JButton buttonStop = new JButton("stop");
		 buttonStop.setBackground(colorBackground);
		 panel.add(buttonStop); 
		 ActionListener alStop = new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
		 stopDiashow();
		 }
		 };
		 buttonStop.addActionListener(alStop); 
		 */

		// Anzahl
		labelAnzahl = new JLabel("   ");
		panelLeft.add(labelAnzahl);
		labelAnzahl.setFont(new Font("Arial", Font.BOLD, fontSize));

		// lesen Bildnr asynchron
		/*
		 lesenBildNr = new JLabel("x");
		 panel.add(lesenBildNr);        
		 Font font = lesenBildNr.getFont();
		 Font newFont = new Font(font.getName(), Font.PLAIN, font.getSize());
		 lesenBildNr.setFont(newFont);
		 */

		// Button "Aendern"
		/*
		 JButton buttonAendern = new JButton("aen");
		 buttonAendern.setBackground(colorBackground);
		 panel.add(buttonAendern); 
		 ActionListener alAen = new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
		 aendernAufrufen = true;
		 stopDiashow();
		 }
		 };
		 buttonAendern.addActionListener(alAen); 
		 
		 */

		// --------------------------------------------------------------------
		// Bezeichnung
		// --------------------------------------------------------------------
		labelBezeichnung = new JLabel();
		panelLeft.add(labelBezeichnung);
		labelBezeichnung.setFont(new Font("Arial", Font.BOLD, fontSize));
		panelLeft.setPreferredSize(new Dimension(0, fontSize + 10));

		
	 
//		JPanel panelRight = new JPanel();
//		panelRight.setBackground(colorBackground);
//		panelRight.setPreferredSize(new Dimension(0, 30));
	 
	 

		 
		
	 
		
		return panelLeft ;
	}

	//======================================================
	//  doZeigenBild
	//
	// Das erste Bild bei start anzeigen
	//======================================================  
	private void doZeigenBild() {
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		boolean prefetchRichtung = true; // vorw�rts
		imageOriginal = getImageOriginal(prefetchRichtung);

		updateStatusZeile();

		picturePanel.repaint();

		//  Cursor wieder zurueck !!!!! 
		setCursor(cursor);
	}

	//======================================================
	//  doZeigenNextBild
	//
	//======================================================  
	private boolean doZeigenNextBild() {
		// Bild holen, wenn Automatic und random  
		if (automatic && automaticSequ == false) {
			return doZeigenNextBildRandom();
		}

		// Jetzt keine Automatic mit Randon	
		int index = pictureList.indexOf(picture);
		if (index == -1 || index +1 >= pictureList.size()) {
			JOptionPane.showConfirmDialog(this, "Keine weiteren Bilder", "Ende", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return false; // kein Bild mehrr
		}
		picture = pictureList.get(index + 1) ;

		 

		// --------------------------------------------------------------------
		// naechstes Bild darstellen
		// Mit wait Cursor
		// --------------------------------------------------------------------
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		 
		boolean prefetchRichtung = true; // vorw�rts
		imageOriginal = getImageOriginal(prefetchRichtung);

		updateStatusZeile();

		picturePanel.repaint();
		// Cursor wieder zurueck !!!!! 
		setCursor(cursor);

		return true; // es wurde noch ein Bild dargestellt
	}

	//======================================================
	//  doZeigenPreviousBild
	//
	//======================================================  
	private void doZeigenPreviousBild() {

		// Jetzt keine Automatic mit Randon	
		int index = pictureList.indexOf(picture);
		if (index <= 0) {
			JOptionPane.showConfirmDialog(this, "Keine weiteren Bilder", "Ende", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
			return; // kein Bild mehrr
		}
		picture = pictureList.get(index - 1) ;

 

		// --------------------------------------------------------------------
		// vorheriges Bild darstellen
		// (mit wait-cursor)
		// --------------------------------------------------------------------
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		 

		boolean prefetchRichtung = false; // r�ckw�rts
		imageOriginal = getImageOriginal(prefetchRichtung);

		updateStatusZeile();
		picturePanel.repaint();
		// Cursor wieder zurueck !!!!! 
		setCursor(cursor);
	}

	//======================================================
	//  doZeigenNextBildRandom
	//
	//======================================================  
	private boolean doZeigenNextBildRandom() {

		imageOriginal = getImageOriginalRandom();

		// --------------------------------------------------------------------
		// keine weiteren Bilder mehr
		// --------------------------------------------------------------------
		if (imageOriginal == null) {
			//      JOptionPane.showConfirmDialog(
			//                       this, 
			//                       "Kein Bild vorhanden",
			//                       "Ende", 
			//                       JOptionPane.DEFAULT_OPTION, 
			//                       JOptionPane.INFORMATION_MESSAGE);
			return false; // kein Bild mehrr     
		}

		// --------------------------------------------------------------------
		// naechstes Bild darstellen
		// Mit wait Cursor
		// --------------------------------------------------------------------
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		updateStatusZeile();

		picturePanel.repaint();
		// Cursor wieder zurueck !!!!! 
		setCursor(cursor);

		return true; // es wurde noch ein Bild dargestellt
	}

	//======================================================
	//  getImageOriginal()
	//
	// Lesen ImageOriginal mit Prefetch weiterer Images
	//
	// prefetchRichtung: true = vorw�rts
	//                   false = r�ckw�rts
	//======================================================  
	private Image getImageOriginal(boolean prefetchRichtung) {

		// Lesen 'picture' sofort und einige im Voraus.     
		List<PM_Picture> sofort = Collections.singletonList(picture);
		List<PM_Picture> prefetch =  getPrefetchList(picture , pictureList, prefetchRichtung);
		PM_Picture.readImageOriginal(sofort, prefetch);

		return picture.getImageOriginal();

	}
	// =====================================================
	// getPicture()
	// =====================================================   
	public PM_Picture getPicture() {
		return picture;
	}
	
	
	// ======================================================
	// getPrefetchList()
	//
	// Es wird nur die Liste der PM_Picture's ermittelt. Die Images
	// werden hier noch nicht gelesen !!
	//
	// F�r Lesen Images im Voraus:
	// Ab dem �bergebenen PM_PictureView werden im Voraus (vorw�rts = true)
	// bzw. r�ckw�rts (vorw�rts = false) PM_Picture-Instanzen in die
	// Return-liste
	// geschrieben. Die Anzahl wird aus der Datei "Einstellungen" geholt.
	// ======================================================
	static public List<PM_Picture> getPrefetchList(PM_Picture  picture, List<PM_Picture> picList, boolean vorwaerts) {
		
		List<PM_Picture> returnListe = new ArrayList<PM_Picture>();
		int index = picList.indexOf(picture);
		if (index == -1) {
			return returnListe;
		}
		PM_Configuration einstellungen = PM_Configuration.getInstance();
		int plus ;
		int minus ;
		if (vorwaerts) {
		    plus = einstellungen.getPrefetchPlus();
		    minus = einstellungen.getPrefetchMinus();
		} else {
			minus = einstellungen.getPrefetchPlus();
			plus = einstellungen.getPrefetchMinus();	
		}
		
		// vorw�rts
		for (int i = 0; i < plus; i++) {			 	 
			if (index +2 >= picList.size()) {
				break;
			}
			returnListe.add(picList.get(index + 1)) ;
			index++;		
		}
		
		// r�ckw�rts
		for (int i = 0; i < minus; i++) {			 	 
			if (index == 0) {
				break;
			}
			returnListe.add(picList.get(index - 1)) ;
			index--;		
		}
	 	
		 
		return returnListe;
	}

	 
 
	
	
	//======================================================
	// getImageOriginalRandom()
	//======================================================  
	private Image getImageOriginalRandom() {

		picture   = pictureList.get(random.nextInt(pictureList.size()));
		 		 
		List<PM_Picture> sofort = Collections.singletonList(picture);
		PM_Picture.readImageOriginal(sofort, new ArrayList<PM_Picture>());
		return picture.getImageOriginal();

	}

	//======================================================
	// Statuszeile: updateStatusZeile
	//
	// Die (untere) Zeile (Statuszeile) wird erstellt
	//======================================================  
	private void updateStatusZeile() {

		setLabelAnzahl();
		setTextUnterBild();
		setBearbeitet();
		setTimeSeconds();
		setTemporare();
	}

	//======================================================
	// Statuszeile:  setTextUnterBild()
	//
	//======================================================  
	private void setTextUnterBild() {
		PM_Configuration einStllg = PM_Configuration.getInstance();

		String textResult = "";

		textResult = addText(textResult, einStllg.getSlideshowText1());
		textResult = addText(textResult, einStllg.getSlideshowText2());
		textResult = addText(textResult, einStllg.getSlideshowText3());

		labelBezeichnung.setText(textResult);

	}

	//======================================================
	// Statuszeile:  addText()
	//
	// Hilfsmethode f�r setTextUnterBild()
	//======================================================  
	private String addText(String text, String type) {
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
	//		neu = picture.meta.getSequenz();
			String seq = picture.meta.getSequence();
			String[] sa = seq.split(" ");
			for (int i = 0; i < sa.length; i++) {
				String s = sa[i];
				if (s.indexOf("s") < 0) continue;
				String[] ss = s.split("_");
				if (ss.length != 2) break;
				PM_Sequence sequenz = PM_Sequence.getSequenzFromAll(ss[0]);
				if (!(sequenz.getType() == SequenceType.BASE || sequenz.getType() == SequenceType.EXTENDED)) {
					// weder X noch B
					continue;
				}
				neu = sequenz.getPath();
				 
				break;
				 
			}
			
//			neu += picture.meta.getQs();
		}
		
		
		

		// anh�ngen wenn vorhanden
		if (neu.length() == 0) return text; // nichs anh�ngen

		if (text.length() == 0) return neu;

		return text + " " + neu;
	}

	//======================================================
	// Statuszeile:  setBearbeitet()
	//
	//======================================================  
	private void setBearbeitet() {
		boolean cut = picture.meta.hasCutRectangle();
		if (cut) {
			bearbeitet.setText("#");
			return;
		}
		if (picture.meta.getModified()) {
			bearbeitet.setText("x");
		} else {
			bearbeitet.setText("");
		}
	}

	//======================================================
	// Statuszeile:  setTimeSeconds()
	//
	// ganz rechts die Zeit in Sekunden wenn Automatic
	//======================================================  
	private void setTimeSeconds() {

		if (!automatic) {
			// Sekundenanzeige l�schen, da keine Automatic
			labelTime.setText("");
			return;
		}

		// Zeit aufbereiten
		String z = Integer.toString(timeSeconds);
		if (!automaticSequ) z += "r"; // Random	  
		labelTime.setText(z);
	}

	//======================================================
	//  setLowerPanel
	//
	//======================================================  
	private void setLabelAnzahl() {

		// schreiben Anzahl
		int size = pictureList.size();
		int index = pictureList.indexOf(picture );
		String indexSize = Integer.toString(index + 1) + "/" + Integer.toString(size);
		labelAnzahl.setText(indexSize);

	}
	
	//======================================================
	//  setTemporare()
	//
	//======================================================  
	private void setTemporare() {
		if (tempViewed) {
			temporare.setText("T");
			return;
		}  			
		if (tempPictureList.contains(picture)) {
			temporare.setText("t");	
			return;
		}  
		temporare.setText(" ");	
	}

	// ============================================================
	// ============================================================
	// InnerClass: RadioListener
	// ============================================================
	// ============================================================  
	/** Listens to the radio buttons. */
	class RadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String q = e.getActionCommand().substring(1);
			//System.out.println("--- set QS = " + q);
			picture.meta.setCategory(q);
		}
	}

	//======================================================
	//======================================================
	//  Inner Class: MyKeyAdapter
	// (fuer den gesamten Dialog)
	//======================================================  

	// ***********  ACHTUNG: im JDIalog kein KeyAdapter moeglich ???? !!!!!!!!!!!!!

	class MyKeyAdapter extends KeyAdapter {

		public void keyPressed(KeyEvent e) {

		//System.out.println(" ............. Dialog: keyPressed");

		/****
		 if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		 // beenden
		 if ( buttonUebernehmen.isEnabled() ) {
		 warnung();
		 return;
		 } 
		 stopDiashow();  
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

			if (e.getButton() == 3) { //  && e.getClickCount() == 1) {
				// vorwaerts

				keyPressedPfeil(KeyEvent.VK_RIGHT);
				return;
			}
			if (e.getButton() == 1) { // && e.getClickCount() == 1) {
				// zurueck

				keyPressedPfeil(KeyEvent.VK_LEFT);
				return;
			}

		} // mouseClicked 
	} // MyMouseAdapter   

} // Ende Klasse