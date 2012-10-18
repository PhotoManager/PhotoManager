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
package pm.print;

import pm.gui.*; 
import pm.index.PM_Index;
import pm.inout.PM_All_InitValues;
import pm.utilities.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.List;
import java.util.*;

import javax.print.*;
import javax.swing.*;
import javax.swing.event.*;
 

 

/**
 * The GUI for the subwindow "Print"
 * 
 *  
 *
 */
@SuppressWarnings("serial")
public class PM_WindowPrintUI extends PM_WindowBase implements PM_Interface, MouseListener {

 
	protected final PM_Index  indexViewThumbnails;  
	protected JPanel windowPaper = null;

	protected PrintService printService = null;
	protected PrinterJob printerJob = null;

	
	protected PM_PmPrinter pmPrinter = null;
	protected PM_PictureFormat pictureFormat = null;

	protected PM_AllSystemPrinter allSystemPrinter;
	protected PM_Configuration properties;
	protected PM_PaperFormat paperFormat;

	// ================================================================================
	//    JComponent's
	// ================================================================================
	private JButton buttonEinstellen = null;
	private JLabel hinweisDruckerEinstellen = null;
	private JComboBox bildFormatListe = null;
	protected JComboBox listSystemPrinter = null;
	private JComboBox listePmDrucker = null;
	private JComboBox bildBeschriftungsListe = null;

	protected JTextField maxPapierBereich = null;
	protected JCheckBox hilfslinien = null;

	protected float bildschirmAufloesung;

	private JSplitPane splitPane = null;
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_WindowPrintUI(  ) {
		super(PM_Index.createIndexRight( ));

		paperFormat = PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F1); // Default

		properties = PM_Configuration.getInstance();
		bildschirmAufloesung = properties.getMonitorResolution();

		indexViewThumbnails = windowMain.getIndexViewThumbnails();

		
		buildUI();

		// Initialisieren

		allSystemPrinter = properties.getAllSystemPrinter();
		 
		
		List<PM_SystemPrinter> systemPrinters = allSystemPrinter.getSystemPrinters();
		
		if ( ! systemPrinters.isEmpty()) {
			listSystemPrinter.setModel(new DefaultComboBoxModel(systemPrinters.toArray()));
			listSystemPrinter.setSelectedItem(systemPrinters.get(0));
			setListPmPrinter(systemPrinters.get(0));
			setHinweisDruckerEinstellen();

			setBildFormatListe(PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F1));
			pictureFormat = (PM_PictureFormat) bildFormatListe.getSelectedItem();

		}
		
		// --------------------------------------------------------
		// Change Listener f�r message
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
		labelSystemPrinter.setText(PM_MSG.getMsg("winPrtSysPrinter"));
		buttonEinstellen.setText(PM_MSG.getMsg("winPrtButtonConfigure"));
		labelPMprinter.setText(PM_MSG.getMsg("winPrtPMprinter"));
		buttonPrint.setText(PM_MSG.getMsg("winPrtButtonPrint"));
		labelFormat.setText(PM_MSG.getMsg("winPrtLabelFormat"));
		labelRemark.setText(PM_MSG.getMsg("winPrtLabelRemark"));
		labelPaperArea.setText(PM_MSG.getMsg("winPrtLabelPaperArea"));
		hilfslinien.setText(PM_MSG.getMsg("winPrtCheckBoxSubLine"));
		
		
		
		
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
	// closeAlbum()
	// ======================================================
	@Override
	public void closeAlbum() {	 

	} 
 
	// ======================================================
	// close()
	//
	// Ende der Verarbeitung
	// ======================================================
	@Override
	public void close() {	 	
		PM_All_InitValues.getInstance().putValueInt(this, "vertical-devider",
				splitPane.getDividerLocation());		
	}
	
	
	
	/**
	 * flush all windows.
	 * 
	 * remove all displayed thumbs so i.e. you can do the import 
	 *
	 */
	@Override
	public boolean flush() {
 		getIndex().data.removeAllPictures();
 		doClear();
 		doRepaint();
		return true;
	}
	
	
	
	
	// ======================================================
	// buildUI()
	// ======================================================
	protected void buildUI() {
		setBackground(Color.green);

		 

		JScrollPane scRightPanel = new JScrollPane(getRightPanel());
		//		scRightPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//		scRightPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getIndex().getIndexPanel(), scRightPanel);
		
		
		int location = PM_All_InitValues.getInstance().getValueInt(this, "vertical-devider");
		if (location == 0) {
			location = 100;
		}
		splitPane.setDividerLocation(location);
		
		
		 

		setLayout(new BorderLayout());

		JScrollPane scUpperPanel = new JScrollPane(getUpperPanel());
		//		scUpperPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//		scUpperPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		add(scUpperPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);

	 
 
	}

 

	protected void doSetupSystemPrinter() {
	// wird �berladen
	}

	/**
	 * The Print button is pressed.
	 */
	protected void doPrint() {
	 
	}

	/**
	 * Set defalt zooming 100 %.
	 */
	protected void setDefaultZooming() {
	//		 wird �berladen
	}

	//======================================================
	//  doClear
	//
	// Alle aufbereiteten Bilder l�schen
	//======================================================
	protected void doClear() {
	//		 wird �berladen
	}

	 
	 
	/**
	 * A new paper format is selected.
	 */
	protected void setPaperFormat(PM_PaperFormat papierFormat) {
	 
	}

	 
	/**
	 * A new picture format is selected.
	 */
	protected void setPictureFormat(PM_PictureFormat pf) {
	 
	}

	 

	//==========================================================
	// getUpperPanel
	//
	// (Oben: einige Buttons)
	//==========================================================

	private JLabel labelSystemPrinter;
	private JLabel labelPMprinter;
	private JPanel getUpperPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.yellow);
		panel.setPreferredSize(new Dimension(0, 100));
		panel.setAlignmentX(0);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel zeile1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel zeile2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		//	------------------------------------------------------
		//  Liste aktueller Systemname (System-Drucker)     
		// ---------------------------------------------------------
		labelSystemPrinter = new JLabel("System Drucker:");
		zeile1.add(labelSystemPrinter);
		listSystemPrinter = new JComboBox();
		zeile1.add(listSystemPrinter);
		ItemListener ilSystemName = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) return;
				Object o = e.getItem();
				if (o instanceof PM_SystemPrinter) {
					PM_SystemPrinter sd = (PM_SystemPrinter) o;
					// setzen Liste der PM-Drucker in ComboBox
					setListPmPrinter(sd);
					setHinweisDruckerEinstellen();
				}
			}
		};
		listSystemPrinter.addItemListener(ilSystemName);

		// -------------------------------------------------------
		// Button "Drucker einstellen"
		// ---------------------------------------------------------
		buttonEinstellen = new JButton("Einstellen");
		zeile1.add(buttonEinstellen);
		ActionListener alEinstellen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	//			String info = "Drucker bitte im Betriebssystem einstellen,\naber 'System Drucker' hier ausw�hlen.";
				String info = PM_MSG.getMsg("winPrtMsgSelectPrinter");
				JOptionPane.showConfirmDialog(null, info, "Info",
						JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				
	// Achtung: Wenn Meldung nicht mehr erscheint, d.h. Drucker wiede hier eingestellt
				// wird, dann auch setHinweisDruckerEinstellen() korrigieren !!!!!!!!!!!
				
	 //			doSetupSystemPrinter();  
				 
			}
		};
		buttonEinstellen.addActionListener(alEinstellen);

		// --------------------------------------------------------
		// Text f�r "Drucker nicht eingestellt"		
		// ---------------------------------------------------------
		hinweisDruckerEinstellen = new JLabel(" ");
		zeile1.add(hinweisDruckerEinstellen);

		// ----------------------------------------------------------
		// aktueller PM-Name  (Liste der zugeh�rigen PM-Drucker)
		// ---------------------------------------------------------
		labelPMprinter = new JLabel("PM Drucker:");
		zeile2.add(labelPMprinter);
		listePmDrucker = new JComboBox();
		zeile2.add(listePmDrucker);
		ItemListener ilPmName = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) return;
				Object o = e.getItem();
				if (o instanceof PM_PmPrinter) {
					setPmPrinter((PM_PmPrinter) o);
				}
			}
		};
		listePmDrucker.addItemListener(ilPmName);

		// ----------------------------------------------------------
		// Papierformat(Radiobuttons)
		// ----------------------------------------------------------
		JPanel format = new JPanel();
		zeile2.add(format);
		format.setLayout(new FlowLayout(FlowLayout.LEFT));
		JRadioButton f11 = new JRadioButton(PAPER_FORMAT_F1);
		JRadioButton f12 = new JRadioButton(PAPER_FORMAT_F2);
		JRadioButton f14 = new JRadioButton(PAPER_FORMAT_F4);
		JRadioButton f16 = new JRadioButton(PAPER_FORMAT_F6);
		f11.setActionCommand(PAPER_FORMAT_F1);
		f12.setActionCommand(PAPER_FORMAT_F2);
		f14.setActionCommand(PAPER_FORMAT_F4);
		f16.setActionCommand(PAPER_FORMAT_F6);
		ButtonGroup group = new ButtonGroup();
		group.add(f11);
		group.add(f12);
		group.add(f14);
		group.add(f16);
		format.add(f11);
		format.add(f12);
		format.add(f14);
		format.add(f16);
		f11.setSelected(true); // Radiobutton auf PAPER_FORMAT_11 setzen
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPaperFormat(PM_PaperFormat.getPaperFormat(e.getActionCommand()));
				doRepaint();
			}
		};
		f11.addActionListener(al);
		f12.addActionListener(al);
		f14.addActionListener(al);
		f16.addActionListener(al);

		// ----------------------------------------------------------
		// Fertig: Zeile 1 und 2 zusammen
		// ----------------------------------------------------------
		panel.add(zeile1);
		panel.add(zeile2);

		return panel;
	} 
	
	//==========================================================
	// getRightPanel
	//
	// (rechts �ber dem Papierbereich mit den Bildern)
	//==========================================================
	private JButton buttonPrint;
	private JLabel labelFormat;
	private JLabel labelRemark;
	private JLabel labelPaperArea;
	private JPanel getRightPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel zeile1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel zeile2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// -----------------------------------------------------------------
		// Button "drucken"
		// -----------------------------------------------------------------
		buttonPrint = new JButton("drucken");
		zeile1.add(buttonPrint);
		buttonPrint.setBackground(Color.red);
		ActionListener alDrucken = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPrint();
			}
		};
		buttonPrint.addActionListener(alDrucken);

		// -------------------------------------------------------------------
		// Bildformat(-Liste) (Combo-Box)
		// -------------------------------------------------------------------
		labelFormat = new JLabel("Format:");
		zeile1.add(labelFormat);
		bildFormatListe = new JComboBox();
		zeile1.add(bildFormatListe);
		ItemListener ilBildFormat = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) return;
				Object o = e.getItem();
				if (o instanceof PM_PictureFormat) {
					setPictureFormat((PM_PictureFormat) o);
				}
			}
		};
		bildFormatListe.addItemListener(ilBildFormat);

		// -------------------------------------------------------------------
		// Bildbeschriftung
		// -------------------------------------------------------------------
		labelRemark = new JLabel("Beschriftung:");
		zeile1.add(labelRemark);
		bildBeschriftungsListe = new JComboBox(getBildBeschriftungsListe());
		zeile1.add(bildBeschriftungsListe);
//		ItemListener ilBildBeschriftung = new ItemListener() {
//			public void itemStateChanged(ItemEvent e) {
//				Object o = e.getItem();
//				if (o instanceof String) {
//					String str = (String)o;
//				}
//			}
//		};
//		bildBeschriftungsListe.addItemListener(ilBildBeschriftung);

		// --------------------------------------------------------------
		// Button "clear"
		//
		// Alle aufbereiteten Bilder l�schen
		// --------------------------------------------------------------		
		JButton buttonClear = PM_Utils.getJButon(ICON_DELETE);
		zeile2.add(buttonClear);
		ActionListener alClear = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				doClear();
				doRepaint();
			}
		};
		buttonClear.addActionListener(alClear);

		// -------------------------------------------------------------
		// Button Zoom (press --> 100 %)
		// -------------------------------------------------------------
		JButton buttonZoom = new JButton("100 %");
		zeile2.add(buttonZoom);
		ActionListener alZoom = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaultZooming();
				doRepaint();
			}
		};
		buttonZoom.addActionListener(alZoom);

		// -------------------------------------------------------
		// Papierbereich f�r ein Bild
		// -------------------------------------------------------
		labelPaperArea = new JLabel("Papierbereich:");
		zeile2.add(labelPaperArea);
		maxPapierBereich = new JTextField("  ");
		maxPapierBereich.setEditable(false);
		//	 maxPapierBereich(10);
		zeile2.add(maxPapierBereich);
		zeile2.add(new JLabel("mm"));

		//	-------------------------------------------------------
		// CheckBock Hilfslinien drucken
		// -------------------------------------------------------
		hilfslinien = new JCheckBox("Hilfslinien Drucken");
		zeile2.add(hilfslinien);

		// Diese zwei Zeilen zusammen ---> panelOben 
		JPanel panelOben = new JPanel();
		panelOben.setBackground(Color.yellow);
		panelOben.setLayout(new BoxLayout(panelOben, BoxLayout.Y_AXIS));

		panelOben.add(zeile1);
		panelOben.add(zeile2);

		// ------------------------------------------------------------
		// center:  Papier-Breich  mit den Bildern
		// ------------------------------------------------------------

		windowPaper = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (pmPrinter == null) {
					return;
				}
				windowPaperPaint(g);
			}
		};
		windowPaper.addMouseListener(this);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		p.add(windowPaper, BorderLayout.CENTER);
		p.add(getSliderWindowPaper(), BorderLayout.SOUTH);

		JScrollPane scrollDruckPanel = new JScrollPane(p);

		 
		// jetzt rechtes Gesamtpanel    
		panel.add(panelOben, BorderLayout.NORTH);
		panel.add(scrollDruckPanel, BorderLayout.CENTER);

		return panel;
	}

 

	//==========================================================
	// setPmDrucker()
	//
	// (drucker wurde neu ausgewaehlt oder geaendert)
	//==========================================================
	protected void setPmPrinter(PM_PmPrinter drucker) {
	// wird �berladen
	}

	//======================================================
	//  doRepaint
	//
	// Hier jetzt das Neuzeichnen veranlassen
	//======================================================
	protected void doRepaint() {
		windowPaper.repaint();
		//		windowDruckenPaper.doLayout();

		//	    indexPanel.repaint();
		//	    indexPanel.doLayout();    

	}

	//==========================================================
	// setBildFormatListe()
	//
	// Die entsprechende Liste wird in der ComboBox dargestellt
	//==========================================================
	protected void setBildFormatListe(PM_PaperFormat papierFormat) {
		PM_PictureFormatCollection instance = PM_PictureFormatCollection.getInstance();
		PM_PictureFormat[] liste = instance.getBildFormate(papierFormat);
		bildFormatListe.removeAllItems();
		for (int i = 0; i < liste.length; i++) {
			bildFormatListe.addItem(liste[i]); //.toString()); 
		}
	}

	// ==========================================================
	// getDruckerName
	//
	// ==========================================================
	protected String getDruckerName() {
		PrintService printService = printerJob.getPrintService();
		return printService.getName();
	}

	//==========================================================
	// setHinweisDruckerEinstellen()
	//
	// Der selektierte Systemdrucker:
	//    wenn nicht eingestellt, dann button-einstellen rot
	//==========================================================
	protected void setHinweisDruckerEinstellen() {

		return;
		
		// Auskommenrierungen NICHT l�schen bis Drucker wieder ��ber PM
		// eingestellt wird !!!!!!!!!!!!!!!!!!!!!
		
 	// NICHT L�SCHEN !!!!!!!!
//		Object o = listeSystemDrucker.getSelectedItem();
//		if (!(o instanceof PM_SystemDrucker)) {
//			// Kein Systemdrucker selektiert
//			buttonEinstellen.setBackground(PM_WindowBase.COLOR_ENABLED);
//			return; // Kein Systemdrucker selektiert
//		}
//		PM_SystemDrucker sysDru = (PM_SystemDrucker) o;
//		boolean eingestellt = sysDru.getEingestellt();
//		if (eingestellt) {
//			hinweisDruckerEinstellen.setText("");
//			hinweisDruckerEinstellen.setForeground(Color.BLACK);
//		} else {
//			hinweisDruckerEinstellen.setForeground(PM_WindowBase.COLOR_WARNING);
//			hinweisDruckerEinstellen.setText("Drucker nicht eingestellt");
//		}

	}

	//==========================================================
	// setListePmDrucker()
	//
	// Holt den selectierten Systemdrucker und setzt die
	// zugeh�rigen PM-Drucker-Namen in die pmNamen-Combobox
	//==========================================================
	private void setListPmPrinter(PM_SystemPrinter sysDru) {

		if (sysDru == null) return;

		listePmDrucker.setModel(new DefaultComboBoxModel(sysDru.getAllePmDrucker()));
		Object o = listePmDrucker.getSelectedItem();
		if (o instanceof PM_PmPrinter) {
			setPmPrinter((PM_PmPrinter) o);
		}
	}

	//======================================================
	// windowPaperPaint()   
	//======================================================
	private void windowPaperPaint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.translate(20, 20); // alle verschieben

		AffineTransform scaling = new AffineTransform();
		scaling.setToScale(sliderValue, sliderValue);

		// -------------------------------------------------------------------
		// Drucken aus PM_PapierFormat:
		//     �usserer Papier- und Druckbereich und
		//     die Hilfslinien zwischen den Bildern
		// -------------------------------------------------------------------
		hilfslinienAufbereiten(g2, scaling);

		// -------------------------------------------------------------------
		// Jetzt in einer Loop alle Bilder aufbereiten
		// -------------------------------------------------------------------
		PM_PicturePrint[] alleBilder = pmPrinter.getAlleBilder();
		for (int i = 0; i < alleBilder.length; i++) {
			PM_PicturePrint bild = alleBilder[i];
			if (bild.isEmpty()) continue;
			einBildAufbereiten(g2, bild, scaling);
		}
	}

	// ======================================================
	// hilfslinienDrucken()
	//
	// Check-Box abfragen
	// ======================================================	
	public boolean hilfslinienDrucken() {
		return hilfslinien.isSelected();
	}

	// ======================================================
	// hilfslinienAufbereiten()
	//
	//     �usserer Papier- und Druckbereich und
	//     die Hilfslinien zwischen den Bildern
	// ======================================================	
	public void hilfslinienAufbereiten(Graphics2D g2, AffineTransform scaling) {
 
		
		g2.draw(scaling.createTransformedShape(pmPrinter.getPapierBereichGesamt()));
		g2.draw(scaling.createTransformedShape(pmPrinter.getDruckBereichGesamt()));

		List hilfslinien = pmPrinter.getPapierFormat().getHilfsLinien();
		Iterator it = hilfslinien.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof Line2D) {
				g2.draw(scaling.createTransformedShape((Line2D) o));
			}
		}
	}

	// ======================================================
	// einBildAufbereiten()
	//
	// Wird sowohl von der Bildschirmdarstellung als auch vom
	// Druckprozess aufgerufen.
	// ======================================================
	public void einBildAufbereiten(Graphics2D g2, PM_PicturePrint bild, AffineTransform scaling) {

		Image image = bild.getImage();

		// Jetzt Bild aufbereiten   		
		Rectangle2D druckBereich = (Rectangle2D) bild.getDruckBereich();
		Rectangle2D cutRectangle = (Rectangle2D) bild.getCutRectangle();

		// muss gedreht werden ?
		double ratioDrukBereich = druckBereich.getWidth() / druckBereich.getHeight();
		double ratioCutRectangle = cutRectangle.getWidth() / cutRectangle.getHeight();
		boolean drehen = !PM_UtilsGrafik.istSelbeDarstellung(ratioDrukBereich, ratioCutRectangle);
		if (drehen) {
			// cutRectangle drehen
			cutRectangle = getCutRectangleGedreht(image, cutRectangle);
		}
		//  scalen
		druckBereich = scaling.createTransformedShape(druckBereich).getBounds();
		cutRectangle = scaling.createTransformedShape(cutRectangle).getBounds();

		AffineTransform at = new AffineTransform();
		// (5) verschieben auf Druckbereich
		at.translate(druckBereich.getX(), druckBereich.getY());
		// (4) scalen, so dass cutRectangle auf Druckbereich passt
		double scaleCut = druckBereich.getWidth() / cutRectangle.getWidth();
		at.scale(scaleCut, scaleCut);
		// (3) verschieben auf cutRectangle, das bereits gescaled ist
		at.translate(-cutRectangle.getX(), -cutRectangle.getY());
		// (2) scalen mit sliderValue (der ist 1.0 wenn drucken)		
		at.concatenate(scaling);
		// (1) ggf drehen
		if (drehen) {
			Dimension imSize = PM_UtilsGrafik.getImageSize(image);
			at.translate(imSize.getHeight(), 0);
			at.rotate(90 * Math.PI / 180.);
		}

		// Jetzt Zeichnen. (Clip-Rec auf Druck-Bereich setzen)
		Rectangle saveClip = g2.getClipBounds();
		g2.setClip(druckBereich);
		g2.drawImage(image, at, this);
		
		 
	 	
     String text = bild.getBeschriftung();
      if ( !(text == null || text.length() == 0)) {  
         	g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(Color.YELLOW);    
         	g2.drawString(text, (int)(druckBereich.getX() + 20), (int)(druckBereich.getY() + druckBereich.getHeight() - 20));  
      }   
		
		g2.setClip(saveClip);
	}

	// ======================================================
	// getSliderWindowPaper()
	// ======================================================
	public JSlider getSliderWindowPaper() {
		ChangeListener sliderCL = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					setSliderValue((int) source.getValue());
				}
			}
		};
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 40); // anfang,
		// ende,
		// init-value
		slider.addChangeListener(sliderCL);
		//	slider.setMajorTickSpacing(100);
		//	slider.setMinorTickSpacing(30);
		// slider.setPaintTicks(true);
		// slider.setPaintLabels(true);
		return slider;
	}
	// ======================================================
	// setSliderValue
	//
	// (der aktuell geaenderte Slider Value)
	//
	// hier jetzt alle Bilder mit der geaenderten Gr��e neu zeichnen !!
	// ======================================================
	protected float sliderValue = 0.4F;

	private void setSliderValue(int sliderValue) {
		this.sliderValue = sliderValue / 100.0F;
		doRepaint();
	}

	// =====================================================
	// getCutRectangleGedreht() (rechts 90 Grad)
	// =====================================================
	public Rectangle2D getCutRectangleGedreht(Image im, Rectangle2D cutRec) {

		// erst drehen, dann verschieben um Image-H�he, da rechts herum
		AffineTransform at = new AffineTransform();
		at.translate(PM_UtilsGrafik.getImageSize(im).getHeight(), 0);
		at.rotate(90 * Math.PI / 180.);

		return at.createTransformedShape(cutRec).getBounds2D();
	}

	// =====================================================
	// rechte Maustaste
	// =====================================================  
	public void mouseClicked(MouseEvent e) {
//		System.out.println("-----mouseClicked) -----");
		if (e.getButton() == 3 && e.getClickCount() == 1) {
			// holen Bild
			PM_PicturePrint[] alleBilder = pmPrinter.getAlleBilder();
			AffineTransform scaling = new AffineTransform();
			scaling.setToScale(sliderValue, sliderValue);
			for (int i = 0; i < alleBilder.length; i++) {
				PM_PicturePrint bild = alleBilder[i];
				if (bild.isEmpty()) continue;
				// jetzt pr�fen, ob cursor in diesem Bild
				Rectangle2D papierBereich = (Rectangle2D) bild.getPapierBereich();
				papierBereich = scaling.createTransformedShape(papierBereich).getBounds();
				if (papierBereich.contains(e.getPoint())) {
//					System.out.println("---- Click auf Bild nr " + i);
					doPopUp(bild, e);
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	//======================================================
	//  doPopUp()
	//
	// Die rechte Maustaste steht ueber ein zu druckendes Bild.
	// Hier wird ein Pop-Up-Menu creiert und aufgepoppt)
	//======================================================
	private void doPopUp(final PM_PicturePrint pictureDruckdaten, MouseEvent e) {

		JPopupMenu popup = new JPopupMenu();

		// ----------------------------------------------
		//   Menue: aendern 
		// ----------------------------------------------
		JMenuItem menuItemAendern = new JMenuItem("Bild �ndern");
		ActionListener alAendern = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				// tempor�re �nderungen in einem modalen Dialog vornehmen
				windowMain.doBildAendernDrucken(pictureDruckdaten);
				// geaendert == true: es wurden �nderungen im Dialog vorgenommen
			}
		};

		menuItemAendern.addActionListener(alAendern);
		popup.add(menuItemAendern);

		// ----------------------------------------------
		//   Menue: loeschen 
		// ----------------------------------------------
		JMenuItem menuItemLoeschen = new JMenuItem("Bild L�schen");
		ActionListener alLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				// Bild aus dem Papierbereich l�schen
				// (evtl. vorgenommene �nderungen werden verworfen)
				pictureDruckdaten.delete();
				doRepaint();
			}
		};
		menuItemLoeschen.addActionListener(alLoeschen);
		popup.add(menuItemLoeschen);

		// --------------  Menue aufpoppen ----------------------      
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

	//======================================================
	//  getBildBeschriftungsListe()
	//
	//======================================================
	private Vector<String> getBildBeschriftungsListe() {	
		Vector<String> v = new Vector<String>();
		v.add("");
		v.add("Datum");
		
		return v;		
		
	}
	
	//======================================================
	//  getItemBildBeschriftung()
	//
	// Der String, der gerade in der bildBeschriftungsListe ausgew�hlt wurde
	//======================================================
	protected String getItemBildBeschriftung() {	
		bildBeschriftungsListe.getSelectedItem();

		Object o = bildBeschriftungsListe.getSelectedItem();
		if (o instanceof String) {
			return (String)o;
		}  	
		return "";
	}
	
	
}
