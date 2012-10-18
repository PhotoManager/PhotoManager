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

import java.io.File;
 
import java.util.*;

import com.drew.imaging.jpeg.*;
import com.drew.metadata.*;
 


/**
 * Read the metadata EXIF from a picture file
 *  
 *
 */
public class PM_ReadImageMetadaten {

	/**
	 * Create a EXIF reader.
	 *
	 */
	public PM_ReadImageMetadaten() {
		 	
	}
	
	/**
	 * Get the EXIF Metadata from a picture file.
	 *  
	 * @param file - the picture file 
	 * @return the metadata for the image (a tree map)
	 */ 
	public PM_PictureImageMetadaten readMetadaten(File file) {
       // --------------------------------------------------------------
      // lesen Metadaten aus dem Original-Bild
      // --------------------------------------------------------------
      Metadata metadata = null;
      try {
        metadata = JpegMetadataReader.readMetadata(file);
      } catch (Exception e) {
//        System.out.println("Fehler beim Lesen der Metadaten");
        return new PM_PictureImageMetadaten(new TreeMap<String, String>(), new ArrayList<Tag>());
      }
      
      
      TreeMap<String, String> map = new TreeMap<String, String>();
      
      List<Tag> allTags = new ArrayList<Tag>();
      
      Iterator directories = metadata.getDirectoryIterator();
      while (directories.hasNext()) {
    	  Directory directory = (Directory) directories.next();
    	  // iterate through tags 
    	  Iterator tags = directory.getTagIterator();
    	  while (tags.hasNext()) {
    		  Object o = tags.next();
    		  if ( ! (o instanceof Tag)) continue;       
    		  Tag tag = (Tag)o;
    		  String tagName = tag.getTagName();  
 
    		  
    		  
    		  String tagValue;
    		  try {
    			  tagValue = tag.getDescription();
    		  } catch (MetadataException e) {
    			  continue;
    		  }
    		  // Tag gültig:  tagName / tagValue
    		  map.put(tagName, tagValue);
    		  allTags.add(tag);
       		  
    		  int tagType = tag.getTagType();
    		  String tagTypeHex = tag.getTagTypeHex();
    		  String tagDirName = tag.getDirectoryName();
    System.out.println("DirName: " + tagDirName + ", hex: " + tagTypeHex + ", type: " + tagType + ", key: " + tagName + ", value: " + tagValue);
                                
    	  }
      }   
      
      return new PM_PictureImageMetadaten(map, allTags);

    }
	
	
	
}
