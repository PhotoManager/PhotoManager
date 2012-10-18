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

import pm.gui.*;
import pm.inout.*;
import pm.picture.*;
import pm.utilities.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;
 

/**
 * Class to display the thumbs in a window.
 * <p>
 * methods: repaintOnePicture(PM_Picture)
 * 
 * repaintViewPort() // without check
 * 
 * paintAll(PM_Picture firstPicInTheViewport)
 * 
 * 
 * 
 * The thumbs to display are managed in the class PM_IndexViewCollection. An
 * instance creates in the constructor and is not known outside from here.
 * <p>
 * Only this class create the pictureView instances. This class too read the
 * thumbs and make a The thumbs to display in this panel reads lazy. Only the
 * thumbs for the view port are present. Therefore I can't use the swing layout
 * manager. I calculate myself the positions of thumbs.
 * 
 */
@SuppressWarnings("serial")
public class PM_IndexView extends PM_IndexViewDragAndDrop implements PM_Interface 
		  {



	private static final int DEFAULT_SLIDER_VALUE = 100;

	/**
	 * All the pictures visible in the view port.
	 */
	final protected Map<PM_Picture, PM_PictureView> pictureViewTable = new ConcurrentHashMap<PM_Picture, PM_PictureView>();
	/**
	 * A list of pictures shown as white square.
	 */
	final private Set<PM_Picture> pictureClosed = new HashSet<PM_Picture>();
	/**
	 * A list of hidden mini sequences. If a minisequence are not shown, here
	 * stores the shown pictures of the sequence. This is only for to paint a
	 * marker.
	 */
	final protected Set<PM_Picture> pictureHidden = new HashSet<PM_Picture>();

	protected PM_IndexController indexController;
	private PM_IndexData indexData;
	private PM_FocusPanel focusPanel;
	private PM_Configuration config = null;
	protected PM_WindowMain windowMain = null;

	private int sliderValue = 100;

	/**
	 * The panel to display the thumbs (toolbar, panel for thumbs, and slider)
	 */
	private JPanel indexPanel; // the whole panel
	private JScrollPane indexToolbar; // on top
	private JScrollPane scrollPane; // center
	private JTextField selPictures; // count for selected pictures

	




	protected PM_WindowBase windowBase;

	private final int hGap = 10; // Horizontal
	private final int vGap = 10; // Vertical

	private final Queue<PM_Picture> thumbsToRead = new ConcurrentLinkedQueue<PM_Picture>();
	private ReadThumbsThread readThumbsThread = null;

	/**
	 * Constructor.
	 */
	public PM_IndexView() {
	}

	
	
	/**
	 * init: Build the indexPanel (on top the toolbar, panel for thumbs, and
	 * slider)
	 */
	protected void init(PM_Index index, PM_WindowBase windowBase,
			PM_IndexController indexController, PM_IndexData indexModel) {
		this.windowMain = PM_WindowMain.getInstance(); // windowMain;
		this.windowBase = windowBase;
		this.index = index;
		config = PM_Configuration.getInstance();
		this.indexController = indexController;
		this.indexData = indexModel;
		
		 
		
		// create a ChangeListener for deletion in export subwindow
		PM_Listener deleteListener = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				// a picture was deleted in the subwindow export.
				// Paint it now red.
				if (e.getObject() instanceof PM_Picture) {
					PM_Picture pic = (PM_Picture) e.getObject();
					if (pictureViewTable.containsKey(pic)) {
						pictureViewTable.get(pic).repaint();
					}
					// Remove it from the pictureClosed List
					pictureClosed.remove(pic);
				}
			} // actionPerformed
		}; // PM_Listenert
		PM_DeletePictures.addDeleteListener(deleteListener);

		// Models change listerer
		index.data.addChangeListener(new PM_Listener() {
			 
			public void actionPerformed(PM_Action e) {
				pictureListChanged(e);
			}

		});

		// build the indexPanel (toolbar, panel for thumbs, and slider)
		createScrollPane();
		indexPanel = new JPanel(new BorderLayout());
		indexToolbar = getToolbar();
		indexPanel.add(indexToolbar, BorderLayout.NORTH);
		indexPanel.add(scrollPane, BorderLayout.CENTER);
		indexPanel.add(getSlider(), BorderLayout.SOUTH);

		// the final instance to compute viewPort after changed some values.
		// (call the instance only synchronized)
 	 
	}

	 

	protected boolean isLeft() {
		return true; // Overrides
	}

	/**
	 * return the indexPanel
	 * <p>
	 * on top the toolbar, center the panel for thumbs, and on bottom the
	 * slider.
	 * 
	 */
	public JPanel getIndexPanel() {
		return indexPanel;
	}

	public JPanel getIndexToolbar() {
		return (JPanel) indexToolbar.getViewport().getView();
	}

	/**
	 * 
	 */
	protected Set<PM_Picture> getPictureClosed() {
		return pictureClosed;
	}

	protected void repaintPicture(PM_Picture picture) {
		if (pictureViewTable.containsKey(picture)) {
			pictureViewTable.get(picture).repaint();
		}
	}

	protected void initEnd() {

		indexPanel.setPreferredSize(indexViewPort.getExtentSize());
		indexViewPort.validate();
		// vpExtentSizeOld = indexViewPort.getExtentSize();
	}

	protected void closePicture(List<PM_Picture> list) {
		pictureClosed.addAll(list);
	}

	protected boolean openPicture(List<PM_Picture> list) {
		return pictureClosed.removeAll(list);
	}

	protected void requestFocus(PM_Picture picture) {
		if (pictureViewTable.containsKey(picture)) {
			pictureViewTable.get(picture).requestFocusInWindow();
		}
	}

	

	


	// ======================================================
	// getPreferredSizePictureView()
	//
	// Errechnet die View-Groesse bei gegebener SliderValue
	// 
	// In diesem Rechteck muss alles f�r ein Thumb Platz haben:
	// In der Mitte das Bild (quadratisch)
	// Darunter Text
	// Und einen Rand (THUMBNAIL_BORDER) rundherum
	// ======================================================
	private Dimension getPreferredSizePictureView() {
		return new Dimension(sliderValue
				+ (2 * PM_PictureView.THUMBNAIL_BORDER), sliderValue
				+ (2 * PM_PictureView.THUMBNAIL_BORDER)
				+ PM_PictureView.THUMBNAIL_TEXT);
	}

	// ======================================================
	// getBounds()
	// ======================================================
	private Rectangle getBounds(int ind, int colCount, int rowCount,
			Dimension pictureSize, int sizeDisplayed) {
		// System.out.println(" getBounds " + index + " colCount = " +
		// colCount + ", rowCont = " + rowCount);

		Rectangle rec = new Rectangle();

		if (ind >= sizeDisplayed) {
			return rec; // ung�tiger index
		}

		int row = (ind + 1) / colCount;
		if ((ind + 1) % colCount != 0) {
			row++;
		}

		int col = (ind + 1) % colCount;
		if (col == 0) {
			col = colCount;
		}

		row--;
		col--;
		rec.x = col * (pictureSize.width + vGap) + vGap;
		rec.y = row * (pictureSize.height + hGap) + hGap;
		rec.width = pictureSize.width + vGap;
		rec.height = pictureSize.height + hGap;

		return rec;
	}

	// ================= Listener ====================================

	/**
	 * TEST TEST TEST
	 */
	private boolean isTest() {
 		return false;
		//  return !index.isLeft();
	}

	/**
	 * The scroll bar adjustment value has changed.
	 * 
	 * Add new picture views to the view port and remove the picture views not
	 * visible in the view port.
	 */
	private int scrollBarAdjEvtValueOld = 0;

	private void verticalScrollBarChanged(AdjustmentEvent e) {
		if (isTest()) { // TEST TEST TEST TEST !!!!!!!!!!!!!
			return;
		} 
	

		int size = index.data.getPictureSize();
		if (size == 0) {
			removeAllPictureViews(); // nothing to paint
			return;
		}

		if (e.getValue() == scrollBarAdjEvtValueOld) {
			return; // nothing changed
		}
		scrollBarAdjEvtValueOld = e.getValue();
//		System.out.println("..... verticalScrollBarChanged");
		// update the view port
		paintViewport(null);

	}

	/**
	 * The viewport size has changed.
	 */
	// private Dimension vpExtentSizeOld = new Dimension();
	private Point fromToRowOld = new Point();
	private Point colRowSizeOld = new Point();

	private void viewPortChanged(ComponentEvent e) {

		if (isTest()) { // TEST TEST TEST TEST !!!!!!!!!!!!!
			return;
		}
		Object o = e.getSource();
		if (o != indexViewPort) {
			return;
		}
	
		
		 
		// Check visible rows and columns
		Point fromToRow = getColRowSizeInViewPort(getPreferredSizePictureView());
		Point colRowSize = getColRowSize(indexController.getPictureSize());
		boolean rowChanged = !fromToRow.equals(fromToRowOld);
		boolean colChanged = !colRowSize.equals(colRowSizeOld);
		fromToRowOld = fromToRow;
		colRowSizeOld = colRowSize;
		if (!rowChanged && !colChanged) {
			return; // visible rows and columns in view port not changed
		}
//		System.out.println("..... viewPortChanged");
		paintViewport(null);
	}

	/**
	 * The mouse wheel has changed.
	 */
	private void mouseWheelChanged(MouseWheelEvent me) {

		if (index.data.getPictureSize() == 0) {
			removeAllPictureViews(); // nothing to paint
			return;
		}
		// calculate the new view port position
		Dimension d = getPreferredSizePictureView();
		int vpPosY = indexViewPort.getViewPosition().y;
		int vpExtY = indexViewPort.getExtentSize().height;
		int rest = client.getHeight() - vpPosY - vpExtY;
		int rotation = me.getWheelRotation();
		if (rest > 0 && rotation == 1) {
			// forward
			vpPosY = vpPosY + d.height + hGap;
		} else if (rotation == -1 && vpPosY > 0) {
			// backward
			vpPosY = vpPosY - d.height - hGap;
			vpPosY = (vpPosY <= 0) ? 0 : vpPosY;
		} else {
			return; // can't scroll
		}
		// correct position (cut not the picture at the top)
		if (vpPosY > 0 && (vpPosY % (d.height + hGap)) > 0) {
			int n = vpPosY / (d.height + hGap);
			if (rotation == 1) {
				vpPosY = n * (d.height + hGap);
			} else {
				vpPosY = (n + 1) * (d.height + hGap);
			}
		}
		// now change the view port position
		Point vPos = indexViewPort.getViewPosition();
		vPos.y = vpPosY;
		// change view port position.
		// (the ComponentListener cause painting)
		indexViewPort.setViewPosition(vPos);
	}

	/**
	 * The Slider has changed.
	 */
	private void sliderChanged(ChangeEvent e) {
		if (isTest()) { // TEST TEST TEST TEST !!!!!!!!!!!!!
			return;
		}
	
		JSlider source = (JSlider) e.getSource();
		sliderValue = (int) source.getValue();
		// System.out.println(windowBase.getName() + ": sliderChanged: " +
		// sliderValue);
		int size = index.data.getPictureSize();
		if (size == 0) {
			removeAllPictureViews();  
			return;
		}
		
		 
		PM_Picture pic = null;
		
		if (client.getComponentCount() > 0) {
			Object o = client.getComponent(0);
			if (o instanceof PM_PictureView) {
				pic = ((PM_PictureView)o).getPicture();
			}
		}
		
//		System.out.println("..... sliderChanged");
		paintViewport(pic);
		// paintViewport(null);
	}

	/**
	 * The picture list has changed.
	 */
	private void pictureListChanged(PM_Action e) {
		if (isTest()) { // TEST TEST TEST TEST !!!!!!!!!!!!!
			return;
		}
		
		int size = index.data.getPictureSize();
		if (size == 0) {
			removeAllPictureViews(); // nothing to paint
			return;
		}

		// System.out.println(windowBase.getName() + ": pictureListChanged: " +
		// e.getString() + ", size = " + size);
//		System.out.println("..... pictureListChanged");
		paintViewport(null);

		// paintViewport(null);

	}

	/**
	 * Remove all picture Views from client
	 */
	private void removeAllPictureViews() {
		scrollBarAdjEvtValueOld = 0;
		pictureViewTable.clear();
		int count = client.getComponentCount();
		if (count == 0) {
			return; // no components (picture views painted)
		}

		pictureClosed.clear();
		client.removeAll();
		client.repaint();
		Dimension cl = client.getPreferredSize();
		cl.height = 0;
		client.setPreferredSize(cl);
		client.revalidate();
	}

	// ====================================================================================

	/**
	 * return the index toolbar for the left subwindow
	 * 
	 */
	protected JScrollPane getToolbar() {
		return null; // Overrides

	}

	/**
	 * Create a Scrollpane for to display the thumbs
	 */
	private void createScrollPane() {

		// --------------------------------------------
		// first create the client
		// --------------------------------------------
		client = new Client(this);
//		client.setTransferHandler(PM_TransferHandler.getInstance());
		focusPanel = new PM_FocusPanel(null, this, this);

		if (config.isNurLesen()) {
			client.setBackground(PM_WindowBase.COLOR_NUR_LESEN);
		} else {
			client.setBackground(PM_WindowBase.COLOR_BACKGROUND);
		}
		client.setLayout(null); // I do it myself

		client.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// System.out.println("Inndex View: mouseClicked: requestFocusInWindow aufrufen");
				requestFocusInWindow();
			}
		});

		// ------------------------------------------
		// now the scrollpane
		// ------------------------------------------
		scrollPane = new JScrollPane(client);
		indexViewPort = scrollPane.getViewport();

		scrollPane.setWheelScrollingEnabled(false);

		//		 
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// Achtung: VERTICAL_SCROLLBAR_ALWAYS, da sonst unterschiedliche
		// ExtendSize und
		// damit funktioniert der stateChanged nicht mehr.
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// ----------------------------------------------------------------------
		// MouseWheelListener
		// ----------------------------------------------------------------------
		MouseWheelListener mwl = new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent me) {
				mouseWheelChanged(me);
			}
		};
		scrollPane.addMouseWheelListener(mwl);

		// ----------------------------------------------------------------------
		// ChangeListener
		// ----------------------------------------------------------------------
		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				// viewPortSizeChanged(ce);
			}
		};
		scrollPane.getViewport().addChangeListener(cl);

		// addComponentListener
		scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				viewPortChanged(e);
			}
		});

		// addAdjustmentListener(AdjustmentListener l)
		// Scrollbar AdjustmentListener
		JScrollBar sb = scrollPane.getVerticalScrollBar();
		sb.addAdjustmentListener(new AdjustmentListener() {

			 
			public void adjustmentValueChanged(AdjustmentEvent e) {
				verticalScrollBarChanged(e);

			}

		});

		// oldViewPortSize = indexViewPort.getExtentSize();
		
		
		
		
	}

	/**
	 * return the slider to zoom the thumbs
	 * 
	 */
	private JComponent getSlider() {
		
		// ----------- slider -----------
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 400,
				DEFAULT_SLIDER_VALUE);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					sliderChanged(e);
				}
			}
		});
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(30);
		
		// ---------- text field ----------
		selPictures = new JTextField("");
		 
		JPanel p = new JPanel(new BorderLayout());
		p.add(selPictures, BorderLayout.CENTER);
 		p.add(slider, BorderLayout.SOUTH);
		
		return p;
	}

	/**
	 * Paint the count of selected pictures in botton
	 */
	protected void printNumberSelectedPicturesOnStatusLine( ) {
		SwingUtilities.invokeLater(new Runnable() {
			 
			public void run() {
				String text;
				int count = indexData.pictureSelected.cardinality();
				if (count == 1) {
					text = "1 picture selected.";
				} else if (count == 0) {
					text = "NO pictures selected";
				} else {
					text = String.valueOf(count)  + " pictures selected";
				}
				selPictures.setText(text);	
			}
		});
	}
	
 

	/**
	 * refresh View port
	 * 
	 */
	protected void refreshViewport() {
	//	paintViewport();
	 	client.repaint();
	}

	protected void autoscroll(Point cursorLocation) {

		JScrollBar sb = scrollPane.getVerticalScrollBar();

//		System.out.println("ScrollBar. value: " + sb.getValue()
//				+ ", visible aumount: " + sb.getVisibleAmount());
		sb.setValue(sb.getValue() + 4);
	}

	/**
	 * Paint the view port.
	 * @param pic TODO
	 * 
	 */
	private void paintViewport(PM_Picture pic) {

		if (readThumbsThread != null) {
			readThumbsThread.stopIt(); // Stop the thumbsToRead queue
			readThumbsThread = null;
		}
		thumbsToRead.clear();

		int pictureSize = indexController.getPictureSize();
		if (pictureSize <= 0) {
			removeAllPictureViews();
			return; // nothing to paint
		}
		Point colRowSize = getColRowSize(pictureSize);
		int colSize = colRowSize.x;
		int rowSize = colRowSize.y;
		// calculate client
		Dimension pictureViewSize = getPreferredSizePictureView();
		int clientY = (pictureViewSize.height + vGap) * rowSize + vGap;
		Dimension cl = client.getPreferredSize();
		if (cl.height != clientY) {
			cl.height = clientY;
			client.setPreferredSize(cl);
			client.revalidate();
		}

		Point fromTo = getColRowSizeInViewPort(pictureViewSize);
		int fromRow = fromTo.x;
		int toRow = fromTo.y;
		
	 
		int row  = getRow(pic,  colRowSize);
	 	if (row > 0) {		
			int diff = toRow - fromRow;
			fromRow = row;
			toRow = fromRow + diff;
System.out.println("---- new from/to.  from: "+ fromRow + ", to: "+ toRow);

			Point vPos = indexViewPort.getViewPosition();
			vPos.y = (pictureViewSize.height + vGap) * row  + vGap;
// change view port position.
// (the ComponentListener cause painting)
			indexViewPort.setViewPosition(vPos);
	     }
		
	 	
	 	
		
		
		int fromIndex = colSize * fromRow;
		Set<PM_Picture> picturesInViewport = new HashSet<PM_Picture>();

		
		
 		
		
		
		// System.out.println("............ from row: " + fromRow
		// + ",to row " + toRow
		// + ",from index: " + fromIndex);

		client.removeAll(); // is this necessary ?
	//	client.repaint(); // ???
		boolean firstInViewPort = true;
		for (int i = 0; i < index.data.getPictureSize(); i++) {
			if (i < fromIndex) {
				continue; // not yet in the view port
			}
			if (i > (colSize * (toRow + 1)) - 1) {
				break; // at the end of the view port
			}
			if (firstInViewPort) {
				firstInViewPort = false;
				setNextToRead(i); // to read for import
			}
			// Now we are in the view port.
			PM_Picture picture = index.data.getPicture(i);

			// Get or create the pictureView
			PM_PictureView pictureView;
			if (pictureViewTable.containsKey(picture)) {
				pictureView = pictureViewTable.get(picture);
			} else {
				pictureView = new PM_PictureView (picture,
						windowMain, index);
				pictureViewTable.put(picture, pictureView);
			}
			client.add(pictureView);
			picturesInViewport.add(picture);
			pictureView.setPictureViewPreferredSize(pictureViewSize);
			Rectangle bounds = getBounds(i, colSize, rowSize, pictureViewSize,
					pictureSize);
			pictureView.setBounds(bounds);
			// now check if the thumb nail is available
			if (picture.hasImageThumbnail() == false) {
				// it is not available. 
				// It must be read in the "ReadThumbsThread" inner class.
				thumbsToRead.add(picture);
			}
		} // end for

		// paint now
		indexViewPort.repaint();
		indexViewPort.validate();
		// remove pictureViews not in the view port
		Set<PM_Picture> keys = pictureViewTable.keySet();
		keys.retainAll(picturesInViewport);
		// read thumbs if not yet painted
		if (!thumbsToRead.isEmpty()) {
			readThumbsThread = new ReadThumbsThread(this, thumbsToRead,
					pictureViewTable);
			readThumbsThread.start();
		}
	}

	/**
	 * Now get the Thumbnail.
	 * 
	 * This method overrides for import.
	 */
	protected void checkForThumbnail(PM_Picture picture, int ind) {
		if (picture.hasImageThumbnail() == false) {
			thumbsToRead.add(picture);
		}
	}

	/**
	 * Set index of next to read.
	 * 
	 * Import: This is the first picture in the index view.
	 */
	public void setNextToRead(int nextToRead) {
		// Overrides
	}

	/**
	 * Check if pictures are importing.
	 */
	protected boolean isImport() {
		// Overrides
		return false;
	}

	/**
	 * get the number of columns and rows to display.
	 * 
	 * @return point.x are columns, point.y are rows.
	 */
	protected Point getColRowSize(int pictureSize) {
		Point p = new Point();
		if (pictureSize <= 0) {
			return p; // nothing to paint
		}
		// calculate the number of columns
		Dimension pictureViewSize = getPreferredSizePictureView(); // size for
																	// one
																	// picure
		Rectangle viewRect = scrollPane.getViewport().getViewRect();
		p.x = viewRect.width / (pictureViewSize.width + hGap);
		if (p.x == 0) {
			p.x = 1;
		}
		// calculate the number of rows
		p.y = (pictureSize % p.x == 0) ? pictureSize / p.x : pictureSize
				/ p.x + 1;

		return p;
	}
	
	/**
	 * Returns from and to row in the view port.
	 * 
	 * 
	 */
	private Point getColRowSizeInViewPort(Dimension pictureViewSize) {
		int pvHeight = pictureViewSize.height + vGap;
		int vpPosHeight = indexViewPort.getViewPosition().y;
		int from = vpPosHeight / pvHeight;
		int to = (vpPosHeight + indexViewPort.getExtentSize().height)
				/ pvHeight;
		return new Point(from, to);
	}



	/**
	 * 
	 */
	private int getRow(PM_Picture picture, Point colRowSize) {
	 
		int colSize = colRowSize.x;
		int rwoSize = colRowSize.y;
		if (picture == null) {
			return 0;
		}
	 
		int pictureIndex = index.data.indexOf(picture);
		if (pictureIndex < 0) {
			return 0;
		}
		int pictureSize = index.data.getPictureSize();
		
		
		int row = (pictureIndex + 1)/ colSize;
		
		System.out.println("####  getRow #####: " + row + ", pictureIndex: " + pictureIndex);
		
		return row;
	}
	
	/**
	 * this is a hack !!
	 */
	protected JPanel getClient() {
		return client;
	}

	/**
	 * 
	 *  
	 *
	 */
	
	// ================= INNER CLASS "ComputeViewPort"
	// =============================================

	private class ReadThumbsThread extends Thread {

		private Queue<PM_Picture> thumbsToRead;
		private Map<PM_Picture, PM_PictureView> pictureViewTable;
		private PM_IndexView indexView;

		private boolean interrupt = true;
		/**
		 * Constructor
		 */
		public ReadThumbsThread(PM_IndexView indexView,
				Queue<PM_Picture> thumbsToRead,
				Map<PM_Picture, PM_PictureView> pictureViewTable) {
			this.indexView = indexView;
			this.thumbsToRead = thumbsToRead;
			this.pictureViewTable = pictureViewTable;
		}
		
		protected void stopIt() {
			interrupt = true;
		}

		public void run() {
			interrupt = false;
			while (interrupt == false) {
				PM_Picture picture = thumbsToRead.poll(); // get and remove
				if (picture == null) {
					return; // the queue is empty
				}
				// Keep the image instance in a variable until
				// you have painted, because it is managed by SoftReference.
				// >>>>>> Don't remove the image declaration <<<<<<<<<
				Image image = picture.getImageThumbnail(false);
		//		picture.setImageThumbnail(image);
				// System.out.println("   Index  import. thumb file read: " +
				// picture.getFileOriginal().getName()) ;
				// if (!pictureViewTable.containsValue(picView)) {
				// return; // pictureView unknown. Terminate the thread.
				// }
				if (interrupt == true) {
					image = null;
					return;
				}
				// indexView.thumbAvailable(indexView.index.data.indexOf(picture));
				if (pictureViewTable.containsKey(picture)) {
					pictureViewTable.get(picture).repaint();
				}  
				image = null;

			} // while (true)
		}
	} // End Class ReadThumbsThread

	
  

}
