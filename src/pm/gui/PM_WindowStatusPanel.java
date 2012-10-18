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

 
import pm.index.*;
import pm.inout.*;
import pm.picture.*; 
import pm.utilities.*;
 
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import java.awt.*;


@SuppressWarnings("serial")
public class PM_WindowStatusPanel extends JPanel implements // PM_Listener,
		PM_Interface {

	private JDialog dialog;
	private JLabel picStatus = null;
	 
	private JLabel importText = null;
	
	private boolean initDone = false;
	
	private JProgressBar progressionBar = null;
	private PM_Listener metadatenChangeListener;
	private PM_MetadataContainer metadatenContainer;
//	private PM_XML_File_Einstellungen einstellungen;
//	private File homeBilder;
	private static PM_WindowStatusPanel instance = new PM_WindowStatusPanel();

 
	
	// ==========================================================
	// static: getInstance()
	// ==========================================================
	public static PM_WindowStatusPanel getInstance() {
		return instance;
	}

	// ==========================================================
	// init()
	// ==========================================================
	public void init() {
		metadatenContainer = PM_MetadataContainer.getInstance();
//		einstellungen = PM_XML_File_Einstellungen.getInstance();
//		homeBilder = einstellungen.getFileHomeBilder();

		JPanel panelLinks = getPanelLinks();
		JPanel panelRechts = getPanelRechts();

		// links und rechts zusammenf?gen
//		setLayout(new FlowLayout(FlowLayout.LEFT));

		setLayout(new BorderLayout());

		setBackground(Color.yellow);
		add(panelLinks, BorderLayout.WEST);
		add(panelRechts, BorderLayout.EAST);

		initDone = true;
		PM_Listener changeListerer = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// update Anzahl Pictures
				if (e.getObject() == null) {
					updateAnzahlBilder();
				}
			}
		};	
		PM_IndexViewImport.addImportListener(changeListerer);
		PM_DeletePictures.addDeleteListener(changeListerer);
		
		
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
		updateAnzahlBilder( );	
	}

	// ==========================================================
	// updateAnzahlBilder()
	// ==========================================================
	public void updateAnzahlBilder( ) {
		int gesamt = PM_MetadataContainer.getInstance().getPictureSizeValid();	
		int geloescht = PM_MetadataContainer.getInstance().getPictureSizeInvalid();	
		picStatus.setText(String.format(PM_MSG.getMsg("winStatPicSize"), gesamt, geloescht));		
	}
	

 
	
	// ==========================================================
	// progressionBarInit()
	// ==========================================================
	public void progressionBarInit(Object o, int max) {
		progressionBar.setMinimum(1);
		progressionBar.setMaximum(max);
		progressionBar.setValue(0);
	}

	// ==========================================================
	// progressionBarSetValue()
	// ==========================================================
	public void progressionBarSetValue(Object o, final int value) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressionBar.setValue(value);
			}
		});
	}

	// ==========================================================
	// progressionBarStop()
	// ==========================================================
	public void progressionBarStop(Object object) {
		if (dialog != null) {
			dialog.dispose();
		}
		
	}

	// ==========================================================
	// progressionBarStart()
	// ==========================================================
	public void progressionBarStart() {
		dialog = new JDialog(new JFrame(), true);
		dialog.setUndecorated(false);// true);
		dialog.pack();
		dialog.setVisible(true);
	}

	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================
	// ============================ PRIVATE ===================================

	// ==========================================================
	// Konstruktor
	// ==========================================================
	private PM_WindowStatusPanel() {
	}

	// ==========================================================
	// getPanelLeft()
	// ==========================================================
	private JPanel getPanelLinks() {

		// Panel links
		JPanel panel = new JPanel();
		panel.setBackground(Color.yellow);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		picStatus = new JLabel();
		panel.add(picStatus);
		importText = new JLabel();
		panel.add(importText);
		
		
		// Change Listerner fuer Metadatenanederungen hier erzeugen
		metadatenChangeListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// die Metadaten wurden geaendert
				if (e.getObject() instanceof PM_MetadataContainer) {
//					setStatus();
				}
			}
		};
		metadatenContainer.addChangeListener(metadatenChangeListener);

		return panel;
	}

	// ==========================================================
	// getPanelRight()
	// ==========================================================
	private JPanel getPanelRechts() {
		JPanel panel = new JPanel();
		/**
		 * JButton buttonSichern = new JButton("Stopp");
		 * buttonSichern.setLayout(new FlowLayout(FlowLayout.LEFT));
		 * buttonSichern.setBackground(Color.ORANGE); panel.add(buttonSichern);
		 * ActionListener alButtonSichern = new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { // doHitsDarstellen(false); } };
		 * buttonSichern.addActionListener(alButtonSichern);
		 */

		// Progression bar
		progressionBar = new JProgressBar();
		progressionBar.setForeground(Color.RED);
		panel.add(progressionBar);

		return panel;
	}

	 
	// ==============================================================
	//  import timer
	// ==============================================================
	public void startImportTimer() {	
		if (importTimer == null) {
			return;
		}
		importTimer = new Timer();
		importTimer.schedule(new ImportTimerTask(), 0, 1000);
	}
	public void stopImportTimer() {	
		if (importTimer == null) {
			return;
		}
		importTimer.cancel();
		instance.hasNotImport();
		
		 
	}
	
	private Timer importTimer = null;
	
	
	private void hasImport() {
		importText.setText("           Neue Bilder importieren (Arbeitsfenster 'Importieren')");
	}
	private void hasNotImport() {
		importText.setText("");
	}
	//==========================================================
	// Innerclass: ImportTimerTask
	//
	// Wenn in der Statuszeile etwas blinken soll
	// ==========================================================
	static private boolean importToggle = false;
	public class ImportTimerTask extends TimerTask {
		public void run() {
			if (instance.initDone == false) {
				return; 
			}
			if (importToggle) {
				instance.hasImport();
				importToggle = !importToggle;
			} else {
				instance.hasNotImport();
				importToggle = !importToggle;
			}
			 
		}
	}
	
	
}
