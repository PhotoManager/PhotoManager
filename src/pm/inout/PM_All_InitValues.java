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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
 

import pm.utilities.PM_Configuration;
import pm.utilities.PM_Utils;

public class PM_All_InitValues {
	  
	  private final PM_Configuration einstellungen; 
	    
	  private static final PM_All_InitValues instance = new PM_All_InitValues(); 
	   
	  private Map<String, String> alleInitValues = new HashMap<String, String>();  // alle externen Programm-Instanzen

	  private final String TRENNUNG = "%%%";
	   
	  
	   private File fileInitValues;
	  
	  // =====================================================
	  // Class Method: getInstance()
	  // =====================================================
	  static public PM_All_InitValues getInstance() {        
	    return instance;
	  }

	  // =====================================================
	  // Konstruktor
	  // =====================================================
	  private  PM_All_InitValues() {
		  einstellungen =  PM_Configuration.getInstance();
	  }
	  
	  
	  // =====================================================
	  // init()
	  //
	  // =====================================================
	  public void  init() { 
		  fileInitValues = einstellungen.getFileHomeInitValues();
		   if ( !fileInitValues.isFile()) return;  // noch keine Datei vorhanden
		    
		   try {
			   BufferedReader in = new BufferedReader(new FileReader(fileInitValues));
			   while (true) {
				   String line = in.readLine();
				   if (line == null) break;
				   String[] a = line.split(TRENNUNG);
				   if (a.length != 2) continue;
				   String key = a[0];
				   String value = a[1];
				   alleInitValues.put(key, value);				  
			   }
			   in.close();
		   } catch (IOException e)  {
			   alleInitValues = new HashMap<String, String>();
			   return;   //  fehler beim Lesen (Open) File
		   }	
	 
		    
	  } 

	  // =====================================================
	  // close()
	  //
	  // Datei schreiben
	  // =====================================================
	  public void  close() {  
		  	   
		   try {
			   PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileInitValues)));
			   Set keys =alleInitValues.keySet();
			   for (Iterator it=keys.iterator(); it.hasNext();) {
				   String key = (String)it.next();
				   if (alleInitValues.containsKey(key)) {
					   String value = (String)alleInitValues.get(key);
					   out.println(key + TRENNUNG + value);
				   }
			   }			   	    
			   out.close();
		   } catch (IOException e)   {}			    	 	  		  
	  }
	  
	  // =====================================================
	  // putValueBoolean() 
	  // getValueBoolean()
	  //
	  // =====================================================  
	  public  void putValueBoolean(Object o, String key, boolean b )  { 
		  String k = o.getClass().getName() + "." + key;
		  alleInitValues.put(k, Boolean.toString(b));
	  } 
	  public  boolean getValueBoolean(Object o, String key )  { 
		  String k = o.getClass().getName() + "." + key;
		  if ( !alleInitValues.containsKey(k)) return false;
		  String v = alleInitValues.get(k);
		  if (v.equals("true")) {
			  return true;
		  }
		  return false;
	  }  	  	  	    
	  
	  // =====================================================
	  // putValueInt() 
	  // getValueInt()
	  //
	  // =====================================================  
	  public  void putValueInt(Object o, String key, int i )  { 
		  String k = o.getClass().getName() + "." + key;
		  alleInitValues.put(k, Integer.toString(i));
	  } 
	  public  int getValueInt(Object o, String key )  { 
		  String k = o.getClass().getName() + "." + key;
		  if ( !alleInitValues.containsKey(k)) return 0;
		  
	    return PM_Utils.stringToInt((String)alleInitValues.get(k));
	  }  	  	  	  
	 

}
