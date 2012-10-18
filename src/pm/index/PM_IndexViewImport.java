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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
 
import javax.swing.*;
import javax.swing.tree.*;
 
import pm.dragndrop.PM_PictureTransferable;
import pm.gui.*;
import pm.inout.*;
import pm.picture.*;
import pm.picture.PM_Import.PictureMiniSequence;
import pm.search.*;
import pm.sequence.*;
import pm.utilities.*;


@SuppressWarnings("serial")
public class PM_IndexViewImport  extends PM_IndexView {
	 
	
	
	private BitSet picturesImported = new BitSet();
	private JProgressBar progressBar = new JProgressBar();
	private int barValue = 0;
	private boolean start = false;
	private JButton buttonAlle;
	private JButton buttonAlleAppend;
	private JButton buttonLoeschen;
	private JButton buttonCancel;
	private PM_WindowImport windowImport;
	private PM_Listener endListener;
	
	private ReadThumbFilesThread readThumbFilesThread;
	
	private Map<PM_Picture, PictureMiniSequence> miniSequenceNumbers;
	
	
	/**
	 * Constructor.
	 */
	public PM_IndexViewImport(PM_Index index, PM_WindowImport windowImport) {
		this.index = index;
		this.windowImport = windowImport;
		// create Listener for end of import
		endListener = new PM_Listener() {
			 
			public void actionPerformed(PM_Action e) {
				 stop();
			}
			
		};
	}

	private static Set<PM_Listener> importListeners = new HashSet<PM_Listener>();

	/**
	 * add an import listener 'import done'
	 */
	public static void addImportListener(PM_Listener listener) {
		importListeners.add(listener);
	}

	/**
	 * fire if the import is done.
	 */
	public void fireImportListener() {
		for (PM_Listener listener : importListeners) {
			listener.actionPerformed(new PM_Action(null));
		}
	}

	/**
	 * Now get the Thumbnail.
	 * 
	 * This method overrides for import. 
	 */
	@Override
	protected void checkForThumbnail(PM_Picture picture, int ind) {
		if (!picturesImported.get(ind)) {
			// The thumbnail file is available
			super.checkForThumbnail(picture, ind);
			return;
		} 
	}
	
	/**
	 * Set index of next to read.
	 * 
	 * This is the first picture in the index view.
	 */
	@Override
	public void setNextToRead(int nextToRead) {
		if (readThumbFilesThread != null) {
			readThumbFilesThread.setNextToRead(nextToRead);
		}	
	}
	
	 
 
	@Override
	public boolean canDrop(PM_PictureTransferable pictures) {
		return false;  
	}
	
	@Override
	public boolean canDrop(List<File> remotePictures) {
		return false;
	}
	
	/**
	 * Start a new import.
	 */
	protected void start(List<PM_Picture> pictures ,Map<PM_Picture, 
			PictureMiniSequence> miniSequenceNumbers) {
		 
		this.miniSequenceNumbers = miniSequenceNumbers;
		
		buttonAlle.setEnabled(false);
		buttonAlleAppend.setEnabled(false);
		buttonLoeschen.setEnabled(false);
		buttonCancel.setEnabled(true);
		windowImport.importTakePlace(true);
		
		picturesImported.clear();
		picturesImported.set(0, pictures.size()); // toIndex is exclusive
		
		progressBar.setStringPainted(true); // paint percent
		progressBar.setMaximum(pictures.size());
		barValue = 0;
		start = true;
		
		// create and start import thread
		readThumbFilesThread = new ReadThumbFilesThread(this, pictures, pictureViewTable, endListener);
		readThumbFilesThread.start();
		
	}

	/**
	 * Stop the import.
	 */
	protected void stop() {
		if (!start) {
			return;
		}
		start = false;
		
		
		List<PM_Picture> picList = new ArrayList<PM_Picture>(index.data.getPictureList());
		// pictures not imported
		List<PM_Picture> notImported = new ArrayList<PM_Picture>(picturesImported.cardinality());
		for (int i = picturesImported.nextSetBit(0); i >= 0; i = picturesImported.nextSetBit(i+1)) {
			notImported.add(index.data.getPicture(i));
		}
		picturesImported.clear();
		// Handle not imported pictures
		if (notImported.isEmpty()) {
			String txt =  "All pictures imported.";
			JOptionPane.showConfirmDialog(null, txt, "Help",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			// delete in idex.data
			picList.removeAll(notImported);
			index.data.clearAndAdd(picList);	
			// message
			String txt = notImported.size() + " pictures not imported.";
			JOptionPane.showConfirmDialog(null, txt, "Help",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.INFORMATION_MESSAGE);
		}
		
		progressBar.setStringPainted(false); // paint percent
		progressBar.setValue(0);
		
		// Make a sequence
		makeSequence(picList);
		makeAllMiniSequences(picList);
		
		buttonAlle.setEnabled(true);
		buttonAlleAppend.setEnabled(true);
		buttonLoeschen.setEnabled(true);
		buttonCancel.setEnabled(false);
		windowImport.importTakePlace(false);
		
		// Fire the import listener
		fireImportListener();
		
		readThumbFilesThread = null;
	}

	/**
	 * Check if pictures are importing.
	 * 
	 * @return true if pictures are importing.
	 * @return false if no import take place.
	 */
	@Override
	protected boolean isImport() {
		return start; 
	}
	
	/**
	 *  Make ONE new sequence for the just added pictures.
	 *  
	 *  Create a sequence type new.  
	 */
	private void makeSequence (List<PM_Picture>  newPictures ) {
		
		// create the new sequence
		String pfad = new Long(System.currentTimeMillis()).toString();	
//		Collections.sort(newPictures, PM_Utils.SORT_TIME_ORDER);	
		PM_Sequence neueSequenz = new PM_SequenceNew(pfad, newPictures);
		PM_SequencesInout.getInstance().setChanged(true);

		// Add in the tree in sub-window "Import"
		PM_TreeModelNew treeModel = PM_TreeModelNew.getInstance();
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
				neueSequenz);
		treeModel.insertNodeInto(newChild, treeModel.rootNode, 0);
		treeModel.nodeChanged(treeModel.getRootNode());
		// the following is a hack !!!
		PM_WindowImport wi = PM_WindowMain.getInstance().getWindowRechts().getWindowImport();
		wi.getTreeWindowNew().expandTree(-1);
		 	
		// save all
		PM_MetadataContainer.getInstance().flush();
		PM_DatabaseLucene.getInstance().flush();
		PM_SequencesInout.getInstance().flush();
	
	}
	
	/**
	 * Make mini sequences and one new sequence.
	 * 
	 */
	 
	private void makeAllMiniSequences(List<PM_Picture> newPictures) {
 		
		
		if (miniSequenceNumbers == null || miniSequenceNumbers.isEmpty()) {
			return;
		}
		 
		// look for new mini sequences and make them
		long time = 0;
		List<PictureMiniSequence> miniSeq = new ArrayList<PictureMiniSequence>();
		for (PM_Picture pic: newPictures) {
			if (!miniSequenceNumbers.containsKey(pic)) {	 
				continue;
			}
			PictureMiniSequence pms = miniSequenceNumbers.get(pic);
			long currentTime = pic.meta.getDateCurrent().getTime();
			if (time == 0) {
				time = currentTime;
				miniSeq.add(pms);
				continue;
			}			
			if (currentTime - time >= 2000) {
				// now we found a new mini-sequence
				makeOneMiniSequence(miniSeq);
				miniSeq.clear();
			}			
			time = currentTime;
			miniSeq.add(pms);			 
		}
		// we must make the last mini-sequence
		makeOneMiniSequence(miniSeq);
		miniSeq.clear(); 	
	}
	
	/**
	 * make one new mini sequence.
	 *  
	 *  The first picture get at the end a "X".
	 *  That is for visibility if compressed.
	 */
	private void makeOneMiniSequence(List<PictureMiniSequence> miniSeq) {
		if (miniSeq.size() <= 1) {
			miniSeq.clear();
			return; 
		}
		int i = PM_MetadataContainer.getInstance().getNewMiniSequenceNumber();
		int n = 0;
		for (PictureMiniSequence pi:miniSeq) {
			String ms = "m" + String.valueOf(i) + "_" + String.valueOf(pi.seqNumber) + "_";
			if (n == 0) {
				// the first picture: it is visible if compressed
				ms  += "X";
			}
			n++;
			pi.picture.meta.setMiniSequence(ms);
		}
		
	}
	
	 
	/**
	 * Interrupt the Read Thumb Thread.
	 */
	protected void stopReadingThumbs() {
		if (readThumbFilesThread != null) {
			readThumbFilesThread.setStop(); // Stop the thumbsToRead queue
		}
	}
	
	
	
	/**
	 * A Thumbnail file was read and is available.
	 */
	protected void thumbAvailable(int bitIndex) {
		if (bitIndex < 0) {
			return;
		}
		if (picturesImported.get(bitIndex)) {
			barValue++;
			SwingUtilities.invokeLater(new Runnable() {
				 
				public void run() {
					progressBar.setValue(barValue);			
				}
			});
		}
		picturesImported.set(bitIndex, false);
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
		
		// Button move all pictures to the left side (not copy)
		buttonAlle = PM_Utils.getJButon(ICON_1_LEFT);  
		buttonAlle.setEnabled(false);
		panel.add(buttonAlle);
		buttonAlle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (index.controller.sizeDargestellt() == 0) {
					return; // no pictures to move
				}
				PM_Index ivTo = PM_WindowMain.getInstance().getIndexViewThumbnails();
				ivTo.data.clearAndAdd(index.controller.getPictureListDisplayed());
				index.data.removeAllPictures();
			}
		});

		// Button all pictures append to the pictures on the left side.
		buttonAlleAppend = PM_Utils.getJButon(ICON_2_LEFT); 
		buttonAlleAppend.setEnabled(false);
		panel.add(buttonAlleAppend);
		buttonAlleAppend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (index.controller.sizeDargestellt() == 0) {
					return; // no pictures to append
				}
				PM_Index ivTo = PM_WindowMain.getInstance().getIndexViewThumbnails();
				ivTo.data.appendPictureList(index.controller.getPictureListDisplayed());
				index.data.removeAllPictures();
			}
		});

		// Button "delete" all pictures.
		buttonLoeschen = PM_Utils.getJButon(ICON_DELETE);
		panel.add(buttonLoeschen);
		buttonLoeschen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				index.data.removeAllPictures();
			}
		});
		// Button "cancel"
		buttonCancel = new JButton("cancel");
		buttonCancel.setEnabled(false);
		panel.add(buttonCancel);
		buttonCancel.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				stopReadingThumbs();
			}		
		});
 	
		// Progress bar
		progressBar.setMinimum(0);
		progressBar.setForeground(Color.RED);
		panel.add(progressBar);
		
		// make scrollpane
		JScrollPane sc = new JScrollPane(panel);
		sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		return sc;
	}
	
	/**
	 * Read all the Thumbnail files.
	 * 
	 * 
	 *
	 */
	private class ReadThumbFilesThread extends Thread {
		 
		private List<PM_Picture> pictures;
		private Map<PM_Picture, PM_PictureView> pictureViewTable;
		private PM_IndexView indexView;
		private PM_Listener endListener;
		
		private int nextToRead = 0;
		
		private boolean stop = false;
		
		/**
		 * Constructor 
		 */
		public ReadThumbFilesThread(PM_IndexView indexView, List<PM_Picture> pictures,
				Map<PM_Picture, PM_PictureView> pictureViewTable, PM_Listener endListener) {
			this.indexView = indexView;
			this.pictures = pictures;
			this.pictureViewTable = pictureViewTable;
			this.endListener = endListener;
		}
		
		/**
		 * Set index of next to read.
		 * 
		 * This is the first picture in the index view.
		 */
		public void setNextToRead(int nextToRead) {
			this.nextToRead = nextToRead;
		}
		
		public void setStop() {
			stop = true;
		}
		
		public void run() {
			while (true) { 	
				
				if (picturesImported.isEmpty()) {
					endListener.actionPerformed(new PM_Action(this));
					return;
				}
				
				// get next thumbnail file  to read
				int i = 0;
				PM_Picture picture = null;
				for (i = picturesImported.nextSetBit(nextToRead); i >= 0; i = picturesImported.nextSetBit(i+1)) {
					picture = index.data.getPicture(i);
					break;
				}
				if (picture == null && nextToRead > 0) {
					for (i = picturesImported.nextSetBit(0); i >= 0; i = picturesImported.nextSetBit(i+1)) {
						picture = index.data.getPicture(i);
						break;
					}
				}				 
				if (picture == null) { 
					return; // the queue is empty			 
				}
				// There are a thumbnail file to read.
				
				
				// Keep the image instance in a variable until
				// you have painted, because it is managed by SoftReference.
				// >>>>>> Don't remove the image declaration <<<<<<<<<
				Image image = picture.getImageThumbnail(true /* to import */);
	
//System.out.println("Index VIEW IMPORT. thumb file read: " + picture.getFileOriginal().getName())	;		
				if (pictureViewTable.containsKey(picture)) {
					pictureViewTable.get(picture).repaint();
				}
				thumbAvailable(i);
				image = null;
				if (stop) {
					endListener.actionPerformed(new PM_Action(this));
					return;
				}
			} // while (true)
		}
	} // End Class ReadThumbsThread	
	
}
