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

 


public class PM_AddOn {

	private static PM_AddOn  instance = null;
	
	private boolean index2 = true; //false;
	private boolean extSequence = true; //false;
	private boolean miniSequence = true; //false;
	
	
	static public PM_AddOn getInstance() {
		if (instance == null) {
			instance = new PM_AddOn();
		}
		return instance;
	}
	
	public boolean getAddOnExtSequence() {
		return extSequence;
	}
	
	public boolean getAddOnMiniSequence() {
		return miniSequence;
	}
	
	
	
	public boolean getAddOnIndex2() {
		return index2;
	}
	
	
	
}
