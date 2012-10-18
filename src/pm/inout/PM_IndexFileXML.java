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
import pm.picture.*;
import pm.search.*;
import pm.sequence.PM_Sequence;

import org.dom4j.*;
import org.dom4j.tree.DefaultElement;

 

import java.awt.*;
 
 
import java.io.*;
import java.text.*;
 
import java.util.*;
import java.util.List;

 

 
/**
 * lesen/schreiben eine XML-Index-Datei.
 * 
  
 * 
 */
public class PM_IndexFileXML extends PM_XML implements PM_Interface , PM_IndexFile {

	static private final int version = 1;

	static private final String XML_BILDER = "bilder"; // root tag name
//	static private final String XML_MAX_PIC_ID = "max-pic-id";
	static public final String XML_FILE_ID = "file-id";
//	static private final String XML_UUID = "uuid";
	
	
	static private final String XML_BILD = "bild";
//	static private final String XML_PIC_ID = "pic-id"; // unique picture number
	static private final String XML_ID = "id";
	static private final String XML_VALID = "valid";
	static private final String XML_DATUM = "d";
	static private final String XML_DATUM_IMPORT = "import";
	static private final String XML_QS = "qs";
	static private final String XML_HOEHE = "h";
	static private final String XML_BREITE = "b";
	static private final String XML_ROTATE = "drehen";
	static private final String XML_SPIEGELN = "spiegeln";
	static private final String XML_INDEX = "index";
	static private final String XML_ORT = "index2";
	static private final String XML_BEMERKUNGEN = "bemerkungen";
	static private final String XML_BEARBEITET = "bearbeitet";
	static private final String XML_SEQUENCE = "seq";
	static private final String XML_MINI_SQUENCE = "mseq";
	

	static private final String XML_CUT = "cut";
	static private final String XML_CUT_X = "x";
	static private final String XML_CUT_Y = "y";
	static private final String XML_CUT_B = "b";
	static private final String XML_CUT_H = "h";

	// alle bild-Elemente in einer Liste

	private Map<String, Element> pictureValid = new HashMap<String, Element>();
	private Map<String, Element> pictureInvalid = new HashMap<String, Element>();
	
	private String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
 
	private int indexFileID = 0;
//	private String uuid = "";

	// =====================================================
	// Konstruktor
	// =====================================================
	public PM_IndexFileXML(File xmlFile) {
		this.xmlFile = xmlFile;
	 
		rootTagName = XML_BILDER;

		openDoc();
	}

	public int getIndexFileID() {
		return indexFileID;
	}

/*
	public String getUUID() {
		return uuid;
	}
	
	
	public void setUUID(String uuid) {
		uuid = new String(uuid);		
		Element rootElement = document.getRootElement();
		updateAttribute(rootElement, XML_UUID, uuid);
		writeDocument();
	}
	
	*/
	
	
	/**
	 * Delete irreversible all invalid pictures and metadata.
	 * 
	 */
	public int deletePictureInvalid(File dirOrigFile) {
		int size = pictureInvalid.size();
		if (size == 0) {
			return 0;
		}
		Element rootElement = document.getRootElement();
		for (String id: pictureInvalid.keySet()) {
			// alle Dateien l�schen
			File orig = new File(dirOrigFile, id);
			PM_Utils.getFileThumbnail(orig).delete();
			for (File f: PM_Utils.getFilesBearbeitet(orig)) {
				f.delete();
			}
			orig.delete();
			// XML-Element l�schen	
			rootElement.remove(pictureInvalid.get(id));		
		} 
		pictureInvalid.clear();
		if (size != 0) {
			writeDocument();
		}
		
		return size;
	}
	
	
	
	/**
	 * Get the metadata from xml file and put into 
	 * PM_PictureMetadaten.
	 */
	public boolean getMetadaten(PM_Picture picture) {
		 
		Element element  = getElementById(picture.meta.getId());
		if (element == null) {
			return false;
		}
	 
		getMetadaten(picture, element);

		return true;
	}
	
	// =====================================================
	// getElementById()
	//
	// =====================================================
	private Element getElementById(String id) {
		if (pictureValid.containsKey(id)) {
			return pictureValid.get(id);
		}
		if (pictureInvalid.containsKey(id)) {
			return pictureInvalid.get(id);
		}
		return null; 
	}
	
	public void setIndexFileID(int fileID) {
		indexFileID = fileID;
		Element rootElement = document.getRootElement();
		updateAttribute(rootElement, XML_FILE_ID, String.valueOf(fileID));
		writeDocument();
	}
	
	// =====================================================
	// update()
	//
	// Updaten Element.
	//
	// F�r die id wird ein xml-Element gesucht:
	//    gefunden:  dieses updaten
	//    nigefu:  neues f�r die id mit den picture-Werten erzeugen
	// =====================================================
	public void update(PM_Picture picture, String id) {

	 
		// ----------------------------------------------------
		// Element lesen oder neu anlegen
		// ----------------------------------------------------
		Element bildElement = getElementById(id);
		if (bildElement == null) {
			bildElement = new DefaultElement(XML_BILD);
			pictureValid.put(id, bildElement);		
			Element rootElement = document.getRootElement();
			rootElement.add(bildElement);
		} 

		// ----------------------------------------------------
		// Alle Eintraege im bildElement updaten oder neu anlegen
		// ----------------------------------------------------
 
		// ----------- alle Attribute von "bild" --------------------
		updateAttribute(bildElement, XML_ID, id);
		if (picture.meta.isInvalid()) {
			// Picture ist gel�scht
			updateAttribute(bildElement, XML_VALID, "no");	
		} else {
			updateAttribute(bildElement, XML_VALID, null);
		}		
		setElementInList(bildElement, picture.meta.isInvalid(), id);
		
		
		updateDate(bildElement, picture);
		
//		updateAttribute(bildElement, XML_DATUM, picture.meta.getDatum());	
//		updateAttribute(bildElement, XML_DATUM_IMPORT, picture.meta
//				.getDatumImport());
		
		
//		updateAttribute(bildElement, XML_QS, picture.meta.getQs());
		// update qs
		int q = picture.meta.getCategory();
		 
		if (q < 1 || q > 4) {
			q  = 3;			 
		}
		updateAttribute(bildElement, XML_QS, String.valueOf(q));
		
		
		
		
		
		
		
		
		updateImageSize(bildElement, picture);
//		updateAttribute(bildElement, XML_BREITE, picture.meta.getBreite());
//		updateAttribute(bildElement, XML_HOEHE, picture.meta.getHoehe());
		
		
//		updateAttribute(bildElement, XML_ROTATE, picture.meta
//				.getRotationString());
		updateRotation(bildElement, picture);
		
		
		if (picture.meta.getMirror()) {
			updateAttribute(bildElement, XML_SPIEGELN, "S");
		} else {
			updateAttribute(bildElement, XML_SPIEGELN, null);
		}
		String bearbeitet = "";
		if (picture.meta.getModified()) {
			bearbeitet = "ja";
		}
		updateAttribute(bildElement, XML_BEARBEITET, bearbeitet);
		updateAttribute(bildElement, XML_MINI_SQUENCE, picture.meta.getMiniSequence());
		
		// ------- TAGS: index, bemerkungen, sequenz und ort ------------
		addTag(bildElement, XML_INDEX, picture.meta.getIndex1());
		addTag(bildElement, XML_BEMERKUNGEN, picture.meta.getRemarks());
		addTag(bildElement, XML_ORT, picture.meta.getIndex2());
		addTag(bildElement, XML_SEQUENCE, picture.meta.getSequence());
		
		
		
		// ---------- cut Rectangle ----------------------
		java.util.List<Element> cutElements = bildElement.elements(XML_CUT);
		Element cutElement = null;
		if (cutElements.size() != 0)
			cutElement = (Element) cutElements.get(0);
		if (picture.meta.hasCutRectangle() && cutElement == null) {
			// cut- Tag neu anlegen
			cutElement = new DefaultElement(XML_CUT);
			cutElements.add(cutElement);
		}
		if (picture.meta.hasCutRectangle() && cutElement != null) {
			updateCutRectangle(cutElement, picture);
		}

		return;
	}
  
	/**
	 * Update date
	 * 
	 * 
	 */
	private void updateDate(Element bildElement, PM_Picture picture) {		 		
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);			 		
		updateAttribute(bildElement, XML_DATUM, dateFormat.format(picture.meta.getDateCurrent()));	
		updateAttribute(bildElement, XML_DATUM_IMPORT, dateFormat.format(picture.meta.getDateImport()));		
	}
	
	
	/**
	 * Update imageSIze
	 */
	private void updateImageSize(Element bildElement, PM_Picture picture) {
		Dimension imageSize = picture.meta.getImageSize();
		updateAttribute(bildElement, XML_BREITE, String.valueOf(imageSize.width));
		updateAttribute(bildElement, XML_HOEHE, String.valueOf(imageSize.height));
//	updateAttribute(bildElement, XML_BREITE, picture.meta.getBreite());
//	updateAttribute(bildElement, XML_HOEHE, picture.meta.getHoehe());
	}
	
	/**
	 * Update rotation
	 */
	private void updateRotation(Element bildElement, PM_Picture picture) {

		String rotation = "";
		switch (picture.meta.getRotation()) {
		case CLOCKWISE_90_DEGREES:
			rotation = "R";
			break;
		case CLOCKWISE_180_DEGREES:
			rotation = "U";
			break;
		case CLOCKWISE_270_DEGREES:
			rotation = "L";
			break;
		}
		updateAttribute(bildElement, XML_ROTATE, rotation);
	}
	
	
	/**
	 * Update the cutRectangle in cutElement
	 */
	private void updateCutRectangle(Element cutElement, PM_Picture picture) {
		Rectangle rec = picture.meta.getCutRectangle();
		 
		updateAttribute(cutElement, XML_CUT_X, Integer.toString(rec.x));
		updateAttribute(cutElement, XML_CUT_Y, Integer.toString(rec.y));
		updateAttribute(cutElement, XML_CUT_B, Integer.toString(rec.width));
		updateAttribute(cutElement, XML_CUT_H, Integer.toString(rec.height));
		
	}
	
	// =====================================================
	//  
	//                     
	// =====================================================
 

	public int getPictureSizeValid() {
		return pictureValid.size();
	}

	public int getPictureSizeInvalid() {
		return pictureInvalid.size();
	}
	
	

	/**
	 * the index file is complete.
	 * 
	 * All elements are complete after initialization.
	 * Update the lucene index lists and sequence lists.
	 * 
	 */
	public void initComplete() {
		
		 
		PM_LuceneLists luceneListen = PM_LuceneLists.getInstance();
		Set<Integer> miniSequences =  PM_MetadataContainer.getInstance().getMiniSequences();
		for (Element bildElement: pictureValid.values()) { 
			 
			// get all mini sequence numbers (need for new mini sequences)
			String miniSeq = getAttribute(bildElement, XML_MINI_SQUENCE);		
			if (miniSeq.length() > 0 ) {
				System.out.println("            mini seq: " + miniSeq);
				String[] ss = miniSeq.split("_");
				if (ss.length == 2) {
					miniSequences.add(PM_Utils.stringToInt(ss[0]));
				}
				 
				System.out.println("mini seq: " + miniSequences);
			}
			
			// hier noch den Index-Eintrag fuer Liste aller vorhandenen
			// Index-Eintraege sammeln
			luceneListen.initAddIndex1(getTagValue(bildElement, XML_INDEX));
			luceneListen.initAddIndex2(getTagValue(bildElement, XML_ORT));
			PM_MetadataContainer.getInstance().addInitSequence(
					getTagValue(bildElement, XML_SEQUENCE));
		}	
		luceneListen.initComplete();
	}
	
	
	public Set<String> getValidFileNames() {	 
		return new HashSet<String>(pictureValid.keySet());				
	}
	public Set<String> getInvalidFileNames() {	 
		return new HashSet<String>(pictureInvalid.keySet());				
	}
	 
	

	
	
	// =================================================================
	// loeschenIndex1()
	//
	// Alle Index-1 Eintr�ge l�schen
	// ==================================================================
	public void loeschenIndex1() {

		for (Element el: pictureValid.values()) {
			addTag(el, XML_INDEX, ""); // Index-1 l�schen
		}
		for (Element el: pictureInvalid.values()) {
			addTag(el, XML_INDEX, ""); // Index-1 l�schen
		}

		writeDocument();
	}

	// =================================================================
	// loeschenIndex2()
	//
	// Alle Index-2 Eintr�ge l�schen
	// ==================================================================
	public void loeschenIndex2() {

		for (Element el: pictureValid.values()) {
			addTag(el, XML_ORT, ""); // Index-1 l�schen
		}
		for (Element el: pictureInvalid.values()) {
			addTag(el, XML_ORT, ""); // Index-1 l�schen
		}
		
		 

		writeDocument();
	}

	// =================================================================
	// loeschenSeq()
	//
	// Alle Index-2 Eintr�ge l�schen
	// ==================================================================
	public void alleSequenzenLoeschen() {

		for (Element el: pictureValid.values()) {
			addTag(el, XML_SEQUENCE, "");  
		}
		for (Element el: pictureInvalid.values()) {
			addTag(el, XML_SEQUENCE, "");  
		}
	 

		writeDocument();
	}

 

 

	// =====================================================
	// removeID()
	//
	// Ein Eintrag im Document wird geloescht
	// =====================================================
	public void removeID(String id) {
		 

		// holen zu loeschendes Element
		Element bildElemente = getElementById(id);
		if (bildElemente == null) {
			return;
		}

		// l�schen in den Listen (in einer der beiden muss es sein)
		pictureValid.remove(id);
		pictureInvalid.remove(id);
		 
	 
		
		
		// Neue Gesamtliste erstellen
		List<Element> liste = new ArrayList<Element>(pictureValid.values());
		liste.addAll(pictureInvalid.values());
		
		 
		// neu erzeugen
		Element e = new DefaultElement(XML_BILDER);
		e.setContent(liste);
		document.setRootElement(e);
		 
		 

	}

	// =====================================================
	// alleLuceneEintraegeNeuErstellen()
	//                
	// =====================================================
	public void alleLuceneEintraegeNeuErstellen(PM_ListenerX listener) {

		openDoc();

		for (Element bildElement: pictureValid.values()) {
			String id = bildElement.attributeValue(XML_ID);
			File fileOriginal = PM_Utils.xmlIdToFileOriginal(xmlFile, id);
			// Hier PM_Picture mit Metadaten erzeugen und sofort wieder
			// vernichten
			PM_Picture picture = PM_Picture.getPicture(fileOriginal);
			if (picture == null)
				continue;
			getMetadaten(picture, bildElement);
			PM_DatabaseLucene luceneDatenbank = PM_DatabaseLucene
					.getInstance();
			luceneDatenbank.createEintrag(picture);
			if (listener != null)
				listener.actionPerformed(new PM_Action(null, 1));
		}
		return;
	}

 

	// =====================================================
	// bilderOhneSequenzen()
	//      
	//  
	//
	// Liefert eine Liste von Bildern, die keiner Serie zugeordnet sind
	// =====================================================
	public List<PM_Picture> getAllPicturesNotInSequences( ) {

		boolean basisSerie = true;
		
		openDoc();

		List<PM_Picture> liste = new ArrayList<PM_Picture>();

		outerLoop: for (Element bildElement: pictureValid.values()) {
			String seq = getTagValue(bildElement, XML_SEQUENCE);
			if (!(seq == null || seq.length() == 0)) {
				// pr�fen auf s-Sequenzen (keine neuen Bilder)

				String[] sa = seq.split(" ");
				// Loop �ber alle Sequenznamen EINES Bildes
				for (int ii = 0; ii < sa.length; ii++) {
					String name = sa[ii];
					if (basisSerie) {
						if (name.startsWith(SEQ_CLOSED_BASE))
							continue outerLoop;// ist einer Sequenz zugeordnet
					} else {
						if (name.startsWith(SEQ_CLOSED_BASE)
								|| name.startsWith(SEQ_CLOSED_NORMAL))
							continue outerLoop;// ist einer Sequenz zugeordnet
					}

				}
			}
			// Bild ist keiner Sequenz zugeordnet
			String id = bildElement.attributeValue(XML_ID);
			File fileOriginal = PM_Utils.xmlIdToFileOriginal(xmlFile, id);
			PM_Picture pic = PM_Picture.getPicture(fileOriginal);
			if (pic == null)
				continue;
			liste.add(pic);
		}

		return liste;
	}

	// =====================================================
	// bilderDoppelteSequenzen()
	//      
	//  
	//
	// Liefert eine Liste von Bildern, die mehreren Serien zugeordnet sind
	// =====================================================
	public List<PM_Picture> bilderDoppelteSequenzen() {

		openDoc();

		List<PM_Picture> liste = new ArrayList<PM_Picture>();

		for (Element bildElement: pictureValid.values()) {
			String seq = getTagValue(bildElement, XML_SEQUENCE);
			if (!(seq == null || seq.length() == 0)) {
				// pr�fen auf s-Sequenzen (keine neuen Bilder)

				String[] sa = seq.split(" ");
				// Loop �ber alle Sequenznamen EINES Bildes
				int anz = 0;
				for (int ii = 0; ii < sa.length; ii++) {
					String name = sa[ii];
					if (name.startsWith(SEQ_CLOSED_BASE)
							|| name.startsWith(SEQ_CLOSED_NORMAL)) {
						anz++;
					}
				}
				if (anz > 1) {
					// Bild ist mehreren Sequenzen zugeordnet
					String id = bildElement.attributeValue(XML_ID);
					File fileOriginal = PM_Utils.xmlIdToFileOriginal(xmlFile,
							id);
					PM_Picture pic = PM_Picture.getPicture(fileOriginal);
					if (pic == null) {
						continue;
					}
					liste.add(pic);
				}
			}
		}

		return liste;
	}

	// =====================================================
	// bilderNichtInSerien()
	//      
	// Liefert eine Liste von Bildern , die nicht in Serien sind
	// =====================================================
	public List<File> getPicturesNotInSequences(List seqNamen, boolean doppelte) {
		openDoc();
		List<File> liste = new ArrayList<File>();
		for (Element bildElement: pictureValid.values()) {
			String seq = getTagValue(bildElement, XML_SEQUENCE);
			if (doppelte) {
				// Es werden die gesucht, die mehrfach in der Sequenzliste
				// auftauchen
				if (!mehrfach(seq, seqNamen)) {
					continue; // nicht mehrfach
				}
			} else {
				// Es werden die Gesucht, die nicht in der Sequenzliste
				// auftauchen
				if (istInSeq(seq, seqNamen)) {
					continue; // ja, in Sequenzliste
				}
			}
			// nicht in Sequenzliste oder mehrfach in Sequenzliste
			String id = bildElement.attributeValue(XML_ID);
			File fileOriginal = PM_Utils.xmlIdToFileOriginal(xmlFile, id);
			liste.add(fileOriginal);
		}

		return liste;
	}

	// =====================================================
	// istInSeq()
	//             
	// private f�r "bilderNichtInSerien()"
	// =====================================================
	private boolean istInSeq(String seq, List seqNamen) {
		if (seq == null || seq.length() == 0) {
			return false; // nicht in Sequenz
		}
		// seq: s1_001 s5_003
		// seqNamen: s3,s7,s2
		String[] sa = seq.split(" ");
		// Loop �ber alle Sequenznamen EINES Bildes
		for (int ii = 0; ii < sa.length; ii++) {
			String name = PM_Sequence.getSequenzKurzName(sa[ii]);
			// nun pr�fen, ob Name (im Bild eingetragen) einer von den
			// �begebenen ist.
			// Wenn ja kommt DIESES Bild nicht in die Return-Liste
			Iterator it = seqNamen.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof String) {
					String s = (String) o;
					if (s.equals(name)) {
						return true; // ja, ist in Sequenz
					}
				}
			}
		}

		return false; // ist nicht in Sequenz enthalten
	}

	// =====================================================
	// mehrfach()
	//             
	// private f�r "bilderNichtInSerien()"
	// =====================================================
	private boolean mehrfach(String seq, List seqNamen) {
		if (seq == null || seq.length() == 0) {
			return false; // nicht mehrfach
		}
		// seq: s1_001 s5_003
		// seqNamen: s3,s7,s2
		String[] sa = seq.split(" ");
		int treffer = 0;
		// Loop �ber alle Sequenznamen EINES Bildes
		for (int ii = 0; ii < sa.length; ii++) {
			String name = PM_Sequence.getSequenzKurzName(sa[ii]);
			// nun pr�fen, ob Name (im Bild eingetragen) einer von den
			// �begebenen ist.
			// Wenn ja kommt DIESES Bild nicht in die Return-Liste
			Iterator it = seqNamen.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof String) {
					String s = (String) o;
					if (s.equals(name)) {
						treffer++; // ja, ist in Sequenz
					}
				}
			}
		}

		return treffer > 1; // wenn > 1, dann mehrfach
	}

 
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================
	// ======================= PRIVATE ==============================



	 
	/**
	 * Get the metadata from dom element and put into
	 * PM_PictureMetadaten.
	 */
	private void getMetadaten(PM_Picture picture,
			Element bildElement) {
		 
 
		picture.meta.setInit(true);  // init mode proceed
		
		
		
		// ---- Attributes im bild-Tag ----------------------
		if (getAttribute(bildElement, XML_VALID).equals("no")) {
			picture.meta.setInvalid(true);
		} else {
			picture.meta.setInvalid(false);
		}
		
		
		setDate( picture, bildElement);			 	
		picture.meta.setCategory(getAttribute(bildElement, XML_QS));
		setImageSize(picture, bildElement);
		
		
//		picture.meta
//				.setRotationString(getAttribute(bildElement, XML_ROTATE));
		
		setRotation(picture, bildElement);
		
		picture.meta.setMiniSequence(getAttribute(bildElement, XML_MINI_SQUENCE));
// test
String s = getAttribute(bildElement, XML_MINI_SQUENCE);
if (s != null && s.length() != 0) {
	System.out.println("---- mini sequence: " +s); 
}
// end
		String spiegeln = getAttribute(bildElement, XML_SPIEGELN);
		picture.meta.setMirror(spiegeln.equalsIgnoreCase("S"));
		if (getAttribute(bildElement, XML_BEARBEITET).equals("ja")) {
			picture.meta.setModified(true);
		} else {
			picture.meta.setModified(false);
		}		
		// tags
		picture.meta.setIndex1(getTagValue(bildElement, XML_INDEX));
		picture.meta.setRemarks(getTagValue(bildElement, XML_BEMERKUNGEN));
		picture.meta.setIndex2(getTagValue(bildElement, XML_ORT));
		picture.meta.setSequenz(getTagValue(bildElement, XML_SEQUENCE));
		

		// im cut-tag
		List elements = bildElement.elements(XML_CUT);
		Element cutElement = null;
		if (elements.size() != 0) {
			cutElement = (Element) elements.get(0);
		}
		
		setCutRectangle( picture,   cutElement);
		
//		picture.meta.setCutX(getAttribute(cutElement, XML_CUT_X));
//		picture.meta.setCutY(getAttribute(cutElement, XML_CUT_Y));
//		picture.meta.setCutBreite(getAttribute(cutElement, XML_CUT_B));
//		picture.meta.setCutHoehe(getAttribute(cutElement, XML_CUT_H));

		 
		// work done
		picture.meta.setInit(false); // end init mode
		 
	}
	
	/**
	 * Set image size
	 */
	private void setImageSize(PM_Picture picture, Element bildElement) {
		int w = PM_Utils.stringToInt(getAttribute(bildElement, XML_BREITE));
		int h = PM_Utils.stringToInt(getAttribute(bildElement, XML_HOEHE));
		picture.meta.setImageSize(new Dimension(w,h)); 
	}
	
	
//	picture.meta.setBreite(getAttribute(bildElement, XML_BREITE));
//	picture.meta.setHoehe(getAttribute(bildElement, XML_HOEHE));
	
	 
	
	
	/**
	 * set date
	 */
	private void setDate(PM_Picture picture, Element bildElement) {			
		// Current date: must be available !!
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);   
		Date currentDate;
		try {
			currentDate = dateFormat.parse(getAttribute(bildElement, XML_DATUM));
		} catch (ParseException e) {
System.out.println("<<<<<<<<<< ERROR set Date : current date. file " + picture.getFileOriginal().getPath());
			currentDate = new Date(System.currentTimeMillis());
		}
		picture.meta.setDateCurrent(currentDate);
		
		// Import date
		String imprt = getAttribute(bildElement,XML_DATUM_IMPORT);
//System.out.println("Import string = >" + imprt + "<");
		if (imprt.length() == 0) {
			// Import date not available. Current date set to import date.
			picture.meta.setDateImport(new Date(currentDate.getTime()));
			return;
		} 
				
		// I have found the import date	
		Date importDate;
		try {
			importDate = dateFormat.parse(imprt);
		} catch (ParseException e) {
System.out.println("<<<<<<<<<< ERROR set Date : IMPORT date. file " + picture.getFileOriginal().getPath());
			importDate = new Date(System.currentTimeMillis());
		}	
 
		picture.meta.setDateImport(importDate);		 						
	}

	/**
	 * Set rotation
	 */
	private void setRotation(PM_Picture picture, Element bildElement) {		
		String r = getAttribute(bildElement, XML_ROTATE);		
		int rotation = CLOCKWISE_0_DEGREES;
		if (r.equals("R")) {
			rotation = CLOCKWISE_90_DEGREES;
		} else if (r.equals("U")) {
			rotation = CLOCKWISE_180_DEGREES;
		} else if (r.equals("L")) {
			rotation = CLOCKWISE_270_DEGREES;
		}		
		picture.meta.setRotation(rotation);		
	}
 
	/**
	 * Set cutRectangle into metadata
	 */
	private void setCutRectangle(PM_Picture picture, Element cutElement) {
		
		String cutX =  getAttribute(cutElement, XML_CUT_X);
		String cutY =  getAttribute(cutElement, XML_CUT_Y);
		String cutBreite =  getAttribute(cutElement, XML_CUT_B) ;
		String cutHoehe = getAttribute(cutElement, XML_CUT_H) ;
		
		
		Rectangle rec = new Rectangle(PM_Utils.stringToInt(cutX), PM_Utils
				.stringToInt(cutY), PM_Utils.stringToInt(cutBreite), PM_Utils
				.stringToInt(cutHoehe));
		
		picture.meta.setCutRectangle(rec);
		
		
	}
	
	
	/**
	 * Umsetzen des Elements wenn es invalid wird.
	 * 
	 * 
	 */
	private void setElementInList(Element element, boolean deleted, String id) {
		if (deleted) {
			// Element muss in der pictureValid sein
			if (pictureInvalid.containsValue(element)) {
				return;
			}	  
			pictureInvalid.put(id, pictureValid.remove(id));
		} else {
			// Element muss in der bilderListe  sein
			if (pictureValid.containsValue(element)) {
				return;
			}
			pictureValid.put(id, pictureInvalid.remove(id));
		}
		
		
	}
	
	 
	// =====================================================
	// writeDocument()
	//
	// Schreiben Document
	// HIER die EINZIGE Stelle, wenn Document auf Platte geschrieben wird !!!!!!
	// =====================================================
	public void writeDocument () {
		 
		super.writeDocument();
	}

	// =====================================================
	// openDoc()
	//
	// Schreiben Document wenn nicht vorhanden
	// =====================================================
	private void openDoc() {
		if (document != null) {
			return;
		}

		openDocument(OPEN_CREATE);

		setVersionWrite(version);

		
		
		// Hack f�r Window
		
		if (!PM_Utils.isLinux()) {
			boolean changed = false;
			// IDs in der XML Datei �ndern, falls
			// sie unterschiedlich in der Gro�/Kleinschreibg sind
			File origDir = xmlFile.getParentFile().getParentFile(); 
			String[] origNames = origDir.list();
			Element rootElement = document.getRootElement();
			List<Element> elements = rootElement.elements();
			for (int i = 0; i<elements.size(); i++) {
				Element el = (Element)elements.get(i);
				String id = getAttribute(el, XML_ID);
				if (id == null || id.length() == 0) {
					continue;
				}
				for (String name: origNames) {
					if (name.equals(id)) {
						break;
					}
					if (name.equalsIgnoreCase(id)) {
						// ID unterscheidet sich im Case
						updateAttribute(el, XML_ID, name);
						changed = true;
					}
				}
			}
			if (changed) {
				writeDocument();
			}
		}

		
		// bilderListe (Liste aller Bild-Elemente) fuellen
		Element rootElement = document.getRootElement();
		indexFileID = getAttributeInt(rootElement, XML_FILE_ID);
//		uuid = getAttribute(rootElement, XML_UUID);
		List<Element> elements = rootElement.elements();
		for (int i = 0; i<elements.size(); i++) {
			Element el = (Element)elements.get(i);
			String id = getAttribute(el, XML_ID);
			if (id == null || id.length() == 0) {
				continue;
			}
			if (getAttribute(el, XML_VALID).equals("no")) {
				pictureInvalid.put(id, el);			 
			} else {
				pictureValid.put(id, el);
			}						
		}
	}

 
	 

}
