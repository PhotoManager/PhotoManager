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

import java.text.*;
 
import java.util.*;

import pm.picture.*;
import pm.utilities.*;
 

import org.apache.lucene.document.*;

public class PM_LuceneDocument implements PM_Interface {

	static public final String LUCENE_ID = "id";
	static public final String LUCENE_INDEX1 = "i";
	static public final String LUCENE_INDEX2 = "j";
	static public final String LUCENE_TIME = "time"; // in Millisecods (zum
														// Sortieren)
	static public final String LUCENE_DATE = "date"; // yyyyMMdd (zum Suchen)
	static public final String LUCENE_QS = "q";
	static public final String LUCENE_BEARBEITET = "b"; // mit ecternem Programm
														// berabeitet
	static public final String LUCENE_SEQUENZ = "s";
	static public final String LUCENE_MINI_SEQUENZ = "m";

	// ---------- private ---------------

	private String id = "";
	private String category = "";
	private String index = "";
	private String index2 = "";
	private String time = "";
	private String date = "";
	private String modified = ""; //"ja" if the file was modified with an external program, "" otherwise
	private String sequenz = "";
	private String miniSequenz = "";

	private Document doc = new Document();

 
	/**
	 * @param picture
	 * <br>Should not be null
	 * @return A new instance of {@link PM_LuceneDocument}
	 */
	static public PM_LuceneDocument create(final PM_Picture picture) {
		final PM_LuceneDocument result = new PM_LuceneDocument();
		result.id = picture.getFileOriginal().getPath();
		result.category = String.valueOf(picture.meta.getCategory());
		result.index = picture.meta.getIndex1();
		result.index2 = picture.meta.getIndex2();
		final Date date = picture.meta.getDateCurrent();
		result.time = Long.toString(date.getTime());
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		result.date = dateFormat.format(date); // date for search	
		if (picture.meta.getModified()) {
			result.modified = "ja";
		} else {
			result.modified = "";
		}
		result.sequenz = picture.meta.getSequence();
		result.miniSequenz = picture.meta.getMiniSequence();
		
		if (result.makeFields()) {
			return result;
		}

		return null;
	}

	// =====================================================
	// isRelevant()
	//
	// return true, wenn Type relevant fuer Lucene ist,
	// d.h. wenn davon Indizes aufbereitet werden
	// =====================================================
	public static boolean isRelevant(int type) {
		switch (type) {
		case PM_PictureMetadaten.INDEX_1:
		case PM_PictureMetadaten.INDEX_2:
		case PM_PictureMetadaten.QS:
		case PM_PictureMetadaten.DATUM:
		case PM_PictureMetadaten.BEARBEITET: // mit externem Programm
		case PM_PictureMetadaten.LOESCHEN_INDEX_FILE: // Eintrag in XML-Datei
														// loeschen
		case PM_PictureMetadaten.SEQUENCE:
		case PM_PictureMetadaten.MINI_SEQUENCE:
			
			
			
			return true;
		}

		return false;
	}

	// =====================================================
	// Konstruktor
	// =====================================================
	private PM_LuceneDocument() {
		// Deliberately left empty
	}

	 
	/**
	 * Initializes {@code this.doc}'s fields:<ul>
	 *  <li>{@link #LUCENE_INDEX1} with {@code this.index} if it is not blank
	 *  <li>{@link #LUCENE_INDEX2} with {@code this.index2} if it is not blank
	 *  <li>{@link #LUCENE_SEQUENZ} with {@code this.sequenz} if it is not blank
	 *  <li>{@link #LUCENE_QS} with {@code this.category} if it is not blank
	 *  <li>{@link #LUCENE_BEARBEITET} with {@code this.edited} if it is not blank
	 * </ul>
	 * <br>If all of the above are blank, then the "search fields" are initialized:<ul>
	 *  <li>{@link #LUCENE_TIME} with {@code this.time}
	 *  <li>{@link #LUCENE_DATE} with {@code this.date}
	 *  <li>{@link #LUCENE_ID} with {@code this.id}
	 * </ul>
	 * 
	 * @return {@code true} if the "search fields" were initialized
	 */
	private boolean makeFields() {
		// -----------------------------------------------------------
		// prepare search fields
		// -----------------------------------------------------------
		if (index.length() != 0) {
//			System.out.println(index);
			String[] indexes = index.split(" ");
			for (int i = 0; i < indexes.length; i++) {
				String s = indexes[i].trim();
				doc.add(new Field(LUCENE_INDEX1, s, Field.Store.YES,
						Field.Index.UN_TOKENIZED));
			}
		} else {
			// no index
			doc.add(new Field(LUCENE_INDEX1, OHNE_INDEX_1, Field.Store.YES,
					Field.Index.UN_TOKENIZED));
		}
		
		if (index2.length() != 0) {
			String[] orte = index2.split(" ");
			for (int i = 0; i < orte.length; i++) {
				String s = orte[i].trim();
				doc.add(new Field(LUCENE_INDEX2, s, Field.Store.YES,
						Field.Index.UN_TOKENIZED));
			}
		} else {
			// kein Ort
			doc.add(new Field(LUCENE_INDEX2, OHNE_INDEX_2, Field.Store.YES,
					Field.Index.UN_TOKENIZED));
		}

		if (sequenz.length() != 0) {
			String[] sequenzen = sequenz.split(" ");
			for (int i = 0; i < sequenzen.length; i++) {
				String s = sequenzen[i].trim();
				doc.add(new Field(LUCENE_SEQUENZ, s, Field.Store.YES,
						Field.Index.UN_TOKENIZED));
			}
		}

		if (miniSequenz.length() != 0) {
			doc.add(new Field(LUCENE_MINI_SEQUENZ, miniSequenz, Field.Store.YES,
					Field.Index.UN_TOKENIZED));
		}
		
		if (category.length() != 0) {
			doc.add(new Field(LUCENE_QS, category, Field.Store.YES,
					Field.Index.UN_TOKENIZED));
		}

		if (modified.length() != 0) {
			doc.add(new Field(LUCENE_BEARBEITET, modified, Field.Store.YES,
					Field.Index.UN_TOKENIZED));
		}

		// ------------------------------------------------------------
		// no search fields prepared ---> return false
		// ------------------------------------------------------------
		if (doc.getFields().isEmpty()) {
			return false;
		}

		// -----------------------------------------------------------
		// Default fields: id and time
		// -----------------------------------------------------------

		doc.add(new Field(LUCENE_TIME, time, Field.Store.YES,
				Field.Index.UN_TOKENIZED));
		doc.add(new Field(LUCENE_DATE, date, Field.Store.YES,
				Field.Index.UN_TOKENIZED));
		doc.add(new Field(LUCENE_ID, id, Field.Store.YES,
				Field.Index.UN_TOKENIZED));

		return true;
	}

	// =====================================================
	// getDocument()
	// =====================================================
	public Document getDocument() {
		return doc;
	}

}
