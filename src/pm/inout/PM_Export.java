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
package pm.inout;

import pm.utilities.*;
import pm.gui.*;
import pm.picture.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.text.*;

/**
 * The base class to export pictures
 * 
 * You can export pictures changed, unchanged, with or without metadata.
 * 
 */
public class PM_Export implements PM_Interface, PropertyChangeListener {

	protected PM_WindowMain windowMain;
	protected File tempPictureDirectory = null;

	protected boolean neuerBildName = false;
	protected String baseName = "";
	protected int pictureNumber = 1;

	private File dirThumbnails = null;
	private PM_IndexFile xmlIndexFile = null;

	protected PM_WindowExport windowExport;

	protected boolean withMetadata = false;

	protected ProgressMonitor progressMonitor;
	protected Task task;

	private List<PM_Picture> pictures;
	
	public PM_Export(PM_WindowMain windowMain, PM_WindowExport windowExport) {
		this.windowMain = windowMain;
		this.windowExport = windowExport;
	}

	public void setBildName(String bildName) {
		this.baseName = bildName;
		neuerBildName = true;
	}

	public void setStartLfdBildNummer(int lfdNrBildName) {
		this.pictureNumber = lfdNrBildName;
		neuerBildName = true;
	}

	public void setMitMetadaten(boolean mitMetadaten) {
		this.withMetadata = mitMetadaten;
	}
	
	public void setPicturesToExport(List<PM_Picture> pictures) {
		this.pictures = pictures;
	}

	
	
	/**
	 * write pictures to the temp directory.
	 */
	public void doExport( ) {
		progressMonitor = new ProgressMonitor(windowExport,
				"Write out the pictures", "", 0, pictures.size());
		executeTask();
		return;
	}

	protected void executeTask() {
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToDecideToPopup(1 );
		progressMonitor.setMillisToPopup(1 );
		task = new Task( );
		task.addPropertyChangeListener(PM_Export.this);
		task.execute();
	}
	
	
	private int getAnzahlZuExportieren() {
		if (windowExport.getIndex() == null)
			return 0;
		return windowExport.getIndex().controller.sizeDargestellt();
	}

	// =====================================================
	// getTempDirFile()
	//
	// Holen Temp-Directory, in das alle Bilder geschrieben werden.
	//
	// z.B. am 11.3.2002 um 12:33 geschrieben
	// c:\\temp\\PhotoManager\\pm_11032002_1233
	// =====================================================
	private File createTemporaryDirectory() {
		PM_Configuration einstellungen = PM_Configuration.getInstance();
		File fileTempDir = einstellungen.getFileHomeTemp();

		// Directory "PhotoManager" einrichten, in das exportiert wird
		File filePM = new File(fileTempDir.getPath() + File.separator
				+ "PhotoManager");
		filePM.mkdirs();

		// darunter Directory mit Datum+Uhrzeit einrichten
		// und diesen Namen zurueckgeben
		Date date = new Date(System.currentTimeMillis());
		;
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		File fileBiler = new File(fileTempDir.getPath() + File.separator
				+ "PhotoManager" + File.separator + "pm_" + df.format(date));
		// System.out.println("-------- getTempDir. makedir mit = " +
		// tempDir + File.separator + "PhotoManager" + File.separator + "pm" +
		// df.format(date));
		fileBiler.mkdir();
		// System.out.println("-------- getTempDir. okay");
		return fileBiler;
	}

	private int schreibenAlleBilder( ) {
		tempPictureDirectory = createTemporaryDirectory();
		open();
		if (withMetadata) {
			openMetadaten();
		}

		// now export all the pictures
		int pictureCount = 0;
		for (PM_Picture picture : pictures) {

			if (progressMonitor.isCanceled()) {
				break;
			}

			pictureCount++;
			File fileOut = getFileOriginalOut(picture);
			progressMonitor.setNote(fileOut.getName());
			writePicture(picture, fileOut, pictureCount, pictures.size());

			if (withMetadata) {
				schreibenMetadaten(picture, fileOut, pictureCount,
						pictures.size());
			}
			task.setTheProgress(pictureNumber);
			pictureNumber++;
		}
		// export done
		close();
		if (withMetadata) {
			xmlIndexFile.writeDocument();
			xmlIndexFile = null;
		}

		return pictureCount;
	}

	// =====================================================
	// open()
	//
	// Verzeichnisse anlegen:
	//
	// .../bilder_dir/bild1.jpg
	// /bild2.jpg
	// ...
	// /bildn.jpg
	// /pm.metadaten/pm_index.xml
	// /pm.thumbnails/bild1.jpg_th
	// /pm.thumbnails/bild2.jpg_th
	// ...
	// /pm.thumbnails/bildn.jpg_th
	// =====================================================
	private void open() {
		// Bilder-Dir = tempBilderDirectory
		File dirMetadaten = new File(tempPictureDirectory.getPath()
				+ File.separator + DIR_METADATEN);
		dirMetadaten.mkdirs();
		dirThumbnails = new File(dirMetadaten.getPath() + File.separator
				+ DIR_THUMBNAILS);
		dirThumbnails.mkdirs();

		File filePmIndex = new File(dirMetadaten.getPath() + File.separator
				+ FILE_INDEX_XML);
		xmlIndexFile = new PM_IndexFileXML(filePmIndex);

	}

	private void close() {
		xmlIndexFile.writeDocument();
		// jetz Instanz vernichten um Platz zu machen
		xmlIndexFile = null;

	}

	protected boolean copyFile(File in, File out) {

		BufferedInputStream bufin = null;
		BufferedOutputStream bufout = null;

		long inLastModified = in.lastModified();

		try {

			FileInputStream fis = new FileInputStream(in);
			bufin = new BufferedInputStream(fis);

			FileOutputStream fos = new FileOutputStream(out);
			bufout = new BufferedOutputStream(fos);

			byte[] buffer = new byte[1024];
			int len;
			while (bufin.available() != 0) {
				len = bufin.read(buffer);
				bufout.write(buffer, 0, len);
			}
			;
		} // try
		catch (FileNotFoundException fnfe) {
			return false;
			// System.out.println(in.getPath() + " does not exist!");
		} catch (IOException ioe) {
			// System.out.println("Error reading/writing files!");
			return false;
		} finally {
			try {
				if (bufin != null)
					bufin.close();
				if (bufout != null) {
					bufout.close();
					out.setLastModified(inLastModified);
				}
			} catch (IOException ioe) {
				return false;
			}
		}

		return true;
	}

	protected boolean writePicture(PM_Picture picture, File fileOut, int nr,
			int bilderGes) {
		return false;
	}

	// =====================================================
	// getFileOriginalOut()
	//
	// an diese Stelle wird das Originalbild kopiert.
	// (auch f�r export mit Metadaten)
	//
	// DIE ID WIRD GGF. GEAENDERT:
	// Falls mit "neuem Bildnamen" wird dieser hier eingetragen.
	// Falls Datei bereits vorhanden, wird die Datei hier EINDEUTIG gemacht.
	// --> _xxxx anhaengen
	// =====================================================
	protected File getFileOriginalOut(PM_Picture picture) {

		String fileNameOut = tempPictureDirectory.getPath() + File.separator;

		if (neuerBildName) {
			fileNameOut += getNeueBildId(picture.getFileOriginal().getName(),
					pictureNumber);
		} else {
			fileNameOut += picture.getFileOriginal().getName();
		}

		File fileOut = new File(fileNameOut);

		// make fileOut unique
		while (fileOut.canRead()) {
			String filePathName = fileOut.getPath();
			String extension = PM_Utils.getExtension(filePathName);
			String ohneExt = PM_Utils.getWithoutExtension(filePathName);
			if (ohneExt.indexOf("_x") > 0) {
				filePathName = ohneExt + "x";
			} else {
				filePathName = ohneExt + "_x";
			}
			fileOut = new File(filePathName + "." + extension);
		}
		;
		return fileOut;
	}

	// =====================================================
	// getNeueBildId()
	//
	// Liefert die neue Bild-Id mit
	// neuem Bildnamen + lfd Nr + Extension
	//
	// (die lfd-Nr wird nich erhoeht)
	// =====================================================
	private String getNeueBildId(String idAlt, int number) {

		String lfdNr = PM_Utils.stringToString(
				"000" + Integer.toString(number), 3);
		if (number > 999)
			lfdNr = Integer.toString(number);

		String neueBildId = baseName + lfdNr + "."
				+ PM_Utils.getExtension(idAlt);
		return neueBildId;
	}

	// =====================================================
	// abschlussMeldung
	// =====================================================
	protected void abschlussMeldung(int numberExported) {

		// String message = anzahlBilder
		// // + " Bilder erfolgreich in das Verzeichnis \n"
		// + tempBilderDirectory.getPath() + "\n" + "geschrieben";

		String message = String.format(PM_MSG.getMsg("expEnd"), numberExported,
				tempPictureDirectory.getPath());

		JOptionPane.showConfirmDialog(null, message, "Export",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

	}

	 

	private void openMetadaten() {
		// Bilder-Dir = tempBilderDirectory
		File dirMetadaten = new File(tempPictureDirectory.getPath()
				+ File.separator + DIR_METADATEN);
		dirMetadaten.mkdirs();
		dirThumbnails = new File(dirMetadaten.getPath() + File.separator
				+ DIR_THUMBNAILS);
		dirThumbnails.mkdirs();

		File filePmIndex = new File(dirMetadaten.getPath() + File.separator
				+ FILE_INDEX_XML);
		xmlIndexFile = new PM_IndexFileXML(filePmIndex);

	}

	protected boolean schreibenMetadaten(PM_Picture picture, File origOut,
			int nr, int bilderGes) {

		// Original-File
		File origIn = picture.getFileOriginal();

		// Thumbnail
		File thumbIn = PM_Utils.getFileThumbnail(origIn);
		File thumbOut = PM_Utils.getFileThumbnail(origOut);
		copyFile(thumbIn, thumbOut);

		// extern bearbeitete Bilder
		File[] files = PM_Utils.getFilesBearbeitet(picture.getFileOriginal());
		if (files.length > 0) {
			// Directory anlegen falls noch nicht angelegt
			File dirBilderBearbeitet = new File(tempPictureDirectory.getPath()
					+ File.separator + DIR_METADATEN + File.separator
					+ DIR_BILDER_BEARBEITEN);
			dirBilderBearbeitet.mkdirs();
			for (int i = 0; i < files.length; i++) {
				File fileIn = files[i];
				File fileOut = new File(dirBilderBearbeitet.getPath()
						+ File.separator + fileIn.getName());
				copyFile(fileIn, fileOut);
			}
		}

		// Metadaten in das neue pm_index.xml �bernehmen
		xmlIndexFile.update(picture, origOut.getName());

		return true;

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
System.out.println("+++++ propertyChange ++++: Value: " + progress);
			progressMonitor.setProgress(progress);
			String message = String.format("%d%% exportiert.\n", progress);
//			progressMonitor.setNote(message);
			// taskOutput.append(message);
			if (progressMonitor.isCanceled() || task.isDone()) {

			//	Toolkit.getDefaultToolkit().beep();
				if (progressMonitor.isCanceled()) {
					// //////////////////////// task.cancel(true);
					// taskOutput.append("Task canceled.\n");
				} else {
					// taskOutput.append("Task completed.\n");
				}
				// startButton.setEnabled(true);
			}
		}

	}
	
	
	protected void taskDoInBackground() {
		numberExported = schreibenAlleBilder();	
	}
	
	protected void taskDone() {
		if (progressMonitor.isCanceled()) {

			String message = "Abbruch: Es wurden von " + pictures.size()
					+ " nur " + numberExported + " Bilder exportiert";

			JOptionPane.showConfirmDialog(null, message, "Export",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);

			System.out.println("---xxxxxxxx progressMonitor is canceled ");
			return;
		}
		abschlussMeldung(numberExported);
	}
	
	private int numberExported = 0;

	class Task extends SwingWorker<Void, Void> {
	 
		

		public Task( ) {
			 
		}

		@Override
		public Void doInBackground() {
			taskDoInBackground();
			if (progressMonitor.isCanceled()) {
				System.out.println("---xxxxxxxx progressMonitor is canceled ");
				task.cancel(true);
			}
			
			return null;
		}

		@Override
		public void done() {
			System.out.println("---SwingWorker done done DONE ");
			progressMonitor.close();
			taskDone();
			
		}

		public void setTheProgress(int n) {
			setProgress(n);
		}
		
		 
	}

} // Ende Klasse
