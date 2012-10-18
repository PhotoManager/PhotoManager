package pm.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class PM_GlassPaneDragAndDrop extends JPanel implements MouseMotionListener , MouseListener{
	private AlphaComposite composite;
    private BufferedImage dragged = null;
    private Point location = new Point(0, 0);

    public PM_GlassPaneDragAndDrop()
    {
        setOpaque(false);
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setImage(BufferedImage dragged)
    {
        this.dragged = dragged;
    }

    public void setPoint(Point location)
    {
        this.location = location;
    }

    public void paintComponent(Graphics g)
    {
        if (dragged == null)
            return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(composite);
        g2.drawImage(dragged,
                     (int) (location.getX() - (dragged.getWidth(this)  / 2)),
                     (int) (location.getY() - (dragged.getHeight(this) / 2)),
                     null);
    }

	 
	public void mouseDragged(MouseEvent e) {
System.out.println("PM_GlassPaneDragAndDrop: mouseDragged ");
		
	}

	 
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	 
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	 
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	 
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	 
	public void mousePressed(MouseEvent e) {
		System.out.println("PM_GlassPaneDragAndDrop: mousePressed ");
		
	}

	 
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
