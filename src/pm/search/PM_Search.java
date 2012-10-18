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
package pm.search;

 

import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;
 

import java.util.*;

import java.io.*;

import org.apache.lucene.document.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;

/**
 * Suchen in der Lucene-Datenbank.
 * 
 * 
 * 
 * Mit einer "PM_SearchExpr", die den kompletten Suchstring enth�lt, wird hier
 * in der Lucene-DB gesucht. Aussnahmen: f�r eine Sequenz vom Type
 * "SEQ_ALLE_BILDER" (wird hier direkt im Directory gescht)
 * 
 * Das Ergebnis der Suche ist eine Liste (myResultFileList), die unsortiert ist.
 * Wird das Ergebis abgerufen wird die Ergebnisliste wie gew�nscht aufbereitet
 * (mit PM_Pictures oder nur Files ...)
 * 
 * 
 */
public class PM_Search implements PM_Interface {

	private File fileLuceneDB = null;
	private Hits hits = null;
	private int anzahlHits = 0;
	private Searcher searcher = null;
	private List<Result> resultList = null;
	private PM_SearchExpr searchExpression = null;

	private SearchType searchType = SearchType.NOTHING; 
	private PM_Sequence sequenz = null;
	private PM_Sequence sortSequenz = null; 
	
	// =====================================================
	// Konstruktor
	// =====================================================
	public PM_Search(PM_SearchExpr searchExpression) {

		this.searchExpression = searchExpression;
		PM_Configuration einstellungen = PM_Configuration
				.getInstance();
		fileLuceneDB = einstellungen.getFileHomeLuceneDB();

	}

	// =====================================================
	// search ()
	//
	// Hier wird jetzt gesucht und das Ergebenis in die
	// Liste "resultList" eingetragen.
	//
	// return: counted hits
	// =====================================================
	public int search() {
		
		searchType = searchExpression.getSearchType();
		sequenz = searchExpression.getSequenz();
		sortSequenz = sequenz;
		
		resultList = new ArrayList<Result>();

		 
		// evtl. ist ja garnichts zu suchen ?
		if (searchExpression == null || searchType == SearchType.NOTHING) {
			return 0;
		} 
		// und dieses darf nicht sein (suchen nach Sequenz und es gibt gar keine)
		if (searchType == SearchType.SEQ &&  sequenz == null) {		
			 return 0;
		}
		
		// jetzt kann gesucht werden		
		return searchIntern();
	}

	// =====================================================
	//
	// Sortierte Ergebnislisten holen:
	// 
	// getFileList()
	// getPictureList()
	// =====================================================
	public List<File> getFileList(SearchSortType sortType) {
		return getSortetFileList(sortType);
	}

	public List<PM_Picture> getPictureList(SearchSortType sortType) {
		return PM_Pictures.getPictureList(getFileList(sortType));
	}

	// =====================================================
	// getAnzahlHits()
	// =====================================================
	public int getAnzahlHits() {
		return anzahlHits;
	}

	// ====================================================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ================ private ===========================================
	// ====================================================================

	// =====================================================
	// suchen ()
	//
	// Suchen nach searchString in "searchExpression"
	// =====================================================
	private int searchIntern() {

		// -----------------------------------------------------------------------
		// Suchen nach Dir (direkt im realen Verzeichnis, nicht �ber lucene
		// suchen)
		// ----------------------------------------------------------------------
		if (searchType == SearchType.SEQ &&  sequenz.getType() == SequenceType.ORIGINAL) {		
			return searchAlleBilder(sequenz);
		}

		// -------------------------------------------------------------------
		// jetzt nur �ber lucene suchen
		// -------------------------------------------------------------------
		String searchString = searchExpression.getSearchString();
		
//System.out.println(searchString);
		
		sortSequenz = searchExpression.getSortSequenz();

		if (searchString.trim().length() == 0)
			return 0;

		// jetzt suchen

		// evtl. ausstehende Updates durchfuehren (letzte Gelegenheit)
		PM_DatabaseLucene.getInstance().flush();

		// // System.out.println("PM_LuceneSuchent. Suchstring = " +
		// suchString);
		anzahlHits = 0;
		Query query = getQuery(searchString);
		if (query == null)
			return 0;

		// ----------------------------------------------
		// jetzt mit dem aufbereitetem Suchstring suchen
		// ----------------------------------------------
		hits = searchHits(query);
		if (hits == null)
			return 0; // nichts gefunden

		// ---------------------------------------
		// Es wurden Treffer gefunden
		// ---------------------------------------
		anzahlHits = hits.length();

		// ---------------------------------------
		// Treffer jetzt in einer sorted Collection aufbereiten
		// ---------------------------------------

		try {
			for (int i = 0; i < anzahlHits; i++) {
				Document doc = hits.doc(i);
				// hier wird noch nicht sortiert
				addDocumentToErgebnisListe(doc);
			}
		} catch (Exception e) {
			// // System.out.println("PM_LuceneSuchen.Konstruktor (suchen)
			// caught a " + e.getClass()
			// // + "\n with message: " + e.getMessage());
			return 0;
		}

		// Alle Treffer ausgewertet
		if (searcher != null) {
			try {
				searcher.close();
			} catch (Exception e) {
				return 0;
			}
		}

		// --- return mit Anzahl Treffer
		return anzahlHits;

	}

	// =====================================================
	// searchAlleBilder()
	//
	// sequenz.getType() == SEQ_ALLE_BILDER
	// =====================================================
	private int searchAlleBilder(PM_Sequence seq) {

		List fileList = seq.getFileListFromDirectory();

		Iterator it = fileList.iterator();
		while (it.hasNext()) {
			File file = (File) it.next();
			if (!file.isFile())
				continue;
			if (!PM_Utils.isPictureFile(file)) {
				continue;
			}
			// MyResult wird hier nur mit file aufbereitet, da
			// i.d.R. nur nach File-Name sortiert wird
			resultList.add(new Result(file,"",""));
		}

		return resultList.size();
	}

	// =====================================================
	// getSortetFileList()
	//
	// resultList sortieren
	// ====================================================
	private List<File> getSortetFileList(SearchSortType sortType) {
		
		
		Comparator<Result> sorter = null;
		switch (sortType) {
		case TIME:
			sorter = ORDER_TIME;
			break;
		case SEQ:
			sorter = ORDER_SEQ;
			break;
		case FILE_PATH:
			sorter = ORDER_FILE_PATH;
			break;
		case FILE_NAME:
			sorter = ORDER_FILE_NAME;
			break;
			
		}
	 
		if (sorter != null) {
			Collections.sort(resultList,sorter);			
		}
		
		// Sortiertes Ergebnis in eine FileList bringen
		List<File> fileList = new ArrayList<File>();
		Iterator<Result> iterator = resultList.iterator();
		while (iterator.hasNext()) {
			Result result = iterator.next();
			fileList.add(result.file);
		}
		return fileList;
	}

	// =====================================================
	// getQuery()
	// =====================================================
	private Query getQuery(String suchString) {
		Analyzer analyzer = new WhitespaceAnalyzer(); // new
		// StandardAnalyzer();
		Query query = null;
		try {
			QueryParser queryParser = new QueryParser("", analyzer);
			query = queryParser.parse(suchString);
		} catch (Exception e) {
			return null;
		}
		return query;
	}

	// =====================================================
	// search()
	// =====================================================
	private Hits searchHits(Query query) {
		searcher = null;
		Hits hits = null;

		try {
			searcher = new IndexSearcher(fileLuceneDB.getPath());
			hits = searcher.search(query);
		} catch (Exception e) {
			// System.out.println("PM_LuceneSuchen.suchen: caught a " +
			// e.getClass() + "\n with message: "
			// + e.getMessage());
			return null;
		}
		return hits;
	}

	// =================================================================
	// addDocumentToErgebnisListe()
	//
	// Jedes gefundene Document wird zu einem Eintrag in der Liste
	// aufbereitet.
	// Es wird noch NICHT sortiert
	// =================================================================
	private void addDocumentToErgebnisListe(Document document) {

		String time = "";
		String id = "";
		String sequenzString = "";
		 

		// ----------------------------------------
		// id (Path vom Original File)
		// ----------------------------------------
		String[] ids = document.getValues(PM_LuceneDocument.LUCENE_ID);
		if (ids.length < 0) {
			// System.out.println("LUCENE suchen. 'id' (Path original File)
			// nicht gefunden");
			return;
		}
		id = ids[0];

		// ----------------------------------------
		// Zeit in Millisekunden
		// ----------------------------------------

		String[] times = document.getValues(PM_LuceneDocument.LUCENE_TIME);
		if (times.length >= 0) {
			time = times[0];
		} else {
			// System.out.println("LUCENE suchen. TIME in Milliseconds nicht
			// gefunden");
		}
 

		// ----------------------------------------
		// Sequenzen
		// ----------------------------------------
		if (sequenz != null) {
			String[] sequenzen = document
					.getValues(PM_LuceneDocument.LUCENE_SEQUENZ);
			String sequenzName = sortSequenz.getShortName();
			if (sequenzen != null && sequenzName != null) {
				// Eine Sequenz wurde gefunden

				for (int i = 0; i < sequenzen.length; i++) {

					String s = sequenzen[i].trim();
					// System.out.println("...++ Beim Suchen gefundene Sequenz:
					// " + s + ", sequ-name = " + sequenzName);
					if (s.indexOf(sequenzName + LUCENE_SEQUENZ_TRENNER) == 0) {
						// System.out.println(" " + sequenzen[i]);
						sequenzString = sequenzen[i];
						break; // es gibt ja nur eine g�ltige !!!!
					}
				} // for - loop
			} // if sequenz orhanden
		}

		// das wars !!!
		resultList.add(new Result(new File(id), time, sequenzString ));

	}

	// =====================================================
	// ORDER_TIME:
	// resultList nach Zeit sortieren
	// =====================================================
	static private final Comparator<Result> ORDER_TIME = new Comparator<Result>() {
		public int compare(Result result1, Result result2) {
			String time1 = result1.timeMilliseconds;
			String time2 = result2.timeMilliseconds;
			return time1.compareTo(time2);
		};
	};

	// =====================================================
	// ORDER_SEQ:
	// resultList nach Sequenznamen sortieren
	// =====================================================
	static private final Comparator<Result> ORDER_SEQ = new Comparator<Result>() {
		public int compare(Result result1, Result result2) {
			String sequenz1 = result1.sequenzString;
			String sequenz2 = result2.sequenzString;
			return sequenz1.compareTo(sequenz2);
		};
	};

	// =====================================================
	// ORDER_FILE_NAME:
	//   nach Bildnamen
	// =====================================================
	static private final Comparator<Result> ORDER_FILE_NAME = new Comparator<Result>() {
		public int compare(Result result1, Result result2) {
			String name1 = result1.name;
			String name2 = result2.name;
			return name1.compareTo(name2);
		};
	};
	
	// =====================================================
	// ORDER_FILE_PATH:
	//   nach Bildnamen
	// =====================================================
	static private final Comparator<Result> ORDER_FILE_PATH = new Comparator<Result>() {
		public int compare(Result result1, Result result2) {
			String path1 = result1.path;
			String path2 = result2.path;
			return path1.compareTo(path2);
		};
	};
	
	 
	
	
	
	// ============================================================
	// ============================================================
	// InnerClass: Result
	// ============================================================
	// ============================================================

	class Result {

		public File file;
		public String path;
		public String name;
		public String timeMilliseconds;
		public String sequenzString;
		 
		// =====================================================
		// Konstruktor: fileOriginal und time in Milliseconds
		// =====================================================
		public Result(File file, String timeMilliseconds,
				String sequenzString) {
			this.file = file;
			this.timeMilliseconds = timeMilliseconds;
			this.sequenzString = sequenzString;
			path = file.getPath();
			name = file.getName();
			 
		}

		 

	}

}
