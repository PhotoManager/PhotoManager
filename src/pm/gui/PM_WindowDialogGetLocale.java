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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import pm.utilities.*;
 
 

public class PM_WindowDialogGetLocale {
	private JDialog dialog; 
	
	private Locale locale;
	 
	public PM_WindowDialogGetLocale(Locale locale) {
		this.locale = locale;
	}
	public PM_WindowDialogGetLocale( ) {
		 locale = new Locale("en");
	}
	
	public Locale getLocale() {
		return getLocale_();
	}
	
	private Locale getLocale_() {
		dialog =  getDialogGetLocale( );
		int w = 300;
		int h = 200;
		Dimension screen = PM_Utils.getScreenSize();
		int x = screen.width / 2 - w / 2;
		int y = screen.height / 2 - h / 2;
		// dialog.setSize(w, h);
		dialog.setLocation(x, y);

		dialog.pack();
		dialog.setVisible(true);
		
//		String tld = topLevelDirectory.getText();
//		if (tld.length() == 0) {
//			System.exit(0);
//		}
		
//		File f = new File(tld);	
		return locale;
	}
	
	
	
	 	 
	private JLabel headerLabel;
	private JTextArea whatText;
	private JButton continueButton;
	 
	private JTextArea infoText;
	
	private JDialog getDialogGetLocale( ) {
		
		int dimX = 500;
		// --------------------------------------
		// überschrift
		// -------------------------------------
		JPanel headerPanel = new JPanel();
		headerPanel.setPreferredSize(new Dimension(dimX, 40));
	    headerLabel = new JLabel(PM_MSG.getMsg("PhotoManager"));
		Font font = headerLabel.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, 20);
		headerLabel.setFont(fontBold);	
		headerLabel.setPreferredSize(new Dimension(dimX, 40));
		headerPanel.add(headerLabel, BorderLayout.CENTER);
		
		// -------------------------------------
		// whatPanel
		// ------------------------------------
		JPanel whatPanel = new JPanel();
		whatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));	
	    whatText = new JTextArea(PM_MSG.getMsg("winDiaLocaleHeader"));	 
	    whatText.setPreferredSize(new Dimension(dimX, 30));
	    font = whatText.getFont();
		fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		whatText.setFont(fontBold);	
		whatText.setBackground(headerPanel.getBackground());
		whatPanel.add(whatText);
		
	 
		// ------------------------------------------
		// The languages
		// ------------------------------------------
		
		// *******************  Dies ist ein Hack !!!!!!!!!!!!!!!!! *********
		// TODO  make it better !!!!
		
		JRadioButton en = new JRadioButton("English");
		en.setActionCommand("en");
		JRadioButton de = new JRadioButton("German");
		de.setActionCommand("de");
		ButtonGroup group = new ButtonGroup();
		group.add(en);
		group.add(de);
		 
		RadioListener rl = new RadioListener();
		en.addActionListener(rl);
		de.addActionListener(rl);
		
		en.setSelected(true);
		if (locale.getLanguage().equals("de")) {
			de.setSelected(true);
		} 
		
		// ---------------------------------------
		// Hinweis
		// ---------------------------------------
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));	
 
	
		 
			 
	    infoText = new JTextArea(PM_MSG.getMsg("winDiaPrompLocale"));	
	    

	    font = infoText.getFont();
		 fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
	//    Font font = new Font("Verdana", Font.BOLD, 12);
		 infoText.setFont(fontBold);
	 	 infoText.setForeground(Color.BLUE);
	    
	    
		infoText.setPreferredSize(new Dimension(dimX, 95));
 		infoPanel.add(infoText);
		
		// --------------------------------------------
		// Button
		// -------------------------------------------
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		continueButton = new JButton(PM_MSG.getMsg("continue"));
		buttonPanel.add(continueButton);
		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				 
					dialog.dispose();		 
			}
		}); 
		 
		 
		
	 	
		
		// -----------------------------------
		// fertig, alles zusammen
		// -----------------------------------
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.add(headerPanel);
		dialogPanel.add(whatPanel);
		dialogPanel.add(en);
		dialogPanel.add(de);
 		dialogPanel.add(infoPanel);
 		dialogPanel.add(buttonPanel);
		
		JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);
//		dialog.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		 		
		return dialog;
	}
	
	// ============================================================
	// ============================================================
	// InnerClass: RadioListener
	// ============================================================
	// ============================================================
	/** Listens to the radio buttons. */
	class RadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			locale = new Locale(e.getActionCommand());
			PM_MSG.setResourceBundle(locale);
			infoText.setText(PM_MSG.getMsg("winDiaPrompLocale"));	 
			whatText.setText(PM_MSG.getMsg("winDiaLocaleHeader"));  
			continueButton.setText(PM_MSG.getMsg("continue"));
		}
	}
}
