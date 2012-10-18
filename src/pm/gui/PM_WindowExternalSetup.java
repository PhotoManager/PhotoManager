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
 
package pm.gui;

 
import pm.utilities.*;
import pm.inout.*;
 

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
//import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*; 
//import java.awt.image.*;
import javax.swing.border.*;

/** Externe Programme PM bekannt machen.
 *
 *
 *  Es werden 2 Fenster (über Reiter erreichbar) dargestellt:
 *
 *  Fenster 1: Starten externes Programm
 *  Fenster 2: Externes Programm einrichten  (DIESE KLASSE HIER)
 */
@SuppressWarnings("serial")
public class PM_WindowExternalSetup extends PM_WindowExtern {
   
    private JList liste = null; 
    private JTextField pfadListe  =null;
    
    private JTextField pfadName = null;
    private JTextField progName = null;
    private int lastSelected = -1;   // in der Liste 
    
    private Border border;
    
    private Vector<PM_Listener> listenerExterneProgramme = new Vector<PM_Listener>();
    
    // ===========================================================
    // Konstruktor  
    // ===========================================================
    public PM_WindowExternalSetup() {
       border = BorderFactory.createLineBorder(Color.GRAY, 3);    
       // Alle Panels
       JPanel p = new JPanel();
       p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));  
       p.setAlignmentY(0);
       p.add(Box.createVerticalStrut(20));
       p.add(getPanelEinrichtenOben());
       p.add(Box.createVerticalStrut(30));
       p.add(getPanelEinrichtenUnten());      
       setLayout(new BorderLayout());  
       add(p, BorderLayout.NORTH);
       
       
 //      String progName = PM_XML_Session.getInstance().getExternesProgramm();      
 //      PM_ExternesProgramm extProg = PM_XML_MetadatenGlobal.getInstance().getExternesProgramm(progName);
       setzenProgrammNamen(null);  //  extProg);
		// --------------------------------------------------------
		// Change Listener für message
		// --------------------------------------------------------
		PM_Listener msgListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {				 
				setMsg();
			}			
		};		
		PM_MSG.addChangeListener(msgListener);		
		// jetzt werden erstmalig die Tab Titels gesetzt
		setMsg();
	}
	
	
	private void setMsg() {
		
		labelShortName.setText(PM_MSG.getMsg("modExtShortName"));	
		labelProgramPath.setText(PM_MSG.getMsg("modExtProgramPath"));	
		buttonUebernehmen.setText(PM_MSG.getMsg("apply"));
		
		buttonTestenOben.setText(PM_MSG.getMsg("modExtButtonTest"));
		titleSetup.setTitle(PM_MSG.getMsg("modExtTitleSetup"));
		titleAllPrograms.setTitle(PM_MSG.getMsg("modExtTitleAllProgs"));
		
		labelAllNames.setText(PM_MSG.getMsg("modExtListNames"));
		buttonLoeschen.setText(PM_MSG.getMsg("modExtButtonDelete"));
	}
	 
	
  // =====================================================
  // addChangeListenerExterneProgramme()
  //
  // =====================================================
  public void  addChangeListenerExterneProgramme(PM_Listener listener)  {
    if ( !listenerExterneProgramme.contains(listener))  listenerExterneProgramme.add(listener);
  }
    

    
   // ========================================================
   // getPanelEinrichtenOben()
   // ========================================================    
  private JLabel labelProgramPath;
  private JLabel labelShortName;
  private JButton buttonUebernehmen;
  private TitledBorder titleSetup;
  private JButton buttonTestenOben;
  
   public JPanel  getPanelEinrichtenOben() {
     JPanel panel = new JPanel();           
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  
     panel.setAlignmentY(0);
     
     // ---------------------------------------------------
     // Oben: Ein neues Programm ermitteln.
     // ---------------------------------------------------
     // Zeile "Pfad mit Button aufrufen FileChooser"
     JPanel pfadPanel = new JPanel();
     pfadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));  
     labelProgramPath = new JLabel(PM_MSG.getMsg("modExtProgramPath"));
     pfadPanel.add(labelProgramPath);
     pfadName = new JTextField(PM_MSG.getMsg("modExtProgramPath"));  
     pfadName.setColumns(30);
     pfadPanel.add(pfadName);
     JButton browserButton = new JButton("..."); 
     ActionListener alButtonBrowser = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          pfadName.setText(doFileChooser(pfadName.getText()));
       }
     };
     browserButton.addActionListener(alButtonBrowser); 
     pfadPanel.add(browserButton);
      
     // Zeile mit Programm Name
     JPanel namePanel = new JPanel();
     namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));   
     labelShortName = new JLabel("Programm-Kurzname:");
     namePanel.add(labelShortName);
     progName = new JTextField("Kurzname");  
     progName.setColumns(10);
     namePanel.add(progName);
      
     // Zeile mit Buttons "übernehmen" und "abbrechen"
     JPanel buttonPanel = new JPanel();
     buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));   
     buttonUebernehmen = new JButton("übernehmen");
     buttonPanel.add(buttonUebernehmen);
     ActionListener alUebernehmen = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          doUebernehmenNeuesProgramm();
       }
     };
     buttonUebernehmen.addActionListener(alUebernehmen); 
     buttonTestenOben = new JButton("Testen");
     buttonPanel.add(buttonTestenOben);
     ActionListener alTestenOben = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          if (waitForProcessEnde()) return;
          startenProgramm(pfadName.getText(), "");          
       }
     };
     buttonTestenOben.addActionListener(alTestenOben); 
     
      
      
     // panel zusammensetzen
     panel.add(pfadPanel);
     panel.add(namePanel);
     panel.add(buttonPanel);    
      
      
     
     JPanel p = new JPanel(new BorderLayout()); 
     p.add(panel, BorderLayout.NORTH);
 
     titleSetup = BorderFactory.createTitledBorder(border, 
                                                "Ein neues Programm aufnehmen",
                                                TitledBorder.TOP,
                                                TitledBorder.CENTER);             
      p.setBorder(titleSetup); 
      
      return p;
   }   
 
          
    
    
   // ========================================================
   // getPanelEinrichtenUnten()
   // ========================================================   
   private TitledBorder titleAllPrograms;
   private JLabel labelAllNames;
   private JButton buttonLoeschen;
   public JPanel  getPanelEinrichtenUnten() {
     JPanel panel = new JPanel();           
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  
     panel.setAlignmentY(0);
     
      
     
     // ---------------------------------------------------
     // Unten: Alle bereits vorhandenen Einträge
     // ---------------------------------------------------
     
      
     // Liste der Kurz-Namen
     JPanel listeHeaderPanel = new JPanel();
     listeHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     labelAllNames = new JLabel("Liste alle Kurznamen:");
     listeHeaderPanel.add(labelAllNames);     
     JPanel listePanel = new JPanel();
     listePanel.setLayout(new FlowLayout(FlowLayout.LEFT));     
     liste = new JList();      
     liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     liste.setPreferredSize(new Dimension(100, 0)); 
    liste.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        Object o = liste.getSelectedValue();
        if (o == null) return;
        // valueChanged wird bei jedem Klicken 2 mal aufgerufen !!!
        if (lastSelected == -1) {
          PM_ExternalProgram extProg = (PM_ExternalProgram)liste.getSelectedValue();
          pfadListe.setText(extProg.getPath());
          lastSelected = liste.getSelectedIndex();
          fireListenerExterneProgramme(extProg);
        } else lastSelected = -1;
      }
    }); 
    JScrollPane indexListeScrollPane = new JScrollPane(liste);
     listePanel.add(indexListeScrollPane);
   
     // Vollständiger Pfad eines Eintrages (unter der Liste)
     JPanel pfadListePanel = new JPanel();
     pfadListePanel.setLayout(new FlowLayout(FlowLayout.LEFT));        
     pfadListe  = new JTextField();  
     pfadListe.setColumns(30);
     pfadListePanel.add(pfadListe);
     JButton browserListeButton = new JButton("..."); 
     ActionListener alButtonListeBrowser = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          pfadListe.setText(doFileChooser(pfadListe.getText()));
          PM_ExternalProgram extProg = (PM_ExternalProgram)liste.getSelectedValue();
          extProg.setPath(pfadListe.getText());
 //         PM_XML_File_MetadatenGlobal.getInstance().setInitGeaendert(true);
          fireListenerExterneProgramme(extProg);
       }
     };
     browserListeButton.addActionListener(alButtonListeBrowser); 
     pfadListePanel.add(browserListeButton);
     
     // Button "löschen" und "Testen"
     JPanel loschenPanel = new JPanel();
     loschenPanel.setLayout(new FlowLayout(FlowLayout.LEFT));  
     buttonLoeschen = new JButton("Eintrag löschen");
     loschenPanel.add(buttonLoeschen);
     ActionListener alLoeschen = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          if (waitForProcessEnde()) return;
          doLoeschen();          
       }
     };
     buttonLoeschen.addActionListener(alLoeschen); 
     JButton buttonTestenUnten = new JButton("Testen");
     loschenPanel.add(buttonTestenUnten);
     ActionListener alTestenUnten = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          if (waitForProcessEnde()) return;
          startenProgramm(pfadListe.getText(), "");       
       }
     };
     buttonTestenUnten.addActionListener(alTestenUnten); 
 
      
     // panel zusammensetzen
     panel.add(listeHeaderPanel);
     panel.add(listePanel);
     panel.add(pfadListePanel);
     panel.add(loschenPanel);
     
     JPanel p = new JPanel(new BorderLayout()); 
     p.add(panel, BorderLayout.NORTH);
    
     titleAllPrograms = BorderFactory.createTitledBorder(border, 
                                                "Alle Bearbeitungsprogramme",
                                                TitledBorder.TOP,
                                                TitledBorder.CENTER);
      p.setBorder(titleAllPrograms); 
      
      return p;
   }   
 
   
   
   
   
  // ========================================================
  // setzenProgrammNamen()
  //
  // Setzen alle Programmnamen in der ComboBox und
  // selectieren.
  // ========================================================      
  private void  setzenProgrammNamen(PM_ExternalProgram extProg) { 
     
     // Alle Programmnamen
     Vector progNamen = PM_All_ExternalPrograms.getInstance().getAlleExternenProgramme(); 
     
     liste.setListData(progNamen); 
     if (progNamen.size() == 0)  return; // leere List. Es kann auch nichts selektiert werden
     
     // Eintrag selektieren
     if (extProg == null) {
         // ersten Eintrag selektieren
         liste.setSelectedIndex(0);
         return;
     }
 
     for (int i=0; i<progNamen.size(); i++) {
         if (progNamen.elementAt(i).equals(extProg)) {
            liste.setSelectedIndex(i);
            return; 
         }
     }
             
     
     
  }   
  
  
  // =====================================================
  // fireListenerExterneProgramme()
  //
  // =====================================================
  private  void  fireListenerExterneProgramme(PM_ExternalProgram extProg)  {
    for (int i=0; i<listenerExterneProgramme.size(); i++) {
      PM_Listener listener = (PM_Listener)listenerExterneProgramme.elementAt(i);
      listener.actionPerformed(new PM_Action(extProg));
    } 
  }
  
   // ========================================================
   // doUebernehmenNeuesProgramm()
   // ========================================================     
   private void  doUebernehmenNeuesProgramm() {
       
    
       
	   PM_All_ExternalPrograms metadatenGlobal = PM_All_ExternalPrograms.getInstance();  
     // prüfen, ob gültiger Pfad
     String pfad =  pfadName.getText().trim();
     if (false) {
    	 String msg = PM_MSG.getMsg("modExtMsgNoValid");
         JOptionPane.showConfirmDialog(
                        this, 
                       msg,
                        //"Programmname fehlt oder bereits vergeben",
                        "Error", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.ERROR_MESSAGE);
         return;         
     }
     
     // prüfen Programmname
     String name = progName.getText().trim();
     PM_ExternalProgram extProg = metadatenGlobal.getExternesProgramm(name);
     if (name.length() == 0 || extProg != null) {
    	 String msg = PM_MSG.getMsg("modExtMsgNoValid");
         JOptionPane.showConfirmDialog(
                        this, 
                        msg, //"Programmname fehlt oder bereits vergeben",
                        "Name falsch", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.ERROR_MESSAGE);
         return;         
     }
     
     // Prüfung O.K.
     // "PM_ExternesProgramm" - Instanz anlegen
     extProg = new PM_ExternalProgram(name, pfad, false);
     metadatenGlobal.addExternalProgram(extProg);
      
     // Liste updaten
     setzenProgrammNamen(extProg);
     fireListenerExterneProgramme(extProg);
   }
   
   // ========================================================
   // doLoeschen()
   //
   // File Chooser um externes Programm zu suchen
   // ========================================================     
   private void  doLoeschen() {  
        int n = JOptionPane.showConfirmDialog(
                        this, 
                        PM_MSG.getMsg("modExtMsgDeleteLine"),//"Eintrag löschen ?",
                        " ", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
        if (n==JOptionPane.NO_OPTION) { return; };        
        PM_ExternalProgram extProg = (PM_ExternalProgram)liste.getSelectedValue();          
        PM_All_ExternalPrograms.getInstance().removeExternalProgram(extProg);
        // Liste updaten
        setzenProgrammNamen(null);
        fireListenerExterneProgramme(null);
   }
           
   // ========================================================
   // doFileChooser()
   //
   // File Chooser um externes Programm zu suchen
   // ========================================================     
   private String  doFileChooser(String filePath) {
     JFileChooser fileChooser = new  JFileChooser();
     fileChooser. setSelectedFile(new File(filePath));
     int result = fileChooser.showOpenDialog(null);
     if (result == JFileChooser.APPROVE_OPTION) {
         File f = fileChooser.getSelectedFile();    
         return f.getPath();      
     }
     return "";
   }    
   
  // ========================================================
  // processEnde()
  // 
  // ========================================================    
   protected void  processEnde() {  
     // wird ueberschrieben
   }   
}
