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

//import sstest.inout.*;
//import sstest.view.*;
//import sstest.schnittstellen.*;
//import sstest.telegramme.*;
import java.awt.*;

public interface PM_Interface {

	public static enum DragAndDropType {
		NEW_SEQUENCE_B_X, NEW_SEQUENCE_ALBUM, UNKNOWN,  
	};
	
	public static enum Import {
		INTERN, EXTERN,  
	};
	
//	public static enum TreeModelChangedType {
//		INSERT, REMOVE, NAME  
//	};	
	
	static public final int NODE_INSERTED = 0;
	static public final int NODE_REMOVED = 1;
	static public final int NODE_RENAMED = 2;
	
	public static enum SequenceType {
		ALBUM, NEW, ORIGINAL,  EXTENDED, BASE, 
	};
	public static final String SEQU_NAME_UNKNOWN = "????????";
	
	public static enum SearchType {
		NOTHING, NO_INDEX_1, NO_INDEX_2, DOUBLE, SEQ, MINI_SEQ, NORMAL,
	}

	public static enum IndexType {
		 INDEX_1,  INDEX_2 
	}
	
	public static enum SearchSortType {
		NOTHING, TIME, FILE_PATH, FILE_NAME, SEQ,  
	};

	 
	public static enum Rotate {
		LEFT, RIGHT
	};
	
	// Selectieren im IndexView
	static public final int THUMB_SINGLE_CLICK = 1; // nur selectiert
	static public final int THUMB_DOUBLE_CLICK = 2; // zu uebernehmen
	
//	public static enum Clicks { SINGLE, DOUBLE };
	
	 
	
	static public final int CLOCKWISE_0_DEGREES = 0; // O
	static public final int CLOCKWISE_90_DEGREES = 90; // R
	static public final int CLOCKWISE_180_DEGREES = 180; // U
	static public final int CLOCKWISE_270_DEGREES = 270; // L

	// Icons
	public static final String ICON_PATH = "images/";
	public static final String PROPERTIES_PATH = "properties/";
	public static final String HELP_SET = "online-help/hs/main.hs";
	public static final String PM_PROPERTIES_FILE = "pm-properties.txt";
	
	public static final String ICON_DELETE = "button_cancel.png"; // "cross.gif";
	public static final String ICON_1_LEFT = "1leftarrow.png";
	public static final String ICON_2_LEFT = "2leftarrow.png";
	public static final String ICON_1_RIGHT = "1rightarrow.png";
	public static final String ICON_2_RIGHT = "2rightarrow.png";
	public static final String ICON_1_UP = "1uparrow.png";
	public static final String ICON_2_UP = "2uparrow.png";
	public static final String ICON_1_DOWN = "1downarrow.png";
	public static final String ICON_2_DOWN = "2downarrow.png";
	public static final String ICON_REREAD = "rebuild.png";
	public static final String ICON_UNDO = "undo.png";
	public static final String ICON_NEW = "new.gif";
	public static final String ICON_SLIDESHOW = "slideshow.gif";

	public static final String SPLIT_PUNKT = "\\.";

	public static final String PAPER_FORMAT_UNBEKANNT = "F0";
	public static final String PAPER_FORMAT_F1 = "F1";
	public static final String PAPER_FORMAT_F2 = "F2";
	public static final String PAPER_FORMAT_F4 = "F4";
	public static final String PAPER_FORMAT_F6 = "F6";

	public static final int OPEN_READ_ONLY = 1;
	public static final int OPEN_WRITE = 2;
	public static final int OPEN_CREATE = 4;

	static public final int QS_UNBEKANNT = 0;
	static public final int QS_1 = 1;
	static public final int QS_2 = 2;
	static public final int QS_3 = 3;
	static public final int QS_4 = 4;

	static public final Rectangle THUMBNAIL = new Rectangle(400, 300);

	static public final String FILE_EINSTELLUNGEN = "pm_einstellungen.xml";

	static public final String EXT_THUMBNAIL = "_th";
	static public final String EXT_MPEG = ".mpg";
	static public final String DIR_METADATEN = "pm.metadaten";
	static public final String DIR_METADATEN_ROOT = "pm_metadaten_root";
	 
	
	static public final String DIR_SEQUENCES = "pm_sequences";
	static public final String DIR_SEQUENCES_BASE = "base";
	static public final String DIR_SEQUENCES_NORMAL = "normal";
	static public final String DIR_SEQUENCES_VIRTUAL = "virtual";
	static public final String DIR_SEQUENCES_NEW = "new";
	
	static public final String DIR_THUMBNAILS = "pm.thumbnails";
	static public final String DIR_MPEG = "pm.mpeg";
	static public final String DIR_BILDER_BEARBEITEN = "pm.bilder_bearbeiten";
//	static public final String DIR_SICHERUNGEN = "pm_sicherungen";
	static public final String DIR_PM_TEMP = "pm_temp";
	static public final String DIR_SICHERUNGEN_INDEX_NEU = "index_neu";
	static public final String DIR_SICHERUNGEN_INDEX_GELADEN = "index_geladen";
	static public final String FILE_INDEX_XML = "pm_index.xml";
	static public final String FILE_XML_SESSION = "pm_session.xml";
	static public final String FILE_INDEX_XML_BAK = "pm_index.bak";
	static public final String DIR_LUCENE_DB = "pm_lucene.db";
	static public final String FILE_EXTERNE_PROGRAMME = "pm_external_programs.txt";
	static public final String FILE_INIT_VALUES = "pm_init_values.txt";
	static public final String FILE_LOG = "pm_log.txt";
	static public final String FILE_USER_HOME = ".photo-manager";
 
	static public final String DIR_STOP_FILES = "pm.stop-files";
	static public final String FILE_ALBUM_NAME = "pm_album_name.txt";
	static public final String FILE_LOCK = "pm_lock.temp";
//	static public final String FILE_VDR_SERVER = "vdr-server.txt";
	
	
	// Sequenzen
	static public final String FILE_XML_SEQUENZEN = "pm_sequenzen.xml";
	static public final String FILE_XML_SEQUENZEN_SAVE = "pm_sequenzen.xml_";
	static public final String FILE_XML_SEQUENZEN_DEPRECATED = "pm_metadaten_global.xml";


	static public final String DIR_VIRTUAL_BILDER = "pm_virtual_bilder";
	static public final String DIR_VIRTUAL_TXT = "pm_virtual_text";
	static public final String FILE_VIRTUAL_TXT = "pm_virtual_text.txt";

	static public final String OHNE_INDEX_1 = "OHNE_INDEX_1";
	static public final String OHNE_INDEX_2 = "OHNE_INDEX_2";

	static public final String LUCENE_SEQUENZ_TRENNER = "_";

	static public final int TOC_MAX_COL = 6; // Anzahl der Spalten
	static public final int TOC_COL_VON = 0;
	static public final int TOC_COL_BIS = 1;
	static public final int TOC_COL_INHALT = 2;
	static public final int TOC_COL_QS = 3;
	static public final int TOC_COL_INDEX = 4;
	static public final int TOC_COL_ORT = 5;

	
	static public final int TAB_SEARCH = 0;
	static public final int TAB_SEQUENCE = 1;
	static public final int TAB_ZEIGEN_EINZEL = 2;
	static public final int TAB_ZEIGEN_GRUPPE = 3;
	static public final int TAB_DRUCKEN = 4;
	static public final int TAB_EXPORTIEREN = 5;
	static public final int TAB_IMPORTIEREN = 6;
	static public final int TAB_INFO_BILD = 7;
	static public final int TAB_THUMBNAILS = 30; // h�chsten Wert !!

 
	
	
	// Suchen
	static public final String SUCHEN_SEQ_TRENNER = ".";

	// Sequenzen
	public static final int SEQ_OFFEN = 1; // komplex (offen)
	public static final int SEQ_GESCHLOSSEN = 2; // einfach (geschlossen)
	public static final int SEQ_NEUE_BILDER = 4; // neue Bilder (geschlossen)
	public static final int SEQ_ALLE_BILDER = 8; // alle Bilder (geschlossen

	public static final String SEQ_CLOSED_NORMAL = "s";
	public static final String SEQ_CLOSED_BASE = "b";
	public static final String SEQ_CLOSED_NEW = "n";

	public static final String SEQ_CHARACTER_BASE = "b";
	public static final String SEQ_CHARACTER_EXTENDED = "s";
	public static final String SEQ_CHARACTER_NEW = "n";
	public static final String SEQ_CHARACTER_ALBUM = "a";
 
	
	
	// drehen
	static public final int OBEN = 0;
	static public final int RECHTS = 1;
	static public final int UNTEN = 2;
	static public final int LINKS = 3;

	// Properties
	static public final String PROP_BILDER_HOME = "bilder.home";
	static public final String PROP_HOME_LUCENE = "home.lucene";
	static public final String PROP_EINSTELLUNGEN = "pm_einstellungen";

	static public final String NUL = "\0";
	static public final String SOH = "\1";
	static public final String STX = "\2";
	static public final String ETX = "\3";
	static public final String EOT = "\4";
	static public final String ENQ = "\5";
	static public final String ACK = "\6";
	static public final String BEL = "\7";
	static public final String BS = "\10";
	static public final String TAB = "\11";
	static public final String LF = "\12"; // wie NL
	static public final String NL = "\12"; // wie LF
	static public final String VT = "\13";
	static public final String FF = "\14";
	static public final String VR = "\15"; // wie CR
	static public final String CR = "\15"; // wie VR
	static public final String SO = "\16";
	static public final String SI = "\17";
	static public final String DLE = "\20";
	static public final String DC1 = "\21"; // wie XON
	static public final String XON = "\21"; // wie DC1
	static public final String DC2 = "\22";
	static public final String DC3 = "\23"; // wie XOFF
	static public final String XOFF = "\23"; // wie DC3
	static public final String DC4 = "\24";
	static public final String NAK = "\25";
	static public final String SYN = "\26";
	static public final String ETB = "\27";
	static public final String CAN = "\30";
	static public final String EM = "\31";
	static public final String SUB = "\32";
	static public final String ESC = "\33";
	static public final String FS = "\34";
	static public final String GS = "\35";
	static public final String RS = "\36";
	static public final String US = "\37";

	// Fonts zur Darstellung von Text in textPanes
	static public final String STYLE_REGULAR = "z_regular";
	static public final String STYLE_BOLD = "z_bold";
	static public final String STYLE_ITALIC = "z_italic";
	static public final String STYLE_UNDERLINE = "z_underline";

	// Symbolische Tasten f�r Diashow
	public static final int DIASHOW_NORMAL = 1;
	public static final int DIASHOW_AUTOM_SEQUENT = 2;
	public static final int DIASHOW_AUTOM_RANDOM = 3;

	public static final String STR_DIASHOW_NORMAL = "normal";
	public static final String STR_DIASHOW_AUTOM_SEQUENT = "sequent";
	public static final String STR_DIASHOW_AUTOM_RANDOM = "random";

}
