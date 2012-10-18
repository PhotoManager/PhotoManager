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

package pm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 
import java.io.*;
 
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
 
import javax.swing.tree.*;

import pm.gui.*;
import pm.utilities.*;

/**
 * The very first start ask for the Top Level Picture Directory.
 * 
 * A dialog prompt the TLPD.
 * 
 */
public class PM_WindowGetTLPD implements PM_Interface {

	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private File userHome;
	private DefaultListModel listModel;
	private JProgressBar progressionBar;
	private JPanel imagePanel;
	private Image image = null;
	private final JList list = new JList();
	private JTextField tlpd;
	private File tlpdFile;
	private JButton buttonAccept;
	private JButton buttonCancel;
	private final JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);
	
	
	/**
	 * Constructor.
	 */
	public PM_WindowGetTLPD() {
		init();
	}

	/**
	 * Tree Node Selected
	 */
	private void nodeSelected(MyTreeNode my, DefaultMutableTreeNode node) {

		File dir = my.getFile();
		image = null;
		imagePanel.repaint();
		listModel.clear();
		if (my.getPictures() != 0) {			
			for (File file : dir.listFiles()) {
				if (PM_Utils.isPictureFile(file)) {
					listModel.addElement(file );
				}
			}		
			list.setSelectedIndex(0);
		}	
		if (node.isLeaf() ) {  		 
			tlpd.setText("");
			tlpdFile = null;
			buttonAccept.setEnabled(false);
		} else {
			tlpdFile = my.getFile();
			tlpd.setText(tlpdFile.getAbsolutePath() + " (" + my.getSumOfPictures() + " Bilder)");
			buttonAccept.setEnabled(true);		
		}	
	}
	
	/**
	 * get result
	 */
	public File getResult() {
		return tlpdFile;
	}
	
	/**
	 * Create the Dialog.
	 */
	private void init() {
		JPanel dialogPanel = new JPanel();
		// dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		// dialogPanel.add(headerPanel);
		Dimension screen = PM_Utils.getScreenSize(); 
 		screen.width = screen.width -100;
 		screen.height = screen.height -100;
 		
 		Dimension ps = new Dimension(screen.width/3, 0);
		JComponent left = getLeftComponent();
		JComponent middle = getMiddleComponent();
		JComponent right  = getRightComponent();

 		left.setPreferredSize(ps);
  		middle.setPreferredSize(ps);
 
 		
 		
		JSplitPane spLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				left, middle);	 
		JSplitPane spRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				spLeft, right);
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(spRight, BorderLayout.CENTER);
		dialogPanel.add(getBottomPanel(), BorderLayout.SOUTH);
	 

		dialog.setPreferredSize(screen);
		dialog.setLocation(50, 25);
	 
		dialog.getContentPane().add(dialogPanel);
		dialog.pack();
	 
		
		Thread startThread = new Thread() {
			public void run() {
				progressionBar.setIndeterminate(true);
				// make the tree
				readAllPictureFiles(rootNode, userHome, null);
				// expand all
				for (int i = 0; i < tree.getRowCount(); i++) {
					// tree.expandRow(i);
				}
				// expand root and roots children
				expandNode(rootNode);
				Enumeration<DefaultMutableTreeNode> en = rootNode.children();
				while (en.hasMoreElements()) {
					expandNode(en.nextElement());
				}
				progressionBar.setIndeterminate(false);
			}
		};
		startThread.start();
		dialog.setVisible(true);
		dialog.dispose();
	}

	/**
	 * The left side with JTextField and buttons.
	 * 
	 */
	private JComponent getLeftComponent() {
		JTextField tf = new JTextField("Kommentar");
		JScrollPane scrollPane = new JScrollPane(tf); 
		// all together
		JPanel panel = new JPanel(new BorderLayout());
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(scrollPane, BorderLayout.CENTER);
	 
		return panel;
		 
	}

	/**
	 * The middle side with Tree
	 * 
	 */
	private JComponent getMiddleComponent() {
		userHome = PM_Utils.getHomeDir();

	 	/* TEST */userHome = new File(userHome.getAbsolutePath() + File.separator + "tmp");

		rootNode = new DefaultMutableTreeNode(userHome.getPath());
		tree = new JTree(rootNode);
	 	tree.setRootVisible(false);
		tree.setCellRenderer(getDefaultTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Listen for when the selection changes.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			 
			public void valueChanged(TreeSelectionEvent arg0) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (node == null) {
					return; // Nothing is selected.
				}
				Object nodeInfo = node.getUserObject();
				if (nodeInfo instanceof MyTreeNode) {
					nodeSelected((MyTreeNode) nodeInfo, node);
				}

			}
		});

		JScrollPane scrollPane = new JScrollPane(tree);

		
		
		// Progression bar
		progressionBar = new JProgressBar();
		progressionBar.setForeground(Color.RED);
		JPanel pb = new JPanel();
		pb.add(progressionBar);
		// all together
		JPanel panel = new JPanel();   
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(scrollPane);
		panel.add(pb);	
		return panel;
	}


	/**
	 * The right side with list and icon
	 */
	private JComponent getRightComponent() {
		listModel = new DefaultListModel(); 
		list.setModel(listModel);
		list.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
		 
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					int index = list.getSelectedIndex();
					if (index >= 0) {
						// Paint picture
						Object o = listModel.getElementAt(index);
						if (o instanceof File) {
							paintPicture((File)o);
						}
					}
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(list);

		// Panel for Icon
		imagePanel = new JPanel() {
			public void paintComponent(Graphics g) {
				// fill the background
				g.setColor(Color.GRAY);
				g.fillRect(0,0,getWidth(), getHeight());
				// now draw the image
				if (image != null) {
					int w = image.getWidth(null);
					int h = image.getHeight(null);
					int side = Math.max(w,h);
					double scale = 200.0/(double)side;
					w = (int)(scale * (double)w);
					h = (int)(scale * (double)h);
					g.drawImage(image, 10,10,w,h,null);
				}
			}
		};

		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane,
				new JScrollPane(imagePanel));

		return sp;
	}

	/**
	 * Bottom Panel
	 */
	private JComponent getBottomPanel() {
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
		// Button cancel
		buttonCancel = new JButton("cancel");
		buttonCancel.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				tlpdFile = null;
				dialog.dispose();			
			}			
		});
		p.add(buttonCancel);
		// Button accept
		buttonAccept = new JButton("accept");
		buttonAccept.setEnabled(false);
		buttonAccept.addActionListener(new ActionListener() {
			 
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}			
		});
		p.add(buttonAccept);
		// Label and Text Field		
		p.add(new JLabel("Bilderverzeichnis: "));
		tlpd = new JTextField();
		tlpd.setColumns(30);
		p.add(tlpd);
		
		
		return p;	
	}
	
	
	/**
	 * Paint picture
	 */
	private void paintPicture(File file) {
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			image = null; 
		}
		
		imagePanel.repaint(); 
	}
	
	/**
	 * Read recursive all picture files and build the tree.
	 */
	private void readAllPictureFiles(DefaultMutableTreeNode root, File dir,
			PM_ListenerX listener) {

		if (!isPictureDir(dir)) {
			return;
		}

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				new MyTreeNode(dir.getName(), dir));
		root.add(node);

		File[] files = dir.listFiles();
		Arrays.sort(files);
		int pictures = 0;
		// are pictures in the dir ?
		for (File file : files) {
			if (PM_Utils.isPictureFile(file)) {
				pictures++;
			}
			if (isPictureDir(file)) {
				readAllPictureFiles(node, file, listener);
			}
		}
		if (pictures != 0) {
			((MyTreeNode) node.getUserObject()).setPictures(pictures);
			// add picture count to all parents
			TreeNode tn = node.getParent();
			while (tn != null) {

				if (tn instanceof DefaultMutableTreeNode) {
					Object o = ((DefaultMutableTreeNode) tn).getUserObject();
					if (o instanceof MyTreeNode) {
						MyTreeNode my = (MyTreeNode) o;
						my.add(pictures);
					}
				}
				tn = tn.getParent();

			}
			return;
		} else {
			if (node.isLeaf()) {
				node.removeFromParent();
			}
		}
	}

	/**
	 * Test if directory can have valid pictures.
	 */
	private boolean isPictureDir(File dir) {

		// check if directory
		if (!dir.isDirectory()) {
			return false; // NO Directory
		}

		String dirName = dir.getName();
		if (dirName.equals(DIR_METADATEN) || dirName.equals(DIR_METADATEN_ROOT)
				|| dirName.equals(DIR_PM_TEMP)) {
			return false;
		}

		return true;
	}

	/**
	 * Expand a node
	 */
	private void expandNode(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		TreePath treePath = new TreePath(path);
		tree.expandPath(treePath);
	}

	// =====================================================
	// getDefaultTreeCellRenderer()
	//
	// Hier werden die Markierungen behandelt
	// =====================================================

	@SuppressWarnings("serial")
	private DefaultTreeCellRenderer getDefaultTreeCellRenderer() {
		DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				if (value instanceof DefaultMutableTreeNode) {
					Object o = ((DefaultMutableTreeNode) value).getUserObject();
					if (o instanceof MyTreeNode) {
						MyTreeNode my = (MyTreeNode) o;
						if (my.getPictures() != 0) {
							this.setForeground(Color.RED);
						}
					}
				}
				return this;
			};
		};
		return cellRenderer;
	}

	/**
	 * Inner class Tree Node
	 */
	@SuppressWarnings("serial")
	static class MyTreeNode extends DefaultMutableTreeNode {

		private String name;
		private int pictures = 0;
		private File file;
		private int sumOfPictures = 0;

		/**
		 * Constructor
		 */
		protected MyTreeNode(String name, File file) {
			this.name = name;
			this.file = file;

		}
		
		/**
		 * add picture counts.
		 */
		protected void add(int pictures) {
			sumOfPictures += pictures;
		}

		/**
		 * get sumOfPictures
		 */
		protected int getSumOfPictures() {
			return sumOfPictures;
		}
		
		/**
		 * Get file
		 */
		protected File getFile() {
			return file;
		}

		/**
		 * set pictures.
		 */
		protected void setPictures(int pictures) {
			this.pictures = pictures;
			sumOfPictures += pictures;
		}

		/**
		 * get pictures
		 */
		protected int getPictures() {
			return pictures;
		}

		/**
		 * To string
		 */
		@Override
		public String toString() {
			if (pictures != 0) {
				return name + "(" + pictures + " Bilder)";
			}
			return name;
		}

	}

}
