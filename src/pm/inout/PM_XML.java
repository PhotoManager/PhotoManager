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
 

import org.dom4j.*;
//import javax.xml.stream.*;
//import javax.xml.stream.events.XMLEvent;

import org.dom4j.tree.*;

import java.io.*;
 

/**
 * Basisklasse fuer XML-Routinen auf dom4j-Basis
 * 
 */
public class PM_XML implements PM_Interface  {

	protected Document document = null;
	protected String rootTagName = "";
	protected File xmlFile;

	private int versionRead = 0;
	private int versionWrite = 0;
 

	// =====================================================
	// openDocument()
	// =====================================================
	protected void openDocument(int mode) {

	 

		if (document != null) {
			return;
		}
		document = PM_XML_Utils.getDocumentFromXMLfile(xmlFile, rootTagName, mode);

		// Lesen Version-Nummer (im root-tag)
		if (document != null) {
			Element rootElement = document.getRootElement();
			versionRead = PM_XML_Utils.getAttributeInt(rootElement, "version");
			versionWrite = versionRead;		 
		}
	}
 
	// =====================================================
	// protected: closeDocument()
	//
	// Damit die Ressourcen freigegeben werden
	// =====================================================
	protected void closeDocument() {
		document = null;
	}
	 

 

	// =====================================================
	// protected: writeDocument() (ohne close)
	// =====================================================
	protected void writeDocument() {

		// Versionsnummer updaten
		Element rootElement = document.getRootElement();
		updateAttribute(rootElement, "version", Integer.toString(versionWrite));
	 
		
		// Schreiben Doc
		PM_XML_Utils.writeOutDocument(document, xmlFile);

		System.out.println("Schreiben XML-Datei nach: " + xmlFile.getPath());
	}
	
	// =====================================================
	// getVersion() / setVersion()
	//
	// Versionsnummer vom rootTag
	// =====================================================
	public int getVersionRead() {
		return versionRead;
	}

	public int getVersionWrite() {
		return versionWrite;
	}

	public void setVersionWrite(int versionWrite) {
		this.versionWrite = versionWrite;
	}


	// =====================================================
	// getAttribute()
	// =====================================================
	protected String getAttribute(Element element, String attribut) {
		if (element == null)
			return "";

		String retString = element.attributeValue(attribut); // null if not
		// found
		if (retString == null)
			return "";

		return retString;
	}

	protected int getAttributeInt(Element element, String attribut) {
		if (element == null)
			return 0;

		String str = getAttribute(element, attribut);
		if (str.length() == 0)
			return 0;

		int retInt = 0;
		try {
			retInt = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return 0;
		}

		return retInt;
	}

	// =====================================================
	// updateAttribute()
	//
	// Attribut vorhanden:
	//    Attribut-Wert wird uebrschrieben
	// Attribut nicht vorhanden:
	//    Attribut wird neu mit dem Wert angelegt
	// Uebergebener Wert null oder leer:
	//    wenn Attribut vorhanden wird es geloescht
	// =====================================================
	protected void updateAttribute(Element element, String attribut, String val) {
		String value = val;
		if (value != null && value.length() == 0)
			value = null;

		element.addAttribute(attribut, value);
	}

	// =====================================================
	// addTag()
	//
	// Tag vorhanden:
	// Text wird ueberschrieben
	// Tag nicht vorhanden:
	// Tag wird mit text uebrschrieben
	// Uebergebener Wert null oder leer:
	// wenn Tag vorhanden wird er geloescht
	// =====================================================
	protected void addTag(Element el, String tagName, String val) {
		String value = val;
		if (value != null && value.length() == 0)
			value = null;

		java.util.List<Element> elements = el.elements(tagName);
		Element element = null;
		if (elements.size() == 0) {
			// -----------------------------------------------------
			// tag nicht vorhanden
			// -----------------------------------------------------
			if (value == null)
				return; // nigefu, auch nicht neu anlegen
			// jetzt neu anlegen
			element = new DefaultElement(tagName);
			element.setText(value);
			elements.add(element);
			return;
		}

		// -----------------------------------------------------
		// tag vorhanden: Wert updaten oder tag loeschen
		// ----------------------------------------------------

		// wenn value == null, dann diesen Tag loeschen
		if (value == null) {
			for (int i = 0; i < elements.size(); i++) {
				elements.remove(i);
			}
			return;
		}
		element = (Element) elements.get(0);
		element.setText(value);
	}

	// =====================================================
	// getTagValue()
	//
	// Tag vorhanden:
	// return mit Wert
	// Tag nicht vorhanden:
	// return mit ""
	// =====================================================
	protected   String getTagValue(Element el, String tagName) {
		java.util.List elements = el.elements(tagName);
		Element element = null;
		if (elements.size() == 0)
			return "";
		element = (Element) elements.get(0);
		String retString = element.getText();
		if (retString == null)
			return "";
		return retString;

	}



} // Ende Klasse
