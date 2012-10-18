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
package pm.picture;

 
import pm.utilities.*;

import java.io.File;
import java.util.*;
 


/**
 * Dieses Interface ist der Kontrakt zum pm_index.xml File !!!!!
 * 
 * 
 * @author dih
 *
 */
public interface PM_IndexFile {

//	public String getUUID();
	
//	public void setUUID(String uuid);
	
	public int getIndexFileID();
	
 	public void setIndexFileID(int fileID);
	
	public boolean getMetadaten(PM_Picture picture); 
	
	public void update(PM_Picture picture, String id);
	
	public void writeDocument ();
	
	public int getPictureSizeValid();
	
	public int getPictureSizeInvalid();
	
	public void initComplete();
	
	public Set<String> getInvalidFileNames();
	
	public Set<String> getValidFileNames();
	
	public void loeschenIndex1();
	
	public void loeschenIndex2();
	
	public void alleSequenzenLoeschen();
	
	public void removeID(String id);
	
	public void alleLuceneEintraegeNeuErstellen(PM_ListenerX listener);
	
	 
	public List<PM_Picture> getAllPicturesNotInSequences( );
	
	public List<PM_Picture> bilderDoppelteSequenzen();
	
	public List<File> getPicturesNotInSequences(List seqNamen, boolean doppelte);
	
	public int getVersionRead();
	
	public int getVersionWrite();
	
	public int deletePictureInvalid(File dirOrigFile);
	
}
