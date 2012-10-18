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

 
import pm.index.PM_Index;
import pm.picture.*;
 
 
import pm.search.*;
 
import pm.sequence.*;
import pm.utilities.*;
 
 
 

import java.awt.*;
import java.awt.event.*;
import java.io.File;
 
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
 

/**
 * create GUI for PM_WindowImport.
 * 
 *  
 *
 */
@SuppressWarnings("serial")
public class PM_WindowImportGUI implements PM_Interface {

	final protected PM_Index index;
 
	
	private PM_WindowImport master;
	 
	private Border border;
	protected PM_TreeWindow  treeWindowNew;
 
	private JComponent component;
	private TreeSelectionListener treeSelectionListener;
	private PM_WindowMain windowMain;
	
	protected JButton importButtonExternal;
	protected JButton importButtonInternal;
	private boolean externalIsEnable = false;
	
	public PM_WindowImportGUI(PM_WindowImport master, PM_WindowMain windowMain) {
		this.master = master;
		this.windowMain = windowMain;
		 
		border = BorderFactory.createLineBorder(Color.GRAY, 3);

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
				 			
				TreePath tp = e.getPath();
				Object oo = tp.getLastPathComponent();
				
				if (oo instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode tn = (DefaultMutableTreeNode) oo;
					selectSequence(tn, tw);
				}
			}
		};
		
		// the indexView for the new pictures
		index = PM_Index.createIndexImport(master);
		index.init(master);
		
		init();
		 	
		// --------------------------------------------------------
		// Change Listener f�r message
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
	
	protected void init() {
		// ------  left side --------------
		JScrollPane sc = new JScrollPane(getNew());	 
		JComponent left = new JPanel(new BorderLayout());
		left.add(getImportExternalPanel(), BorderLayout.NORTH);
		left.add(getImportInternalPanel(), BorderLayout.CENTER);
		left.add(sc, BorderLayout.SOUTH);

		// -------- right side (an indexView) ---------------
		JPanel right = index.getIndexPanel();
		
		
		component = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(left),
				right);
		
		
	}
	
	
	private void setMsg() {
		newDiashow.setText(PM_MSG.getMsg("diashow"));
		
		textExternal.setText(PM_MSG.getMsg("winImpPicsFromExtern"));
		importButtonExternal.setText(PM_MSG.getMsg("winImpButtFromExtern"));
		titleExtern.setTitle(PM_MSG.getMsg("winImpTitleExtern"));
		
		textInternal.setText(PM_MSG.getMsg("winImpPicsFromIntern"));
		importButtonInternal.setText(PM_MSG.getMsg("winImpButtFromIntern"));
		titleIntern.setTitle(PM_MSG.getMsg("winImpTitleIntern"));
		
	 
		
	}
	

	
	/**
	 * An import taking place.
	 * 
	 * While there is import, the import buttons set to disable.
	 */
	public void importTakePlace(boolean takePlace) {
		if (takePlace) {
			externalIsEnable = importButtonExternal.isEnabled();
			importButtonInternal.setEnabled(false);
			importButtonExternal.setEnabled(false);
		} else {
			importButtonInternal.setEnabled(true);
			importButtonExternal.setEnabled(externalIsEnable);
		} 
	}
	
	protected PM_Index getIndex() {
		return index;
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
 	
		int anzahl = 0;
		if (sequenz != null) {
			PM_SearchExpr searchExpr = new PM_SearchExpr(SearchType.SEQ);
			searchExpr.setSequenz(sequenz);
			PM_Search luceneSuchen = new PM_Search(searchExpr);
			anzahl = luceneSuchen.search();			
		} 
		String anz = String.valueOf(anzahl);
		// Hits anzeigen
		if (tw == treeWindowNew) {
			hitsNewTree.setText(anz);
		}
		
	 
		
// System.out.println("Anzahl gefunden = " + anzahl);
//		if (sequenz != null) {
//			darstellungSequenz(sequenz, null);
//		???	markAlbum(tw, sequenz);
//		} else {
//			darstellungSequenz(null, null);
//		????	markAlbum(tw, null);
//		}
 		 
 
 		  
	}
	

	
	// =====================================================
	// getNew()
	// =====================================================
	private JTextField hitsNewTree; 
	private JButton newDiashow;
	private JPanel getNew() {
		// oben
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		 

		// Button "< links darstellen"
		JButton newDisplay = PM_Utils.getJButon(ICON_1_LEFT);
		panel.add(newDisplay);
		ActionListener alNewDisplay = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = 
					getPictureList(treeWindowNew.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				windowMain.getIndexViewThumbnails().data.clearAndAdd(picList);
			}
		};
		newDisplay.addActionListener(alNewDisplay);
		
		// Button "<< links darstellen"
		JButton newAppend = PM_Utils.getJButon(ICON_2_LEFT);
		panel.add(newAppend);
		ActionListener alNewAppend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PM_Picture> picList = 
					getPictureList(treeWindowNew.getSelectedNode());
				if (picList.size() == 0) {
					return;
				}
				windowMain.getIndexViewThumbnails().data.appendPictureList(picList);
			}
		};
		newAppend.addActionListener(alNewAppend);
			
		// Hits
		hitsNewTree = new JTextField("");
		hitsNewTree.setForeground(Color.BLACK);
		hitsNewTree.setEditable(false);
		hitsNewTree.setFocusable(false);
		hitsNewTree.setColumns(4);
		panel.add(hitsNewTree);
		Font font = hitsNewTree.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
		hitsNewTree.setFont(fontBold);					

		// Button "Diashow"
	    newDiashow = new JButton("?Diashow?");		 
		panel.add(newDiashow);
		ActionListener alNewDia = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDiashowFromTreeWindow(treeWindowNew.getSelectedNode());
			}
		};
		newDiashow.addActionListener(alNewDia);
		 
		JPanel upper = new JPanel();
		upper.setAlignmentX(0);
		upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));	
		upper.add( panel  ); 
  		
		JScrollPane spUpper = new JScrollPane(upper);
		spUpper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spUpper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		 			
		// --- Tree -----		
		treeWindowNew = new PM_TreeWindowNew(master);
		treeWindowNew.addTreeSelectionListener(treeSelectionListener);
		JScrollPane sc = new JScrollPane(treeWindowNew);		
				 				
		// Zusammensetzen		 
 		JPanel p = new JPanel(new BorderLayout());
 		p.add(spUpper,  BorderLayout.NORTH);
 		p.add( sc, BorderLayout.CENTER  );
 		
		return p;		 
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
	// getPictureList( );
	// ======================================================
	public List<PM_Picture> getPictureList(DefaultMutableTreeNode tn) {

		if (tn == null) {
			return new ArrayList<PM_Picture>();
		}
		PM_Sequence sequenz = null;
		Object s = tn.getUserObject();
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
		
		return luceneSuchen.getPictureList(SearchSortType.SEQ);
	}
	/**
	 * 
	 */
	public JComponent getComponent() {
		return component;
	}

	/**
	 * build the tree with Image files from import directory.
	 * 
	 * Clear the tree and build it new.
	 */
	public int buildPictureTree(File imageDir) {
		int count = 0;
//		count = treeModelPictures.buildPictureTree(imageDir);
		treeWindowNew.expandTree(-1);
		return count;
	}

	/**
	 * import finish.
	 * 
	 * clear the import data
	 */
	public void importDone() {
		
//		importButtonExternal.setText(PM_MSG.getMsg("winImpNoPictures"));
		importButtonExternal.setEnabled(false);
		externalIsEnable = importButtonExternal.isEnabled();
		importDirField.setText("");

//		treeModelPictures.clear();

	}

	private final Color colorRechts = Color.YELLOW;
	
	
	// ======================================================
	// getImportExternalPanel()
	// ======================================================
	
	private JTextField importDirField;
 
	private JTextArea textExternal;
	
	private TitledBorder titleExtern;

	private JPanel getImportExternalPanel() {
       
		// Beschreibung
		textExternal = new JTextArea();
 		String tEx = "Importieren von Bildern ausserhalb des Bilder-Verzeichnisses.";
		textExternal.setText(tEx);
	  
		// -------- Import button -------------------------------
		importButtonExternal = new JButton("Importieren extern");
		importButtonExternal.setEnabled(false);
		ActionListener alImportButt = new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				importButtonExternal.setEnabled(false);
				master.doImportExternal( );
				importButtonExternal.setEnabled(false);
			}
		};
		importButtonExternal.addActionListener(alImportButt);

		/*
		// ------  Target-Field ---------------------------------
		JPanel target = new JPanel(new FlowLayout(FlowLayout.LEFT));
		target.setBackground(colorRechts);
		target.add(new JLabel("Ziel-Verzeichnis:    "));
		targetDirField = new JTextField(getDefaultTargetDir().getPath());
		targetDirField.setEnabled(false);
		targetDirField.setColumns(30);
		target.add(targetDirField);
		JButton targetBrowserButton = new JButton("...");
		ActionListener alTargetDir = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = chooseImportDir(targetDirField.getText());
				if (!PM_Utils.isUnderTLD(file)) {
					String text = "Ziel-Verzeichnis ist NICHT unterhalb des TLD's";
					JOptionPane.showConfirmDialog(null, text, "ERROR",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				targetDirField.setText(file.getAbsolutePath());
			}
		};
		targetBrowserButton.addActionListener(alTargetDir);

		//		browserButton.setEnabled(false);
		target.add(targetBrowserButton);

*/

		JPanel panel = new JPanel();
		panel.setBackground(colorRechts);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);
		panel.add(textExternal);
//		panel.add(target);
		panel.add(importButtonExternal);

		titleExtern = BorderFactory
				.createTitledBorder(border, "Importieren Bilder von extern");
		panel.setBorder(titleExtern);

		JPanel out = new JPanel(new FlowLayout(FlowLayout.LEFT));
		out.setBackground(colorRechts);
		out.add(panel);
		return out;
	}
 
	
	private JTextArea textInternal;
	private TitledBorder titleIntern;
	private JPanel getImportInternalPanel() {
       
		// Beschreibung
		textInternal = new JTextArea();
		String tEx = "Importieren von Bildern innerhalb des Bilder-Verzeichnisses.";
		textInternal.setText(tEx);
		
		// -------- Import button -------------------------------
		importButtonInternal = new JButton("Importieren intern");
		 
		ActionListener alImportButt = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				

				PM_ImportInternal iInternal = new PM_ImportInternal( );
				iInternal.checkAndImport();
				
				
//				PM_WindowDialog_deprecated.getInstance().importInternalPictures(newPictures,
//						  null);
			}
		};
		importButtonInternal.addActionListener(alImportButt);

	 



		JPanel panel = new JPanel();
		panel.setBackground(colorRechts);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentY(0);
		panel.add(textInternal);
		 
		panel.add(importButtonInternal);

		titleIntern = BorderFactory
				.createTitledBorder(border, "Importieren Bilder von intern");
		panel.setBorder(titleIntern);

		JPanel out = new JPanel(new FlowLayout(FlowLayout.LEFT));
		out.setBackground(colorRechts);
		out.add(panel);
		return out;
	}
 

  

	 
 
	
	
	
	
	
	
	
	
	
	
	
}
