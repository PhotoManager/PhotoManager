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



import java.io.File;

 
// 
import pm.gui.*;
import pm.picture.*;
import pm.utilities.*;
 

//import java.awt.*;
//import java.awt.event.*;
//import java.awt.image.*;



//import javax.swing.event.*;
import javax.swing.*;

//import java.io.*;
//import java.util.*;

//import java.text.*;

// ******************************************************************************

// ******************************************************************************
public class PM_ExportUnchanged extends PM_Export implements PM_Interface {  

 
  
  // =====================================================
  // Konstruktor 
  // =====================================================
  public  PM_ExportUnchanged(PM_WindowMain windowMain, PM_WindowExport windowExport) {
   super(windowMain, windowExport);
    
  }


  // ==============================================================
  // ==============================================================
  // ==============================================================
  // ==============================================================
  // =================  PRIVATE/PROTECTED  ======================== 
  // ==============================================================
  // ==============================================================
  // ==============================================================
  
  
  
  // =====================================================
  // schreibenBild 
  //
  // Ein Bild wird kopiert
  // =====================================================
  @Override
  protected boolean writePicture(PM_Picture picture, File fileOut,int nr, int bilderGes) {    
    copyFile(picture.getFileOriginal(), fileOut);
 
    return true;
  }

 
      
  // =====================================================
  // getAddOnDialogPanel() 
  //
  // Wird in den "Export-Dialog" geschrieben.
  // =====================================================
  protected void getAddOnDialogPanel(JPanel panel) {
//    panel.add(new JLabel("Die Bilder werden unverändert exportiert")); 
    panel.add(new JLabel(PM_MSG.getMsg("expDialogNotChng"))); 
     
  }  
  
  
  
}  // Ende Klasse
