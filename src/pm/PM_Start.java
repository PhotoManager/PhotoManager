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
 
package pm;

import pm.utilities.*;
import pm.gui.*;
import pm.inout.*;
import pm.picture.*;
import pm.search.*;

import gnu.getopt.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
  
/**
 * Main class of photo manager.
 * <p>
 * PhotoManager is a single-launch application. To find out another program instance
 * PM use the "single-launch application pattern" (see o'reily "Swing Hack" #84).
 * <p>
 * When start up the application create a server socket. If the creation failed then another
 * instance is running otherwise it is the first instance. 
 * <p>
 * When <b>first instance</b> it creates a thread and wait for a socket connection.
 * Then in 'firstMain' reads the meta data and set up
 * the grafical user interface.
 * <p>
 * When <b>another instance</b> (the server socket creation failed) it connect to the
 * running first instance and send over the local socket connection his arguments
 * and terminate. The first instance read and process the arguments.  
 * <pre>
 *   main(args) --> launch(args) --> firstMain(args)  // first instance
 *                               --> relaunch(args)   // another instance
 * </pre>
 * 
 */
public class PM_Start implements PM_Interface, Runnable {

	private static final int PORT = 37683;
	private ServerSocket server;
	private static PM_Start pmStart;
	private static PM_Configuration config;
	private static PM_Demon demon;
	private static boolean open = false; 
 
	
	/**
	 * Starting point of first application instance.
	 * 
	 * The main method has identified this as first appliction.
	 * (no other program instance is running).
	 * <p>
	 * The start up take place: all meta data are reading
	 * and the grafical user interface (swing) is created.
	 * <p>
	 * Normaly the sub window "Search" will be displayed.
	 * 
	 */
	public static void firstMain(String[] args) {

		
		// first the demon to create the socket connection.
		demon = new PM_Demon();
					
		PM_MSG.setResourceBundle(new Locale("en"));

		String msg = "PM-Version = " + PM_Utils.getPmVersion() + "/"
				+ PM_Utils.getDateCompiled();
		System.out.println(msg);
		System.out.print("firstMain: args = ");
		for (String s : args) {
			System.out.print(" " + s);
		}
		System.out.println();

		config = PM_Configuration.init(args);
		
		
		PM_MSG.setAndFileUIManager();
		// now the demon can get the TLPD
		demon.initConfigDone();
		
		
		
		if (!PM_Configuration.getInstance().getBatch()) {

			UIManager.LookAndFeelInfo[] lafs = UIManager
					.getInstalledLookAndFeels();
			for (UIManager.LookAndFeelInfo laf : lafs) {
				System.out.println("lookandfeel: " + laf);
			}

			try {

				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				UIManager
						.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (Exception e) {

				System.out.println("set Look and Feel: ERROR: " + e);
			}

		}
		
		
		// create a temp file to mark that the application is running.
		// (this is only for the backup mode; you cannot run a 
		//  backup if there is the remote application running)
		File temp = new File(config.getPathMetadatenRoot() + File.separator +  FILE_LOCK); 
		temp.delete();
		try {
			temp.createNewFile();
			temp.deleteOnExit();
		} catch (IOException e) 
		{
			System.out.println("cannot create the temp-lock-file. " + e);
		}
			
		 
		 
		PM_LogFile.getInstance().start();
		System.out.println("Start PM");
		System.out.print("Operating System = ");
		if (PM_Utils.isLinux()) {
			System.out.println("Linux");
		} else {
			System.out.println(PM_Utils.getPropertyString(System
					.getProperties(), "os.name"));
		}

		// open some files  ....
		PM_All_ExternalPrograms.getInstance().init();
		PM_All_InitValues.getInstance().init();

		// start the program with a modale dialog
		PM_WindowDialog_deprecated startDialog = null;
		PM_ListenerX dialogListener = null;
		if (!config.getBatch()) {
			startDialog = PM_WindowDialog_deprecated.getInstance()
					.startProgram();
			dialogListener = startDialog.getListener();
		}

		// open the files for the session
		PM_XML_File_Session.getInstance().open();
		PM_SequencesInout.getInstance().open();
		PM_DatabaseLucene.getInstance().open();
		PM_MetadataContainer.getInstance().initialize(dialogListener);
		PM_DatabaseLucene.getInstance().flush();
		// ggf. Lucene neu erstellen
		if (PM_DatabaseLucene.getInstance().getNeuErzeugen()
				|| PM_XML_File_Session.getInstance().indexFilesGeaendert()) {
			PM_DatabaseLucene.getInstance().alleLuceneEintraegeNeuErstellen(
					dialogListener);
		}
		if (startDialog != null) {
			startDialog.stop();
		}

		
		
		// you start the program in batch mode
		if (config.getBatch()) {
			System.out.println("PM: batch (ready for use)");
			
			
			
			
			
			demon.setReadyForUse(true); 
			open = true;
			// Shutdown Hook 
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.out.println("Start shutdown hook");
					if (open) {
						PM_MetadataContainer.getInstance().close();
						PM_DatabaseLucene.getInstance().close();
						PM_SequencesInout.getInstance().close();
						PM_All_ExternalPrograms.getInstance().close();
						PM_XML_File_Session.getInstance().close();
						config.close();
						System.out.println("Closed (batch) in shutdown hook");
						open = false;
					}
				}
				
			});
			
			
			
			while (!demon.getStop()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
	//			System.out.println("Demon running");
			}
			
			
			
			// close batch
			PM_MetadataContainer.getInstance().close();
			PM_DatabaseLucene.getInstance().close();
			PM_SequencesInout.getInstance().close();
			PM_All_ExternalPrograms.getInstance().close();
			PM_XML_File_Session.getInstance().close();
			config.close();
			open = false;
			
			
			
			System.out.println("PM normal end (batch)");
			System.exit(0);
		}

		// check the integrity of meta-data in a modale dialog
		PM_WindowDialog_deprecated startDialogGUI = PM_WindowDialog_deprecated
				.getInstance().startProgram();
		PM_ListenerX dialogListenerGUI = startDialogGUI.getListener();

		// The meta-data are checked
		// Now open the grafical user interface 
		// Not yet close the startDialog 
		PM_WindowMain windowMain = new PM_WindowMain();
		windowMain.init(dialogListenerGUI);
		
		// Now the GUI is up. Close the StartDialog.
		windowMain.setVisible(true);
		if (startDialogGUI != null) {
			startDialogGUI.stop();
		}

		
		// -------------------------------------------------------------
		// Now check if there are pictures to import from external.
		// If there are something to import do it now.
		// -------------------------------------------------------------
		List<File> externalImportFiles = PM_Configuration.getInstance()
				.getImportFiles();
		PM_WindowImport.addImportFiles(externalImportFiles );	 
		// Check if there are new pictures to import (external and internal).
		if (!externalImportFiles.isEmpty()) {
			// New pictures found to import external.
			// --> select the sub-window "Import" (not yet import)
			windowMain.getWindowRechts().doStartExternalImport();
		} else {
			// ----------------------------------------------------------
			// If there are nothing to import from external
			//  check if there are something to import from internal
			// ----------------------------------------------------------
			List<File> internalImportFiles = PM_MetadataContainer.getInstance()
					.getPicturesWithoutThumbs(null);
			if (internalImportFiles.size() > 0) {

				// New pictures found to import internal (under the TLPD)
				// --> Start a modale dialog and import the new pictures
				
				PM_ImportInternal iInternal = new PM_ImportInternal( );
				iInternal.checkAndImport();
			}
		}

		// now the application is ready for use
		demon.setReadyForUse(true);

	}

	 
	/**
	 * The applications main method. 
	 * <p>
	 * Before initialization the program check out 
	 * if there are another program instance is running.
	 */
	public static void main(String[] args) {
		pmStart = new PM_Start();
		pmStart.launch(args);

	}

	 
	/**
	 * Here the appliction find out if there are another program instance running.
	 * <p>
	 * Create a server socket. If creation fails (throw an IOException)
	 * there are another program instance running (call method 'relaunch').
	 * Otherwise this is the only (first) instance (call method 'firstMain')
	 */
	public void launch(String[] args) {

		try {
			server = new ServerSocket(PORT);
			new Thread(this).start();
			// No other program instance is running.
			// Now I start the program.
			firstMain(args);
		} catch (IOException e) {

			// There are another program instance running.
			
			
			
			
			relaunch(args);

		}

	}

	
	/**
	 * Wait for a connection of another program instance.
	 * <p>
	 * The original instance has successfully created a server socket
	 * and wait here for connection.
	 * The connection take place if another instance is running.
	 * <p>
	 * The 'args' of the other instance are reading
	 * Then call 'otherMain' to analyze 'args'. 
	 */
	public void run() {
		// System.out.println("waiting for a connection");
		while (true) {
			try {
				// wait for a socket connection
				Socket sock = server.accept();

				// read the contents into a string buffer
				InputStreamReader in = new InputStreamReader(sock
						.getInputStream());
				StringBuffer sb = new StringBuffer();
				char[] buf = new char[256];
				while (true) {
					int n = in.read(buf);
					if (n < 0) {
						break;
					}
					sb.append(buf, 0, n);
				}
				// split the string buffer into strings
				String[] results = sb.toString().split("\\n");
				// call other main
				otherMain(results);
			} catch (IOException ex) {
				System.out.println("ex: " + ex);
				ex.printStackTrace();
			}
		}
	}


	/**
	 * There are running another program instance.
	 * 
	 * Open a socket to the running program instance
	 * and transfer the "args" to the first instance.
	 * 
	 * The running instance is waiting for connection in 
	 * method "run()".
	 * There the args are reading and calling otherMain().
	 */
	public void relaunch(String[] args) {

		// test if batch
		 
		int c;
		Getopt g = new Getopt("photo-manager", args, "n::");
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'n':    // batch
				System.out.println("====================================");
				System.out.println("PM is yet running.");	 
				System.out.println("====================================");
				System.exit(0);
			}
		}
		
		
		// transfer the "args" to the running (first) instance
		try {
			// open a socket to the original instance
			Socket sock = new Socket("localhost", PORT);

			// write the args to the output stream
			OutputStreamWriter out = new OutputStreamWriter(sock
					.getOutputStream());
			for (int i = 0; i < args.length; i++) {
				out.write(args[i] + "\n");
			}
			// cleanup
			out.flush();
			out.close();
		} catch (Exception ex) {
			System.out.println("ex: " + ex);
			ex.printStackTrace();
		}
		
		// The running program instance received the "args".
		// Now terminate this (just started) program instance.
		System.exit(0);
	}

 
	/**
	 * The running (first) instance received some "args".
	 * 
	 * This is the running instance. 
	 * If another instance are launched you get here the args
	 * of the (just started) instance (over a socket connection).
	 * <p>
	 * Here you check the args. If there are something to import from "external"
	 * you transfer the data into the  sub-window "Import".
	 * 
	 */
	public void otherMain(final String[] args) {
		System.out.print("otherMain: args = ");
		for (String s : args) {
			System.out.print(" " + s);
		}
		System.out.println();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				List<File> importFiles = new ArrayList<File>();
				Getopt g = new Getopt("testprog", args, "e:b:i:");
				int c;
				while ((c = g.getopt()) != -1) {
					if (c == 'i') {
						String arg = g.getOptarg();
						if (arg != null) {
							// I found pictures to import
							importFiles.add(new File(arg));
						}
						break;
					}
				}

				// check if there are values WITHOUT options
				for (int i = g.getOptind(); i < args.length; i++) {
					importFiles.add(new File(args[i]));
				}

				if (importFiles.isEmpty()) {
					JOptionPane.showConfirmDialog(null,
							"PM is yet running.", "Error",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					// Enable the import button in the sub-window "Import"
					// and transfer the data.
					if (PM_WindowImport.addImportFiles(importFiles)) {
						PM_WindowImport.setTimer();
						String msg = "Ein EXTERNER Bilder-Import wurde erkannt."
								+ "\nWechseln Sie ins Fenster \"Import\" und"
								+ "\nbet�tigen Sie dort den Button \"Extern importieren\"";
						JOptionPane.showConfirmDialog(null, msg,
								"Import extern", JOptionPane.DEFAULT_OPTION,
								JOptionPane.INFORMATION_MESSAGE);
					}
					
				}

			}
		});

	}

}
