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

 

//import java.awt.*;
import java.io.*;
//import java.util.*;

import org.dom4j.*;
import org.dom4j.io.*;
//import org.dom4j.tree.DefaultDocument;

public class PM_XML_Utils  implements PM_Interface {

 

  // ====================================================================
  // getDocumentFromXMLfile()
  // 
  // Eine XML-Datei wird gelesen und als Document zurueckgegeben
  //
  // mode:
  //   OPEN_READ_ONLY  
  //   OPEN_WRITE    
  //   OPEN_CREATE     (create leeres Document und schreiben file, wenn nicht vorhanden)
  // ====================================================================   
  static public Document getDocumentFromXMLfile(File xmlFile, String rootTagName, int mode) {

    
    boolean write = false;
    boolean create =  false;
//    if ((mode & ~OPEN_READ_ONLY) == 0) readOnly = true;  
    if ((mode & ~OPEN_WRITE) == 0) write = true;  
    if ((mode & ~OPEN_CREATE) == 0) create = true;  
    Document document = null;

    // --------------------------------------------------------------
    // OPEN_CREATE: Wenn Datei nigefu, dann jetzt anlegen
    // --------------------------------------------------------------
    if (create && xmlFile != null && ( !xmlFile.exists() || xmlFile.length() == 0)) {   
      if ( !xmlFile.isDirectory() ) xmlFile.getParentFile().mkdirs(); 
      try {
        xmlFile.createNewFile();
      } catch (IOException e) {
        PM_Utils.writeErrorExit("Fehler beim Anlegen der Datei " + xmlFile.getPath() + ". " + e);
      }
      // leeres Document anlegen
      document = DocumentHelper.createDocument(); 
      document.addElement(rootTagName);
      writeOutDocument(document, xmlFile); 
      
      return document;
    } 
    
    // --------------------------------------------------------------    
    // Datei muss vorhanden und lesbar sein.
    // Wenn OPEN_WRITE muss sie auch schreibbar sein.
    // --------------------------------------------------------------               
    // pruefen ob file vorhanden
    if ( !xmlFile.isFile() ) PM_Utils.writeErrorExit(xmlFile.getPath() + "  keine Datei");    
    // prufen ob Datei can read (sie muss IMMER lesbar sein)
    if ( !xmlFile.canRead() ) PM_Utils.writeErrorExit(xmlFile.getPath() + "  nicht lesbar");  
    // prufen ob Datei can write
    if (write && !xmlFile.canWrite()) PM_Utils.writeErrorExit(xmlFile.getPath() + "  nicht schreibbar");  
   
    // --------------------------------------------------------------   
    // jetzt eroeffnen und document erzeugen
    // --------------------------------------------------------------   
    SAXReader xmlReader = new SAXReader();
    try {
      document = xmlReader.read(xmlFile);
    }
    catch (DocumentException de) {
      String fehlerText = "ERROR: Datei >>" + xmlFile.getPath() + "<< nigefu oder ungueltig" + de;
    	PM_Utils.writeErrorExit(fehlerText);    	    	 
    }
    
    return document;
  }
  
  
  
  
  // ====================================================================
  // getAttribute
  // 
  // ==================================================================== 
  static public String getAttribute(Element element, String name){
    Attribute attribute = element.attribute(name);
    if (attribute == null) { 
    	return "";
    };
    return attribute.getValue();
  }
   
  // ====================================================================
  // getAttributeInt
  // 
  // ====================================================================  
  static public int getAttributeInt(Element element, String name){
    Attribute attribute = element.attribute(name);
    int ret = 0;
    if (attribute == null) { return ret;};
    try {
    	ret =Integer.parseInt(attribute.getValue());
    }
    catch (NumberFormatException e) { return 0;}   
    return ret;   
  }   

  static public boolean getAttributeBoolean(Element element, String name){
	    Attribute attribute = element.attribute(name);
	    if (attribute == null) { 
	    	return false;
	    };
	    String value = attribute.getValue();
	    if (value == null || value.length() == 0) {
	    	return false;
	    }
	    if (value.equalsIgnoreCase("yes")) {
	    	return true;
	    }
	    return false;
	  }
  
  // ====================================================================
  // getAttributeDouble
  // 
  // ====================================================================  
  static public double getAttributeDouble(Element element, String name){
    Attribute attribute = element.attribute(name);
    double ret = 0;
    if (attribute == null) { return ret;};
    try {
    	ret =Double.parseDouble(attribute.getValue());
    }
    catch (NumberFormatException e) { return 0;}   
    return ret;   
  }   


  // ====================================================================
  // getElementListe()
  // 
  // ====================================================================  
  static public java.util.List getElementListe(Node element, String xpathSelectorString){  
    XPath xpathSelector = null;
    try {
      xpathSelector = DocumentHelper.createXPath(xpathSelectorString);  
    } 
    catch (InvalidXPathException e) {
 //     System.out.println(" InvalidXPathException vom >" + xpathSelectorString + "<  " + e);
      return new java.util.ArrayList(); 
    } 
    
    
      
    return xpathSelector.selectNodes(element);    
  }
  
  
  
  // =========================================================================
  // writeOutDocument()
  // 
  // Schreiben XML File von einem DOM. Das Document wird NICHT zerstoert!!!
  // =========================================================================    

  static public void writeOutDocument(Document outDoc, File outFile){
    
    

     // ------------------------------------------------------------
     //  Ins XML-Format wandeln (noch nicht ins File schreiben)
     // ------------------------------------------------------------
        XMLWriter writer = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
          OutputFormat  outputFormat = OutputFormat.createPrettyPrint();
          outputFormat.setEncoding("ISO-8859-1");
          writer = new XMLWriter(out, outputFormat);             
        }       
        catch (UnsupportedEncodingException ee) {
       	 System.out.println("ERROR: write: UnsupportedEncodingException" + ee); 
        } 
        try {
          writer.write(outDoc); 
        }
        catch (IOException e) {    
        	System.out.println("ERROR: write: IOException" + e);
        	} 

     // ------------------------------------------------------------
     //  Jetzt auf die Platte ins File schreiben
     // ------------------------------------------------------------
        
        
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(outFile); 

    }
    catch (IOException e) {
       System.err.println("ERROR: Open output File >" + outFile.getPath() + "< gescheitert");  
    }   
      
    try {
    
      fileWriter.write(out.toString()); 
       fileWriter.close();
    }  
    catch (IOException e) {
       System.err.println("ERROR: Fehler beim Schreiben in Datei " + e); 
    } 

  
    
    return;
  }   
 



  
}
