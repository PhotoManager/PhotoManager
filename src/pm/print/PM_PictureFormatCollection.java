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
package pm.print;

 
import pm.utilities.*;

import java.util.*;
//import java.awt.*;



// *************************************************************************
//

//
//                           
// ***************************************************************************
//                                                                    
//                                                                    
//                                                                    
public class PM_PictureFormatCollection  implements PM_Interface  {   


  // =================================================
  // Class Variable
  // =================================================
  private static  PM_PictureFormatCollection instance = null;
   
  private static Vector<PM_PictureFormat> bildFormatCollection  = new Vector<PM_PictureFormat>();                                                                
 
 

  // =====================================================
  // Class Method: getInstance()
  //
  // Es wird nur eine Instanz angelegt (Singleton)
  // =====================================================
  static public PM_PictureFormatCollection getInstance() {
    if (instance == null) instance = new PM_PictureFormatCollection();
    return instance;
  }


  // =====================================================
  // getBildFormate()
  //
  // Alle Bildformate fuer ein Format
  // =====================================================
  public PM_PictureFormat[] getBildFormate(PM_PaperFormat papierFormat) { 
     return getBF(papierFormat);
  }    

  // =====================================================
  // addBildFormat()
  //
  // =====================================================
  public void addBildFormat(PM_PictureFormat bildFormat) { 
     bildFormatCollection.add(bildFormat);
  }    


// *****************  private ************************************
// *****************  private ************************************
// *****************  private ************************************
// *****************  private ************************************




  // =====================================================
  // Konstruktor 
  // =====================================================
  private  PM_PictureFormatCollection() {       
     PM_PictureFormat f;
     PM_PaperFormat papierFormat = null;
     
    // fuer PAPER_FORMAT_11
    papierFormat = PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F1);
    f = new PM_PictureFormatMaxPaper(papierFormat, null, "max Papier");
    bildFormatCollection.add(f);
    f = new PM_PictureFormatMaxPicture(papierFormat, null, "max Bild");
    bildFormatCollection.add(f);
    
    // fuer PAPER_FORMAT_12
    papierFormat = PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F2);
    f = new PM_PictureFormatMaxPaper(papierFormat, null, "max Papier");
    bildFormatCollection.add(f);
    f = new PM_PictureFormatMaxPicture(papierFormat, null, "max Bild");
    bildFormatCollection.add(f); 
           
    // fuer PAPER_FORMAT_14
    papierFormat = PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F4);
    f = new PM_PictureFormatMaxPaper(papierFormat, null, "max Papier");
    bildFormatCollection.add(f);
    f = new PM_PictureFormatMaxPicture(papierFormat, null, "max Bild");
    bildFormatCollection.add(f);
    
     // fuer PAPER_FORMAT_16
    papierFormat = PM_PaperFormat.getPaperFormat(PAPER_FORMAT_F6);
    f = new PM_PictureFormatMaxPaper(papierFormat, null, "max Papier");
    bildFormatCollection.add(f);
    f = new PM_PictureFormatMaxPicture(papierFormat, null, "max Bild");
    bildFormatCollection.add(f);   
        
  }

  // =====================================================
  // getBF()
  //
  // Alle Bildformate fuer ein Format
  // =====================================================
  private PM_PictureFormat[] getBF(PM_PaperFormat papierFormat) {       
    Vector<PM_PictureFormat> v = new Vector<PM_PictureFormat>();
    for (int i=0; i<bildFormatCollection.size(); i++) {
      PM_PictureFormat f =  bildFormatCollection.elementAt(i);
//      System.out.println("das sind alle: get Bild Format. name = " + f);
      if (papierFormat == f.getPapierFormat()) {
        v.add(f);
// System.out.println("get Bild Format. name = " + f);
      }
    }
   
   return (PM_PictureFormat[]) v.toArray(new PM_PictureFormat[v.size()]);
       
  }  


}
