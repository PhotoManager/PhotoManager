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
package pm.sequence;

import pm.gui.*;
import pm.picture.*;
import pm.utilities.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

@SuppressWarnings("serial")
abstract public class PM_TreeWindow extends JTree implements PM_Interface,
		Autoscroll {

	protected DefaultMutableTreeNode rootNode;
	private PM_WindowBase windowBase;
	private boolean isClearedSelection = false;
	protected PM_TreeModel treeModel;
	private PM_TreeWindow treeWindow;
	private boolean expanded = false;
	
	public PM_TreeWindow(PM_WindowBase windowBase, PM_TreeModel treeModel, MouseListener mouseListener) {

		super();
		treeWindow = this;
		setModel(treeModel);
		this.treeModel = treeModel;
		this.rootNode = treeModel.getRootNode();
		this.windowBase = windowBase;
		initTree();
		createAndAddChangeListener();
		if (mouseListener == null) {
			makeMouseListener();
		} else {
			addMouseListener(mouseListener);
		}
		

		setCellRenderer(getDefaultTreeCellRenderer());

	}
 
	 
	public void toggleExpandCollapse(JButton button) {
		if (expanded) {
			expanded = false;
			collapseAll();
			button.setText("Expand");
		} else {
			expanded = true;
			expandAll();
			button.setText("Collapse");
		}
	}
	
	private void expandAll() {
		expanded = true;
		int row = 0;
		while (row < getRowCount()) {
			expandRow(row);
			row++;
		}
	}

	private void collapseAll() {
		expanded = false;
		int row = getRowCount() - 1;
		while (row >= 1) {
			collapseRow(row);
			row--;
		}
	}
	
	public void selectSequenz(PM_Sequence sequenz, DefaultMutableTreeNode node) {
		windowBase.darstellungSequenz(sequenz, node);
	}

	public PM_TreeModel getTreeModel() {
		return treeModel;
	}

	public boolean isSequenceSelected() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (node == null) {
			return false;
		}
		return (node.getUserObject() instanceof PM_Sequence);
	}

	public boolean isNodeSelected() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (node == null)
			return false;
		return (node != rootNode);
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return (DefaultMutableTreeNode) getLastSelectedPathComponent();
	}

	public void clearSelection(boolean isClearedSelection) {
		this.isClearedSelection = isClearedSelection;
		if (isClearedSelection) {
			super.clearSelection();
		}
	}

	public boolean isClearedSelection() {
		return isClearedSelection;
	}

	 

	private void initTree() {

		this.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setRootVisible(true);
		this.putClientProperty("JTree.lineStyle", "Angled");
		this.setToggleClickCount(3); // default should be 2 !!??

		makeKeyBinding();

	}

	private void createAndAddChangeListener() {

		// ----------------------------------------------------------------
		// TreeWillExpandListener
		// (Root node ALWAYS expanded)
		// ----------------------------------------------------------------
		TreeWillExpandListener twel = new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent e)
					throws ExpandVetoException {
				TreePath tp = e.getPath();
				if (tp.getPathCount() > 1) {
					return;
				}
				// root node NOT collapse
				throw new ExpandVetoException(e);
			}

			public void treeWillExpand(TreeExpansionEvent e)
					throws ExpandVetoException {
			}

		};
		addTreeWillExpandListener(twel);
	}

	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	private void makeKeyBinding() {

		Action actionSequent = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				// -----------------------------------------------------
				// pressed enter: Display all Pictures of the series
				// -----------------------------------------------------
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
				if (node == null) {
					return;
				}

				Object uo = node.getUserObject();
				if (uo instanceof PM_Sequence) {
					selectSequenz((PM_Sequence) uo, node);
				} else {
					selectSequenz(null, null);
				}
			}
		};

		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"enter");
		this.getActionMap().put("enter", actionSequent);

	}

	private void makeMouseListener() {
		KeyAdapter keyAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
System.out.println("Keyadapter pressed (tree window)");
				if (PM_Utils.isTemp(e)) {
					System.out.println("ist Temp Picture");
				}
			}
		};
//		addKeyListener(keyAdapter);

		MouseAdapter tableMouseListener = new MouseAdapter() {
//			public void mousePressed(MouseEvent e) {
//			}

//			public void mouseReleased(MouseEvent e) {
//			}

			public void mouseClicked(MouseEvent e) {
				Point clickPoint = e.getPoint();
				TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
				if (path == null) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				int button = e.getButton();
				int click = e.getClickCount();

				if (button == 1) {
					if (click >= 2) {
						windowBase.doubleClickOnTree(node, treeWindow);
					}
				} else if (button == 3) {
					showContextMenu(e, node);
				}
			}
		};
		addMouseListener(tableMouseListener);

	}

	private Font font = null;
	private Color fgColor = null;
	private Color bgColor = null;

	private DefaultTreeCellRenderer getDefaultTreeCellRenderer() {

		DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);

				if (font == null) {
					font = this.getFont();
				}
				if (fgColor == null) {
					fgColor = this.getForeground();
				}
				if (bgColor == null) {
					bgColor = this.getBackground();
				}
				this.setFont(font);
				this.setForeground(fgColor);
				this.setBackground(bgColor);

				if (value instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode user = (DefaultMutableTreeNode) value;

					// if leaf and no sequence and no PM_PictureDirectory
					if (user.isLeaf()
							&& !(user.getUserObject() instanceof PM_Sequence)
							&& !(user.getUserObject() instanceof PM_PictureDirectory)) {
						this.setForeground(Color.GREEN);
						this.setFont(new Font("Arial", Font.BOLD, 12));
						return this;
					}

					renderCell(tree, user, this);
				}
				return this;
			};
		};

		return cellRenderer;
	}

	protected void renderCell(JTree tree, DefaultMutableTreeNode node,
			DefaultTreeCellRenderer renderer) {
		// overrides
		return;
	}

	 

	protected JMenuItem menuItemDelete = null;
	protected JMenuItem menuItemChange = null;
	protected JMenuItem menuItemNewNode = null;

	private JPopupMenu popup = null;

	private void showContextMenu(MouseEvent e, final DefaultMutableTreeNode node) {

		popup = new JPopupMenu();
		// ----------------------------------------------
		// Menu: delete line
		// ----------------------------------------------
		menuItemDelete = new JMenuItem("löschen Eintrag");
		ActionListener alZeileLoeschen = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				doMenuItemDelete(node);
			}
		};
		menuItemDelete.addActionListener(alZeileLoeschen);

		// ----------------------------------------------
		// Menu: change name
		// ----------------------------------------------
		menuItemChange = new JMenuItem("ändern Name");
		ActionListener alZeileAendern = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				if (doMenuItemChangeName(node)) {
					treeModel.nodeChanged(node);
					treeModel.fireNameChanged(node);
				}
			}
		};
		menuItemChange.addActionListener(alZeileAendern);

		// ----------------------------------------------
		// Menu: new node
		// ----------------------------------------------
		menuItemNewNode = new JMenuItem("neuer Knoten");
		ActionListener alNewNode = new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				doMenuItemNewNode(node);
			}
		};
		menuItemNewNode.addActionListener(alNewNode);

		// ---------------------------------------------------------
		// put it all together
		// ---------------------------------------------------------
		editPopupMenu(popup, node);

		// ---------------------------------------------------------
		// -------------- popup the menu ----------------------
		// ---------------------------------------------------------
		if (popup.getSubElements().length > 0) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}

	}

	protected void editPopupMenu(JPopupMenu popup, DefaultMutableTreeNode node) {
		// overrides
	}

	protected void doMenuItemDelete(DefaultMutableTreeNode node) {

		int n = JOptionPane.showConfirmDialog(this, "Eintrag entfernen?",
				"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}
		// remove from tree
		treeModel.removeNodeFromParent(node);
		treeModel.nodeChanged(treeModel.getRootNode());

	}

	private boolean doMenuItemChangeName(DefaultMutableTreeNode node) {
		// return true: the name was changed

		if (node.getParent() == null) {
			return false; // root-node: cannot change the name
		}

		Object user = node.getUserObject();
		if (user instanceof String) {
			String name = getNewNameDialog(user.toString());
			if (name == null) {
				return false;
			}
			node.setUserObject(name);
			return true;
		} else if (user instanceof PM_Sequence) {
			PM_Sequence seq = (PM_Sequence) user;
			String name = getNewNameDialog(seq.getLongName());
			if (name == null) {
				return false;
			}
			seq.setLongName(name);
			return true;
		}
		return false;
	}

	private boolean doMenuItemNewNode(DefaultMutableTreeNode node) {
		String name = getNewNameDialog("neuer Name");
		if (name == null) {
			return false;
		}
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
		treeModel.insertNodeInto(newNode, node, 0);
		// make the new node visible
		TreePath tp = getTreePath(node);
		if (isCollapsed(tp)) {
			expandPath(tp);
		}
		return true;
	}

	protected String getNewNameDialog(String oldName) {
		String mesage = "Gib einen neuen Namen ein.";
		Object obj = JOptionPane.showInputDialog(mesage, oldName);
		if (obj == null || !(obj instanceof String)) {
			return null; // invalid or break
		}
		String name = (String) obj;
		name = (String) name.trim();
		name = name.replaceAll("\\.", " ");
		if (name.length() == 0) {
			return SEQU_NAME_UNKNOWN;
		}
		return name;

	}

	public void expandTree(int level) {
		Enumeration en = rootNode.preorderEnumeration();
		while (en.hasMoreElements()) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) en
					.nextElement();
			TreePath tp = getTreePath(tn);
			if (tp.getPathCount() > level) {
				continue;
			}
			if (isCollapsed(tp)) {
				expandPath(tp);
			}
		}
	}

	public TreePath getTreePath(DefaultMutableTreeNode node) {
		List<Object> list = new ArrayList<Object>();
		list.add(node);
		DefaultMutableTreeNode tn = (DefaultMutableTreeNode) node.getParent();
		while (tn != null) {
			list.add(0, tn);
			tn = (DefaultMutableTreeNode) tn.getParent();
		}

		return new TreePath(list.toArray());
	}

}
