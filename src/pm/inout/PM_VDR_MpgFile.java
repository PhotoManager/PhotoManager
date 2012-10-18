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
package pm.inout;

 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

 
import pm.gui.PM_WindowMain;
import pm.picture.*;
import pm.sequence.PM_Sequence;
import pm.utilities.*;
 
 

/**
 * Create a mpeg file from jepeg file
 * 
 * 
 *  pm.metadaten/pm_index.xml
 *  			/pm.thumbnails/ ... all thumbnails
 *  			/pm.mpeg        ... all mpeg files for vdr
 * 
 *  
 *  
 *  
 * jpegtopnm p1300206.jpg 2> /dev/null 
|pnmscale  --xscale=0.263671875 --yscale=0.375 
|pnmpad  --black --width 720 --height 576 
|ppmntsc  --pal 
|ppmtoy4m -v 0 -F 25:1 -I p -S 420mpeg2 
|mpeg2enc -v 0 -f 3 -b 12500 -a 3 -q 1 -n p     -o p1300206.mpg
 * 
 *   my ($Pict, $Mpeg) = @_;
  (my $Type) = lc($Pict) =~ /\.([^\.]*)$/;
  if (!defined $PNMCONV{$Type}) {
     return if ($Ignore);
     die "unknown file type '$Type': '$Pict'\n";
     }
  my ($w, $h) = imgsize($Pict);
  print "image size is $w x $h\n" if ($Detailed);
  if ($w / $h <= $ScreenRatio) {
     $w = $h * $ScreenRatio;
     }
  else {
     $h = $w / $ScreenRatio;
     }
  my $ScaleW = $SW / $w * (100 - 2 * $OverscanX) / 100;
  my $ScaleH = $SH / $h * (100 - 2 * $OverscanY) / 100;
  $Pict = EscapeMeta($Pict);
  $Mpeg = EscapeMeta($Mpeg);
  print "$Pict -> $Mpeg\n" if $ListFiles;
  my $Cmd = "$PNMCONV{$Type} $Pict 2> /dev/null |"
          . "pnmscale $verbose1 --xscale=$ScaleW --yscale=$ScaleH |"
          . "pnmpad $verbose1 --black --width $SW --height $SH |"
          . "ppmntsc $verbose1 $system1 |"
          . "ppmtoy4m $verbose2 -F $framerate -I p -S 420mpeg2 |"
          . "mpeg2enc $verbose2 -f 3 -b 12500 -a $aspect -q 1 -n $system2 -o $Mpeg";
 *
 */
public class PM_VDR_MpgFile implements PM_Interface {

	static private double screenRatio = 16.0 / 9.0;
	static private int screenSizeW = 720;
	static private int screenSizeH = 576;
	
	/**
	 *  Constructor  
	 *
	 */
	public PM_VDR_MpgFile(PM_Picture picture) {
		 
	   this.picture = picture;
		 
	}
	
	/**
	 * Check for all programms to convert to mpeg
	 * (all programms are in the debian package  "mjpegtools")
	 * 
	 * 	jpegtopnm
	 * 	pnmscale
	 * 	pnmpad
	 * 	ppmntsc
	 * 	ppmtoy4m
	 * 	mpeg2enc
	 */
	static public String checkForProgramms () {
		
		String msg = "";
		
	     File f = new File("/usr/bin/jpegtopnm");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/jpegtopnm not found\n";
	     }
	     f = new File("/usr/bin/pnmscale");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/pnmscale not found\n";
	     }
	     f = new File("/usr/bin/pnmpad");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/pnmpad not found\n";
	     }
	     f = new File("/usr/bin/ppmntsc");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/ppmntsc not found\n";
	     }
	     f = new File("/usr/bin/pnmflip");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/pnmflip not found\n";
	     }
	     f = new File("/usr/bin/ppmtoy4m");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/jpegtopnm not found\n";
	     }
	     f = new File("/usr/bin/mpeg2enc");
	     if (!f.canExecute()) {
	    	 msg += "/usr/bin/mpeg2enc not found\n";
	     }     
	     
	    if (msg.length() == 0) {
	    	return null;
	    }
		return msg;
	}
	
	/**
	 * Is picture converted
	 * 
	 * Must be same modfication-time:  Thumbnail and mpegfile
	 */
	static public boolean isPictureConverted(File fileThumbnail , File fileMpeg) {
	
		if (fileThumbnail.exists() && fileMpeg.exists() ) {
			return true;
		}
		return false;
	}
	
	/**
	 * metadataChanged()
	 * 
	 * The picture has changed and I must delete a mpeg file
	 * (if available).
	 * It must then create new at program  end.
	 */
	static public void metadataChanged(PM_Picture picture, int type) {
		if (type == PM_PictureMetadaten.BEARBEITET
				|| type == PM_PictureMetadaten.ROTATE
				|| type == PM_PictureMetadaten.SPIEGELN
				|| type == PM_PictureMetadaten.CUT_RECTANGLE)
		{
			// now delete the mpegfile (if available)
			File fileMpeg = PM_Utils.getFileMPEG(picture.getFileOriginal());
			fileMpeg.delete();
		}
	}
	
	
	private PM_Picture picture;
	

	
	/**
	 * Convert()
	 *  
	 *  There are not a pm-script running.
	 *  The command only calls some programms.
	 */
	public boolean  convert(File fileOrig)   {
		

		File fileThumbnail = PM_Utils.getFileThumbnail(fileOrig);
		File fileMpeg = PM_Utils.getFileMPEG(fileOrig);
		
		Dimension imageSize = picture.meta.getImageSize();		
	 
		// order:   cut --> flip --> rotate		
		 
		String cmd =  jpegtopnm()  
					+ pnmcut(imageSize)		 // cut
					+ pnmflip(imageSize)     // flip and rotate
					+ " | " + pnmscale(imageSize) 
					+ " | " + pnmpad()
					+ " | " + ppmntsc()
					+ " | " + ppmtoy4m()
					+ " | " + mpeg2enc(fileMpeg);
		
 //		System.out.println("Command=>" + cmd + "<");
		
		// the command must run in a shell !!!
		// (the exec method of Runtime don't invoke the command in a shell)
		String[] command = {"/bin/sh", "-c", cmd};
		
		Process process = null;
		try {
		    process = Runtime.getRuntime().exec(command);
			 
		} catch (IOException e) {
			System.out.println("ERROR exec(): " + e);
			return false;
		}
		
		// Now check the if errors.
		BufferedReader is = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	    String line;
	    try {
			while ((line = is.readLine()) != null) {
				System.out.println(line);
				return false;
			}
		} catch (IOException e1) {
			return false;
		}
		
		
		
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			System.out.println("ERROR process.waitFor(): " + e);
			return false;
		}
	 
		// the command was executed successfully.
		// Set date/time of new mpeg file to date of thumbnail. 
		 
//		fileMpeg.setLastModified(fileThumbnail.lastModified());
		return true;
	}
	
	/**
	 * jpegtopnm()
	 *  
	 */
	private String jpegtopnm() { 
		return "jpegtopnm " +  "\""  + picture.getFileOriginal().getPath() + "\""  + " 2> /dev/null ";
	}
	
	/**
	 * pnmcut()
	 * 
	 */
	private String pnmcut(Dimension imageSize) {
		if (!picture.meta.hasCutRectangle()) {
			return "";
		}
		
		Rectangle cut = picture.meta.getCutRectangle();
		
		
		// check if cut fit into image
		if (cut.x < 0) {
			cut.x = 0;
		}
		if (cut.y < 0) {
			cut.y = 0;
		}
		Dimension iS = picture.meta.getImageSize();
		if (cut.x + cut.width > iS.width) {
			cut.x = 0;
			cut.width = iS.width;
		}
		if (cut.y + cut.height > iS.height) {
			cut.y = 0;
			cut.height = iS.height;
		}
		
		// now the cut is o.k.
		imageSize.width = cut.width;
		imageSize.height = cut.height;
		
		String left = " -left " + String.valueOf(cut.x);
		String top = " -top " + String.valueOf(cut.y);
		String width = " -width " + String.valueOf(cut.width);
		String height = " -height " + String.valueOf(cut.height);
		
		
		return " | pnmcut  "  + left + top + width + height + " ";
		
		
	}
	
	/**
	 * pnmflip()
	 * 
	 * make flip and rotate  
	 *    ( order:   flip --> rotate )
	 */
	private String pnmflip(Dimension imageSize) { 
		String cmd = "";
		
		// (1) flip
		if (picture.meta.getMirror()) {
			cmd = " -leftright ";
		}
			
		// (2) rotation
		int rotation = picture.meta.getRotation();
		
		// I rotate clockwise, pnmflip rotate counterclockwise !!!!
		switch (rotation) {
		case 0:	
			break;
		case 90: 
			cmd += " -rotate270 "; //  pnmflip rotate counterclockwise
			break;
		case 180: 
			cmd += " -rotate180 ";
			break;
		case 270: 
			cmd += " -rotate90 "; // pnmflip rotate counterclockwise
			break;
		}
		
		if (cmd.length() == 0) {
			return "";
		}
		
 
 		if (rotation == 90  || rotation == 270) {
 			int x = imageSize.width;
 			imageSize.width = imageSize.height;
 			imageSize.height = x;
 		}
		
		
		
		return " | pnmflip " + cmd + " ";
		
	}
	
	/**
	 * pnmscale()
	 *  
	 *  "pnmscale  --xscale=0.263671875 --yscale=0.375"
	 */
	private String pnmscale(Dimension imageSize) {
	 
		
//		static private double screenRatio = 16.0 / 9.0;
//		static private int screenSizeW = 720;
//		static private int screenSizeH = 576;
		
		
		double imageSizew = imageSize.width;
		double imageSizeh = imageSize.height;	 

		if (screenSizeW / imageSizeh <= screenRatio) {
			imageSizew = imageSizeh * screenRatio;
		} else {
			imageSizeh = imageSizew / screenRatio;
		}
		
		return "pnmscale -xscale=" + (double)screenSizeW/(double)imageSizew 
					  + " -yscale=" + (double)screenSizeH/(double)imageSizeh + " ";
	}
	 
	
	/**
	 * pnmpad()
	 * 
	 * pnmpad  --black --width 720 --height 576
	 */
	private String pnmpad() {
		int screenSizeW =  720;
		int screenSizeH =  576;
		return "pnmpad --black --width 720 --height 576 ";
	}
	 
	/**
	 * ppmntsc()
	 * 
	 * ppmntsc  --pal
	 */
	private String ppmntsc() {
		return "ppmntsc  --pal ";
	}
	
	/**
	 * ppmtoy4m()
	 * 
	 * ppmtoy4m -v 0 -F 25:1 -I p -S 420mpeg2
	 */
	private String ppmtoy4m() {
		String framerate = "25:1";
		return "ppmtoy4m -v 0 -F 25:1 -I p -S 420mpeg2 ";
	}
	
	/**
	 * mpeg2enc()
	 * 
	 * mpeg2enc -v 0 -f 3 -b 12500 -a 3 -q 1 -n p     -o p1300206.mpg
	 */
	private String mpeg2enc(File fileMpeg) { 
	    fileMpeg.getParentFile().mkdirs();
		String str = "mpeg2enc " 
					+ "-v 0 "
					+ "-f 3 "    // 
					+ "-b 12500 " 
					+ "-a 3 "   // 3 -> 16:9   2 -> 5:4
					+ "-q 1 " 
					+ "-n p " 
					+ "-o \""  +fileMpeg.getPath() + "\"";
	    
	    
		return str;
	}
	
	 
	/**
	 * makeMpegfiles()
	 * 
	 * Make all mpeg files not valid
	 */
	static int anzahlFuerProgressionBar = 0;
	static boolean cancel = false;
	static public int makeMpegfiles(int count) {
		cancel = false;
		final int max =  count;
		PM_MetadataContainer.getInstance().flush();
 
		
		
		final JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);

		JPanel dialogPanel = new JPanel();
		dialogPanel.setPreferredSize(new Dimension(300, 200));
		dialogPanel.setLayout(new BorderLayout());

		
		
		// progression
		final JProgressBar progressionBar = new JProgressBar(1, max);
		dialogPanel.add(progressionBar, BorderLayout.NORTH);
		
		// cancel button
		
		JButton cancelButton = new JButton("cancel");
		cancelButton.addActionListener(new ActionListener() {

			 
			public void actionPerformed(ActionEvent e) {
				cancel = true;			
			}
			
		});
		dialogPanel.add(cancelButton);
		//   dialogPanel.setUndecorated(true);
		dialog.getContentPane().add(dialogPanel);
		dialog.pack();

 		
		// ---------- alle indexieren -----------------------
		
		final PM_ListenerX listener = new PM_ListenerX() {
			  public boolean actionPerformed(PM_Action e) {
				 
				SwingUtilities.invokeLater(new Runnable() {
					  public void run() {
						anzahlFuerProgressionBar++;
						progressionBar.setValue(anzahlFuerProgressionBar + 1);
					}
				});		 
				return !cancel;
			}
		};
 
		anzahlFuerProgressionBar = 0;
		 
		// Start importThread
		Thread thread = new Thread (new Runnable() {
			 public void run() {
				
				PM_MetadataContainer.getInstance().makeMpegFiles(listener);
				
				 
				if (dialog != null) dialog.dispose();
			
			} 
		});
			
			
			
			
			
	 	 
		
		
		
		thread.start();

		dialog.setVisible(true);
		
		return anzahlFuerProgressionBar;

	}
	//=============================================================
	//=============================================================
	//=============================================================
	//
	// Inner Class:  Thread indexieren
	//
	//=============================================================
	//=============================================================
	//=============================================================

	private class MakeMPEG extends Thread {

		private PM_ListenerX listener;
		private JDialog dialog;

		// =====================================================
		// Konstruktor
		// =====================================================
		public MakeMPEG(PM_ListenerX listener, JDialog dialog) {
			this.listener = listener;
			this.dialog = dialog;
		}

		// =====================================================
		// run()
		// =====================================================
		public void run() {

	//		luceneDBneuErstellen(listener);

			// -----------------------------------------------------------------
			// alle indexiert. Jetzt Dialog wieder freigeben und thread beenden
			// -----------------------------------------------------------------

			if (dialog != null) dialog.dispose();
		}

	} // Ende inner class Indexieren
	
}
