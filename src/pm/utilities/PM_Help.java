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

import java.awt.event.*;
 
//import javax.help.*;


public class PM_Help implements PM_Interface {

	private static PM_Help instance = null;
	private ActionListener actionListener = null;

	// =====================================================
	// Class Method: getInstance()
	//
	// Es wird nur eine Instanz angelegt (Singleton)
	// =====================================================
	static public PM_Help getInstance() {
		if (instance == null) {
// **********************************************************************
// ***********************************************************************
	//   **************** HIER HELP EINSCHALTEN **************************
//			 ***********************************************************************
//			 ***********************************************************************
//			 ***********************************************************************
 //			instance = new PM_Help();
		}
		return instance;
	}

	// ==========================================================
	// Konstruktor
	// ==========================================================
	public PM_Help() {
		/*
		final ClassLoader cl = PM_Help.this.getClass().getClassLoader();
		System.out.println("classloader = " + cl);
		final URL url = HelpSet.findHelpSet(cl, HELP_SET);
		System.out.println("url = " + url);
		try {
			final HelpSet hs = new HelpSet(cl, url);
			HelpBroker hb = hs.createHelpBroker();
			actionListener = new CSH.DisplayHelpFromSource(hb);
		} catch (Exception e) {
			System.out.println("PM_Help: Helpset nicht gefunden. Url = " + url);
			actionListener = null;
		}
		*/
	}

	// ==========================================================
	// getActionListener()
	// ==========================================================
	public ActionListener getActionListener() {
		return actionListener;
	}

}
