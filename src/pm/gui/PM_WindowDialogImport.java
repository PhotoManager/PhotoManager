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
import pm.utilities.PM_Interface.Import;
 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.locks.*;
 

import javax.swing.*; 






public class PM_WindowDialogImport {

	private JDialog dialog; 
 
	
	  
	private	JProgressBar progressBar = new JProgressBar(1,5);
	
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
 
	
	private JCheckBox enableDouble;
	
	private   Lock lock;             
	private Condition condition; 
 
	 
	private PM_ListenerX listener;
	private boolean stop = false;
	
	private Import imprt;
	
	
	private static File targetDir = null;
	
	// ==================================== 
	// Konstruktor
	// ==================================== 
	public PM_WindowDialogImport(Import imprt) {
		this.imprt = imprt;
		lock =  new ReentrantLock();
		condition = lock.newCondition();
		createDialogAndStart();  
		listener =  new PM_ListenerX() {
			public boolean actionPerformed(PM_Action e) {
				Object o = e.getObject();
				int i = e.getType();
				String s = e.getString();
				if (o == null) {
					// Progress bar value +1
					progressBar.setValue(progressBar.getValue() + 1);
					fileLabel.setText(s);
				} else if (o.equals("max")) {
					if (i >= 0) {
						progressBar.setIndeterminate(false);
						setProgressMax(i);
					} else {
						progressBar.setIndeterminate(true);
					}
					fileLabel.setText("");
					if (s.length() > 0) {
						infoText.append(s + "\n");
					}
				}
				if (stop) {
					stop = false;
					return false;
				}
				return true;
			}
		};
		
	}
	
	/**
	 * The target dir to copy pictures from external.
	 * 
	 * This director must be under the Top Level Picture Directory.
	 *  
	 */
	public File getTargetDir() {
		if (targetDir == null) {
			targetDir = getDefaultTargetDir();
		}
		return targetDir;
	}
	
	public PM_ListenerX getListener() {
		return listener;
	}
	
	/**
	 * return the lock for await/signal
	 *  
	 */
	public Lock getLock() {
		return lock;
	}
	
	public void dispose() {
		if (dialog != null) {
			dialog.dispose();
		}
	}
	
	public boolean await() {
		lock.lock();
		try {
			condition.await();
		} catch (InterruptedException e) {
		} finally {
			lock.unlock();
		}
		return stop;
	}
	
	public void setProgressMax(int max) {
		progressBar.setMaximum(max);
		progressBar.setValue(0);
	}
	
	public void setProgressAddValue( ) {
		progressBar.setValue(progressBar.getValue() + 1);
	}
	public void setProgressSetValue(int value ) {
		progressBar.setValue(value);
	}
	public void setEnableContinue(boolean c) {
		continueButton.setEnabled(c);
	}
	public void setEnableStop(boolean s) {
		stopButton.setEnabled(s);
	}
	public boolean isDoublePicSelected() {
		return enableDouble.isSelected();
	}
	
	public void setInfoDoublePic() {
		if (enableDouble.isSelected()) {
			 infoText.append(PM_MSG.getMsg("importDiaDouble") + "\n");
		 } else {
			 infoText.append(PM_MSG.getMsg("importDiaNoDouble") + "\n");
		 }
	}
	
	public void setEnableInfoDoublePic(boolean enable) {
		enableDouble.setEnabled(enable);
	}
	
	public void createDialogAndStart( ) {
		Thread startThread = new Thread() {
			public void run() {
				dialog = getDialogPanel(imprt);
	//			continueButton.setEnabled(false);
				continueButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						stop = false;
						lock.lock();
						condition.signal();
						lock.unlock();
					}
				});
	//			stopButton.setEnabled(false);
				stopButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						stop = true;
						lock.lock();
						condition.signal();
						lock.unlock();
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
	 	
		 
		 
	}
	
	private JTextField targetDirField;
	private JDialog getDialogPanel(Import imprt) {  // null Startphase
		 
		 
		
		int dimX = 500;
		// --------------------------------------
		// �berschrift
		// -------------------------------------
		JPanel headerPanel = new JPanel();
		headerPanel.setPreferredSize(new Dimension(dimX, 40));
	    headerLabel = new JLabel("           Photo Manager");
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
			what = PM_MSG.getMsg("importDiaExternal");
		} else if (imprt == Import.INTERN) {
			what = PM_MSG.getMsg("importDiaInternal");
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
	    infoText = new JTextArea();	 
	    JScrollPane sc = new JScrollPane(infoText);
		infoText.setPreferredSize(new Dimension(dimX, 120));
		infoPanel.add(sc);
		
		// ----------------------------------------------
		// check box  doppelte
		// ----------------------------------------------
		JPanel doublePanel = new JPanel();
		doublePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		enableDouble = new JCheckBox(PM_MSG.getMsg("importDiaEnableDouble"));
		doublePanel.add(enableDouble);
		enableDouble.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				setInfoDoublePic();
			}		
		});  
		
		// -------------------------------------------------
		// Zielverzeichnis w�hlen
		// -------------------------------------------------	
		if (targetDir == null) {
			targetDir = getDefaultTargetDir();
		}
		JPanel target = new JPanel(new FlowLayout(FlowLayout.LEFT));
		target.add(new JLabel("Ziel-Verzeichnis: "));
		targetDirField = new JTextField(targetDir.getPath());
		targetDirField.setEnabled(false);
		targetDirField.setColumns(30);
		target.add(targetDirField);
		JButton targetBrowserButton = new JButton("...");
		ActionListener alTargetDir = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = chooseImportDir(targetDir.getPath());
				if (!PM_Utils.isUnderTLD(file)) {					
//					String text = "Ziel-Verzeichnis ist NICHT unterhalb des TLD's";					
					JOptionPane.showConfirmDialog(null, PM_MSG.getMsg("importDiaNoTLD"), "ERROR",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				targetDirField.setText(file.getAbsolutePath());
				targetDir = file;
			}
		};
		targetBrowserButton.addActionListener(alTargetDir);
		targetBrowserButton.setEnabled(false);
		target.add(targetBrowserButton);
		
		
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
		if (imprt == Import.INTERN) {
			dialogPanel.add(doublePanel);
		} else if (imprt == Import.EXTERN) {
			dialogPanel.add(target);
		}
		dialogPanel.add(buttonPanel);
		
		JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);
//		dialog.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		 		 				
		return dialog;
	}
	
	/**
	 * return default target directory.
	 * 
	 * target directory for the pictures to import.
	 */
	private File getDefaultTargetDir() {
		String path = PM_Configuration.getInstance().getTopLevelPictureDirectory().getPath();

		Date date = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyy");
		path += File.separator + df.format(date);
		df = new SimpleDateFormat("yyyy_MM");
		path += File.separator + df.format(date);
		File f = new File(path);

		f.mkdirs();

		return f;
	}

	/**
	 * 
	 */
	private File chooseImportDir(String dir) {

		// File Chooser
		JFileChooser fc = new JFileChooser(dir);		
		fc.setDialogTitle(PM_MSG.getMsg("importDiaChooseTarget"));
		fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return true;
			}
			
			public String getDescription() {
				return PM_MSG.getMsg("importDiaTLD"); // ??????
			}
		});
		fc.setMultiSelectionEnabled(false);

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(null);

		// return from fileChooser
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = fc.getSelectedFile();
		if (file.isDirectory()) {
			return file;
		}
		return null;
	}

}
