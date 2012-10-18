package pm.inout;

import java.awt.Color;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.tree.*;

import pm.gui.*;
import pm.inout.PM_Export.Task;
import pm.picture.PM_Picture;
import pm.search.PM_Search;
import pm.search.PM_SearchExpr;
import pm.sequence.*;
import pm.utilities.PM_Utils;
import pm.utilities.PM_Configuration.*;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;

/**
 * 
 * Bilder auf Stick: Path:
 * urlaub/2012/schweden/oeresund-<seq#>/<lfd#>-<pdir#>_p12002.jpg Beispiel:
 * oeresund-a43/001-23_p23400.jpg /002-3_p44003.jpg /003-345_P3200.JPEG <seq#>
 * short name (a12, a33 ...) <lfd#> laufende Nummer der Bilder in Ausgabe
 * <pdir#> Picture Directory
 * 
 * 
 * 
 * 
 */
public class PM_ExportChangedAlbum extends PM_ExportChanged {

	private List<PM_Sequence> sequences;
	private File outDir;
	private boolean update;
	private Hashtable<String, PM_Sequence> allSequences = new Hashtable<String, PM_Sequence>();
	private File tempFile;

	public PM_ExportChangedAlbum(PM_WindowMain windowMain,
			PM_WindowExport windowExport, List<PM_Sequence> sequences,
			BackUp backUp, boolean update) {
		super(windowMain, windowExport);

		this.update = update;
		this.sequences = sequences;
		outDir = backUp.getFileDirTo();
		outDir.mkdirs();
		tempFile = new File(outDir, "tmp");
		// All sequences from the current album
		List<PM_Sequence> allSequ = PM_TreeModelAlbum.getInstance()
				.getSequenceListClose(null);
		removeNoSequences(allSequ);
		for (PM_Sequence s : allSequ) {
			allSequences.put(getShortName(getFile(s)), s);
			// File f = getFile(s);
			// System.out.println("name: " + getShortName(f) + ", exist: " +
			// f.exists() + ", path: "+ f.getPath());
		}

		for (String s : allSequences.keySet()) {
			// System.out.println("**** key: " + s);
		}

		System.out.println();

		List<File> l = getAllDirs();
		for (File f : l) {
			// System.out.println("  shortName: " + getShortName(f) + ", path: "
			// + f.getPath());
		}

		makeConsistently();

	}

	/*
	 * make the exported data "self-consistent"
	 * 
	 * (1) Delete all invalid data (2) Rename if necesary
	 */
	private void makeConsistently() {
		// (1) Delete all invalid data
		List<File> l = getAllDirs();
		for (File f : l) {
			if (allSequences.containsKey(getShortName(f))) {
				continue;
			}
			// System.out.println("      wird gelöscht: " + getShortName(f) +
			// ", path: " + f.getPath());
			PM_Utils.deleteDirectory(f);
		}
		// (2) rename if nesessary
		for (File targetFile : l) {
			PM_Sequence s = allSequences.get(getShortName(targetFile));
			File sourceFile = getFile(s);
			if (!(sourceFile.getPath().equals(targetFile.getPath()))) {
				// System.out.println("...... must move: source: " +
				// sourceFile.getPath() + ", target: " + targetFile.getPath());
				sourceFile.mkdirs();
				targetFile.renameTo(sourceFile);
			}
		}
	}

	/**
	 *   
	 */
	public void doExport() {
		progressMonitor = new ProgressMonitor(windowExport,
				"Update Album Sequences", "", 0, sequences.size() + 1);
		executeTask();
		PM_Utils.deleteDirectory(tempFile);
		PM_Utils.deleteEmptyDirectories(outDir);
		return;
	}

	private int sequencesWritten = 0;

	protected void taskDoInBackground() {
		sequencesWritten = 0;
		int n = 1;
		for (PM_Sequence s : sequences) {
			task.setTheProgress(n);
			progressMonitor.setNote(s.getLongName()); // + "<" +
														// s.getShortName() +
														// ">");
			writeSequence(s);
			sequencesWritten++;
			n++;
			if (progressMonitor.isCanceled()) {
				System.out.println("******************* cancel task");
				task.cancel(true);
				return;
			}
		}
	}

	protected void taskDone() {

		if (sequences.size() != sequencesWritten) {
			String msg = sequencesWritten + " von " + sequences.size()
					+ " Sequenzen geschrieben";
			JOptionPane
					.showConfirmDialog(null, msg, "Album export",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE);
			System.out.println("---xxxxxxxx progressMonitor is canceled ");
		} else {
			JOptionPane.showConfirmDialog(null, "Alle Sequenzen geschrieben",
					"Album export", JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);

		}

	}

	/**
	 * Init Values to change the picture.
	 * 
	 * TODO Export Album: Init values to change must get from "config file"
	 */
	protected void getInitValues() {
		cut = true;
		rotateReflected = true;
		ratio = 0;
		resolution = 0;
		overscan = false;
		if (overscan) {
			overscanX = windowExport.getOverscanValueX();
			overscanY = windowExport.getOverscanValueY();
			testOverscan = windowExport.getTestOverscancolor();
		} else {
			overscanX = 0;
			overscanY = 0;
			testOverscan = false;
		}
		imageText = false;
		colorBG = null;
		colorFG = null;
		transparent = false;
	}

	static public void removeNoSequences(List<PM_Sequence> sequences) {
		Iterator<PM_Sequence> it = sequences.iterator();
		while (it.hasNext()) {
			PM_Sequence seq = it.next();
			if (!(seq instanceof PM_SequenceAlbum)) {
				it.remove();
				continue;
			}
			String shortName = seq.getShortName().trim();
			// remove seq if leaf without a sequence
			if (shortName.length() == 0
					|| shortName.equals(SEQ_CHARACTER_ALBUM)) {
				it.remove();
			}
		}
	}

	private void writeSequence(PM_Sequence seq) {
		File targetDir = getFile(seq);
		if (update && !targetDir.exists()) {
			return;
		}

		targetDir.mkdirs();
		System.out.println("####  write sequence: " + targetDir.getPath());

		// source
		List<PM_Picture> source = getPictureList(seq);

		// target
		List<File> target = new ArrayList<File>();
		Collections.addAll(target, targetDir.listFiles());
		Collections.sort(target);

		if (checkSequence(seq, source, target)) {
			return;
		}
		writeSequence_(seq, targetDir, source);
	}

	private boolean checkSequence(PM_Sequence seq, List<PM_Picture> source,
			List<File> target) {
		if (source.size() != target.size()) {
			return false;
		}
		// check
		for (int i = 0; i < source.size(); i++) {
			File trg = target.get(i);
			String srcFileKey = source.get(i).getPictureKey();
			String trgFileKey = getFileKey(trg);
			// System.out.println("  source: " + srcFileKey + ", target: "
			// + trgFileKey);
			if (!(srcFileKey.equals(trgFileKey))) {
				System.out
						.println(" >>>>>>>>>>>>>>> source key ungleich target");
				return false;
			}
		}

		for (int i = 0; i < source.size(); i++) {
			File src = source.get(i).getFileOriginal();
			File fileThumbnail = PM_Utils.getFileThumbnail(src);
			File trg = target.get(i);

			if (fileThumbnail.lastModified() != trg.lastModified()) {
				System.out.println("        last modified ungleich: "
						+ trg.getPath());
				writeMyPicture(source.get(i), trg, i + 1, source.size(), seq);
				trg.setLastModified(fileThumbnail.lastModified());
			}
			// System.out.println("        source: " + src.getPath()
			// +",target: "+trg.getPath());
		}
		return true;
	}

	private void writeSequence_(PM_Sequence seq, File targetDir,
			List<PM_Picture> source) {
		// System.out.println("++++ write Sequence with move target to temp dir");
		// move target to temp dir

		PM_Utils.deleteDirectory(tempFile);
		tempFile.mkdir(); // deleteDirectory destroy the dir !!!

		targetDir.renameTo(tempFile);
		targetDir.mkdir();
		Hashtable<String, File> sss = new Hashtable<String, File>();
		for (File f : tempFile.listFiles()) {
			sss.put(getFileKey(f), f);
		}

		for (int i = 0; i < source.size(); i++) {
			if (progressMonitor.isCanceled() || task.isDone()) {
				return;
			}
			PM_Picture pic = source.get(i);
			File fileThumbnail = PM_Utils.getFileThumbnail(pic
					.getFileOriginal());
			String key = pic.getPictureKey();
			File targetFile = new File(targetDir, getFileName(pic, i + 1));
			if (sss.containsKey(key)) {
				File f = sss.get(key);
				if (fileThumbnail.lastModified() == f.lastModified()) {
					f.renameTo(targetFile);
					continue;
				}
			}

			writeMyPicture(source.get(i), targetFile, i + 1, source.size(), seq);
			targetFile.setLastModified(fileThumbnail.lastModified());
		}
	}

	protected boolean writeMyPicture(PM_Picture picture, File fileWrite,
			int bildNr, int bilderGes, PM_Sequence seq) {
		progressMonitor.setNote(seq.getLongName() + " (" + bildNr + "/"
				+ bilderGes + "): " + picture.getFileOriginal().getName());
		// System.out.println("#### Write Picture. Target: " +
		// fileWrite.getPath()) ;
		fileWrite.delete();
		boolean ret = writePicture(picture, fileWrite, bildNr, bilderGes);
		return ret;
	}

	private List<PM_Picture> getPictureList(PM_Sequence seq) {
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(seq);
		PM_Search luceneSuchen = new PM_Search(searchExpr);
		int anzahl = luceneSuchen.search();
		if (anzahl == 0) {
			return new ArrayList<PM_Picture>();
		}
		return luceneSuchen.getPictureList(SearchSortType.SEQ);
	}

	private File getFile(PM_Sequence seq) {
		String path = seq.getPath().replaceAll("\\.", File.separator) + "-"
				+ seq.getShortName();
		return new File(outDir, path);
	}

	private String getShortName(File file) {
		int i = file.getName().lastIndexOf('-');
		return (i < 0) ? "" : file.getName().substring(i + 1);
	}

	private String getFileName(PM_Picture pic, int lfd) {
		String lfdString = PM_Utils.stringToString(
				"00000" + Integer.toString(lfd), 4);
		return lfdString + "-" + pic.getPictureKey();
	}

	private String getFileKey(File f) {
		String[] ss = f.getName().split("-", 2);
		if (ss.length > 1) {
			return ss[1];
		}
		return "";

	}

	/**
	 * get all directories having shortName in file-name.
	 * 
	 * That are all the directories having picture-fies Example:
	 * /home/dih/tmp/urlaub/hedemora-a200 /home/dih/tmp/urlaub/italien/pisa-a202
	 * .....
	 */
	private List<File> getAllDirs() {
		List<File> list = new ArrayList<File>();
		readDir(outDir, null, list);
		return list;
	}

	/**
	 * return File having shortName in file Name.
	 * 
	 * Example: getDir("-a22") result: /home/dih/tmp/urlaub/sverige-a22
	 * 
	 * @return null - not found
	 */
	private File getDir(String shortName) {
		List<File> list = new ArrayList<File>();
		readDir(outDir, shortName, list);
		return (list.isEmpty() ? null : list.get(0));
	}

	private void readDir(File dir, String shortName, List<File> list) {
		if (shortName != null && !list.isEmpty()) {
			return;
		}
		File[] files = dir.listFiles();
		for (File file : files) {
			if (PM_Utils.isPictureFile(file)) {
				list.add(dir);
				return;
			}
		}
		for (File file : files) {
			if (file.isDirectory()) {
				readDir(file, shortName, list);
				if (shortName != null && !list.isEmpty()) {
					return;
				}
			}
		}
	}

}
