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

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.io.*; 
import java.util.concurrent.locks.*;
 

/**
 * Dialog f�r den Start und Import von Bildern
 *
 * Es wird EINE Instanz erzeugt. Sie wird nicht zerst�rt.
 * 
 * 
 * Aufrufe:
 * 
 *  File getTopLevelDirectory():
 *  		Unmittelbar nach Programmstart: Bilder-Verzeichnis prompten.
 * 
 * 	Thread startProgram():
 * 			Parallel zum Programmstart.
 * 
 * 	void importPictures(List<File>, Import imprt, String what, File targetDir):
 * 			Importieren intern und extern.
 * 
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_WindowDialog_deprecated extends JPanel implements PM_Interface {
	 
	private int anzahlFuerProgressionBar = 0;	 
	private	JProgressBar progressBar = new JProgressBar(1,5);	 	 	
	private static final PM_WindowDialog_deprecated instance = new PM_WindowDialog_deprecated();
	
	/**
	 * 
	 * Listener:
	 * 	Action:  "max" , maxAnzahl     
	 * 			 null  , inkrement , "text"
	 *
	 */
	static public PM_WindowDialog_deprecated getInstance() {
		return instance;
	}
	
	private PM_ListenerX listener;
	private   Lock lock;             
	private Condition condition; 
 
	// =============================================================
	// Konstruktor
	// =============================================================
	private PM_WindowDialog_deprecated( ) {
		
		lock =  new ReentrantLock();
		condition = lock.newCondition();
		
		listener = new PM_ListenerX() {
			public boolean actionPerformed(PM_Action e) {
				if (e == null) {
					return true;
				}
				
				boolean max =  (e.getObject() == null) ? false :  e.getObject().equals("max");
				int anz = (e.getType() == -1) ? 0 :  e.getType() ;
				String text = (e.getString() == null) ? "" :  e.getString();
				 
				if (max) {
					progressBar.setValue(0);
					progressBar.setMaximum(anz);
					anzahlFuerProgressionBar = 0;
					if (whatText != null) {
	//					whatText.setText(text);
					}
				} else {
					if (fileLabel != null) {
						fileLabel.setText(text);
						 
					}
					anzahlFuerProgressionBar += anz;
				    progressBar.setValue(anzahlFuerProgressionBar);
				}
				return true;
			}
		};
	}
	
	/**
	 * getListener()
	 */
	public PM_ListenerX getListener() {
		return listener;
	}
	
	/**
	 * startProgram()
	 * 
	 * W�hrend der Startphase.
	 * 
	 * Modalen Dialog in einem Thread mit Progression Bar anzeigen bis GUI
	 * initialisiert und show aufgerufen. Im Progression bar wird das �ffnen der
	 * IndexFiles angezeigt, ggf lucene neu erstellt ...
	 * 
	 */
	 
	private JDialog dialog; 
	public PM_WindowDialog_deprecated startProgram( ) {
		Thread startThread = new Thread() {
			public void run() {
				dialog = getDialogPanel(null, 0,0,0);
				continueButton.setEnabled(false);
				continueButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

					}
				});
				stopButton.setEnabled(false);
				stopButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

					}
				});

				int w = 300;
				int h = 200;
				Dimension screen = PM_Utils.getScreenSize();
				int x = screen.width / 2 - w / 2;
				int y = screen.height / 2 - h / 2;
				// dialog.setSize(w, h);
				dialog.setLocation(x, y);

				dialog.pack();
				lock.lock();
				condition.signal();
				lock.unlock();
				dialog.setVisible(true);
			}
		};
		
		lock.lock();	
		
		startThread.start();
		
		try {
			condition.await();
		} catch (InterruptedException e) {
		} finally {
			lock.unlock();
		}
		
		return this;
	}
	
	/**
	 * create a picture.
	 */
	public PM_Picture createPicture(File file) {	
		PM_Picture pic = PM_Picture.getPicture(file);
		if ( ! PM_Utils.hasFileThumbnail(file)) {
			pic.getImageThumbnail(false); // damit wird ein jpeg-File erzeugt					
		}	 
		return pic;
	}
	
	
	/**
	 * stop
	 */
	public void stop() {
		if (dialog != null) {
 
			dialog.dispose();
		}
	}
 
	 
	
  
	/**
	 * 
	 *
	 */
	 
	private JLabel headerLabel;
	private JTextArea whatText;
	private JLabel fileLabel;
	private JButton continueButton;
	private JButton stopButton;
	private JTextArea infoText;
	private JRadioButton doubleAll = new JRadioButton();
	private JRadioButton doubleNotAll = new JRadioButton();
	private JRadioButton doubleDelete = new JRadioButton();
	

	
	 
	private JDialog getDialogPanel(Import imprt ,  // null Startphase
			int newPictureFileSize, int newDoubleSize,   int newInStockSize) {

		
 	
		boolean doubleButton = (newDoubleSize + newInStockSize == 0) ? false: true;
		
		int dimX = 500;
		// --------------------------------------
		// �berschrift
		// -------------------------------------
		JPanel headerPanel = new JPanel();
		headerPanel.setPreferredSize(new Dimension(dimX, 40));
	    headerLabel = new JLabel("           Start Photo Manager");
		Font font = headerLabel.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, 20);
		headerLabel.setFont(fontBold);	
		headerLabel.setPreferredSize(new Dimension(dimX, 40));
		headerPanel.add(headerLabel, BorderLayout.CENTER);
		
		// -------------------------------------
		// whatPanel
		// ------------------------------------
		JPanel whatPanel = new JPanel();
		whatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));	
		String what;
		if (imprt == Import.EXTERN) {
			what = "Bilder extern zu importieren";
		} else if (imprt == Import.INTERN) {
			what = "Bilder intern zu importieren";
		} else {
			what = "Start Photo-Manager";
		}
	    whatText = new JTextArea(what);	 
	    whatText.setPreferredSize(new Dimension(dimX, 30));
	    
	    font = whatText.getFont();
		fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		whatText.setFont(fontBold);	
		whatText.setBackground(headerPanel.getBackground());
	    
	    
		whatPanel.add(whatText);
		
		// --------------------------------------
		// File-output
		// -------------------------------------
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		filePanel.setPreferredSize(new Dimension(dimX, 35));
		fileLabel = new JLabel("            ");	
		filePanel.add(fileLabel);
		
		// ---------------------------------------
		// Progression
		// ---------------------------------------
		JPanel progressionPanel = new JPanel();
		progressionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		progressBar = new JProgressBar( );
		progressBar.setPreferredSize(new Dimension(dimX , 20));	
		progressionPanel.add(progressBar);
		
		// ---------------------------------------
		// Hinweis
		// ---------------------------------------
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));			
	    infoText = new JTextArea("   ");	 
		infoText.setPreferredSize(new Dimension(dimX, 120));
		infoPanel.add(infoText);
		
		// ----------------------------------------------
		// Radiobuttons zur Entscheidung ob l�schen
		// ----------------------------------------------
		
		JPanel doublePanel = new JPanel();
		if (doubleButton) {
			

			ButtonGroup group = new ButtonGroup();
			
			
			doublePanel.setLayout(new BoxLayout(doublePanel, BoxLayout.Y_AXIS));

			int diff = newPictureFileSize - newDoubleSize - newInStockSize;
			 

			// -----  alle importieren
			doubleAll = new JRadioButton("alle " + newPictureFileSize
					+ " Bilder importieren");
			doublePanel.add(doubleAll);
			group.add(doubleAll);
			// -----  L�schen und nicht vorhandene importieren
			String anz = String.valueOf(newDoubleSize) + " + "
						+ String.valueOf(newInStockSize);			 
			doubleDelete = new JRadioButton(anz
					+ " vorhandene UNWIDERRUFLICH l�schen, " + diff
					+ " importieren, ");
			doublePanel.add(doubleDelete);
			group.add(doubleDelete);
			// ------ Nur die nicht vorhandenen (nicht 
			if (diff > 0) {
				doubleNotAll = new JRadioButton("nur die " + diff
						+ " nicht vorhandenen Bilder importieren");
				doublePanel.add(doubleNotAll);
				group.add(doubleNotAll);
				doubleNotAll.setSelected(true);
			}  else {
				doubleAll.setSelected(true);
			}		
		}
			
		// --------------------------------------------
		// Button
		// -------------------------------------------
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		continueButton = new JButton(PM_MSG.getMsg("continue"));
		stopButton = new JButton(PM_MSG.getMsg("exit"));
		buttonPanel.add(continueButton);
		buttonPanel.add(stopButton);

		// -----------------------------------
		// fertig, alles zusammen
		// -----------------------------------
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.add(headerPanel);
		dialogPanel.add(whatPanel);
		dialogPanel.add(filePanel);
		dialogPanel.add(progressionPanel);
		dialogPanel.add(infoPanel);
		if (doubleButton) {
			dialogPanel.add(doublePanel);
		}
		dialogPanel.add(buttonPanel);
		
		JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);
		dialog.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		 		 				
		return dialog;
	}
	
	 
	 
	
 
	
	 
	
	
	
}  
