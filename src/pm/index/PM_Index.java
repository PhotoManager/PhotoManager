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

 

import javax.swing.*;

import pm.gui.*;
 
 


 
public class PM_Index {

	  
	 
	
	 
	public PM_IndexData data;	
	public PM_IndexController controller;	
	public PM_IndexView indexView;
	
	static protected PM_Index indexLeft;
	
 
	/**
	 * Create an index panel for the left side of the main window.
	 *  
	 */
	static public PM_Index createIndexLeft( ) {
		PM_Index index = new PM_Index();
		index.setIndexView(new PM_IndexViewLeft(index));
		indexLeft = index;
		return index;
	}
	 
	/**
	 * Create an index panel for the right side of the main window.
	 * 
	 * The right side of the main window are sub windows. You select the
	 * sub windows with tabs.
	 *  
	 */
	static public PM_Index createIndexRight( ) {
		PM_Index index = new PM_Index();
		index.setIndexView(new PM_IndexViewRight(index));
		return index;
	}
	
	/**
	 * Create an right side index panel to import new pictures.
	 *  
	 */
	static public PM_Index createIndexImport(PM_WindowImport windowImport) {
		PM_Index index = new PM_Index();
		index.setIndexView(new PM_IndexViewImport(index, windowImport));
		return index;
	}
	
	/**
	 * Set the index view.
	 * 
	 * This method is necessary because the create methods are
	 * class methods.
	 *  
	 */
	private void setIndexView(PM_IndexView indexView) {
		this.indexView = indexView;
	}
		
 
	/**
	 * private constructor
	 */
	private PM_Index() {}

 
	/**
	 * Initialize the instance.
	 * 
	 */
	public void init(PM_WindowBase windowBase) {
		data = new PM_IndexData(this);
		controller = new PM_IndexController(this );	 
		indexView.init(this,   windowBase,  controller, data);
		controller.setViewAndModel(indexView, data); 
	}
	
	
	public JPanel getIndexPanel() {		
		return indexView.getIndexPanel();
	}
	 
	public JPanel getIndexToolbar() {
		return indexView.getIndexToolbar();
	}		
	 
	public boolean isLeft() {
		return indexView.isLeft();
 
	}
 
	
	
}
