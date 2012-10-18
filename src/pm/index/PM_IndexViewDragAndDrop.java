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
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;


import pm.dragndrop.*;
import pm.picture.*;
import pm.utilities.*;

/**
 * Class to handle all drag and drop features for the index view.
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_IndexViewDragAndDrop extends JPanel implements PM_Interface {

	protected JPanel client;
	protected JViewport indexViewPort;
	private static final int MARGIN = 30; // for autoscrolling
	protected PM_Index index;

 
	
	
	
	/**
	 * Get Inset for Client autoscroll
	 * 
	 * @return
	 */
	protected Insets getAutoscrollInsets() {
		int top = indexViewPort.getViewPosition().y;
		int bottom = client.getHeight() - top
				- indexViewPort.getExtentSize().height;
		top = (top == 0) ? 0 : top + MARGIN;
		bottom = (bottom == 0) ? 0 : bottom + MARGIN;

		return new Insets(top, 0, bottom, 0);
	}

	 
	public boolean canDrop(PM_PictureTransferable localPictures) {
		return false;  // Overrides 
	}
	
	public boolean canDrop(List<File> remotePictures) {
		return false; // Overrides 
	}
	
	public void doDrop(PM_PictureTransferable transferData, PM_Picture insertAt) {	
		
		PM_IndexView sourceIV = transferData.getIndexView();
		if (sourceIV == this) {
			PM_Picture fromPicture = transferData.getPictureUnderCursor();
			index.data.movePictureList(fromPicture, transferData.getPictureList(), insertAt);
			return  ;  
		}
	 
		
		 index.indexView.windowBase.insertPictureList(transferData.getPictureList(), insertAt);
			
		 
		 
		 
	}
	
	/**
	 * Inner class for all the picture views in a panel.
	 *  
	 * It handle also the autoscrolling.
	 *
	 */
	class Client extends JPanel implements Autoscroll {

		private final PM_IndexView indexView;

		public Client(final PM_IndexView indexView) {
			this.indexView = indexView;

			
			
			PM_DropTargetPictureList dtl = new PM_DropTargetPictureList( 
					indexView) {
								
				@Override
				public void dragEnter(DropTargetDragEvent event) {
	//				System.out.println("......drag enter IndexView");
					Transferable transferable = event.getTransferable();
					PM_PictureTransferable data = getTransferPictureList(transferable);
					if (data == null) {
						event.rejectDrag();
						return; 
					}					
					if (canDrop(data)) {
						event.acceptDrag(DnDConstants.ACTION_COPY);
						return;
					}
					event.rejectDrag();
				}	
				
				@Override
				public void drop(DropTargetDropEvent event) {
					System.out.println(">>>>>>>>>> drop in IndexView");	
					Transferable transferable = event.getTransferable();
					PM_PictureTransferable transferData = getTransferPictureList(transferable);
					if (transferData == null) {
						return; 
					}	
					doDrop(transferData, null);
				};
				
			};
			
		
			
			setAutoscrolls(false); // I set the timing myself
			new PM_DropTargetAutoScroller(this, dtl);
		}

		 
		public void autoscroll(Point cursorLocn) {
			indexView.autoscroll(cursorLocn);
		}

		 
		public Insets getAutoscrollInsets() {
			return indexView.getAutoscrollInsets();
		}

		/**
		 * Paint the autoscroll region (on top and buttom of the index panel)
		 */
		 
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Color color = g.getColor();
			g.setColor(Color.RED);
			int top = indexViewPort.getViewPosition().y;
			int bottom = client.getHeight() - top
					- indexViewPort.getExtentSize().height;
			int x2 = indexViewPort.getExtentSize().width;
			int y = 0;
			// top
			if (top != 0) {
				y = top + MARGIN;
				drawLine(g2d, 0, y, x2, y);
			}
			// bottom
			if (bottom != 0) {
				y = top + indexViewPort.getExtentSize().height - MARGIN;
				drawLine(g2d, 0, y, x2, y);
			}
			// reset color
			g.setColor(color);
		}

		private void drawLine(Graphics2D g2d, int x, int y, int width,
				int height) {

			Stroke strokeOld = g2d.getStroke();
			Color color = g2d.getColor();
			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 50.0f, 5.0f },
					4.0f));
			g2d.drawLine(x, y, width, height);
			g2d.setStroke(strokeOld);
			g2d.setColor(color);
		}

	 
	}

	/**
	 *  Inner Class 
	 */
	class PM_DropTargetAutoScroller extends DropTarget {

		private Component component;
		private Autoscroll autoScroll;

		private Timer timer;

		private Point locn;
		private Point prev;
		private Point screenLocation;

		private Rectangle outer = new Rectangle();
		private Rectangle inner = new Rectangle();

		private int hysteresis = 10;

		/**
		 * Constructor.
		 */
		protected PM_DropTargetAutoScroller(Component c, DropTargetListener dtl) {
			super(c, dtl);
		}

		@Override
		protected DropTargetAutoScroller createDropTargetAutoScroller(
				Component c, Point p) {
			return new MyClass(c, p);
		}

		// ----------- Inner class ------------
		protected class MyClass extends DropTarget.DropTargetAutoScroller
				implements ActionListener {

			 
			protected MyClass(Component c, Point p) {
				super(c, p);
				super.stop();

				component = c;
				autoScroll = (Autoscroll) component;

				Toolkit t = Toolkit.getDefaultToolkit();

				Integer initial = Integer.valueOf(100);
				Integer interval = Integer.valueOf(100);

				try {
					initial = (Integer) t
							.getDesktopProperty("DnD.Autoscroll.initialDelay");
				} catch (Exception e) {
					// ignore
				}

				try {
					interval = (Integer) t
							.getDesktopProperty("DnD.Autoscroll.interval");
				} catch (Exception e) {
					// ignore
				}

				timer = new Timer(interval.intValue(), this);

				timer.setCoalesce(true);
				timer.setInitialDelay(initial.intValue());

				locn = p;
				prev = p;

				screenLocation = new Point(p); // 30.7.2011
				SwingUtilities.convertPointToScreen(screenLocation, c); // 30.7.2011

				try {
					hysteresis = ((Integer) t
							.getDesktopProperty("DnD.Autoscroll.cursorHysteresis"))
							.intValue();
				} catch (Exception e) {
					// ignore
				}

				timer.start();
			}

			/**
			 * update the geometry of the autoscroll region
			 */

			private void updateRegion() {
				Insets i = autoScroll.getAutoscrollInsets();
				Dimension size = component.getSize();

				if (size.width != outer.width || size.height != outer.height)
					outer.reshape(0, 0, size.width, size.height);

				if (inner.x != i.left || inner.y != i.top)
					inner.setLocation(i.left, i.top);

				int newWidth = size.width - (i.left + i.right);
				int newHeight = size.height - (i.top + i.bottom);

				if (newWidth != inner.width || newHeight != inner.height)
					inner.setSize(newWidth, newHeight);

			}

			/**
			 * cause autoscroll to occur
			 
			 */
			protected synchronized void updateLocation(Point newLocn) {
				prev = locn;
				locn = newLocn;

				screenLocation = new Point(locn); // 30.7.2011
				SwingUtilities.convertPointToScreen(screenLocation, component); // 30.7.2011

				if (Math.abs(locn.x - prev.x) > hysteresis
						|| Math.abs(locn.y - prev.y) > hysteresis) {
					if (timer.isRunning())
						timer.stop();
				} else {
					if (!timer.isRunning())
						timer.start();
				}
			}

			/**
			 * cause autoscrolling to stop
			 */
			protected void stop() {
				timer.stop();
			}

			/**
			 * cause autoscroll to occur
		
			 */

			public synchronized void actionPerformed(ActionEvent e) {

				updateRegion();

				Point componentLocation = new Point(screenLocation); // 30.7.2011
				SwingUtilities.convertPointFromScreen(componentLocation,
						component); // 30.7.2011

				if (outer.contains(componentLocation)
						&& !inner.contains(componentLocation))
					autoScroll.autoscroll(componentLocation);
			}

	 
	
		}

	}

}
