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

import pm.gui.*;
import pm.picture.*;
import pm.sequence.*;

import java.io.*;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.util.List;
import javax.swing.tree.*;
 


 

/**
 * Implement methods for remote access.
 * 
 * The daemon class "PM_Demon" provide the remote socket interface.
 * All remote methods for this socket connention are implemented 
 * in this class.
 * <p>
 * From daemon they are called with "invoke" method, declared here.
 * Therefore the method names must be the same comes from remote.
 *
 */
public class PM_RemoteAccess implements PM_Interface {

	private File topLevelPictureDirectory = null;	 
	private List<DefaultMutableTreeNode> rootNodeList = new ArrayList<DefaultMutableTreeNode>();
	
	
 	
	/**
	 * Create an instance (constructor)
	 */
	public PM_RemoteAccess() {	
	}
	
	
	/**
	 * initialize
	 */
	public void init() {
		if (topLevelPictureDirectory == null) {
			topLevelPictureDirectory = PM_Configuration.getInstance().getTopLevelPictureDirectory();	
			rootNodeList.add(PM_TreeModelAlbum.getInstance().getRootNode());
			rootNodeList.add(PM_TreeModelBase.getInstance().getRootNode());
			rootNodeList.add(PM_TreeModelExtended.getInstance().getRootNode());
		}
	}
	
	
	/**
	 * Invoke a remote method.
	 * 
	 * All methods to invoke must have exactly one argument.
	 * 
	 * @param name - the method name
	 * @param arg - the argument for the method
	 * @return must be a String: the result of the method
	 */
	public String invoke(String name, String arg ) {
		 
		Object ret = null;
		Method method;
		try {
			method = this.getClass().getMethod(name, String.class);
			ret = method.invoke(this, arg );
		} catch (Exception e) {
			System.out.println("exception e" + e.getCause());
			return null;
		}  
		
		if (ret instanceof String) {
			return (String)ret;
		}
		return null;
	}
	
 
	
	
	/**
	 * Returns with PM identification.
	 * 
	 * @return with TLPD name
	 *  
	 */
	public String getAlbumName(String dummy) {
		return topLevelPictureDirectory.getName();
		
	}
	
	
	/**
	 * Returns with a tree Node.
	 * 
	 * @param -str "0.3.2" 
	 * @return <in-param>/<node-list>/<child-list>
	 * 
	 * example: return "0.3/photoalbum.urlaub/2007.2008.2009.2010.test(b5: 7 pictures)"
	 * 
	 */
	public String  getTreeNode(String in) {
//	System.out.println("method getTreeNode in = >" + in + "<");	
		
		// -----------------------------------------
		// make the node list
		// ----------------------------------------
		StringBuffer nodeList = new StringBuffer();
		in = in.trim();
		if (in.length() == 0) {
			//	root 
			for (DefaultMutableTreeNode rootNode : rootNodeList) {
				if (nodeList.length() != 0) {
					nodeList.append(";");
				}
				nodeList.append(rootNode.getUserObject());
			}
			// root: node list is the child list (in is empty)
			return "//" + nodeList.toString();
		}
		 
	    // split the input
		DefaultMutableTreeNode node = null;
		for (String s : in.split(SPLIT_PUNKT)) {
			int index = PM_Utils.stringToInt(s);
			if (node == null) {
				node = (index >= rootNodeList.size()) ? rootNodeList.get(0)
						: rootNodeList.get(index);
				nodeList.append(node.getUserObject());
				continue;
			} else {
				nodeList.append(".");
			}
			int count = node.getChildCount();
			if (count == 0) {
				break;  // no childs
			}
			node = (index >= count) ? (DefaultMutableTreeNode)node.getChildAt(count-1) 
					: (DefaultMutableTreeNode)node.getChildAt(index);
			nodeList.append(node.getUserObject());		 
		}

		// -------------------------------------------
		// make the child list
		// ------------------------------------------
		Object obj = node.getUserObject();
		
		// ------- node is leaf and a sequence --------
		// ---- (make a picture list) ----
		if (node.isLeaf() && obj instanceof PM_Sequence) {
			int n = 0;
			PM_Sequence sequ = (PM_Sequence) obj;
			StringBuffer picList = new StringBuffer();
			for (PM_Picture pic : sequ.getAlleBilder()) {
				if (picList.length() > 0) {
					picList.append(";");
				}
				n++;
				picList.append(PM_Picture.getPictureKey(pic.getFileOriginal()));

			}
			// count before picList
			String count = String.valueOf(n) + "%;";
			return in + "/" + nodeList.toString() + "/" + count
					+ picList.toString();

		}
		
		// ----- node is leave (but not a sequence) ---------------
		if (node.isLeaf()) {
			
		}
		
		
		// It is not a sequence:  
		StringBuffer childList = new StringBuffer();
		Enumeration en = node.children();
		while (en.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) en
					.nextElement();
			if (childList.length() != 0) {
				childList.append(";");
			}
			obj = childNode.getUserObject();
			if (childNode.isLeaf() && !(childNode.getUserObject() instanceof PM_Sequence)) {			
				childList.append("_%%_"); // leaf not (not selectable)
			}
			if (obj instanceof PM_Sequence) {
				childList.append("_%_" + obj.toString());
			} else {
				childList.append(childNode.getUserObject() + " ...");
			}	
			
		}

		return in + "/" + nodeList.toString() + "/" + childList.toString();
	}

	/**
	 * Returns with all index directories.
	 * 
	 * <max id>;<id>_<path>;<id>_<path>; .....
	 * 
	 * <pre>
	 *  
	 * 
	 * One index dir: &lt;id&gt;_&lt;relativePath&gt;
	 *     id = id of PM_PictureDirectory instance
	 *     relativePath = relative to TLPD
	 *      
	 * </pre>
	 * 
	 * 
	 * 
	 * @return string of all relative directory names.
	 */
	public String getAllIndexDirs(String dummy) {
		
	     PM_MetadataContainer metadatenContainer = PM_MetadataContainer.getInstance();
	     StringBuffer  str = new StringBuffer();
	     int maxId = 0;
	     for (PM_PictureDirectory pd: metadatenContainer.getPictureDirectories()) {
	    	 String pathRelative = pd.getRelativePathTLD();
	    	 int id = pd.getIndexFileID(); 
	    	 maxId = Math.max(maxId, id);
	    	 str.append(String.valueOf(id) + "_" + pathRelative + ";");
	     }
		return String.valueOf(maxId) + ";" + str.toString();
	}

	
	/**
	 * getTempPicture()
	 */
	public String getTempPicture(String dummy) {
////// ****		List<PM_Picture> tempPictureList = PM_IndexView_deprecated.getTempPictureList();
		List<PM_Picture> tempPictureList = new ArrayList<PM_Picture>();
		StringBuffer  str = new StringBuffer();
		for (PM_Picture picture: tempPictureList) {
			str.append(picture.getPictureKey() + ";");
		}
		return String.valueOf(tempPictureList.size()) + ";" + str.toString();
	}
	
	
	
	/**
	 * getMetaData()
	 */
	public String getMetaData(String pictureKey) {
		String notFound = "metadata-not-found";
		
		 
		
		File file = PM_MetadataContainer.getInstance().getFileOriginal(pictureKey);	
		if (file == null) {
System.out.println("PM_RemoteAccess: PM_Picture not found for >" + pictureKey);
			return notFound;
		}
		PM_Picture picture = PM_Picture.getPicture(file);
		if (picture == null) {
System.out.println("PM_RemoteAccess: PM_Picture not found for >" + file.getAbsolutePath());
			return notFound;
		}
		
		DateFormat dateFormat = new SimpleDateFormat(" dd.MM.yyyy ");
		String date = dateFormat.format(picture.meta.getDateImport());
		
		String qs = " K" + picture.meta.getCategory() + " ";
		
		return qs + date;
		
	}
	
	/**
	 * Returns the full path to the mpeg file.
	 */
	public String getVideoPath(String pictureKey) {	
		String notFound = "video-path-not-found";
		File file = PM_MetadataContainer.getInstance().getFileOriginal(pictureKey);	
		if (file == null) {
			return notFound;
		}
		File fileMpeg = PM_Utils.getFileMPEG(file);
		if (fileMpeg.exists()) {
			return fileMpeg.getAbsolutePath();
		}		
		return notFound;				
	}


}
