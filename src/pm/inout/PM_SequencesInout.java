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

import java.util.*;

 
import pm.picture.*;
 
import pm.sequence.*;
import pm.utilities.*;
 

/**
 * schreibt und liest die Sequenzen
 * 
 * Bei Programmstart werden alle Sequenzen von der Platte gelesen.
 * 
 * Zwischendurch wird nicht mehr gelesen
 * 
 * Bei Programmende werden alle Sequenzen, wenn Änderungen anstehen, auf die
 * Platte geschrieben.
 * 
 * 
 */
public class PM_SequencesInout extends PM_SequencesInoutXML implements
		PM_Interface {

	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	private static PM_SequencesInout instance = null;

	static public PM_SequencesInout getInstance() {
		if (instance == null)
			instance = new PM_SequencesInout();
		return instance;
	}

	// =====================================================
	// Konstruktor
	// =====================================================
	protected PM_SequencesInout() {
		 
	}

	// ========================================================
	// open()
	// 
	// Damit wird nur die Datei eröffnet.
	// Es wird noch nichts gelesen.
	// ========================================================
	public void open() {
		super.open();
	}

	// =====================================================
	// flush()
	// close()
	// =====================================================
	public void flush() {
		// TODO wenn flush dann close ?????
		close();
	}

	public void close() {

		if (!changed) {
			return; // Inhalt wurde nicht verändert
		}

		changed = false; // da flush nochmals aufgerufen werden kann

		// Jetzt Datei zurückschreiben und close
		List<PM_Sequence> liste = PM_TreeModelBase.getInstance()
				.getSequenceListClose(null);
		liste.addAll(PM_TreeModelAlbum.getInstance().getSequenceListClose(null));
		liste.addAll(PM_TreeModelExtended.getInstance().getSequenceListClose(null));
		liste.addAll(PM_TreeModelNew.getInstance().getSequenceListClose(null));

		super.close(liste);
	}
	
	public int getMaxSequenceNumber() {
		return maxSequNumber;
	}

	public void setMaxSequenceNumber(int maxSequNumber) {
		this.maxSequNumber = maxSequNumber;
	}
	
	public int getMaxSequAlbumNumber() {
		return maxSequAlbumNumber;
	}

	public void setMaxSequAlbumNumber(int maxSequAlbumNumber) {
		this.maxSequAlbumNumber = maxSequAlbumNumber;
	}
	
	// ========================================================
	// Initialisierung: getList()
	// 
	// Es wird die Liste der Sequenzen für die Init-Phase
	// aus der XML-Datei gelesen.
	// Die Liste ist mit den vorhandenen Serien in den Metadaten
	// abgeglichen.
	// ========================================================
	private boolean readDone = false;

	public List<PM_Sequence> getList(SequenceType type) {

		if (readDone == false) {
			readDone = true;
			readAllSequences();
		}

		switch (type) {
		case ALBUM:
			return listAlbum;
		case BASE:
			return listBase;
		case EXTENDED:
			return listExtended;
		case NEW:
			return listNew;
		}

		return new ArrayList<PM_Sequence>();
	}

	// ========================================================
	// setChangeListener()
	// 
	//  Dies ist ein Hack !!!
	// (zum Schluss werden die ChangeListerner gesetzt)
	// ========================================================
	private boolean setChangeListenerDone = false;
	public void setChangeListener() {
		if (setChangeListenerDone)
			return;
		setChangeListenerDone = true;

		// Add ChangeListerner
		PM_Listener l = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				getInstance().setChanged(true);
			}
		};
		PM_TreeModelBase.getInstance().addChangeListener(l);
		PM_TreeModelExtended.getInstance().addChangeListener(l);
		PM_TreeModelAlbum.getInstance().addChangeListener(l);
		PM_TreeModelNew.getInstance().addChangeListener(l);
	}

	// =====================================================
	// readAllSequences()
	// =====================================================
	@Override
	protected void readAllSequences() {

		PM_MetadataContainer metadatenContainer = PM_MetadataContainer
				.getInstance();

		// --------------------------------------------------
		// alle aus xml-Datei lesen
		// --------------------------------------------------
		super.readAllSequences();
		// ------------------------------------------------------------
		// jetzt noch die fehlenden neu erzeugen
		// ------------------------------------------------------------
		Enumeration en = metadatenContainer.getInitSequences().keys();
		while (en.hasMoreElements()) {
			String sequenceName = (String) en.nextElement();
//System.out.println("readAllSequences Name" + sequenceName);
			int count = metadatenContainer.getInitSequences().get(sequenceName);
			if (sequenceName.startsWith(SEQ_CHARACTER_BASE)) {
				PM_SequenceBase sequence = new PM_SequenceBase(
						SEQU_NAME_UNKNOWN, sequenceName);
				sequence.setAnzahlBilder(count);
				listBase.add(sequence);
				changed = true;
				continue;
			} else if (sequenceName.startsWith(SEQ_CHARACTER_EXTENDED)) {
				PM_SequenceExtended sequence = new PM_SequenceExtended(
						SEQU_NAME_UNKNOWN, sequenceName);
				sequence.setAnzahlBilder(count);
				listExtended.add(sequence);
				changed = true;
				continue;
			} else if (sequenceName.startsWith(SEQ_CHARACTER_NEW)) {
				PM_SequenceNew sequence = new PM_SequenceNew(SEQU_NAME_UNKNOWN,
						sequenceName);
				sequence.setAnzahlBilder(count);
				listNew.add(sequence);
				changed = true;
				continue;
			}
		}
		// ---- Album -------
		// first check the maxAlbumSequenceNumber
		Iterator<PM_Sequence> it1 = listAlbum.iterator();
		while (it1.hasNext()) {
			PM_SequenceAlbum seqAlbum = (PM_SequenceAlbum) it1.next();
			int nextNumber = PM_SequencesInout.getInstance().getMaxSequAlbumNumber();
			nextNumber = Math.max(nextNumber, seqAlbum.getSequenceNumber());
			PM_SequencesInout.getInstance().setMaxSequAlbumNumber(nextNumber);		
		}
		// set shortName if not exist and
		// set closed sequence instance.
		Iterator<PM_Sequence> it2 = listAlbum.iterator();
		while (it2.hasNext()) {
			PM_SequenceAlbum seqAlbum = (PM_SequenceAlbum) it2.next();
			if (seqAlbum.getSequenceNumber() == 0) {
				String shortName = SEQ_CHARACTER_ALBUM + PM_Sequence.getNextFreeAlbumSequenceNumber();				
				seqAlbum.setShortName(shortName);
			}
			if (seqAlbum.getStringLeaf()) {
				continue;
			}
			String name = seqAlbum.getSeqClosedName();
			if (name.length() == 0) {
				continue;
			}
			PM_Sequence seqB = getSequence(name, listBase);
			if (seqB != null) {
				seqAlbum.setSeqClosed(seqB);
				continue;
			}
			PM_Sequence seqX = getSequence(name, listExtended);
			if (seqX != null) {
				seqAlbum.setSeqClosed(seqX);
				continue;
			}
			// Es wurde kein Eintrag gefunden
			seqAlbum.setSeqClosedName("");
			changed = true;
		}
		 

	}

	
	
	
	
	
	// =====================================================
	// setNeueBilder()
	//
	// Damit wird die PM_Sequence der neuen Bilder bekannt gemacht.
	// =====================================================
	private PM_Sequence neueBilder = null;
	public PM_Sequence getNeueBilder() {
		return neueBilder;
	}

	// =====================================================
	// setChanged()
	//
	// Es wurde Änderungen in den Sequenzen durchgeführt.
	// Bei close Datei zurückschreiben.
	// =====================================================
    public void setChanged(boolean ch) {
//		System.out
//				.println(">>>> PM_SequencesInout <<<<<. setChanged aufgerufen");
		changed = ch;
	}

	// ===============================================================
	// getSequenz()
	//
	// suchen Sequenz (mit dem Namen der Sequenz) in einer Liste mit Sequenzen
	// ===============================================================
	private PM_Sequence getSequence(String name, List<PM_Sequence> liste) {
		for (PM_Sequence seq: liste) {
			if (name.equals(seq.getShortName())) {
				return seq;
			}
		}
		return null;
	}

}
