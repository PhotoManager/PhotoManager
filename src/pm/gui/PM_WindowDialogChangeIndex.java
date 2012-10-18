package pm.gui;

 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
 
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
 

 
import pm.search.PM_IndicesComboBox;
import pm.search.PM_LuceneLists;
import pm.utilities.PM_MSG;
import pm.utilities.PM_Utils;
import pm.utilities.PM_Interface.*;

public class PM_WindowDialogChangeIndex {

	private JDialog dialog;
	private IndexType indexType;
	
	public PM_WindowDialogChangeIndex(IndexType indexType) {
		this.indexType = indexType;
	    dialog = getDialog();
		int w = 300;
		int h = 200;
		Dimension screen = PM_Utils.getScreenSize();
		int x = screen.width / 2 - w / 2;
		int y = screen.height / 2 - h / 2;
		// dialog.setSize(w, h);
		dialog.setLocation(x, y);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	private JTextField neu;
	private JComboBox cb;
	private List<String> data;
	private Vector<String> vector;
	private JDialog getDialog( ) {
		
		
		JPanel headerPanel = new JPanel();
		JLabel headerLabel = new JLabel(indexType.toString());
		Font font = headerLabel.getFont();
		Font fontBold = new Font(font.getName(), Font.BOLD, 20);
		headerLabel.setFont(fontBold);	
		headerPanel.add(headerLabel, BorderLayout.CENTER);
		
		
		JPanel header = new JPanel();
		header.setLayout(new FlowLayout(FlowLayout.LEFT));
		header.add(new JLabel("Name alt"));
		if (indexType == IndexType.INDEX_2) {			 
			data = PM_LuceneLists.getInstance().getIndex_2();
		} else {
			data = PM_LuceneLists.getInstance().getIndex_1();
		}
		vector = new Vector<String>(data);
		vector.add(0, " ");
		cb = new JComboBox(vector);
		
		ActionListener al  = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				neu.setText(cb.getSelectedItem().toString());
			}
		};
		cb.addActionListener(al );
		
		
		header.add(cb);
		header.add(new JLabel("Name neu"));
		neu = new JTextField();
		neu.setColumns(15);
		header.add(neu);
		
		// buttons
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JButton buttonChange = new JButton("Change Name");
		buttons.add(buttonChange);
		ActionListener alChange = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				changeName(cb.getSelectedItem().toString().trim(), neu.getText().trim()); 
			}
		};
		buttonChange.addActionListener(alChange);
		
		 
	
		JButton buttonBreak = new JButton("Abbrechen");
		buttons.add(buttonBreak);
		ActionListener alBreak = new ActionListener() {
			public void actionPerformed(ActionEvent e) {			 
				dialog.dispose(); 
			}
		};
		buttonBreak.addActionListener(alBreak);
		
		// both lines together
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.setAlignmentY(0);
		all.add(headerPanel);
		all.add(header);
		all.add(buttons);
		
		
		JDialog dialog = new JDialog(PM_WindowMain.getInstance(), true);
 		dialog.setUndecorated(false);
		dialog.add(all);
	 
		 		
		return dialog;
	}
	
	
	private void changeName(String strOld, String strNew) {
		String message = PM_LuceneLists.getInstance().changeIndexName(indexType,   strOld,   strNew , vector);
		
		if (vector.contains(strNew)) {
			cb.setSelectedIndex(0);
		}
		
		JOptionPane.showConfirmDialog(null, message,
				 null, JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		
		
	}
	
	
}
