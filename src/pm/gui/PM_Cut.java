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
package pm.gui;

 
import pm.utilities.*;
 
import javax.swing.*;
 
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
 

/** Basisklasse zum Ausschneiden 
 *
 * Folgende Methode  muss von der abgeleiteten Klasse aufgerufen werden:
 *    void drawRectangleResize(Graphics)
 *          muss aufgerufen werden, wenn gezeichnet wird. Es werden
 *          das zu movende und zu resizende Recheck mit den Anfassern
 *          gezeichnet. Die Color muss vorher vom Aufrufer gesetzt werden.
 *          
 * Folgende Methoden müssen mit protected in der abgeleiteten 
 * Klasse Überschrieben werden:
 *    boolean resize()
 *          fragt, ob das Rechteck gemoved und resized wird
 *    boolean resizeSeiten()
 *          fragt, ob auch die Anfasser an den Seiten zu zeichnen sind.
 *          Wird nur ausgewertet, wenn hasResized() mit true antwortet.
 *          Default auf true.
 *    Rectangle getRectangleResize()
 *          liefert das zu movende und resizende Rechteck
 *    Rectangle getRectangleMax()
 *          liefert das Rechteck in dem gemoved und resized wird
 *    
 *    void drawRectangleResize(Graphics)
 *          das zu resizende Rechteck wird gezeichnet.
 *          Wenn nicht überschrieben wird es vom PM_Ausschneiden
 *          gezeichnet.
 *          Hier hat die abgeleitete Klasse die Möglichkeit,
 *          z.B. das Recheck gestrichelt zu zeichnen falls das
 *          Seitenverhältnis sich geändert hat.
 *    void setRectangleResized(Rectangle)  
 *          wird aufgerufen, wenn sich das Rechteck geaendert hat
 *
 *
 */
@SuppressWarnings("serial")
public class PM_Cut extends JPanel implements MouseListener, MouseMotionListener {

	 

	// Werden von der abgeleiteten Klasse versorgt  
	private Rectangle2D rectangleResize = new Rectangle2D.Double(); // dieses Rechteck wird manipuliert

	private Point pointPressed = new Point();
	private Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);

	// Anfasser  Ecken 
	private Rectangle2D recNW = null;
	private Rectangle2D recNE = null;
	private Rectangle2D recSW = null;
	private Rectangle2D recSE = null;
	// Anfasser  Seiten 
	private Rectangle2D recN = null;
	private Rectangle2D recE = null;
	private Rectangle2D recS = null;
	private Rectangle2D recW = null;

	// *************************************************************************
	// ******* protected ************************************************
	// *************************************************************************  

	// =====================================================
	// protected: Konstruktor
	// =====================================================
	protected PM_Cut() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	// =====================================================
	// protected: setRectangleResized(Rectangle)
	//
	// Wird ueberschrieben:
	//    Wird von dieser Klasse aufgerufen, wenn sich das Rechteck
	//    geaendert hat:
	//       bei move in der Positon,
	//       bei resized in der Position und Groesse.
	// =====================================================   
	protected void setRectangleResized(Rectangle2D rectangleResize, AffineTransform at) {
		this.rectangleResize = rectangleResize;
	}

	// =====================================================
	// resize()     (deprecated)                generell zeichnen der Anfasser
	// resizeDiagonal()             Diagonale Anfasser zeichnen
	// resizeDiagonalSymmetrich()   Diagonale symmetrich
	// resizeVertiHorizontal()      links/rechts/oben/unten
	// resizeVertiHoriSymmetrich()  links/rechts/oben/unten symmetrich
	//
	// Müssen alle in der abgeleiteten Klasse überschrieben werden
	// =====================================================  
	protected boolean move() {
		return true;
	}

	protected boolean resizeDiagonal() {
		return true;
	}

	protected boolean resizeDiagonalSymmetrich() {
		return false;
	}

	protected boolean resizeVertiHorizontal() {
		return false;
	}

	protected boolean resizeVertiHoriSymmetrich() {
		return false;
	}

	// =====================================================
	// protected: getRectangleResize()
	//
	// Muss in der abgeleiteten Klasse ueberschrieben werden
	//
	// Liefert das zu movende und resizende Rechteck.
	// =====================================================   
	protected Rectangle2D getRectangleResize() {
//		System.out.println("PM_Ausschneiden: getRectangleResize() nicht ueberschrieben");
		return null;
	}
	protected Rectangle2D getRectangleResizeOtherSize() {
//		System.out.println("PM_Ausschneiden: getRectangleResize() nicht ueberschrieben");
		return null;
	}

	// =====================================================
	// protected: getRectangleMax()
	//
	// Muss in der abgeleiteten Klasse ueberschrieben werden
	//
	// liefert das Rechteck in dem gemoved und resized wird.
	// (dar�ber hinaus soll das Rectangle nicht gemoved/resized werden)
	// =====================================================   
	protected Rectangle2D getRectangleMax() {
//		System.out.println("PM_Ausschneiden: getRectangleMax() nicht ueberschrieben");
		return null;
	}

	// =====================================================
	// protected: drawCutRectangle()
	//
	// Muss in der abgeleiteten Klasse ueberschrieben werden.
	//
	// Zeichnet das CutRectangle
	// (wenn nicht überschrieben wird es hier gezeichnet)
	// =====================================================   
	protected void drawRectangleResize(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.YELLOW);
		g2d.draw(getRectangleResize());
		
//		g2d.setColor(Color.RED);
//		g2d.draw(getRectangleResizeOtherSize());
	}

	// =====================================================
	// protected: drawRectangleResize()
	//
	// Zeichnen resized-Rectangle mit Anfasser
	// Die Color muss vorher vom Aufrufer gesetzt sein
	//
	// (wird NICHT von dieser Klasse aufgerufen)
	// ===================================================== 
	private Point correct = new Point(); // (Workaround !!!!!!!!!!!!!!)

	protected void drawRectangleResize(Graphics g, Point correct) {
		Graphics2D g2d = (Graphics2D) g;

		// Offset vom Origin (0,0) des Bildschirmes bis zum Origin des Images !!
		//   (Workaround !!!!!!!!!!!!!!)
		this.correct = correct;

		// das zu resizende und zu movende Rectangle  
		// Muss in jedem Fall hier von der abgeleiteten Klasse geholt werden,
		// da das bereits VOR mousePressed gezeichnet wird !!! 
		Rectangle rectCut = PM_UtilsGrafik.rectangle2DToRectangle(getRectangleResize());
		if (rectCut == null) return;
		if (rectCut.getWidth() == 0 || rectCut.getHeight() == 0) return;

		// --------------------------------------------------------   
		// Cut-Rectangle zeichnen
		// (von der abgeleiteten Klasse oder, wenn nicht überschrieben
		//  mit der hier erstellten Methode)
		// (z.B. wenn gestrichelt gezeichnet bei verändertem Ratio)
		// --------------------------------------------------------
		drawRectangleResize(g); // ggf. von der abgeleiteten Methode

		// ----------------------------------------------------
		// Anfasser zeichnen (nur wenn hasResized() auf true)
		// ----------------------------------------------------
		if (!(resizeOrMove())) return; // resize und move nicht zulaessig     

		recNW = null;
		recNE = null;
		recSW = null;
		recSE = null;
		recN = null;
		recE = null;
		recS = null;
		recW = null;

		// ----------------------------------------------------
		// Alle Anfasser an den Ecken zeichnen
		// ----------------------------------------------------

		if (resizeDiagonal()) {
			// links oben (NW)
			recNW = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.NW_RESIZE_CURSOR), g2d);
			// rechts oben (NE)
			recNE = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.NE_RESIZE_CURSOR), g2d);
			// links unten (SW)
			recSW = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.SW_RESIZE_CURSOR), g2d);
			// rechts unten (SE)
			recSE = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.SE_RESIZE_CURSOR), g2d);
		}
		// ----------------------------------------------------
		// Alle Anfasser an den Seiten zeichnen
		// ----------------------------------------------------
		if (resizeVertiHorizontal()) {
			// oben 
			recN = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.N_RESIZE_CURSOR), g2d);
			// rechts 
			recE = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.E_RESIZE_CURSOR), g2d);
			// unten
			recS = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.S_RESIZE_CURSOR), g2d);
			// links
			recW = getAndDrawAnfasser(getPointAnfasser(rectCut, Cursor.W_RESIZE_CURSOR), g2d);
		}

	}

	// =====================================================
	// getPointAnfasser()
	// =====================================================   
	private Point2D getPointAnfasser(Rectangle2D rec, int cursor) {
		switch (cursor) {
			// -----------------  Ecken ---------------------
			case Cursor.NW_RESIZE_CURSOR: // links oben
				return new Point2D.Double(rec.getX(), rec.getY());
			case Cursor.NE_RESIZE_CURSOR: // rechts oben
				return new Point2D.Double(rec.getX() + rec.getWidth(), rec.getY());
			case Cursor.SE_RESIZE_CURSOR: // rechts unten
				return new Point2D.Double(rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight());
			case Cursor.SW_RESIZE_CURSOR: // links unten
				return new Point2D.Double(rec.getX(), rec.getY() + rec.getHeight());
			// ------------------  Seiten ---------------------
			case Cursor.N_RESIZE_CURSOR: //   oben	
				return (new Point2D.Double(rec.getX() + (rec.getWidth() / 2), rec.getY()));
			case Cursor.E_RESIZE_CURSOR: //   rechts	
				return new Point2D.Double(rec.getX() + rec.getWidth(), rec.getY() + (rec.getHeight() / 2));
			case Cursor.S_RESIZE_CURSOR: //   unten				 
				return new Point2D.Double(rec.getX() + (rec.getWidth() / 2), rec.getY() + rec.getHeight());
			case Cursor.W_RESIZE_CURSOR: //   links		
				return new Point2D.Double(rec.getX(), rec.getY() + (rec.getHeight() / 2));
		}
		return new Point();
	}

	// =====================================================
	// getTranslationPoint()
	//
	// Dieser Punkt wird nach dem moven/resizen in den Nullpunkt
	// veschoben um anschliessend zu scalen und wieder zurück zum
	// Ausgangspunkt.
	// i.d.R. ist es hier der gegenüberligende Punkt.
	//
	// cursor ist der Anfasser.
	// =====================================================   
	private Point2D getTranslationPoint(Rectangle2D rec, int cursor) {
		switch (cursor) {
			//			case Cursor.MOVE_CURSOR: // Move
			//				return getPointAnfasser(rec, Cursor.NW_RESIZE_CURSOR);
			// ------------- Ecken -------------------
			case Cursor.NW_RESIZE_CURSOR: // links oben
				return getPointAnfasser(rec, Cursor.SE_RESIZE_CURSOR);
			case Cursor.NE_RESIZE_CURSOR: // rechts oben
				return getPointAnfasser(rec, Cursor.SW_RESIZE_CURSOR);
			case Cursor.SE_RESIZE_CURSOR: // rechts unten
				return getPointAnfasser(rec, Cursor.NW_RESIZE_CURSOR);
			case Cursor.SW_RESIZE_CURSOR: // links unten
				return getPointAnfasser(rec, Cursor.NE_RESIZE_CURSOR);
			// ------------- Seiten ------------------
			case Cursor.N_RESIZE_CURSOR: //   oben	
				return getPointAnfasser(rec, Cursor.S_RESIZE_CURSOR);
			case Cursor.E_RESIZE_CURSOR: //   rechts	
				return getPointAnfasser(rec, Cursor.W_RESIZE_CURSOR);
			case Cursor.S_RESIZE_CURSOR: //   unten				 
				return getPointAnfasser(rec, Cursor.N_RESIZE_CURSOR);
			case Cursor.W_RESIZE_CURSOR: //   links		
				return getPointAnfasser(rec, Cursor.E_RESIZE_CURSOR);
		}
		return new Point();
	}

	// =====================================================
	// getAndDrawAnfasser()
	// =====================================================   
	private Rectangle2D getAndDrawAnfasser(Point2D point, Graphics2D g2d) {
		Rectangle2D rec = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
		int anfasserBreite = 4;
		rec = PM_UtilsGrafik.grow(rec, new Point2D.Double(anfasserBreite, anfasserBreite));
		//rec.grow(anfasserBreite, anfasserBreite);
		g2d.fill(rec);
		g2d.draw(rec);
		//	g2d.fillRect(rec.getX(), rec.y, rec.width, rec.height);
		//	g2d.drawRect(rec.x, rec.y, rec.width, rec.height);
		return rec;
	}

	 
	// =====================================================
	// Interface MouseListener
	//
	// 	mouseClicked(MouseEvent e)
	// 	    Invoked when the mouse button has been clicked (pressed and released) on a component.
	// 	mouseEntered(MouseEvent e)
	// 	    Invoked when the mouse enters a component.
	// 	mouseExited(MouseEvent e)
	// 	   Invoked when the mouse exits a component.
	// 	mousePressed(MouseEvent e)
	// 	   Invoked when a mouse button has been pressed on a component.
	// 	mouseReleased(MouseEvent e)
	//     Invoked when a mouse button has been released on a component.
	// =====================================================   
	// =====================================================
	// mousePressed
	//
	// Ein neuer Vorgang (Move/Resize) wird eingeleitet.
	// =====================================================       
	public void mousePressed(MouseEvent e) {
		
		if (!(resizeOrMove())) return; // resize und move nicht zulaessig      

		// cursor Point holen und sichern
		Point cPoint = getCursorPoint(e);
		pointPressed.setLocation(cPoint);
		// Neuer Vorgang: alle Werte holen    
		rectangleResize = getRectangleResize();	 		

		// Cursor sichern und neu setzen   
		cursor = getCursor();
		if (recNW != null && recNW.contains(cPoint)) {
			setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
			return;
		}
		if (recNE != null && recNE.contains(cPoint)) {
			setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
			return;
		}
		if (recSW != null && recSW.contains(cPoint)) {
			setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
			return;
		}
		if (recSE != null && recSE.contains(cPoint)) {
			setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
			return;
		}
		if (recN != null && recN.contains(cPoint)) {
			setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			return;
		}
		if (recE != null && recE.contains(cPoint)) {
			setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			return;
		}
		if (recS != null && recS.contains(cPoint)) {
			setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
			return;
		}
		if (recW != null && recW.contains(cPoint)) {
			setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
			return;
		}

		// Keine Ecke angeklickt. Kann nur Move werden
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
	}

	// =====================================================
	// mouseReleased
	// =====================================================  
	public void mouseReleased(MouseEvent e) {
		if (!(resizeOrMove())) return; // resize und move nicht zulaessig            
		setCursor(cursor);
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	// =====================================================  
	// =====================================================
	// Interface MouseMotionListener
	//
	// 	mouseDragged(MouseEvent e)
	//        Invoked when a mouse button is pressed on a component and then dragged.
	// 	mouseMoved(MouseEvent e)
	//        Invoked when the mouse button has been moved on a component (with no buttons down).
	// =====================================================   
	// =====================================================

	public void mouseMoved(MouseEvent e) {}

	// =====================================================
	// mouseDragged
	//
	// Wird nach mousePressed aufgerufen.
	//
	// "pointPressed" ist die Position als die Mousetaste gedrueckt wurde oder
	// zuletzt dies Methode (mouseDragged) aufgerufen wurde.
	// =====================================================   
	public void mouseDragged(MouseEvent e) {

		if (!(resizeOrMove())) return; // resize und move nicht zulaessig      

		int cursorType = getCursor().getType();

		// ----------------------------------------------------------------------
		//   Alte Position: pointPressedOld
		//   Neue Position: pointPressed
		// ----------------------------------------------------------------------
		Point pointPressedOld = new Point(pointPressed);
		pointPressed.setLocation(getCursorPoint(e));

		// ----------------------------------------------------------
		// AffineTransform ermitteln
		// ------------------------------------------------------------	
		AffineTransform at = new AffineTransform();
		Point2D scaling = null;
		Point2D tP = getTranslationPoint(rectangleResize, cursorType);
		if (cursorType == Cursor.MOVE_CURSOR) {
			// ----- Move ----------
			double x = pointPressed.getX() - pointPressedOld.getX();
			double y = pointPressed.getY() - pointPressedOld.getY();
			at.translate(x, y);
		} else {
			// ---- An einer Ecke oder Seite wurde gezogen ----
			scaling = getScaling(rectangleResize, tP, pointPressed, cursorType);
			at.translate(tP.getX(), tP.getY());
			at.scale(scaling.getX(), scaling.getY());
			at.translate(-tP.getX(), -tP.getY());
		}
		// ---------------------------------------------------
		// rectangleResize neu ermitteln und zeichnen
		// ----------------------------------------------------
		rectangleResize = at.createTransformedShape(rectangleResize).getBounds2D();
		setRectangleResized(rectangleResize, at);
		repaint();
	}

	// =====================================================
	// getScaling()
	//
	//  cursorType vom Anfasser.
	// =====================================================   
	private Point2D getScaling(Rectangle2D rectangleResize, Point2D translationPoint, Point2D pointPressed,
			int cursorType) {
		double distX = pointPressed.getX() - translationPoint.getX();
		double distY = pointPressed.getY() - translationPoint.getY();
		double scX = 1;
		double scY = 1;
		Point2D ret = new Point2D.Double(1, 1);
		switch (cursorType) {
			// -----------------------------------------------
			// Ecken
			// -----------------------------------------------
			case Cursor.NW_RESIZE_CURSOR: // links oben
			case Cursor.NE_RESIZE_CURSOR: // rechts oben
				distY = Math.abs(distY);
				if (distY < 20) distY = 20;
				scY = distY / rectangleResize.getHeight();
				return new Point2D.Double(scY, scY);
			case Cursor.SE_RESIZE_CURSOR: // rechts unten
			case Cursor.SW_RESIZE_CURSOR: // links unten
				distY = Math.abs(distY);
				if (distY < 20) distY = 20;
				scY = distY / rectangleResize.getHeight();
				return new Point2D.Double(scY, scY);
			// -----------------------------------------------
			// Seiten
			// -----------------------------------------------
			case Cursor.N_RESIZE_CURSOR: //   oben	
				distY = Math.abs(distY);
				if (distY < 20) distY = 20;
				scY = distY / rectangleResize.getHeight();
				return new Point2D.Double(1, scY);
			case Cursor.E_RESIZE_CURSOR: //   rechts	
				if (distX < 20) distX = 20;
				scX = distX / rectangleResize.getWidth();
				return new Point2D.Double(scX, 1);
			case Cursor.S_RESIZE_CURSOR: //   unten				 
				if (distY < 20) distY = 20;
				scY = distY / rectangleResize.getHeight();
				return new Point2D.Double(1, scY);
			case Cursor.W_RESIZE_CURSOR: //   links	
				distX = Math.abs(distX);
				if (distX < 20) distX = 20;
				scX = distX / rectangleResize.getWidth();
				return new Point2D.Double(scX, 1);
			default:
				return ret;
		}
	}

	// =====================================================
	// resizeOrMove()
	//
	// �berhaupt resizen oder moven
	// =====================================================   
	private boolean resizeOrMove() {
		return resizeDiagonal() || resizeVertiHorizontal() || move();
	}

	// =====================================================
	// getCursorPoint()
	//
	// Der MouseEvent bezieht sich immer auf den Origin, d.h. (0,0).
	// Die gezeichneten Rechtecke jedoch auf das jeweilige JPanel.
	// Daher hier den MousePoint korrigieren.
	// =====================================================   
	private Point getCursorPoint(MouseEvent e) {

		//	System.out.println("..... hier x und y = " + getX() + "," + getY());

		return new Point(e.getX() - correct.x, e.getY() - correct.y);
	}

} // Ende Klasse