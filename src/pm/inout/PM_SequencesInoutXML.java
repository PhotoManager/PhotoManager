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

 
import pm.picture.*;
import pm.sequence.*;
import pm.utilities.*;
 
 
 

import org.dom4j.*;

import java.util.*;
import java.io.*;

//import javax.xml.stream.events.XMLEvent;


/** Schreiben und lesen der Sequenzdaten in eine xml-Datei
 *   
 * (die 
 */
public class PM_SequencesInoutXML extends PM_XML implements PM_Interface {

	protected List<PM_Sequence> listAlbum = new ArrayList<PM_Sequence>();	
	protected List<PM_Sequence> listBase = new ArrayList<PM_Sequence>();
	protected List<PM_Sequence> listExtended = new ArrayList<PM_Sequence>();
	protected List<PM_Sequence> listNew = new ArrayList<PM_Sequence>(); 	
	protected int getAttributeInt = 0;
	protected int maxSequNumber = 0;
	protected int maxSequAlbumNumber = 0;
	
	static protected boolean changed = false;
 
	
	// =====================================================
	// Konstruktor
	// =====================================================
	protected PM_SequencesInoutXML() {
		rootTagName = "pm-metadaten-global";	 
	}
	
	// =====================================================
	// open()
	//
	// =====================================================
	protected void open() {
		
		if (xmlFile != null) return ; // bereits eröffnet
		
		
		File homeBilder = PM_Configuration.getInstance().getTopLevelPictureDirectory();	
		 
		String path = homeBilder.getAbsolutePath() + File.separator + DIR_METADATEN_ROOT
			+ File.separator + FILE_XML_SEQUENZEN;
		xmlFile = new File(path);
		
		
		openDocument(OPEN_CREATE);
					 
		 
		
	}

	 
	protected void close(List<PM_Sequence> sequenzen) {
		
		// create empty document
		document = DocumentHelper.createDocument(); 	 
		document.addElement(rootTagName);
 
		// write all sequence headers to document
		updateDocument(sequenzen);
		
		// write document to harddisc
		writeDocument();
		
	 
		
		
	}

	// ====================== PRIVATE ========================================
	// ====================== PRIVATE ======================================== 
	// ====================== PRIVATE ======================================== 
	// ====================== PRIVATE ======================================== 
	// ====================== PRIVATE ======================================== 
	// ====================== PRIVATE ======================================== 
	// ====================== PRIVATE ======================================== 

	// =====================================================
	// private: readToc()
	//
	//  <alle-tocs>
	//     <toc> ....
	//     ...
	//  </alle-tocs>
	//
	// =====================================================
	static public final String TAG_ALLE_TOCS = "alle-tocs";
	static public final String TAG_ALLE_ATTR_MAX_SEQUENCE_NUMBER = "max-seq-number";
	static public final String TAG_ALLE_ATTR_MAX_ALBUM_SEQUENCE_NUMBER = "max-album-seq-number";
	
	static public final String TAG_TOC = "toc";
	public static final String TOC_ATTR_TYPE = "type";
	public static final String TOC_ATTR_VON = "von";
	public static final String TOC_ATTR_BIS = "bis";
	public static final String TOC_ATTR_INHALT = "inhalt";
	
	public static final String TOC_ATTR_INDEX = "index";
	public static final String TOC_ATTR_QS = "qs";
	public static final String TOC_ATTR_ORT = "ort";
	public static final String TOC_ATTR_NAME = "name";
	public static final String TOC_ATTR_SG_NAME = "sg-name"; // Geschl.-Sequ.-Name in einer offenen Sequenz
	public static final String TOC_ATTR_ANZAHL = "anzahl";
	public static final String TOC_ATTR_MARKIEREN = "markieren";

	
	protected void readAllSequences( ) {
		
		 
		listNew = new ArrayList<PM_Sequence>(); 	
		listBase = new ArrayList<PM_Sequence>();
		listExtended = new ArrayList<PM_Sequence>();
		listAlbum = new ArrayList<PM_Sequence>();	
	 

		PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();
		XPath xpathSelector = null;
		List<Element>  result = null;

		// Wenn <alle-tocs> nicht vorhanden, dann anlegen
		xpathSelector = DocumentHelper.createXPath("//" + TAG_ALLE_TOCS);
		result = xpathSelector.selectNodes(document);
		if (result.size() == 0) {
			// not found
			Element rootElement = document.getRootElement();
			List<Element> l = rootElement.elements();
			Element neuesElement = new org.dom4j.tree.DefaultElement(TAG_ALLE_TOCS);
			l.add(neuesElement);
			return ;
		}

		// read max-a and max-bx (max album number and max base/extended - sequence number)
		 
		Element root = result.get(0);
		maxSequNumber = PM_XML_Utils.getAttributeInt(root, TAG_ALLE_ATTR_MAX_SEQUENCE_NUMBER);	
		maxSequAlbumNumber = PM_XML_Utils.getAttributeInt(root, TAG_ALLE_ATTR_MAX_ALBUM_SEQUENCE_NUMBER);
		// read all sequences elements
		// ( <toc type="einfach" name="b503" inhalt="hamburg"/> )
		xpathSelector = DocumentHelper.createXPath("//" + TAG_TOC);

		result = xpathSelector.selectNodes(document);
		for (Iterator<Element> it = result.iterator();  it.hasNext();) {

			Element toc =  it.next();

			String xmlType = PM_XML_Utils.getAttribute(toc, TOC_ATTR_TYPE);
			String sequName = PM_XML_Utils.getAttribute(toc, TOC_ATTR_NAME);			 
			String sgName = PM_XML_Utils.getAttribute(toc, TOC_ATTR_SG_NAME);
			String von = PM_XML_Utils.getAttribute(toc, TOC_ATTR_VON);
			String bis = PM_XML_Utils.getAttribute(toc, TOC_ATTR_BIS);
			String inhalt = PM_XML_Utils.getAttribute(toc, TOC_ATTR_INHALT);
			String index = PM_XML_Utils.getAttribute(toc, TOC_ATTR_INDEX);
			String qual = PM_XML_Utils.getAttribute(toc, TOC_ATTR_QS);
			String ort = PM_XML_Utils.getAttribute(toc, TOC_ATTR_ORT);
 
			
 
			// Anzahl Bilder holen
			int numberPictures = 0;
			if (metadatenContainer.getInitSequences().containsKey(sequName)) {
				numberPictures = metadatenContainer.getInitSequences().get(sequName);
			}
			
			
			if (sequName.startsWith(SEQ_CHARACTER_BASE)) {
				// ----------------------------------------------------------------------------
				//  Base:
				//       <toc type="einfach" name="b206" inhalt="1998.venedig" />
				// -----------------------------------------------------------------------------
				PM_SequenceBase sequence;
				if (sequName.equals(SEQ_CHARACTER_BASE)) {
					sequence = new PM_SequenceBase(inhalt,sequName);
					sequence.setStringLeaf(true);
					listBase.add(sequence);
					continue;
				}
				if (! inInitDicAllSequences(sequName)) {
					continue;
				}
				sequence = new PM_SequenceBase(inhalt,sequName);
				sequence.setAnzahlBilder(numberPictures);
				listBase.add(sequence);
				continue;
				
			} else if (sequName.startsWith(SEQ_CHARACTER_EXTENDED)) {
				// ----------------------------------------------------------------------------
				//  Extended:
				//       <toc type="einfach" name="s169" inhalt="s.haus-vom-see" />
				// -----------------------------------------------------------------------------
				PM_SequenceExtended sequence;
				if (sequName.equals(SEQ_CHARACTER_EXTENDED)) {
					sequence = new PM_SequenceExtended(inhalt,sequName);
					sequence.setStringLeaf(true);
					listExtended.add(sequence);
					continue;
				}
				if (! inInitDicAllSequences(sequName)) {
					continue;
				}
				sequence = new PM_SequenceExtended(inhalt,sequName);
				sequence.setAnzahlBilder(numberPictures);
				listExtended.add(sequence);
				continue;
			} else if (sequName.startsWith(SEQ_CHARACTER_NEW)) {
				// ----------------------------------------------------------------------------
				//  New:
				//       <toc type="neu" name="n475" inhalt="1249670506839"/>
				// -----------------------------------------------------------------------------
				if (! inInitDicAllSequences(sequName)) {
					continue;
				}
				PM_SequenceNew sequence = new PM_SequenceNew(inhalt,sequName);
				sequence.setAnzahlBilder(numberPictures);
				listNew.add(sequence);
				continue;				
			} else if (xmlType.equals("komplex")) {
				// ----------------------------------------------------------------------------
				//  Album: 
				//    <toc type="komplex" name="v481" von="1998.03.01" bis="2000.02.01" 
				//         inhalt="test.ich.harz" index="  kal2008  kal2009kuechealle" 
				//         qs="13" ort="  bad_sooden towcqnq" sg-name="b17" markieren="n"/>
				// -----------------------------------------------------------------------------
				PM_SequenceAlbum sequence;
				if (sequName.equals(SEQ_CHARACTER_ALBUM)) {
					sequence = new PM_SequenceAlbum(inhalt, sequName);
					sequence.setStringLeaf(true);
					listAlbum.add(sequence);
					continue;
				}
			    sequence = new PM_SequenceAlbum(inhalt, sequName);
				sequence.setAnzahlBilder(numberPictures);
				sequence.setVon(von);
				sequence.setBis(bis);
				sequence.setIndex(index);
				sequence.setQual(qual);
				sequence.setOrt(ort);
				sequence.setSeqClosedName(sgName);
				listAlbum.add(sequence);
				continue;				
			}  	 
		} // for
		

		 
		// -----------------------------------------------------------
		// Version auf 1
		// -----------------------------------------------------------
		setVersionWrite(1);
		
	}

	// ===========================================================================
	// inInitDicAllSequences()
	//
	//	 der Name (b11, n34, ....) muss in den Metadaten vorhanden sein
	// ===========================================================================
	private boolean inInitDicAllSequences(String sequName) {	
		PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();
		if (!metadatenContainer.getInitSequences().containsKey(sequName)) {
			return false; // nicht in den Metadaten
		}
		metadatenContainer.getInitSequences().remove(sequName);
		return true;
	}
	 

	private void updateDocument(List<PM_Sequence> sequenzen) {
 
		Element root = new org.dom4j.tree.DefaultElement(TAG_ALLE_TOCS);
		updateAttribute(root, TAG_ALLE_ATTR_MAX_SEQUENCE_NUMBER, String.valueOf(maxSequNumber));
		updateAttribute(root, TAG_ALLE_ATTR_MAX_ALBUM_SEQUENCE_NUMBER, String.valueOf(maxSequAlbumNumber));
		document.getRootElement().add(root);
		
		for (PM_Sequence sequenz: sequenzen) {
//System.out.println("        sequ = " + sequenz);
			Element e = new org.dom4j.tree.DefaultElement(TAG_TOC);

			String type =  typeToXmlType(sequenz.getType());
			String sequName = "";						
			String sgName = "";
			String von = "";
			String bis = "";
			String  inhalt = sequenz.getPath();
			String index = "";
			String qual = "";
			String ort = "";
			if (sequenz.getStringLeaf()) {
				sequName = sequenz.getSequenceCharacter();
			} else {
				sequName = sequenz.getShortName();
				if (sequenz instanceof PM_SequenceAlbum) {
					PM_SequenceAlbum album = (PM_SequenceAlbum) sequenz;
					von = album.getVon();
					bis = album.getBis();
					index = album.getIndex();
					qual = album.getQual();
					ort = album.getOrt();
					PM_Sequence seq = album.getSeqClosed();
					if (seq != null) {
						sgName = seq.getShortName();
					}
				}
			}

			updateAttribute(e, TOC_ATTR_TYPE, type);
			updateAttribute(e, TOC_ATTR_NAME, sequName);
			updateAttribute(e, TOC_ATTR_VON, von);
			updateAttribute(e, TOC_ATTR_BIS, bis);
			updateAttribute(e, TOC_ATTR_INHALT, inhalt);
			updateAttribute(e, TOC_ATTR_INDEX, index);
			updateAttribute(e, TOC_ATTR_QS, qual);
			updateAttribute(e, TOC_ATTR_ORT, ort);
			updateAttribute(e, TOC_ATTR_SG_NAME, sgName);
 
			root.add(e);
		}

	}

	// ===================================================================
	// xmlTypeToType()
	//
	// xml-type (so wie in der xml-Datei steht) --> Type der Sequenz-Instanz
	// =====================================================================
	// Inhalte der Attribute in pm_metadaten_global.xml
	private static final String XML_TYPE_GESCHLOSSEN = "einfach"; // 'normal' und Basis																// Sequenzen
	private static final String XML_TYPE_OFFEN = "komplex"; // virtuell															// Sequenzen
	private static final String XML_TYPE_NEU = "neu"; // neue Bilder

	 

	private String typeToXmlType(SequenceType type) {
		if (type == SequenceType.EXTENDED || type == SequenceType.BASE )
			return XML_TYPE_GESCHLOSSEN;
		if (type == SequenceType.ALBUM  )
			return XML_TYPE_OFFEN;
		if (type == SequenceType.NEW  )
			return XML_TYPE_NEU;
		return XML_TYPE_OFFEN;
	}	
	
	
	
 
	
	protected int getSequendeNumber(String name)  {	
		if (name.length() == 0) return 0;
		 return PM_Utils.stringToInt(name.substring(1));
	}
	
}