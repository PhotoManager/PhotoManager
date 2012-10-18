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





//
// 
//
//
public class PM_Action  {    

  private Object object;
  private int type = -1;
  private String str = "";
  // =====================================================
  // Konstruktor 
  // =====================================================
  public  PM_Action(Object object) { 
    this.object = object;
  }
  public  PM_Action(Object object, int type) { 
    this.object = object;
    this.type = type;
  }
  public  PM_Action(Object object, int type, String str) { 
	    this.object = object;
	    this.type = type;
	    this.str = str;
	  }
  // =====================================================
  // getObject()/getTyp()
  // =====================================================
  public Object getObject() { 
    return object;
  } 
  public int getType() { 
    return type;
  }
  public String getString() { 
	    return str;
  }
  // =====================================================
  // toString() 
  // =====================================================
  public String toString() { 
    return object.toString();
  }   
  
}
