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

import pm.sequence.PM_Sequence;
import pm.sequence.PM_SequenceAlbum;
import pm.utilities.PM_Interface;
 

/**
 * Aufbereitung des Suchstrings f�r PM_Search
 * 
 * Mit dem Suchstring wird dann in PM_Search gesucht.
 * 
 * (Der Suchstring wird NUR f�r Lucene ben�tigt. F�r den
 * "Anwender" ist er unzug�nglich, d.h. er kennt ihn nicht und
 * darf ihn auch nicht manipulieren !!!!!)
 * 
 */
public class PM_SearchExpr implements PM_Interface { 

	private SearchType searchType = SearchType.NOTHING;
	private SearchSortType searchSortType = SearchSortType.TIME;

	private PM_Sequence sequenz = null;
	// Falls in einer OPEN eine (interne) CLOSED eingetragen ist.
	private PM_Sequence sortSequenz = null;  
	

	private String searchString = "";
	
	// =====================================================
	// Konstruktor
	// =====================================================
	public PM_SearchExpr(SearchType searchType) {
 		this.searchType = searchType;
	}
	public PM_SearchExpr(SearchType searchType, String searchString) {
 		this.searchType = searchType;
 		this.searchString = searchString;
	}
	
	// ======================================================
	// setSequenz()
	// ======================================================
	public void setSequenz(PM_Sequence sequenz) {
		this.sequenz = sequenz;
		this.sortSequenz = sequenz;
	}

	public PM_Sequence getSequenz() {
		return sequenz;
	}
	public PM_Sequence getSortSequenz() {
		return sortSequenz;
	}

	// =====================================================
	// getSearchString
	// =====================================================
	public String getSearchString() {

		switch (searchType) {

		case NO_INDEX_1 :
			return PM_LuceneDocument.LUCENE_INDEX1 + ":(" + OHNE_INDEX_1 + ")";
		case NO_INDEX_2:
			return PM_LuceneDocument.LUCENE_INDEX2 + ":(" + OHNE_INDEX_2 + ")";
		case SEQ:
			return getSearchStringForSequenze();
		case MINI_SEQ:	 
 			return searchString;	
		case NORMAL:
			return searchString;
		}

		return "";
	}

	// ======================================================
	// getSearchType()
	// getSortType()
	// ======================================================
	public SearchType getSearchType() {
		return searchType;
	}

	public SearchSortType getSortType() {
		return searchSortType;
	}

	// ================================================================
	// ================================================================
	// ============ Private ==========================================
	// ================================================================
	// ================================================================
	// ================================================================
	// ================================================================

	// ======================================================
	// getSearchStringForSequenze()
	// ======================================================
	private String getSearchStringForSequenze() {

		if (sequenz == null)
			return "";

		switch (sequenz.getType()) {
		case EXTENDED:
		case BASE:
		case NEW:
			return PM_LuceneDocument.LUCENE_SEQUENZ + ":"
					+ sequenz.getShortName() + LUCENE_SEQUENZ_TRENNER + "*";
		case ALBUM:
			return getSearchStringFromOpenSequenz((PM_SequenceAlbum)sequenz);
				   
		}

		return "";

	}

	// ======================================================
	// getDatumString()
	//
	// ======================================================
	private String getDatumString(PM_Sequence seq) {

		String von = "";
		String bis = "";
		if (sequenz instanceof PM_SequenceAlbum) {
			PM_SequenceAlbum album = (PM_SequenceAlbum)sequenz;
			von = album.getVon();
			bis = album.getBis();
		}


		String vonJahr = getJJ(von);
		String vonMonat = getMM(von);
		String vonTag = getTT(von);

		String bisJahr = getJJ(bis);
		String bisMonat = getMM(bis);
		String bisTag = getTT(bis);

		// -------------- von ---------------------------------------------
		if (vonJahr.length() != 4)
			vonJahr = "????";
		if (vonMonat.length() != 2)
			vonMonat = "??";
		if (vonTag.length() != 2)
			vonTag = "??";

		von = vonJahr + vonMonat + vonTag;
		if (von.startsWith("?"))
			return "";

		// -------------- bis ---------------------------------------------
		if (bisJahr.length() != 4)
			bisJahr = "????";
		if (bisMonat.length() != 2)
			bisMonat = "??";
		if (bisTag.length() != 2)
			bisTag = "??";
		bis = bisJahr + bisMonat + bisTag;
		if (bis.startsWith("?"))
			return "date:" + von; // nur von eingetragen

		// von UND bis eingetragen
		return "date:[" + von + " TO " + bis + "]";
	}

	private String getJJ(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 1)
			return s[0];
		return "";
	}

	private String getMM(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 2)
			return s[1];
		return "";
	}

	private String getTT(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 3)
			return s[2];
		return "";
	}

	// ======================================================
	// getIndex()
	// ======================================================
	private String getIndex1(PM_SequenceAlbum seq) {
		String indexString = seq.getIndex().trim();
		if (indexString.length() != 0) {
			indexString = PM_LuceneDocument.LUCENE_INDEX1 + ":(" + indexString
					+ ")";
		}
		return indexString;
	}

	// ======================================================
	// getOrtString()
	// ======================================================
	private String getIndex2(PM_SequenceAlbum seq) {
		String ortString = seq.getOrt().trim();
		if (ortString.length() != 0) {
			ortString = PM_LuceneDocument.LUCENE_INDEX2 + ":(" + ortString + ")";
		}
		return ortString;
	}

	// ======================================================
	// getQsString()
	// ======================================================
	private String getQsString(PM_SequenceAlbum seq) {

		String qs = seq.getQual();
		String qString = "";
		if (qs.indexOf("1") >= 0)
			qString += " q:1 ";
		if (qs.indexOf("2") >= 0)
			qString += " q:2 ";
		if (qs.indexOf("3") >= 0)
			qString += " q:3 ";
		if (qs.indexOf("4") >= 0)
			qString += " q:4 ";

		qString = qString.trim();
		if (qString.length() != 0)
			qString = "(" + qString + ")";
		return qString;
	}

	// ======================================================
	// getSeqString()
	// ======================================================
	private String getSeqString(PM_SequenceAlbum sequenz) {

		PM_Sequence seq = sequenz.getSeqClosed();
		if (seq == null)
			return "";
		sortSequenz = seq;
		return PM_LuceneDocument.LUCENE_SEQUENZ + ":" + seq.getShortName()
				+ LUCENE_SEQUENZ_TRENNER + "*";
	}

	// =======================================================================
	// getSearchStringFromOpenSequenz()
	// =======================================================================
	private String getSearchStringFromOpenSequenz(PM_SequenceAlbum seq) {

		// put it all together
		String suchString = "";
		suchString = andToSuchstring(suchString, getQsString(seq));
		suchString = andToSuchstring(suchString, getDatumString(seq).trim());
		suchString = andToSuchstring(suchString, getIndex1(seq));
		suchString = andToSuchstring(suchString, getIndex2(seq));
		suchString = andToSuchstring(suchString, getSeqString(seq));
 	 
		return  "(" + suchString + ") AND NOT (m:m*_)";
 
	}
 
	// ======================================================
	// AND: andToSuchstring();
	// ======================================================
	private String andToSuchstring(String suchString, String addString) {
		String sString = suchString.trim();
		String aString = addString.trim();
		if (sString.length() != 0 && aString.length() != 0) {
			// beide haben Inhalt
			sString += " AND " + aString;
		} else {
			// einer von beiden oder keiner hat Inhalt
			sString += "  " + aString;
		}
		;
		return sString.trim();
	}
}
