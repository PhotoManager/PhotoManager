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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pm.index.PM_Index;
import pm.picture.PM_MetadataContainer;
import pm.picture.PM_Picture;
import pm.search.PM_LuceneDocument;
import pm.search.PM_Search;
import pm.search.PM_SearchExpr;
import pm.utilities.PM_MSG;
import pm.utilities.PM_Utils;
import pm.utilities.PM_Interface.SearchSortType;
import pm.utilities.PM_Interface.SearchType;



public class PM_SubWindowSeqMini extends PM_SubWindowSequence {

	protected JButton miniNew;
	protected JButton miniMod;
	protected JButton miniDel;
	
	 
	 
	private JComponent gui;
	 
	
	/**
	 * constructor
	 */
	public PM_SubWindowSeqMini(PM_WindowMain windowMain, PM_WindowBase windowBase) {
		super(windowMain, windowBase);
		 
		gui = createGui();
		 
	}
	
	
	private JComponent createGui( ) {
		
		// add some buttons to the toolbar
		JPanel toolPanel = index.getIndexToolbar(); // the default toolbar	
		 	 
		miniNew = new JButton("New");
		miniNew.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				makeMinSequence();
			}
		});
		toolPanel.add(miniNew);
		miniMod = new JButton("Mod");
		miniMod.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		toolPanel.add(miniMod);
		miniDel = new JButton("Del");
		miniDel.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
	 System.out.println("Delete mini new");
			}
		});
		toolPanel.add(miniDel);
 						
		index.controller.setAllowGetFromRight(true);
		index.controller.setAllowCrossing(true);
		index.controller.setPopUpAendern(true);
		index.controller.setPopUpDiaShow(true);
		index.controller.setPopUpLoeschen(true);
		index.controller.setPopUpLoeschenAufheben(true);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(index.getIndexPanel(), BorderLayout.CENTER);
 	
		return p;				
	}
	
	
	
	/**
	 * make a new Mini Sequence
	 */
	private void makeMinSequence() {
		List<PM_Picture> l = index.controller.getPictureListDisplayed();
		if (l.isEmpty()) {
			noPictures();
			return;
		}
		int mSeq = 0;
		for (PM_Picture pic: l) {
			if (pic.meta.hasMiniSequence()) {
				mSeq++;
			}
		}
		if (mSeq > 0) {
			JOptionPane.showConfirmDialog(null, mSeq + " Bilder haben bereits miniSequ" ,
					"Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE);
			return;			
		}
			
		// get new mini sequence number
		Set<Integer> miniSequences =  PM_MetadataContainer.getInstance().getMiniSequences();
		int i;
		for (i=1;;i++) {
			if (!miniSequences.contains(i)) {		 
				break;
			} 
		}	 
		
		// neue Mini Sequence erstellen
		int ii = 0;
		for (PM_Picture pic: l) {
			ii++;
			String ms = "m" + String.valueOf(i) + "_" + String.valueOf(ii);
			pic.meta.setMiniSequence(ms);			 
		}
		
		
		
		System.out.println("the new seq numder: " + i);
		miniSequences.add(i);
		
		
	}
	
	/**
	 * Error message no pictures in the index View
	 */
	private void noPictures() {
		JOptionPane.showConfirmDialog(null, "Keine Bilder Dargestellt" ,
				"Error", JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE);
		
	}
	
	/**
	 * Add a compressed picture from the left.
	 */
	public boolean addCompressedPicture (PM_Picture picture) {
		List<PM_Picture> miniSequence = PM_Utils.getMiniSequence(picture);
		if (miniSequence.isEmpty()) {
			return false;
		}
		// Append the list
		index.data.appendPictureList(miniSequence);	
		return true;
	}
	
	
	
	/**
	 * Returns the Component for the TabbedPane
	 */
	@Override
	protected  JComponent getTabComponent() {
		return gui; // Overrides
	}
	 
	/**
	 * Set the hits into the JTextField.
	 */
	@Override
	protected void setHits() {	
		// not for mini sequence
	}
	
	/**
	 * Display the sequence name.
	 */
	@Override
	protected void setSequenceName() {
		// not for mini sequence
	} 
	
	@Override
	protected void setButtons() {
		
	}
	
	/**
	 * Tab changed.
	 */
	protected void tabChanged(JLabel upperLabel) {
		upperLabel.setText("Mini Sequence");
	}
	
	
}
