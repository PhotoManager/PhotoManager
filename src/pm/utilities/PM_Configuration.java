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
package pm.utilities;

import pm.PM_WindowGetTLPD;
import pm.gui.*;
import pm.inout.PM_XML;
import pm.print.*;

import org.dom4j.*;
import gnu.getopt.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * The configuration file.
 * 
 * A singleton (only one instance) is created.
 * 
 */
public class PM_Configuration extends PM_XML implements PM_Interface {

	private static PM_Configuration instance = null;

	private Locale locale = null;

	private File homeBilder = null;
	private File homeLuceneDB = null;
	private File homeTemp = null;
	private File homeExtProgramme = null;
	private File homeInitValues = null;
	private File homeFileAlbumName = null;
	private File homeICCProfiles = null;
	private List<File> importFiles = new ArrayList<File>();

	private boolean nurLesen = false;

	private final PM_AllSystemPrinter systemPrinters = PM_AllSystemPrinter
			.getInstance();

	// Datum: <datum jahr-von="1998" jahr-bis="2007"/>
	private final String TAG_DATUM = "datum";
	private final String ATTR_DATUM_JAHR_VON = "jahr-von";
	private final String ATTR_DATUM_JAHR_BIS = "jahr-bis";
	private int datumJahrVon = 1998; // default
	private int datumJahrBis = 2011; // default

	private String slideshowText1 = "kategorie"; // default
	private String slideshowText2 = "datum"; // default
	private String slideshowText3 = "serie"; // default

	// Prefetch von Images
	private final String TAG_PREFETCH = "prefetch";
	private final String ATTR_PREFETCH_PLUS = "plus";
	private final String ATTR_PREFETCH_MINUS = "minus";
	private int prefetchPlus = 2;
	private int prefetchMinus = 2;
	private String rootTag = "einstellungen";

	private boolean batch = false;
	private boolean demon = false;

	private boolean mpeg = false;

	// =====================================================
	// Class Method: setFileEinstellungen
	// =====================================================
	static public PM_Configuration init(String[] args) {
		if (instance == null) {
			instance = new PM_Configuration(args);
		}
		return instance;
	}

	/**
	 * Get the instance of the singleton.
	 * 
	 * Only one instance of the class is created.
	 * 
	 */
	static public PM_Configuration getInstance() {
		return instance;
	}

	// Private constructor prevents instantiation from other classes
	private PM_Configuration() {
	}; // damit keiner angelegt wird

	// The private constructor
	private PM_Configuration(String[] args) {

		File fileEinstellungen = null;
		File directoryHomeBilder = null;

		// --------------------------------
		// �bernehmen Start-Parameter
		// --------------------------------
		int c;
		Getopt g = new Getopt("photo-manager", args, "e:b:i:n::d::");
		String arg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'e':
				arg = g.getOptarg();
				fileEinstellungen = (arg == null) ? null : new File(arg);
				break;
			case 'b':
				arg = g.getOptarg();
				directoryHomeBilder = (arg == null) ? null : new File(arg);
				break;
			case 'n': // batch
				batch = true;
				break;
			// case 'd': // daemon und batch
			// demon = true;
			// batch = true;
			// break;
			case 'i':
				arg = g.getOptarg();
				if (arg != null) {
					importFiles.add(new File(arg));
				}
				break;
			}
		}

		// Jetzt noch die ohne Options.Das sind dann Import-Files
		for (int i = g.getOptind(); i < args.length; i++) {
			importFiles.add(new File(args[i]));
		}

		// --------------------------------------------------------------
		// Reihenfolge:
		// 1. a. -e Parameter auswerten
		// b. nigefu: .photo-manager/pm_einstellungen.xml suchen
		// c. nigefu: Locale prompten und .photo-manager/pm_einstellungen.xml
		// neu anlegen
		// 2. a. -b Parameter auswerten
		// b. nigefu: Eintrag in .photo-manager/pm_einstellungen.xml suchen
		// c. nigefu: prompten (ERROR wenn batch)
		// 3. Wenn in .photo-manager/pm_einstellungen.xml
		// Bilder-Dir nicht eingetragen, dann dort eintragen.
		// (wenn vorhanden, nicht �berschreiben)
		// ------------------------------------------------------------------

		// --------------------------------------------------------------------
		// (1) pm_einstellungen.xml und locale ermitteln
		// --------------------------------------------------------------------
		if (fileEinstellungen != null && fileEinstellungen.isFile()) {
			// (1.a.) -e Parameter vorhanden:
			// open und lesen locale
			xmlFile = fileEinstellungen;
			openDocument(OPEN_READ_ONLY);
			locale = getLocale();
			if (locale == null) {
				locale = (new PM_WindowDialogGetLocale().getLocale());
				setLocale(locale);
				writeDocument();
			}
		} else {
			// (1.b.) -e nicht angegeben
			fileEinstellungen = new File(PM_Utils.getConfigDir()
					+ File.separator + FILE_EINSTELLUNGEN);
			if (fileEinstellungen.isFile()) {
				// (1.b) in .photo-manager/pm_einstellungen.xml Datei gefunden
				xmlFile = fileEinstellungen;
				openDocument(OPEN_READ_ONLY);
				locale = getLocale();

				if (locale == null) {
					locale = (new PM_WindowDialogGetLocale().getLocale());
					setLocale(locale);
					writeDocument();
				}
			} else {
				// pm_einstellungen.xml nigefu:
				// locale prompten und pm_einstellungen neu anlegen
				locale = (new PM_WindowDialogGetLocale().getLocale());
				xmlFile = fileEinstellungen;
				rootTagName = rootTag;
				openDocument(OPEN_CREATE);
				setLocale(locale);
				writeDocument();
			}

		}

		// ---------------------------------------------------------------
		// (2) Bilder Dir ermitteln
		// ---------------------------------------------------------------

		if (directoryHomeBilder != null && directoryHomeBilder.isDirectory()) {
			// --- es wurde -b <top level directory> angegeben
			homeBilder = directoryHomeBilder;
			setHomeBilder(homeBilder.getPath());
			writeDocument();
		} else {
			// jetzt muss homeBilder aus der xml-Datei gelesen werden.
			// Wenn nigefu., dann prompten und eingtragen
			homeBilder = getHomeFile();
			if (homeBilder == null || homeBilder.isDirectory() == false) {
				if (batch) {
					System.out.println("ERROR: batch kein TLPD gefunden");
					System.out.println("abnormal end");
					System.exit(0);
				}
				PM_MSG.setResourceBundle(locale);
				PM_WindowGetTLPD sp = new PM_WindowGetTLPD();
				homeBilder = sp.getResult();
				if (homeBilder == null) {
					setLocale(locale);
					writeDocument();
					System.out.println("abnormal end (no TLPD)");
					System.exit(0);
				}
				setLocale(locale);
				setHomeBilder(homeBilder.getPath());
				writeDocument();
			}
		}

		// -----------------------------------------------------
		// Jetzt gibt es:
		// ein korrektes xmlFile mit einem homeBilder Eintrag
		// homeBilder ist korrekt versorgt
		// --------------------------------------------------------

		// System.out.println("Locale = "+ locale);
		PM_MSG.setResourceBundle(locale);

		setAllPrinter();
		setPrinterFormat();
		setDateFromTo();
		setPrefetch();
		setSlideshow();
		setBackup();
		setSequences();
		setMpeg();

		// homeLuceneDB in -e nicht eingetragn
		if (homeLuceneDB == null) {
			homeLuceneDB = new File(homeBilder.getPath() + File.separator
					+ DIR_METADATEN_ROOT + File.separator + DIR_LUCENE_DB);
			homeLuceneDB.mkdirs();
		}

		// Temp-Dir nicht vorhanden
		if (homeTemp == null) {
			homeTemp = new File(homeBilder.getPath() + File.separator
					+ DIR_PM_TEMP); // "pm_temp");
			homeTemp.mkdirs();
		}

		// Externe-Programme nicht vorhanden
		if (homeExtProgramme == null) {
			homeExtProgramme = new File(PM_Utils.getConfigDir()
					+ File.separator + FILE_EXTERNE_PROGRAMME);
		}

		// InitValues nicht vorhanden
		if (homeInitValues == null) {
			homeInitValues = new File(PM_Utils.getConfigDir() + File.separator
					+ FILE_INIT_VALUES);
		}

	}

	/**
	 * Write back the configuation file.
	 * 
	 */
	public void close() {
		writeDocument();
	}

	/**
	 * Get monitor resoluton (hard coded)
	 * 
	 */
	public float getMonitorResolution() {
		return 91.3F; // for Notebook;
	}

	/**
	 * Get printer resoluton (hard coded)
	 * 
	 */
	public float getPrinterResolution() {
		return 72.0F;
	}

	/**
	 * Get import files for an external import.
	 * 
	 * The files from start arguments -i and files without options.
	 */
	public List<File> getImportFiles() {
		return importFiles;
	}

	/**
	 * Check if batch mode
	 * 
	 * @return
	 */
	public boolean getBatch() {
		return batch;
	}

	public boolean isNurLesen() {
		return nurLesen;
	}

	// =====================================================
	// getDatum ....
	// =====================================================
	public int getDatumJahrVon() {
		return datumJahrVon;
	}

	public int getDatumJahrBis() {
		return datumJahrBis;
	}

	// =====================================================
	// getFile ....
	// =====================================================
	public File getTopLevelPictureDirectory() {
		return homeBilder;
	}

	public File getFileHomeLuceneDB() {
		return homeLuceneDB;
	}

	public File getFileHomeTemp() {
		return homeTemp;
	}

	public File getFileHomeExtProgramme() {
		return homeExtProgramme;
	}

	public File getFileHomeInitValues() {
		return homeInitValues;
	}

	private String albumName = null;

	public String getAlbumName() {
		if (albumName != null) {
			return albumName;
		}
		// from text file "pm_album_name.txt"
		if (homeBilder == null) {
			return "not-found";
		}
		// File Album-name nicht vorhanden
		if (albumName == null) {
			homeFileAlbumName = new File(getMetaRootDir() + File.separator
					+ FILE_ALBUM_NAME);
			if (!homeFileAlbumName.exists()) {
				try {
					homeFileAlbumName.createNewFile();
					PrintWriter out = new PrintWriter(new BufferedWriter(
							new FileWriter(homeFileAlbumName)));
					out.println(homeBilder.getName());
					out.close();
					// read it now ...
				} catch (IOException e) {
					albumName = "cannot-create";
					return albumName;
				}
			}
			// File exist. Read it.
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						homeFileAlbumName));
				while (true) {
					String line = in.readLine().trim();
					if (line.startsWith("#")) {
						continue;
					}
					albumName = line.trim();
					break;
				}
				in.close();
			} catch (IOException e) {
				albumName = "cannot-create";
				return albumName;
			}

		}
		return albumName;
	}

	public File getFileHomeICCProfiles() {
		return homeICCProfiles;
	}

	public File getFileEinstellungen() {
		return xmlFile;
	}

	public String getPathMetadatenRoot() {
		File directory = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT);
		return directory.getPath();
	}

	public File getMetaRootDir() {
		return new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT);
	}

	// =====================================================
	// vdr present
	// =====================================================
	public boolean isMpeg() {
		return mpeg;
	}

	// =====================================================
	// getPrefetch ....
	// =====================================================
	public int getPrefetchPlus() {
		return prefetchPlus;
	}

	public int getPrefetchMinus() {
		return prefetchMinus;
	}

	// =====================================================
	// getBackup ....
	// =====================================================

	// ---------- <vdr-pluin> ------------------------
	public boolean vdrPlugin() {
		return vdrPlugin;
	}

	public String getVdrPluginHost() {
		return vdrPlugHost;
	}

	public String getVdrPluginName() {
		return vdrPlugName;
	}

	public File getVdrPluginIndexFile() {
		return new File(vdrPlugINDEX);
	}

	public File getVdrPluginMpgFile() {
		return new File(vdrPlugMPG);
	}

	public String getVdrOverscanX() {
		return vdrOverscanX;
	}

	public String getVdrOverscanY() {
		return vdrOverscanY;
	}

	// ------------ <bilder> ----------------------------
	public List<BackUp> getBackupBilderList() {
		return backupBilderList;
	}

	// ------------ <daten> ----------------------------
	public List<BackUp> getBackupDatenList() {
		return backupDatenList;
	}

	// ------------ <album> ----------------------------
	public List<BackUp> getBackupAlbumList() {
		return backupAlbumList;
	}
	
	// =====================================================
	// Sicherungen
	// =====================================================
	// ######## 9.10.2006 auf false ###############
	public boolean getSichernBilderZIP() {
		return false;
	} // sichernBilderZIP; }

	public PM_AllSystemPrinter getAllSystemPrinter() {
		return systemPrinters;
	}

	// ===========================================================
	// Diashow()
	// ===========================================================
	public String getSlideshowText1() {
		return slideshowText1;
	}

	public String getSlideshowText2() {
		return slideshowText2;
	}

	public String getSlideshowText3() {
		return slideshowText3;
	}

	// =====================================================
	// private: setDruckerFormat()
	//
	// <bild-format name="13 x 15" papier-format="F2" breite-mm="130"
	// hoehe-mm="150" />
	//    
	//
	// =====================================================
	private final String TAG_BILD_FORMAT = "bild-format";
	private final String ATTR_FORMAT_NAME = "name";
	private final String ATTR_FORMAT_FORMAT = "papier-format";
	private final String ATTR_FORMAT_BREITE = "breite-mm";
	private final String ATTR_FORMAT_HOEHE = "hoehe-mm";

	/**
	 * Read all printer formats from config file.
	 * 
	 * <pre>
	 *   <bild-format name="13 x 15" papier-format="F2" breite-mm="130" hoehe-mm="150" />
	 * </pre>
	 * 
	 */
	private void setPrinterFormat() {
		PM_PictureFormatCollection instance = PM_PictureFormatCollection
				.getInstance();
		List formatList = PM_XML_Utils.getElementListe(document, "//"
				+ TAG_BILD_FORMAT);
		// loop over all tags
		for (Iterator itFormate = formatList.iterator(); itFormate.hasNext();) {
			Element format = (Element) itFormate.next();
			String name = PM_XML_Utils.getAttribute(format, ATTR_FORMAT_NAME);
			String papierFormatString = PM_XML_Utils.getAttribute(format,
					ATTR_FORMAT_FORMAT);
			int breite = PM_XML_Utils.getAttributeInt(format,
					ATTR_FORMAT_BREITE);
			int hoehe = PM_XML_Utils.getAttributeInt(format, ATTR_FORMAT_HOEHE);
			PM_PaperFormat papierFormat = PM_PaperFormat
					.getPaperFormat(papierFormatString);
			if (papierFormat == null) {
				continue;
			}
			if (hoehe == 0 || breite == 0)
				continue; // unzul�ssige Eingaben
			instance.addBildFormat(new PM_PictureFormatFix(papierFormat,
					new Rectangle(breite, hoehe), name));

		}

	}

	// =====================================================
	// private: setDatumJahrVonBis()
	//
	// <datum jahr-von="1998" jahr-bis="2007"/>
	//
	// =====================================================
	private void setDateFromTo() {
		java.util.List result = new java.util.ArrayList();

		// -----------------------------------------------------------------
		// <datum>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_DATUM);
		if (result.size() == 1) {
			Element elDatum = (Element) result.get(0);
			datumJahrVon = PM_XML_Utils.getAttributeInt(elDatum,
					ATTR_DATUM_JAHR_VON);
			datumJahrBis = PM_XML_Utils.getAttributeInt(elDatum,
					ATTR_DATUM_JAHR_BIS);

		}

	}

	// =====================================================
	// private: setPreftch()
	//
	// <prefetch plus="2" minus="0"/>
	//
	// =====================================================
	private void setPrefetch() {
		java.util.List result = new java.util.ArrayList();

		// -----------------------------------------------------------------
		// <datum>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_PREFETCH);
		if (result.size() == 1) {
			Element elPrefetch = (Element) result.get(0);
			prefetchPlus = PM_XML_Utils.getAttributeInt(elPrefetch,
					ATTR_PREFETCH_PLUS);
			prefetchMinus = PM_XML_Utils.getAttributeInt(elPrefetch,
					ATTR_PREFETCH_MINUS);

		}
	}

	// =====================================================
	// private: setVdr()
	//
	// <vdr/>
	//
	// =====================================================
	private final String TAG_VDR = "mpeg";

	private void setMpeg() {
		java.util.List result = new java.util.ArrayList();

		// -----------------------------------------------------------------
		// <diashow>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_VDR);
		if (result.size() == 1) {
			// support vdr (make for example mpeg files)
			mpeg = true;
		}
	}

	// =====================================================
	// private: setDiashow()
	//
	// <diashow text1="index1" text2="datum" text3="bemerkung"/>
	//
	// =====================================================
	private final String TAG_DIASHOW = "diashow";
	private final String ATTR_DIASHOW_TEXT1 = "text1";
	private final String ATTR_DIASHOW_TEXT2 = "text2";
	private final String ATTR_DIASHOW_TEXT3 = "text3";

	private void setSlideshow() {
		java.util.List result = new java.util.ArrayList();

		// -----------------------------------------------------------------
		// <diashow>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_DIASHOW);
		if (result.size() == 1) {
			Element el = (Element) result.get(0);
			slideshowText1 = PM_XML_Utils.getAttribute(el, ATTR_DIASHOW_TEXT1);
			slideshowText2 = PM_XML_Utils.getAttribute(el, ATTR_DIASHOW_TEXT2);
			slideshowText3 = PM_XML_Utils.getAttribute(el, ATTR_DIASHOW_TEXT3);
		}
	}

	// =====================================================
	// private: setSequences()
	//
	// Hier nur Directories f�r Sequences anlegen
	//
	// =====================================================
	private void setSequences() {
		getSequencesDirNormal();
		getSequencesDirBase();
		getSequencesDirVirtual();
		getSequencesDirNew();
	}

	public File getSequencesDir() {
		File dir = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT + File.separator + DIR_SEQUENCES);
		dir.mkdirs();
		return dir;
	}

	public File getSequencesDirNormal() {
		File dir = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT + File.separator + DIR_SEQUENCES
				+ File.separator + DIR_SEQUENCES_NORMAL);
		dir.mkdirs();
		return dir;
	}

	public File getSequencesDirBase() {
		File dir = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT + File.separator + DIR_SEQUENCES
				+ File.separator + DIR_SEQUENCES_BASE);
		dir.mkdirs();
		return dir;
	}

	public File getSequencesDirVirtual() {
		File dir = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT + File.separator + DIR_SEQUENCES
				+ File.separator + DIR_SEQUENCES_VIRTUAL);
		dir.mkdirs();
		return dir;
	}

	public File getSequencesDirNew() {
		File dir = new File(homeBilder.getPath() + File.separator
				+ DIR_METADATEN_ROOT + File.separator + DIR_SEQUENCES
				+ File.separator + DIR_SEQUENCES_NEW);
		dir.mkdirs();
		return dir;
	}

	// =====================================================
	// private: setBackup()
	//
	// <backup>
	// <vdr-plugin remote="vdr" mount="/home/dih/remote/vdr/pictures"
	// index="/home/dih/tmp/pic2mpg/pictures.INDEX/test"
	// mpg="/home/dih/tmp/pic2mpg/pictures.MPG/test"
	// overscan-x="5" overscan-y="4"/>
	// <daten name="desktop" host="desktop" from="/home/dih/tmp/daten"
	// to="/home/dih/tmp/daten-to"/>
	//
	// <bilder remote="vdr" mount="/home/dih/remote/vdr/bilder/bilder"
	// to="/home/dih/remote/vdr/bilder/bilder" />
	// </backup>
	//
	// =====================================================
	private final String TAG_BACKUP = "backup";
	// ---------- vdr-plugin ---------------------
	private final String TAG_VDR_PLUGIN = "vdr-plugin";
	private final String ATTR_VDR_HOST = "host";
	private final String ATTR_VDR_NAME = "name";
	private final String ATTR_VDR_INDEX = "index";
	private final String ATTR_VDR_MPG = "mpg";
	private final String ATTR_VDR_OV_X = "overscan-x";
	private final String ATTR_VDR_OV_Y = "overscan-y";

	private String vdrPlugHost = "";
	private String vdrPlugName = "";
	private String vdrPlugINDEX = "";
	private String vdrPlugMPG = "";
	private String vdrOverscanX = "0";
	private String vdrOverscanY = "0";
	private boolean vdrPlugin = false;
	// ---------- bilder (alle Bilder sichern) -------------------
	private final String TAG_BILDER_BILDER = "bilder";
	private final String ATTR_BILDER_NAME = "name";
	private final String ATTR_BILDER_DIR = "to-dir";
	private final String ATTR_BILDER_MPEG = "mpeg";
	
	// ----------- daten --------------------------------------------
	private final String TAG_DATEN = "daten";
	private final String ATTR_DATEN_NAME = "name";
	// private final String ATTR_DATEN_HOST = "host";
	private final String ATTR_DATEN_FROM = "from";
	private final String ATTR_DATEN_TO = "to";

	// -------- album --------------------
	private final String TAG_ALBUM = "album";
	private final String ATTR_ALBUM_NAME = "name";
	private final String ATTR_ALBUM_DIR = "to-dir";
	
	// --- backup List's --------------------------
	private List<BackUp> backupBilderList = new ArrayList<BackUp>();
	private List<BackUp> backupDatenList = new ArrayList<BackUp>();
	private List<BackUp> backupAlbumList = new ArrayList<BackUp>();

	private void setBackup() {
		List result = new ArrayList();

		// -----------------------------------------------------------------
		// <vdr-plugin>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_BACKUP + "/"
				+ TAG_VDR_PLUGIN);
		if (result.size() == 1) {
			vdrPlugin = true;
			Element elPlugin = (Element) result.get(0);

			vdrPlugHost = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_HOST);
			vdrPlugName = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_NAME);
			vdrPlugINDEX = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_INDEX);
			vdrPlugMPG = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_MPG);
			vdrOverscanX = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_OV_X);
			vdrOverscanY = PM_XML_Utils.getAttribute(elPlugin, ATTR_VDR_OV_Y);
		}
		// -----------------------------------------------------------------
		// <bilder>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_BACKUP + "/"
				+ TAG_BILDER_BILDER);
		for (int i = 0; i < result.size(); i++) {
			Element el = (Element) result.get(i);
			BackUp bB = new BackUp(
					PM_XML_Utils.getAttribute(el, ATTR_BILDER_NAME), 
					homeBilder.getPath(), // from
					PM_XML_Utils.getAttribute(el, ATTR_BILDER_DIR), // TO
					PM_XML_Utils.getAttributeBoolean(el, ATTR_BILDER_MPEG)
			);
			backupBilderList.add(bB);
		}

		// ----------------------------------------------------------------------------------------------
		// <daten>
		// ----------------------------------------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_BACKUP + "/"
				+ TAG_DATEN);
		for (int i = 0; i < result.size(); i++) {
			Element el = (Element) result.get(i);
			BackUp bB = new BackUp(
					PM_XML_Utils.getAttribute(el, ATTR_DATEN_NAME), 
					PM_XML_Utils.getAttribute(el, ATTR_DATEN_FROM), // from
					PM_XML_Utils.getAttribute(el, ATTR_DATEN_TO), // TO
					false        // no mepeg files
			);
			backupDatenList.add(bB);
		}
		// ----------------------------------------------------------------------------------------------
		// <album>
		// ----------------------------------------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_BACKUP + "/"
				+ TAG_ALBUM);
		for (int i = 0; i < result.size(); i++) {
			Element el = (Element) result.get(i);
			BackUp bB = new BackUp(
					PM_XML_Utils.getAttribute(el, ATTR_ALBUM_NAME), 
					homeBilder.getPath(), // from
					PM_XML_Utils.getAttribute(el, ATTR_ALBUM_DIR),
					false       // no mpeg files
					 
			);
			backupAlbumList.add(bB);
		}

	}

	// =====================================================
	// private: setHomeFile()
	//
	// <home>
	// <bilder>d:\Bilder_TEST</bilder>
	// </home>
	//  
	// =====================================================
	private final String TAG_HOME = "home";
	private final String TAG_BILDER = "bilder";

	private File getHomeFile() {

		File home = null;
		java.util.List result = new java.util.ArrayList();
		String value = null;

		// -----------------------------------------------------------------
		// <bilder>
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_HOME + "/"
				+ TAG_BILDER);
		if (result.size() == 1) {
			value = ((Element) result.get(0)).getTextTrim();
			value = PM_Utils.removeLastSeparatorChar(value);
			home = new File(value);
			if (!home.isDirectory()) {
				home = null;
			}
		}
		return home;
	}

	/**
	 * read locale from xml File
	 * 
	 * <locale language="en" country="GB" variant="J" />
	 * 
	 */
	private final String TAG_LOCALE = "locale";
	private final String ATTR_LOCALE_LANGUAGE = "language";
	private final String ATTR_LOCALE_COUNTRY = "country";
	private final String ATTR_LOCALE_VARIANT = "variant";

	private Locale getLocale() {

		java.util.List result = new java.util.ArrayList();
		String language = "";
		String country = "";
		String variant = "";

		// -----------------------------------------------------------------
		// <locale language="en" country="GB" variant="J" />
		// -----------------------------------------------------------------
		result = PM_XML_Utils.getElementListe(document, "//" + TAG_LOCALE);
		if (result.size() == 1) {
			Element el = (Element) result.get(0);
			language = PM_XML_Utils.getAttribute(el, ATTR_LOCALE_LANGUAGE);
			country = PM_XML_Utils.getAttribute(el, ATTR_LOCALE_COUNTRY);
			variant = PM_XML_Utils.getAttribute(el, ATTR_LOCALE_VARIANT);
		}

		if (language.length() == 0) {
			return null;
		}
		// if (country.length() == 0) {
		// return new Locale(language);
		// }
		// if (variant.length() == 0) {
		// return new Locale(language, country);
		// }

		return new Locale(language, country, variant);
	}

	public void setLocale(Locale locale) {
		// locale == null --> l�schen
		Element root = document.getRootElement();
		// pr�fen,ob locale-tag vorhanden
		java.util.List localeListe = PM_XML_Utils.getElementListe(document,
				"//" + TAG_LOCALE);
		Element localeElement = null;
		if (localeListe.isEmpty()) {
			if (locale == null) {
				return; // soll gel�scht sein
			}
			localeElement = new org.dom4j.tree.DefaultElement(TAG_LOCALE);
			updateAttribute(localeElement, ATTR_LOCALE_LANGUAGE, locale
					.getLanguage());
			root.add(localeElement);
		} else {
			localeElement = (Element) localeListe.get(0);
			if (locale != null) {
				updateAttribute(localeElement, ATTR_LOCALE_LANGUAGE, locale
						.getLanguage());
			} else {
				// l�schen
				updateAttribute(localeElement, ATTR_LOCALE_LANGUAGE, null);
			}

		}
	}

	// =====================================================
	// private: setAlleDrucker()
	//
	//  
	//
	// (noch nicht implementiert) <format breite-mm="130" hoehe-mm="150"
	// text="13 x 15"/>
	//
	// <system-drucker system-name="Laserdrucker">
	// <papier-bereich x="18" y="12" breite="559" hoehe="812" />
	// <pm-drucker pm-name="Laser"
	// <druck-bereich x1="18" x2="577" y1="12" y2="824" />
	// </pm-drucker>
	// </system-drucker>
	//    
	// <system-drucker system-name="Farbdrucker">
	// <pm-drucker pm-name="max"
	// <druck-bereich x1="10" x2="585" y1="5" y2="809" />
	// </pm-drucker>
	// <pm-drucker pm-name="Kalender"
	// <druck-bereich x1="25" x2="485" y1="10" y2="609" />
	// </pm-drucker>
	// </system-drucker>
	//  
	// =====================================================
	private final String TAG_SYSTEM_DRUCKER = "system-drucker";
	private final String ATTR_SYSTEM_DRUCKER_NAME = "system-name";
	private final String TAG_PM_DRUCKER = "pm-drucker";
	private final String ATTR_PM_DRUCKER_NAME = "pm-name";
	private final String TAG_DRUCK_BEREICH = "druck-bereich";
	private final String ATTR_DRUCK_BEREICH_OBEN = "oben";
	private final String ATTR_DRUCK_BEREICH_RECHTS = "rechts";
	private final String ATTR_DRUCK_BEREICH_UNTEN = "unten";
	private final String ATTR_DRUCK_BEREICH_LINKS = "links";

	private void setAllPrinter() {
		java.util.List systemDruckerList = PM_XML_Utils.getElementListe(
				document, "//" + TAG_SYSTEM_DRUCKER);
		// --------------------------------------------------------------
		// Loop ueber ALLE Systemdrucker
		// --------------------------------------------------------------
		for (Iterator itSystemDrucker = systemDruckerList.iterator(); itSystemDrucker
				.hasNext();) {
			Element systemDrucker = (Element) itSystemDrucker.next();
			String systemPrinterName = PM_XML_Utils.getAttribute(systemDrucker,
					ATTR_SYSTEM_DRUCKER_NAME);
			PM_SystemPrinter systemPrinter = systemPrinters
					.getSystemPrinter(systemPrinterName);
			if (systemPrinter == null) {
				continue; // system printer not found
			}
			// --------------------------------------------------------------
			// loop ueber alle pm-drucker innerhalb EINES Systemdruckers
			// --------------------------------------------------------------
			java.util.List pmDruckerList = PM_XML_Utils.getElementListe(
					systemDrucker, TAG_PM_DRUCKER);
			for (Iterator itPmDrucker = pmDruckerList.iterator(); itPmDrucker
					.hasNext();) {
				Element pmDrucker = (Element) itPmDrucker.next();
				String pmDruckerName = PM_XML_Utils.getAttribute(pmDrucker,
						ATTR_PM_DRUCKER_NAME);
				// init pmPrinter
				PM_PmPrinter pmPrinter = new PM_PmPrinter(systemPrinter,
						pmDruckerName);
				// add to system printer
				systemPrinter.addPmDrucker(pmPrinter);
				// drucker.setPapierBereich(papierBereich);
				// ---- Druck-Bereich innerhalb eines pm-druckers -------
				Element druckBereichElement = pmDrucker
						.element(TAG_DRUCK_BEREICH);
				if (druckBereichElement != null) {
					double oben = PM_XML_Utils.getAttributeDouble(
							druckBereichElement, ATTR_DRUCK_BEREICH_OBEN);
					double rechts = PM_XML_Utils.getAttributeDouble(
							druckBereichElement, ATTR_DRUCK_BEREICH_RECHTS);
					double unten = PM_XML_Utils.getAttributeDouble(
							druckBereichElement, ATTR_DRUCK_BEREICH_UNTEN);
					double links = PM_XML_Utils.getAttributeDouble(
							druckBereichElement, ATTR_DRUCK_BEREICH_LINKS);
					pmPrinter.setDruckBereichGesamtRaender(oben, rechts, unten,
							links);
				}
			} // ende alle pm-drucker fuer einen system-drucker
		} // ende alle Systemdrucker

		// Wenn kein Drucker, dann Default-Drucker anlegen

	}

	// =====================================================
	// private: setHomeBilder()
	//
	// <home>
	// <bilder> ..path.. </bilder>
	// </home>
	//
	// =====================================================
	private void setHomeBilder(String path) {
		Element root = document.getRootElement();
		// pr�fen,ob home-tag vorhanden
		java.util.List homeListe = PM_XML_Utils.getElementListe(document, "//"
				+ TAG_HOME);
		Element homeElement = null;
		if (homeListe.isEmpty()) {
			homeElement = new org.dom4j.tree.DefaultElement(TAG_HOME);
			addTag(homeElement, TAG_BILDER, path);
			root.add(homeElement);
		} else {
			homeElement = (Element) homeListe.get(0);
			addTag(homeElement, TAG_BILDER, path);

		}

	}

	// ==================================================================
	//
	// Inner Class: BackUp
	// ==================================================================
	public class BackUp {

		private String name;

		private File fileDirFrom;
		private File fileDirTo;
		private boolean mpeg;
		
		// =============================================================
		// Konstruktor
		// =============================================================
		public BackUp(String name, String dirFrom, String dirTo, boolean mpeg) {

			this.name = name;

			fileDirFrom = new File(dirFrom);
			fileDirTo = new File(dirTo);
			this.mpeg = mpeg;

		}

		public String getName() {
			return name;
		}

		public File getFileDirFrom() {
			return fileDirFrom;
		}

		public File getFileDirTo() {
			return fileDirTo;
		}

		public boolean getMpeg() {
			return mpeg;
		}
	}

} // Ende Klasse
