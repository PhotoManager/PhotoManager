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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pm.dragndrop.PM_PictureTransferable;
import pm.gui.PM_WindowBase;
import pm.gui.PM_WindowMain;
import pm.gui.PM_WindowRightTabbedPane;
 
import pm.picture.PM_Picture;
import pm.utilities.PM_Utils;

@SuppressWarnings("serial")
public class PM_IndexViewRight extends PM_IndexView {

	 
	
	public PM_IndexViewRight(PM_Index index) {
		this.index = index; 
	}
	
	@Override 
	protected boolean isLeft() {
		return false;  
	}
	
	
	@Override 
	public boolean canDrop(PM_PictureTransferable localPictures) {
		PM_IndexView sourceIV = localPictures.getIndexView();
		if (sourceIV == this) {
			if (windowBase == windowMain.getWindowRechts().getWindowSequence()) {
				return true; 
			}
return true;  /////      TEST TEST    false;  
		}
		return true; // yes, we can copy from left to right  --->  
	}
	
 
	
	
	/**
	 * return the index toolbar for a right subwindow
	 *  
	 */
	@Override 
	protected  JScrollPane getToolbar(  ) {	 
		 
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBackground(Color.YELLOW);
		panel.setAlignmentY(0);	 
		
		// -----------------------------------------------------------------
		// <  (Button "von rechts nach links �berschreiben")
		// ----------------------------------------------------------------
		JButton buttonAlle = PM_Utils.getJButon(ICON_1_LEFT);  
		panel.add(buttonAlle);
		ActionListener alAlle = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PM_Index  ivFrom = PM_WindowMain.getInstance()
									.getIndexViewWindowRight();
				PM_Index  ivTo = PM_WindowMain.getInstance()
									.getIndexViewThumbnails();
				if (ivFrom != null) {
					if (ivFrom.controller.sizeDargestellt() == 0) {
						return;
					}
					List<PM_Picture> list =  ivFrom.controller.getPictureListDisplayed();	 
					ivTo.data.clearAndAdd(list);
					 			
				} 	 
			}
		};
		buttonAlle.addActionListener(alAlle);

		// ----------------------------------------------------------------------
		// <<  (Button "von rechts nach links  anh�ngen")
		// ----------------------------------------------------------------------
		JButton buttonAlleAppend = PM_Utils.getJButon(ICON_2_LEFT); 
		panel.add(buttonAlleAppend);
		ActionListener alAlleAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PM_Index  ivFrom = PM_WindowMain.getInstance()
						.getIndexViewWindowRight();
				PM_Index  ivTo = PM_WindowMain.getInstance()
						.getIndexViewThumbnails();
				if (ivFrom != null) {
					if (ivFrom.controller.sizeDargestellt() == 0) {
						return;
					}
					List<PM_Picture> list = ivFrom.controller.getPictureListDisplayed();
					ivTo.data.appendPictureList(list);
					 
				}
			}

		};
		buttonAlleAppend.addActionListener(alAlleAppend);

		// -------------------------------------------------------------
		// X (Button "alle loeschen")
		// ------------------------------------------------------------
		JButton buttonLoeschen = PM_Utils.getJButon(ICON_DELETE);
		panel.add(buttonLoeschen);
		ActionListener alLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PM_WindowRightTabbedPane wrt = PM_WindowMain.getInstance()
						.getWindowRechts();
//				PM_IndexView iv = wrt.getWindowSelected().getIndexView();
//				if (iv.getPictureSize() == 0) {
//					return; // rechts nichts vorhanden
//				}

				// jetzt gehts los
				wrt.getWindowSelected().removeAllPictures();
				// links neu zeichnen (da rechts ja nichts mehr ist)
	//			PM_WindowMain.getInstance().getIndexViewThumbnails()
	//					.controller.repaintViewport();

			}
		};
		buttonLoeschen.addActionListener(alLoeschen);

		// ----------------------------------------------------------------
		// Button "reread"
		// ---------------------------------------------------------------
		JButton buttonReRead = PM_Utils.getJButon(ICON_REREAD);
		// panelNorth.add(buttonReRead);
		panel.add(buttonReRead);
		ActionListener alReRead = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				index.controller.rereadAllThumbs();
			}
		};
		buttonReRead.addActionListener(alReRead);
		
		
		
		// make scrollpane
		JScrollPane sc = new JScrollPane(panel);
		sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		return sc;
	}
}
