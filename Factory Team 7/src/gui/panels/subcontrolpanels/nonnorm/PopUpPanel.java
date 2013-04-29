package gui.panels.subcontrolpanels.nonnorm;

import gui.panels.subcontrolpanels.NonNormPanel;
import gui.panels.FactoryPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import transducer.TChannel;
import transducer.TEvent;

public class PopUpPanel extends JPanel implements ActionListener {
	NonNormPanel parent;
	JButton backButton;
	
	List<JLabel> popUpButtons;
	List<JLabel> workStationButtons;
	List<JCheckBox> breakGlassCheck;
	Map<String,ImageIcon> icons;
	
	public PopUpPanel(NonNormPanel p) {
		parent = p;
		setLayout(new FlowLayout());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300,200));
		add(panel,BorderLayout.CENTER);
		backButton = new JButton("Return to Non-Norm Menu");
		backButton.addActionListener(this);
		add(backButton,BorderLayout.SOUTH);

		JButton j = new JButton("popup1 jam");
		j.addActionListener(this);
		add(j, BorderLayout.PAGE_START);
		j = new JButton("popup2 jam");
		j.addActionListener(this);
		add(j, BorderLayout.PAGE_START);
//		icons = new HashMap<String,ImageIcon>();
//		icons.put("popup-good",new ImageIcon("imageicons/popup-good.png"));
//		icons.put("popup", new ImageIcon("imageicons/popup.png"));
//		icons.put("drill-good",new ImageIcon("imageicons/drill/drillImage002.png"));
//		icons.put("drill", new ImageIcon("imageicons/drill/drillImage001.png"));
//		icons.put("crossseamer-good", new ImageIcon("imageicons/crossseamer/crossSeamerImage002.png"));
//		icons.put("crossseamer",new ImageIcon("imageicons/crossseamer/crossSeamerImage001.png"));
//		icons.put("grinder-good",new ImageIcon("imageicons/grinder/grinderImage002.png"));
//		icons.put("grinder", new ImageIcon("imageicons/grinder/grinderImage027.png"));
//		popUpButtons = new ArrayList<JLabel>();
//		workStationButtons = new ArrayList<JLabel>();
//		
//		
//		for (int i = 0; i < 3; i ++) {
//			popUpButtons.add(new JLabel(icons.get("popup-good")));
//		}
//		
//		PopUpListener popUpListener = new PopUpListener();
//		for (JLabel l: popUpButtons) {
//			l.addMouseListener(popUpListener);
//		}
//		
//
//		
//		workStationButtons.add(new JLabel(icons.get("drill-good")));
//		workStationButtons.add(new JLabel(icons.get("drill-good")));
//		workStationButtons.add(new JLabel(icons.get("crossseamer-good")));
//		workStationButtons.add(new JLabel(icons.get("crossseamer-good")));
//		workStationButtons.add(new JLabel(icons.get("grinder-good")));
//		workStationButtons.add(new JLabel(icons.get("grinder-good")));
//	
//		WorkStationListener workStationListener = new WorkStationListener();
//		for (JLabel j: workStationButtons) {
//			j.addMouseListener(workStationListener);
//		}
//		
//		breakGlassCheck = new ArrayList<JCheckBox>();
//		
//		BreakGlassListener breakGlassListener = new BreakGlassListener();
//		for (int i = 0; i < 6; i++) {
//			JCheckBox cb = new JCheckBox();
//			cb.addItemListener(breakGlassListener);
//			breakGlassCheck.add(cb);
//		}
//		panel.setLayout(new GridBagLayout());
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridy = 1;
//		c.gridx = 1;
//		c.gridwidth = 3;
//		c.gridheight = 1;
//		c.insets = new Insets(0,0,15,0); 
//		panel.add(new JLabel("Popup and Workstation"),c);
//		c.insets = new Insets(0,10,5,10);
//		c.gridy = 2;
//		c.gridwidth = 1;
//		c.gridx = 1;
//		panel.add(breakGlassCheck.get(0),c);
//		c.gridx = 2;
//		panel.add(breakGlassCheck.get(2),c);
//		c.gridx = 3;
//		panel.add(breakGlassCheck.get(4),c);
//		
//		
//		c.gridy = 3;
//		c.gridx = 1;
//		panel.add(workStationButtons.get(0),c);
//		c.gridx = 2;
//		panel.add(workStationButtons.get(2),c);
//		c.gridx = 3;
//		panel.add(workStationButtons.get(4),c);
//		
//		c.gridy = 4;
//		c.gridx = 1;
//		panel.add(popUpButtons.get(0),c);
//		c.gridx = 2;
//		panel.add(popUpButtons.get(1),c);
//		c.gridx = 3;
//		panel.add(popUpButtons.get(2),c);
//		
//		c.gridy = 5;
//		c.gridx = 1;
//		panel.add(workStationButtons.get(1),c);
//		c.gridx = 2;
//		panel.add(workStationButtons.get(3),c);
//		c.gridx = 3;
//		panel.add(workStationButtons.get(5),c);
//
//		c.gridy = 6;
//		c.gridx = 1;
//		panel.add(breakGlassCheck.get(1),c);
//		c.gridx = 2;
//		panel.add(breakGlassCheck.get(3),c);
//		c.gridx = 3;
//		panel.add(breakGlassCheck.get(5),c);
//		
//		c.gridy = 2;
//		c.gridx = 4;
//		panel.add(new JLabel("break glass"),c);
//		c.gridy = 6;
//		panel.add(new JLabel("break glass"),c);

	}
	@Override
	public void actionPerformed(ActionEvent ae) {
		// TODO Auto-generated method stub
		if (ae.getSource() == backButton) {
			 CardLayout cl = (CardLayout)(parent.cardContainer.getLayout());
		        cl.show(parent.cardContainer, "MAINPANEL");
		}
	}
	
	public class PopUpListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			
			for (int i = 0; i < popUpButtons.size(); i++) {
				JLabel j = popUpButtons.get(i);
				if (e.getSource() == j) {
					if ((ImageIcon)j.getIcon() == icons.get("popup-good")) {
						j.setIcon(icons.get("popup"));
						Integer[] args = new Integer[1];
						args[0] = i;
						parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.POPUP_JAM,args);
					}
					else {
						j.setIcon(icons.get("popup-good"));
						Integer[] args = new Integer[1];
						args[0] = i;
						parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.POPUP_UNJAM,args);

					}
				}
			}
		}
		
	}
	
	public class WorkStationListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			for (int i = 0; i < workStationButtons.size(); i++) {
				JLabel label = workStationButtons.get(i);
				if (e.getSource() == label) {
					Integer[] args = new Integer[2];
					int stationIndex = i%2;
					if (i < 2) {
						args[0] = 0;
					}
					else if (i < 4) {
						args[0] = 1;
					}
					else  {
						args[0] = 2;
					}
					args[1] = stationIndex;
					if (i < 2) {
						if ((ImageIcon)label.getIcon() == icons.get("drill")) { 
							label.setIcon(icons.get("drill-good"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_WORKING,args);
							breakGlassCheck.get(i).setEnabled(true);
						}
						else {
							label.setIcon(icons.get("drill"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_BROKEN,args);
							breakGlassCheck.get(i).setEnabled(false);
						}
					}
					else if (i < 4) {
						if ((ImageIcon)label.getIcon() == icons.get("crossseamer")) {
							label.setIcon(icons.get("crossseamer-good"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_WORKING,args);
							breakGlassCheck.get(i).setEnabled(true);
						}
						else {
							label.setIcon(icons.get("crossseamer"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_BROKEN,args);
							breakGlassCheck.get(i).setEnabled(false);
						}
					}
					else {
						if ((ImageIcon)label.getIcon() == icons.get("grinder")) {
							label.setIcon(icons.get("grinder-good"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_WORKING,args);
							breakGlassCheck.get(i).setEnabled(true);
						}
						else {
							label.setIcon(icons.get("grinder"));
							parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.WORKSTATION_BROKEN,args);
							breakGlassCheck.get(i).setEnabled(false);
						}
					}
				}
			}

		}
	}
	
	public class BreakGlassListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			for (int i = 0; i < breakGlassCheck.size();i++) {
				JCheckBox c = breakGlassCheck.get(i);
				int stationIndex = i%2;
				Integer[] args = new Integer[2];
				args[1] = stationIndex;
				if (i < 2) {
					args[0] = 0;
				}
				else if (i < 4) {
					args[0] = 1;
				}
				else  {
					args[0] = 2;
				}
				if (e.getSource() == c) {
					boolean a = c.isSelected();
				//	a = !a;
				//	breakGlassCheck.get(i).setSelected(a);
					if (a == true) {	///
						parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.WORKSTATION_BREAK_GLASS,args);
						//FactoryPanel.conveyor5.breakGlassTop();
					}
					else {
						
						parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.WORKSTATION_DONT_BREAK_GLASS,args);
						
					}
				}
			}
			
		}
		
	}
}