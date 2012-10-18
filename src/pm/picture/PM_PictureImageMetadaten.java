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

import java.util.*;

import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

/**
 * Class to encapsulate the EXIF data in a tree map for a picture. 
 *  
 * There are directories (Exif, Interoperability, Jpeg, FujiFilm Makernote ...)
 *   (Image File Directory, or IFD structure)
 *   
 * Each entry in a directory has: 
 *           type:  integer or hex    (0x829a / 33434)
 *           key:   String            (Exposure Time)
 *           value: String            (0.01 sec)
 *  
 *  
 *  
 *  
 * The class provide methods to get the data by tags from the map.
 */
public class PM_PictureImageMetadaten {
	
	private SortedMap<String, String> map;// = new TreeMap<String, String>();
	private List<Tag> allTags;
	
	
	
	
	
	/**
	 * Create the instance.
	 * 
	 * @param map - a sorted map with all the EXIF data for a picture.
	 */
	public PM_PictureImageMetadaten(SortedMap<String, String> map, List<Tag> allTags) {
		this.map = map;	
		this.allTags = allTags;
	}
	
	 
	public boolean hasTag(String tag ) {		
		return map.containsKey(tag) ;		
	}	 

	
	 
	public String getDescription(String tag ) {		
		return  map.get(tag) ;		
	}	 
	
	 
	public Tag getTag(String dirName, int type) {
		for (Tag t: allTags) {
			if (t.getDirectoryName().equalsIgnoreCase(dirName) && t.getTagType() == type) {
				return t;
			}
		}
		return null;		
	}
	
	public String getTagValue(String dirName, int type) {
		String value = "";
		Tag t = getTag(dirName,type);
		if (t == null) {
			return value;
		}
		try {
			value  = t.getDescription();
		} catch (MetadataException e) {
			return "";
		}	
		return value;
	}
	
	// ==================================================
	// getMap()
	// ==================================================
	public SortedMap getMap( ) {
		return map;		
	}		
	
}
