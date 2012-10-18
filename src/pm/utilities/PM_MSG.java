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
package pm.utilities;

import java.util.*;
 

import javax.swing.*;
 

 
 

/**
 *  all what to do with resourceBundle
 *  
 *
 */
public class PM_MSG {

	
	
 
	private static Locale locale = new Locale("en");
	
	
	private static ResourceBundle resourceBundle = null;
	
	private static Set<PM_Listener> changeListener = new HashSet<PM_Listener>();
	
	public static void addChangeListener(PM_Listener listener) {
		 changeListener.add(listener);
	}

	private static void fireChangeListener() {
		for (PM_Listener listener: changeListener) {
			listener.actionPerformed(new PM_Action(null));
		}
	}	
	
	public static void setResourceBundle(Locale myLocale) {
		locale = myLocale;
		PM_Utils utils = new PM_Utils();	 
		try {
			resourceBundle = ResourceBundle.getBundle("MessagesBundle", locale, utils.getClass().getClassLoader());
			 
		}catch ( Exception ee) {
			
			JOptionPane.showConfirmDialog(null, "Fehler: Locale unbekannt", "Locale error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			
			System.out.println(" ERROR = " + ee);
	 
		}
		
//	if (!PM_Configuration.getInstance().getBatch()) {
//		setUIManager();
//			fireChangeListener(); // set text ...
//		}
	}
	 
	public static void setAndFileUIManager( ) {
		if (!PM_Configuration.getInstance().getBatch()) {
			setUIManager();
			fireChangeListener(); // set text ...
		}
	}
	
	
	public static Locale getLocale() {
		return locale;
	}
	
	public static String getMsg(String msg) {
		if (resourceBundle == null) {
			return "????";
		}
				
	// erst ab Version 6 !!!!	resourceBundle.containsKey(msg)
		
		String str = "";
		try {
		  str = resourceBundle.getString(msg);
		} catch ( Exception ee) {
			return "????";
			 
		}
		return str;
		
		
	}
	
	private static void setUIManager() {

		UIManager.put("OptionPane.cancelButtonText", getMsg("cancelButtonText"));
		UIManager.put("OptionPane.noButtonText", getMsg("noButtonText"));
		UIManager.put("OptionPane.okButtonText", getMsg("okButtonText"));
		UIManager.put("OptionPane.yesButtonText", getMsg("yesButtonText"));

	}
	
}
