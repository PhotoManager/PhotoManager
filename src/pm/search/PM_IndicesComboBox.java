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
package pm.search;
 
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;

import pm.gui.*;
import pm.utilities.*;

@SuppressWarnings("serial")
public class PM_IndicesComboBox extends JComboBox implements PM_Interface {

	private PM_WindowMain windowMain;
	private List<PM_Listener> changeListener = new ArrayList<PM_Listener>();
	 
	private String cbText = "";
	private ListComboBoxModel model;
	private ListBasicComboBoxEditor comboBoxEditor;
	private JTextField textField = new JTextField();
	protected final IndexType indexType;
	public PM_IndicesComboBox(IndexType it) {
		indexType = it;
		windowMain = PM_WindowMain.getInstance();
		setMaximumRowCount(10);
		model = new ListComboBoxModel(indexType);
		setModel(model);
		setEditable(true);
		setRenderer(new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				// System.out.println("----renderer -----index = " + index);
				setText(value.toString());
				return this;
			}
		});

		// ---- KeyListener --------
		final KeyListener keylIndex = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				windowMain.keyTextFieldPressed(e);
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					return;
				}
				cbText = textField.getText();
//System.out.println("key Listener: " + e);
				if (e.getKeyChar() == 'x') {
//					setPopupVisible(true);
				}
				

				fireChangeListener();
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}
		};
		comboBoxEditor = new ListBasicComboBoxEditor();
		setEditor(comboBoxEditor);
		textField = comboBoxEditor.getTextField();
		if (comboBoxEditor != null) {
			textField.addKeyListener(keylIndex);
		}
		
		
		// The indices have changed
		PM_Listener indexListener = new PM_Listener() {
			public void actionPerformed(PM_Action ee) {
				if (ee.getObject() instanceof IndexType && indexType == (IndexType)ee.getObject()) {
					model.actionPerformed(new ActionEvent(this, 0, "update"));
				}		
			}
		};
		PM_LuceneLists.getInstance().addListener(indexListener);				 		
	}

	public void setBackgroundTextField(Color color) {
		JTextField textField = comboBoxEditor.getTextField();
		if (textField != null) {
			textField.setBackground(color);
		}
	}

	public String getText() {
		return textField.getText();
	}

	public void setText(String txt) {
		cbText = "";
		textField.setText(txt.trim());
		selectedItemReminder = null;
	}
 
	public void addChangeListener(PM_Listener listener) {
		if (!changeListener.contains(listener))
			changeListener.add(listener);
	}

	private void fireChangeListener() {
		for (PM_Listener l : changeListener) {
			l.actionPerformed(new PM_Action(getEditor().getItem()));
		}
	}

	public void setColumns(int columns) {
		JTextField textField = comboBoxEditor.getTextField();
		if (textField != null) {
			textField.setColumns(columns);
		}
	}

	
	class ListComboBoxModel implements  ComboBoxModel , ActionListener {
		// --------------------------------------------------
		// HACK #20 (O'Reilly Swing Hack)
		// --------------------------------------------------
		private List<String> data;
		private List<ListDataListener> listeners;
		private Object selected;	
		
		public ListComboBoxModel(IndexType it) {	
			if (it == IndexType.INDEX_2) {			 
				data = PM_LuceneLists.getInstance().getIndex_2();
			} else {
				data = PM_LuceneLists.getInstance().getIndex_1();
			}
			this.listeners = new ArrayList<ListDataListener>();
			 
//			if (data.size() > 0) {
//			selected = data.get(0);
//		}
		}

		public void setSelectedItem(Object item) {
			this.selected = item;
			cbText = textField.getText();
		}

		public Object getSelectedItem() {
			return this.selected;
		}

		public Object getElementAt(int index) {
			return data.get(index);
		}

		public int getSize() {
			return data.size();
		}

		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			this.listeners.remove(l);
		}

		public void actionPerformed(ActionEvent evt) {
			if (evt.getActionCommand().equals("update")) {
				this.fireUpdate();
			}
		}

		private void fireUpdate() {
			ListDataEvent le = new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, 0, data.size());
			for (ListDataListener l: listeners) {
				l.contentsChanged(le);
			}
		}
	}

	
	class ListBasicComboBoxEditor extends BasicComboBoxEditor {

		public JTextField getTextField() {
			if (editor instanceof JTextField) {
				return (JTextField) editor;
			}
			return null;
		}

		@Override
		public void setItem(Object anObject) {
			// set a String into the JTextField (the editor)
			if (anObject == null) {
				textField.setText("");
				fireChangeListener();
				return;
			}
			String txt = (cbText + " " + anObject).trim() + " ";
			textField.setText(txt);
			fireChangeListener();
		}
	}

}
