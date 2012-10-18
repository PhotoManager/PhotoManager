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
 
 
import pm.picture.*;
import pm.search.PM_LuceneDocument;
import pm.search.PM_Search;
import pm.search.PM_SearchExpr;
import pm.sequence.*;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;
 
import java.io.*;
 
import java.util.*;
import java.util.List;
import java.text.*;  
import javax.swing.*;

import java.security.*;

import java.net.*;

 

import java.awt.*;
import java.awt.event.*;
 
  
public class PM_Utils implements PM_Interface {

  
	 

	// =====================================================
	// getJButon()
	//
	// ... buttonIndexLoschen = PM_Utils.getJButonKlein(ICON_DELETE);
	// =====================================================
	static public JButton getJButonKlein(String name) {
		int xy = (new JTextField(" ")).getPreferredSize().height;
		Dimension dimension = new Dimension(xy, xy);
		return getJButon(name, dimension);
	}

	static public JButton getJButon(String name) {

		// / int xy = (new JTextField(" ")).getPreferredSize().height;
		int xy = (new JButton(" ")).getPreferredSize().height;
		Dimension dimension = new Dimension(xy, xy);
		return getJButon(name, dimension);
	}

	static public JButton getJButon(String name, Dimension dimension) {

		URL url = getImageURL(name);
 
		ImageIcon iconButton = new ImageIcon(url);

		Image im = iconButton.getImage();
		im = im.getScaledInstance(dimension.height, dimension.height,
				Image.SCALE_FAST);
		iconButton.setImage(im);

		JButton button = new JButton(iconButton);
		button.setPreferredSize(dimension); // new Dimension(15,15));
		return button;

	}

	
	
	
	
	
 

	// =====================================================
	// getJButon()
	// =====================================================
	static public JButton getJButon() {
		// holen TextField size fï¿½r preferredSize

		int xy = (new JTextField(" ")).getPreferredSize().height;
		Dimension pf = new Dimension(xy, xy);

		JButton button = new JButton(".");
		button.setPreferredSize(pf); // new Dimension(15,15));
		return button;
	}

	// =====================================================
	// removeDirectory()
	//
	// path = gesamter, absoluter Pfad
	// dir = erster Teil, der von path abgetrennt werden soll
	//
	// return: relativer Pfad
	// =====================================================
	/**
	 * 
	 *  
	 */
	static public String removeDirectory(String path, String dir) {

		// pruefen, ob dir enthalten ist
		int offset = path.indexOf(dir);
		if (offset < 0)
			return path;

		String ret = path.substring(dir.length());
		if (ret.charAt(0) == File.separatorChar)
			ret = ret.substring(1);

		return ret;
	}

	// =====================================================
	// removeLastSeparatorChar()
	//
	// Wenn am Ende des Strings ein path-separator-char ist, dann diesen
	// entfernen
	// =====================================================
	static public String removeLastSeparatorChar(String path) {
		if (path.length() < 1)
			return path;
		if (path.charAt(path.length() - 1) == File.separatorChar) {
			return path.substring(0, path.length() - 1);
		}

		return path;
	}

	// =========================================================================
	// getExtension()
	// 
	// =========================================================================
	public static String getExtension(File f) {
		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1);
		}
		return ext;
	}

	// =========================================================================
	// getExtension()
	// 
	// =========================================================================
	public static String getExtension(String s) {
		String ext = "";
		// String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1);
		}
		return ext;
	}

	// =========================================================================
	// getJavaVersion()
	// 
	// =========================================================================
	public static String getJavaVersion() {

		return System.getProperty("java.version");
	}

	// =========================================================================
	// isPictureFile()
	// 
	// =========================================================================
	public static boolean isPictureFile(File f) {
		
		if (f.isFile() == false) {
			return false;
		}
		
	
		if (getStopFile(f).isFile()) {
			return false;
		}
 
		String extension = getExtension(f);
		if (extension.equalsIgnoreCase("jpg")
				|| extension.equalsIgnoreCase("jpeg")) {
			return true;
		}
		return false;
	}
	
 
	/**
	 * return stop file instance
	 */
	public static File getStopFile(File file) {
		return new File(file.getParent() 
				+ File.separator + DIR_METADATEN 
				+ File.separator + DIR_STOP_FILES
				+ File.separator + file.getName());	
	}
 
	/**
	 *
	 */
	public static void setStop(File file) {
		File stopDir = new File(file.getParent() 
				+ File.separator + DIR_METADATEN
				+ File.separator + DIR_STOP_FILES);
		stopDir.mkdirs();
		try {
			getStopFile(file).createNewFile();
		} catch (IOException e) {
			 
//			e.printStackTrace();
		}
		
		
	}
	
	
	
	// =========================================================================
	// isLink()
	// 
	// =========================================================================
	public static boolean isLink(File file) throws IOException
	  {
	    
		  String cnnpath = file.getCanonicalPath();
		  String abspath = file.getAbsolutePath();
		  return !abspath.equals(cnnpath);
		 
	  } //isLink
	
	
	

	// =========================================================================
	// isUnderTLD()
	// 
	// true if file is under the top level picture directory
	// =========================================================================
	public static boolean isUnderTLD(File f ) {
		if (f == null) {
			return false;
		}
		File homeBilder = PM_Configuration.getInstance().getTopLevelPictureDirectory();
		String pathTLD = "";
		File file = null;
		try {
			pathTLD = homeBilder.getCanonicalPath();
			file = new File(f.getCanonicalPath());
		} catch (IOException e) {		 
			return false;
		}
		
		while (file != null) {
			// the absolute path is the canocical 
			// due file has created with canonical path name.
			if (file.getAbsolutePath().equalsIgnoreCase(pathTLD)) {
				return true;
			}
			file = file.getParentFile();
		}
		
		return false;				 
	}		
	
	// =========================================================================
	// int stringToInt(String)
	// 
	// "1234" --> 1234
	//
	// nicht numerisch --> 0 (ohne exception)
	// =========================================================================
	static public int stringToInt(String str) {
		int ret;
		try {
			ret = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return 0;
		}
		return ret;
	}

	static public long stringToLong(String str) {
		long ret;
		try {
			ret = Long.parseLong(str);
		} catch (NumberFormatException e) {
			return 0;
		}
		return ret;
	}

	// =========================================================================
	// stringToString
	//
	// static public String stringToString(String, int laenge)
	// "123", 6 --> " 123" (rechtsbuedig)
	// =========================================================================
	static public String stringToString(String in, int lg) {
		String ret = "                                                                  ";
		ret += in;

		return ret.substring(ret.length() - lg);
	}

	// =========================================================================
	// getPropertyString()
	// 
	// =========================================================================
	static public String getPropertyString(Properties properties,
			String propertyName) {
		String value = "";
		value = properties.getProperty(propertyName);
		if (value == null) {
			return "";
		}
		;
		return value;
	}

	// =========================================================================
	// getPropertyString()
	// 
	// =========================================================================
	static public int getPropertyInt(Properties properties, String propertyName) {
		String value = "";
		value = properties.getProperty(propertyName);
		if (value == null) {
			return 0;
		}
		;
		return PM_Utils.stringToInt(value);
	}

	// =========================================================================
	// isLinux()
	// 
	// =========================================================================
	static public boolean isLinux() {
		String osName = getPropertyString(System.getProperties(), "os.name");

		return osName.equalsIgnoreCase("linux");
	}

	// =========================================================================
	// getWorkingDirectory()
	// 
	// =========================================================================
	static public String getWorkingDirectory() {
		String userDir = getPropertyString(System.getProperties(), "user.dir");
		if (userDir.length() == 0)
			PM_Utils
					.writeErrorExit("java-Fehler: Property 'user.dir' nicht gefunden");
		return userDir;
	}

	// =========================================================================
	// getConfigDir()
	// 
	// If not found it will be created.
	//
	//
	// Linux:    ~/.photo-manager
	//
	// =========================================================================
	static public File getConfigDir() {
		 
		File homeDir = getHomeDir();
		File pmDir = new File(homeDir.getPath() + File.separator
				+ FILE_USER_HOME);
		if (!pmDir.isDirectory()) {
			pmDir.mkdirs();
		}

		return pmDir;

	}

	/**
	 * Get the home directory 
	 */
	static public File getHomeDir() {
		String userHome = getPropertyString(System.getProperties(), "user.home");
		File dirUserHome = new File(userHome);
		if (userHome.length() == 0 || dirUserHome.isDirectory() == false) {
			writeErrorExit("java-Fehler: Property 'user.home' nicht gefunden oder kein Verzeichnis");
		}
		return dirUserHome;
	}
	
	// ====================================================================
	// removeExtension
	// 
	// ====================================================================
	public static String getWithoutExtension(String name) {

		int i = name.lastIndexOf('.');

		if (i > 0 && i < name.length() - 1) {
			return name.substring(0, i);
		}
		return name;
	}

	// =========================================================================
	// getDestinationRectangle()
	// 
	// Es wird ein Rectangle errechnet, in dem das source-Rectangle
	// mit unveraendertem Seitenverhaeltnis Platz hat
	//
	// source = Rectangle vom Bild, das aufzubereiten ist
	// destination = in dieses Rectangle muss dass Bild vollstandig passen
	//
	// return: Rectangle innerhalb vom destination-Ractangle, in das das Bild
	// vollstaendig Platz hat.
	// =========================================================================
	public static Rectangle getDestinationRectangle(Rectangle source,
			Rectangle destination) {

		Rectangle ret = null;
		int offset = 0;
		double sourceRatio = (double) source.height / (double) source.width;
		double destinationRatio = (double) destination.height
				/ (double) destination.width;
		if (sourceRatio == destinationRatio) {
			return new Rectangle(destination);
		}
		;
		if (sourceRatio > destinationRatio) {
			int b = (int) ((double) destination.height / sourceRatio);
			offset = (int) ((double) (destination.width - b) / 2);
			ret = new Rectangle(offset, 0, b, destination.height);
		} else {
			int h = (int) ((double) destination.width * sourceRatio);
			offset = (int) ((double) (destination.height - h) / 2);
			ret = new Rectangle(0, offset, destination.width, h);
		}

		ret.x += destination.x;
		ret.y += destination.y;
		return ret;
	}

	// ====================================================================
	// hasFileThumbnail()
	// ====================================================================
	public static boolean hasFileThumbnail(File fileOriginal) {
		File dirMetadaten = new File(fileOriginal.getParent() + File.separator
				+ DIR_METADATEN);
		if (!dirMetadaten.isDirectory()) {
			return false;
		}
		File dirThumnails = new File(dirMetadaten.getPath() + File.separator
				+ DIR_THUMBNAILS);
		if (!dirThumnails.isDirectory()) {
			return false;
		}
		String pathThumbnail = dirThumnails.getPath() + File.separator
				+ fileOriginal.getName() + EXT_THUMBNAIL;
 
		File fileThumbnail = new File(pathThumbnail);
		if (fileThumbnail.isFile()) {
			return true;
		}
		;
		return false;

	}

	// ====================================================================
	// getFileThumbnail()
	// ====================================================================
	public static File getFileThumbnail(File fileOriginal) {
		File dirMetadaten = new File(fileOriginal.getParent() + File.separator
				+ DIR_METADATEN);
		File dirThumnails = new File(dirMetadaten.getPath() + File.separator
				+ DIR_THUMBNAILS);
		String pathThumbnail = dirThumnails.getPath() + File.separator
				+ fileOriginal.getName() + EXT_THUMBNAIL;
		return new File(pathThumbnail);

	}

	// ====================================================================
	// getFileMPEG()
	// ====================================================================
	public static File getFileMPEG(File fileOriginal) {
		File dirMetadaten = new File(fileOriginal.getParent() + File.separator
				+ DIR_METADATEN);
		File dirMPEG = new File(dirMetadaten.getPath() + File.separator
				+ DIR_MPEG);
		String pathMPEG = dirMPEG.getPath() + File.separator
				+ getWithoutExtension(fileOriginal.getName()) + EXT_MPEG;   
		return new File(pathMPEG);

	}
	
 
	
	// ====================================================================
	// getFilesBearbeitet()
	// ====================================================================
	public static File[] getFilesBearbeitet(File fileOriginal) {
		Vector<File> v = new Vector<File>();
		File dirMetadaten = new File(fileOriginal.getParent() + File.separator
				+ DIR_METADATEN);
		File dirBilderBearb = new File(dirMetadaten.getPath() + File.separator
				+ DIR_BILDER_BEARBEITEN);
		if (!dirBilderBearb.isDirectory()) {
			return (File[]) v.toArray(new File[v.size()]);
		}
		File[] liste = dirBilderBearb.listFiles();
		String fileName = fileOriginal.getName();
		for (int i = 0; i < liste.length; i++) {
			File f = liste[i];
			// eigentlich bessser mit "indexOf", aber der Punkt (dot) im
			// Dateinamen kann mit indexOf nicht gefunden werden ????!!!!!!
			if (fileName.length() <= 1)
				continue;
			// System.out.println(" getFilesBearbeitet . fileName = " + fileName
			// + ". f = " + f.getName());
			// System.out.println(" l/l = " + fileName.length() + "/" +
			// f.getName().length());
			if (fileName.length() > f.getName().length()) {
				// System.out.println(" getFilesBearbeitet . fileName = " +
				// fileName + ". f = " + f.getName());
				// System.out.println(" l/l = " + fileName.length() + "/" +
				// f.getName().length());
				continue;
			}
			String n = f.getName().substring(0, fileName.length());
			if (fileName.equals(n)) {
				// ja, es ist ein bearbeitetes Bild
				v.add(f);
			}
		}

		return (File[]) v.toArray(new File[v.size()]);

	}

	// ====================================================================
	// hasFileIndexXML()
	// ====================================================================
	public static boolean hasFileIndexXML(File dirOriginal) {
		File dirMetadaten = new File(dirOriginal.getPath() + File.separator
				+ DIR_METADATEN);
		if (!dirMetadaten.isDirectory()) {
			return false;
		}
		File fileIndexXML = new File(dirMetadaten.getPath() + File.separator
				+ FILE_INDEX_XML);
		if (fileIndexXML.isFile()) {
			return true;
		}
		;
		return false;
	}

	// ====================================================================
	// getFileIndexXML()
	// ====================================================================
	public static File getFileIndexXML(File dirOriginal) {
		File dirMetadaten = new File(dirOriginal.getPath() + File.separator
				+ DIR_METADATEN);
		return new File(dirMetadaten.getPath() + File.separator
				+ FILE_INDEX_XML);
	}

	// ====================================================================
	// fileOriginalToXmlId()
	//
	// Erzeugt id in der XML-Datei
	// ====================================================================
	public static String fileOriginalToXmlId(File fileOriginal) {
		return fileOriginal.getName();
	}

	// ====================================================================
	// xmlIdToFileOriginal()
	//
	//
	// ====================================================================
	public static File xmlIdToFileOriginal(File xmlFile, String id) {
		String pathOriginal = (new File(xmlFile.getParent())).getParent()
				+ File.separator + id;
		;
		return new File(pathOriginal);
	}

	// ====================================================================
	// getScreenSize()
	//
	// ====================================================================
	public static Dimension getScreenSize() {

		 
		// System.out.println("----- ScreenSize = " +
		// Toolkit.getDefaultToolkit().getScreenSize());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println("screenSize. " + screenSize);
		return screenSize;
	}

	public static Insets getScreenInsets() {
		return Toolkit.getDefaultToolkit().getScreenInsets(new Frame().getGraphicsConfiguration());
	}
	
	// ====================================================================
	// writeErrorExit()
	//
	// ====================================================================
	public static void writeErrorExit(String text) {

		// wenn Text zu lang, dann hart umbrechen
		String errorText = text;
		if (errorText.length() > 50) {
			errorText = "";
			for (int i = 0; i < text.length();) {
				if (i >= text.length())
					break;
				int ii = i + 80;
				if (ii >= text.length())
					ii = text.length();
				errorText += text.substring(i, ii) + NL;
				i += 80;

			}
		}

		System.out
				.println("=====================================================");
		System.out.println(errorText);
		System.out
				.println("=====================================================");

		// System.out
		// .println("------------------------------------------------------");
		// System.out.println("ERROR: " + errorText);
		// System.out
		// .println("------------------------------------------------------");
		if ( !PM_Configuration.getInstance().getBatch() ) {
			javax.swing.JOptionPane.showConfirmDialog(null, errorText,
					"Fehler", javax.swing.JOptionPane.DEFAULT_OPTION,
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}

		System.exit(0);
	}

	// =====================================================
	// dateienIdentisch(File in, File out)
	//
	// Es wird geprï¿½ft, ob die beiden Dateien identisch sind.
	// =====================================================
	static public boolean dateienIdentisch(File f1, File f2) {
		// Wenn die Laengen ungleich sind, dann return false:
		if (f1.length() != f2.length())
			return false;

		// TODO jetzt mit MD5 vergleichen

		// *********** to do *************

		// Mit Class MessageDigest

		return true;
	}

	// =====================================================
	// getMessageDigest()
	//
	// =====================================================
	static public byte[] getMessageDigest(File file) {
		DigestInputStream dis = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			dis = new DigestInputStream(fis, md5);
			// I read only data.length bytes (due performance)
			byte[] data = new byte[5*1024];
			int l = data.length;
			while (true) {
				int bytesRead = dis.read(data, 0, l);
				if (bytesRead == -1) {
					break;
				}
				l = l - bytesRead;
				if (l <= 0) {
					break;
				}
				
				// >>>>> if you want to read the whole file, then do this:  <<<<<<
				// int bytesRead = dis.read(data);
				// if (bytesRead < 0) {
				//	  break;
				// }	
				
			} // while
		} // try
		catch (FileNotFoundException fnfe) {
			  System.out.println(file.getPath() + " does not exist!");
			return null;
		} catch (NoSuchAlgorithmException e) {
			  System.out.println("NoSuchAlgorithmException" + e);
			return null;
		} catch (IOException ioe) {
			  System.out.println("Error reading/writing files!");
			return null;
		}

		MessageDigest md = dis.getMessageDigest();
		try {
			fis.close();
		} catch (IOException e) {		 
			e.printStackTrace();
		}
		return md.digest();
	}

	 
	 
	static public boolean copyFile(File in, File out) {

		BufferedInputStream bufin = null;
		BufferedOutputStream bufout = null;

		try {
			FileInputStream fis = new FileInputStream(in);
			bufin = new BufferedInputStream(fis);

			out.delete();
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
			// System.out.println(in.getPath() + " does not exist!");
		} catch (IOException ioe) {
			// System.out.println("Error reading/writing files!");
		} finally {
			try {
				if (bufin != null)
					bufin.close();
				if (bufout != null)
					bufout.close();
			} catch (IOException ioe) {
			}
		}

		return true;
	}

	// =====================================================
	// getSortedSet()
	// getList()
	//
	// "splitted" den String in Tokens (getrennt durch space) und
	// ueberfuehrt diese in einen sortedSet.
	// Wenn der String leer, dann einen leeren SortedSet zurueck.
	// =====================================================
	static public SortedSet<String> getSortedSet(String s) {
		SortedSet<String> sortedSet = new TreeSet<String>();
		if (s == null || s.length() == 0)
			return sortedSet;
		String[] sa = s.split(" ");
		for (int i = 0; i < sa.length; i++) {
			sortedSet.add(sa[i]);
		}
		return sortedSet;
	}

	static public List<String> getList(String s) {
		List<String> list = new ArrayList<String>();
		if (s == null || s.length() == 0)
			return list;
		String[] sa = s.split(" ");
		for (int i = 0; i < sa.length; i++) {
			list.add(sa[i].trim());
		}
		return list;
	}

	// =====================================================
	// sortedSetToString()
	//
	// =====================================================
	static public String sortedSetToString(SortedSet sortedSet) {
		String s = "";
		if (sortedSet.size() == 0)
			return s;
		Iterator it = sortedSet.iterator();
		while (it.hasNext()) {
			s += " " + (String) it.next();
		}

		return s.trim();
	}

	// =====================================================
	// private: datumToDate()
	//
	// datum als String: "dd.MM.yyyy HH:mm:ss"
	// Dieses hier in Date wandeln
	// =====================================================
	static public Date datumToDate(String datum) {

		Date date = new Date(System.currentTimeMillis());
		if (datum == null)
			return date;

		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		try {
			date = df.parse(datum);
		} catch (ParseException e) {
			return new Date(System.currentTimeMillis());
		}
		return date;
	}

	// =====================================================
	// getDateString()
	//
	// Date in String wandeln.
	// =====================================================
	static public String getDateString(Date date, String format) {

		DateFormat df = new SimpleDateFormat(format);

		return df.format(date);
	}

	// =====================================================
	// getTimeStamp()
	//
	// datum als String: "dd.MM.yyyy_HH:mm:ss"
	// Dieses hier in Date wandeln
	// =====================================================
	static public String getTimeStamp() {

		Date date = new Date(System.currentTimeMillis());

		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

		return df.format(date);
	}

	// =====================================================
	// getJahre()
	//
	// =====================================================
	static public Vector<String> getJahre(boolean mitBlank) {
		PM_Configuration einstellungen = PM_Configuration
				.getInstance();
		int von = einstellungen.getDatumJahrVon();
		int bis = einstellungen.getDatumJahrBis();
		return getArrayString(von, bis, 4, mitBlank);
	}

	static public Vector getMonate(boolean mitBlank) {
		return getArrayString(1, 12, 2, mitBlank);
	}

	static public Vector getTage(boolean mitBlank) {
		return getArrayString(1, 31, 2, mitBlank);
	}

	static public Vector getStunden(boolean mitBlank) {
		return getArrayString(0, 23, 2, mitBlank);
	}

	static public Vector getMinuten(boolean mitBlank) {
		return getArrayString(0, 59, 2, mitBlank);
	}

	static public Vector getSekunden(boolean mitBlank) {
		return getArrayString(0, 59, 2, mitBlank);
	}

	static public Vector<String> getArrayString(int von, int anzahl, int lg,
			boolean mitBlank) {
		Vector<String> v = new Vector<String>();
		if (mitBlank && lg == 1)
			v.add(" ");
		if (mitBlank && lg == 2)
			v.add("  ");
		if (mitBlank && lg == 3)
			v.add("   ");
		if (mitBlank && lg == 4)
			v.add("    ");
		for (int i = von; i < anzahl + 1; i++) {
			String s = stringToString("00000" + Integer.toString(i), lg);
			v.add(s);
		}
		return v;
	}

	// =====================================================
	// loeschenBilder()
	//
	// Es wurde ein XML-Index-Eintrag geloescht.
	// Hier nun alle zugehoerigen Bilderdateien (auch Thumbnails)
	// loeschen.
	// =====================================================
	static public File[] loeschenBilder(File fileOriginal) {

		Vector<File> geloescht = new Vector<File>();

		// Loeschen Thumbnail
		if (hasFileThumbnail(fileOriginal)) {
			File fileThumbnail = getFileThumbnail(fileOriginal);
			fileThumbnail.delete();
			geloescht.add(fileThumbnail);
		}

		// Loeschen evtl. veraenderte Bilder
		File[] filesBearbeitet = getFilesBearbeitet(fileOriginal);
		for (int i = 0; i < filesBearbeitet.length; i++) {
			File f = filesBearbeitet[i];
			f.delete();
			geloescht.add(f);
		}

		// Zum Schluss Originalfile loeschen
		boolean ok = fileOriginal.delete();
		if (!ok) {
			// System.out
			// .println("ERROR !!!!!: File konnte nicht gelï¿½scht werden "
			// + fileOriginal.getPath());
		}
		geloescht.add(fileOriginal);

		return (File[]) geloescht.toArray(new File[geloescht.size()]);
	}

	// =====================================================
	// setWrite()
	//
	// Setzen Attribut auf "Schreiben".
	// 
	// Z.Zt. Nur für Windows.
	// 
	// return: true -> konnte gesetzt werden oder ist bereits gesetzt
	// false --> kann nicht gesetzt werden
	//  
	// =====================================================
	static public boolean setWrite(File file) {

		if (file.canWrite())
			return true; // bereits schreibbar

		// System.out.println("--- set to write: " + file.getPath());

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		String cmd = "attrib -R " + file.getPath();
		try {
			process = runtime.exec(cmd);
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(null, "setWrite Fehler. (cmd = "
					+ cmd + ")", "Fehler", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		// Jetzt Processï¿½berwachung starten.
		// Es kann erst weitergearbeitet werden, nachdem das
		// Bildbearbeitungsprogramm
		// beendet wurde.
		try {
			process.waitFor();
		} catch (InterruptedException e) {
		}

		if (file.canWrite())
			return true; // ok

		return false; // gescheitert
	}

	// =====================================================
	// isBilderDir()
	//
	// Prï¿½fen, ob beim Lesen aller Bilder dieses Directory
	// gelesen werden darf
	// =====================================================
	static public boolean isBilderDir(File file) {

		// check if directory
		if (!file.isDirectory()) {
			return false; // NO Directory
		}

		PM_Configuration einstellungen = PM_Configuration
				.getInstance();
		String path = file.getPath();

		// Temp-Dir nicht verarbeiten
		String temp = einstellungen.getFileHomeTemp().getPath();
		if (path.equals(temp)) {
			return false;
		}

		// metadaten-root nicht verarbeiten
		String root = einstellungen.getMetaRootDir().getPath();
		if (path.equals(root)) {
			return false;
		}

		// Metadaten nicht darstellen
		if (file.getName().equals(DIR_METADATEN)) {
			return false;
		}

		// Es ist ein zulaessiges Directory
		return true;

	}

	// =====================================================
	// isAncestorOf()
	//
	// Prï¿½fen, ob beim Lesen aller Bilder dieses Directory
	// gelesen werden darf
	// =====================================================
	static public boolean isAncestorOf(Component leaf, Component root) {
		if (leaf == null || root == null)
			return false;
		if (leaf == root)
			return true;
		Component parent = leaf.getParent();
		if (parent == root)
			return true;
		while (parent != null) {

			parent = parent.getParent();
			if (parent == root)
				return true;
		}

		return false;
	}

	// =====================================================
	// removeKeyCode()
	//
	// von JComponent wird die InputMap von allen conditions
	// geholt und der keyCode (z.B. KeyEvent.VK_ENTER) entfernt
	// =====================================================
	static public void removeKeyCode(JComponent comp, int keyCode) {
		removeKeyCode(comp.getInputMap(JComponent.WHEN_FOCUSED), keyCode);
		removeKeyCode(comp
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
				keyCode);
		removeKeyCode(comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
				keyCode);
	}

	// nur von InputMap wird der key entfernt
	static public void removeKeyCode(InputMap im, int keyCode) {
		KeyStroke[] ks = im.allKeys();
		if (ks == null)
			return;
		im.clear();
		for (int i = 0; i < ks.length; i++) {
			if (ks[i].getKeyCode() == keyCode) {
				// im.put(ks[i],null);
				// System.out.println("---remove keycode " + keyCode);
				// im.remove(ks[i]);
			}
		}

	}

	// =====================================================
	// getImageURL()
	//
	// =====================================================
	static public URL getImageURL(String name) {
		PM_Utils utils = new PM_Utils();
		URL url = utils.getClass().getClassLoader().getResource(
				ICON_PATH + name);
		if (url == null) {
			url = utils.getClass().getClassLoader().getResource(name);
		}
		
		return url;
	}
 
	// =====================================================
	// isToDay()
	//
	// Wenn am Ende des Strings ein path-separator-char ist, dann diesen
	// entfernen
	// =====================================================
	static public boolean isToDay(long millis) {
		Calendar now = Calendar.getInstance();
		Calendar compare = Calendar.getInstance();
		compare.setTimeInMillis(millis);

		if (now.get(Calendar.YEAR) != compare.get(Calendar.YEAR))
			return false;
		if (now.get(Calendar.DAY_OF_YEAR) != compare.get(Calendar.DAY_OF_YEAR))
			return false;

		return true;
	}

	// =====================================================
	// getNextRotationLeft()
	// getNextRotationRight()
	//
	// 90 Grad nach links/rechts weiter und neuen Wert zurï¿½ckgeben
	// =====================================================
	static public int getNextRotation(int from, Rotate richtung) {
		 if (richtung == Rotate.LEFT) {
			 return getNextRotationLeft(from);
		 }
		 return  getNextRotationRight(from);
	}

	static private int getNextRotationLeft(int from) {
		switch (from) {
		case CLOCKWISE_0_DEGREES:
			return CLOCKWISE_270_DEGREES;
		case CLOCKWISE_90_DEGREES:
			return CLOCKWISE_0_DEGREES;
		case CLOCKWISE_180_DEGREES:
			return CLOCKWISE_90_DEGREES;
		case CLOCKWISE_270_DEGREES:
			return CLOCKWISE_180_DEGREES;
		}
		 
		return CLOCKWISE_0_DEGREES; // error
	}
	
	static private int getNextRotationRight(int from) {
		switch (from) {
		case CLOCKWISE_0_DEGREES:
			return CLOCKWISE_90_DEGREES;
		case CLOCKWISE_90_DEGREES:
			return CLOCKWISE_180_DEGREES;
		case CLOCKWISE_180_DEGREES:
			return CLOCKWISE_270_DEGREES;
		case CLOCKWISE_270_DEGREES:
			return CLOCKWISE_0_DEGREES;
		}
		 
		return CLOCKWISE_0_DEGREES; // error
	}

	  // =====================================================
	  // getTime()
	  //
	  // Es wird (meist eine xml-Datei) nach dem Strin update-time="1234567" gesucht
	  // 
	  // return mit dem Zeitwert  (0 wenn nigefu)
	  //       
	  // =====================================================
	static public long getTime(File file) {

		if (!file.isFile())
			return 0;

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while (true) {
				String line = in.readLine();
				int ind = line.indexOf("update-time=\"");
				if (ind != -1) {
					String sub = line.substring(ind);
					ind = sub.indexOf("\"");
					if (ind != -1) {
						sub = sub.substring(ind+1);
						ind = sub.indexOf("\"");
						if (ind != -1) {
							sub = sub.substring(0,ind);
							try {
								return Long.parseLong(sub);
							} catch (NumberFormatException e) {
								return 0;
							}
						}
					}
					break;
				}

			}
			in.close();
		} catch (IOException e) {

			return 0;
		}

		return 0;

	} 
	

	// =====================================================
	// getRelativePath()
	//
	// =====================================================
	static public String getRelativePath(File baseFile, File file) {

		String basePath = "";
		String path = "";

		 
			basePath = baseFile.getAbsolutePath();
			path = file.getAbsolutePath();
		
		if (!basePath.endsWith(File.separator)) {
			basePath = basePath + File.separator;
		}
		if (path.indexOf(basePath) != 0)
			return "";

		return path.substring(basePath.length());

	}
	
	 	
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	
	static public void deleteEmptyDirectories(File dir) {
		 
		for (File f: dir.listFiles()) {
			if (f.isDirectory()) {
				deleteEmptyDirectories(f);
				f.delete();
			}
		}
		if (dir.isDirectory()) {
			dir.delete();
		}
	}
	
	// =====================================================
	// Compare fuer das Sortieren nach Sequenzen
	// =====================================================

	static public final Comparator<PM_Sequence> SORT_SEQUENZ_NUMMER = new Comparator<PM_Sequence>() {
		public int compare(PM_Sequence s1, PM_Sequence s2) {

			int i1 = s1.getSequenceNumber();
			int i2 = s2.getSequenceNumber();
			SequenceType t1 = s1.getType();
			SequenceType t2 = s2.getType();

			String c1 = t1 // Integer.toString(t1)
					+ PM_Utils.stringToString("0000000" + Integer.toString(i1),
							6);
			String c2 = t2 // Integer.toString(t2)
					+ PM_Utils.stringToString("0000000" + Integer.toString(i2),
							6);

			return c1.compareTo(c2);

		};
	};

	static public final Comparator<PM_Sequence> SORT_SEQUENZ_PATH = new Comparator<PM_Sequence>() {
		public int compare(PM_Sequence s1, PM_Sequence s2) {
			return s1.getPath().compareTo(s2.getPath());

		};
	};

	static public final Comparator<PM_Sequence> SORT_SEQUENZ_NAME = new Comparator<PM_Sequence>() {
		public int compare(PM_Sequence s1, PM_Sequence s2) {
			String t1 = s1.getShortName();
			String t2 = s2.getShortName();
			String c1 = t1.substring(0) + s1.getPath();
			String c2 = t2.substring(0) + s2.getPath();
			return c1.compareTo(c2);

		};
	};
	 
	static public final Comparator<File> SORT_FILE_NAMES = new Comparator<File>() {
		public int compare(File file1, File file2) {
			String c1 = file1.getName();
			String c2 = file2.getName();

			return c1.compareTo(c2);

		};
	};
	


	
	static public final Comparator<PM_Picture> SORT_TIME_ORDER = new Comparator<PM_Picture>() {
		public int compare(PM_Picture pic1, PM_Picture pic2) {
			long time1 = pic1.meta.getDateCurrent().getTime();
			long time2 = pic2.meta.getDateCurrent().getTime();
			if (time1 == time2)
				return 0;
			if (time1 < time2)
				return -1;
			return 1;
		};
	};
	
	static public final Comparator<PM_Picture> SORT_MINI_SEQUENCE = new Comparator<PM_Picture>() {
		public int compare(PM_Picture pic1, PM_Picture pic2) {
			int int1 = 0;
			if (pic1.meta.hasMiniSequence()) {
				int1 =  pic1.meta.getCurrentMiniNumber(); 
			}
			int int2 = 0;
			if (pic2.meta.hasMiniSequence()) {
				int2 =  pic2.meta.getCurrentMiniNumber(); 
			}
			if (int1 == int2)
				return 0;
			if (int1 < int2)
				return -1;
			return 1;
		};
	};
	
	
	/**
	 * Returns a sorted mini sequence.
	 */
	public static List<PM_Picture> getMiniSequence(PM_Picture picture) {
		if (!picture.meta.hasMiniSequence()) {
			new ArrayList<PM_Picture>();
		}
		 
		// get mini sequence
		int number = picture.meta.getMiniSequenceNumber();
		String searchString = PM_LuceneDocument.LUCENE_MINI_SEQUENZ + ":m"
				+ number + "_*";
		PM_Search search = new PM_Search(new PM_SearchExpr(SearchType.MINI_SEQ,
				searchString));
		search.search();
		List<PM_Picture> miniSequence = search.getPictureList(SearchSortType.NOTHING);
		if (miniSequence.isEmpty()) {
			return miniSequence; // no mini sequence
		}
		Collections.sort(miniSequence, PM_Utils.SORT_MINI_SEQUENCE);
		
		return miniSequence;
	}
	
	
	
	// ==============================================================================
	// Convenience - Methoden fï¿½r eine ComboBox
	//
	// getComboBoxVector() Die Liste (Vector) fï¿½r eine Combobox wird returnt
	// getSelectedSequenz() Die selectierte Sequenz einer Combo-Box wird gesucht
	// getFreieSequenzNummer() Der selectierte Index einer Combo-Box wird
	// gesucht
	// ==============================================================================
	static public Vector<String> getComboBoxVector(List<PM_Sequence> listeSequenzen) {
		Vector<String> v = new Vector<String>();
		v.add("                      "); // am Anfang eine leere Zeile
		Iterator it = listeSequenzen.iterator();
		while (it.hasNext()) {
			PM_Sequence seq = (PM_Sequence) it.next();
			String s = seq.toStringComboBox();
			v.add(s);
		}
		return v;
	}

	static public PM_Sequence getSelectedSequenz(int i,
			List<PM_Sequence> listeGeschlSequenzen) {
		if (listeGeschlSequenzen.size() <= 0)
			return null;
		if (i < 0)
			return null;
		if (listeGeschlSequenzen.size() <= i)
			return null;
		return listeGeschlSequenzen.get(i);
	}

	static public int getSelectedIndex(PM_Sequence sq,
			List<PM_Sequence> listeGeschlSequenzen) {
		Iterator<PM_Sequence> it = listeGeschlSequenzen.iterator();
		int i = 1; // nicht 0, da der erste eine Leerzeile ist
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
			if (sq == seq)
				return i;
			i++;
		}
		return 0;
	}

	
	/**
	 * Write file with an unique name.
	 *
	 */
	static public File  copyFileUnique(File in, File outDir) {
		if (in.isFile() == false || outDir.isDirectory() == false) {
			return null;
		}
 
		// make outFile
		int n = 0;
		String name = getWithoutExtension(in.getName());
		String ext = getExtension(in.getName());
		File out = new File(outDir.getPath() +   File.separator + in.getName());
		while (out.isFile()) {
			n++;
			out = new File(outDir.getPath() +   File.separator + name + "_" + n + "." + ext);		
		}
		if (copyFile(in, out)) {
			return out;
		}
		return null;
	}
	
	
	
	
	
	// ==============================================================================
	// isTemp()
	//
	// ==============================================================================
	static public boolean isTemp(KeyEvent e) {	
		return e.getKeyChar() == 't' && e.getModifiers() == 0;
	}
	static public boolean isTempDelete(KeyEvent e) {
		int mod = e.getModifiers();
		if (e.getKeyChar() != 't') {
			return false;
		}
		if (mod == InputEvent.CTRL_DOWN_MASK || mod == InputEvent.META_DOWN_MASK)  {
			return true;
		}
		if (mod == InputEvent.CTRL_DOWN_MASK && mod == InputEvent.META_DOWN_MASK)  {
			return true;
		}	
		return false;
	}

	static public boolean isTempViewed(KeyEvent e) {
		return e.getKeyChar() == 'T';
	}

	
	

	
	
	/**
	 * get the properties for the program 
	 * 
	 * The properties Version, DateCompiled and CompilerVersion
	 * are stored in the text-file "pm-properties.txt".
	 * 
	 */
	private static String pmVersion = "unknown";
	private static String pmDateCompiled = "unknown";
	private static String pmCompilerVersion = "unknown";
	private static boolean getPmPropertiesDone = false;
	private static void getPmProperties() {
		if (getPmPropertiesDone) {
			return;
		}
		getPmPropertiesDone = true;
		// read the pm properties from the file

		PM_Utils utils = new PM_Utils();
		URL url = utils.getClass().getClassLoader().getResource(
				PM_PROPERTIES_FILE);

		if (url == null) {
//			System.out.println("pm-properties.txt NOT FOUND");
			return;
		}
		InputStream is = null;
		try {
			is = url.openStream();
		} catch (IOException e) {
			System.out.println("url.openStream() ERROR = " + e);
			return;
		}
		DataInputStream d = new DataInputStream(is);
		BufferedReader ss = new BufferedReader(new InputStreamReader(d));
		try {
			while (true) {
				String line = ss.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
				String[] s = line.split(":",2);
				if (s.length != 2) {
					continue;
				}
				if (s[0].contains("pm-version")) {
					pmVersion = s[1].trim();
					continue;
				}
				if (s[0].contains("date-compiled")) {
					pmDateCompiled = s[1].trim();
					continue;
				}
				if (s[0].contains("compiler-version")) {
					pmCompilerVersion = s[1].trim();
					continue;
				}  
			}
		} catch (IOException e) {
			System.out.println("read ERROR = " + e);
			return;
		}

	}
	static public String getPmVersion() {
		getPmProperties();
		return pmVersion;
	}

	static public String getCompilerVersion() {
		getPmProperties();
		return pmCompilerVersion;
	}

	static public String getDateCompiled() {
		getPmProperties();
		return pmDateCompiled;
	}

	
	
	
	
	
	
	
	
} // Klasse PM_Utils
