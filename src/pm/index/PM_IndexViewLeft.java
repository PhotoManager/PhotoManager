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


package pm.index;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pm.dragndrop.PM_DropTargetPictureList;
import pm.dragndrop.PM_PictureTransferable;
import pm.gui.*;
 
import pm.picture.PM_Picture;
import pm.utilities.PM_Action;
import pm.utilities.PM_Listener;
import pm.utilities.PM_Utils;


@SuppressWarnings("serial")
public class PM_IndexViewLeft extends PM_IndexView {

	public PM_IndexViewLeft(PM_Index index) {
		this.index = index;
		 
		PM_WindowRightTabbedPane.addChangeListener(new PM_Listener() {
			 
			public void actionPerformed(PM_Action e) {
				int tab = e.getType();
				if (tab == TAB_IMPORTIEREN ||
					tab == TAB_INFO_BILD || 
					tab == TAB_SEARCH ||
					tab == TAB_ZEIGEN_EINZEL) {
					// disable > and >> button
					rButton.setEnabled(false);
					rrButton.setEnabled(false);
				} else {
					// enable > and >> button
					rButton.setEnabled(true);
					rrButton.setEnabled(true);
				}
			}

		});
		
	
	}

	
	@Override 
	protected boolean isLeft() {
		return true;  
	}
	
	private JButton rButton;
	private JButton rrButton;
	
	
	@Override 
	public boolean canDrop(PM_PictureTransferable localPictures) {
		PM_IndexView sourceIV = localPictures.getIndexView();
		if (sourceIV == this) {
return true;  /////////////////////     TEST TEST TESfalse; // in left not move
		}
		return true; // yes, we can copy from right to left  <---- 
	}
	
 
	
	/**
	 * return the index toolbar for the left subwindow
	 *  
	 */
	@Override 
	protected JScrollPane getToolbar(  ) {
		 
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		panel.setBackground(Color.YELLOW);
		panel.setAlignmentY(0);	 
		 
		// Button slideshow
		JButton buttonDiashow = new JButton("?Diashow?");
		//   panelErsteZeile.setBackground(color);
		panel.add(buttonDiashow);
		ActionListener alDiashow = new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				List<PM_Picture> pictures = indexController.getPictureListDisplayed();
	 			if (!pictures.isEmpty()) {
	 				PM_Picture picSelected = indexController.getFirstPictureSelected();
	 				if (picSelected == null) {
	 					picSelected = pictures.get(0);
	 				}				 
	 				windowMain.doDiaShow(picSelected ,pictures, DIASHOW_NORMAL);
	 			}			
			}
		};
		buttonDiashow.addActionListener(alDiashow);
 
		// Clear Button  
		JButton clearButton = PM_Utils.getJButon(ICON_DELETE);
		panel.add(clearButton);
		ActionListener alClearButton = new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				windowBase.removeAllPictures();			
			}
		};
		clearButton.addActionListener(alClearButton);

		// Button "reread"
		JButton buttonReRead = PM_Utils.getJButon(ICON_REREAD);  
		panel.add(buttonReRead);
		ActionListener alReRead = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				index.controller.rereadAllThumbs();
			}
		};
		buttonReRead.addActionListener(alReRead);

		// Button to right >
		rButton = PM_Utils.getJButon(ICON_1_RIGHT);  
		panel.add(rButton);
		ActionListener alR = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PM_WindowBase window = windowMain.getWindowRightSelected();			 
				window.getAllThumbs(index);	
			}
		};
		rButton.addActionListener(alR);	
		 
		// Button append to right >>
		rrButton = PM_Utils.getJButon(ICON_2_RIGHT); // new JButton(">")
		panel.add(rrButton);
		ActionListener alRR = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PM_WindowBase window = windowMain.getWindowRightSelected();			 
				window.appendAllThumbs(index);		 								
			}
		};
		rrButton.addActionListener(alRR);	
	 		
		// make scroll pane
		JScrollPane sc = new JScrollPane(panel);
		sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		return sc;
	}
	
 
	
	
}
