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

 
 
import pm.PM_AddOn;
import pm.utilities.*;
 
import pm.dragndrop.PM_Transferable;
import pm.index.PM_Index;
import pm.inout.*;
import pm.picture.*;
import pm.search.*;
import pm.sequence.*;
 
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;
 
import javax.swing.tree.*;

/**
 * 
 * 
 */
@SuppressWarnings("serial")
public class PM_WindowSearch extends PM_WindowBase implements PM_Interface {

	private PM_FocusPanel upperPanel = null;

	private PM_TreeWindowAlbum treeAlbum;
	private JPanel panelLeftTreeLabel;

	private PM_TreeWindow treeWindowBase;
	private PM_TreeWindow treeWindowExtended;
	private PM_TreeWindow treeWindowHardDisk;

	private TreeSelectionListener treeSelectionListener;

	// private JPanel belowPanel = null;
	// private JButton buttonSuchen = null;
	private JButton buttonDarstellen = null;
	private JButton buttonAppend = null;
	private JButton buttonDiaShow = null;
	 

	private JButton clearButtonHits = null;
	private JTextField hitsUpperPanel = null;

	private PM_IndicesComboBox	index1;
	private PM_IndicesComboBox	index2;
	
	// Suchen mit Lucene
	private PM_Search luceneSuchen = null;
	private int anzahlHits = 0;

	// Qualitaet: Check-Boxes
	private JCheckBox q1 = null;
	private JCheckBox q2 = null;
	private JCheckBox q3 = null;
	private JCheckBox q4 = null;

	 
	private JComboBox comboBoxSequenzen = null;

	// Datum
	private JComboBox vonJahr = null;
	private JComboBox vonMonat = null;
	private JComboBox vonTag = null;
	private JComboBox bisJahr = null;
	private JComboBox bisMonat = null;
	private JComboBox bisTag = null;

 

	private Vector<PM_Listener> changeAnzahlListener = new Vector<PM_Listener>();
	private JSplitPane splitPaneBottom = null;
	private JTabbedPane tabbedPaneRight;
//	private JTabbedPane tabbedPaneLeft;
	private Vector<SortOrder> sortOrder_deprecated = new Vector<SortOrder>();
    private JSplitPane splitPaneLeftRight = null;
	
	
	 
	private JPanel albumLeft;
 
 
	private JPanel treePanelExtended;
	private JPanel treePanelBase;
	private JPanel treePanelHardDisk;

	private final PM_Index   indexViewThumbnails;
	
	private boolean addOnIndex2 = false;
	private boolean addOnExtSequence = false;
	
	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_WindowSearch(PM_WindowMain windowMain ) {
		super(null);

		addOnIndex2 = PM_AddOn.getInstance().getAddOnIndex2();
		addOnExtSequence = PM_AddOn.getInstance().getAddOnExtSequence();
		
		 
	 
		indexViewThumbnails = windowMain.getIndexViewThumbnails();	
		
		// ComboBox fÃ¼r SortOrder aufbereiten
		sortOrder_deprecated.add(new SortOrder(SearchSortType.NOTHING, " "));
		sortOrder_deprecated.add(new SortOrder(SearchSortType.TIME, "Bild Datum"));
		sortOrder_deprecated.add(new SortOrder(SearchSortType.SEQ, "Serie"));
		sortOrder_deprecated.add(new SortOrder(SearchSortType.FILE_NAME, "Datei Name"));
		sortOrder_deprecated.add(new SortOrder(SearchSortType.FILE_PATH, "Datei Pfad"));

		// ------------------------------------------------------
		// selection Listener wenn eine Zeile in einem
		// Sequenz-Baum selektiert wurde.
		// -------------------------------------------------------
		treeSelectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Object o = e.getSource();	
				if (!(o instanceof PM_TreeWindow)) {
					return;
				}
				PM_TreeWindow tw = (PM_TreeWindow) o;
				if (tw.isSelectionEmpty()) {
					return;
				}			
				TreePath tp = e.getPath();
				Object oo = tp.getLastPathComponent();
				
				if (oo instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode tn = (DefaultMutableTreeNode) oo;
					selectSequence(tn, tw);
				}
																										
	//			clearTreeSelection(e, tw);
			}
		};

		// -----------------------------------------
		// alle Windows aufbereiten und Keybindigs setzen
		// -----------------------------------------
		initWindow();
		initKeyBindigs();
		
		
		int location = PM_All_InitValues.getInstance().getValueInt(this, "vertical-devider");
		if (location == 0) {
			location = 150;
		}
		
		splitPaneLeftRight.setDividerLocation(location);
		
		
		
		
		
		
		// --------------------------------------------------------
		// Change Listener for message
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
		bDiashow.setText(PM_MSG.getMsg("diashow"));
		xDiashow.setText(PM_MSG.getMsg("diashow"));
		hdDiashow.setText(PM_MSG.getMsg("diashow"));
		albumLeftDiashow.setText(PM_MSG.getMsg("diashow"));
		buttonDiaShow.setText(PM_MSG.getMsg("diashow"));
		
		datLabelVon.setText(PM_MSG.getMsg("winSrcDatFrom"));		
		labelNew.setText(PM_MSG.getMsg("winSrcNew"));
		labelDnD.setText(PM_MSG.getMsg("winSrcDnD"));
		qLabel.setText(PM_MSG.getMsg("category14"));
		sLabel.setText(PM_MSG.getMsg("winSrcSeq"));
		datLabelTo.setText(PM_MSG.getMsg("winSrcDatTo"));
		
		tabbedPaneRight.setTitleAt(0, PM_MSG.getMsg("winSrcSeqB"));
		
		
		if (addOnExtSequence) {
			tabbedPaneRight.setTitleAt(1, PM_MSG.getMsg("winSrcSeqX"));
			tabbedPaneRight.setTitleAt(2, PM_MSG.getMsg("winSrcDisc"));
		} else {
			tabbedPaneRight.setTitleAt(1, PM_MSG.getMsg("winSrcDisc"));
		}
		
		
		
		
		
//		tabbedPaneLeft.setTitleAt(0, PM_MSG.getMsg("photalbum"));
//		tabbedPaneLeft.setTitleAt(1, PM_MSG.getMsg("indices"));
			
		
	}
	
	// ======================================================
	// close()
	//
	// Ende der Verarbeitung
	// ======================================================
	@Override
	public void close() {
	 
		 
		
		PM_All_InitValues.getInstance().putValueInt(this, "vertical-devider",
				splitPaneLeftRight.getDividerLocation());
		
		
	}
	
	
	
	// ======================================================
	// closeAlbum()
	// ======================================================
	@Override
	public void closeAlbum() {	 
		DefaultMutableTreeNode node = treeAlbum.getRootNode( );
		if (node == null) { 
			return;
		}
		 
		node.removeAllChildren();
		treeAlbum.getTreeModel().nodeStructureChanged(node);
	} 

	/**
	 * flush all windows.
	 * 
	 * remove all displayed thumbs so i.e. you can do the import 
	 *
	 */
	@Override
	public boolean flush() {
		doClearHits();
		
		treeAlbum.clearSelection(true);
		hitsAlbumTree.setText("");

		 
		
		
		return true;
	}
 
	
	
	// =====================================================
	// selectSequence()
	//
	// Eine Zeile in einem Sequenz-Baum wurde selektiert.
	// Jetzt diese Sequenz darstellten.
	// =====================================================
	private void selectSequence(DefaultMutableTreeNode tn, PM_TreeWindow tw) {
		
		// Anzahl Suchen und anzeigen
		PM_Sequence sequenz = null;
		Object s = tn.getUserObject();
		if (s instanceof PM_Sequence) {
			sequenz = (PM_Sequence) s;			 
		} 
 	
		if (s instanceof PM_PictureDirectory) {
			// Suchen von Original (Festplatte)
			PM_PictureDirectory pd = (PM_PictureDirectory)s;
			hitsHardDiskTree.setText(String.valueOf(pd.getPictureSizeValid()));		
			
			return; 
		}
		
		int anzahl = 0;
		if (sequenz != null) {
			PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
			searchExpr.setSequenz(sequenz);
			PM_Search luceneSuchen = new PM_Search(searchExpr);
			anzahl = luceneSuchen.search();			
		} 
		String anz = String.valueOf(anzahl);
		// Hits anzeigen
		if (tw == treeAlbum) {		
			hitsAlbumTree.setText(anz);
			if (anzahl > 0) {
				albumUp.setEnabled(true);
			} else {
				albumUp.setEnabled(false);
			}	
//		} else if (tw == treeNeu) {
//			hitsNewTree.setText(anz);
		} else if (tw == treeWindowBase) {
			hitsBaseTree.setText(anz);
			if (anzahl > 0) {
				seriesBup.setEnabled(true);
			} else {
				seriesBup.setEnabled(false);
			}
		} else if (tw == treeWindowExtended) {
			hitsExtendedTree.setText(anz);
			if (anzahl > 0) {
				seriesXup.setEnabled(true);
			} else {
				seriesXup.setEnabled(false);
			}
		} else if (tw == treeWindowHardDisk) {
			hitsHardDiskTree.setText(anz);
		}  
 
 		  
	}
	
	// ======================================================
	// getPictureList( );
	// ======================================================
	public List<PM_Picture> getPictureList(DefaultMutableTreeNode tn) {

		if (tn == null) {
			return new ArrayList<PM_Picture>();
		}
		
		Object s = tn.getUserObject(); 
		if (s instanceof PM_PictureDirectory) {
			// Suchen von Original (Festplatte)
			PM_PictureDirectory pd = (PM_PictureDirectory)s;				
			List<File> files = pd.getOrigFiles();	
			List<PM_Picture> pics = PM_Pictures.getPictureList(files); 
			Collections.sort(pics, PM_Utils.SORT_TIME_ORDER);
			return 	pics;	 
		}
	
		PM_Sequence sequenz = null;
		if (s instanceof PM_Sequence) {
			sequenz = (PM_Sequence) s;			 
		} 
		if (sequenz == null) {	
			return new ArrayList<PM_Picture>();
		}	
		// Der TreeNode hat eine Sequenz
 		
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(sequenz);
		PM_Search luceneSuchen = new PM_Search(searchExpr);
		int anzahl = luceneSuchen.search();
		if (anzahl == 0) {
			return new ArrayList<PM_Picture>();
		}
		if (sequenz instanceof PM_SequenceOriginal) {
			return luceneSuchen.getPictureList(SearchSortType.FILE_PATH);
		}
		
		
		if (sequenz instanceof PM_SequenceAlbum && ((PM_SequenceAlbum)sequenz).getSeqClosed() == null) {
			return luceneSuchen.getPictureList(SearchSortType.TIME);
		}	
		return luceneSuchen.getPictureList(SearchSortType.SEQ);
	}
	
	
	
	
	// ======================================================
	// doubleClickOnTree( );
	//
	// z.B. bei Doppelclick
	// ======================================================
	@Override
	public void doubleClickOnTree(DefaultMutableTreeNode tn, PM_TreeWindow tw) {
		List<PM_Picture> picList = getPictureList(tn);
		if (picList.size() == 0) {
			return;
		}
		indexViewThumbnails.data.clearAndAdd(picList);	
	}
	
	 
	 
  

	// =====================================================
	// initWindow()
	// =====================================================
	private void initWindow() {

		setLayout(new BorderLayout());

		// ------------------------------------------------------
		// oben
		// ------------------------------------------------------
		upperPanel = getSearchPanel();
		JScrollPane scUpperPanel = new JScrollPane(upperPanel);
		scUpperPanel
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scUpperPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);


		// -------------------------------------------------------
		// unten (rechts und links)
		// -------------------------------------------------------
		tabbedPaneRight = getTabbedPaneRight();
		
	 	
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(upperPanel,BorderLayout.NORTH);
		right.add(tabbedPaneRight,BorderLayout.CENTER);
		
		
		 	
		albumLeft = getAlbum();

		JScrollPane scRight = new JScrollPane(right);
		scRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scRight.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		splitPaneLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				albumLeft, scRight);
		splitPaneLeftRight.setDividerLocation(500);
		splitPaneLeftRight.setOneTouchExpandable(true);
		// ------------------------------------------------
		// fertig: jetzt alle zusammensetzen
		// ------------------------------------------------

		add(scUpperPanel, BorderLayout.NORTH);
		add(splitPaneLeftRight, BorderLayout.CENTER);

	}

	// =====================================================
	// initKeyBindigs()
	// =====================================================
	private void initKeyBindigs() {
		// --------------------------------------------------------
		// Key Bindings
		// -------------------------------------------------------
		// Action aVK_CTRL_ENTER =  AbstractAction() {
		// public void actionPerformed(ActionEvent e) {
		// keyPressedEnter();
		// }
		// };
		Action aVK_1 = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(1);
			}
		};
		Action aVK_2 = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(2);
			}
		};
		Action aVK_3 = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(3);
			}
		};
		Action aVK_4 = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				keyPressedKategorie(4);
			}
		};

		InputMap imap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap map = this.getActionMap();
		// imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
		// InputEvent.CTRL_MASK), "VK_CTRL_ENTER");
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK),
				"VK_1");
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK),
				"VK_2");
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK),
				"VK_3");
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK),
				"VK_4");
		map.put("VK_1", aVK_1);
		map.put("VK_2", aVK_2);
		map.put("VK_3", aVK_3);
		map.put("VK_4", aVK_4);
		// map.put("VK_CTRL_ENTER", aVK_CTRL_ENTER);

		// set default aktiver Focus
		setAktiverFocus(treeWindowBase);

		// Focus Cycle Policy (die Componenten, in denen der Focus mit Tab
		// wechselt)

		// ------------------------------------------------------
		// Focus-Panels aufbereiten
		// ------------------------------------------------------
		addFocusPanel(upperPanel);
		 
		setBackgroundUpperPanel(COLOR_BG_PANEL);

	}

	// =====================================================
	// getTabbedPaneRight()
	// =====================================================
	private JTabbedPane getTabbedPaneRight() {

		JTabbedPane tabbedPane = new JTabbedPane();

		 
//		scTreeNew = getNew();
		treePanelBase = getTreePanelBase();
		treePanelExtended = getSeriesX();
		treePanelHardDisk = getHardDisk();
		// Reihenfolge der Tabs	
		//	(Wenn Index-ï¿½nderungen, dann SourceBundle berï¿½cksichtigen !!!)
		tabbedPane.insertTab("B-Serien",null, treePanelBase, "", 0);
		if (addOnExtSequence) {
			tabbedPane.insertTab("X-Serien",null, treePanelExtended, "", 1);
			tabbedPane.insertTab("Festplatte",null, treePanelHardDisk, "", 2);
		} else {
			tabbedPane.insertTab("Festplatte",null, treePanelHardDisk, "", 1);
		}
		 

		return tabbedPane;

	}
/*
	// =====================================================
	// getTabbedPaneRight()
	// =====================================================
	private JTabbedPane getTabbedPaneLeft() {

		JTabbedPane tabbedPane = new JTabbedPane();

	//	getIndex12();
		albumLeft = getAlbum();
		 
		// Reihenfolge der Tabs
		// (Wenn Index-ï¿½nderungen, dann SourceBundle berï¿½cksichtigen !!!)
		tabbedPane.insertTab("Fotoalbum",null, albumLeft, "", 0);
		 
		 
		
		
		
		return tabbedPane;

	}

	
	*/
	
	
	 
	private JTextField hitsBaseTree;
	private JButton seriesBup;
	private JButton bDiashow;
	private JPanel getTreePanelBase() {
		// oben
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Button "< links darstellen"
		JButton sereiesBDisplay = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(sereiesBDisplay);
		ActionListener alBDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowBase
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.clearAndAdd(picList);
			}
		};
		sereiesBDisplay.addActionListener(alBDisplay);
 
		// Button "<< links darstellen"
		JButton bAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(bAppend);
		ActionListener albAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowBase
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.appendPictureList(picList);
			}
		};
		bAppend.addActionListener(albAppend);

		// Hits
		hitsBaseTree = new JTextField("");
		hitsBaseTree.setForeground(Color.BLACK);
		hitsBaseTree.setEditable(false);
		hitsBaseTree.setFocusable(false);
		hitsBaseTree.setColumns(4);
		panel.add(hitsBaseTree);
		Font font = hitsBaseTree.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsBaseTree.setFont(fontBold);

		// Button "Diashow"
//		bDiashow = PM_Utils.getJButon(ICON_SLIDESHOW); //new JButton("?Diashow");	
		bDiashow =  new JButton("?Diashow");	
		panel.add(bDiashow);
		ActionListener alBDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDiashowFromTreeWindow(treeWindowBase.getSelectedNode());
			}
		};
		bDiashow.addActionListener(alBDia);
				
		// Button "^ oben darstellen  "
		seriesBup = PM_Utils.getJButon(ICON_1_UP);
		seriesBup.setEnabled(false);
		panel.add(seriesBup);
		ActionListener alUp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = treeWindowBase.getSelectedNode();	
				Object s = node.getUserObject();
				PM_Sequence sequenz = (s instanceof PM_Sequence) ? (PM_Sequence)s: null;
				darstellungSequenz(sequenz, null);							 
			} 					 
		};
		seriesBup.addActionListener(alUp);
			
		// Filter "Not in Album"
		final JButton notInAlbum = new JButton("No Album");
		panel.add(notInAlbum);
		notInAlbum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				Set<DefaultMutableTreeNode> nodesNotInAlbum = treeAlbum.getTreeModel().getNodesNotInAlbum();
				
System.out.println("Sequences not in Album:"  );				
				List<DefaultMutableTreeNode> nodeList = new ArrayList<DefaultMutableTreeNode>();    	 
		    	Enumeration en = treeWindowBase.getRootNode().preorderEnumeration();
		    	while (en.hasMoreElements()) {
		    		DefaultMutableTreeNode tn  = (DefaultMutableTreeNode)en.nextElement();
System.out.println("    Next tree node: " + tn);
 if (tn.isLeaf()) {
	 System.out.println("                       >>>>>>> Leaf: "+ tn); 
 }
		    	//		if (seq == tn.getUserObject())  {
		    	//			nodeList.add(tn);	
		    	//		}		 
		    	}
				
				
				
				
		   // 	protected Set<DefaultMutableTreeNode> nodesNotInAlbum = new HashSet<DefaultMutableTreeNode>();
		   // 	public Set<DefaultMutableTreeNode> getNodesNotInAlbum () {
		   //// 		return nodesNotInAlbum;
		    //	}			
				
				
				
				
			}
		});

		// Expand tree
		final JButton expandTree = new JButton("Expand");
		panel.add(expandTree);
		expandTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeWindowBase.toggleExpandCollapse(expandTree);
			}
		});
		
		// -----------------------------------------------------------
		// alles zusammenbasteln
		// -----------------------------------------------------------
		JPanel upper = new JPanel();
		upper.setAlignmentX(0);
		upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
		upper.add(panel);

		JScrollPane scUpper = new JScrollPane(upper);
//		scUpper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		scUpper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// --- Tree -----
		treeWindowBase = new PM_TreeWindowBase(this);
		treeWindowBase.addTreeSelectionListener(treeSelectionListener);
		JScrollPane scTree = new JScrollPane(treeWindowBase);

		// Zusammensetzen
		JPanel p = new JPanel(new BorderLayout());
		p.add(scUpper, BorderLayout.NORTH);
		p.add(scTree, BorderLayout.CENTER); 

		return p;

	}

	// =====================================================
	// getSeriesX()
	// =====================================================
	private JTextField hitsExtendedTree;
	private JButton seriesXup;
	private JButton xDiashow;
	private JPanel getSeriesX() {
		// oben
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Button "< links darstellen"
		JButton sereiesXDisplay = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(sereiesXDisplay);
		ActionListener alXDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowExtended
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.clearAndAdd(picList);
			}
		};
		sereiesXDisplay.addActionListener(alXDisplay);

		// Button "<< links darstellen"
		JButton xAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(xAppend);
		ActionListener alxAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowExtended
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.appendPictureList(picList);
			}
		};
		xAppend.addActionListener(alxAppend);

		// Hits
		hitsExtendedTree = new JTextField("");
		hitsExtendedTree.setForeground(Color.BLACK);
		hitsExtendedTree.setEditable(false);
		hitsExtendedTree.setFocusable(false);
		hitsExtendedTree.setColumns(4);
		panel.add(hitsExtendedTree);
		Font font = hitsExtendedTree.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsExtendedTree.setFont(fontBold);

		// Button "Diashow"
		xDiashow  = new JButton("?Diashow");
		panel.add(xDiashow);
		ActionListener alXDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDiashowFromTreeWindow(treeWindowExtended.getSelectedNode());
			}
		};
		xDiashow.addActionListener(alXDia);
		
		// Button "^ oben darstellen  "
		seriesXup = PM_Utils.getJButon(ICON_1_UP);
 		seriesXup.setEnabled(false);
		panel.add(seriesXup);
		ActionListener alUp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = treeWindowExtended.getSelectedNode();	
				Object s = node.getUserObject();
				PM_Sequence sequenz = (s instanceof PM_Sequence) ? (PM_Sequence)s: null;
				darstellungSequenz(sequenz, null);							 
				} 					 
		};
		seriesXup.addActionListener(alUp);
		
		
		// Expand tree
		final JButton expandTree = new JButton("Expand");
		panel.add(expandTree);
		expandTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeWindowExtended.toggleExpandCollapse(expandTree);
			}
		});
		
		
		// -----------------------------------------------------------
		// alles zusammenbasteln
		// -----------------------------------------------------------
		JPanel upper = new JPanel();
		upper.setAlignmentX(0);
		upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
		upper.add(panel);

		JScrollPane spUpper = new JScrollPane(upper);
		spUpper
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spUpper
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// --- Tree -----
		treeWindowExtended = new PM_TreeWindowExtended(this);
		treeWindowExtended.addTreeSelectionListener(treeSelectionListener);
		JScrollPane sc = new JScrollPane(treeWindowExtended);

		// Zusammensetzen
		JPanel p = new JPanel(new BorderLayout());
		p.add(spUpper, BorderLayout.NORTH);
		if (addOnExtSequence) {
			p.add(sc, BorderLayout.CENTER);
		}

		return p;

	}
	
 
	
	// =====================================================
	// getHardDisk()
	// =====================================================
	private JTextField hitsHardDiskTree;
	private JButton hdDiashow;
	private JPanel getHardDisk() {
		// oben
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Button "< links darstellen"
		JButton hdDisplay = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(hdDisplay);
		ActionListener alHdDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowHardDisk
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.clearAndAdd(picList);
			}
		};
		hdDisplay.addActionListener(alHdDisplay);

		// Button "<< links darstellen"
		JButton hdAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(hdAppend);
		ActionListener alhdAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = getPictureList(treeWindowHardDisk
						.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.appendPictureList(picList);
			}
		};
		hdAppend.addActionListener(alhdAppend);

		// Hits
		hitsHardDiskTree = new JTextField("");
		hitsHardDiskTree.setForeground(Color.BLACK);
		hitsHardDiskTree.setEditable(false);
		hitsHardDiskTree.setFocusable(false);
		hitsHardDiskTree.setColumns(4);
		panel.add(hitsHardDiskTree);
		Font font = hitsHardDiskTree.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsHardDiskTree.setFont(fontBold);

		// Button "Diashow"
	    hdDiashow = new JButton("?Diashow");
		panel.add(hdDiashow);
		ActionListener alhdDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDiashowFromTreeWindow(treeWindowHardDisk.getSelectedNode());
			}
		};
		hdDiashow.addActionListener(alhdDia);

		JPanel upper = new JPanel();
		upper.setAlignmentX(0);
		upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
		upper.add(panel);

		JScrollPane spUpper = new JScrollPane(upper);
		spUpper
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spUpper
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// --- Tree -----
		treeWindowHardDisk = new PM_TreeWindowOriginal(this);
		treeWindowHardDisk.addTreeSelectionListener(treeSelectionListener);
		JScrollPane sc = new JScrollPane(treeWindowHardDisk);

		// Zusammensetzen
		JPanel p = new JPanel(new BorderLayout());
		p.add(spUpper, BorderLayout.NORTH);
		p.add(sc, BorderLayout.CENTER);

		return p;

	}
	
 
	// =====================================================
	// getAlbum()
	// =====================================================
	private JTextField hitsAlbumTree;
	private JButton albumDown;
	private JButton albumUp;
	private JButton albumLeftDiashow;
	private JPanel getAlbum() {
		// oben
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));		 

		// Button "< links darstellen"
		JButton albumLeftDisplay = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(albumLeftDisplay);
		ActionListener alLeftDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = 
					getPictureList(treeAlbum.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.clearAndAdd(picList);
			}
		};
		albumLeftDisplay.addActionListener(alLeftDisplay);
		
		// Button "<< links darstellen"
		JButton albumLeftAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(albumLeftAppend);
		ActionListener alLeftAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = 
					getPictureList(treeAlbum.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				indexViewThumbnails.data.appendPictureList(picList);
			}
		};
		albumLeftAppend.addActionListener(alLeftAppend);		
		
		// Hits
		hitsAlbumTree = new JTextField("");
		hitsAlbumTree.setForeground(Color.BLACK);
		hitsAlbumTree.setEditable(false);
		hitsAlbumTree.setFocusable(false);
		hitsAlbumTree.setColumns(4);
		panel.add(hitsAlbumTree);
		Font font = hitsAlbumTree.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsAlbumTree.setFont(fontBold);
						 
		// Button "Diashow"
	    albumLeftDiashow = new JButton("?Diashow");		 
		panel.add(albumLeftDiashow);
		ActionListener alAlbumLeftDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDiashowFromTreeWindow(treeAlbum.getSelectedNode());
			}
		};
		albumLeftDiashow.addActionListener(alAlbumLeftDia);
				
		// Button "v unten ï¿½ndern  "
	    albumDown = PM_Utils.getJButon(ICON_1_DOWN);
	    albumDown.setEnabled(false);
		panel.add(albumDown);
		ActionListener alLeftDown = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = treeAlbum.getSelectedNode();	
				if (node == null) {
					return;
				}
				PM_SequenceAlbum sequenz = null;
				Object s = node.getUserObject();
				if (s instanceof PM_SequenceAlbum) {
					sequenz = (PM_SequenceAlbum) s;	
					aendernAlbum(sequenz, node, treeAlbum);
				} 				
			}
		};
		albumDown.addActionListener(alLeftDown);
 	
	 	
		// Button "^ oben darstellen  "
	    albumUp = PM_Utils.getJButon(ICON_1_UP);
	    albumUp.setEnabled(false);
		panel.add(albumUp);
		ActionListener alLeftUp = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = treeAlbum.getSelectedNode();	
				Object s = node.getUserObject();
				PM_Sequence sequenz = (s instanceof PM_Sequence) ? (PM_Sequence)s: null;
				darstellungSequenz(sequenz, null);							 
				} 					 
		};
		albumUp.addActionListener(alLeftUp);
		
		// Expand tree
		final JButton expandAlbum = new JButton("Expand");
		panel.add(expandAlbum);
		expandAlbum.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeAlbum.toggleExpandCollapse(expandAlbum);
			}		 					 
		});
		
		
		JPanel upper = new JPanel();
		upper.setAlignmentX(0);
		upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));	
		upper.add( panel  );

		panelLeftTreeLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel leftTreeLabel = new JLabel("keine Bilder selektiert");	
		panelLeftTreeLabel.add(leftTreeLabel);
//		upper.add(panelLeftTreeLabel);	
  
		
		JScrollPane spUpper = new JScrollPane(upper);
		spUpper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spUpper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		// --- Tree -----		
		treeAlbum = new PM_TreeWindowAlbum(this, null);	
		treeAlbum.addTreeSelectionListener(treeSelectionListener);
		treeAlbum.setMarkSelectedSequence(true);
		JScrollPane spTreeAlbumLeft = new JScrollPane(treeAlbum);
		
		
		// Zusammensetzen		 
 		JPanel p = new JPanel(new BorderLayout());
 		p.add(spUpper,  BorderLayout.NORTH);
 		p.add( spTreeAlbumLeft, BorderLayout.CENTER  );
 		
		return p;
	}

	 
  

	// =====================================================
	// initRequestFocus: Unmittelbar nach Start den Focus auf Tree setzen
	// =====================================================
	public Component initRequestFocus() {
		JTree t = treeWindowExtended;
		t.setBackground(COLOR_BG_PANEL_SEL);
		t.requestFocusInWindow();
		t.setSelectionRow(0); // die erste Zeile (virtuelles Verzeichnis) wird
		// selctiert
		return t;

	}

	// =====================================================
	// addChangeAnzahlListener()
	//
	// Die Anzahl der Bilder haben sich veraendert
	// =====================================================
	public void addChangeAnzahlListener(PM_Listener listener) {
		if (!changeAnzahlListener.contains(listener))
			changeAnzahlListener.add(listener);
	}

	/**
	 * 
	 *
	 */
	public void initTreeOriginal() {
		PM_TreeModel tm = treeWindowHardDisk.getTreeModel();
		PM_TreeModelOriginal tmo = (PM_TreeModelOriginal)tm;
		tmo.init();
	}
	public PM_TreeWindow getTreeWindowOriginal() {
		return treeWindowHardDisk;
	}
	
	
	
	// ======================================================
	// initSelectTree()
	//
	// Init-Phase: unten tab selectieren
	// ======================================================
	public void initSelectTree() {
		// prï¿½fen, ob neue Bilder in dieser Session aufgenommen wurden.
		// Wenn ja, dann neu selektieren
//		PM_Sequence neueBilder = PM_SequencesInout.getInstance().getNeueBilder();
//		if (neueBilder != null) {		 
//			PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
//			searchExpr.setSequenz(neueBilder);
//			luceneSuchen = new PM_Search(searchExpr);	
//			if (luceneSuchen.search() == 0) {
//				return;
//			}
//			setSortOrder(SearchSortType.TIME);		 
//			indexViewThumbnails.setAndPaintPictureList(getSortedPictureResultList());			
//			tabbedPaneRight.setSelectedComponent(scTreeNew);
//			return;
//		}
		// keine neuen Bilder
		if (treeWindowBase.getRowCount() > 1) {
			tabbedPaneRight.setSelectedComponent(treePanelBase);
		} else if (treeWindowExtended.getRowCount() > 1) {
			tabbedPaneRight.setSelectedComponent(treePanelExtended);
//		} else if (treeNeu.getRowCount() > 1) {
//			tabbedPaneRight.setSelectedComponent(scTreeNew);
//		} else if (treeAlbumRight.getRowCount() > 1) {
//			tabbedPaneRight.setSelectedComponent(albumRight);
		} else {
			tabbedPaneRight.setSelectedComponent(treePanelHardDisk);
		}		
	}

	// ======================================================
	// requestToChange()
	//
	// Aufruf beim Tab-Wechsel: Pruefen, ob Aktivitaeten abgeschlossen.
	// ======================================================
	@Override
	public boolean requestToChange() {
		return true;
	}

	// ======================================================
	// startDiashow()
	//
	// ï¿½ber Tastatur soll die Diashow gestartet werden.
	//  
	// Mode: DIASHOW_NORMAL
	// DIASHOW_AUTOM_SEQUENT
	// DIASHOW_AUTOM_RANDOM
	// ======================================================
	public void startDiashow(int mode) {
		if (anzahlHits == 0)
			return; // keine Bilder gefunden

		doHitsDiashowDarstellen(mode);

	}

 

	// ======================================================
	// getListDivPos()
	// getTocColPos()
	// ======================================================
	// public int getListDivPos() {
	// return splitListenPane.getDividerLocation();
	// }

	public int getListBreite() {
		return splitPaneBottom.getDividerLocation();
	}

	// =====================================================================
	// =====================================================================
	// =====================================================================
	// ============ Private ================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================
	// =====================================================================

	// ==========================================================
	// keyPressedKategorie()
	//
	// ï¿½ndern der Kategorie mit Ctrl+1, Ctrl+2 ....
	// ==========================================================
	private void keyPressedKategorie(int kategorie) {

		if (kategorie == 1)
			q1.setSelected(!q1.isSelected());
		if (kategorie == 2)
			q2.setSelected(!q2.isSelected());
		if (kategorie == 3)
			q3.setSelected(!q3.isSelected());
		if (kategorie == 4)
			q4.setSelected(!q4.isSelected());
		setQsEnable();
		doSuchen();
	}

	// =====================================================
	// fireAnzahlListener()
	//
	// Die Anzahl der Hits hat sich geaendert
	// =====================================================
	private void fireAnzahlListener() {
		for (int i = 0; i < changeAnzahlListener.size(); i++) {
			PM_Listener listener = (PM_Listener) changeAnzahlListener
					.elementAt(i);
			listener.actionPerformed(new PM_Action(this));
		}
	}

	// ======================================================
	// setBackgroundUpperPanel()
	//
	// 
	// ======================================================
	private void setBackgroundUpperPanel(Color color) {

		qPanel.setBackground(color);
		panelQualitaet.setBackground(color);
		panelIndex1.setBackground(color);
		panelIndex2.setBackground(color);
		panelDatum.setBackground(color);

		datPanel.setBackground(color);
		belowPanel.setBackground(color);

		comboBoxSequenzen.setBackground(COLOR_ENABLED);
 		index1.setBackgroundTextField(COLOR_ENABLED);
		index2.setBackgroundTextField(COLOR_ENABLED);
		vonJahr.setBackground(COLOR_ENABLED);
		vonMonat.setBackground(COLOR_ENABLED);
		vonTag.setBackground(COLOR_ENABLED);
		bisJahr.setBackground(COLOR_ENABLED);
		bisMonat.setBackground(COLOR_ENABLED);
		bisTag.setBackground(COLOR_ENABLED);
		buttonDarstellen.setBackground(COLOR_ENABLED);
		buttonDiaShow.setBackground(COLOR_ENABLED);
		q1.setBackground(COLOR_ENABLED);
		q2.setBackground(COLOR_ENABLED);
		q3.setBackground(COLOR_ENABLED);
		q4.setBackground(COLOR_ENABLED);
	}

	// ======================================================
	// Upper Panel generieren
	//
	// Achtung: hier wird die "focusListUpperPanel" aufbereitet !!!!!
	// ======================================================
	private JButton clearButtonIndex1 = null;
	private JButton clearButtonIndex2 = null;
	private JButton clearButtonQS = null;
	private JButton clearButtonSequ = null;
	private JButton clearButtonDatum = null;
	
	private JPanel panelIndex1 = null;
	private JPanel panelIndex2 = null;
	private JPanel panelQualitaet = null;
	private JPanel panelDatum = null;
	private JPanel qPanel = null;
	private JPanel datPanel = null;
	private JPanel belowPanel = null;

	// ======================================================
	// getListeGeschlSeqenzen()
	//
	// Fï¿½r die Combo-Box wird hier die Liste der geschl. Sequenzen geholt.
	// Sie wird hier gesichert um nachher den angeklickten Index zu suchen.
	// ======================================================
	private List<PM_Sequence> listeGeschlSequenzen = null; 
	private Vector<String> getListeGeschlSeqenzen() {
		
		PM_TreeModel base = PM_TreeModelBase.getInstance();
		PM_TreeModel extended = PM_TreeModelExtended.getInstance();
		listeGeschlSequenzen = base.getSequenceList();
		listeGeschlSequenzen.addAll(extended.getSequenceList());
		Collections.sort(listeGeschlSequenzen, PM_Utils.SORT_SEQUENZ_PATH);
		return PM_Utils.getComboBoxVector(listeGeschlSequenzen);
	}

	// ======================================================
	// doClearAlle() (alle Suchbegriffe loeschen)
	// ======================================================
	protected void doClearAlle() {
		doClearIndex1();
		doClearIndex2();
		doClearDatum();
		doClearQS();
		doClearSequ();

		clearButtonDatum.setBackground(COLOR_BG_PANEL); // dies ist ein Hack
		// !!!!

	}
 
	private void doClearIndex1() {
		index1.setText("");
		setEnableIndex1();
	}
 
	private void doClearIndex2() {
		index2.setText("");
		setEnableIndex2();	 
	}
 
	private void setEnableIndex1() {	
		String s = index1.getText();
		if (s == null || s.length() == 0) {
			clearButtonIndex1.setBackground(COLOR_BG_PANEL);
		} else {
			clearButtonIndex1.setBackground(Color.RED);
		}
	}
 
	private void setEnableIndex2() {	 
		String s = index2.getText();
		if (s == null || s.length() == 0) {
			clearButtonIndex2.setBackground(COLOR_BG_PANEL);
		} else {
			clearButtonIndex2.setBackground(Color.RED);
		}
	}

	// ======================================================
	// setQsEnable()
	// ======================================================
	private void setQsEnable() {
		boolean enabled = false;
		if (q1.isSelected())
			enabled = true;
		if (q2.isSelected())
			enabled = true;
		if (q3.isSelected())
			enabled = true;
		if (q4.isSelected())
			enabled = true;

		if (enabled) {
			clearButtonQS.setBackground(Color.RED);
		} else {
			clearButtonQS.setBackground(COLOR_BG_PANEL);
		}
	}

	// ======================================================
	// setDatumEnable()
	// ======================================================
	private void setDatumEnable() {
		boolean enabled = true;
		String vonJahrS = ((String) vonJahr.getSelectedItem()).trim();
		String vonMonatS = ((String) vonMonat.getSelectedItem()).trim();
		String vonTagS = ((String) vonTag.getSelectedItem()).trim();
		String bisJahrS = ((String) bisJahr.getSelectedItem()).trim();
		String bisMonatS = ((String) bisMonat.getSelectedItem()).trim();
		String bisTagS = ((String) bisTag.getSelectedItem()).trim();

		if (vonJahrS.length() == 0 && vonMonatS.length() == 0
				&& vonTagS.length() == 0 && bisJahrS.length() == 0
				&& bisMonatS.length() == 0 && bisTagS.length() == 0)
			enabled = false;

		if (enabled) {
			clearButtonDatum.setBackground(Color.RED);
		} else {
			clearButtonDatum.setBackground(COLOR_BG_PANEL);
		}

	}

	// ======================================================
	// setEnableClearButtons()
	// ======================================================
	private void setEnableClearButtons() {
		setDatumEnable();
		setQsEnable();
	}

	// ======================================================
	// doClearQS()
	// ======================================================
	private void doClearQS() {
		q1.setSelected(false);
		q2.setSelected(false);
		q3.setSelected(false);
		q4.setSelected(false);

		setQsEnable();
	}

	 
	private void doClearSequ() {
//		sortierung.setSelectedIndex(0);
		comboBoxSequenzen.setSelectedIndex(0); // erste Zeile in Liste
	}

 
	private void doClearHits() {
		doClearAlle();
		anzahlHits = 0;
		setAnzahlHits();
	}

	// ======================================================
	// setSortOrder();
	// getSortOrder();
	//
	// Die ComboBox wird gesetzt bzw. abgefragt
	// ======================================================
 	private SearchSortType sortOrder = SearchSortType.NOTHING;
	private void setSortOrder(SearchSortType sortOrder) {
		this.sortOrder = sortOrder;
		/*
		if (anzahlHits == 0) {
			sortOrder = SearchSortType.NOTHING;
//			sortierung.setSelectedIndex(0);
			return;
		}

		Iterator<SortOrder> it = sortOrder_deprecated.iterator();
		while (it.hasNext()) {
			SortOrder so = it.next();
			if (so.sortType == sortType) {
				sortierung.setSelectedItem(so);
				return;
			}
		}
		sortierung.setSelectedIndex(0);
		*/
	}

	private SearchSortType getSortOrder() {
		return sortOrder;
//		return ((SortOrder) sortierung.getSelectedItem()).sortType;
	}

	// ======================================================
	// doSuchen();
	//
	// Zum Suchen wird eine temporï¿½re offene Sequenz erzeugt.
	// Wird nach dem Suchen wieder zerstï¿½rt.
	// ======================================================
	private void doSuchen() {

		anzahlHits = 0;
		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);

		// es wird mit einer temporï¿½ren offenen Sequenz gesucht.
		// nach dem Suchen wird sie wieder zerstï¿½rt.
		searchExpr.setSequenz(getTempOpenSequenz());

		luceneSuchen = new PM_Search(searchExpr);
		anzahlHits = luceneSuchen.search();
		setAnzahlHits();

		if (anzahlHits == 0) {
			return;
		}

		// SortOrder seztzen
		if (comboBoxSequenzen.getSelectedIndex() > 0) {
			setSortOrder(SearchSortType.SEQ);
		} else {
			setSortOrder(SearchSortType.TIME);
		}

 
		
	}

	// ======================================================
	// doSuchenDir();
	//
	// Suchen Dir und new
	// ======================================================
	private void doSuchen(PM_Sequence seq, SearchSortType type) {

		PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
		searchExpr.setSequenz(seq);
		luceneSuchen = new PM_Search(searchExpr);
		anzahlHits = luceneSuchen.search();
		setAnzahlHits();

		if (anzahlHits == 0) {
			return;
		}
		
		setSortOrder(type);
	}

	// ======================================================
	// getTempOpenSequenz();
	//
	// Es wird zum Suchen eine temporï¿½re offene Sequenz
	// erzeugt. Nach dem Suchen wird sie wieder zerstï¿½rt.
	// ======================================================
	private PM_Sequence getTempOpenSequenz() {
		
	 
		
		PM_SequenceAlbum  sequenz = new PM_SequenceAlbum ( );
		
		setSequenzFromGUI(sequenz);
		return sequenz;
	}

	// ======================================================
	// getDatum()
	// ======================================================
	private String getDatum(String jjjj, String mm, String tt) {
		if (jjjj.length() != 4)
			return "";
		if (mm.length() != 2) {
			return jjjj;
		}
		if (tt.length() != 2) {
			return jjjj + "." + mm;
		}
		return jjjj + "." + mm + "." + tt;
	}

	// ======================================================
	// getSortedPictureResultList();
	//
	// returnt die (PM_Picture !!) -Liste der gefundenen Bilder
	// ======================================================
	private List<PM_Picture> getSortedPictureResultList() {
		if (luceneSuchen == null)
			return new ArrayList<PM_Picture>();

		return luceneSuchen.getPictureList(getSortOrder());

	}

	// =======================================================================
	// setAnzahlHits()
	//
	// Setzt ins Feld 'hits' die Anzahl der gefundenen Hits.
	// =======================================================================
	private void setAnzahlHits() {

		hitsUpperPanel.setText(Integer.toString(anzahlHits));
		if (anzahlHits == 0) {
			setSortOrder(SearchSortType.NOTHING);
			hitsUpperPanel.setBackground(COLOR_BG_PANEL);
			clearButtonHits.setBackground(COLOR_BG_PANEL);
			albumDown.setEnabled(false);
			neueSerie.setEnabled(false);
		} else {
			hitsUpperPanel.setBackground(Color.RED);
			clearButtonHits.setBackground(Color.RED);
			albumDown.setEnabled(true);
			neueSerie.setEnabled(true);
		}

		fireAnzahlListener();
	}

	// ======================================================
	// suchenGeaendert();
	//
	// Die Suchbegriffe wurden geï¿½ndert.
	// Damit stimmt das Suchergebnis nicht mehr
	// ======================================================
	private void suchenGeaendert() {
		doSuchen(); 		 
	}		
	
	// ======================================================
	// startDiashowFromTreeWindow();
	//
	//
	// ======================================================
	private void startDiashowFromTreeWindow(DefaultMutableTreeNode node) {
		List<PM_Picture> picList = getPictureList(node);
		if (picList.size() == 0) {
			return;
		}	 
		// Diashow aufrufen (von Anfang an)
		windowMain.doDiaShow(picList.get(0), picList, DIASHOW_NORMAL);
	}

	// ======================================================
	// doHitsDiashowDarstellen();
	//
	//
	// ======================================================
	private void doHitsDiashowDarstellen(int mode) {
		if (anzahlHits == 0)
			return;

		List<PM_Picture> list = getSortedPictureResultList();
		if (list.size() == 0) {
			return;
		}	
	 
		// Diashow aufrufen (von Anfang an)
		windowMain.doDiaShow(list.get(0), list, mode);
		
	}

	// ======================================================
	// doHitsLinksDarstellen();
	//
	// Alle hits links als Thumbnails darstellen
	// ======================================================
	public void doHitsLinksDarstellen() {
		if (anzahlHits == 0) {
			return;	 
		}		
		indexViewThumbnails.data.clearAndAdd(getSortedPictureResultList());
	}

	// ======================================================
	// doClearDatum();
	//
	//
	// ======================================================
	private void doClearDatum() {
		vonJahr.getModel().setSelectedItem("");
		vonMonat.getModel().setSelectedItem("");
		vonTag.getModel().setSelectedItem("");

		bisJahr.getModel().setSelectedItem("");
		bisMonat.getModel().setSelectedItem("");
		bisTag.getModel().setSelectedItem("");

	}

	// ======================================================
	// setSequenzFromGUI(PM_Sequenz);
	//
	// Alle Felder von der GUI in die Sequenz ï¿½bertragen
	// ======================================================

	private void setSequenzFromGUI(PM_SequenceAlbum  sequenz) {
 		
		sequenz.setIndex(index1.getText());
		if (addOnIndex2) {
			sequenz.setOrt(index2.getText());
		}

		// -------------- von ---------------------------------------------
		String vonJahrS = ((String) vonJahr.getSelectedItem()).trim();
		String vonMonatS = ((String) vonMonat.getSelectedItem()).trim();
		String vonTagS = ((String) vonTag.getSelectedItem()).trim();
		sequenz.setVon(getDatum(vonJahrS, vonMonatS, vonTagS));
		 

		// -------------- bis ---------------------------------------------
		String bisJahrS = ((String) bisJahr.getSelectedItem()).trim();
		String bisMonatS = ((String) bisMonat.getSelectedItem()).trim();
		String bisTagS = ((String) bisTag.getSelectedItem()).trim();
		sequenz.setBis(getDatum(bisJahrS, bisMonatS, bisTagS));

		// Qualitaet
		String qString = "";
		if (q1.isSelected())
			qString += "1";
		if (q2.isSelected())
			qString += "2";
		if (q3.isSelected())
			qString += "3";
		if (q4.isSelected())
			qString += "4";
		sequenz.setQual(qString);

		// ---------------------------------------------------
		// Sequenz
		// ---------------------------------------------------
		int ind = comboBoxSequenzen.getSelectedIndex() - 1; // -1, da erste
		// Zeile leer
		PM_Sequence s = PM_Utils.getSelectedSequenz(ind,
				listeGeschlSequenzen);
		if (s instanceof PM_Sequence) {
			PM_Sequence seqClosed = (PM_Sequence) s;
			sequenz.setSeqClosed(seqClosed);
		} else {
			sequenz.setSeqClosed(null);
		}
	}


	// ======================================================
	// darstellungSequenz(PM_Sequenz);
	//
	// Es wurde eine Sequenz in irgendeinem Baum selektiert.
	// Die Bilder suchen und oben das Ergebnis anzeigen.
	// Alle Suchfelder fï¿½llen.
	// ======================================================
	@Override
	public void darstellungSequenz(PM_Sequence sequenz, DefaultMutableTreeNode node) {

		doClearAlle();
		anzahlHits = 0;
		setAnzahlHits();

		// Wenn Sequenz == null, dann alle Anzeigen lï¿½schen
		if (sequenz == null) {	
			return; 
		}
		
		switch (sequenz.getType()) {
		case ORIGINAL:
			doSuchen(sequenz, SearchSortType.FILE_PATH);
			return;
		case BASE:
		case EXTENDED:
			comboBoxSequenzen.setSelectedIndex(PM_Utils.getSelectedIndex(
					sequenz, listeGeschlSequenzen));
			setEnableClearButtons();
			doSuchen();
			return;
		case NEW:
			doSuchen(sequenz, SearchSortType.TIME);
			return;
		case ALBUM:
			darstellungAlbum(sequenz);
			setEnableClearButtons();
			doSuchen();
			return;
		}
			 
		 
	}

	// ======================================================
	// aendernAlbum();
	//
	// Es wurde eine Sequenz im Album selektiert (sequenz).
	// Diese jetzt mit den GUI-Werten ï¿½ndern.
	// ======================================================
	public void aendernAlbum(PM_SequenceAlbum seq, DefaultMutableTreeNode node, PM_TreeWindow  tw) {
		String seqName = seq.getShortName();
		
System.out.println("PM_WindowSeqrch: shortName: " + seqName);		
		
		
		PM_SequenceAlbum seqGUI = new PM_SequenceAlbum();
		setSequenzFromGUI(seqGUI);
		seqGUI.setLongName(seq.getLongName());
		seqGUI.setShortName(seq.getShortName());
		if (seq.getSeqClosed() != seqGUI.getSeqClosed()) {
			String text = "Sequenzen ungleich. Änderung durchführen?";
			int n = JOptionPane.showConfirmDialog(null, text,
					"Sicherheitsabfrage", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.NO_OPTION) {
				return;
			}
			// set new shortName
			String shortName = SEQ_CHARACTER_ALBUM + PM_Sequence.getNextFreeAlbumSequenceNumber();				
			seqGUI.setShortName(shortName);
		}
		// Sequence ï¿½ndern
		node.setUserObject(seqGUI);
		tw.getTreeModel().nodeChanged(node);
		hitsAlbumTree.setText(hitsUpperPanel.getText());
		albumUp.setEnabled(false);
	}
	
	 
	private void darstellungAlbum(PM_Sequence seq ) {
		
		
		if (! (seq instanceof PM_SequenceAlbum)) {
			return; // Fehler
		}
		
		PM_SequenceAlbum sequenz = (PM_SequenceAlbum)seq;
		
		PM_Sequence seqClosed = sequenz.getSeqClosed();
		if (seqClosed != null) {
			comboBoxSequenzen.setSelectedIndex(PM_Utils.getSelectedIndex(
					seqClosed, listeGeschlSequenzen));
		} else {
			setSortOrder(SearchSortType.NOTHING);
		}

		// jetzt alles in die GUI-Suchfelder uebertragen
		String von = sequenz.getVon();
		String bis = sequenz.getBis();

		vonJahr.getModel().setSelectedItem(getJJ(von));
		vonMonat.getModel().setSelectedItem(getMM(von));
		vonTag.getModel().setSelectedItem(getTT(von));

		bisJahr.getModel().setSelectedItem(getJJ(bis));
		bisMonat.getModel().setSelectedItem(getMM(bis));
		bisTag.getModel().setSelectedItem(getTT(bis));

		index1.setText(sequenz.getIndex());
		setEnableIndex1();

		index2.setText(sequenz.getOrt());
		setEnableIndex2();

		// QS (COL_QS)
		setQS(sequenz.getQual());

	 
	}
	
	
	
	private String getJJ(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 1)
			return s[0];
		return "";
	}

	private String getMM(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 2)
			return s[1];
		return "";
	}

	private String getTT(String datum) {
		String[] s = datum.split(SPLIT_PUNKT);
		if (s.length >= 3)
			return s[2];
		return "";
	}

	/**
	 * setQS()
	 */
	private void setQS(String qs) {
		if (qs.indexOf("1") >= 0)
			q1.setSelected(true);
		if (qs.indexOf("2") >= 0)
			q2.setSelected(true);
		if (qs.indexOf("3") >= 0)
			q3.setSelected(true);
		if (qs.indexOf("4") >= 0)
			q4.setSelected(true);

		setQsEnable();
	}

	// ============================================================
	// ===================================private DefaultMutableTreeNode
	// rootNode = null;=========================
	// InnerClass: SortOrder
	// ============================================================
	// ============================================================

	class SortOrder {

		public SearchSortType sortType;
		public String text;

		// =====================================================
		// Konstruktor
		// =====================================================
		public SortOrder(SearchSortType sortType, String text) {
			this.sortType = sortType;
			this.text = text;

		}

		public String toString() {
			return text;
		}

	} // inner Class

	// =================================================================
	// getSearchPanel()
	//
	// Panel zur Eingabe der Suchbegriffe.
	// (Das Ergebnis wird oberhalb vom SearchPanel angezeigt
	// =================================================================
	
	 
	
	
	private JLabel datLabelVon;
	private JLabel datLabelTo;
	private JLabel qLabel;
	private JLabel sLabel;
	private PM_FocusPanel getSearchPanel() {

		int columsIndexComboBox = 20;
		PM_FocusPanel panel = new PM_FocusPanel() {

			public void setBackgroundColor(Color color) {
				setBackgroundUpperPanel(color);
			}
		};
		panel.setAlignmentX(0);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panelIndex1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelIndex2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelQualitaet = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelDatum = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// ---------------------------------------------------
		// Popup-Menu-Listener um ein Suchen auszulï¿½sen
		// Fï¿½r Combo-box
		// ---------------------------------------------------
		PopupMenuListener pl = new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				suchenGeaendert();
				setEnableClearButtons();
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		};

		// ---------------------------------------------------
		// Mouse Listener um ein Suchen auszulï¿½sen
		// ---------------------------------------------------

		MouseListener ml = new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				suchenGeaendert();
				setEnableClearButtons();
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		};

		// ---------------------------------------------------
		// Index
		// ---------------------------------------------------

		// ----- clear-Button -------------
		clearButtonIndex1 = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonIndex = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearIndex1();
				suchenGeaendert();
			}
		};
		clearButtonIndex1.addActionListener(alClearButtonIndex);
		panelIndex1.add(clearButtonIndex1);

		// Index
		JLabel indexLabel = new JLabel("Index 1:");
		panelIndex1.add(indexLabel);
		index1 = new PM_IndicesComboBox(IndexType.INDEX_1);
		index1.setColumns(columsIndexComboBox);
	 
		 
		
		index1.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				setEnableIndex1();
				suchenGeaendert();
			}
		});
 
		// index1.setColumns(39);
		panelIndex1.add(index1);
 

		// ---------------------------------------------------
		// Ort
		// ---------------------------------------------------

		// ----- clear-Button -------------
		clearButtonIndex2 = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonOrt = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearIndex2();
				suchenGeaendert();
			}
		};
		clearButtonIndex2.addActionListener(alClearButtonOrt);
		panelIndex2.add(clearButtonIndex2);

		// Index
		JLabel ortLabel = new JLabel("Index 2:");
		panelIndex2.add(ortLabel);
		index2 = new PM_IndicesComboBox(IndexType.INDEX_2);
		index2.setColumns(columsIndexComboBox);
		index2.addChangeListener(new PM_Listener() {
			public void actionPerformed(PM_Action e) {
				setEnableIndex2();
				suchenGeaendert();
			}
		});

		// indexNew2.setColumns(39);
		panelIndex2.add(index2);
	 
		// ---------------------------------------------------
		// Qualitaet
		// ---------------------------------------------------
		// ----- clear-Button -------------
		clearButtonQS = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonQS = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearQS();
				suchenGeaendert();
			}
		};
		clearButtonQS.addActionListener(alClearButtonQS);
		panelQualitaet.add(clearButtonQS);

		qPanel = new JPanel();

		qLabel = new JLabel("Kategorie 1..4:");
		qPanel.add(qLabel);

		q1 = new JCheckBox();
		q2 = new JCheckBox();
		q3 = new JCheckBox();
		q4 = new JCheckBox();

		qPanel.add(q1);
		qPanel.add(q2);
		qPanel.add(q3);
		qPanel.add(q4);

		panelQualitaet.add(qPanel);

		q1.addMouseListener(ml);
		q2.addMouseListener(ml);
		q3.addMouseListener(ml);
		q4.addMouseListener(ml);
		/**
		 * ChangeListener ch = new ChangeListener() { public void
		 * stateChanged(ChangeEvent ce){ setQsEnable(); suchenGeaendert(); } };
		 * q1.addChangeListener(ch ); q2.addChangeListener(ch );
		 * q3.addChangeListener(ch ); q4.addChangeListener(ch );
		 */

		// --------------------------------------------------
		// Combo-box offene Sequenzen
		// -------------------------------------------------
		// ----- clear-Button Sequenz -------------
		clearButtonSequ = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonSequ = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearSequ();
				suchenGeaendert();
			}
		};
		clearButtonSequ.addActionListener(alClearButtonSequ);
		panelQualitaet.add(clearButtonSequ);
		sLabel = new JLabel("Serien:");
		panelQualitaet.add(sLabel);

		
		comboBoxSequenzen = new JComboBox(getListeGeschlSeqenzen().toArray());
		comboBoxSequenzen.addPopupMenuListener(pl);

		
		PM_Listener l = new PM_Listener() {
			public void actionPerformed(PM_Action e) {
// 
				updateComboBoxSequences();
			}
		};
		PM_TreeModelBase.getInstance().addChangeListener(l); 
		PM_TreeModelExtended.getInstance().addChangeListener(l); 

 		
		ActionListener alGeschlSequenz = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBoxSequenzen.getSelectedIndex() == 0) {
					clearButtonSequ.setBackground(COLOR_BG_PANEL);
				} else {
					clearButtonSequ.setBackground(Color.RED);
				}
			}
		};
		comboBoxSequenzen.addActionListener(alGeschlSequenz);
		panelQualitaet.add(comboBoxSequenzen);

		// ---------------------------------------------------
		// Datum
		// ---------------------------------------------------
		datPanel = new JPanel();

		// ----- clear-Button -------------
		clearButtonDatum = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonDatum = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearDatum();
				setEnableClearButtons();
				suchenGeaendert();
			}
		};
		clearButtonDatum.addActionListener(alClearButtonDatum);
		panelDatum.add(clearButtonDatum);

		// ------- Label "von:" -----------
		datLabelVon = new JLabel("%Datum von%");
		datPanel.add(datLabelVon);
		// ---- von Jahr -------------
		vonJahr = new JComboBox(PM_Utils.getJahre(true));

		vonJahr.addPopupMenuListener(pl);
		datPanel.add(vonJahr);

		vonMonat = new JComboBox(PM_Utils.getMonate(true));

		vonMonat.addPopupMenuListener(pl);
		datPanel.add(vonMonat);
		vonTag = new JComboBox(PM_Utils.getTage(true));

		vonTag.addPopupMenuListener(pl);
		datPanel.add(vonTag);

		datLabelTo = new JLabel(" bis ");
		datPanel.add(datLabelTo);

		bisJahr = new JComboBox(PM_Utils.getJahre(true));

		bisJahr.addPopupMenuListener(pl);
		datPanel.add(bisJahr);
		bisMonat = new JComboBox(PM_Utils.getMonate(true));

		bisMonat.addPopupMenuListener(pl);
		datPanel.add(bisMonat);
		bisTag = new JComboBox(PM_Utils.getTage(true));
		//    
		bisTag.addPopupMenuListener(pl);
		datPanel.add(bisTag);

		panelDatum.add(datPanel);

		// ---------------------------------------------------
		// Jetzt sind alles Einzelpanels (= Zeilen) aufbereitet)
		// ---------------------------------------------------
		belowPanel = getSearchResultPanel();

		JScrollPane scBelowPanel = new JScrollPane(belowPanel);
		scBelowPanel 
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scBelowPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		panel.add(scBelowPanel);
		panel.add(panelDatum);
		panel.add(panelQualitaet);
		panel.add(panelIndex1);
		if (addOnIndex2) {
			panel.add(panelIndex2);
		}
		
		
	 
//		panel.add(scBelowPanel);

		// --------------------------------
		// Focus Liste aufbereiten
		// ---------------------------------
		List<Component> zeile1 = new ArrayList<Component>();
		zeile1.add(clearButtonDatum);
		zeile1.add(vonJahr);
		zeile1.add(vonMonat);
		zeile1.add(vonTag);
		zeile1.add(bisJahr);
		zeile1.add(bisMonat);
		zeile1.add(bisTag);
		List<Component> zeile2 = new ArrayList<Component>();
		zeile2.add(clearButtonQS);
		zeile2.add(q1);
		zeile2.add(q2);
		zeile2.add(q3);
		zeile2.add(q4);
		zeile2.add(clearButtonSequ);
		zeile2.add(comboBoxSequenzen);
		List<Component> zeile3 = new ArrayList<Component>();
		zeile3.add(clearButtonIndex1);
		zeile3.add(index1);
		List<Component> zeile4 = new ArrayList<Component>();
		zeile4.add(clearButtonIndex2);
		zeile4.add(index2);
		List<Component> zeile5 = new ArrayList<Component>();
		zeile5.add(buttonDarstellen);
		zeile5.add(buttonDiaShow);

		List<List> focusList = new ArrayList<List>();
		focusList.add(zeile1);
		focusList.add(zeile2);
		focusList.add(zeile3);
		focusList.add(zeile4);
		focusList.add(zeile5);

		panel.setFocusListe(focusList);
		panel.setLastFocus(index1); // default
		panel.setContainer(panel);

		return panel;
	}

	// ======================================================
	// updateComboBoxSequences()
	// ======================================================
	private void updateComboBoxSequences() {
		comboBoxSequenzen.removeAllItems();
		List<String> v = getListeGeschlSeqenzen();
		Iterator<String> it = v.iterator();
		while (it.hasNext()) {
			comboBoxSequenzen.addItem(it.next());
		}
	}
 
	// ======================================================
	// getSearchResultPanel()
	//
	// ganz oben das Ergebnis der Suche
	// ======================================================
	private JButton neueSerie;
	private JLabel labelNew;
	private JLabel labelDnD;
	private JPanel getSearchResultPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 40));
		// panel.setBackground(COLOR_BACKGROUND);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		
		// Button "< links darstellen"
		buttonDarstellen = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(buttonDarstellen);
		ActionListener alButtonDarstellen = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// links darsstellen
				if (anzahlHits == 0) {
					return;	 
				}		
				indexViewThumbnails.data.clearAndAdd(getSortedPictureResultList());
			}
		};
		buttonDarstellen.addActionListener(alButtonDarstellen);
		
		// Button "<< links anï¿½ngen"
		buttonAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(buttonAppend);
		ActionListener alButtonAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// links darsstellen
				if (anzahlHits == 0) {
					return;	 
				}		
				indexViewThumbnails.data.appendPictureList(getSortedPictureResultList());
			}
		};
		buttonAppend.addActionListener(alButtonAppend);


		// Hits
//		JLabel hitsLabel = new JLabel("Treffer");
//		panel.add(hitsLabel);
		hitsUpperPanel = new JTextField("");
		// hits.setBackground(COLOR_BACKGROUND);
		hitsUpperPanel.setForeground(Color.BLACK);
		hitsUpperPanel.setEnabled(true);
		hitsUpperPanel.setEditable(false);
		hitsUpperPanel.setFocusable(false);
 		hitsUpperPanel.setColumns(4);
		panel.add(hitsUpperPanel);
		Font font = hitsUpperPanel.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsUpperPanel.setFont(fontBold);

		// Lï¿½schen
		clearButtonHits = PM_Utils.getJButon(ICON_DELETE);

		ActionListener alClearButtonHits = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClearHits();
			}
		};
		clearButtonHits.addActionListener(alClearButtonHits);
		panel.add(clearButtonHits);

		// Button "Diashow"
		buttonDiaShow = new JButton("?Diashow");
		panel.add(buttonDiaShow);
		ActionListener alButtonDiaShow = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doHitsDiashowDarstellen(DIASHOW_NORMAL);
			}
		};
		buttonDiaShow.addActionListener(alButtonDiaShow);

	 

		// neuer Albumeientrag
	    neueSerie = PM_Utils.getJButon(ICON_NEW);
	    neueSerie.setEnabled(false);
		// Button "neue Sequenz"
	    labelNew = new JLabel("Neu: ");
		panel.add(labelNew);
		panel.add(neueSerie);
 		new DragSourceNew(neueSerie);
 		labelDnD = new JLabel("(mit Drag & Drop ins Fotoalbum)");
		panel.add(labelDnD);

		return panel;
	}

	 
 
 
	
	
	
	
	// =====================================================================================
	// Inner Class: DragSourceNew
	//
	//  DragSource fï¿½r die Neuerstellung von Serien.
	//  Neue Serien werden NUR mit Drag & Drop erzeugt.
	//
	//  Es wird einmalig fï¿½r beiden Serien-Typen (B und X) je eine
	//  Dragsource - Instanz erzeugt. Sie werden nicht vernichtet.
	//
	//
	// =====================================================================================
	class DragSourceNew implements DragSourceListener, DragGestureListener {

		private DragSource dragSource;

		// =====================================================
		// Konstruktor
		// =====================================================
		public DragSourceNew(Component source) {
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(source,
					DnDConstants.ACTION_COPY_OR_MOVE, this);
		}

		// ==================================================================
		// Methoden fï¿½r DragSourceListener
		// ==================================================================

		public void dragDropEnd(DragSourceDropEvent dsde) {
 
		}

		public void dragEnter(DragSourceDragEvent evt) {
			DragSourceContext dsx = evt.getDragSourceContext();
			dsx.setCursor(DragSource.DefaultMoveDrop);
		}

		public void dragExit(DragSourceEvent evt) {
			DragSourceContext dsx = evt.getDragSourceContext();
			dsx.setCursor(DragSource.DefaultMoveNoDrop);
		}

		public void dragOver(DragSourceDragEvent arg0) {
		}

		public void dropActionChanged(DragSourceDragEvent arg0) {
		}

		// ====================================================================
		// Methode fï¿½r DragSourceListener
		// ====================================================================
		public void dragGestureRecognized(DragGestureEvent dge) {

	//		System.out.println("dragGestureRecognized aufgerufen");
			Component c = dge.getComponent();
			if (!(c instanceof JButton)) {
				return;
			}
			JButton b = (JButton) c;
			if (!(b.isEnabled())) {
				return;
			}
			PM_Sequence newSequence = getSequenceNew();
			if (newSequence == null) {
				 return;
			}

			// Jetzt kann Drag&Drop eingeleitet werden				
			List<Object> list = new ArrayList<Object>();
			list.add(DragAndDropType.NEW_SEQUENCE_ALBUM);
			list.add(newSequence);

			Transferable transferable = new PM_Transferable(list);
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop,
					transferable, this);

		}
	}

	// ======================================================
	// dragDropDone()
	//
	// Die Sequence wurde ordnungsgemï¿½ï¿½ mit Drag And Drop
	// angelegt. Hier jetzt der Drop-Abschluss
	// ======================================================	 
	private PM_Sequence getSequenceNew() {
		
		PM_SequenceAlbum  sequenz = new PM_SequenceAlbum ( );
		setSequenzFromGUI(sequenz);
		return sequenz;
	}

}
